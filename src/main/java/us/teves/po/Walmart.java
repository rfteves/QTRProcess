/*s
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.po;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author rfteves
 */
public class Walmart extends PO {

    Set<String> address = new LinkedHashSet<String>();
    private boolean pickup;

    public Walmart(boolean pickup) {
        this.pickup = pickup;
        this.shipping = new BigDecimal(0);
    }

    @Override
    public void extract(Message message) {
        try {
            MimeMultipart multipart = (MimeMultipart) message.getContent();
            BodyPart bodypart = multipart.getBodyPart(0);
            String body = (String) bodypart.getContent();
            if (pickup) {
                //System.out.println(body);
                //System.exit(0);
                this.zip = "92606";
                this.city = "Irvine";
                this.state = "CA";
            }
            if (false) {
                System.out.println(body);
                System.exit(0);
            }
            String[] lines = body.split("\n");
            boolean captureShippingAddress = false, captureProduct = false, captureSub = false, captureTax = false, captureShipping = false, captureTotal = false;
            for (String line : lines) {
                String dash = line.trim().replaceAll("&#039;", "'");
                if (dash.length() == 0) {
                    continue;
                }
                //System.out.println("dash: " + dash);
                if (captureProduct) {
                    if (dash.startsWith("--------")) {
                    } else {
                        captureProduct = false;
                        this.product = dash.substring(0, Math.min(30, dash.trim().length()));
                    }
                } else if (captureShippingAddress) {
                    if (dash.startsWith("====================================")) {
                        captureShippingAddress = false;
                        if (!pickup) {
                            String[] adds = address.toArray(new String[0]);
                            this.zip = adds[adds.length - 2].substring(adds[adds.length - 2].indexOf(",") + 5);
                            this.city = adds[adds.length - 2].substring(0, adds[adds.length - 2].indexOf(","));
                            this.state = adds[adds.length - 2].substring(adds[adds.length - 2].indexOf(",") + 1, adds[adds.length - 2].indexOf(",") + 4);
                            System.out.println("");
                        }
                    } else {
                        address.add(dash.trim());
                    }
                } else if (captureSub) {
                    if (dash.startsWith("$")) {
                        captureSub = false;
                        this.subtotal = new BigDecimal(dash.substring(1, dash.length() - 5));
                    }
                } else if (captureTotal) {
                    if (dash.startsWith("$")) {
                        captureTotal = false;
                        this.total = new BigDecimal(dash.substring(1, dash.length() - 5));
                    }
                } else if (captureTax) {
                    if (dash.startsWith("$")) {
                        this.tax = new BigDecimal(dash.substring(1, dash.length() - 5));
                    }
                } else if (captureShipping) {
                    if (dash.startsWith("$")) {
                        captureShipping = false;
                        this.shipping = new BigDecimal(dash.substring(1, dash.length() - 5));
                    }
                } else if (dash.toLowerCase().indexOf("walmart.com") != -1 &&
                        dash.toLowerCase().indexOf("order number:") != -1) {
                    dash = dash.substring(dash.indexOf(":") + 1).trim();// 2677808-695810
                    this.orderNumber = dash.replaceAll("-", "").substring(5);
                } else if (dash.startsWith("Walmart Pick Up Today                                Order Number:")) {
                    dash = dash.substring(dash.indexOf(":") + 1).trim();// 2677808-695810
                    this.orderNumber = dash.replaceAll("-", "").substring(5);
                } else if (dash.startsWith("Order Date:")) {
                    // 22 AUG 2011 06:51
                    dash = dash.substring(11).trim();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    this.transdate = sdf.parse(dash);
                } else if (dash.startsWith("ITEM                                     QTY  ARRIVAL DATE                  PRICE")) {
                    captureProduct = true;
                } else if (dash.startsWith("ITEM                                     QTY  APPROXIMATE PICKUP TIME       PRICE")) {
                    captureProduct = true;
                } else if (dash.startsWith("Shipping: $")) {
                    this.subtotal = this.subtotal.add(new BigDecimal(dash.substring(11).replaceAll(",", "").trim()));
                } else if (dash.startsWith("Ship to Home Address:")) {
                    captureShippingAddress = true;
                } else if (dash.startsWith("Shipping Address:")) {
                    captureShipping = true;
                } else if (dash.startsWith("Tax: $")) {
                    this.tax = new BigDecimal(dash.substring(6).replaceAll(",", "").trim());
                } else if (dash.startsWith("Subtotal: $")) {
                    this.subtotal = new BigDecimal(dash.substring(11).replaceAll(",", "").trim());
                } else if (dash.startsWith("Walmart.com Total: $")) {
                    this.total = new BigDecimal(dash.substring("Walmart.com Total: $".length()).replaceAll(",", "").trim());
                } else if (dash.startsWith("Walmart Pick Up Today Total: $")) {
                    this.total = new BigDecimal(dash.substring("Walmart Pick Up Today Total: $".length()).replaceAll(",", "").trim());
                }
            }
        } catch (ParseException ex) {
            Logger.getLogger(Walmart.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(Walmart.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            ex.printStackTrace();
            Logger.getLogger(Walmart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String quickenString() {
        StringBuilder builder = new StringBuilder();
        SimpleDateFormat md = new SimpleDateFormat("M/d");
        SimpleDateFormat yy = new SimpleDateFormat("yy");
        if (pickup) {
            //builder.append("*********************\n");
        }
        builder.append("D" + md.format(transdate) + "'" + yy.format(transdate));
        builder.append("\n");
        builder.append("U" + this.total.negate());
        builder.append("\n");
        builder.append("T" + this.total.negate());
        builder.append("\n");
        builder.append("C*");
        builder.append("\n");
        builder.append("N" + this.orderNumber);
        builder.append("\n");
        builder.append("PWalmart.com");
        builder.append("\n");
        builder.append("CNatick, MA 01760-2400");
        builder.append("\n");
        builder.append("LMerchandise");
        builder.append("\n");
        builder.append("AWalmart.com");
        builder.append("\n");
        builder.append("A1 Mercer Rd");
        builder.append("\n");
        builder.append("ANatick, MA 01760-2400");
        builder.append("\n");
        builder.append("A");
        builder.append("\n");
        builder.append("A");
        builder.append("\n");
        builder.append("SMerchandise");
        builder.append("\n");
        builder.append("E" + this.product);
        builder.append("\n");
        builder.append("$" + this.subtotal.add(this.shipping).negate());
        builder.append("\n");
        builder.append("XI2");
        builder.append("\n");
        builder.append("XE" + md.format(transdate) + "'" + yy.format(transdate));
        builder.append("\n");
        builder.append("XR0.0");
        builder.append("\n");
        builder.append("XT0.0");
        builder.append("\n");
        builder.append("XN");
        builder.append("\n");
        builder.append("XS" + this.product);
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
        SimpleDateFormat orderdate = new SimpleDateFormat("yyyyMMdd");
        builder.append("insert into purchases values (");
        builder.append("'walmart', ");
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
        return builder.toString();
    }
}
