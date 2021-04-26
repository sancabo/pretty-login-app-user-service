package com.devsancabo.users.utils;

import com.devsancabo.users.entity.User;
import com.devsancabo.users.entity.Verification;
import com.devsancabo.users.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MailResolverImpl implements MailResolver{

  private final Map<MailType, ConfigurableMessagePreparator> handlers = new HashMap<>();
  private final JavaMailSender mailSender;
  @Value("${user.service.web.domain.url}")
  private String webDomain;

  @Autowired
  public MailResolverImpl(final JavaMailSender mailSender,
                          final PasswordRecoveryMessagePreparator passwordRecoveryMessagePreparator,
                          final ValidationMessagePreparator validationMessagePreparator){
    handlers.put(MailType.VERIFY_ACCOUNT, validationMessagePreparator);
    handlers.put(MailType.RECOVER_FORGOTTEN_PASSWORD, passwordRecoveryMessagePreparator);
    this.mailSender = mailSender;
  }
  @Override
  @Async
  public void sendMail(MailType type, User user, Map<String, String> params) {
    if(!handlers.containsKey(type)) throw new ApiException("No handler for email");
    mailSender.send(handlers.get(type).feedParams(user,params));
  }
  @Async
  @Override
  public void sendMailVerify(final User user, final Verification verification){
    var params = new HashMap<String,String>();
    params.put("token", verification.getToken());
    params.put("webDomain", this.webDomain);
    sendMail(MailResolver.MailType.VERIFY_ACCOUNT,user, params);
  }
  @Async
  @Override
  public void sendMailRecovery(final User user, final Integer recoveryCode){
    var params = new HashMap<String,String>();
    params.put("code", recoveryCode.toString());
    sendMail(MailResolver.MailType.RECOVER_FORGOTTEN_PASSWORD,user, params);
  }

}
