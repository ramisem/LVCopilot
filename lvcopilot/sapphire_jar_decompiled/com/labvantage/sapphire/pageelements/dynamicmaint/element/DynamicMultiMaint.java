/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.element;

import com.labvantage.sapphire.pageelements.dynamicmaint.util.ColumnManager;
import com.labvantage.sapphire.pageelements.dynamicmaint.util.GridHandler;
import com.labvantage.sapphire.pageelements.dynamicmaint.util.Utils;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DynamicMultiMaint
extends BaseElement {
    @Override
    public String getHtml() {
        StringBuilder html = new StringBuilder();
        StringBuilder scriptElements = new StringBuilder();
        PropertyListCollection extraFieldCollection = new PropertyListCollection();
        PropertyListCollection dropDownSqls = new PropertyListCollection();
        try {
            DataSet detailTableLinks;
            PropertyList requestData = (PropertyList)JstlUtil.evaluateExpression("${requestdata}", this.pageContext);
            PropertyList elementProps = requestData.getPropertyList(this.elementid);
            PropertyList detailCollection = elementProps.getPropertyList("detailcollection");
            String detailCollectionItem = detailCollection.getProperty("detailcollectionitem", "(none)");
            String detailLink = detailCollection.getProperty("detaillink");
            String fixedcolumns = elementProps.getProperty("fixedcolumns", "999");
            String sdcid = elementProps.getProperty("sdcid", "");
            if (sdcid.equals("")) {
                sdcid = requestData.getPropertyListNotNull("pagedata").getProperty("sdcid", "");
            }
            DataSet columnProperties = this.getSDCProcessor().getColumnData(sdcid);
            DataSet links = this.getSDCProcessor().getLinksData(sdcid);
            PropertyListCollection columns = elementProps.getCollectionNotNull("columns");
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList col = columns.getPropertyList(i);
                String dropDownSql = col.getProperty("dropdownsql", "");
                if (dropDownSql.equals("")) continue;
                String simplecolumnid = col.getProperty("columnid", "");
                if (simplecolumnid.trim().startsWith("(")) {
                    simplecolumnid = simplecolumnid.substring(simplecolumnid.lastIndexOf(41) + 1).trim();
                } else if (simplecolumnid.contains(".")) {
                    simplecolumnid = simplecolumnid.replaceAll("\\.", "_");
                }
                PropertyList dropdown = Utils.formatDropDownSql(dropDownSql, simplecolumnid, this.elementid);
                dropDownSqls.add(dropdown);
                col.setProperty("dropdownsql", dropdown.getProperty("sqlAndVariables"));
            }
            Map<String, PropertyListCollection> notifyColumns = Utils.extractNotifyColumns(columns);
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList extraField;
                PropertyList col = columns.getPropertyList(i);
                String columnid = col.getProperty("columnid", "");
                String columntype = col.getProperty("columntype", "");
                String title = col.getProperty("title", "").replaceAll("\"", "\\\\\"");
                String inputwidth = col.getProperty("inputwidth", "20");
                String reftypeid = col.getProperty("reftypeid", "");
                String linktarget = col.getProperty("linktarget", "blank");
                if (columnid.equals("keycolid1")) {
                    columnid = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
                }
                if (columnid.equals("keycolid2") && (columnid = this.getSDCProcessor().getProperty(sdcid, "keycolid2")) == null) {
                    columnid = "";
                }
                if (columnid.equals("keycolid3") && (columnid = this.getSDCProcessor().getProperty(sdcid, "keycolid3")) == null) {
                    columnid = "";
                }
                if (columnid.equals("desccol")) {
                    columnid = this.getSDCProcessor().getProperty(sdcid, "desccol");
                }
                boolean isExtraColumn = false;
                if (columnid.trim().startsWith("(")) {
                    extraField = new PropertyList();
                    extraField.setProperty("columnid", this.getSubstitutedValue(columnid));
                    extraField.setProperty("tableid", "primary");
                    extraFieldCollection.add(extraField);
                    columnid = columnid.substring(columnid.lastIndexOf(41) + 1).trim();
                    isExtraColumn = true;
                } else if (columnid.contains(".")) {
                    extraField = new PropertyList();
                    extraField.setProperty("columnid", columnid);
                    extraField.setProperty("tableid", "primary");
                    extraFieldCollection.add(extraField);
                    columnid = columnid.replaceAll("\\.", "_");
                    isExtraColumn = true;
                }
                String pkflag = "";
                String datatype = "";
                String maxlength = "";
                String columndesc = "";
                String columnlinktype = "";
                String linkSdcid = "";
                String linkReftypeid = "";
                if (!isExtraColumn) {
                    for (int j = 0; j < columnProperties.getRowCount(); ++j) {
                        if (!columnid.equals(columnProperties.getString(j, "columnid"))) continue;
                        pkflag = columnProperties.getString(j, "pkflag", "");
                        datatype = columnProperties.getString(j, "datatype", "");
                        maxlength = columnProperties.getValue(j, "columnlength", "");
                        columndesc = columnProperties.getString(j, "columnlabel", "");
                        if (!datatype.equals("N") && !datatype.equals("R")) break;
                        maxlength = "20";
                        break;
                    }
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("sdccolumnid", columnid);
                    DataSet link = links.getFilteredDataSet(filter);
                    if (link.getRowCount() == 1) {
                        linkSdcid = link.getString(0, "linksdcid", "");
                        linkReftypeid = link.getString(0, "reftypeid", "");
                        columnlinktype = link.getString(0, "linktype", "");
                    }
                }
                if (linkSdcid.equals("RefType") && !columnlinktype.equals("F")) {
                    linkSdcid = "";
                }
                if (title.equals("")) {
                    title = columndesc;
                }
                if (datatype.equals("D") && !columntype.equals("hidden") && !columntype.equals("readonly")) {
                    columntype = "datelookup";
                }
                if (!linkSdcid.equals("") && (columntype.equals("input") || columntype.equals("datelookup") || columntype.equals("textarea"))) {
                    columntype = "lookup";
                }
                if (!linkReftypeid.equals("") && (columntype.equals("input") || columntype.equals("datelookup") || columntype.equals("textarea"))) {
                    columntype = "dropdownlist";
                }
                if (columntype.equals("dropdowncombo") && (columnlinktype.equals("V") || columnlinktype.equals("F"))) {
                    columntype = "dropdownlist";
                }
                if (reftypeid.equals("") && !linkReftypeid.equals("")) {
                    reftypeid = linkReftypeid;
                }
                col.setProperty("columnid", columnid);
                col.setProperty("columntype", columntype);
                col.setProperty("pkflag", pkflag);
                col.setProperty("maxlength", maxlength);
                col.setProperty("reftypeid", reftypeid);
                col.setProperty("title", title);
                if (notifyColumns.containsKey(columnid)) {
                    col.setProperty("notifycolumns", notifyColumns.get(columnid));
                }
                col.setProperty("inputsize", inputwidth);
                col.setProperty("linktarget", linktarget);
                if (scriptElements.length() > 0) {
                    scriptElements.append(",");
                }
                scriptElements.append(col.toJSONObject().toString());
                if (columntype.equals("lookup")) {
                    String lookupsdcid = col.getPropertyList("lookup").getProperty("lookupsdcid", "");
                    String extendedwhere = col.getPropertyList("lookup").getProperty("extendedwhere", "");
                    PropertyListCollection lookupColumnCollection = col.getPropertyList("lookup").getCollectionNotNull("lookupcolumns");
                    ColumnManager columnManager = new ColumnManager(this.getTranslationProcessor(), this.getSDCProcessor());
                    columnManager.setFormPrefix("mm");
                    String lookupScript = columnManager.getLookupScript(lookupsdcid, columnid, extendedwhere, lookupColumnCollection);
                    html.append(lookupScript);
                }
                if (!reftypeid.equals("")) {
                    DataSet reftypeDataset = this.getQueryProcessor().getRefTypeDataSet(reftypeid);
                    String refvalues = "";
                    String refdisplayvalues = "";
                    if (reftypeDataset != null) {
                        refvalues = reftypeDataset.getColumnValues("refvalueid", "','");
                        if (col.getProperty("translate").equals("Y")) {
                            Utils.translateRefDisplayValues(reftypeDataset, this.getTranslationProcessor(), this.getConnectionProcessor().getLanguage(), "RefType");
                        }
                        refdisplayvalues = reftypeDataset.getColumnValues("refdisplayvalue", "','");
                    }
                    String reftypeContents = "dd_dropdownvalues['" + reftypeid + "']=['" + refvalues + "'];refdisplayvalues['" + reftypeid + "']=['" + refdisplayvalues + "'];";
                    html.append("<script>").append(reftypeContents).append("</script>");
                }
                if (!linkSdcid.equals("") && columntype.equals("dropdownlist") && col.getProperty("dropdownsql", "").equals("")) {
                    String primaryKey = this.getSDCProcessor().getProperty(linkSdcid, "keycolid1");
                    String table = this.getSDCProcessor().getProperty(linkSdcid, "tableid");
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid(linkSdcid);
                    sdiRequest.setQueryFrom(table);
                    sdiRequest.setRequestItem("primary");
                    sdiRequest.setRetainRsetid(false);
                    SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                    DataSet reftypeDataset = new DataSet();
                    if (sdiData != null) {
                        reftypeDataset = sdiData.getDataset("primary");
                    }
                    String refvalues = "";
                    String refdisplayvalues = "";
                    if (reftypeDataset != null) {
                        refvalues = reftypeDataset.getColumnValues(primaryKey, "','");
                        refdisplayvalues = reftypeDataset.getColumnValues(primaryKey, "','");
                    }
                    String reftypeContents = " dd_dropdownvalues['SDC_" + linkSdcid + "']=['" + refvalues + "'];refdisplayvalues['SDC_" + linkSdcid + "']=['" + refdisplayvalues + "'];";
                    html.append("<script>").append(reftypeContents).append("</script>");
                    col.setProperty("reftypeid", "SDC_" + linkSdcid);
                }
                PropertyList dropDownDefinition = col.getPropertyListNotNull("dropdowndefinition");
                if (!columntype.equals("dropdownlist") && !columntype.equals("dropdowncombo") || dropDownDefinition.size() <= 0) continue;
                String querySdc = dropDownDefinition.getProperty("sdcid", linkSdcid);
                String queryFrom = dropDownDefinition.getProperty("queryfrom");
                String queryWhere = dropDownDefinition.getProperty("querywhere");
                String orderBy = dropDownDefinition.getProperty("queryorderby");
                String valueCol = dropDownDefinition.getProperty("valuecolumn");
                String displayCol = dropDownDefinition.getProperty("displaycolumn");
                html.append(this.getSDCDropdownValues(columnid, querySdc, queryFrom, queryWhere, orderBy, valueCol, displayCol));
                col.setProperty("reftypeid", columnid);
            }
            html.append("<div id='").append(this.elementid).append("'>");
            html.append("<div id='").append(this.elementid).append("_waitdiv'><img src='WEB-OPAL/images/wait.gif'/>Loading, please wait...</div>");
            html.append("<div id='").append(this.elementid).append("_hidden' style='display:none'>");
            html.append(this.getTranslationProcessor().translate("Click the tab to show more information."));
            html.append("</div>");
            html.append("</div>\n");
            String secondarySDC = "";
            String detailSDC = "";
            String detailTable = detailCollectionItem;
            String detailKeyColumns = "";
            String detailKeyColumn1 = "";
            if (detailCollectionItem.equals("sdidata")) {
                secondarySDC = "ParamList";
                detailSDC = "DataSet";
                detailKeyColumns = "paramlistid;paramlistversionid;variantid";
                detailKeyColumn1 = "paramlistid";
            } else if (detailCollectionItem.equals("sdiworkitem")) {
                secondarySDC = "WorkItem";
                detailSDC = "SDIWorkItem";
                detailKeyColumns = "workitemid;workitemversionid";
                detailKeyColumn1 = "workitemid";
            } else if (detailCollectionItem.equals("sdispec")) {
                secondarySDC = "SpecSDC";
                detailSDC = "SDISpec";
                detailKeyColumns = "specid;specversionid";
                detailKeyColumn1 = "specid";
            } else if (detailCollectionItem.equals("category")) {
                secondarySDC = "Category";
                detailTable = "categoryitem";
                detailKeyColumns = "categoryid";
                detailKeyColumn1 = "categoryid";
            } else if (detailCollectionItem.equals("sdirole")) {
                secondarySDC = "Role";
                detailKeyColumns = "roleid";
                detailKeyColumn1 = "roleid";
            } else if (detailCollectionItem.equals("sdidataitem")) {
                detailSDC = "DataItem";
                detailKeyColumns = "paramlistid;paramlistversionid;variantid;dataset;paramid;paramtype;replicateid";
            } else if (!detailLink.equals("")) {
                String sql = "SELECT linksdcid, linktableid FROM sdclink WHERE sdcid=? AND linkid=?";
                Object[] params = new String[]{sdcid, detailLink};
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
                secondarySDC = ds.getString(0, "linksdcid", "");
                detailTable = ds.getString(0, "linktableid", "");
                HashMap detailSdcProps = this.getSDCProcessor().getSDCProperties(secondarySDC);
                detailKeyColumns = (String)detailSdcProps.get("keycolid1");
                if (detailSdcProps.get("keycolid2") != null) {
                    detailKeyColumns = detailKeyColumns + ";" + detailSdcProps.get("keycolid2");
                }
            } else if (detailCollectionItem.startsWith("propertylist_")) {
                detailKeyColumns = elementProps.getProperty("keycolumns");
                detailKeyColumn1 = elementProps.getProperty("keycolumns").split(";")[0];
            }
            if (detailCollection.getProperty("detailheader", "").equals("")) {
                detailCollection.setProperty("detailheader", "[" + detailKeyColumns.replaceAll(";", "] [") + "]");
            }
            String newitemlookuppage = "";
            if (!detailCollectionItem.equals("(none)") || !detailLink.equals("")) {
                PropertyList lookupColumn;
                String lookupsdcid = detailCollection.getPropertyList("detaillookup").getProperty("lookupsdcid", secondarySDC);
                newitemlookuppage = detailCollection.getPropertyList("detaillookup").getProperty("lookuppage", "");
                String extendedwhere = detailCollection.getPropertyList("detaillookup").getProperty("extendedwhere", "");
                PropertyListCollection lookupColumnCollection = detailCollection.getPropertyList("detaillookup").getCollectionNotNull("lookupcolumns");
                ColumnManager columnManager = new ColumnManager(this.getTranslationProcessor(), this.getSDCProcessor());
                columnManager.setFormPrefix(this.elementid);
                if (detailCollectionItem.equals("sdiworkitem") && lookupColumnCollection.size() > 0 && (lookupColumn = lookupColumnCollection.getPropertyList(0)).getProperty("maptocolumn", "").equals("sdiworkitemid")) {
                    lookupColumn.setProperty("maptocolumn", "workitemid");
                }
                String lookupScript = detailLink.equals("") ? columnManager.getLookupScript(lookupsdcid, detailKeyColumn1, extendedwhere, lookupColumnCollection) : columnManager.getLookupScript(lookupsdcid, this.elementid + "m2mid", extendedwhere, lookupColumnCollection);
                html.append(lookupScript);
            }
            if (!detailSDC.equals("")) {
                detailTableLinks = this.getSDCProcessor().getLinksData(detailSDC);
            } else {
                String sql = "select * from sdclink where sdcid=? and linkid=?";
                Object[] params = new String[]{sdcid, detailLink};
                detailTableLinks = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
                if (detailTableLinks.getRowCount() > 0) {
                    detailSDC = detailTableLinks.getString(0, "linksdcid");
                }
            }
            html.append(this.generateDetailCellConfig(detailCollection.getCollectionNotNull("detailcolumns"), detailTable, detailTableLinks));
            String jsFunc = this.elementid + "JS";
            PropertyListCollection simpleColumnConfig = Utils.getColumnConfig(elementProps.getCollectionNotNull("columns"), "primary");
            String pageId = requestData.getProperty("page");
            HttpSession session = this.pageContext.getSession();
            PropertyList pageConfig = (PropertyList)session.getAttribute("DYM_" + pageId);
            if (pageConfig == null) {
                pageConfig = new PropertyList();
            }
            PropertyList cachedElementProps = new PropertyList();
            cachedElementProps.setProperty("type", "multimaint");
            cachedElementProps.setProperty("extrafields", extraFieldCollection);
            cachedElementProps.setProperty("dropdownsqls", dropDownSqls);
            cachedElementProps.setProperty("detailsdcid", detailSDC);
            cachedElementProps.setProperty("detailkeycolumns", detailKeyColumns);
            cachedElementProps.setProperty("detailcollection", detailCollection);
            if (!detailLink.equals("")) {
                cachedElementProps.setProperty("linktype", "sdclink:" + detailLink);
            }
            cachedElementProps.setProperty("columns", simpleColumnConfig);
            pageConfig.setProperty(this.elementid, cachedElementProps);
            session.setAttribute("DYM_" + pageId, (Object)pageConfig);
            html.append("<script>\n");
            elementProps.setProperty("type", "multimaint");
            elementProps.setProperty("detailsdcid", detailSDC);
            elementProps.setProperty("detailkeycolumns", detailKeyColumns);
            if (!detailLink.equals("")) {
                elementProps.setProperty("linktype", "sdclink:" + detailLink);
            }
            elementProps.setProperty("fixedcolumns", fixedcolumns);
            elementProps.setProperty("jsfunc", jsFunc);
            html.append("dynamicmaint.addFormConfig('").append(this.elementid).append("',").append(elementProps.toJSONObject().toString()).append(");\n");
            html.append("var ").append(jsFunc).append(" = new DYM.MultiMaint('").append(jsFunc).append("', '").append(this.elementid).append("')");
            html.append("</script>\n");
            html.append(new GridHandler(this.elementid, jsFunc, this.getTranslationProcessor(), false, this.browser.isIE()).getGridHandler(null));
        }
        catch (Exception e) {
            this.logger.error("Error generating element", e);
            html.append("<span style='color:red'>" + this.getTranslationProcessor().translate("Error generating element. See the log for more information.") + "</span>");
        }
        return html.toString();
    }

    private String generateDetailCellConfig(PropertyListCollection detailColumns, String detailTable, DataSet links) {
        String retVal = "";
        DataSet detailSdcProps = this.getSDCProcessor().getTableColumnData(detailTable);
        for (int i = 0; i < detailColumns.size(); ++i) {
            PropertyList col = detailColumns.getPropertyList(i);
            for (int j = 0; j < detailSdcProps.getRowCount(); ++j) {
                String columnid = col.getProperty("columnid");
                if (!columnid.equals(detailSdcProps.getString(j, "columnid"))) continue;
                String pkflag = detailSdcProps.getString(j, "pkflag", "");
                String datatype = detailSdcProps.getString(j, "datatype", "");
                String maxlength = detailSdcProps.getValue(j, "columnlength", "0");
                String columndesc = detailSdcProps.getString(j, "columnlabel", "");
                String editorStyle = col.getProperty("editorstyle", "");
                String linkReftypeid = "";
                String linkSdcid = "";
                String columnLinkType = "";
                for (int k = 0; k < links.getRowCount(); ++k) {
                    if (!links.getString(k, "sdccolumnid", "").equals(columnid)) continue;
                    linkReftypeid = links.getString(k, "reftypeid", "");
                    linkSdcid = links.getString(k, "linksdcid", "");
                    columnLinkType = links.getString(k, "linktype", "");
                    if (!linkSdcid.equals("") && linkReftypeid.equals("")) {
                        PropertyList lookupProps = col.getPropertyListNotNull("lookup");
                        lookupProps.setProperty("lookupsdcid", linkSdcid);
                        col.setProperty("lookup", lookupProps);
                        continue;
                    }
                    if (linkReftypeid.equals("") || !linkSdcid.equals("RefType")) continue;
                    linkSdcid = "";
                }
                if (!editorStyle.equals("")) {
                    Utils.getEditorStyleConfig(editorStyle, col, this.getQueryProcessor(), this.logger);
                    if (linkReftypeid.equals("")) {
                        linkReftypeid = col.getProperty("reftypeid", "");
                    } else if (columnLinkType.equals("V")) {
                        col.setProperty("iseditable", "N");
                    }
                    if (linkSdcid.equals("")) {
                        linkSdcid = col.getPropertyListNotNull("lookup").getProperty("lookupsdcid", "");
                    } else {
                        col.setProperty("iseditable", "N");
                    }
                }
                String columntype = col.getProperty("columntype", "readonly");
                String title = col.getProperty("title", columndesc);
                if (datatype.equals("N") || datatype.equals("R")) {
                    maxlength = "20";
                }
                if (datatype.equals("D") && !columntype.equals("hidden") && !columntype.equals("readonly")) {
                    columntype = "datelookup";
                }
                if (!linkSdcid.equals("") && (columntype.equals("input") || columntype.equals("datelookup") || columntype.equals("textarea"))) {
                    columntype = "lookup";
                }
                if (!linkReftypeid.equals("") && (columntype.equals("input") || columntype.equals("datelookup") || columntype.equals("textarea"))) {
                    columntype = "dropdownlist";
                }
                if (columntype.equals("dropdowncombo") && (columnLinkType.equals("V") || columnLinkType.equals("F"))) {
                    columntype = "dropdownlist";
                }
                col.setProperty("columntype", columntype);
                col.setProperty("maxlength", maxlength);
                col.setProperty("inputsize", col.getProperty("inputwidth"));
                col.setProperty("title", title);
                if (col.getProperty("columntype").equals("lookup")) {
                    String restrictivewhere = col.getPropertyListNotNull("lookup").getProperty("extendedwhere", "");
                    String selectorType = col.getPropertyListNotNull("lookup").getProperty("selectortype", "");
                    PropertyListCollection lookupColumns = col.getPropertyListNotNull("lookup").getCollectionNotNull("lookupcolumns");
                    ColumnManager columnManager = new ColumnManager(this.getTranslationProcessor(), this.getSDCProcessor());
                    columnManager.setFormPrefix(detailTable);
                    retVal = columnManager.getLookupScript(linkSdcid, columnid, restrictivewhere, lookupColumns, selectorType, false);
                }
                if (!col.getProperty("reftypeid", "").equals("")) {
                    linkReftypeid = col.getProperty("reftypeid", "");
                }
                if (!linkReftypeid.equals("") && columntype.equals("dropdownlist")) {
                    DataSet reftypeDataset = this.getQueryProcessor().getRefTypeDataSet(linkReftypeid);
                    String refvalues = "";
                    String refdisplayvalues = "";
                    if (reftypeDataset != null) {
                        refvalues = reftypeDataset.getColumnValues("refvalueid", "','");
                        refdisplayvalues = reftypeDataset.getColumnValues("refvaluedesc", "','");
                    }
                    String reftypeContents = "dd_dropdownvalues['" + linkReftypeid + "']=['" + refvalues + "'];refdisplayvalues['" + linkReftypeid + "']=['" + refdisplayvalues + "'];";
                    retVal = "<script>" + reftypeContents + "</script>";
                    col.setProperty("reftypeid", linkReftypeid);
                }
                if (linkSdcid.equals("") || !columntype.equals("dropdownlist") && !columntype.equals("dropdowncombo")) continue;
                String queryFrom = col.getProperty("queryfrom");
                String queryWhere = col.getProperty("querywhere");
                String orderBy = col.getProperty("queryorderby");
                String valueCol = col.getProperty("valuecolumn");
                String displayCol = col.getProperty("displaycolumn");
                retVal = this.getSDCDropdownValues("SDC_" + linkSdcid, linkSdcid, queryFrom, queryWhere, orderBy, valueCol, displayCol);
                col.setProperty("reftypeid", "SDC_" + linkSdcid);
            }
        }
        return retVal;
    }

    private String getSDCDropdownValues(String dropdownId, String sdcId, String queryFrom, String queryWhere, String orderBy, String valueCol, String displayCol) {
        DataSet ds;
        String primaryKey = this.getSDCProcessor().getProperty(sdcId, "keycolid1");
        String table = this.getSDCProcessor().getProperty(sdcId, "tableid");
        if (valueCol.equals("")) {
            valueCol = primaryKey;
        }
        if (displayCol.equals("")) {
            displayCol = primaryKey;
        }
        if (queryFrom.equals("")) {
            queryFrom = table;
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setRequestItem("primary");
        sdiRequest.setSDCid(sdcId);
        sdiRequest.setQueryFrom(queryFrom);
        sdiRequest.setQueryWhere(queryWhere);
        sdiRequest.setQueryOrderBy(orderBy);
        SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
        String refvalues = "";
        String refdisplayvalues = "";
        if (sdiData != null && (ds = sdiData.getDataset("primary")) != null) {
            refvalues = ds.getColumnValues(valueCol, "','");
            refdisplayvalues = ds.getColumnValues(displayCol, "','");
        }
        String reftypeContents = " dd_dropdownvalues['" + dropdownId + "']=['" + refvalues + "'];refdisplayvalues['" + dropdownId + "']=['" + refdisplayvalues + "'];";
        String retVal = "<script>" + reftypeContents + "</script>";
        return retVal;
    }

    private String getSubstitutedValue(String columnid) {
        String[] tokens = StringUtil.getTokens(columnid, "[", "]", false);
        if (tokens.length > 0) {
            for (String token : tokens) {
                String replaceWith = this.getSubstitution(token);
                if (replaceWith == null || replaceWith.length() <= 0) continue;
                columnid = StringUtil.replaceAll(columnid, "[" + token + "]", replaceWith);
            }
        }
        return columnid;
    }

    protected String getSubstitution(String token) {
        return this.requestContext.getProperty(token);
    }
}

