/*s
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.po;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author rfteves
 */
public class KeurigPO extends PO {

    private String body;
    Set<String> orders = new HashSet<String>();

    public KeurigPO() {
        this.shipping = new BigDecimal(0);
    }

    public void extract2() {
        int pos = 0;
        String[] lines = null;
        String group = null;
        Pattern pattern = Pattern.compile("Order Number: H[0-9]+");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            group = matcher.group();
            pattern = Pattern.compile("[0-9]+");
            matcher = pattern.matcher(group);
            matcher.find();
            this.orderNumber = matcher.group().trim();
        }
        //System.out.println("order # " + this.orderNumber);
        pattern = Pattern.compile("Order Entered On: [0-9]{2}/[0-9]{2}/[0-9]{4}");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            group = matcher.group();
            pattern = Pattern.compile("[0-9]{2}/[0-9]{2}/[0-9]{4}");
            matcher = pattern.matcher(group);
            matcher.find();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            try {
                this.transdate = sdf.parse(matcher.group());
            } catch (ParseException ex) {
                Logger.getLogger(BjsPO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //System.out.println("order # " + this.orderNumber);
        pattern = Pattern.compile("Net Price");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            pos = matcher.start();
            group = body.substring(pos);
            lines = group.split("\n");
            this.product = lines[1].substring(0, lines[1].indexOf("$"));
        }
        //System.out.println("product line: " + group);
        pattern = Pattern.compile("Ship To");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            pos = matcher.start();
            group = body.substring(pos);
            lines = group.split("\n");
            pattern = Pattern.compile("[a-zA-Z ]+, [a-zA-Z]{2} [0-9\\-]{5,10}");
            boolean first = true;
            for (String line : lines) {
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    group = matcher.group();
                    break;
                } else if (first) {
                    first = false;
                } else if (this.getPayee() == null) {
                    this.setPayee(line);
                } else if (this.getAddress() == null) {
                    this.setAddress(line);
                } else if (this.getAddress2() == null) {
                    this.setAddress2(line);
                }
            }
            pos = group.lastIndexOf(",");
            this.city = group.substring(0, pos);
            this.state = group.substring(pos + 2, pos + 4);
            this.zip = group.substring(pos + 5);
        }
        //System.out.println("ciy, state zip: " + group);
        pattern = Pattern.compile("Order Sub-Total:");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            pos = matcher.start();
            group = body.substring(pos);
            lines = group.split("\n");
            this.subtotal = new BigDecimal(lines[1].substring(1));
        }
        //System.out.println("subtotal: " + this.subtotal);
        pattern = Pattern.compile("Taxes");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            pos = matcher.start();
            group = body.substring(pos);
            lines = group.split("\n");
            this.tax = new BigDecimal(lines[1].substring(1));
        }
        //System.out.println("tax: " + this.tax);
        pattern = Pattern.compile("Shipping Charges");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            pos = matcher.start();
            group = body.substring(pos);
            lines = group.split("\n");
            this.shipping = new BigDecimal(lines[1].substring(1));
        }
        //System.out.println("shipping: " + this.shipping);
        pattern = Pattern.compile("Order Total");
        matcher = pattern.matcher(body);
        if (matcher.find()) {
            pos = matcher.start();
            group = body.substring(pos);
            lines = group.split("\n");
            this.total = new BigDecimal(lines[1].substring(1));
        }
    }

    @Override
    public void extract(Message message) {
        try {
            if (this.getVendor().equals("keurigorder")) {
                this.parseKeurigOrder(message);
                //this.setVendor("keurig");
                return;
            } else if (this.getVendor().equals("keurigshipping")) {
                this.parseKeurigShipping(message);
                //this.setVendor("keurig");
                return;
            }
            if (message.getContent() instanceof MimeMultipart) {
                MimeMultipart mp = (MimeMultipart) message.getContent();
                body = (String) mp.getBodyPart(0).getContent();
            } else {
                body = (String) message.getContent();
                body = CostcoPO.stripHtml(body, true);
            }
            if (false) {
                System.exit(0);
            } else {
                this.extract2();
                return;
            }
            //MimeMultipart mp = (MimeMultipart) message.getContent();
            //body = (String) mp.getBodyPart(0).getContent();
            //System.out.println(body);
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
            Logger.getLogger(KeurigPO.class.getName()).log(Level.SEVERE, null, ex);
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
        builder.append("PKeurig");
        builder.append("\n");
        builder.append("LMerchandise/Ebay");
        builder.append("\n");
        builder.append("AKEURIG.COM 01867");
        builder.append("\n");
        if (this.state.equalsIgnoreCase("CA") && this.tax.negate().doubleValue() != 0) {
            builder.append("M" + this.city + ", " + this.zip + " tax paid " + this.tax);
            builder.append("\n");
        }
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
        builder.append("E" + this.product);
        builder.append("\n");
        if (this.state.equalsIgnoreCase("CA") && this.tax.negate().doubleValue() != 0) {
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
        } else {
            //return null;
        }
        StringBuilder builder = new StringBuilder();
        try {
            SimpleDateFormat orderdate = new SimpleDateFormat("yyyyMMdd");
            builder.append("insert into purchases values (");
            builder.append("'keurig', ");
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
        String seq = "Order Number: <strong>[H][0-9]{9}</strong>";
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
        Pattern pattern = Pattern.compile("[0-9]{5}-[09]{4}$");
        Matcher matcher = null;
        while (true) {
            line = br.readLine().trim();
            matcher = pattern.matcher(line);
            if (this.getPayee() == null) {
                this.setPayee(line);
            } else if (matcher.find()) {
                this.setCity(line.substring(0, line.indexOf(",")));
                this.setState(line.substring(line.indexOf(", ") + 2, line.indexOf(", ") + 4));
                this.setZip(line.substring(line.indexOf(",") + 5, line.indexOf("<br />")));
            } else if (this.getAddress() == null) {
                this.setAddress(line);
            } else if (this.getAddress2() == null) {
            }
        }
        /*line = br.readLine().trim(); // Name
        this.setPayee(line.substring(8, line.indexOf("<br />")));
        line = br.readLine().trim(); // Street
        line = br.readLine().trim(); // City, State Zip
        this.setCity(line.substring(0, line.indexOf(",")));
        this.setState(line.substring(line.indexOf(", ") + 2, line.indexOf(", ") + 4));
        this.setZip(line.substring(line.indexOf(",") + 5, line.indexOf("<br />")));*/
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
        String seq = "Sub-Total:</td>";
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
        String seq = "Taxes:</td>";
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine(); // This line is Order Total:</td>
        line = br.readLine().trim(); // This line is like <td align="right">$61.12</td>
        line = line.substring(line.indexOf("$") + 1, line.indexOf("</td>"));
        this.tax = new BigDecimal(line);
    }

    private void parseProduct() throws IOException {
        String seq = "Net Price</th>";
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

    public void parseKeurigShipping(Message message) {
        try {
            if (message.getContent() instanceof MimeMultipart) {
                MimeMultipart mp = (MimeMultipart) message.getContent();
                body = (String) mp.getBodyPart(0).getContent();
            } else {
                body = (String) message.getContent();
            }
            Document doc = Jsoup.parse(body);
            Elements products = doc.select("a");
            for (int xx = 0; xx < products.size(); ++xx) {
                if (products.get(xx).outerHtml().contains("http://www.keurig.com/my-account/order")) {
                    this.orderNumber = products.get(xx).text();
                } else if (products.get(xx).outerHtml().contains("www.fedex.com") ||
                        products.get(xx).outerHtml().contains("www.google.com")) {
                    this.setTracking(products.get(xx).text());
                }
            }
            this.setViatype("FXHD");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void parseKeurigOrder(Message message) {
        try {
            if (message.getContent() instanceof MimeMultipart) {
                MimeMultipart mp = (MimeMultipart) message.getContent();
                body = (String) mp.getBodyPart(0).getContent();
            } else {
                body = (String) message.getContent();
            }
            //System.out.println(body);//if (true)System.exit(0);
            if (body.toLowerCase().indexOf("california") != -1) {
                System.out.println("california");
            }
            Document doc = Jsoup.parse(body);
            Elements elements = doc.select("span");
            int capture = 0;
            for (Element element : elements) {
                //System.out.println(element.outerHtml());
                //System.out.println(element.ownText());
                if (element.ownText().equalsIgnoreCase("your order number")) {
                    capture = 1;
                } else if (capture == 1) {
                    capture = 0;
                    this.orderNumber = element.ownText();
                } else if (element.ownText().startsWith("Entered on: ")) {
                    this.transdate = mdy.parse(element.ownText().substring(12).trim());
                } else if (element.ownText().indexOf("ship your order with") != -1 ||
                        element.ownText().indexOf("Shipping Address:") != -1) {
                    capture = 2;
                } else if (capture == 2) {
                    String[] lines = element.outerHtml().replace("<span>", "").replace("</span>", "").split("<br />");
                    this.parseShipToLines(lines);
                    capture = 0;
                } else if (element.ownText().contains("Gift Message:")) {
                    capture = 3;
                } else if (capture == 3) {
                    capture = 0;
                    System.out.println();
                    this.setMarketOrderNumber(element.ownText().replaceAll(" - THANK YOU!!!", ""));
                }
            }
            elements = doc.select("td");
            capture = 0;
            this.product = "Coffee/Brewer";
            for (Element element : elements) {
                //System.out.println(element.outerHtml());
                //System.out.println(element.ownText());
                if (element.ownText().indexOf("Estimated Total") != -1 ||
                        element.ownText().indexOf("Order Total") != -1) {
                    capture = 1;
                } else if (capture == 1) {
                    capture = 0;
                    this.total = new BigDecimal(element.ownText().replaceAll(",", "").substring(1));
                } else if (element.ownText().indexOf("Items Subtotal") != -1 ||
                        element.ownText().indexOf("Item Subtotal") != -1) {
                    capture = 2;
                } else if (capture == 2) {
                    capture = 0;
                    this.subtotal = new BigDecimal(element.ownText().replaceAll(",", "").substring(1));
                } else if (element.ownText().startsWith("Shipping")) {
                    capture = 3;
                } else if (capture == 3) {
                    capture = 0;
                    if (element.ownText().equals("FREE")) {
                        this.shipping = BigDecimal.ZERO;
                    } else {
                        this.shipping = new BigDecimal(element.ownText().substring(1));
                    }
                } else if (element.ownText().startsWith("Taxes")) {
                    capture = 4;
                } else if (capture == 4) {
                    capture = 0;
                    this.tax = new BigDecimal(element.ownText().substring(1));
                }
            }
            if (tax == null) {
                this.tax = BigDecimal.ZERO;
            }
            String startProducts = "<!-- inner data table -->";
            String endProducts = "<!-- end inner data table -->";
            int startIndex = body.indexOf(startProducts) + startProducts.length();
            int endIndex = body.toString().indexOf(endProducts);
            elements = Jsoup.parse(body.toString().substring(startIndex, endIndex)).select("td");
            for (Element element : elements) {
                //System.out.println("-->>" + element.outerHtml());
                //System.out.println("-->>" + element.ownText());
                if (element.outerHtml().indexOf("href") != -1 && element.outerHtml().indexOf("<b>") != -1) {
                    element = element.html(element.outerHtml().replaceAll("<br />", ""));
                    this.product = element.text().replaceAll("[^0-9\\)\\(\\.a-zA-Z ]", "");
                    capture = 1;
                } else if (capture == 1) {
                    capture = 0;
                    this.product += " QTY: " + element.ownText();
                    break;
                }
            }
            //if (true)System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] s) throws Exception {
        FileReader reader = new FileReader("c:/java/shippingxd.txt");
        BufferedReader buf = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = buf.readLine()) != null) {
            sb.append(line);
        }
        //System.out.println(sb.toString());
        Document doc = Jsoup.parse(sb.toString());
        int capture = 0;
        String strpattern = "a href=\"http://www.keurig.com/my-account/order/[0-9]{8,10}\"";
        Pattern pattern = Pattern.compile(strpattern);
        Matcher matcher = pattern.matcher(sb.toString());
        if (matcher.find()) {
            String href = matcher.group();
            pattern = Pattern.compile("[0-9]{8,10}");
            matcher = pattern.matcher(href);
            matcher.find();
            //System.out.println(matcher.group());
        }
        Pattern datePattern = Pattern.compile("[0-9]{2}/[0-9]{2}/[0-9]{4}");

        Elements elements = doc.select("span");
        for (Element element : elements) {
            //System.out.println(element.outerHtml());
            //System.out.println(element.ownText());
            line = element.ownText();
            Matcher datematcher = null;
            if (line.length() == 10) {
                datematcher = datePattern.matcher(line);
            }
            if (datematcher != null && datematcher.find()) {
                //System.out.println(dmy.parse(datematcher.group()));
            } else if (line.startsWith("Tracking Number")) {
                capture = 1;
            } else if (capture == 1) {
                String[] lines = element.outerHtml().replace("<!--Ms -->", "").replace("<span>", "").replace("</span>", "").split("<br />");
                for (String linex : lines) {
                    System.out.println(linex);
                }
                if (lines.length == 6) {
                    //this.setAddress(lines[1]);
                    //this.setCity(lines[2]);
                    //this.setState(lines[3].toLowerCase().startsWith("CALIFORNIA") ? "CA" : lines[3].substring(0, lines[3].indexOf(",")));
                    //this.setPayee(lines[0]);
                }
                capture = 2;
            } else if (capture == 2) {
                System.out.println("TRACKING #: " + element.ownText());
                capture = 0;
            }
        }
        String startProducts = "<!-- inner data table -->";
        String endProducts = "<!-- end inner data table -->";
        int startIndex = sb.toString().indexOf(startProducts) + startProducts.length();
        int endIndex = sb.toString().indexOf(endProducts);
        elements = Jsoup.parse(sb.toString().substring(startIndex, endIndex)).select("td");
        for (Element element : elements) {
            //System.out.println("-->>" + element.outerHtml());
            //System.out.println("-->>" + element.ownText());
            if (element.outerHtml().indexOf("href") != -1 && element.outerHtml().indexOf("<b>") != -1) {
                element = element.html(element.outerHtml().replaceAll("<br />", ""));
                System.out.println("--<<|||||" + element.text().replaceAll("[^0-9\\)\\(\\.a-zA-Z ]", ""));
                capture = 1;
            } else if (capture == 1) {
                capture = 0;
                System.out.println("--<<|||||QTY: " + element.ownText());
            }
        }
    }
    static SimpleDateFormat mdy = new SimpleDateFormat("MM-dd-yyyy");
    static SimpleDateFormat dmy = new SimpleDateFormat("dd/MM/yyyy");

    private void parseShipToLines(String[] lines) throws UnsupportedEncodingException {
        if (lines.length == 6 || lines.length == 7) {
            this.setAddress(StringEscapeUtils.unescapeHtml4(lines[1]).trim());
            this.setCity(lines[lines.length - 4].trim());
            this.setState(lines[lines.length - 3].trim().substring(0, lines[lines.length - 3].trim().indexOf(",")));
            this.setPayee(lines[0].trim().replaceAll("<[\\!\\-a-zA-Z ]{1,}>","").replaceAll("[ ]{2,}", " "));
            this.setZip(lines[lines.length - 3].trim().substring(lines[lines.length - 3].indexOf(",") + 1));

        }
    }
}
