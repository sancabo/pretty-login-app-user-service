package com.devsancabo.users.dto;

import lombok.Data;

@Data
public class AuthenticateRequestDTO {
  private String username;
  private String password;
}
