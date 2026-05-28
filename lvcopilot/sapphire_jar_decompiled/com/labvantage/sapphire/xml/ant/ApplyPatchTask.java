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
import com.labvantage.sapphire.platform.jboss.JBoss;
import com.labvantage.sapphire.servlet.BaseController;
import com.labvantage.sapphire.servlet.ConsoleController;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ApplyPatchTask
extends Task {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private String sapphirehome = "";
    private String serverid = "";
    private String managedear = "";
    private String serverinfo = "";
    private String databaseid = "";
    private String patchzipfile = "";
    private String applymode = "";
    private String patchid = "";
    List<ConnectionTask> connections = new ArrayList<ConnectionTask>();

    public void setPatchid(String patchid) {
        this.patchid = patchid;
    }

    public void setManagedear(String managedear) {
        this.managedear = managedear;
    }

    public void setDatabaseid(String databaseid) {
        this.databaseid = databaseid;
    }

    public void setPatchzipfile(String patchzipfile) {
        this.patchzipfile = patchzipfile;
        if (this.patchid.isEmpty()) {
            this.patchid = patchzipfile;
        }
    }

    public void setSapphirehome(String sapphirehome) {
        this.sapphirehome = sapphirehome;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public void setServerinfo(String serverinfo) {
        this.serverinfo = serverinfo;
    }

    public void setApplymode(String applymode) {
        this.applymode = applymode;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void execute() throws BuildException {
        if (this.connections.size() != 2) {
            throw new BuildException("Connection task not defined properly! Both AdminDB and LabVantage DB tasks need to be defined!");
        }
        if (this.patchid == null) {
            throw new BuildException("Patch ID not defined!");
        }
        String appid = "";
        String applicationtype = "";
        DBUtil adminDb = null;
        ConnectionTask labvantageConnection = this.connections.get(1);
        DBUtil labvantageDb = labvantageConnection.getConnection();
        try {
            adminDb = this.connections.get(0).getConnection();
            adminDb.createResultSet("select * from Application");
            DataSet applicationDs = new DataSet(adminDb.getResultSet());
            System.out.println(applicationDs.toHTML());
            if (applicationDs.getRowCount() != 1) {
                throw new SapphireException("Application information not found or invalid in admindb");
            }
            appid = applicationDs.getValue(0, "applicationid");
            applicationtype = applicationDs.getValue(0, "typeflag");
            this.managedear = applicationDs.getValue(0, "managedear");
            adminDb.closeResultSet();
            adminDb.createPreparedResultSet("select * from databaselist where databaselistid = ?", new String[]{this.databaseid});
            DataSet databasesDs = new DataSet(adminDb.getResultSet());
            if (databasesDs.getRowCount() != 1) {
                throw new BuildException("Database: " + this.databaseid + " not found in AdminDB!");
            }
            adminDb.closeResultSet();
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
        params.setProperty("patchid", this.patchid);
        params.setProperty("patchzipfile", this.patchzipfile);
        params.setProperty("applicationid", appid);
        params.setProperty("sapphire.home", this.sapphirehome);
        params.setProperty("type", applicationtype);
        params.setProperty("managedear", this.managedear);
        params.setProperty("applymode", this.applymode);
        params.setProperty("jndiname", "");
        params.setProperty("jndiprefix", "");
        params.setProperty("sapphire.db.hoststring", labvantageConnection.getHoststring());
        params.setProperty("sapphire.db.sqldatabase", labvantageConnection.getSqldatabase());
        params.setProperty("sapphire.db.username", labvantageConnection.getUsername());
        params.setProperty("sapphire.db.password", labvantageConnection.getPassword());
        params.setProperty("sapphire.db.instancename", labvantageConnection.getInstancename());
        params.setProperty("sapphire.db.sid", labvantageConnection.getSid());
        params.setProperty("sapphire.db.port", labvantageConnection.getPort());
        Configuration configuration = BaseController.getConfiguration(this.serverinfo, this.sapphirehome, this.serverid);
        params.setProperty("platform", String.valueOf(configuration.getPlatform()));
        String deployed = configuration instanceof JBoss && applicationtype.equals("P") ? "Y" : "N";
        params.setProperty("deployed", deployed);
        try {
            ConsoleController.applyPatch(configuration, null, params, adminDb, labvantageDb);
            if (adminDb != null && adminDb.getConnection() != null) {
                adminDb.getConnection().commit();
            }
            if (labvantageDb != null && labvantageDb.getConnection() != null) {
                labvantageDb.getConnection().commit();
            }
        }
        catch (IOException | SQLException | SapphireException exception) {
            throw new BuildException("Applying patch failed: " + exception.getMessage());
        }
        finally {
            if (adminDb != null) {
                adminDb.reset();
            }
            if (labvantageDb != null) {
                labvantageDb.reset();
            }
        }
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connections.add(connection);
    }
}

