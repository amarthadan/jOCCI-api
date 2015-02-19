package cz.cesnet.cloud.occi;

import cz.cesnet.cloud.occi.core.Action;
import cz.cesnet.cloud.occi.core.ActionInstance;
import cz.cesnet.cloud.occi.core.Attribute;
import cz.cesnet.cloud.occi.core.Entity;
import cz.cesnet.cloud.occi.core.Kind;
import cz.cesnet.cloud.occi.core.Link;
import cz.cesnet.cloud.occi.core.Mixin;
import cz.cesnet.cloud.occi.core.Resource;
import cz.cesnet.cloud.occi.exception.InvalidAttributeValueException;
import cz.cesnet.cloud.occi.infrastructure.Compute;
import cz.cesnet.cloud.occi.infrastructure.NetworkInterface;
import cz.cesnet.cloud.occi.infrastructure.StorageLink;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataGenerator {

    public static List<Kind> getMinimalKind() throws URISyntaxException {
        List<Kind> kinds = new ArrayList<>();
        Kind kind = new Kind(new URI("http://schemas.ogf.org/occi/core#"), "entity");
        kind.setLocation(new URI("/entity/"));
        kinds.add(kind);

        return kinds;
    }

    public static List<Kind> getFiveKinds() throws URISyntaxException {
        Set<Attribute> attributes = new HashSet<>();
        List<Kind> kinds = new ArrayList<>();

        Attribute a = new Attribute("occi.core.id");
        attributes.add(a);
        a = new Attribute("occi.core.title");
        attributes.add(a);
        Kind entity = new Kind(new URI("http://schemas.ogf.org/occi/core#"), "entity", "Entity", new URI("/entity/"), attributes);
        kinds.add(entity);

        attributes.clear();
        a = new Attribute("occi.core.summary");
        attributes.add(a);
        Kind resource = new Kind(new URI("http://schemas.ogf.org/occi/core#"), "resource", "Resource", new URI("/resource/"), attributes);
        resource.addRelation(entity);
        resource.setParentKind(entity);
        kinds.add(resource);

        attributes.clear();
        a = new Attribute("occi.core.target");
        attributes.add(a);
        a = new Attribute("occi.core.source");
        attributes.add(a);
        Kind link = new Kind(new URI("http://schemas.ogf.org/occi/core#"), "link", "Link", new URI("/link/"), attributes);
        link.addRelation(entity);
        link.setParentKind(entity);
        kinds.add(link);

        attributes.clear();
        a = new Attribute("occi.compute.architecture", false, true);
        attributes.add(a);
        a = new Attribute("occi.compute.cores");
        attributes.add(a);
        a = new Attribute("occi.compute.hostname");
        attributes.add(a);
        a = new Attribute("occi.compute.speed");
        attributes.add(a);
        a = new Attribute("occi.compute.memory");
        attributes.add(a);
        a = new Attribute("occi.compute.state");
        attributes.add(a);
        Kind k = new Kind(new URI("http://schemas.ogf.org/occi/infrastructure#"), "compute", "Compute Resource", new URI("/compute/"), attributes);
        Action ac = new Action(new URI("http://schemas.ogf.org/occi/infrastructure/compute/action#"), "start");
        k.addAction(ac);
        ac = new Action(new URI("http://schemas.ogf.org/occi/infrastructure/compute/action#"), "stop");
        k.addAction(ac);
        ac = new Action(new URI("http://schemas.ogf.org/occi/infrastructure/compute/action#"), "restart");
        k.addAction(ac);
        ac = new Action(new URI("http://schemas.ogf.org/occi/infrastructure/compute/action#"), "suspend");
        k.addAction(ac);
        k.addRelation(resource);
        k.setParentKind(resource);
        kinds.add(k);

        attributes.clear();
        a = new Attribute("occi.storagelink.deviceid", true, false);
        attributes.add(a);
        a = new Attribute("occi.storagelink.mountpoint");
        attributes.add(a);
        a = new Attribute("occi.storagelink.state", true, true);
        attributes.add(a);
        k = new Kind(new URI("http://schemas.ogf.org/occi/infrastructure#"), "storagelink", "Storage Link", new URI("/storagelink/"), attributes);
        k.addRelation(link);
        k.setParentKind(link);
        kinds.add(k);

        return kinds;
    }

    public static List<Mixin> getMinimalMixin() throws URISyntaxException {
        List<Mixin> mixins = new ArrayList<>();
        Mixin ostpl = new Mixin(new URI("http://schemas.ogf.org/occi/infrastructure#"), "os_tpl");
        ostpl.setLocation(new URI("/mixins/os_tpl/"));
        mixins.add(ostpl);

        return mixins;
    }

    public static List<Mixin> getFiveMixins() throws URISyntaxException {
        Set<Attribute> attributes = new HashSet<>();
        List<Mixin> mixins = new ArrayList<>();

        Mixin ostpl = new Mixin(new URI("http://schemas.ogf.org/occi/infrastructure#"), "os_tpl", "Operating System Template", new URI("/mixins/os_tpl/"), attributes);
        mixins.add(ostpl);

        attributes.clear();
        Attribute a = new Attribute("occi.network.address", true, false);
        attributes.add(a);
        a = new Attribute("occi.network.gateway");
        attributes.add(a);
        a = new Attribute("occi.network.allocation");
        attributes.add(a);
        a = new Attribute("occi.network.state");
        attributes.add(a);
        Mixin m = new Mixin(new URI("http://schemas.ogf.org/occi/infrastructure/network#"), "ipnetwork", "IP Network Mixin", new URI("/mixins/ipnetwork/"), attributes);
        mixins.add(m);

        attributes.clear();
        Mixin resourcetpl = new Mixin(new URI("http://schemas.ogf.org/occi/infrastructure#"), "resource_tpl", "Resource Template", new URI("/mixins/resource_tpl/"), attributes);
        mixins.add(resourcetpl);

        attributes.clear();
        a = new Attribute("occi.compute.architecture");
        attributes.add(a);
        a = new Attribute("occi.compute.cores", true, true);
        attributes.add(a);
        a = new Attribute("occi.compute.speed");
        attributes.add(a);
        a = new Attribute("occi.compute.memory", false, true);
        attributes.add(a);
        m = new Mixin(new URI("https://occi.localhost/occi/infrastructure/resource_tpl#"), "larger", "Larger Instance - 4 cores and 10 GB of RAM", new URI("/mixins/larger/"), attributes);
        m.addRelation(resourcetpl);
        mixins.add(m);

        attributes.clear();
        m = new Mixin(new URI("https://occi.localhost/occi/infrastructure/os_tpl#"), "debianvm", "debianvm", new URI("/mixins/debianvm/"), attributes);
        m.addRelation(ostpl);
        mixins.add(m);

        return mixins;
    }

    public static List<Action> getMinimalAction() throws URISyntaxException {
        List<Action> actions = new ArrayList<>();
        Action ac = new Action(new URI("http://schemas.ogf.org/occi/infrastructure/network/action#"), "up");
        actions.add(ac);

        return actions;
    }

    public static List<Action> getFiveActions() throws URISyntaxException {
        List<Action> actions = new ArrayList<>();
        Set<Attribute> attributes = new HashSet<>();

        attributes.clear();
        Attribute a = new Attribute("method");
        attributes.add(a);
        Action ac = new Action(new URI("http://schemas.ogf.org/occi/infrastructure/compute/action#"), "restart", "Restart Compute instance", attributes);
        actions.add(ac);

        attributes.clear();
        a = new Attribute("method");
        attributes.add(a);
        ac = new Action(new URI("http://schemas.ogf.org/occi/infrastructure/compute/action#"), "suspend", "Suspend Compute instance", attributes);
        actions.add(ac);

        ac = new Action(new URI("http://schemas.ogf.org/occi/infrastructure/network/action#"), "up", "Activate network", null);
        actions.add(ac);

        ac = new Action(new URI("http://schemas.ogf.org/occi/infrastructure/network/action#"), "down", "Deactivate network", null);
        actions.add(ac);

        ac = new Action(new URI("http://schemas.ogf.org/occi/infrastructure/storage/action#"), "backup", "Backup Storage", null);
        actions.add(ac);

        return actions;
    }

    public static List<URI> getLocations() throws URISyntaxException {
        List<URI> locations = new ArrayList<>();
        locations.add(new URI("http://rocci-server-1-1-x.herokuapp.com:80/compute/87f3bfc3-42d4-4474-b45c-757e55e093e9"));
        locations.add(new URI("http://rocci-server-1-1-x.herokuapp.com:80/compute/17679ebd-975f-4ea0-b42b-47405178c360"));
        locations.add(new URI("http://rocci-server-1-1-x.herokuapp.com:80/compute/509afbd3-abff-427c-9b25-7913d17e5102"));

        return locations;
    }

    public static Resource getResource() throws InvalidAttributeValueException, URISyntaxException {
        Kind k = new Kind(new URI("http://schemas.ogf.org/occi/infrastructure#"), "compute", "compute resource", new URI("/compute/"), null);
        Resource r = new Resource("87f3bfc3-42d4-4474-b45c-757e55e093e9", k);
        r.setTitle("compute1");
        r.addAttribute(Compute.ARCHITECTURE_ATTRIBUTE_NAME, "x86");
        r.addAttribute(Compute.HOSTNAME_ATTRIBUTE_NAME, "compute1.example.org");
        r.addAttribute(Compute.MEMORY_ATTRIBUTE_NAME, "1.7");
        r.addAttribute(Compute.SPEED_ATTRIBUTE_NAME, "1.0");
        r.addAttribute(Compute.STATE_ATTRIBUTE_NAME, "active");

        List<Mixin> mixins = getFiveMixins();
        for (Mixin mixin : mixins) {
            r.addMixin(mixin);
        }

        List<Link> links = getLinks();
        for (Link link : links) {
            link.setSource(r);
            r.addLink(link);
        }

        List<Action> actions = getActions();
        for (Action action : actions) {
            r.addAction(action);
        }

        return r;
    }

    public static List<Action> getActions() throws URISyntaxException {
        List<Action> actions = new ArrayList<>();
        actions.add(new Action(new URI("http://schemas.ogf.org/occi/infrastructure/compute/action#"), "start"));
        actions.add(new Action(new URI("http://schemas.ogf.org/occi/infrastructure/compute/action#"), "stop"));

        return actions;
    }

    public static List<Link> getLinks() throws URISyntaxException, InvalidAttributeValueException {
        List<Link> links = new ArrayList<>();

        Kind k = new Kind(new URI("http://schemas.ogf.org/occi/infrastructure#"), "networkinterface", null, new URI("/link/networkinterface/"), null);
        Link l = new Link("456", k);
        l.addAttribute(NetworkInterface.INTERFACE_ATTRIBUTE_NAME, "eth0");
        l.addAttribute(NetworkInterface.MAC_ATTRIBUTE_NAME, "00:11:22:33:44:55");
        l.addAttribute(NetworkInterface.STATE_ATTRIBUTE_NAME, "active");
        l.setTarget("/network/123");
        l.setRelation("http://schemas.ogf.org/occi/infrastructure#network");
        links.add(l);

        k = new Kind(new URI("http://schemas.ogf.org/occi/infrastructure#"), "storagelink", null, new URI("/link/storagelink/"), null);
        l = new Link("789", k);
        l.addAttribute(StorageLink.DEVICE_ID_ATTRIBUTE_NAME, "1234qwerty");
        l.addAttribute(StorageLink.MOUNTPOINT_ATTRIBUTE_NAME, "/mnt/somewhere/");
        l.addAttribute(StorageLink.STATE_ATTRIBUTE_NAME, "active");
        l.setTarget("/storage/852");
        l.setRelation("http://schemas.ogf.org/occi/infrastructure#storage");
        links.add(l);

        return links;
    }

    public static Link getLink() throws InvalidAttributeValueException, URISyntaxException {
        Kind k = new Kind(new URI("http://schemas.ogf.org/occi/infrastructure#"), "networkinterface", null, null, null);
        Link l = new Link("87f3bfc3-42d4-4474-b45c-757e55e093e9", k);
        l.addAttribute(NetworkInterface.INTERFACE_ATTRIBUTE_NAME, "eth0");
        l.addAttribute(NetworkInterface.MAC_ATTRIBUTE_NAME, "00:11:22:33:44:55");
        l.addAttribute(NetworkInterface.STATE_ATTRIBUTE_NAME, "active");
        l.setSource("/vms/foo/vm1");
        l.setTarget("/network/123");

        List<Mixin> mixins = getFiveMixins();
        for (Mixin mixin : mixins) {
            l.addMixin(mixin);
        }

        return l;
    }

    public static ActionInstance getAction() throws InvalidAttributeValueException, URISyntaxException {
        Action a = new Action(new URI("http://schemas.ogf.org/occi/infrastructure/storage/action#"), "backup", "Backup Storage", null);
        ActionInstance ai = new ActionInstance(a);
        ai.addAttribute(new Attribute(Entity.ID_ATTRIBUTE_NAME), "87f3bfc3-42d4-4474-b45c-757e55e093e9");
        ai.addAttribute(new Attribute(NetworkInterface.INTERFACE_ATTRIBUTE_NAME), "eth0");
        ai.addAttribute(new Attribute(NetworkInterface.MAC_ATTRIBUTE_NAME), "00:11:22:33:44:55");
        ai.addAttribute(new Attribute(NetworkInterface.STATE_ATTRIBUTE_NAME), "active");
        ai.addAttribute(new Attribute(Link.SOURCE_ATTRIBUTE_NAME), "/vms/foo/vm1");
        ai.addAttribute(new Attribute(Link.TARGET_ATTRIBUTE_NAME), "/network/123");

        return ai;
    }
}
