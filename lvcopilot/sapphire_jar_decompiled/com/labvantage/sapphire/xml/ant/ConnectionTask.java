/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class ConnectionTask
extends Task {
    private String jndiname;
    private String databaseid;
    private String file;
    private String dbms;
    private String instancename;
    private String server;
    private String hoststring;
    private String port;
    private String sid;
    private String sqldatabase;
    private String username;
    private String password;
    private String tablespacename;
    private String indexspacename;
    private String encrypt = "false";
    private String verbose = "false";

    public void setFile(String file) {
        this.file = file;
    }

    public String getJndiname() {
        return this.jndiname;
    }

    public void setJndiname(String jndiname) {
        this.jndiname = jndiname;
    }

    public String getDatabaseid() {
        return this.databaseid;
    }

    public void setDatabaseid(String databaseid) {
        this.databaseid = databaseid;
    }

    public String getDbms() {
        return this.dbms;
    }

    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    public String getInstancename() {
        return this.instancename;
    }

    public void setInstancename(String instancename) {
        this.instancename = instancename;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getServer() {
        return this.server;
    }

    public String getHoststring() {
        return this.hoststring != null && !this.hoststring.isEmpty() ? this.hoststring : this.server;
    }

    public void setHoststring(String hoststring) {
        this.hoststring = hoststring;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return this.port;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getSid() {
        return this.sid;
    }

    public void setSqldatabase(String sqldatabase) {
        this.sqldatabase = sqldatabase;
    }

    public String getSqldatabase() {
        return this.sqldatabase;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public String getTablespacename() {
        return this.tablespacename;
    }

    public void setTablespacename(String tablespacename) {
        this.tablespacename = tablespacename;
    }

    public String getIndexspacename() {
        return this.indexspacename;
    }

    public void setIndexspacename(String indexspacename) {
        this.indexspacename = indexspacename;
    }

    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt;
    }

    public void setVerbose(String verbose) {
        this.verbose = verbose;
    }

    public DBUtil getConnection() {
        return this.getConnection(false);
    }

    public DBUtil getConnection(boolean autocommit) {
        DBUtil dbu;
        block28: {
            dbu = new DBUtil();
            try {
                if (this.file != null && !this.file.isEmpty()) {
                    File propsFile = new File(this.file);
                    if (propsFile.exists()) {
                        Properties props = new Properties();
                        try {
                            props.load(Files.newInputStream(propsFile.toPath(), new OpenOption[0]));
                            this.databaseid = props.getProperty("databaseid");
                            this.instancename = props.getProperty("instancename");
                            this.server = props.getProperty("server");
                            this.hoststring = props.getProperty("hoststring");
                            this.port = props.getProperty("port");
                            this.sid = props.getProperty("sid");
                            this.sqldatabase = props.getProperty("sqldatabase");
                            this.username = props.getProperty("username");
                            this.password = props.getProperty("password");
                            this.tablespacename = props.getProperty("tablespacename");
                            this.indexspacename = props.getProperty("indexspacename");
                        }
                        catch (IOException e) {
                            throw new SapphireException("Failed to load properies file '" + this.file + "'");
                        }
                    } else {
                        throw new SapphireException("Properties file '" + this.file + "' does not exist");
                    }
                }
                if (this.jndiname != null && !this.jndiname.isEmpty()) {
                    String jndidatasourceprefix = this.getProject().getUserProperty("sapphire.admindb.jndidatasourceprefix");
                    ServiceLocator sl = this.getProject().getUserProperties().containsKey("sapphire.admindb.jndidatasourceprefix") ? ServiceLocator.getInstance(jndidatasourceprefix, "", "") : ServiceLocator.getInstance();
                    dbu.setConnection(this.dbms, sl.getDataSource(this.jndiname).getConnection());
                    try {
                        String sysuserid = this.getProject().getUserProperty("sapphire.sysuserid");
                        String callstmt = "{call lv_app" + (dbu.isOracle() ? "." : "_") + "setappinfouser( ? ) }";
                        try (CallableStatement cs = dbu.prepareCall(callstmt);){
                            cs.setString(1, sysuserid);
                            cs.executeUpdate();
                        }
                    }
                    catch (Exception e) {
                        this.log("exception: " + e.getMessage());
                    }
                    dbu.getConnection().setAutoCommit(autocommit);
                    dbu.setReleaseConnection(true);
                    this.log("Connected to database " + this.jndiname + " with autocommit " + (autocommit ? "on" : "off"));
                    break block28;
                }
                if (this.sqldatabase != null && !this.sqldatabase.isEmpty() && !this.sqldatabase.contains("${")) {
                    this.dbms = "MSS";
                    this.server = StringUtil.replaceAll(this.server, "{backslash}", "\\");
                    this.hoststring = StringUtil.replaceAll(this.hoststring, "{backslash}", "\\");
                    if (this.verbose.equalsIgnoreCase("true")) {
                        ConnectionTask.initJDBCLogging("com.microsoft.sqlserver.jdbc");
                    }
                    dbu.setDatabase(this.instancename, this.server, this.port, this.sqldatabase, this.username, this.password, !this.encrypt.equalsIgnoreCase("false"), this.encrypt);
                    dbu.getConnection().setAutoCommit(autocommit);
                    this.log("Connected to database " + this.sqldatabase + " with user " + this.username + " on " + this.server + " with autocommit " + (autocommit ? "on" : "off"));
                } else {
                    this.dbms = "ORA";
                    if (this.verbose.equalsIgnoreCase("true")) {
                        ConnectionTask.initJDBCLogging("oracle.jdbc");
                    }
                    dbu.setDatabase(this.server, this.port, this.sid, this.username, this.password, this.encrypt.equalsIgnoreCase("true"));
                    dbu.getConnection().setAutoCommit(autocommit);
                    this.log("Connected to database " + this.username + " on " + this.server + " with autocommit " + (autocommit ? "on" : "off"));
                }
            }
            catch (SapphireException se) {
                throw new BuildException("Failed to connect to database. Exception: " + se.getMessage());
            }
            catch (SQLException sqle) {
                throw new BuildException("SQLException: " + sqle.getMessage());
            }
        }
        return dbu;
    }

    private static void initJDBCLogging(String jdbcLogger) {
        Logger logger = Logger.getLogger(jdbcLogger);
        logger.setLevel(Level.FINE);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);
    }

    public static void generateAntTask(PrintStream out, int level, String dbms) {
        String level0 = StringUtil.repeat("\t", level);
        out.println(level0 + "<connection jndiname=\"${sapphire.db.jndiname}\" dbms=\"${sapphire.db.dbms}\" server=\"${sapphire.db.servername}\" " + (dbms.equals("ORA") ? "port=\"${sapphire.db.port}\" sid=\"${sapphire.db.sid}\"" : "sqldatabase=\"${sapphire.db.sqldatabase}\"") + " username=\"${sapphire.db.username}\" password=\"${sapphire.db.password}\"/>");
    }
}

