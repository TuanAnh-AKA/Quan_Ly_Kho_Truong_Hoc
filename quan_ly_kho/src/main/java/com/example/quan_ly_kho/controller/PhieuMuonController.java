package com.example.quan_ly_kho.controller;

import com.example.quan_ly_kho.dto.PhieuMuonForm;
import com.example.quan_ly_kho.dto.PhieuMuonUpdateForm;
import com.example.quan_ly_kho.model.PhieuMuon;
import com.example.quan_ly_kho.repository.ThietBiRepo;
import com.example.quan_ly_kho.service.MuonTraService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/phieumuon")
@RequiredArgsConstructor
public class PhieuMuonController {

    private final MuonTraService muonTraService;
    private final ThietBiRepo thietBiRepo;

    // --- Phương thức hỗ trợ để load lại các attribute cần thiết cho layout ---
    private void addCommonAttributes(Model model) {
        model.addAttribute("dsPhieuMuon", muonTraService.findAllPhieuMuon());
        model.addAttribute("dsThietBiRanh", muonTraService.dsThietBiRanh());
        model.addAttribute("dsLoaiThietBi", muonTraService.findAllLoaiThietBi());
    }

    // ---------------------------------------------------------------------------------------------------

    // --- 1. HIỂN THỊ DANH SÁCH & FORM TẠO MỚI (GET) ---
    @GetMapping
    public String listPhieuMuon(
            Model model,
            // Thêm tham số phân trang
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "id,desc") String sort)
    {
        // 1. Load các attribute chung (không bao gồm dsPhieuMuon)
        addCommonAttributes(model);

        // 2. Xử lý phân trang
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        PageRequest pageable = PageRequest.of(page, size, sortOrder);

        // Lấy dữ liệu đã được phân trang
        Page<PhieuMuon> phieuMuonPage = muonTraService.findAllPhieuMuon(pageable); // <-- Cần phương thức Service mới

        // 3. Thêm Page object vào Model
        model.addAttribute("phieuMuonPage", phieuMuonPage);

        // 4. Xử lý form thêm mới (giữ nguyên)
        if (!model.containsAttribute("phieuMuonForm")) {
            model.addAttribute("phieuMuonForm", new PhieuMuonForm());
        }

        // Loại bỏ pmUpdateForm nếu có, để chỉ hiển thị form thêm mới
        model.addAttribute("pmUpdateForm", null);

        return "phieu-muon-layout";
    }

    // --- 2. XỬ LÝ TẠO MỚI (CREATE - Process) ---
    @PostMapping("/save")
    public String savePhieuMuon(
            @ModelAttribute("phieuMuonForm") PhieuMuonForm form,
            RedirectAttributes redirectAttributes
    ) {
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
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tạo phiếu: " + e.getMessage());
            // FlashAttribute DTO về để giữ lại dữ liệu người dùng đã nhập
            redirectAttributes.addFlashAttribute("phieuMuonForm", form);
        }

        return "redirect:/phieumuon";
    }

    // ---------------------------------------------------------------------------------------------------

    // --- 3. HIỂN THỊ FORM CẬP NHẬT (GET) ---
    @GetMapping("/edit/{id}")
    public String editPhieuMuon(@PathVariable("id") Integer id, Model model) {
        // Load lại các attribute chung cho layout
        addCommonAttributes(model);

        PhieuMuon pm = muonTraService.findPhieuMuonById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn "));

        // Khởi tạo DTO từ Entity
        PhieuMuonUpdateForm form = new PhieuMuonUpdateForm();
        form.setId(pm.getId());
        form.setMaPhieu(pm.getMaPhieu());
        form.setNgayMuon(pm.getNgayMuon());
        form.setNguoiMuonText(pm.getNguoiMuonText());
        form.setTrangThai(pm.getTrangThai());

        // Lấy danh sách ID thiết bị đang được mượn
        form.setThietBiIds(muonTraService.findThietBiIdsByPhieuId(id));

        // FIX 1: Dùng tên **pmUpdateForm** nhất quán cho Form Cập nhật
        model.addAttribute("pmUpdateForm", form);
        model.addAttribute("dsThietBiAll", thietBiRepo.findAll()); // Dùng cho form update

        // Đặt phieuMuonForm thành null để chỉ hiển thị form update trong HTML
        model.addAttribute("phieuMuonForm", null);

        return "phieu-muon-layout";
    }

    // --- 4. XỬ LÝ CẬP NHẬT (POST) ---
    @PostMapping("/update")
    public String updatePhieuMuon(@ModelAttribute("pmUpdateForm") PhieuMuonUpdateForm form,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        try {
            muonTraService.capNhatPhieuMuon(form);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phiếu mượn **" + form.getMaPhieu() + "** thành công!");
            return "redirect:/phieumuon"; // Thành công thì chuyển hướng về danh sách
        } catch (RuntimeException e) {
            // FIX 2: Khi có lỗi, KHÔNG DÙNG REDIRECT. Dùng Model và return view trực tiếp.
            model.addAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());

            // Thêm lại các attribute cần thiết cho form (ds thiết bị, form data)
            model.addAttribute("pmUpdateForm", form);
            model.addAttribute("dsThietBiAll", thietBiRepo.findAll());

            // Load lại các attribute chung cho layout
            addCommonAttributes(model);

            // Đặt phieuMuonForm thành null để tiếp tục hiển thị form update
            model.addAttribute("phieuMuonForm", null);

            return "phieu-muon-layout"; // Trả về view để hiển thị lỗi và dữ liệu đã nhập
        }
    }

    // ---------------------------------------------------------------------------------------------------
    // --- Các chức năng khác giữ nguyên ---

    // --- 5. XỬ LÝ TRẢ PHIẾU (UPDATE/RETURN) ---
    @GetMapping("/return/{id}")
    public String traPhieu(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            muonTraService.traHetPhieu(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã trả hết thiết bị cho phiếu mượn thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi trả phiếu: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi trả phiếu: " + e.getMessage());
        }
        return "redirect:/phieumuon";
    }

    // --- 6. XỬ LÝ XÓA PHIẾU (DELETE) ---
    @GetMapping("/delete/{id}")
    public String xoaPhieu(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            muonTraService.xoaPhieuMuon(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa Phiếu mượn và hoàn trả tồn kho thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa phiếu: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi xóa phiếu: " + e.getMessage());
        }
        return "redirect:/phieumuon";
    }
}