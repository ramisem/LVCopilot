/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaintRequest;
import com.labvantage.sapphire.pageelements.gwt.shared.SecuritySetConstants;
import java.util.ArrayList;
import java.util.Collection;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class SecuritySetRequest
extends SDIMaintRequest
implements SecuritySetConstants {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    @Override
    protected boolean processCommand(String command, CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        if (command.equals("retrievedata")) {
            String users = commandRequest.getString("users");
            String sdcids = commandRequest.getString("sdcids");
            SafeSQL safeSQL = new SafeSQL();
            String sdcsingular = "select sdcid, singular from sdc where sdcid in (" + safeSQL.addIn(sdcids) + ") order by singular";
            DataSet sdcSingularDS = this.getQueryProcessor().getPreparedSqlDataSet(sdcsingular, safeSQL.getValues());
            safeSQL.reset();
            String userdesc = "select sysuserid, sysuserdesc from sysuser where sysuserid in (" + safeSQL.addIn(users) + ") order by sysuserdesc, sysuserid";
            DataSet userDescDS = this.getQueryProcessor().getPreparedSqlDataSet(userdesc, safeSQL.getValues());
            safeSQL.reset();
            String sdcoperation = "SELECT sdcid, operationid FROM sdcoperation WHERE sdcid in ( " + safeSQL.addIn(sdcids) + " ) order by sdcid,usersequence";
            DataSet sdcOperationDS = this.getQueryProcessor().getPreparedSqlDataSet(sdcoperation, safeSQL.getValues());
            commandResponse.set("sdcsingular", sdcSingularDS);
            commandResponse.set("sdcoperation", sdcOperationDS);
            commandResponse.set("userdesc", userDescDS);
        } else if (command.equals("userjobdata")) {
            String itemid = commandRequest.getString("itemid");
            String itemtype = commandRequest.getString("itemtype");
            SafeSQL safeSQL = new SafeSQL();
            String itemdatasql = "SELECT * FROM securitysetitem WHERE securitysetitemid = '" + itemid + "' and " + "itemtypeflag" + " = " + safeSQL.addVar(itemtype);
            DataSet itemdata = this.getQueryProcessor().getPreparedSqlDataSet(itemdatasql, safeSQL.getValues());
            if (itemdata != null && itemdata.getRowCount() > 0) {
                String insecuritysetids = this.getDelimitedString(this.getDistinctCollection(itemdata, "securitysetid"), ",", "'");
                safeSQL.reset();
                String securitysetsdcsql = "SELECT securitysetid, sdcid, singular FROM securitysetsdc, sdc WHERE securitysetid IN (" + safeSQL.addIn(insecuritysetids) + ") and sdc.sdcid = " + "securitysetsdcid" + " order by singular";
                DataSet securitysetsdcs = this.getQueryProcessor().getPreparedSqlDataSet(securitysetsdcsql, safeSQL.getValues());
                String insdcs = this.getDelimitedString(this.getDistinctCollection(securitysetsdcs, "sdcid"), ",", "'");
                safeSQL.reset();
                String sdcopsql = "SELECT sdcid, operationid FROM sdcoperation WHERE sdcid in ( " + safeSQL.addIn(insdcs) + " ) order by sdcid,usersequence";
                DataSet sdcoperation = this.getQueryProcessor().getPreparedSqlDataSet(sdcopsql, safeSQL.getValues());
                commandResponse.set("securitysetsdcsing", securitysetsdcs);
                commandResponse.set("sdcoperation", sdcoperation);
            }
            commandResponse.set("securitysetitem", itemdata);
        } else if (command.equals("retrievejobtype")) {
            String currentJobtype = this.connectionInfo.getCurrentJobtype();
            commandResponse.set("jobtype", currentJobtype == null ? "" : currentJobtype);
        }
        return true;
    }

    private Collection getDistinctCollection(DataSet dataset, String column) {
        int count = dataset.getRowCount();
        ArrayList<String> data = new ArrayList<String>();
        for (int i = 0; i < count; ++i) {
            String value = dataset.getValue(i, column);
            if (data.contains(value)) continue;
            data.add(value);
        }
        return data;
    }

    private String getDelimitedString(Collection data, String delimiter, String wrapper) {
        String delimitedstr = "";
        for (Object value : data) {
            delimitedstr = delimitedstr + delimiter + wrapper + value + wrapper;
        }
        return delimitedstr.substring(1);
    }
}

