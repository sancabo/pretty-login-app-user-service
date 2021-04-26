package com.devsancabo.users.repository;

import com.devsancabo.users.entity.Verification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
  List<Verification> findByUserId(final Long userId);
  List<Verification> findByToken(final String token);
}
