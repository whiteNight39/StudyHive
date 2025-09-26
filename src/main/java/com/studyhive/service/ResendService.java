package com.studyhive.service;

import com.resend.*;
import com.resend.services.emails.model.SendEmailRequest;
import com.resend.services.emails.model.SendEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResendService {

    private final Resend resend;

    public ResendService(@Value("${RESEND_API_KEY}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    // ================== Message Templates ==================
    public String MSG_ENROLLMENT_BODY = "Please use the 6 digit code... %s to complete your enrollment.\nOTP expires after 1 hour.";
    public String MSG_ENROLLMENT_TITLE = "Enrollment";

    public String MSG_PASSWORD_RESET_BODY = "Please use the 6 digit code... %s to reset your password.\nOTP expires after 1 hour.";
    public String MSG_PASSWORD_RESET_TITLE = "Password Reset";
    // ========================================================

    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .from("onboarding@resend.dev") // For testing. Replace with your verified domain later.
                    .to(toEmail)
                    .subject(subject)
                    .text(body) // use text since your templates are plain
                    .build();

            SendEmailResponse response = resend.emails().send(request);
            System.out.println("Resend response: " + response.getId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendEnrollmentEmail(String toEmail, String otp) {
        sendEmail(toEmail, MSG_ENROLLMENT_TITLE, String.format(MSG_ENROLLMENT_BODY, otp));
    }

    public void sendPasswordResetEmail(String toEmail, String otp) {
        sendEmail(toEmail, MSG_PASSWORD_RESET_TITLE, String.format(MSG_PASSWORD_RESET_BODY, otp));
    }
}