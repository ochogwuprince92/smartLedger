package com.finance.smartLedger.security.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.security.domain.Permission;
import com.finance.smartLedger.security.domain.Role;
import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.service.UserService;
import com.finance.smartLedger.shared.security.SecurityContext;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionEvaluator Tests")
class PermissionEvaluatorTest {

  @Mock private UserService userService;

  private PermissionEvaluator permissionEvaluator;
  private User testUser;

  @BeforeEach
  void setUp() {
    permissionEvaluator = new PermissionEvaluator(userService);

    testUser = new User("john.doe", "john@example.com", "password");
    testUser.setId(UUID.randomUUID());
  }

  @AfterEach
  void tearDown() {
    SecurityContext.clear();
  }

  @Test
  @DisplayName("Should return true when user has permission")
  void shouldReturnTrueWhenUserHasPermission() {
    Permission permission = new Permission("READ_ACCOUNT", "Read Account", "account:read");
    testUser.grantPermission(permission);

    when(userService.getUserById(any(UUID.class))).thenReturn(testUser);
    SecurityContext.setUserContext(
        SecurityContext.UserContext.of(testUser.getId().toString(), testUser.getUsername()));

    boolean result = permissionEvaluator.hasPermission("READ_ACCOUNT");

    assertTrue(result);
  }

  @Test
  @DisplayName("Should return false when user does not have permission")
  void shouldReturnFalseWhenUserDoesNotHavePermission() {
    when(userService.getUserById(any(UUID.class))).thenReturn(testUser);
    SecurityContext.setUserContext(
        SecurityContext.UserContext.of(testUser.getId().toString(), testUser.getUsername()));

    boolean result = permissionEvaluator.hasPermission("READ_ACCOUNT");

    assertFalse(result);
  }

  @Test
  @DisplayName("Should return false when no user context is set")
  void shouldReturnFalseWhenNoUserContextIsSet() {
    boolean result = permissionEvaluator.hasPermission("READ_ACCOUNT");

    assertFalse(result);
  }

  @Test
  @DisplayName("Should return false when user service throws exception")
  void shouldReturnFalseWhenUserServiceThrowsException() {
    SecurityContext.setUserContext(SecurityContext.UserContext.of("user123", "john.doe"));
    lenient()
        .when(userService.getUserById(any(UUID.class)))
        .thenThrow(new RuntimeException("User not found"));

    boolean result = permissionEvaluator.hasPermission("READ_ACCOUNT");

    assertFalse(result);
  }

  @Test
  @DisplayName("Should return true when user has role")
  void shouldReturnTrueWhenUserHasRole() {
    Role role = new Role("ADMIN", "Administrator");
    testUser.grantRole(role);

    when(userService.getUserById(any(UUID.class))).thenReturn(testUser);
    SecurityContext.setUserContext(
        SecurityContext.UserContext.of(testUser.getId().toString(), testUser.getUsername()));

    boolean result = permissionEvaluator.hasRole("ADMIN");

    assertTrue(result);
  }

  @Test
  @DisplayName("Should return false when user does not have role")
  void shouldReturnFalseWhenUserDoesNotHaveRole() {
    when(userService.getUserById(any(UUID.class))).thenReturn(testUser);
    SecurityContext.setUserContext(
        SecurityContext.UserContext.of(testUser.getId().toString(), testUser.getUsername()));

    boolean result = permissionEvaluator.hasRole("ADMIN");

    assertFalse(result);
  }

  @Test
  @DisplayName("Should return true when user has any of the specified permissions")
  void shouldReturnTrueWhenUserHasAnyOfTheSpecifiedPermissions() {
    Permission permission1 = new Permission("READ_ACCOUNT", "Read Account", "account:read");
    testUser.grantPermission(permission1);

    when(userService.getUserById(any(UUID.class))).thenReturn(testUser);
    SecurityContext.setUserContext(
        SecurityContext.UserContext.of(testUser.getId().toString(), testUser.getUsername()));

    boolean result = permissionEvaluator.hasAnyPermission("READ_ACCOUNT", "WRITE_ACCOUNT");

    assertTrue(result);
  }

