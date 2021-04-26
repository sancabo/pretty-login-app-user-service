package com.devsancabo.users.services;

import com.devsancabo.users.dto.*;
import com.devsancabo.users.exception.ApiException;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
  AuthenticateResponseDTO authenticate(String user, String password) throws ApiException;

  RegisterResponseDTO register(RegisterRequestDTO request) throws ApiException;

  String confirmMail(String verificationToken) throws ApiException;

  UserDTO getUserById(Long userId, String jwt);

  String changePassword(final ChangePasswordRequestDTO request, String jwt) throws ApiException;
  String resetPassword(final ResetPasswordRequestDTO request) throws ApiException;
  ConfirmCodeForResetPasswordDTO confirmCodeForResetPassword(Integer code) throws ApiException;
  String changePasswordAfterReset(ChangePasswordAfterResetDTO theDto, String jwt) throws ApiException;
}
