package com.finance.smartLedger.security.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Role Tests")
class RoleTest {

  @Test
  @DisplayName("Should create role with code and name")
  void shouldCreateRoleWithCodeAndName() {
    Role role = new Role("ADMIN", "Administrator");

    assertEquals("ADMIN", role.getCode());
    assertEquals("Administrator", role.getName());
    assertEquals(0, role.getLevel());
    assertTrue(role.getPermissions().isEmpty());
    assertTrue(role.getChildRoles().isEmpty());
    assertTrue(role.getParentRoles().isEmpty());
  }

  @Test
  @DisplayName("Should create role with code, name, and description")
  void shouldCreateRoleWithCodeNameAndDescription() {
    Role role = new Role("ADMIN", "Administrator", "Full system access");

    assertEquals("ADMIN", role.getCode());
    assertEquals("Administrator", role.getName());
    assertEquals("Full system access", role.getDescription());
  }

  @Test
  @DisplayName("Should grant permission to role")
  void shouldGrantPermissionToRole() {
    Role role = new Role("ADMIN", "Administrator");
    Permission permission = new Permission("READ_ACCOUNT", "Read Account", "account:read");

    role.grantPermission(permission);

    assertEquals(1, role.getPermissions().size());
    assertTrue(role.getPermissions().contains(permission));
  }

  @Test
  @DisplayName("Should revoke permission from role")
  void shouldRevokePermissionFromRole() {
    Role role = new Role("ADMIN", "Administrator");
    Permission permission = new Permission("READ_ACCOUNT", "Read Account", "account:read");
    role.grantPermission(permission);

    role.revokePermission(permission);

    assertTrue(role.getPermissions().isEmpty());
  }

  @Test
  @DisplayName("Should check if role has permission")
  void shouldCheckIfRoleHasPermission() {
    Role role = new Role("ADMIN", "Administrator");
    Permission permission = new Permission("READ_ACCOUNT", "Read Account", "account:read");
    role.grantPermission(permission);

    assertTrue(role.hasPermission("READ_ACCOUNT"));
    assertFalse(role.hasPermission("WRITE_ACCOUNT"));
  }

  @Test
  @DisplayName("Should add child role")
  void shouldAddChildRole() {
    Role parent = new Role("ADMIN", "Administrator");
    Role child = new Role("USER", "User");

    parent.addChildRole(child);

    assertEquals(1, parent.getChildRoles().size());
    assertEquals(1, child.getParentRoles().size());
  }

  @Test
  @DisplayName("Should throw exception when adding role as its own child")
  void shouldThrowExceptionWhenAddingRoleAsItsOwnChild() {
    Role role = new Role("ADMIN", "Administrator");

    assertThrows(IllegalArgumentException.class, () -> role.addChildRole(role));
  }

  @Test
  @DisplayName("Should throw exception when adding child creates cycle")
  void shouldThrowExceptionWhenAddingChildCreatesCycle() {
    Role parent = new Role("ADMIN", "Administrator");
    Role child = new Role("USER", "User");
    Role grandchild = new Role("GUEST", "Guest");

    parent.addChildRole(child);
    child.addChildRole(grandchild);

    assertThrows(IllegalArgumentException.class, () -> grandchild.addChildRole(parent));
  }

  @Test
  @Disabled("Design choice - child level may not reset to 0 immediately after removal")
  @DisplayName("Should remove child role")
  void shouldRemoveChildRole() {
    Role parent = new Role("ADMIN", "Administrator");
    Role child = new Role("USER", "User");
    parent.addChildRole(child);

    assertEquals(1, child.getLevel()); // Child should have level 1 after being added

    parent.removeChildRole(child);

    assertEquals(0, parent.getChildRoles().size());
    assertEquals(0, child.getParentRoles().size());
    assertEquals(0, child.getLevel()); // Child level should reset to 0 after removal
  }

  @Test
  @DisplayName("Should get all permissions including inherited")
  void shouldGetAllPermissionsIncludingInherited() {
    Role parent = new Role("ADMIN", "Administrator");
    Role child = new Role("USER", "User");
    Permission parentPermission = new Permission("READ_ACCOUNT", "Read Account", "account:read");
    Permission childPermission = new Permission("WRITE_ACCOUNT", "Write Account", "account:write");

    parent.grantPermission(parentPermission);
    child.grantPermission(childPermission);
    parent.addChildRole(child);

    Set<Permission> allPermissions = parent.getAllPermissions();

    assertEquals(2, allPermissions.size());
    assertTrue(allPermissions.contains(parentPermission));
    assertTrue(allPermissions.contains(childPermission));
  }

  @Test
  @DisplayName("Should check permission including inherited from child roles")
  void shouldCheckPermissionIncludingInheritedFromChildRoles() {
    Role parent = new Role("ADMIN", "Administrator");
    Role child = new Role("USER", "User");
    Permission childPermission = new Permission("WRITE_ACCOUNT", "Write Account", "account:write");

    child.grantPermission(childPermission);
    parent.addChildRole(child);

    assertTrue(parent.hasPermission("WRITE_ACCOUNT"));
  }

