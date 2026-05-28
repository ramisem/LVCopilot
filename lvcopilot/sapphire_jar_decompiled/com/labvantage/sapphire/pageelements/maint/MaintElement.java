/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.maint.MaintColumn;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class MaintElement
extends BaseElement {
    protected Tab tab = null;
    protected int rownum = -1;
    protected String selector = "checkbox";
    protected String id = "";
    protected String datasetname = "";
    protected String imgsrc = "";
    protected String[] keycols;
    private boolean fixedRowInputsDone = false;
    protected PropertyListCollection columns;
    private boolean showNoteImage = false;
    private DataSet notes;
    private HashMap findNotesMap;
    private PropertyList sdcProps;

    protected void init(PageContext pageContext, SDITagInfo sdiInfo, String connectionid, String datasetname) {
        this.pageContext = pageContext;
        this.sdiInfo = sdiInfo;
        this.setConnectionId(connectionid);
        this.datasetname = datasetname;
    }

    protected String getDatasetHtml(String dataset) {
        this.notes = this.sdiInfo.getDataSet("notes");
        TranslationProcessor tp = new TranslationProcessor(this.pageContext);
        boolean bl = this.showNoteImage = this.notes != null && this.notes.size() > 0;
        if (this.showNoteImage) {
            this.findNotesMap = new HashMap();
            this.sdcProps = (PropertyList)this.pageContext.getAttribute(this.sdiInfo.getSdcid() + "_props");
            if (this.sdcProps == null) {
                this.sdcProps = new PropertyList();
            }
        }
        StringBuffer html = new StringBuffer();
        if (this.pageContext.getAttribute("maintdetail.js") == null) {
            html.append("\n<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/maintdetail.js\"></script>\n");
            this.pageContext.setAttribute("maintdetail.js", (Object)"Y");
        }
        html.append("\n<table class=\"maintdetail_table\" id=\"").append(this.datasetname).append("\" cellspacing=\"0\">\n");
        QueryData queryData = this.sdiInfo.getQueryData(dataset);
        boolean dynamicTable = this.element.getProperty("dynamictable").equals("true");
        if (queryData != null && queryData.getRowCount() > 0) {
            queryData.resetRow(-1);
            html.append(this.getHeaderHtml());
            while (queryData.nextRow(-1)) {
                html.append(this.getRowHtml());
            }
        } else {
            html.append("<tr height=\"10\"><td>").append(ElementUtil.getText(this.element, "norowsfound", tp.translate("No data is available"))).append("</td></tr>\n");
        }
        if (dynamicTable) {
            queryData.setTemplateGenerate();
            html.append(SDITagUtil.getTemplateRowStart(this.datasetname));
            html.append(this.getRowHtml());
            html.append(SDITagUtil.getTemplateRowEnd());
        }
        html.append("</table>\n");
        return html.toString();
    }

    protected abstract String getDefaultRowHtml();

    protected abstract String getDefaultHeaderHtml();

    protected abstract void setRowKeyids();

    protected void processColumn(MaintColumn column) {
    }

    protected void setRowKeyCols() {
    }

    protected String getRowHtml() {
        this.setRowKeyCols();
        this.setRowKeyids();
        StringBuffer html = new StringBuffer();
        this.rownum = this.sdiInfo.getCurrentRow(this.datasetname);
        if (!this.fixedRowInputsDone) {
            html.append(SDITagUtil.getFixedRowInputs(this.datasetname, this.sdiInfo.getDataSet(this.datasetname).getColumns(), this.sdiInfo.getRowCount(this.datasetname), ""));
            this.fixedRowInputsDone = true;
        }
        html.append(SDITagUtil.getRepeatedRowInputs(this.datasetname, this.keycols, this.sdiInfo.getQueryData(this.datasetname), "", "", 1));
        html.append("<tr height=\"10\" class=\"maintdetail_row\" id=\"").append(this.datasetname).append("_row_").append(this.sdiInfo.getRowId(this.datasetname)).append("\">\n");
        html.append(this.getRowSelector());
        if (this.columns != null && this.columns.size() > 0) {
            MaintColumn maintColumn = new MaintColumn(this.pageContext, this.sdiInfo, this.getConnectionId());
            maintColumn.setElementProperties(this.element);
            maintColumn.setRowStatus("S");
            maintColumn.setDatasetname(this.datasetname);
            maintColumn.setSdcPropertyList((PropertyList)this.pageContext.getAttribute("sdc"));
            for (int j = 0; j < this.columns.size(); ++j) {
                PropertyList column = this.columns.getPropertyList(j);
                boolean isTranslate = "Y".equals(column.getProperty("translatevalue"));
                column.setProperty("class", "maintdetail_input");
                html.append("<td class=\"maintdetail_field\">");
                maintColumn.setColumn(column);
                String columnid = column.getProperty("columnid");
                PropertyList link = column.getPropertyList("link");
                this.processColumn(maintColumn);
                if (link != null && link.getProperty("href").length() > 0) {
                    if (isTranslate) {
                        html.append(ElementUtil.getLink(this.datasetname, columnid, this.sdiInfo, link, "", -1, this.getTranslationProcessor()));
                    }
                } else if (column.getProperty("pseudocolumn").length() > 0) {
                    if (isTranslate) {
                        html.append(ElementUtil.evaluateExpression(this.datasetname, this.rownum, columnid, column.getProperty("pseudocolumn"), this.sdiInfo, this.getTranslationProcessor()));
                    } else {
                        html.append(ElementUtil.evaluateExpression(this.datasetname, this.rownum, columnid, column.getProperty("pseudocolumn"), this.sdiInfo));
                    }
                } else {
                    html.append(maintColumn.getHtml());
                }
                html.append("</td>");
            }
        } else {
            return this.getDefaultRowHtml();
        }
        html.append("</tr>\n");
        return html.toString();
    }

    protected String getHeaderHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_headerrow\">\n");
        html.append(this.getHeaderSelector());
        if (this.columns != null && this.columns.size() > 0) {
            for (int j = 0; j < this.columns.size(); ++j) {
                PropertyList column = this.columns.getPropertyList(j);
                String title = column.getProperty("title");
                if (title == null || title.length() == 0) {
                    title = column.getProperty("columnid");
                }
                html.append("<td class=\"maintdetail_header\" ").append(column.getProperty("width").length() > 0 ? "width=\"" + column.getProperty("width") + "\"" : "").append(">").append(title).append("</td>\n");
            }
        } else {
            return this.getDefaultHeaderHtml();
        }
        html.append("</tr>\n");
        return html.toString();
    }

    @Override
    public String getHtml() {
        String sdcid;
        if (this.element.getPropertyList("selector") != null && this.element.getPropertyList("selector").getProperty("type").length() > 0) {
            this.selector = this.element.getPropertyList("selector").getProperty("type");
            if (this.selector.equals("img")) {
                this.imgsrc = this.element.getPropertyList("selector").getProperty("img", "WEB-CORE/images/blank.gif");
            }
        }
        if ((sdcid = this.element.getProperty("sdcid")).length() > 0) {
            ElementUtil.setSdcPropertyCache(this.pageContext, this.getConnectionId(), sdcid, "sdc");
        }
        StringBuffer html = new StringBuffer();
        StringBuffer buttonhtml = new StringBuffer();
        PropertyList tabprops = this.element.getPropertyList("tab");
        this.columns = this.element.getCollection("columns");
        PropertyListCollection buttons = this.element.getCollection("buttons");
        String buttonpos = this.element.getProperty("buttonposition");
        if (buttons != null && buttons.size() > 0) {
            if (buttonpos.indexOf("right") > 0) {
                buttonhtml.append("<table width=\"100%\"><tr><td align=\"right\">");
            } else if (buttonpos.indexOf("middle") > 0) {
                buttonhtml.append("<table width=\"100%\"><tr><td align=\"middle\">");
            } else {
                buttonhtml.append("<table width=\"100%\"><tr><td align=\"left\">");
            }
            buttonhtml.append("<table><tr>");
            for (int i = 0; i < buttons.size(); ++i) {
                buttonhtml.append("<td>");
                PropertyList buttonprops = buttons.getPropertyList(i);
                Button button = new Button(this.pageContext);
                ElementUtil.setButtonProperties(button, buttonprops);
                buttonhtml.append(button.getHtml());
                buttonhtml.append("</td>");
            }
            buttonhtml.append("</tr></table>");
            buttonhtml.append("</td></tr></table>");
        }
        if (buttonpos.indexOf("top") >= 0) {
            html.append(buttonhtml.toString()).append(this.getDatasetHtml(this.datasetname));
        } else if (buttonpos.indexOf("bottom") >= 0) {
            html.append(this.getDatasetHtml(this.datasetname)).append(buttonhtml.toString());
        } else {
            html.append(buttonhtml.toString()).append(this.getDatasetHtml(this.datasetname)).append(buttonhtml.toString());
        }
        TranslationProcessor tp = this.getTranslationProcessor();
        if (tabprops != null && !tabprops.getProperty("show").equals("N")) {
            this.tab = new Tab();
            ElementUtil.setTabProperties(this.tab, tabprops, this.datasetname, tp);
            this.tab.setContent(html.toString());
            return this.tab.getHtml();
        }
        if (tabprops == null) {
            this.tab = new Tab();
            ElementUtil.setTabProperties(this.tab, new PropertyList(), this.datasetname, tp);
            this.tab.setContent(html.toString());
            return this.tab.getHtml();
        }
        return html.toString();
    }

    protected String getHeaderSelector() {
        StringBuffer html = new StringBuffer();
        html.append("<td class=\"maintdetail_header\" width=\"10\">");
        String headeronclick = "checkAllDetail( this, '" + this.datasetname + "')";
        PropertyList tempSelector = this.element.getPropertyList("selector");
        if (tempSelector != null && tempSelector.getProperty("headeronclick").length() > 0) {
            headeronclick = tempSelector.getProperty("headeronclick");
        }
        if (this.selector.equals("checkbox")) {
            html.append("<input type=\"checkbox\" onClick=\"").append(headeronclick).append(";\" id=\"").append(this.datasetname).append("_selectAll\"/>");
        } else {
            html.append("&nbsp;");
        }
        html.append("</td>\n");
        return html.toString();
    }

    protected String getRowSelector() {
        StringBuffer html = new StringBuffer();
        String onclick = "";
        PropertyList tempSelector = this.element.getPropertyList("selector");
        if (tempSelector != null && tempSelector.getProperty("onclick").length() > 0) {
            onclick = "onClick=\"" + tempSelector.getProperty("onclick") + ";\"";
        }
        if (this.selector.equals("img") && this.imgsrc.length() == 0) {
            this.selector = "checkbox";
        }
        if (tempSelector != null && tempSelector.getProperty("tip") != null && tempSelector.getProperty("tip").length() > 0) {
            html.append("<td width=\"50px\" class=\"maintdetail_field\" title=\"").append(tempSelector.getProperty("tip")).append("\" width=\"10\">");
        } else {
            html.append("<td width=\"50px\" class=\"maintdetail_field\" width=\"10\">");
        }
        if (this.selector.equals("checkbox")) {
            html.append("<input type=\"checkbox\" name=\"").append(this.datasetname).append("_selector\" value=\"").append(this.id).append("\" ").append(onclick).append(" id=\"").append(this.datasetname).append("_").append(this.sdiInfo.getRowId(this.datasetname)).append("\"/>");
        } else if (this.selector.equals("radio")) {
            html.append("<input type=\"radio\" name=\"").append(this.datasetname).append("_selector\" value=\"").append(this.id).append("\" ").append(onclick).append(" id=\"").append(this.datasetname).append("_").append(this.sdiInfo.getRowId(this.datasetname)).append("\"/>");
        } else if (this.selector.equals("img")) {
            html.append("<img border=\"0\" src=\"").append(this.imgsrc).append("\" ").append(onclick).append("/>");
        } else {
            html.append("&nbsp;");
        }
        if (this.showNoteImage) {
            int currentRow = this.sdiInfo.getCurrentRow(this.datasetname);
            String keyid1 = this.sdiInfo.getDataSet("primary").getValue(currentRow, this.sdcProps.getProperty("keycolid1"), "");
            String keyid2 = this.sdiInfo.getDataSet("primary").getValue(currentRow, this.sdcProps.getProperty("keycolid2"), "(null)");
            String keyid3 = this.sdiInfo.getDataSet("primary").getValue(currentRow, this.sdcProps.getProperty("keycolid3"), "(null)");
            this.findNotesMap.put("sdcid", this.sdiInfo.getSdcid());
            this.findNotesMap.put("keyid1", keyid1);
            this.findNotesMap.put("keyid2", keyid2);
            this.findNotesMap.put("keyid3", keyid3);
            if (this.datasetname.equals("attachment")) {
                this.findNotesMap.put("attachmentnum", this.sdiInfo.getBigDecimal(this.datasetname, "attachmentnum"));
            }
            if (this.notes.findRow(this.findNotesMap) >= 0) {
                html.append("<a href=\"#\" title=\"Show notes\" onclick=\"parent.openSDINotes('").append(this.sdiInfo.getSdcid()).append("','").append(keyid1).append("','").append(keyid2).append("','").append(keyid3).append("',").append("function(){parent.sdiNotes.flashContextNotes('").append(this.sdiInfo.getSdcid()).append(";").append(keyid1).append(";").append(keyid2).append(";").append(keyid3);
                if (this.datasetname.equals("attachment")) {
                    html.append(";").append(this.sdiInfo.getBigDecimal(this.datasetname, "attachmentnum"));
                }
                html.append("')})\">").append("<img src=\"WEB-CORE/imageref/finance_business_and_trade/office/notes/16/note.png\" style=\"border:none;margin-bottom:-2px\"/></a>");
            }
        }
        html.append("</td>\n");
        return html.toString();
    }
}

