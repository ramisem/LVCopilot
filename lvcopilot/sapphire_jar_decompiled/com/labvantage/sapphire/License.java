/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.EncryptDecrypt;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import sapphire.SapphireException;

public class License {
    public static final String LICENSEID = "licensekeyid";
    public static final String LICENSED_TO = "licensedto";
    public static final String LICENSE_TYPE = "licensetype";
    public static final String PHONE_HOME = "phonehome";
    public static final String PHONE_HOME_URL = "phonehomeurl";
    public static final String NAMED_USERS = "namedusers";
    public static final String CONCURRENT_USERS = "concurrentusers";
    public static final String VIRTUAL_USERS = "virtualusers";
    public static final String VIRTUALCONCURRENT_USERS = "virtualconcurrentusers";
    public static final String PORTALNAMED_USERS = "portalnamedusers";
    public static final String PORTALCONCURRENT_USERS = "portalconcurrentusers";
    public static final String EXPRESS_USERS = "expressusers";
    public static final String EXPRESSCONCURRENT_USERS = "expressconcurrentusers";
    public static final String NONCOREEXTERNALAPP_COUNT = "noncoreexternalappcount";
    public static final String SDMS_INSTRUMENT_COUNT = "sdmsinstrumentcount";
    public static final String SOM_USERS = "somusers";
    public static final String VIRTUAL_PAGE_COUNT = "virtualpagecount";
    public static final String VIRTUAL_FORM_COUNT = "virtualformcount";
    public static final String DATABASE_COUNT = "databasecount";
    public static final String API = "api";
    public static final String EXPRESS_INSTALL = "expressinstall";
    public static final String R4_INSTALL = "r4install";
    public static final String SAAS = "saas";
    public static final String VIRTUAL_USER_PAGES = "virtualuserpages";
    public static final String EXPIRY_DATE = "expirydate";
    public static final String SERVER_COUNT = "servercount";
    public static final String MODULE_PREFIX = "module_";
    public static final String APP_PREFIX = "app_";
    private Properties licenseProps = new Properties();

    public static String[][] getLicenseProperties() {
        return new String[][]{{LICENSEID, ""}, {LICENSED_TO, ""}, {LICENSE_TYPE, ""}, {PHONE_HOME, "Y"}, {PHONE_HOME_URL, ""}, {EXPRESS_INSTALL, "N"}, {EXPIRY_DATE, "(none)"}, {NAMED_USERS, "0"}, {CONCURRENT_USERS, "0"}, {EXPRESS_USERS, "0"}, {EXPRESSCONCURRENT_USERS, "0"}, {PORTALNAMED_USERS, "0"}, {PORTALCONCURRENT_USERS, "0"}, {NONCOREEXTERNALAPP_COUNT, "0"}, {SDMS_INSTRUMENT_COUNT, "0"}, {VIRTUAL_USERS, "0"}, {VIRTUALCONCURRENT_USERS, "0"}, {SOM_USERS, "0"}, {DATABASE_COUNT, "2"}, {SERVER_COUNT, "1"}, {VIRTUAL_PAGE_COUNT, "0"}, {VIRTUAL_FORM_COUNT, "0"}, {VIRTUAL_USER_PAGES, ""}, {API, "Y"}, {R4_INSTALL, "Y"}, {SAAS, "N"}};
    }

    public static String[][] getModules() {
        return new String[][]{{"AdhocQuery", "Adhoc Query Module"}, {"AQC", "Analytical Quality Control"}, {"ASL", "Advanced Storage and Logistics"}, {"BatchManagement", "Batch processing with SamplePlanning, Reduced Testing and SkipLot"}, {"Formulations", "Formulation Management"}, {"BBEDC", "BioBanking Electronic Forms"}, {"BBProtocol", "BioBanking Protocol Management"}, {"CAPA", "CAPA"}, {"ConfigReport", "Configuration Reporting"}, {"Dashboard", "Dashboard Analytics Module"}, {"Empower", "Empower Connector"}, {"eNotebook", "Electronic Notebook"}, {"eSubmission", "Electronic Forms"}, {"ELN", "Electronic Lab Notebook"}, {"LES", "Lab Execution System"}, {"Kits", "Kit Management"}, {"NWA", "NWA"}, {"RSM", "Reagent and Standards Management Module"}, {"SampleMonitoring", "Sample Monitoring"}, {"SDMS", "Scientific Data Management System Module"}, {"SEC", "Sapphire Enterprise Connector Module"}, {"SMS", "Sample Management System"}, {"Stability", "Stability module"}, {"WAP", "Work Activity Planning"}, {"Portal", "Portal"}, {"WPDPro", "Evergreen Studio Web Page Designer Professional Edition"}, {"WPDStd", "Evergreen Studio Web Page Designer Standard Edition"}};
    }

