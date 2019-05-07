package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.composer.ICompositeElement;
import com.aquima.interactions.composer.IContainer;
import com.aquima.interactions.foundation.DataType;
import com.aquima.interactions.foundation.Parameters;
import com.aquima.interactions.foundation.logging.LogFactory;
import com.aquima.interactions.foundation.logging.Logger;
import com.aquima.interactions.foundation.text.ILanguage;
import com.aquima.interactions.foundation.xml.generation.IXmlElement;
import com.aquima.interactions.framework.renderer.page.r6.PageR6XmlRenderer;
import com.aquima.interactions.test.templates.ApplicationTemplate;
import com.aquima.interactions.test.templates.EngineFactory;
import com.aquima.interactions.test.templates.IApplicationTemplate;
import com.aquima.interactions.test.templates.ParametersTemplate;
import com.aquima.interactions.test.templates.composer.ContainerTemplate;
import com.aquima.interactions.test.templates.composer.PageTemplate;
import com.aquima.interactions.test.templates.model.EntityTemplate;
import com.aquima.interactions.test.templates.session.ElementFinder;
import com.aquima.interactions.test.templates.session.PortalSessionTestFacade;

import junit.framework.TestCase;

import java.util.HashMap;

/**
 * Test case for AQR-1011 Page element ids are not generated for instanceselector headers when there are no instances
 * 
 * @author d.roest
 * @since 8.0
 */
public class InstanceSelectorPlusNoInstancesTestCase extends TestCase {
  private static final Logger LOG = LogFactory.getLogger(InstanceSelectorPlusNoInstancesTestCase.class);

  private PortalSessionTestFacade mSession;

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    if (this.mSession != null) {
      this.mSession.close();
      this.mSession = null;
    }
  }

  public void testIdsWithoutInstances() throws Exception {
    ApplicationTemplate template = this.createTemplate();
    this.mSession = new PortalSessionTestFacade(template);

    this.mSession.startFlow("start");
    renderPageXml(this.mSession.getCurrentPage(), template);

    assertNotNull("Header should have an id", ElementFinder
        .findElement((IContainer) this.mSession.getCurrentPage().getElement(0), "text_header1").getRuntimeKey());
    assertNotNull("Header should have an id", ElementFinder
        .findElement((IContainer) this.mSession.getCurrentPage().getElement(0), "text_header2").getRuntimeKey());
  }

  private ApplicationTemplate createTemplate() {
    String filterExpression = "";
    ApplicationTemplate app = new ApplicationTemplate("test");
    app.getProject().addTheme("default");
    app.getProject().addTheme("default-json");

    ParametersTemplate containerParams = new ParametersTemplate();

    // containerParams.setParameterReference("noinstancecontainer", "NoInstancesContainer");
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

    EntityTemplate bedrijf = app.getMetaModel().addEntity("Bedrijf", null, false);
    bedrijf.addAttribute("Naam", DataType.STRING, false);
    bedrijf.addAttribute("Werknemers", DataType.ENTITY, true).setRelation("Persoon", null);
    EntityTemplate persoon = app.getMetaModel().addEntity("Persoon", null, false);
    persoon.addAttribute("Naam", DataType.STRING, false);

    Parameters createInstanceParameters = new Parameters(true);
    createInstanceParameters.setParameter("type", "persoon");
    createInstanceParameters.setParameter("attribute-path", "Bedrijf.Werknemers");

    Parameters deleteInstanceParameters = new Parameters(true);
    deleteInstanceParameters.setParameter("type", "persoon");

    app.getFlowEngine().addFlow("start").addPage("page");

    app.getComposer().addAsset("text_header1").addText("kmtext", "Header 1");
    app.getComposer().addAsset("text_header2").addText("kmtext", "Header 2");
    app.getComposer().addAsset("text_noInstances").addText("kmtext", "no instances available");
    app.getComposer().addButton("submit");
    app.getComposer().addButton("add");
    app.getComposer().addButton("update");
    app.getComposer().addButton("delete");
    app.getComposer().addButton("ok");
    app.getComposer().addButton("cancel");
    app.getComposer().addContainer("NoInstancesContainer").addAsset("text_noInstances");
    app.getComposer().addField("Persoon.Naam"); // .setRequired(true);
    app.getFactoryManager().getContainerFactory().addExpander("AQ_InstanceSelectorPlus", new InstanceSelectorPlus());

    ContainerTemplate container = app.getComposer().addContainer("container");
    container.setTypeName("AQ_InstanceSelectorPlus");
    container.setParameters(containerParams);
    container.addContainer("sub-container");

    ContainerTemplate headerContainer = app.getComposer().addContainer("headerContainer");
    headerContainer.addAsset("text_header1");
    headerContainer.addAsset("text_header2");

    PageTemplate page = app.getComposer().addPage("page");
    page.addContainer("container");// .setRelation("Bedrijf.Werknemers");
    // app.getComposer().addPage("page2");

    return app;
  }

  private IXmlElement renderPageXml(ICompositeElement element, IApplicationTemplate app) {
    return renderPageXml(element, app, EngineFactory.createProject(app).getDefaultLanguage());
  }

  private IXmlElement renderPageXml(ICompositeElement element, IApplicationTemplate app, ILanguage language) {
    IXmlElement xml = PageR6XmlRenderer.createFor(new HashMap(0), false, false, false, true).generateXml(element,
        app.getApplicationId(), language, language, new Parameters(true));
    LOG.info(xml.toXmlFragment(true));
    return xml;
  }
}
