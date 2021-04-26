package com.devsancabo.users.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "USER_PASSWORD_RECOVERY")
public class PasswordRecovery {
  @Id
  @GeneratedValue(strategy= GenerationType.AUTO)
  private Long id;

  @Column(name = "USER_ID", nullable = false)
  private Long userId;

  @Column(name = "CODE", nullable = false)
  private String codeHash;

  @Column(name = "ISSUED_AT", nullable = false)
  private Date issuedAt;

  @Column(name = "USER_EMAIL", nullable = false)
  private String userEmail;
}
