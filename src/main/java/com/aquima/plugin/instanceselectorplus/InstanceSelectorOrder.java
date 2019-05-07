package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.composer.model.Element;
import com.aquima.interactions.foundation.DataType;
import com.aquima.interactions.foundation.utility.EqualsUtil;

/**
 * This class is used to specify the sort order.
 * 
 * @author O. Kerpershoek
 * @since 5.0
 */
public class InstanceSelectorOrder extends Element {
  private static final long serialVersionUID = 1L;

  /**
   * This member defines the ASCENDING sort order.
   */
  public static final int ASCENDING = 0;

  /**
   * This member defines the DESCENDING sort order.
   */
  public static final int DESCENDING = 1;

  private String mAttribute;
  private final int mOrder;
  private final DataType mDataType;
  private int mColumnNr = -1;

  /**
   * Constructs the class with the required parameters.
   * 
   * @param attribute The attribute that should be used to sort on.
   * @param order The direction of the sorting.
   * @param dataType The type of the attribute.
   * @param columnNr The column number.
   */
  public InstanceSelectorOrder(String attribute, int order, DataType dataType, int columnNr) {
    super("order");

    this.mAttribute = attribute;
    this.mOrder = order;
    this.mDataType = dataType;
    this.mColumnNr = columnNr;
  }

  public Element duplicate() {
    return new InstanceSelectorOrder(this.mAttribute, this.mOrder, this.mDataType, this.mColumnNr);
  }

  /**
   * This method returns the attribute that should be used for sorting.
   * 
   * @return the attribute that should be used for sorting.
   */
  public String getAttribute() {
    return this.mAttribute;
  }

  /**
   * This method returns an integer indicating the direction of the sorting.
   * 
   * @return an integer indicating the direction of the sorting.
   */
  public int getOrder() {
    return this.mOrder;
  }

  /**
   * This method returns the type of the attribute.
   * 
   * @return the type of the attribute.
   */
  public DataType getDataType() {
    return this.mDataType;
  }

  /**
   * This method returns the column number containing the attribute.
   * 
   * @return the column number containing the attribute.
   */
  public int getColumnNr() {
    return this.mColumnNr;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer(64);

    buffer.append("[Order column=");
    buffer.append(this.mColumnNr);
    buffer.append(" attribute=");
    buffer.append(this.mAttribute);
    buffer.append(" direction=");
    buffer.append(this.mOrder == ASCENDING ? "ascending" : "descending");
    buffer.append(']');

    return (buffer.toString());
  }

  public boolean equals(Object other) {
    if (other instanceof InstanceSelectorOrder) {
      InstanceSelectorOrder o = (InstanceSelectorOrder) other;
      return super.equals(other) && this.getOrder() == o.getOrder()
          && EqualsUtil.equals(this.getAttribute(), o.getAttribute())
          && EqualsUtil.equals(this.getDataType(), o.getDataType()) && this.getColumnNr() == o.getColumnNr();
    }
    return false;
  }

  public int hashCode() {
    return super.getName().hashCode();
  }
}
