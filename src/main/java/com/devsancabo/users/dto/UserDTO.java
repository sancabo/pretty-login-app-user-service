package com.devsancabo.users.dto;

import lombok.Data;

@Data
public class UserDTO {
  private Long id;
  private String username;
  private String email;
  private Boolean verified;
}
