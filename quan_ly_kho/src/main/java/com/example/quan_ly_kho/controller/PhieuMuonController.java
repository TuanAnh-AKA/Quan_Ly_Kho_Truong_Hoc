package com.example.quan_ly_kho.controller;

import com.example.quan_ly_kho.dto.PhieuMuonForm;
import com.example.quan_ly_kho.dto.PhieuMuonUpdateForm;
import com.example.quan_ly_kho.model.PhieuMuon;
import com.example.quan_ly_kho.repository.ThietBiRepo;
import com.example.quan_ly_kho.service.MuonTraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/phieumuon")
@RequiredArgsConstructor
public class PhieuMuonController {

    private final MuonTraService muonTraService;
    private final ThietBiRepo thietBiRepo; // Dùng để load ds Thiet Bi All
    // --- 1. HIỂN THỊ DANH SÁCH & FORM TẠO MỚI ---
    @GetMapping
    public String listPhieuMuon(Model model) {
        // ... (Logic đã có)
        model.addAttribute("dsPhieuMuon", muonTraService.findAllPhieuMuon());
        model.addAttribute("dsThietBiRanh", muonTraService.dsThietBiRanh());
        model.addAttribute("phieuMuonForm", new PhieuMuonForm());
        model.addAttribute("dsLoaiThietBi", muonTraService.findAllLoaiThietBi()); // Cho bộ lọc
        return "phieu-muon-layout";
    }

    // --- 2. XỬ LÝ TẠO MỚI (CREATE - Process) ---
    @PostMapping("/save")
    public String savePhieuMuon(
            @ModelAttribute("phieuMuonForm") PhieuMuonForm form, // KHÔNG DÙNG @Valid
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        // Chuyển đổi List thiết bị mượn sang Map<ID, SoLuong> (Cần điều chỉnh DTO nếu dùng List)
        // GIẢ ĐỊNH DTO VẪN LÀ MAP<Integer, Integer> cho đơn giản

        Map<Integer, Integer> thietBiMuonMap = form.getThietBiMuon();

        try {
            muonTraService.taoPhieuMuon(
                    thietBiMuonMap,
                    form.getMaPhieu(),
                    form.getNgayMuon(),
                    form.getNguoiMuonText()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu mượn **" + form.getMaPhieu() + "** thành công!");

        } catch (RuntimeException e) {
            // Bắt lỗi nghiệp vụ từ Service
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tạo phiếu: " + e.getMessage());
            // FlashAttribute sẽ chuyển hướng DTO và lỗi về lại trang GET
            redirectAttributes.addFlashAttribute("phieuMuonForm", form);
        }

        return "redirect:/phieumuon";
    }

// ---------------------------------------------------------------------------------------------------

    // --- 3. HIỂN THỊ FORM CẬP NHẬT (GET) ---
    @GetMapping("/edit/{id}")
    public String editPhieuMuon(@PathVariable("id") Integer id, Model model) {
        PhieuMuon pm = muonTraService.findPhieuMuonById(id) // Giả định có phương thức findById trong Service
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn ID: " + id));

        // Khởi tạo DTO từ Entity
        PhieuMuonUpdateForm form = new PhieuMuonUpdateForm();
        form.setId(pm.getId());
        form.setMaPhieu(pm.getMaPhieu());
        form.setNgayMuon(pm.getNgayMuon());
        form.setNguoiMuonText(pm.getNguoiMuonText());
        form.setTrangThai(pm.getTrangThai());

        // Lấy danh sách ID thiết bị đang được mượn
        form.setThietBiIds(muonTraService.findThietBiIdsByPhieuId(id));

        model.addAttribute("pmUpdateForm", form);
        model.addAttribute("dsThietBiAll", thietBiRepo.findAll());

        return "phieu-muon-layout";
    }


    // --- 4. XỬ LÝ CẬP NHẬT (POST) ---
    @PostMapping("/update")
    public String updatePhieuMuon(@ModelAttribute("pmUpdateForm") PhieuMuonUpdateForm form, // KHÔNG DÙNG @Valid
                                  RedirectAttributes redirectAttributes,
                                  Model model) {

        try {
            muonTraService.capNhatPhieuMuon(form);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phiếu mượn **" + form.getMaPhieu() + "** thành công!");
        } catch (RuntimeException e) {
            // Bắt lỗi nghiệp vụ từ Service
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
            // Chuyển hướng DTO và lỗi về lại trang GET
            redirectAttributes.addFlashAttribute("pmUpdateForm", form);
            // Cần load lại dsThietBiAll cho trang update
            redirectAttributes.addFlashAttribute("dsThietBiAll", thietBiRepo.findAll());
        }

        return "redirect:/phieu-muon-layout";
    }

// ---------------------------------------------------------------------------------------------------

    // --- 4. XỬ LÝ TRẢ PHIẾU (UPDATE/RETURN) ---

    /**
     * Xử lý trả toàn bộ thiết bị trong phiếu mượn.
     */
    @GetMapping("/return/{id}")
    public String traPhieu(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            muonTraService.traHetPhieu(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã trả hết thiết bị cho Phiếu ID **" + id + "** thành công.");
        } catch (RuntimeException e) {
            // Xử lý các lỗi nghiệp vụ từ Service
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi trả phiếu: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi trả phiếu: " + e.getMessage());
        }
        return "redirect:/phieumuon/list";
    }

// ---------------------------------------------------------------------------------------------------

    // --- 5. XỬ LÝ XÓA PHIẾU (DELETE) ---

    /**
     * Xử lý xóa Phiếu mượn và hoàn trả tồn kho (nếu chưa trả).
     */
    @GetMapping("/delete/{id}")
    public String xoaPhieu(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            muonTraService.xoaPhieuMuon(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa Phiếu mượn ID **" + id + "** và hoàn trả tồn kho thành công.");
        } catch (RuntimeException e) {
            // Xử lý các lỗi nghiệp vụ từ Service
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa phiếu: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi xóa phiếu: " + e.getMessage());
        }
        return "redirect:/phieumuon/list";
    }
}
