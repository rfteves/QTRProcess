/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.teves.po;

/**
 *
 * @author rfteves
 */
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;

public class RecvGmail {

    private static final String IMAP_HOST_NAME = "imap.gmail.com";
    private static final String IMAP_AUTH_USER = "ricardo@teves.us";
    private static final String IMAP_AUTH_PWD  = "Kop0Io98";

    public static void main(String[] args) throws Exception{
       new RecvGmail().test();
    }

    public void test() throws Exception{
        Properties props = new Properties();
        SimpleDateFormat format = new SimpleDateFormat("yyyyyMMdd");
        ReceivedDateTerm dateTerm = new ReceivedDateTerm(ComparisonTerm.EQ, format.parse("20130601"));

        props.put("mail.store.protocol", "imaps");

        Session mailSession = Session.getDefaultInstance(props);
        //mailSession.setDebug(true);
        Store store = mailSession.getStore("imaps");
        store.connect(this.IMAP_HOST_NAME,this.IMAP_AUTH_USER, this.IMAP_AUTH_PWD);
        Folder folder = store.getFolder("Accounting/Costco");
        folder.open(Folder.READ_ONLY);

// Get directory
        Message messages[] = folder.getMessages();
        for (int i = 0; i < messages.length; i++) {
            System.out.println(messages[i].getReceivedDate() +  ":" + messages[i].getSubject().substring(0, 25));
        }
    }
}