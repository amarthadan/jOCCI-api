package cz.cesnet.cloud.occi.api.example;

import cz.cesnet.cloud.occi.api.Authentication;
import cz.cesnet.cloud.occi.api.Client;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.api.http.HTTPClient;
import cz.cesnet.cloud.occi.api.http.auth.DigestAuthentication;
import java.net.URI;
import java.util.List;

/**
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class DigestAuthenticationExample {

    public static void main(String[] args) {
        try {
            Authentication authentication = new DigestAuthentication("username", "password");
            Client client = new HTTPClient(URI.create("http://localhost:1234"), authentication);

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
