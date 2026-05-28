/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.joda.time.DateTimeComparator
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTimeComparator;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateScannedContainer
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        QueryProcessor qp = this.getQueryProcessor();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        TranslationProcessor tp = this.getTranslationProcessor();
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
        String paramlistid = ajaxResponse.getRequestParameter("paramlistid", "");
        String paramlistversionid = ajaxResponse.getRequestParameter("paramlistversionid", "");
        String variantid = ajaxResponse.getRequestParameter("variantid", "");
        String reagenttypeid = ajaxResponse.getRequestParameter("reagenttypeid", "");
        String reagenttypeversionid = ajaxResponse.getRequestParameter("reagenttypeversionid", "");
        String reagentlottitle = ajaxResponse.getRequestParameter("reagentlottitle", "Reagent Lot");
        String trackitemtitle = ajaxResponse.getRequestParameter("trackitemtitle", "Container");
        String trackitemindexid = ajaxResponse.getRequestParameter("trackitemindexid");
        String selectclause = ajaxResponse.getRequestParameter("selectclause");
        selectclause = StringUtil.replaceAll(selectclause, "%3B", ",");
        String donotfirelotonchange = ajaxResponse.getRequestParameter("donotfirelotonchange");
        String errorMsg = null;
        boolean isDataRelation = sdcid.equalsIgnoreCase("Sample");
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("SELECT trackitem.trackitemid,trackitem.trackitemstatus,trackitem.qtycurrent,trackitem.qtycurrenttype,trackitem.qtyunits");
        sql.append(",trackitem.expirydt trackitemExpiredt,reagentlot.reagentlotid,reagentlot.reagentstatus,reagentlot.expirydt reagentExpiredt");
        sql.append(",reagentlot.reagenttypeid,reagentlot.reagenttypeversionid ").append(isDataRelation ? ",sc.certificationstatus" : "");
        if (selectclause.length() > 0) {
            sql.append("," + selectclause);
        }
        sql.append(" FROM trackitem,reagentlot").append(isDataRelation ? ",s_sdicertification sc" : "");
        sql.append(",(SELECT coalesce(NULLIF(rt2.reagenttypeid,'') ,rt1.reagenttypeid) reagenttypeid, coalesce(NULLIF(rt2.reagenttypeversionid,'') ,rt1.reagenttypeversionid) reagenttypeversionid  FROM reagenttype rt1 left join reagenttype rt2 on rt1.activematerialid=rt2.activematerialid where rt1.reagenttypeid=" + safeSQL.addVar(reagenttypeid) + " AND rt1.reagenttypeversionid=coalesce(NULLIF(" + safeSQL.addVar(reagenttypeversionid) + ",'') ,rt1.reagenttypeversionid)) reagenttypealias ");
        sql.append(" WHERE trackitem.trackitemid=" + safeSQL.addVar(trackitemid));
        sql.append(" AND trackitem.linksdcid='LV_ReagentLot'");
        sql.append(" AND trackitem.linkkeyid1=reagentlot.reagentlotid");
        sql.append(" AND reagentlot.reagenttypeid=reagenttypealias.reagenttypeid");
        sql.append(" AND reagentlot.reagenttypeversionid=reagenttypealias.reagenttypeversionid");
        if (isDataRelation) {
            sql.append(" AND sc.resourcesdcid='LV_ReagentLot'");
            sql.append(" AND sc.resourcekeyid1=reagentlot.reagentlotid");
            sql.append(" AND (CERTIFIEDFORKEYID1='*' OR (CERTIFIEDFORKEYID1=" + safeSQL.addVar(paramlistid));
            sql.append(" AND CERTIFIEDFORKEYID2=" + safeSQL.addVar(paramlistversionid) + " AND CERTIFIEDFORKEYID3=" + safeSQL.addVar(variantid) + "))");
        }
        String reagentlotid = "";
        String qtycurrent = "";
        String qtyunits = "";
        String qtycurrenttype = "";
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            String trackitemstatus = ds.getString(0, "trackitemstatus", "");
            String reagentstatus = ds.getString(0, "reagentstatus", "");
            String certificationstatus = ds.getString(0, "certificationstatus", "");
            Timestamp trackitemExpiredt = ds.getTimestamp(0, "trackitemExpiredt");
            Timestamp reagentExpiredt = ds.getTimestamp(0, "reagentExpiredt");
            Calendar trackitemExpireDate = Calendar.getInstance();
            Calendar reagentExpireDate = Calendar.getInstance();
            if (trackitemExpiredt != null) {
                trackitemExpireDate.setTime(trackitemExpiredt);
            }
            if (reagentExpiredt != null) {
                reagentExpireDate.setTime(reagentExpiredt);
            }
            if (!ReagentUtil.hasDepartmentalSecurityAccess("LV_ReagentLot", reagentlotid = ds.getString(0, "reagentlotid", ""), "", "", this.getSDCProcessor(), this.getConnectionId())) {
                errorMsg = "Due to departmental security You are not allowed to access this container";
            } else if (trackitemExpiredt != null && this.isDatePassedToday(trackitemExpireDate) || "Expired".equalsIgnoreCase(trackitemstatus)) {
                errorMsg = trackitemtitle + " has been expired";
            } else if (reagentExpiredt != null && this.isDatePassedToday(reagentExpireDate) || "Expired".equalsIgnoreCase(reagentstatus)) {
                errorMsg = reagentlottitle + " has been expired";
            } else if (!"Valid".equalsIgnoreCase(trackitemstatus)) {
                errorMsg = trackitemtitle + " status not Valid.";
            } else if (!"Active".equalsIgnoreCase(reagentstatus)) {
                errorMsg = reagentlottitle + " status not Active.";
            } else if (isDataRelation && !"Valid".equalsIgnoreCase(certificationstatus)) {
                errorMsg = reagentlottitle + " is not certified";
            } else {
                reagentlotid = ds.getString(0, "reagentlotid", "");
                qtycurrent = ds.getValue(0, "qtycurrent", "");
                qtyunits = ds.getValue(0, "qtyunits", "");
                qtycurrenttype = ds.getValue(0, "qtycurrenttype", "");
            }
        } else {
            ds = new DataSet();
            errorMsg = trackitemtitle + " not found.";
        }
        if (errorMsg == null) {
            ajaxResponse.addCallbackArgument("errormsg", errorMsg);
        } else {
            ajaxResponse.addCallbackArgument("errormsg", tp.translate(errorMsg));
        }
        ajaxResponse.addCallbackArgument("reagentlotid", reagentlotid);
        ajaxResponse.addCallbackArgument("qtycurrent", qtycurrent);
        ajaxResponse.addCallbackArgument("qtyunits", qtyunits);
        ajaxResponse.addCallbackArgument("trackitemindexid", trackitemindexid);
        ajaxResponse.addCallbackArgument("qtycurrenttype", qtycurrenttype);
        ajaxResponse.addCallbackArgument("ds", ds);
        ajaxResponse.addCallbackArgument("donotfirelotonchange", donotfirelotonchange);
        ajaxResponse.print();
    }

    private boolean isDatePassedToday(Calendar date) {
        boolean containsTimeComponent;
        Calendar todayDate = Calendar.getInstance();
        todayDate.setTime(new Date());
        boolean bl = containsTimeComponent = date.get(11) + date.get(12) + date.get(13) > 0;
        if (containsTimeComponent) {
            return todayDate.after(date);
        }
        DateTimeComparator dateTimeComparator = DateTimeComparator.getDateOnlyInstance();
        return dateTimeComparator.compare((Object)date, (Object)todayDate) < 0;
    }
}

