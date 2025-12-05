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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // Đảm bảo tính toàn vẹn dữ liệu cho các thao tác phức tạp
public class MuonTraService {

    private final PhieuMuonRepo phieuMuonRepo;
    private final PhieuMuonThietBiRepo ctRepo;
    private final ThietBiRepo thietBiRepo;
    private final LoaiThietBiRepo loaiThietBiRepo; // Đã thêm repo mới

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
    public List<Integer> findThietBiIdsByPhieuId(Integer phieuId) {
        PhieuMuon pm = phieuMuonRepo.findById(phieuId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn ID: " + phieuId));

        // Trả về danh sách ID của các thiết bị đang có trong phiếu (ngayTra == null)
        return pm.getChiTietList().stream()
                .filter(ct -> ct.getNgayTra() == null) // Chỉ các mục chưa được trả
                .map(ct -> ct.getThietBi().getId())
                .collect(Collectors.toList());
    }


    // --- LOGIC CẬP NHẬT PHIẾU MƯỢN ---

    /**
     * Cập nhật Phiếu Mượn (Thông tin cơ bản và danh sách thiết bị mượn)
     * Đây là logic phức tạp vì phải xử lý: GIẢM tồn kho (khi thêm) và TĂNG tồn kho (khi xóa khỏi phiếu)
     */

    public PhieuMuon capNhatPhieuMuon(PhieuMuonUpdateForm form) {

        // 🚨 Kiểm tra dữ liệu bắt buộc (Do đã bỏ Validation)
        if (form.getMaPhieu() == null || form.getMaPhieu().trim().isEmpty()) {
            throw new RuntimeException("Mã phiếu không được để trống.");
        }
        if (form.getNguoiMuonText() == null || form.getNguoiMuonText().trim().isEmpty()) {
            throw new RuntimeException("Tên người mượn không được để trống.");
        }

        PhieuMuon oldPm = phieuMuonRepo.findById(form.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn ID: " + form.getId()));

        if (Boolean.FALSE.equals(oldPm.getTrangThai())) {
            throw new RuntimeException("Không thể cập nhật phiếu mượn đã hoàn tất.");
        }

        // 1. Cập nhật thông tin cơ bản
        if (!oldPm.getMaPhieu().equals(form.getMaPhieu()) && phieuMuonRepo.existsByMaPhieu(form.getMaPhieu())) {
            throw new RuntimeException("Mã phiếu đã tồn tại.");
        }
        oldPm.setMaPhieu(form.getMaPhieu());
        oldPm.setNgayMuon(form.getNgayMuon());
        oldPm.setNguoiMuonText(form.getNguoiMuonText());

        // Lấy Map ID -> ChiTiet cũ (chỉ các mục chưa trả)
        Map<Integer, PhieuMuonThietBi> oldDetailsMap = oldPm.getChiTietList().stream()
                .filter(ct -> ct.getNgayTra() == null)
                .collect(Collectors.toMap(ct -> ct.getThietBi().getId(), ct -> ct));

        List<Integer> newThietBiIds = form.getThietBiIds() != null ? form.getThietBiIds() : List.of();

        // 2. Xử lý XÓA BỎ (Hoàn trả tồn kho)
        for (PhieuMuonThietBi oldCt : oldDetailsMap.values()) {
            Integer oldTbId = oldCt.getThietBi().getId();
            if (!newThietBiIds.contains(oldTbId)) {
                // Thiết bị bị xóa khỏi phiếu -> Hoàn trả tồn kho (Giả định số lượng mượn là 1)
                ThietBi tb = oldCt.getThietBi();
                tb.setSoLuong(tb.getSoLuong() + oldCt.getSoLuongMuon());
                tb.setDaMuon(tb.getSoLuong() == 0);
                thietBiRepo.save(tb);

                ctRepo.delete(oldCt); // Xóa chi tiết khỏi phiếu
            }
        }

        // 3. Xử lý THÊM MỚI (Trừ tồn kho, giả định số lượng mượn là 1)
        for (Integer newTbId : newThietBiIds) {
            if (!oldDetailsMap.containsKey(newTbId)) {
                // Thiết bị mới được thêm vào phiếu
                ThietBi tb = thietBiRepo.findById(newTbId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị ID: " + newTbId));

                if (tb.getSoLuong() < 1) {
                    throw new RuntimeException("Thiết bị " + tb.getTenThietBi() + " đã hết hàng.");
                }

                // Trừ tồn kho và cập nhật cờ hiệu
                tb.setSoLuong(tb.getSoLuong() - 1);
                tb.setDaMuon(true); // Nếu còn 0 chiếc
                thietBiRepo.save(tb);

                // Tạo chi tiết mới (Giả định số lượng mượn là 1)
                PhieuMuonThietBi newCt = new PhieuMuonThietBi();
                newCt.setPhieuMuon(oldPm);
                newCt.setThietBi(tb);
                newCt.setSoLuongMuon(1);
                newCt.setTrangThai("Đang mượn");
                ctRepo.save(newCt);
            }
        }

        return phieuMuonRepo.save(oldPm);
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
}