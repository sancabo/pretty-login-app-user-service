package com.devsancabo.users.services;

import com.devsancabo.users.dto.AuthenticateResponseDTO;
import com.devsancabo.users.dto.RegisterRequestDTO;
import com.devsancabo.users.dto.RegisterResponseDTO;
import com.devsancabo.users.entity.User;
import com.devsancabo.users.exception.ApiException;
import com.devsancabo.users.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class UserServiceImplTest {
  @Autowired
  private UserServiceImpl service;

  @MockBean
  private UserRepository repository;

  private static final User USER = getUser("Santiagus", "Cabus");
  private static final User NON_EXISTANT_USER = getUser("John", "Doe");
  private static final String STATUS_OK = "Ok";

  private static User getUser(String username, String pass){
    User user = new User();
    user.setUsername(username);
    user.setId(1L);
    user.setPassword(pass);
    return user;
  }

  @BeforeAll
  private void initRepo(){
    Mockito.when(repository.save(Mockito.any(User.class))).thenReturn(USER);
    Mockito.when(
      repository.findByUsername(Mockito.eq(USER.getUsername())))
      .thenReturn(Arrays.asList(new User[]{USER}));
    Mockito.when(
    repository.findByUsername(
      Mockito.eq(NON_EXISTANT_USER.getUsername())))
      .thenReturn(new ArrayList<>());
  }
  @Test
  private void register(){
    RegisterRequestDTO requestDTO = new RegisterRequestDTO();
    requestDTO.setUsername(USER.getUsername());
    requestDTO.setPassword(USER.getPassword());
    RegisterResponseDTO response = service.register(requestDTO);
    Assertions.assertEquals(STATUS_OK,response.getStatus());
  }

  @Test
  private void registerFails(){

  }

  @Test
  private void authenticate(){
    final AuthenticateResponseDTO result = service.authenticate(USER.getUsername(), USER.getPassword());
    Assertions.assertEquals("token", result.getToken());

  }

  @Test
  private void authenticateFails(){
    Assertions.assertThrows(
      ApiException.class,
      () -> service.authenticate(NON_EXISTANT_USER.getUsername(), NON_EXISTANT_USER.getPassword()));

  }
}
