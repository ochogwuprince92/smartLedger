package com.finance.smartLedger.security.application;

import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.service.UserService;
import com.finance.smartLedger.shared.security.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionEvaluator {

  private final UserService userService;

  public boolean hasPermission(String permissionCode) {
    return SecurityContext.getUserContext()
        .map(
            userContext -> {
              try {
                User user =
                    userService.getUserById(java.util.UUID.fromString(userContext.userId()));
                return user.hasPermission(permissionCode);
              } catch (Exception e) {
                return false;
              }
            })
        .orElse(false);
  }

  public boolean hasRole(String roleCode) {
    return SecurityContext.getUserContext()
        .map(
            userContext -> {
              try {
                User user =
                    userService.getUserById(java.util.UUID.fromString(userContext.userId()));
                return user.getRoles().stream().anyMatch(role -> role.getCode().equals(roleCode));
              } catch (Exception e) {
                return false;
              }
            })
        .orElse(false);
  }

  public boolean hasAnyPermission(String... permissionCodes) {
    return SecurityContext.getUserContext()
        .map(
            userContext -> {
              try {
                User user =
                    userService.getUserById(java.util.UUID.fromString(userContext.userId()));
                for (String code : permissionCodes) {
                  if (user.hasPermission(code)) return true;
                }
                return false;
              } catch (Exception e) {
                return false;
              }
            })
        .orElse(false);
  }

  public boolean hasAllPermissions(String... permissionCodes) {
    return SecurityContext.getUserContext()
        .map(
            userContext -> {
              try {
                User user =
                    userService.getUserById(java.util.UUID.fromString(userContext.userId()));
                for (String code : permissionCodes) {
                  if (!user.hasPermission(code)) return false;
                }
                return true;
              } catch (Exception e) {
                return false;
              }
            })
        .orElse(false);
  }
}
