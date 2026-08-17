package com.finance.smartLedger.web.presentation;

import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.service.PasswordResetService;
import com.finance.smartLedger.security.service.UserService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebController {

  private final UserService userService;
  private final PasswordResetService passwordResetService;

  @GetMapping({"/", "/dashboard"})
  public String dashboard() {
    return "dashboard";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/forgot-password")
  public String forgotPassword() {
    return "forgot-password";
  }

  @PostMapping("/forgot-password")
  public String forgotPassword(
      @RequestParam String email,
      RedirectAttributes redirectAttributes) {
    
    try {
      passwordResetService.initiatePasswordReset(email);
      redirectAttributes.addFlashAttribute("success", 
          "If the email exists in our system, a password reset link has been sent");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to send password reset link: " + e.getMessage());
    }
    
    return "redirect:/forgot-password";
  }

  @GetMapping("/reset-password")
  public String resetPassword(@RequestParam String token, RedirectAttributes redirectAttributes) {
    // Validate token before showing the form
    try {
      // Add the token to the model so it can be used in the form
      redirectAttributes.addFlashAttribute("token", token);
      return "redirect:/reset-password-form";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Invalid or expired reset token");
      return "redirect:/login";
    }
  }

  @GetMapping("/reset-password-form")
  public String resetPasswordForm() {
    return "reset-password";
  }

  @PostMapping("/reset-password")
  public String resetPassword(
      @RequestParam String token,
      @RequestParam String newPassword,
      @RequestParam String confirmPassword,
      RedirectAttributes redirectAttributes) {
    
    // Password validation
    if (newPassword == null || newPassword.length() < 8) {
      redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters long");
      redirectAttributes.addFlashAttribute("token", token);
      return "redirect:/reset-password-form";
    }
    
    if (!newPassword.equals(confirmPassword)) {
      redirectAttributes.addFlashAttribute("error", "New password and confirmation do not match");
      redirectAttributes.addFlashAttribute("token", token);
      return "redirect:/reset-password-form";
    }
    
    try {
      passwordResetService.resetPassword(token, newPassword);
      redirectAttributes.addFlashAttribute("success", "Password has been reset successfully");
      return "redirect:/login";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to reset password: " + e.getMessage());
      redirectAttributes.addFlashAttribute("token", token);
      return "redirect:/reset-password-form";
    }
  }

  @GetMapping("/change-password")
  public String changePassword(Principal principal) {
    // This page is accessible even when mustChangePassword=true
    // The filter allows access to this endpoint for users with mustChangePassword=true
    return "change-password";
  }

  @PostMapping("/change-password")
  public String changePassword(
      @RequestParam(required = false) String currentPassword,
      @RequestParam String newPassword,
      @RequestParam String confirmPassword,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    
    // Password validation
    if (newPassword == null || newPassword.length() < 8) {
      redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters long");
      return "redirect:/change-password";
    }
    
    if (!newPassword.equals(confirmPassword)) {
      redirectAttributes.addFlashAttribute("error", "New password and confirmation do not match");
      return "redirect:/change-password";
    }
    
    try {
      // Get current user ID from principal
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String username = authentication.getName();
      User user = userService.getUserByUsername(username);
      
      // Call the existing password update logic
      // If currentPassword is null/empty, it means forced password change (no current password required)
      // The UserService.updatePassword method handles this case automatically
      String oldPasswordParam = (currentPassword != null && !currentPassword.isEmpty()) ? currentPassword : null;
      userService.updatePassword(user.getId(), oldPasswordParam, newPassword);
      
      redirectAttributes.addFlashAttribute("success", "Password changed successfully");
      return "redirect:/dashboard";
      
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to change password: " + e.getMessage());
      return "redirect:/change-password";
    }
  }

  @GetMapping("/fees")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public String fees() {
    return "fees";
  }

  @GetMapping("/payments")
  @PreAuthorize("hasAuthority('PAYMENT:READ')")
  public String payments() {
    return "payments";
  }

  @GetMapping("/payment/callback")
  public String paymentCallback(
      @RequestParam(required = false) String reference,
      @RequestParam(required = false) String trxref,
      RedirectAttributes redirectAttributes) {
    
    // Add the reference to redirect attributes for processing
    if (reference != null) {
      redirectAttributes.addFlashAttribute("paymentReference", reference);
    } else if (trxref != null) {
      redirectAttributes.addFlashAttribute("paymentReference", trxref);
    }
    
    redirectAttributes.addFlashAttribute("paymentCallback", true);
    
    // Redirect to payments page which will handle the verification
    return "redirect:/payments";
  }

  @GetMapping("/ledger")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public String ledger() {
    return "ledger";
  }

  @GetMapping("/journal")
  @PreAuthorize("hasAuthority('JOURNAL:READ')")
  public String journal() {
    return "journal";
  }

  @GetMapping("/reconciliation")
  @PreAuthorize("hasAuthority('RECONCILIATION:READ')")
  public String reconciliation() {
    return "reconciliation";
  }

  @GetMapping("/reports")
  @PreAuthorize("hasAuthority('REPORT:READ')")
  public String reports() {
    return "reports";
  }

  @GetMapping("/ai-insights")
  @PreAuthorize("hasAuthority('AI:READ')")
  public String aiInsights() {
    return "ai-insights";
  }
}
