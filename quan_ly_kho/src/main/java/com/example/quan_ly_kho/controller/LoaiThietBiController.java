package com.example.quan_ly_kho.controller;

import com.example.quan_ly_kho.model.LoaiThietBi;
import com.example.quan_ly_kho.service.LoaiThietBiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/loai-thiet-bi")
@RequiredArgsConstructor
public class LoaiThietBiController {
    private final LoaiThietBiService loaiThietBiService;

    // --- 1. HIỂN THỊ DANH SÁCH VÀ FORM THÊM MỚI ---
    @GetMapping
    public String listLoaiThietBi(Model model) {
        model.addAttribute("dsLoaiThietBi", loaiThietBiService.layTatCaLoai());

        // Form Thêm Mới rỗng
        if (!model.containsAttribute("loaiThietBiForm")) {
            model.addAttribute("loaiThietBiForm", new LoaiThietBi());
        }

        return "loai-thiet-bi"; // Trả về tên file HTML mới
    }

    // --- 2. XỬ LÝ LƯU (THÊM MỚI VÀ SỬA) ---
    @PostMapping("/save")
    public String saveLoaiThietBi(@ModelAttribute LoaiThietBi loaiThietBi, RedirectAttributes redirectAttributes) {
        try {
            String message;
            if (loaiThietBi.getId() == null) {
                loaiThietBiService.save(loaiThietBi);
                message = "Thêm loại thiết bị thành công!";
            } else {
                loaiThietBiService.save(loaiThietBi);
                message = "Cập nhật loại thiết bị thành công!";
            }
            redirectAttributes.addFlashAttribute("successMessage", message);

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("loaiThietBiForm", loaiThietBi); // Giữ lại dữ liệu đã nhập
        }
        return "redirect:/loai-thiet-bi";
    }

    // --- 3. XỬ LÝ FORM SỬA (Tải dữ liệu vào form) ---
    @GetMapping("/edit/{id}")
    public String editLoaiThietBi(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<LoaiThietBi> loaiThietBi = loaiThietBiService.findById2(id);

        if (loaiThietBi.isPresent()) {
            // Dùng Flash Attribute để đưa đối tượng cần sửa về trang GET
            redirectAttributes.addFlashAttribute("loaiThietBiForm", loaiThietBi.get());
        }
        return "redirect:/loai-thiet-bi";
    }

    // --- 4. XỬ LÝ XÓA ---
    @GetMapping("/delete/{id}")
    public String deleteLoaiThietBi(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            loaiThietBiService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa loại thiết bị thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/loai-thiet-bi";
    }
}
