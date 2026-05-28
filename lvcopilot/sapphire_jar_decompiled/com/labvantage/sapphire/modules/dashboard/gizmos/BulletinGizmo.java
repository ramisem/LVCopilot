/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.layouts.modern.GizmoTargetAjaxManager;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseGizmo;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.xml.PropertyList;

public class BulletinGizmo
extends BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";
    public static final String SHOWBULLETINTEXTPREVIEW_PROPERTY = "showBulletinTextPreview";
    public static final String CHARTOSHOWINPREVIEW_PROPERTY = "charsToShowinPreview";
    public static final String URL = "rc?command=page&page=UserBulletinList";
    private DataSet bulletinData = null;

    @Override
    public boolean init() {
        this.setRefreshOnResize(true);
        return true;
    }

    @Override
    public PropertyList getUserProperties() {
        PropertyList up = super.getUserProperties();
        up.setProperty(SHOWBULLETINTEXTPREVIEW_PROPERTY, "Y");
        up.setProperty(CHARTOSHOWINPREVIEW_PROPERTY, "Y");
        return up;
    }

    private void getData(String user) {
        this.bulletinData = this.getQueryProcessor().getPreparedSqlDataSet(10320, new Object[]{user});
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor translationProcessor = this.pageContext == null ? new TranslationProcessor(this.getConnectionid()) : this.getTranslationProcessor();
        if (this.element != null) {
            GizmoTargetAjaxManager.GizmoType gizmoType = GizmoTargetAjaxManager.GizmoType.SIDEBAR;
            try {
                gizmoType = GizmoTargetAjaxManager.GizmoType.valueOf(this.element.getProperty("gizmotype", GizmoTargetAjaxManager.GizmoType.SIDEBAR.toString()).toUpperCase());
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (gizmoType != GizmoTargetAjaxManager.GizmoType.SIDEBAR_CONTENTONLY && gizmoType != GizmoTargetAjaxManager.GizmoType.TOPBAR_CONTENTONLY) {
                html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/bulletingizmo.js\"></script>");
            }
            if (this.element.getProperty("sendnewbulletinmode").equalsIgnoreCase("Y")) {
                String SEND_BULLETIN_URL = "rc?command=file&file=WEB-OPAL/pagetypes/bulletins/sendbulletin.jsp&return=N&sapphiredialog=Y";
                if (this.requestContext.getPropertyList("pagedata") != null) {
                    String bulletinbody;
                    String bulletinurl;
                    String bulletindescription;
                    String bulletindept;
                    String bulletinrole;
                    PropertyList pagedata = this.requestContext.getPropertyList("pagedata");
                    String bulletinuser = pagedata.getProperty("bulletinuser", LABVANTAGE_CVS_ID);
                    if (bulletinuser.length() > 0) {
                        SEND_BULLETIN_URL = SEND_BULLETIN_URL + "&bulletinuser=" + bulletinuser;
                    }
                    if ((bulletinrole = pagedata.getProperty("bulletinrole", LABVANTAGE_CVS_ID)).length() > 0) {
                        SEND_BULLETIN_URL = SEND_BULLETIN_URL + "&bulletinrole=" + bulletinrole;
                    }
                    if ((bulletindept = pagedata.getProperty("bulletindept", LABVANTAGE_CVS_ID)).length() > 0) {
                        SEND_BULLETIN_URL = SEND_BULLETIN_URL + "&bulletindept=" + bulletindept;
                    }
                    if ((bulletindescription = pagedata.getProperty("bulletindescription", LABVANTAGE_CVS_ID)).length() > 0) {
                        SEND_BULLETIN_URL = SEND_BULLETIN_URL + "&bulletindescription=" + bulletindescription;
                    }
                    if ((bulletinurl = pagedata.getProperty("bulletinurl", LABVANTAGE_CVS_ID)).length() > 0) {
                        SEND_BULLETIN_URL = SEND_BULLETIN_URL + "&bulletinurl=" + bulletinurl;
                    }
                    if ((bulletinbody = pagedata.getProperty("bulletinbody", LABVANTAGE_CVS_ID)).length() > 0) {
                        SEND_BULLETIN_URL = SEND_BULLETIN_URL + "&bulletinbody=" + bulletinbody;
                    }
                }
                html.append("<iframe onload=\"\" name=\"").append(this.elementid).append("_bulletin_frame\" id=\"").append(this.getGizmoDefId().length() > 0 ? this.getGizmoDefId() : this.elementid).append("_bulletin_frame\" frameborder=0 scrolling=false src=\"").append(this.browser.getBlankSrc()).append("\" style=\"width:100%;height:100%;\"></iframe>");
                html.append("<form style=\"display:none\" method=\"post\" id=\"").append(this.elementid).append("_bulletin_form\" name=\"").append(this.elementid).append("_bulletin_form\" action=\"").append(SEND_BULLETIN_URL).append("\" target=\"").append(this.elementid).append("_bulletin_frame\">");
                html.append("<input type=\"hidden\" name=\"showclose\" value=\"").append("N").append("\">");
                html.append("<input type=\"hidden\" name=\"target\" value=\"").append("_sapphiredialog").append("\">");
                html.append("<input type=\"hidden\" name=\"showBulletinTextPreview\" value=\"").append(this.element.getProperty(SHOWBULLETINTEXTPREVIEW_PROPERTY)).append("\">");
                html.append("<input type=\"hidden\" name=\"charsToShowinPreview\" value=\"").append(this.element.getProperty(CHARTOSHOWINPREVIEW_PROPERTY)).append("\">");
                html.append("</form>");
            } else if (this.element.getProperty("compact").equalsIgnoreCase("Y")) {
                SapphireConnection sc = this.getConnectionProcessor().getSapphireConnection();
                String user = sc.getSysuserId();
                String userdesc = sc.getSysuserName();
                if (userdesc == null) {
                    userdesc = LABVANTAGE_CVS_ID;
                }
                RequestContext rc = this.requestContext != null ? this.requestContext : RequestContext.getInstance(this.request);
                boolean html5 = true;
                this.getData(user);
                html.append("<div style=\"position:relative;height:100%;width:100%;overflow-y:hidden;").append(LABVANTAGE_CVS_ID).append("\">");
                String onclick = "bulletinGizmo.sendBulletin('" + this.elementid + "');";
                html.append("<table width=\"100%\" cellpadding=0 cellspacing=0 border=0 style=\"height:34px;").append(html5 ? LABVANTAGE_CVS_ID : "padding-left:2px;").append("\">");
                html.append("<tbody>");
                html.append("<tr>");
                html.append("<td>");
                Image image = new Image(this.pageContext);
                image.setConnectionId(this.getConnectionId());
                image.setImageId("Mail");
                image.setDimensions(32, 32);
                html.append(image.getHtml());
                html.append("</td>");
                html.append("<td>");
                html.append("<strong style=\"font-size:10pt;\">").append(translationProcessor.translate("Bulletins")).append("</strong><br>");
                html.append(userdesc.length() > 0 ? userdesc : user);
                html.append("</td>");
                html.append("<td align=\"center\" style=\"font-size:10pt;color:#7F9DB9;width:30px;\">");
                html.append(this.bulletinData != null ? this.bulletinData.getRowCount() : 0);
                html.append("</td>");
                if (this.browser.isMobile()) {
                    html.append("<td ontouchend=\"").append(onclick).append("\"\">");
                } else {
                    html.append("<td style=\"cursor:pointer;\" onclick=\"").append(onclick).append("\">");
                }
                image = new Image(this.pageContext);
                image.setConnectionId(this.getConnectionId());
                image.setImageId("MailAdd");
                image.setDimensions(16, 16);
                html.append(image.getHtml());
                html.append("</td>");
                html.append("</tr>");
                html.append("</tbody>");
                html.append("</table>");
                if (html5) {
                    html.append("<div id=\"bdiv_").append(this.elementid).append("\" style=\"height:auto;position:absolute;bottom:0;top:45px;overflow:hidden;border-top:solid 1px #7F9DB9;left:4px;right:18px;\">");
                } else {
                    html.append("<div id=\"bdiv_").append(this.elementid).append("\" style=\"border-top: solid 1px #7F9DB9;height:auto;overflow-y:hidden;position:absolute;top:65px;bottom: 4px;left:4px;right:24px;\">");
                }
                html.append("<table width=\"100%\" cellpadding=0 cellspacing=0 border=0 style=\"" + (html5 ? "padding-right:2px;" : "padding:2px;") + "\">");
                html.append("<tbody>");
                if (this.bulletinData != null && this.bulletinData.getRowCount() > 0) {
                    for (int i = 0; i < this.bulletinData.getRowCount(); ++i) {
                        boolean read = this.bulletinData.getValue(i, "readflag", "N").equalsIgnoreCase("Y");
                        html.append("<tr>");
                        html.append("<td>");
                        onclick = "bulletinGizmo.viewBulletin('" + this.bulletinData.getValue(i, "bulletinid", LABVANTAGE_CVS_ID) + "','" + this.elementid + "');";
                        if (this.browser.isMobile()) {
                            html.append("<table ontouchend=\"").append(onclick).append("\" width=\"100%\" cellpadding=0 cellspacing=0 border=0 style=\"min-height:50px;border-bottom:solid 1px #DDDDDD;background-color:").append(read ? "#F4F4F4" : "#FFFFFF").append(";\">");
                        } else {
                            html.append("<table onclick=\"").append(onclick).append("\" width=\"100%\" cellpadding=0 cellspacing=0 border=0 style=\"cursor:pointer;min-height:50px;border-bottom:solid 1px #DDDDDD;background-color:").append(read ? "#F4F4F4" : "#FFFFFF").append(";\">");
                        }
                        html.append("<tbody>");
                        html.append("<tr>");
                        html.append("<td>");
                        html.append(read ? LABVANTAGE_CVS_ID : "<strong>");
                        html.append("<span style=\"font-size:10pt;\">").append(this.bulletinData.getValue(i, "source", translationProcessor.translate("Unknown Sender"))).append("</span>");
                        html.append(read ? LABVANTAGE_CVS_ID : "</strong>");
                        html.append("</td>");
                        html.append("<td align=\"right\">");
                        html.append(read ? LABVANTAGE_CVS_ID : "<strong>");
                        html.append(this.bulletinData.getValue(i, "createdt", LABVANTAGE_CVS_ID));
                        html.append(read ? LABVANTAGE_CVS_ID : "</strong>");
                        html.append("</td>");
                        html.append("</tr>");
                        html.append("<tr>");
                        html.append("<td colspan=2>");
                        html.append(read ? LABVANTAGE_CVS_ID : "<strong>");
                        html.append("<span>").append(SafeHTML.encodeForHTML(this.bulletinData.getValue(i, "bulletindesc", translationProcessor.translate("No Subject")), true)).append("</span>");
                        if (!this.element.getProperty(SHOWBULLETINTEXTPREVIEW_PROPERTY).equalsIgnoreCase("N")) {
                            String text = this.bulletinData.getValue(i, "bulletintext", "&nbsp;");
                            int max = 60;
                            try {
                                max = Integer.parseInt(this.element.getProperty(CHARTOSHOWINPREVIEW_PROPERTY, LABVANTAGE_CVS_ID + max));
                            }
                            catch (Exception e) {
                                max = 60;
                            }
                            if (text.length() > max) {
                                text = text.substring(0, max) + "...";
                            }
                            html.append(" - ").append(SafeHTML.encodeForHTML(text, true));
                        }
                        html.append(read ? LABVANTAGE_CVS_ID : "</strong>");
                        html.append("</td>");
                        html.append("</tr>");
                        html.append("</tbody>");
                        html.append("</table>");
                        html.append("</td>");
                        html.append("</tr>");
                    }
                } else {
                    html.append("<tr>");
                    html.append("<td>");
                    html.append(translationProcessor.translate("No Bulletins"));
                    html.append("</td>");
                    html.append("</tr>");
                }
                html.append("</tbody>");
                html.append("</table>");
                html.append("</div>");
                image = new Image(this.pageContext);
                image.setConnectionId(this.getConnectionId());
                image.setImageId("ArrowDownBlue");
                image.setTitle("Scroll Down");
                if (this.browser.isMobile()) {
                    html.append("<div style=\"width:25px;height:20px;padding-top:2px;padding-left:2px;background-color:white;opacity:0.5;border-radius:6px;position:absolute;bottom:" + (html5 ? 0 : 20) + "px;right:" + (html5 ? 0 : 5) + "px;cursor:pointer;\" ontouchstart=\"bulletinGizmo.scrollMouseDown(10,'").append(this.elementid).append("');\" ontouchend=\"bulletinGizmo.scrollMouseUp('").append(this.elementid).append("');\">");
                } else {
                    html.append("<div style=\"width:25px;height:20px;padding-top:2px;padding-left:2px;background-color:white;opacity:0.5;border-radius:6px;position:absolute;bottom:" + (html5 ? 0 : 20) + "px;right:" + (html5 ? 0 : 5) + "px;cursor:pointer;\" onmousedown=\"bulletinGizmo.scrollMouseDown(10,'").append(this.elementid).append("');\" onmouseup=\"bulletinGizmo.scrollMouseUp('").append(this.elementid).append("');\">");
                }
                if (!html5) {
                    image.setStyle("margin-left: 5px;");
                }
                html.append(image.getHtml());
                html.append("</div>");
                image = new Image(this.pageContext);
                image.setConnectionId(this.getConnectionId());
                image.setImageId("ArrowUpBlue");
                image.setTitle("Scroll Up");
                if (this.browser.isMobile()) {
                    html.append("<div style=\"width:25px;height:20px;padding-top:2px;padding-left:2px;background-color:white;opacity:0.5;border-radius:6px;position:absolute;top:" + (html5 ? 52 : 72) + "px;right:" + (html5 ? 0 : 5) + "px;cursor:pointer;\" ontouchstart=\"bulletinGizmo.scrollMouseDown(-10,'").append(this.elementid).append("');\" ontouchend=\"bulletinGizmo.scrollMouseUp('").append(this.elementid).append("');\">");
                } else {
                    html.append("<div style=\"width:25px;height:20px;padding-top:2px;padding-left:2px;background-color:white;opacity:0.5;border-radius:6px;position:absolute;top:" + (html5 ? 52 : 72) + "px;right:" + (html5 ? 0 : 5) + "px;cursor:pointer;\" onmousedown=\"bulletinGizmo.scrollMouseDown(-10,'").append(this.elementid).append("');\" onmouseup=\"bulletinGizmo.scrollMouseUp('").append(this.elementid).append("');\">");
                }
                if (!html5) {
                    image.setStyle("margin-left: 5px;");
                }
                html.append(image.getHtml());
                html.append("</div>");
            } else {
                html.append("<iframe onload=\"\" name=\"").append(this.elementid).append("_bulletin_frame\" id=\"").append(this.getGizmoDefId().length() > 0 ? this.getGizmoDefId() : this.elementid).append("_bulletin_frame\" frameborder=0 scrolling=false src=\"").append(this.browser.getBlankSrc()).append("\" style=\"width:100%;height:100%;\"></iframe>");
                html.append("<form style=\"display:none\" method=\"post\" id=\"").append(this.elementid).append("_bulletin_form\" name=\"").append(this.elementid).append("_bulletin_form\" action=\"").append(URL).append("\" target=\"").append(this.elementid).append("_bulletin_frame\">");
                html.append("<input type=\"hidden\" name=\"showclose\" value=\"").append("N").append("\">");
                html.append("<input type=\"hidden\" name=\"target\" value=\"").append("_sapphiredialog").append("\">");
                html.append("<input type=\"hidden\" name=\"showBulletinTextPreview\" value=\"").append(this.element.getProperty(SHOWBULLETINTEXTPREVIEW_PROPERTY)).append("\">");
                html.append("<input type=\"hidden\" name=\"charsToShowinPreview\" value=\"").append(this.element.getProperty(CHARTOSHOWINPREVIEW_PROPERTY)).append("\">");
                html.append("</form>");
            }
        } else {
            html.append("<font size=2>").append(translationProcessor.translate("No element data found.")).append("</font>");
        }
        html.append("</div>");
        return html.toString();
    }

    @Override
    public String getIconHtml() {
        StringBuffer html = new StringBuffer();
        String h = this.getHelpText();
        h = SafeHTML.encodeForHTML(h, true);
        if (this.getGizmoStyle().showImage) {
            html.append("<span title=\"").append(h).append("\" onclick=\"").append(this.getNavigateJS()).append("\" ").append(this.getGizmoStyle().className.length() > 0 ? " class=\"" + this.getGizmoStyle().className + "_img\"" : LABVANTAGE_CVS_ID).append(">");
            html.append(this.getIcon());
            if (this.getGizmoStyle() != BaseGizmo.GizmoStyle.SMALLTEXT) {
                html.append(this.getNotifyHtml(this.browser, this.getCount(), this.getPreviewJS() + ";event.cancelBubble=true;", this.elementid));
            }
            html.append("</span>");
        }
        if (this.getGizmoStyle().showTitle) {
            String titleColor = this.getTitleColor();
            html.append("<span title=\"").append(h).append("\" id=\"").append(this.elementid).append("_text\" onclick=\"").append(this.getNavigateJS()).append("\" ").append(this.getGizmoStyle().className.length() > 0 ? " class=\"" + this.getGizmoStyle().className + "_txt\"" : LABVANTAGE_CVS_ID);
            html.append(titleColor.length() > 0 ? " style=\"color:" + titleColor + "\"" : LABVANTAGE_CVS_ID).append(">");
            String t = this.getTitle();
            t = SafeHTML.encodeForHTML(t, true);
            html.append("<span id=\"").append(this.elementid).append("_changetext\">").append(t).append("</span>");
            if (this.getGizmoStyle() == BaseGizmo.GizmoStyle.SMALLTEXT) {
                html.append("</span>");
                html.append("<span").append(this.getGizmoStyle().className.length() > 0 ? " class=\"" + this.getGizmoStyle().className + "_notify\"" : LABVANTAGE_CVS_ID).append(">");
                html.append(this.getNotifyHtml(this.browser, this.getCount(), this.getPreviewJS(), this.elementid));
                html.append("</span>");
            } else {
                if (!this.getGizmoStyle().showImage || this.getGizmoStyle() == BaseGizmo.GizmoStyle.SMALLTEXT) {
                    html.append(this.getNotifyHtml(this.browser, this.getCount(), this.getPreviewJS(), this.elementid));
                }
                html.append("</span>");
            }
        }
        return html.toString();
    }

    @Override
    public String getScript() {
        StringBuffer html = new StringBuffer();
        if (this.getGizmoLocation() != BaseGizmo.GizmoLocation.SIDEBAR && this.getGizmoLocation() != BaseGizmo.GizmoLocation.TOPBAR) {
            if (this.element.getProperty("sendnewbulletinmode").equalsIgnoreCase("N") && this.element.getProperty("compact").equalsIgnoreCase("Y")) {
                html.append("if ( typeof(bulletinGizmo) !='undefined' ) {bulletinGizmo.elementid='").append(this.elementid).append("'};");
            } else {
                html.append("if (typeof(").append(this.elementid).append("_bulletin_form) != 'undefined'){");
                html.append("sapphire.page.addCSRFToken( " + this.elementid + "_bulletin_form );");
                html.append(this.elementid + "_bulletin_form.submit();");
                html.append("}");
            }
        }
        return html.toString();
    }

    @Override
    public String getURL() {
        return "javascript:sapphire.ui.dialog.open('View Bulletins','rc?command=page&page=UserBulletinList', false, 650, 535);";
    }

    @Override
    public String getDefaultImageSrc() {
        return "rc?command=image&image=FlatWhiteEnvelope1";
    }

    @Override
    public String getIcon() {
        return this.getImage("Bulletin Gizmo", this.getGizmoStyle().size).getHtml();
    }

    @Override
    public int evalCount() {
        if (this.bulletinData == null) {
            SapphireConnection sc = this.getConnectionProcessor().getSapphireConnection();
            String user = sc.getSysuserId();
            this.getData(user);
        }
        int count = 0;
        if (this.bulletinData != null) {
            for (int i = 0; i < this.bulletinData.getRowCount(); ++i) {
                if (!this.bulletinData.getValue(i, "readflag", "N").equalsIgnoreCase("N")) continue;
                ++count;
            }
        }
        return count;
    }
}

