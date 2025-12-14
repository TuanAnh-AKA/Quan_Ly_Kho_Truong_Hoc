package com.example.quan_ly_kho.controller;


import com.example.quan_ly_kho.service.SendGridEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/email")
public class EmailTestController {

    @Autowired
    private SendGridEmailService emailService;

    @GetMapping("/test")
    public String sendTest(@RequestParam String to) throws IOException {
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000); // OTP 6 số
        emailService.sendOtpEmail(to, otp);
        return "Đã gửi mã OTP " + otp + " đến " + to;
    }
}

