/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.teves.po;

/**
 *
 * @author rfteves
 */

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2012</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Monitor extends JDialog {
    JPanel panel1 = new JPanel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JLabel user = new JLabel();
    private JLabel subject = new JLabel();
    private JLabel total = new JLabel();



    public Monitor(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            this.user.setText("USER NAME");
            this.subject.setText("SUBJECT HERE");
            this.total.setText("TOTAL HERE");
            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public Monitor() {
        this(new Frame(), "Monitor", false);
    }

    private void jbInit() throws Exception {
        panel1.setLayout(gridBagLayout1);
        user.setText("jLabel1");
        subject.setText("jLabel2");
        total.setText("jLabel1");
        getContentPane().add(panel1);
        panel1.add(total, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.NONE,
                                                 new Insets(10, 10, 10, 10), 0,
                                                 0));
        panel1.add(subject, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(10, 10, 10, 10), 0, 0));
        panel1.add(user, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(10, 10, 10, 10), 0,
                                                0));
    }

    /**
     * @return the user
     */
    public JLabel getUser() {
        return user;
    }

    /**
     * @return the subject
     */
    public JLabel getSubject() {
        return subject;
    }

    /**
     * @return the total
     */
    public JLabel getTotal() {
        return total;
    }
}
