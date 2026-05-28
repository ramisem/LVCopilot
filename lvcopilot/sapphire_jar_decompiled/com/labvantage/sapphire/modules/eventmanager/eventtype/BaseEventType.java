/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.Condition;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;

public abstract class BaseEventType
extends BaseCustom {
    private ErrorHandler errorHandler;
    protected FormatUtil formatUtil;
    protected M18NUtil m18n;
    protected SapphireConnection sapphireConnection;

    public abstract Class[] getEventObjectImplementations();

    public boolean requiresSupplementalData(DataSet conditionValues) {
        return false;
    }

    public boolean isSetupConditionsCascading() {
        return false;
    }

    public Condition getSetupCondition(int index, DataSet setupConditionValues) {
        return null;
    }

    public abstract Condition[] getSetupConditions(DataSet var1);

    public abstract Condition getFilterConditionTemplate(DataSet var1);

    public abstract boolean hasEventFired(BaseEventObject var1, DataSet var2);

    public abstract HashMap getProcessingInputs(BaseEventObject var1, String[] var2);

    public String getUserContextToken() {
        return "";
    }

    public static void addEventTypeConditions(DataSet sourceConditions, DataSet eventTypeConditions) {
        if (eventTypeConditions != null) {
            for (int i = eventTypeConditions.size() - 1; i >= 0; --i) {
                if (eventTypeConditions.getValue(i, "value1").length() <= 0 && eventTypeConditions.getValue(i, "value2").length() <= 0) continue;
                int row = sourceConditions.addRow(0);
                sourceConditions.setString(row, "conditionitem", eventTypeConditions.getValue(i, "conditionitem"));
                sourceConditions.setString(row, "operator1", eventTypeConditions.getValue(i, "operator1"));
                sourceConditions.setString(row, "value1", eventTypeConditions.getValue(i, "value1"));
                sourceConditions.setString(row, "operator2", eventTypeConditions.getValue(i, "operator2"));
                sourceConditions.setString(row, "value2", eventTypeConditions.getValue(i, "value2"));
            }
        }
    }

    void startEventType(SapphireConnection sapphireConnection, String loggerName) {
        this.sapphireConnection = sapphireConnection;
        this.setConnectionId(sapphireConnection.getConnectionId());
        this.logger.setLoggerName(loggerName);
        this.formatUtil = FormatUtil.getInstance(sapphireConnection);
        this.m18n = new M18NUtil(sapphireConnection);
    }

    void startEventType(SapphireConnection sapphireConnection, String loggerName, ErrorHandler errorHandler) {
        this.startEventType(sapphireConnection, loggerName);
        this.errorHandler = errorHandler != null ? errorHandler : new ErrorHandler();
    }

    void endEventType() throws SapphireException {
        if (this.errorHandler != null && this.errorHandler.size() > 0 && this.hasErrors()) {
            throw new SapphireException(this.errorHandler.getEncodedString());
        }
    }

    boolean hasErrors() {
        if (this.errorHandler != null && this.errorHandler.size() > 0) {
            for (int i = 0; i < this.errorHandler.size(); ++i) {
                ErrorDetail ruleError = (ErrorDetail)this.errorHandler.get(i);
                if (!ruleError.getErrorType().equals("VALIDATION") && !ruleError.getErrorType().equals("CONFIRM")) continue;
                return true;
            }
        }
        return false;
    }

    public static DataSet getConditionValues(DataSet eventTypeConditions, DataSet eventPlanConditions, boolean excludeEventSourceConditions) {
        int i;
        DataSet conditionValues = new DataSet();
        if (eventTypeConditions != null) {
            for (i = 0; i < eventTypeConditions.size(); ++i) {
                if (eventTypeConditions.getValue(i, "value1").length() <= 0 && eventTypeConditions.getValue(i, "value2").length() <= 0) continue;
                conditionValues.copyRow(eventTypeConditions, i, 1);
            }
        }
        if (eventPlanConditions != null) {
            for (i = 0; i < eventPlanConditions.size(); ++i) {
                if (excludeEventSourceConditions && (!excludeEventSourceConditions || eventPlanConditions.getValue(i, "conditionitem").equals("eventsource"))) continue;
                conditionValues.copyRow(eventPlanConditions, i, 1);
            }
        }
        return conditionValues;
    }
}

