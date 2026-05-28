/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.dataentry;

import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataEntryColumn
extends BaseElement {
    private String[] keyCols;
    private String id = "";
    private PropertyList dataEntryColumn;
    private PropertyList commonAttributes;
    private PropertyList colors;
    private QueryData queryData;
    private DataSet dataitems;
    private DataSet dataspecs;
    private String layout = "list";
    private HashMap specFilter = new HashMap();
    private HashMap specFind = new HashMap();
    private String[] specCondition;
    private String[] specColor;
    private int gridrow = -1;
    private int gridcol = -1;

    public DataEntryColumn(PageContext pageContext, SDITagInfo sdiInfo, String connectionid) {
        this.pageContext = pageContext;
        this.sdiInfo = sdiInfo;
        this.setConnectionId(connectionid);
        this.queryData = sdiInfo.getQueryData("dataitem");
        this.dataitems = sdiInfo.getQueryData("dataitem").getQuerydata();
        this.dataspecs = sdiInfo.getQueryData("dataspec") != null && sdiInfo.getQueryData("dataspec").getQuerydata() != null ? sdiInfo.getQueryData("dataspec").getQuerydata() : null;
    }

    public void setKeyCols(String[] keyCols) {
        this.keyCols = keyCols;
    }

    public void setGridPos(int row, int col) {
        this.gridrow = row;
        this.gridcol = col;
    }

    public void setColumnProperties(PropertyList dataEntryColumn) {
        PropertyListCollection events;
        String[] conditionColors;
        this.dataEntryColumn = dataEntryColumn;
        this.colors = dataEntryColumn != null ? dataEntryColumn.getPropertyList("colors") : null;
        String[] stringArray = conditionColors = this.colors != null ? StringUtil.split(this.colors.getProperty("specification"), ";") : null;
        if (conditionColors != null && conditionColors.length > 0) {
            this.specCondition = new String[conditionColors.length];
            this.specColor = new String[conditionColors.length];
            for (int i = 0; i < conditionColors.length; ++i) {
                String[] split = StringUtil.split(conditionColors[i], "=");
                if (split.length == 0) {
                    this.specCondition[i] = "";
                    this.specColor[i] = "";
                    continue;
                }
                if (split.length == 1) {
                    this.specCondition[i] = "";
                    this.specColor[i] = split[0];
                    continue;
                }
                if (split.length != 2) continue;
                this.specCondition[i] = split[0];
                this.specColor[i] = split[1];
            }
        }
        this.commonAttributes = new PropertyList();
        this.commonAttributes.setProperty("data", "dataitem");
        this.commonAttributes.setProperty("mode", "data");
        this.commonAttributes.setProperty("nullvalue", "");
        PropertyListCollection propertyListCollection = events = dataEntryColumn != null ? dataEntryColumn.getCollection("events") : null;
        if (events != null) {
            for (int i = 0; i < events.size(); ++i) {
                PropertyList event = events.getPropertyList(i);
                this.commonAttributes.setProperty(event.getProperty("event"), event.getProperty("js"));
            }
        }
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        html.append(SDITagUtil.getRepeatedRowInputs("dataitem", this.keyCols, this.queryData, "", "", 1));
        PropertyList attributes = new PropertyList();
        attributes.putAll(this.commonAttributes);
        SDITagUtil.setIdentifierAttributes(attributes, this.sdiInfo);
        this.id = attributes.getProperty("name");
        StringBuffer style = new StringBuffer("");
        StringBuffer extraAttributes = new StringBuffer("");
        if (this.gridrow >= 0 && this.gridcol >= 0) {
            attributes.setProperty("gridrow", Integer.toString(this.gridrow));
            attributes.setProperty("gridcol", Integer.toString(this.gridcol));
        }
        int diRow = this.queryData.getCurrentRow();
        if (this.dataspecs != null) {
            this.specFilter.put("keyid1", this.dataitems.getString(diRow, "keyid1"));
            this.specFilter.put("keyid2", this.dataitems.getString(diRow, "keyid2"));
            this.specFilter.put("keyid3", this.dataitems.getString(diRow, "keyid3"));
            this.specFilter.put("paramlistid", this.dataitems.getString(diRow, "paramlistid"));
            this.specFilter.put("paramlistversionid", this.dataitems.getString(diRow, "paramlistversionid"));
            this.specFilter.put("variantid", this.dataitems.getString(diRow, "variantid"));
            this.specFilter.put("dataset", this.dataitems.getBigDecimal(diRow, "dataset"));
            this.specFilter.put("paramid", this.dataitems.getString(diRow, "paramid"));
            this.specFilter.put("paramtype", this.dataitems.getString(diRow, "paramtype"));
            this.specFilter.put("replicateid", this.dataitems.getBigDecimal(diRow, "replicateid"));
            DataSet specs = this.dataspecs.getFilteredDataSet(this.specFilter);
            if (specs != null && specs.size() > 0 && this.specCondition != null && this.specColor != null && this.specCondition.length == this.specColor.length) {
                boolean match = false;
                for (int i = 0; i < this.specCondition.length && !match; ++i) {
                    if (this.specCondition[i].length() > 0) {
                        this.specFind.put("condition", this.specCondition[i]);
                        if (specs.findRow(this.specFind) == -1) continue;
                        match = true;
                        style.append("color:" + this.specColor[i] + ";");
                        continue;
                    }
                    if (this.specColor[i].length() <= 0) continue;
                    match = true;
                    style.append("color:" + this.specColor[i] + ";");
                }
            }
        }
        extraAttributes.append(";released=" + this.queryData.getValue(diRow, "releasedflag", "N"));
        if (this.queryData.getValue(diRow, "releasedflag", "N").equals("Y")) {
            style.append("font-style: italic;");
            attributes.setProperty("readonly", "true");
            attributes.setProperty("released", "true");
        }
        String borderColor = "white";
        extraAttributes.append(";mandatory=" + this.queryData.getValue(diRow, "mandatoryflag", "N"));
        if (this.queryData.getValue(diRow, "mandatoryflag", "N").equals("Y")) {
            String backgroundColor = this.colors != null ? this.colors.getProperty("mandatory", "#FAFAD2") : "#FAFAD2";
            style.append("background-color: " + backgroundColor + ";");
            borderColor = backgroundColor;
        }
        if (this.queryData.getValue(diRow, "valuestatus", "").length() > 0) {
            String backgroundColor = this.colors != null ? this.colors.getProperty("error", "#FF0000") : "#FF0000";
            style.append("background-color: " + backgroundColor + "; border: 2px solid " + backgroundColor + ";");
            extraAttributes.append(";title=" + (this.dataEntryColumn.getProperty("translatevalue", "N").equals("N") ? this.queryData.getValue(diRow, "valuestatus", "") : this.getTranslationProcessor().translate(this.queryData.getValue(diRow, "valuestatus", ""))));
            borderColor = backgroundColor;
        }
        boolean isLocked = this.queryData.getValue(diRow, "__lockedby", "").length() > 0;
        String lockedImage = "";
        String lockedStyle = "";
        if (isLocked) {
            lockedImage = "<img src=\"WEB-CORE/elements/images/locked.gif\" title=\"" + this.getTranslationProcessor().translate("Locked by") + " " + this.queryData.getValue(diRow, "__lockedby", "") + "\"/>";
            lockedStyle = ";background-color:#dcdcdc;border:2px solid #dcdcdc";
        }
        extraAttributes.append(";locked=" + (isLocked ? "Y" : "N"));
        style.append("border: 2px solid " + borderColor + ";");
        String datatypes = this.queryData.getValue(diRow, "datatypes", "N");
        extraAttributes.append(";datatype=" + datatypes);
        attributes.setProperty("extraattributes", extraAttributes.toString());
        if ("Y".equals(this.element.getProperty("readonly")) || isLocked) {
            attributes.setProperty("mode", "text");
            attributes.setProperty("readonly", "true");
        }
        if (datatypes.equals("V") || datatypes.equals("R")) {
            attributes.setProperty("class", "dataentry_" + this.layout + "_cell_select");
            attributes.setProperty("style", style.toString() + lockedStyle);
            attributes.setProperty("reftypeid", this.queryData.getValue(diRow, "entryreftypeid", ""));
            attributes.setProperty("dataentrymode", "dropdowncombo");
            if (datatypes.equals("V")) {
                attributes.setProperty("dataentrymode", "dropdownlist");
                attributes.setProperty("translatevalue", this.dataEntryColumn.getProperty("translatevalue"));
            }
            html.append(lockedImage + SDITagUtil.getInstance(this.pageContext).getInputHtml(attributes, this.sdiInfo));
            SDITagUtil.getInstance(this.pageContext).collectDropDownComboInfo(attributes, this.sdiInfo, this.pageContext);
        } else if (datatypes.equals("D") || datatypes.equals("O") || datatypes.equals("S")) {
            attributes.setProperty("class", "dataentry_" + this.layout + "_cell_input");
            attributes.setProperty("style", style.toString() + ";text-align:left" + lockedStyle);
            attributes.setProperty("dataentrymode", datatypes.equals("D") || datatypes.equals("O") ? "datelookup" : "lookup");
            if (datatypes.equals("O")) {
                attributes.setProperty("format", "O");
            }
            attributes.setProperty("img", datatypes.equals("D") || datatypes.equals("O") ? "WEB-CORE/elements/images/lookup_date.gif" : "WEB-CORE/elements/images/lookup.gif");
            if (datatypes.equals("S")) {
                attributes.setProperty("readonly", "true");
                String entrysdcid = this.queryData.getValue(diRow, "entrysdcid", "");
                attributes.setProperty("sdcid", entrysdcid);
                String lookuppageid = ElementUtil.getSDCLookUpPage(entrysdcid, this.getQueryProcessor());
                if (lookuppageid != null && lookuppageid.length() > 0) {
                    attributes.setProperty("lookuppageid", lookuppageid);
                }
            }
            html.append(lockedImage + SDITagUtil.getInstance(this.pageContext).getInputHtml(attributes, this.sdiInfo));
        } else {
            if (datatypes.equals("NC")) {
                attributes.setProperty("readonly", "true");
                String backgroundColor = this.colors != null ? this.colors.getProperty("calculation", "lightgrey") : "lightgrey";
                style.append("background-color: " + backgroundColor + "; border: 2px solid " + backgroundColor + ";");
            }
            attributes.setProperty("class", "dataentry_" + this.layout + "_cell_input");
            attributes.setProperty("style", style.toString() + (datatypes.equals("T") ? ";text-align:left" : "") + lockedStyle);
            html.append(lockedImage + "<input " + SDITagUtil.getInputAttributes("text", attributes.getProperty("value"), attributes.getProperty("readonly").equals("true")) + SDITagUtil.getInputCommonAttributes(attributes) + "/>");
        }
        return html.toString();
    }
}

