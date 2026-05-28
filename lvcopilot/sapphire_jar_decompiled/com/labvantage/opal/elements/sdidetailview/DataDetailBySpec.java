/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.sdidetailview;

import com.labvantage.opal.elements.sdidetailview.DataDetailUtil;
import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataDetailBySpec
extends BaseElement {
    private String LABVANTAGE_CVS_ID = "$Revision: 67311 $";
    private TranslationProcessor __Tp;
    private QueryProcessor __qp;
    private SDCProcessor __sdcProcessor;
    private SQLGenerator __SqlGenerator;
    private boolean __expandPrimary = true;
    private boolean __expandSpecs = true;
    private boolean __expandDataApprovals = true;
    private boolean __expandDataset = true;
    private boolean __expandSingleDataitem = true;
    private boolean __expandDataitems = true;
    private boolean __expandDataitemSpecs = true;
    protected int _dataitemspecCount = 0;
    protected String _expandText = "";
    protected String _sdcId = "";
    protected String _keyid1 = "";
    protected String _keyid2 = "";
    protected String _keyid3 = "";
    protected String _currentKeyid = "";
    protected String _dataset = "";
    protected String _paramid = "";
    protected String _paramlistid = "";
    protected String _paramlistversionid = "";
    protected String _paramtype = "";
    protected String _variantid = "";
    protected String _replicateid = "";
    protected String _specid = "";
    protected String _specversionid = "";
    protected String _colId = "";
    protected String _colTitle = "";
    protected String _tableid = "";
    protected String _keyColId1 = "";
    protected String _descCol = "";
    protected String _singular = "";
    protected HashMap _hmSDCProperties = new HashMap();
    protected DataSet _dsPrimary = new DataSet();
    protected DataSet _dsSpec = new DataSet();
    protected DataSet _dsSpecParamLimits = new DataSet();
    protected DataSet _dsDataset = new DataSet();
    protected DataSet _dsDataitem = new DataSet();
    protected DataSet _dsApprovals = new DataSet();
    protected DataSet _dsDataitemLimits = new DataSet();
    protected DataSet _dsDataitemSpecs = new DataSet();
    protected DataSet _dsFilteredDataApprovals = new DataSet();
    protected DataSet _dsFilteredDataitemSpecs = new DataSet();
    protected DataSet _dsFilteredDataitems = new DataSet();
    protected DataSet _dsFilteredDataSet = new DataSet();
    protected DataSet _dsTableid = new DataSet();
    protected HashMap _hmFilterMap = new HashMap();
    protected HashMap _hmFilterDISpecs = new HashMap();
    protected ArrayList _alPrimaryColumnIds = new ArrayList();
    protected ArrayList _alPrimaryColumnTitles = new ArrayList();
    protected ArrayList _alSpecColumnIds = new ArrayList();
    protected ArrayList _alSpecColumnTitles = new ArrayList();
    protected ArrayList _alDataApprovalColumnIds = new ArrayList();
    protected ArrayList _alDataApprovalColumnTitles = new ArrayList();
    protected ArrayList _alDatasetColumnIds = new ArrayList();
    protected ArrayList _alDatasetColumnTitles = new ArrayList();
    protected ArrayList _alDataitemColumnIds = new ArrayList();
    protected ArrayList _alDataitemColumnTitles = new ArrayList();
    protected ArrayList _alDataapprovalColumnIds = new ArrayList();
    protected ArrayList _alDataapprovalColumnTitles = new ArrayList();
    protected ArrayList _alDataitemSpecColumnIds = new ArrayList();
    protected ArrayList _alDataitemSpecColumnTitles = new ArrayList();
    protected ArrayList _alDataitemLimitColumnIds = new ArrayList();
    protected ArrayList _alDataitemLimitColumnTitles = new ArrayList();
    protected ArrayList _KeyCols = new ArrayList();
    protected ArrayList _UniqueCols = new ArrayList();
    public PropertyListCollection plcPrimaryCols = new PropertyListCollection();
    public PropertyListCollection plcDatasetCols = new PropertyListCollection();
    public PropertyListCollection plcDataitemCols = new PropertyListCollection();

    private void setTranslationProcessor() {
        this.__Tp = this.getTranslationProcessor();
    }

    public DataDetailBySpec(PropertyList element) {
        this.element = element;
        this.setTranslationProcessor();
    }

    public DataDetailBySpec(PageContext pageContext, String connectionid) {
        this.setPageContext(pageContext);
        this.setConnectionId(connectionid);
        this.setTranslationProcessor();
    }

    @Override
    public String getHtml() {
        int i;
        int i2;
        StringBuffer sbHtml = new StringBuffer();
        long prevTime = System.currentTimeMillis();
        this._expandText = this.__Tp.translate("Expand to see") + " ";
        this.__SqlGenerator = SQLFactory.getSqlGenerator(this.pageContext);
        PropertyList pagedata = (PropertyList)this.pageContext.getAttribute("pagedata", 2);
        this._sdcId = this.element.getProperty("sdcid");
        this._keyid1 = pagedata.getProperty("keyid1");
        this._keyid2 = pagedata.getProperty("keyid2");
        this._keyid3 = pagedata.getProperty("keyid3");
        this._dataset = pagedata.getProperty("dataset");
        this._paramid = pagedata.getProperty("paramid");
        this._paramlistid = pagedata.getProperty("paramlistid");
        this._paramlistversionid = pagedata.getProperty("paramlistversionid");
        this._paramtype = pagedata.getProperty("paramtype");
        this._variantid = pagedata.getProperty("variantid");
        this._replicateid = pagedata.getProperty("replicateid");
        this._specid = pagedata.getProperty("specid");
        this._specversionid = pagedata.getProperty("specversionid");
        if (this._sdcId == null || this._sdcId.trim().equalsIgnoreCase("")) {
            this._sdcId = pagedata.getProperty("sdcid");
        }
        if (this._sdcId == null || this._sdcId.trim().equalsIgnoreCase("")) {
            this._sdcId = "Sample";
        }
        this.__qp = new QueryProcessor(this.pageContext);
        this.__sdcProcessor = this.getSDCProcessor();
        this._hmSDCProperties = this.__sdcProcessor.getSDCProperties(this._sdcId);
        this._tableid = (String)this._hmSDCProperties.get("tableid");
        this._keyColId1 = (String)this._hmSDCProperties.get("keycolid1");
        this._descCol = (String)this._hmSDCProperties.get("desccol");
        this._singular = (String)this._hmSDCProperties.get("singular");
        this._alPrimaryColumnIds.add(this._keyColId1);
        this._alPrimaryColumnIds.add(this._descCol);
        this._alPrimaryColumnIds.add("usersequence");
        this._alPrimaryColumnTitles.add(this.__Tp.translate(this._singular.substring(0, 1).toUpperCase() + this._singular.substring(1) + "Id"));
        this._alPrimaryColumnTitles.add(this.__Tp.translate("Description"));
        this._alPrimaryColumnTitles.add(this.__Tp.translate("User Sequence"));
        this._alSpecColumnIds.add("specid");
        this._alSpecColumnIds.add("specdesc");
        this._alSpecColumnIds.add("specversionid");
        this._alSpecColumnTitles.add(this.__Tp.translate("Spec Id"));
        this._alSpecColumnTitles.add(this.__Tp.translate("Spec Description"));
        this._alSpecColumnTitles.add(this.__Tp.translate("Spec Version Id"));
        this._alDatasetColumnIds.add("paramlistid");
        this._alDatasetColumnIds.add("variantid");
        this._alDatasetColumnIds.add("dataset");
        this._alDatasetColumnTitles.add(this.__Tp.translate("ParamList"));
        this._alDatasetColumnTitles.add(this.__Tp.translate("Variant"));
        this._alDatasetColumnTitles.add(this.__Tp.translate("Instance"));
        this._alDataApprovalColumnIds.add("approvalstep");
        this._alDataApprovalColumnIds.add("roleid");
        this._alDataApprovalColumnIds.add("mandatoryflag");
        this._alDataApprovalColumnIds.add("approvalflag");
        this._alDataApprovalColumnTitles.add(this.__Tp.translate("Step"));
        this._alDataApprovalColumnTitles.add(this.__Tp.translate("Role"));
        this._alDataApprovalColumnTitles.add(this.__Tp.translate("Mandatory"));
        this._alDataApprovalColumnTitles.add(this.__Tp.translate("Result"));
        this._alDataitemColumnIds.add("paramid");
        this._alDataitemColumnIds.add("paramtype");
        this._alDataitemColumnIds.add("replicateid");
        this._alDataitemColumnIds.add("enteredtext");
        this._alDataitemColumnIds.add("displayvalue");
        this._alDataitemColumnIds.add("displayunits");
        this._alDataitemColumnIds.add("condition");
        this._alDataitemColumnTitles.add(this.__Tp.translate("Param"));
        this._alDataitemColumnTitles.add(this.__Tp.translate("Type"));
        this._alDataitemColumnTitles.add(this.__Tp.translate("Rep#"));
        this._alDataitemColumnTitles.add(this.__Tp.translate("Entered Data"));
        this._alDataitemColumnTitles.add(this.__Tp.translate("Display Data"));
        this._alDataitemColumnTitles.add(this.__Tp.translate("Unit"));
        this._alDataitemColumnTitles.add(this.__Tp.translate("Condition"));
        this._alDataitemSpecColumnIds.add("specid");
        this._alDataitemSpecColumnIds.add("specversionid");
        this._alDataitemSpecColumnIds.add("condition");
        this._alDataitemSpecColumnIds.add("val1");
        this._alDataitemSpecColumnIds.add("val2");
        this._alDataitemSpecColumnIds.add("unitsid");
        this._alDataitemSpecColumnIds.add("typecondition");
        this._alDataitemSpecColumnTitles.add(this.__Tp.translate("Spec"));
        this._alDataitemSpecColumnTitles.add(this.__Tp.translate("Ver."));
        this._alDataitemSpecColumnTitles.add(this.__Tp.translate("Result"));
        this._alDataitemSpecColumnTitles.add(this.__Tp.translate("Limit1"));
        this._alDataitemSpecColumnTitles.add(this.__Tp.translate("Limit2"));
        this._alDataitemSpecColumnTitles.add(this.__Tp.translate("Unit"));
        this._alDataitemSpecColumnTitles.add(this.__Tp.translate("LimitType"));
        this._alDataitemLimitColumnIds.add("limittypeid");
        this._alDataitemLimitColumnIds.add("operator");
        this._alDataitemLimitColumnIds.add("value1");
        this._alDataitemLimitColumnIds.add("value2");
        this._alDataitemLimitColumnIds.add("unitsid ");
        this._alDataitemLimitColumnTitles.add(this.__Tp.translate("LimitType"));
        this._alDataitemLimitColumnTitles.add(this.__Tp.translate("Operator"));
        this._alDataitemLimitColumnTitles.add(this.__Tp.translate("Value1"));
        this._alDataitemLimitColumnTitles.add(this.__Tp.translate("Value2"));
        this._alDataitemLimitColumnTitles.add(this.__Tp.translate("Unit"));
        this._KeyCols.add("sdcid");
        this._KeyCols.add("keyid2");
        this._KeyCols.add("keyid3");
        this._KeyCols.add("usersequence");
        this.plcPrimaryCols = this.element.getCollection("primarycols");
        this.plcDatasetCols = this.element.getCollection("datasetcols");
        this.plcDataitemCols = this.element.getCollection("dataitemcols");
        if (this.plcPrimaryCols == null) {
            this.plcPrimaryCols = new PropertyListCollection();
        }
        if (this.plcDatasetCols == null) {
            this.plcDatasetCols = new PropertyListCollection();
        }
        if (this.plcDataitemCols == null) {
            this.plcDataitemCols = new PropertyListCollection();
        }
        for (i2 = 0; i2 < this.plcPrimaryCols.size(); ++i2) {
            this._colId = this.plcPrimaryCols.getPropertyList(i2).getProperty("columnid");
            this._colTitle = this.plcPrimaryCols.getPropertyList(i2).getProperty("title");
            String string = this._colTitle = this._colTitle.equalsIgnoreCase("") ? this._colId : this._colTitle;
            if (this._colId.equalsIgnoreCase(" ") || this._alPrimaryColumnIds.contains(this._colId)) continue;
            this._alPrimaryColumnIds.add(this._colId);
            this._alPrimaryColumnTitles.add(this._colTitle);
        }
        for (i2 = 0; i2 < this.plcDatasetCols.size(); ++i2) {
            this._colId = this.plcDatasetCols.getPropertyList(i2).getProperty("columnid");
            this._colTitle = this.plcDatasetCols.getPropertyList(i2).getProperty("title");
            String string = this._colTitle = this._colTitle.equalsIgnoreCase("") ? this._colId : this._colTitle;
            if (this._colId.equalsIgnoreCase("") || this._alDatasetColumnIds.contains(this._colId)) continue;
            this._alDatasetColumnIds.add(this._colId);
            this._alDatasetColumnTitles.add(this._colTitle);
        }
        for (i2 = 0; i2 < this.plcDataitemCols.size(); ++i2) {
            this._colId = this.plcDataitemCols.getPropertyList(i2).getProperty("columnid");
            this._colTitle = this.plcDataitemCols.getPropertyList(i2).getProperty("title");
            String string = this._colTitle = this._colTitle.equalsIgnoreCase("") ? this._colId : this._colTitle;
            if (this._colId.equalsIgnoreCase("") || this._alDataitemColumnIds.contains(this._colId) || this._alDatasetColumnIds.contains(this._colId)) continue;
            this._alDataitemColumnIds.add(this._colId);
            this._alDataitemColumnTitles.add(this._colTitle);
        }
        String primarySelectList = DataDetailUtil.getColList(this._alPrimaryColumnIds);
        String sampleIdWhere = " where " + this._keyColId1 + " = ? ";
        String _sbPrimarySql = this.__SqlGenerator.getPrimarySql(primarySelectList, sampleIdWhere, this._tableid, this._sdcId, this._keyColId1);
        SafeSQL _sbSpecSql = this.__SqlGenerator.getSpecSql(this._keyid1, this._paramlistid, this._paramid);
        this._dsPrimary = this.__qp.getPreparedSqlDataSet(_sbPrimarySql, new Object[]{this._keyid1});
        this._dsSpec = this.__qp.getPreparedSqlDataSet(_sbSpecSql.getPreparedSQL(), _sbSpecSql.getValues());
        sbHtml.append("\t \t </td>");
        sbHtml.append("\t </tr>");
        sbHtml.append("</table>");
        sbHtml.append("<table cellpadding=5 cellspacing=0 valign=\"top\"  style=\"").append(this.browser.isIE() ? "position: fixed;" : "").append("top: 50px; margin-top: 10px; vertical-align: top; \" >");
        sbHtml.append("\t <tr>");
        sbHtml.append("\t \t <td>");
        sbHtml.append("<p><b>" + this.__Tp.translate(this._sdcId.substring(0, 1).toUpperCase() + this._sdcId.substring(1) + " No. ") + pagedata.getProperty("keyid1") + "</b>");
        String src = "WEB-OPAL/images/" + (this.__expandPrimary ? "minus" : "plus") + ".gif";
        sbHtml.append("<table width=\"100%\" border=0 valign=\"top\">");
        sbHtml.append("<tr>");
        sbHtml.append("<td width=1 valign=top>");
        sbHtml.append("<img id=\"" + this._currentKeyid + "\" class=\"Outline\" style=\"cursor:pointer\" width=\"12\" height=\"12\" src=\"" + src + "\"> ");
        sbHtml.append("</td>\n");
        sbHtml.append("<td>");
        sbHtml.append("<table border=0 cellpadding=\"3\" cellspacing=\"0\" class=\"info_list_primarytable\"  width=\"100%\" align=\"top\"> \n ");
        sbHtml.append("<tr class=\"info_list_primaryheader\"> \n ");
        if (this._alPrimaryColumnTitles != null) {
            for (i = 0; i < this._alPrimaryColumnTitles.size(); ++i) {
                sbHtml.append("<td>" + (String)this._alPrimaryColumnTitles.get(i) + "</td>");
            }
        }
        sbHtml.append("</tr> \n");
        sbHtml.append("<tr class=\"info_list_primaryrow\">");
        for (i = 0; i < this._alPrimaryColumnIds.size(); ++i) {
            String columnid = (String)this._alPrimaryColumnIds.get(i);
            String value = this._dsPrimary.getValue(0, columnid, "&nbsp;");
            sbHtml.append("<td height=10><b>" + value + "</b></td>");
        }
        sbHtml.append("</tr>");
        sbHtml.append("</table>");
        sbHtml.append("<div style=\"display:" + (this.__expandPrimary ? "block" : "none") + "\" id=\"" + this._currentKeyid + "data\"> \n ");
        for (int dsSpecRow = 0; dsSpecRow < this._dsSpec.size(); ++dsSpecRow) {
            int i3;
            this._specid = this._dsSpec.getValue(dsSpecRow, "specid", "&nbsp;");
            this._dataset = pagedata.getProperty("dataset");
            this._paramid = pagedata.getProperty("paramid");
            this._paramlistid = pagedata.getProperty("paramlistid");
            this._paramlistversionid = pagedata.getProperty("paramlistversionid");
            this._paramtype = pagedata.getProperty("paramtype");
            this._variantid = pagedata.getProperty("variantid");
            this._replicateid = pagedata.getProperty("replicateid");
            SafeSQL _sbDatasetSql = this.__SqlGenerator.getDatasetSql(this._keyid1, this._paramlistid, this._paramlistversionid, this._variantid, this._dataset, this._specid);
            SafeSQL _sbDataApprovalSql = this.__SqlGenerator.getApprovalSql(this._sdcId, this._keyid1, this._paramlistid, this._paramlistversionid, this._variantid, this._dataset);
            SafeSQL _sbDataitemSql = this.__SqlGenerator.getDataitemSql(this._sdcId, this._keyid1, this._specid, this._paramid, this._paramlistid, this._paramlistversionid, this._variantid, this._dataset, this._paramtype, this._replicateid);
            SafeSQL _sbSpecParamLimitsSql = this.__SqlGenerator.getSpecParamLimitsSql(this._keyid1);
            this.logger.debug("About to execute SQL statements .... ");
            this._dsSpecParamLimits = this.__qp.getPreparedSqlDataSet(_sbSpecParamLimitsSql.getPreparedSQL(), _sbSpecParamLimitsSql.getValues());
            this._dsDataset = this.__qp.getPreparedSqlDataSet(_sbDatasetSql.getPreparedSQL(), _sbDatasetSql.getValues());
            this._dsApprovals = this.__qp.getPreparedSqlDataSet(_sbDataApprovalSql.getPreparedSQL(), _sbDataApprovalSql.getValues());
            this._dsDataitem = this.__qp.getPreparedSqlDataSet(_sbDataitemSql.getPreparedSQL(), _sbDataitemSql.getValues());
            this.logger.debug("Completed Execution of SQL Statements");
            sbHtml.append("<table border=0 bordercolor=green cellpadding=2 cellspacing=0 width=\"100%\"> \n");
            sbHtml.append("<tr>");
            sbHtml.append("<td width=12>&nbsp;</td> \n");
            String src_dataapproval = "WEB-OPAL/images/" + (this.__expandSpecs ? "minus" : "plus") + ".gif";
            sbHtml.append("<td width=\"12\" valign=top> \n");
            sbHtml.append("<img id=\"" + this._currentKeyid + "-" + dsSpecRow + "_spec_\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src_dataapproval + "\"></td>\n");
            sbHtml.append("<td>");
            sbHtml.append("<div style=\"display:" + (this.__expandSpecs ? "none" : "block") + "; color:brown;\" id=\"" + this._currentKeyid + "-" + dsSpecRow + "_spec_text\">\n");
            sbHtml.append(this._expandText + " <b> " + this.__Tp.translate(this._specid) + " </b> " + this.__Tp.translate("Specification"));
            sbHtml.append("</div>\n");
            sbHtml.append("<div style=\"display:" + (this.__expandSpecs ? "block" : "none") + "\" id=\"" + this._currentKeyid + "-" + dsSpecRow + "_spec_data\">\n");
            sbHtml.append("<table cellspacing=0 cellpadding=4 class=\"info_list_dataspectable\">\n");
            sbHtml.append("<tr class=\"info_list_dataspecheader\">\n");
            if (this._alSpecColumnTitles != null) {
                for (i3 = 0; i3 < this._alSpecColumnTitles.size(); ++i3) {
                    sbHtml.append("<td>" + (String)this._alSpecColumnTitles.get(i3) + "</td>");
                }
            }
            sbHtml.append("</tr>");
            sbHtml.append("<tr class=\"info_list_dataspecrow\"> \n");
            for (i3 = 0; i3 < this._alSpecColumnIds.size(); ++i3) {
                String columnid = (String)this._alSpecColumnIds.get(i3);
                String value = this._dsSpec.getValue(dsSpecRow, columnid, "&nbsp;");
                sbHtml.append("<td height=10><b>" + value + "</b></td>");
            }
            sbHtml.append("</tr>");
            sbHtml.append("</table> \n");
            this._hmFilterMap.clear();
            this._hmFilterMap.put("keyid1", this._dsSpec.getString(dsSpecRow, "keyid1"));
            this._hmFilterMap.put("keyid2", this._dsSpec.getString(dsSpecRow, "keyid2"));
            this._hmFilterMap.put("keyid3", this._dsSpec.getString(dsSpecRow, "keyid3"));
            this._hmFilterMap.put("specid", this._dsSpec.getString(dsSpecRow, "specid"));
            if (this._dsDataset != null) {
                this._dsFilteredDataSet = this._dsDataset.getFilteredDataSet(this._hmFilterMap);
            }
            sbHtml.append("<table width=100%  cellspacing=0 cellpadding=0 class=\"info_list_datasettable\">\n");
            sbHtml.append("<tr class=\"info_list_datasetrow\"> \n");
            for (int dsDatasetRow = 0; dsDatasetRow < this._dsFilteredDataSet.size(); ++dsDatasetRow) {
                sbHtml.append("<tr> \n");
                sbHtml.append("<td width=\"12\" valign=top> \n");
                this._currentKeyid = this._dsDataset.getString(dsDatasetRow, "keyid1");
                String srcDataset = "WEB-OPAL/images/" + (this.__expandDataset ? "plus" : "minus") + ".gif";
                sbHtml.append("<img id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + srcDataset + "\"> \n");
                sbHtml.append("</td>");
                sbHtml.append("<td width=\"*\">\n");
                sbHtml.append("<div style=\"display:" + (this.__expandDataset ? "block" : "none") + "\" id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "text\"> \n");
                sbHtml.append("<table border=0 width=100% cellpadding=\"3\" cellspacing=\"0\">\n");
                sbHtml.append("<tr class=\"info_list_datasetheader\">\n");
                if (this._alDatasetColumnTitles != null) {
                    for (int i4 = 0; i4 < this._alDatasetColumnTitles.size(); ++i4) {
                        sbHtml.append("<td>" + (String)this._alDatasetColumnTitles.get(i4) + "</td>");
                    }
                }
                sbHtml.append("</tr>");
                sbHtml.append("<tr class=\"info_list_datasetrow\"> \n");
                for (int i5 = 0; i5 < this._alDatasetColumnIds.size(); ++i5) {
                    String columnid = (String)this._alDatasetColumnIds.get(i5);
                    String value = this._dsFilteredDataSet.getValue(dsDatasetRow, columnid, "&nbsp;");
                    sbHtml.append("<td> " + value + "</td>");
                }
                sbHtml.append("</tr>");
                sbHtml.append("</table>");
                sbHtml.append("</div>");
                sbHtml.append("<div style=\"display:" + (this.__expandDataset ? "none" : "block") + "\" id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "data\"> \n");
                sbHtml.append("<table border=0 width=100% cellpadding=\"3\" cellspacing=\"0\">\n");
                sbHtml.append("<tr>");
                sbHtml.append("<td width=* colspan=" + this._alDatasetColumnIds.size() + ">\n");
                this._hmFilterMap.clear();
                this._hmFilterMap.put("keyid1", this._dsDataset.getString(dsDatasetRow, "keyid1"));
                this._hmFilterMap.put("keyid2", this._dsDataset.getString(dsDatasetRow, "keyid2"));
                this._hmFilterMap.put("keyid3", this._dsDataset.getString(dsDatasetRow, "keyid3"));
                this._hmFilterMap.put("paramlistid", this._dsDataset.getString(dsDatasetRow, "paramlistid"));
                this._hmFilterMap.put("paramlistversionid", this._dsDataset.getString(dsDatasetRow, "paramlistversionid"));
                this._hmFilterMap.put("variantid", this._dsDataset.getString(dsDatasetRow, "variantid"));
                this._hmFilterMap.put("dataset", this._dsDataset.getBigDecimal(dsDatasetRow, "dataset"));
                if (this._dsApprovals != null) {
                    this._dsFilteredDataApprovals = this._dsApprovals.getFilteredDataSet(this._hmFilterMap);
                }
                if (this._dsFilteredDataApprovals.size() > 0 && this._dsFilteredDataApprovals != null && !this._dsFilteredDataApprovals.isNull(dsDatasetRow, (String)this._alDataApprovalColumnIds.get(0))) {
                    sbHtml.append("<table border=0 bordercolor=green cellpadding=2 cellspacing=0> \n");
                    sbHtml.append("<tr> \n");
                    sbHtml.append("<td width=12>&nbsp;</td> \n");
                    String src_dataapprovals = "WEB-OPAL/images/" + (this.__expandDataApprovals ? "minus" : "plus") + ".gif";
                    sbHtml.append("<td width=\"12\" valign=top> \n");
                    sbHtml.append("<img id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "_approval_\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src_dataapprovals + "\"></td>\n");
                    sbHtml.append("<td>");
                    sbHtml.append("<div style=\"display:" + (this.__expandDataApprovals ? "none" : "block") + "; color:brown;\" id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "_approval_text\">\n");
                    sbHtml.append(this._expandText + " " + this.__Tp.translate("Approval(s)"));
                    sbHtml.append("</div>\n");
                    sbHtml.append("<div style=\"display:" + (this.__expandDataApprovals ? "block" : "none") + "\" id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "_approval_data\">\n");
                    sbHtml.append("<table border=0 cellpadding=4 cellspacing=0 class=\"info_list_dataapprovaltable\">\n");
                    sbHtml.append("<tr class=\"info_list_dataapprovalheader\">\n");
                    if (this._alDataApprovalColumnTitles != null) {
                        for (int i6 = 0; i6 < this._alDataApprovalColumnTitles.size(); ++i6) {
                            sbHtml.append("<td>" + (String)this._alDataApprovalColumnTitles.get(i6) + "</td>");
                        }
                    }
                    sbHtml.append("</tr>");
                    for (int dataapprovalRow = 0; dataapprovalRow < this._dsFilteredDataApprovals.size(); ++dataapprovalRow) {
                        sbHtml.append("<tr class=\"info_list_dataapprovalrow\"> \n ");
                        for (int i7 = 0; i7 < this._alDataApprovalColumnIds.size(); ++i7) {
                            String columnid = (String)this._alDataApprovalColumnIds.get(i7);
                            String value = this._dsFilteredDataApprovals.getValue(dataapprovalRow, columnid, "&nbsp;");
                            if (((String)this._alDataApprovalColumnIds.get(i7)).equalsIgnoreCase("approvalflag")) {
                                sbHtml.append("<td align=center><img src=\"WEB-OPAL/elements/sdidetailview/images/" + this._dsFilteredDataApprovals.getString(dataapprovalRow, columnid));
                                sbHtml.append(".gif\" border=0 height=14 title=\"ApprovalFlag = " + this._dsFilteredDataApprovals.getString(dataapprovalRow, columnid) + "\"></td>\n");
                                continue;
                            }
                            if (((String)this._alDataApprovalColumnIds.get(i7)).equalsIgnoreCase("mandatoryflag")) {
                                sbHtml.append("<td align=center>" + this._dsFilteredDataApprovals.getValue(dataapprovalRow, columnid, "&nbsp;") + "</td>\n");
                                continue;
                            }
                            sbHtml.append("<td height=10>" + value + "</td> \n");
                        }
                        sbHtml.append("</tr>");
                    }
                    sbHtml.append("</table> \n");
                    sbHtml.append("</div> \n");
                    sbHtml.append("</td> \n");
                    sbHtml.append("</tr> \n");
                    sbHtml.append("</table> \n");
                }
                sbHtml.append("</td> \n");
                sbHtml.append("</tr> \n");
                sbHtml.append("<tr> \n");
                sbHtml.append("<td width=* colspan=" + this._alDatasetColumnIds.size() + ">\n");
                this._hmFilterMap.clear();
                this._hmFilterMap.put("keyid1", this._dsDataset.getString(dsDatasetRow, "keyid1"));
                this._hmFilterMap.put("keyid2", this._dsDataset.getString(dsDatasetRow, "keyid2"));
                this._hmFilterMap.put("keyid3", this._dsDataset.getString(dsDatasetRow, "keyid3"));
                this._hmFilterMap.put("paramlistid", this._dsDataset.getString(dsDatasetRow, "paramlistid"));
                this._hmFilterMap.put("paramlistversionid", this._dsDataset.getString(dsDatasetRow, "paramlistversionid"));
                this._hmFilterMap.put("variantid", this._dsDataset.getString(dsDatasetRow, "variantid"));
                this._hmFilterMap.put("dataset", this._dsDataset.getBigDecimal(dsDatasetRow, "dataset"));
                this._dsFilteredDataitems = null;
                this._dsFilteredDataitems = this._dsDataitem.getFilteredDataSet(this._hmFilterMap);
                if (this._alDataitemColumnTitles != null && this._dsFilteredDataitems.size() != 0) {
                    sbHtml.append("<table border=0 bordercolor=green cellpadding=2 cellspacing=0> \n");
                    sbHtml.append("<tr> \n");
                    sbHtml.append("<td width=12>&nbsp;</td> \n");
                    String src_dataitems = "WEB-OPAL/images/" + (this.__expandDataitems ? "minus" : "plus") + ".gif";
                    sbHtml.append("<td width=\"12\" valign=top> \n");
                    sbHtml.append("<img id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "dataitem_spec_\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src_dataitems + "\"></td>\n");
                    sbHtml.append("<td>");
                    sbHtml.append("<div style=\"display:" + (this.__expandDataitems ? "none" : "block") + "; color:brown;\" id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "dataitem_spec_text\">\n");
                    sbHtml.append(this._expandText + " " + this.__Tp.translate("dataitem(s)"));
                    sbHtml.append("</div>\n");
                    sbHtml.append("<div style=\"display:" + (this.__expandDataitems ? "block" : "none") + "\" id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "dataitem_spec_data\">\n");
                    sbHtml.append("<table width=100% cellpadding=\"3\" border=0  cellspacing=\"0\" class=\"info_list_dataitemtable\"> \n ");
                    sbHtml.append("<tr class=\"info_list_dataitemheader\"> \n ");
                    sbHtml.append("<td width=\"1\">&nbsp;</td>\n");
                    sbHtml.append("<td width=\"1\">&nbsp;</td>\n");
                    for (int i8 = 0; i8 < this._alDataitemColumnTitles.size(); ++i8) {
                        sbHtml.append("<td>" + (String)this._alDataitemColumnTitles.get(i8) + "</td> \n");
                    }
                    sbHtml.append("</tr> \n");
                    sbHtml.append("<tr class=\"info_list_dataitemrow\"> \n");
                    for (int dsDataitemRow = 0; dsDataitemRow < this._dsFilteredDataitems.size(); ++dsDataitemRow) {
                        this._dataitemspecCount = 0;
                        String src_singledataitem = "WEB-OPAL/images/" + (this.__expandSingleDataitem ? "plus" : "minus") + ".gif";
                        sbHtml.append("<td width=\"12\" valign=top> \n");
                        sbHtml.append("<img id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "-" + dsDataitemRow + "dataitem_spec_\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src_singledataitem + "\"></td>\n");
                        sbHtml.append("<td width=\"20\"> \n");
                        sbHtml.append("<div style=\"display: block; color:brown;\" id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "-" + dsDataitemRow + "dataitem_singlespec_data\">\n");
                        sbHtml.append("<img src=\"WEB-CORE/images/gif/Sample.gif\" height=15 width=15>");
                        sbHtml.append("\n </div>");
                        sbHtml.append("\n </td>");
                        for (int i9 = 0; i9 < this._alDataitemColumnIds.size(); ++i9) {
                            String columnid = (String)this._alDataitemColumnIds.get(i9);
                            String value = this._dsFilteredDataitems.getValue(dsDataitemRow, columnid, "&nbsp;");
                            sbHtml.append("<td>");
                            sbHtml.append("<div style=\"display:" + (this.__expandSingleDataitem ? "block" : "block") + "; color:black;\" id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "-" + dsDataitemRow + "dataitem_singlespec_data\">\n");
                            sbHtml.append(value);
                            sbHtml.append("</div>");
                            sbHtml.append("</td>");
                        }
                        sbHtml.append("</tr>");
                        this._replicateid = this._dsFilteredDataitems.getBigDecimal(dsDataitemRow, "replicateid").toString();
                        this._paramid = this._dsFilteredDataitems.getString(dsDataitemRow, "paramid");
                        this._paramlistid = this._dsFilteredDataitems.getString(dsDataitemRow, "paramlistid");
                        this._paramlistversionid = this._dsFilteredDataitems.getString(dsDataitemRow, "paramlistversionid");
                        this._paramtype = this._dsFilteredDataitems.getString(dsDataitemRow, "paramtype");
                        this._variantid = this._dsFilteredDataitems.getString(dsDataitemRow, "variantid");
                        this._dataset = this._dsFilteredDataitems.getBigDecimal(dsDataitemRow, "dataset").toString();
                        SafeSQL _sbDataItemSpecSql = this.__SqlGenerator.getDataItemSpecSql(this._sdcId, this._keyid1, this._keyid2, this._keyid3, this._paramid, this._paramlistid, this._paramlistversionid, this._variantid, this._dataset, this._paramtype, this._replicateid, this._specid, this._specversionid);
                        this._dsDataitemSpecs = this.__qp.getPreparedSqlDataSet(_sbDataItemSpecSql.getPreparedSQL(), _sbDataItemSpecSql.getValues());
                        this._paramid = "";
                        this._paramtype = "";
                        this._paramlistid = "";
                        this._paramlistversionid = "";
                        this._replicateid = "";
                        this._variantid = "";
                        this._dataset = "";
                        if (this._dsDataitemSpecs != null) {
                            this._dsFilteredDataitemSpecs = this._dsDataitemSpecs;
                        }
                        sbHtml.append("<tr>");
                        sbHtml.append("<td width=* colspan=" + this._alDataitemColumnIds.size() + ">\n");
                        if (this._dsFilteredDataitemSpecs.size() > 0 && this._dsFilteredDataitemSpecs != null) {
                            sbHtml.append("<div style=\"display:" + (this.__expandSingleDataitem ? "none" : "block") + "; color:brown;\" id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "-" + dsDataitemRow + "dataitem_spec_data\">\n");
                            sbHtml.append("<table border=0 bordercolor=green cellpadding=2 cellspacing=0> \n");
                            sbHtml.append("<tr> \n");
                            String src_dataitemspec = "WEB-OPAL/images/" + (this.__expandDataitemSpecs ? "minus" : "plus") + ".gif";
                            sbHtml.append("<td width=12>  &nbsp; </td>");
                            sbHtml.append("<td width=\"12\" valign=top> \n");
                            sbHtml.append("<img id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "-" + dsDataitemRow + "_spec_\" class=\"Outline\" style=\"cursor: pointer\" width=\"12\" height=\"12\" src=\"" + src_dataitemspec + "\"> ");
                            sbHtml.append("</td> \n");
                            sbHtml.append("<td>");
                            sbHtml.append("<div style=\"display:" + (this.__expandDataitemSpecs ? "none" : "block") + "; color:brown;\"  id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "-" + dsDataitemRow + "_spec_text\">\n");
                            sbHtml.append(this._expandText + " " + this.__Tp.translate("dataitem spec(s)"));
                            sbHtml.append("</div>");
                            sbHtml.append("<div style=\"display:" + (this.__expandDataitemSpecs ? "block" : "none") + "; color:brown;\"  id=\"" + this._currentKeyid + "-" + dsSpecRow + "-" + dsDatasetRow + "-" + dsDataitemRow + "_spec_data\">\n");
                            String oldSpec = "";
                            boolean newSpec = true;
                            for (int dsDataitemSpecRow = 0; dsDataitemSpecRow < this._dsFilteredDataitemSpecs.size(); ++dsDataitemSpecRow) {
                                if (!oldSpec.equalsIgnoreCase(this._dsFilteredDataitemSpecs.getString(dsDataitemSpecRow, "specid") + this._dsFilteredDataitemSpecs.getString(dsDataitemSpecRow, "specversionid"))) {
                                    newSpec = true;
                                }
                                if (newSpec) {
                                    if (dsDataitemSpecRow > 0) {
                                        sbHtml.append("</table> <!-- if dataitem spec rows nonzero --> \n </div>");
                                    }
                                    sbHtml.append("<table cellspacing=0 cellpadding=4 border=0 class=\"info_list_dataspectable\">\n");
                                    sbHtml.append("<tr class=\"info_list_dataspecheader\">\n");
                                    for (int j = 0; j < this._alDataitemSpecColumnTitles.size(); ++j) {
                                        sbHtml.append("<td>" + (String)this._alDataitemSpecColumnTitles.get(j) + "</td>\n");
                                    }
                                    sbHtml.append("</tr> \n");
                                }
                                sbHtml.append("<tr class=\"info_list_dataspecrow\"> \n ");
                                for (int i10 = 0; i10 < this._alDataitemSpecColumnIds.size(); ++i10) {
                                    if (newSpec) {
                                        String columnid = (String)this._alDataitemSpecColumnIds.get(i10);
                                        String value = this._dsFilteredDataitemSpecs.getValue(dsDataitemSpecRow, columnid, "&nbsp;");
                                        sbHtml.append("<td height=10>" + value + "</td> \n");
                                        continue;
                                    }
                                    if (((String)this._alDataitemSpecColumnIds.get(i10)).equalsIgnoreCase("specid") || ((String)this._alDataitemSpecColumnIds.get(i10)).equalsIgnoreCase("specversionid") || ((String)this._alDataitemSpecColumnIds.get(i10)).equalsIgnoreCase("condition")) {
                                        sbHtml.append("<td>&nbsp;</td> \n");
                                        continue;
                                    }
                                    sbHtml.append("<td>" + this._dsFilteredDataitemSpecs.getValue(dsDataitemSpecRow, (String)this._alDataitemSpecColumnIds.get(i10), "&nbsp;") + "</td>\n");
                                }
                                sbHtml.append("</tr>");
                                oldSpec = this._dsFilteredDataitemSpecs.getString(dsDataitemSpecRow, "specid") + this._dsFilteredDataitemSpecs.getString(dsDataitemSpecRow, "specversionid");
                                newSpec = false;
                            }
                            sbHtml.append("</td>");
                            sbHtml.append("</tr>");
                            sbHtml.append("</table>");
                            sbHtml.append("</div>");
                            sbHtml.append("</td> ");
                            sbHtml.append("</tr>");
                            sbHtml.append("</table> \n");
                            sbHtml.append("</div>");
                        }
                        sbHtml.append("</td></tr> \n");
                    }
                    sbHtml.append("</table> \n");
                    sbHtml.append("</div>");
                    sbHtml.append("</td>");
                    sbHtml.append("</tr>");
                    sbHtml.append("</table>");
                }
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
        }
        sbHtml.append("</div>");
        sbHtml.append("</td>");
        sbHtml.append("</tr>");
        sbHtml.append("</table>");
        this._hmFilterMap = null;
        this._hmFilterDISpecs = null;
        return sbHtml.toString();
    }
}

