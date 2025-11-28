package com.example.quan_ly_kho;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner; // Import cần thiết
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder; // Import cần thiết

@SpringBootApplication
public class QuanLyKhoApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuanLyKhoApplication.class, args);
    }


}