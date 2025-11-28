package com.example.quan_ly_kho.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable // Đánh dấu đây là một lớp có thể nhúng (Embedded)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhieuMuonThietBiId implements Serializable {

    @Column(name = "phieu_muon_id")
    private Integer phieuMuonId;

    @Column(name = "thiet_bi_id")
    private Integer thietBiId;

    // JPA yêu cầu phải có equals() và hashCode() cho khóa kép
    // Lombok @Data đã tạo chúng
}
