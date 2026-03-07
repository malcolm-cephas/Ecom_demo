package com.malcolm.ecomauth.config;

import com.malcolm.ecomauth.model.User;
import com.malcolm.ecomauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner initializer() {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(User.builder().username("user").password(passwordEncoder.encode("password"))
                        .roles("USER").build());
                userRepository.save(User.builder().username("malcolm").password(passwordEncoder.encode("password"))
                        .roles("USER").build());
                userRepository.save(User.builder().username("testuser").password(passwordEncoder.encode("password"))
                        .roles("USER").build());
                System.out.println("Default users seeded into database.");
            }
        };
    }
}
