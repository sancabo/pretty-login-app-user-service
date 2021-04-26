package com.devsancabo.users.utils;

import com.devsancabo.users.entity.User;
import com.devsancabo.users.entity.Verification;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface MailResolver {

  enum MailType{
    VERIFY_ACCOUNT, RECOVER_FORGOTTEN_PASSWORD
  }

  @Async
  void sendMail(MailType type, User user, Map<String, String> params);

  @Async
  void sendMailVerify(final User user, final Verification verification);

  @Async
  void sendMailRecovery(final User user, final Integer recoveryCode);
}
