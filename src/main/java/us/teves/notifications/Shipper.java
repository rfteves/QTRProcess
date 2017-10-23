/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.notifications;

import com.borland.dx.sql.dataset.Load;
import com.borland.dx.sql.dataset.QueryDescriptor;
import com.cwd.db.ColumnFactory;
import com.cwd.db.DBCPManager;
import com.cwd.db.Data;
import com.sun.mail.gimap.GmailFolder;
import com.sun.mail.gimap.GmailRawSearchTerm;
import com.sun.mail.gimap.GmailSSLStore;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import us.teves.campaign.util.EmailTransport;
import us.teves.campaign.util.Utility;
import us.teves.po.KeurigPO;
import us.teves.po.PO;

/**
 *
 * @author ricardo
 */
public class Shipper extends Thread {

    private static Logger logger = Logger.getLogger(Shipper.class.getName());
    public static boolean DEBUG;

    public static void main(String[] s) {
        System.getProperties().setProperty("mail.smtp.host", Utility.getApplicationProperty("mail.smtp.host"));
        System.getProperties().setProperty("mail.username", Utility.getApplicationProperty("mail.username"));
        System.getProperties().setProperty("mail.password", Utility.getApplicationProperty("mail.password"));
        System.getProperties().setProperty("mail.smtp.port", Utility.getApplicationProperty("mail.smtp.port"));
        System.getProperties().setProperty("max.number.to.process", Utility.getApplicationProperty("max.number.to.process"));
        System.getProperties().put("mail.smtp.auth", "true");
        System.getProperties().put("mail.smtp.starttls.enable", "true");
        Shipper.DEBUG = true;
        new Shipper().start();
    }
    private GmailFolder folder;

    public void run() {
        while (true) {
            try {
                this.initData(false);
                this.processOrders();
                data.closeAll();
                //this.initData(true);
                //this.emailTracking();
            } catch (SQLException sqlex) {
                sqlex.printStackTrace();
            }
            if (true) {
                break;
            }
        }
    }

