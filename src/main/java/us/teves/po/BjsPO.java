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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author rfteves
 */
public class BjsPO extends PO {

    Set<String> address = new LinkedHashSet<String>();
    private String body;

    public BjsPO() {
        this.shipping = new BigDecimal(0);
    }

    public void extract2() {
        int pos = 0;
        String[]lines = null;
        String group = null;
        Pattern pattern = Pattern.compile("Order #: [0-9]+");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            group = matcher.group();
            pattern = Pattern.compile("[0-9]+");
            matcher = pattern.matcher(group);
            matcher.find();
            this.orderNumber = matcher.group();
        }
        //System.out.println("order # " + this.orderNumber);
        pattern = Pattern.compile("Date: [0-9]{2}\\-[0-9]{2}\\-[0-9]{4}");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            group = matcher.group();
            pattern = Pattern.compile("[0-9]{2}\\-[0-9]{2}\\-[0-9]{4}");
            matcher = pattern.matcher(group);
            matcher.find();
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
            try {
                this.transdate = sdf.parse(matcher.group());
            } catch (ParseException ex) {
                Logger.getLogger(BjsPO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //System.out.println("order # " + this.orderNumber);
        pattern = Pattern.compile("Product Line");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            pos = matcher.start();
            group = body.substring(body.indexOf(":", pos) + 1, body.indexOf("\n", pos)).trim();
            this.product = group;
        }
        //System.out.println("product line: " + group);
        pattern = Pattern.compile("Ship to");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            pos = matcher.start();
            group = body.substring(pos);
            lines = group.split("\n");
            pattern = Pattern.compile("[a-zA-Z ]+, [a-zA-Z]{2} [0-9\\-]{5,10}");
            for (String line: lines) {
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    group = matcher.group();
                    break;
                }
            }
            pos = group.lastIndexOf(",");
            this.city = group.substring(0, pos);
            this.state = group.substring(pos + 2, pos + 4);
            this.zip = group.substring(pos + 5);
        }
        //System.out.println("ciy, state zip: " + group);
        pattern = Pattern.compile("Subtotal:");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            pos = matcher.start();
            group = body.substring(body.indexOf("$", pos) + 1, body.indexOf("\n", pos));
            this.subtotal = new BigDecimal(group);
        }
        //System.out.println("subtotal: " + this.subtotal);
        pattern = Pattern.compile("Tax:");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            pos = matcher.start();
            group = body.substring(body.indexOf("$", pos) + 1, body.indexOf("\n", pos));
            this.tax = new BigDecimal(group);
        }
        //System.out.println("tax: " + this.tax);
        pattern = Pattern.compile("Shipping:");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            pos = matcher.start();
            group = body.substring(body.indexOf("$", pos) + 1, body.indexOf("\n", pos));
            this.shipping = new BigDecimal(group);
        }
        //System.out.println("shipping: " + this.shipping);
        pattern = Pattern.compile("Total[$0-9\\. ]+");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            pos = matcher.start();
            group = matcher.group();
            pattern = Pattern.compile("[0-9\\.]+");
            matcher = pattern.matcher(group);
            matcher.find();
            group = matcher.group();
            this.total = new BigDecimal(group);
        }
    }

    @Override
    public void extract(Message message) {
        String dash = null;
        try {

            if (message.getContent() instanceof MimeMultipart) {
                MimeMultipart mp = (MimeMultipart) message.getContent();
                body = (String) mp.getBodyPart(0).getContent();
            } else {
                body = (String) message.getContent();
                body = CostcoPO.stripHtml(body);
            }
            if (false) {
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println(body);
                System.out.println("");
                System.exit(0);
            } else {
                this.extract2();
                return;
            }
            String[]lines = body.split("\n");
            boolean captureShippingAddress = false, captureProduct = false, captureSub = false, captureTax = false, captureShipping = false, captureTotal = false;
            for (String line: lines) {
                dash = line.trim().replaceAll("&#039;", "'");
                if (dash.length() == 0) {
                    continue;
                }
                //System.out.println("dash: " + dash);
                if (captureProduct) {
                   if (dash.equals("</td>") || dash.equals("<td>")) {

                   } else {
                       captureProduct = false;
                       this.product = dash.substring(0, dash.length() - 5);
                   }
                } else if (captureShippingAddress) {
                    if (dash.startsWith("<!-- End - JSP File Name:  Address2.jspf -->")) {
                        captureShippingAddress = false;
                        //System.out.println("address: " + address);
                        String[]adds = address.toArray(new String[0]);
                        if (adds.length == 7) {
                            this.zip = adds[5].substring(adds[5].lastIndexOf(" "));
                            this.city = adds[4].substring(0, adds[4].indexOf(","));
                            this.state = adds[4].substring(adds[4].indexOf(",") + 2);
                        } else if (adds.length == 6) {
                            this.zip = adds[4].substring(adds[4].lastIndexOf(" "));
                            this.city = adds[3].substring(0, adds[3].indexOf(","));
                            this.state = adds[3].substring(adds[3].indexOf(",") + 2);
                        } else {
                            this.zip = adds[3].substring(adds[3].lastIndexOf(" "));
                            this.city = adds[2].substring(0, adds[2].indexOf(","));
                            this.state = adds[2].substring(adds[2].indexOf(",") + 2);
                        }
                        break;
                    } else {
                        address.add(dash.substring(4, dash.length() - 5));
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
                       captureTax = false;
                       this.tax = new BigDecimal(dash.substring(1, dash.length() - 5));
                   }
                } else if (captureShipping) {
                   if (dash.startsWith("$")) {
                       captureShipping = false;
                       this.shipping = new BigDecimal(dash.substring(1, dash.length() - 5));
                   }
                } else if (dash.startsWith("Order #:")) {
                    dash = dash.substring(8).trim();
                    this.orderNumber = dash.substring(0, 7);
                } else if (dash.startsWith("Date:")) {
                    dash = dash.trim().substring(6);
                    dash = dash.substring(0, 10);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    this.transdate = sdf.parse(dash);
                } else if (dash.startsWith("Product Line  :")) {
                    captureProduct = true;
                } else if (dash.startsWith("Subtotal:")) {
                    captureSub = true;
                } else if (dash.indexOf("<!-- Start - JSP File Name:  Address2.jspf -->") != -1) {
                    captureShippingAddress = true;
                } else if (dash.startsWith("Shipping:")) {
                    captureShipping = true;
                } else if (dash.startsWith("Tax:")) {
                    captureTax = true;
                } else if (dash.startsWith("Sub-Total:")) {
                    this.subtotal = new BigDecimal(dash.substring(dash.indexOf("$") + 1));
                } else if (dash.startsWith("Total")) {
                    captureTotal = true;
                }
            }
        } catch (ParseException ex) {
            System.out.println("dash: " + dash);
            Logger.getLogger(BjsPO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("dash: " + dash);
            ex.printStackTrace();
            Logger.getLogger(BjsPO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            System.out.println("dash: " + dash);
            ex.printStackTrace();
            Logger.getLogger(BjsPO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
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
        builder.append("C*");
        builder.append("\n");
        builder.append("N" + this.orderNumber);
        builder.append("\n");
        builder.append("PBJ's Wholesale");
        builder.append("\n");
        builder.append("CNatick, MA 01760-2400");
        builder.append("\n");
        builder.append("LMerchandise");
        builder.append("\n");
        builder.append("ABJ's Wholesale");
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
        builder.append("$" + this.total.add(this.shipping).negate());
        builder.append("\n");
        /*if (this.state.equalsIgnoreCase("CA")) {
            builder.append("S[Sales Tax - CA]");
            builder.append("\n");
            builder.append("$" +
                    "" + this.tax.negate());
            builder.append("\n");
        }*/
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
        builder.append("'bjs', ");
        builder.append(orderdate.format(transdate) + ", ");
        builder.append(orderNumber + ", ");
        builder.append("'" + MySQLPO.encode(product) + "',");
        builder.append("'" + city + "',");
        builder.append("'" + state + "',");
        builder.append("'" + zip.replaceAll("United", "").replaceAll("States", "").trim()  + "',");
        builder.append(subtotal.doubleValue() + ", ");
            builder.append(total.subtract(subtotal).subtract(tax).doubleValue() + ", ");
        builder.append(tax.doubleValue() + ", ");
        builder.append(total.doubleValue());
        builder.append(");");
        return builder.toString();
    }
}
