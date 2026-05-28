/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryAdmin;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.sql.CallableStatement;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class EditSDCColumn
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 84852 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet newValues = new DataSet();
        newValues.addColumnValues("tableid", 0, properties.getProperty("tableid"), ";");
        newValues.addColumnValues("columnid", 0, properties.getProperty("columnid"), ";");
        newValues.addColumnValues("__datatype", 0, properties.getProperty("datatype"), ";");
        newValues.addColumnValues("columnlength", 1, properties.getProperty("columnlength"), ";");
        newValues.addColumnValues("columndesc", 0, properties.getProperty("columndesc"), ";");
        newValues.addColumnValues("columnlabel", 0, properties.getProperty("columnlabel"), ";");
        if (!"".equals(properties.getProperty("columndoc")) && !"(null)".equals(properties.getProperty("columndoc"))) {
            newValues.addColumnValues("columndoc", 0, properties.getProperty("columndoc"), ";");
        }
        newValues.addColumnValues("columnsequence", 1, properties.getProperty("columnsequence"), ";");
        newValues.addColumnValues("searchableflag", 0, properties.getProperty("searchableflag"), ";");
        for (int i = 0; i < newValues.size(); ++i) {
            if (newValues.getString(i, "columndesc").equals("(null)")) {
                newValues.setString(i, "columndesc", "");
            }
            if (newValues.getString(i, "columnlabel").equals("(null)")) {
                newValues.setString(i, "columnlabel", "");
            }
            if (!"".equals(properties.getProperty("columndoc")) && !"(null)".equals(properties.getProperty("columndoc")) && newValues.getString(i, "columndoc").equals("(null)")) {
                newValues.setString(i, "columndoc", "");
            }
            if (newValues.getString(i, "searchableflag").equals("(null)")) {
                newValues.setString(i, "searchableflag", "N");
            }
            if (!"C".equals(newValues.getString(i, "__datatype"))) continue;
            String strLen = newValues.getValue(i, "columnlength");
            if (strLen.length() == 0) {
                newValues.setNumber(i, "columnlength", 20);
                continue;
            }
            try {
                int intLen = Integer.parseInt(strLen);
                if (intLen >= 1 && intLen <= 4000) continue;
                throw new SapphireException("Column length for column '" + newValues.getString(i, "columnid") + "' must be between 1 and 4000!");
            }
            catch (NumberFormatException e) {
                newValues.setNumber(i, "columnlength", 20);
            }
        }
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT columnid, columnlength FROM syscolumn WHERE tableid = " + safeSQL.addVar(newValues.getValue(0, "tableid")) + " AND columnid IN (" + safeSQL.addIn(newValues.getColumnValues("columnid", "','")) + ")";
        DataSet oldLengths = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        DataSetUtil.update(this.database, newValues, "syscolumn", new String[]{"tableid", "columnid"});
        try {
            HashMap<String, String> findMap = new HashMap<String, String>();
            for (int col = 0; col < newValues.size(); ++col) {
                if (!"C".equals(newValues.getString(col, "__datatype")) || newValues.getBigDecimal(col, "columnlength") == null) continue;
                String columnid = newValues.getString(col, "columnid");
                findMap.put("columnid", columnid);
                int findRow = oldLengths.findRow(findMap);
                if (findRow < 0 || newValues.getInt(col, "columnlength") == oldLengths.getInt(findRow, "columnlength")) continue;
                String callstmt = "{call lv_tab" + (this.connectionInfo.isOracle() ? "." : "_") + "resizecolumn( ?, ?, ? ) }";
                CallableStatement cs = this.database.prepareCall(callstmt);
                cs.setString(1, newValues.getString(col, "tableid"));
                cs.setString(2, columnid);
                cs.setInt(3, newValues.getInt(col, "columnlength"));
                cs.executeUpdate();
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to alter columns '" + properties.getProperty("columnid") + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
        new AdhocQueryAdmin((SapphireConnection)this.connectionInfo).resetHibernateCache(this.connectionInfo.getDatabaseId());
    }
}

