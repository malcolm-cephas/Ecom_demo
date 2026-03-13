package com.malcolm.ecomauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import java.util.ArrayList;
import java.util.Arrays;
import com.malcolm.ecomauth.model.AppUser;
import com.malcolm.ecomauth.model.AppUserRole;
import com.malcolm.ecomauth.service.UserService;

@SpringBootApplication
public class EcomAuthServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcomAuthServerApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public CommandLineRunner runner(UserService userService) {
        return args -> {
            try {
                userService.search("admin");
            } catch (Exception e) {
                // If admin doesn't exist, create default users
                AppUser admin = new AppUser();
                admin.setUsername("admin");
                admin.setPassword("admin1234");
                admin.setEmail("admin@email.com");
                admin.setAppUserRoles(new ArrayList<>(Arrays.asList(AppUserRole.ROLE_ADMIN)));
                userService.signup(admin);

                AppUser client = new AppUser();
                client.setUsername("client");
                client.setPassword("client1234");
                client.setEmail("client@email.com");
                client.setAppUserRoles(new ArrayList<>(Arrays.asList(AppUserRole.ROLE_CLIENT)));
                userService.signup(client);
            }
        };
    }
}
