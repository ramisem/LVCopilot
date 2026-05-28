/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.pageelements.list.ListColumn;
import sapphire.util.Browser;

public class SmartScrollGridRenderer {
    private StringBuffer topleft = new StringBuffer();
    private StringBuffer topright = new StringBuffer("");
    private StringBuffer bottomleft = new StringBuffer();
    private StringBuffer bottomright = new StringBuffer();
    private int tlcolindex = 0;
    private int tlrowindex = 0;
    private int trcolindex = 0;
    private int trrowindex = 0;
    private int blcolindex = 0;
    private int blrowindex = 0;
    private int brcolindex = 0;
    private int brrowindex = 0;
    private boolean debug = false;
    private String elementid = "";
    private String rowheight = "28px";
    private Browser browser = null;

    public SmartScrollGridRenderer(String elementid) {
        this.elementid = elementid;
    }

    public SmartScrollGridRenderer(String elementid, Browser browser) {
        this.elementid = elementid;
        this.browser = browser;
    }

    public void setDefaultRowHeight(String height) {
        this.rowheight = height;
    }

    public void addTopLeftCell(String cellstr, String htmlclass, boolean isNewRow) {
        this.renderTopLeftCell(this.topleft, cellstr, htmlclass, "", isNewRow);
    }

    public void addTopLeftCell(String cellstr, String htmlclass, boolean isNewRow, int width) {
        this.renderTopLeftCell(this.topleft, cellstr, htmlclass, "", isNewRow, width);
    }

    public void renderTopLeftStart(StringBuffer html) {
        html.append("\n<table id=\"").append(this.elementid).append("top_t\" class=\"scrollgrid_top_Table\">").append("<tr id=\"").append(this.elementid).append("top_r0\">").append("<td id=\"").append(this.elementid).append("top_tl_td\" class=\"scrollgrid_Table maintform_fieldtitle_color\">").append("<div id=\"").append(this.elementid).append("tl_div\" class=\"scrollgrid_topleft_Div\" ").append(this.browser != null && !this.browser.isIE() ? "style=\"height:29px;\"" : "").append(">").append("<table id=\"").append(this.elementid).append("tl_t\" class=\"scrollgrid_Table\">").append("<tr height=\"" + this.rowheight + "\">");
    }

    public void renderTopLeftCell(StringBuffer html, String cellstr, String htmlclass, String onclick, boolean isNewRow) {
        this.renderTopLeftCell(html, cellstr, htmlclass, onclick, isNewRow, 0);
    }

    public void renderTopLeftCell(StringBuffer html, String cellstr, String htmlclass, String onclick, boolean isNewRow, int width) {
        String id = this.elementid + "tl_td" + this.tlcolindex;
        String w = "";
        w = width < 1 ? " style=\"width:100%\" " : " style=\"width:" + width + "px\" ";
        html.append("<td nowrap id=\"").append(id).append("\" class=\"").append(htmlclass).append("\" ").append(w).append(" >").append(cellstr).append(this.debug ? "(" + id + ")" : "").append("</td>");
        ++this.tlcolindex;
    }

    public void renderTopLeftEnd(StringBuffer html) {
        html.append("</tr></table>").append("</div>").append("</td>");
    }

    public void renderTopRightStart(StringBuffer html) {
        html.append("<td id=\"" + this.elementid + "top_tr_td\"  class=\"scrollgrid_Table\" >").append("<div id=\"" + this.elementid + "tr_div\" class=\"scrollgrid_topright_Div\">").append("<table id=\"" + this.elementid + "tr_t\" class=\"scrollgrid_Table\">").append("\n<tr height=\"" + this.rowheight + "\">");
    }

    public void addTopRightCell(String cellstr, String htmlclass, String onclick, boolean isNewRow) {
        this.renderTopRightCell(this.topright, cellstr, htmlclass, onclick, isNewRow);
    }

    public void renderTopRightCell(StringBuffer html, String cellstr, String htmlclass, String onclick, boolean isNewRow) {
        String id = this.elementid + "tr_td" + this.trcolindex;
        html.append("<td nowrap class=\"" + htmlclass + "\" id=\"" + id + "\"");
        if (onclick.length() > 0) {
            html.append(" onclick=\"" + onclick + "\" ");
        }
        html.append(">" + cellstr + (this.debug ? "(" + id + ")" : "") + "</td>");
        ++this.trcolindex;
    }

