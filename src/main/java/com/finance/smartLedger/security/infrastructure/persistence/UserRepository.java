package com.finance.smartLedger.security.infrastructure.persistence;

import com.finance.smartLedger.security.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByIdAndEnabledTrue(UUID id);

  @Query("SELECT u FROM User u JOIN u.roles r WHERE r.code = :roleCode AND u.enabled = true")
  List<User> findByRoleCodeAndEnabled(@Param("roleCode") String roleCode);
}
