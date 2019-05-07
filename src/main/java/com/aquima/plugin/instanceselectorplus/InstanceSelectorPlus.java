package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.composer.IAsset;
import com.aquima.interactions.composer.IContainer;
import com.aquima.interactions.composer.IElement;
import com.aquima.interactions.composer.IField;
import com.aquima.interactions.composer.exception.UnknownElementException;
import com.aquima.interactions.composer.model.Asset;
import com.aquima.interactions.composer.model.Button;
import com.aquima.interactions.composer.model.Container;
import com.aquima.interactions.composer.model.ContentStyle;
import com.aquima.interactions.composer.model.HiddenElement;
import com.aquima.interactions.composer.model.definition.ContainerDefinition;
import com.aquima.interactions.foundation.IPrimitiveValue;
import com.aquima.interactions.foundation.IValue;
import com.aquima.interactions.foundation.exception.AppException;
import com.aquima.interactions.foundation.text.MultilingualText;
import com.aquima.interactions.foundation.text.StringUtil;
import com.aquima.interactions.foundation.types.BooleanValue;
import com.aquima.interactions.foundation.types.EntityValue;
import com.aquima.interactions.framework.container.instance.InstanceSelectorDefines;
import com.aquima.interactions.framework.container.model.table.TableHeader;
import com.aquima.interactions.framework.util.InstanceSelection;
import com.aquima.interactions.portal.IContainerContext;
import com.aquima.interactions.portal.IContainerExpander;
import com.aquima.interactions.portal.IElementComposer;
import com.aquima.interactions.portal.util.InstanceLocator;
import com.aquima.interactions.profile.IAttributeValue;
import com.aquima.interactions.profile.IEntityInstance;
import com.aquima.interactions.profile.ValueReference;
import com.aquima.interactions.rule.ICondition;
import com.aquima.plugin.instanceselectorplus.param.InstanceSelectorPlusParameters;

import com.blueriq.component.api.annotation.AquimaExpander;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Instance selector Plus dynamic container.
 * 
 * @see com.aquima.plugin.instanceselectorplus.param.InstanceSelectorPlusParameters
 * @author Jon van Leuven
 * @since 5.0
 */
@AquimaExpander("AQ_InstanceSelectorPlus")
public class InstanceSelectorPlus implements IContainerExpander {
  private static final String INSTANCEACTION_NOW = "instanceaction_now";

  @Override
  public Container expand(Container container, ContainerDefinition definition, IContainerContext context)
      throws AppException {

    if (container.getContentStyle().equals(ContainerDefinition.DEFAULT_CONTAINER_CONTENT_STYLE)) {
      container.setContentStyle(ContentStyle.valueOf("instance_selector"));
    }

    InstanceSelectorPlusParameters parameters = InstanceSelectorPlusParameters.getInstance(context.getParameters());
    int columns = definition.getContainmentCount();

    InstanceSelectorContainer selectorContainer =
        new InstanceSelectorContainer(columns, container, "instanceselectorplus");

    IElementComposer elementComposer = context.getElementComposer();
    IEntityInstance[] instances = this.getInstances(context, parameters);

    boolean showResult = ((instances.length > 0) || StringUtil.isEmpty(parameters.getNoInstancesContainer()));

    if (showResult) {
      // header
      if (StringUtil.notEmpty(parameters.getHeaderContainer())) {
        Container headerContainer = elementComposer.expandContainer(parameters.getHeaderContainer());

        Collection headerCells = new ArrayList(16);
        for (int i = 0; i < headerContainer.getElementCount(); i++) {
          IElement element = headerContainer.getElement(i);
          if (element instanceof IAsset) {
            headerCells.add(element);
          } else if (element instanceof IField) {
            headerCells.add(new Asset("header_" + i, "header", ((IField) element).getQuestionText()));
          } else if (element instanceof IContainer) {
            headerCells.add(new Asset("header_" + i, "header", ((IContainer) element).getDisplayText()));
          } else if (element instanceof HiddenElement) { // AQU-4186
            headerCells.add(element);
          } else {
            headerCells.add(new Asset("header_" + i, "header", new MultilingualText("")));
          }
        }
        if (headerCells.size() > 0) {
          Serializable[] headersArray = (Serializable[]) headerCells.toArray(new Serializable[headerCells.size()]);
          TableHeader tableHeader = new TableHeader(headersArray);
          selectorContainer.setHeader(tableHeader);
        }
      }

      // instances
      for (int i = 0; i < instances.length; i++) {
        IEntityInstance instance = instances[i];
        IElementComposer instanceComposer = elementComposer.forInstance(instance);

        Container instanceRow = new Container("instance_" + i);

        instanceRow.addElements(instanceComposer.expandChildren(definition));
        instanceRow.setEntityContext(instance.getInstanceReference());

        instanceRow.accept(new ButtonActivateInstanceVisitor(context, instance.getInstanceReference()));

        selectorContainer.addInstance(instance.getInstanceReference(), instanceRow.getElements());
      }
      // sort after all rows have been added (AQR-2993):
      selectorContainer.sortRows(new InstanceSelectorPlusComparator(context, parameters));
    } else {
      // no instances container
      Container noResult = elementComposer.expandContainer(parameters.getNoInstancesContainer());
      selectorContainer.addElement(noResult);
    }

    // buttons
    String[] addButtonStrings = parameters.getAddButtons();
    if (addButtonStrings != null && addButtonStrings.length > 0) {
      ICondition addButtonsCondition = null;
      if (StringUtil.notEmpty(parameters.getAddButtonCondition())) {
        addButtonsCondition = context.getExpressionParser().conditionFor(parameters.getAddButtonCondition());
      }
      if (addButtonsCondition == null
          || addButtonsCondition.evaluateConditionWith(context.getProfile(), BooleanValue.FALSE).booleanValue()) {
        ValueReference referrer = null;

        if (!StringUtil.isEmpty(parameters.getReferrerPath())) {
          InstanceLocator locator = new InstanceLocator(context.getProfile());
          IAttributeValue attributeValue = locator.getValueOf(parameters.getReferrerPath());
          referrer = attributeValue.getValueReference();
        }

        selectorContainer.addElement(this.addButtons(context, parameters, referrer));
      }
    }

    return selectorContainer;
  }

