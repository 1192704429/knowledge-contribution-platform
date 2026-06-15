package com.kp.mall.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * @author qnzs
 * @description: 邮件发送
 * @date 2025/5/21 17:02
 */
@Component
public class MailUtil {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * 发送简单邮件
     *
     * @param subject 主题
     * @param text    内容
     * @param toEmail 收件人
     */
    public void sendSimpleMail(String subject, String text, String toEmail) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);
        javaMailSender.send(mailMessage);
    }


}
