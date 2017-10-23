/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.teves.po;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 *
 * @author rfteves
 */
public class CACounties {
    public static void main(String[]s) throws Exception {
        File f = new File("C:/Users/rfteves/Documents/EbayStuffs/californiacounties.txt");
        FileReader reader = new FileReader(f);
        File ff = new File("C:/Users/rfteves/Documents/EbayStuffs/californiacounties.txt");
        BufferedReader r = new BufferedReader(reader);
        String line = null;
        String county = null;
        String city = null;
        while ((line = r.readLine()) != null) {
            if (line.charAt(0) ==  ' ') {
                city = line;
                System.out.println("insert into ca_districts values('" + county + "','"+ city.trim() + "');");
            } else {
                county = line;
            }
        }
        r.close();
    }
}
