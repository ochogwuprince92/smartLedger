package com.finance.smartLedger.security.infrastructure.persistence;

import com.finance.smartLedger.security.domain.Permission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository
    extends JpaRepository<Permission, UUID>, JpaSpecificationExecutor<Permission> {

  Optional<Permission> findByCode(String code);

  boolean existsByCode(String code);
}
