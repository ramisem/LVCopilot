/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.servlet.BaseController;
import com.labvantage.sapphire.servlet.ConsoleController;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import sapphire.xml.PropertyList;

public class InstallAppTask
extends Task {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private String sapphirehome = "";
    private String serverid = "";
    private String appid = "";
    private String serverinfo = "";
    private String source = "";
    private String applicationtype = "";
    private String addclusternode = "";
    private String useexistinghome = "";
    ConnectionTask connection;
    private String customear = "";
    private String externaljarlist = "";
    private String applicationjar = "";
    private String applicationwar = "";
    private String applicationwebdir = "";
    private String webcontext = "";
    private String ejbprefix = "";

    public void setApplicationtype(String applicationtype) {
        this.applicationtype = applicationtype;
    }

    public void setSapphirehome(String sapphirehome) {
        this.sapphirehome = sapphirehome;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public void setServerinfo(String serverinfo) {
        this.serverinfo = serverinfo;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setAddClusterNode(String addclusternode) {
        this.addclusternode = addclusternode;
    }

    public void setUseexistinghome(String useexistinghome) {
        this.useexistinghome = useexistinghome;
    }

    public void setCustomear(String customear) {
        this.customear = customear;
    }

    public void setExternaljarlist(String externaljarlist) {
        this.externaljarlist = externaljarlist;
    }

    public void setApplicationjar(String applicationjar) {
        this.applicationjar = applicationjar;
    }

    public void setApplicationwar(String applicationwar) {
        this.applicationwar = applicationwar;
    }

    public void setApplicationwebdir(String applicationwebdir) {
        this.applicationwebdir = applicationwebdir;
    }

    public void setWebcontext(String webcontext) {
        this.webcontext = webcontext;
    }

    public void setEjbprefix(String ejbprefix) {
        this.ejbprefix = ejbprefix;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void execute() throws BuildException {
        if (this.connection == null) {
            throw new BuildException("Connection task not defined");
        }
        if (this.source == null) {
            this.source = "labvantageear";
        }
        DBUtil dbu = this.connection.getConnection();
        PropertyList params = new PropertyList();
        params.setProperty("applicationid", this.appid);
        params.setProperty("sapphire.home", this.sapphirehome);
        params.setProperty("applicationtype", this.applicationtype);
        params.setProperty("applicationdesc", this.appid);
        params.setProperty("source", this.source);
        params.setProperty("addclusternode", this.addclusternode);
        params.setProperty("deployear", "Y");
        if (this.applicationtype.equals("J")) {
            if (!this.useexistinghome.isEmpty()) {
                params.setProperty("useexistinghome", this.useexistinghome);
            }
            if (!this.customear.isEmpty()) {
                params.setProperty("customear", this.customear);
            }
            if (!this.externaljarlist.isEmpty()) {
                params.setProperty("externaljarlist", this.externaljarlist);
            }
            if (!this.applicationjar.isEmpty()) {
                params.setProperty("applicationjar", this.applicationjar);
            }
            if (!this.applicationwar.isEmpty()) {
                params.setProperty("applicationwar", this.applicationwar);
            }
            if (!this.applicationwebdir.isEmpty()) {
                params.setProperty("applicationwebdir", this.applicationwebdir);
            }
            if (!this.webcontext.isEmpty()) {
                params.setProperty("webcontext", this.webcontext);
            }
            if (!this.ejbprefix.isEmpty()) {
                params.setProperty("ejbprefix", this.ejbprefix);
            }
        }
        Configuration configuration = BaseController.getConfiguration(this.serverinfo, this.sapphirehome, this.serverid);
        params.setProperty("platform", String.valueOf(configuration.getPlatform()));
        try {
            ConsoleController.installApp(configuration, null, params, dbu);
            dbu.getConnection().commit();
        }
        catch (IOException | SQLException exception) {
        }
        finally {
            dbu.reset();
        }
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connection = connection;
    }
}

