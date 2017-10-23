/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.po;

import java.io.FileOutputStream;
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
public class Main {

    private static Set<String> TICKETS = new HashSet<String>();
    private static boolean searchTransnum = false;
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
        EmailOrder[]orders = new EmailOrder[21];
        for (int i = 0; i < orders.length; i++) {
            orders[i] = new EmailOrder();
        }
        orders[0].setIndex(1110);
        orders[0].setUsername("admin@teves.us");
        orders[1].setIndex(2550);
        orders[1].setUsername("costco@teves.us");
        orders[2].setIndex(3240);
        orders[2].setUsername("renz.costco@teves.us");
        orders[3].setIndex(820);
        orders[3].setUsername("purser@teves.us");
        orders[4].setIndex(1870);
        orders[4].setUsername("riz.costco@teves.us");
        orders[5].setIndex(2420);
        orders[5].setUsername("edit.costco@teves.us");
        orders[6].setIndex(1920);
        orders[6].setUsername("samsclub@teves.us");
        orders[7].setIndex(390);
        orders[7].setUsername("walmart@teves.us");
        orders[8].setIndex(5920);
        orders[8].setUsername("keurig@teves.us");

        orders[9].setIndex(270);
        orders[9].setUsername("costco2012@teves.us");
        orders[10].setIndex(0);
        orders[10].setUsername("renz2012@teves.us");
        orders[11].setIndex(1320);
        orders[11].setUsername("mama2012@teves.us");
        orders[12].setIndex(1260);
        orders[12].setUsername("riz2012@teves.us");
        orders[13].setIndex(0);
        orders[13].setUsername("lola2012@teves.us");
        orders[14].setIndex(327);
        orders[14].setUsername("walm2012@teves.us");
        orders[15].setIndex(4420);
        orders[15].setUsername("bjs2012@teves.us");
        orders[16].setIndex(15996);
        orders[16].setUsername("sams2012@teves.us");
        orders[17].setIndex(510);
        orders[17].setUsername("costco2012a@teves.us");
        orders[18].setIndex(14060);
        orders[18].setUsername("march2012@teves.us");
        orders[19].setIndex(115);
        orders[19].setUsername("gmc2012@teves.us");
        orders[20].setIndex(253);
        orders[20].setUsername("uline@teves.us");


        /*
        orders[9].setIndex(0);
        orders[9].setUsername("renz2012@teves.us");
        orders[10].setIndex(0);
        orders[10].setUsername("mama2012@teves.us");
        orders[11].setIndex(0);
        orders[11].setUsername("riz2012@teves.us");
        orders[12].setIndex(0);
        orders[12].setUsername("lola2012@teves.us");
        orders[13].setIndex(0);
        orders[13].setUsername("sams2012@teves.us");
        orders[14].setIndex(0);
        orders[14].setUsername("walmart@teves.us");
        orders[15].setIndex(0);
        orders[15].setUsername("bjs2012@teves.us");*/

        int ordinal = 0;
        //String basedate = "20130216";
        //String uptoDate = "20130228";
        String basedate = "20130601";
        String uptoDate = "20130615";
        for (EmailOrder order: orders) {
            if (true && ordinal != 15) {
               ordinal++;
                continue;
            } else {
                COUNTER = 0;
                SKIPPER = 0;
                MARKER = 0;
                Main.doit(order.getUsername(), basedate, uptoDate, order.getIndex(), ordinal++);
                System.out.println("COUNTER: " + COUNTER);
                System.out.println("SKIPPER: " + SKIPPER);
                System.out.println(" MARKER: " + MARKER);
            }
        }
    }

    public static void doit(String username, String basedate, String uptoDate, int i, int ordinal) throws Exception {
        // TODO code application logic here
        String host = "204.232.200.80";
        String password = "kop0io9";

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
        System.out.println("\n>" + username + ": " + messages.length);
        //for (Message message: messages) {
        FileOutputStream fos = new FileOutputStream("C:/Users/rfteves/Documents/Quicken/merch.qif", !first);
        if (first) {
            fos.write("!Type:Bill\n".getBytes());
            fos.write("!Type:Tax\n".getBytes());
            first = false;
        }

        for (; i < messages.length; i++) {
            String quicken = Main.getQuickenInfo(messages[i], basedate, uptoDate);
            if (quicken != null) {
                if (quicken.equals("more than")) {
                    break;
                } else {
                fos.write(quicken.getBytes());
                //fos.write("^\n".getBytes());
                }
            }
        }
        fos.close();

// Close connection
        folder.close(false);
        store.close();
    }

    private static String getQuickenInfo(Message message, String startDate, String endDate) throws Exception {
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
        if (transdate.compareTo(endDate) > 0) {
            System.out.print(")");
            return "more than";
        } else if (transdate.compareTo(startDate) < 0) {
            System.out.print("{");
            ++SKIPPER;
            return null;
        }
        if (false && subject.indexOf("51952585") == -1) {
            System.out.print("1<");
            return null;
        }
        ++COUNTER;
        System.out.println("\t" + subject + " " + transdate);
        PO po = null;
        if (subject.toLowerCase().indexOf("Your Costco.com Order Was Received.".toLowerCase()) != -1) {
            po = new CostcoPO();
            po.setVendor("costco");
            po.extract(message);
        } else if (subject.toLowerCase().indexOf("\"BJ's Wholesale Club\" Order".toLowerCase()) != -1 &&
                subject.indexOf("8978162") == -1) {
            po = new BjsPO();
            po.setVendor("bjs");
            po.extract(message);
        } else if (/*subject.toLowerCase().startsWith("Your Order Confirmation".toLowerCase()) ||*/
                (subject.toLowerCase().equalsIgnoreCase("Your SamsClub.com order") ||
                subject.equalsIgnoreCase("SamsClub.com order confirmation")) &&
                subject.toLowerCase().indexOf("canceled") == -1) {
            po = new SamsClub(message.getReceivedDate());
            po.setSubject(subject.toLowerCase());
            po.setVendor("samsclub");
            po.extract(message);
        } else if (subject.toLowerCase().indexOf("Thanks for your Walmart".toLowerCase()) != -1) {

            MimeMultipart multipart = (MimeMultipart)message.getContent();
            BodyPart bodypart = multipart.getBodyPart(0);
            String body = (String)bodypart.getContent();
            po = new Walmart(body.toLowerCase().indexOf("walmart pick up today") != -1);
            po.setVendor("walmart");
            po.extract(message);
        } else if (subject.toLowerCase().startsWith("Your Keurig Shipping".toLowerCase())) {
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
        if (po == null ) {
            return null;
        } else {
            return po.quickenString();
        }
    }


    public static boolean isProcessed(String ticket) {
        boolean value = true;
        if (Main.TICKETS.contains(ticket) == false) {
            value = false;
            Main.TICKETS.add(ticket);
        }
        return value;
    }
}
