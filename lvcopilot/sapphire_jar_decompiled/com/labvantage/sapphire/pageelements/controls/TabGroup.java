/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.controls;

import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.util.ArrayList;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class TabGroup
extends BaseElement {
    private String id;
    private String context = null;
    private String tip = "";
    private String bodyWidth = "100%";
    private String bodyHeight = "";
    private String appearance = "standard";
    private boolean multiTab = true;
    private boolean useChangeTab = true;
    private int selectedTab = 0;
    private ArrayList tabs = new ArrayList();

    public void setId(String id) {
        id = StringUtil.replaceAll(id, "(", "_");
        id = StringUtil.replaceAll(id, ")", "_");
        id = StringUtil.replaceAll(id, "\ufb22", "_");
        id = StringUtil.replaceAll(id, "\ufb1d", "_");
        id = StringUtil.replaceAll(id, "-", "_");
        id = StringUtil.replaceAll(id, "'", "_");
        id = StringUtil.replaceAll(id, "\"", "_");
        this.id = id = StringUtil.replaceAll(id, "&", "_");
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public void setUseChangeTab(boolean useChangeTab) {
        this.useChangeTab = useChangeTab;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public void setBodywidth(String bodyWidth) {
        this.bodyWidth = bodyWidth;
    }

    public void setBodyheight(String bodyHeight) {
        this.bodyHeight = bodyHeight;
    }

    public void setMultiTab(boolean multiTab) {
        this.multiTab = multiTab;
    }

    public void setSelectedTab(int tabIndex) {
        this.selectedTab = tabIndex;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setTab(Tab tab) {
        this.tabs.add(tab);
    }

    @Override
    public String getHtml() {
        int nextSelectedTab = 0;
        if (this.pageContext != null) {
            try {
                if (this.context != null && this.context.length() > 0) {
                    RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
                    PropertyList userConfig = requestContext.getPropertyList("userconfig");
                    String enId = sapphire.util.HttpUtil.encodeURIComponent(this.id);
                    String lastContext = userConfig.getProperty("tab_" + enId + "__lastcontext");
                    String lastSelected = userConfig.getProperty("tab_" + enId + "__lastselected");
                    if (lastContext.equals(this.context)) {
                        nextSelectedTab = Integer.parseInt(lastSelected);
                    }
                }
            }
            catch (Exception requestContext) {
                // empty catch block
            }
        }
        StringBuffer html = new StringBuffer();
        html.append(this.getJavaScriptAPI());
        if (this.id != null && this.id.length() > 0) {
            if (this.multiTab) {
                html.append(this.getTabGroupHtml());
            } else {
                html.append(this.getSeparateTabsHtml());
            }
        } else {
            html.append("TAG ERROR: id not defined for tab tag");
        }
        html.append("<script type=\"text/javascript\">");
        if (nextSelectedTab == 0) {
            html.append("if ( typeof sapphire != 'undefined' ) {");
            html.append("    sapphire.userConfig.set( \"tab_").append(sapphire.util.HttpUtil.encodeURIComponent(this.id)).append("__lastselected\", '0' )\n;");
            html.append("}");
        } else {
            html.append("if ( typeof sapphire != 'undefined' ) {");
            html.append("  sapphire.events.registerLoadListener(new Function( 'var oEl = document.getElementById( \"tab_").append(this.id).append("__tab").append(nextSelectedTab).append("\" ); if (oEl != null) sapphire.events.fireEvent( oEl, \"onclick\" );'), false, 0);");
            html.append("}");
        }
        html.append("</script>");
        return html.toString();
    }

    public String getTabGroupHtml() {
        Tab tab;
        int i;
        StringBuffer html = new StringBuffer();
        boolean renderStyleNew = "modern".equalsIgnoreCase(this.appearance);
        this.appearance = renderStyleNew ? "modernstandard " : this.appearance;
        html.append("<script type=\"text/javascript\">\n");
        html.append("function changeTab_").append(this.id).append("( newTab ) {\n");
        html.append("if(typeof( listlayout )!='undefined' && typeof( listlayout.resize ) != 'undefined' ) listlayout.resize();");
        if (this.context != null && this.context.length() > 0) {
            html.append("if (typeof sapphire != 'undefined') {");
            String enId = sapphire.util.HttpUtil.encodeURIComponent(this.id);
            html.append("sapphire.userConfig.set(\"tab_").append(enId).append("__lastcontext\", '").append(this.context).append("');");
            html.append("sapphire.userConfig.set(\"tab_").append(enId).append("__lastselected\", newTab);");
            html.append("}");
        }
        html.append("var tabid = document.getElementById('tab_").append(this.id).append("__tab');");
        html.append("var currentTab = (tabid != null ? sapphire.util.dom.getAttribute(tabid,'currentTab') : newTab);");
        html.append("if (currentTab != newTab){");
        html.append("sapphire.util.dom.setAttribute(tabid,'currentTab',newTab);");
        html.append("var newTabElement = document.getElementById('tab_").append(this.id).append("__tab' + newTab );");
        html.append("var newTextDivElement = document.getElementById('tab_").append(this.id).append("__textdiv' + newTab );");
        html.append("if (newTabElement != null){");
        html.append("newTabElement.className = 'tab_").append(this.appearance).append("_tab_selected';");
        html.append("sapphire.util.dom.setAttribute(newTabElement,'selected','Y');");
        html.append("}");
        html.append("\n if (newTextDivElement != null ){ ");
        html.append("\n newTextDivElement.className = 'tab_text _selected';");
        html.append("\n }");
        html.append("var oCont1 = document.getElementById( 'tab_").append(this.id).append("__content' + newTab );");
        html.append("if (oCont1 != null) oCont1.style.display = 'block';");
        html.append("var currentTabElement = document.getElementById( 'tab_").append(this.id).append("__tab' + currentTab );");
        html.append("var currentTextDivElement = document.getElementById('tab_").append(this.id).append("__textdiv' + currentTab );");
        html.append("if (currentTabElement != null){");
        html.append("currentTabElement.className = 'tab_").append(this.appearance).append("_tab_unselected';");
        html.append("sapphire.util.dom.setAttribute(currentTabElement,'selected', 'N');");
        html.append("}");
        html.append("\n if (currentTextDivElement != null ){");
        html.append("\n currentTextDivElement.className='tab_text';");
        html.append("\n }");
        html.append("var oCont2 = document.getElementById('tab_").append(this.id).append("__content' + currentTab);");
        html.append("if (oCont2 != null) oCont2.style.display = 'none';");
        html.append("if (typeof(dataView) != 'undefined'){");
        html.append("var sElementId = dataView.getElementIdFromTab('").append(this.id).append("',newTab);");
        html.append("if(sElementId.length > 0) dataView.changeTab(newTab,sElementId,true);");
        html.append("}");
        html.append("}");
        html.append("}");
        html.append("</script>");
        html.append("<table width=\"").append(this.bodyWidth).append("\" height=\"").append(this.bodyHeight).append("\" border=\"0\" cellspacing=\"0\" style=\"background-color: white\" cellpadding=\"0\" id=\"tab_").append(this.id).append("__tab\" currentTab=\"").append(this.selectedTab).append("\">");
        if (this.bodyHeight.length() > 0) {
            html.append("<tr height=18><td ").append(renderStyleNew ? "class=\"tab_modernstandard_bodyheader\"" : "").append("><table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" ><tr ").append(renderStyleNew ? " class=\"tab_table_header\"" : "").append(">");
        } else {
            html.append("<tr><td ").append(renderStyleNew ? "class=\"tab_modernstandard_bodyheader\"" : "").append("><table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr ").append(renderStyleNew ? " class=\"tab_table_header\"" : "").append(">");
        }
        for (i = 0; i < this.tabs.size(); ++i) {
            String curTip;
            tab = (Tab)this.tabs.get(i);
            html.append("<td width=\"").append(tab.getWidth()).append("\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr>");
            html.append("\t<td nowrap text=\"").append(HttpUtil.htmlEncode(tab.getText())).append("\" id=\"tab_").append(this.id).append("__tab").append(i).append("\" class=\"tab_").append(this.appearance).append("_tab").append(i == this.selectedTab ? "_selected" : "_unselected").append("\" ");
            String string = curTip = tab.getTip().length() > 0 ? tab.getTip() : this.tip;
            if (curTip.length() > 0) {
                html.append(" title=\"").append(tab.getTip()).append("\" ");
            }
            html.append(" onMouseOver=\"if (sapphire.util.dom.getAttribute(this,'selected') == 'N') this.className='tab_").append(this.appearance).append("_tab_unselected_highlight';\" ");
            html.append(" onMouseOut =\"if (sapphire.util.dom.getAttribute(this,'selected') == 'N' ) this.className='tab_").append(this.appearance).append("_tab_unselected';\" ");
            html.append(" style=\"cursor: pointer;\"  selected=\"").append(i == this.selectedTab ? "Y" : "N").append("\" ");
            html.append(" tabNumber=\"").append(i).append("\" ");
            if (this.useChangeTab) {
                html.append(" onclick=\"changeTab_").append(this.id).append("( '").append(i).append("' );").append(tab.getAction() != null && tab.getAction().length() > 0 ? tab.getAction() : "").append(";\" ");
            } else {
                html.append(" onclick=\"").append(tab.getAction() != null && tab.getAction().length() > 0 ? tab.getAction() : "").append(";\" ");
            }
            html.append(">");
            if (renderStyleNew) {
                html.append("<div id=\"tab_").append(this.id).append("__textdiv").append(i).append("\" class=\"tab_text").append(i == this.selectedTab ? " _selected" : "").append("\">");
            }
            html.append(tab.getText());
            if (renderStyleNew) {
                html.append("</div>");
            }
            html.append("</td></tr></table></td>");
        }
        html.append("<td id=\"tab_").append(this.id).append("__spacer\" class=\"tab_").append(this.appearance).append("_spacer\" width=\"*\">&nbsp;</td>");
        html.append("</tr>");
        html.append("</table>");
        html.append("</td></tr>");
        html.append("<tr>");
        html.append("\t<td id=\"tab_").append(this.id).append("__body\" class=\"tab_").append(this.appearance).append("_body\">");
        for (i = 0; i < this.tabs.size(); ++i) {
            tab = (Tab)this.tabs.get(i);
            html.append("\t<div id=\"tab_").append(this.id).append("__content").append(i).append("\" class=\"tab_content\" tabnumber=\"").append(i).append("\" style=\"");
            if (!tab.getBodyHeight().equals("")) {
                html.append("height: ").append(tab.getBodyHeight()).append("; ");
            }
            if (!tab.getBodyWidth().equals("")) {
                html.append("width: ").append(tab.getBodyWidth()).append("; ");
            }
            if (this.requestContext != null && this.requestContext.getProperty("html5").equalsIgnoreCase("Y")) {
                html.append("position:relative; ");
            }
            html.append(" overflow: auto;" + (this.browser != null && this.browser.isMobile() ? "-webkit-overflow-scrolling:touch;" : "") + "display: ").append(i == this.selectedTab ? "block" : "none").append("\">\n");
            html.append(tab.getContent());
            html.append("</div>\n");
        }
        html.append("</td></tr></table>\n");
        return html.toString();
    }

    public String getSeparateTabsHtml() {
        StringBuffer html = new StringBuffer();
        for (int i = 0; i < this.tabs.size(); ++i) {
            Tab tab = (Tab)this.tabs.get(i);
            html.append(tab.getHtml()).append("<br>");
        }
        return html.toString();
    }
}

