/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.util.layout;

import com.labvantage.opal.layouts.LayoutUtil;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GenericLayout {
    public static final String YES = "Y";

    public static String getProcessingDiv(PropertyList layout) {
        PropertyList oProcessingDiv;
        String html = "";
        if (layout != null && (oProcessingDiv = layout.getPropertyList("processingdiv")) != null) {
            String sText = oProcessingDiv.getProperty("text");
            String sImage = oProcessingDiv.getProperty("image");
            if (sImage == null || sImage.equalsIgnoreCase("")) {
                sImage = "WEB-OPAL/images/loadingbar.gif";
            }
            html = "<iframe id=processingdiviframe class=processingdiviframe src=\"WEB-CORE/blank.html\"></iframe><div id=\"processingdiv\" class=\"processingdiv\"><div id=\"processingdiv_text\" class=\"processingdiv_text\">" + sText + "</div><img src=\"" + sImage + "\"/></div>";
        }
        return html;
    }

    public static void renderLinkHTML(StringBuffer html, int index, String text, String link, String linkid, String className, String ddclassName, PropertyList plLink) {
        GenericLayout.renderLinkHTML(html, html, index, text, link, linkid, className, ddclassName, plLink);
    }

    public static void renderLinkHTML(StringBuffer html, StringBuffer dropdowns, int index, String text, String link, String linkid, String className, String ddclassName, PropertyList plLink) {
        if (text.indexOf("|") > -1 && link.indexOf("|") > -1) {
            html.append("<td class=\"").append(className.length() > 0 ? "" + className : "").append("\" id=\"layout_linkcell").append(index).append("\">");
            html.append("<table width=\"100%\" cellpadding=0 cellspacing=0 border=0>");
            html.append("<tr><td>");
            dropdowns.append("<div style=\"display:none;position:absolute;z-index:200;\" class=\"link_dropdown_box\" id=\"link_dropdown").append(index).append("\" onmouseover=\"mouseOverLinkDropdown( ").append(index).append(" );\" onmouseout=\"mouseOutLinkDropdown( ").append(index).append(" );\">");
            dropdowns.append("<table width=\"100%\" height=\"100%\" cellpadding=0 cellspaing=0 border=0 >");
            String[] texts = StringUtil.split(text, "|");
            String[] links = StringUtil.split(link, "|");
            String[] ids = StringUtil.split(linkid, "|");
            for (int l = 0; l < texts.length; ++l) {
                dropdowns.append("<tr >");
                dropdowns.append("<td class=\"link_dropdown_box_cell\" onmouseover=\"this.className='link_dropdown_box_cell_over';\" onclick=\"").append(links[l]).append("\" onmouseout=\"this.className='link_dropdown_box_cell'\">");
                dropdowns.append("<a class=\"layout_links_dropdown\" title=\"").append(texts[l]).append("\" id=\"").append(ids[l]).append("\" u_alias=\"layout_sublink").append(l).append("\">").append(texts[l]).append("</a>");
                dropdowns.append("</td>");
                dropdowns.append("</tr>");
            }
            dropdowns.append("</table>");
            dropdowns.append("</div>");
            html.append("</td><td class=\"layout_linkcell").append(className.length() > 0 ? " " + className : "").append("\" align=\"center\">");
            html.append("<a title=\"").append(texts[0]).append("\" href=\"").append(links[0]).append("\" id=\"").append(ids[0]).append("\" u_alias=\"layout_link").append(0).append("\" onmouseover=\"doOverLink(event,").append(index).append(",'").append(className).append("_over', true);\" onmouseout=\"doOutLink(event,").append(index).append(",'").append(className).append("',true);\">").append(GenericLayout.getTheLinkImage(plLink, texts[0])).append("</a>");
            html.append("</td></tr>");
            html.append("</table>");
            html.append("</td>");
            html.append("<td class=\"").append(ddclassName).append("\" id=\"layout_linkdropdown").append(index).append("\" onclick=\"doLinkDropdown(").append(index).append(", true);\" onmouseover=\"doOverLinkDropDown(event,").append(index).append(",'").append(ddclassName).append("_over');\" onmouseout=\"doOutLinkDropDown(event,").append(index).append(",'").append(ddclassName).append("');\">");
            html.append("</td>");
        } else {
            html.append("<td class=\"layout_linkcell").append(className.length() > 0 ? " " + className : "").append("\" align=\"center\" id=\"layout_linkcell").append(index).append("\">");
            html.append("<a title=\"").append(text).append("\" href=\"").append(link).append("\" id=\"").append(linkid).append("\" u_alias=\"layout_link").append(0).append("\" onmouseover=\"doOverLink(event,").append(index).append(",'").append(className).append("_over',false);\" onmouseout=\"doOutLink(event,").append(index).append(",'").append(className).append("',false);\">").append(GenericLayout.getTheLinkImage(plLink, text)).append("</a>");
            html.append("</td>");
        }
    }

    public static String getPageInfo(PropertyList pagedata, PropertyList layout) {
        StringBuffer sbText = new StringBuffer();
        if (pagedata != null && layout != null) {
            sbText.append("Page=");
            sbText.append(pagedata.getProperty("page"));
            sbText.append("\n").append("PageType=");
            sbText.append(pagedata.getProperty("propertytreeid"));
            sbText.append("\n").append("Jsp=");
            sbText.append(pagedata.getProperty("objectname"));
            sbText.append("\n").append("Layout=");
            sbText.append(layout.getProperty("objectname"));
        }
        return sbText.toString();
    }

    public static String getTheLinkImage(Object oLink, String text) {
        String sTheIcon = "";
        String sTheImg = "";
        if (oLink != null) {
            PropertyList plLink = (PropertyList)oLink;
            sTheIcon = plLink.getProperty("icon");
            if (sTheIcon.contains("|")) {
                sTheIcon = StringUtil.split(sTheIcon, "|")[0];
            }
            sTheImg = sTheIcon != null && !sTheIcon.equalsIgnoreCase("") ? "<img src='" + sTheIcon + "' border=0 title='" + text + "' width=16 height=16 >" : "<img src='WEB-OPAL/layouts/generic/images/blank.gif' border=0 title='" + text + "' width=16 height=16 >";
        }
        return sTheImg;
    }

    public static String getIncludes(PropertyList layout, String position) {
        StringBuffer sb = new StringBuffer();
        PropertyListCollection plcIncludes = layout.getCollection("includes");
        if (plcIncludes != null) {
            for (int i = 0; i < plcIncludes.size(); ++i) {
                PropertyList plInclude = plcIncludes.getPropertyList(i);
                if (!plInclude.getProperty("position").equalsIgnoreCase("") && !plInclude.getProperty("position").equalsIgnoreCase(position)) continue;
                sb.append("<script type=\"text/javascript\" src=\"").append(plInclude.getProperty("url")).append("\"></script>\n");
            }
        }
        if (position.equals("head") && layout.containsKey("tabs")) {
            sb.append("\n<script type=\"text/javascript\" src=\"WEB-OPAL/layouts/generic/scripts/genericlayout.js\"></script>");
            sb.append("\n<script type=\"text/javascript\" src=\"WEB-OPAL/scripts/layout.js\"></script>");
        }
        return sb.toString();
    }

    public static String getStyleSheets(PropertyList layout) {
        StringBuffer sb = new StringBuffer();
        PropertyListCollection styleSheetCollection = layout.getCollection("stylesheets");
        if (styleSheetCollection != null) {
            for (int i = 0; i < styleSheetCollection.size(); ++i) {
                PropertyList styleSheet = styleSheetCollection.getPropertyList(i);
                if (styleSheet == null || styleSheet.getProperty("url").trim().length() <= 0) continue;
                sb.append("<link rel=\"stylesheet\" href=\"").append(styleSheet.getProperty("url").trim());
                sb.append("\" type=\"text/css\" /> \n");
            }
        }
        return sb.toString();
    }

    public static String getMenuDivs(PropertyListCollection plcTabs) {
        return GenericLayout.getMenuDivs(plcTabs, null, null);
    }

    public static String getMenuDivs(PropertyListCollection plcTabs, Browser browser) {
        return GenericLayout.getMenuDivs(plcTabs, browser, null);
    }

    public static String getMenuDivs(PropertyListCollection plcTabs, Browser browser, RequestContext rc) {
        boolean html5;
        StringBuffer menu = new StringBuffer();
        boolean bl = html5 = rc != null && rc.getProperty("html5").equalsIgnoreCase(YES);
        if (plcTabs != null) {
            for (int i = 0; i < plcTabs.size(); ++i) {
                PropertyListCollection plcMenus;
                PropertyList plTab = plcTabs.getPropertyList(i);
                String showTab = plTab.getProperty("show", YES);
                if (!YES.equalsIgnoreCase(showTab)) continue;
                if (browser == null || browser.isIE() && !html5) {
                    menu.append("<div id=\"MenuDiv").append(i).append("\" class=\"layout_popupmenu_div time\" onMouseEnter=\"mouseEnterMenuDropdown(").append(i).append(" )\" onMouseLeave=\"mouseLeaveMenuDropdown(").append(i).append(" )\" style=\"display:none;position:absolute;\" dur=\"indefinite\" >");
                    menu.append("<t:TRANSITIONFILTER targetElement=\"MenuDiv").append(i).append("\" begin=\"indefinate\" id=\"MenuDivTransA").append(i).append("\" DUR=\"0.25\" MODE=\"IN\" TYPE=\"barWipe\" subtype=\"topToBottom\" onbegin=\"doShowMenuOnBegin( ").append(i).append(" )\" />");
                    menu.append("<t:TRANSITIONFILTER targetElement=\"MenuDiv").append(i).append("\" begin=\"indefinate\" id=\"MenuDivTransB").append(i).append("\" DUR=\"0.2\" MODE=\"OUT\" TYPE=\"fade\" onend=\"doHideMenuOnEnd( ").append(i).append(" )\" />");
                    menu.append("<iframe id=\"selectblocker").append(i).append("\" frameborder=\"0\" style=\"position:relative; top:0px; left:0px;\" src=\"WEB-CORE/blank.html\"></iframe>");
                } else {
                    menu.append("<div id=\"MenuDiv").append(i).append("\" class=\"layout_popupmenu_div\" onmouseout=\"mouseOutMenuDropdown(event,").append(i).append(" );\" onmouseover=\"mouseOverMenuDropdown(event,").append(i).append(");\" style=\"display:none;position:absolute;\">");
                }
                String className = "";
                if (browser != null && browser.isMozilla() || html5) {
                    className = "layout_popupmenu_table";
                }
                menu.append("<table id=\"menutable").append(i).append("\" class=\"").append(className).append("\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"position:absolute; top:0px; left:0px;\">");
                if (plTab != null && (plcMenus = plTab.getCollection("menus")) != null) {
                    for (int j = 0; j < plcMenus.size(); ++j) {
                        PropertyList plMenu = plcMenus.getPropertyList(j);
                        plMenu.setProperty("selectedtab", plTab.getProperty("id"));
                        plMenu.setProperty("selectedmenu", plMenu.getProperty("id"));
                        String showMenuItem = plMenu.getProperty("show", YES);
                        if (!YES.equalsIgnoreCase(showMenuItem)) continue;
                        menu.append("<tr><td>");
                        menu.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'><tr>");
                        menu.append("<td id=\"menuitem_").append(i).append("_").append(j).append("\" ");
                        menu.append("class=\"layout_popupmenu\" onMouseOver=\"menu_onmouseover( '").append(i).append("', '").append(j).append("' )\" ");
                        menu.append("onMouseOut=\"menu_onmouseout( '").append(i).append("', '").append(j).append("' )\" onClick=\"");
                        menu.append(LayoutUtil.getTheLink(plMenu, false));
                        menu.append("\" nowrap valign=\"middle\">");
                        menu.append("<span class=\"layout_popupmenu_text\">");
                        menu.append(plMenu.getProperty("text")).append("</span></td></tr></table>");
                        menu.append("</td></tr>");
                    }
                }
                menu.append("</table>\n").append("</div>");
            }
        }
        return menu.toString();
    }

    public static String getTopBarToggle(PageContext pageContext) {
        StringBuffer tabcontent = new StringBuffer();
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        tabcontent.append("<td id=\"layout_topbar_toggle\" align=\"right\" class=\"\" style=\"cursor:pointer;padding:1px 1px 1px 1px;width:20px;\" onclick=\"__topBarToggle.toggle()\" onmouseover=\"this.style.border='solid 1px orange';this.style.padding='0 0 0 0';\" onmouseout=\"this.style.border='none';this.style.padding='1px 1px 1px 1px';\">");
        tabcontent.append("<img title=\"").append(tp.translate("Restore menu bar")).append("\" src=\"WEB-OPAL/layouts/generic/images/expand.gif\" style=\"display:none\">");
        tabcontent.append("<img title=\"").append(tp.translate("Hide menu bar")).append("\" src=\"WEB-OPAL/layouts/generic/images/collapse.gif\">");
        tabcontent.append("</td>");
        return tabcontent.toString();
    }

    public static PropertyList buildTabs(StringBuffer tabcontent, PropertyListCollection plcTabs, PropertyList poptions, String linktab) {
        return GenericLayout.buildTabs(tabcontent, plcTabs, poptions, linktab, null);
    }

    public static PropertyList buildTabs(StringBuffer tabcontent, PropertyListCollection plcTabs, PropertyList poptions, String linktab, Browser browser) {
        PropertyList plSelectedTab = new PropertyList();
        boolean showMenu = false;
        if (plcTabs != null) {
            PropertyListCollection menus;
            String showSelectedTab;
            String selectText;
            StringBuffer tab = new StringBuffer();
            if (plcTabs.size() == 1) {
                String linkhover;
                String hover;
                PropertyList plTab = plcTabs.getPropertyList(0);
                plTab.setProperty("selectedtab", plTab.getProperty("id"));
                String sTheLinkText = LayoutUtil.getTheLink(plTab, false);
                boolean selected = false;
                if (poptions != null) {
                    selected = plTab.getProperty("id").equalsIgnoreCase(linktab);
                }
                if (selected) {
                    plSelectedTab = (PropertyList)plTab.clone();
                }
                if (browser == null || browser.isIE()) {
                    hover = selected ? "" : " onMouseLeave=\"mouseLeaveMenuDropdown(0)\" ";
                    linkhover = selected ? "" : " onMouseEnter=\"mouseEnterMenuDropdown(0)\" ";
                } else {
                    hover = selected ? "" : " onmouseout=\"mouseOutMenuDropdown(event,0)\" ";
                    linkhover = selected ? "" : " onmouseover=\"mouseOverMenuDropdown(event,0)\" ";
                }
                tab.append("<td id=\"Tab").append(0).append("_L\" class=\"tabs_startleft").append(selected ? "_selected" : "").append("\"></td>");
                tab.append("<td nowrap id=\"Tab").append(0).append("_C\" class=\"tabs_main").append(selected ? "_selected" : "").append("\" ").append(hover).append(" >");
                tab.append("<a class=\"layout_tab_text").append(selected ? "_selected" : "").append("\" href=\"").append(sTheLinkText).append("\" ").append(linkhover).append(" >").append(plTab.getProperty("text")).append("</a>");
                tab.append("</td>");
                tab.append("<td id=\"Tab").append(0).append("_R\" class=\"tabs_endright").append(selected ? "_selected" : "").append("\"></td>");
            } else {
                boolean first = true;
                for (int i = 0; i < plcTabs.size(); ++i) {
                    String linkhover;
                    String hover;
                    PropertyList plTab = plcTabs.getPropertyList(i);
                    plTab.setProperty("selectedtab", plTab.getProperty("id"));
                    String sTheLinkText = LayoutUtil.getTheLink(plTab, false);
                    boolean selected = false;
                    if (poptions != null) {
                        selected = plTab.getProperty("id").equalsIgnoreCase(linktab);
                    }
                    if (selected) {
                        plSelectedTab = (PropertyList)plTab.clone();
                    }
                    if (browser == null || browser.isIE()) {
                        hover = selected ? "" : "  onMouseLeave=\"mouseLeaveMenuDropdown(" + i + ")\" ";
                        linkhover = selected ? "" : " onMouseEnter=\"mouseEnterMenuDropdown(" + i + ")\" ";
                    } else {
                        hover = selected ? "" : "  onmouseout=\"mouseOutMenuDropdown(event," + i + ")\" ";
                        String string = linkhover = selected ? "" : " onmouseover=\"mouseOverMenuDropdown(event," + i + ")\" ";
                    }
                    if (first) {
                        first = false;
                        tab.append("<td id=\"Tab").append(i).append("_L\" class=\"tabs_startleft").append(selected ? "_selected" : "").append("\"></td>");
                        tab.append("<td nowrap id=\"Tab").append(i).append("_C\" class=\"tabs_main").append(selected ? "_selected" : "").append("\" ").append(hover).append(" >");
                        tab.append("<a class=\"layout_tab_text").append(selected ? "_selected" : "").append("\" href=\"").append(sTheLinkText).append("\" ").append(linkhover).append(" >").append(plTab.getProperty("text")).append("</a>");
                        tab.append("</td>");
                        tab.append("<td id=\"Tab").append(i).append("_R\" class=\"tabs_right").append(selected ? "_selected" : "").append("\"></td>");
                        continue;
                    }
                    if (i == plcTabs.size() - 1) {
                        tab.append("<td id=\"Tab").append(i).append("_L\" class=\"tabs_left").append(selected ? "_selected" : "").append("\"  ></td>");
                        tab.append("<td nowrap id=\"Tab").append(i).append("_C\" class=\"tabs_main").append(selected ? "_selected" : "").append("\" ").append(hover).append(" >");
                        tab.append("<a class=\"layout_tab_text").append(selected ? "_selected" : "").append("\" href=\"").append(sTheLinkText).append("\" ").append(linkhover).append(" >").append(plTab.getProperty("text")).append("</a>");
                        tab.append("</td>");
                        tab.append("<td id=\"Tab").append(i).append("_R\" class=\"tabs_endright").append(selected ? "_selected" : "").append("\"></td>");
                        continue;
                    }
                    tab.append("<td id=\"Tab").append(i).append("_L\" class=\"tabs_left").append(selected ? "_selected" : "").append("\"></td>");
                    tab.append("<td nowrap id=\"Tab").append(i).append("_C\" class=\"tabs_main").append(selected ? "_selected" : "").append("\" ").append(hover).append(" >");
                    tab.append("<a class=\"layout_tab_text").append(selected ? "_selected" : "").append("\" href=\"").append(sTheLinkText).append("\" ").append(linkhover).append(" >").append(plTab.getProperty("text")).append("</a>");
                    tab.append("</td>");
                    tab.append("<td id=\"Tab").append(i).append("_R\" class=\"tabs_right").append(selected ? "_selected" : "").append("\"></td>");
                }
            }
            if (tab.length() > 0) {
                tabcontent.append(tab);
            }
            if (plSelectedTab != null && (selectText = plSelectedTab.getProperty("text")) != null && selectText.length() > 0 && YES.equalsIgnoreCase(showSelectedTab = plSelectedTab.getProperty("show", YES)) && (menus = plSelectedTab.getCollection("menus")) != null && menus.size() > 0) {
                showMenu = true;
            }
            if (showMenu) {
                tabcontent.append("</tr><tr height=1>");
                boolean first = true;
                for (int i = 0; i < plcTabs.size(); ++i) {
                    PropertyList plTab = plcTabs.getPropertyList(i);
                    plTab.setProperty("selectedtab", plTab.getProperty("id"));
                    boolean selected = false;
                    if (poptions != null) {
                        selected = plTab.getProperty("id").equalsIgnoreCase(linktab);
                    }
                    if (first) {
                        first = false;
                        if (selected) {
                            tabcontent.append("<td class=\"layout_menubarborder_startleft_selected\"></td><td class=\"layout_menubarborder_selected\"></td><td class=\"layout_menubarborder_right_selected\"></td>");
                            continue;
                        }
                        tabcontent.append("<td class=\"layout_menubarborder_startleft\"></td><td class=\"layout_menubarborder\"></td><td class=\"layout_menubarborder_right\"></td>");
                        continue;
                    }
                    if (i == plcTabs.size() - 1) {
                        if (selected) {
                            tabcontent.append("<td class=\"layout_menubarborder_left_selected\"></td><td class=\"layout_menubarborder_selected\"></td><td class=\"layout_menubarborder_endright_selected\"></td>");
                            continue;
                        }
                        tabcontent.append("<td class=\"layout_menubarborder_left\"></td><td class=\"layout_menubarborder\"></td><td class=\"layout_menubarborder_endright\"></td>");
                        continue;
                    }
                    if (selected) {
                        tabcontent.append("<td class=\"layout_menubarborder_left_selected\"></td><td class=\"layout_menubarborder_selected\"></td><td class=\"layout_menubarborder_right_selected\"></td>");
                        continue;
                    }
                    tabcontent.append("<td class=\"layout_menubarborder_left\"></td><td class=\"layout_menubarborder\"></td><td class=\"layout_menubarborder_right\"></td>");
                }
            }
        }
        plSelectedTab.setProperty("showmenubar", showMenu ? YES : "N");
        return plSelectedTab;
    }

    public static String getMenuBar(boolean showMenubar, PropertyList plSelectedTab, int menuheight, PropertyList poptions, String linkmenu) {
        return GenericLayout.getMenuBar(showMenubar, plSelectedTab, menuheight, poptions, linkmenu, false, 0, "");
    }

    public static String getMenuBar(boolean showMenubar, PropertyList plSelectedTab, int menuheight, PropertyList poptions, String linkmenu, boolean ribbonStyle, int sidebarWidth, String sideBarDisplay) {
        PropertyListCollection menus;
        StringBuffer sb = new StringBuffer();
        if (showMenubar && (menus = plSelectedTab.getCollection("menus")) != null && menus.size() > 0) {
            sb.append("<tr height=\"").append(menuheight).append("\">");
            sb.append("<td>");
            sb.append("<table height=\"100%\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
            sb.append("<tr>");
            sb.append("<td class=\"layout_mainmenubar_left\">");
            sb.append("</td>");
            sb.append("<td nowrap class=\"layout_mainmenubar\">");
            sb.append("<table height=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
            sb.append("<tr>");
            if (ribbonStyle && sidebarWidth > 0) {
                sb.append("<td id=\"sidebarspacetd\" style=\"width:").append(sidebarWidth + 10).append("px;display:").append(sideBarDisplay).append(";\"></td>");
            }
            for (int i = 0; i < menus.size(); ++i) {
                PropertyList menu = menus.getPropertyList(i);
                String showSelectedTabMenuItem = menu.getProperty("show", YES);
                if (!YES.equalsIgnoreCase(showSelectedTabMenuItem)) continue;
                if (i == 0) {
                    sb.append("<td align=\"center\" valign=\"top\" class=\"layout_menupadding\">");
                    sb.append("");
                    sb.append("</td>");
                }
                String id = menu.getProperty("id");
                menu.setProperty("selectedtab", plSelectedTab.getProperty("id"));
                menu.setProperty("selectedmenu", menu.getProperty("id"));
                if (poptions != null && id.equalsIgnoreCase(linkmenu)) {
                    if (ribbonStyle) {
                        sb.append("<td><table cellpadding=0 cellspacing=0 border=0 style=\"height:20px;\"><tbody><tr>");
                        sb.append("<td class=\"ribbon_menu_imageback ribbon_menu_start\"></td>");
                        sb.append("<td nowrap valign=\"top\" id=\"layout_menulinkcell").append(i).append("\" class=\"ribbon_menu_imageback ribbon_menu_center\">");
                        sb.append("<a onmouseover=\"__menubar.ribbonMouseOver(this)\" onmouseout=\"__menubar.ribbonMouseOut(this)\" href=\"").append(LayoutUtil.getTheLink(menu, false)).append("\" ");
                        sb.append("id=\"layout_menulink").append(i).append("\" class=\"ribbon_menu_text\">");
                    } else {
                        sb.append("<td nowrap class=\"layout_selected_menu_cell\" valign=\"top\" id=\"layout_menulinkcell").append(i).append("\">");
                        sb.append("<a href=\"").append(LayoutUtil.getTheLink(menu, false)).append("\" ");
                        sb.append("class=\"layout_selected_menu_text\" id=\"layout_menulink").append(i).append("\">");
                    }
                } else if (ribbonStyle) {
                    sb.append("<td><table cellpadding=0 cellspacing=0 border=0 style=\"height:20px;\"><tbody><tr>");
                    sb.append("<td class=\"\" style=\"width:6px;\"></td>");
                    sb.append("<td nowrap valign=\"top\" id=\"layout_menulinkcell").append(i).append("\" class=\"layout_notselected_menu_cell\" style=\"text-align:center;padding-left:6px;padding-right:6px;\">");
                    sb.append("<a onmouseover=\"__menubar.ribbonMouseOver(this)\" onmouseout=\"__menubar.ribbonMouseOut(this)\" href=\"").append(LayoutUtil.getTheLink(menu, false)).append("\" ");
                    sb.append("id=\"layout_menulink").append(i).append("\" class=\"layout_notselected_menu_text\">");
                } else {
                    sb.append("<td nowrap class=\"layout_notselected_menu_cell\" valign=\"top\" id=\"layout_menulinkcell").append(i).append("\">");
                    sb.append("<a href=\"").append(LayoutUtil.getTheLink(menu, false)).append("\" ");
                    sb.append("class=\"layout_notselected_menu_text\" id=\"layout_menulink").append(i).append("\">");
                }
                sb.append(menu.getProperty("text"));
                sb.append("</a>");
                sb.append("</td>");
                if (ribbonStyle) {
                    if (poptions != null && id.equalsIgnoreCase(linkmenu)) {
                        sb.append("<td class=\"ribbon_menu_imageback ribbon_menu_end\"></td>");
                        sb.append("</tr></tbody></table></td>");
                    } else {
                        sb.append("<td class=\"\" style=\"width:6px;\"></td>");
                        sb.append("</tr></tbody></table></td>");
                    }
                }
                if (i == menus.size() - 1) continue;
                sb.append("<td align=\"center\" valign=\"top\" class=\"layout_menusplitter\">");
                sb.append("");
                sb.append("</td>");
            }
            sb.append("</tr>");
            sb.append("</table>");
            sb.append("</td>");
            sb.append("<td class=\"layout_mainmenubar_right\">");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</table>");
            sb.append("</td>");
            sb.append("</tr>");
        }
        return sb.toString();
    }
}

