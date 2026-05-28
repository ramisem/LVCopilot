/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.admin.security;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.ext.BaseWebMFAHandler;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class MFAUtil {
    public static BaseWebMFAHandler getWebMFAHandler(String username, String database, String connectionid, HttpServletRequest request, HttpServletResponse response) throws ServletException {
        BaseWebMFAHandler baseWebMFAHandler;
        block19: {
            baseWebMFAHandler = null;
            PropertyList authenticationPL = null;
            com.labvantage.sapphire.admin.system.ConfigurationProcessor configurationProcessor = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(connectionid);
            boolean prompt2ndFA = true;
            String mfaprovider = "";
            String secretkey = "";
            String baseWebMFAHandlerClass = "";
            try {
                String sysmfaprovider = configurationProcessor.getProfileProperty("(system)", "mfaprovider", "");
                DataSet ds = new QueryProcessor(connectionid).getPreparedSqlDataSet("SELECT sysuser.mfaprovider, sysuser.mfasecretkey, propertytree.objectname, (SELECT objectname FROM propertytree WHERE propertytreeid=? ) sysdefaultobjectname FROM sysuser JOIN connection ON sysuser.sysuserid=connection.sysuserid LEFT JOIN propertytree ON sysuser.mfaprovider=propertytree.propertytreeid WHERE connectionid=?", new Object[]{sysmfaprovider, connectionid});
                secretkey = ds.getValue(0, "mfasecretkey");
                mfaprovider = ds.getValue(0, "mfaprovider");
                if (mfaprovider.length() == 0) {
                    mfaprovider = sysmfaprovider;
                }
                if ((baseWebMFAHandlerClass = ds.getValue(0, "objectname")).length() == 0) {
                    baseWebMFAHandlerClass = ds.getValue(0, "sysdefaultobjectname");
                }
                if ("Disable".equals(mfaprovider) || baseWebMFAHandlerClass.length() == 0) {
                    Trace.log("MFA provider not enabled");
                    prompt2ndFA = false;
                } else {
                    ConfigurationProcessor configProcessor = new ConfigurationProcessor(connectionid);
                    authenticationPL = configProcessor.getPolicy(mfaprovider, "Sapphire Custom");
                    authenticationPL.setDatabaseid(database);
                    if (authenticationPL != null && "Y".equals(authenticationPL.getProperty("enable", "Y"))) {
                        String rememberdevicedays = authenticationPL.getProperty("rememberdevicedays");
                        if (rememberdevicedays.length() > 0 && !"0".equals(rememberdevicedays)) {
                            String rememberdevice = new HttpUtil(request, response).getCookieValue("rememberdevice");
                            if (rememberdevice.length() > 0) {
                                String[] usernameTimes = StringUtil.split(rememberdevice = EncryptDecrypt.decrypt(rememberdevice, database), "|%|");
                                if (usernameTimes.length == 2 && usernameTimes[0].equals(username)) {
                                    try {
                                        String timestamp = usernameTimes[1];
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        sdf.parse(timestamp);
                                        int days = Integer.parseInt(rememberdevicedays);
                                        Calendar cal = sdf.getCalendar();
                                        cal.add(5, days);
                                        if (Calendar.getInstance().before(cal)) {
                                            Trace.log("Remembered device until " + sdf.format(cal.getTime()));
                                            String value = username + "|%|" + new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                                            value = EncryptDecrypt.encrypt(value, database);
                                            new HttpUtil(request, response).setCookieValue("rememberdevice", value, true, true);
                                            prompt2ndFA = false;
                                        }
                                    }
                                    catch (Exception e) {
                                        Trace.logWarn(e.getMessage());
                                    }
                                }
                            } else {
                                Trace.log("Not remembered device. Prompt for 2FA.");
                            }
                        }
                        if (baseWebMFAHandlerClass.indexOf("LVDefault2FAHandler") > 0) {
                            request.getSession().setAttribute("allowReset2FA", (Object)"Y");
                        }
                    } else {
                        prompt2ndFA = false;
                    }
                }
                if (!prompt2ndFA) break block19;
                try {
                    baseWebMFAHandler = (BaseWebMFAHandler)Class.forName(baseWebMFAHandlerClass).newInstance();
                    baseWebMFAHandler.init(authenticationPL, username, database, connectionid, secretkey);
                    request.getSession().setAttribute("baseWebMFAHandler", (Object)baseWebMFAHandler);
                }
                catch (Exception cnf) {
                    throw new ServletException((Throwable)cnf);
                }
            }
            catch (SapphireException se) {
                throw new ServletException((Throwable)se);
            }
        }
        return baseWebMFAHandler;
    }

    public static BaseWebMFAHandler getWebMFAHandler(String username, String database, String connectionid, HttpSession httpSession) throws ServletException {
        BaseWebMFAHandler baseWebMFAHandler;
        block19: {
            baseWebMFAHandler = null;
            PropertyList authenticationPL = null;
            com.labvantage.sapphire.admin.system.ConfigurationProcessor configurationProcessor = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(connectionid);
            boolean prompt2ndFA = true;
            String mfaprovider = "";
            String secretkey = "";
            String baseWebMFAHandlerClass = "";
            try {
                QueryProcessor qp = new QueryProcessor(connectionid);
                DataSet userds = qp.getPreparedSqlDataSet("SELECT sysuser.nameduserflag FROM sysuser WHERE sysuserid=?  or logonname=? ", new Object[]{username, username});
                String sysmfaprovider = "";
                sysmfaprovider = userds.getRowCount() > 0 && (userds.getValue(0, "nameduserflag", "").equalsIgnoreCase("P") || userds.getValue(0, "nameduserflag", "").equalsIgnoreCase("Q")) ? configurationProcessor.getProfileProperty("(system)", "mfaportalprovider", "") : configurationProcessor.getProfileProperty("(system)", "mfaprovider", "");
                DataSet ds = qp.getPreparedSqlDataSet("SELECT sysuser.mfaprovider, sysuser.mfasecretkey, propertytree.objectname, (SELECT objectname FROM propertytree WHERE propertytreeid=? ) sysdefaultobjectname FROM sysuser JOIN connection ON sysuser.sysuserid=connection.sysuserid LEFT JOIN propertytree ON sysuser.mfaprovider=propertytree.propertytreeid WHERE connectionid=?", new Object[]{sysmfaprovider, connectionid});
                secretkey = ds.getRowCount() > 0 ? ds.getValue(0, "mfasecretkey") : "";
                String string = mfaprovider = ds.getRowCount() > 0 ? ds.getValue(0, "mfaprovider") : "";
                if (mfaprovider.length() == 0) {
                    mfaprovider = sysmfaprovider;
                }
                String string2 = baseWebMFAHandlerClass = ds.getRowCount() > 0 ? ds.getValue(0, "objectname") : "";
                if (baseWebMFAHandlerClass.length() == 0) {
                    String string3 = baseWebMFAHandlerClass = ds.getRowCount() > 0 ? ds.getValue(0, "sysdefaultobjectname") : "";
                }
                if ("Disable".equals(mfaprovider) || baseWebMFAHandlerClass.length() == 0) {
                    Trace.log("MFA provider not enabled");
                    prompt2ndFA = false;
                } else {
                    ConfigurationProcessor configProcessor = new ConfigurationProcessor(connectionid);
                    authenticationPL = configProcessor.getPolicy(mfaprovider, "Sapphire Custom");
                    authenticationPL.setDatabaseid(database);
                    if (authenticationPL != null && "Y".equals(authenticationPL.getProperty("enable", "Y"))) {
                        String rememberdevicedays = authenticationPL.getProperty("rememberdevicedays");
                        if (rememberdevicedays.length() > 0 && !"0".equals(rememberdevicedays)) {
                            String rememberDeviceCookie = (String)httpSession.getAttribute("rememberdevice");
                            if (rememberDeviceCookie != null && rememberDeviceCookie.length() > 0) {
                                String[] usernameTimes = StringUtil.split(rememberDeviceCookie = EncryptDecrypt.decrypt(rememberDeviceCookie, database), "|%|");
                                if (usernameTimes.length == 2 && usernameTimes[0].equals(username)) {
                                    try {
                                        String timestamp = usernameTimes[1];
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        sdf.parse(timestamp);
                                        int days = Integer.parseInt(rememberdevicedays);
                                        Calendar cal = sdf.getCalendar();
                                        cal.add(5, days);
                                        if (Calendar.getInstance().before(cal)) {
                                            Trace.log("Remembered device until " + sdf.format(cal.getTime()));
                                            String value = username + "|%|" + new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                                            value = EncryptDecrypt.encrypt(value, database);
                                            httpSession.setAttribute("rememberdevice", (Object)value);
                                            prompt2ndFA = false;
                                        }
                                    }
                                    catch (Exception e) {
                                        Trace.logWarn(e.getMessage());
                                    }
                                }
                            } else {
                                Trace.log("Not remembered device. Prompt for 2FA.");
                            }
                        }
                        if (baseWebMFAHandlerClass.indexOf("LVDefault2FAHandler") > 0) {
                            httpSession.setAttribute("allowReset2FA", (Object)"Y");
                        }
                    } else {
                        prompt2ndFA = false;
                    }
                }
                if (!prompt2ndFA) break block19;
                try {
                    baseWebMFAHandler = (BaseWebMFAHandler)Class.forName(baseWebMFAHandlerClass).newInstance();
                    baseWebMFAHandler.init(authenticationPL, username, database, connectionid, secretkey);
                    httpSession.setAttribute("baseWebMFAHandler", (Object)baseWebMFAHandler);
                }
                catch (Exception cnf) {
                    throw new ServletException((Throwable)cnf);
                }
            }
            catch (SapphireException se) {
                throw new ServletException((Throwable)se);
            }
        }
        return baseWebMFAHandler;
    }

    public static boolean isGlobalMFAEnabled(String connectionid, boolean isWebSSO, boolean isPortalUser) {
        com.labvantage.sapphire.admin.system.ConfigurationProcessor configurationProcessor = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(connectionid);
        boolean enabled = false;
        try {
            String mfaOption = configurationProcessor.getProfileProperty("(system)", isPortalUser ? "mfaportaloption" : "mfaoption", "0");
            if ("2".equals(mfaOption) && !isWebSSO) {
                enabled = true;
            }
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
        return enabled;
    }
}

