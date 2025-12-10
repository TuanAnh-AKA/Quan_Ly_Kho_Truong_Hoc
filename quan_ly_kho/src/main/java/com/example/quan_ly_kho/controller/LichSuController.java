package com.example.quan_ly_kho.controller;

import com.example.quan_ly_kho.dto.PhieuMuonForm;
import com.example.quan_ly_kho.model.PhieuMuon;
import com.example.quan_ly_kho.model.PhieuMuonThietBi;
import com.example.quan_ly_kho.service.ExportService;
import com.example.quan_ly_kho.service.MuonTraService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/lich-su")
@RequiredArgsConstructor
public class LichSuController {
    private final MuonTraService muonTraService;
    private final ExportService exportService;
    @GetMapping
    public String listLichSu(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "id,desc") String sort,
            // THAM SỐ LỌC
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            // 🚨 THAM SỐ MỚI: Lọc theo trạng thái
            @RequestParam(required = false) String trangThaiFilter) // Giá trị: "DANG_MUON" hoặc "DA_TRA"
    {
        // 1. Load các attribute chung (chỉ cần dsLoaiThietBi cho form lọc chính/footer)
        addCommonAttributes(model);

        // 2. Xử lý phân trang và sắp xếp
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        PageRequest pageable = PageRequest.of(page, size, sortOrder);

        // 3. Tải dữ liệu TỔNG HỢP (Page<PhieuMuon>)
        // 🚨 LOẠI BỎ THAM SỐ LOAI_ID VÀ INCLUDE_ACTIVE TRỰC TIẾP
        Page<PhieuMuon> lichSuPage = muonTraService.searchPhieuMuonHistory( // 🚨 Tên hàm mới
                keyword,
                fromDate,
                toDate,
                trangThaiFilter, // 🚨 Truyền trạng thái vào Service
                pageable);

        // 4. Thêm Page object và tham số lọc vào Model
        model.addAttribute("lichSuPage", lichSuPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("trangThaiFilter", trangThaiFilter); // Truyền lại trạng thái đã chọn

        return "lich-su-layout";
    }
    @GetMapping("/chi-tiet/{id}")
    public String chiTietPhieuMuon(@PathVariable Integer id, Model model) {

        // 1. Tải Phiếu Mượn chính
        Optional<PhieuMuon> phieuMuonOptional = muonTraService.findPhieuMuonById(id);

        if (phieuMuonOptional.isEmpty()) {
            // Xử lý nếu không tìm thấy phiếu mượn (ví dụ: chuyển hướng về trang lỗi hoặc trang danh sách)
            throw new RuntimeException("Không tìm thấy Phiếu Mượn ID: " + id);
        }

        PhieuMuon phieuMuon = phieuMuonOptional.get();

        // 2. Thêm dữ liệu vào Model
        model.addAttribute("phieuMuon", phieuMuon);

        // 3. Tải các danh sách chung (ví dụ: dsLoaiThietBi) nếu cần cho layout
        // addCommonAttributes(model); // Có thể bỏ qua nếu trang chi tiết không cần form lọc

        return "phieu-muon-chi-tiet";
    }
    // Đặt hàm này trong class LichSuController
    private void addCommonAttributes(Model model) {
        // Tải danh sách Loại thiết bị để hiển thị trong bộ lọc
        model.addAttribute("dsLoaiThietBi", muonTraService.findAllLoaiThietBi());

        // Tải danh sách thiết bị rảnh (nếu bạn có hiển thị form Thêm mới trên trang này)
        model.addAttribute("dsThietBiRanh", muonTraService.dsThietBiRanh());
        // Nếu trang Lịch sử không cần thiết bị rảnh, bạn có thể bỏ dòng này.
    }
    @GetMapping("/export/excel")
    public void exportToExcel(
            HttpServletResponse response,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam(required = false) String trangThaiFilter) throws IOException {

        // 1. Lấy toàn bộ danh sách đã lọc từ MuonTraService
        List<PhieuMuon> dsPhieuMuon = muonTraService.getFilteredLichSuList(
                keyword, fromDate, toDate, trangThaiFilter
        );

        // 2. Gọi ExportService để tạo và xuất file Excel
        exportService.exportLichSuToExcel(response, dsPhieuMuon);
    }
}
