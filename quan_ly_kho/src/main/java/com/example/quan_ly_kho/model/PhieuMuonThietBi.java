package com.example.quan_ly_kho.model;



import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "PhieuMuon_ThietBi")
@Data
public class PhieuMuonThietBi {

    // --- Khóa chính kép ---
    @EmbeddedId // Sử dụng lớp ID nhúng
    private PhieuMuonThietBiId id;

    // --- Mối quan hệ Many-to-One với PhieuMuon (Khóa ngoại 1) ---
    // @MapsId("phieuMuonId") cho biết trường này ánh xạ tới phieuMuonId trong @EmbeddedId
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("phieuMuonId")
    @JoinColumn(name = "phieu_muon_id")
    private PhieuMuon phieuMuon;

    // --- Mối quan hệ Many-to-One với ThietBi (Khóa ngoại 2) ---
    // @MapsId("thietBiId") cho biết trường này ánh xạ tới thietBiId trong @EmbeddedId
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("thietBiId")
    @JoinColumn(name = "thiet_bi_id")
    private ThietBi thietBi;

    // --- Các cột dữ liệu khác ---

    @Column(name = "so_luong_muon", nullable = false)
    private Integer soLuongMuon; // Số lượng thiết bị được mượn trong phiếu này

    @Column(name = "ngay_tra")
    private LocalDate ngayTra;

    @Column(name = "trang_thai", length = 50)
    private String trangThai; // Ví dụ: 'Đang mượn', 'Đã trả', 'Hư hỏng'
}
