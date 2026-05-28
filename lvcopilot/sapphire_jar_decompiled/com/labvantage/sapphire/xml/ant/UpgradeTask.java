/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.xml.ExportPackageHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import java.io.File;
import java.sql.CallableStatement;
import java.sql.SQLException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import sapphire.SapphireException;

public class UpgradeTask
extends Task {
    ConnectionTask connection;
    File upddata;

    public void setUpddata(File upddata) {
        this.upddata = upddata;
    }

    public void execute() throws BuildException {
        if (this.connection == null) {
            throw new BuildException("Connection task not defined");
        }
        this.log("Upgrading database...");
        DBUtil dbu = this.connection.getConnection(true);
        try {
            this.log("...deleting old upgrade data...");
            dbu.executeSQL("DELETE FROM updrefcolumn");
            dbu.createResultSet("SELECT tableid FROM systable WHERE tableid = 'updcolumnproperty'");
            if (dbu.getNext()) {
                dbu.executeSQL("DELETE FROM updcolumnproperty");
            }
            dbu.createResultSet("SELECT tableid FROM systable WHERE tableid = 'updviewdepends'");
            if (dbu.getNext()) {
                dbu.executeSQL("DELETE FROM updviewdepends");
            }
            dbu.executeSQL("DELETE FROM updcolumn");
            dbu.executeSQL("DELETE FROM updref");
            dbu.executeSQL("DELETE FROM updtable");
            dbu.executeSQL("DELETE FROM updlog");
            dbu.createResultSet("SELECT tableid FROM systable WHERE tableid = 'updauditindex'");
            if (dbu.getNext()) {
                dbu.executeSQL("DELETE FROM updauditindex");
            }
            this.log("...checking current data model...");
            this.executeCallableStatement(dbu, "{call lv_ddt" + (dbu.getDbms().equals("ORA") ? "." : "_") + "syncddt( ? ) }", Build.getBuild());
            dbu.createPreparedResultSet("SELECT logtext FROM UPDLOG WHERE logtext = ?", new Object[]{Build.getBuild()});
            if (!dbu.getNext()) {
                throw new SapphireException("Current data model is invalid - check updlog table for errors.");
            }
            dbu.closeResultSet();
            this.log("...importing new upgrade data...");
            ExportPackageHandler handler = new ExportPackageHandler();
            handler.setXMLFile(new File(this.upddata.getAbsolutePath()));
            handler.setLogFile(new File(this.upddata.getAbsolutePath() + "_log"));
            handler.setDBUtil(dbu);
            SaxUtil.parseFile(handler);
            this.log("...upgrading data model...");
            this.executeCallableStatement(dbu, "{call lv_upd" + (dbu.getDbms().equals("ORA") ? "." : "_") + "syncupd( ? ) }", Build.getBuild());
            dbu.createPreparedResultSet("SELECT logtext FROM UPDLOG WHERE logtext = ?", new Object[]{Build.getBuild()});
            if (dbu.getNext()) {
                if (dbu.getDbms().equals("ORA")) {
                    this.executeCallableStatement(dbu, "{call lv_util.recomp( ? ) }", "I");
                }
            } else {
                throw new SapphireException("Upgrade of data model failed - check updlog table for errors.");
            }
            dbu.closeResultSet();
        }
        catch (SapphireException se) {
            throw new BuildException("SapphireException: " + se.getMessage());
        }
        finally {
            dbu.reset();
        }
        this.log("Upgrade complete");
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connection = connection;
    }

    private void executeCallableStatement(DBUtil dbu, String stmt, String param1) throws SapphireException {
        try {
            CallableStatement cs = dbu.getConnection().prepareCall(stmt);
            cs.setString(1, param1);
            cs.executeUpdate();
            cs.close();
            cs = null;
        }
        catch (SQLException se) {
            throw new SapphireException(se.getMessage());
        }
    }
}

