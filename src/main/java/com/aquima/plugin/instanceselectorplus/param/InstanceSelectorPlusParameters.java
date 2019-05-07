package com.aquima.plugin.instanceselectorplus.param;

import com.aquima.interactions.foundation.IParameters;
import com.aquima.interactions.foundation.exception.InvalidStateException;
import com.aquima.interactions.foundation.text.StringUtil;
import com.aquima.interactions.foundation.types.IntegerValue;
import com.aquima.plugin.instanceselectorplus.InstanceSelectorOrder;

/**
 * This class parses the parameters needed for the instance selector plus container.
 * 
 * @author Jon van Leuven
 * @since 5.0
 */
public final class InstanceSelectorPlusParameters {
  private static final String PARAM_NOINSTANCECONTAINER = "noinstancecontainer";
  private static final String PARAM_ADDBUTTONS = "addbuttons";
  private static final String PARAM_ADDBUTTONSCONDITION = "addbuttoncondition";
  private static final String PARAM_ADDBUTTONSEVENTS = "addbuttonevents";
  private static final String PARAM_HEADERCONTAINER = "headercontainer";
  private static final String PARAM_WHERECLAUSE = "whereclause";
  private static final String PARAM_SORTATTRIBUTE = "sortattribute";
  private static final String PARAM_SORTORDER = "sortorder";
  private static final String PARAM_SORTUNKNOWNASLAST = "sortunknownsaslast";
  private static final String PARAM_ACTIONNOWSAVE = "directcreate";
  private static final String PARAM_REFERRER_PATH = "referrer-path";
  private static final String PARAM_ENTITY = "entity";

  /**
   * This method converts the parameters object passed to the container to an instance-selector parameters object.
   * 
   * @param parameters The parameters object that was passed to the container.
   * 
   * @return The parameters object for the instance selector.
   */
  public static InstanceSelectorPlusParameters getInstance(IParameters parameters) {
    if (parameters == null) {
      throw new IllegalArgumentException(
          "Invalid params (null) passed to constructor of InstanceSelectorPlusParameters");
    }
    String oldParameters = parameters.getParameter(PARAM_OLDPARAMETERS);
    if (StringUtil.notEmpty(oldParameters)) {
      return parseOldParameters(oldParameters);
    } else {
      return parseParameters(parameters);
    }
  }

  private static InstanceSelectorPlusParameters parseParameters(IParameters parameters) {
    String noInstancesContainer = "";
    String[] addButtons = new String[0];
    String[] addButtonEvents = new String[0];
    String addButtonCondition = "";
    String headerContainer = "";
    String filter = "";
    String sortAttribute = "";
    int sortOrder = InstanceSelectorOrder.ASCENDING;
    boolean actionNowSave = false;
    String referrerPath = null;
    String entityName = null;
    boolean unknownsAsLast = false;
    IntegerValue pagingSize = null;

    if (StringUtil.notEmpty(parameters.getParameter(PARAM_NOINSTANCECONTAINER))) {
      noInstancesContainer = parameters.getParameter(PARAM_NOINSTANCECONTAINER);
    }
    if (StringUtil.notEmpty(parameters.getParameter(PARAM_ADDBUTTONS))) {
      addButtons = StringUtil.split(parameters.getParameter(PARAM_ADDBUTTONS), ",");
    }
    if (StringUtil.notEmpty(parameters.getParameter(PARAM_ADDBUTTONSEVENTS))) {
      addButtonEvents = StringUtil.split(parameters.getParameter(PARAM_ADDBUTTONSEVENTS), ",");
    }
    if (StringUtil.notEmpty(parameters.getParameter(PARAM_ADDBUTTONSCONDITION))) {
      addButtonCondition = parameters.getParameter(PARAM_ADDBUTTONSCONDITION);
    }
    if (StringUtil.notEmpty(parameters.getParameter(PARAM_HEADERCONTAINER))) {
      headerContainer = parameters.getParameter(PARAM_HEADERCONTAINER);
    }
    if (StringUtil.notEmpty(parameters.getParameter(PARAM_WHERECLAUSE))) {
      filter = parameters.getParameter(PARAM_WHERECLAUSE);
    }
    if (StringUtil.notEmpty(parameters.getParameter(PARAM_SORTATTRIBUTE))) {
      sortAttribute = parameters.getParameter(PARAM_SORTATTRIBUTE);
    }
    if (StringUtil.notEmpty(parameters.getParameter(PARAM_SORTORDER))) {
      sortOrder = parameters.getParameter(PARAM_SORTORDER).equals("ascending")
          || parameters.getParameter(PARAM_SORTORDER).equals("1") ? InstanceSelectorOrder.ASCENDING
              : InstanceSelectorOrder.DESCENDING;
    }
    if (StringUtil.notEmpty(parameters.getParameter(PARAM_ACTIONNOWSAVE))) {
      actionNowSave = parameters.getParameter(PARAM_ACTIONNOWSAVE).equalsIgnoreCase("TRUE");
    }
    if (StringUtil.notEmpty(parameters.getParameter(PARAM_REFERRER_PATH))) {
      referrerPath = parameters.getParameter(PARAM_REFERRER_PATH);
    }
    if (StringUtil.notEmpty(parameters.getParameter(PARAM_ENTITY))) {
      entityName = parameters.getParameter(PARAM_ENTITY);
    }
    if (StringUtil.notEmpty(parameters.getParameter(PARAM_SORTUNKNOWNASLAST))) {
      unknownsAsLast = parameters.getParameter(PARAM_SORTUNKNOWNASLAST).equalsIgnoreCase("TRUE");
    }

    return new InstanceSelectorPlusParameters(noInstancesContainer, addButtons, addButtonEvents, addButtonCondition,
        headerContainer, entityName, referrerPath, filter, sortAttribute, sortOrder, actionNowSave, unknownsAsLast);
  }

