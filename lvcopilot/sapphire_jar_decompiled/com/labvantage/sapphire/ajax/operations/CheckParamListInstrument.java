/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class CheckParamListInstrument
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    private final String cFNS = "concat(";
    private final String cFNE = ")";
    private final String specialDelimer = "^^^";
    private boolean isOracle = true;

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        QueryProcessor qp = this.getQueryProcessor();
        ConnectionProcessor connectionProcessor = this.getConnectionProcessor();
        ConnectionInfo connectionInfo = connectionProcessor.getConnectionInfo(connectionProcessor.getConnectionid());
        this.isOracle = connectionInfo.isOracle();
        String instrumenttype = ajaxResponse.getRequestParameter("instrumenttype", "");
        String workitemid = ajaxResponse.getRequestParameter("workitemid", "");
        String workitemversionid = ajaxResponse.getRequestParameter("workitemversionid", "");
        DataSet plDS = new DataSet();
        try {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT keyid1,keyid2,keyid3");
            sql.append(" FROM workitemitem ");
            sql.append(" WHERE workitemid = " + safeSQL.addVar(workitemid));
            sql.append(" and workitemversionid = " + safeSQL.addVar(workitemversionid));
            DataSet wiiDS = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (wiiDS != null && wiiDS.size() > 0) {
                String keyid1 = wiiDS.getColumnValues("keyid1", ";");
                String keyid2 = wiiDS.getColumnValues("keyid2", ";");
                String keyid3 = wiiDS.getColumnValues("keyid3", ";");
                if (keyid2.contains("C")) {
                    keyid2 = SdiInfo.getCurrentVersion("ParamList", keyid1, keyid3, this.getConnectionProcessor().getSapphireConnection());
                }
                String[] keyid1Arr = StringUtil.split(keyid1, ";");
                String[] keyid2Arr = StringUtil.split(keyid2, ";");
                String[] keyid3Arr = StringUtil.split(keyid3, ";");
                String whereClauseForPLs = "";
                for (int i = 0; i < keyid1Arr.length; ++i) {
                    whereClauseForPLs = whereClauseForPLs + "','" + keyid1Arr[i] + "^^^" + keyid2Arr[i] + "^^^" + keyid3Arr[i];
                }
                if (whereClauseForPLs.length() > 1) {
                    whereClauseForPLs = whereClauseForPLs.substring(3);
                }
                safeSQL.reset();
                sql.delete(0, sql.length());
                sql.append("SELECT s_instrumenttype,paramlistid,paramlistversionid,variantid");
                sql.append(" FROM paramlist ");
                sql.append(" WHERE " + this.concatFields("paramlistid", "paramlistversionid", "variantid"));
                sql.append(" IN (" + safeSQL.addIn(whereClauseForPLs) + ")");
                sql.append(" and s_instrumenttype = " + safeSQL.addVar(instrumenttype));
                plDS = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
        }
        catch (Exception e) {
            ajaxResponse.setError("Failed to check ParamList Instrument. Exception: " + e.getMessage(), e);
        }
        ajaxResponse.addCallbackArgument("ds", plDS);
        ajaxResponse.print();
    }

    private String concatFields(String ... fields) {
        String str = "";
        boolean firstItem = true;
        for (String f : fields) {
            if (firstItem) {
                str = this.isOracle ? f : "cast(" + f + " as nvarchar(100))";
                firstItem = false;
                continue;
            }
            str = "concat(concat(" + str + ",'" + "^^^" + "'" + ")" + "," + (this.isOracle ? f : "cast(" + f + " as nvarchar(100))") + ")";
        }
        return str;
    }
}

