package org.example.config;

import java.time.LocalDate;
import java.util.Set;

import org.example.entity.Role;
import org.example.entity.User;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminUserInitializer {

    @Bean
    public CommandLineRunner initAdminUser(UserRepository userRepo,
            PasswordEncoder encoder,
            RoleRepository roleRepo) {
        return args -> {
            String username = "admin";
            String rawPassword = "admin1234";

            if (!userRepo.existsByUsername(username)) {

                // Obtener o crear el rol ADMIN
                Role roleAdmin = roleRepo.findByName("ROLE_ADMIN")
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setName("ROLE_ADMIN");
                            return roleRepo.save(newRole);
                        });

                // Crear usuario admin
                User admin = new User();
                admin.setUsername(username);
                admin.setPassword(encoder.encode(rawPassword));
                admin.setEnabled(true);
                admin.setBirthDate(LocalDate.of(1990, 1, 1));
                admin.setDisplayName("Administrador del Sistema");
                admin.setEmail("admin@system.local");
                admin.setRoles(Set.of(roleAdmin));

                userRepo.save(admin);

                System.out.println("Usuario ADMIN creado correctamente.");
            } else {
                System.out.println("Usuario ADMIN ya existe.");
            }
        };
    }
}
