package com.devsancabo.users.services;

import com.devsancabo.users.dto.*;
import com.devsancabo.users.entity.PasswordRecovery;
import com.devsancabo.users.entity.User;
import com.devsancabo.users.entity.Verification;
import com.devsancabo.users.exception.ApiException;
import com.devsancabo.users.repository.PasswordRecoveryRepository;
import com.devsancabo.users.repository.UserRepository;
import com.devsancabo.users.repository.VerificationRepository;
import com.devsancabo.users.utils.MailResolver;
import io.jsonwebtoken.InvalidClaimException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final VerificationRepository verificationRepository;
  private final PasswordRecoveryRepository passwordRecoveryRepository;
  private final SecureRandom random = new SecureRandom();
  private final BCryptPasswordEncoder encoderForPasswords = new BCryptPasswordEncoder(9,random);
  private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
  private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
  private final MailResolver mailResolver;

  @Autowired
  UserServiceImpl(final UserRepository repository,
                  final VerificationRepository verificationRepository,
                  final PasswordRecoveryRepository passwordRecoveryRepository,
                  final MailResolver mailResolver){
    this.userRepository = repository;
    this.verificationRepository = verificationRepository;
    this.passwordRecoveryRepository =  passwordRecoveryRepository;
    this.mailResolver = mailResolver;
  }

  @Override
  public AuthenticateResponseDTO authenticate(final String user, final String password) throws ApiException {
    logger.info("Authenticating user : {}", user);
    List<User> userResult = userRepository.findByUsername(user);
    if(userResult.isEmpty()) throw new ApiException("User not found");
    else {
      List<User> validUsers =
      userResult.stream().filter(u -> encoderForPasswords.matches(password, u.getPassword())).collect(Collectors.toList());
      if(validUsers.isEmpty()) throw new ApiException("Incorrect Password");
      else {
        final String jws = buildToken(new HashMap<>());
        logger.info("Authenticated User: {} ", user);
        return new AuthenticateResponseDTO(jws);
      }
    }
  }

  private String buildToken(Map<String, String> claims) {
    final Date now = Date.from(Instant.now());
    final Date nowPlusOneDay = Date.from(now.toInstant().plus(Duration.ofDays(1)));
    return Jwts.builder()
      .setClaims(claims)
      .setIssuer("user-service")
      .setSubject("material-ui-page")
      .setIssuedAt(now)
      .setExpiration(nowPlusOneDay)
      .signWith(this.key)
      .compact();
  }

  @Override
  @Transactional
  public RegisterResponseDTO register(RegisterRequestDTO request) throws ApiException{
    logger.info("About to save new User: {} ", request.getUsername());
    List<User> existingUsers =  userRepository.findByUsername(request.getUsername());
    if(!existingUsers.isEmpty()) throw new ApiException("User already exists");
    existingUsers =  userRepository.findByEmail(request.getEmail());
    if(!existingUsers.isEmpty()) throw new ApiException("That mail is already registered");
    final User newUser = convert(request);
    try {
      userRepository.save(newUser);
    } catch (Exception e){
      throw new ApiException("Error Saving User: " + newUser);
    }
    final Verification verification= getVerification(newUser.getId());
    try {
      this.verificationRepository.save(verification);
    } catch (Exception e){
      throw new ApiException("Error Saving User: " + newUser);
    }
    mailResolver.sendMailVerify(newUser,verification);
    RegisterResponseDTO response = new RegisterResponseDTO();
    response.setStatus("Ok");
    logger.info("Registered User: {} ", request.getUsername());
    return response;
  }

  @Override
  @Transactional
  public String confirmMail(String verificationToken) {
    List<Verification> found = this.verificationRepository.findByToken(verificationToken);
    if(found.isEmpty()) throw new ApiException("Invalid verification token.");
    Verification verification = found.get(0);
    User user = null;
    try {
      user = this.userRepository.getOne(verification.getUserId());
    } catch (EntityNotFoundException e){
      throw new ApiException("Token exists, but user doesn't");
    }
    user.setVerified(true);
    try {
      this.userRepository.save(user);
      this.verificationRepository.delete(verification);
    } catch (EntityNotFoundException e) {
      throw new ApiException("Error updating user.");
    }
    return "Ok";
  }

  @Override
  public UserDTO getUserById(final Long userId, final String jwt) {
    return toUserDto(this.userRepository.findById(userId).orElseThrow(() -> new ApiException("User not found")));
  }

  private UserDTO toUserDto(User user) {
    var userDto = new UserDTO();
    userDto.setUsername(user.getUsername());
    userDto.setEmail(user.getEmail());
    userDto.setId(user.getId());
    userDto.setVerified(user.getVerified());
    return userDto;
  }

  @Override
  public String changePassword(final ChangePasswordRequestDTO request, String jwt) {
    try {
      Jwts.parserBuilder()
        .setSigningKey(this.key)
        .requireIssuer("user-service")
        .requireSubject("material-ui-page")
        .build().parseClaimsJws(getTokenString(jwt));
    } catch(InvalidClaimException ice) {
      throw new ApiException("Invalid token: claim \"" + ice.getClaimName() + "\" is invalid");
    } catch (Exception e) {
      throw new ApiException("Error validating token");
    }
    User user = userRepository.findById(request.getUserId())
      .orElseThrow(() -> new ApiException("User does not exist."));
    if (this.encoderForPasswords.matches(request.getOldPassword(), user.getPassword())){
      user.setPassword(encoderForPasswords.encode(request.getNewPassword()));
      userRepository.save(user);
    } else {
      throw new ApiException("Current password is incorrect.");
    }
    return "Ok";
  }


  @Override
  @Transactional
  public String resetPassword(final ResetPasswordRequestDTO request) {
    User user = userRepository.findByEmail(request.getEmail()).stream()
      .findFirst().orElseThrow(() -> new ApiException("User not found"));
    Integer recoveryCode = random.nextInt(999999);
    PasswordRecovery record = buildRecovery(request.getEmail(), recoveryCode);
    try {
      List<PasswordRecovery> existing = passwordRecoveryRepository.findByEmail(request.getEmail());
      logger.info("Found existing recovery code for {}. Deleting.", existing.get(0).getUserEmail());
      if (!existing.isEmpty()) passwordRecoveryRepository.delete(existing.get(0));
      logger.info("Existing recovery code deleted.");
    } catch (Exception e) {
      throw new ApiException("Could not delete previous recovery code.");
    }
    try {
      passwordRecoveryRepository.save(record);
    } catch(Exception e) {
      throw new ApiException("Could not save recovery request");
    }
    logger.info("Sent recovery code to {}", request.getEmail(), recoveryCode);
    mailResolver.sendMailRecovery(user, recoveryCode);
    return "Ok";
  }

  @Override
  public ConfirmCodeForResetPasswordDTO confirmCodeForResetPassword(Integer code){
    PasswordRecovery recovery = this.passwordRecoveryRepository.findByCodeHash(code.toString()).stream()
      .findFirst().orElseThrow(() -> new ApiException("No recovery found"));
    logger.info("Issued at: {}, Now: {} ", recovery.getIssuedAt(), Date.from(Instant.now()));
    if(recovery.getIssuedAt().toInstant().plusSeconds(70).isBefore(Instant.now())) {
      this.passwordRecoveryRepository.delete(recovery);
      throw new ApiException("Recovery has expired");
    }
    User user = userRepository.findById(recovery.getUserId()).orElseThrow(() -> new ApiException("User not found"));
    var claims = new HashMap<String, String>();
    claims.put("userId", user.getId().toString());
    claims.put("role", "PASSWORD_RECOVERY");
    String token = buildToken(claims);
    this.passwordRecoveryRepository.delete(recovery);
    return new ConfirmCodeForResetPasswordDTO(token);
  }

  @Override
  public String changePasswordAfterReset(ChangePasswordAfterResetDTO passwordReset, String jwt){
    logger.info("Attempting to validate token at header: {}", jwt);
    String userIdClaim;
    try {
      userIdClaim = Jwts.parserBuilder()
        .setSigningKey(this.key)
        .requireIssuer("user-service")
        .requireSubject("material-ui-page")
        .require("role", "PASSWORD_RECOVERY")
        .build().parseClaimsJws(getTokenString(jwt)).getBody().get("userId", String.class);
    } catch(InvalidClaimException ice) {
      throw new ApiException("Invalid token: claim \"" + ice.getClaimName() + "\" is invalid");
    } catch (Exception e) {
      throw new ApiException("Error validating token");
    }
    logger.info("Token for password reset is valid");
    Long userId;
    try {
      userId = Long.decode(userIdClaim);
    } catch (NumberFormatException e) {
      throw new ApiException("Invalid userId value.");
    }
    User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("User not found"));
    user.setPassword(encoderForPasswords.encode(passwordReset.getNewPassword()));
    try {
      this.userRepository.save(user);
    } catch (Exception e){
      throw new ApiException("Could not save password");
    }
    logger.info("Changed password for userId = {}", userId);
    return "Ok";
  }

  private User convert(final RegisterRequestDTO request) {
    final User newUser = new User();
    newUser.setUsername(request.getUsername());
    newUser.setPassword(encoderForPasswords.encode(request.getPassword()));
    newUser.setEmail(request.getEmail());
    newUser.setVerified(false);
    return newUser;
  }

  private Verification getVerification(final Long userId){
    final var verification= new Verification();
    verification.setUserId(userId);
    verification.setToken(java.util.UUID.randomUUID().toString());
    return verification;
  }

  private String getTokenString(String header){
    return header.replace("Bearer ","");

  }

  private PasswordRecovery buildRecovery(String email, Integer code){
    PasswordRecovery record = new PasswordRecovery();
    record.setIssuedAt(Date.from(Instant.now()));
    record.setUserId(1L);
    record.setCodeHash(code.toString());
    record.setUserEmail(email);
    return record;
  }

}
