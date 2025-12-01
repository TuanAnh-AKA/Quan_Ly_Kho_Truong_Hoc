package com.example.quan_ly_kho.controller;

import com.example.quan_ly_kho.model.PhieuMuonThietBi;
import com.example.quan_ly_kho.service.MuonTraService;
import lombok.RequiredArgsConstructor;
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
            Model model) {

        List<PhieuMuonThietBi> dsLichSu = muonTraService.findAllLichSu(keyword, trangThai);

        model.addAttribute("dsLichSu", dsLichSu);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("activePage", "lichsu");

        return "lich-su-layout";
    }
}
