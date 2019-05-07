package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.composer.IField;
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
public class InstanceSelectorPlusDelayedActionsTestCase extends TestCase {
  private PortalSessionTestFacade mSession;

  @Override public void tearDown() throws Exception {
    super.tearDown();
    if (this.mSession != null) {
      this.mSession.close();
      this.mSession = null;
    }
  }

  public void testSelectorAddInstanceOK() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDelayedActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    // press add button
    this.mSession.handleButtonEvent("add", null);
    assertNotNull(this.mSession.getCurrentPage());
    assertEquals("page2", this.mSession.getCurrentPage().getName());
    assertEquals("Persoon instance must be added", 4,
        this.mSession.getProfile().getAllInstancesForEntity("Persoon", false).length);

    // fill required field and press ok button
    this.mSession.handleButtonEvent("ok", new RequestTemplate("Persoon.Naam", "naam"));
    assertNotNull(this.mSession.getCurrentPage());
    assertEquals("page", this.mSession.getCurrentPage().getName());

    assertEquals("Persoon instance must be added", 4,
        this.mSession.getProfile().getAllInstancesForEntity("Persoon", false).length);
  }

  public void testSelectorAddInstanceCancel() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDelayedActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());
    assertEquals("page", this.mSession.getCurrentPage().getName());

    // press add button
    this.mSession.handleButtonEvent("add", null);
    assertNotNull(this.mSession.getCurrentPage());
    assertEquals("page2", this.mSession.getCurrentPage().getName());
    assertEquals("Persoon instance must be added", 4,
        this.mSession.getProfile().getAllInstancesForEntity("Persoon", false).length);

    // press cancel button
    this.mSession.handleButtonEvent("cancel", null);
    assertNotNull(this.mSession.getCurrentPage());
    assertEquals("page", this.mSession.getCurrentPage().getName());

    assertEquals("Persoon instance must NOT be added", 3,
        this.mSession.getProfile().getAllInstancesForEntity("Persoon", false).length);
  }

  public void testSelectorAddInstancePopFlow() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDelayedActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    // press add button
    this.mSession.handleButtonEvent("add", null);
    assertNotNull(this.mSession.getCurrentPage());
    assertEquals("page2", this.mSession.getCurrentPage().getName());
    assertEquals("Persoon instance must be added", 4,
        this.mSession.getProfile().getAllInstancesForEntity("Persoon", false).length);

    // fill required field and press button to end flow
    this.mSession.handleButtonEvent("endflow", new RequestTemplate("Persoon.Naam", "naam"));
    assertNotNull(this.mSession.getCurrentPage());
    assertEquals("page", this.mSession.getCurrentPage().getName());

    assertEquals("Persoon instance must NOT be added", 3,
        this.mSession.getProfile().getAllInstancesForEntity("Persoon", false).length);
  }

  public void testSelectorDeleteInstanceOK() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDelayedActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    // select press delete button
    this.mSession.handleButtonEvent("delete", null);
    assertNotNull(this.mSession.getCurrentPage());

    // press ok
    this.mSession.handleButtonEvent("ok", null);
    assertNotNull(this.mSession.getCurrentPage());

    assertEquals("Persoon instance must be deleted", 2,
        this.mSession.getProfile().getAllInstancesForEntity("Persoon", false).length);
  }

  public void testSelectorDeleteInstanceCancel() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDelayedActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    // select and press delete button
    this.mSession.handleButtonEvent("delete", null);
    assertNotNull(this.mSession.getCurrentPage());

    // press cancel
    this.mSession.handleButtonEvent("cancel", null);
    assertNotNull(this.mSession.getCurrentPage());

    assertEquals("Persoon instance must NOT be deleted", 3,
        this.mSession.getProfile().getAllInstancesForEntity("Persoon", false).length);
  }

  public void testSelectorDeleteInstancePopFlow() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDelayedActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    // select and press delete button
    this.mSession.handleButtonEvent("delete", null);
    assertNotNull(this.mSession.getCurrentPage());

    // press button to end flow
    this.mSession.handleButtonEvent("endflow", null);
    assertNotNull(this.mSession.getCurrentPage());

    assertEquals("Persoon instance must NOT be deleted", 3,
        this.mSession.getProfile().getAllInstancesForEntity("Persoon", false).length);
  }

  /**
   * Delayed delete should validate the fields on the page.
   *
   * AQU-502: Validations after in-place add and delete
   *
   * @throws Exception when an error occurs
   */
  public void testValidateFieldsDelete() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDelayedActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    // emtpy required field Peroon.Naam and select press delete button
    RequestTemplate request = new RequestTemplate();
    request.addFieldValue("Persoon.Naam", "");
    this.mSession.handleButtonEvent("delete", request);

    IField field = (IField) ElementFinder.findElement(this.mSession.getCurrentPage(), "Persoon.Naam");
    assertNotNull("field persoon.naam must be on page", field);
    assertNotNull("field error expected", field.getMessages());
    assertEquals("field error expected", 1, field.getMessages().length);
  }

  /**
   * Delayed add should validate the fields on the page.
   *
   * AQU-502: Validations after in-place add and delete
   *
   * @throws Exception when an error occurs
   */
  public void testValidateFieldsAdd() throws Exception {
    this.mSession = new PortalSessionTestFacade(new InstanceSelectorPlusDelayedActionsTemplate());

    this.mSession.startFlow("start");
    assertNotNull(this.mSession.getCurrentPage());

    // emtpy required field Peroon.Naam and press add button
    RequestTemplate request = new RequestTemplate();
    request.addFieldValue("Persoon.Naam", "");
    this.mSession.handleButtonEvent("add", request);

    IField field = (IField) ElementFinder.findElement(this.mSession.getCurrentPage(), "Persoon.Naam");
    assertNotNull("field persoon.naam must be on page", field);
    assertNotNull("field error expected", field.getMessages());
    assertEquals("field error expected", 1, field.getMessages().length);
  }
}
