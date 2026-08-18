package com.finance.smartLedger.security.presentation;

import com.finance.smartLedger.security.application.dto.ServiceCredentialRequest;
import com.finance.smartLedger.security.application.dto.ServiceCredentialResponse;
import com.finance.smartLedger.security.application.ServiceCredentialService;
import com.finance.smartLedger.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/service-credentials")
@RequiredArgsConstructor
public class ServiceCredentialController {

  private final ServiceCredentialService serviceCredentialService;

  @PostMapping
  @PreAuthorize("hasAuthority('SERVICE_CREDENTIAL:MANAGE')")
  public ResponseEntity<ApiResponse<ServiceCredentialResponse>> createServiceCredential(
      @Valid @RequestBody ServiceCredentialRequest request) {
    // Generate a cryptographically random API key (32 bytes = 256 bits)
    SecureRandom secureRandom = new SecureRandom();
    byte[] randomBytes = new byte[32];
    secureRandom.nextBytes(randomBytes);
    String apiKey = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    
    ServiceCredentialResponse response = serviceCredentialService.createServiceCredential(
        request.name(),
        request.grantedPermissions(),
        apiKey);
    
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Service credential created successfully", response));
  }

  @GetMapping
  @PreAuthorize("hasAuthority('SERVICE_CREDENTIAL:MANAGE')")
  public ResponseEntity<ApiResponse<List<ServiceCredentialResponse>>> listServiceCredentials() {
    List<ServiceCredentialResponse> credentials = serviceCredentialService.listServiceCredentials();
    return ResponseEntity.ok(ApiResponse.success(credentials));
  }

  @PatchMapping("/{id}/disable")
  @PreAuthorize("hasAuthority('SERVICE_CREDENTIAL:MANAGE')")
  public ResponseEntity<ApiResponse<ServiceCredentialResponse>> disableServiceCredential(
      @PathVariable UUID id) {
    ServiceCredentialResponse response = serviceCredentialService.disableServiceCredential(id);
    return ResponseEntity.ok(ApiResponse.success("Service credential disabled successfully", response));
  }

  @PatchMapping("/{id}/enable")
  @PreAuthorize("hasAuthority('SERVICE_CREDENTIAL:MANAGE')")
  public ResponseEntity<ApiResponse<ServiceCredentialResponse>> enableServiceCredential(
      @PathVariable UUID id) {
    ServiceCredentialResponse response = serviceCredentialService.enableServiceCredential(id);
    return ResponseEntity.ok(ApiResponse.success("Service credential enabled successfully", response));
  }
}
