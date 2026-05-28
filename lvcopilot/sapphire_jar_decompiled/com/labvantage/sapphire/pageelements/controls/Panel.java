/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.controls;

import sapphire.pageelements.BaseElement;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Panel
extends BaseElement {
    public static final String SRC_PROPERTY = "src";
    public static final String ACTION_PROPERTY = "action";
    public static final String TIP_PROPERTY = "tip";
    public static final String NAME_PROPERTY = "name";
    public static final String VALUE_PROPERTY = "value";
    private String id;
    private String text = "";
    private String bodyWidth = "300";
    private String bodyHeight = "300";
    private String left = "0";
    private String top = "0";
    private String zIndex = "0";
    private String resizable = "false";
    private String dragable = "false";
    private String content = "";
    private String register = "true";
    private String resizeEvent = "";
    private String dropEvent = "";
    private int snapDX = 0;
    private int snapDY = 0;
    private int snapRX = 0;
    private int snapRY = 0;
    private boolean hideTitle = false;
    private PropertyListCollection buttons = new PropertyListCollection();
    private PropertyListCollection attributes = new PropertyListCollection();

    public void setHideTitleBar(boolean hide) {
        this.hideTitle = hide;
    }

    public void setSnapDrag(int snapDX, int snapDY) {
        this.snapDX = snapDX;
        this.snapDY = snapDY;
    }

    public void setSnapResize(int snapRX, int snapRY) {
        this.snapRX = snapRX;
        this.snapRY = snapRY;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setResizeEvent(String event) {
        this.resizeEvent = event;
    }

    public void setDropEvent(String event) {
        this.dropEvent = event;
    }

    public void addButton(String src, String action) {
        this.addButton(src, action, "");
    }

    public void addButton(String src, String action, String tip) {
        PropertyList pl = new PropertyList();
        pl.setProperty(SRC_PROPERTY, src);
        pl.setProperty(ACTION_PROPERTY, action);
        pl.setProperty(TIP_PROPERTY, tip);
        this.buttons.add(pl);
    }

    public void addCustomAttribute(String attributeName, String value) {
        PropertyList pl = new PropertyList();
        pl.setProperty(NAME_PROPERTY, attributeName);
        pl.setProperty(VALUE_PROPERTY, value);
        this.attributes.add(pl);
    }

    public void setButtons(PropertyListCollection buttons) {
        this.buttons = buttons;
    }

    public void setCustomAttributes(PropertyListCollection attributes) {
        this.attributes = attributes;
    }

    public void setRegister(String register) {
        this.register = register;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setResizable(String resizable) {
        this.resizable = resizable;
    }

    public void setDragable(String dragable) {
        this.dragable = dragable;
    }

    public void setTop(String top) {
        this.top = top;
    }

    public void setZIndex(String zIndex) {
        this.zIndex = zIndex;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public void setBodywidth(String bodyWidth) {
        this.bodyWidth = bodyWidth;
    }

    public void setBodyheight(String bodyHeight) {
        this.bodyHeight = bodyHeight;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }

    public String getRegister() {
        return this.register;
    }

    public String getDragable() {
        return this.dragable;
    }

    public String getResizable() {
        return this.resizable;
    }

    public String getLeft() {
        return this.left;
    }

    public String getTop() {
        return this.top;
    }

    public String getZIndex() {
        return this.zIndex;
    }

    public String getBodyWidth() {
        return this.bodyWidth;
    }

    public String getBodyHeight() {
        return this.bodyHeight;
    }

    public String getContent() {
        return this.content;
    }

    public String getResizeEvent() {
        return this.resizeEvent;
    }

    public String getDropEvent() {
        return this.dropEvent;
    }

    public PropertyListCollection getButtons() {
        return this.buttons;
    }

    public PropertyListCollection getCustomAttributes() {
        return this.attributes;
    }

    @Override
    public String getHtml() {
        boolean isIE = this.browser != null && this.browser.isIE();
        StringBuffer html = new StringBuffer();
        if (this.id != null && this.id.length() > 0) {
            boolean isResizable = false;
            boolean isDragable = false;
            String snapdrag = "null";
            String snapresize = "null";
            if (this.resizable.equalsIgnoreCase("true") || this.resizable.equalsIgnoreCase("y")) {
                isResizable = true;
                if (this.snapRX > 0 || this.snapRY > 0) {
                    snapresize = "{x:" + this.snapRX + ",y:" + this.snapRY + "}";
                }
            }
            if (this.dragable.equalsIgnoreCase("true") || this.dragable.equalsIgnoreCase("y")) {
                isDragable = true;
                if (this.snapDX > 0 || this.snapDY > 0) {
                    snapdrag = "{x:" + this.snapDX + ",y:" + this.snapDY + "}";
                }
            }
            html.append("\n<div id=\"sapphirePanel_").append(this.id).append("\" panelId=\"").append(this.id).append("\" onclick=\"sapphire.ui.panel.focus('").append(this.id).append("');\" ");
            if (this.attributes != null && this.attributes.size() > 0) {
                for (int index = 0; index < this.attributes.size(); ++index) {
                    PropertyList attribute = this.attributes.getPropertyList(index);
                    html.append(" ").append(attribute.getProperty(NAME_PROPERTY, "attribute" + index)).append("=\"").append(attribute.getProperty(VALUE_PROPERTY, "")).append("\" ");
                }
            }
            int bh = 0;
            try {
                bh = Integer.parseInt(this.bodyHeight);
            }
            catch (Exception e) {
                bh = 0;
            }
            html.append("class=\"panel_sapphire\" style=\"position:").append(isIE ? "absolute" : "absolute").append(";width:").append(this.bodyWidth).append("px;height:").append(bh).append("px;top:").append(this.top).append("px;left:").append(this.left).append("px;z-index:").append(this.zIndex).append(";\">\n");
            if (this.hideTitle) {
                html.append("<div id=\"pnlmenu_").append(this.id).append("\" style=\"display:none;\" class=\"panel_menu\" onmouseover=\"sapphire.ui.panel.mouseover('").append(this.id).append("',this,event)\">");
                html.append("<div class=\"panel_menue\">");
                if (this.buttons == null) {
                    this.buttons = new PropertyListCollection();
                }
                PropertyList dragbtn = new PropertyList();
                dragbtn.setProperty(SRC_PROPERTY, "WEB-CORE/modules/dashboard/images/DragButton.gif");
                dragbtn.setProperty(TIP_PROPERTY, "Drag");
                dragbtn.setProperty("buttonid", "dragbtn_" + this.id);
                if (this.buttons.size() > 0) {
                    this.buttons.add(0, dragbtn);
                } else {
                    this.buttons.add(dragbtn);
                }
                this.renderButtons(html, 0);
                html.append("</div>");
                html.append("</div>");
            }
            html.append("<table id=\"pnl_tbl_").append(this.id).append("\" width=\"100%\" height=\"100%\" cellpadding=0 cellspacing=0 border=0 style=\"table-layout:fixed;\">\n");
            if (this.hideTitle) {
                html.append("<tr onmouseover=\"sapphire.ui.panel.mouseover('").append(this.id).append("',this,event)\">\n");
                html.append("<td class=\"panel_contents_headl\" style=\"height:5px;\">\n");
                if (isResizable) {
                    html.append("<table width=\"100%\" height=\"100%\" cellpadding=0 cellspacing=0 border=0>\n");
                    html.append("<tr height=5>\n<td width=5 style=\"cursor:nw-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[pnl_contents_").append(this.id).append("], 'nw')\"></td>\n");
                    html.append("<td style=\"cursor:n-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[pnl_contents_").append(this.id).append("], 'n')\"></td>\n</tr>\n");
                    html.append("<tr>\n<td width=5 style=\"cursor:w-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[], 'w')\"></td>\n<td></td>\n</tr>\n</table>\n");
                }
                html.append("</td>\n");
                if (isResizable) {
                    html.append("<td class=\"panel_contents_head panel_contents_title\" style=\"height:5px;\">\n");
                    html.append("<table width=\"100%\" height=\"100%\" cellpadding=0 cellspacing=0 border=0>\n");
                    html.append("<tr height=5>\n<td colspan=2 style=\"cursor:n-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[pnl_contents_").append(this.id).append("], 'n')\"></td>\n</tr>\n");
                    html.append("<tr>\n");
                    if (isDragable) {
                        html.append("<td id=\"pnl_head_").append(this.id).append("\" class=\"panel_contents_title\" nowrap valign=middle style=\"cursor:move;overflow-x:hidden;overflow-y:hidden;\">");
                    } else {
                        html.append("<td id=\"pnl_head_").append(this.id).append("\" class=\"panel_contents_title\" nowrap valign=middle style=\"overflow-x:hidden;overflow-y:hidden;\">");
                    }
                    html.append("</td>\n");
                    if (isDragable) {
                        html.append("<td width=1 style=\"cursor:default;\"></td>\n");
                    } else {
                        html.append("<td width=1></td>\n");
                    }
                    html.append("</tr>\n");
                    html.append("</table>\n");
                    html.append("</td>\n");
                } else {
                    html.append("<td class=\"panel_contents_head panel_contents_title\">\n");
                    html.append("<table width=\"100%\" height=\"100%\" cellpadding=0 cellspacing=0 border=0>\n");
                    html.append("<tr height=5>\n<td colspan=2 ></td>\n</tr>\n");
                    html.append("<tr>\n");
                    if (isDragable) {
                        html.append("<td id=\"pnl_head_").append(this.id).append("\" class=\"panel_contents_title\" nowrap valign=middle style=\"cursor:move;overflow-x:hidden;overflow-y:hidden;\">");
                    } else {
                        html.append("<td id=\"pnl_head_").append(this.id).append("\" class=\"panel_contents_title\" nowrap valign=middle style=\"overflow-x:hidden;overflow-y:hidden;\">");
                    }
                    html.append(this.text);
                    html.append("</td>\n");
                    html.append("<td width=1></td>\n");
                    html.append("</tr>\n");
                    html.append("<tr height=5>\n<td colspan=2></td>\n</tr>\n</table>\n");
                    html.append("</td>\n");
                }
                html.append("<td class=\"panel_contents_headr\" align=right style=\"height:5px;\">\n");
                if (isResizable) {
                    html.append("<table width=\"100%\" height=\"100%\" cellpadding=0 cellspacing=0 border=0>");
                    html.append("<tr height=5><td width=5 style=\"cursor:ne-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[pnl_contents_").append(this.id).append("], 'ne')\"></td></tr>");
                    html.append("<tr><td width=5 style=\"cursor:e-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[], 'e')\"></td></tr>");
                    html.append("</table>");
                }
                html.append("</td>");
                html.append("</tr>\n");
            } else {
                html.append("<tr>\n");
                html.append("<td class=\"panel_contents_headl\">\n");
                if (isResizable) {
                    html.append("<table width=\"100%\" height=\"100%\" cellpadding=0 cellspacing=0 border=0>\n");
                    html.append("<tr height=5>\n<td width=5 style=\"cursor:nw-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[pnl_contents_").append(this.id).append("], 'nw')\"></td>\n");
                    html.append("<td style=\"cursor:n-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[pnl_contents_").append(this.id).append("], 'n')\"></td>\n</tr>\n");
                    html.append("<tr>\n<td width=5 style=\"cursor:w-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[], 'w')\"></td>\n<td></td>\n</tr>\n</table>\n");
                }
                html.append("</td>\n");
                if (isResizable) {
                    html.append("<td class=\"panel_contents_head panel_contents_title\">\n");
                    html.append("<table width=\"100%\" height=\"100%\" cellpadding=0 cellspacing=0 border=0>\n");
                    html.append("<tr height=5>\n<td colspan=2 style=\"cursor:n-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[pnl_contents_").append(this.id).append("], 'n')\"></td>\n</tr>\n");
                    html.append("<tr>\n");
                    if (isDragable) {
                        html.append("<td id=\"pnl_head_").append(this.id).append("\" class=\"panel_contents_title\" nowrap valign=middle style=\"cursor:move;overflow-x:hidden;overflow-y:hidden;\">");
                    } else {
                        html.append("<td id=\"pnl_head_").append(this.id).append("\" class=\"panel_contents_title\" nowrap valign=middle style=\"overflow-x:hidden;overflow-y:hidden;\">");
                    }
                    html.append(this.text);
                    html.append("</td>\n");
                    if (this.buttons != null && this.buttons.size() > 0) {
                        html.append("<td valign=middle align=right width=").append(this.buttons.size() * 18).append(" >\n");
                        this.renderButtons(html, 0);
                        html.append("</td>\n");
                    } else if (isDragable) {
                        html.append("<td width=1 style=\"cursor:default;\"></td>\n");
                    } else {
                        html.append("<td width=1></td>\n");
                    }
                    html.append("</tr>\n");
                    html.append("<tr height=5 style=\"display:none;\">\n<td colspan=2></td>\n</tr>\n</table>\n");
                    html.append("</td>\n");
                } else {
                    html.append("<td class=\"panel_contents_head panel_contents_title\">\n");
                    html.append("<table width=\"100%\" height=\"100%\" cellpadding=0 cellspacing=0 border=0>\n");
                    html.append("<tr height=5>\n<td colspan=2 ></td>\n</tr>\n");
                    html.append("<tr>\n");
                    if (isDragable) {
                        html.append("<td id=\"pnl_head_").append(this.id).append("\" class=\"panel_contents_title\" nowrap valign=middle style=\"cursor:move;overflow-x:hidden;overflow-y:hidden;\">");
                    } else {
                        html.append("<td id=\"pnl_head_").append(this.id).append("\" class=\"panel_contents_title\" nowrap valign=middle style=\"overflow-x:hidden;overflow-y:hidden;\">");
                    }
                    html.append(this.text);
                    html.append("</td>\n");
                    if (this.buttons != null && this.buttons.size() > 0) {
                        html.append("<td valign=middle align=right width=").append(this.buttons.size() * 18).append(" >\n");
                        this.renderButtons(html, 0);
                        html.append("</td>\n");
                    } else if (isDragable) {
                        html.append("<td width=1 style=\"cursor:default;\"></td>\n");
                    } else {
                        html.append("<td width=1></td>\n");
                    }
                    html.append("</tr>\n");
                    html.append("<tr height=5>\n<td colspan=2></td>\n</tr>\n</table>\n");
                    html.append("</td>\n");
                }
                html.append("<td class=\"panel_contents_headr\" align=right>\n");
                if (isResizable) {
                    html.append("<table width=\"100%\" height=\"100%\" cellpadding=0 cellspacing=0 border=0>");
                    html.append("<tr height=5><td width=5 style=\"cursor:ne-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[pnl_contents_").append(this.id).append("], 'ne')\"></td></tr>");
                    html.append("<tr><td width=5 style=\"cursor:e-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[], 'e')\"></td></tr>");
                    html.append("</table>");
                }
                html.append("</td>");
                html.append("</tr>");
            }
            html.append("<tr>");
            html.append("<td colspan=3 id=\"pnl_body_").append(this.id).append("\" class=\"panel_contents_body\">");
            html.append("<table cellpadding=0 cellspacing=0 width=\"100%\" height=\"100%\" border=0>");
            if (isResizable) {
                html.append("<tr height=3><td width=3 style=\"cursor:w-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[], 'w')\"></td>");
                html.append("<td></td><td width=3 style=\"cursor:e-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[], 'e')\"></td></tr>");
                html.append("<tr><td width=3 style=\"cursor:w-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[], 'w')\"></td><td>");
            } else {
                html.append("<tr height=3><td width=3></td><td></td><td width=3></td></tr>");
                html.append("<tr><td width=3></td><td>");
            }
            html.append("<table cellpadding=0 cellspacing=0 width=\"100%\" height=\"100%\" style=\"table-layout:fixed;\" border=0>");
            html.append("<tr>");
            html.append("<td id=\"pnl_contentscell_").append(this.id).append("\" class=\"panel_contents_body_html\" valign=top align=left>");
            html.append("<div id=\"pnl_contents_").append(this.id).append("\"  class=\"panel_contents_body_div\" style=\"height:" + (bh - (this.hideTitle ? 13 : 34)) + "px;\">");
            html.append(this.content);
            html.append("</div>");
            html.append("</td>");
            html.append("</tr>");
            html.append("</table>");
            if (isResizable) {
                html.append("</td><td width=3 style=\"cursor:e-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[],'e')\"></td></tr>");
                html.append("<tr height=3><td width=3 style=\"cursor:sw-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[pnl_contents_").append(this.id).append("], 'sw')\"></td>");
                html.append("<td style=\"cursor:s-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[pnl_contents_").append(this.id).append("], 's')\"></td>");
                html.append("<td width=3 style=\"cursor:se-resize;\" onmousedown=\"sapphire.ui.resize.start(sapphirePanel_").append(this.id).append(",[pnl_contents_").append(this.id).append("], 'se')\"></td></tr>");
            } else {
                html.append("</td><td width=3></td></tr>");
                html.append("<tr height=3><td width=3></td><td></td><td width=3></td></tr>");
            }
            html.append("</table>");
            html.append("</td>");
            html.append("</tr>");
            html.append("</table>");
            html.append("</div>");
            if (this.register.equalsIgnoreCase("true") || this.register.equalsIgnoreCase("y")) {
                html.append("<script type=\"text/javascript\" defer=false>\n");
                html.append(this.getRegisterScript());
                html.append("sapphire.events.detachEvent( window, 'onload', sapphire.ui.panel.load );\n");
                html.append("sapphire.events.attachEvent( window, 'onload', sapphire.ui.panel.load );\n");
                html.append("</script>\n");
            }
        } else {
            html.append("TAG ERROR: id not defined for panel tag");
        }
        return html.toString();
    }

    public String getRegisterScript() {
        StringBuffer html = new StringBuffer();
        String snapdrag = "null";
        String snapresize = "null";
        if ((this.resizable.equalsIgnoreCase("true") || this.resizable.equalsIgnoreCase("y")) && (this.snapRX > 0 || this.snapRY > 0)) {
            snapresize = "{x:" + this.snapRX + ",y:" + this.snapRY + "}";
        }
        if (this.dragable.equalsIgnoreCase("true") || this.dragable.equalsIgnoreCase("y")) {
            if (this.snapDX > 0 || this.snapDY > 0) {
                snapdrag = "{x:" + this.snapDX + ",y:" + this.snapDY + "}";
            }
            html.append("sapphire.ui.panel.register('").append(this.id).append("',true,'").append(this.resizeEvent).append("','").append(this.dropEvent).append("',").append(snapdrag).append(",").append(snapresize).append(",").append(this.hideTitle).append(");");
        } else {
            html.append("sapphire.ui.panel.register('").append(this.id).append("',false,'").append(this.resizeEvent).append("','',").append(snapdrag).append(",").append(snapresize).append(",").append(this.hideTitle).append(");");
        }
        return html.toString();
    }

    private void renderButtons(StringBuffer html, int padding) {
        if (this.buttons != null && this.buttons.size() > 0) {
            for (int index = 0; index < this.buttons.size(); ++index) {
                PropertyList thelist = this.buttons.getPropertyList(index);
                String src = thelist.getProperty(SRC_PROPERTY, "");
                if (src.length() <= 0) continue;
                html.append("<img panelId=\"").append(this.id).append("\" width=12 height=12 src=\"").append(src).append("\" ");
                String action = thelist.getProperty(ACTION_PROPERTY, "");
                if (action.length() > 0) {
                    html.append(" onclick=\"").append(action).append("\" ");
                }
                html.append(" class=\"panel_btn\" ");
                html.append(" id=\"").append(thelist.getProperty("buttonid", "btn" + index + "_" + this.id)).append("\"");
                html.append(" onmouseover=\"this.className='panel_btn_over';\"");
                html.append(" onmouseout=\"this.className='panel_btn';\"");
                if (padding > 0) {
                    html.append(" style=\"cursor: pointer;position:relative;top:").append(padding).append("px;\" border=0 ");
                } else {
                    html.append(" style=\"cursor: pointer;\"");
                }
                String tip = thelist.getProperty(TIP_PROPERTY, "");
                if (tip.length() > 0) {
                    html.append(" title=\"").append(tip).append("\" ");
                }
                html.append(">");
                html.append("<img src=\"WEB-CORE/images/blank.gif\" width=\"2\" height=\"1\">");
            }
        }
    }
}

