/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.servlet.BaseController;
import com.labvantage.sapphire.servlet.ConsoleController;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import sapphire.xml.PropertyList;

public class InitConsoleTask
extends Task {
    private String sapphirehome = "";
    private String serverid = "";
    private String serverinfo = "";
    private String admindbdbms = "";
    private String admindbjndiname = "admindb";

    public void setSapphirehome(String sapphirehome) {
        this.sapphirehome = sapphirehome;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public void setServerinfo(String serverinfo) {
        this.serverinfo = serverinfo;
    }

    public void setAdmindbdbms(String admindbdbms) {
        this.admindbdbms = admindbdbms;
    }

    public void setAdmindbjndiname(String admindbjndiname) {
        this.admindbjndiname = admindbjndiname;
    }

    public void execute() throws BuildException {
        PropertyList consoleConfig = new PropertyList();
        consoleConfig.setProperty("admindbms", this.admindbdbms);
        consoleConfig.setProperty("admindb", this.admindbjndiname);
        try {
            ConsoleController.initConsoleLoggingProps(this.serverid, this.sapphirehome);
            ConsoleController.saveConsoleConfig(consoleConfig, BaseController.getConfiguration(this.serverinfo, this.sapphirehome, this.serverid));
        }
        catch (IOException e) {
            throw new BuildException(e.getMessage());
        }
    }
}

