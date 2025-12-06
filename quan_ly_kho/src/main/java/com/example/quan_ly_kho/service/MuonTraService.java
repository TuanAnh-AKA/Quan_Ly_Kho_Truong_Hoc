package com.example.quan_ly_kho.service;

import com.example.quan_ly_kho.dto.PhieuMuonUpdateForm;
import com.example.quan_ly_kho.model.LoaiThietBi;
import com.example.quan_ly_kho.model.PhieuMuon;
import com.example.quan_ly_kho.model.PhieuMuonThietBi;
import com.example.quan_ly_kho.model.ThietBi;
import com.example.quan_ly_kho.repository.LoaiThietBiRepo;
import com.example.quan_ly_kho.repository.PhieuMuonRepo;
import com.example.quan_ly_kho.repository.PhieuMuonThietBiRepo;
import com.example.quan_ly_kho.repository.ThietBiRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.*;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // Đảm bảo tính toàn vẹn dữ liệu cho các thao tác phức tạp
public class MuonTraService {

    private final PhieuMuonRepo phieuMuonRepo;
    private final PhieuMuonThietBiRepo ctRepo;
    private final ThietBiRepo thietBiRepo;
    private final LoaiThietBiRepo loaiThietBiRepo; // Đã thêm repo mới
    private final PhieuMuonThietBiRepo phieuMuonThietBiRepo; // Đã thêm repo mới

    // --- 1. TRUY VẤN DỮ LIỆU CHO VIEW (READ) ---

    public List<PhieuMuon> findAllPhieuMuon() {
        // Lấy tất cả phiếu mượn. (Lưu ý: Có thể cần phân trang hoặc sắp xếp trong thực tế)
        return phieuMuonRepo.findAll();
    }

    public List<ThietBi> dsThietBiRanh() {
        // Lấy danh sách Thiết Bị có tồn kho > 0
        return thietBiRepo.findBySoLuongGreaterThan(0);
    }

