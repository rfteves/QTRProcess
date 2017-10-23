/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.teves.po;

/**
 *
 * @author rfteves
 */
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class TestGmail {
 
    private static final String SMTP_HOST_NAME = "";
    private static final int SMTP_HOST_PORT = 465;
    private static final String SMTP_AUTH_USER = "";
    private static final String SMTP_AUTH_PWD  = "";

    public static void main(String[] args) throws Exception{
       new TestGmail().test();
    }

    public void test() throws Exception{
        Properties props = new Properties();

        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.host", SMTP_HOST_NAME);
        props.put("mail.smtps.auth", "true");
        // props.put("mail.smtps.quitwait", "false");

        Session mailSession = Session.getDefaultInstance(props);
        mailSession.setDebug(true);
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);
        // message subject
        message.setSubject("Java Programming Forums");
        // message body
        message.setContent("Hey! Visit http://www.JavaProgrammingForums.com", "text/plain");

        message.addRecipient(Message.RecipientType.TO,
             new InternetAddress("rfteves@yahoo.com"));

        transport.connect
          (SMTP_HOST_NAME, SMTP_HOST_PORT, SMTP_AUTH_USER, SMTP_AUTH_PWD);

        transport.sendMessage(message,
            message.getRecipients(Message.RecipientType.TO));
        transport.close();
    }
}