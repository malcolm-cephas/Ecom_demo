package com.malcolm.ecomauth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Disable CSRF (cross site request forgery)
        http.csrf(csrf -> csrf.disable());

        // Standard security for user management and protocol fallbacks
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/users/signin").permitAll()
                .requestMatchers("/users/signup").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/error").permitAll()
                // Disallow everything else
                .anyRequest().authenticated());

        // Enable standard form login for the OIDC authorization flow
        http.formLogin(Customizer.withDefaults());

        // Apply JWT for existing custom endpoints (optional but maintained for compatibility)
        http.addFilterBefore(new JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/v2/api-docs")
                .requestMatchers("/swagger-resources/**")
                .requestMatchers("/swagger-ui.html")
                .requestMatchers("/configuration/**")
                .requestMatchers("/webjars/**")
                .requestMatchers("/public")
                .requestMatchers("/h2-console/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
