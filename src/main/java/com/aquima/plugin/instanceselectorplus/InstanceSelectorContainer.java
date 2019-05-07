package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.composer.ICompositeElement;
import com.aquima.interactions.composer.IElement;
import com.aquima.interactions.composer.IField;
import com.aquima.interactions.composer.IVisitor;
import com.aquima.interactions.composer.model.Container;
import com.aquima.interactions.composer.model.Element;
import com.aquima.interactions.foundation.exception.AppException;
import com.aquima.interactions.foundation.logging.LogFactory;
import com.aquima.interactions.foundation.logging.Logger;
import com.aquima.interactions.foundation.types.EntityValue;
import com.aquima.interactions.foundation.utility.EqualsUtil;
import com.aquima.interactions.framework.container.model.table.Table;
import com.aquima.interactions.framework.container.model.table.TableRow;
import com.aquima.interactions.profile.IProfile;
import com.aquima.plugin.instanceselectorplus.comparator.DefaultInstanceComparator;
import com.aquima.plugin.instanceselectorplus.comparator.InstanceComparator;

/**
 * Container class used by the instance selectors to hold the instance table.
 * 
 * @author Jon van Leuven
 * @since 5.0
 */
public class InstanceSelectorContainer extends Table {
  private static final Logger LOG = LogFactory.getLogger(InstanceSelectorContainer.class);

  private InstanceSelectorOrder mOrder;
  private final String mType;

  /**
   * Constructs the instance selector container with the required parameters.
   * 
   * @param columns The number of column that should be shown.
   * @param basedOn The container that should be used copy the definition from.
   * @param type The entity type for which the selector is created.
   */
  public InstanceSelectorContainer(int columns, Container basedOn, String type) {
    super(columns, basedOn);
    this.mType = type;
  }

  protected InstanceSelectorContainer(InstanceSelectorContainer other) {
    super(other);

    this.mOrder = other.getOrder();
    this.mType = other.getType();
  }

  @Override
  public Element duplicate() {
    return new InstanceSelectorContainer(this);
  }

  /**
   * <p>
   * This method can be used to specify the sort order.
   * </p>
   * <p>
   * Use the sortRows method to actually sort the rows in this container.
   * </p>
   * 
   * @param order The sort order for the instance selector.
   */
  public void setOrder(InstanceSelectorOrder order) {
    if (order == null) {
      throw new IllegalArgumentException("Invalid order object passed to setOrder method");
    }

    this.mOrder = order;

    if (LOG.isDebugEnabled()) {
      LOG.debug("[setOrder] for " + super.getName() + " to: " + order);
    }
  }

  /**
   * This method sorts the rows that are available in the instance selector container.
   * 
   * @param profile The profile values that are used for sorting the rows, may not be null.
   */
  public void sortRows(IProfile profile) {
    if (this.mOrder == null) {
      super.sortRows(new DefaultInstanceComparator());
    } else {
      super.sortRows(new InstanceComparator(this.mOrder, profile));
    }
  }

  /**
   * This method returns the sort order of the instance selector.
   * 
   * @return the sort order of the instance selector.
   */
  public InstanceSelectorOrder getOrder() {
    return this.mOrder;
  }

  /**
   * This method returns the entity type for which the selector is defined.
   * 
   * @return the entity type for which the selector is defined.
   */
  public String getType() {
    return this.mType;
  }

  /**
   * This method may be used to add an instance row.
   * 
   * @param instance The ID of the instance.
   * @param fields The values for the columns.
   */
  public void addInstance(EntityValue instance, IField[] fields) {
    TableRow row = new TableRow(instance, fields);
    row.setEntityContext(instance);
    this.addRow(row);
  }

  /**
   * This method may be used to add an instance row.
   * 
   * @param entityContext The ID of the instance.
   * @param containment The values for the columns.
   */
  public void addInstance(EntityValue entityContext, IElement[] containment) {
    if (entityContext == null) {
      throw new IllegalArgumentException("Error adding instance to instanceSelector, entityContext cannot be null");
    }
    TableRow row = new TableRow(entityContext, containment);
    row.setEntityContext(entityContext);
    this.addRow(row);
  }

  /**
   * This method may be used to add a row to the instance selector table.
   * 
   * @param row The row that should be added.
   */
  @Override
  public void addRow(TableRow row) {
    if (!(row.getId() instanceof EntityValue)) {
      throw new IllegalArgumentException("Error adding row to instance selector: id of row must be an EntityValue");
    }
    if (row.getEntityInstanceId() == null) {
      throw new IllegalArgumentException("Error adding row to instance selector: row must have an entity-context");
    }

    super.addRow(row);
  }

  @Override
  public IVisitor accept(IVisitor visitor) throws AppException {
    IVisitor childVisitor = visitor.accept(this);
    if (childVisitor != null) {
      // AQR-1011: Page element ids are not generated for instanceselector headers when there are no instances
      if (this.getHeader() != null) {
        this.getHeader().accept(childVisitor);
      }
      if (this.getRowCount() > 0) {

        if (this.mOrder != null) {
          this.mOrder.accept(childVisitor);
        }

        TableRow[] rows = this.getRows();

        for (int i = 0; i < this.getRowCount(); i++) {
          rows[i].accept(childVisitor);
        }
      }

      IElement[] children = super.getElements();

      for (int index = 0; index < children.length; index++) {
        Element child = (Element) children[index];

        child.accept(childVisitor);
      }

      visitor.leave(this, childVisitor);
    }

    return visitor;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof InstanceSelectorContainer) {
      InstanceSelectorContainer s = (InstanceSelectorContainer) other;
      return super.equals(other) && EqualsUtil.equals(this.getOrder(), s.getOrder())
          && EqualsUtil.equals(this.getType(), s.getType());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.getName().hashCode();
  }

  @Override
  public boolean equalsContent(ICompositeElement other) {
    if (other instanceof InstanceSelectorContainer) {
      InstanceSelectorContainer s = (InstanceSelectorContainer) other;

      return super.equalsContent(s);
    }
    return false;
  }
}
