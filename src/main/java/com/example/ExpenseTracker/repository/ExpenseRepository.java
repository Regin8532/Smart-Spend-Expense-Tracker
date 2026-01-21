package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
      select e from Expense e
      where e.user.id = :userId
        and (:type is null or e.type = :type)
        and (:category is null or e.category = :category)
        and (:fromDate is null or e.date >= :fromDate)
        and (:toDate is null or e.date <= :toDate)
        and (:minAmount is null or e.amount >= :minAmount)
        and (:maxAmount is null or e.amount <= :maxAmount)
      order by
        case when :sort = 'amountAsc' then e.amount end asc,
        case when :sort = 'amountDesc' then e.amount end desc,
        case when :sort = 'dateAsc' then e.date end asc,
        case when :sort = 'dateDesc' then e.date end desc,
        e.id desc
    """)
    List<Expense> filter(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("category") String category,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("sort") String sort
    );

    @Query("""
      select coalesce(sum(e.amount), 0) from Expense e
      where e.user.id = :userId
        and e.type = 'EXPENSE'
        and e.category = :category
        and e.date >= :from and e.date <= :to
    """)
    BigDecimal sumCategoryExpenseInRange(Long userId, String category, LocalDate from, LocalDate to);

    @Query("""
      select coalesce(sum(e.amount), 0) from Expense e
      where e.user.id = :userId
        and e.type = :type
        and e.date >= :from and e.date <= :to
    """)
    BigDecimal sumByTypeInRange(Long userId, String type, LocalDate from, LocalDate to);
}
