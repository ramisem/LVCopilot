/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.misc;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.ArrayList;
import java.util.List;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;

public class WhoDoneIt {
    static final String LABVANTAGE_CVS_ID = "$Revision: 80518 $";
    public static final String OPERATOR_EQUAL = "=";
    public static final String OPERATOR_NOTEQUAL = "!=";
    public static final String MODE_NULLOUT = "nullout";
    public static final String MODE_CURRENT = "current";
    private List<ColumnPair> columnPairs = new ArrayList<ColumnPair>();
    private String dataSetName;

    public WhoDoneIt() {
        this.dataSetName = "primary";
    }

    public WhoDoneIt(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    public void addColumnPair(String dateField, String byField, String column, String conditionOperator, String conditionValue, String valueMode) {
        int operator = 0;
        if (OPERATOR_EQUAL.equals(conditionOperator)) {
            operator = 1;
        } else if (OPERATOR_NOTEQUAL.equals(conditionOperator)) {
            operator = 2;
        }
        int mode = 1;
        if (MODE_NULLOUT.equals(valueMode)) {
            mode = 2;
        } else if (MODE_CURRENT.equals(valueMode)) {
            mode = 1;
        }
        if (operator > 0 && column.length() > 0 && conditionValue.length() > 0) {
            this.columnPairs.add(new ColumnPair(dateField, byField, column, operator, conditionValue, mode));
        }
    }

    public void addColumnPair(String dateField, String byField, String column, String conditionOperator, String conditionValue) {
        int operator = 0;
        int mode = 1;
        if (OPERATOR_EQUAL.equals(conditionOperator)) {
            operator = 1;
            mode = 1;
        } else if (OPERATOR_NOTEQUAL.equals(conditionOperator)) {
            operator = 2;
            mode = 2;
        }
        if (operator > 0 && column.length() > 0 && conditionValue.length() > 0) {
            this.columnPairs.add(new ColumnPair(dateField, byField, column, operator, conditionValue, mode));
        }
    }

    public void process(DataSet primary, BaseSDCRules sdcRule) {
        for (int i = 0; i < primary.getRowCount(); ++i) {
            for (ColumnPair columnPair : this.columnPairs) {
                boolean doPopulate = false;
                switch (columnPair.conditionOperator) {
                    case 1: {
                        if (!columnPair.conditionValue.equals(primary.getValue(i, columnPair.column))) break;
                        doPopulate = true;
                        break;
                    }
                    case 2: {
                        if (!primary.isValidColumn(columnPair.column) || columnPair.conditionValue.equals(primary.getValue(i, columnPair.column)) || "Cancelled".equals(primary.getValue(i, columnPair.column))) break;
                        doPopulate = true;
                        break;
                    }
                }
                if (doPopulate) {
                    doPopulate = false;
                    if (this.dataSetName.equals("primary")) {
                        if (sdcRule.hasPrimaryValueChanged(primary, i, columnPair.column)) {
                            doPopulate = true;
                        }
                    } else if (this.dataSetName.equals("sdiworkitem")) {
                        if (sdcRule.hasSDIWorkItemValueChanged(primary, i, columnPair.column)) {
                            if ("starteddt".equals(columnPair.dateField)) {
                                if (OpalUtil.isEmpty(sdcRule.getOldSDIWorkItemValue(primary, i, "starteddt"))) {
                                    doPopulate = true;
                                }
                            } else {
                                doPopulate = true;
                            }
                        }
                    } else if (this.dataSetName.equals("dataset") && sdcRule.hasSDIDataValueChanged(primary, i, columnPair.column)) {
                        if ("starteddt".equals(columnPair.dateField)) {
                            if (OpalUtil.isEmpty(sdcRule.getOldSDIDataValue(primary, i, "starteddt"))) {
                                doPopulate = true;
                            }
                        } else {
                            doPopulate = true;
                        }
                    }
                }
                if (!doPopulate) continue;
                if (2 == columnPair.mode) {
                    if (columnPair.dateField != null) {
                        primary.setDate(i, columnPair.dateField, "(null)");
                    }
                    if (columnPair.byField == null) continue;
                    primary.setString(i, columnPair.byField, "");
                    continue;
                }
                if (1 != columnPair.mode) continue;
                if (columnPair.dateField != null) {
                    primary.setDate(i, columnPair.dateField, DateTimeUtil.getNowTimestamp());
                }
                if (columnPair.byField == null) continue;
                primary.setString(i, columnPair.byField, this.getSysUserId(sdcRule.getConnectionInfo()));
            }
        }
    }

    private String getSysUserId(ConnectionInfo connectionInfo) {
        return "(system)".equals(connectionInfo.getSysuserId()) ? "" : connectionInfo.getSysuserId();
    }

    private class ColumnPair {
        private String dateField;
        private String byField;
        private String column;
        private int conditionOperator;
        private String conditionValue;
        private int mode;
        private static final int OPERATOR_EQUAL = 1;
        private static final int OPERATOR_NOTEQUAL = 2;
        private static final int MODE_CURRENT = 1;
        private static final int MODE_NULLOUT = 2;

        public ColumnPair(String dateField, String byField, String column, int conditionOperator, String conditionValue, int mode) {
            this.dateField = dateField;
            this.byField = byField;
            this.column = column;
            this.conditionOperator = conditionOperator;
            this.conditionValue = conditionValue;
            this.mode = mode;
        }
    }
}

