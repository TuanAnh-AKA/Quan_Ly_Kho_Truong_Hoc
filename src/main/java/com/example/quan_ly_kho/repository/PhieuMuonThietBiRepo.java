package com.example.quan_ly_kho.repository;

import com.example.quan_ly_kho.model.LoaiThietBi;
import com.example.quan_ly_kho.model.PhieuMuon;
import com.example.quan_ly_kho.model.PhieuMuonThietBi;
import com.example.quan_ly_kho.model.PhieuMuonThietBiId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



import java.util.List;

@Repository
public interface PhieuMuonThietBiRepo extends JpaRepository<PhieuMuonThietBi, PhieuMuonThietBiId> {

    // Được sử dụng trong MuonTraService.traHetPhieu() và xoaPhieuMuon()
    List<PhieuMuonThietBi> findByPhieuMuon(PhieuMuon phieuMuon);

    // Tìm kiếm các chi tiết đang mượn
    List<PhieuMuonThietBi> findByTrangThai(String trangThai);

    List<PhieuMuonThietBi> findByPhieuMuon_Id(Integer phieuId);

    Page<PhieuMuonThietBi> findAll(Specification<PhieuMuonThietBi> spec, Pageable pageable);
}