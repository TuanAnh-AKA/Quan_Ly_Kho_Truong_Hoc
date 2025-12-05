package com.example.quan_ly_kho.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThietBiRequest {
    private String maThietBi;
    private String tenThietBi;
    private String moTa;
    private Integer soLuong;
    private LocalDate ngayNhap;
    private String nhaCungCap;
    private Integer loaiThietBiId; // Chỉ nhận ID từ client
}
