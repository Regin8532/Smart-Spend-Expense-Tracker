package com.example.ExpenseTracker.controller;

import com.example.ExpenseTracker.dto.BudgetDto;
import com.example.ExpenseTracker.entity.Budget;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.repository.BudgetRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BudgetControllerUnitTest {

    @Mock BudgetRepository budgetRepo;
    @Mock UserRepository userRepo;
    @Mock HttpSession session;
    @Mock Model model;
    @Mock BindingResult br;

    BudgetController controller;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        controller = new BudgetController(budgetRepo, userRepo);
        when(session.getAttribute("USER_ID")).thenReturn(1L);
    }

    @Test
    void page_whenMonthNull_setsCurrentMonth_returnsListView() {
        when(budgetRepo.findByUserIdAndMonth(eq(1L), anyString())).thenReturn(List.of());

        String view = controller.page(null, null, session, model);

        assertThat(view).isEqualTo("budgets/list");
        verify(model).addAttribute(eq("month"), anyString());
        verify(model).addAttribute(eq("budgets"), anyList());
        verify(model).addAttribute(eq("budgetDto"), any(BudgetDto.class));
    }

    @Test
    void page_withEditId_owner_setsEditIdAndDto() {
        Budget b = new Budget();
//        b.setId(5L);
        b.setMonth("2026-01");
        b.setCategory("Food");
        b.setLimitAmount(new BigDecimal("1500"));
        User owner = new User();
        owner.setId(1L);
        b.setUser(owner);

        when(budgetRepo.findByUserIdAndMonth(1L, "2026-01")).thenReturn(List.of(b));
        when(budgetRepo.findById(5L)).thenReturn(Optional.of(b));

        String view = controller.page("2026-01", 5L, session, model);

        assertThat(view).isEqualTo("budgets/list");
        verify(model).addAttribute("editId", 5L);
    }

    @Test
    void page_withEditId_notOwner_redirectsForbidden() {
        Budget b = new Budget();
//        b.setId(5L);
        User other = new User();
        other.setId(99L);
        b.setUser(other);

        when(budgetRepo.findByUserIdAndMonth(1L, "2026-01")).thenReturn(List.of());
        when(budgetRepo.findById(5L)).thenReturn(Optional.of(b));

        String view = controller.page("2026-01", 5L, session, model);

        assertThat(view).isEqualTo("redirect:/budgets?forbidden");
    }

    @Test
    void save_whenErrors_redirectsMonth() {
        when(br.hasErrors()).thenReturn(true);

        BudgetDto dto = new BudgetDto();
        dto.setMonth("2026-01");

        String view = controller.save(dto, br, session, model);

        assertThat(view).isEqualTo("redirect:/budgets?month=2026-01");
        verifyNoInteractions(userRepo);
    }

    @Test
    void save_createsNewBudget_andRedirectsSaved() {
        when(br.hasErrors()).thenReturn(false);

        User u = new User();
        u.setId(1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(u));
        when(budgetRepo.findByUserIdAndMonthAndCategory(1L, "2026-01", "Food"))
                .thenReturn(Optional.empty());

        BudgetDto dto = new BudgetDto();
        dto.setMonth("2026-01");
        dto.setCategory("Food");
        dto.setLimitAmount(new BigDecimal("1500"));

        String view = controller.save(dto, br, session, model);

        assertThat(view).isEqualTo("redirect:/budgets?month=2026-01&saved");
        verify(budgetRepo).save(any(Budget.class));
    }

    @Test
    void delete_notOwner_redirectsForbidden() {
        Budget b = new Budget();
//        b.setId(7L);
        b.setMonth("2026-01");
        User other = new User();
        other.setId(99L);
        b.setUser(other);

        when(budgetRepo.findById(7L)).thenReturn(Optional.of(b));

        String view = controller.delete(7L, session);

        assertThat(view).isEqualTo("redirect:/budgets?forbidden");
        verify(budgetRepo, never()).deleteById(anyLong());
    }

    @Test
    void delete_owner_deletes_andRedirectsDeleted() {
        Budget b = new Budget();
//        b.setId(7L);
        b.setMonth("2026-01");
        User owner = new User();
        owner.setId(1L);
        b.setUser(owner);

        when(budgetRepo.findById(7L)).thenReturn(Optional.of(b));

        String view = controller.delete(7L, session);

        assertThat(view).isEqualTo("redirect:/budgets?month=2026-01&deleted");
        verify(budgetRepo).deleteById(7L);
    }
}
