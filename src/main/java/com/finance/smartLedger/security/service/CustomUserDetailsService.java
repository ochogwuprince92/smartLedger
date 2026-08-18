package com.finance.smartLedger.security.service;

import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.infrastructure.persistence.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    if (!user.getEnabled()) {
      throw new UsernameNotFoundException("User account is disabled: " + username);
    }

    if (user.isAccountLocked()) {
      throw new UsernameNotFoundException("User account is locked: " + username);
    }

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getUsername())
        .password(user.getPassword())
        .authorities(getAuthorities(user))
        .accountExpired(!user.getAccountNonExpired())
        .accountLocked(!user.getAccountNonLocked())
        .credentialsExpired(!user.getCredentialsNonExpired())
        .disabled(!user.getEnabled())
        .build();
  }

  private Collection<? extends GrantedAuthority> getAuthorities(User user) {
    List<GrantedAuthority> authorities = new ArrayList<>();

    authorities.addAll(
        user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
            .collect(Collectors.toList()));

    authorities.addAll(
        user.getPermissions().stream()
            .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
            .collect(Collectors.toList()));

    authorities.addAll(
        user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
            .collect(Collectors.toList()));

    return authorities;
  }
}
