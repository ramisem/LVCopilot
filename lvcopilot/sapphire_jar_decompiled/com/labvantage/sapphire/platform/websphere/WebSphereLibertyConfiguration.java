/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.platform.websphere;

import com.labvantage.sapphire.platform.websphere.WebSphereConfiguration;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class WebSphereLibertyConfiguration
extends WebSphereConfiguration {
    @Override
    public String getJNDILocalEJBPrefix() {
        return "java:global/" + this.getApplicationEARName() + "/sapphire/[ejbname]!com.labvantage.sapphire.ejb.[ejbname]HomeLocal";
    }

    public ThreadFactory getThreadFactory() {
        try {
            return (ThreadFactory)new InitialContext().lookup("java:comp/DefaultManagedThreadFactory");
        }
        catch (NamingException e) {
            return Executors.defaultThreadFactory();
        }
    }
}

