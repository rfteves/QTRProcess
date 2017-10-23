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


/**
 *
 * @author rfteves
 */
public class GreenMountain extends PO {


    Set<String> address = new LinkedHashSet<String>();
    private String body;
    Set<String> orders = new HashSet<String>();

    public GreenMountain() {
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
            }
            if (true) {
                //System.out.println(body);
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
        builder.append("PGMC");
        builder.append("\n");
        builder.append("LMerchandise/Ebay");
        builder.append("\n");
        builder.append("PKeurig");
        builder.append("\n");
        builder.append("A55 Walkers Brook Drive");
        builder.append("\n");
        builder.append("AReading, MA 01867");
        builder.append("\n");
        builder.append("A");
        builder.append("\n");
        builder.append("A");
        builder.append("\n");
        builder.append("SMerchandise");
        builder.append("\n");
        builder.append("EKeurig coffee brewer");
        builder.append("\n");
        builder.append("$" + this.total.negate());
        builder.append("\n");
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
        builder.append("XSKeurig coffee brewer");
        builder.append("\n^\n");
        return builder.toString();
    }

    @Override
    public String mysqlString() {
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
            builder.append("'gmc', ");
            builder.append(orderdate.format(transdate) + ", ");
            builder.append(orderNumber + ", ");
            builder.append("'" + MySQLPO.encode(product) + "',");
            builder.append("'" + city + "',");
            builder.append("'" + state + "',");
            builder.append("'" + zip.replaceAll("United States ", "").trim() + "',");
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
        String seq = "Order Number: <strong>[A-Za-z0-9][0-9]{9}</strong>";
        Pattern p = Pattern.compile(seq);
        Matcher m = p.matcher(body);
        if (m.find()) {
            int start = m.start();
            int end = m.end();
            this.setOrderNumber(body.substring(start + 23, end - 9));
        }
    }

    private void parseShipTo() throws IOException {
        String seq = "Ship To:<br />";
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine(); // This line is Ship To:<br />
        line = br.readLine().trim(); // Name
        this.setPayee(line.substring(8, line.indexOf("<br />")));
        line = br.readLine().trim(); // Street
        line = br.readLine().trim(); // City, State Zip
        this.setCity(line.substring(0, line.indexOf(",")));
        this.setState(line.substring(line.indexOf(", ") + 2, line.indexOf(", ") + 4));
        line = br.readLine().trim(); // City, State Zip
        this.setZip(line.substring(line.indexOf(",") + 5, line.indexOf("<br />")));
    }

    private void parseOrderTotal() throws IOException {
        String seq = "Order Total:</td>";
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine(); // This line is Order Total:</td>
        line = br.readLine().trim(); // This line is like <td align="right">$61.12</td>
        line = line.substring(line.indexOf("$") + 1, line.indexOf("</td>"));
        this.total = new BigDecimal(line);
    }

    private void parseSubTotal() throws IOException {
        String seq = "Product Total:</td>";
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine(); // This line is Order Total:</td>
        line = br.readLine().trim(); // This line is like <td align="right">$61.12</td>
        line = line.substring(line.indexOf("$") + 1, line.indexOf("</td>"));
        this.subtotal = new BigDecimal(line);
        // Discount
        seq = "Discount:</td>";
        if (body.indexOf(seq) != -1) {
            parseThis = body.substring(body.indexOf(seq));
            br = new BufferedReader(new StringReader(parseThis));
            line = br.readLine(); // This line is Order Total:</td>
            line = br.readLine().trim(); // This line is like <td align="right">$61.12</td>
            line = line.substring(line.indexOf("$") + 1, line.indexOf("</td>"));
            BigDecimal discount = new BigDecimal(line);
            this.subtotal = subtotal.subtract(discount);
        }

    }

    private void parseTax() throws IOException {
        String seq = "Tax:</td>";
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine(); // This line is Order Total:</td>
        br.readLine();
        line = br.readLine().trim(); // This line is like <td align="right">$61.12</td>
        line = line.substring(line.indexOf("$") + 1, line.indexOf("</td>"));
        this.tax = new BigDecimal(line);
    }


    private void parseProduct() throws IOException {
        String seq = "Extended Price</th>";
        if (body.indexOf(seq) == -1) {
            seq = "Net Price</th>";
        }
        if (body.indexOf(seq) == -1) {
            seq = "Est. Cost</th>";
        }//Est. Cost
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine(); // Net price
        line = br.readLine(); // </tr>
        line = br.readLine().trim(); // product line
        int firstIndex = line.indexOf("</td><td>");
        int secondIndex = line.indexOf("</td><td", firstIndex + 1);
        this.product = line.substring(firstIndex + 9, secondIndex);
    }

    private void parseShipping() throws IOException {
        this.shipping = total.subtract(subtotal).subtract(tax);
    }


    private void parseOrderDate() throws IOException, ParseException {
        //Order Entered On: <strong>10/26/2011</strong><br />
        String seq = "Order Entered On: <strong>";
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine().trim(); // This line is Order Entered On: <strong>10/26/2011</strong><br />
        line = line.substring(26, 36);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        this.setTransdate(sdf.parse(line));
    }
}
