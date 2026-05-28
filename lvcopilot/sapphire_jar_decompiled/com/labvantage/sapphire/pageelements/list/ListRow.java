/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.list;

import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.pageelements.list.List;
import com.labvantage.sapphire.pageelements.list.ListColumn;
import java.util.ArrayList;
import javax.servlet.jsp.PageContext;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.SDCProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ListRow
extends BaseElement {
    private SDCProcessor sdcProcessor;
    private String datasetName = "primary";
    private ArrayList returncolids;
    private int currentRow;
    private boolean hasGroupby = false;
    private List.ListMode listmode = List.ListMode.LIST;
    private String returnvalue;
    private String rowvalue;

    public ListRow(PageContext pageContext, SDITagInfo sdiInfo, SDCProcessor sdcProcessor) {
        this.pageContext = pageContext;
        this.sdiInfo = sdiInfo;
        this.sdcProcessor = sdcProcessor;
    }

    public void setListmode(List.ListMode listmode) {
        this.listmode = listmode;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public void setHasGroupby(boolean hasGroupby) {
        this.hasGroupby = hasGroupby;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer(this.prefix != null ? this.prefix : "");
        this.currentRow = this.sdiInfo.getCurrentRow(this.datasetName);
        if (this.currentRow == 0) {
            this.pageContext.setAttribute(this.element.getId() + "_columnidlist", (Object)new StringBuffer());
        }
        PropertyListCollection columns = this.element.getCollection("columns");
        String sdcid = this.element.getProperty("sdcid", "");
        PropertyList sdc = this.getSDCProperties(sdcid);
        this.returnvalue = this.getReturnValue(columns, sdcid, sdc);
        this.rowvalue = this.getRowValue(columns, sdcid, sdc);
        if (columns != null && columns.size() > 0) {
            String keyid1 = "";
            String keyid2 = "";
            String keyid3 = "";
            if (sdc != null) {
                String keycolid1 = sdc.getProperty("keycolid1");
                keyid1 = this.sdiInfo.getValue(this.datasetName, keycolid1);
                String keycolid2 = sdc.getProperty("keycolid2");
                keyid2 = keycolid2.length() > 0 ? this.sdiInfo.getValue(this.datasetName, keycolid2) : "";
                String keycolid3 = sdc.getProperty("keycolid3");
                keyid3 = keycolid3.length() > 0 ? this.sdiInfo.getValue(this.datasetName, keycolid3) : "";
            }
            ListColumn listColumn = new ListColumn(this.pageContext, this.sdiInfo);
            listColumn.setDatasetname(this.datasetName);
            listColumn.setElementProperties(this.element);
            listColumn.setListmode(this.listmode);
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < columns.size(); ++i) {
                if (columns.getPropertyList(i).getProperty("mode").equals("Do Not Retrieve")) continue;
                String columnid = columns.getPropertyList(i).getProperty("columnid");
                if (this.currentRow == 0) {
                    if (columnid.indexOf(" ") > 0) {
                        columnid = RequestParser.parseAlias(columnid);
                    }
                    ((StringBuffer)this.pageContext.getAttribute(this.element.getId() + "_columnidlist")).append(";").append(columnid);
                }
                if (i == 0 && !this.listmode.equals((Object)List.ListMode.LIST)) {
                    String selector = this.buildSelector(this.returnvalue, keyid1, keyid2, keyid3, this.rowvalue, false);
                    String eventid = this.returnvalue;
                    try {
                        rowObject.put("selector", selector.toString());
                        rowObject.put("eventid", eventid);
                        rowObject.put("primaryrow", this.sdiInfo.getRowId(this.datasetName));
                    }
                    catch (JSONException jSONException) {
                        // empty catch block
                    }
                }
                if (i == 0 && this.listmode.equals((Object)List.ListMode.LIST)) {
                    html.append(this.buildSelector(this.returnvalue, keyid1, keyid2, keyid3, this.rowvalue, this.listmode.equals((Object)List.ListMode.LIST)));
                }
                listColumn.setColumn(columns.getPropertyList(i));
                if (!this.listmode.equals((Object)List.ListMode.LIST)) {
                    try {
                        rowObject.put(columnid, new JSONObject(listColumn.getHtml()));
                    }
                    catch (JSONException jSONException) {}
                    continue;
                }
                html.append(listColumn.getHtml());
            }
            if (!this.listmode.equals((Object)List.ListMode.LIST)) {
                html.append(rowObject.toString());
            }
        }
        return html.toString();
    }

    public String getReturnvalue() {
        return this.returnvalue;
    }

    public String getRowvalue() {
        return this.rowvalue;
    }

    private PropertyList getSDCProperties(String sdcid) {
        if (sdcid.length() > 0) {
            PropertyList sdc = (PropertyList)this.pageContext.getAttribute(sdcid + "_props");
            if (sdc == null) {
                sdc = this.sdcProcessor.getPropertyList(sdcid);
                this.pageContext.setAttribute(sdcid + "_props", (Object)sdc);
            }
            if (sdc == null) {
                return null;
            }
            return sdc;
        }
        return null;
    }

    private String getReturnValue(PropertyListCollection columns, String sdcid, PropertyList sdc) {
        Object temp = this.pageContext.getAttribute("returncolids");
        if (temp == null) {
            this.returncolids = new ArrayList();
            StringBuffer returncolidlist = new StringBuffer();
            if (columns != null) {
                for (int i = 0; i < columns.size(); ++i) {
                    String orgcol;
                    String returnvalue = columns.getPropertyList(i).getProperty("returnvalue", "");
                    if (!returnvalue.equals("Y") || (orgcol = columns.getPropertyList(i).getProperty("columnid", "")).length() <= 0) continue;
                    String columnid = RequestParser.parseAlias(orgcol);
                    this.returncolids.add(columnid);
                    returncolidlist.append(";").append(columnid);
                }
            }
            if (this.returncolids.size() == 0) {
                if (sdcid.length() > 0) {
                    if (sdc == null) {
                        return "";
                    }
                    String keycolid1 = sdc.getProperty("keycolid1");
                    String keycolid2 = sdc.getProperty("keycolid2");
                    String keycolid3 = sdc.getProperty("keycolid3");
                    this.returncolids.add(keycolid1);
                    returncolidlist.append(";").append(keycolid1);
                    if (keycolid2.length() > 0) {
                        this.returncolids.add(keycolid2);
                        returncolidlist.append(";").append(keycolid2);
                    }
                    if (keycolid3.length() > 0) {
                        this.returncolids.add(keycolid3);
                        returncolidlist.append(";").append(keycolid3);
                    }
                } else if (columns != null && columns.size() > 0) {
                    String firstcolid = columns.getPropertyList(0).getProperty("columnid");
                    this.returncolids.add(firstcolid);
                    returncolidlist.append(";").append(firstcolid);
                }
            }
            if (returncolidlist.length() == 0) {
                returncolidlist.append(";");
            }
            this.pageContext.setAttribute("returncolids", (Object)this.returncolids);
            this.pageContext.setAttribute(this.element.getId() + "_returncolidlist", (Object)returncolidlist);
        } else {
            this.returncolids = (ArrayList)temp;
        }
        StringBuffer returnvalue = new StringBuffer();
        for (int i = 0; i < this.returncolids.size(); ++i) {
            if (i != 0) {
                returnvalue.append("|");
            }
            String s = this.sdiInfo.getValue(this.datasetName, (String)this.returncolids.get(i));
            s = StringUtil.replaceAll(s, "|", "%7C");
            returnvalue.append(s);
        }
        return returnvalue.toString();
    }

    private String getRowValue(PropertyListCollection columns, String sdcid, PropertyList sdc) {
        ArrayList<String> rowcolids;
        Object temp = this.pageContext.getAttribute("rowcolids");
        if (temp == null) {
            rowcolids = new ArrayList<String>();
            StringBuffer rowcolidlist = new StringBuffer();
            if (columns != null) {
                String keycol1 = "";
                String keycol2 = "";
                String keycol3 = "";
                if (sdcid.length() > 0 && sdc != null) {
                    keycol1 = sdc.getProperty("keycolid1", "");
                    keycol2 = sdc.getProperty("keycolid2", "");
                    keycol3 = sdc.getProperty("keycolid3", "");
                }
                ArrayList returncolids = this.returncolids != null ? this.returncolids : new ArrayList();
                for (int i = 0; i < columns.size(); ++i) {
                    String columnid;
                    String orgcol;
                    String returnvalue = columns.getPropertyList(i).getProperty("returnvalue", "");
                    if (returnvalue.equals("Y") || (orgcol = columns.getPropertyList(i).getProperty("columnid", "")).length() <= 0 || (columnid = RequestParser.parseAlias(orgcol)).length() <= 0 || columnid.equalsIgnoreCase(keycol1) || columnid.equalsIgnoreCase(keycol2) || columnid.equalsIgnoreCase(keycol3) || returncolids.contains(columnid)) continue;
                    rowcolids.add(columnid);
                    rowcolidlist.append(";").append(columnid);
                }
            }
            if (rowcolidlist.length() == 0) {
                rowcolidlist.append(";");
            }
            this.pageContext.setAttribute("rowcolids", rowcolids);
            this.pageContext.setAttribute(this.element.getId() + "_rowcolidlist", (Object)rowcolidlist);
        } else {
            rowcolids = (ArrayList<String>)temp;
        }
        StringBuffer rowvalue = new StringBuffer();
        for (int i = 0; i < rowcolids.size(); ++i) {
            String value;
            if (i != 0) {
                rowvalue.append("|");
            }
            if ((value = this.sdiInfo.getValue(this.datasetName, (String)rowcolids.get(i))) == null) {
                value = "";
            }
            value = StringUtil.replaceAll(value, "|", "%7C");
            rowvalue.append(value);
        }
        return rowvalue.toString();
    }

    private String buildSelector(String returnvalue, String keyid1, String keyid2, String keyid3, String rowvalue, boolean visible) {
        String styleClause;
        StringBuffer html = new StringBuffer();
        String selectorType = this.element.getProperty("selectortype");
        boolean isGroupDefined = this.sdiInfo.getQueryData(this.datasetName).isGroupDefined();
        String appearance = this.element.getProperty("appearance");
        appearance = appearance + (appearance.length() > 0 ? "_" : "");
        String rowid = this.sdiInfo.getRowId(this.datasetName);
        String prefix = SDIData.getDatasetCode(this.datasetName);
        returnvalue = StringUtil.replaceAll(returnvalue, "\"", "&quot;");
        returnvalue = StringUtil.replaceAll(returnvalue, ">", "&gt;");
        returnvalue = StringUtil.replaceAll(returnvalue, "<", "&lt;");
        rowvalue = StringUtil.replaceAll(rowvalue, "\"", "&quot;");
        rowvalue = StringUtil.replaceAll(rowvalue, ">", "&gt;");
        rowvalue = StringUtil.replaceAll(rowvalue, "<", "&lt;");
        String string = styleClause = isGroupDefined ? " style=\"margin-left:16px\"" : "";
        if (selectorType.equals("radiobutton")) {
            String checked = "";
            if (this.sdiInfo.getCurrentRow() == 0) {
                checked = " checked";
            }
            html.append("<td style=\"width:30px").append(visible ? "" : ";display:none").append("\" class=\"list_tablebodycell").append(" list_rowselector\" id=\"__").append(prefix).append(rowid).append("_sc\">");
            html.append("<input type=\"radio\" onclick=\"selectorclicked( this, event )\" name=\"selector\" id=\"").append(returnvalue).append("\" primaryrow=\"").append(rowid).append("\" ").append(styleClause).append(checked).append(" keyid1=\"").append(keyid1).append("\"  keyid2=\"").append(keyid2).append("\"  keyid3=\"").append(keyid3).append("\" rowvalue=\"").append(rowvalue).append("\">");
            if (visible) {
                html.append("</td>\n");
            }
        } else if (selectorType.equals("none")) {
            html.append("<td style=\"display:none\" class=\"list_tablebodycell").append("\" id=\"__").append(prefix).append(rowid).append("_sc\">");
            html.append("<input type=\"hidden\" id=\"").append(returnvalue).append("\" primaryrow=\"").append(rowid).append("\" keyid1=\"").append(keyid1).append("\"  keyid2=\"").append(keyid2).append("\"  keyid3=\"").append(keyid3).append("\" rowvalue=\"").append(rowvalue).append("\">");
            html.append("</td>\n");
            if (this.hasGroupby) {
                html.append("<td style=\"width:25px\">&nbsp;</td>");
            }
        } else {
            html.append("<td style=\"width:30px").append(visible ? "" : ";display:none").append("\" class=\"list_tablebodycell").append(" list_rowselector\" id=\"__").append(prefix).append(rowid).append("_sc\">");
            html.append("<input type=\"checkbox\" onclick=\"selectorclicked( this, event )\" name=\"selector\" id=\"").append(returnvalue).append("\" primaryrow=\"").append(rowid).append("\" ").append(styleClause).append(" " + this.element.getProperty("checkedclause")).append(" keyid1=\"").append(keyid1).append("\"  keyid2=\"").append(keyid2).append("\"  keyid3=\"").append(keyid3).append("\" rowvalue=\"").append(rowvalue).append("\">");
            if (visible) {
                html.append("</td>\n");
            }
        }
        return html.toString();
    }
}

