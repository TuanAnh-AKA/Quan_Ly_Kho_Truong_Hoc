package com.example.quan_ly_kho.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
@Entity
@Table(name = "PhieuMuon_ThietBi")
@Data
public class PhieuMuonThietBi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ✅ ID tự tăng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_muon_id", nullable = false)
    private PhieuMuon phieuMuon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thiet_bi_id", nullable = false)
    private ThietBi thietBi;

    @Column(name = "so_luong_muon", nullable = false)
    private Integer soLuongMuon;

    @Column(name = "ngay_tra")
    private LocalDate ngayTra;

    @Column(name = "trang_thai", length = 50)
    private String trangThai;
}

