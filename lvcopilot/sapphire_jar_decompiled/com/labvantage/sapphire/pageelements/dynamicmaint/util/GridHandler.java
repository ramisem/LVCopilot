/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.util;

import sapphire.accessor.TranslationProcessor;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GridHandler {
    private String elementid;
    private boolean isDatasetGrid;
    private String jsFunc;
    private TranslationProcessor tp;
    boolean printCopyPasteOptions;

    public GridHandler(String elementid, String jsFunc, TranslationProcessor tp, boolean isDatasetGrid, boolean printCopyPasteOptions) {
        this.elementid = elementid;
        this.isDatasetGrid = isDatasetGrid;
        this.tp = tp;
        this.jsFunc = jsFunc;
        this.printCopyPasteOptions = printCopyPasteOptions;
    }

    public String getGridHandler(PropertyListCollection contextMenu) {
        StringBuilder html = new StringBuilder();
        html.append("\n<div id=\"").append(this.elementid).append("menu1\" selectblockerid=\"").append(this.elementid).append("menu1selectblocker\" style=\"position: absolute;display:none;\" >");
        html.append("\n<iframe id=\"").append(this.elementid).append("menu1selectblocker\" frameborder=\"0\" width=\"100\" style=\"position:relative; top:0px; left:0px;\"");
        html.append("\nsrc=\"WEB-CORE/blank.html\"></iframe>");
        html.append("\n<div id=\"").append(this.elementid).append("menu1shadow\" class=\"menushadow\" style=\"position:absolute; top:5px; left:5px;\"></div>");
        html.append("\n<div class=\"menuholder\" style=\"position:absolute; top:0px; left:0px\">");
        if (this.printCopyPasteOptions) {
            html.append("\n    <div class=\"menu\"  onclick=\"").append(this.elementid).append("menu1.handler.cut()\"");
            html.append("\n      onmouseover=\"this.className='menuselected'\"");
            html.append("\n      onmouseout=\"this.className='menu'\"");
            html.append("\n      id=\"").append(this.elementid).append("cut\">&nbsp;").append(this.tp.translate("Cut")).append("</div>");
            html.append("\n    <div class=\"menu\"  onclick=\"").append(this.elementid).append("menu1.handler.copy()\"");
            html.append("\n      onmouseover=\"this.className='menuselected'\"");
            html.append("\n      onmouseout=\"this.className='menu'\" id=\"").append(this.elementid).append("copy\">&nbsp;").append(this.tp.translate("Copy")).append("</div>");
            html.append("\n    <div class=\"menu\"  onclick=\"").append(this.elementid).append("menu1.handler.paste()\"");
            html.append("\n      onmouseover=\"this.className='menuselected'\"");
            html.append("\n      onmouseout=\"this.className='menu'\"");
            html.append("\n      id=\"").append(this.elementid).append("paste\">&nbsp;").append(this.tp.translate("Paste")).append("</div>");
            html.append("\n    <div class=\"menusep1\" id=\"").append(this.elementid).append("sep1_1\"></div>");
        }
        html.append("\n    <div class=\"menusep2\" id=\"").append(this.elementid).append("sep1_2\"></div>");
        html.append("\n    <div class=\"menu\" onclick=\"").append(this.elementid).append("menu1.handler.fillDown()\"");
        html.append("\n      onmouseover=\"this.className='menuselected'\"");
        html.append("\n      onmouseout=\"this.className='menu'\" id=\"").append(this.elementid).append("filldown\">&nbsp;").append(this.tp.translate("Fill down")).append("</div>");
        html.append("\n    <div class=\"menu\" onclick=\"").append(this.elementid).append("menu1.handler.fillAcross()\"");
        html.append("\n      onmouseover=\"this.className='menuselected'\"");
        html.append("\n      onmouseout=\"this.className='menu'\"");
        html.append("\n      id=\"").append(this.elementid).append("fillacross\">&nbsp;").append(this.tp.translate("Fill across")).append("</div>");
        if (!this.isDatasetGrid) {
            html.append("\n    <div class=\"menu\" onclick=\"dmgridhandler.increaseDown('").append(this.elementid).append("')\"");
            html.append("\n      onmouseover=\"this.className='menuselected'\"");
            html.append("\n      onmouseout=\"this.className='menu'\"");
            html.append("\n      id=\"").append(this.elementid).append("increasedown\">&nbsp;").append(this.tp.translate("Increase down")).append("</div>");
        }
        if (contextMenu != null && contextMenu.size() > 0) {
            html.append("\n    <div class=\"menusep1\" id=\"").append(this.elementid).append("sep1_3\"></div>");
            html.append("\n    <div class=\"menusep2\" id=\"").append(this.elementid).append("sep1_4\"></div>");
            for (int i = 0; i < contextMenu.size(); ++i) {
                PropertyList menuItem = contextMenu.getPropertyList(i);
                String id = menuItem.getProperty("id", "menuitem" + i);
                String title = menuItem.getProperty("title", "");
                String functionType = menuItem.getProperty("functiontype", "Standard");
                if (functionType.equals("Standard")) {
                    html.append(this.standardFunction(this.jsFunc, id, title, menuItem.getProperty("function", "")));
                    continue;
                }
                if (!functionType.equals("Custom")) continue;
                html.append(this.function(id, title, menuItem.getProperty("customfunction", "")));
            }
        }
        html.append("\n</div>");
        html.append("\n</div>");
        return html.toString();
    }

    private String standardFunction(String jsFunc, String id, String title, String function) {
        String functionStr = "";
        if (function.equals("release")) {
            functionStr = jsFunc + ".release()";
        } else if (function.equals("unrelease")) {
            functionStr = jsFunc + ".unrelease()";
        } else if (function.equals("accept default")) {
            functionStr = jsFunc + ".acceptDefault()";
        } else if (function.equals("cancel paramlist")) {
            functionStr = jsFunc + ".cancelParamList(true)";
        } else if (function.equals("uncancel paramlist")) {
            functionStr = jsFunc + ".cancelParamList(false)";
        } else if (function.equals("mark outlier")) {
            functionStr = jsFunc + ".markOutlier(true)";
        } else if (function.equals("clear outlier")) {
            functionStr = jsFunc + ".markOutlier(false)";
        }
        if (!functionStr.equals("")) {
            return this.function(id, title, functionStr);
        }
        return "";
    }

    private String function(String id, String title, String function) {
        String retval = "";
        retval = retval + "\n    <div class=\"menu\" onclick=\"" + function + "\"";
        retval = retval + "\n      onmouseover=\"this.className='menuselected'\"";
        retval = retval + "\n      onmouseout=\"this.className='menu'\"";
        retval = retval + "\n      id=\"" + this.elementid + id + "\">&nbsp;" + this.tp.translate(title) + "</div>";
        return retval;
    }
}

