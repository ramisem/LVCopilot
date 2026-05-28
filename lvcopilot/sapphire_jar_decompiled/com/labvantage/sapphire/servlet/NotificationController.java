/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.eventmanager.AsyncNotify;
import com.labvantage.sapphire.modules.eventmanager.Notify;
import com.labvantage.sapphire.modules.eventmanager.NotifyManager;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.NotificationConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.BaseHttpServlet;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;

public class NotificationController
extends BaseHttpServlet
implements NotificationConstants {
    private static boolean debug = false;
    private static ServletContext servletContext;

    public static boolean isDebug() {
        return debug;
    }

    public void init() throws ServletException {
        super.init();
        servletContext = this.getServletContext();
        NotificationController.logInfo("init - Servlet startup");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        block54: {
            NotificationController.logDebug("In doGet");
            String command = request.getParameter("command");
            response.setContentType("application/json");
            if (command == null) {
                command = "";
            }
            PropertyList responseProps = new PropertyList();
            NotificationController.logDebug("doGet request command = " + command);
            boolean requestManager = false;
            try {
                PropertyList commandProps;
                try {
                    commandProps = new PropertyList(new JSONObject(request.getParameter(command)));
                }
                catch (Exception e) {
                    commandProps = new PropertyList();
                }
                requestManager = commandProps.getProperty("requestmanager").equals("Y");
                HttpUtil httpUtil = new HttpUtil(request, response);
                String connectionid = httpUtil.getCookieValue("connectionid");
                ConnectionProcessor cp = new ConnectionProcessor(connectionid);
                if (connectionid.length() > 0) {
                    Trace.startThreadMDCByConnectionid(connectionid, "Browser");
                    if (cp.checkConnection(connectionid)) {
                        SapphireConnection sapphireConnection = null;
                        try {
                            sapphireConnection = cp.getSapphireConnection();
                        }
                        catch (Exception e) {
                            this.logError("Failed to get Sapphire Connection from connection id.");
                        }
                        if (sapphireConnection != null && sapphireConnection.getSysuserId() != null && sapphireConnection.getSysuserId().length() > 0) {
                            String subscriberid = commandProps.getProperty("subscriberid", sapphireConnection.getSysuserId() + ";" + connectionid);
                            if (subscriberid.length() > 0) {
                                String databaseid = sapphireConnection.getDatabaseId();
                                String windowname = commandProps.getProperty("windowname");
                                NotificationController.logDebug("doGet - " + NotificationController.getCommandText(command) + " command from " + databaseid + "-" + subscriberid + (windowname.equals(subscriberid) ? "" : " (" + windowname + ")"));
                                try {
                                    if (command.equalsIgnoreCase("ping")) {
                                        NotificationController.logDebug("doGet - Network ping received.");
                                    } else if (command.equalsIgnoreCase("rn")) {
                                        NotifyManager.requestNotifications(databaseid, subscriberid, windowname, requestManager ? new AsyncNotify(connectionid, subscriberid, request.startAsync((ServletRequest)request, (ServletResponse)response)) : null, commandProps.getProperty("initialrequest", "N").equals("Y"));
                                    } else if (command.equalsIgnoreCase("reln")) {
                                        NotifyManager.requestElementNotifications(databaseid, subscriberid, commandProps.getProperty("elementtype"), commandProps.getProperty("elementid"));
                                    } else if (command.equalsIgnoreCase("lo")) {
                                        NotifyManager.notifyLogoff(sapphireConnection, true);
                                    } else if (command.equalsIgnoreCase("clo")) {
                                        NotifyManager.notifyCancelLogoff(sapphireConnection);
                                    } else if (command.equalsIgnoreCase("rcn")) {
                                        AsyncNotify asyncNotify = new AsyncNotify(connectionid, "__chat_" + subscriberid, request.startAsync((ServletRequest)request, (ServletResponse)response));
                                        NotifyManager.requestChatNotifications(databaseid, subscriberid, asyncNotify, commandProps.getProperty("initialrequest", "N").equals("Y"));
                                    } else if (command.equalsIgnoreCase("rcu")) {
                                        responseProps.setProperty("users", NotifyManager.requestChatUsers(connectionid).toJSONString());
                                    } else if (command.equalsIgnoreCase("ec")) {
                                        NotifyManager.endChat(databaseid, subscriberid);
                                    } else if (command.equalsIgnoreCase("cn")) {
                                        NotifyManager.clearNotifications(databaseid, subscriberid, commandProps.getProperty("notificationid"));
                                        responseProps.setProperty("elementid", commandProps.getProperty("elementid"));
                                        responseProps.setProperty("notificationid", commandProps.getProperty("notificationid"));
                                        responseProps.setProperty("callbackArgs", "elementid,notificationid");
                                    } else if (command.equalsIgnoreCase("sn")) {
                                        String[] users;
                                        ActionProcessor ap = new ActionProcessor(connectionid);
                                        QueryProcessor qp = new QueryProcessor(connectionid);
                                        for (String sysuserid : users = StringUtil.split(commandProps.getProperty("sysuserid"), ";")) {
                                            PropertyList sendProps;
                                            DataSet user = qp.getPreparedSqlDataSet("SELECT nameduserflag,externalappid FROM sysuser WHERE sysuserid=?", (Object[])new String[]{sysuserid});
                                            String usertype = user.getValue(0, "nameduserflag");
                                            if (usertype.equals("P") || usertype.equals("Q")) {
                                                sendProps = new PropertyList();
                                                sendProps.setProperty("description", commandProps.getProperty("message"));
                                                sendProps.setProperty("body", commandProps.getProperty("message"));
                                                sendProps.setProperty("user", sysuserid);
                                                try {
                                                    ap.processAction("SendBulletin", "1", sendProps);
                                                }
                                                catch (ActionException e) {
                                                    e.printStackTrace();
                                                }
                                                continue;
                                            }
                                            sendProps = new PropertyList();
                                            sendProps.setProperty("type", "T");
                                            sendProps.setProperty("message", DOMUtil.convertChars(commandProps.getProperty("message")));
                                            sendProps.setProperty("link", "javascript:sapphire.alert('" + DOMUtil.convertChars(StringUtil.replaceAll(commandProps.getProperty("message"), "'", "\\'")) + "')");
                                            sendProps.setProperty("subscriberid", sysuserid);
                                            try {
                                                ap.processActionClass(Notify.class.getName(), sendProps);
                                            }
                                            catch (ActionException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else if (command.equalsIgnoreCase("sc")) {
                                        NotifyManager.sendChat(connectionid, subscriberid, commandProps.getProperty("tousers"), commandProps.getProperty("message"));
                                        Calendar now = DateTimeUtil.getNowCalendar();
                                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                                        responseProps.setProperty("createdt", sdf.format(now.getTime()));
                                    } else if (!command.equalsIgnoreCase("ss")) {
                                        if (command.equalsIgnoreCase("debug")) {
                                            debug = true;
                                        } else if (command.equalsIgnoreCase("nodebug")) {
                                            debug = false;
                                        }
                                    }
                                    responseProps.setProperty("fromcommand", command);
                                    if (responseProps.getProperty("status").length() == 0) {
                                        responseProps.setProperty("status", "OK");
                                    }
                                    if (responseProps.getProperty("callback").length() == 0) {
                                        responseProps.setProperty("callback", request.getParameter("callback") != null && request.getParameter("callback").length() > 0 ? SafeHTML.encodeForJavaScript(request.getParameter("callback")) : "sapphire.notification.requestManagerCallback");
                                    }
                                    if (responseProps.getProperty("errorcallback").length() == 0) {
                                        responseProps.setProperty("errorcallback", request.getParameter("errorcallback") != null && request.getParameter("errorcallback").length() > 0 ? SafeHTML.encodeForJavaScript(request.getParameter("errorcallback")) : "sapphire.notification.requestError");
                                    }
                                    break block54;
                                }
                                catch (Exception e3) {
                                    this.logError("Failed to process command: " + e3.getMessage(), e3);
                                    responseProps.setProperty("status", "FAIL");
                                    responseProps.setProperty("statusmessage", "Failed to process command!");
                                }
                                break block54;
                            }
                            NotificationController.logInfo("init - No subscriber ID provided");
                            break block54;
                        }
                        this.logError("Could not create sapphire connection in ping");
                        responseProps.setProperty("status", "FAIL");
                        responseProps.setProperty("statusmessage", "Connection Expired");
                        break block54;
                    }
                    this.logError("Check connection failed");
                    responseProps.setProperty("status", "FAIL");
                    responseProps.setProperty("statusmessage", "Connection Expired");
                    break block54;
                }
                this.logError("Invalid connection id in Ping");
                responseProps.setProperty("status", "FAIL");
                responseProps.setProperty("statusmessage", "Connection Expired");
            }
            catch (Exception e) {
                this.logError("Error found in ping:" + e.getMessage());
                responseProps.setProperty("status", "FAIL");
                responseProps.setProperty("statusmessage", "Exception thrown in ping");
            }
            finally {
                if (!requestManager || !request.isAsyncStarted()) {
                    if (request.getParameter("callback") != null && request.getParameter("callback").length() > 0) {
                        PropertyList parent = new PropertyList();
                        parent.put("callback", request.getParameter("callback"));
                        if (request.getParameter("errorcallback") != null && request.getParameter("errorcallback").length() > 0) {
                            parent.put("errorcallback", request.getParameter("errorcallback"));
                        }
                        parent.put("response", responseProps);
                        parent.put("callbackObj", "response");
                        responseProps = parent;
                    }
                    this.respond(response, responseProps);
                }
                Trace.clearThreadMDC();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void respond(HttpServletResponse response, PropertyList responseProps) throws IOException {
        block7: {
            NotificationController.logDebug("In respond...");
            try {
                if (response.isCommitted()) break block7;
                try (PrintWriter out = response.getWriter();){
                    NotificationController.logDebug("respond - Writing JSON.");
                    out.print(responseProps.toJSONObject(false));
                    NotificationController.logDebug("respond: respond - Finished Writing JSON.");
                }
            }
            catch (IOException e1) {
                this.logError("Failed to respond with message " + e1.getMessage());
                throw e1;
            }
        }
        NotificationController.logDebug("respond -...response sent.");
    }

    private static String getCommandText(String command) {
        return command.equalsIgnoreCase("rn") ? "RequestNotifications" : (command.equalsIgnoreCase("lo") ? "Logoff" : (command.equalsIgnoreCase("clo") ? "CancelLogoff" : (command.equalsIgnoreCase("rcn") ? "RequestChatNotifications" : (command.equalsIgnoreCase("rcu") ? "RequestChatUsers" : (command.equalsIgnoreCase("ec") ? "EndChat" : (command.equalsIgnoreCase("cn") ? "ClearNotifications" : (command.equalsIgnoreCase("sn") ? "SendNotification" : (command.equalsIgnoreCase("sc") ? "SendChat" : (command.equalsIgnoreCase("ss") ? "Subscribe" : (command.equalsIgnoreCase("debug") ? "Debug" : (command.equalsIgnoreCase("nodebug") ? "NoDebug" : (command.equalsIgnoreCase("reln") ? "RequestElementNotifications" : "Unknown"))))))))))));
    }

    public static void logInfo(String message) {
        servletContext.log("INFO: NOTIFYCONTROLLER: " + message);
    }

    public static void logDebug(String message) {
        if (NotificationController.isDebug()) {
            servletContext.log("DEBUG: NOTIFYCONTROLLER: " + message);
        }
    }

    @Override
    public void logError(String message) {
        super.logError("NOTIFYCONTROLLER: " + message);
    }
}

