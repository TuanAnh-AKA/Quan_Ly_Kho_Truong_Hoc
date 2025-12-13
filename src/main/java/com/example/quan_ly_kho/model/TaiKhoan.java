package com.example.quan_ly_kho.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Tai_Khoan")
public class TaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_dang_nhap", unique = true, nullable = false)
    private String tenDangNhap;

    @Column(name = "mat_khau_hash", nullable = false)
    private String matKhauHash; // Mật khẩu đã mã hóa

    @Column(name = "ten_hien_thi", nullable = false)
    private String tenHienThi;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "vai_tro", nullable = false)
    private String vaiTro; // Ví dụ: Admin

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    @Column(name = "tinh_trang", nullable = false)
    private Boolean tinhTrang;
}
