/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.DirectoryScanner
 *  org.apache.tools.ant.Task
 *  org.apache.tools.ant.types.FileSet
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.xml.SqlProcessor;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import com.labvantage.sapphire.xml.ant.SQLStmtTask;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import sapphire.SapphireException;

public class ExecuteSQLTask
extends Task {
    boolean failonerror = true;
    ConnectionTask connection;
    ArrayList sql = new ArrayList();

    public void setFailonerror(boolean failonerror) {
        this.failonerror = failonerror;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void execute() throws BuildException {
        String indexspacename;
        if (this.connection == null) {
            throw new BuildException("Connection task not defined");
        }
        if (this.sql.size() == 0) {
            throw new BuildException("No SQL has been defined for execution");
        }
        String tablespacename = this.getProject().getProperty("sapphire.db.tablespacename");
        if (tablespacename != null && tablespacename.length() > 0) {
            this.getProject().setProperty("&&1", tablespacename);
        }
        if ((indexspacename = this.getProject().getProperty("sapphire.db.indexspacename")) != null && indexspacename.length() > 0) {
            this.getProject().setProperty("&&2", indexspacename);
        }
        DBUtil dbu = this.connection.getConnection(true);
        try {
            for (int i = 0; i < this.sql.size(); ++i) {
                if (this.sql.get(i) instanceof FileSet) {
                    FileSet fileset = (FileSet)this.sql.get(i);
                    DirectoryScanner ds = fileset.getDirectoryScanner(this.getProject());
                    ds.scan();
                    String[] includedFiles = ds.getIncludedFiles();
                    for (int j = 0; j < includedFiles.length; ++j) {
                        File file = new File(ds.getBasedir(), includedFiles[j]);
                        this.log("Executing " + file.getAbsolutePath() + "...");
                        try (BufferedReader br = new BufferedReader(new FileReader(file));){
                            SqlProcessor.processSQL(dbu, new HashMap(this.getProject().getProperties()), br, null);
                            continue;
                        }
                    }
                    continue;
                }
                if (!(this.sql.get(i) instanceof SQLStmtTask)) continue;
                String sqlstmt = ((SQLStmtTask)((Object)this.sql.get(i))).getSQL();
                this.log("Executing statement:" + sqlstmt);
                try (BufferedReader br = new BufferedReader(new StringReader(sqlstmt));){
                    SqlProcessor.processSQL(dbu, new HashMap(this.getProject().getProperties()), br, null);
                    continue;
                }
            }
        }
        catch (SapphireException se) {
            throw new BuildException("SapphireException: " + se.getMessage());
        }
        catch (IOException ioe) {
            throw new BuildException("IOException: " + ioe.getMessage());
        }
        finally {
            dbu.reset();
        }
        this.log("Execute SQL complete");
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connection = connection;
    }

    public void addFileset(FileSet fileset) {
        this.sql.add(fileset);
    }

    public void addConfiguredSQLStmt(SQLStmtTask sqlstmt) {
        this.sql.add(sqlstmt);
    }
}

