package com.bankingapp.backend.service;

import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;

public class MailService {
    public void sendMail(String mailText, String firstName, String lastName, String email, int customerId) throws MailjetException, MailjetSocketTimeoutException {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        client = new MailjetClient(System.getenv("MAIL_API_KEY"), System.getenv("MAIL_API_SECRET"), new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", "bogy.help.sender@hotmail.com")
                                        .put("Name", "Bank"))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", "bogy.help.receiver@hotmail.com")
                                                .put("Name", "Bank")))
                                .put(Emailv31.Message.SUBJECT, "Bank Support Request From " + firstName + " " + lastName)
                                .put(Emailv31.Message.TEXTPART, "Customer name: " + firstName + " " + lastName + "\n" +
                                                                "Customer E-Mail: " + email + "\n" +
                                                                "Customer ID: " + customerId + "\n\n" +
                                                                "Message text: " + mailText)));
        response = client.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
    }
    /* function responsible for sending two-factor authentication code in an email to the customer */
    public void sendTwoFactorCode(String code, String firstName, String lastName, String email) throws MailjetException, MailjetSocketTimeoutException {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        client = new MailjetClient(System.getenv("MAIL_API_KEY"), System.getenv("MAIL_API_SECRET"), new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", "bogy.help.sender@hotmail.com")
                                        .put("Name", "Bank of Galway - verification"))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", email)
                                                .put("Name", firstName + " " + lastName)))
                                .put(Emailv31.Message.SUBJECT, "Your Two-Factor Authentication Code")
                                .put(Emailv31.Message.TEXTPART, "Dear " + firstName + " " + lastName + ",\n\n" +
                                        "Your login verification code is: " + code + "\n\n" +
                                        "Please enter this code to complete your login.")));
        response = client.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
    }
    /* function responsible for sending password reset email to the user */
    public void sendPasswordResetMail(String resetToken, String email) throws MailjetException, MailjetSocketTimeoutException {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        client = new MailjetClient(System.getenv("MAIL_API_KEY"), System.getenv("MAIL_API_SECRET"), new ClientOptions("v3.1"));

        /* construct the reset URL */
        String resetUrl = "https://bogproject.online:8443/api/reset-password?token=" + resetToken;
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", "bogy.help.sender@hotmail.com")
                                        .put("Name", "Bank of Galway"))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", email)))
                                .put(Emailv31.Message.SUBJECT, "Password Reset Request")
                                .put(Emailv31.Message.TEXTPART, "Hello,\n\n" +
                                        "We received a request to reset your password. Please click the link below to reset your password:\n" +
                                        resetUrl + "\n\n" +
                                        "If you did not request a password reset, please ignore this email.\n\n" +
                                        "Thank you,\nBank of Galway")));
        response = client.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
    }
}
