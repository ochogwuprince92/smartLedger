package com.finance.smartLedger.security.infrastructure.persistence;

import com.finance.smartLedger.security.domain.ServiceCredential;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceCredentialRepository extends JpaRepository<ServiceCredential, UUID> {

  Optional<ServiceCredential> findByName(String name);

  boolean existsByName(String name);

  List<ServiceCredential> findByEnabledTrue();
}