    public static String[][] getApps() {
        return new String[][]{{"SubmissionRequestApp", "Submission Request App"}, {"KitRequestApp", "Kit Request App"}};
    }

    public License(String licenseText) throws SapphireException {
        this.loadLicense(licenseText);
        String errors = License.validateLicense(this.licenseProps);
        if (errors.length() > 0) {
            throw new SapphireException(errors);
        }
    }

    public License(File licenseFile) throws SapphireException {
        this.loadLicense(licenseFile);
        String errors = License.validateLicense(this.licenseProps);
        if (errors.length() > 0) {
            throw new SapphireException(errors);
        }
    }

    public String getProperty(String propertyid) {
        String value = this.licenseProps.getProperty(propertyid);
        return value != null ? value : "";
    }

    public Properties getProperties() {
        return this.licenseProps;
    }

    private void loadLicense(File licenseFile) throws SapphireException {
        try {
            String licensefile = License.loadLicenseString(licenseFile);
            this.loadLicense(licensefile);
        }
        catch (Exception ioe) {
            throw new SapphireException("Invalid LabVantage license. Exception: " + ioe.getMessage(), ioe);
        }
    }

    private void loadLicense(String licensefile) throws SapphireException {
        try {
            String properties = "";
            int pos = licensefile.indexOf("##");
            if (pos >= 0) {
                properties = licensefile.indexOf("##", pos + 1) > 0 ? License.decrypt(licensefile.substring(licensefile.indexOf("##", pos + 1)).replaceAll("\r\n", "")) : EncryptDecrypt.decrypt(licensefile.substring(pos + 3));
            } else {
                throw new IOException("License file token ## not found");
            }
            ByteArrayInputStream licensePropsStream = new ByteArrayInputStream(properties.getBytes());
            this.licenseProps.load(licensePropsStream);
        }
        catch (Exception ioe) {
            throw new SapphireException("Invalid Sapphire license - error reading file. Exception: " + ioe.getMessage(), ioe);
        }
    }

