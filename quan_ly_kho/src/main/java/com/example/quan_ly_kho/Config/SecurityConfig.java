package com.example.quan_ly_kho.Config;
import com.example.quan_ly_kho.service.TaiKhoanDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
private TaiKhoanDetailsService taiKhoanDetailsService; // Inject service đã tạo ở bước trước

    // 1. Định nghĩa PasswordEncoder (Đã có)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Định nghĩa SecurityFilterChain (Cấu hình bảo mật chính)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Đảm bảo Spring Security biết cách tải người dùng từ database
                .userDetailsService(taiKhoanDetailsService) // <-- Dòng quan trọng

                // Cấu hình Authorization (Quyền truy cập)
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập trang login và các file static mà không cần xác thực
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**","/forgot-password","/forgot-password/request"
                        ,"/verify-otp","/verify-otp/check","/reset-password","/reset-password/save").permitAll()
                        // Tất cả các request khác đều cần xác thực (đăng nhập)
                        .anyRequest().authenticated()
                )

                // Cấu hình Form Login
                .formLogin(form -> form
                        .loginPage("/login") // Chỉ định sử dụng URL /login của bạn
                        .loginProcessingUrl("/login") // URL mà form POST đến (mặc định)
                        .defaultSuccessUrl("/dashboard", true) // Chuyển hướng khi thành công
                        .failureUrl("/login?error") // Chuyển hướng khi thất bại (sẽ hiển thị thông báo lỗi trên trang của bạn)
                        .permitAll()
                )

                // Cấu hình Logout
                .logout(logout -> logout
                        .permitAll()
                );

        return http.build();
    }
}
