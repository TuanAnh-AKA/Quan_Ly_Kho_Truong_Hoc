package com.example.quan_ly_kho.repository;

import com.example.quan_ly_kho.model.LoaiThietBi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoaiThietBiRepo extends JpaRepository<LoaiThietBi, Integer> {
    // Phương thức tiêu chuẩn cho phép lấy danh sách Loại Thiết Bị
    Optional<LoaiThietBi> findByTenLoai(String tenLoai);
}