  @Test
  @DisplayName("Should return false when user has none of the specified permissions")
  void shouldReturnFalseWhenUserHasNoneOfTheSpecifiedPermissions() {
    when(userService.getUserById(any(UUID.class))).thenReturn(testUser);
    SecurityContext.setUserContext(
        SecurityContext.UserContext.of(testUser.getId().toString(), testUser.getUsername()));

    boolean result = permissionEvaluator.hasAnyPermission("READ_ACCOUNT", "WRITE_ACCOUNT");

    assertFalse(result);
  }

  @Test
  @DisplayName("Should return true when user has all specified permissions")
  void shouldReturnTrueWhenUserHasAllSpecifiedPermissions() {
    Permission permission1 = new Permission("READ_ACCOUNT", "Read Account", "account:read");
    Permission permission2 = new Permission("WRITE_ACCOUNT", "Write Account", "account:write");
    testUser.grantPermission(permission1);
    testUser.grantPermission(permission2);

    when(userService.getUserById(any(UUID.class))).thenReturn(testUser);
    SecurityContext.setUserContext(
        SecurityContext.UserContext.of(testUser.getId().toString(), testUser.getUsername()));

    boolean result = permissionEvaluator.hasAllPermissions("READ_ACCOUNT", "WRITE_ACCOUNT");

    assertTrue(result);
  }

  @Test
  @DisplayName("Should return false when user does not have all specified permissions")
  void shouldReturnFalseWhenUserDoesNotHaveAllSpecifiedPermissions() {
    Permission permission1 = new Permission("READ_ACCOUNT", "Read Account", "account:read");
    testUser.grantPermission(permission1);

    when(userService.getUserById(any(UUID.class))).thenReturn(testUser);
    SecurityContext.setUserContext(
        SecurityContext.UserContext.of(testUser.getId().toString(), testUser.getUsername()));

    boolean result = permissionEvaluator.hasAllPermissions("READ_ACCOUNT", "WRITE_ACCOUNT");

    assertFalse(result);
  }

  @Test
  @DisplayName("Should check permissions through role hierarchy")
  void shouldCheckPermissionsThroughRoleHierarchy() {
    Permission permission = new Permission("READ_ACCOUNT", "Read Account", "account:read");
    Role childRole = new Role("USER", "User");
    childRole.grantPermission(permission);
    Role parentRole = new Role("ADMIN", "Administrator");
    parentRole.addChildRole(childRole);
    testUser.grantRole(parentRole);

    when(userService.getUserById(any(UUID.class))).thenReturn(testUser);
    SecurityContext.setUserContext(
        SecurityContext.UserContext.of(testUser.getId().toString(), testUser.getUsername()));

    boolean result = permissionEvaluator.hasPermission("READ_ACCOUNT");

    assertTrue(result);
  }

  @Test
  @DisplayName("Should handle empty permission list")
  void shouldHandleEmptyPermissionList() {
    when(userService.getUserById(any(UUID.class))).thenReturn(testUser);
    SecurityContext.setUserContext(
        SecurityContext.UserContext.of(testUser.getId().toString(), testUser.getUsername()));

    boolean result = permissionEvaluator.hasAnyPermission();

    assertFalse(result);
  }

  @Test
  @DisplayName("Should verify user service is called")
  void shouldVerifyUserServiceIsCalled() {
    when(userService.getUserById(any(UUID.class))).thenReturn(testUser);
    SecurityContext.setUserContext(
        SecurityContext.UserContext.of(testUser.getId().toString(), testUser.getUsername()));

    permissionEvaluator.hasPermission("READ_ACCOUNT");

    verify(userService, times(1)).getUserById(testUser.getId());
  }
}
