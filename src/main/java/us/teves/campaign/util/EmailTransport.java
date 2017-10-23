/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.campaign.util;

/**
 *
 * @author ricardo
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.apache.log4j.Logger;

/**
 *
 * @author RICARDO
 */
public class EmailTransport implements Serializable {

    private static Logger logger = Logger.getLogger(EmailTransport.class);
    protected String to, from, subject, textmessage, cc, bcc;
    public String[] replyto;
    protected Session session;
    protected Store store;
    protected Properties properties = System.getProperties();
    protected String host, protocol;
    Authenticator auth;

    public static void main(String[] s) throws Exception {
        /*System.getProperties().put("mail.smtp.auth", "true");
        System.getProperties().put("mail.smtp.starttls.enable", "true");
        System.getProperties().put("mail.smtp.host", "smtp.gmail.com");
        System.getProperties().put("mail.smtp.port", "587");

        System.getProperties().put("mail.username", "");
        Sytem.getProperties().put("mail.password", "");*/
        /*System.getProperties().put("mail.username", "");
        System.getProperties().put("mail.password", "");
        System.getProperties().put("mail.smtp.STARTTLS.enable", "true");

        System.getProperties().put("mail.transport.protocol", "smtps");
        System.getProperties().put("mail.smtps.**ssl.enable", "false");
        System.getProperties().put("mail.smtps.**ssl.required", "false");*/


        
        System.getProperties().setProperty("mail.smtp.host", Utility.getApplicationProperty("mail.smtp.host"));
        System.getProperties().setProperty("mail.username", Utility.getApplicationProperty("mail.username"));
        System.getProperties().setProperty("mail.password", Utility.getApplicationProperty("mail.password"));
        System.getProperties().setProperty("mail.smtp.port", Utility.getApplicationProperty("mail.smtp.port"));
        System.getProperties().put("mail.smtp.auth", "true");
        System.getProperties().put("mail.smtp.starttls.enable", "true");


        EmailTransport sendEmail = new EmailTransport("ricardo@drapers.com", "ricardo@drapers.com", new String[]{"ricardo@drapers.com"}, "subject test", "message test");
        sendEmail.send();
    }

    public EmailTransport() {
        logger.info(String.format("Mail Server: %s", properties.getProperty("mail.smtp.host")));
        try {
            this.checkAuth();
            session = Session.getInstance(properties, auth);
        } catch (Exception e) {
            logger.error("Session error", e);
        }
    }

    private void checkAuth() {
        final StringBuilder authuser = new StringBuilder();
        final StringBuilder authpwd = new StringBuilder();
        try {
            authuser.append(System.getProperty("mail.username"));
            authpwd.append(System.getProperty("mail.password"));
            auth = new javax.mail.Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(authuser.toString(), authpwd.toString());
                }
            };
        } catch (Exception e) {
        }
    }
    /*
     * mail.smtp.host=smtp.bizmail.yahoo.com
    mail.username=support@rebatesfactory.com
    mail.password=zxc321cxzmail.smtp.host

     * */

    public EmailTransport(String server) {
        properties.put("mail.smtp.host", server);
        properties.put("mail.transport.protocol", "smtp");
        logger.info(String.format("Mail Server: %s", properties.getProperty("mail.smtp.host")));
        try {
            this.checkAuth();
            session = Session.getInstance(properties, auth);
        } catch (Exception e) {
            logger.error("Session error", e);
        }
    }

    public EmailTransport(String server, String to, String from, String[] replyto, String subject,
            String textmessage) {
        this(server);
        setTo(to);
        setFrom(from);
        setReplyto(replyto);
        setSubject(subject);
        setTextmessage(textmessage);
    }

    public EmailTransport(String to, String from, String[] replyto, String subject,
            String textmessage) {
        this();
        setTo(to);
        setFrom(from);
        setReplyto(replyto);
        setSubject(subject);
        setTextmessage(textmessage);
    }

    public void setCC(String cc) {
        this.cc = cc;
    }

    public void setBCC(String bcc) {
        this.bcc = bcc;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setReplyto(String[] replyto) {
        this.replyto = replyto;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTextmessage(String textmessage) {
        this.textmessage = textmessage;
    }

    public void send() throws Exception {
        Exception error = null;
        try {
            Multipart multi = new MimeMultipart();
            Message message = new MimeMessage(session);
            BodyPart body = new MimeBodyPart();
            message.setFrom(new InternetAddress(this.from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(this.to));
            InternetAddress[] ia = null;
            if (replyto != null) {
                ia = new InternetAddress[replyto.length];
                for (int i = 0; i < replyto.length; i++) {
                    ia[i] = new InternetAddress(replyto[i]);
                }
            }
            if (this.cc != null) {
                message.setRecipients(Message.RecipientType.CC,
                        InternetAddress.parse(this.cc));
            }
            if (this.bcc != null) {
                message.setRecipients(Message.RecipientType.BCC,
                        InternetAddress.parse(this.bcc));
            }

            message.setReplyTo(ia);
            body.setContent(this.textmessage, "text/html");
            multi.addBodyPart(body);
            message.setSubject(this.subject);
            message.setContent(this.textmessage, "text/html");
            message.setContent(multi);
            message.setSentDate(Calendar.getInstance().getTime());
            Transport.send(message);
            //Transport.send(message);
        } catch (Exception e) {
            error = e;
            logger.error("Session transport error", e);
        } finally {
            to = null;
            from = null;
            subject = null;
            textmessage = null;
            cc = null;
            bcc = null;
            replyto = null;
            if (error != null) {
                throw error;
            }
        }
    }
}
