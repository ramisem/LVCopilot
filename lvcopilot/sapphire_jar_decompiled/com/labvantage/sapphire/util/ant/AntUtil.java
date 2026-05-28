/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildListener
 *  org.apache.tools.ant.DefaultLogger
 *  org.apache.tools.ant.Project
 *  org.apache.tools.ant.ProjectHelper
 */
package com.labvantage.sapphire.util.ant;

import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import com.labvantage.sapphire.xml.ant.ApplicationNameChangeTask;
import com.labvantage.sapphire.xml.ant.BuildOptionsTask;
import com.labvantage.sapphire.xml.ant.ColumnTask;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import com.labvantage.sapphire.xml.ant.DataTask;
import com.labvantage.sapphire.xml.ant.EjbJndiNameChangeTask;
import com.labvantage.sapphire.xml.ant.ExecuteSQLTask;
import com.labvantage.sapphire.xml.ant.ExportXMLTask;
import com.labvantage.sapphire.xml.ant.GetJarListTask;
import com.labvantage.sapphire.xml.ant.ImageToNVarcharmax;
import com.labvantage.sapphire.xml.ant.ImportXMLTask;
import com.labvantage.sapphire.xml.ant.InstallerFileTask;
import com.labvantage.sapphire.xml.ant.InstallerPackageTask;
import com.labvantage.sapphire.xml.ant.InstallerTask;
import com.labvantage.sapphire.xml.ant.JasperReportTask;
import com.labvantage.sapphire.xml.ant.NodeTask;
import com.labvantage.sapphire.xml.ant.PropertiesFileEntryTask;
import com.labvantage.sapphire.xml.ant.PropertiesFileTask;
import com.labvantage.sapphire.xml.ant.PropertyListTask;
import com.labvantage.sapphire.xml.ant.PropertyTask;
import com.labvantage.sapphire.xml.ant.PropertyTreeTask;
import com.labvantage.sapphire.xml.ant.QueueNameChangeTask;
import com.labvantage.sapphire.xml.ant.SDCTask;
import com.labvantage.sapphire.xml.ant.SDITask;
import com.labvantage.sapphire.xml.ant.SQLStmtTask;
import com.labvantage.sapphire.xml.ant.SapphireHomeChangeTask;
import com.labvantage.sapphire.xml.ant.ServerTask;
import com.labvantage.sapphire.xml.ant.SyncDDTTask;
import com.labvantage.sapphire.xml.ant.TableTask;
import com.labvantage.sapphire.xml.ant.TransferPackageTask;
import com.labvantage.sapphire.xml.ant.UpgradeTask;
import com.labvantage.sapphire.xml.ant.WARNameChangeTask;
import com.labvantage.sapphire.xml.ant.WebContextChangeTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;

