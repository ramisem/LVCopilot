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

import com.labvantage.sapphire.DateTimeUtil;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;

public class SynchronizeActionProperties
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String error = "Operation Successful";
        String operation = ajaxResponse.getRequestParameter("operation").trim();
        if (operation.equalsIgnoreCase("add")) {
            try {
                this.addProperty(ajaxResponse);
                ajaxResponse.addCallbackArgument("response", "Action Property added successfully");
            }
            catch (SapphireException e) {
                Logger.logError("Error in adding property to the database, action: " + ajaxResponse.getRequestParameter("actionid") + " from the database ", e);
                error = "Error in adding property to the database, action: " + ajaxResponse.getRequestParameter("actionid") + e.getMessage();
                ajaxResponse.setError(error);
            }
        } else if (operation.equalsIgnoreCase("delete")) {
            try {
                this.deleteProperty(ajaxResponse);
                ajaxResponse.addCallbackArgument("response", "Action Property Deleted successfully");
            }
            catch (ActionException e) {
                Logger.logError("Error in removing property from the database, action: " + ajaxResponse.getRequestParameter("actionid") + " from the database ", e);
                error = "Error in removing property from the database, action: " + ajaxResponse.getRequestParameter("actionid") + e.getMessage();
                ajaxResponse.setError(error);
            }
        } else if (operation.equalsIgnoreCase("deleteAction")) {
            try {
                this.deleteAction(ajaxResponse);
                ajaxResponse.addCallbackArgument("response", "Action deleted successfully");
            }
            catch (ActionException e) {
                Logger.logError("Error in removing action: " + ajaxResponse.getRequestParameter("actionid") + " from the database ", e);
                error = "Error in removing action: " + ajaxResponse.getRequestParameter("actionid") + " from the database " + e.getMessage();
                ajaxResponse.setError(error);
            }
        } else if (operation.equalsIgnoreCase("insertAll")) {
            try {
                this.insertAll(ajaxResponse);
                ajaxResponse.addCallbackArgument("response", "Action Properties added successfully");
            }
            catch (ActionException e) {
                Logger.logError("Error in adding properties to the database ", e);
                error = "Error in adding properties to the database " + e.getMessage();
                ajaxResponse.setError(error);
            }
        }
        ajaxResponse.print();
    }

    private void insertAll(AjaxResponse ajaxResponse) throws ActionException {
        DataSet ds = new DataSet();
        ds.addColumnValues("actionid", 0, ajaxResponse.getRequestParameter("actionid").trim(), ";");
        ds.addColumnValues("actionversionid", 0, ajaxResponse.getRequestParameter("actionversionid").trim(), ";");
        ds.addColumnValues("propertyid", 0, ajaxResponse.getRequestParameter("propertyid").trim(), ";");
        ds.addColumnValues("propertytype", 0, ajaxResponse.getRequestParameter("propertytype").trim(), ";");
        ds.addColumnValues("propertyhelp", 0, ajaxResponse.getRequestParameter("propertyhelp").trim(), ";");
        ds.addColumnValues("propertytitle", 0, ajaxResponse.getRequestParameter("propertytitle").trim(), ";");
        ds.addColumnValues("propertytypeflag", 0, ajaxResponse.getRequestParameter("propertytypeflag").trim(), ";");
        ds.addColumnValues("usersequence", 0, ajaxResponse.getRequestParameter("usersequence").trim(), ";");
        ds.sort("actionid,actionversionid");
        ArrayList<DataSet> dsAL = ds.getGroupedDataSets("actionid,actionversionid");
        for (int i = 0; i < dsAL.size(); ++i) {
            DataSet actionDS = dsAL.get(0);
            String actionid = actionDS.getValue(0, "actionid");
            String actionversionid = actionDS.getValue(0, "actionversionid");
            HashMap<String, Object> props = new HashMap<String, Object>();
            props.put("sdcid", "Action");
            props.put("linkid", "properties");
            props.put("keyid1", actionid);
            props.put("keyid2", actionversionid);
            props.put("propertyid", actionDS.getColumnValues("propertyid", ";"));
            props.put("propertytype", actionDS.getColumnValues("propertytype", ";"));
            props.put("propertyhelp", actionDS.getColumnValues("propertyhelp", ";"));
            props.put("propertytitle", actionDS.getColumnValues("propertytitle", ";"));
            props.put("propertytypeflag", actionDS.getColumnValues("propertytypeflag", ";"));
            props.put("usersequence", actionDS.getColumnValues("usersequence", ";"));
            props.put("createdt", DateTimeUtil.getNowTimestamp());
            this.getActionProcessor().processAction("AddSDIDetail", "1", props);
        }
    }

    private void deleteAction(AjaxResponse ajaxResponse) throws ActionException {
        String actionid = ajaxResponse.getRequestParameter("actionid").trim();
        String actionversionid = ajaxResponse.getRequestParameter("actionversionid").trim();
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", "Action");
        props.put("keyid1", actionid);
        props.put("keyid2", actionversionid);
        this.getActionProcessor().processAction("DeleteSDI", "1", props);
    }

    private void deleteProperty(AjaxResponse ajaxResponse) throws ActionException {
        String actionid = ajaxResponse.getRequestParameter("actionid").trim();
        String actionversionid = ajaxResponse.getRequestParameter("actionversionid").trim();
        String propertyid = ajaxResponse.getRequestParameter("propertyid").trim();
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", "Action");
        props.put("linkid", "properties");
        props.put("keyid1", actionid);
        props.put("keyid2", actionversionid);
        props.put("propertyid", propertyid);
        this.getActionProcessor().processAction("DeleteSDIDetail", "1", props);
    }

    private void addProperty(AjaxResponse ajaxResponse) throws SapphireException {
        HashMap<String, Object> props;
        String actionid = ajaxResponse.getRequestParameter("actionid").trim();
        String actionversionid = ajaxResponse.getRequestParameter("actionversionid").trim();
        String sql = "SELECT count(actionid) FROM action where actionid = ?";
        if (this.getQueryProcessor().getPreparedCount(sql, new Object[]{actionid}) == 0) {
            props = new HashMap<String, Object>();
            props.put("sdcid", "Action");
            props.put("keyid1", actionid);
            props.put("keyid2", actionversionid);
            props.put("actiontype", "S");
            props.put("objectname", ajaxResponse.getRequestParameter("objectname").trim());
            props.put("actionlanguage", "java");
            props.put("versionstatus", "P");
            this.getActionProcessor().processAction("AddSDI", "1", props);
        }
        props = new HashMap();
        props.put("sdcid", "Action");
        props.put("linkid", "properties");
        props.put("keyid1", actionid);
        props.put("keyid2", actionversionid);
        props.put("propertyid", ajaxResponse.getRequestParameter("propertyid").trim());
        props.put("propertytype", ajaxResponse.getRequestParameter("propertytype").trim());
        props.put("propertyhelp", ajaxResponse.getRequestParameter("propertyhelp").trim());
        props.put("propertytitle", ajaxResponse.getRequestParameter("propertytitle").trim());
        props.put("defaultvalue", ajaxResponse.getRequestParameter("propertydefault").trim());
        props.put("propertytypeflag", ajaxResponse.getRequestParameter("propertyflag").trim());
        props.put("usersequence", ajaxResponse.getRequestParameter("usersequence").trim());
        props.put("createdt", DateTimeUtil.getNowTimestamp());
        this.getActionProcessor().processAction("AddSDIDetail", "1", props);
    }

    private void deleteProperties(String propstobedelted, String actionid, String actionversionid) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "DELETE FROM actionproperty WHERE actionid = " + safeSQL.addVar(actionid) + " AND actionversionid =" + safeSQL.addVar(actionversionid) + " AND  propertyid in (" + safeSQL.addIn(propstobedelted, ";") + ")";
        this.getQueryProcessor().execPreparedUpdate(sql, safeSQL.getValues());
    }
}

