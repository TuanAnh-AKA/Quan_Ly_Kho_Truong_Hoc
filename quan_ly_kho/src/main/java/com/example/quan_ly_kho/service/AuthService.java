package com.example.quan_ly_kho.service;

import com.example.quan_ly_kho.model.TaiKhoan;
import com.example.quan_ly_kho.repository.TaiKhoanRepo;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Giả sử dùng Spring Security

import org.springframework.stereotype.Service;
@Service

public class AuthService {
    @Autowired
    private TaiKhoanRepo taiKhoanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Cần phải định nghĩa Bean này trong cấu hình

    /**
     * Xác thực người dùng
     * @param tenDangNhap Tên đăng nhập từ form
     * @param matKhau Mat khẩu thô từ form
     * @return TaiKhoan nếu xác thực thành công, null nếu thất bại
     */
    public TaiKhoan authenticate(String tenDangNhap, String matKhau) {
        // 1. Tìm tài khoản theo tên đăng nhập
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(tenDangNhap)
                .orElse(null);

        if (taiKhoan == null) {
            return null; // Không tìm thấy tài khoản
        }

        // 2. So sánh mật khẩu đã mã hóa (QUAN TRỌNG: KHÔNG so sánh trực tiếp)
        // Đây là nơi bạn cần thư viện mã hóa (ví dụ: BCrypt)
        if (passwordEncoder.matches(matKhau, taiKhoan.getMatKhauHash())) {
            return taiKhoan; // Đăng nhập thành công
        } else {
            return null; // Mật khẩu sai
        }
    }
}
