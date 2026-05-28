/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.servlet.ConsoleController;
import com.labvantage.sapphire.servlet.command.fileupload.FileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppConstants;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppException;
import com.labvantage.sapphire.servlet.externalapp.ExternalAuthenticationUtil;
import com.labvantage.sapphire.servlet.rest.BaseNameSpaceHandler;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.BaseExternalHandler;
import sapphire.servlet.BaseHttpServlet;
import sapphire.util.ForwardUtil;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;

public class SapphireController
extends BaseHttpServlet
implements ExternalAppConstants {
    private static final String SERVER_LOGON = "logon.jsp?serverlogon=true";

    public void init() throws ServletException {
        super.init();
        this.getServletContext().log("LabVantage".toUpperCase() + ": Servlet startup");
        try {
            ServiceLocator.clearInstance();
            Configuration.clearInstance();
            PropertyList startupProps = new PropertyList();
            startupProps.setProperty("LABVANTAGE_HOME", Configuration.getSapphireHome(this.getServletContext()));
            startupProps.setProperty("serverinfo", this.getServletContext().getServerInfo());
            startupProps.setProperty("hostname", InetAddress.getLocalHost().getHostName());
            startupProps.setProperty("applicationid", this.getServletContext().getInitParameter("applicationid"));
            startupProps.setProperty("httpurl", "http://[httphost]:[httpport]" + this.getServletContext().getContextPath());
            startupProps.setProperty("LABVANTAGE_SERVER", System.getProperty("LABVANTAGE_SERVER"));
            startupProps.setProperty("SERVERPROFILE", System.getProperty("SERVERPROFILE"));
            startupProps.setProperty("AUTOMATION_SERVER", System.getProperty("AUTOMATION_SERVER"));
            startupProps.setProperty("JUNIT_SERVER", System.getProperty("JUNIT_SERVER"));
            this.getServletContext().log("LabVantage".toUpperCase() + ": " + ConsoleController.PRODUCT_HOME + "=" + (startupProps.get("LABVANTAGE_HOME") != null ? startupProps.get("LABVANTAGE_HOME") : ""));
            this.getServletContext().log("LabVantage".toUpperCase() + ": serverinfo=" + (startupProps.get("serverinfo") != null ? startupProps.get("serverinfo") : ""));
            this.getServletContext().log("LabVantage".toUpperCase() + ": hostname=" + (startupProps.get("hostname") != null ? startupProps.get("hostname") : ""));
            this.getServletContext().log("LabVantage".toUpperCase() + ": serverid=" + (startupProps.get("LABVANTAGE_SERVER") != null ? startupProps.get("LABVANTAGE_SERVER") : ""));
            this.getServletContext().log("LabVantage".toUpperCase() + ": applicationid=" + (startupProps.get("applicationid") != null ? startupProps.get("applicationid") : ""));
            Configuration.createInstance(startupProps);
            ArrayList messages = ServiceLocator.getInstance().getSapphireManager().startup(new HashMap());
            for (int i = 0; i < messages.size(); ++i) {
                this.getServletContext().log("LabVantage".toUpperCase() + ": " + (String)messages.get(i));
            }
            this.getServletContext().log("LabVantage".toUpperCase() + ": serverhttpurl=" + Configuration.getInstance().getServerHttpURL());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        try {
            ServiceLocator.getInstance().getSapphireManager().shutdown();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doInit();
        Trace.startThreadMDCByDatabaseid("", "SC");
        String command = request.getParameter("command");
        if (command == null || command.length() == 0 || "file".equals(request.getHeader("LVCommandType"))) {
            if ("file".equals(request.getHeader("LVCommandType"))) {
                JSONObject jsonResponse = new JSONObject();
                try {
                    BaseExternalHandler commandHandler = BaseExternalHandler.getInstance(request, response);
                    if (commandHandler == null) {
                        throw new SapphireException("Failed to create a command handler");
                    }
                    JSONObject jsonRequest = new JSONObject();
                    FileUpload fu = new FileUpload();
                    long maxsize = FileUpload.getMaxUploadSize("", commandHandler.getConnectionId());
                    fu.setSizeMax(maxsize);
                    fu.setSizeThreshold(maxsize);
                    List fileItems = fu.parseRequest(request);
                    InputStream inputStream = null;
                    String fileName = "";
                    for (FileItem fi : fileItems) {
                        fileName = fi.getName();
                        if (fileName != null) {
                            int lastFileSepIndex = fileName.lastIndexOf(File.separator);
                            if (lastFileSepIndex >= 0) {
                                fileName = fileName.substring(lastFileSepIndex + 1);
                            }
                            inputStream = fi.getInputStream();
                            continue;
                        }
                        String fieldname = fi.getFieldName();
                        if (fieldname == null) continue;
                        if (fieldname.equals("command")) {
                            command = fi.getString();
                            continue;
                        }
                        try {
                            jsonRequest.put(fieldname, fi.getString());
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (command != null && command.length() > 0 && inputStream != null) {
                        Trace.startThreadMDCByDatabaseid(commandHandler.getDatabaseId(), "SC");
                        try {
                            jsonResponse = commandHandler.processFileCommand(command, fileName != null ? fileName : "", inputStream, jsonRequest);
                        }
                        catch (Exception e) {
                            jsonResponse.put("_exception", "Failed to process command " + command + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    response.getWriter().println(jsonResponse.toString());
                }
                catch (ExternalAppException e) {
                    this.sendErrorResponse(request, response, e.getHttpCode(), e);
                }
                catch (Exception e) {
                    this.sendErrorResponse(request, response, 500, e);
                }
            }
        } else {
            JSONObject jsonResponse = new JSONObject();
            try {
                block45: {
                    JSONObject jsonRequest = this.getJsonRequest(request);
                    if (command.equals("RequestToken")) {
                        String authorizationCode = jsonRequest.optString("authorizationcode");
                        String reason = jsonRequest.optString("reason");
                        String externalUserid = jsonRequest.optString("externaluserid");
                        String hostname = request.getRemoteHost();
                        String token = ExternalAuthenticationUtil.createTokenRequest(authorizationCode, reason, hostname, externalUserid);
                        jsonResponse.put("token", token);
                    } else if (command.equals("IsTokenActive")) {
                        String token = jsonRequest.optString("token");
                        String databaseid = SapphireService.getDatabaseForToken(token);
                        Trace.startThreadMDCByDatabaseid(databaseid, "SC");
                        boolean isTokenActive = ExternalAuthenticationUtil.isTokenActive(token);
                        jsonResponse.put("istokenactive", isTokenActive ? "Y" : "N");
                    } else if (command.equals("GetConnectionId")) {
                        String username = jsonRequest.optString("username");
                        String password = jsonRequest.optString("password");
                        String database = jsonRequest.optString("database");
                        Trace.startThreadMDCByDatabaseid(database, "SC");
                        String connectionid = ExternalAuthenticationUtil.getExternalConnectionId(username, password, database);
                        jsonResponse.put("connectionid", connectionid);
                    } else if (command.equals("ClearConnection")) {
                        String connectionid = jsonRequest.optString("connectionid");
                        Trace.startThreadMDCByConnectionid(connectionid, "SC");
                        ConnectionProcessor connectionProcessor = new ConnectionProcessor(connectionid);
                        connectionProcessor.clearConnection(connectionid);
                    } else if (command.equals("CheckConnection")) {
                        String connectionid = jsonRequest.optString("connectionid");
                        Trace.startThreadMDCByConnectionid(connectionid, "SC");
                        ConnectionProcessor connectionProcessor = new ConnectionProcessor(connectionid);
                        if (!connectionProcessor.checkConnection(connectionid)) throw new SapphireException(connectionid + " connection resource is not valid.");
                        jsonResponse.put("CheckConnection", "checked");
                    } else {
                        BaseExternalHandler commandHandler = BaseExternalHandler.getInstance(request, response);
                        Trace.startThreadMDCByDatabaseid(commandHandler.getDatabaseId(), "SC");
                        if (commandHandler == null) {
                            throw new SapphireException("Failed to create a command handler");
                        }
                        try {
                            if (jsonRequest.has("propertylistrequest")) {
                                PropertyList propertylistRequest = new PropertyList();
                                propertylistRequest.setJSONString(jsonRequest.optString("propertylistrequest"));
                                PropertyList propertylistResponse = null;
                                try {
                                    propertylistResponse = commandHandler.processCommand(command, propertylistRequest);
                                }
                                catch (SapphireException e) {
                                    throw new ServletException(e.getMessage(), (Throwable)e);
                                }
                                jsonResponse.put("propertylistresponse", propertylistResponse.toJSONString(true));
                                break block45;
                            }
                            if (jsonRequest.has("downloadrequest")) {
                                File downloadFile = null;
                                try {
                                    downloadFile = commandHandler.processFileDownloadCommand(command, jsonRequest);
                                }
                                catch (SapphireException e) {
                                    this.sendErrorResponse(request, response, 500, e);
                                }
                                if (downloadFile != null && downloadFile.exists()) {
                                    int DEFAULT_BUFFER_SIZE = 8192;
                                    BufferedInputStream input = null;
                                    FilterOutputStream output = null;
                                    try {
                                        int length;
                                        input = new BufferedInputStream(new FileInputStream(downloadFile), 8192);
                                        output = new BufferedOutputStream((OutputStream)response.getOutputStream(), 8192);
                                        response.setContentType(downloadFile.getName().endsWith("zip") ? "application/zip" : (downloadFile.getName().endsWith("tar.gz") ? "application/tar" : "text"));
                                        response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFile.getName() + "\"");
                                        byte[] buffer = new byte[8192];
                                        while ((length = input.read(buffer)) > -1) {
                                            ((BufferedOutputStream)output).write(buffer, 0, length);
                                        }
                                        ((BufferedOutputStream)output).flush();
                                        break block45;
                                    }
                                    finally {
                                        if (output != null) {
                                            output.close();
                                        }
                                        if (input != null) {
                                            input.close();
                                        }
                                    }
                                }
                                this.sendErrorResponse(request, response, 500, new SapphireException("Failed to process file downlaod command " + command));
                                Trace.logError("Failed to process command " + command);
                                break block45;
                            }
                            jsonResponse = commandHandler.processCommand(command, jsonRequest);
                        }
                        catch (Exception e) {
                            jsonResponse.put("_exception", "Failed to process command " + command + ": " + e.getMessage());
                            Trace.logError("Failed to process command " + command + ": " + e.getMessage());
                        }
                    }
                }
                response.getWriter().println(jsonResponse.toString());
            }
            catch (ExternalAppException e) {
                this.sendErrorResponse(request, response, e.getHttpCode(), e);
            }
            catch (Exception e) {
                this.sendErrorResponse(request, response, 500, e);
            }
        }
        Trace.clearThreadMDC();
    }

    public JSONObject getJsonRequest(HttpServletRequest request) throws Exception {
        String body;
        String contentType;
        JSONObject jsonRequest = null;
        String json = request.getParameter("json");
        String string = json = json == null ? request.getParameter("jsonrequest") : json;
        if (json != null && json.length() > 0) {
            try {
                jsonRequest = new JSONObject(json);
            }
            catch (Exception e) {
                throw new Exception("Failed to parse JSON request body");
            }
        }
        if (jsonRequest == null && (contentType = request.getContentType()) != null && (contentType.toLowerCase().contains("text/plain") || contentType.toLowerCase().contains("application/json")) && (body = this.getContentBody(request)).startsWith("{")) {
            try {
                jsonRequest = new JSONObject(body);
            }
            catch (Exception e) {
                throw new Exception("Failed to parse JSON request body");
            }
        }
        if (jsonRequest == null) {
            jsonRequest = new JSONObject();
            Enumeration params = request.getParameterNames();
            while (params.hasMoreElements()) {
                String key = (String)params.nextElement();
                jsonRequest.put(key, request.getParameter(key));
            }
        }
        return jsonRequest;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getContentBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        request.setCharacterEncoding("UTF-8");
        try (BufferedReader reader = request.getReader();){
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    public PropertyList getCommandRequest(HttpServletRequest request) throws SapphireException {
        PropertyList commandRequest = new PropertyList();
        try {
            if (request.getParameter("in") != null && request.getParameter("in").length() > 0) {
                commandRequest.setJSONString(request.getParameter("in"));
            }
        }
        catch (JSONException e) {
            throw new SapphireException("Unable to parse incoming in JSON", e);
        }
        return commandRequest;
    }

    private boolean restart(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            ServiceLocator.getInstance().getSapphireManager().restart();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return this.redirect(SERVER_LOGON, response, HttpUtil.getRequestMap((ServletRequest)request));
    }

    private boolean redirect(String nextpage, HttpServletResponse response, HashMap props) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter output = response.getWriter();
        output.println("Processed operation. Redirecting...");
        ForwardUtil forward = new ForwardUtil();
        forward.setProperty("dummy", "dummy");
        forward.setProperties(props);
        output.println(forward.getForm("", nextpage, "post", true));
        response.getWriter().close();
        return true;
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, int httpCode, Throwable e) throws IOException, ServletException {
        ConnectionProcessor cp;
        SapphireConnection sapphireConnection;
        HttpUtil httpUtil = new HttpUtil(request, response);
        String connectionid = httpUtil.getCookieValue("connectionid");
        if (connectionid != null && connectionid.length() > 0 && (sapphireConnection = (cp = new ConnectionProcessor(connectionid)).getSapphireConnection()) != null && sapphireConnection.getTool().equalsIgnoreCase("RequestController")) {
            throw new ServletException(e);
        }
        String errormsg = e.getMessage();
        Trace.logError("SapphireController", (Object)errormsg, e);
        response.setStatus(httpCode);
        if (!response.isCommitted()) {
            PrintWriter out = response.getWriter();
            out.print("{\n \"request\": \"" + request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : "") + "\", \n \"method\": \"" + request.getMethod() + "\", \n \"code\": " + httpCode + ", \n \"status\": \"" + BaseNameSpaceHandler.getStatusFromCode(httpCode) + "\", \n \"error\": \"" + (e instanceof ExternalAppException ? ((ExternalAppException)e).getError() : "Unexpected error") + "\", \n \"message\": \"" + e.getMessage() + "\"\n}");
            out.close();
        }
    }
}

