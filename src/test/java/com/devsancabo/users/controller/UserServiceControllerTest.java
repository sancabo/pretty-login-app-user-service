package com.devsancabo.users.controller;

import com.devsancabo.users.dto.AuthenticateResponseDTO;
import com.devsancabo.users.dto.RegisterRequestDTO;
import com.devsancabo.users.dto.RegisterResponseDTO;
import com.devsancabo.users.services.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class UserServiceControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserServiceImpl service;

  private static final AuthenticateResponseDTO AUTH_RESPONSE = new AuthenticateResponseDTO("token");
  private static final RegisterResponseDTO REGISTER_RESPONSE = new RegisterResponseDTO("Ok");

  @BeforeAll()
  public void initService() {
    Mockito.when(service.authenticate(Mockito.anyString(),Mockito.anyString()))
      .thenReturn(AUTH_RESPONSE);
    Mockito.when(service.register(Mockito.any(RegisterRequestDTO.class)))
      .thenReturn(REGISTER_RESPONSE);

  }

  @Test()
  private void wrongUrl() throws Exception {
    mockMvc.perform(
      get("/sarasa")).andExpect(status().is4xxClientError());
  }

  @Test()
  private void register() throws Exception {
    RegisterRequestDTO request = new RegisterRequestDTO("Santiagus", "Cabus", "a@a.com");
    ObjectMapper mapper = new ObjectMapper();

    mockMvc.perform(
      post("/register").content(mapper.writeValueAsString(request)))
      .andExpect(status().is2xxSuccessful());
  }

  @Test
  private void authenticate() throws Exception{
    String username = "Satiagus";
    String password = "Cabus";
    mockMvc.perform(
      get("/authenticate").param("user",username).param("password",password)).andExpect(
        content().json("{\"token\":\"token\"}")
    );
  }
}
