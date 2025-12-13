package com.example.quan_ly_kho.service;



import com.example.quan_ly_kho.model.PhieuMuon;
import com.example.quan_ly_kho.model.PhieuMuonThietBi;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    /**
     * Tạo và Ghi dữ liệu Lịch Sử Phiếu Mượn ra file Excel
     * @param response Đối tượng response của Servlet để ghi dữ liệu
     * @param dsPhieuMuon Danh sách Phiếu Mượn đã được lọc
     * @throws IOException
     */
    public void exportLichSuToExcel(HttpServletResponse response, List<PhieuMuon> dsPhieuMuon) throws IOException {

        // 1. Cấu hình Header của Response
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LichSuMuonTra_" + LocalDate.now() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        // 2. Tạo Workbook và Sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Lịch Sử Tổng Hợp");
        sheet.setDefaultColumnWidth(20);

        // 3. Tạo Header (Dòng tiêu đề)
        String[] headers = {"ID", "Mã Phiếu", "Ngày Mượn", "Người Mượn", "Tổng SL Mượn", "Trạng Thái"};
        Row headerRow = sheet.createRow(0);

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 4. Ghi dữ liệu chi tiết
        int rowNum = 1;
        for (PhieuMuon phieu : dsPhieuMuon) {
            Row row = sheet.createRow(rowNum++);

            // Tính tổng số lượng mượn
            long totalQty = phieu.getChiTietList().stream()
                    .mapToInt(PhieuMuonThietBi::getSoLuongMuon).sum();

            row.createCell(0).setCellValue(phieu.getId());
            row.createCell(1).setCellValue(phieu.getMaPhieu());
            // Cần kiểm tra null cho ngày mượn, nhưng thường ngày mượn không null
            row.createCell(2).setCellValue(phieu.getNgayMuon().toString());
            row.createCell(3).setCellValue(phieu.getNguoiMuonText());
            row.createCell(4).setCellValue(totalQty);
            row.createCell(5).setCellValue(phieu.getTrangThai() ? "Đang Mượn" : "Đã Trả Hết");
        }

        // 5. Ghi Workbook ra Response OutputStream
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
