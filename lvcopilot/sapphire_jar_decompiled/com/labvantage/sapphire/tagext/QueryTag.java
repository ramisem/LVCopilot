/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.QueryStatus;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import javax.servlet.jsp.JspTagException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.tagext.QueryTagInfo;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.util.StringUtil;

public class QueryTag
extends BaseBodyTagSupport {
    private QueryData _querydata = new QueryData();
    private String _sql = "";
    private int _sqlcode = -1;
    private String _bindvars = "";
    private String _queryid = "";
    private String _basedonid = "";
    private String _queryargid = "";
    private String _var = "querydata";
    private String _varStatus = "querystatus";
    private boolean _unregisteredsqloverride = false;
    private String _error = "";

    public void setSql(String sql) {
        this._sql = sql;
    }

    public void setSqlcode(int sqlcode) {
        this._sqlcode = sqlcode;
    }

    public void setBindvars(String bindvars) {
        this._bindvars = bindvars;
    }

    public void setQueryid(String queryid) {
        this._queryid = queryid;
    }

    public void setBasedonid(String basedonid) {
        this._basedonid = basedonid;
    }

    public void setQueryargid(String queryargid) {
        this._queryargid = queryargid;
    }

    public void setunregisteredsqloverride(String unregisteredsqloverride) {
        this._unregisteredsqloverride = unregisteredsqloverride.equalsIgnoreCase("Y") || unregisteredsqloverride.equalsIgnoreCase("true");
    }

    public void setNullvalue(String nullvalue) {
        this._querydata.setNullValue(nullvalue);
    }

    public void setVar(String var) {
        this._var = var;
    }

    public void setVarStatus(String varStatus) {
        this._varStatus = varStatus;
    }

    public QueryData getQuerydata() {
        return this.getQuerydata("");
    }

    public QueryData getQuerydata(String filter) {
        QueryData qd = null;
        if (filter == null || filter.length() <= 0) {
            qd = this._querydata;
        }
        return qd;
    }

    public int doStartTag() throws JspTagException {
        int rc = 0;
        this.doInit();
        this.evaluateExpressions();
        if (this.isControlledPage()) {
            if (this.getQueryData() == 1) {
                rc = 2;
            }
        } else {
            this.goErrorPage("RequestContext of controlled page tag does not exist. Tags can only be used via the Request Controller and in a controlled page.");
        }
        return rc;
    }

    public int getQueryData() {
        int rc = 2;
        boolean sqlquery = false;
        boolean sqlcode = false;
        QueryProcessor qp = new QueryProcessor(this.pageContext);
        ConnectionProcessor cp = new ConnectionProcessor(this.pageContext);
        ConnectionInfo connectionInfo = cp.getConnectionInfo(this.requestContext.getConnectionid());
        if (this._queryid == null || this._queryid.length() == 0) {
            if (this._sql == null || this._sql.length() == 0) {
                if (this._sqlcode == -1) {
                    this._error = this._error + "TAG ERROR: Queryid, sql or sqlcode attribute not specified in Query tag<br>";
                } else {
                    sqlcode = true;
                }
            } else {
                sqlquery = true;
            }
        } else if (this._basedonid == null || this._basedonid.length() == 0 || this._queryargid == null || this._queryargid.length() == 0) {
            this._error = this._error + "TAG ERROR: Queryid must be specified with a basedon and queryargid attribute in Query tag<br>";
        }
        if (this._error.length() == 0) {
            if (sqlquery) {
                if (this._unregisteredsqloverride || SecurityPolicyUtil.isUnregisteredSQLPermitted(connectionInfo.getConnectionId(), "ajax", "QueryTag.getQueryData", this._sql)) {
                    if (Trace.on) {
                        this.logTrace("Executing sql: " + this._sql);
                    }
                    this._querydata.setQueryData(qp.getSqlDataSet(this._sql));
                } else {
                    this._error = "Failed to execute SQL:" + this._sql + ". Reason: Allow unregistered SQL is set to No in security policy";
                }
            } else if (sqlcode) {
                if (this._bindvars.length() > 0) {
                    Object[] bindvars = StringUtil.split(this._bindvars, ";");
                    this._querydata.setQueryData(qp.getPreparedSqlDataSet(this._sqlcode, bindvars));
                } else {
                    this._querydata.setQueryData(qp.getSqlDataSet(this._sqlcode));
                }
            } else {
                DataSet queryargSql;
                if (Trace.on) {
                    this.logTrace("Executing query: " + this._queryid);
                }
                if ((queryargSql = qp.getPreparedSqlDataSet("SELECT argdata FROM queryarg WHERE queryid = ? AND basedonid = ? AND argid = ?", new Object[]{this._queryid, this._basedonid, this._queryargid})).size() == 1) {
                    String sql = queryargSql.getValue(0, "argdata");
                    sql = StringUtil.replaceAll(sql, "[%currentuser%]", connectionInfo.getSysuserId());
                    sql = StringUtil.replaceAll(sql, "[%CURRENTUSER%]", connectionInfo.getSysuserId());
                    sql = StringUtil.replaceAll(sql, "[%CurrentUser%]", connectionInfo.getSysuserId());
                    sql = StringUtil.replaceAll(sql, "[%sdcid%]", this._basedonid);
                    sql = StringUtil.replaceAll(sql, "[%SdcId%]", this._basedonid);
                    sql = StringUtil.replaceAll(sql, "[%SDCID%]", this._basedonid);
                    this._querydata.setQueryData(qp.getSqlDataSet(sql));
                }
            }
            if (this._querydata.getQuerydata() != null) {
                this.pageContext.setAttribute("queryinfo", (Object)new QueryTagInfo(this._querydata));
                this.pageContext.setAttribute(this._var, (Object)this._querydata);
                this.pageContext.setAttribute(this._varStatus, (Object)new QueryStatus(this._querydata));
                rc = 1;
            } else {
                this._error = this._error + "TAG ERROR: Failed to get query data";
            }
        }
        return rc;
    }

    public int doAfterBody() throws JspTagException {
        this.writeBodyContent();
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        int rc = 6;
        if (this._error.length() > 0) {
            this.write(this._error);
        }
        this._querydata = new QueryData();
        this._queryid = "";
        this._sql = "";
        this._var = "querydata";
        this._varStatus = "querystatus";
        this._error = "";
        this._unregisteredsqloverride = false;
        super.doEndTag();
        return rc;
    }

    protected void evaluateExpressions() {
        this._sql = JstlUtil.evaluateExpression(this._sql, this.pageContext, "").toString();
        this._queryid = JstlUtil.evaluateExpression(this._queryid, this.pageContext, "").toString();
        this._basedonid = JstlUtil.evaluateExpression(this._basedonid, this.pageContext, "").toString();
        this._queryargid = JstlUtil.evaluateExpression(this._queryargid, this.pageContext, "").toString();
    }
}

