/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.sdidetailview;

import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataDetailUtil
extends BaseElement {
    private String LABVANTAGE_CVS_ID = "$Revision: 54489 $";
    private TranslationProcessor __Tp;
    private boolean __Debug = false;
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
    protected boolean _InitExpandDataLimit = true;
    protected boolean _InitExpandDataSpec = true;
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
    protected DataSet _Primary = new DataSet();
    protected DataSet _Datasets = new DataSet();
    protected DataSet _DataApprovals = new DataSet();
    protected DataSet _Dataitems = new DataSet();
    protected DataSet _DataitemLimits = new DataSet();
    protected DataSet _DataitemSpecs = new DataSet();
    protected DataSet _DataSpecs = new DataSet();
    protected ArrayList _KeyCols = new ArrayList();
    protected ArrayList _PrimaryFindCols = new ArrayList();
    protected ArrayList _PrimaryCols = new ArrayList();
    protected ArrayList _PrimaryColumnHeaders = new ArrayList();
    protected ArrayList _DatasetColumns = new ArrayList();
    protected ArrayList _DatasetColumnHeaders = new ArrayList();
    protected ArrayList _DataApprovalColumns = new ArrayList();
    protected ArrayList _DataApprovalColumnHeaders = new ArrayList();
    protected ArrayList _DataitemColumns = new ArrayList();
    protected ArrayList _DataitemColumnHeaders = new ArrayList();
    protected ArrayList _DataitemLimitColumns = new ArrayList();
    protected ArrayList _DataitemLimitColumnHeaders = new ArrayList();
    protected ArrayList _DataitemSpecColumns = new ArrayList();
    protected ArrayList _DataitemSpecColumnHeaders = new ArrayList();
    protected ArrayList _DataSpecColumns = new ArrayList();
    protected ArrayList _DataSpecColumnHeaders = new ArrayList();

    private void setTranslationProcessor() {
        this.__Tp = this.getTranslationProcessor();
    }

    public DataDetailUtil(PropertyList element) {
        this.element = element;
        this.setTranslationProcessor();
    }

    public DataDetailUtil(PageContext pageContext, String connectionid) {
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
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> Got inputs to the page as..");
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SAMPLEID=" + this._SampleId);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _PARAMLISTID=" + this._ParamlistId);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _PARAMLISTVERSIONID=" + this._ParamlistVersionId);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _VARIANTID=" + this._VariantId);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _DATASET=" + this._Dataset);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _PARAMID=" + this._ParamId);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _PARAMTYPE=" + this._ParamType);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _REPLICATEID=" + this._ReplicateId);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SPECID=" + this._SpecId);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SPECVERSIONID=" + this._SpecVersionId);
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
            this._InitExpandDataLimit = !this.element.getProperty("initexpanddatalimit").equalsIgnoreCase("N");
            this._InitExpandDataSpec = !this.element.getProperty("initexpanddataspec").equalsIgnoreCase("N");
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> Got display props as..");
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SHOW_PRIMARY=" + this._ShowPrimary);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SHOW_PRIMARY_HEADER=" + this._ShowPrimaryHeader);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SHOW_DATASET_HEADER=" + this._ShowDatasetHeader);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SHOW_DATAAPPROVAL_HEADER=" + this._ShowDataApprovalHeader);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SHOW_DATAAPPROVAL=" + this._ShowDataApproval);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SHOW_DATAITEM_HEADER=" + this._ShowDataitemHeader);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SHOW_DATAITEM=" + this._ShowDataitem);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SHOW_DATAITEMLIMITS_HEADER=" + this._ShowDataitemLimitsHeader);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SHOW_DATAITEMLIMITS=" + this._ShowDataitemLimits);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SHOW_DATAITEMSPECS_HEADER=" + this._ShowDataitemSpecsHeader);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _SHOW_DATAITEMSPECS=" + this._ShowDataitemSpecs);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _INIT_EXPAND_PRIMARY=" + this._InitExpandPrimary);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _INIT_EXPAND_DATASET=" + this._InitExpandDataset);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _INIT_EXPAND_DATALIMIT=" + this._InitExpandDataLimit);
            this.logger.debug("DataDetailUtil.prepareDisplayOptions() -> _INIT_EXPAND_DATASPEC=" + this._InitExpandDataSpec);
            return true;
        }
        catch (Exception ex) {
            this.logger.error("Got an exception at DataDetailUtil.prepareDisplayOptions() -> " + ex, ex);
            return false;
        }
    }

    protected boolean prepareSqls() {
        try {
            PropertyListCollection plcPrimaryCols = this.element.getCollection("primarycols");
            PropertyListCollection plcDatasetCols = this.element.getCollection("datasetcols");
            PropertyListCollection plcDataitemCols = this.element.getCollection("dataitemcols");
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
            String keyColIdList = DataDetailUtil.getColList(this._KeyCols);
            this._PrimaryFindCols.add("s_sampleid");
            this._PrimaryCols.add("s_sampleid");
            this._PrimaryCols.add("sampledesc");
            this._PrimaryCols.add("samplestatus");
            this._PrimaryColumnHeaders.add(this.__Tp.translate("SampleId"));
            this._PrimaryColumnHeaders.add(this.__Tp.translate("Description"));
            this._PrimaryColumnHeaders.add(this.__Tp.translate("Status"));
            for (int i = 0; i < plcPrimaryCols.size(); ++i) {
                colId = plcPrimaryCols.getPropertyList(i).getProperty("columnid");
                colTitle = plcPrimaryCols.getPropertyList(i).getProperty("title");
                String string = colTitle = colTitle.equalsIgnoreCase("") ? colId : colTitle;
                if (colId.equalsIgnoreCase("") || this._PrimaryCols.contains(colId) || this._KeyCols.contains(colId)) continue;
                this._PrimaryCols.add(colId);
                this._PrimaryColumnHeaders.add(colTitle);
            }
            String primarySelectList = DataDetailUtil.getColList(this._PrimaryCols);
            if (this.__Debug) {
                this.logger.debug("OPAL_INFO: DataDetailUtil.prepareSqls() -> primarySelectList = " + primarySelectList);
            }
            this._DatasetColumns.add("paramlistid");
            this._DatasetColumns.add("paramlistversionid");
            this._DatasetColumns.add("variantid");
            this._DatasetColumns.add("dataset");
            this._DatasetColumnHeaders.add(this.__Tp.translate("ParamList"));
            this._DatasetColumnHeaders.add(this.__Tp.translate("Ver."));
            this._DatasetColumnHeaders.add(this.__Tp.translate("Variant"));
            this._DatasetColumnHeaders.add(this.__Tp.translate("DS#"));
            String datasetColList = DataDetailUtil.getColList(this._DatasetColumns);
            for (int i = 0; i < plcDatasetCols.size(); ++i) {
                colId = plcDatasetCols.getPropertyList(i).getProperty("columnid");
                colTitle = plcDatasetCols.getPropertyList(i).getProperty("title");
                String string = colTitle = colTitle.equalsIgnoreCase("") ? colId : colTitle;
                if (colId.equalsIgnoreCase("") || this._DatasetColumns.contains(colId) || this._KeyCols.contains(colId)) continue;
                this._DatasetColumns.add(colId);
                this._DatasetColumnHeaders.add(colTitle);
            }
            String datasetSelectList = keyColIdList + ", " + DataDetailUtil.getColList(this._DatasetColumns);
            if (this.__Debug) {
                this.logger.debug("OPAL_INFO: DataDetailUtil.prepareSqls() -> datasetSelectList = " + datasetSelectList);
            }
            this._DataApprovalColumns.add("approvalstep");
            this._DataApprovalColumns.add("roleid");
            this._DataApprovalColumns.add("mandatoryflag");
            this._DataApprovalColumns.add("approvalflag");
            this._DataApprovalColumnHeaders.add(this.__Tp.translate("Step"));
            this._DataApprovalColumnHeaders.add(this.__Tp.translate("Role"));
            this._DataApprovalColumnHeaders.add(this.__Tp.translate("Mandatory"));
            this._DataApprovalColumnHeaders.add(this.__Tp.translate("Result"));
            String dataApprovalSelectList = keyColIdList + ", " + datasetColList + ", " + DataDetailUtil.getColList(this._DataApprovalColumns);
            if (this.__Debug) {
                this.logger.debug("OPAL_INFO: DataDetailUtil.prepareSqls() -> dataApprovalSelectList = " + dataApprovalSelectList);
            }
            this._DataitemColumns.add("paramid");
            this._DataitemColumns.add("paramtype");
            this._DataitemColumns.add("replicateid");
            String dataitemColList = DataDetailUtil.getColList(this._DataitemColumns);
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
            for (int i = 0; i < plcDataitemCols.size(); ++i) {
                colId = plcDataitemCols.getPropertyList(i).getProperty("columnid");
                colTitle = plcDataitemCols.getPropertyList(i).getProperty("title");
                String string = colTitle = colTitle.equalsIgnoreCase("") ? colId : colTitle;
                if (colId.equalsIgnoreCase("") || this._DataitemColumns.contains(colId) || this._DatasetColumns.contains(colId) || this._KeyCols.contains(colId)) continue;
                this._DataitemColumns.add(colId);
                this._DataitemColumnHeaders.add(colTitle);
            }
            String dataitemSelectList = keyColIdList + ", " + datasetColList + ", " + DataDetailUtil.getColList(this._DataitemColumns);
            if (this.__Debug) {
                this.logger.debug("OPAL_INFO: DataDetailUtil.prepareSqls() -> dataitemSelectList = " + dataitemSelectList);
            }
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
            String dataitemlimitSelectList = keyColIdList + ", " + datasetColList + ", " + dataitemColList + ", " + DataDetailUtil.getColList(this._DataitemLimitColumns);
            if (this.__Debug) {
                this.logger.debug("OPAL_INFO: DataDetailUtil.prepareSqls() -> dataitemlimitSelectList = " + dataitemlimitSelectList);
            }
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
            String dataspecSelectList = keyColIdList + ", " + DataDetailUtil.getColList(this._DataSpecColumns);
            if (this.__Debug) {
                this.logger.debug("OPAL_INFO: DataDetailUtil.prepareSqls() -> dataspecSelectList = " + dataspecSelectList);
            }
            QueryProcessor qp = this.getQueryProcessor();
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select ").append(primarySelectList);
            sql.append(" from s_sample");
            sql.append(" where s_sampleid = ").append(safeSQL.addVar(this._SampleId));
            sql.append(" order by s_sampleid");
            this._Primary = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            safeSQL.reset();
            sql.setLength(0);
            sql.append("select ").append(datasetSelectList);
            sql.append(" from sdidata");
            sql.append(" where sdcid = 'Sample'");
            sql.append(" and keyid1 = ").append(safeSQL.addVar(this._SampleId));
            sql.append(" and keyid2 = '(null)'");
            sql.append(" and keyid3 = '(null)'");
            sql.append(OpalUtil.isNotEmpty(this._ParamlistId) ? " and paramlistid = " + safeSQL.addVar(this._ParamlistId) : "");
            sql.append(OpalUtil.isNotEmpty(this._ParamlistVersionId) ? " and paramlistversionid = " + safeSQL.addVar(this._ParamlistVersionId) : "");
            sql.append(OpalUtil.isNotEmpty(this._VariantId) ? " and variantid = " + safeSQL.addVar(this._VariantId) : "");
            sql.append(OpalUtil.isNotEmpty(this._Dataset) ? " and dataset = " + safeSQL.addVar(this._Dataset) : "");
            sql.append(" order by keyid1, paramlistid, paramlistversionid, variantid, dataset, usersequence");
            this._Datasets = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            safeSQL.reset();
            sql.setLength(0);
            sql.append("select ").append(dataApprovalSelectList);
            sql.append(" from sdidataapproval");
            sql.append(" where sdcid = 'Sample'");
            sql.append(" and keyid1 = ").append(safeSQL.addVar(this._SampleId));
            sql.append(" and keyid2 = '(null)'");
            sql.append(" and keyid3 = '(null)'");
            sql.append(OpalUtil.isNotEmpty(this._ParamlistId) ? " and paramlistid = " + safeSQL.addVar(this._ParamlistId) : "");
            sql.append(OpalUtil.isNotEmpty(this._ParamlistVersionId) ? " and paramlistversionid = " + safeSQL.addVar(this._ParamlistVersionId) : "");
            sql.append(OpalUtil.isNotEmpty(this._VariantId) ? " and variantid = " + safeSQL.addVar(this._VariantId) : "");
            sql.append(OpalUtil.isNotEmpty(this._Dataset) ? " and dataset = " + safeSQL.addVar(this._Dataset) : "");
            sql.append(" order by keyid1, paramlistid, paramlistversionid, variantid, dataset, usersequence");
            this._DataApprovals = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            safeSQL.reset();
            sql.setLength(0);
            sql.append("select ").append(dataitemSelectList);
            sql.append(" from sdidataitem");
            sql.append(" where sdcid = 'Sample' ");
            sql.append(" and keyid1 = ").append(safeSQL.addVar(this._SampleId));
            sql.append(" and keyid2 = '(null)' ");
            sql.append(" and keyid3 = '(null)' ");
            sql.append(OpalUtil.isNotEmpty(this._ParamlistId) ? " and paramlistid = " + safeSQL.addVar(this._ParamlistId) : "");
            sql.append(OpalUtil.isNotEmpty(this._ParamlistVersionId) ? " and paramlistversionid = " + safeSQL.addVar(this._ParamlistVersionId) : "");
            sql.append(OpalUtil.isNotEmpty(this._VariantId) ? " and variantid = " + safeSQL.addVar(this._VariantId) : "");
            sql.append(OpalUtil.isNotEmpty(this._Dataset) ? " and dataset = " + safeSQL.addVar(this._Dataset) : "");
            sql.append(OpalUtil.isNotEmpty(this._ParamId) ? " and paramid = " + safeSQL.addVar(this._ParamId) : "");
            sql.append(OpalUtil.isNotEmpty(this._ParamType) ? " and paramtype = " + safeSQL.addVar(this._ParamType) : "");
            sql.append(OpalUtil.isNotEmpty(this._ReplicateId) ? " and replicateid = " + safeSQL.addVar(this._ReplicateId) : "");
            sql.append(" order by keyid1, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, usersequence");
            this._Dataitems = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (this._ShowDataitemLimits) {
                safeSQL.reset();
                sql.setLength(0);
                sql.append("select ").append(dataitemlimitSelectList);
                sql.append(" from sdidataitemlimits");
                sql.append(" where sdcid = 'Sample' ");
                sql.append(" and keyid1 = ").append(safeSQL.addVar(this._SampleId));
                sql.append(" and keyid2 = '(null)'");
                sql.append(" and keyid3 = '(null)'");
                sql.append(OpalUtil.isNotEmpty(this._ParamlistId) ? " and paramlistid = " + safeSQL.addVar(this._ParamlistId) : "");
                sql.append(OpalUtil.isNotEmpty(this._ParamlistVersionId) ? " and paramlistversionid = " + safeSQL.addVar(this._ParamlistVersionId) : "");
                sql.append(OpalUtil.isNotEmpty(this._VariantId) ? " and variantid = " + safeSQL.addVar(this._VariantId) : "");
                sql.append(OpalUtil.isNotEmpty(this._Dataset) ? " and dataset = " + safeSQL.addVar(this._Dataset) : "");
                sql.append(OpalUtil.isNotEmpty(this._ParamId) ? " and paramid = " + safeSQL.addVar(this._ParamId) : "");
                sql.append(OpalUtil.isNotEmpty(this._ParamType) ? " and paramtype = " + safeSQL.addVar(this._ParamType) : "");
                sql.append(OpalUtil.isNotEmpty(this._ReplicateId) ? " and replicateid = " + safeSQL.addVar(this._ReplicateId) : "");
                sql.append(" order by keyid1, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, limittypeid, usersequence");
                this._DataitemLimits = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (this._ShowDataitemSpecs) {
                safeSQL.reset();
                sql.setLength(0);
                sql.append("SELECT sdis.sdcid, sdis.keyid1, sdis.keyid2, sdis.keyid3, sdis.usersequence,");
                sql.append(" sdis.paramlistid, sdis.paramlistversionid, sdis.variantid, sdis.dataset, sdis.paramid, sdis.paramtype, sdis.replicateid,");
                sql.append(" sdis.specid, sdis.specversionid, sdis.condition, spl.limittypesequence,");
                if (this.getConnectionProcessor().isOra()) {
                    sql.append(" spl.operator1 || spl.value1 val1, spl.operator2 || spl.value2 val2, spl.unitsid, ");
                    sql.append(" slt.limittypeid || '(' || slt.condition || ')' typecondition ");
                } else {
                    sql.append(" IsNull (spl.operator1, '') + IsNull (spl.value1,'') val1,");
                    sql.append(" IsNull (spl.operator2, '') + IsNull (spl.value2, '') val2, spl.unitsid,");
                    sql.append(" IsNull (slt.limittypeid,'') + '(' + IsNull ( slt.condition,'') + ')' typecondition");
                }
                sql.append(" FROM sdidataitemspec sdis, specparamitems spi, specparamlimits spl, speclimittype slt");
                sql.append(" WHERE sdis.sdcid = 'Sample'");
                sql.append(" and sdis.keyid1 = ").append(safeSQL.addVar(this._SampleId));
                sql.append(" AND sdis.keyid2 = '(null)'");
                sql.append(" AND sdis.keyid3 = '(null)'");
                sql.append(OpalUtil.isNotEmpty(this._ParamlistId) ? " and sdis.paramlistid = " + safeSQL.addVar(this._ParamlistId) : "");
                sql.append(OpalUtil.isNotEmpty(this._ParamlistVersionId) ? " and sdis.paramlistversionid = " + safeSQL.addVar(this._ParamlistVersionId) : "");
                sql.append(OpalUtil.isNotEmpty(this._VariantId) ? " and sdis.variantid = " + safeSQL.addVar(this._VariantId) : "");
                sql.append(OpalUtil.isNotEmpty(this._Dataset) ? " and sdis.dataset = " + safeSQL.addVar(this._Dataset) : "");
                sql.append(OpalUtil.isNotEmpty(this._ParamId) ? " and sdis.paramid = " + safeSQL.addVar(this._ParamId) : "");
                sql.append(OpalUtil.isNotEmpty(this._ParamType) ? " and sdis.paramtype = " + safeSQL.addVar(this._ParamType) : "");
                sql.append(OpalUtil.isNotEmpty(this._ReplicateId) ? " and sdis.replicateid = " + safeSQL.addVar(this._ReplicateId) : "");
                sql.append(OpalUtil.isNotEmpty(this._SpecId) ? " and sdis.specid = " + safeSQL.addVar(this._SpecId) : "");
                sql.append(OpalUtil.isNotEmpty(this._SpecVersionId) ? " and sdis.specversionid = " + safeSQL.addVar(this._SpecVersionId) : "");
                sql.append(" AND sdis.specid = spi.specid");
                sql.append(" AND sdis.specversionid = spi.specversionid");
                sql.append(" AND ((spi.allowanyparamlistflag = 'Y')");
                sql.append(" OR (sdis.paramlistid = spi.paramlistid");
                sql.append("   AND sdis.paramlistversionid = spi.paramlistversionid");
                sql.append("   AND sdis.variantid = spi.variantid");
                sql.append("   AND (spi.allowanyparamlistflag = 'N' OR spi.allowanyparamlistflag IS NULL OR spi.allowanyparamlistflag = ''))");
                sql.append("  OR");
                sql.append("  (sdis.paramlistid = spi.paramlistid AND sdis.variantid = spi.variantid AND spi.allowanyparamlistflag = 'V')");
                sql.append("  OR");
                sql.append("  (sdis.paramlistid = spi.paramlistid AND spi.allowanyparamlistflag = 'A'))");
                sql.append(" AND sdis.paramid = spi.paramid ");
                sql.append(" AND sdis.paramtype = spi.paramtype ");
                sql.append(" AND spl.specid = spi.specid ");
                sql.append(" AND spl.specversionid = spi.specversionid ");
                sql.append(" AND spl.paramlistid = spi.paramlistid ");
                sql.append(" AND spl.paramlistversionid = spi.paramlistversionid ");
                sql.append(" AND spl.variantid = spi.variantid ");
                sql.append(" AND spl.paramid = spi.paramid ");
                sql.append(" AND spl.paramtype = spi.paramtype ");
                sql.append(" AND slt.specid = spl.specid ");
                sql.append(" AND slt.specversionid = spl.specversionid ");
                sql.append(" AND slt.limittypesequence = spl.limittypesequence ");
                sql.append(" ORDER BY sdis.keyid1, sdis.paramlistid, sdis.paramlistversionid, sdis.variantid,  ");
                sql.append(" sdis.dataset, sdis.paramid, sdis.paramtype, sdis.replicateid,  ");
                sql.append(" sdis.specid, sdis.specversionid, sdis.usersequence");
                this._DataitemSpecs = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            safeSQL.reset();
            sql.setLength(0);
            sql.append("select sdispec.keyid1, sdispec.specid, sdispec.specversionid, specdesc, versionstatus, sampledesc");
            sql.append(" from sdispec, spec, s_sample");
            sql.append(" where sdcid = 'Sample'");
            sql.append(" and keyid1 = ").append(safeSQL.addVar(this._SampleId));
            sql.append(" and keyid2 = '(null)'");
            sql.append(" and keyid3 = '(null)'");
            sql.append(OpalUtil.isNotEmpty(this._SpecId) ? " and sdispec.specid = " + safeSQL.addVar(this._SpecId) : "");
            sql.append(OpalUtil.isNotEmpty(this._SpecVersionId) ? " and sdispec.specversionid = " + safeSQL.addVar(this._SpecVersionId) : "");
            sql.append(" and sdispec.specid = spec.specid");
            sql.append(" and sdispec.specversionid = spec.specversionid");
            sql.append(" and sdispec.keyid1 = s_sample.s_sampleid");
            sql.append(" order by keyid1, sdispec.specid, sdispec.specversionid, sdispec.usersequence");
            this._DataSpecs = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            return true;
        }
        catch (Exception ex) {
            this.logger.error("OPAL_ERR: Got an exception at DataDetailUtil.prepareSqls() -> " + ex, ex);
            return false;
        }
    }

    @Override
    public String getHtml() {
        return "";
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
        String sqlWhere = "";
        sqlWhere = where.indexOf(";") >= 0 ? (type.equalsIgnoreCase("C") ? " in ('" + where.replaceAll(";", "','") + "') " : " in (" + where.replaceAll(";", ", ") + ") ") : (type.equalsIgnoreCase("C") ? " = '" + where + "' " : " = " + where + " ");
        return sqlWhere;
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

