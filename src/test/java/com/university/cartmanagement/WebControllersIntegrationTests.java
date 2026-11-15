package com.university.cartmanagement;

import com.university.cartmanagement.model.Admin;
import com.university.cartmanagement.model.Role;
import com.university.cartmanagement.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebControllersIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / returns 200 OK and renders index view")
    void testHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("featuredProducts"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("GET /products returns 200 OK and renders product list")
    void testProductListPage() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("GET /products/1 returns 200 OK and renders product detail")
    void testProductDetailPage() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/detail"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    @DisplayName("GET /login returns 200 OK and renders login form")
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("GET /register returns 200 OK and renders registration form")
    void testRegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("GET /about returns 200 OK and renders architecture explanation")
    void testAboutPage() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
    }

    @Test
    @DisplayName("Unauthenticated access to /admin/dashboard redirects to /login")
    void testAdminSecurityGuardUnauthorized() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=admin_required"));
    }

    @Test
    @DisplayName("Authenticated Admin access to /admin/dashboard returns 200 OK")
    void testAdminDashboardAuthorized() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Admin admin = new Admin("admin", "admin@shoppingcart.com", "hash", "System Admin");
        admin.setId(1L);
        admin.setRole(Role.ADMIN);
        session.setAttribute("currentUser", admin);

        mockMvc.perform(get("/admin/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("stats"));
    }

    @Test
    @DisplayName("Authenticated Admin access to /admin/jdbc-comparison returns 200 OK")
    void testJdbcComparisonPage() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Admin admin = new Admin("admin", "admin@shoppingcart.com", "hash", "System Admin");
        admin.setId(1L);
        admin.setRole(Role.ADMIN);
        session.setAttribute("currentUser", admin);

        mockMvc.perform(get("/admin/jdbc-comparison").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/jdbc-comparison"))
                .andExpect(model().attributeExists("springDurationMs"))
                .andExpect(model().attributeExists("traditionalDurationMs"));
    }
}
