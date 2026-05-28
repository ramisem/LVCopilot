/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.servlet.command.Ping;
import com.labvantage.sapphire.servlet.command.TagRequestPropertyHandler;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.error.ErrorHandler;
import sapphire.servlet.RequestContext;
import sapphire.util.ForwardUtil;
import sapphire.util.Logger;
import sapphire.util.StringUtil;

public class TagRequest {
    public static final String LOGNAME = "TagRequest";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sdiForm(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String formCommand = requestContext.getProperty("__formcommand");
        String prefixList = requestContext.getProperty("__prefixlist");
        String nextpage = requestContext.getProperty("__nexturl");
        String self = requestContext.getProperty("__self");
        String hasOldFormSuccessTag = requestContext.getProperty("__hasoldformsucesstag");
        boolean formSuccess = true;
        boolean selfSubmit = false;
        String lastError = "";
        String ruleError = "";
        String ruleWarning = "";
        if (nextpage.startsWith("(self)")) {
            nextpage = nextpage.substring(6);
            selfSubmit = true;
        }
        Trace.log(LOGNAME, "SDIForm, formcommand=" + formCommand + ", prefixlist=" + prefixList + ", nexturl=" + nextpage + ", __hassucessfailuretag=" + hasOldFormSuccessTag);
        String[] prefix = StringUtil.split(prefixList, ";");
        ForwardUtil forward = new ForwardUtil((ServletRequest)request);
        for (int i = 0; i < prefix.length; ++i) {
            String[] attributes = RequestParser.parseFormAttributes(requestContext.getProperty("__" + prefix[i] + "attributes"));
            if (attributes.length == 31) {
                String queryid = attributes[0];
                String[] queryparams = new String[]{attributes[1], attributes[2], attributes[3], attributes[4], attributes[5], attributes[6], attributes[7], attributes[8], attributes[9], attributes[10], attributes[11], attributes[12]};
                String queryfrom = attributes[13];
                String querywhere = attributes[14];
                String queryorderby = attributes[15];
                String sdcid = attributes[16];
                String keyid1list = attributes[17];
                String keyid2list = attributes[18];
                String keyid3list = attributes[19];
                String rsetid = attributes[20];
                String sdiRequest = attributes[21];
                String lockoption = attributes[22];
                String nullvalue = attributes[23];
                String retrieve = attributes[24];
                String propsmatch = attributes[25];
                String mergequerywhere = attributes[26];
                String versionstatus = attributes[27];
                String retrievelimit = attributes[28];
                String pageid = attributes[29];
                String pageedition = attributes[30];
                if (selfSubmit) {
                    this.setUserForwardParams(forward, request);
                    forward.setProperty("__tagrequest", "sdiform");
                    forward.setProperty("__sdcid", sdcid);
                    forward.setProperty("__queryid", queryid);
                    forward.setProperty("__param1", queryparams[0]);
                    forward.setProperty("__param2", queryparams[1]);
                    forward.setProperty("__param3", queryparams[2]);
                    forward.setProperty("__param4", queryparams[3]);
                    forward.setProperty("__param5", queryparams[4]);
                    forward.setProperty("__param6", queryparams[5]);
                    forward.setProperty("__param7", queryparams[6]);
                    forward.setProperty("__param8", queryparams[7]);
                    forward.setProperty("__param9", queryparams[8]);
                    forward.setProperty("__param10", queryparams[9]);
                    forward.setProperty("__param11", queryparams[10]);
                    forward.setProperty("__param12", queryparams[11]);
                    forward.setProperty("__queryfrom", EncryptDecrypt.obfsql(queryfrom));
                    forward.setProperty("__querywhere", EncryptDecrypt.obfsql(querywhere));
                    forward.setProperty("__queryorderby", EncryptDecrypt.obfsql(queryorderby));
                    forward.setProperty("__keyid1", keyid1list);
                    forward.setProperty("__keyid2", keyid2list);
                    forward.setProperty("__keyid3", keyid3list);
                    forward.setProperty("__rsetid", rsetid);
                    forward.setProperty("__request", sdiRequest);
                    forward.setProperty("__lockoption", lockoption);
                    forward.setProperty("__nullvalue", nullvalue);
                    forward.setProperty("__retrieve", retrieve);
                    forward.setProperty("__propsmatch", propsmatch);
                    forward.setProperty("__mergequerywhere", mergequerywhere);
                    forward.setProperty("__versionstatus", versionstatus);
                    forward.setProperty("__retrievelimit", retrievelimit);
                    forward.setProperty("__pageid", pageid);
                    forward.setProperty("__pageedition", pageedition);
                    forward.setProperty("keyid1", keyid1list);
                    forward.setProperty("keyid2", keyid2list);
                    forward.setProperty("keyid3", keyid3list);
                }
                if (!formCommand.equalsIgnoreCase("save")) continue;
                requestContext.setProperty("__prefix", prefix[i]);
                requestContext.setProperty("__" + prefix[i] + "sdcid", sdcid);
                requestContext.setProperty("__" + prefix[i] + "request", sdiRequest);
                RequestProcessor requestProcessor = new RequestProcessor(requestContext.getConnectionId());
                try {
                    HashMap requestProps = requestProcessor.processRequest(TagRequestPropertyHandler.class.getName(), requestContext.getPropertyList());
                    String traceLogId = (String)requestProps.get("tracelogid");
                    if (traceLogId != null && traceLogId.length() > 0) {
                        forward.setProperty("tracelogid", traceLogId);
                    }
                    HashMap keylist = (HashMap)requestProps.get("__keylist");
                    ErrorHandler errorHandler = (ErrorHandler)requestProps.get("ERRORHANDLER");
                    if (errorHandler != null && errorHandler.size() > 0 && self != null && self.length() > 0) {
                        selfSubmit = true;
                        nextpage = self;
                        ruleError = errorHandler.hasErrors() ? errorHandler.getEncodedString() : "";
                        String string = ruleWarning = !errorHandler.hasErrors() && errorHandler.hasInfoErrors() ? errorHandler.getEncodedString() : "";
                    }
                    if (errorHandler == null || !errorHandler.hasErrors()) {
                        this.setUserForwardParams(forward, request);
                        String newkeyid1 = (String)keylist.get("newkeyid1");
                        String newkeyid2 = (String)keylist.get("newkeyid2");
                        String newkeyid3 = (String)keylist.get("newkeyid3");
                        if (newkeyid1 != null && newkeyid1.length() > 0) {
                            Pattern pattern1 = Pattern.compile("\\(auto_keyid1_(\\d)+\\)");
                            String string = keyid1list = keyid1list.length() > 0 && !pattern1.matcher(keyid1list).matches() ? keyid1list + ";" + newkeyid1 : newkeyid1;
                        }
                        if (newkeyid2 != null && newkeyid2.length() > 0) {
                            Pattern pattern2 = Pattern.compile("\\(auto_keyid2_(\\d)+\\)");
                            String string = keyid2list = keyid2list.length() > 0 && !pattern2.matcher(keyid2list).matches() ? keyid2list + ";" + newkeyid2 : newkeyid2;
                        }
                        if (newkeyid3 != null && newkeyid3.length() > 0) {
                            Pattern pattern3 = Pattern.compile("\\(auto_keyid3_(\\d)+\\)");
                            keyid3list = keyid3list.length() > 0 && !pattern3.matcher(keyid3list).matches() ? keyid3list + ";" + newkeyid3 : newkeyid3;
                        }
                        forward.setProperty("keyid1", keyid1list);
                        forward.setProperty("keyid2", keyid2list);
                        forward.setProperty("keyid3", keyid3list);
                        forward.setProperty("action_keyid1", keyid1list);
                        forward.setProperty("action_keyid2", keyid2list);
                        forward.setProperty("action_keyid3", keyid3list);
                        if (selfSubmit) {
                            if (newkeyid1 != null && newkeyid1.length() > 0) {
                                Ping ping = new Ping();
                                sapphire.util.HttpUtil httpUtil = new sapphire.util.HttpUtil(request, response);
                                ping.clearRSets(request, servletContext, httpUtil.getCookieValue("rsetlist"));
                                forward.setProperty("__rsetid", "");
                                forward.setProperty("__keyid1", keyid1list);
                                forward.setProperty("__keyid2", keyid2list);
                                forward.setProperty("__keyid3", keyid3list);
                                forward.setProperty("hasnewkeyid", "Y");
                                continue;
                            }
                            String extraprops = requestContext.getProperty("__pr_extraprops");
                            CMTPolicy cmtPolicy = CMTPolicy.getPolicy(requestContext.getConnectionId(), sdcid);
                            String changecontrolledflag = cmtPolicy.getChangeControlledFlag();
                            if (!extraprops.contains("checkinsdiflag=Y") && !"Y".equals(changecontrolledflag) && !"T".equals(changecontrolledflag)) continue;
                            new DAMProcessor(requestContext.getConnectionId()).clearRSet(rsetid);
                            forward.setProperty("__rsetid", "");
                            continue;
                        }
                        forward.setProperty("sdcid", sdcid);
                        forward.setProperties(keylist);
                        DAMProcessor damProcessor = new DAMProcessor(requestContext.getConnectionId());
                        damProcessor.clearRSet(rsetid);
                        continue;
                    }
                    throw new SapphireException(errorHandler.getEncodedString());
                }
                catch (SapphireException e) {
                    if (selfSubmit) {
                        formSuccess = false;
                        this.setAllForwardParams(forward, request);
                        forward.setProperty("__formcommand", "refresh");
                        lastError = e.getMessage();
                        continue;
                    }
                    Trace.log(LOGNAME, requestProcessor.getErrorStack().toString());
                    throw new ServletException("Failed to save sdi data. Exception: " + e.getMessage());
                }
            }
            throw new ServletException("Form attributes (__attributes) not defined correctly");
        }
        try (PrintWriter output = response.getWriter();){
            output.print(HttpUtil.getDocType());
            output.print("<html>");
            output.print("<head>");
            output.print(HttpUtil.getMetaTags());
            output.print("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
            output.print(HttpUtil.getCoreStyleSheets(true, request));
            output.print("<script>");
            output.print("var i = 20;");
            output.print("function doDot(){var d = document.getElementById('dots');if (d != null && i > 0){dots.innerHTML += '.';i--;window.setTimeout(doDot, 500);}}");
            output.print("</script>");
            output.print("</head>");
            output.print("<body style=\"padding:20px;font-size:10pt;overflow:hidden;\">");
            forward.setProperty("__formsuccess", formSuccess ? "true" : "false");
            forward.setProperty("__lasterror", lastError);
            forward.setProperty("__ruleerror", ruleError);
            forward.setProperty("__rulewarning", ruleWarning);
            TranslationProcessor tp = new TranslationProcessor(requestContext.getConnectionId());
            if ("true".equals(hasOldFormSuccessTag)) {
                output.println(tp.translate("Data Saved. Processing Success") + "<span id=\"dots\">..</span><script>doDot()</script>");
                forward.setProperty("__nexturl", nextpage);
                forward.setProperty("__hasoldformsucesstag", "true");
                output.println(forward.getForm("", self, "post", true));
            } else {
                output.println(tp.translate("Data Saved. Refreshing") + "<span id=\"dots\">..</span><script>doDot()</script>");
                output.println(forward.getForm("", nextpage, "post", true));
            }
            output.print("</body>");
            output.print("</html>");
        }
        catch (IOException ioe) {
            Logger.logStackTrace(ioe);
            throw new ServletException(ioe.getMessage());
        }
    }

    private void setUserForwardParams(ForwardUtil forward, HttpServletRequest request) {
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String propertyid = (String)e.nextElement();
            if (propertyid.indexOf("forward_") != 0 && (RequestParser.isSDIName(propertyid) || propertyid.equals("command"))) continue;
            forward.setProperty(propertyid, request.getParameter(propertyid));
        }
    }

    private void setAllForwardParams(ForwardUtil forward, HttpServletRequest request) {
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String propertyid = (String)e.nextElement();
            forward.setProperty(propertyid, request.getParameter(propertyid));
        }
    }
}

