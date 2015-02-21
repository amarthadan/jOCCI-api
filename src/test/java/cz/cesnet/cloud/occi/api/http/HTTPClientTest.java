package cz.cesnet.cloud.occi.api.http;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import cz.cesnet.cloud.occi.Model;
import cz.cesnet.cloud.occi.api.EntityBuilder;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.api.http.auth.BasicAuthentication;
import cz.cesnet.cloud.occi.api.http.auth.NoAuthentication;
import cz.cesnet.cloud.occi.core.ActionInstance;
import cz.cesnet.cloud.occi.core.Attribute;
import cz.cesnet.cloud.occi.core.Entity;
import cz.cesnet.cloud.occi.core.Kind;
import cz.cesnet.cloud.occi.core.Link;
import cz.cesnet.cloud.occi.core.Mixin;
import cz.cesnet.cloud.occi.core.Resource;
import cz.cesnet.cloud.occi.infrastructure.Compute;
import cz.cesnet.cloud.occi.infrastructure.Network;
import cz.cesnet.cloud.occi.infrastructure.NetworkInterface;
import cz.cesnet.cloud.occi.infrastructure.Storage;
import cz.cesnet.cloud.occi.infrastructure.enumeration.Architecture;
import cz.cesnet.cloud.occi.infrastructure.enumeration.ComputeState;
import cz.cesnet.cloud.occi.infrastructure.enumeration.NetworkState;
import cz.cesnet.cloud.occi.infrastructure.enumeration.StorageState;
import cz.cesnet.cloud.occi.parser.MediaType;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;

