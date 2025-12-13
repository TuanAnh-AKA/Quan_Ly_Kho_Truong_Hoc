package com.example.quan_ly_kho.service;

import com.example.quan_ly_kho.model.LoaiThietBi;
import com.example.quan_ly_kho.repository.LoaiThietBiRepo;
import com.example.quan_ly_kho.repository.ThietBiRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional; // Cần import cho findById

@Service
@RequiredArgsConstructor
@Transactional
public class LoaiThietBiService {
    private final LoaiThietBiRepo loaiThietBiRepo;
    private final ThietBiRepo thietBiRepo; // 🚨 Dùng để kiểm tra ràng buộc
    private final LoaiThietBiRepo loaiThietBiRepository;



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

    public List<LoaiThietBi> findAll() {
        return loaiThietBiRepo.findAll();
    }

    // --- 1. THÊM MỚI/CẬP NHẬT LOẠI THIẾT BỊ ---
    public LoaiThietBi save(LoaiThietBi loaiThietBi) {
        // Kiểm tra tên trùng lặp (khi thêm)
        if (loaiThietBi.getId() == null) {
            if (loaiThietBiRepo.existsByTenLoai(loaiThietBi.getTenLoai())) {
                throw new RuntimeException("Tên loại thiết bị đã tồn tại: " + loaiThietBi.getTenLoai());
            }
        }
        // Kiểm tra tên trùng lặp (khi sửa)
        else {
            if (loaiThietBiRepo.existsByTenLoaiAndIdNot(loaiThietBi.getTenLoai(), loaiThietBi.getId())) {
                throw new RuntimeException("Tên loại thiết bị đã tồn tại: " + loaiThietBi.getTenLoai());
            }
        }
        return loaiThietBiRepo.save(loaiThietBi);
    }

    // --- 2. XÓA LOẠI THIẾT BỊ (CÓ KIỂM TRA RÀNG BUỘC) ---
    public void delete(Integer id) {
        // 🚨 QUY TẮC BẮT BUỘC: KHÔNG XÓA NẾU ĐANG CÓ THIẾT BỊ SỬ DỤNG
        if (thietBiRepo.countByLoaiThietBi_Id(id) > 0) {
            throw new RuntimeException("Không thể xóa. Loại thiết bị này đang được sử dụng bởi ít nhất một thiết bị.");
        }

        loaiThietBiRepo.deleteById(id);
    }

    // Hàm tìm theo ID (dùng cho form sửa)
    public Optional<LoaiThietBi> findById2(Integer id) {
        return loaiThietBiRepo.findById(id);
    }
}