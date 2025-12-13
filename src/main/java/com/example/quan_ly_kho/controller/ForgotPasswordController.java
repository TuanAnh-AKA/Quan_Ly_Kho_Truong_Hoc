package com.example.quan_ly_kho.controller;

import com.example.quan_ly_kho.model.ForgotPasswordRequest;
import com.example.quan_ly_kho.model.ResetPasswordRequest;
import com.example.quan_ly_kho.model.VerifyOtpRequest;
import com.example.quan_ly_kho.service.ForgotPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ForgotPasswordController {
    @Autowired
    private ForgotPasswordService forgotPasswordService;

    // ======================== BƯỚC 1: YÊU CẦU ĐẶT LẠI (NHẬP EMAIL) ========================

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        // Đảm bảo model có attribute 'request' để form Thymeleaf sử dụng
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new ForgotPasswordRequest());
        }
        return "forgot-password"; // Trả về forgot-password.html
    }

    @PostMapping("/forgot-password/request")
    public String handleForgotPasswordRequest(
            @ModelAttribute("request") ForgotPasswordRequest request,
            RedirectAttributes redirectAttributes) {

        if (forgotPasswordService.requestPasswordReset(request.getEmail())) {
            // Thành công: Gửi OTP thành công và chuyển sang Bước 2
            redirectAttributes.addFlashAttribute("success", "Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra.");
            redirectAttributes.addAttribute("email", request.getEmail());
            return "redirect:/verify-otp";
        } else {
            // Thất bại: Email không tồn tại
            redirectAttributes.addFlashAttribute("error", "Email không tồn tại trong hệ thống. Vui lòng kiểm tra lại.");
            return "redirect:/forgot-password";
        }
    }

    // ======================== BƯỚC 2: XÁC THỰC OTP ========================

    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@RequestParam(value = "email", required = false) String email, Model model) {
        if (email == null || email.isEmpty() || model.containsAttribute("error")) {
            // Nếu không có email (lần đầu truy cập hoặc lỗi)
            // Lấy email từ FlashAttributes nếu có lỗi trước đó
            if (model.containsAttribute("error")) {
                email = (String) model.asMap().get("email");
            }
        }

        if (email == null || email.isEmpty()) {
            // Không có email, quay lại bước 1
            return "redirect:/forgot-password";
        }

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail(email);
        model.addAttribute("request", request);
        return "verify-otp"; // Trả về verify-otp.html
    }

    @PostMapping("/verify-otp/check")
    public String handleVerifyOtp(@ModelAttribute("request") VerifyOtpRequest request, RedirectAttributes redirectAttributes) {

        if (forgotPasswordService.validateOtp(request.getEmail(), request.getOtp())) {
            // Xác thực thành công: Chuyển sang bước 3 (Reset Mật khẩu)
            redirectAttributes.addFlashAttribute("success", "Mã xác thực hợp lệ. Vui lòng đặt mật khẩu mới.");
            redirectAttributes.addAttribute("email", request.getEmail());
            redirectAttributes.addAttribute("otp", request.getOtp());
            return "redirect:/reset-password";
        } else {
            // Xác thực thất bại
            redirectAttributes.addFlashAttribute("error", "Mã OTP không hợp lệ hoặc đã hết hạn.");
            redirectAttributes.addFlashAttribute("email", request.getEmail()); // Lưu lại email để hiển thị lại form
            return "redirect:/verify-otp";
        }
    }

    // ======================== BƯỚC 3: ĐẶT LẠI MẬT KHẨU ========================

    @GetMapping("/reset-password")
    public String showResetPasswordForm(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "otp", required = false) String otp,
            Model model) {

        if (email == null || otp == null || !model.containsAttribute("request")) {
            // Nếu không có dữ liệu cần thiết, kiểm tra tính hợp lệ của OTP trước khi hiển thị form
            // (Thực tế nên kiểm tra lại OTP ở đây, nhưng ta tin tưởng luồng RedirectAttributes)
        }

        // Nếu chuyển hướng từ lỗi, model đã có request. Ngược lại, tạo request mới.
        if (!model.containsAttribute("request")) {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setEmail(email);
            request.setOtp(otp);
            model.addAttribute("request", request);
        }

        return "reset-password"; // Trả về reset-password.html
    }

    @PostMapping("/reset-password/save")
    public String handleResetPassword(@ModelAttribute("request") ResetPasswordRequest request, RedirectAttributes redirectAttributes) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
            // Giữ lại email và otp để quay lại form
            redirectAttributes.addFlashAttribute("request", request);
            return "redirect:/reset-password";
        }

        // Bắt lỗi kiểm tra mật khẩu yếu nếu cần

        if (forgotPasswordService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword())) {
            // Thành công: Chuyển hướng về trang đăng nhập
            redirectAttributes.addFlashAttribute("success", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.");
            return "redirect:/login";
        } else {
            // Thất bại (có thể OTP hết hạn trong quá trình nhập form)
            redirectAttributes.addFlashAttribute("error", "Phiên đặt lại mật khẩu không hợp lệ hoặc mã OTP đã hết hạn. Vui lòng thử lại từ đầu.");
            return "redirect:/forgot-password";
        }
    }
}
