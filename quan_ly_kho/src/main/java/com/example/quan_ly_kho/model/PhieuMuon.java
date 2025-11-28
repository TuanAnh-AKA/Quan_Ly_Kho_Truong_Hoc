package com.example.quan_ly_kho.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Phieu_Muon") // Ánh xạ tới bảng Phieu_Muon trong CSDL
@Data // Lombok: Tự động tạo getter, setter, toString, equals, hashCode
public class PhieuMuon {
    @Id // Khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự tăng (IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "ma_phieu", nullable = false, unique = true, length = 50)
    private String maPhieu;

    @Column(name = "ngay_muon", nullable = false)
    private LocalDate ngayMuon;

    // 0: Đang mượn, 1: Đã trả hết. (Bit trong SQL Server ánh xạ tới Boolean/Byte trong Java)
    @Column(name = "trang_thai", nullable = false)
    private Boolean trangThai;

    @Column(name = "nguoi_muon_text", length = 100)
    private String nguoiMuonText; // Tên người mượn

    // --- Mối quan hệ với Chi tiết Phiếu Mượn ---
    // Phiếu Mượn (One) có nhiều Chi Tiết (Many)
    // mappedBy trỏ đến trường PhieuMuon trong entity PhieuMuonThietBi
    @OneToMany(mappedBy = "phieuMuon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PhieuMuonThietBi> chiTietList;

    // Phương thức tiện ích để lấy tên thiết bị hiển thị trên bảng
    // Đây là logic tạm thời, thực tế nên tối ưu hóa truy vấn
    public String getThietBiHienThi() {
        if (chiTietList == null || chiTietList.isEmpty()) {
            return "Không có thiết bị";
        }
        // Lấy tên thiết bị đầu tiên của chi tiết đầu tiên
        // Giả định PhieuMuonThietBi có phương thức getThietBi()
        return chiTietList.get(0).getThietBi().getTenThietBi();
    }
}
