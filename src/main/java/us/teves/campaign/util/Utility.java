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
import com.sun.mail.gimap.GmailFolder;
import com.sun.mail.gimap.GmailRawSearchTerm;
import com.sun.mail.gimap.GmailSSLStore;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import org.apache.log4j.Logger;

/**
 *
 * @author ricardo
 */
public class Utility {

    private static Logger logger = Logger.getLogger(Utility.class.getName());

    public static void assignValue(Properties props, String key) {
        String value = Utility.getApplicationProperty(key);
        if (value != null) {
            props.setProperty(key, value);
        }
    }

    public static String getApplicationProperty(String name) {
        String userDirectory = null;
        System.getProperties().list(System.out);
        userDirectory = System.getProperties().getProperty("user.dir");
        String companyPropertiesUrl = "".concat(userDirectory.concat(
                "/application.properties"));
        System.out.println("properties file: " + companyPropertiesUrl);
        Properties props = new Properties();
        String value = null;
        try {
            props.load(new FileReader(companyPropertiesUrl));
            props.list(System.out);
            value = props.getProperty(name);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return value;
        }
    }

    public static void main(String[] s) {
        //"<!--${ctx.order.deliveryAddress.title} --> LAWRENCE H SANFORD";
        //String ss = "<!--${ctx.order.deliveryAddress.title} --> LAWRENCE H SANFORD";
        //ss = "<dfd-dfd@ddfd.com>";
        //System.out.println(Utility.cleanName(ss));
        //String ss = "REBATES <keurig2014@teves.us>";
        //System.out.println(ss + ":" + Utility.extractEmail(ss));
    }
    private static Pattern emailPattern = Pattern.compile("<[\\-\\.a-zA-Z0-9@]{1,}>");
    private static Pattern namePattern = Pattern.compile("<[\\-\\.a-zA-Z@]{1,}>");

    public static String extractEmail(Address address) {
        return Utility.extractEmail(address.toString());
    }

    public static String extractEmail(String strAddress) {
        String value = null;
        Matcher matcher = emailPattern.matcher(strAddress);
        if (matcher.find()) {
            value = matcher.group();
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    public static String cleanName(String name) {
        String value = name.replaceAll("<!--[\\w\\W\\.]{1,}-->", "").trim();
        return value;
    }

    public static Message[] getMessages(String folderName, String searchTerm) throws NoSuchProviderException, MessagingException {
        Message[] foundMessages = null;
        String gmailServer = null;
        try {
            gmailServer = Utility.getApplicationProperty("imap.host.name");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Properties props = System.getProperties();
        Session mailSession = null;
        GmailSSLStore store = null;
        props.put("mail.store.protocol", "gimaps");
        //props.setProperty("mail.debug", "true");
        mailSession = Session.getDefaultInstance(props);
        store = (GmailSSLStore) mailSession.getStore("gimaps");
        store.connect(gmailServer, Utility.getApplicationProperty("imap.user.name"), Utility.getApplicationProperty("imap.user.password"));
        GmailFolder folder = (GmailFolder) store.getFolder(folderName);
        folder.open(Folder.READ_ONLY);
        GmailRawSearchTerm searchCondition = new GmailRawSearchTerm(searchTerm);
        foundMessages = folder.search(searchCondition);
        return foundMessages;
    }
}
