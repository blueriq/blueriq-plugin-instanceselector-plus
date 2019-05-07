package com.aquima.plugin.instanceselectorplus.handle;

import com.aquima.interactions.composer.IElement;
import com.aquima.interactions.foundation.exception.AppException;
import com.aquima.interactions.foundation.types.EntityValue;
import com.aquima.interactions.portal.ICallbackFunction;
import com.aquima.interactions.portal.IContainerEventContext;
import com.aquima.interactions.profile.IEntityInstance;

/**
 * This call-back function is used to activate an instance when a button is clicked.
 * 
 * @author Jon van Leuven
 * @since 5.0
 */
public class ActivateInstanceCallBack implements ICallbackFunction {
  private final EntityValue mInstanceReference;

  /**
   * Constructs the callback with the instance ID of the instance that should be activated.
   * 
   * @param instance the instance ID of the instance that should be activated.
   */
  public ActivateInstanceCallBack(EntityValue instance) {
    this.mInstanceReference = instance;
  }

  public void handle(IElement pressedButton, IContainerEventContext context) throws AppException {
    IEntityInstance instance = context.getProfile().getInstance(this.mInstanceReference);
    context.pushActiveInstance(instance);
  }

  public int hashCode() {
    return this.mInstanceReference.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof ActivateInstanceCallBack) {
      ActivateInstanceCallBack other = (ActivateInstanceCallBack) obj;

      return this.mInstanceReference.equals(other.mInstanceReference);
    } else {
      return false;
    }
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer(64);

    buffer.append('[');
    buffer.append(this.getClass().getName());
    buffer.append(" instance=");
    buffer.append(this.mInstanceReference);
    buffer.append(']');

    return buffer.toString();
  }
}
