package com.example.quan_ly_kho.service;

import com.example.quan_ly_kho.model.ThietBi;
import com.example.quan_ly_kho.model.LoaiThietBi;
import com.example.quan_ly_kho.repository.LoaiThietBiRepo;
import com.example.quan_ly_kho.repository.ThietBiRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ThietBiService {

    @Autowired
    private ThietBiRepo thietBiRepository;

    @Autowired
    private LoaiThietBiRepo loaiThietBiRepository;

    // --- CÁC PHƯƠNG THỨC CHÍNH ---

    /**
     * 1. Phương thức LƯU/CẬP NHẬT chung (Bao gồm logic kiểm tra)
     * Thao tác chính để thêm mới hoặc cập nhật thiết bị.
     */
    public ThietBi save(ThietBi thietBi) {


        // Nếu có ID => cập nhật
        if (thietBi.getId() != null) {
            ThietBi existing = thietBiRepository.findById(thietBi.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị ID: " + thietBi.getId()));

            existing.setTenThietBi(thietBi.getTenThietBi());
            existing.setSoLuong(thietBi.getSoLuong());
            existing.setMoTa(thietBi.getMoTa());
            existing.setLoaiThietBi(thietBi.getLoaiThietBi());
            existing.setNgayNhap(thietBi.getNgayNhap());
            existing.setNhaCungCap(thietBi.getNhaCungCap());


            return thietBiRepository.save(existing);
        }

        // Nếu thêm mới

        thietBi.setTinhTrang(true);
        thietBi.setDaMuon(false);
        return thietBiRepository.save(thietBi);
    }



    /**
     * 2. Phương thức tìm Thiết Bị theo ID (Dùng cho chức năng Cập nhật - GET)
     */
    public ThietBi findById(Integer id) {
        // Phương thức findById của JPA trả về Optional, cần xử lý khi không tìm thấy
        return thietBiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thiết bị với ID " + id + " không tồn tại."));
    }

    // --- PHƯƠNG THỨC HỖ TRỢ KHÁC ---

    /**
     * Lấy danh sách tất cả thiết bị (Cần cho trang danh sách)
     */
    public List<ThietBi> findAll() {
        return thietBiRepository.findAll();
    }

    /**
     * Xóa Thiết Bị theo ID
     */
    public void deleteById(Integer id) {
        // Nên kiểm tra logic nghiệp vụ: Thiết bị này có đang được mượn không?
        ThietBi thietBi = findById(id);
        if (thietBi.getDaMuon() != null && thietBi.getDaMuon()) {
            throw new RuntimeException("Không thể xóa thiết bị đang được mượn.");
        }

        thietBiRepository.deleteById(id);
    }

    public Page<ThietBi> findAllThietBi(Pageable pageable) {
        return thietBiRepository.findAll(pageable);
    }
}