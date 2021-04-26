package com.devsancabo.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
  @NonNull
  private String username;
  @NonNull
  private String password;
  @NonNull
  private String email;
}
