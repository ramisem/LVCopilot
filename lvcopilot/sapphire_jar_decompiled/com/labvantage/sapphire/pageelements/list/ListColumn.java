/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.list;

import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.list.CalendarHelper;
import com.labvantage.sapphire.pageelements.list.List;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.format.RelativeDateFormat;
import java.util.Calendar;
import javax.servlet.jsp.PageContext;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.JstlUtil;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ListColumn
extends BaseElement {
    private int currentRow = -1;
    private String datasetname = "primary";
    private PropertyList column;
    private TranslationProcessor tp;
    private PropertyList sdc;
    CalendarHelper calendar;
    private List.ListMode listmode = List.ListMode.LIST;

    public ListColumn(PageContext pageContext, SDITagInfo sdiInfo) {
        this.pageContext = pageContext;
        this.sdiInfo = sdiInfo;
        this.tp = this.getTranslationProcessor();
        if (sdiInfo.getSdcid() != null && sdiInfo.getSdcid().length() > 0) {
            this.sdc = (PropertyList)pageContext.getAttribute(sdiInfo.getSdcid() + "_props");
        }
    }

    public void setListmode(List.ListMode listmode) {
        this.listmode = listmode;
    }

    public void setRow(String currentRow) {
        this.currentRow = (Integer)JstlUtil.evaluateExpression(currentRow, this.pageContext);
    }

    public void setRow(int currentRow) {
        this.currentRow = currentRow;
    }

    public void setDatasetname(String datasetname) {
        this.datasetname = datasetname;
    }

    public void setColumn(PropertyList column) {
        this.column = column;
    }

    public void setColumnProperty(String propertyid, String value) {
        if (this.column != null) {
            this.column.setProperty(propertyid, value);
        }
    }

    @Override
    public String getHtml() {
        boolean bl;
        StringBuffer html = new StringBuffer();
        if (this.listmode.equals((Object)List.ListMode.CALENDAR)) {
            this.calendar = new CalendarHelper(this.pageContext);
        }
        if (this.currentRow == -1) {
            this.currentRow = this.sdiInfo.getCurrentRow(this.datasetname);
        }
        boolean enablefixedheader = !"N".equals(this.element.getProperty("enablefixedheader"));
        String mode = this.column.getProperty("mode");
        String columnid = this.column.getProperty("columnid");
        PropertyList sdcprops = (PropertyList)this.pageContext.getAttribute(this.sdiInfo.getSdcid() + "_props");
        if (sdcprops == null) {
            bl = false;
        } else {
            this.getSDCProcessor();
            if (sdcprops.getCollectionNotNull("columns").getPropertyList(columnid) == null) {
                bl = false;
            } else {
                this.getSDCProcessor();
                PropertyList propertyList = sdcprops.getCollection("columns").getPropertyList(columnid);
                this.getSDCProcessor();
                bl = propertyList.getProperty("timezoneindependent").equals("Y");
            }
        }
        boolean isTimeZoneIndependent = bl;
        String translatevalue = this.column.getProperty("translatevalue");
        try {
            String value;
            if (columnid.indexOf(" ") > 0 && columnid.indexOf("sdidataitem[") < 0) {
                columnid = RequestParser.parseAlias(columnid);
                this.column.setProperty("columnid", columnid);
            }
            JSONObject columnObject = new JSONObject();
            if (this.sdc != null) {
                this.tp.setTextType(this.sdc.getProperty("sdcid"));
            }
            if (mode.indexOf("Deferred Display") == 0) {
                value = "&nbsp;";
            } else if (this.column.getProperty("pseudocolumn").length() > 0) {
                value = ElementUtil.evaluateExpression(this.datasetname, this.currentRow, columnid, this.column.getProperty("pseudocolumn"), this.sdiInfo, this.tp, null, true);
            } else {
                value = this.sdiInfo.getValue(this.datasetname, this.currentRow, columnid);
                if (value != null) {
                    value = ListColumn.sanitizeHTMLValue(value);
                }
                if (this.sdc != null && this.sdc.getProperty(columnid + "_linkreftypeid").length() == 0) {
                    String reftypeid;
                    PropertyList columnLink;
                    PropertyList columnDef;
                    PropertyListCollection sdccolumns = this.sdc.getCollection("columns");
                    this.sdc.setProperty(columnid + "_linkreftypeid", "N");
                    if (sdccolumns != null && (columnDef = sdccolumns.getPropertyList(columnid)) != null && (columnLink = columnDef.getPropertyList("link")) != null && (reftypeid = columnLink.getProperty("reftypeid")).length() > 0) {
                        this.sdc.setProperty(columnid + "_linkreftypeid", reftypeid);
                    }
                }
                if (this.sdc != null && !"N".equals(this.sdc.getProperty(columnid + "_linkreftypeid"))) {
                    this.tp.setTextType(this.sdc.getProperty(columnid + "_linkreftypeid"));
                }
            }
            String originalValue = value;
            if (mode.equals("Hidden Value") || mode.equals("hidden")) {
                if (this.listmode.equals((Object)List.ListMode.LIST)) {
                    html.append("<input type=\"hidden\" name=\"").append(SDIData.getDatasetCode(this.datasetname)).append(this.currentRow).append("_").append(this.column.getProperty("columnid")).append("\" id=\"").append(SDIData.getDatasetCode(this.datasetname)).append(this.currentRow).append("_").append(this.column.getProperty("columnid")).append("\" value=\"").append(value).append("\"/>");
                } else {
                    columnObject.put("value", value);
                }
                if (this.listmode.equals((Object)List.ListMode.CALENDAR)) {
                    columnObject.put("origvalue", value);
                    this.calendar.insertColumnCalendarField(this.element, this.column, columnObject, this.sdiInfo.getObject(this.datasetname, this.currentRow, columnid), isTimeZoneIndependent);
                }
            } else {
                boolean rowClickSelection;
                String tip;
                String idclause;
                String width;
                String rowselector = "Y";
                if (this.column.getProperty("displayvalue").length() > 0 && mode.indexOf("Deferred Display") != 0) {
                    if ((value = SDITagUtil.getDisplayValue(value, this.column.getProperty("displayvalue"))).indexOf("[") >= 0 && (value = ElementUtil.evaluateExpression(this.datasetname, this.currentRow, columnid, value, this.sdiInfo, !"N".equals(translatevalue) ? this.tp : null)).startsWith("[img=") && value.endsWith("]")) {
                        String f = value.substring(value.indexOf("=") + 1);
                        if (f.length() > 1) {
                            f = f.substring(0, f.length() - 1);
                            Image i = new Image(this.pageContext);
                            i.setDimensions(16, 16);
                            i.setImageSrc(f);
                            value = i.getHtml();
                        } else {
                            value = "";
                        }
                    }
                    rowselector = "N";
                }
                String tvalue = value;
                if ("Y".equals(translatevalue)) {
                    tvalue = this.tp.translate(value);
                }
                PropertyList link = this.column.getPropertyList("link");
                if (rowselector.equals("Y") && link != null && link.getProperty("href").length() > 0) {
                    rowselector = "N";
                }
                String widthclause = (width = this.column.getProperty("width")).length() > 0 && !enablefixedheader ? " width=\"" + width + "\" " : "";
                String styleclause = "";
                String align = this.column.getProperty("align");
                String alignclause = align.length() > 0 ? " style=\"text-align:" + (align.equals("middle") ? "center" : align) + "\"" : "";
                String id = this.column.getProperty("id");
                String string = idclause = id.length() > 0 ? " id=\"" + id + "\"" : "";
                if (mode.indexOf("Deferred Display") == 0) {
                    String keyid1 = this.sdiInfo.getValue(this.datasetname, this.currentRow, this.sdc.getProperty("keycolid1"));
                    String keyid2 = this.sdiInfo.getValue(this.datasetname, this.currentRow, this.sdc.getProperty("keycolid2"));
                    String keyid3 = this.sdiInfo.getValue(this.datasetname, this.currentRow, this.sdc.getProperty("keycolid3"));
                    idclause = " id=\"" + columnid + "_" + keyid1 + ";" + keyid2 + ";" + keyid3 + "\" rowid=\"" + this.sdiInfo.getRowId(this.datasetname) + "\"";
                }
                String tipclause = (tip = this.column.getProperty("tip")).length() > 0 ? " title=\"" + ElementUtil.evaluateExpression(this.datasetname, this.currentRow, columnid, tip, this.sdiInfo, this.tp) + "\"" : "";
                String cssclass = " class=\"" + this.column.getProperty("class", "list_tablebodycell") + "\" ";
                String selectorType = this.element.getProperty("selectortype");
                boolean bl2 = rowClickSelection = !selectorType.equals("none") && this.element.getProperty("rowclickselection", "N").equals("Y");
                if (this.listmode.equals((Object)List.ListMode.LIST)) {
                    html.append("<td").append(idclause).append(alignclause).append(tipclause).append(widthclause).append(styleclause).append(cssclass).append(" ").append(this.column.getProperty("tdattributes")).append(rowClickSelection ? " rowselector=\"" + rowselector + "\"" : "").append(">");
                } else {
                    columnObject.put("tip", tipclause);
                    columnObject.put("id", id);
                    columnObject.put("mode", mode);
                }
                if (value == null || value.length() == 0) {
                    if (this.listmode.equals((Object)List.ListMode.LIST)) {
                        html.append("&nbsp;");
                    } else if (this.column.getProperty("columnid").length() > 0) {
                        columnObject.put("value", "");
                    }
                    if (this.listmode.equals((Object)List.ListMode.CALENDAR)) {
                        this.calendar.insertColumnCalendarField(this.element, this.column, columnObject, "", isTimeZoneIndependent);
                    }
                } else if (link != null && (link.getProperty("href").length() > 0 || link.getProperty("toolbarbuttonid").length() > 0)) {
                    if (mode.length() == 0 || mode.equals("Display Text") || mode.equals("Display Icon") || mode.equals("Display Value") || mode.equals("readonly")) {
                        String buttonid = link.getProperty("toolbarbuttonid", "").trim();
                        String linkhref = link.getProperty("href", "").trim();
                        if (linkhref.toLowerCase().startsWith("javascript:")) {
                            String text;
                            int endpos;
                            int pos = linkhref.indexOf("parent.accept(");
                            if (pos > -1 && linkhref.indexOf("[__row]") == -1 && (endpos = linkhref.indexOf(")", pos)) > -1 && (text = linkhref.substring(pos + 14, endpos)).indexOf(",") == -1) {
                                linkhref = text.trim().length() == 0 ? linkhref.substring(0, endpos) + "'',[rowid]" + linkhref.substring(endpos) : linkhref.substring(0, endpos) + ",[rowid]" + linkhref.substring(endpos);
                                link.setProperty("href", linkhref);
                            }
                            link = (PropertyList)link.clone();
                            String temp = link.getProperty("href");
                            String[] tokens = StringUtil.getTokens(temp);
                            for (int t = 0; t < tokens.length; ++t) {
                                String evalTok = ElementUtil.evaluateExpression(this.datasetname, this.currentRow, columnid, "[" + tokens[t] + "]", this.sdiInfo, "Y".equals(translatevalue) ? this.tp : null);
                                evalTok = StringUtil.replaceAll(StringUtil.replaceAll(evalTok, "'", "\\'"), "\"", "\\\"");
                                temp = StringUtil.replaceAll(temp, "[" + tokens[t] + "]", evalTok);
                            }
                            link.setProperty("href", temp);
                        }
                        String finalString = ElementUtil.evaluateExpression(this.datasetname, this.currentRow, columnid, link.toJSONString(false, false), this.sdiInfo, "Y".equals(translatevalue) ? this.tp : null);
                        String[] keys = this.sdiInfo.getKeycols();
                        StringBuilder linkUrl = new StringBuilder();
                        if (keys != null) {
                            String key1 = this.sdiInfo.getValue(this.datasetname, this.currentRow, keys[0]);
                            String key2 = keys.length > 1 ? this.sdiInfo.getValue(this.datasetname, this.currentRow, keys[1]) : "";
                            String key3 = keys.length > 2 ? this.sdiInfo.getValue(this.datasetname, this.currentRow, keys[2]) : "";
                            String keyid = key1 + (key2.length() > 0 ? "|" + key2 : "") + (key3.length() > 0 ? "|" + key3 : "");
                            linkUrl.append("<a href=\"JavaScript:;\" onClick=\"JavaScript:sapphire.page.list.link.open('").append(keyid).append("',").append(StringUtil.replaceAll(finalString, "\"", "&quot;")).append(");void(0);\">");
                            linkUrl.append(tvalue);
                            linkUrl.append("</a>");
                        } else {
                            linkUrl.append(ElementUtil.getLink(this.datasetname, columnid, this.sdiInfo, link, tvalue, this.currentRow, "Y".equals(translatevalue) ? this.tp : null));
                        }
                        if (this.listmode.equals((Object)List.ListMode.LIST)) {
                            html.append((CharSequence)linkUrl);
                        } else {
                            columnObject.put("value", linkUrl);
                            columnObject.put("origvalue", originalValue);
                        }
                        if (this.listmode.equals((Object)List.ListMode.CALENDAR)) {
                            this.calendar.insertColumnCalendarField(this.element, this.column, columnObject, this.sdiInfo.getObject(this.datasetname, this.currentRow, columnid), isTimeZoneIndependent);
                        }
                    }
                } else {
                    if (this.listmode.equals((Object)List.ListMode.LIST)) {
                        if ("Y".equals(this.column.getProperty("userelativedateformat"))) {
                            try {
                                Calendar calendar = this.sdiInfo.getCalendar(this.datasetname, columnid);
                                RelativeDateFormat relativeDateFormat = new RelativeDateFormat(false, new ConfigurationProcessor(this.pageContext).getPolicy("DateFormatPolicy", "Sapphire Custom", false), this.getTranslationProcessor());
                                tvalue = calendar != null ? "<span title=\"" + DOMUtil.convertChars(tvalue) + "\">" + DOMUtil.convertChars(relativeDateFormat.format(calendar.getTime())) + "</span>" : "&nbsp;";
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        html.append(this.column.getProperty("prefix")).append(tvalue).append(this.column.getProperty("suffix"));
                    } else if (this.column.getProperty("columnid").length() > 0) {
                        columnObject.put("value", tvalue);
                        columnObject.put("origvalue", originalValue);
                    }
                    if (this.listmode.equals((Object)List.ListMode.CALENDAR)) {
                        this.calendar.insertColumnCalendarField(this.element, this.column, columnObject, this.sdiInfo.getObject(this.datasetname, this.currentRow, columnid), isTimeZoneIndependent);
                    }
                }
                if (this.listmode.equals((Object)List.ListMode.LIST)) {
                    html.append("</td>");
                }
            }
            if (!this.listmode.equals((Object)List.ListMode.LIST)) {
                html.append(columnObject.toString());
            }
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        return html.toString();
    }

    public static String sanitizeHTMLValue(String value) {
        value = StringUtil.replaceAll(value, "&", "&amp;");
        value = StringUtil.replaceAll(value, "\"", "&#x22;");
        value = StringUtil.replaceAll(value, "'", "&#x27;");
        value = StringUtil.replaceAll(value, "<", "&lt;");
        value = StringUtil.replaceAll(value, ">", "&gt;");
        return value;
    }
}

