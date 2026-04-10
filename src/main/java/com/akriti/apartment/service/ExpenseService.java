package com.akriti.apartment.service;

import com.akriti.apartment.dto.ExpenseRequest;
import com.akriti.apartment.entity.Expense;
import com.akriti.apartment.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    public List<Expense> getByMonth(int month, int year) {
        return expenseRepository.findByMonthAndYearOrderByDateAsc(month, year);
    }

    public List<Expense> getByYear(int year) {
        return expenseRepository.findByYearOrderByDateAsc(year);
    }

    public int getAllTimeExpenses() {
        return expenseRepository.findAll().stream()
                .mapToInt(Expense::getAmount).sum();
    }

    public Expense create(ExpenseRequest req) {
        LocalDate date = req.getDate() != null ? req.getDate() : LocalDate.now();
        Expense expense = Expense.builder()
            .description(req.getDescription())
            .category(Expense.Category.valueOf(req.getCategory().toUpperCase()))
            .amount(req.getAmount())
            .date(date)
            .month(date.getMonthValue())
            .year(date.getYear())
            .addedBy("Admin")
            .build();
        return expenseRepository.save(expense);
    }

    public void delete(Long id) {
        expenseRepository.deleteById(id);
    }

    public Map<String, Object> getMonthlySummary(int month, int year) {
        List<Expense> expenses = expenseRepository.findByMonthAndYearOrderByDateAsc(month, year);
        int total = expenses.stream().mapToInt(Expense::getAmount).sum();
        Map<String, Integer> byCategory = expenses.stream()
            .collect(Collectors.groupingBy(
                e -> e.getCategory().name(),
                Collectors.summingInt(Expense::getAmount)
            ));
        return Map.of("total", total, "byCategory", byCategory, "expenses", expenses);
    }
}
