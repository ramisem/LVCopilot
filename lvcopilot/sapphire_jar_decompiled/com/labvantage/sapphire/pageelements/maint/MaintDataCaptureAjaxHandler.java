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

import com.labvantage.sapphire.pageelements.maint.MaintDataCapture;
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

public class MaintDataCaptureAjaxHandler
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block19: {
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
                    boolean viewonly = ajaxresponse.getRequestParameter("viewonly", "N").equalsIgnoreCase("Y");
                    PropertyList properties = null;
                    String props2 = ajaxresponse.getRequestParameter("properties");
                    if (props2.length() > 0) {
                        try {
                            properties = new PropertyList(new JSONObject(props2));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    DataSet datacapture = null;
                    if (!ajaxresponse.getRequestParameter("full").equalsIgnoreCase("Y") && (data = ajaxresponse.getRequestParameter("data")).length() > 0) {
                        try {
                            datacapture = new DataSet(new JSONObject(data));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    String elementid = ajaxresponse.getRequestParameter("elementid", "datacapture");
                    MaintDataCapture maintDataCapture = new MaintDataCapture(ajaxresponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                    maintDataCapture.setElementid(elementid);
                    maintDataCapture.setElementProperties(properties);
                    maintDataCapture.setPrimary(sdcid, keyid1, keyid2, keyid3);
                    if (datacapture != null) {
                        maintDataCapture.setDataCaptureData(datacapture);
                    }
                    if (viewonly) {
                        maintDataCapture.setViewOnly(viewonly);
                    }
                    ajaxresponse.addCallbackArgument("elementid", elementid);
                    ajaxresponse.addCallbackArgument("html", maintDataCapture.getHtml());
                    ajaxresponse.addCallbackArgument("script", maintDataCapture.getScript());
                    if (ajaxresponse.getRequestParameter("full").equalsIgnoreCase("Y") && (datacapture = maintDataCapture.getDataCaptureData()) != null) {
                        ajaxresponse.addCallbackArgument("data", datacapture.toJSONString(true, true));
                    }
                    break block19;
                }
                if (mode != Mode.CREATE) break block19;
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
                    catch (Exception props2) {
                        // empty catch block
                    }
                }
                String elementid = ajaxresponse.getRequestParameter("elementid", "datacapture");
                MaintDataCapture maintDataCapture = new MaintDataCapture(ajaxresponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                maintDataCapture.setAjaxCreate(true);
                maintDataCapture.setElementid(elementid);
                maintDataCapture.setElementProperties(properties);
                maintDataCapture.setPrimary(sdcid, keyid1, keyid2, keyid3);
                JSONObject captureElement = maintDataCapture.getElement();
                String script = maintDataCapture.getScript();
                ajaxresponse.addCallbackArgument("elementid", elementid);
                ajaxresponse.addCallbackArgument("html", maintDataCapture.getHtml());
                ajaxresponse.addCallbackArgument("script", script);
                ajaxresponse.addCallbackArgument("captureElement", captureElement.toString());
                ajaxresponse.addCallbackArgument("properties", maintDataCapture.getElementProperties().toJSONString());
                DataSet datacapture = maintDataCapture.getDataCaptureData();
                if (datacapture != null) {
                    ajaxresponse.addCallbackArgument("data", datacapture.toJSONString(true, true));
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

