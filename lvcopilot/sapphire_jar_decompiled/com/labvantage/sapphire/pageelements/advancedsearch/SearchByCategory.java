/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.advancedsearch;

import com.labvantage.sapphire.pageelements.advancedsearch.SearchContent;
import com.labvantage.sapphire.pageelements.scrollpanel.ScrollPanel;
import com.labvantage.sapphire.pageelements.search.SearchUtil;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;

public class SearchByCategory
extends SearchContent {
    String contentName = "category";

    @Override
    public String getHtml() {
        StringBuffer output = new StringBuffer();
        String categoryquerywhere = "category.sdcid='" + this.sdcid + "'";
        String filterlist = this.contentProperties.getProperty("filter");
        if (filterlist != null && filterlist.trim().length() > 0) {
            categoryquerywhere = categoryquerywhere + " and categoryid in " + SearchUtil.toQueryInClause(filterlist);
        }
        SDIProcessor sdip = new SDIProcessor(this.pageContext);
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setQueryFrom("category");
        sdiRequest.setQueryWhere(categoryquerywhere);
        sdiRequest.setQueryOrderBy("categoryid");
        sdiRequest.setRequestItem("primary[categoryid,categorydesc]");
        sdiRequest.setSDCid("Category");
        sdiRequest.setRetrieve(true);
        PropertyList pageData = (PropertyList)this.pageContext.getAttribute("pagedata", 2);
        DataSet categoryids = null;
        if (pageData != null && pageData.getProperty("page").length() > 0) {
            String cachekey = pageData.getProperty("page") + "_CachedCategoryDs";
            if (this.pageContext.getSession().getAttribute(cachekey) == null) {
                this.pageContext.setAttribute(cachekey, (Object)sdip.getSDIData(sdiRequest).getDataset("primary"));
            }
            categoryids = (DataSet)this.pageContext.getAttribute(cachekey);
        } else {
            categoryids = sdip.getSDIData(sdiRequest).getDataset("primary");
        }
        int rows = categoryids.getRowCount();
        if (rows > 0) {
            String selectcategoryid = "";
            RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
            PropertyList userConfig = requestContext.getPropertyList("userconfig");
            if (this.isLastSearchType) {
                selectcategoryid = userConfig.getProperty("as_searchid_" + this.cookieKey);
            }
            boolean isSelect = this.contentProperties.getProperty("style").equals("dropdownlist");
            output.append("<table id=\"categorysearch_row\" style=\"padding-top:3px;padding-bottom:2px\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">");
            if (isSelect) {
                output.append("<tr>").append("<td><img style=\"margin:2px;visibility:hidden\" id=\"categorypointer\" src=\"WEB-CORE/elements/images/selected_item.gif\"></td>").append("<td><select id=\"categoryidselect\" class=\"search_inputfield search_selectfield\" onchange=\"openCategory( this.value )\">").append("<option value=\"\"></option>\n");
            }
            for (int i = 0; i < rows; ++i) {
                String categoryid = categoryids.getString(i, "categoryid");
                String categorydesc = categoryids.getString(i, "categorydesc");
                String dcategoryid = this.translator.translate(categoryid);
                String dcategorydesc = this.translator.translate(categorydesc);
                if (categorydesc == null || categorydesc.length() == 0) {
                    categorydesc = categoryid;
                }
                if (isSelect) {
                    output.append("<option value=\"").append(categoryid).append("\">").append(dcategoryid).append("</option>\n");
                    continue;
                }
                output.append("<tr><td id=\"td_" + categoryid + "\"><img src=\"WEB-CORE/elements/images/selected_item.gif\" style=\"margin:2px;visibility: hidden\" id=\"categorypointer_").append(categoryid).append("\"/>\n").append("<span class=\"modern_href\"><a href=\"javascript:openCategory( '").append(categoryid).append("' )\" title=\"").append(dcategorydesc).append("\">").append(dcategoryid).append("</a></span>").append("</td></tr>\n");
            }
            if (isSelect) {
                output.append("</select></td></tr>");
            }
            output.append("</table>");
            SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
            output.append("<script>\n").append("var lastcategoryid = \"\";").append("function openCategory( categoryid ) {\n").append("  sapphire.userConfig.set( \"as_searchid_").append(this.cookieKey).append("\",  categoryid )\n;").append("  if ( lastcategoryid != categoryid ) hideCategoryPointer();\n").append("  lastcategoryid = categoryid;\n").append("  querywhere = 'categoryitem.categoryid=\\'' + categoryid + '\\' and categoryitem.sdcid=\\'").append(this.sdcid).append("\\' and categoryitem.keyid1=").append(sdcProcessor.getProperty(this.sdcid, "tableid")).append(".").append(sdcProcessor.getProperty(this.sdcid, "keycolid1")).append("';\n").append("  if ( document.getElementById( \"categoryidselect\" ) != null ) { ").append("     document.getElementById( \"categoryidselect\" ).value = categoryid;\n").append("     document.getElementById( \"categorypointer\" ).style.visibility = 'visible';\n").append("     document.getElementById( \"categorysearch_row\" ).style.backgroundColor='#eff6fe';document.getElementById( \"categorysearch_row\" ).style.border='1px solid #43a2d6';\n").append("  } ").append("  else {").append("     document.getElementById( \"categorypointer_\" + categoryid ).style.visibility='visible';\n").append("     var categoryTD = document.getElementById( \"td_\" + categoryid );\n").append("     if ( categoryTD != null ) { categoryTD.className ='modern_href selected'; }\n").append("  } ").append("\tdoCallback( 'category',  categoryid, '").append(this.sdcid).append("','").append(sdcProcessor.getProperty(this.sdcid, "tableid")).append(", categoryitem', querywhere );\n").append("}\n").append("function openSelectedCategory() {\n").append("  openCategory( \"").append(selectcategoryid).append("\" );\n").append("}").append("function runDefaultCategory() {\n").append("  openCategory( '").append(this.contentProperties.getProperty("default")).append("' );\n").append("}\n");
            if (selectcategoryid.length() > 0) {
                output.append("sapphire.events.attachEvent( window, 'onload', openSelectedCategory );");
            }
            output.append("</script>");
        }
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

