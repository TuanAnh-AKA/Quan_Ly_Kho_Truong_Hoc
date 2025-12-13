package com.example.quan_ly_kho.repository;

import com.example.quan_ly_kho.model.PhieuMuon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;

@Repository
public interface PhieuMuonRepo extends JpaRepository<PhieuMuon, Integer>, JpaSpecificationExecutor<PhieuMuon>{

    // Kiểm tra trùng mã phiếu trước khi lưu
    boolean existsByMaPhieu(String maPhieu);
    @Query("SELECT DISTINCT pm FROM PhieuMuon pm " +
            "LEFT JOIN pm.chiTietList ctm " +
            "LEFT JOIN ctm.thietBi tb " +
            "LEFT JOIN tb.loaiThietBi ltb " +
            "WHERE ( " +
            // --- ĐIỀU KIỆN LỌC TRẠNG THÁI TÙY CHỌN (IS NULL OR... ) ---
            "   :trangThaiMuon IS NULL OR pm.trangThai = :trangThaiMuon " +
            ") AND ( " +
            // --- LỌC THEO KEYWORD ---
            "   :keyword IS NULL OR :keyword = '' OR " +
            "   LOWER(pm.maPhieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "   LOWER(pm.nguoiMuonText) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            ") AND ( " +
            // ... (Giữ nguyên các điều kiện lọc ngày và loại thiết bị) ...
            "   :fromDate IS NULL OR pm.ngayMuon >= :fromDate " +
            ") AND ( " +
            "   :toDate IS NULL OR pm.ngayMuon <= :toDate " +
            ") AND ( " +
            "   :loaiId IS NULL OR ltb.id = :loaiId " +
            ")"
    )
    Page<PhieuMuon> searchPhieuMuon(
            @Param("keyword") String keyword,
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate,
            @Param("loaiId") Integer loaiId,
            @Param("trangThaiMuon") Boolean trangThaiMuon, // Re-added: Tham số lọc trạng thái
            Pageable pageable);

}
