/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class TaskRO
extends BaseSDCRO {
    public void initialize(SapphireConnection sapphireConnection) throws SapphireException {
        super.initialize("Task", sapphireConnection);
    }

    public String getTaskId() {
        return this.getKeyid1();
    }

    public String getTaskDesc() {
        return this.getDescription();
    }

    public String getTaskActionId() {
        return this.getPrimaryValue("actionid");
    }

    public String getTaskActionVersionId() {
        String ret = this.getPrimaryValue("actionversionid");
        if (ret.length() == 0) {
            return "1";
        }
        return ret;
    }

    public String getTaskStatus() {
        String colVal = this.getPrimaryValue("activeflag");
        if ("Y".equals(colVal)) {
            return "Active";
        }
        return "Not Active";
    }

    public String getTaskScheduleType() {
        String colValue = this.getPrimaryValue("tasktypeflag");
        if ("R".equals(colValue)) {
            return "Recurring";
        }
        if ("P".equals(colValue)) {
            return "Scheduled";
        }
        return "Unscheduled";
    }

    public String getDeleteWhenFinished() {
        String colValue = this.getPrimaryValue("deletetaskflag");
        if ("Y".equals(colValue)) {
            return "Yes";
        }
        return "No";
    }

    public String getTaskType() {
        String colValue = this.getPrimaryValue("csuflag");
        if ("C".equals(colValue)) {
            return "Core";
        }
        if ("S".equals(colValue)) {
            return "System";
        }
        return "User";
    }

    public DataSet getTaskProperties() {
        DataSet ds = this.getDataSet("taskproperty");
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Property ID", 0);
        ret.addColumn("Property Value", 0);
        ret.addColumn("Extra Property", 0);
        ret.addColumnValues("Property ID", 0, ds.getColumnValues("propertyid", ";"), ";");
        ret.addColumnValues("Property Value", 0, ds.getColumnValues("propertyvalue", ";"), ";");
        String[] extraPropsVals = StringUtil.split(ds.getColumnValues("extrapropertyflag", ";"), ";");
        for (int i = 0; i < extraPropsVals.length; ++i) {
            String val = "";
            val = "Y".equals(extraPropsVals[i]) ? "Yes" : "No";
            ret.setString(i, "Extra Property", val);
        }
        return ret;
    }

    public DataSet getTaskCategories() {
        DataSet ds = this.getCategories();
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Category ID", 0);
        ret.addColumnValues("Category ID", 0, ds.getColumnValues("categoryid", ";"), ";");
        return ret;
    }

    public String getScheduleRule() {
        return this.getPrimaryValue("schedulerule");
    }

    public String getScheduleDt() {
        return this.getPrimaryValue("scheduledt");
    }
}

