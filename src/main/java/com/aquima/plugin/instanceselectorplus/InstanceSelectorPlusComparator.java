package com.aquima.plugin.instanceselectorplus;

import com.aquima.interactions.composer.model.Container;
import com.aquima.interactions.foundation.IPrimitiveValue;
import com.aquima.interactions.foundation.exception.AppException;
import com.aquima.interactions.foundation.exception.InvalidStateException;
import com.aquima.interactions.foundation.exception.SysException;
import com.aquima.interactions.foundation.logging.LogFactory;
import com.aquima.interactions.foundation.logging.Logger;
import com.aquima.interactions.foundation.text.StringUtil;
import com.aquima.interactions.portal.IPortalContext;
import com.aquima.interactions.profile.IEntityInstance;
import com.aquima.interactions.rule.IExpression;
import com.aquima.interactions.rule.exception.RuleEngineParseException;
import com.aquima.interactions.rule.inference.InstanceContext;
import com.aquima.plugin.instanceselectorplus.param.InstanceSelectorPlusParameters;

import java.util.Comparator;

/**
 * Internal class to sort the instances of the instance selector plus.
 * 
 * @author Jon van Leuven
 * @since 6.1
 */
public class InstanceSelectorPlusComparator implements Comparator {
  private static final Logger LOG = LogFactory.getLogger(InstanceSelectorPlusComparator.class);
  private final IPortalContext mContext;
  private final IExpression mExpression;
  private final boolean mIsAscending;
  private final boolean mUnknownsAsLast;

  /**
   * Construct a comparator for specific context and parameter.
   * 
   * @param ctx The portal context.
   * @param parameters The instance selector parameters.
   */
  public InstanceSelectorPlusComparator(IPortalContext ctx, InstanceSelectorPlusParameters parameters) {
    this.mContext = ctx;
    try {
      this.mExpression = StringUtil.isEmpty(parameters.getSortExpression()) ? null
          : ctx.getExpressionParser().expressionFor(parameters.getSortExpression());
    } catch (RuleEngineParseException e) {
      throw new IllegalArgumentException("Unable to parse sort expression '" + parameters.getSortExpression() + "'", e);
    }
    this.mIsAscending = parameters.getSortOrder() == InstanceSelectorOrder.ASCENDING;
    this.mUnknownsAsLast = parameters.isSortUnknownAsLast();
    if (LOG.isDebugEnabled()) {
      LOG.debug("sort expression: '" + this.mExpression + (this.mIsAscending ? "' (ascending)" : "' (descending)"));
    }
  }

  @Override
  public int compare(Object arg0, Object arg1) {
    try {
      if (arg0 == arg1) {
        return 0;
      }
      Container first = (Container) arg0;
      Container second = (Container) arg1;

      IEntityInstance instance1 = this.mContext.getProfile().getInstance(first.getEntityInstanceId());
      IEntityInstance instance2 = this.mContext.getProfile().getInstance(second.getEntityInstanceId());

      if (this.mExpression == null) {
        return this.sortByInstance(first, second);
      }

      IPrimitiveValue value1 = this.evaluateSortExpression(instance1);
      IPrimitiveValue value2 = this.evaluateSortExpression(instance2);

      if (this.mUnknownsAsLast && (value1.isUnknown() || value2.isUnknown())) {
        if (value1.isUnknown() && value2.isUnknown()) {
          return this.sortByInstance(first, second);
        }
        return value1.isUnknown() ? 1 : -1;
      }
      int result = value2.compareTo(value1);
      if (result == 0) {
        return this.sortByInstance(first, second);
      }
      return this.useDirection(result);
    } catch (AppException e) {
      throw new SysException(e);
    }
  }

  private int sortByInstance(Container first, Container second) {
    return this.useDirection(first.getEntityInstanceId().compareTo(second.getEntityInstanceId()));
  }

  private int useDirection(int compare) {
    return (this.mIsAscending ? -1 * compare : compare);
  }

  private IPrimitiveValue evaluateSortExpression(IEntityInstance instance) {
    try {
      return this.mExpression.evaluateWith(new InstanceContext(this.mContext.getProfile(), instance)).toSingleValue();
    } catch (AppException e) {
      throw new InvalidStateException("Unable to evaluate sort expression '" + this.mExpression + "'", e);
    }
  }
}
