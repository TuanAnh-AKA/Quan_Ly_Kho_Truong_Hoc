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
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import jakarta.persistence.criteria.Predicate;

import java.time.ZoneId;
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

        return thietBiRepo.findBySoLuongGreaterThanAndTinhTrangTrue(0);
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
// --- 1. KIỂM TRA BẮT BUỘC PHẢI CÓ THIẾT BỊ (ĐÃ CÓ, GIỮ NGUYÊN) ---
    if (thietBiMuon.isEmpty() ||
            thietBiMuon.values().stream().allMatch(qty -> qty == null || qty <= 0)) {
        throw new RuntimeException("Phải có ít nhất 1 thiết bị được chọn với số lượng > 0.");
    }
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

        // --- 1️⃣ Validate dữ liệu cơ bản (GIỮ NGUYÊN) ---
        if (form.getMaPhieu() == null || form.getMaPhieu().trim().isEmpty()) {
            throw new RuntimeException("Mã phiếu không được để trống.");
        }
        if (form.getNguoiMuonText() == null || form.getNguoiMuonText().trim().isEmpty()) {
            throw new RuntimeException("Tên người mượn không được để trống.");
        }

        // --- 2-4: Lấy phiếu, kiểm tra trạng thái, kiểm tra trùng mã, cập nhật thông tin cơ bản (GIỮ NGUYÊN) ---
        PhieuMuon pm = phieuMuonRepo.findById(form.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn ID: " + form.getId()));

        if (Boolean.FALSE.equals(pm.getTrangThai())) {
            throw new RuntimeException("Không thể cập nhật phiếu mượn đã hoàn tất.");
        }

        if (!pm.getMaPhieu().equals(form.getMaPhieu()) && phieuMuonRepo.existsByMaPhieu(form.getMaPhieu())) {
            throw new RuntimeException("Mã phiếu đã tồn tại.");
        }

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

        // ----------------------------------------------------------------------------------
        // ⚠️ LƯU Ý: BỎ LOGIC KIỂM TRA ĐẦU (PRE-CHECK) ĐỂ TRÁNH NHẦM LẪN. TẬP TRUNG VÀO KIỂM TRA CUỐI
        // ----------------------------------------------------------------------------------

        // --- 7️⃣ Xử lý các thiết bị cũ (Rollback tồn kho và Xóa chi tiết nếu không còn) ---
        List<PhieuMuonThietBi> listToRemove = new ArrayList<>();

        for (PhieuMuonThietBi oldCt : pm.getChiTietList()) {
            Integer tbId = oldCt.getThietBi().getId();
            Integer soLuongMoi = newMap.getOrDefault(tbId, 0);

            if (oldCt.getNgayTra() == null) { // Chỉ xử lý chi tiết đang mượn
                if (soLuongMoi <= 0) {
                    // Thiết bị bị xóa khỏi phiếu -> Hoàn trả tồn kho và đánh dấu để xóa
                    ThietBi tb = oldCt.getThietBi();
                    tb.setSoLuong(tb.getSoLuong() + oldCt.getSoLuongMuon());
                    tb.setDaMuon(tb.getSoLuong() == 0);
                    thietBiRepo.save(tb);

                    listToRemove.add(oldCt);
                }
            }
        }

        // Xóa các chi tiết đã bị loại bỏ khỏi phiếu mượn
        pm.getChiTietList().removeAll(listToRemove);

        // --- 8️⃣ Xử lý thêm mới hoặc cập nhật số lượng (GIỮ NGUYÊN) ---
        // (Phần này sẽ thêm các chi tiết mới vào pm.getChiTietList())
        for (Map.Entry<Integer, Integer> entry : newMap.entrySet()) {
            Integer tbId = entry.getKey();
            Integer soLuongMoi = entry.getValue();

            if (soLuongMoi == null || soLuongMoi <= 0) continue;

            ThietBi tb = thietBiRepo.findById(tbId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị ID: " + tbId));

            if (oldMap.containsKey(tbId) && !listToRemove.contains(oldMap.get(tbId))) {
                // Cập nhật số lượng
                PhieuMuonThietBi oldCt = oldMap.get(tbId);
                int delta = soLuongMoi - oldCt.getSoLuongMuon();

                // Logic kiểm tra và trừ/cộng tồn kho
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

            } else if (!oldMap.containsKey(tbId) || listToRemove.contains(oldMap.get(tbId))) {
                // Thiết bị mới hoặc thiết bị được thêm lại
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

        // ----------------------------------------------------------------------------------
        // ✅ KIỂM TRA BẮT BUỘC PHẢI CÓ THIẾT BỊ (KIỂM TRA CUỐI CÙNG VÀ HIỆU QUẢ NHẤT)
        // ----------------------------------------------------------------------------------
        // Sau khi đã xử lý thêm/bớt/cập nhật, kiểm tra xem còn lại chi tiết nào đang mượn không.
        boolean hasActiveItems = pm.getChiTietList().stream()
                .anyMatch(ct -> ct.getNgayTra() == null); // Chỉ cần có 1 mục đang mượn

        if (!hasActiveItems) {
            throw new RuntimeException("Cập nhật thất bại. Phiếu mượn bắt buộc phải có ít nhất một thiết bị đang mượn.");
        }

        // --- 9️⃣ Lưu lại toàn bộ phiếu (cascade tự lưu chi tiết) ---
        return phieuMuonRepo.save(pm);
    }


    /**
     * Lấy tất cả Thiết Bị (Cần cho form Cập nhật/Form chi tiết)
     */
    public List<ThietBi> findAllThietBi() {
        return thietBiRepo.findByTinhTrangTrue();
    }

    /**
     * Lấy danh sách ID Thiết bị được mượn và số lượng (Giống findThietBiIdsByPhieuId, nhưng đặt tên rõ ràng hơn cho Controller)
     */
    public Map<Integer, Integer> findThietBiMuonDetails(Integer phieuId) {
        // Sử dụng lại logic đã có:
        return findThietBiIdsByPhieuId(phieuId);
    }
    // Trong MuonTraService.java

    public Page<PhieuMuon> searchPhieuMuon(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            Integer loaiId,
            boolean trangThaiMuon, // Sử dụng tham số boolean của Controller
            Pageable pageable)
    {

        Specification<PhieuMuon> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 🚨 1. Lọc theo TRẠNG THÁI (tham số bắt buộc: true)
            // Luôn lọc theo trạng thái = true (Đang Mượn)
            predicates.add(criteriaBuilder.equal(root.get("trangThai"), trangThaiMuon));

            // 2. Lọc theo TỪ KHÓA (GIỮ NGUYÊN logic đã kiểm tra)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("maPhieu")), searchKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nguoiMuonText")), searchKeyword)
                ));
            }

            // 3. Lọc theo NGÀY MƯỢN/NGÀY TRẢ (SỬ DỤNG LOGIC AN TOÀN CỦA LỊCH SỬ)
            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ngayMuon"), fromDate));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("ngayMuon"), toDate));
            }

            // 4. Lọc theo LOẠI THIẾT BỊ (Cần Join nếu bạn không dùng @Query)
            if (loaiId != null) {
                // Cần join tới ChiTietList -> ThietBi -> LoaiThietBi
                Join<PhieuMuon, PhieuMuonThietBi> chiTietJoin = root.join("chiTietList", JoinType.INNER);
                Join<PhieuMuonThietBi, ThietBi> thietBiJoin = chiTietJoin.join("thietBi", JoinType.INNER);
                Join<ThietBi, LoaiThietBi> loaiThietBiJoin = thietBiJoin.join("loaiThietBi", JoinType.INNER);

                predicates.add(criteriaBuilder.equal(loaiThietBiJoin.get("id"), loaiId));
                query.distinct(true); // Tránh trùng lặp
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return phieuMuonRepo.findAll(spec, pageable);
    }
    public Page<PhieuMuonThietBi> findLichSu(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            Integer loaiId, // Không được dùng trong HTML hiện tại, nhưng giữ để đồng bộ
            String trangThai,
            Pageable pageable) {

        // Đây là nơi bạn sẽ gọi Specification đã viết trước đó
        // (Vì bạn chưa cung cấp code Specification cuối cùng, tôi sẽ để lại đây như một placeholder)

        // Ví dụ: return phieuMuonThietBiRepo.findAll(spec, pageable);

        // TẠM THỜI: Để tránh lỗi biên dịch, ta sẽ giả định gọi một hàm cơ bản
        // Bạn cần đảm bảo logic lọc trạng thái (Đang mượn, Đã trả, Hư hỏng) được áp dụng tại đây.

        return phieuMuonThietBiRepo.findAll(pageable);
    }
    public Page<PhieuMuon> searchPhieuMuonHistory(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            String trangThaiFilter, // 🚨 Tham số mới để lọc trạng thái
            Pageable pageable)
    {

        Specification<PhieuMuon> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo TỪ KHÓA (GIỮ NGUYÊN)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("maPhieu")), searchKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nguoiMuonText")), searchKeyword)
                ));
            }

            // 2. Lọc theo NGÀY MƯỢN (GIỮ NGUYÊN)
            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ngayMuon"), fromDate));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("ngayMuon"), toDate));
            }

            // 🚨 3. LỌC THEO TRẠNG THÁI (MỚI)
            if (trangThaiFilter != null && !trangThaiFilter.isEmpty()) {
                if ("DANG_MUON".equals(trangThaiFilter)) {
                    // Lọc trạng thái = true (Đang Mượn)
                    predicates.add(criteriaBuilder.equal(root.get("trangThai"), true));
                } else if ("DA_TRA".equals(trangThaiFilter)) {
                    // Lọc trạng thái = false (Đã Trả)
                    predicates.add(criteriaBuilder.equal(root.get("trangThai"), false));
                }
            }
            // Nếu trangThaiFilter là null hoặc trống, ta sẽ lấy tất cả (Lịch sử)

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return phieuMuonRepo.findAll(spec, pageable);
    }
    public List<PhieuMuon> getFilteredLichSuList(String keyword, LocalDate fromDate, LocalDate toDate, String trangThaiFilter) {
        // Tái sử dụng logic Specification (spec) từ searchPhieuMuonHistory
        Specification<PhieuMuon> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ... (Dán toàn bộ logic Predicate từ searchPhieuMuonHistory vào đây) ...

            // Logic lọc theo KEYWORD, DATE, TRANG THAI phải được dán vào đây
            // ...

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Trả về tất cả kết quả đã lọc dưới dạng List
        return phieuMuonRepo.findAll(spec);
    }

}