/**
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class HTTPClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8123);

    private HTTPClient client;

    @Before
    public void setUp() throws Exception {
        client = new HTTPClient(URI.create("http://localhost:8123"), null, MediaType.TEXT_PLAIN, false);
    }

    @Test
    public void testFullConstructor() throws Exception {
        HTTPClient client = new HTTPClient(URI.create("http://localhost:8123"), null, MediaType.TEXT_PLAIN, true);

        assertEquals(client.getMediaType(), "text/plain");
        assertTrue(client.getAuthentication() instanceof NoAuthentication);
        assertTrue(client.isConnected());

        client = new HTTPClient(URI.create("http://localhost:8123"), new BasicAuthentication("username", "password"), MediaType.TEXT_OCCI, false);

        assertEquals(client.getMediaType(), MediaType.TEXT_OCCI);
        assertTrue(client.getAuthentication() instanceof BasicAuthentication);
        assertFalse(client.isConnected());
    }

    @Test
    public void testPartialConstructor() throws Exception {
        HTTPClient client = new HTTPClient(URI.create("http://localhost:8123"), new NoAuthentication());

        assertEquals(client.getMediaType(), MediaType.TEXT_PLAIN);
        assertTrue(client.getAuthentication() instanceof NoAuthentication);
        assertTrue(client.isConnected());
    }

    @Test
    public void testMinimalConstructor() throws Exception {
        HTTPClient client = new HTTPClient(URI.create("http://localhost:8123"));

        assertEquals(client.getMediaType(), MediaType.TEXT_PLAIN);
        assertTrue(client.getAuthentication() instanceof NoAuthentication);
        assertFalse(client.isConnected());
    }

    @Test
    public void testSetMediaType() {
        client.setMediaType("xyz/uvw");
        assertEquals(client.getMediaType(), "xyz/uvw");
    }

    @Test
    public void testConnect() throws Exception {
        client.connect();

        assertTrue(client.isConnected());
    }

    @Test
    public void testList() throws Exception {
        List<URI> list = listOfAll();
        client.connect();

        assertEquals(list, client.list());
        client.setMediaType(MediaType.TEXT_OCCI);
        assertEquals(list, client.list());
    }

    @Test
    public void testListWithString() throws Exception {
        List<URI> list = listOfComputes();
        client.connect();

        assertEquals(list, client.list("compute"));
        client.setMediaType(MediaType.TEXT_OCCI);
        assertEquals(list, client.list("compute"));
    }

    @Test
    public void testInvalidListWithString() throws Exception {
        client.connect();
        try {
            client.list("unknown");
        } catch (CommunicationException ex) {
            //cool
        }

        try {
            Kind kind = new Kind(URI.create("http://different.uri.same/term/infrastructure#"), "compute");
            client.getModel().addKind(kind);
            client.list("compute");
        } catch (CommunicationException ex) {
            //cool
        }
    }

    @Test
    public void testListWithURI() throws Exception {
        List<URI> list = listOfComputes();
        client.connect();

        assertEquals(list, client.list(URI.create("http://schemas.ogf.org/occi/infrastructure#compute")));
        client.setMediaType(MediaType.TEXT_OCCI);
        assertEquals(list, client.list(URI.create("http://schemas.ogf.org/occi/infrastructure#compute")));
    }

    @Test
    public void testInvalidListWithURI() throws Exception {
        client.connect();
        try {
            client.list(URI.create("http://nonexisting.abc.org/icco/infrastructure#compute"));
        } catch (CommunicationException ex) {
            //cool
        }
    }

    private List<URI> listOfComputes() {
        List<URI> list = new ArrayList<>();
        list.add(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/compute/0054b25a-ddb9-412e-869e-7b800a13aa46"));
        list.add(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/compute/29ce3084-23b6-44e0-b53e-55a34b924920"));
        list.add(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/compute/987654321"));
        list.add(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/compute/9b36c234-7e4a-400d-bab8-58dead9e0ef8"));
        list.add(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/compute/29b814ad-c5b2-4bc4-888b-470f769a2930"));
        list.add(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/compute/123456789"));

        return list;
    }

    private List<URI> listOfNetworks() {
        List<URI> list = new ArrayList<>();
        list.add(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/network/05940332-7926-4cf5-b1fc-7479b529524a"));
        list.add(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/network/1bdff9e2-7a5d-4e87-b2e3-9a6cfb7b6619"));
        list.add(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/network/24b94558-c46a-41e3-981d-16600f71cddb"));

        return list;
    }

    private List<URI> listOfStorages() {
        List<URI> list = new ArrayList<>();
        list.add(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/storage/8f423fd4-0fdb-4422-a01b-fb6594173fbb"));
        list.add(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/storage/1902326a-2092-4cb6-b998-6d6e73be6212"));
        list.add(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/storage/a7eeebf0-a93f-4187-bd86-dab2725d5bfa"));

        return list;
    }

    private List<URI> listOfAll() {
        List<URI> list = listOfComputes();
        list.addAll(listOfNetworks());
        list.addAll(listOfStorages());

        return list;
    }

    @Test
    public void testDescribe() throws Exception {
        Set<Entity> expectedSet = new HashSet<>(descriptionOfAll());
        client.connect();

        Set<Entity> clientSet = new HashSet<>(client.describe());
        assertEquals(expectedSet, clientSet);
        client.setMediaType(MediaType.TEXT_OCCI);
        assertEquals(expectedSet, clientSet);

    }

    @Test
    public void testDescribeWithString() throws Exception {
        Set<Entity> expectedSet = new HashSet<>(descriptionOfComputes());
        client.connect();

        Set<Entity> clientSet = new HashSet<>(client.describe("compute"));
        assertEquals(expectedSet, clientSet);
        client.setMediaType(MediaType.TEXT_OCCI);
        assertEquals(expectedSet, clientSet);
    }

    @Test
    public void testInvalidDescribeWithString() throws Exception {
        client.connect();
        try {
            client.describe("unknown");
        } catch (CommunicationException ex) {
            //cool
        }

        try {
            Kind kind = new Kind(URI.create("http://different.uri.same/term/infrastructure#"), "compute");
            client.getModel().addKind(kind);
            client.describe("compute");
        } catch (CommunicationException ex) {
            //cool
        }
    }

    @Test
    public void testDescribeWithURI() throws Exception {
        Set<Entity> expectedSet = new HashSet<>(descriptionOfComputes());
        client.connect();

        Set<Entity> clientSet = new HashSet<>(client.describe(URI.create("http://schemas.ogf.org/occi/infrastructure#compute")));
        assertEquals(expectedSet, clientSet);
        client.setMediaType(MediaType.TEXT_OCCI);
        assertEquals(expectedSet, clientSet);

        client.setMediaType(MediaType.TEXT_PLAIN);
        expectedSet = new HashSet<>(descriptionOfSpecificCompute());
        clientSet = new HashSet<>(client.describe(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/compute/9b36c234-7e4a-400d-bab8-58dead9e0ef8")));
        assertEquals(expectedSet, clientSet);
        client.setMediaType(MediaType.TEXT_OCCI);
        assertEquals(expectedSet, clientSet);
    }

    @Test
    public void testInvalidDescribeWithURI() throws Exception {
        client.connect();
        try {
            client.describe(URI.create("http://nonexisting.abc.org/icco/infrastructure#compute"));
        } catch (CommunicationException ex) {
            //cool
        }

        try {
            client.describe(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/compute/nonexistent-id"));
        } catch (CommunicationException ex) {
            //cool
        }
    }

    private List<Entity> descriptionOfComputes() throws Exception {
        List<Entity> entities = new ArrayList<>();
        List<Attribute> computeAttributes = new ArrayList<>();
        computeAttributes.add(new Attribute(Compute.ID_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.TITLE_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.SUMMARY_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.ARCHITECTURE_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.CORES_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.HOSTNAME_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.MEMORY_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.SPEED_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.STATE_ATTRIBUTE_NAME));
        Kind compute = new Kind(URI.create("http://schemas.ogf.org/occi/infrastructure#"), "compute", "compute resource", URI.create("/compute/"), computeAttributes);
        Mixin debian6 = new Mixin(URI.create("http://occi.example.org/occi/infrastructure/os_tpl#"), "debian6", "debian", URI.create("/mixin/os_tpl/debian6/"), null);
        Mixin small = new Mixin(URI.create("http://occi.example.org/occi/infrastructure/resource_tpl#"), "small", "Small Instance - 1 core and 2 GB RAM", URI.create("/mixin/resource_tpl/small/"), null);
        Mixin sl6golden = new Mixin(URI.create("http://occi.example.org/occi/infrastructure/os_tpl#"), "sl6golden", "monitoring", URI.create("/mixin/os_tpl/sl6golden/"), null);
        Mixin mammoth = new Mixin(URI.create("http://occi.example.org/occi/infrastructure/resource_tpl#"), "mammoth", "Mammoth Instance - 8 cores and 32 GB RAM", URI.create("/mixin/resource_tpl/mammoth/"), null);
        Kind networkinterface = new Kind(URI.create("http://schemas.ogf.org/occi/infrastructure#"), "networkinterface");

        Compute c = new Compute("0054b25a-ddb9-412e-869e-7b800a13aa46", compute);
        c.setTitle("test_title");
        c.setArchitecture(Architecture.X_86);
        c.setCores(1);
        c.setMemory(2);
        c.setSpeed(1);
        c.setState(ComputeState.ACTIVE);
        c.addAttribute("eu.egi.fedcloud.appdb.uuid", "appdb:uuid:debian6");
        c.addMixin(debian6);
        c.addMixin(small);
        entities.add(c);

        c = new Compute("29ce3084-23b6-44e0-b53e-55a34b924920", compute);
        c.setTitle("fhkgf");
        c.setArchitecture(Architecture.X_86);
        c.setCores(8);
        c.setMemory(32);
        c.setSpeed(1);
        c.setState(ComputeState.ACTIVE);
        c.addAttribute("eu.egi.fedcloud.appdb.uuid", "appdb:uuid:sl6golden");
        c.addMixin(sl6golden);
        c.addMixin(mammoth);
        entities.add(c);

        Link link = new Link("e5f8f7bd-7d84-4c46-9a4e-325cc950f0ed", networkinterface);
        link.setTarget("http://rocci-server-1-1-x.herokuapp.com:80/network/e36ee51c-bfb6-4264-a5bf-2e71b9145b14");
        link.setRelation("http://schemas.ogf.org/occi/core#link");
        link.setSource("http://rocci-server-1-1-x.herokuapp.com:80/compute/987654321");

        c = new Compute("987654321", compute);
        c.setTitle("vm_test02");
        c.setArchitecture(Architecture.X_86);
        c.setCores(1);
        c.setMemory(2);
        c.setSpeed(1);
        c.setState(ComputeState.ACTIVE);
        c.addMixin(debian6);
        c.addMixin(small);
        c.addLink(link);
        entities.add(c);

        link = new Link("31f185d5-9379-4479-9809-b4cce6bcfdee", networkinterface);
        link.setTarget("/network/e36ee51c-bfb6-4264-a5bf-2e71b9145b14");
        link.setRelation("http://schemas.ogf.org/occi/core#link");
        link.setSource("/compute/9b36c234-7e4a-400d-bab8-58dead9e0ef8");
        link.addAttribute(NetworkInterface.INTERFACE_ATTRIBUTE_NAME, "eth0");
        link.addAttribute(NetworkInterface.MAC_ATTRIBUTE_NAME, "00:11:22:33:44:55");
        link.addAttribute(NetworkInterface.STATE_ATTRIBUTE_NAME, NetworkState.ACTIVE.toString());

        c = new Compute("9b36c234-7e4a-400d-bab8-58dead9e0ef8", compute);
        c.setTitle("VMTest");
        c.setArchitecture(Architecture.X_86);
        c.setCores(1);
        c.setMemory(2);
        c.setSpeed(1);
        c.setState(ComputeState.ACTIVE);
        c.addMixin(debian6);
        c.addMixin(small);
        c.addLink(link);
        entities.add(c);

        link = new Link("920ad837-1fa3-40a2-8810-fb8f2dc1wrt7", networkinterface);
        link.setTarget("/network/e36ee51c-bfb6-4264-a5bf-2e71b9145b14");
        link.setRelation("http://schemas.ogf.org/occi/core#link");
        link.setSource("/compute/29b814ad-c5b2-4bc4-888b-470f769a2930");

        c = new Compute("29b814ad-c5b2-4bc4-888b-470f769a2930", compute);
        c.setTitle("VMTest2");
        c.setArchitecture(Architecture.X_86);
        c.setCores(1);
        c.setMemory(2);
        c.setSpeed(1);
        c.setState(ComputeState.ACTIVE);
        c.addMixin(debian6);
        c.addMixin(small);
        c.addLink(link);
        entities.add(c);

        link = new Link("9e9aced1-a2a8-459e-8fc8-690beb3f1533", networkinterface);
        link.setTarget("/network/e36ee51c-bfb6-4264-a5bf-2e71b9145b14");
        link.setRelation("http://schemas.ogf.org/occi/core#link");
        link.setSource("/compute/123456789");
        link.addAttribute(NetworkInterface.INTERFACE_ATTRIBUTE_NAME, "eth0");
        link.addAttribute(NetworkInterface.MAC_ATTRIBUTE_NAME, "00:11:22:33:44:55");
        link.addAttribute(NetworkInterface.STATE_ATTRIBUTE_NAME, NetworkState.ACTIVE.toString());

        c = new Compute("123456789", compute);
        c.setTitle("vm_test01");
        c.setArchitecture(Architecture.X_86);
        c.setCores(1);
        c.setMemory(2);
        c.setSpeed(1);
        c.setState(ComputeState.ACTIVE);
        c.addMixin(debian6);
        c.addMixin(small);
        c.addLink(link);
        entities.add(c);

        return entities;
    }

    private List<Entity> descriptionOfSpecificCompute() throws Exception {
        List<Entity> entities = new ArrayList<>();
        List<Attribute> computeAttributes = new ArrayList<>();
        computeAttributes.add(new Attribute(Compute.ID_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.TITLE_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.SUMMARY_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.ARCHITECTURE_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.CORES_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.HOSTNAME_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.MEMORY_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.SPEED_ATTRIBUTE_NAME));
        computeAttributes.add(new Attribute(Compute.STATE_ATTRIBUTE_NAME));
        Kind compute = new Kind(URI.create("http://schemas.ogf.org/occi/infrastructure#"), "compute", "compute resource", URI.create("/compute/"), computeAttributes);
        Mixin debian6 = new Mixin(URI.create("http://occi.example.org/occi/infrastructure/os_tpl#"), "debian6", "debian", URI.create("/mixin/os_tpl/debian6/"), null);
        Mixin small = new Mixin(URI.create("http://occi.example.org/occi/infrastructure/resource_tpl#"), "small", "Small Instance - 1 core and 2 GB RAM", URI.create("/mixin/resource_tpl/small/"), null);
        Kind networkinterface = new Kind(URI.create("http://schemas.ogf.org/occi/infrastructure#"), "networkinterface");

        Link link = new Link("31f185d5-9379-4479-9809-b4cce6bcfdee", networkinterface);
        link.setTarget("/network/e36ee51c-bfb6-4264-a5bf-2e71b9145b14");
        link.setRelation("http://schemas.ogf.org/occi/core#link");
        link.setSource("/compute/9b36c234-7e4a-400d-bab8-58dead9e0ef8");
        link.addAttribute(NetworkInterface.INTERFACE_ATTRIBUTE_NAME, "eth0");
        link.addAttribute(NetworkInterface.MAC_ATTRIBUTE_NAME, "00:11:22:33:44:55");
        link.addAttribute(NetworkInterface.STATE_ATTRIBUTE_NAME, NetworkState.ACTIVE.toString());

        Compute c = new Compute("9b36c234-7e4a-400d-bab8-58dead9e0ef8", compute);
        c.setTitle("VMTest");
        c.setArchitecture(Architecture.X_86);
        c.setCores(1);
        c.setMemory(2);
        c.setSpeed(1);
        c.setState(ComputeState.ACTIVE);
        c.addMixin(debian6);
        c.addMixin(small);
        c.addLink(link);
        entities.add(c);

        return entities;
    }

    private List<Entity> descriptionOfNetworks() throws Exception {
        List<Entity> entities = new ArrayList<>();
        List<Attribute> networkAttributes = new ArrayList<>();
        networkAttributes.add(new Attribute(Network.ID_ATTRIBUTE_NAME));
        networkAttributes.add(new Attribute(Network.STATE_ATTRIBUTE_NAME));
        Kind network = new Kind(URI.create("http://schemas.ogf.org/occi/infrastructure#"), "network", "network resource", URI.create("/network/"), networkAttributes);

        Network n = new Network("05940332-7926-4cf5-b1fc-7479b529524a", network);
        n.setState(NetworkState.INACTIVE);
        entities.add(n);

        n = new Network("1bdff9e2-7a5d-4e87-b2e3-9a6cfb7b6619", network);
        n.setState(NetworkState.INACTIVE);
        entities.add(n);

        n = new Network("24b94558-c46a-41e3-981d-16600f71cddb", network);
        n.setState(NetworkState.INACTIVE);
        entities.add(n);

        return entities;
    }

    private List<Entity> descriptionOfStorages() throws Exception {
        List<Entity> entities = new ArrayList<>();
        List<Attribute> storageAttributes = new ArrayList<>();
        storageAttributes.add(new Attribute(Storage.ID_ATTRIBUTE_NAME));
        storageAttributes.add(new Attribute(Storage.STATE_ATTRIBUTE_NAME));
        Kind storage = new Kind(URI.create("http://schemas.ogf.org/occi/infrastructure#"), "storage", "storage resource", URI.create("/storage/"), storageAttributes);

        Storage s = new Storage("1902326a-2092-4cb6-b998-6d6e73be6212", storage);
        s.setState(StorageState.OFFLINE);
        entities.add(s);

        s = new Storage("8f423fd4-0fdb-4422-a01b-fb6594173fbb", storage);
        s.setState(StorageState.OFFLINE);
        entities.add(s);

        s = new Storage("a7eeebf0-a93f-4187-bd86-dab2725d5bfa", storage);
        s.setState(StorageState.OFFLINE);
        entities.add(s);

        return entities;
    }

    private List<Entity> descriptionOfAll() throws Exception {
        List<Entity> entities = descriptionOfComputes();
        entities.addAll(descriptionOfNetworks());
        entities.addAll(descriptionOfStorages());

        return entities;
    }

    @Test
    public void testCreate() throws Exception {
        client.connect();
        Model model = client.getModel();
        EntityBuilder eb = new EntityBuilder(model);
        Resource r = eb.getResource("compute");
        r.setId("157754bb-af01-40be-853a-6a1f1b5ac500");
        r.addMixin(model.findMixin("debian6", "os_tpl"));
        r.addMixin(model.findMixin("small"));

        assertEquals(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/compute/157754bb-af01-40be-853a-6a1f1b5ac500"), client.create(r));
        client.setMediaType(MediaType.TEXT_OCCI);
        r.setId("5537b49a-bb2e-4302-bf8b-da38611247ca");
        assertEquals(URI.create("http://rocci-server-1-1-x.herokuapp.com/compute/5537b49a-bb2e-4302-bf8b-da38611247ca"), client.create(r));
    }

    @Test
    public void testDeleteWithString() throws Exception {
        client.connect();

        assertTrue(client.delete("network"));
        client.setMediaType(MediaType.TEXT_OCCI);
        assertTrue(client.delete("network"));
    }

    @Test
    public void testInvalidDeleteWithString() throws Exception {
        client.connect();
        try {
            client.delete("unknown");
        } catch (CommunicationException ex) {
            //cool
        }

        try {
            Kind kind = new Kind(URI.create("http://different.uri.same/term/infrastructure#"), "network");
            client.getModel().addKind(kind);
            client.delete("network");
        } catch (CommunicationException ex) {
            //cool
        }
    }

    @Test
    public void testDeleteWithURI() throws Exception {
        client.connect();

        assertTrue(client.delete(URI.create("http://schemas.ogf.org/occi/infrastructure#storage")));
        client.setMediaType(MediaType.TEXT_OCCI);
        assertTrue(client.delete(URI.create("http://schemas.ogf.org/occi/infrastructure#storage")));

        client.setMediaType(MediaType.TEXT_PLAIN);
        assertTrue(client.delete(URI.create("http://rocci-server-1-1-x.herokuapp.com/compute/157754bb-af01-40be-853a-6a1f1b5ac500")));
        client.setMediaType(MediaType.TEXT_OCCI);
        assertTrue(client.delete(URI.create("http://rocci-server-1-1-x.herokuapp.com/compute/5537b49a-bb2e-4302-bf8b-da38611247ca")));
    }

    @Test
    public void testInvalidDeleteWithURI() throws Exception {
        client.connect();
        try {
            client.delete(URI.create("http://nonexisting.abc.org/icco/infrastructure#network"));
        } catch (CommunicationException ex) {
            //cool
        }

        try {
            client.delete(URI.create("http://rocci-server-1-1-x.herokuapp.com/compute/nonexisting-id"));
        } catch (CommunicationException ex) {
            //cool
        }
    }

    @Test
    public void testTriggerWithStringAndActionInstance() throws Exception {
        client.connect();
        Model model = client.getModel();
        EntityBuilder eb = new EntityBuilder(model);
        ActionInstance a = eb.getActionInstance("start");

        assertTrue(client.trigger("compute", a));
        client.setMediaType(MediaType.TEXT_OCCI);
        assertTrue(client.trigger("compute", a));
    }

    @Test
    public void testInvalidTriggerWithStringAndActionInstance() throws Exception {
        client.connect();
        Model model = client.getModel();
        EntityBuilder eb = new EntityBuilder(model);
        ActionInstance a = eb.getActionInstance("start");
        try {
            client.trigger("unknown", a);
        } catch (CommunicationException ex) {
            //cool
        }

        try {
            Kind kind = new Kind(URI.create("http://different.uri.same/term/infrastructure#"), "compute");
            model.addKind(kind);
            client.trigger("compute", a);
        } catch (CommunicationException ex) {
            //cool
        }
    }

    @Test
    public void testTriggerWithURIAndActionInstance() throws Exception {
        client.connect();
        Model model = client.getModel();
        EntityBuilder eb = new EntityBuilder(model);
        ActionInstance a = eb.getActionInstance("start");

        assertTrue(client.trigger(URI.create("http://schemas.ogf.org/occi/infrastructure#compute"), a));
        client.setMediaType(MediaType.TEXT_OCCI);
        assertTrue(client.trigger(URI.create("http://schemas.ogf.org/occi/infrastructure#compute"), a));

        client.setMediaType(MediaType.TEXT_PLAIN);
        assertTrue(client.trigger(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/compute/29b814ad-c5b2-4bc4-888b-470f769a2930"), a));
        client.setMediaType(MediaType.TEXT_OCCI);
        assertTrue(client.trigger(URI.create("http://rocci-server-1-1-x.herokuapp.com:80/compute/29b814ad-c5b2-4bc4-888b-470f769a2930"), a));
    }

    @Test
    public void testInvalidTriggerWithURIAndActionInstance() throws Exception {
        client.connect();
        Model model = client.getModel();
        EntityBuilder eb = new EntityBuilder(model);
        ActionInstance a = eb.getActionInstance("start");
        try {
            client.trigger(URI.create("http://rocci-server-1-1-x.herokuapp.com/compute/nonexisting-id"), a);
        } catch (CommunicationException ex) {
            //cool
        }

        try {
            client.trigger(URI.create("http://nonexisting.abc.org/icco/infrastructure#network"), a);
        } catch (CommunicationException ex) {
            //cool
        }
    }

    @Test
    public void testRefresh() throws Exception {
        client.connect();
        Model model = client.getModel();
        Kind kind = new Kind(URI.create("http://different.uri.same/term/infrastructure#"), "network");
        model.addKind(kind);

        client.refresh();
        assertFalse(model.equals(client.getModel()));
    }
}
