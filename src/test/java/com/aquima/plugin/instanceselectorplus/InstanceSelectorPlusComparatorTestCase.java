package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.composer.model.Container;
import com.aquima.interactions.foundation.DataType;
import com.aquima.interactions.foundation.IListValue;
import com.aquima.interactions.foundation.IPrimitiveValue;
import com.aquima.interactions.foundation.Parameters;
import com.aquima.interactions.foundation.logging.LogFactory;
import com.aquima.interactions.foundation.logging.Logger;
import com.aquima.interactions.foundation.types.EntityValue;
import com.aquima.interactions.foundation.types.IntegerValue;
import com.aquima.interactions.foundation.types.ListValue;
import com.aquima.interactions.portal.IPortalContext;
import com.aquima.interactions.profile.IEntityInstance;
import com.aquima.interactions.test.templates.ApplicationTemplate;
import com.aquima.interactions.test.templates.context.ServiceContextTemplate;
import com.aquima.interactions.test.templates.model.EntityTemplate;
import com.aquima.plugin.instanceselectorplus.param.InstanceSelectorPlusParameters;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Test case to test instance ordering of the instance selector plus. See AQU-1830.
 * 
 * @author Jon van Leuven
 * @since 6.1
 */
public class InstanceSelectorPlusComparatorTestCase extends TestCase {
  private static final Logger LOG = LogFactory.getLogger(InstanceSelectorPlusComparatorTestCase.class);
  private IPortalContext mContext;

  @Override
  public void setUp() throws Exception {
    ApplicationTemplate application = new ApplicationTemplate("test");
    EntityTemplate person = application.getMetaModel().addEntity("Person", null, false);
    person.addAttribute("Name", DataType.STRING, false);
    person.addAttribute("Age", DataType.INTEGER, false);
    application.getMetaModel().addEntity("Application", null, false).addAttribute("Name", DataType.STRING, false);
    application.getFactoryManager().getContainerFactory().addExpander("selector_plus", new InstanceSelectorPlus());
    this.mContext = new ServiceContextTemplate(application).toContext();
  }

  public void testNoSortExpressionAscending() throws Exception {
    InstanceSelectorPlusComparator comparator =
        new InstanceSelectorPlusComparator(this.mContext, this.createParams("", true, false));
    ArrayList containers = this.createInstanceContainers(
        new ListValue(new IPrimitiveValue[] { this.createPerson("Jon"), this.createPerson("Friso1"),
            this.createPerson("Ocker"), this.createPerson("Friso2"), this.createPerson(null) }));
    Collections.sort(containers, comparator);
    this.logResult(containers);

    assertEquals("Jon", ((Container) containers.get(0)).getEntityInstanceId().getName());
    assertEquals("Friso1", ((Container) containers.get(1)).getEntityInstanceId().getName());
    assertEquals("Ocker", ((Container) containers.get(2)).getEntityInstanceId().getName());
    assertEquals("Friso2", ((Container) containers.get(3)).getEntityInstanceId().getName());
    assertEquals(null, ((Container) containers.get(4)).getEntityInstanceId().getName());
  }

  public void testNoSortExpressionDescending() throws Exception {
    InstanceSelectorPlusComparator comparator =
        new InstanceSelectorPlusComparator(this.mContext, this.createParams("", false, false));
    ArrayList containers = this.createInstanceContainers(
        new ListValue(new IPrimitiveValue[] { this.createPerson("Jon"), this.createPerson("Friso1"),
            this.createPerson("Ocker"), this.createPerson("Friso2"), this.createPerson(null) }));
    Collections.sort(containers, comparator);
    this.logResult(containers);

    assertEquals(null, ((Container) containers.get(0)).getEntityInstanceId().getName());
    assertEquals("Friso2", ((Container) containers.get(1)).getEntityInstanceId().getName());
    assertEquals("Ocker", ((Container) containers.get(2)).getEntityInstanceId().getName());
    assertEquals("Friso1", ((Container) containers.get(3)).getEntityInstanceId().getName());
    assertEquals("Jon", ((Container) containers.get(4)).getEntityInstanceId().getName());
  }

