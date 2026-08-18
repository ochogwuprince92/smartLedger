package com.finance.smartLedger.security.presentation;

import com.finance.smartLedger.security.config.JwtService;
import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.service.PasswordResetService;
import com.finance.smartLedger.security.service.UserService;
import com.finance.smartLedger.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UserService userService;
  private final PasswordResetService passwordResetService;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));

    User user = userService.recordSuccessfulLogin(request.username());

    String token = jwtService.generateToken(user.getUsername(), user.getId().toString());

    LoginResponse response =
        new LoginResponse(token, user.getId(), user.getUsername(), user.getEmail());
    return ResponseEntity.ok(ApiResponse.success("Login successful", response));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse<Void>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    try {
      passwordResetService.initiatePasswordReset(request.email());
    } catch (Exception e) {
      // Log the error but don't reveal it to the user
      // This prevents account enumeration and maintains the security contract
      // The token will still be generated and logged for development purposes
    }
    return ResponseEntity.ok(
        ApiResponse.success(
            "If the email exists in our system, a password reset link has been sent", null));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    passwordResetService.resetPassword(request.token(), request.newPassword());
    return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully", null));
  }

  record LoginRequest(String username, String password) {}

  record LoginResponse(String token, java.util.UUID userId, String username, String email) {}
}
