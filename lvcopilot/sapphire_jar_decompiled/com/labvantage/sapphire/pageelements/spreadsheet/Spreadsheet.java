/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.spreadsheet;

import com.labvantage.sapphire.modules.datafile.DataFileUtil;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.servlet.jsp.PageContext;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Spreadsheet
extends BaseElement {
    public static final String VIEWONLY_PROP = "viewonly";
    public static final String WIDTH_PROP = "width";
    public static final String HEIGHT_PROP = "height";
    public static final String EVENTS_PROP = "events";
    public static final String NAME_PROP = "name";
    public static final String ACTION_PROP = "action";
    public static final String sJSOBJECTNAME = "spreadsheet";
    private DataSet noteData = null;
    private String width = "800";
    private String height = "600";
    private boolean viewOnly = false;
    private PropertyListCollection events;

    public Spreadsheet(PageContext pageContext) {
        this.setPageContext(pageContext);
    }

    public Spreadsheet(PageContext pageContext, PropertyList element) {
        this.setElementProperties(element);
        this.setPageContext(pageContext);
    }

    public Spreadsheet(PageContext pageContext, PropertyList element, DataSet noteData) {
        this.setElementProperties(element);
        this.setPageContext(pageContext);
        this.setSpreadsheetData(noteData);
    }

    public void setUp() {
        if (this.element != null) {
            if (this.element.containsKey(EVENTS_PROP) && this.element.getCollection(EVENTS_PROP).size() > 0) {
                this.events = this.element.getCollection(EVENTS_PROP);
            }
            this.width = this.element.getProperty(WIDTH_PROP, this.width);
            this.height = this.element.getProperty(HEIGHT_PROP, this.height);
            String temp = this.element.getProperty(VIEWONLY_PROP, "" + this.viewOnly);
            this.viewOnly = temp.equalsIgnoreCase("y") || temp.equalsIgnoreCase("yes") || temp.equalsIgnoreCase("true");
        }
    }

    public int addEvent(String eventname, String eventFunction) {
        if (this.events == null) {
            this.events = new PropertyListCollection();
        }
        if (eventname.length() > 0) {
            if ((eventname = eventname.toLowerCase()).startsWith("on")) {
                eventname = eventname.substring(2);
            }
            PropertyList event = new PropertyList(eventname);
            event.setProperty(NAME_PROP, eventname);
            event.setProperty(ACTION_PROP, eventFunction);
            this.events.add(event);
        }
        return this.events.size() - 1;
    }

    public void setWidth(int width) {
        this.width = "" + width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = "" + height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public void setSpreadsheetData(DataSet noteData) {
        this.noteData = noteData;
    }

    private DataSet createNewSpreadSheetData(int numCols) {
        this.logger.debug("createNewSpreadSheetData called...");
        DataSet out = new DataSet();
        for (int i = 0; i < numCols; ++i) {
            out.addColumn(Spreadsheet.columnNumToChar(i + 1), 0);
        }
        return out;
    }

    private DataSet getNewSpreadsheetData() {
        this.logger.debug("getSpreadsheetData called...");
        DataSet out = this.createNewSpreadSheetData(26);
        this.addRows(out);
        return out;
    }

    private void addRows(DataSet data) {
        int size = 50;
        if (data.getRowCount() > 50) {
            size = data.getRowCount() + 2;
        }
        while (data.getRowCount() < size) {
            data.addRow();
        }
    }

    private DataSet getSpreadsheetData(DataSet data) {
        this.logger.debug("getSpreadsheetData called...");
        int colsize = data.getColumnCount() + 2;
        if (colsize < 15) {
            colsize = 15;
        }
        DataSet out = this.createNewSpreadSheetData(colsize);
        for (int k = 0; k < data.getRowCount(); ++k) {
            out.addRow();
            for (int i = 0; i < data.getColumnCount(); ++i) {
                String oldcolname = data.getColumnId(i);
                String newcolname = out.getColumnId(i);
                out.setValue(k, newcolname, data.getValue(k, oldcolname, ""));
            }
        }
        this.addRows(out);
        return out;
    }

    private String getCellType(String value, int datasettype) {
        String type;
        switch (datasettype) {
            case 1: {
                type = "number";
                break;
            }
            case 2: {
                type = "date";
                break;
            }
            default: {
                type = "string";
            }
        }
        if (type.equalsIgnoreCase("string") && value.length() > 0) {
            try {
                Double.parseDouble(value);
                type = "number";
            }
            catch (NumberFormatException e1) {
                if (value.indexOf("/") > -1) {
                    value = StringUtil.replaceAll(value, "/", "-");
                }
                if (value.indexOf("-") > -1) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
                    dateFormat.setLenient(true);
                    try {
                        dateFormat.parse(value);
                        type = "date";
                    }
                    catch (ParseException e2) {
                        type = "string";
                    }
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yy");
                try {
                    dateFormat.parse(value);
                    type = "date";
                }
                catch (ParseException e3) {
                    type = "string";
                }
            }
        }
        return type;
    }

    protected static String columnNumToChar(int number) {
        return DataFileUtil.getColumnName(number);
    }

    protected static String columnNumToChar(String number) {
        if (number.length() > 0) {
            try {
                int num = Integer.parseInt(number);
                return Spreadsheet.columnNumToChar(num);
            }
            catch (NumberFormatException e) {
                return "";
            }
        }
        return "";
    }

    private void renderSpreadsheet(StringBuffer html, DataSet noteData, String widthBounds, String heightBounds) {
        String action;
        String name;
        PropertyList event;
        this.logger.debug("renderSpreadSheet called...");
        StringBuffer rows = new StringBuffer();
        noteData = noteData != null ? this.getSpreadsheetData(noteData) : this.getNewSpreadsheetData();
        int colunWidth = 80;
        int labelWidth = 25;
        int rowHeight = 20;
        int tableWidth = labelWidth + colunWidth * noteData.getColumnCount();
        html.append("<div class='spreadsheet_div' style='width: auto;height: auto;position: absolute;top: 0;left: 0;right: 0;bottom: 0;'>");
        html.append("<table border=0 id='").append(this.elementid).append("_table' class='spreadsheet_table' width='").append(tableWidth).append("' cellspacing=0 cellpadding=0>");
        html.append("<tr height='").append(rowHeight).append("'>");
        StringBuffer columns = new StringBuffer();
        html.append("<th class='label_col'  width='").append(labelWidth).append("' id='").append(this.elementid).append("_r0_c0' ");
        html.append(" _row='").append(0).append("' _col='").append(0).append("' _type='header' ");
        if (this.events != null) {
            for (int i = 0; i < this.events.size(); ++i) {
                PropertyList event2 = this.events.getPropertyList(i);
                String name2 = event2.getProperty(NAME_PROP, "");
                String action2 = event2.getProperty(ACTION_PROP, "");
                if (name2.length() <= 0 || action2.length() <= 0) continue;
                if (!name2.toLowerCase().startsWith("on")) {
                    name2 = "on" + name2;
                }
                html.append(" ").append(name2).append("='").append(action2).append("(\"").append(this.elementid).append("\",0,0);' ");
            }
        }
        html.append(" >");
        html.append("&nbsp;");
        html.append("</th>");
        columns.append("[").append(this.elementid).append("_r0_c0");
        for (int colIndex = 0; colIndex < noteData.getColumnCount(); ++colIndex) {
            html.append("<th class='header_col'  width='").append(colunWidth).append("' id='").append(this.elementid).append("_r0_c").append(colIndex + 1).append("' ");
            html.append(" _row='").append(0).append("' _col='").append(colIndex + 1).append("' _type='header' ");
            if (this.events != null) {
                for (int i = 0; i < this.events.size(); ++i) {
                    event = this.events.getPropertyList(i);
                    name = event.getProperty(NAME_PROP, "");
                    action = event.getProperty(ACTION_PROP, "");
                    if (name.length() <= 0 || action.length() <= 0) continue;
                    if (!name.toLowerCase().startsWith("on")) {
                        name = "on" + name;
                    }
                    html.append(" ").append(name).append("='").append(action).append("(\"").append(this.elementid).append("\",0,").append(colIndex + 1).append(");' ");
                }
            }
            html.append(" >");
            html.append(noteData.getColumnId(colIndex).toUpperCase());
            html.append("</th>");
            columns.append(",").append(this.elementid).append("_r0_c").append(colIndex + 1);
        }
        html.append("</tr>");
        rows.append("[").append(columns.toString()).append("]");
        this.logger.debug("dsSSData.getRowCount = " + noteData.getRowCount());
        for (int rowIndex = 0; rowIndex < noteData.getRowCount(); ++rowIndex) {
            html.append("<tr height='").append(rowHeight).append("'>");
            columns = new StringBuffer();
            html.append("<td class='label_cell' align=center id='").append(this.elementid).append("_r").append(rowIndex + 1).append("_c0' ");
            html.append(" _row='").append(rowIndex + 1).append("' _col='").append(0).append("' _type='header' ");
            if (this.events != null) {
                for (int i = 0; i < this.events.size(); ++i) {
                    event = this.events.getPropertyList(i);
                    name = event.getProperty(NAME_PROP, "");
                    action = event.getProperty(ACTION_PROP, "");
                    if (name.length() <= 0 || action.length() <= 0) continue;
                    if (!name.toLowerCase().startsWith("on")) {
                        name = "on" + name;
                    }
                    html.append(" ").append(name).append("='").append(action).append("(\"").append(this.elementid).append("\",").append(rowIndex + 1).append(",0);' ");
                }
            }
            html.append(" >");
            html.append("").append(rowIndex + 1).append("");
            html.append("</td>");
            columns.append("[").append(this.elementid).append("_r").append(rowIndex + 1).append("_c0");
            for (int colIndex = 0; colIndex < noteData.getColumnCount(); ++colIndex) {
                String type;
                String id = noteData.getColumnId(colIndex);
                String value = noteData.getValue(rowIndex, id, "");
                String className = "normal_cell";
                if (value.startsWith("{[") && value.endsWith("]}")) {
                    className = className + " markup_cell";
                    value = value.substring(2, value.length() - 2);
                    type = "markup";
                } else {
                    type = this.getCellType(value, noteData.getColumnType(id));
                }
                html.append("<td class='").append(className).append("' align=right nowrap ");
                html.append(" id='").append(this.elementid).append("_r").append(rowIndex + 1).append("_c").append(colIndex + 1).append("' ");
                html.append(" _row='").append(rowIndex + 1).append("' _col='").append(colIndex + 1).append("' _type='").append(type).append("' ");
                if (this.events != null) {
                    for (int i = 0; i < this.events.size(); ++i) {
                        PropertyList event3 = this.events.getPropertyList(i);
                        String name3 = event3.getProperty(NAME_PROP, "");
                        String action3 = event3.getProperty(ACTION_PROP, "");
                        if (name3.length() <= 0 || action3.length() <= 0) continue;
                        if (!name3.toLowerCase().startsWith("on")) {
                            name3 = "on" + name3;
                        }
                        html.append(" ").append(name3).append("='").append(action3).append("(\"").append(this.elementid).append("\",").append(rowIndex + 1).append(",").append(colIndex + 1).append(");' ");
                    }
                }
                html.append(" >");
                if (value != null && value.length() > 0) {
                    html.append(value);
                } else {
                    html.append("&nbsp;");
                }
                html.append("</td>");
                columns.append(",").append(this.elementid).append("_r").append(rowIndex + 1).append("_c").append(colIndex + 1);
            }
            html.append("</tr>");
            rows.append(",").append(columns.toString()).append("]");
        }
        html.append("</table>");
        rows.append("]");
        html.append("</div>");
        html.append("<script>");
        html.append("spreadsheet.data['").append(this.elementid).append("'].grid=").append(rows.toString()).append(";");
        html.append("</script>");
    }

    private void renderSriptAndStyle(StringBuffer html, boolean viewOnly) {
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/spreadsheet/scripts/spreadsheet.js\"></script>");
        if (this.browser.isWebkit()) {
            html.append("<link id=\"spreadsheetstyle\" rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/spreadsheet/stylesheets/spreadsheet_webkit.css", this.pageContext) + "\" type=\"text/css\">");
        } else {
            html.append("<link id=\"spreadsheetstyle\" rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/spreadsheet/stylesheets/spreadsheet.css", this.pageContext) + "\" type=\"text/css\">");
        }
        html.append("<script>");
        html.append("spreadsheet.data['").append(this.elementid).append("']=").append("{}").append(";");
        html.append("spreadsheet.data['").append(this.elementid).append("'].viewOnly=").append(viewOnly).append(";");
        html.append("spreadsheet.data['").append(this.elementid).append("'].currentEdit=").append("null").append(";");
        html.append("spreadsheet.data['").append(this.elementid).append("'].selected=").append("new Array()").append(";");
        html.append("spreadsheet.data['").append(this.elementid).append("'].allSelected=").append("false").append(";");
        html.append("</script>");
        html.append("<div id=\"colourcomparator\" style=\"display:none;\"></div>");
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        this.setUp();
        this.renderSriptAndStyle(html, this.viewOnly);
        this.renderSpreadsheet(html, this.noteData, this.width, this.height);
        return html.toString();
    }
}

