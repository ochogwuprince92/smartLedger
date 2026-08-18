package com.finance.smartLedger.shared.security;

import java.util.Optional;

public final class SecurityContext {

  private static final ThreadLocal<UserContext> USER_CONTEXT = new ThreadLocal<>();

  private SecurityContext() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static void setUserContext(UserContext userContext) {
    USER_CONTEXT.set(userContext);
  }

  public static Optional<UserContext> getUserContext() {
    return Optional.ofNullable(USER_CONTEXT.get());
  }

  public static String getCurrentUserId() {
    return getUserContext().map(UserContext::userId).orElse("system");
  }

  public static String getCurrentUsername() {
    return getUserContext().map(UserContext::username).orElse("system");
  }

  public static void clear() {
    USER_CONTEXT.remove();
  }

  public record UserContext(String userId, String username, String email, String role) {
    public static UserContext of(String userId, String username) {
      return new UserContext(userId, username, null, null);
    }

    public static UserContext of(String userId, String username, String email, String role) {
      return new UserContext(userId, username, email, role);
    }
  }
}
