package com.devsancabo.users.utils;

import com.devsancabo.users.entity.User;
import com.devsancabo.users.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public abstract class ConfigurableMessagePreparator implements MimeMessagePreparator {

  protected Map<String, String> params;
  protected User user;
  @Value("${spring.mail.username}")
  protected String from;

  public ConfigurableMessagePreparator feedParams(User user, Map<String, String> params){
    this.params = params;
    this.user = user;
    return this;
  }
  @Override
  public void prepare(MimeMessage mimeMessage) throws Exception {
    if (this.params == null || this.user == null) throw new ApiException("Params for email template not set");
    prepareWithParams(mimeMessage);
    this.params = null;
    this.user = null;
  }

  public abstract void prepareWithParams(MimeMessage mimeMessage) throws MessagingException, UnsupportedEncodingException;
}