public class AntUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 75721 $";

    public static boolean isRunAntPermitted(String connectionId) {
        QueryProcessor qp = new QueryProcessor(connectionId);
        DataSet ds = qp.getSqlDataSet("SELECT propertyvalue FROM sysconfig WHERE propertyid = 'allowrunant'");
        if (ds != null && ds.getRowCount() > 0 && ds.getValue(0, "propertyvalue", "Y").equalsIgnoreCase("N")) {
            return false;
        }
        return SecurityPolicyUtil.isRunAntPermitted(connectionId);
    }

    public static void runFile(int platform, File antFile, File logFile, String target, HashMap antParams, BuildListener listener) throws SapphireException {
        FileOutputStream fos = null;
        PrintStream ps = null;
        try {
            Project antProject = new Project();
            if (logFile != null) {
                fos = new FileOutputStream(logFile);
                ps = new PrintStream(fos);
                DefaultLogger log = new DefaultLogger();
                log.setErrorPrintStream(ps);
                log.setOutputPrintStream(ps);
                log.setMessageOutputLevel(2);
                antProject.addBuildListener((BuildListener)log);
            }
            if (listener != null) {
                antProject.addBuildListener(listener);
            }
            antProject.setUserProperty("ant.file", antFile.getAbsolutePath());
            antProject.setUserProperty("sapphire.server.platform", Configuration.getPlatformName(platform));
            for (String param : antParams.keySet()) {
                if (antParams.get(param) == null) continue;
                antProject.setUserProperty(param, (String)antParams.get(param));
            }
            antProject.addTaskDefinition("installer", InstallerTask.class);
            antProject.addTaskDefinition("connection", ConnectionTask.class);
            antProject.addTaskDefinition("server", ServerTask.class);
            antProject.addTaskDefinition("sqlstmt", SQLStmtTask.class);
            antProject.addTaskDefinition("executesql", ExecuteSQLTask.class);
            antProject.addTaskDefinition("importXML", ImportXMLTask.class);
            antProject.addTaskDefinition("exportXML", ExportXMLTask.class);
            antProject.addTaskDefinition("upgrade", UpgradeTask.class);
            antProject.addTaskDefinition("syncddt", SyncDDTTask.class);
            antProject.addTaskDefinition("table", TableTask.class);
            antProject.addTaskDefinition("sdc", SDCTask.class);
            antProject.addTaskDefinition("sdi", SDITask.class);
            antProject.addTaskDefinition("propertytree", PropertyTreeTask.class);
            antProject.addTaskDefinition("propertylist", PropertyListTask.class);
            antProject.addTaskDefinition("property", PropertyTask.class);
            antProject.addTaskDefinition("node", NodeTask.class);
            antProject.addTaskDefinition("column", ColumnTask.class);
            antProject.addTaskDefinition("data", DataTask.class);
            antProject.addTaskDefinition("transferpackage", TransferPackageTask.class);
            antProject.addTaskDefinition("jasperreport", JasperReportTask.class);
            antProject.addTaskDefinition("imagetonvarcharmax", ImageToNVarcharmax.class);
            antProject.addTaskDefinition("getJarList", GetJarListTask.class);
            antProject.addTaskDefinition("changeEjbJndiName", EjbJndiNameChangeTask.class);
            antProject.addTaskDefinition("changeQueueName", QueueNameChangeTask.class);
            antProject.addTaskDefinition("changeSapphireHome", SapphireHomeChangeTask.class);
            antProject.addTaskDefinition("changeApplicationName", ApplicationNameChangeTask.class);
            antProject.addTaskDefinition("changeWARName", WARNameChangeTask.class);
            antProject.addTaskDefinition("changeWebContextName", WebContextChangeTask.class);
            antProject.addTaskDefinition("propertiesfile", PropertiesFileTask.class);
            antProject.addTaskDefinition("propertiesentry", PropertiesFileEntryTask.class);
            antProject.addTaskDefinition("buildoptions", BuildOptionsTask.class);
            antProject.addTaskDefinition("installerpackage", InstallerPackageTask.class);
            antProject.addTaskDefinition("installerfile", InstallerFileTask.class);
            antProject.init();
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            antProject.addReference("ant.projectHelper", (Object)helper);
            helper.parse(antProject, (Object)antFile);
            antProject.executeTarget(target);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to execute ANT file '" + antFile.getAbsolutePath() + "'. Reason: " + e.getMessage(), e);
        }
        finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
            catch (IOException iOException) {}
        }
    }

    public static String[] getTargets(File antFile) throws SapphireException {
        try {
            Project antProject = new Project();
            antProject.init();
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            antProject.addReference("ant.projectHelper", (Object)helper);
            helper.parse(antProject, (Object)antFile);
            Hashtable t = antProject.getTargets();
            DataSet ds = new DataSet();
            ds.addColumn("target", 0);
            Iterator it = t.keySet().iterator();
            while (it.hasNext()) {
                ds.setString(ds.addRow(), "target", (String)it.next());
            }
            ds.sort("target A");
            String[] targets = new String[ds.size()];
            for (int i = 0; i < ds.size(); ++i) {
                targets[i] = ds.getString(i, "target");
            }
            return targets;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get ANT file targets. Reason: " + e.getMessage(), e);
        }
    }

    public static Hashtable getProperties(File antFile) throws SapphireException {
        try {
            Project antProject = new Project();
            antProject.init();
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            antProject.addReference("ant.projectHelper", (Object)helper);
            helper.parse(antProject, (Object)antFile);
            Hashtable t = antProject.getTargets();
            return antProject.getProperties();
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get ANT file targets. Reason: " + e.getMessage(), e);
        }
    }

    public static HashMap resolveParams(HashMap antParams) throws SapphireException {
        Properties props = new Properties();
        props.putAll((Map<?, ?>)antParams);
        Enumeration<Object> e = props.keys();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            Stack referencesSeen = new Stack();
            AntUtil.resolve(props, name, referencesSeen);
        }
        return new HashMap<Object, Object>(props);
    }

    public static Properties resolvePropertyFile(File propsFile, HashMap antParams) throws SapphireException {
        try {
            FileInputStream fis = new FileInputStream(propsFile);
            Properties props = new Properties();
            props.load(fis);
            fis.close();
            props.putAll((Map<?, ?>)antParams);
            Enumeration<Object> e = props.keys();
            while (e.hasMoreElements()) {
                String name = (String)e.nextElement();
                Stack referencesSeen = new Stack();
                AntUtil.resolve(props, name, referencesSeen);
            }
            return props;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to resolve property file '" + propsFile.getAbsolutePath() + "'. Reason: " + e.getMessage(), e);
        }
    }

    private static void resolve(Properties props, String name, Stack referencesSeen) throws SapphireException {
        if (referencesSeen.contains(name)) {
            throw new SapphireException("Property " + name + " was circularly defined.");
        }
        String value = props.getProperty(name);
        Vector fragments = new Vector();
        Vector propertyRefs = new Vector();
        ProjectHelper.parsePropertyString((String)value, fragments, propertyRefs);
        if (propertyRefs.size() != 0) {
            referencesSeen.push(name);
            StringBuffer sb = new StringBuffer();
            Enumeration i = fragments.elements();
            Enumeration j = propertyRefs.elements();
            while (i.hasMoreElements()) {
                String propertyName;
                String fragment = (String)i.nextElement();
                if (fragment == null && (fragment = props.getProperty(propertyName = (String)j.nextElement())) == null) {
                    if (props.containsKey(propertyName)) {
                        AntUtil.resolve(props, propertyName, referencesSeen);
                        fragment = props.getProperty(propertyName);
                    } else {
                        fragment = "${" + propertyName + "}";
                    }
                }
                sb.append(fragment);
            }
            value = sb.toString();
            props.put(name, value);
            referencesSeen.pop();
        }
    }

    public static HashMap getConnectionProps(ConnectionInfo connectionInfo) {
        HashMap<String, String> antProps = new HashMap<String, String>();
        antProps.put("sapphire.db.jndiname", "");
        antProps.put("sapphire.db.dbms", connectionInfo.getDbms());
        return antProps;
    }

    public static String getAntFileHeader(String projectName, String defaultTarget, HashMap antProps) {
        StringBuffer out = new StringBuffer();
        out.append("<?xml version=\"1.0\" ?>\n");
        out.append("<project name=\"").append(projectName).append("\" default=\"").append(defaultTarget).append("\" basedir=\".\">\n");
        out.append("\t<description>Generated ANT script</description>\n\n");
        for (String propertyname : antProps.keySet()) {
            out.append("\t<property name=\"").append(propertyname).append("\" value=\"").append(antProps.get(propertyname)).append("\"/>\n");
        }
        out.append("\n");
        return out.toString();
    }

    public static String getAntFileFooter() {
        StringBuffer out = new StringBuffer();
        out.append("</project>");
        return out.toString();
    }
}

