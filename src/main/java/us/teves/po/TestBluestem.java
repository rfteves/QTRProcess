/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.teves.po;

import com.sun.mail.gimap.GmailFolder;
import com.sun.mail.gimap.GmailSSLStore;
import com.sun.mail.imap.IMAPSSLStore;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import us.teves.campaign.util.Utility;

/**
 *
 * @author rfteves
 */
public class TestBluestem {
    public static void main(String[] args) throws Exception {

        //System.getProperties().setProperty("mail.smtp.host", "smtp.office365.com");
        System.getProperties().setProperty("mail.imap.host", "outlook.office365.com");
        //System.getProperties().setProperty("mail.username", "");
        //System.getProperties().setProperty("mail.password", "");
        //System.getProperties().setProperty("mail.smtp.port", "587");
        System.getProperties().setProperty("mail.imap.port", "993");
        System.getProperties().put("mail.imap.auth", "true");
        System.getProperties().put("mail.imap.starttls.enable", "true");
        System.getProperties().put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        System.getProperties().put("mail.imap.ssl.enable", "true");
        String folderName = "Inbox";
        Message[] foundMessages = null;
        String gmailServer = "outlook.office365.com";
        Properties props = System.getProperties();
        Session mailSession = null;
        GmailSSLStore store = null;
        IMAPSSLStore ddd = null;
        props.put("mail.store.protocol", "gimaps");
        props.setProperty("mail.debug", "true");
        mailSession = Session.getDefaultInstance(props);
        store = (GmailSSLStore) mailSession.getStore("gimaps");
        store.connect(gmailServer, "reports@bluestem.com", "Mko0Nji9");
        GmailFolder folder = (GmailFolder) store.getFolder(folderName);
        folder.open(Folder.READ_ONLY);
        System.out.println("count: " + folder.getMessageCount());
        System.out.println("count: " + folder.getMessageCount());
        System.out.println("count: " + folder.getMessageCount());
        for (Message message: folder.getMessages()) {
            System.out.println(message.getContent());
            if (true)break;
        }
    }
}
