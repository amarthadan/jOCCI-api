package cz.cesnet.cloud.occi.api;

import cz.cesnet.cloud.occi.DataGenerator;
import cz.cesnet.cloud.occi.Model;
import cz.cesnet.cloud.occi.api.exception.EntityBuildingException;
import cz.cesnet.cloud.occi.core.Action;
import cz.cesnet.cloud.occi.core.ActionInstance;
import cz.cesnet.cloud.occi.core.Kind;
import cz.cesnet.cloud.occi.core.Link;
import cz.cesnet.cloud.occi.core.Mixin;
import cz.cesnet.cloud.occi.core.Resource;
import cz.cesnet.cloud.occi.infrastructure.Compute;
import cz.cesnet.cloud.occi.infrastructure.IPNetwork;
import cz.cesnet.cloud.occi.infrastructure.IPNetworkInterface;
import cz.cesnet.cloud.occi.infrastructure.Network;
import cz.cesnet.cloud.occi.infrastructure.NetworkInterface;
import cz.cesnet.cloud.occi.infrastructure.Storage;
import cz.cesnet.cloud.occi.infrastructure.StorageLink;
import java.net.URI;
import java.net.URISyntaxException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class EntityBuilderTest {

    private Model model;
    private EntityBuilder eb;

    @Before
    public void setUp() throws Exception {
        model = new Model();

        for (Kind kind : DataGenerator.getFiveKinds()) {
            model.addKind(kind);
        }

        for (Mixin mixin : DataGenerator.getFiveMixins()) {
            model.addMixin(mixin);
        }

        for (Action action : DataGenerator.getFiveActions()) {
            model.addAction(action);
        }

        eb = new EntityBuilder(model);
    }

    private void setUpCustom() throws URISyntaxException {
        model.addKind(DataGenerator.getCustomComputeKind());
        model.addKind(DataGenerator.getCustomNetworkKind());
        model.addKind(DataGenerator.getCustomStorageKind());
        model.addKind(DataGenerator.getCustomNetworkInterfaceKind());
        model.addKind(DataGenerator.getCustomStorageLinkKind());
        model.addMixin(DataGenerator.getCustomIPNetworkInterfaceMixin());
        model.addMixin(DataGenerator.getCustomIPNetworkMixin());
    }

    @Test
    public void testConstructor() {
        EntityBuilder eb = new EntityBuilder(model);

        assertEquals(model, eb.getModel());
    }

    @Test
    public void testInvalidConstructor() {
        try {
            new EntityBuilder(null);
            fail();
        } catch (NullPointerException ex) {
            //cool
        }
    }

    @Test
    public void testGetResourceWithString() throws Exception {
        Kind kind = DataGenerator.getFiveKinds().get(3);
        Resource resource = eb.getResource("compute");

        assertEquals(kind, resource.getKind());
        assertNotNull(resource.getId());
    }

    @Test
    public void testInvalidGetResourceWithString() throws Exception {
        try {
            eb.getResource("nonexisting");
        } catch (EntityBuildingException ex) {
            //cool
        }

        try {
            Kind k = new Kind(new URI("http://different.uri.same/term/infrastructure#"), "compute", "Compute Resource", new URI("/compute/"), null);
            model.addKind(k);
            eb.getResource("compute");
        } catch (EntityBuildingException ex) {
            //cool
        }
    }

    @Test
    public void testGetResourceWithURI() throws Exception {
        Kind kind = DataGenerator.getFiveKinds().get(3);
        Resource resource = eb.getResource(URI.create("http://schemas.ogf.org/occi/infrastructure#compute"));

        assertEquals(kind, resource.getKind());
        assertNotNull(resource.getId());
    }

    @Test
    public void testInvalidGetResourceWithURI() throws Exception {
        try {
            eb.getResource(URI.create("http://nonexisting.abc.org/icco/infrastructure#compute"));
        } catch (EntityBuildingException ex) {
            //cool
        }
    }

    @Test
    public void testGetLinkWithString() throws Exception {
        Kind kind = DataGenerator.getFiveKinds().get(4);
        Link link = eb.getLink("storagelink");

        assertEquals(kind, link.getKind());
        assertNotNull(link.getId());
    }

    @Test
    public void testInvalidGetLinkWithString() throws Exception {
        try {
            eb.getLink("nonexisting");
        } catch (EntityBuildingException ex) {
            //cool
        }

        try {
            Kind k = new Kind(new URI("http://different.uri.same/term/infrastructure#"), "storagelink", "Storage Link", new URI("/storagelink/"), null);
            model.addKind(k);
            eb.getLink("storagelink");
        } catch (EntityBuildingException ex) {
            //cool
        }
    }

    @Test
    public void testGetLinkWithURI() throws Exception {
        Kind kind = DataGenerator.getFiveKinds().get(4);
        Link link = eb.getLink(URI.create("http://schemas.ogf.org/occi/infrastructure#storagelink"));

        assertEquals(kind, link.getKind());
        assertNotNull(link.getId());
    }

    @Test
    public void testInvalidLinkWithURI() throws Exception {
        try {
            eb.getLink(URI.create("http://nonexisting.abc.org/icco/infrastructure#storagelink"));
        } catch (EntityBuildingException ex) {
            //cool
        }
    }

    @Test
    public void testGetActionInstanceWithString() throws Exception {
        Action action = DataGenerator.getFiveActions().get(2);
        ActionInstance ai = eb.getActionInstance("up");

        assertEquals(new ActionInstance(action), ai);
    }

    @Test
    public void testInvalidGetActionInstanceWithString() throws Exception {
        try {
            eb.getActionInstance("nonexisting");
        } catch (EntityBuildingException ex) {
            //cool
        }

        try {
            Action ac = new Action(new URI("http://different.uri.same/term/infrastructure/network/action#"), "up", "Activate network", null);
            model.addAction(ac);
            eb.getActionInstance("up");
        } catch (EntityBuildingException ex) {
            //cool
        }
    }

    @Test
    public void testGetActionInstanceWithURI() throws Exception {
        Action action = DataGenerator.getFiveActions().get(2);
        ActionInstance ai = eb.getActionInstance(URI.create("http://schemas.ogf.org/occi/infrastructure/network/action#up"));

        assertEquals(new ActionInstance(action), ai);
    }

    @Test
    public void testInvalidGetActionInstanceWithURI() throws Exception {
        try {
            eb.getActionInstance(URI.create("http://nonexisting.abc.org/icco/infrastructure/network/action#up"));
        } catch (EntityBuildingException ex) {
            //cool
        }
    }

    @Test
    public void testGetComputeWithURI() throws Exception {
        setUpCustom();
        Kind kind = DataGenerator.getCustomComputeKind();
        Compute compute = eb.getCompute(URI.create("http://custom.testing.org/occi/infra#compute"));

        assertEquals(kind, compute.getKind());
        assertNotNull(compute.getId());
    }

    @Test
    public void testInvalidGetComputeWithURI() throws Exception {
        try {
            eb.getCompute(URI.create("http://nonexisting.abc.org/icco/infra#compute"));
        } catch (EntityBuildingException ex) {
            //cool
        }
    }

    @Test
    public void testGetCompute() throws Exception {
        setUpCustom();
        model = new Model();
        model.addKind(Compute.getDefaultKind());

        Compute compute = eb.getCompute();
        assertEquals(Compute.getDefaultKind(), compute.getKind());
        assertNotNull(compute.getId());
    }

    @Test
    public void testGetNetworkWithURI() throws Exception {
        setUpCustom();
        Kind kind = DataGenerator.getCustomNetworkKind();
        Network network = eb.getNetwork(URI.create("http://custom.testing.org/occi/infra#network"));

        assertEquals(kind, network.getKind());
        assertNotNull(network.getId());
    }

    @Test
    public void testInvalidGetNetworkWithURI() throws Exception {
        try {
            eb.getNetwork(URI.create("http://nonexisting.abc.org/icco/infra#network"));
        } catch (EntityBuildingException ex) {
            //cool
        }
    }

    @Test
    public void testGetNetwork() throws Exception {
        setUpCustom();
        model = new Model();
        model.addKind(Network.getDefaultKind());

        Network network = eb.getNetwork();
        assertEquals(Network.getDefaultKind(), network.getKind());
        assertNotNull(network.getId());
    }

    @Test
    public void testGetStorageWithURI() throws Exception {
        setUpCustom();
        Kind kind = DataGenerator.getCustomStorageKind();
        Storage storage = eb.getStorage(URI.create("http://custom.testing.org/occi/infra#storage"));

        assertEquals(kind, storage.getKind());
        assertNotNull(storage.getId());
    }

    @Test
    public void testInvalidGetStorageWithURI() throws Exception {
        try {
            eb.getStorage(URI.create("http://nonexisting.abc.org/icco/infra#storage"));
        } catch (EntityBuildingException ex) {
            //cool
        }
    }

    @Test
    public void testGetStorage() throws Exception {
        setUpCustom();
        model = new Model();
        model.addKind(Storage.getDefaultKind());

        Storage storage = eb.getStorage();
        assertEquals(Storage.getDefaultKind(), storage.getKind());
        assertNotNull(storage.getId());
    }

    @Test
    public void testGetStorageLinkWithURI() throws Exception {
        setUpCustom();
        Kind kind = DataGenerator.getCustomStorageLinkKind();
        StorageLink storagelink = eb.getStorageLink(URI.create("http://custom.testing.org/occi/infra#storagelink"));

        assertEquals(kind, storagelink.getKind());
        assertNotNull(storagelink.getId());
    }

    @Test
    public void testInvalidGetStorageLinkWithURI() throws Exception {
        try {
            eb.getStorageLink(URI.create("http://nonexisting.abc.org/icco/infra#storagelink"));
        } catch (EntityBuildingException ex) {
            //cool
        }
    }

    @Test
    public void testGetStorageLink() throws Exception {
        setUpCustom();
        model = new Model();
        model.addKind(StorageLink.getDefaultKind());

        StorageLink storagelink = eb.getStorageLink();
        assertEquals(StorageLink.getDefaultKind(), storagelink.getKind());
        assertNotNull(storagelink.getId());
    }

    @Test
    public void testGetNetworkInterfaceWithURI() throws Exception {
        setUpCustom();
        Kind kind = DataGenerator.getCustomNetworkInterfaceKind();
        NetworkInterface networkinterface = eb.getNetworkInterface(URI.create("http://custom.testing.org/occi/infra#networkinterface"));

        assertEquals(kind, networkinterface.getKind());
        assertNotNull(networkinterface.getId());
    }

    @Test
    public void testInvalidGetNetworkInterfaceWithURI() throws Exception {
        try {
            eb.getNetworkInterface(URI.create("http://nonexisting.abc.org/icco/infra#networkinterface"));
        } catch (EntityBuildingException ex) {
            //cool
        }
    }

    @Test
    public void testGetNetworkInterface() throws Exception {
        setUpCustom();
        model = new Model();
        model.addKind(NetworkInterface.getDefaultKind());

        NetworkInterface networkinterface = eb.getNetworkInterface();
        assertEquals(NetworkInterface.getDefaultKind(), networkinterface.getKind());
        assertNotNull(networkinterface.getId());
    }

    @Test
    public void testGetIPNetworkWithURI() throws Exception {
        setUpCustom();
        Kind kind = DataGenerator.getCustomNetworkKind();
        Mixin mixin = DataGenerator.getCustomIPNetworkMixin();
        IPNetwork ipNetwork = eb.getIPNetwork(URI.create("http://custom.testing.org/occi/infra#network"), URI.create("http://custom.testing.org/occi/infra#ipnetwork"));

        assertEquals(kind, ipNetwork.getKind());
        assertEquals(mixin, ipNetwork.getMixin("http://custom.testing.org/occi/infra#ipnetwork"));
        assertNotNull(ipNetwork.getId());
    }

    @Test
    public void testInvalidGetIPNetworkWithURI() throws Exception {
        try {
            eb.getIPNetwork(URI.create("http://nonexisting.abc.org/icco/infra#network"), URI.create("http://nonexisting.abc.org/icco/infra#ipnetwork"));
        } catch (EntityBuildingException ex) {
            //cool
        }
    }

    @Test
    public void testGetIPNetwork() throws Exception {
        setUpCustom();
        model = new Model();
        model.addKind(Network.getDefaultKind());
        model.addMixin(IPNetwork.getDefaultMixin());

        IPNetwork ipNetwork = eb.getIPNetwork();
        assertEquals(Network.getDefaultKind(), ipNetwork.getKind());
        assertEquals(IPNetwork.getDefaultMixin(), ipNetwork.getMixin(IPNetwork.MIXIN_IDENTIFIER_DEFAULT));
        assertNotNull(ipNetwork.getId());
    }

    @Test
    public void testGetIPNetworkInterfaceWithURI() throws Exception {
        setUpCustom();
        Kind kind = DataGenerator.getCustomNetworkInterfaceKind();
        Mixin mixin = DataGenerator.getCustomIPNetworkInterfaceMixin();
        IPNetworkInterface iPNetworkInterface = eb.getIPNetworkInterface(URI.create("http://custom.testing.org/occi/infra#networkinterface"), URI.create("http://custom.testing.org/occi/infra#ipnetworkinterface"));

        assertEquals(kind, iPNetworkInterface.getKind());
        assertEquals(mixin, iPNetworkInterface.getMixin("http://custom.testing.org/occi/infra#ipnetworkinterface"));
        assertNotNull(iPNetworkInterface.getId());
    }

    @Test
    public void testInvalidGetIPNetworkInterfaceWithURI() throws Exception {
        try {
            eb.getIPNetworkInterface(URI.create("http://nonexisting.abc.org/icco/infra#networkinterface"), URI.create("http://nonexisting.abc.org/icco/infra#ipnetworkinterface"));
        } catch (EntityBuildingException ex) {
            //cool
        }
    }

    @Test
    public void testGetIPNetworkInterface() throws Exception {
        setUpCustom();
        model = new Model();
        model.addKind(NetworkInterface.getDefaultKind());
        model.addMixin(IPNetworkInterface.getDefaultMixin());

        System.out.println(model.getMixins());

        IPNetworkInterface iPNetworkInterface = eb.getIPNetworkInterface();
        assertEquals(NetworkInterface.getDefaultKind(), iPNetworkInterface.getKind());
        assertEquals(IPNetworkInterface.getDefaultMixin(), iPNetworkInterface.getMixin(IPNetworkInterface.MIXIN_IDENTIFIER_DEFAULT));
        assertNotNull(iPNetworkInterface.getId());
    }
}
