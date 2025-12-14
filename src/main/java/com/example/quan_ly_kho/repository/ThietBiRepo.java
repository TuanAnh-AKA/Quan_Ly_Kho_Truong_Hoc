package com.example.quan_ly_kho.repository;

import com.example.quan_ly_kho.model.PhieuMuon;
import com.example.quan_ly_kho.model.ThietBi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThietBiRepo extends JpaRepository<ThietBi, Integer> {

    // Được sử dụng trong MuonTraService.dsThietBiRanh()
    List<ThietBi> findBySoLuongGreaterThanAndTinhTrangTrue(Integer soLuong);
    List<ThietBi> findByTenThietBiContainingIgnoreCaseOrMaThietBiContainingIgnoreCase(String ten, String ma);
    Optional<ThietBi> findByMaThietBi(String maThietBi);
    @Query("SELECT tb FROM ThietBi tb " +
            "WHERE ( " +
            // --- LỌC THEO KEYWORD (Mã Thiết Bị HOẶC Tên Thiết Bị) ---
            "   :keyword IS NULL OR :keyword = '' OR " +
            "   LOWER(tb.maThietBi) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "   LOWER(tb.tenThietBi) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            ") AND ( " +
            // --- LỌC THEO LOẠI THIẾT BỊ (Sử dụng tên field mối quan hệ: loaiThietBi) ---
            "   :loaiId IS NULL OR tb.loaiThietBi.id = :loaiId " +
            ") AND ( " +
            // --- LỌC THEO TÌNH TRẠNG (Sử dụng tên field: tinhTrang) ---
            "   :tinhTrang IS NULL OR tb.tinhTrang = :tinhTrang " +
            ")"
    )
    Page<ThietBi> searchThietBi(
            @Param("keyword") String keyword,
            @Param("loaiId") Integer loaiId,
            @Param("tinhTrang") Boolean tinhTrang,
            Pageable pageable);
    @Query("SELECT COUNT(ctm) FROM PhieuMuonThietBi ctm " +
            "WHERE ctm.thietBi.id = :thietBiId AND (ctm.trangThai <> 'Đã trả')")
    long countActiveLoanDetails(@Param("thietBiId") Integer thietBiId);
    long countByLoaiThietBi_Id(Integer loaiId); // 🚨 HÀM MỚI BẮT BUỘC
    List<ThietBi> findByTinhTrangTrue();
}