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

import com.labvantage.sapphire.actions.documents.InvokeInstrumentCertProc;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetCertInstrumentsForPL
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String certflag = ajaxResponse.getRequestParameter("certflag");
        String sdidataids = ajaxResponse.getRequestParameter("sdidataid").replaceAll("%3B", "','");
        String fromchoosereagent = ajaxResponse.getRequestParameter("fromchoosereagent", "N");
        SafeSQL safeSQL = new SafeSQL();
        String sdidataidSQL = "SELECT paramlistid, paramlistversionid, variantid FROM sdidata WHERE sdidataid IN (" + safeSQL.addIn(sdidataids) + ")";
        DataSet plds = this.getQueryProcessor().getPreparedSqlDataSet(sdidataidSQL, safeSQL.getValues());
        String paramlistid = plds.getColumnValues("paramlistid", ";");
        String paramlistversionid = plds.getColumnValues("paramlistversionid", ";");
        String variantid = plds.getColumnValues("variantid", ";");
        String[] paramlistIdArr = StringUtil.split(paramlistid, ";");
        String[] paramlistVersionIdArr = StringUtil.split(paramlistversionid, ";");
        String[] variantIdArr = StringUtil.split(variantid, ";");
        String instrumenttype = "";
        String error = "";
        if (fromchoosereagent.equalsIgnoreCase("Y")) {
            instrumenttype = ajaxResponse.getRequestParameter("instrumenttype", "");
        } else {
            for (int i = 0; i < paramlistIdArr.length; ++i) {
                safeSQL.reset();
                String sql = "SELECT s_instrumenttype FROM paramlist WHERE paramlistid =" + safeSQL.addVar(paramlistIdArr[i]) + " AND paramlistversionid = " + safeSQL.addVar(paramlistVersionIdArr[i]) + " AND variantid = " + safeSQL.addVar(variantIdArr[i]);
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (instrumenttype.length() == 0) {
                    instrumenttype = ds.getColumnValues("s_instrumenttype", ";");
                    continue;
                }
                if (instrumenttype.equals(ds.getColumnValues("s_instrumenttype", ";"))) continue;
                error = this.getTranslationProcessor().translate("Please select DataSets of same Instrument Type");
                break;
            }
        }
        String instrumentid = "";
        if (error.length() == 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("instrumenttypeid", instrumenttype);
            actionProps.setProperty("paramlistId", paramlistid.replaceAll("%3B", ";"));
            actionProps.setProperty("paramlistVersionId", paramlistversionid.replaceAll("%3B", ";"));
            actionProps.setProperty("variantId", variantid.replaceAll("%3B", ";"));
            actionProps.setProperty("certflag", certflag);
            actionProps.setProperty("connectionId", this.getConnectionId());
            actionProps.setProperty("sysUserId", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
            try {
                this.getActionProcessor().processActionClass(InvokeInstrumentCertProc.class.getName(), actionProps);
            }
            catch (ActionException e) {
                throw new ServletException((Throwable)e);
            }
            String rsetid = actionProps.getProperty("rsetResult");
            safeSQL.reset();
            String sql = "SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            instrumentid = ds.getColumnValues("keyid1", ";");
            this.getDAMProcessor().clearRSet(rsetid);
        }
        ajaxResponse.addCallbackArgument("keyid1", instrumentid);
        ajaxResponse.addCallbackArgument("paramlistid", paramlistid);
        ajaxResponse.addCallbackArgument("paramlistversionid", paramlistversionid);
        ajaxResponse.addCallbackArgument("variantid", variantid);
        ajaxResponse.addCallbackArgument("errormsg", error);
        ajaxResponse.print();
    }
}

