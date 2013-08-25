/*
 * MailSender.java
 *
 * Copyright (C) 2012-2013 LucasEasedUp
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

import io.github.lucaseasedup.logit.LogItCoreObject;
import java.io.IOException;
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

public final class MailSender extends LogItCoreObject
{
    public MailSender()
    {
        this.properties = new Properties();
    }
    
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
    
    public void sendMail(String[] to, String from, String subject, String body, boolean html)
            throws IOException
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
            
            for (int i = 0; i < to.length; i++)
            {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
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
    
    private final Properties properties;
    private String user;
    private String password;
}
