package com.malcolm.ecomai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the MCP Client.
 * This module contains the AI chat logic and connects to the MCP Server.
 */
@SpringBootApplication
public class McpClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
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
                System.out.println("[MCP-CLIENT-REQUEST] " + req.getMethod() + " " + req.getRequestURI());
                chain.doFilter(request, response);
            }
        });
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

}
