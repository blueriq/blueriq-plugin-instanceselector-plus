package com.aquima.plugin.instanceselectorplus.comparator;

import com.aquima.interactions.foundation.types.EntityValue;
import com.aquima.interactions.framework.container.model.table.TableRow;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This comparator class is used by default when no order has been specified in the instance selector. The comparator
 * will order the instances on ID, forcing newly created instances to be shown last.
 * 
 * @author O. Kerpershoek
 * @since 5.1
 */
public class DefaultInstanceComparator implements Serializable, Comparator {

  private static final long serialVersionUID = 6931937267846515954L;

  @Override
  public int compare(Object firstObj, Object secondObj) {
    TableRow firstRow = (TableRow) firstObj;
    TableRow secondRow = (TableRow) secondObj;

    if (firstRow == secondRow) {
      return 0;
    }

    EntityValue firstEntity = firstRow.getEntityInstanceId();
    EntityValue secondEntity = secondRow.getEntityInstanceId();

    if (firstEntity.getSequenceNr() < secondEntity.getSequenceNr()) {
      return (-1);
    } else if (firstEntity.getSequenceNr() > secondEntity.getSequenceNr()) {
      return 1;
    } else {
      return 0;
    }
  }
}
