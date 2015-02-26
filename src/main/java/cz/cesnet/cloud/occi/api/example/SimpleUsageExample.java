package cz.cesnet.cloud.occi.api.example;

import cz.cesnet.cloud.occi.api.Client;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.api.http.HTTPClient;
import java.net.URI;
import java.util.List;

/**
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class SimpleUsageExample {

    public static void main(String[] args) {

        try {
            Client client = new HTTPClient(URI.create("http://localhost:1234"));
            client.connect();

            List<URI> list = client.list();
            System.out.println("Locations:");
            for (URI uri : list) {
                System.out.println(uri);
            }
        } catch (CommunicationException ex) {
            throw new RuntimeException(ex);
        }
    }

}
