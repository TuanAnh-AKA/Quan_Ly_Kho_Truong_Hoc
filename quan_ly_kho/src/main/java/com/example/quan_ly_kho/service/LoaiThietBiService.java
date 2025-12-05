package com.example.quan_ly_kho.service;

import com.example.quan_ly_kho.model.LoaiThietBi;
import com.example.quan_ly_kho.repository.LoaiThietBiRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional; // Cần import cho findById

@Service
public class LoaiThietBiService {

    @Autowired
    private LoaiThietBiRepo loaiThietBiRepository;

    /**
     * 1. Thêm Loại Thiết Bị (Cập nhật logic kiểm tra trùng lặp cho cả Thêm và Sửa)
     */
    public LoaiThietBi themLoaiThietBi(LoaiThietBi loai) {
        // 1. Kiểm tra tính hợp lệ
        if (loai.getTenLoai() == null || loai.getTenLoai().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại thiết bị không được để trống.");
        }

        // 2. Kiểm tra trùng lặp (Chỉ báo lỗi nếu tên đã tồn tại VÀ không phải là chính đối tượng đang được sửa)
        Optional<LoaiThietBi> existingLoai = loaiThietBiRepository.findByTenLoai(loai.getTenLoai());

        if (existingLoai.isPresent() && !existingLoai.get().getId().equals(loai.getId())) {
            throw new RuntimeException("Loại thiết bị '" + loai.getTenLoai() + "' đã tồn tại.");
        }

        // 3. Thêm/Cập nhật vào CSDL
        // JpaRepository.save() sẽ tự động thực hiện INSERT nếu ID null hoặc UPDATE nếu ID có giá trị.
        return loaiThietBiRepository.save(loai);
    }

    /**
     * 2. Lấy danh sách tất cả loại thiết bị
     */
    public List<LoaiThietBi> layTatCaLoai() {
        return loaiThietBiRepository.findAll();
    }

    // --- BỔ SUNG CÁC PHƯƠNG THỨC MỚI ---

    /**
     * 3. Tìm Loại Thiết Bị theo ID (Dùng cho chức năng Cập nhật hoặc Xem chi tiết)
     */
    public LoaiThietBi findById(Integer id) {
        // Trả về đối tượng nếu tìm thấy, ngược lại ném ngoại lệ RuntimeException
        return loaiThietBiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loại thiết bị với ID " + id + " không tồn tại."));
    }

    /**
     * 4. Xóa Loại Thiết Bị theo ID
     */
    public void deleteById(Integer id) {
        // Kiểm tra xem đối tượng có tồn tại không trước khi xóa (tùy chọn)
        if (!loaiThietBiRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy Loại Thiết Bị có ID " + id + " để xóa.");
        }

        // Cần xem xét thêm logic kiểm tra: Loại thiết bị này có đang được sử dụng bởi Thiết Bị nào không?
        // Nếu có, bạn nên ném ngoại lệ để ngăn chặn việc xóa FK đang liên kết.

        loaiThietBiRepository.deleteById(id);
    }
}