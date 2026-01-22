package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.entity.Expense;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ReportServiceTest {

    ReportService reportService = new ReportService();

    @Test
    void writePdf_writesBytes_andSetsHeaders() throws Exception {
        Expense e = new Expense();
        e.setDate(LocalDate.of(2026, 1, 1));
        e.setType("EXPENSE");
        e.setCategory("Food");
        e.setTitle("Burger");
        e.setAmount(new BigDecimal("100"));
        e.setDescription("Nice");

        MockHttpServletResponse res = new MockHttpServletResponse();

        reportService.writePdf(res, List.of(e));

        assertThat(res.getContentType()).isEqualTo("application/pdf");
        assertThat(res.getHeader("Content-Disposition")).contains("smartspend_report.pdf");
        assertThat(res.getContentAsByteArray().length).isGreaterThan(100);
    }

    @Test
    void writeExcel_writesBytes_andSetsHeaders() throws Exception {
        Expense e = new Expense();
        e.setDate(LocalDate.of(2026, 1, 1));
        e.setType("EXPENSE");
        e.setCategory("Food");
        e.setTitle("Burger");
        e.setAmount(new BigDecimal("100"));

        MockHttpServletResponse res = new MockHttpServletResponse();

        reportService.writeExcel(res, List.of(e));

        assertThat(res.getContentType()).contains("spreadsheetml.sheet");
        assertThat(res.getHeader("Content-Disposition")).contains("smartspend_report.xlsx");
        assertThat(res.getContentAsByteArray().length).isGreaterThan(100);
    }
}
