/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.instrument;

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
import sapphire.xml.PropertyList;

public class CapturerDefValueTree
extends BaseElement {
    public static final String PAGEURL = "rc?command=file&file=WEB-CORE/modules/instrument/capturertype_valuetree.jsp";
    public static final String JS_CLASS = "capturerDefValueTree";
    public static final String PROPERTY_INSTRUMENTMODELID = "instrumentmodelid";
    public static final String PROPERTY_INSTRUMENTTYPEID = "instrumenttypeid";
    public static final String PROPERTY_INSTRUMENTID = "instrumentid";
    public static final String PROPERTY_PROPERTYTREEID = "collectorpropertytreeid";
    public static final String PROPERTY_EXTENDNODEID = "collectorextendnodeid";
    public static final String PROPERTY_VALUETREE = "collectorvaluetree";
    public static final String PROPERTY_MODELVALUETREE = "modelvaluetree";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_ADVANCED = "advanced";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_OVERRIDES = "overrides";
    public static final String PROPERTY_MERGED = "merged";
    public static final String PROPERTY_ISINSTRUMENT = "isinstrument";
    public static final String NODE_IM_VT = "_capturer_im_vt";
    public static final String NODE_VT = "_capturer_vt";
    private boolean devMode = false;
    private PropertyList userConfig;
    private boolean viewonly = false;
    private PropertyList valuetree = null;
    private PropertyList modelvaluetree = null;
    private String instrumentmodelid = "";
    private String instrumenttypeid = "";
    private String instrumentid = "";
    private String propertytreeid = "";
    private String extendnodeid = "";
    private String dothis = "";
    private String title = "";
    private boolean advanced = false;
    private boolean isinstrument = false;

    public CapturerDefValueTree(PageContext pageContext) {
        this.setPageContext(pageContext);
        try {
            RequestContext rc = RequestContext.getRequestContext(pageContext);
            ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
            try {
                this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
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
        this.instrumentmodelid = pagedata.getProperty(PROPERTY_INSTRUMENTMODELID, "");
        this.logger.debug("instrumentmodelid = " + this.instrumentmodelid);
        this.instrumenttypeid = pagedata.getProperty(PROPERTY_INSTRUMENTTYPEID, "");
        this.logger.debug("instrumenttypeid = " + this.instrumenttypeid);
        this.instrumentid = pagedata.getProperty(PROPERTY_INSTRUMENTID, "");
        this.logger.debug("instrumentid = " + this.instrumentid);
        this.propertytreeid = pagedata.getProperty(PROPERTY_PROPERTYTREEID, "");
        this.logger.debug("collectorpropertytreeid = " + this.propertytreeid);
        this.extendnodeid = pagedata.getProperty(PROPERTY_EXTENDNODEID, "");
        this.logger.debug("collectorextendnodeid = " + this.extendnodeid);
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("viewonly = " + this.viewonly);
        this.title = pagedata.getProperty(PROPERTY_TITLE, "");
        this.advanced = pagedata.getProperty(PROPERTY_ADVANCED, "N").equalsIgnoreCase("Y");
        this.isinstrument = pagedata.getProperty(PROPERTY_ISINSTRUMENT, "n").equalsIgnoreCase("y");
        this.dothis = pagedata.getProperty("dothis");
        if (this.instrumentmodelid.length() > 0 && this.instrumenttypeid.length() > 0) {
            this.valuetree = this.getTreeValue(pagedata, PROPERTY_VALUETREE);
            this.modelvaluetree = new PropertyList();
        } else if (this.instrumentid.length() > 0) {
            this.valuetree = this.getTreeValue(pagedata, PROPERTY_VALUETREE);
            this.modelvaluetree = this.getTreeValue(pagedata, PROPERTY_MODELVALUETREE);
        } else {
            this.valuetree = new PropertyList();
            this.modelvaluetree = new PropertyList();
        }
        this.userConfig = RequestContext.getInstance(request).getPropertyList("userconfig");
        if (this.userConfig == null) {
            throw new SapphireException("User configuration could not be obtained.");
        }
    }

    protected static PropertyTree getTree(PropertyList modelvaluetreeoverrides, PropertyList valuetreeoverrides, String instrumentmodelid, String propertytreeid, String extendnodeid, String connectionId) throws SapphireException {
        PropertyTree tree = null;
        WebAdminProcessor webadminProcessor = new WebAdminProcessor(connectionId);
        try {
            tree = webadminProcessor.getPropertyTree(propertytreeid);
            tree.setId(propertytreeid);
            Node overrideNode = tree.getNode(extendnodeid);
            Node modelvalueNode = tree.createNode(NODE_IM_VT, overrideNode);
            if (modelvaluetreeoverrides != null) {
                modelvalueNode.setPropertyList(modelvaluetreeoverrides);
            }
            Node vt_Node = tree.getNode(NODE_IM_VT);
            Node valueNode = tree.createNode(NODE_VT, vt_Node);
            if (valuetreeoverrides != null) {
                valueNode.setPropertyList(valuetreeoverrides);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to obtain step node.", e);
        }
        return tree;
    }

    protected static PropertyList getProperties(PropertyList valuetreePropertyList, PropertyList modelvaluetreePropertyList, String instrumentmodelid, String propertytreeid, String extendnodeid, boolean devMode, boolean advanced, PageContext pageContext, Logger logger) {
        PropertyList ret = new PropertyList();
        try {
            PropertyList[] pa = CapturerDefValueTree.renderHtml(new StringBuffer(), valuetreePropertyList, modelvaluetreePropertyList, instrumentmodelid, propertytreeid, extendnodeid, false, devMode, advanced, pageContext);
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

    private static PropertyList[] renderHtml(StringBuffer html, PropertyList valuetreePropertyList, PropertyList modelvaluetreePropertyList, String instrumentmodelid, String propertytreeid, String extendnodeid, boolean viewonly, boolean devMode, boolean advanced, PageContext pageContext) throws SapphireException {
        PropertyList out = null;
        PropertyList merged = null;
        if (valuetreePropertyList != null) {
            PropertyTree tree = CapturerDefValueTree.getTree(modelvaluetreePropertyList, valuetreePropertyList, instrumentmodelid, propertytreeid, extendnodeid, RequestContext.getRequestContext(pageContext).getConnectionId());
            try {
                Node capturerNode = tree.getNode(NODE_VT);
                PropertyDefinitionList propdeflist = tree.getPropertyDefinitionList();
                if (propdeflist.size() > 0) {
                    HashMap<String, String> inputs = new HashMap<String, String>(4);
                    inputs.put(PROPERTY_INSTRUMENTMODELID, instrumentmodelid);
                    inputs.put(PROPERTY_PROPERTYTREEID, propertytreeid);
                    inputs.put(PROPERTY_EXTENDNODEID, extendnodeid);
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
                    html.append(PropertyTreeRenderer.getPropertyEditorPage(pageContext, tree, NODE_VT, PAGEURL, inputs, options));
                    out = capturerNode.getPropertyList();
                    merged = tree.getNodePropertyList(NODE_VT, true);
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
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/modules/instrument/scripts/capturerdefvaluetree.js\"></script>");
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
        html.append(JS_CLASS).append(".instrumentmodelid='").append(this.instrumentmodelid).append("';");
        html.append(JS_CLASS).append(".instrumenttypeid='").append(this.instrumenttypeid).append("';");
        html.append(JS_CLASS).append(".instrumentid='").append(this.instrumentid).append("';");
        html.append(JS_CLASS).append(".isinstrument='").append(this.instrumentid != null && this.instrumentid.length() > 0 ? "Y" : "N").append("';");
        html.append(JS_CLASS).append(".collectorpropertytreeid='").append(this.propertytreeid).append("';");
        html.append(JS_CLASS).append(".collectorextendnodeid='").append(this.extendnodeid).append("';");
        html.append(JS_CLASS).append(".collectorvaluetree=sapphire.util.propertyList.create(").append(this.valuetree != null ? this.valuetree.toJSONString(true, true) : "").append(");");
        html.append(JS_CLASS).append(".modelvaluetree=sapphire.util.propertyList.create(").append(this.modelvaluetree != null ? this.modelvaluetree.toJSONString(true, true) : "").append(");");
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
                html.append("<a style=\"font-size: 8pt;margin-top: 0.5em;margin-bottom: 0.3em;\" href=\"javascript:parent.pt.hideAdvanced()\">").append(this.getTranslationProcessor().translate("Hide Advanced")).append("</a>");
            } else {
                html.append("<a style=\"font-size: 8pt;margin-top: 0.5em;margin-bottom: 0.3em;\" href=\"javascript:parent.pt.showAdvanced()\">").append(this.getTranslationProcessor().translate("Show Advanced")).append("</a>");
            }
            try {
                PropertyList[] ret = CapturerDefValueTree.renderHtml(html, this.valuetree, this.modelvaluetree, this.instrumentmodelid, this.propertytreeid, this.extendnodeid, this.viewonly, this.devMode, this.advanced, this.pageContext);
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
            html.append(this.getTranslationProcessor().translate("Please provide a propertytreeid and extendnodeid for the InstrumentModel."));
        }
        return html.toString();
    }

    private PropertyList getTreeValue(PropertyList pagedata, String treeName) {
        PropertyList treeValue;
        block5: {
            String p = pagedata.getProperty(treeName, "").trim();
            if (p.length() > 0) {
                try {
                    if (p.startsWith("{")) {
                        treeValue = new PropertyList(new JSONObject(p));
                        break block5;
                    }
                    treeValue = new PropertyList();
                    treeValue.setPropertyList(p);
                }
                catch (Exception e) {
                    treeValue = null;
                    this.logger.warn("Invalid property definition provided.");
                }
            } else {
                treeValue = new PropertyList();
            }
        }
        return treeValue;
    }
}

