/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.sql.CallableStatement;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AddSDCColumn
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77311 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String compcode = Configuration.getCompcode(this.connectionInfo.getDatabaseId());
        String columnType = Configuration.isDevmode(this.connectionInfo.getDatabaseId()) || compcode.length() > 0 ? "S" : "U";
        DataSet values = new DataSet();
        values.addColumnValues("tableid", 0, properties.getProperty("tableid"), ";");
        values.addColumnValues("columnid", 0, properties.getProperty("columnid"), ";");
        values.addColumnValues("datatype", 0, properties.getProperty("datatype"), ";");
        values.addColumnValues("columntype", 0, properties.getProperty("columntype"), ";");
        values.addColumnValues("columnlength", 1, properties.getProperty("columnlength"), ";");
        values.addColumnValues("columnlabel", 0, properties.getProperty("columnlabel"), ";");
        values.addColumnValues("columndoc", 0, properties.getProperty("columndoc"), ";");
        values.addColumnValues("columndesc", 0, properties.getProperty("columndesc"), ";");
        values.addColumnValues("columnsequence", 1, properties.getProperty("columnsequence"), ";");
        values.addColumnValues("pkflag", 0, properties.getProperty("pkflag"), ";");
        values.addColumnValues("nnflag", 0, properties.getProperty("nnflag"), ";");
        values.addColumnValues("searchableflag", 0, properties.getProperty("searchableflag"), ";");
        for (int i = 0; i < values.size(); ++i) {
            String tableid = values.getString(i, "tableid");
            this.database.createPreparedResultSet("SELECT * FROM systable WHERE tableid = ?", new Object[]{tableid});
            if (!this.database.getNext()) {
                throw new SapphireException("Table '" + tableid + "' not found in Sapphire data dictionary!");
            }
            String tabletype = this.database.getString("tabletype");
            String columnid = values.getString(i, "columnid").trim().toLowerCase();
            if (!"U".equals(tabletype) && columnType.equals("U") && !values.getString(i, "columnid").toLowerCase().startsWith("u_")) {
                columnid = "u_" + columnid;
            } else if (!("U".equals(tabletype) || compcode.length() <= 0 || values.getString(i, "columnid").toLowerCase().startsWith(compcode.toLowerCase() + "_") || tableid.substring(0, 3).toLowerCase().equals(compcode.toLowerCase()))) {
                columnid = compcode.toLowerCase() + "_" + columnid;
            }
            if (columnid.equals("sdcid") || columnid.equals("keyid1") || columnid.equals("keyid3") || columnid.equals("keyid3")) {
                throw new SapphireException("Columns cannot be named 'sdcid', 'keyid1', 'keyid2' or 'keyid3'");
            }
            if (columnid.length() > 30) {
                throw new SapphireException("Column '" + values.getString(i, "columnid") + "' name too long (max = 30). Note that user columns for System SDC's are prepended with u_.");
            }
            values.setString(i, "columnid", columnid);
            if (values.getString(i, "columnlabel").equals("(null)")) {
                values.setString(i, "columnlabel", "");
            }
            if (values.getString(i, "columndoc").equals("(null)")) {
                values.setString(i, "columndoc", "");
            }
            if (values.getString(i, "columndesc").equals("(null)")) {
                values.setString(i, "columndesc", "");
            }
            if (values.getString(i, "datatype").equals("C")) {
                String strLen = values.getValue(i, "columnlength");
                if (strLen.length() == 0) {
                    values.setNumber(i, "columnlength", 20);
                    continue;
                }
                try {
                    int intLen = Integer.parseInt(strLen);
                    if (intLen < 1 || intLen > 4000) {
                        throw new SapphireException("Column length for column '" + values.getString(i, "columnid") + "' must be between 1 and 4000!");
                    }
                }
                catch (NumberFormatException e) {
                    values.setNumber(i, "columnlength", 20);
                }
                continue;
            }
            values.setNumber(i, "columnlength", 0);
        }
        int col = 0;
        try {
            DataSetUtil.insert(this.database, values, "syscolumn");
            for (col = 0; col < values.size(); ++col) {
                String callstmt = "{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "addnewcolumn( ?, ? ) }";
                CallableStatement cs = this.database.prepareCall(callstmt);
                cs.setString(1, values.getString(col, "tableid"));
                cs.setString(2, values.getString(col, "columnid"));
                cs.executeUpdate();
            }
        }
        catch (Exception e) {
            for (int i = 0; i < col; ++i) {
                try {
                    String callstmt = "{call lv_clean" + (this.connectionInfo.isOracle() ? "." : "_") + "dropcolumn( ?, ? ) }";
                    CallableStatement cs = this.database.prepareCall(callstmt);
                    cs.setString(1, values.getString(i, "tableid"));
                    cs.setString(2, values.getString(i, "columnid"));
                    cs.executeUpdate();
                    continue;
                }
                catch (SQLException sQLException) {
                    // empty catch block
                }
            }
            throw new SapphireException("Failed to add new columns '" + properties.getProperty("columnid") + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
    }
}

