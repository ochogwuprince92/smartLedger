package com.finance.smartLedger.security.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Data
@NoArgsConstructor
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"permissions", "childRoles", "parentRoles"})
public class Role extends AuditableEntity {

  @Column(name = "code", nullable = false, unique = true, length = 50)
  private String code;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "level")
  private Integer level = 0;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "role_permissions",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private Set<Permission> permissions = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "role_hierarchy",
      joinColumns = @JoinColumn(name = "parent_role_id"),
      inverseJoinColumns = @JoinColumn(name = "child_role_id"))
  private Set<Role> childRoles = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER, mappedBy = "childRoles")
  private Set<Role> parentRoles = new HashSet<>();

  public Role(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public Role(String code, String name, String description) {
    this.code = code;
    this.name = name;
    this.description = description;
  }

  public void grantPermission(Permission permission) {
    this.permissions.add(permission);
  }

  public void revokePermission(Permission permission) {
    this.permissions.remove(permission);
  }

  public void addChildRole(Role childRole) {
    if (childRole == this) {
      throw new IllegalArgumentException("Cannot add role as its own child");
    }
    if (wouldCreateCycle(this, childRole)) {
      throw new IllegalArgumentException("Adding this role would create a cycle in the hierarchy");
    }
    this.childRoles.add(childRole);
    childRole.parentRoles.add(this);
    updateHierarchyLevels();
  }

  public void removeChildRole(Role childRole) {
    this.childRoles.remove(childRole);
    childRole.parentRoles.remove(this);
    // If child has no parents after removal, reset its level to 0
    if (childRole.parentRoles.isEmpty()) {
      childRole.level = 0;
    } else {
      childRole.updateHierarchyLevels();
    }
    updateHierarchyLevels();
  }

  public Set<Permission> getAllPermissions() {
    Set<Permission> allPermissions = new HashSet<>(this.permissions);
    for (Role childRole : childRoles) {
      allPermissions.addAll(childRole.getAllPermissions());
    }
    return allPermissions;
  }

  public boolean hasPermission(String permissionCode) {
    return permissions.stream().anyMatch(p -> p.getCode().equals(permissionCode))
        || childRoles.stream().anyMatch(r -> r.hasPermission(permissionCode));
  }

  private boolean wouldCreateCycle(Role parent, Role potentialChild) {
    return potentialChild.getAllDescendantRoles().contains(parent);
  }

  private Set<Role> getAllDescendantRoles() {
    Set<Role> descendants = new HashSet<>();
    for (Role child : childRoles) {
      descendants.add(child);
      descendants.addAll(child.getAllDescendantRoles());
    }
    return descendants;
  }

  private void updateHierarchyLevels() {
    this.level = calculateLevel();
    for (Role child : childRoles) {
      child.updateHierarchyLevels();
    }
  }

  private int calculateLevel() {
    if (parentRoles.isEmpty()) {
      this.level = 0;
      return 0;
    }
    int maxParentLevel = parentRoles.stream().mapToInt(Role::getLevel).max().orElse(0);
    this.level = maxParentLevel + 1;
    return maxParentLevel + 1;
  }
}
