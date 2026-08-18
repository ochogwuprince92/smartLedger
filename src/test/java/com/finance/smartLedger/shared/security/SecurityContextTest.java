package com.finance.smartLedger.shared.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SecurityContext Tests")
class SecurityContextTest {

  @AfterEach
  void tearDown() {
    SecurityContext.clear();
  }

  @Test
  @DisplayName("Should set and get user context")
  void shouldSetAndGetUserContext() {
    SecurityContext.UserContext userContext = SecurityContext.UserContext.of("user123", "john.doe");

    SecurityContext.setUserContext(userContext);

    Optional<SecurityContext.UserContext> result = SecurityContext.getUserContext();

    assertTrue(result.isPresent());
    assertEquals(userContext, result.get());
  }

  @Test
  @DisplayName("Should return empty when no user context is set")
  void shouldReturnEmptyWhenNoUserContextIsSet() {
    Optional<SecurityContext.UserContext> result = SecurityContext.getUserContext();

    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("Should get current user id")
  void shouldGetCurrentUserId() {
    SecurityContext.UserContext userContext = SecurityContext.UserContext.of("user123", "john.doe");
    SecurityContext.setUserContext(userContext);

    String userId = SecurityContext.getCurrentUserId();

    assertEquals("user123", userId);
  }

  @Test
  @DisplayName("Should return system default when no user context is set for user id")
  void shouldReturnSystemDefaultWhenNoUserContextIsSetForUserId() {
    String userId = SecurityContext.getCurrentUserId();

    assertEquals("system", userId);
  }

  @Test
  @DisplayName("Should get current username")
  void shouldGetCurrentUsername() {
    SecurityContext.UserContext userContext = SecurityContext.UserContext.of("user123", "john.doe");
    SecurityContext.setUserContext(userContext);

    String username = SecurityContext.getCurrentUsername();

    assertEquals("john.doe", username);
  }

  @Test
  @DisplayName("Should return system default when no user context is set for username")
  void shouldReturnSystemDefaultWhenNoUserContextIsSetForUsername() {
    String username = SecurityContext.getCurrentUsername();

    assertEquals("system", username);
  }

  @Test
  @DisplayName("Should clear user context")
  void shouldClearUserContext() {
    SecurityContext.UserContext userContext = SecurityContext.UserContext.of("user123", "john.doe");
    SecurityContext.setUserContext(userContext);

    assertTrue(SecurityContext.getUserContext().isPresent());

    SecurityContext.clear();

    assertFalse(SecurityContext.getUserContext().isPresent());
  }

  @Test
  @DisplayName("Should create UserContext with userId and username")
  void shouldCreateUserContextWithUserIdAndUsername() {
    SecurityContext.UserContext userContext = SecurityContext.UserContext.of("user123", "john.doe");

    assertEquals("user123", userContext.userId());
    assertEquals("john.doe", userContext.username());
    assertNull(userContext.email());
    assertNull(userContext.role());
  }

  @Test
  @DisplayName("Should create UserContext with all fields")
  void shouldCreateUserContextWithAllFields() {
    SecurityContext.UserContext userContext =
        SecurityContext.UserContext.of("user123", "john.doe", "john@example.com", "ADMIN");

    assertEquals("user123", userContext.userId());
    assertEquals("john.doe", userContext.username());
    assertEquals("john@example.com", userContext.email());
    assertEquals("ADMIN", userContext.role());
  }

  @Test
  @DisplayName("Should handle multiple set operations")
  void shouldHandleMultipleSetOperations() {
    SecurityContext.UserContext userContext1 = SecurityContext.UserContext.of("user1", "user.one");
    SecurityContext.UserContext userContext2 = SecurityContext.UserContext.of("user2", "user.two");

    SecurityContext.setUserContext(userContext1);
    assertEquals("user1", SecurityContext.getCurrentUserId());

    SecurityContext.setUserContext(userContext2);
    assertEquals("user2", SecurityContext.getCurrentUserId());
  }

  @Test
  @DisplayName("Should handle thread-local isolation")
  void shouldHandleThreadLocalIsolation() throws InterruptedException {
    SecurityContext.UserContext mainContext = SecurityContext.UserContext.of("main-user", "main");
    SecurityContext.setUserContext(mainContext);

    Thread thread =
        new Thread(
            () -> {
              SecurityContext.UserContext threadContext =
                  SecurityContext.UserContext.of("thread-user", "thread");
              SecurityContext.setUserContext(threadContext);
              assertEquals("thread-user", SecurityContext.getCurrentUserId());
              SecurityContext.clear();
            });

    thread.start();
    thread.join();

    assertEquals("main-user", SecurityContext.getCurrentUserId());
  }

  @Test
  @DisplayName("Should handle null values in UserContext")
  void shouldHandleNullValuesInUserContext() {
    SecurityContext.UserContext userContext =
        SecurityContext.UserContext.of("user123", "john.doe", null, null);

    assertEquals("user123", userContext.userId());
    assertEquals("john.doe", userContext.username());
    assertNull(userContext.email());
    assertNull(userContext.role());
  }

  @Test
  @DisplayName("Should update user context after clear")
  void shouldUpdateUserContextAfterClear() {
    SecurityContext.UserContext userContext1 = SecurityContext.UserContext.of("user1", "user.one");
    SecurityContext.setUserContext(userContext1);

    SecurityContext.clear();

    SecurityContext.UserContext userContext2 = SecurityContext.UserContext.of("user2", "user.two");
    SecurityContext.setUserContext(userContext2);

    assertEquals("user2", SecurityContext.getCurrentUserId());
  }

  @Test
  @DisplayName("Should return system default after clear")
  void shouldReturnSystemDefaultAfterClear() {
    SecurityContext.UserContext userContext = SecurityContext.UserContext.of("user123", "john.doe");
    SecurityContext.setUserContext(userContext);

    SecurityContext.clear();

    assertEquals("system", SecurityContext.getCurrentUserId());
    assertEquals("system", SecurityContext.getCurrentUsername());
  }

  @Test
  @DisplayName("Should handle UserContext record equality")
  void shouldHandleUserContextRecordEquality() {
    SecurityContext.UserContext context1 =
        SecurityContext.UserContext.of("user123", "john.doe", "john@example.com", "ADMIN");
    SecurityContext.UserContext context2 =
        SecurityContext.UserContext.of("user123", "john.doe", "john@example.com", "ADMIN");

    assertEquals(context1, context2);
    assertEquals(context1.hashCode(), context2.hashCode());
  }

  @Test
  @DisplayName("Should handle UserContext record with different values")
  void shouldHandleUserContextRecordWithDifferentValues() {
    SecurityContext.UserContext context1 = SecurityContext.UserContext.of("user1", "user.one");
    SecurityContext.UserContext context2 = SecurityContext.UserContext.of("user2", "user.two");

    assertNotEquals(context1, context2);
  }
}
