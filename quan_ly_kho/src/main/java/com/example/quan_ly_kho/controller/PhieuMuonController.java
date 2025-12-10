package com.example.quan_ly_kho.controller;

import com.example.quan_ly_kho.dto.PhieuMuonForm;
import com.example.quan_ly_kho.dto.PhieuMuonUpdateForm;
import com.example.quan_ly_kho.model.PhieuMuon;
import com.example.quan_ly_kho.model.ThietBi;
import com.example.quan_ly_kho.repository.ThietBiRepo;
import com.example.quan_ly_kho.service.LoaiThietBiService;
import com.example.quan_ly_kho.service.MuonTraService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/phieumuon")
@RequiredArgsConstructor
public class PhieuMuonController {

    private final MuonTraService muonTraService;
    private final ThietBiRepo thietBiRepo;
    private final LoaiThietBiService loaiThietBiService;

    private void addCommonAttributes(Model model) {

        model.addAttribute("dsThietBiRanh", muonTraService.dsThietBiRanh());
        model.addAttribute("dsLoaiThietBi", muonTraService.findAllLoaiThietBi());
    }

    @GetMapping
    public String listPhieuMuon(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "id,desc") String sort,
            // THÊM THAM SỐ LỌC
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate, // Đã sửa
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,   // Đã sửa
            @RequestParam(required = false) Integer loaiId)
    {
        // 1. Load các attribute chung (dsThietBiRanh, dsLoaiThietBi, etc.)
        addCommonAttributes(model);

        // 2. Xử lý phân trang và sắp xếp
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        PageRequest pageable = PageRequest.of(page, size, sortOrder);

        // 3. Tải dữ liệu đã được phân trang và lọc (Gọi Service mới)
        Page<PhieuMuon> phieuMuonPage = muonTraService.searchPhieuMuon(
                keyword,
                fromDate,
                toDate,
                loaiId,
                true,
                pageable);

        // 4. Thêm Page object vào Model
        model.addAttribute("phieuMuonPage", phieuMuonPage);

        // 5. Thêm các tham số lọc vào Model để Thymeleaf có thể giữ lại trạng thái trên form
        // (Mặc dù Thymeleaf có thể tự động lấy từ 'param', nhưng việc thêm rõ ràng sẽ giúp code dễ đọc và kiểm soát hơn)
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("loaiId", loaiId);


        // 6. Xử lý form thêm mới (giữ nguyên)
        if (!model.containsAttribute("phieuMuonForm")) {
            model.addAttribute("phieuMuonForm", new PhieuMuonForm());
        }

        // 7. Loại bỏ pmUpdateForm nếu có, để chỉ hiển thị form thêm mới
        model.addAttribute("pmUpdateForm", null);

        return "phieu-muon-layout";
    }
    // --- 2. HIỂN THỊ FORM CẬP NHẬT (EDIT) ---
    @GetMapping("/edit/{id}")
    public String editPhieuMuon(
            @PathVariable("id") Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            // THÊM THAM SỐ LỌC
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate, // Đã sửa
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,   // Đã sửa
            @RequestParam(required = false) Integer loaiId,
            // Đã thêm tham số sắp xếp
            @RequestParam(defaultValue = "id,desc") String sort,
            Model model) {

        // 1. Load các attribute chung (Sidebar, Header, Filter dropdown data)
        addCommonAttributes(model);

        // 2. TẠO PAGEABLE ĐỂ HIỂN THỊ DANH SÁCH (Đã đồng bộ)
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        PageRequest pageable = PageRequest.of(page, size, sortOrder);

        // Tải dữ liệu danh sách/phân trang với sắp xếp đã đồng bộ
        Page<PhieuMuon> phieuMuonPage = muonTraService.searchPhieuMuon(
                keyword,
                fromDate,
                toDate,
                loaiId,
                true,
                pageable);

        model.addAttribute("phieuMuonPage", phieuMuonPage);

        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("loaiId", loaiId);

        // 3. TẢI DỮ LIỆU PHIẾU MƯỢN ĐỂ CHỈNH SỬA
        PhieuMuon pm = muonTraService.findPhieuMuonById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn có ID: " + id));

        // ... (Giữ nguyên phần DTO mapping và tải dsThietBiAll) ...
        PhieuMuonUpdateForm form = new PhieuMuonUpdateForm();
        form.setId(pm.getId());
        form.setMaPhieu(pm.getMaPhieu());
        form.setNgayMuon(pm.getNgayMuon());
        form.setNguoiMuonText(pm.getNguoiMuonText());
        form.setTrangThai(pm.getTrangThai());
        form.setThietBiIds(muonTraService.findThietBiMuonDetails(id));

        List<ThietBi> dsThietBiAll = muonTraService.findAllThietBi();

        // 4. Truyền dữ liệu Form
        model.addAttribute("pmUpdateForm", form);
        model.addAttribute("dsThietBiAll", dsThietBiAll);

        // 5. Truyền lại các tham số phân trang/sắp xếp/lọc (Cần thiết cho HTML)
        model.addAttribute("sort", sort);
        // ... (Bạn nên truyền thêm các tham số lọc nếu có: keyword, fromDate, toDate, loaiId)

        // Ẩn Form Thêm mới
        model.addAttribute("phieuMuonForm", null);

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
    private void loadPhieuMuonData(Model model,
                                   int page,
                                   int size,
                                   String sort,
                                   String keyword,
                                   LocalDate fromDate,
                                   LocalDate toDate,
                                   Integer loaiId) {

        // 1. Xử lý phân trang và sắp xếp
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        PageRequest pageable = PageRequest.of(page, size, sortOrder);

        // 2. Tải dữ liệu đã được phân trang và lọc (Gọi Service)
        Page<PhieuMuon> phieuMuonPage = muonTraService.searchPhieuMuon(
                keyword,
                fromDate,
                toDate,
                loaiId,
                true,
                pageable);

        // 3. Thêm Page object vào Model
        model.addAttribute("phieuMuonPage", phieuMuonPage);

        // 4. Thêm các tham số lọc vào Model để Thymeleaf có thể giữ lại trạng thái trên form
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("loaiId", loaiId);
    }

    @PostMapping("/update")
    public String updatePhieuMuon(@ModelAttribute("pmUpdateForm") PhieuMuonUpdateForm form,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        try {
            muonTraService.capNhatPhieuMuon(form);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phiếu mượn **" + form.getMaPhieu() + "** thành công!");
            return "redirect:/phieumuon";
        } catch (RuntimeException e) {
            // --- 1. Xử lý lỗi ---
            model.addAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());

            // --- 2. Tải lại dữ liệu cho FORM CẬP NHẬT ---
            // Giữ lại dữ liệu form đã nhập
            model.addAttribute("pmUpdateForm", form);
            // Tải lại danh sách thiết bị đầy đủ
            // Lưu ý: Nếu dsThietBiAll không phải là thuộc tính chung, bạn cần tải nó ở đây
            // model.addAttribute("dsThietBiAll", thietBiRepo.findAll());

            // --- 3. Tải lại DỮ LIỆU CHUNG và PHÂN TRANG cho LAYOUT ---
            // Tải lại dsThietBiRanh, dsLoaiThietBi
            addCommonAttributes(model);

            // Tải lại PHIEUMUONPAGE để khắc phục lỗi "phieuMuonPage.size" is null
            // Đặt mặc định về trang 0, 6 mục/trang, sắp xếp id,desc
            loadPhieuMuonData(model, 0, 6, "id,desc", null, null, null, null);

            // Loại bỏ form thêm mới để chỉ hiển thị form sửa
            model.addAttribute("phieuMuonForm", null);

            return "phieu-muon-layout";
        }
    }


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