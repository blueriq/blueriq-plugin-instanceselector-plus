package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.foundation.Parameters;
import com.aquima.plugin.instanceselectorplus.param.InstanceSelectorPlusParameters;

import junit.framework.TestCase;

/**
 * This test case validates the behavior of the Parameter parser fro the instance selector++ container.
 *
 * @author J. van Leuven
 *
 * @since 6.4
 */
public class ParametersTestCase extends TestCase {
  public void testParameters() {
    Parameters parameters = new Parameters(true);
    parameters.setParameter("noinstancecontainer", "noInstancesContainer");
    parameters.setParameter("addbuttons", "button1");
    parameters.setParameter("addbuttoncondition", "button-condition");
    parameters.setParameter("headercontainer", "headerContainer");
    parameters.setParameter("whereclause", "selectionExpression");
    parameters.setParameter("sortattribute", "Adres.postcode");
    parameters.setParameter("sortorder", "ascending");
    parameters.setParameter("directcreate", "true");
    // TODO add new parameter

    InstanceSelectorPlusParameters selectorParams = InstanceSelectorPlusParameters.getInstance(parameters);
    assertEquals("no instances containername", "noInstancesContainer", selectorParams.getNoInstancesContainer());
    assertNotNull("add buttons", selectorParams.getAddButtons());
    assertEquals("add buttons", 1, selectorParams.getAddButtons().length);

    String buttonDef = selectorParams.getAddButtons()[0];
    assertEquals("add button name", "button1", buttonDef);
    assertEquals("button condition", "button-condition", selectorParams.getAddButtonCondition());
    assertEquals("header", "headerContainer", selectorParams.getHeaderContainer());
    assertEquals("selection", "selectionExpression", selectorParams.getFilterExpression());
    assertEquals("sort attribute", "Adres.postcode", selectorParams.getSortExpression());
    assertEquals("sort direction", 0, selectorParams.getSortOrder());
    assertEquals("action now save", true, selectorParams.isInstanceActionNowForSave());
  }
}
