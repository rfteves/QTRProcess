/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.test;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

public class CheckingMails {

    public static void check(String host, String storeTypes, String user,
            String password) {
        try {

            // create properties field
            Properties properties = new Properties();

            properties.put("mail.imaps.host", host);
            properties.put("mail.imaps.port", "993");
            properties.put("mail.imaps.starttls.enable", "true");
            properties.put("mail.imap.auth", "true");
            properties.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            // Setup authentication, get session
            Session emailSession = Session.getInstance(properties,
                    new javax.mail.Authenticator() {

                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    "ricardo.teves@bluestem.com", "Bhu8Vgy7");
                        }
                    });
            // emailSession.setDebug(true);

            // create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("imaps");

            store.connect();

            // create the folder object and open it
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_WRITE);

            // retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            System.out.println("messages.length---" + messages.length);

            for (int i = 0, n = messages.length; i < n; i++) {
                Message message = messages[i];
                System.out.println("---------------------------------");
                System.out.println("Email Number " + (i + 1));
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);
                System.out.println("Text: " + message.getContent().toString());
                if (true)break;
            }

            // close the store and folder objects
            emailFolder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String host = "outlook.office365.com";// change accordingly
        String mailStoreType = "imaps";
        String username = "";// change accordingly
        String password = "";// change accordingly

        check(host, mailStoreType, username, password);

    }
}
