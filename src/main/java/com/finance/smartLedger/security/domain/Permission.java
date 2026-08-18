package com.finance.smartLedger.security.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permissions", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Data
@NoArgsConstructor
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"roles", "users"})
public class Permission extends AuditableEntity {

  @Column(name = "code", nullable = false, unique = true, length = 100)
  private String code;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "resource", length = 50)
  private String resource;

  @Column(name = "action", length = 50)
  private String action;

  @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
  private Set<Role> roles = new HashSet<>();

  @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
  private Set<User> users = new HashSet<>();

  public Permission(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public Permission(String code, String name, String description) {
    this.code = code;
    this.name = name;
    this.description = description;
  }

  public Permission(String code, String name, String resource, String action) {
    this.code = code;
    this.name = name;
    this.resource = resource;
    this.action = action;
  }

  public Permission(String code, String name, String description, String resource, String action) {
    this.code = code;
    this.name = name;
    this.description = description;
    this.resource = resource;
    this.action = action;
  }

  public static Permission of(String resource, String action) {
    String code = (resource + ":" + action).toUpperCase();
    String name = action + " " + resource;
    return new Permission(code, name, resource, action);
  }
}
