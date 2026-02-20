package com.malcolm.ecomai.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
@EnableWebSecurity
public class McpSecurityConfig {

    /**
     * Creates a logging filter to capture incoming HTTP requests.
     * Useful for debugging MCP protocol messages.
     */

    @Bean
    public FilterRegistrationBean<CommonsRequestLoggingFilter> requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(10000);
        loggingFilter.setAfterMessagePrefix("REQUEST DATA: ");
        FilterRegistrationBean<CommonsRequestLoggingFilter> bean = new FilterRegistrationBean<>(loggingFilter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    /**
     * Configures the security filter chain.
     * Currently allows all requests (permitAll) for easy local development.
     * In production, you would strip this out or enforce authentication.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // CSRF disabled for API usage
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()) // Require authentication for all requests
                .httpBasic(Customizer.withDefaults()) // Enable HTTP Basic Auth
                .build();
    }

    /**
     * Defines in-memory users for basic authentication (if enabled).
     * Currently unused due to permitAll() but ready for enabling security.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username(System.getenv("MCP_CLIENT_USER") != null ? System.getenv("MCP_CLIENT_USER") : "client-01")
                .password("{noop}"
                        + (System.getenv("MCP_API_KEY") != null ? System.getenv("MCP_API_KEY") : "ecom-secret-key-123")) // {noop}
                                                                                                                         // means
                                                                                                                         // plain
                                                                                                                         // text
                                                                                                                         // (dev
                                                                                                                         // only)
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}