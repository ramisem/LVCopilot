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
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class UpgradeEarTask
extends Task {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private String sapphirehome = "";
    private String serverinfo = "";
    private String serverid = "";
    private String appddfiles = "";
    private String ejbddfiles = "";
    private String webddfiles = "";
    private String sapphirejarfiles = "";
    private String sapphirewarfiles = "";
    private String otherjarfiles = "";
    ConnectionTask connection;

    public void setSapphirehome(String sapphirehome) {
        this.sapphirehome = sapphirehome;
    }

    public void setServerinfo(String serverinfo) {
        this.serverinfo = serverinfo;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public void setAppddfiles(String appddfiles) {
        this.appddfiles = appddfiles;
    }

    public void setEjbddfiles(String ejbddfiles) {
        this.ejbddfiles = ejbddfiles;
    }

    public void setWebddfiles(String webddfiles) {
        this.webddfiles = webddfiles;
    }

    public void setSapphirejarfiles(String sapphirejarfiles) {
        this.sapphirejarfiles = sapphirejarfiles;
    }

    public void setSapphirewarfiles(String sapphirewarfiles) {
        this.sapphirewarfiles = sapphirewarfiles;
    }

    public void setOtherjarfiles(String otherjarfiles) {
        this.otherjarfiles = otherjarfiles;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void execute() throws BuildException {
        String appid = "";
        String source = "";
        String applicationtype = "";
        String managedear = "";
        if (this.connection == null) {
            throw new BuildException("Connection task not defined");
        }
        DBUtil adminDb = null;
        try {
            adminDb = this.connection.getConnection();
            adminDb.createResultSet("select * from Application");
            DataSet applicationDs = new DataSet(adminDb.getResultSet());
            System.out.println(applicationDs.toHTML());
            if (applicationDs.getRowCount() != 1) {
                throw new SapphireException("Application information not found or invalid in admindb");
            }
            appid = applicationDs.getValue(0, "applicationid");
            applicationtype = applicationDs.getValue(0, "typeflag");
            managedear = applicationDs.getValue(0, "managedear");
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        finally {
            if (adminDb != null) {
                adminDb.releaseConnection();
                adminDb.reset();
            }
        }
        PropertyList params = new PropertyList();
        System.out.println("appid: " + appid);
        params.setProperty("applicationid", appid);
        params.setProperty("sapphire.home", this.sapphirehome);
        params.setProperty("managedear", managedear);
        params.setProperty("deployed", "Y");
        Configuration configuration = BaseController.getConfiguration(this.serverinfo, this.sapphirehome, this.serverid);
        params.setProperty("platform", String.valueOf(configuration.getPlatform()));
        try {
            if (applicationtype.equals("P")) {
                ConsoleController.upgradeEAR(configuration, null, params);
            } else if (applicationtype.equals("J")) {
                params.setProperty("appddfiles", this.appddfiles);
                params.setProperty("ejbddfiles", this.ejbddfiles);
                params.setProperty("webddfiles", this.webddfiles);
                params.setProperty("sapphirejarfiles", this.sapphirejarfiles);
                params.setProperty("sapphirewarfiles", this.sapphirewarfiles);
                params.setProperty("otherjarfiles", this.otherjarfiles);
                ConsoleController.upgradeExplodedEAR(configuration, null, params);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connection = connection;
    }
}

