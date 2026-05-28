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
import com.labvantage.sapphire.actions.documents.InvokeUserCertProc;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class FindDSAndCertResource
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdiworkitemid = ajaxResponse.getRequestParameter("sdiworkitemid").replaceAll("%3B", ";");
        String sampleid = ajaxResponse.getRequestParameter("sampleid").replaceAll("%3B", ";");
        String certresource = ajaxResponse.getRequestParameter("certresource");
        String certflag = ajaxResponse.getRequestParameter("certflag");
        String getCertAnalystOnly = ajaxResponse.getRequestParameter("getcertanalystonly");
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance from sdiworkitem where sdiworkitemid in (" + safeSQL.addIn(sdiworkitemid, ";") + ")";
        DataSet ds1 = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        String rsetid1 = "";
        try {
            rsetid1 = this.getDAMProcessor().createRSetWI(StringUtil.split(ds1.getColumnValues("sdcid", ";"), ";")[0], ds1.getColumnValues("keyid1", ";"), ds1.getColumnValues("keyid2", ";"), ds1.getColumnValues("keyid3", ";"), ds1.getColumnValues("workitemid", ";"), ds1.getColumnValues("workiteminstance", ";"), true);
        }
        catch (SapphireException e) {
            e.printStackTrace();
            throw new ServletException((Throwable)e);
        }
        safeSQL.reset();
        String sql1 = "select d.sdcid, d.keyid1, d.keyid2, d.keyid3, d.paramlistid, d.paramlistversionid, d.variantid, d.dataset, d.sdidataid from sdidata d, rsetitemsds r where r.rsetid in (" + safeSQL.addIn(rsetid1, ";") + ") and r.sdcid = d.sdcid and r.keyid1 = d.keyid1 and r.keyid2=d.keyid2 and r.keyid3=d.keyid3 and r.paramlistid = d.paramlistid and r.paramlistversionid = d.paramlistversionid and r.variantid = d.variantid and r.dataset = d.dataset ";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql1, safeSQL.getValues());
        String resourceid = "";
        if (certresource.equals("user")) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("paramlistId", ds.getColumnValues("paramlistid", ";"));
            actionProps.setProperty("paramlistVersionId", ds.getColumnValues("paramlistversionid", ";"));
            actionProps.setProperty("variantId", ds.getColumnValues("variantid", ";"));
            actionProps.setProperty("sampleId", sampleid);
            actionProps.setProperty("connectionId", this.getConnectionId());
            actionProps.setProperty("sysUserId", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
            try {
                this.getActionProcessor().processActionClass(InvokeUserCertProc.class.getName(), actionProps);
            }
            catch (ActionException e) {
                throw new ServletException((Throwable)e);
            }
            String rsetid = actionProps.getProperty("rsetResult");
            DataSet userds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT keyid1 FROM rsetitems WHERE rsetid=?", new Object[]{rsetid});
            resourceid = userds.getColumnValues("keyid1", ";");
            this.getDAMProcessor().clearRSet(rsetid);
        } else if (certresource.equals("instrument")) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("paramlistId", this.getUniqueValues(ds.getColumnValues("paramlistid", ";")));
            actionProps.setProperty("paramlistVersionId", this.getUniqueValues(ds.getColumnValues("paramlistversionid", ";")));
            actionProps.setProperty("variantId", this.getUniqueValues(ds.getColumnValues("variantid", ";")));
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
            DataSet insds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT keyid1 FROM rsetitems WHERE rsetid=?", new Object[]{rsetid});
            resourceid = insds.getColumnValues("keyid1", ";");
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (getCertAnalystOnly != null && getCertAnalystOnly.equals("true")) {
            ajaxResponse.addCallbackArgument("resourceid", resourceid);
        } else {
            ajaxResponse.addCallbackArgument("sdcid", ds.getColumnValues("sdcid", ";"));
            ajaxResponse.addCallbackArgument("keyid1", ds.getColumnValues("keyid1", ";"));
            ajaxResponse.addCallbackArgument("keyid2", ds.getColumnValues("keyid2", ";"));
            ajaxResponse.addCallbackArgument("keyid3", ds.getColumnValues("keyid3", ";"));
            ajaxResponse.addCallbackArgument("paramlistid", ds.getColumnValues("paramlistid", ";"));
            ajaxResponse.addCallbackArgument("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
            ajaxResponse.addCallbackArgument("variantid", ds.getColumnValues("variantid", ";"));
            ajaxResponse.addCallbackArgument("dataset", ds.getColumnValues("dataset", ";"));
            ajaxResponse.addCallbackArgument("sdidataid", ds.getColumnValues("sdidataid", ";"));
            ajaxResponse.addCallbackArgument("resourceid", resourceid);
        }
        ajaxResponse.print();
    }

    private String getUniqueValues(String delimstr) {
        String str2Ret = "";
        String[] strArr = StringUtil.split(delimstr, ";");
        for (int i = 0; i < strArr.length; ++i) {
            if (str2Ret.contains(strArr[i])) continue;
            str2Ret = str2Ret + ";" + strArr[i];
        }
        return str2Ret.substring(1);
    }
}

