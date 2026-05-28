/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.RemoteAccessKey;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import sapphire.SapphireException;

public class GenerateRAKTask
extends Task {
    private File file;
    private String platformName = "JBoss";
    private String initialContextFactory = "org.jnp.interfaces.NamingContextFactory";
    private String applicationid = "";
    private String applicationear = "";
    private String databaseid = "";
    private String serverURL = "";
    private String username = "";
    private String password = "";
    private String jndiPrefix = "com/labvantage/sapphire";

    public void setFile(File file) {
        this.file = file;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }

    public void setApplicationid(String applicationid) {
        this.applicationid = applicationid;
    }

    public void setApplicationear(String applicationear) {
        this.applicationear = applicationear;
    }

    public void setDatabaseid(String databaseid) {
        this.databaseid = databaseid;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setJndiPrefix(String jndiPrefix) {
        this.jndiPrefix = jndiPrefix;
    }

    public void execute() {
        try {
            if (this.applicationear == null || this.applicationear.length() == 0) {
                this.applicationear = this.applicationid;
            }
            RemoteAccessKey.generateRemoteAccessKey(this.file, this.platformName, this.initialContextFactory, this.applicationid, this.applicationear, this.databaseid, this.serverURL, this.username, this.password, this.jndiPrefix);
        }
        catch (SapphireException e) {
            throw new BuildException("Failed to generate RAK file. Reason: " + e.getMessage());
        }
    }
}

