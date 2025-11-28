package com.example.quan_ly_kho.model;



import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Thiet_Bi") // Ánh xạ tới bảng Thiet_Bi trong CSDL
@Data // Lombok: Tự động tạo getter, setter, toString, equals, hashCode
public class ThietBi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "ma_thiet_bi", nullable = false, unique = true, length = 50)
    private String maThietBi;

    @Column(name = "ten_thiet_bi", nullable = false, length = 100)
    private String tenThietBi;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong; // Số lượng tồn kho

    @Column(name = "ngay_nhap")
    private LocalDate ngayNhap;

    @Column(name = "nha_cung_cap", length = 100)
    private String nhaCungCap;

    @Column(name = "tinh_trang", nullable = false)
    private Boolean tinhTrang; // Tình trạng chung (ví dụ: sẵn sàng sử dụng)

    @Column(name = "da_muon", nullable = false)
    private Boolean daMuon; // Cờ hiệu: Có thiết bị nào đang được mượn không (thường set true nếu soLuong = 0)

    // --- Mối quan hệ Many-to-One với LoaiThietBi (Khóa ngoại) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loai_thiet_bi_id", nullable = false)
    private LoaiThietBi loaiThietBi;

    // --- Mối quan hệ One-to-Many với Chi tiết Phiếu Mượn ---
    // mappedBy trỏ đến trường ThietBi trong entity PhieuMuonThietBi
    @OneToMany(mappedBy = "thietBi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PhieuMuonThietBi> chiTietMuonList;
}