    private void emailTracking() {
        data.getConfiguration().open();
        if (data.getConfiguration().getRowCount() > 0) {
            data.getConfiguration().getColumn("keurigordernumber").setRowId(true);
            StringBuilder sb = new StringBuilder();
            do {
                sb.setLength(0);
                sb.append("<html><body>");
                sb.append("NAME: " + data.getConfiguration().getString("name"));
                sb.append("<br />");
                sb.append("TRAK: " + data.getConfiguration().getString("trackingnumber"));
                sb.append("<br />");
                sb.append("TYPE: " + data.getConfiguration().getString("viatype"));
                sb.append("<br />");
                sb.append("MRKT: " + data.getConfiguration().getString("marketordernumber"));
                sb.append("<br />");
                sb.append("KORD: " + data.getConfiguration().getString("keurigordernumber"));
                sb.append("<br />");
                sb.append("ACCT: " + data.getConfiguration().getString("account"));
                sb.append("<br />");
                sb.append("</body></html>");
                String subject = "KZ";
                if (data.getConfiguration().getString("marketordernumber").toLowerCase().startsWith("tv")) {
                    subject = "KT";
                } else if (data.getConfiguration().getString("marketordernumber").toLowerCase().startsWith("rb")) {
                    subject = "KR";
                }
                EmailTransport sendEmail = new EmailTransport(System.getProperty("recipients"), "ricardo@teves.us", new String[]{"ricardo@teves.us"},
                        subject + "-SHIPPING " + data.getConfiguration().getString("marketordernumber"), sb.toString());
                try {
                    sendEmail.send();
                } catch (Exception ex) {
                    logger.error("Email error", ex);
                }
            } while (data.getConfiguration().next());
            data.getConfiguration().first();
            data.getTarget().open();
            do {
                data.getTarget().refresh();
                do {
                    logger.info("conf: " + data.getConfiguration().getString("keurigordernumber") +
                            " targ: " + data.getTarget().getString("keurigordernumber"));
                    data.getTarget().setString("notified", "Y");
                } while (data.getTarget().next());
                data.getTarget().saveChanges();
            } while (data.getConfiguration().next());

        }

    }
    SimpleDateFormat mdykk = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    private void processOrders() {

        try {
            Calendar after = Calendar.getInstance();
            after.add(Calendar.DAY_OF_MONTH, -2);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Calendar before = Calendar.getInstance();
            String searchTerm = "after:" + format.format(after.getTime()) +
                    " before:" + format.format(before.getTime());
            Message[] foundMessages = Utility.getMessages("Keurig/All", searchTerm);
            System.out.println("foundMessages" + foundMessages.length);
            int count = foundMessages.length;
            if (count > 0) {
                logger.info("count: " + count);
                data.getSource().open();
                data.getTarget().open();
                data.getAll().open();
                for (Message message : foundMessages) {
                    String subject = message.getSubject();
                    Address[] recipients = message.getRecipients(Message.RecipientType.TO);
                    String account = Utility.extractEmail(recipients[0]);
                    Date receiptDate = message.getReceivedDate();
                    long halfHourAgo = System.currentTimeMillis() - 600000;
                    if (receiptDate.getTime() > halfHourAgo) {
                        continue;
                    }
                    //logger.info(mdykk.format(receiptDate) + " - " + message.getSubject());
                    PO po = null;
                    if (message.getFrom()[0].toString().contains("@keurig.com")) {
                        po = new KeurigPO();
                        if (subject.contains("We got your order")) {
                            po.setVendor("keurigorder");
                            po.extract(message);
                            logger.info("Order processed, order#: " + po.getOrderNumber() + " market: " + po.getMarketOrderNumber());
                            if (Shipper.DEBUG) {
                            } else {
                                data.getParameters().setString("keurigordernumber", po.getOrderNumber().trim());
                                data.getSource().refresh();
                                if (data.getSource().getRowCount() == 0) {
                                    String statename = po.getState();
                                    if (statename.length() != 2) {
                                        data.getParameters().setString("statename", po.getState());
                                        data.getAll().refresh();
                                        statename = data.getAll().getString("abbreviation");
                                    }
                                    data.getSource().insertRow(true);
                                    data.getSource().setString("account", account);
                                    data.getSource().setString("name", Utility.cleanName(po.getPayee()));
                                    data.getSource().setString("address", po.getAddress());
                                    data.getSource().setString("address2", po.getAddress2() == null ? "" : po.getAddress2());
                                    data.getSource().setString("city", po.getCity());
                                    data.getSource().setString("state", statename);
                                    data.getSource().setString("zip", po.getZip());
                                    data.getSource().setString("keurigordernumber", po.getOrderNumber());
                                    data.getSource().setString("marketordernumber", po.getMarketOrderNumber());
                                    data.getSource().setDate("transdate", receiptDate.getTime());
                                    data.getSource().setBigDecimal("amountpaid", po.getTotal());
                                    data.getSource().saveChanges();
                                }
                            }
                        } else if (subject.contains("Your Order has Shipped")) {
                            po.setVendor("keurigshipping");
                            po.extract(message);
                            logger.info("Order shipped, order#: " + po.getOrderNumber() + " tracking#: " + po.getTracking());
                            if (Shipper.DEBUG) {
                            } else {
                                data.getParameters().setString("keurigordernumber", po.getOrderNumber());
                                data.getParameters().setString("trackingnumber", po.getTracking());
                                data.getTarget().refresh();
                                if (data.getTarget().getRowCount() == 0) {
                                    data.getTarget().insertRow(true);
                                    data.getTarget().setString("keurigordernumber", po.getOrderNumber());
                                    data.getTarget().setString("trackingnumber", po.getTracking());
                                    data.getTarget().setString("viatype", po.getViatype());
                                    data.getTarget().setDate("transdate", System.currentTimeMillis());
                                    data.getTarget().saveChanges();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    Data data;

    private void initData(boolean notify) throws SQLException {
        data = Data.getData();
        DataSource source = DBCPManager.getDataSource("jdbc:mysql://teves.us/amazonkeurig",
                "cmZ0ZXZlcw==", "V3J4NjU0eHJ3", null, 4, 4);
        data.getSourcedb().setJdbcConnection(source.getConnection());
        data.getParameters().addColumn(ColumnFactory.createStringColumn("keurigordernumber"));
        data.getParameters().addColumn(ColumnFactory.createStringColumn("statename"));
        data.getParameters().addColumn(ColumnFactory.createStringColumn("trackingnumber"));
        data.getSource().setQuery(new QueryDescriptor(data.getSourcedb(),
                "select * from keurigorders where keurigordernumber = " +
                " :keurigordernumber", data.getParameters(), true, Load.ALL));
        data.getAll().setQuery(new QueryDescriptor(data.getSourcedb(),
                "select * from statenames where statename = :statename", data.getParameters(), true, Load.ALL));
        if (notify) {
            data.getTarget().setQuery(new QueryDescriptor(data.getSourcedb(),
                    "select * from keurigtracking where keurigordernumber = " +
                    " :keurigordernumber", data.getConfiguration(), true, Load.ALL));
            data.getTarget().setTableName("keurigtracking");
        } else {
            data.getTarget().setQuery(new QueryDescriptor(data.getSourcedb(),
                    "select * from keurigtracking where trackingnumber= " +
                    " :trackingnumber", data.getParameters(), true, Load.ALL));
        }
        data.getConfiguration().setQuery(new QueryDescriptor(data.getSourcedb(),
                "select keurigordernumber, name,keurigorders.transdate,trackingnumber,marketordernumber,account,viatype from keurigtracking " +
                "join keurigorders using (keurigordernumber) where notified='N' and " +
                "keurigorders.keurigordernumber!=keurigorders.marketordernumber", data.getParameters(), true, Load.ALL));

    }
}
