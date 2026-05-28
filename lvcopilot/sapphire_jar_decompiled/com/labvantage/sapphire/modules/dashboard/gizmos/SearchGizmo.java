/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.search.Indexer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseGizmo;
import sapphire.xml.PropertyList;

public class SearchGizmo
extends BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";
    public static final String SEARCHTEXT = "Search LabVantage";
    private boolean searchingEnabled = false;
    private String searchBoxText = "";
    private String searchBoxPage = "";
    private String searchBoxWidth = "150";
    private boolean showSuggest = true;
    private boolean showSearchBox = false;
    private boolean showListOnFocus = false;
    private boolean showRecentItems = false;
    private boolean showMenuLinks = false;

    @Override
    public boolean init() {
        boolean show;
        block6: {
            this.setRefreshOnResize(true);
            this.setTimeout(-1);
            PropertyList searchinputbox = this.element.getPropertyList("searchinputbox");
            if (searchinputbox == null) {
                searchinputbox = new PropertyList();
            }
            this.showSearchBox = searchinputbox.getProperty("expanded", "Y").equals("Y");
            this.searchBoxText = searchinputbox.getProperty("text", this.searchBoxText);
            this.searchBoxWidth = searchinputbox.getProperty("width", this.searchBoxWidth);
            PropertyList searchsuggest = this.element.getPropertyList("searchsuggest");
            if (searchsuggest == null) {
                searchsuggest = new PropertyList();
            }
            this.showSuggest = searchsuggest.getProperty("show", "Y").equals("Y");
            this.showListOnFocus = searchsuggest.getProperty("showlistonfocus", "Y").equals("Y");
            this.showRecentItems = this.element.getProperty("showrecentitems", "Y").equals("Y");
            this.showMenuLinks = this.element.getProperty("showmenulinks", "Y").equals("Y");
            show = true;
            try {
                Indexer indexer = Indexer.getInstance(this.connectionInfo.getDatabaseId());
                if (indexer != null) {
                    this.searchBoxPage = indexer.getSearchPolicy().getProperty("searchboxpage", "rc?command=page&page=Search");
                    if (indexer.isSearching()) {
                        this.searchingEnabled = true;
                        this.searchBoxText = this.searchBoxText.length() > 0 ? this.searchBoxText : indexer.getSearchPolicy().getProperty("searchboxtext", this.getTranslationProcessor().translate("Search System"));
                        boolean bl = indexer.getSearchPolicy().getProperty("searchboxsuggest", "Y").equals("Y");
                    }
                }
            }
            catch (SapphireException e) {
                if (this.showRecentItems || this.showMenuLinks) break block6;
                show = false;
            }
        }
        return show;
    }

    @Override
    public String getHtml() {
        return LABVANTAGE_CVS_ID;
    }

    @Override
    public String getScript() {
        return "searchGizmo.init( " + (this.searchingEnabled ? "true" : "false") + ",'" + this.searchBoxPage + "','" + this.searchBoxWidth + "');searchGizmo.register('" + this.elementid + "', " + (this.showSearchBox ? "true" : "false") + ", " + (this.showSuggest ? "true" : "false") + ", " + (this.showListOnFocus ? "true" : "false") + ", " + (this.showRecentItems ? "true" : "false") + ", " + (this.showMenuLinks ? "true" : "false") + ");";
    }

    @Override
    public String getURL() {
        BaseGizmo.GizmoLocation gizmoLocation = this.getGizmoLocation();
        switch (gizmoLocation) {
            case TOPBAR: {
                if (this.browser.isMobile()) {
                    return "javascript:sapphire.page.navigate( '" + this.searchBoxPage + "', true );";
                }
                return this.showSearchBox ? "javascript:searchGizmo.searchBoxClick( '" + this.elementid + "' );" : "javascript:searchGizmo.toggleSearchBox( '" + this.elementid + "' );";
            }
            case SIDEBAR: {
                return "javascript:sapphire.page.navigate( '" + this.searchBoxPage + "', true );";
            }
        }
        return LABVANTAGE_CVS_ID;
    }

    @Override
    public String getHelpText() {
        if (this.getTranslationProcessor() != null) {
            return this.getTranslationProcessor().translate("Search");
        }
        return "Search";
    }

    @Override
    public String getIconHtml() {
        StringBuffer html = new StringBuffer();
        html.append(super.getIconHtml());
        html.append("<div id=\"" + this.elementid + "_div\" style=\"display:none;position: absolute;left: -190px;top: 5px;\">");
        html.append(this.getHtml());
        html.append("</div>");
        return html.toString();
    }

    @Override
    public String getTitle() {
        return this.searchBoxText;
    }

    @Override
    public String getIcon() {
        TranslationProcessor tp = this.getTranslationProcessor();
        if (this.element.getProperty("imagetitle").length() == 0) {
            this.element.setProperty("imagetitle", this.getHelpText());
        }
        return "<script src=\"WEB-CORE/modules/dashboard/scripts/searchgizmo.js\"></script><div style=\"display:" + (this.browser.isMobile() ? "none" : "inline-flex") + "\">  <input type=\"text\" id=\"searchboxquery_" + this.elementid + "\" placeholder=\"" + this.searchBoxText + "\" style=\"" + (this.showSearchBox ? "width:" + this.searchBoxWidth + "px;height:14px;display:block" : "width:1px;height:14px;display:none") + "\" onclick=\"searchGizmo.searchBoxClick('" + this.elementid + "')\" onblur=\"searchGizmo.searchBoxBlur('" + this.elementid + "')\" onkeyup=\"searchGizmo.searchBoxKeyUp('" + this.elementid + "',event)\"/></div><div style=\"display:inline-flex;padding-left: 3px\">" + this.getImage(this.searchBoxText, this.getGizmoStyle().size).getHtml() + "</div><div id=\"searchpanel_" + this.elementid + "\" style=\"z-index:1000;display:none;position:absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" class=\"search_table\" style=\"min-width: " + this.searchBoxWidth + "px;\">  <tr id=\"searchrecentitemsrow_" + this.elementid + "\">    <td align=\"left\" style=\"vertical-align: top;\">      <table cellspacing=\"0\" cellpadding=\"0\" style=\"width: 100%;\" id=\"searchrecentitemstable_" + this.elementid + "\">        <tr>          <td class=\"search_headerrow\"><span id=\"searchrecentitemstext_" + this.elementid + "\">" + tp.translate("Recent Items") + "</span></td>        </tr>      </table>    </td>  </tr>  <tr id=\"searchmenulinksrow_" + this.elementid + "\" style=\"display:none\">    <td align=\"left\" style=\"vertical-align: top;\">      <table cellspacing=\"0\" cellpadding=\"0\" style=\"width: 100%;\" id=\"searchmenulinkstable_" + this.elementid + "\">        <tr>          <td class=\"search_headerrow\"><span id=\"searchmenulinkstext_" + this.elementid + "\">" + tp.translate("Matching Menu Links") + "</span></td>        </tr>      </table>    </td>  </tr>  <tr id=\"searchlatestsearchesrow_" + this.elementid + "\">    <td align=\"left\" style=\"vertical-align: top;\">      <table cellspacing=\"0\" cellpadding=\"0\" style=\"width: 100%;\" id=\"searchlatestsearchestable_" + this.elementid + "\">        <tr>          <td class=\"search_headerrow\"><span id=\"searchlatestsearchestext_" + this.elementid + "\">" + tp.translate("Latest Searches") + "</span></td>        </tr>      </table>    </td>  </tr>  <tr id=\"searchwithrow_" + this.elementid + "\">    <td align=\"left\" style=\"vertical-align: top;\">      <table cellspacing=\"0\" cellpadding=\"0\" style=\"width: 100%;\" id=\"searchwithtable_" + this.elementid + "\">        <tr style=\"display:none\"><td colspan=\"10\"></td></tr>      </table>    </td>  </tr></table></div>";
    }

    @Override
    public String getDefaultImageSrc() {
        return this.elementid.equals("genericlayout") ? "WEB-CORE/images/png/Search.png" : "FlatWhiteSearch";
    }

    public static SearchGizmo getSearchGizmo(PageContext pageContext, String gizmodefid, PropertyList gizmoproperties) {
        SearchGizmo searchGizmo = new SearchGizmo();
        searchGizmo.setPageContext(pageContext);
        searchGizmo.setRequest((HttpServletRequest)pageContext.getRequest());
        searchGizmo.setGizmoDefId(gizmodefid);
        searchGizmo.setElementid(gizmodefid);
        searchGizmo.elementType = "searchgizmo";
        searchGizmo.setElementProperties(gizmoproperties);
        searchGizmo.setParameters(null);
        searchGizmo.setBaseProperties();
        searchGizmo.init();
        return searchGizmo;
    }
}

