/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.coc;

import com.labvantage.opal.elements.coc.COCFaceStd;
import com.labvantage.opal.elements.coc.SDICOC;
import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.util.CocUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.ArrayList;
import java.util.List;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class COCElement
extends BaseElement {
    public static String LABVANTAGE_CVS_ID = "$Revision: 70468 $";
    private Button __Button;
    private String __ButtonPlacement;
    private String __ButtonsTopPlacement;
    private String __ButtonsBottomPlacement;
    private List __ColumnsList = new ArrayList();
    private List __TitleList = new ArrayList();
    private List __WidthList = new ArrayList();
    private List __AlignList = new ArrayList();
    private List __ModeList = new ArrayList();

    @Override
    public String getHtml() {
        String buttons = "";
        StringBuffer sb = new StringBuffer();
        SQLGenerator __SqlGenerator = SQLFactory.getSqlGenerator(this.pageContext);
        PropertyList pagedata = (PropertyList)this.pageContext.getRequest().getAttribute("pagedata");
        sb.append("<script>");
        sb.append("function fnCallback() {");
        sb.append("var winObjectt = getWindowObject();");
        sb.append("winObjectt.opener.").append(this.pageContext.getRequest().getParameter("callback")).append(";");
        sb.append("closePage();");
        sb.append("}");
        sb.append("function getWindowObject() {");
        sb.append("var winObjectt;");
        sb.append("var iDN;");
        sb.append("if ( ( typeof(window.opener) == 'undefined' || window.opener == null) && window.frameElement != null ) {");
        sb.append("iDN = sapphire.util.dom.getAttribute( window.frameElement, 'dialogNumber' );");
        sb.append("if ( iDN != null ) {");
        sb.append("winObjectt = sapphire.ui.dialog.getDialogObject( iDN );");
        sb.append("}");
        sb.append("}");
        sb.append("else {");
        sb.append("winObjectt = window;");
        sb.append("}");
        sb.append("return winObjectt");
        sb.append("}");
        sb.append("</script>");
        Object obj = CocUtil.getSDICOC(pagedata, this.getQueryProcessor(), this.getSDCProcessor(), __SqlGenerator);
        if (obj instanceof String) {
            sb.append("<center>");
            sb.append("<DIV style='border:1px solid red; width:95%;' align=left>");
            sb.append("<font color=red><b>An error has occured while");
            sb.append(" getting the COC information:</b></font>");
            sb.append("<br><br>");
            sb.append(obj.toString());
            sb.append("</DIV>");
            sb.append("<br><DIV style='width:95%' align=right>");
            sb.append("<input type='button' value='Close' onClick='closePage();'>");
            sb.append("</DIV>");
            return sb.toString();
        }
        SDICOC __SdiCoc = (SDICOC)obj;
        __SdiCoc.setElement(this.element);
        __SdiCoc.setCustodianPwdMap(CocUtil.getCustodianPwdRequiredMap(this.getQueryProcessor(), __SqlGenerator));
        if (__SdiCoc.size() == 0) {
            sb.append("<script>");
            sb.append("    fnCallback();");
            sb.append("</script>");
        } else {
            String manual = this.requestContext.getProperty("manual");
            if (manual == null || manual.length() == 0) {
                manual = "N";
            }
            sb.append("<script>");
            if (manual.equalsIgnoreCase("Y")) {
                sb.append("    var _manualmode = true;");
            } else {
                sb.append("    var _manualmode = false;");
            }
            sb.append("</script>");
            this.setElementButtonPlacement();
            if (!this.__ButtonPlacement.equals("none")) {
                buttons = this.getButtons(this.element.getId());
            }
            COCFaceStd cocface = new COCFaceStd(this.element, __SdiCoc);
            cocface.setTranslationProcessor(new TranslationProcessor(this.pageContext));
            this.parseElementColumns();
            sb.append("<script>");
            sb.append(this.getScriptVariables());
            sb.append(this.getColumnScript());
            sb.append(__SdiCoc.getJSArray());
            sb.append("</script>");
            sb.append("<table cellpadding=2 cellspacing=0 border=0>");
            sb.append("<tr><td colspan=3>");
            if (!this.__ButtonPlacement.equals("none") && !this.__ButtonsTopPlacement.equals("none")) {
                sb.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'>");
                sb.append("<tr><td width='100%' align='").append(this.__ButtonsTopPlacement).append("'>");
                sb.append(buttons);
                sb.append("</td></tr></table>");
            }
            sb.append("</td></tr>");
            sb.append("<tr><td rowspan=3 valign=top>");
            sb.append(cocface.getGroupBar());
            sb.append("</td><td>");
            sb.append(cocface.getFromTab());
            sb.append("</td><td>");
            sb.append(cocface.getToTab());
            sb.append("</td></tr>");
            if (__SdiCoc.isWitnessrequired()) {
                sb.append("<tr><td colspan=2>");
                sb.append(cocface.getWitnessTab(CocUtil.getCertifiedAnalysts(this.getQueryProcessor(), __SqlGenerator)));
                sb.append("</td></tr>");
            }
            sb.append("<tr><td colspan=2>");
            sb.append(cocface.getDataTab());
            sb.append("</td></tr>");
            sb.append("<tr><td colspan=3>");
            if (!this.__ButtonPlacement.equals("none") && !this.__ButtonsBottomPlacement.equals("none")) {
                sb.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'>");
                sb.append("<tr><td width='100%' align='");
                sb.append(this.__ButtonsBottomPlacement).append("'>");
                sb.append(buttons);
                sb.append("</td></tr></table>");
            }
            sb.append("</td></tr>");
            sb.append("</table>");
            sb.append("<script>");
            sb.append("    fnInitCOC();");
            sb.append("    if ( _witnessrequired ) {");
            sb.append("        fnResizeWindow( 'witnessblock' );");
            sb.append("    }");
            sb.append("</script>");
        }
        return sb.toString();
    }

    private String getScriptVariables() {
        StringBuffer sb = new StringBuffer();
        String historypage = this.element.getProperty("historypage");
        if (historypage == null || historypage.length() == 0) {
            historypage = "";
        }
        sb.append("var _elementid = '").append(this.element.getId()).append("';");
        sb.append("var c1_").append(this.element.getId()).append(" = new Array();");
        sb.append("var _historypage = '").append(historypage).append("';");
        return sb.toString();
    }

    private String getColumnScript() {
        StringBuffer sb = new StringBuffer();
        StringBuffer array = new StringBuffer();
        for (int i = 0; i < this.__ColumnsList.size(); ++i) {
            array.delete(0, array.length());
            array.append("[\"").append((String)this.__ColumnsList.get(i)).append("\", \"").append((String)this.__WidthList.get(i)).append("\", \"").append((String)this.__AlignList.get(i)).append("\", \"").append((String)this.__ModeList.get(i)).append("\"]");
            sb.append("c1_").append(this.element.getId()).append("[").append(i).append("] = ").append(array.toString()).append(";\n");
        }
        return sb.toString();
    }

    private boolean parseElementColumns() {
        PropertyListCollection columns = this.element.getPropertyList("data").getPropertyList("body").getCollection("columns");
        if (columns == null) {
            return false;
        }
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            String columnid = column.getProperty("column");
            String columntitle = column.getProperty("title");
            String columnwidth = column.getProperty("width");
            String columnalign = column.getProperty("align");
            String columnmode = column.getProperty("mode");
            if (columntitle == null || columntitle.length() == 0) {
                columntitle = columnid;
            }
            if (columnwidth == null || columnwidth.length() == 0) {
                columnwidth = "150";
            }
            if (columnalign == null || columnalign.length() == 0) {
                columnalign = "left";
            }
            if (columnmode == null || columnmode.length() == 0) {
                columnmode = "readonly";
            }
            this.__ColumnsList.add(i, columnid);
            this.__TitleList.add(i, columntitle);
            this.__WidthList.add(i, columnwidth);
            this.__AlignList.add(i, columnalign);
            this.__ModeList.add(i, columnmode);
        }
        return true;
    }

    public void setElementButtonPlacement() {
        this.__ButtonPlacement = this.element.getProperty("buttonplacement");
        if (this.__ButtonPlacement == null) {
            this.__ButtonPlacement = "none";
        } else if (this.__ButtonPlacement.equals("topleft")) {
            this.__ButtonsTopPlacement = "left";
            this.__ButtonsBottomPlacement = "none";
        } else if (this.__ButtonPlacement.equals("topmiddle")) {
            this.__ButtonsTopPlacement = "middle";
            this.__ButtonsBottomPlacement = "none";
        } else if (this.__ButtonPlacement.equals("topright")) {
            this.__ButtonsTopPlacement = "right";
            this.__ButtonsBottomPlacement = "none";
        } else if (this.__ButtonPlacement.equals("bottomleft")) {
            this.__ButtonsBottomPlacement = "left";
            this.__ButtonsTopPlacement = "none";
        } else if (this.__ButtonPlacement.equals("bottommiddle")) {
            this.__ButtonsBottomPlacement = "middle";
            this.__ButtonsTopPlacement = "none";
        } else if (this.__ButtonPlacement.equals("bottomright")) {
            this.__ButtonsBottomPlacement = "right";
            this.__ButtonsTopPlacement = "none";
        } else if (this.__ButtonPlacement.equals("left")) {
            this.__ButtonsBottomPlacement = "left";
            this.__ButtonsTopPlacement = "left";
        } else if (this.__ButtonPlacement.equals("middle")) {
            this.__ButtonsBottomPlacement = "middle";
            this.__ButtonsTopPlacement = "middle";
        } else if (this.__ButtonPlacement.equals("right")) {
            this.__ButtonsBottomPlacement = "right";
            this.__ButtonsTopPlacement = "right";
        }
    }

    protected String getButtons(String id) {
        StringBuffer sb = new StringBuffer();
        PropertyListCollection buttonscollection = this.element.getCollection("buttons");
        sb.append("<table border=0 cellspacing=3 cellpadding=0><tr>");
        for (int i = 0; i < buttonscollection.size(); ++i) {
            PropertyList propertylist = buttonscollection.getPropertyList(i);
            String show = propertylist.getProperty("show");
            String text = propertylist.getProperty("text");
            String width = propertylist.getProperty("width");
            String js = propertylist.getProperty("js");
            String tip = propertylist.getProperty("tip");
            String img = propertylist.getProperty("img");
            String appearance = propertylist.getProperty("appearance");
            String margin = propertylist.getProperty("margin");
            String style = propertylist.getProperty("style");
            String highlight = propertylist.getProperty("highlight");
            String buttonid = propertylist.getProperty("id");
            if (show == null || show.length() == 0) {
                show = "Y";
            }
            if (text == null) {
                text = "";
            }
            if (width == null || width.length() == 0) {
                width = "80";
            }
            if (js == null || js.length() == 0) {
                js = "";
            }
            if (appearance == null || appearance.length() == 0) {
                appearance = "standard";
            }
            if (tip == null) {
                tip = "";
            }
            if (margin == null) {
                margin = "";
            }
            if (style == null) {
                style = "";
            }
            if (highlight == null || highlight.length() == 0) {
                highlight = "Y";
            }
            if (buttonid == null) {
                buttonid = "button";
            }
            sb.append("<td>");
            if (show.equals("Y")) {
                sb.append(this.getButton(text, width, js, tip, img, appearance, margin, style, highlight, buttonid));
            }
            sb.append("</td>");
        }
        sb.append("</tr></table>");
        return sb.toString();
    }

    protected String getButton(String text, String width, String js, String tip, String img, String appearance, String margin, String style, String highlight, String buttonid) {
        TranslationProcessor tp = new TranslationProcessor(this.pageContext);
        if (this.__Button == null) {
            this.__Button = new Button(this.pageContext);
        }
        this.__Button.setText(tp.translate(text));
        this.__Button.setWidth(width);
        this.__Button.setAction(js);
        this.__Button.setTip(tip);
        if (img.length() > 0) {
            this.__Button.setImg(img);
        }
        this.__Button.setAppearance(appearance);
        this.__Button.setMargin(margin);
        this.__Button.setStyle(style);
        this.__Button.setHighlight(highlight);
        this.__Button.setId(buttonid);
        return this.__Button.getHtml();
    }
}

