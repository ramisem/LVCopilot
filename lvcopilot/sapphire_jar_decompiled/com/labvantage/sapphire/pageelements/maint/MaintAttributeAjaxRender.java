/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.ajax.operations.AddActivityLog;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.Attributes;
import com.labvantage.sapphire.pageelements.maint.MaintAttribute;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.tagext.SDITagInfo;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;

public class MaintAttributeAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 87916 $";

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block51: {
            ajaxResponse = new AjaxResponse(request, response, "MaintAttributeHandler");
            String elementid = ajaxResponse.getRequestParameter("elementid", "");
            if (elementid.length() > 0) {
                String data = ajaxResponse.getRequestParameter("attributedata", "");
                DataSet attributedata = null;
                if (data.length() > 0) {
                    this.logger.debug("Data provided through Ajax.");
                    try {
                        attributedata = new DataSet(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                        attributedata.setJSONObject(new JSONObject(data));
                    }
                    catch (Exception e) {
                        this.logger.error("Data string provided is not valid.");
                    }
                }
                String mode = ajaxResponse.getRequestParameter("mode", "view");
                if (attributedata != null) {
                    PropertyList element = null;
                    String props = ajaxResponse.getRequestParameter("properties");
                    if (props.length() > 0) {
                        try {
                            element = new PropertyList(new JSONObject(props));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    if (element != null) {
                        if (mode.equalsIgnoreCase("reset")) {
                            Object em1 = null;
                            Object em2 = null;
                            ajaxResponse.addCallbackArgument("elementid", elementid);
                            ajaxResponse.addCallbackArgument("html", "");
                            ajaxResponse.addCallbackArgument("script", "");
                            ajaxResponse.addCallbackArgument("data", (Object)em1);
                            ajaxResponse.addCallbackArgument("selected", (Object)em2);
                            ajaxResponse.addCallbackArgument("message", "");
                        } else {
                            try {
                                if (this.getServlet() != null) {
                                    PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
                                    JavaScriptAPITag.setProcessed(request, true);
                                    String rsetid = ajaxResponse.getRequestParameter("rsetid", "");
                                    String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
                                    String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
                                    String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
                                    String keyid3 = ajaxResponse.getRequestParameter("keyid3", "");
                                    String dynamicaudit = ajaxResponse.getRequestParameter("dynamicaudit", "-1");
                                    String webpageid = ajaxResponse.getRequestParameter("webpageid", "");
                                    String savetransaction = ajaxResponse.getRequestParameter("savetransaction", "");
                                    String rows = ajaxResponse.getRequestParameter("selected", "");
                                    JSONArray jrows = null;
                                    int[] selected = null;
                                    if (rows.length() > 0) {
                                        jrows = new JSONArray(rows);
                                    }
                                    if (sdcid.length() > 0 && keyid1.length() > 0) {
                                        AddActivityLog.ActivityLogData activityLogData = new AddActivityLog.ActivityLogData();
                                        HashMap<String, String> activityMap = new HashMap<String, String>();
                                        MaintAttribute ma = new MaintAttribute(pageContext, rsetid, sdcid, keyid1, keyid2, keyid3, attributedata);
                                        activityMap.put("sdcid", sdcid);
                                        activityMap.put("keyid1", keyid1);
                                        activityMap.put("keyid2", OpalUtil.isNotEmpty(keyid2) ? keyid2 : "(null)");
                                        activityMap.put("keyid3", OpalUtil.isNotEmpty(keyid2) ? keyid3 : "(null)");
                                        activityMap.put("webpageid", webpageid);
                                        activityMap.put("activitygroup", "" + System.currentTimeMillis());
                                        activityMap.put("savetransaction", savetransaction);
                                        ma.setElementid(elementid);
                                        ma.setElementProperties(element);
                                        ma.setShowContext(ajaxResponse.getRequestParameter("showcontext", "Y").equalsIgnoreCase("Y"));
                                        String message = "";
                                        if (mode.equalsIgnoreCase("add")) {
                                            String attributeid = ajaxResponse.getRequestParameter("attributeid", "");
                                            String attributesdcid = ajaxResponse.getRequestParameter("attributesdcid", "");
                                            String datatype = ajaxResponse.getRequestParameter("datatype", "");
                                            String instructionflag = ajaxResponse.getRequestParameter("instructionflag", "R");
                                            if (attributeid.length() <= 0 || attributesdcid.length() <= 0) throw new ServletException("Add mode detected but attributeid or attributesdcid not provided.");
                                            ArrayList<String> addedList = ma.addAttribute(attributeid, attributesdcid, datatype, instructionflag, "");
                                            for (int i = 0; i < addedList.size(); ++i) {
                                                HashMap<String, String> addedRowMap = new HashMap<String, String>();
                                                addedRowMap.putAll(activityMap);
                                                addedRowMap.put("activitytype", "Attribute_Add");
                                                addedRowMap.put("detailtableid", "sdiattribute");
                                                addedRowMap.put("detailkeyvalues", addedList.get(i));
                                                activityLogData.addActivity(addedRowMap);
                                            }
                                        } else if (mode.equalsIgnoreCase("remove") || mode.equalsIgnoreCase("move")) {
                                            if (jrows == null) {
                                                String attributeid = ajaxResponse.getRequestParameter("attributeid", "");
                                                String attributesdcid = ajaxResponse.getRequestParameter("attributesdcid", "");
                                                String attributeinstance = ajaxResponse.getRequestParameter("attributeinstance", "");
                                                if (attributeid.length() <= 0 || attributesdcid.length() <= 0 || attributeinstance.length() <= 0) throw new ServletException("Remove mode detected but either rows or attributeid, attributesdcid and attributeinstance not provided.");
                                                if (mode.equalsIgnoreCase("move")) {
                                                    int move;
                                                    try {
                                                        move = Integer.parseInt(ajaxResponse.getRequestParameter("move", "1"));
                                                    }
                                                    catch (Exception e) {
                                                        move = 1;
                                                    }
                                                    try {
                                                        ma.moveAttribute(attributeid, attributesdcid, attributeinstance, move);
                                                    }
                                                    catch (SapphireException e) {
                                                        this.logger.info("Could not move attribute(s). " + e.getMessage());
                                                    }
                                                } else {
                                                    ma.removeAttribute(attributeid, attributesdcid, attributeinstance);
                                                    activityMap.put("activitytype", "Attribute_Remove");
                                                    activityMap.put("detailtableid", "sdiattribute");
                                                    activityMap.put("detailkeyvalues", attributeid + ";" + attributesdcid + ";" + attributeinstance);
                                                    activityLogData.addActivity(activityMap);
                                                    selected = new int[]{};
                                                }
                                            } else if (mode.equalsIgnoreCase("move")) {
                                                int move;
                                                try {
                                                    move = Integer.parseInt(ajaxResponse.getRequestParameter("move", "1"));
                                                }
                                                catch (Exception e) {
                                                    move = 1;
                                                }
                                                try {
                                                    ma.moveAttribute(jrows, move);
                                                }
                                                catch (SapphireException e) {
                                                    this.logger.info("Could not move attribute(s). " + e.getMessage());
                                                }
                                            } else {
                                                ArrayList<String> removedList = ma.removeAttribute(jrows);
                                                for (int i = 0; i < removedList.size(); ++i) {
                                                    HashMap<String, String> removedMap = new HashMap<String, String>();
                                                    removedMap.putAll(activityMap);
                                                    removedMap.put("activitytype", "Attribute_Remove");
                                                    removedMap.put("detailtableid", "sdiattribute");
                                                    removedMap.put("detailkeyvalues", removedList.get(i));
                                                    activityLogData.addActivity(removedMap);
                                                }
                                                selected = new int[]{};
                                            }
                                        } else if (mode.equalsIgnoreCase("validate")) {
                                            // empty if block
                                        }
                                        if (jrows != null) {
                                            if (selected == null) {
                                                selected = new int[jrows.length()];
                                                for (int i = 0; i < jrows.length(); ++i) {
                                                    selected[i] = jrows.getInt(i);
                                                }
                                            }
                                            ma.setSelected(selected);
                                        }
                                        ajaxResponse.addCallbackArgument("elementid", elementid);
                                        String jsonStr = ajaxResponse.getRequestParameter("config", "");
                                        PropertyList config = new PropertyList();
                                        if (jsonStr.length() > 0) {
                                            try {
                                                config.setJSONString(jsonStr);
                                            }
                                            catch (JSONException removedMap) {
                                                // empty catch block
                                            }
                                        }
                                        if (config != null && "Standard".equalsIgnoreCase(config.getProperty("attributemode", "worksheet")) && config.getProperty("sourcesdcid").length() > 0) {
                                            boolean instructionOnlyCount = false;
                                            boolean fullWidth = "Y".equalsIgnoreCase(config.getProperty("fullwidth"));
                                            int instrOnly = 0;
                                            StringBuilder html = new StringBuilder();
                                            PropertyList sdc = this.getSDCProcessor().getPropertyList(attributedata.getValue(0, "sdcid"));
                                            ConnectionInfo connectionInfo = new ConnectionProcessor(pageContext).getConnectionInfo(this.getConnectionId());
                                            M18NUtil m18nServer = new M18NUtil(connectionInfo);
                                            SDITagInfo sdiInfo = new SDITagInfo(new HashMap());
                                            SDCProcessor sdcProcessor = new SDCProcessor(pageContext);
                                            sdiInfo = Attributes.createSDIInfo(rsetid, sdcid, keyid1, keyid2, keyid3, attributedata, this.getSDCProcessor());
                                            Attributes.renderStandardModeEditorHtml(html, sdc, attributedata, config, "maintAttribute", elementid, instrOnly, fullWidth, null, this.getTranslationProcessor(), this.getQueryProcessor(), this.getConnectionProcessor(), this.logger, false, pageContext, sdiInfo, m18nServer, connectionInfo, sdcProcessor, selected, "sdms_dcattributes");
                                            ajaxResponse.addCallbackArgument("html", html);
                                        } else {
                                            ajaxResponse.addCallbackArgument("html", ma.getHtml());
                                        }
                                        ajaxResponse.addCallbackArgument("script", ma.getScript());
                                        DataSet outdata = ma.getSDIInfo().getDataSet("attribute");
                                        ajaxResponse.addCallbackArgument("data", outdata.toJSONObject(true, outdata.getColumns(), false, true, true));
                                        ajaxResponse.addCallbackArgument("selected", selected != null && selected.length > 0 ? jrows : null);
                                        ajaxResponse.addCallbackArgument("message", message);
                                        if (!"-1".equals(dynamicaudit)) {
                                            HashMap<String, Object> activitylogRequestprops = new HashMap<String, Object>();
                                            activitylogRequestprops.put("reason", "");
                                            activitylogRequestprops.put("activityDataSet", activityLogData.getActivityDataSet());
                                            new RequestProcessor(this.getConnectionid()).processRequest("com.labvantage.sapphire.ajax.operations.AddActivityLogPropertyHandler", activitylogRequestprops);
                                        }
                                        break block51;
                                    }
                                    ajaxResponse.setError(this.getTranslationProcessor().translate("No sdcid or keyid1 provided."));
                                    break block51;
                                }
                                ajaxResponse.setError(this.getTranslationProcessor().translate("Servlet could not be found."));
                            }
                            catch (Exception e) {
                                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain attribute HTML."));
                                this.logger.error("Could not obtain attribute HTML.", e);
                            }
                        }
                    } else if (!mode.equalsIgnoreCase("reset")) {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("No properties found in session."));
                    } else {
                        this.logger.warn("No properties found in session. May have already been cleared.");
                        ajaxResponse.addCallbackArgument("elementid", elementid);
                        ajaxResponse.addCallbackArgument("html", "");
                        ajaxResponse.addCallbackArgument("script", "");
                        Object em1 = null;
                        Object em2 = null;
                        ajaxResponse.addCallbackArgument("data", (Object)em1);
                        ajaxResponse.addCallbackArgument("selected", (Object)em2);
                        ajaxResponse.addCallbackArgument("message", "");
                    }
                } else {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain attribute data."));
                    this.logger.error("Could not obtain attribute data");
                }
            } else {
                ajaxResponse.setError(this.getTranslationProcessor().translate("No element id provided."));
            }
        }
        ajaxResponse.print();
    }
}

