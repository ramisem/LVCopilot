/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import javax.servlet.ServletContext;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class JBoss6xConfiguration
extends Configuration
implements JBoss {
    protected static final String serverdir = new File(System.getProperty("jboss.server.base.dir")).getAbsolutePath();

    @Override
    public String getJNDIDataSourcePrefix() {
        return "java:jboss/datasources/";
    }

    @Override
    public String getJNDILocalEJBPrefix() {
        return "java:global/" + this.getApplicationEARName() + "/sapphire/[ejbname]!com.labvantage.sapphire.ejb.[ejbname]Local";
    }

    @Override
    public String getJCEProvider() {
        return "SunJCE";
    }

    @Override
    public String getServerVersion() {
        return "6x";
    }

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
        return "4447";
    }

    @Override
    public String getProviderPort() {
        try {
            return ConfigService.getConfigProperty("com.labvantage.sapphire.server.providerport", this.getDefaultProviderPort());
        }
        catch (Exception e) {
            return this.getDefaultProviderPort();
        }
    }

    @Override
    public String getInitialContextFactory() {
        return "org.jboss.naming.remote.client.InitialContextFactory";
    }

    @Override
    public String getProviderURL() throws SapphireException {
        return "remote://" + this.getServerHostName() + ":" + this.getProviderPort();
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
    public PropertyList getPlatformProperties() {
        PropertyList platformProps = new PropertyList();
        PropertyListCollection errors = new PropertyListCollection();
        platformProps.setProperty("errors", errors);
        String basedir = System.getProperty("jboss.server.base.dir");
        platformProps.setProperty("serverinstance", basedir.substring(basedir.lastIndexOf(File.separator) + 1));
        platformProps.setProperty("jboss.deploy", this.getDeploymentDir().getAbsolutePath());
        return platformProps;
    }

    @Override
    public File getDeploymentDir() {
        return new File(serverdir, "deployments");
    }

    @Override
    public boolean isDeployed(String managedEAR) {
        return new File(this.getDeploymentDir(), managedEAR).exists() && new File(this.getDeploymentDir(), managedEAR + ".deployed").exists();
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
                if (!appBuildProps.exists() || !new File(this.getDeploymentDir(), file.getName() + ".deployed").exists() || (applicationid = (appProps = ConsoleController.loadPropertiesFile(appBuildProps)).getProperty("application.id")).length() <= 0) continue;
                applications.put(applicationid, file.getName());
                applications.put(applicationid + "_exploded", exploded ? "Y" : "N");
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return applications;
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
    public void deleteApplicationDir(String managedEAR, ConsoleFileOperationProgress progress) throws IOException {
        File earDir = new File(this.getDeploymentDir(), managedEAR);
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
        cvs = new File(earDir, "/lib/CVS");
        upgradeProps.setProperty("otherjarfiles", cvs.exists() && cvs.isDirectory() ? "N" : "Y");
        String upgradenote = "";
        if (!new File(earDir, "lib").exists()) {
            upgradenote = "NOTE: Old EAR format! Existing JARs will be moved to a lib subdirectory of the EAR.";
            if (appBuildProps.getProperty("application.war").equals("sapphire.war")) {
                upgradenote = upgradenote + " Existing sapphire.war will be renamed to labvantage.war.";
            }
        }
        upgradeProps.setProperty("upgradenote", upgradenote);
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
        File undeployEAR = new File(this.getDeploymentDir(), managedEAR.getName());
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
}

