package com.finance.smartLedger.security.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = "username"),
      @UniqueConstraint(columnNames = "email")
    })
@Data
@NoArgsConstructor
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"roles", "permissions"})
public class User extends AuditableEntity {

  @Column(name = "username", nullable = false, length = 50)
  private String username;

  @Column(name = "email", nullable = false, length = 100)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "first_name", length = 50)
  private String firstName;

  @Column(name = "last_name", length = 50)
  private String lastName;

  @Column(name = "phone", length = 20)
  private String phone;

  @Column(name = "enabled", nullable = false)
  private Boolean enabled = true;

  @Column(name = "account_non_expired", nullable = false)
  private Boolean accountNonExpired = true;

  @Column(name = "account_non_locked", nullable = false)
  private Boolean accountNonLocked = true;

  @Column(name = "credentials_non_expired", nullable = false)
  private Boolean credentialsNonExpired = true;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "failed_login_attempts")
  private Integer failedLoginAttempts = 0;

  @Column(name = "locked_until")
  private LocalDateTime lockedUntil;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_permissions",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private Set<Permission> permissions = new HashSet<>();

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  public void grantRole(Role role) {
    this.roles.add(role);
  }

  public void revokeRole(Role role) {
    this.roles.remove(role);
  }

  public void grantPermission(Permission permission) {
    this.permissions.add(permission);
  }

  public void revokePermission(Permission permission) {
    this.permissions.remove(permission);
  }

  public boolean hasPermission(String permissionCode) {
    return permissions.stream().anyMatch(p -> p.getCode().equals(permissionCode))
        || roles.stream().anyMatch(role -> role.hasPermission(permissionCode));
  }

  public void recordSuccessfulLogin() {
    this.lastLoginAt = LocalDateTime.now();
    this.failedLoginAttempts = 0;
    this.lockedUntil = null;
  }

  public void recordFailedLogin() {
    this.failedLoginAttempts++;
    if (this.failedLoginAttempts >= 5) {
      this.accountNonLocked = false;
      this.lockedUntil = LocalDateTime.now().plusMinutes(30);
    }
  }

  public boolean isAccountLocked() {
    if (!accountNonLocked && lockedUntil != null) {
      if (LocalDateTime.now().isAfter(lockedUntil)) {
        this.accountNonLocked = true;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
        return false;
      }
      return true;
    }
    return !accountNonLocked;
  }

  public String getFullName() {
    if (firstName != null && lastName != null) {
      return firstName + " " + lastName;
    }
    return username;
  }
}
