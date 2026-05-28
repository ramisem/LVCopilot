/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.sdms.handlers.helpers;

import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler;
import com.labvantage.sapphire.util.LabVantageClassLoader;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.attachmenthandler.HandlerType;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;

public class AttachmentHandlerHelperAjax
extends BaseAjaxRequest
implements SDMSConstants {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block28: {
            AjaxResponse ajaxResponse = new AjaxResponse(request, response, "getActionPropertiesHandler");
            try {
                String attachmenthandler = ajaxResponse.getRequestParameter("attachmenthandlerid");
                String datacaptureid = ajaxResponse.getRequestParameter("datacaptureid");
                String out = "N";
                String url = "";
                if (attachmenthandler.length() > 0) {
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid("LV_AttachmentHandler");
                    sdiRequest.setRequestItem("primary");
                    sdiRequest.setRequestItem("attachment");
                    sdiRequest.setExtendedDataTypes(true);
                    sdiRequest.setKeyid1List(attachmenthandler);
                    SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                    if (sdiData.getDataset("primary") != null && sdiData.getDataset("primary").size() == 1) {
                        DataSet ds = sdiData.getDataset("primary");
                        HandlerType handlerType = HandlerType.getHandlerType(ds.getValue(0, "typeflag", ""));
                        if (handlerType == HandlerType.HANDLERCLASS) {
                            String hc = ds.getValue(0, "handlerclass", "");
                            if (hc.length() > 0) {
                                String[] excludedJars = new String[]{"sapphire"};
                                LabVantageClassLoader labVantageClassLoader = null;
                                if (SecurityPolicyUtil.isJavaAttachmentsPermitted(this.getConnectionId(), LabVantageClassLoader.ClassLoaderType.ATTACHMENTHANDLER.getArea())) {
                                    labVantageClassLoader = LabVantageClassLoader.getClassLoader(LabVantageClassLoader.ClassLoaderType.ATTACHMENTHANDLER, attachmenthandler, sdiData.getDataset("primary").getValue(0, "appresourceid", ""), sdiData.getDataset("attachment"), "HandlerLibrary", excludedJars, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                                    this.logger.debug("AttachmentHandler loaded.");
                                } else {
                                    this.logger.debug("Class loaders disabled in security policy.");
                                }
                                BaseAttachmentHandler bdh = null;
                                try {
                                    Class<?> c = labVantageClassLoader != null ? labVantageClassLoader.loadClass(hc) : Class.forName(hc);
                                    bdh = (BaseAttachmentHandler)c.newInstance();
                                }
                                catch (Throwable e) {
                                    this.logger.debug("Unable to load handler class");
                                    this.logger.debug(e.getMessage());
                                }
                                if (bdh != null && (url = bdh.getHelperURL()).length() > 0) {
                                    out = "Y";
                                }
                            } else {
                                this.logger.debug("No class value found");
                            }
                        } else {
                            this.logger.debug("Not a handler class");
                        }
                    } else {
                        this.logger.warn("Could not obtain handler data.");
                    }
                    ajaxResponse.addCallbackArgument("show", out);
                    ajaxResponse.addCallbackArgument("url", url);
                    break block28;
                }
                if (datacaptureid.length() > 0) {
                    boolean processing = true;
                    String results = "";
                    String status = "";
                    DataSet execution = this.getQueryProcessor().getPreparedSqlDataSet("SELECT executionstatus, handlerlog FROM sdiattachmentoperationexec WHERE sdcid=? AND keyid1=?", new Object[]{"LV_DataCapture", datacaptureid}, true);
                    if (execution != null) {
                        if (execution.getRowCount() > 0 && ((status = execution.getValue(0, "executionstatus", "")).equalsIgnoreCase("fail") || status.equalsIgnoreCase("success"))) {
                            String[] logLines;
                            processing = false;
                            status = status.equalsIgnoreCase("fail") ? "<span style=\"color:red;\">" + this.getTranslationProcessor().translate("Fail") + "</span>" : "<span style=\"color:green;\">" + this.getTranslationProcessor().translate("Successful") + "</span>";
                            String log = execution.getClob(0, "handlerlog", "");
                            StringBuilder logout = new StringBuilder();
                            for (String logLine : logLines = StringUtil.split(log, "\n")) {
                                if (logout.length() > 0) {
                                    logout.append("<br>");
                                }
                                logout.append("<span");
                                if (logLine.indexOf("DEBUG:") == 0) {
                                    logout.append(" style=\"color:grey;\"");
                                } else if (logLine.indexOf("ERROR:") == 0 || logLine.indexOf("EXCEPTION:") == 0) {
                                    logout.append(" style=\"color:red;\"");
                                }
                                if (logLine.indexOf("WARN:") == 0) {
                                    logout.append(" style=\"color:orange;\"");
                                }
                                logout.append(">");
                                logout.append(logLine);
                                logout.append("</span>");
                            }
                            results = logout.toString();
                        }
                    } else {
                        processing = false;
                        results = "Failed to test handler";
                    }
                    ajaxResponse.addCallbackArgument("processing", processing ? "Y" : "N");
                    ajaxResponse.addCallbackArgument("datacaptureid", datacaptureid);
                    ajaxResponse.addCallbackArgument("dialognum", ajaxResponse.getRequestParameter("dialognum"));
                    ajaxResponse.addCallbackArgument("status", status);
                    ajaxResponse.addCallbackArgument("results", results);
                } else {
                    this.logger.warn("No attachment handler provided");
                    ajaxResponse.addCallbackArgument("show", out);
                    ajaxResponse.addCallbackArgument("url", url);
                }
            }
            catch (Exception e) {
                ajaxResponse.setError(e.getMessage());
            }
            finally {
                ajaxResponse.print();
            }
        }
    }
}

