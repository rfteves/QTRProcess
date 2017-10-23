/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.po;

import java.net.URI;
import java.net.URL;
import microsoft.exchange.webservices.data.AutodiscoverLocalException;
import microsoft.exchange.webservices.data.EmailMessage;
import microsoft.exchange.webservices.data.ExchangeCredentials;
import microsoft.exchange.webservices.data.ExchangeService;
import microsoft.exchange.webservices.data.ExchangeVersion;
import microsoft.exchange.webservices.data.FindItemsResults;
import microsoft.exchange.webservices.data.Folder;
import microsoft.exchange.webservices.data.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.ItemView;
import microsoft.exchange.webservices.data.Item;
import microsoft.exchange.webservices.data.MessageBody;
import microsoft.exchange.webservices.data.WebCredentials;
import microsoft.exchange.webservices.data.WellKnownFolderName;

/**
 *
 * @author rfteves
 */
public class TestEWS {

    public static void main(String[] s) throws Exception {
        System.setProperty("jcifs.http.insecureBasic", "true");
        System.setProperty("jcifs.http.enableBasic", "true");
        System.setProperty("jcifs.util.loglevel", "3");
        ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

        ExchangeCredentials credentials = new WebCredentials("",
                "");
        service.setCredentials(credentials);
        service.autodiscoverUrl("ricardo@surigao.com", new IAutodiscoverRedirectionUrl() {

            public boolean autodiscoverRedirectionUrlValidationCallback(String arg0) throws AutodiscoverLocalException {
                return true;
            }
        });
        //service.setUrl(new URI("https://ch1prd0610.outlook.com"));
        System.out.println("ok " + service.getUrl().getHost());

        /*EmailMessage msg = new EmailMessage(service);
        msg.setSubject("Hello world!");
        msg.setBody(MessageBody.getMessageBodyFromText("Sent using the EWS Managed API."));
        msg.getToRecipients().add("ricardo@teves.us");
        msg.getToRecipients().add("rfteves@yahoo.com");
        msg.send();*/



        ItemView view = new ItemView(50);
        FindItemsResults<Item> findResults = null;
        Folder inbox = Folder.bind(service, WellKnownFolderName.Inbox);
        do {
            findResults = service.findItems(WellKnownFolderName.Inbox, view);

            for (Item item : findResults.getItems()) {
                item.load();
                System.out.println(item.getSubject());
                System.out.println(item.getBody());
// Do something with the item.
            }

            view.setOffset(50);
        } while (findResults.isMoreAvailable());

    }
}
