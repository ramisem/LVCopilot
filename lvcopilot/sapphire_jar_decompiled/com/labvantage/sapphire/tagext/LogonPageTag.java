/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.servlet.command.LoginException;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.Browser;
import sapphire.util.JstlUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LogonPageTag
extends BaseBodyTagSupport {
    static final String LABVANTAGE_CVS_ID = "$Revision: 91612 $";
    public static final String LOGON_NAME = "logon";
    private PropertyList logonProps = null;
    private String file;
    public static final String DEFAULTAPPLICATION = "LabVantage";

    public void setFile(String file) {
        this.file = file;
    }

    public static void addOverrides(PropertyList logonProps, File appHome) throws SapphireException {
        LogonPageTag.addOverrides(logonProps, appHome, null);
    }

    public static void addOverrides(PropertyList logonProps, File appHome, String subconfiguration) throws SapphireException {
        File logonxml;
        if (subconfiguration != null && !subconfiguration.isEmpty()) {
            if (subconfiguration.indexOf(".") > 0 || subconfiguration.indexOf("/") > 0 || subconfiguration.indexOf("\\") > 0) {
                throw new SapphireException("Subconfiguration contains invalid characters.");
            }
            String suffix = "_" + subconfiguration;
            logonxml = new File(appHome, "labvantagelogon" + suffix + ".xml");
        } else {
            logonxml = new File(appHome, "labvantagelogon.xml");
            if (!logonxml.exists()) {
                logonxml = new File(appHome, "sapphirelogon.xml");
            }
        }
        if (logonxml.exists()) {
            LogonPageTag.addOverridesImpl(logonProps, logonxml);
        }
    }

    private static void addOverridesImpl(PropertyList logonProps, File logonxml) throws SapphireException {
        PropertyList fileProps = new PropertyList();
        if (logonxml.exists()) {
            fileProps.setPropertyList(logonxml, false, false);
        } else {
            Trace.logDebug("Logon XML file '" + logonxml.getName() + "' does not exist.");
        }
        for (String prop : fileProps.keySet()) {
            PropertyList fileText;
            if (prop.equals("text")) {
                fileText = fileProps.getPropertyList("text");
                for (String textprop : fileText.keySet()) {
                    logonProps.getPropertyList("text").setProperty(textprop, fileText.getProperty(textprop));
                }
                continue;
            }
            if (prop.equals("databasealias")) {
                fileText = fileProps.getPropertyList("databasealias");
                for (String databasealiasprop : fileText.keySet()) {
                    logonProps.getPropertyList("databasealias").setProperty(databasealiasprop, fileText.getProperty(databasealiasprop));
                }
                continue;
            }
            if (prop.equals("browsercheck")) {
                fileText = fileProps.getPropertyList("browsercheck");
                for (String browsercheckprop : fileText.keySet()) {
                    logonProps.getPropertyList("browsercheck").setProperty(browsercheckprop, fileText.getProperty(browsercheckprop));
                }
                continue;
            }
            if (prop.equals("languages")) {
                PropertyListCollection collection = fileProps.getCollectionNotNull("languages");
                for (int i = 0; i < collection.size(); ++i) {
                    PropertyList lang = collection.getPropertyList(i);
                    if (lang == null || lang.isEmpty()) continue;
                    logonProps.getCollectionNotNull("languages").add(lang);
                }
                continue;
            }
            logonProps.setProperty(prop, fileProps.getProperty(prop));
        }
    }

    @Override
    public int doEndTag() throws JspTagException {
        this.logonProps = null;
        this.file = null;
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions() {
        this.file = JstlUtil.evaluateExpression(this.file, this.pageContext, "").toString();
    }

    public int doStartTag() throws JspTagException {
        this.logonProps = new PropertyList();
        LogonPageTag.setDefaults(this.logonProps);
        this.doInit();
        this.evaluateExpressions();
        Browser browser = new Browser(this.pageContext);
        try {
            String ib;
            boolean absoluteP;
            HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
            sapphire.util.HttpUtil util = new sapphire.util.HttpUtil(this.pageContext);
            Object databaseSelected = "";
            String subConfigurationId = "";
            boolean checkAlias = false;
            if (request.getParameter("config") != null && (subConfigurationId = request.getParameter("config")) != null && !subConfigurationId.isEmpty()) {
                subConfigurationId = subConfigurationId.toLowerCase();
            }
            if (request.getParameter("database") != null && (databaseSelected = request.getParameter("database")) != null && !((String)databaseSelected).isEmpty()) {
                databaseSelected = ((String)databaseSelected).toLowerCase();
                checkAlias = true;
            }
            if (databaseSelected == null || ((String)databaseSelected).isEmpty()) {
                databaseSelected = util.getCookieValue("previouslogondatabase");
            }
            String languageid = "";
            if (request.getParameter("language") != null) {
                languageid = request.getParameter("language");
                util.setCookieValue("consolelanguageid", languageid);
            } else {
                languageid = util.getCookieValue("consolelanguageid");
            }
            if (Configuration.getState() == 1) {
                this.logonProps.setProperty("isvalidconfig", "false");
                this.logonProps.setProperty("errormsg", Configuration.getErrorMsg());
            } else {
                LoginException loginException;
                Configuration configuration = Configuration.getInstance();
                this.logonProps.setProperty("buildinfo", "V" + Build.getVersion() + " Build " + Build.getBuild() + " " + Build.getBuildDate() + " (Patch " + Build.getPatch() + ")");
                this.logonProps.setProperty("serverinfo", "" + Configuration.getPlatformName(configuration.getPlatform()) + (configuration.isCluster() ? " Cluster " : " "));
                this.logonProps.setProperty("isvalidconfig", "true");
                try {
                    String tfile;
                    String webContext = ((HttpServletRequest)this.pageContext.getRequest()).getContextPath();
                    if (webContext.length() > 1) {
                        webContext = webContext.substring(1);
                    }
                    File logonXML = null;
                    if (this.file.length() > 0) {
                        if (this.file.toLowerCase().indexOf("[sapphirehome]") > -1) {
                            this.file = StringUtil.replaceAll(this.file, "[sapphirehome]", Configuration.getInstance().getSapphireHome());
                        }
                        if (this.file.toLowerCase().indexOf("[labvantagehome]") > -1) {
                            this.file = StringUtil.replaceAll(this.file, "[labvantagehome]", Configuration.getInstance().getSapphireHome());
                        }
                        if (this.file.toLowerCase().indexOf("[applicationhome]") > -1) {
                            this.file = StringUtil.replaceAll(this.file, "[applicationhome]", Configuration.getInstance().getApplicationHome());
                        }
                        tfile = FileUtil.getFileName(this.file, false) + "_" + webContext.toLowerCase() + ".xml";
                        this.logDebug("Logon File = " + tfile);
                        logonXML = new File(tfile);
                        if (!logonXML.exists()) {
                            this.logDebug("Logon File '" + logonXML.getName() + "' does not exist. ");
                        }
                    } else {
                        tfile = Configuration.getInstance().getApplicationHome() + "/labvantagelogon.xml";
                        logonXML = new File(tfile);
                        if (!logonXML.exists()) {
                            logonXML = new File(Configuration.getInstance().getApplicationHome() + "/sapphirelogon.xml");
                        }
                        if (!logonXML.exists()) {
                            this.logDebug("Logon File '" + logonXML.getName() + "' does not exist.");
                        }
                    }
                    LogonPageTag.addOverridesImpl(this.logonProps, logonXML);
                    if (!languageid.isEmpty()) {
                        LogonPageTag.addOverrides(this.logonProps, new File(Configuration.getInstance().getApplicationHome()), languageid);
                    }
                }
                catch (Exception e) {
                    this.logError("Error reading logon XML", e);
                    LogonPageTag.setDefaults(this.logonProps);
                }
                ConnectionProcessor cp = new ConnectionProcessor();
                Object[] serverDatabaseList = cp.getDatabaseList();
                Arrays.sort(serverDatabaseList);
                boolean isValidDatabaseId = false;
                boolean isSubdomainMode = this.logonProps.getProperty("subdomainmode", "N").startsWith("Y");
                if (isSubdomainMode) {
                    String domainName = request.getServerName().toLowerCase();
                    String mainDomainName = this.logonProps.getProperty("maindomain", "").toLowerCase();
                    if (mainDomainName.isEmpty()) {
                        mainDomainName = domainName.indexOf(".") > 0 ? domainName.substring(domainName.lastIndexOf(".")) : "";
                    }
                    if (!mainDomainName.isEmpty() && domainName.length() > mainDomainName.length() && domainName.indexOf(mainDomainName) > 0) {
                        domainName = domainName.substring(0, domainName.indexOf(mainDomainName) + (domainName.startsWith(".") ? 1 : -1));
                    }
                    String[] subDomains = StringUtil.split(domainName, ".");
                    StringBuilder subConfig = new StringBuilder();
                    for (int j = subDomains.length; j > 0; --j) {
                        String subDomain = subDomains[j - 1];
                        if (subConfig.length() > 0) {
                            subConfig.append("_");
                        }
                        subConfig.append(subDomain);
                        PropertyList logonPropsBackup = this.logonProps.copy();
                        try {
                            if (subConfig.length() <= 0) continue;
                            LogonPageTag.addOverrides(this.logonProps, new File(Configuration.getInstance().getApplicationHome()), subConfig.toString());
                            continue;
                        }
                        catch (Exception e) {
                            this.logError("Error reading subdomain specific logon XML", e);
                            this.logonProps = logonPropsBackup;
                        }
                    }
                }
                PropertyList logonPropsBackup = this.logonProps.copy();
                try {
                    if (subConfigurationId != null && subConfigurationId.length() > 0) {
                        LogonPageTag.addOverrides(this.logonProps, new File(Configuration.getInstance().getApplicationHome()), subConfigurationId);
                    }
                }
                catch (Exception e) {
                    this.logError("Error reading subconfiguration specific logon XML", e);
                    this.logonProps = logonPropsBackup;
                }
                if (databaseSelected != null && !((String)databaseSelected).isEmpty()) {
                    isValidDatabaseId = false;
                    for (Object dbListStr : serverDatabaseList) {
                        if (dbListStr == null || !((String)dbListStr).toLowerCase().equals(((String)databaseSelected).toLowerCase())) continue;
                        databaseSelected = dbListStr;
                        isValidDatabaseId = true;
                        break;
                    }
                }
                PropertyList alias = this.logonProps.getPropertyList("databasealias");
                if (!isValidDatabaseId && checkAlias && alias != null) {
                    HashMap<String, String> aliasMap = new HashMap<String, String>();
                    for (Object aliases : alias.keySet()) {
                        String aliasDb = (String)aliases;
                        String aliasId = alias.getProperty(aliasDb).toLowerCase();
                        aliasMap.put(aliasId, aliasDb);
                    }
                    String databaseAlias = (String)aliasMap.get(((String)databaseSelected).toLowerCase());
                    if (databaseAlias != null && !databaseAlias.isEmpty()) {
                        databaseSelected = databaseAlias;
                    }
                    for (Object dbListStr : serverDatabaseList) {
                        if (dbListStr == null || !((String)dbListStr).toLowerCase().equals(((String)databaseSelected).toLowerCase())) continue;
                        databaseSelected = dbListStr;
                        isValidDatabaseId = true;
                        break;
                    }
                }
                String[] logonPropsDatabaseList = StringUtil.split(this.logonProps.getProperty("databaselist"), ";");
                boolean hasLogonPropsDatabaseList = false;
                if (logonPropsDatabaseList.length > 0) {
                    ArrayList<Object> xmlDbList = new ArrayList<Object>();
                    block12: for (String xmlDb : logonPropsDatabaseList) {
                        for (Object serverDb : serverDatabaseList) {
                            if (serverDb == null || !((String)serverDb).toLowerCase().equals(xmlDb.trim().toLowerCase())) continue;
                            xmlDbList.add(serverDb);
                            continue block12;
                        }
                    }
                    if (xmlDbList.size() > 0) {
                        logonPropsDatabaseList = xmlDbList.toArray(new String[0]);
                        hasLogonPropsDatabaseList = true;
                    }
                }
                if (configuration.requireLicensePerDatabase()) {
                    if (hasLogonPropsDatabaseList) {
                        this.logonProps.setProperty("requiredatabaselicense", "L");
                    } else {
                        this.logonProps.setProperty("requiredatabaselicense", "Y");
                    }
                } else {
                    this.logonProps.setProperty("requiredatabaselicense", "N");
                }
                this.pageContext.setAttribute("databaselist", (Object)(hasLogonPropsDatabaseList ? logonPropsDatabaseList : serverDatabaseList));
                if (isValidDatabaseId) {
                    this.pageContext.setAttribute("databaseparam", databaseSelected);
                    this.logonProps.setProperty("databaseparam", (String)databaseSelected);
                    if (configuration.requireLicensePerDatabase() && !hasLogonPropsDatabaseList) {
                        this.logonProps.setProperty("selectdatabase", "T");
                    }
                }
                String errorMsg = request.getParameter("errormsg");
                if (request.getAttribute("javax.servlet.error.exception") != null) {
                    loginException = (LoginException)((Object)request.getAttribute("javax.servlet.error.exception"));
                    errorMsg = loginException.getMessage();
                    if (errorMsg.indexOf("command=logoff") > -1) {
                        this.logonProps.setProperty("encode", "N");
                    }
                } else {
                    loginException = new LoginException("rc?command=page&page=SystemAdminTramline");
                }
                if (Configuration.getState() == 0) {
                    this.logonProps.setProperty("logonmode", "database");
                } else if (Configuration.getErrorMsg() == null || Configuration.getErrorMsg().length() == 0) {
                    this.logonProps.setProperty("logonmode", "database");
                    errorMsg = "LabVantage is still starting up - please try again later.<br>If the problem persists then contact your system administrator.";
                } else {
                    this.logonProps.setProperty("logonmode", "database");
                    errorMsg = "A problem has been found starting LabVantage. Last error reported was:<br>" + Configuration.getErrorMsg();
                }
                if (errorMsg == null) {
                    errorMsg = "";
                }
                this.logonProps.setProperty("errormsg", errorMsg);
                this.logonProps.setProperty("nexturl", loginException.getNextURL());
                this.logonProps.setProperty("ignorelogonurl", loginException.ignoreLogonUrl() ? "Y" : "N");
                HashMap nexturlParams = loginException.getNexturlParams();
                if (nexturlParams != null && nexturlParams.size() > 0) {
                    this.logonProps.setProperty("nexturlparams", new PropertyList(nexturlParams));
                }
                String logoHtml = HttpUtil.getCompanyLogo(this.pageContext, false);
                String applogoHtml = HttpUtil.getApplicationLogo(this.logonProps.getProperty("applicationtitle") + (browser.isMobile() ? " <i>MOBILE<i>" : ""), this.logonProps.getProperty("applicationimage"));
                this.pageContext.setAttribute("logo", (Object)logoHtml);
                this.pageContext.setAttribute("applogo", (Object)applogoHtml);
                if (!browser.isMobile()) {
                    String style;
                    String image1 = this.logonProps.getProperty("topleftimage", "WEB-OPAL/layouts/images/logon_image1.jpg");
                    image1 = image1 != null && image1.length() > 0 ? "<img src=\"" + image1 + "\" alt=\"" + DEFAULTAPPLICATION + "\" title=\"" + DEFAULTAPPLICATION + "\">" : "&nbsp;";
                    this.pageContext.setAttribute("topleftimage", (Object)image1);
                    String image2 = this.logonProps.getProperty("bottomrightimage", "WEB-OPAL/layouts/images/logon_image2.jpg");
                    image2 = image2 != null && image2.length() > 0 ? "<img src=\"" + image2 + "\" alt=\"" + DEFAULTAPPLICATION + "\" title=\"" + DEFAULTAPPLICATION + "\">" : "&nbsp;";
                    this.pageContext.setAttribute("bottomrightimage", (Object)image2);
                    String bg = this.logonProps.getProperty("backgroundimage", "");
                    if (bg != null && bg.length() > 0) {
                        style = this.logonProps.getProperty("bodystyle", "");
                        if (style != null && style.length() > 0) {
                            if (!style.endsWith(";")) {
                                style = style + ";";
                            }
                            style = style + "background-image: url(" + bg + ");";
                        } else {
                            style = "background-image: url(" + bg + ");";
                        }
                        style = style + "background-repeat: " + this.logonProps.getProperty("backgroundrepeat", "no-repeat") + ";";
                        style = style + "background-position: " + this.logonProps.getProperty("backgroundposition", "top center") + ";";
                        style = style + "background-attachment: " + this.logonProps.getProperty("backgroundattachment", "") + ";";
                        this.logonProps.setProperty("bodystyle", style);
                    }
                    if ((bg = this.logonProps.getProperty("cellbackgroundimage", "WEB-OPAL/layouts/images/bg_logo.jpg")) != null && bg.length() > 0) {
                        style = this.logonProps.getProperty("cellstyle", "");
                        if (style != null && style.length() > 0) {
                            if (!style.endsWith(";")) {
                                style = style + ";";
                            }
                            style = style + "background-image: url(" + bg + ");";
                        } else {
                            style = "background-image: url(" + bg + ");";
                        }
                        style = style + "background-repeat: " + this.logonProps.getProperty("cellbackgroundrepeat", "no-repeat") + ";";
                        style = style + "background-attachment: " + this.logonProps.getProperty("cellbackgroundattachment", "fixed") + ";";
                        style = style + "background-position: " + this.logonProps.getProperty("cellbackgroundposition", "top center") + ";";
                        style = style + "width: " + this.logonProps.getProperty("cellwidth", "480px") + ";";
                        this.logonProps.setProperty("cellstyle", style);
                    }
                }
            }
            boolean bl = absoluteP = request.getParameter("absolutepaths") != null ? request.getParameter("absolutepaths").equalsIgnoreCase("y") : false;
            if (absoluteP) {
                StringBuffer url = request.getRequestURL();
                this.logonProps.setProperty("urlprefix", url.substring(0, url.lastIndexOf("/") + 1));
            } else {
                this.logonProps.setProperty("urlprefix", "");
            }
            if (this.logonProps.getPropertyList("text") != null && this.logonProps.getPropertyList("text").getProperty("invalidbrowser").length() > 0) {
                ib = this.logonProps.getPropertyList("text").getProperty("invalidbrowser");
                ib = StringUtil.replaceAll(ib, "[browsername]", browser.getName());
                ib = StringUtil.replaceAll(ib, "[browserversion]", "" + browser.getVersion());
                ib = StringUtil.replaceAll(ib, "[browser]", browser.getName() + " " + browser.getVersion());
                ib = StringUtil.replaceAll(ib, "[browsersupported]", browser.getSupportedText());
                this.logonProps.getPropertyList("text").setProperty("invalidbrowser", ib);
            }
            if (this.logonProps.getPropertyList("text") != null && this.logonProps.getPropertyList("text").getProperty("unsupportedbrowser").length() > 0) {
                ib = this.logonProps.getPropertyList("text").getProperty("unsupportedbrowser");
                ib = StringUtil.replaceAll(ib, "[browsername]", browser.getName());
                ib = StringUtil.replaceAll(ib, "[browserversion]", "" + browser.getVersion());
                ib = StringUtil.replaceAll(ib, "[browser]", browser.getName() + " " + browser.getVersion());
                ib = StringUtil.replaceAll(ib, "[browsersupported]", browser.getSupportedText());
                this.logonProps.getPropertyList("text").setProperty("unsupportedbrowser", ib);
            }
            if (this.logonProps.getPropertyList("text") != null && this.logonProps.getPropertyList("text").getProperty("logonunsecureconnection").length() > 0) {
                if (request.getProtocol().indexOf("HTTP") != 0 || request.isSecure()) {
                    this.logonProps.getPropertyList("text").setProperty("logonunsecureconnection", "");
                } else {
                    ib = this.logonProps.getPropertyList("text").getProperty("logonunsecureconnection");
                    String secureURL = this.logonProps.getPropertyList("text").getProperty("logonsecureurl");
                    ib = StringUtil.replaceAll(ib, "[logonsecureURL]", "<a href=\"" + secureURL + "\">Secure Logon<a>");
                    this.logonProps.getPropertyList("text").setProperty("logonunsecureconnection", ib);
                }
            }
            this.pageContext.setAttribute(LOGON_NAME, (Object)this.logonProps);
        }
        catch (Exception e) {
            LogonPageTag.setDefaults(this.logonProps);
            this.logonProps.setProperty("isvalidconfig", "false");
            this.logonProps.setProperty("errormsg", "Configuration instance not created. Check SapphireController servlet is starting correctly.");
        }
        return 1;
    }

    public static void setDefaults(PropertyList logonProps) {
        logonProps.setProperty("autocomplete", "Y");
        logonProps.setProperty("applicationtitle", "");
        logonProps.setProperty("applicationimage", "");
        logonProps.setProperty("applicationimagetitle", "");
        logonProps.setProperty("headercolor", "");
        logonProps.setProperty("pagetitle", "LabVantage Logon");
        logonProps.setProperty("homepageurl", "rc?command=page&page=SystemAdminTramline");
        logonProps.setProperty("defaultdatabase", "");
        logonProps.setProperty("databasealias", new PropertyList());
        logonProps.setProperty("languages", new PropertyList());
        logonProps.setProperty("showdatabase", "Y");
        logonProps.setProperty("showresetpassword", "Y");
        logonProps.setProperty("selectdatabase", "Y");
        logonProps.setProperty("subdomainmode", "N");
        PropertyList text = new PropertyList();
        text.setProperty("enterdatabaselogon", "Sign into [database]");
        text.setProperty("database", "Database");
        text.setProperty("databaseusername", "Username");
        text.setProperty("databasepassword", "Password");
        text.setProperty("logonbutton", "Sign in");
        text.setProperty("resetbutton", "Reset");
        text.setProperty("enterresetpassword", "Enter your username and email address");
        text.setProperty("databaseemail", "Email");
        text.setProperty("resetpasswordbutton", "Reset Password");
        text.setProperty("returntologonbutton", "Cancel");
        text.setProperty("passwordreset", "Reset password");
        text.setProperty("forgotcredentials", "Forgot password?");
        text.setProperty("forgotcredentialsmessage", "We cannot lookup or send your old password but we will send you an email with a link to let you change it.");
        text.setProperty("externalauthentication", "Your password is managed by an external authentication system and cannot be changed here - contact your system administrator to reset your external password.");
        text.setProperty("forgotdisableduser", "This username is disabled - you cannot reset the password of a disabled user.");
        text.setProperty("requestemailsubject", "LabVantage Reset Password Request");
        text.setProperty("requestemailmessage", "<p>We have received a request to reset your password.</p>\n<p>Click on the <i>Reset Password</i> link below to receive a further email with your new temporary password.</p>\n<p>If you have not requested a password reset then please ignore this email.</p>\n<p><a href=\"[resetpasswordlink]\">Reset Password</a></p>\n<p>You can manually copy and paste the following text into your browser if the link doesn't work.</p>\n<p>[resetpasswordlink]</p><p>This link expires after [resetlinkexpirytimeout] minutes from the time it was sent.</p>");
        text.setProperty("requestemailsent", "We have sent you an email with a link to confirm the reset password request.");
        text.setProperty("resetemailsubject", "LabVantage Password Reset");
        text.setProperty("resetemailmessage", "<p>Your password has been reset to: [newpassword]</p>\n<p>Goto the <a href=\"[resetpasswordlink]\">LabVantage logon page</a> to logon.</p>\n<p>Immediately after logon you will prompted to create a new password.</p>");
        text.setProperty("resetemailsent", "We have sent you an email with your new temporary password.");
        text.setProperty("emailsendfail", "Failed to send reset password email - please contact your system administrator");
        text.setProperty("expiredresetlink", "This link for a password reset has expired. If you still wish to reset your password, goto the LabVantage logon page and click the 'Forgot password' link.");
        text.setProperty("invalidsystememail", "Your system is not configured correctly to reset passwords - contact your system administrator to configure the system mail settings.");
        text.setProperty("invalidresetpassword", "You need to enter a username and email address");
        text.setProperty("invalidusernameemail", "Unrecognized username/email combination - please try again");
        text.setProperty("invaliddatabaselogon", "You need to enter a username and password.");
        text.setProperty("resetpasswordgenericmessage", "If your information matches, you will receive an email, on your registered email address, with instructions on how to reset your password. If you don't receive it in the next few minutes please contact you Administrator.");
        text.setProperty("cookiesnotenabled", "You do not have cookies enabled on your browser. Some pages may not function properly.");
        text.setProperty("unsupportedbrowser", "This browser ([browsername], [browserversion]) is not recommended for this application.");
        text.setProperty("invalidbrowser", "This browser version combination ([browsername], [browserversion]) is not supported by this version of LabVantage.");
        text.setProperty("logonunsecureconnection", "Your connection to the application is not secure. Certain content may not function as designed.");
        logonProps.setProperty("text", text);
        logonProps.setProperty("rtl", "N");
        PropertyList browser = new PropertyList();
        browser.setProperty("allowedge", "Y");
        browser.setProperty("minedge", "");
        browser.setProperty("maxedge", "");
        browser.setProperty("allowchrome", "Y");
        browser.setProperty("minchrome", "");
        browser.setProperty("maxchrome", "");
        browser.setProperty("allowsafari", "Y");
        browser.setProperty("minsafari", "");
        browser.setProperty("maxsafari", "");
        browser.setProperty("allowmobile", "Y");
        browser.setProperty("stoplogin", "N");
        logonProps.setProperty("browsercheck", browser);
    }
}

