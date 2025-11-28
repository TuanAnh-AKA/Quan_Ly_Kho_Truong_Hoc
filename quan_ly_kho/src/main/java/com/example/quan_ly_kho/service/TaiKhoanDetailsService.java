package com.example.quan_ly_kho.service;

import com.example.quan_ly_kho.model.TaiKhoan;
import com.example.quan_ly_kho.repository.TaiKhoanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class TaiKhoanDetailsService implements UserDetailsService {
    @Autowired
    private TaiKhoanRepo taiKhoanRepository;

    // Phương thức bắt buộc phải triển khai
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm kiếm Tài Khoản trong Database
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại: " + username));

        // 2. Trả về đối tượng UserDetails mà Spring Security mong muốn
        return new User(
                taiKhoan.getTenDangNhap(),
                taiKhoan.getMatKhauHash(), // Mật khẩu đã mã hóa (BCrypt hash)
                Collections.emptyList() // Danh sách quyền/vai trò (Authorities/Roles)

        );
    }
}
