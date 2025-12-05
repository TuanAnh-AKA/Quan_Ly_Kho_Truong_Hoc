package com.example.quan_ly_kho.controller;

import com.example.quan_ly_kho.model.LoaiThietBi;
import com.example.quan_ly_kho.service.LoaiThietBiService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/v1/loai-thiet-bi")
public class LoaiThietBiController {

    @Autowired
    private LoaiThietBiService loaiThietBiService;

    /**
     * POST /api/v1/loai-thiet-bi
     * Thêm một loại thiết bị mới
     */
    @PostMapping
    public ResponseEntity<LoaiThietBi> createLoaiThietBi(@RequestBody LoaiThietBi loai) {
        try {
            LoaiThietBi newLoai = loaiThietBiService.themLoaiThietBi(loai);
            // Trả về HTTP 201 Created
            return new ResponseEntity<>(newLoai, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Xử lý lỗi dữ liệu đầu vào (ví dụ: tên loại để trống)
            // Trong thực tế, nên dùng @ControllerAdvice để xử lý lỗi tốt hơn
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            // Xử lý lỗi logic (ví dụ: tên loại đã tồn tại)
            return new ResponseEntity(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // (Các phương thức GET, PUT, DELETE khác sẽ được thêm sau)


    @GetMapping
    public ResponseEntity<List<LoaiThietBi>> getAllLoaiThietBi() {
        List<LoaiThietBi> loaiList = loaiThietBiService.layTatCaLoai();
        return new ResponseEntity<>(loaiList, HttpStatus.OK);
    }
}
