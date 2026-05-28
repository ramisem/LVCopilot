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
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import java.sql.CallableStatement;
import java.sql.SQLException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import sapphire.SapphireException;

public class SyncUpdTablesTask
extends Task {
    ConnectionTask connection;

    public void execute() throws BuildException {
        if (this.connection == null) {
            throw new BuildException("Connection task not defined");
        }
        this.log("Synchronizing UPD tables...");
        DBUtil dbu = this.connection.getConnection();
        try {
            this.log("SyncDDT...");
            CallableStatement cs = dbu.prepareCall("{call lv_ddt" + (dbu.getDbms().equals("ORA") ? "." : "_") + "syncddt( ? ) }");
            cs.setString(1, Build.getBuild());
            cs.executeUpdate();
            dbu.reset(cs);
            this.log("Reset UPD tables...");
            dbu.executeSQL("DELETE FROM updrefcolumn");
            dbu.createResultSet("SELECT tableid FROM systable WHERE tableid = 'updcolumnproperty'");
            if (dbu.getNext()) {
                dbu.executeSQL("DELETE FROM updcolumnproperty");
            }
            dbu.executeSQL("DELETE FROM updcolumn");
            dbu.executeSQL("DELETE FROM updref");
            dbu.executeSQL("DELETE FROM updtable");
            this.log("Populate UPD tables...");
            dbu.executeSQL("INSERT INTO updtable ( tableid, tabletype, storageid, tabledesc, nextsize ) SELECT tableid, tabletype, storageid, tabledesc, nextsize FROM systable");
            dbu.executeSQL("INSERT INTO updcolumn ( tableid, columnid, datatype, columnlength, columnsequence, pkflag, columntype, columndesc, nnflag ) SELECT tableid, columnid, datatype, columnlength, columnsequence, pkflag, columntype, columndesc, nnflag FROM syscolumn");
            dbu.executeSQL("INSERT INTO updref ( refid, tableid, reftypeflag, storageid, refindexid ) SELECT refid, tableid, reftypeflag, storageid, refindexid FROM sysref");
            dbu.executeSQL("INSERT INTO updrefcolumn ( refid, columnid, columnsequence ) SELECT refid, columnid, columnsequence FROM sysrefcolumn");
        }
        catch (SapphireException se) {
            throw new BuildException("SapphireException: " + se.getMessage());
        }
        catch (SQLException sqle) {
            throw new BuildException("SQLException: " + sqle.getMessage());
        }
        finally {
            dbu.reset();
        }
        this.log("Synchronization complete");
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connection = connection;
    }
}

