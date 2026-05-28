/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.dataentry;

import com.labvantage.opal.util.LegendHtmlGenerator;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.dataentry.DataEntryColumn;
import com.labvantage.sapphire.pageelements.list.ListColumn;
import com.labvantage.sapphire.pageelements.maint.MaintColumn;
import com.labvantage.sapphire.pageelements.maint.RegexConverter;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataEntryList
extends BaseElement {
    private ArrayList idGrid = new ArrayList();

    @Override
    public String getHtml() {
        this.logger.info("Initailizing data entry list from properties...");
        SDITagInfo sdiInfo = this.getSDIInfo();
        if (sdiInfo == null || sdiInfo.getQueryData("dataitem") == null || sdiInfo.getQueryData("dataitem").getQuerydata() == null) {
            return "SDIDataItem data not found. DataEntryList must be inside an SDI tag and include dataitem in the request attribute.";
        }
        boolean showDataitem = (this.element != null ? this.element.getProperty("showdataitem", "Y") : "Y").equals("Y");
        boolean showPrimary = (this.element != null ? this.element.getProperty("showprimary", "Y") : "Y").equals("Y");
        boolean primaryLoaded = sdiInfo.getQueryData("primary") != null && sdiInfo.getQueryData("primary").getQuerydata() != null;
        boolean showCheckBox = (this.element != null ? this.element.getProperty("showcheckbox", "Y") : "Y").equals("Y");
        boolean viewOnly = (this.element != null ? this.element.getProperty("readonly", "N") : "N").equals("Y");
        if (showPrimary && !primaryLoaded) {
            return "Primary data not found. DataEntryList must be inside an SDI tag and include primary in the request attribute.";
        }
        boolean showDataitemHeader = (this.element != null ? this.element.getProperty("showdataitemheader", "Y") : "Y").equals("Y") && showDataitem;
        boolean showDatasetHeader = (this.element != null ? this.element.getProperty("showdatasetheader", "Y") : "Y").equals("Y");
        boolean datasetsLoaded = sdiInfo.getQueryData("dataset") != null && sdiInfo.getQueryData("dataset").getQuerydata() != null;
        PropertyListCollection primaryColumns = this.getPrimaryColumns();
        PropertyListCollection datasetColumns = this.getDatasetColumns();
        PropertyListCollection dataitemColumns = this.getDataitemColumns();
        PropertyList dataEntryColumn = this.element != null ? this.element.getPropertyList("dataentrycolumn") : null;
        QueryData prQueryData = sdiInfo.getQueryData("primary");
        DataSet primary = primaryLoaded ? prQueryData.getQuerydata() : null;
        QueryData dsQueryData = sdiInfo.getQueryData("dataset");
        DataSet datasets = datasetsLoaded ? dsQueryData.getQuerydata() : null;
        QueryData diQueryData = sdiInfo.getQueryData("dataitem");
        DataSet dataitems = diQueryData.getQuerydata();
        I18nUtil.localizeDisplayValues(dataitems, this.pageContext);
        dataitems.sort(sdiInfo.getSDIRequest().getPropsMatch() ? "__rsetseq, usersequence, paramid, paramtype, replicateid " : "keyid1, __sdidata_usersequence, paramlistid, paramlistversionid, variantid, dataset, usersequence, paramid, paramtype, replicateid");
        StringBuffer html = new StringBuffer();
        if (datasetsLoaded) {
            html.append(SDITagUtil.getFixedRowInputs("dataset", datasets.getColumns(), datasets.size(), ""));
        }
        html.append(SDITagUtil.getFixedRowInputs("dataitem", dataitems.getColumns(), dataitems.size(), ""));
        String currentKeyid = "";
        String currentDataset = "";
        int primaryRow = 0;
        int datasetRow = 0;
        HashMap<String, String> findPrimary = new HashMap<String, String>();
        HashMap<String, Object> findDataset = new HashMap<String, Object>();
        String[] keyidCols = sdiInfo.getKeycols();
        boolean newPrimary = false;
        boolean newDataset = false;
        boolean primaryDivPending = false;
        boolean datasetDivPending = false;
        boolean datasetHeaderRendered = false;
        boolean dataitemTablePending = false;
        boolean dataColumnPending = false;
        int dataitemCount = 0;
        String entryColumnAfter = dataEntryColumn != null ? dataEntryColumn.getProperty("positionafter", "replicateid") : "replicateid";
        String dataEntryTitle = dataEntryColumn != null ? dataEntryColumn.getProperty("title", "&nbsp;") : "&nbsp;";
        ListColumn listColumn = new ListColumn(this.pageContext, sdiInfo);
        listColumn.setElementProperties(this.element);
        DataEntryColumn dataColumn = new DataEntryColumn(this.pageContext, sdiInfo, this.getConnectionId());
        dataColumn.setElementProperties(this.element);
        dataColumn.setColumnProperties(dataEntryColumn);
        SDIData sdiData = new SDIData();
        dataColumn.setKeyCols(sdiData.getKeys("dataitem"));
        dataColumn.setLayout("list");
        ElementUtil.setSdcPropertyCache(this.pageContext, this.getConnectionId(), "DataSet", "datasetsdc");
        MaintColumn datasetMaintColumn = new MaintColumn(this.pageContext, sdiInfo, this.getConnectionId());
        datasetMaintColumn.setElementProperties(this.element);
        datasetMaintColumn.setSdcPropertyList((PropertyList)this.pageContext.getAttribute("datasetsdc"));
        datasetMaintColumn.setDatasetname("dataset");
        ElementUtil.setSdcPropertyCache(this.pageContext, this.getConnectionId(), "DataItem", "dataitemsdc");
        MaintColumn dataitemMaintColumn = new MaintColumn(this.pageContext, sdiInfo, this.getConnectionId());
        dataitemMaintColumn.setElementProperties(this.element);
        dataitemMaintColumn.setSdcPropertyList((PropertyList)this.pageContext.getAttribute("dataitemsdc"));
        dataitemMaintColumn.setDatasetname("dataitem");
        this.logger.info("Generating HTML for " + dataitems.size() + " dataitems...");
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/dataentry.js\"></script>\n");
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/scripts/grid.js\"></script>\n");
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/lookup.js\"></script>\n");
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/maint.js\"></script>\n");
        String viewSelected = this.requestContext.getProperty("view");
        if (viewSelected == null || viewSelected.trim().length() == 0) {
            viewSelected = "singlesdi";
        }
        String expandCollapseText = "multisdi".equals(viewSelected) ? "Collapse All " + sdiInfo.getSdcid() + "s" : ("singlesdi".equals(viewSelected) ? "Collapse All DataSets" : "Collapse All " + sdiInfo.getSdcid() + "s");
        html.append("<table border=\"0\"><tr> <td width=\"600\"> </td>");
        html.append("<td allign=\"left\" width=\"12\"><img onclick=\"expandCollapse()\" id=\"ExpandCollapseAll_Img\" title=\"Collapse\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"WEB-CORE/elements/images/minus.gif\"></td>");
        html.append("<td allign=\"left\" width=\"150\" id=\"ExpandCollapseAll_Text\" onclick=\"expandCollapse()\" nowrap align=\"left\">" + this.getTranslationProcessor().translate(expandCollapseText) + "</td>");
        html.append("<td align=\"right\" width = \"12\">" + LegendHtmlGenerator.getHtml("dataentrylist", this.pageContext) + "</td>");
        html.append("</tr></table>");
        html.append("<div style=\"position:absolute;z-index:1;width:*;height:*;overflow:auto\" id=\"dataentry_list_div\" >");
        html.append("<table border=\"0\" cellspacing=\"0\">");
        for (int i = 0; i < dataitems.size(); ++i) {
            int j;
            boolean isLocked = dataitems.getValue(i, "__lockedby") != null && dataitems.getValue(i, "__lockedby").length() > 0;
            String lockedby = "";
            String lockedImage = "";
            String lockedClass = "maint_lockedfield";
            if (isLocked) {
                isLocked = true;
                lockedby = datasets.getValue(datasetRow, "__lockedby");
                lockedImage = "<img src=\"WEB-CORE/elements/images/locked.gif\" title=\"" + this.getTranslationProcessor().translate("Locked by") + " " + lockedby + "\"/>";
            }
            if (!currentKeyid.equals(dataitems.getString(i, "keyid1"))) {
                currentKeyid = dataitems.getString(i, "keyid1");
                currentDataset = "";
                newPrimary = true;
                if (dataitemTablePending) {
                    dataitemTablePending = false;
                    html.append("</table></td></tr>");
                }
                if (datasetDivPending) {
                    datasetDivPending = false;
                    html.append("</table></div></td></tr>");
                }
                if (primaryDivPending) {
                    primaryDivPending = false;
                    html.append("</table></div></td></tr>");
                }
                if (showPrimary) {
                    for (j = 0; j < keyidCols.length; ++j) {
                        if (keyidCols[j].length() <= 0) continue;
                        findPrimary.put(keyidCols[j], dataitems.getString(i, "keyid" + (j + 1)));
                    }
                    primaryRow = primary.findRow(findPrimary, 0);
                    sdiInfo.getQueryData("primary").setCurrentRow(primaryRow);
                    this.logger.info("Primary row: " + currentKeyid);
                    html.append("<tr><td width=\"12\"><img id=\"" + currentKeyid + "\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"WEB-CORE/elements/images/minus.gif\"></td>");
                    if (showCheckBox) {
                        if (isLocked) {
                            html.append("<td>&nbsp;</td>");
                        } else {
                            html.append("<td width=\"12\"><input type=\"checkbox\" index=\"" + prQueryData.getRowId(i) + "\" name=\"primaryselector\" id=\"" + currentKeyid + "\" onclick=\"setallfor( '" + currentKeyid + "data', 'datasetselector', this.checked );setallfor( '" + currentKeyid + "data', 'dataitemselector', this.checked );\"/></td>");
                        }
                    }
                    html.append("<td width=\"*\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"dataentry_list_primarytable\"><tr class=\"dataentry_list_primaryrow\">");
                    listColumn.setDatasetname("primary");
                    listColumn.setRow(primaryRow);
                    for (j = 0; j < primaryColumns.size(); ++j) {
                        listColumn.setColumn(primaryColumns.getPropertyList(j));
                        html.append(listColumn.getHtml());
                    }
                    html.append("</tr></table></td></tr>");
                }
                html.append("<tr><td colspan=\"3\"><div id=\"" + currentKeyid + "data\">");
                html.append("<table border=\"0\" cellspacing=\"0\" class=\"dataentry_list_datasettable\">");
                primaryDivPending = true;
            }
            if (!currentDataset.equals(dataitems.getString(i, "paramlistid") + ";" + dataitems.getString(i, "paramlistversionid") + ";" + dataitems.getString(i, "variantid") + ";" + dataitems.getInt(i, "dataset"))) {
                int j2;
                currentDataset = dataitems.getString(i, "paramlistid") + ";" + dataitems.getString(i, "paramlistversionid") + ";" + dataitems.getString(i, "variantid") + ";" + dataitems.getInt(i, "dataset");
                if (datasetsLoaded) {
                    findDataset.put("keyid1", dataitems.getString(i, "keyid1"));
                    findDataset.put("keyid2", dataitems.getString(i, "keyid2"));
                    findDataset.put("keyid3", dataitems.getString(i, "keyid3"));
                    findDataset.put("paramlistid", dataitems.getString(i, "paramlistid"));
                    findDataset.put("paramlistversionid", dataitems.getString(i, "paramlistversionid"));
                    findDataset.put("variantid", dataitems.getString(i, "variantid"));
                    findDataset.put("dataset", dataitems.getBigDecimal(i, "dataset"));
                    datasetRow = datasets.findRow(findDataset, 0);
                }
                newDataset = true;
                if (dataitemTablePending) {
                    dataitemTablePending = false;
                    html.append("</table></td></tr>");
                }
                if (datasetDivPending) {
                    datasetDivPending = false;
                    html.append("</table></div></td></tr>");
                }
                if (!showDataitem) {
                    this.idGrid.add(new ArrayList());
                }
                boolean useDatasetValues = datasetsLoaded && datasetRow >= 0 && datasetRow < datasets.size();
                html.append("<tr><td>&nbsp;</td>");
                html.append(showDataitem ? "<td width=\"12\"><img id=\"" + currentKeyid + ";" + currentDataset + "\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"WEB-CORE/elements/images/minus.gif\"></td>" : "<td>&nbsp;</td>");
                if (!showDataitemHeader && showCheckBox) {
                    if (isLocked) {
                        html.append("<td>&nbsp;</td>");
                    } else {
                        html.append("<td width=\"12\" valign=\"bottom\"><input type=\"checkbox\" index=\"" + (useDatasetValues ? dsQueryData.getRowId(i) : diQueryData.getRowId(i)) + "\" name=\"datasetselector\" id=\"" + currentKeyid + ";" + currentDataset + "\" " + (showDataitem ? "onclick=\"setallfor( '" + currentKeyid + ";" + currentDataset + "data', 'dataitemselector', this.checked );\"" : "") + "/></td>");
                    }
                }
                html.append("<td width=\"*\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
                if (showDatasetHeader && newPrimary) {
                    if (!showDataitem) {
                        newPrimary = false;
                    }
                    if (!datasetHeaderRendered) {
                        if (isLocked) {
                            html.append("<tr class=\"dataentry_list_datasetheader\" style=\"background-color: gainsboro;\">");
                        } else {
                            html.append("<tr class=\"dataentry_list_datasetheader\">");
                        }
                        for (j2 = 0; j2 < datasetColumns.size(); ++j2) {
                            if ("hidden".equals(datasetColumns.getPropertyList(j2).getProperty("mode"))) continue;
                            String align = datasetColumns.getPropertyList(j2).getProperty("align");
                            String alignclause = align != null && align.length() > 0 ? " align=\"" + align + "\"" : "";
                            html.append("<td nowrap " + alignclause + ">" + datasetColumns.getPropertyList(j2).getProperty("title") + "</td>");
                        }
                        html.append("</tr>");
                    }
                    if (!showPrimary) {
                        datasetHeaderRendered = true;
                    }
                }
                if (isLocked) {
                    html.append("<tr class=\"dataentry_list_datasetrow\" style=\"background-color: gainsboro;\">");
                } else {
                    html.append("<tr class=\"dataentry_list_datasetrow\">");
                }
                if (useDatasetValues) {
                    dsQueryData.setCurrentRow(datasetRow);
                    listColumn.setDatasetname("dataset");
                    listColumn.setRow(datasetRow);
                    html.append(SDITagUtil.getRepeatedRowInputs("dataset", sdiData.getKeys("dataset"), dsQueryData, "", "", 1));
                    boolean showLockedImage = true;
                    for (int j3 = 0; j3 < datasetColumns.size(); ++j3) {
                        PropertyList columnprops = isLocked ? datasetColumns.getPropertyList(j3).copy() : datasetColumns.getPropertyList(j3);
                        String columnid = columnprops.getProperty("columnid");
                        if (isLocked) {
                            html.append(showLockedImage && isLocked ? lockedImage : "");
                            showLockedImage = false;
                        }
                        if ((viewOnly || isLocked) && !"hidden".equals(columnprops.getProperty("mode"))) {
                            columnprops.setProperty("mode", "readonly");
                        }
                        if (columnprops.getProperty("mode").equals("readonly") || columnprops.getProperty("pseudocolumn").length() > 0 || columnid.equals("keyid1") || columnid.equals("paramlistid") || columnid.equals("paramlistversionid") || columnid.equals("variantid")) {
                            listColumn.setColumn(columnprops);
                            html.append(listColumn.getHtml());
                            continue;
                        }
                        datasetMaintColumn.setColumn(columnprops);
                        datasetMaintColumn.setColumnProperty("dataentry", "true");
                        datasetMaintColumn.setColumnProperty("width", columnprops.getProperty("width", "100"));
                        if (columnprops.getProperty("mode").equals("hidden")) {
                            html.append(datasetMaintColumn.getHtml());
                        } else if (isLocked) {
                            html.append("<td class=\"" + lockedClass + "\">" + datasetMaintColumn.getHtml() + "</td>\n");
                        } else {
                            html.append("<td class=\"dataentry_list_field\">" + datasetMaintColumn.getHtml() + "</td>\n");
                        }
                        if (showDataitem || columnprops.getProperty("mode").equals("hidden")) continue;
                        ((ArrayList)this.idGrid.get(this.idGrid.size() - 1)).add(datasetMaintColumn.getId());
                    }
                } else {
                    listColumn.setDatasetname("dataitem");
                    listColumn.setRow(i);
                    for (j2 = 0; j2 < datasetColumns.size(); ++j2) {
                        listColumn.setColumn(datasetColumns.getPropertyList(j2));
                        html.append(listColumn.getHtml());
                    }
                }
                html.append("</tr></table></td></tr>");
                if (showDataitem) {
                    html.append("<tr><td colspan=\"" + (showDataitemHeader ? "2" : "3") + "\"><div style=\"display: none\">&nbsp;</div></td>");
                    html.append("<td><div id=\"" + currentKeyid + ";" + currentDataset + "data\">");
                    html.append("<table border=\"0\" width=\"100%\" cellspacing=\"0\">");
                    datasetDivPending = true;
                }
            }
            if (!showDataitem) continue;
            diQueryData.setCurrentRow(i);
            listColumn.setDatasetname("dataitem");
            listColumn.setRow(i);
            this.idGrid.add(new ArrayList());
            if (newDataset) {
                dataitemCount = 0;
                newDataset = false;
                html.append("<tr><td>");
                html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"dataentry_list_dataitemtable\">");
                if (showDataitemHeader) {
                    html.append("<tr class=\"dataentry_list_dataitemheader\">");
                    if (showCheckBox) {
                        if (isLocked) {
                            html.append("<td>&nbsp;</td>");
                        } else {
                            html.append("<td><input type=\"checkbox\" index=\"" + diQueryData.getRowId(i) + "\" name=\"datasetselector\" id=\"" + currentKeyid + ";" + currentDataset + "\" onclick=\"setallfor( '" + currentKeyid + ";" + currentDataset + "data', 'dataitemselector', this.checked );\"/></td>");
                        }
                    }
                    dataColumnPending = true;
                    for (j = 0; j < dataitemColumns.size(); ++j) {
                        String alignclause;
                        String align = dataitemColumns.getPropertyList(j).getProperty("align");
                        String string = alignclause = align != null && align.length() > 0 ? " align=\"" + align + "\"" : "";
                        if (!dataitemColumns.getPropertyList(j).getProperty("mode").equals("hidden")) {
                            html.append("<td nowrap " + alignclause + ">" + dataitemColumns.getPropertyList(j).getProperty("title") + "</td>");
                        }
                        if (!entryColumnAfter.equals(dataitemColumns.getPropertyList(j).getProperty("columnid")) && (!entryColumnAfter.equals("last") || j != dataitemColumns.size() - 1)) continue;
                        html.append("<td>" + dataEntryTitle + "</td>");
                        dataColumnPending = false;
                    }
                    if (dataColumnPending) {
                        html.append("<td>" + dataEntryTitle + "</td>");
                    }
                    html.append("</tr>");
                }
            }
            html.append("<tr class=\"dataentry_list_dataitemrow" + (dataitemCount++ % 2 == 0 ? "even" : "odd") + "\">");
            if (showCheckBox) {
                if (isLocked) {
                    html.append("<td>&nbsp;</td>");
                } else {
                    html.append("<td><input type=\"checkbox\" index=\"" + diQueryData.getRowId(i) + "\" name=\"dataitemselector\" id=\"" + currentKeyid + ";" + currentDataset + ";" + dataitems.getValue(i, "paramid") + ";" + dataitems.getValue(i, "paramtype") + ";" + dataitems.getValue(i, "replicateid") + "\"/></td>");
                }
            }
            dataColumnPending = true;
            for (j = 0; j < dataitemColumns.size(); ++j) {
                PropertyList currentColumnPL = dataitemColumns.getPropertyList(j);
                if (isLocked) {
                    currentColumnPL = currentColumnPL.copy();
                    currentColumnPL.setProperty("class", "maint_lockedfield");
                }
                String columnid = currentColumnPL.getProperty("columnid");
                if (viewOnly && !"hidden".equals(currentColumnPL.getProperty("mode"))) {
                    currentColumnPL.setProperty("mode", "readonly");
                }
                if (currentColumnPL.getProperty("mode").equals("readonly") || currentColumnPL.getProperty("pseudocolumn").length() > 0 || columnid.equals("paramid") || columnid.equals("paramtype") || columnid.equals("replicateid") || columnid.equals("keyid1") || columnid.equals("paramlistid") || columnid.equals("paramlistversionid") || columnid.equals("variantid")) {
                    listColumn.setColumn(currentColumnPL);
                    String width = currentColumnPL.getProperty("width", "100");
                    listColumn.setColumnProperty("width", currentColumnPL.getProperty("width", "100"));
                    html.append(listColumn.getHtml());
                } else {
                    dataitemMaintColumn.setColumn(currentColumnPL);
                    dataitemMaintColumn.setColumnProperty("dataentry", "true");
                    dataitemMaintColumn.setColumnProperty("width", currentColumnPL.getProperty("width", "100"));
                    dataitemMaintColumn.setColumnProperty("class", currentColumnPL.getProperty("class", "dataentry_list_field"));
                    String mode = currentColumnPL.getProperty("mode");
                    if (!mode.equals("hidden")) {
                        if (isLocked) {
                            currentColumnPL.setProperty("mode", "readonly");
                            html.append("<td class=\"" + lockedClass + "\">" + dataitemMaintColumn.getHtml() + "</td>\n");
                        } else {
                            html.append("<td class=\"dataentry_list_field\">" + dataitemMaintColumn.getHtml() + "</td>\n");
                        }
                        if (mode.equals("checkbox")) {
                            ((ArrayList)this.idGrid.get(i)).add(dataitemMaintColumn.getId() + "_chx");
                        } else if (mode.indexOf("radiobutton") >= 0) {
                            ((ArrayList)this.idGrid.get(i)).add(dataitemMaintColumn.getId() + "_radio");
                        } else {
                            ((ArrayList)this.idGrid.get(i)).add(dataitemMaintColumn.getId());
                        }
                    } else {
                        html.append(dataitemMaintColumn.getHtml());
                    }
                }
                if (!entryColumnAfter.equals(dataitemColumns.getPropertyList(j).getProperty("columnid")) && (!entryColumnAfter.equals("last") || j != dataitemColumns.size() - 1)) continue;
                if (isLocked) {
                    html.append("<td class=\"" + lockedClass + "\">" + dataColumn.getHtml() + "</td>");
                } else {
                    html.append("<td class=\"dataentry_list_cell\">" + dataColumn.getHtml() + "</td>");
                }
                ((ArrayList)this.idGrid.get(i)).add(dataColumn.getId());
                dataColumnPending = false;
            }
            if (dataColumnPending) {
                if (isLocked) {
                    html.append("<td class=\"" + lockedClass + "\">" + dataColumn.getHtml() + "</td>");
                } else {
                    html.append("<td class=\"dataentry_list_cell\">" + dataColumn.getHtml() + "</td>");
                }
                ((ArrayList)this.idGrid.get(i)).add(dataColumn.getId());
            }
            html.append("</tr>");
            dataitemTablePending = true;
        }
        if (dataitemTablePending) {
            html.append("</table></td></tr>");
        }
        if (datasetDivPending) {
            html.append("</table></div></td></tr>");
        }
        if (primaryDivPending) {
            html.append("</table></div></td></tr>");
        }
        html.append("</table>");
        html.append("</div>\n");
        this.logger.info("Generating id grid...");
        html.append(SDITagUtil.getGrid(this.idGrid, this.getTranslationProcessor()));
        html.append("<div style=\"position:absolute; display:none\" id=\"dd_div\" class=\"dropdowndiv\" onkeydown=\"dd_divKeyPress()\" onmouseover=\"this.onblur = null;\" onmouseout=\"this.onblur = dd_divBlur;\"></div>\n");
        html.append("<textarea style=\"display:none;width:0;height:0\" id=\"clipboard\"></textarea>\n");
        html.append("\n<script>\n");
        html.append("handler1.allowCherryPicking=true;");
        html.append(this.pageContext.getAttribute("dd_dropdownvalues") != null ? this.pageContext.getAttribute("dd_dropdownvalues") : "");
        html.append("\nvar sapdateformat = " + RegexConverter.getSapDateFormat(this.pageContext) + ";\n");
        FormatUtil formatUtil = FormatUtil.getInstance(I18nUtil.getSessionLocale(this.pageContext));
        html.append("var decimalSeparator = \"" + formatUtil.getDecimalSeparator() + "\";\n");
        html.append("var groupingSeparator = \"" + formatUtil.getGroupingSeparator() + "\";\n");
        html.append("\n</script>\n");
        return html.toString();
    }

    private PropertyListCollection getPrimaryColumns() {
        PropertyListCollection columns;
        PropertyListCollection propertyListCollection = columns = this.element != null ? this.element.getCollection("primarycolumns") : null;
        if (columns == null) {
            columns = new PropertyListCollection();
            PropertyList c1 = new PropertyList();
            c1.setProperty("columnid", "keyid1");
            columns.add(c1);
        }
        return columns;
    }

    private PropertyListCollection getDatasetColumns() {
        PropertyListCollection columns;
        PropertyListCollection propertyListCollection = columns = this.element != null ? this.element.getCollection("datasetcolumns") : null;
        if (columns == null) {
            columns = new PropertyListCollection();
            PropertyList c1 = new PropertyList();
            c1.setProperty("columnid", "paramlist");
            c1.setProperty("width", "100%");
            c1.setProperty("pseudocolumn", "<b>[columnid=paramlistid]</b> ([columnid=variantid]) Version: [columnid=paramlistversionid] Dataset: #[columnid=dataset]");
            columns.add(c1);
        }
        return columns;
    }

    private PropertyListCollection getDataitemColumns() {
        PropertyListCollection columns;
        PropertyListCollection propertyListCollection = columns = this.element != null ? this.element.getCollection("dataitemcolumns") : null;
        if (columns == null) {
            columns = new PropertyListCollection();
            PropertyList c1 = new PropertyList();
            c1.setProperty("columnid", "paramid");
            c1.setProperty("title", "Parameter");
            columns.add(c1);
            PropertyList c2 = new PropertyList();
            c2.setProperty("columnid", "paramtype");
            c2.setProperty("title", "Type");
            columns.add(c2);
            PropertyList c3 = new PropertyList();
            c3.setProperty("columnid", "replicateid");
            c3.setProperty("title", "Rep");
            columns.add(c3);
            PropertyList c4 = new PropertyList();
            c4.setProperty("columnid", "displayunits");
            c4.setProperty("title", "Units");
            columns.add(c4);
        }
        return columns;
    }
}

