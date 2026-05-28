/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.scrollpanel;

import javax.servlet.jsp.PageContext;
import sapphire.pageelements.BaseElement;

public class ScrollPanel
extends BaseElement {
    private StringBuffer content;
    private boolean readonly = false;
    private String id;
    int maxHeight = 200;
    String backgroundImageDown;
    String backgroundImageUp;
    boolean hideScrollbars = false;
    boolean modernScroll = false;

    public ScrollPanel(PageContext pageContext) {
        this.setPageContext(pageContext);
    }

    public void setModernScroll(boolean modernScroll) {
        this.modernScroll = modernScroll;
    }

    public void setContent(StringBuffer html) {
        this.content = html;
    }

    public void setContent(String html) {
        this.content = new StringBuffer(html);
    }

    public String getContent() {
        return this.content == null ? "" : this.content.toString();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMaxHeight() {
        return this.maxHeight;
    }

    public void setHideScrollbars(boolean hide) {
        this.hideScrollbars = hide;
    }

    public boolean getHideScrollbars() {
        return this.hideScrollbars;
    }

    public void setBackgroundImageDown(String backgroundImageDown) {
        this.backgroundImageDown = backgroundImageDown;
    }

    public String getBackgroundImageDown() {
        return this.backgroundImageDown;
    }

    public void setBackgroundImageUp(String backgroundImageUp) {
        this.backgroundImageUp = backgroundImageUp;
    }

    public String getBackgroundImageUp() {
        return this.backgroundImageUp;
    }

    @Override
    public String getHtml() {
        StringBuffer out = new StringBuffer();
        if (this.content != null && this.content.length() > 0) {
            if (this.id != null && this.id.length() > 0) {
                String backgroundUp = this.backgroundImageUp != null && this.backgroundImageUp.length() > 0 && (!this.browser.isIE() || this.browser.getVersion() > 7.0) ? "background-image:url('" + this.backgroundImageUp + "');background-repeat:repeat-x;" : "";
                if (!this.modernScroll) {
                    if (this.hideScrollbars) {
                        out.append("<div id=\"__scrolldiv_").append(this.id).append("_up\" style=\"display:none;height:30px;position:relative;top:30px;margin-top:-30px;").append(backgroundUp).append("\" align=\"right\">");
                        out.append("<img id=\"__scrollbtn_").append(this.id).append("_up\" src=\"WEB-CORE/elements/scrollpanel/images/arrow_up.gif\" onclick=\"sapphire.ui.util.scrollY(__scrolldiv_").append(this.id).append(",40);\" style=\"cursor:pointer;height:16px;").append(this.browser.isWebkit() ? "margin-right:2px;" : "margin-right:6px").append("\">");
                        out.append("</div>");
                    }
                    out.append("<div id=\"__scrolldiv_").append(this.id).append("\" style=\"overflow-y:").append(this.hideScrollbars ? "hidden" : "auto").append(";overflow-x:hidden;height:").append(15).append("px;width:100%;\">");
                    out.append(this.content);
                    out.append("</div>");
                    String backgroundDown = this.backgroundImageDown != null && this.backgroundImageDown.length() > 0 && (!this.browser.isIE() || this.browser.getVersion() > 7.0) ? "background-image:url('" + this.backgroundImageDown + "');background-repeat:repeat-x;" : "";
                    if (this.hideScrollbars) {
                        out.append("<div id=\"__scrolldiv_").append(this.id).append("_down\" style=\"display:none;height:30px;;position:relative;top:0;margin-top:-30px;").append(backgroundDown).append("\" align=\"right\">");
                        out.append("<img id=\"__scrollbtn_").append(this.id).append("_down\" src=\"WEB-CORE/elements/scrollpanel/images/arrow_down.gif\" onclick=\"sapphire.ui.util.scrollY(__scrolldiv_").append(this.id).append(",-40);\" style=\"cursor:pointer;height:16px;margin-top:14px;").append(this.browser.isWebkit() ? "margin-right:2px;" : "margin-right:6px").append("\">");
                        out.append("</div>");
                    }
                } else {
                    if (this.maxHeight > 0) {
                        out.append("<div id=\"__scrolldiv_").append(this.id).append("\" class=\"__scrolldiv_modern\" style=\"max-height:").append(this.maxHeight).append("px;\">");
                    }
                    out.append(this.content);
                    if (this.maxHeight > 0) {
                        out.append("</div>");
                    }
                }
                out.append("<script>\n");
                out.append("sapphire.ui.util.scrollTo(__scrolldiv_").append(this.id).append(",null,").append(this.maxHeight).append(");");
                out.append("</script>");
            } else {
                this.logger.error("No id provided.");
                out.append("No id provided.");
            }
        } else {
            this.logger.error("No content provided.");
            out.append(this.getTranslationProcessor().translate("No content provided."));
        }
        return out.toString();
    }
}

