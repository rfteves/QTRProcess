/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.po;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author rfteves
 */
public class SumPurchases {

    private static boolean first = true;
    private static boolean firstHit = false;
    public static int COUNTER;
    public static int SKIPPER;
    public static int MARKER;
    //public static boolean DEBUG = true;
    public static boolean DEBUG = false;
    public static Monitor MONITOR;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        new Thread() {

            @Override
            public void run() {
                MONITOR = new Monitor();
                MONITOR.setVisible(true);
            }
        }.start();
        Thread.currentThread().sleep(5000);
        EmailOrder[] orders = new EmailOrder[1];
        for (int i = 0; i < orders.length; i++) {
            orders[i] = new EmailOrder();
        }
        orders[0].setIndex(2624);
        orders[0].setUsername("march2012@teves.us");
        orders[0].setFullname("Dear Editha Teves");
        /*orders[1].setIndex(0);
        orders[1].setUsername("march2012@teves.us");
        orders[1].setFullname("Dear Ricardo Teves");
        orders[2].setIndex(0);
        orders[2].setUsername("march2012@teves.us");
        orders[2].setFullname("Dear Renz Teves");
        orders[3].setIndex(0);
        orders[3].setUsername("march2012@teves.us");
        orders[3].setFullname("Dear Riz Teves");*/
        /*orders[2].setIndex(0);
        orders[2].setUsername("purser@teves.us");
        orders[3].setIndex(0);
        orders[3].setUsername("riz.costco@teves.us");
        orders[4].setIndex(0);
        orders[4].setUsername("edit.costco@teves.us");
        orders[5].setIndex(0);
        orders[5].setUsername("costco2012@teves.us");
        orders[6].setIndex(0);
        orders[6].setUsername("renz2012@teves.us");
        orders[7].setIndex(0);
        orders[7].setUsername("mama2012@teves.us");
        orders[8].setIndex(0);
        orders[8].setUsername("riz2012@teves.us");
        orders[9].setIndex(0);
        orders[9].setUsername("lola2012@teves.us");
        orders[10].setIndex(0);
        orders[10].setUsername("costco2012a@teves.us");
        orders[11].setIndex(0);
        orders[11].setUsername("march2012@teves.us");*/


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
        String basedate = "20120318";
        String uptoDate = "20120824";
        for (EmailOrder order : orders) {
            if (false && ordinal != 3) {
                ordinal++;
                continue;
            } else {
                COUNTER = 0;
                SKIPPER = 0;
                MARKER = 0;
                SumPurchases.doit(order, basedate, uptoDate, order.getIndex(), ordinal++);
                System.out.println("COUNTER: " + COUNTER);
                System.out.println("SKIPPER: " + SKIPPER);
                System.out.println(" MARKER: " + MARKER);
            }
        }
    }

    public static void doit(EmailOrder order, String basedate, String uptoDate, int i, int ordinal) throws Exception {
        // TODO code application logic here
        String username = order.getUsername();
        MONITOR.getUser().setText(username);
        MONITOR.getSubject().setText(username);
        MONITOR.getTotal().setText(username);
        if (order.getFullname() != null) {
            MONITOR.getUser().setText(order.getFullname());
        }
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
        System.out.println("\n>" + username + ": " + messages.length);
        //for (Message message: messages) {
        FileOutputStream fos = new FileOutputStream("C:/Users/rfteves/Documents/Quicken/merch.qif", !first);
        if (first) {
            fos.write("!Type:Bill\n".getBytes());
            first = false;
        }
        BigDecimal totalNet = BigDecimal.ZERO;
        int min = i;
        i = messages.length - 1;
        for (; i > min; i--) {
            String subject = null;
            try {
                subject = messages[i].getSubject();
            } catch (Exception eee){}
            System.out.println(totalNet);
            //System.out.println(i + " of " + messages.length + " " + messages[i].getReceivedDate() + " " + totalNet + " " + subject);
            BigDecimal net = SumPurchases.getNet(order, messages[i], basedate, uptoDate);
            if (net != null) {
                if (net.doubleValue() == -1) {
                    break;
                } else if (net.doubleValue() > 0) {
                    totalNet = totalNet.add(net);
                    MONITOR.getTotal().setText("" + totalNet);
                }
            }
        }
        if (order.getFullname() != null) {
            username = order.getFullname();
        }
        System.out.println(username + ">>>>>");
        System.out.println(username + " TOTAL: " + totalNet);
        fos.close();

// Close connection
        folder.close(false);
        store.close();
    }

    private static BigDecimal getNet(EmailOrder order, Message message, String startDate, String endDate) throws Exception {
        String subject = null;
        try {
            subject = message.getSubject();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (subject == null) {
            return null;
        }
        MONITOR.getSubject().setText(subject);
        ++MARKER;
        SimpleDateFormat md = new SimpleDateFormat("yyyyMMdd");
        String transdate = md.format(message.getReceivedDate());
        if (transdate.compareTo(endDate) > 0) {
            return BigDecimal.ONE.negate();
        } else if (transdate.compareTo(startDate) < 0) {
            ++SKIPPER;
            return BigDecimal.ONE.add(BigDecimal.ONE).negate();
        } else if (subject.startsWith("Your Costco.com Order Was Received") == false) {
            return BigDecimal.ZERO;
        }
        MimeMultipart mp = (MimeMultipart) message.getContent();
        String body = (String) mp.getBodyPart(0).getContent();
        if (body.indexOf(order.getFullname()) == -1) {
            return BigDecimal.ZERO;
        }
        ++COUNTER;
        MONITOR.getSubject().setText(subject + " " + transdate);
        System.out.println("\t" + subject + " " + transdate);
        BigDecimal value = BigDecimal.ZERO;
        if (subject.toLowerCase().indexOf("Your Costco.com Order Was Received.".toLowerCase()) != -1) {
            PO po = new CostcoPO();
            po.setVendor("costco");
            po.extract(message);
            value = po.getSubtotal();
            System.out.print(".");
            System.out.flush();
        }
        return value;
    }
}
