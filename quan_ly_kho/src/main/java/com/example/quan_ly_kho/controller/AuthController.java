package com.example.quan_ly_kho.controller;

import com.example.quan_ly_kho.model.LoginRequest; // Cần giữ lại để dùng trong @GetMapping
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {


    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login"; // Trả về src/main/resources/templates/login.html
    }



    @GetMapping("/dashboard")
    public String showDashboard() {
        return "layout";
    }
}