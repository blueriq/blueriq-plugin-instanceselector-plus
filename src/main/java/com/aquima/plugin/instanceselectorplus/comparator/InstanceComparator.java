package com.aquima.plugin.instanceselectorplus.comparator;

import com.aquima.interactions.foundation.IPrimitiveValue;
import com.aquima.interactions.foundation.exception.AppException;
import com.aquima.interactions.foundation.types.EntityValue;
import com.aquima.interactions.foundation.types.StringValue;
import com.aquima.interactions.framework.container.model.table.TableRow;
import com.aquima.interactions.profile.IProfile;
import com.aquima.plugin.instanceselectorplus.InstanceSelectorOrder;

import java.util.Comparator;

/**
 * This instance comparator will order the instances on the specified attribute from the Order object.
 * 
 * @author O. Kerpershoek
 * @since 5.0
 */
public class InstanceComparator implements Comparator // AQR-2993: not Serializable
{
  private final InstanceSelectorOrder mOrder;
  private final IProfile mProfile;

  /**
   * Constructs the comparator with the Order object that should be used.
   * 
   * @param order The order object containing the attribute that should be used.
   */
  public InstanceComparator(InstanceSelectorOrder order, IProfile profile) {
    this.mOrder = order;
    this.mProfile = profile;
  }

  @Override public int compare(Object firstObj, Object secondObj) {
    TableRow firstRow = (TableRow) firstObj;
    TableRow secondRow = (TableRow) secondObj;

    // FIX: AQU-4410/AQU-3596, made it possible to sort on attributes which are not displayed in the instance selector
    EntityValue firstEntityValue = (EntityValue) firstRow.getId();
    EntityValue secondEntityValue = (EntityValue) secondRow.getId();

    IPrimitiveValue value1;
    IPrimitiveValue value2;
    try {
      value1 = this.mProfile.getInstance(firstEntityValue).getValue(this.mOrder.getAttribute()).toSingleValue();
      value2 = this.mProfile.getInstance(secondEntityValue).getValue(this.mOrder.getAttribute()).toSingleValue();
    } catch (AppException e) {
      throw new IllegalArgumentException("Unable to order instances", e);
    }

    // sort string values case insensitive
    if (value1 instanceof StringValue && !value1.isUnknown()) {
      String value = value1.stringValue();
      value1 = new StringValue(value.toUpperCase());
    }
    if (value2 instanceof StringValue && !value2.isUnknown()) {
      String value = value2.stringValue();
      value2 = new StringValue(value.toUpperCase());
    }

    return this.mOrder.getOrder() == InstanceSelectorOrder.DESCENDING ? value2.compareTo(value1)
        : value1.compareTo(value2);
  }

}
