/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.misc;

import com.labvantage.sapphire.modules.configreport.renderer.BaseRenderer;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
import com.labvantage.sapphire.modules.configreport.ro.SysConfigRO;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SysConfigRenderer
extends BaseRenderer {
    private SysConfigRO sysconfigRO;
    private SysConfigRO refSysconfigRO;
    private boolean reportAllProperties = true;
    private PropertyList config;
    public static final String OPTION_DISPLAY_DETAILS = "Display All Properties";

    @Override
    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO ro, HashMap sdisIncluded) {
        super.initialize(sapphireConnection, config, ro, sdisIncluded);
        this.sysconfigRO = (SysConfigRO)ro;
        this.config = config;
    }

    @Override
    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO ro, BaseRO refRO, HashMap sdisIncluded, boolean includeDiffReport) {
        super.initialize(sapphireConnection, config, ro, refRO, sdisIncluded, includeDiffReport);
        this.sysconfigRO = (SysConfigRO)ro;
        if (refRO != null) {
            this.refSysconfigRO = (SysConfigRO)refRO;
        }
        this.config = config;
    }

    public PropertyListCollection getOptions() {
        PropertyListCollection ret = new PropertyListCollection();
        PropertyList option = new PropertyList();
        option.setProperty("optionid", "DisplayAllProperties");
        option.setProperty("title", OPTION_DISPLAY_DETAILS);
        ret.add(option);
        return ret;
    }

    public void setOptions(PropertyListCollection options) {
        PropertyList selectedOptions = new PropertyList();
        for (int i = 0; i < options.size(); ++i) {
            PropertyList currOptions = options.getPropertyList(i);
            String option = currOptions.getProperty("optionid");
            if (!"DisplayAllProperties".equals(option)) continue;
            selectedOptions.setProperty(OPTION_DISPLAY_DETAILS, currOptions.getProperty("optionvalue"));
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_DISPLAY_DETAILS))) {
            this.reportAllProperties = false;
        }
    }

    @Override
    public ArrayList getSectionList() throws SapphireException {
        ArrayList<String> list = new ArrayList<String>();
        if (this.reportAllProperties) {
            list.add("All Properties");
        }
        list.add("API Options");
        list.add("Automation Options");
        list.add("Configuration Transfer Options");
        list.add("Miscellaneous Options");
        list.add("Security Options");
        list.add("System Configuration Summary");
        list.add("User Options");
        list.add("SMTP Settings");
        return list;
    }

    public boolean hasSectionChanged() {
        return false;
    }

    @Override
    public boolean hasChapterChanged() {
        return this.chapterChanged;
    }

    public void reportNoFrames(String chapterNo, OutputStream reportStream) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "system configuration");
        if (this.reportAllProperties) {
            configReportContent.clearContent();
            configReportContent.startSection("All Properties");
            configReportContent.appendSubSection(this.renderCompleteTables(), "All Properties", this.diffOnly);
            configReportContent.endSection();
            this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("All Properties"), configReportContent);
            configReportContent.pageBreak();
        }
        configReportContent = new ConfigReportContent(this.config, "API Options");
        configReportContent.startSection("API Options");
        configReportContent.startSubHeading("Custom Business Rules", "");
        configReportContent.appendSubSection(this.renderAPICustomBusinessOptions(), "Custom Business Rules", this.diffOnly);
        configReportContent.startSubHeading("Custom SQL Register", "");
        configReportContent.appendSubSection(this.renderAPICustomSQLRegister(), "Custom SQL Register", this.diffOnly);
        configReportContent.startSubHeading("ExecSQL Action", "");
        configReportContent.appendSubSection(this.renderAPIExecSQLOptions(), "ExecSQL Action", this.diffOnly);
        configReportContent.startSubHeading("Action Locking Options", "");
        configReportContent.appendSubSection(this.renderAPIActionLocking(), "Action Locking Options", this.diffOnly);
        configReportContent.startSubHeading("Dynamic Class Loading", "");
        configReportContent.appendSubSection(this.renderAPIDynamicClassLoading(), "Dynamic Class Loading", this.diffOnly);
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("API Options"), configReportContent);
        configReportContent.endSection();
        configReportContent.pageBreak();
        configReportContent.clearContent();
        configReportContent.startSection("Automation Options");
        configReportContent.startSubHeading("Poll and Scheduling Options", "");
        configReportContent.appendSubSection(this.renderPollSchedulingConfiguration(), "Poll and Scheduling Options", this.diffOnly);
        configReportContent.startSubHeading("Server Ping Options", "");
        configReportContent.appendSubSection(this.renderServerPingOptions(), "Server Ping Options", this.diffOnly);
        configReportContent.startSubHeading("Housekeeping Options", "");
        configReportContent.appendSubSection(this.renderHousekeepingOptions(), "Housekeeping Options", this.diffOnly);
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("Poll and Scheduling Options"), configReportContent);
        configReportContent.endSection();
        configReportContent.pageBreak();
        configReportContent.clearContent();
        configReportContent.startSection("Configuration Transfer Options");
        configReportContent.setFoundDiff(false);
        configReportContent.startSubHeading("Configuration Transfer Options", "");
        configReportContent.appendSubSection(this.renderConfigurationTransferOptions(), "Configuration Transfer Options", this.diffOnly);
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("Configuration Transfer Options"), configReportContent);
        configReportContent.endSection();
        configReportContent.pageBreak();
        configReportContent.clearContent();
        configReportContent.startSection("Miscellaneous Options");
        configReportContent.setFoundDiff(false);
        configReportContent.startSubHeading("Application Settings", "");
        configReportContent.appendSubSection(this.renderApplicationSetting(), "Application Settings", this.diffOnly);
        configReportContent.startSubHeading("RSET and SQL Control", "");
        configReportContent.appendSubSection(this.renderRSETControl(), "RSET and SQL Control", this.diffOnly);
        configReportContent.startSubHeading("Stability Data Definitions", "");
        configReportContent.appendSubSection(this.renderStabilityDefinitions(), "Stability Data Definitions", this.diffOnly);
        configReportContent.startSubHeading("Translations", "");
        configReportContent.appendSubSection(this.renderTranlationsSetting(), "Translations", this.diffOnly);
        configReportContent.startSubHeading("File Management", "");
        configReportContent.appendSubSection(this.renderFileManagement(), "File Management", this.diffOnly);
        configReportContent.startSubHeading("Report PDF Signing", "");
        configReportContent.appendSubSection(this.renderReportPDFSigning(), "Report PDF Signing", this.diffOnly);
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("Miscellaneous Options"), configReportContent);
        configReportContent.endSection();
        configReportContent.pageBreak();
        configReportContent.clearContent();
        configReportContent.startSection("Security Options");
        configReportContent.setFoundDiff(false);
        configReportContent.appendSubSection(this.renderSystemConfiguration(), "Security Options", this.diffOnly);
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("Security Options"), configReportContent);
        configReportContent.endSection();
        configReportContent.pageBreak();
        configReportContent.clearContent();
        configReportContent.startChapter(chapterNo, "System Configuration", "");
        configReportContent.startSection("System Configuration Summary");
        configReportContent.setFoundDiff(false);
        configReportContent.appendSubSection(this.renderOverview(), "System Configuration Summary", this.diffOnly);
        configReportContent.endSection();
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("System Configuration Summary"), configReportContent);
        configReportContent.clearContent();
        configReportContent.startSection("User Options");
        configReportContent.setFoundDiff(false);
        configReportContent.startSubHeading("Language Options", "");
        configReportContent.appendSubSection(this.renderLogonOptions(), "Language Options", this.diffOnly);
        configReportContent.startSubHeading("BusinessObjects Options", "");
        configReportContent.appendSubSection(this.renderBOOptions(), "BusinessObjects Options", this.diffOnly);
        configReportContent.startSubHeading("Animation Options", "");
        configReportContent.appendSubSection(this.renderAnimationOptions(), "Animation Options", this.diffOnly);
        configReportContent.startSubHeading("Notification Options", "");
        configReportContent.appendSubSection(this.renderNotificationOptions(), "Notification Options", this.diffOnly);
        configReportContent.endSection();
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("User Options"), configReportContent);
        configReportContent.pageBreak();
        configReportContent.clearContent();
        configReportContent.startSection("SMTP Settings");
        configReportContent.setFoundDiff(false);
        configReportContent.startSubHeading("Mail Settings - For the application server", "Changes made to these mail settings affect all database schemas");
        configReportContent.appendSubSection(this.renderConfigMailSetting(), "Mail Settings", this.diffOnly);
        configReportContent.startSubHeading("Mail Settings - For this database schema", "Changes made to these mail settings affect all database schemas");
        configReportContent.appendSubSection(this.renderSysMailSetting(), "Mail Settings", this.diffOnly);
        configReportContent.endSection();
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("SMTP Settings"), configReportContent);
        configReportContent.pageBreak();
        configReportContent.endChapter(chapterNo);
        try {
            reportStream.write(configReportContent.toString().getBytes());
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
    }

    public void reportWithFrames(String chapterNo) throws SapphireException {
        FileOutputStream sectionFile;
        String sectionFileName;
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "system configuration");
        if (this.reportAllProperties) {
            sectionFileName = ConfigReportContent.generateSectionFileName("System Configuration", "All Properties");
            try {
                sectionFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report file " + sectionFileName);
            }
            configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("System Configuration", "All Properties"));
            configReportContent.startSection("All Properties");
            configReportContent.setFoundDiff(false);
            configReportContent.appendSubSection(this.renderCompleteTables(), "All Properties", this.diffOnly);
            configReportContent.insertDiffAnchors();
            configReportContent.endSection();
            this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("All Properties"), configReportContent);
            configReportContent.endFile();
            this.createSubSectionInfo("System Configuration", ConfigReportContent.generateSectionTitle("All Properties"), configReportContent.diffInfo);
            try {
                sectionFile.write(configReportContent.toString().getBytes());
                sectionFile.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
        }
        configReportContent.clearContent();
        sectionFileName = ConfigReportContent.generateSectionFileName("System Configuration", "API Options");
        try {
            sectionFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report file " + sectionFileName);
        }
        configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("System Configuration", "API Options"));
        configReportContent.startSection("API Options");
        configReportContent.setFoundDiff(false);
        configReportContent.startSubHeading("Custom Business Rules", "");
        configReportContent.appendSubSection(this.renderAPICustomBusinessOptions(), "Custom Business Rules", this.diffOnly);
        configReportContent.startSubHeading("Custom SQL Register", "");
        configReportContent.appendSubSection(this.renderAPICustomSQLRegister(), "Custom SQL Register", this.diffOnly);
        configReportContent.startSubHeading("ExecSQL Action", "");
        configReportContent.appendSubSection(this.renderAPIExecSQLOptions(), "ExecSQL Action", this.diffOnly);
        configReportContent.startSubHeading("Action Locking Options", "");
        configReportContent.appendSubSection(this.renderAPIActionLocking(), "Action Locking Options", this.diffOnly);
        configReportContent.startSubHeading("Dynamic Class Loading", "");
        configReportContent.appendSubSection(this.renderAPIDynamicClassLoading(), "Dynamic Class Loading", this.diffOnly);
        configReportContent.insertDiffAnchors();
        configReportContent.endSection();
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("API Options"), configReportContent);
        configReportContent.endFile();
        this.createSubSectionInfo("System Configuration", ConfigReportContent.generateSectionTitle("API Options"), configReportContent.diffInfo);
        try {
            sectionFile.write(configReportContent.toString().getBytes());
            sectionFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
        sectionFileName = ConfigReportContent.generateSectionFileName("System Configuration", "Automation Options");
        try {
            sectionFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report file " + sectionFileName);
        }
        configReportContent.clearContent();
        configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("System Configuration", "Automation Options"));
        configReportContent.startSection("Automation Options");
        configReportContent.startSubHeading("Poll and Scheduling Options", "");
        configReportContent.appendSubSection(this.renderPollSchedulingConfiguration(), "Poll and Scheduling Options", this.diffOnly);
        configReportContent.startSubHeading("Server Ping Options", "");
        configReportContent.appendSubSection(this.renderServerPingOptions(), "Server Ping Options", this.diffOnly);
        configReportContent.startSubHeading("Housekeeping Options", "");
        configReportContent.appendSubSection(this.renderHousekeepingOptions(), "Housekeeping Options", this.diffOnly);
        configReportContent.insertDiffAnchors();
        configReportContent.endSection();
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("Automation Options"), configReportContent);
        configReportContent.endFile();
        this.createSubSectionInfo("System Configuration", ConfigReportContent.generateSectionTitle("Automation Options"), configReportContent.diffInfo);
        try {
            sectionFile.write(configReportContent.toString().getBytes());
            sectionFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
        sectionFileName = ConfigReportContent.generateSectionFileName("System Configuration", "Configuration Transfer Options");
        try {
            sectionFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report file " + sectionFileName);
        }
        configReportContent.clearContent();
        configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("System Configuration", "Configuration Transfer Options"));
        configReportContent.startSection("Configuration Transfer Options");
        configReportContent.setFoundDiff(false);
        configReportContent.startSubHeading("Configuration Transfer Options", "");
        configReportContent.appendSubSection(this.renderConfigurationTransferOptions(), "Configuration Transfer Options", this.diffOnly);
        configReportContent.insertDiffAnchors();
        configReportContent.endSection();
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("Configuration Transfer Options"), configReportContent);
        configReportContent.endFile();
        this.createSubSectionInfo("System Configuration", ConfigReportContent.generateSectionTitle("Configuration Transfer Options"), configReportContent.diffInfo);
        try {
            sectionFile.write(configReportContent.toString().getBytes());
            sectionFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
        sectionFileName = ConfigReportContent.generateSectionFileName("System Configuration", "Miscellaneous Options");
        try {
            sectionFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report file " + sectionFileName);
        }
        configReportContent.clearContent();
        configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("System Configuration", "Miscellaneous Options"));
        configReportContent.startSection("Miscellaneous Options");
        configReportContent.startSubHeading("Application Settings", "");
        configReportContent.appendSubSection(this.renderApplicationSetting(), "Application Settings", this.diffOnly);
        configReportContent.startSubHeading("RSET and SQL Control", "");
        configReportContent.appendSubSection(this.renderRSETControl(), "RSET and Control", this.diffOnly);
        configReportContent.startSubHeading("Stability Data Definitions", "");
        configReportContent.appendSubSection(this.renderStabilityDefinitions(), "Stability Data Definitions", this.diffOnly);
        configReportContent.startSubHeading("Translations", "");
        configReportContent.appendSubSection(this.renderTranlationsSetting(), "Translations", this.diffOnly);
        configReportContent.startSubHeading("File Management", "");
        configReportContent.appendSubSection(this.renderFileManagement(), "File Management", this.diffOnly);
        configReportContent.startSubHeading("Report PDF Signing", "");
        configReportContent.appendSubSection(this.renderReportPDFSigning(), "Report PDF Signing", this.diffOnly);
        configReportContent.insertDiffAnchors();
        configReportContent.endSection();
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("Miscellaneous Options"), configReportContent);
        configReportContent.endFile();
        this.createSubSectionInfo("System Configuration", ConfigReportContent.generateSectionTitle("Miscellaneous Options"), configReportContent.diffInfo);
        try {
            sectionFile.write(configReportContent.toString().getBytes());
            sectionFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
        sectionFileName = ConfigReportContent.generateSectionFileName("System Configuration", "Security Options");
        try {
            sectionFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report file " + sectionFileName);
        }
        configReportContent.clearContent();
        configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("System Configuration", "Security Options"));
        configReportContent.startSection("Security Options");
        configReportContent.startSubHeading("Security Options", "");
        configReportContent.appendSubSection(this.renderSystemConfiguration(), "Security Options", this.diffOnly);
        configReportContent.insertDiffAnchors();
        configReportContent.endSection();
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("Security Options"), configReportContent);
        configReportContent.endFile();
        this.createSubSectionInfo("System Configuration", ConfigReportContent.generateSectionTitle("Security Options"), configReportContent.diffInfo);
        try {
            sectionFile.write(configReportContent.toString().getBytes());
            sectionFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
        sectionFileName = ConfigReportContent.generateSectionFileName("System Configuration", "System Configuration Summary");
        try {
            sectionFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report file " + sectionFileName);
        }
        configReportContent.clearContent();
        configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("System Configuration", "System Configuration Summary"));
        configReportContent.setFoundDiff(false);
        configReportContent.startSection("System Configuration Summary");
        configReportContent.appendSubSection(this.renderOverview(), "System Configuration Summary", this.diffOnly);
        configReportContent.insertDiffAnchors();
        configReportContent.endSection();
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("System Configuration Summary"), configReportContent);
        configReportContent.endFile();
        this.createSubSectionInfo("System Configuration", ConfigReportContent.generateSectionTitle("System Configuration Summary"), configReportContent.diffInfo);
        try {
            sectionFile.write(configReportContent.toString().getBytes());
            sectionFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
        sectionFileName = ConfigReportContent.generateSectionFileName("System Configuration", "User Options");
        try {
            sectionFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report file " + sectionFileName);
        }
        configReportContent.clearContent();
        configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("System Configuration", "User Options"));
        configReportContent.startSection("User Options");
        configReportContent.setFoundDiff(false);
        configReportContent.startSubHeading("Language Options", "");
        configReportContent.appendSubSection(this.renderLogonOptions(), "Language Options", this.diffOnly);
        configReportContent.startSubHeading("BusinessObjects Options", "");
        configReportContent.appendSubSection(this.renderBOOptions(), "BusinessObjects Options", this.diffOnly);
        configReportContent.endSection();
        configReportContent.startSubHeading("Animation Options", "");
        configReportContent.appendSubSection(this.renderAnimationOptions(), "Animation Options", this.diffOnly);
        configReportContent.startSubHeading("Notification Options", "");
        configReportContent.appendSubSection(this.renderNotificationOptions(), "Notification Options", this.diffOnly);
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("User Options"), configReportContent);
        configReportContent.insertDiffAnchors();
        configReportContent.endFile();
        this.createSubSectionInfo("System Configuration", ConfigReportContent.generateSectionTitle("User Options"), configReportContent.diffInfo);
        try {
            sectionFile.write(configReportContent.toString().getBytes());
            sectionFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
        sectionFileName = ConfigReportContent.generateSectionFileName("System Configuration", "SMTP Settings");
        try {
            sectionFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report file " + sectionFileName);
        }
        configReportContent.clearContent();
        configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("System Configuration", "SMTP Settings"));
        configReportContent.startSection("SMTP Settings");
        configReportContent.setFoundDiff(false);
        configReportContent.startSubHeading("Mail Settings - For the application server", "Changes made to these mail settings affect all database schemas");
        configReportContent.appendSubSection(this.renderConfigMailSetting(), "Mail Settings", this.diffOnly);
        configReportContent.startSubHeading("Mail Settings - For this database schema", "Changes made to these mail settings affect all database schemas");
        configReportContent.appendSubSection(this.renderSysMailSetting(), "Mail Settings", this.diffOnly);
        this.updateSectionChangeInfo("System Configuration", ConfigReportContent.generateSectionTitle("SMTP Settings"), configReportContent);
        configReportContent.insertDiffAnchors();
        configReportContent.endFile();
        this.createSubSectionInfo("System Configuration", ConfigReportContent.generateSectionTitle("SMTP Settings"), configReportContent.diffInfo);
        try {
            sectionFile.write(configReportContent.toString().getBytes());
            sectionFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
    }

    public ConfigReportContent renderSystemConfiguration() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "system configuration");
        PropertyList pl = this.sysconfigRO.getSecurityOptions();
        configReportContent.startBulletList();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("The maximum number of times the user can incorrectly enter a password before the account is disabled is : " + pl.getProperty("logonattempts") + " times");
            configReportContent.addBullet("The number of times a user can incorrectly enter a password before their account enters a cooldown period: " + pl.getProperty("additionallogonattempts") + " times");
            configReportContent.addBullet("The maximum cooldown time in seconds: " + pl.getProperty("maxcooldown"));
            configReportContent.addBullet("MFA Options: " + SysConfigRenderer.checkenableordisable(pl.getProperty("mfaoption")));
            configReportContent.addBullet("MFA Provider: " + SysConfigRenderer.checkmfa(pl.getProperty("mfaprovider")));
            configReportContent.addBullet("Portal MFA Option: " + SysConfigRenderer.checkenableordisable(pl.getProperty("mfaportaloption")));
            configReportContent.addBullet("Portal MFA Provider: " + SysConfigRenderer.checkmfa(pl.getProperty("mfaportalprovider")));
            configReportContent.addBullet("Is log generated each time a user successfully logs in? : " + SysConfigRenderer.yesorno(pl.getProperty("log_setuser")));
            configReportContent.addBullet("Is log generated each time a user fails a login attempt? : " + SysConfigRenderer.yesorno(pl.getProperty("log_setuserfail")));
            configReportContent.addBullet("Determines if an audit record is generated for every successful logon attempt. It is disabled by default: " + SysConfigRenderer.yesorno(pl.getProperty("disablelegacysessiontracking")));
            configReportContent.addBullet("The number of days before a password expires that a user is notified of the pending password expiry : " + pl.getProperty("passwordexpirywarningdays") + " days");
            configReportContent.addBullet("The duration after which the new password will expire : " + pl.getProperty("passwordexpirydays") + " days");
            configReportContent.addBullet("The duration after reset password link will expire : " + pl.getProperty("passwordresetmins") + " minutes");
            configReportContent.addBullet("Is the user disabled when the password expires? : " + SysConfigRenderer.yesorno(pl.getProperty("disableuseronpasswordexpiry")));
            configReportContent.addBullet("Determines the default password when a new user is created by copying another: " + pl.getProperty("defaultpassword") + "***********");
            configReportContent.addBullet("Whether to disable electronic signature globally. This will override esig configurations on all buttons: " + SysConfigRenderer.yesorno(pl.getProperty("disableesig")));
        } else {
            PropertyList refPl = this.refSysconfigRO.getSecurityOptions();
            configReportContent.addBullet("The maximum number of times the user can incorrectly enter a password before the account is disabled is :" + ConfigReportContent.getDiffString(pl.getProperty("logonattempts"), refPl.getProperty("logonattempts")) + " times");
            configReportContent.addBullet("The number of times a user can incorrectly enter a password before their account enters a cooldown period: " + ConfigReportContent.getDiffString(pl.getProperty("additionallogonattempts"), refPl.getProperty("additionallogonattempts")) + " times");
            configReportContent.addBullet("The maximum cooldown time in seconds: " + ConfigReportContent.getDiffString(pl.getProperty("maxcooldown"), refPl.getProperty("maxcooldown")));
            configReportContent.addBullet("MFA Options: " + SysConfigRenderer.checkenableordisable(pl.getProperty("mfaoption")));
            configReportContent.addBullet("MFA Provider: " + SysConfigRenderer.checkmfa(pl.getProperty("mfaprovider")));
            configReportContent.addBullet("Portal MFA Option: " + SysConfigRenderer.checkenableordisable(pl.getProperty("mfaportaloption")));
            configReportContent.addBullet("Portal MFA Provider: " + SysConfigRenderer.checkmfa(pl.getProperty("mfaportalprovider")));
            configReportContent.addBullet("Is log generated each time a user successfully logs in? : " + ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("log_setuser")), SysConfigRenderer.yesorno(refPl.getProperty("log_setuser"))));
            configReportContent.addBullet("Is log generated each time a user fails a login attempt? : " + ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("log_setuserfail")), SysConfigRenderer.yesorno(refPl.getProperty("log_setuserfail"))));
            configReportContent.addBullet("Determines if an audit record is generated for every successful logon attempt. It is disabled by default: " + ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("disablelegacysessiontracking")), SysConfigRenderer.yesorno(refPl.getProperty("disablelegacysessiontracking"))));
            configReportContent.addBullet("The number of days before a password expires that a user is notified of the pending password expiry : " + ConfigReportContent.getDiffString(pl.getProperty("passwordexpirywarningdays"), refPl.getProperty("passwordexpirywarningdays")) + " days");
            configReportContent.addBullet("The duration after which the new password will expire : " + ConfigReportContent.getDiffString(pl.getProperty("passwordexpirydays"), refPl.getProperty("passwordexpirydays")) + " days");
            configReportContent.addBullet("The duration after reset password link will expire : " + ConfigReportContent.getDiffString(pl.getProperty("passwordresetmins"), refPl.getProperty("passwordresetmins")) + " minutes");
            configReportContent.addBullet("Is the user disabled when the password expires? : " + ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("disableuseronpasswordexpiry")), SysConfigRenderer.yesorno(refPl.getProperty("disableuseronpasswordexpiry"))));
            configReportContent.addBullet("Determines the default password when a new user is created by copying another: " + ConfigReportContent.getDiffString(pl.getProperty("defaultpassword"), refPl.getProperty("defaultpassword")) + "***********");
            configReportContent.addBullet("Whether to disable electronic signature globally. This will override esig configurations on all buttons: " + ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("disableesig")), SysConfigRenderer.yesorno(refPl.getProperty("disableesig"))));
        }
        configReportContent.endBulletList();
        return configReportContent;
    }

    public ConfigReportContent renderPollSchedulingConfiguration() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "poll scheduling configuration");
        PropertyList pl = this.sysconfigRO.getPollSchedulingOptions();
        configReportContent.startBulletList();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("ToDoList is polled every " + pl.getProperty("todolistpoll") + " seconds.");
            configReportContent.addBullet("The number of threads per server that can process todolist actions simultaneously: " + pl.getProperty("aappoolsize"));
            configReportContent.addBullet("When processing a backlog of todolist entries, this slight delay allows for threads to get synchronized before grabbing more entries to process: " + pl.getProperty("todolistbuffermillis") + " ms");
            configReportContent.addBullet("Enabled database-level logging in the todolistlog table:" + SysConfigRenderer.yesorno(pl.getProperty("todolistlogging")));
            configReportContent.addBullet("Scheduled Task's are polled every  " + pl.getProperty("taskpoll") + " seconds.");
            configReportContent.addBullet("Scheduler engine checks scheduled plan items every " + pl.getProperty("schedulepoll") + " seconds.");
            configReportContent.addBullet("LabVantage system polls the connections and RSets every " + pl.getProperty("timeoutpoll") + " seconds.");
            configReportContent.addBullet("If a connection is left for more than " + pl.getProperty("connectiontimeout") + " seconds then it will be deleted.");
            configReportContent.addBullet("If a RSet is left for more than  " + pl.getProperty("rsettimeout") + " seconds, then it will be cleared.");
            configReportContent.addBullet("Default scheduler execute ahead period is : " + pl.getProperty("executeahead") + " " + pl.getProperty("executeaheadunits"));
        } else {
            PropertyList refPl = this.refSysconfigRO.getPollSchedulingOptions();
            configReportContent.addBullet("ToDoList is polled every " + ConfigReportContent.getDiffString(pl.getProperty("todolistpoll"), refPl.getProperty("todolistpoll")) + " seconds.");
            configReportContent.addBullet("The number of threads per server that can process todolist actions simultaneously: " + ConfigReportContent.getDiffString(pl.getProperty("aappoolsize"), refPl.getProperty("aappoolsize")));
            configReportContent.addBullet("When processing a backlog of todolist entries, this slight delay allows for threads to get synchronized before grabbing more entries to process: " + ConfigReportContent.getDiffString(pl.getProperty("todolistbuffermillis"), refPl.getProperty("todolistbuffermillis")) + " ms");
            configReportContent.addBullet("Enabled database-level logging in the todolistlog table:" + ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("todolistlogging")), SysConfigRenderer.yesorno(refPl.getProperty("todolistlogging"))));
            configReportContent.addBullet("Scheduled Task's are polled every  " + ConfigReportContent.getDiffString(pl.getProperty("taskpoll"), refPl.getProperty("taskpoll")) + " seconds.");
            configReportContent.addBullet("Scheduler engine checks scheduled plan items every " + ConfigReportContent.getDiffString(pl.getProperty("schedulepoll"), refPl.getProperty("schedulepoll")) + " seconds.");
            configReportContent.addBullet("LabVantage system polls the connections and RSets every " + ConfigReportContent.getDiffString(pl.getProperty("timeoutpoll"), refPl.getProperty("timeoutpoll")) + " seconds.");
            configReportContent.addBullet("If a connection is left for more than " + ConfigReportContent.getDiffString(pl.getProperty("connectiontimeout"), refPl.getProperty("connectiontimeout")) + " seconds then it will be deleted.");
            configReportContent.addBullet("If a RSet is left for more than  " + ConfigReportContent.getDiffString(pl.getProperty("rsettimeout"), refPl.getProperty("rsettimeout")) + " seconds, then it will be cleared.");
            configReportContent.addBullet("Default scheduler execute ahead period is : " + ConfigReportContent.getDiffString(pl.getProperty("executeahead") + " " + pl.getProperty("executeaheadunits"), refPl.getProperty("executeahead") + " " + refPl.getProperty("executeaheadunits")));
        }
        configReportContent.endBulletList();
        return configReportContent;
    }

    public ConfigReportContent renderLogonOptions() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "logon options");
        PropertyList pl = this.sysconfigRO.getLogonOptions();
        configReportContent.startTable();
        configReportContent.startRow();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.startRow();
            configReportContent.addRowItem("The default language is : ", pl.getProperty("language"));
        } else {
            PropertyList refPl = this.refSysconfigRO.getLogonOptions();
            configReportContent.startRow();
            configReportContent.addRowItem("The default language is : ", ConfigReportContent.getDiffString(pl.getProperty("language"), refPl.getProperty("language")));
        }
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public ConfigReportContent renderAnimationOptions() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "animation options");
        PropertyList pl = this.sysconfigRO.getAnimationOptions();
        configReportContent.startTable();
        configReportContent.startRow();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.startRow();
            configReportContent.addRowItem("Enable Menu Animations: : ", SysConfigRenderer.yesorno(pl.getProperty("menuanimation")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Enable Fade-in & Fade-out Animations: ", SysConfigRenderer.yesorno(pl.getProperty("fadeanimation")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Enable Resize-in & Resize-out Animations: : ", SysConfigRenderer.yesorno(pl.getProperty("resizeanimation")));
            configReportContent.endRow();
        } else {
            PropertyList refPl = this.refSysconfigRO.getAnimationOptions();
            configReportContent.startRow();
            configReportContent.addRowItem("Enable Menu Animations: : ", ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("menuanimation")), SysConfigRenderer.yesorno(refPl.getProperty("menuanimation"))));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Enable Fade-in & Fade-out Animations: ", ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("fadeanimation")), SysConfigRenderer.yesorno(refPl.getProperty("fadeanimation"))));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Enable Resize-in & Resize-out Animations: : ", ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("resizeanimation")), SysConfigRenderer.yesorno(refPl.getProperty("resizeanimation"))));
            configReportContent.endRow();
        }
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public ConfigReportContent renderBOOptions() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "BO options");
        PropertyList pl = this.sysconfigRO.getBOOptions();
        configReportContent.startTable();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.startRow();
            String bourltext = StringUtil.replaceAll(StringUtil.replaceAll(pl.getProperty("bourl"), "<", "&#60;"), ">", "&#62;");
            configReportContent.addRowItem("BO URL:", bourltext);
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO CMS Name:", pl.getProperty("bocmsname"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Username:", pl.getProperty("bousername"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Password:", pl.getProperty("bopassword1") + "***********");
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Authentication Type:", pl.getProperty("boauthenticationtype"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Root Folder Name:", pl.getProperty("borootfoldername"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Connection Timeout in Seconds", pl.getProperty("boconnectiontimeout"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Crystal Date Format:", pl.getProperty("bocrystaldateformat"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Desktop Date Format:", pl.getProperty("bodeskidateformat"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Webi Date Format:", pl.getProperty("bowebidateformat"));
            configReportContent.endRow();
        } else {
            PropertyList refPl = this.refSysconfigRO.getBOOptions();
            configReportContent.startRow();
            String bourltext = StringUtil.replaceAll(StringUtil.replaceAll(pl.getProperty("bourl"), "<", "&#60;"), ">", "&#62;");
            String bourlreftext = StringUtil.replaceAll(StringUtil.replaceAll(refPl.getProperty("bourl"), "<", "&#60;"), ">", "&#62;");
            configReportContent.addRowItem("BO URL:", ConfigReportContent.getDiffString(bourltext, bourlreftext));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO CMS Name:", ConfigReportContent.getDiffString(pl.getProperty("bocmsname"), refPl.getProperty("bocmsname")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Username:", ConfigReportContent.getDiffString(pl.getProperty("bousername"), refPl.getProperty("bousername")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Password:", ConfigReportContent.getDiffString(pl.getProperty("bopassword1"), refPl.getProperty("bopassword1")) + "***********");
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Authentication Type:", ConfigReportContent.getDiffString(pl.getProperty("boauthenticationtype"), refPl.getProperty("boauthenticationtype")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Root Folder Name:", ConfigReportContent.getDiffString(pl.getProperty("borootfoldername"), refPl.getProperty("borootfoldername")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Connection Timeout in Seconds", ConfigReportContent.getDiffString(pl.getProperty("boconnectiontimeout"), refPl.getProperty("boconnectiontimeout")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Crystal Date Format:", ConfigReportContent.getDiffString(pl.getProperty("bocrystaldateformat"), refPl.getProperty("bocrystaldateformat")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Desktop Date Format:", ConfigReportContent.getDiffString(pl.getProperty("bodeskidateformat"), refPl.getProperty("bodeskidateformat")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("BO Webi Date Format:", ConfigReportContent.getDiffString(pl.getProperty("bowebidateformat"), refPl.getProperty("bowebidateformat")));
            configReportContent.endRow();
        }
        configReportContent.endTable();
        return configReportContent;
    }

    public ConfigReportContent renderNotificationOptions() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "logon options");
        PropertyList pl = this.sysconfigRO.getNotificationOptions();
        configReportContent.startTable();
        configReportContent.startRow();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.startRow();
            configReportContent.addRowItem("Notification Format : ", pl.getProperty("notificationformat"));
        } else {
            PropertyList refPl = this.refSysconfigRO.getNotificationOptions();
            configReportContent.startRow();
            configReportContent.addRowItem("Notification Format : ", ConfigReportContent.getDiffString(pl.getProperty("notificationformat"), refPl.getProperty("notificationformat")));
        }
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public static String checkenableordisable(String flag) {
        if (flag.equals("0")) {
            return "Disable";
        }
        return "Enable";
    }

    public static String checkmfa(String flag) {
        if (flag.equalsIgnoreCase("DuoSecurity2ndFactorAuthentication")) {
            return "Duo Security";
        }
        if (flag.equalsIgnoreCase("DefaultLV2ndFactorAuthentication")) {
            return "LabVantage";
        }
        return "Property tree for default SSO authentication";
    }

    public static String yesorno(String flag) {
        if ("Y".equals(flag)) {
            return "Yes";
        }
        return "No";
    }

    public static String checksumprocessing(String flag) {
        if (flag == null || flag.length() == 0) {
            return "";
        }
        if ("I".equals(flag)) {
            return "Ignore";
        }
        if ("W".equals(flag)) {
            return "Warn";
        }
        return "Block";
    }

    public static String importprocessing(String flag) {
        if (flag == null || flag.length() == 0) {
            return "";
        }
        if ("F".equals(flag)) {
            return "File";
        }
        if ("L".equals(flag)) {
            return "Log";
        }
        if ("R".equals(flag)) {
            return "Esig with reason";
        }
        return "Esig";
    }

    public ConfigReportContent renderConfigurationTransferOptions() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "configuration transfer options");
        PropertyList pl = this.sysconfigRO.getConfigurationTransferOptions();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("Checksum Processing : " + SysConfigRenderer.checksumprocessing(pl.getProperty("checksumprocessing")));
            configReportContent.addBullet("Import Processing : " + SysConfigRenderer.importprocessing(pl.getProperty("importprocessing")));
            configReportContent.addBullet("The Default export Directory is : " + pl.getProperty("exportdir"));
            configReportContent.addBullet("The Default import Directory is : " + pl.getProperty("importdir"));
        } else {
            PropertyList refPl = this.refSysconfigRO.getConfigurationTransferOptions();
            configReportContent.addBullet("Checksum Processing : " + ConfigReportContent.getDiffString(SysConfigRenderer.checksumprocessing(pl.getProperty("checksumprocessing")), SysConfigRenderer.checksumprocessing(refPl.getProperty("checksumprocessing"))));
            configReportContent.addBullet("Import Processing : " + ConfigReportContent.getDiffString(SysConfigRenderer.importprocessing(pl.getProperty("importprocessing")), SysConfigRenderer.importprocessing(refPl.getProperty("importprocessing"))));
            configReportContent.addBullet("The Default export Directory is : " + ConfigReportContent.getDiffString(pl.getProperty("exportdir"), refPl.getProperty("exportdir")));
            configReportContent.addBullet("The Default import Directory is : " + ConfigReportContent.getDiffString(pl.getProperty("importdir"), refPl.getProperty("importdir")));
        }
        return configReportContent;
    }

    public ConfigReportContent renderAPICustomBusinessOptions() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "API custom business options");
        PropertyList pl = this.sysconfigRO.getAPIOptions();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("The Java Package which contains the Custom Business rule classes is : " + pl.getProperty("customrulesjavapackage"));
        } else {
            PropertyList refPl = this.refSysconfigRO.getAPIOptions();
            configReportContent.addBullet("The Java Package which contains the Custom Business rule classes is : " + ConfigReportContent.getDiffString(pl.getProperty("customrulesjavapackage"), refPl.getProperty("customrulesjavapackage")));
        }
        return configReportContent;
    }

    public ConfigReportContent renderAPICustomSQLRegister() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "API custom SQL Register");
        PropertyList pl = this.sysconfigRO.getAPIOptions();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("The Java class which implements Custom SQL register: " + pl.getProperty("sqlregisterclass"));
        } else {
            PropertyList refPl = this.refSysconfigRO.getAPIOptions();
            configReportContent.addBullet("The Java class which implements Custom SQL register: " + ConfigReportContent.getDiffString(pl.getProperty("sqlregisterclass"), refPl.getProperty("sqlregisterclass")));
        }
        return configReportContent;
    }

    public ConfigReportContent renderAPIExecSQLOptions() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "API execlSQL options");
        PropertyList pl = this.sysconfigRO.getAPIOptions();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("Is the ExecSQL action available? : " + SysConfigRenderer.yesorno(pl.getProperty("enableexecsql")));
        } else {
            PropertyList refPl = this.refSysconfigRO.getAPIOptions();
            configReportContent.addBullet("Is the ExecSQL action available? : " + ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("enableexecsql")), SysConfigRenderer.yesorno(refPl.getProperty("enableexecsql"))));
        }
        return configReportContent;
    }

    public ConfigReportContent renderAPIActionLocking() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "API Action Lock");
        PropertyList pl = this.sysconfigRO.getAPIOptions();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("Do you want actions to automatically lock data? : " + SysConfigRenderer.yesorno(pl.getProperty("lockactions")));
        } else {
            PropertyList refPl = this.refSysconfigRO.getAPIOptions();
            configReportContent.addBullet("Do you want actions to automatically lock data? : " + ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("lockactions")), SysConfigRenderer.yesorno(refPl.getProperty("lockactions"))));
        }
        return configReportContent;
    }

    public ConfigReportContent renderAPIDynamicClassLoading() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "API Dynamic Class Loading");
        PropertyList pl = this.sysconfigRO.getAPIOptions();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("LabVantage can use dynamic class loading for functionality such as the Attachment Handlers. Specify the library path: " + pl.getProperty("dynamicclasslibrary"));
        } else {
            PropertyList refPl = this.refSysconfigRO.getAPIOptions();
            configReportContent.addBullet("LabVantage can use dynamic class loading for functionality such as the Attachment Handlers. Specify the library path: " + ConfigReportContent.getDiffString(pl.getProperty("dynamicclasslibrary"), refPl.getProperty("dynamicclasslibrary")));
        }
        return configReportContent;
    }

    public ConfigReportContent renderApplicationSetting() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Web Application base URL");
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("Web Application Base URL is : " + this.sysconfigRO.getApplicationSettings());
        } else {
            configReportContent.addBullet("Web Application Base URL is :" + ConfigReportContent.getDiffString(this.sysconfigRO.getApplicationSettings(), this.refSysconfigRO.getApplicationSettings()));
        }
        return configReportContent;
    }

    public ConfigReportContent renderConfigMailSetting() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Mail settings");
        PropertyList pl = this.sysconfigRO.getConfigMailSetting();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.startTable();
            configReportContent.startRow();
            configReportContent.addRowItem("The SMTP mail server for dispatching messages using JavaMail is : ", pl.getProperty("smtphost"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("The default 'from' address that messages will use is : ", pl.getProperty("emailaddressfrom"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("The Email address to receive security emails is : ", pl.getProperty("securityemailaddress"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Additional Mail Server Connection Properties: ", pl.getProperty("smtpadditionalprops"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Mail Server User Name: ", pl.getProperty("smtpusername"));
            configReportContent.endRow();
            configReportContent.endTable();
        } else {
            PropertyList refPl = this.refSysconfigRO.getConfigMailSetting();
            configReportContent.startTable();
            configReportContent.startRow();
            configReportContent.addRowItem("The SMTP mail server for dispatching messages using JavaMail is : ", ConfigReportContent.getDiffString(pl.getProperty("smtphost"), refPl.getProperty("smtphost")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("The default 'from' address that messages will use is : ", ConfigReportContent.getDiffString(pl.getProperty("emailaddressfrom"), refPl.getProperty("emailaddressfrom")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("The Email address to receive security emails is : ", ConfigReportContent.getDiffString(pl.getProperty("securityemailaddress"), refPl.getProperty("securityemailaddress")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Additional Mail Server Connection Properties: ", ConfigReportContent.getDiffString(pl.getProperty("smtpadditionalprops"), refPl.getProperty("smtpadditionalprops")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Mail Server User Name: ", ConfigReportContent.getDiffString(pl.getProperty("smtpusername"), refPl.getProperty("smtpusername")));
            configReportContent.endRow();
            configReportContent.endTable();
        }
        return configReportContent;
    }

    public ConfigReportContent renderSysMailSetting() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Mail settings");
        PropertyList pl = this.sysconfigRO.getSysMailSetting();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.startTable();
            configReportContent.startRow();
            configReportContent.addRowItem("The SMTP mail server for dispatching messages using JavaMail is : ", pl.getProperty("smtphost"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("The default 'from' address that messages will use is : ", pl.getProperty("emailaddressfrom"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("The Email address to receive security emails is : ", pl.getProperty("securityemailaddress"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Additional Mail Server Connection Properties: ", pl.getProperty("smtpadditionalprops"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Mail Server User Name: ", pl.getProperty("smtpusername"));
            configReportContent.endRow();
            configReportContent.endTable();
        } else {
            PropertyList refPl = this.refSysconfigRO.getSysMailSetting();
            configReportContent.startTable();
            configReportContent.startRow();
            configReportContent.addRowItem("The SMTP mail server for dispatching messages using JavaMail is : ", ConfigReportContent.getDiffString(pl.getProperty("smtphost"), refPl.getProperty("smtphost")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("The default 'from' address that messages will use is : ", ConfigReportContent.getDiffString(pl.getProperty("emailaddressfrom"), refPl.getProperty("emailaddressfrom")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("The Email address to receive security emails is : ", ConfigReportContent.getDiffString(pl.getProperty("securityemailaddress"), refPl.getProperty("securityemailaddress")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Additional Mail Server Connection Properties: ", ConfigReportContent.getDiffString(pl.getProperty("smtpadditionalprops"), refPl.getProperty("smtpadditionalprops")));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Mail Server User Name: ", ConfigReportContent.getDiffString(pl.getProperty("smtpusername"), refPl.getProperty("smtpusername")));
            configReportContent.endRow();
            configReportContent.endTable();
        }
        return configReportContent;
    }

    public ConfigReportContent renderRSETControl() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "RSET control options");
        PropertyList pl = this.sysconfigRO.getRSETSQLControl();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("The upper-limit on the number of SDIs that get retrieved as part of a single query  are : " + pl.getProperty("rsetquerylimit"));
            configReportContent.addBullet("Are single quotes allowed in RSET parameters ?: " + SysConfigRenderer.yesorno(pl.getProperty("rsetallowquotes")));
            configReportContent.addBullet("The level of logging required for debugging RSET queries is : " + pl.getProperty("rsetloglevel"));
            configReportContent.addBullet("Embed security within RSet queries: " + SysConfigRenderer.yesorno(pl.getProperty("rsetembedsecurity")));
            configReportContent.addBullet("SQL Statement Timeout: " + pl.getProperty("sqltimeout"));
            configReportContent.addBullet("SQL Obfuscation: " + ("O".equals(pl.getProperty("obfuscatesql")) ? "Yes" : "No"));
        } else {
            PropertyList refPl = this.refSysconfigRO.getRSETSQLControl();
            configReportContent.addBullet("The upper-limit on the number of SDIs that get retrieved as part of a single query  are : " + ConfigReportContent.getDiffString(pl.getProperty("rsetquerylimit"), refPl.getProperty("rsetquerylimit")));
            configReportContent.addBullet("Are single quotes allowed in RSET parameters ?: " + ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("rsetallowquotes")), SysConfigRenderer.yesorno(refPl.getProperty("rsetallowquotes"))));
            configReportContent.addBullet("The level of logging required for debugging RSET queries is : " + ConfigReportContent.getDiffString(pl.getProperty("rsetloglevel"), refPl.getProperty("rsetloglevel")));
            configReportContent.addBullet("Embed security within RSet queries: " + ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("rsetembedsecurity")), SysConfigRenderer.yesorno(refPl.getProperty("rsetembedsecurity"))));
            configReportContent.addBullet("SQL Statement Timeout: " + ConfigReportContent.getDiffString(pl.getProperty("sqltimeout"), refPl.getProperty("sqltimeout")));
            configReportContent.addBullet("SQL Obfuscation: " + ConfigReportContent.getDiffString("O".equals(pl.getProperty("obfuscatesql")) ? "Yes" : "No", "O".equals(refPl.getProperty("obfuscatesql")) ? "Yes" : "No"));
        }
        return configReportContent;
    }

    public ConfigReportContent renderStabilityDefinitions() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "stability definitions");
        PropertyList pl = this.sysconfigRO.getStabilityDefinitions();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("The Protocol product SDC is : " + pl.getProperty("protocolproductsdcid"));
            configReportContent.addBullet("Study product column is: " + pl.getProperty("studyproductcolumnid"));
        } else {
            PropertyList refPl = this.refSysconfigRO.getStabilityDefinitions();
            configReportContent.addBullet("The Protocol product SDC is : " + ConfigReportContent.getDiffString(pl.getProperty("protocolproductsdcid"), refPl.getProperty("protocolproductsdcid")));
            configReportContent.addBullet("Study product column is : " + ConfigReportContent.getDiffString(pl.getProperty("studyproductcolumnid"), refPl.getProperty("studyproductcolumnid")));
        }
        return configReportContent;
    }

    public ConfigReportContent renderTranlationsSetting() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "translations setting");
        PropertyList pl = this.sysconfigRO.getTranlationsSetting();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("Will the transmastertemp table be populated with text where no translation was found ? : " + SysConfigRenderer.yesorno(pl.getProperty("masterupdate")));
            configReportContent.addBullet("All texts that are translated on the server side or on the client side will be tagged: " + SysConfigRenderer.yesorno(pl.getProperty("showtranslations")));
        } else {
            PropertyList refPl = this.refSysconfigRO.getTranlationsSetting();
            configReportContent.addBullet("Will the transmastertemp table be populated with text where no translation was found ? : " + ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("masterupdate")), SysConfigRenderer.yesorno(refPl.getProperty("masterupdate"))));
            configReportContent.addBullet("All texts that are translated on the server side or on the client side will be tagged: " + ConfigReportContent.getDiffString(SysConfigRenderer.yesorno(pl.getProperty("showtranslations")), SysConfigRenderer.yesorno(refPl.getProperty("showtranslations"))));
        }
        return configReportContent;
    }

    public ConfigReportContent renderFileManagement() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "file managment");
        PropertyList pl = this.sysconfigRO.getFileManagemnt();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("The maximum size of file that can be uploaded is : " + pl.getProperty("fileuploadmaxsize") + " mb");
            configReportContent.addBullet("The maximum size of file that can be downloaded is : " + pl.getProperty("filedownloadmaxsize") + " mb");
            configReportContent.addBullet("The maximum size of file that can be uploaded as an example data file in DFD maintenance is : " + pl.getProperty("fileuploaddfdmaxsize") + " mb");
        } else {
            PropertyList refPl = this.refSysconfigRO.getFileManagemnt();
            configReportContent.addBullet("The maximum size of file that can be uploaded is : " + ConfigReportContent.getDiffString(pl.getProperty("fileuploadmaxsize"), refPl.getProperty("fileuploadmaxsize")) + " mb");
            configReportContent.addBullet("The maximum size of file that can be downloaded is : " + ConfigReportContent.getDiffString(pl.getProperty("filedownloadmaxsize"), refPl.getProperty("filedownloadmaxsize")) + " mb");
            configReportContent.addBullet("The maximum size of file that can be uploaded as an example data file in DFD maintenance is : " + ConfigReportContent.getDiffString(pl.getProperty("fileuploaddfdmaxsize"), refPl.getProperty("fileuploaddfdmaxsize")) + " mb");
        }
        return configReportContent;
    }

    public ConfigReportContent renderReportPDFSigning() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "report pdf signing");
        PropertyList pl = this.sysconfigRO.getReportPDFSigning();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("The default signing mode for reports is : " + pl.getProperty("signingmode"));
            configReportContent.addBullet("The default signing provider for reports is : " + pl.getProperty("signingprovider"));
            configReportContent.addBullet("The default signing provider node for reports is : " + pl.getProperty("signingprovidernode"));
        } else {
            PropertyList refPl = this.refSysconfigRO.getReportPDFSigning();
            configReportContent.addBullet("The default signing mode for reports is : " + ConfigReportContent.getDiffString(pl.getProperty("signingmode"), refPl.getProperty("signingmode")));
            configReportContent.addBullet("The default signing provider for reports is : " + ConfigReportContent.getDiffString(pl.getProperty("signingprovider"), refPl.getProperty("signingprovider")));
            configReportContent.addBullet("The default signing provider node for reports is : " + ConfigReportContent.getDiffString(pl.getProperty("signingprovidernode"), refPl.getProperty("signingprovidernode")));
        }
        return configReportContent;
    }

    public ConfigReportContent renderServerPingOptions() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "server ping options");
        PropertyList pl = this.sysconfigRO.getServerPingOptions();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("How often each LabVantage server updates the admin database to indicate that it is still alive: " + pl.getProperty("serverheartbeat") + " s");
            configReportContent.addBullet("Remove from the admin database any server that has not pinged for a time greater than the latency period. This setting should be approximately threes times the server ping interval: " + pl.getProperty("serverlatency") + " s");
            configReportContent.addBullet("Frequency with which servers in a cluster check to see if there are any notifications or cache reset commands sent from other servers: " + pl.getProperty("processservercommand") + " s");
        } else {
            PropertyList refPl = this.refSysconfigRO.getServerPingOptions();
            configReportContent.addBullet("How often each LabVantage server updates the admin database to indicate that it is still alive: " + ConfigReportContent.getDiffString(pl.getProperty("serverheartbeat"), refPl.getProperty("serverheartbeat")) + " s");
            configReportContent.addBullet("Remove from the admin database any server that has not pinged for a time greater than the latency period. This setting should be approximately threes times the server ping interval: " + ConfigReportContent.getDiffString(pl.getProperty("serverlatency"), refPl.getProperty("serverlatency")) + " s");
            configReportContent.addBullet("Frequency with which servers in a cluster check to see if there are any notifications or cache reset commands sent from other servers: " + ConfigReportContent.getDiffString(pl.getProperty("processservercommand"), refPl.getProperty("processservercommand")) + " s");
        }
        return configReportContent;
    }

    public ConfigReportContent renderHousekeepingOptions() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "house keeping options");
        PropertyList pl = this.sysconfigRO.getHousekeepingOptions();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("LabVantage will retain the history of webpages visited by a user for : " + pl.getProperty("hkwebpagehistory") + " Days");
            configReportContent.addBullet("LabVantage will keep a copy of Bulletins stored in the database for : " + pl.getProperty("hkbulletins") + " Days");
            configReportContent.addBullet("LabVantage will retain the connection log of all connections made to the server : " + pl.getProperty("hkconnectionlog") + " Days");
            configReportContent.addBullet("LabVantage will retain the Department Assignment History for : " + pl.getProperty("hkdeptassignment") + " Days");
        } else {
            PropertyList refPl = this.refSysconfigRO.getHousekeepingOptions();
            configReportContent.addBullet("LabVantage will retain the history of webpages visited by a user for : " + ConfigReportContent.getDiffString(pl.getProperty("hkwebpagehistory"), refPl.getProperty("hkwebpagehistory")) + " Days");
            configReportContent.addBullet("LabVantage will keep a copy of Bulletins stored in the database for : " + ConfigReportContent.getDiffString(pl.getProperty("hkbulletins"), refPl.getProperty("hkbulletins")) + " Days");
            configReportContent.addBullet("LabVantage will retain the connection log of all connections made to the server : " + ConfigReportContent.getDiffString(pl.getProperty("hkconnectionlog"), refPl.getProperty("hkconnectionlog")) + " Days");
            configReportContent.addBullet("LabVantage will retain the Department Assignment History for : " + ConfigReportContent.getDiffString(pl.getProperty("hkdeptassignment"), refPl.getProperty("hkdeptassignment")) + " Days");
        }
        return configReportContent;
    }

    private ConfigReportContent renderCompleteTables() {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "sysconfig properties");
        DataSet sysconfigProps = this.sysconfigRO.getSysConfigProperties();
        DataSet profileProps = this.sysconfigRO.getProfileProperties();
        configReportContent.startSubHeading("SysConfig Properties", "");
        String[] keycols = new String[]{"propertyid"};
        if (this.includeDiffReport && this.refSysconfigRO != null) {
            configReportContent.renderDiffListTable(sysconfigProps, this.refSysconfigRO.getSysConfigProperties().copy(), keycols);
        } else {
            configReportContent.renderListTable(sysconfigProps, this.getTranslationProcessor());
        }
        configReportContent.startSubHeading("Profile Properties", "");
        if (this.includeDiffReport && this.refSysconfigRO != null) {
            configReportContent.renderDiffListTable(profileProps, this.refSysconfigRO.getProfileProperties().copy(), keycols);
        } else {
            configReportContent.renderListTable(profileProps, this.getTranslationProcessor());
        }
        return configReportContent;
    }

    public ConfigReportContent renderOverview() throws SapphireException {
        String db;
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "overview");
        PropertyList pl = this.sysconfigRO.getOverview();
        PropertyList refPl = new PropertyList();
        if (this.includeDiffReport && this.refRO != null) {
            refPl = this.refSysconfigRO.getOverview();
        }
        configReportContent.startBulletList();
        if (this.includeDiffReport && this.refSysconfigRO != null) {
            configReportContent.addBullet("Database: " + ConfigReportContent.getDiffString(pl.getProperty("database"), refPl.getProperty("database")));
        } else {
            configReportContent.addBullet("Database: " + pl.getProperty("database"));
        }
        String string = db = "Y".equals(pl.getProperty("isora", "Y")) ? "Oracle database" : "SQLServer database";
        if (this.includeDiffReport && this.refSysconfigRO != null) {
            String refdb = "Y".equals(refPl.getProperty("isora", "Y")) ? "Oracle database" : "SQLServer database";
            configReportContent.addDiffBullet(db, refdb);
        } else {
            configReportContent.addBullet(db);
        }
        if (this.includeDiffReport && this.refSysconfigRO != null) {
            configReportContent.addBullet("Server host name: " + ConfigReportContent.getDiffString(pl.getProperty("hostname"), refPl.getProperty("hostname")));
            configReportContent.addBullet("Http Port: " + ConfigReportContent.getDiffString(pl.getProperty("port"), refPl.getProperty("port")));
            configReportContent.addBullet("Platform: " + ConfigReportContent.getDiffString(pl.getProperty("platform"), refPl.getProperty("platform")));
            configReportContent.addBullet("LabVantage Home: " + ConfigReportContent.getDiffString(pl.getProperty("sapphirehome"), refPl.getProperty("sapphirehome")));
            configReportContent.addBullet("Build: " + ConfigReportContent.getDiffString(pl.getProperty("build"), refPl.getProperty("build")));
            configReportContent.addBullet("Server Info: " + ConfigReportContent.getDiffString(pl.getProperty("serverinfo"), refPl.getProperty("serverinfo")));
            configReportContent.addBullet("Is Cluster Enabled: " + ConfigReportContent.getDiffString(pl.getProperty("cluster"), refPl.getProperty("cluster")));
            configReportContent.addBullet("Cluster Name: " + ConfigReportContent.getDiffString(pl.getProperty("clustername"), refPl.getProperty("clustername")));
            configReportContent.addBullet("License file: " + ConfigReportContent.getDiffString(pl.getProperty("licensefile"), refPl.getProperty("licensefile")));
        } else {
            configReportContent.addBullet("Server host name: " + pl.getProperty("hostname"));
            configReportContent.addBullet("Http Port: " + pl.getProperty("port"));
            configReportContent.addBullet("Platform: " + pl.getProperty("platform"));
            configReportContent.addBullet("LabVantage Home: " + pl.getProperty("sapphirehome"));
            configReportContent.addBullet("Build: " + pl.getProperty("build"));
            configReportContent.addBullet("Server Info: " + pl.getProperty("serverinfo"));
            configReportContent.addBullet("Is Cluster Enabled: " + pl.getProperty("cluster"));
            configReportContent.addBullet("Cluster Name: " + pl.getProperty("clustername"));
            configReportContent.addBullet("License file: " + pl.getProperty("licensefile"));
        }
        PropertyList licProps = new PropertyList();
        licProps.setPropertyList(pl.getProperty("licenseproperties"), false, false);
        PropertyList refLicProps = null;
        if (this.includeDiffReport && this.refSysconfigRO != null) {
            refLicProps = new PropertyList();
            refLicProps.setPropertyList(refPl.getProperty("licenseproperties"), false, false);
        }
        configReportContent.addBullet("License Properties: " + this.renderLicenseProperties(licProps, refLicProps).toString());
        PropertyList sysProps = new PropertyList();
        sysProps.setPropertyList(pl.getProperty("sysproperties"), false, false);
        if (this.includeDiffReport && this.refSysconfigRO != null) {
            PropertyList refsysprops = new PropertyList();
            refsysprops.setPropertyList(refPl.getProperty("sysproperties"), false, false);
            configReportContent.addBullet("Java System Properties: " + configReportContent.renderPropertyListDiff(sysProps, refsysprops, true, this.getTranslationProcessor()));
        } else {
            configReportContent.addBullet("Java System Properties: " + configReportContent.renderPropertyList(sysProps, true).toString());
        }
        configReportContent.endBulletList();
        return configReportContent;
    }

    private ConfigReportContent renderLicenseProperties(PropertyList licProps, PropertyList refLicProps) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "license properties");
        configReportContent.startTable();
        configReportContent.startRow();
        if (refLicProps == null) {
            configReportContent.addRowItem("Licensed to: ", licProps.getProperty("licensedto"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Server Count: ", licProps.getProperty("servercount"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Concurrent Users: ", licProps.getProperty("concurrentusers"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Database Count: ", licProps.getProperty("databasecount"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("License Expiry Date: ", licProps.getProperty("expirydate"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Named Users: ", licProps.getProperty("namedusers"));
            configReportContent.endRow();
            Object[] keyes = licProps.keySet().toArray();
            for (int i = 0; i < licProps.size(); ++i) {
                if (!keyes[i].toString().toLowerCase().startsWith("module_")) continue;
                configReportContent.startRow();
                configReportContent.addRowItem("Access to Module " + keyes[i].toString().substring("module_".length()) + ": ", licProps.getProperty(keyes[i].toString()));
                configReportContent.endRow();
            }
            configReportContent.startRow();
            configReportContent.addRowItem("R4 Install: ", licProps.getProperty("r4install"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Virtual Page Count: ", licProps.getProperty("virtualpagecount"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Virtual User Pages: ", licProps.getProperty("virtualuserpages"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Virtual Users: ", licProps.getProperty("virtualusers"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Virtual Concurrent Users: ", licProps.getProperty("virtualconcurrentusers"));
            configReportContent.endRow();
        } else {
            configReportContent.addDiffRowItem("Licensed to: ", licProps.getProperty("licensedto"), refLicProps.getProperty("licensedto"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Server Count: ", licProps.getProperty("servercount"), refLicProps.getProperty("servercount"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Concurrent Users: ", licProps.getProperty("concurrentusers"), refLicProps.getProperty("concurrentusers"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Database Count: ", licProps.getProperty("databasecount"), refLicProps.getProperty("databasecount"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("License Expiry Date: ", licProps.getProperty("expirydate"), refLicProps.getProperty("expirydate"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Named Users: ", licProps.getProperty("namedusers"), refLicProps.getProperty("namedusers"));
            configReportContent.endRow();
            Object[] keyes = licProps.keySet().toArray();
            for (int i = 0; i < licProps.size(); ++i) {
                if (!keyes[i].toString().toLowerCase().startsWith("module_")) continue;
                configReportContent.startRow();
                configReportContent.addDiffRowItem("Access to Module " + keyes[i].toString().substring("module_".length()) + ": ", licProps.getProperty(keyes[i].toString()), refLicProps.getProperty(keyes[i].toString()));
                configReportContent.endRow();
            }
            configReportContent.startRow();
            configReportContent.addDiffRowItem("R4 Install: ", licProps.getProperty("r4install"), refLicProps.getProperty("r4install"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Virtual Page Count: ", licProps.getProperty("virtualpagecount"), refLicProps.getProperty("virtualpagecount"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Virtual User Pages: ", licProps.getProperty("virtualuserpages"), refLicProps.getProperty("virtualuserpages"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Virtual Users: ", licProps.getProperty("virtualusers"), refLicProps.getProperty("virtualusers"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Virtual Concurrent Users: ", licProps.getProperty("virtualconcurrentusers"), refLicProps.getProperty("virtualconcurrentusers"));
            configReportContent.endRow();
        }
        configReportContent.endTable();
        return configReportContent;
    }

    public void createXMLReport() throws SapphireException {
        if (this.sysconfigRO != null) {
            FileOutputStream sdiXMLFile;
            String xmlReportContent = this.sysconfigRO.getProfileProperties().toXML();
            String xmlFileName = ConfigReportContent.generateSectionXMLFileName("SysConfig", "profileprops");
            try {
                sdiXMLFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlFileName);
            }
            try {
                sdiXMLFile.write(xmlReportContent.getBytes());
                sdiXMLFile.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a profilepros file");
            }
            xmlReportContent = this.sysconfigRO.getSysConfigProperties().toXML();
            xmlFileName = ConfigReportContent.generateSectionXMLFileName("SysConfig", "sysconfigprops");
            try {
                sdiXMLFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlFileName);
            }
            try {
                sdiXMLFile.write(xmlReportContent.getBytes());
                sdiXMLFile.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a sysconfigprops file");
            }
            xmlReportContent = this.sysconfigRO.getOverview().toXMLString();
            xmlFileName = ConfigReportContent.generateSectionXMLFileName("SysConfig", "overviewprops");
            try {
                sdiXMLFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlFileName);
            }
            try {
                sdiXMLFile.write(xmlReportContent.getBytes());
                sdiXMLFile.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a overviewprops file");
            }
            xmlReportContent = this.sysconfigRO.getConfigurationProperties().toXMLString();
            xmlFileName = ConfigReportContent.generateSectionXMLFileName("SysConfig", "configprops");
            try {
                sdiXMLFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlFileName);
            }
            try {
                sdiXMLFile.write(xmlReportContent.getBytes());
                sdiXMLFile.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a configprops file");
            }
        }
    }

    public ConfigReportContent renderWorkflowConfiguration() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "workflow configuration");
        PropertyList pl = this.sysconfigRO.getWorkFlowOptions();
        configReportContent.startBulletList();
        if (!this.includeDiffReport || this.refSysconfigRO == null) {
            configReportContent.addBullet("The times workflow  will retry  is     : " + pl.getProperty("maxtriggerretry"));
            configReportContent.addBullet("Time to wait between retry attempts is : " + pl.getProperty("triggerdelay") + " seconds.");
        } else {
            PropertyList refPl = this.refSysconfigRO.getWorkFlowOptions();
            configReportContent.addBullet("The times workflow  will retry is : " + ConfigReportContent.getDiffString(pl.getProperty("maxtriggerretry"), refPl.getProperty("maxtriggerretry")));
            configReportContent.addBullet("Time to wait between retry attempts is : " + ConfigReportContent.getDiffString(pl.getProperty("triggerdelay"), refPl.getProperty("triggerdelay")) + " seconds.");
        }
        configReportContent.endBulletList();
        return configReportContent;
    }
}

