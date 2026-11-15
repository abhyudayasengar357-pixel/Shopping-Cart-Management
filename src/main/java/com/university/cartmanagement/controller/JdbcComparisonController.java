package com.university.cartmanagement.controller;

import com.university.cartmanagement.model.Product;
import com.university.cartmanagement.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller providing an educational, interactive benchmark and comparison view
 * between Traditional JDBC and Spring JDBC (JdbcTemplate).
 *
 * Fulfills University Case Study Requirement:
 * "Demonstrate the difference between traditional JDBC and Spring JDBC"
 */
@Controller
@RequestMapping("/admin/jdbc-comparison")
public class JdbcComparisonController {

    private final ProductRepository springJdbcProductRepository;
    private final ProductRepository traditionalJdbcProductRepository;

    public JdbcComparisonController(
            @Qualifier("springJdbcProductRepository") ProductRepository springJdbcProductRepository,
            @Qualifier("traditionalJdbcProductRepository") ProductRepository traditionalJdbcProductRepository) {
        this.springJdbcProductRepository = springJdbcProductRepository;
        this.traditionalJdbcProductRepository = traditionalJdbcProductRepository;
    }

    @GetMapping
    public String showComparison(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            Model model) {

        // 1. Benchmark Spring JDBC (JdbcTemplate)
        long startSpring = System.nanoTime();
        List<Product> springResults = keyword.isBlank() ?
                springJdbcProductRepository.findAll() :
                springJdbcProductRepository.search(keyword);
        long endSpring = System.nanoTime();
        double springDurationMs = (endSpring - startSpring) / 1_000_000.0;

        // 2. Benchmark Traditional JDBC (DriverManager, Connection, PreparedStatement, ResultSet)
        long startTraditional = System.nanoTime();
        List<Product> traditionalResults = keyword.isBlank() ?
                traditionalJdbcProductRepository.findAll() :
                traditionalJdbcProductRepository.search(keyword);
        long endTraditional = System.nanoTime();
        double traditionalDurationMs = (endTraditional - startTraditional) / 1_000_000.0;

        // Model attributes for template rendering
        model.addAttribute("keyword", keyword);
        model.addAttribute("springResultsCount", springResults.size());
        model.addAttribute("traditionalResultsCount", traditionalResults.size());
        model.addAttribute("springDurationMs", String.format("%.3f", springDurationMs));
        model.addAttribute("traditionalDurationMs", String.format("%.3f", traditionalDurationMs));
        model.addAttribute("sampleProducts", springResults.stream().limit(5).toList());

        return "admin/jdbc-comparison";
    }
}
