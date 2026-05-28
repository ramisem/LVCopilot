/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.advancedsearch;

import com.labvantage.sapphire.pageelements.advancedsearch.SearchContent;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AdvancedSearch
extends BaseElement {
    private final int BASIC = 0;
    private final int QUERY = 1;
    private final int CATEGORY = 2;
    private final int SCAN = 3;
    private final int FOLDER = 4;
    private final int ADHOC = 5;
    private final int VERSIONFILTER = 6;
    private final int CONTENT_TYPES = 7;
    private PropertyList[] props = new PropertyList[7];
    private String ONLOAD_DEFAULTQUERY = "Default Query";
    private String ONLOAD_DEFAULTCATEGORY = "Default Category";
    private String ONLOAD_FIRSTFOLDER = "First Folder";
    private String ONLOAD_LASTSEARCH = "Last Search";
    private String onloadAction;
    private String sdcid = null;
    private int width = 195;
    private String closePaneImg = "WEB-CORE/imageref/flat/16/flat_black_angle_up.svg";
    private String openPaneImg = "WEB-CORE/imageref/flat/16/flat_black_angle_down.svg";
    private boolean hasSideBarSearchContent = false;

    @Override
    public String getHtml() {
        if (this.element.getProperty("show").equalsIgnoreCase("") || this.element.getProperty("show").equalsIgnoreCase("Y")) {
            this.props[0] = this.element.getPropertyList("basicsearch");
            this.props[1] = this.element.getPropertyList("querysearch");
            this.props[2] = this.element.getPropertyList("categorysearch");
            this.props[3] = this.element.getPropertyList("scansearch");
            this.props[4] = this.element.getPropertyList("foldersearch");
            this.props[5] = this.element.getPropertyList("adhocsearch");
            this.props[6] = this.element.getPropertyList("versionfilter");
            for (int i = 0; i < this.props.length; ++i) {
                if (this.props[i] != null) continue;
                this.props[i] = new PropertyList();
            }
            this.props[4].setProperty("show", "N");
            this.props[6].setProperty("show", "N");
            this.sdcid = this.element.getProperty("sdcid");
            this.onloadAction = this.element.getProperty("onloadaction");
            String ws = this.element.getProperty("width", "185px");
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(ws);
            int width = 185;
            if (m.find()) {
                try {
                    width = Integer.parseInt(m.group());
                }
                catch (Exception e) {
                    width = 185;
                }
            }
            String lastSearchType = "";
            PropertyList pageData = (PropertyList)this.pageContext.getAttribute("pagedata", 2);
            String webpageid = pageData == null ? "" : pageData.getProperty("page");
            String cookieKey = webpageid;
            if (cookieKey.length() == 0) {
                String file = pageData.getProperty("file");
                if (file.length() > 0) {
                    int start = file.lastIndexOf("/");
                    cookieKey = file.substring(start + 1);
                    cookieKey = cookieKey.substring(0, cookieKey.indexOf(".")) + "_" + this.sdcid;
                } else {
                    cookieKey = this.sdcid;
                }
            }
            cookieKey = StringUtil.escape(cookieKey);
            RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
            PropertyList userConfig = requestContext.getPropertyList("userconfig");
            if (pageData == null || pageData.getProperty("forcelastsearchtype").equals("Y") || pageData.getProperty("keyid1").length() == 0 && pageData.getProperty("queryfrom").length() == 0 && pageData.getProperty("querywhere").length() == 0 && pageData.getProperty("queryid").length() == 0) {
                lastSearchType = this.onloadAction.equals(this.ONLOAD_LASTSEARCH) && userConfig != null ? userConfig.getProperty("as_lasttype_" + cookieKey) : "";
            }
            TranslationProcessor tp = this.getTranslationProcessor();
            StringBuffer output = new StringBuffer("");
            PropertyListCollection plugins = new PropertyListCollection();
            PropertyList plugin = new PropertyList();
            plugin.setProperty("pluginid", "perfect-scrollbar");
            plugin.setProperty("css", "Y");
            plugin.setProperty("allowminimized", "Y");
            plugins.add(plugin);
            output.append(JavaScriptAPITag.getJQueryAPI(true, false, plugins, this.pageContext));
            output.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/lookup.js\"></script>\n");
            output.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/search.js\"></script>\n");
            output.append("<script language=\"JavaScript\" src=\"WEB-OPAL/elements/advancedsearch/scripts/advancedsearch.js\"></script>\n");
            if (this.sdcid.equalsIgnoreCase("")) {
                output.append(tp.translate("SDC not defined for the AdvancedSearch element. Cannot continue."));
            } else {
                output.append("<div id=\"searchBarContainer\" style=\"").append("width:" + width + "px").append("\">\n");
                output.append("<table class=\"modern_search_outermaintable\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" height=\"100%\">\n");
                output.append("<tr valign=\"top\"><td>\n");
                output.append("<table class=\"modern_search_maintable\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\n");
                output.append("<tr class=\"modern_search_titlebar_row maintitle\">\n");
                output.append("  <td class=\"modern_search_titlebar\" id=\"search_titlebar\" nowrap align=center valign=middle>").append("&nbsp;").append(tp.translate("Advanced Search")).append("</td>\n");
                output.append("  <td class=\"modern_search_expandcollapse\" align=center valign=middle>\n");
                output.append("    <img title=\"").append(tp.translate("Close the Search Bar")).append("\" src=\"WEB-CORE/imageref/flat/32/flat_black_double_chevron_left.svg\" width=\"8px\" height=\"8px\" onClick=\"toggleSearch(null, null)\" style=\"cursor:pointer;\">\n");
                output.append("  </td>\n");
                output.append("</tr>");
                output.append("<tr valign=\"top\"><td colspan=\"2\">\n");
                output.append("<div class=\"modern_search_contentpane\" style=\"").append("width:" + width + "px").append("\">");
                output.append(this.getContentPanes(lastSearchType, cookieKey, userConfig, width));
                output.append("</div>");
                output.append("</td></tr></table>\n");
                output.append("</td></tr></table>\n");
                output.append("</div>\n");
                output.append("<div id=\"searchBarOpener\" style=\"background-color: #6495ed; border-right: 3 double gray; display: none; height: 100%; overflow-y: auto\">\n");
                output.append("<img style=\"cursor:pointer\" src=\"WEB-CORE/elements/advancedsearch/images/showsearchbar.gif\" onclick=\"toggleSearch( null, null );\" title=\"Click to Show/Hide the side bar\" border=0>");
                output.append("<span style=\"padding-left: 3px; color: white; font-weight: bold; writing-mode:tb-rl; \">&nbsp;&nbsp;").append(tp.translate("Search Bar")).append("</span>");
                output.append("</div>\n");
                if (!pageData.getProperty("file").contains("WEB-CORE/elements/gwt/arraysource.jsp")) {
                    output.append(this.getFinalJavaScript(cookieKey, lastSearchType, userConfig));
                }
            }
            return output.toString();
        }
        return "&nbsp;";
    }

    private StringBuffer getContentPanes(String lastSearchType, String cookieKey, PropertyList userConfig, int width) {
        StringBuffer output = new StringBuffer();
        String[] type = new String[]{"basic", "query", "category", "scan", "folder", "adhoc", "versionfilter"};
        String[] defaultTitle = new String[]{"Search", "Search By Query", "Search By Category", "Search By Scanned Ids", "Search By Folder", "Adhoc Search", "Version Filter"};
        String[] className = new String[]{"com.labvantage.sapphire.pageelements.advancedsearch.SearchByBasic", "com.labvantage.sapphire.pageelements.advancedsearch.SearchByQuery", "com.labvantage.sapphire.pageelements.advancedsearch.SearchByCategory", "com.labvantage.sapphire.pageelements.advancedsearch.SearchByScan", "com.labvantage.sapphire.pageelements.advancedsearch.SearchByFolder", "com.labvantage.sapphire.pageelements.advancedsearch.SearchByAdhoc", "com.labvantage.sapphire.pageelements.advancedsearch.VersionStatusFilter"};
        ArrayList<String> contentTypes = new ArrayList<String>();
        contentTypes.add("Basic Search");
        contentTypes.add("Queries");
        contentTypes.add("Categories");
        contentTypes.add("Scan List");
        contentTypes.add("Folders");
        contentTypes.add("Adhoc Search");
        contentTypes.add("Version Filter");
        boolean showCollapseExpand = this.element.getProperty("showcollapseexpand", "Y").equals("Y");
        PropertyListCollection sequence = this.element.getCollection("sequence");
        output.append("<table  border=0  width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">");
        String expandFlags = userConfig.getProperty("as_expand_" + cookieKey);
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList modules = this.requestContext.getPropertyList("hasModule");
        boolean hasAdhocModule = modules != null && "true".equals(modules.getProperty("AdhocQuery"));
        boolean isVersionedFlag = sdcProcessor.getProperty(this.sdcid, "versionedflag").equals("Y");
        boolean canShowAdhocModule = "Y".equals(sdcProcessor.getProperty(this.sdcid, "searchableflag")) && hasAdhocModule;
        this.element.setProperty("showtopsearch", "Y".equals(this.requestContext.getPropertyList().getProperty("modernlayout")) ? "Y" : "N");
        if (sequence == null || sequence.size() == 0) {
            for (int index = 0; index < 7; ++index) {
                boolean show;
                if (this.props[index].getProperty("show").equals("N") || !(show = this.canShowSearchType(isVersionedFlag, canShowAdhocModule, index))) continue;
                if ("T".equals(this.props[index].getProperty("displayoption")) || "B".equals(this.props[index].getProperty("displayoption"))) {
                    this.element.setProperty("showtopsearch", "Y");
                }
                String title = this.props[index].getProperty("title", this.getTranslationProcessor().translate(defaultTitle[index]));
                boolean expand = expandFlags.length() <= index || expandFlags.charAt(index) == 'Y';
                output.append(this.showContentPane(this.sdcid, cookieKey, className[index], this.props[index], title, expand, index, lastSearchType.equals(type[index]), showCollapseExpand, 0, width));
            }
        } else {
            for (PropertyList sequencePropertyList : sequence) {
                String contentType;
                int index;
                String show = sequencePropertyList.getProperty("show");
                if (show.equals("N") || (index = contentTypes.indexOf(contentType = sequencePropertyList.getProperty("contentname"))) < 0 || !this.canShowSearchType(isVersionedFlag, canShowAdhocModule, index)) continue;
                if ("T".equals(this.props[index].getProperty("displayoption")) || "B".equals(this.props[index].getProperty("displayoption"))) {
                    this.element.setProperty("showtopsearch", "Y");
                }
                String title = sequencePropertyList.getProperty("title", this.getTranslationProcessor().translate(defaultTitle[index]));
                String maxHeight = sequencePropertyList.getProperty("maxheight");
                int maxHeightN = 0;
                if (maxHeight.length() > 0) {
                    try {
                        maxHeightN = Integer.parseInt(maxHeight);
                    }
                    catch (Exception e) {
                        this.logger.error("maxheight is not a valid number.");
                    }
                }
                boolean expand = expandFlags.length() > index ? expandFlags.charAt(index) == 'Y' : sequencePropertyList.getProperty("initiallyexpanded", "Y").equals("Y");
                output.append(this.showContentPane(this.sdcid, cookieKey, className[index], this.props[index], title, expand, index, lastSearchType.equals(type[index]), showCollapseExpand, maxHeightN, width));
            }
        }
        output.append("<tr class=\"modern_search_titlebar_row\">");
        output.append("<td colspan=2 nowrap  valign=middle>&nbsp;</td>");
        output.append("</tr>");
        output.append("</table>");
        return output;
    }

    private boolean canShowSearchType(boolean versionedFlag, boolean canShowAdhocModule, int index) {
        boolean show = true;
        if (index == 5) {
            show = canShowAdhocModule;
        } else if (index == 6) {
            show = versionedFlag;
        }
        return show;
    }

    private StringBuffer showContentPane(String sdcid, String cookieKey, String className, PropertyList props, String title, boolean expand, int index, boolean islastSearchType, boolean showCollapseExpand, int maxHeight, int width) {
        StringBuffer output = new StringBuffer();
        try {
            SearchContent content = (SearchContent)Class.forName(className).newInstance();
            content.init(this.pageContext, islastSearchType, props, sdcid, this.element, cookieKey, maxHeight, this.getTranslationProcessor());
            String html = content.getHtml();
            if (html != null && html.length() > 0) {
                String displayOption;
                String display;
                if (index == 3) {
                    title = title + " <span id=\"countdiv\"></span>";
                }
                String string = display = "T".equals(displayOption = props.getProperty("displayoption")) ? "none" : "";
                if (className.equals("com.labvantage.sapphire.pageelements.advancedsearch.SearchByBasic")) {
                    output.append("\n<script>\n");
                    output.append("var showTopBasicSearch=" + ("S".equals(displayOption) ? "false" : "true") + ";\n");
                    output.append("var showTextSearchOption=" + ("O".equals(props.getProperty("textsearch")) ? "true" : "false") + ";\n");
                    output.append("var basicSearchTitle='" + title + "';\n");
                    output.append("</script>\n");
                } else if (className.equals("com.labvantage.sapphire.pageelements.advancedsearch.SearchByQuery")) {
                    output.append("\n<script>\n");
                    output.append("var showTopQuerySearch=" + ("S".equals(displayOption) ? "false" : "true") + ";\n");
                    output.append("var querySearchDisplayOption='" + props.getProperty("topsearchoption") + "';\n");
                    output.append("var querySearchTitle='" + title + "';\n");
                    output.append("</script>\n");
                }
                output.append(HttpUtil.getEncryptionJS(""));
                output.append("<tr style=\"display:" + display + ";\" class=\"modern_search_titlebar_row\">");
                if ("".equals(display)) {
                    this.hasSideBarSearchContent = true;
                }
                output.append("<td nowrap class=\"modern_search_titlebar subtitle\" valign=middle >").append(title).append("</td>");
                if (showCollapseExpand) {
                    output.append("<td align=\"center\" valign=middle class=\"modern_search_expandcollapse\"><img index=\"");
                    output.append(index).append("\" style=\"cursor:pointer;\" id=\"togglecontentimage").append(index);
                    output.append("\" expanded=\"").append(expand ? "Y" : "N").append("\" ").append(" width=\"8px\" height=\"8px\" src=\"");
                    output.append(expand ? this.closePaneImg : this.openPaneImg).append("\" onclick=\"toggleContent( ").append(index).append(" )\">");
                    output.append("</td>");
                } else {
                    output.append("<td align=\"right\" valign=middle class=\"modern_search_expandcollapse\"></td>");
                }
                output.append("</tr>");
                output.append("<tr style=\"display:" + display + ";\">");
                output.append("<td colspan=\"2\" class=\"modern_search_contentcell\">");
                output.append("<div style=\"width:").append(width - 5).append("px;display:").append(expand ? "block" : "none").append("\" class=\"modern_search_contentbar\" id=\"search_contentbar").append(index).append("\">").append(html).append("</div>");
                output.append("</td>");
                output.append("</tr>");
            }
        }
        catch (Exception e) {
            this.logger.error("Unable to load content class: " + className, e);
        }
        return output;
    }

    private StringBuffer getFinalJavaScript(String cookieKey, String lastSearchType, PropertyList userConfig) {
        StringBuffer output = new StringBuffer();
        output.append("<script>\n");
        output.append("var hasSideBarSearchContent=" + this.hasSideBarSearchContent + ";\n");
        try {
            String defaultCategory = this.props[2].getProperty("default");
            String defaultQuery = this.props[1].getProperty("default");
            PropertyList plPagedata = (PropertyList)this.pageContext.getAttribute("pagedata", 2);
            if (plPagedata != null && lastSearchType.length() == 0 && plPagedata.getProperty("cascadequeryid").equalsIgnoreCase("") && plPagedata.getProperty("keyid1").equalsIgnoreCase("") && plPagedata.getProperty("queryid").equalsIgnoreCase("") && plPagedata.getProperty("queryfrom").equalsIgnoreCase("") && plPagedata.getProperty("querywhere").equalsIgnoreCase("")) {
                if ((this.onloadAction.equals("") || this.onloadAction.equals(this.ONLOAD_DEFAULTQUERY)) && defaultQuery.length() > 0) {
                    output.append("if ( window.runDefaultQuery ) sapphire.events.attachEvent( window, \"onload\", runDefaultQuery );\n");
                } else if ((this.onloadAction.equals("") || this.onloadAction.equals(this.ONLOAD_DEFAULTCATEGORY)) && defaultCategory.length() > 0) {
                    output.append("if ( window.runDefaultCategory )  sapphire.events.attachEvent( window, \"onload\", runDefaultCategory );\n");
                } else if (this.onloadAction.equals(this.ONLOAD_FIRSTFOLDER)) {
                    output.append("if ( window.runFirstFolder )  sapphire.events.attachEvent( window, \"onload\", runFirstFolder );\n");
                } else if (defaultQuery.length() > 0) {
                    output.append("if ( window.runDefaultQuery ) sapphire.events.attachEvent( window, \"onload\", runDefaultQuery );\n");
                } else if (defaultCategory.length() > 0) {
                    output.append("if ( window.runDefaultCategory )  sapphire.events.attachEvent( window, \"onload\", runDefaultCategory );\n");
                }
            }
        }
        catch (Exception ex) {
            this.logger.error("AdvancedSearch.getSearchBarFooterHtml()-> Got html for the footer : " + ex, ex);
        }
        output.append("CONTENT_TYPES = 7;\n");
        String callbackfunction = this.element.getProperty("callback", "showResult");
        output.append("searchCallbackFunction = '").append(callbackfunction).append("';\n");
        if (this.element.getProperty("showinitially").equals("N") || userConfig.getProperty("as_searchbar_" + cookieKey).equalsIgnoreCase("N")) {
            output.append("toggleSearch(null, null );\n");
            output.append(" var container = document.getElementById( 'searchbarContainer' );\n ");
            output.append(" if ( container != null ){\n ");
            output.append(" container.style.display = 'none';\n ");
            output.append(" }\n ");
        }
        output.append("if (typeof(as_cookieKey) != 'undefined'){\n");
        output.append("as_cookieKey = \"").append(cookieKey).append("\";\n");
        output.append("}else{\n");
        output.append("var as_cookieKey = \"").append(cookieKey).append("\";\n");
        output.append("};\n");
        output.append("</script>\n");
        return output;
    }
}

