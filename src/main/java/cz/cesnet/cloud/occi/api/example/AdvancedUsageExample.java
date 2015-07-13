package cz.cesnet.cloud.occi.api.example;

import cz.cesnet.cloud.occi.Model;
import cz.cesnet.cloud.occi.api.Client;
import cz.cesnet.cloud.occi.api.EntityBuilder;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.api.exception.EntityBuildingException;
import cz.cesnet.cloud.occi.api.http.HTTPClient;
import cz.cesnet.cloud.occi.api.http.auth.HTTPAuthentication;
import cz.cesnet.cloud.occi.api.http.auth.X509Authentication;
import cz.cesnet.cloud.occi.core.ActionInstance;
import cz.cesnet.cloud.occi.core.Entity;
import cz.cesnet.cloud.occi.core.Mixin;
import cz.cesnet.cloud.occi.core.Resource;
import cz.cesnet.cloud.occi.exception.AmbiguousIdentifierException;
import cz.cesnet.cloud.occi.exception.InvalidAttributeValueException;
import cz.cesnet.cloud.occi.exception.RenderingException;
import cz.cesnet.cloud.occi.infrastructure.Compute;
import cz.cesnet.cloud.occi.parser.MediaType;
import java.net.URI;
import java.util.List;

/**
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class AdvancedUsageExample {

    public static void main(String[] args) {
        try {
            HTTPAuthentication authentication = new X509Authentication("/path/to/certificate.pem", "password");
            //set custom certificates if needed
            authentication.setCAPath("/path/to/certificate/directory");
            Client client = new HTTPClient(URI.create("https://localhost:1234"), authentication, MediaType.TEXT_PLAIN, false);

            //connect client
            client.connect();

            //list all resources
            System.out.println("Listing resources...");
            List<URI> list = client.list();
            System.out.println("Locations:");
            for (URI uri : list) {
                System.out.println(uri);
            }

            //creating a compute resource
            System.out.println("Creating compute resource...");

            Model model = client.getModel();
            EntityBuilder eb = new EntityBuilder(model);

            System.out.println("Listing available os template mixins...");
            List<Mixin> mixins = model.findRelatedMixins("os_tpl");

            if (mixins.isEmpty()) {
                System.err.println("No os template mixins available. Quiting.");
                return;
            }

            Resource compute = eb.getResource("compute");
            Mixin mixin = mixins.get(0);
            System.out.println("Mixin:");
            System.out.println(mixin.toText());
            compute.addMixin(mixins.get(0));
            compute.addAttribute(Compute.ARCHITECTURE_ATTRIBUTE_NAME, "x86");
            compute.addAttribute(Compute.CORES_ATTRIBUTE_NAME, "2");
            compute.addAttribute(Compute.HOSTNAME_ATTRIBUTE_NAME, "jocci-test");
            compute.addAttribute(Compute.MEMORY_ATTRIBUTE_NAME, "2");

            URI location = client.create(compute);
            System.out.println("Created compute instance at location: '" + location + "'.");

            //describing resource
            List<Entity> entities = client.describe(location);
            System.out.println("Description:");
            System.out.println(entities.get(0).toText());

            System.out.println("Waiting for compute to become active...");
            for (int i = 0; i < 5; i++) {
                entities = client.describe(location);
                if (entities.get(0).getValue(Compute.STATE_ATTRIBUTE_NAME).equals("active")) {
                    System.out.println("Compute active.");
                    break;
                }
                System.out.println(".");
                Thread.sleep(5000);
            }

            //triggering actions
            //stopping compute
            System.out.println("Stopping previously created compute...");
            ActionInstance actionInstance = eb.getActionInstance(URI.create("http://schemas.ogf.org/occi/infrastructure/compute/action#stop"));
            boolean status = client.trigger(location, actionInstance);
            if (status) {
                System.out.println("Triggered: OK");
            } else {
                System.out.println("Triggered: FAIL");
            }

            //starting compute
            System.out.println("Starting previously created compute...");
            actionInstance = eb.getActionInstance(URI.create("http://schemas.ogf.org/occi/infrastructure/compute/action#start"));
            status = client.trigger(location, actionInstance);
            if (status) {
                System.out.println("Triggered: OK");
            } else {
                System.out.println("Triggered: FAIL");
            }

            //deleting resource
            System.out.println("Deleting previously created compute...");
            status = client.delete(location);
            if (status) {
                System.out.println("Deleted: OK");
            } else {
                System.out.println("Deleted: FAIL");
            }
        } catch (CommunicationException | AmbiguousIdentifierException | EntityBuildingException |
                InvalidAttributeValueException | RenderingException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
