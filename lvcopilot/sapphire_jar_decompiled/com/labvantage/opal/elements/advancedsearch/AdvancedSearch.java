/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.advancedsearch;

import com.labvantage.opal.elements.advancedsearch.DynamicGroupBy;
import com.labvantage.opal.elements.advancedsearch.SearchBarDragDropHtml;
import com.labvantage.opal.elements.advancedsearch.SearchByScan;
import com.labvantage.sapphire.pageelements.search.Search;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.xml.PropertyList;

public class AdvancedSearch
extends BaseElement {
    private String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private TranslationProcessor __Tp;

    @Override
    public String getHtml() {
        this.__Tp = new TranslationProcessor(this.pageContext);
        Search eSearch = new Search(this.pageContext, this.getConnectionId());
        StringBuffer sbHtml = new StringBuffer("");
        StringBuffer sbFinalHtml = new StringBuffer("");
        String dragEnabledHtml = "";
        PropertyList plLayout = null;
        PropertyList sidebar = null;
        try {
            plLayout = (PropertyList)this.pageContext.getAttribute("layout", 2);
            if (plLayout != null) {
                sidebar = plLayout.getPropertyList("sidebar");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        String showSideBar = "N";
        if (sidebar != null) {
            showSideBar = sidebar.getProperty("showsidebar");
        }
        String refElement = this.element.getProperty("referenceelement").equalsIgnoreCase("") ? "contentdiv" : this.element.getProperty("referenceelement");
        String container = this.element.getProperty("searchbarcontainer").equalsIgnoreCase("") ? "searchtd" : this.element.getProperty("searchbarcontainer");
        String height = this.element.getProperty("height").equalsIgnoreCase("") ? "500" : this.element.getProperty("height");
        String width = this.element.getProperty("width").equalsIgnoreCase("") ? "180" : this.element.getProperty("width");
        String top = this.element.getProperty("top").equalsIgnoreCase("") ? "138" : this.element.getProperty("top");
        String left = this.element.getProperty("left").equalsIgnoreCase("") ? "128" : this.element.getProperty("left");
        String showInitially = this.element.getProperty("showinitially");
        if (this.element.getProperty("sdcid").equalsIgnoreCase("")) {
            return this.__Tp.translate("SDCId not defined for the AdvancedSearch element. Cannot continue.");
        }
        if (showSideBar.equalsIgnoreCase("N") || showSideBar.equalsIgnoreCase("")) {
            left = "0";
        }
        this.logger.debug("OPAL_INFO: AdvancedSearch.getHtml()-> show=" + this.element.getProperty("show") + "  enabledocking=" + this.element.getProperty("enabledocking"));
        if (this.element.getProperty("show").equalsIgnoreCase("") || this.element.getProperty("show").equalsIgnoreCase("Y")) {
            if (this.element.getProperty("showgroupby").equalsIgnoreCase("") || this.element.getProperty("showgroupby").equalsIgnoreCase("Y")) {
                DynamicGroupBy dynamicGroupBy = new DynamicGroupBy(this.pageContext, this.getConnectionId());
                dynamicGroupBy.setElementProperties(this.element);
                sbHtml.append(dynamicGroupBy.getHtml());
                dynamicGroupBy = null;
            }
            eSearch.setElementProperties(this.element);
            sbHtml.append("\n<!-- START PAINTING THE CORE SEARCH ELEMENT -->\n");
            sbHtml.append(eSearch.getHtml());
            sbHtml.append("\n<!-- END PAINTING THE CORE SEARCH ELEMENT -->\n");
            this.logger.debug("OPAL_INFO: AdvancedSearch.getHtml()-> Got html from the search element as : " + sbHtml);
            PropertyList plScanSearch = this.element.getPropertyList("scansearch");
            if (plScanSearch == null || plScanSearch != null && !plScanSearch.getProperty("show").equals("N")) {
                SearchByScan scanSearch = new SearchByScan(this.pageContext, this.getConnectionId());
                scanSearch.setElementProperties(this.element);
                String scanSearchHtml = scanSearch.getHtml();
                if (scanSearchHtml.length() > 0) {
                    String title = this.element.getPropertyList("scansearch") == null ? this.__Tp.translate("Search By Scanned Ids:") : this.element.getPropertyList("scansearch").getProperty("title");
                    sbHtml.append("<p class=\"search_header\"><b>" + title + "</b> <span id=countdiv></span></p>");
                    sbHtml.append(scanSearchHtml);
                }
                this.logger.debug("OPAL_INFO: AdvancedSearch.getHtml()-> Got html from scansearch as : " + scanSearchHtml);
                scanSearch = null;
            }
            plScanSearch = null;
            if (this.element.getProperty("enabledocking").equalsIgnoreCase("Y") || this.element.getProperty("enabledocking").equalsIgnoreCase("")) {
                this.logger.debug("OPAL_INFO: AdvancedSearch.getHtml()-> About to get the dragdrop code");
                dragEnabledHtml = this.addDragDropSupport(sbHtml.toString(), showSideBar, showInitially, refElement, container, height, width, top, left, this.pageContext);
                this.logger.debug("OPAL_INFO: AdvancedSearch.getHtml()-> Got the dragdrop code as: " + dragEnabledHtml);
                sbHtml = new StringBuffer(dragEnabledHtml);
            }
            String headerHtml = this.getSearchBarHeaderHtml();
            String footerHtml = this.getSearchBarFooterHtml();
            this.logger.debug("OPAL_INFO: AdvancedSearch.getHtml()-> Got html for the header : " + headerHtml);
            this.logger.debug("OPAL_INFO: AdvancedSearch.getHtml()-> Got html for the footer : " + footerHtml);
            sbFinalHtml.append(headerHtml);
            sbFinalHtml.append(sbHtml);
            sbFinalHtml.append(footerHtml);
            sbHtml = new StringBuffer(sbFinalHtml.toString());
        } else {
            sbHtml.append("&nbsp;");
        }
        plLayout = null;
        eSearch = null;
        return sbHtml.toString();
    }

    private String addDragDropSupport(String html, String showSideBar, String showInitially, String refElement, String container, String height, String width, String top, String left, PageContext pageContext) {
        return SearchBarDragDropHtml.addDragDropSupport(html, showSideBar, showInitially, refElement, container, height, width, top, left, pageContext);
    }

    private String getSearchBarHeaderHtml() {
        StringBuffer sbHtml = new StringBuffer("");
        if (this.element.getProperty("enabledocking").equalsIgnoreCase("") || this.element.getProperty("enabledocking").equalsIgnoreCase("Y")) {
            sbHtml.append("         <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" height=\"100%\" class=searchtable_docking>\n");
        } else {
            sbHtml.append("         <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" height=\"100%\" class=searchtable_nondocking>\n");
        }
        sbHtml.append("                 <tr>\n");
        sbHtml.append("                     <td width=\"100%\" height=\"50%\" align=\"left\" valign=\"top\">\n");
        return sbHtml.toString();
    }

    private String getSearchBarFooterHtml() {
        StringBuffer sbHtml = new StringBuffer("");
        sbHtml.append("                     </td>\n");
        sbHtml.append("                 </tr>\n");
        sbHtml.append("                 </table>\n");
        PropertyList plPagedata = null;
        String defaultQuery = "";
        String defaultCategory = "";
        try {
            defaultQuery = this.element.getPropertyList("querysearch") != null ? this.element.getPropertyList("querysearch").getProperty("default") : "";
            defaultCategory = this.element.getPropertyList("categorysearch") != null ? this.element.getPropertyList("categorysearch").getProperty("default") : "";
            plPagedata = (PropertyList)this.pageContext.getAttribute("pagedata", 2);
            if (plPagedata != null && plPagedata.getProperty("keyid1").equalsIgnoreCase("") && plPagedata.getProperty("cascadequeryid").equalsIgnoreCase("") && plPagedata.getProperty("queryid").equalsIgnoreCase("") && plPagedata.getProperty("queryfrom").equalsIgnoreCase("") && plPagedata.getProperty("querywhere").equalsIgnoreCase("")) {
                sbHtml.append("<script>\n");
                sbHtml.append(" function runDefaultQuery(){\n");
                sbHtml.append("     startSearch( 'query', '" + defaultQuery + "');\n");
                sbHtml.append(" }\n\n");
                sbHtml.append(" function runDefaultCategory(){\n");
                sbHtml.append("     startSearch( 'category', '" + defaultCategory + "');\n");
                sbHtml.append(" }\n");
                if (!defaultQuery.equalsIgnoreCase("")) {
                    sbHtml.append(" sapphire.events.attachEvent( window, \"onload\", runDefaultQuery );\n");
                } else if (!defaultCategory.equalsIgnoreCase("")) {
                    sbHtml.append(" sapphire.events.attachEvent( window, \"onload\", runDefaultCategory );\n");
                }
                sbHtml.append("</script>\n");
            }
        }
        catch (Exception ex) {
            this.logger.error("AdvancedSearch.getSearchBarFooterHtml()-> Got html for the footer : " + ex, ex);
        }
        return sbHtml.toString();
    }
}

