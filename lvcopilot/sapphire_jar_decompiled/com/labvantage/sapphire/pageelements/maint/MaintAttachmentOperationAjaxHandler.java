/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.pageelements.maint.MaintAttachmentOperation;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class MaintAttachmentOperationAjaxHandler
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block21: {
            AjaxResponse ajaxresponse = new AjaxResponse(request, response);
            try {
                Mode mode = Mode.REFRESH;
                try {
                    mode = Mode.valueOf(ajaxresponse.getRequestParameter("mode", Mode.REFRESH.toString()).toUpperCase());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (mode == Mode.REFRESH) {
                    String data;
                    String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                    String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                    String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                    String sdcid = ajaxresponse.getRequestParameter("sdcid");
                    boolean move = ajaxresponse.getRequestParameter("move", "N").equalsIgnoreCase("Y");
                    boolean viewonly = ajaxresponse.getRequestParameter("viewonly", "N").equalsIgnoreCase("Y");
                    PropertyList properties2 = null;
                    String props = ajaxresponse.getRequestParameter("properties");
                    if (props.length() > 0) {
                        try {
                            properties2 = new PropertyList(new JSONObject(props));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    DataSet capturedata = null;
                    if (!ajaxresponse.getRequestParameter("full").equalsIgnoreCase("Y") && (data = ajaxresponse.getRequestParameter("data")).length() > 0) {
                        try {
                            capturedata = new DataSet(new JSONObject(data));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    String elementid = ajaxresponse.getRequestParameter("elementid", "attachmentoperation");
                    MaintAttachmentOperation maintAttachmentOperation = new MaintAttachmentOperation(ajaxresponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                    maintAttachmentOperation.setElementid(elementid);
                    maintAttachmentOperation.setElementProperties(properties2);
                    maintAttachmentOperation.setPrimary(sdcid, keyid1, keyid2, keyid3);
                    if (capturedata != null) {
                        if (move) {
                            capturedata.sort("usersequence");
                        }
                        maintAttachmentOperation.setOperationData(capturedata);
                    }
                    if (viewonly) {
                        maintAttachmentOperation.setViewOnly(viewonly);
                    }
                    ajaxresponse.addCallbackArgument("elementid", elementid);
                    ajaxresponse.addCallbackArgument("html", maintAttachmentOperation.getHtml());
                    ajaxresponse.addCallbackArgument("script", maintAttachmentOperation.getScript());
                    if (ajaxresponse.getRequestParameter("full").equalsIgnoreCase("Y") && (capturedata = maintAttachmentOperation.getOperationData()) != null) {
                        ajaxresponse.addCallbackArgument("data", capturedata.toJSONString(true, true));
                    }
                    if (capturedata != null && move) {
                        ajaxresponse.addCallbackArgument("data", capturedata.toJSONString(true, true));
                    }
                    break block21;
                }
                if (mode != Mode.CREATE) break block21;
                String keyid1 = ajaxresponse.getRequestParameter("keyid1");
                String keyid2 = ajaxresponse.getRequestParameter("keyid2");
                String keyid3 = ajaxresponse.getRequestParameter("keyid3");
                String sdcid = ajaxresponse.getRequestParameter("sdcid");
                PropertyList properties = null;
                String props = ajaxresponse.getRequestParameter("properties");
                if (props.length() > 0) {
                    try {
                        properties = new PropertyList(new JSONObject(props));
                    }
                    catch (Exception properties2) {
                        // empty catch block
                    }
                }
                String elementid = ajaxresponse.getRequestParameter("elementid", "attachmentoperation");
                MaintAttachmentOperation attachmentOperation = new MaintAttachmentOperation(ajaxresponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                attachmentOperation.setAjaxCreate(true);
                attachmentOperation.setElementid(elementid);
                attachmentOperation.setElementProperties(properties);
                attachmentOperation.setPrimary(sdcid, keyid1, keyid2, keyid3);
                JSONObject captureElement = attachmentOperation.getElement();
                String script = attachmentOperation.getScript();
                ajaxresponse.addCallbackArgument("elementid", elementid);
                ajaxresponse.addCallbackArgument("html", attachmentOperation.getHtml());
                ajaxresponse.addCallbackArgument("script", script);
                ajaxresponse.addCallbackArgument("captureElement", captureElement.toString());
                ajaxresponse.addCallbackArgument("properties", attachmentOperation.getElementProperties().toJSONString());
                DataSet captureData = attachmentOperation.getOperationData();
                if (captureData != null) {
                    ajaxresponse.addCallbackArgument("data", captureData.toJSONString(true, true));
                }
            }
            finally {
                ajaxresponse.print();
            }
        }
    }

    private static enum Mode {
        REFRESH,
        CREATE;

    }
}

