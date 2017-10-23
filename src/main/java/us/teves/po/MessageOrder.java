/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.teves.po;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 *
 * @author RICARDO
 */
public class MessageOrder extends Thread {
    private Message message;
    private MessageConsumer consumer;
    private long duration;
    private PO po = null;
    private int order;
    private long start;
    
    public static MessageOrder getInstance(MessageConsumer consumer, Message message, int order) {
        return new MessageOrder(consumer, message, order);
    }

    private MessageOrder(MessageConsumer consumer, Message message, int order) {
        this.message = message;
        this.consumer = consumer;
        this.order = order;
    }

    public void run() {
        start = System.currentTimeMillis();
        try {
            String subject = null;
            try {
                subject = message.getSubject();
                System.out.println(message.getMessageNumber() + " subject: " + subject);
            } catch (Exception e) {
                System.out.println(message.getMessageNumber() + " no subject");
            }
            if (subject == null) {
            } else if (subject.toLowerCase().indexOf("your costco.com order") != -1 &&
              subject.toLowerCase().indexOf("was received") != -1) {
                po = new CostcoPO();
                po.setVendor("costco");
                po.extract(message);
            } else if (subject.toLowerCase().indexOf("Confirmation".toLowerCase()) != -1 && message.getFrom()[0].toString().endsWith("bjs.com")) {
                po = new BjsPO();
                po.setVendor("bjs");
                po.extract(message);
            } else if ((subject.toLowerCase().equalsIgnoreCase("Your SamsClub.com order") || subject.equalsIgnoreCase("SamsClub.com order confirmation")) && subject.toLowerCase().indexOf("canceled") == -1) {
                po = new SamsClub(message.getReceivedDate());
                po.setSubject(subject.toLowerCase());
                po.setVendor("samsclub");
                po.extract(message);
            } else if (subject.toLowerCase().startsWith("Your Keurig".toLowerCase())) {
                po = new KeurigPO();
                po.setVendor("keurig");
                po.extract(message);
            } else if (subject.toLowerCase().startsWith("Your order has shipped from Green Mountain Coffee".toLowerCase())) {
                po = new GreenMountain();
                po.setVendor("keurig");
                po.extract(message);
            } else if (message.getFrom()[0].toString().toLowerCase().indexOf("@keurig.com") != -1 && subject.toLowerCase().indexOf("shipped") == -1) {
                po = new KeurigPO();
                if (subject.toLowerCase().indexOf("shipped") != -1) {
                    //po.setVendor("keurigshipping");
                    po = null;
                } else {
                    //if (subject.toLowerCase().indexOf("got your order") != -1 || subject.toLowerCase().indexOf("order confirmation") != -1) {
                    po.setVendor("keurigorder");
                }
                po.extract(message);
            } else if (subject.toLowerCase().indexOf("ULINE Confirmation - Order#".toLowerCase()) != -1) {
                po = new ULine();
                po.setVendor("ULine");
                po.extract(message);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(MessageOrder.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            duration = System.currentTimeMillis() - start;
            consumer.remove(this);
        }
    }

    /**
     * @return the po
     */
    public PO getPo() {
        return po;
    }

    /**
     * @return the message
     */
    public Message getMessage() {
        return message;
    }

    /**
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }
}
