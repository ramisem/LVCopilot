/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.search;

import com.labvantage.sapphire.pageelements.search.SearchUtil;
import javax.servlet.jsp.PageContext;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class SearchByCategory
extends BaseElement {
    public SearchByCategory(PageContext pageContext, String connectionid) {
        this.pageContext = pageContext;
        this.setConnectionId(connectionid);
    }

    @Override
    public String getHtml() {
        String selected;
        String filterlist;
        StringBuffer html = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        String categoryquerywhere = "sdcid=" + safeSQL.addVar(this.element.getProperty("sdcid"));
        if (this.element.getPropertyList("categorysearch") != null && (filterlist = this.element.getPropertyList("categorysearch").getProperty("filter")) != null && filterlist.trim().length() > 0) {
            categoryquerywhere = categoryquerywhere + " and categoryid in (" + safeSQL.addIn(filterlist, ";") + ")";
        }
        String sql = "select categoryid, categorydesc from category where " + categoryquerywhere + " order by categoryid";
        DataSet categoryids = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        int rows = categoryids.getRowCount();
        String firstcat = categoryids.getString(0, "categoryid");
        String string = selected = this.element.getPropertyList("categorysearch") != null ? this.element.getPropertyList("categorysearch").getProperty("default") : "";
        if (this.element.getProperty("hasdefault") != null && this.element.getProperty("hasdefault").equals("N")) {
            if (this.element.getPropertyList("categorysearch") == null) {
                this.element.setProperty("categorysearch", new PropertyList());
            }
            this.element.getPropertyList("categorysearch").setProperty("default", firstcat);
            selected = firstcat;
        }
        if (rows > 0) {
            boolean isSelect;
            boolean bl = isSelect = this.element.getPropertyList("categorysearch") != null ? this.element.getPropertyList("categorysearch").getProperty("style").equals("dropdownlist") : false;
            if (isSelect) {
                html.append("<select id=\"categoryidselect\" class=\"search_categoryidselect\" onchange=\"javascript:startSearch( 'category', this.value )\">");
                html.append("<option value=\"\"></option>\n");
            }
            for (int i = 0; i < rows; ++i) {
                String categoryid = categoryids.getString(i, "categoryid");
                String categorydesc = categoryids.getString(i, "categorydesc");
                if (categorydesc == null || categorydesc.length() == 0) {
                    categorydesc = categoryid;
                }
                if (isSelect) {
                    if (categoryid.equals(selected)) {
                        html.append("<option value=\"" + categoryid + "\" selected>\n");
                    } else {
                        html.append("<option value=\"" + categoryid + "\">\n");
                    }
                    html.append(categoryid).append("</option>\n");
                    continue;
                }
                html.append(SearchUtil.getIdHtml(categoryid, categorydesc, true));
            }
        }
        if (this.element.getProperty("hasdefault") != null && this.element.getProperty("hasdefault").equals("N")) {
            if (this.element.getPropertyList("categorysearch") == null) {
                this.element.setProperty("categorysearch", new PropertyList());
            }
            this.element.getPropertyList("categorysearch").setProperty("default", firstcat);
        }
        return html.toString();
    }
}

