/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 *  org.apache.tools.ant.types.PropertySet
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.admin.command.CommandLine;
import com.labvantage.sapphire.admin.command.SapphireCLI;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.PropertySet;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class ImageToNVarcharmax
extends Task
implements CommandLine {
    boolean failonerror = true;
    ConnectionTask connection;
    private String tableid;
    private String keycolumnid;
    private String fromcolumnid;
    private String tocolumnid;
    ArrayList propertSet = new ArrayList();
    private DBAccess dbu;
    boolean isCommandLine = false;

    @Override
    public String getCommandName() {
        return "imageToNVarcharmax";
    }

    @Override
    public String getCommandDescription() {
        return "Copy values from MSS image columns that store charactor data and renamed to [columnid]_img to nvarcharmax columns with corresponding name of [columnid] ";
    }

    @Override
    public String getCommandUsage() {
        return "imagetonvarcharmax -servername=[servername] -sqldatabase=[sid] -username=[username] -password=[password]";
    }

    @Override
    public void processCommand(HashMap commandParams, boolean verbose) throws SapphireException {
        this.isCommandLine = true;
        this.dbu = SapphireCLI.getDatabase(commandParams, verbose);
        this.execute();
    }

    @Override
    public void processCommand() throws SapphireException {
        throw new SapphireException("Not supported in this version!");
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    public void execute() throws BuildException {
        HashSet<String> excludeSet = new HashSet<String>();
        excludeSet.add("");
        if (this.connection == null && this.dbu == null) {
            throw new BuildException("Connection task not defined");
        }
        if (this.tableid == null || this.fromcolumnid == null || this.tocolumnid == null) {
            if (this.connection != null) {
                this.dbu = this.connection.getConnection(true);
            }
            try {
                ((DBUtil)this.dbu).setReleaseConnection(false);
                this.dbu.createResultSet("select distinct tableid from syscolumn where datatype in ('T', 'B') order by tableid");
                DataSet syscols = new DataSet();
                syscols.setResultSet(this.dbu.getResultSet());
                for (int t = 0; t < syscols.getRowCount(); ++t) {
                    String currentTable = syscols.getString(t, "tableid");
                    int rowcount = this.dbu.getCount("select count(*) c from " + currentTable);
                    if (rowcount > 0) {
                        if (this.isCommandLine || rowcount < 500000) {
                            String logMessage = "Table " + currentTable + " Rows:" + rowcount;
                            if (this.isCommandLine) {
                                System.out.println(logMessage);
                            } else {
                                this.log(logMessage);
                            }
                            this.dbu.createResultSet("select top 1 * from " + currentTable);
                            ResultSet rs = this.dbu.getResultSet();
                            ResultSetMetaData rsmd = rs.getMetaData();
                            int cols = rsmd.getColumnCount();
                            StringBuffer fromcolumnid = new StringBuffer("");
                            StringBuffer tocolumnid = new StringBuffer("");
                            for (int fc = 1; fc <= cols; ++fc) {
                                if (rsmd.getColumnType(fc) != -1) continue;
                                String currenttocolumnid = rsmd.getColumnName(fc);
                                String currentfromcolumnid = "";
                                for (int tc = 1; tc <= cols; ++tc) {
                                    if (!(currenttocolumnid + "_im").equals(rsmd.getColumnName(tc))) continue;
                                    currentfromcolumnid = currenttocolumnid + "_im";
                                    break;
                                }
                                if (!(currenttocolumnid + "_im").equals(currentfromcolumnid)) continue;
                                fromcolumnid.append("," + currentfromcolumnid);
                                tocolumnid.append("," + currenttocolumnid);
                            }
                            if (fromcolumnid.length() > 1) {
                                this.dbu.createPreparedResultSet("Select columnid from syscolumn where tableid=? and pkflag='Y'", new Object[]{currentTable});
                                DataSet ds = new DataSet();
                                ds.setResultSet(this.dbu.getResultSet());
                                String keycols = ds.getColumnValues("columnid", ",");
                                this.transferColumnValue(currentTable, keycols, fromcolumnid.substring(1), tocolumnid.substring(1));
                                continue;
                            }
                            logMessage = "No Clob columns";
                            if (this.isCommandLine) {
                                System.out.println(logMessage);
                                continue;
                            }
                            this.log(logMessage);
                            continue;
                        }
                        this.log("Table " + currentTable + " Has too many rows to transfer value during ungrade, please do it later. Rows: " + rowcount);
                        continue;
                    }
                    this.log("Table " + currentTable + " has no row to transfer.");
                }
            }
            catch (Exception e) {
                throw new BuildException("Transfer column value failed. " + e.getMessage());
            }
            finally {
                ((DBUtil)this.dbu).reset();
            }
        } else {
            this.transferColumnValue(this.tableid, this.keycolumnid, this.fromcolumnid, this.tocolumnid);
        }
    }

    private void transferColumnValue(String tableid, String keycolumnid, String fromcolumnid, String tocolumnid) {
        long starttime = System.currentTimeMillis();
        try {
            String sql = "SELECT " + keycolumnid + ", " + fromcolumnid + ("action".equals(tableid) ? ", actionlanguage" : "") + ", " + tocolumnid + " FROM " + tableid;
            Connection conn = this.dbu.getConnection();
            Statement stmt = conn.createStatement(1005, 1008);
            ResultSet rs = stmt.executeQuery(sql);
            String[] keycols = StringUtil.split(keycolumnid, ",");
            String[] fcols = StringUtil.split(fromcolumnid, ",");
            String[] tcols = StringUtil.split(tocolumnid, ",");
            if (fcols.length != tcols.length) {
                throw new BuildException("Fromcolumnid and tocolumnid do not match.");
            }
            int row = 0;
            while (rs.next()) {
                for (int c = 0; c < fcols.length; ++c) {
                    String value = "";
                    StringBuffer sb = null;
                    InputStream is = rs.getBinaryStream(fcols[c]);
                    if (is != null) {
                        sb = new StringBuffer("");
                        BufferedReader in = null;
                        in = "todolist".equals(tableid) && "propertyclob".equals(tocolumnid) || "action".equals(tableid) && "actionscript".equals(tocolumnid) && !"actionblock".equals(rs.getString("actionlanguage")) || "sdc".equals(tableid) && "defaultform".equals(tocolumnid) || "sdclink".equals(tableid) && "defaultgrid".equals(tocolumnid) ? new BufferedReader(new InputStreamReader(is, "UTF-16LE")) : new BufferedReader(new InputStreamReader(is));
                        String buffer = "";
                        while ((buffer = in.readLine()) != null) {
                            sb.append(buffer + System.getProperty("line.separator"));
                        }
                        is.close();
                        in.close();
                    }
                    String string = value = sb == null ? null : sb.toString();
                    if (value == null || value.length() == 0) {
                        rs.updateNull(tcols[c]);
                        continue;
                    }
                    rs.updateString(tcols[c], value);
                }
                rs.updateRow();
                ++row;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new BuildException("SapphireException: " + e.getMessage());
        }
        finally {
            ((DBUtil)this.dbu).reset();
        }
        if (this.isCommandLine) {
            System.out.println("Copy value from " + fromcolumnid + " to column " + tocolumnid + " for table " + tableid + " complete in " + (System.currentTimeMillis() - starttime) + "ms");
        } else {
            this.log("Copy value from " + fromcolumnid + " to column " + tocolumnid + " for table " + tableid + " complete in " + (System.currentTimeMillis() - starttime) + "ms");
        }
    }

    public void setFailonerror(boolean failonerror) {
        this.failonerror = failonerror;
    }

    public void setTableid(String tableid) {
        this.tableid = tableid;
    }

    public void setKeycolumnid(String keycolumnid) {
        this.keycolumnid = keycolumnid;
    }

    public void setFromcolumnid(String fromcolumnid) {
        this.fromcolumnid = fromcolumnid;
    }

    public void setTocolumnid(String tocolumnid) {
        this.tocolumnid = tocolumnid;
    }

    public void setDbu(DBUtil dbu) {
        this.dbu = dbu;
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connection = connection;
    }

    public void addPropertyset(PropertySet propertySet) {
        this.propertSet.add(propertySet);
    }
}

