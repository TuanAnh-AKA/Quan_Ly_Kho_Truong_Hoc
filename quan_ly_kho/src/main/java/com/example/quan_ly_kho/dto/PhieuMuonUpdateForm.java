package com.example.quan_ly_kho.dto;


import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class PhieuMuonUpdateForm {

    private Integer id;

    private String maPhieu;

    private String nguoiMuonText;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayMuon;

    private Boolean trangThai;

    // Danh sách ID thiết bị được chọn sau khi submit form
    private List<Integer> thietBiIds;
}
