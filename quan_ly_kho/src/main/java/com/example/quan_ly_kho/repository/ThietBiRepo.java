package com.example.quan_ly_kho.repository;

import com.example.quan_ly_kho.model.PhieuMuon;
import com.example.quan_ly_kho.model.ThietBi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThietBiRepo extends JpaRepository<ThietBi, Integer> {

    // Được sử dụng trong MuonTraService.dsThietBiRanh()
    List<ThietBi> findBySoLuongGreaterThan(Integer soLuong);
    List<ThietBi> findByTenThietBiContainingIgnoreCaseOrMaThietBiContainingIgnoreCase(String ten, String ma);
    Optional<ThietBi> findByMaThietBi(String maThietBi);


}