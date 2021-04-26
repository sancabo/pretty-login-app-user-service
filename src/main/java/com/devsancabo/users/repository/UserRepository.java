package com.devsancabo.users.repository;

import com.devsancabo.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  List<User> findByUsername(final String user);
  List<User> findByEmail(final String email);
}
