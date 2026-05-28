/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.email.system.collections.ArrayList
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.aspose.email.system.collections.ArrayList;
import java.util.Collection;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class CheckControlledFlag
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        ConnectionProcessor connectionProcessor = this.getConnectionProcessor();
        ConnectionInfo connectionInfo = connectionProcessor.getConnectionInfo(connectionProcessor.getConnectionid());
        ArrayList controlledFlagList = new ArrayList();
        String keyids = ajaxResponse.getRequestParameter("keyids");
        try {
            if (connectionInfo.isOracle()) {
                StringBuffer ids = new StringBuffer();
                String[] idList = StringUtil.split(keyids, "%3B");
                for (int count = 0; count < idList.length; ++count) {
                    String[] keyidList = StringUtil.split(idList[count], "|");
                    ids.append(" , ").append("('").append(keyidList[0]).append("','").append(keyidList[1]).append("')");
                }
                String selectedIds = ids.substring(3);
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select controlledflag from query where (queryid,basedonid) IN (" + selectedIds + " )", new Object[0]);
                for (int count = 0; count < ds.size(); ++count) {
                    controlledFlagList.add(ds.getValue(count, "controlledflag"));
                }
            } else if (connectionInfo.isSqlServer()) {
                String[] idList = StringUtil.split(keyids, "%3B");
                for (int count = 0; count < idList.length; ++count) {
                    SafeSQL safeSQL = new SafeSQL();
                    String[] keyidList = StringUtil.split(idList[count], "|");
                    StringBuffer sql = new StringBuffer();
                    sql.append("select controlledflag ");
                    sql.append(" from query ");
                    sql.append(" where queryid = " + safeSQL.addVar(keyidList[0]));
                    sql.append(" and basedonid = " + safeSQL.addVar(keyidList[1]));
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (ds.size() <= 0) continue;
                    controlledFlagList.add(ds.getValue(0, "controlledflag"));
                }
            }
            ajaxResponse.addCallbackArgument("controlledFlagList", (Collection)controlledFlagList);
            ajaxResponse.addCallbackArgument("extra_props", ajaxResponse.getRequestParameter("extra_props"));
        }
        catch (Exception exp) {
            ajaxResponse.setError("Failed to get controlledflag value. Exception: " + exp.getMessage(), exp);
        }
        ajaxResponse.print();
    }
}

