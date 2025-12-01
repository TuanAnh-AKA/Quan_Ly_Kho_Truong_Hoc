package com.example.quan_ly_kho.controller;

import com.example.quan_ly_kho.model.LoaiThietBi;
import com.example.quan_ly_kho.model.ThietBi;
import com.example.quan_ly_kho.repository.LoaiThietBiRepo;
import com.example.quan_ly_kho.repository.ThietBiRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/thiet-bi")
@RequiredArgsConstructor
public class ThietBiController {

    private final ThietBiRepo thietBiRepo;
    private final LoaiThietBiRepo loaiThietBiRepo;

    // --- 1. HIỂN THỊ DANH SÁCH THIẾT BỊ (READ) ---
    @GetMapping
    public String listThietBi(Model model) {
        // Lấy danh sách thiết bị và loại thiết bị để hiển thị bộ lọc
        List<ThietBi> dsThietBi = thietBiRepo.findAll();
        List<LoaiThietBi> dsLoaiThietBi = loaiThietBiRepo.findAll();

        model.addAttribute("dsThietBi", dsThietBi);
        model.addAttribute("dsLoaiThietBi", dsLoaiThietBi);

        // Chuẩn bị một đối tượng ThietBi mới cho form Thêm/Sửa (nếu dùng chung form)
        model.addAttribute("thietBi", new ThietBi());

        // Trả về tên file view
        return "thiet-bi-layout";
    }

    // --- 2. XỬ LÝ TÌM KIẾM/LỌC (SEARCH) ---
    @GetMapping("/search")
    public String searchThietBi(@RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "loaiId", required = false) Integer loaiId,
                                Model model) {

        // **Lưu ý:** Trong thực tế, bạn cần logic phức tạp hơn (sử dụng Specification)
        // Hiện tại, chúng ta chỉ làm đơn giản (tùy thuộc vào keyword)
        List<ThietBi> dsThietBi;
        if (keyword != null && !keyword.isEmpty()) {
            // Giả định có phương thức tìm kiếm theo tên/mã
            dsThietBi = thietBiRepo.findByTenThietBiContainingIgnoreCaseOrMaThietBiContainingIgnoreCase(keyword, keyword);
        } else {
            dsThietBi = thietBiRepo.findAll();
        }

        // Thêm lại các thuộc tính cần thiết cho view
        model.addAttribute("dsThietBi", dsThietBi);
        model.addAttribute("dsLoaiThietBi", loaiThietBiRepo.findAll());
        model.addAttribute("thietBi", new ThietBi());

        return "thiet-bi-layout";
    }


    // --- 3. XỬ LÝ LƯU (THÊM MỚI / CẬP NHẬT) ---
    @PostMapping("/save")
    public String saveThietBi(@ModelAttribute("thietBi") ThietBi thietBi,
                              RedirectAttributes redirectAttributes) {

        try {
            // Trong thực tế, bạn cần logic kiểm tra validation và mã thiết bị trùng
            thietBiRepo.save(thietBi);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thiết bị **" + thietBi.getTenThietBi() + "** thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu thiết bị: " + e.getMessage());
        }

        return "redirect:/thiet-bi";
    }

    // --- 4. XỬ LÝ XÓA (DELETE) ---
    @GetMapping("/delete/{id}")
    public String deleteThietBi(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            thietBiRepo.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thiết bị ID **" + id + "** thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không thể xóa thiết bị ID **" + id + "** vì có phiếu mượn liên quan.");
        }

        return "redirect:/thiet-bi";
    }
}
