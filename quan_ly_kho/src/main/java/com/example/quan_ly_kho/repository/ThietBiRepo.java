package com.example.quan_ly_kho.repository;

import com.example.quan_ly_kho.model.ThietBi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThietBiRepo extends JpaRepository<ThietBi, Integer> {

    // Được sử dụng trong MuonTraService.dsThietBiRanh()
    List<ThietBi> findBySoLuongGreaterThan(Integer soLuong);

    // (Thêm các phương thức tìm kiếm khác nếu cần)
    List<ThietBi> findByTenThietBiContainingIgnoreCaseOrMaThietBiContainingIgnoreCase(String ten, String ma);
}