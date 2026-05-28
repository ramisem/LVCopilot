/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.controls;

import com.labvantage.opal.elements.advancedtoolbar.AdvancedToolbar;
import com.labvantage.sapphire.modules.dashboard.gizmos.MenuGizmo;
import com.labvantage.sapphire.pageelements.controls.Image;
import javax.servlet.jsp.PageContext;
import sapphire.pageelements.BaseElement;
import sapphire.xml.PropertyList;

public class Button
extends BaseElement {
    private String id = "";
    private String text = "";
    private String action = "";
    private String img = "";
    private String imgposition = "left";
    private String tip = "";
    private String width = "";
    private String style = "";
    private String highlight = "true";
    private String appearance = "standard";
    private String margin = "";
    private boolean disabled = false;
    private boolean isModern = true;
    private String styleclass = "";
    private static int idIndex = 0;

    public Button(PageContext pageContext) {
        this.setPageContext(pageContext);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setModern(boolean isModern) {
        this.isModern = isModern;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public void setMargin(String margin) {
        this.margin = margin;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setImgposition(String imgposition) {
        this.imgposition = imgposition;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public boolean getDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight ? "true" : "false";
    }

    @Override
    public String getHtml() {
        boolean safari;
        boolean hasText;
        StringBuffer html = new StringBuffer();
        if (this.isModern) {
            this.appearance = "modern";
        }
        boolean isHTML5 = false;
        if (this.requestContext != null) {
            isHTML5 = this.requestContext.getProperty("html5").equalsIgnoreCase("Y");
        }
        html.append(this.getJavaScriptAPI());
        boolean hasImage = this.img.length() > 0;
        boolean bl = hasText = this.text.length() > 0;
        if (hasText && AdvancedToolbar.isTextOnly()) {
            hasImage = false;
        }
        boolean imageLeft = !this.imgposition.equalsIgnoreCase("right") || this.appearance.equalsIgnoreCase("ribbonsmall");
        boolean isTable = !this.appearance.equalsIgnoreCase("standard") && !this.appearance.equalsIgnoreCase("smallicon") && !this.appearance.equalsIgnoreCase("smalltext");
        boolean isButton = !isTable;
        boolean imageRight = !imageLeft;
        boolean ie = this.browser == null || this.browser.isIE();
        boolean chrome = this.browser != null && this.browser.isChrome();
        boolean ff = this.browser != null && (this.browser.isFireFox() || this.browser.isMozilla());
        boolean bl2 = safari = this.browser != null && this.browser.isSafari();
        if (this.margin.equals("")) {
            if (this.isModern) {
                this.margin = "thin";
            } else {
                String string = this.margin = hasImage ? "thin" : "thick";
            }
        }
        if (isTable) {
            String buttonid = this.id.length() > 0 ? this.id : "standalone_button" + idIndex++;
            html.append("<a style=\"color:black\" onkeyup=\"if(event.keyCode == 13 ) $(this).children().attr('id','" + buttonid + "').trigger( 'onclick' )\" href=\"javascript:void(0)\"><table id=\"" + buttonid + "\" cellspacing=\"0\" cellpadding=\"0\"");
        } else {
            html.append("<button type=\"button\" ");
        }
        if (this.disabled) {
            html.append(" disabled");
        }
        html.append("\trefresh=\"N\" ");
        if (this.element != null && this.element.getProperty("isdropdown", "N").equalsIgnoreCase("Y")) {
            this.styleclass = this.element.getProperty("isdropdownarrow", "N").equalsIgnoreCase("Y") ? "button_ddarrow" : "button_ddmain";
        }
        html.append(" class=\"button_").append(this.appearance).append(this.styleclass.length() > 0 ? " " + this.styleclass : "").append("\"");
        html.append(" appearance=\"").append(this.appearance).append("\"");
        html.append(" onclick=\"if(sapphire.ui.button.preClick(this)){").append(this.action).append("}\"");
        if (isTable) {
            html.append(" mouseover=\"N\"");
        }
        if (hasImage) {
            html.append(" img=\"").append(this.img).append("\"");
            html.append(" hasimg=\"Y\"");
        } else {
            html.append(" hasimg=\"N\"");
        }
        if (this.tip.length() > 0) {
            html.append(" title=\"").append(this.tip).append("\"");
        }
        if (hasText) {
            html.append(" text=\"").append(this.text).append("\"");
            html.append(" hastext=\"Y\"");
        } else {
            html.append(" hastext=\"N\"");
        }
        if (this.id.length() > 0) {
            html.append(" id=\"").append(this.id).append("\"");
        }
        if (isTable && (this.highlight.equalsIgnoreCase("true") || this.highlight.equals("Y"))) {
            html.append(" onmouseover=\"sapphire.ui.button.mouseover(this);\"");
            html.append(" onmouseout=\"sapphire.ui.button.mouseout(this);\"");
        }
        html.append(" style=\"");
        if (ie && !isHTML5) {
            if (this.width.length() > 0) {
                html.append("width:").append(this.width).append("px;");
            }
            if (isButton) {
                html.append("display:inline;overflow:visible;");
            }
            if (isHTML5) {
                html.append("padding-left:0;padding-right:0;");
            }
        } else if (isTable) {
            if (this.width.length() > 0) {
                html.append("width:").append(this.width).append("px;");
            }
        } else {
            html.append("text-align:center;overflow-x:visible; margin-left:0; margin-right:0;");
            if (this.margin.equals("none")) {
                html.append("padding-left:0;padding-right:0;");
            }
        }
        if (this.action.length() > 0) {
            html.append("cursor:pointer;");
        }
        if (this.style.length() > 0) {
            html.append(this.style);
        }
        html.append("\"");
        html.append(">");
        if (isTable) {
            html.append("<tr align=\"center\">");
            html.append("<td class=\"buttonmargin_").append(this.margin).append("\">");
        }
        html.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"");
        if (isButton) {
            if (this.margin.equals("none")) {
                if (!hasImage) {
                    if (ie && !isHTML5) {
                        html.append("margin: -1px; margin-left: 1px; margin-right: 1px");
                    }
                    if (isHTML5 && this.width.length() > 0) {
                        html.append("width:").append(this.width).append("px");
                    }
                } else {
                    if (ie && !isHTML5) {
                        html.append("margin: -1px");
                    }
                    if (isHTML5 && this.width.length() > 0) {
                        html.append("margin: -1px; width:").append(this.width).append("px");
                    }
                }
            } else {
                if (ie && !isHTML5) {
                    html.append("margin-left: 5px; margin-right: 5px; padding-bottom:0");
                } else if (safari) {
                    html.append("padding-left: 1px; margin-left: -3px; margin-right: -4px; padding-top: 1px");
                } else {
                    html.append("margin-left: 0px; margin-right: 0px; padding-bottom:0");
                }
                if (isHTML5 && this.width.length() > 0) {
                    html.append("width:").append(this.width).append("px");
                }
            }
        } else if (this.appearance.equalsIgnoreCase("ribbonlarge")) {
            html.append("width:34px;height:").append(hasText ? 60 : 36).append("px;");
        }
        html.append("\">");
        html.append("<tr valign=\"middle\">");
        if (imageLeft && hasImage) {
            html.append("<td valign=\"middle\" align=\"center\">");
            if (isHTML5 && hasText) {
                html.append("<div style=\"overflow:hidden;width:auto;");
                if (this.appearance.equalsIgnoreCase("ribbonsmall")) {
                    html.append("height:16px;");
                } else if (this.appearance.equalsIgnoreCase("ribbonlarge")) {
                    html.append("height:32px;");
                }
                html.append("\">");
            }
            Image image = new Image(this.pageContext);
            image.setImageSrc(this.img);
            if (this.appearance.equalsIgnoreCase("ribbonsmall")) {
                image.setDimensions(16, 16);
            } else if (this.appearance.equalsIgnoreCase("ribbonlarge")) {
                image.setDimensions(32, 32);
            } else if (this.styleclass.equalsIgnoreCase("button_ddarrow")) {
                image.setDimensions(12, 12);
            }
            image.setTitle(this.tip != null ? this.tip : "");
            if (this.element != null && this.element.getProperty("isdropdownarrow", "N").equalsIgnoreCase("Y")) {
                MenuGizmo menuGizmo = new MenuGizmo();
                menuGizmo.setPageContext(this.pageContext);
                menuGizmo.setElementid(this.id + "menu");
                PropertyList menuProps = new PropertyList();
                menuProps.setProperty("renderincludes", "Y");
                menuProps.setProperty("customhtml", image.getHtml());
                menuProps.setProperty("custommenu", this.element.getCollection("dropdownbuttons"));
                menuProps.setProperty("customclass", "menugizmo_buttondd");
                menuProps.setProperty("menutype", "custom");
                menuProps.setProperty("click", "Y");
                menuProps.setProperty("mouseover", "Y");
                menuProps.setProperty("customx", "60");
                menuProps.setProperty("customy", "15");
                menuGizmo.setElementProperties(menuProps);
                menuGizmo.init();
                html.append(menuGizmo.getHtml());
                html.append("<script>" + menuGizmo.getScript() + "</script>");
            } else {
                html.append(image.getHtml());
            }
            if (isHTML5) {
                html.append("</div>");
            }
            html.append("</td>");
            if (hasText) {
                if (!this.appearance.equalsIgnoreCase("ribbonlarge")) {
                    if (ie && !isHTML5) {
                        html.append("<td width=\"4\">&nbsp;</td>");
                    } else {
                        html.append("<td width=\"3\">&nbsp;</td>");
                    }
                } else {
                    html.append("</tr><tr>");
                }
            }
        }
        if (hasText) {
            html.append("<td ");
            if ("modern".equals(this.appearance)) {
                html.append("class=\"button_modern_text\" ");
            }
            html.append("style=\"");
            if (isButton && !this.appearance.equals("smalltext") && ie) {
                html.append("padding-top: 1px;");
            }
            if (ff) {
                html.append("padding-bottom: 1px;");
            }
            if (chrome) {
                html.append("padding-right: -2px;");
            }
            if (this.appearance.equalsIgnoreCase("ribbonsmall") || this.appearance.equalsIgnoreCase("ribbonlarge")) {
                html.append("font-family:inherit;font-size:8pt;color:#15428b;");
            }
            html.append("\" ");
            if (!this.appearance.equalsIgnoreCase("ribbonlarge")) {
                html.append("nowrap ");
            }
            html.append("valign=\"middle\" align=\"center\">");
            if (this.appearance.equalsIgnoreCase("ribbonlarge")) {
                html.append("<div style=\"height:27px;overflow-y:hidden;overflow-x:hidden;\">");
            }
            html.append(this.text);
            if (this.appearance.equalsIgnoreCase("ribbonlarge")) {
                html.append("</div>");
            }
            html.append("</td>");
        }
        if (imageRight && hasImage) {
            if (hasText) {
                html.append("<td width=\"4\">&nbsp;</td>");
            }
            html.append("<td valign=\"middle\" align=\"center\">");
            if (isHTML5 && hasText) {
                html.append("<div style=\"overflow:hidden;width:auto;");
                html.append("height:16px;");
                html.append("\">");
            }
            html.append("<img src=\"").append(this.img).append("\" border=\"0\">");
            if (isHTML5) {
                html.append("</div>");
            }
            html.append("</td>");
        }
        html.append("</tr></table>");
        if (isTable) {
            html.append("</td></tr>");
        }
        if (isTable) {
            html.append("</table></a>");
        } else {
            html.append("</button>");
        }
        if (this.element != null && this.element.getProperty("isdropdown", "N").equalsIgnoreCase("Y") && this.element.getProperty("isdropdownarrow", "N").equalsIgnoreCase("N")) {
            Button dropdown = new Button(this.pageContext);
            dropdown.setAppearance(this.appearance);
            dropdown.styleclass = "button_ddarrow";
            dropdown.setElementProperties(this.getElementProperties().copy());
            dropdown.getElementProperties().setProperty("isdropdownarrow", "Y");
            dropdown.setImg("WEB-CORE/imageref/flat/16/flat_blue_triangle_down.svg");
            dropdown.setId(this.id + "_dd");
            html.append(dropdown.getHtml());
        }
        return html.toString();
    }
}

