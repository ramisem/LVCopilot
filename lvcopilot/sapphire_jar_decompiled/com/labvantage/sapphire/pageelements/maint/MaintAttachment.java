/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.maint.MaintElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.tagext.PageTagInfo;
import sapphire.tagext.SDITagInfo;

public class MaintAttachment
extends MaintElement {
    public MaintAttachment() {
        this.datasetname = "attachment";
    }

    public MaintAttachment(PageContext pageContext, SDITagInfo sdiInfo, String connectionid) {
        this.init(pageContext, sdiInfo, connectionid, "attachment");
    }

    private String getStartHtml() {
        StringBuffer out = new StringBuffer();
        String machineName = this.pageContext.getRequest().getServerName();
        this.logger.debug("machineName = " + machineName);
        String protocol = this.pageContext.getRequest().getScheme();
        this.logger.debug("protocol = " + protocol);
        int port = this.pageContext.getRequest().getServerPort();
        this.logger.debug("port = " + port);
        String webApp = ((HttpServletRequest)this.pageContext.getRequest()).getContextPath();
        if (webApp.startsWith("/")) {
            webApp = webApp.substring(1);
        }
        this.logger.debug("webApp = " + webApp);
        String connectionId = this.getConnectionId();
        this.logger.debug("connectionId = " + connectionId);
        String sdcId = this.sdiInfo.getSdcid();
        this.logger.debug("sdcId = " + sdcId);
        PageTagInfo pageInfo = new PageTagInfo(this.pageContext, this.requestContext);
        String keyid1 = this.sdiInfo.getKeycols().length > 0 ? pageInfo.getProperty("keyid1") : "";
        String keyid2 = this.sdiInfo.getKeycols().length > 1 ? pageInfo.getProperty("keyid2") : "";
        String keyid3 = this.sdiInfo.getKeycols().length > 2 ? pageInfo.getProperty("keyid3") : "";
        this.logger.debug("keyid1 = " + keyid1);
        this.logger.debug("keyid2 = " + keyid2);
        this.logger.debug("keyid3 = " + keyid3);
        out.append("\n<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/attachments.js\"></script>\n");
        out.append("<script>\n");
        out.append("window.onbeforeunload = attachments.doSaveCheck;\n");
        out.append("sapphire.events.attachEvent( window, 'onunload', attachments.doFinish );\n");
        out.append("sapphire.events.attachEvent( window, 'onload', new Function( 'attachments.doInit( \"").append(protocol).append("\", \"").append(machineName);
        out.append("\", \"").append(port).append("\", \"").append(webApp).append("\", \"").append(connectionId);
        out.append("\", \"").append(sdcId).append("\", \"").append(keyid1).append("\", \"").append(keyid2);
        if (this.element.getProperty("readonly", "N").equalsIgnoreCase("Y")) {
            out.append("\", \"").append(keyid3).append("\", false, true );' ) );\n");
        } else {
            out.append("\", \"").append(keyid3).append("\", false, false  );' ) );\n");
        }
        out.append("</script>\n");
        return out.toString();
    }

    @Override
    public String getHtml() {
        return this.getStartHtml() + super.getHtml();
    }

    @Override
    protected String getDefaultHeaderHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_headerrow\">\n");
        html.append(this.getHeaderSelector());
        html.append("<td class=\"maintdetail_header\">&nbsp;</td>\n");
        html.append("<td class=\"maintdetail_header\">").append(ElementUtil.getText(this.element, "description", "Description")).append("</td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected String getDefaultRowHtml() {
        StringBuffer html = new StringBuffer();
        String link = "?command=ViewAttachment&sdcid=" + this.sdiInfo.getString("attachment", "sdcid") + "&keyid1=" + this.sdiInfo.getString("attachment", "keyid1") + "&keyid2=" + this.sdiInfo.getString("attachment", "keyid2") + "&keyid3=" + this.sdiInfo.getString("attachment", "keyid3") + "&attachmentnum=" + this.sdiInfo.getInt("attachment", "attachmentnum");
        html.append("<tr height=\"10\" class=\"maintdetail_row\" id=\"").append(this.datasetname).append("_row_").append(this.rownum).append("\">\n");
        html.append(this.getRowSelector());
        html.append("<td class=\"maintdetail_field\" width=\"10\">");
        html.append("<a target=\"_window\" href=\"").append(link).append("\">");
        html.append("<img border=\"0\" src=\"WEB-CORE/images/gif/Attachments.gif\" title=\"").append(ElementUtil.getText(this.element, "viewattachment", "View Attachment")).append("\" />");
        html.append("</a>");
        html.append("</td>\n");
        html.append("<td class=\"maintdetail_field\">");
        html.append("<a target=\"_window\" href=\"").append(link).append("\">");
        html.append(this.sdiInfo.getString("attachment", "attachmentdesc") != null && this.sdiInfo.getString("attachment", "attachmentdesc").length() > 0 ? this.sdiInfo.getString("attachment", "attachmentdesc") : ElementUtil.getText(this.element, "nodescription", "(No Description)"));
        html.append("</a>");
        html.append("</td>");
        html.append("</tr>\n");
        return html.toString();
    }

    @Override
    protected void setRowKeyids() {
        this.id = this.sdiInfo.getString("attachment", "keyid1") + "|" + this.sdiInfo.getString("attachment", "keyid2") + "|" + this.sdiInfo.getString("attachment", "keyid3") + "|" + this.sdiInfo.getInt("attachment", "attachmentnum");
    }
}

