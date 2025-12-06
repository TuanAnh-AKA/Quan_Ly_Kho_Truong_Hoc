package com.example.quan_ly_kho.controller;

import com.example.quan_ly_kho.model.PhieuMuonThietBi;
import com.example.quan_ly_kho.service.MuonTraService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
@RequestMapping("/lich-su")
@RequiredArgsConstructor
public class LichSuController {

    private final MuonTraService muonTraService;

    @GetMapping
    public String listLichSu(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            // Thêm tham số phân trang
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id,desc") String sort,
            Model model) {

        // 1. Xử lý phân trang và sắp xếp
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        PageRequest pageable = PageRequest.of(page, size, sortOrder);

        // 2. Gọi Service với phân trang và lọc
        Page<PhieuMuonThietBi> dsLichSuPage = muonTraService.findLichSu(keyword, trangThai, pageable);

        // 3. Thêm Page object vào Model
        model.addAttribute("dsLichSuPage", dsLichSuPage);

        // 4. Giữ lại tham số lọc
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("activePage", "lichsu");

        return "lich-su-layout";
    }
}
