package com.university.cartmanagement.config;

import com.university.cartmanagement.model.Category;
import com.university.cartmanagement.model.User;
import com.university.cartmanagement.service.CartService;
import com.university.cartmanagement.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC Configuration providing:
 * 1. Global model attributes for templates (currentUser, cartCount, categories)
 * 2. Admin security interceptor enforcing role checks
 * 3. Static resource mappings
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CategoryService categoryService;
    private final CartService cartService;

    public WebMvcConfig(CategoryService categoryService, CartService cartService) {
        this.categoryService = categoryService;
        this.cartService = cartService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Global attribute interceptor
        registry.addInterceptor(new GlobalAttributeInterceptor());

        // Admin authentication & authorization guard
        registry.addInterceptor(new AdminAuthInterceptor())
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * Injects common model attributes into every view rendering.
     */
    private class GlobalAttributeInterceptor implements HandlerInterceptor {
        @Override
        public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                               @NonNull Object handler, ModelAndView modelAndView) {
            if (modelAndView != null && !isRedirectView(modelAndView)) {
                HttpSession session = request.getSession(false);
                User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
                modelAndView.addObject("currentUser", currentUser);

                int cartCount = 0;
                if (currentUser != null) {
                    try {
                        cartCount = cartService.getCartItemCount(currentUser.getId());
                    } catch (Exception ignored) {}
                }
                modelAndView.addObject("cartCount", cartCount);

                try {
                    List<Category> globalCategories = categoryService.getAllCategories();
                    modelAndView.addObject("globalCategories", globalCategories);
                } catch (Exception ignored) {}

                modelAndView.addObject("currentUri", request.getRequestURI());
            }
        }

        private boolean isRedirectView(ModelAndView mv) {
            String viewName = mv.getViewName();
            return viewName != null && viewName.startsWith("redirect:");
        }
    }

    /**
     * Interceptor ensuring only logged-in users with ADMIN role can access /admin/** paths.
     */
    private static class AdminAuthInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler) throws Exception {
            HttpSession session = request.getSession(false);
            User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;

            if (currentUser == null) {
                session = request.getSession(true);
                session.setAttribute("returnUrl", request.getRequestURI());
                response.sendRedirect("/login?error=admin_required");
                return false;
            }

            if (!currentUser.isAdmin()) {
                response.sendRedirect("/?error=forbidden");
                return false;
            }

            return true;
        }
    }
}
