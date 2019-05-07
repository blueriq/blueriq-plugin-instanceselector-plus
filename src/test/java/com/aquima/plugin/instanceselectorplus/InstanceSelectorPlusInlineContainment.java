package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.composer.IContainer;
import com.aquima.interactions.composer.IElement;
import com.aquima.interactions.foundation.DataType;
import com.aquima.interactions.foundation.exception.AppException;
import com.aquima.interactions.foundation.types.StringValue;
import com.aquima.interactions.framework.service.ServiceResult;
import com.aquima.interactions.portal.IService;
import com.aquima.interactions.portal.IServiceContext;
import com.aquima.interactions.portal.IServiceResult;
import com.aquima.interactions.portal.ServiceException;
import com.aquima.interactions.profile.IEntityInstance;
import com.aquima.interactions.profile.IProfile;
import com.aquima.interactions.test.templates.ApplicationTemplate;
import com.aquima.interactions.test.templates.ParametersTemplate;
import com.aquima.interactions.test.templates.composer.ContainerTemplate;
import com.aquima.interactions.test.templates.composer.PageTemplate;
import com.aquima.interactions.test.templates.model.EntityTemplate;
import com.aquima.interactions.test.templates.session.ElementFinder;
import com.aquima.interactions.test.templates.session.PortalSessionTestFacade;
import com.aquima.interactions.test.templates.session.RequestTemplate;

import junit.framework.TestCase;

/**
 * Test class top reproduce AQU-1050: problem with refresh in InstanceSelectorPlus.
 * 
 * @author Jon van Leuven
 * @since 5.1
 */
public class InstanceSelectorPlusInlineContainment extends TestCase {

  private PortalSessionTestFacade mSession;

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    if (this.mSession != null) {
      this.mSession.close();
      this.mSession = null;
    }
  }

  public void testSelectorContent() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusInlineContainmentTemplate());

    this.mSession.startFlow("start");
    assertNotNull("instanceselector must have an add button",
        ElementFinder.findElement(this.mSession.getCurrentPage(), "add"));
    assertNotNull("instanceselector must have an update button",
        ElementFinder.findElement(this.mSession.getCurrentPage(), "update"));
    assertNotNull("instanceselector must have an delete button",
        ElementFinder.findElement(this.mSession.getCurrentPage(), "delete"));
    assertNotNull("instanceselector must have an inline adress container",
        ElementFinder.findElement(this.mSession.getCurrentPage(), "adres_container"));
  }

  public void testRefresh() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusInlineContainmentTemplate());
    this.mSession.startFlow("start");
    IContainer instanceSelector = (IContainer) ElementFinder.findElement(this.mSession.getCurrentPage(), "container");
    IElement straatField = ElementFinder.findElement(this.mSession.getCurrentPage(), "Adres.Straat");
    assertNotNull(instanceSelector);
    assertNotNull(straatField);

    // refresh field adres.straat
    this.mSession.handleFieldRefresh("Adres.Straat", new RequestTemplate("Adres.Straat", "straat_naam"));
    IContainer instanceSelectorAfterRefresh =
        (IContainer) ElementFinder.findElement(this.mSession.getCurrentPage(), "container");
    assertNotNull(instanceSelectorAfterRefresh);
    assertNotSame(instanceSelector, instanceSelectorAfterRefresh);
    assertFalse("content check on instance selector must be false",
        instanceSelector.equalsContent(instanceSelectorAfterRefresh));
  }

}


/**
 * Helper class for the test case.
 * 
 * @author J. van Leuven
 * 
 * @since 5.1
 */
