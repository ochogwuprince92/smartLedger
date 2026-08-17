package com.finance.smartLedger.security.config;

import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminPasswordInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    try {
      userRepository.findByUsername("admin").ifPresent(admin -> {
        String correctHash = passwordEncoder.encode("admin");
        
        if (!passwordEncoder.matches("admin", admin.getPassword())) {
          log.warn("Admin password mismatch detected. Updating to correct password hash.");
          admin.setPassword(correctHash);
          userRepository.save(admin);
          log.info("Admin password has been reset to 'admin'");
        } else {
          log.info("Admin password is correct");
        }
      });
    } catch (Exception e) {
      log.error("Error initializing admin password", e);
    }
  }
}
