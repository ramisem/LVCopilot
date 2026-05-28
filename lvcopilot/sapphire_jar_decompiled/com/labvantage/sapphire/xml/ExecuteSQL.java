/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.SqlProcessor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DBAccess;

public class ExecuteSQL
implements Logger {
    DBAccess database;
    ArrayList sqlFiles = new ArrayList();
    HashMap sqlProps;
    Logger logger = this;

    public ExecuteSQL() {
    }

    public ExecuteSQL(DBAccess database, File sqlFile, HashMap sqlProps) {
        this.database = database;
        this.sqlFiles.add(sqlFile);
        this.sqlProps = sqlProps;
    }

    public ExecuteSQL(DBUtil database, ArrayList sqlFiles, HashMap sqlProps) {
        this.database = database;
        this.sqlFiles.addAll(sqlFiles);
        this.sqlProps = sqlProps;
    }

    public void setDatabase(DBAccess database) {
        this.database = database;
    }

    public void setSqlProps(HashMap sqlProps) {
        this.sqlProps = sqlProps;
    }

    public void setSqlFiles(ArrayList sqlFiles) {
        this.sqlFiles = sqlFiles;
    }

    public void setSqlLog(Logger logger) {
        this.logger = logger;
    }

    public void executeSQL() throws SapphireException {
        this.executeSQL(false);
    }

    public void executeSQL(boolean verbose) throws SapphireException {
        if (this.database == null) {
            throw new SapphireException("Database not defined");
        }
        if (this.sqlFiles == null) {
            throw new SapphireException("SQL file(s) not defined");
        }
        try {
            for (int i = 0; i < this.sqlFiles.size(); ++i) {
                BufferedReader br;
                if (this.sqlFiles.get(i) instanceof File) {
                    File installFile = (File)this.sqlFiles.get(i);
                    br = new BufferedReader(new FileReader(installFile));
                    SqlProcessor.processSQL(this.database, new HashMap(this.sqlProps), br, null);
                    br.close();
                    continue;
                }
                if (!(this.sqlFiles.get(i) instanceof String)) continue;
                String sqlstmt = (String)this.sqlFiles.get(i);
                this.log("Executing statement:" + sqlstmt);
                br = new BufferedReader(new StringReader(sqlstmt));
                SqlProcessor.processSQL(this.database, new HashMap(this.sqlProps), br, null);
                br.close();
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to execute sql. Reason: " + e.getMessage(), e);
        }
        finally {
            ((DBUtil)this.database).reset();
        }
    }

    public void addSQL(File executeFile) {
        this.sqlFiles.add(executeFile);
    }

    @Override
    public void log(String message) {
    }
}

