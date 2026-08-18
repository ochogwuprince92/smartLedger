package com.finance.smartLedger.shared.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AuditableEntity Tests")
class AuditableEntityTest {

  @Test
  @DisplayName("Should create AuditableEntity with default constructor")
  void shouldCreateAuditableEntityWithDefaultConstructor() {
    AuditableEntity entity = new AuditableEntity() {};
    // Simulate JPA auditing behavior in unit test
    entity.setCreatedAt(LocalDateTime.now());
    entity.setUpdatedAt(LocalDateTime.now());

    assertNull(entity.getId());
    assertNotNull(entity.getCreatedAt());
    assertNotNull(entity.getUpdatedAt());
    assertNull(entity.getDeletedAt());
    assertNull(entity.getCreatedBy());
    assertNull(entity.getUpdatedBy());
  }

  @Test
  @DisplayName("Should set and get created by")
  void shouldSetAndGetCreatedBy() {
    AuditableEntity entity = new AuditableEntity() {};
    String createdBy = "system";

    entity.setCreatedBy(createdBy);

    assertEquals(createdBy, entity.getCreatedBy());
  }

  @Test
  @DisplayName("Should set and get updated by")
  void shouldSetAndGetUpdatedBy() {
    AuditableEntity entity = new AuditableEntity() {};
    String updatedBy = "admin";

    entity.setUpdatedBy(updatedBy);

    assertEquals(updatedBy, entity.getUpdatedBy());
  }

  @Test
  @DisplayName("Should inherit BaseEntity functionality")
  void shouldInheritBaseEntityFunctionality() {
    AuditableEntity entity = new AuditableEntity() {};

    assertFalse(entity.isDeleted());

    entity.softDelete();

    assertTrue(entity.isDeleted());

    entity.restore();

    assertFalse(entity.isDeleted());
  }

  @Test
  @DisplayName("Should set audit fields together")
  void shouldSetAuditFieldsTogether() {
    AuditableEntity entity = new AuditableEntity() {};
    String createdBy = "user123";
    String updatedBy = "user456";

    entity.setCreatedBy(createdBy);
    entity.setUpdatedBy(updatedBy);

    assertEquals(createdBy, entity.getCreatedBy());
    assertEquals(updatedBy, entity.getUpdatedBy());
  }

  @Test
  @DisplayName("Should handle null values for audit fields")
  void shouldHandleNullValuesForAuditFields() {
    AuditableEntity entity = new AuditableEntity() {};

    entity.setCreatedBy(null);
    entity.setUpdatedBy(null);

    assertNull(entity.getCreatedBy());
    assertNull(entity.getUpdatedBy());
  }

  @Test
  @DisplayName("Should handle empty strings for audit fields")
  void shouldHandleEmptyStringsForAuditFields() {
    AuditableEntity entity = new AuditableEntity() {};
    String emptyString = "";

    entity.setCreatedBy(emptyString);
    entity.setUpdatedBy(emptyString);

    assertEquals(emptyString, entity.getCreatedBy());
    assertEquals(emptyString, entity.getUpdatedBy());
  }
}
