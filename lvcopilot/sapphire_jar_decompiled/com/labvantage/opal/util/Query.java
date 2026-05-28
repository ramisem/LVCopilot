/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class Query {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54139 $";
    private QueryProcessor __QueryProcessor;
    private String __SdcID;
    private String __QueryID;
    private String __Param1;
    private String __Param2;
    private String __Param3;
    private String __Param4;
    private String __Param5;
    private String __Param6;
    private String __Param7;
    private String __Param8;
    private String __Param9;
    private String __Param10;
    private String __Param11;
    private String __Param12;
    private String __SysuserID;
    protected boolean MULTISELECTFLAG = true;
    protected boolean WHERECLAUSEFLAG = false;
    protected boolean ORDERBYCLAUSEFLAG = false;
    protected boolean isaccesscontrolled;
    protected boolean istemplatable;
    private PageContext __PageContext;
    private String __OrderBy;
    private SQLGenerator __SqlGenerator;

    public Query(PageContext pagecontext) {
        this.__PageContext = pagecontext;
        this.__SysuserID = "(system)";
        this.__QueryProcessor = new QueryProcessor(pagecontext);
        this.__SqlGenerator = SQLFactory.getSqlGenerator(pagecontext);
    }

    public Query(PageContext pagecontext, String queryid) {
        this(pagecontext);
        this.__QueryID = queryid;
    }

    public DataSet getGenericQueryDataset(HashMap parammap) {
        StringBuffer query = new StringBuffer();
        String queryid = this.getQueryid();
        if (queryid == null) {
            return null;
        }
        query.append("SELECT ");
        SafeSQL safeSQL = this.__SqlGenerator.getQueryAndArgDetails2(queryid, "DataSet");
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        for (int i = 0; i < ds.size(); ++i) {
            String param;
            if (i == 0) {
                String selectclause = ds.getString(0, "SELECTCLAUSE", "");
                if (selectclause.trim().equals("[application defined columns]") || selectclause.trim().length() == 0) {
                    selectclause = "*";
                }
                query.append(selectclause).append(" FROM ");
                query.append(ds.getValue(0, "FROMCLAUSE"));
                if (ds.getValue(0, "WHERECLAUSE").length() > 0) {
                    query.append(" WHERE ").append(ds.getValue(0, "WHERECLAUSE"));
                }
                if (ds.getValue(0, "ORDERBYCLAUSE").length() > 0) {
                    query.append(" ORDER BY ").append(ds.getValue(0, "ORDERBYCLAUSE"));
                }
            }
            if ((param = ds.getValue(i, "ARGINTO", "")).trim().length() <= 0) continue;
            String paramvalue = (String)parammap.get(param);
            int paramindex = query.indexOf(param);
            if (paramindex == -1) continue;
            query.replace(paramindex, query.indexOf("]", paramindex) + 1, paramvalue);
        }
        return this.__QueryProcessor.getSqlDataSet(query.toString());
    }

    public List getQueryParams() {
        ArrayList<String> queryparamlist = new ArrayList<String>();
        SafeSQL safeSQL = this.__SqlGenerator.getQueryArgsForQuery(this.__QueryID);
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        for (int i = 0; i < ds.size(); ++i) {
            queryparamlist.add(i, ds.getValue(i, "ARGINTO"));
        }
        return queryparamlist;
    }

    public String getKeyid1List(String sdcid, String queryid, String param1, String param2, String param3, String param4, String param5) {
        return this.getKeyid1List(sdcid, queryid, param1, param2, param3, param4, param5, "", "", "", "", "", "", "");
    }

    public String getKeyid1List(String sdcid, String queryid, String param1, String param2, String param3, String param4, String param5, String param6, String param7, String param8, String param9, String param10, String param11, String param12) {
        StringBuilder sb = new StringBuilder();
        this.__SdcID = sdcid;
        this.__QueryID = queryid;
        this.__Param1 = param1;
        this.__Param2 = param2;
        this.__Param3 = param3;
        this.__Param4 = param4;
        this.__Param5 = param5;
        this.__Param6 = param6;
        this.__Param7 = param7;
        this.__Param8 = param8;
        this.__Param9 = param9;
        this.__Param10 = param10;
        this.__Param11 = param11;
        this.__Param12 = param12;
        SDCProcessor sdc = new SDCProcessor(this.__PageContext);
        HashMap sdcprops = sdc.getSDCProperties(sdcid);
        String keyid1 = (String)sdcprops.get("keycolid1");
        String accesscontrolledflag = (String)sdcprops.get("accesscontrolledflag");
        String templatableflag = (String)sdcprops.get("templatableflag");
        if (accesscontrolledflag.equalsIgnoreCase("Y")) {
            this.isaccesscontrolled = true;
        }
        if (templatableflag.equalsIgnoreCase("Y")) {
            this.istemplatable = true;
        }
        String sql = this.getSQL(keyid1);
        DataSet ds = this.__QueryProcessor.getSqlDataSet(sql);
        for (int i = 0; i < ds.size(); ++i) {
            sb.append(ds.getValue(i, keyid1)).append(";");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    protected String getSQL(String keyid1) {
        StringBuilder sql = new StringBuilder();
        boolean whereflag = false;
        String resolvedquery = this.getResolvedQuery(keyid1);
        sql.append(resolvedquery);
        if (this.isaccesscontrolled && !this.getSysuserid().equals("(system)")) {
            if (this.WHERECLAUSEFLAG || whereflag) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append(keyid1).append(" IN (SELECT T4.KEYID1 FROM SDIROLE T4 WHERE T4.SDCID = '").append(this.getSdcid()).append("'").append(" AND T4.PRIVID = 'list' AND T4.ROLEID IN ").append("(SELECT T5.ROLEID FROM SYSUSERROLE T5 WHERE T5.SYSUSERID = '").append(this.getSysuserid()).append("'))");
        }
        if (!this.ORDERBYCLAUSEFLAG) {
            sql.append(" ORDER BY ").append(keyid1);
        } else {
            sql.append(this.__OrderBy);
        }
        return sql.toString();
    }

    protected String getResolvedQuery(String column1) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ").append(column1).append(" FROM ");
        SafeSQL safeSQL = this.__SqlGenerator.getQueryDetails(this.getQueryid());
        DataSet ds1 = this.__QueryProcessor.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        if (ds1.getRowCount() == 1) {
            query.append(ds1.getValue(0, "FROMCLAUSE"));
            if (ds1.getValue(0, "WHERECLAUSE").length() > 0) {
                this.WHERECLAUSEFLAG = true;
                query.append(" WHERE ").append(ds1.getValue(0, "WHERECLAUSE"));
            }
            if (ds1.getValue(0, "ORDERBYCLAUSE").length() > 0) {
                this.ORDERBYCLAUSEFLAG = true;
                this.__OrderBy = " ORDER BY " + ds1.getValue(0, "ORDERBYCLAUSE");
            }
            if (this.getParam1().length() > 0) {
                safeSQL = this.__SqlGenerator.getQueryArgsForQuery(this.getQueryid());
                DataSet ds2 = this.__QueryProcessor.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
                for (int i = 0; i < ds2.getRowCount(); ++i) {
                    if (i == 0) {
                        query.replace(query.indexOf("["), query.indexOf("]") + 1, this.getParam1());
                    }
                    if (i == 1) {
                        query.replace(query.indexOf("["), query.indexOf("]") + 1, this.getParam2());
                    }
                    if (i == 2) {
                        query.replace(query.indexOf("["), query.indexOf("]") + 1, this.getParam3());
                    }
                    if (i == 3) {
                        query.replace(query.indexOf("["), query.indexOf("]") + 1, this.getParam4());
                    }
                    if (i == 4) {
                        query.replace(query.indexOf("["), query.indexOf("]") + 1, this.getParam5());
                    }
                    if (i == 5) {
                        query.replace(query.indexOf("["), query.indexOf("]") + 1, this.getParam6());
                    }
                    if (i == 6) {
                        query.replace(query.indexOf("["), query.indexOf("]") + 1, this.getParam7());
                    }
                    if (i == 7) {
                        query.replace(query.indexOf("["), query.indexOf("]") + 1, this.getParam8());
                    }
                    if (i == 8) {
                        query.replace(query.indexOf("["), query.indexOf("]") + 1, this.getParam9());
                    }
                    if (i == 9) {
                        query.replace(query.indexOf("["), query.indexOf("]") + 1, this.getParam10());
                    }
                    if (i == 10) {
                        query.replace(query.indexOf("["), query.indexOf("]") + 1, this.getParam11());
                    }
                    if (i != 11) continue;
                    query.replace(query.indexOf("["), query.indexOf("]") + 1, this.getParam12());
                }
            }
        }
        return query.toString();
    }

    public QueryProcessor getQueryProcessor() {
        return this.__QueryProcessor;
    }

    public void setQueryProcessor(QueryProcessor parQueryProcessor) {
        this.__QueryProcessor = parQueryProcessor;
    }

    public String getQueryid() {
        return this.__QueryID;
    }

    public void setQueryid(String parQueryid) {
        this.__QueryID = parQueryid;
    }

    public String getParam1() {
        return this.__Param1;
    }

    public void setParam1(String parParam1) {
        this.__Param1 = parParam1;
    }

    public String getParam2() {
        return this.__Param2;
    }

    public void setParam2(String parParam2) {
        this.__Param2 = parParam2;
    }

    public String getParam3() {
        return this.__Param3;
    }

    public void setParam3(String parParam3) {
        this.__Param3 = parParam3;
    }

    public String getParam4() {
        return this.__Param4;
    }

    public void setParam4(String parParam4) {
        this.__Param4 = parParam4;
    }

    public String getParam5() {
        return this.__Param5;
    }

    public void setParam5(String parParam5) {
        this.__Param5 = parParam5;
    }

    public String getParam6() {
        return this.__Param6;
    }

    public void setParam6(String param6) {
        this.__Param6 = param6;
    }

    public String getParam7() {
        return this.__Param7;
    }

    public void setParam7(String param7) {
        this.__Param7 = param7;
    }

    public String getParam8() {
        return this.__Param8;
    }

    public void setParam8(String param8) {
        this.__Param8 = param8;
    }

    public String getParam9() {
        return this.__Param9;
    }

    public void setParam9(String param9) {
        this.__Param9 = param9;
    }

    public String getParam10() {
        return this.__Param10;
    }

    public void setParam10(String param10) {
        this.__Param10 = param10;
    }

    public String getParam11() {
        return this.__Param11;
    }

    public void setParam11(String param11) {
        this.__Param11 = param11;
    }

    public String getParam12() {
        return this.__Param12;
    }

    public void setParam12(String param12) {
        this.__Param12 = param12;
    }

    public String getSysuserid() {
        return this.__SysuserID;
    }

    public void setSysuserid(String parSysuserid) {
        this.__SysuserID = parSysuserid;
    }

    public String getSdcid() {
        return this.__SdcID;
    }

    public void setSdcid(String parSdcid) {
        this.__SdcID = parSdcid;
    }

    public void setPagecontext(PageContext parPagecontext) {
        this.__PageContext = parPagecontext;
    }
}

