/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.teves.po;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.lang3.StringEscapeUtils;


/**
 *
 * @author rfteves
 */
public class ULine extends PO {

    private static Set<String>ordersProcessed = new HashSet<String>();
    Set<String> address = new LinkedHashSet<String>();
    private String body;
    Set<String> orders = new HashSet<String>();

    public ULine() {
        this.shipping = new BigDecimal(0);
    }

    @Override
    public void extract(Message message) {
        try {
            if (message.getContent() instanceof MimeMultipart) {
                MimeMultipart mp = (MimeMultipart) message.getContent();
                body = (String) mp.getBodyPart(0).getContent();
            } else {
                body = (String) message.getContent();
                body = CostcoPO.stripHtml(body);
            }
            if (true) {
                //System.out.println("\n\n\n\n\n\n\n\n\n\n"+body);
                //System.exit(0);
            }
            this.parseOrderNumber();
            this.parseShipTo();
            this.parseOrderTotal();
            this.parseOrderDate();
            this.parseSubTotal();
            this.parseTax();
            this.parseShipping();
            this.parseProduct();
            if (MainGmail.DEBUG) {
                System.out.println("order    #: " + this.getOrderNumber());
                System.out.println("order    #: " + this.getCity());
                System.out.println("state    #: " + this.getState());
                System.out.println("zip      #: " + this.getZip());
                System.out.println("payee    #: " + this.getPayee());
                System.out.println("shipping #: " + this.getShipping());
                System.out.println("subtotal #: " + this.getSubtotal());
                System.out.println("tax      #: " + this.getTax());
                System.out.println("total    #: " + this.getTotal());
                System.out.println("transdate#: " + this.getTransdate());
                System.out.println(body);
                System.exit(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(GreenMountain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static String stripHtml(String body) {
        int lt = 0;
        while ((lt = body.indexOf("<")) != -1) {
            int gt = body.indexOf(">", lt);
            String lined = "";
            if (body.substring(lt).toLowerCase().startsWith("</tr") ||
                    body.substring(lt).toLowerCase().startsWith("<br")) {
                lined = "\n";
            }
            if (lt == 0) {
                body = body.substring(gt + 1);
            } else {
                body = body.substring(0, lt) + lined + body.substring(gt + 1);
            }

        }
        String[] splitlines = StringEscapeUtils.unescapeHtml4(body).split("\n");
        StringBuilder lines = new StringBuilder();
        for (String splitline : splitlines) {
            if (splitline.trim().length() != 0) {
                lines.append(splitline.trim() + "\n");
            }
        }

        return lines.toString();
    }

    @Override
    public String quickenString() {
        StringBuilder builder = new StringBuilder();
        SimpleDateFormat md = new SimpleDateFormat("M/d");
        SimpleDateFormat yy = new SimpleDateFormat("yy");
        builder.append("D" + md.format(transdate) + "'" + yy.format(transdate));
        builder.append("\n");
        builder.append("U" + this.total.negate());
        builder.append("\n");
        builder.append("T" + this.total.negate());
        builder.append("\n");
        builder.append("N" + this.orderNumber);
        builder.append("\n");
        builder.append("PULine");
        builder.append("\n");
        if (this.state.equalsIgnoreCase("CA") && this.tax.negate().doubleValue() != 0) {
            builder.append("M" + this.city + ", " + this.zip + " tax paid " + this.tax);
            builder.append("\n");
        }
        builder.append("COntario, CA 91761");
        builder.append("\n");
        builder.append("A12575 Uline Drive");
        builder.append("\n");
        builder.append("LMerchandise/Ebay");
        builder.append("\n");
        builder.append("PULine");
        builder.append("\n");
        builder.append("A12575 Uline Drive");
        builder.append("\n");
        builder.append("APleasant Prairie, WI 53158");
        builder.append("\n");
        builder.append("A");
        builder.append("\n");
        builder.append("A");
        builder.append("\n");
        builder.append("SMerchandise");
        builder.append("\n");
        builder.append("EShipping boxes");
        builder.append("\n");
        if (this.state.equalsIgnoreCase("CA")  && this.tax.negate().doubleValue() != 0) {
            builder.append("$" + this.total.subtract(this.tax).negate());
            builder.append("\n");
            builder.append("S[*Sales Tax*]\n");
            builder.append("$" + this.tax.negate());
            builder.append("\n");
        } else {
            builder.append("$" + this.total.negate());
            builder.append("\n");
        }
        builder.append("XI2");
        builder.append("\n");
        builder.append("XE" + md.format(transdate) + "'" + yy.format(transdate));
        builder.append("\n");
        builder.append("XR0.0");
        builder.append("\n");
        builder.append("XT0.00");
        builder.append("\n");
        builder.append("XN");
        builder.append("\n");
        builder.append("XSShipping boxes");
        builder.append("\n^\n");
        return builder.toString();
    }

    @Override
    public String mysqlString() {
        String ticket = "" + this.getVendor() + this.orderNumber;
        if (MainGmail.isProcessed(ticket)) {
            return null;
        }
        if (state == null) {
            //return null;
        } else if (state.equalsIgnoreCase("ca") || state.equalsIgnoreCase("california")) {
            state = "CA";
        }  else {
            //return null;
        }
        StringBuilder builder = new StringBuilder();
        try {
            SimpleDateFormat orderdate = new SimpleDateFormat("yyyyMMdd");
            builder.append("insert into purchases values (");
            builder.append("'ULine', ");
            builder.append(orderdate.format(transdate) + ", ");
            builder.append(orderNumber + ", ");
            builder.append("'" + MySQLPO.encode(product) + "',");
            builder.append("'" + city + "',");
            builder.append("'" + state + "',");
            builder.append("'" + MySQLPO.encode(zip.replaceAll("United States ", "")) + "',");
            builder.append(subtotal.doubleValue() + ", ");
            builder.append(total.subtract(subtotal).subtract(tax).doubleValue() + ", ");
            builder.append(tax.doubleValue() + ", ");
            builder.append(total.doubleValue());
            builder.append(");");
        } catch (Exception e) {
            System.out.println(">>>> " + this.getVendor() + " " + this.getOrderNumber());
            System.out.println("body[" + body + "]");
            e.printStackTrace();
        } finally {
            return builder.toString();
        }
    }

    private void parseOrderNumber() {
        String seq = "Order#:[\\s]{0,50}[0-9]{0,10}";
        Pattern p = Pattern.compile(seq);
        Matcher m = p.matcher(body);
        if (m.find()) {
            int start = m.start();
            int end = m.end();
            //System.out.println("["+body+"]");
            this.setOrderNumber(body.substring(start + 9, end).trim());
        }
    }

    private void parseShipTo() throws IOException {
        String seq = "Ship To:";
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine().trim(); // This line is Ship To:<br />
        while (line.toLowerCase().startsWith("mira") == false) {
            line = br.readLine().trim();
        }
        line = line.substring(37);
        this.setCity(line.substring(0, line.indexOf(",")));
        this.setState(line.substring(line.indexOf(", ") + 2, line.indexOf(", ") + 4));
        this.setZip(line.substring(line.indexOf(",") + 5));
    }

    private void parseOrderTotal() throws IOException {
        String seq = "Total*";
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine().trim(); // Total*      $167.71
        line = line.substring(line.indexOf("$") + 1, line.indexOf(".") + 3);
        this.total = new BigDecimal(line);
    }

    private void parseSubTotal() throws IOException {
        String seq = "Sub-Total";
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine().trim(); // Sub-Total   $155.65
        line = line.substring(line.indexOf("$") + 1, line.indexOf(".") + 3);
        this.subtotal = new BigDecimal(line);

    }

    private void parseTax() throws IOException {
        String seq = "Sales Tax";
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine(); // Sales Tax    $12.06
        line = line.substring(line.indexOf("$") + 1, line.indexOf(".") + 3);
        this.tax = new BigDecimal(line);
    }


    private void parseProduct() throws IOException {
        String seq = "Description";
        //System.out.println(body);
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine().trim();
        line = br.readLine().trim();
        this.product = line;
    }

    private void parseShipping() throws IOException {
        this.shipping = total.subtract(subtotal).subtract(tax);
    }


    private void parseOrderDate() throws IOException, ParseException {
        //Order Entered On: <strong>10/26/2011</strong><br />
        String seq = "Order Date: ";
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine().trim(); // This line is Order Entered On: <strong>10/26/2011</strong><br />
        line = line.substring(12);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        this.setTransdate(sdf.parse(line));
    }
}
