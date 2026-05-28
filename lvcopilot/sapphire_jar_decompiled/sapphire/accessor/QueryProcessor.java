/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.accessor;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.ejb.RemoteAccessManager;
import java.io.File;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;

public class QueryProcessor
extends BaseAccessor {
    private ConnectionInfo connectionInfo;

    public QueryProcessor(String connectionid) {
        super(connectionid);
        ConnectionProcessor conn = new ConnectionProcessor(connectionid);
        this.connectionInfo = conn.getConnectionInfo(connectionid);
    }

    public QueryProcessor(String nameserverlist, String connectionid) {
        super(connectionid);
        ConnectionProcessor conn = new ConnectionProcessor(connectionid);
        this.connectionInfo = conn.getConnectionInfo(connectionid);
    }

    public QueryProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
        ConnectionProcessor conn = new ConnectionProcessor(rakFile, connectionid);
        this.connectionInfo = conn.getConnectionInfo(connectionid);
    }

    public QueryProcessor(PageContext pageContext) {
        super(pageContext);
        ConnectionProcessor conn = new ConnectionProcessor(pageContext);
        this.connectionInfo = conn.getConnectionInfo(this.getConnectionid());
    }

    public DataSet getSqlDataSet(String name, String sql) {
        return this.getSqlDataSet(name, sql, false);
    }

    public DataSet getSqlDataSet(String sql) {
        return this.getSqlDataSet("", sql, false);
    }

    public DataSet getSqlDataSet(int sqlCode) {
        return this.getSqlDataSet(sqlCode, false);
    }

    public DataSet getSqlDataSet(int sqlCode, Object[] bindVars) {
        return this.getSqlDataSet(sqlCode, bindVars, false);
    }

    public DataSet getSqlDataSet(String sql, boolean extendedDataTypes) {
        return this.getSqlDataSet("", sql, extendedDataTypes);
    }

    public DataSet getSqlDataSet(int sqlCode, boolean extendedDataTypes) {
        return this.getSqlDataSet(sqlCode, null, extendedDataTypes);
    }

    public DataSet getSqlDataSet(int sqlCode, Object[] bindVars, boolean extendedDataTypes) {
        DataSet ds = null;
        try {
            ds = local ? this.getLocalAccessManager().getSQLDataSet(this.getConnectionid(), sqlCode, bindVars, extendedDataTypes) : this.getRemoteAccessManager().getSQLDataSet(this.getConnectionid(), sqlCode, bindVars, extendedDataTypes);
            ds.setM18NUtil(new M18NUtil(this.connectionInfo));
        }
        catch (Exception e) {
            this.setError("Failed to get SQL DataSet. Exception: " + e.getMessage(), e);
        }
        return ds;
    }

    public DataSet getSqlDataSet(String name, String sql, boolean extendedDataTypes) {
        return this.getSqlDataSet(name, sql, extendedDataTypes, -1);
    }

    public DataSet getSqlDataSet(String name, String sql, boolean extendedDataTypes, int queryTimeout) {
        return this.getSqlDataSet(name, sql, extendedDataTypes, -1, true);
    }

    public DataSet getSqlDataSet(String name, String sql, boolean extendedDataTypes, int queryTimeout, boolean keepAlive) {
        DataSet ds = null;
        try {
            sql = StringUtil.replaceAll(sql, "[currentuser]", this.connectionInfo.getSysuserId());
            DataSet dataSet = ds = local ? this.getLocalAccessManager().getSQLDataSet(this.getConnectionid(), name, sql, extendedDataTypes, queryTimeout, keepAlive) : this.getRemoteAccessManager().getSQLDataSet(this.getConnectionid(), name, sql, extendedDataTypes, queryTimeout, keepAlive);
            if (ds != null) {
                ds.setM18NUtil(new M18NUtil(this.connectionInfo));
            }
        }
        catch (Exception e) {
            this.setError("Failed to get SQL DataSet. Exception: " + e.getMessage(), e);
        }
        return ds;
    }

    public DataSet getPreparedSqlDataSet(String sql, Object[] params) {
        return this.getPreparedSqlDataSet("", sql, params, false, -1);
    }

    public DataSet getPreparedSqlDataSet(int sqlCode, Object[] params) {
        return this.getPreparedSqlDataSet(sqlCode, params, false);
    }

    public DataSet getPreparedSqlDataSet(String name, String sql, Object[] params) {
        return this.getPreparedSqlDataSet(name, sql, params, false, -1);
    }

    public DataSet getPreparedSqlDataSet(String sql, Object[] params, boolean extendedDataTypes) {
        return this.getPreparedSqlDataSet("", sql, params, extendedDataTypes, -1);
    }

    public DataSet getPreparedSqlDataSet(int sqlCode, Object[] params, boolean extendedDataTypes) {
        DataSet ds = null;
        try {
            ds = local ? this.getLocalAccessManager().getPreparedSqlDataSet(this.getConnectionid(), sqlCode, params, extendedDataTypes) : this.getRemoteAccessManager().getPreparedSqlDataSet(this.getConnectionid(), sqlCode, params, extendedDataTypes);
            ds.setM18NUtil(new M18NUtil(this.connectionInfo));
        }
        catch (Exception e) {
            this.setError("Failed to get SQL DataSet. Exception: " + e.getMessage(), e);
        }
        return ds;
    }

    public DataSet getPreparedSqlDataSet(String name, String sql, Object[] params, boolean extendedDataTypes) {
        return this.getPreparedSqlDataSet(name, sql, params, extendedDataTypes, -1);
    }

    public DataSet getPreparedSqlDataSet(String name, String sql, Object[] params, boolean extendedDataTypes, int queryTimeout) {
        DataSet ds = null;
        try {
            sql = StringUtil.replaceAll(sql, "[currentuser]", this.connectionInfo.getSysuserId());
            ds = local ? this.getLocalAccessManager().getPreparedSqlDataSet(this.getConnectionid(), name, sql, params, extendedDataTypes, queryTimeout) : this.getRemoteAccessManager().getPreparedSqlDataSet(this.getConnectionid(), name, sql, params, extendedDataTypes, queryTimeout);
            ds.setM18NUtil(new M18NUtil(this.connectionInfo));
        }
        catch (Exception e) {
            this.setError("Failed to get SQL DataSet. Exception: " + e.getMessage(), e);
        }
        return ds;
    }

    public int getPreparedCount(String sql, Object[] params) throws SapphireException {
        DataSet ds = null;
        try {
            DataSet dataSet = ds = local ? this.getLocalAccessManager().getPreparedSqlDataSet(this.getConnectionid(), "", sql, params, false, -1) : this.getRemoteAccessManager().getPreparedSqlDataSet(this.getConnectionid(), "", sql, params, false, -1);
            if (ds != null && ds.size() == 1) {
                String columnid = ds.getColumnId(0);
                if (ds.getColumnType(columnid) == 1) {
                    return ds.getInt(0, columnid);
                }
                throw new SapphireException("Unable to determine count. SQL should be of the type SELECT count(*) FROM ...");
            }
            throw new SapphireException("Unable to determine cound. SQL should be of the type SELECT count(*) FROM ...");
        }
        catch (Exception e) {
            throw new SapphireException("Unable to determine execute the sql :" + sql + " Message: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public int getCount(String sql) throws SapphireException {
        DataSet ds = null;
        try {
            DataSet dataSet = ds = local ? this.getLocalAccessManager().getSQLDataSet(this.getConnectionid(), "", sql, false, -1) : this.getRemoteAccessManager().getSQLDataSet(this.getConnectionid(), "", sql, false, -1);
            if (ds != null && ds.size() == 1) {
                String columnid = ds.getColumnId(0);
                if (ds.getColumnType(columnid) == 1) {
                    return ds.getInt(0, columnid);
                }
                throw new SapphireException("Unable to determine count. SQL should be of the type SELECT count(*) FROM ...");
            }
            throw new SapphireException("Unable to determine cound. SQL should be of the type SELECT count(*) FROM ...");
        }
        catch (Exception e) {
            throw new SapphireException("Unable to determine execute the sql :" + sql + " Message: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public DataSet getRefTypeDataSet(String reftypeid) {
        DataSet ds = null;
        try {
            ds = local ? this.getLocalAccessManager().getRefTypeDataSet(this.getConnectionid(), reftypeid) : this.getRemoteAccessManager().getRefTypeDataSet(this.getConnectionid(), reftypeid);
        }
        catch (Exception e) {
            this.setError("Failed to get RefType DataSet. Exception: " + e.getMessage(), e);
        }
        return ds;
    }

    public String getKeyid1List(String sdcid, String queryid, String param1, String param2, String param3, String param4, String param5) {
        String keyid1 = "";
        try {
            if (local) {
                keyid1 = local ? this.getLocalAccessManager().getQueryKeyid1List(this.getConnectionid(), sdcid, queryid, new String[]{param1, param2, param3, param4, param5}) : this.getRemoteAccessManager().getQueryKeyid1List(this.getConnectionid(), sdcid, queryid, new String[]{param1, param2, param3, param4, param5});
            } else {
                RemoteAccessManager s = this.getRemoteAccessManager();
                String[] ss = new String[]{param1, param2, param3, param4, param5};
                keyid1 = s.getQueryKeyid1List(this.getConnectionid(), sdcid, queryid, ss);
            }
        }
        catch (Exception e) {
            this.setError("Failed to get keyid1 list. Exception: " + e.getMessage(), e);
        }
        return keyid1;
    }

    public String getKeyid1List(String sdcid, String queryid) {
        String keyid1 = "";
        try {
            if (local) {
                keyid1 = local ? this.getLocalAccessManager().getQueryKeyid1List(this.getConnectionid(), sdcid, queryid, new String[0]) : this.getRemoteAccessManager().getQueryKeyid1List(this.getConnectionid(), sdcid, queryid, new String[0]);
            } else {
                RemoteAccessManager s = this.getRemoteAccessManager();
                String[] ss = new String[]{"1", "2"};
                keyid1 = s.getQueryKeyid1List(this.getConnectionid(), sdcid, queryid, ss);
            }
        }
        catch (Exception e) {
            this.setError("Failed to get keyid1 list. Exception: " + e.getMessage(), e);
        }
        return keyid1;
    }

    public String getKeyid1List(String sdcid, String queryid, String[] params) {
        String keyid1 = "";
        try {
            keyid1 = local ? this.getLocalAccessManager().getQueryKeyid1List(this.getConnectionid(), sdcid, queryid, params) : this.getRemoteAccessManager().getQueryKeyid1List(this.getConnectionid(), sdcid, queryid, params);
        }
        catch (Exception e) {
            this.setError("Failed to get keyid1 list. Exception: " + e.getMessage(), e);
        }
        return keyid1;
    }

    public int execSQL(String sql) {
        int rc = 1;
        try {
            if (local) {
                this.getLocalAccessManager().execSQL(this.getConnectionid(), sql);
            } else {
                this.getRemoteAccessManager().execSQL(this.getConnectionid(), sql);
            }
        }
        catch (Exception e) {
            rc = 2;
            this.setError("Failed to get keyid1 list. Exception: " + e.getMessage(), e);
        }
        return rc;
    }

    public int execPreparedUpdate(String sql, Object[] bindvars) {
        int rc = 1;
        try {
            if (local) {
                this.getLocalAccessManager().execPreparedUpdate(this.getConnectionid(), sql, bindvars);
            } else {
                this.getRemoteAccessManager().execPreparedUpdate(this.getConnectionid(), sql, bindvars);
            }
        }
        catch (Exception e) {
            rc = 2;
            this.setError("Failed to get keyid1 list. Exception: " + e.getMessage(), e);
        }
        return rc;
    }

    public int execSQL(int sqlCode) {
        int rc = 1;
        try {
            if (local) {
                this.getLocalAccessManager().execSQL(this.getConnectionid(), sqlCode, null);
            } else {
                this.getRemoteAccessManager().execSQL(this.getConnectionid(), sqlCode, null);
            }
        }
        catch (Exception e) {
            rc = 2;
            this.setError("Failed to get keyid1 list. Exception: " + e.getMessage(), e);
        }
        return rc;
    }

    public int execSQL(int sqlCode, Object[] bindVars) {
        int rc = 1;
        try {
            if (local) {
                this.getLocalAccessManager().execSQL(this.getConnectionid(), sqlCode, bindVars);
            } else {
                this.getRemoteAccessManager().execSQL(this.getConnectionid(), sqlCode, bindVars);
            }
        }
        catch (Exception e) {
            rc = 2;
            this.setError("Failed to get keyid1 list. Exception: " + e.getMessage(), e);
        }
        return rc;
    }

    public String getSecurityFilterWhere(String sdcid) {
        String whereClause = "";
        try {
            whereClause = local ? this.getLocalAccessManager().getSecurityFilterWhere(this.getConnectionid(), sdcid) : this.getRemoteAccessManager().getSecurityFilterWhere(this.getConnectionid(), sdcid);
        }
        catch (Exception e) {
            this.setError("Failed to get Security filter clause. Exception: " + e.getMessage(), e);
        }
        return whereClause;
    }
}

