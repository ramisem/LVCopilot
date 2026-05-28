/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.kit;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ReserveRequestKits
extends BaseAjaxRequest {
    public static final String ID = "ReserveRequestKits";
    public static final String PROPERTY_REQUESTID = "requestid";
    public static final String PROPERTY_REQUESTITEMID = "requestitemid";
    public static final String PROPERTY_TRACKITEMID = "trackitemid";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        DataSet ds;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String requestid = ajaxResponse.getRequestParameter(PROPERTY_REQUESTID, "").trim();
        String requestitemid = ajaxResponse.getRequestParameter(PROPERTY_REQUESTITEMID, "").trim();
        int reservecount = Integer.parseInt(ajaxResponse.getRequestParameter("reservecount", "0").trim());
        String trackitemid = ajaxResponse.getRequestParameter(PROPERTY_TRACKITEMID, "").trim();
        if (requestitemid.length() == 0) {
            message = this.getTranslationProcessor().translate("Missing mandatory input") + " :" + PROPERTY_REQUESTITEMID;
        }
        if (trackitemid.length() == 0 && reservecount == 0) {
            message = this.getTranslationProcessor().translate("Reserve count must be more than 0 (zero)");
        }
        if (message.length() == 0 && trackitemid.length() == 0 && OpalUtil.isNotEmpty(ds = this.getQueryProcessor().getPreparedSqlDataSet("select reagentclass, reagenttypeid, reagenttypeversionid from s_requestitem where s_requestitemid = ?", (Object[])new String[]{requestitemid}))) {
            String reagenttypeid = ds.getString(0, "reagenttypeid", "");
            String reagenttypeversionid = ds.getValue(0, "reagenttypeversionid", "1");
            String sql = "select trackitem.trackitemid from trackitem, reagentlot where trackitem.linksdcid = 'LV_ReagentLot' and trackitem.linkkeyid1 = reagentlot.reagentlotid and reagentlot.reagenttypeid = ? and reagentlot.reagenttypeversionid = ? and trackitem.trackitemstatus = 'Valid'";
            DataSet ds2 = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{reagenttypeid, reagenttypeversionid});
            if (OpalUtil.isNotEmpty(ds2)) {
                DataSet ds3 = new DataSet();
                if (reservecount <= ds2.size()) {
                    for (int i = 0; i < reservecount; ++i) {
                        ds3.copyRow(ds2, i, 1);
                    }
                    trackitemid = ds3.getColumnValues(PROPERTY_TRACKITEMID, ";");
                } else {
                    message = this.getTranslationProcessor().translate("Requested quantity no longer available to be reserved. Please use Generate & Reserve.");
                }
            }
        }
        if (message.length() == 0 && trackitemid.length() > 0) {
            if (requestid.length() == 0) {
                requestid = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_requestitem", PROPERTY_REQUESTID, "s_requestitemid=?", new String[]{requestitemid});
            }
            try {
                int copies = trackitemid.split(";").length;
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_RequestItemDetail");
                props.setProperty("copies", String.valueOf(copies));
                props.setProperty(PROPERTY_REQUESTID, requestid);
                props.setProperty(PROPERTY_REQUESTITEMID, requestitemid);
                props.setProperty("linksdcid", "TrackItemSDC");
                props.setProperty("linkkeyid1", trackitemid);
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                props.clear();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("keyid1", trackitemid);
                props.setProperty("trackitemstatus", "Reserved");
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            catch (ActionException e) {
                message = e.getMessage();
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