  public void testSortExpressionAscending() throws Exception {
    InstanceSelectorPlusComparator comparator =
        new InstanceSelectorPlusComparator(this.mContext, this.createParams("Person.Name", true, false));
    ArrayList containers = this.createInstanceContainers(
        new ListValue(new IPrimitiveValue[] { this.createPerson("Jon"), this.createPerson("Friso1"),
            this.createPerson("Ocker"), this.createPerson("Friso2"), this.createPerson(null) }));
    Collections.sort(containers, comparator);
    this.logResult(containers);

    assertEquals(null, ((Container) containers.get(0)).getEntityInstanceId().getName());
    assertEquals("Friso1", ((Container) containers.get(1)).getEntityInstanceId().getName());
    assertEquals("Friso2", ((Container) containers.get(2)).getEntityInstanceId().getName());
    assertEquals("Jon", ((Container) containers.get(3)).getEntityInstanceId().getName());
    assertEquals("Ocker", ((Container) containers.get(4)).getEntityInstanceId().getName());
  }

  public void testSortExpressionAscendingSortUnknowsAsLast() throws Exception {
    InstanceSelectorPlusComparator comparator =
        new InstanceSelectorPlusComparator(this.mContext, this.createParams("Person.Name", true, true));
    ArrayList containers = this.createInstanceContainers( //
        new ListValue(new IPrimitiveValue[] { //
            this.createPerson("Jon"), //
            this.createPerson("Friso1"), //
            this.createPerson("Ocker"), //
            this.createPerson("Friso2"), //
            this.createPerson(null), //
            this.createPerson(null) //
        }));

    Collections.sort(containers, comparator);
    this.logResult(containers);

    assertEquals("Friso1", ((Container) containers.get(0)).getEntityInstanceId().getName());
    assertEquals("Friso2", ((Container) containers.get(1)).getEntityInstanceId().getName());
    assertEquals("Jon", ((Container) containers.get(2)).getEntityInstanceId().getName());
    assertEquals("Ocker", ((Container) containers.get(3)).getEntityInstanceId().getName());
    assertEquals(null, ((Container) containers.get(4)).getEntityInstanceId().getName());
    assertEquals(null, ((Container) containers.get(5)).getEntityInstanceId().getName());
  }

  public void testSortExpressionDescending() throws Exception {
    InstanceSelectorPlusComparator comparator =
        new InstanceSelectorPlusComparator(this.mContext, this.createParams("Person.Name", false, false));
    ArrayList containers = this.createInstanceContainers(
        new ListValue(new IPrimitiveValue[] { this.createPerson("Jon"), this.createPerson("Friso1"),
            this.createPerson("Ocker"), this.createPerson("Friso2"), this.createPerson(null) }));
    Collections.sort(containers, comparator);
    this.logResult(containers);

    assertEquals("Ocker", ((Container) containers.get(0)).getEntityInstanceId().getName());
    assertEquals("Jon", ((Container) containers.get(1)).getEntityInstanceId().getName());
    assertEquals("Friso2", ((Container) containers.get(2)).getEntityInstanceId().getName());
    assertEquals("Friso1", ((Container) containers.get(3)).getEntityInstanceId().getName());
    assertEquals(null, ((Container) containers.get(4)).getEntityInstanceId().getName());
  }

  public void testInvalidExperssion() throws Exception {
    // this.enablePassMessages();
    try {
      new InstanceSelectorPlusComparator(this.mContext, this.createParams("BlaBla.Name", false, false));
      fail("Exception expected");
    } catch (IllegalArgumentException e) {
      // success, do nothing
    }

    InstanceSelectorPlusComparator comparator =
        new InstanceSelectorPlusComparator(this.mContext, this.createParams("Application.Name", false, false));
    ArrayList containers = this.createInstanceContainers(
        new ListValue(new IPrimitiveValue[] { this.createPerson("Jon"), this.createPerson("Friso1"),
            this.createPerson("Ocker"), this.createPerson("Friso2"), this.createPerson(null) }));
    try {
      Collections.sort(containers, comparator);
      fail("Exception expected");
    } catch (Exception e) {
      // success, do nothing
    }
  }

