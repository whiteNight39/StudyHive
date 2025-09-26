package com.studyhive.service;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailGunService {

    @Value("${MAILGUN_API_KEY}")
    private String apiKey;

    @Value("${MAILGUN_DOMAIN}")
    private String domain;

    // ================== Message Templates ==================
    public String MSG_ENROLLMENT_BODY = "Please use the 6 digit code... %s to complete your enrollment.\nOTP expires after 1 hour.";
    public String MSG_ENROLLMENT_TITLE = "Enrollment";

    public String MSG_PASSWORD_RESET_BODY = "Please use the 6 digit code... %s to reset your password.\nOTP expires after 1 hour.";
    public String MSG_PASSWORD_RESET_TITLE = "Password Reset";
    // ========================================================

    public void sendEmail(String toEmail, String subject, String body) {
        try {
            HttpResponse<JsonNode> response = Unirest.post("https://api.mailgun.net/v3/" + domain + "/messages")
                    .basicAuth("api", apiKey)
                    .field("from", "Your App <mailgun@" + domain + ">")
                    .field("to", toEmail)
                    .field("subject", subject)
                    .field("text", body)
                    .asJson();

            System.out.println("Mailgun response: " + response.getBody());
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
