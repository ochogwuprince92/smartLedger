package com.finance.smartLedger.security.presentation;

import com.finance.smartLedger.security.application.dto.RoleRequest;
import com.finance.smartLedger.security.application.dto.RoleResponse;
import com.finance.smartLedger.security.service.RoleService;
import com.finance.smartLedger.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

  private final RoleService roleService;

  @PostMapping
  @PreAuthorize("hasAuthority('ROLE:CREATE')")
  public ResponseEntity<ApiResponse<RoleResponse>> createRole(
      @Valid @RequestBody RoleRequest request) {
    var role =
        roleService.createRole(request.getCode(), request.getName(), request.getDescription());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Role created successfully", RoleResponse.from(role)));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE:UPDATE')")
  public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
      @PathVariable UUID id, @RequestBody RoleRequest request) {
    var role = roleService.updateRole(id, request.getName(), request.getDescription());
    return ResponseEntity.ok(
        ApiResponse.success("Role updated successfully", RoleResponse.from(role)));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
    roleService.deleteRole(id);
    return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE:READ')")
  public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable UUID id) {
    var role = roleService.getRoleById(id);
    return ResponseEntity.ok(ApiResponse.success(RoleResponse.from(role)));
  }

  @GetMapping("/code/{code}")
  @PreAuthorize("hasAuthority('ROLE:READ')")
  public ResponseEntity<ApiResponse<RoleResponse>> getRoleByCode(@PathVariable String code) {
    var role = roleService.getRoleByCode(code);
    return ResponseEntity.ok(ApiResponse.success(RoleResponse.from(role)));
  }

  @GetMapping
  @PreAuthorize("hasAuthority('ROLE:READ')")
  public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
    var roles = roleService.getAllRoles();
    var responses = roles.stream().map(RoleResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @PostMapping("/{roleId}/permissions/{permissionId}")
  @PreAuthorize("hasAuthority('ROLE:ASSIGN_PERMISSION')")
  public ResponseEntity<ApiResponse<RoleResponse>> grantPermission(
      @PathVariable UUID roleId, @PathVariable UUID permissionId) {
    var role = roleService.grantPermission(roleId, permissionId);
    return ResponseEntity.ok(
        ApiResponse.success("Permission granted successfully", RoleResponse.from(role)));
  }

  @DeleteMapping("/{roleId}/permissions/{permissionId}")
  @PreAuthorize("hasAuthority('ROLE:ASSIGN_PERMISSION')")
  public ResponseEntity<ApiResponse<RoleResponse>> revokePermission(
      @PathVariable UUID roleId, @PathVariable UUID permissionId) {
    var role = roleService.revokePermission(roleId, permissionId);
    return ResponseEntity.ok(
        ApiResponse.success("Permission revoked successfully", RoleResponse.from(role)));
  }

  @PostMapping("/{parentRoleId}/children/{childRoleId}")
  @PreAuthorize("hasAuthority('ROLE:MANAGE_HIERARCHY')")
  public ResponseEntity<ApiResponse<RoleResponse>> addChildRole(
      @PathVariable UUID parentRoleId, @PathVariable UUID childRoleId) {
    var role = roleService.addChildRole(parentRoleId, childRoleId);
    return ResponseEntity.ok(
        ApiResponse.success("Child role added successfully", RoleResponse.from(role)));
  }

  @DeleteMapping("/{parentRoleId}/children/{childRoleId}")
  @PreAuthorize("hasAuthority('ROLE:MANAGE_HIERARCHY')")
  public ResponseEntity<ApiResponse<RoleResponse>> removeChildRole(
      @PathVariable UUID parentRoleId, @PathVariable UUID childRoleId) {
    var role = roleService.removeChildRole(parentRoleId, childRoleId);
    return ResponseEntity.ok(
        ApiResponse.success("Child role removed successfully", RoleResponse.from(role)));
  }
}
