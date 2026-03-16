package com.akriti.apartment.repository;

import com.akriti.apartment.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByMonthAndYearOrderByDateAsc(int month, int year);
    List<Expense> findByYearOrderByDateAsc(int year);
}
