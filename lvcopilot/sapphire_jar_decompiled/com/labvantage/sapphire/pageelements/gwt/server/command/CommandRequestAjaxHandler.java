/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.gwt.server.command;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.shared.CommandConstants;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CommandRequestAjaxHandler
extends BaseAjaxRequest
implements CommandConstants {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        CommandResponse commandResponse;
        block16: {
            commandResponse = new CommandResponse();
            CommandRequest commandRequest = new CommandRequest(request);
            String commandHandler = commandRequest.getCommandHandler();
            RequestProcessor requestProcessor = new RequestProcessor(this.getRequestContext().getConnectionid());
            HashMap<String, CommandConstants> requestMap = new HashMap<String, CommandConstants>();
            requestMap.put("commandrequest", commandRequest);
            requestMap.put("commandresponse", commandResponse);
            try {
                if (commandHandler != null && commandHandler.length() > 0) {
                    long start = System.currentTimeMillis();
                    if (!commandRequest.getBoolean("nolog")) {
                        this.logInfo("++++++ Start GWT CommandRequest: " + commandRequest.getCommand() + " ++++++");
                        Trace.logInfo("GWT CommandRequest:", "[#" + Thread.currentThread().getId() + "] = " + Thread.currentThread().getName());
                    }
                    if (this.processCommand(commandRequest.getCommand(), commandRequest, commandResponse)) {
                        if (!commandRequest.getCommand().equals("pingrset")) {
                            HashMap returnMap = requestProcessor.processRequest(commandHandler, requestMap);
                            commandResponse = (CommandResponse)returnMap.get("commandresponse");
                            if (commandResponse.containsKey("setuserconfig")) {
                                ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getRequestContext().getConnectionid());
                                String hostwebpageid = commandResponse.getString("__hostwebpageid");
                                String hostsysuserid = commandResponse.getString("__hostsysuserid");
                                PropertyList userConfig = commandRequest.getPropertyList("userconfig");
                                for (String propertyid : userConfig.keySet()) {
                                    this.setUserConfigProperty(configProcessor, request, hostsysuserid, hostwebpageid + "_" + propertyid, userConfig.getProperty(propertyid));
                                }
                            }
                            if (commandRequest.getBoolean("getuserconfig")) {
                                commandResponse.set("userconfig", (PropertyList)request.getSession().getAttribute("userconfig"));
                            }
                        } else if (commandRequest.getCommand().equals("pingrset")) {
                            DAMProcessor dam = new DAMProcessor(this.getConnectionId());
                            String[] rsets = StringUtil.split(commandRequest.getString("rsetid"), ";");
                            for (int i = 0; i < rsets.length; ++i) {
                                if (rsets[i].length() <= 0) continue;
                                dam.touchRSet(rsets[i]);
                            }
                        }
                    }
                    if (commandResponse.getStatus().equals("fail")) {
                        if (commandResponse.hasException()) {
                            this.logError("CommandResponse FAIL: " + commandResponse.getStatusMessage(), commandResponse.getException());
                        } else {
                            this.logDebug("CommandResponse FAIL: " + commandResponse.getStatusMessage());
                        }
                    }
                    if (!commandRequest.getBoolean("nolog")) {
                        Trace.logInfo("GWT CommandRequest:", "[#" + Thread.currentThread().getId() + "] = " + Thread.currentThread().getName());
                        this.logInfo("++++++ Complete GWT CommandRequest: " + commandRequest.getCommand() + " - took: " + (System.currentTimeMillis() - start) + "ms ++++++");
                    }
                    break block16;
                }
                throw new SapphireException("No command handler specified for command request.");
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to process command '" + (commandRequest != null ? commandRequest.getCommand() : "(unknown)") + "'. Reason: " + e.getMessage());
                this.logError(commandResponse.getStatusMessage(), e);
            }
        }
        this.write(commandResponse.toJSONString());
    }

    protected boolean processCommand(String command, CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        return true;
    }

    private void setUserConfigProperty(ConfigurationProcessor configProcessor, HttpServletRequest request, String sysuserid, String propertyid, String propertyvalue) {
        try {
            PropertyList userConfig = (PropertyList)request.getSession().getAttribute("userconfig");
            userConfig.setProperty(propertyid, propertyvalue);
            configProcessor.setProfileProperty(sysuserid, "userconfig_" + propertyid, propertyvalue);
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
    }
}

