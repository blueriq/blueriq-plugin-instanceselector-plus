package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.foundation.DataType;
import com.aquima.interactions.foundation.Parameters;
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
import com.aquima.interactions.test.templates.composer.ButtonReference;
import com.aquima.interactions.test.templates.composer.ContainerTemplate;
import com.aquima.interactions.test.templates.composer.PageTemplate;
import com.aquima.interactions.test.templates.flow.PageNodeTemplate;
import com.aquima.interactions.test.templates.model.EntityTemplate;

/**
 * Application template for the instance selector+.
 * 
 * @author Jon van Leuven
 * @since 6.0
 */
public class InstanceSelectorPlusDirectActionsTemplate extends ApplicationTemplate {
  /**
   * Default constructor for the template.
   */
  public InstanceSelectorPlusDirectActionsTemplate() {
    this("");
  }

  /**
   * Constructs the template with the required parameters.
   * 
   * @param filterExpression The filter expression that should be used by the instance selector.
   */
  public InstanceSelectorPlusDirectActionsTemplate(String filterExpression) {
    super("InstanceSelectorPlusDirectActions");
    this.getProject().addTheme("default");
    this.getProject().addTheme("default-json");

    ParametersTemplate containerParams = new ParametersTemplate();

    containerParams.setParameterReference("noinstancecontainer", "NoInstancesContainer");
    containerParams.setParameter("addbuttons", "add");
    containerParams.setParameter("addbuttonevents", "create-instance");
    containerParams.setParameter("addbuttoncondition", "TRUE");
    containerParams.setParameterReference("entity", "persoon");
    containerParams.setParameter("headercontainer", "headerContainer");
    containerParams.setParameterExpression("whereclause", filterExpression);
    containerParams.setParameterExpression("sortattribute", "Persoon.naam");
    containerParams.setParameter("sortorder", "ascending");
    containerParams.setParameter("directcreate", "true");
    // containerParams.setParameter("directdelete", "true");

    EntityTemplate bedrijf = this.getMetaModel().addEntity("Bedrijf", null, true);
    bedrijf.addAttribute("Naam", DataType.STRING, false);
    bedrijf.addAttribute("Werknemers", DataType.ENTITY, true).setRelation("Persoon", null);
    EntityTemplate persoon = this.getMetaModel().addEntity("Persoon", null, false);
    persoon.addAttribute("Naam", DataType.STRING, false);

    Parameters createInstanceParameters = new Parameters(true);
    createInstanceParameters.setParameter("type", "persoon");
    createInstanceParameters.setParameter("attribute-path", "Bedrijf.Werknemers");

    Parameters deleteInstanceParameters = new Parameters(true);
    deleteInstanceParameters.setParameter("type", "persoon");

    this.getFlowEngine().addFlow("start").addServiceCall("initprofile").addFlow("page_flow");

    PageNodeTemplate isPageNode = this.getFlowEngine().addFlow("page_flow").addPage("page");
    isPageNode.addPage("page2").addFlow("page_flow");
    isPageNode.getEdge("create-instance")
        .addServiceCall("AQ_CreateInstance", "AQ_CreateInstance", createInstanceParameters).addPage("page");
    isPageNode.getEdge("delete-instance")
        .addServiceCall("AQ_DeleteInstance", "AQ_DeleteInstance", deleteInstanceParameters).addPage("page");

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
    ContainerTemplate subContainer = this.getComposer().addContainer("sub-container");
    subContainer.addField("Bedrijf.Naam").setReadonly(true);
    ContainerTemplate container = this.getComposer().addContainer("container");
    container.setTypeName("AQ_InstanceSelectorPlus");
    container.setParameters(containerParams);
    container.addContainer("sub-container");
    container.addField("Persoon.Naam").setIsRefreshField(false);
    container.addButton("update");
    ButtonReference deleteRef = container.addButton("delete");
    deleteRef.addAction("_delete_instance");
    deleteRef.addAction("_draft");
    this.getComposer().addContainer("container_wrapper").addContainer("container");
    this.getComposer().addContainer("static_container").addField("Bedrijf.Naam").setIsRefreshField(true);
    this.getComposer().addContainer("static_container_wrapper").addContainer("static_container");
    ContainerTemplate headerContainer = this.getComposer().addContainer("headerContainer");
    headerContainer.addAsset("text_header1");
    headerContainer.addAsset("text_header2");
    ContainerTemplate container2 = this.getComposer().addContainer("container2");
    container2.addField("Persoon.Naam").getField().setRequired(true);
    container2.addButton("ok").addAction("_save_instance");
    container2.addButton("endflow").addAction("doflow");
    container2.addButton("cancel").addAction("_void");
    PageTemplate page = this.getComposer().addPage("page");
    page.addContainer("container_wrapper").setRelation("Bedrijf.Werknemers");
    page.addContainer("static_container_wrapper");
    this.getComposer().addPage("page2");

    this.getFactoryManager().getServiceFactory().addService("initprofile", new IService() {
      @Override public IServiceResult handle(IServiceContext context) throws ServiceException, AppException {
        IProfile profile = context.getProfile();
        IEntityInstance bedrijfInst = profile.getSingletonInstance("Bedrijf", true);
        bedrijfInst.setValue("Naam", new StringValue("Everest"));
        IEntityInstance persoon1 = profile.createInstance("Persoon");
        IEntityInstance persoon2 = profile.createInstance("Persoon");
        IEntityInstance persoon3 = profile.createInstance("Persoon");
        persoon1.setValue("Naam", new StringValue("naam 1ste instantie"));
        persoon2.setValue("Naam", new StringValue("naam 2de instantie"));
        persoon3.setValue("Naam", new StringValue("naam 3de instantie"));

        bedrijfInst.addValue("Werknemers", persoon1.getInstanceReference());
        bedrijfInst.addValue("Werknemers", persoon2.getInstanceReference());
        bedrijfInst.addValue("Werknemers", persoon3.getInstanceReference());
        return new ServiceResult();
      }
    });
    this.getFactoryManager().getContainerFactory().addExpander("AQ_InstanceSelectorPlus", new InstanceSelectorPlus());
  }
}
