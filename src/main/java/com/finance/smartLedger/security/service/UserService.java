package com.finance.smartLedger.security.service;

import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.infrastructure.persistence.UserRepository;
import com.finance.smartLedger.shared.exception.BusinessException;
import com.finance.smartLedger.shared.exception.ErrorCodes;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleService roleService;
  private final PermissionService permissionService;

  public User createUser(String username, String email, String password) {
    if (userRepository.existsByUsername(username)) {
      throw new BusinessException(ErrorCodes.CONFLICT, "Username already exists");
    }
    if (userRepository.existsByEmail(email)) {
      throw new BusinessException(ErrorCodes.CONFLICT, "Email already exists");
    }

    User user = new User(username, email, passwordEncoder.encode(password));
    user.setEnabled(true);
    user.setCreatedBy("SYSTEM");
    user.setUpdatedBy("SYSTEM");
    return userRepository.save(user);
  }

  public User createUser(
      String username, String email, String password, String firstName, String lastName) {
    User user = createUser(username, email, password);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    return userRepository.save(user);
  }

  public User updateUser(UUID userId, String firstName, String lastName, String phone) {
    User user = getUserById(userId);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setPhone(phone);
    return userRepository.save(user);
  }

  public User updatePassword(UUID userId, String oldPassword, String newPassword) {
    User user = getUserById(userId);
    
    if (Boolean.TRUE.equals(user.getMustChangePassword())) {
      user.setPassword(passwordEncoder.encode(newPassword));
      user.setMustChangePassword(false);
      return userRepository.save(user);
    }
    
    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw new BusinessException(ErrorCodes.BAD_REQUEST, "Current password is incorrect");
    }
    user.setPassword(passwordEncoder.encode(newPassword));
    return userRepository.save(user);
  }

  public String adminResetPassword(UUID userId) {
    User user = getUserById(userId);
    
    String tempPassword = generateSecureRandomPassword();
    user.setPassword(passwordEncoder.encode(tempPassword));
    user.setMustChangePassword(true);
    userRepository.save(user);
    
    return tempPassword;
  }

  private String generateSecureRandomPassword() {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    SecureRandom random = new SecureRandom();
    StringBuilder password = new StringBuilder();
    
    for (int i = 0; i < 16; i++) {
      password.append(chars.charAt(random.nextInt(chars.length())));
    }
    
    return password.toString();
  }

  public void enableUser(UUID userId) {
    User user = getUserById(userId);
    user.setEnabled(true);
    userRepository.save(user);
  }

  public void disableUser(UUID userId) {
    User user = getUserById(userId);
    user.setEnabled(false);
    userRepository.save(user);
  }

  public void lockUser(UUID userId) {
    User user = getUserById(userId);
    user.setAccountNonLocked(false);
    userRepository.save(user);
  }

  public void unlockUser(UUID userId) {
    User user = getUserById(userId);
    user.setAccountNonLocked(true);
    user.setLockedUntil(null);
    user.setFailedLoginAttempts(0);
    userRepository.save(user);
  }

  public void deleteUser(UUID userId) {
    User user = getUserById(userId);
    user.softDelete();
    userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public User getUserById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "User not found"));
  }

  @Transactional(readOnly = true)
  public User getUserByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "User not found"));
  }

  @Transactional(readOnly = true)
  public User getUserByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "User not found"));
  }

  @Transactional(readOnly = true)
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public User grantRole(UUID userId, UUID roleId) {
    User user = getUserById(userId);
    var role = roleService.getRoleById(roleId);
    user.grantRole(role);
    return userRepository.save(user);
  }

  public User revokeRole(UUID userId, UUID roleId) {
    User user = getUserById(userId);
    var role = roleService.getRoleById(roleId);
    user.revokeRole(role);
    return userRepository.save(user);
  }

  public User grantPermission(UUID userId, UUID permissionId) {
    User user = getUserById(userId);
    var permission = permissionService.getPermissionById(permissionId);
    user.grantPermission(permission);
    return userRepository.save(user);
  }

  public User revokePermission(UUID userId, UUID permissionId) {
    User user = getUserById(userId);
    var permission = permissionService.getPermissionById(permissionId);
    user.revokePermission(permission);
    return userRepository.save(user);
  }

  public User recordSuccessfulLogin(String username) {
    User user = getUserByUsername(username);
    user.recordSuccessfulLogin();
    return userRepository.save(user);
  }
}
