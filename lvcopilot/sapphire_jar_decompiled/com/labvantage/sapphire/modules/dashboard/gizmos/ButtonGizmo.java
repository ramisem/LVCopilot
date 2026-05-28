/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.servlet.command.ResourceRequest;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseGizmo;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ButtonGizmo
extends BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";

    @Override
    public boolean init() {
        this.setRefreshOnResize(true);
        this.setTimeout(-1);
        return true;
    }

    @Override
    public String getHtml() {
        TranslationProcessor translationProcessor = null;
        translationProcessor = this.pageContext == null ? new TranslationProcessor(this.getConnectionid()) : this.getTranslationProcessor();
        StringBuffer html = new StringBuffer();
        if (this.element != null) {
            PropertyListCollection plcIncludes = this.getElementProperties().getCollectionNotNull("includes");
            for (int i = 0; i < plcIncludes.size(); ++i) {
                PropertyList plInclude = plcIncludes.getPropertyList(i);
                try {
                    html.append(ResourceRequest.getResourceTag(this.pageContext, new ConnectionProcessor(this.getConnectionid()).getConnectionInfo(this.getConnectionid()).getDatabaseId(), plInclude, "text/javascript")).append("\n");
                    continue;
                }
                catch (SapphireException e) {
                    this.logger.warn(e.getMessage());
                }
            }
            PropertyListCollection buttons = this.element.getCollection("buttons");
            html.append("<div style=\"padding-top:5px; padding-left:5px;\">");
            html.append("<table class=\"\" border=0>");
            html.append("<tbody>");
            if (buttons != null) {
                html.append("<tr>");
                for (int r = 0; r < buttons.size(); ++r) {
                    PropertyList b = buttons.getPropertyList(r);
                    Button button = new Button(this.pageContext);
                    if (b.getId().length() <= 0 || !b.getProperty("show", "Y").equalsIgnoreCase("Y")) continue;
                    html.append("<td>");
                    button.setElementid(b.getProperty("id"));
                    button.setText(translationProcessor.translate(b.getProperty("text", b.getProperty("id"))));
                    if (b.getProperty("image", LABVANTAGE_CVS_ID).length() > 0) {
                        button.setImg(b.getProperty("image", LABVANTAGE_CVS_ID));
                    }
                    button.setAction(b.getProperty("action"));
                    html.append(button.getHtml());
                    html.append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        } else {
            html.append("<font size=2>").append(translationProcessor.translate("No element data found.")).append("</font>");
        }
        return html.toString();
    }

    @Override
    public String getScript() {
        return LABVANTAGE_CVS_ID;
    }

    @Override
    public String getIcon() {
        return this.getImage("Buttons", this.getGizmoStyle().size).getHtml();
    }

    @Override
    public String getDefaultImageSrc() {
        return "FlatBlackInterfaceButton";
    }

    @Override
    public String getIconHtml() {
        StringBuffer html = new StringBuffer();
        html.append(super.getIconHtml());
        html.append("<div style=\"display:none;\">" + this.getHtml() + "</div>");
        return html.toString();
    }
}

