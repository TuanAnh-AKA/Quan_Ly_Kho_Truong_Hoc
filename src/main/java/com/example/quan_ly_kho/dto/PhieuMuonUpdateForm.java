package com.example.quan_ly_kho.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Getter
@Setter
@Data
public class PhieuMuonUpdateForm {

    private Integer id;

    private String maPhieu;

    private String nguoiMuonText;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayMuon;

    private Boolean trangThai;

    // Danh sách ID thiết bị được chọn sau khi submit form
    private Map<Integer, Integer> thietBiIds = new HashMap<>();

}
