/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import java.sql.CallableStatement;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Query
extends BaseSDCRules {
    public static final String SDCID = "Query";
    public static final String TABLEID = "query";
    public static final String CONTROLLEDFLAG_CORE = "Y";
    public static final String CONTROLLEDFLAG_CHANGED = "N";
    public static final String CONTROLLEDFLAG_USER = "U";
    public static final String CONTROLLEDFLAG_IGNORED = "I";
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.setControlledFlag(primary, CONTROLLEDFLAG_USER);
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (this.connectionInfo.isOracle()) {
            this.checkDBSyntax(primary);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String operation;
        DataSet primary = sdiData.getDataset("primary");
        String string = operation = actionProps.containsKey("operation") ? actionProps.getProperty("operation", "") : "";
        if (operation.equalsIgnoreCase("revert")) {
            this.overwrite(primary);
            this.setControlledFlag(primary, actionProps.getProperty("controlledflag", CONTROLLEDFLAG_CORE));
        } else if (operation.equalsIgnoreCase("accept")) {
            this.setControlledFlag(primary, actionProps.getProperty("controlledflag", CONTROLLEDFLAG_IGNORED));
        } else {
            String[] basedonIds;
            String[] queryIds = StringUtil.split(actionProps.getProperty("keyid1"), ";");
            if (!this.isItUserQuery(queryIds[0], (basedonIds = StringUtil.split(actionProps.getProperty("keyid2"), ";"))[0]) && this.validateControlledColumnChanges(primary)) {
                this.setControlledFlag(primary, CONTROLLEDFLAG_CHANGED);
            }
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (this.connectionInfo.isOracle()) {
            this.checkDBSyntax(primary);
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String[] basedonIds;
        String[] queryIds = StringUtil.split(actionProps.getProperty("queryid"), ";");
        if (!this.isItUserQuery(queryIds[0], (basedonIds = StringUtil.split(actionProps.getProperty("basedonid"), ";"))[0])) {
            if (actionProps.containsKey("argid")) {
                DataSet argds = sdiData.getDataset("queryarg");
                this.setControlledFlag(argds);
            } else if (actionProps.containsKey("queryunionno")) {
                DataSet unionds = sdiData.getDataset("queryunion");
                this.setControlledFlag(unionds);
            }
        }
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String[] basedonIds;
        String[] queryIds = StringUtil.split(actionProps.getProperty("queryid"), ";");
        if (!this.isItUserQuery(queryIds[0], (basedonIds = StringUtil.split(actionProps.getProperty("basedonid"), ";"))[0])) {
            if (actionProps.containsKey("argid")) {
                DataSet argds = sdiData.getDataset("queryarg");
                this.setControlledFlag(argds);
            } else if (actionProps.containsKey("queryunionno")) {
                DataSet unionds = sdiData.getDataset("queryunion");
                this.setControlledFlag(unionds);
            }
        }
    }

    @Override
    public void postDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        String[] basedonIds;
        String test = rsetid;
        String[] queryIds = StringUtil.split(actionProps.getProperty("queryid"), ";");
        if (!this.isItUserQuery(queryIds[0], (basedonIds = StringUtil.split(actionProps.getProperty("basedonid"), ";"))[0])) {
            this.setControlledFlag(queryIds[0], basedonIds[0]);
        }
    }

    private void setControlledFlag(DataSet primary, String value) throws SapphireException {
        for (int i = 0; i < primary.size(); ++i) {
            primary.addColumn("controlledflag", 0);
            primary.setString(i, "controlledflag", value);
        }
    }

    private void setControlledFlag(DataSet data) throws SapphireException {
        for (int i = 0; i < data.size(); ++i) {
            String queryid = data.getValue(i, "queryid", "");
            String basedonid = data.getValue(i, "basedonid", "");
            if (queryid.length() <= 0 || basedonid.length() <= 0) continue;
            SafeSQL safeSQL = new SafeSQL();
            this.database.executePreparedUpdate("UPDATE query SET controlledflag = " + safeSQL.addVar(CONTROLLEDFLAG_CHANGED) + " WHERE queryid = " + safeSQL.addVar(queryid) + "  AND basedonid = " + safeSQL.addVar(basedonid), safeSQL.getValues());
        }
    }

    private void setControlledFlag(String queryid, String basedonid) throws SapphireException {
        if (queryid.length() > 0 && basedonid.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            this.database.executePreparedUpdate("UPDATE query SET controlledflag = " + safeSQL.addVar(CONTROLLEDFLAG_CHANGED) + " WHERE queryid = " + safeSQL.addVar(queryid) + "  AND basedonid = " + safeSQL.addVar(basedonid), safeSQL.getValues());
        }
    }

    private void overwrite(DataSet primary) throws SapphireException {
        try {
            for (int i = 0; i < primary.size(); ++i) {
                String queryid = primary.getValue(i, "queryid", "");
                String basedonid = primary.getValue(i, "basedonid", "");
                if (queryid.length() <= 0 || basedonid.length() <= 0) continue;
                String callstmt = "{call LV_RSET" + (this.connectionInfo.isOracle() ? "." : "_") + "QueryOverWrite( ?, ? ) }";
                CallableStatement cs = this.database.prepareCall(callstmt);
                cs.setString(1, queryid);
                cs.setString(2, basedonid);
                cs.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new SapphireException("Failed to overwrite to OOB Definition. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }

    private String checkDBSyntax(DataSet primary) throws SapphireException {
        String response = "";
        try {
            for (int i = 0; i < primary.size(); ++i) {
                String queryid = primary.getValue(i, "queryid", "");
                String basedonid = primary.getValue(i, "basedonid", "");
                if (queryid.length() <= 0 || basedonid.length() <= 0) continue;
                String callstmt = "{? = call LV_RSET.QueryCheck( ?, ? ) }";
                CallableStatement cs = this.database.prepareCall(callstmt);
                cs.registerOutParameter(1, 12);
                cs.setString(2, queryid);
                cs.setString(3, basedonid);
                cs.executeUpdate();
                response = cs.getString(1);
            }
        }
        catch (SQLException e) {
            throw new SapphireException("Failed to parse Database Syntax. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        return response;
    }

    private boolean validateControlledColumnChanges(DataSet primary) {
        boolean columnvaluechanged = false;
        SafeSQL safeSQL = new SafeSQL();
        for (int i = 0; i < primary.size(); ++i) {
            DataSet ds;
            String queryid = primary.getValue(i, "queryid", "");
            String basedonid = primary.getValue(i, "basedonid", "");
            String pr_fromclause = primary.getValue(i, "fromclause", "");
            String pr_whereclause = primary.getValue(i, "whereclause", "");
            String pr_orderbyclause = primary.getValue(i, "orderbyclause", "");
            String pr_withclause = primary.getValue(i, "withclause", "");
            if (queryid.length() <= 0 || basedonid.length() <= 0 || (ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT fromclause, whereclause, orderbyclause, withclause FROM query WHERE queryid = " + safeSQL.addVar(queryid) + "  AND basedonid = " + safeSQL.addVar(basedonid), safeSQL.getValues())).size() <= 0) continue;
            String fromclause = ds.getValue(0, "fromclause", "");
            String whereclause = ds.getValue(0, "whereclause", "");
            String orderbyclause = ds.getValue(0, "orderbyclause", "");
            String withclause = ds.getValue(0, "withclause", "");
            if (pr_fromclause.equals(fromclause) && pr_whereclause.equals(whereclause) && pr_orderbyclause.equals(orderbyclause) && pr_withclause.equals(withclause)) continue;
            columnvaluechanged = true;
        }
        return columnvaluechanged;
    }

    private boolean isItUserQuery(String queryid, String basedonid) {
        boolean isUser = false;
        if (queryid.length() > 0 && basedonid.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT  controlledflag FROM query WHERE queryid = " + safeSQL.addVar(queryid) + "  AND basedonid = " + safeSQL.addVar(basedonid), safeSQL.getValues());
            if (ds.getValue(0, "controlledflag").equalsIgnoreCase(CONTROLLEDFLAG_USER)) {
                isUser = true;
            }
        }
        return isUser;
    }
}