  private IEntityInstance[] getInstances(IContainerContext context, InstanceSelectorPlusParameters parameters)
      throws AppException {
    IEntityInstance[] instances = new IEntityInstance[0];

    String referrerPath = parameters.getReferrerPath();
    if (!StringUtil.isEmpty(referrerPath)) {

      InstanceLocator locator = new InstanceLocator(context.getProfile());
      IAttributeValue attributeValue = locator.getValueOf(referrerPath);

      IValue pathResult = attributeValue.getValue();

      if (!pathResult.isUnknown()) {
        IPrimitiveValue[] values = pathResult.toListValue().getValues();

        instances = new IEntityInstance[values.length];
        for (int i = 0; i < values.length; i++) {
          instances[i] = context.getProfile().getInstance((EntityValue) values[i]);
        }
      }
    } else {
      instances = context.getProfile().getAllInstancesForEntity(parameters.getEntityName(), true);
    }

    InstanceSelection selector = new InstanceSelection(context.getProfile(), context.getExpressionParser());

    // The relation attribute may contain instances that are not of the correct type when the relation
    // attribute is defined for a more generic entity type.
    // For instance a relation attribute for Person instances, while the instance selector is defined
    // for employee instances.
    return selector.filterInstances(instances, parameters.getFilterExpression(), parameters.getEntityName());
  }

  private Container addButtons(IContainerContext context, InstanceSelectorPlusParameters parameters,
      ValueReference referenceAttribute) throws UnknownElementException, AppException {
    Container buttonsContainer = new Container(InstanceSelectorDefines.BUTTONSCONTAINERNAME);

    String baseEntity = parameters.getEntityName();

    String[] addButtons = parameters.getAddButtons();
    for (int idx = 0; idx < addButtons.length; idx++) {
      Button button = this.addButton(context, parameters, addButtons[idx], referenceAttribute, baseEntity);
      if (StringUtil.notEmpty(parameters.getAddButtonEvent(idx))) {
        button.setEventName(parameters.getAddButtonEvent(idx));
      }
      buttonsContainer.addElement(button);
    }

    return (buttonsContainer);
  }

  private Button addButton(IContainerContext context, InstanceSelectorPlusParameters parameters, String buttonString,
      ValueReference referenceAttribute, String baseEntityType) throws UnknownElementException, AppException {
    String entityName = baseEntityType;
    String buttonName = null;
    if (buttonString.indexOf("|") != -1) {
      String[] s = StringUtil.split(buttonString, "|");
      entityName = s[1];
      buttonName = s[0];
    } else {
      // backwards compatible behaviour
      buttonName = buttonString;
    }
    Button button = context.getElementComposer().expandButton(buttonName);

    if (parameters.isInstanceActionNowForSave()) {
      button.addAction(INSTANCEACTION_NOW); // event
      button.addAction("_draft");
    }
    button.setEventName(InstanceSelectorDefines.EVENT_CREATE);
    button.addAction(InstanceSelectorDefines.ACTION_ADD_INSTANCE);
    button.addAction(
        new StringBuffer(InstanceSelectorDefines.ACTION_ADD_INSTANCE).append('_').append(entityName).toString());

    return (button);
  }
}
