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
import java.util.Date;
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
public class SamsClub extends PO {

  private String name, address, body;

  public SamsClub(Date transdate) {
    this.transdate = transdate;
  }
  private boolean nullifyQuicken = false;

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
      if (body.indexOf("CLICK 'N' PULL ORDERS:") != -1
        || body.indexOf("Pick Up Location:") != -1) {
        nullifyQuicken = true;
        this.setNullifyPO(true);
        return;
      }
      //System.out.println(body);
      if (false) {
        System.out.println("[");
        System.out.println(body);
        System.out.println("]");
        System.exit(0);
      }

      if (subject.toLowerCase().equalsIgnoreCase("Your SamsClub.com order")
        || subject.toLowerCase().equalsIgnoreCase("SamsClub.com order confirmation")) {
        this.parseOrderNumber();
        this.parseShipTo();
        this.parseOrderTotal();
        this.parseShipping();
        this.parseTax();
        this.parseSubTotal();
        this.parseProduct();
      }
    } catch (NumberFormatException nex) {
      nex.printStackTrace();
      System.out.println("dash: " + dash);
      System.exit(0);
    } catch (IOException ex) {
      ex.printStackTrace();
      Logger.getLogger(SamsClub.class.getName()).log(Level.SEVERE, null, ex);
    } catch (MessagingException ex) {
      ex.printStackTrace();
      Logger.getLogger(SamsClub.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public String quickenString() {
    if (nullifyQuicken) {
      return null;
    }
    String ticket = "" + this.getVendor() + this.orderNumber;
    if (MainGmail.isProcessed(ticket)) {
      return null;
    }
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
    builder.append("PSams Internet");
    builder.append("\n");
    if (this.state.equalsIgnoreCase("CA") && this.tax.negate().doubleValue() != 0) {
      builder.append("M" + this.city + ", " + this.zip + " tax paid " + this.tax);
      builder.append("\n");
    }
    builder.append("CBENTONVILLE, AR 72712-6209");
    builder.append("\n");
    builder.append("LMerchandise");
    builder.append("\n");
    builder.append("ASAMSCLUB.COM 00796686");
    builder.append("\n");
    builder.append("A702 SW 8TH ST");
    builder.append("\n");
    builder.append("ABENTONVILLE, AR 72712-6209");
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
    /*if (this.state.equalsIgnoreCase("CA")) {
        builder.append("$" + this.subtotal.add(this.shipping).negate());
        builder.append("\n");
        builder.append("S[Sales Tax - CA]");
        builder.append("\n");
        builder.append("$" +
        "" + this.tax.negate());
        builder.append("\n");
        } else {*/
    //}
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
    } else {
      //return null;
    }
    StringBuilder builder = new StringBuilder();
    try {
      SimpleDateFormat orderdate = new SimpleDateFormat("yyyyMMdd");
      builder.append("insert into purchases values (");
      builder.append("'samsclub', ");
      builder.append(orderdate.format(transdate) + ", ");
      builder.append(orderNumber + ", ");
      builder.append("'" + MySQLPO.encode(product) + "',");
      builder.append("'" + city + "',");
      builder.append("'" + state + "',");
      builder.append("'" + zip.replaceAll("United States ", "").trim() + "',");
      builder.append(subtotal.doubleValue() + ", ");
      builder.append(total.subtract(subtotal).subtract(tax).doubleValue() + ", ");
      builder.append(tax.doubleValue() + ", ");
      builder.append(total.doubleValue() + " ");
      builder.append(");");
    } catch (Exception e) {
      System.out.println(">>>> " + this.getVendor() + " " + this.getOrderNumber());
      e.printStackTrace();
      System.exit(0);
    } finally {
      return builder.toString();
    }
  }

  private void parseOrderNumber() throws IOException {
    String seq = "Your Order Number is[\\s]+[0-9]+";
    Pattern p = Pattern.compile(seq, Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(body);
    if (m.find()) {
      p = Pattern.compile("[0-9]+");
      m = p.matcher(m.group());
      m.find();
      String s = m.group().trim();
      this.orderNumber = s;
    } else {
      seq = "Order #[\\s]+[0-9]+See your order status";
      p = Pattern.compile(seq, Pattern.CASE_INSENSITIVE);
      m = p.matcher(body);
      if (m.find()) {
        p = Pattern.compile("[0-9]+");
        m = p.matcher(m.group());
        m.find();
        String s = m.group().trim();
        this.orderNumber = s;
      } else {
        System.out.println("Not able to find order number");
        System.exit(0);
      }
    }
    System.out.println("order number: " + this.orderNumber);
  }

  private void parseShipTo() throws IOException {
    String seq = body.indexOf("Shipping to:") != -1 ? "Shipping to:" : "Ship to:";
    String parseThis = body.substring(body.indexOf(seq));
    BufferedReader br = new BufferedReader(new StringReader(parseThis));
    String line = br.readLine(); // This line is Ship To:<br />
    line = br.readLine(); // First
    //line = br.readLine(); // Last
    line = br.readLine(); // Address
    line = br.readLine(); // cityStateLine
    if (line.indexOf("HAWAIIAN GARDENS") != -1) {
      System.out.println("x");
    }
    String cityStateLine = "[A-Za-z\\s]+, [A-Za-z]{2}";
    Pattern p = Pattern.compile(cityStateLine);
    Matcher m = p.matcher(line);
    if (m.matches()) {
      line = m.group();
      this.setCity(line.substring(0, line.indexOf(",")).trim());
      this.setState(line.substring(line.indexOf(",") + 2).trim());
    }
    line = br.readLine(); // zip
    String zipLine = "[0-9]{5}";
    p = Pattern.compile(zipLine);
    m = p.matcher(line);
    if (m.matches()) {
      this.setZip(m.group());
    }
  }

  private void parseOrderTotal() throws IOException {
    if (body.indexOf("Pay online$") != -1) {
      String parseThis = body.substring(body.indexOf("Pay online"));
      BufferedReader br = new BufferedReader(new StringReader(parseThis));
      String line = br.readLine();
      String totalLine = "[0-9]{1,3}.[0-9]{2}";
      Pattern p = Pattern.compile(totalLine);
      Matcher m = p.matcher(line);
      if (m.find()) {
        line = m.group();
        this.total = new BigDecimal(line.substring(0));
      }
    } else {
      String seq = "Grand Total:";
      if (body.indexOf(seq) == -1) {
        seq = "Shipment 1 Total";
      }
      String parseThis = body.substring(body.indexOf(seq));
      BufferedReader br = new BufferedReader(new StringReader(parseThis));
      String line = br.readLine(); // This line is Ship To:<br />
      line = br.readLine(); // This line is Ship To:<br />
      String totalLine = "\\$[0-9]{1,3}.[0-9]{2}";
      Pattern p = Pattern.compile(totalLine);
      Matcher m = p.matcher(line);
      if (m.matches()) {
        line = m.group();
        this.total = new BigDecimal(line.substring(1));
      }
    }
  }

  private void parseSubTotal() throws IOException {
    this.subtotal = total.subtract(shipping).subtract(tax);

  }

  private void parseTax() throws IOException {
    String line = null;
    if (body.indexOf("Sales Tax$") != -1) {
      String parseThis = body.substring(body.indexOf("Sales Tax$"));
      BufferedReader br = new BufferedReader(new StringReader(parseThis));
      line = br.readLine(); // This line is Ship To:<br />
      String totalLine = "[0-9]{1,3}.[0-9]{2}";
      Pattern p = Pattern.compile(totalLine);
      Matcher m = p.matcher(line);
      if (m.find()) {
        line = m.group();
        this.tax = new BigDecimal(line.substring(0));
      }
    } else {
      String seq = "Tax:";
      if (body.indexOf(seq) == -1) {
        seq = "Subtotal$";
      } else {
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        line = br.readLine(); // This line is Ship To:<br />
        line = br.readLine(); // This line is Ship To:<br />
      }
      String totalLine = "\\$[0-9]{1,3}.[0-9]{2}";
      Pattern p = Pattern.compile(totalLine);
      Matcher m = p.matcher(line);
      if (m.matches()) {
        line = m.group();
        this.tax = new BigDecimal(line.substring(1));
      }
    }
  }

  private void parseProduct() throws IOException {
    String line = null;
    if (body.indexOf("ItemQtyOrig") != -1) {
    String parseThis = body.substring(body.lastIndexOf("ItemQtyOrig. priceSubtotal"));
    BufferedReader br = new BufferedReader(new StringReader(parseThis));
    br.readLine();
    this.product = br.readLine();
    } else {
    String seq = "ITEM";
    String parseThis = body.substring(body.lastIndexOf(seq));
    BufferedReader br = new BufferedReader(new StringReader(parseThis));
    Pattern p = Pattern.compile("\\$[0-9]{1,3}.[0-9]{2}");
    Matcher m = null;
    while ((line = br.readLine()) != null) {
      System.out.println(line);
      m = p.matcher(line);
      if (m.find()) {
        String group = m.group();
        this.product = line.substring(0, line.indexOf(group));
        break;
      }
    }
    }
    System.out.println(line);
  }

  private void parseShipping() throws IOException {
    if (body.indexOf("Shipping costs$") != -1) {
      String parseThis = body.substring(body.indexOf("Shipping costs"));
      BufferedReader br = new BufferedReader(new StringReader(parseThis));
      String line = br.readLine();
      String totalLine = "[0-9]{1,3}.[0-9]{2}";
      Pattern p = Pattern.compile(totalLine);
      Matcher m = p.matcher(line);
      if (m.find()) {
        line = m.group();
        this.shipping = new BigDecimal(line.substring(0));
      }
    } else {
      String seq = "Shipping:";
      if (body.indexOf(seq) != -1) {
        String parseThis = body.substring(body.indexOf(seq));
        BufferedReader br = new BufferedReader(new StringReader(parseThis));
        String line = br.readLine(); // This line is Ship To:<br />
        line = br.readLine(); // This line is Ship To:<br />
        String totalLine = "\\$[0-9]{1,3}.[0-9]{2}";
        Pattern p = Pattern.compile(totalLine);
        Matcher m = p.matcher(line);
        if (m.matches()) {
          line = m.group();
          this.shipping = new BigDecimal(line.substring(1));
        }
      } else {
        this.shipping = BigDecimal.ZERO;
      }
    }
  }

  /**
   * @return the nullifyQuicken
   */
  public boolean isNullifyQuicken() {
    return nullifyQuicken;
  }

  /**
   * @param nullifyQuicken the nullifyQuicken to set
   */
  public void setNullifyQuicken(boolean nullifyQuicken) {
    this.nullifyQuicken = nullifyQuicken;
  }
}