class InstanceSelectorPlusInlineContainmentTemplate extends ApplicationTemplate {
  protected InstanceSelectorPlusInlineContainmentTemplate() {
    super("InstanceSelectorPlusInlineContainment");
    ParametersTemplate containerParams = new ParametersTemplate();

    containerParams.setParameterReference("noinstancecontainer", "NoInstancesContainer");
    containerParams.setParameter("addbuttons", "add");
    containerParams.setParameterReference("entity", "persoon");
    containerParams.setParameterReference("headercontainer", "headerContainer");
    containerParams.setParameterExpression("whereclause", "");
    containerParams.setParameterExpression("sortattribute", "Persoon.naam");
    containerParams.setParameter("sortorder", "ascending");
    containerParams.setParameter("directcreate", "true");
    containerParams.setParameter("directdelete", "true");

    EntityTemplate bedrijf = this.getMetaModel().addEntity("Bedrijf", null, true);
    bedrijf.addAttribute("Naam", DataType.STRING, false);
    bedrijf.addAttribute("Werknemers", DataType.ENTITY, true).setRelation("Persoon", null);
    EntityTemplate persoon = this.getMetaModel().addEntity("Persoon", null, false);
    persoon.addAttribute("Naam", DataType.STRING, false);
    persoon.addAttribute("Adres", DataType.ENTITY, false).setRelation("Adres", null);
    EntityTemplate adres = this.getMetaModel().addEntity("Adres", null, false);
    adres.addAttribute("Straat", DataType.STRING, false);

    this.getFlowEngine().addFlow("start").addServiceCall("initprofile").addFlow("page_flow");
    this.getFlowEngine().addFlow("page_flow").addPage("page").addPage("page2").addFlow("page_flow");

    this.getComposer().addAsset("text_header1").addText("kmtext", "Header 1");
    this.getComposer().addAsset("text_header2").addText("kmtext", "Header 2");
    this.getComposer().addAsset("text_noInstances").addText("kmtext", "no instances available");
    this.getComposer().addButton("submit");
    this.getComposer().addButton("add");
    this.getComposer().addButton("update");
    this.getComposer().addButton("delete");
    this.getComposer().addButton("ok");
    this.getComposer().addButton("endflow");
    this.getComposer().addButton("cancel");
    this.getComposer().addContainer("NoInstancesContainer").addAsset("text_noInstances");
    this.getComposer().addField("Persoon.Naam"); // .setRequired(true);

    ContainerTemplate adresContainer = this.getComposer().addContainer("adres_container");
    adresContainer.addField("Adres.Straat").setIsRefreshField(true);
    ContainerTemplate adresContainer2 = this.getComposer().addContainer("adres_container2");
    adresContainer2.addField("Adres.Straat").setReadonly(true);
    ContainerTemplate container = this.getComposer().addContainer("container");
    container.setTypeName("AQ_InstanceSelectorPlus");
    container.setParameters(containerParams);
    container.addField("Bedrijf.Naam").setReadonly(true);
    container.addField("Persoon.Naam").setIsRefreshField(false);
    container.addContainer("adres_container").setRelation("Persoon.Adres");
    container.addContainer("adres_container2").setRelation("Persoon.Adres");
    container.addButton("update");
    container.addButton("delete").addAction("_delete_instance");
    this.getComposer().addContainer("container_wrapper").addContainer("container");
    this.getComposer().addContainer("static_container").addField("Bedrijf.Naam").setIsRefreshField(true);
    this.getComposer().addContainer("static_container_wrapper").addContainer("static_container");
    ContainerTemplate headerContainer = this.getComposer().addContainer("headerContainer");
    headerContainer.addAsset("text_header1");
    headerContainer.addAsset("text_header2");
    ContainerTemplate container2 = this.getComposer().addContainer("container2");
    container2.addField("Persoon.Naam");
    container2.addButton("ok").addAction("_save_instance");
    container2.addButton("endflow").addAction("doflow");
    container2.addButton("cancel").addAction("_void");
    PageTemplate page = this.getComposer().addPage("page");
    page.addContainer("container_wrapper").setRelation("Bedrijf.Werknemers");
    page.addContainer("static_container_wrapper");
    this.getComposer().addPage("page2");

    this.getFactoryManager().getServiceFactory().addService("initprofile", new IService() {
      @Override
      public IServiceResult handle(IServiceContext context) throws ServiceException, AppException {
        IProfile profile = context.getProfile();
        IEntityInstance bedrijf = profile.getSingletonInstance("Bedrijf", true);
        bedrijf.setValue("Naam", new StringValue("Everest"));
        IEntityInstance persoon1 = profile.createInstance("Persoon");
        IEntityInstance persoon2 = profile.createInstance("Persoon");
        IEntityInstance persoon3 = profile.createInstance("Persoon");
        persoon1.setValue("Naam", new StringValue("naam 1ste instantie"));
        persoon2.setValue("Naam", new StringValue("naam 2de instantie"));
        persoon3.setValue("Naam", new StringValue("naam 3de instantie"));

        bedrijf.addValue("Werknemers", persoon1.getInstanceReference());
        bedrijf.addValue("Werknemers", persoon2.getInstanceReference());
        bedrijf.addValue("Werknemers", persoon3.getInstanceReference());
        return new ServiceResult();
      }
    });
    this.getFactoryManager().getContainerFactory().addExpander("AQ_InstanceSelectorPlus", new InstanceSelectorPlus());
  }
}
