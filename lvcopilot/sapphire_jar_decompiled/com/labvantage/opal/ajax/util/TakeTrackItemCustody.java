/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.util;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class TakeTrackItemCustody
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String status = "";
        String message = "";
        String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "").trim();
        String custodialuserid = ajaxResponse.getRequestParameter("custodialuserid", sysuserid);
        String custodialdepartmentid = ajaxResponse.getRequestParameter("custodialdepartmentid", this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment());
        String bypasscustodyrules = ajaxResponse.getRequestParameter("__bypasscustodyrules", "N");
        String confirmflag = ajaxResponse.getRequestParameter("confirmflag", "N");
        if (trackitemid.length() > 0) {
            if (!"Y".equals(confirmflag)) {
                String rsetid = null;
                try {
                    String userglpflag = OpalUtil.getColumnValue(this.getQueryProcessor(), "sysuser", "glpflag", "sysuserid = ?", new String[]{custodialuserid});
                    if (!"Y".equals(userglpflag)) {
                        rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", trackitemid, null, null);
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_sample.s_sampleid from s_sample, trackitem where s_sample.s_sampleid = trackitem.linkkeyid1 and trackitem.linksdcid = 'Sample' and trackitem.trackitemid in (select r.keyid1 from rsetitems r where r.rsetid = ?) and s_sample.glpflag = 'Y'", (Object[])new String[]{rsetid});
                        if (ds != null && ds.size() > 0) {
                            status = "CONFIRM";
                            message = this.getTranslationProcessor().translate("On or more of the sample(s) will lose GLP. Continue?");
                        }
                    }
                }
                catch (SapphireException e) {
                    status = "ERROR";
                    message = this.getTranslationProcessor().translate("Error validating GLP. If problem persists, please contact your Administrator.");
                }
                finally {
                    if (OpalUtil.isNotEmpty(rsetid)) {
                        this.getDAMProcessor().clearRSet(rsetid);
                    }
                }
            }
            if (OpalUtil.isEmpty(status)) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("keyid1", trackitemid);
                props.setProperty("custodialuserid", custodialuserid);
                props.setProperty("custodialdepartmentid", custodialdepartmentid);
                props.setProperty("currentstorageunitid", "(null)");
                props.setProperty("__bypasscustodyrules", bypasscustodyrules);
                props.setProperty("__sdcruleconfirm", confirmflag);
                try {
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                }
                catch (ActionException e) {
                    status = "ERROR";
                    message = this.getTranslationProcessor().translate("Failed to Take Custody. If problem persists, please contact your Administrator.");
                    e.printStackTrace();
                }
            }
        }
        ajaxResponse.addCallbackArgument("status", status);
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

