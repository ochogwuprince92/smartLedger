package com.finance.smartLedger.security.presentation;

import com.finance.smartLedger.security.application.dto.PermissionRequest;
import com.finance.smartLedger.security.application.dto.PermissionResponse;
import com.finance.smartLedger.security.service.PermissionService;
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
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

  private final PermissionService permissionService;

  @PostMapping
  @PreAuthorize("hasAuthority('PERMISSION:CREATE')")
  public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
      @Valid @RequestBody PermissionRequest request) {
    var permission =
        permissionService.createPermission(
            request.getCode(),
            request.getName(),
            request.getDescription(),
            request.getResource(),
            request.getAction());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Permission created successfully", PermissionResponse.from(permission)));
  }

  @PostMapping("/resource-action")
  @PreAuthorize("hasAuthority('PERMISSION:CREATE')")
  public ResponseEntity<ApiResponse<PermissionResponse>> createPermissionFromResourceAction(
      @RequestBody ResourceActionRequest request) {
    var permission = permissionService.createPermission(request.resource(), request.action());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Permission created successfully", PermissionResponse.from(permission)));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('PERMISSION:UPDATE')")
  public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
      @PathVariable UUID id, @RequestBody PermissionRequest request) {
    var permission =
        permissionService.updatePermission(id, request.getName(), request.getDescription());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Permission updated successfully", PermissionResponse.from(permission)));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('PERMISSION:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable UUID id) {
    permissionService.deletePermission(id);
    return ResponseEntity.ok(ApiResponse.success("Permission deleted successfully", null));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('PERMISSION:READ')")
  public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable UUID id) {
    var permission = permissionService.getPermissionById(id);
    return ResponseEntity.ok(ApiResponse.success(PermissionResponse.from(permission)));
  }

  @GetMapping("/code/{code}")
  @PreAuthorize("hasAuthority('PERMISSION:READ')")
  public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionByCode(
      @PathVariable String code) {
    var permission = permissionService.getPermissionByCode(code);
    return ResponseEntity.ok(ApiResponse.success(PermissionResponse.from(permission)));
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PERMISSION:READ')")
  public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
    var permissions = permissionService.getAllPermissions();
    var responses = permissions.stream().map(PermissionResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  record ResourceActionRequest(String resource, String action) {}
}
