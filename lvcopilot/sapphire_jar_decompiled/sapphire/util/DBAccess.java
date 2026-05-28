/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import sapphire.SapphireException;

public interface DBAccess {
    public String newName();

    public boolean checkExists(String var1) throws SapphireException;

    public boolean checkPreparedExists(String var1, Object[] var2) throws SapphireException;

    public int getCount(String var1) throws SapphireException;

    public int getPreparedCount(String var1, Object[] var2) throws SapphireException;

    public void createResultSet(String var1, String var2) throws SapphireException;

    public void createResultSet(String var1) throws SapphireException;

    public void createPreparedResultSet(String var1, Object[] var2) throws SapphireException;

    public void createPreparedResultSet(String var1, String var2, Object[] var3) throws SapphireException;

    public CallableStatement prepareCall(String var1, String var2) throws SapphireException;

    public CallableStatement prepareCall(String var1) throws SapphireException;

    public PreparedStatement prepareStatement(String var1, String var2) throws SapphireException;

    public PreparedStatement prepareStatement(String var1) throws SapphireException;

    public boolean getNext(String var1) throws SapphireException;

    public boolean getNext() throws SapphireException;

    public String getString(String var1, String var2) throws SapphireException;

    public String getString(String var1) throws SapphireException;

    public String getValue(String var1, String var2) throws SapphireException;

    public String getValue(String var1) throws SapphireException;

    public BigDecimal getBigDecimal(String var1, String var2) throws SapphireException;

    public BigDecimal getBigDecimal(String var1) throws SapphireException;

    public int getInt(String var1, String var2) throws SapphireException;

    public int getInt(String var1) throws SapphireException;

    public Timestamp getTimestamp(String var1, String var2) throws SapphireException;

    public Timestamp getTimestamp(String var1) throws SapphireException;

    public String getBinaryStream(String var1, String var2) throws SapphireException;

    public String getBinaryStream(String var1) throws SapphireException;

    public Blob getBlob(String var1, String var2) throws SapphireException;

    public Blob getBlob(String var1) throws SapphireException;

    public String getClob(String var1, String var2) throws SapphireException;

    public String getClob(String var1) throws SapphireException;

    public void closeResultSet(String var1);

    public void closeResultSet();

    public void closeCall(String var1);

    public void closeCall();

    public void closeStatement(String var1);

    public void closeStatement();

    public void executeSQL(String var1) throws SapphireException;

    public int executeUpdate(String var1) throws SapphireException;

    public int getColumnCount(String var1) throws SapphireException;

    public int getColumnCount() throws SapphireException;

    public String getColumnName(String var1, int var2) throws SapphireException;

    public String getColumnName(int var1) throws SapphireException;

    public ResultSet getResultSet() throws SapphireException;

    public ResultSet getResultSet(String var1) throws SapphireException;

    public Connection getConnection();

    public int executePreparedUpdate(String var1, Object[] var2) throws SapphireException;

    public int getQueryTimeout();

    public void setQueryTimeout(int var1);

    public String hint(String var1);

    public boolean isOracle();

    public boolean isSqlServer();
}

