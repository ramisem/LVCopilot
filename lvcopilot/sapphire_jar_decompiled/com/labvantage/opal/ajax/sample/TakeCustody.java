/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.sample;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class TakeCustody
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String reservelocation = ajaxResponse.getRequestParameter("reservelocation");
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid");
        String auditreason = ajaxResponse.getRequestParameter("auditreason");
        String message = "";
        if (OpalUtil.isNotEmpty(trackitemid)) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select ti.trackitemid, ti.linksdcid, ti.linkkeyid1, ti.custodialuserid, ti.custodialdepartmentid, p.s_packageid, p.packagetype, p.packagestatus, p.recipientdepartmentid");
            sql.append(" ,(select sysuser.sysuserdesc from sysuser where sysuser.sysuserid = ti.custodialuserid) sysusername");
            sql.append(" ,(select su1.labelpath from storageunit su1 where ti.currentstorageunitid = su1.storageunitid) locationpath");
            sql.append(" from trackitem ti left outer join s_package p on ti.currentstorageunitid = (select su.storageunitid from storageunit su");
            sql.append(" where su.linksdcid = 'LV_Package' and su.linkkeyid1 = p.s_packageid)");
            sql.append(" where ti.linksdcid = 'Sample'");
            sql.append(" and ti.trackitemid in (").append(safeSQL.addIn(trackitemid, ";")).append(" )");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                ArrayList<String> receivePackageList = new ArrayList<String>();
                for (int i = 0; i < ds.size(); ++i) {
                    String packagestatus;
                    String packageid = ds.getString(i, "s_packageid");
                    if (!OpalUtil.isNotEmpty(packageid) || !"Shipped".equals(packagestatus = ds.getString(i, "packagestatus")) && !"Expected".equals(packagestatus)) continue;
                    receivePackageList.add(packageid);
                }
                if (receivePackageList.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_Package");
                    props.setProperty("keyid1", OpalUtil.toDelimitedString(receivePackageList, ";"));
                    props.setProperty("packagestatus", "Received");
                    props.setProperty("__sdcruleconfirm", "Y");
                    try {
                        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                    }
                    catch (ActionException e) {
                        message = this.getTranslationProcessor().translate("Error while receiving package") + "<br>Exception:" + e.getMessage();
                        e.printStackTrace(System.out);
                    }
                }
            }
            if (OpalUtil.isEmpty(message)) {
                List<String> reserveList = null;
                List<String> takecustodyList = OpalUtil.toList(trackitemid, ";");
                if (OpalUtil.isNotEmpty(reservelocation)) {
                    reserveList = OpalUtil.toList(reservelocation, ";");
                    takecustodyList.removeAll(reserveList);
                }
                try {
                    PropertyList props = new PropertyList();
                    props.setProperty("trackitemid", OpalUtil.toDelimitedString(takecustodyList, ";"));
                    props.setProperty("custodytakendt", "n");
                    props.setProperty("currentstorageunitid", "(null)");
                    props.setProperty("custodialuserid", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                    props.setProperty("custodialdepartmentid", this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment());
                    props.setProperty("reservelocation", "N");
                    props.setProperty("__bypasscustodyrules", "Y");
                    props.setProperty("__sdcruleconfirm", "Y");
                    props.setProperty("auditreason", auditreason);
                    this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
                    if (OpalUtil.isNotEmpty(reserveList)) {
                        props.clear();
                        props.setProperty("trackitemid", OpalUtil.toDelimitedString(reserveList, ";"));
                        props.setProperty("custodytakendt", "n");
                        props.setProperty("currentstorageunitid", "(null)");
                        props.setProperty("custodialuserid", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                        props.setProperty("custodialdepartmentid", this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment());
                        props.setProperty("reservelocation", "Y");
                        props.setProperty("__bypasscustodyrules", "Y");
                        props.setProperty("__sdcruleconfirm", "Y");
                        props.setProperty("auditreason", auditreason);
                        this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
                    }
                }
                catch (ActionException e) {
                    message = "Error while taking custody: " + e.getMessage();
                    e.printStackTrace(System.out);
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

