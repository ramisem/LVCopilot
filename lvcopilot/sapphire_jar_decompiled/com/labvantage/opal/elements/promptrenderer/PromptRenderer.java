/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.promptrenderer;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.advancedsearch.QueryArg;
import com.labvantage.sapphire.pageelements.controls.FileUploader;
import com.labvantage.sapphire.pageelements.maint.DataView;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.pageelements.maint.RegexConverter;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.servlet.command.ResourceRequest;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.HttpUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PromptRenderer
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 90331 $";
    static HashMap hmArgTypes = new HashMap();
    String sdcId = "";
    String currentUser = "";
    List<String> alKeywordTokens = null;
    Map<String, String> hmRequest = null;
    private TranslationProcessor tp;
    private PropertyList pagedata;

    @Override
    public String getHtml() {
        boolean usepromptrenderer;
        this.alKeywordTokens = new ArrayList<String>();
        this.alKeywordTokens.add("sdcid");
        this.alKeywordTokens.add("keyid1");
        this.alKeywordTokens.add("keyid2");
        this.alKeywordTokens.add("keyid3");
        this.alKeywordTokens.add("param1");
        this.alKeywordTokens.add("param2");
        this.alKeywordTokens.add("param3");
        this.alKeywordTokens.add("currentuser");
        this.alKeywordTokens.add("sysuserid");
        this.sdcId = this.requestContext.getProperty("sdcid");
        this.currentUser = this.requestContext.getProperty("sysuserid");
        this.hmRequest = OpalUtil.getRequestParameters(this.requestContext.getPropertyList());
        this.hmRequest.put("currentuser", this.currentUser);
        if (this.hmRequest.get("keyid1list") != null) {
            this.hmRequest.put("keyid1list", StringUtil.replaceAll(this.hmRequest.get("keyid1list"), ";", "','"));
        }
        this.tp = this.getTranslationProcessor();
        this.pagedata = this.requestContext.getPropertyList("pagedata");
        boolean bl = usepromptrenderer = "Y".equals(this.pagedata.getProperty("usepromptrenderer")) || this.pagedata.getCollection("columns") == null || this.pagedata.getCollection("columns").size() == 0;
        if (usepromptrenderer) {
            return this.getClassicPromptHtml();
        }
        return this.getMaintHtml();
    }

    public static HashMap processCreateSDI(PageContext pageContext, PropertyList pagedata) throws ActionException {
        HashMap<String, String> requestMap = new HashMap<String, String>();
        Enumeration enumeration = pageContext.getRequest().getParameterNames();
        while (enumeration.hasMoreElements()) {
            String key = (String)enumeration.nextElement();
            requestMap.put(key, pageContext.getRequest().getParameter(key));
        }
        ActionBlock ab = new ActionBlock();
        PropertyList addsdiPL = new PropertyList(requestMap);
        String sdcid = pagedata.getProperty("sdcid");
        addsdiPL.setProperty("sdcid", sdcid);
        addsdiPL.setProperty("templatekeyid1", pagedata.getProperty("templateid"));
        addsdiPL.setProperty("templatekeyid2", "1");
        ab.setAction("addsdi", "AddSDI", "1", addsdiPL);
        SDCProcessor sdcProcessor = new SDCProcessor(pageContext);
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        if ("on".equals(requestMap.get("addfollowupnote"))) {
            String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
            String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
            addsdiPL.setProperty("keyid1", addsdiPL.getProperty(keycolid1));
            addsdiPL.setProperty("keyid2", addsdiPL.getProperty(keycolid2));
            addsdiPL.setProperty("keyid3", addsdiPL.getProperty(keycolid3));
            if ("on".equals(requestMap.get("followupnotifyuser"))) {
                addsdiPL.setProperty("followupnotifyuser", "Y");
            }
            if ("on".equals(requestMap.get("requestnotification"))) {
                addsdiPL.setProperty("requestnotification", "Y");
            }
            if ("Y".equals(addsdiPL.getProperty("followup")) && addsdiPL.getProperty("followupuserid").length() == 0) {
                String assigntome = HttpUtil.getConnectionInfo(pageContext).getSysuserId();
                addsdiPL.setProperty("followupuserid", assigntome);
            }
            ab.setAction("addsdinotes", "AddSDINote", "1", addsdiPL);
        }
        try {
            ActionProcessor actionProcessor = new ActionProcessor(pageContext);
            actionProcessor.processActionBlock(ab);
            String keyid1 = ab.getActionProperty("addsdi", "newkeyid1");
            String keyid2 = ab.getActionProperty("addsdi", "newkeyid2");
            String keyid3 = ab.getActionProperty("addsdi", "newkeyid3");
            PropertyList returnproperties = (PropertyList)ab.getReturnProperties();
            returnproperties.setProperty("newkeyid1", keyid1);
            returnproperties.setProperty("newkeyid2", keyid2);
            returnproperties.setProperty("newkeyid3", keyid3);
            if (keycolid1.length() > 0) {
                returnproperties.setProperty(keycolid1, keyid1);
            }
            requestMap.putAll(returnproperties);
        }
        catch (ActionException ae) {
            String errorMessage = ae.getErrorHandler().getLastErrorMessage();
            if (errorMessage.indexOf("ORA-00001") > -1 || ae.getMessage().indexOf("duplicate key") > -1) {
                TranslationProcessor tp = new TranslationProcessor(pageContext);
                errorMessage = tp.translate("An item with the same ID or name already exists.") + "\n" + tp.translate("Please choose a different one.");
            }
            requestMap.put("errormessage", errorMessage);
        }
        return requestMap;
    }

    private String getMaintHtml() {
        PropertyList maint = this.pagedata;
        maint.remove("sortby");
        String templateid = this.pagedata.getProperty("templateid");
        String sdcid = this.pagedata.getProperty("sdcid");
        maint.setProperty("mode", "prompt");
        DataSet dataset = null;
        boolean hasTemplate = false;
        if (sdcid.length() > 0 && templateid.length() > 0) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(sdcid);
            sdiRequest.setKeyid1List(templateid);
            sdiRequest.setKeyid2List("1");
            sdiRequest.setRequestItem("primary");
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            dataset = sdiData.getDataset("primary");
            if (dataset == null || dataset.getRowCount() == 0) {
                return "Error: no data retrieved from " + sdcid + " using template:" + templateid;
            }
            hasTemplate = true;
        } else {
            dataset = new DataSet();
            dataset.addRow();
        }
        DataView dataView = new DataView(this.pageContext, dataset, this.getConnectionid());
        dataView.setElementid("promptrenderer");
        StringBuilder promptidList = new StringBuilder();
        PropertyListCollection columns = maint.getCollection("columns");
        boolean hasDynamicLookup = false;
        if (columns != null && columns.size() > 0) {
            for (int i = 0; i < columns.size(); ++i) {
                String defaultvalue;
                String keygenrule;
                PropertyList dropdowndefinition;
                PropertyList dynamicrendering;
                PropertyList column = columns.getPropertyList(i);
                column.setProperty("sql", this.replaceTokens(column.getProperty("sql")));
                PropertyList lookuplink = column.getPropertyList("lookuplink");
                if (lookuplink != null) {
                    lookuplink.setProperty("href", this.replaceTokens(lookuplink.getProperty("href")));
                    lookuplink.setProperty("querywhere", this.replaceTokens(lookuplink.getProperty("querywhere")));
                    lookuplink.setProperty("restrictivewhere", this.replaceTokens(lookuplink.getProperty("restrictivewhere")));
                }
                if ((dynamicrendering = column.getPropertyList("dynamicrendering")) != null) {
                    dynamicrendering.setProperty("hiddenif", this.replaceTokens(dynamicrendering.getProperty("hiddenif")));
                    dynamicrendering.setProperty("mandatoryif", this.replaceTokens(dynamicrendering.getProperty("mandatoryif")));
                    dynamicrendering.setProperty("readonlyif", this.replaceTokens(dynamicrendering.getProperty("readonlyif")));
                }
                if ((dropdowndefinition = column.getPropertyList("dropdowndefinition")) != null) {
                    dropdowndefinition.setProperty("querywhere", this.replaceTokens(dropdowndefinition.getProperty("querywhere")));
                }
                if (!hasDynamicLookup) {
                    hasDynamicLookup = "lookup".equals(column.getProperty("mode")) && column.getPropertyList("lookuplink") != null && column.getPropertyList("lookuplink").getProperty("enablesuggest").indexOf("Y") == 0;
                }
                String columnid = column.getProperty("columnid");
                SDCProcessor sdcProcessor = this.getSDCProcessor();
                if ("keycolid1".equals(columnid)) {
                    columnid = sdcProcessor.getProperty(sdcid, "keycolid1");
                } else if ("keycolid2".equals(columnid)) {
                    columnid = sdcProcessor.getProperty(sdcid, "keycolid2");
                } else if ("keycolid3".equals(columnid)) {
                    columnid = sdcProcessor.getProperty(sdcid, "keycolid3");
                } else if ("desccol".equals(columnid)) {
                    columnid = sdcProcessor.getProperty(sdcid, "desccol");
                }
                column.setProperty("columnid", columnid);
                String pkflag = this.getSDCProcessor().getSDCColumnProperty(sdcid, columnid, "pkflag");
                promptidList.append(";" + columnid);
                if (!hasTemplate) {
                    dataset.addColumn(columnid, 0);
                    if (dataset.getValue(0, columnid).length() == 0) {
                        dataset.setValue(0, columnid, column.getProperty("defaultvalue"));
                    }
                }
                if (!"Y".equals(pkflag)) {
                    column.setProperty("defaultvalue", dataset.getValue(0, columnid));
                } else if (column.getProperty("defaultvalue").length() == 0 && (keygenrule = this.getSDCProcessor().getProperty(sdcid, "keygenerationrule").trim()).length() > 0 && keygenrule.charAt(0) == 'A' && columnid != null && columnid.equals(sdcProcessor.getProperty(sdcid, "keycolid1")) && !this.element.getProperty("templateflag").equals("Y")) {
                    column.setProperty("defaultvalue", "(Auto)");
                }
                if (column.getProperty("defaultvalue").contains("[currentuser]")) {
                    defaultvalue = column.getProperty("defaultvalue", "");
                    defaultvalue = StringUtil.replaceAll(defaultvalue, "[currentuser]", this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId());
                    column.setProperty("defaultvalue", defaultvalue);
                    continue;
                }
                if (!column.getProperty("defaultvalue").contains("[currentdate]")) continue;
                defaultvalue = column.getProperty("defaultvalue", "");
                SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
                M18NUtil m18n = new M18NUtil(sapphireConnection);
                defaultvalue = StringUtil.replaceAll(defaultvalue, "[currentdate]", m18n.formatDateOnly(m18n.getNowCalendar()));
                column.setProperty("defaultvalue", defaultvalue);
            }
        }
        dataView.setPrefix("");
        dataView.setElementProperties(maint);
        StringBuilder html = new StringBuilder();
        if ("Y".equals(maint.getProperty("showtemplateselector"))) {
            String editorstyleid = this.pagedata.getProperty("templateeditorstyleid");
            String templateHTML = "";
            if (editorstyleid.length() == 0) {
                SDITagUtil sdiTagUtil = SDITagUtil.getInstance(this.pageContext);
                PropertyList attributes = new PropertyList();
                attributes.setProperty("name", "templateselector");
                attributes.setProperty("columnid", "templateselector");
                attributes.setProperty("mode", "dropdownlist");
                attributes.setProperty("sdcid", sdcid);
                attributes.setProperty("querywhere", "templateflag='Y'");
                attributes.setProperty("onchange", "ontemplatechange( this )");
                attributes.setProperty("value", templateid);
                templateHTML = sdiTagUtil.getInputHtml(attributes, new SDITagInfo(new HashMap()));
            } else {
                try {
                    EditorStyleField editorStyleField = new EditorStyleField(this.pageContext);
                    editorStyleField.setEditorStyleId(editorstyleid);
                    editorStyleField.setFieldName("templateid");
                    PropertyList column = new PropertyList();
                    column.setProperty("columnid", "templateid");
                    PropertyListCollection events = new PropertyListCollection();
                    PropertyList oninput = new PropertyList();
                    oninput.setProperty("event", "oninput");
                    oninput.setProperty("js", "this.value=this.value");
                    events.add(oninput);
                    column.setProperty("events", events);
                    editorStyleField.setReadonly(false);
                    editorStyleField.setChangeEvent("ontemplatechange( this );");
                    editorStyleField.setColumn(column);
                    if (templateid != null) {
                        editorStyleField.setFieldValue(templateid);
                    }
                    templateHTML = editorStyleField.getHtml();
                }
                catch (SapphireException se) {
                    html.append("<span>Error rendering editorstyle:" + editorstyleid + "." + se.getMessage() + "</span>");
                }
            }
            html.append("<table><tr><td>" + this.tp.translate("Use Template") + "</td><td>" + templateHTML + "</td></tr></table>");
            html.append("<br/>");
        }
        html.append("<table><tr><td id=\"tab_maintform__body\" class=\"tab_modernstandard _body\"><div id=\"tab_maintform__expanded\" style=\"overflow: visible;display: block\">");
        try {
            PropertyListCollection plcIncludes = this.pagedata.getCollection("includes");
            int noOfIncludes = 0;
            if (plcIncludes != null) {
                noOfIncludes = plcIncludes.size();
            }
            for (int i = 0; i < noOfIncludes; ++i) {
                PropertyList plInclude = plcIncludes.getPropertyList(i);
                try {
                    html.append(ResourceRequest.getResourceTag(this.pageContext, new ConnectionProcessor(this.getConnectionid()).getConnectionInfo(this.getConnectionid()).getDatabaseId(), plInclude, "text/javascript")).append("\n");
                    continue;
                }
                catch (SapphireException e) {
                    html.append("<!--").append(e.getMessage()).append("-->");
                }
            }
            html.append("\n<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/lookup.js\"></script>\n<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/maint.js\"></script>\n<script>var promptidList='" + SafeHTML.encodeForJavaScript(promptidList.substring(1)) + "';</script>\n");
            html.append(dataView.getHtml());
            int windowHeight = columns.size() * 30 + (hasDynamicLookup ? 450 : 300);
            int windowWidth = hasDynamicLookup ? 650 : 500;
            try {
                if (this.pagedata.getProperty("width").length() > 0) {
                    windowWidth = Integer.parseInt(this.pagedata.getProperty("width"));
                }
                if (this.pagedata.getProperty("height").length() > 0) {
                    windowHeight = Integer.parseInt(this.pagedata.getProperty("height"));
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            html.append("\n<input type=hidden id=numberofprompts name=numberofprompts value=\"" + columns.size() + "\">\n<input type=hidden id=windowheight name=windowheight value=\"" + windowHeight + "\">\n<input type=hidden id=windowwidth name=windowwidth value=\"" + windowWidth + "\">");
            html.append("</div></td></tr></table>");
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
        return html.toString();
    }

    private String replaceTokens(String inputString) {
        if (inputString != null && inputString.indexOf("[") >= 0 && inputString.indexOf("]") > 0) {
            this.alKeywordTokens.addAll(OpalUtil.getKeywordTokens(inputString));
            return OpalUtil.searchAndReplaceTokens(inputString, this.alKeywordTokens, this.hmRequest, false);
        }
        return inputString;
    }

    private String getClassicPromptHtml() {
        PropertyListCollection plcIncludes = this.element.getCollection("includes");
        int noOfIncludes = 0;
        StringBuffer includeHtml = new StringBuffer();
        if (plcIncludes != null) {
            noOfIncludes = plcIncludes.size();
        }
        for (int i = 0; i < noOfIncludes; ++i) {
            PropertyList plInclude = plcIncludes.getPropertyList(i);
            String url = plInclude.getProperty("url");
            if (url.length() <= 0) continue;
            includeHtml.append("<script language=\"JavaScript\" src=\"").append(url).append("\"></script>\n");
        }
        StringBuilder html = new StringBuilder();
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/lookup.js\"></script>");
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/search.js\"></script>");
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/maint.js\"></script>");
        html.append(includeHtml.toString());
        html.append("<style>");
        html.append("input{");
        html.append("border:1px solid gray;");
        html.append("font:8pt verdana;");
        html.append("}");
        html.append(".search_queryargtitle{");
        html.append("font-weight:bold;");
        html.append("}");
        html.append(".validationfail {");
        html.append("border:1px solid gray;");
        html.append("font:8pt verdana;");
        html.append("background-color: orange;");
        html.append("width:130px;");
        html.append("}");
        html.append("</style>");
        html.append("<script>usePromptRenderer=true;");
        html.append("</script>");
        String errMsg = "";
        ArrayList alArgs = new ArrayList();
        int windowHeight = 0;
        int windowWidth = 0;
        try {
            PropertyListCollection plcPrompts = this.element.getCollection("prompts");
            PropertyList plFileUpload = this.element.getPropertyList("fileupload");
            boolean hasParams = false;
            boolean gotBadPrompt = false;
            HashMap<String, Object> hmQueryProps = new HashMap<String, Object>();
            if (plFileUpload.getProperty("show") != null && plFileUpload.getProperty("show").equalsIgnoreCase("Y") || plcPrompts.size() > 0) {
                hasParams = true;
            }
            TranslationProcessor tp = this.getTranslationProcessor();
            for (int i = 0; i < plcPrompts.size(); ++i) {
                HashMap<String, String> hmArgProps = new HashMap<String, String>();
                PropertyList plPrompt = plcPrompts.getPropertyList(i);
                PropertyList plArgDetails = plPrompt.getPropertyList("typedetails");
                String displayType = plPrompt.getProperty("type");
                String argIdentifier = plPrompt.getProperty("id");
                if (argIdentifier == null || argIdentifier.equals("")) continue;
                String argType = (String)hmArgTypes.get(displayType);
                hmArgProps.put("argid", argIdentifier);
                hmArgProps.put("argdesc", tp != null ? tp.translate(plPrompt.getProperty("title")) : plPrompt.getProperty("title"));
                hmArgProps.put("argtype", argType);
                hmArgProps.put("defaultvalue", plArgDetails.getProperty("defaultvalue"));
                String webLookupUrl = plArgDetails.getProperty("weblookupurl");
                if (webLookupUrl.length() > 0) {
                    this.alKeywordTokens.addAll(OpalUtil.getKeywordTokens(webLookupUrl));
                    webLookupUrl = OpalUtil.searchAndReplaceTokens(webLookupUrl, this.alKeywordTokens, this.hmRequest, false);
                }
                hmArgProps.put("weblookupurl", webLookupUrl);
                if (argType.equals("ddlb")) {
                    if (plArgDetails.getProperty("listofvalues").length() > 0) {
                        hmArgProps.put("argdata", plArgDetails.getProperty("listofvalues"));
                    } else {
                        gotBadPrompt = true;
                        errMsg = errMsg + this.getErrorMessage("list of values", argIdentifier, displayType);
                    }
                } else if (argType.equals("dddef") || argType.equals("ddsdc")) {
                    String ddsdcid = "";
                    String ddqueryfrom = "";
                    String ddquerywhere = "";
                    String ddqueryorderby = "";
                    String ddvaluecolumn = "";
                    String dddisplaycolumn = "";
                    if (argType.equals("dddef")) {
                        PropertyList dddef = plArgDetails.getPropertyListNotNull("dropdowndefinition");
                        ddsdcid = dddef.getProperty("sdcid").trim();
                        ddqueryfrom = dddef.getProperty("queryfrom").trim();
                        ddquerywhere = dddef.getProperty("querywhere").trim();
                        ddqueryorderby = dddef.getProperty("queryorderby").trim();
                        ddvaluecolumn = dddef.getProperty("valuecolumn").trim();
                        dddisplaycolumn = dddef.getProperty("displaycolumn").trim();
                    } else {
                        ddsdcid = plArgDetails.getProperty("sdcid");
                    }
                    if (ddsdcid.length() == 0) {
                        gotBadPrompt = true;
                        errMsg = errMsg + this.getErrorMessage("SDC", argIdentifier, displayType);
                    } else {
                        DataSet primary;
                        SDCProcessor sdcProcessor = this.getSDCProcessor();
                        String keycolid1 = (String)sdcProcessor.getSDCProperties(ddsdcid).get("keycolid1");
                        String descCol = (String)sdcProcessor.getSDCProperties(ddsdcid).get("desccol");
                        SDIRequest sdirequest = new SDIRequest();
                        sdirequest.setSDCid(ddsdcid);
                        String orderby = ddqueryorderby;
                        if (StringUtil.getLen(ddqueryfrom) > 0L) {
                            sdirequest.setQueryFrom(ddqueryfrom);
                        } else {
                            String tableid = (String)sdcProcessor.getSDCProperties(ddsdcid).get("tableid");
                            sdirequest.setQueryFrom(tableid);
                            orderby = keycolid1;
                        }
                        if (ddquerywhere.length() > 0) {
                            this.alKeywordTokens.addAll(OpalUtil.getKeywordTokens(ddquerywhere));
                            ddquerywhere = OpalUtil.searchAndReplaceTokens(ddquerywhere, this.alKeywordTokens, this.hmRequest, false, true, this.getConnectionProcessor().isOra());
                        }
                        sdirequest.setQueryWhere(ddquerywhere);
                        if (StringUtil.getLen(orderby) > 0L) {
                            sdirequest.setQueryOrderBy(orderby);
                        }
                        if (StringUtil.getLen(ddvaluecolumn) > 0L) {
                            sdirequest.setRequestItem("primary[" + ddvaluecolumn + (StringUtil.getLen(dddisplaycolumn) > 0L ? "," + dddisplaycolumn : "") + "]");
                        } else {
                            sdirequest.setRequestItem("primary");
                            ddvaluecolumn = keycolid1;
                            dddisplaycolumn = descCol;
                        }
                        sdirequest.setShowTemplates(true);
                        SDIData sdidata = this.getSDIProcessor().getSDIData(sdirequest);
                        if (sdidata != null && (primary = sdidata.getDataset("primary")) != null) {
                            ArrayList<String> list = new ArrayList<String>();
                            for (int row = 0; row < primary.size(); ++row) {
                                String value = primary.getValue(row, ddvaluecolumn, "");
                                String displayvalue = primary.getValue(row, dddisplaycolumn, value);
                                if (StringUtil.getLen(dddisplaycolumn) <= 0L) continue;
                                list.add(value + "|" + displayvalue);
                            }
                            hmArgProps.put("argdata", OpalUtil.toDelimitedString(list, ";"));
                            hmArgProps.put("argtype", "dddef");
                        }
                    }
                } else if (argType.equals("ddsql") || argType.equals("sql") || argType.equals("sqllookup")) {
                    if (plArgDetails.getProperty("sql").length() > 0) {
                        String sql = plArgDetails.getProperty("sql");
                        if (sql.length() > 0) {
                            this.alKeywordTokens.addAll(OpalUtil.getKeywordTokens(sql));
                            sql = OpalUtil.searchAndReplaceTokens(sql, this.alKeywordTokens, this.hmRequest, false);
                        }
                        hmArgProps.put("argdata", sql);
                    } else {
                        gotBadPrompt = true;
                        errMsg = errMsg + this.getErrorMessage("SQL statement", argIdentifier, displayType);
                    }
                } else if (argType.equals("reftype")) {
                    if (plArgDetails.getProperty("reftypeid").length() > 0) {
                        hmArgProps.put("reftypeid", plArgDetails.getProperty("reftypeid"));
                    } else {
                        gotBadPrompt = true;
                        errMsg = errMsg + this.getErrorMessage("Reftype", argIdentifier, displayType);
                    }
                } else if (argType.equals("sdc")) {
                    if (plArgDetails.getProperty("sdcid").length() > 0) {
                        hmArgProps.put("sdcid", plArgDetails.getProperty("sdcid"));
                    } else {
                        gotBadPrompt = true;
                        errMsg = errMsg + this.getErrorMessage("SDC", argIdentifier, displayType);
                    }
                }
                alArgs.add(hmArgProps);
            }
            if (hasParams) {
                if (!gotBadPrompt) {
                    hmQueryProps.put("cascadedargflag", "N");
                    hmQueryProps.put("arglist", alArgs);
                    QueryArg queryArgs = new QueryArg(this.pageContext, false, null);
                    String cookieKey = "Prompt_" + this.sdcId;
                    html.append(queryArgs.getArgDivHtml("prompt_[1p2r3o4m5p6t7]", hmQueryProps, this.sdcId, cookieKey, this.currentUser, 2));
                    html.append(this.getValidations(plcPrompts));
                    html.append(this.getOnChangeJS(plcPrompts));
                    if (plFileUpload.getProperty("show").equalsIgnoreCase("Y")) {
                        html.append("<tr><td colspan=2>&nbsp;</td></tr>");
                        html.append("<tr><td colspan=2>");
                        html.append("<fieldset id=uploadfieldset><legend><strong>").append(tp.translate("Upload File")).append("</strong></legend>");
                        FileUploader fileUploader = new FileUploader(this.pageContext);
                        fileUploader.setElementid("uploadedfiles");
                        fileUploader.setUploadMultiple(false);
                        fileUploader.setLocationPolicy(plFileUpload.getProperty("filelocationpolicynode", "Upload Custom"), plFileUpload.getProperty("filelocationpolicyitem", ""));
                        fileUploader.setCreateTempFile(false);
                        fileUploader.setShowAdvancedThumbnails(true);
                        if (plFileUpload.getProperty("createuserfolder").equalsIgnoreCase("Y")) {
                            fileUploader.setSubDirectory(this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                        }
                        if (plFileUpload.getProperty("renamefileafterupload").equalsIgnoreCase("Y")) {
                            fileUploader.setRenameExpression("[currentuser]_[timestamp]_[filename].[extension]");
                        }
                        fileUploader.setErrorCallback("fileuploadErrorCallback");
                        html.append(fileUploader.getHtml());
                        html.append("<script>");
                        html.append("filelocationPolicy = '").append(plFileUpload.getProperty("filelocationpolicynode", "Upload Custom")).append(";").append(plFileUpload.getProperty("filelocationpolicyitem", "")).append("';");
                        html.append("</script>");
                        html.append("</td></tr>");
                    }
                }
            } else {
                errMsg = tp.translate("No input prompts defined!");
            }
            windowHeight = (alArgs.size() + (plFileUpload.getProperty("show").equalsIgnoreCase("Y") ? 1 : 0)) * 30 + 110;
            windowWidth = plFileUpload.getProperty("show").equalsIgnoreCase("Y") ? 320 : 300;
            try {
                if (this.pagedata.getProperty("width").length() > 0) {
                    windowWidth = Integer.parseInt(this.pagedata.getProperty("width"));
                }
                if (this.pagedata.getProperty("height").length() > 0) {
                    windowHeight = Integer.parseInt(this.pagedata.getProperty("height"));
                }
            }
            catch (Exception exception) {}
        }
        catch (Exception ex) {
            errMsg = ex.toString();
            this.logger.error("PromptRenderer Element", ex);
        }
        if (errMsg.length() > 0) {
            html.append("<tr><td><div class=error > ").append(errMsg).append(" </div></td></tr>");
        }
        html.append("<input type=hidden id=numberofprompts name=numberofprompts value=\"").append(alArgs.size()).append("\">");
        html.append("<input type=hidden id=windowheight name=windowheight value=\"").append(windowHeight).append("\">");
        html.append("<input type=hidden id=windowwidth name=windowwidth value=\"").append(windowWidth).append("\">");
        return html.toString();
    }

    private String getValidations(PropertyListCollection plcPrompts) {
        StringBuilder html = new StringBuilder("");
        html.append("<script>");
        for (int i = 0; i < plcPrompts.size(); ++i) {
            PropertyList field = plcPrompts.getPropertyList(i);
            String name = field.getProperty("id");
            String valitypes = field.getProperty("validations");
            if (valitypes == null || valitypes.length() <= 0) continue;
            html.append("  validations[ '").append(name).append("'] = '").append(valitypes).append("';");
        }
        html.append("var sapdateformat = ").append(RegexConverter.getSapDateFormat(this.pageContext)).append(";");
        FormatUtil formatUtil = FormatUtil.getInstance(I18nUtil.getSessionLocale(this.pageContext));
        html.append("var decimalSeparator = \"").append(formatUtil.getDecimalSeparator()).append("\";");
        html.append("var groupingSeparator = \"").append(formatUtil.getGroupingSeparator()).append("\";");
        String[] textids = new String[]{"validationfail", "notnumber", "notinteger", "notdecimal", "lengthnotinrange", "mandatorynotfilled", "valuenotinrange", "notdate"};
        for (int i = 0; i < textids.length; ++i) {
            String message = ElementUtil.getText(this.element, textids[i], "");
            if (message.trim().length() <= 0) continue;
            html.append("  texts['").append(textids[i]).append("'] = '").append(message).append("';");
        }
        html.append("</script>");
        return html.toString();
    }

    private String getErrorMessage(String msg, String argIdentifier, String displayType) {
        String message = "";
        HashMap<String, String> valueMap = new HashMap<String, String>();
        valueMap.put("msg", msg);
        message = "- " + this.tp.translate("A \"[msg]\" must be specified for", valueMap) + "<br> " + this.tp.translate("prompt:") + " " + argIdentifier + "<br> " + this.tp.translate("type:") + " " + displayType + "<br>";
        return message;
    }

    private String getOnChangeJS(PropertyListCollection plcPrompts) {
        StringBuilder html = new StringBuilder("");
        html.append("<script>");
        for (int i = 0; i < plcPrompts.size(); ++i) {
            PropertyList field = plcPrompts.getPropertyList(i);
            String id = "prompt_arg" + (i + 1);
            String onChangeJS = field.getProperty("onchange");
            if (onChangeJS == null || onChangeJS.length() <= 0) continue;
            html.append("var element = document.getElementById('").append(id).append("');");
            html.append(onChangeJS.indexOf("(") > -1 ? "element.onchange = function(){" + onChangeJS + ";};" : "element.onchange = " + onChangeJS + ";");
        }
        html.append("</script>");
        return html.toString();
    }

    static {
        hmArgTypes.put("", "string");
        hmArgTypes.put("String", "string");
        hmArgTypes.put("Number", "number");
        hmArgTypes.put("Absolute/Relative Date", "absreldt");
        hmArgTypes.put("Reftype", "reftype");
        hmArgTypes.put("DropDown from a SDC", "ddsdc");
        hmArgTypes.put("DropDown from a List of Values", "ddlb");
        hmArgTypes.put("DropDown from SQL", "ddsql");
        hmArgTypes.put("Dropdown Definition", "dddef");
        hmArgTypes.put("SQL", "sql");
        hmArgTypes.put("Lookup from SDC", "sdc");
        hmArgTypes.put("Lookup from SQL", "sqllookup");
    }
}

