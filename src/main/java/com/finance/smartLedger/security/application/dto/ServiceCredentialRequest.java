package com.finance.smartLedger.security.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record ServiceCredentialRequest(
    @NotBlank(message = "Name is required")
    String name,
    
    @NotEmpty(message = "At least one permission is required")
    Set<String> grantedPermissions
) {}
