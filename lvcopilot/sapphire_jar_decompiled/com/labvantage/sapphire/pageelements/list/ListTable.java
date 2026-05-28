/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.list;

import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.pageelements.list.List;
import com.labvantage.sapphire.pageelements.list.ListRow;
import com.labvantage.sapphire.tagext.QueryData;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.SDCProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.Browser;
import sapphire.util.HttpUtil;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ListTable
extends BaseElement {
    private SDCProcessor sdcProcessor;
    private String sortby;
    private String datasetName = "primary";
    private boolean renderHeader = true;
    private StringBuffer groupHeaderRowHtml;
    private boolean showCollapseAll = false;
    private int headerHeight = 29;
    private List.ListMode listmode = List.ListMode.LIST;

    public ListTable(PageContext pageContext, SDITagInfo sdiInfo, SDCProcessor sdcProcessor) {
        this.pageContext = pageContext;
        if (pageContext != null && this.browser == null) {
            this.browser = new Browser(pageContext);
        }
        this.sdiInfo = sdiInfo;
        this.sdcProcessor = sdcProcessor;
    }

    public void setListmode(List.ListMode listmode) {
        this.listmode = listmode;
    }

    public void setRenderHeader(boolean renderHeader) {
        this.renderHeader = renderHeader;
    }

    public void setGroupHeaderRowHtml(StringBuffer groupHeaderRowHtml) {
        this.groupHeaderRowHtml = groupHeaderRowHtml;
    }

    public void setShowCollapseAll(boolean showCollapseAll) {
        this.showCollapseAll = showCollapseAll;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public void setSortbyColumn(String sortbycol) {
        this.sortby = sortbycol;
    }

    @Override
    public String getHtml() {
        QueryData queryData = this.sdiInfo.getQueryData(this.datasetName);
        boolean enablefixedheader = this.browser != null && (this.browser.isPhone() || this.browser.isTablet()) ? false : !"N".equals(this.element.getProperty("enablefixedheader"));
        StringBuffer html = new StringBuffer();
        JSONArray headersArr = new JSONArray();
        if (this.renderHeader) {
            int headercol = 0;
            if (enablefixedheader && this.listmode.equals((Object)List.ListMode.LIST)) {
                html.append("<div class=\"list_tableheaderbg\" id=\"list_tableheaderdiv\"></div>");
                html.append("<div class=\"list_scrollcontainer\" id=\"list_scrollcontainer\">");
            }
            if (this.listmode.equals((Object)List.ListMode.LIST)) {
                html.append("<table id=\"list_" + this.element.getId() + "\" class=\"list_table\" cellspacing=\"0\">\n");
            }
            PropertyListCollection columns = this.element.getCollection("columns");
            if (this.listmode.equals((Object)List.ListMode.LIST)) {
                html.append("<thead id=\"listlayout\">");
                html.append("<tr" + (enablefixedheader ? " " : " class=\"list_tableheadrow\" style=\"text-align: left;\"") + ">\n");
                StringBuffer expandAllImageHtml = new StringBuffer();
                if (this.showCollapseAll) {
                    String initexpandimg = this.element.getProperty("initexpandimg").length() > 0 ? this.element.getProperty("initexpandimg") : "WEB-CORE/pagetypes/list/images/minus.gif";
                    expandAllImageHtml.append("<img src=\"").append(initexpandimg).append("\" id=\"collapse\" style=\"cursor: pointer;margin-top:4px;margin-left:" + (enablefixedheader ? "-2" : "2") + "px;\" width=\"12\" height=\"12\" onClick=\"expandall( this, document.getElementById( 'collapsetext' ),'").append("").append("','").append("").append("' )\"/>");
                }
                if (!this.element.getProperty("selectortype").equals("hyperlink") || !this.element.getProperty("selectortype").equals("none")) {
                    int width;
                    String selectorType = this.element.getProperty("selectortype");
                    int n = width = expandAllImageHtml.length() > 0 ? 30 : 16;
                    if (selectorType != null && (selectorType.equals("none") || selectorType.equals("radiobutton"))) {
                        html.append("<th id=\"list_header" + headercol + "\" " + (expandAllImageHtml.length() > 0 || selectorType.equals("radiobutton") ? "" : "style=\"display:none\"") + ">");
                        if (enablefixedheader) {
                            html.append("<div id=\"list_header" + headercol + "div\" class=\"list_tableheaderdiv\" style=\"min-width:20px\">");
                        }
                        html.append(expandAllImageHtml);
                        if (enablefixedheader) {
                            html.append("</div>");
                        }
                        html.append("</th>\n");
                    } else {
                        html.append("<th id=\"list_header" + headercol + "\" style=\"width:35px\">");
                        if (enablefixedheader) {
                            html.append("<div id=\"list_header" + headercol + "div\" class=\"list_tableheaderdiv\" style=\"width:" + width + "px;min-width:" + width + "px\">");
                        }
                        html.append(expandAllImageHtml);
                        html.append("<input style=\"margin-top:5px;margin-left:" + (expandAllImageHtml.length() > 0 ? 6 : (enablefixedheader ? 4 : 8)) + "px\" id=\"headerselector1\" type=\"checkbox\" name=\"").append("list_topheaderchx").append("\" onClick=\"setall( this.checked, document.getElementById( 'selectall' ),'").append("").append("','").append("").append("',this )\"").append(this.element.get("checkedclause")).append("/>\n");
                        if (enablefixedheader) {
                            html.append("</div>");
                        }
                        html.append("</th>\n");
                    }
                }
            }
            String sortbycolumnid = "";
            String sortbydirection = "";
            if (this.sortby != null && this.sortby.length() > 2 && this.sortby.indexOf(" ") > 0) {
                sortbycolumnid = this.sortby.substring(0, this.sortby.length() - 2);
                sortbydirection = this.sortby.substring(this.sortby.length() - 2, this.sortby.length());
            } else {
                sortbycolumnid = this.sortby;
                sortbydirection = " a";
            }
            if (columns != null && columns.size() > 0) {
                for (int i = 0; i < columns.size(); ++i) {
                    int configWidth;
                    String title;
                    boolean isSortbyCol;
                    PropertyList column = columns.getPropertyList(i);
                    String mode = column.getProperty("mode");
                    if (mode.equals("Hidden Value") || mode.equals("Do Not Retrieve")) continue;
                    ++headercol;
                    String sortimage = "";
                    String columnid = column.getProperty("columnid");
                    if (columnid.indexOf(" ") > 0 && columnid.indexOf("sdidataitem[") < 0) {
                        columnid = RequestParser.parseAlias(columnid);
                    }
                    if (isSortbyCol = sortbycolumnid.equals(columnid)) {
                        sortimage = sortbydirection.equals(" d") ? "<img src=\"WEB-CORE/imageref/flat/16/flat_white_sort_down_dropdown.svg\" id=\"" + columnid + "_sortimage\" direction=\"down\" style=\"width:10px;height:10px\"/>" : "<img src=\"WEB-CORE/imageref/flat/16/flat_white_sort_up.svg\"  id=\"" + columnid + "_sortimage\" direction=\"up\" style=\"width:10px;height:10px\"/>";
                    }
                    String wtitle = title = column.getProperty("title") != null && column.getProperty("title").length() > 0 ? column.getProperty("title") : columnid;
                    String[] titlerows = StringUtil.split(title, title.indexOf("<BR>") > 0 ? "<BR>" : (title.indexOf("<BR/>") > 0 ? "<BR/>" : (title.indexOf("<br/>") > 0 ? "<br/>" : "<br>")));
                    if (titlerows.length > 1) {
                        this.headerHeight = this.headerHeight < 20 * titlerows.length ? 20 * titlerows.length : this.headerHeight;
                        for (int t = 0; t < titlerows.length; ++t) {
                            if (t != 0 && titlerows[t].length() <= titlerows[t - 1].length()) continue;
                            wtitle = titlerows[t];
                        }
                    }
                    if (this.element.getProperty("headerheight").length() > 0) {
                        this.headerHeight = Integer.parseInt(this.element.getProperty("headerheight"));
                    }
                    String width = column.getProperty("width");
                    String widthclause = "";
                    int calcWidth = (wtitle.length() > 3 ? wtitle.length() * 8 : (wtitle.length() == 3 ? 30 : 20)) + (isSortbyCol ? 10 : 0);
                    if (calcWidth > 200) {
                        calcWidth = 200;
                    }
                    if (width.length() > 0 && !width.endsWith("px") && !width.endsWith("%") && (configWidth = Integer.parseInt(width)) < calcWidth) {
                        width = "" + calcWidth;
                    }
                    String styleclause = " style=\"vertical-align:middle;min-width:" + (width.length() > 0 ? (width.endsWith("px") || width.endsWith("%") ? width : width + "px") : calcWidth + "px") + (width.length() > 0 ? ";width:" + width + "px" : "") + "\" ";
                    String align = column.getProperty("align");
                    String alignclause = align != null && align.length() > 0 ? " align=\"" + align + "\"" : "align=\"left\"";
                    String headerContent = "";
                    headerContent = this.element.getProperty("sortcallback").length() > 0 && !this.element.getProperty("sortcallback").equals("N") ? (mode.equals("Deferred Display Text") || mode.equals("Deferred Display Icon") ? "<span title=\"" + this.getTranslationProcessor().translate("Sorting by the deferred column not supported") + "\">" + SafeHTML.encodeForHTML(title, true) + "</span>" : "<a class=\"list_tableheadlink\" href=\"JavaScript:sortResult( '" + SafeHTML.encodeForJavaScript(columnid) + "');\">" + SafeHTML.encodeForHTML(this.getTranslationProcessor().translate(title), true) + "</a>&nbsp;" + sortimage) : SafeHTML.encodeForHTML(title, true);
                    if (this.listmode.equals((Object)List.ListMode.LIST)) {
                        html.append("<th id=\"list_header" + headercol + "\" ").append(title.trim().length() > 0 ? "name=\"coltitle\" " : "").append(this.browser != null && this.browser.getOS() == 14 ? "style=\"border-left: 1px solid #6699CC;border-right: 1px solid #6699CC\" " : "").append(widthclause).append(styleclause).append(alignclause).append(">");
                        if (enablefixedheader) {
                            html.append("<div  id=\"list_header" + headercol + "div\" class=\"list_tableheaderdiv" + (title.trim().length() == 0 ? "empty" : "") + "\">");
                        }
                        html.append(headerContent.trim().length() == 0 ? "&nbsp;" : headerContent);
                        if (enablefixedheader) {
                            html.append("</div>");
                        }
                        html.append("</th>\n");
                        continue;
                    }
                    JSONObject col = new JSONObject();
                    try {
                        col.put("title", title);
                        col.put("mode", mode);
                        col.put("columnid", columnid);
                        headersArr.put(col);
                        continue;
                    }
                    catch (JSONException jSONException) {
                        // empty catch block
                    }
                }
            }
            if (this.listmode.equals((Object)List.ListMode.LIST)) {
                html.append("</tr>\n");
                html.append("</thead>");
                html.append("<tbody id=\"list_tablebody\">");
                if (enablefixedheader && this.headerHeight != 29) {
                    String style = "\n<style>\n    .list_scrollcontainer { margin-top:" + this.headerHeight + "px;}\n    .list_tableheaderbg { height:" + (this.headerHeight + 4) + "px;}\n    .list_tableheaderdiv, .list_tableheaderdivempty { margin-top: -" + (this.headerHeight + 2) + "px;height:" + (this.headerHeight + 2) + "px;}\n</style>\n";
                    html.insert(0, style);
                }
            }
        }
        ListRow listRow = new ListRow(this.pageContext, this.sdiInfo, this.sdcProcessor);
        listRow.setHasGroupby(this.groupHeaderRowHtml != null);
        listRow.setDatasetName(this.datasetName);
        listRow.setElementProperties(this.element);
        listRow.setListmode(this.listmode);
        queryData.resetRow(0);
        JSONObject listObject = new JSONObject();
        JSONArray sdiArr = new JSONArray();
        try {
            listObject.put("sdis", sdiArr);
            listObject.put("columns", headersArr);
            StringBuilder tableRowHtml = new StringBuilder();
            boolean isFirstRow = true;
            int groupItemCount = 0;
            while (queryData.nextRow(-1)) {
                if (this.listmode.equals((Object)List.ListMode.LIST)) {
                    tableRowHtml.append("<tr name=\"list_tablerow\" " + (isFirstRow ? "id=\"list_firstrow\"" : "") + " rownum=\"").append(queryData.getCurrentRow()).append("\" class=\"list_tablerow").append(queryData.getCurrentRow() % 2 == 0 ? "even" : "odd").append("\">");
                    tableRowHtml.append(listRow.getHtml());
                    tableRowHtml.append("</tr>");
                    isFirstRow = false;
                    ++groupItemCount;
                    continue;
                }
                JSONObject row = new JSONObject(listRow.getHtml());
                sdiArr.put(row);
            }
            if (this.listmode.equals((Object)List.ListMode.LIST) && this.groupHeaderRowHtml != null) {
                String tempGroupHeaderStr = this.groupHeaderRowHtml.toString();
                if (tempGroupHeaderStr.indexOf("[count]") >= 0) {
                    tempGroupHeaderStr = StringUtil.replaceAll(tempGroupHeaderStr, "[count]", " <span style=\"font-style:italic;font-weight:bold\">(" + groupItemCount + ")&lrm;</span>");
                }
                html.append(tempGroupHeaderStr);
            }
            if (this.listmode.equals((Object)List.ListMode.LIST)) {
                html.append((CharSequence)tableRowHtml);
            } else {
                html.append("<input type='hidden' id='sdijson' value = \"").append(HttpUtil.encodeURIComponent(listObject.toString())).append("\"</input>");
            }
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        return html.toString();
    }

    public int getHeaderHeight() {
        return this.headerHeight;
    }
}

