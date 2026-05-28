/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Build {
    private static Properties buildProperties = Build.getBuildProperties();

    private static Properties getBuildProperties() {
        Properties buildProperties;
        block15: {
            buildProperties = new Properties();
            try (InputStream input = Build.class.getResourceAsStream("/labvantage.build.props");){
                if (input != null) {
                    buildProperties.load(input);
                    break block15;
                }
                throw new IOException("labvantage.build.props file not found in classpath!");
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("labvantage.build.props file not found in classpath!", e);
            }
        }
        return buildProperties;
    }

    public static String getBuild() {
        return buildProperties.getProperty("DM.number") + "." + buildProperties.getProperty("build") + "." + buildProperties.getProperty("subbuild");
    }

    public static String getReleaseVersion() {
        return buildProperties.getProperty("release.version");
    }

    public static String getAppServerBuild() {
        return Build.getBuild() + buildProperties.getProperty("appserverbuild");
    }

    public static String getVersion() {
        return buildProperties.getProperty("version");
    }

    public static String getVersionSuffix() {
        return buildProperties.getProperty("versionsuffix");
    }

    public static String getVersionIdentifier() {
        return buildProperties.getProperty("versionidentifier");
    }

    public static String getPatch() {
        return buildProperties.getProperty("patch");
    }

    public static String getUpgradePatch() {
        return buildProperties.getProperty("upgradepatch");
    }

    public static String getBuildDate() {
        return buildProperties.getProperty("build.date");
    }
}

