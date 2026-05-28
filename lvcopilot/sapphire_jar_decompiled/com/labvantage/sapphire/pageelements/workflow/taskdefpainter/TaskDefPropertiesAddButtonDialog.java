/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefPainter;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefProperties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.pageelements.BaseElement;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskDefPropertiesAddButtonDialog
extends BaseElement {
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_FROMSTEPID = "fromstepid";
    public static final String PROPERTY_TOSTEPID = "tostepid";
    public static final String PROPERTY_TRANSITIONID = "transitionid";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_SELECTEDBUTTON = "selectedbutton";
    boolean devMode = false;
    String steptype = "";
    String stepnode = "";

    public TaskDefPropertiesAddButtonDialog(PageContext pageContext, PropertyList pageproperties) {
        this.setPageContext(pageContext);
        try {
            ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
            try {
                this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                this.devMode = false;
            }
            this.setUpProperties(pageproperties, (HttpServletRequest)pageContext.getRequest());
        }
        catch (Exception e) {
            this.logger.error("Could not set up dialog: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    private void setUpProperties(PropertyList pagedata, HttpServletRequest request) throws Exception {
        pagedata.setProperty("jsrequest", "exclude=properties");
        this.steptype = pagedata.getProperty("steptypeid", "");
        this.stepnode = pagedata.getProperty("steptypenode", "");
    }

    private StringBuffer renderButtons(PropertyList buttonprops, String idPrefix, String style, PageContext pageContext, StringBuffer script, Logger logger) {
        StringBuffer sb = new StringBuffer();
        if (buttonprops != null) {
            String id;
            String string = id = buttonprops.getCollection("buttons") != null ? "buttons" : "buttontemplates";
            if (buttonprops.getCollection(id) != null) {
                for (int i = 0; i < buttonprops.getCollection(id).size(); ++i) {
                    PropertyList buttonprop = buttonprops.getCollection(id).getPropertyList(i);
                    sb.append("<div style=\"padding: 5px 5px 5px 5px;\">");
                    Button btn = new Button(pageContext);
                    btn.setStyle(style + ";width:125px;");
                    btn.setText(buttonprop.getProperty("text", ""));
                    btn.setImg(buttonprop.getProperty("image", ""));
                    btn.setAppearance("ribbonsmall");
                    btn.setTip("Click to select");
                    btn.setId("btn_" + idPrefix + "_" + i);
                    script.append("_btn").append(idPrefix).append("_").append(i).append(" = sapphire.util.propertyList.create(").append(buttonprop.toJSONString(true)).append(");");
                    btn.setAction("__opener.taskProps.buttons.addButton_Callback(_btn" + idPrefix + "_" + i + ");sapphire.ui.dialog.close(__dn);");
                    sb.append(btn.getHtml());
                    sb.append("</div>");
                }
            }
        } else {
            logger.warn("Could not obtain button props");
        }
        return sb;
    }

    private StringBuffer getScriptAndStyle() {
        StringBuffer html = new StringBuffer();
        return html;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        StringBuffer script = new StringBuffer("<script>");
        if (this.steptype.length() > 0) {
            PropertyList useprops;
            boolean devMode;
            ConfigurationProcessor cp = new ConfigurationProcessor(this.pageContext);
            try {
                devMode = cp.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                devMode = false;
            }
            html.append("<div align=\"center\">");
            PropertyList buttonprops = TaskDefPainter.getStepTypeProperties(this.steptype, this.stepnode, new WebAdminProcessor(this.pageContext), this.logger);
            if (buttonprops != null && buttonprops.getPropertyList("stepdef") != null) {
                html.append(this.renderButtons(buttonprops.getPropertyList("stepdef"), "steptype", "background-color: #E3EFFF", this.pageContext, script, this.logger).toString());
            }
            PropertyList defaultprops = TaskDefProperties.getDefaultStepButtons(devMode, this.logger, this.pageContext.getSession());
            if (buttonprops == null || buttonprops.getPropertyList("stepdef") == null || buttonprops.getPropertyList("stepdef").getCollection("buttontemplates") == null) {
                useprops = defaultprops;
            } else {
                useprops = new PropertyList();
                PropertyListCollection btns = new PropertyListCollection();
                useprops.put("buttons", btns);
                if (defaultprops.getCollection("buttons") != null) {
                    for (int i = 0; i < defaultprops.getCollection("buttons").size(); ++i) {
                        PropertyList b = defaultprops.getCollection("buttons").getPropertyList(i);
                        String buttonid = b.getProperty("buttonid", "");
                        if (buttonid.length() != 0 && buttonprops.getPropertyList("stepdef").getCollection("buttontemplates").find("id", buttonid, false) != null) continue;
                        btns.add(b);
                    }
                }
            }
            html.append(this.renderButtons(useprops, "default", "background-color: #D9FFBC", this.pageContext, script, this.logger).toString());
            buttonprops = new PropertyList();
            PropertyListCollection buttons = new PropertyListCollection();
            PropertyList button = new PropertyList();
            button.setProperty("text", "Custom");
            button.setProperty("image", "WEB-CORE/images/png/AddPage.png");
            buttons.add(button);
            buttonprops.setProperty("buttons", buttons);
            html.append(this.renderButtons(buttonprops, "custom", "background-color: #D9FFBC", this.pageContext, script, this.logger).toString());
            html.append("</div>");
            script.append("var __dn = sapphire.ui.dialog.getDialogNumber(window);");
            script.append("var __opener = sapphire.ui.dialog.getDialogObject(__dn).opener;");
            script.append("</script>");
            html.append(script);
        } else {
            html.append("No step type provided.");
        }
        return html.toString();
    }
}

