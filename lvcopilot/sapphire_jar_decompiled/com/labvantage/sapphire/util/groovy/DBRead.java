/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  groovy.lang.Closure
 */
package com.labvantage.sapphire.util.groovy;

import com.labvantage.sapphire.DBUtil;
import groovy.lang.Closure;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import sapphire.SapphireException;

public class DBRead {
    DBUtil dbu;

    public DBRead(DBUtil dbu) {
        this.dbu = dbu;
    }

    public String getDbms() {
        return this.dbu.getDbms();
    }

    public boolean isOracle() {
        return this.dbu.isOracle();
    }

    public boolean isSqlServer() {
        return this.dbu.isSqlServer();
    }

    public boolean exists(String sql) throws SapphireException {
        return this.dbu.checkExists(sql);
    }

    public int count(String sql) throws SapphireException {
        return this.dbu.getCount(sql);
    }

    public String selectValue(String sql) throws SapphireException {
        return this.selectValue(sql, "");
    }

    public String selectValue(String sql, String defaultValue) throws SapphireException {
        String value = this.selectString(sql);
        return value != null ? value : defaultValue;
    }

    public String selectString(String sql) throws SapphireException {
        Object o = this.selectObject(sql);
        return o != null ? (String)o : null;
    }

    public Calendar selectDate(String sql) throws SapphireException {
        Calendar c = null;
        if (this.isSqlServer()) {
            sql = DBUtil.checkUnicode(sql);
        }
        try {
            Statement stmt = this.dbu.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                String temptimestamp = rs.getString(1);
                if (temptimestamp != null) {
                    Date tempdate = new Date();
                    tempdate.setTime(rs.getTimestamp(1).getTime());
                    c = Calendar.getInstance();
                    c.setTime(tempdate);
                }
                return c;
            }
            stmt.close();
            stmt = null;
        }
        catch (SQLException e) {
            throw new SapphireException("selectObject Exception: " + e.getMessage(), e);
        }
        return c;
    }

    public BigDecimal selectNumber(String sql) throws SapphireException {
        Object o = this.selectObject(sql);
        return o != null ? (BigDecimal)o : null;
    }

    private Object selectObject(String sql) throws SapphireException {
        Object o = null;
        if (this.isSqlServer()) {
            sql = DBUtil.checkUnicode(sql);
        }
        try {
            Statement stmt = this.dbu.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                o = rs.getObject(1);
            }
            stmt.close();
            stmt = null;
        }
        catch (SQLException e) {
            throw new SapphireException("selectObject Exception: " + e.getMessage(), e);
        }
        return o;
    }

    public HashMap selectRow(String sql) throws SapphireException {
        try {
            String name = this.dbu.newName();
            this.dbu.createResultSet(name, sql);
            ResultSet rs = this.dbu.getResultSet(name);
            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();
            if (rs.next()) {
                return this.getRow(rs, rsmd, cols);
            }
            return null;
        }
        catch (Exception e) {
            throw new SapphireException("Error processing selectRow. Exception: " + e.getMessage(), e);
        }
    }

    public void eachRow(String sql, Closure c) throws SapphireException {
        try {
            String name = this.dbu.newName();
            this.dbu.createResultSet(name, sql);
            ResultSet rs = this.dbu.getResultSet(name);
            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();
            while (rs.next()) {
                c.call((Object)this.getRow(rs, rsmd, cols));
            }
            this.dbu.closeResultSet(name);
        }
        catch (Exception e) {
            throw new SapphireException("Error processing eachRow. Exception: " + e.getMessage(), e);
        }
    }

    private HashMap getRow(ResultSet rs, ResultSetMetaData rsmd, int cols) throws SQLException, IOException {
        HashMap<String, Object> row = new HashMap<String, Object>();
        block6: for (int i = 0; i < cols; ++i) {
            String columnid = rsmd.getColumnName(i + 1).toLowerCase();
            switch (rsmd.getColumnType(i + 1)) {
                case -9: 
                case 1: 
                case 12: {
                    row.put(columnid, rs.getString(columnid));
                    continue block6;
                }
                case -4: 
                case 2005: {
                    String value = "";
                    if (this.dbu.isOracle()) {
                        int length;
                        Clob clob = rs.getClob(columnid);
                        if (clob != null && (length = (int)clob.length()) > 0) {
                            value = clob.getSubString(1L, length);
                        }
                    } else {
                        StringBuffer sb = null;
                        InputStream is = rs.getBinaryStream(columnid);
                        if (is != null) {
                            sb = new StringBuffer("");
                            BufferedReader in = null;
                            in = new BufferedReader(new InputStreamReader(is));
                            String buffer = "";
                            while ((buffer = in.readLine()) != null) {
                                sb.append(buffer + System.getProperty("line.separator"));
                            }
                        }
                        value = sb == null ? null : sb.toString();
                    }
                    row.put(columnid, value == null ? null : value.trim());
                    continue block6;
                }
                case 2: 
                case 3: 
                case 4: {
                    row.put(columnid, rs.getBigDecimal(columnid));
                    continue block6;
                }
                case 91: 
                case 93: {
                    String temptimestamp = rs.getString(columnid);
                    Calendar cal = null;
                    if (temptimestamp != null) {
                        Date tempdate = new Date();
                        tempdate.setTime(rs.getTimestamp(columnid).getTime());
                        cal = Calendar.getInstance();
                        cal.setTime(tempdate);
                    }
                    row.put(columnid, cal);
                }
            }
        }
        return row;
    }
}

