package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.composer.IPage;
import com.aquima.interactions.foundation.DataType;
import com.aquima.interactions.foundation.Parameters;
import com.aquima.interactions.foundation.types.EntityValue;
import com.aquima.interactions.foundation.types.IntegerValue;
import com.aquima.interactions.foundation.types.StringValue;
import com.aquima.interactions.framework.service.ServiceResult;
import com.aquima.interactions.portal.IPortalContext;
import com.aquima.interactions.portal.IService;
import com.aquima.interactions.portal.IServiceContext;
import com.aquima.interactions.portal.IServiceResult;
import com.aquima.interactions.portal.ServiceException;
import com.aquima.interactions.profile.IEntityInstance;
import com.aquima.interactions.test.templates.ApplicationTemplate;
import com.aquima.interactions.test.templates.composer.ContainerTemplate;
import com.aquima.interactions.test.templates.model.EntityTemplate;
import com.aquima.interactions.test.templates.session.PortalSessionTestFacade;

import junit.framework.TestCase;

/**
 * This test case test sorting issue with the instance selector plus. (see AQU-1830 for more details)
 * 
 * @author Jon van Leuven
 * @since 6.2
 */
public class SortingTestCase extends TestCase {
  private PortalSessionTestFacade mSession;

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    if (this.mSession != null) {
      this.mSession.close();
    }
  }

  /**
   * Test sorting in ascending order.
   * 
   * @throws Exception Something wrong.
   */
  public void testSortingAscending() throws Exception {
    // prepare:
    this.mSession = new PortalSessionTestFacade(this.createApplicationTemplate(true, false));

    // invoke:
    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    // assert:
    InstanceSelectorContainer selector = this.assertContainer(this.mSession.getCurrentPage());
    assertEquals("Coen", ((EntityValue) selector.getRowAt(0).getId()).getName());
    assertEquals("Jon", ((EntityValue) selector.getRowAt(1).getId()).getName());
    assertEquals("Friso", ((EntityValue) selector.getRowAt(2).getId()).getName());
    assertEquals("Ocker", ((EntityValue) selector.getRowAt(3).getId()).getName());
  }

  /**
   * Test sorting in descending order.
   * 
   * @throws Exception Something wrong.
   */
  public void testSortingDescending() throws Exception {
    // prepare:
    this.mSession = new PortalSessionTestFacade(this.createApplicationTemplate(false, false));

    // invoke:
    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    // assert:
    InstanceSelectorContainer selector = this.assertContainer(this.mSession.getCurrentPage());
    assertEquals("Ocker", ((EntityValue) selector.getRowAt(0).getId()).getName());
    assertEquals("Friso", ((EntityValue) selector.getRowAt(1).getId()).getName());
    assertEquals("Jon", ((EntityValue) selector.getRowAt(2).getId()).getName());
    assertEquals("Coen", ((EntityValue) selector.getRowAt(3).getId()).getName());
  }

  /**
   * Test sorting in ascending order with unknows as last.
   * 
   * @throws Exception Something wrong.
   */
  public void testSortingAscendingUnknownsAsLast() throws Exception {
    // prepare:
    this.mSession = new PortalSessionTestFacade(this.createApplicationTemplate(true, true));

    // invoke:
    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    // assert:
    InstanceSelectorContainer selector = this.assertContainer(this.mSession.getCurrentPage());
    assertEquals("Jon", ((EntityValue) selector.getRowAt(0).getId()).getName());
    assertEquals("Friso", ((EntityValue) selector.getRowAt(1).getId()).getName());
    assertEquals("Ocker", ((EntityValue) selector.getRowAt(2).getId()).getName());
    assertEquals("Coen", ((EntityValue) selector.getRowAt(3).getId()).getName());
  }

  private InstanceSelectorContainer assertContainer(IPage page) {
    assertTrue(page.getElement(0) instanceof InstanceSelectorContainer);
    InstanceSelectorContainer selector = (InstanceSelectorContainer) page.getElement(0);
    assertEquals("invalid number of instances", 4, selector.getRowCount());
    return selector;
  }

  private ApplicationTemplate createApplicationTemplate(boolean ascending, boolean sortUnknownsAsLast) {
    Parameters parameters = new Parameters(true);
    parameters.setParameter("entity", "Person");
    parameters.setParameter("sortattribute", "Person.SortValue");
    parameters.setParameter("sortorder", ascending ? "ascending" : "descending");
    if (sortUnknownsAsLast) {
      parameters.setParameter("sortunknownsaslast", "True");
    }

    ApplicationTemplate application = new ApplicationTemplate("test");
    EntityTemplate person = application.getMetaModel().addEntity("Person");
    person.addAttribute("Name", DataType.STRING, false);
    person.addAttribute("SortValue", DataType.INTEGER, false);

    ContainerTemplate container = application.getComposer().addPage("page").addContainer("container").getContainer();
    container.setTypeName("selector_plus");
    container.setParameters(parameters);
    container.addField("Person.Name");

    application.getFlowEngine().addFlow("start").addServiceCall("init").addPage("page");

    application.getFactoryManager().getServiceFactory().addService("init", new IService() {
      @Override
      public IServiceResult handle(IServiceContext context) throws ServiceException, Exception {
        SortingTestCase.this.createPerson(new StringValue("Jon"), new IntegerValue(1), context);
        SortingTestCase.this.createPerson(new StringValue("Ocker"), new IntegerValue(3), context);
        SortingTestCase.this.createPerson(new StringValue("Friso"), new IntegerValue(2), context);
        SortingTestCase.this.createPerson(new StringValue("Coen"), IntegerValue.UNKNOWN, context);
        return new ServiceResult();
      }
    });
    application.getFactoryManager().getContainerFactory().addExpander("selector_plus", new InstanceSelectorPlus());

    return application;
  }

  private IEntityInstance createPerson(StringValue name, IntegerValue sortValue, IPortalContext context)
      throws Exception {
    IEntityInstance person = context.getProfile().createInstance("Person", null, name.stringValue());
    person.setValue("Name", name);
    person.setValue("SortValue", sortValue);
    return person;
  }
}
