/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 */
package com.labvantage.sapphire.platform.weblogic;

import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.ServiceException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;
import javax.servlet.ServletContext;
import sapphire.SapphireException;

public class WeblogicConfiguration
extends Configuration {
    @Override
    public String getHttpPort() throws SapphireException {
        try {
            return ConfigService.getConfigProperty("com.labvantage.sapphire.server.httpport", "7001");
        }
        catch (ServiceException e) {
            return "7001";
        }
    }

    @Override
    public String getDefaultProviderPort() {
        return "7001";
    }

    @Override
    public String getProviderPort() {
        try {
            return ConfigService.getConfigProperty("com.labvantage.sapphire.server.providerport", "7001");
        }
        catch (Exception e) {
            return this.getDefaultProviderPort();
        }
    }

    @Override
    public String getInitialContextFactory() {
        return "weblogic.jndi.WLInitialContextFactory";
    }

    @Override
    public String getProviderURL() throws SapphireException {
        return "t3://" + this.getServerHostName() + ":" + this.getProviderPort();
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
            return servletContext.getResource("/").getFile();
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public String getJNDIDataSourcePrefix() {
        return "";
    }

    @Override
    public String getJNDILocalEJBPrefix() {
        return "";
    }

    @Override
    public String getJCEProvider() {
        String jceProvider = "SunJCE";
        Properties prop = System.getProperties();
        if (prop.getProperty("java.vendor").equals("IBM Corporation")) {
            jceProvider = "IBMJCE";
        }
        return jceProvider;
    }

    @Override
    public String getServerVersion() {
        return "Not available";
    }
}

