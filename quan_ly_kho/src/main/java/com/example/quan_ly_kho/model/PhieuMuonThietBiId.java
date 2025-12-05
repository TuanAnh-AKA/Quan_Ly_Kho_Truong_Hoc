package com.example.quan_ly_kho.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class PhieuMuonThietBiId implements Serializable {

    @Column(name = "phieu_muon_id")
    private Integer phieuMuonId;

    @Column(name = "thiet_bi_id")
    private Integer thietBiId;
}
