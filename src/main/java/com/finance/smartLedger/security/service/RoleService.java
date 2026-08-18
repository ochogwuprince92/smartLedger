package com.finance.smartLedger.security.service;

import com.finance.smartLedger.security.domain.Role;
import com.finance.smartLedger.security.infrastructure.persistence.RoleRepository;
import com.finance.smartLedger.shared.exception.BusinessException;
import com.finance.smartLedger.shared.exception.ErrorCodes;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

  private final RoleRepository roleRepository;
  private final PermissionService permissionService;

  public Role createRole(String code, String name, String description) {
    if (roleRepository.existsByCode(code)) {
      throw new BusinessException(ErrorCodes.CONFLICT, "Role code already exists");
    }
    Role role = new Role(code, name, description);
    return roleRepository.save(role);
  }

  public Role updateRole(UUID roleId, String name, String description) {
    Role role = getRoleById(roleId);
    role.setName(name);
    role.setDescription(description);
    return roleRepository.save(role);
  }

  public void deleteRole(UUID roleId) {
    Role role = getRoleById(roleId);
    roleRepository.delete(role);
  }

  @Transactional(readOnly = true)
  public Role getRoleById(UUID roleId) {
    return roleRepository
        .findById(roleId)
        .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "Role not found"));
  }

  @Transactional(readOnly = true)
  public Role getRoleByCode(String code) {
    return roleRepository
        .findByCode(code)
        .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "Role not found"));
  }

  @Transactional(readOnly = true)
  public List<Role> getAllRoles() {
    return roleRepository.findAll();
  }

  public Role grantPermission(UUID roleId, UUID permissionId) {
    Role role = getRoleById(roleId);
    var permission = permissionService.getPermissionById(permissionId);
    role.grantPermission(permission);
    return roleRepository.save(role);
  }

  public Role revokePermission(UUID roleId, UUID permissionId) {
    Role role = getRoleById(roleId);
    var permission = permissionService.getPermissionById(permissionId);
    role.revokePermission(permission);
    return roleRepository.save(role);
  }

  public Role addChildRole(UUID parentRoleId, UUID childRoleId) {
    Role parent = getRoleById(parentRoleId);
    Role child = getRoleById(childRoleId);
    parent.addChildRole(child);
    return roleRepository.save(parent);
  }

  public Role removeChildRole(UUID parentRoleId, UUID childRoleId) {
    Role parent = getRoleById(parentRoleId);
    Role child = getRoleById(childRoleId);
    parent.removeChildRole(child);
    return roleRepository.save(parent);
  }
}
