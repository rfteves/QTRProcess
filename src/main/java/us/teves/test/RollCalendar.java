/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.teves.test;

import java.util.Calendar;

/**
 *
 * @author rfteves
 */
public class RollCalendar {
    public static void main(String[]s) {
        Calendar ca = Calendar.getInstance();
        int roll = 0;
        while (++roll < 400) {
            ca.add(Calendar.DAY_OF_MONTH, -1);
            System.out.println(ca.getTime());
        }
    }
}
