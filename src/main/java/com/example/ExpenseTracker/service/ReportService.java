package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.entity.Expense;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.List;

@Service
public class ReportService {

    public void writePdf(HttpServletResponse response, List<Expense> list) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=smartspend_report.pdf");

        OutputStream os = response.getOutputStream();
        PdfWriter writer = new PdfWriter(os);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        doc.add(new Paragraph("SmartSpend Report"));

        Table t = new Table(new float[]{2, 2, 2, 2, 2, 4});
        t.addCell("Date");
        t.addCell("Type");
        t.addCell("Category");
        t.addCell("Title");
        t.addCell("Amount");
        t.addCell("Description");

        for (Expense e : list) {
            t.addCell(String.valueOf(e.getDate()));
            t.addCell(e.getType());
            t.addCell(e.getCategory());
            t.addCell(e.getTitle());
            t.addCell(String.valueOf(e.getAmount()));
            t.addCell(e.getDescription() == null ? "" : e.getDescription());
        }

        doc.add(t);
        doc.close();
    }

    public void writeExcel(HttpServletResponse response, List<Expense> list) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=smartspend_report.xlsx");

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            var sheet = wb.createSheet("Report");

            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("Date");
            header.createCell(1).setCellValue("Type");
            header.createCell(2).setCellValue("Category");
            header.createCell(3).setCellValue("Title");
            header.createCell(4).setCellValue("Amount");
            header.createCell(5).setCellValue("Description");

            int r = 1;
            for (Expense e : list) {
                var row = sheet.createRow(r++);
                row.createCell(0).setCellValue(String.valueOf(e.getDate()));
                row.createCell(1).setCellValue(e.getType());
                row.createCell(2).setCellValue(e.getCategory());
                row.createCell(3).setCellValue(e.getTitle());
                row.createCell(4).setCellValue(String.valueOf(e.getAmount()));
                row.createCell(5).setCellValue(e.getDescription() == null ? "" : e.getDescription());
            }

            for (int i = 0; i <= 5; i++) sheet.autoSizeColumn(i);

            wb.write(response.getOutputStream());
        }
    }
}
