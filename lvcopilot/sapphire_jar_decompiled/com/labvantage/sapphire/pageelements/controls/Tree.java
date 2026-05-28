/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.controls;

import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Tree
extends BaseElement {
    public static final String PROPERTY_ROOTITEMS = "rootitems";
    public static final String PROPERTY_ITEMS = "items";
    public static final String PROPERTY_TEXT = "text";
    public static final String PROPERTY_TRANSLATE = "translate";
    public static final String PROPERTY_EXPANDED = "expanded";
    public static final String PROPERTY_PREHTML = "prehtml";
    public static final String PROPERTY_POSTHTML = "posthtml";
    public static final String PROPERTY_EXAPANDEDIMAGE = "expandedimage";
    public static final String PROPERTY_COLLAPSEDIMAGE = "collapsedimage";
    public static final String PROPERTY_IMAGE = "image";
    public static final String PROPERTY_SHOWEXPANDCOLLAPSE = "showexpandcollapse";
    public static final String PROPERTY_BUTTONCLICK = "buttonclick";
    public static final String PROPERTY_TEXTCLICK = "textclick";
    public static final String PROPERTY_IMAGECLICK = "imageclick";
    private static final String IMAGE_EXPANDED = "WEB-CORE/elements/images/minus.gif";
    private static final String IMAGE_COLLAPSED = "WEB-CORE/elements/images/plus.gif";
    private static final int CELLPADDING = 2;
    private static final int INDENT = 10;
    private TranslationProcessor tp = null;
    private PropertyList userConfig = null;
    private String id = "";
    private int cellpadding = 2;
    PropertyList attributes = new PropertyList();

    public Tree(PageContext pageContext) {
        this.setPageContext(pageContext);
        this.tp = this.getTranslationProcessor();
        if (pageContext != null && this.requestContext != null) {
            this.userConfig = this.requestContext.getPropertyList("userconfig");
        }
    }

    public Tree(String connectionId, TranslationProcessor tp) {
        this.setConnectionId(connectionId);
        if (tp == null) {
            this.tp = new TranslationProcessor(connectionId);
        }
    }

    public void setPadding(int padding) {
        this.cellpadding = padding;
    }

    public void setUserConfig(PropertyList userConfig) {
        this.userConfig = userConfig;
    }

    public void setAttribute(String attribute, String value) {
        this.attributes.setProperty(attribute, value);
    }

    public String getAttribute(String attribute) {
        return this.attributes.getProperty(attribute, "");
    }

    public void setId(String id) {
        this.id = id;
        if (this.elementid == null) {
            this.elementid = id;
        }
    }

    private StringBuffer getItems(PropertyListCollection items, String parentid, int level) {
        StringBuffer html = new StringBuffer();
        for (int i = 0; i < items.size(); ++i) {
            String image;
            String textonclick;
            String imageonclick;
            PropertyList item = items.getPropertyList(i);
            String text = item.getProperty(PROPERTY_TEXT, "");
            if (text.length() <= 0) continue;
            boolean cantranslate = item.getProperty(PROPERTY_TRANSLATE, "").equalsIgnoreCase("Y");
            boolean showimage = item.getProperty(PROPERTY_SHOWEXPANDCOLLAPSE, "Y").equalsIgnoreCase("Y");
            String prehtml = item.getProperty(PROPERTY_PREHTML, "");
            String posthtml = item.getProperty(PROPERTY_POSTHTML, "");
            if (cantranslate && this.tp != null) {
                text = this.getTranslationProcessor().translate(text);
            }
            String thisid = parentid + "_" + level + "_" + i;
            String state = "";
            if (this.userConfig != null) {
                state = this.userConfig.getProperty("tree_" + this.elementid + "_" + thisid, "");
                this.logger.debug("config state - " + state);
            }
            boolean isexpanded = state.length() == 0 ? item.getProperty(PROPERTY_EXPANDED, "").equalsIgnoreCase("Y") : !state.equalsIgnoreCase("collapsed");
            html.append("<table cellpadding=\"").append(this.cellpadding).append("\" cellspacing=\"0\" border=\"0\"><tbody>");
            PropertyListCollection subitems = item.getCollection(PROPERTY_ITEMS);
            String display = "block";
            String buttoncursor = "default";
            String imagecursor = "default";
            String textcursor = "default";
            String buttononclick = item.getProperty(PROPERTY_BUTTONCLICK, "");
            if (buttononclick.length() > 0) {
                buttononclick = "onclick=\"" + buttononclick + "\"";
            }
            if ((imageonclick = item.getProperty(PROPERTY_IMAGECLICK, "")).length() > 0) {
                imageonclick = "onclick=\"" + imageonclick + "\"";
                imagecursor = "pointer";
            }
            if ((textonclick = item.getProperty(PROPERTY_TEXTCLICK, "")).length() > 0) {
                textonclick = "onclick=\"" + textonclick + "\"";
                textcursor = "pointer";
            }
            int sidepadding = this.cellpadding > 0 ? this.cellpadding : 2;
            int indent = 10 + this.cellpadding + this.cellpadding;
            if (subitems != null) {
                if (buttononclick.length() == 0) {
                    buttononclick = "onclick=\"tree.collapseExpand('" + thisid + "','" + this.elementid + "')\"";
                }
                if (showimage) {
                    buttoncursor = "pointer";
                }
                imagecursor = "pointer";
                if (imageonclick.length() == 0) {
                    imageonclick = "onclick=\"tree.collapseExpand('" + thisid + "','" + this.elementid + "')\"";
                }
                textcursor = "pointer";
                if (textonclick.length() == 0) {
                    textonclick = "onclick=\"tree.collapseExpand('" + thisid + "','" + this.elementid + "')\"";
                }
                if (isexpanded) {
                    html.append("<tr id=\"").append(thisid).append("_row").append("\" state=\"expanded\">");
                    display = "block";
                    html.append("<td valign=\"middle\" ").append(buttononclick).append(" style=\"padding-left:").append(sidepadding).append("px;padding-right:").append(sidepadding).append("px;cursor:").append(buttoncursor).append(";\">");
                    if (showimage) {
                        html.append("<img width=\"10\" height=\"10\"  id=\"").append(thisid).append("_img_e1").append("\" style=\"display:block;\" src=\"").append(IMAGE_EXPANDED).append("\">");
                        html.append("<img width=\"10\" height=\"10\"  id=\"").append(thisid).append("_img_c1").append("\" style=\"display:none;\" src=\"").append(IMAGE_COLLAPSED).append("\">");
                    } else {
                        html.append("<img width=\"10\" height=\"1\" src=\"WEB-CORE/images/blank.gif\">");
                    }
                    html.append("</td>");
                    image = item.getProperty(PROPERTY_EXAPANDEDIMAGE, item.getProperty(PROPERTY_IMAGE, ""));
                    if (image.length() > 0) {
                        html.append("<td valign=\"middle\" ").append(imageonclick).append(" style=\"padding-left:").append(sidepadding).append("px;padding-right:").append(sidepadding).append("px;cursor:").append(imagecursor).append(";\">");
                        html.append("<img id=\"").append(thisid).append("_img_e2").append("\" style=\"display:block;\" width=\"16\" height=\"16\" src=\"").append(image).append("\">");
                        image = item.getProperty(PROPERTY_COLLAPSEDIMAGE, item.getProperty(PROPERTY_IMAGE, image));
                        html.append("<img id=\"").append(thisid).append("_img_c2").append("\" style=\"display:none;\" width=\"16\" height=\"16\" src=\"").append(image).append("\">");
                        html.append("</td>");
                    }
                } else {
                    html.append("<tr id=\"").append(thisid).append("_row").append("\" state=\"collapsed\">");
                    display = "none";
                    html.append("<td valign=\"middle\" ").append(buttononclick).append(" style=\"padding-left:").append(sidepadding).append("px;padding-right:").append(sidepadding).append("px;cursor:").append(buttoncursor).append(";\">");
                    if (showimage) {
                        html.append("<img width=\"10\" height=\"10\" id=\"").append(thisid).append("_img_e1").append("\" style=\"display:none;\" src=\"").append(IMAGE_EXPANDED).append("\">");
                        html.append("<img width=\"10\" height=\"10\"  id=\"").append(thisid).append("_img_c1").append("\" style=\"display:block;\" src=\"").append(IMAGE_COLLAPSED).append("\">");
                    } else {
                        html.append("<img width=\"10\" height=\"1\" src=\"WEB-CORE/images/blank.gif\">");
                    }
                    html.append("</td>");
                    image = item.getProperty(PROPERTY_COLLAPSEDIMAGE, item.getProperty(PROPERTY_IMAGE, ""));
                    if (image.length() > 0) {
                        html.append("<td valign=\"middle\" ").append(imageonclick).append(" style=\"padding-left:").append(sidepadding).append("px;padding-right:").append(sidepadding).append("px;cursor:").append(imagecursor).append(";\">");
                        html.append("<img id=\"").append(thisid).append("_img_c2").append("\" style=\"display:block;\" width=\"16\" height=\"16\" src=\"").append(image).append("\">");
                        image = item.getProperty(PROPERTY_EXAPANDEDIMAGE, item.getProperty(PROPERTY_IMAGE, image));
                        html.append("<img id=\"").append(thisid).append("_img_e2").append("\" style=\"display:none;\" width=\"16\" height=\"16\" src=\"").append(image).append("\">");
                        html.append("</td>");
                    }
                }
            } else {
                html.append("<tr id=\"").append(thisid).append("_row").append("\" state=\"normal\">");
                image = item.getProperty(PROPERTY_IMAGE, "");
                html.append("<td valign=\"middle\" style=\"padding-left:").append(sidepadding).append("px;padding-right:").append(sidepadding).append("px;\">");
                html.append("<img width=\"10\" height=\"1\" src=\"WEB-CORE/images/blank.gif\">");
                html.append("</td>");
                if (image.length() > 0) {
                    html.append("<td valign=\"middle\" ").append(imageonclick).append(" style=\"padding-left:").append(sidepadding).append("px;padding-right:").append(sidepadding).append("px;cursor:").append(imagecursor).append(";\">");
                    html.append("<img id=\"").append(thisid).append("_img").append("\" width=\"16\" height=\"16\" src=\"").append(image).append("\">");
                    html.append("</td>");
                }
            }
            html.append("<td valign=\"middle\" ").append(textonclick).append(" style=\"cursor:").append(textcursor).append(";\">");
            html.append(prehtml).append(text).append(posthtml);
            html.append("</td>");
            html.append("</tr></tbody></table>");
            html.append("<div id=\"").append(thisid).append("\" style=\"display:").append(display).append(";border:solid 0 red;padding-left:").append(indent).append("px;\">");
            if (subitems != null) {
                html.append(this.getItems(subitems, thisid, level + 1));
            }
            html.append("</div>");
        }
        return html;
    }

    public static String getScriptInclude() {
        return "<script type=\"text/javascript\" src=\"WEB-CORE/elements/scripts/tree.js\"></script>";
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.element != null && this.element.size() > 0) {
            PropertyListCollection rootitems = this.element.getCollection(PROPERTY_ROOTITEMS);
            if (rootitems != null && rootitems.size() > 0) {
                if (this.element.getProperty("includejs", "Y").equalsIgnoreCase("Y")) {
                    html.append(Tree.getScriptInclude());
                }
                if (this.id == null || this.id.length() == 0) {
                    this.id = this.elementid;
                }
                html.append("<div id=\"").append(this.id).append("\"");
                for (Object k : this.attributes.keySet()) {
                    html.append(" ").append(k.toString()).append("=\"").append(this.getAttribute(k.toString())).append("\"");
                }
                html.append(">");
                html.append(this.getItems(rootitems, this.id, 0));
                html.append("</div>");
            } else {
                String msg = "Properties contains no items.";
                if (this.tp != null) {
                    msg = this.tp.translate(msg);
                }
                html.append("<font>").append(msg).append("</font>");
            }
        } else {
            String msg = "No properties provided.";
            if (this.tp != null) {
                msg = this.tp.translate(msg);
            }
            html.append("<font>").append(msg).append("</font>");
        }
        return html.toString();
    }
}

