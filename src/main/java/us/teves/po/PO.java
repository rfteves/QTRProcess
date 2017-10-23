/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.teves.po;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.mail.Message;

/**
 *
 * @author rfteves
 */
public abstract class PO {
    protected String orderNumber;
    protected Date transdate;
    protected BigDecimal total, tax, shipping, subtotal;
    protected String city, state, zip, product, subject;
    private String payee, vendor, name, address, address2, marketOrderNumber;
    private String tracking, viatype;
    private boolean nullifyPO;

    SimpleDateFormat md = new SimpleDateFormat("yyyy-MM-dd");
    StringBuilder builder = new StringBuilder();
    public String excelDelimitedFormat() {
        if (state.equalsIgnoreCase("ca") || state.equalsIgnoreCase("california")) {
            state = "CA";
        }
        builder.setLength(0);
        builder.append(vendor);
        builder.append("\t");
        builder.append(md.format(transdate));
        builder.append("\t");
        builder.append(this.orderNumber);
        builder.append("\t");
        builder.append(this.product.substring(0, Math.min(50, this.product.length())));
        builder.append("\t");
        if (this.state.equalsIgnoreCase("CA")) {
            builder.append(this.city + ", " + this.zip);
        } else {
            builder.append("Other states");
        }
        builder.append("\t");
        if (this.state.equalsIgnoreCase("CA") && this.tax.negate().doubleValue() != 0) {
            builder.append(this.total.subtract(this.tax));
            builder.append("\t");
            builder.append(this.tax);
        } else {
            builder.append(this.total);
            builder.append("\t");
        }
        builder.append("\t");
        builder.append(this.total);
        builder.append("\n");
        return builder.toString();
    }

    public abstract void extract(Message message);

    /**
     * @return the orderNumber
     */
    public String getOrderNumber() {
        return orderNumber;
    }

    /**
     * @param orderNumber the orderNumber to set
     */
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    /**
     * @return the transdate
     */
    public Date getTransdate() {
        return transdate;
    }

    /**
     * @param transdate the transdate to set
     */
    public void setTransdate(Date transdate) {
        this.transdate = transdate;
    }

    /**
     * @return the total
     */
    public BigDecimal getTotal() {
        return total;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    /**
     * @return the tax
     */
    public BigDecimal getTax() {
        return tax;
    }

    /**
     * @param tax the tax to set
     */
    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    /**
     * @return the shipping
     */
    public BigDecimal getShipping() {
        return shipping;
    }

    /**
     * @param shipping the shipping to set
     */
    public void setShipping(BigDecimal shipping) {
        this.shipping = shipping;
    }

    /**
     * @return the subtotal
     */
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    /**
     * @param subtotal the subtotal to set
     */
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the zip
     */
    public String getZip() {
        return zip;
    }

    /**
     * @param zip the zip to set
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * @return the product
     */
    public String getProduct() {
        return product;
    }

    /**
     * @param product the product to set
     */
    public void setProduct(String product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return String.format("%12s: %s\n", "SubTotal", "" + this.subtotal) +
                String.format("%12s: %s\n", "Shipping", "" + this.shipping) +
                String.format("%12s: %s\n", "Tax", "" + this.tax) +
                String.format("%12s: %s\n", "Total", "" + this.total) +
                city + ", " + state + " " + zip;
    }

    public abstract String quickenString();
    public abstract String mysqlString();

    /**
     * @return the payee
     */
    public String getPayee() {
        return payee;
    }

    /**
     * @param payee the payee to set
     */
    public void setPayee(String payee) {
        this.payee = payee;
    }

    /**
     * @return the vendor
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * @param vendor the vendor to set
     */
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the nullifyPO
     */
    public boolean isNullifyPO() {
        return nullifyPO;
    }

    /**
     * @param nullifyPO the nullifyPO to set
     */
    public void setNullifyPO(boolean nullifyPO) {
        this.nullifyPO = nullifyPO;
    }

    public String getCustomerInfo() {
        return this.payee + ":" + address + ":" + address2 + ":" + city + ":" + state + ":" + zip;
    }

    public String getOrderInfo() {
        return this.orderNumber + ":" + this.product + ":" + this.total;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the address2
     */
    public String getAddress2() {
        return address2;
    }

    /**
     * @param address2 the address2 to set
     */
    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    /**
     * @return the tracking
     */
    public String getTracking() {
        return tracking;
    }

    /**
     * @param tracking the tracking to set
     */
    public void setTracking(String tracking) {
        this.tracking = tracking;
    }

    /**
     * @return the viatype
     */
    public String getViatype() {
        return viatype;
    }

    /**
     * @param viatype the viatype to set
     */
    public void setViatype(String viatype) {
        this.viatype = viatype;
    }

    /**
     * @return the marketOrderNumber
     */
    public String getMarketOrderNumber() {
        if (marketOrderNumber == null) {
            return "";
        } else if (marketOrderNumber.length() <= 30) {
            return marketOrderNumber;
        } else {
            return marketOrderNumber.substring(0, 30);
        }
    }

    /**
     * @param marketOrderNumber the marketOrderNumber to set
     */
    public void setMarketOrderNumber(String marketOrderNumber) {
        this.marketOrderNumber = marketOrderNumber;
    }
    /**
     * Order #:                220048314
Membership #:        111767844347
Date Placed:        3/31/2011

     */
}
