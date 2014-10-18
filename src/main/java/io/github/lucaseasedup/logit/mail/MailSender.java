package io.github.lucaseasedup.logit.mail;

import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class MailSender
{
    private MailSender(String host, int port, String user, String password)
    {
        if (host == null || port < 0 || user == null || password == null)
            throw new IllegalArgumentException();
        
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }
    
    /**
     * Sends a mail through a SMTP server.
     * 
     * @param to      a collection of recipient addresses to which the mail will be sent.
     * @param from    the sender address.
     * @param subject the mail subject.
     * @param body    the mail body.
     * @param html    whether HTML should be enabled in this mail.
     * 
     * @throws MessagingException if an error occurred.
     */
    public void sendMail(Collection<String> to,
                         String from,
                         String subject,
                         String body,
                         boolean html) throws MessagingException
    {
        if (to == null || from == null || subject == null || body == null)
            throw new IllegalArgumentException();
        
        Properties properties = new Properties();
        
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", String.valueOf(port));
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.socketFactory.port", String.valueOf(port));
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        
        Session session = Session.getDefaultInstance(properties, new Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(user, password);
            }
        });
        MimeMessage message = new MimeMessage(session);
        
        message.setFrom(new InternetAddress(from));
        
        for (String address : to)
        {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
        }
        
        message.setSentDate(new Date());
        message.setSubject(subject);
        
        if (html)
        {
            message.setContent(body, "text/html; charset=utf-8");
        }
        else
        {
            message.setText(body);
        }
        
        message.saveChanges();
        
        Transport.send(message);
    }
    
    public static MailSender from(String host, int port, String user, String password)
    {
        return new MailSender(host, port, user, password);
    }
    
    private final String host;
    private final int port;
    private final String user;
    private final String password;
}
