/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.platform.jboss;

import com.labvantage.sapphire.platform.jboss.JBoss;
import com.labvantage.sapphire.platform.jboss.JBoss6xConfiguration;
import sapphire.SapphireException;

public class JBoss7xConfiguration
extends JBoss6xConfiguration
implements JBoss {
    @Override
    public String getServerVersion() {
        return "7x";
    }

    @Override
    public String getDefaultProviderPort() {
        return "8080";
    }

    @Override
    public String getJNDILocalEJBPrefix() {
        return super.getJNDILocalEJBPrefix();
    }

    @Override
    public String getInitialContextFactory() {
        return "org.wildfly.naming.client.WildFlyInitialContextFactory";
    }

    @Override
    public String getProviderURL() throws SapphireException {
        return "http-remoting://" + this.getServerHostName() + ":" + this.getProviderPort();
    }
}

