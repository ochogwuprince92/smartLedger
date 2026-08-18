package com.finance.smartLedger.security.infrastructure.persistence;

import com.finance.smartLedger.security.domain.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID>, JpaSpecificationExecutor<Role> {

  Optional<Role> findByCode(String code);

  boolean existsByCode(String code);
}
