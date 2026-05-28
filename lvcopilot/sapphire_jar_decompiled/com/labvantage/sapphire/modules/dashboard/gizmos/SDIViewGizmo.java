/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.pageelements.controls.Image;
import java.util.ArrayList;
import sapphire.action.BaseSDCRules;
import sapphire.pageelements.BaseGizmo;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SDIView;
import sapphire.util.StringUtil;

public class SDIViewGizmo
extends BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";
    public static final String SDCID_PROPERTY = "sdcid";
    public static final String KEYID1_PROPERTY = "keyid1";
    public static final String KEYID2_PROPERTY = "keyid2";
    public static final String KEYID3_PROPERTY = "keyid3";
    private SDIView sdiView = null;

    @Override
    public boolean init() {
        this.setRefreshOnResize(true);
        this.setTimeout(-1);
        return true;
    }

    private SDIView getSDIView() {
        if (this.sdiView == null) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(this.evaluateExpression(this.element.getProperty(SDCID_PROPERTY), BaseGizmo.I18NFormat.DATABASE));
            String k1 = this.evaluateExpression(this.element.getProperty(KEYID1_PROPERTY), BaseGizmo.I18NFormat.DATABASE);
            String k2 = this.evaluateExpression(this.element.getProperty(KEYID2_PROPERTY), BaseGizmo.I18NFormat.DATABASE);
            String k3 = this.evaluateExpression(this.element.getProperty(KEYID3_PROPERTY), BaseGizmo.I18NFormat.DATABASE);
            if (k1.indexOf("%3B") > -1) {
                String[] s1 = StringUtil.split(k1, "%3B");
                k1 = s1[s1.length - 1];
                String[] s2 = StringUtil.split(k2, "%3B");
                k2 = s2[s2.length - 1];
                String[] s3 = StringUtil.split(k3, "%3B");
                k3 = s3[s3.length - 1];
            }
            sdiRequest.setKeyid1List(k1);
            sdiRequest.setKeyid2List(k2);
            sdiRequest.setKeyid3List(k3);
            sdiRequest.setRequestItem("primary");
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            if (sdiData != null) {
                Class ruleClass;
                try {
                    ruleClass = Class.forName("com.labvantage.sapphire.admin.ddt." + this.element.getProperty(SDCID_PROPERTY));
                }
                catch (Exception e) {
                    ruleClass = BaseSDCRules.class;
                }
                try {
                    BaseSDCRules rule = (BaseSDCRules)ruleClass.newInstance();
                    rule.setConnectionInfo(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                    this.sdiView = rule.getSDIView(sdiData);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return this.sdiView;
    }

    private void renderFields(StringBuffer html, ArrayList<SDIView.SDIViewResponsiveField> fields, SDIView.FieldPriority priority) {
        for (SDIView.SDIViewResponsiveField field : fields) {
            if (field.getPriority() != priority) continue;
            html.append("<tr>");
            html.append("<td>");
            html.append(field.getLabel()).append(":");
            html.append("</td>");
            html.append("<td>");
            String v = field.getValue();
            html.append(v.length() == 0 ? "(null)" : v);
            html.append("</td>");
            html.append("<tr>");
        }
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        this.setGizmoStyle(BaseGizmo.GizmoStyle.LARGETEXT);
        if (this.getWidth() >= 300) {
            html.append("<table style=\"width:100%; height:100%;padding-bottom:25px;\" cellpadding=0 cellspacing=5>");
            html.append("<tbody>");
            html.append("<tr>");
            html.append("<td style=\"text-align:center;\">");
            html.append(this.getIconHtml());
            if (this.getHeight() >= 200) {
                this.renderButtons(html, this.getHeight() >= 300);
            }
            html.append("</td>");
            html.append("<td>");
            html.append("<table style=\"width:100%;border-left:solid 1px #999;\" cellpadding=0 cellspacing=5>");
            html.append("<tbody>");
            if (this.getHeight() >= 200) {
                ArrayList<SDIView.SDIViewResponsiveField> fields = this.sdiView.getFields();
                this.renderFields(html, fields, SDIView.FieldPriority.HIGH);
                if (this.getHeight() >= 300) {
                    this.renderFields(html, fields, SDIView.FieldPriority.MEDIUM);
                }
                if (this.getHeight() >= 400) {
                    this.renderFields(html, fields, SDIView.FieldPriority.LOW);
                }
            } else {
                html.append("<tr>");
                html.append("<td>");
                this.renderButtons(html, false);
                html.append("</td>");
                html.append("</tr>");
            }
            html.append("</tbody>");
            html.append("</table>");
            html.append("</td>");
            html.append("</tr>");
            html.append("</tbody>");
            html.append("</table>");
        } else if (this.getHeight() >= 200) {
            html.append("<table style=\"width:100%; height:100%;padding-bottom:20px;padding-left:5px;\" cellpadding=0 cellspacing=5>");
            html.append("<tbody>");
            html.append("<tr>");
            html.append("<td style=\"text-align:center;\" colspan=\"2\">");
            html.append(this.getIconHtml());
            html.append("</td>");
            html.append("</tr>");
            ArrayList<SDIView.SDIViewResponsiveField> fields = this.sdiView.getFields();
            this.renderFields(html, fields, SDIView.FieldPriority.HIGH);
            if (this.getHeight() >= 300) {
                this.renderFields(html, fields, SDIView.FieldPriority.MEDIUM);
            }
            if (this.getHeight() >= 400) {
                this.renderFields(html, fields, SDIView.FieldPriority.LOW);
            }
            html.append("<tr>");
            html.append("<td colspan=\"2\">");
            this.renderButtons(html, this.getHeight() >= 400);
            html.append("</td>");
            html.append("</tr>");
            html.append("</tbody>");
            html.append("</table>");
        } else {
            html.append(this.getIconHtml());
        }
        return html.toString();
    }

    public void renderButtons(StringBuffer html, boolean large) {
        String viewURL = this.sdiView.getViewURL();
        String editUrl = this.sdiView.getEditURL();
        if (editUrl.length() > 0 || viewURL.length() > 0) {
            Image im;
            html.append("<div style=\"width:100%;text-align:center;display:table;\">");
            html.append("<div style=\"width:").append(large ? "90" : "80").append("px;margin: 0 auto;\">");
            if (editUrl.length() > 0) {
                im = new Image(this.pageContext);
                im.setImageId("Edit");
                im.setTitle("Edit");
                im.setDimensions(large ? 32 : 16, large ? 32 : 16);
                if (viewURL.length() > 0) {
                    html.append("<div style=\"float:right;padding-left:5px;padding-right:5px;cursor:pointer;\" onclick=\"sapphire.page.navigate('").append(editUrl).append("')\">").append(im.getHtml()).append("</div>");
                } else {
                    html.append("<div style=\"\" onclick=\"sapphire.page.navigate('").append(editUrl).append("')\">").append(im.getHtml()).append("</div>");
                }
            }
            if (viewURL.length() > 0) {
                im = new Image(this.pageContext);
                im.setImageId("View");
                im.setTitle("View");
                im.setDimensions(large ? 32 : 16, large ? 32 : 16);
                if (editUrl.length() > 0) {
                    html.append("<div style=\"float:right;padding-left:5px;padding-right:5px;cursor:pointer;\" onclick=\"sapphire.lookup.open('").append(viewURL).append("', 'View')\">").append(im.getHtml()).append("</div>");
                } else {
                    html.append("<div style=\"\" onclick=\"sapphire.lookup.open('").append(viewURL).append("', 'View')\">").append(im.getHtml()).append("</div>");
                }
            }
            html.append("</div>");
            html.append("</div>");
        }
    }

    @Override
    public String getScript() {
        return LABVANTAGE_CVS_ID;
    }

    @Override
    public String getURL() {
        return this.sdiView != null ? this.sdiView.getViewURL() : LABVANTAGE_CVS_ID;
    }

    @Override
    public String getTitle() {
        String s = this.getSDCProcessor().getProperty(this.element.getProperty(SDCID_PROPERTY), "singular", this.element.getProperty(SDCID_PROPERTY));
        return s + " " + this.sdiView.getLabel();
    }

    @Override
    public String getIcon() {
        return this.getImage(this.getSDIView().getLabel(), this.getGizmoStyle().size).getHtml();
    }

    @Override
    public String getDefaultImageSrc() {
        return this.getSDIView().getImageRef().getImageRefId();
    }
}

