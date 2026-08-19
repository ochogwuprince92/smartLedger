package com.finance.smartLedger.security.config;

import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes the admin user only if it doesn't already exist.
 * 
 * Unlike the old AdminPasswordInitializer, this does NOT overwrite passwords.
 * It only creates the admin user on first startup if migrations haven't run.
 * This is a safety net for development environments where migrations might not run.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${admin.default-password:admin@me}")
  private String defaultAdminPassword;

  @Value("${admin.email:admin.smartledger@gmail.com}")
  private String adminEmail;

  @Override
  public void run(String... args) {
    try {
      // Only create admin user if it doesn't exist
      // NEVER reset passwords - this ensures password changes persist across restarts
      if (!userRepository.findByUsername("admin").isPresent()) {
        log.warn("Admin user not found. Creating admin user with default password.");
        
        User admin = new User("admin", adminEmail, passwordEncoder.encode(defaultAdminPassword));
        admin.setFirstName("Administrator");
        admin.setEnabled(true);
        admin.setAccountNonExpired(true);
        admin.setAccountNonLocked(true);
        admin.setCredentialsNonExpired(true);
        admin.setMustChangePassword(true);
        admin.setFailedLoginAttempts(0);
        admin.setCreatedBy("SYSTEM");
        admin.setUpdatedBy("SYSTEM");
        
        userRepository.save(admin);
        log.info("Admin user created successfully. Please change the password after first login.");
      } else {
        log.info("Admin user already exists. Skipping initialization.");
      }
    } catch (Exception e) {
      log.error("Error initializing admin user", e);
    }
  }
}
