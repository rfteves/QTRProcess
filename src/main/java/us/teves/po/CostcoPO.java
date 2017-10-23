/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.po;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;

/**
 *
 * @author rfteves
 */
public class CostcoPO extends PO {

  private String body;

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
      if (Variables.DEBUG) {
        System.out.println(body);
        System.exit(0);
      }
      String[] lines = body.split("\n");
      boolean captureShippingAddress = false, captureProduct = false, captureDatePlaced = false, captureOrderNumber = false,
        captureAddress = false, captureState = false, captureShipping = false, captureSubtotal = false, captureOrderTotal = false,
        captureTax = false;
      int itemTotal = 0;
      int first = 0;
      for (String line : lines) {
        dash = line.trim();
        if (dash.length() == 0) {
          continue;
        }
        if (line.startsWith("Order Received") && first < 2) {
          ++first;
          continue;
        } else if (first < 2) {
          continue;
        }
        if (body.contains("655532565")) {
          int k = 0;
        }
        //System.out.println("dash: " + dash);
        if (captureProduct) {
          this.product = dash;
          captureProduct = false;
        } else if (captureOrderTotal) {
          this.total = new BigDecimal(dash.substring(dash.indexOf("$") + 2).replaceAll(",", ""));
          captureOrderTotal = false;
        } else if (captureDatePlaced) {
          SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
          this.transdate = sdf.parse(dash);
          captureDatePlaced = false;
        } else if (captureAddress) {
          captureAddress = false;
          captureShippingAddress = true;
        } else if (captureShippingAddress) {
          captureShippingAddress = false;
          captureState = true;
        } else if (dash.equals("Item Total") || (itemTotal > 0 && itemTotal <= 2)) {
          if (++itemTotal == 2) {
            this.product = dash;
          }
        } else if (captureTax) {
          captureTax = false;
          this.tax = new BigDecimal(dash.substring(dash.indexOf("$") + 2).replaceAll(",", ""));
        } else if (captureState) {
          if (dash.indexOf(",") != -1 && dash.indexOf("8211") == -1) {
            captureState = false;
            String linex = dash;
            this.city = linex.substring(0, linex.indexOf(","));
            this.state = linex.substring(linex.indexOf(",") + 2, linex.indexOf(",") + 4);
            this.zip = linex.substring(linex.indexOf(",") + 5);
          }
        } else if (captureShippingAddress) {
          if (dash.startsWith("====")) {
            String linex = this.city;
            this.city = linex.substring(0, linex.indexOf(","));
            this.state = linex.substring(linex.indexOf(", ") + 2, linex.indexOf(", ") + 4);
            this.zip = linex.substring(linex.indexOf(", ") + 4);
            captureShippingAddress = false;
          } else if (dash.length() != 0) {
            this.city = dash;
          }
        } else if (dash.startsWith("Ship To")) {
          captureAddress = true;
        } else if (dash.startsWith("Tax")) {
          captureTax = true;
        } else if (dash.startsWith("Standard 3 to 5 Business Days:")) {
          captureShipping = true;
        } else if (dash.startsWith("Order #:")) {
          this.orderNumber = dash.substring(8).trim();
        } else if (captureOrderNumber) {
          this.orderNumber = dash.trim();
          captureOrderNumber = false;
        } else if (dash.startsWith("Order Number") && orderNumber == null) {
          captureOrderNumber = true;
        } else if (dash.startsWith("Order Placed") && transdate == null) {
          captureDatePlaced = true;
        } else if (dash.startsWith("Shipping Details") && product == null) {
          captureProduct = true;
        } else if (dash.startsWith("Order Total") && total == null) {
          captureOrderTotal = true;
        } else if ((dash.startsWith("Date Placed:") || dash.startsWith("Order Date")) || captureDatePlaced) {
          if (!captureDatePlaced && dash.startsWith("Date Placed:")) {
            dash = dash.substring("Date Placed:".length()).trim();
          } else if (!captureDatePlaced && dash.startsWith("Order Date")) {
            dash = dash.substring("Order Date".length()).trim();
          }
          if (dash.length() == 0) {
            captureDatePlaced = true;
          } else {
            captureDatePlaced = false;
            while (true) {
              if (dash.charAt(1) == '/') {
                dash = '0' + dash;
              } else if (dash.charAt(4) == '/') {
                dash = dash.substring(0, 3) + '0' + dash.substring(3);
              } else {
                break;
              }
            }
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            this.transdate = sdf.parse(dash);
          }
        } else if (dash.startsWith("Product:")) {
          captureProduct = true;
        } else if (dash.equals("Shipping Address")) {
          captureAddress = true;
        } else if (dash.equals("Shipping Address:")) {
          captureShippingAddress = true;
          //Shipping & Handling
        } else if (dash.startsWith("Shipping & Handling") || captureShipping) {
          if (dash.indexOf("$") != -1) {
            captureShipping = false;
            this.shipping = new BigDecimal(dash.substring(dash.indexOf("$") + 2).replaceAll(",", ""));
          } else {
            captureShipping = true;
          }
          //
        } else if (dash.startsWith("Shipping and Handling:")) {
          this.shipping = new BigDecimal(dash.substring(dash.indexOf("$") + 1));
        } else if (dash.startsWith("Tax:") || captureTax) {
          if (dash.indexOf("$") != -1) {
            captureTax = false;
            this.tax = new BigDecimal(dash.substring(dash.indexOf("$") + 1).replaceAll(",", ""));
          } else {
            captureTax = true;
          }
        } else if (dash.startsWith("Line Total:")) {
          this.subtotal = new BigDecimal(dash.substring(dash.indexOf("$") + 1));
        } else if (dash.startsWith("Subtotal:") || captureSubtotal) {
          if (dash.indexOf("$") != -1) {
            captureSubtotal = false;
            this.subtotal = new BigDecimal(dash.substring(dash.indexOf("$") + 1).replaceAll(",", ""));
          } else {
            captureSubtotal = true;
          }
        } else if (dash.startsWith("Total:")) {
          dash = dash.replace(",", "");
          this.total = new BigDecimal(dash.substring(dash.indexOf("$") + 1).replaceAll(",", ""));
          break;
        } else if ((dash.startsWith("Order Total") || captureOrderTotal) && this.subtotal != null) {
          if (dash.indexOf("$") != -1) {
            captureOrderTotal = false;
            dash = dash.replace(",", "");
            this.total = new BigDecimal(dash.substring(dash.indexOf("$") + 1).replaceAll(",", ""));
            break;
          } else {
            captureOrderTotal = true;
          }
        }
      }
    } catch (Throwable x) {
      x.printStackTrace();
      System.out.println("dash: " + dash);
      System.out.println("body: " + body);
      System.exit(0);
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
    builder.append("PCostco");
    builder.append("\n");
    if (this.state.equalsIgnoreCase("CA") && this.tax.negate().doubleValue() != 0) {
      builder.append("M" + this.city + ", " + this.zip + " tax paid " + this.tax);
      builder.append("\n");
    }
    builder.append("CSeattle, WA 98124");
    builder.append("\n");
    builder.append("LMerchandise/Ebay");
    builder.append("\n");
    builder.append("ACOSTCO.COM 9500847");
    builder.append("\n");
    builder.append("A999 LAKE DR");
    builder.append("\n");
    builder.append("AISSAQUAH, WA 98027-8990");
    builder.append("\n");
    builder.append("A");
    builder.append("\n");
    builder.append("A");
    builder.append("\n");
    builder.append("SMerchandise");
    builder.append("\n");
    builder.append("E" + this.product.substring(0, Math.min(30, this.product.length())));
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
    builder.append("XT0.0");
    builder.append("\n");
    builder.append("XN");
    builder.append("\n");
    builder.append("XS" + this.product.substring(0, Math.min(30, this.product.length())));
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
    SimpleDateFormat orderdate = new SimpleDateFormat("yyyyMMdd");
    builder.append("insert into purchases values (");
    builder.append("'costco', ");
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
    return builder.toString();
  }

  public static String stripHtml(String body) {
    return CostcoPO.stripHtml(body, false);
  }

  public static String stripHtml(String body, boolean stripBr) {
    if (body.indexOf("<!-- BEGIN EmailOrderHeader.jsp -->") != -1) {
      body = body.substring(body.indexOf("<!-- BEGIN EmailOrderHeader.jsp -->"));
    }
    int lt = 0;
    if (stripBr) {
      body = body.replaceAll("<br />", "\n").replaceAll("<br/>", "\n");
    }
    while ((lt = body.indexOf("<")) != -1) {
      int gt = body.indexOf(">", lt);
      if (lt == 0) {
        body = body.substring(gt + 1);
      } else {
        body = body.substring(0, lt) + body.substring(gt + 1);
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
}
