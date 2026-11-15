package com.university.cartmanagement;

import com.university.cartmanagement.model.Product;
import com.university.cartmanagement.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TraditionalJdbcProductRepositoryTests {

    @Autowired
    @Qualifier("springJdbcProductRepository")
    private ProductRepository springJdbcRepo;

    @Autowired
    @Qualifier("traditionalJdbcProductRepository")
    private ProductRepository traditionalJdbcRepo;

    @Test
    @DisplayName("Verify Traditional JDBC and Spring JDBC retrieve identical product records")
    void testTraditionalVsSpringJdbcParity() {
        assertNotNull(springJdbcRepo);
        assertNotNull(traditionalJdbcRepo);

        // 1. Compare total count
        int springCount = springJdbcRepo.count();
        int traditionalCount = traditionalJdbcRepo.count();
        assertEquals(springCount, traditionalCount, "Both repositories must report identical product counts");

        // 2. Compare findById
        Optional<Product> springP1 = springJdbcRepo.findById(1L);
        Optional<Product> traditionalP1 = traditionalJdbcRepo.findById(1L);

        assertTrue(springP1.isPresent());
        assertTrue(traditionalP1.isPresent());
        assertEquals(springP1.get().getName(), traditionalP1.get().getName());
        assertEquals(springP1.get().getPrice(), traditionalP1.get().getPrice());

        // 3. Compare keyword search
        List<Product> springSearch = springJdbcRepo.search("book");
        List<Product> traditionalSearch = traditionalJdbcRepo.search("book");
        assertEquals(springSearch.size(), traditionalSearch.size());
    }
}
