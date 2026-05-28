/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.platform;

import com.labvantage.sapphire.License;
import java.io.Serializable;
import sapphire.SapphireException;

public class SapphireDatabase
implements Serializable {
    public static final String ORA = "ORA";
    public static final String MSS = "MSS";
    public static final String DBMS = "dbms";
    public static final String DATABASEID = "databaseid";
    public static final String STATUS_ACTIVE = "A";
    public static final String STATUS_DISABLED = "D";
    public static final String STATUS_REQUIRESUPGRADE = "RU";
    public static final String STATUS_REQUIRESINSTALL = "RI";
    public static final String STATUS_CONNECTIONFAILED = "CF";
    public static final String STATUS_INVALID = "X";
    protected String jndiname;
    protected String databaseId;
    protected String dbms;
    protected String dbServer;
    protected String sqlDatabase;
    protected String dbConnectionKey;
    protected String port;
    protected String sid;
    protected String build;
    protected String patch;
    protected String statusflag;
    protected String opalConfig;
    protected License license;

    public String getJndiname() {
        return this.jndiname == null || this.jndiname.length() == 0 ? this.databaseId : this.jndiname;
    }

    public void setJndiname(String jndiname) {
        this.jndiname = jndiname;
    }

    public String getDatabaseId() {
        return this.databaseId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    public String getDbms() {
        return this.dbms;
    }

    public void setDbms(String dbms) {
        this.dbms = dbms.equals("O87") ? ORA : dbms;
    }

    public String getDbServer() {
        return this.dbServer;
    }

    public void setDbServer(String dbServer) {
        this.dbServer = dbServer;
    }

    public String getSqlDatabase() {
        return this.sqlDatabase;
    }

    public void setSqlDatabase(String sqlDatabase) {
        this.sqlDatabase = sqlDatabase;
    }

    public String getDbConnectionKey() {
        return this.dbConnectionKey;
    }

    public void setDbConnectionKey(String dbConnectionKey) {
        this.dbConnectionKey = dbConnectionKey;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSid() {
        return this.sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getBuild() {
        return this.build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getStatusflag() {
        return this.statusflag;
    }

    public void setStatusflag(String statusflag) {
        this.statusflag = statusflag;
    }

    public String getStatus() {
        return this.statusflag.equals(STATUS_ACTIVE) ? "Active" : (this.statusflag.equals(STATUS_REQUIRESUPGRADE) ? "Requires Upgrade" : (this.statusflag.equals(STATUS_REQUIRESINSTALL) ? "Requires Install" : (this.statusflag.equals(STATUS_DISABLED) ? "Disabled" : (this.statusflag.equals(STATUS_INVALID) ? "Invalid" : (this.statusflag.equals(STATUS_CONNECTIONFAILED) ? "Connection Failed" : "Unknown Status")))));
    }

    public String getPatch() {
        return this.patch;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }

    public String getOpalConfig() {
        return this.opalConfig;
    }

    public void setOpalConfig(String opalConfig) {
        this.opalConfig = opalConfig;
    }

    public boolean isOracle() {
        return this.dbms.equals(ORA);
    }

    public boolean isSqlServer() {
        return this.dbms.equals(MSS);
    }

    public String toString() {
        return this.databaseId;
    }

    public void setLicense(String licenseText) {
        try {
            if (licenseText != null && !licenseText.isEmpty()) {
                this.license = new License(licenseText);
            }
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
    }

    public License getLicense() {
        return this.license;
    }
}

