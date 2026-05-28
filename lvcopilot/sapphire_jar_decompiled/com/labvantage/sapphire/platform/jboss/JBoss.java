/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.platform.jboss;

import com.labvantage.sapphire.util.file.ConsoleFileOperationProgress;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public interface JBoss {
    public File getDeploymentDir();

    public boolean isDeployed(String var1);

    public HashMap getDeployedApplications(File var1);

    public void getExplodedApps(DataSet var1);

    public void getApplicationUpgradeFiles(String var1, PropertyList var2, Properties var3);

    public void deleteApplicationDir(String var1, ConsoleFileOperationProgress var2) throws IOException;

    public void deployEAR(File var1, String var2, File var3, boolean var4, ConsoleFileOperationProgress var5) throws IOException, SapphireException;

    public void undeployEAR(File var1, String var2, File var3, ConsoleFileOperationProgress var4) throws IOException, SapphireException;
}

