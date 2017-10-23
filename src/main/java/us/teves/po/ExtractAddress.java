/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.po;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author rfteves
 */
public class ExtractAddress {

    private static Set<String> TICKETS = new HashSet<String>();
    private static boolean first = true;
    public static int COUNTER;
    public static int SKIPPER;
    public static int MARKER;
    //public static boolean DEBUG = true;
    public static boolean DEBUG = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Needs folder name. Try again.");
            System.exit(0);
        } else {
            System.out.println("Folder name is " + args[0]);
        }
        EmailOrder[] orders = new EmailOrder[1];
        for (int i = 0; i < orders.length; i++) {
            orders[i] = new EmailOrder();
        }
        orders[0].setIndex(0);
        orders[0].setUsername(args[0]);
        //orders[1].setIndex(0);
        //orders[1].setUsername("Costco");
        //orders[2].setIndex(0);
        //orders[2].setUsername("Keurig");
        //orders[3].setIndex(0);
        //orders[3].setUsername("Samsclub");
        //orders[4].setIndex(0);
        //orders[4].setUsername("Uline");
        //orders[5].setIndex(0);
        //orders[5].setUsername("Walmart");

        int ordinal = 0;
        //String basedate = "20130216";
        //String uptoDate = "20130228";
        String basedate = "20140501";
        String uptoDate = "20140531";
        for (EmailOrder order : orders) {
            if (false && ordinal != 1) {
                ordinal++;
                continue;
            } else {
                COUNTER = 0;
                SKIPPER = 0;
                MARKER = 0;
                ExtractAddress.doit(true, order.getUsername(), basedate, uptoDate, order.getIndex(), ordinal++);
                System.out.println("COUNTER: " + COUNTER);
                System.out.println("SKIPPER: " + SKIPPER);
                System.out.println(" MARKER: " + MARKER);
            }
        }
    }
    private static BigDecimal total = BigDecimal.ZERO;
    public static final String IMAP_HOST_NAME = "";
    public static final String IMAP_AUTH_USER = "";
    public static final String IMAP_AUTH_PWD = "";

    public static void doit(boolean gmail, String foldername, String basedate, String uptoDate, int i, int ordinal) throws Exception {
        // TODO code application logic here
        Properties props = new Properties();
        Session mailSession = null;
        Store store = null;
        if (gmail) {
            props.put("mail.store.protocol", "imaps");
            mailSession = Session.getDefaultInstance(props);
            store = mailSession.getStore("imaps");
        } else {
            mailSession = Session.getDefaultInstance(props, null);
            store = mailSession.getStore("imap");
        }


        //mailSession.setDebug(true);

        if (gmail) {
            store.connect(ExtractAddress.IMAP_HOST_NAME,
                    ExtractAddress.IMAP_AUTH_USER,
                    ExtractAddress.IMAP_AUTH_PWD);
        } else {
            store.connect("204.232.200.80", "bjs2012@teves.us", "kop0io9");
        }
        Folder folder = store.getFolder("Keurig/" + foldername);

        //Folder folder = store.getFolder(gmail ? "Accounting" + "" : "INBOX");

        folder.open(Folder.READ_ONLY);

// Get directory
        Message messages[] = folder.getMessages();
        System.out.println("\n>" + foldername + ": " + messages.length);
        //for (Message message: messages) {
        FileOutputStream fos = new FileOutputStream("./" + (gmail ? foldername : "INBOX") + ".sql", !first);
        StringBuilder sql = new StringBuilder("");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        for (; i < messages.length; i++) {
            PO po = ExtractAddress.getPOInfo(messages[i], basedate, uptoDate);
            if (po != null) {
                //System.out.println(po.getCustomerInfo());
                //System.out.println(po.getOrderInfo());
                sql.setLength(0);
                sql.append("0");
                sql.append("\t");
                sql.append(po.getPayee());
                sql.append("\t");
                sql.append(po.getAddress());
                sql.append("\t");
                sql.append(po.getAddress2() == null ? "" : po.getAddress2());
                sql.append("\t");
                sql.append(po.getCity());
                sql.append("\t");
                sql.append(po.getState());
                sql.append("\t");
                sql.append(po.getZip());
                sql.append("\t");
                sql.append(po.getOrderNumber());
                sql.append("\t");
                sql.append(po.getProduct().replaceAll("®", ""));
                sql.append("\t");
                sql.append(sdf.format(po.getTransdate()));
                sql.append("\t");
                sql.append(po.getTotal());
                sql.append("\n");
                fos.write(sql.toString().getBytes());
            }
        }
        fos.close();

// Close connection
        folder.close(false);
        store.close();
    }
    static String key;
    static Set<String> keys = new HashSet<String>();
    static int limit = 0;

    private static PO getPOInfo(Message message, String startDate, String endDate) throws Exception {
        String subject = null;
        try {
            subject = message.getSubject();
            //System.out.println("subject: " + subject);
        } catch (Exception e) {
            System.out.println(message.getMessageNumber() + " no subject");
        }
        if (subject == null) {
            return null;
        }
        ++MARKER;
        SimpleDateFormat md = new SimpleDateFormat("yyyyMMdd");
        String transdate = md.format(message.getReceivedDate());
        //System.out.println("\t" + subject + " >>> " + transdate);
        //System.out.println(message.getReceivedDate());
        if (false && transdate.compareTo(endDate) > 0) {
            System.out.print("endDate." + transdate);
            return null;
        } else if (false && transdate.compareTo(startDate) < 0) {
            System.out.print("startDate." + transdate);
            return null;
        }
        if (false && subject.indexOf("51952585") == -1) {
            //System.out.print("1<");
            //return null;
        }
        ++COUNTER;
        /*if (subject.toLowerCase().indexOf("sams") == -1) {
        return null;
        }
        if (++limit > 20500) {
        return "more than";
        }*/
        System.out.println("\t" + subject + " " + transdate + " " + message.getFrom()[0].toString());
        PO po = null;
        if (subject.toLowerCase().startsWith("your costco.com order was received.".toLowerCase())) {
            po = new CostcoPO();
            po.setVendor("costco");
            po.extract(message);
            if (po != null && po.getTotal() != null) {
                total = total.add(po.getTotal().subtract(po.getTax()));
                System.out.println(transdate + " Total: " + total);
            }
        } else if (subject.toLowerCase().indexOf("Confirmation".toLowerCase()) != -1 &&
                message.getFrom()[0].toString().endsWith("bjs.com")) {
            po = new BjsPO();
            po.setVendor("bjs");
            po.extract(message);
        } else if (/*subject.toLowerCase().startsWith("Your Order Confirmation".toLowerCase()) ||*/(subject.toLowerCase().equalsIgnoreCase("Your SamsClub.com order") ||
                subject.equalsIgnoreCase("SamsClub.com order confirmation")) &&
                subject.toLowerCase().indexOf("canceled") == -1) {
            po = new SamsClub(message.getReceivedDate());
            po.setSubject(subject.toLowerCase());
            po.setVendor("samsclub");
            po.extract(message);
        } else if (subject.toLowerCase().indexOf("Thanks for your Walmart".toLowerCase()) != -1) {

            MimeMultipart multipart = (MimeMultipart) message.getContent();
            BodyPart bodypart = multipart.getBodyPart(0);
            String body = (String) bodypart.getContent();
            po = new Walmart(body.toLowerCase().indexOf("walmart pick up today") != -1);
            po.setVendor("walmart");
            po.extract(message);
        } else if (subject.toLowerCase().startsWith("Your Keurig".toLowerCase())) {
            po = new KeurigPO();
            po.setVendor("keurig");
            po.extract(message);
        } else if (subject.toLowerCase().startsWith("Your order has shipped from Green Mountain Coffee".toLowerCase())) {
            po = new GreenMountain();
            po.setVendor("keurig");
            po.extract(message);
        } else if (subject.toLowerCase().indexOf("ULINE Confirmation - Order#".toLowerCase()) != -1) {
            po = new ULine();
            po.setVendor("ULine");
            po.extract(message);
        }
        if (po == null) {
            return null;
        } else {
            key = po.orderNumber + po.getVendor();
            //return po.quickenString();
            return po;
        }
    }

    public static boolean isProcessed(String ticket) {
        boolean value = true;
        if (ExtractAddress.TICKETS.contains(ticket) == false) {
            value = false;
            ExtractAddress.TICKETS.add(ticket);
        }
        return value;
    }
}
