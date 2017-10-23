/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.teves.po;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author rfteves
 */
public class Test {

    public static void main(String[]s) throws Exception {
        System.out.println(StringEscapeUtils.escapeHtml4("Hansel &amp; gretele"));
        System.out.println(StringEscapeUtils.unescapeHtml4("Hansel &amp; gretele"));
    }

}
