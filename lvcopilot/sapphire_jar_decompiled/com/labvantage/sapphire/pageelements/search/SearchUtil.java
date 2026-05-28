/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.search;

import sapphire.util.StringUtil;

public class SearchUtil {
    public static String toQueryInClause(String itemlist) {
        return SearchUtil.toQueryInClause(itemlist, ";");
    }

    public static String toQueryInClause(String itemlist, String delimiter) {
        return SearchUtil.toQueryInClause(itemlist, delimiter, false);
    }

    public static String toQueryInClause(String itemlist, String delimiter, boolean unicode) {
        String[] items = StringUtil.split(itemlist, delimiter);
        String whereclause = unicode ? "(N'" : "('";
        for (int i = 0; i < items.length; ++i) {
            whereclause = i == items.length - 1 ? whereclause + items[i] + "')" : whereclause + items[i] + (unicode ? "', N'" : "','");
        }
        return whereclause;
    }

    public static String getIdHtml(String id, String tip, boolean isCategory) {
        StringBuffer html = new StringBuffer("");
        html.append("<tr class=\"search_idrow\"><td><img src=\"WEB-CORE/elements/images/transgif.gif\" width=\"5\" height=\"9\" id=\"" + id + "\"/>\n").append("\t  <a href=\"javascript:startSearch( " + (isCategory ? "'category','" : "'query','") + id + "')\" title=\"" + tip + "\">").append(id).append("</a>").append("</td></tr>\n");
        return html.toString();
    }
}

