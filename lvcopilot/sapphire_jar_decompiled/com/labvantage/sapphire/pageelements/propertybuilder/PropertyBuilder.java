/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.propertybuilder;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.PropertyDefinition;
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
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class PropertyBuilder
extends BaseElement {
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_CALLBACK = "callback";
    public static final String PROPERTY_ONUPDATE = "onupdate";
    public static final String PROPERTY_ID = "fieldid";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_SHOWBUTTONS = "showbuttons";
    public static final String PROPERTY_RENDERSTYLE = "renderstyle";
    public static final String PROPERTY_SHOWHEADER = "showheader";
    public static final String CACHE_OBJECTS_POST = "_propertybuilder_def";
    public static final String JS_OBJECT = "pb";
    private PropertyList props;
    private PropertyList userConfig;
    private String callbackFunc;
    private String fieldid;
    private boolean viewonly = false;
    private boolean showbuttons = false;
    private RenderStyle renderStyle = RenderStyle.Element;
    private boolean devMode;
    private String xmldef;
    private boolean showHeader = true;
    private String onupdate = "";
    private PropertyDefinitionList propdeflist = null;

    public boolean isEmbedded() {
        return this.renderStyle == RenderStyle.Embedded;
    }

    public PropertyBuilder(PageContext pageContext, PropertyList pageproperties) {
        this.setPageContext(pageContext);
        try {
            ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
            try {
                this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                this.devMode = false;
            }
            this.xmldef = pageproperties.getProperty("definitionxml", "");
            if (this.xmldef.length() > 0) {
                String cache = this.xmldef + CACHE_OBJECTS_POST;
                this.setUpObjects(this.xmldef, cache, pageContext.getSession());
                this.setUp(pageproperties, (HttpServletRequest)pageContext.getRequest());
            } else {
                this.props = null;
                this.logger.error("Could not set up property builder as definition xml was not provided.");
            }
        }
        catch (Exception e) {
            this.props = null;
            this.logger.error("Could not set up property builder: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    public PropertyBuilder(PageContext pageContext, String xmldef) {
        this.setPageContext(pageContext);
        try {
            ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
            try {
                this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                this.devMode = false;
            }
            this.xmldef = xmldef;
            if (xmldef.length() > 0) {
                String cache = xmldef + CACHE_OBJECTS_POST;
                this.setUpObjects(xmldef, cache, pageContext.getSession());
            } else {
                this.props = null;
                this.logger.error("Could not set up property builder as definition xml was not provided.");
            }
        }
        catch (Exception e) {
            this.props = null;
            this.logger.error("Could not set up property builder: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    public PropertyBuilder(String connectionId, PropertyList pageproperties, HttpServletRequest request) {
        try {
            ConfigurationProcessor config = new ConfigurationProcessor(connectionId);
            try {
                this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                this.devMode = false;
            }
            this.xmldef = pageproperties.getProperty("definitionxml", "");
            if (this.xmldef.length() > 0) {
                String cache = this.xmldef + CACHE_OBJECTS_POST;
                this.setUpObjects(this.xmldef, cache, request.getSession());
                this.setUp(pageproperties, request);
            } else {
                this.props = null;
                this.logger.error("Could not set up property builder as definition xml was not provided.");
            }
        }
        catch (Exception e) {
            this.props = null;
            this.logger.error("Could not set up property builder: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    public PropertyDefinitionList getPropertyDefinition() {
        return this.propdeflist;
    }

    public void setPropertyDefinition(PropertyDefinitionList def) {
        this.propdeflist = def;
        if (this.pageContext != null && this.pageContext.getSession() != null) {
            this.pageContext.getSession().setAttribute(this.xmldef + CACHE_OBJECTS_POST, (Object)this.propdeflist);
        }
    }

    public void hideProperty(String id) {
        PropertyDefinition pd = this.propdeflist.getPropertyDef(id);
        if (pd != null) {
            HashMap attr = pd.getAttributes();
            if (attr.containsKey("visible")) {
                attr.remove("visible");
            }
            attr.put("visible", "N");
            pd.setAttributes(attr);
        }
    }

    public void setPropertyEditor(String id, String editor) {
        PropertyDefinition pd = this.propdeflist.getPropertyDef(id);
        if (pd != null) {
            pd.setEditor(editor);
        }
    }

    public void showProperty(String id) {
        PropertyDefinition pd = this.propdeflist.getPropertyDef(id);
        if (pd != null) {
            HashMap attr = pd.getAttributes();
            if (attr.containsKey("visible")) {
                attr.remove("visible");
            }
            attr.put("visible", "Y");
            pd.setAttributes(attr);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setUpObjects(String xmlfile, String cache, HttpSession session) {
        PropertyDefinitionList propdeflist = null;
        Object ob = session.getAttribute(cache);
        if (ob == null || !(ob instanceof PropertyDefinitionList) || this.devMode) {
            block10: {
                this.logger.debug("Objects not in cache, thus load.");
                try {
                    InputStream is = this.getResourceStream(xmlfile);
                    if (is != null) {
                        try {
                            PropertyTree tree = new PropertyTree();
                            PropertyTreeDefHandler handler = new PropertyTreeDefHandler(tree);
                            handler.setXMLString(FileUtil.getInputStreamString(is));
                            handler.setPrintStream(null);
                            SaxUtil.parseString(handler);
                            propdeflist = tree.getPropertyDefinitionList();
                            this.logger.debug("Objects loaded from resource.");
                            break block10;
                        }
                        finally {
                            is.close();
                        }
                    }
                    this.logger.warn("Property definition could be obtained");
                }
                catch (Exception e) {
                    this.logger.warn("Could not load xml resource. File could not be parsed. " + e.getMessage());
                }
            }
            if (propdeflist == null) {
                propdeflist = new PropertyDefinitionList("objects");
            } else {
                propdeflist.setPropertyDefId("objects");
            }
            this.setPropertyDefinition(propdeflist);
        } else {
            this.logger.debug("Obtained objects from cache.");
            this.propdeflist = (PropertyDefinitionList)ob;
        }
    }

    public static InputStream getResourceStream(String xmlfile, Logger logger, Class c) {
        URL fileurl = c.getResource(xmlfile);
        InputStream out = null;
        if (fileurl != null) {
            if (fileurl.getPath().contains(".jar!/")) {
                logger.info("Resource " + xmlfile + " is Jar resource.");
            } else {
                logger.info("Resource " + xmlfile + " is class resource.");
            }
            InputStream is = c.getResourceAsStream(xmlfile);
            if (is != null) {
                logger.debug("Input stream obatined.");
                out = is;
            } else {
                logger.warn("Could not load " + xmlfile + " resource. Input stream could not be created.");
            }
        } else {
            logger.warn("Could not load " + xmlfile + " resource. File could not be found.");
        }
        return out;
    }

    private InputStream getResourceStream(String xmlfile) {
        return PropertyBuilder.getResourceStream(xmlfile, this.logger, this.getClass());
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void setUp(PropertyList pagedata, HttpServletRequest request) throws Exception {
        pagedata.setProperty("jsrequest", "exclude=properties");
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
        this.showHeader = pagedata.getProperty(PROPERTY_SHOWHEADER, "Y").equalsIgnoreCase("y");
        this.logger.debug("showHeader = " + this.showHeader);
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("viewonly = " + this.viewonly);
        this.showbuttons = !pagedata.getProperty(PROPERTY_SHOWBUTTONS, "y").equalsIgnoreCase("n");
        this.logger.debug("showbuttons = " + this.showbuttons);
        String stringRenderStyle = pagedata.getProperty(PROPERTY_RENDERSTYLE, "element");
        this.renderStyle = stringRenderStyle.equalsIgnoreCase("embedded") ? RenderStyle.Embedded : (stringRenderStyle.equalsIgnoreCase("popup") ? RenderStyle.Popup : RenderStyle.Element);
        this.logger.debug("renderStyle = " + (Object)((Object)this.renderStyle));
        if (pagedata.isPropertyList(PROPERTY_PROPERTIES)) {
            this.logger.debug("Propertylist in PropertyList format.");
            this.props = pagedata.getPropertyListNotNull(PROPERTY_PROPERTIES);
        } else {
            String propertylist = pagedata.getProperty(PROPERTY_PROPERTIES, "");
            if (propertylist.length() > 0) {
                this.logger.debug("Propertylist provided.");
                if (propertylist.startsWith("<propertylist") && propertylist.endsWith("</propertylist>") || propertylist.equals("<propertylist/>")) {
                    this.logger.debug("Propertylist in XML format.");
                    try {
                        this.props = new PropertyList();
                        this.props.setPropertyList(propertylist);
                    }
                    catch (Exception e) {
                        throw new SapphireException("Could not create propertylist(1). Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                    }
                } else {
                    if (!propertylist.startsWith("{") || !propertylist.endsWith("}")) throw new SapphireException("Propertylist in invalid format.");
                    this.logger.debug("Propertylist in JSON format.");
                    try {
                        this.props = new PropertyList(new JSONObject(propertylist));
                    }
                    catch (Exception e) {
                        throw new SapphireException("Could not create propertylist(2). Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                    }
                }
            } else {
                this.logger.debug("No propertylist provided...start.");
                this.props = new PropertyList();
            }
        }
        if (this.props == null) throw new SapphireException("Propertylist could not be created.");
        this.fieldid = pagedata.getProperty(PROPERTY_ID, "");
        this.callbackFunc = pagedata.getProperty(PROPERTY_CALLBACK, "");
        this.onupdate = pagedata.getProperty(PROPERTY_ONUPDATE, "");
        this.logger.debug("Id = " + this.fieldid + ", callbackFunc = " + this.callbackFunc + ", onupdate = " + this.onupdate);
        if (this.callbackFunc.length() <= 0 && this.fieldid.length() <= 0 && this.onupdate.length() <= 0) throw new SapphireException("Either a callback function or return id is required.");
        this.userConfig = RequestContext.getInstance(request).getPropertyList("userconfig");
        if (this.userConfig != null) return;
        throw new SapphireException("User configuration could not be obtained.");
    }

    private StringBuffer getScriptAndStyle() {
        StringBuffer html = new StringBuffer();
        html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/richtext/stylesheets/formbuilder.css", this.pageContext) + "\" type=\"text/css\">");
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/propertybuilder/scripts/propertybuilder.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/propertybuilder/scripts/propertybuilder_propertychange.js\"></script>");
        return html;
    }

    private StringBuffer getEndScript(PropertyList props, String fieldid, String callback, boolean viewOnly, String onupdate, RenderStyle renderStyle) {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        html.append(JS_OBJECT).append(".properties=sapphire.util.propertyList.create(").append(props.toJSONString(false)).append(");");
        html.append(JS_OBJECT).append(".fieldid='").append(fieldid).append("';");
        html.append(JS_OBJECT).append(".xmlfile='").append(this.xmldef).append("';");
        html.append(JS_OBJECT).append(".callback='").append(callback).append("';");
        html.append(JS_OBJECT).append(".propertyGrouping='").append(this.userConfig != null ? this.userConfig.getProperty("propertybuilder_groupby", "cat") : "cat").append("';");
        html.append(JS_OBJECT).append(".viewonly=").append(viewOnly).append(";");
        html.append(JS_OBJECT).append(".renderStyle='").append(renderStyle == RenderStyle.Popup ? "popup" : (renderStyle == RenderStyle.Embedded ? "embedded" : "element")).append("';");
        if (onupdate.length() > 0) {
            html.append(JS_OBJECT).append(".onupdate=").append(onupdate).append(";");
        }
        html.append("</script>");
        return html;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.props != null && this.xmldef.length() > 0) {
            boolean rtl = this.getConnectionProcessor().getSapphireConnection().isRtl();
            html.append(this.getScriptAndStyle());
            html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%;height:100%;table-layout:auto;\">");
            html.append("<tbody>");
            html.append("<tr>");
            html.append(this.getLeftBarStart());
            html.append("<table style=\"table-layout:fixed;\" width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"form_bar\">");
            html.append("<tbody>");
            if (this.showHeader) {
                html.append("<tr class=\"layout_sidebar_tab_row\">");
                html.append("<td align=\"left\" class=\"layout_sidebar_tab layout_sidebar_tab_back\" style=\"width:16px;\">");
                html.append("<img src=\"WEB-CORE/elements/richtext/images/objects-h.gif\" width=\"16\" height=\"17\">");
                html.append("</td>");
                html.append("<td align=\"").append(rtl ? "right" : "left").append("\" class=\"layout_sidebar_tab layout_sidebar_tab_back\" valign=\"middle\" nowrap>");
                html.append(this.getTranslationProcessor().translate("Properties"));
                html.append("</td>");
                html.append("<td align=").append(rtl ? "right" : "left").append(" valign=middle class=\"layout_sidebar_expandcollapse layout_sidebar_tab_back\">");
                html.append("<div id=\"groupingbutton\" onclick=\"pb.showPropertyGrouping()\" class=\"form_groupingbutton\" onmouseenter=\"this.style.backgroundColor='#F9F9F5';this.style.border='solid 1px #CECEC3';\" onmouseleave=\"this.style.backgroundColor='';this.style.border='';\"><img src=\"WEB-CORE/elements/richtext/images/grouping.gif\"><img src=\"WEB-CORE/elements/richtext/images/dropdownarrow.gif\"></div>");
                html.append("</td>");
                html.append("</tr>");
            }
            html.append("<tr class=\"form_merge\">");
            html.append("<td colspan=\"3\">");
            html.append("</td>");
            html.append("</tr>");
            String rowheight = "";
            html.append("<tr id=\"pb_objects_row\" style=\"height:").append(rowheight).append("px\">");
            html.append("<td colspan=\"3\" class=\"form_bar_parentcell\">");
            html.append("<table width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"form_bar_childtable\">");
            html.append("<tbody>");
            html.append("<tr>");
            html.append("<td valign=\"top\" align=\"left\" class=\"form_bar_childcell\" id=\"pb_properties_content_cell\" style=\"position:relative;\" nowrap>");
            html.append("<div align=\"left\" id=\"pb_properties_content\"  style=\"overflow-y:auto;overflow-x:auto;width:100%;height:100%;\">");
            this.setPropertyDefinition(this.propdeflist);
            html.append(PropertyBuilder.getPropertiesHtml(this.props, this.viewonly, this.xmldef, this.getConnectionId(), this.userConfig, this.getTranslationProcessor(), this.pageContext.getSession(), ProcessingUtil.createBindingsMap(null, this.getQueryProcessor(), this.getSDCProcessor(), null, null)));
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
                html.append("<div id=\"pb_buttons_content\"  style=\"overflow:auto;width:100%;height:100%;\">");
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
            if (this.renderStyle == RenderStyle.Element) {
                html.append("<textarea name=\"").append(this.fieldid).append("\" id=\"").append(this.fieldid).append("\" style=\"display:none;\">");
                html.append("</textarea>");
            }
            html.append(this.getEndScript(this.props, this.fieldid, this.callbackFunc, this.viewonly, this.onupdate, this.renderStyle));
        } else {
            this.logger.warn("XML file def is either empty or  properties is null.");
            html.append("<font color=\"red\">").append(this.getTranslationProcessor().translate("Could not load editor.")).append("</font>");
        }
        return html.toString();
    }

    public static String getPropertiesHtml(PropertyList props, String xmldef, String connectionId, TranslationProcessor tp, boolean primaryCall, HttpSession session, HashMap bindingMap) {
        return PropertyBuilder.getPropertiesHtml(props, xmldef, connectionId, null, tp, session, bindingMap);
    }

    public static String getPropertiesHtml(PropertyList props, String xmldef, String connectionId, PropertyList userConfig, TranslationProcessor tp, HttpSession session, HashMap bindingMap) {
        return PropertyBuilder.getPropertiesHtml(props, false, xmldef, connectionId, userConfig, tp, session, bindingMap);
    }

    public static String getPropertiesHtml(PropertyList props, boolean viewOnly, String xmlfile, String connectionId, PropertyList userConfig, TranslationProcessor tp, HttpSession session, HashMap bindingMap) {
        StringBuffer sb = new StringBuffer();
        if (connectionId != null && connectionId.length() > 0) {
            if (tp == null) {
                tp = new TranslationProcessor(connectionId);
            }
            com.labvantage.sapphire.pageelements.forms.PropertyBuilder.clearSession(session);
            Object defob = session.getAttribute(xmlfile + CACHE_OBJECTS_POST);
            if (defob != null && defob instanceof PropertyDefinitionList) {
                PropertyDefinitionList propertydeflist = (PropertyDefinitionList)defob;
                com.labvantage.sapphire.pageelements.forms.PropertyBuilder.renderProperties(propertydeflist, props, viewOnly, sb, connectionId, userConfig, tp, session, bindingMap);
            } else {
                com.labvantage.sapphire.pageelements.forms.PropertyBuilder.addPropertyMsg("Property definition not provided", sb, tp);
            }
        } else {
            com.labvantage.sapphire.pageelements.forms.PropertyBuilder.addPropertyMsg("No connection Id provided", sb, tp);
        }
        return sb.toString();
    }

    private StringBuffer getLeftBarStart() {
        String width = "100%";
        StringBuffer sb = new StringBuffer();
        sb.append("<td id=\"pb_leftbar\" class=\"form_leftbar\" style=\"display:block;width:").append(width).append(";height:100%;\">");
        return sb;
    }

    private String getButtonsHtml(boolean viewOnly) {
        Button but;
        StringBuffer sb = new StringBuffer();
        if (!viewOnly) {
            but = new Button(this.pageContext);
            but.setText(this.getTranslationProcessor().translate("OK"));
            but.setTip(this.getTranslationProcessor().translate("OK and return"));
            but.setId("pb_btn_ok");
            but.setImg("WEB-CORE/images/gif/Confirm.gif");
            but.setAction("pb.buttons.doOK()");
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
        but.setId("pb_btn_cancel");
        but.setImg("WEB-CORE/images/gif/Cancel.gif");
        but.setAction("pb.buttons.doCancel()");
        but.setWidth("100");
        sb.append(but.getHtml());
        return sb.toString();
    }

    private String getLeftBarEnd() {
        return "</td>";
    }

    public static enum RenderStyle {
        Popup,
        Embedded,
        Element;

    }
}