  /**
   * Test case trying to reproduce ims issue AQU-4606.
   * 
   * @throws Exception Unexpected exception
   */
  public void testSortIntegerAttributes() throws Exception {
    InstanceSelectorPlusComparator comparator =
        new InstanceSelectorPlusComparator(this.mContext, this.createParams("Person.Age", false, false));
    ArrayList containers = this.createInstanceContainers(
        new ListValue(new IPrimitiveValue[] { this.createPerson("Jan", 25), this.createPerson("Piet", 24),
            this.createPerson("Klaas", 22), this.createPerson("Kees", 23), this.createPerson("Hein", 35) }));
    Collections.sort(containers, comparator);
    this.logResult(containers);

    assertEquals("Hein", ((Container) containers.get(0)).getEntityInstanceId().getName());
    assertEquals("Jan", ((Container) containers.get(1)).getEntityInstanceId().getName());
    assertEquals("Piet", ((Container) containers.get(2)).getEntityInstanceId().getName());
    assertEquals("Kees", ((Container) containers.get(3)).getEntityInstanceId().getName());
    assertEquals("Klaas", ((Container) containers.get(4)).getEntityInstanceId().getName());
  }

  private void logResult(ArrayList result) throws Exception {
    LOG.info("result: ");
    for (Iterator iterator = result.iterator(); iterator.hasNext();) {
      Container container = (Container) iterator.next();
      LOG.info(container.getEntityInstanceId() + " - "
          + this.mContext.getProfile().getInstance(container.getEntityInstanceId()).getValue("Name"));
    }
  }

  private EntityValue createPerson(String name) throws Exception {
    return this.createPerson(name, -1);
  }

  /**
   * Create a person instance with a set Person.Name value.
   * 
   * @param name
   * @return
   * @throws Exception
   */
  private EntityValue createPerson(String name, int age) throws Exception {
    IEntityInstance instance = this.mContext.getProfile().createInstance("Person", null, name);
    if (name != null) {
      instance.setValue("Name", name);
    }
    if (age != -1) {
      instance.setValue("Age", new IntegerValue(age));
    }
    return instance.getInstanceReference();
  }

  /**
   * Create instance selector plus parameters.
   * 
   * @param sortExpression
   * @param ascending
   * @return
   */
  private InstanceSelectorPlusParameters createParams(String sortExpression, boolean ascending,
      boolean sortUnknownsAsLast) {
    Parameters parameters = new Parameters(true);
    parameters.setParameter("sortorder", ascending ? "ascending" : "descending");
    parameters.setParameter("sortattribute", sortExpression);
    if (sortUnknownsAsLast) {
      parameters.setParameter("sortunknownsaslast", "True");
    }
    return InstanceSelectorPlusParameters.getInstance(parameters);
  }

  /**
   * Create instance containers for every instance reference in the listvalue.
   * 
   * @param instances
   * @return
   * @throws Exception
   */
  private ArrayList createInstanceContainers(IListValue instances) throws Exception {
    ArrayList containers = new ArrayList(instances.getValueCount());
    for (int i = 0; i < instances.getValueCount(); i++) {
      LOG.info(instances.getValueAt(i) + " - "
          + this.mContext.getProfile().getInstance((EntityValue) instances.getValueAt(i)).getValue("Name"));
      containers.add(this.createInstanceContainer((EntityValue) instances.getValueAt(i)));
    }
    return containers;
  }

  /**
   * Create one instance container for an instance reference.
   * 
   * @param instanceRef
   * @return
   */
  private Container createInstanceContainer(EntityValue instanceRef) {
    Container result = new Container("test");
    result.setEntityContext(instanceRef);
    return result;
  }
}
