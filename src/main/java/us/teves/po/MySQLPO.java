/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.po;

import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author rfteves
 */
public class MySQLPO {

    private static boolean first = true;
    public static int COUNTER;
    public static int SKIPPER;
    public static int MARKER;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        EmailOrder[] orders = new EmailOrder[21];
        for (int i = 0; i < orders.length; i++) {
            orders[i] = new EmailOrder();
        }
        orders[0].setIndex(1100);
        orders[0].setUsername("admin@teves.us");
        orders[1].setIndex(2540);
        orders[1].setUsername("costco@teves.us");
        orders[2].setIndex(3230);
        orders[2].setUsername("renz.costco@teves.us");
        orders[3].setIndex(760);
        orders[3].setUsername("purser@teves.us");
        orders[4].setIndex(1860);
        orders[4].setUsername("riz.costco@teves.us");
        orders[5].setIndex(2420);
        orders[5].setUsername("edit.costco@teves.us");
        orders[6].setIndex(19000);
        orders[6].setUsername("samsclub@teves.us");
        orders[7].setIndex(380);
        orders[7].setUsername("walmart@teves.us");
        orders[8].setIndex(1750);
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
        orders[14].setIndex(150);
        orders[14].setUsername("walm2012@teves.us");
        orders[15].setIndex(960);
        orders[15].setUsername("bjs2012@teves.us");
        orders[16].setIndex(2440);
        orders[16].setUsername("sams2012@teves.us");
        orders[17].setIndex(390);
        orders[17].setUsername("costco2012a@teves.us");
        orders[18].setIndex(2750);
        orders[18].setUsername("march2012@teves.us");
        orders[19].setIndex(20);
        orders[19].setUsername("gmc2012@teves.us");
        orders[20].setIndex(0);
        orders[20].setUsername("uline@teves.us");

        int ordinal = 0;
        String basedate = "20120101";
        String uptoDate = "20120728";
        for (EmailOrder order : orders) {
            if (true && ordinal != 20) {
                ordinal++;
                continue;
            } else {
                COUNTER = 0;
                SKIPPER = 0;
                MARKER = 0;
                MySQLPO.doit(order.getUsername(), basedate, uptoDate, order.getIndex(), ordinal++);
                System.out.println("COUNTER: " + COUNTER);
                System.out.println("SKIPPER: " + SKIPPER);
                System.out.println(" MARKER: " + MARKER);
            }
        }

    }

    public static void doit(String username, String basedate, String uptoDate, int i, int ordinal) throws Exception {
        // TODO code application logic here
        String host = "teves.us";
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
        System.out.println(username + ": " + messages.length);
        //for (Message message: messages) {
        FileOutputStream fos = new FileOutputStream("c:/Users/rfteves/Documents/Quicken/mysqlpo20120630.sql", !first);
        if (first) {
            first = false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar beginDate = Calendar.getInstance();
        beginDate.setTime(sdf.parse(basedate));
        MARKER = i;
        for (; i < messages.length; i++) {
            if (messages[i].getFlags().contains(Flag.DELETED) == false) {
                String quicken = MySQLPO.getMySQLInfo(messages[i], basedate, uptoDate);
                if (quicken != null) {
                    if (quicken.equals("}")) {
                        break;
                    } else if (quicken.equals("{")) {
                        continue;
                    } else {
                        fos.write(quicken.getBytes());
                        fos.write("\n".getBytes());
                    }
                }
            } else {
                MARKER++;
                SKIPPER++;
                COUNTER++;
            }
        }
        fos.close();

// Close connection
        folder.close(false);
        store.close();
    }

    private static String getMySQLInfo(Message message, String startDate, String endDate) throws Exception {
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
            return "}";
        } else if (transdate.compareTo(startDate) < 0) {
            System.out.print("{");
            ++SKIPPER;
            return null;
        }
        ++COUNTER;
        //System.out.println("\t" + subject + " " + transdate);
        System.out.print(".");
        PO po = null;
        if (subject.toLowerCase().indexOf("Your Costco.com Order Was Received.".toLowerCase()) != -1) {
            po = new CostcoPO();
            po.setVendor("costco");
            po.extract(message);
        } else if (subject.toLowerCase().indexOf("\"BJ's Wholesale Club\" Order".toLowerCase()) != -1) {
            po = new BjsPO();
            po.setVendor("bjs");
            po.extract(message);
        } else if (subject.toLowerCase().startsWith("Your Order Confirmation".toLowerCase()) ||
                subject.toLowerCase().startsWith("Your SamsClub.com order".toLowerCase()) &&
                subject.toLowerCase().indexOf("canceled") == -1) {
            po = new SamsClub(message.getReceivedDate());
            po.setSubject(subject.toLowerCase());
            po.setVendor("samsclub");
            po.extract(message);
            if (po.isNullifyPO()) {
                po = null;
            }
        } else if (subject.toLowerCase().indexOf("Thanks for your Walmart".toLowerCase()) != -1) {

            MimeMultipart multipart = (MimeMultipart) message.getContent();
            BodyPart bodypart = multipart.getBodyPart(0);
            String body = (String) bodypart.getContent();
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
        if (po == null) {
            return null;
        } else {
            return po.mysqlString();
        }
    }

    public static String encode(String sql) {
        return sql.replaceAll("[^$\\.#a-zA-Z0-9\\- ]", "").trim();
    }
}
