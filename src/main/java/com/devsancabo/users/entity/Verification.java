package com.devsancabo.users.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "USER_VERIFICATION")
public class Verification {

  @Id
  @GeneratedValue(strategy= GenerationType.AUTO)
  private Long id;

  @Column(name = "USER_ID", nullable =false)
  private Long userId;

  @Column(name = "VERIFICATION_TOKEN", nullable =false)
  private String token;
}
