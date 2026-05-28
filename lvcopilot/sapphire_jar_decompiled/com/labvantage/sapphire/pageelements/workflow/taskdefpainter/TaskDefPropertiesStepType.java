/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

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
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class TaskDefPropertiesStepType
extends BaseElement {
    public static final String JS_CLASS = "taskStepType";
    public static final String PAGEURL = "rc?command=file&file=WEB-CORE/elements/workflow/taskdefproperties_steptype.jsp";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_STEPID = "stepid";
    public static final String PROPERTY_STEPTYPEID = "steptypeid";
    public static final String PROPERTY_STEPTYPENODE = "steptypenode";
    public static final String PROPERTY_STEPTYPEOVERRIDES = "steptypeoverrides";
    public static final String PROPERTY_STEPTYPEMERGED = "steptypemerged";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_ADVANCED = "advanced";
    public static final String NODE = "_steptypeoverrides";
    private PropertyList userConfig;
    private boolean viewonly = false;
    private boolean advanced = false;
    private boolean devMode;
    private PropertyList props = null;
    private String stepid = "";
    private String steptypeid = "";
    private String steptypenode = "";
    private String dothis = "";

    public TaskDefPropertiesStepType(PageContext pageContext, PropertyList pageproperties) {
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
            this.props = null;
            this.logger.error("Could not set up properties: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    private void setUpProperties(PropertyList pagedata, HttpServletRequest request) throws Exception {
        String p;
        pagedata.setProperty("jsrequest", "exclude=properties");
        this.stepid = pagedata.getProperty(PROPERTY_STEPID, "");
        this.logger.debug("stepid = " + this.stepid);
        this.steptypeid = pagedata.getProperty(PROPERTY_STEPTYPEID, "");
        this.logger.debug("steptypeid = " + this.steptypeid);
        this.steptypenode = pagedata.getProperty(PROPERTY_STEPTYPENODE, "");
        this.logger.debug("steptypenode = " + this.steptypenode);
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("viewonly = " + this.viewonly);
        this.advanced = pagedata.getProperty(PROPERTY_ADVANCED, "N").equalsIgnoreCase("Y");
        this.logger.debug("advanced = " + this.advanced);
        this.dothis = pagedata.getProperty("dothis");
        if (this.stepid.length() > 0 && (p = pagedata.getProperty(PROPERTY_PROPERTIES, "")).length() > 0) {
            try {
                this.props = new PropertyList(new JSONObject(p));
            }
            catch (Exception e) {
                this.props = null;
                this.logger.warn("Invalid property definition provided.");
            }
        }
        this.userConfig = RequestContext.getInstance(request).getPropertyList("userconfig");
        if (this.userConfig == null) {
            throw new SapphireException("User configuration could not be obtained.");
        }
    }

    private StringBuffer getScriptAndStyle() {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/workflow/scripts/taskpropssteptype.js\"></script>");
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
        html.append(JS_CLASS).append(".steptypeid='").append(this.steptypeid).append("';");
        html.append(JS_CLASS).append(".steptypenode='").append(this.steptypenode).append("';");
        html.append(JS_CLASS).append(".advanced='").append(this.advanced).append("';");
        if (this.dothis.equalsIgnoreCase("save")) {
            html.append(JS_CLASS).append(".save_Callback();");
        } else if (this.dothis.equalsIgnoreCase("store")) {
            html.append(JS_CLASS).append(".store_Callback();");
        } else if (this.dothis.length() > 0) {
            html.append(JS_CLASS).append(".propertyChange(null);");
        }
        html.append("</script>");
        return html;
    }

    protected static PropertyList getMergedProperties(PropertyList overrides, String stepid, String steptypeid, String steptypenode, String connectionId) {
        try {
            PropertyTree tree = TaskDefPropertiesStepType.getTree(overrides, stepid, steptypeid, steptypenode, connectionId);
            Node stepTypeNode = tree.getNode(NODE);
            return tree.getNodePropertyList(NODE, true);
        }
        catch (Exception e) {
            return null;
        }
    }

    protected static PropertyTree getTree(PropertyList overrides, String stepid, String steptypeid, String steptypenode, String connectionId) throws SapphireException {
        PropertyTree tree = null;
        Node stepTypeNode = null;
        WebAdminProcessor webadminProcessor = new WebAdminProcessor(connectionId);
        try {
            tree = webadminProcessor.getPropertyTree(steptypeid);
            tree.setId(steptypeid);
            Node overrideNode = tree.getNode(steptypenode);
            stepTypeNode = tree.createNode(NODE, overrideNode);
            if (overrides != null) {
                stepTypeNode.setPropertyList(overrides);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to obtain step node.", e);
        }
        return tree;
    }

    private static PropertyList[] renderHtml(StringBuffer html, PropertyList stepprops, String stepid, String steptypeid, String steptypenode, boolean viewonly, boolean advanced, PageContext pageContext) throws SapphireException {
        PropertyList out = null;
        PropertyList merged = null;
        if (stepprops != null) {
            TranslationProcessor tp = new TranslationProcessor(pageContext);
            if (advanced) {
                html.append("<a style=\"font-size: 8pt;margin-top: 0.5em;margin-bottom: 0.3em;\" href=\"javascript:").append(JS_CLASS).append(".hideAdvanced()\">").append(tp.translate("Hide Advanced")).append("</a>");
            } else {
                html.append("<a style=\"font-size: 8pt;margin-top: 0.5em;margin-bottom: 0.3em;\" href=\"javascript:").append(JS_CLASS).append(".showAdvanced()\">").append(tp.translate("Show Advanced")).append("</a>");
            }
            if (stepprops.getPropertyList(PROPERTY_STEPTYPEOVERRIDES) == null) {
                stepprops.setProperty(PROPERTY_STEPTYPEOVERRIDES, new PropertyList());
            }
            PropertyTree tree = TaskDefPropertiesStepType.getTree(stepprops.getPropertyList(PROPERTY_STEPTYPEOVERRIDES), stepid, steptypeid, steptypenode, RequestContext.getRequestContext(pageContext).getConnectionId());
            try {
                Node stepTypeNode = tree.getNode(NODE);
                PropertyList defprops = tree.getNodePropertyList(steptypenode, true).getPropertyList("stepdef");
                PropertyDefinitionList propdeflist = tree.getPropertyDefinitionList();
                if (propdeflist.getPropertyDef("stepdef") != null) {
                    tree.getPropertyDefinitionList().remove(propdeflist.getPropertyDef("stepdef"));
                }
                if (defprops != null) {
                    if (defprops.getProperty("hidesteptypeprops", "N").equalsIgnoreCase("Y") && propdeflist.getPropertyDef("steptype") != null) {
                        tree.getPropertyDefinitionList().remove(propdeflist.getPropertyDef("steptype"));
                        propdeflist.setColor("#FFFFFF;display:none;");
                    }
                    if (defprops.getProperty("hidestepprops", "N").equalsIgnoreCase("Y") && propdeflist.getPropertyDef("stepprops") != null) {
                        tree.getPropertyDefinitionList().remove(propdeflist.getPropertyDef("stepprops"));
                        propdeflist.setColor("#FFFFFF;display:none;");
                    }
                }
                HashMap<String, String> inputs = new HashMap<String, String>(4);
                inputs.put(PROPERTY_STEPID, stepid);
                inputs.put(PROPERTY_STEPTYPEID, steptypeid);
                inputs.put(PROPERTY_STEPTYPENODE, steptypenode);
                inputs.put(PROPERTY_PROPERTIES, stepprops.toJSONString(false));
                inputs.put(PROPERTY_VIEWONLY, viewonly ? "Y" : "N");
                PropertyTreeDisplayOptions options = new PropertyTreeDisplayOptions();
                options.showAdvanced = advanced;
                options.collectionitemcopy = true;
                options.collectionitempaste = true;
                options.showModules = false;
                html.append(PropertyTreeRenderer.getPropertyEditorPage(pageContext, tree, NODE, PAGEURL, inputs, options));
                out = stepTypeNode.getPropertyList();
                merged = tree.getNodePropertyList(NODE, true);
            }
            catch (Exception e) {
                throw new SapphireException("Failed to render editor.", e);
            }
        } else {
            html.append("No properties provided.");
        }
        return new PropertyList[]{out, merged};
    }

    protected static PropertyList getProperties(PropertyList props, String stepid, String steptypeid, String steptypenode, PageContext pageContext, Logger logger) {
        PropertyList ret = new PropertyList();
        try {
            PropertyList[] pa = TaskDefPropertiesStepType.renderHtml(new StringBuffer(), props, stepid, steptypeid, steptypenode, false, true, pageContext);
            ret.setProperty(PROPERTY_STEPTYPEOVERRIDES, pa[0]);
            ret.setProperty(PROPERTY_STEPTYPEMERGED, pa[1]);
        }
        catch (Exception e) {
            ret.setProperty(PROPERTY_STEPTYPEOVERRIDES, new PropertyList());
            ret.setProperty(PROPERTY_STEPTYPEMERGED, new PropertyList());
            logger.error("Failed to create propertylist", e);
        }
        return ret;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        html.append(this.getScriptAndStyle());
        PropertyList overrides = null;
        PropertyList merged = null;
        try {
            PropertyList[] ret = TaskDefPropertiesStepType.renderHtml(html, this.props, this.stepid, this.steptypeid, this.steptypenode, this.viewonly, this.advanced, this.pageContext);
            overrides = ret[0];
            merged = ret[1];
        }
        catch (Exception e) {
            html.append("Failed to render editor.");
        }
        if (overrides == null) {
            overrides = new PropertyList();
        }
        html.append(this.getEndScript(overrides, merged));
        return html.toString();
    }
}

