package com.example.quan_ly_kho.repository;

import com.example.quan_ly_kho.model.PhieuMuon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhieuMuonRepo extends JpaRepository<PhieuMuon, Integer> {

    // Kiểm tra trùng mã phiếu trước khi lưu
    boolean existsByMaPhieu(String maPhieu);
}
