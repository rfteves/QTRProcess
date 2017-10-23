/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.po;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import us.teves.campaign.util.Utility;

/**
 *
 * @author rfteves
 */
public class MainGmailX {

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

        System.getProperties().setProperty("mail.smtp.host", Utility.getApplicationProperty("mail.smtp.host"));
        System.getProperties().setProperty("mail.username", Utility.getApplicationProperty("mail.username"));
        System.getProperties().setProperty("mail.password", Utility.getApplicationProperty("mail.password"));
        System.getProperties().setProperty("mail.smtp.port", Utility.getApplicationProperty("mail.smtp.port"));
        System.getProperties().put("mail.smtp.auth", "true");
        System.getProperties().put("mail.smtp.starttls.enable", "true");
        MainGmailX.doit("Keurig/All", "after:2017-07-01 before:2017-10-01 from:order_confirmation@keurig.com");
        //MainGmailX.doit("Costco/All", "after:2017-09-01 before:2017-10-01 from:orderstatus@costco.com ");
        //MainGmailX.doit("SamsClub/Orders", "subject:\"order confirmation\" after:2017-07-01 before:2017-10-01");
    }
    private static BigDecimal total = BigDecimal.ZERO;
    public static final String IMAP_HOST_NAME = Utility.getApplicationProperty("imap.host.name");
    public static final String IMAP_AUTH_USER = Utility.getApplicationProperty("imap.user.name");
    public static final String IMAP_AUTH_PWD = Utility.getApplicationProperty("imap.user.password");

    public static void doit(String folderName, String searchTerm) throws Exception {
        Message messages[] = Utility.getMessages(folderName, searchTerm);
        System.out.println("count: " + messages.length);
        if (messages.length > 3500) {
            System.exit(-messages.length);
        }
        if (true) {
            MessageConsumer consumer = MessageConsumer.getInstance(messages.length);
            int index = 0;
            for (Message message : messages) {
                MessageOrder order = MessageOrder.getInstance(consumer, message, ++index);
                consumer.add(order);
            }
            consumer.quit();
            consumer.join();
            System.exit(0);
        } else {
            FileOutputStream fos = new FileOutputStream("./invoices.qif");
            int order = 0;
            for (Message message : messages) {
                String quicken = MainGmailX.getQuickenInfo(message, messages.length, ++order);
                if (quicken != null) {
                    if (quicken.equals("more than")) {
                        break;
                    } else {
                        if (keys.contains(key) == false) {
                            keys.add(key);
                            fos.write(quicken.getBytes());
                            fos.flush();
                        }
                    }
                }
            }
            fos.close();
        }
    }
    static String key;
    static Set<String> keys = new HashSet<String>();
    static int limit = 0;
    static boolean marked = true;

    private static String getQuickenInfo(Message message, int orders, int order) throws Exception {
        String subject = null;
        try {
            subject = message.getSubject();
        } catch (Exception e) {
            System.out.println(message.getMessageNumber() + " no subject");
        }
        if (subject == null) {
            return null;
        }
        PO po = null;
        System.out.print(orders + ":" + order + ":" + subject);
        if (subject.toLowerCase().startsWith("your costco.com order was received.".toLowerCase())) {
            po = new CostcoPO();
            po.setVendor("costco");
            po.extract(message);
            if (po != null && po.getTotal() != null) {
                total = total.add(po.getTotal().subtract(po.getTax()));
            }
        } else if (subject.toLowerCase().indexOf("Confirmation".toLowerCase()) != -1 &&
                message.getFrom()[0].toString().endsWith("bjs.com")) {
            po = new BjsPO();
            po.setVendor("bjs");
            po.extract(message);
        } else if ((subject.toLowerCase().equalsIgnoreCase("Your SamsClub.com order") ||
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
        } else if (message.getFrom()[0].toString().toLowerCase().indexOf("@keurig.com") != -1 && subject.toLowerCase().indexOf("shipped") == -1) {
            po = new KeurigPO();
            if (subject.toLowerCase().indexOf("shipped") != -1) {
                //po.setVendor("keurigshipping");
                po = null;
            } else {//if (subject.toLowerCase().indexOf("got your order") != -1 || subject.toLowerCase().indexOf("order confirmation") != -1) {
                po.setVendor("keurigorder");
            }
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
            return po.excelDelimitedFormat();
        }
    }

    public static boolean isProcessed(String ticket) {
        boolean value = true;
        if (MainGmailX.TICKETS.contains(ticket) == false) {
            value = false;
            MainGmailX.TICKETS.add(ticket);
        }
        return value;
    }
}
