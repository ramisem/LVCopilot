/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.Column;
import com.labvantage.sapphire.xml.ant.PropertyListTask;
import com.labvantage.sapphire.xml.ant.PropertyTreeTask;
import org.apache.tools.ant.Task;

public class ColumnTask
extends Task {
    private String columnid;
    private String forceupdate;
    private String forcenullupdate;
    private String value;
    private String file;
    private String columnalias;
    private boolean excluded;
    private PropertyListTask propertyListTask;
    private PropertyTreeTask propertyTreeTask;

    public void setColumnid(String columnid) {
        this.columnid = columnid;
    }

    public void setForceupdate(String forceupdate) {
        this.forceupdate = forceupdate;
    }

    public void setForcenullupdate(String forcenullupdate) {
        this.forcenullupdate = forcenullupdate;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public void setColumnalias(String columnalias) {
        this.columnalias = columnalias;
    }

    public Column getColumn() {
        Column column = new Column(this.columnid);
        column.setUpdateDefinition(this.forceupdate, this.forcenullupdate);
        column.setValue(this.value);
        column.setFile(this.file);
        column.setExcluded(this.excluded);
        column.setColumnalias(this.columnalias);
        if (this.propertyListTask != null) {
            column.setPropertyListTransfer(this.propertyListTask.getPropertyListTransfer());
        }
        if (this.propertyTreeTask != null) {
            column.setPropertyTreeTransfer(this.propertyTreeTask.getPropertyTreeTransfer());
        }
        return column;
    }

    public void addConfiguredPropertyTree(PropertyTreeTask propertyTree) {
        this.propertyTreeTask = propertyTree;
    }

    public void addConfiguredPropertyList(PropertyListTask propertyList) {
        this.propertyListTask = propertyList;
    }
}

