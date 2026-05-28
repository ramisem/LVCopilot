/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.DateTimeUtil;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Task
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53881 $";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        DataSet taskproperty = sdiData.getDataset("taskproperty");
        if (taskproperty == null) {
            taskproperty = new DataSet();
            sdiData.setDataset("taskproperty", taskproperty);
        }
        if (actionProps.getProperty("templatekeyid1").length() == 0 && actionProps.getProperty("templateid").length() == 0) {
            this.setScheduleDt(primary);
            for (int i = 0; i < primary.size(); ++i) {
                String actionid = primary.getString(i, "actionid");
                if (actionid == null || actionid.length() == 0) {
                    throw new SapphireException("Action not defined for task");
                }
                SafeSQL safeSQL = new SafeSQL();
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM actionproperty WHERE actionid = " + safeSQL.addVar(actionid) + " AND actionversionid = '1' ORDER BY usersequence, propertyid", safeSQL.getValues());
                for (int j = 0; j < ds.size(); ++j) {
                    int newrow = taskproperty.addRow();
                    taskproperty.setString(newrow, "taskid", primary.getString(i, "taskid"));
                    taskproperty.setString(newrow, "propertyid", ds.getString(j, "propertyid"));
                    taskproperty.setString(newrow, "extrapropertyflag", "N");
                }
            }
        } else {
            for (int i = 0; i < primary.size(); ++i) {
                primary.setValue(i, "csuflag", "");
            }
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.setScheduleDt(primary);
    }

    private void setScheduleDt(DataSet primary) {
        for (int i = 0; i < primary.size(); ++i) {
            String rule;
            if (primary.getCalendar(i, "planneddt") != null && primary.getCalendar(i, "scheduledt") == null) {
                primary.setDate(i, "scheduledt", primary.getCalendar(i, "planneddt"));
            }
            if ((rule = this.correctDateFormat(primary.getValue(i, "schedulerule", ""), this.getOldPrimaryValue(primary, i, "schedulerule"))) == null || rule.length() <= 0) continue;
            primary.setString(i, "schedulerule", rule);
            String[] ruleparts = StringUtil.split(rule, ";");
            String[] date = StringUtil.split(ruleparts[1], "/");
            String[] time = StringUtil.split(ruleparts[2], ":");
            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.set(Integer.parseInt(date[2]), Integer.parseInt(date[0]) - 1, Integer.parseInt(date[1]), Integer.parseInt(time[0]), Integer.parseInt(time[1]));
            primary.setDate(i, "scheduledt", cal);
        }
    }

    private String correctDateFormat(String rule, String oldRule) {
        StringBuffer modifiedRule = new StringBuffer();
        String returnValue = new String();
        if (rule != null && rule.length() > 0) {
            String[] ruleParts = rule.split(";", -1);
            String oldStartDate = "";
            String oldEndDate = "";
            if (oldRule.length() > 0) {
                String[] oldRuleParts = oldRule.split(";", -1);
                oldStartDate = oldRuleParts[1];
                oldEndDate = oldRuleParts[3];
            }
            DateTimeUtil dateutil = new DateTimeUtil(this.connectionInfo);
            if (!oldStartDate.equals(ruleParts[1])) {
                Calendar startdate = dateutil.getCalendar(ruleParts[1]);
                ruleParts[1] = startdate.get(2) + 1 + "/" + startdate.get(5) + "/" + startdate.get(1);
            }
            if (!oldEndDate.equals(ruleParts[3]) && ruleParts[3].contains("D")) {
                Calendar enddate = dateutil.getCalendar(ruleParts[3].split("D", -1)[1]);
                ruleParts[3] = "D" + (enddate.get(2) + 1) + "/" + enddate.get(5) + "/" + enddate.get(1);
            }
            for (int i = 0; i < ruleParts.length; ++i) {
                modifiedRule.append(";").append(ruleParts[i]);
            }
            returnValue = modifiedRule.substring(1);
        }
        return returnValue;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.checkCoreType(rsetid, "You cannot delete 'Core' or 'System' tasks", false);
    }

    @Override
    public boolean requiresEditDetailPrimary() {
        return true;
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.checkCoreType(sdiData);
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.checkCoreType(sdiData);
    }

    @Override
    public void preDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        this.checkCoreType(rsetid, "You cannot delete 'Core' task properties", true);
    }

    private void checkCoreType(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary == null) {
            throw new SapphireException("Primary dataset not available");
        }
        for (int i = 0; i < primary.size(); ++i) {
            if (!"C".equals(primary.getString(i, "csuflag"))) continue;
            throw new SapphireException("You cannot modify 'Core' tasks");
        }
    }

    private void checkCoreType(String rsetid, String message, boolean allowSystem) throws SapphireException {
        this.database.createPreparedResultSet("SELECT csuflag FROM task, rsetitems WHERE task.taskid = rsetitems.keyid1 AND rsetid = ?", new Object[]{rsetid});
        while (this.database.getNext()) {
            String csuflag = this.database.getString("csuflag");
            if (csuflag == null || (allowSystem || !csuflag.equals("S")) && !csuflag.equals("C")) continue;
            throw new SapphireException(message);
        }
    }
}

