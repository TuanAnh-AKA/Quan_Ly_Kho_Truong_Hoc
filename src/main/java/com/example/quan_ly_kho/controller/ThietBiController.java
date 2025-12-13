package com.example.quan_ly_kho.controller;

import com.example.quan_ly_kho.dto.ThietBiRequest;
import com.example.quan_ly_kho.model.LoaiThietBi;
import com.example.quan_ly_kho.model.ThietBi;
import com.example.quan_ly_kho.repository.LoaiThietBiRepo;
import com.example.quan_ly_kho.repository.ThietBiRepo;
import com.example.quan_ly_kho.service.LoaiThietBiService;
import com.example.quan_ly_kho.service.ThietBiService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final ThietBiService thietBiService;
    private final LoaiThietBiService loaiThietBiService;


    // --- 1. HIỂN THỊ DANH SÁCH THIẾT BỊ (READ) ---
    @GetMapping
    public String listThietBi(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id,desc") String sort,
            // --- THAM SỐ LỌC ---
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer loaiId,
            @RequestParam(required = false) Boolean tinhTrang)
    {
        // 1. Xử lý phân trang và sắp xếp
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        PageRequest pageable = PageRequest.of(page, size, sortOrder);

        // 2. Tải dữ liệu đã được phân trang và lọc
        Page<ThietBi> thietBiPage = thietBiService.searchThietBi(
                keyword,
                loaiId,
                tinhTrang,
                pageable);

        // 3. Tải danh sách Loại Thiết Bị (cho bộ lọc dropdown)
        model.addAttribute("dsLoaiThietBi", loaiThietBiRepo.findAll());

        // 4. Thêm Page object và các tham số lọc vào Model
        model.addAttribute("thietBiPage", thietBiPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("loaiId", loaiId);
        model.addAttribute("tinhTrang", tinhTrang);

        // 5. Thêm đối tượng form mới (cho chức năng thêm/sửa)
        // model.addAttribute("thietBiForm", new ThietBiForm()); // Cần thay bằng DTO của bạn

        return "thiet-bi-layout"; // Giả định tên trang HTML là thiet-bi-layout.html
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

    @PostMapping("/save")
    public String saveThietBi(@ModelAttribute("thietBi") ThietBi thietBi,
                              RedirectAttributes redirectAttributes) {

        try {
            // 1. Kiểm tra và Gán lại Khóa Ngoại (FK)
            Integer loaiId = thietBi.getLoaiThietBi().getId();
            if (loaiId == null) {
                throw new IllegalArgumentException("Vui lòng chọn Loại Thiết Bị.");
            }

            LoaiThietBi loaiThietBi = loaiThietBiService.findById(loaiId); // Giả định Service có findById
            thietBi.setLoaiThietBi(loaiThietBi);

            // 2. Gọi Service để Lưu (Service sẽ tự động biết INSERT hay UPDATE)
            thietBiService.save(thietBi); // Giả định Service có phương thức save() chung

            // 3. Thông báo thành công và chuyển hướng
            redirectAttributes.addFlashAttribute("successMessage",
                    thietBi.getId() == null ? "Thêm mới thiết bị thành công!" : "Cập nhật thiết bị thành công!");

            // Chuyển hướng người dùng về trang danh sách
            return "redirect:/thiet-bi";

        } catch (Exception e) {
            // 4. Xử lý lỗi và chuyển hướng về form kèm thông báo lỗi
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());

            // Giữ lại ID nếu đang cập nhật
            String redirectUrl = "/thiet-bi/view-update";
            if (thietBi.getId() != null) {
                redirectUrl += "?id=" + thietBi.getId();
            }
            return "redirect:" + redirectUrl;
        }

    }
    @GetMapping("/view-update/{id}")
    public String viewUpdate(@PathVariable("id") Integer id, Model model) {

        try {
            // 1. Tìm Thiết Bị theo ID
            ThietBi thietBi = thietBiService.findById(id);

            // 2. Truyền đối tượng Thiết Bị đã tìm được sang view (dùng cho việc điền sẵn dữ liệu)
            model.addAttribute("thietBi", thietBi);

            // 3. Lấy và truyền danh sách Loại Thiết Bị (Bắt buộc cho dropdown)
            model.addAttribute("dsLoaiThietBi", loaiThietBiService.layTatCaLoai());

            return "thietbi-form";

        } catch (RuntimeException e) {
            // Xử lý khi không tìm thấy ID
            model.addAttribute("errorMessage", "Không tìm thấy Thiết Bị để cập nhật.");
            // Chuyển hướng về trang danh sách hoặc trang lỗi
            return "redirect:/thiet-bi";
        }
    }

    // --- 4. XỬ LÝ XÓA (DELETE) ---
    @GetMapping("/delete/{id}")
    public String deleteThietBi(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            thietBiService.deleteThietBi(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thiết bị thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không thể xóa thiết bị vì có phiếu mượn liên quan.");
        }

        return "redirect:/thiet-bi";
    }
    @GetMapping("/view-update")
    public String viewUpdate(Model model) {

        // 1. Chuẩn bị đối tượng ThietBi rỗng (Dùng cho chức năng Thêm Mới)
        model.addAttribute("thietBi", new ThietBi());

        // 2. Lấy và truyền danh sách Loại Thiết Bị (Bắt buộc cho dropdown)
        model.addAttribute("dsLoaiThietBi", loaiThietBiService.layTatCaLoai());

        // Trả về tên file HTML (thietbi-form.html)
        return "thietbi-form";
    }

}
