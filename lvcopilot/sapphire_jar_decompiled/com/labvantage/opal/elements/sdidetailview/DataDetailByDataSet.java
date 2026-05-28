/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.sdidetailview;

import com.labvantage.opal.elements.sdidetailview.DataDetailByDataSetUtil;
import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class DataDetailByDataSet
extends BaseElement {
    private String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private boolean __Stats = false;
    private boolean __Debug = false;
    private TranslationProcessor __Tp;

    private void setTranslationProcessor() {
        this.__Tp = this.getTranslationProcessor();
    }

    public DataDetailByDataSet(PropertyList element) {
        this.element = element;
        this.setTranslationProcessor();
    }

    public DataDetailByDataSet(PageContext pageContext, String connectionid) {
        this.setPageContext(pageContext);
        this.setConnectionId(connectionid);
        this.setTranslationProcessor();
    }

    @Override
    public String getHtml() {
        long prevTime;
        StringBuffer sbHtml;
        block80: {
            sbHtml = new StringBuffer();
            prevTime = System.currentTimeMillis();
            try {
                int j;
                boolean newPrimary = false;
                boolean newDataset = false;
                boolean newSpec = true;
                boolean primaryDivPending = false;
                boolean datasetDivPending = false;
                boolean dataitemTablePending = false;
                String currentKeyid = "";
                String currentDataset = "";
                String currentDataitem = "";
                String expandText = this.__Tp.translate("Expand to see") + " ";
                DataDetailByDataSetUtil dataDetailUtil = new DataDetailByDataSetUtil(this.pageContext, this.getConnectionId());
                dataDetailUtil.setElementProperties(this.element);
                if (dataDetailUtil.prepareDisplayOptions()) {
                    if (!dataDetailUtil.prepareSqls()) {
                        return "<font class=error>" + this.__Tp.translate("Got an error resolving the database selects in element DataDetail. Cannot continue.") + "</font>";
                    }
                } else {
                    return "<font class=error>" + this.__Tp.translate("Got an error resolving the display options in element DataDetail. Cannot continue.") + "</font>";
                }
                boolean showPrimary = dataDetailUtil._ShowPrimary;
                boolean showPrimaryHeader = dataDetailUtil._ShowPrimaryHeader;
                boolean showDatasetHeader = dataDetailUtil._ShowDatasetHeader;
                boolean showDataApprovalHeader = dataDetailUtil._ShowDataApprovalHeader;
                boolean showDataApproval = dataDetailUtil._ShowDataApproval;
                boolean showDataitemHeader = dataDetailUtil._ShowDataitemHeader;
                boolean showDataitem = dataDetailUtil._ShowDataitem;
                boolean showDataitemLimitsHeader = dataDetailUtil._ShowDataitemLimitsHeader;
                boolean showDataitemLimits = dataDetailUtil._ShowDataitemLimits;
                boolean showDataitemSpecsHeader = dataDetailUtil._ShowDataitemSpecsHeader;
                boolean showDataitemSpecs = dataDetailUtil._ShowDataitemSpecs;
                boolean initExpandPrimary = dataDetailUtil._InitExpandPrimary;
                boolean initExpandDataset = dataDetailUtil._InitExpandDataset;
                boolean initExpandDataApproval = dataDetailUtil._InitExpandDataApproval;
                boolean initExpandDataItem = dataDetailUtil._InitExpandDataItem;
                boolean initSingleExpandDataItem = true;
                boolean initExpandDataLimit = dataDetailUtil._InitExpandDataLimit;
                boolean initExpandDataSpec = dataDetailUtil._InitExpandDataSpec;
                ArrayList alPrimaryFindCols = dataDetailUtil._PrimaryFindCols;
                ArrayList alPrimaryColumns = dataDetailUtil._PrimaryCols;
                ArrayList alPrimaryColumnHeaders = dataDetailUtil._PrimaryColumnHeaders;
                ArrayList alPrimaryColumnsTranslate = dataDetailUtil._PrimaryColumnsTranslate;
                ArrayList alDatasetColumns = dataDetailUtil._DatasetColumns;
                ArrayList alDataApprovalColumnHeaders = dataDetailUtil._DataApprovalColumnHeaders;
                ArrayList alDataApprovalColumns = dataDetailUtil._DataApprovalColumns;
                ArrayList alDatasetColumnHeaders = dataDetailUtil._DatasetColumnHeaders;
                ArrayList alDatasetColumnsTranslate = dataDetailUtil._DatasetColumnsTranslate;
                ArrayList alDataitemColumns = dataDetailUtil._DataitemColumns;
                ArrayList alDataitemColumnHeaders = dataDetailUtil._DataitemColumnHeaders;
                ArrayList alDataitemColumnsTranslate = dataDetailUtil._DataitemColumnsTranslate;
                ArrayList alDataitemlimitColumns = dataDetailUtil._DataitemLimitColumns;
                ArrayList alDataitemlimitColumnHeaders = dataDetailUtil._DataitemLimitColumnHeaders;
                ArrayList alDataitemspecColumns = dataDetailUtil._DataitemSpecColumns;
                ArrayList alDataitemspecColumnHeaders = dataDetailUtil._DataitemSpecColumnHeaders;
                DataSet primary = dataDetailUtil._Primary;
                DataSet datasets = dataDetailUtil._Datasets;
                DataSet dataapprovals = dataDetailUtil._DataApprovals;
                DataSet dataitems = dataDetailUtil._Dataitems;
                DataSet dataitemlimits = dataDetailUtil._DataitemLimits;
                DataSet dataitemspecs = dataDetailUtil._DataitemSpecs;
                int primaryRow = 0;
                int datasetRow = 0;
                int dataitemCount = 0;
                int dataitemspecCount = 0;
                int dataitemlimitCount = 0;
                HashMap<String, String> findPrimary = new HashMap<String, String>();
                HashMap<String, Object> findDataset = new HashMap<String, Object>();
                HashMap<String, Object> findDataApprovals = new HashMap<String, Object>();
                HashMap<String, Object> findDataitemLimits = new HashMap<String, Object>();
                HashMap<String, Object> findDataitemSpecs = new HashMap<String, Object>();
                sbHtml.append("\t \t </td>");
                sbHtml.append("\t </tr>");
                sbHtml.append("</table>");
                sbHtml.append("<table cellpadding=0 cellspacing=0 valign=\"top\"  style=\"").append(this.browser.isIE() ? "position: fixed;" : "").append("top: 50px; margin-top: 10px; vertical-align: top; \" >");
                if (dataitems.size() == 0 && showPrimary) {
                    sbHtml.append("<tr>\n");
                    String src = "WEB-OPAL/images/" + (initExpandPrimary ? "minus" : "plus") + ".gif";
                    sbHtml.append("<td width=1 valign=top><img id=\"" + currentKeyid + "\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src + "\"></td>\n");
                    sbHtml.append("<td width=1>&nbsp;</td>\n");
                    sbHtml.append("<td width=\"100%\" align=left>\n");
                    sbHtml.append("<table border=0 cellpadding=\"3\" cellspacing=\"0\" class=\"info_list_primarytable\">\n");
                    if (showPrimaryHeader) {
                        sbHtml.append("<tr class=\"info_list_primaryheader\">\n");
                        for (j = 0; j < alPrimaryColumnHeaders.size(); ++j) {
                            sbHtml.append("<td>" + (String)alPrimaryColumnHeaders.get(j) + "</td>\n");
                        }
                        sbHtml.append("</tr>\n");
                    }
                    sbHtml.append("<tr class=\"info_list_primaryrow\">\n");
                    for (j = 0; j < alPrimaryColumns.size(); ++j) {
                        String colid = (String)alPrimaryColumns.get(j);
                        String value = primary.getValue(primaryRow, colid, "&nbsp;");
                        if (((Boolean)alPrimaryColumnsTranslate.get(j)).booleanValue()) {
                            value = this.__Tp.translate(value);
                        }
                        sbHtml.append("<td>" + value + "</td>\n");
                    }
                    sbHtml.append("</tr>\n</table>\n <!-- closes primary table --> </td>\n</tr>\n");
                }
                for (int dataitemRow = 0; dataitemRow < dataitems.size(); ++dataitemRow) {
                    int j2;
                    int j3;
                    String src;
                    dataitemspecCount = 0;
                    dataitemlimitCount = 0;
                    if (!currentKeyid.equals(dataitems.getString(dataitemRow, "keyid1"))) {
                        currentKeyid = dataitems.getString(dataitemRow, "keyid1");
                        currentDataset = "";
                        newPrimary = true;
                        if (dataitemTablePending) {
                            dataitemTablePending = false;
                            sbHtml.append("</table> <!-- if dataitem table pending --> \n </div> <!-- closes dataitem div -->  \n </td>\n</tr>\n");
                        }
                        if (datasetDivPending) {
                            datasetDivPending = false;
                            sbHtml.append("</table> <!-- if dataset div pending --> \n\n\n \t </div>\n</td>\n</tr>\n");
                        }
                        if (primaryDivPending) {
                            primaryDivPending = false;
                            sbHtml.append("</table> <!-- if primary div pending --> \n \n \n \t </div>\n</td>\n</tr>\n");
                        }
                        if (showPrimary) {
                            findPrimary.clear();
                            for (j = 0; j < alPrimaryFindCols.size(); ++j) {
                                if (((String)alPrimaryFindCols.get(j)).length() <= 0) continue;
                                findPrimary.put((String)alPrimaryFindCols.get(j), dataitems.getString(dataitemRow, "keyid" + (j + 1)));
                            }
                            primaryRow = primary.findRow(findPrimary, 0);
                            sbHtml.append("<tr>\n");
                            String src2 = "WEB-OPAL/images/" + (initExpandPrimary ? "minus" : "plus") + ".gif";
                            sbHtml.append("<td width=1 valign=top><img id=\"" + currentKeyid + "\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src2 + "\"></td>\n");
                            sbHtml.append("<td width=1>&nbsp;</td>\n");
                            sbHtml.append("<td width=\"100%\" align=left>\n");
                            sbHtml.append("<table border=0 cellpadding=\"3\" cellspacing=\"0\" class=\"info_list_primarytable\">\n");
                            if (showPrimaryHeader) {
                                sbHtml.append("<tr class=\"info_list_primaryheader\">\n");
                                for (int j4 = 0; j4 < alPrimaryColumnHeaders.size(); ++j4) {
                                    sbHtml.append("<td>" + (String)alPrimaryColumnHeaders.get(j4) + "</td>\n");
                                }
                                sbHtml.append("</tr>\n");
                            }
                            sbHtml.append("<tr class=\"info_list_primaryrow\">\n");
                            for (int j5 = 0; j5 < alPrimaryColumns.size(); ++j5) {
                                String value = primary.getValue(primaryRow, (String)alPrimaryColumns.get(j5), "&nbsp;");
                                if (((Boolean)alPrimaryColumnsTranslate.get(j5)).booleanValue()) {
                                    value = this.__Tp.translate(value);
                                }
                                sbHtml.append("<td>" + value + "</td>\n");
                            }
                            sbHtml.append("</tr>\n</table> <!-- closes primary table --> \n</td>\n</tr>\n");
                        }
                        sbHtml.append("<tr>\n<td colspan=\"3\">\n");
                        sbHtml.append("\n \n \t <div style=\"display:" + (initExpandPrimary ? "block" : "none") + "\" id=\"" + currentKeyid + "data\">\n");
                        sbHtml.append("<table border=0 width=100%  cellspacing=0 cellpadding=0 class=\"info_list_datasettable\">\n");
                        primaryDivPending = true;
                    }
                    if (!currentDataset.equals(dataitems.getString(dataitemRow, "paramlistid") + ";" + dataitems.getString(dataitemRow, "paramlistversionid") + ";" + dataitems.getString(dataitemRow, "variantid") + ";" + dataitems.getInt(dataitemRow, "dataset"))) {
                        currentDataset = dataitems.getString(dataitemRow, "paramlistid") + ";" + dataitems.getString(dataitemRow, "paramlistversionid") + ";" + dataitems.getString(dataitemRow, "variantid") + ";" + dataitems.getInt(dataitemRow, "dataset");
                        findDataset.clear();
                        findDataset.put("keyid1", dataitems.getString(dataitemRow, "keyid1"));
                        findDataset.put("keyid2", dataitems.getString(dataitemRow, "keyid2"));
                        findDataset.put("keyid3", dataitems.getString(dataitemRow, "keyid3"));
                        findDataset.put("paramlistid", dataitems.getString(dataitemRow, "paramlistid"));
                        findDataset.put("paramlistversionid", dataitems.getString(dataitemRow, "paramlistversionid"));
                        findDataset.put("variantid", dataitems.getString(dataitemRow, "variantid"));
                        findDataset.put("dataset", dataitems.getBigDecimal(dataitemRow, "dataset"));
                        datasetRow = datasets.findRow(findDataset, 0);
                        newDataset = true;
                        if (dataitemTablePending) {
                            dataitemTablePending = false;
                            sbHtml.append("</table> <!-- if dataitem table pending -->  \n </div> <!-- closes dataitem div --> \n </td> \n</tr>\n");
                        }
                        if (datasetDivPending) {
                            datasetDivPending = false;
                            sbHtml.append("</table> <!-- if dataset div pending --> \n \n \t </div>\n</td>\n</tr>\n");
                        }
                        sbHtml.append("<tr><td>&nbsp;</td>\n");
                        if (showDataitem) {
                            String src3 = "WEB-OPAL/images/" + (initExpandDataset ? "minus" : "plus") + ".gif";
                            sbHtml.append("<td width=\"20\" valign=top><img id=\"" + currentKeyid + ";" + currentDataset + "\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src3 + "\"></td>\n");
                        } else {
                            sbHtml.append("<td>&nbsp;</td>\n");
                        }
                        if (!showDataitemHeader) {
                            sbHtml.append("<td width=\"12\" valign=\"bottom\">\n");
                            sbHtml.append("-");
                            sbHtml.append("</td>\n");
                        }
                        sbHtml.append("<td width=\"*\">\n");
                        sbHtml.append("<table border=0 width=100% cellpadding=\"3\" cellspacing=\"0\">\n");
                        if (showDatasetHeader && newPrimary) {
                            if (!showDataitem) {
                                newPrimary = false;
                            }
                            sbHtml.append("<tr class=\"info_list_datasetheader\" >\n");
                            for (int j6 = 0; j6 < alDatasetColumnHeaders.size(); ++j6) {
                                alDatasetColumnHeaders.remove(this.__Tp.translate("Ver."));
                                sbHtml.append("<td>" + (String)alDatasetColumnHeaders.get(j6) + "</td>\n");
                            }
                            sbHtml.append("</tr>\n");
                        }
                        sbHtml.append("<tr class=\"info_list_datasetrow\">\n");
                        int indexOfParamListVersion = alDatasetColumns.indexOf("paramlistversionid");
                        if (indexOfParamListVersion >= 0) {
                            alDatasetColumns.remove(indexOfParamListVersion);
                            alDatasetColumnsTranslate.remove(indexOfParamListVersion);
                        }
                        for (int j7 = 0; j7 < alDatasetColumns.size(); ++j7) {
                            String value = datasets.getValue(datasetRow, (String)alDatasetColumns.get(j7), "&nbsp;");
                            if (((Boolean)alDatasetColumnsTranslate.get(j7)).booleanValue()) {
                                value = this.__Tp.translate(value);
                            }
                            sbHtml.append("<td>" + value + "</td>\n");
                        }
                        sbHtml.append("</tr>\n");
                        sbHtml.append("</table> <!-- closes dataset --> \n</td>\n</tr>\n");
                        if (showDataitem) {
                            sbHtml.append("<tr><td colspan=\"" + (showDataitemHeader ? "2" : "3") + "\"><div style=\"display: none\">&nbsp;</div></td>\n");
                            sbHtml.append("<td> \n \n \t <div style=\"display:" + (initExpandDataset ? "block" : "none") + "\" id=\"" + currentKeyid + ";" + currentDataset + "data\">\n");
                            if (showDataApproval) {
                                DataSet dsFilteredDataApprovals = new DataSet();
                                findDataApprovals.clear();
                                findDataApprovals.put("keyid1", datasets.getString(datasetRow, "keyid1"));
                                findDataApprovals.put("keyid2", datasets.getString(datasetRow, "keyid2"));
                                findDataApprovals.put("keyid3", datasets.getString(datasetRow, "keyid3"));
                                findDataApprovals.put("paramlistid", datasets.getString(datasetRow, "paramlistid"));
                                findDataApprovals.put("paramlistversionid", datasets.getString(datasetRow, "paramlistversionid"));
                                findDataApprovals.put("variantid", datasets.getString(datasetRow, "variantid"));
                                findDataApprovals.put("dataset", datasets.getBigDecimal(datasetRow, "dataset"));
                                dsFilteredDataApprovals = dataapprovals.getFilteredDataSet(findDataApprovals);
                                if (dsFilteredDataApprovals.size() > 0) {
                                    ArrayList<String> alCols = new ArrayList<String>();
                                    int approvals = dsFilteredDataApprovals.size();
                                    alCols.clear();
                                    alCols.add("approvalstep");
                                    int uniqueApprovals = OpalUtil.getUniqueTreeSetOfColumns(alCols, dsFilteredDataApprovals).size();
                                    sbHtml.append("<table border=0 bordercolor=green cellpadding=2 cellspacing=0><tr>");
                                    sbHtml.append("<td width=12>&nbsp;</td>");
                                    src = "WEB-OPAL/images/" + (initExpandDataApproval ? "minus" : "plus") + ".gif";
                                    sbHtml.append("<td width=\"12\" valign=top><img id=\"" + currentKeyid + ";" + currentDataset + "_approval_\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src + "\"></td>\n");
                                    sbHtml.append("<td>");
                                    sbHtml.append("\n <div style=\"display:" + (initExpandDataApproval ? "none" : "block") + "; color:brown;\" id=\"" + currentKeyid + ";" + currentDataset + "_approval_text\">\n");
                                    sbHtml.append(expandText + uniqueApprovals + " " + this.__Tp.translate("approval(s)"));
                                    sbHtml.append("</div>\n");
                                    sbHtml.append("\n <div style=\"display:" + (initExpandDataApproval ? "block" : "none") + "\" id=\"" + currentKeyid + ";" + currentDataset + "_approval_data\">\n");
                                    sbHtml.append("<table border=0 cellpadding=4 cellspacing=0 class=\"info_list_dataapprovaltable\">\n");
                                    if (showDataApprovalHeader) {
                                        sbHtml.append("<tr class=\"info_list_dataapprovalheader\">\n");
                                        sbHtml.append("<td width=\"1\">&nbsp;</td>\n");
                                        for (j3 = 0; j3 < alDataApprovalColumnHeaders.size(); ++j3) {
                                            sbHtml.append("<td>" + (String)alDataApprovalColumnHeaders.get(j3) + "</td>\n");
                                        }
                                        sbHtml.append("</tr>\n");
                                    }
                                    for (int dataapprovalRow = 0; dataapprovalRow < approvals; ++dataapprovalRow) {
                                        sbHtml.append("<tr class=\"info_list_dataapprovalrow" + (dataapprovalRow % 2 != 0 ? "odd" : "even") + "\">\n");
                                        sbHtml.append("<td>&nbsp;</td>");
                                        for (j2 = 0; j2 < alDataApprovalColumns.size(); ++j2) {
                                            if (((String)alDataApprovalColumns.get(j2)).equalsIgnoreCase("approvalflag")) {
                                                sbHtml.append("<td align=center><img src=\"WEB-OPAL/elements/sdidetailview/images/" + dsFilteredDataApprovals.getString(dataapprovalRow, (String)alDataApprovalColumns.get(j2)));
                                                sbHtml.append(".gif\" border=0 height=14 title=\"ApprovalFlag = " + dsFilteredDataApprovals.getString(dataapprovalRow, (String)alDataApprovalColumns.get(j2)) + "\"></td>\n");
                                                continue;
                                            }
                                            if (((String)alDataApprovalColumns.get(j2)).equalsIgnoreCase("mandatoryflag")) {
                                                sbHtml.append("<td align=center>" + dsFilteredDataApprovals.getValue(dataapprovalRow, (String)alDataApprovalColumns.get(j2), "&nbsp;") + "</td>\n");
                                                continue;
                                            }
                                            sbHtml.append("<td>" + dsFilteredDataApprovals.getValue(dataapprovalRow, (String)alDataApprovalColumns.get(j2), "&nbsp;") + "</td>\n");
                                        }
                                        sbHtml.append("</tr>\n");
                                    }
                                    sbHtml.append("</table> <!-- closes dataapprovals --> \n</div>\n</td>\n</tr>\n</table>\n");
                                }
                            }
                            sbHtml.append("<table width=100%  cellspacing=0 cellpadding=0 border=0>\n");
                            datasetDivPending = true;
                        }
                    }
                    if (!showDataitem) continue;
                    if (newDataset) {
                        sbHtml.append("\n \n <table border=0 bordercolor=green cellpadding=2 cellspacing=0> \n");
                        sbHtml.append("<tr> \n");
                        String src_dataitems = "WEB-OPAL/images/" + (initExpandDataItem ? "minus" : "plus") + ".gif";
                        sbHtml.append("<td width=\"12\" valign=top> \n");
                        sbHtml.append("<img id=\"" + currentKeyid + "-" + currentDataset + "-" + dataitemRow + "dataitem_spec_\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src_dataitems + "\"></td>\n");
                        sbHtml.append("<td>");
                        sbHtml.append(" \n \n <div style=\"display:" + (initExpandDataItem ? "none" : "block") + "; color:brown;\" id=\"" + currentKeyid + "-" + currentDataset + "-" + dataitemRow + "dataitem_spec_text\">\n");
                        DataSet dsFilteredDataItems = new DataSet();
                        findDataitemLimits.clear();
                        findDataitemLimits.put("keyid1", dataitems.getString(dataitemRow, "keyid1"));
                        findDataitemLimits.put("keyid2", dataitems.getString(dataitemRow, "keyid2"));
                        findDataitemLimits.put("keyid3", dataitems.getString(dataitemRow, "keyid3"));
                        findDataitemLimits.put("paramlistid", dataitems.getString(dataitemRow, "paramlistid"));
                        findDataitemLimits.put("paramlistversionid", dataitems.getString(dataitemRow, "paramlistversionid"));
                        findDataitemLimits.put("variantid", dataitems.getString(dataitemRow, "variantid"));
                        findDataitemLimits.put("dataset", dataitems.getBigDecimal(dataitemRow, "dataset"));
                        dsFilteredDataItems = dataitems.getFilteredDataSet(findDataitemLimits);
                        int uniqueDataItems = dsFilteredDataItems.size();
                        sbHtml.append(expandText + uniqueDataItems + " " + this.__Tp.translate("dataitem(s)"));
                        sbHtml.append("\n </div>\n");
                        sbHtml.append("</td>");
                        sbHtml.append("<td> \n");
                        sbHtml.append("\n \n <div style=\"display:" + (initExpandDataItem ? "block" : "none") + "\" id=\"" + currentKeyid + "-" + currentDataset + "-" + dataitemRow + "dataitem_spec_data\" name=\"" + currentKeyid + "-" + currentDataset + "-" + dataitemRow + "dataitem_spec_data\">\n");
                        newDataset = false;
                        sbHtml.append("\n <table width=100% cellpadding=\"3\" border=0 cellspacing=\"0\" class=\"info_list_dataitemtable\">\n");
                        if (showDataitemHeader) {
                            sbHtml.append("<tr class=\"info_list_dataitemheader\">\n");
                            sbHtml.append("<td width=\"1\">&nbsp;</td>\n");
                            sbHtml.append("<td width=\"1\">&nbsp;</td>\n");
                            for (int j8 = 0; j8 < alDataitemColumnHeaders.size(); ++j8) {
                                sbHtml.append("<td>" + (String)alDataitemColumnHeaders.get(j8) + "</td>\n");
                            }
                            sbHtml.append("</tr>\n");
                        }
                    }
                    sbHtml.append("<tr class=\"info_list_dataitemrow" + (dataitemCount++ % 2 != 0 ? "odd" : "even") + "\">\n");
                    String src_singledataitem = "WEB-OPAL/images/" + (initSingleExpandDataItem ? "plus" : "false") + ".gif";
                    currentDataitem = dataitems.getString(dataitemRow, "paramid") + ";" + dataitems.getString(dataitemRow, "paramtype") + ";" + dataitems.getValue(dataitemRow, "replicateid");
                    sbHtml.append("<td width=\"12\" valign=top> \n");
                    sbHtml.append("<img id=\"" + currentKeyid + ";" + currentDataset + ";" + dataitemRow + "singledi_spec_\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src_singledataitem + "\"></td>\n");
                    sbHtml.append("<td width=\"20\">");
                    sbHtml.append("<img src=\"WEB-CORE/images/gif/Sample.gif\" height=15 width=15>");
                    sbHtml.append("</td>");
                    for (int j9 = 0; j9 < alDataitemColumns.size(); ++j9) {
                        sbHtml.append("<td>");
                        String value = dataitems.getValue(dataitemRow, (String)alDataitemColumns.get(j9), "&nbsp;");
                        if (((Boolean)alDataitemColumnsTranslate.get(j9)).booleanValue()) {
                            value = this.__Tp.translate(value);
                        }
                        sbHtml.append(value.replaceAll("<", "&lt;"));
                        sbHtml.append("</td>\n");
                    }
                    sbHtml.append("</tr> <!-- closes dataitemrow --> \n");
                    if (showDataitemLimits) {
                        DataSet dsFilteredDataItemLimits = new DataSet();
                        findDataitemLimits.clear();
                        findDataitemLimits.put("keyid1", dataitems.getString(dataitemRow, "keyid1"));
                        findDataitemLimits.put("keyid2", dataitems.getString(dataitemRow, "keyid2"));
                        findDataitemLimits.put("keyid3", dataitems.getString(dataitemRow, "keyid3"));
                        findDataitemLimits.put("paramlistid", dataitems.getString(dataitemRow, "paramlistid"));
                        findDataitemLimits.put("paramlistversionid", dataitems.getString(dataitemRow, "paramlistversionid"));
                        findDataitemLimits.put("variantid", dataitems.getString(dataitemRow, "variantid"));
                        findDataitemLimits.put("dataset", dataitems.getBigDecimal(dataitemRow, "dataset"));
                        findDataitemLimits.put("paramid", dataitems.getString(dataitemRow, "paramid"));
                        findDataitemLimits.put("paramtype", dataitems.getString(dataitemRow, "paramtype"));
                        findDataitemLimits.put("replicateid", dataitems.getBigDecimal(dataitemRow, "replicateid"));
                        dsFilteredDataItemLimits = dataitemlimits.getFilteredDataSet(findDataitemLimits);
                        if (dsFilteredDataItemLimits.size() > 0) {
                            ArrayList<String> alCols = new ArrayList<String>();
                            int limits = dsFilteredDataItemLimits.size();
                            alCols.clear();
                            alCols.add("limittypeid");
                            int uniqueLimits = OpalUtil.getUniqueTreeSetOfColumns(alCols, dsFilteredDataItemLimits).size();
                            sbHtml.append("<tr>\n");
                            sbHtml.append(" <td width=* colspan=" + (alDataitemColumns.size() + 2) + ">\n");
                            sbHtml.append(" \n \n <div style=\"display:" + (initSingleExpandDataItem ? "none" : "block") + "; color:brown;\" name=\"" + currentKeyid + ";" + currentDataset + ";" + dataitemRow + "singledi_spec_data\" id=\"" + currentKeyid + ";" + currentDataset + ";" + dataitemRow + "singledi_spec_data\">\n");
                            sbHtml.append("<table width=\"100%\"><tr>");
                            sbHtml.append("<td width=\"10\">&nbsp;</td>\n");
                            src = "WEB-OPAL/images/" + (initExpandDataLimit ? "minus" : "plus") + ".gif";
                            sbHtml.append("<td width=\"12\" valign=top><img id=\"" + currentKeyid + ";" + currentDataset + ";" + currentDataitem + "_limit_\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src + "\"></td>\n");
                            sbHtml.append("<td>");
                            sbHtml.append("\n <div style=\"display:" + (initExpandDataLimit ? "none" : "block") + "; color:brown;\" id=\"" + currentKeyid + ";" + currentDataset + ";" + currentDataitem + "_limit_text\">\n");
                            sbHtml.append(expandText + uniqueLimits + " " + this.__Tp.translate("limit(s)"));
                            sbHtml.append("\n </div>\n");
                            sbHtml.append("\n \t <div style=\"display:" + (initExpandDataLimit ? "block" : "none") + "\" id=\"" + currentKeyid + ";" + currentDataset + ";" + currentDataitem + "_limit_data\">\n");
                            sbHtml.append("<table cellspacing=0 cellpadding=4 class=\"info_list_datalimittable\">\n");
                            if (showDataitemLimitsHeader) {
                                sbHtml.append("<tr class=\"info_list_datalimitheader\">\n");
                                sbHtml.append("<td width=10>&nbsp;</td>");
                                for (j3 = 0; j3 < alDataitemlimitColumnHeaders.size(); ++j3) {
                                    sbHtml.append("<td>" + (String)alDataitemlimitColumnHeaders.get(j3) + "</td>\n");
                                }
                                sbHtml.append("</tr>\n");
                            }
                            for (int dataitemlimitRow = 0; dataitemlimitRow < limits; ++dataitemlimitRow) {
                                sbHtml.append("<tr class=\"info_list_datalimitrow" + (dataitemlimitCount++ % 2 != 0 ? "odd" : "even") + "\">\n");
                                sbHtml.append("<td>&nbsp;</td>");
                                for (j2 = 0; j2 < alDataitemlimitColumns.size(); ++j2) {
                                    if (((String)alDataitemlimitColumns.get(j2)).equalsIgnoreCase("operator")) {
                                        String operator = dsFilteredDataItemLimits.getString(dataitemlimitRow, (String)alDataitemlimitColumns.get(j2), "");
                                        if (dsFilteredDataItemLimits.getString(dataitemlimitRow, (String)alDataitemlimitColumns.get(j2), "").equalsIgnoreCase("IB")) {
                                            operator = this.__Tp.translate("Inclusive Between");
                                        } else if (dsFilteredDataItemLimits.getString(dataitemlimitRow, (String)alDataitemlimitColumns.get(j2), "").equalsIgnoreCase("EB")) {
                                            operator = this.__Tp.translate("Exclusive Between");
                                        } else if (dsFilteredDataItemLimits.getString(dataitemlimitRow, (String)alDataitemlimitColumns.get(j2), "").equalsIgnoreCase("IO")) {
                                            operator = this.__Tp.translate("Inclusive Outside");
                                        } else if (dsFilteredDataItemLimits.getString(dataitemlimitRow, (String)alDataitemlimitColumns.get(j2), "").equalsIgnoreCase("EO")) {
                                            operator = this.__Tp.translate("Exclusive Outside");
                                        } else if (dsFilteredDataItemLimits.getString(dataitemlimitRow, (String)alDataitemlimitColumns.get(j2), "").equalsIgnoreCase("NIN")) {
                                            operator = this.__Tp.translate("Not In");
                                        } else if (dsFilteredDataItemLimits.getString(dataitemlimitRow, (String)alDataitemlimitColumns.get(j2), "").equalsIgnoreCase("IN")) {
                                            operator = this.__Tp.translate("In");
                                        }
                                        sbHtml.append("<td>" + operator + "</td>\n");
                                        continue;
                                    }
                                    sbHtml.append("<td>" + dsFilteredDataItemLimits.getValue(dataitemlimitRow, (String)alDataitemlimitColumns.get(j2), "&nbsp;") + "</td>\n");
                                }
                                sbHtml.append("</tr>\n");
                            }
                            sbHtml.append("</table> <!-- closes dataitem limits --> \n</div>\n</td>\n</tr>\n ");
                            sbHtml.append("</table>\n");
                            sbHtml.append("</div>");
                            sbHtml.append("</td>\n</tr>\n");
                        }
                    }
                    if (showDataitemSpecs) {
                        DataSet dsFilteredDataItemSpecs = new DataSet();
                        findDataitemSpecs.clear();
                        findDataitemSpecs.put("keyid1", dataitems.getString(dataitemRow, "keyid1"));
                        findDataitemSpecs.put("keyid2", dataitems.getString(dataitemRow, "keyid2"));
                        findDataitemSpecs.put("keyid3", dataitems.getString(dataitemRow, "keyid3"));
                        findDataitemSpecs.put("paramlistid", dataitems.getString(dataitemRow, "paramlistid"));
                        findDataitemSpecs.put("paramlistversionid", dataitems.getString(dataitemRow, "paramlistversionid"));
                        findDataitemSpecs.put("variantid", dataitems.getString(dataitemRow, "variantid"));
                        findDataitemSpecs.put("dataset", dataitems.getBigDecimal(dataitemRow, "dataset"));
                        findDataitemSpecs.put("paramid", dataitems.getString(dataitemRow, "paramid"));
                        findDataitemSpecs.put("paramtype", dataitems.getString(dataitemRow, "paramtype"));
                        findDataitemSpecs.put("replicateid", dataitems.getBigDecimal(dataitemRow, "replicateid"));
                        dsFilteredDataItemSpecs = dataitemspecs.getFilteredDataSet(findDataitemSpecs);
                        if (dsFilteredDataItemSpecs.size() > 0) {
                            ArrayList<String> alCols = new ArrayList<String>();
                            int specs = dsFilteredDataItemSpecs.size();
                            alCols.clear();
                            alCols.add("specid");
                            alCols.add("specversionid");
                            int uniqueSpecs = OpalUtil.getUniqueTreeSetOfColumns(alCols, dsFilteredDataItemSpecs).size();
                            sbHtml.append("<tr>\n");
                            sbHtml.append("<td width=* colspan=" + (alDataitemColumns.size() + 2) + ">\n");
                            sbHtml.append(" \n \n <div style=\"display:" + (initSingleExpandDataItem ? "none" : "block") + "; color:brown; border: 0px outset; background-color: white; \" name=\"" + currentKeyid + ";" + currentDataset + ";" + dataitemRow + "singledi_spec_data\" id=\"" + currentKeyid + ";" + currentDataset + ";" + dataitemRow + "singledi_spec_data\">\n");
                            sbHtml.append("<table width=\"100%\"><tr>");
                            sbHtml.append("<td width=\"10\">&nbsp;</td>\n");
                            src = "WEB-OPAL/images/" + (initExpandDataSpec ? "minus" : "plus") + ".gif";
                            sbHtml.append("<td width=\"12\" valign=top><img id=\"" + currentKeyid + ";" + currentDataset + ";" + currentDataitem + "_spec_\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src + "\"></td>\n");
                            sbHtml.append("<td>");
                            sbHtml.append("\n <div style=\"display:" + (initExpandDataSpec ? "none" : "block") + "; color:brown;\" id=\"" + currentKeyid + ";" + currentDataset + ";" + currentDataitem + "_spec_text\">\n");
                            sbHtml.append(expandText + uniqueSpecs + " " + this.__Tp.translate("spec(s)"));
                            sbHtml.append("\n </div>");
                            String oldSpec = "";
                            for (int dataitemspecRow = 0; dataitemspecRow < specs; ++dataitemspecRow) {
                                int j10;
                                if (!oldSpec.equalsIgnoreCase(dsFilteredDataItemSpecs.getString(dataitemspecRow, "specid") + dsFilteredDataItemSpecs.getString(dataitemspecRow, "specversionid"))) {
                                    newSpec = true;
                                }
                                if (newSpec) {
                                    if (dataitemspecRow > 0) {
                                        sbHtml.append("</table> <!-- if dataitem spec rows nonzero --> \n </div>");
                                    }
                                    sbHtml.append("\n \n <div style=\"display:" + (initExpandDataSpec ? "block" : "none") + "\" id=\"" + currentKeyid + ";" + currentDataset + ";" + currentDataitem + "_spec_data\" name=\"" + currentKeyid + ";" + currentDataset + ";" + currentDataitem + "_spec_data\">\n");
                                    sbHtml.append("<table cellspacing=0 cellpadding=4 class=\"info_list_dataspectable\">\n");
                                    if (showDataitemSpecsHeader) {
                                        sbHtml.append("<tr class=\"info_list_dataspecheader\">\n");
                                        sbHtml.append("<td width=10>&nbsp;</td>");
                                        for (j10 = 0; j10 < alDataitemspecColumnHeaders.size(); ++j10) {
                                            sbHtml.append("<td>" + (String)alDataitemspecColumnHeaders.get(j10) + "</td>\n");
                                        }
                                        sbHtml.append("</tr>\n");
                                    }
                                }
                                sbHtml.append("<tr class=\"info_list_dataspecrow" + (dataitemspecCount++ % 2 != 0 ? "odd" : "even") + "\">\n");
                                sbHtml.append("<td>&nbsp;</td>");
                                for (j10 = 0; j10 < alDataitemspecColumns.size(); ++j10) {
                                    if (newSpec) {
                                        sbHtml.append("<td>" + dsFilteredDataItemSpecs.getValue(dataitemspecRow, (String)alDataitemspecColumns.get(j10), "&nbsp;") + "</td>\n");
                                        continue;
                                    }
                                    if (((String)alDataitemspecColumns.get(j10)).equalsIgnoreCase("specid") || ((String)alDataitemspecColumns.get(j10)).equalsIgnoreCase("specversionid") || ((String)alDataitemspecColumns.get(j10)).equalsIgnoreCase("condition")) {
                                        sbHtml.append("<td>&nbsp;</td>\n");
                                        continue;
                                    }
                                    sbHtml.append("<td>" + dsFilteredDataItemSpecs.getValue(dataitemspecRow, (String)alDataitemspecColumns.get(j10), "&nbsp;") + "</td>\n");
                                }
                                sbHtml.append("</tr>\n");
                                oldSpec = dsFilteredDataItemSpecs.getString(dataitemspecRow, "specid") + dsFilteredDataItemSpecs.getString(dataitemspecRow, "specversionid");
                                newSpec = false;
                            }
                            sbHtml.append("</table>\n</div> <!-- closes initExpandDataSpec dataitem specs --> \n</td>\n</tr>\n</table> <!-- closes dataitem specs container table --> </div> <!-- closes single dataitem div --> \n</td>\n</tr>\n");
                        }
                    }
                    dataitemTablePending = true;
                }
                sbHtml.append("</td>");
                sbHtml.append("</tr>");
                sbHtml.append("\n \t </table>  <!-- closes dataitem table -->");
                if (dataitemTablePending) {
                    sbHtml.append("\n \n \t </table> <!-- if dataitem table pending -->  \n </div> <!-- closes all dataitems div -->  \n </td></tr>\n ");
                }
                if (datasetDivPending) {
                    sbHtml.append("</table> <!-- if dataset div pending --> \n</div>\n</td>\n</tr>\n");
                }
                if (primaryDivPending) {
                    sbHtml.append("</table> <!-- if primary div pending --> \n</div>\n</td>\n</tr>\n");
                }
                sbHtml.append("</table>");
                findPrimary = null;
                findDataset = null;
                findDataitemLimits = null;
                findDataitemSpecs = null;
                primary = null;
                datasets = null;
                dataitems = null;
                dataitemlimits = null;
                dataitemspecs = null;
                alDataitemColumnHeaders = null;
                alDataitemColumns = null;
                alDataitemColumnsTranslate = null;
                alDataitemlimitColumnHeaders = null;
                alDataitemlimitColumns = null;
                alDataitemspecColumnHeaders = null;
                alDataitemspecColumns = null;
                alDatasetColumnHeaders = null;
                alDatasetColumns = null;
                alDatasetColumnsTranslate = null;
                alPrimaryColumnHeaders = null;
                alPrimaryColumns = null;
                alPrimaryColumnsTranslate = null;
                alPrimaryFindCols = null;
            }
            catch (Exception ex) {
                this.logger.error("DataDetailByDataSet.getHtml() -> Exception thrown " + ex, ex);
                if (!this.__Debug) break block80;
                return ex.toString();
            }
        }
        if (this.__Stats) {
            long currTime = System.currentTimeMillis();
            this.logger.debug("DataDetailByDataSet.getHtml() -> Element Took " + (currTime - prevTime) + " ms.");
            sbHtml.append("DataDetail Element Took " + (currTime - prevTime) + " ms.");
        }
        return sbHtml.toString();
    }
}

