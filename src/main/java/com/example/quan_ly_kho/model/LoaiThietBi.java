package com.example.quan_ly_kho.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "Loai_Thiet_Bi") // Ánh xạ tới bảng Loai_Thiet_Bi trong CSDL
@Data // Lombok: Tự động tạo getter, setter, toString, equals, hashCode
public class LoaiThietBi {

    @Id // Khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "ten_loai", nullable = false, length = 100)
    private String tenLoai;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    // --- Mối quan hệ với Thiết Bị ---
    // Loại Thiết Bị (One) có nhiều Thiết Bị (Many)
    // mappedBy trỏ đến trường loaiThietBiId trong entity ThietBi
    @OneToMany(mappedBy = "loaiThietBi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ThietBi> thietBiList;
}
