/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.webadmin.configreport;

import com.labvantage.sapphire.admin.webadmin.configreport.BaseConfigReport;
import com.labvantage.sapphire.admin.webadmin.configreport.ConfigReportRequestHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class ConfigReportButtonList
extends BaseConfigReport {
    ArrayList buttonList = new ArrayList();
    StringBuffer html;

    @Override
    public void beginReport() {
        this.buttonList = new ArrayList();
        this.html = new StringBuffer();
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("dd-MMM-yyyy 'at' HH:mm z");
        this.html.append("<span style=\"width:100%; text-align: right\">" + sdf.format(Calendar.getInstance().getTime()) + "</span>\n");
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
        this.html.append("<table border=\"1\" cellspacing=\"0\">");
        this.html.append("<tr>");
        this.html.append("<th>Button Text</th>");
        this.html.append("<th>Image</th>");
        this.html.append("<th>Image Filename</th>");
        this.html.append("<th>Button Type</th>");
        this.html.append("<th>Role(s)</th>");
        this.html.append("<th>WebPage</th>");
        this.html.append("</tr>");
        Collections.sort(this.buttonList);
        for (ConfigReportRequestHandler.Button button : this.buttonList) {
            this.html.append("<tr>");
            this.html.append("<td>").append(button.text).append("</td>");
            if (button.image.length() > 0) {
                this.html.append("<td><img src=\"").append(this.imageRoot + button.image).append("\"></td>");
            } else {
                this.html.append("<td>&nbsp;</td>");
            }
            this.html.append("<td>").append(button.image.length() == 0 ? "&nbsp;" : button.image).append("</td>");
            this.html.append("<td>").append(button.type).append("</td>");
            this.html.append("<td>").append(button.rolelist.length() == 0 ? "&nbsp;" : button.rolelist).append("</td>");
            this.html.append("<td>").append(button.webpageid).append(" (").append(button.productedition).append(")").append("</td>");
            this.html.append("</tr>\n");
        }
        this.html.append("</table>");
        return this.html.toString();
    }

    @Override
    public String getFileExtension() {
        return "html";
    }
}

