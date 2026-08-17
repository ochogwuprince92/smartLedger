package com.finance.smartLedger.security.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.infrastructure.persistence.UserRepository;
import com.finance.smartLedger.shared.exception.BusinessException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private RoleService roleService;

  @Mock private PermissionService permissionService;

  @InjectMocks private UserService userService;

  private User testUser;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    testUser = new User("testuser", "test@example.com", "encodedOldPassword");
    testUser.setId(userId);
    testUser.setMustChangePassword(false);
  }

  @Test
  void adminResetPassword_generatesRandomPassword_setsFlagAndReturnsPlaintext() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.encode(any(String.class))).thenReturn("encodedNewPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    String tempPassword = userService.adminResetPassword(userId);

    assertNotNull(tempPassword);
    assertFalse(tempPassword.isEmpty());
    assertTrue(tempPassword.length() >= 12);
    verify(passwordEncoder).encode(tempPassword);
    verify(userRepository).save(testUser);
    assertTrue(testUser.getMustChangePassword());
  }

  @Test
  void adminResetPassword_userNotFound_throwsException() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> userService.adminResetPassword(userId));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void updatePassword_withMustChangePasswordTrue_skipsOldPasswordCheck_andClearsFlag() {
    testUser.setMustChangePassword(true);
    testUser.setPassword("encodedTempPassword");
    
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.encode(any(String.class))).thenReturn("encodedNewPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updatePassword(userId, "wrongOldPassword", "newPassword");

    assertNotNull(result);
    verify(passwordEncoder).encode("newPassword");
    verify(userRepository).save(testUser);
    assertFalse(testUser.getMustChangePassword());
  }

  @Test
  void updatePassword_withMustChangePasswordFalse_requiresOldPasswordMatch() {
    testUser.setMustChangePassword(false);
    testUser.setPassword("encodedOldPassword");
    
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("wrongOldPassword", "encodedOldPassword")).thenReturn(false);

    assertThrows(BusinessException.class, 
        () -> userService.updatePassword(userId, "wrongOldPassword", "newPassword"));
    
    verify(passwordEncoder).matches("wrongOldPassword", "encodedOldPassword");
    verify(passwordEncoder, never()).encode(any(String.class));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void updatePassword_withMustChangePasswordFalse_andCorrectOldPassword_succeeds() {
    testUser.setMustChangePassword(false);
    testUser.setPassword("encodedOldPassword");
    
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("correctOldPassword", "encodedOldPassword")).thenReturn(true);
    when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updatePassword(userId, "correctOldPassword", "newPassword");

    assertNotNull(result);
    verify(passwordEncoder).matches("correctOldPassword", "encodedOldPassword");
    verify(passwordEncoder).encode("newPassword");
    verify(userRepository).save(testUser);
    assertFalse(testUser.getMustChangePassword());
  }

  @Test
  void updatePassword_withMustChangePasswordFalse_flagRemainsFalse() {
    testUser.setMustChangePassword(false);
    testUser.setPassword("encodedOldPassword");
    
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("correctOldPassword", "encodedOldPassword")).thenReturn(true);
    when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    userService.updatePassword(userId, "correctOldPassword", "newPassword");

    assertFalse(testUser.getMustChangePassword());
  }
}
