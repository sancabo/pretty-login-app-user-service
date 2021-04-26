package com.devsancabo.users.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "USER")
public class User {
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;

  @Column(name = "USERNAME", nullable = false)
  private String username;

  @Column(name = "PASSWORD", nullable =false)
  private String password;

  @Column(name = "EMAIL", nullable =false)
  private String email;

  @Column(name = "VERIFIED", nullable =false)
  private Boolean verified;
}

