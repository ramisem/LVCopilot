/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.License;
import com.labvantage.sapphire.admin.install.Installer;
import com.labvantage.sapphire.console.diagnostics.BaseDiagnostic;
import com.labvantage.sapphire.console.diagnostics.DiagnosticsUtils;
import com.labvantage.sapphire.servlet.ConsoleController;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import com.labvantage.sapphire.xml.ant.ServerTask;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;

public class InstallerTask
extends Task {
    boolean admindb = false;
    String installtarget;
    String upgradetarget;
    String upgrademode;
    File licensefile;
    File databaselicensefile;
    String licensetext;
    boolean resetdblicense = false;
    ServerTask server;
    ArrayList connections = new ArrayList();

    public void setInstalltarget(String installtarget) {
        this.installtarget = installtarget;
    }

    public void setUpgradetarget(String upgradetarget) {
        this.upgradetarget = upgradetarget;
    }

    public void setUpgrademode(String upgrademode) {
        this.upgrademode = upgrademode;
    }

    public void setLicensefile(File licensefile) {
        this.licensefile = licensefile;
    }

    public void setDatabaselicensefile(File databaselicensefile) {
        this.databaselicensefile = databaselicensefile;
        if ((this.licensetext == null || this.licensetext.isEmpty()) && databaselicensefile != null && databaselicensefile.isFile()) {
            try {
                this.licensetext = License.loadLicenseString(databaselicensefile);
            }
            catch (Exception e) {
                throw new BuildException("Failed to read database license file.");
            }
        }
    }

    public void setLicensetext(String licensetext) {
        if ((this.licensetext == null || this.licensetext.isEmpty()) && licensetext != null && !licensetext.isEmpty()) {
            this.licensetext = licensetext;
        }
    }

    public void setResetdblicense(String resetdblicense) {
        this.resetdblicense = resetdblicense != null && resetdblicense.equalsIgnoreCase("true");
    }

    public void setAdmindb(boolean admindb) {
        this.admindb = admindb;
    }

    /*
     * Unable to fully structure code
     */
    public void execute() throws BuildException {
        if (this.installtarget == null || this.installtarget.length() == 0) {
            throw new BuildException("Install target must be defined");
        }
        if (this.upgradetarget == null || this.upgradetarget.length() == 0) {
            throw new BuildException("Upgrade target must be defined");
        }
        if (this.admindb) {
            if (this.connections.size() != 1) {
                throw new BuildException("An AdminDb connection task only must be defined");
            }
            admindbConnection = (ConnectionTask)this.connections.get(0);
            admindb = admindbConnection.getConnection();
            try {
                admindb.getConnection().setAutoCommit(true);
                if (admindb.isOracle()) {
                    Installer.checkInstallPrivs(admindb.getConnection());
                } else {
                    Installer.checkRCSOn(admindb.getConnection());
                }
                target = this.upgradetarget;
                try {
                    admindb.createResultSet("SELECT * FROM adminconfig");
                    admindb.getNext();
                }
                catch (SapphireException se) {
                    target = this.installtarget;
                }
                this.getProject().executeTarget(target);
                admindb.createResultSet("SELECT propertyvalue FROM adminconfig WHERE propertyid = 'lastbuild'");
                if (admindb.getNext()) {
                    if (!admindb.getString("propertyvalue").equals(Build.getBuild())) {
                        admindb.executeUpdate("UPDATE adminconfig SET propertyvalue = ( SELECT propertyvalue FROM adminconfig WHERE propertyid = 'build' ) WHERE propertyid = 'lastbuild'");
                    }
                } else {
                    admindb.executeSQL("INSERT INTO adminconfig ( propertyid, propertyvalue ) SELECT 'lastbuild', propertyvalue FROM adminconfig WHERE propertyid = 'build'");
                }
                if (admindb.executeUpdate("UPDATE adminconfig SET propertyvalue = '" + Build.getBuild() + "' WHERE propertyid = 'build'") == 1) ** GOTO lbl264
                admindb.executeSQL("INSERT INTO adminconfig ( propertyid, propertyvalue ) VALUES ( 'build', '" + Build.getBuild() + "' )");
            }
            catch (SapphireException se) {
                throw new BuildException("SapphireException: " + se.getMessage());
            }
            catch (SQLException sqle) {
                throw new BuildException("SQLException: " + sqle.getMessage());
            }
            finally {
                admindb.reset();
            }
        } else {
            if (!(this.licensefile != null && this.licensefile.exists() || this.licensetext != null && !this.licensetext.isEmpty())) {
                throw new BuildException("A valid license must be defined");
            }
            if (this.connections.size() < 2) {
                throw new BuildException("An AdminDb connection task plus at least 1 Sapphire database connection task must be defined");
            }
            admindbConnection = (ConnectionTask)this.connections.get(0);
            if (!admindbConnection.getDatabaseid().equals("AdminDb")) {
                throw new BuildException("The first connection task must have a databaseid of AdminDb and define the connection to the Admin Database");
            }
            admindb = admindbConnection.getConnection();
            db = null;
            try {
                admindb.getConnection().setAutoCommit(true);
                serverLicense = null;
                if (this.licensefile != null && this.licensefile.exists()) {
                    serverLicense = new License(this.licensefile);
                }
                if ((databaseLicense = this.licensetext != null && this.licensetext.isEmpty() == false ? new License(this.licensetext) : serverLicense) == null) {
                    throw new BuildException("A valid license must be defined");
                }
                for (i = 1; i < this.connections.size(); ++i) {
                    block76: {
                        sapphireConnection = (ConnectionTask)this.connections.get(i);
                        this.log("Checking license...");
                        if (serverLicense != null) {
                            admindb.createResultSet("SELECT count(*) \"count\" FROM databaselist");
                            if (admindb.getNext()) {
                                licenseCount = serverLicense.getDatabaseCount();
                                if (admindb.getInt("count") >= licenseCount + (admindb.checkPreparedExists("SELECT databaselistid FROM databaselist WHERE databaselistid = ?", new Object[]{sapphireConnection.getDatabaseid()}) != false ? 1 : 0)) {
                                    throw new SapphireException("Exceeded database count. License only allows " + serverLicense.getDatabaseCount() + " databases.");
                                }
                            }
                        }
                        if (!(serverLicense != null && serverLicense.equals(databaseLicense) || (isValidLicenseBasedOnDatabaseCount = ConsoleController.isValidLicenseBasedOnDatabaseCount(sapphireConnection.getDatabaseid(), admindb, serverLicense, databaseLicense, false)))) {
                            throw new SapphireException("Exceeded database count on the same license only allows " + databaseLicense.getDatabaseCount() + " databases.");
                        }
                        this.log("Defining connection cache...");
                        db = sapphireConnection.getConnection();
                        db.getConnection().setAutoCommit(true);
                        if (db.isOracle()) {
                            this.log("Checking privileges...");
                            Installer.checkInstallPrivs(db.getConnection());
                        } else {
                            this.log("Checking RCS on...");
                            Installer.checkRCSOn(db.getConnection());
                        }
                        this.log("Running installer scripts...");
                        target = this.installtarget;
                        if (this.upgrademode == null || this.upgrademode.length() == 0) {
                            this.upgrademode = "full";
                        }
                        try {
                            db.createResultSet("SELECT propertyvalue FROM sysconfig WHERE propertyid = 'build'");
                            if (!db.getNext()) break block76;
                            compare = Installer.compareBuild(db.getString("propertyvalue"));
                            if (compare == 0) {
                                if (this.upgrademode.equals("full")) {
                                    target = this.upgradetarget;
                                } else {
                                    target = "";
                                    this.log(this.upgrademode.equals("statusonly") != false ? "Partial upgrade complete." : "Database build matches current build - no action required.");
                                }
                                break block76;
                            }
                            if (compare < 0) {
                                target = this.upgradetarget;
                                break block76;
                            }
                            throw new BuildException("Sapphire build/version is older than your database - check your software versions.");
                        }
                        catch (SapphireException compare) {
                            // empty catch block
                        }
                    }
                    if (target.equals(this.upgradetarget)) {
                        opalConfig = this.getProject().getProperty("sapphire.install.opal.configuration");
                        if (opalConfig != null && opalConfig.equals("new")) {
                            this.getProject().setUserProperty("sapphire.install.opal.configuration", "update");
                            this.getProject().setUserProperty("sapphire.install.opal.data.category", "update");
                            this.getProject().setUserProperty("sapphire.install.opal.data.profile", "update");
                            this.getProject().setUserProperty("sapphire.install.opal.data.property", "update");
                            this.getProject().setUserProperty("sapphire.install.opal.data.query", "update");
                            this.getProject().setUserProperty("sapphire.install.opal.data.ref", "update");
                            this.getProject().setUserProperty("sapphire.install.opal.data.role", "update");
                            this.getProject().setUserProperty("sapphire.install.opal.data.storage", "update");
                            this.getProject().setUserProperty("sapphire.install.opal.data.webpage", "update");
                            this.getProject().setUserProperty("sapphire.install.opal.data.workflow", "update");
                            this.getProject().setUserProperty("sapphire.install.opal.installexampledata", "false");
                        }
                        try {
                            db.createResultSet("SELECT propertyvalue FROM sysconfig WHERE propertyid = 'blockupgrades'");
                            if (db.getNext() && (blockUpgrades = db.getString("propertyvalue")) != null && (blockUpgrades.equals("Y") || blockUpgrades.equals("true"))) {
                                throw new BuildException("This database has been blocked for upgrades.");
                            }
                        }
                        catch (SapphireException blockUpgrades) {
                            // empty catch block
                        }
                        diagnostics = DiagnosticsUtils.getUpgradeDiagnostics(db, new ConnectionInfo(this.server.getUsername(), this.server.getPassword()), "General");
                        for (j = 0; j < diagnostics.size(); ++j) {
                            diagnostic = (BaseDiagnostic)diagnostics.get(j);
                            this.log("Running diagnostic " + diagnostic.getDescription());
                            this.log(diagnostic.runRepair(new PropertyList()));
                        }
                    }
                    if (target.length() > 0) {
                        this.getProject().executeTarget(target);
                    }
                    this.log("Updating admin database...");
                    admindb.getConnection().setAutoCommit(false);
                    labvantageinternalkey = null;
                    admindb.createResultSet("SELECT propertyid, propertyvalue FROM adminconfig WHERE propertyid = 'labvantageinternalkey'");
                    if (admindb.getNext()) {
                        labvantageinternalkey = admindb.getString("propertyvalue");
                    }
                    if (labvantageinternalkey == null) {
                        labvantageinternalkey = EncryptDecrypt.generateRandomAESKey();
                        admindb.executePreparedUpdate("INSERT INTO adminconfig ( propertyid, propertyvalue ) values( ?, ? )", new Object[]{"labvantageinternalkey", labvantageinternalkey});
                        EncryptDecrypt.setLVInternalKey("admindb", labvantageinternalkey);
                        admindb.createResultSet("SELECT databaselistid, dbconnectionkey FROM databaselist");
                        while (admindb.getNext()) {
                            databaseid = admindb.getString("databaselistid");
                            dbconnectionkey = admindb.getString("dbconnectionkey");
                            newdbconnectionkey = EncryptDecrypt.encrypt(EncryptDecrypt.decrypt(dbconnectionkey), "admindb");
                            admindb.executePreparedUpdate("UPDATE databaselist SET dbconnectionkey= ? WHERE databaselistid=?", new Object[]{newdbconnectionkey, databaseid});
                        }
                    }
                    EncryptDecrypt.setLVInternalKey("admindb", labvantageinternalkey);
                    dbconnectionkey = EncryptDecrypt.encrypt(sapphireConnection.getUsername() + "|" + sapphireConnection.getPassword(), "admindb");
                    updateSql = "UPDATE databaselist SET \tdbms = '" + (sapphireConnection.getDbms().equals("ORA") != false ? "O87" : "MSS") + "'" + (sapphireConnection.getHoststring().length() > 0 ? ", dbserver = '" + sapphireConnection.getHoststring() + "'" : "") + (sapphireConnection.getSqldatabase() != null && sapphireConnection.getSqldatabase().length() > 0 ? ", sqldatabase = '" + sapphireConnection.getSqldatabase() + "'" : "") + (sapphireConnection.getUsername() != null && sapphireConnection.getUsername().length() > 0 ? ", dbconnectionkey = '" + dbconnectionkey + "'" : "") + ", statusflag = 'A', jndiname = '" + (sapphireConnection.getJndiname() != null && sapphireConnection.getJndiname().length() > 0 ? sapphireConnection.getJndiname() : "") + "' WHERE databaselistid = '" + sapphireConnection.getDatabaseid() + "'";
                    if (admindb.executeUpdate(updateSql) == 0) {
                        insertSql = "INSERT INTO databaselist ( databaselistid, dbms, dbserver, sqldatabase, dbconnectionkey, statusflag, jndiname ) VALUES ( '" + sapphireConnection.getDatabaseid() + "', '" + (sapphireConnection.getDbms().equals("ORA") != false ? "O87" : "MSS") + "', '" + sapphireConnection.getHoststring() + "', '" + sapphireConnection.getSqldatabase() + "', '" + dbconnectionkey + "', 'A', '" + (sapphireConnection.getJndiname() != null && sapphireConnection.getJndiname().length() > 0 ? sapphireConnection.getJndiname() : "") + "' )";
                        admindb.executeUpdate(insertSql);
                    }
                    if (!this.resetdblicense) {
                        if (this.licensetext != null && !this.licensetext.isEmpty()) {
                            updateLicenseSql = "UPDATE databaselist SET  licensekey = ? where databaselistid = ? ";
                            admindb.executePreparedUpdate(updateLicenseSql, new Object[]{this.licensetext, sapphireConnection.getDatabaseid()});
                        }
                    } else {
                        updateLicenseSql = "UPDATE databaselist SET  licensekey = null where databaselistid = ? ";
                        admindb.executePreparedUpdate(updateLicenseSql, new Object[]{sapphireConnection.getDatabaseid()});
                    }
                    this.log("Updating configuration tables...");
                    db.createResultSet("SELECT propertyvalue FROM sysconfig WHERE propertyid = 'lastbuild'");
                    if (db.getNext()) {
                        if (!db.getString("propertyvalue").equals(Build.getBuild())) {
                            db.executeUpdate("UPDATE sysconfig SET propertyvalue = ( SELECT propertyvalue FROM sysconfig WHERE propertyid = 'build' ) WHERE propertyid = 'lastbuild'");
                        }
                    } else {
                        db.executeSQL("INSERT INTO sysconfig ( propertyid, propertyvalue ) SELECT 'lastbuild', propertyvalue FROM sysconfig WHERE propertyid = 'build'");
                    }
                    if (db.executeUpdate("UPDATE sysconfig SET propertyvalue = '" + Build.getBuild() + "' WHERE propertyid = 'build'") == 0) {
                        db.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'build', '" + Build.getBuild() + "' )");
                        db.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'todolistpoll', '1' )");
                        db.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'taskpoll', '60' )");
                        db.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'schedulepoll', '3600' )");
                        db.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'timeoutpoll', '60' )");
                        db.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'connectiontimeout', '3600' )");
                        db.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'rsettimeout', '60' )");
                        db.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'productedition', 'R5' )");
                    }
                    if ((opalConfig = this.getProject().getProperty("sapphire.install.opal.configuration")) == null) {
                        opalConfig = "none";
                    }
                    if (db.executeUpdate("UPDATE sysconfig SET propertyvalue = '" + opalConfig + "' WHERE propertyid = 'opalconfig'") == 0) {
                        db.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'opalconfig', '" + opalConfig + "' )");
                    }
                    if (target.equals(this.upgradetarget)) {
                        db.createResultSet("SELECT propertyvalue FROM sysconfig WHERE propertyid = 'productedition'");
                        if (!db.getNext()) {
                            productedition = opalConfig.equals("none") != false ? "V3" : "R4";
                            db.executeUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'productedition', '" + productedition + "' )");
                        }
                    }
                    db.executeSQL("DELETE FROM sysconfig where propertyid = 'patch'");
                    if (Integer.parseInt(Build.getPatch()) > 0) {
                        db.executeSQL("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'patch', '" + Build.getBuild() + "_" + Build.getPatch() + "' )");
                    }
                    db.executeSQL("DELETE FROM sysconfig where propertyid = 'synchformbuild'");
                    diagnostics = DiagnosticsUtils.getDatabaseDiagnostics(db, new ConnectionInfo(this.server.getUsername(), this.server.getPassword()), "General");
                    for (j = 0; j < diagnostics.size(); ++j) {
                        diagnostic = (BaseDiagnostic)diagnostics.get(j);
                        if (!diagnostic.canAutoRepair()) continue;
                        this.log("Running diagnostic " + diagnostic.getId());
                        this.log(diagnostic.runRepair(new PropertyList()));
                    }
                    if (target.equals(this.upgradetarget)) {
                        licFilePath = "";
                        labvantageHome = "";
                        try {
                            licFilePath = this.licensefile.getCanonicalPath();
                            labvantageHome = licFilePath.substring(0, licFilePath.lastIndexOf(File.separator));
                        }
                        catch (IOException e) {
                            throw new SapphireException(e.getMessage(), e);
                        }
                        postUpgradeDiagnostics = DiagnosticsUtils.getPostUpgradeDiagnostics(db, new ConnectionInfo(this.server.getUsername(), this.server.getPassword()), "General", labvantageHome);
                        for (j = 0; j < postUpgradeDiagnostics.size(); ++j) {
                            diagnostic = (BaseDiagnostic)postUpgradeDiagnostics.get(j);
                            this.log("Running post upgrade diagnostic " + diagnostic.getId() + ": " + diagnostic.getDescription());
                            this.log(diagnostic.runRepair(new PropertyList()));
                        }
                    }
                    if (target.equals(this.installtarget) && this.getProject().getProperty("sapphire.install.opal.createadminuser") != null && this.getProject().getProperty("sapphire.install.opal.createadminuser").equals("true")) {
                        adminuserid = this.getProject().getProperty("sapphire.install.opal.adminuserid");
                        adminpassword = this.getProject().getProperty("sapphire.install.opal.adminuserpw");
                        if (adminuserid == null || adminuserid.isEmpty()) {
                            adminuserid = "admin";
                        }
                        if (adminpassword == null || adminpassword.isEmpty()) {
                            adminpassword = adminuserid;
                        }
                        password = EncryptDecrypt.encodePassword(adminpassword);
                        db.executePreparedUpdate("INSERT INTO sysuser ( sysuserid, sysuserdesc, nameduserflag, password, changepasswordflag, casesensitivepasswordflag, templateflag, activeflag ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )", new Object[]{adminuserid, "Install created admin user", databaseLicense.getNamedUsers() > 0 ? "S" : "C", password, "Y", "Y", "N", "Y"});
                        db.executeUpdate("INSERT INTO sysuserrole ( roleid, sysuserid ) SELECT roleid, '" + adminuserid + "' FROM role");
                        db.executeUpdate("INSERT INTO modulesysuser ( moduleid, sysuserid ) SELECT moduleid, '" + adminuserid + "' FROM module WHERE licensedflag = 'N'");
                        modules = databaseLicense.getModuleProperties();
                        e = modules.propertyNames();
                        while (e.hasMoreElements()) {
                            module = (String)e.nextElement();
                            if (modules.getProperty(module, "").length() <= 0 || !modules.getProperty(module).equals("S") && !modules.getProperty(module).equals("U") && Integer.parseInt(modules.getProperty(module)) <= 0) continue;
                            db.executeUpdate("INSERT INTO modulesysuser ( moduleid, sysuserid ) VALUES ( '" + module + "', '" + adminuserid + "' )");
                        }
                    }
                    db.getConnection().setAutoCommit(false);
                    db.getConnection().commit();
                    db.reset();
                    admindb.getConnection().setAutoCommit(false);
                    admindb.getConnection().commit();
                }
            }
            catch (SapphireException se) {
                throw new BuildException("SapphireException: " + se.getMessage());
            }
            catch (SQLException sqle) {
                throw new BuildException("SQLException: " + sqle.getMessage());
            }
            finally {
                if (db != null) {
                    db.reset();
                }
                admindb.reset();
            }
        }
        this.log("Installer complete");
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connections.add(connection);
    }

    public void addConfiguredServer(ServerTask server) {
        this.server = server;
    }
}

