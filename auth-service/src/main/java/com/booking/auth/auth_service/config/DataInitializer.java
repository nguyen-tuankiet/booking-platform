package com.booking.auth.auth_service.config;

import com.booking.auth.auth_service.entity.Role;
import com.booking.auth.auth_service.repository.RoleRepository;
import com.booking.auth.auth_service.utils.RoleName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
    }

    private void initializeRoles() {
        log.info("Initializing roles...");
        
        List<RoleName> roleNames = Arrays.asList(
                RoleName.ROLE_USER,
                RoleName.ROLE_ADMIN,
                RoleName.ROLE_MODERATOR,
                RoleName.ROLE_CUSTOMER_SUPPORT
        );

        for (RoleName roleName : roleNames) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description("Default " + roleName.name() + " role")
                        .build();
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            } else {
                log.info("Role already exists: {}", roleName);
            }
        }
        
        log.info("Role initialization completed.");
    }
} 