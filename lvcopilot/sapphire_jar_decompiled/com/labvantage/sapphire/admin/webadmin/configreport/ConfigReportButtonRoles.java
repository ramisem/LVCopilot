/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.webadmin.configreport;

import com.labvantage.sapphire.admin.webadmin.configreport.BaseConfigReport;
import com.labvantage.sapphire.admin.webadmin.configreport.ConfigReportRequestHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class ConfigReportButtonRoles
extends BaseConfigReport {
    ArrayList buttonList = new ArrayList();
    StringBuffer html;

    @Override
    public void beginReport() {
        this.buttonList = new ArrayList();
        this.html = new StringBuffer();
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("dd-MMM-yyyy 'at' HH:mm z");
        this.html.append("<span style=\"width:100%; text-align: right\">Report generated " + sdf.format(Calendar.getInstance().getTime()) + "</span>\n");
        if (this.reportStyles.trim().length() > 0) {
            this.html.append("<style>\n" + this.reportStyles + "\n</style>\n");
        }
        if (this.reportTitle.trim().length() > 0) {
            this.html.append("<h1>" + this.reportTitle + "</h1>\n");
        }
    }

    @Override
    public boolean wantsListColumns() {
        return false;
    }

    @Override
    public boolean wantsMaintColumns() {
        return false;
    }

    public void nextWebPage(String webpageid, String pageDesc) {
    }

    @Override
    public void beginButtons(boolean hasButtons) {
    }

    @Override
    public void nextButton(ConfigReportRequestHandler.Button button) {
        this.buttonList.add(button);
    }

    @Override
    public void endButtons(boolean hasButtons) {
    }

    @Override
    public void endReport() {
    }

    @Override
    public String getFinalOutput() {
        String COLUMN_WEBPAGEID = "column_webpageid";
        String COLUMN_TEXT = "column_text";
        DataSet results = new DataSet();
        for (ConfigReportRequestHandler.Button button : this.buttonList) {
            int newRow = results.addRow(0);
            results.setString(newRow, "column_webpageid", button.webpageid);
            results.setString(newRow, "column_text", button.text);
            if (button.rolelist == null || button.rolelist.length() <= 0) continue;
            String[] roles = StringUtil.split(button.rolelist, ";");
            for (int i = 0; i < roles.length; ++i) {
                String role = roles[i];
                results.setString(newRow, role, "Y");
            }
        }
        results.sort("column_webpageid,column_text");
        this.html.append("<table border=\"1\" cellspacing=\"0\">");
        this.html.append("<tr>");
        for (int col = 0; col < results.getColumnCount(); ++col) {
            this.html.append("<th>");
            String columnid = results.getColumnId(col);
            if (columnid.equals("column_webpageid")) {
                this.html.append("Page");
            } else if (columnid.equals("column_text")) {
                this.html.append("Button");
            } else {
                this.html.append(columnid);
            }
            this.html.append("</th>");
        }
        this.html.append("</tr>");
        for (int i = 0; i < results.size(); ++i) {
            this.html.append("<tr>");
            for (int col = 0; col < results.getColumnCount(); ++col) {
                this.html.append("<td>");
                String columnid = results.getColumnId(col);
                if (columnid.equals("column_webpageid")) {
                    this.html.append(results.getValue(i, "column_webpageid"));
                } else if (columnid.equals("column_text")) {
                    this.html.append(results.getValue(i, "column_text"));
                } else {
                    boolean exists = results.getValue(i, columnid).equals("Y");
                    if (exists) {
                        this.html.append("X");
                    } else {
                        this.html.append("&nbsp;");
                    }
                }
                this.html.append("</td>");
            }
            this.html.append("</tr>");
        }
        this.html.append("</table>");
        return this.html.toString();
    }

    @Override
    public String getFileExtension() {
        return "html";
    }
}

