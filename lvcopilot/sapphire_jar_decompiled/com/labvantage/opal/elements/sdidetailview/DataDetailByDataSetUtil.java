/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.sdidetailview;

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

public class DataDetailByDataSetUtil
extends BaseElement {
    private String LABVANTAGE_CVS_ID = "$Revision: 55971 $";
    private TranslationProcessor __Tp;
    private SDCProcessor __sdcProcessor;
    private QueryProcessor __qp;
    protected boolean _ShowPrimary = true;
    protected boolean _ShowPrimaryHeader = true;
    protected boolean _ShowDatasetHeader = true;
    protected boolean _ShowDataApprovalHeader = true;
    protected boolean _ShowDataApproval = true;
    protected boolean _ShowDataitemHeader = true;
    protected boolean _ShowDataitem = true;
    protected boolean _ShowDataitemLimitsHeader = true;
    protected boolean _ShowDataitemLimits = true;
    protected boolean _ShowDataitemSpecsHeader = true;
    protected boolean _ShowDataitemSpecs = true;
    protected boolean _InitExpandPrimary = true;
    protected boolean _InitExpandDataset = true;
    protected boolean _InitExpandDataApproval = true;
    protected boolean _InitExpandDataItem = true;
    protected boolean _InitExpandDataLimit = true;
    protected boolean _InitExpandDataSpec = true;
    protected boolean _IsOra = true;
    protected String _SampleId = "";
    protected String _ParamlistId = "";
    protected String _ParamlistVersionId = "";
    protected String _VariantId = "";
    protected String _Dataset = "";
    protected String _ParamId = "";
    protected String _ParamType = "";
    protected String _ReplicateId = "";
    protected String _SpecId = "";
    protected String _SpecVersionId = "";
    protected String _sdcid = "";
    protected String _tableid = "";
    protected String _keyColId1 = "";
    protected String _descCol = "";
    protected String _singular = "";
    protected StringBuffer sbPrimarySql = new StringBuffer("");
    protected StringBuffer sbDataitemSql = new StringBuffer("");
    protected StringBuffer sbDatasetSql = new StringBuffer("");
    protected StringBuffer sbDataApprovalSql = new StringBuffer("");
    protected StringBuffer sbDataitemLimitSql = new StringBuffer("");
    protected StringBuffer sbDataItemSpecSql = new StringBuffer("");
    protected StringBuffer sbDataSpecSql = new StringBuffer("");
    protected DataSet _Primary = new DataSet();
    protected DataSet _Datasets = new DataSet();
    protected DataSet _DataApprovals = new DataSet();
    protected DataSet _Dataitems = new DataSet();
    protected DataSet _DataitemLimits = new DataSet();
    protected DataSet _DataitemSpecs = new DataSet();
    protected DataSet _DataSpecs = new DataSet();
    protected HashMap _hmSDCProperties = new HashMap();
    protected ArrayList _KeyCols = new ArrayList();
    protected ArrayList _PrimaryFindCols = new ArrayList();
    protected ArrayList _PrimaryCols = new ArrayList();
    protected ArrayList _PrimaryColumnHeaders = new ArrayList();
    protected ArrayList _PrimaryColumnsTranslate = new ArrayList();
    protected ArrayList _DatasetColumns = new ArrayList();
    protected ArrayList _DatasetColumnHeaders = new ArrayList();
    protected ArrayList _DatasetColumnsTranslate = new ArrayList();
    protected ArrayList _DatasetSelectColumns = new ArrayList();
    protected ArrayList _DataApprovalColumns = new ArrayList();
    protected ArrayList _DataApprovalColumnHeaders = new ArrayList();
    protected ArrayList _DataitemColumns = new ArrayList();
    protected ArrayList _DataitemColumnHeaders = new ArrayList();
    protected ArrayList _DataitemColumnsTranslate = new ArrayList();
    protected ArrayList _DataitemLimitColumns = new ArrayList();
    protected ArrayList _DataitemLimitColumnHeaders = new ArrayList();
    protected ArrayList _DataitemSpecColumns = new ArrayList();
    protected ArrayList _DataitemSpecColumnHeaders = new ArrayList();
    protected ArrayList _DataSpecColumns = new ArrayList();
    protected ArrayList _DataSpecColumnHeaders = new ArrayList();

    private void setTranslationProcessor() {
        this.__Tp = this.getTranslationProcessor();
    }

    public DataDetailByDataSetUtil(PropertyList element) {
        this.element = element;
        this.setTranslationProcessor();
    }

    public DataDetailByDataSetUtil(PageContext pageContext, String connectionid) {
        this.pageContext = pageContext;
        this.setConnectionId(connectionid);
        this.setTranslationProcessor();
    }

    protected boolean prepareDisplayOptions() {
        try {
            PropertyList plPagedata = new PropertyList();
            plPagedata = (PropertyList)this.pageContext.getAttribute("pagedata", 2);
            this._SampleId = plPagedata.getProperty("keyid1");
            this._ParamlistId = plPagedata.getProperty("paramlistid");
            this._ParamlistVersionId = plPagedata.getProperty("paramlistversionid");
            this._VariantId = plPagedata.getProperty("variantid");
            this._Dataset = plPagedata.getProperty("dataset");
            this._ParamId = plPagedata.getProperty("paramid");
            this._ParamType = plPagedata.getProperty("paramtype");
            this._ReplicateId = plPagedata.getProperty("replicateid");
            this._SpecId = plPagedata.getProperty("specid");
            this._SpecVersionId = plPagedata.getProperty("specversionid");
            this._ShowPrimary = !this.element.getProperty("showprimary").equalsIgnoreCase("N");
            this._ShowPrimaryHeader = !this.element.getProperty("showprimaryheader").equalsIgnoreCase("N");
            this._ShowDatasetHeader = !this.element.getProperty("showdatasetheader").equalsIgnoreCase("N");
            this._ShowDataApprovalHeader = !this.element.getProperty("showdataapprovalheader").equalsIgnoreCase("N");
            this._ShowDataApproval = !this.element.getProperty("showdataapproval").equalsIgnoreCase("N");
            this._ShowDataitemHeader = !this.element.getProperty("showdataitemheader").equalsIgnoreCase("N");
            this._ShowDataitem = !this.element.getProperty("showdataitem").equalsIgnoreCase("N");
            this._ShowDataitemLimitsHeader = !this.element.getProperty("showdataitemlimitheader").equalsIgnoreCase("N");
            this._ShowDataitemLimits = !this.element.getProperty("showdataitemlimit").equalsIgnoreCase("N");
            this._ShowDataitemSpecsHeader = !this.element.getProperty("showdataitemspecheader").equalsIgnoreCase("N");
            this._ShowDataitemSpecs = !this.element.getProperty("showdataitemspec").equalsIgnoreCase("N");
            this._InitExpandPrimary = !this.element.getProperty("initexpandprimary").equalsIgnoreCase("N");
            this._InitExpandDataset = !this.element.getProperty("initexpanddataset").equalsIgnoreCase("N");
            this._InitExpandDataApproval = !this.element.getProperty("initexpanddataapproval").equalsIgnoreCase("N");
            this._InitExpandDataItem = !this.element.getProperty("initexpanddataitem").equalsIgnoreCase("N");
            this._InitExpandDataLimit = !this.element.getProperty("initexpanddatalimit").equalsIgnoreCase("N");
            this._InitExpandDataSpec = !this.element.getProperty("initexpanddataspec").equalsIgnoreCase("N");
            PropertyList pagedata = (PropertyList)this.pageContext.getAttribute("pagedata", 2);
            this._sdcid = this.element.getProperty("sdcid");
            if (this._sdcid == null || this._sdcid.trim().equalsIgnoreCase("")) {
                this._sdcid = pagedata.getProperty("sdcid");
            }
            return true;
        }
        catch (Exception ex) {
            this.logger.error("OPAL_ERR: Got an exception at DataDetailByDataSetUtil.prepareDisplayOptions() -> " + ex, ex);
            return false;
        }
    }

    protected boolean prepareSqls() {
        try {
            this._IsOra = this.getConnectionProcessor().isOra();
            this.__qp = new QueryProcessor(this.pageContext);
            this.__sdcProcessor = this.getSDCProcessor();
            this._hmSDCProperties = this.__sdcProcessor.getSDCProperties(this._sdcid);
            this._tableid = (String)this._hmSDCProperties.get("tableid");
            this._keyColId1 = (String)this._hmSDCProperties.get("keycolid1");
            this._descCol = (String)this._hmSDCProperties.get("desccol");
            this._singular = (String)this._hmSDCProperties.get("singular");
            String paramlistIdWhere = " and paramlistid " + DataDetailByDataSetUtil.getSqlWhereClause(this._ParamlistId, "C");
            String paramlistVersionIdWhere = " and paramlistversionid " + DataDetailByDataSetUtil.getSqlWhereClause(this._ParamlistVersionId, "C");
            String variantIdWhere = " and variantid " + DataDetailByDataSetUtil.getSqlWhereClause(this._VariantId, "C");
            String datasetWhere = " and dataset " + DataDetailByDataSetUtil.getSqlWhereClause(this._Dataset, "N");
            PropertyListCollection plcPrimaryCols = new PropertyListCollection();
            PropertyListCollection plcDatasetCols = new PropertyListCollection();
            PropertyListCollection plcDataitemCols = new PropertyListCollection();
            plcPrimaryCols = this.element.getCollection("primarycols");
            plcDatasetCols = this.element.getCollection("datasetcols");
            plcDataitemCols = this.element.getCollection("dataitemcols");
            if (plcPrimaryCols == null) {
                plcPrimaryCols = new PropertyListCollection();
            }
            if (plcDatasetCols == null) {
                plcDatasetCols = new PropertyListCollection();
            }
            if (plcDataitemCols == null) {
                plcDataitemCols = new PropertyListCollection();
            }
            String colId = "";
            String colTitle = "";
            this._KeyCols.add("sdcid");
            this._KeyCols.add("keyid1");
            this._KeyCols.add("keyid2");
            this._KeyCols.add("keyid3");
            this._KeyCols.add("usersequence");
            String keyColIdList = DataDetailByDataSetUtil.getColList(this._KeyCols);
            this._PrimaryFindCols.add(this._keyColId1);
            this._PrimaryCols.add(this._keyColId1);
            this._PrimaryCols.add(this._descCol);
            this._PrimaryCols.add("usersequence");
            this._PrimaryColumnHeaders.add(this.__Tp.translate(this._singular.substring(0, 1).toUpperCase() + this._singular.substring(1) + "Id"));
            this._PrimaryColumnHeaders.add(this.__Tp.translate("Description"));
            this._PrimaryColumnHeaders.add(this.__Tp.translate("User Sequence"));
            this._PrimaryColumnsTranslate.add(false);
            this._PrimaryColumnsTranslate.add(false);
            this._PrimaryColumnsTranslate.add(false);
            if (plcPrimaryCols != null) {
                for (int i = 0; i < plcPrimaryCols.size(); ++i) {
                    PropertyList properties = plcPrimaryCols.getPropertyList(i);
                    colId = properties.getProperty("columnid");
                    colTitle = properties.getProperty("title");
                    colTitle = colTitle.equalsIgnoreCase("") ? colId : colTitle;
                    boolean translate = properties.getProperty("translatevalue", "N").equals("Y");
                    if (colId.equalsIgnoreCase("") || this._PrimaryCols.contains(colId) || this._KeyCols.contains(colId)) continue;
                    this._PrimaryCols.add(colId);
                    this._PrimaryColumnHeaders.add(colTitle);
                    this._PrimaryColumnsTranslate.add(translate);
                }
            }
            String primarySelectList = DataDetailByDataSetUtil.getColList(this._PrimaryCols);
            this._DatasetColumns.add("paramlistid");
            this._DatasetColumns.add("paramlistversionid");
            this._DatasetColumns.add("variantid");
            this._DatasetColumns.add("dataset");
            this._DatasetSelectColumns.add("paramlistid");
            this._DatasetSelectColumns.add("paramlistversionid");
            this._DatasetSelectColumns.add("variantid");
            this._DatasetSelectColumns.add("dataset");
            this._DatasetColumnHeaders.add(this.__Tp.translate("ParamList"));
            this._DatasetColumnHeaders.add(this.__Tp.translate("Ver."));
            this._DatasetColumnHeaders.add(this.__Tp.translate("Variant"));
            this._DatasetColumnHeaders.add(this.__Tp.translate("Instance"));
            this._DatasetColumnsTranslate.add(false);
            this._DatasetColumnsTranslate.add(false);
            this._DatasetColumnsTranslate.add(false);
            this._DatasetColumnsTranslate.add(false);
            String datasetColList = DataDetailByDataSetUtil.getColList(this._DatasetColumns);
            for (int i = 0; i < plcDatasetCols.size(); ++i) {
                PropertyList properties = plcDatasetCols.getPropertyList(i);
                colId = properties.getProperty("columnid");
                colTitle = properties.getProperty("title");
                colTitle = colTitle.equalsIgnoreCase("") ? colId : colTitle;
                boolean translate = properties.getProperty("translatevalue", "N").equals("Y");
                if (colId.equalsIgnoreCase("") || this._DatasetColumns.contains(colId) || this._KeyCols.contains(colId)) continue;
                this._DatasetColumns.add(colId);
                this._DatasetColumnHeaders.add(colTitle);
                this._DatasetColumnsTranslate.add(translate);
            }
            String datasetSelectList = keyColIdList + ", " + DataDetailByDataSetUtil.getColList(this._DatasetColumns);
            this._DataApprovalColumns.add("approvalstep");
            this._DataApprovalColumns.add("roleid");
            this._DataApprovalColumns.add("mandatoryflag");
            this._DataApprovalColumns.add("approvalflag");
            this._DataApprovalColumnHeaders.add(this.__Tp.translate("Step"));
            this._DataApprovalColumnHeaders.add(this.__Tp.translate("Role"));
            this._DataApprovalColumnHeaders.add(this.__Tp.translate("Mandatory"));
            this._DataApprovalColumnHeaders.add(this.__Tp.translate("Result"));
            String dataApprovalSelectList = keyColIdList + ", " + datasetColList + ", " + DataDetailByDataSetUtil.getColList(this._DataApprovalColumns);
            this._DataitemColumns.add("paramid");
            this._DataitemColumns.add("paramtype");
            this._DataitemColumns.add("replicateid");
            String dataitemColList = DataDetailByDataSetUtil.getColList(this._DataitemColumns);
            this._DataitemColumns.add("enteredtext");
            this._DataitemColumns.add("displayvalue");
            this._DataitemColumns.add("displayunits");
            this._DataitemColumns.add("condition");
            this._DataitemColumnHeaders.add(this.__Tp.translate("Param"));
            this._DataitemColumnHeaders.add(this.__Tp.translate("Type"));
            this._DataitemColumnHeaders.add(this.__Tp.translate("Rep#"));
            this._DataitemColumnHeaders.add(this.__Tp.translate("Entered Data"));
            this._DataitemColumnHeaders.add(this.__Tp.translate("Display Data"));
            this._DataitemColumnHeaders.add(this.__Tp.translate("Unit"));
            this._DataitemColumnHeaders.add(this.__Tp.translate("Condition"));
            this._DataitemColumnsTranslate.add(false);
            this._DataitemColumnsTranslate.add(false);
            this._DataitemColumnsTranslate.add(false);
            this._DataitemColumnsTranslate.add(false);
            this._DataitemColumnsTranslate.add(false);
            this._DataitemColumnsTranslate.add(false);
            this._DataitemColumnsTranslate.add(false);
            for (int i = 0; i < plcDataitemCols.size(); ++i) {
                PropertyList properties = plcDataitemCols.getPropertyList(i);
                colId = properties.getProperty("columnid");
                colTitle = properties.getProperty("title");
                colTitle = colTitle.equalsIgnoreCase("") ? colId : colTitle;
                boolean translate = properties.getProperty("translatevalue", "N").equals("Y");
                if (colId.equalsIgnoreCase("") || this._DataitemColumns.contains(colId) || this._DatasetColumns.contains(colId) || this._KeyCols.contains(colId)) continue;
                this._DataitemColumns.add(colId);
                this._DataitemColumnHeaders.add(colTitle);
                this._DataitemColumnsTranslate.add(translate);
            }
            String dataitemSelectList = keyColIdList + ", " + datasetColList + ", " + DataDetailByDataSetUtil.getColList(this._DataitemColumns);
            this._DataitemLimitColumns.add("limittypeid");
            this._DataitemLimitColumns.add("operator");
            this._DataitemLimitColumns.add("value1");
            this._DataitemLimitColumns.add("value2");
            this._DataitemLimitColumns.add("unitsid");
            this._DataitemLimitColumnHeaders.add(this.__Tp.translate("LimitType"));
            this._DataitemLimitColumnHeaders.add(this.__Tp.translate("Operator"));
            this._DataitemLimitColumnHeaders.add(this.__Tp.translate("Value1"));
            this._DataitemLimitColumnHeaders.add(this.__Tp.translate("Value2"));
            this._DataitemLimitColumnHeaders.add(this.__Tp.translate("Unit"));
            String dataitemlimitSelectList = keyColIdList + ", " + datasetColList + ", " + dataitemColList + ", " + DataDetailByDataSetUtil.getColList(this._DataitemLimitColumns);
            this._DataitemSpecColumns.add("specid");
            this._DataitemSpecColumns.add("specversionid");
            this._DataitemSpecColumns.add("condition");
            this._DataitemSpecColumns.add("val1");
            this._DataitemSpecColumns.add("val2");
            this._DataitemSpecColumns.add("unitsid");
            this._DataitemSpecColumns.add("typecondition");
            this._DataitemSpecColumnHeaders.add(this.__Tp.translate("Spec"));
            this._DataitemSpecColumnHeaders.add(this.__Tp.translate("Ver."));
            this._DataitemSpecColumnHeaders.add(this.__Tp.translate("Result"));
            this._DataitemSpecColumnHeaders.add(this.__Tp.translate("Limit1"));
            this._DataitemSpecColumnHeaders.add(this.__Tp.translate("Limit2"));
            this._DataitemSpecColumnHeaders.add(this.__Tp.translate("Unit"));
            this._DataitemSpecColumnHeaders.add(this.__Tp.translate("LimitType"));
            this._DataSpecColumns.add("specid");
            this._DataSpecColumns.add("specversionid");
            this._DataSpecColumnHeaders.add(this.__Tp.translate("Spec"));
            this._DataSpecColumnHeaders.add(this.__Tp.translate("Ver."));
            String dataspecSelectList = keyColIdList + ", " + DataDetailByDataSetUtil.getColList(this._DataSpecColumns);
            SafeSQL safeSQL = new SafeSQL();
            this.sbPrimarySql.append("select ").append(primarySelectList);
            this.sbPrimarySql.append(" from ").append(this._tableid);
            this.sbPrimarySql.append(" where ").append(this._keyColId1).append(" in ( ").append(safeSQL.addIn(this._SampleId, ";")).append(")");
            this.sbPrimarySql.append(" order by ").append(this._keyColId1);
            this._Primary = this.__qp.getPreparedSqlDataSet(this.sbPrimarySql.toString(), safeSQL.getValues());
            safeSQL.reset();
            this.sbDatasetSql.append("select ").append(datasetSelectList);
            this.sbDatasetSql.append(" from sdidata");
            this.sbDatasetSql.append(" where sdcid = ").append(safeSQL.addVar(this._sdcid));
            this.sbDatasetSql.append(" and keyid1 = ").append(safeSQL.addVar(this._SampleId));
            this.sbDatasetSql.append(" and keyid2 = '(null)'");
            this.sbDatasetSql.append(" and keyid3 = '(null)'");
            if (!this._ParamlistId.equalsIgnoreCase("")) {
                this.sbDatasetSql.append(paramlistIdWhere);
            }
            if (!this._ParamlistVersionId.equalsIgnoreCase("")) {
                this.sbDatasetSql.append(paramlistVersionIdWhere);
            }
            if (!this._VariantId.equalsIgnoreCase("")) {
                this.sbDatasetSql.append(variantIdWhere);
            }
            if (!this._Dataset.equalsIgnoreCase("")) {
                this.sbDatasetSql.append(datasetWhere);
            }
            this.sbDatasetSql.append(" order by keyid1, paramlistid, paramlistversionid, variantid, dataset, usersequence");
            this._Datasets = this.__qp.getPreparedSqlDataSet(this.sbDatasetSql.toString(), safeSQL.getValues());
            safeSQL.reset();
            this.sbDataApprovalSql.append("select ").append(dataApprovalSelectList);
            this.sbDataApprovalSql.append(" from sdidataapproval");
            this.sbDataApprovalSql.append(" where sdcid = ").append(safeSQL.addVar(this._sdcid));
            this.sbDataApprovalSql.append(" and keyid1 = ").append(safeSQL.addVar(this._SampleId));
            this.sbDataApprovalSql.append(" and keyid2 = '(null)'");
            this.sbDataApprovalSql.append(" and keyid3 = '(null)'");
            if (!this._ParamlistId.equalsIgnoreCase("")) {
                this.sbDataApprovalSql.append(" and paramlistid = ").append(safeSQL.addVar(this._ParamlistId));
            }
            if (!this._ParamlistVersionId.equalsIgnoreCase("")) {
                this.sbDataApprovalSql.append(" and paramlistversionid = ").append(safeSQL.addVar(this._ParamlistVersionId));
            }
            if (!this._VariantId.equalsIgnoreCase("")) {
                this.sbDataApprovalSql.append(" and variantid = ").append(safeSQL.addVar(this._VariantId));
            }
            if (!this._Dataset.equalsIgnoreCase("")) {
                this.sbDataApprovalSql.append(" and dataset = ").append(safeSQL.addVar(this._Dataset));
            }
            this.sbDataApprovalSql.append(" order by keyid1, paramlistid, paramlistversionid, variantid, dataset, usersequence");
            this._DataApprovals = this.__qp.getPreparedSqlDataSet(this.sbDataApprovalSql.toString(), safeSQL.getValues());
            safeSQL.reset();
            this.sbDataitemSql.append("select ").append(dataitemSelectList);
            this.sbDataitemSql.append(" from sdidataitem");
            this.sbDataitemSql.append(" where sdcid = ").append(safeSQL.addVar(this._sdcid));
            this.sbDataitemSql.append(" and keyid1 = ").append(safeSQL.addVar(this._SampleId));
            this.sbDataitemSql.append(" and keyid2 = '(null)'");
            this.sbDataitemSql.append(" and keyid3 = '(null)'");
            if (!this._ParamlistId.equalsIgnoreCase("")) {
                this.sbDataitemSql.append(" and paramlistid = ").append(safeSQL.addVar(this._ParamlistId));
            }
            if (!this._ParamlistVersionId.equalsIgnoreCase("")) {
                this.sbDataitemSql.append(" and paramlistversionid = ").append(safeSQL.addVar(this._ParamlistVersionId));
            }
            if (!this._VariantId.equalsIgnoreCase("")) {
                this.sbDataitemSql.append(" and variantid = ").append(safeSQL.addVar(this._VariantId));
            }
            if (!this._Dataset.equalsIgnoreCase("")) {
                this.sbDataitemSql.append(" and dataset = ").append(safeSQL.addVar(this._Dataset));
            }
            if (!this._ParamId.equalsIgnoreCase("")) {
                this.sbDataitemSql.append(" and paramid = ").append(safeSQL.addVar(this._ParamId));
            }
            if (!this._ParamType.equalsIgnoreCase("")) {
                this.sbDataitemSql.append(" and paramtype = ").append(safeSQL.addVar(this._ParamType));
            }
            if (!this._ReplicateId.equalsIgnoreCase("")) {
                this.sbDataitemSql.append(" and replicateid = ").append(safeSQL.addVar(this._ReplicateId));
            }
            this.sbDataitemSql.append(" order by keyid1, paramlistid, paramlistversionid, variantid, dataset, usersequence, paramid, paramtype, replicateid");
            this._Dataitems = this.__qp.getPreparedSqlDataSet(this.sbDataitemSql.toString(), safeSQL.getValues());
            if (this._ShowDataitemLimits) {
                safeSQL.reset();
                this.sbDataitemLimitSql.append("select ").append(dataitemlimitSelectList);
                this.sbDataitemLimitSql.append(" from sdidataitemlimits");
                this.sbDataitemLimitSql.append(" where sdcid = ").append(safeSQL.addVar(this._sdcid));
                this.sbDataitemLimitSql.append(" and keyid1 = ").append(safeSQL.addVar(this._SampleId));
                this.sbDataitemLimitSql.append(" and keyid2 = '(null)' ");
                this.sbDataitemLimitSql.append(" and keyid3 = '(null)' ");
                if (!this._ParamlistId.equalsIgnoreCase("")) {
                    this.sbDataitemLimitSql.append(" and paramlistid = ").append(safeSQL.addVar(this._ParamlistId));
                }
                if (!this._ParamlistVersionId.equalsIgnoreCase("")) {
                    this.sbDataitemLimitSql.append(" and paramlistversionid = ").append(safeSQL.addVar(this._ParamlistVersionId));
                }
                if (!this._VariantId.equalsIgnoreCase("")) {
                    this.sbDataitemLimitSql.append(" and variantid = ").append(safeSQL.addVar(this._VariantId));
                }
                if (!this._Dataset.equalsIgnoreCase("")) {
                    this.sbDataitemLimitSql.append(" and dataset = ").append(safeSQL.addVar(this._Dataset));
                }
                if (!this._ParamId.equalsIgnoreCase("")) {
                    this.sbDataitemLimitSql.append(" and paramid = ").append(safeSQL.addVar(this._ParamId));
                }
                if (!this._ParamType.equalsIgnoreCase("")) {
                    this.sbDataitemLimitSql.append(" and paramtype = ").append(safeSQL.addVar(this._ParamType));
                }
                if (!this._ReplicateId.equalsIgnoreCase("")) {
                    this.sbDataitemLimitSql.append(" and replicateid = ").append(safeSQL.addVar(this._ReplicateId));
                }
                this.sbDataitemLimitSql.append(" order by keyid1, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, limittypeid, usersequence");
                this._DataitemLimits = this.__qp.getPreparedSqlDataSet(this.sbDataitemLimitSql.toString(), safeSQL.getValues());
            }
            if (this._ShowDataitemSpecs) {
                safeSQL.reset();
                this.sbDataItemSpecSql.append("select dis.sdcid, dis.keyid1, dis.keyid2, dis.keyid3, dis.usersequence , ");
                this.sbDataItemSpecSql.append(" dis.paramlistid, dis.paramlistversionid, dis.variantid, dis.dataset, dis.paramid, dis.paramtype, dis.replicateid,  ");
                if (this._IsOra) {
                    this.sbDataItemSpecSql.append(" dis.specid, dis.specversionid, dis.condition, lim.limittypesequence, lim.operator1||lim.value1 val1, lim.operator2||lim.value2 val2, spi.unitsid, ");
                    this.sbDataItemSpecSql.append(" limtype.limittypeid || '(' || limtype.condition || ')' typecondition ");
                } else {
                    this.sbDataItemSpecSql.append(" dis.specid, dis.specversionid, dis.condition, lim.limittypesequence, lim.operator1+lim.value1 val1, lim.operator2+lim.value2 val2, spi.unitsid, ");
                    this.sbDataItemSpecSql.append(" limtype.limittypeid + '(' + limtype.condition + ')' typecondition ");
                }
                this.sbDataItemSpecSql.append(" from sdidataitemspec dis, specparamitems spi, specparamlimits lim, speclimittype limtype");
                this.sbDataItemSpecSql.append(" where dis.sdcid = ").append(safeSQL.addVar(this._sdcid));
                this.sbDataItemSpecSql.append(" and dis.keyid1 = ").append(safeSQL.addVar(this._SampleId));
                this.sbDataItemSpecSql.append(" and dis.keyid2 = '(null)' ");
                this.sbDataItemSpecSql.append(" and dis.keyid3 = '(null)' ");
                if (!this._ParamlistId.equalsIgnoreCase("")) {
                    this.sbDataItemSpecSql.append(" and dis.paramlistid = ").append(safeSQL.addVar(this._ParamlistId));
                }
                if (!this._ParamlistVersionId.equalsIgnoreCase("")) {
                    this.sbDataItemSpecSql.append(" and dis.paramlistversionid = ").append(safeSQL.addVar(this._ParamlistVersionId));
                }
                if (!this._VariantId.equalsIgnoreCase("")) {
                    this.sbDataItemSpecSql.append(" and dis.variantid = ").append(safeSQL.addVar(this._VariantId));
                }
                if (!this._Dataset.equalsIgnoreCase("")) {
                    this.sbDataItemSpecSql.append(" and dis.dataset = ").append(safeSQL.addVar(this._Dataset));
                }
                if (!this._ParamId.equalsIgnoreCase("")) {
                    this.sbDataItemSpecSql.append(" and dis.paramid = ").append(safeSQL.addVar(this._ParamId));
                }
                if (!this._ParamType.equalsIgnoreCase("")) {
                    this.sbDataItemSpecSql.append(" and dis.paramtype = ").append(safeSQL.addVar(this._ParamType));
                }
                if (!this._ReplicateId.equalsIgnoreCase("")) {
                    this.sbDataItemSpecSql.append(" and dis.replicateid = ").append(safeSQL.addVar(this._ReplicateId));
                }
                if (!this._SpecId.equalsIgnoreCase("")) {
                    this.sbDataItemSpecSql.append(" and dis.specid = ").append(safeSQL.addVar(this._SpecId));
                }
                if (!this._SpecVersionId.equalsIgnoreCase("")) {
                    this.sbDataItemSpecSql.append(" and dis.specversionid = ").append(safeSQL.addVar(this._SpecVersionId));
                }
                this.sbDataItemSpecSql.append(" and dis.specid = spi.specid ");
                this.sbDataItemSpecSql.append(" and dis.specversionid = spi.specversionid ");
                this.sbDataItemSpecSql.append(" AND ");
                this.sbDataItemSpecSql.append(" ( ");
                this.sbDataItemSpecSql.append("   ( ");
                this.sbDataItemSpecSql.append("   spi.allowanyparamlistflag = 'Y' ");
                this.sbDataItemSpecSql.append("   ) ");
                this.sbDataItemSpecSql.append("   OR ");
                this.sbDataItemSpecSql.append("   ( ");
                this.sbDataItemSpecSql.append("   dis.paramlistid = spi.paramlistid ");
                this.sbDataItemSpecSql.append("   and dis.paramlistversionid = spi.paramlistversionid ");
                this.sbDataItemSpecSql.append("   and dis.variantid = spi.variantid ");
                this.sbDataItemSpecSql.append("   and (spi.allowanyparamlistflag = 'N' or spi.allowanyparamlistflag is null or spi.allowanyparamlistflag = '') ");
                this.sbDataItemSpecSql.append("   ) ");
                this.sbDataItemSpecSql.append("   OR ");
                this.sbDataItemSpecSql.append("   ( ");
                this.sbDataItemSpecSql.append("   dis.paramlistid = spi.paramlistid ");
                this.sbDataItemSpecSql.append("   and dis.variantid = spi.variantid ");
                this.sbDataItemSpecSql.append("   and spi.allowanyparamlistflag = 'V' ");
                this.sbDataItemSpecSql.append("   ) ");
                this.sbDataItemSpecSql.append("   OR ");
                this.sbDataItemSpecSql.append("   ( ");
                this.sbDataItemSpecSql.append("   dis.paramlistid = spi.paramlistid ");
                this.sbDataItemSpecSql.append("   and spi.allowanyparamlistflag = 'A' ");
                this.sbDataItemSpecSql.append("   ) ");
                this.sbDataItemSpecSql.append(" ) ");
                this.sbDataItemSpecSql.append(" and dis.paramid = spi.paramid ");
                this.sbDataItemSpecSql.append(" and dis.paramtype = spi.paramtype ");
                this.sbDataItemSpecSql.append(" and lim.specid = spi.specid ");
                this.sbDataItemSpecSql.append(" and lim.specversionid = spi.specversionid ");
                this.sbDataItemSpecSql.append(" and lim.paramlistid = spi.paramlistid ");
                this.sbDataItemSpecSql.append(" and lim.paramlistversionid = spi.paramlistversionid ");
                this.sbDataItemSpecSql.append(" and lim.variantid = spi.variantid ");
                this.sbDataItemSpecSql.append(" and lim.paramid = spi.paramid ");
                this.sbDataItemSpecSql.append(" and lim.paramtype = spi.paramtype ");
                this.sbDataItemSpecSql.append(" and limtype.specid = lim.specid ");
                this.sbDataItemSpecSql.append(" and limtype.specversionid = lim.specversionid ");
                this.sbDataItemSpecSql.append(" and limtype.limittypesequence = lim.limittypesequence ");
                this.sbDataItemSpecSql.append(" order by dis.keyid1, dis.paramlistid, dis.paramlistversionid, dis.variantid, lim.limittypesequence,  ");
                this.sbDataItemSpecSql.append(" dis.dataset, dis.paramid, dis.paramtype, dis.replicateid,  ");
                this.sbDataItemSpecSql.append(" dis.specid, dis.specversionid, dis.usersequence");
                this._DataitemSpecs = this.__qp.getPreparedSqlDataSet(this.sbDataItemSpecSql.toString(), safeSQL.getValues());
            }
            safeSQL.reset();
            this.sbDataSpecSql.append("select sdispec.keyid1, sdispec.specid, sdispec.specversionid, specdesc, versionstatus, ").append(this._descCol);
            this.sbDataSpecSql.append(" from sdispec, spec, ").append(this._tableid);
            this.sbDataSpecSql.append(" where sdcid = ").append(safeSQL.addVar(this._sdcid));
            this.sbDataSpecSql.append(" and keyid1 = ").append(safeSQL.addVar(this._SampleId));
            this.sbDataSpecSql.append(" and keyid2 = '(null)' ");
            this.sbDataSpecSql.append(" and keyid3 = '(null)' ");
            if (!this._SpecId.equalsIgnoreCase("")) {
                this.sbDataSpecSql.append(" and sdispec.specid = ").append(safeSQL.addVar(this._SpecId));
            }
            if (!this._SpecVersionId.equalsIgnoreCase("")) {
                this.sbDataSpecSql.append(" and sdispec.specversionid = ").append(safeSQL.addVar(this._SpecVersionId));
            }
            this.sbDataSpecSql.append(" and sdispec.specid = spec.specid ");
            this.sbDataSpecSql.append(" and sdispec.specversionid = spec.specversionid ");
            this.sbDataSpecSql.append(" and sdispec.keyid1 = ").append(this._tableid).append(".").append(this._keyColId1);
            this.sbDataSpecSql.append(" order by keyid1, sdispec.specid, sdispec.specversionid, sdispec.usersequence");
            this._DataSpecs = this.__qp.getPreparedSqlDataSet(this.sbDataSpecSql.toString(), safeSQL.getValues());
            return true;
        }
        catch (Exception ex) {
            this.logger.error("OPAL_ERR: Got an exception at prepareSqls() -> " + ex, ex);
            return false;
        }
    }

    @Override
    public String getHtml() {
        StringBuffer sbHtml = new StringBuffer();
        return sbHtml.toString();
    }

    protected static String getColList(ArrayList columns) {
        String colList = "";
        if (columns.size() > 0) {
            colList = (String)columns.get(0);
        }
        for (int i = 1; i < columns.size(); ++i) {
            colList = colList + ", " + (String)columns.get(i);
        }
        return colList;
    }

    protected static String getSqlWhereClause(String where, String type) {
        StringBuffer sqlWhere = new StringBuffer();
        if (where.indexOf(";") >= 0) {
            if (type.equalsIgnoreCase("C")) {
                sqlWhere.append(" in (").append("'").append(where.replaceAll(";", "','")).append("') ");
            } else {
                sqlWhere.append(" in (").append(where.replaceAll(";", ", ")).append(") ");
            }
        } else if (type.equalsIgnoreCase("C")) {
            sqlWhere.append(" = ").append("'").append(where).append("' ");
        } else {
            sqlWhere.append(" = ").append(where).append(" ");
        }
        return sqlWhere.toString();
    }

    protected static HashMap getUniqueSpecsForDataitem(DataSet ds) {
        HashMap<String, String> hmSpecs = new HashMap<String, String>();
        ds.sort("specid,specversionid");
        String newStr = "";
        String oldStr = "";
        if (ds.size() > 0) {
            oldStr = ds.getString(0, "specid") + ds.getString(0, "specversionid");
            hmSpecs.put(ds.getString(0, "specid"), ds.getString(0, "specversionid"));
        }
        for (int i = 1; i < ds.size(); ++i) {
            newStr = ds.getString(i, "specid") + ds.getString(i, "specversionid");
            if (!newStr.equalsIgnoreCase(oldStr)) {
                hmSpecs.put(ds.getString(i, "specid"), ds.getString(i, "specversionid"));
            }
            oldStr = newStr;
        }
        return hmSpecs;
    }
}

