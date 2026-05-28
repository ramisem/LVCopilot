/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.layouts;

import com.labvantage.sapphire.pageelements.scrollpanel.ScrollPanel;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.Browser;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LayoutUtil {
    public static String getSidebarItem(PropertyList plLayout, String type, String titleOnClick, PropertyListCollection items, String baseItemLink, int sidebarWidth, String emptyText, TranslationProcessor tp, PropertyList userConfig, String cookieKey, String title, String sideBarListId) {
        return LayoutUtil.getSidebarItem(plLayout, type, titleOnClick, items, baseItemLink, sidebarWidth, emptyText, tp, userConfig, cookieKey, title, sideBarListId, 0, null);
    }

    public static String getSidebarItem(PropertyList plLayout, String type, String titleOnClick, PropertyListCollection items, String baseItemLink, int sidebarWidth, String emptyText, TranslationProcessor tp, PropertyList userConfig, String cookieKey, String title, String sideBarListId, int maxHeight, PageContext pageContext) {
        Browser b;
        if (sideBarListId == null) {
            sideBarListId = "";
        }
        StringBuffer html = new StringBuffer();
        boolean showItem = !userConfig.getProperty("sidebaritem_" + type + "_" + cookieKey).equalsIgnoreCase("n");
        html.append("<table class=\"layout_sidebar_tab_back\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" id=\"layout_sidebartable_").append(type).append("\">");
        html.append("<tr class=\"layout_sidebar_tab_row\">");
        html.append("<td align=\"left\" class=\"layout_sidebar_tab\" id=\"layout_sidebartitlecell_").append(type).append("\" >");
        html.append(title);
        if (titleOnClick.length() > 0) {
            html.append("<a href=\"").append(title).append("\" onClick=\"").append(titleOnClick).append("\" id=\"layout_sidebartitlelink_").append(type).append("\" class=\"layout_sidebar_link\">");
            html.append(" (").append(tp.translate("open")).append("...)");
            html.append("</a>");
        }
        html.append("</td>");
        html.append("<td align=right valign=middle class=\"layout_sidebar_tab_expandcollapse\">");
        html.append("<image src=\"WEB-OPAL/layouts/generic/images/sidebar_collapse_item.gif\" title=\"");
        html.append(tp.translate("Collapse Item")).append("\" id=\"layout_sidebar_itemcollapse_").append(sideBarListId.trim().length() == 0 ? type : sideBarListId + "_" + type);
        if (showItem) {
            html.append("\" style=\"display:block;cursor:pointer;\" onclick=\"toggleSideBarItem('").append(sideBarListId).append("',event);\">");
        } else {
            html.append("\" style=\"display:none;cursor:pointer;\" onclick=\"toggleSideBarItem('").append(sideBarListId).append("',event);\">");
        }
        html.append("<image src=\"WEB-OPAL/layouts/generic/images/sidebar_expand_item.gif\" title=\"");
        html.append(tp.translate("Expand Item")).append("\" id=\"layout_sidebar_itemexpand_").append(sideBarListId.trim().length() == 0 ? type : sideBarListId + "_" + type);
        if (showItem) {
            html.append("\" style=\"display:none;cursor:pointer;\" onclick=\"toggleSideBarItem('").append(sideBarListId).append("',event);\">");
        } else {
            html.append("\" style=\"display:block;cursor:pointer;\" onclick=\"toggleSideBarItem('").append(sideBarListId).append("',event);\">");
        }
        html.append("</td>");
        html.append("</tr>");
        html.append("<tr>");
        html.append("<td colspan=2 align=\"left\" valign=\"top\" class=\"layout_sidebar_content\" id=\"layout_sidebarcontentcell_").append(type).append("\" >");
        if (showItem) {
            html.append("<div id=\"div_").append(type).append(sideBarListId.trim().length() == 0 ? "" : "_" + sideBarListId).append("\" style=\"display:block;width:").append(sidebarWidth - 5).append("px;\">");
        } else {
            html.append("<div id=\"div_").append(type).append(sideBarListId.trim().length() == 0 ? "" : "_" + sideBarListId).append("\" style=\"display:none;width:").append(sidebarWidth - 5).append("px\">");
        }
        StringBuffer content = new StringBuffer();
        String postyle = "";
        if (pageContext != null && (b = new Browser(pageContext)).isIE()) {
            postyle = " style=\"display:block;overflow-x:hidden;width:" + (sidebarWidth - 22) + "px;\"";
        }
        if (items == null || items.size() == 0) {
            content.append("").append(tp.translate(emptyText));
        } else {
            content.append("<table cellspacing=\"0\" cellpadding=\"1\">");
            for (int j = 0; j < items.size(); ++j) {
                PropertyList plItem = items.getPropertyList(j);
                if (type.equalsIgnoreCase("Favorites")) {
                    content.append("<tr><td valign=\"top\" width=\"1\">-</td>");
                    content.append("<td>");
                    content.append("</td></tr>");
                }
                if (type.equalsIgnoreCase("Operations")) {
                    content.append("<tr><td valign=\"top\" width=\"1\">-</td>");
                    content.append("<td>");
                    String id = plItem.getProperty("id");
                    String action = plItem.getProperty("action");
                    String text = plItem.getProperty("text");
                    content.append("<a class=\"pageoperation\"").append(postyle).append("id=\"operation_").append(id).append("\" href=\"").append(text).append("\" onclick=\"javascript:").append(action).append("\">");
                    content.append(text);
                    content.append("</a>");
                    content.append("</td></tr>");
                    continue;
                }
                if (type.equalsIgnoreCase("Bulletins")) {
                    String image;
                    content.append("<tr><td valign=\"top\" width=\"1\">-</td>");
                    content.append("<td>");
                    if (plItem.getProperty("priorityflag").equalsIgnoreCase("Y") && (image = LayoutUtil.getImage(plLayout, "bulletin_priority")) != null && image.length() > 0) {
                        content.append("<img src=\"").append(image).append("\">");
                    }
                    String bulldesc = plItem.getProperty("bulletindesc", "");
                    content.append("<a class=\"pageoperation\"").append(postyle).append(" ");
                    content.append("href=\"").append(plItem.getProperty("bulletinid")).append("\" ");
                    if (bulldesc.length() == 0) {
                        content.append("title=\"").append(plItem.getProperty("bulletinid")).append("\" ");
                    } else {
                        content.append("title=\"").append(bulldesc).append("\" ");
                    }
                    content.append("onclick=\"navigateTo('").append(baseItemLink).append(plItem.getProperty("bulletinid")).append("&mode=View','N',true ); return false;\" target=_blank>");
                    if (bulldesc.length() == 0) {
                        content.append(plItem.getProperty("bulletinid"));
                    } else if (bulldesc.length() > 17) {
                        content.append(bulldesc.substring(0, 17)).append("...");
                    } else {
                        content.append(bulldesc);
                    }
                    content.append("</a>");
                    content.append("</td></tr>");
                    continue;
                }
                if (type.equalsIgnoreCase("History")) {
                    content.append("<tr><td valign=\"top\" width=\"1\">-</td>");
                    content.append("<td>");
                    content.append("<a class=\"pageoperation\"").append(postyle).append(" ");
                    content.append("href=\"").append(plItem.getProperty("title")).append("\" ");
                    content.append("onclick=\"navigateTo('").append(baseItemLink).append(plItem.getProperty("webpagelogid")).append("','Y',false ); return false;\" ");
                    content.append("title=\"").append(plItem.getProperty("tip")).append("\">");
                    content.append(tp.translate(plItem.getProperty("title")));
                    content.append("</a>");
                    content.append("</td></tr>");
                    continue;
                }
                if (!type.equalsIgnoreCase("User") || !plItem.getProperty("showitem").equalsIgnoreCase("") && !plItem.getProperty("showitem").equalsIgnoreCase("Y")) continue;
                content.append("<tr><td valign=\"top\" width=\"1\">-</td>");
                content.append("<td>");
                content.append("<a class=\"pageoperation\"").append(postyle).append(" ");
                content.append("href=\"").append(LayoutUtil.getTheLinkText(plItem, tp)).append("\" ");
                content.append("onclick=\"").append(LayoutUtil.getTheLink(plItem, false)).append("; return false;\" >");
                content.append(plItem.getProperty("text"));
                content.append("</a>");
                content.append("</td></tr>");
            }
            content.append("</table>");
        }
        if (maxHeight > 0 && pageContext != null) {
            ScrollPanel scrollPanel = new ScrollPanel(pageContext);
            scrollPanel.setId("sidebar" + type);
            scrollPanel.setMaxHeight(maxHeight);
            scrollPanel.setContent(content);
            html.append(scrollPanel.getHtml());
        } else {
            html.append(content);
        }
        html.append("</div>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");
        return html.toString();
    }

    public static String getImage(PropertyList layout, String type) {
        String imgSrc = "";
        imgSrc = layout.getPropertyList("images").getProperty(type);
        if (imgSrc.equalsIgnoreCase("")) {
            imgSrc = layout.getPropertyList("images").getProperty("default_" + type, "WEB-CORE/images/blank.gif");
        }
        return imgSrc;
    }

    public static String getTheLink(PropertyList plLink, boolean lReplaceQuoteFlag) {
        String sTheLink = "";
        if (plLink != null) {
            sTheLink = plLink.getProperty("link");
            String sReleaseLockFlag = plLink.getProperty("releaselock");
            String selectedTab = plLink.getProperty("selectedtab");
            String selectedMenu = plLink.getProperty("selectedmenu");
            sTheLink = LayoutUtil.getNavigateToFunction(sTheLink, sReleaseLockFlag, lReplaceQuoteFlag, selectedTab, selectedMenu);
        }
        return sTheLink;
    }

    public static String getTheLinkText(Object oLink, TranslationProcessor tp) {
        String sTheText = "";
        if (oLink != null) {
            PropertyList plLink = (PropertyList)oLink;
            sTheText = plLink.getProperty("text");
            if (sTheText.length() == 0 && plLink.getProperty("icon").length() == 0) {
                sTheText = tp.translate("No Text");
            }
            sTheText = StringUtil.replaceAll(sTheText, "|", "");
        }
        return sTheText;
    }

    public static String getNavigateToFunction(String link, String releaseLockFlag, boolean replaceQuoteFlag, String selectedTab, String selectedMenu) {
        String navigateToFunction = "javascript:";
        if (selectedTab.length() > 0) {
            navigateToFunction = navigateToFunction + "sapphire.userConfig.set('genericlayout_lastlinktab','" + selectedTab + "');";
            navigateToFunction = navigateToFunction + "sapphire.userConfig.set('genericlayout_lastlinkmenu','" + selectedMenu + "');";
        }
        if (link != null && link.toLowerCase().indexOf("javascript") >= 0) {
            if (replaceQuoteFlag) {
                link = StringUtil.replaceAll(link, "'", "&034;");
            }
            navigateToFunction = navigateToFunction + "releaseLock('" + releaseLockFlag + "'); " + link.substring(11);
        } else {
            navigateToFunction = navigateToFunction + "navigateTo('" + link + "', '" + releaseLockFlag + "')";
        }
        return navigateToFunction;
    }
}

