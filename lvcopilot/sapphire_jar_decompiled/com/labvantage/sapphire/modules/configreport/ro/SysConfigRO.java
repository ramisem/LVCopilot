/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
import com.labvantage.sapphire.platform.Configuration;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SysConfigRO
extends BaseRO {
    private DataSet profileProperties;
    private DataSet sysConfigProperties;
    private PropertyList overview;
    private PropertyList configProperties;

    @Override
    public void startChapter() throws SapphireException {
        if (this.dataSource.equals("DATABASE")) {
            String sqlPP = "SELECT propertyid, propertyvalue FROM profileproperty where PROFILEID='System' and sysuserid='(system)' and propertyid not like '%userconfig_%' ";
            this.profileProperties = this.getQueryProcessor().getSqlDataSet(sqlPP);
            String sysconfigSQL = "SELECT propertyid, propertyvalue FROM sysconfig";
            this.sysConfigProperties = this.getQueryProcessor().getSqlDataSet(sysconfigSQL);
            this.overview = this.initOverview();
            this.configProperties = this.getConfigProperties();
        } else {
            DataSet unfilteredProfileProperties = this.getProfilePropertiesFromXMLReport(this.refReportFolder);
            this.profileProperties = new DataSet();
            for (int i = 0; i < unfilteredProfileProperties.getRowCount(); ++i) {
                if (unfilteredProfileProperties.getString(i, "propertyid", "").contains("userconfig_")) continue;
                this.profileProperties.copyRow(unfilteredProfileProperties, i, 1);
            }
            this.sysConfigProperties = this.getSysConfigPropertiesFromXMLReport(this.refReportFolder);
            this.overview = this.getOverviewPropertiesFromXMLReport(this.refReportFolder);
            this.configProperties = this.getConfigPropertiesFromXMLReport(this.refReportFolder);
        }
        if (this.configProperties == null) {
            Trace.log("SysConfig_configprops.xml does not exists in the folder " + this.refReportFolder + "/xmlreport/");
            this.configProperties = new PropertyList();
        }
    }

    public PropertyList getSecurityOptions() throws SapphireException {
        PropertyList securityProps = new PropertyList();
        securityProps.setProperty("logonattempts", this.getProfileProperty("logonattempts"));
        securityProps.setProperty("additionallogonattempts", this.getProfileProperty("additionallogonattempts"));
        securityProps.setProperty("maxcooldown", this.getProfileProperty("maxcooldown"));
        securityProps.setProperty("mfaoption", this.getProfileProperty("mfaoption"));
        securityProps.setProperty("mfaprovider", this.getProfileProperty("mfaprovider"));
        securityProps.setProperty("mfaportaloption", this.getProfileProperty("mfaportaloption"));
        securityProps.setProperty("mfaportalprovider", this.getProfileProperty("mfaportalprovider"));
        securityProps.setProperty("disablelegacysessiontracking", this.getProfileProperty("disablelegacysessiontracking"));
        securityProps.setProperty("defaultpassword", this.getProfileProperty("defaultpassword"));
        securityProps.setProperty("disableesig", this.getProfileProperty("disableesig"));
        securityProps.setProperty("log_setuser", this.getProfileProperty("log_setuser"));
        securityProps.setProperty("log_setuserfail", this.getProfileProperty("log_setuserfail"));
        securityProps.setProperty("passwordexpirywarningdays", this.getProfileProperty("PasswordExpiryWarningDays"));
        securityProps.setProperty("passwordexpirydays", this.getProfileProperty("PasswordExpiryDays"));
        securityProps.setProperty("passwordresetmins", this.getProfileProperty("PasswordResetMins"));
        securityProps.setProperty("disableuseronpasswordexpiry", this.getProfileProperty("DisableUserOnPasswordExpiry"));
        return securityProps;
    }

    public PropertyList getPollSchedulingOptions() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("aappoolsize", this.getSysConfigProperty("aappoolsize"));
        props.setProperty("todolistbuffermillis", this.getSysConfigProperty("todolistbuffermillis"));
        props.setProperty("todolistlogging", this.getSysConfigProperty("todolistlogging"));
        props.setProperty("todolistpoll", this.getSysConfigProperty("todolistpoll"));
        props.setProperty("taskpoll", this.getSysConfigProperty("taskpoll"));
        props.setProperty("schedulepoll", this.getSysConfigProperty("schedulepoll"));
        props.setProperty("timeoutpoll", this.getSysConfigProperty("timeoutpoll"));
        props.setProperty("connectiontimeout", this.getSysConfigProperty("connectiontimeout"));
        props.setProperty("rsettimeout", this.getSysConfigProperty("rsettimeout"));
        props.setProperty("executeahead", this.getProfileProperty("executeahead"));
        props.setProperty("executeaheadunits", this.getProfileProperty("executeaheadunits"));
        return props;
    }

    public PropertyList getWorkFlowOptions() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("maxtriggerretry", this.getSysConfigProperty("maxtriggerretry"));
        props.setProperty("triggerdelay", this.getSysConfigProperty("triggerdelay"));
        return props;
    }

    public PropertyList getActionLockingOptions() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("lockactions", this.getProfileProperty("lockactions"));
        return props;
    }

    public PropertyList getLogonOptions() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("logonpageurl", this.getProfileProperty("logonpageurl"));
        props.setProperty("language", this.getProfileProperty("language"));
        return props;
    }

    public PropertyList getAnimationOptions() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("menuanimation", this.getProfileProperty("menuanimation"));
        props.setProperty("fadeanimation", this.getProfileProperty("fadeanimation"));
        props.setProperty("resizeanimation", this.getProfileProperty("resizeanimation"));
        return props;
    }

    public PropertyList getNotificationOptions() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("notificationformat", this.getProfileProperty("notificationformat"));
        return props;
    }

    public PropertyList getBOOptions() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("bourl", this.getProfileProperty("bourl"));
        props.setProperty("bocmsname", this.getProfileProperty("bocmsname"));
        props.setProperty("boconnectiontimeout", this.getProfileProperty("boconnectiontimeout"));
        props.setProperty("bousername", this.getProfileProperty("bousername"));
        props.setProperty("bopassword", this.getProfileProperty("bopassword"));
        props.setProperty("boauthenticationtype", this.getProfileProperty("boauthenticationtype"));
        props.setProperty("borootfoldername", this.getProfileProperty("borootfoldername"));
        props.setProperty("bocrystaldateformat", this.getProfileProperty("bocrystaldateformat"));
        props.setProperty("bodeskidateformat", this.getProfileProperty("bodeskidateformat"));
        props.setProperty("bowebidateformat", this.getProfileProperty("bowebidateformat"));
        return props;
    }

    public PropertyList getLoggingStatsOptions() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("bourl", this.getProfileProperty("bourl"));
        props.setProperty("bodocumentdomain", this.getProfileProperty("bodocumentdomain"));
        props.setProperty("bodomain", this.getProfileProperty("bodomain"));
        props.setProperty("boexchangemode", this.getProfileProperty("boexchangemode"));
        props.setProperty("bouniverse", this.getProfileProperty("bouniverse"));
        props.setProperty("bousername", this.getProfileProperty("bousername"));
        props.setProperty("bopassword", this.getProfileProperty("bopassword"));
        props.setProperty("boauthenticationtype", this.getProfileProperty("boauthenticationtype"));
        return props;
    }

    public PropertyList getConfigurationTransferOptions() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("checksumprocessing", this.getSysConfigProperty("checksumprocessing"));
        props.setProperty("importprocessing", this.getSysConfigProperty("importprocessing"));
        props.setProperty("importdir", this.getSysConfigProperty("importdir"));
        props.setProperty("exportdir", this.getSysConfigProperty("exportdir"));
        return props;
    }

    public PropertyList getAPIOptions() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("enableexecsql", this.getSysConfigProperty("enableexecsql"));
        props.setProperty("customrulesjavapackage", this.getProfileProperty("customrulesjavapackage"));
        props.setProperty("sqlregisterclass", this.getProfileProperty("sqlregisterclass"));
        props.setProperty("lockactions", this.getProfileProperty("lockactions"));
        props.setProperty("dynamicclasslibrary", this.getSysConfigProperty("dynamicclasslibrary"));
        return props;
    }

    public String getApplicationSettings() throws SapphireException {
        return this.configProperties.getProperty("webappbaseurl");
    }

    public PropertyList getConfigMailSetting() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("smtphost", this.configProperties.getProperty("smtphost"));
        props.setProperty("emailaddressfrom", this.configProperties.getProperty("emailaddressfrom"));
        props.setProperty("securityemailaddress", this.configProperties.getProperty("securityemailaddress"));
        props.setProperty("smtpadditionalprops", this.configProperties.getProperty("smtpadditionalprops"));
        props.setProperty("smtpusername", this.configProperties.getProperty("smtpusername"));
        props.setProperty("smtppassword", this.configProperties.getProperty("smtppassword"));
        return props;
    }

    public PropertyList getSysMailSetting() throws SapphireException {
        PropertyList props = new PropertyList();
        for (int i = 0; i < this.sysConfigProperties.size(); ++i) {
            if (this.sysConfigProperties.getString(i, "propertyid").equalsIgnoreCase("smtphost")) {
                props.setProperty("smtphost", this.sysConfigProperties.getString(i, "propertyvalue"));
            }
            if (this.sysConfigProperties.getString(i, "propertyid").equalsIgnoreCase("emailfromaddress")) {
                props.setProperty("emailaddressfrom", this.sysConfigProperties.getString(i, "propertyvalue"));
            }
            if (this.sysConfigProperties.getString(i, "propertyid").equalsIgnoreCase("securityemailtoaddress")) {
                props.setProperty("securityemailaddress", this.sysConfigProperties.getString(i, "propertyvalue"));
            }
            if (this.sysConfigProperties.getString(i, "propertyid").equalsIgnoreCase("mailserveradditionalprops")) {
                props.setProperty("smtpadditionalprops", this.sysConfigProperties.getString(i, "propertyvalue"));
            }
            if (this.sysConfigProperties.getString(i, "propertyid").equalsIgnoreCase("mailserverusernam")) {
                props.setProperty("smtpusername", this.sysConfigProperties.getString(i, "propertyvalue"));
            }
            if (!this.sysConfigProperties.getString(i, "propertyid").equalsIgnoreCase("mailserverpassword")) continue;
            props.setProperty("smtppassword", this.sysConfigProperties.getString(i, "propertyvalue"));
        }
        return props;
    }

    private PropertyList getConfigProperties() throws SapphireException {
        ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
        PropertyList ret = new PropertyList();
        ret.setProperty("webappbaseurl", cp.getConfigProperty("com.labvantage.sapphire.server.webappbaseurl", ""));
        ret.setProperty("smtphost", cp.getConfigProperty("com.labvantage.sapphire.server.smtphost", ""));
        ret.setProperty("emailaddressfrom", cp.getConfigProperty("com.labvantage.sapphire.server.emailfromaddress", ""));
        ret.setProperty("securityemailaddress", cp.getConfigProperty("com.labvantage.sapphire.server.securityemailtoaddress", ""));
        ret.setProperty("smtpadditionalprops", cp.getConfigProperty("com.labvantage.sapphire.server.mailserveradditionalprops", ""));
        ret.setProperty("smtpusername", cp.getConfigProperty("com.labvantage.sapphire.server.mailserverusernam", ""));
        ret.setProperty("smtppassword", cp.getConfigProperty("com.labvantage.sapphire.server.mailserverpassword", ""));
        ret.setProperty("obfuscatesql", cp.getConfigProperty("com.labvantage.sapphire.server.obfuscatesql", ""));
        return ret;
    }

    public PropertyList getRSETSQLControl() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("rsetquerylimit", this.getSysConfigProperty("RSetQueryLimit"));
        props.setProperty("rsetallowquotes", this.getSysConfigProperty("RSetAllowQuotes"));
        props.setProperty("rsetloglevel", this.getSysConfigProperty("RSetLogLevel"));
        props.setProperty("rsetembedsecurity", this.getSysConfigProperty("RSetEmbedSecurity"));
        props.setProperty("sqltimeout", this.getSysConfigProperty("SQLTimeout"));
        props.setProperty("obfuscatesql", this.configProperties.getProperty("obfuscatesql"));
        return props;
    }

    public PropertyList getStabilityDefinitions() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("protocolproductsdcid", this.getProfileProperty("protocolproductsdcid"));
        props.setProperty("studyproductcolumnid", this.getProfileProperty("studyproductcolumnid"));
        return props;
    }

    public PropertyList getTranlationsSetting() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("masterupdate", this.getProfileProperty("masterupdate"));
        props.setProperty("showtranslations", this.getProfileProperty("showtranslations"));
        return props;
    }

    public PropertyList getFileManagemnt() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("fileuploadmaxsize", this.getSysConfigProperty("fileuploadmaxsize"));
        props.setProperty("filedownloadmaxsize", this.getSysConfigProperty("filedownloadmaxsize"));
        props.setProperty("fileuploaddfdmaxsize", this.getSysConfigProperty("fileuploaddfdmaxsize"));
        return props;
    }

    public PropertyList getReportPDFSigning() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("signingmode", this.getSysConfigProperty("signingmode"));
        props.setProperty("signingprovider", this.getSysConfigProperty("signingprovider"));
        props.setProperty("signingprovidernode", this.getSysConfigProperty("signingprovidernode"));
        return props;
    }

    public PropertyList getServerPingOptions() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("serverheartbeat", this.getSysConfigProperty("serverheartbeat"));
        props.setProperty("serverlatency", this.getSysConfigProperty("serverlatency"));
        props.setProperty("processservercommand", this.getSysConfigProperty("processservercommand"));
        return props;
    }

    public PropertyList getHousekeepingOptions() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("hkwebpagehistory", this.getSysConfigProperty("hkwebpagehistory"));
        props.setProperty("hkbulletins", this.getSysConfigProperty("hkbulletins"));
        props.setProperty("hkconnectionlog", this.getSysConfigProperty("hkconnectionlog"));
        props.setProperty("hkdeptassignment", this.getSysConfigProperty("hkdeptassignment"));
        return props;
    }

    public String getProfileProperty(String propertyid) throws SapphireException {
        for (int i = 0; i < this.profileProperties.size(); ++i) {
            String currPropertyId = this.profileProperties.getString(i, "propertyid");
            if (!currPropertyId.equals(propertyid)) continue;
            String currVal = this.profileProperties.getString(i, "propertyvalue");
            if (currVal == null) {
                currVal = "";
            }
            return currVal;
        }
        return "";
    }

    public String getSysConfigProperty(String propertyid) throws SapphireException {
        for (int i = 0; i < this.sysConfigProperties.size(); ++i) {
            String currPropertyId = this.sysConfigProperties.getString(i, "propertyid");
            if (!currPropertyId.equals(propertyid)) continue;
            String currVal = this.sysConfigProperties.getString(i, "propertyvalue");
            if (currVal == null) {
                currVal = "";
            }
            return currVal;
        }
        Trace.log("Invalid property:" + propertyid);
        return "";
    }

    public DataSet getSysConfigProperties() {
        return this.sysConfigProperties;
    }

    public DataSet getProfileProperties() {
        return this.profileProperties;
    }

    public PropertyList getConfigurationProperties() {
        return this.configProperties;
    }

    private PropertyList initOverview() throws SapphireException {
        PropertyList ret = new PropertyList();
        ret.setProperty("database", this.sapphireConnection.getDatabaseId());
        ret.setProperty("isora", this.getConnectionProcessor().isOra() ? "Y" : "N");
        Configuration conf = Configuration.getInstance();
        ret.setProperty("hostname", conf.getServerHostName());
        ret.setProperty("port", conf.getHttpPort());
        ret.setProperty("platform", Configuration.getPlatformName(conf.getPlatform()));
        ret.setProperty("sapphirehome", conf.getSapphireHome());
        String licensePath = conf.hasDatabaseSpecificLicense(this.sapphireConnection.getDatabaseId()) ? "Using Database Specific License" : conf.getServerLicenseFile().getAbsolutePath();
        ret.setProperty("licensefile", licensePath);
        ret.setProperty("build", conf.getAdmindbBuild());
        ret.setProperty("cluster", conf.isCluster() ? "Y" : "N");
        ret.setProperty("clustername", conf.getClusterName());
        Properties p = conf.getLicense(this.sapphireConnection.getDatabaseId()).getProperties();
        Enumeration<?> propertyNames = p.propertyNames();
        PropertyList lic = new PropertyList();
        while (propertyNames.hasMoreElements()) {
            String currProperty = (String)propertyNames.nextElement();
            String currVal = p.getProperty(currProperty);
            lic.setProperty(currProperty, currVal);
        }
        ret.setProperty("licenseproperties", lic.toXMLString());
        ret.setProperty("serverinfo", conf.getServerInfo());
        p = System.getProperties();
        PropertyList sys = new PropertyList();
        sys.setProperty("java.vm.vendor", p.getProperty("java.vm.vendor"));
        sys.setProperty("java.runtime.name", p.getProperty("java.runtime.name"));
        sys.setProperty("java.class.path", p.getProperty("java.class.path"));
        ret.setProperty("sysproperties", sys.toXMLString());
        return ret;
    }

    public PropertyList getOverview() {
        return this.overview;
    }

    private DataSet getProfilePropertiesFromXMLReport(String refReportFolder) {
        String xmlFileName = this.generateSectionXMLFileName("SysConfig", "profileprops");
        String refSDIFileName = refReportFolder + "/xmlreport/" + xmlFileName;
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                DataSet ds = new DataSet(xml);
                return ds;
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    private DataSet getSysConfigPropertiesFromXMLReport(String refReportFolder) {
        String xmlFileName = this.generateSectionXMLFileName("SysConfig", "sysconfigprops");
        String refSDIFileName = refReportFolder + "/xmlreport/" + xmlFileName;
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                DataSet ds = new DataSet(xml);
                return ds;
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    private PropertyList getOverviewPropertiesFromXMLReport(String refReportFolder) {
        String xmlFileName = this.generateSectionXMLFileName("SysConfig", "overviewprops");
        String refSDIFileName = refReportFolder + "/xmlreport/" + xmlFileName;
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                PropertyList pl = new PropertyList();
                pl.setPropertyList(xml, false, false);
                return pl;
            }
        }
        catch (SapphireException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    private PropertyList getConfigPropertiesFromXMLReport(String refReportFolder) {
        String xmlFileName = this.generateSectionXMLFileName("SysConfig", "configprops");
        String refSDIFileName = refReportFolder + "/xmlreport/" + xmlFileName;
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                PropertyList pl = new PropertyList();
                pl.setPropertyList(xml, false, false);
                return pl;
            }
        }
        catch (SapphireException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    public String generateSectionXMLFileName(String chapterName, String sectionName) {
        sectionName = chapterName + "_" + sectionName.replaceAll(",", "");
        String sectionFileName = sectionName.trim().replaceAll(" ", "_");
        return sectionFileName + ".xml";
    }
}

