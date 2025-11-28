package com.example.quan_ly_kho.repository;

import com.example.quan_ly_kho.model.DatLaiMatKhauOTP;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DatLaiMatKhauOTPRepository extends JpaRepository<DatLaiMatKhauOTP, Integer> {

    // Tìm OTP hợp lệ (cho tài khoản, chưa hết hạn, chưa dùng)
    @Query("SELECT o FROM DatLaiMatKhauOTP o WHERE o.taiKhoan.id = :accountId AND o.maOtp = :otp AND o.thoiGianHetHan > :currentTime AND o.daSuDung = false")
    Optional<DatLaiMatKhauOTP> findValidOtp(
            @Param("accountId") Integer accountId,
            @Param("otp") String otp,
            @Param("currentTime") LocalDateTime currentTime
    );

    // Đánh dấu OTP là đã sử dụng
    @Transactional
    @Modifying
    @Query("UPDATE DatLaiMatKhauOTP o SET o.daSuDung = true WHERE o.taiKhoan.id = :accountId AND o.maOtp = :otp")
    void disableUsedOtp(
            @Param("accountId") Integer accountId,
            @Param("otp") String otp
    );
}
