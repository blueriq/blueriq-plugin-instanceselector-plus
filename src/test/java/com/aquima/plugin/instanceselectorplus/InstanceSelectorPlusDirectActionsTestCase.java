package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.composer.model.Button;
import com.aquima.interactions.foundation.IListValue;
import com.aquima.interactions.foundation.types.StringValue;
import com.aquima.interactions.test.templates.session.ElementFinder;
import com.aquima.interactions.test.templates.session.PortalSessionTestFacade;
import com.aquima.interactions.test.templates.session.RequestTemplate;

import junit.framework.TestCase;

/**
 * This test case validates the behavior of the instance selector++ container.
 *
 * @author J. van Leuven
 *
 * @since 5.1
 */
public class InstanceSelectorPlusDirectActionsTestCase extends TestCase {
  private PortalSessionTestFacade mSession;

  @Override public void tearDown() throws Exception {
    super.tearDown();
    if (this.mSession != null) {
      this.mSession.close();
      this.mSession = null;
    }
  }

  public void testSelectorContent() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDirectActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    Button button = (Button) ElementFinder.findElementSafe(this.mSession.getCurrentPage(), "add");
    assertNotNull("instanceselector must have an add button", button);
    assertNotNull("event-name", button.getEventName());
    assertNotNull("instanceselector must have an update button",
        ElementFinder.findElementSafe(this.mSession.getCurrentPage(), "update"));
    assertNotNull("instanceselector must have an delete button",
        ElementFinder.findElementSafe(this.mSession.getCurrentPage(), "delete"));
  }

  public void testSelectorContentNoInstances() throws Exception {
    this.mSession = new PortalSessionTestFacade(
        new InstanceSelectorPlusDirectActionsTemplate("Persoon.Naam = 'naam_die_niet_voorkomt'"));

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    assertNotNull("instanceselector must contain NoInstancesContainer",
        ElementFinder.findElementSafe(this.mSession.getCurrentPage(), "NoInstancesContainer"));
    assertNotNull("instanceselector must contain add button",
        ElementFinder.findElementSafe(this.mSession.getCurrentPage(), "add"));
    assertNull("instanceselector must NOT contain update button",
        ElementFinder.findElementSafe(this.mSession.getCurrentPage(), "update"));
    assertNull("instanceselector must NOT contain delete button",
        ElementFinder.findElementSafe(this.mSession.getCurrentPage(), "delete"));
  }

  public void testSelectorAddInstance() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDirectActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());
    assertEquals("page", this.mSession.getCurrentPage().getName());

    // press add button
    this.mSession.handleButtonEvent("add", null);
    assertNotNull(this.mSession.getCurrentPage());
    assertEquals("page", this.mSession.getCurrentPage().getName());
    assertEquals("Persoon instance must be added", 4,
        this.mSession.getProfile().getAllInstancesForEntity("Persoon", false).length);

    // AQU-1162
    IListValue systemEvent =
        this.mSession.getProfile().getSingletonInstance("system", false).getValue("event").toListValue();
    assertEquals("system.event no correct value", 4, systemEvent.getValueCount());
    assertTrue("system.event no correct value", systemEvent.contains(new StringValue("_add_instance")));
    assertTrue("system.event no correct value", systemEvent.contains(new StringValue("_add_instance_Persoon")));
    assertTrue("system.event no correct value", systemEvent.contains(new StringValue("_draft")));
    assertTrue("system.event no correct value", systemEvent.contains(new StringValue("instanceaction_now")));
  }

  public void testSelectorUpdateInstance() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDirectActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    // press update button
    this.mSession.handleButtonEvent("update", null);
    assertNotNull(this.mSession.getCurrentPage());

    // assertNotNull("Persoon instance must be active for update", session.getProfile().getActiveInstance("Persoon"));
    // assertEquals("first Persoon instance must be active for update", 1,
    // session.getContext().getActiveInstance("Persoon").getId());
  }

  public void testSelectorDeleteInstance() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDirectActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());
    assertEquals("page", this.mSession.getCurrentPage().getName());

    // press delete button
    this.mSession.handleButtonEvent("delete", null);
    assertNotNull(this.mSession.getCurrentPage());
    assertEquals("page", this.mSession.getCurrentPage().getName());

    assertEquals("persoon must be deleted", 2,
        this.mSession.getProfile().getAllInstancesForEntity("Persoon", true).length);

  }

  /**
   * Direct delete should NOT validate the fields on the page.
   *
   * AQU-502: Validations after in-place add and delete
   *
   * @throws Exception when an error occurs
   */
  public void testValidateFieldsDelete() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDirectActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    // emtpy required field Peroon.Naam and press delete button
    this.mSession.handleButtonEvent("delete", new RequestTemplate("Persoon.Naam", ""));
    assertNotNull(this.mSession.getCurrentPage());

    assertEquals("persoon must be deleted", 2,
        this.mSession.getProfile().getAllInstancesForEntity("Persoon", true).length);

  }

  /**
   * Direct add should NOT validate the fields on the page.
   *
   * AQU-502: Validations after in-place add and delete
   *
   * @throws Exception when an error occurs
   */
  public void testValidateFieldsAdd() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDirectActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());
    assertNotNull(this.mSession.getCurrentPage());

    // emtpy required field Peroon.Naam and press add button
    this.mSession.handleButtonEvent("add", new RequestTemplate("Persoon.Naam", ""));
    assertNotNull(this.mSession.getCurrentPage());
    assertEquals("Persoon instance must be added", 4,
        this.mSession.getProfile().getAllInstancesForEntity("Persoon", false).length);
  }
}
