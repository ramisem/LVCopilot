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

import com.labvantage.sapphire.actions.documents.InvokeUserCertProc;
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

public class GetCertUsersForPL
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String[] sdiids;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdiid = ajaxResponse.getRequestParameter("sdidataid");
        SafeSQL safeSQL = new SafeSQL();
        DataSet plds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdidataid, paramlistid, paramlistversionid, variantid, keyid1 , s_assigneddepartment FROM sdidata WHERE sdidataid IN (" + safeSQL.addIn(sdiid, "%3B") + ")", safeSQL.getValues());
        StringBuffer paramlistid = new StringBuffer();
        StringBuffer paramlistversionid = new StringBuffer();
        StringBuffer variantid = new StringBuffer();
        StringBuffer sampleid = new StringBuffer();
        for (String did : sdiids = StringUtil.split(sdiid, "%3B")) {
            for (int r = 0; r < plds.getRowCount(); ++r) {
                if (!did.equalsIgnoreCase(plds.getValue(r, "sdidataid"))) continue;
                if (paramlistid.length() > 0) {
                    paramlistid.append(";");
                    paramlistversionid.append(";");
                    variantid.append(";");
                    sampleid.append(";");
                }
                paramlistid.append(plds.getValue(r, "paramlistid"));
                paramlistversionid.append(plds.getValue(r, "paramlistversionid"));
                variantid.append(plds.getValue(r, "variantid"));
                sampleid.append(plds.getValue(r, "keyid1"));
            }
        }
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("paramlistId", paramlistid.toString());
        actionProps.setProperty("paramlistVersionId", paramlistversionid.toString());
        actionProps.setProperty("variantId", variantid.toString());
        actionProps.setProperty("sampleId", sampleid.toString());
        actionProps.setProperty("department", plds.getString(0, "s_assigneddepartment", ""));
        actionProps.setProperty("connectionId", this.getConnectionId());
        actionProps.setProperty("sysUserId", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
        try {
            this.getActionProcessor().processActionClass(InvokeUserCertProc.class.getName(), actionProps);
        }
        catch (ActionException e) {
            throw new ServletException((Throwable)e);
        }
        String userid = "";
        String rsetid = actionProps.getProperty("rsetResult");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT keyid1 FROM rsetitems WHERE rsetid=?", new Object[]{rsetid});
        userid = ds.getColumnValues("keyid1", ";");
        this.getDAMProcessor().clearRSet(rsetid);
        ajaxResponse.addCallbackArgument("keyid1", userid);
        ajaxResponse.addCallbackArgument("paramlistId", paramlistid);
        ajaxResponse.addCallbackArgument("paramlistVersionId", paramlistversionid);
        ajaxResponse.addCallbackArgument("variantId", variantid);
        ajaxResponse.print();
    }
}

