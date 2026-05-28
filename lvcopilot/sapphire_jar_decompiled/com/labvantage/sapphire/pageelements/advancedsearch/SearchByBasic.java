/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.advancedsearch;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.pageelements.advancedsearch.SearchContent;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.scrollpanel.ScrollPanel;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SearchByBasic
extends SearchContent {
    String contentName = "basic";

    @Override
    public String getHtml() {
        SDCProcessor sdcProcessor;
        String temp;
        StringBuffer output = new StringBuffer("");
        Button okButton = new Button(this.pageContext);
        okButton.setAppearance("smalltext");
        okButton.setMargin("none");
        okButton.setAction("openBasic()");
        if (this.browser.isWebkit()) {
            okButton.setStyle("height:18px;padding-left:2px;padding-right:2px;");
        }
        boolean nullSearch = false;
        RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        String searchText = userConfig.getProperty("as_basicsearch_" + this.cookieKey);
        if ("(nullsearch)".equals(searchText)) {
            nullSearch = true;
            searchText = "";
        }
        String textsearch = this.contentProperties.getProperty("textsearch", "N");
        try {
            Indexer indexer = Indexer.getInstance(requestContext.getProperty("databaseid"));
            if (indexer == null || !indexer.isSearching()) {
                textsearch = "N";
            }
        }
        catch (Exception e) {
            textsearch = "N";
        }
        okButton.setImg("WEB-CORE/imageref/flat/16/flat_black_search.svg");
        okButton.setAppearance("ribbonsmall");
        okButton.setStyle("height:22px;width:25px; border: 1px solid rgba(26,26,26, 0.4); opacity:0.6; border-radius:3px; padding:2px 2px 2px 2px;");
        boolean enableSuggest = "Y".equals(this.contentProperties.getProperty("enablesuggest", "N"));
        PropertyList pagedata = requestContext.getPropertyList("pagedata");
        String restrictivewhere = pagedata != null && pagedata.getProperty("restrictivewhere").length() > 0 ? requestContext.getPropertyList("pagedata").getProperty("restrictivewhere") : "";
        PropertyList pagedirectives = requestContext.getPropertyList("pagedirectives");
        String string = temp = pagedirectives == null ? "" : pagedirectives.getProperty("restrictivewhere");
        if (temp.length() > 0) {
            temp = EncryptDecrypt.unobfsql(temp);
            String mw = pagedata.getProperty("mergewhere", "Merge");
            if (!mw.equalsIgnoreCase("ignore")) {
                if (mw.equalsIgnoreCase("merge") && restrictivewhere.length() > 0) {
                    restrictivewhere = "( " + temp + ") AND ( " + restrictivewhere + " )";
                } else if (restrictivewhere.length() == 0 || mw.equalsIgnoreCase("override")) {
                    restrictivewhere = temp;
                }
            }
        }
        if ("Y".equals((sdcProcessor = new SDCProcessor(this.pageContext)).getProperty(this.sdcid, "versionedflag"))) {
            PropertyList versionstatusPL = requestContext.getPropertyList("pagedata").getPropertyList("versionstatus");
            String versionstatusIn = "";
            if (versionstatusPL != null) {
                versionstatusIn = ("Y".equals(versionstatusPL.getProperty("provisional")) ? ",'P'" : "") + ("Y".equals(versionstatusPL.getProperty("current")) ? ",'C'" : "") + ("Y".equals(versionstatusPL.getProperty("active")) ? ",'A'" : "") + ("Y".equals(versionstatusPL.getProperty("expired")) ? ",'E'" : "");
            }
            if (versionstatusIn.indexOf(",") == 0) {
                restrictivewhere = restrictivewhere.length() > 0 ? "(" + restrictivewhere + ") AND ( versionstatus in ( " + versionstatusIn.substring(1) + ") )" : "versionstatus in (" + versionstatusIn + ")";
            }
        }
        restrictivewhere = EncryptDecrypt.obfsql(restrictivewhere);
        output.append("<table id=\"basicsearch_row\" style=\"padding-top:3px;padding-bottom:2px\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n").append("<form id=\"basicsearchform\" action=\"javascript:openBasic();\">\n").append("    <tr>").append("       <td width=\"10px\"><img style=\"margin:2px;visibility: hidden\" id=\"basicpointer\" src=\"WEB-CORE/elements/images/selected_item.gif\"></td>").append("       <td width=\"140px\"><input value=\"").append(this.isLastSearchType ? SafeHTML.encodeForHTMLAttribute(searchText) : "").append("\" class=\"search_inputfield\" id=\"searchtext\"" + (enableSuggest ? "onkeypress=\"showSuggestion( { 'sdcid' : '" + this.sdcid + "','restrictivewhere':'" + restrictivewhere + "' } )\"" : "") + " type=\"text\" onkeydown=\"if ( event.keyCode == 13 ) javascript:openBasic();\"/></td>\n").append("       <td width=\"25px\" valign=\"center\" align=\"right\">").append(okButton.getHtml()).append("</td>").append("       <td width=\"10px\" valign=\"center\" align=\"right\">").append("&nbsp;").append("</td>").append("    </tr>\n");
        if (textsearch.equals("O")) {
            TranslationProcessor tp = new TranslationProcessor(this.pageContext);
            output.append("    <tr>").append("       <td height=\"20px\">&nbsp;</td><td colspan=\"2\"valign=\"middle\" align=\"left\"><input type=\"checkbox\" id=\"textsearch\"/>&nbsp;<label for=\"textsearch\">").append(tp.translate("Text Search")).append("</label>").append("</td>").append("    </tr>\n");
        }
        output.append("</form>\n").append("</table>\n");
        String querywhere = this.contentProperties.getProperty("whereclause");
        if (querywhere.length() > 0) {
            if (!EncryptDecrypt.isObfuscating()) {
                querywhere = StringUtil.replaceAll(querywhere, "'", "\\'");
                querywhere = StringUtil.replaceAll(querywhere, "[]", "' + searchid + '");
            }
        } else {
            querywhere = EncryptDecrypt.isObfuscating() ? "lower( " + sdcProcessor.getProperty(this.sdcid, "tableid") + "." + sdcProcessor.getProperty(this.sdcid, "keycolid1") + " ) like lower( '%[]%' ) or lower( " + sdcProcessor.getProperty(this.sdcid, "desccol") + " ) like lower( '%[]%' )" : "lower( " + sdcProcessor.getProperty(this.sdcid, "tableid") + "." + sdcProcessor.getProperty(this.sdcid, "keycolid1") + " ) like lower( \\'%' + searchid + '%\\' ) or lower( " + sdcProcessor.getProperty(this.sdcid, "desccol") + " ) like lower( \\'%' + searchid + '%\\' )";
        }
        String querysearchJS = "var querywhere ='" + EncryptDecrypt.obfsql(querywhere) + "';\ndoCallback( 'basic', searchid, '" + this.sdcid + "','" + EncryptDecrypt.obfsql(sdcProcessor.getProperty(this.sdcid, "tableid")) + "', querywhere );\n";
        String textsearchJS = "doCallback( 'text', '', '" + this.sdcid + "', '', '', '', '', '', '', searchid );\n";
        output.append("<script>\n").append("var basicsearch_sdcid='" + this.sdcid + "';\n").append("var basicsearch_pagerestrictive='" + SafeHTML.encodeForJavaScript(EncryptDecrypt.obfsql(restrictivewhere)) + "';\n").append("var basicsearch_enablesuggest=" + enableSuggest + ";\n").append("document.getElementById('searchtext').focus();\n").append("function openBasic( intext ) {\n").append("  var searchtext = typeof( intext ) != 'string' ? document.getElementById('searchtext').value : intext;\n").append("  document.getElementById('searchtext').value = searchtext;\n").append("  var searchid = replaceSingeQuot( searchtext );\n").append("  searchid = searchid.replace(/^\\s*|\\s*$/g,\"\");\n").append("  if ( typeof( listtopsearch ) != 'undefined' && typeof( listtopsearch.setBasicLastSearch ) != 'undefined' ) { listtopsearch.setBasicLastSearch( searchtext );}").append("  document.getElementById( \"basicsearch_row\" ).style.backgroundColor='#eff6fe';document.getElementById( \"basicsearch_row\" ).style.border='1px solid #43a2d6';\n").append("  document.getElementById( \"basicpointer\" ).style.visibility='visible';\n").append("  sapphire.userConfig.set( \"as_basicsearch_").append(this.cookieKey).append("\",  searchid==''?'(nullsearch)':searchid )\n;");
        if (textsearch.equals("A")) {
            output.append(textsearchJS);
        } else if (textsearch.equals("O")) {
            output.append("  if ( !document.getElementById('textsearch').checked ) {\n").append(querysearchJS).append("  } else {\n").append(textsearchJS).append("  }\n");
        } else {
            output.append(querysearchJS);
        }
        output.append("}\n");
        if (this.isLastSearchType && (searchText.length() > 0 || nullSearch)) {
            output.append("sapphire.events.attachEvent( window, 'onload', openBasic );\n");
        }
        output.append("</script>");
        if (this.maxHeight > 0) {
            ScrollPanel scrollPanel = new ScrollPanel(this.pageContext);
            scrollPanel.setId(this.contentName);
            scrollPanel.setMaxHeight(this.maxHeight);
            scrollPanel.setModernScroll(true);
            scrollPanel.setContent(output);
            return scrollPanel.getHtml();
        }
        return output.toString();
    }
}

