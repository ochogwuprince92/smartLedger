package com.finance.smartLedger.shared.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BaseEntity Tests")
class BaseEntityTest {

  @Test
  @DisplayName("Should create BaseEntity with default constructor")
  void shouldCreateBaseEntityWithDefaultConstructor() {
    BaseEntity entity = new BaseEntity() {};

    assertNull(entity.getId());
    assertNull(entity.getCreatedAt());
    assertNull(entity.getUpdatedAt());
    assertNull(entity.getDeletedAt());
  }

  @Test
  @DisplayName("Should soft delete entity")
  void shouldSoftDeleteEntity() {
    BaseEntity entity = new BaseEntity() {};
    entity.setCreatedAt(LocalDateTime.now());
    entity.setUpdatedAt(LocalDateTime.now());
    LocalDateTime beforeDelete = LocalDateTime.now();

    entity.softDelete();

    assertNotNull(entity.getDeletedAt());
    assertTrue(
        entity.getDeletedAt().isAfter(beforeDelete) || entity.getDeletedAt().isEqual(beforeDelete));
    assertTrue(entity.isDeleted());
  }

  @Test
  @DisplayName("Should restore soft deleted entity")
  void shouldRestoreSoftDeletedEntity() {
    BaseEntity entity = new BaseEntity() {};
    entity.softDelete();

    assertTrue(entity.isDeleted());

    entity.restore();

    assertNull(entity.getDeletedAt());
    assertFalse(entity.isDeleted());
  }

  @Test
  @DisplayName("Should check if entity is deleted")
  void shouldCheckIfEntityIsDeleted() {
    BaseEntity entity = new BaseEntity() {};

    assertFalse(entity.isDeleted());

    entity.softDelete();

    assertTrue(entity.isDeleted());
  }

  @Test
  @DisplayName("Should set and get ID")
  void shouldSetAndGetId() {
    BaseEntity entity = new BaseEntity() {};
    UUID id = UUID.randomUUID();

    entity.setId(id);

    assertEquals(id, entity.getId());
  }

  @Test
  @DisplayName("Should set and get created at")
  void shouldSetAndGetCreatedAt() {
    BaseEntity entity = new BaseEntity() {};
    LocalDateTime createdAt = LocalDateTime.now();

    entity.setCreatedAt(createdAt);

    assertEquals(createdAt, entity.getCreatedAt());
  }

  @Test
  @DisplayName("Should set and get updated at")
  void shouldSetAndGetUpdatedAt() {
    BaseEntity entity = new BaseEntity() {};
    LocalDateTime updatedAt = LocalDateTime.now();

    entity.setUpdatedAt(updatedAt);

    assertEquals(updatedAt, entity.getUpdatedAt());
  }

  @Test
  @DisplayName("Should set and get deleted at")
  void shouldSetAndGetDeletedAt() {
    BaseEntity entity = new BaseEntity() {};
    LocalDateTime deletedAt = LocalDateTime.now();

    entity.setDeletedAt(deletedAt);

    assertEquals(deletedAt, entity.getDeletedAt());
    assertTrue(entity.isDeleted());
  }

  @Test
  @DisplayName("Should handle multiple soft deletes")
  void shouldHandleMultipleSoftDeletes() {
    BaseEntity entity = new BaseEntity() {};

    entity.softDelete();
    LocalDateTime firstDelete = entity.getDeletedAt();

    entity.softDelete();
    LocalDateTime secondDelete = entity.getDeletedAt();

    assertNotNull(secondDelete);
    assertTrue(secondDelete.isAfter(firstDelete) || secondDelete.isEqual(firstDelete));
  }

  @Test
  @DisplayName("Should handle restore on non-deleted entity")
  void shouldHandleRestoreOnNonDeletedEntity() {
    BaseEntity entity = new BaseEntity() {};

    assertFalse(entity.isDeleted());

    entity.restore();

    assertNull(entity.getDeletedAt());
    assertFalse(entity.isDeleted());
  }
}
