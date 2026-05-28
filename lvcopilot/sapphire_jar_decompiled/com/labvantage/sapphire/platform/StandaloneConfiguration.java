/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 */
package com.labvantage.sapphire.platform;

import com.labvantage.sapphire.platform.Configuration;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import sapphire.SapphireException;

public class StandaloneConfiguration
extends Configuration {
    @Override
    public String getHttpPort() throws SapphireException {
        return "8080";
    }

    @Override
    public String getDefaultProviderPort() {
        return "9000";
    }

    @Override
    public String getProviderPort() {
        return this.getDefaultProviderPort();
    }

    @Override
    public String getInitialContextFactory() {
        return null;
    }

    @Override
    public String getProviderURL() throws SapphireException {
        return null;
    }

    @Override
    public void checkPlatformConfiguration(ArrayList errorList) throws SapphireException {
    }

    @Override
    public void applyPatches(ArrayList errorList) throws SapphireException {
    }

    @Override
    public String getAppRoot(ServletContext servletContext) {
        return null;
    }

    @Override
    public String getWebAppRoot(ServletContext servletContext) {
        return null;
    }

    @Override
    public String getJNDIDataSourcePrefix() {
        return null;
    }

    @Override
    public String getJNDILocalEJBPrefix() {
        return null;
    }

    @Override
    public String getJCEProvider() {
        return null;
    }

    @Override
    public String getServerVersion() {
        return "Not available";
    }
}

