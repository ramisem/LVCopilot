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
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.services.SapphireConnection;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class PingConnection
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block27: {
            AjaxResponse ajaxResponse = new AjaxResponse(request, response, "sapphire.connection.pingOrCheckConnectionHandler");
            try {
                try {
                    String connectionid = ajaxResponse.getRequestParameter("connectionId");
                    boolean keepalive = Boolean.parseBoolean(ajaxResponse.getRequestParameter("keepalive"));
                    boolean forcetimeout = Boolean.parseBoolean(ajaxResponse.getRequestParameter("forceTimeout"));
                    int connectionPoll = Integer.parseInt(ajaxResponse.getRequestParameter("connectionTimeout", "60"));
                    boolean alreadyloggedoff = false;
                    if (connectionid != null && connectionid.length() > 0) {
                        int interval;
                        int timeToTimeout;
                        if (keepalive) {
                            new QueryProcessor(connectionid).execPreparedUpdate("UPDATE connection SET lastaccesseddt= ? where connectionid= ? ", new Object[]{DateTimeUtil.getNowTimestamp(), connectionid});
                            ConfigurationProcessor cp = new ConfigurationProcessor(connectionid);
                            try {
                                timeToTimeout = Integer.parseInt(cp.getSysConfigProperty("connectiontimeout"));
                            }
                            catch (Exception e) {
                                timeToTimeout = 3600;
                            }
                            interval = 60;
                            ajaxResponse.addCallbackArgument("keepalive", Boolean.TRUE);
                        } else {
                            try {
                                Object timedoutobj = servletContext.getAttribute("tof_" + connectionid);
                                if (timedoutobj == null || !timedoutobj.toString().equalsIgnoreCase("Y")) {
                                    if (!forcetimeout) {
                                        ConnectionProcessor cp = new ConnectionProcessor(connectionid);
                                        if (cp != null) {
                                            if (cp.checkConnection(connectionid)) {
                                                SapphireConnection sc = cp.getSapphireConnection();
                                                if (sc != null) {
                                                    Date now = Calendar.getInstance().getTime();
                                                    if (sc.getDeleteDt() == null) {
                                                        Date from = sc.getLastAccessedDt().getTime();
                                                        long diff = now.getTime() - from.getTime();
                                                        if (diff < 0L) {
                                                            timeToTimeout = connectionPoll;
                                                        } else {
                                                            BigDecimal bd = new BigDecimal("" + (double)diff / 1000.0);
                                                            bd = bd.setScale(0, 4);
                                                            timeToTimeout = connectionPoll - bd.intValue();
                                                        }
                                                    } else {
                                                        timeToTimeout = 0;
                                                        this.logDebug("Deleted date present - timeToTimeout = 0");
                                                    }
                                                } else {
                                                    timeToTimeout = 0;
                                                    this.logDebug("No sapphire connection object available timeToTimeout = 0");
                                                }
                                            } else {
                                                alreadyloggedoff = true;
                                                timeToTimeout = 0;
                                                this.logDebug("Connection already logged out timeToTimeout = 0");
                                            }
                                        } else {
                                            timeToTimeout = 0;
                                            this.logDebug("Could not create connection processor timeToTimeout = 0");
                                        }
                                    } else {
                                        timeToTimeout = 0;
                                        this.logDebug("Force timeout - timeToTimeout = 0");
                                    }
                                    if (timeToTimeout < 1) {
                                        servletContext.setAttribute("tof_" + connectionid, (Object)"Y");
                                    }
                                } else {
                                    timeToTimeout = 0;
                                    this.logDebug("Timedout session variable found - timeToTimeout = 0");
                                }
                            }
                            catch (Exception e) {
                                timeToTimeout = 300;
                                this.logDebug("Exception " + e.getMessage() + " - check connection again in 30 seconds.");
                            }
                            interval = timeToTimeout > 600 ? 120 : (timeToTimeout > 480 && timeToTimeout <= 600 ? 60 : (timeToTimeout > 300 && timeToTimeout <= 480 ? 30 : (timeToTimeout > 120 && timeToTimeout <= 300 ? 20 : (timeToTimeout > 60 && timeToTimeout <= 120 ? 10 : (timeToTimeout > 30 && timeToTimeout <= 60 ? 5 : (timeToTimeout > 15 && timeToTimeout <= 30 ? 1 : 1))))));
                            ajaxResponse.addCallbackArgument("keepalive", Boolean.FALSE);
                        }
                        this.logger.debug("Connection properties: interval = '" + interval + "', timeToTimeout = '" + timeToTimeout + "', loggedOff = '" + alreadyloggedoff + "'");
                        ajaxResponse.addCallbackArgument("interval", new Integer(interval));
                        ajaxResponse.addCallbackArgument("timeToTimeout", new Integer(timeToTimeout));
                        ajaxResponse.addCallbackArgument("loggedOff", alreadyloggedoff);
                        break block27;
                    }
                    this.logger.warn("No connectionid provided to PingConnection. Suppressing error.");
                }
                catch (Exception e) {
                    this.logger.error("Exception generated during pingConnection. Error suppressed");
                }
            }
            finally {
                ajaxResponse.print();
            }
        }
    }
}

