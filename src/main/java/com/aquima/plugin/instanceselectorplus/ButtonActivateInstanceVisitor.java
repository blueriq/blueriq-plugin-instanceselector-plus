package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.composer.ICompositeElement;
import com.aquima.interactions.composer.IElement;
import com.aquima.interactions.composer.IVisitor;
import com.aquima.interactions.composer.model.Button;
import com.aquima.interactions.foundation.exception.AppException;
import com.aquima.interactions.foundation.text.StringUtil;
import com.aquima.interactions.foundation.types.EntityValue;
import com.aquima.interactions.framework.container.instance.InstanceSelectorDefines;
import com.aquima.interactions.portal.IContainerContext;

/**
 * This visitor iterates over the content of the container, and adds an action to all the buttons inside which will
 * activate the correct instance once clicked.
 * 
 * @author Jon van Leuven
 * @since 5.0
 */
public class ButtonActivateInstanceVisitor implements IVisitor {
  private final EntityValue mInstanceContext;

  protected ButtonActivateInstanceVisitor(IContainerContext context, EntityValue instanceId) {
    this.mInstanceContext = instanceId;
  }

  @Override public IVisitor accept(IElement element) throws AppException {
    if (element instanceof InstanceSelectorContainer) {
      // Do not visit nested instance selector containers
      // Issue: AQU-3062
      return null;
    }

    if (element instanceof Button) {
      Button button = (Button) element;
      boolean isDeleteButton = false;
      String[] actions = button.getActions();
      for (int i = 0; actions != null && i < actions.length; i++) {
        if (actions[i].equals(InstanceSelectorDefines.ACTION_DELETE_INSTANCE)) {
          isDeleteButton = true;
          break;
        }
      }

      // if ( ! this.mContext.isAutoInstanceActivationEnabled()) {
      // FIXME This behavior will no longer be needed for version 6.0, as the correct instance will automatically be
      // activated.
      // only activate the instance when the default activation is turned off
      // this.mContext.registerCallback(button, new ActivateInstanceCallBack(this.mInstanceContext),
      // InvocationType.ON_EVENT);
      // }

      if (isDeleteButton) {
        if (StringUtil.isEmpty(button.getEventName())) {
          button.setEventName(InstanceSelectorDefines.EVENT_DELETE);
        }
      } else {
        if (StringUtil.isEmpty(button.getEventName())) {
          button.setEventName(InstanceSelectorDefines.EVENT_EDIT);
        }
        button.addAction(new StringBuffer(InstanceSelectorDefines.ACTION_EDIT_INSTANCE).append('_')
            .append(this.mInstanceContext.getTypeName()).toString());
        button.addAction(InstanceSelectorDefines.ACTION_EDIT_INSTANCE);
      }
    }
    return this;
  }

  @Override public void leave(ICompositeElement container, IVisitor childVisitor) throws AppException {
    // void
  }
}
