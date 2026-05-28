/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.sdidetailview;

import com.labvantage.sapphire.Trace;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataDetailBySDC
extends BaseElement {
    private String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private TranslationProcessor __Tp;
    protected ArrayList alPrimaryColumnIds = new ArrayList();
    protected ArrayList alPrimaryColumnTitles = new ArrayList();
    protected ArrayList alDataApprovalColumnIds = new ArrayList();
    protected ArrayList alDataApprovalColumnTitles = new ArrayList();
    protected ArrayList alDatasetColumnIds = new ArrayList();
    protected ArrayList alDatasetColumnTitles = new ArrayList();
    protected ArrayList alDataitemColumnIds = new ArrayList();
    protected ArrayList alDataitemColumnTitles = new ArrayList();
    protected ArrayList alDataapprovalColumnIds = new ArrayList();
    protected ArrayList alDataapprovalColumnTitles = new ArrayList();
    protected ArrayList alDataitemSpecColumnIds = new ArrayList();
    protected ArrayList alDataitemSpecColumnTitles = new ArrayList();
    protected ArrayList alDataitemLimitIds = new ArrayList();
    protected ArrayList alDataitemLimitTitles = new ArrayList();
    protected DataSet dsDataitemSpecs = new DataSet();
    protected DataSet dsFilteredDataitems = new DataSet();
    protected DataSet dsFilteredDataitemSpecs = new DataSet();
    protected DataSet dsFilteredDataitemLimits = new DataSet();
    protected HashMap hmFilterMap = new HashMap();
    String sdcId = "";
    String currentKeyid = "";

    public DataDetailBySDC(PageContext pageContext, String nameserverlist, String connectionid) {
        this.pageContext = pageContext;
        this.setConnectionId(connectionid);
        this.setTranslationProcessor();
    }

    private void setTranslationProcessor() {
        this.__Tp = this.getTranslationProcessor();
    }

    @Override
    public String getHtml() {
        String title;
        String columnid;
        String columnid2;
        int i;
        long prevTime = System.currentTimeMillis();
        boolean __Debug = true;
        boolean expandPrimary = true;
        boolean expandDataset = true;
        boolean expandDataApprovals = true;
        boolean expandDataitemSpecs = true;
        boolean bShowTemplates = true;
        String keyid1 = "";
        String keyid2 = "";
        String keyid3 = "";
        String queryfrom = "";
        String querywhere = "";
        String queryorderby = "";
        String queryid = "";
        String param1 = "";
        String param2 = "";
        String param3 = "";
        String param4 = "";
        String param5 = "";
        String param6 = "";
        String param7 = "";
        String param8 = "";
        String param9 = "";
        String param10 = "";
        String param11 = "";
        String param12 = "";
        String paramlistidlist = "";
        String paramlistversionidlist = "";
        String variantidlist = "";
        String datasetlist = "";
        String requestitem = "";
        String expandText = this.__Tp.translate("Expand to see") + " ";
        String[] arrparams = new String[]{param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12};
        if (__Debug) {
            Trace.log("OPAL-INFO: Entering DataDetailBySDC.getHtml() ");
        }
        PropertyList pagedata = (PropertyList)this.pageContext.getAttribute("pagedata", 2);
        this.sdcId = this.element.getProperty("sdcid");
        if (this.sdcId == null || this.sdcId.trim().equalsIgnoreCase("")) {
            this.sdcId = pagedata.getProperty("sdcid");
        }
        if (__Debug) {
            Trace.log("OPAL-INFO: The SDC obtained is = " + this.sdcId);
        }
        keyid1 = pagedata.getProperty("keyid1");
        keyid2 = pagedata.getProperty("keyid2");
        keyid3 = pagedata.getProperty("keyid3");
        if (__Debug) {
            Trace.log("OPAL-INFO: keyid1sfrom the element  keyid1 = " + pagedata.getProperty("keyid1") + " keyid2 = " + pagedata.getProperty("keyid2") + " keyid3 = " + pagedata.getProperty("keyid3"));
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(this.sdcId);
        if (__Debug) {
            Trace.log("OPAL-INFO: sdcid from sdiRequest = " + sdiRequest.getSDCid());
        }
        sdiRequest.setKeyid1List(keyid1);
        sdiRequest.setKeyid2List(keyid2);
        sdiRequest.setKeyid3List(keyid3);
        sdiRequest.setQueryid(queryid);
        sdiRequest.setQueryFrom(queryfrom);
        sdiRequest.setQueryWhere(querywhere);
        sdiRequest.setQueryParams(arrparams);
        sdiRequest.setQueryOrderBy(queryorderby);
        sdiRequest.setParamlistidList(paramlistidlist);
        sdiRequest.setParamlistversionidList(paramlistversionidlist);
        sdiRequest.setVariantidList(variantidlist);
        sdiRequest.setDatasetList(datasetlist);
        sdiRequest.setShowTemplates(bShowTemplates);
        SDIProcessor sdiProcessor = new SDIProcessor(this.pageContext);
        requestitem = "primary";
        sdiRequest.setRequestItem(requestitem);
        SDIData sdiDataPrimary = sdiProcessor.getSDIData(sdiRequest);
        DataSet dsPrimary = sdiDataPrimary.getDataset("primary");
        requestitem = "dataset";
        sdiRequest.setRequestItem(requestitem);
        SDIData sdiDataDataset = sdiProcessor.getSDIData(sdiRequest);
        DataSet dsDataset = sdiDataDataset.getDataset("dataset");
        requestitem = "dataapproval";
        sdiRequest.setRequestItem(requestitem);
        SDIData sdiDataDataapproval = sdiProcessor.getSDIData(sdiRequest);
        DataSet dsDataApprovals = sdiDataDataapproval.getDataset("dataapproval");
        requestitem = "dataitem";
        sdiRequest.setRequestItem(requestitem);
        SDIData sdiDataDataitem = sdiProcessor.getSDIData(sdiRequest);
        DataSet dsDataitem = sdiDataDataitem.getDataset("dataitem");
        requestitem = "dataspec";
        sdiRequest.setRequestItem(requestitem);
        SDIData sdiDataDataitemSpecs = sdiProcessor.getSDIData(sdiRequest);
        DataSet dsDataitemSpecs = sdiDataDataitemSpecs.getDataset("dataspec");
        requestitem = "datalimit";
        sdiRequest.setRequestItem(requestitem);
        SDIData sdiDataDatalimits = sdiProcessor.getSDIData(sdiRequest);
        DataSet dsDataLimits = sdiDataDatalimits.getDataset("datalimit");
        if (dsDataLimits == null) {
            Trace.log("OPAL-INFO", "dsDataLimits is null ");
        }
        if (dsDataLimits != null) {
            Trace.log("OPAL-INFO", "dsDataLimits size = " + dsDataLimits.size());
        }
        if (dsDataApprovals == null) {
            Trace.log("OPAL-INFO", "dsDataApprovals is null ");
        }
        if (dsDataApprovals != null) {
            Trace.log("OPAL-INFO", "dsDataApprovals size = " + dsDataApprovals.size());
        }
        if (__Debug) {
            int i2;
            Trace.log("OPAL-INFO: Primary dataset size = " + dsPrimary.size());
            Trace.log("OPAL-INFO: Dataset dataset size = " + dsDataset.size());
            Trace.log("OPAL-INFO: Dataitem dataset size = " + dsDataitem.size());
            Trace.log("OPAL-INFO: keyid1 from sdiRequest = " + sdiRequest.getKeyid1List());
            String[] saDatasetNames = SDIData.getDatasetNames();
            for (i2 = 0; i2 < saDatasetNames.length; ++i2) {
                Trace.log("OPAL-INFO: SDIData Dataset Names = " + saDatasetNames[i2]);
            }
            for (i2 = 0; i2 < dsPrimary.size(); ++i2) {
                Trace.log("OPAL-INFO: Primary Dataset items for " + this.sdcId + " = " + dsPrimary.get(i2).toString());
            }
            for (i2 = 0; i2 < dsDataset.size(); ++i2) {
                Trace.log("OPAL-INFO: Dataset Dataset items = " + dsDataset.get(i2).toString());
            }
            String[] saColumns = dsPrimary.getColumns();
            for (int i3 = 0; i3 < saColumns.length; ++i3) {
                Trace.log("OPAL-INFO: Primary Dataset column names = " + saColumns[i3]);
            }
        }
        StringBuffer sbHtml = new StringBuffer("");
        PropertyListCollection plcPrimaryCols = this.element.getCollection("primarycols");
        PropertyListCollection plcDatasetCols = this.element.getCollection("datasetcols");
        PropertyListCollection plcDataitemCols = this.element.getCollection("dataitemcols");
        if (__Debug && plcPrimaryCols == null) {
            Trace.log("OPAL-INFO: columns PLC is null ! ");
        }
        if (__Debug) {
            Trace.log("OPAL-INFO: Primary Cols plc size = " + plcPrimaryCols.size());
            Trace.log("OPAL-INFO: Dataset Cols plc size = " + plcDatasetCols.size());
            Trace.log("OPAL-INFO: Dataitems Cols plc size = " + plcDataitemCols.size());
        }
        sbHtml.append("<p><b>" + this.element.getProperty("sdcid") + " No. " + pagedata.getProperty("keyid1") + "</b>");
        if (this.sdcId.trim().equalsIgnoreCase("sample")) {
            this.alPrimaryColumnIds.add("s_sampleid");
            this.alPrimaryColumnIds.add("sampledesc");
            this.alPrimaryColumnIds.add("samplestatus");
            this.alPrimaryColumnTitles.add(this.__Tp.translate("SampleId"));
            this.alPrimaryColumnTitles.add(this.__Tp.translate("Description"));
            this.alPrimaryColumnTitles.add(this.__Tp.translate("Status"));
            this.alDatasetColumnIds.add("paramlistid");
            this.alDatasetColumnIds.add("paramlistversionid");
            this.alDatasetColumnIds.add("variantid");
            this.alDatasetColumnIds.add("dataset");
            this.alDatasetColumnTitles.add(this.__Tp.translate("ParamList"));
            this.alDatasetColumnTitles.add(this.__Tp.translate("Ver."));
            this.alDatasetColumnTitles.add(this.__Tp.translate("Variant"));
            this.alDatasetColumnTitles.add(this.__Tp.translate("DS#"));
            this.alDataApprovalColumnIds.add("approvalstep");
            this.alDataApprovalColumnIds.add("roleid");
            this.alDataApprovalColumnIds.add("mandatoryflag");
            this.alDataApprovalColumnIds.add("approvalflag");
            this.alDataApprovalColumnTitles.add(this.__Tp.translate("Step"));
            this.alDataApprovalColumnTitles.add(this.__Tp.translate("Role"));
            this.alDataApprovalColumnTitles.add(this.__Tp.translate("Mandatory"));
            this.alDataApprovalColumnTitles.add(this.__Tp.translate("Result"));
            this.alDataitemColumnIds.add("paramid");
            this.alDataitemColumnIds.add("paramtype");
            this.alDataitemColumnIds.add("replicateid");
            this.alDataitemColumnIds.add("enteredtext");
            this.alDataitemColumnIds.add("displayvalue");
            this.alDataitemColumnIds.add("displayunits");
            this.alDataitemColumnIds.add("condition");
            this.alDataitemColumnTitles.add(this.__Tp.translate("Param"));
            this.alDataitemColumnTitles.add(this.__Tp.translate("Type"));
            this.alDataitemColumnTitles.add(this.__Tp.translate("Rep#"));
            this.alDataitemColumnTitles.add(this.__Tp.translate("Entered Data"));
            this.alDataitemColumnTitles.add(this.__Tp.translate("Display Data"));
            this.alDataitemColumnTitles.add(this.__Tp.translate("Unit"));
            this.alDataitemColumnTitles.add(this.__Tp.translate("Condition"));
            this.alDataitemSpecColumnIds.add("specid");
            this.alDataitemSpecColumnIds.add("specversionid");
            this.alDataitemSpecColumnIds.add("condition");
            this.alDataitemSpecColumnIds.add("val1");
            this.alDataitemSpecColumnIds.add("val2");
            this.alDataitemSpecColumnIds.add("unitsid");
            this.alDataitemSpecColumnIds.add("typecondition");
            this.alDataitemSpecColumnTitles.add(this.__Tp.translate("Spec"));
            this.alDataitemSpecColumnTitles.add(this.__Tp.translate("Ver."));
            this.alDataitemSpecColumnTitles.add(this.__Tp.translate("Result"));
            this.alDataitemSpecColumnTitles.add(this.__Tp.translate("Limit1"));
            this.alDataitemSpecColumnTitles.add(this.__Tp.translate("Limit2"));
            this.alDataitemSpecColumnTitles.add(this.__Tp.translate("Unit"));
            this.alDataitemSpecColumnTitles.add(this.__Tp.translate("LimitType"));
        }
        if (plcPrimaryCols != null) {
            for (i = 0; i < plcPrimaryCols.size(); ++i) {
                PropertyList plcolumn = plcPrimaryCols.getPropertyList(i);
                columnid2 = plcolumn.getProperty("columnid");
                String title2 = plcPrimaryCols.getPropertyList(i).getProperty("title");
                String string = title2 = columnid2.equalsIgnoreCase("") ? columnid2 : title2;
                if (columnid2.equalsIgnoreCase("") || this.alPrimaryColumnIds.contains(columnid2)) continue;
                this.alPrimaryColumnIds.add(columnid2);
                this.alPrimaryColumnTitles.add(title2);
            }
        }
        if (plcDatasetCols != null) {
            for (i = 0; i < plcDatasetCols.size(); ++i) {
                columnid = plcDatasetCols.getPropertyList(i).getProperty("columnid");
                title = plcDatasetCols.getPropertyList(i).getProperty("title");
                String string = title = columnid.equalsIgnoreCase("") ? columnid : title;
                if (__Debug) {
                    Trace.log("OPAL-INFO: Dataset columnid = " + columnid + " title = " + title);
                }
                if (columnid.equalsIgnoreCase("") || this.alDatasetColumnIds.contains(columnid)) continue;
                this.alDatasetColumnIds.add(columnid);
                this.alDatasetColumnTitles.add(title);
            }
        }
        if (plcDataitemCols != null) {
            for (i = 0; i < plcDataitemCols.size(); ++i) {
                columnid = plcDataitemCols.getPropertyList(i).getProperty("columnid");
                title = plcDataitemCols.getPropertyList(i).getProperty("title");
                String string = title = columnid.equalsIgnoreCase("") ? columnid : title;
                if (__Debug) {
                    Trace.log("OPAL-INFO: Dataitem columnid = " + columnid + " title = " + title);
                }
                if (columnid.equalsIgnoreCase("") || this.alDataitemColumnIds.contains(columnid)) continue;
                this.alDataitemColumnIds.add(columnid);
                this.alDataitemColumnTitles.add(title);
            }
        }
        String src = "WEB-OPAL/images/" + (expandPrimary ? "minus" : "plus") + ".gif";
        sbHtml.append("<table width=100% border=0>");
        sbHtml.append("<tr>");
        sbHtml.append("<td width=1 valign=top>");
        sbHtml.append("<img id=\"" + this.currentKeyid + "\" class=\"Outline\" style=\"cursor:pointer\" width=\"12\" height=\"12\" src=\"" + src + "\"> ");
        sbHtml.append("</td>\n");
        sbHtml.append("<td>");
        sbHtml.append("<table border=0 cellpadding=\"3\" cellspacing=\"0\" class=\"info_list_primarytable\"> \n ");
        sbHtml.append("<tr class=\"info_list_primaryheader\"> \n ");
        if (this.alPrimaryColumnTitles != null) {
            for (int i4 = 0; i4 < this.alPrimaryColumnTitles.size(); ++i4) {
                sbHtml.append("<td>" + (String)this.alPrimaryColumnTitles.get(i4) + "</td>");
            }
        }
        sbHtml.append("</tr>");
        sbHtml.append("<tr class=\"info_list_primaryrow\">");
        for (int i5 = 0; i5 < this.alPrimaryColumnIds.size(); ++i5) {
            columnid2 = (String)this.alPrimaryColumnIds.get(i5);
            String value = dsPrimary.getValue(0, columnid2, "&nbsp;");
            if (__Debug) {
                Trace.log("OPAL-INFO: Primary columnid:Dataset values = " + columnid2 + ":" + value);
            }
            sbHtml.append("<td height=10><b>" + value + "</b></td>");
        }
        sbHtml.append("</tr>");
        sbHtml.append("</table>");
        sbHtml.append("<div style=\"display:" + (expandPrimary ? "block" : "none") + "\" id=\"" + this.currentKeyid + "data\"> \n ");
        sbHtml.append("<table width=100%  cellspacing=0 cellpadding=0 class=\"info_list_datasettable\">\n");
        sbHtml.append("<tr class=\"info_list_datasetrow\">");
        for (int dsDatasetRow = 0; dsDatasetRow < dsDataset.size(); ++dsDatasetRow) {
            int i6;
            sbHtml.append("<tr> ");
            sbHtml.append("<td width=\"12\" valign=top> \n");
            this.currentKeyid = dsDataset.getString(dsDatasetRow, "keyid1");
            String srcDataset = "WEB-OPAL/images/" + (expandDataset ? "minus" : "plus") + ".gif";
            sbHtml.append("<img id=\"" + this.currentKeyid + "-" + dsDatasetRow + "\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + srcDataset + "\"> \n");
            sbHtml.append("</td>");
            sbHtml.append("<td width=\"*\">\n");
            sbHtml.append("<div style=\"display:" + (expandDataset ? "block" : "none") + "\" id=\"" + this.currentKeyid + "-" + dsDatasetRow + "text\"> \n");
            sbHtml.append("<table border=0 width=100% cellpadding=\"3\" cellspacing=\"0\">\n");
            sbHtml.append("<tr class=\"info_list_datasetheader\">\n");
            if (this.alDatasetColumnTitles != null) {
                for (int i7 = 0; i7 < this.alDatasetColumnTitles.size(); ++i7) {
                    sbHtml.append("<td>" + (String)this.alDatasetColumnTitles.get(i7) + "</td>");
                }
            }
            sbHtml.append("</tr>");
            sbHtml.append("<tr class=\"info_list_datasetrow\">");
            for (int i8 = 0; i8 < this.alDatasetColumnIds.size(); ++i8) {
                String columnid3 = (String)this.alDatasetColumnIds.get(i8);
                String value = dsDataset.getValue(dsDatasetRow, columnid3, "&nbsp;");
                if (__Debug) {
                    Trace.log("OPAL-INFO: Primary dataset columnid:value = " + columnid3 + ":" + value);
                }
                sbHtml.append("<td> " + value + "</td>");
            }
            sbHtml.append("</tr>");
            sbHtml.append("</table>");
            sbHtml.append("</div>");
            sbHtml.append("<div style=\"display:" + (expandDataset ? "none" : "block") + "\" id=\"" + this.currentKeyid + "-" + dsDatasetRow + "data\"> \n");
            sbHtml.append("<table border=0 width=100% cellpadding=\"3\" cellspacing=\"0\">\n");
            sbHtml.append("<tr>");
            sbHtml.append("<td width=* colspan=" + this.alDatasetColumnIds.size() + ">\n");
            sbHtml.append("<table border=0 bordercolor=green cellpadding=2 cellspacing=0> ");
            sbHtml.append("<tr>");
            sbHtml.append("<td width=12>&nbsp;</td>");
            String src_dataapproval = "WEB-OPAL/images/" + (expandDataApprovals ? "minus" : "plus") + ".gif";
            sbHtml.append("<td width=\"12\" valign=top>");
            sbHtml.append("<img id=\"" + this.currentKeyid + "-" + dsDatasetRow + "_approval_\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src_dataapproval + "\"></td>\n");
            sbHtml.append("<td>");
            sbHtml.append("<div style=\"display:" + (expandDataApprovals ? "none" : "block") + "; color:brown;\" id=\"" + this.currentKeyid + "-" + dsDatasetRow + "_approval_text\">\n");
            sbHtml.append(expandText + " " + this.__Tp.translate("approval(s)"));
            sbHtml.append("</div>\n");
            sbHtml.append("<div style=\"display:" + (expandDataApprovals ? "block" : "none") + "\" id=\"" + this.currentKeyid + "-" + dsDatasetRow + "_approval_data\">\n");
            sbHtml.append("<table border=0 cellpadding=4 cellspacing=0 class=\"info_list_dataapprovaltable\">\n");
            sbHtml.append("<tr class=\"info_list_dataapprovalheader\">\n");
            if (this.alDataApprovalColumnTitles != null) {
                for (int i9 = 0; i9 < this.alDataApprovalColumnTitles.size(); ++i9) {
                    sbHtml.append("<td>" + (String)this.alDataApprovalColumnTitles.get(i9) + "</td>");
                }
            }
            sbHtml.append("</tr>");
            DataSet dsFilteredDataApprovals = new DataSet();
            this.hmFilterMap.clear();
            this.hmFilterMap.put("keyid1", dsDataset.getString(dsDatasetRow, "keyid1"));
            this.hmFilterMap.put("keyid2", dsDataset.getString(dsDatasetRow, "keyid2"));
            this.hmFilterMap.put("keyid3", dsDataset.getString(dsDatasetRow, "keyid3"));
            this.hmFilterMap.put("paramlistid", dsDataset.getString(dsDatasetRow, "paramlistid"));
            this.hmFilterMap.put("paramlistversionid", dsDataset.getString(dsDatasetRow, "paramlistversionid"));
            this.hmFilterMap.put("variantid", dsDataset.getString(dsDatasetRow, "variantid"));
            this.hmFilterMap.put("dataset", dsDataset.getBigDecimal(dsDatasetRow, "dataset"));
            if (__Debug && dsFilteredDataApprovals == null) {
                Trace.log("\n OPAL-INFO : dsFilteredDataApprovals is null");
            }
            if (dsDataApprovals != null) {
                Trace.log("\n # OPAL-INFO : dsDataApprovals size = " + dsDataApprovals.size());
                Trace.log("\n # OPAL-INFO : dsFilteredDataApprovals size = " + dsFilteredDataApprovals.size());
                dsFilteredDataApprovals = dsDataApprovals.getFilteredDataSet(this.hmFilterMap);
            }
            sbHtml.append("<tr class=\"info_list_dataapprovalrow\"> \n ");
            for (i6 = 0; i6 < this.alDataApprovalColumnIds.size(); ++i6) {
                String columnid4 = (String)this.alDataApprovalColumnIds.get(i6);
                String value = dsDataApprovals.getValue(dsDatasetRow, columnid4, "&nbsp;");
                if (__Debug) {
                    Trace.log("OPAL-INFO: DataApproval columnids:values = " + columnid4 + ":" + value);
                }
                sbHtml.append("<td height=10>" + value + "</td>");
            }
            sbHtml.append("</tr>");
            sbHtml.append("</table>");
            sbHtml.append("</div>");
            sbHtml.append("</td>");
            sbHtml.append("</tr>");
            sbHtml.append("</table>");
            sbHtml.append("</td>");
            sbHtml.append("</tr>");
            sbHtml.append("<tr>");
            sbHtml.append("<td width=* colspan=" + this.alDatasetColumnIds.size() + ">\n");
            sbHtml.append("<table width=100% cellpadding=\"3\" border=0 cellspacing=\"0\" class=\"info_list_dataitemtable\"> \n ");
            sbHtml.append("<tr class=\"info_list_dataitemheader\"> \n ");
            sbHtml.append("<td width=\"1\">&nbsp;</td>\n");
            if (this.alDataitemColumnTitles != null) {
                for (i6 = 0; i6 < this.alDataitemColumnTitles.size(); ++i6) {
                    sbHtml.append("<td>" + (String)this.alDataitemColumnTitles.get(i6) + "</td>");
                }
            }
            sbHtml.append("</tr>");
            sbHtml.append("<tr class=\"info_list_dataitemrow\">");
            this.hmFilterMap.clear();
            this.hmFilterMap.put("keyid1", dsDataset.getString(dsDatasetRow, "keyid1"));
            this.hmFilterMap.put("keyid2", dsDataset.getString(dsDatasetRow, "keyid2"));
            this.hmFilterMap.put("keyid3", dsDataset.getString(dsDatasetRow, "keyid3"));
            this.hmFilterMap.put("paramlistid", dsDataset.getString(dsDatasetRow, "paramlistid"));
            this.hmFilterMap.put("paramlistversionid", dsDataset.getString(dsDatasetRow, "paramlistversionid"));
            this.hmFilterMap.put("variantid", dsDataset.getString(dsDatasetRow, "variantid"));
            this.hmFilterMap.put("dataset", dsDataset.getBigDecimal(dsDatasetRow, "dataset"));
            this.dsFilteredDataitems = null;
            this.dsFilteredDataitems = dsDataitem.getFilteredDataSet(this.hmFilterMap);
            Trace.log("OPAL-INFO: size of the filtered Dataitem dataset = " + this.dsFilteredDataitems.size() + " & Dataset Number, ie. dsDatasetRow = " + dsDatasetRow);
            Trace.log("OPAL-INFO: hmFitlerMap = " + this.hmFilterMap);
            for (int dsDataitemRow = 0; dsDataitemRow < this.dsFilteredDataitems.size(); ++dsDataitemRow) {
                sbHtml.append("<td width=\"20\">");
                sbHtml.append("<img src=\"WEB-CORE/images/gif/Sample.gif\" height=15 width=15></td>");
                Trace.log("OPAL-INFO: Primary Dataset columnid : value = ");
                for (int i10 = 0; i10 < this.alDataitemColumnIds.size(); ++i10) {
                    String columnid5 = (String)this.alDataitemColumnIds.get(i10);
                    String value = this.dsFilteredDataitems.getValue(dsDataitemRow, columnid5, "&nbsp;");
                    if (__Debug) {
                        this.logger.debug(" Filtered Dataitems " + columnid5 + " : " + value + " | ");
                    }
                    sbHtml.append("<td>" + value + "</td>");
                }
                sbHtml.append("</tr>");
                this.hmFilterMap.clear();
                this.hmFilterMap.put("keyid1", dsDataitem.getString(dsDataitemRow, "keyid1"));
                this.hmFilterMap.put("keyid2", dsDataitem.getString(dsDataitemRow, "keyid2"));
                this.hmFilterMap.put("keyid3", dsDataitem.getString(dsDataitemRow, "keyid3"));
                this.hmFilterMap.put("paramlistid", dsDataitem.getString(dsDataitemRow, "paramlistid"));
                this.hmFilterMap.put("paramlistversionid", dsDataitem.getString(dsDataitemRow, "paramlistversionid"));
                this.hmFilterMap.put("variantid", dsDataitem.getString(dsDataitemRow, "variantid"));
                this.hmFilterMap.put("dataset", dsDataitem.getBigDecimal(dsDataitemRow, "dataset"));
                this.hmFilterMap.put("paramid", dsDataitem.getString(dsDataitemRow, "paramid"));
                this.hmFilterMap.put("paramtype", dsDataitem.getString(dsDataitemRow, "paramtype"));
                this.hmFilterMap.put("replicateid", dsDataitem.getBigDecimal(dsDataitemRow, "replicateid"));
                if (__Debug && dsDataitemSpecs == null) {
                    Trace.log("\n OPAL-INFO : dsDataitemSpecs is null");
                }
                if (dsDataitemSpecs != null) {
                    Trace.log("\n OPAL-INFO : dsDataitemSpecs is not null ");
                    this.dsFilteredDataitemSpecs = dsDataitemSpecs.getFilteredDataSet(this.hmFilterMap);
                }
                sbHtml.append("<tr>");
                sbHtml.append("<td width=* colspan=" + this.alDataitemColumnIds.size() + ">\n");
                if (dsDataitemSpecs.size() > 0) {
                    sbHtml.append("<table border=0 bordercolor=green cellpadding=2 cellspacing=0> ");
                    sbHtml.append("<tr>");
                    String src_dataitemspec = "WEB-OPAL/images/" + (expandDataitemSpecs ? "minus" : "plus") + ".gif";
                    sbHtml.append("<td width=\"12\" valign=top>");
                    sbHtml.append("<img id=\"" + this.currentKeyid + "-" + dsDatasetRow + "-" + dsDataitemRow + "_spec_\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src_dataitemspec + "\"> ");
                    sbHtml.append("</td> \n");
                    sbHtml.append("<td>");
                    sbHtml.append("<div style=\"display:" + (expandDataitemSpecs ? "none" : "block") + "; color:brown;\"  id=\"" + this.currentKeyid + "-" + dsDatasetRow + "-" + dsDataitemRow + "_spec_text\">\n");
                    sbHtml.append(expandText + " " + this.__Tp.translate("spec(s)"));
                    sbHtml.append("</div>");
                    sbHtml.append("<div style=\"display:" + (expandDataitemSpecs ? "block" : "none") + "; color:brown;\"  id=\"" + this.currentKeyid + "-" + dsDatasetRow + "-" + dsDataitemRow + "_spec_data\">\n");
                    for (int dsDataitemSpecRow = 0; dsDataitemSpecRow < this.dsFilteredDataitemSpecs.size(); ++dsDataitemSpecRow) {
                        sbHtml.append("<table cellspacing=0 cellpadding=4 border=1 class=\"info_list_dataspectable\">\n");
                        sbHtml.append("<tr class=\"info_list_dataspecheader\"> \n ");
                        for (int j = 0; j < this.alDataitemSpecColumnIds.size(); ++j) {
                            sbHtml.append("<td>" + (String)this.alDataitemSpecColumnTitles.get(j) + "</td>\n");
                        }
                        sbHtml.append("</tr> \n");
                        sbHtml.append("<tr class=\"info_list_dataspecrow\"> \n ");
                        for (int i11 = 0; i11 < this.alDataitemSpecColumnIds.size(); ++i11) {
                            String columnid6 = (String)this.alDataitemSpecColumnIds.get(i11);
                            String value = this.dsFilteredDataitemSpecs.getValue(dsDataitemSpecRow, columnid6, "&nbsp;");
                            if (__Debug) {
                                Trace.log("OPAL-INFO: DataitemSpec columnids:values = " + columnid6 + ":" + value);
                            }
                            sbHtml.append("<td height=10>" + value + "</td>");
                        }
                        sbHtml.append("</td>");
                        sbHtml.append("</tr>");
                        sbHtml.append("</table>");
                    }
                    sbHtml.append("</div>");
                    sbHtml.append("</td> ");
                    sbHtml.append("</tr>");
                    sbHtml.append("</table>");
                }
                sbHtml.append("</td></tr>");
            }
            sbHtml.append("</table>");
            sbHtml.append("</td>");
            sbHtml.append("</tr>");
            sbHtml.append("</table>");
            sbHtml.append("</div> ");
        }
        sbHtml.append("</td>");
        sbHtml.append("</tr>");
        sbHtml.append("</table>");
        sbHtml.append("</div>");
        sbHtml.append("</td>");
        sbHtml.append("</tr>");
        sbHtml.append("</table>");
        if (__Debug) {
            long currTime = System.currentTimeMillis();
            Trace.log("OPAL_INFO: sdidetailview Element using SDIProcessor took " + (currTime - prevTime) + " ms.");
            sbHtml.append("sdidetailview Element using SDIProcessor took " + (currTime - prevTime) + " ms.");
        }
        return sbHtml.toString();
    }
}

