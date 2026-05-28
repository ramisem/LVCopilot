/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public abstract class BaseWorkgroupItem
extends BaseAction {
    public static final String WORKGROUPID = "workgroupid";
    public static final String WORKGROUPITEMID = "workgroupitemid";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void maintainWorkgroupItem(boolean adddetail, PropertyList properties) throws SapphireException {
        DAMProcessor damProcessor = this.getDAMProcessor();
        String rsetid = damProcessor.createRSet("Workgroup", properties.getProperty(WORKGROUPID), "", "");
        String separator = properties.getProperty("separator", ";");
        DataSet workgroupitems = new DataSet(this.connectionInfo);
        String[] workgroupid = StringUtil.split(properties.getProperty(WORKGROUPID), separator);
        String[] workgroupitemid = StringUtil.split(properties.getProperty(WORKGROUPITEMID), separator);
        workgroupitems.addColumn(WORKGROUPID, 0);
        workgroupitems.addColumn(WORKGROUPITEMID, 0);
        for (int i = 0; i < workgroupid.length; ++i) {
            this.logger.info("Adding row for: " + workgroupid[i]);
            int row = workgroupitems.addRow();
            workgroupitems.setString(row, WORKGROUPID, workgroupid[i]);
            workgroupitems.setString(row, WORKGROUPITEMID, workgroupitemid[i]);
        }
        try {
            this.database.createResultSet("SELECT columnid, datatype FROM syscolumn WHERE lower( tableid ) = 'workgroupitem'");
            while (this.database.getNext()) {
                int i;
                String id = this.database.getString("columnid");
                String value = properties.getProperty(id);
                if (value.length() <= 0) continue;
                this.logger.info("Adding the column '" + id + "'");
                if (this.database.getString("datatype").equalsIgnoreCase("C")) {
                    workgroupitems.addColumn(id, 0);
                } else if (this.database.getString("datatype").equalsIgnoreCase("N") || this.database.getString("datatype").equalsIgnoreCase("R")) {
                    workgroupitems.addColumn(id, 1);
                } else {
                    workgroupitems.addColumn(id, 2);
                }
                this.logger.info("Setting the value '" + value + "'");
                String[] rowvalues = StringUtil.split(value, separator);
                if (rowvalues.length > 1) {
                    for (i = 0; i < rowvalues.length; ++i) {
                        if (rowvalues[i].equalsIgnoreCase("(null)")) {
                            workgroupitems.setValue(i, id, "");
                            continue;
                        }
                        workgroupitems.setValue(i, id, rowvalues[i]);
                    }
                    continue;
                }
                for (i = 0; i < workgroupid.length; ++i) {
                    if (rowvalues[0].equalsIgnoreCase("(null)")) {
                        workgroupitems.setValue(i, id, "");
                        continue;
                    }
                    workgroupitems.setValue(i, id, rowvalues[0]);
                }
            }
        }
        catch (SapphireException e) {
            if (rsetid != null) {
                damProcessor.clearRSet(rsetid);
            }
            throw new SapphireException("DB_ACTION_FAILED", "Database action failed:: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        try {
            if (adddetail) {
                DataSetUtil.insert(this.database, workgroupitems, "workgroupitem");
            } else {
                String[] keycolids = new String[]{WORKGROUPID, WORKGROUPITEMID};
                DataSetUtil.update(this.database, workgroupitems, "workgroupitem", keycolids);
            }
        }
        finally {
            if (rsetid != null) {
                damProcessor.clearRSet(rsetid);
            }
        }
    }
}

