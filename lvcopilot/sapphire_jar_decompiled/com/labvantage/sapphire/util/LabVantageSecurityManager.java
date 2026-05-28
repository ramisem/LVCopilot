/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.platform.Configuration;
import java.io.File;
import java.io.FileDescriptor;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.util.StringUtil;

public class LabVantageSecurityManager
extends SecurityManager {
    static final String classLoader = "com.labvantage.sapphire.util.LabVantageClassLoader";
    private static Path appHome = null;
    private static Path labVantageHome = null;
    private static Path tempShort = null;
    private static Path tempFull = null;
    private static JSONObject securitySettings = LabVantageSecurityManager.getSecuritySettings();
    private ArrayList<String> errorStack = new ArrayList();

    private static boolean getBoolSetting(String setting, boolean defaultSetting) {
        try {
            return securitySettings.getBoolean(setting);
        }
        catch (Exception e) {
            return defaultSetting;
        }
    }

    private static JSONArray getJSONArraySetting(String setting) {
        try {
            return securitySettings.getJSONArray(setting);
        }
        catch (Exception e) {
            return new JSONArray();
        }
    }

    public List<String> getErrorStack() {
        return this.errorStack;
    }

    private void doError(String msg) throws SecurityException {
        Trace.logError("LSM EXCEPTION - " + msg);
        this.errorStack.add(msg);
        throw new SecurityException(msg);
    }

    private static boolean hasValue(JSONArray array, String value, boolean ignoreCase) {
        for (int i = 0; i < array.length(); ++i) {
            try {
                String v;
                String string = v = array.get(i) != null && array.get(i) instanceof String ? array.getString(i) : "";
                if (v.length() <= 0 || (!ignoreCase || !v.equalsIgnoreCase(value)) && (ignoreCase || !v.equals(value))) continue;
                return true;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return false;
    }

    public static void addFilePathException(String path, boolean read, boolean write, boolean delete) {
        String pathFinal = Paths.get(path, new String[0]).toString() + System.getProperty("file.separator") + "*";
        if (securitySettings != null) {
            if (read) {
                try {
                    JSONArray allowFileReads = securitySettings.getJSONArray("allowedFileReads");
                    if (allowFileReads != null && !LabVantageSecurityManager.hasValue(allowFileReads, pathFinal, true)) {
                        allowFileReads.put(pathFinal);
                    }
                }
                catch (Exception e) {
                    Trace.logError("Failed to update path exception for read");
                }
            }
            if (write) {
                try {
                    JSONArray allowedFileWrites = securitySettings.getJSONArray("allowedFileWrites");
                    if (allowedFileWrites != null && !LabVantageSecurityManager.hasValue(allowedFileWrites, pathFinal, true)) {
                        allowedFileWrites.put(pathFinal);
                    }
                }
                catch (Exception e) {
                    Trace.logError("Failed to update path exception for write");
                }
            }
            if (delete) {
                try {
                    JSONArray allowedFileDeletes = securitySettings.getJSONArray("allowedFileDeletes");
                    if (allowedFileDeletes != null && !LabVantageSecurityManager.hasValue(allowedFileDeletes, pathFinal, true)) {
                        allowedFileDeletes.put(pathFinal);
                    }
                }
                catch (Exception e) {
                    Trace.logError("Failed to update path exception for delete");
                }
            }
        }
    }

    public static JSONArray getClassesBlackList() {
        return LabVantageSecurityManager.getJSONArraySetting("classesBlackList");
    }

    public static JSONArray getClassesWhiteList() {
        return LabVantageSecurityManager.getJSONArraySetting("classesWhiteList");
    }

    private static JSONObject getSecuritySettings() {
        JSONObject customSecuritySettings = null;
        JSONObject productSecuritySettings = null;
        Path appHome = LabVantageSecurityManager.getApplicationHome();
        File file = null;
        try {
            file = Configuration.getInstance().getSecurityManagerFile();
            if (file.exists()) {
                try {
                    customSecuritySettings = new JSONObject(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8.name()));
                }
                catch (Exception e) {
                    customSecuritySettings = null;
                    Trace.logDebug("No security settings in custom overrides.", e);
                }
            } else {
                Trace.logDebug("No custom security settings.");
            }
        }
        catch (Exception e) {
            // empty catch block
        }
        productSecuritySettings = new JSONObject();
        try {
            Path javahomeShort = Paths.get(System.getProperty("java.home"), new String[0]);
            Path javahomeFull = LabVantageSecurityManager.getRealPath(javahomeShort);
            tempShort = Paths.get(System.getProperty("java.io.tmpdir"), new String[0]);
            tempFull = LabVantageSecurityManager.getRealPath(tempShort);
            Path userhomeShort = Paths.get(System.getProperty("user.home"), new String[0]);
            Path userhomeFull = LabVantageSecurityManager.getRealPath(userhomeShort);
            Trace.logDebug("LSM JavaHome: " + javahomeFull + "( " + javahomeShort + ")");
            Trace.logDebug("LSM TMPDIR: " + tempFull + "( " + tempShort + ")");
            Trace.logDebug("LSM UserHome: " + userhomeFull + "( " + userhomeShort + ")");
            Path appdataShort = null;
            Path appdataFull = null;
            if (System.getenv("APPDATA") != null && System.getenv("APPDATA").length() > 0) {
                appdataShort = Paths.get(System.getenv("APPDATA"), new String[0]);
                appdataFull = LabVantageSecurityManager.getRealPath(appdataShort);
                Trace.logDebug("LSM Appdata: " + appdataFull + "( " + appdataShort + ")");
            }
            Path windirFull = null;
            Path windirShort = null;
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            if (isWindows && System.getenv("WINDIR") != null && System.getenv("WINDIR").length() > 0) {
                windirShort = Paths.get(System.getenv("WINDIR"), new String[0]);
                windirFull = LabVantageSecurityManager.getRealPath(windirShort);
                Trace.logDebug("LSM Windir: " + windirFull + "( " + windirShort + ")");
            }
            String fileseperator = System.getProperty("file.separator");
            productSecuritySettings.put("disableSandbox", customSecuritySettings != null && customSecuritySettings.has("disableSandbox") ? customSecuritySettings.optBoolean("disableSandbox") : false);
            productSecuritySettings.put("allowExit", customSecuritySettings != null && customSecuritySettings.has("allowExit") ? customSecuritySettings.optBoolean("allowExit") : false);
            productSecuritySettings.put("allowSecurityAccess", customSecuritySettings != null && customSecuritySettings.has("allowSecurityAccess") ? customSecuritySettings.optBoolean("allowSecurityAccess") : false);
            productSecuritySettings.put("allowCreateClassLoader", customSecuritySettings != null && customSecuritySettings.has("allowCreateClassLoader") ? customSecuritySettings.optBoolean("allowCreateClassLoader") : true);
            productSecuritySettings.put("allowFileRead", customSecuritySettings != null && customSecuritySettings.has("allowFileRead") ? customSecuritySettings.optBoolean("allowFileRead") : true);
            productSecuritySettings.put("allowTempFileRead", customSecuritySettings != null && customSecuritySettings.has("allowTempFileRead") ? customSecuritySettings.optBoolean("allowTempFileRead") : true);
            productSecuritySettings.put("allowLabVantageHomeFileRead", customSecuritySettings != null && customSecuritySettings.has("allowLabVantageHomeFileRead") ? customSecuritySettings.optBoolean("allowLabVantageHomeFileRead") : true);
            JSONArray allowedFileReads = new JSONArray();
            allowedFileReads.put("*jndi.properties");
            allowedFileReads.put("*accessibility.properties");
            allowedFileReads.put("*.class");
            allowedFileReads.put("*.docx");
            allowedFileReads.put("*.doc");
            allowedFileReads.put("*.xls");
            allowedFileReads.put("*.xlsx");
            allowedFileReads.put("*.pdf");
            allowedFileReads.put("*.csv");
            allowedFileReads.put("*.jar");
            allowedFileReads.put("*.txt");
            allowedFileReads.put("*.xps");
            allowedFileReads.put("*.jpeg");
            allowedFileReads.put("*.jpg");
            allowedFileReads.put("*.png");
            allowedFileReads.put("*commons-logging.properties");
            allowedFileReads.put("*cacerts");
            allowedFileReads.put("*jssecacerts");
            allowedFileReads.put("*META-INF*");
            allowedFileReads.put("*" + fileseperator + "sapphire.jar");
            allowedFileReads.put("*" + fileseperator + "resources.jar");
            allowedFileReads.put("*" + fileseperator + "netty_tcnative_windows_x86_64.dll");
            allowedFileReads.put("*" + fileseperator + "netty_tcnative_x86_64.dll");
            allowedFileReads.put("*" + fileseperator + "netty_tcnative.dll");
            allowedFileReads.put("*" + fileseperator + "sunec.jar");
            allowedFileReads.put("*" + fileseperator + "net.dll");
            allowedFileReads.put("*" + fileseperator + "os-release");
            allowedFileReads.put("*" + fileseperator + "somaxconn");
            allowedFileReads.put("*" + fileseperator + "user_projects" + fileseperator + "*");
            allowedFileReads.put("*" + fileseperator + "oracle_common" + fileseperator + "*");
            allowedFileReads.put("*" + fileseperator + "wlserver" + fileseperator + "*");
            allowedFileReads.put("*" + fileseperator + "jre" + fileseperator + "lib" + fileseperator + "fonts" + fileseperator + "*.ttf");
            allowedFileReads.put("*" + fileseperator + "jre" + fileseperator + "lib" + fileseperator + "cmm" + fileseperator + "*.pf");
            allowedFileReads.put(javahomeShort.toString() + fileseperator + "*");
            allowedFileReads.put(javahomeFull.toString() + fileseperator + "*");
            allowedFileReads.put(userhomeShort.resolve(".aws").resolve("credentials").toString());
            allowedFileReads.put(userhomeShort.resolve(".aws").resolve("config").toString());
            allowedFileReads.put(userhomeFull.resolve(".aws").resolve("credentials").toString());
            allowedFileReads.put(userhomeFull.resolve(".aws").resolve("config").toString());
            if (appdataShort != null) {
                allowedFileReads.put(appdataShort.getParent().resolve("Local").resolve("Microsoft").resolve("Windows").resolve("Fonts").toString());
                allowedFileReads.put(appdataShort.getParent().resolve("Local").resolve("Microsoft").resolve("Windows").resolve("Fonts").toString() + fileseperator + "*");
                allowedFileReads.put(appdataFull.getParent().resolve("Local").resolve("Microsoft").resolve("Windows").resolve("Fonts").toString());
                allowedFileReads.put(appdataFull.getParent().resolve("Local").resolve("Microsoft").resolve("Windows").resolve("Fonts").toString() + fileseperator + "*");
            }
            if (isWindows) {
                if (windirShort != null) {
                    allowedFileReads.put(windirShort.resolve("Fonts").toString());
                    allowedFileReads.put(windirShort.resolve("Fonts").toString() + fileseperator + "*");
                    allowedFileReads.put(windirFull.resolve("Fonts").toString());
                    allowedFileReads.put(windirFull.resolve("Fonts").toString() + fileseperator + "*");
                }
                allowedFileReads.put("\\etc\\release");
                allowedFileReads.put("*" + fileseperator + "Windows" + fileseperator + "Fonts*");
            } else {
                allowedFileReads.put("/etc/release");
                allowedFileReads.put("/etc/release/*");
                allowedFileReads.put("/root/.java/fonts/*");
                allowedFileReads.put("/root/.local/share/Microsoft/Windows/Fonts");
                allowedFileReads.put("/etc/*-release");
                allowedFileReads.put("/usr/lib/fontconfig/cache*");
                allowedFileReads.put("/root/.cache/fontconfig*");
                allowedFileReads.put("/root/.fontconfig*");
                allowedFileReads.put("/usr/share/*/fonts/*");
                allowedFileReads.put("/usr/*/lib/*/fonts/*");
                allowedFileReads.put("/var/lib/defoma/x-ttcidfont-conf.d/*");
                allowedFileReads.put("/usr/local/share/fonts");
                allowedFileReads.put("$home/.fonts");
                allowedFileReads.put("/home/*/$home/.fonts");
            }
            if (customSecuritySettings != null && customSecuritySettings.optJSONArray("allowedFileReads").length() > 0) {
                for (int i = 0; i < customSecuritySettings.optJSONArray("allowedFileReads").length(); ++i) {
                    allowedFileReads.put(customSecuritySettings.optJSONArray("allowedFileReads").optString(i));
                }
            }
            productSecuritySettings.put("allowedFileReads", allowedFileReads);
            productSecuritySettings.put("allowFileWrite", customSecuritySettings != null && customSecuritySettings.has("allowFileWrite") ? customSecuritySettings.optBoolean("allowFileWrite") : true);
            productSecuritySettings.put("allowTempFileWrite", customSecuritySettings != null && customSecuritySettings.has("allowTempFileWrite") ? customSecuritySettings.optBoolean("allowTempFileWrite") : true);
            productSecuritySettings.put("allowApplicationFileWrite", customSecuritySettings != null && customSecuritySettings.has("allowApplicationFileWrite") ? customSecuritySettings.optBoolean("allowApplicationFileWrite") : true);
            JSONArray allowedFileWrites = new JSONArray();
            Path lvhome = LabVantageSecurityManager.getLabvantageHome();
            allowedFileWrites.put(lvhome.resolve("attachments").toString() + fileseperator + "*");
            allowedFileWrites.put(lvhome.resolve("examplefiles").toString() + fileseperator + "*");
            allowedFileWrites.put(lvhome.resolve("files").toString() + fileseperator + "*");
            allowedFileWrites.put(lvhome.resolve("eln").toString() + fileseperator + "*");
            allowedFileWrites.put(lvhome.resolve("libraries").toString() + fileseperator + "*");
            allowedFileWrites.put(lvhome.resolve("files").toString() + fileseperator + "*");
            allowedFileWrites.put(appHome.resolve("reports").toString() + fileseperator + "*");
            if (customSecuritySettings != null && customSecuritySettings.optJSONArray("allowedFileWrites").length() > 0) {
                for (int i = 0; i < customSecuritySettings.optJSONArray("allowedFileWrites").length(); ++i) {
                    allowedFileWrites.put(customSecuritySettings.optJSONArray("allowedFileWrites").optString(i));
                }
            }
            productSecuritySettings.put("allowedFileWrites", allowedFileWrites);
            productSecuritySettings.put("allowFileDelete", customSecuritySettings != null && customSecuritySettings.has("allowFileDelete") ? customSecuritySettings.optBoolean("allowFileDelete") : true);
            productSecuritySettings.put("allowTempFileDelete", customSecuritySettings != null && customSecuritySettings.has("allowTempFileDelete") ? customSecuritySettings.optBoolean("allowTempFileDelete") : true);
            productSecuritySettings.put("allowApplicationFileDelete", customSecuritySettings != null && customSecuritySettings.has("allowApplicationFileDelete") ? customSecuritySettings.optBoolean("allowApplicationFileDelete") : true);
            JSONArray allowedFileDeletes = new JSONArray();
            allowedFileDeletes.put(lvhome.resolve("attachments").toString() + fileseperator + "*");
            allowedFileDeletes.put(lvhome.resolve("examplefiles").toString() + fileseperator + "*");
            allowedFileDeletes.put(lvhome.resolve("eln").toString() + fileseperator + "*");
            allowedFileDeletes.put(lvhome.resolve("libraries").toString() + fileseperator + "*");
            allowedFileDeletes.put(lvhome.resolve("files").toString() + fileseperator + "*");
            allowedFileDeletes.put(appHome.resolve("reports").toString() + fileseperator + "*");
            if (customSecuritySettings != null && customSecuritySettings.optJSONArray("allowedFileDeletes").length() > 0) {
                for (int i = 0; i < customSecuritySettings.optJSONArray("allowedFileDeletes").length(); ++i) {
                    allowedFileDeletes.put(customSecuritySettings.optJSONArray("allowedFileDeletes").optString(i));
                }
            }
            productSecuritySettings.put("allowedFileDeletes", allowedFileDeletes);
            productSecuritySettings.put("allowExec", customSecuritySettings != null && customSecuritySettings.has("allowExec") ? customSecuritySettings.optBoolean("allowExec") : true);
            JSONArray allowedExecs = new JSONArray();
            allowedExecs.put("reg");
            allowedExecs.put("cmd.exe");
            if (customSecuritySettings != null && customSecuritySettings.optJSONArray("allowedExecs").length() > 0) {
                for (int i = 0; i < customSecuritySettings.optJSONArray("allowedExecs").length(); ++i) {
                    allowedExecs.put(customSecuritySettings.optJSONArray("allowedExecs").optString(i));
                }
            }
            productSecuritySettings.put("allowedExecs", allowedExecs);
            productSecuritySettings.put("allowLink", customSecuritySettings != null && customSecuritySettings.has("allowLink") ? customSecuritySettings.optBoolean("allowLink") : true);
            JSONArray allowedLinks = new JSONArray();
            allowedLinks.put("awt");
            allowedLinks.put("fontmanager");
            allowedLinks.put("t2k");
            allowedLinks.put("net");
            allowedLinks.put("jpeg");
            allowedLinks.put("javajpeg");
            allowedLinks.put("freetype");
            allowedLinks.put("netty_tcnative_windows_x86_64");
            allowedLinks.put("netty_tcnative_x86_64");
            allowedLinks.put("netty_tcnative");
            allowedLinks.put(tempShort.toString() + fileseperator + "*.dll");
            allowedLinks.put(tempFull.toString() + fileseperator + "*.dll");
            if (customSecuritySettings != null && customSecuritySettings.optJSONArray("allowedLinks").length() > 0) {
                for (int i = 0; i < customSecuritySettings.optJSONArray("allowedLinks").length(); ++i) {
                    allowedLinks.put(customSecuritySettings.optJSONArray("allowedLinks").optString(i));
                }
            }
            productSecuritySettings.put("allowedLinks", allowedLinks);
            productSecuritySettings.put("allowConnect", customSecuritySettings != null && customSecuritySettings.has("allowConnect") ? customSecuritySettings.optBoolean("allowConnect") : true);
            productSecuritySettings.put("allowListen", customSecuritySettings != null && customSecuritySettings.has("allowListen") ? customSecuritySettings.optBoolean("allowListen") : true);
            productSecuritySettings.put("allowAccept", customSecuritySettings != null && customSecuritySettings.has("allowAccept") ? customSecuritySettings.optBoolean("allowAccept") : true);
            productSecuritySettings.put("allowMulticast", customSecuritySettings != null && customSecuritySettings.has("allowMulticast") ? customSecuritySettings.optBoolean("allowMulticast") : true);
            productSecuritySettings.put("allowPropertiesAccess", customSecuritySettings != null && customSecuritySettings.has("allowPropertiesAccess") ? customSecuritySettings.optBoolean("allowPropertiesAccess") : true);
            productSecuritySettings.put("allowPropertyAccess", customSecuritySettings != null && customSecuritySettings.has("allowPropertyAccess") ? customSecuritySettings.optBoolean("allowPropertyAccess") : true);
            productSecuritySettings.put("allowPrintJobAccess", customSecuritySettings != null && customSecuritySettings.has("allowPrintJobAccess") ? customSecuritySettings.optBoolean("allowPrintJobAccess") : true);
            productSecuritySettings.put("allowSetFactory", customSecuritySettings != null && customSecuritySettings.has("allowSetFactory") ? customSecuritySettings.optBoolean("allowSetFactory") : true);
            JSONArray classesBlackList = new JSONArray();
            classesBlackList.put("com.labvantage.sapphire.*");
            classesBlackList.put("javax.naming.InitialContext");
            if (customSecuritySettings != null && customSecuritySettings.optJSONArray("classesBlackList").length() > 0) {
                for (int i = 0; i < customSecuritySettings.optJSONArray("classesBlackList").length(); ++i) {
                    classesBlackList.put(customSecuritySettings.optJSONArray("classesBlackList").optString(i));
                }
            }
            productSecuritySettings.put("classesBlackList", classesBlackList);
            JSONArray classesWhiteList = new JSONArray();
            classesWhiteList.put("com.labvantage.sapphire.*.DatabaseAttachmentRepository");
            classesWhiteList.put("com.labvantage.sapphire.*.AWSAttachmentRepository");
            classesWhiteList.put("com.labvantage.sapphire.*.AzureAttachmentRepository");
            classesWhiteList.put("com.labvantage.sapphire.*.AzureNettyHttpClientProvider");
            classesWhiteList.put("com.labvantage.sapphire.*.NetworkAttachmentRepository");
            classesWhiteList.put("com.labvantage.sapphire.*.StandardAttachmentRepository");
            classesWhiteList.put("com.labvantage.sapphire.*.TalendJobAttachmentHandler");
            classesWhiteList.put("com.labvantage.sapphire.*.handlers.TalendUtil");
            classesWhiteList.put("com.labvantage.sapphire.*.TalendJavaReport");
            classesWhiteList.put("com.labvantage.sapphire.services.Attachment");
            classesWhiteList.put("com.labvantage.sapphire.services.SapphireConnection");
            classesWhiteList.put("com.labvantage.sapphire.pageelements.lookup.IAWSFileView");
            classesWhiteList.put("com.labvantage.sapphire.util.file.FileType");
            classesWhiteList.put("com.labvantage.sapphire.*.BasePDFSigner");
            classesWhiteList.put("com.labvantage.sapphire.*.CSRHandler");
            classesWhiteList.put("com.labvantage.sapphire.*.PDFSigner");
            classesWhiteList.put("com.labvantage.sapphire.*.SignatureData");
            classesWhiteList.put("com.labvantage.sapphire.modules.sdms.handlers.PDFHandler");
            classesWhiteList.put("com.labvantage.sapphire.util.file.DocumentFileParsingOptions");
            classesWhiteList.put("com.labvantage.sapphire.services.ConnectionInfo");
            classesWhiteList.put("com.labvantage.sapphire.util.file.FileType$NamedType");
            classesWhiteList.put("com.labvantage.sapphire.util.file.FileManager");
            classesWhiteList.put("com.labvantage.sapphire.Trace");
            classesWhiteList.put("com.labvantage.sapphire.modules.sdms.util.ResultDataGrid");
            classesWhiteList.put("com.labvantage.sapphire.Build");
            classesWhiteList.put("com.labvantage.sapphire.modules.sdms.handlers.IterateFilesAttachmentHandler");
            classesWhiteList.put("com.labvantage.sapphire.admin.system.automation.LAMRunnable");
            if (customSecuritySettings != null && customSecuritySettings.optJSONArray("classesWhiteList").length() > 0) {
                for (int i = 0; i < customSecuritySettings.optJSONArray("classesWhiteList").length(); ++i) {
                    classesWhiteList.put(customSecuritySettings.optJSONArray("classesWhiteList").optString(i));
                }
            }
            productSecuritySettings.put("classesWhiteList", classesWhiteList);
        }
        catch (Exception javahomeShort) {
            // empty catch block
        }
        if (file != null && !file.exists()) {
            File example = new File(StringUtil.replaceAll(file.getAbsolutePath(), ".json", "_example.json"));
            StringBuilder contents = new StringBuilder();
            contents.append("// Remove this comment and rename file from xxx_example.json to xxx.json.\n");
            try {
                contents.append(productSecuritySettings.toString(2));
            }
            catch (Exception userhomeShort) {
                // empty catch block
            }
            try {
                Files.write(example.toPath(), contents.toString().getBytes(StandardCharsets.UTF_8.name()), new OpenOption[0]);
            }
            catch (Exception e) {
                Trace.logWarn("Failed to write security manager example settings.", e);
            }
            Trace.logDebug("No custom security settings.");
        }
        return productSecuritySettings;
    }

    private static Path getApplicationHome() {
        if (appHome == null) {
            try {
                appHome = Paths.get(Configuration.getInstance().getApplicationHome(), new String[0]);
                return appHome;
            }
            catch (Exception e) {
                return null;
            }
        }
        return appHome;
    }

    private static Path getLabvantageHome() {
        if (labVantageHome == null) {
            try {
                labVantageHome = Paths.get(Configuration.getInstance().getSapphireHome(), new String[0]);
                return labVantageHome;
            }
            catch (Exception e) {
                return null;
            }
        }
        return labVantageHome;
    }

    private boolean isSandboxed() {
        return this.isInLabVantageClassLoader() && this.isLSMEnabled();
    }

    private boolean isLSMEnabled() {
        boolean disabled = LabVantageSecurityManager.getBoolSetting("disableSandbox", false);
        if (disabled && !this.errorStack.contains("LSM Disabled")) {
            Trace.logWarn("LSM Disabled Through Configuration");
            this.errorStack.add("LSM Disabled");
        }
        return !disabled;
    }

    private boolean isInLabVantageClassLoader() {
        try {
            if (Thread.currentThread().getContextClassLoader() != null) {
                return Thread.currentThread().getContextClassLoader().getClass().getName().equals(classLoader);
            }
            return false;
        }
        catch (Throwable e) {
            return false;
        }
    }

    @Override
    public void checkPackageAccess(String pkg) {
        super.checkPackageAccess(pkg);
    }

    @Override
    public void checkPermission(Permission perm) {
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        super.checkPermission(perm, context);
    }

    @Override
    public void checkAccess(Thread t) {
        super.checkAccess(t);
    }

    @Override
    public void checkAccess(ThreadGroup g) {
        super.checkAccess(g);
    }

    @Override
    public void checkExit(int status) {
        if (this.isSandboxed() && !LabVantageSecurityManager.getBoolSetting("allowExit", false)) {
            this.doError("System exit not allowed from external sources (allowExit).");
        } else {
            super.checkExit(status);
        }
    }

    @Override
    public void checkSecurityAccess(String target) {
        if (this.isSandboxed() && !LabVantageSecurityManager.getBoolSetting("allowSecurityAccess", false)) {
            if (target.equals("putProviderProperty.BC")) {
                super.checkSecurityAccess(target);
            } else {
                this.doError("Security access not allowed from external sources (allowSecurityAccess).");
            }
        } else {
            super.checkSecurityAccess(target);
        }
    }

    @Override
    public void checkCreateClassLoader() {
        if (this.isSandboxed() && !LabVantageSecurityManager.getBoolSetting("allowCreateClassLoader", true)) {
            this.doError("Creating class loaders not allowed from external sources (allowCreateClassLoader).");
        } else {
            super.checkCreateClassLoader();
        }
    }

    @Override
    public void checkExec(String cmd) {
        if (this.isSandboxed()) {
            if (LabVantageSecurityManager.getBoolSetting("allowExec", true)) {
                JSONArray allowedExecs = LabVantageSecurityManager.getJSONArraySetting("allowedExecs");
                if (allowedExecs != null && allowedExecs.length() > 0) {
                    boolean found = false;
                    for (int i = 0; i < allowedExecs.length(); ++i) {
                        try {
                            if (!allowedExecs.getString(i).equalsIgnoreCase(cmd)) continue;
                            found = true;
                            break;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    if (!found) {
                        this.doError("Sub process '" + cmd + "' not executable by external sources (allowedExecs).");
                    }
                } else {
                    this.doError("Sub process '" + cmd + "' not executable by external sources (allowedExecs).");
                }
            } else {
                this.doError("Executing sub-processes not allowed from external sources (allowExec).");
            }
        } else {
            super.checkExec(cmd);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void checkLink(String lib) {
        boolean found;
        if (!this.isSandboxed()) {
            super.checkLink(lib);
            return;
        }
        if (!LabVantageSecurityManager.getBoolSetting("allowLink", false)) {
            this.doError("Library linking not allowed from external sources (allowLink).");
            return;
        }
        JSONArray allowedLinks = LabVantageSecurityManager.getJSONArraySetting("allowedLinks");
        try {
            allowedLinks = new JSONArray(allowedLinks.toString());
            allowedLinks.put("jpeg");
            allowedLinks.put("javajpeg");
            if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                // empty if block
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (allowedLinks != null && allowedLinks.length() > 0) {
            found = false;
        } else {
            this.doError("Link '" + lib + "' not allowed by external sources (allowedLinks).");
            return;
        }
        for (int i = 0; i < allowedLinks.length(); ++i) {
            try {
                if (allowedLinks.getString(i).equalsIgnoreCase(lib)) {
                    found = true;
                    break;
                }
                if (!FileUtil.wildcardMatch(lib, allowedLinks.getString(i), true)) continue;
                found = true;
                break;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (found) return;
        this.doError("Link '" + lib + "' not allowed by external sources (allowedLinks).");
    }

    private static String getRealPath(String filepath) {
        return LabVantageSecurityManager.getRealPath(Paths.get(filepath, new String[0])).toAbsolutePath().toString();
    }

    private static Path getRealPath(Path filepath) {
        try {
            return filepath.toRealPath(new LinkOption[0]);
        }
        catch (Exception e) {
            return filepath;
        }
    }

    @Override
    public void checkRead(String file) {
        if (this.isSandboxed()) {
            if (LabVantageSecurityManager.getBoolSetting("allowFileRead", true)) {
                Path filePath = Paths.get(file, new String[0]);
                if (filePath.startsWith(tempShort) || filePath.startsWith(tempFull)) {
                    if (!LabVantageSecurityManager.getBoolSetting("allowTempFileRead", true)) {
                        this.doError("Temp file '" + file + "' not accessible by external sources (allowTempRead).");
                    }
                } else {
                    Path lvhome = LabVantageSecurityManager.getLabvantageHome();
                    if (lvhome != null && filePath.startsWith(lvhome)) {
                        if (!LabVantageSecurityManager.getBoolSetting("allowLabVantageHomeFileRead", true)) {
                            this.doError("Labvantage home file '" + file + "' not accessible by external sources (allowLabVantageHomeFileRead).");
                        }
                    } else {
                        JSONArray allowedFileReads = LabVantageSecurityManager.getJSONArraySetting("allowedFileReads");
                        if (allowedFileReads != null && allowedFileReads.length() > 0) {
                            String f = filePath.toAbsolutePath().toString();
                            boolean found = false;
                            for (int i = 0; i < allowedFileReads.length(); ++i) {
                                try {
                                    if (!FileUtil.wildcardMatch(f, allowedFileReads.getString(i), true)) continue;
                                    found = true;
                                    break;
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                            if (!found) {
                                this.doError("File '" + f + "' not accessible by external sources (allowedFileReads).");
                            }
                        } else {
                            this.doError("File '" + file + "' not accessible by external sources (allowedFileReads).");
                        }
                    }
                }
            } else {
                this.doError("All files including '" + file + "' are not accessible by external sources (allowFileRead).");
            }
        }
        super.checkRead(file);
    }

    @Override
    public void checkRead(FileDescriptor fd) {
        super.checkRead(fd);
    }

    @Override
    public void checkRead(String file, Object context) {
        super.checkRead(file, context);
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        super.checkWrite(fd);
    }

    @Override
    public void checkWrite(String file) {
        if (this.isSandboxed()) {
            if (LabVantageSecurityManager.getBoolSetting("allowFileWrite", true)) {
                Path filePath = Paths.get(file, new String[0]);
                if (filePath.startsWith(tempShort) || filePath.startsWith(tempFull)) {
                    if (!LabVantageSecurityManager.getBoolSetting("allowTempFileWrite", true)) {
                        this.doError("Temp file '" + file + "' not writeable by external sources (allowTempFileWrite).");
                    }
                } else {
                    Path appHome = LabVantageSecurityManager.getApplicationHome();
                    if (appHome != null && filePath.startsWith(appHome)) {
                        if (!LabVantageSecurityManager.getBoolSetting("allowApplicationFileWrite", true)) {
                            this.doError("Application file '" + file + "' not writeable by external sources (allowApplicationFileWrite).");
                        }
                    } else {
                        JSONArray allowedFileWrites = LabVantageSecurityManager.getJSONArraySetting("allowedFileWrites");
                        if (allowedFileWrites != null && allowedFileWrites.length() > 0) {
                            boolean found = false;
                            for (int i = 0; i < allowedFileWrites.length(); ++i) {
                                try {
                                    if (!FileUtil.wildcardMatch(file, allowedFileWrites.getString(i), true)) continue;
                                    found = true;
                                    break;
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                            if (!found) {
                                this.doError("File '" + file + "' not writeable by external sources (allowedFileWrites).");
                            }
                        } else {
                            this.doError("File '" + file + "' not writeable by external sources (allowedFileWrites).");
                        }
                    }
                }
            } else {
                this.doError("All files including '" + file + "' are not writeable by external sources (allowFileWrite).");
            }
        }
        super.checkWrite(file);
    }

    @Override
    public void checkDelete(String file) {
        if (this.isSandboxed()) {
            if (LabVantageSecurityManager.getBoolSetting("allowFileDelete", true)) {
                Path filePath = Paths.get(file, new String[0]);
                if (filePath.startsWith(tempShort) || filePath.startsWith(tempFull)) {
                    if (!LabVantageSecurityManager.getBoolSetting("allowTempFileDelete", true)) {
                        this.doError("Temp file '" + file + "' not deletable by external sources (allowTempFileDelete).");
                    }
                } else {
                    Path appHome = LabVantageSecurityManager.getApplicationHome();
                    if (appHome != null && filePath.startsWith(appHome)) {
                        if (!LabVantageSecurityManager.getBoolSetting("allowApplicationFileDelete", true)) {
                            this.doError("Application file '" + file + "' not deletable by external sources (allowApplicationFileDelete).");
                        }
                    } else {
                        JSONArray allowedFileDeletes = LabVantageSecurityManager.getJSONArraySetting("allowedFileDeletes");
                        if (allowedFileDeletes != null && allowedFileDeletes.length() > 0) {
                            boolean found = false;
                            for (int i = 0; i < allowedFileDeletes.length(); ++i) {
                                try {
                                    if (!FileUtil.wildcardMatch(file, allowedFileDeletes.getString(i), true)) continue;
                                    found = true;
                                    break;
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                            if (!found) {
                                this.doError("File '" + file + "' not deletable by external sources (allowedFileDeletes).");
                            }
                        } else {
                            this.doError("File '" + file + "' not deletable by external sources (allowedFileDeletes).");
                        }
                    }
                }
            } else {
                this.doError("All files including '" + file + "' are not deletable by external sources (allowFileDelete).");
            }
        }
        super.checkDelete(file);
    }

    @Override
    public void checkConnect(String host, int port) {
        if (this.isSandboxed() && !LabVantageSecurityManager.getBoolSetting("allowConnect", true)) {
            this.doError("Connecting to external hosts not allowed from external sources (allowConnect).");
        } else {
            super.checkConnect(host, port);
        }
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        super.checkConnect(host, port, context);
    }

    @Override
    public void checkListen(int port) {
        if (this.isSandboxed() && !LabVantageSecurityManager.getBoolSetting("allowListen", true)) {
            this.doError("Listening to ports not allowed from external sources (allowListen).");
        } else {
            super.checkListen(port);
        }
    }

    @Override
    public void checkAccept(String host, int port) {
        if (this.isSandboxed() && !LabVantageSecurityManager.getBoolSetting("allowAccept", true)) {
            this.doError("Accepting traffic from external host not allowed from external sources (allowAccept).");
        } else {
            super.checkAccept(host, port);
        }
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
        if (this.isSandboxed() && !LabVantageSecurityManager.getBoolSetting("allowMulticast", true)) {
            this.doError("Multicast not allowed from external sources (allowMulticast).");
        } else {
            super.checkMulticast(maddr);
        }
    }

    @Override
    public void checkPropertiesAccess() {
        if (this.isSandboxed() && !LabVantageSecurityManager.getBoolSetting("allowPropertiesAccess", true)) {
            this.doError("Properties Access is not allowed from external sources (allowPropertiesAccess).");
        } else {
            super.checkPropertiesAccess();
        }
    }

    @Override
    public void checkPropertyAccess(String key) {
        if (this.isSandboxed() && !LabVantageSecurityManager.getBoolSetting("allowPropertyAccess", true)) {
            this.doError("Property Access is not allowed from external sources (allowPropertyAccess).");
        } else {
            super.checkPropertyAccess(key);
        }
    }

    @Override
    public void checkPackageDefinition(String pkg) {
        super.checkPackageDefinition(pkg);
    }

    @Override
    public void checkPrintJobAccess() {
        if (this.isSandboxed() && !LabVantageSecurityManager.getBoolSetting("allowPrintJobAccess", true)) {
            this.doError("Print Job Access is not allowed from external sources (allowPrintJobAccess).");
        } else {
            super.checkPrintJobAccess();
        }
    }

    @Override
    public void checkSetFactory() {
        if (this.isSandboxed() && !LabVantageSecurityManager.getBoolSetting("allowSetFactory", true)) {
            this.doError("Setting Factory is not allowed from external sources (allowSetFactory).");
        } else {
            super.checkSetFactory();
        }
    }
}

