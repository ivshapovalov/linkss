package ru.ivan.linkss.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.ivan.linkss.repository.entity.User;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class Mail {

    @Autowired
    @Qualifier(value = "mailSender")
    private JavaMailSender mailSender;

    public void sendVerifyEmail(User user, String verifyURL) {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            String htmlMsg = String.format(
                    "<html><body><h4>Hello, %s. </H4>" +
                            "Your credentials is:" +
                            "<p>Username:" + user.getUserName() + "<br>" +
                            "E-mail:" + user.getEmail() + "<br>" +
                            "Password:" + user.getPassword() + "</p>" +
                            String.format("Verify your email by click <a href=\"%s\">Verify " +
                                    "URL</a>", verifyURL) +
                            "</body></html>", user
                            .getUserName
                                    ());
            mimeMessage.setContent(htmlMsg, "text/html");
            helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
            helper.setTo(user.getEmail());
            helper.setSubject(String.format("Linkss app. Verify registration, '%s'", user
                    .getUserName
                            ()));
            helper.setFrom("linkss.verify@gmail.com");
            mailSender.send(mimeMessage);
            System.out.println("Mail sended");

        } catch (MessagingException e) {
            System.out.println("Mail sendVerifyEmail failed.");

            e.printStackTrace();
        }
    }

    public void sendRemindEmail(Map<User, String> users) {

        String email = ((User)(users.keySet().toArray()[0])).getEmail();
        StringBuilder credentials = new StringBuilder();
        users.entrySet().forEach(user -> {
                    credentials
                            .append("<p>Username:").append(user.getKey().getUserName()).append("<br>")
                            .append("E-mail:").append(user.getKey().getEmail()).append("<br>")
                            .append("Password:").append(user.getKey().getPassword()).append("<br>");
                    if (user.getValue()!=null) {
                        credentials.append("<a href=\"" + user.getValue()+"\">Verified url</a>");
                    }
                    credentials.append("</p>");
                }
        );
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            String htmlMsg =
                    "<html><body><h4>Hello. </H4>" +
                            "Your credentials is:" +
                            credentials.toString() +
                            "</body></html>";
            mimeMessage.setContent(htmlMsg, "text/html");
            helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
            helper.setTo(email);
            helper.setSubject(String.format("Linkss app. Credentials for email '%s'", email));
            helper.setFrom("linkss.verify@gmail.com");
            mailSender.send(mimeMessage);
            System.out.println("Mail sended");

        } catch (MessagingException e) {
            System.out.println("Mail sendVerifyEmail failed.");

            e.printStackTrace();
        }

    }
}
