package com.finance.smartLedger.security.application.dto;

import com.finance.smartLedger.security.domain.User;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class UserResponse {

  private UUID id;
  private String username;
  private String email;
  private String firstName;
  private String lastName;
  private String phone;
  private Boolean enabled;
  private Boolean accountNonExpired;
  private Boolean accountNonLocked;
  private Boolean credentialsNonExpired;
  private LocalDateTime lastLoginAt;
  private Integer failedLoginAttempts;
  private LocalDateTime lockedUntil;
  private Set<String> roles;
  private Set<String> permissions;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  public static UserResponse from(User user) {
    UserResponse response = new UserResponse();
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    response.setEmail(user.getEmail());
    response.setFirstName(user.getFirstName());
    response.setLastName(user.getLastName());
    response.setPhone(user.getPhone());
    response.setEnabled(user.getEnabled());
    response.setAccountNonExpired(user.getAccountNonExpired());
    response.setAccountNonLocked(user.getAccountNonLocked());
    response.setCredentialsNonExpired(user.getCredentialsNonExpired());
    response.setLastLoginAt(user.getLastLoginAt());
    response.setFailedLoginAttempts(user.getFailedLoginAttempts());
    response.setLockedUntil(user.getLockedUntil());
    response.setRoles(
        user.getRoles().stream().map(role -> role.getCode()).collect(Collectors.toSet()));
    response.setPermissions(
        user.getPermissions().stream().map(perm -> perm.getCode()).collect(Collectors.toSet()));
    response.setCreatedAt(user.getCreatedAt());
    response.setUpdatedAt(user.getUpdatedAt());
    response.setCreatedBy(user.getCreatedBy());
    response.setUpdatedBy(user.getUpdatedBy());
    return response;
  }
}
