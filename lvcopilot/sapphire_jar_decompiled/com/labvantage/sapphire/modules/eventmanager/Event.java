/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager;

import sapphire.util.DataSet;

public class Event {
    private String eventid;
    private String eventtypeid;
    private int conditionid = 0;
    private DataSet conditionItems = new DataSet("conditionid;conditionitem;operator1;value1;operator2;value2", "");
    private String scripttype;
    private String script;
    private boolean processAsync = false;

    public Event(String eventid, String eventtypeid) {
        this.eventid = eventid;
        this.eventtypeid = eventtypeid;
    }

    public String getEventid() {
        return this.eventid;
    }

    public String getEventtypeid() {
        return this.eventtypeid;
    }

    public void addCondition(String conditionitem, String operator, String value) {
        this.addConditionRow(conditionitem, operator, value, "", "");
    }

    private void addConditionRow(String conditionitem, String operator1, String value1, String operator2, String value2) {
        int row = this.conditionItems.addRow();
        this.conditionItems.setString(row, "conditionid", String.valueOf(this.conditionid));
        this.conditionItems.setString(row, "conditionitem", conditionitem);
        this.conditionItems.setString(row, "operator1", operator1);
        this.conditionItems.setString(row, "value1", value1);
        this.conditionItems.setString(row, "operator2", operator2);
        this.conditionItems.setString(row, "value2", value2);
        ++this.conditionid;
    }

    public DataSet getConditions() {
        return this.conditionItems;
    }

    public void setFunction(String scripttype, String script, boolean processAsync) {
        this.scripttype = scripttype;
        this.script = script;
        this.processAsync = processAsync;
    }

    public String getScripttype() {
        return this.scripttype;
    }

    public String getScript() {
        return this.script;
    }

    public boolean isProcessAsync() {
        return this.processAsync;
    }
}