    public List<LoaiThietBi> findAllLoaiThietBi() {
        // Cung cấp danh sách loại thiết bị cho bộ lọc
        return loaiThietBiRepo.findAll();
    }


// ---------------------------------------------------------------------------------------------------
@Transactional
public PhieuMuon taoPhieuMuon(Map<Integer, Integer> thietBiMuon,
                              String maPhieu,
                              LocalDate ngayMuon,
                              String nguoiMuonText) {

    if (phieuMuonRepo.existsByMaPhieu(maPhieu)) {
        throw new RuntimeException("Mã phiếu '" + maPhieu + "' đã tồn tại.");
    }

    if (thietBiMuon.isEmpty() ||
            thietBiMuon.values().stream().allMatch(qty -> qty == null || qty <= 0)) {
        throw new RuntimeException("Phải có ít nhất 1 thiết bị được chọn với số lượng > 0.");
    }

    // --- Tạo phiếu mượn chính ---
    PhieuMuon pm = new PhieuMuon();
    pm.setMaPhieu(maPhieu);
    pm.setNgayMuon(ngayMuon);
    pm.setNguoiMuonText(nguoiMuonText);
    pm.setTrangThai(true); // true = Đang mượn
    PhieuMuon savedPm = phieuMuonRepo.save(pm);

    // --- Duyệt danh sách thiết bị được mượn ---
    for (Map.Entry<Integer, Integer> entry : thietBiMuon.entrySet()) {
        Integer thietBiId = entry.getKey();
        Integer soLuongMuon = entry.getValue();

        if (soLuongMuon == null || soLuongMuon <= 0) continue;

        ThietBi tb = thietBiRepo.findById(thietBiId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị ID: " + thietBiId));

        if (soLuongMuon > tb.getSoLuong()) {
            throw new RuntimeException("Không đủ tồn kho (" + tb.getSoLuong() + ") để mượn " + soLuongMuon + " chiếc " + tb.getTenThietBi());
        }

        // Cập nhật tồn kho
        tb.setSoLuong(tb.getSoLuong() - soLuongMuon);
        tb.setDaMuon(tb.getSoLuong() == 0);
        thietBiRepo.save(tb);

        // Tạo chi tiết phiếu
        PhieuMuonThietBi ct = new PhieuMuonThietBi();
        ct.setPhieuMuon(savedPm);
        ct.setThietBi(tb);
        ct.setSoLuongMuon(soLuongMuon);
        ct.setTrangThai("Đang mượn");
        ctRepo.save(ct);
    }

    return savedPm;
}

// ---------------------------------------------------------------------------------------------------

    // --- 3. LOGIC TRẢ PHIẾU (UPDATE) ---

    /**
     * Trả toàn bộ thiết bị của một Phiếu Mượn và cập nhật tồn kho.
     * @param phieuId ID của Phiếu Mượn cần trả
     */
    public void traHetPhieu(Integer phieuId) {
        PhieuMuon pm = phieuMuonRepo.findById(phieuId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn ID: " + phieuId));

        if (Boolean.FALSE.equals(pm.getTrangThai())) {
            throw new RuntimeException("Phiếu mượn này đã được trả hết trước đó.");
        }

        // Lấy tất cả chi tiết đang mượn của phiếu này
        List<PhieuMuonThietBi> listChiTiet = ctRepo.findByPhieuMuon(pm);

        for (PhieuMuonThietBi ct : listChiTiet) {
            // Chỉ xử lý các mục chưa được đánh dấu là đã trả (ngayTra == null)
            if (ct.getNgayTra() == null) {

                // 1. HOÀN TRẢ SỐ LƯỢNG VÀO KHO
                ThietBi tb = ct.getThietBi();
                tb.setSoLuong(tb.getSoLuong() + ct.getSoLuongMuon());

                // Cập nhật cờ hiệu daMuon về false nếu có tồn kho > 0
                tb.setDaMuon(tb.getSoLuong() == 0);
                thietBiRepo.save(tb);

                // 2. CẬP NHẬT CHI TIẾT PHIẾU
                ct.setNgayTra(LocalDate.now());
                ct.setTrangThai("Đã trả");
                ctRepo.save(ct);
            }
        }

        // 3. CẬP NHẬT TRẠNG THÁI PHIẾU MƯỢN
        pm.setTrangThai(false); // ✅ Đã trả hết
        phieuMuonRepo.save(pm);
    }

// ---------------------------------------------------------------------------------------------------

    // --- 4. LOGIC XÓA PHIẾU (DELETE) ---

    /**
     * Xóa Phiếu Mượn và Hoàn trả Tồn kho cho những thiết bị CHƯA TRẢ.
     * @param phieuId ID của Phiếu Mượn cần xóa
     */
    public void xoaPhieuMuon(Integer phieuId) {
        PhieuMuon pm = phieuMuonRepo.findById(phieuId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn ID: " + phieuId));

        // Hoàn trả số lượng về kho (Rollback)
        List<PhieuMuonThietBi> listChiTiet = ctRepo.findByPhieuMuon(pm);

        for (PhieuMuonThietBi ct : listChiTiet) {
            // Chỉ hoàn trả những mục đang mượn (để tránh hoàn trả 2 lần nếu đã trả rồi)
            if (ct.getNgayTra() == null) {
                ThietBi tb = ct.getThietBi();
                tb.setSoLuong(tb.getSoLuong() + ct.getSoLuongMuon());

                // Cập nhật cờ hiệu (daMuon)
                tb.setDaMuon(tb.getSoLuong() == 0);
                thietBiRepo.save(tb);
            }
        }

        // Xóa phiếu mượn. (Nhờ CascadeType.ALL, chi tiết cũng bị xóa)
        phieuMuonRepo.delete(pm);
    }
    /**
     * Lấy Phiếu Mượn theo ID (Hỗ trợ Controller)
     */
    public Optional<PhieuMuon> findPhieuMuonById(Integer id) {
        return phieuMuonRepo.findById(id);
    }

    /**
     * Lấy danh sách ID thiết bị đang được mượn và chưa trả (Hỗ trợ Controller load form)
     */
    public Map<Integer, Integer> findThietBiIdsByPhieuId(Integer phieuId) {
        Map<Integer, Integer> map = new HashMap<>();
        List<PhieuMuonThietBi> ds = phieuMuonThietBiRepo.findByPhieuMuon_Id(phieuId);
        for (PhieuMuonThietBi tbm : ds) {
            map.put(tbm.getThietBi().getId(), tbm.getSoLuongMuon());
        }
        return map;
    }


    // --- LOGIC CẬP NHẬT PHIẾU MƯỢN ---

    /**
     * Cập nhật Phiếu Mượn (Thông tin cơ bản và danh sách thiết bị mượn)
     * Đây là logic phức tạp vì phải xử lý: GIẢM tồn kho (khi thêm) và TĂNG tồn kho (khi xóa khỏi phiếu)
     */

    @Transactional
    public PhieuMuon capNhatPhieuMuon(PhieuMuonUpdateForm form) {

        // --- 1️⃣ Validate dữ liệu cơ bản ---
        if (form.getMaPhieu() == null || form.getMaPhieu().trim().isEmpty()) {
            throw new RuntimeException("Mã phiếu không được để trống.");
        }
        if (form.getNguoiMuonText() == null || form.getNguoiMuonText().trim().isEmpty()) {
            throw new RuntimeException("Tên người mượn không được để trống.");
        }

        // --- 2️⃣ Lấy phiếu mượn cũ từ DB ---
        PhieuMuon pm = phieuMuonRepo.findById(form.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn ID: " + form.getId()));

        if (Boolean.FALSE.equals(pm.getTrangThai())) {
            throw new RuntimeException("Không thể cập nhật phiếu mượn đã hoàn tất.");
        }

        // --- 3️⃣ Kiểm tra trùng mã phiếu (nếu đổi mã) ---
        if (!pm.getMaPhieu().equals(form.getMaPhieu()) && phieuMuonRepo.existsByMaPhieu(form.getMaPhieu())) {
            throw new RuntimeException("Mã phiếu đã tồn tại.");
        }

        // --- 4️⃣ Cập nhật thông tin cơ bản ---
        pm.setMaPhieu(form.getMaPhieu());
        pm.setNgayMuon(form.getNgayMuon());
        pm.setNguoiMuonText(form.getNguoiMuonText());
        pm.setTrangThai(form.getTrangThai());

        // --- 5️⃣ Lấy danh sách chi tiết cũ (chưa trả) ---
        Map<Integer, PhieuMuonThietBi> oldMap = pm.getChiTietList().stream()
                .filter(ct -> ct.getNgayTra() == null)
                .collect(Collectors.toMap(ct -> ct.getThietBi().getId(), ct -> ct));

        // --- 6️⃣ Lấy danh sách mới từ form (ID → Số lượng) ---
        Map<Integer, Integer> newMap = form.getThietBiIds() != null ? form.getThietBiIds() : Map.of();

        // --- 7️⃣ Xử lý thiết bị bị xóa khỏi phiếu ---
        for (PhieuMuonThietBi oldCt : new ArrayList<>(pm.getChiTietList())) {
            Integer tbId = oldCt.getThietBi().getId();

            if (!newMap.containsKey(tbId) || newMap.get(tbId) == null || newMap.get(tbId) <= 0) {
                ThietBi tb = oldCt.getThietBi();
                tb.setSoLuong(tb.getSoLuong() + oldCt.getSoLuongMuon());
                tb.setDaMuon(tb.getSoLuong() == 0);
                thietBiRepo.save(tb);

                pm.getChiTietList().remove(oldCt); // orphanRemoval sẽ tự xóa trong DB
            }
        }

        // --- 8️⃣ Xử lý thêm mới hoặc cập nhật số lượng ---
        for (Map.Entry<Integer, Integer> entry : newMap.entrySet()) {
            Integer tbId = entry.getKey();
            Integer soLuongMoi = entry.getValue();

            if (soLuongMoi == null || soLuongMoi <= 0) continue;

            ThietBi tb = thietBiRepo.findById(tbId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị ID: " + tbId));

            if (oldMap.containsKey(tbId)) {
                // --- 🔁 Cập nhật số lượng ---
                PhieuMuonThietBi oldCt = oldMap.get(tbId);
                int delta = soLuongMoi - oldCt.getSoLuongMuon();

                if (delta > 0) {
                    if (tb.getSoLuong() < delta) {
                        throw new RuntimeException("Thiết bị " + tb.getTenThietBi() + " không đủ tồn kho.");
                    }
                    tb.setSoLuong(tb.getSoLuong() - delta);
                } else if (delta < 0) {
                    tb.setSoLuong(tb.getSoLuong() + Math.abs(delta));
                }

                tb.setDaMuon(tb.getSoLuong() == 0);
                thietBiRepo.save(tb);

                oldCt.setSoLuongMuon(soLuongMoi);
                oldCt.setTrangThai(pm.getTrangThai() ? "Đang mượn" : "Đã trả");

            } else {
                // --- 🆕 Thiết bị mới ---
                if (tb.getSoLuong() < soLuongMoi) {
                    throw new RuntimeException("Thiết bị " + tb.getTenThietBi() + " không đủ tồn kho.");
                }

                tb.setSoLuong(tb.getSoLuong() - soLuongMoi);
                tb.setDaMuon(tb.getSoLuong() == 0);
                thietBiRepo.save(tb);

                PhieuMuonThietBi newCt = new PhieuMuonThietBi();
                newCt.setPhieuMuon(pm);
                newCt.setThietBi(tb);
                newCt.setSoLuongMuon(soLuongMoi);
                newCt.setTrangThai("Đang mượn");

                pm.getChiTietList().add(newCt);
            }
        }

        // --- 9️⃣ Lưu lại toàn bộ phiếu (cascade tự lưu chi tiết) ---
        return phieuMuonRepo.save(pm);
    }

    public Page<PhieuMuon> findAllPhieuMuon(Pageable pageable) {
        return phieuMuonRepo.findAll(pageable);
    }
    /**
     * Lấy toàn bộ lịch sử chi tiết mượn trả, có thể lọc theo keyword và trạng thái.
     * 🚨 LƯU Ý: Hiện tại chỉ lọc theo trạng thái. Lọc theo keyword cần JPQL phức tạp hơn.
     */
    public List<PhieuMuonThietBi> findAllLichSu(String keyword, String trangThai) {

        // 1. Logic lọc theo Trạng Thái (Đang mượn, Đã trả)
        if (trangThai != null && !trangThai.isEmpty()) {
            // Giả định bạn có phương thức tìm kiếm theo trạng thái trong PhieuMuonThietBiRepo
            // Nếu không có, bạn có thể lọc sau khi lấy findAll()
            // Ví dụ: return ctRepo.findByTrangThai(trangThai);
        }

        // 2. Lấy toàn bộ danh sách chi tiết phiếu
        List<PhieuMuonThietBi> allLichSu = ctRepo.findAll();

        // 3. Xử lý Lọc bằng Java Stream (cho mục đích đơn giản hóa)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerCaseKeyword = keyword.trim().toLowerCase();
            return allLichSu.stream()
                    .filter(ct ->
                            // Lọc theo Mã phiếu
                            ct.getPhieuMuon().getMaPhieu().toLowerCase().contains(lowerCaseKeyword) ||
                                    // Lọc theo Tên thiết bị
                                    ct.getThietBi().getTenThietBi().toLowerCase().contains(lowerCaseKeyword) ||
                                    // Lọc theo Tên người mượn
                                    ct.getPhieuMuon().getNguoiMuonText().toLowerCase().contains(lowerCaseKeyword)
                    )
                    .collect(Collectors.toList());
        }

        // Nếu không có bộ lọc nào được áp dụng
        return allLichSu;
    }
    public Page<PhieuMuonThietBi> findLichSu(String keyword, String trangThai, Pageable pageable) {

        Specification<PhieuMuonThietBi> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo trạng thái (trangThai)
            if (trangThai != null && !trangThai.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("trangThai"), trangThai));
            }

            // 2. Lọc theo từ khóa (keyword)
            if (keyword != null && !keyword.isEmpty()) {
                String searchKeyword = "%" + keyword.toLowerCase() + "%";

                // Điều kiện tìm kiếm: theo Mã Phiếu HOẶC Tên Thiết Bị
                Predicate keywordPredicate = criteriaBuilder.or(
                        // Tìm theo Mã Phiếu Mượn (JOIN tới PhieuMuon)
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("phieuMuon").get("maPhieu")), searchKeyword),
                        // Tìm theo Tên Thiết Bị (JOIN tới ThietBi)
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("thietBi").get("tenThietBi")), searchKeyword)
                );
                predicates.add(keywordPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Trả về Page đã được lọc và phân trang
        return phieuMuonThietBiRepo.findAll(spec, pageable);
    }
}