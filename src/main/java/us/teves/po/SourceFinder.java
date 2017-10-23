/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.po;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

/**
 *
 * @author rfteves
 */
public class SourceFinder {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        String host = "";
        String username = "";
        if (true) {
            username = "";
        }
        String password = "";

// Create empty properties
        Properties props = new Properties();

// Get session
        Session session = Session.getDefaultInstance(props, null);

// Get the store
        Store store = session.getStore("imap");
        store.connect(host, username, password);

// Get folder
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);

// Get directory
        Message messages[] = folder.getMessages();
        System.out.println("messages: " + messages.length);
        //for (Message message: messages) {
        FileOutputStream fos = new FileOutputStream("D:/Users/rfteves/Documents/Quicken/merch.txt");
            for (int i = 0; i < messages.length; i++) {
                String quicken = SourceFinder.getInfo(messages[i], "20110501", "20110503", "\"BJ's Wholesale Club\" Order");
                if (quicken != null) {
                    fos.write(quicken.getBytes());
                    fos.write("\n".getBytes());
                }
            }
        fos.close();

// Close connection
        folder.close(false);
        store.close();
    }

    private static String getInfo(Message message, String start, String end, String key) throws Exception {
        String subject = null;
        try {
            subject = message.getSubject();
        } catch (Exception e) {
            System.out.println(message.getMessageNumber() + " no subject");
        }
        if (subject == null) {
            return null;
        }
        SimpleDateFormat md = new SimpleDateFormat("yyyyMMdd");
        String transdate = md.format(message.getReceivedDate());
        System.out.println(message.getReceivedDate());

        if (transdate.compareTo(end) > 0) {
            System.exit(0);
        } else if (transdate.compareTo(end) < 0) {
            return null;
        }
        System.out.println("\t" + subject);
        PO po = null;
        if (subject.toLowerCase().indexOf("Your Costco.com Order Was Received.".toLowerCase()) != -1) {
            po = new CostcoPO();
            po.extract(message);
        } else if (subject.toLowerCase().indexOf("\"BJ's Wholesale Club\" Order".toLowerCase()) != -1) {
            po = new BjsPO();
            po.extract(message);
        }
        if (po == null) {
            return null;
        } else {
            return po.quickenString();
        }
    }
}
