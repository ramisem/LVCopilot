/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.finance;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.util.StringHolder;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class BaseFinanceAction
extends BaseAction {
    private DAMProcessor dam;
    private String rsetid = null;
    private boolean applylock;
    private String sdcid;
    private String keyid1;
    private String keyid2;
    private String financeid;
    private String financeitemid;
    private String[] financeitemids;
    private PropertyList properties;
    protected DataSet financeitems;
    protected String[] financeids;
    protected String separator;

    protected void initRSet(String lock_sdcid, String financeid, String financeitemid, PropertyList properties) throws SapphireException {
        String applylock = properties.getProperty("applylock");
        String separator = properties.getProperty("separator", ";");
        int rc = 1;
        this.properties = properties;
        this.applylock = applylock != null && applylock.equals("Y");
        this.financeid = financeid;
        this.financeitemid = financeitemid;
        this.separator = separator;
        if (lock_sdcid == "PriceList") {
            this.sdcid = "pricelistitem";
            this.keyid1 = "pricelistid";
            this.keyid2 = "pricelistitemid";
        } else {
            this.sdcid = "chargelistitem";
            this.keyid1 = "chargelistid";
            this.keyid2 = "chargelistitemid";
        }
        this.dam = this.getDAMProcessor();
        StringHolder rsetidHolder = new StringHolder();
        rc = this.applylock ? this.dam.createLockedRSet(lock_sdcid, financeid, "", "", rsetidHolder) : this.dam.createRSet(lock_sdcid, financeid, "", "", rsetidHolder);
        if (rc != 1) {
            throw new SapphireException("INVALID_PARAMETER", "Invalid parameter passed");
        }
        this.rsetid = rsetidHolder.value;
    }

    protected void setValues() throws SapphireException {
        this.financeitems = new DataSet(this.connectionInfo);
        this.financeids = StringUtil.split(this.financeid, this.separator);
        this.financeitemids = StringUtil.split(this.financeitemid, this.separator);
        for (int i = 0; i < this.financeids.length; ++i) {
            this.logger.info("Adding row for: " + this.financeids[i]);
            int row = this.financeitems.addRow();
            this.financeitems.addColumn(this.keyid1, 0);
            this.financeitems.setString(row, this.keyid1, this.financeids[i]);
            this.financeitems.addColumn(this.keyid2, 0);
            this.financeitems.setString(row, this.keyid2, this.financeitemids[i]);
        }
        this.logger.info("Setting the values");
        try {
            this.database.createPreparedResultSet("SELECT columnid, datatype FROM syscolumn WHERE lower( tableid ) = ?", new Object[]{this.sdcid});
            while (this.database.getNext()) {
                int i;
                String id = this.database.getString("columnid");
                String value = this.properties.getProperty(id);
                if (StringUtil.getLen(value) <= 0L) continue;
                this.logger.info("Adding the column '" + id + "'");
                if (this.database.getString("datatype").equalsIgnoreCase("C")) {
                    this.financeitems.addColumn(id, 0);
                } else if (this.database.getString("datatype").equalsIgnoreCase("N") || this.database.getString("datatype").equalsIgnoreCase("R")) {
                    this.financeitems.addColumn(id, 1);
                } else {
                    this.financeitems.addColumn(id, 2);
                    if ("Y".equals(this.getSDCProcessor().getSDCColumnProperty(this.sdcid, id, "timezoneindependent"))) {
                        this.financeitems.setTimeZoneInsensitive(id);
                    }
                }
                this.logger.info("Setting the value '" + value + "'");
                String[] rowvalues = StringUtil.split(value, this.separator);
                if (rowvalues.length > 1) {
                    for (i = 0; i < rowvalues.length; ++i) {
                        if (rowvalues[i].equalsIgnoreCase("(null)")) {
                            this.financeitems.setValue(i, id, "");
                            continue;
                        }
                        this.financeitems.setValue(i, id, rowvalues[i]);
                    }
                    continue;
                }
                for (i = 0; i < this.financeids.length; ++i) {
                    if (rowvalues[0].equalsIgnoreCase("(null)")) {
                        this.financeitems.setValue(i, id, "");
                        continue;
                    }
                    this.financeitems.setValue(i, id, rowvalues[0]);
                }
            }
        }
        catch (SapphireException e) {
            if (this.rsetid != null) {
                this.dam.clearRSet(this.rsetid);
            }
            throw new SapphireException("INVALID_PARAMETER", "Could not lookup the table definition for '" + this.sdcid + "'", e);
        }
    }

    protected void updateDatabase(boolean adddetail) throws SapphireException {
        if (adddetail) {
            try {
                DataSetUtil.insert(this.database, this.financeitems, this.sdcid);
            }
            catch (Exception e) {
                throw new SapphireException("DB_INSERT_FAILED", "Failed to insert in " + this.sdcid + ": " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
            finally {
                if (this.rsetid != null) {
                    this.dam.clearRSet(this.rsetid);
                }
            }
        }
        String[] keycolids = new String[]{this.keyid1, this.keyid2};
        try {
            DataSetUtil.update(this.database, this.financeitems, this.sdcid, keycolids);
        }
        catch (SapphireException e) {
            throw new SapphireException("DB_UPDATE_FAILED", "Failed to update " + this.sdcid, e);
        }
        finally {
            if (this.rsetid != null) {
                this.dam.clearRSet(this.rsetid);
            }
        }
    }

    protected void deleteItem() throws SapphireException {
        this.financeids = StringUtil.split(this.financeid, this.separator);
        this.financeitemids = StringUtil.split(this.financeitemid, this.separator);
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer delete = new StringBuffer("DELETE FROM ").append(this.sdcid).append(" WHERE ");
        delete.append("( ").append(this.keyid1).append(" = ").append(safeSQL.addVar(this.financeids[0])).append(" AND ").append(this.keyid2);
        delete.append(" = ").append(safeSQL.addVar(this.financeitemids[0])).append(" )");
        for (int i = 1; i < this.financeids.length; ++i) {
            delete.append(" OR ( ").append(this.keyid1).append(" = ").append(safeSQL.addVar(this.financeids[i])).append(" AND ");
            delete.append(this.keyid2).append(" = ").append(safeSQL.addVar(this.financeitemids[i])).append(" ) ");
        }
        try {
            this.database.executePreparedUpdate(delete.toString(), safeSQL.getValues());
        }
        catch (Exception e) {
            throw new SapphireException("DB_UPDATE_FAILED", "Failed to delete " + this.sdcid + "s using: " + delete.toString(), e);
        }
        finally {
            if (this.rsetid != null) {
                this.dam.clearRSet(this.rsetid);
            }
        }
    }
}

