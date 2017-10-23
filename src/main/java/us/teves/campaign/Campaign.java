/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.campaign;

import com.sun.sql.rowset.CachedRowSetXImpl;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author rfteves
 */
public class Campaign extends javax.mail.Authenticator {

    public static void main(String[] args) throws Exception {
        //new Campaign().retrieveFromGmailFolder();
        new Campaign().retrieveFromSystemFile(args);
    }

    /*private void retrieveFromGmailFolder() throws Exception {
    if (imapSession == null) {
    Properties props = new Properties();
    props.put("mail.store.protocol", "imaps");
    imapSession = Session.getInstance(props);
    }
    Store store = imapSession.getStore("imaps");
    store.connect(MainGmail.IMAP_HOST_NAME,
    MainGmail.IMAP_AUTH_USER,
    MainGmail.IMAP_AUTH_PWD);
    Folder folder = store.getFolder("TEVESUSX");
    //Folder folder = store.getFolder("Paypal/Keurig");
    folder.open(Folder.READ_ONLY);
    Message messages[] = folder.getMessages();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    Set<String> mailed = new HashSet<String>();
    for (Message m : messages) {
    String date = sdf.format(m.getReceivedDate());
    if (date.compareTo("20130101") >= 0 && date.compareTo("20130831") <= 0 && mailed.contains(m.getReplyTo()[0].toString()) == false) {
    //mailed.add(m.getReplyTo()[0].toString());
    System.out.println(m.getReceivedDate() + " " + m.getReplyTo()[0].toString());
    if (m.getReplyTo()[0].toString().endsWith("@ebay.com") || m.getReplyTo()[0].toString().endsWith("@paypal.com")) {
    System.out.println("skipped");
    } else {
    //this.reply(m.getReplyTo());
    }
    }
    //
    }
    }*/
    private void retrieveFromSystemFile(String[] args) throws Exception {
        int sleep = 15000;
        if (smtpSession == null) {
            String file = "file:./application.properties";
            URL url = new URL(file);
            smtpProperties.load(url.openStream());
            smtpSession = Session.getInstance(smtpProperties, this);
            smtpProperties.list(System.out);
            if (smtpProperties.getProperty("mail.interval") != null) {
                try {
                    sleep = Integer.parseInt(smtpProperties.getProperty("mail.interval"));
                } catch (Exception e) {
                }
            }
        }
        String emailFilename = args[0];
        String adsFilename = args[1];
        String adsSubject = args[2];
        BufferedReader f = new BufferedReader(new FileReader(emailFilename));
        String line = null;
        int count = 0;
        while ((line = f.readLine()) != null) {
            String email = line.toLowerCase();
            if (answer.length() == 0) {
                answer.append(Campaign.getBody(adsFilename)); //20130831
            }
            if (email.endsWith("@ebay.com") || email.endsWith("@paypal.com")) {
                System.out.println("ebay/paypal skipped: " + email);
            } else if (this.isSubscribed(email).equalsIgnoreCase("ok")) {
                System.out.println(++count + " - " + email);
                String bodyMessage = answer.toString().replaceAll("unsubscribe.url", this.smtpProperties.getProperty("unsubscribe.url")).replaceAll("email_to", email);
                this.reply(email, bodyMessage, adsSubject);
                Thread.sleep(sleep);
            } else {
                System.out.println(this.isSubscribed(email) + " - " + email);
            }
        }
    }
    Session smtpSession;
    Session imapSession;
    Properties smtpProperties = new Properties();

    private void reply(String emailTo, String message, String adsSubject) throws Exception {
        Message mess = new MimeMessage(smtpSession);
        MimeMultipart mainPart = new MimeMultipart();
        mess.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(emailTo));
        mess.setReplyTo(InternetAddress.parse(smtpProperties.getProperty("mail.username")));
        mess.setFrom(InternetAddress.parse(smtpProperties.getProperty("mail.username"))[0]);
        //mess.setSubject("Thank you! RE: " + message.getSubject());
        mess.setSubject(adsSubject);
        MimeBodyPart mainBody = new MimeBodyPart();
        mainBody.setContent(message, "text/html");
        mainPart.addBodyPart(mainBody);
        mess.setContent(mainPart);
        System.out.println("\tSending to " + emailTo);
        Transport.send(mess);
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(smtpProperties.getProperty("mail.username"), smtpProperties.getProperty("mail.password"));
    }
    StringBuilder answer = new StringBuilder();

    private static String getBody(String filename) throws FileNotFoundException, IOException {
        String body = "";
        FileReader reader = new FileReader(filename);
        BufferedReader bf = new BufferedReader(reader);
        String line = null;
        while ((line = bf.readLine()) != null) {
            body += line;
        }
        return body;
    }
    Calendar transdate = Calendar.getInstance();

    private String isSubscribed(String email) {
        String emailOk = "ok";
        try {
            CachedRowSetXImpl classes = new CachedRowSetXImpl();
            classes.setUrl("jdbc:mysql://teves.us/ebay");
            classes.setPassword("eMaiL");
            classes.setUsername("email");
            classes.setCommand("select * from emailcontrol where email = ?");
            classes.setString(1, email);
            classes.execute();
            if (classes.first() == false) {
                classes.moveToInsertRow();
                classes.updateString("email", email);
                classes.updateTimestamp("transdate", new Timestamp(System.currentTimeMillis()));
                classes.updateString("emailstatus", "Y");
                classes.insertRow();
                classes.acceptChanges();
            } else {
                long lastCommunication = classes.getTimestamp("transdate").getTime();
                if (classes.getString("emailstatus").equalsIgnoreCase("Y")) {
                    transdate.setTimeInMillis(lastCommunication);
                    Calendar twoWeeksAgo = Calendar.getInstance();
                    twoWeeksAgo.add(Calendar.DATE, -14);
                    if (lastCommunication > twoWeeksAgo.getTimeInMillis()) {
                        emailOk = " recent " + transdate.getTime();
                    } else {
                        classes.updateTimestamp("transdate", new Timestamp(System.currentTimeMillis()));
                        classes.updateRow();
                        classes.acceptChanges();
                    }
                } else {
                    emailOk = "opt out " + transdate.getTime();
                }
            }
            classes.close();
        } catch (SQLException ex) {
            Logger.getLogger(Campaign.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return emailOk;
        }
    }

    static {
        try {
            java.lang.Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
        }
    }
}
