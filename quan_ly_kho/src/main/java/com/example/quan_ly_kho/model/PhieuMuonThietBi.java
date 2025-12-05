package com.example.quan_ly_kho.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "PhieuMuon_ThietBi")
@Data
public class PhieuMuonThietBi {

    @EmbeddedId
    private PhieuMuonThietBiId id = new PhieuMuonThietBiId(); // ✅ tránh null

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("phieuMuonId")
    @JoinColumn(name = "phieu_muon_id", nullable = false)
    private PhieuMuon phieuMuon;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("thietBiId")
    @JoinColumn(name = "thiet_bi_id", nullable = false)
    private ThietBi thietBi;

    @Column(name = "so_luong_muon", nullable = false)
    private Integer soLuongMuon;

    @Column(name = "ngay_tra")
    private LocalDate ngayTra;

    @Column(name = "trang_thai", length = 50)
    private String trangThai;
}
