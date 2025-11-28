package com.example.quan_ly_kho.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Dat_Lai_Mat_Khau_OTP")
public class DatLaiMatKhauOTP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Liên kết với TaiKhoan (Tai_Khoan)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tai_khoan_id", nullable = false)
    private TaiKhoan taiKhoan; // Thay cho tai_khoan_id

    @Column(name = "ma_otp", nullable = false)
    private String maOtp;

    @Column(name = "thoi_gian_tao", nullable = false)
    private LocalDateTime thoiGianTao;

    @Column(name = "thoi_gian_het_han", nullable = false)
    private LocalDateTime thoiGianHetHan;

    @Column(name = "da_su_dung", nullable = false)
    private Boolean daSuDung = false;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }

    public String getMaOtp() { return maOtp; }
    public void setMaOtp(String maOtp) { this.maOtp = maOtp; }

    public LocalDateTime getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(LocalDateTime thoiGianTao) { this.thoiGianTao = thoiGianTao; }

    public LocalDateTime getThoiGianHetHan() { return thoiGianHetHan; }
    public void setThoiGianHetHan(LocalDateTime thoiGianHetHan) { this.thoiGianHetHan = thoiGianHetHan; }

    public Boolean getDaSuDung() { return daSuDung; }
    public void setDaSuDung(Boolean daSuDung) { this.daSuDung = daSuDung; }
}
