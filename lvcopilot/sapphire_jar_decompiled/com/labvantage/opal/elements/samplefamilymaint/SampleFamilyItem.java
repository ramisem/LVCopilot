/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.samplefamilymaint;

import com.labvantage.opal.elements.detailmaint.BaseItem;
import com.labvantage.opal.handler.ErrorUtil;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;

public class SampleFamilyItem
extends BaseItem {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 77321 $";
    private QueryProcessor queryProcessor;
    private String smsSampleViewPageURL = null;

    @Override
    public String getHtml() {
        StringBuilder html = new StringBuilder();
        this.setSdcid(this.requestContext.getProperty("sdcid"));
        this.setKeyid1(this.requestContext.getProperty("keyid1"));
        this.setKeyid2(this.requestContext.getProperty("keyid2"));
        this.setKeyid3(this.requestContext.getProperty("keyid3"));
        if (this.smsSampleViewPageURL == null || this.smsSampleViewPageURL.length() == 0) {
            this.smsSampleViewPageURL = this.element.getProperty("smssampleviewpageurl");
        }
        if (this.getSdcid() == null || this.getKeyid1().equals("(null)")) {
            return SampleFamilyItem.toErrorString("Data Error", "No information found for Master in Request.");
        }
        this.initElement();
        html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">").append("<tr align=\"left\" valign=\"middle\"><td colspan=\"2\">").append(this.getMainHtml()).append("</td></tr>").append("</table>");
        return html.toString();
    }

    @Override
    protected String getMainHtml() {
        DataSet ds;
        StringBuilder samplestatuschild = new StringBuilder();
        StringBuilder samplestatus = new StringBuilder();
        StringBuilder outputString = new StringBuilder();
        try {
            ds = this.getDataSet();
        }
        catch (SapphireException e) {
            return SampleFamilyItem.toErrorString("Data Error", e.getMessage());
        }
        String parentstudyid = "";
        for (int i = 0; i < ds.size(); ++i) {
            String sampleId = ds.getValue(i, "s_sampleid");
            DataSet childDS = this.getHierarchyDataSet(sampleId);
            String initial = "";
            for (int j = 0; j < childDS.getRowCount(); ++j) {
                if (!childDS.getValue(j, "level").equals("1") || childDS.getValue(j, "srcsampleid").trim().equals(sampleId.trim())) continue;
                childDS.deleteRow(j);
            }
            childDS.trimToSize();
            samplestatus.setLength(0);
            if (ds.getValue(i, "glp").equalsIgnoreCase("Y")) {
                samplestatus.append(" <img src='WEB-CORE/images/gif/GLP.gif' /> ; ");
            }
            samplestatus.append(ds.getValue(i, "status"));
            samplestatus.append(" ; ").append(ds.getValue(i, "cdomain"));
            samplestatus.append(" ; ").append(ds.getValue(i, "sampletype"));
            samplestatus.append("[").append(ds.getValue(i, "preptype")).append("]");
            parentstudyid = ds.getValue(i, "sstudyid");
            outputString.setLength(0);
            if (this.getSMSSampleViewPageURL() != null) {
                outputString.append("<BR><a href=\"").append(this.getSMSSampleViewPageURL()).append("&keyid1=").append(HttpUtil.encodeURIComponent(ds.getValue(i, "s_sampleid").trim())).append("&sdcid=Sample&mode=View\" target=\"View\" title=\"Show Details for ").append(ds.getValue(i, "s_sampleid").trim()).append("\">").append(ds.getValue(i, "s_sampleid").trim()).append("</a> ( ").append((CharSequence)samplestatus).append(" ) ");
            } else {
                outputString.append("<BR>").append(ds.getValue(i, "s_sampleid").trim()).append(" ( ").append((CharSequence)samplestatus).append(" ) ");
            }
            for (int k = 0; k < childDS.getRowCount(); ++k) {
                String childstudyid;
                String italicEnd;
                String italicStart;
                String outvalue = childDS.getValue(k, "outvalue");
                initial = childDS.getValue(k, "level").equals("1") ? "&nbsp;&nbsp;&nbsp;&nbsp;" : "";
                if (outvalue.trim().equals(this.getKeyid1())) {
                    italicStart = "<I><B>";
                    italicEnd = "</B></I>";
                } else {
                    italicStart = "";
                    italicEnd = "";
                }
                samplestatuschild.setLength(0);
                if (childDS.getValue(k, "glp").equalsIgnoreCase("Y")) {
                    samplestatuschild.append(" <img src='WEB-CORE/images/gif/GLP.gif' /> ; ");
                }
                if (!(childstudyid = childDS.getValue(k, "sstudyid")).equals(parentstudyid)) {
                    samplestatuschild.append("<b>").append(childstudyid).append("</b> ; ");
                }
                samplestatuschild.append(childDS.getValue(k, "status"));
                samplestatuschild.append(" ; ").append(childDS.getValue(k, "cdomain"));
                samplestatuschild.append(" ; ").append(childDS.getValue(k, "sampletype"));
                samplestatuschild.append("[").append(childDS.getValue(k, "preptype")).append("]");
                outputString.append("<br>");
                outputString.append(initial).append(italicStart);
                outputString.append(this.SetURL(outvalue)).append(" ( ").append((CharSequence)samplestatuschild).append(" )");
                outputString.append(italicEnd);
            }
        }
        return outputString.toString();
    }

    protected String SetURL(String text) {
        String sampleid = text.trim();
        String link = this.getSMSSampleViewPageURL() != null ? "<a href=\"" + this.getSMSSampleViewPageURL() + "&keyid1=" + HttpUtil.encodeURIComponent(sampleid) + "&sdcid=Sample&mode=View\" target=\"View\" title=\"Show Details for " + sampleid + "\">" + sampleid + "</a>" : sampleid;
        int index = text.indexOf(sampleid);
        if (index != -1 && index > 0) {
            link = StringUtil.repeat("&nbsp;&nbsp;", index) + link;
        }
        return link;
    }

    protected DataSet getDataSet() throws SapphireException {
        StringBuilder sql = new StringBuilder();
        sql.append("select s_sample.s_sampleid, s_sample.storagestatus status, trackitem.custodialdepartmentid cdomain, nvl(s_sample.glpflag,'N') glp,");
        sql.append(" s_preptype.preptypedesc preptype, s_sample.sampletypeid sampletype, s_sample.sstudyid");
        sql.append(" from s_sample, trackitem, s_preptype");
        sql.append(" where s_sample.s_sampleid in (select distinct sm1.sourcesampleid from s_samplemap sm1");
        sql.append("   where (select sm2.sourcesampleid srcsampleid from s_samplemap sm2 where sm1.sourcesampleid = sm2.destsampleid and rownum=1) is null");
        sql.append(" start with sm1.destsampleid = ? connect by sm1.destsampleid = prior sm1.sourcesampleid)");
        sql.append(" and trackitem.linksdcid = 'Sample'");
        sql.append(" and trackitem.linkkeyid1 = s_sample.s_sampleid");
        sql.append(" and s_sample.preptypeid = s_preptypeid(+)");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{this.getKeyid1()});
        if (ds.getRowCount() == 0) {
            sql.setLength(0);
            sql.append("select s_sample.s_sampleid,s_sample.storagestatus status, trackitem.custodialdepartmentid cdomain, nvl(s_sample.glpflag,'N') glp,");
            sql.append(" s_preptype.preptypedesc preptype, s_sample.sampletypeid sampletype, s_sample.sstudyid");
            sql.append(" from trackitem, s_sample, s_preptype");
            sql.append(" where s_sample.s_sampleid = ?");
            sql.append(" and trackitem.linksdcid = 'Sample'");
            sql.append(" and trackitem.linkkeyid1 = s_sample.s_sampleid");
            sql.append(" and s_sample.preptypeid = s_preptypeid(+)");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{this.getKeyid1()});
        }
        if (ds == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query: <br>" + sql, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        return ds;
    }

    protected DataSet getHierarchyDataSet(String sampleId) {
        StringBuilder sql = new StringBuilder();
        sql.append("select rpad('  ',4*(LEVEL-1)) || destsampleid outvalue, sourcesampleid srcsampleid, level, (select storagestatus from s_sample where s_sampleid = destsampleid) status,");
        sql.append(" (select custodialdepartmentid from trackitem where linksdcid = 'Sample' and linkkeyid1 = destsampleid) cdomain,");
        sql.append(" (select nvl (glpflag, 'N') from s_sample where s_sampleid = destsampleid) glp,");
        sql.append(" (select preptypedesc from s_sample, s_preptype where s_sampleid = destsampleid and preptypeid = s_preptypeid) preptype,");
        sql.append(" (select sstudyid from s_sample where s_sampleid = destsampleid) sstudyid,");
        sql.append(" (select s_sampletypeid from s_sample, s_sampletype where s_sampleid = destsampleid and sampletypeid = s_sampletypeid) sampletype");
        sql.append(" from s_samplemap");
        sql.append(" start with sourcesampleid = ? connect by prior destsampleid = sourcesampleid");
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{sampleId});
    }

    public void setQueryProcessor(QueryProcessor qp) {
        this.queryProcessor = qp;
    }

    @Override
    public QueryProcessor getQueryProcessor() {
        if (this.queryProcessor == null) {
            this.queryProcessor = new QueryProcessor(this.pageContext);
        }
        return this.queryProcessor;
    }

    public void setSMSSampleViewPageURL(String smsSampleViewPageURL) {
        this.smsSampleViewPageURL = smsSampleViewPageURL;
    }

    public String getSMSSampleViewPageURL() {
        if (this.smsSampleViewPageURL != null && this.smsSampleViewPageURL.trim().equals("")) {
            return null;
        }
        return this.smsSampleViewPageURL;
    }
}

