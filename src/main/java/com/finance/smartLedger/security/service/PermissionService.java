package com.finance.smartLedger.security.service;

import com.finance.smartLedger.security.domain.Permission;
import com.finance.smartLedger.security.infrastructure.persistence.PermissionRepository;
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
public class PermissionService {

  private final PermissionRepository permissionRepository;

  public Permission createPermission(
      String code, String name, String description, String resource, String action) {
    if (permissionRepository.existsByCode(code)) {
      throw new BusinessException(ErrorCodes.CONFLICT, "Permission code already exists");
    }
    Permission permission = new Permission(code, name, description, resource, action);
    return permissionRepository.save(permission);
  }

  public Permission createPermission(String resource, String action) {
    return createPermission(
        (resource + ":" + action).toUpperCase(), action + " " + resource, null, resource, action);
  }

  public Permission updatePermission(UUID permissionId, String name, String description) {
    Permission permission = getPermissionById(permissionId);
    permission.setName(name);
    permission.setDescription(description);
    return permissionRepository.save(permission);
  }

  public void deletePermission(UUID permissionId) {
    Permission permission = getPermissionById(permissionId);
    permissionRepository.delete(permission);
  }

  @Transactional(readOnly = true)
  public Permission getPermissionById(UUID permissionId) {
    return permissionRepository
        .findById(permissionId)
        .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "Permission not found"));
  }

  @Transactional(readOnly = true)
  public Permission getPermissionByCode(String code) {
    return permissionRepository
        .findByCode(code)
        .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "Permission not found"));
  }

  @Transactional(readOnly = true)
  public List<Permission> getAllPermissions() {
    return permissionRepository.findAll();
  }
}
