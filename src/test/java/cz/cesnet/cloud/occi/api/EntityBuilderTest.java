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
import java.net.URI;
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
}
