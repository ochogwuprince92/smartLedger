package com.finance.smartLedger.security.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "service_credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCredential {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private String hashedApiKey;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "service_credential_permissions",
      joinColumns = @JoinColumn(name = "credential_id"))
  @Column(name = "permission_code")
  @Builder.Default
  private Set<String> grantedPermissions = new HashSet<>();

  @Column(nullable = false)
  @Builder.Default
  private boolean enabled = true;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Column(nullable = true)
  private LocalDateTime deletedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