  private static InstanceSelectorPlusParameters parseOldParameters(String oldParameter) {
    String noInstancesContainer = "";
    String[] addButtons = new String[0];
    String addButtonCondition = "";
    String headerContainer = "";
    String selectionExpression = "";
    String sortColumn = "";
    int sortOrder = InstanceSelectorOrder.ASCENDING;
    boolean actionNowSave = false;

    String[] tokens = StringUtil.split(oldParameter, "|");
    int idx = 0;
    if (oldParameter != null) {
      if (idx < tokens.length) {
        noInstancesContainer = tokens[idx++].trim();
      }
      if (idx < tokens.length) {
        addButtons = StringUtil.split(tokens[idx++].trim(), ",");
      }
      if (idx < tokens.length) {
        headerContainer = tokens[idx++].trim();
      }
      if (idx < tokens.length) {
        selectionExpression = tokens[idx++].trim();
      }
      if (idx < tokens.length) {
        String[] orders = StringUtil.split(tokens[idx++], "=");
        sortColumn = orders[0];
        if (orders.length > 1) {
          try {
            // <java>
            sortOrder = Integer.parseInt(orders[1], 10);
            // </java>
            // @formatter:off special java -> .Net conversion
            /* <cs>
            sortOrder = System.Int32.Parse(orders[1], System.Globalization.CultureInfo.InvariantCulture);
            </cs> */
            // @formatter:on special java -> .Net conversion

          } catch (NumberFormatException ex) {
            throw new InvalidStateException(
                "Could not determine sort order for: " + orders[1] + ", message: " + ex.getMessage(), ex);
          }
        }
      }
      if (idx < tokens.length) {
        addButtonCondition = tokens[idx++].trim();
      }
      if (idx < tokens.length) {
        String token = tokens[idx++].trim();
        Boolean now = Boolean.valueOf(token.substring(token.indexOf('=') + 1));
        actionNowSave = now.booleanValue();
      }
    }
    return new InstanceSelectorPlusParameters(noInstancesContainer, addButtons, null, addButtonCondition,
        headerContainer, null, "", selectionExpression, sortColumn, sortOrder, actionNowSave, false);
  }

  // implementation

  private final String mNoInstancesContainer;
  private final String[] mAddButtons;
  private final String[] mAddButtonEvents;
  private final String mAddButtonCondition;
  private final String mHeaderContainer;
  private final String mFilterExpression;
  private final String mSortAttribute;
  private final int mSortOrder;
  private final boolean mActionNowSave;
  private final String mReferrerPath;
  private final String mEntityName;
  private final boolean mSortUnknownAsLast;

