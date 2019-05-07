package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.composer.IPage;
import com.aquima.interactions.foundation.DataType;
import com.aquima.interactions.foundation.Parameters;
import com.aquima.interactions.foundation.exception.AppException;
import com.aquima.interactions.framework.service.ServiceResult;
import com.aquima.interactions.portal.IService;
import com.aquima.interactions.portal.IServiceContext;
import com.aquima.interactions.portal.IServiceResult;
import com.aquima.interactions.profile.IEntityInstance;
import com.aquima.interactions.profile.IProfile;
import com.aquima.interactions.test.templates.ApplicationTemplate;
import com.aquima.interactions.test.templates.composer.ComposerTemplate;
import com.aquima.interactions.test.templates.composer.ContainerTemplate;
import com.aquima.interactions.test.templates.composer.PageTemplate;
import com.aquima.interactions.test.templates.model.EntityTemplate;
import com.aquima.interactions.test.templates.model.MetaModelTemplate;
import com.aquima.interactions.test.templates.session.ElementFinder;
import com.aquima.interactions.test.templates.session.PortalSessionTestFacade;

import junit.framework.TestCase;

/**
 * Test case for issue: AQU-3062.
 * 
 * @author O. Kerpershoek
 * @since 6.4
 */
public class NestedSelectorsTestCase extends TestCase {
  private ApplicationTemplate getApplication() {
    ApplicationTemplate application = new ApplicationTemplate("test");
    MetaModelTemplate model = application.getMetaModel();
    EntityTemplate company = model.addEntity("Company", null, true);
    EntityTemplate person = model.addEntity("Person");

    company.addRelation("Employees", true, "Person");

    person.addRelation("Children", true, "Person");
    person.addAttribute("Name", DataType.STRING, false);

    ComposerTemplate pageComposer = application.getComposer();
    ContainerTemplate employeeSelector = pageComposer.addContainer("employees");
    ContainerTemplate childrenSelector = pageComposer.addContainer("children");
    PageTemplate page = pageComposer.addPage("test");

    page.addContainer("employees");

    Parameters employeeParameters = new Parameters(false);
    employeeParameters.setParameter("directdelete", "true");
    employeeParameters.setParameter("referrer-path", "Company.Employees");

    employeeSelector.addContainer("children");
    employeeSelector.addButton("DeleteEmployee").addAction("_delete_instance");
    employeeSelector.setTypeName("AQ_InstanceSelectorPlus");
    employeeSelector.setParameters(employeeParameters);

    Parameters childParameters = new Parameters(false);
    childParameters.setParameter("directdelete", "true");
    childParameters.setParameter("referrer-path", "Person.Children");

    childrenSelector.addButton("DeleteChild").addAction("_delete_instance");
    childrenSelector.setTypeName("AQ_InstanceSelectorPlus");
    childrenSelector.setParameters(childParameters);

    application.getFlowEngine().addFlow("start").addServiceCall("init").addFlow("selector");
    application.getFlowEngine().addFlow("selector").addPage("test").addFlow("selector");

    application.addServiceCall("init", "init");
    application.getFactoryManager().getServiceFactory().addService("init", new IService() {
      @Override
      public IServiceResult handle(IServiceContext context) throws AppException {
        IEntityInstance company = context.getActiveInstance("Company");

        this.addEmployee(context.getProfile(), company, 1, "first");
        this.addEmployee(context.getProfile(), company, 2, "second");
        this.addEmployee(context.getProfile(), company, 3, "third");
        return new ServiceResult();
      }

      private void addEmployee(IProfile profile, IEntityInstance company, int childCount, String name)
          throws AppException {
        IEntityInstance employee = profile.createInstance("Person", null, name);

        for (int index = 0; index < childCount; index++) {
          IEntityInstance child = profile.createInstance("Person");

          child.setValue("Name", "Child-" + index);
          employee.addValue("Children", child.getInstanceReference());
        }

        company.addValue("Employees", employee.getInstanceReference());
      }
    });
    application.getFactoryManager().getContainerFactory().addExpander("AQ_InstanceSelectorPlus",
        new InstanceSelectorPlus());

    return application;
  }

  public void testNestedDelete() throws AppException {
    PortalSessionTestFacade session = new PortalSessionTestFacade(this.getApplication());

    try {
      session.startFlow("start");

      IPage page = session.getCurrentPage();

      assertNotNull(page);

      IEntityInstance first = session.getProfile().getInstanceByName("Person", "first");

      assertNotNull(first.getInstanceValue("Children"));

      // delete the child of the first employee
      session.handleButtonEvent("DeleteChild", 0, null);

      // Test disabled, as the instance selector no longer deletes an instances
      // Only a "delete-instance" event is generated.
      // first = session.getProfile().getInstanceByName("Person", "first");
      // assertNull(first.getInstanceValue("Children"));
    } finally {
      session.close();
    }
  }

  public void testIds() throws Exception {
    ApplicationTemplate app = this.getApplication();
    PortalSessionTestFacade session = new PortalSessionTestFacade(app);

    try {
      session.startFlow("start");

      IPage page = session.getCurrentPage();

      assertNotNull(page);
      assertNotNull("Button should have an id", ElementFinder.findElement(page, "DeleteChild").getRuntimeKey());
    } finally {
      session.close();
    }
  }
}
