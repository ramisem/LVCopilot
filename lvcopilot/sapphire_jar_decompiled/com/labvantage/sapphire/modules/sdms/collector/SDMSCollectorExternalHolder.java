/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.email.License
 *  org.apache.commons.codec.binary.Base64
 *  org.tanukisoftware.wrapper.WrapperManager
 */
package com.labvantage.sapphire.modules.sdms.collector;

import com.aspose.email.License;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollectorHolder;
import com.labvantage.sapphire.modules.sdms.collector.SDMSFullUpgradeCounter;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.ExternalSenderFactory;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.stream.Stream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.tanukisoftware.wrapper.WrapperManager;
import sapphire.SapphireException;
import sapphire.servlet.ExternalHandlerProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDMSCollectorExternalHolder
implements SDMSConstants,
SDMSCollectorHolder {
    private SDMSCollector sdmsCollector = null;
    String collectorid;
    String token = null;
    protected String sapphireControllerURL = "";
    Path bootstrapPropsPath;
    Path rootPath;
    ExternalHandlerProcessor processor = null;
    boolean isExternalIndirect = false;
    Calendar lastPingFailed = null;
    private String myUniqueProcessId;
    private long lastProcessIdCheck = System.currentTimeMillis();
    private String internalKey = "9e09e75ec7c79bf564f2aa531d628e23";
    private String isolatedConfiguration = "isolated.conf";

    public SDMSCollectorExternalHolder(String token, String collectorid, String url, String bootstrapPropsFileName) throws SapphireException {
        this.token = token;
        this.collectorid = collectorid;
        this.sapphireControllerURL = url;
        this.bootstrapPropsPath = Paths.get(bootstrapPropsFileName, new String[0]);
        this.rootPath = this.bootstrapPropsPath.resolve("../..");
        WrapperManager.setConsoleTitle((String)("LabVantage SDMS - " + collectorid));
    }

    public void start() throws ExternalAppException, SapphireException {
        System.out.println();
        System.out.println();
        System.out.println(".____          ___. ____   ____              __                         \n|    |   _____ \\_ |_\\   \\ /   /____    _____/  |______     ____   ____  \n|    |   \\__  \\ | __ \\   Y   /\\__  \\  /    \\   __\\__  \\   / ___\\_/ __ \\ \n|    |___ / __ \\| \\_\\ \\     /  / __ \\|   |  \\  |  / __ \\_/ /_/  >  ___/ \n|_______ (____  /___  /\\___/  (____  /___|  /__| (____  /\\___  / \\___  >\n        \\/    \\/    \\/             \\/     \\/          \\//_____/      \\/ ");
        System.out.println();
        System.out.println();
        System.out.println("  _________________      _____    _________ _________        .__  .__                 __                \n /   _____/\\______ \\    /     \\  /   _____/ \\_   ___ \\  ____ |  | |  |   ____   _____/  |_  ___________ \n \\_____  \\  |    |  \\  /  \\ /  \\ \\_____  \\  /    \\  \\/ /  _ \\|  | |  | _/ __ \\_/ ___\\   __\\/  _ \\_  __ \\\n /        \\ |    `   \\/    Y    \\/        \\ \\     \\___(  <_> )  |_|  |_\\  ___/\\  \\___|  | (  <_> )  | \\/\n/_______  //_______  /\\____|__  /_______  /  \\______  /\\____/|____/____/\\___  >\\___  >__|  \\____/|__|   \n        \\/         \\/         \\/        \\/          \\/                      \\/     \\/                   ");
        System.out.println();
        System.out.println();
        boolean continueStart = false;
        if (this.token == null || this.token.length() == 0) {
            SDMSCollectorExternalHolder.pleaseContact("No token has been found.");
            System.exit(1);
        }
        if (this.collectorid == null || this.collectorid.length() == 0) {
            SDMSCollectorExternalHolder.pleaseContact("No collectorid has been found.");
            System.exit(1);
        }
        String databaseid = SapphireService.getDatabaseForToken(this.token);
        this.processor = new ExternalHandlerProcessor(this.token, this.sapphireControllerURL);
        Path configFile = this.rootPath.resolve("conf/" + this.isolatedConfiguration);
        boolean canSwitchToIsolatedIndirect = configFile.toFile().exists();
        boolean startAsIsolatedIndirect = false;
        try {
            this.log("COLLECTOR", "Connecting to LIMS on " + this.sapphireControllerURL);
            boolean isTokenActive = this.processor.isTokenActive();
            if (!isTokenActive) {
                SDMSCollectorExternalHolder.pleaseContact("Token " + this.token + " is not Active");
                System.exit(1);
            }
        }
        catch (SapphireException e) {
            if (canSwitchToIsolatedIndirect) {
                startAsIsolatedIndirect = true;
            }
            throw new SapphireException("Unable to communicate with the LIMS and isolated mode not available. Please contact your system administrator.", e);
        }
        this.log("COLLECTOR", "Connected.");
        this.log("COLLECTOR", "");
        try {
            InputStream licenseStream = null;
            if (Paths.get("../lib/sapphire.jar", new String[0]).toFile() == null) {
                throw new SapphireException("sapphire.jar is not found");
            }
            licenseStream = Paths.get("../lib/sapphire.jar", new String[0]).toFile().isFile() ? this.getClass().getResourceAsStream("/META-INF/Aspose.Total.Java.lic") : Files.newInputStream(Paths.get(Paths.get("../lib/sapphire.jar/META-INF/Aspose.Total.Java.lic", new String[0]).toFile().getCanonicalPath(), new String[0]), new OpenOption[0]);
            License license = new License();
            license.setLicense(licenseStream);
            licenseStream.close();
        }
        catch (IOException e) {
            throw new SapphireException(e.getMessage(), e);
        }
        this.myUniqueProcessId = "" + System.currentTimeMillis();
        try {
            Properties bootstrapProps = new Properties();
            bootstrapProps.load(new FileInputStream(this.bootstrapPropsPath.toFile()));
            bootstrapProps.setProperty("processid", this.myUniqueProcessId);
            bootstrapProps.store(new FileOutputStream(this.bootstrapPropsPath.toFile()), "Added upgrade diretive");
        }
        catch (IOException e) {
            this.myUniqueProcessId = "";
            throw new SapphireException("Failed to write the processid into the bootstart file " + this.bootstrapPropsPath + ": " + e.getMessage(), e);
        }
        continueStart = true;
        if (!startAsIsolatedIndirect) {
            PropertyList commandRequest = new PropertyList();
            commandRequest.setProperty("collectorid", this.collectorid);
            this.sendCommandToLIMS("COMMAND_REGISTERCOLLECTOR", commandRequest);
        }
        if (continueStart) {
            PropertyList localConfigProps = new PropertyList();
            try {
                if (canSwitchToIsolatedIndirect && configFile != null && configFile.toFile().exists()) {
                    localConfigProps.setPropertyList(configFile.toFile());
                    this.encryptOrDecryptPropertyForEmailCollector(localConfigProps, false);
                }
            }
            catch (SapphireException e) {
                this.log("STARTUP", "");
                this.log("STARTUP", "ERROR: Old startup properties may be corrupt. Will replace with new set if possible: " + e.getMessage());
                this.log("STARTUP", "");
            }
            PropertyList configProps = null;
            if (startAsIsolatedIndirect) {
                System.out.println("");
                System.out.println("------------------------------------------------------------");
                System.out.println("--------------------- W A R N I N G ------------------------");
                System.out.println("------------------------------------------------------------");
                System.out.println("> Failed to communicate to LIMS.");
                System.out.println("> Bootstrapping from last known configuration");
                System.out.println("> Attempting to enable Isolated Indirect Mode until communication is restored...");
                System.out.println();
                try {
                    this.isExternalIndirect = true;
                    DataSet collector = new DataSet(new JSONObject(localConfigProps.getProperty("collector_dataset")));
                    boolean allowIsolatedFlag = collector.getValue(0, "allowisolatedflag").equals("Y");
                    if (!allowIsolatedFlag) {
                        throw new SapphireException("Isolated execution is not allowed for this Collector. Shutting down.");
                    }
                    configProps = localConfigProps;
                }
                catch (Exception e) {
                    System.out.println("> Failed to startup using last known configuration: ");
                    throw new SapphireException(e);
                }
            }
            this.isExternalIndirect = false;
            PropertyList commandRequest = new PropertyList();
            commandRequest.setProperty("collectorid", this.collectorid);
            configProps = this.sendCommandToLIMS("COMMAND_GETCONFIGPROPS", commandRequest);
            try {
                PropertyList serverRuntimeProps = configProps.getPropertyListNotNull("runtimeproperties");
                long lastServerUpdate = Long.parseLong(serverRuntimeProps.getProperty("_lastupdate", "0"));
                PropertyList localRuntimeProps = localConfigProps.getPropertyListNotNull("runtimeproperties");
                long lastLocalUpdate = Long.parseLong(localRuntimeProps.getProperty("_lastupdate", "0"));
                if (lastLocalUpdate > lastServerUpdate) {
                    configProps.setProperty("runtimeproperties", localRuntimeProps);
                    commandRequest = new PropertyList();
                    commandRequest.setProperty("collectorid", this.collectorid);
                    commandRequest.setProperty("runtimeproperties", localRuntimeProps);
                    commandRequest.setProperty("skiplocalsave", "Y");
                    this.sendCommandToLIMS("COMMAND_SAVERUNTIMEPROPERTIES", commandRequest);
                }
            }
            catch (Exception e) {
                this.log("STARTUP", "");
                this.log("STARTUP", "ERROR: Failed to determine the most up to date runtime properties: " + e.getMessage());
                this.log("STARTUP", "");
            }
            PropertyList configPropsClone = new PropertyList();
            configPropsClone.putAll(configProps);
            this.encryptOrDecryptPropertyForEmailCollector(configPropsClone, true);
            this.saveConfigPropertiesToFile(configFile.toFile(), configPropsClone);
            ExternalSenderFactory senderFactory = new ExternalSenderFactory(this.isExternalIndirect);
            this.sdmsCollector = new SDMSCollector(this.collectorid, databaseid, this, configProps, senderFactory);
            this.sdmsCollector.startPinging();
            this.sdmsCollector.beginOperations();
        } else {
            this.sdmsCollector.startPinging();
        }
    }

    private void encryptOrDecryptPropertyForEmailCollector(PropertyList configProps, boolean isEncrypt) throws SapphireException {
        String fieldToEncryptAndDecrypt = "password";
        try {
            DataSet instruments = new DataSet(new JSONObject(configProps.getProperty("instruments_dataset")));
            boolean found = false;
            for (int i = 0; i < instruments.size(); ++i) {
                String collectorType = instruments.getValue(i, "_collectortype");
                if (!collectorType.equals("EmailCollectorType")) continue;
                found = true;
                PropertyList collectorTypeProps = new PropertyList();
                collectorTypeProps.setPropertyList(instruments.getValue(i, "_collectorrules", "<propertylist />"));
                PropertyList collectorProps = collectorTypeProps.getPropertyListNotNull("collectorprops");
                PropertyList credentialsPropertyList = collectorProps.getPropertyListNotNull("credentials");
                String password = credentialsPropertyList.getProperty(fieldToEncryptAndDecrypt);
                password = isEncrypt ? this.encryptAES(password) : this.decryptAES(password);
                credentialsPropertyList.setProperty(fieldToEncryptAndDecrypt, password);
                instruments.setValue(i, "_collectorrules", collectorTypeProps.toXMLString());
            }
            if (found) {
                configProps.setProperty("instruments_dataset", instruments.toJSONString(true, false));
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to encrypt or decrypt: " + e.getMessage(), e);
        }
    }

    private String encryptAES(String text) {
        try {
            byte[] encryptText = text.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(this.internalKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(1, skeySpec);
            return Base64.encodeBase64String((byte[])cipher.doFinal(encryptText));
        }
        catch (Exception e) {
            return text;
        }
    }

    public String decryptAES(String text) {
        try {
            byte[] encryptText = Base64.decodeBase64((String)text);
            SecretKeySpec skeySpec = new SecretKeySpec(this.internalKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(2, skeySpec);
            return new String(cipher.doFinal(encryptText));
        }
        catch (Exception e) {
            return text;
        }
    }

    private void saveConfigPropertiesToFile(File configFile, PropertyList configProps) throws SapphireException {
        try {
            if (configFile.exists()) {
                configFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(configFile.getCanonicalFile());
            fos.write(configProps.toXMLString().getBytes());
            fos.close();
        }
        catch (IOException se) {
            throw new SapphireException("File IO error writing file " + configFile.getAbsolutePath() + ": " + se.getMessage(), se);
        }
    }

    @Override
    public void log(String type, String message) {
        this.log(type, message, "");
    }

    @Override
    public void log(String type, String message, Throwable e) {
        this.log(type, message, "");
        System.out.println("DIAGNOSTIC DETAILS");
        System.out.println("==================");
        e.printStackTrace();
    }

    @Override
    public void log(String type, String message, String instrumentid) {
        System.out.println(type + ": " + (instrumentid != null && instrumentid.length() > 0 ? instrumentid + ": " : "") + message);
    }

    @Override
    public void log(String type, String message, String instrumentid, Throwable e) {
        this.log(type, message, instrumentid);
        System.out.println("DIAGNOSTIC DETAILS");
        System.out.println("==================");
        e.printStackTrace();
    }

    @Override
    public JSONObject sendFileCommandToLIMS(String processAs, String command, Path file, JSONObject jsonRequest) throws SapphireException {
        if (this.isExternalIndirect) {
            return null;
        }
        return this.processor.sendFileCommandToLIMS(processAs, command, file, jsonRequest);
    }

    @Override
    public PropertyList sendCommandToLIMS(String command, PropertyList commandProps) throws SapphireException {
        block15: {
            block16: {
                if (!command.equals("COMMAND_PING")) break block16;
                try {
                    PropertyList commandResponse = this.processor.sendCommandToLIMS(command, commandProps);
                    this.lastPingFailed = null;
                    if (this.isExternalIndirect) {
                        try {
                            System.out.println("");
                            System.out.println(">");
                            System.out.println("> Communication to LIMS has been established!");
                            System.out.println(">");
                            System.out.println(">");
                            this.reboot();
                        }
                        catch (ExternalAppException externalAppException) {}
                        break block15;
                    }
                    if (this.lastProcessIdCheck < System.currentTimeMillis() - 10000L) {
                        try {
                            Properties bootstrapProps = new Properties();
                            bootstrapProps.load(new FileInputStream(this.bootstrapPropsPath.toFile()));
                            String processid = bootstrapProps.getProperty("processid");
                            if (this.myUniqueProcessId != null && this.myUniqueProcessId.length() > 0 && processid.length() > 0 && !this.myUniqueProcessId.equals(processid)) {
                                this.log("CARETAKER", "Raising alert because sessions mismatch");
                                this.sdmsCollector.raiseCollectorAlert("SDMS Collector State", "Warning", "Multiple sessions identified", "There appears to be more than one instance of collector " + this.collectorid + " running. This may lead to instability and lost data collection.", false);
                            }
                            this.lastProcessIdCheck = System.currentTimeMillis();
                        }
                        catch (IOException bootstrapProps) {
                            // empty catch block
                        }
                    }
                    return commandResponse;
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                    if (this.isExternalIndirect) break block15;
                    System.out.println("> Failed to ping LIMS. Will keep checking for " + this.sdmsCollector.getWaitForIsolatedModeSeconds() + "s before taking action..");
                    if (this.lastPingFailed == null) {
                        this.lastPingFailed = Calendar.getInstance();
                        break block15;
                    }
                    if (Calendar.getInstance().getTimeInMillis() - this.lastPingFailed.getTimeInMillis() <= (long)(this.sdmsCollector.getWaitForIsolatedModeSeconds() * 1000)) break block15;
                    try {
                        System.out.println("");
                        System.out.println(">");
                        System.out.println("> Communication to LIMS has been LOST");
                        System.out.println(">");
                        this.reboot();
                        break block15;
                    }
                    catch (ExternalAppException bootstrapProps) {}
                }
                break block15;
            }
            if (command.equals("COMMAND_SAVERUNTIMEPROPERTIES") && !commandProps.getProperty("skiplocalsave").equals("Y")) {
                PropertyList runtimeProperties = commandProps.getPropertyList("runtimeproperties");
                Path configFile = this.rootPath.resolve("conf/" + this.isolatedConfiguration);
                if (configFile.toFile().exists()) {
                    PropertyList configProps = new PropertyList();
                    configProps.setPropertyList(configFile.toFile());
                    configProps.setProperty("runtimeproperties", runtimeProperties);
                    this.saveConfigPropertiesToFile(configFile.toFile(), configProps);
                }
            }
            if (this.isExternalIndirect) {
                return null;
            }
            return this.processor.sendCommandToLIMS(command, commandProps);
        }
        return null;
    }

    @Override
    public void executeRebootCommand() {
        this.sdmsCollector.shutdown();
        WrapperManager.restart();
    }

    @Override
    public void upgrade(String upgradeMode) throws SapphireException {
        try {
            JSONObject commandRequest = new JSONObject();
            commandRequest.put("collectorid", this.collectorid);
            commandRequest.put("upgrademode", upgradeMode);
            String platform = System.getProperty("os.name").toLowerCase().startsWith("windo") ? "windows" : "unix";
            commandRequest.put("platform", platform);
            System.out.println("");
            System.out.println("");
            System.out.println("AutoUpgrade Sequence Initiated...");
            System.out.println("=================================");
            System.out.println("");
            System.out.println("");
            this.log("UPGRADE", "Upgrading will begin in 3 seconds. The Collector will restart several times.");
            SDMSCollectorExternalHolder.doWait(1000);
            this.log("UPGRADE", "Upgrading will begin in 2 seconds. The Collector will restart several times.");
            SDMSCollectorExternalHolder.doWait(1000);
            this.log("UPGRADE", "Upgrading will begin in 1 seconds. The Collector will restart several times.");
            SDMSCollectorExternalHolder.doWait(1000);
            Path upgradeFolder = Paths.get(this.rootPath.resolve("upgrade").toFile().getCanonicalPath(), new String[0]);
            if (!upgradeFolder.toFile().exists()) {
                upgradeFolder.toFile().mkdir();
            } else {
                if (upgradeFolder.toFile().exists()) {
                    this.log("UPGRADE", "Cleaning upgrade folder of prior upgrades");
                    FileUtil.deleteDirectory(upgradeFolder.toFile());
                }
                for (int i = 0; i < 10; ++i) {
                    Path check = Paths.get(upgradeFolder.toFile().getAbsolutePath(), new String[0]);
                    if (!check.toFile().exists()) continue;
                    SDMSCollectorExternalHolder.doWait(500);
                    FileUtil.deleteDirectory(upgradeFolder.toFile());
                }
                if (upgradeFolder.toFile().exists()) {
                    this.log("UPGRADE", "ERROR: Failed to delete the upgrade folder. Make sure you haven't got any files open");
                    throw new SapphireException("");
                }
            }
            this.log("UPGRADE", "Requesting upgrade zip");
            Path installzip = null;
            if (platform.equals("unix")) {
                installzip = FileUtil.createTempFile("install", ".tar.gz");
            } else if (platform.equals("windows")) {
                installzip = FileUtil.createTempFile("install", ".zip");
            }
            this.processor.sendDownloadFileCommandToLIMS("COMMAND_DOWNLOADUPGRADEZIP", commandRequest, installzip);
            if (platform.equals("windows")) {
                this.log("UPGRADE", "Upgrade zip has been received");
                this.log("UPGRADE", "Unpacking zip file into /upgrade folder");
                SDMSUtil.extractZipStream(new FileInputStream(installzip.toFile()), upgradeFolder.toFile());
            } else if (platform.equals("unix")) {
                this.log("UPGRADE", "Upgrade tar has been received");
                this.log("UPGRADE", "Unpacking tar file into /upgrade folder");
                SDMSUtil.extractTarStream(new FileInputStream(installzip.toFile()), upgradeFolder.toFile());
            }
            if (!upgradeMode.equals("Full")) {
                this.log("UPGRADE", "Creating auto-upgrade directive for restart");
                Properties bootstrapProps = new Properties();
                bootstrapProps.load(new FileInputStream(this.bootstrapPropsPath.toFile()));
                bootstrapProps.setProperty("upgrademode", upgradeMode);
                bootstrapProps.store(new FileOutputStream(this.bootstrapPropsPath.toFile()), "Added upgrade diretive");
                this.log("UPGRADE", "Removing lib and lib custom from the class path");
                SDMSCollectorExternalHolder.modifyWrapperConfClasspath(this.rootPath, true);
                this.reboot();
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to auto-upgrade: " + e.getMessage(), e);
        }
    }

    @Override
    public String getActualHostname() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            return address.toString();
        }
        catch (UnknownHostException e) {
            return "Unknown External";
        }
    }

    @Override
    public void setStartupStateProperties(PropertyList startupState) {
        startupState.setProperty("upgradecounter", "" + SDMSFullUpgradeCounter.getCounter());
        Path libcustom = this.rootPath.resolve("libcustom");
        Path customConfigFile = this.rootPath.resolve("conf/wrapper-custom.conf");
        String hash = SDMSFullUpgradeCounter.getLibCustomHash(libcustom, customConfigFile);
        startupState.setProperty("libcustomhash", hash);
        Properties wrapperConf = new Properties();
        try {
            wrapperConf.load(new FileInputStream(this.rootPath.resolve("conf/wrapper.conf").toFile().getCanonicalFile()));
            String option = wrapperConf.getProperty("labvantage.jreoption");
            String bitOption = wrapperConf.getProperty("labvantage.wrapper.bits");
            startupState.setProperty("jreoption", option == null || option.length() == 0 ? "Unknown" : option);
            startupState.setProperty("osbits", bitOption == null || bitOption.length() == 0 ? "Unknown" : bitOption);
        }
        catch (IOException e) {
            startupState.setProperty("jreoption", "Unknown");
            startupState.setProperty("osbits", "Unknown");
        }
    }

    public String getCollectorid() {
        return this.collectorid;
    }

    public void reboot() throws ExternalAppException, SapphireException {
        if (this.sdmsCollector != null) {
            this.sdmsCollector.shutdown();
        }
        WrapperManager.restart();
    }

    public static void pleaseContact(String message) {
        String please = "Please contact your System Administrator for more information";
        String delim = StringUtil.repeat("=", message.length() > please.length() ? message.length() : please.length());
        System.out.println();
        System.out.println(delim);
        System.out.println(message);
        System.out.println();
        System.out.println(please);
        System.out.println(delim);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void saveBootstrapProps(Path bootstrapPropsFile, Properties bootstrapProps) {
        if (bootstrapPropsFile != null) {
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(bootstrapPropsFile.toFile().getAbsoluteFile());
                bootstrapProps.store(output, "");
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            finally {
                if (output != null) {
                    try {
                        output.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void doWait(int wait) {
        try {
            Thread.sleep(wait);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void modifyWrapperConfClasspath(Path rootFolder, boolean upgrade) throws IOException {
        OutputStream wrapperConfFOS = null;
        PrintWriter wrapperConfWriter = null;
        try {
            Path wrapperConf = Paths.get(rootFolder.resolve("conf/wrapper.conf").toFile().getCanonicalPath(), new String[0]);
            Stream<String> lines = Files.lines(wrapperConf);
            ArrayList allLines = new ArrayList();
            lines.forEach(line -> allLines.add(line));
            lines.close();
            wrapperConfFOS = new FileOutputStream(wrapperConf.toFile());
            wrapperConfWriter = new PrintWriter(wrapperConfFOS);
            for (String line2 : allLines) {
                if (line2.contains("=../lib/*") || line2.contains("=../libcustom/*") || line2.endsWith("sapphire.jar")) {
                    if (upgrade) {
                        wrapperConfWriter.println("#" + line2);
                        continue;
                    }
                    wrapperConfWriter.println(line2.substring(line2.indexOf("wrapper")));
                    continue;
                }
                wrapperConfWriter.println(line2);
            }
        }
        finally {
            if (wrapperConfFOS != null) {
                wrapperConfWriter.flush();
                wrapperConfFOS.flush();
                ((FileOutputStream)wrapperConfFOS).close();
            }
        }
    }
}

