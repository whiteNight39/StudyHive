package com.studyhive.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {

        this.mailSender = mailSender;
    }

    public String MSG_ENROLLMENT_BODY = "Please use the 6 digit code... %s  to complete your enrollment.\nOTP expires after 1 hour.";
    public String MSG_ENROLLMENT_TITLE ="Enrollment";

    public String MSG_PASSWORD_RESET_BODY = "Please use the 6 digit code... %s  to reset your password.\nOTP expires after 1 hour.";
    public String MSG_PASSWORD_RESET_TITLE ="Password Reset";


    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String fromEmail = "titania100039@gmail.com";
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
