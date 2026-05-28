/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServlet
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.JspWriter
 *  org.apache.commons.codec.digest.DigestUtils
 */
package com.labvantage.sapphire.servlet;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.gwt.shared.CommandConstants;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.sql.DataSource;
import org.apache.commons.codec.digest.DigestUtils;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public abstract class BaseController
extends HttpServlet
implements CommandConstants {
    public static final String COMMAND_HOME = "home";
    public static final String COMMAND_LOGON = "logon";
    public static final String COMMAND_LOGOFF = "logoff";
    private static long jspScrollCounter = 0L;

    protected void renderBootstrap(HttpServletResponse response, String title, String root, String nocachejs, String propsVar, PropertyList props) throws SapphireException, IOException {
        PrintWriter out = response.getWriter();
        String html = "<!DOCTYPE html>\n<html>\n<head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n    <title>" + title + "</title>\n    <script src='" + root + "?requestcommand=loadtranslations&command=ajax'></script>\n    <script type=\"text/javascript\" language='javascript' src='" + root + "/" + nocachejs + "'></script>\n" + HttpUtil.getGWTEncryptionJS(this.getConfigPropsFile(this.getConfiguration())) + "    <script type=\"text/javascript\" language='javascript'>\n    var " + propsVar + " = " + props.toJSONString(false) + ";    </script>\n</head>\n<body>\n<noscript>\n    <div style=\"width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color:white; border: 1px solid red; padding: 4px; font-family: sans-serif\">\n        Your web browser must have JavaScript enabled\n        in order for this application to display correctly.\n    </div>\n</noscript>\n</body>\n</html>";
        out.print(html);
        out.close();
    }

    protected void htmlCommandError(HttpServletResponse response, String productName, Exception e) throws IOException {
        PrintWriter out = response.getWriter();
        String html = "<!DOCTYPE html>\n<html>\n<head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n    <title>" + productName + "</title>\n</head>\n<style>\nbody    { font-size: 0.7em; margin-top: 0; margin-left:0; margin-right:0; font-family: Verdana, Arial, Helvetica, \"MS Sans Serif\" }\np       { margin-top: .5em; margin-bottom: .5em; margin-left: .5em }\n.ConsoleMainHeader { background: #FFD700 }\n.ConsolePageTitle  { color: white; font-weight: bold; margin-top: 0; margin-bottom:.5em; font-size: 11pt; background: steelblue }\n</style>\n<body>\n<table border=\"0\" cellpadding=\"4\" cellspacing=\"0\" style=\"width:100%; height:3.5em\"><tr class=\"ConsoleMainHeader\"><td class=\"ConsolePageTitle\">" + productName + "</td></tr></table><p>" + productName + " Error: " + e.getMessage() + "</p></body>\n</html>";
        out.print(html);
        out.close();
    }

    protected void loadTranslations(DataSet transTable, String root, String logger) {
        try {
            File configFile;
            String language;
            Configuration configuration = this.getConfiguration();
            transTable = new DataSet();
            transTable.addColumn("textid", 0);
            transTable.addColumn("transtext", 0);
            if (new File(configuration.getSapphireHome() + "console").exists() && (language = BaseController.getConfigProperty(configFile = this.getConfigPropsFile(configuration), "com.labvantage.sapphire.server.language", "en")).length() > 0) {
                String[] translations = StringUtil.split(FileUtil.getFileString(new File(configuration.getSapphireHome() + "/" + root + "/languages/translations_" + language + ".txt")), "\n");
                for (int i = 0; i < translations.length; ++i) {
                    if (translations[i].startsWith("//")) continue;
                    int row = transTable.addRow();
                    int pos = translations[i].indexOf("\t");
                    transTable.setValue(row, "textid", pos != -1 ? translations[i].substring(0, pos) : translations[i]);
                    transTable.setValue(row, "transtext", pos != -1 ? translations[i].substring(pos + 1) : "");
                }
            }
        }
        catch (Exception e) {
            transTable = null;
            Trace.logError(logger, (Object)("Failed to load translations. Reason: " + e.getMessage()), e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected String logon(HttpServletRequest request, HttpServletResponse response, PropertyList commandProps, String logonCookie, String logger) {
        HttpUtil httpUtil = new HttpUtil(request, response);
        Connection conn = null;
        try {
            Configuration configuration = this.getConfiguration();
            File configFile = this.getConfigPropsFile(configuration);
            conn = BaseController.getDataSource(configuration, BaseController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb")).getConnection();
            String url = conn.getMetaData().getURL();
            String username = commandProps.getProperty("username");
            if (!username.equalsIgnoreCase(conn.getMetaData().getUserName())) {
                throw new Exception("Unrecognized username!");
            }
            Class.forName(BaseController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms").equals("ORA") ? BaseController.getConfigProperty(configFile, "com.labvantage.sapphire.server.jdbcdriver.oracle", "oracle.jdbc.driver.OracleDriver") : BaseController.getConfigProperty(configFile, "com.labvantage.sapphire.server.jdbcdriver.sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver")).newInstance();
            String password = commandProps.getProperty("password");
            if (password != null && password.indexOf("{|}") == 0) {
                password = EncryptDecrypt.decryptConsoleRSA(this.getConfigPropsFile(this.getConfiguration()), password.substring("{|}".length()));
            }
            DriverManager.getConnection(url, username, password);
            String authCookieValue = DigestUtils.md5Hex((String)String.valueOf(Math.round(Math.random() * 1.0E7 * 1000000.0)));
            httpUtil.setCookieValue(logonCookie, authCookieValue, false, true);
            request.getSession().setAttribute(logonCookie, (Object)authCookieValue);
            String string = "ok";
            return string;
        }
        catch (Exception e) {
            Trace.logError(logger, (Object)("Logon failed. Reason: " + e.getMessage()), e);
            String string = "fail";
            return string;
        }
        finally {
            try {
                conn.close();
                conn = null;
            }
            catch (SQLException sQLException) {}
        }
    }

    protected void logoff(HttpServletRequest request, HttpServletResponse response, String logonCookie, String productName, String loginURI) throws IOException {
        HttpUtil httpUtil = new HttpUtil(request, response);
        httpUtil.removeCookie(logonCookie);
        request.getSession().removeAttribute(logonCookie);
        PrintWriter out = response.getWriter();
        String html = "<!DOCTYPE html>\n<html>\n<head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n    <title>" + productName + "</title>\n</head>\n<style>\nbody    { font-size: 0.7em; margin-top: 0; margin-left:0; margin-right:0; font-family: Verdana, Arial, Helvetica, \"MS Sans Serif\" }\np       { margin-top: .5em; margin-bottom: .5em; margin-left: .5em }\n.ConsoleMainHeader { background: steelblue }\n.ConsolePageTitle  { color: white; font-weight: bold; margin-top: 0; margin-bottom:.5em; font-size: 11pt; background: steelblue }\n</style>\n<body onload=\"document.cookie='" + logonCookie + "=N;'\">\n<table border=\"0\" cellpadding=\"4\" cellspacing=\"0\" style=\"width:100%; height:3.5em\"><tr class=\"ConsoleMainHeader\"><td class=\"ConsolePageTitle\">" + productName + "</td></tr></table><p>You are no longer logged onto " + productName + "!</p><p><a href=\"" + loginURI + "\">Log back in</a></p></body>\n</html>";
        out.print(html);
        out.close();
    }

    public static Configuration getConfiguration(ServletContext servletContext) {
        Configuration configuration = Configuration.getPlatformConfiguration(servletContext.getServerInfo());
        if (configuration != null) {
            try {
                configuration.setServerHostName(InetAddress.getLocalHost().getHostName());
                configuration.setServerid(System.getProperty("LABVANTAGE_SERVER"));
                configuration.setAutomationServer(System.getProperty("AUTOMATION_SERVER"));
                configuration.setJndiEJBPrefix("com/labvantage/sapphire");
            }
            catch (UnknownHostException e) {
                configuration.setServerHostName("(unknown)");
            }
            configuration.setSapphireHome(Configuration.getSapphireHome(servletContext));
        }
        return configuration;
    }

    public static Configuration getConfiguration(String serverInfo, String labvantagehome, String serverId) {
        Configuration configuration = Configuration.getPlatformConfiguration(serverInfo);
        configuration.setServerid(serverId);
        configuration.setJndiEJBPrefix("com/labvantage/sapphire");
        configuration.setServerHostName("(unknown)");
        configuration.setSapphireHome(labvantagehome);
        return configuration;
    }

    protected Configuration getConfiguration() {
        return BaseController.getConfiguration(this.getServletContext());
    }

    protected abstract File getConfigPropsFile(Configuration var1) throws SapphireException;

    protected static File getConfigPropsFile(Configuration configuration, String dirname, String filename) throws SapphireException {
        String sapphireHome = configuration.getSapphireHome();
        String platform = Configuration.getPlatformName(configuration.getPlatform());
        File file = new File(sapphireHome + "/" + dirname + "/" + filename + "_" + platform.toLowerCase());
        if (file.exists()) {
            return file;
        }
        file = new File(sapphireHome + "/" + dirname + "/" + filename);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public static String getConfigProperty(File file, String propertyid) {
        return BaseController.getConfigProperty(file, propertyid, "");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getConfigProperty(File file, String propertyid, String defaultValue) {
        FileInputStream fis = null;
        try {
            String filevalue;
            Properties props = new Properties();
            if (file.exists()) {
                fis = new FileInputStream(file);
                props.load(fis);
            }
            String string = (filevalue = props.getProperty(propertyid)) != null && filevalue.length() > 0 ? filevalue : defaultValue;
            return string;
        }
        catch (Exception e) {
            String string = "";
            return string;
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    public static DataSource getDataSource(Configuration configuration, String datasource) throws SapphireException {
        return ServiceLocator.getInstance(configuration.getJNDIDataSourcePrefix(), configuration.getJNDILocalEJBPrefix(), configuration.getJNDIEJBPrefix()).getDataSource(datasource);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void savePropertiesFile(Properties properties, File propertiesFile, String header) throws IOException {
        try (FileOutputStream fos = null;){
            fos = new FileOutputStream(propertiesFile);
            properties.store(fos, header);
        }
    }

    public static void jspLiveLog(JspWriter out, String message) throws IOException {
        BaseController.jspLiveLog(out, message, false);
    }

    public static void jspLiveLog(JspWriter out, String message, boolean webOnly) throws IOException {
        if (out != null) {
            out.println(message + "<br id=\"" + jspScrollCounter + "\"/><script>document.getElementById( \"" + jspScrollCounter + "\" ).scrollIntoView( true )</script>");
            out.flush();
            ++jspScrollCounter;
        } else if (!webOnly) {
            System.out.println(message.replaceAll("\\<[^>]*>", ""));
        }
        if (!webOnly) {
            Trace.logInfo(message);
        }
    }

    public static void jspLiveScript(JspWriter out, String script) throws IOException {
        if (out != null) {
            out.println(script);
            out.flush();
        }
    }
}

