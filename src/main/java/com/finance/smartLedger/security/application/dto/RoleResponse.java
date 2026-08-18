package com.finance.smartLedger.security.application.dto;

import com.finance.smartLedger.security.domain.Role;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class RoleResponse {

  private UUID id;
  private String code;
  private String name;
  private String description;
  private Integer level;
  private Set<String> permissions;
  private Set<String> childRoles;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static RoleResponse from(Role role) {
    RoleResponse response = new RoleResponse();
    response.setId(role.getId());
    response.setCode(role.getCode());
    response.setName(role.getName());
    response.setDescription(role.getDescription());
    response.setLevel(role.getLevel());
    response.setPermissions(
        role.getPermissions().stream().map(perm -> perm.getCode()).collect(Collectors.toSet()));
    response.setChildRoles(
        role.getChildRoles().stream().map(child -> child.getCode()).collect(Collectors.toSet()));
    response.setCreatedAt(role.getCreatedAt());
    response.setUpdatedAt(role.getUpdatedAt());
    return response;
  }
}