  private static final String PARAM_OLDPARAMETERS = "oldParameters";

  // CHECKSTYLE:OFF
  // Ugly constructor, but private, so who cares...
  private InstanceSelectorPlusParameters(String noInstancesContainer, String[] addButtons, String[] addButtonEvents,
      String addButtonCondition, String headerContainer, String entityName, String referrerPath,
      String filterExpression, String sortColumn, int sortOrder, boolean actionNowSave, boolean sortUnknownAsLast) {
    this.mNoInstancesContainer = noInstancesContainer;
    this.mAddButtons = addButtons;
    this.mAddButtonEvents = addButtonEvents;
    this.mAddButtonCondition = addButtonCondition;
    this.mHeaderContainer = headerContainer;
    this.mReferrerPath = referrerPath;
    this.mFilterExpression = filterExpression;
    this.mSortAttribute = sortColumn;
    this.mSortOrder = sortOrder;
    this.mActionNowSave = actionNowSave;
    this.mEntityName = entityName;
    this.mSortUnknownAsLast = sortUnknownAsLast;
  }

  // CHECKSTYLE:ON

  /**
   * This method returns an array containing the add buttons.
   * 
   * @return an array containing the add buttons.
   */
  public String[] getAddButtons() {
    return (this.mAddButtons != null ? this.mAddButtons : new String[0]);
  }

  public String getAddButtonEvent(int buttonIndex) {
    if (this.mAddButtonEvents == null || buttonIndex > this.mAddButtonEvents.length - 1) {
      return null;
    }
    return (this.mAddButtonEvents[buttonIndex]);
  }

  /**
   * This method returns the value for the 'add-button-condition' parameter.
   * 
   * @return the value for the 'addbuttoncondition' parameter.
   */
  public String getAddButtonCondition() {
    return (this.mAddButtonCondition);
  }

  /**
   * This method returns the name of the container that should be used as header.
   * 
   * @return the name of the container that should be used as header.
   */
  public String getHeaderContainer() {
    return this.mHeaderContainer;
  }

  /**
   * This method returns a boolean indicating if the header container has been specified.
   * 
   * @return a boolean indicating if the header container has been specified.
   */
  public boolean hasHeaderContainer() {
    return this.mHeaderContainer.trim().length() > 0;
  }

  /**
   * This method returns the name of the container that should be used when no instances are available.
   * 
   * @return the name of the container that should be used when no instances are available.
   */
  public String getNoInstancesContainer() {
    return this.mNoInstancesContainer;
  }

  /**
   * This method returns the name of the entity whose instances should be selected.
   * 
   * @return the name of the entity whose instances should be selected.
   */
  public String getEntityName() {
    return (this.mEntityName);
  }

  /**
   * This method returns the expression that should be used to sort the instances on.
   * 
   * @return the attribute name that should be used to sort the instances on.
   */
  public String getSortExpression() {
    return this.mSortAttribute;
  }

  /**
   * This method returns an integer indicating the sort order.
   * 
   * @return an integer indicating the sort order.
   */
  public int getSortOrder() {
    return this.mSortOrder;
  }

  /**
   * This method returns an expression that should be used to check if an instance should be included in the list.
   * 
   * @return an expression that should be used to check if an instance should be included in the list.
   */
  public String getFilterExpression() {
    return this.mFilterExpression;
  }

  /**
   * This method returns a boolean indicating if the save action should directly update the instance.
   * 
   * @return a boolean indicating if the save action should directly update the instance.
   */
  public boolean isInstanceActionNowForSave() {
    return this.mActionNowSave;
  }

  /**
   * This method returns the relation attribute that was used to refer to the container.
   * 
   * @return the relation attribute that was used to refer to the container.
   */
  public String getReferrerPath() {
    return (this.mReferrerPath);
  }

  /**
   * This method returns a boolean indicating if the the instances with a sort key that result in unknown must be sorted
   * at the end of the instance list.
   * 
   * @return a boolean indicating if unknowns must be sorted at the end of the instance list.
   */
  public boolean isSortUnknownAsLast() {
    return this.mSortUnknownAsLast;
  }
}
