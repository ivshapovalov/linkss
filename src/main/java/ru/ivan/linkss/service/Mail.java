package ru.ivan.linkss.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.ivan.linkss.repository.entity.User;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class Mail {

    @Autowired
    @Qualifier(value = "mailSender")
    JavaMailSender mailSender;

    public void send(User user, String verifyURL) {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
              String htmlMsg = String.format(
                      "<html><body><h3>Hello, %s. Verify your email by url</H3>" +
                              "<br>" +
                              String.format("<a href=\"%s\">Verify URL</a>",verifyURL) +
                              "</body></html>", user
                    .getUserName
                            ());
            mimeMessage.setContent(htmlMsg, "text/html");
            helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
            helper.setTo(user.getEmail());
            helper.setSubject(String.format("Linkss. Verify URL for user '%s'",user.getUserName()));
            //helper.setText(htmlMsg);
            helper.setFrom("linkss.verify@gmail.com");
            mailSender.send(mimeMessage);
            System.out.println("Mail sended");

        } catch (MessagingException e) {
            System.out.println("Mail send failed.");

            e.printStackTrace();
        }

    }
}
