/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.forms;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.forms.PropertyBuilder;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeDefHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.xml.PropertyList;

public class FieldBuilder
extends BaseElement {
    public static final String PROPERTY_FIELDPROPERTIES = "fieldproperties";
    public static final String PROPERTY_CALLBACK = "callback";
    public static final String PROPERTY_FIELDID = "fieldid";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_SHOWBUTTONS = "showbuttons";
    public static final String PROPERTY_SHOWOBJECTS = "showobjects";
    public static final String PROPERTY_EMBEDDED = "embedded";
    public static final String PROPERTY_FIELDTYPE = "fieldtype";
    public static final String PROPERTY_DEFAULTEDITOR = "defaulteditor";
    private PropertyList fieldprops;
    private PropertyList userConfig;
    private String callbackFunc;
    private String fieldId;
    private String datatype = "string";
    private String editor = "";
    private boolean viewonly = false;
    private boolean showbuttons = false;
    private boolean embedded = false;
    private boolean devMode;

    public FieldBuilder(PageContext pageContext, PropertyList pageproperties) {
        this.setPageContext(pageContext);
        try {
            ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
            try {
                this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                this.devMode = false;
            }
            this.setUpObjects(pageContext);
            this.setUp(pageproperties, (HttpServletRequest)pageContext.getRequest());
        }
        catch (Exception e) {
            this.fieldprops = null;
            this.logger.error("Could not set up field builder: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setUpObjects(PageContext pageConext) {
        PropertyDefinitionList propdeflist = null;
        Object ob = pageConext.getSession().getAttribute("formbuilder_formobjects_pd");
        if (ob == null || !(ob instanceof PropertyDefinitionList) || this.devMode) {
            block12: {
                this.logger.debug("Objects not in cache, thus load.");
                try {
                    InputStream is = this.getResourceStream();
                    if (is != null) {
                        try {
                            PropertyTree tree = new PropertyTree();
                            PropertyTreeDefHandler handler = new PropertyTreeDefHandler(tree);
                            handler.setXMLString(FileUtil.getInputStreamString(is));
                            handler.setPrintStream(null);
                            SaxUtil.parseString(handler);
                            propdeflist = tree.getPropertyDefinitionList();
                            break block12;
                        }
                        finally {
                            is.close();
                        }
                    }
                    this.logger.warn("Property definition could be obtained");
                }
                catch (Exception e) {
                    this.logger.warn("Could not load formobjects.xml resource. File could not be parsed. " + e.getMessage());
                }
            }
            if (propdeflist == null) {
                propdeflist = new PropertyDefinitionList("formobjects");
            } else {
                propdeflist.setPropertyDefId("formobjects");
            }
            if (pageConext != null && pageConext.getSession() != null) {
                pageConext.getSession().setAttribute("formbuilder_formobjects_pd", (Object)propdeflist);
            } else {
                this.logger.warn("Could not cache formobjects. PageContext is null.");
            }
        } else {
            this.logger.debug("Obtained objects from cache.");
        }
    }

    private InputStream getResourceStream() {
        URL fileurl = this.getClass().getResource("formobjects.xml");
        InputStream out = null;
        if (fileurl != null) {
            if (fileurl.getPath().contains(".jar!/")) {
                this.logger.info("Resource formobjects.xml is Jar resource.");
            } else {
                this.logger.info("Resource formobjects.xml is class resource.");
            }
            InputStream is = this.getClass().getResourceAsStream("formobjects.xml");
            if (is != null) {
                this.logger.debug("Input stream obatined.");
                out = is;
            } else {
                this.logger.warn("Could not load formobjects.xml resource. Input stream could not be created.");
            }
        } else {
            this.logger.warn("Could not load formobjects.xml resource. File could not be found.");
        }
        return out;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void setUp(PropertyList pagedata, HttpServletRequest request) throws Exception {
        pagedata.setProperty("jsrequest", "exclude=fieldproperties");
        String show = pagedata.getProperty("showtitle", "N");
        this.logger.debug("show = " + show);
        PropertyList layout = pagedata.getPropertyList("layout");
        if (layout != null) {
            layout.setProperty("hideshadow", "Y");
            if (show.equalsIgnoreCase("N")) {
                layout.setProperty("hidetitle", "Y");
            } else {
                layout.setProperty("hidetitle", "N");
            }
        }
        this.datatype = pagedata.getProperty(PROPERTY_FIELDTYPE, this.datatype);
        this.editor = pagedata.getProperty(PROPERTY_DEFAULTEDITOR, "");
        this.logger.debug("datatype = " + this.datatype + ", editor = " + this.editor);
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("viewonly = " + this.viewonly);
        this.showbuttons = !pagedata.getProperty(PROPERTY_SHOWBUTTONS, "y").equalsIgnoreCase("n");
        this.logger.debug("showbuttons = " + this.showbuttons);
        this.embedded = pagedata.getProperty(PROPERTY_EMBEDDED, "n").equalsIgnoreCase("y");
        this.logger.debug("embedded = " + this.embedded);
        String propertylist = pagedata.getProperty(PROPERTY_FIELDPROPERTIES, "");
        if (propertylist.length() > 0) {
            this.logger.debug("Propertylist provided.");
            if (propertylist.startsWith("<propertylist") && propertylist.endsWith("</propertylist>") || propertylist.equals("<propertylist/>")) {
                this.logger.debug("Propertylist in XML format.");
                try {
                    this.fieldprops = new PropertyList();
                    this.fieldprops.setPropertyList(propertylist);
                }
                catch (Exception e) {
                    throw new SapphireException("Could not create propertylist(1): " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                }
            } else {
                if (!propertylist.startsWith("{") || !propertylist.endsWith("}")) throw new SapphireException("Propertylist in invalid format.");
                this.logger.debug("Propertylist in JSON format.");
                try {
                    this.fieldprops = new PropertyList(new JSONObject(propertylist));
                }
                catch (Exception e) {
                    throw new SapphireException("Could not create propertylist(2): " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                }
            }
        } else {
            this.logger.debug("No propertylist provided...start.");
            this.fieldprops = new PropertyList();
        }
        if (this.fieldprops == null) throw new SapphireException("Propertylist could not be created.");
        this.fieldprops.setProperty("datatype", this.datatype);
        this.fieldprops.setProperty("type", this.editor);
        this.fieldId = pagedata.getProperty(PROPERTY_FIELDID, "");
        this.fieldprops.setProperty(PROPERTY_FIELDID, this.fieldId);
        this.callbackFunc = pagedata.getProperty(PROPERTY_CALLBACK, "");
        this.logger.debug("fieldId = " + this.fieldId + ", callbackFunc = " + this.callbackFunc);
        if (this.callbackFunc.length() <= 0 && this.fieldId.length() <= 0) throw new SapphireException("Either a callback function or return field id is required.");
        this.userConfig = RequestContext.getInstance(request).getPropertyList("userconfig");
        if (this.userConfig != null) return;
        throw new SapphireException("User configuration could not be obtained.");
    }

    private StringBuffer getScriptAndStyle() {
        StringBuffer html = new StringBuffer();
        html.append("<link rel=\"stylesheet\" href=\"").append(HttpUtil.getCSS("WEB-CORE/elements/richtext/stylesheets/formbuilder.css", this.pageContext)).append("\" type=\"text/css\">");
        html.append("<script type=\"text/javascript\" src=\"").append(HttpUtil.getScript("WEB-CORE/elements/richtext/scripts/fieldbuilder.js", this.pageContext)).append("\"></script>");
        html.append("<script type=\"text/javascript\" src=\"").append(HttpUtil.getScript("WEB-CORE/elements/richtext/scripts/fieldbuilder_propertychange.js", this.pageContext)).append("\"></script>");
        return html;
    }

    private StringBuffer getEndScript(PropertyList props, String fieldId, String callback, boolean viewOnly, boolean embedded) {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        html.append("fieldBuilder.properties=sapphire.util.propertyList.create(").append(props.toJSONString(false)).append(");");
        html.append("fieldBuilder.fieldId='").append(fieldId).append("';");
        html.append("fieldBuilder.callback='").append(callback).append("';");
        html.append("fieldBuilder.propertyGrouping='").append(this.userConfig != null ? this.userConfig.getProperty("propertybuilder_groupby", "cat") : "cat").append("';");
        html.append("fieldBuilder.viewonly=").append(viewOnly).append(";");
        html.append("fieldBuilder.embedded=").append(embedded).append(";");
        html.append("</script>");
        return html;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.fieldprops != null) {
            html.append(this.getScriptAndStyle());
            html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%;height:100%;table-layout:auto;\">");
            html.append("<tbody>");
            html.append("<tr>");
            html.append(this.getLeftBarStart());
            html.append("<table style=\"table-layout:fixed;\" width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"form_bar\">");
            html.append("<tbody>");
            html.append("<tr class=\"layout_sidebar_tab_row\">");
            html.append("<td align=\"left\" class=\"layout_sidebar_tab layout_sidebar_tab_back\" style=\"width:16px;\">");
            html.append("<img src=\"WEB-CORE/elements/richtext/images/objects-h.gif\" width=\"16\" height=\"17\">");
            html.append("</td>");
            html.append("<td align=\"left\" class=\"layout_sidebar_tab layout_sidebar_tab_back\" valign=\"middle\" nowrap>");
            html.append(this.getTranslationProcessor().translate("Properties"));
            html.append("</td>");
            html.append("<td align=left valign=middle class=\"layout_sidebar_expandcollapse layout_sidebar_tab_back\">");
            html.append("<div id=\"groupingbutton\" onclick=\"fieldBuilder.showPropertyGrouping()\" class=\"form_groupingbutton\" onmouseenter=\"this.style.backgroundColor='#F9F9F5';this.style.border='solid 1px #CECEC3';\" onmouseleave=\"this.style.backgroundColor='';this.style.border='';\"><img src=\"WEB-CORE/elements/richtext/images/grouping.gif\"><img src=\"WEB-CORE/elements/richtext/images/dropdownarrow.gif\"></div>");
            html.append("</td>");
            html.append("</tr>");
            html.append("<tr class=\"form_merge\">");
            html.append("<td colspan=\"3\">");
            html.append("</td>");
            html.append("</tr>");
            String rowheight = "";
            html.append("<tr id=\"field_objects_row\" style=\"height:").append(rowheight).append("px\">");
            html.append("<td colspan=\"3\" class=\"field_bar_parentcell\">");
            html.append("<table width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"form_bar_childtable\">");
            html.append("<tbody>");
            html.append("<tr>");
            html.append("<td valign=\"top\" align=\"left\" class=\"form_bar_childcell\" id=\"field_properties_content_cell\" nowrap>");
            html.append("<div align=\"left\" id=\"field_properties_content\"  style=\"overflow-y:auto;overflow-x:auto;width:100%;height:100%;\">");
            html.append(FieldBuilder.getPropertiesHtml(this.fieldprops, this.viewonly, this.getConnectionId(), this.userConfig, this.getTranslationProcessor(), this.pageContext.getSession(), ProcessingUtil.createBindingsMap(null, this.getQueryProcessor(), this.getSDCProcessor(), null, null)));
            html.append("</div>");
            html.append("</td>");
            html.append("</tr>");
            html.append("</tbody>");
            html.append("</table>");
            html.append("</td>");
            html.append("</tr>");
            if (this.showbuttons) {
                html.append("<tr style=\"height:40px;\">");
                html.append("<td valign=\"middle\" align=\"right\" class=\"form_buttons\" nowrap colspan=\"3\">");
                html.append("<div id=\"field_buttons_content\"  style=\"overflow:auto;width:100%;height:100%;\">");
                html.append(this.getButtonsHtml(this.viewonly));
                html.append("</div>");
                html.append("</td>");
                html.append("</tr>");
            }
            html.append("</tbody>");
            html.append("</table>");
            html.append(this.getLeftBarEnd());
            html.append("</tr>");
            html.append("</tbody>");
            html.append("</table>");
            html.append(this.getEndScript(this.fieldprops, this.fieldId, this.callbackFunc, this.viewonly, this.embedded));
        } else {
            html.append("<font color=\"red\">").append(this.getTranslationProcessor().translate("Could not load editor.")).append("</font>");
        }
        return html.toString();
    }

    public static String getPropertiesHtml(PropertyList props, String connectionId, TranslationProcessor tp, boolean primaryCall, HttpSession session, HashMap bindingMap) {
        return FieldBuilder.getPropertiesHtml(props, connectionId, null, tp, session, bindingMap);
    }

    public static String getPropertiesHtml(PropertyList props, String connectionId, PropertyList userConfig, TranslationProcessor tp, HttpSession session, HashMap bindingMap) {
        return FieldBuilder.getPropertiesHtml(props, false, connectionId, userConfig, tp, session, bindingMap);
    }

    public static String getPropertiesHtml(PropertyList props, boolean viewOnly, String connectionId, PropertyList userConfig, TranslationProcessor tp, HttpSession session, HashMap bindingMap) {
        StringBuffer sb = new StringBuffer();
        if (connectionId != null && connectionId.length() > 0) {
            if (tp == null) {
                tp = new TranslationProcessor(connectionId);
            }
            PropertyBuilder.clearSession(session);
            Object defob = session.getAttribute("formbuilder_formobjects_pd");
            if (defob != null && defob instanceof PropertyDefinitionList) {
                PropertyDefinitionList propertydeflist = (PropertyDefinitionList)defob;
                bindingMap.put("field", props);
                bindingMap.put("fieldbuilder", true);
                PropertyBuilder.renderProperties(1, "", 0, propertydeflist, props, viewOnly, sb, connectionId, userConfig, tp, session, bindingMap);
            } else {
                PropertyBuilder.addPropertyMsg("Property definition not provided", sb, tp);
            }
        } else {
            PropertyBuilder.addPropertyMsg("No connection Id provided", sb, tp);
        }
        return sb.toString();
    }

    private StringBuffer getLeftBarStart() {
        String width = "100%";
        StringBuffer sb = new StringBuffer();
        sb.append("<td id=\"field_leftbar\" class=\"form_leftbar\" style=\"").append(this.browser.isIE() ? "display:block;" : "").append(";width:").append(width).append(";\">");
        return sb;
    }

    private String getButtonsHtml(boolean viewOnly) {
        Button but;
        StringBuffer sb = new StringBuffer();
        if (!viewOnly) {
            but = new Button(this.pageContext);
            but.setText(this.getTranslationProcessor().translate("OK"));
            but.setTip(this.getTranslationProcessor().translate("OK and return"));
            but.setId("fieldbuilder_btn_ok");
            but.setImg("WEB-CORE/images/gif/Confirm.gif");
            but.setAction("fieldBuilder.buttons.doOK()");
            but.setWidth("100");
            sb.append(but.getHtml());
            sb.append("&nbsp;&nbsp;");
        }
        but = new Button(this.pageContext);
        if (viewOnly) {
            but.setText(this.getTranslationProcessor().translate("Close"));
            but.setTip(this.getTranslationProcessor().translate("Close"));
        } else {
            but.setText(this.getTranslationProcessor().translate("Cancel"));
            but.setTip(this.getTranslationProcessor().translate("Cancel and return"));
        }
        but.setId("fieldbuilder_btn_cancel");
        but.setImg("WEB-CORE/images/gif/Cancel.gif");
        but.setAction("fieldBuilder.buttons.doCancel()");
        but.setWidth("100");
        sb.append(but.getHtml());
        return sb.toString();
    }

    private String getLeftBarEnd() {
        return "</td>";
    }
}

