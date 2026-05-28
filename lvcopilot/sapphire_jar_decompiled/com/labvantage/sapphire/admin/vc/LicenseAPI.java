/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.labvantage.sapphire.admin.command.license.LicenseGenerate
 */
package com.labvantage.sapphire.admin.vc;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.command.license.LicenseGenerate;
import java.util.HashMap;
import java.util.Properties;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class LicenseAPI {
    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_VERSION_LV7 = "LV7";
    public static final String PROPERTY_VERSION_LV8 = "LV8";
    public static final String PROPERTY_VERSION_LV81 = "LV81";
    public static final String PROPERTY_VERSION_LV82 = "LV82";
    public static final String PROPERTY_VERSION_LV83 = "LV83";
    public static final String PROPERTY_VERSION_LV84 = "LV84";
    public static final String PROPERTY_VERSION_LV85 = "LV85";
    public static final String PROPERTY_VERSION_LV86 = "LV86";
    public static final String PROPERTY_VERSION_LV87 = "LV87";
    public static final String PROPERTY_VERSION_LV88 = "LV88";
    public static final String PROPERTY_COMMAND = "command";
    public static final String PROPERTY_GENERATE_DIR = "dir";
    public static final String PROPERTY_GENERATE_TEMPLATE = "template";
    public static final String PROPERTY_GENERATE_FILE = "file";
    public static final String PROPERTY_CONVERT_DIR = "dir";
    public static final String PROPERTY_CONVERT_SOURCE = "source";
    public static final String PROPERTY_CONVERT_FILE = "file";
    public static final String PROPERTY_DECODE_DIR = "dir";
    public static final String PROPERTY_DECODE_FILE = "file";
    public static final String PROPERTY_PASSWORD_USERNAME = "username";
    public static final String PROPERTY_PASSWORD_DAYS = "days";
    public static final String PROPERTY_TEMPLATE_DIR = "dir";
    public static final String PROPERTY_TEMPLATE_FILE = "file";
    public static final String KEY_licenseid = "licensekeyid";
    public static final String KEY_licensedto = "licensedto";
    public static final String KEY_licensetype = "licensedto";
    public static final String KEY_phonehome = "phonehome";
    public static final String KEY_phonehomeurl = "phonehomeurl";
    public static final String KEY_expressinstall = "expressinstall";
    public static final String KEY_expirydate = "expirydate";
    public static final String KEY_namedusers = "namedusers";
    public static final String KEY_concurrentusers = "concurrentusers";
    public static final String KEY_noncoreexternalappcount = "noncoreexternalappcount";
    public static final String KEY_sdmsinstrumentcount = "sdmsinstrumentcount";
    public static final String KEY_expressusers = "expressusers";
    public static final String KEY_expressconcurrentusers = "expressconcurrentusers";
    public static final String KEY_portalusers = "portalnamedusers";
    public static final String KEY_portalconcurrentusers = "portalconcurrentusers";
    public static final String KEY_virtualusers = "virtualusers";
    public static final String KEY_virtualconcurrentusers = "virtualusers";
    public static final String KEY_somusers = "somusers";
    public static final String KEY_databasecount = "databasecount";
    public static final String KEY_servercount = "servercount";
    public static final String KEY_virtualpagecount = "virtualpagecount";
    public static final String KEY_virtualformcount = "virtualformcount";
    public static final String KEY_virtualuserpages = "virtualuserpages";
    public static final String KEY_api = "api";
    public static final String KEY_saas = "saas";
    public static final String KEY_r4install = "r4install";
    public static final String KEY_module_AdhocQuery = "module_AdhocQuery";
    public static final String KEY_module_AQC = "module_AQC";
    public static final String KEY_module_ASL = "module_ASL";
    public static final String KEY_module_BatchManagement = "module_BatchManagement";
    public static final String KEY_module_Formulations = "module_Formulations";
    public static final String KEY_module_BBEDC = "module_BBEDC";
    public static final String KEY_module_BBProtocol = "module_BBProtocol";
    public static final String KEY_module_CAPA = "module_CAPA";
    public static final String KEY_module_ConfigReport = "module_ConfigReport";
    public static final String KEY_module_Dashboard = "module_Dashboard";
    public static final String KEY_module_Empower = "module_Empower";
    public static final String KEY_module_eNotebook = "module_eNotebook";
    public static final String KEY_module_eSubmission = "module_eSubmission";
    public static final String KEY_module_ELN = "module_ELN";
    public static final String KEY_module_LES = "module_LES";
    public static final String KEY_module_WAP = "module_WAP";
    public static final String KEY_module_Portal = "module_Portal";
    public static final String KEY_module_Kits = "module_Kits";
    public static final String KEY_module_NWA = "module_NWA";
    public static final String KEY_module_RSM = "module_RSM";
    public static final String KEY_module_SampleMonitoring = "module_SampleMonitoring";
    public static final String KEY_module_SDMS = "module_SDMS";
    public static final String KEY_module_SEC = "module_SEC";
    public static final String KEY_module_SMS = "module_SMS";
    public static final String KEY_module_Stability = "module_Stability";
    public static final String KEY_module_WPDPro = "module_WPDPro";
    public static final String KEY_module_WPDStd = "module_WPDStd";
    public static final String KEY_app_SubmissionRequestApp = "app_SubmissionRequestApp";
    public static final String KEY_app_KitRequestApp = "app_KitRequestApp";

    public static void generate(PropertyList properties) throws SapphireException {
        LicenseGenerate commandClass = new LicenseGenerate();
        commandClass.setDefaults(new Properties());
        commandClass.setCommandParams((HashMap)properties);
        commandClass.setVerbose(true);
        Trace.logInfo("Processing command: " + commandClass.getCommandName() + "\n");
        long start = System.currentTimeMillis();
        commandClass.processCommand();
        Trace.logInfo("Complete (" + (System.currentTimeMillis() - start) / 1000L + " secs)");
    }
}

