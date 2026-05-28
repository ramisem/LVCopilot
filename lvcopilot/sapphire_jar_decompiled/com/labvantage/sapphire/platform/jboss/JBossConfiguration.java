/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  org.jboss.Version
 */
package com.labvantage.sapphire.platform.jboss;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.admin.install.PackagerFile;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.jboss.JBoss;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.ConsoleController;
import com.labvantage.sapphire.util.file.ConsoleFileOperationProgress;
import com.labvantage.sapphire.util.file.ZipFileUtil;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import javax.servlet.ServletContext;
import org.jboss.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class JBossConfiguration
extends Configuration
implements JBoss {
    protected static final String jbosshome = System.getenv("JBOSS_HOME");
    protected static final String jbosscp = System.getenv("JBOSS_CLASSPATH");
    protected static final String serverinstance = System.getProperty("jboss.server.name");
    protected static final String serverdir = System.getProperty("jboss.server.base.dir");
    protected static final String propertiesFilename = System.getProperty("com.labvantage.sapphire.server.jboss.properties-service", "properties-service.xml");
    protected static final String admindbdsFilename = System.getProperty("com.labvantage.sapphire.server.jboss.admindb-ds", "admindb-ds.xml");
    protected static final String rolesFilename = System.getProperty("com.labvantage.sapphire.server.jboss.sapphire-roles", "sapphire-roles.properties");
    protected static final String usersFilename = System.getProperty("com.labvantage.sapphire.server.jboss.sapphire-users", "sapphire-users.properties");
    protected static final String jmsFilename = System.getProperty("com.labvantage.sapphire.server.jboss.sapphire-jms-service", "sapphire-jms-service.xml");
    protected static final String oraPersistenceFilename = System.getProperty("com.labvantage.sapphire.server.jboss.oracle-persistence-service", "oracle-persistence-service.xml");
    protected static final String mssPersistenceFilename = System.getProperty("com.labvantage.sapphire.server.jboss.mssql-persistence-service", "mssql-persistence-service.xml");
    protected static final String destinationsFilename = System.getProperty("com.labvantage.sapphire.server.jboss.destinations-service", "destinations-service.xml");
    protected static final String messagingFilename = System.getProperty("com.labvantage.sapphire.server.jboss.messaging-service", "messaging-service.xml");
    protected static final String messagingJBossBeansFilename = System.getProperty("com.labvantage.sapphire.server.jboss.messaging-jboss-beans", "messaging-jboss-beans.xml");
    protected static final String webserverFilename = System.getProperty("com.labvantage.sapphire.server.jboss.server", "server.xml");
    protected static final String wardeployerFilename = System.getProperty("com.labvantage.sapphire.server.jboss.war-deployers-jboss-beans", "war-deployers-jboss-beans.xml");
    protected static final String jbossusername = System.getProperty("com.labvantage.sapphire.server.jboss.username", "jbossadmin");
    protected static final String jbosspassword = System.getProperty("com.labvantage.sapphire.server.jboss.password", "sapphire");
    protected static final String jbossrole = System.getProperty("com.labvantage.sapphire.server.jboss.role", "SapphireConnect");
    protected static final String authenticationPolicy = System.getProperty("com.labvantage.sapphire.server.jboss.authentication-policy", "Sapphire");
    protected static final String messagingSuckerPassword = System.getProperty("com.labvantage.sapphire.server.jboss.messagingsuckerpassword", "4439aa482ac7a35c01253e126eea49bb");
    protected static final String messagingJBossBeansSuckerPassword = System.getProperty("com.labvantage.sapphire.server.jboss.messagingjbossbeanssuckerpassword", "LABVANTAGE");
    protected static final String maxPostSize = System.getProperty("com.labvantage.sapphire.server.jboss.maxpostsize", "-1");
    private static final String oraDatasourceTemplate = System.getProperty("com.labvantage.sapphire.server.jboss.template.oracle-ds", "oracle-ds.xml");
    private static final String mssDatasourceTemplate = System.getProperty("com.labvantage.sapphire.server.jboss.template.mssql-ds", "mssql-ds.xml");

    @Override
    public String getHttpPort() throws SapphireException {
        try {
            return ConfigService.getConfigProperty("com.labvantage.sapphire.server.httpport", "8080");
        }
        catch (ServiceException e) {
            return "8080";
        }
    }

    @Override
    public String getDefaultProviderPort() {
        return "1099";
    }

    @Override
    public String getProviderPort() {
        try {
            return ConfigService.getConfigProperty("com.labvantage.sapphire.server.providerport", "1099");
        }
        catch (Exception e) {
            return this.getDefaultProviderPort();
        }
    }

    @Override
    public String getInitialContextFactory() {
        return "org.jnp.interfaces.NamingContextFactory";
    }

    @Override
    public String getProviderURL() throws SapphireException {
        return "jnp://" + this.getServerHostName() + ":" + this.getProviderPort();
    }

    @Override
    public void checkPlatformConfiguration(ArrayList errorList) throws SapphireException {
    }

    @Override
    public void applyPatches(ArrayList errorList) throws SapphireException {
    }

    @Override
    public String getAppRoot(ServletContext servletContext) {
        try {
            String path = servletContext.getResource("/").getFile();
            path = path.substring(0, path.lastIndexOf("/"));
            path = path.substring(0, path.lastIndexOf("/"));
            path = path.substring(0, path.lastIndexOf("/"));
            return path;
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public String getWebAppRoot(ServletContext servletContext) {
        try {
            return servletContext.getRealPath("/");
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getJNDIDataSourcePrefix() {
        return "java:";
    }

    @Override
    public String getJNDILocalEJBPrefix() {
        return "";
    }

    @Override
    public String getJCEProvider() {
        return "SunJCE";
    }

    @Override
    public String getServerVersion() {
        Version myversion = Version.getInstance();
        int jbossVersionMajor = myversion.getMajor();
        int jbossVersionMinor = myversion.getMinor();
        return String.valueOf(jbossVersionMajor) + "." + String.valueOf(jbossVersionMinor);
    }

    @Override
    public PropertyList getPlatformProperties() {
        File deploy = new File(serverdir, serverinstance + "/deploy");
        File deployers = new File(serverdir, serverinstance + "/deployers");
        File file = null;
        PropertyList platformProps = new PropertyList();
        PropertyListCollection errors = new PropertyListCollection();
        platformProps.setProperty("errors", errors);
        platformProps.setProperty("jbosshome", jbosshome);
        platformProps.setProperty("jbossclasspath", jbosscp);
        Version version = Version.getInstance();
        platformProps.setProperty("jbossversion", version.getMajor() + "." + version.getMinor() + "." + version.getRevision());
        boolean autofix = false;
        File templateDir = new File(this.getSapphireHome(), "/console/templates");
        File[] templates = templateDir.listFiles();
        if (templates != null) {
            for (int i = 0; i < templates.length; ++i) {
                File template = templates[i];
                if (!template.isDirectory() || !template.getName().equalsIgnoreCase("jboss" + version.getMajor() + version.getMinor() + (version.getRevision() > 0 ? Integer.valueOf(version.getRevision()) : ""))) continue;
                platformProps.setProperty("jbossconfiguration", "Y");
                autofix = true;
                break;
            }
        }
        if (autofix) {
            autofix = System.getProperty("com.labvantage.sapphire.server.jboss.customconfig", "false").equals("false");
        }
        platformProps.setProperty("serverinstance", serverinstance);
        platformProps.setProperty("clusterableinstance", new File(deploy, "/cluster").exists() && new File(deploy, "/management").exists() && new File(deploy, "/messaging").exists() ? "Y" : "N");
        String defaultDSDBMS = "ORA";
        DataSet datasources = new DataSet();
        PropertyList jndimap = new PropertyList();
        try {
            File[] files = deploy.listFiles();
            for (int i = 0; i < files.length; ++i) {
                if (!files[i].isFile() || !files[i].getName().endsWith("-ds.xml")) continue;
                int row = datasources.addRow();
                Document dom = DOMUtil.getNewDocument(files[i], false);
                int pos1 = files[i].getName().indexOf("-ds.xml");
                datasources.setString(row, "datasourceid", files[i].getName().substring(0, pos1));
                NodeList nodelist = dom.getElementsByTagName("connection-url");
                if (nodelist != null) {
                    String url = nodelist.item(0).getFirstChild().getNodeValue();
                    String[] parts = StringUtil.split(url, ":");
                    if (url.contains("oracle")) {
                        datasources.setString(row, "dbms", "ORA");
                        datasources.setString(row, "servername", parts[3].substring(1));
                        datasources.setString(row, "port", parts[4]);
                        datasources.setString(row, "sidorinstance", parts[5]);
                    } else if (url.contains("JSQLConnect")) {
                        datasources.setString(row, "dbms", "MSS");
                        String[] subParts = StringUtil.split(parts[2], "/");
                        datasources.setString(row, "servername", subParts[2]);
                        for (int j = 0; j < subParts.length; ++j) {
                            if (subParts[j] == null) continue;
                            if (subParts[j].startsWith("database=")) {
                                datasources.setString(row, "sqldatabase", subParts[j].substring(9));
                                continue;
                            }
                            if (subParts[j].startsWith("portNumber=")) {
                                datasources.setString(row, "port", subParts[j].substring(11));
                                continue;
                            }
                            if (!subParts[j].startsWith("instanceName=")) continue;
                            datasources.setString(row, "sidorinstance", subParts[j].substring(13));
                        }
                    }
                }
                if ((nodelist = dom.getElementsByTagName("jndi-name")) != null) {
                    datasources.setString(row, "jndiname", nodelist.item(0).getFirstChild().getNodeValue());
                    jndimap.setProperty(nodelist.item(0).getFirstChild().getNodeValue(), String.valueOf(row));
                }
                if ((nodelist = dom.getElementsByTagName("user-name")) != null) {
                    datasources.setString(row, "username", nodelist.item(0).getFirstChild().getNodeValue());
                }
                if ((nodelist = dom.getElementsByTagName("password")) != null) {
                    datasources.setString(row, "password", nodelist.item(0).getFirstChild().getNodeValue());
                }
                if ((nodelist = dom.getElementsByTagName("min-pool-size")) != null) {
                    datasources.setString(row, "minpoolsize", nodelist.item(0).getFirstChild().getNodeValue());
                }
                if ((nodelist = dom.getElementsByTagName("max-pool-size")) != null) {
                    datasources.setString(row, "maxpoolsize", nodelist.item(0).getFirstChild().getNodeValue());
                }
                if ((nodelist = dom.getElementsByTagName("blocking-timeout-millis")) != null) {
                    datasources.setString(row, "blockingtimeout", nodelist.item(0).getFirstChild().getNodeValue());
                }
                if ((nodelist = dom.getElementsByTagName("idle-timeout-minutes")) == null) continue;
                datasources.setString(row, "idletimeout", nodelist.item(0).getFirstChild().getNodeValue());
            }
        }
        catch (Exception files) {
            // empty catch block
        }
        platformProps.setProperty("datasources", JSONUtil.toJSONString(datasources));
        platformProps.setProperty("jndimap", jndimap);
        String sapphireHome = this.getSapphireHome();
        if (sapphireHome != null && sapphireHome.length() > 0 && new File(this.getSapphireHome()).exists()) {
            if (System.getProperty("com.labvantage.sapphire.server.ignoreplatformcheck.admindb", "false").equals("false")) {
                try {
                    file = new File(deploy, admindbdsFilename);
                    if (!file.exists()) {
                        PropertyList error = new PropertyList();
                        error.setProperty("error", "Admindb datasource file '" + file.getAbsolutePath() + "' missing!");
                        error.setProperty("desc", "A datasource deployment file (" + admindbdsFilename + ") defining the connection to the admin database is required.");
                        error.setProperty("fix", "Create a datasource deployment file (" + admindbdsFilename + ") in '" + file.getParentFile().getAbsolutePath() + "'.");
                        error.setProperty("fixcode", autofix ? "ADS:" + admindbdsFilename.substring(0, admindbdsFilename.indexOf("-ds")) : "");
                        error.setProperty("type", "E");
                        errors.add(error);
                    }
                }
                catch (Exception e) {
                    PropertyList error = new PropertyList();
                    error.setProperty("error", "Failed to open '" + file.getAbsolutePath() + "'");
                    errors.add(error);
                }
            }
            if (System.getProperty("com.labvantage.sapphire.server.ignoreplatformcheck.tomcatutf8", "false").equals("false")) {
                try {
                    file = new File(deploy, "jbossweb.sar/" + webserverFilename);
                    Document dom = DOMUtil.getNewDocument(file, false);
                    NodeList nodelist = dom.getElementsByTagName("Connector");
                    if (nodelist != null) {
                        for (int i = 0; i < nodelist.getLength(); ++i) {
                            String value;
                            if (!nodelist.item(i).getAttributes().getNamedItem("protocol").getNodeValue().equals("HTTP/1.1")) continue;
                            String string = value = nodelist.item(i).getAttributes().getNamedItem("URIEncoding") != null ? nodelist.item(i).getAttributes().getNamedItem("URIEncoding").getNodeValue() : "";
                            if (platformProps.containsKey("uriencoding")) {
                                platformProps.setProperty("uriencoding", platformProps.getProperty("uriencoding").length() > 0 && value.length() > 0 ? value : "");
                                continue;
                            }
                            platformProps.setProperty("uriencoding", value);
                        }
                    }
                    if (!platformProps.containsKey("uriencoding") || !platformProps.getProperty("uriencoding").equalsIgnoreCase("UTF-8")) {
                        PropertyList error = new PropertyList();
                        error.setProperty("error", "Tomcat URI encoding not set to UTF-8!");
                        error.setProperty("desc", "To decode URLs properly from the browser the URI encoding must set to UTF-8 in '" + file.getAbsolutePath() + "'.");
                        error.setProperty("fix", "Set the URIEncoding attribute for the HTTP/1.1 connector to UTF-8 in file (" + webserverFilename + ") in '" + file.getParentFile().getAbsolutePath() + "'.");
                        error.setProperty("fixcode", autofix ? "DURIE:" + webserverFilename : "");
                        error.setProperty("type", "E");
                        errors.add(error);
                    }
                }
                catch (Exception e) {
                    PropertyList error = new PropertyList();
                    error.setProperty("error", "Failed to open '" + file.getAbsolutePath() + "'");
                    errors.add(error);
                }
            }
            if (System.getProperty("com.labvantage.sapphire.server.ignoreplatformcheck.maxpostsize", "false").equals("false")) {
                try {
                    file = new File(deploy, "jbossweb.sar/" + webserverFilename);
                    Document dom = DOMUtil.getNewDocument(file, false);
                    NodeList nodelist = dom.getElementsByTagName("Connector");
                    if (nodelist != null) {
                        for (int i = 0; i < nodelist.getLength(); ++i) {
                            String value;
                            if (!nodelist.item(i).getAttributes().getNamedItem("protocol").getNodeValue().equals("HTTP/1.1")) continue;
                            String string = value = nodelist.item(i).getAttributes().getNamedItem("maxPostSize") != null ? nodelist.item(i).getAttributes().getNamedItem("maxPostSize").getNodeValue() : "";
                            if (platformProps.containsKey("maxpostsize")) {
                                platformProps.setProperty("maxpostsize", platformProps.getProperty("maxpostsize").length() > 0 && value.length() > 0 ? value : "");
                                continue;
                            }
                            platformProps.setProperty("maxpostsize", value);
                        }
                    }
                    if (!platformProps.containsKey("maxpostsize") || !platformProps.getProperty("maxpostsize").equalsIgnoreCase(maxPostSize)) {
                        PropertyList error = new PropertyList();
                        error.setProperty("error", "Max Post Size not set to " + maxPostSize + "!");
                        error.setProperty("desc", "To post large forms from the browser the maxPostSize must set to " + maxPostSize + " in '" + file.getAbsolutePath() + "'.");
                        error.setProperty("fix", "Set the maxPostSize attribute for the HTTP/1.1 connector to " + maxPostSize + " in file (" + webserverFilename + ") in '" + file.getParentFile().getAbsolutePath() + "'.");
                        error.setProperty("fixcode", autofix ? "DMPS:" + webserverFilename : "");
                        error.setProperty("type", "E");
                        errors.add(error);
                    }
                }
                catch (Exception e) {
                    PropertyList error = new PropertyList();
                    error.setProperty("error", "Failed to open '" + file.getAbsolutePath() + "'");
                    errors.add(error);
                }
            }
        } else {
            PropertyList error = new PropertyList();
            error.setProperty("error", ConsoleController.PRODUCT_HOME + " (" + this.getSapphireHome() + ") invalid or not defined!");
            error.setProperty("desc", "LabVantage Console needs the " + ConsoleController.PRODUCT_HOME + " directory to be defined before further configuration checks/fixes can be performed.");
            error.setProperty("fix", "Define " + ConsoleController.PRODUCT_HOME + " by assigning the directory to one of the following: 1. A " + ConsoleController.PRODUCT_HOME + " environment variable 2. A " + ConsoleController.PRODUCT_HOME + " JVM property 3. A " + ConsoleController.PRODUCT_HOME + " context parameter in web.xml.");
            error.setProperty("fixcode", autofix ? "DLVH" : "");
            error.setProperty("type", "E");
            errors.add(error);
        }
        return platformProps;
    }

    @Override
    public boolean executePlatformAction(PropertyList actionProps) {
        String action = actionProps.getProperty("action");
        if (action.equals("fixall")) {
            PropertyListCollection fixes = actionProps.getCollection("fixes");
            for (int i = 0; i < fixes.size(); ++i) {
                PropertyList fix = fixes.getPropertyList(i);
                this.executePlatformAction(fix);
            }
            return true;
        }
        if (action.equals("definelvh")) {
            return this.defineLVHome(actionProps);
        }
        if (action.equals("adddatasource")) {
            return this.createDatasource(actionProps, false);
        }
        if (action.equals("editdatasource")) {
            return this.createDatasource(actionProps, true);
        }
        if (action.equals("deletedatasource")) {
            return this.deleteDatasource(actionProps);
        }
        if (action.equals("addroles")) {
            return this.addRoles(actionProps);
        }
        if (action.equals("addusers")) {
            return this.addUsers(actionProps);
        }
        if (action.equals("addauthenticationpolicy")) {
            return this.addAuthenticationPolicy(actionProps);
        }
        if (action.equals("correctauthenticationpolicy")) {
            return this.correctAuthenticationPolicy(actionProps);
        }
        if (action.equals("definetomcaturiencoding")) {
            return this.defineTomcatURIEncoding(actionProps);
        }
        if (action.equals("definemaxpostsize")) {
            return this.defineMaxPostSize(actionProps);
        }
        if (action.equals("makeclusterinstance")) {
            return this.makeClusterInstance(actionProps);
        }
        return false;
    }

    private boolean makeClusterInstance(PropertyList actionProps) {
        try {
            int pos;
            String serverPeerInt = actionProps.getProperty("serverpeerid", "0");
            File file = new File(JBossConfiguration.getInstanceDir(), "deploy/jbossweb.sar/" + webserverFilename);
            String fileContents = FileUtil.getFileString(file);
            Document dom = DOMUtil.getNewDocument(file, false);
            NodeList nodelist = dom.getElementsByTagName("Engine");
            String value = "";
            boolean exists = false;
            if (nodelist != null) {
                for (int i = 0; i < nodelist.getLength(); ++i) {
                    if (!nodelist.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("jboss.web")) continue;
                    exists = true;
                    value = nodelist.item(i).getAttributes().getNamedItem("jvmRoute") != null ? nodelist.item(i).getAttributes().getNamedItem("jvmRoute").getNodeValue() : "";
                }
            }
            int start = fileContents.indexOf("<Engine ");
            int end = fileContents.indexOf(">", start + 1);
            if (start > -1 && end > -1) {
                pos = fileContents.substring(start, end).indexOf("jvmRoute=");
                if (pos > -1) {
                    if (!value.equals("node" + serverPeerInt)) {
                        pos = fileContents.indexOf("jvmRoute=\"");
                        int pos2 = fileContents.indexOf("\"", pos + 10);
                        fileContents = fileContents.substring(0, pos + 10) + "node" + serverPeerInt + fileContents.substring(pos2);
                    }
                } else {
                    fileContents = fileContents.substring(0, end) + " jvmRoute=\"node" + serverPeerInt + "\"" + fileContents.substring(end);
                }
                FileUtil.writeFileString(file, fileContents);
            }
            if ((pos = (fileContents = FileUtil.getFileString(file = new File(JBossConfiguration.getInstanceDir(), "deploy/" + propertiesFilename))).indexOf("<attribute name=\"Properties\">")) > -1) {
                fileContents = fileContents.substring(0, pos + 29) + "\n        " + "LABVANTAGE_SERVER" + "=" + serverPeerInt + fileContents.substring(pos + 29);
                FileUtil.writeFileString(file, fileContents);
            }
        }
        catch (Exception e) {
            actionProps.setProperty("status", "Failed to make clustered instance. Reason: " + e.getMessage());
        }
        return false;
    }

    private boolean defineLVHome(PropertyList actionProps) {
        try {
            File file = new File(JBossConfiguration.getInstanceDir(), "deploy/" + propertiesFilename);
            String fileContents = FileUtil.getFileString(file);
            Document dom = DOMUtil.getNewDocument(file, false);
            NodeList nodelist = dom.getElementsByTagName("attribute");
            boolean exists = false;
            if (nodelist != null) {
                for (int i = 0; !exists && i < nodelist.getLength(); ++i) {
                    if (!nodelist.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("Properties")) continue;
                    exists = true;
                }
            }
            int pos = fileContents.indexOf("<attribute name=\"Properties\">");
            if (!exists && pos > -1) {
                int pos2 = fileContents.indexOf("-->", pos + 1);
                fileContents = fileContents.substring(0, pos) + "-->\n    <attribute name=\"Properties\">\n        LABVANTAGE_HOME=" + StringUtil.replaceAll(actionProps.getProperty("lvh"), "\\", "/") + "\n    </attribute>" + fileContents.substring(pos2 + 3);
            } else if (!exists && pos == -1) {
                pos = fileContents.indexOf("</mbean");
                fileContents = fileContents.substring(0, pos) + "   <attribute name=\"Properties\">\n      LABVANTAGE_HOME=" + StringUtil.replaceAll(actionProps.getProperty("lvh"), "\\", "/") + "\n    </attribute>\n   " + fileContents.substring(pos);
            } else {
                int pos2 = fileContents.indexOf("</attribute>", pos + 1);
                fileContents = fileContents.substring(0, pos + 29) + "LABVANTAGE_HOME=" + StringUtil.replaceAll(actionProps.getProperty("lvh"), "\\", "/") + fileContents.substring(pos2);
            }
            FileUtil.writeFileString(file, fileContents);
        }
        catch (Exception e) {
            actionProps.setProperty("status", "Failed to define LV Home. Reason: " + e.getMessage());
        }
        return false;
    }

    private boolean defineTomcatURIEncoding(PropertyList actionProps) {
        try {
            File file = new File(JBossConfiguration.getInstanceDir(), "deploy/jbossweb.sar/" + webserverFilename);
            String fileContents = FileUtil.getFileString(file);
            int start = fileContents.indexOf("<Connector ");
            while (start > -1) {
                int pos;
                int end = fileContents.indexOf("/>", start + 1);
                if (fileContents.substring(start, end).indexOf("protocol=\"HTTP/1.1\"") > -1 && (pos = fileContents.substring(start, end).indexOf("URIEncoding=")) <= -1) {
                    fileContents = fileContents.substring(0, end) + " URIEncoding=\"UTF-8\"" + fileContents.substring(end);
                }
                start = fileContents.indexOf("<Connector ", end);
            }
            FileUtil.writeFileString(file, fileContents);
        }
        catch (Exception e) {
            actionProps.setProperty("status", "Failed to modify tomcat file '" + webserverFilename + "'. Reason: " + e.getMessage());
        }
        return false;
    }

    private boolean defineMaxPostSize(PropertyList actionProps) {
        try {
            File file = new File(JBossConfiguration.getInstanceDir(), "deploy/jbossweb.sar/" + webserverFilename);
            String fileContents = FileUtil.getFileString(file);
            int start = fileContents.indexOf("<Connector ");
            while (start > -1) {
                int pos;
                int end = fileContents.indexOf("/>", start + 1);
                if (fileContents.substring(start, end).indexOf("protocol=\"HTTP/1.1\"") > -1 && (pos = fileContents.substring(start, end).indexOf("maxPostSize=")) <= -1) {
                    fileContents = fileContents.substring(0, end) + " maxPostSize=\"" + maxPostSize + "\"" + fileContents.substring(end);
                }
                start = fileContents.indexOf("<Connector ", end);
            }
            FileUtil.writeFileString(file, fileContents);
        }
        catch (Exception e) {
            actionProps.setProperty("status", "Failed to modify tomcat file '" + webserverFilename + "'. Reason: " + e.getMessage());
        }
        return false;
    }

    private boolean correctAuthenticationPolicy(PropertyList actionProps) {
        try {
            File file = new File(JBossConfiguration.getInstanceDir(), "conf/login-config.xml");
            Document dom = DOMUtil.getNewDocument(file, false);
            NodeList nodelist = dom.getElementsByTagName("application-policy");
            if (nodelist != null) {
                for (int i = 0; i < nodelist.getLength(); ++i) {
                    if (!nodelist.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(authenticationPolicy)) continue;
                    nodelist.item(i).getParentNode().removeChild(nodelist.item(i));
                }
            }
            DOMUtil.save(dom, file);
            this.addAuthenticationPolicy(actionProps);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private boolean addAuthenticationPolicy(PropertyList actionProps) {
        try {
            File file = new File(JBossConfiguration.getInstanceDir(), "conf/login-config.xml");
            Document dom = DOMUtil.getNewDocument(file, false);
            NodeList nodelist = dom.getElementsByTagName("policy");
            if (nodelist != null) {
                Element applicationpolicyNode = dom.createElement("application-policy");
                applicationpolicyNode.getAttributes().setNamedItem(dom.createAttribute("name"));
                applicationpolicyNode.getAttributes().getNamedItem("name").setNodeValue(authenticationPolicy);
                nodelist.item(0).appendChild(applicationpolicyNode);
                Element authenticationNode = dom.createElement("authentication");
                applicationpolicyNode.appendChild(authenticationNode);
                Element loginmoduleNode = dom.createElement("login-module");
                loginmoduleNode.getAttributes().setNamedItem(dom.createAttribute("code"));
                loginmoduleNode.getAttributes().getNamedItem("code").setNodeValue("org.jboss.security.auth.spi.UsersRolesLoginModule");
                loginmoduleNode.getAttributes().setNamedItem(dom.createAttribute("flag"));
                loginmoduleNode.getAttributes().getNamedItem("flag").setNodeValue("required");
                authenticationNode.appendChild(loginmoduleNode);
                Element moduleoptionNodeUsers = dom.createElement("module-option");
                moduleoptionNodeUsers.getAttributes().setNamedItem(dom.createAttribute("name"));
                moduleoptionNodeUsers.getAttributes().getNamedItem("name").setNodeValue("usersProperties");
                moduleoptionNodeUsers.appendChild(dom.createTextNode("props/" + usersFilename));
                loginmoduleNode.appendChild(moduleoptionNodeUsers);
                Element moduleoptionNodeRoles = dom.createElement("module-option");
                moduleoptionNodeRoles.getAttributes().setNamedItem(dom.createAttribute("name"));
                moduleoptionNodeRoles.getAttributes().getNamedItem("name").setNodeValue("rolesProperties");
                moduleoptionNodeRoles.appendChild(dom.createTextNode("props/" + rolesFilename));
                loginmoduleNode.appendChild(moduleoptionNodeRoles);
                DOMUtil.save(dom, file);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private boolean addUsers(PropertyList actionProps) {
        try {
            File file = new File(JBossConfiguration.getInstanceDir(), "conf/props/" + usersFilename);
            FileUtil.writeFileString(file, jbossusername + "=" + jbosspassword);
        }
        catch (Exception e) {
            actionProps.setProperty("status", "Failed to add roles. Reason: " + e.getMessage());
        }
        return true;
    }

    private boolean addRoles(PropertyList actionProps) {
        try {
            File file = new File(JBossConfiguration.getInstanceDir(), "conf/props/" + rolesFilename);
            FileUtil.writeFileString(file, jbossusername + "=" + jbossrole);
        }
        catch (Exception e) {
            actionProps.setProperty("status", "Failed to add roles. Reason: " + e.getMessage());
        }
        return true;
    }

    private boolean createDatasource(PropertyList datasourceProps, boolean blockOverwrite) {
        String datasourceid = datasourceProps.getProperty("datasourceid");
        boolean added = false;
        try {
            File ds = new File(JBossConfiguration.getInstanceDir(), "deploy/" + datasourceid + "-ds.xml");
            if (!ds.exists() || ds.exists() && blockOverwrite) {
                File template = new File(this.getTemplateDir(), datasourceProps.getProperty("dbms", "ORA").equals("ORA") ? oraDatasourceTemplate : mssDatasourceTemplate);
                String fileContents = FileUtil.getFileString(template);
                fileContents = StringUtil.replaceAll(fileContents, "${jndiname}", datasourceProps.getProperty("jndiname"));
                fileContents = StringUtil.replaceAll(fileContents, "${servername}", datasourceProps.getProperty("servername"));
                fileContents = StringUtil.replaceAll(fileContents, "${port}", datasourceProps.getProperty("port", datasourceProps.getProperty("dbms", "ORA").equals("ORA") ? "1521" : "1433"));
                fileContents = StringUtil.replaceAll(fileContents, "${sid}", datasourceProps.getProperty("sidorinstance"));
                fileContents = StringUtil.replaceAll(fileContents, "${instancename}", datasourceProps.getProperty("sidorinstance"));
                fileContents = StringUtil.replaceAll(fileContents, "${sqldatabase}", datasourceProps.getProperty("sqldatabase"));
                fileContents = StringUtil.replaceAll(fileContents, "${username}", datasourceProps.getProperty("username"));
                fileContents = StringUtil.replaceAll(fileContents, "${password}", datasourceProps.getProperty("password"));
                fileContents = StringUtil.replaceAll(fileContents, "${minpoolsize}", datasourceProps.getProperty("minpoolsize", "0"));
                fileContents = StringUtil.replaceAll(fileContents, "${maxpoolsize}", datasourceProps.getProperty("maxpoolsize", "30"));
                fileContents = StringUtil.replaceAll(fileContents, "${blockingtimeout}", datasourceProps.getProperty("blockingtimeout", "5000"));
                fileContents = StringUtil.replaceAll(fileContents, "${idletimeout}", datasourceProps.getProperty("idletimeout", "15"));
                FileUtil.writeFileString(ds, fileContents);
                added = true;
            } else {
                datasourceProps.setProperty("status", "Datasource '" + datasourceid + "' already exists!");
            }
        }
        catch (Exception e) {
            datasourceProps.setProperty("status", "Failed to create datasource '" + datasourceid + "'. Reason: " + e.getMessage());
        }
        return added;
    }

    private boolean deleteDatasource(PropertyList datasourceProps) {
        String datasourceid = datasourceProps.getProperty("datasourceid");
        try {
            File ds = new File(JBossConfiguration.getInstanceDir(), "deploy/" + datasourceid + "-ds.xml");
            if (ds.exists()) {
                ds.delete();
            } else {
                datasourceProps.setProperty("status", "Datasource '" + datasourceid + "' not found!");
            }
        }
        catch (Exception e) {
            datasourceProps.setProperty("status", "Failed to delete datasource '" + datasourceid + "'. Reason: " + e.getMessage());
        }
        return true;
    }

    public static File getInstanceDir() {
        return new File(serverdir, serverinstance);
    }

    @Override
    public File getDeploymentDir() {
        return new File(serverdir, serverinstance + "/deploy");
    }

    @Override
    public void getExplodedApps(DataSet sources) {
        File[] files = this.getDeploymentDir().listFiles();
        block0: for (int i = 0; i < files.length; ++i) {
            File[] subfiles;
            if (!files[i].isDirectory() || !files[i].getName().endsWith(".ear") || (subfiles = new File(files[i], "META-INF").listFiles()) == null) continue;
            for (int j = 0; j < subfiles.length; ++j) {
                if (!subfiles[j].getName().equals("application-build.props")) continue;
                int row = sources.addRow();
                sources.setString(row, "sourcename", files[i].getName() + " (exploded in JBoss)");
                sources.setString(row, "sourcevalue", files[i].getName());
                continue block0;
            }
        }
    }

    @Override
    public void deployEAR(File appHome, String type, File managedEAR, boolean overwrite, ConsoleFileOperationProgress progress) throws IOException, SapphireException {
        File appEAR;
        File deployEAR = new File(this.getDeploymentDir(), managedEAR.getName());
        if (deployEAR.exists() && !overwrite && deployEAR.isDirectory()) {
            throw new SapphireException(managedEAR.getName() + " already exists as an exploded EAR in the deploy directory!");
        }
        if (deployEAR.exists() && !overwrite && deployEAR.isFile()) {
            throw new SapphireException(managedEAR.getName() + " already exists as an EAR in the deploy directory!");
        }
        File file = appEAR = type.equals("P") ? new File(appHome, "/ear/" + managedEAR.getName()) : new File(appHome, "/earx/" + managedEAR.getName());
        if (!appEAR.exists()) {
            throw new SapphireException(appEAR.getAbsolutePath() + " does not exist!");
        }
        progress.startProgress(" - copying " + appEAR.getAbsolutePath() + " to " + deployEAR.getAbsolutePath(), appEAR);
        if (appEAR.isFile()) {
            FileUtil.copyFile(appEAR, deployEAR);
        } else {
            FileUtil.copyAll(appEAR, deployEAR, progress);
        }
        progress.endProgress();
    }

    @Override
    public void undeployEAR(File appHome, String type, File managedEAR, ConsoleFileOperationProgress progress) throws IOException, SapphireException {
        File undeployEAR = new File(JBossConfiguration.getInstanceDir(), "/deploy/" + managedEAR.getName());
        if (!undeployEAR.exists()) {
            throw new SapphireException(undeployEAR + " not found!");
        }
        if (type.equals("P")) {
            progress.startProgress(" - deleting " + undeployEAR.getAbsolutePath() + "...");
            FileUtil.deleteAll(undeployEAR);
            progress.endProgress();
            progress.startProgress("<b>Master EAR at " + new File(appHome, "ear/" + managedEAR.getName()).getAbsolutePath() + "</b>");
            progress.endProgress();
        } else {
            File undeployTo = new File(appHome, "/earx/" + managedEAR.getName());
            if (undeployTo.exists()) {
                progress.startProgress(" - deleting " + undeployTo.getAbsolutePath(), undeployTo);
                FileUtil.deleteAll(undeployTo, progress, true);
                progress.endProgress();
            }
            undeployTo.mkdirs();
            progress.startProgress(" - zipping " + undeployEAR.getAbsolutePath() + " to " + undeployTo.getAbsolutePath());
            PackagerFile zip = new PackagerFile(new File(undeployTo, "exploded.zip"));
            zip.addFile(undeployEAR, undeployEAR);
            zip.save(progress);
            progress.endProgress();
            progress.startProgress(" - deleting " + undeployEAR.getAbsolutePath(), undeployEAR);
            FileUtil.deleteAll(undeployEAR, progress, true);
            progress.endProgress();
        }
    }

    @Override
    public boolean isDeployed(String managedEAR) {
        return new File(this.getDeploymentDir(), managedEAR).exists();
    }

    @Override
    public HashMap getDeployedApplications(File temp) {
        File[] deployFiles;
        HashMap<String, String> applications = new HashMap<String, String>();
        for (File file : deployFiles = this.getDeploymentDir().listFiles()) {
            File appBuildProps = null;
            try {
                Properties appProps;
                String applicationid;
                boolean exploded = false;
                if (file.isDirectory() && file.getName().toLowerCase().endsWith(".ear")) {
                    appBuildProps = new File(file, "META-INF/application-build.props");
                    exploded = true;
                } else if (file.isFile() && file.getName().toLowerCase().endsWith(".ear")) {
                    appBuildProps = ZipFileUtil.extractFile(file, "META-INF/application-build.props", temp);
                }
                if (!appBuildProps.exists() || (applicationid = (appProps = ConsoleController.loadPropertiesFile(appBuildProps)).getProperty("application.id")).length() <= 0) continue;
                applications.put(applicationid, file.getName());
                applications.put(applicationid + "_exploded", exploded ? "Y" : "N");
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return applications;
    }

    public void explodeApplication(File labVantageEAR, String applicationid) throws SapphireException {
        File earDir = new File(JBossConfiguration.getInstanceDir(), "/deploy/" + applicationid + ".ear");
        if (earDir.exists()) {
            throw new SapphireException("LabVantage EAR cannot be exploded as the application directory '" + earDir.getAbsolutePath() + "' already exists!");
        }
        earDir.mkdirs();
        ZipFileUtil.extractAll(labVantageEAR, earDir);
        File sapphireJAR = new File(earDir, "sapphire.jar");
        File sapphireJARTemp = new File(earDir, "sapphire.jar_temp");
        FileUtil.renameTo(sapphireJAR, sapphireJARTemp);
        ZipFileUtil.extractAll(sapphireJARTemp, sapphireJAR);
        sapphireJARTemp.delete();
        File sapphireWAR = new File(earDir, "labvantage.war");
        File sapphireWARTemp = new File(earDir, "labvantage.war_temp");
        FileUtil.renameTo(sapphireWAR, sapphireWARTemp);
        ZipFileUtil.extractAll(sapphireWARTemp, sapphireWAR);
        sapphireWARTemp.delete();
    }

    @Override
    public void deleteApplicationDir(String managedEAR, ConsoleFileOperationProgress progress) throws IOException {
        File earDir = new File(JBossConfiguration.getInstanceDir(), "/deploy/" + managedEAR);
        progress.startProgress(FileUtil.fileCount(earDir));
        FileUtil.deleteAll(earDir, progress, true);
        progress.endProgress();
    }

    @Override
    public void getApplicationUpgradeFiles(String managedEAR, PropertyList upgradeProps, Properties appBuildProps) {
        File earDir = new File(this.getDeploymentDir(), managedEAR);
        File cvs = new File(earDir, "META-INF/CVS");
        upgradeProps.setProperty("appddfiles", cvs.exists() && cvs.isDirectory() ? "N" : "Y");
        cvs = new File(earDir, "sapphire.jar/META-INF/CVS");
        upgradeProps.setProperty("ejbddfiles", cvs.exists() && cvs.isDirectory() ? "N" : "Y");
        cvs = new File(earDir, appBuildProps.getProperty("application.war", "labvantage.war") + "/WEB-INF/CVS");
        upgradeProps.setProperty("webddfiles", cvs.exists() && cvs.isDirectory() ? "N" : "Y");
        cvs = new File(earDir, "sapphire.jar/CVS");
        upgradeProps.setProperty("sapphirejarfiles", cvs.exists() && cvs.isDirectory() ? "N" : "Y");
        cvs = new File(earDir, appBuildProps.getProperty("application.war", "labvantage.war") + "/CVS");
        upgradeProps.setProperty("sapphirewarfiles", cvs.exists() && cvs.isDirectory() ? "N" : "Y");
        cvs = new File(earDir, "CVS");
        upgradeProps.setProperty("otherjarfiles", cvs.exists() && cvs.isDirectory() ? "N" : "Y");
    }

    protected File getTemplateDir() {
        Version version = Version.getInstance();
        return new File(this.getSapphireHome(), "/console/templates/jboss" + version.getMajor() + version.getMinor() + (version.getRevision() > 0 ? Integer.valueOf(version.getRevision()) : ""));
    }
}

