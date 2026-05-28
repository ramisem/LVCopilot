/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.BaseClass;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;

public class DBUtil
extends BaseClass
implements DBAccess {
    public static final String LOGNAME = "DBUtil";
    public static final String ORA = "ORA";
    public static final String MSS = "MSS";
    public static final String JDBCURL_ORA = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=${protocol})(HOST=${server})(PORT=${port}))(CONNECT_DATA=(SERVICE_NAME=${sid})))";
    public static final String JDBCURL_MSS = "jdbc:sqlserver://${server}:${port};databaseName=${database};instanceName=${instancename};encrypt=${encrypt};trustServerCertificate=true;hostNameInCertificate=${server};loginTimeout=300";
    public static final String JDBCDRIVER_ORA = "oracle.jdbc.driver.OracleDriver";
    public static final String JDBCDRIVER_MSS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String DEFAULT_NAME = "(default)";
    private ResultSetMetaData _md = null;
    private HashMap _stmtmap = new HashMap();
    private HashMap _rsmap = new HashMap();
    private HashMap _csmap = new HashMap();
    private HashMap _psmap = new HashMap();
    protected Connection _conn = null;
    protected String _dbms = "";
    private String jdbcDriver;
    private int counter = 0;
    private boolean releaseConnection = true;
    private String contextInfo = "(none)";
    private int queryTimeout = -1;
    private String databaseid = "";
    private static HashMap<String, Integer> dbTimeoutMap = new HashMap();

    public static void setDefaultQueryTimeout(String databaseid, int queryTimeout) {
        dbTimeoutMap.put(databaseid, queryTimeout);
    }

    public static int getDefaultQueryTimeout(String databaseid) {
        return databaseid.length() > 0 && dbTimeoutMap.get(databaseid) != null && dbTimeoutMap.get(databaseid) >= 0 ? dbTimeoutMap.get(databaseid) : 0;
    }

    @Override
    public synchronized String newName() {
        return Integer.toString(this.counter++);
    }

    public DBUtil() {
    }

    public DBUtil(String contextInfo) {
        if (contextInfo != null && contextInfo.length() > 0) {
            this.contextInfo = contextInfo;
        }
    }

    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    public void setDatabase(String server, String port, String sid, String username, String password) throws SapphireException {
        this.setDatabase(server, port, sid, username, password, false);
    }

    public void setDatabase(String server, String port, String sid, String username, String password, boolean encryptConnection) throws SapphireException {
        try {
            Class.forName(this.jdbcDriver != null && !this.jdbcDriver.isEmpty() ? this.jdbcDriver : JDBCDRIVER_ORA);
            String jdbcUrl = JDBCURL_ORA.replaceAll("\\$\\{server}", server).replaceAll("\\$\\{port}", port).replaceAll("\\$\\{sid}", sid).replaceAll("\\$\\{protocol}", encryptConnection ? "TCPS" : "TCP");
            this._conn = DriverManager.getConnection(jdbcUrl, username, password);
            this._dbms = ORA;
            this.releaseConnection = false;
        }
        catch (Exception e) {
            throw new SapphireException("setDatabase Exception: " + e.getMessage(), e);
        }
    }

    public void setDatabase(String instancename, String server, String port, String database, String username, String password) throws SapphireException {
        this.setDatabase(instancename, server, port, database, username, password, false, "");
    }

    public void setDatabase(String instancename, String server, String port, String database, String username, String password, boolean encryptConnection, String encryptMode) throws SapphireException {
        try {
            Class.forName(this.jdbcDriver != null && !this.jdbcDriver.isEmpty() ? this.jdbcDriver : JDBCDRIVER_MSS);
            String jdbcUrl = JDBCURL_MSS.replaceAll("\\$\\{server}", server).replaceAll("\\$\\{database}", database).replaceAll("\\$\\{port}", port).replaceAll("\\$\\{instancename}", instancename).replaceAll("\\$\\{encrypt}", encryptConnection ? encryptMode : "false");
            this._conn = DriverManager.getConnection(jdbcUrl, username, password);
            this._dbms = MSS;
            this.releaseConnection = false;
        }
        catch (Exception e) {
            throw new SapphireException("setDatabase Exception: " + e.getMessage(), e);
        }
    }

    public void setReleaseConnection(boolean releaseConnection) {
        this.releaseConnection = releaseConnection;
    }

    public void setConnection(SapphireConnection sapphireConnection) {
        this.databaseid = sapphireConnection.getDatabaseId();
        this.setConnection(sapphireConnection.getDbms(), sapphireConnection.getConnection());
    }

    public void setConnection(String dbms, Connection connection) {
        this.releaseConnection = false;
        this._dbms = dbms;
        this._conn = connection;
    }

    public void setDatabase(String databaseid) throws SapphireException {
        throw new SapphireException("The DBUtil.setDatabase method has been removed - you should not get this error as it is not part of the public API! Contact Development.");
    }

    public String getDbms() {
        return this._dbms;
    }

    @Override
    public boolean isOracle() {
        return this._dbms.equals(ORA);
    }

    @Override
    public boolean isSqlServer() {
        return this._dbms.equals(MSS);
    }

    @Override
    public int getQueryTimeout() {
        return this.queryTimeout >= 0 ? this.queryTimeout : DBUtil.getDefaultQueryTimeout(this.databaseid);
    }

    @Override
    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void reset() {
        this.closeResultSets();
        this.closeCallableStatements();
        this.closePreparedStatements();
        if (this.releaseConnection) {
            this.releaseConnection();
        }
    }

    public void closeResultSets() {
        Set rskeyset = this._rsmap.keySet();
        for (String name : rskeyset) {
            this.closeResultSet(name);
        }
    }

    public void closeCallableStatements() {
        Set cskeyset = this._csmap.keySet();
        for (String name : cskeyset) {
            this.closeCall(name);
        }
    }

    public void closePreparedStatements() {
        Set pskeyset = this._psmap.keySet();
        for (String name : pskeyset) {
            this.closeStatement(name);
        }
    }

    public void reset(Statement stmt) {
        this.closeStatement(stmt);
    }

    @Override
    public Connection getConnection() {
        return this._conn;
    }

    public void releaseConnection() {
        try {
            if (this._conn != null) {
                this._conn.close();
                this._conn = null;
            }
        }
        catch (SQLException e) {
            Trace.logError("ERROR:", "releaseConnection Exception: " + e.getMessage());
        }
    }

    @Override
    public boolean checkExists(String existsSQL) throws SapphireException {
        Trace.logDebug(LOGNAME, existsSQL);
        boolean exists = false;
        try {
            if (this.getDbms().equals(MSS)) {
                existsSQL = DBUtil.checkUnicode(existsSQL);
            }
            Statement stmt = this._conn.createStatement();
            stmt.setQueryTimeout(this.getQueryTimeout());
            ResultSet rs = stmt.executeQuery(existsSQL);
            exists = rs.next();
            stmt.close();
            stmt = null;
        }
        catch (SQLException e) {
            throw new SapphireException("checkExists Exception: " + e.getMessage(), e);
        }
        return exists;
    }

    @Override
    public boolean checkPreparedExists(String existsSQL, Object[] o1) throws SapphireException {
        Trace.logDebug(LOGNAME, existsSQL);
        boolean exists = false;
        try {
            this.createPreparedResultSet(existsSQL, o1);
            exists = this.getNext();
        }
        catch (Exception e) {
            throw new SapphireException("checkExists Exception: " + e.getMessage(), e);
        }
        return exists;
    }

    @Override
    public int getCount(String countSQL) throws SapphireException {
        Trace.logDebug(LOGNAME, countSQL);
        int count = -1;
        if (this.getDbms().equals(MSS)) {
            countSQL = DBUtil.checkUnicode(countSQL);
        }
        try {
            Statement stmt = this._conn.createStatement();
            stmt.setQueryTimeout(this.getQueryTimeout());
            ResultSet rs = stmt.executeQuery(countSQL);
            if (rs.next()) {
                count = rs.getInt(1);
            }
            stmt.close();
            stmt = null;
        }
        catch (SQLException e) {
            throw new SapphireException("getCount Exception: " + e.getMessage(), e);
        }
        return count;
    }

    public int getPreparedCount(String countSQL, Object o1) throws SapphireException {
        int count = -1;
        try {
            this.createPreparedResultSet(countSQL, o1);
            if (this.getNext()) {
                count = this.getResultSet().getInt(1);
            }
        }
        catch (SQLException e) {
            throw new SapphireException("getPreparedCount Exception: " + e.getMessage(), e);
        }
        return count;
    }

    @Override
    public int getPreparedCount(String countSQL, Object[] o1) throws SapphireException {
        int count = -1;
        try {
            this.createPreparedResultSet(countSQL, o1);
            if (this.getNext()) {
                count = this.getResultSet().getInt(1);
            }
        }
        catch (SQLException e) {
            throw new SapphireException("getPreparedCount Exception: " + e.getMessage(), e);
        }
        return count;
    }

    @Override
    public CallableStatement prepareCall(String name, String statement) throws SapphireException {
        Trace.logDebug(LOGNAME, statement);
        this.closeCall(name);
        CallableStatement cs = null;
        try {
            cs = this._conn.prepareCall(statement);
            cs.setQueryTimeout(this.getQueryTimeout());
            this._csmap.put(name, cs);
        }
        catch (SQLException e) {
            throw new SapphireException("createCallableStatement failed for statement " + statement + ". Exception: " + e.getMessage(), e);
        }
        return cs;
    }

    @Override
    public CallableStatement prepareCall(String statement) throws SapphireException {
        return this.prepareCall(DEFAULT_NAME, statement);
    }

    @Override
    public PreparedStatement prepareStatement(String name, String statement) throws SapphireException {
        Trace.logDebug(LOGNAME, statement);
        this.closeStatement(name);
        PreparedStatement ps = null;
        try {
            ps = this._conn.prepareStatement(statement);
            ps.setQueryTimeout(this.getQueryTimeout());
            this._psmap.put(name, ps);
        }
        catch (SQLException e) {
            throw new SapphireException("createPreparedStatement failed for statement " + statement + ". Exception: " + e.getMessage(), e);
        }
        return ps;
    }

    @Override
    public PreparedStatement prepareStatement(String statement) throws SapphireException {
        return this.prepareStatement(DEFAULT_NAME, statement);
    }

    public void createPreparedResultSet(String name, String sql, Object o1) throws SapphireException {
        Object[] o = new Object[]{o1};
        this.createPreparedResultSet(name, sql, o);
    }

    public void createPreparedResultSet(String sql, Object o1) throws SapphireException {
        this.createPreparedResultSet(DEFAULT_NAME, sql, o1);
    }

    public void createPreparedResultSet(String name, String sql, Object o1, Object o2) throws SapphireException {
        Object[] o = new Object[]{o1, o2};
        this.createPreparedResultSet(name, sql, o);
    }

    @Override
    public void createPreparedResultSet(String name, String sql, Object[] o) throws SapphireException {
        Trace.logDebug(LOGNAME, sql);
        this.closeStatement(name);
        try {
            if (Trace.stats) {
                Trace.setStartSQL(!name.equals(DEFAULT_NAME) ? name : sql, (!name.equals(DEFAULT_NAME) ? sql + " " : "") + "Bind Vars: " + Arrays.toString(o));
            }
            if (this.getDbms().equals(MSS)) {
                sql = DBUtil.checkUnicode(sql);
            }
            PreparedStatement ps = this.prepareStatement(name, sql);
            ps.setQueryTimeout(this.getQueryTimeout());
            for (int i = 0; i < o.length; ++i) {
                ps.setObject(i + 1, o[i]);
            }
            ResultSet rs = ps.executeQuery();
            if (this._rsmap.get(name) != null) {
                this.closeResultSet(name);
            }
            this._rsmap.put(name, rs);
            this._stmtmap.put(name, ps);
            if (Trace.stats) {
                Trace.setEndSQL(!name.equals(DEFAULT_NAME) ? name : sql);
            }
        }
        catch (SQLException e) {
            throw new SapphireException("createPreparedResultSet Exception executing " + sql + ". Exception: " + e.getMessage(), e);
        }
    }

    @Override
    public void createPreparedResultSet(String sql, Object[] o) throws SapphireException {
        this.createPreparedResultSet(DEFAULT_NAME, sql, o);
    }

    @Override
    public void createResultSet(String name, String sql) throws SapphireException {
        Trace.logDebug(LOGNAME, sql);
        this.closeResultSet(name);
        try {
            if (Trace.stats) {
                Trace.setStartSQL(sql, "");
            }
            Statement stmt = this._conn.createStatement();
            if (this.getDbms().equals(MSS)) {
                sql = DBUtil.checkUnicode(sql);
            }
            stmt.setQueryTimeout(this.getQueryTimeout());
            ResultSet rs = stmt.executeQuery(sql);
            this._stmtmap.put(name, stmt);
            this._rsmap.put(name, rs);
            if (Trace.stats && !name.equals(DEFAULT_NAME)) {
                Trace.setEndSQL(sql);
            }
        }
        catch (SQLException e) {
            throw new SapphireException("createResultSet Exception executing " + sql + ". Exception: " + e.getMessage(), e);
        }
    }

    @Override
    public void createResultSet(String sql) throws SapphireException {
        this.createResultSet(DEFAULT_NAME, sql);
    }

    @Override
    public boolean getNext(String name) throws SapphireException {
        boolean rc = false;
        try {
            ResultSet rs = (ResultSet)this._rsmap.get(name);
            rc = rs.next();
        }
        catch (SQLException e) {
            throw new SapphireException("getNext Exception: " + e.getMessage(), e);
        }
        return rc;
    }

    @Override
    public boolean getNext() throws SapphireException {
        return this.getNext(DEFAULT_NAME);
    }

    public Object getObject(String column) throws SapphireException {
        return this.getObject(DEFAULT_NAME, column);
    }

    public Object getObject(String name, String column) throws SapphireException {
        try {
            ResultSet rs = (ResultSet)this._rsmap.get(name);
            return rs.getObject(column);
        }
        catch (SQLException e) {
            throw new SapphireException("getValue '" + column + "', Exception: " + e.getMessage(), e);
        }
    }

    @Override
    public String getValue(String column) throws SapphireException {
        return this.getValue(DEFAULT_NAME, column);
    }

    @Override
    public String getValue(String name, String column) throws SapphireException {
        String value = "";
        try {
            ResultSet rs = (ResultSet)this._rsmap.get(name);
            Object o = rs.getObject(column);
            if (o != null) {
                if (o instanceof Date) {
                    java.util.Date tempdate = new java.util.Date();
                    tempdate.setTime(((Date)o).getTime());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(tempdate);
                    DateFormat dateFormat = DateFormat.getInstance();
                    value = dateFormat.format(cal.getTime());
                } else if (o instanceof Timestamp) {
                    java.util.Date tempdate = new java.util.Date();
                    tempdate.setTime(((Timestamp)o).getTime());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(tempdate);
                    DateFormat dateFormat = DateFormat.getInstance();
                    value = dateFormat.format(cal.getTime());
                } else {
                    value = o.toString();
                }
            }
        }
        catch (SQLException e) {
            throw new SapphireException("getValue '" + column + "', Exception: " + e.getMessage(), e);
        }
        return value;
    }

    @Override
    public String getString(String name, String column) throws SapphireException {
        String value = "";
        try {
            ResultSet rs = (ResultSet)this._rsmap.get(name);
            value = rs.getString(column);
        }
        catch (SQLException e) {
            throw new SapphireException("getString '" + column + "', Exception: " + e.getMessage(), e);
        }
        return value;
    }

    @Override
    public String getString(String column) throws SapphireException {
        return this.getString(DEFAULT_NAME, column);
    }

    @Override
    public BigDecimal getBigDecimal(String name, String column) throws SapphireException {
        BigDecimal value = null;
        try {
            ResultSet rs = (ResultSet)this._rsmap.get(name);
            value = rs.getBigDecimal(column);
        }
        catch (SQLException e) {
            throw new SapphireException("getBigDecimal '" + column + "', Exception: " + e.getMessage());
        }
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(String column) throws SapphireException {
        return this.getBigDecimal(DEFAULT_NAME, column);
    }

    @Override
    public int getInt(String name, String column) throws SapphireException {
        int value = 0;
        try {
            ResultSet rs = (ResultSet)this._rsmap.get(name);
            value = rs.getInt(column);
        }
        catch (SQLException e) {
            throw new SapphireException("getInt '" + column + "', Exception: " + e.getMessage(), e);
        }
        return value;
    }

    @Override
    public int getInt(String column) throws SapphireException {
        return this.getInt(DEFAULT_NAME, column);
    }

    @Override
    public Timestamp getTimestamp(String name, String column) throws SapphireException {
        Timestamp value = null;
        try {
            ResultSet rs = (ResultSet)this._rsmap.get(name);
            value = rs.getTimestamp(column);
        }
        catch (SQLException e) {
            throw new SapphireException("getTimestamp '" + column + "', Exception: " + e.getMessage(), e);
        }
        return value;
    }

    @Override
    public Timestamp getTimestamp(String column) throws SapphireException {
        return this.getTimestamp(DEFAULT_NAME, column);
    }

    @Override
    public Blob getBlob(String name, String column) throws SapphireException {
        Blob value = null;
        try {
            ResultSet rs = (ResultSet)this._rsmap.get(name);
            value = rs.getBlob(column);
        }
        catch (SQLException e) {
            throw new SapphireException("getBlob '" + column + "', Exception: " + e.getMessage(), e);
        }
        return value;
    }

    @Override
    public Blob getBlob(String column) throws SapphireException {
        return this.getBlob(DEFAULT_NAME, column);
    }

    @Override
    public String getClob(String name, String column) throws SapphireException {
        String value = "";
        ResultSet rs = (ResultSet)this._rsmap.get(name);
        try {
            if (this.isOracle()) {
                int length;
                Clob clob = rs.getClob(column);
                if (clob != null && (length = (int)clob.length()) > 0) {
                    value = clob.getSubString(1L, length);
                }
            } else {
                value = rs.getString(column);
            }
        }
        catch (SQLException e) {
            throw new SapphireException("getClob '" + column + "', Exception: " + e.getMessage(), e);
        }
        return value == null ? null : value.trim();
    }

    @Override
    public String getClob(String column) throws SapphireException {
        return this.getClob(DEFAULT_NAME, column);
    }

    public String getBytes(String name, String column) throws SapphireException {
        String value = "";
        try {
            ResultSet rs = (ResultSet)this._rsmap.get(name);
            value = new String(rs.getBytes(column));
        }
        catch (SQLException e) {
            throw new SapphireException("getClob '" + column + "', Exception: " + e.getMessage(), e);
        }
        return value;
    }

    public String getBytes(String column) throws SapphireException {
        return this.getBytes(DEFAULT_NAME, column);
    }

    @Override
    public String getBinaryStream(String column) throws SapphireException {
        return this.getClob(DEFAULT_NAME, column);
    }

    @Override
    public String getBinaryStream(String name, String column) throws SapphireException {
        return this.getClob(name, column);
    }

    @Override
    public void closeResultSet(String name) {
        try {
            Statement stmt = (Statement)this._stmtmap.get(name);
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        }
        catch (SQLException e) {
            Trace.logError("ERROR:", "closeResultSet Exception: " + e.getMessage());
        }
    }

    @Override
    public void closeResultSet() {
        this.closeResultSet(DEFAULT_NAME);
    }

    @Override
    public void closeCall(String name) {
        try {
            CallableStatement cs = (CallableStatement)this._csmap.get(name);
            if (cs != null) {
                cs.close();
                cs = null;
            }
        }
        catch (SQLException e) {
            Trace.logError("ERROR:", "closeCall Exception: " + e.getMessage());
        }
    }

    @Override
    public void closeCall() {
        this.closeCall(DEFAULT_NAME);
    }

    @Override
    public void closeStatement(String name) {
        this.closeStatement((PreparedStatement)this._psmap.get(name));
    }

    @Override
    public void closeStatement() {
        this.closeStatement(DEFAULT_NAME);
    }

    public void closeStatement(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        }
        catch (SQLException e) {
            Trace.logError("ERROR:", "closeStatement Exception: " + e.getMessage());
        }
    }

    @Override
    public void executeSQL(String sql) throws SapphireException {
        Trace.logDebug(LOGNAME, sql);
        Statement stmt = null;
        try {
            stmt = this._conn.createStatement();
            stmt.setQueryTimeout(this.getQueryTimeout());
            stmt.execute(this.isSqlServer() ? DBUtil.checkUnicode(sql) : sql);
        }
        catch (SQLException e) {
            throw new SapphireException("executeSQL " + sql + ". Exception: " + e.getMessage(), e);
        }
        finally {
            this.closeStatement(stmt);
        }
    }

    @Override
    public int executeUpdate(String sql) throws SapphireException {
        Trace.logDebug(LOGNAME, sql);
        int rows = 0;
        Statement stmt = null;
        try {
            stmt = this._conn.createStatement();
            stmt.setQueryTimeout(this.getQueryTimeout());
            rows = stmt.executeUpdate(this.isSqlServer() ? DBUtil.checkUnicode(sql) : sql);
        }
        catch (SQLException e) {
            throw new SapphireException("executeUpdate " + sql + ". Exception: " + e.getMessage(), e);
        }
        finally {
            this.closeStatement(stmt);
        }
        return rows;
    }

    public int executePreparedUpdate(String sql, Object o1) throws SapphireException {
        Object[] o = new Object[]{o1};
        return this.executePreparedUpdate(sql, o);
    }

    public int executePreparedUpdate(String sql, Object o1, Object o2) throws SapphireException {
        Object[] o = new Object[]{o1, o2};
        return this.executePreparedUpdate(sql, o);
    }

    @Override
    public int executePreparedUpdate(String sql, Object[] o) throws SapphireException {
        Trace.logDebug(LOGNAME, sql);
        int rows = 0;
        PreparedStatement ps = null;
        try {
            if (Trace.stats) {
                Trace.setStartSQL(sql, "Bind Vars: " + Arrays.toString(o));
            }
            ps = this._conn.prepareStatement(sql);
            ps.setQueryTimeout(this.getQueryTimeout());
            for (int i = 0; i < o.length; ++i) {
                ps.setObject(i + 1, o[i]);
            }
            rows = ps.executeUpdate();
            if (Trace.stats) {
                Trace.setEndSQL(sql);
            }
            this.closeStatement(ps);
        }
        catch (SQLException e) {
            try {
                throw new SapphireException("executePreparedUpdate " + sql + ". Exception: " + e.getMessage(), e);
            }
            catch (Throwable throwable) {
                this.closeStatement(ps);
                throw throwable;
            }
        }
        return rows;
    }

    @Override
    public String hint(String table) {
        return table;
    }

    public void updateClob(String tableid, String clobcolumnid, String clobvalue, String[] keycolumnids, Object[] keyvalues) throws SapphireException {
        try {
            String sql = "UPDATE " + tableid + " SET " + clobcolumnid + " = ? ";
            StringBuffer whereclause = new StringBuffer(" WHERE ");
            if (keycolumnids != null && keyvalues != null && keycolumnids.length == keyvalues.length) {
                for (int i = 0; i < keycolumnids.length; ++i) {
                    whereclause.append(keycolumnids[i] + " = ? ");
                    if (keycolumnids.length <= 1 || i == keycolumnids.length - 1) continue;
                    whereclause.append(" AND ");
                }
            } else {
                throw new SapphireException("GENERAL_ERROR", "Key columnids and values not match.");
            }
            sql = sql + whereclause;
            PreparedStatement ps = this.getConnection().prepareStatement(sql);
            ps.setCharacterStream(1, (Reader)new StringReader(clobvalue), clobvalue.length());
            for (int i = 0; i < keycolumnids.length; ++i) {
                String value = keyvalues[i] instanceof String ? (String)keyvalues[i] : String.valueOf((Integer)keyvalues[i]) + " ";
                ps.setObject(i + 2, value);
            }
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception e) {
            throw new SapphireException("updateClob. Exception: " + e.getMessage(), e);
        }
    }

    public void updateOracleClob(String tableid, String clobcolumnid, String clobvalue, String[] keycolumnids, String[] keyvalues) throws SapphireException {
        this.updateClob(tableid, clobcolumnid, clobvalue, keycolumnids, keyvalues);
    }

    public void updateOracleClob(String tableid, String clobcolumnid, String clobvalue, String whereclause) throws SapphireException {
        if (this.isSqlServer()) {
            throw new SapphireException("GENERAL_ERROR", "updateOracleClob cannot update SQLServer.");
        }
        if (clobvalue != null) {
            try {
                Statement stmt = this._conn.createStatement();
                stmt.execute("UPDATE " + tableid + " SET " + clobcolumnid + "= empty_clob() " + whereclause);
                String sql = "SELECT " + clobcolumnid + " FROM " + tableid + " " + whereclause + " FOR UPDATE";
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    Clob clob = rs.getClob(clobcolumnid);
                    Writer writeClob = clob.setCharacterStream(1L);
                    writeClob.write(clobvalue);
                    writeClob.close();
                    PreparedStatement pstatement = this._conn.prepareStatement("UPDATE " + tableid + " SET " + clobcolumnid + " = ? " + whereclause);
                    pstatement.setClob(1, clob);
                    pstatement.execute();
                    pstatement.close();
                }
                rs.close();
                stmt.close();
            }
            catch (IOException ioe) {
                throw new SapphireException("updateOracleClob. Exception: " + ioe.getMessage(), ioe);
            }
            catch (SQLException sqle) {
                throw new SapphireException("updateOracleClob. Exception: " + sqle.getMessage(), sqle);
            }
        }
    }

    public void updateOracleBlob(String tableid, String blobcolumnid, byte[] blobvalue, String[] keycolumnids, String[] keyvalues) throws SapphireException {
        StringBuffer whereclause = this.buildWhereClause(keycolumnids, keyvalues);
        this.updateOracleBlob(tableid, blobcolumnid, blobvalue, whereclause.toString());
    }

    public void updateOracleBlob(String tableid, String blobcolumnid, byte[] blobvalue, String whereclause) throws SapphireException {
        if (this.isSqlServer()) {
            throw new SapphireException("GENERAL_ERROR", "updateOracleBlob cannot update SQLServer.");
        }
        try {
            Statement stmt = this._conn.createStatement();
            stmt.execute("UPDATE " + tableid + " SET " + blobcolumnid + "= empty_blob() " + whereclause);
            if (blobvalue != null) {
                String sql = "SELECT " + blobcolumnid + " FROM " + tableid + " " + whereclause + " FOR UPDATE";
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    Blob blob = rs.getBlob(blobcolumnid);
                    OutputStream writeBlob = blob.setBinaryStream(1L);
                    writeBlob.write(blobvalue);
                    writeBlob.close();
                    PreparedStatement pstatement = this._conn.prepareStatement("UPDATE " + tableid + " SET " + blobcolumnid + " = ? " + whereclause);
                    pstatement.setBlob(1, blob);
                    pstatement.execute();
                    pstatement.close();
                }
                rs.close();
            }
            stmt.close();
        }
        catch (IOException ioe) {
            throw new SapphireException("updateOracleBlob1. Exception: " + ioe.getMessage(), ioe);
        }
        catch (SQLException sqle) {
            throw new SapphireException("updateOracleBlob2. Exception: " + sqle.getMessage(), sqle);
        }
    }

    private StringBuffer buildWhereClause(String[] keycolumnids, Object[] keyvalues) throws SapphireException {
        StringBuffer whereclause = new StringBuffer(" WHERE ");
        if (keycolumnids != null && keyvalues != null && keycolumnids.length == keyvalues.length) {
            for (int i = 0; i < keycolumnids.length; ++i) {
                whereclause.append(keycolumnids[i] + " = " + (keyvalues[i] instanceof String ? "'" + (String)keyvalues[i] + "' " : String.valueOf((Integer)keyvalues[i]) + " "));
                if (keycolumnids.length <= 1 || i == keycolumnids.length - 1) continue;
                whereclause.append(" AND ");
            }
        } else {
            throw new SapphireException("GENERAL_ERROR", "Key columnids and values not match.");
        }
        return whereclause;
    }

    @Override
    public int getColumnCount(String name) throws SapphireException {
        int cols = 0;
        try {
            ResultSet rs = (ResultSet)this._rsmap.get(name);
            if (this._md == null) {
                this._md = rs.getMetaData();
            }
            cols = this._md.getColumnCount();
        }
        catch (SQLException e) {
            throw new SapphireException("getColumnCount Exception: " + e.getMessage(), e);
        }
        return cols;
    }

    @Override
    public int getColumnCount() throws SapphireException {
        return this.getColumnCount(DEFAULT_NAME);
    }

    @Override
    public String getColumnName(String name, int col) throws SapphireException {
        String column = "";
        try {
            ResultSet rs = (ResultSet)this._rsmap.get(name);
            if (this._md == null) {
                this._md = rs.getMetaData();
            }
            column = this._md.getColumnName(col);
        }
        catch (SQLException e) {
            throw new SapphireException("getColumnName Exception: " + e.getMessage(), e);
        }
        return column;
    }

    @Override
    public String getColumnName(int col) throws SapphireException {
        return this.getColumnName(DEFAULT_NAME, col);
    }

    @Override
    public ResultSet getResultSet(String name) throws SapphireException {
        ResultSet rs = null;
        if (!this._rsmap.containsKey(name)) {
            throw new SapphireException("Invalid ResultSet name.");
        }
        rs = (ResultSet)this._rsmap.get(name);
        return rs;
    }

    @Override
    public ResultSet getResultSet() throws SapphireException {
        return this.getResultSet(DEFAULT_NAME);
    }

    public void getUniqueIds(String dbms, String table, int rows, String processid) throws SapphireException {
        String callstmt = "{call lv_app" + (dbms.equals(ORA) ? "." : "_") + "sdifill( ?, ?, ? ) }";
        Trace.logDebug(LOGNAME, callstmt);
        try {
            CallableStatement cs = this._conn.prepareCall(callstmt);
            cs.setString(1, table);
            cs.setInt(2, rows);
            cs.setString(3, processid);
            cs.execute();
            cs.close();
            cs = null;
        }
        catch (SQLException e) {
            throw new SapphireException("getUniqueIds (" + dbms + ", " + table + ", " + rows + ", " + processid + ") Exception: " + e.getMessage(), e);
        }
    }

    public String[] getUUIDList(int number) throws SapphireException {
        Statement cs = null;
        String out = "";
        try {
            String[] chunk2;
            String callstmt;
            if (this.isOracle()) {
                callstmt = "{call LV_APP.GetUUIDList( ?, ? ) }";
                cs = this._conn.prepareCall(callstmt);
                cs.setInt(1, number);
                cs.registerOutParameter(2, 2005);
                cs.execute();
                Clob c = cs.getClob(2);
                int length = (int)c.length();
                out = length > 0 ? c.getSubString(1L, length) : "";
            } else {
                int chunk2;
                callstmt = "{call LV_APP_GetUniqueVal( ?, ? ) }";
                cs = this._conn.prepareCall(callstmt);
                int n = chunk2 = number > 100 ? 100 : number;
                while (number > 0) {
                    cs.setInt(1, chunk2);
                    cs.registerOutParameter(2, 12);
                    cs.execute();
                    out = out + ";" + cs.getString(2);
                    chunk2 = (number -= chunk2) > 100 ? 100 : number;
                }
                if (out.length() > 0) {
                    out = out.substring(1);
                }
            }
            Trace.logDebug(LOGNAME, callstmt);
            if (out != null && out.length() > 0) {
                chunk2 = StringUtil.split(out, ";");
                return chunk2;
            }
            chunk2 = null;
            return chunk2;
        }
        catch (SQLException e) {
            throw new SapphireException("GetUUIDList( " + number + ") Exception: " + e.getMessage(), e);
        }
        finally {
            if (cs != null) {
                try {
                    cs.close();
                }
                catch (Exception e2) {
                    Trace.logWarn(LOGNAME, "Could not close statement.");
                }
            }
        }
    }

    public String[] getTableSequence(String tableId, int number) throws SapphireException {
        Statement cs = null;
        String out = "";
        try {
            String[] chunk2;
            String callstmt;
            if (this.isOracle()) {
                callstmt = "{call LV_APP.GetTabSeqVal( ?, ?, ? ) }";
                cs = this._conn.prepareCall(callstmt);
                cs.setString(1, tableId);
                cs.setInt(2, number);
                cs.registerOutParameter(3, 2005);
                cs.execute();
                Clob c = cs.getClob(3);
                int length = (int)c.length();
                out = length > 0 ? c.getSubString(1L, length) : "";
            } else {
                int chunk2;
                callstmt = "{call LV_APP_GetUniqueVal( ?, ? ) }";
                cs = this._conn.prepareCall(callstmt);
                int n = chunk2 = number > 100 ? 100 : number;
                while (number > 0) {
                    cs.setInt(1, chunk2);
                    cs.registerOutParameter(2, 12);
                    cs.execute();
                    out = out + ";" + cs.getString(2);
                    chunk2 = (number -= chunk2) > 100 ? 100 : number;
                }
                if (out.length() > 0) {
                    out = out.substring(1);
                }
            }
            Trace.logDebug(LOGNAME, callstmt);
            if (out != null && out.length() > 0) {
                chunk2 = StringUtil.split(out, ";");
                return chunk2;
            }
            chunk2 = null;
            return chunk2;
        }
        catch (SQLException e) {
            throw new SapphireException("getTableSequence (" + tableId + ", " + number + ") Exception: " + e.getMessage(), e);
        }
        finally {
            if (cs != null) {
                try {
                    cs.close();
                }
                catch (Exception e2) {
                    Trace.logWarn(LOGNAME, "Could not close statement.");
                }
            }
        }
    }

    public static String checkUnicode(String sql) {
        StringBuffer value = new StringBuffer();
        boolean isSecondQuote = true;
        boolean foundUnicode = false;
        for (int i = sql.length() - 1; i >= 0; --i) {
            if (sql.charAt(i) != '\'') {
                value.insert(0, sql.charAt(i));
                if (sql.charAt(i) <= '\u007f') continue;
                foundUnicode = true;
                continue;
            }
            if (!isSecondQuote) {
                if (sql.charAt(i - 1) == 'N') {
                    value.insert(0, "N'");
                    --i;
                    isSecondQuote = true;
                    foundUnicode = false;
                    continue;
                }
                if (sql.charAt(i - 1) == '\'') {
                    value.insert(0, "''");
                    --i;
                    continue;
                }
                value.insert(0, foundUnicode ? "N'" : "'");
                isSecondQuote = true;
                foundUnicode = false;
                continue;
            }
            value.insert(0, "'");
            isSecondQuote = false;
        }
        return value.toString();
    }

    public static void main(String[] arg) {
        try {
            DBUtil db = new DBUtil();
            db.setDatabase("MSSQLSERVER", "cliut440pus", "1433", "lv8", "lv8", "lv8");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

