package com.devsancabo.users.utils;

import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Component
public class PasswordRecoveryMessagePreparator extends ConfigurableMessagePreparator {


  @Override
  public void prepareWithParams(MimeMessage mimeMessage) throws MessagingException, UnsupportedEncodingException {
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
    helper.setTo(super.user.getEmail());
    helper.setFrom(new InternetAddress(super.from, "Devsancabo's Login Page"));
    helper.setSubject("Password Recovery");
    String text = "<html><body><h1>Hello, this is your password recovery code: "
      + super.params.get("code") + "</h1></body></html>";
    helper.setText(text, true);
  }
}
