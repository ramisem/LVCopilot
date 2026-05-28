/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.dashboard;

import com.labvantage.sapphire.admin.propertytree.PropertyTreeDisplayOptions;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.webadmin.PropertyTreeRenderer;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.Logger;
import sapphire.util.SafeHTML;
import sapphire.xml.PropertyList;

public class GizmoDefValueTree
extends BaseElement {
    public static final String PAGEURL = "rc?command=file&file=WEB-CORE/modules/dashboard/gizmodeflist_valuetree.jsp";
    public static final String JS_CLASS = "gizmoDefValueTree";
    public static final String PROPERTY_GIZMODEFID = "gizmodefid";
    public static final String PROPERTY_PROPERTYTREEID = "propertytreeid";
    public static final String PROPERTY_EXTENDNODEID = "extendnodeid";
    public static final String PROPERTY_VALUETREE = "valuetree";
    public static final String PROPERTY_PRODUCTVALUETREE = "productvaluetree";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_ADVANCED = "advanced";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_OVERRIDES = "overrides";
    public static final String PROPERTY_MERGED = "merged";
    public static final String NODE_PVT = "_gizmodef_pvt";
    public static final String NODE_VT = "_gizmodef_vt";
    private boolean devMode = false;
    private boolean isCompMode = false;
    private String configCompCode = "";
    private PropertyList userConfig;
    private boolean viewonly = false;
    private PropertyList productvaluetree = null;
    private PropertyList valuetree = null;
    private String gizmodefid = "";
    private String propertytreeid = "";
    private String extendnodeid = "";
    private String dothis = "";
    private String title = "";
    private boolean advanced = false;

    public GizmoDefValueTree(PageContext pageContext) {
        this.setPageContext(pageContext);
        try {
            RequestContext rc = RequestContext.getRequestContext(pageContext);
            ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
            try {
                this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
                this.configCompCode = this.devMode ? "" : config.getSysConfigProperty("compcode");
            }
            catch (Exception e) {
                this.devMode = false;
            }
            this.setUpProperties(rc.getPropertyList(), (HttpServletRequest)pageContext.getRequest());
        }
        catch (Exception e) {
            this.logger.error("Could not set up properties: " + e.getMessage(), e);
        }
    }

    private void setUpProperties(PropertyList pagedata, HttpServletRequest request) throws Exception {
        block17: {
            this.gizmodefid = pagedata.getProperty(PROPERTY_GIZMODEFID, "");
            this.logger.debug("gizmodefid = " + this.gizmodefid);
            this.propertytreeid = pagedata.getProperty(PROPERTY_PROPERTYTREEID, "");
            this.logger.debug("propertytreeid = " + this.propertytreeid);
            this.extendnodeid = pagedata.getProperty(PROPERTY_EXTENDNODEID, "");
            this.logger.debug("extendnodeid = " + this.extendnodeid);
            this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
            this.logger.debug("viewonly = " + this.viewonly);
            this.title = pagedata.getProperty(PROPERTY_TITLE, "");
            this.advanced = pagedata.getProperty(PROPERTY_ADVANCED, "N").equalsIgnoreCase("Y");
            if (this.gizmodefid.length() == 0) {
                this.isCompMode = this.configCompCode.length() > 0;
            } else if (this.configCompCode.length() > 0) {
                String compcode = this.getQueryProcessor().getPreparedSqlDataSet("SELECT compcode FROM gizmodef WHERE gizmodefid=?", (Object[])new String[]{this.gizmodefid}).getValue(0, "compcode");
                this.isCompMode = compcode.equals(this.configCompCode);
            }
            this.dothis = pagedata.getProperty("dothis");
            if (this.gizmodefid.length() > 0) {
                String p;
                block16: {
                    p = pagedata.getProperty(PROPERTY_PRODUCTVALUETREE, "").trim();
                    if (p.length() > 0) {
                        try {
                            if (p.startsWith("{")) {
                                this.productvaluetree = new PropertyList(new JSONObject(p));
                                break block16;
                            }
                            this.productvaluetree = new PropertyList();
                            this.productvaluetree.setPropertyList(p);
                        }
                        catch (Exception e) {
                            this.productvaluetree = null;
                            this.logger.warn("Invalid property definition provided.");
                        }
                    } else {
                        this.productvaluetree = new PropertyList();
                    }
                }
                if ((p = pagedata.getProperty(PROPERTY_VALUETREE, "").trim()).length() > 0) {
                    try {
                        if (p.startsWith("{")) {
                            this.valuetree = new PropertyList(new JSONObject(p));
                            break block17;
                        }
                        this.valuetree = new PropertyList();
                        this.valuetree.setPropertyList(p);
                    }
                    catch (Exception e) {
                        this.valuetree = null;
                        this.logger.warn("Invalid property definition provided.");
                    }
                } else {
                    this.valuetree = new PropertyList();
                }
            } else {
                this.valuetree = new PropertyList();
                this.productvaluetree = new PropertyList();
            }
        }
        this.userConfig = RequestContext.getInstance(request).getPropertyList("userconfig");
        if (this.userConfig == null) {
            throw new SapphireException("User configuration could not be obtained.");
        }
    }

    protected static PropertyTree getTree(PropertyList productvaluetreeoverrides, PropertyList valuetreeoverrides, String gizmodefid, String propertytreeid, String extendnodeid, String connectionId) throws SapphireException {
        PropertyTree tree = null;
        Node productNode = null;
        Node valueNode = null;
        WebAdminProcessor webadminProcessor = new WebAdminProcessor(connectionId);
        try {
            tree = webadminProcessor.getPropertyTree(propertytreeid);
            tree.setId(propertytreeid);
            Node overrideNode = tree.getNode(extendnodeid);
            productNode = tree.createNode(NODE_PVT, overrideNode);
            if (productvaluetreeoverrides != null) {
                productNode.setPropertyList(productvaluetreeoverrides);
            }
            Node overrideNode2 = tree.getNode(NODE_PVT);
            valueNode = tree.createNode(NODE_VT, overrideNode2);
            if (valuetreeoverrides != null) {
                valueNode.setPropertyList(valuetreeoverrides);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to obtain step node.", e);
        }
        return tree;
    }

    protected static PropertyList getProperties(PropertyList productvaluetreePropertyList, PropertyList valuetreePropertyList, String gizmodefid, String propertytreeid, String extendnodeid, boolean devMode, boolean isCompMode, boolean advanced, PageContext pageContext, Logger logger) {
        PropertyList ret = new PropertyList();
        try {
            PropertyList[] pa = GizmoDefValueTree.renderHtml(new StringBuffer(), productvaluetreePropertyList, valuetreePropertyList, gizmodefid, propertytreeid, extendnodeid, false, devMode, isCompMode, advanced, pageContext);
            ret.setProperty(PROPERTY_OVERRIDES, pa[0]);
            ret.setProperty(PROPERTY_MERGED, pa[1]);
        }
        catch (Exception e) {
            ret.setProperty(PROPERTY_OVERRIDES, new PropertyList());
            ret.setProperty(PROPERTY_MERGED, new PropertyList());
            logger.error("Failed to create propertylist", e);
        }
        return ret;
    }

    private static PropertyList[] renderHtml(StringBuffer html, PropertyList productvaluetreePropertyList, PropertyList valuetreePropertyList, String gizmodefid, String propertytreeid, String extendnodeid, boolean viewonly, boolean devMode, boolean isCompMode, boolean advanced, PageContext pageContext) throws SapphireException {
        PropertyList out = null;
        PropertyList merged = null;
        if (productvaluetreePropertyList != null && valuetreePropertyList != null) {
            PropertyTree tree = GizmoDefValueTree.getTree(productvaluetreePropertyList, valuetreePropertyList, gizmodefid, propertytreeid, extendnodeid, RequestContext.getRequestContext(pageContext).getConnectionId());
            try {
                Node gizmoNode = tree.getNode(devMode || isCompMode ? NODE_PVT : NODE_VT);
                PropertyDefinitionList propdeflist = tree.getPropertyDefinitionList();
                if (propdeflist.getPropertyDef("gizmoprops") != null) {
                    PropertyDefinitionList dashboardonly = propdeflist.getPropertyDef("gizmoprops").getPropertyDefinitionList();
                    if (dashboardonly.getPropertyDef("tabid") != null) {
                        dashboardonly.remove(dashboardonly.getPropertyDef("tabid"));
                    }
                    if (dashboardonly.getPropertyDef("visible") != null) {
                        dashboardonly.remove(dashboardonly.getPropertyDef("visible"));
                    }
                    if (dashboardonly.getPropertyDef("height") != null) {
                        dashboardonly.remove(dashboardonly.getPropertyDef("height"));
                    }
                    if (dashboardonly.getPropertyDef("width") != null) {
                        dashboardonly.remove(dashboardonly.getPropertyDef("width"));
                    }
                    if (dashboardonly.getPropertyDef("resizable") != null) {
                        dashboardonly.remove(dashboardonly.getPropertyDef("resizable"));
                    }
                }
                if ((propdeflist = tree.getPropertyDefinitionList()).size() > 0) {
                    HashMap<String, String> inputs = new HashMap<String, String>(4);
                    inputs.put(PROPERTY_GIZMODEFID, gizmodefid);
                    inputs.put(PROPERTY_PROPERTYTREEID, propertytreeid);
                    inputs.put(PROPERTY_EXTENDNODEID, extendnodeid);
                    inputs.put(PROPERTY_PRODUCTVALUETREE, productvaluetreePropertyList.toJSONString(true));
                    inputs.put(PROPERTY_VALUETREE, valuetreePropertyList.toJSONString(true));
                    inputs.put(PROPERTY_VIEWONLY, viewonly ? "Y" : "N");
                    PropertyTreeDisplayOptions options = new PropertyTreeDisplayOptions();
                    options.showAdvanced = advanced;
                    if (viewonly) {
                        options.readonly = true;
                    }
                    options.collectionitemcopy = true;
                    options.collectionitempaste = true;
                    if (devMode) {
                        options.showModules = true;
                    }
                    html.append(PropertyTreeRenderer.getPropertyEditorPage(pageContext, tree, devMode || isCompMode ? NODE_PVT : NODE_VT, PAGEURL, inputs, options));
                    out = gizmoNode.getPropertyList();
                    merged = tree.getNodePropertyList(devMode || isCompMode ? NODE_PVT : NODE_VT, true);
                }
                html.append("No property definition to override.");
            }
            catch (Exception e) {
                throw new SapphireException("Failed to render editor.", e);
            }
        } else {
            html.append("No properties could be found.");
        }
        return new PropertyList[]{out, merged};
    }

    private StringBuffer getScriptAndStyle() {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/modules/dashboard/scripts/gizmodefvaluetree.js\"></script>");
        html.append(HttpUtil.getCoreStyleSheets(false, this.pageContext));
        html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/modules/webadmin/webadmin.css", this.pageContext) + "\" type=\"text/css\">");
        html.append("<style>");
        if (this.browser.isIE()) {
            html.append("\nbody{overflow:auto !important;}");
        }
        html.append("\ntable[border=\"1\"]{border:solid 1px #B0C4DE;}");
        html.append("\n.task_field_dragover{border:solid 2px #FFD691;background-color:#FFF2BE;}");
        if (!this.browser.isIE()) {
            html.append("#layout_maintable{table-layout:fixed;width:100%;height:100%;}");
        }
        html.append("</style>");
        return html;
    }

    private StringBuffer getEndScript(PropertyList overrides, PropertyList merged) {
        StringBuffer html = new StringBuffer();
        html.append("<script>");
        html.append("function propertyChange(e){");
        html.append(JS_CLASS).append(".propertyChange(e);");
        html.append("}");
        html.append("function checkunload(){");
        html.append("return ").append(JS_CLASS).append(".checkunload();");
        html.append("}");
        html.append("function bodykeydown(){");
        html.append("return ").append(JS_CLASS).append(".bodykeydown();");
        html.append("}");
        html.append("document.body.onkeydown =  ").append(JS_CLASS).append(".bodykeydown;");
        html.append("document.body.onbeforeunload = ").append(JS_CLASS).append(".checkunload;");
        html.append(JS_CLASS).append(".viewonly=").append(this.viewonly).append(";");
        html.append(JS_CLASS).append(".gizmodefid='").append(SafeHTML.encodeForJavaScript(this.gizmodefid)).append("';");
        html.append(JS_CLASS).append(".propertytreeid='").append(SafeHTML.encodeForJavaScript(this.propertytreeid)).append("';");
        html.append(JS_CLASS).append(".extendnodeid='").append(SafeHTML.encodeForJavaScript(this.extendnodeid)).append("';");
        html.append(JS_CLASS).append(".productvaluetree=sapphire.util.propertyList.create(").append(this.productvaluetree != null ? this.productvaluetree.toJSONString(true, true) : "").append(");");
        html.append(JS_CLASS).append(".valuetree=sapphire.util.propertyList.create(").append(this.valuetree != null ? this.valuetree.toJSONString(true, true) : "").append(");");
        if (this.dothis.equalsIgnoreCase("save")) {
            html.append(JS_CLASS).append(".propertyChange(null);");
            html.append(JS_CLASS).append(".save_Callback();");
        } else if (this.dothis.equalsIgnoreCase("store")) {
            html.append(JS_CLASS).append(".propertyChange(null);");
            html.append(JS_CLASS).append(".store_Callback();");
        } else if (this.dothis.length() > 0) {
            html.append(JS_CLASS).append(".propertyChange(null);");
        }
        html.append("</script>");
        return html;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.propertytreeid.length() > 0 && this.extendnodeid.length() > 0) {
            html.append(this.getScriptAndStyle());
            PropertyList overrides = null;
            PropertyList merged = null;
            if (this.title.length() > 0) {
                html.append("<span style=\"font-size: 10pt;font-weight: bold;margin-top: 0.5em;margin-bottom: 0.3em;\">").append(this.title).append("</span>");
                html.append("&nbsp;&nbsp;");
            }
            if (this.advanced) {
                html.append("<a style=\"font-size: 8pt;margin-top: 0.5em;margin-bottom: 0.3em;\" href=\"javascript:parent.gtm.hideAdvanced()\">").append(this.getTranslationProcessor().translate("Hide Advanced")).append("</a>");
            } else {
                html.append("<a style=\"font-size: 8pt;margin-top: 0.5em;margin-bottom: 0.3em;\" href=\"javascript:parent.gtm.showAdvanced()\">").append(this.getTranslationProcessor().translate("Show Advanced")).append("</a>");
            }
            try {
                PropertyList[] ret = GizmoDefValueTree.renderHtml(html, this.productvaluetree, this.valuetree, this.gizmodefid, this.propertytreeid, this.extendnodeid, this.viewonly, this.devMode, this.isCompMode, this.advanced, this.pageContext);
                overrides = ret[0];
                merged = ret[1];
            }
            catch (Exception e) {
                html.append("Failed to render editor.");
            }
            if (overrides == null) {
                overrides = new PropertyList();
            }
            if (merged == null) {
                merged = new PropertyList();
            }
            html.append(this.getEndScript(overrides, merged));
        } else {
            html.append(this.getTranslationProcessor().translate("Please provide a propertytreeid and extendnodeid for the Gizmo."));
        }
        return html.toString();
    }
}

