package com.devsancabo.users.controller;

import com.devsancabo.users.dto.*;
import com.devsancabo.users.exception.ApiException;
import com.devsancabo.users.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
//TODO: Cors: only allow webpageDomain, allow all for test profile
//TODO: Add an API key to all requests

/**
 * Problem 1: DDOS attack.
 * Problem 2: An attacker can bruteforce resetpassword/confirmCode
 * Possible confirm codes are 10_000_000
 * I cannot keep secret in client side.
 * A solution: make confirm code last 500ms
 */

@Controller
public class UserServiceController {
  private final UserService service;
  private final Logger logger = LoggerFactory.getLogger(UserServiceController.class);

  @Autowired
  public UserServiceController(final UserService service){
    this.service = service;
  }

  @CrossOrigin(origins = "${user.service.cors.allowed:*}")
  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticateResponseDTO> authenticate(@RequestBody AuthenticateRequestDTO request) throws ApiException {
    return ResponseEntity.of(Optional.of(service.authenticate(request.getUsername(), request.getPassword())));

  }

  @CrossOrigin(origins = "${user.service.cors.allowed:*}")
  @PostMapping("/register")
  public ResponseEntity<RegisterResponseDTO> register(@RequestBody RegisterRequestDTO request) throws ApiException{
    logger.info("Register being called");
    return ResponseEntity.of(Optional.of(service.register(request)));

  }

  @CrossOrigin(origins = "${user.service.cors.allowed:*}")
  @GetMapping("/confirm/{verificationToken}")
  public ResponseEntity<RegisterResponseDTO> confirm(@PathVariable String verificationToken) throws ApiException{
    logger.info("Confirmation called for token : {}", verificationToken);
    return ResponseEntity.of(Optional.of(new RegisterResponseDTO(service.confirmMail(verificationToken))));
  }

  @CrossOrigin(origins = "${user.service.cors.allowed:*}")
  @GetMapping("/user/{userId}")
  public ResponseEntity<UserDTO> getUserById(@PathVariable final Long userId,
                                             @RequestHeader("Authorization") String jwtoken) throws ApiException{
    logger.info("Getting user by id: {}", userId);
    return ResponseEntity.of(Optional.of(this.service.getUserById(userId, jwtoken)));
  }

  @CrossOrigin(origins = "${user.service.cors.allowed:*}")
  @PostMapping("/passwordRecovery")
  public ResponseEntity<Object> passwordRecovery(@RequestBody ResetPasswordRequestDTO body) throws ApiException{
    logger.info("Reset password, mail: {}", body.getEmail());
    service.resetPassword(body);
    return ResponseEntity.ok().build();
  }

  @CrossOrigin(origins = "${user.service.cors.allowed:*}")
  @GetMapping("/confirmRecoveryCode")
  public ResponseEntity<ConfirmCodeForResetPasswordDTO> confirmRecoveryCode(
    @RequestParam(name="code") final Integer recoveryCode) throws ApiException{
    logger.info("Received recovery code:  {}", recoveryCode);
    return ResponseEntity.of(Optional.of(service.confirmCodeForResetPassword(recoveryCode)));
  }

  @CrossOrigin(origins = "${user.service.cors.allowed:*}")
  @PostMapping("/changePasswordAfterReset")
  public ResponseEntity<Object> changePasswordAfterReset(@RequestBody ChangePasswordAfterResetDTO body,
                                                         @RequestHeader(name="Authorization") String jwt) throws ApiException{
    logger.info("Changing password after a reset.");
    this.service.changePasswordAfterReset(body, jwt);
    return ResponseEntity.ok().build();
  }

}
