/*
 * MailSender.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.lucaseasedup.logit.mail;

import io.github.lucaseasedup.logit.Disposable;
import io.github.lucaseasedup.logit.LogItCoreObject;
import java.io.IOException;
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

public final class MailSender extends LogItCoreObject implements Disposable
{
    @Override
    public void dispose()
    {
        if (properties != null)
        {
            properties.clear();
            properties = null;
        }
        
        user = null;
        password = null;
    }
    
    /**
     * Configures this mail sender for a SMTP server.
     * 
     * @param host     the host.
     * @param port     the port number.
     * @param user     the user.
     * @param password the password.
     */
    public void configure(String host, int port, String user, String password)
    {
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", String.valueOf(port));
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.socketFactory.port", String.valueOf(port));
		properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        
        this.user = user;
        this.password = password;
    }
    
    /**
     * Sends a mail through a SMTP server.
     * 
     * <p> Configure this {@code MailSender} using
     * {@link #configure(String, int, String, String)} before calling this method.
     * 
     * @param to      a collection of recipient addresses to which the mail will be sent.
     * @param from    the sender address.
     * @param subject the mail subject.
     * @param body    the mail body.
     * @param html    whether HTML should be enabled in this mail.
     * 
     * @throws IOException if an I/O error occurred.
     */
    public void sendMail(Collection<String> to,
                         String from,
                         String subject,
                         String body,
                         boolean html) throws IOException
    {
        Session session = Session.getDefaultInstance(properties, new Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(user, password);
            }
        });
        MimeMessage message = new MimeMessage(session);
        
        try
        {
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
        catch (MessagingException ex)
        {
            throw new IOException(ex);
        }
    }
    
    private Properties properties = new Properties();
    private String user;
    private String password;
}
