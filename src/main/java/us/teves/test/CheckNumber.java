/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.teves.test;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rfteves
 */
public class CheckNumber {
    public static void main(String[] s) {
        String line = "Renew Sam&apos;s Plus Renewal - 118This item is Not-ShippableNo1$55.00";
        Pattern p = Pattern.compile("\\$[0-9]{1,3}.[0-9]{2}");
        Matcher m = p.matcher(line);
        System.out.println(m.find());
    }
}
