package com.finance.smartLedger.security.presentation;

import com.finance.smartLedger.security.config.JwtService;
import com.finance.smartLedger.security.domain.User;
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

  record LoginRequest(String username, String password) {}

  record LoginResponse(String token, java.util.UUID userId, String username, String email) {}
}
