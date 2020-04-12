package com.mtli.model.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;

/**
 * @Description: 对SimpleMailMessage进行封装
 * @Author: Mt.Li
 */
public class MailMessage {

    // 注入配置文件的username值
    @Value("${spring.mail.username}")
    private String fromMail;

    private MailMessage() {
    }

    public SimpleMailMessage create(String toMail, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromMail);
        message.setTo(toMail);
        message.setSubject(subject);
        message.setText(text);
        return message;
    }
}
