package com.example.quan_ly_kho.dto;



import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Map;

@Data // Lombok: getter, setter, v.v.
public class PhieuMuonForm {

    private Integer id;

    private String maPhieu;


    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayMuon;


    private String nguoiMuonText;

    private Map<Integer, Integer> thietBiMuon;
}
