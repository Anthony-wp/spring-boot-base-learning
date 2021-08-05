package com.softkit.service;
import com.softkit.configuration.EmailConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

@Service
public class EmailService {

    @Autowired
    EmailConfiguration emailConfiguration;


    public void sendMail(String recipient, String msg){
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", emailConfiguration.getHost());
        prop.put("mail.smtp.port", emailConfiguration.getPort());
        prop.put("mail.smtp.ssl.trust", emailConfiguration.getHost());

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailConfiguration.getUsername(), emailConfiguration.getPassword());
            }
        });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConfiguration.getUsername()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject("Registration successful");

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msg, "text/html");

//            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
//            attachmentBodyPart.attachFile(new File("pom.xml"));

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
//            multipart.addBodyPart(attachmentBodyPart);

            message.setContent(multipart);

            Transport.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
