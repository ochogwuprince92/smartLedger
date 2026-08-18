package com.finance.smartLedger.web.presentation;

import com.finance.smartLedger.security.config.JwtService;
import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebAuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UserService userService;

  @PostMapping("/login-web")
  public String loginWeb(
      @RequestParam String username,
      @RequestParam String password,
      HttpServletResponse response,
      RedirectAttributes redirectAttributes) {
    
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(username, password));

      User user = userService.recordSuccessfulLogin(username);
      String token = jwtService.generateToken(user.getUsername(), user.getId().toString());

      // Set HttpOnly, Secure, SameSite=Strict cookie using ResponseCookie builder
      ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
          .httpOnly(true)
          .secure(true)
          .sameSite("Strict")
          .path("/")
          .maxAge(3600) // 1 hour
          .build();
      response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

      return "redirect:/dashboard";
      
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Invalid username or password");
      return "redirect:/login";
    }
  }

  @PostMapping("/logout")
  public String logout(HttpServletResponse response) {
    // Clear the JWT cookie using ResponseCookie builder
    ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path("/")
        .maxAge(0) // Immediately expire
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

    return "redirect:/login";
  }
}