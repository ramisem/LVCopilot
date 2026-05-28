/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MenuGizmo
extends sapphire.pageelements.BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";
    public static final String STARTURL = "rc?command=page&page=";
    public static final String IMAGE_TOP = "FlatWhiteArrow6Down";
    public static final String IMAGE_SUB = "FlatWhiteTriangleDown";
    private HashMap<String, int[]> allmenus = new HashMap();
    private boolean smallRendered = false;
    private StringBuffer script = null;
    protected String imageTitle = "Main Menu";

    @Override
    public boolean init() {
        this.setRefreshOnResize(false);
        this.setTimeout(-1);
        this.script = new StringBuffer();
        this.setUpProperties();
        return true;
    }

    private void renderMenu(StringBuffer html, PropertyListCollection menu, MenuType menuType) {
        String mt = this.element.getProperty("menutype");
        if (menu != null) {
            if (menuType != MenuType.NAVIGATION && !this.element.getProperty("showrootheader", "Y").equalsIgnoreCase("N")) {
                this.renderMenuHeader(html, this.elementid, this.element.getProperty("rootmenu", this.getGizmoDefId()), this.elementid);
            }
            int c = menu.size();
            this.renderMenu(html, menu, c, 0, menuType);
            if (menuType == MenuType.NAVIGATION) {
                int x = 0;
                int y = 0;
                try {
                    x = Integer.parseInt(this.element.getProperty("customx", "0"));
                    y = Integer.parseInt(this.element.getProperty("customy", "0"));
                }
                catch (Exception e) {
                    x = 0;
                    y = 0;
                }
                if (y == 0) {
                    int n = y = this.browser.isIE() ? -1 : 0;
                }
                if (x == 0) {
                    x = -75;
                }
                this.allmenus.put(this.elementid + "_" + (c + 1), new int[]{y, x});
                html.append("<span style=\"display:none;\" id=\"e_").append(this.elementid).append("_").append(c + 1).append("\" class=\"menugizmo_menu menugizmo_full menugizmo_menu_up menugizmo_menu_more\" _menuid=\"").append(LABVANTAGE_CVS_ID).append("\">");
                html.append(this.getTranslationProcessor().translate("More"));
                html.append("</span>");
                html.append("<div class=\"menugizmo_div menugizmo_nav\" style=\"display:none;position:absolute;\" id=\"").append("mdiv_").append(this.elementid).append("_").append(c + 1).append("\">");
                html.append("<ul id=\"").append(this.elementid + "_").append(c + 1).append("\">");
                this.renderMenu(html, menu, menu.size(), 0, MenuType.STANDARD);
                html.append("</ul>");
                html.append("</div>");
            }
        }
    }

    private void renderLink(StringBuffer html, PropertyList menuItem, String id, String ownerId) {
        String url = menuItem.getProperty("link");
        String text = menuItem.getProperty("text");
        String trans = menuItem.getProperty("text").length() > 0 ? this.getTranslationProcessor().translate(text) : LABVANTAGE_CVS_ID;
        String t = LABVANTAGE_CVS_ID + StringUtil.replaceAll(DOMUtil.convertChars(trans), "'", "\\'");
        String link = "#";
        if (!text.equals("-")) {
            link = url.toLowerCase().startsWith("javascript:") ? url : "sapphire.page.navigate('" + url + "','" + menuItem.getProperty("releaselock", "Y") + "',null, null, true, document.getElementById('" + id + "_" + ownerId + "'))";
            link = "javascript:menuGizmo.navigate('" + ownerId + "','mdiv_" + (ownerId == this.elementid ? this.elementid : this.elementid + "_" + ownerId) + "','" + id + "','" + t + "',function(){" + link + "})";
        }
        String customclass = menuItem.getProperty("customclass");
        html.append("<a id=\"").append(id).append("_").append(ownerId).append("\"").append(menuItem.getProperty("alias").length() > 0 ? " _alias=\"" + menuItem.getProperty("alias") + "\"" : LABVANTAGE_CVS_ID).append(" title=\"").append(this.getTranslationProcessor().translate(menuItem.getProperty("tip"))).append("\" href=\"").append(link).append("\" class=\"menugizmo_link").append(customclass.length() > 0 ? " " + customclass : LABVANTAGE_CVS_ID).append("\" _menuid=\"").append(ownerId).append("\">");
        if (trans.length() > 0) {
            html.append(trans);
        }
        html.append("</a>");
    }

    private void renderMenuHeader(StringBuffer html, String id, String menuGizmo, String ownerId) {
        if (this.element.getProperty("menuheader").length() > 0) {
            String headerlink = "#";
            if (this.element.getProperty("menuheaderclick").length() > 0) {
                headerlink = this.element.getProperty("menuheaderclick") + "('" + menuGizmo + "')";
            }
            String link = "javascript:menuGizmo.navigate('" + ownerId + "','mdiv_" + this.elementid + "_" + ownerId + "','" + id + "','" + this.element.getProperty("menuheader") + "',function(){" + headerlink + "})";
            html.append("<li>");
            html.append("<a id=\"" + id + "_head\" href=\"" + link + "\" class=\"menugizmo_header\">");
            html.append(this.element.getProperty("menuheader"));
            html.append("</a>");
            html.append("</li>");
        }
    }

    private void renderMenuItem(StringBuffer html, PropertyList menuItem, MenuType menuType, boolean topMost, String ownerId) {
        if (menuItem != null) {
            MenuItemType type = (topMost || !this.element.getProperty("showchildren", "Y").equalsIgnoreCase("N")) && (menuItem.getProperty("submenu").length() > 0 || menuItem.getCollection("menus") != null && menuItem.getCollection("menus").size() > 0) ? MenuItemType.SUBMENU : MenuItemType.MENU_ITEM;
            String mid = StringUtil.replaceAll(StringUtil.replaceAll(menuItem.getProperty("id", menuItem.getId()), "&", "n"), " ", "_").replaceAll("[^\\w\\s]", LABVANTAGE_CVS_ID);
            String id = this.elementid + "_" + mid;
            if (type == MenuItemType.MENU_ITEM) {
                if (!menuItem.getProperty("show", "Y").equalsIgnoreCase("N") && !this.element.getProperty("showlinks", "Y").equalsIgnoreCase("N")) {
                    html.append("<li>");
                    this.renderLink(html, menuItem, id, ownerId);
                    html.append("</li>");
                }
            } else if (topMost || !this.element.getProperty("showchildren", "Y").equalsIgnoreCase("N")) {
                PropertyListCollection menus = null;
                String submenu = menuItem.getProperty("submenu");
                if (submenu.length() > 0) {
                    sapphire.pageelements.BaseGizmo bg = BaseGizmo.getInstance(this.pageContext, submenu, true);
                    if (bg != null && bg instanceof MenuGizmo && (menus = ((MenuGizmo)bg).element.getCollection("tabs")) == null) {
                        menus = ((MenuGizmo)bg).element.getCollection("menu");
                    }
                } else {
                    menus = menuItem.getCollection("menus");
                }
                if (menus != null && menus.size() > 0) {
                    if (!topMost) {
                        html.append("<li>");
                        String customclass = menuItem.getProperty("customclass");
                        html.append("<a href=\"#\" class=\"menugizmo_parent").append(customclass.length() > 0 ? " " + customclass : LABVANTAGE_CVS_ID).append("\">");
                        html.append(this.getTranslationProcessor().translate(menuItem.getProperty("text", submenu)));
                        html.append("</a>");
                    }
                    if (menuType == MenuType.NAVIGATION) {
                        html.append("<ul id=\"").append(id).append("\">");
                    } else {
                        html.append("<ul>");
                    }
                    this.renderMenuHeader(html, id, submenu, ownerId);
                    this.renderMenuItems(html, menus, menuType, false, mid);
                    if (topMost && menuType == MenuType.NAVIGATION) {
                        html.append("</ul>");
                        html.append("</div>");
                    } else {
                        html.append("</ul>");
                    }
                    if (!topMost) {
                        html.append("</li>");
                    }
                } else if (topMost && menuType == MenuType.NAVIGATION) {
                    html.append("</ul>");
                    html.append("</div>");
                } else {
                    html.append("</ul>");
                }
            }
        }
    }

    private void renderMenuItems(StringBuffer html, PropertyListCollection menuItems, MenuType menuType, boolean topMost, String ownerId) {
        for (int mi = 0; mi < menuItems.size(); ++mi) {
            PropertyList menuItem = menuItems.getPropertyList(mi);
            this.renderMenuItem(html, menuItem, menuType, topMost, ownerId);
        }
    }

    public static LinkedHashMap<String, String[]> getMenuColors(boolean oldstyle) {
        LinkedHashMap<String, String[]> colors = new LinkedHashMap<String, String[]>();
        Object color = null;
        if (oldstyle) {
            colors.put("red", new String[]{"#DC143C", "#1a1a1a", "red"});
            colors.put("yellow", new String[]{"#FFA500", "#1a1a1a", "yellow"});
            colors.put("green", new String[]{"#008000", "#1a1a1a", "green"});
            colors.put("darkblue", new String[]{"#4682B4", "#1a1a1a", "darkblue"});
            colors.put("pink", new String[]{"#C71585", "#1a1a1a", "pink"});
            colors.put("lightgreen", new String[]{"#000000", "#1a1a1a", "lightgreen"});
            colors.put("lightblue", new String[]{"#6A5ACD", "#1a1a1a", "lightblue"});
            colors.put("brown", new String[]{"#9A3F48", "#1a1a1a", "brown"});
            colors.put("maroon", new String[]{"#DC143C", "#ffffff", "red"});
            colors.put("orange", new String[]{"#E57D21", "#ffffff", "yellow"});
            colors.put("black", new String[]{"#1A1A1A", "#ffffff", "brown"});
            colors.put("teal", new String[]{"#60B5B3", "#ffffff", "darkblue"});
            colors.put("purple", new String[]{"#67308F", "#ffffff", "lightblue"});
        } else {
            colors.put("red", new String[]{"#D83A29", "#FFFFFF", "red"});
            colors.put("orange", new String[]{"#E57D21", "#ffffff", "orange"});
            colors.put("yellow", new String[]{"#EBC412", "#1a1a1a", "yellow"});
            colors.put("green", new String[]{"#349946", "#FFFFFF", "green"});
            colors.put("lightblue", new String[]{"#43A2D6", "#1a1a1a", "lightblue"});
            colors.put("purple", new String[]{"#67308F", "#ffffff", "purple"});
            colors.put("maroon", new String[]{"#9A3F48", "#ffffff", "maroon"});
            colors.put("lightgreen", new String[]{"#95E700", "#ffffff", "lightgreen"});
            colors.put("darkblue", new String[]{"#4682B4", "#ffffff", "darkblue"});
            colors.put("pink", new String[]{"#FE7BAC", "#1a1a1a", "pink"});
            colors.put("black", new String[]{"#1A1A1A", "#ffffff", "black"});
            colors.put("teal", new String[]{"#60B5B3", "#ffffff", "teal"});
            colors.put("brown", new String[]{"#C7753F", "#ffffff", "brown"});
        }
        return colors;
    }

    private void renderMenu(StringBuffer html, PropertyListCollection menus, int numberOfItems, int startitem, MenuType menuType) {
        if (menus != null) {
            int x = 0;
            int y = 0;
            try {
                x = Integer.parseInt(this.element.getProperty("customx", "0"));
                y = Integer.parseInt(this.element.getProperty("customy", "0"));
            }
            catch (Exception e) {
                x = 0;
                y = 0;
            }
            if (y == 0) {
                int n = y = this.browser.isIE() ? -1 : 0;
            }
            if (x == 0) {
                x = -75;
            }
            LinkedHashMap<String, String[]> colors = null;
            Iterator<String> color = null;
            boolean ignorecolor = this.element.getProperty("ignorecolor", "N").equalsIgnoreCase("Y");
            if (menuType == MenuType.SITEMAP && !ignorecolor) {
                colors = MenuGizmo.getMenuColors(false);
                color = colors.keySet().iterator();
            }
            for (int t = startitem; t < numberOfItems; ++t) {
                PropertyList menuitem = menus.getPropertyList(t);
                if (menuitem.getProperty("show", "Y").equalsIgnoreCase("N") || menuitem.getProperty("text").length() <= 0) continue;
                MenuItemType type = menuitem.getProperty("submenu").length() > 0 || menuitem.getCollection("menus") != null && menuitem.getCollection("menus").size() > 0 ? MenuItemType.SUBMENU : MenuItemType.MENU_ITEM;
                String mid = StringUtil.replaceAll(StringUtil.replaceAll(menuitem.getProperty("id", menuitem.getId()), "&", "n"), " ", "_").replaceAll("[^\\w\\s]", LABVANTAGE_CVS_ID);
                String id = this.elementid + "_" + mid;
                if (menuType == MenuType.NAVIGATION) {
                    if (type == MenuItemType.MENU_ITEM) {
                        html.append("<span id=\"e_").append(id).append("\" class=\"menugizmo_menu menugizmo_full menugizmo_topitem menugizmo_menu_up\" _menuid=\"").append(mid).append("\">");
                        this.renderLink(html, menuitem, id, id);
                        html.append("</span>");
                    } else {
                        this.allmenus.put(id, new int[]{y, x});
                        html.append("<span id=\"e_").append(id).append("\" class=\"menugizmo_menu menugizmo_full menugizmo_menu_up\" _menuid=\"").append(mid).append("\">");
                        html.append(this.getTranslationProcessor().translate(menuitem.getProperty("text")));
                        html.append("</span>");
                        html.append("<div class=\"menugizmo_div menugizmo_nav\" style=\"display:none;position:absolute;\" id=\"").append("mdiv_").append(id).append("\">");
                        this.renderMenuItem(html, menuitem, MenuType.NAVIGATION, true, mid);
                    }
                } else if (menuType == MenuType.SITEMAP) {
                    String colorChoice;
                    String string = colorChoice = ignorecolor ? LABVANTAGE_CVS_ID : menuitem.getProperty("colorcode", menuitem.getProperty("color"));
                    if (colorChoice.length() == 0 && colors != null && color != null) {
                        if (color.hasNext()) {
                            colorChoice = color.next();
                        } else {
                            color = colors.keySet().iterator();
                            colorChoice = color.next();
                        }
                    }
                    String backColor = LABVANTAGE_CVS_ID;
                    String foreColor = LABVANTAGE_CVS_ID;
                    if (colorChoice.length() > 0 && colors != null && colors.containsKey(colorChoice)) {
                        String[] c = colors.get(colorChoice);
                        backColor = c[0];
                        foreColor = c[1];
                    } else {
                        backColor = colorChoice;
                        foreColor = "#000000";
                    }
                    html.append("<div id=\"e_").append(id).append("\" class=\"menugizmo_menu menugizmo_sitemap\" style=\"display:block;\" _menuid=\"").append(mid).append("\">");
                    html.append("<div class=\"menugizmo_header\" " + (backColor.length() > 0 && foreColor.length() > 0 ? " style=\"" + (backColor.length() > 0 ? "background-color:" + backColor + ";" : LABVANTAGE_CVS_ID) + (foreColor.length() > 0 ? "color:" + foreColor + ";" : LABVANTAGE_CVS_ID) + "\"" : LABVANTAGE_CVS_ID) + ">");
                    html.append(this.getTranslationProcessor().translate(menuitem.getProperty("text")));
                    html.append("</div>");
                    html.append("<div class=\"menugizmo_container\" id=\"").append(LABVANTAGE_CVS_ID).append(id).append("\">");
                    this.renderMenuItem(html, menuitem, MenuType.SITEMAP, true, mid);
                    html.append("</div>");
                    html.append("</div>");
                } else {
                    html.append("<li>");
                    if (type == MenuItemType.MENU_ITEM) {
                        this.renderLink(html, menuitem, id, this.elementid);
                    } else {
                        String customclass = menuitem.getProperty("customclass");
                        html.append("<a href=\"#\" class=\"menugizmo_parent").append(customclass.length() > 0 ? " " + customclass : LABVANTAGE_CVS_ID).append("\">");
                        html.append(this.getTranslationProcessor().translate(menuitem.getProperty("text")));
                        html.append("</a>");
                        this.renderMenuItem(html, menuitem, MenuType.STANDARD, true, this.elementid);
                    }
                }
                if (menuType == MenuType.NAVIGATION || menuType == MenuType.SITEMAP) continue;
                html.append("</li>");
            }
        }
    }

    protected void getCustomMenu(StringBuffer html, PropertyListCollection links) {
        try {
            if (links != null && links.size() > 0) {
                for (int i = 0; i < links.size(); ++i) {
                    PropertyList link = links.getPropertyList(i);
                    String url = link.getProperty("link", LABVANTAGE_CVS_ID);
                    if (link.getProperty("show", "Y").equalsIgnoreCase("N")) continue;
                    String txt = link.getProperty("text");
                    if (!txt.equalsIgnoreCase("-")) {
                        txt = txt.length() > 0 ? this.getTranslationProcessor().translate(txt) : txt;
                    }
                    String t = LABVANTAGE_CVS_ID + StringUtil.replaceAll(DOMUtil.convertChars(txt), "'", "\\'");
                    if (url.length() > 0) {
                        String id = this.elementid + "_" + i;
                        String l = url.toLowerCase().startsWith("javascript:") ? url : "sapphire.page.navigate('" + url + "','" + link.getProperty("releaselock", "Y") + "', null, null, true,document.getElementById('e_" + this.elementid + "'))";
                        l = "javascript:menuGizmo.navigate('" + this.elementid + "','mdiv_" + this.elementid + "','" + id + "','" + t + "',function(){" + l + "})";
                        html.append("<li id=\"").append(this.elementid).append("_").append(link.getProperty("id", LABVANTAGE_CVS_ID + i)).append("\">");
                        if (!txt.equalsIgnoreCase("-")) {
                            html.append("<a href=\"").append(l).append("\">");
                            if (link.getProperty("imageid").length() > 0) {
                                Image image = new Image(this.pageContext);
                                image.setImageId(link.getProperty("imageid"));
                                if (link.getProperty("imagetitle").length() > 0) {
                                    image.setTitle(this.getTranslationProcessor().translate(link.getProperty("imagetitle")));
                                }
                                image.setDimensions(16, 16);
                                image.setStyle("vertical-align:middle;");
                                html.append(image.getHtml());
                            }
                            if (txt.length() > 0) {
                                if (link.getProperty("imageid").length() > 0) {
                                    html.append("&nbsp;");
                                }
                                html.append(txt);
                            } else {
                                html.append("<span style=\"color:transparent;\">|</span>");
                            }
                            html.append("</a>");
                            PropertyListCollection s = link.getCollection("sublinks");
                            if (s != null && s.size() > 0) {
                                html.append("<ul>");
                                this.getCustomMenu(html, s);
                                html.append("</ul>");
                            }
                        } else {
                            html.append("-");
                        }
                        html.append("</li>");
                        continue;
                    }
                    if (!txt.equalsIgnoreCase("-")) continue;
                    html.append("<li id=\"").append(this.elementid).append("_").append(link.getProperty("id", LABVANTAGE_CVS_ID + i)).append("\">");
                    html.append("-");
                    html.append("</li>");
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static String getStartStyleAndScript(boolean rtl, boolean useFullIncludes) {
        StringBuffer html = new StringBuffer();
        html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/menugizmo.js\"></script>");
        html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/modules/dashboard/style/menugizmo.css", rtl, useFullIncludes) + "\" type=\"text/css\">");
        html.append("<style>.ui-menu { min-width: 150px; }</style>");
        return html.toString();
    }

    public static String renderNavigationDiv(String id, String contents) {
        StringBuffer html = new StringBuffer();
        html.append("<div class=\"ws_navigation\" id=\"" + id + "\" data-breakpoint=\"800\">");
        html.append(contents);
        html.append("</div>");
        return html.toString();
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor translationProcessor = this.pageContext == null ? new TranslationProcessor(this.getConnectionid()) : this.getTranslationProcessor();
        if (this.element != null) {
            PropertyListCollection menus;
            String mt;
            boolean ajaxRender = this.element.getProperty("ajaxrender", "N").equalsIgnoreCase("Y");
            boolean renderincludes = this.element.getProperty("renderincludes", "Y").equalsIgnoreCase("Y");
            if (!ajaxRender && renderincludes) {
                html.append(MenuGizmo.getStartStyleAndScript(this.connectionInfo.isRtl(), this.connectionInfo.getUseFullIncludes()));
                html.append(JavaScriptAPITag.getJQueryAPI(true, false, null, this.pageContext));
            }
            if ((mt = this.element.getProperty("menutype")).equalsIgnoreCase("navigation")) {
                PropertyList submenu;
                menus = this.element.getCollection("tabs");
                if (menus == null) {
                    menus = this.element.getCollection("menu");
                }
                if (menus != null && this.element.getProperty("type").equalsIgnoreCase("Single Item") && this.element.getProperty("singlemenu").length() > 0 && (submenu = menus.find("id", this.element.getProperty("singlemenu"), false)) != null) {
                    menus = submenu.getCollection("menus");
                }
                if (ajaxRender) {
                    this.renderMenu(html, menus, MenuType.NAVIGATION);
                } else {
                    StringBuffer htmltemp = new StringBuffer();
                    this.renderMenu(htmltemp, menus, MenuType.NAVIGATION);
                    html.append(MenuGizmo.renderNavigationDiv(this.elementid, htmltemp.toString()));
                }
            } else if (mt.equalsIgnoreCase("custom")) {
                String customHTML = this.element.getProperty("customhtml", LABVANTAGE_CVS_ID);
                StringBuffer temp = new StringBuffer();
                if (!ajaxRender) {
                    temp.append("<ul id=\"" + this.elementid + "\">");
                }
                this.getCustomMenu(temp, this.element.getCollection("custommenu"));
                if (!ajaxRender) {
                    temp.append("</ul>");
                }
                if (customHTML.length() > 0 && !this.smallRendered) {
                    int x = 0;
                    int y = 0;
                    try {
                        x = Integer.parseInt(this.element.getProperty("customx", "0"));
                        y = Integer.parseInt(this.element.getProperty("customy", "0"));
                    }
                    catch (Exception e) {
                        x = 0;
                        y = 0;
                    }
                    this.allmenus.put(this.elementid, new int[]{y, x});
                    this.renderMenu(html, customHTML, temp.toString(), this.element.getProperty("customclass", LABVANTAGE_CVS_ID));
                } else {
                    html.append(temp);
                }
            } else if (this.getGizmoStyle() == BaseGizmo.GizmoStyle.FULL) {
                menus = this.element.getCollection("tabs");
                if (menus == null) {
                    menus = this.element.getCollection("menu");
                }
                if (menus != null) {
                    Cloneable submenu;
                    boolean si = true;
                    if (!this.element.getProperty("type").equalsIgnoreCase("Single Item")) {
                        for (int i = 0; i < menus.size(); ++i) {
                            submenu = menus.getPropertyList(i).getCollection("menus");
                            if (submenu == null || ((ArrayList)submenu).size() <= 0) continue;
                            si = false;
                            break;
                        }
                    }
                    if (si) {
                        PropertyListCollection tousemenus = null;
                        if (this.element.getProperty("singlemenu").length() > 0) {
                            submenu = menus.find("id", this.element.getProperty("singlemenu"), false);
                            if (submenu != null) {
                                tousemenus = ((PropertyList)submenu).getCollection("menus");
                            }
                        } else {
                            tousemenus = menus;
                        }
                        if (tousemenus != null) {
                            PropertyListCollection newmenus = new PropertyListCollection();
                            PropertyList newmenu = new PropertyList();
                            newmenu.setProperty("id", "NewMenu");
                            newmenu.setProperty("text", this.getTitle());
                            newmenu.setProperty("menus", tousemenus);
                            newmenus.add(newmenu);
                            menus = newmenus;
                        }
                    }
                }
                html.append("<div id=\"").append(this.elementid).append("_container\" style=\"vertical-align:top;margin:0 auto;height:auto;width:auto;display:flex;flex-wrap:wrap;justify-content:center;\">");
                this.renderMenu(html, menus, MenuType.SITEMAP);
                html.append("</div>");
                this.script.append("menuGizmo.setUpCards('").append(this.elementid).append("_container');");
            } else {
                PropertyList submenu;
                if (!ajaxRender) {
                    html.append("<ul id=\"" + this.elementid + "\">");
                }
                if ((menus = this.element.getCollection("tabs")) == null) {
                    menus = this.element.getCollection("menu");
                }
                if (menus != null && this.element.getProperty("type").equalsIgnoreCase("Single Item") && this.element.getProperty("singlemenu").length() > 0 && (submenu = menus.find("id", this.element.getProperty("singlemenu"), false)) != null) {
                    menus = submenu.getCollection("menus");
                }
                this.renderMenu(html, menus, MenuType.STANDARD);
                if (!ajaxRender) {
                    html.append("</ul>");
                }
            }
            if (Configuration.isDevmode(this.getConnectionProcessor().getSapphireConnection().getDatabaseId())) {
                this.script.append("menuGizmo.isDebug = true;");
            }
            for (String menu : this.allmenus.keySet()) {
                this.script.append("menuGizmo.register('" + menu + "'");
                if (mt.equalsIgnoreCase("navigation")) {
                    this.script.append(", true");
                    if (this.element.getProperty("click", "Y").equalsIgnoreCase("Y")) {
                        this.script.append(", true");
                    } else {
                        this.script.append(", false");
                    }
                    if (this.element.getProperty("mouseover", "N").equalsIgnoreCase("Y")) {
                        this.script.append(", true");
                    } else {
                        this.script.append(", false");
                    }
                } else if (mt.equalsIgnoreCase("custom")) {
                    this.script.append(", false");
                    if (this.element.getProperty("click", "Y").equalsIgnoreCase("Y")) {
                        this.script.append(", true");
                    } else {
                        this.script.append(", false");
                    }
                    if (this.element.getProperty("mouseover", "N").equalsIgnoreCase("Y")) {
                        this.script.append(", true");
                    } else {
                        this.script.append(", false");
                    }
                } else {
                    this.script.append(", false");
                    this.script.append(", true, false");
                }
                int[] params = this.allmenus.get(menu);
                if (params.length == 0 || params.length > 2) {
                    this.script.append(", undefined");
                    this.script.append(", undefined");
                } else if (params.length == 1) {
                    this.script.append(", ").append(params[0]);
                    this.script.append(", undefined");
                } else {
                    this.script.append(", ").append(params[0]);
                    this.script.append(", ").append(params[1]);
                }
                String dyanmicMenuAjaxClass = this.element.getProperty("dynamicmenuajaxclass");
                if (dyanmicMenuAjaxClass.length() > 0) {
                    this.script.append(", '").append(dyanmicMenuAjaxClass).append("'");
                    String dyanmicMenuAjaxProps = this.element.getProperty("dynamicmenuajaxprops");
                    if (dyanmicMenuAjaxProps.length() > 0) {
                        this.script.append(", ").append(dyanmicMenuAjaxProps);
                    } else {
                        this.script.append(", null");
                    }
                } else {
                    this.script.append(", ''");
                    this.script.append(", null");
                }
                if (this.element.getProperty("onshow").length() > 0) {
                    this.script.append(", ").append(this.element.getProperty("onshow"));
                } else {
                    this.script.append(", null");
                }
                this.script.append(");");
            }
        } else {
            html.append("<font size=2>").append(translationProcessor.translate("No element data found.")).append("</font>");
        }
        return html.toString();
    }

    @Override
    public String getScript() {
        return this.script == null ? LABVANTAGE_CVS_ID : this.script.toString();
    }

    @Override
    public String getURL() {
        return LABVANTAGE_CVS_ID;
    }

    private void renderMenu(StringBuffer html, String clickContent, String menuContent, String className) {
        BaseGizmo.GizmoStyle gizmoStyle = this.getGizmoStyle();
        String menuclass = gizmoStyle == BaseGizmo.GizmoStyle.TEXT ? "menugizmo_textstyle" : LABVANTAGE_CVS_ID;
        html.append("<span id=\"e_").append(this.elementid).append("\" class=\"menugizmo_menu menugizmo_menu_up ").append(menuclass).append("\" _menuid=\"").append(this.elementid).append("\" ").append(this.element.getProperty("forcepositioncss", LABVANTAGE_CVS_ID).length() > 0 ? "data-forcecss=\"" + StringUtil.replaceAll(this.element.getProperty("forcepositioncss", LABVANTAGE_CVS_ID), "\"", "&quot;") + "\"" : LABVANTAGE_CVS_ID).append(">");
        html.append(clickContent);
        html.append("</span>");
        html.append("<div class=\"menugizmo_div").append(className.length() > 0 ? " " + className : LABVANTAGE_CVS_ID).append("\" style=\"display:none;position:absolute;\" id=\"mdiv_" + this.elementid + "\">");
        html.append(menuContent);
        html.append("</div>");
    }

    protected void setUpProperties() {
    }

    @Override
    public String getIconHtml() {
        BaseGizmo.GizmoStyle gizmoStyle = this.getGizmoStyle();
        BaseGizmo.GizmoLocation gizmoLocation = this.getGizmoLocation();
        this.element.setProperty("menutype", "button");
        StringBuffer html = new StringBuffer();
        if (gizmoLocation == BaseGizmo.GizmoLocation.TOPBAR) {
            this.allmenus.put(this.elementid, new int[]{10});
        } else if (gizmoLocation == BaseGizmo.GizmoLocation.SIDEBAR) {
            this.allmenus.put(this.elementid, new int[]{-20, 90});
        } else {
            this.allmenus.put(this.elementid, new int[]{15});
        }
        this.smallRendered = true;
        if (gizmoStyle == BaseGizmo.GizmoStyle.TEXT) {
            String t = this.getTitle();
            t = SafeHTML.encodeForHTML(t, true);
            String h = this.getHelpText();
            h = SafeHTML.encodeForHTML(h, true);
            StringBuffer embed = new StringBuffer();
            embed.append("<span title=\"").append(h).append("\" class=\"").append(gizmoStyle.className).append("_txt").append("\"").append(" _menuid=\"").append(this.elementid).append("\">");
            embed.append(t);
            embed.append("</span>");
            this.renderMenu(html, embed.toString(), this.isPreviewMode() ? LABVANTAGE_CVS_ID : this.getHtml(), this.element.getProperty("customclass", LABVANTAGE_CVS_ID));
        } else {
            StringBuffer embed = new StringBuffer();
            String h = this.getHelpText();
            h = SafeHTML.encodeForHTML(h, true);
            embed.append("<span title=\"").append(h).append("\" class=\"").append(gizmoStyle.className).append("_img").append("\"").append(" _menuid=\"").append(this.elementid).append("\">");
            embed.append(this.getIcon());
            embed.append("</span>");
            if (gizmoStyle.showTitle) {
                String t = this.getTitle();
                t = SafeHTML.encodeForHTML(t, true);
                embed.append("<span").append(gizmoStyle.className.length() > 0 ? "  class=\"" + gizmoStyle.className + "_txt\"" : LABVANTAGE_CVS_ID).append(" title=\"").append(this.getHelpText()).append("\" _menuid=\"").append(this.elementid).append("\">");
                embed.append(t);
                embed.append("</span>");
            }
            this.renderMenu(html, embed.toString(), this.isPreviewMode() ? LABVANTAGE_CVS_ID : this.getHtml(), this.element.getProperty("customclass", LABVANTAGE_CVS_ID));
        }
        return html.toString();
    }

    @Override
    public String getIcon() {
        return this.getImage(this.getTranslationProcessor().translate(this.imageTitle), this.getGizmoStyle().size).getHtml();
    }

    @Override
    public String getDefaultImageSrc() {
        return this.element.getProperty("topmenu", "Y").equalsIgnoreCase("Y") ? IMAGE_TOP : IMAGE_SUB;
    }

    public static enum MenuType {
        NAVIGATION,
        SITEMAP,
        STANDARD;

    }

    public static enum MenuItemType {
        MENU_ITEM,
        SUBMENU;

    }
}