    public void renderTopRightEnd(StringBuffer html) {
        html.append("</tr></table></div></td>").append("</tr><tr id=\"" + this.elementid + "top_r1\">");
    }

    public void renderBottomLeftStart(StringBuffer html) {
        html.append("<td id=\"" + this.elementid + "top_bl_td\"  class=\"scrollgrid_Table\" valign=\"top\" >").append("<div id=\"" + this.elementid + "bl_div\" class=\"scrollgrid_bottomLeft_Div\">").append("<table id=\"" + this.elementid + "bl_t\" class=\"scrollgrid_Table\">");
    }

    public void addBottomLeftCell(String cellstr, String htmlclass, boolean isNewRow) {
        this.renderBottomLeftCell(this.bottomleft, cellstr, htmlclass, "", isNewRow);
    }

    public void addBottomLeftCell(ListColumn listColumn, boolean isNewRow) {
        this.renderBottomLeftCell(this.bottomleft, listColumn, isNewRow);
    }

    public void renderBottomLeftCell(StringBuffer html, ListColumn listColumn, boolean isNewRow) {
        if (isNewRow) {
            this.blcolindex = 0;
            if (this.blrowindex == 1) {
                html.append("\n<tr id=\"" + this.elementid + "bl_r" + this.blrowindex + "\" height=\"" + this.rowheight + "\">");
            } else {
                html.append("</tr>\n<tr id=\"" + this.elementid + "bl_r" + this.blrowindex + "\" height=\"" + this.rowheight + "\">");
            }
            ++this.blrowindex;
        }
        String id = "(row:" + this.elementid + "bl_r" + this.blrowindex + "c:" + this.elementid + "bl_td" + this.blcolindex + ")";
        if (this.blrowindex == 1) {
            listColumn.setColumnProperty("id", this.elementid + "bl_td" + this.blcolindex);
        } else {
            listColumn.setColumnProperty("id", "");
        }
        html.append(listColumn.getHtml());
        ++this.blcolindex;
    }

    public void renderBottomLeftCell(StringBuffer html, String cellstr, String htmlclass, String onclick, boolean isNewRow) {
        if (isNewRow) {
            this.blcolindex = 0;
            if (this.blrowindex == 1) {
                html.append("\n<tr id=\"" + this.elementid + "bl_r" + this.blrowindex + "\" height=\"" + this.rowheight + "\">");
            } else {
                html.append("</tr>\n<tr id=\"" + this.elementid + "bl_r" + this.blrowindex + "\" height=\"" + this.rowheight + "\">");
            }
            ++this.blrowindex;
        }
        String id = "(row:" + this.elementid + "bl_r" + this.blrowindex + "c:" + this.elementid + "bl_td" + this.blcolindex + ")";
        if (this.blrowindex == 1) {
            html.append("<td nowrap class=\"" + htmlclass + "\" id=\"" + this.elementid + "bl_td" + this.blcolindex + "\" width=\"*\">" + cellstr + (this.debug ? id : "") + "</td>");
        } else {
            html.append("<td nowrap class=\"" + htmlclass + "\" >" + cellstr + "</td>");
        }
        ++this.blcolindex;
    }

    public void renderBottomLeftEnd(StringBuffer html) {
        html.append("</tr></table></div></td>");
    }

    public void renderBottomRightStart(StringBuffer html) {
        html.append("<td id=\"" + this.elementid + "top_br_td\"  class=\"scrollgrid_Table\" valign=\"top\">").append("<div id=\"" + this.elementid + "br_div\" class=\"scrollgrid_bottomright_Div\" onscroll=\"scrollHeader( this, '" + this.elementid + "' )\">").append("<table id=\"" + this.elementid + "br_t\" class=\"scrollgrid_Table\">");
    }

    public void addBottomRightCell(String cellstr, String htmlclass, boolean isNewRow) {
        this.renderBottomRightCell(this.bottomright, cellstr, htmlclass, "", isNewRow);
    }

