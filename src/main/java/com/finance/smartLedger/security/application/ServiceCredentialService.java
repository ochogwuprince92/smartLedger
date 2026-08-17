package com.finance.smartLedger.security.application;

import com.finance.smartLedger.security.application.dto.ServiceCredentialResponse;
import com.finance.smartLedger.security.domain.ServiceCredential;
import com.finance.smartLedger.security.infrastructure.persistence.ServiceCredentialRepository;
import com.finance.smartLedger.shared.exception.BusinessException;
import com.finance.smartLedger.shared.exception.ErrorCodes;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceCredentialService {

  private final ServiceCredentialRepository serviceCredentialRepository;
  private final PasswordEncoder passwordEncoder;

  public ServiceCredentialResponse createServiceCredential(
      String name, Set<String> grantedPermissions, String rawApiKey) {
    // Check if name already exists
    if (serviceCredentialRepository.findByName(name).isPresent()) {
      throw new BusinessException(ErrorCodes.CONFLICT, "Service credential with this name already exists");
    }

    // Hash the API key
    String hashedApiKey = passwordEncoder.encode(rawApiKey);

    ServiceCredential credential = ServiceCredential.builder()
        .name(name)
        .hashedApiKey(hashedApiKey)
        .grantedPermissions(grantedPermissions)
        .enabled(true)
        .build();

    ServiceCredential saved = serviceCredentialRepository.save(credential);

    // Return the raw API key ONLY on creation (never retrievable again)
    return ServiceCredentialResponse.from(
        saved.getId(),
        saved.getName(),
        saved.getGrantedPermissions(),
        saved.isEnabled(),
        saved.getCreatedAt(),
        saved.getUpdatedAt(),
        rawApiKey
    );
  }

  public List<ServiceCredentialResponse> listServiceCredentials() {
    return serviceCredentialRepository.findAll().stream()
        .map(cred -> ServiceCredentialResponse.fromWithoutApiKey(
            cred.getId(),
            cred.getName(),
            cred.getGrantedPermissions(),
            cred.isEnabled(),
            cred.getCreatedAt(),
            cred.getUpdatedAt()
        ))
        .collect(Collectors.toList());
  }

  public ServiceCredentialResponse disableServiceCredential(UUID id) {
    ServiceCredential credential = serviceCredentialRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCodes.SERVICE_CREDENTIAL_NOT_FOUND));
    
    credential.setEnabled(false);
    credential.setUpdatedAt(LocalDateTime.now());
    
    ServiceCredential saved = serviceCredentialRepository.save(credential);
    
    return ServiceCredentialResponse.fromWithoutApiKey(
        saved.getId(),
        saved.getName(),
        saved.getGrantedPermissions(),
        saved.isEnabled(),
        saved.getCreatedAt(),
        saved.getUpdatedAt()
    );
  }

  public ServiceCredentialResponse enableServiceCredential(UUID id) {
    ServiceCredential credential = serviceCredentialRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCodes.SERVICE_CREDENTIAL_NOT_FOUND));
    
    credential.setEnabled(true);
    credential.setUpdatedAt(LocalDateTime.now());
    
    ServiceCredential saved = serviceCredentialRepository.save(credential);
    
    return ServiceCredentialResponse.fromWithoutApiKey(
        saved.getId(),
        saved.getName(),
        saved.getGrantedPermissions(),
        saved.isEnabled(),
        saved.getCreatedAt(),
        saved.getUpdatedAt()
    );
  }
}
