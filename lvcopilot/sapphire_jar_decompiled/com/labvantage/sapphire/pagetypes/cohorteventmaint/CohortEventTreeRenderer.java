/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pagetypes.cohorteventmaint;

import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class CohortEventTreeRenderer {
    public static String drawEventTree(String cohortid, String cpid, String cprevision, String cpversionid, PageContext pageContext, PropertyList pageLabels, QueryProcessor qp) {
        boolean subExpanded = false;
        StringBuffer output = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT s_eventdefid, eventdefdesc, eventdeflabel, eventdeftype FROM s_eventdef WHERE clinicalprotocolid = " + safeSQL.addVar(cpid) + " AND clinicalprotocolversionid =" + safeSQL.addVar(cpversionid) + " AND clinicalprotocolrevision = " + safeSQL.addVar(cprevision) + " AND cohortid = " + safeSQL.addVar(cohortid) + "AND (parenteventdefid is null or parenteventdefid = '') ORDER BY usersequence";
        DataSet parentEventds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        Logger.logDebug("Retrieving parent events/visits for the protocol...Data set size is " + parentEventds.getRowCount());
        if (parentEventds.getRowCount() > 0) {
            output.append("<table id=\"nodetree\" class=\"navigator\" cellpadding=\"0\" cellspacing=\"0\"><font size=\"2pt\">");
            for (int i = 0; i < parentEventds.getRowCount(); ++i) {
                Logger.logDebug("Rendering events/visits...Visits Count : " + parentEventds.getRowCount() + "for cohort - " + cohortid + " and for clinical protocol - " + cpid);
                String eventid = parentEventds.getValue(i, "s_eventdefid");
                String eventlabel = parentEventds.getValue(i, "eventdeflabel");
                String eventtype = parentEventds.getValue(i, "eventdeftype");
                String title = eventtype + ": " + eventlabel;
                output.append("<tr width=100% height=\"20\" >");
                output.append("<input type=\"hidden\" name=\"eventtype_" + eventid + "\"  id=\"eventtype_" + eventid + "\" value=\"" + eventtype + "\" >");
                output.append("<td width=\"18\">");
                if (CohortEventTreeRenderer.getChildNodesCount(eventid, qp) > 0) {
                    output.append("<span id=\"row_").append(eventid).append("\" onclick=\"cohorteventmaint.showNodes( this, '").append(eventid).append("')\"><img src=\"WEB-CORE/pagetypes/cohorteventmaint/images/").append(subExpanded ? "minus" : "plus").append(".gif\"></span>");
                }
                output.append("&nbsp;&nbsp;</td>");
                output.append("<td class=\"node\" ").append("onmouseover=\"this.className='nodeselected';\"  ").append(" onmouseout=\"this.className='node';\"").append(" nowrap width=\"100%\" onClick=\"cohorteventmaint.showNodeMaintenance( '").append(eventid).append("','").append(title).append("', '").append(eventtype).append("' );sapphire.events.cancelEvent(event, false);\">");
                output.append("<span id=\"label__").append(eventid).append("\">");
                output.append("<label for=\"title").append("__").append(title).append("\">").append("</label>");
                output.append("<label for=\"label").append("__").append(eventid).append("\">").append(eventlabel).append("</label>");
                output.append("</span>");
                CohortEventTreeRenderer.displaySingleLineSummary(eventid, pageContext, output, true, cpid, cprevision, cpversionid, pageLabels, qp);
                output.append("</td>");
                output.append("</tr>");
                CohortEventTreeRenderer.displayChildEvents(eventid, cohortid, cpid, cprevision, cpversionid, pageContext, output, pageLabels, qp);
            }
            output.append("</font></table>");
        }
        return output.toString();
    }

    private static StringBuffer displayChildEvents(String eventid, String cohortid, String cpid, String cprevision, String cpversionid, PageContext pageContext, StringBuffer output, PropertyList pageLabels, QueryProcessor qp) {
        boolean subExpanded = false;
        boolean islastTPChild = false;
        String sql = "SELECT s_eventdefid, eventdefdesc, eventdeflabel, eventdeftype FROM s_eventdef WHERE clinicalprotocolid = ? AND clinicalprotocolversionid = ?  AND clinicalprotocolrevision = ? AND cohortid = ?  AND parenteventdefid = ? ORDER BY usersequence";
        DataSet ds = qp.getPreparedSqlDataSet(sql, (Object[])new String[]{cpid, cpversionid, cprevision, cohortid, eventid});
        if (ds.getRowCount() > 0) {
            Logger.logDebug("Timepoints Count : " + ds.getRowCount());
            output.append("<div id=\"div_").append(eventid).append("\">");
            output.append("<tr style=\"display:none\"  id=\"row_").append(eventid).append("TP\">");
            output.append("<td width=\"18\"></td><td>");
            output.append("<table cellspacing=\"0\" cellpadding=\"0\">");
            for (int i = 0; i < ds.getRowCount(); ++i) {
                output.append("<tr height=\"16\">");
                String childid = ds.getValue(i, "s_eventdefid");
                String eventlabel = ds.getValue(i, "eventdeflabel");
                String eventtype = ds.getValue(i, "eventdeftype");
                String title = eventtype + ": " + eventlabel;
                output.append("<input type=\"hidden\" name=\"eventtype_" + childid + "\"  id=\"eventtype_" + childid + "\" value=\"" + eventtype + "\" >");
                if (i == ds.getRowCount() - 1) {
                    output.append("<td align=\"left\" width=\"18\"><img width=\"18\" src=\"WEB-CORE/pagetypes/cohorteventmaint/images/menu_corner.gif\"></td>");
                    islastTPChild = true;
                } else {
                    output.append("<td align=\"left\" style=\"background: url('WEB-CORE/pagetypes/cohorteventmaint/images/menu_tee.gif') no-repeat;\" width=\"18\"></td>");
                }
                output.append("<td width=\"18\">");
                if (CohortEventTreeRenderer.getChildNodesCount(childid, qp) > 0) {
                    output.append("<td width=\"18\">&nbsp;&nbsp;&nbsp;&nbsp;</td>");
                    output.append("<span id=\"row_").append(childid).append("\" onclick=\"cohorteventmaint.showNodes( this, '").append(childid).append("')\"><img src=\"WEB-CORE/pagetypes/cohorteventmaint/images/").append(subExpanded ? "minus" : "plus").append(".gif\"></span>");
                }
                output.append("</td>");
                output.append("<td width=\"18\">&nbsp;&nbsp;</td>");
                output.append("<td class=\"node\" ").append("onmouseover=\"this.className='nodeselected';\"  ").append(" onmouseout=\"this.className='node';\"").append("nowrap width=\"100%\" onClick=\"cohorteventmaint.showNodeMaintenance( '").append(childid).append("','").append(title).append("', '").append(eventtype).append("' );sapphire.events.cancelEvent(event, false);\">");
                output.append("<span id=\"label__").append(childid).append("\">");
                output.append("<label for=\"title").append("__").append(title).append("\">").append("</label>");
                output.append("<label for=\"label").append("__").append(childid).append("\">").append(eventlabel).append("</label>");
                output.append("</span>");
                CohortEventTreeRenderer.displaySingleLineSummary(childid, pageContext, output, false, cpid, cprevision, cpversionid, pageLabels, qp);
                output.append("</td>");
                output.append("</tr>");
            }
            output.append("</table></td>");
            output.append("</tr>");
            output.append("</div>");
        }
        return output;
    }

    private static int getChildNodesCount(String eventid, QueryProcessor qp) {
        int count = 0;
        String sql = "SELECT s_eventdefid FROM s_eventdef WHERE  parenteventdefid = ?";
        DataSet ds = qp.getPreparedSqlDataSet(sql, (Object[])new String[]{eventid});
        count = ds.getRowCount();
        return count;
    }

    private static int getTimePointCount(String eventid, QueryProcessor qp) {
        int count = 0;
        String sql = "SELECT s_eventdefid FROM s_eventdef WHERE  parenteventdefid = ?";
        DataSet ds = qp.getPreparedSqlDataSet(sql, (Object[])new String[]{eventid});
        count = ds.getRowCount();
        return count;
    }

    private static StringBuffer displaySingleLineSummary(String eventid, PageContext pageContext, StringBuffer output, boolean showTimePoints, String cpid, String cprevision, String cpversionid, PropertyList pageLabels, QueryProcessor qp) {
        RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        String showSSL = userConfig.getProperty("showSSL");
        String summaryText = "";
        summaryText = showTimePoints ? "[" + pageLabels.getProperty("Timepoints") + ":" + CohortEventTreeRenderer.getTimePointCount(eventid, qp) + ", " : "[";
        summaryText = summaryText + pageLabels.getProperty("Assay Types") + ":" + CohortEventTreeRenderer.getAssayTypeDSCount(eventid, cpid, cprevision, cpversionid, qp) + ", " + pageLabels.getProperty("Sample Types") + ":" + CohortEventTreeRenderer.getSampleTypeDSCount(eventid, cpid, cprevision, cpversionid, qp) + "]";
        if (showSSL.equals("false")) {
            output.append("<span style=\"visibility:hidden\"  id=\"colSLS_").append(eventid).append("\">");
        } else {
            Logger.logDebug("Rendering Single Line Summary...");
            output.append("<span style=\"visibility:visible\"  id=\"colSLS_").append(eventid).append("\">");
        }
        output.append("&nbsp;").append(summaryText);
        output.append("</span>");
        return output;
    }

    private static int getSampleTypeDSCount(String eventid, String cpid, String cprevision, String cpversionid, QueryProcessor qp) {
        String sql = "SELECT edst.s_sampletypeid  FROM s_eventdefsampletype edst, s_sampletype st, s_cpsampletype cpst WHERE  edst.s_eventdefid = ? AND  edst.s_sampletypeid = st.s_sampletypeid AND  st.s_sampletypeid = cpst.s_sampletypeid AND  cpst.s_clinicalprotocolid = ? AND  cpst.s_clinicalprotocolversionid = ? AND  cpst.s_clinicalprotocolrevision = ?";
        DataSet ds = qp.getPreparedSqlDataSet(sql, (Object[])new String[]{eventid, cpid, cpversionid, cprevision});
        return ds.getRowCount();
    }

    private static int getAssayTypeDSCount(String eventid, String cpid, String cprevision, String cpversionid, QueryProcessor qp) {
        String sql4AT = "SELECT distinct edstat.s_eventdefid  FROM s_eventdefstatmap edstat, s_eventdefsampletype edst, s_cpassaytype cpat WHERE  edstat.s_eventdefid =?  AND  edstat.s_sampletypeid = edst.s_sampletypeid AND edstat.s_assaytypeid = cpat.s_assaytypeid AND  cpat.s_clinicalprotocolid = ? AND  cpat.s_clinicalprotocolversionid = ? AND  cpat.s_clinicalprotocolrevision = ?";
        DataSet ds4AT = qp.getPreparedSqlDataSet(sql4AT, (Object[])new String[]{eventid, cpid, cpversionid, cprevision});
        return ds4AT.getRowCount();
    }

    public static String[] getallEvents(String cohortid, String cpid, String cprevision, String cpversionid, QueryProcessor qp) {
        String sql = "SELECT s_eventdefid, eventdefdesc, eventdeflabel FROM s_eventdef WHERE clinicalprotocolid = ? AND clinicalprotocolversionid = ?  AND clinicalprotocolrevision = ? AND cohortid = ? ";
        DataSet ds = qp.getPreparedSqlDataSet(sql, (Object[])new String[]{cpid, cpversionid, cprevision, cohortid});
        String[] allevents = new String[ds.getRowCount()];
        for (int i = 0; i < ds.getRowCount(); ++i) {
            allevents[i] = ds.getValue(i, "s_eventdefid");
        }
        return allevents;
    }

    public static String[] getParentEvents(String cohortid, String cpid, String cprevision, String cpversionid, QueryProcessor qp) {
        String sql = "SELECT distinct parenteventdefid FROM s_eventdef WHERE clinicalprotocolid = ? AND clinicalprotocolversionid = ?  AND clinicalprotocolrevision = ? AND cohortid = ? AND parenteventdefid is not null";
        DataSet ds = qp.getPreparedSqlDataSet(sql, (Object[])new String[]{cpid, cpversionid, cprevision, cohortid});
        String[] allevents = new String[ds.getRowCount()];
        for (int i = 0; i < ds.getRowCount(); ++i) {
            allevents[i] = ds.getValue(i, "parenteventdefid");
        }
        return allevents;
    }
}

