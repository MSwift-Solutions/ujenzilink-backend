package com.ujenzilink.ujenzilink_backend.auth.admin.services;

import com.ujenzilink.ujenzilink_backend.auth.admin.AdminUser;
import com.ujenzilink.ujenzilink_backend.auth.admin.repos.AdminUserRepository;
import com.ujenzilink.ujenzilink_backend.auth.enums.Roles;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements ApplicationRunner {

    private static final String ADMIN_EMAIL    = "admin@ujenzilink.com";
    private static final String ADMIN_PASSWORD = "Admin@2024!Secure";
    private static final String ADMIN_NAME     = "Super Admin";

//    {
//        "email": "admin@ujenzilink.com",
//            "password": "Admin@2024!Secure"
//    }
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!adminUserRepository.existsByEmail(ADMIN_EMAIL)) {
            AdminUser admin = new AdminUser();
            admin.setEmail(ADMIN_EMAIL);
            admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setName(ADMIN_NAME);
            admin.setRole(Roles.ROLE_SUPER_ADMIN);
            admin.setIsEnabled(true);
            adminUserRepository.save(admin);
            System.out.println("[AdminSeeder] Default admin account created: " + ADMIN_EMAIL);
        }
    }
}
