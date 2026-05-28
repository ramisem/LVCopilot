/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.FileUploader;
import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.list.MapHelper;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.util.format.RelativeDateFormat;
import com.labvantage.sapphire.util.logger.LogUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDITagUtil
extends BaseCustom {
    private PageContext pageContext;
    private Browser browser;
    private boolean html5;
    private TranslationProcessor tp;
    public static final String PAGE_DIRECTIVES_PREFIX = "PGD_";

    public static SDITagUtil getInstance(PageContext pageContext) {
        SDITagUtil sdiTagUtil = (SDITagUtil)pageContext.getAttribute("com.labvantage.sapphire.SDITagUtil");
        if (sdiTagUtil == null) {
            RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
            sdiTagUtil = new SDITagUtil(requestContext.getConnectionId());
            PropertyList rc = requestContext.getPropertyList();
            if (rc.containsKey("language")) {
                sdiTagUtil.setLanguage(rc.getProperty("language"));
            }
            sdiTagUtil.pageContext = pageContext;
            sdiTagUtil.browser = new Browser(pageContext);
            sdiTagUtil.html5 = requestContext.getProperty("html5").equalsIgnoreCase("Y");
            pageContext.setAttribute("com.labvantage.sapphire.SDITagUtil", (Object)sdiTagUtil);
        }
        return sdiTagUtil;
    }

    public SDITagUtil(String connectionid) {
        this.setConnectionId(connectionid);
        this.tp = this.getTranslationProcessor();
    }

    public static String getDisplayValue(String value, String displaylist) {
        String displayValue = value;
        String wildcardValue = "";
        boolean displayValueFound = false;
        boolean wildCardFound = false;
        if (displaylist != null && displaylist.length() > 0) {
            String[] displays = StringUtil.split(StringUtil.replaceAll(displaylist, "&lt;", "<"), ";");
            if (displays != null && displays.length > 0) {
                for (int i = 0; i < displays.length; ++i) {
                    String display = displays[i].trim();
                    int pos = display.indexOf("=");
                    if (pos <= -1) continue;
                    String disval = display.substring(0, pos);
                    if (disval.startsWith(">") || disval.startsWith("<")) {
                        String compare = disval.substring(1);
                        if (compare.length() <= 0) continue;
                        try {
                            int comparenum = Integer.parseInt(compare);
                            int valuenum = Integer.parseInt(value);
                            if (disval.startsWith(">") && valuenum > comparenum) {
                                displayValue = display.substring(pos + 1).trim();
                                break;
                            }
                            if (!disval.startsWith("<") || valuenum >= comparenum) continue;
                            displayValue = display.substring(pos + 1).trim();
                        }
                        catch (NumberFormatException e) {
                            if (!disval.equals(value)) continue;
                            displayValue = display.substring(pos + 1).trim();
                        }
                        break;
                    }
                    if (disval.equals(value)) {
                        displayValue = display.substring(pos + 1).trim();
                        break;
                    }
                    if (!wildCardFound && disval.equals("*")) {
                        wildcardValue = display.substring(pos + 1).trim();
                        wildCardFound = true;
                        continue;
                    }
                    if (disval.equalsIgnoreCase("!null") && value.length() > 0) {
                        displayValue = display.substring(pos + 1).trim();
                        break;
                    }
                    if (!disval.equalsIgnoreCase("null") || value.length() != 0) continue;
                    displayValue = display.substring(pos + 1).trim();
                    break;
                }
            }
            if (!displayValueFound && wildCardFound) {
                displayValue = wildcardValue;
            }
            if (displayValue.toLowerCase().contains("[value]")) {
                displayValue = StringUtil.replaceAll(displayValue, "[value]", value, false);
            }
        }
        return displayValue;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setIdentifierAttributes(PropertyList attributes, SDITagInfo sdiInfo) {
        String data = attributes.getProperty("data", "primary");
        String mode = attributes.getProperty("mode");
        QueryData queryData = sdiInfo.getQueryData(mode.equals("data") ? "dataitem" : data);
        if (queryData != null) {
            String nv = attributes.getProperty("nullvalue");
            if (nv == null) {
                nv = queryData.getNullValue();
            }
            if (mode.equals("data")) {
                attributes.setProperty("name", "di" + queryData.getRowId(queryData.getCurrentRow()) + "_enteredtext");
                attributes.setProperty("rowindex", queryData.getRowId(queryData.getCurrentRow()));
                attributes.setProperty("diindex", queryData.getRowId(queryData.getCurrentRow()));
                attributes.setProperty("value", attributes.getProperty("value").length() > 0 ? attributes.getProperty("value") : queryData.getValue(queryData.getCurrentRow(), "displayvalue", nv));
            } else {
                String display;
                int row = queryData.getCurrentRow();
                String value = attributes.getProperty("value");
                attributes.setProperty("__valuemasked", queryData.isMasked(queryData.getCurrentRow(), attributes.getProperty("columnid")) ? "Y" : "N");
                if (attributes.getProperty("columnid").length() == 0) {
                    value = value.length() > 0 ? value : nv;
                } else {
                    int rowAttribute = -1;
                    try {
                        rowAttribute = Integer.parseInt(attributes.getProperty("row"));
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                    if (rowAttribute > -1) {
                        value = value.length() > 0 ? value : queryData.getValue(rowAttribute, attributes.getProperty("columnid"), nv);
                        row = rowAttribute;
                    } else if (attributes.getProperty("find").length() > 0) {
                        value = value.length() > 0 ? value : queryData.findColValue(attributes.getProperty("find"), attributes.getProperty("columnid"), nv);
                        row = queryData.findRow(attributes.getProperty("find"));
                    } else {
                        String string = value = value.length() > 0 ? value : queryData.getValue(attributes.getProperty("columnid"), nv);
                    }
                }
                if (attributes.getProperty("mode").equals("datelookup")) {
                    attributes.setProperty("dateformat", ((SimpleDateFormat)queryData.getQuerydata().getDateDisplayFormat(attributes.getProperty("columnid"))).toPattern());
                }
                if (attributes.getProperty("displayvalue").equals("*=[dateonly]")) {
                    attributes.setProperty("displayvalue", "");
                    if (value.length() > 0 && queryData.getQuerydata().getColumnType(attributes.getProperty("columnid")) == 2) {
                        DataSet ds = queryData.getQuerydata();
                        DateFormat df = ds.getDateDisplayFormat(attributes.getProperty("columnid"));
                        ds.setDateDisplayFormat(attributes.getProperty("columnid"), new M18NUtil(sdiInfo.getPageContext()).getDefaultDateOnlyFormat(false));
                        try {
                            value = ds.getValue(queryData.getCurrentRow(), attributes.getProperty("columnid"), nv);
                        }
                        finally {
                            ds.setDateDisplayFormat(attributes.getProperty("columnid"), df);
                        }
                    }
                }
                attributes.setProperty("value", (display = attributes.getProperty("display")).length() > 0 ? SDITagUtil.getDisplayValue(value, display) : value);
                if (row > -1 || row == -9999) {
                    attributes.setProperty("rowindex", queryData.getRowId(row));
                    StringBuffer name = new StringBuffer(attributes.getProperty("_prefix") + SDIData.getDatasetCode(data) + queryData.getRowId(row) + "_");
                    attributes.setProperty("fieldprefix", attributes.getProperty("_prefix") + SDIData.getDatasetCode(data));
                    name.append(attributes.getProperty("columnid"));
                    attributes.setProperty("name", name.toString());
                }
            }
        } else {
            attributes.setProperty("value", "TAG ERROR: Failed to get querydata for data=" + data);
        }
    }

    private static void getRichtextEditorSize(HTMLEditorControl rt, PropertyList attributes) {
        if (attributes.containsKey("size") && attributes.getProperty("size", "").length() > 0) {
            String[] size = attributes.getProperty("size", "").split(";");
            try {
                double w = Double.parseDouble(size[0]);
                if (w < 190.0) {
                    rt.setInline(true);
                }
            }
            catch (Exception w) {
                // empty catch block
            }
            rt.setWidth(size[0]);
            if (size.length > 1) {
                rt.setHeight(size[1]);
                try {
                    double h = Double.parseDouble(size[1]);
                    if (h < 60.0) {
                        rt.setShowToolbar(false);
                    }
                }
                catch (Exception exception) {}
            } else {
                rt.setHeight("300");
            }
        } else {
            rt.setWidth("auto");
            rt.setHeight("300");
        }
    }

    public String getInputHtml(PropertyList attributes, SDITagInfo sdiInfo) {
        String name;
        String displayStyle;
        if (attributes.getProperty("reftypeid").length() > 0) {
            this.tp.setTextType(attributes.getProperty("reftypeid"));
        } else if (sdiInfo != null && sdiInfo.getSdcid() != null) {
            if ("RefType".equals(sdiInfo.getSdcid()) && "refvalue".equals(attributes.getProperty("data"))) {
                this.tp.setTextType(sdiInfo.getSDIRequest().getKeyid1List());
            } else {
                this.tp.setTextType(sdiInfo.getSdcid());
            }
        }
        StringBuffer html = new StringBuffer();
        String data = attributes.getProperty("data", "primary");
        String mode = attributes.getProperty("mode");
        String valueMasked = attributes.getProperty("__valuemasked");
        String escapedValue = DOMUtil.convertChars(attributes.getProperty("value"));
        String string = displayStyle = attributes.getProperty("released").equals("true") ? "none" : "inline";
        if ("Y".equalsIgnoreCase(valueMasked) && !"hidden".equalsIgnoreCase(mode)) {
            String n = SDITagUtil.getNameAndIdAttributes(attributes);
            String ia = SDITagUtil.getInputAttributes("text", escapedValue, true);
            String fk = SDITagUtil.getFKInputAttributes(attributes);
            String e = SDITagUtil.getExtraAttributes(attributes);
            html.append("<input ").append(n.length() > 0 ? n + " " : "").append(ia.length() > 0 ? ia + " " : "").append(fk.length() > 0 ? fk + " " : "").append(e.length() > 0 ? e : "").append("disabled").append(">");
        } else if (mode.equalsIgnoreCase("input")) {
            html.append("<input ").append(SDITagUtil.getInputAttributes("text", escapedValue, attributes.getProperty("readonly").equals("true"))).append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5)).append(attributes.getProperty("disabled").equalsIgnoreCase("true") ? " disabled" : "").append(">");
            if (attributes.getProperty("translatevalue").indexOf("E") == 1) {
                html.append(SDITagUtil.getTranslateIcon("", this.tp.getTextType(), attributes.getProperty("name"), this.pageContext));
            }
        } else if (mode.equalsIgnoreCase("fileuploader")) {
            FileUploader fileUploader = new FileUploader(this.pageContext);
            fileUploader.setElementid(attributes.getProperty("name") + "_fu");
            fileUploader.setUploadMultiple(false);
            fileUploader.setCreateTempFile(false);
            fileUploader.setShowAdvancedThumbnails(true);
            fileUploader.setUploadCallback("sapphire.fileUpload.fieldCallback");
            fileUploader.setErrorCallback("sapphire.fileUpload.fieldErrorCallback");
            if (attributes.getPropertyList("fileupload") != null) {
                fileUploader.setLocationPolicy(attributes.getPropertyList("fileupload").getProperty("filelocationpolicynode", "Upload Custom"), attributes.getPropertyList("fileupload").getProperty("filelocationpolicyitem", ""));
                if (attributes.getPropertyList("fileupload").getProperty("createuserfolder").equalsIgnoreCase("Y")) {
                    fileUploader.setSubDirectory(this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                }
                if (attributes.getPropertyList("fileupload").getProperty("renamefileafterupload").equalsIgnoreCase("Y")) {
                    fileUploader.setRenameExpression("[currentuser]_[timestamp]_[filename].[extension]");
                }
            } else {
                fileUploader.setLocationPolicy("Upload Custom", "");
            }
            html.append(fileUploader.getHtml());
            html.append("<input ").append(SDITagUtil.getInputAttributes("hidden", escapedValue, false)).append("name=\"").append(attributes.getProperty("name")).append("\" id=\"").append(attributes.getProperty("name")).append("\" ").append(SDITagUtil.getExtraAttributes(attributes)).append(">");
        } else if (mode.startsWith("richtexthtml") || mode.startsWith("formattedtext") || mode.startsWith("html")) {
            String name2 = attributes.getProperty("name");
            String[] modeparts = StringUtil.split(mode, "|", true);
            HTMLEditorControl editor = new HTMLEditorControl(this.logger);
            if (attributes.getProperty("phrasetype").length() > 0) {
                editor.setPhraseType(attributes.getProperty("phrasetype"));
            }
            if (attributes.getProperty("phraselookup").length() > 0) {
                editor.setPhraseLookup(attributes.getProperty("phraselookup"));
            }
            editor.setId(name2);
            boolean isDevMode = false;
            try {
                isDevMode = "Y".equals(new ConfigurationProcessor(this.pageContext).getSysConfigProperty("devmode"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                editor.setRtl(new ConnectionProcessor(this.pageContext).getSapphireConnection().isRtl());
            }
            catch (Exception exception) {
                // empty catch block
            }
            HTMLEditorControl.EditorType editorType = HTMLEditorControl.EditorType.PRINTABLE;
            if (modeparts.length > 1) {
                try {
                    editorType = HTMLEditorControl.EditorType.valueOf(modeparts[1].toUpperCase());
                }
                catch (Exception e) {
                    editorType = HTMLEditorControl.EditorType.PRINTABLE;
                }
            } else if (mode.startsWith("richtexthtml")) {
                editorType = HTMLEditorControl.EditorType.ADVANCED;
            }
            editor.setEditorType(editorType);
            if (modeparts[0].endsWith("_dm") || modeparts[0].endsWith("_inline")) {
                editor.setInline(true);
            }
            SDITagUtil.getRichtextEditorSize(editor, attributes);
            if (modeparts[0].startsWith("html")) {
                editor.setViewOnly(true);
                editor.setShowToolbar(false);
            }
            editor.setCanUpload(false);
            String val = attributes.getProperty("value");
            if (val.startsWith("%3C") && val.endsWith("%3E")) {
                val = HttpUtil.decodeURIComponent(val);
            }
            editor.setContent(val);
            editor.setAutoUpdateField(true);
            SDITagUtil.getRichtextEditorEvents(editor, attributes, true);
            html.append(editor.getIncludesHTML((HttpServletRequest)this.pageContext.getRequest()));
            html.append(editor.getHtml());
            html.append("<script type=\"text/javascript\">");
            html.append(editor.getScript());
            if (attributes.containsKey("rowindex") && attributes.get("rowindex").toString().equalsIgnoreCase("[__row]")) {
                html.append("window['__" + editor.getId() + "_init'] = function(){").append(editor.getInitScript("")).append("};");
                html.append("window['__" + editor.getId() + "_init']();").append("");
            } else {
                html.append("__" + editor.getId() + "_init = function(){").append(editor.getInitScript("")).append("};");
                html.append("sapphire.events.registerLoadListener(__" + editor.getId() + "_init").append(");");
            }
            html.append("</script>");
        } else if (mode.equalsIgnoreCase("text") || mode.equalsIgnoreCase("displayicon")) {
            String textid;
            String value;
            String displayvalue;
            DataSet ds;
            attributes.setProperty("style", ";border:0;" + attributes.getProperty("style"));
            if (attributes.getProperty("sql").length() > 0 && (ds = this.getSqlDataSet(attributes, sdiInfo)) != null && ds.getColumnCount() == 2) {
                String colid1 = ds.getColumnId(0);
                String colid2 = ds.getColumnId(1);
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    if (!attributes.getProperty("value").equals(ds.getValue(i, colid1)) || ds.getValue(i, colid2) == null || ds.getValue(i, colid2).length() <= 0) continue;
                    attributes.setProperty("displayvalue", ds.getValue(i, colid1) + "=" + ds.getValue(i, colid2));
                    break;
                }
            }
            if ((displayvalue = attributes.getProperty("displayvalue")).length() == 0 && attributes.getProperty("reftypeid").length() > 0) {
                displayvalue = this.getRefTypeDisplayValue(attributes, sdiInfo);
            }
            if (displayvalue.length() > 0) {
                value = SDITagUtil.getDisplayValue(attributes.getProperty("value"), displayvalue);
                if ((value = StringUtil.replaceAll(value, "#semicolon#", ";")).indexOf("[") >= 0 && value.indexOf("]") > 0) {
                    value = ElementUtil.evaluateExpression(attributes.getProperty("datasetname"), -1, attributes.getProperty("columnid"), value, sdiInfo);
                }
                textid = value;
                if (attributes.getProperty("translatevalue").length() > 0 && "Y".equals(attributes.getProperty("translatevalue").substring(0, 1))) {
                    value = this.tp.translate(textid);
                }
                attributes.setProperty("onchange", attributes.getProperty("onchange") + ";document.getElementById( '" + attributes.getProperty("name") + "__dv' ).innerHTML=this.value");
                boolean containsHtml = Pattern.compile("<[a-zA-Z0-9][\\s\\S]*>").matcher(value).find();
                html.append("<span id=\"" + attributes.getProperty("name") + "__dv\">").append(value.length() == 0 ? "&nbsp;" : (containsHtml && value.indexOf("<script") < 0 ? value : DOMUtil.convertChars(value))).append("</span>").append("<input _dv=\"Y\" tabindex=\"-1\" edit=\"text\" type=\"hidden\" value=\"").append(escapedValue).append("\" ").append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5)).append(">");
                if (attributes.getProperty("translatevalue").indexOf("E") == 1) {
                    html.append(SDITagUtil.getTranslateIcon(textid, this.tp.getTextType(), "", this.pageContext));
                }
            } else {
                textid = value = attributes.getProperty("value");
                if ("Y".equals(attributes.getProperty("userelativedateformat"))) {
                    String relativedate = value;
                    try {
                        RelativeDateFormat relativeDateFormat = new RelativeDateFormat(false, this.getConfigurationProcessor().getPolicy("DateFormatPolicy", "Sapphire Custom", false), this.getTranslationProcessor());
                        Calendar calendar = (Calendar)attributes.get("calendar");
                        relativedate = calendar != null ? relativeDateFormat.format(calendar.getTime()) : "";
                    }
                    catch (SapphireException relativeDateFormat) {
                        // empty catch block
                    }
                    html.append("<span id=\"").append(attributes.getProperty("name")).append("__dv\" title=\"").append(escapedValue).append("\">").append(relativedate.length() == 0 ? "&nbsp;" : DOMUtil.convertChars(relativedate)).append("</span>").append("<input _dv=\"Y\" tabindex=\"-1\" edit=\"text\" type=\"hidden\" value=\"").append(escapedValue).append("\" ").append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5)).append(">");
                } else if ("Y".equals(attributes.getProperty("translatevalue"))) {
                    value = this.tp.translate(textid);
                    attributes.setProperty("onchange", attributes.getProperty("onchange") + ";document.getElementById( '" + attributes.getProperty("name") + "__dv' ).innerHTML=this.value");
                    html.append("<input readonly style=\"border:0\" class=\"input_field\" id=\"" + attributes.getProperty("name") + "__dv\"").append(" value=\"" + DOMUtil.convertChars(value) + "\"").append("\" ").append(attributes.getProperty("size").length() > 0 ? "size=" + attributes.getProperty("size") : "").append("/>").append("<input _dv=\"Y\" tabindex=\"-1\" edit=\"text\" type=\"hidden\" value=\"").append(escapedValue).append("\" ").append(SDITagUtil.getInputCommonAttributes(attributes)).append(">");
                } else {
                    html.append("<input tabindex=\"-1\" edit=\"text\" ").append(SDITagUtil.getInputAttributes("text", DOMUtil.convertChars(value), true)).append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5)).append(">");
                }
                if (attributes.getProperty("translatevalue").indexOf("E") == 1) {
                    html.append(SDITagUtil.getTranslateIcon(textid, this.tp.getTextType(), "", this.pageContext));
                }
            }
        } else if (mode.equalsIgnoreCase("imagefile") || mode.equalsIgnoreCase("image") || mode.equalsIgnoreCase("icon") || mode.equalsIgnoreCase("iconsvg") || mode.equalsIgnoreCase("picture")) {
            String hint;
            String className;
            StringBuffer open = new StringBuffer();
            if (mode.equalsIgnoreCase("imagefile")) {
                className = "image";
                hint = "Lookup an image file";
                open.append("sapphire.lookup.").append(className).append(".openFile( '").append(attributes.getProperty("name")).append("','" + HttpUtil.getAppRoot(this.pageContext.getServletContext()) + "');");
            } else {
                String m;
                className = "image";
                hint = "Lookup an image";
                if (mode.equalsIgnoreCase("icon")) {
                    m = "I";
                    open.append("sapphire.lookup.").append(className).append(".open( '").append(attributes.getProperty("name")).append("','").append(m).append("'").append(");");
                } else if (mode.equalsIgnoreCase("iconsvg")) {
                    m = "";
                    open.append("sapphire.lookup.").append(className).append(".open( '").append(attributes.getProperty("name")).append("','").append(m).append("'").append(",true,true,true);");
                } else if (mode.equalsIgnoreCase("picture")) {
                    m = "P";
                    open.append("sapphire.lookup.").append(className).append(".open( '").append(attributes.getProperty("name")).append("','").append(m).append("');");
                } else {
                    m = "";
                    open.append("sapphire.lookup.").append(className).append(".open( '").append(attributes.getProperty("name")).append("','").append(m).append("');");
                }
            }
            RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
            html.append("<div align=\"center\" style=\"display:").append((this.browser == null || this.browser.isIE()) && !this.html5 ? "inline" : "inline-block").append(";vertical-align:middle;border:solid 1px #B0C4DE; height:").append(this.browser == null || this.browser.isIE() ? "20px" : "17px").append(";width:20px;overflow:hidden;margin-top:").append(this.browser == null || this.browser.isIE() ? "-8px" : "-3px").append(";margin-right:5px;padding-top:1px;").append(this.browser == null || this.browser.isIE() ? "" : "").append("\">");
            Image image = new Image(this.pageContext);
            String imgSrc = attributes.getProperty("value", "");
            if ("iconsvg".equalsIgnoreCase(mode)) {
                imgSrc = "rc?command=image&svgimage=" + imgSrc + "&size=32";
            }
            image.setImageSrc(imgSrc);
            image.setDimensions(16, 16);
            image.setElementid(attributes.getProperty("name") + "_dyn");
            html.append(image.getHtml());
            html.append("</div>");
            html.append("<input ").append(SDITagUtil.getInputAttributes("text", escapedValue, attributes.getProperty("readonly").equals("true"))).append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5)).append(attributes.getProperty("disabled").equalsIgnoreCase("true") ? " disabled" : "").append(" data-mode=\"").append(mode).append("\">");
            html.append("<script>sapphire.events.attachEvent(").append(attributes.getProperty("name")).append(",'onchange',sapphire.lookup.").append(className).append(".change);</script>");
            html.append("<a ");
            if (attributes.getProperty("disabled").equalsIgnoreCase("true")) {
                html.append("style=\"display:").append("none").append(";\" ");
            } else {
                html.append("style=\"display:").append(displayStyle).append(";\" ");
            }
            html.append("displayStyle=\"").append(displayStyle).append("\" ");
            html.append("id=\"").append(attributes.getProperty("name")).append("_img\" href=\"#\" title=\"/").append("Lookup a image").append("\" onClick=\"").append(open).append("return false\" tabindex=\"0\">");
            html.append("<img title=\"").append(hint).append("\" border=\"0\" src=\"").append("WEB-CORE/imageref/flat/32/flat_black_external_lookup1.svg\" class=\"lookup_img\"").append("\">");
            html.append("</a>");
        } else if (mode.equalsIgnoreCase("color")) {
            RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
            html.append("<div id=\"").append(attributes.getProperty("name")).append("_div\" style=\"position:relative;width:20px;height:17px;").append((this.browser == null || this.browser.isIE()) && !this.html5 ? "top:0" : "margin-top:-5px;top:5px").append(";border:solid 1px gray;display:").append((this.browser == null || this.browser.isIE()) && !this.html5 ? "inline" : "inline-block").append(";background-color:").append(attributes.getProperty("value")).append("\"></div>");
            html.append("<input ").append(SDITagUtil.getInputAttributes("text", attributes.getProperty("value"), attributes.getProperty("readonly").equals("true"))).append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5)).append(attributes.getProperty("disabled").equalsIgnoreCase("true") ? " disabled" : "").append(">");
            html.append("<script>sapphire.events.attachEvent(document.getElementById('").append(attributes.getProperty("name")).append("'),'onchange',sapphire.lookup.color.change);</script>");
            html.append("<a ");
            if (attributes.getProperty("disabled").equalsIgnoreCase("true")) {
                html.append("style=\"display:").append("none").append(";\" ");
            } else {
                html.append("style=\"display:").append(displayStyle).append(";\" ");
            }
            html.append("displayStyle=\"").append(displayStyle).append("\" ");
            html.append("id=\"").append(attributes.getProperty("name")).append("_img\" href=\"#\" title=\"/").append("Lookup a image").append("\" onClick=\"sapphire.lookup.color.open( '").append(attributes.getProperty("name")).append("','','','','',true);return false\" tabindex=\"0\">");
            html.append("<img title=\"").append("Lookup a image").append("\" border=\"0\" src=\"").append("WEB-CORE/imageref/flat/32/flat_black_external_lookup1.svg\" class=\"lookup_img").append("\">");
            html.append("</a>");
        } else if (mode.equalsIgnoreCase("password")) {
            html.append("<input ").append(SDITagUtil.getInputAttributes("password", attributes.getProperty("value").length() > 0 && ("User".equals(sdiInfo.getSdcid()) || "Custodian".equals(sdiInfo.getSdcid())) ? "(storedpassword)" : attributes.getProperty("value"), attributes.getProperty("readonly").equals("true"))).append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5)).append(attributes.getProperty("disabled").equalsIgnoreCase("true") ? " disabled" : "").append(">");
        } else if (mode.equalsIgnoreCase("inputarea")) {
            String lookupHTML = this.getLookupIconHTML(attributes, sdiInfo, data, attributes.getProperty("disabled").equalsIgnoreCase("true"), displayStyle);
            if (lookupHTML.trim().length() == 0) {
                html.append("<textarea ").append("1".equals(attributes.getProperty("rows")) ? "style=\"overflow:hidden\" " : "").append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5)).append(attributes.getProperty("rows").length() > 0 ? "rows=\"" + attributes.getProperty("rows") + "\" " : "").append(attributes.getProperty("cols").length() > 0 ? "cols=\"" + attributes.getProperty("cols") + "\" " : "").append(attributes.getProperty("readonly").equals("true") ? "readonly " : "").append(attributes.getProperty("wrap").equals("true") ? "wrap " : "").append(attributes.getProperty("disabled").equalsIgnoreCase("true") ? " disabled" : "").append(">").append(escapedValue).append("</textarea>");
            } else {
                html.append("<table cellspacing=\"0\" cellpadding=\"0\"><tr>");
                html.append("<td><textarea ").append("1".equals(attributes.getProperty("rows")) ? "style=\"overflow:hidden\" " : "").append(SDITagUtil.getInputCommonAttributes(attributes)).append(attributes.getProperty("rows").length() > 0 ? "rows=\"" + attributes.getProperty("rows") + "\" " : "").append(attributes.getProperty("cols").length() > 0 ? "cols=\"" + attributes.getProperty("cols") + "\" " : "").append(attributes.getProperty("readonly").equals("true") ? "readonly " : "").append(attributes.getProperty("wrap").equals("true") ? "wrap " : "").append(attributes.getProperty("disabled").equalsIgnoreCase("true") ? " disabled" : "").append(">").append(escapedValue).append("</textarea></td>");
                html.append("<td>").append(lookupHTML).append("</td>");
                html.append("</tr></table>");
            }
        } else if (mode.equalsIgnoreCase("hidden")) {
            String n = SDITagUtil.getNameAndIdAttributes(attributes);
            String ia = SDITagUtil.getInputAttributes("hidden", escapedValue, false);
            String fk = SDITagUtil.getFKInputAttributes(attributes);
            String e = SDITagUtil.getExtraAttributes(attributes);
            String oc = SDITagUtil.getInputEventAttributes(attributes, true, this.browser, this.html5);
            html.append("<input ").append(n.length() > 0 ? n + " " : "").append(ia.length() > 0 ? ia + " " : "").append(oc.length() > 0 ? oc + " " : "").append(fk.length() > 0 ? fk + " " : "").append(e.length() > 0 ? e : "").append(">");
        } else if (mode.equalsIgnoreCase("maskvalue")) {
            html.append("<span class=\"maint_maskedfield\"></span>");
        } else if (mode.equalsIgnoreCase("dropdowncombo") || attributes.getProperty("dataentrymode").equals("dropdowncombo")) {
            html.append("<input edit=\"lookup\" ").append(SDITagUtil.getInputAttributes("text", escapedValue, attributes.getProperty("readonly").equals("true"))).append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5));
            String dropdownid = attributes.getProperty("reftypeid");
            if (dropdownid.length() == 0) {
                dropdownid = attributes.getProperty("sdcid");
            }
            if (dropdownid.length() == 0 || attributes.getProperty("sql").trim().length() > 0) {
                dropdownid = attributes.getProperty("columnid");
            }
            if (dropdownid.length() == 0 || attributes.getProperty("dropdownvalues").trim().length() > 0) {
                dropdownid = attributes.getProperty("dropdowncomboid");
                String reftypeid = attributes.getProperty("reftypeid", "");
                if (dropdownid.length() == 0 && reftypeid.equals("")) {
                    dropdownid = attributes.getProperty("columnid");
                } else if (dropdownid.length() == 0) {
                    dropdownid = reftypeid;
                }
            }
            html.append("dropdownid=\"").append(dropdownid).append("\" ");
            html.append(" onkeypress=\"dd_inputKeyPress( this )\" onkeyup=\"dd_inputKeyUp( this )\"");
            html.append(attributes.getProperty("disabled").equalsIgnoreCase("true") ? " disabled" : "");
            html.append(" /> ");
            html.append("<div ");
            if (attributes.getProperty("disabled").equalsIgnoreCase("true")) {
                html.append("style=\"display:").append("none").append(";\" ");
            } else {
                html.append("style=\"display:").append(displayStyle).append(";\" ");
            }
            html.append("displayStyle=\"").append(displayStyle).append("\" ");
            html.append("id=\"").append(attributes.getProperty("name")).append("_img\" ");
            html.append("onclick=\"dd_toggleDropDown( '").append(attributes.getProperty("name")).append("' )\"");
            html.append(" class=\"downbutton\"><img src=\"WEB-CORE/elements/images/down.gif\" style=\"heigh:16px;width:16px\"></div>");
            if (attributes.getProperty("translatevalue").indexOf("E") == 1) {
                html.append(SDITagUtil.getTranslateIcon("", this.tp.getTextType(), attributes.getProperty("name"), this.pageContext));
            }
        } else if (mode.equalsIgnoreCase("dropdownlist") || attributes.getProperty("dataentrymode").equals("dropdownlist") || mode.equals("dropdownlistvalidated")) {
            PropertyList dropdowndefinition = attributes.getPropertyList("dropdowndefinition");
            if (this.browser != null && this.browser.isIE() && this.browser.getVersion() > 10.0 && attributes.getProperty("style").length() == 0) {
                attributes.setProperty("style", "border-radius:2px;");
            }
            if (dropdowndefinition != null && (dropdowndefinition.getProperty("sdcid").trim().length() > 0 || dropdowndefinition.getProperty("querywhere").trim().length() > 0)) {
                String[] overrides = new String[]{"sdcid", "queryfrom", "querywhere", "queryorderby"};
                for (int i = 0; i < overrides.length; ++i) {
                    if (dropdowndefinition.getProperty(overrides[i]).length() <= 0) continue;
                    attributes.setProperty(overrides[i], dropdowndefinition.getProperty(overrides[i]));
                }
                attributes.setProperty("valuecolumn", dropdowndefinition.getProperty("valuecolumn"));
                attributes.setProperty("displaycolumn", dropdowndefinition.getProperty("displaycolumn"));
                html.append(this.getSDCSelect(attributes, sdiInfo));
            } else if (attributes.getProperty("sdcid").length() > 0 && attributes.getProperty("sql").trim().length() == 0) {
                html.append(this.getSDCSelect(attributes, sdiInfo));
            } else if (attributes.getProperty("reftypeid").length() > 0 && attributes.getProperty("sql").trim().length() == 0) {
                html.append(this.getRefTypeSelect(attributes, sdiInfo));
            } else if (attributes.getProperty("sql").length() > 0) {
                html.append(this.getSqlSelect(attributes, sdiInfo));
            } else if (attributes.getProperty("displayvalue").length() > 0) {
                html.append(this.getDisplayValueSelect(attributes));
            } else if (attributes.getProperty("dropdownvalues").length() > 0) {
                html.append(this.getDropdownValuesSelect(attributes));
            } else if (attributes.getProperty("emptydropdown").equalsIgnoreCase("true")) {
                html.append("<select " + (attributes.getProperty("readonly").equals("true") || attributes.getProperty("disabled").equals("true") ? "disabled " : "") + SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5) + "><option value=\"\"></option></select>");
            } else if (attributes.getPropertyList("dynamicdropdown") != null && attributes.getPropertyList("dynamicdropdown").getProperty("elementid").length() > 0) {
                Boolean dynamicdropdownrendered;
                html.append("<select " + (attributes.getProperty("readonly").equals("true") || attributes.getProperty("disabled").equals("true") ? "disabled " : "") + SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5) + ">");
                html.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("value"))).append("\" selected>").append(SafeHTML.encodeForHTML(attributes.getProperty("value"))).append("</option>");
                html.append("</select>");
                if (!(this.pageContext == null || (dynamicdropdownrendered = (Boolean)this.pageContext.getAttribute("_ddd_" + attributes.getProperty("data") + "_" + attributes.getProperty("columnid"))) != null && dynamicdropdownrendered.booleanValue())) {
                    PropertyList dynamicDDPropertyList = attributes.getPropertyList("dynamicdropdown");
                    html.append("<script>");
                    html.append("sapphire.events.registerLoadListener(function(){");
                    html.append("var o1 = document.getElementById('__" + dynamicDDPropertyList.getProperty("elementid") + "_dataset');");
                    html.append("sapphire.page.maint.populateDropdown('" + attributes.getProperty("data") + "','" + attributes.getProperty("columnid") + "',o1!=null?o1.value:'','" + dynamicDDPropertyList.getProperty("valuefield") + "','" + dynamicDDPropertyList.getProperty("displayvaluefield") + "','" + dynamicDDPropertyList.getPropertyListNotNull("filter").toJSONString(false) + "', true)");
                    html.append("});");
                    html.append("</script>");
                    this.pageContext.setAttribute("_ddd_" + attributes.getProperty("data") + "_" + attributes.getProperty("columnid"), (Object)Boolean.TRUE);
                }
            }
            if (attributes.getProperty("translatevalue").indexOf("E") == 1) {
                html.append(SDITagUtil.getTranslateIcon("", this.tp.getTextType(), attributes.getProperty("name"), this.pageContext));
            }
        } else if (mode.indexOf("radiobutton") >= 0) {
            if (attributes.getProperty("reftypeid").length() > 0 && attributes.getProperty("sql").trim().length() == 0) {
                html.append(this.getRefTypeRadiobutton(attributes, sdiInfo));
            } else if (attributes.getProperty("sdcid").length() > 0 && attributes.getProperty("sql").trim().length() == 0) {
                html.append(this.getSDCRadiobutton(attributes, sdiInfo));
            } else if (attributes.getProperty("sql").length() > 0) {
                html.append(this.getSqlRadiobutton(attributes, sdiInfo));
            } else if (attributes.getProperty("displayvalue").length() > 0) {
                html.append(this.getDisplayValueRadioButton(attributes));
            }
            if (attributes.getProperty("translatevalue").indexOf("E") == 1) {
                html.append(SDITagUtil.getTranslateIcon("", this.tp.getTextType(), attributes.getProperty("name"), this.pageContext));
            }
            String fieldid = attributes.getProperty("name");
            html.append("<input type=\"hidden\" name=\"" + fieldid + "_radiomarker\" value=\"Y\">");
        } else if (mode.equalsIgnoreCase("checkbox")) {
            html.append("<div>" + this.getRefTypeCheckbox(attributes, sdiInfo) + "</div>");
        } else if (mode.equalsIgnoreCase("datelookup") || attributes.getProperty("dataentrymode").equals("datelookup")) {
            html.append("<input ").append(SDITagUtil.getInputAttributes("text", attributes.getProperty("value"), attributes.getProperty("readonly").equals("true"))).append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5)).append(attributes.getProperty("disabled").equalsIgnoreCase("true") ? " disabled" : "").append(">");
            if (attributes.getProperty("img").length() > 0) {
                html.append("<a ");
                if (attributes.getProperty("disabled").equalsIgnoreCase("true")) {
                    html.append("style=\"display:").append("none").append(";\" ");
                } else {
                    html.append("style=\"display:").append(displayStyle).append(";\" ");
                }
                html.append("displayStyle=\"").append(displayStyle).append("\" ");
                html.append("id=\"").append(attributes.getProperty("name")).append("_img\" href=\"#\" title=\"/").append(attributes.getProperty("imgtext").length() > 0 ? attributes.getProperty("imgtext") : "Lookup a date").append("\" onClick=\"sapphire.lookup.date.open( '").append(attributes.getProperty("name")).append("','', '").append(SDIData.getDatasetCode(data)).append("', '").append(attributes.getProperty("rowindex")).append("', '").append(attributes.getProperty("columnid")).append("', '").append(attributes.getProperty("format")).append("' );return false\" tabindex=\"0\">");
                String img_cssstyle = attributes.getProperty("img_cssstyle");
                html.append("<img title=\"").append(attributes.getProperty("imgtext").length() > 0 ? attributes.getProperty("imgtext") : "Lookup a date").append("\" border=\"0\" src=\"").append(attributes.getProperty("img")).append("\" ").append(img_cssstyle.length() > 0 ? " class=\"" + img_cssstyle + "\"" : "").append(" >");
                html.append("</a>");
            }
        } else if (mode.equalsIgnoreCase("coordinatelookup")) {
            html.append("<input ").append(SDITagUtil.getInputAttributes("text", attributes.getProperty("value"), attributes.getProperty("readonly").equals("true"))).append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5)).append(attributes.getProperty("disabled").equalsIgnoreCase("true") ? " disabled" : "").append(">");
            if (attributes.getProperty("img").length() > 0) {
                html.append("<a ");
                if (attributes.getProperty("disabled").equalsIgnoreCase("true")) {
                    html.append("style=\"display:").append("none").append(";\" ");
                } else {
                    html.append("style=\"display:").append(displayStyle).append(";\" ");
                }
                html.append("displayStyle=\"").append(displayStyle).append("\" ");
                html.append("id=\"").append(attributes.getProperty("name")).append("_img\" href=\"#\" title=\"/").append(attributes.getProperty("imgtext").length() > 0 ? attributes.getProperty("imgtext") : "Lookup coordinates").append("\" onClick=\"sapphire.lookup.coordinate.open( '").append(attributes.getProperty("name")).append("','', '").append(SDIData.getDatasetCode(data)).append("', '").append(attributes.getProperty("rowindex")).append("', '").append(attributes.getProperty("columnid")).append("', '").append(SDIData.getDatasetCode(data)).append(attributes.getProperty("rowindex")).append("_").append("latitude").append("', '").append(SDIData.getDatasetCode(data)).append(attributes.getProperty("rowindex")).append("_").append("longitude").append("', '").append(SDIData.getDatasetCode(data)).append(attributes.getProperty("rowindex")).append("_").append("projection").append("' );return false\" tabindex=\"0\">");
                String img_cssstyle = attributes.getProperty("img_cssstyle");
                html.append("<img title=\"").append(attributes.getProperty("imgtext").length() > 0 ? attributes.getProperty("imgtext") : this.tp.translate("Lookup coordinates")).append("\" border=\"0\" src=\"").append(attributes.getProperty("img")).append("\" ").append(img_cssstyle.length() > 0 ? " class=\"" + img_cssstyle + "\"" : "").append(" >");
                html.append("</a>");
                html.append("<a ");
                if (attributes.getProperty("disabled").equalsIgnoreCase("true")) {
                    html.append("style=\"display:").append("none").append(";\" ");
                } else {
                    html.append("style=\"display:").append(displayStyle).append(";\" ");
                }
                MapHelper map = new MapHelper(this.pageContext);
                if (map.canGeoLocate()) {
                    html.append("displayStyle=\"").append(displayStyle).append("\" ");
                    html.append("id=\"").append(attributes.getProperty("name")).append("_img\" href=\"#\" title=\"/").append(attributes.getProperty("imgtext").length() > 0 ? attributes.getProperty("imgtext") : "Geolocate your position").append("\" onClick=\"sapphire.lookup.coordinate.open( '").append(attributes.getProperty("name")).append("','', '").append(SDIData.getDatasetCode(data)).append("', '").append(attributes.getProperty("rowindex")).append("', '").append(attributes.getProperty("columnid")).append("', '").append(SDIData.getDatasetCode(data)).append(attributes.getProperty("rowindex")).append("_").append("latitude").append("', '").append(SDIData.getDatasetCode(data)).append(attributes.getProperty("rowindex")).append("_").append("longitude").append("', '").append(SDIData.getDatasetCode(data)).append(attributes.getProperty("rowindex")).append("_").append("projection").append("', 'Y' );return false\" tabindex=\"0\">");
                    html.append("<img title=\"").append(this.tp.translate("Geolocate your current position")).append("\" border=\"0\" src=\"").append("WEB-CORE/imageref/flat/32/flat_black_location_checkin.svg").append("\" ").append(img_cssstyle.length() > 0 ? " class=\"" + img_cssstyle + "\"" : "").append(" >");
                    html.append("</a>");
                }
            }
        } else if (mode.equalsIgnoreCase("lookup") || attributes.getProperty("dataentrymode").equals("lookup")) {
            boolean enableSuggest;
            boolean readonly = attributes.getProperty("readonly").equals("true");
            if (attributes.getProperty("sdcid").length() == 0 && attributes.getProperty("reftypeid").length() == 0 && attributes.getProperty("editorstyleid").length() == 0 && (attributes.getProperty("fklink").length() <= 0 || attributes.getProperty("relatedcolumns").length() <= 0)) {
                readonly = false;
            }
            String luhtml = "";
            boolean disabled = attributes.getProperty("disabled").equalsIgnoreCase("true");
            boolean bl = enableSuggest = attributes.getPropertyList("lookuplink") != null && ("Y".equals(attributes.getPropertyList("lookuplink").getProperty("enablesuggest")) || "Yes".equals(attributes.getPropertyList("lookuplink").getProperty("enablesuggest")) || "Yes(hide lookup icon)".equals(attributes.getPropertyList("lookuplink").getProperty("enablesuggest")));
            if (enableSuggest) {
                if (this.browser.isPhone() || this.browser.isTablet()) {
                    attributes.setProperty("onkeypress", attributes.getProperty("onkeypress") + ";showSuggestion( " + (readonly ? "null, true" : "") + " )");
                    readonly = false;
                } else {
                    attributes.setProperty("onkeypress", attributes.getProperty("onkeypress") + ";showSuggestion()");
                }
                attributes.setProperty("autocomplete", "off");
            }
            html.append("<input edit=\"lookup\" ").append(SDITagUtil.getInputAttributes("text", attributes.getProperty("value"), readonly)).append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5));
            if (enableSuggest) {
                html.append(" style=\"border:1px solid green\" ");
            }
            if (readonly) {
                if (attributes.getProperty("dataentrymode").equals("lookup")) {
                    html.append(" onkeydown=\"if(event.keyCode==46 && this.released != 'Y'){sapphire.lookup.sdi.clear('").append(attributes.getProperty("lookupfieldid", attributes.getProperty("name", ""))).append("');}\" ");
                } else {
                    html.append(" onkeydown=\"if(event.keyCode==8){return false;};if(event.keyCode==46){sapphire.lookup.sdi.clear('").append(attributes.getProperty("lookupfieldid", attributes.getProperty("name", ""))).append("');}\" ");
                }
            }
            html.append(disabled ? " disabled" : "").append(">");
            if (attributes.getPropertyList("lookuplink") != null && "Yes(hide lookup icon)".equals(attributes.getPropertyList("lookuplink").getProperty("enablesuggest"))) {
                displayStyle = "none";
            }
            luhtml = this.getLookupIconHTML(attributes, sdiInfo, data, disabled, displayStyle);
            html.append(luhtml);
            if (attributes.getProperty("translatevalue").indexOf("E") == 1) {
                html.append(SDITagUtil.getTranslateIcon("", this.tp.getTextType(), attributes.getProperty("name"), this.pageContext));
            }
        } else if (mode.equalsIgnoreCase("data")) {
            QueryData queryData = sdiInfo.getQueryData("dataitem");
            String datatypes = queryData.getValue(queryData.getCurrentRow(), "datatypes", "N");
            if (datatypes.equals("V")) {
                attributes.setProperty("reftypeid", queryData.getValue(queryData.getCurrentRow(), "entryreftypeid", ""));
                html.append(this.getRefTypeSelect(attributes, sdiInfo));
            } else {
                html.append("<input ").append(SDITagUtil.getInputAttributes("text", attributes.getProperty("value"), attributes.getProperty("readonly").equals("true"))).append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5)).append(attributes.getProperty("disabled").equalsIgnoreCase("true") ? " disabled" : "").append("/>");
            }
        } else if (mode.equalsIgnoreCase("pseudocolumn")) {
            String datasetname = attributes.getProperty("datasetname");
            String name3 = attributes.getProperty("name");
            String content = attributes.getProperty("pseudocolumn", "");
            if ((content = StringUtil.replaceAll(content, "[name]", name3)).contains("[content=")) {
                int start = content.indexOf("[content=");
                int end = content.lastIndexOf("]");
                html.append("<div ").append("id=\"").append(name3 != null ? name3 + "_container" : "").append("\" ");
                if (start > -1 && end > -1) {
                    String sheight;
                    String exp = content.substring(start, end + 1);
                    String url = exp.substring(9, exp.length() - 1);
                    String width = "800";
                    String height = "500";
                    if (url.length() == 0) {
                        url = "about:blank";
                    } else {
                        if (url.contains(";")) {
                            String[] parts = StringUtil.split(url, ";");
                            url = parts[0];
                            if (parts.length > 1) {
                                width = parts[1];
                                if (parts.length > 2) {
                                    height = parts[2];
                                }
                            }
                        }
                        url = ElementUtil.evaluateExpression(attributes.getProperty("datasetname"), -1, attributes.getProperty("columnid"), url, sdiInfo);
                    }
                    String swidth = width;
                    if (!swidth.endsWith("%") || !swidth.endsWith("px")) {
                        swidth = swidth + "px";
                    }
                    if (!(sheight = height).endsWith("%") || !sheight.endsWith("px")) {
                        sheight = sheight + "px";
                    }
                    html.append("style=\"width:").append(swidth).append(";height:").append(sheight).append(";\" swidth=\"").append(width).append("\" sheight=\"").append(height).append("\">");
                    html.append("<iframe name=\"").append(name3 != null ? name3 : "").append("\" id=\"").append(name3 != null ? name3 : "").append("\" src=\"").append(url).append("\" frameborder=\"0\" width=\"100%\" height=\"100%\"></iframe>");
                } else {
                    html.append("style=\"width:800px;height:500px;\">");
                }
                html.append("</div>");
            } else {
                html.append("<span ").append("id=\"").append(name3 != null ? name3 + "_container" : "").append("\" ").append(">");
                if (!"primary".equals(datasetname) && sdiInfo.getCurrentRow(datasetname) == -9999) {
                    if ("Y".equals(attributes.getProperty("translatevalue"))) {
                        html.append(this.tp.translate(content));
                    } else {
                        html.append(content);
                    }
                } else {
                    content = ElementUtil.evaluatePageDirectives(content, sdiInfo);
                    if ("Y".equals(attributes.getProperty("translatevalue"))) {
                        html.append(ElementUtil.evaluateExpression(attributes.getProperty("datasetname"), -1, attributes.getProperty("columnid"), content, sdiInfo, this.tp));
                    }
                    html.append(ElementUtil.evaluateExpression(attributes.getProperty("datasetname"), -1, attributes.getProperty("columnid"), content, sdiInfo, null, null, true));
                }
                html.append("</span>");
            }
        }
        if (attributes.containsKey("outputattributes") && attributes.getPropertyList("outputattributes") != null && (name = attributes.getProperty("name", "")).length() > 0) {
            html.append("<input type=\"hidden\" name=\"").append(name).append("__properties\" value=\"").append(StringUtil.replaceAll(attributes.getPropertyList("outputattributes").toJSONString(false), "\"", "'")).append("\">");
        }
        return html.toString();
    }

    public static PropertyList getListPageDirectives(String sdcid, String primarycolumnid, String versioncolumnid, boolean versioned, String selectortype, boolean hideSearch, String restrictiveWhere, String defaultQuery, String queryFrom, String queryWhere, PropertyListCollection columns, TranslationProcessor tp, SDCProcessor sdcProcessor) {
        return SDITagUtil.getPageDirectives(sdcid, primarycolumnid, versioncolumnid, versioned, selectortype, true, false, hideSearch, restrictiveWhere, defaultQuery, queryFrom, queryWhere, "", columns, true, new StringBuffer(), new StringBuffer(), false, tp, sdcProcessor);
    }

    public static PropertyList getLookupPageDirectives(String sdcid, String primarycolumnid, String versioncolumnid, boolean versioned, String selectortype, boolean hideTtitle, String restrictiveWhere, String defaultQuery, String queryFrom, String queryWhere, String fieldprefix, PropertyListCollection columns, boolean fromScratch, StringBuffer mapcolumnid, StringBuffer fieldid, TranslationProcessor tp, SDCProcessor sdcProcessor) {
        return SDITagUtil.getPageDirectives(sdcid, primarycolumnid, versioncolumnid, versioned, selectortype, hideTtitle, true, false, restrictiveWhere, defaultQuery, queryFrom, queryWhere, fieldprefix, columns, fromScratch, mapcolumnid, fieldid, false, tp, sdcProcessor);
    }

    public static PropertyList getLookupPageDirectives(String sdcid, String primarycolumnid, String versioncolumnid, boolean versioned, String selectortype, boolean hideTtitle, String restrictiveWhere, String defaultQuery, String queryFrom, String queryWhere, String fieldprefix, PropertyListCollection columns, boolean fromScratch, StringBuffer mapcolumnid, StringBuffer fieldid, boolean includetemplates, TranslationProcessor tp, SDCProcessor sdcProcessor) {
        return SDITagUtil.getPageDirectives(sdcid, primarycolumnid, versioncolumnid, versioned, selectortype, hideTtitle, true, false, restrictiveWhere, defaultQuery, queryFrom, queryWhere, fieldprefix, columns, fromScratch, mapcolumnid, fieldid, includetemplates, tp, sdcProcessor);
    }

    private static PropertyList getPageDirectives(String sdcid, String primarycolumnid, String versioncolumnid, boolean versioned, String selectortype, boolean hideTtitle, boolean isLookup, boolean hideSearch, String restrictiveWhere, String defaultQuery, String queryFrom, String queryWhere, String fieldprefix, PropertyListCollection columns, boolean fromScratch, StringBuffer mapcolumnid, StringBuffer fieldid, boolean includetemplates, TranslationProcessor tp, SDCProcessor sdcProcessor) {
        PropertyList layout;
        PropertyList pagedirectives = new PropertyList();
        pagedirectives.setProperty("selectortype", selectortype);
        if (includetemplates) {
            pagedirectives.setProperty("includetemplates", "Y");
        }
        if (restrictiveWhere != null && restrictiveWhere.length() > 0) {
            pagedirectives.setProperty("restrictivewhere", EncryptDecrypt.obfsql(restrictiveWhere));
        }
        if (defaultQuery != null && defaultQuery.length() > 0) {
            pagedirectives.setProperty("defaultquery", defaultQuery);
        }
        if (queryFrom != null && queryFrom.length() > 0) {
            pagedirectives.setProperty("queryfrom", EncryptDecrypt.obfsql(queryFrom));
        }
        if (queryWhere != null && queryWhere.length() > 0) {
            pagedirectives.setProperty("querywhere", EncryptDecrypt.obfsql(queryWhere));
        }
        if (fromScratch) {
            layout = new PropertyList();
            if (isLookup) {
                layout.setProperty("objectname", "WEB-OPAL/layouts/popup/popuplayout.jsp");
            } else {
                layout.setProperty("objectname", "WEB-OPAL/layouts/blank/blank.jsp");
            }
            layout.setProperty("applicationtitle", "LabVantage");
            pagedirectives.setProperty("layout", layout);
            pagedirectives.setProperty("", sdcid);
            pagedirectives.setProperty("sdcid", sdcid);
            pagedirectives.setProperty("rowsperpage", "50");
            String plu = sdcProcessor.getProperty(sdcid, "plural", "");
            plu = plu.substring(0, 1).toUpperCase() + plu.substring(1);
            if (tp != null) {
                pagedirectives.setProperty("title", tp.translate(plu) + " " + tp.translate(isLookup ? "Lookup" : "List"));
            } else {
                pagedirectives.setProperty("title", plu + " " + (isLookup ? "Lookup" : "List"));
            }
            PropertyListCollection sdccols = sdcProcessor.getColumns(sdcid);
            if (hideSearch) {
                PropertyList search = new PropertyList();
                search.setProperty("sdcid", sdcid);
                search.setProperty("show", "N");
                search.setProperty("showinitially", "N");
                pagedirectives.setProperty("advancedsearch", search);
            }
            if (isLookup) {
                String keycolid2;
                PropertyList plcol;
                String coldesc = "Description";
                String colid = sdcProcessor.getProperty(sdcid, "desccol", "");
                PropertyList sdccol = sdccols.getPropertyList(colid);
                if (sdccol != null) {
                    coldesc = sdccol.getProperty("columndesc", coldesc);
                }
                if ((plcol = columns.find("columnid", colid)) == null) {
                    plcol = columns.find("columnid", "desccol");
                }
                if (plcol == null) {
                    plcol = new PropertyList();
                    columns.add(0, plcol);
                }
                plcol.setProperty("id", colid);
                plcol.setProperty("columnid", colid);
                plcol.setProperty("title", plcol.getProperty("title", tp != null ? tp.translate(coldesc) : coldesc));
                plcol.setProperty("returnvalue", plcol.getProperty("returnvalue", "Y"));
                if (versioned) {
                    coldesc = "Version";
                    colid = sdcProcessor.getProperty(sdcid, "keycolid2", "");
                    sdccol = sdccols.getPropertyList(colid);
                    if (sdccol != null) {
                        coldesc = sdccol.getProperty("columndesc", coldesc);
                    }
                    if ((plcol = columns.find("columnid", colid)) == null) {
                        plcol = columns.find("columnid", "keycolid2");
                    }
                    if (plcol == null) {
                        plcol = new PropertyList();
                        columns.add(0, plcol);
                    }
                    plcol.setProperty("id", colid);
                    plcol.setProperty("columnid", colid);
                    plcol.setProperty("title", plcol.getProperty("title", tp != null ? tp.translate(coldesc) : coldesc));
                    plcol.setProperty("returnvalue", plcol.getProperty("returnvalue", "Y"));
                }
                coldesc = "Id";
                colid = sdcProcessor.getProperty(sdcid, "keycolid1", "");
                sdccol = sdccols.getPropertyList(colid);
                if (sdccol != null) {
                    coldesc = sdccol.getProperty("columndesc", coldesc);
                }
                if ((plcol = columns.find("columnid", colid)) == null) {
                    plcol = columns.find("columnid", "keycolid1");
                }
                if (plcol == null) {
                    plcol = new PropertyList();
                    columns.add(0, plcol);
                }
                if ((keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2", "")).length() > 0 && columns.find("columnid", "keycolid2") != null) {
                    columns.find("columnid", "keycolid2").setProperty("columnid", keycolid2);
                } else if (keycolid2.length() == 0 && columns.find("columnid", "keycolid2") != null) {
                    columns.remove(columns.find("columnid", "keycolid2"));
                }
                String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3", "");
                if (keycolid3.length() > 0 && columns.find("columnid", "keycolid3") != null) {
                    columns.find("columnid", "keycolid3").setProperty("columnid", keycolid3);
                } else if (keycolid3.length() == 0 && columns.find("columnid", "keycolid3") != null) {
                    columns.remove(columns.find("columnid", "keycolid3"));
                }
                plcol.setProperty("id", colid);
                plcol.setProperty("columnid", colid);
                plcol.setProperty("title", plcol.getProperty("title", tp != null ? tp.translate(coldesc) : coldesc));
                plcol.setProperty("returnvalue", plcol.getProperty("returnvalue", "Y"));
                pagedirectives.setProperty("rowclickselection", "Y");
                if (!selectortype.equalsIgnoreCase("checkbox")) {
                    PropertyList pllink = plcol.getPropertyList("link");
                    if (pllink == null) {
                        pllink = new PropertyList();
                        pllink.setId("link");
                        plcol.setProperty("link", pllink);
                    }
                    if (versioned) {
                        pllink.setProperty("href", plcol.getProperty("href", "javascript:parent.accept('[keycolid1];[keycolid2]');"));
                    } else {
                        pllink.setProperty("href", plcol.getProperty("href", "javascript:parent.accept('[keycolid1]');"));
                    }
                    if (tp != null) {
                        pllink.setProperty("tip", plcol.getProperty("tip", tp.translate("Select and return")));
                    } else {
                        pllink.setProperty("tip", plcol.getProperty("tip", "Select and return"));
                    }
                }
                pagedirectives.setProperty("columns", columns);
            } else {
                pagedirectives.setProperty("columns", columns);
            }
            PropertyListCollection btns = new PropertyListCollection();
            if (isLookup) {
                PropertyList plstnd;
                PropertyList plcp;
                PropertyList plbtn;
                if (selectortype.equalsIgnoreCase("checkbox") || selectortype.equalsIgnoreCase("radiobutton")) {
                    plbtn = new PropertyList();
                    plcp = new PropertyList();
                    plstnd = new PropertyList();
                    plbtn.setProperty("id", "Accept");
                    if (tp != null) {
                        plcp.setProperty("text", tp.translate("Select & Return"));
                        plcp.setProperty("tip", tp.translate("Select and Close"));
                    } else {
                        plcp.setProperty("text", "Select & Return");
                        plcp.setProperty("tip", "Select and Close");
                    }
                    plcp.setProperty("image", "WEB-CORE/images/gif/SelectAndReturn.gif");
                    plstnd.setProperty("action", "Accept");
                    plbtn.setProperty("commonprops", plcp);
                    plbtn.setProperty("standardbuttonprops", plstnd);
                    btns.add(plbtn);
                    pagedirectives.setProperty("selectortype", selectortype);
                }
                plbtn = new PropertyList();
                plcp = new PropertyList();
                plstnd = new PropertyList();
                plbtn.setProperty("id", "Cancel");
                if (tp != null) {
                    plcp.setProperty("text", tp.translate("Cancel"));
                    plcp.setProperty("tip", tp.translate("Cancel and Close"));
                } else {
                    plcp.setProperty("text", "Cancel");
                    plcp.setProperty("tip", "Cancel and Close");
                }
                plcp.setProperty("image", "WEB-CORE/images/gif/Cancel.gif");
                plstnd.setProperty("action", "Cancel");
                plbtn.setProperty("commonprops", plcp);
                plbtn.setProperty("standardbuttonprops", plstnd);
                btns.add(plbtn);
            }
            pagedirectives.setProperty("buttons", btns);
        }
        if (hideTtitle) {
            layout = pagedirectives.getPropertyList("layout");
            if (layout == null) {
                layout = new PropertyList();
                pagedirectives.setProperty("layout", layout);
            }
            layout.setProperty("hidetitle", "Y");
        }
        if (isLookup) {
            if (columns != null && columns.size() > 0) {
                int blanks = 0;
                boolean currentfieldmapped = false;
                boolean currentversionmapped = false;
                for (int i = 0; i < columns.size(); ++i) {
                    boolean toreturn;
                    PropertyList column = columns.getPropertyList(i);
                    String mapfield = column.getProperty("mapfieldid", "");
                    String columnid = column.getProperty("columnid", "");
                    columnid = RequestParser.parseAlias(columnid);
                    String mode = column.getProperty("mode", "Display Text");
                    String lumode = column.getProperty("lumode", mode);
                    if (lumode.equalsIgnoreCase("Display and Return")) {
                        toreturn = true;
                        mode = "Display Text";
                    } else if (lumode.equalsIgnoreCase("Return Only") || lumode.equalsIgnoreCase("Hidden and Return")) {
                        toreturn = true;
                        mode = "Hidden Value";
                    } else if (lumode.equalsIgnoreCase("Hidden Only")) {
                        toreturn = false;
                        mode = "Hidden Value";
                    } else {
                        mode = "Display Text";
                        toreturn = false;
                    }
                    if (columnid.length() > 0 && toreturn) {
                        if (mapfield.length() > 0) {
                            if (fieldid.length() > 0) {
                                fieldid.append(";");
                                mapcolumnid.append(";");
                            }
                            mapcolumnid.append(columnid);
                            String fieldname = fieldprefix + mapfield;
                            if (fieldname.equals(primarycolumnid)) {
                                currentfieldmapped = true;
                            } else if (fieldname.equals(versioncolumnid)) {
                                currentversionmapped = true;
                            }
                            fieldid.append(fieldname);
                        } else if (blanks == 0) {
                            if (fieldid.length() > 0) {
                                fieldid.append(";");
                                mapcolumnid.append(";");
                            }
                            fieldid.append(primarycolumnid);
                            mapcolumnid.append(columnid);
                            ++blanks;
                            currentfieldmapped = true;
                        } else if (blanks == 1 && versioned) {
                            fieldid.append(";");
                            mapcolumnid.append(";");
                            mapcolumnid.append(columnid);
                            fieldid.append(";").append(versioncolumnid);
                            ++blanks;
                            currentversionmapped = true;
                        }
                    }
                    column.remove("mapfield");
                    column.setProperty("mode", mode);
                    column.setProperty("lumode", lumode);
                }
                pagedirectives.setProperty("columns", columns);
                if (fieldid.length() == 0) {
                    fieldid.append(primarycolumnid);
                    mapcolumnid.append("keycolid1");
                    if (versioned) {
                        fieldid.append(";").append(versioncolumnid);
                        mapcolumnid.append(";").append("keycolid2");
                    }
                } else {
                    if (!currentfieldmapped) {
                        fieldid.append(";").append(primarycolumnid);
                        mapcolumnid.append(";").append("keycolid1");
                    }
                    if (!currentversionmapped && versioned) {
                        fieldid.append(";").append(versioncolumnid);
                        mapcolumnid.append(";").append("keycolid2");
                    }
                }
            } else {
                fieldid.append(primarycolumnid);
                if (versioned) {
                    fieldid.append(";").append(versioncolumnid);
                }
                mapcolumnid.setLength(0);
            }
        }
        return pagedirectives;
    }

    private String getLookupIconHTML(PropertyList attributes, SDITagInfo sdiInfo, String data, boolean disabled, String displayStyle) {
        PropertyList lookuplink;
        String currentcolid = attributes.getProperty("columnid", "");
        String pdid = StringUtil.replaceAll(currentcolid.length() > 0 ? currentcolid : attributes.getProperty("diindex", "0"), ".", "_");
        StringBuffer html = new StringBuffer();
        String img = attributes.getProperty("img", "WEB-CORE/imageref/flat/16/flat_black_external_lookup1.svg");
        String imgtext = attributes.getProperty("imgtext");
        boolean usesapphirelookup = false;
        String sdcid = attributes.getProperty("sdcid", "");
        String lookupurl = attributes.getProperty("lookuppageid");
        if (lookupurl.length() > 0 && !lookupurl.contains("rc?")) {
            lookupurl = "rc?command=page&page=" + lookupurl;
        }
        if ((lookuplink = attributes.getPropertyList("lookuplink")) != null) {
            String hreftrim;
            String href = lookuplink.getProperty("href", "");
            if (href.length() > 0) {
                href = ElementUtil.evaluateExpression(attributes.getProperty("datasetname"), -1, currentcolid, href, sdiInfo);
            }
            if (!((hreftrim = href.trim().toLowerCase()).length() <= 0 || hreftrim.startsWith("rc?") || hreftrim.startsWith("http:") || hreftrim.startsWith("https:") || hreftrim.startsWith("javascript:"))) {
                href = "javascript:" + href;
            }
            lookupurl = href;
            if (sdcid.length() == 0) {
                sdcid = lookuplink.getProperty("sdcid", "");
            }
        }
        if (sdcid.length() > 0 || lookupurl.length() > 0) {
            JSONObject job;
            PropertyListCollection createnewoptions;
            PropertyList pagedirectives;
            String cachekey;
            String multiselect;
            Boolean pagedirectiverendered;
            String datasetcode = SDIData.getDatasetCode(data);
            if (attributes.getProperty("name").startsWith(datasetcode)) {
                attributes.setProperty("lookuppagedirectives", "oLUPD_" + datasetcode + "_" + pdid);
            } else {
                attributes.setProperty("lookuppagedirectives", "oLUPD_" + pdid);
            }
            if (this.pageContext != null) {
                pagedirectiverendered = (Boolean)this.pageContext.getAttribute("_lupd_" + datasetcode + "_" + pdid);
                if (pagedirectiverendered == null) {
                    pagedirectiverendered = Boolean.FALSE;
                }
            } else {
                pagedirectiverendered = null;
            }
            ArrayList columns = null;
            String selectortype = lookuplink == null ? "" : lookuplink.getProperty("selectortype", "");
            String lookupcallback = lookuplink == null ? "" : lookuplink.getProperty("lookupcallback", "");
            String lookupcallbackextra = lookuplink == null ? "" : lookuplink.getProperty("lookupcallbackextra", "");
            String restrictiveWhere = "";
            String defaultQuery = "";
            String queryFrom = "";
            String queryWhere = "";
            boolean isButton = false;
            if (lookuplink != null) {
                String tip;
                img = lookuplink.getProperty("img", img);
                isButton = lookuplink.getProperty("style", "image").equalsIgnoreCase("button");
                if ((sdcid = lookuplink.getProperty("sdcid", sdcid)).equals("[sdcid]") && this.pageContext != null && this.pageContext.getRequest().getParameter("sdcid") != null && this.pageContext.getRequest().getParameter("sdcid").length() > 0) {
                    sdcid = this.pageContext.getRequest().getParameter("sdcid");
                }
                if ((tip = lookuplink.getProperty("tip")).length() > 0) {
                    imgtext = tip;
                }
                if (lookuplink.getProperty("dialogtype").equalsIgnoreCase("Sapphire Dialog")) {
                    usesapphirelookup = true;
                }
                columns = lookuplink.getCollection("columns");
                selectortype = lookuplink.getProperty("selectortype", "");
                multiselect = selectortype.length() > 0 && selectortype.equalsIgnoreCase("checkbox") ? "Y" : "N";
                restrictiveWhere = lookuplink.getProperty("restrictivewhere", "");
                defaultQuery = lookuplink.getProperty("defaultquery", "");
                queryFrom = lookuplink.getProperty("queryfrom", "");
                queryWhere = lookuplink.getProperty("querywhere", "");
            } else {
                multiselect = "N";
            }
            boolean versioned = attributes.getProperty("versionedflag", "N").equals("Y");
            PropertyListCollection pd_columns = columns != null && columns.size() > 0 ? (PropertyListCollection)columns.clone() : new PropertyListCollection();
            StringBuffer mapcolumnid = new StringBuffer();
            StringBuffer fieldid = new StringBuffer();
            String curr_name = attributes.getProperty("name", "");
            String prefix = attributes.getProperty("rowindex").length() == 0 ? "" : attributes.getProperty("_prefix") + datasetcode + attributes.getProperty("rowindex") + "_";
            String curr_version = attributes.getProperty("sdccolumnid2", "").length() > 0 ? prefix + attributes.getProperty("sdccolumnid2") : "";
            if (lookupurl.length() == 0) {
                if (selectortype.trim().length() == 0) {
                    selectortype = "radiobutton";
                }
                lookupurl = "rc?command=file&file=WEB-OPAL/pagetypes/list/maintenance_list.jsp";
                cachekey = sdcid + curr_version + versioned + selectortype + usesapphirelookup + restrictiveWhere + defaultQuery + queryFrom + queryWhere + pd_columns + true + mapcolumnid + "_pagedirectives";
                PropertyList propertyList = pagedirectives = this.pageContext != null ? (PropertyList)this.pageContext.getAttribute(cachekey) : null;
                if (pagedirectives == null) {
                    pagedirectives = SDITagUtil.getLookupPageDirectives(sdcid, curr_name, curr_version, versioned, selectortype, usesapphirelookup, restrictiveWhere, defaultQuery, queryFrom, queryWhere, prefix, pd_columns, true, mapcolumnid, fieldid, this.tp, this.getSDCProcessor());
                    if (fieldid.toString().equals(curr_name) && this.pageContext != null) {
                        this.pageContext.setAttribute(cachekey, (Object)pagedirectives);
                        this.pageContext.setAttribute(cachekey + "mapcolumnid", (Object)mapcolumnid);
                        this.logger.debug(this.logName, "Cached pagedirectives cachekey: " + cachekey);
                    }
                } else {
                    fieldid.append(curr_name);
                    StringBuffer stringBuffer = mapcolumnid = this.pageContext != null ? (StringBuffer)this.pageContext.getAttribute(cachekey + "mapcolumnid") : new StringBuffer();
                }
                if (lookuplink != null && "Y".equals(lookuplink.getProperty("allowcreatenew")) && (createnewoptions = lookuplink.getCollection("createnewoptions")) != null && createnewoptions.size() > 0) {
                    for (int i = 0; i < createnewoptions.size(); ++i) {
                        PropertyList option = createnewoptions.getPropertyList(i);
                        pagedirectives.setProperty("createnewpageid", i == 0 ? option.getProperty("createnewpageid") : pagedirectives.getProperty("createnewpageid") + ";" + option.getProperty("createnewpageid"));
                        pagedirectives.setProperty("createnewtext", i == 0 ? option.getProperty("createnewtext") : pagedirectives.getProperty("createnewtext") + ";" + option.getProperty("createnewtext"));
                        pagedirectives.setProperty("templateid", i == 0 ? option.getProperty("templateid") : pagedirectives.getProperty("templateid") + ";" + option.getProperty("templateid"));
                    }
                }
            } else {
                cachekey = sdcid + curr_version + versioned + selectortype + usesapphirelookup + (restrictiveWhere + defaultQuery + queryFrom + queryWhere + pd_columns + false + mapcolumnid).hashCode() + "_pagedirectives";
                PropertyList propertyList = pagedirectives = this.pageContext != null ? (PropertyList)this.pageContext.getAttribute(cachekey) : null;
                if (pagedirectives == null) {
                    pagedirectives = SDITagUtil.getLookupPageDirectives(sdcid, curr_name, curr_version, versioned, selectortype, usesapphirelookup, restrictiveWhere, defaultQuery, queryFrom, queryWhere, prefix, pd_columns, false, mapcolumnid, fieldid, this.tp, this.getSDCProcessor());
                    pagedirectives.setProperty("sdcid", sdcid);
                    if (fieldid.toString().equals(curr_name) && this.pageContext != null) {
                        this.pageContext.setAttribute(cachekey, (Object)pagedirectives);
                        this.pageContext.setAttribute(cachekey + "mapcolumnid", (Object)mapcolumnid);
                        this.logger.debug(this.logName, "Cached pagedirectives cachekey: " + cachekey);
                    }
                } else {
                    fieldid.append(curr_name);
                    StringBuffer stringBuffer = mapcolumnid = this.pageContext != null ? (StringBuffer)this.pageContext.getAttribute(cachekey + "mapcolumnid") : new StringBuffer();
                }
                if (lookuplink != null && "Y".equals(lookuplink.getProperty("allowcreatenew")) && (createnewoptions = lookuplink.getCollection("createnewoptions")) != null && createnewoptions.size() > 0) {
                    for (int i = 0; i < createnewoptions.size(); ++i) {
                        PropertyList option = createnewoptions.getPropertyList(i);
                        pagedirectives.setProperty("createnewpageid", i == 0 ? option.getProperty("createnewpageid") : pagedirectives.getProperty("createnewpageid") + ";" + option.getProperty("createnewpageid"));
                        pagedirectives.setProperty("createnewtext", i == 0 ? option.getProperty("createnewtext") : pagedirectives.getProperty("createnewtext") + ";" + option.getProperty("createnewtext"));
                        pagedirectives.setProperty("templateid", i == 0 ? option.getProperty("templateid") : pagedirectives.getProperty("templateid") + ";" + option.getProperty("templateid"));
                    }
                }
            }
            attributes.setProperty("lookupfieldid", fieldid.toString());
            if (pagedirectives == null) {
                return "";
            }
            if (pagedirectiverendered != null && !pagedirectiverendered.booleanValue()) {
                String id = SDITagUtil.generateId(pagedirectives.getProperty("sdcid", "pageDirectives"), PAGE_DIRECTIVES_PREFIX, -1);
                pagedirectives.setProperty("pageDirId", id);
                job = pagedirectives.toJSONObject(false);
                job.remove("__propertylistid");
                job.remove("__propertylistsequence");
                this.pageContext.getSession().setAttribute(id, (Object)job.toString());
                html.append("<script>");
                html.append("var ").append(attributes.getProperty("lookuppagedirectives")).append("=").append(job.toString()).append(";");
                html.append("</script>");
                if (this.pageContext != null) {
                    this.pageContext.setAttribute("_lupd_" + datasetcode + "_" + pdid, (Object)Boolean.TRUE);
                }
            }
            StringBuffer script = new StringBuffer();
            if (!lookupurl.trim().toLowerCase().startsWith("javascript:")) {
                script.append("sapphire.lookup.sdi.open('").append(fieldid.toString()).append("','").append(sdcid).append("','','").append(multiselect).append("','','','','','");
                script.append(data).append("','").append(attributes.getProperty("rowindex")).append("'");
                script.append(",'").append(attributes.getProperty("columnid")).append("','").append(lookupurl).append("'");
                script.append(",'").append(mapcolumnid.toString()).append("'");
                script.append(",").append(usesapphirelookup).append("");
                if (pagedirectiverendered != null) {
                    script.append(",").append(attributes.getProperty("lookuppagedirectives"));
                } else {
                    job = pagedirectives.toJSONObject();
                    job.remove("__propertylistid");
                    job.remove("__propertylistsequence");
                    script.append(",").append(StringUtil.replaceAll(StringUtil.replaceAll(job.toString(), "'", "\\'"), "\"", "'"));
                }
                if (lookupcallback.length() > 0) {
                    script.append(",'").append(lookupcallback).append("'");
                }
                if (lookupcallbackextra.length() > 0) {
                    script.append(",'").append(lookupcallbackextra).append("'");
                }
                script.append(");");
            } else {
                if (isButton) {
                    lookupurl = lookupurl.substring(11);
                }
                script.append(lookupurl);
            }
            if (isButton) {
                Button btn = new Button(this.pageContext);
                btn.setId(attributes.getProperty("name") + "_img");
                btn.setText("");
                btn.setStyle("padding-left: 0px;padding-right: 0px;");
                btn.setImg(img);
                btn.setTip(imgtext.length() > 0 ? imgtext : "Lookup");
                btn.setAction(script.toString());
                html.append(btn.getHtml());
            } else {
                html.append("<a ");
                if (disabled) {
                    html.append("style=\"display:").append("none").append(";\" ");
                } else {
                    html.append("style=\"display:").append(displayStyle).append(";\" ");
                }
                html.append("displayStyle=\"").append(displayStyle).append("\" ");
                html.append("id=\"").append(attributes.getProperty("name")).append("_img\" href=\"#\" title=\"/").append(imgtext.length() > 0 ? imgtext : "Lookup").append("\" onClick=\"");
                html.append(script);
                html.append(script.length() > 0 && script.charAt(script.length() - 1) != ';' ? ";" : "").append("return false;");
                html.append("\" tabindex=\"0\">");
                String img_cssstyle = attributes.getProperty("img_cssstyle");
                html.append("<img title=\"").append(imgtext.length() > 0 ? imgtext : "Lookup").append("\" border=\"0\" src=\"").append(img).append("\" ").append(img_cssstyle.length() > 0 ? " class=\"" + img_cssstyle + "\"" : "").append(">");
                html.append("</a>");
            }
        } else if (attributes.getProperty("reftypeid").length() > 0) {
            if (img.length() > 0) {
                if (lookuplink != null && lookuplink.getProperty("dialogtype").equalsIgnoreCase("Sapphire Dialog")) {
                    usesapphirelookup = true;
                }
                html.append("<a style='display:").append(displayStyle).append(";'  href=\"#\" onClick=\"sapphire.lookup.reftype.open('").append(attributes.getProperty("name")).append("','").append(attributes.getProperty("reftypeid")).append("',").append(usesapphirelookup).append(");return false;\" tabindex=\"0\">");
                html.append("<img title=\"").append(imgtext.length() > 0 ? imgtext : "Lookup").append("\" border=\"0\" src=\"").append(img).append("\">");
                html.append("</a>");
            }
        } else {
            return "";
        }
        return html.toString();
    }

    public static String getInputAttributes(String type, String value, boolean readonly) {
        StringBuffer output = new StringBuffer();
        output.append("type=\"").append(type).append("\" ").append("value=\"").append(value.replaceAll("\"", "&#34;")).append("\" ").append(readonly ? "readonly " : "");
        return output.toString();
    }

    public static String getInputCommonAttributes(PropertyList attributes) {
        return SDITagUtil.getInputCommonAttributes(attributes, null, false);
    }

    public static String getFKInputAttributes(PropertyList attributes) {
        StringBuffer output = new StringBuffer();
        if (attributes.getProperty("fkmaster", "").length() > 0 || attributes.getProperty("fkmaster2", "").length() > 0) {
            output.append("data-fkmaster=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("fkmaster", ""))).append("\" ");
            if (attributes.getProperty("fkmaster2", "").length() > 0) {
                output.append("data-fkmaster2=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("fkmaster2", ""))).append("\" ");
            }
            output.append("data-fkkey=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("fkkey", ""))).append("\" ");
        } else if (attributes.getProperty("fklink", "").length() > 0) {
            output.append("data-fklink=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("fklink", ""))).append("\" ");
            output.append("data-fkkey=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("fkkey", ""))).append("\" ");
        }
        return output.toString();
    }

    public static String getNameAndIdAttributes(PropertyList attributes) {
        String name = attributes.containsKey("fkkey") && attributes.getProperty("fkkey").length() == 0 && attributes.getProperty("fkmaster").length() == 0 ? attributes.getProperty("name") + "RO" : attributes.getProperty("name");
        StringBuffer output = attributes.getProperty("nonameattribute", "N").equalsIgnoreCase("Y") ? new StringBuffer("id=\"" + SafeHTML.encodeForHTMLAttribute(attributes.getProperty("name")) + "\" ") : new StringBuffer("name=\"" + SafeHTML.encodeForHTMLAttribute(name) + "\" id=\"" + SafeHTML.encodeForHTMLAttribute(attributes.getProperty("name")) + "\" ");
        return output.toString();
    }

    public static String getInputCommonAttributes(PropertyList attributes, Browser browser, boolean html5) {
        String mandatory;
        StringBuffer output = new StringBuffer();
        output.append(SDITagUtil.getNameAndIdAttributes(attributes));
        output.append(SDITagUtil.getFKInputAttributes(attributes));
        if (attributes.getProperty("relatedcolumns", "").length() > 0) {
            output.append("data-relcols=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("relatedcolumns", ""))).append("\" ");
            output.append("data-relvals=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("relatedvalues", ""))).append("\" ");
        }
        if (attributes.getProperty("editorstyleid").length() > 0) {
            output.append("editorstyleid=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("editorstyleid"))).append("\" ");
        }
        if (attributes.getProperty("direction").equalsIgnoreCase("ltr") || attributes.getProperty("direction").equalsIgnoreCase("rtl")) {
            output.append("dir=\"").append(attributes.getProperty("direction")).append("\" ");
        }
        String string = mandatory = !attributes.getProperty("mode").equals("text") && attributes.getProperty("validation").length() > 0 && attributes.getProperty("validation").indexOf("Mandatory") >= 0 ? "mandatoryfield" : "";
        if (attributes.getProperty("class").indexOf("dataentry_grid") == 0) {
            output.append(attributes.getProperty("class").length() > 0 ? "class=\"" + attributes.getProperty("class") + "\" " : "class=\"" + mandatory + "\" ");
        } else {
            output.append(attributes.getProperty("class").length() > 0 ? "class=\"input_field " + attributes.getProperty("class") + (mandatory.length() > 0 ? " " + mandatory : "") + "\" " : "class=\"input_field " + mandatory + "\" ");
        }
        output.append(attributes.getProperty("align").length() > 0 ? "align=\"" + attributes.getProperty("align") + "\" " : "");
        output.append(attributes.getProperty("tip").length() > 0 ? "title=\"" + SafeHTML.encodeForHTMLAttribute(attributes.getProperty("tip")) + "\" " : "");
        if ((attributes.getProperty("validation").length() > 0 || attributes.getProperty("initialbehavior").length() > 0) && attributes.getProperty("title").length() > 0) {
            output.append("fieldlabel=\"" + SafeHTML.encodeForHTMLAttribute(attributes.getProperty("title")) + "\" ");
        }
        if ("Y".equals(attributes.getProperty("translatevalue"))) {
            output.append("trans=\"" + attributes.getProperty("translatevalue") + "\" ");
        }
        output.append(attributes.getProperty("autocomplete").length() > 0 ? "autocomplete=\"" + attributes.getProperty("autocomplete") + "\" " : "");
        output.append(attributes.getProperty("placeholder").length() > 0 ? "placeholder=\"" + attributes.getProperty("placeholder") + "\" " : "");
        output.append(attributes.getProperty("gridrow").length() > 0 ? "gridrow=\"" + attributes.getProperty("gridrow") + "\" " : "");
        output.append(attributes.getProperty("gridcol").length() > 0 ? "gridcol=\"" + attributes.getProperty("gridcol") + "\" " : "");
        output.append(attributes.getProperty("diindex").length() > 0 ? "diindex=\"" + attributes.getProperty("diindex") + "\" " : "");
        output.append(attributes.getProperty("style").length() > 0 ? "style=\"" + attributes.getProperty("style") + "\" " : "");
        output.append(attributes.getProperty("multiple").length() > 0 ? "multiple=\"" + attributes.getProperty("multiple") + "\" " : "");
        output.append(attributes.getProperty("size").length() > 0 ? "size=\"" + attributes.getProperty("size") + "\" " : "").append(attributes.getProperty("maxlen").length() > 0 ? "maxlength=\"" + attributes.getProperty("maxlen") + "\" " : "");
        if (attributes.getProperty("tabindex").length() > 0) {
            output.append("tabindex=\"").append(attributes.getProperty("tabindex")).append("\" ");
        } else if (attributes.getProperty("readonly").equals("true") && !attributes.getProperty("mode").equals("lookup") || attributes.getProperty("disabled").equals("true")) {
            output.append("tabindex=\"-1\" ");
        }
        output.append(SDITagUtil.getInputEventAttributes(attributes, true, browser, html5));
        output.append(SDITagUtil.getExtraAttributes(attributes));
        if (attributes.getProperty("mode").equalsIgnoreCase("datelookup")) {
            output.append("dateformat=\"").append(attributes.getProperty("dateformat")).append("\" ");
        }
        return output.toString();
    }

    private static void getRichtextEditorEvents(HTMLEditorControl rt, PropertyList attributes, boolean processChangeEvent) {
        String onchange = attributes.getProperty("onchange", "");
        if (onchange.length() > 0) {
            if (onchange.charAt(0) == '+') {
                rt.setEvent("changefield", HTMLEditorControl.Events.FIELDCHANGE, "function(event){" + (attributes.getProperty("mode").equalsIgnoreCase("data") ? "sdiSetRowDataEntry(event)" : "sdiSetRowUpdate(event)") + ";" + onchange.substring(1) + "}");
            } else if (onchange.charAt(onchange.length() - 1) == '+') {
                rt.setEvent("changefield", HTMLEditorControl.Events.FIELDCHANGE, "function(event){" + onchange.substring(0, onchange.length() - 1) + ";" + (attributes.getProperty("mode").equalsIgnoreCase("data") ? "sdiSetRowDataEntry(event)" : "sdiSetRowUpdate(event)") + "}");
            } else {
                rt.setEvent("changefield", HTMLEditorControl.Events.FIELDCHANGE, "function(event){" + onchange + "}");
            }
        } else if (processChangeEvent) {
            rt.setEvent("changefield", HTMLEditorControl.Events.FIELDCHANGE, "function(event){" + (attributes.getProperty("mode").equalsIgnoreCase("data") ? "sdiSetRowDataEntry(event)" : "sdiSetRowUpdate(event)") + "}");
        }
        rt.setEvent("changeeditor", HTMLEditorControl.Events.CHANGE, "function(event){htmlEditor.save(event.target)}");
    }

    public static String getInputEventAttributes(PropertyList attributes, boolean processChangeEvent) {
        return SDITagUtil.getInputEventAttributes(attributes, processChangeEvent, null, false);
    }

    public static String getInputEventAttributes(PropertyList attributes, boolean processChangeEvent, Browser browser, boolean html5) {
        String oninput;
        String onfocus;
        StringBuffer output = new StringBuffer(" ");
        output.append(attributes.getProperty("onkeydown").length() > 0 ? "onkeydown=\"" + attributes.getProperty("onkeydown") + "\" " : "").append(attributes.getProperty("onclick").length() > 0 ? "onclick=\"" + attributes.getProperty("onclick") + "\" " : "").append(attributes.getProperty("ondblclick").length() > 0 ? "ondblclick=\"" + attributes.getProperty("ondblclick") + "\" " : "").append(attributes.getProperty("onblur").length() > 0 ? "onblur=\"" + attributes.getProperty("onblur") + "\" " : "").append(attributes.getProperty("onkeypress").length() > 0 ? "onkeypress=\"" + attributes.getProperty("onkeypress") + "\" " : "").append(attributes.getProperty("onkeyup").length() > 0 ? "onkeyup=\"" + attributes.getProperty("onkeyup") + "\" " : "");
        String onchange = attributes.getProperty("onchange");
        if (attributes.getProperty("dependentcolumnlist") != null && attributes.getProperty("dependentcolumnlist").length() > 0) {
            String depcolumnlist = attributes.getProperty("dependentcolumnlist");
            StringBuilder depfieldid = new StringBuilder();
            String prefix = attributes.getProperty("fieldprefix");
            String rowIndex = attributes.getProperty("rowindex");
            if (rowIndex.length() == 0) {
                rowIndex = "-1";
            }
            String[] depcolumns = StringUtil.split(depcolumnlist, ";");
            for (int i = 0; i < depcolumns.length; ++i) {
                depfieldid.append(";" + (prefix.length() > 0 ? prefix + rowIndex + "_" : "") + depcolumns[i]);
            }
            String sqlcode = attributes.getProperty("dependentsqlcode");
            String dynamiccode = attributes.getProperty("dependentdynamiccode");
            onchange = "+lv_updateTargetFields( '" + attributes.getProperty("name") + "', " + rowIndex + ",'" + depfieldid.substring(1) + "', '" + sqlcode + "', '" + dynamiccode + "' );" + onchange;
        }
        String fullchange = "";
        if (onchange.length() > 0) {
            String string = fullchange = attributes.getProperty("mode").equalsIgnoreCase("data") ? "sdiSetRowDataEntry(event)" : "sdiSetRowUpdate(event)";
            if (onchange.charAt(0) == '+') {
                onchange = attributes.getProperty("mode").equalsIgnoreCase("data") ? "sdiSetRowDataEntry(event)" : "sdiSetRowUpdate(event);" + onchange.substring(1);
            } else if (onchange.charAt(onchange.length() - 1) == '+') {
                onchange = onchange.substring(0, onchange.length() - 1) + ";" + (attributes.getProperty("mode").equalsIgnoreCase("data") ? "sdiSetRowDataEntry(event)" : "sdiSetRowUpdate(event)");
            }
        } else if (processChangeEvent) {
            onchange = fullchange = attributes.getProperty("mode").equalsIgnoreCase("data") ? "sdiSetRowDataEntry(event)" : "sdiSetRowUpdate(event)";
        }
        if (onchange.length() > 0) {
            output.append("onchange=\"").append(onchange).append("\" ");
        }
        if ((onfocus = attributes.getProperty("onfocus")).length() > 0) {
            if (onfocus.charAt(0) == '+') {
                output.append("onfocus=\"").append(attributes.getProperty("mode").equalsIgnoreCase("data") ? "setInputCellFocus(this)" : "").append(";").append(onfocus.substring(1)).append("\" ");
            } else if (onfocus.charAt(onfocus.length() - 1) == '+') {
                output.append("onfocus=\"").append(onfocus.substring(0, onfocus.length() - 1)).append(";").append(attributes.getProperty("mode").equalsIgnoreCase("data") ? "setInputCellFocus(this)" : "").append("\" ");
            } else {
                output.append("onfocus=\"").append(onfocus).append("\" ");
            }
        } else if (processChangeEvent) {
            output.append("onfocus=\"").append(attributes.getProperty("mode").equalsIgnoreCase("data") ? "setInputCellFocus(this)" : "").append("\" ");
        }
        if (browser != null && (browser.isIE() && browser.getVersion() > 8.0 && html5 || browser.isWebkit())) {
            oninput = attributes.getProperty("oninput");
            if (oninput.length() > 0) {
                if (oninput.charAt(0) == '+') {
                    output.append("oninput=\"").append(attributes.getProperty("mode").equalsIgnoreCase("data") ? "sdiSetRowDataEntry(event)" : "sdiSetRowUpdate(event)").append(";").append(oninput.substring(1)).append("\" ");
                } else if (oninput.charAt(oninput.length() - 1) == '+') {
                    output.append("oninput=\"").append(oninput.substring(0, oninput.length() - 1)).append(";").append(attributes.getProperty("mode").equalsIgnoreCase("data") ? "sdiSetRowDataEntry(event)" : "sdiSetRowUpdate(event)").append("\" ");
                } else {
                    output.append("oninput=\"").append(oninput).append("\" ");
                }
            } else if (fullchange.length() > 0) {
                output.append("oninput=\"").append(fullchange).append("\" ");
            }
        } else if (browser != null && browser.isIE() && (browser.getVersion() < 9.0 || !html5)) {
            oninput = attributes.getProperty("onpropertychange");
            if (oninput.length() > 0) {
                if (oninput.charAt(0) == '+') {
                    output.append("onpropertychange=\"if(_isTVC(event)){").append(attributes.getProperty("mode").equalsIgnoreCase("data") ? "sdiSetRowDataEntry(event)" : "sdiSetRowUpdate(event)").append(";").append(oninput.substring(1)).append("}\" ");
                } else if (oninput.charAt(oninput.length() - 1) == '+') {
                    output.append("onpropertychange=\"if(_isTVC(event)){").append(oninput.substring(0, oninput.length() - 1)).append(";").append(attributes.getProperty("mode").equalsIgnoreCase("data") ? "sdiSetRowDataEntry(event)" : "sdiSetRowUpdate(event)").append("}\" ");
                } else {
                    output.append("onpropertychange=\"if(_isTVC(event)){").append(oninput).append("}\" ");
                }
            } else if (fullchange.length() > 0) {
                output.append("onpropertychange=\"if(_isTVC(event)){").append(fullchange).append("}\" ");
            }
        }
        return output.toString();
    }

    private static String getExtraAttributes(PropertyList attributes) {
        StringBuffer output = new StringBuffer("");
        if (attributes.getProperty("extraattributes").length() > 0) {
            String[] extraattributes = StringUtil.split(attributes.getProperty("extraattributes"), ";");
            for (int i = 0; i < extraattributes.length; ++i) {
                String[] attr = StringUtil.split(extraattributes[i], "=");
                if (attr.length != 2) continue;
                output.append(attr[0]).append("=\"").append(SafeHTML.encodeForHTMLAttribute(attr[1])).append("\" ");
            }
        }
        return output.toString();
    }

    public String getDisplayValueSelect(PropertyList attributes) {
        String output;
        boolean valueFound = false;
        String[] displays = StringUtil.split(attributes.getProperty("displayvalue"), ";");
        if (displays != null && displays.length > 0) {
            StringBuffer select = new StringBuffer("<select " + (attributes.getProperty("readonly").equals("true") || attributes.getProperty("disabled").equals("true") ? "disabled " : "") + SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5) + " oldvalue=\"" + attributes.getProperty("value") + "\">");
            boolean displayempty = true;
            StringBuilder temp = new StringBuilder();
            for (int i = 0; i < displays.length; ++i) {
                String display = displays[i].trim();
                int pos = (display = StringUtil.replaceAll(display, "#semicolon#", ";")).indexOf("=");
                if (pos <= -1) continue;
                if (display.equalsIgnoreCase("=__!HIDEDEFAULT!__")) {
                    displayempty = false;
                    continue;
                }
                String value = display.substring(0, pos);
                value = StringUtil.replaceAll(value, "[equals]", "=");
                if (valueFound || attributes.getProperty("value").equals(value)) {
                    valueFound = true;
                }
                String dvalue = display.substring(pos + 1).trim();
                dvalue = StringUtil.replaceAll(dvalue, "[equals]", "=");
                if ("Y".equals(attributes.getProperty("translatevalue"))) {
                    dvalue = this.tp.translate(dvalue);
                }
                temp.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(value)).append("\"").append(attributes.getProperty("value").equals(value) ? " selected>" : ">").append(SafeHTML.encodeForHTML(dvalue)).append("</option>");
            }
            if (!valueFound && attributes.getProperty("value") != null && attributes.getProperty("value").length() > 0) {
                String dvalue = attributes.getProperty("value");
                if ("Y".equals(attributes.getProperty("translatevalue"))) {
                    dvalue = this.tp.translate(dvalue);
                }
                if ("dropdownlistvalidated".equals(attributes.getProperty("mode"))) {
                    temp.append("<option style=\"background-color: red;\" value=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("value"))).append("\"").append(" selected>?").append(SafeHTML.encodeForHTML(dvalue)).append("?</option>");
                } else {
                    temp.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("value"))).append("\"").append(" selected>").append(SafeHTML.encodeForHTML(dvalue)).append("</option>");
                }
            }
            if (displayempty) {
                select.append("<option value=\"\"></option>");
            }
            select.append(temp.toString());
            select.append("</select>");
            output = select.toString();
        } else {
            output = "TAG ERROR: No display values found.";
        }
        return output;
    }

    public String getDropdownValuesSelect(PropertyList attributes) {
        String output;
        boolean valueFound = false;
        String[] dropdownvalues = StringUtil.split(attributes.getProperty("dropdownvalues"), ";");
        if (dropdownvalues != null && dropdownvalues.length > 0) {
            StringBuffer select = new StringBuffer("<select " + (attributes.getProperty("readonly").equals("true") || attributes.getProperty("disabled").equals("true") ? "disabled " : "") + SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5) + " oldvalue=\"" + attributes.getProperty("value") + "\"><option value=\"\"></option>");
            for (int i = 0; i < dropdownvalues.length; ++i) {
                String value = dropdownvalues[i].trim();
                if (valueFound || attributes.getProperty("value").equals(value)) {
                    valueFound = true;
                }
                String dvalue = value;
                if ("Y".equals(attributes.getProperty("translatevalue"))) {
                    dvalue = this.tp.translate(dvalue);
                }
                select.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(value)).append("\"").append(attributes.getProperty("value").equals(value) ? " selected>" : ">").append(SafeHTML.encodeForHTML(dvalue)).append("</option>");
            }
            if (!valueFound && attributes.getProperty("value") != null && attributes.getProperty("value").length() > 0) {
                String dvalue = attributes.getProperty("value");
                if ("Y".equals(attributes.getProperty("translatevalue"))) {
                    dvalue = this.tp.translate(dvalue);
                }
                if ("dropdownlistvalidated".equals(attributes.getProperty("mode"))) {
                    select.append("<option style=\"background-color: red;\" value=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("value"))).append("\"").append(" selected>?").append(SafeHTML.encodeForHTML(dvalue)).append("?</option>");
                } else {
                    select.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("value"))).append("\"").append(" selected>").append(SafeHTML.encodeForHTML(dvalue)).append("</option>");
                }
            }
            select.append("</select>");
            output = select.toString();
        } else {
            output = "TAG ERROR: No display values found.";
        }
        return output;
    }

    public String getSqlSelect(PropertyList attributes, SDITagInfo sdiInfo) {
        String output;
        boolean valueFound = false;
        DataSet ds = this.getSqlDataSet(attributes, sdiInfo);
        if (ds != null) {
            String[] columns = ds.getColumns();
            StringBuffer select = new StringBuffer("<select " + (attributes.getProperty("readonly").equals("true") || attributes.getProperty("disabled").equals("true") ? "disabled " : "") + SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5) + " oldvalue=\"" + attributes.getProperty("value") + "\"><option value=\"\"></option>");
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String value = ds.getValue(i, columns[0]);
                if (valueFound || attributes.getProperty("value").equals(value)) {
                    valueFound = true;
                }
                String displayvalue = value;
                if (columns.length >= 2) {
                    displayvalue = ds.getString(i, columns[1], value);
                }
                if ("Y".equals(attributes.getProperty("translatevalue"))) {
                    displayvalue = this.tp.translate(displayvalue);
                }
                select.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(value)).append("\"").append(attributes.getProperty("value").equals(value) ? " selected>" : ">").append(SafeHTML.encodeForHTML(displayvalue)).append("</option>");
            }
            if (!valueFound && attributes.getProperty("value") != null && attributes.getProperty("value").length() > 0) {
                String dvalue = attributes.getProperty("value");
                if ("Y".equals(attributes.getProperty("translatevalue"))) {
                    dvalue = this.tp.translate(dvalue);
                }
                if ("dropdownlistvalidated".equals(attributes.getProperty("mode"))) {
                    select.append("<option style=\"background-color: red;\" value=\"").append(SafeHTML.encodeForHTMLAttribute(dvalue)).append("\"").append(" selected>?").append(SafeHTML.encodeForHTML(dvalue)).append("?</option>");
                } else {
                    select.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(dvalue)).append("\"").append(" selected>").append(SafeHTML.encodeForHTML(dvalue)).append("</option>");
                }
            }
            select.append("</select>");
            output = select.toString();
        } else {
            output = "TAG ERROR: SQL data could not be retrieved.";
        }
        return output;
    }

    public String getRefTypeSelect(PropertyList attributes, SDITagInfo sdiInfo) {
        String output;
        DataSet ds = this.getRefTypeData(attributes, sdiInfo);
        boolean translatevalue = "Y".equals(attributes.getProperty("translatevalue", "Y"));
        if (translatevalue) {
            attributes.setProperty("translatevalue", "Y");
        }
        if (ds != null) {
            String value;
            StringBuffer select = new StringBuffer("<select " + (attributes.getProperty("readonly").equals("true") || attributes.getProperty("disabled").equals("true") ? "disabled " : "") + SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5) + " oldvalue=\"" + SafeHTML.encodeForHTMLAttribute(attributes.getProperty("value")) + "\"><option value=\"\"></option>");
            if (attributes.getProperty("dataentrymode").equals("dropdownlist") && attributes.getProperty("readonly").equals("true")) {
                String releasedfield = "document.getElementById('" + attributes.getProperty("name").substring(0, attributes.getProperty("name").indexOf("_")) + "_releasedflag')";
                select = new StringBuffer("<span onmousedown=\"if(this.children[0].released=='Y' && ( " + releasedfield + "== null || " + releasedfield + ".value=='Y') ){this.children[0].disabled=true;}\" onmouseup=\"this.children[0].disabled=false;\">");
                select.append("<select ").append(attributes.getProperty("readonly").equals("true") ? "readonly='true' onFocus=this.blur();" : "").append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5)).append("><option value=\"\"></option>");
            }
            boolean valueexist = (value = attributes.getProperty("value", "")).length() == 0;
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String dvalue;
                String refvalueid = ds.getString(i, "refvalueid");
                String string = dvalue = ds.getString(i, "refdisplayvalue") != null && ds.getString(i, "refdisplayvalue").length() > 0 ? ds.getString(i, "refdisplayvalue") : SDITagUtil.getDisplayValue(refvalueid, attributes.getProperty("displayvalue"));
                if (translatevalue) {
                    dvalue = this.tp.translate(dvalue, this.tp.getLanguage(), ds.getString(i, "reftypeid"));
                }
                if (refvalueid.equals(value)) {
                    select.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(ds.getString(i, "refvalueid"))).append("\"").append(" selected>").append(SafeHTML.encodeForHTML(dvalue)).append("</option>");
                    valueexist = true;
                    continue;
                }
                select.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(ds.getString(i, "refvalueid"))).append("\"").append(">").append(SafeHTML.encodeForHTML(dvalue)).append("</option>");
            }
            if (!valueexist) {
                if ("dropdownlistvalidated".equals(attributes.getProperty("mode"))) {
                    select.append("<option style=\"background-color: red;\" value=\"").append(SafeHTML.encodeForHTMLAttribute(value)).append("\"").append(" selected>?").append(SafeHTML.encodeForHTML(translatevalue ? this.tp.translate(value) : value)).append("?</option>");
                } else {
                    select.append("<option value=\"").append(SafeHTML.encodeForHTMLAttribute(value)).append("\"").append(" selected>").append(SafeHTML.encodeForHTML(translatevalue ? this.tp.translate(value) : value)).append("</option>");
                }
            }
            select.append("</select>");
            if (select.indexOf("<span") == 0) {
                select.append("</span>");
            }
            output = select.toString();
        } else {
            output = "TAG ERROR: Reference data could not be retrieved.";
        }
        return output;
    }

    private String getRefTypeDisplayValue(PropertyList attributes, SDITagInfo sdiInfo) {
        StringBuffer output = new StringBuffer();
        DataSet ds = this.getRefTypeData(attributes, sdiInfo);
        if (ds != null) {
            String value = attributes.getProperty("value", "");
            boolean valueexist = value.length() == 0;
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String dvalue;
                String refvalueid = ds.getString(i, "refvalueid");
                String string = dvalue = ds.getString(i, "refdisplayvalue") != null && ds.getString(i, "refdisplayvalue").length() > 0 ? ds.getString(i, "refdisplayvalue") : SDITagUtil.getDisplayValue(refvalueid, attributes.getProperty("displayvalue"));
                if ("Y".equals(attributes.getProperty("translatevalue"))) {
                    dvalue = this.tp.translate(dvalue, this.tp.getLanguage(), ds.getString(i, "reftypeid"));
                }
                if (output.length() > 0) {
                    output.append(";");
                }
                output.append(refvalueid).append("=").append(dvalue);
            }
        }
        return output.toString();
    }

    public String getRefTypeCheckbox(PropertyList attributes, SDITagInfo sdiInfo) {
        String[] values;
        Browser b;
        String output = "";
        String value1 = "";
        String value2 = "";
        Browser browser = b = this.pageContext != null ? new Browser(this.pageContext) : null;
        if (attributes.getProperty("reftypeid").length() > 0) {
            DataSet ds = this.getRefTypeData(attributes, sdiInfo);
            if (ds != null && ds.size() > 0) {
                if (ds != null && ds.getRowCount() != 2) {
                    return this.getRefTypeSelect(attributes, sdiInfo);
                }
                value1 = ds.getString(0, "refvalueid");
                value2 = ds.getString(1, "refvalueid");
            } else {
                output = "TAG ERROR: Reference data could not be retrieved.";
            }
        } else if (attributes.getProperty("refvalues").length() > 0) {
            values = StringUtil.split(attributes.getProperty("refvalues"), ";");
            if (values != null && values.length == 2) {
                value1 = values[0];
                value2 = values[1];
            } else {
                output = "TAG ERROR: refvalues not defined correctly.";
            }
        } else if (attributes.getProperty("displayvalue").length() > 0) {
            values = StringUtil.split(attributes.getProperty("displayvalue"), ";");
            if (values != null && values.length == 2) {
                value1 = values[0].substring(0, values[0].indexOf("=") < 0 ? values[0].length() : values[0].indexOf("=")).trim();
                value2 = values[1].substring(0, values[1].indexOf("=") < 0 ? values[1].length() : values[1].indexOf("=")).trim();
            } else {
                output = "TAG ERROR: displayvalue not defined correctly.";
            }
        } else {
            output = "TAG ERROR: Reference data not defined correctly.";
        }
        if (output.length() == 0) {
            StringBuffer select = new StringBuffer();
            String id = attributes.getProperty("name");
            select.append("<input style=\"display:none\" type=\"text\" ").append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5)).append(" value=\"").append(attributes.getProperty("value")).append("\" />");
            String checked = attributes.getProperty("value").equals(value1) ? " checked " : "";
            String onclick = attributes.getProperty("onclick");
            boolean notSDITag = attributes.getProperty("notsditag", "false").equals("true");
            if (onclick.length() > 0) {
                attributes.setProperty("onclick", "setCheckBoxFieldValue('" + attributes.getProperty("name") + "','" + value1 + "','" + value2 + "'" + (notSDITag ? ", false" : "") + ");setCheckBoxValue('" + attributes.getProperty("name") + "','" + value1 + "','" + value2 + "'" + (notSDITag ? ", false" : "") + ");" + onclick);
            } else {
                attributes.setProperty("onclick", "setCheckBoxFieldValue('" + attributes.getProperty("name") + "','" + value1 + "','" + value2 + "'" + (notSDITag ? ", false" : "") + ");setCheckBoxValue('" + attributes.getProperty("name") + "','" + value1 + "','" + value2 + "'" + (notSDITag ? ", false" : "") + ");");
            }
            select.append("<input ");
            if (this.browser != null && this.browser.isIE() && this.browser.getVersion() > 10.0) {
                select.append("style=\"width:auto;\" ");
            }
            select.append("id=\"").append(id).append("_chx\" value=\"").append(attributes.getProperty("value")).append("\" type=\"checkbox\"").append(checked).append(attributes.getProperty("readonly").equals("true") || attributes.getProperty("disabled").equals("true") ? "disabled " : "").append(SDITagUtil.getInputEventAttributes(attributes, false, this.browser, this.html5)).append(SDITagUtil.getExtraAttributes(attributes)).append("/>");
            output = select.toString();
        }
        return output;
    }

    public String getSDCRadiobutton(PropertyList attributes, SDITagInfo sdiInfo) {
        String output;
        try {
            DataSet ds = this.getSDCSelectData(attributes, sdiInfo).getDataset("primary");
            output = this.getRadioButtonFromDS(attributes, ds);
        }
        catch (Exception e) {
            output = "ERROR:" + e.getMessage();
        }
        return output;
    }

    public String getSqlRadiobutton(PropertyList attributes, SDITagInfo sdiInfo) {
        DataSet ds = this.getSqlDataSet(attributes, sdiInfo);
        String output = ds != null ? this.getRadioButtonFromDS(attributes, ds) : "Error retrieving data using sql:" + attributes.getProperty("sql");
        return output;
    }

    private String getRadioButtonFromDS(PropertyList attributes, DataSet ds) {
        String colid1 = ds.getColumnId(0);
        String colid2 = ds.getColumns().length > 1 ? ds.getColumnId(1) : colid1;
        boolean isRefType = false;
        if (ds.getColumnId(0).equals("reftypeid")) {
            colid1 = "refvalueid";
            colid2 = "refdisplayvalue";
            isRefType = true;
        }
        String style = attributes.getProperty("mode").indexOf("vertical") > 0 ? "radio_vertical" : "radio_horizontal";
        String onchange = "";
        if (attributes.getProperty("dependentcolumnlist") != null && attributes.getProperty("dependentcolumnlist").length() > 0) {
            String depcolumnlist = attributes.getProperty("dependentcolumnlist");
            StringBuilder depfieldid = new StringBuilder();
            String prefix = attributes.getProperty("fieldprefix");
            String rowIndex = attributes.getProperty("rowindex");
            if (rowIndex.length() == 0) {
                rowIndex = "-1";
            }
            String[] depcolumns = StringUtil.split(depcolumnlist, ";");
            for (int i = 0; i < depcolumns.length; ++i) {
                depfieldid.append(";" + (prefix.length() > 0 ? prefix + rowIndex + "_" : "") + depcolumns[i]);
            }
            String sqlcode = attributes.getProperty("dependentsqlcode");
            String dynamiccode = attributes.getProperty("dependentdynamiccode");
            onchange = "lv_updateTargetFields( '" + attributes.getProperty("name") + "', " + rowIndex + ",'" + depfieldid.substring(1) + "', '" + sqlcode + "', '" + dynamiccode + "' )";
        }
        StringBuffer input = new StringBuffer();
        String fieldid = attributes.getProperty("name");
        input.append("<div id=\"").append(fieldid).append("_radio\" ");
        input.append(onchange.length() > 0 ? " onchange=\"" + onchange + "\" " : "");
        if (attributes.getProperty("class").length() == 0 && attributes.getProperty("validation").length() > 0 && attributes.getProperty("validation").indexOf("Mandatory") >= 0) {
            input.append("class=\"maint_radiofield mandatoryfield\" fieldlabel=\"" + SafeHTML.encodeForHTMLAttribute(attributes.getProperty("title")) + "\" ");
        } else if (attributes.getProperty("class").length() > 0) {
            input.append("class=\"maint_radiofield " + attributes.getProperty("class") + "\" ");
        } else {
            input.append("class=\"maint_radiofield\" ");
        }
        input.append(" value=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("value"))).append("\" oldvalue=\"").append(SafeHTML.encodeForHTMLAttribute(attributes.getProperty("value"))).append("\" onBlur=\"setRadioFieldValue('").append(fieldid).append("');\">");
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String dvalue;
            input.append("<div class=\"").append(style).append("\">");
            input.append("<input type=\"radio\" ").append(attributes.getProperty("readonly").equals("true") || attributes.getProperty("disabled").equals("true") ? "disabled " : "");
            input.append(" name=\"").append(fieldid).append("\" ");
            input.append(" id=\"").append(fieldid).append("_").append(i).append("\" ");
            input.append(" onclick=\"setDivValue('").append(fieldid).append("');").append(attributes.getProperty("onclick")).append("\" ");
            input.append(attributes.getProperty("onchange").length() > 0 ? "onchange=\"" + attributes.getProperty("onchange") + "\" " : "");
            input.append(" value=\"").append(SafeHTML.encodeForHTMLAttribute(ds.getString(i, colid1))).append("\"").append(attributes.getProperty("value").equals(ds.getString(i, colid1)) ? " checked>" : ">");
            String string = dvalue = ds.getString(i, colid2) != null && ds.getString(i, colid2).length() > 0 ? ds.getString(i, colid2) : ds.getString(i, colid1);
            if (isRefType && !"N".equals(attributes.getProperty("translatevalue"))) {
                dvalue = this.tp.translate(dvalue);
            }
            input.append(SafeHTML.encodeForHTML(dvalue)).append("");
            input.append("</div>");
        }
        input.append("</div>");
        return input.toString();
    }

    public String getRefTypeRadiobutton(PropertyList attributes, SDITagInfo sdiInfo) {
        DataSet ds = this.getRefTypeData(attributes, sdiInfo);
        String output = ds != null ? this.getRadioButtonFromDS(attributes, ds) : "TAG ERROR: Reference data could not be retrieved.";
        return output;
    }

    public String getDisplayValueRadioButton(PropertyList attributes) {
        String output;
        boolean valueFound = false;
        String[] displays = StringUtil.split(attributes.getProperty("displayvalue"), ";");
        if (displays != null && displays.length > 0) {
            String fieldid = attributes.getProperty("name");
            StringBuffer input = new StringBuffer();
            String onchange = "";
            if (attributes.getProperty("dependentcolumnlist") != null && attributes.getProperty("dependentcolumnlist").length() > 0) {
                String depcolumnlist = attributes.getProperty("dependentcolumnlist");
                StringBuilder depfieldid = new StringBuilder();
                String prefix = attributes.getProperty("fieldprefix");
                String rowIndex = attributes.getProperty("rowindex");
                if (rowIndex.length() == 0) {
                    rowIndex = "-1";
                }
                String[] depcolumns = StringUtil.split(depcolumnlist, ";");
                for (int i = 0; i < depcolumns.length; ++i) {
                    depfieldid.append(";" + (prefix.length() > 0 ? prefix + rowIndex + "_" : "") + depcolumns[i]);
                }
                String sqlcode = attributes.getProperty("dependentsqlcode");
                String dynamiccode = attributes.getProperty("dependentdynamiccode");
                onchange = "lv_updateTargetFields( '" + attributes.getProperty("name") + "', " + rowIndex + ",'" + depfieldid.substring(1) + "', '" + sqlcode + "', '" + dynamiccode + "' )";
            }
            input.append("<div id=\"").append(fieldid).append("_radio\" ");
            input.append(onchange.length() > 0 ? " onchange=\"" + onchange + "\" " : "");
            input.append(attributes.getProperty("class").length() == 0 && attributes.getProperty("validation").length() > 0 && attributes.getProperty("validation").indexOf("Mandatory") >= 0 ? "class=\"maint_radiofield mandatoryfield\" fieldlabel=\"" + attributes.getProperty("title") + "\" " : "");
            input.append(attributes.getProperty("class").length() > 0 ? "class=\"maint_radiofield " + attributes.getProperty("class") + "\" " : " class=\"maint_radiofield\"");
            input.append(" value=\"").append(attributes.getProperty("value")).append("\" oldvalue=\"").append(attributes.getProperty("value")).append("\" onBlur=\"setRadioFieldValue('").append(fieldid).append("');\">");
            String style = attributes.getProperty("mode").indexOf("vertical") > 0 ? "radio_vertical" : "radio_horizontal";
            for (int i = 0; i < displays.length; ++i) {
                input.append("<div class=\"").append(style).append("\">");
                String display = displays[i].trim();
                int pos = display.indexOf("=");
                if (pos > -1) {
                    String value = display.substring(0, pos);
                    if (valueFound || attributes.getProperty("value").equals(value)) {
                        valueFound = true;
                    }
                    input.append("<input type=\"radio\" ").append(attributes.getProperty("readonly").equals("true") || attributes.getProperty("disabled").equals("true") ? "disabled " : "");
                    input.append(" name=\"").append(fieldid).append("\" ");
                    input.append(" id=\"").append(fieldid).append("_").append(i).append("\" ");
                    input.append(" onclick=\"setDivValue('").append(fieldid).append("');").append(attributes.getProperty("onclick")).append("\" ");
                    input.append(attributes.getProperty("onchange").length() > 0 ? "onchange=\"" + attributes.getProperty("onchange") + "\" " : "");
                    input.append(" value=\"").append(value).append("\" ").append(attributes.getProperty("value").equals(value) ? " checked>" : ">");
                    String dvalue = display.substring(pos + 1).trim();
                    if (attributes.getProperty("translatevalue").indexOf("Y") == 0) {
                        dvalue = this.tp.translate(dvalue);
                    }
                    input.append("<label for=\"").append(fieldid).append("_").append(i).append("\">").append(dvalue).append("</label>").append("");
                }
                input.append("</div>");
            }
            if (!valueFound && attributes.getProperty("value") != null && attributes.getProperty("value").length() > 0) {
                input.append("<div class=\"").append(style).append("\">");
                input.append("<input type=\"radio\" ").append(attributes.getProperty("readonly").equals("true") || attributes.getProperty("disabled").equals("true") ? "disabled " : "").append(SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5));
                input.append(" value=\"").append(attributes.getProperty("value")).append("\" checked>");
                String dvalue = attributes.getProperty("value");
                if (attributes.getProperty("translatevalue").indexOf("Y") == 0) {
                    dvalue = this.tp.translate(dvalue);
                }
                input.append(dvalue);
                input.append("</div>");
            }
            input.append("</div>");
            output = input.toString();
        } else {
            output = "TAG ERROR: No display values found.";
        }
        return output;
    }

    private DataSet getSqlDataSet(PropertyList attributes, SDITagInfo sdiInfo) {
        String dsn = attributes.getProperty("datasetname");
        String sql = attributes.getProperty("sql");
        if (sdiInfo != null && dsn.length() > 0 && sdiInfo.getDataSet(dsn) != null && sql.contains("[") && sql.contains("]")) {
            sql = ElementUtil.evaluateExpression(dsn, -1, attributes.getProperty("columnid"), sql, sdiInfo);
        } else if (sql.indexOf("[currentuser]") > 0) {
            String userid = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId();
            sql = StringUtil.replaceAll(sql, "[currentuser]", userid);
        }
        DataSet ds = (DataSet)sdiInfo.getChildTagData("SqlData:" + sql);
        if (ds == null) {
            QueryProcessor qp = this.getQueryProcessor();
            ds = qp.getSqlDataSet(sql);
            sdiInfo.setChildTagData("SqlData:" + sql, ds);
        }
        return ds;
    }

    public DataSet getRefTypeData(PropertyList attributes, SDITagInfo sdiInfo) {
        DataSet ds = (DataSet)sdiInfo.getChildTagData("RefTypeData:" + attributes.getProperty("reftypeid"));
        if (ds == null) {
            QueryProcessor qp = this.getQueryProcessor();
            ds = qp.getRefTypeDataSet(attributes.getProperty("reftypeid"));
            sdiInfo.setChildTagData("RefTypeData:" + attributes.getProperty("reftypeid"), ds);
        }
        return ds;
    }

    private SDIData getSDCSelectData(PropertyList attributes, SDITagInfo sdiInfo) throws Exception {
        SDIData sdidata;
        String queryWhere = attributes.getProperty("querywhere");
        String dsn = attributes.getProperty("datasetname");
        if (sdiInfo != null && dsn.length() > 0 && sdiInfo.getDataSet(dsn) != null && queryWhere.contains("[") && queryWhere.contains("]")) {
            queryWhere = ElementUtil.evaluateExpression(dsn, -1, attributes.getProperty("columnid"), queryWhere, sdiInfo);
        }
        if ((sdidata = (SDIData)sdiInfo.getChildTagData("SDCData:" + attributes.getProperty("sdcid") + ":" + queryWhere)) == null) {
            SDIProcessor sdilist = this.getSDIProcessor();
            SDIRequest sdirequest = new SDIRequest();
            sdirequest.setSDCid(attributes.getProperty("sdcid"));
            String orderby = "";
            if (attributes.getProperty("queryfrom") != null && attributes.getProperty("queryfrom").length() > 0) {
                sdirequest.setQueryFrom(attributes.getProperty("queryfrom"));
            } else {
                SDCProcessor sdcProcessor = this.getSDCProcessor();
                String tableid = (String)sdcProcessor.getSDCProperties(attributes.getProperty("sdcid")).get("tableid");
                sdirequest.setQueryFrom(tableid);
                orderby = (String)sdcProcessor.getSDCProperties(attributes.getProperty("sdcid")).get("keycolid1");
            }
            sdirequest.setQueryWhere(queryWhere);
            sdirequest.setQueryOrderBy(attributes.getProperty("queryorderby", orderby));
            if (attributes.getProperty("valuecolumn").length() > 0) {
                sdirequest.setRequestItem("primary[" + attributes.getProperty("valuecolumn") + (attributes.getProperty("displaycolumn").length() > 0 ? "," + attributes.getProperty("displaycolumn") : "") + "]");
            } else {
                sdirequest.setRequestItem("primary");
            }
            sdirequest.setShowTemplates(true);
            sdidata = sdilist.getSDIData(sdirequest);
            if (sdidata != null) {
                sdiInfo.setChildTagData("SDCData:" + attributes.getProperty("sdcid") + ":" + queryWhere, sdidata);
            } else {
                String error = LogUtil.getStackTraceMessages(sdilist.getLastException(), "<br/>", true, true);
                throw new Exception(error);
            }
        }
        return sdidata;
    }

    public String getSDCSelect(PropertyList attributes, SDITagInfo sdiInfo) {
        String output;
        try {
            DataSet ds;
            boolean translatevalue;
            SDIData sdidata = this.getSDCSelectData(attributes, sdiInfo);
            boolean bl = translatevalue = this.tp != null && !"N".equals(attributes.getProperty("translatevalue"));
            if (translatevalue) {
                attributes.setProperty("translatevalue", "Y");
            }
            if ((ds = sdidata.getDataset("primary")) != null) {
                String[] keycols = sdidata.getKeys("primary");
                StringBuffer select = new StringBuffer("<select " + (attributes.getProperty("readonly").equals("Y") || attributes.getProperty("readonly").equals("true") || attributes.getProperty("disabled").equals("true") ? "disabled " : "") + SDITagUtil.getInputCommonAttributes(attributes, this.browser, this.html5) + " oldvalue=\"" + attributes.getProperty("value") + "\"><option value=\"\"></option>");
                String value = attributes.getProperty("value", "");
                boolean valueexist = value.length() == 0;
                boolean hasDisplayColumn = attributes.getProperty("displaycolumn").length() > 0;
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    String displayvalue;
                    String keyvalue = ds.getString(i, keycols[0]);
                    String string = displayvalue = hasDisplayColumn && ds.getValue(i, attributes.getProperty("displaycolumn")).length() > 0 ? ds.getValue(i, attributes.getProperty("displaycolumn")) : keyvalue;
                    if (value.equals(keyvalue)) {
                        valueexist = true;
                        select.append("<option value=\"").append(keyvalue).append("\" selected>").append(SafeHTML.encodeForHTML(translatevalue ? this.tp.translate(displayvalue) : displayvalue)).append("</option>");
                        continue;
                    }
                    select.append("<option value=\"").append(keyvalue).append("\">").append(SafeHTML.encodeForHTML(translatevalue ? this.tp.translate(displayvalue) : displayvalue)).append("</option>");
                }
                if (!valueexist) {
                    String queryfrom;
                    String displayvalue = value;
                    String valuecolumn = attributes.getProperty("valuecolumn");
                    String displaycolumn = attributes.getProperty("displaycolumn").trim();
                    if (displaycolumn.length() > 0 && !displaycolumn.equals(valuecolumn) && (queryfrom = attributes.getProperty("queryfrom")).length() > 0) {
                        DataSet displayds;
                        String queryorderby = attributes.getProperty("queryorderby");
                        String querywhere = valuecolumn + " = '" + value + "'";
                        String sql = "select " + displaycolumn + " from " + queryfrom + " where " + querywhere;
                        if (queryorderby != null && queryorderby.length() > 0) {
                            sql = sql + " order by " + queryorderby;
                        }
                        if ((displayds = this.getQueryProcessor().getSqlDataSet(sql)) != null && displayds.size() > 0) {
                            displayvalue = displayds.getValue(0, displaycolumn, value);
                        }
                    }
                    if ("dropdownlistvalidated".equals(attributes.getProperty("mode"))) {
                        select.append("<option style=\"background-color: red;\" value=\"").append(value).append("\"").append(" selected>?").append(SafeHTML.encodeForHTML(translatevalue ? this.tp.translate(displayvalue) : displayvalue)).append("?</option>");
                    } else {
                        select.append("<option value=\"").append(value).append("\"").append(" selected>").append(SafeHTML.encodeForHTML(translatevalue ? this.tp.translate(value) : value)).append("</option>");
                    }
                }
                select.append("</select>");
                output = select.toString();
            } else {
                output = "TAG ERROR: Primary data not found in SDIData.";
            }
        }
        catch (Exception e) {
            output = "ERROR:" + e.getMessage();
        }
        return output;
    }

    public String getValueListJSArray(PropertyList attributes, SDITagInfo sdiInfo) {
        StringBuffer html;
        block27: {
            html = new StringBuffer();
            if (attributes.getProperty("sdcid").length() > 0 && attributes.getProperty("sql").trim().length() == 0) {
                try {
                    SDIData sdidata = this.getSDCSelectData(attributes, sdiInfo);
                    DataSet ds = sdidata.getDataset("primary");
                    if (ds != null) {
                        String[] keycols = sdidata.getKeys("primary");
                        html.append("\ndd_dropdownvalues['").append(attributes.getProperty("sdcid")).append("']=['(none)'");
                        for (int i = 0; i < ds.getRowCount(); ++i) {
                            html.append(",'").append(SafeHTML.encodeForJavaScript(ds.getValue(i, keycols[0]))).append("'");
                        }
                        html.append("]");
                        break block27;
                    }
                    html.append("\ndd_dropdownvalues['").append(attributes.getProperty("columnid")).append("']=[");
                    html.append("'TAG ERROR: Primary data not found in SDIData.'");
                    html.append("]");
                }
                catch (Exception e) {
                    html.append("\ndd_dropdownvalues['").append(attributes.getProperty("columnid")).append("']=[");
                    html.append("'ERROR:" + e.getMessage() + "'");
                    html.append("]");
                }
            } else if (attributes.getProperty("reftypeid").length() > 0 && attributes.getProperty("sql").trim().length() == 0) {
                DataSet ds = this.getRefTypeData(attributes, sdiInfo);
                if (ds != null) {
                    html.append("\ndd_dropdownvalues['").append(SafeHTML.encodeForJavaScript(attributes.getProperty("reftypeid"))).append("']=['(none)'");
                    for (int i = 0; i < ds.getRowCount(); ++i) {
                        html.append(",'").append(SafeHTML.encodeForJavaScript(ds.getValue(i, "refvalueid"))).append("'");
                    }
                    html.append("]");
                } else {
                    html.append("\ndd_dropdownvalues['").append(attributes.getProperty("columnid")).append("']=[");
                    html.append("'TAG ERROR: Reference data could not be retrieved.'");
                    html.append("]");
                }
            } else if (attributes.getProperty("sql").length() > 0) {
                String sql = attributes.getProperty("sql");
                DataSet ds = (DataSet)sdiInfo.getChildTagData("SqlData:" + sql);
                if (ds == null) {
                    QueryProcessor qp = this.getQueryProcessor();
                    String dsn = attributes.getProperty("datasetname");
                    if (sdiInfo != null && dsn.length() > 0 && sdiInfo.getDataSet(dsn) != null && sql.contains("[") && sql.contains("]")) {
                        sql = ElementUtil.evaluateExpression(dsn, -1, attributes.getProperty("columnid"), sql, sdiInfo);
                    }
                    ds = qp.getSqlDataSet(sql);
                    sdiInfo.setChildTagData("SqlData:" + attributes.getProperty("sql"), ds);
                }
                if (ds != null) {
                    html.append("\ndd_dropdownvalues['").append(attributes.getProperty("columnid")).append("']=['(none)'");
                    String columnid = ds.getColumnId(0);
                    for (int i = 0; i < ds.getRowCount(); ++i) {
                        html.append(",'").append(SafeHTML.encodeForJavaScript(ds.getValue(i, columnid))).append("'");
                    }
                    html.append("]");
                } else {
                    html.append("\ndd_dropdownvalues['").append(attributes.getProperty("columnid")).append("']=[");
                    html.append("'TAG ERROR: SQL data could not be retrieved.'");
                    html.append("]");
                }
            } else if (attributes.getProperty("displayvalue").length() > 0) {
                html.append("\ndd_dropdownvalues['").append(attributes.getProperty("columnid")).append("']=['(none)'");
                String displaylist = attributes.getProperty("displayvalue");
                String[] displays = StringUtil.split(displaylist, ";");
                if (displays != null && displays.length > 0) {
                    for (int i = 0; i < displays.length; ++i) {
                        String display = displays[i].trim();
                        int pos = (display = StringUtil.replaceAll(display, "#semicolon#", ";")).indexOf("=");
                        if (pos >= 0) {
                            html.append(",'").append(SafeHTML.encodeForJavaScript(display.substring(0, pos).trim())).append("'");
                            continue;
                        }
                        html.append(SafeHTML.encodeForJavaScript(display.trim()));
                    }
                    html.append("]");
                }
            } else if (attributes.getProperty("dropdownvalues").length() > 0) {
                String name = attributes.getProperty("dropdowncomboid");
                if (name == null || name.length() == 0) {
                    name = attributes.getProperty("columnid");
                }
                html.append("\ndd_dropdownvalues['").append(name).append("']=['(none)'");
                String ddvalues = attributes.getProperty("dropdownvalues");
                String[] displays = StringUtil.split(ddvalues, ";");
                if (displays != null && displays.length > 0) {
                    for (int i = 0; i < displays.length; ++i) {
                        String display = displays[i].trim();
                        html.append(",'").append(SafeHTML.encodeForJavaScript(display)).append("'");
                    }
                    html.append("]");
                }
            }
        }
        return html.toString();
    }

    public static String getTemplateRowStart(String datasetname) {
        return "<tr id=\"__" + SDIData.getDatasetCode(datasetname) + "_templaterow\" style=\"display:none\"><td><table id=\"__" + SDIData.getDatasetCode(datasetname) + "_templatetable\">\n";
    }

    public static String getTemplateRowEnd() {
        return "</table></td></tr>\n";
    }

    public static String getFixedRowInputs(String datasetname, String[] columns, int rows, String prefix) {
        return SDITagUtil.getFixedRowInputs(datasetname, columns, rows, prefix, null);
    }

    public static String getFixedRowInputs(String datasetname, String[] columns, int rows, String prefix, String separator) {
        return SDITagUtil.getFixedRowInputs(datasetname, columns, rows, prefix, separator, null, null);
    }

    public static String getFixedRowInputs(String datasetname, String[] columns, int rows, String prefix, String separator, String fkcolumns, String customcolumns) {
        StringBuffer html = new StringBuffer();
        if (columns != null && columns.length > 0) {
            String datasetcode = SDIData.getDatasetCode(datasetname);
            html.append("<input type=\"hidden\" name=\"__").append(prefix).append(datasetcode).append("_rows\" id=\"__").append(prefix).append(datasetcode).append("_rows\" value=\"").append(rows).append("\">\n");
            html.append("<input type=\"hidden\" name=\"__").append(prefix).append(datasetcode).append("_initrows\" id=\"__").append(prefix).append(datasetcode).append("_initrows\" value=\"").append(rows).append("\">\n");
            StringBuffer value = new StringBuffer(columns[0]);
            for (int i = 1; i < columns.length; ++i) {
                value.append(";").append(columns[i]);
            }
            html.append("<input type=\"hidden\" name=\"__").append(prefix).append(datasetcode).append("_cols\" id=\"__").append(prefix).append(datasetcode).append("_cols\" value=\"").append(value.toString()).append("\">\n");
            if (fkcolumns != null && fkcolumns.length() > 0) {
                html.append("<input type=\"hidden\" name=\"__").append(prefix).append(datasetcode).append("_fkcols\" id=\"__").append(prefix).append(datasetcode).append("_fkcols\" value=\"").append(fkcolumns).append("\">\n");
            }
            if (customcolumns != null && customcolumns.length() > 0) {
                String safestr = customcolumns;
                if (customcolumns.startsWith("(")) {
                    safestr = EncryptDecrypt.obfsql(customcolumns);
                }
                html.append("<input type=\"hidden\" name=\"__").append(prefix).append(datasetcode).append("_custcols\" id=\"__").append(prefix).append(datasetcode).append("_custcols\" value=\"").append(safestr).append("\">\n");
            }
            if (separator != null && separator.length() > 0) {
                html.append("<input type=\"hidden\" name=\"__").append(prefix).append(datasetcode).append("_separator\" id=\"__").append(prefix).append(datasetcode).append("_separator\" value=\"").append(separator).append("\">\n");
            }
        }
        return html.toString();
    }

    public static String getRepeatedRowInputs(String datasetname, String[] keycols, QueryData queryData, String prefix, String templateid, int copies) {
        StringBuffer html = new StringBuffer();
        if (keycols != null && keycols.length > 0 && queryData != null) {
            String datasetcode = SDIData.getDatasetCode(datasetname);
            int currentrow = queryData.getCurrentRow();
            String status = queryData.getRowStatus(currentrow);
            String rowid = queryData.getRowId(currentrow);
            String name = "__" + prefix + datasetcode + rowid;
            html.append("<input type=\"hidden\" name=\"").append(name).append("_rs\" id=\"").append(name).append("_rs\" value=\"").append(status).append("\">\n");
            if (datasetcode.equals("di")) {
                String dename = "__" + prefix + "de" + rowid;
                html.append("<input type=\"hidden\" name=\"__").append(prefix).append("de").append(rowid).append("\" id=\"").append(name).append("\" value=\"").append(status).append("\">\n");
            }
            StringBuffer value = new StringBuffer(queryData.getValue(currentrow, keycols[0], "(null)"));
            for (int i = 1; i < keycols.length; ++i) {
                value.append(";").append(queryData.getValue(currentrow, keycols[i], "(null)"));
            }
            html.append("<input type=\"hidden\" id=\"__").append(prefix).append(datasetcode).append(rowid).append("_key\" name=\"__").append(prefix).append(datasetcode).append(rowid).append("_key\" value=\"").append(SafeHTML.encodeForHTMLAttribute(value.toString())).append("\">\n");
            if (templateid.length() > 0) {
                html.append("<input type=\"hidden\" name=\"__").append(prefix).append(datasetcode).append(rowid).append("_templateid\" value=\"").append(SafeHTML.encodeForHTMLAttribute(templateid)).append("\">\n");
            }
            if (copies > 1) {
                html.append("<input type=\"hidden\" name=\"__").append(prefix).append(datasetcode).append(rowid).append("_copies\" value=\"").append(String.valueOf(copies)).append("\">\n");
            }
        }
        return html.toString();
    }

    public static String getRepeatedRowInputs(String datasetname, String[] keycols, QueryData queryData, String prefix, String templateid, String templateKeyId1, String templateKeyId2, String templateKeyId3, int copies) {
        StringBuffer html = new StringBuffer();
        html.append(SDITagUtil.getRepeatedRowInputs(datasetname, keycols, queryData, prefix, templateid, copies));
        if (keycols != null && keycols.length > 0 && queryData != null) {
            String datasetcode = SDIData.getDatasetCode(datasetname);
            int currentrow = queryData.getCurrentRow();
            String status = queryData.getRowStatus(currentrow);
            String rowid = queryData.getRowId(currentrow);
            if (templateKeyId1.length() > 0) {
                html.append("<input type=\"hidden\" name=\"__").append(prefix).append(datasetcode).append(rowid).append("_templatekeyid1\" value=\"").append(SafeHTML.encodeForHTMLAttribute(templateKeyId1)).append("\">\n");
            }
            if (templateKeyId2.length() > 0) {
                html.append("<input type=\"hidden\" name=\"__").append(prefix).append(datasetcode).append(rowid).append("_templatekeyid2\" value=\"").append(SafeHTML.encodeForHTMLAttribute(templateKeyId2)).append("\">\n");
            }
            if (templateKeyId3.length() > 0) {
                html.append("<input type=\"hidden\" name=\"__").append(prefix).append(datasetcode).append(rowid).append("_templatekeyid3\" value=\"").append(SafeHTML.encodeForHTMLAttribute(templateKeyId3)).append("\">\n");
            }
        }
        return html.toString();
    }

    public static String getGridScript(ArrayList grid, String prefix, int totalrows, int totalcols) {
        return SDITagUtil.getGridScript(grid, prefix, totalrows, totalcols, false);
    }

    public static String getGridScript(ArrayList grid, String prefix, int totalrows, int totalcols, boolean defer) {
        StringBuffer html = new StringBuffer();
        html.append("<script>\n");
        html.append("var __").append(prefix).append("cells = new Array();\n");
        html.append("var ").append(prefix).append("handler1;\n");
        html.append("function ").append(prefix).append("RenderFunc(){\n");
        if (grid != null) {
            for (int i = 0; i < grid.size(); ++i) {
                html.append("__").append(prefix).append("cells[").append(i).append("] = new Array( ");
                ArrayList row = (ArrayList)grid.get(i);
                for (int j = 0; j < row.size(); ++j) {
                    html.append("\"").append(row.get(j)).append("\"").append(j < row.size() - 1 ? ", " : "");
                }
                html.append(" );\n");
            }
        }
        html.append("").append(prefix).append("handler1 = new GridHandler( __").append(prefix).append("cells, \"");
        html.append(prefix).append("menu1\", \"").append(prefix).append("\", ").append(totalrows).append(", ").append(totalcols).append(", \"").append(prefix).append("menu1shadow\", \"").append(prefix).append("menu1selectblocker\" );\n");
        html.append("}\n");
        if (!defer) {
            html.append("").append(prefix).append("RenderFunc();\n");
        }
        html.append("</script>");
        return html.toString();
    }

    public static String getGrid(ArrayList grid, String prefix, TranslationProcessor tp) {
        return SDITagUtil.getGrid(grid, prefix, -1, -1, tp);
    }

    public static String getGrid(ArrayList grid, String prefix, Browser browser, TranslationProcessor tp) {
        return SDITagUtil.getGrid(grid, prefix, -1, -1, false, browser, tp);
    }

    public static String getGrid(ArrayList grid, String prefix, int totalrows, int totalcols, Browser browser, TranslationProcessor tp) {
        return SDITagUtil.getGrid(grid, prefix, -1, -1, false, browser, tp);
    }

    public static String getGrid(ArrayList grid, String prefix, int totalrows, int totalcols, boolean defer, Browser browser, TranslationProcessor tp) {
        if (browser == null) {
            return SDITagUtil.getGrid(grid, prefix, totalrows, totalcols, tp);
        }
        StringBuffer html = new StringBuffer();
        html.append("<style>\n");
        html.append(".menushadow {width: 100px;background-color: black;position: absolute;opacity:0.5;}\n");
        html.append(".menuholder {cursor: default;width: 100px;background-color: #FFFFFF;border-style:solid; border-width:1px;border-color:#9C9EA5;}\n");
        html.append(".menuselected {width: 100%;background-color: #BDB6C6; color: black;}\n");
        html.append(".menu {width: 100%;}\n");
        html.append(".menusep1 {font-size: 1px;height: 3px;border-bottom-style: solid;border-bottom-color: gray;border-bottom-width: 1px;}\n");
        html.append(".menusep2 {font-size: 1px;height: 3px;border-bottom-style: solid;border-bottom-color: white;border-bottom-width: 1px;}\n");
        html.append("</style>\n");
        SDITagUtil.buildGridDiv(html, prefix, browser, tp);
        html.append(SDITagUtil.getGridScript(grid, prefix, totalrows, totalcols, defer));
        return html.toString();
    }

    private static void buildGridDiv(StringBuffer html, String prefix, Browser browser, TranslationProcessor tp) {
        html.append("<div id=\"").append(prefix).append("menu1\" selectblockerid=\"").append(prefix).append("menu1selectblocker\" style=\"position: absolute;display:none;\" >");
        if (browser != null && browser.isIE() && browser.getVersion() < 8.0) {
            html.append("<iframe id=\"").append(prefix).append("menu1selectblocker\" frameborder=\"0\" width=\"100\" style=\"position:relative; top:0px; left:0px;\" src=\"WEB-CORE/blank.html\"></iframe>");
        }
        if (browser != null && !browser.isIE()) {
            html.append("<div id=\"").append(prefix).append("menu1shadow\" class=\"menushadow\" style=\"position:absolute; top:3px; left:5px;\"></div>");
        } else {
            html.append("<div id=\"").append(prefix).append("menu1shadow\" class=\"menushadow\" style=\"position:absolute; top:5px; left:5px;\"></div>");
        }
        html.append("<div class=\"menuholder\" style=\"position:absolute; top:0px; left:0px\">");
        String display = "";
        if (browser != null && !browser.isIE()) {
            display = " style=\"display:none;\" ";
        }
        html.append("<div class=\"menu\" ").append(display).append(" onclick=\"").append(prefix).append("menu1.handler.cut()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\" id=\"").append(prefix).append("cut\">&nbsp;" + tp.translate("Cut") + "</div>");
        html.append("<div class=\"menu\" ").append(display).append(" onclick=\"").append(prefix).append("menu1.handler.copy()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\" id=\"").append(prefix).append("copy\">&nbsp;" + tp.translate("Copy") + "</div>");
        html.append("<div class=\"menu\" ").append(display).append(" onclick=\"").append(prefix).append("menu1.handler.paste()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\" id=\"").append(prefix).append("paste\">&nbsp;" + tp.translate("Paste") + "</div>");
        html.append("<div class=\"menusep1\" id=\"").append(prefix).append("sep1_1\"></div>");
        html.append("<div class=\"menusep2\" id=\"").append(prefix).append("sep1_2\"></div>");
        html.append("<div class=\"menu\" onclick=\"").append(prefix).append("menu1.handler.fillDown()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\" id=\"").append(prefix).append("filldown\">&nbsp;" + tp.translate("Fill Down") + "</div>");
        html.append("<div class=\"menu\" onclick=\"").append(prefix).append("menu1.handler.fillAcross()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\" id=\"").append(prefix).append("fillacross\">&nbsp;" + tp.translate("Fill Across") + "</div>");
        html.append("<div class=\"menu\" onclick=\"").append(prefix).append("menu1.handler.increaseDown()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\" id=\"").append(prefix).append("increasedown\">&nbsp;" + tp.translate("Increase Down") + "</div>");
        html.append("</div>");
        html.append("</div>\n");
    }

    public static String getGrid(ArrayList grid, String prefix, int totalrows, int totalcols, TranslationProcessor tp) {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\" >");
        html.append("document.writeln('<style>');\n");
        html.append("if(sapphire.browser.ie){");
        html.append("document.writeln('.menushadow {width: 97px;background-color: black;position: absolute;opacity:0.5;}');\n");
        html.append("document.writeln('.menuholder {cursor: default;width: 100px;background-color: #FFFFFF;border-style:solid; border-width:1px;border-color:#9C9EA5; padding: 2px 2px 2px 2px;}');\n");
        html.append("document.writeln('.menuselected {width: 100%;padding: 2px 2px 2px 10px;background-color: #BDB6C6; color: black;}');\n");
        html.append("document.writeln('.menu {width: 100%;padding: 2px 2px 2px 10px;}');\n");
        html.append("}else{\n");
        html.append("document.writeln('.menushadow {width: 101px;background-color: black;position: absolute;opacity:0.5;}');\n");
        html.append("document.writeln('.menuholder {cursor: default;width: 100px;background-color: #FFFFFF;border-style:solid; border-width:1px;border-color:#9C9EA5;}');\n");
        html.append("document.writeln('.menuselected {width: 100%;background-color: #BDB6C6; color: black;}');\n");
        html.append("document.writeln('.menu {width: 100%;}');\n");
        html.append("}\n");
        html.append("document.writeln('</style>');\n");
        html.append("</script>");
        html.append("<style>\n");
        html.append(".menusep1 {font-size: 1px;height: 3px;border-bottom-style: solid;border-bottom-color: gray;border-bottom-width: 1px;}\n");
        html.append(".menusep2 {font-size: 1px;height: 3px;border-bottom-style: solid;border-bottom-color: white;border-bottom-width: 1px;}\n");
        html.append("</style>\n");
        SDITagUtil.buildGridDiv(html, prefix, null, tp);
        html.append(SDITagUtil.getGridScript(grid, prefix, totalrows, totalcols));
        return html.toString();
    }

    public static String getGrid(ArrayList grid, TranslationProcessor tp) {
        return SDITagUtil.getGrid(grid, "", -1, -1, tp);
    }

    public static String getGrid(int totalrows, int totalcols, TranslationProcessor tp) {
        return SDITagUtil.getGrid(null, "", totalrows, totalcols, tp);
    }

    public void collectDropDownComboInfo(PropertyList attributes, SDITagInfo sdiInfo, PageContext pageContext) {
        StringBuffer dd_dropdownvalues;
        String valuelistid = attributes.getProperty("reftypeid");
        if (valuelistid.length() == 0) {
            valuelistid = attributes.getProperty("sdcid");
        }
        if (valuelistid.length() == 0) {
            valuelistid = attributes.getProperty("columnid");
        }
        if (valuelistid.length() == 0 || attributes.getProperty("dropdownvalues").trim().length() > 0) {
            valuelistid = attributes.getProperty("dropdowncomboid");
        }
        if ((dd_dropdownvalues = (StringBuffer)pageContext.getAttribute("dd_dropdownvalues")) != null) {
            if (dd_dropdownvalues.indexOf("dd_dropdownvalues['" + valuelistid + "']=") < 0) {
                dd_dropdownvalues.append(this.getValueListJSArray(attributes, sdiInfo)).append(";");
            }
        } else {
            dd_dropdownvalues = new StringBuffer().append(this.getValueListJSArray(attributes, sdiInfo)).append(";");
            pageContext.setAttribute("dd_dropdownvalues", (Object)dd_dropdownvalues);
        }
    }

    public static String getTranslateIcon(String textid, String texttype, String fieldid, PageContext pageContext) {
        Button b = new Button(pageContext);
        if (fieldid != null && fieldid.length() > 0) {
            b.setAction("sapphire.lookup.util.openWindow( 'addtranslation', 'Edit Translation', 'rc?command=page&page=LV_AddTranslation&textid=' + document.getElementById('" + fieldid + "').value + '&defaulttexttype=" + texttype + "', 850, 600, true );");
        } else {
            b.setAction("sapphire.lookup.util.openWindow( 'addtranslation', 'Edit Translation', 'rc?command=page&page=LV_AddTranslation&textid=" + textid + "&defaulttexttype=" + texttype + "', 850, 600, true );");
        }
        b.setTip(new TranslationProcessor(pageContext).translate("Click to open the translation editor."));
        b.setImg("WEB-CORE/imageref/society_and_culture/people/various_people/16/user_headset.png");
        b.setMargin("none");
        b.setHighlight("false");
        return "<div style=\"display:inline;vertical-align:top;margin-left:5px\">" + b.getHtml() + "</div>";
    }

    public static String generateId(String sdcId, String prefix, int maxlen) {
        int r = (int)(Math.random() * 100.0);
        String id = "" + sdcId + "_" + r;
        if (maxlen > -1 && id.length() + prefix.length() > maxlen) {
            String t = id;
            id = "" + sdcId.substring(0, sdcId.length() - (id.length() + prefix.length() - maxlen)) + r;
        }
        return prefix + id.trim();
    }
}

