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

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.services.SapphireConnection;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SavePageAsStartup
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 65482 $";

    private void appendMessage(StringBuffer inmessage, String message) {
        TranslationProcessor tp = this.getTranslationProcessor();
        if (message != null && message.length() > 0) {
            message = tp.translate(message);
        }
        if (inmessage.length() > 0) {
            inmessage = new StringBuffer(tp.translate(inmessage.toString()));
            inmessage.append("\n").append(message);
        } else {
            inmessage.append(message);
        }
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String group;
        String menu;
        StringBuffer message;
        String url;
        AjaxResponse ajaxResponse;
        block33: {
            ajaxResponse = new AjaxResponse(request, response);
            SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
            String userid = sapphireConnection.getSysuserId();
            ConfigurationProcessor cf = new ConfigurationProcessor(this.getConnectionId());
            url = "";
            message = new StringBuffer();
            String startupType = "User";
            try {
                startupType = cf.getProfileProperty(userid, "startuptype");
            }
            catch (Exception exception) {
                // empty catch block
            }
            menu = "";
            group = "";
            if (!startupType.equalsIgnoreCase("fixed")) {
                Object o;
                if (ajaxResponse.getRequestParameter("startuptype", "").length() > 0) {
                    try {
                        if (ajaxResponse.getRequestParameter("startuptype", "").equalsIgnoreCase("Last")) {
                            cf.setProfileProperty(userid, "startuptype", "Last");
                        } else {
                            cf.setProfileProperty(userid, "startuptype", "User");
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                Browser browser = new Browser(request);
                if (ajaxResponse.getRequestParameter("page", "Y").equalsIgnoreCase("Y")) {
                    String sql = "";
                    String currentCommand = ajaxResponse.getRequestParameter("currentcommand");
                    String currentPage = ajaxResponse.getRequestParameter("currentpage");
                    String validate = ajaxResponse.getRequestParameter("validate");
                    String stateid = ajaxResponse.getRequestParameter("stateid", "");
                    sql = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDbms().equals("MSS") ? (stateid.length() > 0 ? "SELECT TOP 1 webpagelog.webpagelogid, title, propertyclob FROM webpagelog, webpagelogtitle WHERE webpagelog.webpagelogid = webpagelogtitle.webpagelogid and webpagelog.sysuserid = ? and webpagelog.webpagelogid = ? order by requestdt desc" : "SELECT TOP 1 webpagelog.webpagelogid, title, propertyclob FROM webpagelog, webpagelogtitle WHERE webpagelog.webpagelogid = webpagelogtitle.webpagelogid and webpagelog.sysuserid = ? order by requestdt desc") : (stateid.length() > 0 ? "SELECT *  FROM ( SELECT webpagelog.webpagelogid, title, propertyclob FROM webpagelog, webpagelogtitle WHERE webpagelog.webpagelogid = webpagelogtitle.webpagelogid and webpagelog.sysuserid = ? and webpagelog.webpagelogid = ? order by requestdt desc ) WHERE rownum = 1" : "SELECT *  FROM ( SELECT webpagelog.webpagelogid, title, propertyclob FROM webpagelog, webpagelogtitle WHERE webpagelog.webpagelogid = webpagelogtitle.webpagelogid and webpagelog.sysuserid = ? order by requestdt desc ) WHERE rownum = 1");
                    Object[] params = stateid.length() > 0 ? new Object[]{userid, stateid} : new Object[]{userid};
                    try {
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params, true);
                        if (ds != null && ds.getRowCount() > 0) {
                            String historyPropsXml = ds.getClob(0, "propertyclob");
                            if (historyPropsXml != null && historyPropsXml.length() > 0) {
                                PropertyList historyProps = new PropertyList();
                                historyProps.setPropertyList(historyPropsXml);
                                String historyCommand = historyProps.getProperty("command");
                                String historyCommandValue = historyProps.getProperty(historyCommand);
                                if (!(!"true".equals(validate) || historyCommand.equals(currentCommand) && historyCommandValue.equals(currentPage))) {
                                    this.appendMessage(message, "The current page cannot be set as logon page.");
                                } else {
                                    String old;
                                    url = "rc?command=state&state=" + ds.getString(0, "webpagelogid");
                                    if (historyCommand.equalsIgnoreCase("page") && historyCommandValue.length() > 0) {
                                        url = url + "&page=" + historyCommandValue;
                                    }
                                    if ((old = cf.getProfileProperty(userid, browser.getGUIMode().getId() + "logonpageurl", "")).length() > 0 && (old.contains("&state=") || old.contains("&history="))) {
                                        String webpagelog = "";
                                        int i = old.indexOf("&state=");
                                        webpagelog = i > -1 ? old.substring(i + 7) : old.substring(old.indexOf("&history=") + 9);
                                        int o2 = webpagelog.indexOf("&");
                                        if (o2 > -1) {
                                            webpagelog.substring(0, webpagelog.indexOf("&"));
                                        }
                                        if (!webpagelog.equals(ds.getString(0, "webpagelogid"))) {
                                            String updateSql1 = "UPDATE webpagelog SET logtypeflag='N' WHERE sysuserid = ? and logtypeflag='S' and webpagelogid=?";
                                            this.logInfo("Executing: " + updateSql1);
                                            this.getQueryProcessor().execPreparedUpdate(updateSql1, new Object[]{userid, webpagelog});
                                            cf.setProfileProperty(userid, browser.getGUIMode().getId() + "logonpageurl", url);
                                            String updateSql2 = "UPDATE webpagelog SET logtypeflag='S' WHERE webpagelogid=?";
                                            this.logInfo("Executing: " + updateSql2);
                                            this.getQueryProcessor().execPreparedUpdate(updateSql2, new Object[]{ds.getString(0, "webpagelogid")});
                                        }
                                    } else {
                                        cf.setProfileProperty(userid, browser.getGUIMode().getId() + "logonpageurl", url);
                                        String updateSql2 = "UPDATE webpagelog SET logtypeflag='S' WHERE webpagelogid=?";
                                        this.logInfo("Executing: " + updateSql2);
                                        this.getQueryProcessor().execPreparedUpdate(updateSql2, new Object[]{ds.getString(0, "webpagelogid")});
                                    }
                                }
                            }
                        } else {
                            this.appendMessage(message, "The current page cannot be set as logon page.");
                        }
                    }
                    catch (SapphireException e) {
                        this.appendMessage(message, "Failed to update the logon page");
                    }
                }
                if (ajaxResponse.getRequestParameter("menu", "N").equalsIgnoreCase("Y")) {
                    try {
                        o = request.getSession().getAttribute("_menucache_last");
                        if (o != null && o.toString().length() > 0) {
                            menu = o.toString();
                            cf.setProfileProperty(userid, browser.getGUIMode().getId() + "logonmenu", menu);
                        } else {
                            this.logger.warn("No menu defined to update.");
                        }
                    }
                    catch (Exception e) {
                        this.appendMessage(message, "Could not update startup menu.");
                    }
                }
                if (ajaxResponse.getRequestParameter("group", "N").equalsIgnoreCase("Y")) {
                    try {
                        o = request.getSession().getAttribute("_sidebarcache_last");
                        if (o != null && o.toString().length() > 0) {
                            group = o.toString();
                            cf.setProfileProperty(userid, browser.getGUIMode().getId() + "logongroup", group);
                            break block33;
                        }
                        this.logger.warn("No group defined to update.");
                    }
                    catch (Exception e) {
                        this.appendMessage(message, "Could not update startup group.");
                    }
                }
            } else {
                this.appendMessage(message, "Cannot change fixed startup.");
            }
        }
        if (message.length() > 0) {
            this.logger.warn("MESSAGE: " + message);
        } else {
            this.appendMessage(message, "Your new startup settings have been set.");
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("startupurl", url);
        ajaxResponse.addCallbackArgument("menu", menu);
        ajaxResponse.addCallbackArgument("group", group);
        ajaxResponse.print();
    }
}

