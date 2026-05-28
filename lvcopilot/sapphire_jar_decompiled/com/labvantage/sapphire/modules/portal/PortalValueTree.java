/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.portal;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.propertytree.PropertyTreeDisplayOptions;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.webadmin.PropertyTreeRenderer;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeDefHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.JsonObject;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class PortalValueTree
extends BaseElement {
    public static final String PAGEURL = "rc?command=file&file=WEB-CORE/modules/portal/portal_valuetree.jsp";
    public static final String JS_CLASS = "portalValueTree";
    public static final String PROPERTY_PORTALID = "portalid";
    public static final String PROPERTY_VALUETREE = "valuetree";
    public static final String PROPERTY_PRODUCTVALUETREE = "productvaluetree";
    public static final String PROPERTY_COMPONENTVALUETREE = "componentvaluetree";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_ADVANCED = "advanced";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_OVERRIDES = "overrides";
    public static final String PROPERTY_MERGED = "merged";
    public static final String NODE_PVT = "_portal_pvt";
    public static final String NODE_CVT = "_portal_cvt";
    public static final String NODE_VT = "_portal_vt";
    public static final String RESOURCE_FILE = "portaldef.xml";
    private boolean devMode = false;
    private boolean isCompMode = false;
    private boolean compOverride = false;
    private String configCompCode = "";
    private PropertyList userConfig;
    private boolean viewonly = false;
    private PropertyList productvaluetree = null;
    private PropertyList componentvaluetree = null;
    private PropertyList valuetree = null;
    private String portalid = "";
    private String dothis = "";
    private String title = "";
    private boolean advanced = false;

    public PortalValueTree(PageContext pageContext) {
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
        block23: {
            this.portalid = pagedata.getProperty(PROPERTY_PORTALID, "");
            this.logger.debug("portalid = " + this.portalid);
            this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
            this.logger.debug("viewonly = " + this.viewonly);
            this.title = pagedata.getProperty(PROPERTY_TITLE, "");
            this.advanced = pagedata.getProperty(PROPERTY_ADVANCED, "N").equalsIgnoreCase("Y");
            if (this.portalid.length() == 0) {
                this.isCompMode = this.configCompCode.length() > 0;
            } else if (this.configCompCode.length() > 0) {
                String compcode = this.getQueryProcessor().getPreparedSqlDataSet("SELECT compcode FROM portal WHERE portalid=?", (Object[])new String[]{this.portalid}).getValue(0, "compcode");
                this.isCompMode = compcode.equals(this.configCompCode);
            }
            this.compOverride = !this.devMode && !this.isCompMode && this.configCompCode.length() > 0;
            this.dothis = pagedata.getProperty("dothis");
            if (this.portalid.length() > 0) {
                String p;
                block22: {
                    block21: {
                        p = pagedata.getProperty(PROPERTY_PRODUCTVALUETREE, "").trim();
                        if (p.length() > 0) {
                            try {
                                if (p.startsWith("{")) {
                                    this.productvaluetree = new PropertyList(new JSONObject(p));
                                    break block21;
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
                    if ((p = pagedata.getProperty(PROPERTY_COMPONENTVALUETREE, "").trim()).length() > 0) {
                        try {
                            if (p.startsWith("{")) {
                                this.componentvaluetree = new PropertyList(new JSONObject(p));
                                break block22;
                            }
                            this.componentvaluetree = new PropertyList();
                            this.componentvaluetree.setPropertyList(p);
                        }
                        catch (Exception e) {
                            this.componentvaluetree = null;
                            this.logger.warn("Invalid property definition provided.");
                        }
                    } else {
                        this.componentvaluetree = new PropertyList();
                    }
                }
                if ((p = pagedata.getProperty(PROPERTY_VALUETREE, "").trim()).length() > 0) {
                    try {
                        if (p.startsWith("{")) {
                            this.valuetree = new PropertyList(new JSONObject(p));
                            break block23;
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
                this.componentvaluetree = new PropertyList();
                this.productvaluetree = new PropertyList();
            }
        }
        this.userConfig = RequestContext.getInstance(request).getPropertyList("userconfig");
        if (this.userConfig == null) {
            throw new SapphireException("User configuration could not be obtained.");
        }
    }

    private static InputStream getResourceStream(String xmlfile, Class c) {
        URL fileurl = c.getResource(xmlfile);
        InputStream out = null;
        if (fileurl != null) {
            if (fileurl.getPath().contains(".jar!/")) {
                Trace.logInfo("Resource " + xmlfile + " is Jar resource.");
            } else {
                Trace.logInfo("Resource " + xmlfile + " is class resource.");
            }
            InputStream is = c.getResourceAsStream(xmlfile);
            if (is != null) {
                Trace.logDebug("Input stream obatined.");
                out = is;
            } else {
                Trace.logWarn("Could not load " + xmlfile + " resource. Input stream could not be created.");
            }
        } else {
            Trace.logWarn("Could not load " + xmlfile + " resource. File could not be found.");
        }
        return out;
    }

    public static PropertyList getPortalPropertyList(String prodValueTree, String compValueTree, String customValueTree) throws SapphireException {
        PropertyTree tree;
        PropertyList prodValueTreeProps = new PropertyList();
        PropertyList compValueTreeProps = new PropertyList();
        PropertyList customValueTreeProps = new PropertyList();
        if (prodValueTree != null && prodValueTree.length() > 0) {
            try {
                prodValueTreeProps.setPropertyList(prodValueTree);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (compValueTree != null && compValueTree.length() > 0) {
            try {
                compValueTreeProps.setPropertyList(compValueTree);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (customValueTree != null && customValueTree.length() > 0) {
            try {
                customValueTreeProps.setPropertyList(customValueTree, true);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if ((tree = PortalValueTree.getTree(prodValueTreeProps, compValueTreeProps, customValueTreeProps)) != null) {
            PropertyList merged = tree.getNodePropertyList(NODE_VT, true);
            return merged;
        }
        return null;
    }

    public static JsonObject getPortalProperties(PropertyList productvaluetreeoverrides, PropertyList componentvaluetreeoverrides, PropertyList valuetreeoverrides) throws SapphireException {
        PropertyTree tree = PortalValueTree.getTree(productvaluetreeoverrides, componentvaluetreeoverrides, valuetreeoverrides);
        if (tree != null) {
            PropertyList merged = tree.getNodePropertyList(NODE_VT, true);
            return merged.toSimpleJSON(tree.getPropertyDefinitionList());
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected static PropertyTree getTree(PropertyList productvaluetreeoverrides, PropertyList componentvaluetreeoverrides, PropertyList valuetreeoverrides) throws SapphireException {
        PropertyTree tree;
        block9: {
            tree = null;
            try {
                InputStream is = PortalValueTree.getResourceStream(RESOURCE_FILE, PortalValueTree.class);
                if (is != null) {
                    try {
                        tree = new PropertyTree();
                        PropertyTreeDefHandler handler = new PropertyTreeDefHandler(tree);
                        handler.setXMLString(FileUtil.getInputStreamString(is));
                        handler.setPrintStream(null);
                        SaxUtil.parseString(handler);
                        Node productNode = tree.createNode(NODE_PVT, null);
                        if (productvaluetreeoverrides != null) {
                            productNode.setPropertyList(productvaluetreeoverrides);
                        }
                        Node overrideNode = tree.getNode(NODE_PVT);
                        Node componentNode = tree.createNode(NODE_CVT, overrideNode);
                        if (componentvaluetreeoverrides != null) {
                            componentNode.setPropertyList(componentvaluetreeoverrides);
                        }
                        Node overrideNode2 = tree.getNode(NODE_CVT);
                        Node valueNode = tree.createNode(NODE_VT, overrideNode2);
                        if (valuetreeoverrides != null) {
                            valueNode.setPropertyList(valuetreeoverrides);
                        }
                        Logger.logDebug("Objects loaded from resource.");
                        break block9;
                    }
                    finally {
                        is.close();
                    }
                }
                Logger.logWarn("Property definition could be obtained");
            }
            catch (Exception e) {
                Logger.logWarn("Could not load xml resource. File could not be parsed. " + e.getMessage());
            }
        }
        return tree;
    }

    protected static PropertyList getProperties(PropertyList productvaluetreePropertyList, PropertyList componentvaluetreePropertyList, PropertyList valuetreePropertyList, String portalid, boolean devMode, boolean isCompMode, boolean compOverride, boolean advanced, PageContext pageContext, Logger logger) {
        PropertyList ret = new PropertyList();
        try {
            PropertyList[] pa = PortalValueTree.renderHtml(new StringBuffer(), productvaluetreePropertyList, componentvaluetreePropertyList, valuetreePropertyList, portalid, false, devMode, isCompMode, compOverride, advanced, pageContext);
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

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static PropertyList[] renderHtml(StringBuffer html, PropertyList productvaluetreePropertyList, PropertyList componentvaluetreePropertyList, PropertyList valuetreePropertyList, String portalid, boolean viewonly, boolean devMode, boolean isCompMode, boolean compOverride, boolean advanced, PageContext pageContext) throws SapphireException {
        PropertyList out = null;
        PropertyList merged = null;
        if (productvaluetreePropertyList != null && componentvaluetreePropertyList != null && valuetreePropertyList != null) {
            PropertyTree tree = PortalValueTree.getTree(productvaluetreePropertyList, componentvaluetreePropertyList, valuetreePropertyList);
            try {
                if (tree == null) return new PropertyList[]{out, merged};
                String nodeStr = devMode || isCompMode ? NODE_PVT : (compOverride ? NODE_CVT : NODE_VT);
                Node node = tree.getNode(nodeStr);
                PropertyDefinitionList propdeflist = tree.getPropertyDefinitionList();
                if (propdeflist.size() > 0) {
                    HashMap<String, String> inputs = new HashMap<String, String>(4);
                    inputs.put(PROPERTY_PORTALID, portalid);
                    inputs.put(PROPERTY_PRODUCTVALUETREE, productvaluetreePropertyList.toJSONString(true));
                    inputs.put(PROPERTY_COMPONENTVALUETREE, componentvaluetreePropertyList.toJSONString(true));
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
                    html.append(PropertyTreeRenderer.getPropertyEditorPage(pageContext, tree, nodeStr, PAGEURL, inputs, options));
                    out = node.getPropertyList();
                    merged = tree.getNodePropertyList(nodeStr, true);
                    return new PropertyList[]{out, merged};
                }
                html.append("No property definition to override.");
                return new PropertyList[]{out, merged};
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
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/modules/portal/scripts/portalvaluetree.js\"></script>");
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
        html.append(JS_CLASS).append(".portalid='").append(this.portalid).append("';");
        html.append(JS_CLASS).append(".productvaluetree=sapphire.util.propertyList.create(").append(this.productvaluetree != null ? this.productvaluetree.toJSONString(true, true) : "").append(");");
        html.append(JS_CLASS).append(".componentvaluetree=sapphire.util.propertyList.create(").append(this.componentvaluetree != null ? this.componentvaluetree.toJSONString(true, true) : "").append(");");
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
        html.append(this.getScriptAndStyle());
        PropertyList overrides = null;
        PropertyList merged = null;
        if (this.title.length() > 0) {
            html.append("<span style=\"font-size: 10pt;font-weight: bold;margin-top: 0.5em;margin-bottom: 0.3em;\">").append(this.title).append("</span>");
            html.append("&nbsp;&nbsp;");
        }
        if (this.advanced) {
            html.append("<a style=\"font-size: 8pt;margin-top: 0.5em;margin-bottom: 0.3em;\" href=\"javascript:parent.portalMaint.hideAdvanced()\">").append(this.getTranslationProcessor().translate("Hide Advanced")).append("</a>");
        } else {
            html.append("<a style=\"font-size: 8pt;margin-top: 0.5em;margin-bottom: 0.3em;\" href=\"javascript:parent.portalMaint.showAdvanced()\">").append(this.getTranslationProcessor().translate("Show Advanced")).append("</a>");
        }
        try {
            PropertyList[] ret = PortalValueTree.renderHtml(html, this.productvaluetree, this.componentvaluetree, this.valuetree, this.portalid, this.viewonly, this.devMode, this.isCompMode, this.compOverride, this.advanced, this.pageContext);
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
        return html.toString();
    }
}

