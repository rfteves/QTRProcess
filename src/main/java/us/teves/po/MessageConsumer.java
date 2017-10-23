/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.po;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 *
 * @author RICARDO
 */
public class MessageConsumer extends Thread {

    private final List<MessageOrder> MESSAGES = new ArrayList<MessageOrder>();
    private FileOutputStream fos = null;
    private int total, order;

    public static MessageConsumer getInstance(int total) {
        MessageConsumer consumer = new MessageConsumer(total);
        consumer.start();
        return consumer;
    }

    private MessageConsumer(int total) {
        this.total = total;
        this.start = System.currentTimeMillis();
    }

    public void run() {
        try {
            fos = new FileOutputStream("./invoices.qif");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MessageConsumer.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (work || this.MESSAGES.size() > 0) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(MessageConsumer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    private boolean work = true;

    public void quit() {
        synchronized (this) {
            this.work = false;
            this.notifyAll();
        }
    }

    private int added;
    private long start;
    public void add(MessageOrder order) {
        ++added;
        System.out.println("Add message: " + order.getOrder() + ":" + total);
        synchronized (this) {
            while (MESSAGES.size() > 12) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(MessageConsumer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            MESSAGES.add(order);
            order.start();
        }
    }

    private long durations;

    public void remove(MessageOrder order) {
        float avg = added * 1000000/(System.currentTimeMillis() - start);
        System.out.println("Del message " + order.getOrder() + ":" + total + " duration:" + order.getDuration() + " avg: " + avg);
        synchronized (this) {
            while (MESSAGES.size() == 0) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(MessageConsumer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            MESSAGES.remove(order);
            if (order.getPo() != null && order.getPo().excelDelimitedFormat() != null) {
                try {
                    fos.write(order.getPo().excelDelimitedFormat().getBytes());
                } catch (IOException ex) {
                    Logger.getLogger(MessageConsumer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            this.notifyAll();
        }
    }
}