    public static String validateLicense(Properties licenseProps) {
        String value;
        boolean expressInstall;
        StringBuffer errors = new StringBuffer();
        String prefix = "Invalid LabVantage license file - ";
        if (licenseProps.getProperty(LICENSED_TO, "").length() == 0) {
            errors.append(prefix).append("licensedto property not defined");
        }
        if (licenseProps.getProperty(R4_INSTALL) == null || licenseProps.getProperty(R4_INSTALL).length() == 0) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("r4install property not defined");
        }
        if (expressInstall = (value = licenseProps.getProperty(EXPRESS_INSTALL, "N")).equals("Y")) {
            licenseProps.setProperty(NAMED_USERS, "1");
            licenseProps.setProperty(CONCURRENT_USERS, "0");
        } else {
            licenseProps.setProperty(EXPRESS_INSTALL, "N");
        }
        try {
            if (licenseProps.getProperty(NAMED_USERS, "").length() == 0) {
                licenseProps.setProperty(NAMED_USERS, "0");
            } else {
                Integer.parseInt(licenseProps.getProperty(NAMED_USERS, "0"));
            }
        }
        catch (NumberFormatException nfe) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("namedusers invalid");
        }
        try {
            if (licenseProps.getProperty(CONCURRENT_USERS, "").length() == 0) {
                licenseProps.setProperty(CONCURRENT_USERS, "0");
            } else {
                Integer.parseInt(licenseProps.getProperty(CONCURRENT_USERS, "0"));
            }
        }
        catch (NumberFormatException nfe) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("concurrentusers invalid");
        }
        try {
            if (licenseProps.getProperty(EXPRESS_USERS, "").length() == 0) {
                licenseProps.setProperty(EXPRESS_USERS, "0");
            } else {
                Integer.parseInt(licenseProps.getProperty(EXPRESS_USERS, "0"));
            }
        }
        catch (NumberFormatException nfe) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("expressusers invalid");
        }
        try {
            if (licenseProps.getProperty(EXPRESSCONCURRENT_USERS, "").length() == 0) {
                licenseProps.setProperty(EXPRESSCONCURRENT_USERS, "0");
            } else {
                Integer.parseInt(licenseProps.getProperty(EXPRESSCONCURRENT_USERS, "0"));
            }
        }
        catch (NumberFormatException nfe) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("expressconcurrentusers invalid");
        }
        try {
            if (licenseProps.getProperty(NONCOREEXTERNALAPP_COUNT, "").length() == 0) {
                licenseProps.setProperty(NONCOREEXTERNALAPP_COUNT, "0");
            } else {
                Integer.parseInt(licenseProps.getProperty(NONCOREEXTERNALAPP_COUNT, "0"));
            }
        }
        catch (NumberFormatException nfe) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("noncoreexternalappcount invalid");
        }
        try {
            if (licenseProps.getProperty(SDMS_INSTRUMENT_COUNT, "").length() == 0) {
                licenseProps.setProperty(SDMS_INSTRUMENT_COUNT, "0");
            } else {
                Integer.parseInt(licenseProps.getProperty(SDMS_INSTRUMENT_COUNT, "0"));
            }
        }
        catch (NumberFormatException nfe) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("sdmsinstrumentcount invalid");
        }
        try {
            if (licenseProps.getProperty(VIRTUAL_USERS, "").length() == 0) {
                licenseProps.setProperty(VIRTUAL_USERS, "0");
            } else {
                Integer.parseInt(licenseProps.getProperty(VIRTUAL_USERS, "0"));
            }
        }
        catch (NumberFormatException nfe) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("virtualusers invalid");
        }
        try {
            if (licenseProps.getProperty(VIRTUALCONCURRENT_USERS, "").length() == 0) {
                licenseProps.setProperty(VIRTUALCONCURRENT_USERS, "0");
            } else {
                Integer.parseInt(licenseProps.getProperty(VIRTUALCONCURRENT_USERS, "0"));
            }
        }
        catch (NumberFormatException nfe) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("virtualconcurrentusers invalid");
        }
        try {
            if (licenseProps.getProperty(PORTALNAMED_USERS, "").length() == 0) {
                licenseProps.setProperty(PORTALNAMED_USERS, "0");
            } else {
                Integer.parseInt(licenseProps.getProperty(PORTALNAMED_USERS, "0"));
            }
        }
        catch (NumberFormatException nfe) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("portalnamedusers invalid");
        }
        try {
            if (licenseProps.getProperty(PORTALCONCURRENT_USERS, "").length() == 0) {
                licenseProps.setProperty(PORTALCONCURRENT_USERS, "0");
            } else {
                Integer.parseInt(licenseProps.getProperty(PORTALCONCURRENT_USERS, "0"));
            }
        }
        catch (NumberFormatException nfe) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("portalconcurrentusers invalid");
        }
        try {
            if (licenseProps.getProperty(VIRTUAL_PAGE_COUNT, "").length() == 0) {
                licenseProps.setProperty(VIRTUAL_PAGE_COUNT, "0");
            } else {
                Integer.parseInt(licenseProps.getProperty(VIRTUAL_PAGE_COUNT, "0"));
            }
        }
        catch (NumberFormatException nfe) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("virtualpagecount invalid");
        }
        try {
            if (licenseProps.getProperty(VIRTUAL_FORM_COUNT, "").length() == 0) {
                licenseProps.setProperty(VIRTUAL_FORM_COUNT, "0");
            } else {
                Integer.parseInt(licenseProps.getProperty(VIRTUAL_FORM_COUNT, "0"));
            }
        }
        catch (NumberFormatException nfe) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("virtualformcount invalid");
        }
        try {
            if (licenseProps.getProperty(DATABASE_COUNT, "").length() == 0) {
                licenseProps.setProperty(DATABASE_COUNT, "2");
            } else {
                Integer.parseInt(licenseProps.getProperty(DATABASE_COUNT, "2"));
            }
        }
        catch (Exception nfe) {
            errors.append(errors.length() == 0 ? prefix : ", ").append("databasecount invalid");
        }
        String expirydate = licenseProps.getProperty(EXPIRY_DATE);
        if (expirydate != null && expirydate.length() > 0 && !expirydate.equals("(none)") && !expirydate.equals("(null)")) {
            String[] dateparts = License.split(expirydate, " ");
            if (dateparts.length == 3) {
                try {
                    Integer.parseInt(dateparts[0].substring(1));
                    Integer.parseInt(dateparts[1].substring(1));
                    Integer.parseInt(dateparts[2].substring(1));
                }
                catch (NumberFormatException nfe) {
                    errors.append(errors.length() == 0 ? prefix : ", ").append("expirydate format incorrect - expected Ddd Mmm Yyy");
                }
            } else {
                dateparts = License.split(expirydate, "-");
                if (dateparts.length == 3) {
                    try {
                        Integer.parseInt(dateparts[0]);
                        Integer.parseInt(dateparts[1]);
                        Integer.parseInt(dateparts[2]);
                    }
                    catch (NumberFormatException nfe) {
                        errors.append(errors.length() == 0 ? prefix : ", ").append("expirydate format incorrect - expected dd-mm-yy");
                    }
                } else {
                    errors.append(errors.length() == 0 ? prefix : ", ").append("expirydate format incorrect - expected dd-mm-yy");
                }
            }
        }
        String[][] modules = License.getModules();
        for (int i = 0; i < modules.length; ++i) {
            String count = licenseProps.getProperty(MODULE_PREFIX + modules[i][0], "");
            if (count.length() == 0) {
                count = licenseProps.getProperty((MODULE_PREFIX + modules[i][0]).toLowerCase(), "");
            }
            if (count.length() == 0) {
                licenseProps.setProperty(MODULE_PREFIX + modules[i][0], "0");
                continue;
            }
            if (count.equals("(null)")) {
                licenseProps.setProperty(MODULE_PREFIX + modules[i][0], "0");
                continue;
            }
            if (count.equals("S") || count.equals("U")) continue;
            try {
                Integer.parseInt(licenseProps.getProperty(MODULE_PREFIX + modules[i][0], "0"));
                continue;
            }
            catch (NumberFormatException nfe) {
                errors.append(errors.length() == 0 ? prefix : ", ").append(modules[i][0] + " invalid");
            }
        }
        String[][] apps = License.getApps();
        for (int i = 0; i < apps.length; ++i) {
            String count = licenseProps.getProperty(APP_PREFIX + apps[i][0], "");
            if (count.length() == 0) {
                count = licenseProps.getProperty((APP_PREFIX + apps[i][0]).toLowerCase(), "");
            }
            if (count.length() == 0) {
                licenseProps.setProperty(APP_PREFIX + apps[i][0], "0");
                continue;
            }
            if (count.equals("(null)")) {
                licenseProps.setProperty(APP_PREFIX + apps[i][0], "0");
                continue;
            }
            if (count.equals("S") || count.equals("U")) continue;
            try {
                Integer.parseInt(licenseProps.getProperty(APP_PREFIX + apps[i][0], "0"));
                continue;
            }
            catch (NumberFormatException nfe) {
                errors.append(errors.length() == 0 ? prefix : ", ").append(apps[i][0] + " invalid");
            }
        }
        return errors.toString();
    }

    public boolean isLicenseExpired() {
        block9: {
            String expirydate = this.getProperty(EXPIRY_DATE);
            if (expirydate != null && expirydate.length() > 0 && !expirydate.equals("(none)")) {
                String[] dateparts = License.split(expirydate, " ");
                if (dateparts.length == 3) {
                    Calendar cal = Calendar.getInstance();
                    try {
                        cal.set(Integer.parseInt(dateparts[2].substring(1)), Integer.parseInt(dateparts[1].substring(1)) - 1, Integer.parseInt(dateparts[0].substring(1)), 0, 0, 0);
                        if (cal.before(Calendar.getInstance())) {
                            return true;
                        }
                        break block9;
                    }
                    catch (NumberFormatException nfe) {
                        return false;
                    }
                }
                dateparts = License.split(expirydate, "-");
                if (dateparts.length == 3) {
                    Calendar cal = Calendar.getInstance();
                    try {
                        cal.set(Integer.parseInt(dateparts[2]), Integer.parseInt(dateparts[1]) - 1, Integer.parseInt(dateparts[0]), 0, 0, 0);
                        if (cal.before(Calendar.getInstance())) {
                            return true;
                        }
                    }
                    catch (NumberFormatException nfe) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public boolean isExpress() {
        return this.licenseProps.getProperty(EXPRESS_INSTALL, "N").equals("Y");
    }

    public boolean isSaaS() {
        return "Y".equals(this.licenseProps.getProperty(SAAS, "N"));
    }

    public int getNamedUsers() {
        return Integer.parseInt(this.licenseProps.getProperty(NAMED_USERS, "0"));
    }

    public int getExpressUsers() {
        return Integer.parseInt(this.licenseProps.getProperty(EXPRESS_USERS, "0"));
    }

    public int getNonCoreExternalAppCount() {
        return Integer.parseInt(this.licenseProps.getProperty(NONCOREEXTERNALAPP_COUNT, "0"));
    }

    public int getSDMSInstrumentCount() {
        return Integer.parseInt(this.licenseProps.getProperty(SDMS_INSTRUMENT_COUNT, "0"));
    }

    public int getVirtualUsers() {
        return Integer.parseInt(this.licenseProps.getProperty(VIRTUAL_USERS, "0"));
    }

    public int getPortalNamedUsers() {
        return Integer.parseInt(this.licenseProps.getProperty(PORTALNAMED_USERS, "0"));
    }

    public int getConcurrentUsers() {
        return Integer.parseInt(this.licenseProps.getProperty(CONCURRENT_USERS, "0"));
    }

    public int getExpressConcurrentUsers() {
        return Integer.parseInt(this.licenseProps.getProperty(EXPRESSCONCURRENT_USERS, "0"));
    }

    public String getLicenseid() {
        return this.licenseProps.getProperty(LICENSEID, "");
    }

    public int getVirtualConcurrentUsers() {
        return Integer.parseInt(this.licenseProps.getProperty(VIRTUALCONCURRENT_USERS, "0"));
    }

    public int getPortalConcurrentUsers() {
        return Integer.parseInt(this.licenseProps.getProperty(PORTALCONCURRENT_USERS, "0"));
    }

    public int getVirtualPageCount() {
        return Integer.parseInt(this.licenseProps.getProperty(VIRTUAL_PAGE_COUNT, "0"));
    }

    public int getVirtualFormCount() {
        return Integer.parseInt(this.licenseProps.getProperty(VIRTUAL_FORM_COUNT, "0"));
    }

    public int getDatabaseCount() {
        return Integer.parseInt(this.licenseProps.getProperty(DATABASE_COUNT, "2"));
    }

    public String[] getModuleList() {
        ArrayList<String> modules = new ArrayList<String>();
        for (String string : this.licenseProps.keySet()) {
            if (!string.startsWith(MODULE_PREFIX)) continue;
            modules.add(string.substring(7));
        }
        return modules.toArray(new String[0]);
    }

    public Properties getModuleProperties() {
        Properties moduleProperties = new Properties();
        for (String string : this.licenseProps.keySet()) {
            if (!string.startsWith(MODULE_PREFIX)) continue;
            moduleProperties.setProperty(string.substring(7), this.licenseProps.getProperty(string));
        }
        return moduleProperties;
    }

    public String[] getAppList() {
        ArrayList<String> apps = new ArrayList<String>();
        for (String string : this.licenseProps.keySet()) {
            if (!string.startsWith(APP_PREFIX)) continue;
            apps.add(string.substring(APP_PREFIX.length()));
        }
        return apps.toArray(new String[0]);
    }

    public Properties getAppProperties() {
        Properties appProperties = new Properties();
        for (String string : this.licenseProps.keySet()) {
            if (!string.startsWith(APP_PREFIX)) continue;
            appProperties.setProperty(string.substring(APP_PREFIX.length()), this.licenseProps.getProperty(string));
        }
        return appProperties;
    }

    public static String loadLicenseString(File licenseFile) throws Exception {
        int len = new Long(licenseFile.length()).intValue();
        FileReader fileReader = new FileReader(licenseFile);
        char[] buf = new char[len];
        fileReader.read(buf, 0, len);
        return new String(buf);
    }

    public static String decrypt(String text) throws Exception {
        byte[] encryptText = Base64.decodeBase64((String)text);
        SecretKeySpec skeySpec = new SecretKeySpec(Base64.decodeBase64((String)"YQaFsH9Q98CKm2HjfWQw7g=="), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(2, skeySpec);
        return new String(cipher.doFinal(encryptText));
    }

    public static String[] split(String input, String delimeter) {
        ArrayList<String> tokenlist = new ArrayList<String>();
        if (input != null) {
            if (delimeter != null) {
                int pos = input.indexOf(delimeter);
                if (pos == -1) {
                    tokenlist.add(input);
                } else {
                    int offset = 0;
                    while (pos > -1) {
                        tokenlist.add(input.substring(offset, pos));
                        offset = pos + delimeter.length();
                        pos = input.indexOf(delimeter, offset);
                    }
                    tokenlist.add(input.substring(offset, input.length()));
                }
            } else {
                tokenlist.add(input);
            }
        }
        String[] tokens = new String[tokenlist.size()];
        for (int i = 0; i < tokenlist.size(); ++i) {
            tokens[i] = (String)tokenlist.get(i);
        }
        return tokens;
    }

    public boolean equals(License compareTo) {
        boolean equals = false;
        if (compareTo.getLicenseid().equals(this.getLicenseid())) {
            equals = true;
        }
        return equals;
    }
}

