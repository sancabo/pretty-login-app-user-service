package com.devsancabo.users.repository;

import com.devsancabo.users.entity.PasswordRecovery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordRecoveryRepository extends JpaRepository<PasswordRecovery, Long> {
  List<PasswordRecovery> findByCodeHash(String code);
  List<PasswordRecovery> findByEmail(String code);
}
