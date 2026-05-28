/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.beans;

import com.labvantage.opal.util.ErrorMsg;
import com.labvantage.opal.util.SdcInfo;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class SdcLookupBean {
    protected static final int MAXROWS = 100;
    protected static final String IMAGEPATH = "WEB-OPAL/pagetypes/manage/images";
    private static final String JSPPATH = "WEB-OPAL/pagetypes/lookup/sdc.jsp";
    private String html = "";
    private String appearance = "blue";
    private String sdcid = "";
    private String sdctableid = "void";
    private String function = "";
    private String fieldid = "";
    private String field = "";
    private String index = "";
    private String queryid = "";
    private String param1 = "";
    private String param2 = "";
    private String param3 = "";
    private String param4 = "";
    private String param5 = "";
    private String param6 = "";
    private String param7 = "";
    private String param8 = "";
    private String param9 = "";
    private String param10 = "";
    private String param11 = "";
    private String param12 = "";
    private String sysuserid = "(system)";
    private String desccolumn = "";
    private String sp = "1";
    private String multiselect = "y";
    private PageContext pagecontext;
    private PageTagInfo pageinfo;
    protected boolean MULTISELECTFLAG = true;
    protected boolean WHERECLAUSEFLAG = false;
    protected boolean ORDERBYCLAUSE = false;
    protected boolean isaccesscontrolled;
    protected boolean istemplatable;
    protected HashMap sdcprops = null;

    public String getHtml() {
        StringBuffer sb = new StringBuffer();
        SDCProcessor sdc = new SDCProcessor(this.pagecontext);
        this.sdcprops = sdc.getSDCProperties(this.getSdcid());
        String sql = "";
        String pkey_0 = (String)this.sdcprops.get("keycolid1");
        String pkey_1 = (String)this.sdcprops.get("keycolid2");
        String pkey_2 = (String)this.sdcprops.get("keycolid3");
        String sdctype = (String)this.sdcprops.get("sdctype");
        int keycolcount = Integer.parseInt((String)this.sdcprops.get("keycolcount"));
        if (this.getAppearance().length() == 0) {
            this.setAppearance("standard");
        }
        if (this.getFunction().length() == 0) {
            this.setMultiselect("n");
        }
        sb.append("<script language='Javascript'>");
        sb.append(" var _queryargs = '';");
        sb.append("</script>");
        if (sdctype.equals("D")) {
            sb.append(this.getErrorHeader());
            sb.append("<br><font color=red><b>").append(ErrorMsg.EU002(this.getSdcid())).append("</b></font>");
            return sb.toString();
        }
        this.setSdctableid((String)this.sdcprops.get("tableid"));
        if (this.getSdctableid().equals("void")) {
            sb.append(this.getErrorHeader());
            sb.append("<font color=red><b>").append(ErrorMsg.EU007(this.getSdcid())).append("</b></font>");
            return sb.toString();
        }
        this.setSysuserid(this.getPageinfo().getProperty("sysuserid"));
        String accesscontrolledflag = (String)this.sdcprops.get("accesscontrolledflag");
        String templatableflag = (String)this.sdcprops.get("templatableflag");
        if (accesscontrolledflag.equalsIgnoreCase("Y")) {
            this.isaccesscontrolled = true;
        }
        if (templatableflag.equalsIgnoreCase("Y")) {
            this.istemplatable = true;
        }
        SafeSQL safeSQL = new SafeSQL();
        sql = keycolcount == 3 ? this.getSQL(pkey_0, pkey_1, pkey_2, safeSQL) : (keycolcount == 2 ? this.getSQL(pkey_0, pkey_1, safeSQL) : this.getSQL(pkey_0, safeSQL));
        DataSet dataset = this.getQueryprocesssor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        sb.append(this.getNavHeader(pkey_0, dataset.getRowCount(), true));
        sb.append("<table border=1 cellpadding=2 cellspacing=0 width='100%'").append(" id='header_table' style='position:relative;'>");
        sb.append("<tr class='row_header_").append(this.getAppearance()).append("'>");
        sb.append("<td align=center width=30>");
        if (this.MULTISELECTFLAG) {
            sb.append("<input type=checkbox onClick=\"opal_checkAll(this, 'main', '").append(this.getAppearance()).append("');\">");
        } else {
            sb.append("&nbsp;");
        }
        sb.append("</td>");
        if (keycolcount == 3) {
            sb.append("<td align=center width=150>").append(pkey_0.toUpperCase()).append("</td>");
            sb.append("<td align=center width=150>").append(pkey_1.toUpperCase()).append("</td>");
            sb.append("<td align=center width='*'>").append(pkey_2.toUpperCase()).append("</td>");
        } else if (keycolcount == 2) {
            sb.append("<td align=center width=150>").append(pkey_0.toUpperCase()).append("</td>");
            sb.append("<td align=center width='*'>").append(pkey_1.toUpperCase()).append("</td>");
        } else {
            sb.append("<td align=center width=150>").append(pkey_0.substring(pkey_0.indexOf("_") + 1).toUpperCase()).append("</td>");
            sb.append("<td align=center width='*'>Description</td>");
        }
        sb.append("</tr></table>");
        sb.append(this.getMainTable(dataset, keycolcount, pkey_0, pkey_1, pkey_2));
        sb.append(this.getQueryBar());
        sb.append(this.getPagescripts());
        this.html = sb.toString();
        return this.html;
    }

    protected QueryProcessor getQueryprocesssor() {
        return this.pageinfo.getQueryProcessor();
    }

    public String getAppearance() {
        return this.appearance;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public void setSdcid(String sdc) {
        this.sdcid = sdc;
    }

    public String getFunction() {
        return this.function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public PageTagInfo getPageinfo() {
        return this.pageinfo;
    }

    public void setPageinfo(PageTagInfo pageinfo) {
        this.pageinfo = pageinfo;
    }

    public String getQueryid() {
        return this.queryid;
    }

    public void setQueryid(String queryid) {
        this.queryid = queryid;
    }

    public String getParam1() {
        return this.param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return this.param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    public String getParam3() {
        return this.param3;
    }

    public void setParam3(String param3) {
        this.param3 = param3;
    }

    public String getParam4() {
        return this.param4;
    }

    public void setParam4(String param4) {
        this.param4 = param4;
    }

    public String getParam5() {
        return this.param5;
    }

    public void setParam5(String param5) {
        this.param5 = param5;
    }

    public String getParam6() {
        return this.param6;
    }

    public void setParam6(String param6) {
        this.param6 = param6;
    }

    public String getParam7() {
        return this.param7;
    }

    public void setParam7(String param7) {
        this.param7 = param7;
    }

    public String getParam8() {
        return this.param8;
    }

    public void setParam8(String param8) {
        this.param8 = param8;
    }

    public String getParam9() {
        return this.param9;
    }

    public void setParam9(String param9) {
        this.param9 = param9;
    }

    public String getParam10() {
        return this.param10;
    }

    public void setParam10(String param10) {
        this.param10 = param10;
    }

    public String getParam11() {
        return this.param11;
    }

    public void setParam11(String param11) {
        this.param11 = param11;
    }

    public String getParam12() {
        return this.param12;
    }

    public void setParam12(String param12) {
        this.param12 = param12;
    }

    protected String getResolvedQuery(String column1, String column2) {
        StringBuffer sql = new StringBuffer();
        StringBuffer query = new StringBuffer();
        query.append("SELECT DISTINCT ").append(column1).append(", nvl(").append(column2).append(", '&nbsp;') ").append(column2).append(" FROM ");
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT T1.FROMCLAUSE, NVL(T1.WHERECLAUSE, '') WHERECLAUSE,").append(" NVL(T1.ORDERBYCLAUSE, '') ORDERBYCLAUSE FROM QUERY T1").append(" WHERE T1.QUERYID = ").append(safeSQL.addVar(this.getQueryid()));
        DataSet ds1 = this.getQueryprocesssor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds1.getRowCount() == 1) {
            query.append(ds1.getValue(0, "FROMCLAUSE"));
            if (ds1.getValue(0, "WHERECLAUSE").length() > 0) {
                this.WHERECLAUSEFLAG = true;
                query.append(" WHERE ").append(ds1.getValue(0, "WHERECLAUSE"));
            }
            if (ds1.getValue(0, "ORDERBYCLAUSE").length() > 0) {
                this.ORDERBYCLAUSE = true;
                query.append(" ORDER BY ").append(ds1.getValue(0, "ORDERBYCLAUSE"));
            }
            if (this.getParam1().length() > 0) {
                StringBuffer sb = new StringBuffer();
                safeSQL.reset();
                sb.append("SELECT ARGINTO FROM QUERYARG WHERE QUERYID = ").append(safeSQL.addVar(this.getQueryid())).append(" ORDER BY USERSEQUENCE");
                DataSet ds2 = this.getQueryprocesssor().getPreparedSqlDataSet(sb.toString(), safeSQL.getValues());
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

    protected String getResolvedQuery(String column1, String column2, String column3) {
        StringBuffer sql = new StringBuffer();
        StringBuffer query = new StringBuffer();
        query.append("SELECT DISTINCT ").append(column1).append(", nvl(").append(column2).append(", '&nbsp;') ").append(column2).append(", nvl(").append(column3).append(", '&nbsp;') ").append(column3).append(" FROM ");
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT T1.FROMCLAUSE, NVL(T1.WHERECLAUSE, '') WHERECLAUSE,").append(" NVL(T1.ORDERBYCLAUSE, '') ORDERBYCLAUSE FROM QUERY T1").append(" WHERE T1.QUERYID = ").append(safeSQL.addVar(this.getQueryid()));
        DataSet ds1 = this.getQueryprocesssor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds1.getRowCount() == 1) {
            query.append(ds1.getValue(0, "FROMCLAUSE"));
            if (ds1.getValue(0, "WHERECLAUSE").length() > 0) {
                this.WHERECLAUSEFLAG = true;
                query.append(" WHERE ").append(ds1.getValue(0, "WHERECLAUSE"));
            }
            if (ds1.getValue(0, "ORDERBYCLAUSE").length() > 0) {
                this.ORDERBYCLAUSE = true;
                query.append(" ORDER BY ").append(ds1.getValue(0, "ORDERBYCLAUSE"));
            }
            if (this.getParam1().length() > 0) {
                StringBuffer sb = new StringBuffer();
                safeSQL.reset();
                sb.append("SELECT ARGINTO FROM QUERYARG WHERE QUERYID = ").append(safeSQL.addVar(this.getQueryid())).append(" ORDER BY USERSEQUENCE");
                DataSet ds2 = this.getQueryprocesssor().getPreparedSqlDataSet(sb.toString(), safeSQL.getValues());
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

    protected String getQueryOptions() {
        StringBuffer option = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryprocesssor().getPreparedSqlDataSet("SELECT QUERYID FROM QUERY WHERE BASEDONID = " + safeSQL.addVar(this.getSdcid()), safeSQL.getValues());
        for (int i = 0; i < ds.getRowCount(); ++i) {
            option.append("<option value='").append(ds.getValue(i, "queryid")).append("'>").append(ds.getValue(i, "queryid")).append("</option>");
        }
        return option.toString();
    }

    protected String getQueryArguments() {
        StringBuffer sql = new StringBuffer();
        StringBuffer queryarg = new StringBuffer();
        String oldqueryid = "";
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT T1.QUERYID, T1.ARGID FROM QUERYARG T1, QUERY T2").append(" WHERE T1.QUERYID = T2.QUERYID AND T2.BASEDONID = ").append(safeSQL.addVar(this.getSdcid())).append(" ORDER BY T1.USERSEQUENCE");
        DataSet ds = this.getQueryprocesssor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String newqueryid = ds.getValue(i, "queryid");
            if (!newqueryid.equals(oldqueryid)) {
                queryarg.append("|").append(newqueryid).append(":").append(ds.getValue(i, "argid"));
            } else {
                queryarg.append(";").append(ds.getValue(i, "argid"));
            }
            oldqueryid = newqueryid;
        }
        return queryarg.toString();
    }

    protected String getQueryBar() {
        StringBuffer sb = new StringBuffer();
        String options = this.getQueryOptions();
        Button button = new Button(this.getPagecontext());
        button.setAppearance("standard");
        button.setWidth("80");
        button.setText("Run Query");
        button.setAction("runQuery()");
        button.setImg("WEB-OPAL/pagetypes/manage/images/execute.gif");
        sb.append("<div id='querydivhead' style='position:absolute;' class='lookup_qheader_").append(this.getAppearance()).append("'>");
        sb.append("<table width=100% cellpadding=2 cellspacing=0").append(" border=0 style='border-top:1px solid black;'>");
        sb.append("<tr style='cursor: pointer;' onClick='showQuery();'>");
        sb.append("<td width=50% height='25'>");
        sb.append("Query Selection");
        sb.append("<td width=50% height='25' align='right' valign='middle'>");
        sb.append("<img src='").append(IMAGEPATH).append("/copyright.gif'>");
        sb.append("</td></tr></table>");
        sb.append("</div>");
        sb.append("<table width=100% cellpadding=2 cellspacing=0 border=0 id='querydiv'");
        sb.append(" style='position:absolute;display:none;' class='lookup_qbody_").append(this.getAppearance()).append("'>");
        sb.append("<tr>");
        if (options.length() > 0) {
            sb.append("<script> var _queryvisibleflag = true; </script>");
            sb.append("<td width=50 height='25'>Query:</td>");
            sb.append("<td width=200>");
            sb.append("<select id='queryselect' onChange='queryChanged();'>").append(options).append("</select>");
            sb.append("</td><td width='*' align='right'>");
            sb.append(button.getHtml());
            sb.append("</td></tr>");
            sb.append("<tr>");
            sb.append("<td width=50>Args:</td><td id='queryargsid' width='*' height='25' colspan=2>");
        } else {
            sb.append("<script> var _queryvisibleflag = false; </script>");
            sb.append("<td height='25' width='100%'>");
            sb.append("No Queries defined.");
            sb.append("</td></tr>");
            sb.append("<tr><td id='queryargsid' width=100% height='25'>");
        }
        sb.append("</td></tr></table>");
        sb.append("<script language='Javascript'>");
        sb.append(" _queryargs = '").append(this.getQueryArguments()).append("';");
        sb.append("</script>");
        return sb.toString();
    }

    protected String getNavHeader(String pkey_0, int rowcount, boolean showtotal) {
        StringBuffer sb = new StringBuffer();
        Button button = new Button(this.getPagecontext());
        button.setAppearance("standard");
        button.setWidth("80");
        button.setImg("WEB-OPAL/pagetypes/manage/images/accept.gif");
        sb.append("<div id='header' style='position:relative;' class='lookup_header_").append(this.getAppearance()).append("'>");
        sb.append("<table border=0 cellpadding=0 cellspacing=0 width='100%'>");
        sb.append("<tr height=22>");
        sb.append("<td valign=top width='100%' colspan=3>");
        sb.append("<table border=0 cellpadding=2 cellspacing=0 width='100%'>");
        sb.append("<tr><td width=50>");
        sb.append("<img src='").append(IMAGEPATH).append("/lvsapphire.gif'>");
        sb.append("</td><td width=150 align=center><b>Lookup ").append(this.getSdcid()).append("(s)</b></td>");
        sb.append("<td width='*' align=right>");
        sb.append("<table border=0 cellpadding=3 cellspacing=0><tr><td width=80>");
        button.setText("Accept");
        button.setAction("accept()");
        button.setImg("WEB-OPAL/pagetypes/manage/images/accept.gif");
        sb.append(button.getHtml());
        sb.append("</td>");
        sb.append("<td width=80>");
        button.setText("Cancel");
        button.setAction("cancel()");
        button.setImg("WEB-OPAL/pagetypes/manage/images/close.gif");
        sb.append(button.getHtml());
        sb.append("</td></tr></table></tr></table>");
        sb.append("</td></tr>");
        sb.append("<tr height='25'>");
        if (this.getQueryid().length() != 0) {
            sb.append("<td colspan=3 width='100%' align='center'>");
            sb.append("Showing results for query '").append(this.getQueryid()).append("' (").append(rowcount).append(" records)");
            sb.append("</td>");
        } else {
            int sdicount = SdcInfo.getSDICount(pkey_0, (String)this.sdcprops.get("tableid"), this.getQueryprocesssor(), this.istemplatable);
            sb.append("<td width='25%' onClick='fnPrevRecords();'");
            if (this.getSp().equals("1")) {
                sb.append(" DISABLED");
            } else {
                sb.append(" style='cursor: pointer;' title='Get Last ").append(100).append(" Records.'");
            }
            sb.append(">Last ").append(100).append("</td>");
            sb.append("<td width='50%' align='center'>Showing ");
            if (sdicount <= 100) {
                sb.append("All (").append(rowcount).append(")");
            } else {
                sb.append(this.getSp() + " to " + (Integer.parseInt(this.getSp()) + rowcount - 1));
                if (showtotal) {
                    sb.append(" from ").append(sdicount);
                }
            }
            sb.append(" records.");
            sb.append("</td>");
            sb.append("<td align=right width='25%' onClick='fnNextRecords();'");
            if (rowcount < 100) {
                sb.append(" DISABLED");
            } else {
                sb.append(" style='cursor: pointer;' title='Get Next ").append(100).append(" Records.'");
            }
            sb.append(">Next ").append(100).append("</td>");
        }
        sb.append("</tr></table></div>");
        return sb.toString();
    }

    private String getRecordSelect(int sdicount) {
        StringBuffer sb = new StringBuffer();
        int loopcount = sdicount / 100;
        sb.append("<select id='recordselect'>");
        for (int i = 0; i < loopcount; ++i) {
        }
        return sb.toString();
    }

    protected String getMainTable(DataSet dataset, int keycolcount, String pkey_0, String pkey_1, String pkey_2) {
        StringBuffer sb = new StringBuffer();
        String inputtype = "checkbox";
        String selectfunction = "opal_selectRow";
        if (!this.MULTISELECTFLAG) {
            inputtype = "checkbox";
            selectfunction = "opal_selectIRow";
        }
        sb.append("<table border=1 cellpadding=2 cellspacing=0 width='100%' id='main'>");
        for (int i = 0; i < dataset.getRowCount(); ++i) {
            sb.append("<tr height=8 class='row_default_").append(this.getAppearance()).append("' id='main_row_").append(i).append("'>");
            if (keycolcount == 3) {
                sb.append("<td align=center width=30 class='maintform_field_").append(this.getAppearance()).append("'>");
                sb.append("<input type=").append(inputtype).append(" name='main_selector' onClick=\"").append(selectfunction).append("(this, 'main', '").append(this.getAppearance()).append("');\" value='").append(dataset.getValue(i, pkey_0)).append("|").append(dataset.getValue(i, pkey_1)).append("|").append(dataset.getValue(i, pkey_2)).append("'>");
                sb.append("</td>");
                sb.append("<td width=150 class='maintform_field_").append(this.getAppearance()).append("'>");
                sb.append(dataset.getValue(i, pkey_0));
                sb.append("</td>");
                sb.append("<td width=150 class='maintform_field_").append(this.getAppearance()).append("'>");
                sb.append(dataset.getValue(i, pkey_1));
                sb.append("</td>");
                sb.append("<td width='*' class='maintform_field_").append(this.getAppearance()).append("'>");
                sb.append(dataset.getValue(i, pkey_2));
                sb.append("</td>");
            } else if (keycolcount == 2) {
                sb.append("<td align=center width=30 class='maintform_field_").append(this.getAppearance()).append("'>");
                sb.append("<input type=").append(inputtype).append(" name='main_selector' onClick=\"").append(selectfunction).append("(this, 'main', '").append(this.getAppearance()).append("');\" value='").append(dataset.getValue(i, pkey_0)).append("|").append(dataset.getValue(i, pkey_1)).append("'>");
                sb.append("</td>");
                sb.append("<td width=150 class='maintform_field_").append(this.getAppearance()).append("'>");
                sb.append(dataset.getValue(i, pkey_0));
                sb.append("</td>");
                sb.append("<td width='*' class='maintform_field_").append(this.getAppearance()).append("'>");
                sb.append(dataset.getValue(i, pkey_1));
                sb.append("</td>");
            } else {
                sb.append("<td align=center width=30 class='maintform_field_").append(this.getAppearance()).append("'>");
                sb.append("<input type=").append(inputtype).append(" name='main_selector' onClick=\"").append(selectfunction).append("(this, 'main', '").append(this.getAppearance()).append("');\" value='").append(dataset.getValue(i, pkey_0)).append("'>");
                sb.append("</td>");
                sb.append("<td width=150 class='maintform_field_").append(this.getAppearance()).append("'>");
                sb.append(dataset.getValue(i, pkey_0));
                sb.append("</td>");
                sb.append("<td width='*' class='maintform_field_").append(this.getAppearance()).append("'>");
                sb.append(dataset.getValue(i, this.getDesccolumn()));
                sb.append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");
        if (dataset.getRowCount() == 0) {
            sb.append("<table cellpadding=2 cellspacing=0 border=1 width='100%'>");
            sb.append("<tr><td class='maintform_field_").append(this.getAppearance()).append("'>");
            sb.append("No Records found.");
            sb.append("</td></tr></table>");
        }
        sb.append("<table cellpadding=0 cellspacing=0 border=0 width='100%' id='main_loading' style='display:none;'>");
        sb.append("<tr><td width=100% height=50 align=center>");
        sb.append("<img src='WEB-OPAL/pagetypes/manage/images/loading.gif' title='Loading...'></td></tr></table>");
        return sb.toString();
    }

    protected String getSQL(String pkey_0, String pkey_1, String pkey_2, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        boolean whereflag = false;
        if (this.getQueryid().length() > 0) {
            sql.append(this.getResolvedQuery(pkey_0, pkey_1, pkey_2));
            if (this.istemplatable) {
                if (this.WHERECLAUSEFLAG) {
                    sql.append(" AND TEMPLATEFLAG != 'Y' ");
                } else {
                    sql.append(" WHERE TEMPLATEFLAG != 'Y' ");
                }
                whereflag = true;
            }
            if (this.isaccesscontrolled && !this.getSysuserid().equals("(system)")) {
                if (this.WHERECLAUSEFLAG || whereflag) {
                    sql.append(" AND ");
                } else {
                    sql.append(" WHERE ");
                }
                sql.append(pkey_0).append(" IN (SELECT T4.KEYID1 FROM SDIROLE T4 WHERE T4.SDCID = ").append(safeSQL.addVar(this.getSdcid())).append(" AND T4.PRIVID = 'list' AND T4.ROLEID IN ").append("(SELECT T5.ROLEID FROM SYSUSERROLE T5 WHERE T5.SYSUSERID = ").append(safeSQL.addVar(this.getSysuserid())).append("))");
            }
            if (!this.ORDERBYCLAUSE) {
                sql.append(" ORDER BY ").append(pkey_0).append(", ").append(pkey_1).append(", ").append(pkey_2);
            }
        } else {
            sql.append("SELECT ").append(pkey_0).append(", ").append(pkey_1).append(", ").append(pkey_2).append(" FROM ").append(this.getSdctableid());
            if (this.istemplatable) {
                sql.append(" WHERE TEMPLATEFLAG != 'Y' ");
                whereflag = true;
            }
            if (this.isaccesscontrolled && !this.getSysuserid().equals("(system)")) {
                if (whereflag) {
                    sql.append(" AND ");
                } else {
                    sql.append(" WHERE ");
                }
                sql.append(pkey_0).append(" IN (SELECT KEYID1 FROM SDIROLE WHERE SDCID = ").append(safeSQL.addVar(this.getSdcid())).append(" AND PRIVID = 'list'  AND ROLEID IN ").append("(SELECT ROLEID FROM SYSUSERROLE WHERE SYSUSERID = ").append(safeSQL.addVar(this.getSysuserid())).append("))");
            }
            sql.append(" ORDER BY ").append(pkey_0).append(", ").append(pkey_1).append(", ").append(pkey_2);
        }
        return sql.toString();
    }

    protected String getSQL(String pkey_0, String pkey_1, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        boolean whereflag = false;
        if (this.getQueryid().length() > 0) {
            sql.append(this.getResolvedQuery(pkey_0, pkey_1));
            if (this.istemplatable) {
                if (this.WHERECLAUSEFLAG) {
                    sql.append(" AND TEMPLATEFLAG != 'Y' ");
                } else {
                    sql.append(" WHERE TEMPLATEFLAG != 'Y' ");
                }
                whereflag = true;
            }
            if (this.isaccesscontrolled && !this.getSysuserid().equals("(system)")) {
                if (this.WHERECLAUSEFLAG || whereflag) {
                    sql.append(" AND ");
                } else {
                    sql.append(" WHERE ");
                }
                sql.append(pkey_0).append(" IN (SELECT T4.KEYID1 FROM SDIROLE T4 WHERE T4.SDCID = ").append(safeSQL.addVar(this.getSdcid())).append(" AND T4.PRIVID = 'list' AND T4.ROLEID IN ").append("(SELECT T5.ROLEID FROM SYSUSERROLE T5 WHERE T5.SYSUSERID = ").append(safeSQL.addVar(this.getSysuserid())).append("))");
            }
            if (!this.ORDERBYCLAUSE) {
                sql.append(" ORDER BY ").append(pkey_0).append(", ").append(pkey_1);
            }
        } else {
            sql.append("SELECT ").append(pkey_0).append(", ").append(pkey_1).append(" FROM ").append(this.getSdctableid());
            if (this.istemplatable) {
                sql.append(" WHERE TEMPLATEFLAG != 'Y' ");
                whereflag = true;
            }
            if (this.isaccesscontrolled && !this.getSysuserid().equals("(system)")) {
                if (whereflag) {
                    sql.append(" AND ");
                } else {
                    sql.append(" WHERE ");
                }
                sql.append(pkey_0).append(" IN (SELECT KEYID1 FROM SDIROLE WHERE SDCID = ").append(safeSQL.addVar(this.getSdcid())).append(" AND PRIVID = 'list'  AND ROLEID IN ").append("(SELECT ROLEID FROM SYSUSERROLE WHERE SYSUSERID = ").append(safeSQL.addVar(this.getSysuserid())).append("))");
            }
            sql.append(" ORDER BY ").append(pkey_0).append(", ").append(pkey_1);
        }
        return sql.toString();
    }

    protected String getSQL(String pkey_0, SafeSQL safeSQL) {
        StringBuffer sql = new StringBuffer();
        this.setDesccolumn((String)this.pageinfo.getSDCProcessor().getSDCProperties(this.getSdcid()).get("desccol"));
        boolean whereflag = false;
        if (this.getQueryid().length() > 0) {
            sql.append(this.getResolvedQuery(pkey_0, this.getDesccolumn()));
            if (this.istemplatable) {
                if (this.WHERECLAUSEFLAG) {
                    sql.append(" AND TEMPLATEFLAG != 'Y' ");
                } else {
                    sql.append(" WHERE TEMPLATEFLAG != 'Y' ");
                }
                whereflag = true;
            }
            if (this.isaccesscontrolled && !this.getSysuserid().equals("(system)")) {
                if (this.WHERECLAUSEFLAG || whereflag) {
                    sql.append(" AND ");
                } else {
                    sql.append(" WHERE ");
                }
                sql.append(pkey_0).append(" IN (SELECT T4.KEYID1 FROM SDIROLE T4 WHERE T4.SDCID = ").append(safeSQL.addVar(this.getSdcid())).append(" AND T4.PRIVID = 'list' AND T4.ROLEID IN ").append("(SELECT T5.ROLEID FROM SYSUSERROLE T5 WHERE T5.SYSUSERID = ").append(safeSQL.addVar(this.getSysuserid())).append("))");
            }
            if (!this.ORDERBYCLAUSE) {
                sql.append(" ORDER BY ").append(pkey_0);
            }
        } else {
            sql.append("SELECT ").append(pkey_0).append(", ").append(this.getDesccolumn()).append(" FROM (");
            sql.append("SELECT ROW_NUMBER() OVER (ORDER BY ").append(pkey_0).append(") AS ROW_NUM, ").append(pkey_0).append(", nvl(").append(this.getDesccolumn()).append(", '&nbsp;') ").append(this.getDesccolumn()).append(" FROM ").append(this.getSdctableid());
            if (this.istemplatable) {
                sql.append(" WHERE TEMPLATEFLAG != 'Y' ");
                whereflag = true;
            }
            if (this.isaccesscontrolled && !this.getSysuserid().equals("(system)")) {
                if (whereflag) {
                    sql.append(" AND ");
                } else {
                    sql.append(" WHERE ");
                }
                sql.append(pkey_0).append(" IN (SELECT T4.KEYID1 FROM SDIROLE T4 WHERE T4.SDCID = ").append(safeSQL.addVar(this.getSdcid())).append(" AND T4.PRIVID = 'list' AND T4.ROLEID IN ").append("(SELECT T5.ROLEID FROM SYSUSERROLE T5 WHERE T5.SYSUSERID = ").append(safeSQL.addVar(this.getSysuserid())).append("))");
            }
            sql.append(") WHERE ROW_NUM BETWEEN ").append(safeSQL.addVar(this.getSp())).append(" AND ").append(safeSQL.addVar(Integer.parseInt(this.getSp()) + 100 - 1));
        }
        return sql.toString();
    }

    public String getSysuserid() {
        return this.sysuserid;
    }

    public void setSysuserid(String sysuserid) {
        this.sysuserid = sysuserid;
    }

    public String getSdctableid() {
        return this.sdctableid;
    }

    public void setSdctableid(String sdctableid) {
        if (sdctableid.length() > 0) {
            this.sdctableid = sdctableid;
        } else {
            sdctableid = "void";
        }
    }

    public String getDesccolumn() {
        return this.desccolumn;
    }

    public void setDesccolumn(String desccolumn) {
        this.desccolumn = desccolumn;
    }

    public String getSp() {
        return this.sp;
    }

    public void setSp(String sp) {
        this.sp = sp;
    }

    public String getMultiselect() {
        return this.multiselect;
    }

    public void setMultiselect(String multiselect) {
        this.multiselect = multiselect;
        if (!this.getMultiselect().equalsIgnoreCase("y")) {
            this.MULTISELECTFLAG = false;
        }
    }

    public PageContext getPagecontext() {
        return this.pagecontext;
    }

    public void setPagecontext(PageContext pagecontext) {
        this.pagecontext = pagecontext;
    }

    protected String getPagescripts() {
        StringBuffer sb = new StringBuffer();
        sb.append("<script>");
        sb.append("var header = document.getElementById( 'header' );");
        sb.append("var header_table = document.getElementById( 'header_table' );");
        sb.append("var query_div_head = document.getElementById( 'querydivhead' );");
        sb.append("var query_div = document.getElementById( 'querydiv' );");
        sb.append("var _queryvisible = false;");
        sb.append("var _queries = null;");
        sb.append("if ( query_div_head != undefined ) {");
        sb.append("    query_div_head.style.left = 0;");
        sb.append("    query_div_head.style.top = pagebody.clientHeight - 25;");
        sb.append("}");
        sb.append("function positionit() {");
        sb.append("    header.style.top = document.body.scrollTop;");
        sb.append("    header_table.style.top = document.body.scrollTop;");
        sb.append("    if ( _queryvisible ) {");
        sb.append("        query_div_head.style.top = ( pagebody.clientHeight - 75 ) + document.body.scrollTop;");
        sb.append("        query_div.style.top = ( pagebody.clientHeight - 50 ) + document.body.scrollTop;");
        sb.append("    }");
        sb.append("    else {");
        sb.append("        query_div_head.style.top = ( pagebody.clientHeight - 25 ) + document.body.scrollTop;");
        sb.append("    }");
        sb.append("}");
        sb.append("function showQuery() {");
        sb.append("    if ( _queryvisible ) {");
        sb.append("        query_div.style.display = 'none';");
        sb.append("        query_div_head.style.top = pagebody.clientHeight - 25 + document.body.scrollTop;");
        sb.append("    }");
        sb.append("    else {");
        sb.append("        query_div.style.display = 'block';");
        sb.append("        query_div.style.left = 0;");
        sb.append("        query_div.style.top = pagebody.clientHeight - 50 + document.body.scrollTop;");
        sb.append("        query_div_head.style.top = pagebody.clientHeight - 75 + document.body.scrollTop;");
        sb.append("        if ( _queryvisibleflag ) {");
        sb.append("            queryChanged();");
        sb.append("        }");
        sb.append("    }");
        sb.append("    _queryvisible = !_queryvisible;");
        sb.append("}");
        sb.append("function queryChanged() {");
        sb.append("    document.getElementById( 'queryargsid' ).innerHTML = '';");
        sb.append("    var selectedquery = document.getElementById( 'queryselect' ).value;");
        sb.append("    var inputfield = '';");
        sb.append("    for ( var x=1; x<_queries.length; x++ ) {");
        sb.append("        temp = _queries[x].split(':');");
        sb.append("        if ( temp[0] == selectedquery ) {");
        sb.append("            argid = temp[1].split(';');");
        sb.append("            for ( var y=0; y < argid.length; y++ ) {");
        sb.append("                inputfield += '<input type=text name=queryparam value = ' + argid[y] + ' size=10>&nbsp;';");
        sb.append("            }");
        sb.append("            document.getElementById( 'queryargsid' ).innerHTML = inputfield;");
        sb.append("        }");
        sb.append("    }");
        sb.append("}");
        sb.append("function fnLoading() {");
        sb.append("    document.getElementById( 'main' ).style.display = 'none';");
        sb.append("    document.getElementById( 'main_loading' ).style.display = 'block';");
        sb.append("}");
        sb.append(this.getScripts());
        sb.append("</script>");
        return sb.toString();
    }

    protected String getScripts() {
        StringBuffer sb = new StringBuffer();
        int index = 1;
        if (Integer.parseInt(this.getSp()) != 1) {
            index = Integer.parseInt(this.getSp());
        }
        sb.append("function fnNextRecords() {");
        sb.append("    fnLoading();");
        sb.append("    var url = 'rc?command=file&file=").append(JSPPATH).append("';");
        sb.append("    url += '&sdc=").append(this.getSdcid()).append("';");
        sb.append("    url += '&queryid=").append(this.getQueryid()).append("';");
        sb.append("    url += '&param1=").append(this.getParam1()).append("';");
        sb.append("    url += '&param2=").append(this.getParam2()).append("';");
        sb.append("    url += '&param3=").append(this.getParam3()).append("';");
        sb.append("    url += '&param4=").append(this.getParam4()).append("';");
        sb.append("    url += '&param5=").append(this.getParam5()).append("';");
        sb.append("    url += '&param6=").append(this.getParam6()).append("';");
        sb.append("    url += '&param7=").append(this.getParam7()).append("';");
        sb.append("    url += '&param8=").append(this.getParam8()).append("';");
        sb.append("    url += '&param9=").append(this.getParam9()).append("';");
        sb.append("    url += '&param10=").append(this.getParam10()).append("';");
        sb.append("    url += '&param11=").append(this.getParam11()).append("';");
        sb.append("    url += '&param12=").append(this.getParam12()).append("';");
        sb.append("    url += '&sp=").append(index + 100).append("';");
        sb.append("    url += '&function=").append(this.getFunction()).append("';");
        sb.append("    url += '&fieldid=").append(this.getFieldid()).append("';");
        sb.append("    url += '&field=").append(this.getField()).append("';");
        sb.append("    url += '&index=").append(this.getIndex()).append("';");
        sb.append("    url += '&appearance=").append(this.getAppearance()).append("';");
        sb.append("    window.location = url;");
        sb.append("}");
        sb.append("function fnPrevRecords() {");
        sb.append("    fnLoading();");
        sb.append("    var url = 'rc?command=file&file=").append(JSPPATH).append("';");
        sb.append("    url += '&sdc=").append(this.getSdcid()).append("';");
        sb.append("    url += '&appearance=").append(this.getAppearance()).append("';");
        sb.append("    url += '&queryid=").append(this.getQueryid()).append("';");
        sb.append("    url += '&param1=").append(this.getParam1()).append("';");
        sb.append("    url += '&param2=").append(this.getParam2()).append("';");
        sb.append("    url += '&param3=").append(this.getParam3()).append("';");
        sb.append("    url += '&param4=").append(this.getParam4()).append("';");
        sb.append("    url += '&param5=").append(this.getParam5()).append("';");
        sb.append("    url += '&param6=").append(this.getParam6()).append("';");
        sb.append("    url += '&param7=").append(this.getParam7()).append("';");
        sb.append("    url += '&param8=").append(this.getParam8()).append("';");
        sb.append("    url += '&param9=").append(this.getParam9()).append("';");
        sb.append("    url += '&param10=").append(this.getParam10()).append("';");
        sb.append("    url += '&param11=").append(this.getParam11()).append("';");
        sb.append("    url += '&param12=").append(this.getParam12()).append("';");
        sb.append("    url += '&sp=").append(index - 100).append("';");
        sb.append("    url += '&function=").append(this.getFunction()).append("';");
        sb.append("    url += '&fieldid=").append(this.getFieldid()).append("';");
        sb.append("    url += '&field=").append(this.getField()).append("';");
        sb.append("    url += '&index=").append(this.getIndex()).append("';");
        sb.append("    url += '&appearance=").append(this.getAppearance()).append("';");
        sb.append("    window.location = url;");
        sb.append("}");
        sb.append("function runQuery() {").append("fnLoading();").append("var paramfield = document.getElementsByName( 'queryparam' ); ").append("var url = 'rc?command=file&file=").append(JSPPATH).append("'; ").append("url += '&sdc=").append(this.getSdcid()).append("'; ").append("url += '&appearance=").append(this.getAppearance()).append("'; ").append("url += '&function=").append(this.getFunction()).append("'; ").append("url += '&fieldid=").append(this.getFieldid()).append("'; ").append("url += '&field=").append(this.getField()).append("'; ").append("url += '&index=").append(this.getIndex()).append("'; ").append("url += '&queryid=' + document.getElementById( 'queryselect' ).value; ").append("for ( var x=0; x < paramfield.length; x++ ) { ").append("    if ( x==0 ) { ").append("        url += '&param1=' + paramfield[x].value; ").append("    } ").append("    if ( x==1 ) { ").append("        url += '&param2=' + paramfield[x].value; ").append("    } ").append("    if ( x==3 ) { ").append("        url += '&param3=' + paramfield[x].value; ").append("    } ").append("    if ( x==4 ) { ").append("        url += '&param4=' + paramfield[x].value; ").append("    } ").append("    if ( x==5 ) { ").append("        url += '&param5=' + paramfield[x].value; ").append("    } ").append("    if ( x==6 ) { ").append("        url += '&param6=' + paramfield[x].value; ").append("    } ").append("    if ( x==7 ) { ").append("        url += '&param7=' + paramfield[x].value; ").append("    } ").append("    if ( x==8 ) { ").append("        url += '&param8=' + paramfield[x].value; ").append("    } ").append("    if ( x==9 ) { ").append("        url += '&param9=' + paramfield[x].value; ").append("    } ").append("    if ( x==10 ) { ").append("        url += '&param10=' + paramfield[x].value; ").append("    } ").append("    if ( x==11 ) { ").append("        url += '&param11=' + paramfield[x].value; ").append("    } ").append("    if ( x==12 ) { ").append("        url += '&param12=' + paramfield[x].value; ").append("    } ").append("} ").append("window.location = url; ").append("} ");
        return sb.toString();
    }

    public String getFieldid() {
        return this.fieldid;
    }

    public void setFieldid(String fieldid) {
        this.fieldid = fieldid;
    }

    public String getField() {
        return this.field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getIndex() {
        return this.index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    protected String getErrorHeader() {
        StringBuffer sb = new StringBuffer();
        sb.append("<table cellpadding=2 cellspacing=0 border=0 width=100% bgColor=#CCCCCC><tr>").append("<td height=50 valign=top>").append("<font face=verdana size=2><b>Lookup ").append(this.getSdcid()).append("(s)").append("</b></font></td>").append("<td align=right valign=top>").append("<input type=button value='Close' style='border:1px solid black;'").append(" onClick='window.close();'>").append("</tr></table>");
        return sb.toString();
    }
}

