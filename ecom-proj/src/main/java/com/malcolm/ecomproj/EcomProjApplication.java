package com.malcolm.ecomproj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Ecommerce Backend (REST API).
 * This service handles products, orders, and user data.
 */
@SpringBootApplication
public class EcomProjApplication {

    public static void main(String[] args) {
        // Load .env variables into System properties so Spring Boot can see them
        try {
            io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
        } catch (Exception e) {
            System.err.println("Warning: Could not load .env file. Using system environment variables instead.");
        }

        // Starts the Spring Boot backend
        SpringApplication.run(EcomProjApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<jakarta.servlet.Filter> logFilter() {
        org.springframework.boot.web.servlet.FilterRegistrationBean<jakarta.servlet.Filter> registrationBean = new org.springframework.boot.web.servlet.FilterRegistrationBean<>();

        registrationBean.setFilter(new jakarta.servlet.Filter() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
                    jakarta.servlet.FilterChain chain)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                jakarta.servlet.http.HttpServletRequest req = (jakarta.servlet.http.HttpServletRequest) request;
                System.out.println("[BACKEND-REQUEST] " + req.getMethod() + " " + req.getRequestURI());
                chain.doFilter(request, response);
            }
        });
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

}