    public void renderBottomRightCell(StringBuffer html, String cellstr, String htmlclass, String onclick, boolean isNewRow) {
        if (isNewRow) {
            this.brcolindex = 0;
            if (this.brrowindex == 1) {
                html.append("\n<tr id=\"" + this.elementid + "br_r" + this.brrowindex + "\" height=\"" + this.rowheight + "\">");
            } else {
                html.append("</tr>\n<tr id=\"" + this.elementid + "br_r" + this.brrowindex + "\" height=\"" + this.rowheight + "\">");
            }
            ++this.brrowindex;
        }
        String id = "(row:br_r" + this.brrowindex + "c:br_td" + this.brrowindex + "_" + this.brcolindex + ")";
        if (this.brrowindex == 1) {
            html.append("<td nowrap class=\"" + htmlclass + "\" id=\"" + this.elementid + "br_td" + this.brrowindex + "_" + this.brcolindex + "\">\n" + cellstr + (this.debug ? id : "") + "</td>");
        } else {
            html.append("<td nowrap class=\"" + htmlclass + "\">\n" + cellstr + "</td>");
        }
        ++this.brcolindex;
    }

    public void renderBottomRightEnd(StringBuffer html) {
        html.append("</tr></table></div></td>");
        html.append("</tr></table>");
        html.append("\n<script>scrollgridspecmap['").append(this.elementid).append("scrollcols']=").append(this.brcolindex).append(";scrollgridspecmap['").append(this.elementid).append("scrollrows']=").append(this.brrowindex).append(";");
        html.append("\nscrollgridspecmap['").append(this.elementid).append("fixcols']=").append(this.blcolindex).append(";");
        html.append("\nscrollgridspecmap['elements']+='|").append(this.elementid).append("';");
        html.append("\nsapphire.events.attachEvent(window, 'onload', initScrollGrid);");
        html.append("\nsapphire.events.registerResizeListener(new Function('initScrollGrid();'));");
        html.append("\n</script>");
    }

    private String getGridStyle() {
        String bordercolor = "grey";
        String borderstyle = "groove";
        return "<style type=\"text/css\">\n    .scrollgrid_top_Table\t{ border: 0; padding: 0; margin: 0; border-collapse:collapse; }\n    .scrollgrid_Table\t{ border: 0; padding: 0; margin: 0; border-collapse:collapse }\n    .scrollDiv\t{ border: 0; padding: 0; margin: 0; border-collapse:collapse }\n    .scrollgrid_topleft_Div\t{ border-bottom-width: 2px; border-bottom-style:" + borderstyle + "; border-bottom-color:" + bordercolor + ";border-right-width: 2px; border-right-style:" + borderstyle + "; border-right-color:" + bordercolor + "; }\n    .scrollgrid_topright_Div\t{ border-left: 1px solid black; border-bottom-width: 2px; border-bottom-style:" + borderstyle + "; border-bottom-color:" + bordercolor + ";overflow-x: hidden;overflow-y: hidden; }\n    .scrollgrid_bottomLeft_Div\t{ border-top: 1px solid black;overflow-y: hidden;overflow-x:hidden;border-right-width: 2px; border-right-style:" + borderstyle + "; border-right-color:" + bordercolor + "; }\n    .scrollgrid_bottomright_Div\t{" + (this.browser != null && !this.browser.isIE() ? "position:relative" : "") + ";overflow: auto;border:0 }\n</style>";
    }

    public void renderGrid(StringBuffer html) {
        this.debug = false;
        if (this.trcolindex != this.brcolindex || this.blrowindex != this.brrowindex) {
            html.append("<font color=\"red\" >Wrong Table Format</font>");
        }
        html.append(this.getGridStyle());
        if (this.debug) {
            html.append("<!------------Start grid and Top Left -->");
        }
        this.renderTopLeftStart(html);
        html.append(this.topleft);
        this.renderTopLeftEnd(html);
        if (this.debug) {
            html.append("<!------------Top Right -->");
        }
        this.renderTopRightStart(html);
        html.append(this.topright);
        this.renderTopRightEnd(html);
        if (this.debug) {
            html.append("<!------------Bottom Left -->");
        }
        this.renderBottomLeftStart(html);
        html.append(this.bottomleft);
        this.renderBottomLeftEnd(html);
        if (this.debug) {
            html.append("<!------------Bottom Right and End Grid -->");
        }
        this.renderBottomRightStart(html);
        html.append(this.bottomright);
        this.renderBottomRightEnd(html);
    }
}

