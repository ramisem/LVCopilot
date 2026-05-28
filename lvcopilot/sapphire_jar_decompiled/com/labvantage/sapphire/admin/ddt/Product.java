/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.sdidata.ExtendDataSet;
import com.labvantage.sapphire.pageelements.simplespec.action.GetNewSPId;
import com.labvantage.sapphire.pageelements.simplespec.action.GetNewSpecId;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Product
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 83811 $";
    public static final String SDCID = "Product";
    public static final String TABLEID = "s_product";
    public static final String COLUMN_S_PRODUCTID = "s_productid";
    public static final String COLUMN_S_PRODUCTVERSIONID = "s_productversionid";
    public static final String COLUMN_EMBEDDEDSPECID = "embeddedspecid";
    public static final String COLUMN_EMBEDDEDSPECVERSIONID = "embeddedspecversionid";
    public static final String COLUMN_EMBEDDEDSAMPLINGPLANID = "embeddedsamplingplanid";
    public static final String COLUMN_EMBEDDEDSAMPLINGPLANVERSIONID = "embeddedsamplingplanversionid";
    public static final String COLUMN_PRODUCTMODEFLAG = "productmodeflag";
    public static final String COLUMN_VERSIONSTATUS = "versionstatus";
    public static final String COLUMN_FORMULATIONPROJECTID = "formulationprojectid";
    public static final String COLUMN_TEMPLATEFLAG = "templateflag";
    public static final String COLUMN_SAMPLETYPEID = "sampletypeid";
    public static final String COLUMN_FORMULATIONLABEL = "formulationlabel";
    public static final String COLUMN_FORMULATIONITERATIONFLAG = "formulationiterationflag";
    public static final String COLUMN_USERSEQUENCE = "usersequence";
    public static final String COLUMN_BATCHSAMPLECOUNT = "batchsamplecount";
    public static final String COLUMN_FORMULATIONSTATUS = "formulationstatus";
    public static final String COLUMN_PARENTPRODUCTID = "parentproductid";
    public static final String COLUMN_PARENTPRODUCTVERSIONID = "parentproductversionid";
    public static final String COLUMN_RECIPETYPEFLAG = "recipetypeflag";
    public static final String FORMULATIONSTATUS_DRAFT = "Draft";
    public static final String FORMULATIONSTATUS_PROMOTED = "Promoted";
    public static final String PRODUCTINFO_PARAMLISTID = "ProductInformation";
    public static final String PRODUCTMODEFLAG_SIMPLE = "S";
    public static final String PRODUCTMODEFLAG_FULL = "F";
    public static final String PARAMTYPE_QUANTITY = "Quantity";
    public static final String PARAMTYPE_FRACTION = "Fraction";
    public static final String SAMPLETYPEID_DEVELOPMENTFORMULATION = "Development Formulation";
    public static final String SAMPLETYPEID_PILOTFORMULATION = "Pilot Formulation";

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String templateKeyId1 = actionProps.getProperty("templatekeyid1", "");
        String templateKeyId2 = actionProps.getProperty("templatekeyid2", "1");
        boolean isPromoteOperation = actionProps.getProperty("operation", "").equals("promoteformulation");
        DataSet dsPrimary = sdiData.getDataset("primary");
        boolean doNotCopyFormulationMethodDataSets = false;
        if (!this.isCMTImport() && templateKeyId1.length() > 0) {
            String templateFlag;
            SafeSQL safeSQL = new SafeSQL();
            DataSet templateDs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT templateflag FROM s_product WHERE s_productid = " + safeSQL.addVar(templateKeyId1) + " AND " + COLUMN_S_PRODUCTVERSIONID + " = " + safeSQL.addVar(templateKeyId2), safeSQL.getValues());
            if (templateDs.getRowCount() > 0 && (templateFlag = templateDs.getValue(0, COLUMN_TEMPLATEFLAG, "N")).equals("N")) {
                doNotCopyFormulationMethodDataSets = true;
            }
            String newProductId = actionProps.getProperty("newkeyid1");
            String newProductVersionId = actionProps.getProperty("newkeyid2");
            this.copyProdVarDetails(templateKeyId1, templateKeyId2, newProductId, newProductVersionId);
            this.copyProductStages(templateKeyId1, templateKeyId2, newProductId, newProductVersionId, doNotCopyFormulationMethodDataSets, isPromoteOperation);
            this.copyProductIngredients(templateKeyId1, templateKeyId2, newProductId, newProductVersionId, doNotCopyFormulationMethodDataSets, isPromoteOperation);
            this.copyProductEquipments(templateKeyId1, templateKeyId2, newProductId, newProductVersionId, isPromoteOperation);
            this.copyProductInformation(dsPrimary, templateKeyId1, templateKeyId2, isPromoteOperation);
        }
        if (!this.isCMTImport() && !doNotCopyFormulationMethodDataSets) {
            this.createFormulationDataSets(dsPrimary);
        }
        if (!this.isCMTImport() && !isPromoteOperation) {
            this.setRelationToBaseFormulation(dsPrimary);
        }
    }

    private void copyProductInformation(DataSet dsPrimary, String templateKeyId1, String templateKeyId2, boolean isPromoteOperation) throws SapphireException {
        for (int i = 0; i < dsPrimary.getRowCount(); ++i) {
            String productId = dsPrimary.getValue(i, COLUMN_S_PRODUCTID, "");
            String productVersionId = dsPrimary.getValue(i, COLUMN_S_PRODUCTVERSIONID, "");
            SafeSQL safeSQL = new SafeSQL();
            if (isPromoteOperation) {
                String sql = "SELECT " + safeSQL.addVar(PRODUCTINFO_PARAMLISTID) + " AS paramlistid, '1' AS paramlistversionid, '1' AS variantid";
                sql = sql + ", " + safeSQL.addVar(productId) + " AS s_productid, " + safeSQL.addVar(productVersionId) + " AS s_productversionid";
                sql = sql + ", paramlistitem.mappingparamid AS paramid, paramlistitem.mappingparamtype AS paramtype ";
                sql = sql + ", sdidataitem.replicateid AS replicateid, sdidataitem.dataset AS dataset";
                sql = sql + ", CONCAT( sdidataitem.transformvalue, sdidataitem.displayunits ) AS enteredtext, sdidataitem.transformvalue AS transformvalue";
                sql = sql + " FROM sdidataitem, paramlistitem";
                sql = sql + " WHERE sdidataitem.sdcid =" + safeSQL.addVar(SDCID) + " AND sdidataitem.keyid1 = " + safeSQL.addVar(templateKeyId1) + " AND sdidataitem.keyid2 = " + safeSQL.addVar(templateKeyId2) + " ";
                sql = sql + " AND sdidataitem.paramlistid = paramlistitem.paramlistid AND sdidataitem.paramlistversionid = paramlistitem.paramlistversionid ";
                sql = sql + " AND sdidataitem.variantid = paramlistitem.variantid AND sdidataitem.paramid = paramlistitem.paramid";
                sql = sql + " AND sdidataitem.paramtype = paramlistitem.paramtype AND paramlistitem.mappingparamid IS NOT NULL";
                this.database.createPreparedResultSet("getproductinfo", sql, safeSQL.getValues());
            } else {
                this.database.createPreparedResultSet("getproductinfo", "select paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, enteredtext  from sdidataitem where sdcid = 'Product' and keyid1 = " + safeSQL.addVar(templateKeyId1) + " and keyid2 = " + safeSQL.addVar(templateKeyId2) + " and paramlistid = " + safeSQL.addVar(PRODUCTINFO_PARAMLISTID) + " and paramlistversionid = '1' and variantid = '1' and dataset = '1'", safeSQL.getValues());
            }
            DataSet ds = new DataSet(this.database.getResultSet("getproductinfo"));
            if (ds.getRowCount() <= 0) continue;
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDCID);
            props.setProperty("keyid1", productId);
            props.setProperty("keyid2", productVersionId);
            props.setProperty("paramlistid", PRODUCTINFO_PARAMLISTID);
            props.setProperty("paramlistversionid", "1");
            props.setProperty("variantid", "1");
            props.setProperty("addnewonly", "Y");
            this.getActionProcessor().processAction("AddDataSet", "1", props);
            if (isPromoteOperation) {
                props.clear();
                props.setProperty("sdcid", SDCID);
                props.setProperty("keyid1", ds.getColumnValues(COLUMN_S_PRODUCTID, ";"));
                props.setProperty("keyid2", ds.getColumnValues(COLUMN_S_PRODUCTVERSIONID, ";"));
                props.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
                props.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
                props.setProperty("variantid", ds.getColumnValues("variantid", ";"));
                props.setProperty("dataset", ds.getColumnValues("dataset", ";"));
                props.setProperty("paramid", ds.getColumnValues("paramid", ";"));
                props.setProperty("paramtype", ds.getColumnValues("paramtype", ";"));
                props.setProperty("replicateid", ds.getColumnValues("replicateid", ";"));
                props.setProperty("propsmatch", "Y");
                this.getActionProcessor().processActionClass(ExtendDataSet.class.getName(), props);
            }
            if (isPromoteOperation) {
                HashMap<String, Object> filter = new HashMap<String, Object>();
                filter.put("transformvalue", null);
                ds = ds.getFilteredDataSet(filter, true);
            }
            props.clear();
            props.setProperty("sdcid", SDCID);
            props.setProperty("keyid1", productId);
            props.setProperty("keyid2", productVersionId);
            props.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
            props.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
            props.setProperty("variantid", ds.getColumnValues("variantid", ";"));
            props.setProperty("dataset", ds.getColumnValues("dataset", ";"));
            props.setProperty("paramid", ds.getColumnValues("paramid", ";"));
            props.setProperty("paramtype", ds.getColumnValues("paramtype", ";"));
            props.setProperty("replicateid", ds.getColumnValues("replicateid", ";"));
            props.setProperty("enteredtext", ds.getColumnValues("enteredtext", ";"));
            props.setProperty("propsmatch", "N");
            this.getActionProcessor().processAction("EnterDataItem", "1", props);
        }
    }

    private void setRelationToBaseFormulation(DataSet primary) throws SapphireException {
        DataSet dsAddRelation = new DataSet();
        SequenceProcessor scp = this.getSequenceProcessor();
        String relationType = "Comparator";
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String productId = primary.getValue(i, COLUMN_S_PRODUCTID);
            String productVersionId = primary.getValue(i, COLUMN_S_PRODUCTVERSIONID);
            String fProjId = primary.getValue(i, COLUMN_FORMULATIONPROJECTID);
            if (fProjId.length() <= 0) continue;
            this.database.createPreparedResultSet("getbaseformulation", "select tokeyid1, tokeyid2 from sdirelation s, s_product p  where s.tosdcid = 'Product' and s.fromsdcid = 'Product' and s.fromkeyid1 = p.s_productid and s.fromkeyid2 = p.s_productversionid  and p.formulationprojectid = ?", new String[]{fProjId});
            DataSet ds = new DataSet(this.database.getResultSet("getbaseformulation"));
            this.database.closeResultSet("getbaseformulation");
            if (ds.getRowCount() <= 0) continue;
            int r = dsAddRelation.addRow();
            String baseFormulation = ds.getValue(0, "tokeyid1");
            String baseFormulationVersion = ds.getValue(0, "tokeyid2");
            dsAddRelation.setString(r, "sdirelationid", relationType + "-" + scp.getSequence("SDIRelation", relationType));
            dsAddRelation.setString(r, "fromsdcid", SDCID);
            dsAddRelation.setString(r, "fromkeyid1", productId);
            dsAddRelation.setString(r, "fromkeyid2", productVersionId);
            dsAddRelation.setString(r, "fromkeyid3", "(null)");
            dsAddRelation.setString(r, "relationtype", relationType);
            dsAddRelation.setString(r, "tosdcid", SDCID);
            dsAddRelation.setString(r, "tokeyid1", baseFormulation);
            dsAddRelation.setString(r, "tokeyid2", baseFormulationVersion);
            dsAddRelation.setString(r, "tokeyid3", "(null)");
        }
        if (dsAddRelation.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.put("sdcid", "SDIRelation");
            props.put("overrideautokey", "Y");
            props.put("keyid1", dsAddRelation.getColumnValues("sdirelationid", ";"));
            props.put("fromsdcid", dsAddRelation.getColumnValues("fromsdcid", ";"));
            props.put("fromkeyid1", dsAddRelation.getColumnValues("fromkeyid1", ";"));
            props.put("fromkeyid2", dsAddRelation.getColumnValues("fromkeyid2", ";"));
            props.put("fromkeyid3", dsAddRelation.getColumnValues("fromkeyid3", ";"));
            props.put("tosdcid", dsAddRelation.getColumnValues("tosdcid", ";"));
            props.put("tokeyid1", dsAddRelation.getColumnValues("tokeyid1", ";"));
            props.put("tokeyid2", dsAddRelation.getColumnValues("tokeyid2", ";"));
            props.put("tokeyid3", dsAddRelation.getColumnValues("tokeyid3", ";"));
            props.put("relationtype", dsAddRelation.getColumnValues("relationtype", ";"));
            this.getActionProcessor().processAction("AddSDI", "1", props);
        }
    }

    private void createFormulationDataSets(DataSet primary) throws SapphireException {
        DataSet dsProps = new DataSet();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String productId = primary.getValue(i, COLUMN_S_PRODUCTID);
            String productVersionId = primary.getValue(i, COLUMN_S_PRODUCTVERSIONID);
            String fProjId = primary.getValue(i, COLUMN_FORMULATIONPROJECTID);
            if (fProjId.length() <= 0) continue;
            this.database.createPreparedResultSet("getparamlist", "select pl.paramlistid, pl.paramlistversionid, pl.variantid, pl.targetuse from formulationmethodparamlist pl,formulationprojectmethod fm where pl.formulationmethodid = fm.formulationmethodid and fm.formulationprojectid = ? and pl.targetuse = 'A'", new String[]{fProjId});
            DataSet ds = new DataSet(this.database.getResultSet("getparamlist"));
            this.database.closeResultSet("getparamlist");
            if (ds.getRowCount() <= 0) continue;
            for (int k = 0; k < ds.getRowCount(); ++k) {
                int r = dsProps.addRow();
                dsProps.setString(r, "keyid1", productId);
                dsProps.setString(r, "keyid2", productVersionId);
                dsProps.setString(r, "paramlistid", ds.getValue(k, "paramlistid"));
                dsProps.setString(r, "paramlistversionid", ds.getValue(k, "paramlistversionid"));
                dsProps.setString(r, "variantid", ds.getValue(k, "variantid"));
            }
        }
        if (dsProps.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDCID);
            props.setProperty("keyid1", dsProps.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", dsProps.getColumnValues("keyid2", ";"));
            props.setProperty("paramlistid", dsProps.getColumnValues("paramlistid", ";"));
            props.setProperty("paramlistversionid", dsProps.getColumnValues("paramlistversionid", ";"));
            props.setProperty("variantid", dsProps.getColumnValues("variantid", ";"));
            props.setProperty("addnewonly", "Y");
            props.setProperty("propsmatch", "Y");
            this.getActionProcessor().processAction("AddDataSet", "1", props);
        }
    }

    @Override
    public void postAddKey(DataSet primary, PropertyList actionProps) {
        if (!"Y".equalsIgnoreCase(actionProps.getProperty("copysdi", "N"))) {
            this.processFormulationTasks(primary, actionProps);
        }
    }

    private void processFormulationTasks(DataSet primary, PropertyList actionProps) {
        boolean isPromoteOperation = actionProps.getProperty("operation", "").equals("promoteformulation");
        boolean doOverrideAutoKey = actionProps.getProperty("overrideautokey", "").equals("Y");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String formulationProjectId = primary.getValue(i, COLUMN_FORMULATIONPROJECTID, "");
            if (formulationProjectId.length() <= 0) continue;
            if (isPromoteOperation) {
                primary.setString(i, COLUMN_FORMULATIONSTATUS, "(null)");
                primary.setString(i, COLUMN_FORMULATIONITERATIONFLAG, "N");
                continue;
            }
            primary.setString(i, COLUMN_FORMULATIONITERATIONFLAG, primary.getString(i, COLUMN_FORMULATIONITERATIONFLAG, "Y"));
            if (primary.getValue(i, COLUMN_TEMPLATEFLAG, "N").equals("Y")) {
                String id = formulationProjectId + "-template";
                primary.setString(i, COLUMN_S_PRODUCTID, id);
            } else if (!doOverrideAutoKey) {
                int sequence = this.getSequenceProcessor().getSequence(SDCID, formulationProjectId);
                String id = formulationProjectId + "-" + sequence;
                primary.setString(i, COLUMN_S_PRODUCTID, id);
                primary.setString(i, COLUMN_FORMULATIONSTATUS, FORMULATIONSTATUS_DRAFT);
            }
            primary.setString(i, COLUMN_S_PRODUCTVERSIONID, primary.getValue(i, COLUMN_S_PRODUCTVERSIONID, "1"));
        }
    }

    private void copyProductStages(String templateKeyId1, String templateKeyId2, String newProductId, String newProductVersionId, boolean doNotCopyFormulationMethodDataSets, boolean isPromoteOperation) throws ActionException {
        SDIRequest parentProductStagesSDIRequest = new SDIRequest();
        parentProductStagesSDIRequest.setSDCid("LV_ProductStage");
        parentProductStagesSDIRequest.setRequestItem("primary");
        parentProductStagesSDIRequest.setQueryFrom("s_productstage");
        parentProductStagesSDIRequest.setQueryWhere("s_productid ='" + SafeSQL.encodeForSQL(templateKeyId1, this.database.isOracle()) + "' AND " + COLUMN_S_PRODUCTVERSIONID + " ='" + SafeSQL.encodeForSQL(templateKeyId2, this.database.isOracle()) + "'");
        SDIData parentProductStagesSDIData = this.getSDIProcessor().getSDIData(parentProductStagesSDIRequest);
        DataSet parentProductStagesDataSet = parentProductStagesSDIData.getDataset("primary");
        ActionBlock actionBlock = new ActionBlock();
        for (int i = 0; i < parentProductStagesDataSet.getRowCount(); ++i) {
            PropertyList addStagesProps = new PropertyList();
            addStagesProps.setProperty("keyid1", newProductId);
            addStagesProps.setProperty("keyid2", newProductVersionId);
            addStagesProps.setProperty("sdcid", "LV_ProductStage");
            addStagesProps.setProperty("templatekeyid1", parentProductStagesDataSet.getValue(i, COLUMN_S_PRODUCTID));
            addStagesProps.setProperty("templatekeyid2", parentProductStagesDataSet.getValue(i, COLUMN_S_PRODUCTVERSIONID));
            addStagesProps.setProperty("templatekeyid3", parentProductStagesDataSet.getValue(i, "s_productstageid"));
            if (doNotCopyFormulationMethodDataSets) {
                addStagesProps.setProperty("donotcopyformulationmethoddatasets", "Y");
            }
            if (isPromoteOperation) {
                addStagesProps.setProperty("operation", "promoteformulation");
            }
            addStagesProps.setProperty("copyattachment", "Y");
            actionBlock.setAction("CopyStages" + i, "AddSDI", "1", addStagesProps);
        }
        this.getActionProcessor().processActionBlock(actionBlock);
    }

    private void copyProductEquipments(String templateKeyId1, String templateKeyId2, String newProductId, String newProductVersionId, boolean isPromoteOperation) throws ActionException {
        SDIRequest parentProductEquipmentsSDIRequest = new SDIRequest();
        parentProductEquipmentsSDIRequest.setSDCid("LV_ProductInstrument");
        parentProductEquipmentsSDIRequest.setRequestItem("primary");
        parentProductEquipmentsSDIRequest.setQueryFrom("s_productinstrument");
        parentProductEquipmentsSDIRequest.setQueryWhere("s_productid ='" + SafeSQL.encodeForSQL(templateKeyId1, this.database.isOracle()) + "' AND " + COLUMN_S_PRODUCTVERSIONID + " ='" + SafeSQL.encodeForSQL(templateKeyId2, this.database.isOracle()) + "'");
        SDIData parentProductEquipmentsSDIData = this.getSDIProcessor().getSDIData(parentProductEquipmentsSDIRequest);
        DataSet parentProductEquipmentsSDI = parentProductEquipmentsSDIData.getDataset("primary");
        ActionBlock actionBlock = new ActionBlock();
        for (int i = 0; i < parentProductEquipmentsSDI.getRowCount(); ++i) {
            PropertyList addEquipmentsProps = new PropertyList();
            addEquipmentsProps.setProperty("sdcid", "LV_ProductInstrument");
            addEquipmentsProps.setProperty("keyid1", newProductId);
            addEquipmentsProps.setProperty("keyid2", newProductVersionId);
            addEquipmentsProps.setProperty("keyid3", parentProductEquipmentsSDI.getValue(i, "s_productinstrumentid"));
            addEquipmentsProps.setProperty("templatekeyid1", parentProductEquipmentsSDI.getValue(i, COLUMN_S_PRODUCTID));
            addEquipmentsProps.setProperty("templatekeyid2", parentProductEquipmentsSDI.getValue(i, COLUMN_S_PRODUCTVERSIONID));
            addEquipmentsProps.setProperty("templatekeyid3", parentProductEquipmentsSDI.getValue(i, "s_productinstrumentid"));
            if (isPromoteOperation) {
                addEquipmentsProps.setProperty("operation", "promoteformulation");
            }
            actionBlock.setAction("CopyInstruments" + i, "AddSDI", "1", addEquipmentsProps);
        }
        this.getActionProcessor().processActionBlock(actionBlock);
    }

    private void copyProductIngredients(String templateKeyId1, String templateKeyId2, String newProductId, String newProductVersionId, boolean doNotCopyFormulationMethodDataSets, boolean isPromoteOperation) throws ActionException {
        SDIRequest parentProductIngredientsSDIRequest = new SDIRequest();
        parentProductIngredientsSDIRequest.setSDCid("LV_ProductIngredient");
        parentProductIngredientsSDIRequest.setRequestItem("primary");
        parentProductIngredientsSDIRequest.setQueryFrom("s_productformulation");
        parentProductIngredientsSDIRequest.setQueryWhere("s_productid ='" + SafeSQL.encodeForSQL(templateKeyId1, this.database.isOracle()) + "' AND " + COLUMN_S_PRODUCTVERSIONID + " ='" + SafeSQL.encodeForSQL(templateKeyId2, this.database.isOracle()) + "'");
        SDIData parentProductIngredientsSDIData = this.getSDIProcessor().getSDIData(parentProductIngredientsSDIRequest);
        DataSet parentProductIngredientsDataSet = parentProductIngredientsSDIData.getDataset("primary");
        ActionBlock actionBlock = new ActionBlock();
        for (int i = 0; i < parentProductIngredientsDataSet.getRowCount(); ++i) {
            PropertyList addIngredientsProps = new PropertyList();
            addIngredientsProps.setProperty("keyid1", newProductId);
            addIngredientsProps.setProperty("keyid2", newProductVersionId);
            addIngredientsProps.setProperty("sdcid", "LV_ProductIngredient");
            addIngredientsProps.setProperty("templatekeyid1", parentProductIngredientsDataSet.getValue(i, COLUMN_S_PRODUCTID));
            addIngredientsProps.setProperty("templatekeyid2", parentProductIngredientsDataSet.getValue(i, COLUMN_S_PRODUCTVERSIONID));
            addIngredientsProps.setProperty("templatekeyid3", parentProductIngredientsDataSet.getValue(i, "s_productitemid"));
            if (doNotCopyFormulationMethodDataSets) {
                addIngredientsProps.setProperty("donotcopyformulationmethoddatasets", "Y");
            }
            if (isPromoteOperation) {
                addIngredientsProps.setProperty("operation", "promoteformulation");
            }
            actionBlock.setAction("CopyIngredients" + i, "AddSDI", "1", addIngredientsProps);
        }
        this.getActionProcessor().processActionBlock(actionBlock);
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String templateKeyId1 = actionProps.getProperty("templateid").length() > 0 ? actionProps.getProperty("templateid") : actionProps.getProperty("templatekeyid1");
        String templateKeyId2 = actionProps.getProperty("templatekeyid2", "");
        boolean isPromoteOperation = actionProps.getProperty("operation", "").equals("promoteformulation");
        if (templateKeyId1.length() > 0 && !isPromoteOperation) {
            this.copyEmbeddedSpecAndSamplingPlan(templateKeyId1, templateKeyId2, sdiData);
        }
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String productModeFlag = primary.getValue(i, COLUMN_PRODUCTMODEFLAG, "");
            if (productModeFlag.length() != 0) continue;
            primary.setString(i, COLUMN_PRODUCTMODEFLAG, PRODUCTMODEFLAG_FULL);
        }
        if (!isPromoteOperation) {
            this.checkFormulationMethod(primary);
        }
        this.checkSiteLabWorkAreaHierarchy(primary);
    }

    private void checkFormulationMethod(DataSet primary) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        StringBuffer errMsg = new StringBuffer();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String fProjId;
            if ("Y".equalsIgnoreCase(primary.getValue(i, COLUMN_TEMPLATEFLAG)) || !"Y".equalsIgnoreCase(primary.getValue(i, COLUMN_FORMULATIONITERATIONFLAG)) || (fProjId = primary.getValue(i, COLUMN_FORMULATIONPROJECTID)).length() <= 0) continue;
            this.database.createPreparedResultSet("paramdetail", "select p.paramid, p.paramtype, fp.recipetypeflag from paramlistitem p, formulationmethodparamlist fmpl, formulationprojectmethod fpm, formulationproject fp   where p.paramlistid = fmpl.paramlistid and p.paramlistversionid = fmpl.paramlistversionid and p.variantid = fmpl.variantid and  fmpl.formulationmethodid = fpm.formulationmethodid and fmpl.formulationmethodversionid = fpm.formulationmethodversionid  and fpm.formulationprojectid = fp.formulationprojectid and fp.formulationprojectid = ?", new String[]{fProjId});
            DataSet ds = new DataSet(this.database.getResultSet("paramdetail"));
            if (ds.getRowCount() == 0) {
                this.throwError("Validation Failed", "VALIDATION", tp.translate("To create Formulation, Project must be defined with Formulation Method ParamLists."));
                continue;
            }
            boolean absoluteQty = "A".equalsIgnoreCase(ds.getValue(0, COLUMN_RECIPETYPEFLAG));
            int ptypeQuantity = 0;
            int ptypeFraction = 0;
            for (int d = 0; d < ds.getRowCount(); ++d) {
                if (PARAMTYPE_QUANTITY.equals(ds.getValue(d, "paramtype"))) {
                    ++ptypeQuantity;
                    continue;
                }
                if (!PARAMTYPE_FRACTION.equals(ds.getValue(d, "paramtype"))) continue;
                ++ptypeFraction;
            }
            if (absoluteQty) {
                if (ptypeQuantity != 0) continue;
                errMsg.append(tp.translate("Absolute Quantities Formulation recipe requires a parameter with paramtype ") + " '" + PARAMTYPE_QUANTITY + "'.");
                this.throwError("Validation Failed", "VALIDATION", errMsg.toString());
                continue;
            }
            if (ptypeFraction != 0) continue;
            errMsg.append(tp.translate("Proportional Quantities Formulation recipe requires a parameter with paramtype ") + " '" + PARAMTYPE_FRACTION + "'.");
            this.throwError("Validation Failed", "VALIDATION", errMsg.toString());
        }
    }

    private void copyEmbeddedSpecAndSamplingPlan(String templateKeyId1, String templateKeyId2, SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            SDI newEmbeddedSamplingPlan;
            boolean isUpVersioned;
            String productModeFlag = primary.getValue(i, COLUMN_PRODUCTMODEFLAG, PRODUCTMODEFLAG_FULL);
            if (!productModeFlag.equals(PRODUCTMODEFLAG_SIMPLE)) continue;
            String embeddedSpecId = primary.getValue(i, COLUMN_EMBEDDEDSPECID, "");
            String embeddedSpecVersionId = primary.getValue(i, COLUMN_EMBEDDEDSPECVERSIONID, "");
            String embeddedSamplingPlanId = primary.getValue(i, COLUMN_EMBEDDEDSAMPLINGPLANID, "");
            String embeddedSamplingPlanVersionId = primary.getValue(i, COLUMN_EMBEDDEDSAMPLINGPLANVERSIONID, "");
            String productId = primary.getValue(i, COLUMN_S_PRODUCTID, "");
            String productVersionId = primary.getValue(i, COLUMN_S_PRODUCTVERSIONID, "");
            boolean bl = isUpVersioned = templateKeyId1.equals(productId) && !templateKeyId2.equals(productVersionId);
            if (embeddedSpecId.length() > 0) {
                SDI newEmbeddedSpec;
                String newSpecId = null;
                if (!isUpVersioned) {
                    PropertyList getNewSpecIdProps = new PropertyList();
                    getNewSpecIdProps.setProperty("sdcid", SDCID);
                    getNewSpecIdProps.setProperty("keyid1", productId);
                    this.getActionProcessor().processActionClass(GetNewSpecId.class.getName(), getNewSpecIdProps);
                    newSpecId = getNewSpecIdProps.getProperty("newspecid");
                }
                if ((newEmbeddedSpec = this.createNewEmbeddedSDI(isUpVersioned, "SpecSDC", embeddedSpecId, embeddedSpecVersionId, null, newSpecId, "1", null, "embeddedflag")) != null) {
                    primary.setValue(i, COLUMN_EMBEDDEDSPECID, newEmbeddedSpec.getKeyid1());
                    primary.setValue(i, COLUMN_EMBEDDEDSPECVERSIONID, newEmbeddedSpec.getKeyid2());
                }
            }
            if (embeddedSamplingPlanId.length() <= 0) continue;
            String newSamplingPlanId = null;
            if (!isUpVersioned) {
                PropertyList getNewSamplingPlanIdProps = new PropertyList();
                getNewSamplingPlanIdProps.setProperty("sdcid", SDCID);
                getNewSamplingPlanIdProps.setProperty("keyid1", productId);
                this.getActionProcessor().processActionClass(GetNewSPId.class.getName(), getNewSamplingPlanIdProps);
                newSamplingPlanId = getNewSamplingPlanIdProps.getProperty("newsamplingplanid");
            }
            if ((newEmbeddedSamplingPlan = this.createNewEmbeddedSDI(isUpVersioned, "LV_SamplingPlan", embeddedSamplingPlanId, embeddedSamplingPlanVersionId, null, newSamplingPlanId, "1", null, "embeddedflag")) == null) continue;
            primary.setValue(i, COLUMN_EMBEDDEDSAMPLINGPLANID, newEmbeddedSamplingPlan.getKeyid1());
            primary.setValue(i, COLUMN_EMBEDDEDSAMPLINGPLANVERSIONID, newEmbeddedSamplingPlan.getKeyid2());
        }
    }

    private SDI createNewEmbeddedSDI(boolean isUpVersioned, String sdcId, String embeddedTemplateKeyId1, String embeddedTemplateKeyId2, String embeddedTemplateKeyId3, String newKeyId1, String newKeyId2, String newKeyid3, String embeddedFlagColumnName) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty(embeddedFlagColumnName, "Y");
        if (isUpVersioned) {
            props.setProperty("sdcid", sdcId);
            props.setProperty("keyid1", embeddedTemplateKeyId1);
            if (embeddedTemplateKeyId2 != null && embeddedTemplateKeyId2.length() > 0) {
                props.setProperty("keyid2", embeddedTemplateKeyId2);
            }
            if (embeddedTemplateKeyId3 != null && embeddedTemplateKeyId3.length() > 0) {
                props.setProperty("keyid3", embeddedTemplateKeyId3);
            }
            this.getActionProcessor().processAction("AddSDIVersion", "1", props);
            return new SDI(sdcId, props.getProperty("newkeyid1"), props.getProperty("newkeyid2"), null);
        }
        props.setProperty("sdcid", sdcId);
        props.setProperty("templatekeyid1", embeddedTemplateKeyId1);
        props.setProperty("keyid1", newKeyId1);
        if (embeddedTemplateKeyId2 != null && embeddedTemplateKeyId2.length() > 0) {
            props.setProperty("keyid2", newKeyId2);
            props.setProperty("templatekeyid2", embeddedTemplateKeyId2);
        }
        if (embeddedTemplateKeyId3 != null && embeddedTemplateKeyId3.length() > 0) {
            props.setProperty("keyid3", newKeyid3);
            props.setProperty("templatekeyid3", embeddedTemplateKeyId3);
        }
        props.setProperty("copies", "1");
        this.getActionProcessor().processAction("AddSDI", "1", props);
        return new SDI(sdcId, props.getProperty("newkeyid1"), props.getProperty("newkeyid2"), null);
    }

    private void copyProdVarDetails(String templateKeyId1, String templateKeyId2, String newProductId, String newProductVersionId) throws SapphireException {
        ActionBlock actionBlock = new ActionBlock();
        SafeSQL safeSQL = new SafeSQL();
        String prodVarSQL = "SELECT s_prodvariantid FROM s_prodvariant WHERE productid = " + safeSQL.addVar(templateKeyId1) + " AND productversionid = " + safeSQL.addVar(templateKeyId2);
        DataSet prodVarDS = this.getQueryProcessor().getPreparedSqlDataSet(prodVarSQL, safeSQL.getValues());
        String[] newProductIdArr = StringUtil.split(newProductId, ";");
        String copies = "" + newProductIdArr.length;
        if (prodVarDS != null && prodVarDS.size() > 0) {
            for (int i = 0; i < prodVarDS.size(); ++i) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_ProdVariant");
                props.setProperty("templateid", prodVarDS.getValue(i, "s_prodvariantid"));
                props.setProperty("copies", copies);
                props.setProperty("productid", newProductId);
                props.setProperty("productversionid", newProductVersionId);
                actionBlock.setAction("AddSDI" + (i + 1), "AddSDI", "1", props);
            }
            this.getActionProcessor().processActionBlock(actionBlock);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.validateProdVarExists(primary);
        this.checkSiteLabWorkAreaHierarchy(primary);
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.syncVersionStatusOfEmbeddedSpecAndSamplingPlan(primary);
        this.cancelFormulationBatch(primary);
        this.unCancelFormulationBatch(primary);
        this.callRedoCalcOnProduct(primary);
    }

    private void syncVersionStatusOfEmbeddedSpecAndSamplingPlan(DataSet primary) throws ActionException {
        StringBuffer embeddedSpecIdBuffer = new StringBuffer();
        StringBuffer embeddedSpecVersionIdBuffer = new StringBuffer();
        StringBuffer embeddedSpecVersionStatusBuffer = new StringBuffer();
        StringBuffer embeddedSamplingPlanIdBuffer = new StringBuffer();
        StringBuffer embeddedSamplingPlanVersionIdBuffer = new StringBuffer();
        StringBuffer embeddedSamplingPlanVersionStatusBuffer = new StringBuffer();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String productModeFlag = this.getOldPrimaryValue(primary, i, COLUMN_PRODUCTMODEFLAG);
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_VERSIONSTATUS) || productModeFlag == null || !productModeFlag.equals(PRODUCTMODEFLAG_SIMPLE)) continue;
            String embeddedSpecId = this.getOldPrimaryValue(primary, i, COLUMN_EMBEDDEDSPECID);
            String embeddedSamplingPlanId = this.getOldPrimaryValue(primary, i, COLUMN_EMBEDDEDSAMPLINGPLANID);
            String versionStatus = primary.getValue(i, COLUMN_VERSIONSTATUS, "");
            if (embeddedSpecId.length() > 0) {
                String embeddedSpecVersionId = this.getOldPrimaryValue(primary, i, COLUMN_EMBEDDEDSPECVERSIONID);
                embeddedSpecIdBuffer.append(";").append(embeddedSpecId);
                embeddedSpecVersionIdBuffer.append(";").append(embeddedSpecVersionId);
                embeddedSpecVersionStatusBuffer.append(";").append(versionStatus);
            }
            if (embeddedSamplingPlanId.length() <= 0) continue;
            String embeddedSamplingPlanVersionId = this.getOldPrimaryValue(primary, i, COLUMN_EMBEDDEDSAMPLINGPLANVERSIONID);
            embeddedSamplingPlanIdBuffer.append(";").append(embeddedSamplingPlanId);
            embeddedSamplingPlanVersionIdBuffer.append(";").append(embeddedSamplingPlanVersionId);
            embeddedSamplingPlanVersionStatusBuffer.append(";").append(versionStatus);
        }
        if (embeddedSpecIdBuffer.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "SpecSDC");
            props.setProperty("keyid1", embeddedSpecIdBuffer.substring(1));
            props.setProperty("keyid2", embeddedSpecVersionIdBuffer.substring(1));
            props.setProperty(COLUMN_VERSIONSTATUS, embeddedSpecVersionStatusBuffer.substring(1));
            this.getActionProcessor().processAction("SetSDIVersionStatus", "1", props);
        }
        if (embeddedSamplingPlanIdBuffer.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_SamplingPlan");
            props.setProperty("keyid1", embeddedSamplingPlanIdBuffer.substring(1));
            props.setProperty("keyid2", embeddedSamplingPlanVersionIdBuffer.substring(1));
            props.setProperty(COLUMN_VERSIONSTATUS, embeddedSamplingPlanVersionStatusBuffer.substring(1));
            this.getActionProcessor().processAction("SetSDIVersionStatus", "1", props);
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT pp.protocolid, pp.protocolproductid FROM protocolproduct pp, rsetitems");
        sql.append(" WHERE rsetitems.rsetid = " + safeSQL.addVar(rsetid) + " AND pp.linksdcid = 'Product' AND pp.linkkeyid1 = rsetitems.keyid1 ");
        DataSet protocolproductDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (protocolproductDS != null && protocolproductDS.size() > 0) {
            for (int i = 0; i < protocolproductDS.size(); ++i) {
                safeSQL.reset();
                String sql2 = "SELECT 1 FROM S_PRODUCT  WHERE S_PRODUCTID = " + safeSQL.addVar(protocolproductDS.getValue(i, "protocolproductid"));
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql2, safeSQL.getValues());
                if (ds.getRowCount() != 1) continue;
                this.throwError("ProductUsed", "VALIDATION", "One or more Product(s) are attached to a protocol.");
            }
        }
        this.normalizeEmbeddedSpecAndSamplingPlan(rsetid);
        this.deleteProductIngredients(rsetid);
    }

    private void deleteProductIngredients(String rsetid) throws ActionException {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT ").append(COLUMN_S_PRODUCTID);
        sql.append(", ").append(COLUMN_S_PRODUCTVERSIONID);
        sql.append(", ").append("s_productitemid");
        sql.append(" FROM ").append("s_productformulation").append(", rsetitems");
        sql.append(" WHERE rsetitems.rsetid = ").append(safeSQL.addVar(rsetid));
        sql.append(" AND ").append(COLUMN_S_PRODUCTID).append(" = rsetitems.keyid1");
        sql.append(" AND ").append(COLUMN_S_PRODUCTVERSIONID).append(" = rsetitems.keyid2");
        DataSet productIngredientsDs = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (productIngredientsDs != null && productIngredientsDs.getRowCount() > 0) {
            PropertyList deleteProps = new PropertyList();
            deleteProps.setProperty("keyid1", productIngredientsDs.getColumnValues(COLUMN_S_PRODUCTID, ";"));
            deleteProps.setProperty("keyid2", productIngredientsDs.getColumnValues(COLUMN_S_PRODUCTVERSIONID, ";"));
            deleteProps.setProperty("keyid3", productIngredientsDs.getColumnValues("s_productitemid", ";"));
            deleteProps.setProperty("sdcid", "LV_ProductIngredient");
            this.getActionProcessor().processAction("DeleteSDI", "1", deleteProps);
        }
    }

    private void normalizeEmbeddedSpecAndSamplingPlan(String rsetid) throws ActionException {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT s_productid");
        sql.append(", ").append(COLUMN_EMBEDDEDSPECID);
        sql.append(", ").append(COLUMN_EMBEDDEDSPECVERSIONID);
        sql.append(", ").append(COLUMN_EMBEDDEDSAMPLINGPLANID);
        sql.append(", ").append(COLUMN_EMBEDDEDSAMPLINGPLANVERSIONID);
        sql.append(" FROM s_product, rsetitems");
        sql.append(" WHERE rsetitems.rsetid = " + safeSQL.addVar(rsetid) + " AND s_product.s_productid = rsetitems.keyid1 AND s_product.s_productversionid = rsetitems.keyid2");
        sql.append(" AND ").append(COLUMN_PRODUCTMODEFLAG).append(" = '").append(PRODUCTMODEFLAG_SIMPLE).append("'");
        DataSet productDs = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        StringBuffer embeddedSpecIdBuffer = new StringBuffer();
        StringBuffer embeddedSpecVersionIdBuffer = new StringBuffer();
        StringBuffer embeddedSamplingPlanIdBuffer = new StringBuffer();
        StringBuffer embeddedSamplingPlanVersionIdBuffer = new StringBuffer();
        for (int i = 0; i < productDs.getRowCount(); ++i) {
            String embeddedSamplingPlanId;
            String embeddedSpecId = productDs.getValue(i, COLUMN_EMBEDDEDSPECID, "");
            if (embeddedSpecId.length() > 0) {
                String embeddedSpecVersionId = productDs.getValue(i, COLUMN_EMBEDDEDSPECVERSIONID, "");
                embeddedSpecIdBuffer.append(";").append(embeddedSpecId);
                embeddedSpecVersionIdBuffer.append(";").append(embeddedSpecVersionId);
            }
            if ((embeddedSamplingPlanId = productDs.getValue(i, COLUMN_EMBEDDEDSAMPLINGPLANID, "")).length() <= 0) continue;
            String embeddedSamplingPlanVersionId = productDs.getValue(i, COLUMN_EMBEDDEDSAMPLINGPLANVERSIONID, "");
            embeddedSamplingPlanIdBuffer.append(";").append(embeddedSamplingPlanId);
            embeddedSamplingPlanVersionIdBuffer.append(";").append(embeddedSamplingPlanVersionId);
        }
        if (embeddedSpecIdBuffer.length() > 0) {
            this.setEmbeddedFlag("SpecSDC", embeddedSpecIdBuffer.substring(1), embeddedSpecVersionIdBuffer.substring(1), null, "embeddedflag");
        }
        if (embeddedSamplingPlanIdBuffer.length() > 0) {
            this.setEmbeddedFlag("LV_SamplingPlan", embeddedSamplingPlanIdBuffer.substring(1), embeddedSamplingPlanVersionIdBuffer.substring(1), null, "embeddedflag");
        }
    }

    private void setEmbeddedFlag(String sdcId, String keyId1, String keyId2, String keyId3, String embeddedFlagColumnName) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", sdcId);
        props.setProperty("keyid1", keyId1);
        if (keyId2 != null && keyId2.length() > 0) {
            props.setProperty("keyid2", keyId2);
        }
        if (keyId3 != null && keyId3.length() > 0) {
            props.setProperty("keyid3", keyId3);
        }
        props.setProperty(embeddedFlagColumnName, "N");
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    private void validateProdVarExists(DataSet primary) throws SapphireException {
        DataSet products = new DataSet();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLETYPEID)) continue;
            int row = products.addRow();
            products.setString(row, "productid", primary.getString(i, COLUMN_S_PRODUCTID));
            products.setString(row, "productversionid", primary.getString(i, COLUMN_S_PRODUCTVERSIONID));
        }
        if (products.getRowCount() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String rsetId = this.getDAMProcessor().createRSet(SDCID, products.getColumnValues("productid", ";"), products.getColumnValues("productversionid", ";"), null);
            String prodVarSql = "SELECT COUNT(pv.productid) prodvarcount FROM s_prodvariant pv, rsetitems ri WHERE pv.productid = ri.keyid1 AND pv.productversionid = ri.keyid2 AND ri.rsetid = " + safeSQL.addVar(rsetId);
            DataSet prodVarCountDS = this.getQueryProcessor().getPreparedSqlDataSet(prodVarSql, safeSQL.getValues());
            for (int i = 0; i < prodVarCountDS.getRowCount(); ++i) {
                if (prodVarCountDS.getInt(i, "prodvarcount", 0) <= 0) continue;
                throw new SapphireException("ProdVarExists", "VALIDATION", this.getTranslationProcessor().translate("ProdVariants exists for the selected Product. Cannot change Product Type."));
            }
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    private void cancelFormulationBatch(DataSet primary) throws SapphireException {
        try {
            StringBuffer batchids = new StringBuffer();
            PreparedStatement batchPS = this.database.prepareStatement("batch", "select s_batchid from s_batch where productid=? and productversionid=?");
            for (int i = 0; i < primary.getRowCount(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, COLUMN_FORMULATIONSTATUS) || !primary.getString(i, COLUMN_FORMULATIONSTATUS, "").equalsIgnoreCase("Cancelled")) continue;
                batchPS.setString(1, primary.getString(i, COLUMN_S_PRODUCTID, ""));
                batchPS.setString(2, primary.getString(i, COLUMN_S_PRODUCTVERSIONID, ""));
                DataSet batchDS = new DataSet(batchPS.executeQuery());
                if (batchDS.getRowCount() <= 0) continue;
                batchids.append(";").append(batchDS.getColumnValues("s_batchid", ";"));
            }
            if (batchids.length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "Batch");
                props.setProperty("keyid1", batchids.substring(1));
                props.setProperty("batchstatus", "Cancelled");
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
        }
        catch (Exception e) {
            this.logger.error("Error in cancelling formulation batch:", e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("batch");
        }
    }

    private void unCancelFormulationBatch(DataSet primary) throws SapphireException {
        try {
            DataSet beforeEditImage = this.getBeforeEditImage().getDataset("primary");
            StringBuffer batchids = new StringBuffer();
            PreparedStatement batchPS = this.database.prepareStatement("batch", "select s_batchid from s_batch where productid=? and productversionid=?");
            for (int i = 0; i < primary.getRowCount(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, COLUMN_FORMULATIONSTATUS) || !beforeEditImage.getString(i, COLUMN_FORMULATIONSTATUS, "").equalsIgnoreCase("Cancelled")) continue;
                batchPS.setString(1, primary.getString(i, COLUMN_S_PRODUCTID, ""));
                batchPS.setString(2, primary.getString(i, COLUMN_S_PRODUCTVERSIONID, ""));
                DataSet batchDS = new DataSet(batchPS.executeQuery());
                if (batchDS.getRowCount() <= 0) continue;
                batchids.append(";").append(batchDS.getColumnValues("s_batchid", ";"));
            }
            if (batchids.length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "Batch");
                props.setProperty("keyid1", batchids.substring(1));
                props.setProperty("statuscolumn", "batchstatus");
                props.setProperty("validatestatuscolumnvalue", "Cancelled");
                props.setProperty("defaultvalue", "Active");
                this.getActionProcessor().processAction("UndoSDIColumnValue", "1", props);
            }
        }
        catch (Exception e) {
            this.logger.error("Error in Uncancelling formulation batch:", e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("batch");
        }
    }

    private void callRedoCalcOnProduct(DataSet dsPrimary) throws SapphireException {
        DataSet dsRedoProductCalc = new DataSet();
        DataSet dsRedoStageCalc = new DataSet();
        PreparedStatement checkProduct = this.database.prepareStatement("checkProduct", "select 1 from s_product  where s_productid = ? and s_productversionid = ? and formulationiterationflag = 'Y' and templateflag != 'Y'");
        PreparedStatement getProductStages = this.database.prepareStatement("GetProductStages", "select * from s_productstage where s_productid = ? and s_productversionid = ? and exists (select 1 from sdidata where sdcid = 'LV_ProductStage' AND sdidata.keyid1 = s_productstage.s_productid and sdidata.keyid2 = s_productstage.s_productversionid )");
        try {
            for (int i = 0; i < dsPrimary.getRowCount(); ++i) {
                if (!this.hasPrimaryValueChanged(dsPrimary, i, "cost") && !this.hasPrimaryValueChanged(dsPrimary, i, "expectedbatchsize")) continue;
                String productId = dsPrimary.getString(i, COLUMN_S_PRODUCTID);
                String productVersionId = dsPrimary.getString(i, COLUMN_S_PRODUCTVERSIONID);
                checkProduct.setString(1, productId);
                checkProduct.setString(2, productVersionId);
                ResultSet rs = checkProduct.executeQuery();
                if (!rs.next()) continue;
                getProductStages.setString(1, productId);
                getProductStages.setString(2, productVersionId);
                DataSet dsStages = new DataSet(getProductStages.executeQuery());
                if (dsStages.getRowCount() > 0) {
                    dsRedoStageCalc.copyRow(dsStages, -1, 1);
                    continue;
                }
                dsRedoProductCalc.copyRow(dsPrimary, i, 1);
            }
            PropertyList props = new PropertyList();
            if (dsRedoStageCalc.getRowCount() > 0) {
                props.setProperty("sdcid", "LV_ProductStage");
                props.setProperty("keyid1", dsRedoStageCalc.getColumnValues(COLUMN_S_PRODUCTID, ";"));
                props.setProperty("keyid2", dsRedoStageCalc.getColumnValues(COLUMN_S_PRODUCTVERSIONID, ";"));
                props.setProperty("keyid3", dsRedoStageCalc.getColumnValues("s_productstageid", ";"));
                this.getActionProcessor().processAction("RedoCalculations", "1", props);
            }
            if (dsRedoProductCalc.getRowCount() > 0) {
                props.clear();
                props.setProperty("sdcid", SDCID);
                props.setProperty("keyid1", dsRedoProductCalc.getColumnValues(COLUMN_S_PRODUCTID, ";"));
                props.setProperty("keyid2", dsRedoProductCalc.getColumnValues(COLUMN_S_PRODUCTVERSIONID, ";"));
                this.getActionProcessor().processAction("RedoCalculations", "1", props);
            }
        }
        catch (Exception e) {
            this.logger.error("Error in executing callRedoCalcOnProduct method:", e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("checkProduct");
            this.database.closeStatement("GetProductStages");
        }
    }

    private void checkSiteLabWorkAreaHierarchy(DataSet primary) throws SapphireException {
        for (int indx = 0; indx < primary.size(); ++indx) {
            DataSet ds;
            String siteid = this.getColumnValue(primary, indx, "sitedepartmentid");
            String labid = this.getColumnValue(primary, indx, "testingdepartmentid");
            String workarea = this.getColumnValue(primary, indx, "workareadepartmentid");
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            String exceptionMSG = "";
            if ((this.hasPrimaryValueChanged(primary, indx, "sitedepartmentid") || this.hasPrimaryValueChanged(primary, indx, "testingdepartmentid")) && siteid.length() > 0 && labid.length() > 0 && !labid.equals(siteid)) {
                sql.append("select departmentid from department where departmentid = ").append(safeSQL.addVar(labid)).append(" and parentdepartmentid=").append(safeSQL.addVar(siteid));
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds == null || ds.size() == 0) {
                    exceptionMSG = "Testing Lab (" + labid + ") is not in the Site (" + siteid + ").";
                }
            }
            if ((this.hasPrimaryValueChanged(primary, indx, "workareadepartmentid") || this.hasPrimaryValueChanged(primary, indx, "testingdepartmentid")) && workarea.length() > 0 && labid.length() > 0 && !labid.equals(workarea)) {
                safeSQL.reset();
                sql.delete(0, sql.length());
                sql.append("select departmentid from department where departmentid = ").append(safeSQL.addVar(workarea)).append(" and parentdepartmentid=").append(safeSQL.addVar(labid));
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds == null || ds.size() == 0) {
                    exceptionMSG = (exceptionMSG.length() > 0 ? exceptionMSG + " And " : "") + "Work Area (" + workarea + ") is not in the Testing Lab (" + labid + ").";
                }
            }
            if (exceptionMSG.length() <= 0) continue;
            throw new SapphireException(this.getTranslationProcessor().translate(exceptionMSG));
        }
    }

    private String getColumnValue(DataSet primary, int indx, String columnid) {
        String value = "";
        value = primary.isValidColumn(columnid) ? primary.getString(indx, columnid, "") : this.getOldPrimaryValue(primary, indx, columnid);
        return value;
    }
}

