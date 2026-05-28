/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.webadmin.configreport;

import com.labvantage.sapphire.admin.webadmin.configreport.BaseConfigReport;
import com.labvantage.sapphire.admin.webadmin.configreport.ConfigReportRequestHandler;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ConfigReportHtml
extends BaseConfigReport {
    StringBuffer html;

    @Override
    public void beginReport() {
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
    public void nextWebPage(ConfigReportRequestHandler.WebPage webpage, boolean includeDetails) {
        this.html.append("\n<h2>" + webpage.webpageid + " (" + webpage.productedition + ")</h2>\n");
        if (includeDetails) {
            this.html.append("<table border=\"1\" cellspacing=\"0\">\n");
            this.html.append("<tr><th>Description</th><td>").append(webpage.description.length() == 0 ? "&nbsp;" : webpage.description).append("</td></tr>\n");
            this.html.append("<tr><th>Page Type</th><td>").append(webpage.pagetype.length() == 0 ? "&nbsp;" : webpage.pagetype).append("</td></tr>\n");
            this.html.append("<tr><th>Virtual Access</th><td>").append(webpage.virtualpage ? "Yes" : "No").append("</td></tr>\n");
            this.html.append("<tr><th>Roles</th><td>").append(webpage.rolelist.length() == 0 ? "&nbsp;" : webpage.rolelist).append("</td></tr>\n");
            this.html.append("</table><br>");
        }
    }

    @Override
    public void beginButtons(boolean hasButtons) {
        if (hasButtons) {
            this.html.append("<table border=\"1\" cellspacing=\"0\">\n");
            this.html.append("<tr><th colspan=\"5\">Toolbar Buttons</td></tr>\n");
            this.html.append("<tr>");
            this.html.append("<th colspan=\"2\">Button</td>");
            this.html.append("<th>Type</td>");
            this.html.append("<th>Function</td>");
            this.html.append("<th>Roles</td>");
            this.html.append("</tr>\n");
        } else {
            this.html.append("This page has no toolbar buttons<br>\n");
        }
    }

    @Override
    public void nextButton(ConfigReportRequestHandler.Button button) {
        this.html.append("<tr>");
        if (button.image.length() > 0) {
            this.html.append("<td><img src=\"").append(this.imageRoot + button.image).append("\"></td>");
        } else {
            this.html.append("<td>&nbsp;</td>");
        }
        this.html.append("<td>").append(button.text).append("</td>");
        this.html.append("<td>").append(button.type).append("</td>");
        this.html.append("<td>").append(button.operation).append("</td>");
        this.html.append("<td>").append(button.rolelist.length() == 0 ? "&nbsp;" : button.rolelist).append("</td>");
        this.html.append("</tr>\n");
    }

    @Override
    public void endButtons(boolean hasButtons) {
        if (hasButtons) {
            this.html.append("</table><br>\n");
        }
    }

    @Override
    public void beginListColumns(boolean hasColumns) {
        if (hasColumns) {
            this.html.append("<table border=\"1\" cellspacing=\"0\">\n");
            this.html.append("<tr><th colspan=\"5\">List Columns</td></tr>\n");
            this.html.append("<tr>");
            this.html.append("<th>Title</td>");
            this.html.append("<th>SDC</td>");
            this.html.append("<th>Column/SQL</td>");
            this.html.append("<th>Link</td>");
            this.html.append("<th>Roles</td>");
            this.html.append("</tr>\n");
        } else {
            this.html.append("<table border=\"0\"><tr><td>This page has no list columns</td></tr></table>\n");
        }
    }

    @Override
    public void nextListColumn(ConfigReportRequestHandler.ListColumn column) {
        this.html.append("<tr>");
        this.html.append("<td>").append(column.title.trim().length() == 0 ? "-" : column.title).append("</td>");
        this.html.append("<td>").append(column.sdcid).append("</td>");
        this.html.append("<td>").append(column.columnid).append("</td>");
        this.html.append("<td>").append(column.link.length() == 0 ? "&nbsp;" : column.link).append("</td>");
        this.html.append("<td>").append(column.rolelist.length() == 0 ? "&nbsp;" : column.rolelist).append("</td>");
        this.html.append("</tr>\n");
    }

    @Override
    public void endListColumns(boolean hasColumns) {
        if (hasColumns) {
            this.html.append("</table><br>\n");
        }
    }

    @Override
    public void beginMaintColumns(boolean hasColumns) {
        if (hasColumns) {
            this.html.append("<table border=\"1\" cellspacing=\"0\">\n");
            this.html.append("<tr><th colspan=\"7\">Maint Columns</td></tr>\n");
            this.html.append("<tr>");
            this.html.append("<th>Title</td>");
            this.html.append("<th>SDC</td>");
            this.html.append("<th>Column/SQL</td>");
            this.html.append("<th>Display Type</td>");
            this.html.append("<th>Validation Rule</td>");
            this.html.append("<th>Default Value</td>");
            this.html.append("<th>Roles</td>");
            this.html.append("</tr>\n");
        } else {
            this.html.append("<table border=\"0\"><tr><td>This page has no maintenance columns</td></tr></table>\n");
        }
    }

    @Override
    public void nextMaintColumn(ConfigReportRequestHandler.MaintColumn column) {
        this.html.append("<tr>");
        this.html.append("<td>").append(column.title.trim().length() == 0 ? "-" : column.title).append("</td>");
        this.html.append("<td>").append(column.sdcid).append("</td>");
        this.html.append("<td>").append(column.columnid).append("</td>");
        this.html.append("<td>").append(column.mode.length() == 0 ? "&nbsp;" : column.mode).append("</td>");
        this.html.append("<td>").append(column.validationrule.length() == 0 ? "&nbsp;" : column.validationrule).append("</td>");
        this.html.append("<td>").append(column.defaultvalue.length() == 0 ? "&nbsp;" : column.defaultvalue).append("</td>");
        this.html.append("<td>").append(column.rolelist.length() == 0 ? "&nbsp;" : (column.rolelist.length() == 0 ? "&nbsp;" : column.rolelist)).append("</td>");
        this.html.append("</tr>\n");
    }

    @Override
    public void endMaintColumns(boolean hasColumns) {
        if (hasColumns) {
            this.html.append("</table><br><br>\n");
        }
    }

    @Override
    public void endReport() {
    }

    @Override
    public String getFinalOutput() {
        return this.html.toString();
    }

    @Override
    public String getFileExtension() {
        return "html";
    }
}

