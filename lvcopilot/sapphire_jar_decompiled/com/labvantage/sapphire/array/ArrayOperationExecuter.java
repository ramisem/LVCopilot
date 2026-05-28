/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.array;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ArrayOperationExecuter
extends BaseAjaxRequest {
    public static final String DELIMITER = ";";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "top.arrayOperation_AjaxCallback");
        String arrayOp = ajaxResponse.getRequestParameter("arrayop");
        String returntolistpage = ajaxResponse.getRequestParameter("returntolistpage", "LV_ArrayList");
        String retMessage = "";
        if (arrayOp.equals("AddArray")) {
            retMessage = this.createArray(ajaxResponse);
        } else if (arrayOp.equals("ApplyArrayMethod")) {
            retMessage = this.applyArrayMethod(ajaxResponse, false, "", returntolistpage);
        } else if (arrayOp.equals("LoadArray")) {
            retMessage = this.applyArrayMethod(ajaxResponse, true, "ArrayLoadingMaint", returntolistpage);
        } else if (arrayOp.equals("LoadArrayZone")) {
            retMessage = this.applyArrayMethod(ajaxResponse, true, "LoadArrayZone", returntolistpage);
        }
        ajaxResponse.addCallbackArgument("returnmessage", retMessage);
        ajaxResponse.print();
    }

    private String createArray(AjaxResponse ajaxResponse) {
        PropertyList properties = new PropertyList();
        String arraylayoutid = ajaxResponse.getRequestParameter("arraylayoutid").trim();
        String arraylayoutversionid = ajaxResponse.getRequestParameter("arraylayoutversionid").trim();
        String arraymethodid = ajaxResponse.getRequestParameter("arraymethodid").trim();
        String arraymethodversionid = ajaxResponse.getRequestParameter("arraymethodversionid").trim();
        String numofcopies = ajaxResponse.getRequestParameter("numofcopies").trim();
        String auditreason = ajaxResponse.getRequestParameter("auditreason", "");
        String auditactivity = ajaxResponse.getRequestParameter("auditactivity", "");
        String auditsignedflag = ajaxResponse.getRequestParameter("auditsignedflag", "");
        if (arraylayoutid == null || arraylayoutid.length() == 0) {
            if (arraymethodid == null || arraymethodid.trim().length() == 0) {
                return "ERROR: Either layout or method should be specified to create the array";
            }
            String sql = "SELECT arraylayoutid, arraylayoutversionid, arraytypeid, arraytypeversionid FROM arraymethod WHERE arraymethodid=? AND arraymethodversionid=?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{arraymethodid, arraymethodversionid});
            if (ds != null && ds.getRowCount() > 0) {
                arraylayoutid = ds.getString(0, "arraylayoutid", "");
                arraylayoutversionid = ds.getString(0, "arraylayoutversionid", "1");
                if (arraylayoutid.length() == 0) {
                    properties.setProperty("arraytypeid", ds.getString(0, "arraytypeid", ""));
                    properties.setProperty("arraytypeversionid", ds.getString(0, "arraytypeversionid", "1"));
                }
            } else {
                return "ERROR: Failed to fetch layout for method:" + arraymethodid;
            }
        }
        properties.setProperty("arraylayoutid", arraylayoutid);
        properties.setProperty("arraylayoutversionid", arraylayoutversionid);
        properties.setProperty("copies", numofcopies);
        properties.setProperty("auditreason", auditreason);
        properties.setProperty("auditactivity", auditactivity);
        properties.setProperty("auditsignedflag", auditsignedflag);
        try {
            if (arraymethodid.length() > 0) {
                if (arraylayoutid.length() == 0) {
                    throw new SapphireException("Cannot apply arraymethod " + arraymethodid + " with out a layout");
                }
                properties.setProperty("arraymethodid", arraymethodid);
                properties.setProperty("arraymethodversionid", arraymethodversionid);
            }
            this.getActionProcessor().processAction("CreateArray", "1", properties);
        }
        catch (SapphireException e) {
            return "ERROR: Failed to create Array: " + e.getMessage();
        }
        String arrayids = properties.getProperty("arrayid");
        return "rc?command=page&page=LV_ArrayList&sdcid=LV_Array&keyid1=" + arrayids + "";
    }

    private String applyArrayMethod(AjaxResponse ajaxResponse, boolean redirect, String pageid, String returntolistpage) {
        String arrayid = ajaxResponse.getRequestParameter("arrayid");
        String auditreason = ajaxResponse.getRequestParameter("auditreason");
        String auditactivity = ajaxResponse.getRequestParameter("auditactivity");
        String auditsignedflag = ajaxResponse.getRequestParameter("auditsignedflag");
        PropertyList properties = new PropertyList();
        properties.setProperty("arrayid", arrayid);
        properties.setProperty("arraymethodid", ajaxResponse.getRequestParameter("arraymethodid"));
        properties.setProperty("arraymethodversionid", ajaxResponse.getRequestParameter("arraymethodversionid"));
        properties.setProperty("auditreason", auditreason);
        properties.setProperty("auditactivity", auditactivity);
        properties.setProperty("auditsignedflag", auditsignedflag);
        try {
            this.getActionProcessor().processAction("ApplyArrayMethod", "1", properties);
        }
        catch (SapphireException e) {
            return "ERROR: " + e.getMessage();
        }
        if (!redirect) {
            return "rc?command=page&page=" + returntolistpage + "&sdcid=LV_Array&keyid1=" + arrayid + "";
        }
        return "rc?command=page&page=" + pageid + "&keyid1=" + arrayid + "&methodid=" + ajaxResponse.getRequestParameter("arraymethodid") + "&methodversionid=" + ajaxResponse.getRequestParameter("arraymethodversionid") + "&returntolistpage=" + returntolistpage + "&auditreason=" + auditreason + "&auditactivity=" + auditactivity + "&auditsignedflag=" + auditsignedflag;
    }
}

