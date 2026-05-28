/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.controls;

import com.labvantage.sapphire.util.http.HttpUtil;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Tab
extends BaseElement {
    private String id;
    private String context = null;
    private String text;
    private String collapsedText = "";
    private String width = "1%";
    private String tip = "";
    private String bodyWidth = "100%";
    private String bodyHeight = "";
    private String plusGif = "WEB-CORE/elements/images/plus.gif";
    private String minusGif = "WEB-CORE/elements/images/minus.gif";
    private String expandImg = "WEB-CORE/imageref/flat/16/flat_black_angle_down.svg";
    private String collapseImg = "WEB-CORE/imageref/flat/16/flat_black_angle_up.svg";
    private String expandable = "false";
    private String expanded = "false";
    private String appearance = "standard";
    private String highlight = "true";
    private String content = "";
    private String action = "";

    public void setId(String id) {
        id = StringUtil.replaceAll(id, "(", "_");
        id = StringUtil.replaceAll(id, ")", "_");
        id = StringUtil.replaceAll(id, "\ufb22", "_");
        id = StringUtil.replaceAll(id, "\ufb1d", "_");
        id = StringUtil.replaceAll(id, "-", "_");
        id = StringUtil.replaceAll(id, "+", "_");
        id = StringUtil.replaceAll(id, "'", "_");
        id = StringUtil.replaceAll(id, "\"", "_");
        this.id = id = StringUtil.replaceAll(id, "&", "_");
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCollapsedtext(String collapsedText) {
        this.collapsedText = collapsedText;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public void setBodywidth(String bodyWidth) {
        this.bodyWidth = bodyWidth;
    }

    public void setBodyheight(String bodyHeight) {
        this.bodyHeight = bodyHeight;
    }

    public void setExpandable(String expandable) {
        this.expandable = expandable;
    }

    public void setExpanded(String expanded) {
        this.expanded = expanded;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }

    public String getCollapsedText() {
        return this.collapsedText;
    }

    public String getWidth() {
        return this.width;
    }

    public String getTip() {
        return this.tip;
    }

    public String getBodyWidth() {
        return this.bodyWidth;
    }

    public String getBodyHeight() {
        return this.bodyHeight;
    }

    public String getPlusGif() {
        return this.plusGif;
    }

    public String getMinusGif() {
        return this.minusGif;
    }

    public String getExpandable() {
        return this.expandable;
    }

    public String getExpanded() {
        return this.expanded;
    }

    public String getAppearance() {
        return this.appearance;
    }

    public String getHighlight() {
        return this.highlight;
    }

    public String getContent() {
        return this.content;
    }

    public String getAction() {
        return this.action;
    }

    @Override
    public String getHtml() {
        boolean html5 = this.requestContext != null ? this.requestContext.getProperty("html5").equalsIgnoreCase("Y") : false;
        int selectedTab = 0;
        int nextSelectedTab = 0;
        boolean renderStyleNew = "modern".equalsIgnoreCase(this.appearance);
        String string = this.appearance = renderStyleNew ? "modernstandard " : this.appearance;
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
        if (this.id != null && this.id.length() > 0) {
            String[] tabText = StringUtil.split(this.text, ";");
            String[] tabAction = StringUtil.split(this.action, ";");
            int tabs = tabText.length;
            if (tabs > 1) {
                this.expandable = "false";
                this.expanded = "true";
            }
            if (this.expandable.equals("false")) {
                this.highlight = "false";
            }
            if (this.expandable.equals("true")) {
                html.append("<script type=\"text/javascript\">");
                html.append("function setCollapsedText_").append(this.id).append("(newtext){");
                html.append("document.getElementById('tab_").append(this.id).append("__collapsed').innerText = newtext;");
                html.append("}");
                html.append("function isTabExpanded_").append(this.id).append("(){");
                html.append("return document.getElementById('tab_").append(this.id).append("__tab').getAttribute('expanded') =='Y';");
                html.append("}");
                html.append("function expandTab_").append(this.id).append("(expand) {");
                html.append("var theTab = document.getElementById('tab_").append(this.id).append("__tab');");
                html.append("var theTabE = document.getElementById('tab_").append(this.id).append("__expanded');");
                html.append("var theTabC = document.getElementById('tab_").append(this.id).append("__collapsed');");
                html.append("var theTabI = document.getElementById('tab_").append(this.id).append("__img');");
                html.append("if ( theTab != null && theTabE != null && theTabC != null && theTabI != null){");
                html.append("sapphire.util.dom.setAttribute(theTab,'expanded',(expand ? 'Y' : 'N'));");
                html.append("if (expand){");
                html.append("try{").append("$('#tab_").append(this.id).append("__expanded').show();").append("}catch(e_t){theTabE.style.display='block';}");
                html.append("theTabC.style.display = 'none';");
                html.append("theTabI.src = '").append(renderStyleNew ? this.collapseImg : this.minusGif).append("';");
                html.append("}");
                html.append("else{");
                html.append("try{").append("$('#tab_").append(this.id).append("__expanded').hide();").append("}catch(e_t){theTabE.style.display='none';}");
                html.append("theTabC.style.display = 'block';");
                html.append("theTabI.src = '").append(renderStyleNew ? this.expandImg : this.plusGif).append("';");
                html.append("}");
                html.append("}");
                html.append("}");
                html.append("</script>");
            }
            if (tabs > 1) {
                html.append("<script type=\"text/javascript\">");
                html.append("var oaTab_").append(this.id).append("_listeners = new Array();");
                html.append("function changeTab_").append(this.id).append("(newTab){");
                if (this.context != null && this.context.length() > 0) {
                    html.append("if (typeof sapphire != 'undefined'){");
                    String enId = sapphire.util.HttpUtil.encodeURIComponent(this.id);
                    html.append("sapphire.userConfig.set(\"tab_").append(enId).append("__lastcontext\", '").append(this.context).append("');");
                    html.append("sapphire.userConfig.set(\"tab_").append(enId).append("__lastselected\", newTab);");
                    html.append("}");
                }
                html.append("var tabid = document.getElementById( 'tab_").append(this.id).append("__tab' );");
                html.append("var currentTab = (tabid != null ? sapphire.util.dom.getAttribute(tabid,'currentTab') : newTab);");
                html.append("if (currentTab != newTab){");
                html.append("sapphire.util.dom.setAttribute(tabid,'currentTab',newTab);");
                html.append("var newTabElement = document.getElementById('tab_").append(this.id).append("' + (newTab != 0 ? newTab : '') + '__tab' );");
                html.append("var newTextDivElement = document.getElementById('tab_").append(this.id).append("' + (newTab != 0 ? newTab : '') + '__textdiv' );");
                html.append("if (newTabElement != null){");
                html.append("newTabElement.className = 'tab_").append(this.appearance).append("_tab_selected';");
                html.append("sapphire.util.dom.setAttribute(newTabElement,'selected','Y');");
                html.append("}");
                html.append("\n if (newTextDivElement != null ){");
                html.append("\n newTextDivElement.className = 'tab_text _selected';");
                html.append("\n }");
                html.append("var currentTabElement = document.getElementById( 'tab_").append(this.id).append("' + (currentTab != 0 ? currentTab : '') + '__tab');");
                html.append("var currentTextDivElement = document.getElementById('tab_").append(this.id).append("' + (currentTab != 0 ? currentTab : '') + '__textdiv' );");
                html.append("if (currentTabElement != null){");
                html.append("currentTabElement.className = 'tab_").append(this.appearance).append("_tab_unselected';");
                html.append("sapphire.util.dom.setAttribute(currentTabElement,'selected','N');");
                html.append("}");
                html.append("\n if (currentTextDivElement != null ){");
                html.append("\n currentTextDivElement.className='tab_text';");
                html.append("\n }");
                html.append("if (typeof(dataView) != 'undefined'){");
                html.append("var sElementId = dataView.getElementIdFromTab('").append(this.id).append("');");
                html.append("if(sElementId.length > 0) dataView.changeTab(newTab,sElementId,false);");
                html.append("}else{");
                html.append("sapphire.events.callListeners(oaTab_").append(this.id).append("_listeners, new Array('").append(this.id).append("', newTab, false, currentTab));");
                html.append("}");
                html.append("}");
                html.append("}");
                html.append("</script>");
            }
            html.append("<table width=\"").append(this.bodyWidth).append("\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
            html.append("<tr><td ").append(renderStyleNew ? (this.expandable.equals("true") ? "class=\"tab_modernstandard_bodyheader_expandable\"" : "class=\"tab_modernstandard_bodyheader\"") : "").append(" ><table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" ").append("><tr ").append(renderStyleNew ? "class=\"tab_table_header\"" : "").append(">");
            for (int i = 0; i < tabs; ++i) {
                html.append("<td width=\"").append(this.width).append("\"><table ").append(renderStyleNew ? " width= \"*\" height= \"*\"" : "width=\"100%\"").append(" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr>");
                html.append("\t<td nowrap text=\"").append(HttpUtil.htmlEncode(tabText[i])).append("\" id=\"tab_").append(this.id).append(i > 0 ? String.valueOf(i) : "").append("__tab\" class=\"tab_").append(this.appearance).append("_tab").append(tabs > 1 ? (i == selectedTab ? "_selected" : "_unselected") : "").append("\" ");
                if (this.tip.length() > 0) {
                    html.append(" title=\"").append(this.tip).append("\" ");
                }
                if (this.highlight.equals("true")) {
                    html.append(" onMouseOver=\"this.className='tab_").append(this.appearance).append("_tab_highlight';document.getElementById( 'tab_").append(this.id).append("__spacer' ).className='tab_").append(this.appearance).append("_spacer_highlight'; document.getElementById( 'tab_").append(this.id).append("__body' ).className='tab_").append(this.appearance).append("_body_highlight';\" ");
                    html.append(" onMouseOut =\"this.className='tab_").append(this.appearance).append("_tab'; document.getElementById( 'tab_").append(this.id).append("__spacer' ).className='tab_").append(this.appearance).append("_spacer';document.getElementById( 'tab_").append(this.id).append("__body' ).className='tab_").append(this.appearance).append("_body';\" ");
                } else if (tabs > 1) {
                    html.append(" onMouseOver=\"if (sapphire.util.dom.getAttribute(this,'selected') == 'N')this.className='tab_").append(this.appearance).append("_tab_unselected_highlight';\" ");
                    html.append(" onMouseOut =\"if (sapphire.util.dom.getAttribute(this,'selected') == 'N')this.className='tab_").append(this.appearance).append("_tab_unselected';\" ");
                }
                if (this.expandable.equals("true")) {
                    html.append(" style=\"cursor: pointer;\"  expanded=\"").append(this.expanded.equals("true") ? "Y" : "N").append("\" onclick=\"expandTab_").append(this.id).append("(sapphire.util.dom.getAttribute(this,'expanded') == 'Y'?false:true )\" ");
                } else if (tabs > 1) {
                    html.append(" style=\"cursor: pointer;\"  selected=\"").append(i == selectedTab ? "Y" : "N").append("\" onclick=\"changeTab_").append(this.id).append("( '").append(i).append("' );").append(i < tabAction.length ? tabAction[i] : "").append(";\" ");
                    if (i == 0) {
                        html.append(" currentTab=\"").append(selectedTab).append("\" ");
                    }
                }
                html.append(">");
                if (!renderStyleNew && this.expandable.equals("true")) {
                    html.append("<img id=\"tab_").append(this.id).append("__img\" src=\"").append(this.expanded.equals("true") ? this.minusGif : this.plusGif).append("\">&nbsp;");
                }
                if (renderStyleNew) {
                    html.append("<table cellpadding=\"0\" cellspacing=\"0\" border=0><tr>");
                    if (this.expandable.equals("true")) {
                        html.append("<td><img id=\"tab_").append(this.id).append("__img\" src=\"").append(this.expanded.equals("true") ? this.collapseImg : this.expandImg).append("\" width=\"8px\" height=\"8px\"></td><td>&nbsp;&nbsp;</td>");
                    }
                    html.append("<td nowrap><div id=\"tab_").append(this.id).append(i > 0 ? String.valueOf(i) : "").append("__textdiv\" class=\"tab_text").append(tabs >= 1 ? (i == selectedTab ? " _selected" : "") : "").append("\" >");
                }
                if (this.pageContext != null) {
                    html.append(this.getTranslationProcessor().translate(SafeHTML.encodeForHTML(tabText[i])));
                } else {
                    html.append(SafeHTML.encodeForHTML(tabText[i]));
                }
                if (renderStyleNew) {
                    html.append("</div></td></tr></table>");
                }
                html.append("</td></tr></table></td>");
            }
            html.append("<td id=\"tab_").append(this.id).append("__spacer\" class=\"tab_").append(this.appearance).append("_spacer\" width=\"*\">&nbsp;</td>");
            html.append("</tr>");
            html.append("</table>");
            html.append("</td></tr>");
            html.append("<tr>");
            html.append("\t<td id=\"tab_").append(this.id).append("__body\" class=\"tab_").append(this.appearance).append("_body\">");
            if (this.expandable.equals("true")) {
                html.append("\t<div ");
                if (this.tip.length() > 0) {
                    html.append(" title=\"").append(this.tip).append("\" ");
                }
                html.append(" id=\"tab_").append(this.id).append("__collapsed\" style=\"display: ").append(this.expanded.equals("true") ? "none" : "block").append("\">").append(renderStyleNew ? "<div class=\"tab_modernstandard collapsedText\">" : "").append(this.collapsedText).append(renderStyleNew ? "</div>" : "").append("</div>");
            }
            html.append("\t<div id=\"tab_").append(this.id).append("__expanded\" style=\"");
            if (!this.bodyHeight.equals("")) {
                html.append("height: ").append(this.bodyHeight).append("; ");
            }
            if (!this.bodyWidth.equals("") && (!html5 || html5 && !this.bodyWidth.equalsIgnoreCase("*"))) {
                html.append("width: ").append(this.bodyWidth).append("; ");
            }
            if (html5) {
                html.append("overflow: visible;");
            } else {
                html.append("overflow: auto;");
            }
            html.append("display: ").append(this.expandable.equals("false") || this.expanded.equals("true") ? "block" : "none").append("\">\n");
            html.append(this.content);
            html.append("</div></td></tr></table>\n");
            html.append("<script type=\"text/javascript\">");
            if (nextSelectedTab == 0) {
                html.append("if ( typeof sapphire != 'undefined' ) {");
                html.append("    sapphire.userConfig.set( \"tab_").append(sapphire.util.HttpUtil.encodeURIComponent(this.id)).append("__lastselected\", '0' )\n;");
                html.append("}");
            } else {
                html.append("if ( typeof sapphire != 'undefined' ) {");
                html.append("  sapphire.events.registerLoadListener(new Function( 'oEl = document.getElementById( \"tab_").append(this.id).append(nextSelectedTab).append("__tab\" ); if (oEl != null)sapphire.events.fireEvent( oEl, \"onclick\" );'), false, 0);");
                html.append("}");
            }
            html.append("</script>");
        } else {
            html.append("TAG ERROR: id not defined for tab tag");
        }
        return html.toString();
    }
}

