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
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ConfigurePortalTask
extends Task {
    private String sapphirehome = "";
    private String serverid = "";
    private String managedear = "";
    private String serverinfo = "";
    private String[] databaseid = new String[0];
    private String applicationtype = "";
    private String[] servlet = new String[0];
    private String[] portal = new String[0];
    private String[] debug = new String[0];
    private String[] encrypt = new String[0];
    private String[] encryptcookies = new String[0];
    private String[] autorefreshproperties = new String[0];
    private String portalonly = "";
    private String[] urlpattern = new String[0];
    List<ConnectionTask> connections = new ArrayList<ConnectionTask>();

    public void setManagedear(String managedear) {
        this.managedear = managedear;
    }

    public void setDatabaseid(String databaseid) {
        this.databaseid = databaseid.split(",");
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

    public void setServlet(String servlet) {
        this.servlet = servlet.split(",");
    }

    public void setPortal(String portal) {
        this.portal = portal.split(",");
    }

    public void setDebug(String debug) {
        this.debug = debug.split(",");
    }

    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt.split(",");
    }

    public void setEncryptCookies(String encryptcookies) {
        this.encryptcookies = encryptcookies.split(",");
    }

    public void setAutorefreshproperties(String autorefreshproperties) {
        this.autorefreshproperties = autorefreshproperties.split(",");
    }

    public void setPortalonly(String portalonly) {
        this.portalonly = portalonly;
    }

    public void setUrlpattern(String urlpattern) {
        this.urlpattern = urlpattern.split(",");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void execute() throws BuildException {
        if (this.connections.size() != 1) {
            throw new BuildException("Connection task not defined properly! AdminDB DB task not defined!");
        }
        if (this.databaseid.length == 0) {
            throw new BuildException("Databaseid not defined!");
        }
        if (this.databaseid.length != this.servlet.length) {
            throw new BuildException("Servlet parameter count does not match databaseid count.");
        }
        if (this.databaseid.length != this.urlpattern.length) {
            throw new BuildException("Urlpattern parameter count does not match databaseid count.");
        }
        if (this.portal.length == 0) {
            throw new BuildException("Portal not defined!");
        }
        if (this.portal.length > 1 && this.databaseid.length != this.portal.length) {
            throw new BuildException("Portal parameter count does not match databaseid count.");
        }
        if (this.debug.length > 1 && this.databaseid.length != this.debug.length) {
            throw new BuildException("Debug parameter count does not match databaseid count.");
        }
        if (this.encrypt.length > 1 && this.databaseid.length != this.encrypt.length) {
            throw new BuildException("Encrypt parameter count does not match databaseid count.");
        }
        if (this.encryptcookies.length > 1 && this.databaseid.length != this.encryptcookies.length) {
            throw new BuildException("Encryptcookies parameter count does not match databaseid count.");
        }
        if (this.autorefreshproperties.length > 1 && this.databaseid.length != this.autorefreshproperties.length) {
            throw new BuildException("Autorefreshproperties parameter count does not match databaseid count.");
        }
        Configuration configuration = BaseController.getConfiguration(this.serverinfo, this.sapphirehome, this.serverid);
        DBUtil adminDb = null;
        String appid = "";
        try {
            adminDb = this.connections.get(0).getConnection();
            adminDb.createResultSet("select * from Application");
            DataSet applicationDs = new DataSet(adminDb.getResultSet());
            if (applicationDs.getRowCount() != 1) {
                throw new SapphireException("Application information not found or invalid in admindb");
            }
            appid = applicationDs.getValue(0, "applicationid");
            this.applicationtype = applicationDs.getValue(0, "typeflag");
            this.managedear = applicationDs.getValue(0, "managedear");
            adminDb.closeResultSet();
            adminDb.createResultSet("select databaselistid from databaselist");
            DataSet databasesDs = new DataSet(adminDb.getResultSet());
            if (this.databaseid.length > 0 && databasesDs.getRowCount() > 0) {
                for (String db : this.databaseid) {
                    if (databasesDs.findRow("databaselistid", db) != -1) continue;
                    throw new BuildException("Database: " + db + " not found in AdminDB!");
                }
            } else {
                throw new BuildException("Database not found in AdminDB!");
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
        String deployed = configuration instanceof JBoss && this.applicationtype.equals("P") ? "Y" : "N";
        ArrayList<PropertyList> portalProperties = new ArrayList<PropertyList>();
        for (int i = 0; i < this.databaseid.length; ++i) {
            PropertyList params = new PropertyList();
            params.setProperty("applicationid", appid);
            params.setProperty("sapphire.home", this.sapphirehome);
            params.setProperty("type", this.applicationtype);
            params.setProperty("managedear", this.managedear);
            params.setProperty("deployed", deployed);
            params.setProperty("platform", String.valueOf(configuration.getPlatform()));
            params.setProperty("databaseid", this.databaseid[i]);
            params.setProperty("urlpattern", this.urlpattern[i]);
            params.setProperty("servlet", this.parsePortalProperties(i, this.servlet));
            params.setProperty("portal", this.portal[i]);
            params.setProperty("debug", this.parsePortalProperties(i, this.debug));
            params.setProperty("encrypt", this.parsePortalProperties(i, this.encrypt));
            params.setProperty("encryptcookies", this.parsePortalProperties(i, this.encryptcookies));
            params.setProperty("autorefreshproperties", this.parsePortalProperties(i, this.autorefreshproperties));
            params.setProperty("portalonly", this.portalonly);
            portalProperties.add(params);
        }
        try {
            ConsoleController.configurePortalWebXML(configuration, null, portalProperties);
        }
        catch (IOException exception) {
            throw new BuildException("Configure Portal web.xml failed: " + exception.getMessage());
        }
    }

    public String parsePortalProperties(int index, String[] param) {
        if (param.length > 0) {
            if (param.length > 1) {
                return param[index];
            }
            return param[0];
        }
        return "";
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connections.add(connection);
    }
}

