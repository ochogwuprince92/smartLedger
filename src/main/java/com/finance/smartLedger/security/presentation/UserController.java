package com.finance.smartLedger.security.presentation;

import com.finance.smartLedger.security.application.dto.AdminPasswordResetResponse;
import com.finance.smartLedger.security.application.dto.UserRequest;
import com.finance.smartLedger.security.application.dto.UserResponse;
import com.finance.smartLedger.security.service.UserService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  @PreAuthorize("hasAuthority('USER:CREATE')")
  public ResponseEntity<ApiResponse<UserResponse>> createUser(
      @Valid @RequestBody UserRequest request) {
    var user =
        userService.createUser(
            request.getUsername(),
            request.getEmail(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName());
    user.setPhone(request.getPhone());
    var savedUser =
        userService.updateUser(
            user.getId(), request.getFirstName(), request.getLastName(), request.getPhone());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("User created successfully", UserResponse.from(savedUser)));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('USER:UPDATE')")
  public ResponseEntity<ApiResponse<UserResponse>> updateUser(
      @PathVariable UUID id, @RequestBody UserRequest request) {
    var user =
        userService.updateUser(
            id, request.getFirstName(), request.getLastName(), request.getPhone());
    return ResponseEntity.ok(
        ApiResponse.success("User updated successfully", UserResponse.from(user)));
  }

  @PatchMapping("/{id}/password")
  @PreAuthorize("hasAuthority('USER:UPDATE')")
  public ResponseEntity<ApiResponse<UserResponse>> updatePassword(
      @PathVariable UUID id, @RequestBody PasswordUpdateRequest request) {
    var user = userService.updatePassword(id, request.oldPassword(), request.newPassword());
    return ResponseEntity.ok(
        ApiResponse.success("Password updated successfully", UserResponse.from(user)));
  }

  @PostMapping("/{id}/reset-password")
  @PreAuthorize("hasAuthority('USER:RESET_PASSWORD')")
  public ResponseEntity<ApiResponse<AdminPasswordResetResponse>> resetPassword(
      @PathVariable UUID id) {
    var tempPassword = userService.adminResetPassword(id);
    var response = new AdminPasswordResetResponse(id, tempPassword);
    return ResponseEntity.ok(
        ApiResponse.success("Password reset successfully. Temporary password provided for one-time relay to user.", response));
  }

  @PatchMapping("/{id}/enable")
  @PreAuthorize("hasAuthority('USER:UPDATE')")
  public ResponseEntity<ApiResponse<Void>> enableUser(@PathVariable UUID id) {
    userService.enableUser(id);
    return ResponseEntity.ok(ApiResponse.success("User enabled successfully", null));
  }

  @PatchMapping("/{id}/disable")
  @PreAuthorize("hasAuthority('USER:UPDATE')")
  public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable UUID id) {
    userService.disableUser(id);
    return ResponseEntity.ok(ApiResponse.success("User disabled successfully", null));
  }

  @PatchMapping("/{id}/lock")
  @PreAuthorize("hasAuthority('USER:UPDATE')")
  public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable UUID id) {
    userService.lockUser(id);
    return ResponseEntity.ok(ApiResponse.success("User locked successfully", null));
  }

  @PatchMapping("/{id}/unlock")
  @PreAuthorize("hasAuthority('USER:UPDATE')")
  public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable UUID id) {
    userService.unlockUser(id);
    return ResponseEntity.ok(ApiResponse.success("User unlocked successfully", null));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('USER:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
    userService.deleteUser(id);
    return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('USER:READ')")
  public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
    var user = userService.getUserById(id);
    return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
  }

  @GetMapping("/username/{username}")
  @PreAuthorize("hasAuthority('USER:READ')")
  public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(
      @PathVariable String username) {
    var user = userService.getUserByUsername(username);
    return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
  }

  @GetMapping
  @PreAuthorize("hasAuthority('USER:READ')")
  public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
    var users = userService.getAllUsers();
    var responses = users.stream().map(UserResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @PostMapping("/{userId}/roles/{roleId}")
  @PreAuthorize("hasAuthority('USER:ASSIGN_ROLE')")
  public ResponseEntity<ApiResponse<UserResponse>> grantRole(
      @PathVariable UUID userId, @PathVariable UUID roleId) {
    var user = userService.grantRole(userId, roleId);
    return ResponseEntity.ok(
        ApiResponse.success("Role granted successfully", UserResponse.from(user)));
  }

  @DeleteMapping("/{userId}/roles/{roleId}")
  @PreAuthorize("hasAuthority('USER:ASSIGN_ROLE')")
  public ResponseEntity<ApiResponse<UserResponse>> revokeRole(
      @PathVariable UUID userId, @PathVariable UUID roleId) {
    var user = userService.revokeRole(userId, roleId);
    return ResponseEntity.ok(
        ApiResponse.success("Role revoked successfully", UserResponse.from(user)));
  }

  @PostMapping("/{userId}/permissions/{permissionId}")
  @PreAuthorize("hasAuthority('USER:ASSIGN_PERMISSION')")
  public ResponseEntity<ApiResponse<UserResponse>> grantPermission(
      @PathVariable UUID userId, @PathVariable UUID permissionId) {
    var user = userService.grantPermission(userId, permissionId);
    return ResponseEntity.ok(
        ApiResponse.success("Permission granted successfully", UserResponse.from(user)));
  }

  @DeleteMapping("/{userId}/permissions/{permissionId}")
  @PreAuthorize("hasAuthority('USER:ASSIGN_PERMISSION')")
  public ResponseEntity<ApiResponse<UserResponse>> revokePermission(
      @PathVariable UUID userId, @PathVariable UUID permissionId) {
    var user = userService.revokePermission(userId, permissionId);
    return ResponseEntity.ok(
        ApiResponse.success("Permission revoked successfully", UserResponse.from(user)));
  }

  public record PasswordUpdateRequest(String oldPassword, String newPassword) {}
}
