/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.webservices;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.Trace;
import java.util.HashMap;
import java.util.Set;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.StringUtil;

public class SOMWS {
    public String getVersion() {
        String strVal = "V" + this.f_getVersion() + " (Build " + this.f_getbuild() + ")";
        return strVal;
    }

    public String f_getVersion() {
        return Build.getVersion();
    }

    public String f_getbuild() {
        return Build.getBuild();
    }

    public void logTrace(String header, String message) {
        Trace.log(header, message);
    }

    public int enableAPS() {
        int rc = 1;
        this.logTraceWS("Enabling APS");
        return rc;
    }

    public String getConnectionid(String nameserverlist, String databaseid, String userid, String password) {
        String connectionid = "";
        try {
            ConnectionProcessor connProc = new ConnectionProcessor();
            connectionid = connProc.getConnectionid(databaseid, userid, password);
        }
        catch (Exception e) {
            this.logError(e.getMessage());
        }
        return connectionid;
    }

    public void clearConnection(String nameserverlist, String connectionid) {
        try {
            ConnectionProcessor connProc = new ConnectionProcessor(connectionid);
            connProc.clearConnection(connectionid);
        }
        catch (Exception e) {
            this.logError(e.getMessage());
        }
    }

    public Object[] processAction(String actionid, String actionversionid, String propertyids, String propertyvalues, String nameserverlist, String connectionid) {
        String rc = "SUCCESS";
        Object[] properties = new Object[2];
        HashMap<String, String> props = new HashMap<String, String>();
        try {
            actionversionid = "1";
            String[] keyids = StringUtil.split(propertyids, "-|-");
            String[] values = StringUtil.split(propertyvalues, "-|-");
            for (int i = 0; i < keyids.length; ++i) {
                props.put(keyids[i], values[i]);
            }
            ActionProcessor apm = new ActionProcessor(connectionid);
            apm.processAction(actionid, actionversionid, props);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            rc = "FAILURE";
        }
        if (rc.equals("SUCCESS")) {
            props.put("(return)", "1");
        } else {
            props.put("(return)", "2");
        }
        String[] paramids = null;
        Set set = props.keySet();
        paramids = set.toArray(new String[0]);
        String[] paramvals = new String[paramids.length];
        for (int i = 0; i < paramids.length; ++i) {
            paramvals[i] = (String)props.get(paramids[i]);
        }
        properties[0] = paramids;
        properties[1] = paramvals;
        return properties;
    }

    public String opendataset(String nameserverlist, String connectionid, String sql) {
        QueryProcessor qp = new QueryProcessor(connectionid);
        DataSet ds = qp.getSqlDataSet(sql);
        String xmlstring = "";
        xmlstring = ds != null ? ds.toXML() : new DataSet().toXML();
        return xmlstring;
    }

    public int execSQLCommand(String nameserverlist, String connectionid, String sql) {
        int rc = 1;
        QueryProcessor qp = new QueryProcessor(connectionid);
        rc = qp.execSQL(sql);
        return rc;
    }

    private void logError(String message) {
        this.logTraceWS("ERROR: " + message);
    }

    public void logTraceWS(String message) {
        Trace.log("SOMWS: ", message);
    }
}

