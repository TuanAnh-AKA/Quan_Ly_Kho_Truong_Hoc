package com.example.quan_ly_kho.service;

import com.example.quan_ly_kho.model.DatLaiMatKhauOTP;
import com.example.quan_ly_kho.model.TaiKhoan;
import com.example.quan_ly_kho.repository.DatLaiMatKhauOTPRepository;
import com.example.quan_ly_kho.repository.TaiKhoanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class ForgotPasswordService {
    @Autowired
    private TaiKhoanRepo taiKhoanRepository;

    @Autowired
    private DatLaiMatKhauOTPRepository otpRepository;

    @Autowired
    private SendGridEmailService emailService;


    @Autowired
    private PasswordEncoder passwordEncoder; // <-- Dòng cần thêm

    private final Random random = new Random();

    /**
     * Bước 1: Yêu cầu đặt lại mật khẩu và gửi OTP
     */
    public boolean requestPasswordReset(String email) {
        TaiKhoan taiKhoan = taiKhoanRepository.findByEmail(email).orElse(null);

        if (taiKhoan == null) {
            return false; // Email không tồn tại
        }

        // 1. Tạo OTP ngẫu nhiên (6 chữ số)
        String otp = String.format("%06d", random.nextInt(999999));

        // 2. Lưu OTP vào DB
        DatLaiMatKhauOTP resetToken = new DatLaiMatKhauOTP();
        resetToken.setTaiKhoan(taiKhoan);
        resetToken.setMaOtp(otp);
        resetToken.setThoiGianTao(LocalDateTime.now());
        resetToken.setThoiGianHetHan(LocalDateTime.now().plusMinutes(5)); // OTP hết hạn sau 5 phút
        otpRepository.save(resetToken);

        // 3. Gửi Email
        sendOtpEmail(email, otp);
        return true;
    }

    private void sendOtpEmail(String toEmail, String otp) {
        try {
            emailService.sendOtpEmail(toEmail, otp);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi OTP đến " + toEmail + ": " + e.getMessage());
        }
    }


    /**
     * Bước 2: Xác thực Mã OTP
     */
    public boolean validateOtp(String email, String otp) {
        TaiKhoan taiKhoan = taiKhoanRepository.findByEmail(email).orElse(null);
        if (taiKhoan == null) return false;

        // Tìm OTP hợp lệ (chưa sử dụng, chưa hết hạn, cho đúng tài khoản)
        DatLaiMatKhauOTP validOtp = otpRepository.findValidOtp(
                taiKhoan.getId(),
                otp,
                LocalDateTime.now()
        ).orElse(null);

        return validOtp != null;
    }

    /**
     * Bước 3: Đặt lại Mật khẩu
     */
    public boolean resetPassword(String email, String otp, String newPassword) {
        if (validateOtp(email, otp)) {
            TaiKhoan taiKhoan = taiKhoanRepository.findByEmail(email).get(); // Lấy tài khoản lần nữa

            // Cập nhật mật khẩu mới (phải được hash!)
            taiKhoan.setMatKhauHash(passwordEncoder.encode(newPassword)); // Cần inject PasswordEncoder
            taiKhoanRepository.save(taiKhoan);

            // Vô hiệu hóa OTP đã sử dụng
            otpRepository.disableUsedOtp(taiKhoan.getId(), otp);
            return true;
        }
        return false;
    }
}
