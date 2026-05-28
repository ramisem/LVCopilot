/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import javax.servlet.jsp.PageContext;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;

public class MaintAttachmentOperationExecution
extends BaseElement
implements SDMSConstants {
    private DataSet attachmentoperationexecutions = null;
    private String sdcid = "";
    private String keyid1 = "";
    private String keyid2 = "";
    private String keyid3 = "";

    public void setAttachmentOperationExecutionData(DataSet dataSet) {
        this.attachmentoperationexecutions = dataSet;
    }

    public DataSet getAttachmentOperationExecutionData() {
        return this.attachmentoperationexecutions;
    }

    public MaintAttachmentOperationExecution() {
    }

    public MaintAttachmentOperationExecution(PageContext pageContext) {
        this.setPageContext(pageContext);
    }

    public void setPrimary(String sdcid, String keyid1, String keyid2, String keyid3) {
        this.sdcid = sdcid;
        this.keyid1 = keyid1;
        this.keyid2 = keyid2;
        this.keyid3 = keyid3;
    }

    @Override
    public String getHtml() {
        this.setUpData();
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"attachmentoperationexecution_container\">");
        html.append("<div class=\"attachmentoperationexecution_list\">");
        html.append(this.getListViewSDIAttachmentOperationExecution());
        html.append("</div>");
        html.append("</div>");
        html.append(MaintAttachmentOperationExecution.getScriptAndStyle());
        return html.toString();
    }

    private void setUpData() {
        if ((this.keyid1 == null || this.keyid1.length() == 0) && this.sdiInfo != null) {
            this.keyid1 = this.sdiInfo.getSDIRequest().getKeyid1List();
        }
        if ((this.sdcid == null || this.sdcid.length() == 0) && this.sdiInfo != null) {
            this.sdcid = this.sdiInfo.getSDIRequest().getSDCid();
        }
        String sql = "SELECT e.handlerlog, e.executionstatus, e.executionid, e.createdt,  (SELECT distinct s.operationkeyid1 FROM sdiattachmentoperation s WHERE s.attachmentoperationid = e.attachmentoperationid) operationkeyid1 FROM sdiattachmentoperationexec e WHERE e.sdcid=? AND e.keyid1=? order by e.createdt,e.executionid";
        if ("LV_DataCapture".equalsIgnoreCase(this.sdcid)) {
            DataSet ds = this.getSDIProcessor().getSDIData(this.sdiInfo.getSDIRequest()).getDataset("primary");
            String instrumentid = "";
            if (ds != null && ds.getRowCount() > 0) {
                instrumentid = ds.getString(0, "instrumentid", "");
            }
            if (instrumentid.length() > 0) {
                sql = "SELECT e.handlerlog, e.executionstatus, e.executionid, e.createdt,  (SELECT distinct s.operationkeyid1 FROM sdiattachmentoperation s WHERE s.keyid1=(select instrumentid from datacapture where datacapture.datacaptureid=e.keyid1) and s.attachmentoperationid = e.attachmentoperationid) operationkeyid1 FROM sdiattachmentoperationexec e WHERE e.sdcid=? AND e.keyid1=? order by e.createdt,e.executionid";
            }
        }
        Object[] params = new Object[]{this.sdcid, this.keyid1};
        this.attachmentoperationexecutions = this.getQueryProcessor().getPreparedSqlDataSet(sql, params, true);
    }

    public static String getScriptAndStyle() {
        StringBuilder html = new StringBuilder();
        html.append("<script src=\"WEB-CORE/elements/sdms/scripts/attachmentoperationexecution.js\"></script>");
        return html.toString();
    }

    private String getListViewSDIAttachmentOperationExecution() {
        StringBuffer html = new StringBuffer();
        if (this.attachmentoperationexecutions != null && this.attachmentoperationexecutions.size() > 0) {
            html.append("<table class=\"maintform_table_blue\" border=\"1\" cellpadding=\"2\" cellspacing=\"0\" id=\"" + this.elementid + "\">");
            html.append(this.getHeaderHtml());
            html.append(this.getRowsHtml());
            html.append(" </table>");
        } else {
            html.append(this.getTranslationProcessor().translate("No Executions Found."));
        }
        return html.toString();
    }

    private String getHeaderHtml() {
        boolean hasSDMSAdminRole = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).hasRole("SDMSAdmin");
        StringBuffer headerHtml = new StringBuffer();
        String[] headers = null;
        headers = hasSDMSAdminRole ? new String[]{"ID", "Handler", "Status", "Date", "Log", "&nbsp;"} : new String[]{"ID", "Handler", "Status", "Date", "Log"};
        headerHtml.append("<thead><tr>");
        for (int i = 0; i < headers.length; ++i) {
            String title = this.getTranslationProcessor().translate(headers[i]);
            headerHtml.append("<th class=\"maintform_fieldtitle\" columnindex=\"" + i + "\" elementid=\"" + this.elementid + "\" title=\"" + title + "\"  style=\"cursor: default; width: 150px;\">");
            headerHtml.append(title);
            headerHtml.append("</th>");
        }
        headerHtml.append(" </tr></thead>");
        return headerHtml.toString();
    }

    private String getRowTD(String columnid, String value, int rowid) {
        return "<td class=\"maintform_field_blue\" id=\"cell_" + this.elementid + "_" + columnid + "_" + rowid + "\" width=\"150\" align=\"left\" style=\"white-space:pre-wrap;\">" + value + "</td>";
    }

    private String getRowsHtml() {
        StringBuffer rowHtml = new StringBuffer();
        rowHtml.append("<tbody>");
        for (int i = 0; i < this.attachmentoperationexecutions.size(); ++i) {
            String[] logLines;
            String log;
            String executionid = this.attachmentoperationexecutions.getValue(i, "executionid", "");
            String handler = this.attachmentoperationexecutions.getValue(i, "operationkeyid1", "");
            String date = this.attachmentoperationexecutions.getValue(i, "createdt", "");
            String statuse = this.attachmentoperationexecutions.getValue(i, "executionstatus", "(Processing)");
            if ((statuse = statuse.substring(0, 1).toUpperCase() + statuse.substring(1)).equalsIgnoreCase("fail")) {
                statuse = "<span style=\"color:red;\">" + statuse + "</span>";
            }
            String dis = log = this.attachmentoperationexecutions.getValue(i, "handlerlog", "");
            boolean trimmed = false;
            if (log.contains("\n")) {
                dis = log.substring(0, log.indexOf("\n"));
                trimmed = true;
            }
            if (dis.length() > 20) {
                dis = dis.substring(0, 20);
                trimmed = true;
            }
            if (trimmed) {
                dis = dis + "...";
            }
            boolean hasSDMSAdminRole = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).hasRole("SDMSAdmin");
            StringBuilder logout = new StringBuilder();
            for (String logLine : logLines = StringUtil.split(log, "\n")) {
                if (logout.length() > 0) {
                    logout.append("<br>");
                }
                logout.append("<span");
                if (logLine.indexOf("DEBUG:") == 0) {
                    logout.append(" style=\"color:grey;\"");
                } else if (logLine.indexOf("ERROR:") == 0 || logLine.indexOf("EXCEPTION:") == 0) {
                    logout.append(" style=\"color:red;\"");
                }
                if (logLine.indexOf("WARN:") == 0) {
                    logout.append(" style=\"color:orange;\"");
                }
                logout.append(">");
                logout.append(logLine);
                logout.append("</span>");
            }
            String logValue = "<span onclick=\"attachmentOperationExecution.viewLog(this)\" style=\"cursor:pointer;\" title=\"Click to view\" data-log=\"" + HttpUtil.encodeURIComponent(logout.toString()) + "\">" + dis + "</span>";
            String reExecuteValue = hasSDMSAdminRole ? "<a href=\"javascript:attachmentOperationExecution.rerunExecution('" + this.attachmentoperationexecutions.getValue(i, "executionid", "") + "');\">Re-execute</a>" : "";
            rowHtml.append("<tr class=\"row_default_blue\" id=\"instruments_row_" + i + "\" name=\"" + this.elementid + "_row_" + i + "\" align=\"center\" datarow=\"Y\" blankrow=\"N\" rownum=\"" + i + "\">");
            rowHtml.append(this.getRowTD("executionid", executionid, i));
            rowHtml.append(this.getRowTD("handler", handler, i));
            rowHtml.append(this.getRowTD("status", statuse, i));
            rowHtml.append(this.getRowTD("date", date, i));
            rowHtml.append(this.getRowTD("log", logValue, i));
            if (hasSDMSAdminRole) {
                rowHtml.append(this.getRowTD("reexecute", reExecuteValue, i));
            }
            rowHtml.append("</tr>");
        }
        rowHtml.append("</tbody>");
        return rowHtml.toString();
    }
}