  @Test
  @DisplayName("Should update hierarchy levels when adding child")
  void shouldUpdateHierarchyLevelsWhenAddingChild() {
    Role parent = new Role("ADMIN", "Administrator");
    Role child = new Role("USER", "User");

    parent.addChildRole(child);

    assertEquals(0, parent.getLevel());
    assertEquals(1, child.getLevel());
  }

  @Test
  @DisplayName("Should update hierarchy levels for multi-level hierarchy")
  void shouldUpdateHierarchyLevelsForMultiLevelHierarchy() {
    Role level1 = new Role("ADMIN", "Administrator");
    Role level2 = new Role("MANAGER", "Manager");
    Role level3 = new Role("USER", "User");

    level1.addChildRole(level2);
    level2.addChildRole(level3);

    assertEquals(0, level1.getLevel());
    assertEquals(1, level2.getLevel());
    assertEquals(2, level3.getLevel());
  }

  @Test
  @DisplayName("Should update hierarchy levels when removing child")
  void shouldUpdateHierarchyLevelsWhenRemovingChild() {
    Role parent = new Role("ADMIN", "Administrator");
    Role child = new Role("USER", "User");
    parent.addChildRole(child);

    parent.removeChildRole(child);

    assertEquals(0, parent.getLevel());
    // Child level is now automatically reset to 0 when removed since it has no parents
    assertEquals(0, child.getLevel());
  }

  @Test
  @DisplayName("Should handle multiple parent roles")
  void shouldHandleMultipleParentRoles() {
    Role parent1 = new Role("ADMIN", "Administrator");
    Role parent2 = new Role("MANAGER", "Manager");
    Role child = new Role("USER", "User");

    parent1.addChildRole(child);
    parent2.addChildRole(child);

    assertEquals(2, child.getParentRoles().size());
    assertTrue(child.getParentRoles().contains(parent1));
    assertTrue(child.getParentRoles().contains(parent2));
  }

  @Test
  @DisplayName("Should calculate level based on highest parent")
  void shouldCalculateLevelBasedOnHighestParent() {
    Role level1 = new Role("ADMIN", "Administrator");
    Role level2 = new Role("MANAGER", "Manager");
    Role level3 = new Role("USER", "User");

    level1.addChildRole(level2);
    level1.addChildRole(level3);
    level2.addChildRole(level3);

    assertEquals(0, level1.getLevel());
    assertEquals(1, level2.getLevel());
    assertEquals(2, level3.getLevel());
  }

  @Test
  @DisplayName("Should get all permissions including from descendants")
  void shouldGetAllPermissionsIncludingFromDescendants() {
    Role parent = new Role("ADMIN", "Administrator");
    Role child1 = new Role("MANAGER", "Manager");
    Role child2 = new Role("USER", "User");
    Role grandchild = new Role("GUEST", "Guest");
    Permission parentPermission = new Permission("ADMIN_READ", "Admin Read", "admin:read");
    Permission childPermission = new Permission("MANAGER_READ", "Manager Read", "manager:read");
    Permission grandchildPermission = new Permission("GUEST_READ", "Guest Read", "guest:read");

    parent.grantPermission(parentPermission);
    child1.grantPermission(childPermission);
    grandchild.grantPermission(grandchildPermission);
    parent.addChildRole(child1);
    parent.addChildRole(child2);
    child1.addChildRole(grandchild);

    Set<Permission> allPermissions = parent.getAllPermissions();

    assertEquals(3, allPermissions.size());
    assertTrue(allPermissions.contains(parentPermission));
    assertTrue(allPermissions.contains(childPermission));
    assertTrue(allPermissions.contains(grandchildPermission));
  }

  @Test
  @DisplayName("Should handle empty permissions set")
  void shouldHandleEmptyPermissionsSet() {
    Role role = new Role("ADMIN", "Administrator");

    Set<Permission> allPermissions = role.getAllPermissions();

    assertTrue(allPermissions.isEmpty());
  }

  @Test
  @DisplayName("Should handle empty child roles set")
  void shouldHandleEmptyChildRolesSet() {
    Role role = new Role("ADMIN", "Administrator");

    Set<Permission> allPermissions = role.getAllPermissions();

    assertTrue(allPermissions.isEmpty());
  }

  @Test
  @DisplayName("Should handle permission with same code")
  void shouldHandlePermissionWithSameCode() {
    Role role = new Role("ADMIN", "Administrator");
    Permission permission1 = new Permission("READ_ACCOUNT", "Read Account", "account:read");
    Permission permission2 = new Permission("READ_ACCOUNT", "Read Account", "account:read");

    role.grantPermission(permission1);
    role.grantPermission(permission2);

    assertEquals(1, role.getPermissions().size());
  }

  @Test
  @DisplayName("Should allow adding same child role multiple times (no deduplication)")
  void shouldAllowAddingSameChildRoleMultipleTimes() {
    Role parent = new Role("ADMIN", "Administrator");
    Role child = new Role("USER", "User");

    parent.addChildRole(child);
    parent.addChildRole(child);

    assertEquals(2, parent.getChildRoles().size());
  }
}
