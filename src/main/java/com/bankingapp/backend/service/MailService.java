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
    public void sendMail(String mailText, String username, String firstName, String lastName, String email, int customerId) throws MailjetException, MailjetSocketTimeoutException {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        client = new MailjetClient("MAIL_API_KEY", "MAIL_API_SECRET", new ClientOptions("v3.1"));
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
                                .put(Emailv31.Message.TEXTPART, "Customer name: " + firstName + " " + lastName + " \n" +
                                                                "Customer username: " + username + " \n " +
                                                                "Customer E-Mail: " + email + " \n " +
                                                                "Customer ID: " + customerId + " \n\n" +
                                                                "Message text: " + mailText)));
        response = client.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
    }
}
