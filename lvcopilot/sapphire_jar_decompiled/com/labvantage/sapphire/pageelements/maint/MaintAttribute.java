/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.Attributes;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.pageelements.maint.Maint;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.tagext.QueryData;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MaintAttribute
extends BaseElement {
    protected static final String DEFAULT_GROUP = "NA";
    protected static final String PROPERTY_SDCID = "sdcid";
    private static final String PROPERTY_SHOWINSTANCE = "showinstance";
    public static final String PROPERTY_ATTRIBUTEDATA = "attributedata";
    public static final String PROPERTY_ELEMENTID = "elementid";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_MODE = "mode";
    public static final String PROPERTY_INSTRUCTIONFLAG = "instructionflag";
    public static final String PROPERTY_ATTRIBUTEID = "attributeid";
    public static final String PROPERTY_ATTRIBUTESDCID = "attributesdcid";
    public static final String PROPERTY_ATTRIBUTEINSTANCE = "attributeinstance";
    public static final String PROPERTY_RSETID = "rsetid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_SELECTED = "selected";
    public static final String PROPERTY_MOVE = "move";
    public static final String PROPERTY_COLLAPSEGROUPS = "collapsegroups";
    public static final String PROPERTYHANDLER = "com.labvantage.sapphire.pageelements.maint.MaintAttributePropertyHandler";
    private StringBuffer script = null;
    public static final String JS_OBJECT = "maintAttribute";
    public static final String JS_SRC = "WEB-CORE/elements/attributes/scripts/maintattribute.js";
    private static final String JS_CONTAINER = "__attributeContainer_";
    public static final String PROPERTYHANDLER_PROPERTY_ID = "__attribute_data";
    public static final String COLUMN_ROWSTATUS = "__rowstatus";
    public static final String COLUMN_ROWNUMBER = "__rowid";
    public static final String COLUMN_VALIDFLAG = "__validflag";
    public static final String COLUMN_ATTRIBUTEGROUP = "__attributegroup";
    public static final String AJAXMODE_VIEW = "view";
    public static final String AJAXMODE_ADD = "add";
    public static final String AJAXMODE_REMOVE = "remove";
    public static final String AJAXMODE_VALIDATE = "validate";
    public static final String AJAXMODE_MOVE = "move";
    public static final String AJAXMODE_RESET = "reset";
    public static final String MODE_ADHOC = "adhoc";
    public static final String MODE_LINK = "link";
    public static final String MODE_TEMPLATE = "template";
    private MaintenanceMode mode = MaintenanceMode.adhoc;
    private RenderMode renderMode = RenderMode.full;
    private DataSet attributes;
    private boolean viewOnly = false;
    private boolean locked = false;
    private int[] selectedRows;
    private M18NUtil m18nServer;
    private M18NUtil m18nClient;
    private JSONObject jobValidations;
    private ArrayList<String> links;
    private boolean showContext = false;
    private Comparator<String> comparator = new Comparator<String>(){

        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    };

    public MaintAttribute() {
        this.renderMode = RenderMode.full;
    }

    public MaintAttribute(PageContext pageContext, SDITagInfo sdiInfo) {
        this.setPageContext(pageContext);
        this.sdiInfo = sdiInfo;
        this.connectionInfo = new ConnectionProcessor(pageContext).getConnectionInfo(this.getConnectionId());
        this.setConnectionId(this.connectionInfo.getConnectionId());
        this.renderMode = RenderMode.full;
        if (sdiInfo != null) {
            this.setSDIInfo(sdiInfo);
        }
    }

    public JSONObject getValidations() {
        return this.jobValidations;
    }

    public void setSelected(int[] selected) {
        this.selectedRows = selected;
        Arrays.sort(this.selectedRows);
    }

    public MaintAttribute(String sdcid, String keyid1, String keyid2, String keyid3, DataSet attributedata, ConnectionInfo connectionInfo, Logger logger) {
        this.logger = logger;
        this.connectionInfo = connectionInfo;
        this.setConnectionId(connectionInfo.getConnectionId());
        this.renderMode = RenderMode.ajax;
        this.sdiInfo = this.createSDIInfo("", sdcid, keyid1, keyid2, keyid3, attributedata);
        if (this.sdiInfo != null) {
            this.setSDIInfo(this.sdiInfo);
        }
    }

    public MaintAttribute(PageContext pageContext, String rsetid, String sdcid, String keyid1, String keyid2, String keyid3, DataSet attributedata) {
        this.setPageContext(pageContext);
        this.connectionInfo = new ConnectionProcessor(pageContext).getConnectionInfo(this.getConnectionId());
        this.sdiInfo = this.createSDIInfo(rsetid, sdcid, keyid1, keyid2, keyid3, attributedata);
        this.setConnectionId(this.connectionInfo.getConnectionId());
        this.renderMode = RenderMode.ajax;
        if (this.sdiInfo != null) {
            this.setSDIInfo(this.sdiInfo);
        }
    }

    @Override
    public void setSDIInfo(SDITagInfo sdiInfo) {
        super.setSDIInfo(sdiInfo);
        this.attributes = sdiInfo.getDataSet("attribute");
        if (this.attributes != null) {
            if (!this.attributes.isValidColumn(COLUMN_ROWSTATUS)) {
                this.attributes.addColumn(COLUMN_ROWSTATUS, 0);
                this.attributes.setValue(-1, COLUMN_ROWSTATUS, "S");
            }
            if (!this.attributes.isValidColumn(COLUMN_VALIDFLAG)) {
                this.attributes.addColumn(COLUMN_VALIDFLAG, 0);
            }
            if (!this.attributes.isValidColumn(COLUMN_ROWNUMBER)) {
                this.attributes.addColumn(COLUMN_ROWNUMBER, 0);
                for (int i = 0; i < this.attributes.getRowCount(); ++i) {
                    this.attributes.setString(i, COLUMN_ROWNUMBER, "" + i);
                }
            }
            if (!this.attributes.isValidColumn(COLUMN_ATTRIBUTEGROUP)) {
                this.attributes.addColumn(COLUMN_ATTRIBUTEGROUP, 0);
            }
        }
    }

    public void setRenderMode(RenderMode renderMode) {
        this.renderMode = renderMode;
    }

    public RenderMode getRenderMode() {
        return this.renderMode;
    }

    private void renderError(StringBuffer html, String sMsg) {
        html.append("<font color=\"red\">").append(this.getTranslationProcessor().translate(sMsg)).append("</font>");
        this.logger.warn(sMsg);
    }

    private void renderWarning(StringBuffer html, String sMsg) {
        html.append("<font color=\"black\">").append(this.getTranslationProcessor().translate(sMsg)).append("</font>");
        this.logger.warn(sMsg);
    }

    public static String getModeText(MaintenanceMode mode) {
        if (mode == MaintenanceMode.linkdef) {
            return MODE_LINK;
        }
        if (mode == MaintenanceMode.template) {
            return MODE_TEMPLATE;
        }
        return MODE_ADHOC;
    }

    public static MaintenanceMode getMode(String mode) {
        if (mode.equalsIgnoreCase(MODE_LINK)) {
            return MaintenanceMode.linkdef;
        }
        if (mode.equalsIgnoreCase(MODE_TEMPLATE)) {
            return MaintenanceMode.template;
        }
        return MaintenanceMode.adhoc;
    }

    public void setShowContext(boolean showContext) {
        this.showContext = showContext;
    }

    public ArrayList<String> getLinks(PropertyList sdcprops, SDCProcessor sdcProc, ConfigurationProcessor cp) {
        ArrayList<String> sdcfilter;
        ArrayList<String> ja = new ArrayList<String>();
        PropertyListCollection revlinks = sdcprops.getCollection("reverselinks");
        String sdc = sdcprops.getProperty(PROPERTY_SDCID);
        ArrayList<String> arrayList = sdcfilter = this.element.getProperty("targetsdcid").length() > 0 ? new ArrayList<String>(Arrays.asList(StringUtil.split(this.element.getProperty("targetsdcid").toLowerCase(), ";"))) : null;
        if (revlinks != null) {
            block0: for (int i = 0; i < revlinks.size(); ++i) {
                String linksdcid;
                ArrayList<PropertyList> copyDownPolicy;
                PropertyList revlink = revlinks.getPropertyList(i);
                if (!revlink.getProperty("linktype", "F").equalsIgnoreCase("F") || !revlink.getProperty("linksdcid").equalsIgnoreCase(sdc) || (copyDownPolicy = BaseSDIAction.getCopyDownPolicy(null, sdcProc.getPropertyList(linksdcid = revlink.getProperty(PROPERTY_SDCID)), new String[]{"attributes"}, cp).get("attributes")) == null) continue;
                for (PropertyList cd : copyDownPolicy) {
                    if (!cd.getProperty(PROPERTY_SDCID).equalsIgnoreCase(sdc) || !cd.getProperty("copyattributes").equalsIgnoreCase("Y")) continue;
                    if (sdcfilter != null && !sdcfilter.contains(revlink.getProperty(PROPERTY_SDCID).toLowerCase())) continue block0;
                    ja.add(revlink.getProperty(PROPERTY_SDCID));
                    continue block0;
                }
            }
        }
        if (sdc.equalsIgnoreCase("ParamList")) {
            if (sdcfilter == null || sdcfilter.contains("dataset")) {
                ja.add("DataSet");
            }
        } else if (sdc.equalsIgnoreCase("WorkItem")) {
            if (sdcfilter == null || sdcfilter.contains("dataset")) {
                ja.add("DataSet");
            }
            if (sdcfilter == null || sdcfilter.contains("lv_worksheetitem")) {
                ja.add("LV_WorksheetItem");
            }
            if (sdcfilter == null || sdcfilter.contains("sdiworkitem")) {
                ja.add("SDIWorkItem");
            }
        } else if (sdc.equalsIgnoreCase("SpecSDC")) {
            if (sdcfilter == null || sdcfilter.contains("sdispec")) {
                ja.add("SDISpec");
            }
        } else if (sdc.equalsIgnoreCase("QCMethod") || sdc.equalsIgnoreCase("SampleType") || sdc.equalsIgnoreCase("LV_ReagentType")) {
            if (sdcfilter == null || sdcfilter.contains("lv_worksheetitem")) {
                ja.add("LV_WorksheetItem");
            }
        } else if (sdc.equalsIgnoreCase("LV_InstrumentModel")) {
            if (sdcfilter == null || sdcfilter.contains("lv_worksheetitem")) {
                ja.add("LV_WorksheetItem");
            }
        } else if (sdc.equalsIgnoreCase("LV_InstrumentType") && (sdcfilter == null || sdcfilter.contains("lv_worksheetitem"))) {
            ja.add("LV_WorksheetItem");
        }
        return ja;
    }

    public static ArrayList<String> getScriptIncludes() {
        return MaintAttribute.getScriptIncludes(false);
    }

    public static ArrayList<String> getScriptIncludes(boolean useFullIncludes) {
        ArrayList<String> out = new ArrayList<String>();
        out.add(JS_SRC);
        out.addAll(HTMLEditorControl.getScriptIncludes(useFullIncludes));
        return out;
    }

    public static ArrayList<String> getStyleIncludes() {
        ArrayList<String> out = new ArrayList<String>();
        return out;
    }

    private void renderScriptAndStyleIncludes(StringBuffer html, StringBuffer script) {
        ArrayList<String> scripts = MaintAttribute.getScriptIncludes(this.getConnectionProcessor().getSapphireConnection().getUseFullIncludes());
        for (String scriptitem : scripts) {
            html.append("<script type=\"text/javascript\" src=\"").append(scriptitem).append("\"></script>");
        }
        ArrayList<String> styles = MaintAttribute.getStyleIncludes();
        for (String style : styles) {
            html.append("<link rel=\"stylesheet\" href=\"").append(style).append("\" type=\"text/css\">");
        }
    }

    private void renderDataScript(StringBuffer script, SDITagInfo sdiInfo, PropertyList sdcprops, DataSet attdata) {
        script.append("maintAttribute.instance[\"").append(this.elementid).append("\"].rsetid=\"").append(sdiInfo.getSDIData().getRsetid()).append("\";");
        if (sdcprops != null) {
            String sdc = sdcprops.getProperty(PROPERTY_SDCID);
            script.append("maintAttribute.instance[\"").append(this.elementid).append("\"].sdcid=\"").append(sdc).append("\";");
            DataSet primary = sdiInfo.getDataSet("primary");
            boolean newAddMode = "true".equalsIgnoreCase(this.requestContext != null ? this.requestContext.getProperty("newAddMode") : "false");
            if (primary != null && primary.getRowCount() > 0) {
                script.append(JS_OBJECT).append(".instance[\"").append(this.elementid).append("\"].keyid1=\"").append(newAddMode ? this.requestContext.getProperty(PROPERTY_KEYID1) : primary.getValue(0, sdcprops.getProperty("keycolid1"), "")).append("\";");
                if (sdcprops.getProperty("keycolid2").length() > 0) {
                    script.append(JS_OBJECT).append(".instance[\"").append(this.elementid).append("\"].keyid2=\"").append(newAddMode ? this.requestContext.getProperty(PROPERTY_KEYID2) : primary.getValue(0, sdcprops.getProperty("keycolid2"), "")).append("\";");
                }
                if (sdcprops.getProperty("keycolid3").length() > 0) {
                    script.append(JS_OBJECT).append(".instance[\"").append(this.elementid).append("\"].keyid3=\"").append(newAddMode ? this.requestContext.getProperty(PROPERTY_KEYID3) : primary.getValue(0, sdcprops.getProperty("keycolid3"), "")).append("\";");
                }
            }
        }
        if (attdata != null) {
            script.append(JS_OBJECT).append(".instance[\"").append(this.elementid).append("\"].data").append("=sapphire.util.dataSet.create(").append(attdata.toJSONString(true, attdata.getColumns(), false, true, true)).append(");");
        } else {
            script.append(JS_OBJECT).append(".instance[\"").append(this.elementid).append("\"].data").append("=sapphire.util.dataSet.create();");
        }
    }

    public void setLinks(ArrayList<String> links) {
        this.links = links;
    }

    public void renderScript(StringBuffer script, MaintenanceMode mode, ArrayList<String> links, boolean initial) {
        if (initial) {
            script.append(JS_OBJECT).append(".instance[\"").append(this.elementid).append("\"]=").append("{}").append(";");
            script.append(JS_OBJECT).append(".instance[\"").append(this.elementid).append("\"].selected=new Array();");
            script.append(JS_OBJECT).append(".instance[\"").append(this.elementid).append("\"].mode=\"").append(MaintAttribute.getModeText(mode)).append("\";");
            script.append(JS_OBJECT).append(".instance[\"").append(this.elementid).append("\"].changed=").append(false).append(";");
            script.append(JS_OBJECT).append(".instance.elements.push('").append(this.elementid).append("');");
            JSONArray jlinks = links != null ? new JSONArray(links) : new JSONArray();
            script.append(JS_OBJECT).append(".instance[\"").append(this.elementid).append("\"].linked=").append(jlinks.toString()).append(";");
            if (this.element != null) {
                script.append(JS_OBJECT).append(".instance[\"").append(this.elementid).append("\"].properties=sapphire.util.propertyList.create(").append(this.element.toJSONString()).append(");");
            } else {
                script.append(JS_OBJECT).append(".instance[\"").append(this.elementid).append("\"].properties=sapphire.util.propertyList.create(").append("").append(");");
            }
            script.append(JS_OBJECT).append(".instance[\"").append(this.elementid).append("\"].showcontext=").append(this.showContext).append(";");
        }
    }

    public void renderDynamicScript(StringBuffer script, JSONObject validations) {
        script.append("maintAttribute.instance[\"").append(this.elementid).append("\"].validations").append("=").append(validations.toString()).append(";");
    }

    @Override
    public void setElementProperties(PropertyList properties) {
        super.setElementProperties(properties);
        this.mode = this.element.getProperty(PROPERTY_MODE, MODE_ADHOC).equalsIgnoreCase(MODE_LINK) ? MaintenanceMode.linkdef : (this.element.getProperty(PROPERTY_MODE, MODE_ADHOC).equalsIgnoreCase(MODE_TEMPLATE) ? MaintenanceMode.template : MaintenanceMode.adhoc);
    }

    public String getScript() {
        if (this.script != null && this.script.length() > 0) {
            return this.script.toString();
        }
        return "";
    }

    private String getAttributeContainer(StringBuffer content) {
        StringBuffer html = new StringBuffer();
        String width = this.element.getProperty("width").length() == 0 ? (this.mode == MaintenanceMode.adhoc ? "625px" : "auto") : this.element.getProperty("width");
        boolean scrollable = "Y".equalsIgnoreCase(this.element.getProperty("scrollable"));
        String overflowX = "overflow-x:" + (scrollable ? "auto;" : "hidden;");
        html.append("<div id=\"").append(JS_CONTAINER).append(this.elementid).append("\" class=\"maintattribute_container\" style=\"width:").append(width).append("px;height:100%;").append(this.browser == null || this.browser.isWebkit() ? overflowX : "").append("\">");
        html.append(content);
        html.append("</div>");
        return html.toString();
    }

    public void sortAttributeData() {
        MaintAttribute.sortAttributeData(this.attributes, this.sdiInfo.getSdcid(), this.getConnectionId());
    }

    public static void sortAttributeData(DataSet attributes, String sdcid, String connectionId) {
        if (attributes != null) {
            if (sdcid.equalsIgnoreCase("workitem")) {
                attributes.sort("attributesdcid d, usersequence");
            } else {
                attributes.sort("attributesdcid, usersequence");
            }
            if (attributes != null && attributes.isValidColumn("instructiontext")) {
                for (int row = 0; row < attributes.getRowCount(); ++row) {
                    StringBuffer content = new StringBuffer(attributes.getClob(row, "instructiontext", ""));
                    if (content.length() <= 0) continue;
                    HTMLEditorControl.processImages(content, false, connectionId);
                    attributes.setClob(row, "instructiontext", content.toString());
                }
            }
        }
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.element.getProperty("dynamicaudit").length() > 0) {
            html.append("<script type=\"text/javascript\">sapphire.page.data.dynamicaudit_" + this.elementid + "='" + this.element.getProperty("dynamicaudit") + "';</script>");
        }
        this.script = new StringBuffer();
        SDCProcessor sdcProc = this.getSDCProcessor();
        PropertyList sdc = sdcProc.getPropertyList(this.sdiInfo.getSdcid());
        if (sdc != null) {
            if (this.links == null) {
                this.links = this.getLinks(sdc, sdcProc, this.getConfigurationProcessor());
            }
            if (this.renderMode == RenderMode.instance) {
                DataSet pr = this.sdiInfo.getSDIData().getDataset("primary");
                if (pr != null && pr.getRowCount() > 0) {
                    if (pr.getValue(0, "__lockedby", "").length() > 0) {
                        this.logger.debug("Primary locked therefore lock attributes.");
                        this.element.setProperty("viewonly", "Y");
                        this.locked = true;
                    }
                    if (this.sdiInfo.getSdcid().equalsIgnoreCase("WorkItem")) {
                        this.showContext = !pr.getValue(0, "workitemtypeflag", "S").equalsIgnoreCase("P");
                    } else if (this.sdiInfo.getSdcid().equalsIgnoreCase("QCMethod") || this.sdiInfo.getSdcid().equalsIgnoreCase("LV_InstrumentModel") || this.sdiInfo.getSdcid().equalsIgnoreCase("LV_InstrumentType") || this.sdiInfo.getSdcid().equalsIgnoreCase("SampleType") || this.sdiInfo.getSdcid().equalsIgnoreCase("LV_ReagentType")) {
                        this.showContext = true;
                    }
                }
                this.sortAttributeData();
                html.append("<input type=\"hidden\" id=\"__attribute_data").append("_").append(this.elementid).append("\" name=\"__attribute_data").append("_").append(this.elementid).append("\">");
                html.append("<input type=\"hidden\" name=\"").append("__postpropertyhandler_post_").append(this.elementid).append("\" value=\"").append(PROPERTYHANDLER).append("\"/>");
                this.renderScript(this.script, this.mode, this.links, true);
                this.renderDataScript(this.script, this.sdiInfo, sdc, this.attributes);
                StringBuffer content = new StringBuffer();
                content.append(this.getTranslationProcessor().translate("Loading")).append("...");
                html.append(this.getAttributeContainer(content));
            } else if (this.renderMode == RenderMode.ajax) {
                this.renderScript(this.script, this.mode, this.links, false);
                DataSet finalattributedata = this.attributes;
                StringBuffer content = new StringBuffer();
                JSONObject jobValidations = new JSONObject();
                if (this.element != null && this.element.size() > 0) {
                    if (this.attributes != null) {
                        this.viewOnly = this.element.getProperty("viewonly", "N").equalsIgnoreCase("Y");
                        switch (this.mode) {
                            case adhoc: {
                                finalattributedata = this.renderAdhocMode(content, sdc, this.attributes, this.element, this.selectedRows, jobValidations, this.viewOnly);
                                break;
                            }
                            case linkdef: {
                                if (this.element.getProperty("informationmode", "N").equalsIgnoreCase("Y")) {
                                    finalattributedata = this.renderLinkInformationMode(content, this.script, sdc, this.attributes, this.element, this.selectedRows, jobValidations, this.links, this.viewOnly);
                                    break;
                                }
                                finalattributedata = this.renderLinkMode(content, this.script, sdc, this.attributes, this.element, this.selectedRows, jobValidations, this.links, this.viewOnly);
                                break;
                            }
                            case template: {
                                finalattributedata = this.renderTemplateMode(content, sdc, this.attributes, this.element, this.selectedRows, jobValidations, this.viewOnly);
                            }
                        }
                        if (this.pageContext != null) {
                            this.script.append(this.pageContext.getAttribute("dd_dropdownvalues") != null ? this.pageContext.getAttribute("dd_dropdownvalues") : "");
                        }
                    } else {
                        this.renderError(content, "No attribute data provided.");
                    }
                } else {
                    this.renderError(content, "No element properties.");
                }
                html.append(content);
                if (finalattributedata.getRowCount() != this.attributes.getRowCount()) {
                    this.renderDataScript(this.script, this.sdiInfo, sdc, finalattributedata);
                }
                this.renderDynamicScript(this.script, jobValidations);
            } else {
                boolean rendered = false;
                String temp = (String)this.pageContext.getRequest().getAttribute(PROPERTYHANDLER_PROPERTY_ID);
                if (temp != null && temp.equalsIgnoreCase("Y")) {
                    rendered = true;
                } else {
                    this.pageContext.getRequest().setAttribute(PROPERTYHANDLER_PROPERTY_ID, (Object)"Y");
                }
                DataSet pr = this.sdiInfo.getSDIData().getDataset("primary");
                if (pr != null && pr.getRowCount() > 0) {
                    if (pr.getValue(0, "__lockedby", "").length() > 0) {
                        this.logger.debug("Primary locked therefore lock attributes.");
                        this.element.setProperty("viewonly", "Y");
                        this.locked = true;
                    }
                    if (this.sdiInfo.getSdcid().equalsIgnoreCase("WorkItem")) {
                        this.showContext = !pr.getValue(0, "workitemtypeflag", "S").equalsIgnoreCase("P");
                    } else if (this.sdiInfo.getSdcid().equalsIgnoreCase("QCMethod") || this.sdiInfo.getSdcid().equalsIgnoreCase("LV_InstrumentModel") || this.sdiInfo.getSdcid().equalsIgnoreCase("LV_InstrumentType") || this.sdiInfo.getSdcid().equalsIgnoreCase("SampleType") || this.sdiInfo.getSdcid().equalsIgnoreCase("LV_ReagentType")) {
                        this.showContext = true;
                    }
                }
                if (!rendered) {
                    this.sortAttributeData();
                }
                this.renderScript(this.script, this.mode, this.links, true);
                if (!rendered) {
                    this.renderScriptAndStyleIncludes(html, this.script);
                    html.append("<style id=\"maintatt_style_").append(this.elementid).append("\">");
                    html.append("</style>");
                    this.script.append(Maint.getValidationVariables(this.element, this.pageContext));
                }
                html.append("<input type=\"hidden\" id=\"__attribute_data").append("_").append(this.elementid).append("\" name=\"__attribute_data").append("_").append(this.elementid).append("\">");
                html.append("<input type=\"hidden\" name=\"").append("__postpropertyhandler_post_").append(this.elementid).append("\" value=\"").append(PROPERTYHANDLER).append("\"/>");
                DataSet finalAttributedata = this.attributes;
                StringBuffer content = new StringBuffer();
                JSONObject jobValidations = new JSONObject();
                if (this.element != null && this.element.size() > 0) {
                    if (this.attributes != null) {
                        this.viewOnly = this.element.getProperty("viewonly", "N").equalsIgnoreCase("Y");
                        switch (this.mode) {
                            case adhoc: {
                                finalAttributedata = this.renderAdhocMode(content, sdc, this.attributes, this.element, this.selectedRows, jobValidations, this.viewOnly);
                                break;
                            }
                            case linkdef: {
                                if (this.element.getProperty("informationmode", "N").equalsIgnoreCase("Y")) {
                                    finalAttributedata = this.renderLinkInformationMode(content, this.script, sdc, this.attributes, this.element, this.selectedRows, jobValidations, this.links, this.viewOnly);
                                    break;
                                }
                                finalAttributedata = this.renderLinkMode(content, this.script, sdc, this.attributes, this.element, this.selectedRows, jobValidations, this.links, this.viewOnly);
                                break;
                            }
                            case template: {
                                finalAttributedata = this.renderTemplateMode(content, sdc, this.attributes, this.element, this.selectedRows, jobValidations, this.viewOnly);
                            }
                        }
                        if (this.pageContext != null) {
                            this.script.append(this.pageContext.getAttribute("dd_dropdownvalues") != null ? this.pageContext.getAttribute("dd_dropdownvalues") : "");
                        }
                    } else {
                        this.renderError(content, "No attribute data provided.");
                    }
                } else {
                    this.renderError(content, "No element properties.");
                }
                this.renderDataScript(this.script, this.sdiInfo, sdc, finalAttributedata);
                if (this.renderMode == RenderMode.full) {
                    html.append(this.getAttributeContainer(content));
                } else {
                    html.append(content);
                }
                this.renderDynamicScript(this.script, jobValidations);
                html.append("<script>");
                html.append(this.script);
                html.append("</script>");
            }
        } else {
            this.renderError(html, "Invalid SDC " + this.sdiInfo.getSdcid() + " Provided.");
        }
        return html.toString();
    }

    private void renderLinkModeTableHeader(StringBuffer html, String primarysdcid, String currentsdcid, boolean viewOnly, boolean informationMode) {
        html.append("<thead>");
        html.append("<tr class=\"gridmaint_tablehead\">");
        if (!viewOnly) {
            html.append("<th class=\"gridmaint_fieldtitle\">").append("&nbsp;").append("</th>");
        }
        html.append("<th class=\"gridmaint_fieldtitle linkedattibuteidcol\">").append(this.getTranslationProcessor().translate("Attribute")).append("</th>");
        if (primarysdcid.equalsIgnoreCase("WorkItem") && currentsdcid.equalsIgnoreCase("SDIWorkItem")) {
            html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Attribute Type")).append("</th>");
        }
        if (!informationMode) {
            html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Editor Style")).append("</th>");
            html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Editor SDC Id")).append("</th>");
            html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Editor Ref Type")).append("</th>");
            html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Updateable")).append("</th>");
            html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Hidden")).append("</th>");
            html.append("<th class=\"gridmaint_fieldtitle\" style=\"width:180px;\">").append(this.getTranslationProcessor().translate("Default Value")).append("</th>");
        }
        html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Information Text")).append("</th>");
        html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Required")).append("</th>");
        if (primarysdcid.equalsIgnoreCase("WorkItem")) {
            if ((currentsdcid.equalsIgnoreCase("DataSet") || currentsdcid.equalsIgnoreCase("LV_WorksheetItem")) && this.showContext) {
                html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Linked To")).append("</th>");
            }
            if (currentsdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Position")).append("</th>");
            }
        } else if (primarysdcid.equalsIgnoreCase("QCMethod") || primarysdcid.equalsIgnoreCase("SampleType") || primarysdcid.equalsIgnoreCase("LV_ReagentType")) {
            if (currentsdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Position")).append("</th>");
            }
        } else if (primarysdcid.equalsIgnoreCase("LV_InstrumentModel") || primarysdcid.equalsIgnoreCase("LV_InstrumentType")) {
            if (currentsdcid.equalsIgnoreCase("LV_WorksheetItem") && this.showContext) {
                html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Linked To")).append("</th>");
            }
            if (currentsdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Position")).append("</th>");
            }
        }
        if (informationMode) {
            html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Type")).append("</th>");
            html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Editor Style")).append("</th>");
            html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Editor SDC Id")).append("</th>");
            html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Editor Ref Type")).append("</th>");
            html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Updateable")).append("</th>");
            html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Hidden")).append("</th>");
            html.append("<th class=\"gridmaint_fieldtitle\" style=\"width:180px;\">").append(this.getTranslationProcessor().translate("Default Value")).append("</th>");
        }
        html.append("</tr>");
        html.append("</thead>");
    }

    private M18NUtil getM18NUtil(boolean client) {
        if (client) {
            if (this.m18nClient == null) {
                this.m18nClient = new M18NUtil(this.connectionInfo);
            }
            return this.m18nClient;
        }
        if (this.m18nServer == null) {
            this.m18nServer = new M18NUtil();
        }
        return this.m18nServer;
    }

    private String getAttributeValue(DataSet attributedata, int datarow, boolean defaults, boolean raw) {
        return MaintAttribute.getAttributeValue(this.m18nServer, this.connectionInfo, attributedata, datarow, defaults, raw);
    }

    public static String getAttributeValue(M18NUtil m18nServer, ConnectionInfo connectionInfo, DataSet attributedata, int datarow, boolean defaults, boolean raw) {
        String prefix = "";
        String value = "";
        String dt = attributedata.getValue(datarow, "datatype", "S");
        if (defaults) {
            prefix = "default";
        }
        if (dt.equalsIgnoreCase("N")) {
            value = BaseSDIAttributeAction.getNumericAttributeValue(attributedata.getValue(datarow, prefix + "textvalue", ""), attributedata.getValue(datarow, prefix + "numericvalue", ""), m18nServer != null ? m18nServer : new M18NUtil(connectionInfo));
        } else if (dt.equalsIgnoreCase("D") || dt.equalsIgnoreCase("O")) {
            String tv = attributedata.getValue(datarow, prefix + "textvalue", "");
            String dv = attributedata.getValue(datarow, prefix + "datevalue", "");
            value = BaseSDIAttributeAction.getDateAttributeValue(tv, dv, defaults);
            if (dt.equalsIgnoreCase("O") && attributedata.getM18n() != null && OpalUtil.isNotEmpty(value)) {
                Calendar c = attributedata.getM18n().parseCalendar(value);
                value = attributedata.getM18n().formatDateOnly(c);
            }
        } else {
            value = dt.equalsIgnoreCase("C") ? attributedata.getValue(datarow, prefix + "clobvalue", "") : attributedata.getValue(datarow, prefix + "textvalue", "");
        }
        return value;
    }

    private EditorStyleField getAttributeField(String editorstyle, String attributeid, String attributetitle, int datarow, int realrow, DataSet attributedata, boolean defaults, boolean hidden, boolean updateable, boolean viewonly) throws Exception {
        return MaintAttribute.getAttributeField(this.pageContext, this.sdiInfo, this.m18nServer, this.connectionInfo, this.elementid, editorstyle, attributeid, attributetitle, datarow, realrow, attributedata, defaults, hidden, updateable, viewonly, "maintAttribute.change(event,'" + this.elementid + "'," + realrow + ")");
    }

    public static EditorStyleField getAttributeField(PageContext pageContext, SDITagInfo sdiInfo, M18NUtil m18nServer, ConnectionInfo connectionInfo, String elementid, String editorstyle, String attributeid, String attributetitle, int datarow, int realrow, DataSet attributedata, boolean defaults, boolean hidden, boolean updateable, boolean viewonly, String changeEvent) throws Exception {
        EditorStyleField esf = new EditorStyleField(pageContext, sdiInfo, connectionInfo.getConnectionId());
        esf.setDatasetname("attribute");
        String atdt = attributedata.getValue(datarow, "datatype", "S");
        String sdcid = attributedata.getValue(datarow, "editsdcid", "");
        String reftypeid = attributedata.getValue(datarow, "editreftypeid", "");
        if (editorstyle != null && editorstyle.length() > 0) {
            esf.setEditorStyleId(editorstyle, sdcid, reftypeid);
        } else {
            esf.setDefaultEditorStyleProperties(atdt, sdcid, reftypeid);
        }
        String a = StringUtil.replaceAll(StringUtil.replaceAll(attributeid, " ", "_"), "/", "_");
        esf.setFieldName(elementid + realrow + "_" + a);
        if (changeEvent != null && changeEvent.length() > 0) {
            esf.setChangeEvent(changeEvent);
        }
        esf.setUseNoNameAttribute(true);
        boolean isDateOnly = atdt.equalsIgnoreCase("o");
        esf.setColumnDefinition(attributetitle, atdt, atdt.equalsIgnoreCase("c") ? 0 : 255, false, isDateOnly);
        if (atdt.equalsIgnoreCase("d") || atdt.equalsIgnoreCase("o")) {
            if (isDateOnly) {
                attributedata.setTimeZoneInsensitive(defaults ? "defaultdatevalue" : "datevalue");
                attributedata.setDateDisplayFormat(defaults ? "defaultdatevalue" : "datevalue", new M18NUtil(pageContext).getDefaultDateOnlyFormat(false));
                esf.getColumn().setProperty("format", "O");
            } else {
                attributedata.setDateDisplayFormat(defaults ? "defaultdatevalue" : "datevalue", new M18NUtil(pageContext).getDefaultDateFormat(true));
            }
        }
        String value = MaintAttribute.getAttributeValue(m18nServer, connectionInfo, attributedata, datarow, defaults, false);
        esf.setFieldValue(value);
        if (hidden) {
            esf.setColumnProperty(PROPERTY_MODE, "hidden");
        }
        if (viewonly || !updateable && value.length() > 0) {
            esf.setColumnProperty(PROPERTY_MODE, "readonly");
            if (esf.getColumn().getPropertyList("lookuplink") != null) {
                esf.getColumn().remove("lookuplink");
            }
        }
        return esf;
    }

    private EditorStyleField getLinkField(String field, String mode, String attributeid, int realrow, String value, String datatype, boolean viewonly) {
        EditorStyleField esf = new EditorStyleField(this.pageContext, this.sdiInfo, this.getConnectionId());
        esf.setDatasetname("attribute");
        PropertyList column = new PropertyList();
        column.setProperty(PROPERTY_MODE, mode);
        if (mode.equalsIgnoreCase("richtexthtml_dm")) {
            esf.setColumnDefinition(field, EditorStyleField.getEditorStyleDataType("T"), 40000, false);
        } else {
            esf.setColumnDefinition(field, EditorStyleField.getEditorStyleDataType("C"), 100, false);
        }
        if (mode.equalsIgnoreCase("checkbox")) {
            column.setProperty("displayvalue", "Y=Yes;N=No");
            if (viewonly) {
                esf.setReadonly(true);
            }
        } else if (mode.equalsIgnoreCase("lookup") && field.equalsIgnoreCase("editorstyleid")) {
            if (!viewonly) {
                PropertyList lookuplink = new PropertyList();
                lookuplink.setProperty("href", "rc?command=page&page=LV_EditorStyleLookup");
                lookuplink.setProperty("restrictivewhere", "datatype='" + (datatype.equalsIgnoreCase("o") ? "D" : datatype) + "'");
                lookuplink.setProperty("tip", this.getTranslationProcessor().translate("Lookup Editor Style"));
                lookuplink.setProperty("dialogtype", "Sapphire Dialog");
                column.setProperty("lookuplink", lookuplink);
                column.setProperty("size", "15");
                esf.setReadonly(true);
                esf.setLinkDefinition("LV_EditorStyle", 'F', "", false);
            } else {
                column.setProperty(PROPERTY_MODE, "input");
                esf.setReadonly(true);
            }
            column.setProperty("size", "15");
        } else if (mode.equalsIgnoreCase("lookup") && (field.equalsIgnoreCase("editreftypeid") || field.equalsIgnoreCase("editsdcid"))) {
            if (!viewonly) {
                PropertyList lookuplink = new PropertyList();
                lookuplink.setProperty("href", "rc?command=page&page=" + (field.equalsIgnoreCase("editsdcid") ? "LV_SDCLookup" : "LV_RefTypeLookup"));
                lookuplink.setProperty("tip", this.getTranslationProcessor().translate("Lookup") + " " + (field.equalsIgnoreCase("SDC Id") ? this.getTranslationProcessor().translate("LV_SDCLookup") : this.getTranslationProcessor().translate("Ref Type")));
                lookuplink.setProperty("dialogtype", "Sapphire Dialog");
                column.setProperty("lookuplink", lookuplink);
                esf.setReadonly(true);
            } else {
                column.setProperty(PROPERTY_MODE, "input");
                esf.setReadonly(true);
            }
            column.setProperty("size", "15");
        } else if (mode.equalsIgnoreCase("dropdownlist") && field.equalsIgnoreCase("worksheetcontext")) {
            column.setProperty("reftypeid", "WorksheetContext");
        } else if (mode.equalsIgnoreCase("dropdownlist") && field.equalsIgnoreCase("copydowncontext")) {
            if (this.sdiInfo.getSdcid().equalsIgnoreCase("LV_InstrumentModel") || this.sdiInfo.getSdcid().equalsIgnoreCase("LV_InstrumentType")) {
                column.setProperty("reftypeid", "CertificationInterval");
            }
        } else if (mode.equalsIgnoreCase("richtexthtml_dm")) {
            if (viewonly) {
                column.setProperty(PROPERTY_MODE, "html");
                esf.setReadonly(true);
            } else {
                column.setProperty("size", "400;200");
            }
        } else if (viewonly) {
            column.setProperty(PROPERTY_MODE, "readonly");
        }
        esf.setColumn(column);
        esf.setFieldName(this.elementid + realrow + "_" + field);
        esf.setChangeEvent("maintAttribute.change(event,'" + this.elementid + "'," + realrow + ")");
        esf.setUseNoNameAttribute(true);
        esf.setFieldValue(value);
        return esf;
    }

    private DataSet renderLinkMode(StringBuffer areahtml, StringBuffer script, PropertyList sdc, DataSet attributeindata, PropertyList props, int[] selected, JSONObject validations, ArrayList<String> copydownlinks, boolean viewOnly) {
        DataSet touse = attributeindata;
        String priSDCid = sdc.getProperty(PROPERTY_SDCID);
        StringBuffer html = new StringBuffer();
        if (sdc.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y")) {
            if (this.links != null && this.links.size() > 0) {
                StringBuffer style = new StringBuffer();
                String largestText = "";
                boolean rendered = false;
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.linkdef));
                touse = touse.getFilteredDataSet(filter);
                filter.put(PROPERTY_ATTRIBUTESDCID, priSDCid);
                touse = touse.getFilteredDataSet(filter, true);
                if (touse.getRowCount() > 0) {
                    String targettitle = this.getTranslationProcessor() != null ? this.getTranslationProcessor().translate("Target") + ": " : "Target: ";
                    String currentsdcid = "";
                    PropertyList currentsdcprops = null;
                    PropertyListCollection currentattributes = null;
                    boolean currentallowatts = false;
                    boolean currenttransferatts = false;
                    int defaultCount = 0;
                    for (int i = 0; i < touse.getRowCount(); ++i) {
                        boolean isselected;
                        String rs = touse.getValue(i, COLUMN_ROWSTATUS, "S");
                        String attributesdcid = touse.getValue(i, PROPERTY_ATTRIBUTESDCID, currentsdcid);
                        if (!this.showContext && priSDCid.equalsIgnoreCase("WorkItem") && attributesdcid.equalsIgnoreCase("DataSet")) continue;
                        if (!copydownlinks.contains(attributesdcid)) {
                            touse.deleteRow(i);
                            --i;
                            continue;
                        }
                        if (rs.equalsIgnoreCase("D")) continue;
                        String attributeid = touse.getValue(i, PROPERTY_ATTRIBUTEID, "");
                        String dtype = touse.getValue(i, "datatype", "");
                        if (currentsdcid.length() == 0 || !currentsdcid.equalsIgnoreCase(attributesdcid)) {
                            if (currentsdcid.length() != 0) {
                                html.append("</td>");
                                html.append("</tr>");
                                html.append("</tbody>");
                                html.append("</table>");
                                html.append("</div>");
                                html.append("</td>");
                                html.append("</tr>");
                            }
                            currentsdcid = attributesdcid;
                            currentsdcprops = this.getSDCProcessor().getPropertyList(currentsdcid);
                            String grouptitle = currentsdcprops.getProperty("plural", currentsdcprops.getProperty("singular", currentsdcid));
                            grouptitle = grouptitle.substring(0, 1).toUpperCase() + grouptitle.substring(1);
                            String prefix = "" + this.elementid + "_" + attributesdcid + "_";
                            String rowId = prefix + "row";
                            String divId = prefix + "div";
                            boolean collapseGroups = props.getProperty(PROPERTY_COLLAPSEGROUPS, "N").equalsIgnoreCase("Y");
                            html.append("<tr class=\"list_groupheader\">");
                            String click = "maintAttribute.toggleTargetGroup('" + this.elementid + "','" + prefix + "',this)";
                            html.append("<th class=\"list_grouptitle\" style=\"width:16px;cursor:pointer;\" onclick=\"").append(click).append("\"><img src=\"WEB-CORE/pagetypes/list/images/").append(collapseGroups ? "plus.gif" : "minus.gif").append("\"></th><th align=\"left\" class=\"list_grouptitle\" style=\"cursor:pointer;\" onclick=\"").append(click).append("\">").append(targettitle).append(grouptitle).append("</th>");
                            html.append("</tr>");
                            html.append("<tr id=\"").append(rowId).append("\">");
                            html.append("<td></td><td style=\"padding-left:").append("10px").append(";\">");
                            style.append("#").append(rowId).append("{display:").append(collapseGroups ? "none" : "table-row").append(";}\n");
                            style.append("#").append(divId).append("{display:").append("block").append(";overflow:hidden;height:100%;width:100%;}\n");
                            html.append("<div id=\"").append(divId).append("\">");
                            html.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" class=\"gridmaint_table\">");
                            this.renderLinkModeTableHeader(html, priSDCid, currentsdcid, viewOnly, false);
                            html.append("<tbody>");
                            html.append("<tr>");
                            boolean bl = currentallowatts = currentsdcprops != null && currentsdcprops.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y");
                            if (priSDCid.equalsIgnoreCase("WorkItem") && (currentsdcid.equalsIgnoreCase("DataSet") || currentsdcid.equalsIgnoreCase("SDIWorkItem") || currentsdcid.equals("LV_WorksheetItem"))) {
                                currenttransferatts = true;
                            }
                            if (priSDCid.equalsIgnoreCase("QCMethod") && currentsdcid.equals("LV_WorksheetItem")) {
                                currenttransferatts = true;
                            }
                            if (priSDCid.equalsIgnoreCase("LV_InstrumentModel") && currentsdcid.equals("LV_WorksheetItem")) {
                                currenttransferatts = true;
                            }
                            if (priSDCid.equalsIgnoreCase("LV_InstrumentType") && currentsdcid.equals("LV_WorksheetItem")) {
                                currenttransferatts = true;
                            }
                            if (priSDCid.equalsIgnoreCase("SampleType") && currentsdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                                currenttransferatts = true;
                            }
                            if (priSDCid.equalsIgnoreCase("LV_ReagentType") && currentsdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                                currenttransferatts = true;
                            }
                            if (priSDCid.equalsIgnoreCase("ParamList") && currentsdcid.equalsIgnoreCase("DataSet")) {
                                currenttransferatts = true;
                            } else if (priSDCid.equalsIgnoreCase("SpecSDC") && currentsdcid.equalsIgnoreCase("SDISpec")) {
                                currenttransferatts = true;
                            } else {
                                PropertyList link;
                                PropertyListCollection links;
                                PropertyListCollection propertyListCollection = links = currentsdcprops != null ? currentsdcprops.getCollection("links") : null;
                                if (links != null && (link = links.find("linksdcid", priSDCid)) != null && copydownlinks != null) {
                                    for (int cdl = 0; cdl < copydownlinks.size(); ++cdl) {
                                        try {
                                            if (!copydownlinks.get(cdl).equalsIgnoreCase(link.getProperty(PROPERTY_SDCID))) continue;
                                            currenttransferatts = true;
                                            break;
                                        }
                                        catch (Exception e) {
                                            this.logger.warn("Could not process copydown link.");
                                        }
                                    }
                                }
                            }
                            currentattributes = currentsdcprops != null ? currentsdcprops.getCollection("attributes") : null;
                        } else {
                            html.append("<tr>");
                        }
                        int row = Integer.parseInt(touse.getString(i, COLUMN_ROWNUMBER, i + ""));
                        boolean bl = isselected = selected != null && Arrays.binarySearch(selected, row) > -1;
                        if (!viewOnly) {
                            html.append("<td class=\"gridmaint_field\">");
                            html.append("<input type=\"checkbox\" name=\"").append(this.elementid).append("_selector\" id=\"__").append(this.elementid).append("_").append(row).append("\" onclick=\"maintAttribute.select(event,'").append(this.elementid).append("',").append(row).append(")\"").append(isselected ? " checked" : "").append(">");
                            html.append("</td>");
                        }
                        if (currentsdcprops == null) {
                            Logger.logWarn("Could not obtain SDC Properties.");
                        }
                        if (currentallowatts) {
                            int instance;
                            String tip;
                            String title;
                            String def_instructionflag;
                            String currentinstructionflag = touse.getValue(i, PROPERTY_INSTRUCTIONFLAG, "");
                            PropertyList attributedef = currentattributes.find(PROPERTY_ATTRIBUTEID, attributeid);
                            boolean readOnly = false;
                            if (attributedef != null) {
                                def_instructionflag = attributedef.getProperty(PROPERTY_INSTRUCTIONFLAG);
                                title = this.getTranslationProcessor().translate(attributedef.getProperty("attributetitle", attributeid));
                                if (!currenttransferatts) {
                                    tip = this.getTranslationProcessor().translate("Target SDC has no transfer of attributes defined.");
                                    readOnly = true;
                                } else {
                                    tip = this.getTranslationProcessor().translate(attributedef.getProperty("helptext", ""));
                                    if (tip.length() == 0) {
                                        tip = title;
                                    }
                                    tip = tip + " (" + attributeid + ")";
                                }
                            } else if (currentinstructionflag.equalsIgnoreCase("A") || currentinstructionflag.equalsIgnoreCase("O")) {
                                def_instructionflag = "";
                                title = "";
                                tip = "";
                            } else {
                                title = "(auto)";
                                def_instructionflag = "";
                                tip = attributeid + " " + this.getTranslationProcessor().translate(" has been auto created and has no definition for target SDC.");
                                if (!currenttransferatts) {
                                    tip = tip + " " + this.getTranslationProcessor().translate(" Target SDC has no transfer of attributes defined.");
                                }
                            }
                            try {
                                instance = Integer.parseInt(touse.getValue(i, PROPERTY_ATTRIBUTEINSTANCE));
                            }
                            catch (Exception e) {
                                instance = -1;
                            }
                            String fulltitle = title;
                            if (instance > 1) {
                                fulltitle = fulltitle + " (" + instance + ")";
                            }
                            if (largestText.length() < fulltitle.length()) {
                                largestText = fulltitle;
                            }
                            boolean titleshown = true;
                            html.append("<td id=\"").append(this.elementid).append(row).append("_").append(attributeid).append("_titlecell\" title=\"").append(SafeHTML.encodeForHTMLAttribute(tip)).append("\" class=\"gridmaint_field linkedattibuteidcol\"");
                            if (readOnly) {
                                html.append(" style=\"color:red;\"");
                            }
                            if (!(title.length() != 0 || !def_instructionflag.equalsIgnoreCase("a") && def_instructionflag.equalsIgnoreCase("o") && currentinstructionflag.equalsIgnoreCase("a") && currentinstructionflag.equalsIgnoreCase("o"))) {
                                html.append(" colspan=\"7\"");
                                titleshown = false;
                            }
                            html.append(">");
                            if (titleshown) {
                                html.append(fulltitle);
                                html.append("</td>");
                            }
                            if (priSDCid.equalsIgnoreCase("WorkItem") && currentsdcid.equalsIgnoreCase("SDIWorkItem")) {
                                String attributeTypeFlag = touse.getValue(i, "attributetypeflag", "E");
                                attributeTypeFlag = "B".equalsIgnoreCase(attributeTypeFlag) ? "S" : attributeTypeFlag;
                                String attributeTypeDisplayValue = "E".equalsIgnoreCase(attributeTypeFlag) ? "Execution" : "Setup";
                                String fieldId = this.elementid + row + "_attributetypeflag";
                                TranslationProcessor tp = this.getTranslationProcessor();
                                attributeTypeDisplayValue = this.getTranslationProcessor().translate(attributeTypeDisplayValue);
                                html.append("<td class=\"gridmaint_field\">");
                                if (viewOnly || readOnly) {
                                    html.append("<span>").append(attributeTypeDisplayValue).append("</span");
                                } else {
                                    html.append("<select value=\"").append(attributeTypeFlag).append("\" id=\"").append(fieldId).append("\" class=\"").append("").append("\" name=\"").append(fieldId).append("\" style=\"min-width:160px;\" onchange=\"").append("maintAttribute.change(event,'" + this.elementid + "'," + row + ")\">");
                                    html.append("<option").append("S".equalsIgnoreCase(attributeTypeFlag) ? " SELECTED " : " ").append("value=\"").append("S").append("\">").append(tp.translate("Setup")).append("</option>");
                                    html.append("<option").append("E".equalsIgnoreCase(attributeTypeFlag) || "".equalsIgnoreCase(attributeTypeFlag) ? " SELECTED " : " ").append("value=\"").append("E").append("\">").append(tp.translate("Execution")).append("</option>");
                                    html.append("</select>");
                                }
                                html.append("</td>");
                            }
                            String editorstyle = touse.getValue(i, "editorstyleid", "");
                            String toadd = "";
                            try {
                                String id;
                                String classname;
                                if (!(def_instructionflag.equalsIgnoreCase("a") || def_instructionflag.equalsIgnoreCase("o") || currentinstructionflag.equalsIgnoreCase("a") || currentinstructionflag.equalsIgnoreCase("o"))) {
                                    EditorStyleField esf = this.getAttributeField(editorstyle, attributeid, title, i, row, touse, true, false, true, viewOnly || readOnly);
                                    String validation = esf.getColumnProperty("validation");
                                    if (validation.length() > 0) {
                                        if (validation.toLowerCase().contains("mandatory")) {
                                            if ((validation = StringUtil.replaceAll(validation, "mandatory", "", false)).startsWith(";")) {
                                                validation = validation.substring(1);
                                            }
                                            esf.setColumnProperty("validation", validation);
                                        }
                                        JSONObject valid = new JSONObject();
                                        valid.put("text", validation);
                                        valid.put("row", row);
                                        validations.put(this.elementid + row + "_" + attributeid, valid);
                                    }
                                    if (touse.getValue(i, COLUMN_VALIDFLAG, "Y").equalsIgnoreCase("N")) {
                                        esf.setColumnProperty("class", "validationfail");
                                    }
                                    toadd = esf.getHtml();
                                }
                                if (!(def_instructionflag.equalsIgnoreCase("a") || def_instructionflag.equalsIgnoreCase("o") || currentinstructionflag.equalsIgnoreCase("a") || currentinstructionflag.equalsIgnoreCase("o"))) {
                                    html.append("<td class=\"gridmaint_field\">").append(this.getLinkField("editorstyleid", "lookup", attributeid, row, touse.getValue(i, "editorstyleid", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                    html.append("<td class=\"gridmaint_field\">").append(this.getLinkField("editsdcid", "lookup", attributeid, row, touse.getValue(i, "editsdcid", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                    html.append("<td class=\"gridmaint_field\">").append(this.getLinkField("editreftypeid", "lookup", attributeid, row, touse.getValue(i, "editreftypeid", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                    html.append("<td class=\"gridmaint_field\">").append(this.getLinkField("updateableflag", "checkbox", attributeid, row, touse.getValue(i, "updateableflag", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                    html.append("<td class=\"gridmaint_field\">").append(this.getLinkField("hiddenflag", "checkbox", attributeid, row, touse.getValue(i, "hiddenflag", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                    html.append("<td class=\"gridmaint_field\" id=\"__linkeddefault").append(defaultCount).append("\">").append(toadd).append("</td>");
                                } else {
                                    String t;
                                    if (titleshown) {
                                        html.append("<td class=\"gridmaint_field\" colspan=\"6\">");
                                    }
                                    if (def_instructionflag.equalsIgnoreCase("a") || currentinstructionflag.equalsIgnoreCase("a")) {
                                        t = this.getTranslationProcessor().translate("Instruction Attribute");
                                        html.append("(").append(t).append(")");
                                    } else {
                                        t = this.getTranslationProcessor().translate("Information Only");
                                        html.append("(").append(t).append(")");
                                    }
                                    html.append("</td>");
                                }
                                boolean allowRequired = currentsdcprops == null ? true : currentsdcprops.getProperty("requiredattributesflag", "N").equalsIgnoreCase("Y");
                                String instructiontext = "";
                                if (currentinstructionflag.equalsIgnoreCase("R") || currentinstructionflag.equalsIgnoreCase("A") || currentinstructionflag.equalsIgnoreCase("O")) {
                                    instructiontext = touse.getValue(i, "instructiontext", "");
                                } else if (def_instructionflag.equalsIgnoreCase("a") || def_instructionflag.equalsIgnoreCase("o") || def_instructionflag.equalsIgnoreCase("r")) {
                                    instructiontext = touse != null ? attributedef.getProperty("instructiontext") : "";
                                }
                                html.append("<td class=\"gridmaint_field\">");
                                HTMLEditorControl editor = new HTMLEditorControl(this.logger);
                                editor.setId(this.elementid + row + "_instructiontext");
                                editor.setEditorType(HTMLEditorControl.EditorType.PRINTABLE);
                                editor.setInline(true);
                                editor.setCanUpload(true);
                                editor.setHeight("200");
                                editor.setWidth("300");
                                StringBuffer content = new StringBuffer(instructiontext);
                                editor.setContent(content.toString());
                                editor.setAutoUpdateField(true);
                                editor.setEvent("changefield", HTMLEditorControl.Events.FIELDCHANGE, "function(event){maintAttribute.change(event,'" + this.elementid + "'," + row + ");}");
                                editor.setEvent("changeeditor", HTMLEditorControl.Events.CHANGE, "function(event){maintAttribute.setChanged('" + this.elementid + "')}");
                                html.append(editor.getHtml());
                                script.append(editor.getScript());
                                if (this.renderMode == RenderMode.full) {
                                    script.append("sapphire.events.registerLoadListener(").append(editor.getInitScript("")).append(");");
                                } else {
                                    script.append(editor.getInitScript("")).append(";");
                                }
                                html.append("</td>");
                                if (!def_instructionflag.equalsIgnoreCase("O") && !currentinstructionflag.equalsIgnoreCase("O")) {
                                    html.append("<td class=\"gridmaint_field\">").append(this.getLinkField("mandatoryflag", "checkbox", attributeid, row, touse.getValue(i, "mandatoryflag", ""), dtype, viewOnly || !allowRequired || readOnly).getHtml()).append("</td>");
                                } else {
                                    html.append("<td class=\"gridmaint_field\">").append("").append("</td>");
                                }
                                if (priSDCid.equalsIgnoreCase("WorkItem")) {
                                    if ((currentsdcid.equalsIgnoreCase("DataSet") || copydownlinks.contains("LV_WorksheetItem")) && this.showContext) {
                                        classname = "";
                                        if (currentsdcid.equalsIgnoreCase("DataSet")) {
                                            classname = "mandatoryfield";
                                        }
                                        html.append("<td class=\"gridmaint_field\">");
                                        id = this.elementid + row + "_copydowncontext";
                                        if (viewOnly || readOnly) {
                                            html.append("<input type=hidden class=\"").append(this.elementid).append("_").append("copydowncontext").append(classname.length() > 0 ? " " + classname : "").append("\" id=\"").append(id).append("\" name=\"").append(id).append("\" value=\"").append(touse.getValue(i, "copydowncontext", "")).append("\">");
                                            html.append("<span id=\"").append(id + "_span").append("\"></span>");
                                        } else {
                                            html.append("<select value=\"").append(touse.getValue(i, "copydowncontext", "")).append("\" id=\"").append(id).append("\" class=\"").append(this.elementid).append("_").append("copydowncontext").append(classname.length() > 0 ? " " + classname : "").append("\" name=\"").append(id).append("\" style=\"min-width:160px;\" onchange=\"").append("maintAttribute.change(event,'" + this.elementid + "'," + row + ")\">");
                                            html.append("<option SELECTED value=\"").append(touse.getValue(i, "copydowncontext", "")).append("\">").append(touse.getValue(i, "copydowncontext", "")).append("</option>");
                                            html.append("</select>");
                                        }
                                        html.append("</td>");
                                    }
                                    if (currentsdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                                        html.append("<td class=\"gridmaint_field\">");
                                        html.append(this.getLinkField("worksheetcontext", "dropdownlist", attributeid, row, touse.getValue(i, "worksheetcontext", ""), dtype, viewOnly || readOnly).getHtml());
                                        html.append("</td>");
                                    }
                                } else if (priSDCid.equalsIgnoreCase("QCMethod") || priSDCid.equalsIgnoreCase("SampleType") || priSDCid.equalsIgnoreCase("LV_ReagentType")) {
                                    if (currentsdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                                        html.append("<td class=\"gridmaint_field\">");
                                        html.append(this.getLinkField("worksheetcontext", "dropdownlist", attributeid, row, touse.getValue(i, "worksheetcontext", ""), dtype, viewOnly || readOnly).getHtml());
                                        html.append("</td>");
                                    }
                                } else if (priSDCid.equalsIgnoreCase("LV_InstrumentModel") || priSDCid.equalsIgnoreCase("LV_InstrumentType")) {
                                    if (copydownlinks.contains("LV_WorksheetItem") && this.showContext) {
                                        html.append("<td class=\"gridmaint_field\">");
                                        id = this.elementid + row + "_copydowncontext";
                                        if (viewOnly || readOnly) {
                                            classname = "";
                                            html.append("<input type=hidden class=\"").append(this.elementid).append("_").append("copydowncontext").append(classname.length() > 0 ? " " + classname : "").append("\" id=\"").append(id).append("\" name=\"").append(id).append("\" value=\"").append(touse.getValue(i, "copydowncontext", "")).append("\">");
                                            html.append("<span id=\"").append(id + "_span").append("\"></span>");
                                        } else {
                                            html.append(this.getLinkField("copydowncontext", "dropdownlist", attributeid, row, touse.getValue(i, "copydowncontext", ""), dtype, viewOnly || readOnly).getHtml());
                                        }
                                        html.append("</td>");
                                    }
                                    if (currentsdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                                        html.append("<td class=\"gridmaint_field\">");
                                        html.append(this.getLinkField("worksheetcontext", "dropdownlist", attributeid, row, touse.getValue(i, "worksheetcontext", ""), dtype, viewOnly || readOnly).getHtml());
                                        html.append("</td>");
                                    }
                                }
                                ++defaultCount;
                            }
                            catch (Exception e) {
                                html.append("<td colspan=7 class=\"gridmaint_field\">");
                                this.renderError(html, "Could not render editor style. Error: " + e.getMessage());
                                html.append("</td>");
                            }
                        } else {
                            html.append("<td class=\"gridmaint_field\">").append(attributeid).append("</td>");
                            html.append("<td colspan=7 class=\"gridmaint_field\">");
                            this.renderError(html, "Target SDC does not allow attributes");
                            html.append("</td>");
                        }
                        html.append("</tr>");
                        rendered = true;
                    }
                    html.append("</tbody>");
                    html.append("</table>");
                    html.append("</div>");
                    html.append("</td>");
                    html.append("</tr>");
                    html.append("<tr style=\"height:10px;\">");
                    html.append("<td></td><td></td>");
                    html.append("</tr>");
                }
                if (rendered) {
                    areahtml.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" width=\"100%\">");
                    areahtml.append("<tbody>");
                    areahtml.append(html);
                    areahtml.append("</tbody>");
                    areahtml.append("</table>");
                }
                this.renderNoRecs(areahtml, rendered);
                this.renderButtons(areahtml, this.mode, viewOnly, false);
                areahtml.append("\n<style id=\"__").append(this.elementid).append("_style\">\n");
                areahtml.append(style);
                areahtml.append(".linkedattibuteidcol {");
                if (largestText.length() > 0) {
                    areahtml.append("width:").append(largestText.length() * 6).append("px;white-space: nowrap;");
                } else {
                    areahtml.append("width:100px;");
                }
                areahtml.append("}");
                areahtml.append("</style>\n");
            } else {
                this.renderWarning(areahtml, "SDC " + priSDCid + " does not have any linked sdc's to show.");
            }
        } else {
            this.renderWarning(areahtml, "SDC " + priSDCid + " does not allow attributes.");
        }
        return touse;
    }

    private DataSet renderLinkInformationMode(StringBuffer areahtml, StringBuffer script, PropertyList sdc, DataSet attributeindata, PropertyList props, int[] selected, JSONObject validations, ArrayList<String> copydownlinks, boolean viewOnly) {
        DataSet touse = attributeindata;
        String priSDCid = sdc.getProperty(PROPERTY_SDCID);
        StringBuffer html = new StringBuffer();
        if (sdc.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y")) {
            if (this.links != null && this.links.size() > 0) {
                StringBuffer style = new StringBuffer();
                String largestText = "";
                boolean rendered = false;
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.linkdef));
                touse = touse.getFilteredDataSet(filter);
                filter.put(PROPERTY_ATTRIBUTESDCID, priSDCid);
                touse = touse.getFilteredDataSet(filter, true);
                if (touse.getRowCount() > 0) {
                    String targettitle = this.getTranslationProcessor() != null ? this.getTranslationProcessor().translate("Target") + ": " : "Target: ";
                    String currentsdcid = "";
                    PropertyList currentsdcprops = null;
                    PropertyListCollection currentattributes = null;
                    boolean currentallowatts = false;
                    boolean currenttransferatts = false;
                    int defaultCount = 0;
                    boolean multipleGroups = false;
                    String c_sdcid = "";
                    for (int i = 0; i < touse.getRowCount(); ++i) {
                        String attributesdcid = touse.getValue(i, PROPERTY_ATTRIBUTESDCID, c_sdcid);
                        if (attributesdcid.equalsIgnoreCase("DataSet") && !this.showContext || !copydownlinks.contains(attributesdcid)) continue;
                        if (c_sdcid.length() == 0 || c_sdcid.equalsIgnoreCase(attributesdcid)) {
                            c_sdcid = attributesdcid;
                            continue;
                        }
                        multipleGroups = true;
                        break;
                    }
                    String type_instruction = this.getTranslationProcessor().translate("Instruction Attribute");
                    String type_information = this.getTranslationProcessor().translate("Information Attribute");
                    String type_standard = this.getTranslationProcessor().translate("Standard Attribute");
                    for (int i = 0; i < touse.getRowCount(); ++i) {
                        boolean isselected;
                        String rs = touse.getValue(i, COLUMN_ROWSTATUS, "S");
                        String attributesdcid = touse.getValue(i, PROPERTY_ATTRIBUTESDCID, currentsdcid);
                        if (attributesdcid.equalsIgnoreCase("DataSet") && !this.showContext) continue;
                        if (!copydownlinks.contains(attributesdcid)) {
                            touse.deleteRow(i);
                            --i;
                            continue;
                        }
                        if (rs.equalsIgnoreCase("D")) continue;
                        String attributeid = touse.getValue(i, PROPERTY_ATTRIBUTEID, "");
                        String dtype = touse.getValue(i, "datatype", "");
                        if (currentsdcid.length() == 0 || !currentsdcid.equalsIgnoreCase(attributesdcid)) {
                            if (currentsdcid.length() != 0) {
                                html.append("</td>");
                                html.append("</tr>");
                                html.append("</tbody>");
                                html.append("</table>");
                                html.append("</div>");
                                html.append("</td>");
                                html.append("</tr>");
                            }
                            currentsdcid = attributesdcid;
                            currentsdcprops = this.getSDCProcessor().getPropertyList(currentsdcid);
                            String grouptitle = currentsdcprops.getProperty("plural", currentsdcprops.getProperty("singular", currentsdcid));
                            grouptitle = grouptitle.substring(0, 1).toUpperCase() + grouptitle.substring(1);
                            String prefix = "" + this.elementid + "_" + attributesdcid + "_";
                            String rowId = prefix + "row";
                            String divId = prefix + "div";
                            boolean collapseGroups = props.getProperty(PROPERTY_COLLAPSEGROUPS, "N").equalsIgnoreCase("Y");
                            if (multipleGroups) {
                                html.append("<tr class=\"list_groupheader\">");
                                String click = "maintAttribute.toggleTargetGroup('" + this.elementid + "','" + prefix + "',this)";
                                html.append("<th class=\"list_grouptitle\" style=\"width:16px;cursor:pointer;\" onclick=\"").append(click).append("\"><img src=\"WEB-CORE/pagetypes/list/images/").append(collapseGroups ? "plus.gif" : "minus.gif").append("\"></th><th align=\"left\" class=\"list_grouptitle\" style=\"cursor:pointer;\" onclick=\"").append(click).append("\">").append(targettitle).append(grouptitle).append("</th>");
                                html.append("</tr>");
                            }
                            html.append("<tr id=\"").append(rowId).append("\">");
                            html.append("<td></td><td style=\"padding-left:").append(multipleGroups ? "10px" : "1px").append(";\">");
                            style.append("#").append(rowId).append("{display:").append(collapseGroups ? "none" : "table-row").append(";}\n");
                            style.append("#").append(divId).append("{display:").append("block").append(";overflow:hidden;height:100%;width:100%;}\n");
                            html.append("<div id=\"").append(divId).append("\">");
                            html.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" class=\"gridmaint_table\">");
                            this.renderLinkModeTableHeader(html, priSDCid, currentsdcid, viewOnly, true);
                            html.append("<tbody>");
                            html.append("<tr>");
                            boolean bl = currentallowatts = currentsdcprops != null && currentsdcprops.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y");
                            if (priSDCid.equalsIgnoreCase("WorkItem") && (currentsdcid.equalsIgnoreCase("DataSet") || currentsdcid.equalsIgnoreCase("SDIWorkItem") || currentsdcid.equals("LV_WorksheetItem"))) {
                                currenttransferatts = true;
                            }
                            if ((priSDCid.equalsIgnoreCase("QCMethod") || priSDCid.equalsIgnoreCase("SampleType") || priSDCid.equalsIgnoreCase("LV_ReagentType")) && currentsdcid.equals("LV_WorksheetItem")) {
                                currenttransferatts = true;
                            }
                            if (priSDCid.equalsIgnoreCase("LV_InstrumentModel") && currentsdcid.equals("LV_WorksheetItem")) {
                                currenttransferatts = true;
                            }
                            if (priSDCid.equalsIgnoreCase("LV_InstrumentType") && currentsdcid.equals("LV_WorksheetItem")) {
                                currenttransferatts = true;
                            }
                            if (priSDCid.equalsIgnoreCase("ParamList") && currentsdcid.equalsIgnoreCase("DataSet")) {
                                currenttransferatts = true;
                            } else if (priSDCid.equalsIgnoreCase("SpecSDC") && currentsdcid.equalsIgnoreCase("SDISpec")) {
                                currenttransferatts = true;
                            } else {
                                PropertyList link;
                                PropertyListCollection links;
                                PropertyListCollection propertyListCollection = links = currentsdcprops != null ? currentsdcprops.getCollection("links") : null;
                                if (links != null && (link = links.find("linksdcid", priSDCid)) != null && copydownlinks != null) {
                                    for (int cdl = 0; cdl < copydownlinks.size(); ++cdl) {
                                        try {
                                            if (!copydownlinks.get(cdl).equalsIgnoreCase(link.getProperty(PROPERTY_SDCID))) continue;
                                            currenttransferatts = true;
                                            break;
                                        }
                                        catch (Exception e) {
                                            this.logger.warn("Could not process copydown link.");
                                        }
                                    }
                                }
                            }
                            currentattributes = currentsdcprops != null ? currentsdcprops.getCollection("attributes") : null;
                        } else {
                            html.append("<tr>");
                        }
                        int row = Integer.parseInt(touse.getString(i, COLUMN_ROWNUMBER, i + ""));
                        boolean bl = isselected = selected != null && Arrays.binarySearch(selected, row) > -1;
                        if (!viewOnly) {
                            html.append("<td class=\"gridmaint_field\">");
                            html.append("<input type=\"checkbox\" name=\"").append(this.elementid).append("_selector\" id=\"__").append(this.elementid).append("_").append(row).append("\" onclick=\"maintAttribute.select(event,'").append(this.elementid).append("',").append(row).append(")\"").append(isselected ? " checked" : "").append(">");
                            html.append("</td>");
                        }
                        if (currentsdcprops == null) {
                            Logger.logWarn("Could not obtain SDC Properties.");
                        }
                        if (currentallowatts) {
                            int instance;
                            String tip;
                            String title;
                            String def_instructionflag;
                            String currentinstructionflag = touse.getValue(i, PROPERTY_INSTRUCTIONFLAG, "");
                            PropertyList attributedef = currentattributes.find(PROPERTY_ATTRIBUTEID, attributeid);
                            boolean readOnly = false;
                            if (attributedef != null) {
                                def_instructionflag = attributedef.getProperty(PROPERTY_INSTRUCTIONFLAG);
                                title = this.getTranslationProcessor().translate(attributedef.getProperty("attributetitle", attributeid));
                                if (!currenttransferatts) {
                                    tip = this.getTranslationProcessor().translate("Target SDC has no transfer of attributes defined.");
                                    readOnly = true;
                                } else {
                                    tip = this.getTranslationProcessor().translate(attributedef.getProperty("helptext", ""));
                                    if (tip.length() == 0) {
                                        tip = title;
                                    }
                                    tip = tip + " (" + attributeid + ")";
                                }
                            } else if (currentinstructionflag.equalsIgnoreCase("A") || currentinstructionflag.equalsIgnoreCase("O")) {
                                def_instructionflag = "";
                                title = "(auto)";
                                tip = attributeid;
                            } else {
                                title = "(auto)";
                                def_instructionflag = "";
                                tip = attributeid;
                            }
                            try {
                                instance = Integer.parseInt(touse.getValue(i, PROPERTY_ATTRIBUTEINSTANCE));
                            }
                            catch (Exception e) {
                                instance = -1;
                            }
                            String fulltitle = title;
                            if (instance > 1) {
                                fulltitle = fulltitle + " (" + instance + ")";
                            }
                            if (largestText.length() < fulltitle.length()) {
                                largestText = fulltitle;
                            }
                            html.append("<td id=\"").append(this.elementid).append(row).append("_").append(attributeid).append("_titlecell\" title=\"").append(SafeHTML.encodeForHTMLAttribute(tip)).append("\" class=\"gridmaint_field linkedattibuteidcol\"");
                            if (readOnly) {
                                html.append(" style=\"color:red;\"");
                            }
                            html.append(">");
                            if (title.length() != 0 || !def_instructionflag.equalsIgnoreCase("a") && def_instructionflag.equalsIgnoreCase("o") && currentinstructionflag.equalsIgnoreCase("a") && currentinstructionflag.equalsIgnoreCase("o")) {
                                html.append(fulltitle);
                            }
                            html.append("</td>");
                            String editorstyle = touse.getValue(i, "editorstyleid", "");
                            String toadd = "";
                            try {
                                String v;
                                String id;
                                String classname;
                                if (!(def_instructionflag.equalsIgnoreCase("a") || def_instructionflag.equalsIgnoreCase("o") || currentinstructionflag.equalsIgnoreCase("a") || currentinstructionflag.equalsIgnoreCase("o"))) {
                                    EditorStyleField esf = this.getAttributeField(editorstyle, attributeid, title, i, row, touse, true, false, true, viewOnly || readOnly);
                                    String validation = esf.getColumnProperty("validation");
                                    if (validation.length() > 0) {
                                        if (validation.toLowerCase().contains("mandatory")) {
                                            if ((validation = StringUtil.replaceAll(validation, "mandatory", "", false)).startsWith(";")) {
                                                validation = validation.substring(1);
                                            }
                                            esf.setColumnProperty("validation", validation);
                                        }
                                        JSONObject valid = new JSONObject();
                                        valid.put("text", validation);
                                        valid.put("row", row);
                                        validations.put(this.elementid + row + "_" + attributeid, valid);
                                    }
                                    if (touse.getValue(i, COLUMN_VALIDFLAG, "Y").equalsIgnoreCase("N")) {
                                        esf.setColumnProperty("class", "validationfail");
                                    }
                                    toadd = esf.getHtml();
                                }
                                boolean allowRequired = currentsdcprops == null ? true : currentsdcprops.getProperty("requiredattributesflag", "N").equalsIgnoreCase("Y");
                                String instructiontext = "";
                                if (currentinstructionflag.equalsIgnoreCase("R") || currentinstructionflag.equalsIgnoreCase("A") || currentinstructionflag.equalsIgnoreCase("O")) {
                                    instructiontext = touse.getValue(i, "instructiontext", "");
                                } else if (def_instructionflag.equalsIgnoreCase("a") || def_instructionflag.equalsIgnoreCase("o") || def_instructionflag.equalsIgnoreCase("r")) {
                                    instructiontext = touse != null ? attributedef.getProperty("instructiontext") : "";
                                }
                                html.append("<td class=\"gridmaint_field\">");
                                HTMLEditorControl editor = new HTMLEditorControl(this.logger);
                                editor.setId(this.elementid + row + "_instructiontext");
                                if (currentsdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                                    editor.setEditorType(HTMLEditorControl.EditorType.LESTEXT);
                                } else {
                                    editor.setEditorType(HTMLEditorControl.EditorType.PRINTABLE);
                                }
                                if (readOnly || viewOnly) {
                                    editor.setViewOnly(true);
                                }
                                editor.setInline(true);
                                editor.setCanUpload(true);
                                editor.setHeight("200");
                                editor.setWidth("650");
                                editor.setInlineWrap(true);
                                StringBuffer content = new StringBuffer(instructiontext);
                                editor.setContent(content.toString());
                                editor.setAutoUpdateField(true);
                                editor.setEvent("changefield", HTMLEditorControl.Events.FIELDCHANGE, "function(event){maintAttribute.change(event,'" + this.elementid + "'," + row + ");}");
                                editor.setEvent("changeeditor", HTMLEditorControl.Events.CHANGE, "function(event){maintAttribute.setChanged('" + this.elementid + "')}");
                                html.append(editor.getHtml());
                                script.append(editor.getScript());
                                if (this.renderMode == RenderMode.full) {
                                    script.append("sapphire.events.registerLoadListener(").append(editor.getInitScript("")).append(");");
                                } else {
                                    script.append(editor.getInitScript("")).append(";");
                                }
                                html.append("</td>");
                                if (!def_instructionflag.equalsIgnoreCase("O") && !currentinstructionflag.equalsIgnoreCase("O")) {
                                    html.append("<td class=\"gridmaint_field\">").append(this.getLinkField("mandatoryflag", "checkbox", attributeid, row, touse.getValue(i, "mandatoryflag", ""), dtype, viewOnly || !allowRequired || readOnly).getHtml()).append("</td>");
                                } else {
                                    html.append("<td class=\"gridmaint_field\">").append("").append("</td>");
                                }
                                if (priSDCid.equalsIgnoreCase("WorkItem")) {
                                    if ((currentsdcid.equalsIgnoreCase("DataSet") || copydownlinks.contains("LV_WorksheetItem")) && this.showContext) {
                                        classname = "";
                                        if (currentsdcid.equalsIgnoreCase("DataSet")) {
                                            classname = "mandatoryfield";
                                        }
                                        html.append("<td class=\"gridmaint_field\">");
                                        id = this.elementid + row + "_copydowncontext";
                                        if (viewOnly || readOnly) {
                                            html.append("<input type=hidden class=\"").append(this.elementid).append("_").append("copydowncontext").append(classname.length() > 0 ? " " + classname : "").append("\" id=\"").append(id).append("\" name=\"").append(id).append("\" value=\"").append(touse.getValue(i, "copydowncontext", "")).append("\">");
                                            html.append("<span id=\"").append(id + "_span").append("\"></span>");
                                        } else {
                                            html.append("<select ").append(viewOnly || readOnly ? "disabled" : "").append(" value=\"").append(touse.getValue(i, "copydowncontext", "")).append("\" id=\"").append(id).append("\" class=\"").append(this.elementid).append("_").append("copydowncontext").append(classname.length() > 0 ? " " + classname : "").append("\" name=\"").append(id).append("\" style=\"min-width:160px;\" onchange=\"").append("maintAttribute.change(event,'" + this.elementid + "'," + row + ")\">");
                                            html.append("<option SELECTED value=\"").append(touse.getValue(i, "copydowncontext", "")).append("\">").append(touse.getValue(i, "copydowncontext", "")).append("</option>");
                                            html.append("</select>");
                                        }
                                        html.append("</td>");
                                    }
                                    if (currentsdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                                        html.append("<td class=\"gridmaint_field\">");
                                        html.append(this.getLinkField("worksheetcontext", readOnly || viewOnly ? "readonly" : "dropdownlist", attributeid, row, touse.getValue(i, "worksheetcontext", ""), dtype, viewOnly || readOnly).getHtml());
                                        html.append("</td>");
                                    }
                                } else if (priSDCid.equalsIgnoreCase("QCMethod") || priSDCid.equalsIgnoreCase("SampleType") || priSDCid.equalsIgnoreCase("LV_ReagentType")) {
                                    if (currentsdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                                        html.append("<td class=\"gridmaint_field\">");
                                        html.append(this.getLinkField("worksheetcontext", "dropdownlist", attributeid, row, touse.getValue(i, "worksheetcontext", ""), dtype, viewOnly || readOnly).getHtml());
                                        html.append("</td>");
                                    }
                                } else if (priSDCid.equalsIgnoreCase("LV_InstrumentModel") || priSDCid.equalsIgnoreCase("LV_InstrumentType")) {
                                    if (copydownlinks.contains("LV_WorksheetItem") && this.showContext) {
                                        classname = "";
                                        html.append("<td class=\"gridmaint_field\">");
                                        id = this.elementid + row + "_copydowncontext";
                                        if (viewOnly || readOnly) {
                                            html.append("<input type=hidden class=\"").append(this.elementid).append("_").append("copydowncontext").append(classname.length() > 0 ? " " + classname : "").append("\" id=\"").append(id).append("\" name=\"").append(id).append("\" value=\"").append(touse.getValue(i, "copydowncontext", "")).append("\">");
                                            html.append("<span id=\"").append(id + "_span").append("\"></span>");
                                        } else {
                                            html.append(this.getLinkField("copydowncontext", "dropdownlist", attributeid, row, touse.getValue(i, "copydowncontext", ""), dtype, viewOnly || readOnly).getHtml());
                                        }
                                        html.append("</td>");
                                    }
                                    if (currentsdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                                        html.append("<td class=\"gridmaint_field\">");
                                        html.append(this.getLinkField("worksheetcontext", "dropdownlist", attributeid, row, touse.getValue(i, "worksheetcontext", ""), dtype, viewOnly || readOnly).getHtml());
                                        html.append("</td>");
                                    }
                                }
                                StringBuffer typeselect = new StringBuffer();
                                String typeid = this.elementid + row + "_type";
                                if (attributedef != null) {
                                    v = "";
                                    v = def_instructionflag.equalsIgnoreCase("A") ? type_instruction : (def_instructionflag.equalsIgnoreCase("O") ? type_information : type_standard);
                                    typeselect.append("<input readonly value=\"").append(v).append("\" id=\"").append(typeid).append("\" class=\"").append("").append("\" name=\"").append(typeid).append("\" style=\"min-width:160px;\">");
                                } else {
                                    v = "";
                                    v = currentinstructionflag.equalsIgnoreCase("A") ? type_instruction : (currentinstructionflag.equalsIgnoreCase("O") ? type_information : type_standard);
                                    if (viewOnly) {
                                        typeselect.append("<input readonly value=\"").append(v).append("\" id=\"").append(typeid).append("\" class=\"").append("").append("\" name=\"").append(typeid).append("\" style=\"min-width:160px;\">");
                                    } else {
                                        typeselect.append("<select value=\"").append(v).append("\" id=\"").append(typeid).append("\" class=\"").append("").append("\" name=\"").append(typeid).append("\" style=\"min-width:160px;\" onchange=\"").append("maintAttribute.change(event,'" + this.elementid + "'," + row + ")\">");
                                        typeselect.append("<option").append(currentinstructionflag.equalsIgnoreCase("A") ? " SELECTED " : " ").append("value=\"").append("A").append("\">").append(type_instruction).append("</option>");
                                        typeselect.append("<option").append(currentinstructionflag.equalsIgnoreCase("O") ? " SELECTED " : " ").append("value=\"").append("O").append("\">").append(type_information).append("</option>");
                                        typeselect.append("<option").append(v.equalsIgnoreCase(type_standard) ? " SELECTED " : " ").append("value=\"").append("R").append("\">").append(type_standard).append("</option>");
                                        typeselect.append("</select>");
                                    }
                                }
                                html.append("<td class=\"gridmaint_field\">").append(typeselect).append("</td>");
                                if (!(def_instructionflag.equalsIgnoreCase("a") || def_instructionflag.equalsIgnoreCase("o") || currentinstructionflag.equalsIgnoreCase("a") || currentinstructionflag.equalsIgnoreCase("o"))) {
                                    html.append("<td class=\"gridmaint_field\">").append(this.getLinkField("editorstyleid", "lookup", attributeid, row, touse.getValue(i, "editorstyleid", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                    html.append("<td class=\"gridmaint_field\">").append(this.getLinkField("editsdcid", "lookup", attributeid, row, touse.getValue(i, "editsdcid", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                    html.append("<td class=\"gridmaint_field\">").append(this.getLinkField("editreftypeid", "lookup", attributeid, row, touse.getValue(i, "editreftypeid", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                    html.append("<td class=\"gridmaint_field\">").append(this.getLinkField("updateableflag", "checkbox", attributeid, row, touse.getValue(i, "updateableflag", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                    html.append("<td class=\"gridmaint_field\">").append(this.getLinkField("hiddenflag", "checkbox", attributeid, row, touse.getValue(i, "hiddenflag", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                    html.append("<td class=\"gridmaint_field\" id=\"__linkeddefault").append(defaultCount).append("\">").append(toadd).append("</td>");
                                } else {
                                    String t;
                                    html.append("<td class=\"gridmaint_field\" colspan=\"6\">");
                                    if (def_instructionflag.equalsIgnoreCase("a") || currentinstructionflag.equalsIgnoreCase("a")) {
                                        t = this.getTranslationProcessor().translate("Instruction Attribute");
                                        html.append("(").append(t).append(")");
                                    } else {
                                        t = this.getTranslationProcessor().translate("Information Only");
                                        html.append("(").append(t).append(")");
                                    }
                                    html.append("</td>");
                                }
                                ++defaultCount;
                            }
                            catch (Exception e) {
                                html.append("<td colspan=7 class=\"gridmaint_field\">");
                                this.renderError(html, "Could not render editor style. Error: " + e.getMessage());
                                html.append("</td>");
                            }
                        } else {
                            html.append("<td class=\"gridmaint_field\">").append(attributeid).append("</td>");
                            html.append("<td colspan=7 class=\"gridmaint_field\">");
                            this.renderError(html, "Target SDC does not allow attributes");
                            html.append("</td>");
                        }
                        html.append("</tr>");
                        rendered = true;
                    }
                    html.append("</tbody>");
                    html.append("</table>");
                    html.append("</div>");
                    html.append("</td>");
                    html.append("</tr>");
                    html.append("<tr style=\"height:10px;\">");
                    html.append("<td></td><td></td>");
                    html.append("</tr>");
                }
                PropertyListCollection plugins = new PropertyListCollection();
                PropertyList plugin = new PropertyList();
                plugin.setProperty("pluginid", "dropzone");
                plugin.setProperty("css", "Y");
                plugin.setProperty("allowminimized", "N");
                plugins.add(plugin);
                boolean devMode = false;
                com.labvantage.sapphire.admin.system.ConfigurationProcessor config = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.pageContext);
                try {
                    devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
                }
                catch (Exception e) {
                    devMode = false;
                }
                areahtml.append(JavaScriptAPITag.getJQueryAPI(true, false, plugins, "", !devMode, this.pageContext));
                if (rendered) {
                    areahtml.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" width=\"100%\">");
                    areahtml.append("<tbody>");
                    areahtml.append(html);
                    areahtml.append("</tbody>");
                    areahtml.append("</table>");
                }
                this.renderNoRecs(areahtml, rendered);
                this.renderButtons(areahtml, this.mode, viewOnly, true);
                areahtml.append("\n<style id=\"__").append(this.elementid).append("_style\">\n");
                areahtml.append(style);
                areahtml.append(".linkedattibuteidcol {");
                if (largestText.length() > 0) {
                    areahtml.append("width:").append(largestText.length() * 6).append("px;white-space: nowrap;");
                } else {
                    areahtml.append("width:100px;");
                }
                areahtml.append("}");
                areahtml.append("</style>\n");
            } else {
                this.renderWarning(areahtml, "SDC " + priSDCid + " does not have any linked sdc's to show.");
            }
        } else {
            this.renderWarning(areahtml, "SDC " + priSDCid + " does not allow attributes.");
        }
        return touse;
    }

    private DataSet renderTemplateMode(StringBuffer html, PropertyList sdc, DataSet attributesindata, PropertyList props, int[] selected, JSONObject validations, boolean viewOnly) {
        DataSet attributedata = attributesindata;
        String priSDCid = sdc.getProperty(PROPERTY_SDCID);
        if (sdc.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y")) {
            StringBuffer style = new StringBuffer();
            String largestText = "";
            boolean rendered = false;
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.linkdef));
            filter.put(PROPERTY_ATTRIBUTESDCID, priSDCid);
            attributedata = attributedata.getFilteredDataSet(filter);
            if (attributedata.getRowCount() > 0) {
                String targettitle = this.getTranslationProcessor() != null ? this.getTranslationProcessor().translate("Target") + ": " : "Target: ";
                String currentsdcid = "";
                PropertyList currentsdcprops = null;
                PropertyListCollection currentattributes = null;
                boolean currentallowatts = false;
                boolean currenttransferatts = false;
                int defaultCount = 0;
                boolean grouped = false;
                PropertyListCollection attributesdef = sdc.getCollection("attributes");
                if (attributesdef != null && attributesdef.size() > 0) {
                    for (int i = 0; i < attributedata.getRowCount(); ++i) {
                        String attributeid = attributedata.getValue(i, PROPERTY_ATTRIBUTEID, "");
                        PropertyList attributedef = attributesdef.find(PROPERTY_ATTRIBUTEID, attributeid);
                        attributedata.setObject(i, "_attributedef", attributedef);
                        if (attributedef != null) {
                            if (!attributedata.isValidColumn(COLUMN_ATTRIBUTEGROUP)) continue;
                            String groupid = attributedef.getProperty("attributegroup", "");
                            if (groupid.length() > 0) {
                                grouped = true;
                                attributedata.setValue(i, COLUMN_ATTRIBUTEGROUP, groupid);
                                continue;
                            }
                            attributedata.setValue(i, COLUMN_ATTRIBUTEGROUP, "");
                            continue;
                        }
                        attributedata.setValue(i, COLUMN_ATTRIBUTEGROUP, "");
                    }
                } else {
                    this.logger.info("No attribute definition for grouping.");
                }
                if (grouped) {
                    attributedata.sort(COLUMN_ATTRIBUTEGROUP);
                    this.renumberRows(attributedata, 1);
                }
                TreeMap<String, StringBuffer> groupsofrows = new TreeMap<String, StringBuffer>(this.comparator);
                for (int i = 0; i < attributedata.getRowCount(); ++i) {
                    boolean isselected;
                    String groupid;
                    String rs = attributedata.getValue(i, COLUMN_ROWSTATUS, "S");
                    if (rs.equalsIgnoreCase("D")) continue;
                    String attributeid = attributedata.getValue(i, PROPERTY_ATTRIBUTEID, "");
                    String attributesdcid = attributedata.getValue(i, PROPERTY_ATTRIBUTESDCID, currentsdcid);
                    String dtype = attributedata.getValue(i, "datatype", "");
                    String string = groupid = grouped ? attributedata.getValue(i, COLUMN_ATTRIBUTEGROUP, DEFAULT_GROUP) : DEFAULT_GROUP;
                    if (!groupsofrows.containsKey(groupid)) {
                        groupsofrows.put(groupid, new StringBuffer());
                    }
                    StringBuffer currentRowHTML = (StringBuffer)groupsofrows.get(groupid);
                    if (currentsdcid.length() == 0 || !currentsdcid.equalsIgnoreCase(attributesdcid)) {
                        currentsdcid = attributesdcid;
                        currentsdcprops = this.getSDCProcessor().getPropertyList(currentsdcid);
                        String prefix = "" + this.elementid + "_" + attributesdcid + "_";
                        String rowId = prefix + "row";
                        String divId = prefix + "div";
                        boolean collapseGroups = props.getProperty(PROPERTY_COLLAPSEGROUPS, "N").equalsIgnoreCase("Y");
                        currentRowHTML.append("<tr>");
                        currentallowatts = currentsdcprops != null && currentsdcprops.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y");
                        currenttransferatts = true;
                        currentattributes = currentsdcprops != null ? currentsdcprops.getCollection("attributes") : null;
                    } else {
                        currentRowHTML.append("<tr>");
                    }
                    int row = Integer.parseInt(attributedata.getString(i, COLUMN_ROWNUMBER, i + ""));
                    boolean bl = isselected = selected != null && Arrays.binarySearch(selected, row) > -1;
                    if (!viewOnly) {
                        currentRowHTML.append("<td class=\"gridmaint_field\">");
                        currentRowHTML.append("<input type=\"checkbox\" name=\"").append(this.elementid).append("_selector\" id=\"__").append(this.elementid).append("_").append(row).append("\" onclick=\"maintAttribute.select(event,'").append(this.elementid).append("',").append(row).append(")\"").append(isselected ? " checked" : "").append(">");
                        currentRowHTML.append("</td>");
                    }
                    if (currentsdcprops == null) {
                        Logger.logWarn("Could not obtain SDC Properties.");
                    }
                    if (currentallowatts) {
                        int instance;
                        String tip;
                        String title;
                        String def_instructionflag;
                        String currentinstructionflag = attributedata.getValue(i, PROPERTY_INSTRUCTIONFLAG, "");
                        PropertyList attributedef = currentattributes.find(PROPERTY_ATTRIBUTEID, attributeid);
                        boolean readOnly = false;
                        if (attributedef != null) {
                            def_instructionflag = attributedef.getProperty(PROPERTY_INSTRUCTIONFLAG);
                            title = this.getTranslationProcessor().translate(attributedef.getProperty("attributetitle", attributeid));
                            if (!currenttransferatts) {
                                tip = this.getTranslationProcessor().translate("Target SDC has no transfer of attributes defined.");
                                readOnly = true;
                            } else {
                                tip = this.getTranslationProcessor().translate(attributedef.getProperty("helptext", ""));
                                if (tip.length() == 0) {
                                    tip = title;
                                }
                                tip = tip + " (" + attributeid + ")";
                            }
                        } else if (currentinstructionflag.equalsIgnoreCase("A") || currentinstructionflag.equalsIgnoreCase("O")) {
                            def_instructionflag = "";
                            title = "";
                            tip = "";
                        } else {
                            title = attributeid;
                            def_instructionflag = "";
                            tip = this.getTranslationProcessor().translate("Target SDC has no definition for attribute");
                            if (!currenttransferatts) {
                                tip = tip + " " + this.getTranslationProcessor().translate("and Target SDC has no transfer of attributes defined.");
                            }
                            readOnly = true;
                        }
                        try {
                            instance = Integer.parseInt(attributedata.getValue(i, PROPERTY_ATTRIBUTEINSTANCE));
                        }
                        catch (Exception e) {
                            instance = -1;
                        }
                        String fulltitle = title;
                        if (instance > 1) {
                            fulltitle = fulltitle + " (" + instance + ")";
                        }
                        if (largestText.length() < fulltitle.length()) {
                            largestText = fulltitle;
                        }
                        boolean titleshown = true;
                        currentRowHTML.append("<td id=\"").append(this.elementid).append(row).append("_").append(attributeid).append("_titlecell\" title=\"").append(SafeHTML.encodeForHTMLAttribute(tip)).append("\" class=\"gridmaint_field linkedattibuteidcol\"");
                        if (readOnly) {
                            currentRowHTML.append(" style=\"color:red;\"");
                        }
                        if (!(title.length() != 0 || !def_instructionflag.equalsIgnoreCase("a") && def_instructionflag.equalsIgnoreCase("o") && currentinstructionflag.equalsIgnoreCase("a") && currentinstructionflag.equalsIgnoreCase("o"))) {
                            currentRowHTML.append(" colspan=\"7\"");
                            titleshown = false;
                        }
                        currentRowHTML.append(">");
                        if (titleshown) {
                            currentRowHTML.append(fulltitle);
                            currentRowHTML.append("</td>");
                        }
                        String editorstyle = attributedata.getValue(i, "editorstyleid", "");
                        String toadd = "";
                        try {
                            if (!(def_instructionflag.equalsIgnoreCase("a") || def_instructionflag.equalsIgnoreCase("o") || currentinstructionflag.equalsIgnoreCase("a") || currentinstructionflag.equalsIgnoreCase("o"))) {
                                EditorStyleField esf = this.getAttributeField(editorstyle, attributeid, title, i, row, attributedata, true, false, true, viewOnly || readOnly);
                                String validation = esf.getColumnProperty("validation");
                                if (validation.length() > 0) {
                                    if (validation.toLowerCase().contains("mandatory")) {
                                        if ((validation = StringUtil.replaceAll(validation, "mandatory", "", false)).startsWith(";")) {
                                            validation = validation.substring(1);
                                        }
                                        esf.setColumnProperty("validation", validation);
                                    }
                                    JSONObject valid = new JSONObject();
                                    valid.put("text", validation);
                                    valid.put("row", row);
                                    validations.put(this.elementid + row + "_" + attributeid, valid);
                                }
                                if (attributedata.getValue(i, COLUMN_VALIDFLAG, "Y").equalsIgnoreCase("N")) {
                                    esf.setColumnProperty("class", "validationfail");
                                }
                                toadd = esf.getHtml();
                            }
                            if (!(def_instructionflag.equalsIgnoreCase("a") || def_instructionflag.equalsIgnoreCase("o") || currentinstructionflag.equalsIgnoreCase("a") || currentinstructionflag.equalsIgnoreCase("o"))) {
                                currentRowHTML.append("<td class=\"gridmaint_field\">").append(this.getLinkField("editorstyleid", "lookup", attributeid, row, attributedata.getValue(i, "editorstyleid", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                currentRowHTML.append("<td class=\"gridmaint_field\">").append(this.getLinkField("editsdcid", "lookup", attributeid, row, attributedata.getValue(i, "editsdcid", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                currentRowHTML.append("<td class=\"gridmaint_field\">").append(this.getLinkField("editreftypeid", "lookup", attributeid, row, attributedata.getValue(i, "editreftypeid", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                currentRowHTML.append("<td class=\"gridmaint_field\">").append(this.getLinkField("updateableflag", "checkbox", attributeid, row, attributedata.getValue(i, "updateableflag", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                currentRowHTML.append("<td class=\"gridmaint_field\">").append(this.getLinkField("hiddenflag", "checkbox", attributeid, row, attributedata.getValue(i, "hiddenflag", ""), dtype, viewOnly || readOnly).getHtml()).append("</td>");
                                currentRowHTML.append("<td class=\"gridmaint_field\" id=\"__linkeddefault").append(defaultCount).append("\">").append(toadd).append("</td>");
                            } else {
                                String t;
                                if (titleshown) {
                                    currentRowHTML.append("<td class=\"gridmaint_field\" colspan=\"6\">");
                                }
                                if (def_instructionflag.equalsIgnoreCase("a") || currentinstructionflag.equalsIgnoreCase("a")) {
                                    t = this.getTranslationProcessor().translate("Instruction Attribute");
                                    currentRowHTML.append("(").append(t).append(")");
                                } else {
                                    t = this.getTranslationProcessor().translate("Information Only");
                                    currentRowHTML.append("(").append(t).append(")");
                                }
                                currentRowHTML.append("</td>");
                            }
                            boolean allowRequired = currentsdcprops == null ? true : currentsdcprops.getProperty("requiredattributesflag", "N").equalsIgnoreCase("Y");
                            String instructiontext = "";
                            if (currentinstructionflag.equalsIgnoreCase("R") || currentinstructionflag.equalsIgnoreCase("A") || currentinstructionflag.equalsIgnoreCase("O")) {
                                instructiontext = attributedata.getValue(i, "instructiontext", "");
                            } else if (def_instructionflag.equalsIgnoreCase("a") || def_instructionflag.equalsIgnoreCase("o") || def_instructionflag.equalsIgnoreCase("r")) {
                                instructiontext = attributedata != null ? attributedef.getProperty("instructiontext") : "";
                            }
                            currentRowHTML.append("<td class=\"gridmaint_field\">");
                            HTMLEditorControl editor = new HTMLEditorControl(this.logger);
                            editor.setId(this.elementid + row + "_instructiontext");
                            editor.setEditorType(HTMLEditorControl.EditorType.PRINTABLE);
                            editor.setInline(true);
                            editor.setCanUpload(true);
                            editor.setHeight("200");
                            editor.setWidth("300");
                            StringBuffer content = new StringBuffer(instructiontext);
                            editor.setContent(content.toString());
                            editor.setAutoUpdateField(true);
                            editor.setEvent("changefield", HTMLEditorControl.Events.FIELDCHANGE, "function(event){maintAttribute.change(event,'" + this.elementid + "'," + row + ");}");
                            editor.setEvent("changeeditor", HTMLEditorControl.Events.CHANGE, "function(event){maintAttribute.setChanged('" + this.elementid + "')}");
                            currentRowHTML.append(editor.getHtml());
                            this.script.append(editor.getScript());
                            if (this.renderMode == RenderMode.full) {
                                this.script.append("sapphire.events.registerLoadListener(").append(editor.getInitScript("")).append(");");
                            } else {
                                this.script.append(editor.getInitScript("")).append(";");
                            }
                            currentRowHTML.append("</td>");
                            if (!def_instructionflag.equalsIgnoreCase("O") && !currentinstructionflag.equalsIgnoreCase("O")) {
                                currentRowHTML.append("<td class=\"gridmaint_field\">").append(this.getLinkField("mandatoryflag", "checkbox", attributeid, row, attributedata.getValue(i, "mandatoryflag", ""), dtype, viewOnly || !allowRequired || readOnly).getHtml()).append("</td>");
                            } else {
                                currentRowHTML.append("<td class=\"gridmaint_field\">").append("").append("</td>");
                            }
                            ++defaultCount;
                        }
                        catch (Exception e) {
                            currentRowHTML.append("<td colspan=7 class=\"gridmaint_field\">");
                            this.renderError(currentRowHTML, "Could not render editor style. Error: " + e.getMessage());
                            currentRowHTML.append("</td>");
                        }
                    } else {
                        currentRowHTML.append("<td class=\"gridmaint_field\">").append(attributeid).append("</td>");
                        currentRowHTML.append("<td colspan=7 class=\"gridmaint_field\">");
                        this.renderError(currentRowHTML, "Target SDC does not allow attributes");
                        currentRowHTML.append("</td>");
                    }
                    currentRowHTML.append("</tr>");
                    rendered = true;
                }
                html.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" width=\"100%\">");
                html.append("<tbody>");
                for (Map.Entry groupRowEntry : groupsofrows.entrySet()) {
                    String prefix = "" + this.elementid + "_" + MaintAttribute.getPrefixKey(groupRowEntry.getKey()) + "_";
                    String rowId = prefix + "row";
                    String divId = prefix + "div";
                    String grouptitle = groupRowEntry.getKey();
                    boolean def = grouptitle.equalsIgnoreCase(DEFAULT_GROUP);
                    if (def && groupsofrows.size() <= 1) {
                        this.renderLinkModeTableHeader(html, priSDCid, currentsdcid, viewOnly, false);
                    } else {
                        boolean collapseGroups = props.getProperty(PROPERTY_COLLAPSEGROUPS, "N").equalsIgnoreCase("Y");
                        if (def) {
                            grouptitle = this.getTranslationProcessor().translate("No Grouping");
                        }
                        html.append("<tr class=\"list_groupheader\">");
                        String click = "maintAttribute.toggleTargetGroup('" + this.elementid + "','" + prefix + "',this)";
                        html.append("<th class=\"list_grouptitle\" style=\"width:16px;cursor:pointer;\" onclick=\"").append(click).append("\"><img src=\"WEB-CORE/pagetypes/list/images/").append(collapseGroups ? "plus.gif" : "minus.gif").append("\"></th><th align=\"left\" class=\"list_grouptitle\" style=\"cursor:pointer;\" onclick=\"").append(click).append("\">").append(grouptitle).append("</th>");
                        html.append("</tr>");
                        html.append("<tr id=\"").append(rowId).append("\">");
                        html.append("<td></td><td style=\"padding-left:").append("10px").append(";\">");
                        style.append("#").append(rowId).append("{display:").append(collapseGroups ? "none" : "table-row").append(";}\n");
                        style.append("#").append(divId).append("{display:").append("block").append(";overflow:hidden;height:100%;width:100%;}\n");
                        html.append("<div id=\"").append(divId).append("\">");
                        html.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" class=\"maintform_table\">");
                        this.renderLinkModeTableHeader(html, priSDCid, currentsdcid, viewOnly, false);
                        html.append("<tbody>");
                    }
                    html.append((StringBuffer)groupRowEntry.getValue());
                    if (def && groupsofrows.size() <= 1) continue;
                    html.append("</tbody>");
                    html.append("</table>");
                    html.append("</div>");
                    html.append("</td>");
                    html.append("</tr>");
                }
                html.append("</tbody>");
                html.append("</table>");
            }
            this.renderNoRecs(html, rendered);
            this.renderButtons(html, this.mode, viewOnly, false);
            html.append("\n<style id=\"__").append(this.elementid).append("_style\">\n");
            html.append(style);
            html.append(".linkedattibuteidcol {");
            if (largestText.length() > 0) {
                html.append("width:").append(largestText.length() * 6).append("px;white-space: nowrap;");
            } else {
                html.append("width:100px;");
            }
            html.append("}");
            html.append("</style>\n");
        } else {
            this.renderWarning(html, "SDC " + priSDCid + " does not allow attributes.");
        }
        return attributedata;
    }

    private void renderNoRecs(StringBuffer html, boolean records) {
        String display = !records ? "block" : "none";
        html.append("<div id=\"__").append(this.elementid).append("_norows\" style=\"display:").append(display).append(";\" >");
        if (this.pageContext != null) {
            html.append(this.getTranslationProcessor().translate("No Attributes found"));
        } else {
            html.append("No Attributes found");
        }
        html.append("</div>");
    }

    private void renderButtons(StringBuffer html, MaintenanceMode mode, boolean viewOnly, boolean informationMode) {
        TranslationProcessor tp = this.getTranslationProcessor();
        if (!viewOnly) {
            html.append("<table cellpadding=\"2\" cellspacing=\"0\" border=\"0\">");
            html.append("<tbody><tr>");
            String modetext = mode == MaintenanceMode.adhoc ? MODE_ADHOC : MODE_LINK;
            Button button = new Button(this.pageContext);
            button.setAction("maintAttribute.moveUp('" + this.elementid + "')");
            button.setTip(tp != null ? tp.translate("Move " + modetext + " attribute up") : "Move " + modetext + " attribute up");
            button.setWidth("40");
            button.setImg("WEB-CORE/images/png/MoveUp.png");
            html.append("<td>");
            html.append(button.getHtml());
            html.append("</td>");
            button = new Button(this.pageContext);
            button.setAction("maintAttribute.moveDown('" + this.elementid + "')");
            button.setTip(tp != null ? tp.translate("Move " + modetext + " attribute down") : "Move " + modetext + " attribute down");
            button.setWidth("40");
            button.setImg("WEB-CORE/images/png/MoveDown.png");
            html.append("<td>");
            html.append(button.getHtml());
            html.append("</td>");
            button = new Button(this.pageContext);
            button.setAction("maintAttribute.add('" + this.elementid + "')");
            if (informationMode) {
                button.setText(tp != null ? tp.translate("Add Existing") : "Add Existing");
                button.setTip(tp != null ? tp.translate("Add new existing defined " + modetext + " attribute") : "Add new existing defined " + modetext + " attribute");
            } else {
                button.setText(tp != null ? tp.translate("Add") : "Add");
                button.setTip(tp != null ? tp.translate("Add new " + modetext + " attribute") : "Add new " + modetext + " attribute");
            }
            button.setWidth("75");
            button.setImg("WEB-CORE/images/png/AddRow.png");
            html.append("<td>");
            html.append(button.getHtml());
            html.append("</td>");
            if (mode != MaintenanceMode.adhoc && informationMode) {
                button = new Button(this.pageContext);
                button.setAction("maintAttribute.addInfo('" + this.elementid + "')");
                button.setText(tp != null ? tp.translate("Create Attribute") : "Create Attribute");
                button.setTip(tp != null ? tp.translate("Add a new attribute which is not defined for the SDC") : "Add a new attribute which is not defined for the SDC");
                button.setWidth("75");
                button.setImg("WEB-CORE/images/png/AddRow.png");
                html.append("<td>");
                html.append(button.getHtml());
                html.append("</td>");
            }
            button = new Button(this.pageContext);
            button.setAction("maintAttribute.remove('" + this.elementid + "')");
            button.setText(tp != null ? tp.translate("Remove") : "Remove");
            button.setTip(tp != null ? tp.translate("Remove " + modetext + " attribute") : "Remove " + modetext + " attribute");
            button.setWidth("75");
            button.setImg("WEB-CORE/images/png/RemoveRow.png");
            html.append("<td>");
            html.append(button.getHtml());
            html.append("</td>");
            html.append("</tr></tbody>");
            html.append("</table>");
        }
    }

    private DataSet renderAdhocMode(StringBuffer areahtml, PropertyList sdc, DataSet attributeindata, PropertyList props, int[] selected, JSONObject validations, boolean viewOnly) {
        DataSet attributedata = attributeindata;
        if (sdc.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y")) {
            StringBuffer style = new StringBuffer();
            boolean valuesonly = this.element.getProperty("valuesonly", "N").equalsIgnoreCase("Y");
            PropertyListCollection attributesdef = sdc.getCollection("attributes");
            if (attributesdef != null) {
                if (attributesdef.size() == 0) {
                    this.logger.warn("No attributes have been defined for SDC " + sdc.getProperty(PROPERTY_SDCID) + " in master list");
                }
                boolean allowRequired = sdc.getProperty("requiredattributesflag", "N").equalsIgnoreCase("Y");
                boolean grouped = false;
                attributedata.addColumn("_attributedef", -1);
                for (int i = 0; i < attributedata.getRowCount(); ++i) {
                    String attributeid = attributedata.getValue(i, PROPERTY_ATTRIBUTEID, "");
                    PropertyList attributedef = attributesdef.find(PROPERTY_ATTRIBUTEID, attributeid);
                    attributedata.setObject(i, "_attributedef", attributedef);
                    if (attributedef != null) {
                        if (!attributedata.isValidColumn(COLUMN_ATTRIBUTEGROUP)) continue;
                        String groupid = attributedef.getProperty("attributegroup", "");
                        if (groupid.length() > 0) {
                            grouped = true;
                            attributedata.setValue(i, COLUMN_ATTRIBUTEGROUP, groupid);
                            continue;
                        }
                        attributedata.setValue(i, COLUMN_ATTRIBUTEGROUP, "");
                        continue;
                    }
                    attributedata.setValue(i, COLUMN_ATTRIBUTEGROUP, "");
                }
                if (grouped) {
                    attributedata.sort(COLUMN_ATTRIBUTEGROUP);
                    this.renumberRows(attributedata, 1);
                }
                boolean rendered = false;
                TreeMap<String, StringBuffer> groupsofrows = new TreeMap<String, StringBuffer>(this.comparator);
                HashMap<String, DataSet> refTypeMap = new HashMap<String, DataSet>();
                for (int i = 0; i < attributedata.getRowCount(); ++i) {
                    boolean isselected;
                    String defaultEditorStyle;
                    String tip;
                    String title;
                    String groupid;
                    String def_instructionflag;
                    String rs;
                    attributedata.addColumn("__rendered", 0);
                    BaseSDIAttributeAction.AttributeType type = BaseSDIAttributeAction.getAttributeTypeFromString(attributedata.getValue(i, "attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.linkdef)));
                    if (type == BaseSDIAttributeAction.AttributeType.linkdef || (rs = attributedata.getValue(i, COLUMN_ROWSTATUS, "S")).equalsIgnoreCase("D")) continue;
                    attributedata.setValue(i, "__rendered", "Y");
                    int row = Integer.parseInt(attributedata.getString(i, COLUMN_ROWNUMBER, i + ""));
                    boolean hidden = attributedata.getValue(i, "hiddenflag", "N").equalsIgnoreCase("Y");
                    boolean mandatory = allowRequired && attributedata.getValue(i, "mandatoryflag", "N").equalsIgnoreCase("Y");
                    String attributeid = attributedata.getValue(i, PROPERTY_ATTRIBUTEID, "");
                    BaseSDIAttributeAction.AttributeType attributetype = BaseSDIAttributeAction.getAttributeTypeFromString(attributedata.getValue(i, "attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.adhoc)));
                    PropertyList attributedef = (PropertyList)attributedata.getObject(i, "_attributedef");
                    attributedata.setObject(i, "_attributedef", null);
                    boolean readOnly = false;
                    String current_instructionflag = attributedata.getValue(i, PROPERTY_INSTRUCTIONFLAG, "N");
                    if (attributedef != null) {
                        def_instructionflag = attributedef.getProperty(PROPERTY_INSTRUCTIONFLAG, attributeid);
                        groupid = grouped ? attributedata.getValue(i, COLUMN_ATTRIBUTEGROUP, DEFAULT_GROUP) : DEFAULT_GROUP;
                        title = this.getTranslationProcessor().translate(attributedef.getProperty("attributetitle", ""));
                        tip = this.getTranslationProcessor().translate(attributedef.getProperty("helptext", ""));
                        if (tip.length() == 0) {
                            tip = title.length() > 0 ? title : attributeid;
                        }
                        tip = tip + " (" + attributeid + " ";
                        switch (attributetype) {
                            case adhoc: {
                                tip = tip + " " + this.getTranslationProcessor().translate("adhoc added");
                                break;
                            }
                            case link: {
                                tip = tip + " " + this.getTranslationProcessor().translate("from") + " " + attributedata.getValue(i, "sourcesdcid", "");
                                break;
                            }
                            case sdc: {
                                tip = tip + " " + this.getTranslationProcessor().translate("auto added");
                            }
                        }
                        tip = tip + ")";
                        defaultEditorStyle = attributedef.getProperty("editorstyleid", current_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("A") ? "Yes No Checkbox" : "");
                    } else if (current_instructionflag.equalsIgnoreCase("A") || current_instructionflag.equalsIgnoreCase("O")) {
                        def_instructionflag = "N";
                        groupid = DEFAULT_GROUP;
                        tip = "";
                        title = "";
                        defaultEditorStyle = current_instructionflag.equalsIgnoreCase("A") ? "Yes No Checkbox" : "";
                    } else {
                        if (current_instructionflag.equalsIgnoreCase("R")) {
                            title = "";
                            tip = "";
                            current_instructionflag = "R";
                        } else if (current_instructionflag.equalsIgnoreCase("M")) {
                            title = attributeid;
                            tip = attributeid;
                            current_instructionflag = "M";
                        } else {
                            readOnly = true;
                            title = attributeid;
                            tip = "Attribute no longer defined in master list.";
                            current_instructionflag = "N";
                        }
                        def_instructionflag = "N";
                        defaultEditorStyle = "";
                        groupid = DEFAULT_GROUP;
                    }
                    if (!groupsofrows.containsKey(groupid)) {
                        groupsofrows.put(groupid, new StringBuffer());
                    }
                    StringBuffer currentRowHTML = (StringBuffer)groupsofrows.get(groupid);
                    currentRowHTML.append("<tr").append(hidden ? " style=\"display:none;\"" : "").append(">");
                    boolean bl = isselected = selected != null && Arrays.binarySearch(selected, row) > -1;
                    if (!viewOnly && !valuesonly) {
                        currentRowHTML.append("<td class=\"maintform_fieldtitle\">");
                        currentRowHTML.append("<input type=\"checkbox\" name=\"").append(this.elementid).append("_selector\" id=\"__").append(this.elementid).append("_").append(row).append("\" onclick=\"maintAttribute.select(event,'").append(this.elementid).append("',").append(row).append(")\"").append(isselected ? " checked" : "").append(" ").append(hidden ? " disabled" : "").append(">");
                        currentRowHTML.append("</td>");
                    }
                    String instructiontext = "";
                    if (attributedata.getValue(i, "sourcekeyid1", "").length() > 0 && (current_instructionflag.equalsIgnoreCase("R") || current_instructionflag.equalsIgnoreCase("O") || current_instructionflag.equalsIgnoreCase("A"))) {
                        String k3;
                        SafeSQL safeSQL = new SafeSQL();
                        StringBuffer sql = new StringBuffer();
                        sql.append("SELECT instructiontext FROM sdiattribute ");
                        sql.append("WHERE ");
                        sql.append("attributeid=").append(safeSQL.addVar(attributeid)).append(" ");
                        sql.append("AND ");
                        sql.append("sdcid=").append(safeSQL.addVar(attributedata.getValue(i, "sourcesdcid", ""))).append(" ");
                        sql.append("AND ");
                        sql.append("keyid1=").append(safeSQL.addVar(attributedata.getValue(i, "sourcekeyid1", ""))).append(" ");
                        String k2 = attributedata.getValue(i, "sourcekeyid2", "");
                        if (k2.length() > 0) {
                            sql.append("AND ");
                            sql.append("keyid2=").append(safeSQL.addVar(k2)).append(" ");
                        }
                        if ((k3 = attributedata.getValue(i, "sourcekeyid3", "")).length() > 0) {
                            sql.append("AND ");
                            sql.append("keyid3=").append(safeSQL.addVar(k3)).append(" ");
                        }
                        sql.append("AND ");
                        sql.append("attributeinstance=").append(safeSQL.addVar(attributedata.getValue(i, "sourceattributeinstance", ""))).append("");
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
                        if (ds != null && ds.size() > 0) {
                            instructiontext = ds.getValue(0, "instructiontext", "");
                        } else {
                            this.logger.warn("Could not obtain source of adhoc attribute.");
                        }
                    } else if (def_instructionflag.equalsIgnoreCase("R") || def_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("O")) {
                        instructiontext = attributedef.getProperty("instructiontext");
                    }
                    currentRowHTML.append("<td id=\"").append(this.elementid).append(row).append("_").append(attributeid).append("_titlecell\" title=\"").append(SafeHTML.encodeForHTMLAttribute(tip)).append("\"  style=\"border-left-color:#CCCCCC;color:").append(readOnly ? "red" : "black").append(";\"");
                    boolean showtitle = true;
                    if (title.length() == 0) {
                        if (current_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("A")) {
                            currentRowHTML.append(" colspan=\"2\"");
                            showtitle = false;
                        } else if (current_instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("O")) {
                            currentRowHTML.append(" colspan=\"3\"");
                            showtitle = false;
                        } else if (current_instructionflag.equalsIgnoreCase("M") || def_instructionflag.equalsIgnoreCase("M")) {
                            currentRowHTML.append(instructiontext.length() > 0 ? " " : " colspan=\"2\"");
                            title = attributeid;
                        } else if (attributedef == null) {
                            currentRowHTML.append(instructiontext.length() > 0 ? " colspan=\"2\"" : " colspan=\"3\"");
                            showtitle = false;
                        } else {
                            currentRowHTML.append(instructiontext.length() > 0 ? " " : " colspan=\"2\"");
                            title = attributeid;
                        }
                        currentRowHTML.append(" class=\"maintform_field\"");
                    } else if (instructiontext.length() == 0) {
                        currentRowHTML.append(" class=\"maintform_fieldtitle\"");
                        currentRowHTML.append(" colspan=\"2\"");
                    } else {
                        currentRowHTML.append(" class=\"maintform_fieldtitle\"");
                    }
                    currentRowHTML.append(">");
                    if (showtitle) {
                        int instance;
                        currentRowHTML.append(SafeHTML.encodeForHTMLAttribute(title));
                        try {
                            instance = Integer.parseInt(attributedata.getValue(i, PROPERTY_ATTRIBUTEINSTANCE));
                        }
                        catch (Exception e) {
                            instance = -1;
                        }
                        if (instance > 1) {
                            currentRowHTML.append(" (").append(instance).append(")");
                        }
                        currentRowHTML.append("</td>");
                    }
                    if (showtitle) {
                        if (def_instructionflag.equalsIgnoreCase("O") || current_instructionflag.equalsIgnoreCase("O")) {
                            currentRowHTML.append("<td class=\"maintform_field\" colspan=\"2\">");
                        } else if (instructiontext.length() == 0) {
                            currentRowHTML.append("<td class=\"maintform_field\">");
                        } else {
                            currentRowHTML.append("<td class=\"maintform_field\">");
                        }
                    }
                    if (instructiontext.length() > 0) {
                        String siid = this.elementid + row + "_instructiontext";
                        currentRowHTML.append("<div id=\"").append(siid).append("\" style=\"display:block;\">");
                        currentRowHTML.append(instructiontext);
                        currentRowHTML.append("</div>");
                    }
                    if (!def_instructionflag.equalsIgnoreCase("O") && !current_instructionflag.equalsIgnoreCase("O") && instructiontext.length() > 0) {
                        currentRowHTML.append("</td>");
                    }
                    if (!def_instructionflag.equalsIgnoreCase("O") && !current_instructionflag.equalsIgnoreCase("O")) {
                        String editorstyle = attributedata.getValue(i, "editorstyleid", defaultEditorStyle);
                        try {
                            StringBuffer toadd = new StringBuffer();
                            if (readOnly || viewOnly) {
                                PropertyList editorstyleprops;
                                String sdcid = attributedata.getValue(row, "editsdcid", "");
                                String reftypeid = attributedata.getValue(row, "editreftypeid", "");
                                try {
                                    PropertyList propertyList = editorstyleprops = editorstyle.length() > 0 ? EditorStyleField.getEditorStyleProperties(editorstyle, sdcid, reftypeid, this.getConnectionProcessor().getSapphireConnection(), this.getQueryProcessor()) : null;
                                    if (reftypeid.length() == 0) {
                                        reftypeid = editorstyleprops.getProperty("reftypeid");
                                    }
                                }
                                catch (Exception e) {
                                    editorstyleprops = null;
                                }
                                toadd.append("<div class=\"value_field\">");
                                String v = MaintAttribute.getAttributeValue(this.m18nServer, this.connectionInfo, attributedata, i, false, false);
                                if (reftypeid.length() > 0) {
                                    DataSet dsRefType = new DataSet();
                                    if (refTypeMap.containsKey(reftypeid)) {
                                        dsRefType = (DataSet)refTypeMap.get(reftypeid);
                                    } else {
                                        dsRefType = this.getQueryProcessor().getRefTypeDataSet(reftypeid);
                                        refTypeMap.put(reftypeid, dsRefType);
                                    }
                                    int r = dsRefType.findRow("refvalueid", v);
                                    if (r > -1) {
                                        v = dsRefType.getValue(r, "refdisplayvalue", v);
                                    }
                                }
                                if (editorstyleprops != null && editorstyleprops.getProperty(PROPERTY_MODE).equalsIgnoreCase("checkbox")) {
                                    toadd.append("<input type=\"checkbox\" ").append(v.equalsIgnoreCase("Y") ? " checked" : "").append(" onclick=\"return false;\">");
                                } else {
                                    toadd.append(SafeHTML.encodeForHTMLAttribute(v));
                                }
                                toadd.append("</div>");
                            } else {
                                EditorStyleField esf = this.getAttributeField(editorstyle, attributeid, title, i, row, attributedata, false, hidden, attributedata.getValue(i, "updateableflag", "Y").equalsIgnoreCase("Y"), viewOnly || readOnly);
                                if (attributedata.getValue(i, COLUMN_VALIDFLAG, "Y").equalsIgnoreCase("N")) {
                                    esf.setColumnProperty("class", "validationfail");
                                }
                                String validation = esf.getColumnProperty("validation");
                                if (mandatory && this.element != null && this.element.getProperty("mandatory").equalsIgnoreCase("Y")) {
                                    validation = validation.length() > 0 ? "Mandatory;" + validation : "Mandatory";
                                    esf.setColumnProperty("validation", validation);
                                }
                                if (validation.length() > 0 && !hidden) {
                                    JSONObject valid = new JSONObject();
                                    valid.put("text", validation);
                                    valid.put("row", row);
                                    validations.put(this.elementid + row + "_" + attributeid, valid);
                                }
                                toadd.append(esf.getHtml());
                            }
                            if (instructiontext.length() > 0) {
                                currentRowHTML.append("<td class=\"maintform_field\" nowrap>");
                            }
                            currentRowHTML.append(toadd);
                            if (mandatory) {
                                currentRowHTML.append("<span title=\"").append(this.getTranslationProcessor().translate("Required")).append("\">(R)</span>");
                            }
                            currentRowHTML.append("</td>");
                        }
                        catch (Exception e) {
                            if (instructiontext.length() > 0) {
                                currentRowHTML.append("<td class=\"maintform_field\" nowrap>");
                            } else if (showtitle) {
                                currentRowHTML.append("<td class=\"maintform_field\" nowrap>");
                            }
                            this.renderError(currentRowHTML, "Could not render editor style. Error: " + e.getMessage());
                            currentRowHTML.append("</td>");
                        }
                    } else {
                        currentRowHTML.append("</td>");
                    }
                    currentRowHTML.append("</tr>");
                    if (rendered) continue;
                    rendered = true;
                }
                areahtml.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\"").append(groupsofrows.size() < 2 && groupsofrows.containsKey(DEFAULT_GROUP) ? "" : " width=\"100%\"").append(">");
                areahtml.append("<tbody>");
                for (Map.Entry groupRowEntry : groupsofrows.entrySet()) {
                    String prefix = "" + this.elementid + "_" + MaintAttribute.getPrefixKey(groupRowEntry.getKey()) + "_";
                    String rowId = prefix + "row";
                    String divId = prefix + "div";
                    String grouptitle = groupRowEntry.getKey();
                    boolean def = grouptitle.equalsIgnoreCase(DEFAULT_GROUP);
                    if (!def || groupsofrows.size() > 1) {
                        boolean collapseGroups = props.getProperty(PROPERTY_COLLAPSEGROUPS, "N").equalsIgnoreCase("Y");
                        if (def) {
                            grouptitle = this.getTranslationProcessor().translate("No Grouping");
                        }
                        areahtml.append("<tr class=\"list_groupheader\">");
                        String click = "maintAttribute.toggleTargetGroup('" + this.elementid + "','" + prefix + "',this)";
                        areahtml.append("<th class=\"list_grouptitle\" style=\"width:16px;cursor:pointer;\" onclick=\"").append(click).append("\"><img src=\"WEB-CORE/pagetypes/list/images/").append(collapseGroups ? "plus.gif" : "minus.gif").append("\"></th><th align=\"left\" class=\"list_grouptitle\" style=\"cursor:pointer;\" onclick=\"").append(click).append("\">").append(SafeHTML.encodeForHTMLAttribute(grouptitle)).append("</th>");
                        areahtml.append("</tr>");
                        areahtml.append("<tr id=\"").append(rowId).append("\">");
                        areahtml.append("<td></td><td style=\"padding-left:").append("10px").append(";\">");
                        style.append("#").append(rowId).append("{display:").append(collapseGroups ? "none" : "table-row").append(";}\n");
                        style.append("#").append(divId).append("{display:").append("block").append(";overflow:hidden;height:100%;width:100%;}\n");
                        areahtml.append("<div id=\"").append(divId).append("\">");
                        areahtml.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" class=\"maintform_table\">");
                        areahtml.append("<tbody>");
                    }
                    areahtml.append((StringBuffer)groupRowEntry.getValue());
                    if (def && groupsofrows.size() <= 1) continue;
                    areahtml.append("</tbody>");
                    areahtml.append("</table>");
                    areahtml.append("</div>");
                    areahtml.append("</td>");
                    areahtml.append("</tr>");
                }
                areahtml.append("</tbody>");
                areahtml.append("</table>");
                this.renderNoRecs(areahtml, rendered);
                this.renderButtons(areahtml, this.mode, viewOnly || valuesonly, false);
                areahtml.append("\n<style id=\"__").append(this.elementid).append("_style\">\n");
                areahtml.append(style);
                areahtml.append("</style>");
            } else {
                this.renderWarning(areahtml, "Could not find attribute definitions.");
            }
        } else {
            this.renderWarning(areahtml, "SDC " + sdc.getProperty(PROPERTY_SDCID) + " does not allow attributes.");
        }
        return attributedata;
    }

    public void removeAttribute(String attributeid, String attributesdcid, String attributeinstance) throws SapphireException {
        DataSet ds = this.sdiInfo.getDataSet("attribute");
        String[] attributeids = StringUtil.split(attributeid, ";");
        String[] attributesdcids = StringUtil.split(attributesdcid, ";");
        String[] attributeinstances = StringUtil.split(attributeinstance, ";");
        if (attributeids.length == attributesdcids.length && attributeids.length == attributeinstances.length) {
            for (int i = 0; i < attributeids.length; ++i) {
                HashMap<String, Object> hmfind = new HashMap<String, Object>(3);
                hmfind.put(PROPERTY_ATTRIBUTEID, attributeids[i]);
                hmfind.put(PROPERTY_ATTRIBUTESDCID, attributesdcids[i]);
                hmfind.put(PROPERTY_ATTRIBUTEINSTANCE, new BigDecimal(attributeinstances[i]));
                int row = ds.findRow(hmfind);
                if (row <= -1) continue;
                String rs = ds.getValue(row, COLUMN_ROWSTATUS, "S");
                if (rs.equalsIgnoreCase("I")) {
                    ds.deleteRow(row);
                    continue;
                }
                ds.setValue(row, COLUMN_ROWSTATUS, "D");
            }
        } else {
            throw new SapphireException("Incorrect attributeid, attributesdcid or attributeinstance provided.");
        }
        this.renumberRows(this.attributes, 1);
    }

    public ArrayList<String> removeAttribute(JSONArray jrows) throws SapphireException {
        DataSet ds = this.sdiInfo.getDataSet("attribute");
        ArrayList<String> removedList = new ArrayList<String>();
        if (jrows.length() > 0) {
            for (int i = 0; i < jrows.length(); ++i) {
                int row;
                HashMap<String, String> hmfind = new HashMap<String, String>(1);
                try {
                    hmfind.put(COLUMN_ROWNUMBER, "" + jrows.getInt(i));
                    row = ds.findRow(hmfind);
                }
                catch (Exception e) {
                    row = -1;
                }
                if (row <= -1) continue;
                String rs = ds.getValue(row, COLUMN_ROWSTATUS, "S");
                removedList.add(ds.getValue(row, PROPERTY_ATTRIBUTEID) + ";" + ds.getValue(row, PROPERTY_ATTRIBUTESDCID) + ";" + ds.getValue(row, PROPERTY_ATTRIBUTEINSTANCE));
                if (rs.equalsIgnoreCase("I")) {
                    ds.deleteRow(row);
                    continue;
                }
                ds.setValue(row, COLUMN_ROWSTATUS, "D");
                HashMap<String, String> findMap2 = new HashMap<String, String>();
                findMap2.put(COLUMN_ROWSTATUS, "I");
                findMap2.put(PROPERTY_SDCID, ds.getValue(row, PROPERTY_SDCID));
                findMap2.put(PROPERTY_KEYID1, ds.getValue(row, PROPERTY_KEYID1));
                findMap2.put(PROPERTY_KEYID2, ds.getValue(row, PROPERTY_KEYID2));
                findMap2.put(PROPERTY_KEYID3, ds.getValue(row, PROPERTY_KEYID3));
                findMap2.put(PROPERTY_ATTRIBUTEID, ds.getValue(row, PROPERTY_ATTRIBUTEID));
                findMap2.put(PROPERTY_ATTRIBUTEINSTANCE, ds.getValue(row, PROPERTY_ATTRIBUTEINSTANCE));
                int row1 = ds.findRow(findMap2);
                if (row1 <= -1) continue;
                ds.deleteRow(row1);
            }
        } else {
            throw new SapphireException("Incorrect attributeid, attributesdcid or attributeinstance provided.");
        }
        this.renumberRows(this.attributes, 1);
        return removedList;
    }

    private void moveAttribute(DataSet rowcalculator, DataSet filtered, DataSet full, int move) throws SapphireException {
        if (rowcalculator.getRowCount() > 0) {
            int i;
            for (i = 0; i < rowcalculator.getRowCount(); ++i) {
                int row = rowcalculator.getBigDecimal(i, "row").intValue();
                int destination = row + move;
                if (move > 1 && destination >= filtered.getRowCount()) {
                    destination = filtered.getRowCount() - 1;
                    this.logger.info("Bottom of attribute rows reached. Move to End.");
                } else if (move < -1 && destination < 0) {
                    destination = 0;
                    this.logger.info("Top of attribute rows reached. Move to Start.");
                }
                if (destination < filtered.getRowCount()) {
                    if (destination > -1) {
                        int realdestination;
                        int realorigin;
                        try {
                            int temporigin = Integer.parseInt(filtered.getValue(row, COLUMN_ROWNUMBER, "0"));
                            int tempdestination = Integer.parseInt(filtered.getValue(destination, COLUMN_ROWNUMBER, "0"));
                            HashMap<String, String> find = new HashMap<String, String>();
                            find.put(COLUMN_ROWNUMBER, "" + temporigin);
                            realorigin = full.findRow(find);
                            find = new HashMap();
                            find.put(COLUMN_ROWNUMBER, "" + tempdestination);
                            realdestination = full.findRow(find);
                        }
                        catch (Exception e) {
                            realorigin = -1;
                            realdestination = -1;
                            throw new SapphireException("Could not obtain row numbers.");
                        }
                        if (realdestination <= -1 || realorigin <= -1) continue;
                        if (this.mode == MaintenanceMode.linkdef || this.mode == MaintenanceMode.template) {
                            String originsdc = full.getValue(realorigin, PROPERTY_ATTRIBUTESDCID, "");
                            String destsdc = full.getValue(realdestination, PROPERTY_ATTRIBUTESDCID, "");
                            if (originsdc.length() > 0 && destsdc.length() > 0 && originsdc.equals(destsdc)) {
                                int realmove = realdestination - realorigin;
                                rowcalculator.setNumber(i, "origin", realorigin);
                                rowcalculator.setNumber(i, "move", realmove);
                                continue;
                            }
                            throw new SapphireException("Cannot move rows across target.");
                        }
                        int realmove = realdestination - realorigin;
                        rowcalculator.setNumber(i, "origin", realorigin);
                        rowcalculator.setNumber(i, "move", realmove);
                        continue;
                    }
                    throw new SapphireException("Row cannot be moved upwards.");
                }
                throw new SapphireException("Row cannot be moved downwards.");
            }
            if (move < 0) {
                rowcalculator.sort("origin");
            } else {
                rowcalculator.sort("origin d");
            }
            for (i = 0; i < rowcalculator.getRowCount(); ++i) {
                int realorigin = rowcalculator.getBigDecimal(i, "origin").intValue();
                int realmove = rowcalculator.getBigDecimal(i, "move").intValue();
                full.moveRow(realorigin, realmove);
            }
        } else {
            throw new SapphireException("Could not find attribute(s).");
        }
        this.renumberRows(this.attributes, 1);
    }

    public void moveAttribute(JSONArray jrows, int move) throws SapphireException {
        DataSet rowcalculator;
        DataSet filtered;
        DataSet ds = this.sdiInfo.getDataSet("attribute");
        if (jrows.length() > 0) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.linkdef));
            if (this.mode == MaintenanceMode.adhoc) {
                filtered = ds.getFilteredDataSet(filter, true);
                filter = new HashMap();
                filter.put("hiddenflag", "N");
                filtered = filtered.getFilteredDataSet(filter);
                filter = new HashMap();
                filter.put(COLUMN_ROWSTATUS, "D");
                filtered = filtered.getFilteredDataSet(filter, true);
            } else if (this.mode == MaintenanceMode.template) {
                filter.put(PROPERTY_ATTRIBUTESDCID, this.sdiInfo.getSdcid());
                filtered = ds.getFilteredDataSet(filter);
                filter = new HashMap();
                filter.put(COLUMN_ROWSTATUS, "D");
                filtered = filtered.getFilteredDataSet(filter, true);
            } else {
                filtered = ds.getFilteredDataSet(filter);
                filter = new HashMap();
                filter.put(COLUMN_ROWSTATUS, "D");
                filter.put(PROPERTY_ATTRIBUTESDCID, this.sdiInfo.getSdcid());
                filtered = filtered.getFilteredDataSet(filter, true);
            }
            rowcalculator = new DataSet();
            rowcalculator.addColumn("row", 1);
            rowcalculator.addColumn("origin", 1);
            rowcalculator.addColumn("move", 1);
            for (int i = 0; i < jrows.length(); ++i) {
                int row;
                HashMap<String, String> hmfind = new HashMap<String, String>(1);
                try {
                    hmfind.put(COLUMN_ROWNUMBER, "" + jrows.getInt(i));
                    row = filtered.findRow(hmfind);
                }
                catch (Exception e) {
                    row = -1;
                }
                if (row <= -1) {
                    throw new SapphireException("Row cannot be moved upwards.");
                }
                int r = rowcalculator.addRow();
                rowcalculator.setNumber(r, "row", row);
            }
        } else {
            throw new SapphireException("No rows selected.");
        }
        this.moveAttribute(rowcalculator, filtered, ds, move);
    }

    public void moveAttribute(String attributeid, String attributesdcid, String attributeinstance, int move) throws SapphireException {
        DataSet rowcalculator;
        DataSet filtered;
        DataSet ds = this.sdiInfo.getDataSet("attribute");
        String[] attributeids = StringUtil.split(attributeid, ";");
        String[] attributesdcids = StringUtil.split(attributesdcid, ";");
        String[] attributeinstances = StringUtil.split(attributeinstance, ";");
        if (attributeids.length == attributesdcids.length && attributeids.length == attributeinstances.length) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.linkdef));
            if (this.mode == MaintenanceMode.adhoc) {
                filtered = ds.getFilteredDataSet(filter, true);
                filter = new HashMap();
                filter.put("hiddenflag", "N");
                filtered = filtered.getFilteredDataSet(filter);
                filter = new HashMap();
                filter.put(COLUMN_ROWSTATUS, "D");
                filtered = filtered.getFilteredDataSet(filter, true);
            } else if (this.mode == MaintenanceMode.template) {
                filter.put(PROPERTY_ATTRIBUTESDCID, this.sdiInfo.getSdcid());
                filtered = ds.getFilteredDataSet(filter);
                filter = new HashMap();
                filter.put(COLUMN_ROWSTATUS, "D");
                filtered = filtered.getFilteredDataSet(filter, true);
            } else {
                filtered = ds.getFilteredDataSet(filter);
                filter = new HashMap();
                filter.put(COLUMN_ROWSTATUS, "D");
                filter.put(PROPERTY_ATTRIBUTESDCID, this.sdiInfo.getSdcid());
                filtered = filtered.getFilteredDataSet(filter, true);
            }
            rowcalculator = new DataSet();
            rowcalculator.addColumn("row", 1);
            rowcalculator.addColumn("origin", 1);
            rowcalculator.addColumn("move", 1);
            for (int i = 0; i < attributeids.length; ++i) {
                HashMap<String, Object> hmfind = new HashMap<String, Object>(3);
                hmfind.put(PROPERTY_ATTRIBUTEID, attributeids[i]);
                hmfind.put(PROPERTY_ATTRIBUTESDCID, attributesdcids[i]);
                hmfind.put(PROPERTY_ATTRIBUTEINSTANCE, new BigDecimal(attributeinstances[i]));
                int row = filtered.findRow(hmfind);
                if (row <= -1) {
                    throw new SapphireException("Row cannot be moved upwards.");
                }
                int r = rowcalculator.addRow();
                rowcalculator.setNumber(r, "row", row);
            }
        } else {
            throw new SapphireException("Incorrect attributeid, attributesdcid or attributeinstance provided.");
        }
        this.moveAttribute(rowcalculator, filtered, ds, move);
    }

    private void renumberRows(DataSet attributedata, int seed) {
        MaintAttribute.renumberAttributeRows(attributedata, seed);
    }

    public static void renumberAttributeRows(DataSet attributedata, int seed) {
        int i;
        int us_adhoc = 1;
        int us_link = 1;
        String prevsdcid = "";
        int rowid = 0;
        ArrayList<Integer> rowtonumber = new ArrayList<Integer>();
        for (i = 0; i < attributedata.getRowCount(); ++i) {
            String existingrowid = attributedata.getValue(i, COLUMN_ROWNUMBER, "");
            if (existingrowid.length() == 0) {
                rowtonumber.add(new Integer(i));
            } else {
                try {
                    int iexistingrow = Integer.parseInt(existingrowid);
                    if (iexistingrow > rowid) {
                        rowid = iexistingrow;
                    }
                }
                catch (Exception iexistingrow) {
                    // empty catch block
                }
            }
            BaseSDIAttributeAction.AttributeType type = BaseSDIAttributeAction.getAttributeTypeFromString(attributedata.getValue(i, "attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.adhoc)));
            String rs = attributedata.getValue(i, COLUMN_ROWSTATUS, "S");
            if (!rs.equalsIgnoreCase("D")) {
                if (type == BaseSDIAttributeAction.AttributeType.linkdef) {
                    int cus_link;
                    String currsdcid = attributedata.getValue(i, PROPERTY_ATTRIBUTESDCID, "");
                    if (prevsdcid.length() > 0 && !prevsdcid.equals(currsdcid)) {
                        us_link = 1;
                    }
                    prevsdcid = currsdcid;
                    try {
                        cus_link = Integer.parseInt(attributedata.getValue(i, "usersequence", "-1"));
                    }
                    catch (Exception e) {
                        cus_link = -1;
                    }
                    if (cus_link == -1 || cus_link != us_link) {
                        attributedata.setValue(i, "usersequence", "" + us_link);
                        if (rs.equalsIgnoreCase("S")) {
                            if (attributedata.isValidColumn("__rendered")) {
                                if ("Y".equals(attributedata.getValue(i, "__rendered", "N"))) {
                                    attributedata.setValue(i, COLUMN_ROWSTATUS, "U");
                                }
                            } else {
                                attributedata.setValue(i, COLUMN_ROWSTATUS, "U");
                            }
                        }
                    }
                    ++us_link;
                } else {
                    int cus_adhoc;
                    try {
                        cus_adhoc = Integer.parseInt(attributedata.getValue(i, "usersequence", "-1"));
                    }
                    catch (Exception e) {
                        cus_adhoc = -1;
                    }
                    if (cus_adhoc == -1 || cus_adhoc != us_adhoc) {
                        attributedata.setValue(i, "usersequence", "" + us_adhoc);
                        if (rs.equalsIgnoreCase("S")) {
                            if (attributedata.isValidColumn("__rendered")) {
                                if ("Y".equals(attributedata.getValue(i, "__rendered", "N"))) {
                                    attributedata.setValue(i, COLUMN_ROWSTATUS, "U");
                                }
                            } else {
                                attributedata.setValue(i, COLUMN_ROWSTATUS, "U");
                            }
                        }
                    }
                    ++us_adhoc;
                }
            }
            ++seed;
        }
        for (i = 0; i < rowtonumber.size(); ++i) {
            String rs = attributedata.getValue(i, COLUMN_ROWSTATUS, "S");
            int row = (Integer)rowtonumber.get(i);
            attributedata.setValue(row, COLUMN_ROWNUMBER, "" + (rowid + (i + seed)));
            if (!rs.equalsIgnoreCase("S")) continue;
            if (attributedata.isValidColumn("__rendered")) {
                if (!"Y".equals(attributedata.getValue(i, "__rendered", "N"))) continue;
                attributedata.setValue(i, COLUMN_ROWSTATUS, "U");
                continue;
            }
            attributedata.setValue(i, COLUMN_ROWSTATUS, "U");
        }
    }

    public ArrayList<String> addAttribute(String attributeid, String attributesdcid, String instructionFlag, String usersequence) throws SapphireException {
        return this.addAttribute(attributeid, attributesdcid, "", instructionFlag, usersequence);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public ArrayList<String> addAttribute(String attributeid, String attributesdcid, String datatype, String instructionFlag, String usersequence) throws SapphireException {
        SDCProcessor sdcProc = this.getSDCProcessor();
        PropertyList sdcprops = sdcProc.getPropertyList(this.sdiInfo.getSdcid());
        ArrayList<String> addedList = new ArrayList<String>();
        if (sdcprops == null) throw new SapphireException("Could not obtain SDC Properties.");
        String rsetid = this.sdiInfo.getSDIData().getRsetid();
        String sdcid = this.sdiInfo.getSdcid();
        DataSet pr = this.sdiInfo.getDataSet("primary");
        if (pr == null || pr.getRowCount() <= 0) throw new SapphireException("Could not obtain primary data.");
        String keyid1 = pr.getValue(0, sdcprops.getProperty("keycolid1"), "");
        String keyid2 = "";
        if (sdcprops.getProperty("keycolid2").length() > 0) {
            keyid2 = pr.getValue(0, sdcprops.getProperty("keycolid2"), "");
        }
        String keyid3 = "";
        if (sdcprops.getProperty("keycolid3").length() > 0) {
            keyid3 = pr.getValue(0, sdcprops.getProperty("keycolid3"), "");
        }
        M18NUtil m18n = new M18NUtil(this.connectionInfo);
        HashMap<String, ArrayList<String>> skipped = new HashMap<String, ArrayList<String>>();
        BaseSDIAttributeAction.createAttributeData(this.sdiInfo.getDataSet("attribute"), new DataSet(), sdcprops, keyid1, keyid2, keyid3, attributeid, attributesdcid, datatype, this.mode == MaintenanceMode.linkdef ? BaseSDIAttributeAction.AttributeType.linkdef : (this.mode == MaintenanceMode.template ? BaseSDIAttributeAction.AttributeType.linkdef : BaseSDIAttributeAction.AttributeType.adhoc), "", "", "", "", "", "", "", instructionFlag, "", "", "", "", usersequence, skipped, false, rsetid, ";", sdcProc, this.getQueryProcessor(), m18n, this.connectionInfo, this.logger);
        BaseSDIAttributeAction.logSkipped(skipped, sdcid, this.logger);
        for (int i = 0; i < this.attributes.getRowCount(); ++i) {
            if (!"I".equals(this.attributes.getValue(i, COLUMN_ROWSTATUS))) continue;
            addedList.add(this.attributes.getValue(i, PROPERTY_ATTRIBUTEID) + ";" + this.attributes.getValue(i, PROPERTY_ATTRIBUTESDCID) + ";" + this.attributes.getValue(i, PROPERTY_ATTRIBUTEINSTANCE));
        }
        this.attributes.sort(PROPERTY_ATTRIBUTESDCID);
        this.renumberRows(this.attributes, 1);
        return addedList;
    }

    public SDITagInfo createSDIInfo(String rsetid, String sdcid, String keyid1, String keyid2, String keyid3, DataSet attributedata) {
        HashMap<String, QueryData> querymap = new HashMap<String, QueryData>();
        DataSet primary = new DataSet();
        PropertyList sdc = this.getSDCProcessor().getPropertyList(sdcid);
        primary.addColumn(sdc.getProperty("keycolid1"), 0);
        if (keyid2 != null && keyid2.length() > 0) {
            primary.addColumn(sdc.getProperty("keycolid2"), 0);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            primary.addColumn(sdc.getProperty("keycolid3"), 0);
        }
        int row = primary.addRow();
        primary.setValue(row, sdc.getProperty("keycolid1"), keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            primary.setValue(row, sdc.getProperty("keycolid2"), keyid2);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            primary.setValue(row, sdc.getProperty("keycolid3"), keyid3);
        }
        querymap.put("primary", new QueryData("primary", primary));
        querymap.put("attribute", new QueryData("attribute", attributedata));
        SDITagInfo sdiinfo = new SDITagInfo(querymap);
        SDIData sdi = new SDIData();
        sdi.setSdcid(sdcid);
        sdiinfo.setSdcid(sdcid);
        sdi.setRsetid(rsetid);
        sdiinfo.setSDIData(sdi);
        return sdiinfo;
    }

    public static void saveAttributeData(String attributesvalue, ActionProcessor actionProc, M18NUtil m18n) throws SapphireException {
        DataSet filteredDataSet = new DataSet();
        try {
            DataSet ds = new DataSet();
            ds.setM18NUtil(m18n);
            ds.setJSONObject(new JSONObject(attributesvalue));
            if (ds != null && ds.isValidColumn("instructiontext")) {
                for (int r = 0; r < ds.getRowCount(); ++r) {
                    String c = ds.getClob(r, "instructiontext", "");
                    if (c.length() <= 0) continue;
                    StringBuffer contents = new StringBuffer(HttpUtil.decodeURIComponent(c));
                    HTMLEditorControl.processImages(contents, true, actionProc.getConnectionid());
                    ds.setClob(r, "instructiontext", contents.toString());
                }
            }
            if (!ds.isValidColumn("emptyattribute")) {
                ds.addColumn("emptyattribute", 0);
            }
            for (int count = 0; count < ds.size(); ++count) {
                if (!OpalUtil.isEmpty(ds.getString(count, "emptyattribute"))) continue;
                ds.setValue(count, "emptyattribute", "false");
            }
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("emptyattribute", "false");
            filteredDataSet = ds.getFilteredDataSet(filter);
        }
        catch (Exception e) {
            Logger.logError("Failed to process attributes.", e);
            throw new SapphireException("Could not load attribute data.", e);
        }
        BaseSDIAttributeAction.saveAttributeData(filteredDataSet, actionProc, m18n);
    }

    @Override
    public boolean isVisibleInAddMode() {
        return true;
    }

    public static int renderSingleSDIAttributes(StringBuilder areahtml, PropertyList sdc, DataSet attributedata, boolean grouped, int rowcount, PropertyList config, String cssTable, String cssTitle, String cssValue, String jsobject, String elementid, int instructOnly, boolean fullwidth, Attributes attributes, TranslationProcessor tp, QueryProcessor qp, ConnectionProcessor cp, SapphireConnection sapphireConnection, Logger logger) throws SapphireException {
        int instructionOnlyCount = instructOnly;
        StringBuffer style = new StringBuffer();
        if (config == null) {
            config = new PropertyList();
        }
        PropertyListCollection attributesdef = sdc.getCollection("attributes");
        boolean allowRequired = sdc.getProperty("requiredattributesflag", "N").equalsIgnoreCase("Y");
        if (grouped) {
            attributedata.sort(COLUMN_ATTRIBUTEGROUP);
            MaintAttribute.renumberAttributeRows(attributedata, 1);
        }
        boolean rendered = false;
        M18NUtil m18nServer = new M18NUtil();
        TreeMap<String, StringBuffer> groupsofrows = new TreeMap<String, StringBuffer>(new Comparator<String>(){

            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        if (config.getProperty("hidebordersinfoonly").equals("Y")) {
            boolean infoonly = true;
            for (int i = 0; i < attributedata.size() && infoonly; ++i) {
                infoonly = attributedata.getValue(i, PROPERTY_INSTRUCTIONFLAG).equals("O");
            }
            if (infoonly) {
                cssTable = "";
                cssTitle = "";
                cssValue = "";
            }
        }
        for (int i = 0; i < attributedata.getRowCount(); ++i) {
            String siid;
            String defaultEditorStyle;
            String tip;
            String title;
            String groupid;
            String def_instructionflag;
            String rs;
            BaseSDIAttributeAction.AttributeType type = BaseSDIAttributeAction.getAttributeTypeFromString(attributedata.getValue(i, "attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.linkdef)));
            if (type == BaseSDIAttributeAction.AttributeType.linkdef || (rs = attributedata.getValue(i, COLUMN_ROWSTATUS, "S")).equalsIgnoreCase("D")) continue;
            int row = Integer.parseInt(attributedata.getString(i, COLUMN_ROWNUMBER, i + ""));
            boolean hidden = attributedata.getValue(i, "hiddenflag", "N").equalsIgnoreCase("Y");
            boolean mandatory = allowRequired && attributedata.getValue(i, "mandatoryflag", "N").equalsIgnoreCase("Y");
            String attributeid = attributedata.getValue(i, PROPERTY_ATTRIBUTEID, "");
            BaseSDIAttributeAction.AttributeType attributetype = BaseSDIAttributeAction.getAttributeTypeFromString(attributedata.getValue(i, "attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.adhoc)));
            PropertyList attributedef = (PropertyList)attributedata.getObject(i, "_attributedef");
            attributedata.setObject(i, "_attributedef", null);
            boolean readOnly = false;
            String current_instructionflag = attributedata.getValue(i, PROPERTY_INSTRUCTIONFLAG, "N");
            if (attributedef != null) {
                def_instructionflag = attributedef.getProperty(PROPERTY_INSTRUCTIONFLAG, attributeid);
                groupid = grouped ? attributedata.getValue(i, COLUMN_ATTRIBUTEGROUP, DEFAULT_GROUP) : DEFAULT_GROUP;
                title = tp.translate(attributedef.getProperty("attributetitle", ""));
                tip = tp.translate(attributedef.getProperty("helptext", ""));
                if (tip.length() == 0) {
                    tip = title.length() > 0 ? title : attributeid;
                }
                tip = tip + " (" + attributeid + " ";
                switch (attributetype) {
                    case adhoc: {
                        tip = tip + " " + tp.translate("adhoc added");
                        break;
                    }
                    case link: {
                        tip = tip + " " + tp.translate("from") + " " + attributedata.getValue(i, "sourcesdcid", "");
                        break;
                    }
                    case sdc: {
                        tip = tip + " " + tp.translate("auto added");
                    }
                }
                tip = tip + ")";
                defaultEditorStyle = attributedef.getProperty("editorstyleid", "");
            } else if (current_instructionflag.equalsIgnoreCase("A") || current_instructionflag.equalsIgnoreCase("O")) {
                def_instructionflag = "N";
                groupid = DEFAULT_GROUP;
                tip = "";
                title = "";
                defaultEditorStyle = "";
            } else {
                if (current_instructionflag.equalsIgnoreCase("R")) {
                    title = "";
                    tip = "";
                    current_instructionflag = "R";
                } else {
                    readOnly = true;
                    title = attributeid;
                    tip = "Attribute no longer defined in master list.";
                    current_instructionflag = "N";
                }
                def_instructionflag = "N";
                defaultEditorStyle = "";
                groupid = DEFAULT_GROUP;
            }
            if (!groupsofrows.containsKey(groupid)) {
                groupsofrows.put(groupid, new StringBuffer());
            }
            StringBuffer currentRowHTML = (StringBuffer)groupsofrows.get(groupid);
            currentRowHTML.append("<tr").append(hidden ? " style=\"display:none;\"" : "").append(">");
            String instructiontext = "";
            if (current_instructionflag.equalsIgnoreCase("R") || current_instructionflag.equalsIgnoreCase("O") || current_instructionflag.equalsIgnoreCase("A")) {
                String k3;
                StringBuffer sql = new StringBuffer();
                SafeSQL safeSQL = new SafeSQL();
                sql.append("SELECT instructiontext FROM sdiattribute ");
                sql.append(" WHERE ");
                sql.append("attributeid = ").append(safeSQL.addVar(attributeid));
                sql.append(" AND ");
                sql.append("sdcid = ").append(safeSQL.addVar(attributedata.getValue(i, "sourcesdcid", "")));
                sql.append(" AND ");
                sql.append(" keyid1 = ").append(safeSQL.addVar(attributedata.getValue(i, "sourcekeyid1", "")));
                String k2 = attributedata.getValue(i, "sourcekeyid2", "");
                if (k2.length() > 0) {
                    sql.append(" AND ");
                    sql.append("keyid2 = ").append(safeSQL.addVar(k2));
                }
                if ((k3 = attributedata.getValue(i, "sourcekeyid3", "")).length() > 0) {
                    sql.append(" AND ");
                    sql.append(" keyid3 = ").append(safeSQL.addVar(k3));
                }
                sql.append(" AND ");
                sql.append(" attributeinstance = ").append(safeSQL.addVar(attributedata.getValue(i, "sourceattributeinstance", "")));
                DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
                if (ds != null && ds.size() > 0) {
                    instructiontext = ds.getValue(0, "instructiontext", "");
                } else {
                    logger.warn("Could not obtain source of adhoc attribute.");
                }
            } else if (def_instructionflag.equalsIgnoreCase("R") || def_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("O")) {
                instructiontext = attributedef.getProperty("instructiontext");
            }
            boolean showtitle = true;
            if (title.length() == 0) {
                if (current_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("A")) {
                    showtitle = false;
                } else if (current_instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("O")) {
                    showtitle = false;
                    ++instructionOnlyCount;
                } else if (attributedef == null) {
                    showtitle = false;
                } else {
                    title = attributeid;
                }
            }
            if (def_instructionflag.equalsIgnoreCase("O") || current_instructionflag.equalsIgnoreCase("O")) {
                currentRowHTML.append("<td class=\"" + cssValue + "\" colspan=\"2\">");
            } else {
                currentRowHTML.append("<td class=\"" + cssValue + "\">");
            }
            if (showtitle) {
                siid = elementid + row + "_title";
                currentRowHTML.append("<div id=\"").append(siid).append("\" style=\"display:block;font-weight:bold;\">");
                currentRowHTML.append(SafeHTML.encodeForHTMLAttribute(title));
                currentRowHTML.append("</div>");
            }
            if (instructiontext.length() > 0) {
                siid = elementid + row + "_instructiontext";
                currentRowHTML.append("<div id=\"").append(siid).append("\" style=\"display:block;\">");
                currentRowHTML.append(instructiontext);
                currentRowHTML.append("</div>");
            }
            if (!def_instructionflag.equalsIgnoreCase("O") && !current_instructionflag.equalsIgnoreCase("O")) {
                PropertyList editorstyleprops;
                currentRowHTML.append("</td>");
                String editorstyle = current_instructionflag.equals("A") ? "Yes No Checkbox" : attributedata.getValue(i, "editorstyleid", defaultEditorStyle);
                StringBuffer toadd = new StringBuffer();
                String sdcid = attributedata.getValue(row, "editsdcid", "");
                String reftypeid = attributedata.getValue(row, "editreftypeid", "");
                try {
                    editorstyleprops = editorstyle.length() > 0 ? EditorStyleField.getEditorStyleProperties(editorstyle, sdcid, reftypeid, cp.getSapphireConnection(), qp) : null;
                }
                catch (Exception e) {
                    editorstyleprops = null;
                }
                String v = MaintAttribute.getAttributeValue(m18nServer, cp.getConnectionInfo(sapphireConnection.getConnectionId()), attributedata, i, false, false);
                toadd.append("<div class=\"value_field\"").append(v.length() > 0 ? "" : " style=\"font-style: italic;\"").append(">");
                if (editorstyleprops != null && editorstyleprops.getProperty(PROPERTY_MODE).equalsIgnoreCase("checkbox")) {
                    toadd.append("<input type=\"checkbox\" ").append(v.equalsIgnoreCase("Y") ? " checked" : "").append(" onclick=\"return false;\">");
                } else {
                    toadd.append(v.length() > 0 ? v : tp.translate("Undefined"));
                }
                toadd.append("</div>");
                currentRowHTML.append("<td nowrap class=\"" + cssValue + "\">");
                currentRowHTML.append(toadd);
                if (mandatory) {
                    currentRowHTML.append("<span title=\"").append(tp.translate("Required")).append("\">(R)</span>");
                }
                currentRowHTML.append("</td>");
            } else {
                currentRowHTML.append("</td>");
            }
            currentRowHTML.append("</tr>");
            if (rendered) continue;
            rendered = true;
        }
        areahtml.append("<table class=\"" + cssTable + "\" " + (fullwidth ? "width=\"100%\"" : "") + ">");
        areahtml.append("<tbody>");
        for (Map.Entry groupRowEntry : groupsofrows.entrySet()) {
            String key = StringUtil.replaceAll(groupRowEntry.getKey(), " ", "_");
            key = StringUtil.replaceAll(key, "/", "_sl_");
            String prefix = "" + elementid + "_" + MaintAttribute.getPrefixKey(groupRowEntry.getKey()) + "_";
            String rowId = prefix + "row";
            String divId = prefix + "div";
            String grouptitle = groupRowEntry.getKey();
            boolean def = grouptitle.equalsIgnoreCase(DEFAULT_GROUP);
            if (!def || groupsofrows.size() > 1) {
                boolean collapseGroups = false;
                if (def) {
                    grouptitle = tp.translate("No Grouping");
                }
                areahtml.append("<tr>");
                String click = jsobject + ".toggleGroup('" + elementid + "','" + prefix + "',this)";
                areahtml.append("<th class=\"" + cssTitle + "\" style=\"cursor:pointer;\" onclick=\"").append(click).append("\"><img src=\"WEB-CORE/pagetypes/list/images/").append(collapseGroups ? "plus.gif" : "minus.gif").append("\"></th><th align=\"left\" class=\"list_grouptitle\" style=\"cursor:pointer;\" onclick=\"").append(click).append("\">").append(grouptitle).append("</th>");
                areahtml.append("</tr>");
                areahtml.append("<tr id=\"").append(rowId).append("\">");
                areahtml.append("<td></td><td class=\"" + cssValue + "\">");
                style.append("#").append(rowId).append("{display:").append(collapseGroups ? "none" : "table-row").append(";}\n");
                style.append("#").append(divId).append("{display:").append("block").append(";overflow:hidden;height:100%;width:100%;}\n");
                areahtml.append("<div id=\"").append(divId).append("\">");
                areahtml.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" class=\"maintform_table\">");
                areahtml.append("<tbody>");
            }
            areahtml.append((StringBuffer)groupRowEntry.getValue());
            if (def && groupsofrows.size() <= 1) continue;
            areahtml.append("</tbody>");
            areahtml.append("</table>");
            areahtml.append("</div>");
            areahtml.append("</td>");
            areahtml.append("</tr>");
        }
        areahtml.append("</tbody>");
        areahtml.append("</table>");
        if (!rendered) {
            if (attributes != null) {
                attributes.setAvailability("NoData");
            }
            areahtml.append("<div id=\"__").append(elementid).append("_norows\" style=\"display:").append("block").append(";\" >");
            String noSDIMsg = config.getProperty("nosdiavailablemessage");
            areahtml.append(noSDIMsg.length() > 0 ? SafeHTML.encodeForHTML(noSDIMsg, true) : tp.translate("No Attributes found"));
            areahtml.append("</div>");
        }
        areahtml.append("\n<style id=\"__").append(elementid).append("_style\">\n");
        areahtml.append(style);
        areahtml.append("</style>");
        return instructionOnlyCount;
    }

    private static String getPrefixKey(String orgPrefix) {
        String key = StringUtil.replaceAll(orgPrefix, " ", "_");
        key = StringUtil.replaceAll(orgPrefix, ".", "_7");
        key = HttpUtil.encodeURIComponent(key);
        key = StringUtil.replaceAll(key, "%", "_");
        return key;
    }

    public static enum RenderMode {
        full,
        instance,
        ajax;

    }

    public static enum MaintenanceMode {
        linkdef,
        adhoc,
        template;

    }
}

