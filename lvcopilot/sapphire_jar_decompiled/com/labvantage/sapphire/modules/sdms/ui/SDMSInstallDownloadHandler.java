/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.sdms.ui;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.stream.Stream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDMSInstallDownloadHandler
extends BaseRequest
implements SDMSConstants {
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        ConnectionProcessor cp = this.getConnectionProcessor();
        ConnectionInfo info = cp.getConnectionInfo(cp.getConnectionid());
        if (!info.hasRole("SDMSAdmin")) {
            throw new ServletException("Insufficient privileges to download installer.");
        }
        try {
            Path zipFile;
            String collectorid = request.getParameter("collectorid");
            JSONObject jso = new JSONObject(request.getParameter("jso"));
            boolean isInstall = "Y".equals(jso.optString("install"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String downloadFilename = null;
            if (isInstall) {
                zipFile = this.getInstallZip(collectorid, jso);
                String extension = FileManager.getExtension(zipFile.getFileName().toString());
                if (extension.equals("zip")) {
                    downloadFilename = "SDMSInstall-" + collectorid + " " + sdf.format(Calendar.getInstance().getTime()) + ".zip";
                } else if (extension.equals("gz")) {
                    downloadFilename = "SDMSInstall-" + collectorid + " " + sdf.format(Calendar.getInstance().getTime()) + ".tar.gz";
                }
            } else {
                zipFile = this.getUpgradeZip(collectorid, jso, false);
                String extension = FileManager.getExtension(zipFile.getFileName().toString());
                if (extension.equals("zip")) {
                    downloadFilename = "SDMSUpgrade-" + collectorid + " " + sdf.format(Calendar.getInstance().getTime()) + ".zip";
                } else if (extension.equals("gz")) {
                    downloadFilename = "SDMSUpgrade-" + collectorid + " " + sdf.format(Calendar.getInstance().getTime()) + ".tar.gz";
                }
            }
            this.streamFileOut(response, zipFile.toFile().getAbsolutePath(), downloadFilename, false);
        }
        catch (Exception e) {
            throw new ServletException("Unable to download installer: " + e.getMessage(), (Throwable)e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void createShellForUnix(Path targetFolder, String collectorid) throws IOException {
        Path sdmsCollectorShellTemplate = targetFolder.resolve("bin/SDMSCollector_Template");
        Path sdmsCollectorShellFile = targetFolder.resolve("bin/SDMSCollector");
        FileOutputStream sdmsCollectorShellFileFOS = new FileOutputStream(sdmsCollectorShellFile.toFile());
        PrintWriter sdmsCollectorShellFileWriter = new PrintWriter(sdmsCollectorShellFileFOS);
        try {
            Stream<String> lines = Files.lines(sdmsCollectorShellTemplate);
            lines.forEach(line -> {
                if (line.equals("[APP_NAME]")) {
                    sdmsCollectorShellFileWriter.print("APP_NAME=" + collectorid + "\n");
                } else if (line.equals("[APP_LONG_NAME]")) {
                    sdmsCollectorShellFileWriter.print("APP_LONG_NAME=" + collectorid + "\n");
                } else {
                    sdmsCollectorShellFileWriter.print(line + "\n");
                }
            });
            lines.close();
        }
        finally {
            sdmsCollectorShellFileWriter.flush();
            sdmsCollectorShellFileFOS.flush();
            sdmsCollectorShellFileFOS.close();
        }
        sdmsCollectorShellTemplate.toFile().delete();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Path getInstallZip(String collectorid, JSONObject jso) throws SapphireException, ServletException, IOException {
        Path platformImagePath;
        ConfigurationProcessor configurationProcessor = this.getConfigurationProcessor();
        PropertyList policy = configurationProcessor.getPolicy("SDMSPolicy", "Sapphire Custom");
        String tokenvalue = jso.optString("tokenvalue");
        String jreoption = jso.optString("jreoption").isEmpty() ? "JavaHome" : jso.optString("jreoption");
        String url = jso.optString("externalurl");
        String initialHeapSize = jso.optString("initialheapsize").isEmpty() ? "128" : jso.optString("initialheapsize");
        String maxHeapSize = jso.optString("maxheapsize").isEmpty() ? "512" : jso.optString("maxheapsize");
        String javaadditional = jso.optString("javaadditional");
        String logLevel = jso.optString("loglevel").isEmpty() ? "INFO" : jso.optString("loglevel");
        String maxSizeofLogFile = jso.optString("maxsizeoflogfile").isEmpty() ? "10" : jso.optString("maxsizeoflogfile");
        String maxNumberofLogFiles = jso.optString("maxnumberoflogfiles").isEmpty() ? "10" : jso.optString("maxnumberoflogfiles");
        String checkDeadLock = jso.optString("checkdeadlock", "N");
        String emailDebug = jso.optString("emaildebug", "");
        String emailSmtpHost = jso.optString("emailsmtphost", "");
        String emailSmtpPort = jso.optString("emailsmtpport", "");
        String emailSubject = jso.optString("emailsubject", "");
        String emailSender = jso.optString("emailsender", "");
        String emailRecipient = jso.optString("emailrecipient", "");
        String emailWrapperStarts = jso.optString("emailwrapperstarts", "");
        String emailDeadlock = jso.optString("emaildeadlock", "");
        String emailJvmFails = jso.optString("emailjvmfails", "");
        String emailJvmKilled = jso.optString("emailjvmkilled", "");
        String emailJvmMaxFails = jso.optString("emailjvmmaxfails", "");
        String emailJvmUnexpectedExit = jso.optString("emailjvmunexpectedexit", "");
        String jvmRestartEmailBody = jso.optString("jvmrestartemailbody", "");
        PropertyList installerPL = policy.getPropertyListNotNull("installer");
        Path productImageFolder = Paths.get(FileUtil.substituteConfigurationPaths(installerPL.getProperty("installimagefolder", "[labvantagehome]/console/install/externalapps/sdmscollector/product")), new String[0]);
        Path productJarFolder = Paths.get(FileUtil.substituteConfigurationPaths(installerPL.getProperty("productjarfolder", "[labvantagehome]/console/install/ear")), new String[0]);
        if (!productImageFolder.toFile().exists()) {
            throw new ServletException("Could not locate the product image path " + productImageFolder.toFile().getAbsolutePath());
        }
        if (!productJarFolder.toFile().exists()) {
            throw new ServletException("Could not locate the product jar path " + productJarFolder.toFile().getAbsolutePath());
        }
        String platform = jso.optString("platform", "windows");
        String osNumberOfBits = jso.optString("osbits", "64");
        String platformPath = platform;
        if (osNumberOfBits.equals("32")) {
            platformPath = platformPath + "_32";
        }
        Path path = platformImagePath = "windows".equals(platform) ? productImageFolder.resolve(platformPath) : productImageFolder.resolve(platformPath);
        if (platformImagePath == null || !platformImagePath.toFile().exists()) {
            throw new ServletException("Could not locate the image path " + platformImagePath.toFile().getAbsolutePath());
        }
        Path targetFolder = Files.createTempDirectory("sdmsinstaller", new FileAttribute[0]);
        FileTransferOptions initialCopyOptions = new FileTransferOptions();
        FileTransfer.copyDirectory(platformImagePath.toFile(), targetFolder.toFile(), initialCopyOptions);
        Path lib = targetFolder.resolve("lib");
        Path libwrapper = targetFolder.resolve("libwrapper");
        Path libcustom = targetFolder.resolve("libcustom");
        Path logs = targetFolder.resolve("logs");
        String sapphireJarName = "sapphire.jar";
        String sdmsCollectorJarName = "sdmscollector.jar";
        Files.copy(productJarFolder.resolve(sapphireJarName), lib.resolve(sapphireJarName), new CopyOption[0]);
        Files.copy(productJarFolder.resolve(sdmsCollectorJarName), libwrapper.resolve(sdmsCollectorJarName), new CopyOption[0]);
        String customJarFolderName = installerPL.getProperty("customjarfolder");
        this.copyCustomJarAndConfig(targetFolder, initialCopyOptions, customJarFolderName, libcustom);
        Path logsPath = platformImagePath.resolve("logs");
        if (!logsPath.toFile().exists()) {
            logs.toFile().mkdir();
        }
        Path resources = productImageFolder.resolve("resources/" + platform);
        Path collectorPropsTemplate = targetFolder.resolve("conf/sdmscollector.template.conf");
        Path wrapperConfTemplate = targetFolder.resolve("conf/wrapper.template.conf");
        if (!collectorPropsTemplate.toFile().exists()) {
            throw new ServletException("Failed to locate sdmscollector.template.conf in the working copy");
        }
        if (!wrapperConfTemplate.toFile().exists()) {
            throw new ServletException("Failed to locate wrapper.template.conf in the working copy");
        }
        Path collectorProps = targetFolder.resolve("conf/sdmscollector.conf");
        FileOutputStream collectorPropsFOS = new FileOutputStream(collectorProps.toFile());
        PrintWriter collectorPropsWriter = new PrintWriter(collectorPropsFOS);
        try {
            Stream<String> lines = Files.lines(collectorPropsTemplate);
            lines.forEach(line -> {
                line = StringUtil.replaceAll(line, "[url]", url);
                line = StringUtil.replaceAll(line, "[collectorid]", collectorid);
                line = StringUtil.replaceAll(line, "[tokenvalue]", tokenvalue);
                collectorPropsWriter.println((String)line);
            });
            lines.close();
        }
        finally {
            collectorPropsWriter.flush();
            collectorPropsFOS.flush();
            collectorPropsFOS.close();
        }
        collectorPropsTemplate.toFile().delete();
        if (platform.equals("unix")) {
            this.createShellForUnix(targetFolder, collectorid);
        }
        Path wrapperConf = targetFolder.resolve("conf/wrapper.conf");
        FileOutputStream wrapperConfFOS = new FileOutputStream(wrapperConf.toFile());
        PrintWriter wrapperConfWriter = new PrintWriter(wrapperConfFOS);
        try {
            Stream<String> lines = Files.lines(wrapperConfTemplate);
            lines.forEach(line -> {
                if (line.equals("[wrapper.java.command]")) {
                    if ("Path".equals(jreoption)) {
                        wrapperConfWriter.println("#  Locate the java binary on the system PATH:");
                        wrapperConfWriter.println("labvantage.jreoption=" + jreoption);
                        wrapperConfWriter.println("wrapper.java.command=java");
                        wrapperConfWriter.println("labvantage.wrapper.bits=" + osNumberOfBits);
                    } else if ("Bundled".equals(jreoption)) {
                        wrapperConfWriter.println("#  Using bundled java binary");
                        wrapperConfWriter.println("labvantage.jreoption=" + jreoption);
                        wrapperConfWriter.println("set.JAVA_HOME=../jre");
                        wrapperConfWriter.println("wrapper.java.command=%JAVA_HOME%/bin/java");
                        wrapperConfWriter.println("labvantage.wrapper.bits=" + osNumberOfBits);
                    } else if ("JavaHome".equals(jreoption)) {
                        wrapperConfWriter.println("#  Using local java binary:");
                        wrapperConfWriter.println("labvantage.jreoption=" + jreoption);
                        wrapperConfWriter.println("wrapper.java.command=%JAVA_HOME%/bin/java");
                        wrapperConfWriter.println("labvantage.wrapper.bits=" + osNumberOfBits);
                    }
                } else if (line.equals("[wrapper.java.initmemory]")) {
                    wrapperConfWriter.println("wrapper.java.initmemory=" + initialHeapSize);
                } else if (line.equals("[wrapper.java.maxmemory]")) {
                    wrapperConfWriter.println("wrapper.java.maxmemory=" + maxHeapSize);
                } else if (line.equals("[wrapper.java.additional]")) {
                    if (javaadditional != null && javaadditional.length() > 0) {
                        String[] jvmparams = StringUtil.split(javaadditional, ";");
                        for (int i = 0; i < jvmparams.length; ++i) {
                            wrapperConfWriter.println("wrapper.java.additional." + (i + 1) + "=" + jvmparams[i]);
                        }
                    }
                } else if (line.equals("[wrapper.logfile.loglevel]")) {
                    wrapperConfWriter.println("wrapper.logfile.loglevel=" + logLevel);
                } else if (line.equals("[wrapper.logfile.maxsize]")) {
                    wrapperConfWriter.println("wrapper.logfile.maxsize=" + maxSizeofLogFile + "m");
                } else if (line.equals("[wrapper.logfile.maxfiles]")) {
                    wrapperConfWriter.println("wrapper.logfile.maxfiles=" + maxNumberofLogFiles);
                } else if (line.equals("[wrapper.check.deadlock]")) {
                    wrapperConfWriter.println("wrapper.check.deadlock=" + checkDeadLock);
                } else if (line.equals("[wrapper.event.default.email.debug]")) {
                    wrapperConfWriter.println("wrapper.event.default.email.debug=" + emailDebug);
                } else if (line.equals("[wrapper.event.default.email.smtp.host]")) {
                    wrapperConfWriter.println("wrapper.event.default.email.smtp.host=" + emailSmtpHost);
                } else if (line.equals("[wrapper.event.default.email.smtp.port]")) {
                    wrapperConfWriter.println("wrapper.event.default.email.smtp.port=" + emailSmtpPort);
                } else if (line.equals("[wrapper.event.default.email.subject]")) {
                    wrapperConfWriter.println("wrapper.event.default.email.subject=" + emailSubject);
                } else if (line.equals("[wrapper.event.default.email.sender]")) {
                    wrapperConfWriter.println("wrapper.event.default.email.sender=" + emailSender);
                } else if (line.equals("[wrapper.event.default.email.recipient]")) {
                    wrapperConfWriter.println("wrapper.event.default.email.recipient=" + emailRecipient);
                } else if (line.equals("[wrapper.event.wrapper_start.email]")) {
                    wrapperConfWriter.println("wrapper.event.wrapper_start.email=" + emailWrapperStarts);
                } else if (line.equals("[wrapper.event.jvm_deadlock.email]")) {
                    wrapperConfWriter.println("wrapper.event.jvm_deadlock.email=" + emailDeadlock);
                } else if (line.equals("[wrapper.event.jvm_failed_invocation.email]")) {
                    wrapperConfWriter.println("wrapper.event.jvm_failed_invocation.email=" + emailJvmFails);
                } else if (line.equals("[wrapper.event.jvm_max_failed_invocations.email]")) {
                    wrapperConfWriter.println("wrapper.event.jvm_max_failed_invocations.email=" + emailJvmMaxFails);
                } else if (line.equals("[wrapper.event.jvm_killed.email]")) {
                    wrapperConfWriter.println("wrapper.event.jvm_killed.email=" + emailJvmKilled);
                } else if (line.equals("[wrapper.event.jvm_unexpected_exit.email]")) {
                    wrapperConfWriter.println("wrapper.event.jvm_unexpected_exit.email=" + emailJvmUnexpectedExit);
                } else if (line.equals("[wrapper.event.jvm_restart.email.body]")) {
                    wrapperConfWriter.println("wrapper.event.jvm_restart.email.body=" + jvmRestartEmailBody);
                } else {
                    wrapperConfWriter.println(StringUtil.replaceAll(line, "[collectorid]", collectorid));
                }
            });
            lines.close();
        }
        finally {
            wrapperConfWriter.flush();
            wrapperConfFOS.flush();
            wrapperConfFOS.close();
        }
        wrapperConfTemplate.toFile().delete();
        if ("Bundled".equals(jreoption)) {
            SDMSInstallDownloadHandler.addJRE(platform, osNumberOfBits, productImageFolder, targetFolder);
        }
        if (platform.equals("windows")) {
            Path zipFile = FileUtil.createTempFile("sdmsinstaller", ".zip");
            FileTransferOptions zipCopyOptions = new FileTransferOptions();
            FileTransfer.generateZip(targetFolder.toFile().getAbsolutePath(), zipFile.toFile().getAbsolutePath(), zipCopyOptions);
            return zipFile;
        }
        Path tarGZipFile = FileUtil.createTempFile("sdmsinstaller", ".tar.gz");
        FileTransferOptions zipCopyOptions = new FileTransferOptions();
        FileTransfer.generateTarGZip(targetFolder.toFile(), tarGZipFile.toFile(), zipCopyOptions);
        return tarGZipFile;
    }

    public Path getUpgradeZip(String collectorid, JSONObject jso, boolean autoUpgrade) throws SapphireException, ServletException {
        Path platformImagePath;
        String upgrademode = jso.optString("upgrademode");
        String osBits = jso.optString("osBits");
        ConfigurationProcessor configurationProcessor = this.getConfigurationProcessor();
        PropertyList policy = configurationProcessor.getPolicy("SDMSPolicy", "Sapphire Custom");
        PropertyList installerPL = policy.getPropertyListNotNull("installer");
        Path productImageFolder = Paths.get(FileUtil.substituteConfigurationPaths(installerPL.getProperty("installimagefolder", "[labvantagehome]/console/install/externalapps/sdmscollector/product")), new String[0]);
        Path productJarFolder = Paths.get(FileUtil.substituteConfigurationPaths(installerPL.getProperty("productjarfolder", "[labvantagehome]/console/install/ear")), new String[0]);
        if (!productImageFolder.toFile().exists()) {
            throw new ServletException("Could not locate the product image path " + productImageFolder.toFile().getAbsolutePath());
        }
        if (!productJarFolder.toFile().exists()) {
            throw new ServletException("Could not locate the product jar path " + productJarFolder.toFile().getAbsolutePath());
        }
        String platform = jso.optString("platform", "windows");
        String osNumberOfBits = jso.optString("osbits", "64");
        String platformPath = platform;
        if (osNumberOfBits.equals("32")) {
            platformPath = platformPath + "_32";
        }
        if ((platformImagePath = productImageFolder.resolve(platformPath)) == null || !platformImagePath.toFile().exists()) {
            throw new ServletException("Could not locate the image path " + platformImagePath.toFile().getAbsolutePath());
        }
        try {
            Path targetFolder = Files.createTempDirectory("sdmsinstaller", new FileAttribute[0]);
            String sapphireJarName = "sapphire.jar";
            String sdmsCollectorJarName = "sdmscollector.jar";
            if (upgrademode.equals("Full")) {
                boolean includeJRE;
                this.copyImageFolderToInstall(platformImagePath, targetFolder, "bin");
                if (platform.equals("unix")) {
                    this.createShellForUnix(targetFolder, collectorid);
                }
                this.copyImageFolderToInstall(platformImagePath, targetFolder, "lib");
                this.copyImageFolderToInstall(platformImagePath, targetFolder, "libwrapper");
                Files.copy(productJarFolder.resolve(sapphireJarName), targetFolder.resolve("lib/" + sapphireJarName), new CopyOption[0]);
                Files.copy(productJarFolder.resolve(sdmsCollectorJarName), targetFolder.resolve("libwrapper/" + sdmsCollectorJarName), new CopyOption[0]);
                PropertyList startupState = SDMSUtil.getCollectorStartupState(this.getQueryProcessor(), collectorid);
                boolean bl = includeJRE = startupState == null || startupState.getProperty("jreoption").equals("Bundled");
                if (includeJRE) {
                    SDMSInstallDownloadHandler.addJRE(platform, osBits, productImageFolder, targetFolder);
                }
            } else if (upgrademode.equals("Runtime")) {
                this.copyImageFolderToInstall(platformImagePath, targetFolder, "lib");
                Files.copy(productJarFolder.resolve(sapphireJarName), targetFolder.resolve("lib/" + sapphireJarName), new CopyOption[0]);
            }
            FileTransferOptions initialCopyOptions = new FileTransferOptions();
            String customJarFolderName = installerPL.getProperty("customjarfolder");
            if (customJarFolderName.length() > 0) {
                Path targetLibCustom = targetFolder.resolve("libcustom");
                targetLibCustom.toFile().mkdirs();
                this.copyCustomJarAndConfig(targetFolder, initialCopyOptions, customJarFolderName, targetLibCustom);
            }
            if (platform.equals("windows")) {
                Path zipFile = FileUtil.createTempFile("sdmsinstaller", ".zip");
                FileTransferOptions zipCopyOptions = new FileTransferOptions();
                FileTransfer.generateZip(targetFolder.toFile().getAbsolutePath(), zipFile.toFile().getAbsolutePath(), zipCopyOptions);
                return zipFile;
            }
            Path tarGZipFile = FileUtil.createTempFile("sdmsinstaller", ".tar.gz");
            FileTransferOptions zipCopyOptions = new FileTransferOptions();
            FileTransfer.generateTarGZip(targetFolder.toFile(), tarGZipFile.toFile(), zipCopyOptions);
            return tarGZipFile;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to generate upgrade zip: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }

    private void copyCustomJarAndConfig(Path targetFolder, FileTransferOptions initialCopyOptions, String customJarFolderName, Path targetLibCustom) throws ServletException, IOException {
        if (customJarFolderName.length() > 0) {
            Path customJarFolder = Paths.get(FileUtil.substituteConfigurationPaths(customJarFolderName), new String[0]);
            if (!customJarFolder.toFile().exists()) {
                throw new ServletException("Could not locate the custom image path " + customJarFolderName);
            }
            Path libCustomPath = customJarFolder.resolve("libcustom");
            if (libCustomPath.toFile().exists()) {
                FileTransfer.copyDirectory(libCustomPath.toFile(), targetLibCustom.toFile(), initialCopyOptions);
            } else {
                targetLibCustom.toFile().mkdir();
            }
            Path customConfFile = customJarFolder.resolve("conf/wrapper-custom.conf");
            if (customConfFile.toFile().exists()) {
                Path conf = targetFolder.resolve("conf");
                conf.toFile().mkdir();
                Files.copy(customConfFile, targetFolder.resolve("conf/wrapper-custom.conf"), new CopyOption[0]);
            }
        }
    }

    public static void addJRE(String platform, String osBits, Path productPath, Path tempDir) throws IOException {
        Path jrePath = null;
        if ("windows".equals(platform)) {
            jrePath = osBits.equals("32") ? productPath.resolve("windows_jre_32") : productPath.resolve("windows_jre");
        } else if ("unix".equals(platform)) {
            jrePath = osBits.equals("32") ? productPath.resolve("unix_jre_32") : productPath.resolve("unix_jre");
        }
        Path target = tempDir.resolve("jre");
        target.toFile().mkdir();
        FileTransferOptions jreCopyOptions = new FileTransferOptions();
        FileTransfer.copyDirectory(jrePath.toFile(), target.toFile(), jreCopyOptions);
    }

    private void copyImageFolderToInstall(Path imagePath, Path upgradeFolder, String foldername) throws IOException {
        FileTransferOptions initialCopyOptions = new FileTransferOptions();
        Path tempLib = upgradeFolder.resolve(foldername);
        tempLib.toFile().mkdirs();
        FileTransfer.copyDirectory(imagePath.resolve(foldername).toFile(), tempLib.toFile(), initialCopyOptions);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void streamFileOut(HttpServletResponse response, String downloadFilePath, String downloadFileName, boolean deleteDownloadFile) throws IOException {
        File file = new File(downloadFilePath);
        BufferedInputStream input = null;
        FilterOutputStream output = null;
        try {
            int length;
            input = new BufferedInputStream(new FileInputStream(file), 8192);
            output = new BufferedOutputStream((OutputStream)response.getOutputStream(), 8192);
            response.setContentType(downloadFileName.endsWith("zip") ? "application/zip" : "text");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFileName + "\"");
            byte[] buffer = new byte[8192];
            while ((length = input.read(buffer)) > -1) {
                ((BufferedOutputStream)output).write(buffer, 0, length);
            }
            ((BufferedOutputStream)output).flush();
        }
        finally {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (file.exists() && deleteDownloadFile) {
                file.delete();
            }
        }
    }
}

