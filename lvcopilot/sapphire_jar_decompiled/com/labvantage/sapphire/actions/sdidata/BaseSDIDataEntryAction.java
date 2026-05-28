/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.opal.qcbatch.QCBatch;
import com.labvantage.opal.qcbatch.QCBatchItem;
import com.labvantage.opal.qcbatch.QCBatchPool;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.actions.sdidata.DataEntryCalcUtil;
import com.labvantage.sapphire.actions.sdidata.DataEntryLimitsUtil;
import com.labvantage.sapphire.actions.sdidata.VirtualLimit;
import com.labvantage.sapphire.services.DataAccessService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.services.WebAdminService;
import com.labvantage.sapphire.util.StringHolder;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.evaluator.ExpressionUtil;
import com.labvantage.sapphire.util.format.DateFormatter;
import com.labvantage.sapphire.util.format.NumericFormatter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class BaseSDIDataEntryAction
extends BaseSDIDataAction
implements CacheNames {
    static final String COL_CALCKEYSET = "_calckeyset";
    static final String COL_APPARENTDATATYPE = "_apparentdatatype";
    static final String ARRAY_SDCID = "LV_Array";
    static final String REDOCALC_SDIS = "REDOCALC_SDIS";
    public static final String DATAENTRYPAGE_ALLSDIS = "dataentrypage_allsdis";
    public static final String LIVEDATAENTRY_MODIFIEDDATAITEMS = "livedataentry_modifieddataitems";
    public static final String CROSSSDI_ALL_MODIFIEDDATAITEMS = "crosssdi_all_modifieddataitems";
    boolean includeCancelledDatasets;
    DataSet previouslyCalledRedoCalcSDIS = new DataSet();
    private boolean isLiveLimitChecking = false;
    private String allSdisOnDataEntryPage = null;
    private DataSet crossSDIAllModifiedSDIDataitems = null;
    private String liveDataEntryCrossSdiCheckingMode;
    public static final String LIVELIMITCHECK_CROSSSDI_MODE = "livelimitcheckmode";
    public static final String LIVELIMITCHECK_CROSSSDI_MODE_DEEP = "deep";
    public static final String LIVELIMITCHECK_CROSSSDI_MODE_SHALLOW = "shallow";

    protected void dataEntry(PropertyList properties, boolean specsOnly, boolean limitsOnly) throws SapphireException {
        this.dataEntry(properties, specsOnly, limitsOnly, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void dataEntry(PropertyList properties, boolean specsOnly, boolean limitsOnly, boolean calcsOnly) throws SapphireException {
        boolean hasDatacaptureId;
        boolean hasInstrumentId;
        boolean hasCalcExcludeChange;
        if (!(calcsOnly || "Y".equals(properties.getProperty("calcreportonly")) || limitsOnly || specsOnly || CacheUtil.get(this.connectionInfo.getDatabaseId(), "isSpecConditionInSync", "isSpecConditionInSync") != null || WebAdminService.isSpecConditionInSync(this.connectionInfo.getConnectionId(), this.database))) {
            throw new SapphireException("GENERAL_ERROR", "Spec Condition Reference Type is out of synchronization with Data Entry Policy Spec Conditions.     \n\nYou must go to Data Entry Policy to confirm that Spec Conditions are correctly defined by clicking Save.     \nThe system will synchronize Data Entry Spec Conditions to Spec Condition Reference Type when saving Data Entry Policy.");
        }
        this.includeCancelledDatasets = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("excludecancelleddataset", "Y").equals("N");
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.indexOf(";") > 0) {
            sdcid = sdcid.substring(0, sdcid.indexOf(";") - 1);
        }
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        sdcid = sdcProcessor.getProperty(sdcid, "sdcid");
        boolean calcReportOnly = properties.getProperty("calcreportonly").equals("Y");
        this.previouslyCalledRedoCalcSDIS = new DataSet(properties.getProperty(REDOCALC_SDIS));
        DataSet propds = new DataSet();
        propds.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), ";");
        propds.addColumnValues("keyid2", 0, properties.getProperty("keyid2"), ";", "(null)");
        propds.addColumnValues("keyid3", 0, properties.getProperty("keyid3"), ";", "(null)");
        propds.addColumnValues("paramlistid", 0, properties.getProperty("paramlistid"), ";");
        propds.addColumnValues("paramlistversionid", 0, properties.getProperty("paramlistversionid"), ";");
        propds.addColumnValues("variantid", 0, properties.getProperty("variantid"), ";");
        propds.addColumnValues("dataset", 0, properties.getProperty("dataset"), ";");
        propds.addColumnValues("paramid", 0, properties.getProperty("paramid"), ";");
        propds.addColumnValues("paramtype", 0, properties.getProperty("paramtype"), ";");
        propds.addColumnValues("replicateid", 0, properties.getProperty("replicateid"), ";");
        propds.padColumns();
        propds.addColumnValues("enteredtext", 0, properties.getProperty("enteredtext"), ";");
        this.isLiveLimitChecking = "Y".equalsIgnoreCase(properties.getProperty("islivelimitchecking"));
        this.allSdisOnDataEntryPage = (String)properties.get(DATAENTRYPAGE_ALLSDIS);
        this.crossSDIAllModifiedSDIDataitems = (DataSet)properties.get(CROSSSDI_ALL_MODIFIEDDATAITEMS);
        if (this.crossSDIAllModifiedSDIDataitems == null) {
            this.crossSDIAllModifiedSDIDataitems = new DataSet();
        }
        this.liveDataEntryCrossSdiCheckingMode = properties.getProperty(LIVELIMITCHECK_CROSSSDI_MODE);
        if (OpalUtil.isEmpty(this.liveDataEntryCrossSdiCheckingMode)) {
            PropertyList policy = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom");
            this.liveDataEntryCrossSdiCheckingMode = policy.getProperty(LIVELIMITCHECK_CROSSSDI_MODE, LIVELIMITCHECK_CROSSSDI_MODE_DEEP);
        }
        boolean noOverrideReleased = "N".equalsIgnoreCase(properties.getProperty("overridereleased"));
        boolean isLiveLimitChecking = "Y".equalsIgnoreCase(properties.getProperty("islivelimitchecking"));
        boolean isGWTGridSaving = properties.getProperty("autorelease").length() > 0;
        String calcExcludeFlags = properties.getProperty("calcexcludeflag");
        boolean calculateModifiedDatasetsOnly = properties.getProperty("calculatemodifieddatasetsonly").equals("Y");
        boolean calculateModifiedTestsOnly = properties.getProperty("calculatemodifiedtestsonly").equals("Y");
        boolean bl = hasCalcExcludeChange = !(calculateModifiedDatasetsOnly || calculateModifiedTestsOnly || !isLiveLimitChecking && !isGWTGridSaving || !calcExcludeFlags.toUpperCase().contains("Y") && !calcExcludeFlags.toUpperCase().contains("N"));
        if (hasCalcExcludeChange) {
            propds.addColumnValues("calcexcludeflag", 0, properties.getProperty("calcexcludeflag", "N"), ";");
        }
        boolean bl2 = hasInstrumentId = properties.getProperty("instrumentid").length() > 0;
        if (hasInstrumentId) {
            propds.addColumnValues("instrumentid", 0, properties.getProperty("instrumentid"), ";");
        }
        boolean bl3 = hasDatacaptureId = properties.getProperty("datacaptureid").length() > 0;
        if (hasDatacaptureId) {
            propds.addColumnValues("datacaptureid", 0, properties.getProperty("datacaptureid"), ";");
        }
        String arrayId = properties.getProperty("arrayid");
        String tableid = sdcProcessor.getProperty(sdcid, "tableid");
        String keyColId = sdcProcessor.getProperty(sdcid, "keycolid1");
        if (("LV_ArrayItem".equals(sdcid) || "LV_ArrayZone".equals(sdcid)) && arrayId.length() == 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT arrayid FROM " + tableid + " WHERE " + keyColId + " in ( " + safeSQL.addIn(propds.getColumnValues("keyid1", "','")) + ")";
            DataSet dsArrays = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            arrayId = dsArrays.getColumnValues("arrayid", ";");
            if (arrayId.length() == 0) {
                throw new SapphireException("INVALID_PROPERTIES", "Array Id cannot be determined from the " + keyColId + ":" + propds.getColumnValues("keyid1", ";"));
            }
        }
        StringBuilder keyid1 = new StringBuilder();
        StringBuilder keyid2 = new StringBuilder();
        StringBuilder keyid3 = new StringBuilder();
        StringBuilder paramlistid = new StringBuilder();
        StringBuilder paramlistversionid = new StringBuilder();
        StringBuilder variantid = new StringBuilder();
        StringBuilder dataset = new StringBuilder();
        int proprows = propds.getRowCount();
        for (int i = 0; i < proprows; ++i) {
            keyid1.append(i > 0 ? ";" : "").append(propds.getString(i, "keyid1"));
            keyid2.append(i > 0 ? ";" : "").append(propds.getString(i, "keyid2"));
            keyid3.append(i > 0 ? ";" : "").append(propds.getString(i, "keyid3"));
            paramlistid.append(i > 0 ? ";" : "").append(propds.getString(i, "paramlistid"));
            paramlistversionid.append(i > 0 ? ";" : "").append(propds.getString(i, "paramlistversionid"));
            variantid.append(i > 0 ? ";" : "").append(propds.getString(i, "variantid"));
            dataset.append(i > 0 ? ";" : "").append(propds.getString(i, "dataset"));
        }
        boolean applylock = properties.getProperty("applylock").equals("Y");
        if (isLiveLimitChecking) {
            applylock = false;
        }
        String rsetid = null;
        String extraRsetid = null;
        String arrayPrimaryRsetId = null;
        String formulationProjectid = null;
        String formulationProductVersionid = null;
        try {
            boolean dataSetIndexing;
            DataSet sdispec;
            DataSet sdidataitemspec;
            DataSet sdidataitemlimits;
            DataSet sdidataitems;
            DataSet sdidata;
            DataSet primary;
            boolean isExtraCalc;
            boolean isArrayCalc = arrayId.length() > 0;
            boolean isFormulationsCalc = sdcid.equals("LV_ProductIngredient");
            boolean isPromoteOperation = properties.getProperty("ispromotoperation", "N").equalsIgnoreCase("Y");
            String extraSdcid = properties.getProperty("extrasdcid");
            String extraKeyid1 = properties.getProperty("extrakeyid1");
            String extraKeyid2 = properties.getProperty("extrakeyid2", "(null)");
            String extraKeyid3 = properties.getProperty("extrakeyid3", "(null)");
            boolean bl4 = isExtraCalc = extraSdcid.length() > 0 && extraKeyid1.length() > 0;
            if (isArrayCalc) {
                rsetid = this.getRSet(ARRAY_SDCID, arrayId, "", "", applylock);
            } else if (isFormulationsCalc) {
                String formulationProductId = propds.getString(0, "keyid1");
                formulationProductVersionid = propds.getString(0, "keyid2");
                for (int i = 0; i < propds.getRowCount(); ++i) {
                    if (formulationProductVersionid.equals(propds.getValue(i, "keyid2"))) continue;
                    throw new SapphireException("INVALID_PROPERTIES", "Unable to enter ingredient amounts for multiple product versions.");
                }
                this.database.createPreparedResultSet("findprojectid", "SELECT formulationprojectid FROM s_product WHERE s_productid=? AND s_productversionid=?", new String[]{formulationProductId, formulationProductVersionid});
                if (this.database.getNext("findprojectid")) {
                    formulationProjectid = this.database.getString("findprojectid", "formulationprojectid");
                }
                rsetid = BaseSDIDataAction.createRSet("LV_FormulationProject", formulationProjectid, "", "", this.database, this.connectionInfo, applylock);
            } else {
                rsetid = isLiveLimitChecking || specsOnly || calcsOnly || calcReportOnly ? (calculateModifiedDatasetsOnly || calculateModifiedTestsOnly ? this.getDSRSet(sdcid, keyid1.toString(), keyid2.toString(), keyid3.toString(), paramlistid.toString(), paramlistversionid.toString(), variantid.toString(), dataset.toString(), applylock) : BaseSDIDataAction.createBypassSecurityRSet(sdcid, keyid1.toString(), keyid2.toString(), keyid3.toString(), this.database, this.connectionInfo, applylock)) : this.getDSRSet(sdcid, keyid1.toString(), keyid2.toString(), keyid3.toString(), paramlistid.toString(), paramlistversionid.toString(), variantid.toString(), dataset.toString(), applylock);
            }
            DataSet sdidataTrackedEquipment = new DataSet();
            String crosscalcarraydataitems = null;
            SafeSQL crosscalcarraydataitemsSafeSQL = new SafeSQL();
            if (isArrayCalc) {
                String selectdi;
                this.database.createPreparedResultSet("arrayprimary", "SELECT t." + keyColId + " FROM " + tableid + " t, rsetitems r  WHERE t.arrayid = r.keyid1 AND r.sdcid = ? AND r.rsetid = ?", new Object[]{ARRAY_SDCID, rsetid});
                DataSet arrayPrimary = new DataSet(this.database.getResultSet("arrayprimary"));
                this.database.closeResultSet("arrayprimary");
                arrayPrimaryRsetId = this.getRSet(sdcid, arrayPrimary.getColumnValues(keyColId, ";"), "", "", false);
                String crossArrayCalcSdcId = "LV_ArrayItem".equals(sdcid) ? "LV_ArrayZone" : "LV_ArrayItem";
                String crossCalcTableId = sdcProcessor.getProperty(crossArrayCalcSdcId, "tableid");
                String crossCalcKeyColId1 = sdcProcessor.getProperty(crossArrayCalcSdcId, "keycolid1");
                SafeSQL selectdsSafeSQL = new SafeSQL();
                String selectds = "SELECT\tsdidata.* FROM\tsdidata, rsetitems WHERE sdidata.sdcid = rsetitems.sdcid AND sdidata.keyid1 = rsetitems.keyid1 AND sdidata.keyid2 = rsetitems.keyid2 AND sdidata.keyid3 = rsetitems.keyid3 AND rsetid = " + selectdsSafeSQL.addVar(arrayPrimaryRsetId) + " ";
                selectds = selectds + "UNION SELECT sdidata.* FROM sdidata, " + crossCalcTableId + " t WHERE sdidata.sdcid = " + selectdsSafeSQL.addVar(crossArrayCalcSdcId) + " AND sdidata.keyid1 = t." + crossCalcKeyColId1 + " AND t.arrayid in( " + selectdsSafeSQL.addIn(arrayId, ";") + ")";
                SafeSQL selectdiSafeSQL = new SafeSQL();
                if ("LV_ArrayItem".equals(sdcid)) {
                    selectdi = "SELECT sdidataitem.*, ds.arraymethodid, ds.arraymethodversionid, ai.verticallabel, ai.horizontallabel, ai.arrayid  FROM sdidataitem, sdidata ds, rsetitems, arrayitem ai  WHERE ds.sdcid = sdidataitem.sdcid AND ds.keyid1 = sdidataitem.keyid1 AND ds.keyid2 = sdidataitem.keyid2 AND ds.keyid3 = sdidataitem.keyid3  AND ds.paramlistid = sdidataitem.paramlistid AND ds.paramlistversionid = sdidataitem.paramlistversionid AND ds.variantid = sdidataitem.variantid AND ds.dataset = sdidataitem.dataset  AND ai.arrayitemid = sdidataitem.keyid1  AND sdidataitem.sdcid = rsetitems.sdcid AND sdidataitem.keyid1 = rsetitems.keyid1  AND sdidataitem.keyid2 = rsetitems.keyid2 AND sdidataitem.keyid3 = rsetitems.keyid3 AND rsetid = " + selectdiSafeSQL.addVar(arrayPrimaryRsetId) + " ORDER BY sdidataitem.keyid1";
                    crosscalcarraydataitems = "SELECT sdidataitem.*,ds.arraymethodid, ds.arraymethodversionid, az.zone, az.arrayid  FROM sdidataitem, sdidata ds, arrayzone az, " + crossCalcTableId + " t  WHERE sdidataitem.sdcid = " + crosscalcarraydataitemsSafeSQL.addVar(crossArrayCalcSdcId) + " AND sdidataitem.keyid1 = t." + crossCalcKeyColId1 + " AND t.arrayid in( " + crosscalcarraydataitemsSafeSQL.addIn(arrayId, ";") + ") AND ds.sdcid = sdidataitem.sdcid AND ds.keyid1 = sdidataitem.keyid1 AND ds.keyid2 = sdidataitem.keyid2 AND ds.keyid3 = sdidataitem.keyid3  AND ds.paramlistid = sdidataitem.paramlistid AND ds.paramlistversionid = sdidataitem.paramlistversionid AND ds.variantid = sdidataitem.variantid AND ds.dataset = sdidataitem.dataset  AND az.arrayzoneid = sdidataitem.keyid1 ORDER BY sdidataitem.keyid1";
                } else {
                    selectdi = "SELECT sdidataitem.*, ds.arraymethodid, ds.arraymethodversionid, az.zone, az.arrayid FROM sdidataitem, sdidata ds, arrayzone az, rsetitems  WHERE ds.sdcid = sdidataitem.sdcid AND ds.keyid1 = sdidataitem.keyid1 AND ds.keyid2 = sdidataitem.keyid2 AND ds.keyid3 = sdidataitem.keyid3  AND ds.paramlistid = sdidataitem.paramlistid AND ds.paramlistversionid = sdidataitem.paramlistversionid AND ds.variantid = sdidataitem.variantid AND ds.dataset = sdidataitem.dataset  AND sdidataitem.sdcid = rsetitems.sdcid AND sdidataitem.keyid1 = rsetitems.keyid1  AND sdidataitem.keyid2 = rsetitems.keyid2 AND sdidataitem.keyid3 = rsetitems.keyid3 AND rsetid = " + selectdiSafeSQL.addVar(arrayPrimaryRsetId) + " AND az.arrayzoneid = sdidataitem.keyid1 ORDER BY sdidataitem.keyid1";
                    crosscalcarraydataitems = "SELECT sdidataitem.*,ds.arraymethodid, ds.arraymethodversionid, ai.verticallabel, ai.horizontallabel, ai.arrayid FROM sdidataitem, sdidata ds, arrayitem ai, " + crossCalcTableId + " t  WHERE sdidataitem.sdcid = " + crosscalcarraydataitemsSafeSQL.addVar(crossArrayCalcSdcId) + " AND sdidataitem.keyid1 = t." + crossCalcKeyColId1 + " AND t.arrayid in( " + crosscalcarraydataitemsSafeSQL.addIn(arrayId, ";") + " ) AND ds.sdcid = sdidataitem.sdcid AND ds.keyid1 = sdidataitem.keyid1 AND ds.keyid2 = sdidataitem.keyid2 AND ds.keyid3 = sdidataitem.keyid3  AND ds.paramlistid = sdidataitem.paramlistid AND ds.paramlistversionid = sdidataitem.paramlistversionid AND ds.variantid = sdidataitem.variantid AND ds.dataset = sdidataitem.dataset  AND ai.arrayitemid = sdidataitem.keyid1 ORDER BY sdidataitem.keyid1";
                }
                SafeSQL selectdlSafeSQL = new SafeSQL();
                String selectdl = "SELECT\tsdidataitemlimits.* FROM sdidataitemlimits, rsetitems WHERE\tsdidataitemlimits.sdcid = rsetitems.sdcid AND sdidataitemlimits.keyid1 = rsetitems.keyid1  AND sdidataitemlimits.keyid2 = rsetitems.keyid2 AND sdidataitemlimits.keyid3 = rsetitems.keyid3 AND rsetid = " + selectdlSafeSQL.addVar(arrayPrimaryRsetId) + " ";
                selectdl = selectdl + "UNION SELECT sdidataitemlimits.* FROM sdidataitemlimits, " + crossCalcTableId + " t WHERE sdidataitemlimits.sdcid = " + selectdlSafeSQL.addVar(crossArrayCalcSdcId) + " AND sdidataitemlimits.keyid1 = t." + crossCalcKeyColId1 + " AND t.arrayid in ( " + selectdlSafeSQL.addIn(arrayId, ";") + " )";
                SafeSQL selectdisSafeSQL = new SafeSQL();
                String selectdis = "SELECT\tsdidataitemspec.* FROM\tsdidataitemspec, rsetitems WHERE sdidataitemspec.sdcid = rsetitems.sdcid  AND sdidataitemspec.keyid1 = rsetitems.keyid1 AND sdidataitemspec.keyid2 = rsetitems.keyid2 AND sdidataitemspec.keyid3 = rsetitems.keyid3  AND rsetid = " + selectdisSafeSQL.addVar(arrayPrimaryRsetId) + " ";
                selectdis = selectdis + "UNION SELECT sdidataitemspec.* FROM sdidataitemspec, " + crossCalcTableId + " t WHERE sdidataitemspec.sdcid = " + selectdisSafeSQL.addVar(crossArrayCalcSdcId) + " AND sdidataitemspec.keyid1 = t." + crossCalcKeyColId1 + " AND t.arrayid in (" + selectdisSafeSQL.addIn(arrayId, ";") + " )";
                SafeSQL selectsSafeSQL = new SafeSQL();
                String selects = "SELECT sdispec.* FROM sdispec, rsetitems WHERE sdispec.sdcid = rsetitems.sdcid AND sdispec.keyid1 = rsetitems.keyid1   AND sdispec.keyid2 = rsetitems.keyid2 AND sdispec.keyid3 = rsetitems.keyid3 AND \trsetid = " + selectsSafeSQL.addVar(arrayPrimaryRsetId) + " ";
                selects = selects + "UNION SELECT sdispec.* FROM sdispec, " + crossCalcTableId + " t WHERE sdispec.sdcid = " + selectsSafeSQL.addVar(crossArrayCalcSdcId) + " AND sdispec.keyid1 = t." + crossCalcKeyColId1 + " AND t.arrayid in( " + selectsSafeSQL.addIn(arrayId, ";") + " )";
                this.database.createPreparedResultSet("sdidata", selectds, selectdsSafeSQL.getValues());
                this.database.createPreparedResultSet("sdidataitems", selectdi, selectdiSafeSQL.getValues());
                this.database.createPreparedResultSet("sdidataitemlimits", selectdl, selectdlSafeSQL.getValues());
                this.database.createPreparedResultSet("sdidataitemspec", selectdis, selectdisSafeSQL.getValues());
                this.database.createPreparedResultSet("sdispec", selects, selectsSafeSQL.getValues());
                primary = this.loadPrimary(sdcid, arrayPrimaryRsetId);
                sdidata = new DataSet(this.database.getResultSet("sdidata"));
                sdidataitems = new DataSet(this.database.getResultSet("sdidataitems"));
                sdidataitemlimits = new DataSet(this.database.getResultSet("sdidataitemlimits"));
                sdidataitemspec = new DataSet(this.database.getResultSet("sdidataitemspec"));
                sdispec = new DataSet(this.database.getResultSet("sdispec"));
                if (!isLiveLimitChecking) {
                    SafeSQL selectdrSafeSQL = new SafeSQL();
                    String selectdr = "SELECT dr.sdcid,dr.keyid1,dr.keyid2,dr.keyid3,dr.paramlistid,dr.paramlistversionid,dr.variantid,dr.dataset,dr.tokeyid1 FROM sdidatarelation dr,instrument inst, rsetitems WHERE dr.sdcid = rsetitems.sdcid AND dr.keyid1 = rsetitems.keyid1 AND dr.keyid2 = rsetitems.keyid2 AND dr.keyid3 = rsetitems.keyid3 AND dr.relationfunction='Instrument' AND inst.instrumentid=dr.tokeyid1 AND inst.usagetype='DataSet DataEntry' AND rsetid = " + selectdrSafeSQL.addVar(arrayPrimaryRsetId) + " ";
                    selectdr = selectdr + "UNION SELECT dr.sdcid,dr.keyid1,dr.keyid2,dr.keyid3,dr.paramlistid,dr.paramlistversionid,dr.variantid,dr.dataset,dr.tokeyid1 FROM sdidatarelation dr,instrument inst," + crossCalcTableId + " t WHERE dr.sdcid = " + selectdrSafeSQL.addVar(crossArrayCalcSdcId) + " AND dr.keyid1 = t." + crossCalcKeyColId1 + " AND dr.relationfunction='Instrument' AND inst.instrumentid=dr.tokeyid1 AND inst.usagetype='DataSet DataEntry' AND t.arrayid in( " + selectdrSafeSQL.addIn(arrayId, ";") + ")";
                    this.database.createPreparedResultSet("sdidatarelation", selectdr, selectdrSafeSQL.getValues());
                    sdidataTrackedEquipment = new DataSet(this.database.getResultSet("sdidatarelation"));
                    this.database.closeResultSet("sdidatarelation");
                }
            } else if (isFormulationsCalc) {
                HashSet<String> modifiedFormulations = new HashSet<String>();
                StringBuilder formulationIn = new StringBuilder();
                for (int i = 0; i < propds.size(); ++i) {
                    String formulationid = propds.getString(i, "keyid1");
                    modifiedFormulations.add(formulationid);
                }
                for (String formulationId : modifiedFormulations) {
                    formulationIn.append(formulationIn.length() > 0 ? "," : "").append("'").append(formulationId).append("'");
                }
                boolean isDataEnteredbaseOrParentFormulation = false;
                StringBuilder sql = new StringBuilder();
                sql.append("select 1 from sdirelation where tosdcid = 'Product' and tokeyid1 = ? and relationtype = 'Comparator'");
                sql.append(" UNION ");
                sql.append("select 1 from s_product where parentproductid = ?");
                for (String formulationId : modifiedFormulations) {
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{formulationId, formulationId});
                    if (ds.getRowCount() <= 0) continue;
                    isDataEnteredbaseOrParentFormulation = true;
                    break;
                }
                SafeSQL safeSQL = new SafeSQL();
                String selectIngredientPrimary = "SELECT * FROM s_productformulation WHERE s_productid in ( " + safeSQL.addIn(formulationIn.toString()) + " ) AND s_productversionid = " + safeSQL.addVar(formulationProductVersionid);
                this.database.createPreparedResultSet("primary", selectIngredientPrimary, safeSQL.getValues());
                primary = new DataSet(this.database.getResultSet("primary"));
                this.database.closeResultSet("primary");
                primary.setString(-1, "sdcid", "LV_ProductIngredient");
                safeSQL.reset();
                String selectFormulationPrimary = "SELECT * FROM s_product WHERE ";
                selectFormulationPrimary = selectFormulationPrimary + (isDataEnteredbaseOrParentFormulation ? "" : " s_productid in ( " + safeSQL.addIn(formulationIn.toString()) + " ) AND");
                selectFormulationPrimary = selectFormulationPrimary + " formulationiterationflag = " + (isPromoteOperation ? "'N'" : "'Y'") + " AND formulationprojectid = " + safeSQL.addVar(formulationProjectid) + " AND ( templateflag is null OR templateflag != 'Y' ) ";
                this.database.createPreparedResultSet("primary", selectFormulationPrimary, safeSQL.getValues());
                DataSet formulationsPrimary = new DataSet(this.database.getResultSet("primary"));
                this.database.closeResultSet("primary");
                formulationsPrimary.setString(-1, "sdcid", "Product");
                primary.copyRow(formulationsPrimary, -1, 1);
                safeSQL.reset();
                String dsIngredients = "SELECT\tsdidata.*, paramlist.displayunderparamlistname displayunderparamlistname, paramlist.displayundervariantname displayundervariantname FROM sdidata, paramlist, s_productformulation WHERE sdidata.paramlistid=paramlist.paramlistid AND sdidata.paramlistversionid=paramlist.paramlistversionid AND sdidata.variantid=paramlist.variantid  AND sdidata.sdcid = 'LV_ProductIngredient' AND sdidata.keyid1 = s_productformulation.s_productid AND sdidata.keyid2 = s_productformulation.s_productversionid AND sdidata.keyid3 = s_productformulation.s_productitemid  AND s_productformulation.s_productid in ( " + safeSQL.addIn(formulationIn.toString()) + ") AND s_productformulation.s_productversionid = " + safeSQL.addVar(formulationProductVersionid) + " ";
                String dsIterations = "SELECT\tsdidata.*, paramlist.displayunderparamlistname displayunderparamlistname, paramlist.displayundervariantname displayundervariantname FROM sdidata, paramlist, s_product WHERE sdidata.paramlistid=paramlist.paramlistid AND sdidata.paramlistversionid=paramlist.paramlistversionid AND sdidata.variantid=paramlist.variantid  AND sdidata.sdcid = 'Product' AND sdidata.keyid1 = s_product.s_productid AND sdidata.keyid2 = s_product.s_productversionid AND sdidata.keyid3 = '(null)'  AND s_product.formulationiterationflag=" + (isPromoteOperation ? "'N'" : "'Y'") + " AND s_product.formulationprojectid = " + safeSQL.addVar(formulationProjectid);
                String selectds = dsIngredients + " UNION " + dsIterations;
                this.database.createPreparedResultSet("sdidata", selectds, safeSQL.getValues());
                sdidata = new DataSet(this.database.getResultSet("sdidata"));
                this.database.closeResultSet("sdidata");
                safeSQL.reset();
                String diIngredients = "SELECT sdidataitem.*, paramlistitem.displayunderparamname displayunderparamname, paramlistitem.displayunderparamtype displayunderparamtype FROM sdidataitem left outer join paramlistitem on sdidataitem.paramlistid=paramlistitem.paramlistid AND sdidataitem.paramlistversionid=paramlistitem.paramlistversionid AND sdidataitem.variantid=paramlistitem.variantid AND sdidataitem.paramid=paramlistitem.paramid AND sdidataitem.paramtype=paramlistitem.paramtype,  s_productformulation WHERE sdidataitem.sdcid = 'LV_ProductIngredient' AND sdidataitem.keyid1 = s_productformulation.s_productid AND sdidataitem.keyid2 = s_productformulation.s_productversionid AND sdidataitem.keyid3 = s_productformulation.s_productitemid  AND s_productformulation.s_productid in (" + safeSQL.addIn(formulationIn.toString()) + ") AND s_productformulation.s_productversionid = " + safeSQL.addVar(formulationProductVersionid) + " ";
                String diIterations = "SELECT sdidataitem.*, paramlistitem.displayunderparamname displayunderparamname, paramlistitem.displayunderparamtype displayunderparamtype FROM sdidataitem left outer join paramlistitem on sdidataitem.paramlistid=paramlistitem.paramlistid AND sdidataitem.paramlistversionid=paramlistitem.paramlistversionid AND sdidataitem.variantid=paramlistitem.variantid AND sdidataitem.paramid=paramlistitem.paramid AND sdidataitem.paramtype=paramlistitem.paramtype,  s_product WHERE sdidataitem.sdcid = 'Product' AND sdidataitem.keyid1 = s_product.s_productid AND sdidataitem.keyid2 = s_product.s_productversionid AND sdidataitem.keyid3 = '(null)'  AND s_product.formulationiterationflag=" + (isPromoteOperation ? "'N'" : "'Y'") + " AND s_product.formulationprojectid=" + safeSQL.addVar(formulationProjectid);
                if (!isDataEnteredbaseOrParentFormulation) {
                    diIterations = diIterations + "  AND  sdidataitem.keyid1 in(" + safeSQL.addIn(formulationIn.toString()) + ") ";
                }
                String selectdi = diIngredients + " UNION " + diIterations;
                this.database.createPreparedResultSet("sdidataitem", selectdi, safeSQL.getValues());
                sdidataitems = new DataSet(this.database.getResultSet("sdidataitem"));
                this.database.closeResultSet("sdidataitem");
                for (int i = 0; i < sdidataitems.size(); ++i) {
                    String formulationid = sdidataitems.getString(i, "keyid1");
                    if (modifiedFormulations.contains(formulationid) || sdidataitems.getString(i, "paramtype").endsWith("Comparator")) continue;
                    sdidataitems.setString(i, "_hascalc", "N");
                }
                sdidataitemlimits = new DataSet();
                sdidataitemspec = new DataSet();
                sdispec = new DataSet();
            } else {
                String selectdis;
                String selectdl;
                String selectdi;
                String selectds;
                Object[] vars;
                String where;
                if (isExtraCalc) {
                    extraRsetid = BaseSDIDataAction.createRSet(extraSdcid, extraKeyid1, extraKeyid2, extraKeyid3, this.database, this.connectionInfo, applylock);
                    where = " AND ( rsetid = ? or rsetid=? )";
                    vars = new String[]{rsetid, extraRsetid};
                } else {
                    where = " AND rsetid = ?";
                    vars = new String[]{rsetid};
                }
                if (this.includeCancelledDatasets) {
                    selectds = "SELECT\tsdidata.*, paramlist.displayunderparamlistname displayunderparamlistname, paramlist.displayundervariantname displayundervariantname FROM sdidata, paramlist, rsetitems WHERE sdidata.paramlistid=paramlist.paramlistid AND sdidata.paramlistversionid=paramlist.paramlistversionid AND sdidata.variantid=paramlist.variantid AND sdidata.sdcid = rsetitems.sdcid AND sdidata.keyid1 = rsetitems.keyid1 AND sdidata.keyid2 = rsetitems.keyid2 AND sdidata.keyid3 = rsetitems.keyid3 " + where;
                    selectdi = "SELECT\tsdidataitem.*, paramlistitem.displayunderparamname displayunderparamname, paramlistitem.displayunderparamtype displayunderparamtype FROM sdidataitem left outer join paramlistitem on sdidataitem.paramlistid=paramlistitem.paramlistid AND sdidataitem.paramlistversionid=paramlistitem.paramlistversionid AND sdidataitem.variantid=paramlistitem.variantid AND sdidataitem.paramid=paramlistitem.paramid AND sdidataitem.paramtype=paramlistitem.paramtype, rsetitems WHERE sdidataitem.sdcid = rsetitems.sdcid AND sdidataitem.keyid1 = rsetitems.keyid1 AND sdidataitem.keyid2 = rsetitems.keyid2 AND sdidataitem.keyid3 = rsetitems.keyid3 " + where;
                    selectdl = "SELECT\tsdidataitemlimits.* FROM\tsdidataitemlimits, rsetitems WHERE\tsdidataitemlimits.sdcid = rsetitems.sdcid AND sdidataitemlimits.keyid1 = rsetitems.keyid1 AND sdidataitemlimits.keyid2 = rsetitems.keyid2 AND sdidataitemlimits.keyid3 = rsetitems.keyid3 " + where;
                    selectdis = "SELECT\tsdidataitemspec.* FROM\tsdidataitemspec, sdispec, rsetitems WHERE sdidataitemspec.sdcid = sdispec.sdcid AND sdidataitemspec.keyid1 = sdispec.keyid1 AND sdidataitemspec.keyid2 = sdispec.keyid2 AND sdidataitemspec.keyid3 = sdispec.keyid3 AND sdidataitemspec.specid = sdispec.specid AND sdidataitemspec.specversionid = sdispec.specversionid AND sdidataitemspec.sdcid = rsetitems.sdcid AND sdidataitemspec.keyid1 = rsetitems.keyid1 AND sdidataitemspec.keyid2 = rsetitems.keyid2 AND sdidataitemspec.keyid3 = rsetitems.keyid3 " + where + " order by sdispec.usersequence";
                } else {
                    selectds = "SELECT\tsdidata.*, paramlist.displayunderparamlistname displayunderparamlistname, paramlist.displayundervariantname displayundervariantname FROM sdidata, paramlist, rsetitems WHERE sdidata.s_datasetstatus <> 'Cancelled' AND sdidata.paramlistid=paramlist.paramlistid AND sdidata.paramlistversionid=paramlist.paramlistversionid AND sdidata.variantid=paramlist.variantid AND sdidata.sdcid = rsetitems.sdcid AND sdidata.keyid1 = rsetitems.keyid1 AND sdidata.keyid2 = rsetitems.keyid2 AND sdidata.keyid3 = rsetitems.keyid3 " + where;
                    selectdi = "SELECT\tsdidataitem.*, paramlistitem.displayunderparamname displayunderparamname, paramlistitem.displayunderparamtype displayunderparamtype FROM sdidata, sdidataitem left outer join paramlistitem on sdidataitem.paramlistid=paramlistitem.paramlistid AND sdidataitem.paramlistversionid=paramlistitem.paramlistversionid AND sdidataitem.variantid=paramlistitem.variantid AND sdidataitem.paramid=paramlistitem.paramid AND sdidataitem.paramtype=paramlistitem.paramtype, rsetitems  WHERE " + BaseSDIDataEntryAction.getSDICancelWhere("sdidataitem") + " AND sdidataitem.sdcid = rsetitems.sdcid AND sdidataitem.keyid1 = rsetitems.keyid1 AND sdidataitem.keyid2 = rsetitems.keyid2 AND sdidataitem.keyid3 = rsetitems.keyid3 " + where;
                    selectdl = "SELECT\tsdidataitemlimits.* FROM\tsdidataitemlimits, sdidata, rsetitems  WHERE " + BaseSDIDataEntryAction.getSDICancelWhere("sdidataitemlimits") + " AND sdidataitemlimits.sdcid = rsetitems.sdcid AND sdidataitemlimits.keyid1 = rsetitems.keyid1 AND sdidataitemlimits.keyid2 = rsetitems.keyid2 AND sdidataitemlimits.keyid3 = rsetitems.keyid3 " + where;
                    selectdis = "SELECT\tsdidataitemspec.* FROM\tsdidataitemspec, sdidata, sdispec, rsetitems  WHERE " + BaseSDIDataEntryAction.getSDICancelWhere("sdidataitemspec") + " AND sdidataitemspec.sdcid = sdispec.sdcid AND sdidataitemspec.keyid1 = sdispec.keyid1 AND sdidataitemspec.keyid2 = sdispec.keyid2 AND sdidataitemspec.keyid3 = sdispec.keyid3 AND sdidataitemspec.specid = sdispec.specid AND sdidataitemspec.specversionid = sdispec.specversionid AND sdidataitemspec.sdcid = rsetitems.sdcid AND sdidataitemspec.keyid1 = rsetitems.keyid1 AND sdidataitemspec.keyid2 = rsetitems.keyid2 AND sdidataitemspec.keyid3 = rsetitems.keyid3 " + where + " order by sdispec.usersequence";
                }
                String selects = "SELECT\tsdispec.* FROM\tsdispec, rsetitems WHERE\tsdispec.sdcid = rsetitems.sdcid AND sdispec.keyid1 = rsetitems.keyid1 AND sdispec.keyid2 = rsetitems.keyid2 AND sdispec.keyid3 = rsetitems.keyid3 " + where + " order by sdispec.usersequence";
                this.database.createPreparedResultSet("sdidata", selectds, vars);
                this.database.createPreparedResultSet("sdidataitems", selectdi, vars);
                this.database.createPreparedResultSet("sdidataitemlimits", selectdl, vars);
                this.database.createPreparedResultSet("sdidataitemspec", selectdis, vars);
                this.database.createPreparedResultSet("sdispec", selects, vars);
                primary = this.loadPrimary(sdcid, rsetid);
                if (isExtraCalc) {
                    primary.setString(-1, "sdcid", sdcid);
                    DataSet extraPrimary = this.loadPrimary(extraSdcid, extraRsetid);
                    extraPrimary.setString(-1, "sdcid", extraSdcid);
                    primary.copyRow(extraPrimary, -1, 1);
                }
                sdidata = new DataSet(this.database.getResultSet("sdidata"));
                sdidataitems = new DataSet(this.database.getResultSet("sdidataitems"));
                sdidataitemlimits = new DataSet(this.database.getResultSet("sdidataitemlimits"));
                sdidataitemspec = new DataSet(this.database.getResultSet("sdidataitemspec"));
                sdispec = new DataSet(this.database.getResultSet("sdispec"));
                this.database.closeResultSet("sdidata");
                this.database.closeResultSet("sdidataitems");
                this.database.closeResultSet("sdidataitemlimits");
                this.database.closeResultSet("sdidataitemspec");
                this.database.closeResultSet("sdispec");
                if (!isLiveLimitChecking) {
                    String selectdr = "SELECT dr.sdcid,dr.keyid1,dr.keyid2,dr.keyid3,dr.paramlistid,dr.paramlistversionid,dr.variantid,dr.dataset,dr.tokeyid1 FROM\tsdidatarelation dr, instrument inst, rsetitems WHERE dr.sdcid = rsetitems.sdcid AND dr.keyid1 = rsetitems.keyid1 AND dr.keyid2 = rsetitems.keyid2 AND dr.keyid3 = rsetitems.keyid3 AND dr.relationfunction='Instrument' AND inst.instrumentid=dr.tokeyid1 AND inst.usagetype='DataSet DataEntry' " + where;
                    this.database.createPreparedResultSet("sdidatarelation", selectdr, vars);
                    sdidataTrackedEquipment = new DataSet(this.database.getResultSet("sdidatarelation"));
                    this.database.closeResultSet("sdidatarelation");
                }
            }
            if (isLiveLimitChecking && this.crossSDIAllModifiedSDIDataitems.size() > 0) {
                this.syncCrossSDIValuesForLiveLimitChecking(sdidataitems);
            }
            if (dataSetIndexing = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("datasetindexing", "Y").equals("Y")) {
                sdidataitems.setKeyColumns("sdcid;keyid1;keyid2;keyid3;paramlistid;paramlistversionid;variantid;dataset;paramid;paramtype;replicateid");
                sdidataitems.getIndex().setAutoIndexing(true);
                sdidata.setKeyColumns("sdcid;keyid1;keyid2;keyid3;paramlistid;paramlistversionid;variantid;dataset");
                sdidata.getIndex().setAutoIndexing(true);
                sdidataitemlimits.setKeyColumns("sdcid;keyid1;keyid2;keyid3;paramlistid;paramlistversionid;variantid;dataset;paramid;paramtype;replicateid;limittypeid");
                sdidataitemlimits.getIndex().setAutoIndexing(true);
                sdidataitemspec.setKeyColumns("sdcid;keyid1;keyid2;keyid3;paramlistid;paramlistversionid;variantid;dataset;paramid;paramtype;replicateid;specid;specversionid");
                sdidataitemspec.getIndex().setAutoIndexing(true);
                sdispec.setKeyColumns("sdcid;keyid1;keyid2;keyid3;specid;specversionid");
                sdispec.getIndex().setAutoIndexing(true);
            }
            DataSet arrayItems = null;
            DataSet crossCalcArrayDataItems = null;
            if (isArrayCalc) {
                this.database.createPreparedResultSet("crosscalcdataitems", crosscalcarraydataitems, crosscalcarraydataitemsSafeSQL.getValues());
                crossCalcArrayDataItems = new DataSet(this.database.getResultSet("crosscalcdataitems"));
                this.database.closeResultSet("crosscalcdataitems");
                SafeSQL safeSql = new SafeSQL();
                String selectarrayitems = "SELECT ai.arrayitemid, az.zone, ai.arrayid, az.arrayzoneid   FROM arrayitem ai, arrayzone az, arrayitemarrayzone aiaz  WHERE aiaz.arrayzoneid = az.arrayzoneid AND aiaz.arrayitemid = ai.arrayitemid AND az.arrayid = ai.arrayid AND ai.arrayid in(" + safeSql.addIn(arrayId, ";") + ") AND az.zone!= '(FullArray)' ORDER BY ai.arrayitemid";
                this.database.createPreparedResultSet("arrayitems", selectarrayitems, safeSql.getValues());
                arrayItems = new DataSet(this.database.getResultSet("arrayitems"));
                this.database.closeResultSet("arrayitems");
            }
            DataSet sdidatarelationOverride = properties.getProperty("overridesdidatarelation").length() > 0 ? new DataSet(properties.getProperty("overridesdidatarelation")) : null;
            SDIData sdiData = new SDIData();
            BaseSDCRules sdcRules = null;
            if (!isLiveLimitChecking) {
                String businessRuleSDC = isArrayCalc ? ARRAY_SDCID : sdcid;
                sdcRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), businessRuleSDC, null, "PostDataEntry");
                if (sdcRules.requiresDataEntryPrimary() || sdcRules.customRulesRequiresDataEntryPrimary()) {
                    sdiData.setDataset("primary", primary);
                    sdiData.setDataset("dataset", sdidata);
                }
                if (sdcRules.requiresBeforeDataEntryImage() || sdcRules.customRulesRequiresBeforeDataEntryImage()) {
                    SDIData beforeEditImage = new SDIData();
                    beforeEditImage.setDataset("primary", sdiData.getDataset("primary"));
                    beforeEditImage.setDataset("dataset", sdidata.copy());
                    beforeEditImage.setDataset("dataitem", sdidataitems.copy());
                    beforeEditImage.setDataset("datalimit", sdidataitemlimits.copy());
                    beforeEditImage.setDataset("dataspec", sdidataitemspec.copy());
                    beforeEditImage.setDataset("sdispec", sdispec.copy());
                    sdcRules.setBeforeEditImage(beforeEditImage);
                }
            }
            Trace.log("DataEntry DataSets populated");
            sdidataitems.addColumn("_modified", 0);
            sdidataitems.addColumn("_modifiedtotal", 0);
            sdidataitems.setString(-1, "_modifiedtotal", "N");
            sdidataitemlimits.addColumn("_modifiedtotal", 0);
            sdidataitemlimits.setString(-1, "_modifiedtotal", "N");
            sdidataitemspec.addColumn("_modifiedtotal", 0);
            sdidataitemspec.setString(-1, "_modifiedtotal", "N");
            sdispec.addColumn("_modifiedtotal", 0);
            sdispec.setString(-1, "_modifiedtotal", "N");
            sdidata.addColumn("_modifiedtotal", 0);
            sdidata.setString(-1, "_modifiedtotal", "N");
            for (int i = 0; i < sdidataitems.size(); ++i) {
                if (!sdidataitems.isNull(i, "calcexcludeflag")) continue;
                sdidataitems.setString(i, "calcexcludeflag", "N");
            }
            if (isArrayCalc) {
                crossCalcArrayDataItems.addColumn("_modified", 0);
                crossCalcArrayDataItems.addColumn("_modifiedtotal", 0);
                crossCalcArrayDataItems.setString(-1, "_modifiedtotal", "N");
                crossCalcArrayDataItems.setString(-1, "_modified", "Y");
            }
            if (calcsOnly) {
                sdidataitems.setString(-1, "_modified", "Y");
            } else if (calcReportOnly && propds.size() == 1) {
                HashMap<String, Object> findmap = new HashMap<String, Object>();
                findmap.put("keyid1", propds.getString(0, "keyid1"));
                findmap.put("keyid2", propds.getString(0, "keyid2"));
                findmap.put("keyid3", propds.getString(0, "keyid3"));
                findmap.put("paramlistid", propds.getString(0, "paramlistid"));
                findmap.put("paramlistversionid", propds.getString(0, "paramlistversionid"));
                findmap.put("variantid", propds.getString(0, "variantid"));
                findmap.put("dataset", new BigDecimal(propds.getString(0, "dataset")));
                findmap.put("paramid", propds.getString(0, "paramid"));
                findmap.put("paramtype", propds.getString(0, "paramtype"));
                findmap.put("replicateid", new BigDecimal(propds.getString(0, "replicateid")));
                int findrow = sdidataitems.findRow(findmap);
                if (findrow == -1) {
                    throw new SapphireException("INVALID_PROPERTIES", "Could not Find: " + findmap);
                }
                sdidataitems.addColumn("_calcreport", 0);
                sdidataitems.setString(findrow, "_calcreportrequired", "Y");
                sdidataitems.setString(findrow, "_modified", "Y");
            }
            int[] rownum = new int[proprows];
            String[] values = new String[proprows];
            String[] calcexcludeflags = new String[proprows];
            String[] instrumentid = new String[proprows];
            String[] datacaptureid = new String[proprows];
            HashMap<String, Object> findmap = new HashMap<String, Object>();
            if (limitsOnly) {
                sdidataitems.setString(-1, "_modified", "N");
                findmap.put("sdcid", sdcid);
                for (int i = 0; i < proprows; ++i) {
                    findmap.put("keyid1", propds.getString(i, "keyid1"));
                    findmap.put("keyid2", propds.getString(i, "keyid2"));
                    findmap.put("keyid3", propds.getString(i, "keyid3"));
                    findmap.put("paramlistid", propds.getString(i, "paramlistid"));
                    findmap.put("paramlistversionid", propds.getString(i, "paramlistversionid"));
                    findmap.put("variantid", propds.getString(i, "variantid"));
                    findmap.put("dataset", new BigDecimal(propds.getString(i, "dataset")));
                    if (properties.getProperty("paramid").length() > 0) {
                        findmap.put("paramid", propds.getString(i, "paramid"));
                        findmap.put("paramtype", propds.getString(i, "paramtype"));
                        findmap.put("replicateid", new BigDecimal(propds.getString(i, "replicateid")));
                    }
                    DataSet founditems = sdidataitems.getFilteredDataSet(findmap);
                    founditems.setString(-1, "_modified", "Y");
                }
                this.checkLimits(sdidata, sdidataitems, sdidataitemlimits, isLiveLimitChecking);
            } else if (!(specsOnly || calcsOnly || calcReportOnly)) {
                findmap.put("sdcid", sdcid);
                for (int i = 0; i < proprows; ++i) {
                    findmap.put("keyid1", propds.getString(i, "keyid1"));
                    findmap.put("keyid2", propds.getString(i, "keyid2"));
                    findmap.put("keyid3", propds.getString(i, "keyid3"));
                    findmap.put("paramlistid", propds.getString(i, "paramlistid"));
                    findmap.put("paramlistversionid", propds.getString(i, "paramlistversionid"));
                    findmap.put("variantid", propds.getString(i, "variantid"));
                    findmap.put("dataset", new BigDecimal(propds.getString(i, "dataset")));
                    findmap.put("paramid", propds.getString(i, "paramid"));
                    findmap.put("paramtype", propds.getString(i, "paramtype"));
                    findmap.put("replicateid", new BigDecimal(propds.getString(i, "replicateid")));
                    int findrow = sdidataitems.findRow(findmap);
                    if (findrow == -1) {
                        throw new SapphireException("INVALID_PROPERTIES", "Could not Find: " + findmap);
                    }
                    if (noOverrideReleased && "Y".equals(sdidataitems.getString(findrow, "releasedflag"))) {
                        throw new SapphireException("INVALID_PROPERTIES", "Can not override released data in: " + findmap);
                    }
                    rownum[i] = findrow;
                    String string = values[i] = findrow > -1 ? propds.getString(i, "enteredtext") : "";
                    if (hasCalcExcludeChange) {
                        String string2 = calcexcludeflags[i] = findrow > -1 ? propds.getString(i, "calcexcludeflag", "N") : "N";
                    }
                    if (hasInstrumentId) {
                        String string3 = instrumentid[i] = findrow > -1 ? propds.getString(i, "instrumentid") : "";
                    }
                    if (!hasDatacaptureId) continue;
                    datacaptureid[i] = findrow > -1 ? propds.getString(i, "datacaptureid") : "";
                }
                String[] valuestatus = new String[rownum.length];
                this.enterDataValues(sdidata, sdidataitems, sdidataitemlimits, rownum, values, valuestatus, (String[])(hasCalcExcludeChange ? calcexcludeflags : null), (String[])(hasInstrumentId ? instrumentid : null), (String[])(hasDatacaptureId ? datacaptureid : null));
            }
            if (isArrayCalc) {
                this.appendLinkedDataItems(sdidataitems, crossCalcArrayDataItems);
                sdidataitems.setString(-1, "_modified", "Y");
                sdidataitems.renameColumn("arraymethodid", "_arraymethodid");
                sdidataitems.renameColumn("arraymethodversionid", "_arraymethodversionid");
                sdidataitems.renameColumn("horizontallabel", "_horizontallabel");
                sdidataitems.renameColumn("verticallabel", "_verticallabel");
                sdidataitems.renameColumn("zone", "_zone");
                sdidataitems.renameColumn("arrayid", "_arrayid");
            } else if (isFormulationsCalc) {
                HashMap<String, String> excludeHasCalcNFilter = new HashMap<String, String>();
                excludeHasCalcNFilter.put("_hascalc", "N");
                sdidataitems.getFilteredDataSet(excludeHasCalcNFilter, true).setString(-1, "_modified", "Y");
            }
            HashMap<String, String> filterModifiedTotal = new HashMap<String, String>();
            filterModifiedTotal.put("_modifiedtotal", "Y");
            if (!limitsOnly && !specsOnly) {
                DataEntryCalcUtil dataEntryCalcUtil = new DataEntryCalcUtil(this, this.getSDCProcessor(), this.getQueryProcessor(), this.getConfigurationProcessor(), this.database, this.logger, this.connectionInfo);
                dataEntryCalcUtil.performCalcs(primary, sdidata, sdidataitems, sdidataitemlimits, calcsOnly, calcReportOnly, isLiveLimitChecking, sdidatarelationOverride, hasCalcExcludeChange, calculateModifiedDatasetsOnly, calculateModifiedTestsOnly, isArrayCalc, isFormulationsCalc, isExtraCalc, arrayItems);
            }
            if (isArrayCalc) {
                this.checkArrayLimits(arrayId, sdidataitems);
            }
            DataSet updatedi = sdidataitems.getFilteredDataSet(filterModifiedTotal);
            String tracelogid = properties.getProperty("tracelogid", "").trim();
            if (calcReportOnly) {
                int findRow = sdidataitems.findRow("_calcreportrequired", "Y");
                if (findRow >= 0) {
                    properties.setProperty("calcreport", sdidataitems.getValue(findRow, "_calcreport"));
                }
            } else {
                DataSet modified;
                if (!isLiveLimitChecking) {
                    String auditreason = properties.getProperty("auditreason");
                    String auditactivity = properties.getProperty("auditactivity", "");
                    String auditsignedflag = properties.getProperty("auditsignedflag", "N");
                    if (auditreason.length() > 0 && tracelogid.length() == 0) {
                        if (Trace.on) {
                            this.logger.info("Generate the tracelog records");
                        }
                        PropertyList tracelogprops = new PropertyList();
                        if (isArrayCalc) {
                            tracelogprops.setProperty("sdcid", ARRAY_SDCID);
                        } else {
                            tracelogprops.setProperty("sdcid", sdcid);
                        }
                        tracelogprops.setProperty("standard", "Y");
                        tracelogprops.setProperty("auditactivity", auditactivity);
                        tracelogprops.setProperty("auditsignedflag", auditsignedflag);
                        tracelogprops.setProperty("description", "Data entry action");
                        tracelogprops.setProperty("auditreason", auditreason);
                        tracelogprops.setProperty("auditdt", properties.getProperty("auditdt"));
                        ActionProcessor ap = this.getActionProcessor();
                        ap.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
                        tracelogid = tracelogprops.getProperty("tracelogid");
                        properties.setProperty("tracelogid", tracelogid);
                    }
                    if (tracelogid.length() > 0) {
                        sdidata.setString(-1, "tracelogid", tracelogid);
                        sdispec.setString(-1, "tracelogid", tracelogid);
                        sdidataitems.setString(-1, "tracelogid", tracelogid);
                        sdidataitemlimits.setString(-1, "tracelogid", tracelogid);
                        sdidataitemspec.setString(-1, "tracelogid", tracelogid);
                    }
                    Calendar now = DateTimeUtil.getNowCalendar();
                    String actionId = this.connectionInfo.getTool();
                    String userId = this.connectionInfo.getSysuserId();
                    sdidata.setString(-1, "modby", userId);
                    sdidata.setString(-1, "modtool", actionId);
                    sdidata.setDate(-1, "moddt", now);
                    sdispec.setString(-1, "modby", userId);
                    sdispec.setString(-1, "modtool", actionId);
                    sdispec.setDate(-1, "moddt", now);
                    sdidataitemlimits.setString(-1, "modby", userId);
                    sdidataitemlimits.setString(-1, "modtool", actionId);
                    sdidataitemlimits.setDate(-1, "moddt", now);
                    sdidataitemspec.setString(-1, "modby", userId);
                    sdidataitemspec.setString(-1, "modtool", actionId);
                    sdidataitemspec.setDate(-1, "moddt", now);
                    if (!limitsOnly && !specsOnly) {
                        boolean hasUnrelease;
                        if (Trace.on) {
                            this.logger.info("Saving changes to sdidataitems");
                        }
                        if (tracelogid.length() > 0) {
                            updatedi.setString(-1, "tracelogid", tracelogid);
                        }
                        if (hasUnrelease = "Y".equals(properties.getProperty("hasunrelease"))) {
                            updatedi.setString(-1, "releasedflag", "N");
                        }
                        sdiData.setDataset("dataitem", updatedi);
                        updatedi.renameColumn("displayunderparamname", "__displayunderparamname");
                        updatedi.renameColumn("displayunderparamtype", "__displayunderparamtype");
                        DataSetUtil.update(this.database, updatedi, "sdidataitem", new SDIData().getKeys("dataitem"));
                    }
                    if (!specsOnly) {
                        if (Trace.on) {
                            this.logger.info("Saving changes to sdidatitemlimits");
                        }
                        DataSet updatedil = sdidataitemlimits.getFilteredDataSet(filterModifiedTotal);
                        if (tracelogid.length() > 0) {
                            updatedil.setString(-1, "tracelogid", tracelogid);
                        }
                        sdiData.setDataset("datalimit", updatedil);
                        DataSetUtil.update(this.database, updatedil, "sdidataitemlimits", new SDIData().getKeys("datalimit"));
                    }
                }
                if ((modified = sdidataitems.getFilteredDataSet(filterModifiedTotal)).size() > 0) {
                    if (this.crossSDIAllModifiedSDIDataitems.size() == 0) {
                        this.crossSDIAllModifiedSDIDataitems.copyRow(modified, -1, 1);
                    } else {
                        for (int i = 0; i < modified.size(); ++i) {
                            String sdidataitemid = modified.getValue(i, "sdidataitemid");
                            int row = this.crossSDIAllModifiedSDIDataitems.findRow("sdidataitemid", sdidataitemid);
                            if (row > -1) {
                                this.crossSDIAllModifiedSDIDataitems.deleteRow(row);
                            }
                            this.crossSDIAllModifiedSDIDataitems.copyRow(modified, i, 1);
                        }
                    }
                    properties.put(CROSSSDI_ALL_MODIFIEDDATAITEMS, this.crossSDIAllModifiedSDIDataitems);
                }
                if (isLiveLimitChecking) {
                    properties.put(LIVEDATAENTRY_MODIFIEDDATAITEMS, modified);
                    properties.put("sdidata", sdidata);
                    properties.put("primary", primary);
                }
                if (!limitsOnly) {
                    String[] specidprop = StringUtil.split(properties.getProperty("specid"), ";");
                    String[] specversionidprop = StringUtil.split(properties.getProperty("specversionid"), ";");
                    HashSet<String> specmap = new HashSet<String>();
                    if (specsOnly || properties.getProperty("specid").length() > 0) {
                        for (int i = 0; i < specidprop.length; ++i) {
                            if (StringUtil.getLen(specidprop[i]) <= 0L) continue;
                            specmap.add(specidprop[i] + ";" + (specversionidprop.length > i && specversionidprop[i].length() > 0 ? specversionidprop[i] : "1"));
                        }
                    }
                    if (sdidataitemspec.getRowCount() > 0) {
                        this.checkSpecs(sdidataitems, sdidataitemspec, specmap);
                    }
                    if (isLiveLimitChecking) {
                        properties.put("sdidataitemspec", sdidataitemspec);
                    } else {
                        if (sdidataitemspec.getRowCount() > 0) {
                            this.logger.info("Saving changes to sdidatitemspec");
                            DataSet updatedis = sdidataitemspec.getFilteredDataSet(filterModifiedTotal);
                            sdiData.setDataset("dataspec", updatedis);
                            DataSetUtil.update(this.database, updatedis, "sdidataitemspec", new SDIData().getKeys("dataspec"));
                        }
                        this.logger.info("Checking spec rules");
                        DataSet filteredSpecRuleEvalDataItems = new DataSet();
                        if (sdidataitemspec.getRowCount() > 0) {
                            PropertyList specRuleEvalPolicyPL = this.getSpecRulesPolicy();
                            filteredSpecRuleEvalDataItems = sdidataitemspec.getRowCount() > 0 && this.isSpecFilterRequired(specRuleEvalPolicyPL) ? this.filterSpecDataItemBasedOnPolicy(specRuleEvalPolicyPL, sdidata, sdidataitems, sdidataitemspec) : sdidataitemspec;
                        }
                        this.checkSpecRules(this.database, sdispec, filteredSpecRuleEvalDataItems, sdidataitems);
                        this.logger.info("Saving changes to sdispec");
                        DataSet updatesdispec = sdispec.getFilteredDataSet(filterModifiedTotal);
                        sdiData.setDataset("sdispec", updatesdispec);
                        DataSetUtil.update(this.database, updatesdispec, "sdispec", new SDIData().getKeys("sdispec"));
                        this.logger.info("processing Instruments for sdidata");
                        this.processInstruments(sdidata, sdidataTrackedEquipment, sdidataitems);
                        DataSet updatesdidata = sdidata.getFilteredDataSet(filterModifiedTotal);
                        if (updatesdidata != null && updatesdidata.getRowCount() > 0) {
                            updatesdidata.renameColumn("displayunderparamlistname", "__displayunderparamlistname");
                            updatesdidata.renameColumn("displayundervariantname", "__displayundervariantname");
                            DataSetUtil.update(this.database, updatesdidata, "sdidata", new SDIData().getKeys("dataset"));
                        }
                        if ("Y".equals(properties.getProperty("autorelease"))) {
                            StringBuffer[] releaseProps = new StringBuffer[10];
                            String[] columnids = new String[]{"keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid"};
                            HashMap<String, String> noreleaseFilter = new HashMap<String, String>();
                            PropertyList policy = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom");
                            String allowreleaseblank = policy.getProperty("allowreleaseblank");
                            if ("N".equals(allowreleaseblank)) {
                                noreleaseFilter.put("enteredtext", "");
                            } else {
                                noreleaseFilter.put("mandatoryflag", "Y");
                                noreleaseFilter.put("enteredtext", "");
                            }
                            Set filterset = noreleaseFilter.entrySet();
                            for (int r = 0; r < updatedi.getRowCount(); ++r) {
                                if (((HashMap)updatedi.get(r)).entrySet().containsAll(filterset)) continue;
                                for (int c = 0; c < columnids.length; ++c) {
                                    if (releaseProps[c] == null) {
                                        releaseProps[c] = new StringBuffer();
                                    } else {
                                        releaseProps[c].append(";");
                                    }
                                    releaseProps[c].append(updatedi.getValue(r, columnids[c]));
                                }
                            }
                            if (releaseProps[0] != null) {
                                PropertyList releaseDataItemPl = new PropertyList();
                                releaseDataItemPl.setProperty("sdcid", sdcid);
                                releaseDataItemPl.setProperty("propsmatch", "Y");
                                releaseDataItemPl.setProperty("keyid1", releaseProps[0].toString());
                                releaseDataItemPl.setProperty("keyid2", releaseProps[1].toString());
                                releaseDataItemPl.setProperty("keyid3", releaseProps[2].toString());
                                releaseDataItemPl.setProperty("paramlistid", releaseProps[3].toString());
                                releaseDataItemPl.setProperty("paramlistversionid", releaseProps[4].toString());
                                releaseDataItemPl.setProperty("variantid", releaseProps[5].toString());
                                releaseDataItemPl.setProperty("dataset", releaseProps[6].toString());
                                releaseDataItemPl.setProperty("paramid", releaseProps[7].toString());
                                releaseDataItemPl.setProperty("paramtype", releaseProps[8].toString());
                                releaseDataItemPl.setProperty("replicateid", releaseProps[9].toString());
                                releaseDataItemPl.setProperty("tracelogid", tracelogid);
                                if (calculateModifiedDatasetsOnly) {
                                    releaseDataItemPl.setProperty("calculatemodifieddatasetsonly", "Y");
                                }
                                if (calculateModifiedTestsOnly) {
                                    releaseDataItemPl.setProperty("calculatemodifiedtestsonly", "Y");
                                }
                                this.getActionProcessor().processAction("ReleaseDataItem", "1", releaseDataItemPl);
                            }
                        }
                        if (!"Y".equalsIgnoreCase(properties.getProperty("postspecapply"))) {
                            sdcRules.setEvent("PostDataEntry");
                            String businessRuleSDC = isArrayCalc ? ARRAY_SDCID : sdcid;
                            Trace.startBusinessRule(businessRuleSDC + "." + "PostDataEntry", true);
                            sdcRules.postDataEntry(sdiData, properties);
                            Trace.endBusinessRule(businessRuleSDC + "." + "PostDataEntry", true);
                            Trace.startBusinessRule(businessRuleSDC + "." + "PostDataEntry", false);
                            for (BaseSDCRules customRules : sdcRules.getCustomRuleList()) {
                                customRules.postDataEntry(sdiData, properties);
                            }
                            Trace.endBusinessRule(businessRuleSDC + "." + "PostDataEntry", false);
                        }
                        sdcRules.endRule();
                    }
                    boolean autoCalcCrossSDI = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("autocalculatecrosssdicalcrule", "Y").equals("Y");
                    if (autoCalcCrossSDI) {
                        this.callRedoCalcOnLinkedSDIs(updatedi, rsetid, sdcProcessor, this.getQueryProcessor(), tracelogid);
                    }
                }
            }
        }
        finally {
            if (rsetid != null && rsetid.length() > 0) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
            if (arrayPrimaryRsetId != null && arrayPrimaryRsetId.length() > 0) {
                this.getDAMProcessor().clearRSet(arrayPrimaryRsetId);
            }
            if (extraRsetid != null && extraRsetid.length() > 0) {
                this.getDAMProcessor().clearRSet(extraRsetid);
            }
        }
    }

    private void syncCrossSDIValuesForLiveLimitChecking(DataSet sdidataitems) {
        ArrayList<DataSet> groupedBySampleFromDatabase = sdidataitems.getGroupedDataSets("sdcid,keyid1,keyid2,keyid3");
        for (DataSet dataitemsPerSample : groupedBySampleFromDatabase) {
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("sdcid", dataitemsPerSample.getString(0, "sdcid"));
            findMap.put("keyid1", dataitemsPerSample.getString(0, "keyid1"));
            findMap.put("keyid2", dataitemsPerSample.getString(0, "keyid2"));
            findMap.put("keyid3", dataitemsPerSample.getString(0, "keyid3"));
            DataSet crossSDIModifiedThisSample = this.crossSDIAllModifiedSDIDataitems.getFilteredDataSet(findMap);
            if (crossSDIModifiedThisSample.size() <= 0) continue;
            for (int i = 0; i < dataitemsPerSample.size(); ++i) {
                int findRow = crossSDIModifiedThisSample.findRow("sdidataitemid", dataitemsPerSample.getString(i, "sdidataitemid"));
                if (findRow < 0) continue;
                dataitemsPerSample.setObject(i, "enteredtext", crossSDIModifiedThisSample.getObject(findRow, "enteredtext"));
                dataitemsPerSample.setObject(i, "enteredqualifier", crossSDIModifiedThisSample.getObject(findRow, "enteredqualifier"));
                dataitemsPerSample.setObject(i, "enteredoperator", crossSDIModifiedThisSample.getObject(findRow, "enteredoperator"));
                dataitemsPerSample.setObject(i, "enteredvalue", crossSDIModifiedThisSample.getObject(findRow, "enteredvalue"));
                dataitemsPerSample.setObject(i, "enteredunits", crossSDIModifiedThisSample.getObject(findRow, "enteredunits"));
                dataitemsPerSample.setObject(i, "transformvalue", crossSDIModifiedThisSample.getObject(findRow, "transformvalue"));
                dataitemsPerSample.setObject(i, "transformtext", crossSDIModifiedThisSample.getObject(findRow, "transformtext"));
                dataitemsPerSample.setObject(i, "transformdt", crossSDIModifiedThisSample.getObject(findRow, "transformdt"));
                dataitemsPerSample.setObject(i, "displayvalue", crossSDIModifiedThisSample.getObject(findRow, "displayvalue"));
                dataitemsPerSample.setObject(i, "valuestatus", crossSDIModifiedThisSample.getObject(findRow, "valuestatus"));
            }
        }
    }

    static String getSDICancelWhere(String tableid) {
        return " sdidata.s_datasetstatus <> 'Cancelled' AND sdidata.sdcid=" + tableid + ".sdcid AND sdidata.keyid1=" + tableid + ".keyid1 AND sdidata.keyid2=" + tableid + ".keyid2 AND sdidata.keyid3=" + tableid + ".keyid3 AND sdidata.paramlistid=" + tableid + ".paramlistid AND sdidata.paramlistversionid=" + tableid + ".paramlistversionid AND sdidata.variantid=" + tableid + ".variantid AND sdidata.dataset=" + tableid + ".dataset ";
    }

    public static boolean[] getReleasedAndManadatoryReleased(DBAccess database, String where, SafeSQL safeSQL) throws SapphireException {
        boolean[] releasedAndManadatoryReleased = new boolean[]{true, true};
        database.createPreparedResultSet("checkReleasedAndMandatoryReleased", "SELECT releasedflag, mandatoryflag FROM sdidataitem, rsetitems " + where, safeSQL.getValues());
        while ((releasedAndManadatoryReleased[0] || releasedAndManadatoryReleased[1]) && database.getNext("checkReleasedAndMandatoryReleased")) {
            if (database.getValue("checkReleasedAndMandatoryReleased", "releasedflag").equals("Y")) continue;
            releasedAndManadatoryReleased[0] = false;
            if (!database.getValue("checkReleasedAndMandatoryReleased", "mandatoryflag").equals("Y")) continue;
            releasedAndManadatoryReleased[1] = false;
        }
        return releasedAndManadatoryReleased;
    }

    DataSet loadPrimary(String sdcid, String rsetid) throws SapphireException {
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdcProps = sdcProcessor.getProperties(sdcid);
        StringBuilder selectprimary = new StringBuilder("SELECT * FROM " + sdcProps.getProperty("tableid") + ",rsetitems WHERE rsetid = ? AND rsetitems.keyid1 = " + sdcProps.getProperty("keycolid1"));
        for (int i = 1; i < Integer.parseInt(sdcProps.getProperty("keycolumns")); ++i) {
            selectprimary.append(" AND rsetitems.keyid").append(i + 1).append(" = ").append(sdcProps.getProperty("keycolid" + (i + 1)));
        }
        this.database.createPreparedResultSet("primary", selectprimary.toString(), new Object[]{rsetid});
        DataSet primary = new DataSet(this.database.getResultSet("primary"));
        return primary;
    }

    private String getDSRSet(String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset, boolean applylock) throws SapphireException {
        try {
            DataAccessService das = new DataAccessService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            RSet rset = das.createRSetDS(sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, true, applylock);
            if (applylock) {
                rset.setRSet(das.lockRSet(rset, "DA", 3));
            }
            return rset.getRsetid();
        }
        catch (ServiceException e) {
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create rset for " + new SDI(sdcid, keyid1, keyid2, keyid3).getKeyText() + " and it's datasets", e);
        }
    }

    private void checkLimits(DataSet sdidata, DataSet sdidataitems, DataSet sdidataitemlimits, boolean isLiveLimitChecking) throws SapphireException {
        long stTime = System.currentTimeMillis();
        this.logger.info("CALC_PERFORMANCE: checkLimits() processing started.");
        HashMap<String, DataSet> limitrulecache = new HashMap<String, DataSet>();
        HashMap<String, BigDecimal> expressionParam = new HashMap<String, BigDecimal>();
        String initsdcid = "";
        String initkeyid1 = "";
        String initkeyid2 = "";
        String initkeyid3 = "";
        FormatUtil formatutil = FormatUtil.getInstance(this.connectionInfo);
        DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
        PropertyList policy = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom");
        boolean enteredLimitMet = policy.getProperty("enteredlimitmet").equals("Y");
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("_modified", "Y");
        DataSet di = sdidataitems.getFilteredDataSet(filter);
        DataSet sdidil = null;
        int rows = di.getRowCount();
        for (int row = 0; row < rows; ++row) {
            DataSet dsLimitrule;
            filter.clear();
            filter.put("sdcid", di.getString(row, "sdcid"));
            filter.put("keyid1", di.getString(row, "keyid1"));
            filter.put("keyid2", di.getString(row, "keyid2"));
            filter.put("keyid3", di.getString(row, "keyid3"));
            if (!(di.getString(row, "keyid1").equals(initkeyid1) && di.getString(row, "keyid2").equals(initkeyid2) && di.getString(row, "keyid3").equals(initkeyid3) && di.getString(row, "sdcid").equals(initsdcid))) {
                initsdcid = di.getString(row, "sdcid");
                initkeyid1 = di.getString(row, "keyid1");
                initkeyid2 = di.getString(row, "keyid2");
                initkeyid3 = di.getString(row, "keyid3");
                sdidil = sdidataitemlimits.getFilteredDataSet(filter);
            }
            filter.put("paramlistid", di.getString(row, "paramlistid"));
            filter.put("paramlistversionid", di.getString(row, "paramlistversionid"));
            filter.put("variantid", di.getString(row, "variantid"));
            int sdidatarow = sdidata.findRow(filter);
            String limitruleid = sdidata.getString(sdidatarow, "limitruleid");
            String limitruleversionid = sdidata.getString(sdidatarow, "limitruleversionid");
            if (StringUtil.getLen(limitruleversionid) == 0L) {
                limitruleversionid = "1";
            }
            if (StringUtil.getLen(limitruleid) > 0L) {
                dsLimitrule = (DataSet)limitrulecache.get(limitruleid + ";" + limitruleversionid);
                if (dsLimitrule == null) {
                    this.database.createPreparedResultSet("LimitRule", "SELECT limitrulelimittype.*, limitrule.processingrule, limitrule.actionid finalactionid, limitrule.actionversionid finalactionversionid FROM limitrulelimittype, limitrule WHERE limitrule.limitruleid = limitrulelimittype.limitruleid and limitrule.limitruleversionid = limitrulelimittype.limitruleversionid and limitrule.limitruleid=? and limitrule.limitruleversionid=?", new Object[]{limitruleid, limitruleversionid});
                    dsLimitrule = new DataSet(this.database.getResultSet("LimitRule"));
                    dsLimitrule.sort("usersequence, limittypeid, limittypestatus");
                    limitrulecache.put(limitruleid + ";" + limitruleversionid, dsLimitrule);
                    this.logger.info(dsLimitrule.getRowCount() + " rows loaded for limitrule " + limitruleid);
                }
            } else {
                dsLimitrule = null;
            }
            filter.put("dataset", di.getBigDecimal(row, "dataset"));
            filter.put("paramid", di.getString(row, "paramid"));
            filter.put("paramtype", di.getString(row, "paramtype"));
            filter.put("replicateid", di.getBigDecimal(row, "replicateid"));
            DataSet dil = sdidil == null ? new DataSet() : sdidil.getFilteredDataSet(filter);
            dil.setString(-1, "_modifiedtotal", "Y");
            int limits = dil.getRowCount();
            for (int limit = 0; limit < limits; ++limit) {
                String enteredText = di.getString(row, "enteredtext");
                if (!"(null)".equals(enteredText) && StringUtil.getLen(enteredText) > 0L) {
                    Object comparevalue;
                    String datatypes = di.getString(row, "datatypes");
                    if (datatypes.equals("A") && (datatypes = di.getValue(row, COL_APPARENTDATATYPE)).length() == 0) {
                        DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
                        datatypes = DataEntryLimitsUtil.getApparentDatatype(enteredText, dtu, formatutil);
                    }
                    if (datatypes.equals("N") || datatypes.equals("NC")) {
                        String honorEnteredOperator;
                        comparevalue = di.getBigDecimal(row, "transformvalue");
                        boolean undeterminedLimit = false;
                        PropertyList paramLimitRulePL = policy.getPropertyList("undeterminedparamlimitsrule");
                        String string = honorEnteredOperator = paramLimitRulePL != null ? paramLimitRulePL.getProperty("honorenteredoperator") : "";
                        if (di.getString(row, "enteredoperator", "").length() > 0 && honorEnteredOperator.equals("Y")) {
                            undeterminedLimit = limitsUtil.isLimitUndetermined(comparevalue, dil.getString(limit, "operator"), limitsUtil.getLimitValue(datatypes, dil.getValue(limit, "value1", ""), dil.getValue(limit, "value1num", "")), limitsUtil.getLimitValue(datatypes, dil.getValue(limit, "value2", ""), dil.getValue(limit, "value2num", "")), datatypes, di.getString(row, "enteredoperator"));
                        }
                        if (honorEnteredOperator.equals("N") || di.getString(row, "enteredoperator", "").length() == 0 || !undeterminedLimit) {
                            if (comparevalue == null && enteredLimitMet && enteredText != null && enteredText.length() > 0) {
                                String limittypeid = dil.getString(limit, "limittypeid");
                                if (enteredText.equalsIgnoreCase(limittypeid) || enteredText.equalsIgnoreCase("<" + limittypeid) || enteredText.equalsIgnoreCase("<=" + limittypeid) || enteredText.equalsIgnoreCase(">" + limittypeid) || enteredText.equalsIgnoreCase(">=" + limittypeid) || enteredText.equalsIgnoreCase("=" + limittypeid)) {
                                    dil.setString(limit, "statusflag", "Y");
                                }
                            } else {
                                String diunit = di.getString(row, "displayunits");
                                String limitunit = dil.getString(limit, "unitsid");
                                if (StringUtil.getLen(limitunit) > 0L && StringUtil.getLen(diunit) > 0L && !diunit.equalsIgnoreCase(limitunit)) {
                                    StringHolder unitholder = new StringHolder();
                                    unitholder.value = diunit;
                                    comparevalue = limitsUtil.convertUnits(unitholder, limitunit, (BigDecimal)comparevalue);
                                }
                                if (comparevalue != null) {
                                    if (limitsUtil.isLimitMet(comparevalue, dil.getString(limit, "operator"), limitsUtil.getLimitValue(datatypes, dil.getValue(limit, "value1", ""), dil.getValue(limit, "value1num", "")), limitsUtil.getLimitValue(datatypes, dil.getValue(limit, "value2", ""), dil.getValue(limit, "value2num", "")), datatypes, "", di.getString(row, "enteredoperator"))) {
                                        dil.setString(limit, "statusflag", "Y");
                                    } else {
                                        dil.setString(limit, "statusflag", "N");
                                    }
                                } else {
                                    dil.setString(limit, "statusflag", "U");
                                }
                            }
                        }
                        if (!undeterminedLimit) continue;
                        String useLimit = paramLimitRulePL.getProperty("uselimit");
                        if (useLimit.equals("Limit Met")) {
                            dil.setString(limit, "statusflag", "Y");
                            continue;
                        }
                        if (useLimit.equals("Limit Not Met")) {
                            dil.setString(limit, "statusflag", "N");
                            continue;
                        }
                        if (!useLimit.equals("Skip Limit")) continue;
                        dil.setString(limit, "statusflag", "U");
                        continue;
                    }
                    if (!datatypes.equals("T") && !datatypes.equals("TC") && !datatypes.equals("R") && !datatypes.equals("V") && !datatypes.equals("S") || (comparevalue = di.getString(row, "transformtext")) == null || ((String)comparevalue).length() <= 0) continue;
                    String extrastuff = null;
                    if (datatypes.equals("R") || datatypes.equals("V")) {
                        extrastuff = di.getString(row, "entryreftypeid");
                    } else if (datatypes.equals("S")) {
                        extrastuff = di.getString(row, "entrysdcid");
                    }
                    if (limitsUtil.isLimitMet(comparevalue, dil.getString(limit, "operator"), limitsUtil.getLimitValue(datatypes, dil.getValue(limit, "value1", ""), dil.getValue(limit, "value1num", "")), limitsUtil.getLimitValue(datatypes, dil.getValue(limit, "value2", ""), dil.getValue(limit, "value2num", "")), datatypes, extrastuff, di.getString(row, "enteredoperator"))) {
                        dil.setString(limit, "statusflag", "Y");
                        continue;
                    }
                    dil.setString(limit, "statusflag", "N");
                    continue;
                }
                dil.setString(limit, "statusflag", "U");
            }
            if (dsLimitrule == null) continue;
            String displayValueFormat = null;
            boolean ruleTypeApplied = false;
            for (int limitRule = 0; limitRule < dsLimitrule.size(); ++limitRule) {
                String limittypeid = dsLimitrule.getString(limitRule, "limittypeid");
                String limittypeStatus = dsLimitrule.getString(limitRule, "limittypestatus");
                filter.clear();
                filter.put("limittypeid", limittypeid);
                int dataitemlimitRow = dil.findRow(filter);
                if (dataitemlimitRow < 0) continue;
                String statusFlag = dil.getString(dataitemlimitRow, "statusflag");
                if ("Y".equals(statusFlag) && ("E".equals(limittypeStatus) || "Y".equals(limittypeStatus)) || "N".equals(statusFlag) && ("E".equals(limittypeStatus) || "N".equals(limittypeStatus))) {
                    String condition;
                    String qualifier;
                    String format;
                    String rejectValueFlag = dsLimitrule.getValue(limitRule, "rejectvalueflag");
                    if (rejectValueFlag.equals("Y")) {
                        di.setString(row, "valuestatus", "VALUE_REJECTED");
                        break;
                    }
                    String transformexpression = dsLimitrule.getString(limitRule, "transformrule");
                    if (StringUtil.getLen(transformexpression) > 0L) {
                        transformexpression = StringUtil.replaceAll(transformexpression, "[value1]", dil.getString(dataitemlimitRow, "value1"), false);
                        transformexpression = StringUtil.replaceAll(transformexpression, "[value2]", dil.getString(dataitemlimitRow, "value2"), false);
                        transformexpression = StringUtil.replaceAll(transformexpression, "[operator]", dil.getString(dataitemlimitRow, "operator"), false);
                        transformexpression = StringUtil.replaceAll(transformexpression, "[enteredoperator]", sdidataitems.getString(row, "enteredoperator"), false);
                        expressionParam.clear();
                        expressionParam.put("this", di.getBigDecimal(row, "transformvalue"));
                        String result = ExpressionUtil.evaluate(transformexpression, expressionParam);
                        try {
                            di.setNumber(row, "transformvalue", FormatUtil.getInstance().parseBigDecimal(result));
                            di.setString(row, "displayvalue", result);
                        }
                        catch (NumberFormatException nfe) {
                            di.setString(row, "valuestatus", "TRANSFORM_FAILURE");
                        }
                    }
                    if (StringUtil.getLen(format = dsLimitrule.getString(limitRule, "displayformat")) > 0L) {
                        format = StringUtil.replaceAll(format, "[value1]", dil.getString(dataitemlimitRow, "value1"), false);
                        format = StringUtil.replaceAll(format, "[value2]", dil.getString(dataitemlimitRow, "value2"), false);
                        format = StringUtil.replaceAll(format, "[operator]", dil.getString(dataitemlimitRow, "operator"), false);
                        format = StringUtil.replaceAll(format, "[enteredoperator]", sdidataitems.getString(row, "enteredoperator"), false);
                        if (!"N".equals(di.getString(row, "datatypes")) && !"NC".equals(di.getString(row, "datatypes"))) {
                            format = StringUtil.replaceAll(format, "'", "", false);
                            di.setString(row, "displayvalue", format);
                            displayValueFormat = "[literal]";
                        } else {
                            BigDecimal trValue = di.getBigDecimal(row, "transformvalue");
                            if (trValue != null) {
                                di.setString(row, "displayvalue", NumericFormatter.formatNumber(di.getBigDecimal(row, "transformvalue"), format, I18nUtil.getUnitTrimmedEnteredText(di.getString(row, "enteredtext"), formatutil)));
                                displayValueFormat = format;
                            }
                        }
                    }
                    if (StringUtil.getLen(qualifier = dsLimitrule.getString(limitRule, "qualifier")) > 0L) {
                        qualifier = StringUtil.replaceAll(qualifier, "[value1]", dil.getString(dataitemlimitRow, "value1"), false);
                        qualifier = StringUtil.replaceAll(qualifier, "[value2]", dil.getString(dataitemlimitRow, "value2"), false);
                        qualifier = StringUtil.replaceAll(qualifier, "[operator]", dil.getString(dataitemlimitRow, "operator"), false);
                        qualifier = StringUtil.replaceAll(qualifier, "[enteredoperator]", sdidataitems.getString(row, "enteredoperator"), false);
                        di.setString(row, "enteredqualifier", qualifier);
                    }
                    if (StringUtil.getLen(condition = dsLimitrule.getString(limitRule, "condition")) > 0L) {
                        di.setString(row, "condition", condition);
                    }
                    di.setNumber(row, "textcolor", dsLimitrule.getInt(limitRule, "textcolor"));
                    if (!isLiveLimitChecking) {
                        String actionid = dsLimitrule.getValue(limitRule, "actionid");
                        String actionversionid = dsLimitrule.getValue(limitRule, "actionversionid", "1");
                        this.processLimitRuleAction(limitruleid, limitruleversionid, limittypeid, limittypeStatus, actionid, actionversionid, di, row);
                    }
                    if (dsLimitrule.getString(limitRule, "processingrule").equals("F")) break;
                    ruleTypeApplied = true;
                    continue;
                }
                if (ruleTypeApplied) continue;
                di.setString(row, "displayvalueformat", "");
                di.setString(row, "enteredqualifier", "");
                di.setString(row, "condition", "");
                di.setNumber(row, "textcolor", "");
            }
            di.setString(row, "displayvalueformat", displayValueFormat);
            if (isLiveLimitChecking) continue;
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("limittypeid", "(null)");
            findMap.put("limittypestatus", "(null)");
            int findRow = dsLimitrule.findRow(findMap);
            if (findRow < 0) continue;
            String actionid = dsLimitrule.getValue(findRow, "finalactionid");
            String actionversionid = dsLimitrule.getValue(findRow, "finalactionversionid", "1");
            this.processLimitRuleAction(limitruleid, limitruleversionid, "(null)", "(null)", actionid, actionversionid, di, row);
        }
        long eTime = System.currentTimeMillis();
        this.logger.info("Process time for limit rule: " + (eTime - stTime));
    }

    void enterDataValues(DataSet sdidata, DataSet sdidataitems, DataSet sdidataitemlimits, int[] rownum, String[] values, String[] valuestatus) throws SapphireException {
        this.enterDataValues(sdidata, sdidataitems, sdidataitemlimits, rownum, values, valuestatus, null, null, null);
    }

    private void enterDataValues(DataSet sdidata, DataSet sdidataitems, DataSet sdidataitemlimits, int[] rownum, String[] values, String[] valuestatus, String[] calcexcludeflags, String[] instrumentid, String[] datacaptureid) throws SapphireException {
        sdidataitems.setString(-1, "_modified", "N");
        HashMap sdidataitemlimitsMap = new HashMap();
        M18NUtil m18n = new M18NUtil(this.connectionInfo);
        DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
        FormatUtil formatutil = FormatUtil.getInstance(this.connectionInfo);
        DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
        Calendar now = DateTimeUtil.getNowCalendar();
        boolean hasNewExclude = false;
        for (int row = 0; row < rownum.length; ++row) {
            if (rownum[row] > -1) {
                boolean calcExcludeChangeOnly = sdidataitems.getValue(rownum[row], "enteredtext").equals(values[row]) && calcexcludeflags != null && !sdidataitems.getValue(rownum[row], "calcexcludeflag").equals(calcexcludeflags[row]);
                sdidataitems.setString(rownum[row], "_modified", "Y");
                sdidataitems.setString(rownum[row], "_modifiedtotal", "Y");
                sdidataitems.setString(rownum[row], "modby", this.connectionInfo.getSysuserId());
                sdidataitems.setDate(rownum[row], "moddt", now);
                sdidataitems.setString(rownum[row], "modtool", this.connectionInfo.getTool());
                if (!calcExcludeChangeOnly) {
                    sdidataitems.setString(rownum[row], "enteredtext", values[row]);
                    sdidataitems.setString(rownum[row], "enteredqualifier", "");
                    sdidataitems.setString(rownum[row], "enteredoperator", "");
                    sdidataitems.setString(rownum[row], "enteredvalue", null);
                    sdidataitems.setString(rownum[row], "enteredunits", "");
                    sdidataitems.setString(rownum[row], "displayvalue", "");
                    sdidataitems.setObject(rownum[row], "transformvalue", null);
                    sdidataitems.setObject(rownum[row], "transformdt", null);
                    sdidataitems.setString(rownum[row], "transformtext", null);
                    sdidataitems.setNumber(rownum[row], "uncertaintyvalue", "(null)");
                    sdidataitems.setString(rownum[row], "uncertaintydisplayvalue", null);
                    sdidataitems.setNumber(rownum[row], "uncertaintyvalueupper", "(null)");
                    sdidataitems.setString(rownum[row], "uncertaintydisplayvalueupper", null);
                }
                sdidataitems.setObject(rownum[row], "textcolor", null);
                sdidataitems.setString(rownum[row], "_modified", "Y");
                sdidataitems.setString(rownum[row], "_modifiedtotal", "Y");
                sdidataitems.setString(rownum[row], "valuestatus", valuestatus[row]);
                if (calcexcludeflags != null && !"(null)".equalsIgnoreCase(calcexcludeflags[row])) {
                    String oldExclude = sdidataitems.getValue(rownum[row], "calcexcludeflag");
                    if (this.isLiveLimitChecking || !oldExclude.equals(calcexcludeflags[row])) {
                        sdidataitems.setString(rownum[row], "calcexcludeflag", calcexcludeflags[row]);
                        if (!hasNewExclude && ("Y".equals(calcexcludeflags[row]) || "N".equals(calcexcludeflags[row]))) {
                            hasNewExclude = true;
                        }
                    }
                }
                if (instrumentid != null && !"(null)".equalsIgnoreCase(instrumentid[row])) {
                    sdidataitems.setString(rownum[row], "instrumentid", instrumentid[row]);
                }
                if (datacaptureid != null && !"(null)".equalsIgnoreCase(datacaptureid[row])) {
                    sdidataitems.setString(rownum[row], "datacaptureid", datacaptureid[row]);
                }
                String sysuserid = this.connectionInfo.getSysuserId();
                if (sdidataitems.getColumnType("s_analystid") == 0 && sysuserid != null && !sysuserid.equalsIgnoreCase("(system)")) {
                    sdidataitems.setString(rownum[row], "s_analystid", this.connectionInfo.getSysuserId());
                }
                if (!calcExcludeChangeOnly && rownum[row] >= 0 && (valuestatus[row] == null || valuestatus[row].equals(""))) {
                    int qualifierpos;
                    String datatypes;
                    String entry = values[row];
                    if (entry == null || entry.equals("(null)")) {
                        entry = "";
                    }
                    if (("N".equals(datatypes = sdidataitems.getString(rownum[row], "datatypes")) || "NC".equals(datatypes)) && (qualifierpos = entry.indexOf("!")) > -1) {
                        sdidataitems.setString(rownum[row], "enteredqualifier", entry.substring(qualifierpos + 1));
                        entry = entry.substring(0, qualifierpos);
                    }
                    if (datatypes.equals("A")) {
                        datatypes = DataEntryLimitsUtil.getApparentDatatype(entry, dtu, formatutil);
                        sdidataitems.setString(rownum[row], COL_APPARENTDATATYPE, datatypes);
                    }
                    if (datatypes.equals("N") || datatypes.equals("NC")) {
                        String numtext = entry;
                        String limittypeid = "";
                        String numoperator = "";
                        if (entry.length() > 0) {
                            String enteredunits;
                            block97: {
                                enteredunits = "";
                                try {
                                    formatutil.parseBigDecimal(entry);
                                }
                                catch (NumberFormatException nfe) {
                                    if (Trace.on) {
                                        this.logger.info("Not a number!");
                                    }
                                    switch (entry.charAt(0)) {
                                        case '<': 
                                        case '>': {
                                            if (entry.length() > 1 && entry.charAt(1) == '=') {
                                                numoperator = entry.substring(0, 2);
                                                numtext = entry.substring(2);
                                                break;
                                            }
                                            numoperator = entry.substring(0, 1);
                                            numtext = entry.substring(1);
                                            break;
                                        }
                                        case '=': {
                                            numtext = entry.substring(1);
                                        }
                                    }
                                    sdidataitems.setString(rownum[row], "enteredoperator", numoperator);
                                    try {
                                        formatutil.parseBigDecimal(numtext);
                                    }
                                    catch (NumberFormatException nfe1) {
                                        StringHolder limittypeidholder = new StringHolder();
                                        limittypeidholder.value = limittypeid = numtext;
                                        if (this.isValidLimit(rownum[row], sdidataitems, sdidataitemlimits, limittypeidholder, sdidataitemlimitsMap)) {
                                            limittypeid = limittypeidholder.value;
                                            break block97;
                                        }
                                        limittypeid = "";
                                        sdidataitems.setString(rownum[row], "valuestatus", "NON_NUMERIC");
                                        for (int i = numtext.length(); i > 0; --i) {
                                            try {
                                                formatutil.parseBigDecimal(numtext.substring(0, i));
                                                enteredunits = numtext.substring(i).trim();
                                                numtext = numtext.substring(0, i);
                                                sdidataitems.setString(rownum[row], "valuestatus", "");
                                                break;
                                            }
                                            catch (NumberFormatException nfe2) {
                                                if (!"INVALID_GROUP_SEPARATOR_POSITION".equals(nfe2.getMessage())) continue;
                                                sdidataitems.setString(rownum[row], "valuestatus", "NON_NUMERIC");
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            if (limittypeid.length() == 0) {
                                if (StringUtil.getLen(sdidataitems.getString(rownum[row], "valuestatus")) == 0L) {
                                    String uncertaintyFunction;
                                    String transrule;
                                    try {
                                        BigDecimal enteredValue = formatutil.parseBigDecimal(numtext);
                                        if (this.connectionInfo.getDbms().equals("MSS") && enteredValue.scale() > 10) {
                                            enteredValue = enteredValue.divide(new BigDecimal(1.0), 10, 4);
                                        }
                                        sdidataitems.setNumber(rownum[row], "enteredvalue", enteredValue);
                                    }
                                    catch (NumberFormatException nfe) {
                                        throw new SapphireException("INVALID_PARAMETER", "Not a valid number: " + numtext);
                                    }
                                    if (enteredunits.length() > 0) {
                                        String displayunits = sdidataitems.getString(rownum[row], "displayunits");
                                        StringHolder unitsidholder = new StringHolder();
                                        unitsidholder.value = enteredunits;
                                        if (displayunits == null || displayunits.length() == 0) {
                                            if (this.isValidUnits(unitsidholder)) {
                                                enteredunits = unitsidholder.value;
                                                sdidataitems.setString(rownum[row], "displayunits", unitsidholder.value);
                                                sdidataitems.setNumber(rownum[row], "transformvalue", sdidataitems.getBigDecimal(rownum[row], "enteredvalue"));
                                            } else {
                                                enteredunits = "";
                                                sdidataitems.setString(rownum[row], "valuestatus", "INVALID_UNITS");
                                            }
                                        } else if (!limitsUtil.convertUnits(unitsidholder, displayunits, sdidataitems, rownum[row])) {
                                            enteredunits = "";
                                            sdidataitems.setString(rownum[row], "valuestatus", "INVALID_UNITS");
                                        }
                                    } else {
                                        sdidataitems.setNumber(rownum[row], "transformvalue", sdidataitems.getBigDecimal(rownum[row], "enteredvalue"));
                                    }
                                    sdidataitems.setString(rownum[row], "enteredunits", enteredunits);
                                    HashMap<String, BigDecimal> expressionParams = new HashMap<String, BigDecimal>();
                                    expressionParams.put("this", sdidataitems.getBigDecimal(rownum[row], "transformvalue"));
                                    if (sdidataitems.getString(rownum[row], "transformdeferflag") != null && sdidataitems.getString(rownum[row], "transformdeferflag").equals("N") && (transrule = sdidataitems.getString(rownum[row], "transformrule")) != null && transrule.length() > 0) {
                                        String result = ExpressionUtil.evaluate(transrule, expressionParams);
                                        try {
                                            sdidataitems.setNumber(rownum[row], "transformvalue", FormatUtil.getInstance(I18nUtil.getSysLocale()).parseBigDecimal(result));
                                        }
                                        catch (NumberFormatException nfe) {
                                            sdidataitems.setString(rownum[row], "valuestatus", "TRANSFORM_FAILURE");
                                        }
                                    }
                                    if ((uncertaintyFunction = sdidataitems.getValue(rownum[row], "uncertaintyfunction")).length() > 0) {
                                        String upperUncertaintyFunction;
                                        String uncertaintyValue = ExpressionUtil.evaluateSecure(this.connectionInfo, uncertaintyFunction, expressionParams);
                                        try {
                                            String uDisplayValue;
                                            try {
                                                sdidataitems.setNumber(rownum[row], "uncertaintyvalue", FormatUtil.getInstance(I18nUtil.getSysLocale()).parseBigDecimal(uncertaintyValue));
                                                BigDecimal uValue = sdidataitems.getBigDecimal(rownum[row], "uncertaintyvalue");
                                                String uDisplayFormat = sdidataitems.getString(rownum[row], "uncertaintydisplayformat");
                                                if (uDisplayFormat.toLowerCase().contains("[eval")) {
                                                    uDisplayFormat = NumericFormatter.getEvaluatedFormat(sdidataitems.getBigDecimal(rownum[row], "transformvalue"), uDisplayFormat);
                                                }
                                                uDisplayValue = NumericFormatter.formatNumber(uValue, uDisplayFormat, String.valueOf(uValue));
                                            }
                                            catch (NumberFormatException nfe) {
                                                sdidataitems.setNumber(rownum[row], "uncertaintyvalue", "(null)");
                                                uDisplayValue = uncertaintyValue;
                                            }
                                            sdidataitems.setString(rownum[row], "uncertaintydisplayvalue", uDisplayValue);
                                        }
                                        catch (NumberFormatException nfe) {
                                            sdidataitems.setString(rownum[row], "valuestatus", "UNCERTAINTY_FAILURE");
                                        }
                                        String asymmetricUncertaintyFlag = sdidataitems.getValue(rownum[row], "uncertaintyasymmetricflag");
                                        if ("Y".equalsIgnoreCase(asymmetricUncertaintyFlag) && (upperUncertaintyFunction = sdidataitems.getValue(rownum[row], "uncertaintyfunctionupper")).length() > 0) {
                                            String upperUncertaintyValue = ExpressionUtil.evaluateSecure(this.connectionInfo, upperUncertaintyFunction, expressionParams);
                                            try {
                                                String uDisplayValueUpper;
                                                try {
                                                    sdidataitems.setNumber(rownum[row], "uncertaintyvalueupper", FormatUtil.getInstance(I18nUtil.getSysLocale()).parseBigDecimal(upperUncertaintyValue));
                                                    BigDecimal uValueUpper = sdidataitems.getBigDecimal(rownum[row], "uncertaintyvalueupper");
                                                    String uDisplayFormatUpper = sdidataitems.getString(rownum[row], "uncertaintydisplayformatupper");
                                                    if (uDisplayFormatUpper.toLowerCase().contains("[eval")) {
                                                        uDisplayFormatUpper = NumericFormatter.getEvaluatedFormat(sdidataitems.getBigDecimal(rownum[row], "transformvalue"), uDisplayFormatUpper);
                                                    }
                                                    uDisplayValueUpper = NumericFormatter.formatNumber(uValueUpper, uDisplayFormatUpper, String.valueOf(uValueUpper));
                                                }
                                                catch (NumberFormatException nfe) {
                                                    sdidataitems.setNumber(rownum[row], "uncertaintyvalueupper", "(null)");
                                                    uDisplayValueUpper = upperUncertaintyValue;
                                                }
                                                sdidataitems.setString(rownum[row], "uncertaintydisplayvalueupper", uDisplayValueUpper);
                                            }
                                            catch (NumberFormatException nfe) {
                                                sdidataitems.setString(rownum[row], "valuestatus", "UPPERUNCERTAINTY_FAILURE");
                                            }
                                        }
                                    }
                                    BigDecimal d = sdidataitems.getBigDecimal(rownum[row], "transformvalue");
                                    String displayformat = sdidataitems.getString(rownum[row], "displayformat");
                                    StringBuilder displayvalue = new StringBuilder(numoperator + NumericFormatter.formatNumber(d, displayformat, numtext));
                                    if (sdidataitems.getString(rownum[row], "enteredqualifier") != null) {
                                        displayvalue.append(sdidataitems.getString(rownum[row], "enteredqualifier"));
                                    }
                                    sdidataitems.setString(rownum[row], "displayvalue", displayvalue.toString());
                                }
                            } else {
                                sdidataitems.setString(rownum[row], "displayvalue", numoperator + limittypeid);
                                sdidataitems.setString(rownum[row], "enteredunits", sdidataitems.getString(rownum[row], "displayunits"));
                                sdidataitems.setString(rownum[row], "transformtext", limittypeid);
                            }
                        }
                    } else if (datatypes.equals("T") || datatypes.equals("TC")) {
                        sdidataitems.setString(rownum[row], "transformtext", entry);
                        sdidataitems.setString(rownum[row], "enteredunits", sdidataitems.getString(rownum[row], "displayunits"));
                        sdidataitems.setString(rownum[row], "displayvalue", entry);
                    } else if (datatypes.equals("R")) {
                        StringHolder refvalueholder = new StringHolder();
                        refvalueholder.value = entry;
                        this.isValidReferenceValue(sdidataitems.getString(rownum[row], "entryreftypeid"), refvalueholder, false);
                        sdidataitems.setString(rownum[row], "transformtext", refvalueholder.value);
                        sdidataitems.setString(rownum[row], "enteredunits", sdidataitems.getString(rownum[row], "displayunits"));
                        sdidataitems.setString(rownum[row], "displayvalue", refvalueholder.value);
                    } else if (datatypes.equals("V")) {
                        sdidataitems.setString(rownum[row], "transformtext", entry);
                        StringHolder refvalueholder = new StringHolder();
                        if (entry.length() > 0) {
                            refvalueholder.value = entry;
                            boolean b = false;
                            if (this.isValidReferenceValue(sdidataitems.getString(rownum[row], "entryreftypeid"), refvalueholder, false)) {
                                b = true;
                                entry = refvalueholder.value;
                            } else {
                                refvalueholder.value = entry;
                                if (this.isValidReferenceValue(sdidataitems.getString(rownum[row], "entryreftypeid"), refvalueholder, true)) {
                                    b = true;
                                    entry = refvalueholder.value;
                                }
                            }
                            if (b) {
                                sdidataitems.setString(rownum[row], "enteredunits", sdidataitems.getString(rownum[row], "displayunits"));
                                sdidataitems.setString(rownum[row], "displayvalue", entry);
                            } else {
                                sdidataitems.setString(rownum[row], "valuestatus", "INVALID_REFVALUE");
                            }
                        }
                    } else if (datatypes.equals("S")) {
                        StringHolder keyid1holder = new StringHolder();
                        keyid1holder.value = entry;
                        boolean b = false;
                        if (entry.length() == 0) {
                            b = true;
                        } else if (this.isValidSDI(sdidataitems.getString(rownum[row], "entrysdcid"), keyid1holder, false)) {
                            b = true;
                            entry = keyid1holder.value;
                        } else {
                            keyid1holder.value = entry;
                            if (this.isValidSDI(sdidataitems.getString(rownum[row], "entrysdcid"), keyid1holder, true)) {
                                b = true;
                                entry = keyid1holder.value;
                            }
                        }
                        if (b) {
                            if (entry.length() > 0) {
                                sdidataitems.setString(rownum[row], "enteredunits", sdidataitems.getString(rownum[row], "displayunits"));
                            }
                            sdidataitems.setString(rownum[row], "displayvalue", entry);
                            sdidataitems.setString(rownum[row], "transformtext", entry);
                        } else {
                            sdidataitems.setString(rownum[row], "valuestatus", "INVALID_SDI");
                        }
                    } else if (datatypes.equals("D") || datatypes.equals("O") || datatypes.equals("DC") || datatypes.equals("OC")) {
                        if (!entry.equals("(null)") && entry.length() > 0) {
                            String format = sdidataitems.getValue(rownum[row], "displayformat");
                            Calendar c = null;
                            if (format.length() > 0) {
                                try {
                                    String javaFormat = DateFormatter.getJavaCompatibleFormatString(format);
                                    SimpleDateFormat sdf = new SimpleDateFormat(javaFormat, m18n.getLocale());
                                    if (!datatypes.equals("O") && !datatypes.equals("OC")) {
                                        sdf.setTimeZone(m18n.getTimezone());
                                    }
                                    sdf.parse(entry);
                                    c = sdf.getCalendar();
                                    sdf = DateTimeUtil.correctTwoYearDigits(sdf, entry);
                                    c = sdf.getCalendar();
                                }
                                catch (ParseException parseException) {
                                    // empty catch block
                                }
                            }
                            if (c == null) {
                                c = m18n.parseCalendar(entry, !datatypes.equals("O") && !datatypes.equals("OC"));
                            }
                            if (c != null) {
                                sdidataitems.setDate(rownum[row], "transformdt", c);
                                if (format.equals("")) {
                                    if (datatypes.equals("O") || datatypes.equals("OC")) {
                                        sdidataitems.setString(rownum[row], "displayvalue", m18n.formatDateOnly(c, false));
                                    } else {
                                        sdidataitems.setString(rownum[row], "displayvalue", m18n.format(c, false));
                                    }
                                } else {
                                    sdidataitems.setString(rownum[row], "displayvalue", DateFormatter.formatDateTime(c, format));
                                }
                            } else {
                                sdidataitems.setString(rownum[row], "valuestatus", "INVALID_DATE_FORMAT");
                            }
                        } else {
                            sdidataitems.setString(rownum[row], "displayvalue", "");
                        }
                    } else {
                        throw new SapphireException("GENERAL_ERROR", "Unrecognized datatype");
                    }
                }
            }
            if (!Trace.on) continue;
            this.logger.info("Key:" + sdidataitems.getString(rownum[row], "keyid1") + " ParamList:" + sdidataitems.getString(rownum[row], "paramlistid") + " Param:" + sdidataitems.getString(rownum[row], "paramid") + " Entered:" + sdidataitems.getString(rownum[row], "enteredtext") + " Qualifier:" + sdidataitems.getString(rownum[row], "enteredqualifier") + " Operator:" + sdidataitems.getString(rownum[row], "enteredoperator") + " EnteredValue:" + sdidataitems.getValue(rownum[row], "enteredvalue") + " EnteredUnits:" + sdidataitems.getString(rownum[row], "enteredunits") + " DisplayValue:" + sdidataitems.getString(rownum[row], "displayvalue") + " DisplayUnits:" + sdidataitems.getString(rownum[row], "displayunits") + " TransformValue:" + sdidataitems.getValue(rownum[row], "transformvalue") + " ValueStatus:" + sdidataitems.getString(rownum[row], "valuestatus"));
        }
        if (hasNewExclude) {
            sdidataitems.setString(-1, "_modified", "Y");
        }
        this.checkLimits(sdidata, sdidataitems, sdidataitemlimits, this.isLiveLimitChecking);
    }

    void appendLinkedDataItems(DataSet sdidataitems, DataSet allLinkedDataItems) {
        ArrayList<DataSet> linkedSDIs = allLinkedDataItems.getGroupedDataSets("sdcid,keyid1,keyid2,keyid3");
        for (DataSet linkedSDI : linkedSDIs) {
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("sdcid", linkedSDI.getString(0, "sdcid"));
            findMap.put("keyid1", linkedSDI.getString(0, "keyid1"));
            findMap.put("keyid2", linkedSDI.getString(0, "keyid2"));
            findMap.put("keyid3", linkedSDI.getString(0, "keyid3"));
            String calcKeySet = linkedSDI.getValue(0, COL_CALCKEYSET);
            if (calcKeySet.length() > 0) {
                findMap.put(COL_CALCKEYSET, linkedSDI.getString(0, COL_CALCKEYSET));
            }
            if (sdidataitems.findRow(findMap) != -1) continue;
            findMap.remove(COL_CALCKEYSET);
            DataSet crossSDIModifiedThisSample = this.crossSDIAllModifiedSDIDataitems.getFilteredDataSet(findMap);
            if (crossSDIModifiedThisSample.size() == 0) {
                crossSDIModifiedThisSample = sdidataitems.getFilteredDataSet(findMap);
            }
            if (crossSDIModifiedThisSample.size() > 0) {
                for (int i = 0; i < linkedSDI.size(); ++i) {
                    int findRow = crossSDIModifiedThisSample.findRow("sdidataitemid", linkedSDI.getString(i, "sdidataitemid"));
                    if (findRow < 0) continue;
                    linkedSDI.setObject(i, "enteredtext", crossSDIModifiedThisSample.getObject(findRow, "enteredtext"));
                    linkedSDI.setObject(i, "enteredqualifier", crossSDIModifiedThisSample.getObject(findRow, "enteredqualifier"));
                    linkedSDI.setObject(i, "enteredoperator", crossSDIModifiedThisSample.getObject(findRow, "enteredoperator"));
                    linkedSDI.setObject(i, "enteredvalue", crossSDIModifiedThisSample.getObject(findRow, "enteredvalue"));
                    linkedSDI.setObject(i, "enteredunits", crossSDIModifiedThisSample.getObject(findRow, "enteredunits"));
                    linkedSDI.setObject(i, "transformvalue", crossSDIModifiedThisSample.getObject(findRow, "transformvalue"));
                    linkedSDI.setObject(i, "transformtext", crossSDIModifiedThisSample.getObject(findRow, "transformtext"));
                    linkedSDI.setObject(i, "transformdt", crossSDIModifiedThisSample.getObject(findRow, "transformdt"));
                    linkedSDI.setObject(i, "displayvalue", crossSDIModifiedThisSample.getObject(findRow, "displayvalue"));
                    linkedSDI.setObject(i, "valuestatus", crossSDIModifiedThisSample.getObject(findRow, "valuestatus"));
                }
            }
            sdidataitems.addAll(linkedSDI);
        }
    }

    void appendLinkedDataItemLimits(DataSet sdidataitemlimits, DataSet allLinkedDataItemsLimits) {
        ArrayList<DataSet> linkedSDIs = allLinkedDataItemsLimits.getGroupedDataSets("sdcid,keyid1,keyid2,keyid3");
        for (DataSet linkedItems : linkedSDIs) {
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("sdcid", linkedItems.getString(0, "sdcid"));
            findMap.put("keyid1", linkedItems.getString(0, "keyid1"));
            findMap.put("keyid2", linkedItems.getString(0, "keyid2"));
            findMap.put("keyid3", linkedItems.getString(0, "keyid3"));
            if (sdidataitemlimits.findRow(findMap) != -1) continue;
            sdidataitemlimits.addAll(linkedItems);
        }
    }

    void appendLinkedDataSet(DataSet sdidata, DataSet allLinkedDataSets) {
        ArrayList<DataSet> linkedSDIs = allLinkedDataSets.getGroupedDataSets("sdcid,keyid1,keyid2,keyid3");
        for (DataSet linkedSDI : linkedSDIs) {
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("sdcid", linkedSDI.getString(0, "sdcid"));
            findMap.put("keyid1", linkedSDI.getString(0, "keyid1"));
            findMap.put("keyid2", linkedSDI.getString(0, "keyid2"));
            findMap.put("keyid3", linkedSDI.getString(0, "keyid3"));
            if (linkedSDI.getValue(0, COL_CALCKEYSET).length() > 0) {
                findMap.put(COL_CALCKEYSET, linkedSDI.getString(0, COL_CALCKEYSET));
            }
            if (sdidata.findRow(findMap) != -1) continue;
            sdidata.addAll(linkedSDI);
        }
    }

    BigDecimal getReplicate(String replicateid, BigDecimal currentReplicateid) {
        if (replicateid.equals("#")) {
            return currentReplicateid;
        }
        if (replicateid.equals("max")) {
            return null;
        }
        if (replicateid.equals("*")) {
            return null;
        }
        return new BigDecimal(replicateid);
    }

    BigDecimal getDataSet(String dataset, BigDecimal currentDataset) {
        if (dataset.equals("#")) {
            return currentDataset;
        }
        if (dataset.equals("max")) {
            return null;
        }
        if (dataset.equals("*")) {
            return null;
        }
        if (dataset.contains("=") || dataset.contains("<>")) {
            return null;
        }
        return new BigDecimal(dataset);
    }

    String getParamId(String paramId, String currentParamId) {
        if (paramId.equals("#")) {
            return currentParamId;
        }
        if (paramId.equals("*")) {
            return null;
        }
        return paramId;
    }

    String getParamType(String paramType, String currentParamType) {
        if (paramType.equals("#")) {
            return currentParamType;
        }
        if (paramType.equals("*")) {
            return null;
        }
        return paramType;
    }

    String getParamlistVersionid(String paramlistVersionid, String currentParamlistVersionid) {
        if (paramlistVersionid.equals("#")) {
            return currentParamlistVersionid;
        }
        if (paramlistVersionid.equals("*")) {
            return null;
        }
        return paramlistVersionid;
    }

    String getVariantid(String variantid, String currentVariantid) {
        if (variantid.equals("#")) {
            return currentVariantid;
        }
        if (variantid.equals("*")) {
            return null;
        }
        return variantid;
    }

    String getParamlistid(String paramlistid, String currentParamlistid) {
        if (paramlistid.equals("#")) {
            return currentParamlistid;
        }
        if (paramlistid.equals("*")) {
            return null;
        }
        return paramlistid;
    }

    protected void checkSpecs(DataSet di, DataSet sdidataitemspec, HashSet specmap) throws SapphireException {
        FormatUtil formatutil = FormatUtil.getInstance(this.connectionInfo);
        DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
        DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
        HashMap<String, Object> filter = new HashMap<String, Object>();
        HashMap<String, DataSet> speclimitstore = new HashMap<String, DataSet>();
        boolean checkAllSpecs = specmap == null || specmap.size() == 0;
        boolean checkAllDataItems = specmap != null;
        di.sort("sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid");
        int dirows = di.getRowCount();
        for (int dirow = 0; dirow < dirows; ++dirow) {
            boolean hasEnteredText;
            filter.clear();
            filter.put("sdcid", di.getObject(dirow, "sdcid"));
            filter.put("keyid1", di.getObject(dirow, "keyid1"));
            filter.put("keyid2", di.getObject(dirow, "keyid2"));
            filter.put("keyid3", di.getObject(dirow, "keyid3"));
            filter.put("paramlistid", di.getObject(dirow, "paramlistid"));
            filter.put("paramlistversionid", di.getObject(dirow, "paramlistversionid"));
            filter.put("variantid", di.getObject(dirow, "variantid"));
            filter.put("dataset", di.getObject(dirow, "dataset"));
            filter.put("paramid", di.getObject(dirow, "paramid"));
            filter.put("paramtype", di.getObject(dirow, "paramtype"));
            filter.put("replicateid", di.getObject(dirow, "replicateid"));
            boolean modified = "Y".equals(di.getString(dirow, "_modifiedtotal"));
            boolean bl = hasEnteredText = di.getValue(dirow, "enteredtext").length() > 0;
            if (!checkAllDataItems && !modified && !hasEnteredText) continue;
            DataSet dis = sdidataitemspec.getFilteredDataSet(filter);
            dis.sort("specid, specversionid");
            int rows = dis.getRowCount();
            for (int row = 0; row < rows; ++row) {
                int limit;
                String newvalue;
                boolean checkItem;
                String specid = dis.getString(row, "specid");
                String specversionid = dis.getString(row, "specversionid");
                String condition = dis.getString(row, "condition");
                boolean bl2 = checkItem = checkAllDataItems || modified;
                if (!checkItem) {
                    boolean bl3 = checkItem = condition == null || condition.length() == 0;
                }
                if (!checkItem || !checkAllSpecs && !specmap.contains(specid + ";" + specversionid)) continue;
                DataSet speclimits = (DataSet)speclimitstore.get(specid + ";" + specversionid);
                if (speclimits == null) {
                    if (Trace.on) {
                        this.logger.info("Loading spec definition for " + specid + " v " + specversionid);
                    }
                    String sql = "SELECT\tspec.defaultcondition, specparamlimits.paramlistid, \t\tspecparamlimits.paramlistversionid, \t\tspecparamlimits.variantid, \t\tspecparamlimits.paramid, \t\tspecparamlimits.paramtype, \t\tspecparamitems.allowanyparamlistflag, \t\tspecparamitems.displayformat, \t\tspecparamitems.limittyperule, \tspecparamitems.transformrule, \tspecparamlimits.value1, \t\tspecparamlimits.value1num, \t\tspecparamlimits.operator1, \t\tspecparamlimits.value2, \t\tspecparamlimits.value2num, \t\tspecparamlimits.operator2, \t\tspecparamitems.unitsid, \t\tspecparamlimits.limittypesequence, \t\tspeclimittype.limittypeid, speclimittype.condition  FROM\tspec, speclimittype, \t\tspecparamitems, \t\tspecparamlimits WHERE\tspec.specid = specparamlimits.specid  and spec.specversionid = specparamlimits.specversionid and specparamlimits.specid = specparamitems.specid and \t\tspecparamlimits.specversionid = specparamitems.specversionid and \t\tspecparamlimits.paramlistid = specparamitems.paramlistid and \t\tspecparamlimits.paramlistversionid = specparamitems.paramlistversionid and \t\tspecparamlimits.variantid = specparamitems.variantid and \t\tspecparamlimits.paramid = specparamitems.paramid and \t\tspecparamlimits.paramtype = specparamitems.paramtype and \t\tspeclimittype.specid = specparamlimits.specid and \t\tspeclimittype.specversionid = specparamlimits.specversionid and \t\tspecparamlimits.limittypesequence = speclimittype.limittypesequence";
                    sql = sql + " and spec.specid=? and spec.specversionid=?  order by speclimittype.usersequence";
                    this.database.createPreparedResultSet("loadspeclimits", sql, new Object[]{specid, specversionid});
                    speclimits = new DataSet(this.database.getResultSet("loadspeclimits"));
                    speclimitstore.put(specid + ";" + specversionid, speclimits);
                }
                DataSet limitds = new DataSet();
                for (int secrow = 0; secrow < speclimits.getRowCount(); ++secrow) {
                    String currparam = speclimits.getString(secrow, "paramid", "");
                    String currparamtype = speclimits.getString(secrow, "paramtype", "");
                    if (currparam.length() <= 0 || currparamtype.length() <= 0 || !currparam.equals(dis.getValue(row, "paramid", "")) || !currparamtype.equals(dis.getValue(row, "paramtype", ""))) continue;
                    String currentany = speclimits.getString(secrow, "allowanyparamlistflag", "N");
                    if (currentany.equalsIgnoreCase("Y")) {
                        limitds.copyRow(speclimits, secrow, 1);
                        continue;
                    }
                    String currparamlistid = speclimits.getString(secrow, "paramlistid", "");
                    String currvariant = speclimits.getString(secrow, "variantid", "");
                    String currparamlistversion = speclimits.getString(secrow, "paramlistversionid", "");
                    if (currentany.equalsIgnoreCase("V")) {
                        if (currparamlistid.length() <= 0 || currvariant.length() <= 0 || !currparamlistid.equals(dis.getValue(row, "paramlistid", "")) || !currvariant.equals(dis.getValue(row, "variantid", ""))) continue;
                        limitds.copyRow(speclimits, secrow, 1);
                        continue;
                    }
                    if (currentany.equalsIgnoreCase("A")) {
                        if (currparamlistid.length() <= 0 || !currparamlistid.equals(dis.getValue(row, "paramlistid", ""))) continue;
                        limitds.copyRow(speclimits, secrow, 1);
                        continue;
                    }
                    if (currparamlistid.length() <= 0 || currparamlistversion.length() <= 0 || currvariant.length() <= 0 || !currparamlistid.equals(dis.getValue(row, "paramlistid", "")) || !currparamlistversion.equals(dis.getValue(row, "paramlistversionid", "")) || !currvariant.equals(dis.getValue(row, "variantid", ""))) continue;
                    limitds.copyRow(speclimits, secrow, 1);
                }
                int limits = limitds.getRowCount();
                boolean limitmet = false;
                int undeterminedLimitCount = 0;
                PropertyList policy = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom");
                PropertyList specLimitRulePL = policy.getPropertyList("undeterminedspeclimitrule");
                String honorEnteredOperator = specLimitRulePL.getProperty("honorenteredoperator");
                String oldvalue = dis.getValue(row, "displayvalue");
                if (!oldvalue.equals(newvalue = di.getValue(dirow, "displayvalue"))) {
                    dis.setString(row, "displayvalue", di.getString(dirow, "displayvalue"));
                    dis.setValue(row, "_modifiedtotal", "Y");
                }
                BigDecimal comparevalue = null;
                String enteredtext = null;
                boolean isNonNumericTag = false;
                VirtualLimit virtualLimit = new VirtualLimit();
                String datatypes = di.getString(dirow, "datatypes");
                boolean allNumericLimitsEmpty = true;
                int countNumericLimits = 0;
                for (limit = 0; limit < limits; ++limit) {
                    if (!datatypes.equals("N") && !datatypes.equals("NC")) continue;
                    ++countNumericLimits;
                    comparevalue = di.getBigDecimal(dirow, "transformvalue");
                    String operator1 = limitds.getValue(limit, "operator1");
                    String operator2 = limitds.getValue(limit, "operator2");
                    if (operator1.length() <= 0 && operator2.length() <= 0) continue;
                    allNumericLimitsEmpty = false;
                }
                for (limit = 0; limit < limits; ++limit) {
                    if (StringUtil.getLen(di.getString(dirow, "enteredtext")) > 0L) {
                        String displayformat;
                        String stringvalue;
                        enteredtext = di.getString(dirow, "enteredtext");
                        if (datatypes.equals("A") && (datatypes = di.getValue(dirow, COL_APPARENTDATATYPE)).length() == 0) {
                            datatypes = DataEntryLimitsUtil.getApparentDatatype(enteredtext, dtu, formatutil);
                        }
                        if (datatypes.equals("N") || datatypes.equals("NC")) {
                            comparevalue = di.getBigDecimal(dirow, "transformvalue");
                            String limittyperule = limitds.getValue(limit, "limittyperule", "'");
                            if (limittyperule.length() > 0 && limittyperule.contains(di.getString(dirow, "transformtext", ""))) {
                                String[] limitTypeRulesArr;
                                for (String s : limitTypeRulesArr = StringUtil.split(limittyperule, ";")) {
                                    String limittype = s.split("=")[0];
                                    if (!limittype.equalsIgnoreCase(di.getString(dirow, "transformtext"))) continue;
                                    isNonNumericTag = true;
                                    dis.setValue(row, "checkedtext", limittype);
                                    dis.setNumber(row, "checkedvalue", "");
                                    dis.setValue(row, "condition", s.split("=")[1]);
                                    dis.setString(row, "limittypeid", "");
                                    dis.setString(row, "waivedflag", "N");
                                    dis.setString(row, "_modifiedtotal", "Y");
                                    dis.setValue(row, "transformvalue", null);
                                    dis.setString(row, "displayvalue", limittype);
                                }
                            }
                            if (comparevalue != null && !isNonNumericTag) {
                                String diunit = di.getString(dirow, "displayunits");
                                String specunit = limitds.getString(limit, "unitsid");
                                if (StringUtil.getLen(specunit) > 0L && StringUtil.getLen(diunit) > 0L && !diunit.equalsIgnoreCase(specunit)) {
                                    StringHolder unitholder = new StringHolder();
                                    unitholder.value = diunit;
                                    if ((comparevalue = limitsUtil.convertUnits(unitholder, specunit, comparevalue)) == null) {
                                        HashMap<String, String> token = new HashMap<String, String>();
                                        token.put("diunit", diunit);
                                        token.put("specunit", specunit);
                                        throw new SapphireException(this.getTranslationProcessor().translate("Unit conversion rule not specified from [diunit] to [specunit]", token));
                                    }
                                }
                                String operator1 = limitds.getString(limit, "operator1");
                                String operator2 = limitds.getString(limit, "operator2");
                                String transrule = limitds.getString(limit, "transformrule", "");
                                if (transrule.length() > 0) {
                                    HashMap<String, BigDecimal> expressionParams = new HashMap<String, BigDecimal>();
                                    expressionParams.put("this", comparevalue);
                                    try {
                                        String result = ExpressionUtil.evaluateSecure(this.connectionInfo, transrule, expressionParams);
                                        dis.setNumber(row, "transformvalue", FormatUtil.getInstance(I18nUtil.getSysLocale()).parseBigDecimal(result));
                                        comparevalue = FormatUtil.getInstance(I18nUtil.getSysLocale()).parseBigDecimal(result);
                                    }
                                    catch (SapphireException e) {
                                        this.logger.error("Failed to evaluate the spec transform rule " + transrule, e);
                                        dis.setString(row, "valuestatus", "TRANSFORM_FAILURE");
                                    }
                                }
                                virtualLimit.addLimit(comparevalue, limitds.getValue(limit, "operator1", ""), limitds.getValue(limit, "value1num", limitds.getValue(limit, "value1", "")), limitds.getValue(limit, "operator2", ""), limitds.getValue(limit, "value2num", limitds.getValue(limit, "value2", "")), honorEnteredOperator.equals("Y") ? di.getString(dirow, "enteredoperator", "") : "");
                                if (honorEnteredOperator.equals("Y") && di.getString(dirow, "enteredoperator", "").length() > 0) {
                                    boolean undeterminedLimit = limitsUtil.isLimitUndetermined(comparevalue, operator1, limitsUtil.getLimitValue(datatypes, limitds.getValue(limit, "value1", ""), limitds.getValue(limit, "value1num", "")), "", datatypes, di.getString(dirow, "enteredoperator"));
                                    if (!undeterminedLimit && StringUtil.getLen(operator2) > 0L) {
                                        undeterminedLimit = limitsUtil.isLimitUndetermined(comparevalue, operator2, limitsUtil.getLimitValue(datatypes, limitds.getValue(limit, "value2", ""), limitds.getValue(limit, "value2num", "")), "", datatypes, di.getString(dirow, "enteredoperator"));
                                    }
                                    if (!undeterminedLimit && (limitmet = limitsUtil.isLimitMet(comparevalue, operator1, limitsUtil.getLimitValue(datatypes, limitds.getValue(limit, "value1", ""), limitds.getValue(limit, "value1num", "")), "", datatypes, "", di.getString(dirow, "enteredoperator"))) && StringUtil.getLen(operator2) > 0L) {
                                        limitmet = limitsUtil.isLimitMet(comparevalue, operator2, limitsUtil.getLimitValue(datatypes, limitds.getValue(limit, "value2", ""), limitds.getValue(limit, "value2num", "")), "", datatypes, "", di.getString(dirow, "enteredoperator"));
                                    }
                                    if (undeterminedLimit) {
                                        ++undeterminedLimitCount;
                                    }
                                    dis.setNumber(row, "checkedvalue", comparevalue);
                                    dis.setValue(row, "checkedtext", "");
                                } else {
                                    limitmet = limitsUtil.isLimitMet(comparevalue, operator1, limitsUtil.getLimitValue(datatypes, limitds.getValue(limit, "value1", ""), limitds.getValue(limit, "value1num", "")), "", datatypes, "", "");
                                    if (limitmet && StringUtil.getLen(operator2) > 0L) {
                                        limitmet = limitsUtil.isLimitMet(comparevalue, operator2, limitsUtil.getLimitValue(datatypes, limitds.getValue(limit, "value2", ""), limitds.getValue(limit, "value2num", "")), "", datatypes, "", "");
                                    }
                                    dis.setNumber(row, "checkedvalue", comparevalue);
                                    dis.setValue(row, "checkedtext", "");
                                }
                            }
                        } else if ((datatypes.equals("T") || datatypes.equals("TC") || datatypes.equals("R") || datatypes.equals("V") || datatypes.equals("S")) && (stringvalue = di.getString(dirow, "transformtext")) != null && stringvalue.length() > 0) {
                            String extrastuff = null;
                            if (datatypes.equals("R") || datatypes.equals("V")) {
                                extrastuff = di.getString(dirow, "entryreftypeid");
                            } else if (datatypes.equals("S")) {
                                extrastuff = di.getString(dirow, "entrysdcid");
                            }
                            limitmet = limitsUtil.isLimitMet(stringvalue, limitds.getString(limit, "operator1"), limitsUtil.getLimitValue(datatypes, limitds.getValue(limit, "value1", ""), limitds.getValue(limit, "value1num", "")), "", datatypes, extrastuff, di.getString(dirow, "enteredoperator"));
                            dis.setString(row, "checkedtext", stringvalue);
                        }
                        if ((countNumericLimits <= 0 || !allNumericLimitsEmpty) && !limitmet) continue;
                        if (limitmet) {
                            if (Trace.on) {
                                this.logger.info("Limit met - setting condition to " + limitds.getString(limit, "condition"));
                            }
                            dis.setObject(row, "condition", limitds.getObject(limit, "condition"));
                            dis.setString(row, "waivedflag", "N");
                            dis.setString(row, "limittypeid", limitds.getString(limit, "limittypeid"));
                            dis.setString(row, "_modifiedtotal", "Y");
                        }
                        if ((displayformat = limitds.getValue(limit, "displayformat")).length() <= 0 || comparevalue == null) break;
                        String enteredOperator = di.getString(dirow, "enteredoperator", "");
                        String displayvalue = NumericFormatter.formatNumber(comparevalue, displayformat, I18nUtil.getUnitTrimmedEnteredText(enteredtext, formatutil));
                        if (!displayformat.equals("AsEntered") && enteredOperator.length() > 0) {
                            displayvalue = enteredOperator + displayvalue;
                        }
                        String currentDisplayValue = dis.getValue(row, "displayvalue");
                        String currentDisplayValueFormat = dis.getValue(row, "displayvalueformat");
                        if (currentDisplayValue.equals(displayvalue) && currentDisplayValueFormat.equals(displayformat)) break;
                        dis.setString(row, "displayvalue", displayvalue);
                        dis.setString(row, "displayvalueformat", displayformat);
                        dis.setString(row, "_modifiedtotal", "Y");
                        break;
                    }
                    if (condition == null || condition.length() <= 0) continue;
                    dis.setString(row, "condition", null);
                    dis.setString(row, "_modifiedtotal", "Y");
                }
                if (datatypes.equals("N") || datatypes.equals("NC")) {
                    VirtualLimit.VirtualLimitRange virtualLimitRange = virtualLimit.getLimitRange();
                    if (virtualLimitRange != null) {
                        Trace.log("Found virtual limit range:" + virtualLimitRange.operator1 + virtualLimitRange.value1 + "-" + virtualLimitRange.operator2 + virtualLimitRange.value2);
                        if (!limitmet) {
                            FormatUtil formatUtil = FormatUtil.getInstance();
                            String stringvalue1 = virtualLimitRange.value1 == null ? "" : formatUtil.format(virtualLimitRange.value1);
                            String stringvalue2 = virtualLimitRange.value2 == null ? "" : formatUtil.format(virtualLimitRange.value2);
                            boolean[] checkresults = this.checkUndeterminedOrLimitMet(limitsUtil, datatypes, comparevalue, honorEnteredOperator.equalsIgnoreCase("Y") ? di.getString(dirow, "enteredoperator") : "", virtualLimitRange.operator1, stringvalue1, virtualLimitRange.operator2, stringvalue2);
                            boolean undeterminedLimit = checkresults[0];
                            limitmet = checkresults[1];
                            if (limitmet) {
                                String defaultLimitTypeId = "";
                                String defaultCondition = "";
                                if (StringUtil.getLen(di.getString(dirow, "enteredtext")) > 0L) {
                                    defaultCondition = speclimits.getValue(0, "defaultcondition", "");
                                }
                                if (Trace.on) {
                                    this.logger.info("Limit met - setting condition to " + defaultCondition);
                                }
                                dis.setString(row, "condition", defaultCondition);
                                dis.setString(row, "waivedflag", "N");
                                dis.setString(row, "_modifiedtotal", "Y");
                                dis.setString(row, "limittypeid", defaultLimitTypeId);
                                String displayformat = limitds.getValue(0, "displayformat");
                                if (displayformat.length() > 0 && comparevalue != null) {
                                    String enteredOperator = di.getString(dirow, "enteredoperator", "");
                                    String displayvalue = NumericFormatter.formatNumber(comparevalue, displayformat, I18nUtil.getUnitTrimmedEnteredText(enteredtext, formatutil));
                                    if (!displayformat.equals("AsEntered") && enteredOperator.length() > 0) {
                                        displayvalue = enteredOperator + displayvalue;
                                    }
                                    dis.setString(row, "displayvalue", displayvalue);
                                    dis.setString(row, "displayvalueformat", displayformat);
                                }
                            }
                            if (undeterminedLimit) {
                                ++undeterminedLimitCount;
                            }
                        }
                    } else {
                        Trace.log("No gaps found, hence it is safe to assume that the defaultcondition scenario does not arise");
                    }
                    if (limitmet || undeterminedLimitCount <= 0 || isNonNumericTag) continue;
                    String limitTypeId = "";
                    String undeterminedCondition = specLimitRulePL.getProperty("usecondition");
                    if (Trace.on) {
                        this.logger.info("Limit is undetermined - setting condition to " + undeterminedCondition);
                    }
                    dis.setObject(row, "condition", undeterminedCondition);
                    dis.setString(row, "waivedflag", "N");
                    dis.setString(row, "_modifiedtotal", "Y");
                    dis.setString(row, "limittypeid", limitTypeId);
                    String displayformat = limitds.getValue(0, "displayformat");
                    if (displayformat.length() <= 0 || comparevalue == null) continue;
                    String enteredOperator = di.getString(dirow, "enteredoperator", "");
                    String displayvalue = NumericFormatter.formatNumber(comparevalue, displayformat, I18nUtil.getUnitTrimmedEnteredText(enteredtext, formatutil));
                    if (!displayformat.equals("AsEntered") && enteredOperator.length() > 0) {
                        displayvalue = enteredOperator + displayvalue;
                    }
                    dis.setString(row, "displayvalue", displayvalue);
                    dis.setString(row, "displayvalueformat", displayformat);
                    continue;
                }
                if (limitmet) continue;
                String defaultLimitTypeId = "";
                String defaultCondition = "";
                if (StringUtil.getLen(di.getString(dirow, "enteredtext")) > 0L) {
                    defaultCondition = speclimits.getValue(0, "defaultcondition", "");
                }
                if (Trace.on) {
                    this.logger.info("Limit met - setting condition to " + defaultCondition);
                }
                dis.setString(row, "condition", defaultCondition);
                dis.setString(row, "waivedflag", "N");
                dis.setString(row, "_modifiedtotal", "Y");
                dis.setString(row, "limittypeid", defaultLimitTypeId);
            }
        }
    }

    private void processLimitRuleAction(String limitruleid, String limitruleversionid, String limittypeid, String limittypeStatus, String actionid, String actionversionid, DataSet dsDataItem, int dataitemRow) throws SapphireException {
        if (actionid.length() > 0) {
            PropertyList props = new PropertyList();
            if (!"(null)".equals(limittypeid)) {
                props.setProperty("limitid", limittypeid);
            }
            props.setProperty("sdcid", dsDataItem.getValue(dataitemRow, "sdcid"));
            props.setProperty("keyid1", dsDataItem.getValue(dataitemRow, "keyid1"));
            props.setProperty("keyid2", dsDataItem.getValue(dataitemRow, "keyid2"));
            props.setProperty("keyid3", dsDataItem.getValue(dataitemRow, "keyid3"));
            props.setProperty("paramlistid", dsDataItem.getValue(dataitemRow, "paramlistid"));
            props.setProperty("paramlistversionid", dsDataItem.getValue(dataitemRow, "paramlistversionid"));
            props.setProperty("variantid", dsDataItem.getValue(dataitemRow, "variantid"));
            props.setProperty("dataset", dsDataItem.getValue(dataitemRow, "dataset"));
            props.setProperty("paramid", dsDataItem.getValue(dataitemRow, "paramid"));
            props.setProperty("paramtype", dsDataItem.getValue(dataitemRow, "paramtype"));
            props.setProperty("replicateid", dsDataItem.getValue(dataitemRow, "replicateid"));
            String limitRulePropsSql = "SELECT propertyid, propertyvalue FROM limitruleproperty WHERE limitruleid=? AND limitruleversionid=? AND limittypeid=? AND limittypestatus=?";
            this.database.createPreparedResultSet("limitruleprops", limitRulePropsSql, new Object[]{limitruleid, limitruleversionid, limittypeid, limittypeStatus});
            while (this.database.getNext("limitruleprops")) {
                props.setProperty(this.database.getString("limitruleprops", "propertyid"), this.database.getString("limitruleprops", "propertyvalue"));
            }
            try {
                ActionProcessor ap = this.getActionProcessor();
                ap.processAction(actionid, actionversionid, props);
            }
            catch (ActionException e) {
                throw new SapphireException("ERROR", "Limit rule action failed to execute", e);
            }
        }
    }

    private boolean isValidLimit(int row, DataSet sdidataitem, DataSet sdidataitemlimits, StringHolder limittypeidholder, HashMap sdidataitemlimitsMap) {
        boolean isValidLimit = false;
        HashMap<String, Object> filter = new HashMap<String, Object>();
        String sdcid = sdidataitem.getString(row, "sdcid");
        String keyid1 = sdidataitem.getString(row, "keyid1");
        String keyid2 = sdidataitem.getString(row, "keyid2");
        String keyid3 = sdidataitem.getString(row, "keyid3");
        filter.put("sdcid", sdcid);
        filter.put("keyid1", keyid1);
        filter.put("keyid2", keyid2);
        filter.put("keyid3", keyid3);
        DataSet filtsdidil = (DataSet)sdidataitemlimitsMap.get(sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
        if (filtsdidil == null) {
            filtsdidil = sdidataitemlimits.getFilteredDataSet(filter);
            sdidataitemlimitsMap.put(sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3, filtsdidil);
        }
        filter.put("paramlistid", sdidataitem.getString(row, "paramlistid"));
        filter.put("paramlistversionid", sdidataitem.getString(row, "paramlistversionid"));
        filter.put("variantid", sdidataitem.getString(row, "variantid"));
        filter.put("dataset", sdidataitem.getBigDecimal(row, "dataset"));
        filter.put("paramid", sdidataitem.getString(row, "paramid"));
        filter.put("paramtype", sdidataitem.getString(row, "paramtype"));
        filter.put("replicateid", sdidataitem.getBigDecimal(row, "replicateid"));
        DataSet limittypes = filtsdidil.getFilteredDataSet(filter);
        for (int i = 0; i < limittypes.size() && !isValidLimit; ++i) {
            if (!limittypes.getString(i, "limittypeid").equalsIgnoreCase(limittypeidholder.value)) continue;
            limittypeidholder.value = limittypes.getString(i, "limittypeid");
            isValidLimit = true;
        }
        return isValidLimit;
    }

    private boolean isValidUnits(StringHolder unitsidholder) throws SapphireException {
        boolean isValid = false;
        this.database.createPreparedResultSet("isvalidunits", "SELECT unitsid FROM units WHERE lower( unitsid ) = ?", new Object[]{unitsidholder.value.toLowerCase()});
        if (this.database.getNext("isvalidunits")) {
            unitsidholder.value = this.database.getString("isvalidunits", "unitsid");
            isValid = true;
        }
        return isValid;
    }

    private boolean isValidReferenceValue(String reftypeid, StringHolder refvalueidholder, boolean partial) throws SapphireException {
        String sql;
        Object[] bindvars;
        boolean isValid = false;
        if (partial) {
            bindvars = new String[]{reftypeid, refvalueidholder.value + "%"};
            sql = "SELECT refvalueid FROM refvalue WHERE ( activeflag='Y' OR activeflag is null) AND reftypeid = ? and refvalueid like ?";
        } else {
            bindvars = new String[]{reftypeid, refvalueidholder.value};
            sql = "SELECT refvalueid FROM refvalue WHERE ( activeflag='Y' OR activeflag is null) AND reftypeid = ? and refvalueid = ?";
        }
        this.database.createPreparedResultSet("isvalidreference", sql, bindvars);
        if (this.database.getNext("isvalidreference")) {
            refvalueidholder.value = this.database.getString("isvalidreference", "refvalueid");
            if (Trace.on) {
                this.logger.info("REFTYPE:" + refvalueidholder.value);
            }
            isValid = true;
        } else {
            if (partial) {
                bindvars = new String[]{reftypeid, refvalueidholder.value.toLowerCase() + "%"};
                sql = "SELECT refvalueid FROM refvalue WHERE ( activeflag='Y' OR activeflag is null) AND reftypeid = ? and lower( refvalueid ) like ?";
            } else {
                bindvars = new String[]{reftypeid, refvalueidholder.value.toLowerCase()};
                sql = "SELECT refvalueid FROM refvalue WHERE ( activeflag='Y' OR activeflag is null) AND reftypeid = ? and lower( refvalueid ) = ?";
            }
            this.database.createPreparedResultSet("isvalidreference", sql, bindvars);
            if (this.database.getNext("isvalidreference")) {
                refvalueidholder.value = this.database.getString("isvalidreference", "refvalueid");
                if (Trace.on) {
                    this.logger.info("REFTYPE:" + refvalueidholder.value);
                }
                isValid = true;
            }
        }
        return isValid;
    }

    private boolean isValidSDI(String sdcid, StringHolder keyid1holder, boolean partial) throws SapphireException {
        boolean isValid = false;
        if (StringUtil.getLen(sdcid) > 0L) {
            String sql;
            Object[] bindvars;
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            PropertyList sdc = sdcProcessor.getProperties(sdcid);
            if (partial) {
                bindvars = new String[]{keyid1holder.value + "%"};
                sql = "SELECT " + sdc.getProperty("keycolid1") + " FROM " + sdc.getProperty("tableid") + " WHERE " + sdc.getProperty("keycolid1") + " like ?";
            } else {
                bindvars = new String[]{keyid1holder.value};
                sql = "SELECT " + sdc.getProperty("keycolid1") + " FROM " + sdc.getProperty("tableid") + " WHERE " + sdc.getProperty("keycolid1") + " = ?";
            }
            this.database.createPreparedResultSet("isvalidsdi", sql, bindvars);
            if (this.database.getNext("isvalidsdi")) {
                keyid1holder.value = this.database.getString("isvalidsdi", sdc.getProperty("keycolid1"));
                if (Trace.on) {
                    this.logger.info("KEYID:" + keyid1holder.value);
                }
                isValid = true;
            } else {
                if (partial) {
                    bindvars = new String[]{keyid1holder.value.toLowerCase() + "%"};
                    sql = "SELECT " + sdc.getProperty("keycolid1") + " FROM " + sdc.getProperty("tableid") + " WHERE lower(" + sdc.getProperty("keycolid1") + ") like ?";
                } else {
                    bindvars = new String[]{keyid1holder.value.toLowerCase()};
                    sql = "SELECT " + sdc.getProperty("keycolid1") + " FROM " + sdc.getProperty("tableid") + " WHERE lower(" + sdc.getProperty("keycolid1") + ") = ?";
                }
                this.database.createPreparedResultSet("isvalidsdi", sql, bindvars);
                if (this.database.getNext("isvalidsdi")) {
                    keyid1holder.value = this.database.getString("isvalidsdi", sdc.getProperty("keycolid1"));
                    if (Trace.on) {
                        this.logger.info("KEYID:" + keyid1holder.value);
                    }
                    isValid = true;
                }
            }
        }
        return isValid;
    }

    public void processInstruments(DataSet sdidata, DataSet sdidataTrackedEquipment, DataSet sdidataitems) throws SapphireException {
        HashSet<String> modifiedDataSets = new HashSet<String>();
        int rows = sdidataitems.getRowCount();
        for (int row = 0; row < rows; ++row) {
            if (!sdidataitems.getValue(row, "_modified").equals("Y")) continue;
            modifiedDataSets.add(sdidataitems.getString(row, "sdcid") + ";" + sdidataitems.getString(row, "keyid1") + ";" + sdidataitems.getString(row, "keyid2") + ";" + sdidataitems.getString(row, "keyid3") + ";" + sdidataitems.getString(row, "paramlistid") + ";" + sdidataitems.getString(row, "paramlistversionid") + ";" + sdidataitems.getString(row, "variantid") + ";" + sdidataitems.getBigDecimal(row, "dataset"));
        }
        StringBuilder instrumentBuffer = new StringBuilder();
        StringBuilder paramlistidBuffer = new StringBuilder();
        StringBuilder paramlistversionidBuffer = new StringBuilder();
        StringBuilder variantidBuffer = new StringBuilder();
        HashMap<String, Object> findmap = new HashMap<String, Object>();
        for (int i = 0; i < sdidata.getRowCount(); ++i) {
            String sdcid = sdidata.getString(i, "sdcid");
            String keyid1 = sdidata.getString(i, "keyid1");
            String keyid2 = sdidata.getString(i, "keyid2");
            String keyid3 = sdidata.getString(i, "keyid3");
            String paramlistid = sdidata.getString(i, "paramlistid");
            String paramlistversionid = sdidata.getString(i, "paramlistversionid");
            String variantid = sdidata.getString(i, "variantid");
            BigDecimal dataset = sdidata.getBigDecimal(i, "dataset");
            String s_instrumentUsedFlag = sdidata.getValue(i, "s_instrumentusedflag", "");
            if (s_instrumentUsedFlag.equalsIgnoreCase("Y") || !modifiedDataSets.contains(sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + paramlistid + ";" + paramlistversionid + ";" + variantid + ";" + dataset)) continue;
            boolean counted = false;
            String instrument = sdidata.getValue(i, "s_instrumentid", "");
            if (instrument.length() > 0) {
                instrumentBuffer.append(";").append(instrument);
                paramlistidBuffer.append(";").append(paramlistid);
                paramlistversionidBuffer.append(";").append(paramlistversionid);
                variantidBuffer.append(";").append(variantid);
                counted = true;
            }
            if (sdidataTrackedEquipment != null && sdidataTrackedEquipment.getRowCount() > 0) {
                findmap.clear();
                findmap.put("sdcid", sdcid);
                findmap.put("keyid1", keyid1);
                findmap.put("keyid2", keyid2);
                findmap.put("keyid3", keyid3);
                findmap.put("paramlistid", paramlistid);
                findmap.put("paramlistversionid", paramlistversionid);
                findmap.put("variantid", variantid);
                findmap.put("dataset", dataset);
                DataSet thisDataSetsEquipments = sdidataTrackedEquipment.getFilteredDataSet(findmap);
                for (int j = 0; j < thisDataSetsEquipments.getRowCount(); ++j) {
                    instrumentBuffer.append(";").append(thisDataSetsEquipments.getString(j, "tokeyid1"));
                    paramlistidBuffer.append(";").append(paramlistid);
                    paramlistversionidBuffer.append(";").append(paramlistversionid);
                    variantidBuffer.append(";").append(variantid);
                    counted = true;
                }
            }
            if (!counted) continue;
            sdidata.setValue(i, "s_instrumentusedflag", "Y");
            sdidata.setValue(i, "_modifiedtotal", "Y");
        }
        if (instrumentBuffer.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("instrumentid", instrumentBuffer.substring(1));
            props.setProperty("paramlistid", paramlistidBuffer.substring(1));
            props.setProperty("versionid", paramlistversionidBuffer.substring(1));
            props.setProperty("variant", variantidBuffer.substring(1));
            props.setProperty("usagetype", "DataSet DataEntry");
            props.setProperty("actionid", "InstrumentUsed");
            props.setProperty("actionversionid", "1");
            props.setProperty("processassysuserid", this.connectionInfo.getSysuserId());
            props.setProperty("delete", "Y");
            this.getActionProcessor().processAction("AddToDoListEntry", "1", props);
        }
    }

    private void checkArrayLimits(String arrayid, DataSet sdidataitems) throws SapphireException {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("sdcid", "LV_ArrayZone");
        DataSet zoneDataItems = sdidataitems.getFilteredDataSet(filter);
        filter.put("sdcid", "LV_ArrayItem");
        DataSet arrayItemDataItems = sdidataitems.getFilteredDataSet(filter);
        if (zoneDataItems != null && zoneDataItems.getRowCount() > 0) {
            this.checkLimitsForArrayZone(arrayid, zoneDataItems);
        }
        if (arrayItemDataItems != null && arrayItemDataItems.getRowCount() > 0) {
            this.checkLimitsForArrayItem(arrayid, arrayItemDataItems);
        }
    }

    private void checkLimitsForArrayZone(String arrayid, DataSet di) throws SapphireException {
        SafeSQL safeSql = new SafeSQL();
        String sql = "SELECT arrayid, arrayzoneid, itemid, paramid, paramtype, operator, value1num, value2num, value1, value2, unitsid FROM arrayparamitem WHERE arrayparamitem.arrayid in(" + safeSql.addIn(arrayid, ";") + ") AND operator is not null AND levelflag = 'Z'";
        this.database.createPreparedResultSet("arrayparamitems", sql, safeSql.getValues());
        DataSet arrayparamitems = new DataSet(this.database.getResultSet("arrayparamitems"));
        if (arrayparamitems.getRowCount() > 0) {
            FormatUtil formatutil = FormatUtil.getInstance(this.connectionInfo);
            DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
            for (int row = 0; row < di.getRowCount(); ++row) {
                String operator;
                String currparam = di.getString(row, "paramid");
                String currparamtype = di.getString(row, "paramtype");
                String arrzone = di.getString(row, "keyid1", "");
                HashMap<String, String> find = new HashMap<String, String>();
                find.put("paramid", currparam);
                find.put("paramtype", currparamtype);
                find.put("arrayzoneid", arrzone);
                int match = arrayparamitems.findRow(find);
                if (match <= -1 || (operator = arrayparamitems.getString(match, "operator")) == null || operator.trim().length() <= 0) continue;
                String value1 = arrayparamitems.getValue(match, "value1", "");
                String value2 = arrayparamitems.getValue(match, "value2", "");
                String value1num = arrayparamitems.getValue(match, "value1num", "");
                String value2num = arrayparamitems.getValue(match, "value2num", "");
                String limitunit = arrayparamitems.getString(match, "unitsid");
                this.checkDataItemLimits(row, di, operator, value1, value1num, value2, value2num, limitunit, limitsUtil, formatutil);
            }
        }
    }

    private void checkLimitsForArrayItem(String arrayIds, DataSet di) throws SapphireException {
        String[] arrays;
        for (String arrayId : arrays = StringUtil.split(arrayIds, ";")) {
            String arrayitemzonessql = "SELECT arrayitemid, arrayzoneid FROM arrayitemarrayzone WHERE arrayitemid LIKE ?";
            this.database.createPreparedResultSet("arrayitemzone", arrayitemzonessql, new Object[]{arrayId + "%"});
            DataSet arrayitemarrayzones = new DataSet(this.database.getResultSet("arrayitemzone"));
            this.database.createPreparedResultSet("arrayitemarrayzone", "SELECT arrayid, arrayzoneid, itemid, paramid, paramtype, operator, value1num, value2num, unitsid FROM arrayparamitem WHERE arrayparamitem.arrayid = ? AND levelflag='I' AND arrayparamitem.operator is not null", new Object[]{arrayId});
            DataSet arrayparamitems = new DataSet(this.database.getResultSet("arrayitemarrayzone"));
            if (arrayparamitems.getRowCount() <= 0) continue;
            FormatUtil formatutil = FormatUtil.getInstance(this.connectionInfo);
            DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
            for (int row = 0; row < di.getRowCount(); ++row) {
                String currparam = di.getString(row, "paramid");
                String currparamtype = di.getString(row, "paramtype");
                String arritem = di.getString(row, "keyid1", "");
                HashMap<String, String> find = new HashMap<String, String>();
                find.put("arrayitemid", arritem);
                DataSet arrayitemzones = arrayitemarrayzones.getFilteredDataSet(find);
                for (int zonenum = 0; zonenum < arrayitemzones.getRowCount(); ++zonenum) {
                    String operator;
                    String currzone = arrayitemzones.getString(zonenum, "arrayzoneid");
                    HashMap<String, String> findpi = new HashMap<String, String>();
                    findpi.put("paramid", currparam);
                    findpi.put("paramtype", currparamtype);
                    findpi.put("arrayzoneid", currzone);
                    int match = arrayparamitems.findRow(findpi);
                    if (match <= -1 || (operator = arrayparamitems.getString(match, "operator")) == null || operator.trim().length() <= 0) continue;
                    String value1 = arrayparamitems.getValue(match, "value1", "");
                    String value2 = arrayparamitems.getValue(match, "value2", "");
                    String value1num = arrayparamitems.getValue(match, "value1num", "");
                    String value2num = arrayparamitems.getValue(match, "value2num", "");
                    String limitunit = arrayparamitems.getString(match, "unitsid");
                    this.checkDataItemLimits(row, di, operator, value1, value1num, value2, value2num, limitunit, limitsUtil, formatutil);
                }
            }
        }
    }

    private void checkDataItemLimits(int row, DataSet di, String operator, String value1, String value1num, String value2, String value2num, String limitunit, DataEntryLimitsUtil limitsUtil, FormatUtil formatutil) throws SapphireException {
        String enteredText = di.getString(row, "enteredtext");
        if (!"(null)".equals(enteredText) && StringUtil.getLen(enteredText) > 0L) {
            Object comparevalue;
            String datatypes = di.getString(row, "datatypes");
            if (datatypes.equals("A") && (datatypes = di.getValue(row, COL_APPARENTDATATYPE)).length() == 0) {
                DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
                datatypes = DataEntryLimitsUtil.getApparentDatatype(enteredText, dtu, formatutil);
            }
            if (datatypes.equals("N") || datatypes.equals("NC")) {
                comparevalue = di.getBigDecimal(row, "transformvalue");
                String diunit = di.getString(row, "displayunits");
                if (StringUtil.getLen(limitunit) > 0L && StringUtil.getLen(diunit) > 0L && !diunit.equalsIgnoreCase(limitunit)) {
                    StringHolder unitholder = new StringHolder();
                    unitholder.value = diunit;
                    comparevalue = limitsUtil.convertUnits(unitholder, limitunit, (BigDecimal)comparevalue);
                }
                if (comparevalue != null) {
                    if (limitsUtil.isLimitMet(comparevalue, operator, limitsUtil.getLimitValue(datatypes, value1, value1num), limitsUtil.getLimitValue(datatypes, value2, value2num), datatypes, "", di.getString(row, "enteredoperator"))) {
                        di.setString(row, "s_qcevalstatus", "Met");
                    } else {
                        di.setString(row, "s_qcevalstatus", "Not Met");
                    }
                } else {
                    di.setString(row, "s_qcevalstatus", "Undetermined");
                }
            } else if (datatypes.equals("T") || datatypes.equals("TC") || datatypes.equals("R") || datatypes.equals("V") || datatypes.equals("S")) {
                comparevalue = di.getString(row, "transformtext");
                if (comparevalue != null && ((String)comparevalue).length() > 0) {
                    String extrastuff = null;
                    if (datatypes.equals("R") || datatypes.equals("V")) {
                        extrastuff = di.getString(row, "entryreftypeid");
                    } else if (datatypes.equals("S")) {
                        extrastuff = di.getString(row, "entrysdcid");
                    }
                    if (limitsUtil.isLimitMet(comparevalue, operator, limitsUtil.getLimitValue(datatypes, value1, value1num), limitsUtil.getLimitValue(datatypes, value2, value2num), datatypes, extrastuff, di.getString(row, "enteredoperator"))) {
                        di.setString(row, "s_qcevalstatus", "Met");
                    } else {
                        di.setString(row, "s_qcevalstatus", "Not Met");
                    }
                } else {
                    di.setString(row, "s_qcevalstatus", "Not Met");
                }
            }
        } else {
            di.setString(row, "s_qcevalstatus", "Undetermined");
        }
    }

    public boolean[] checkUndeterminedOrLimitMet(DataEntryLimitsUtil limitsUtil, String datatypes, BigDecimal comparevalue, String enteredoperator, String operator1, String value1, String operator2, String value2) throws SapphireException {
        boolean undeterminedLimit = limitsUtil.isLimitUndetermined(comparevalue, operator1, value1, "", datatypes, enteredoperator);
        if (!undeterminedLimit && StringUtil.getLen(operator2) > 0L) {
            undeterminedLimit = limitsUtil.isLimitUndetermined(comparevalue, operator2, value2, "", datatypes, enteredoperator);
        }
        boolean limitmet = false;
        if (!undeterminedLimit && (limitmet = limitsUtil.isLimitMet(comparevalue, operator1, value1, "", datatypes, "", enteredoperator)) && StringUtil.getLen(operator2) > 0L) {
            limitmet = limitsUtil.isLimitMet(comparevalue, operator2, value2, "", datatypes, "", enteredoperator);
        }
        return new boolean[]{undeterminedLimit, limitmet};
    }

    private void callRedoCalcOnLinkedSDIs(DataSet updateDataitems, String rsetid, SDCProcessor sdcProcessor, QueryProcessor qp, String tracelogid) throws SapphireException {
        String sdcid = updateDataitems.getValue(0, "sdcid");
        long start = System.currentTimeMillis();
        DataSet dsCCDAll = this.getCrossSDICalcDefs(rsetid, sdcid, qp);
        dsCCDAll.sort("crosssdicalcdefid");
        ArrayList<DataSet> ccGrps = dsCCDAll.getGroupedDataSets("crosssdicalcdefid");
        DataSet dsUniqCCD = new DataSet();
        for (int cc = 0; cc < ccGrps.size(); ++cc) {
            this.fetchUniqueCrossSDICalcDefs(updateDataitems, ccGrps, dsUniqCCD, cc);
        }
        this.logger.info("callRedoCalcOnLinkedSDIs: Time taken to fetch the CrossSDICalcDefs: " + (System.currentTimeMillis() - start) + " ms");
        DataSet redocalc = new DataSet();
        redocalc.addColumn("sdcid", 0);
        redocalc.addColumn("keyid1", 0);
        redocalc.addColumn("keyid2", 0);
        redocalc.addColumn("keyid3", 0);
        StringBuffer commonSelect = new StringBuffer();
        StringBuffer commonWhere1 = new StringBuffer();
        StringBuffer commonWhere2 = new StringBuffer();
        commonSelect.append("SELECT ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3, ds.paramlistid, ds.paramlistversionid, ds.variantid, ds.dataset, di.paramid, di.paramtype, di.replicateid, di.releasedflag, di.calcrule, ds.s_datasetstatus, ");
        commonSelect.append(this.database.isOracle() ? " to_char(pc.calcruleparams) calcruleparams " : " convert(varchar, pc.calcruleparams) calcruleparams").append(" FROM sdidata ds, paramlistcrosssdicalc pc, sdidataitem di, sdidatacrosssdicalc ccds, rsetitems, ");
        commonWhere1.append(" WHERE ds.sdcid = ccds.sdcid AND ds.keyid1 = ccds.keyid1 AND ds.keyid2 = ccds.keyid2 AND ds.keyid3 = ccds.keyid3").append(" AND ds.paramlistid = ccds.paramlistid AND ds.paramlistversionid = ccds.paramlistversionid AND ds.variantid = ccds.variantid AND ds.dataset = ccds.dataset ").append(" AND pc.paramlistid = ccds.paramlistid AND pc.paramlistversionid = ccds.paramlistversionid  AND pc.variantid = ccds.variantid AND pc.crosssdicalcdefid = ccds.crosssdicalcdefid").append(" AND ccds.crosssdicalcdefid = ");
        commonWhere2.append(" AND ds.s_datasetstatus NOT IN ('Cancelled')  AND di.sdcid = ds.sdcid AND di.keyid1 = ds.keyid1 AND di.keyid2 = ds.keyid2 AND di.keyid3 = ds.keyid3 ").append(" AND di.paramlistid = ds.paramlistid AND di.paramlistversionid = ds.paramlistversionid AND di.variantid = ds.variantid AND di.dataset = ds.dataset ");
        commonWhere2.append(" AND ");
        SafeSQL safeSQL = new SafeSQL();
        for (int i = 0; i < dsUniqCCD.getRowCount(); ++i) {
            safeSQL.reset();
            String sql = "";
            String crosssdicalcdefid = dsUniqCCD.getValue(i, "crosssdicalcdefid");
            String fromSdc = dsUniqCCD.getValue(i, "fromsdcid");
            String toSdc = dsUniqCCD.getValue(i, "tosdcid");
            String fkColumnId = dsUniqCCD.getValue(i, "fromsdccolumnid");
            String tofkColumnId = dsUniqCCD.getValue(i, "tosdccolumnid");
            String type = dsUniqCCD.getValue(i, "crosssdicalctype");
            String relationtype = dsUniqCCD.getValue(i, "relationtype");
            if (type.startsWith("AQC")) {
                type = "AQC";
            }
            switch (type) {
                case "FK": {
                    sql = this.createFKQuery(i, dsUniqCCD, sdcid, rsetid, commonSelect, commonWhere1, commonWhere2, sdcProcessor, safeSQL);
                    break;
                }
                case "REVERSE_FK": {
                    sql = this.createReverseFKQuery(i, dsUniqCCD, rsetid, commonSelect, commonWhere1, commonWhere2, sdcProcessor, safeSQL);
                    break;
                }
                case "Parent": {
                    sql = this.createParentQuery(i, dsUniqCCD, sdcid, rsetid, commonSelect, commonWhere1, commonWhere2, safeSQL);
                    break;
                }
                case "Child": {
                    sql = this.createChildQuery(i, dsUniqCCD, sdcid, rsetid, commonSelect, commonWhere1, commonWhere2, safeSQL);
                    break;
                }
                case "Ancestor": {
                    sql = this.createAncestorQuery(i, dsUniqCCD, sdcid, rsetid, safeSQL);
                    break;
                }
                case "Descendant": {
                    sql = this.createDescendantQuery(i, dsUniqCCD, sdcid, rsetid, safeSQL);
                    break;
                }
                case "AQC": {
                    sql = this.createAQCQuery(i, dsUniqCCD, rsetid, commonSelect, commonWhere1, commonWhere2, safeSQL);
                    break;
                }
                case "sdirelation": {
                    sql = this.createSDIRelationQuery(i, dsUniqCCD, rsetid, commonSelect, commonWhere1, commonWhere2, safeSQL);
                    break;
                }
                case "reversesdirelation": {
                    sql = this.createReverseSDIRelationQuery(i, dsUniqCCD, rsetid, commonSelect, commonWhere1, commonWhere2, safeSQL);
                    break;
                }
                case "sdidatarelation": {
                    sql = this.createSDIDataRelationQuery(i, dsUniqCCD, rsetid, commonSelect, commonWhere1, commonWhere2, safeSQL);
                    break;
                }
                case "absolute": {
                    sql = this.createAbsoluteQuery(i, dsUniqCCD, commonSelect, commonWhere1, commonWhere2, sdcProcessor, safeSQL);
                    break;
                }
            }
            if (!OpalUtil.isNotEmpty(sql)) continue;
            start = System.currentTimeMillis();
            Trace.setStartCrossSDICalc(crosssdicalcdefid, "");
            String strType = StringUtil.replaceAll(type, ";", "#semicolon#");
            String message = "(Type: " + strType + (relationtype.length() > 0 ? ", Relation Type: " + relationtype : "");
            message = message + (fromSdc.length() > 0 ? ", From SDC: " + fromSdc : "");
            message = message + (toSdc.length() > 0 ? ", To SDC: " + toSdc : "");
            message = message + (fkColumnId.length() > 0 ? ", From SDC Column: " + fkColumnId : "");
            message = message + (tofkColumnId.length() > 0 ? ", To SDC Column: " + tofkColumnId : "");
            int redoCalcCountBefore = redocalc.getRowCount();
            DataSet dsDataSet = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            DataSet dsDataSetFiltered = new DataSet();
            this.logger.info("callRedoCalcOnLinkedSDIs: Time taken to fetch SDIs with " + type + " in calcrule: " + (System.currentTimeMillis() - start) + " ms");
            int rows = 0;
            if (dsDataSet.getRowCount() > 0) {
                for (int d = 0; d < dsDataSet.getRowCount(); ++d) {
                    HashMap<String, String> findPL = new HashMap<String, String>();
                    findPL.put("paramlistid", dsDataSet.getValue(d, "paramlistid"));
                    findPL.put("paramlistversionid", dsDataSet.getValue(d, "paramlistversionid"));
                    findPL.put("variantid", dsDataSet.getValue(d, "variantid"));
                    if (dsCCDAll.findRow(findPL) <= -1) continue;
                    dsDataSetFiltered.copyRow(dsDataSet, d, 1);
                }
                if (dsDataSetFiltered.getRowCount() > 0) {
                    int refrenceSDICount = this.getRedoCalcSDIS(updateDataitems, dsDataSetFiltered, redocalc, type);
                    int redoCount = redocalc.getRowCount() - redoCalcCountBefore;
                    if (refrenceSDICount > 0) {
                        rows = refrenceSDICount;
                    }
                }
            }
            message = message + ")";
            this.logger.info("callRedoCalcOnLinkedSDIs:Number of datasets found with cross SDI calc rules for " + crosssdicalcdefid + " " + message + " " + dsDataSet.getRowCount());
            message = message + ". RedoCalculations called on ";
            Trace.setEndCrossSDICalc(crosssdicalcdefid, message, rows, start);
        }
        DataSet qcBatchSamples = qp.getPreparedSqlDataSet("SELECT DISTINCT qcbi.s_qcbatchid, qcbi.s_qcbatchitemid, qcbi.batchitemtype  FROM sdidata s, s_qcbatchitem qcbi, rsetitems r  WHERE s.sdcid = r.sdcid AND s.keyid1 = r.keyid1  AND qcbi.s_qcbatchid = s.s_qcbatchid AND qcbi.s_qcbatchitemid = s.s_qcbatchitemid AND r.rsetid = ? ", (Object[])new String[]{rsetid});
        if (OpalUtil.isNotEmpty(updateDataitems) && qcBatchSamples.getRowCount() > 0) {
            this.performRedoCalcForQCBatchSamples(updateDataitems, qp, redocalc, safeSQL, qcBatchSamples);
        }
        for (int i = redocalc.size() - 1; i >= 0; --i) {
            if (!this.isLiveLimitChecking || !this.liveDataEntryCrossSdiCheckingMode.equals(LIVELIMITCHECK_CROSSSDI_MODE_SHALLOW) || this.allSdisOnDataEntryPage == null || this.allSdisOnDataEntryPage.length() <= 0) continue;
            String key = redocalc.getValue(i, "keyid1") + ";" + redocalc.getValue(i, "keyid2", "(null)") + ";" + redocalc.getValue(i, "keyid3", "(null)");
            if (("|" + this.allSdisOnDataEntryPage + "|").contains(key)) continue;
            redocalc.deleteRow(i);
        }
        this.logger.info("callRedoCalcOnLinkedSDIs:Number of SDIs on which RedoCalculations action is to be called  : " + redocalc.getRowCount());
        if (redocalc.getRowCount() > 0) {
            this.executeRedoCalculationAction(tracelogid, redocalc);
        }
    }

    private void fetchUniqueCrossSDICalcDefs(DataSet updateDataitems, List<DataSet> ccGrps, DataSet dsUniqCCD, int cc) {
        DataSet dsPL = ccGrps.get(cc);
        boolean crossdefCopied = false;
        for (int p = 0; p < dsPL.getRowCount(); ++p) {
            String[] rules;
            String calcruleParams = dsPL.getValue(p, "calcruleparams");
            if (calcruleParams.length() == 0) {
                dsUniqCCD.copyRow(dsPL, p, 1);
                break;
            }
            for (String rule : rules = StringUtil.split(calcruleParams, ",")) {
                String calcrule = rule.trim();
                if (calcrule.startsWith("[") && calcrule.endsWith("]")) {
                    if (!this.isCalcRuleMatchingParamFound(calcrule, updateDataitems)) continue;
                    dsUniqCCD.copyRow(dsPL, p, 1);
                    crossdefCopied = true;
                } else {
                    dsUniqCCD.copyRow(dsPL, p, 1);
                    crossdefCopied = true;
                }
                if (crossdefCopied) break;
            }
            if (crossdefCopied) break;
        }
    }

    private void executeRedoCalculationAction(String tracelogid, DataSet redocalc) throws ActionException {
        redocalc.sort("sdcid");
        ArrayList<DataSet> sdcGroups = redocalc.getGroupedDataSets("sdcid");
        for (DataSet ds : sdcGroups) {
            String redoSdcid = ds.getValue(0, "sdcid");
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", redoSdcid);
            props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", ds.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", ds.getColumnValues("keyid3", ";"));
            props.setProperty("tracelogid", tracelogid);
            this.previouslyCalledRedoCalcSDIS.copyRow(ds, -1, 1);
            props.setProperty(REDOCALC_SDIS, this.previouslyCalledRedoCalcSDIS.toXML());
            props.put(CROSSSDI_ALL_MODIFIEDDATAITEMS, this.crossSDIAllModifiedSDIDataitems);
            props.put(DATAENTRYPAGE_ALLSDIS, this.allSdisOnDataEntryPage);
            if (this.isLiveLimitChecking) {
                props.setProperty("islivelimitchecking", "Y");
                props.put(LIVELIMITCHECK_CROSSSDI_MODE, this.liveDataEntryCrossSdiCheckingMode);
            }
            long start = System.currentTimeMillis();
            this.getActionProcessor().processAction("RedoCalculations", "1", props);
            long took = System.currentTimeMillis() - start;
            this.logger.info("callRedoCalcOnLinkedSDIs: RedoCalculations on " + ds.getRowCount() + " " + ds.getValue(0, "sdcid") + "(s) took " + took + "ms");
            DataSet modified = (DataSet)props.get(LIVEDATAENTRY_MODIFIEDDATAITEMS);
            if (modified == null) continue;
            this.logger.info("callRedoCalcOnLinkedSDIs:Number of dataitems modified by RedoCalculations action call  : " + modified.getRowCount());
        }
    }

    private void performRedoCalcForQCBatchSamples(DataSet updateDataitems, QueryProcessor qp, DataSet redocalc, SafeSQL safeSQL, DataSet qcBatchSamples) throws SapphireException {
        long start = System.currentTimeMillis();
        Trace.setStartCrossSDICalc("", "");
        String message = "";
        int rows = 0;
        int redoCalcCountBefore = redocalc.getRowCount();
        qcBatchSamples.sort("s_qcbatchid");
        StringBuilder qcbatchIds = new StringBuilder();
        ArrayList<DataSet> qcBatchGrps = qcBatchSamples.getGroupedDataSets("s_qcbatchid");
        Iterator<DataSet> iterator = qcBatchGrps.iterator();
        while (iterator.hasNext()) {
            DataSet qcBatchGrp;
            DataSet dsQCBatch = qcBatchGrp = iterator.next();
            String qcBatchId = dsQCBatch.getValue(0, "s_qcbatchid");
            qcbatchIds.append(",").append(qcBatchId);
            for (int j = 0; j < dsQCBatch.getRowCount(); ++j) {
                DataSet dsQCBDataItems;
                safeSQL.reset();
                StringBuilder findQCBatchDatasetSQL = new StringBuilder();
                String batchitemid = dsQCBatch.getValue(j, "s_qcbatchitemid");
                String batchitemtype = dsQCBatch.getValue(j, "batchitemtype");
                if (batchitemtype.equalsIgnoreCase("Blank")) {
                    findQCBatchDatasetSQL.append("SELECT DISTINCT ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3, ds.paramlistid, ds.paramlistversionid, ds.variantid,").append(" ds.dataset, di.paramid, di.paramtype, ds.s_qcbatchitemid, di.replicateid, di.releasedflag, di.calcrule ").append(" FROM sdidata ds, sdidataitem di").append(" WHERE ds.s_datasetstatus <> 'Cancelled' ").append(" AND di.paramtype = 'BlankCorrected' ").append(" AND di.sdcid = ds.sdcid AND di.keyid1 = ds.keyid1 AND di.keyid2 = ds.keyid2 AND di.keyid3 = ds.keyid3 ").append(" AND di.paramlistid = ds.paramlistid AND di.paramlistversionid = ds.paramlistversionid AND di.variantid = ds.variantid ").append(" AND di.dataset = ds.dataset AND di.calcrule is not null ").append(" AND ds.s_qcbatchid = ").append(safeSQL.addVar(qcBatchId));
                } else {
                    String qcSampleItemid = qp.getPreparedSqlDataSet("select s_qcbatchitemid from s_qcbatchitem where s_qcbatchid = ? and linktoqcbatchitemid = ? ", (Object[])new String[]{qcBatchId, batchitemid}).getColumnValues("s_qcbatchitemid", ";");
                    if (OpalUtil.isNotEmpty(qcSampleItemid)) {
                        findQCBatchDatasetSQL.append("SELECT DISTINCT ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3, ds.paramlistid, ds.paramlistversionid, ds.variantid,").append(" ds.dataset, di.paramid, di.paramtype, ds.s_qcbatchitemid, di.replicateid, di.releasedflag, di.calcrule ").append(" FROM sdidata ds, sdidataitem di ").append(" WHERE ds.s_datasetstatus <> 'Cancelled' ").append(" AND di.sdcid = ds.sdcid AND di.keyid1 = ds.keyid1 AND di.keyid2 = ds.keyid2 AND di.keyid3 = ds.keyid3 ").append(" AND di.paramlistid = ds.paramlistid AND di.paramlistversionid = ds.paramlistversionid AND di.variantid = ds.variantid ").append(" AND di.dataset = ds.dataset AND di.calcrule is not null ").append(" AND ds.s_qcbatchitemid in ( ").append(safeSQL.addIn(qcSampleItemid, ";")).append(" )").append(" AND ds.s_qcbatchid = ").append(safeSQL.addVar(qcBatchId));
                    }
                }
                if (!OpalUtil.isNotEmpty(findQCBatchDatasetSQL.toString()) || !OpalUtil.isNotEmpty(dsQCBDataItems = qp.getPreparedSqlDataSet(findQCBatchDatasetSQL.toString(), safeSQL.getValues()))) continue;
                this.logger.info("callRedoCalcOnLinkedSDIs: Time taken to fetch QCBatchItems in QCBatch: " + qcBatchId + " " + (System.currentTimeMillis() - start) + " ms");
                int refrenceSDICount = this.getRedoCalcSDISForQCBatch(updateDataitems, dsQCBDataItems, redocalc, batchitemid, qcBatchId, qp);
                if (refrenceSDICount <= 0) continue;
                rows += refrenceSDICount;
            }
        }
        int redoCount = redocalc.getRowCount() - redoCalcCountBefore;
        String batchIds = "";
        if (qcbatchIds.length() > 0) {
            batchIds = qcbatchIds.substring(1);
        }
        message = message + "RedoCalculations called on " + redoCount + " QC Batch Item";
        message = message + (redoCount > 1 ? "s" : "");
        message = message + " of QCBatch";
        message = message + (batchIds.contains(",") ? "es" : "");
        message = message + ": " + batchIds;
        Trace.setEndCrossSDICalc("", message, rows, start);
    }

    private int getRedoCalcSDIS(DataSet updatedDI, DataSet dsDataSet, DataSet redoCalc, String type) throws SapphireException {
        HashSet<String> refSDISet = new HashSet<String>();
        for (int d = 0; d < dsDataSet.getRowCount(); ++d) {
            HashMap<String, Object> findMap = new HashMap<String, Object>();
            String sdcId = dsDataSet.getValue(d, "sdcid");
            String keyid1 = dsDataSet.getValue(d, "keyid1");
            String keyid2 = dsDataSet.getValue(d, "keyid2");
            String keyid3 = dsDataSet.getValue(d, "keyid3");
            String paramlistid = dsDataSet.getValue(d, "paramlistid");
            String paramlistversionid = dsDataSet.getValue(d, "paramlistversionid");
            String variantid = dsDataSet.getValue(d, "variantid");
            String paramid = dsDataSet.getValue(d, "paramid");
            String paramtype = dsDataSet.getValue(d, "paramtype");
            String replicateid = dsDataSet.getValue(d, "replicateid");
            String releasedflag = dsDataSet.getValue(d, "releasedflag");
            String dataset = String.valueOf(dsDataSet.getInt(d, "dataset"));
            String calcruleParams = dsDataSet.getValue(d, "calcruleparams");
            if (calcruleParams.length() > 0 && !this.isCalcRuleMatchingParamFoundConsideringHashToken(calcruleParams, updatedDI, dsDataSet, d, type)) continue;
            refSDISet.add(sdcId + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
            findMap.put("sdcid", sdcId);
            findMap.put("keyid1", keyid1);
            findMap.put("keyid2", keyid2);
            findMap.put("keyid3", keyid3);
            findMap.put("paramlistid", paramlistid);
            findMap.put("paramlistversionid", paramlistversionid);
            findMap.put("variantid", variantid);
            findMap.put("paramid", paramid);
            findMap.put("dataset", new BigDecimal(dataset));
            findMap.put("paramtype", paramtype);
            findMap.put("replicateid", new BigDecimal(replicateid));
            findMap.put("releasedflag", releasedflag);
            findMap.put("calcruleparams", calcruleParams);
            if (this.previouslyCalledRedoCalcSDIS.findRow(findMap) > -1 || redoCalc.findRow(findMap) > -1) continue;
            redoCalc.copyRow(dsDataSet, d, 1);
        }
        return refSDISet.size();
    }

    private boolean isCalcRuleMatchingParamFound(String calcrule, DataSet updatedDataItems) {
        calcrule = calcrule.substring(1, calcrule.lastIndexOf("]"));
        String[] parts = StringUtil.split(calcrule, "|");
        String ruleParamlist = "";
        String ruleParamlistVersion = "";
        String ruleParamlistVariant = "";
        String ruleParamlistDataset = "";
        String ruleParam = "";
        String ruleParamType = "";
        String ruleParamReplicate = "";
        if (parts.length > 1) {
            String[] paramPartsArray;
            String[] paramlistPartsArray = StringUtil.split(parts[0], ";");
            if (paramlistPartsArray.length > 0) {
                ruleParamlist = paramlistPartsArray[0];
            }
            if (paramlistPartsArray.length > 1) {
                ruleParamlistVersion = paramlistPartsArray[1];
            }
            if (paramlistPartsArray.length > 2) {
                ruleParamlistVariant = paramlistPartsArray[2];
            }
            if (paramlistPartsArray.length > 3) {
                ruleParamlistDataset = paramlistPartsArray[3];
            }
            if ((paramPartsArray = StringUtil.split(parts[1], ";")).length > 0) {
                ruleParam = paramPartsArray[0];
            }
            if (paramPartsArray.length > 1) {
                ruleParamType = paramPartsArray[1];
            }
            if (paramPartsArray.length > 2) {
                ruleParamReplicate = paramPartsArray[2];
            }
            HashMap<String, Object> findSourceDI = new HashMap<String, Object>();
            if (ruleParamlist.length() > 0 && !ruleParamlist.trim().equals("*") && !ruleParamlist.trim().equals("#")) {
                findSourceDI.put("paramlistid", ruleParamlist);
            }
            if (ruleParamlistVersion.length() > 0 && !ruleParamlistVersion.trim().equals("*") && !ruleParamlistVersion.trim().equals("#")) {
                findSourceDI.put("paramlistversionid", ruleParamlistVersion);
            }
            if (ruleParamlistVariant.length() > 0 && !ruleParamlistVariant.trim().equals("*") && !ruleParamlistVariant.trim().equals("#")) {
                findSourceDI.put("variantid", ruleParamlistVariant);
            }
            if (ruleParamlistDataset.length() > 0 && !ruleParamlistDataset.trim().equals("*") && !ruleParamlistDataset.trim().equals("#")) {
                try {
                    findSourceDI.put("dataset", new BigDecimal(ruleParamlistDataset));
                }
                catch (Exception e) {
                    this.logger.warn("Invalid setting found in calcruleparams of ParamList");
                }
            }
            if (ruleParam.length() > 0 && !ruleParam.trim().equals("*") && !ruleParam.trim().equals("#")) {
                findSourceDI.put("paramid", ruleParam);
            }
            if (ruleParamType.length() > 0 && !ruleParamType.trim().equals("*") && !ruleParamType.trim().equals("#")) {
                findSourceDI.put("paramtype", ruleParamType);
            }
            if (ruleParamReplicate.length() > 0 && !ruleParamReplicate.trim().equals("*") && !ruleParamReplicate.trim().equals("#")) {
                try {
                    findSourceDI.put("replicateid", new BigDecimal(ruleParamReplicate));
                }
                catch (Exception e) {
                    this.logger.warn("Invalid setting found in calcruleparams of ParamList");
                }
            }
            if (findSourceDI.size() > 0) {
                return updatedDataItems.findRow(findSourceDI) >= 0;
            }
            return true;
        }
        return true;
    }

    private boolean isCalcRuleMatchingParamFoundConsideringHashToken(String calcruleParams, DataSet updatedDataItems, DataSet dsDataSet, int d, String type) throws SapphireException {
        String paramlist = dsDataSet.getValue(d, "paramlistid");
        String paramlistVersion = dsDataSet.getValue(d, "paramlistversionid");
        String paramlistVariant = dsDataSet.getValue(d, "variantid");
        String paramlistDS = dsDataSet.getValue(d, "dataset");
        String param = dsDataSet.getValue(d, "paramid");
        String paramType = dsDataSet.getValue(d, "paramtype");
        String replicateid = dsDataSet.getValue(d, "replicateid");
        String[] rules = StringUtil.split(calcruleParams, ",");
        String calcrule = dsDataSet.getValue(d, "calcrule");
        List<String> calcruletoMatchList = this.getCalcruleToMatched(calcrule, type);
        for (String calcruletoMatched : calcruletoMatchList) {
            if (calcruletoMatched.startsWith("[") && calcruletoMatched.endsWith("]")) {
                calcruletoMatched = calcruletoMatched.substring(1, calcruletoMatched.lastIndexOf("]"));
                String[] parts = StringUtil.split(calcruletoMatched, "|");
                String ruleParamlist = "";
                String ruleParamlistVersion = "";
                String ruleParamlistVariant = "";
                String ruleParamlistDataset = "";
                String ruleParam = "";
                String ruleParamType = "";
                String ruleParamReplicate = "";
                if (parts.length > 1) {
                    String[] paramPartsArray;
                    String[] paramlistPartsArray = StringUtil.split(parts[0], ";");
                    if (paramlistPartsArray.length > 0 && (ruleParamlist = paramlistPartsArray[0]).equals("#")) {
                        ruleParamlist = paramlist;
                    }
                    if (paramlistPartsArray.length > 1 && (ruleParamlistVersion = paramlistPartsArray[1]).equals("#")) {
                        ruleParamlistVersion = paramlistVersion;
                    }
                    if (paramlistPartsArray.length > 2 && (ruleParamlistVariant = paramlistPartsArray[2]).equals("#")) {
                        ruleParamlistVariant = paramlistVariant;
                    }
                    if (paramlistPartsArray.length > 3 && (ruleParamlistDataset = paramlistPartsArray[3]).equals("#")) {
                        ruleParamlistDataset = paramlistDS;
                    }
                    if ((paramPartsArray = StringUtil.split(parts[1], ";")).length > 0 && (ruleParam = paramPartsArray[0]).equals("#")) {
                        ruleParam = param;
                    }
                    if (paramPartsArray.length > 1 && (ruleParamType = paramPartsArray[1]).equals("#")) {
                        ruleParamType = paramType;
                    }
                    if (paramPartsArray.length > 2 && (ruleParamReplicate = paramPartsArray[2]).equals("#")) {
                        ruleParamReplicate = replicateid;
                    }
                    HashMap<String, Object> findSourceDI = new HashMap<String, Object>();
                    if (ruleParamlist.length() > 0 && !ruleParamlist.trim().equals("*")) {
                        findSourceDI.put("paramlistid", ruleParamlist);
                    }
                    if (ruleParamlistVersion.length() > 0 && !ruleParamlistVersion.trim().equals("*")) {
                        findSourceDI.put("paramlistversionid", ruleParamlistVersion);
                    }
                    if (ruleParamlistVariant.length() > 0 && !ruleParamlistVariant.trim().equals("*")) {
                        findSourceDI.put("variantid", ruleParamlistVariant);
                    }
                    if (ruleParamlistDataset.length() > 0 && !ruleParamlistDataset.trim().equals("*")) {
                        try {
                            findSourceDI.put("dataset", new BigDecimal(ruleParamlistDataset));
                        }
                        catch (Exception e) {
                            this.logger.warn("Invalid setting found in calcruleparams of ParamList");
                        }
                    }
                    if (ruleParam.length() > 0 && !ruleParam.trim().equals("*")) {
                        findSourceDI.put("paramid", ruleParam);
                    }
                    if (ruleParamType.length() > 0 && !ruleParamType.trim().equals("*")) {
                        findSourceDI.put("paramtype", ruleParamType);
                    }
                    if (ruleParamReplicate.length() > 0 && !ruleParamReplicate.trim().equals("*")) {
                        try {
                            findSourceDI.put("replicateid", new BigDecimal(ruleParamReplicate));
                        }
                        catch (Exception e) {
                            this.logger.warn("Invalid setting found in calcruleparams of ParamList");
                        }
                    }
                    if (findSourceDI.size() > 0) {
                        if (updatedDataItems.findRow(findSourceDI) < 0) continue;
                        return true;
                    }
                    return true;
                }
                return true;
            }
            return true;
        }
        return false;
    }

    private List<String> getCalcruleToMatched(String listOfCalcRule, String calctype) throws SapphireException {
        ArrayList<String> calcruleToMatched = new ArrayList<String>();
        String[] calcrule = StringUtil.getExpressionTokens(listOfCalcRule);
        HashMap<String, List<String>> crossSDICalcMap = new HashMap<String, List<String>>();
        for (int len = 0; len < calcrule.length; ++len) {
            this.populateCrossSDIRuleWithType(calcrule[len], crossSDICalcMap);
        }
        if (!crossSDICalcMap.isEmpty()) {
            for (Map.Entry entry : crossSDICalcMap.entrySet()) {
                String crossSDICalcType = (String)entry.getKey();
                if (!crossSDICalcType.equalsIgnoreCase(calctype)) continue;
                List calcrules = (List)entry.getValue();
                for (int len = 0; len < calcrules.size(); ++len) {
                    String[] calcInfo = StringUtil.split((String)calcrules.get(len), "|");
                    calcruleToMatched.add("[" + calcInfo[1] + "|" + calcInfo[2] + "]");
                }
            }
        }
        return calcruleToMatched;
    }

    private void populateCrossSDIRuleWithType(String calcrule, Map<String, List<String>> crossSDICalcMap) {
        String[] tokenParts = StringUtil.split(calcrule, "|");
        if (tokenParts.length == 3) {
            String[] typeinfo;
            String crossSDICalcType = "";
            crossSDICalcType = tokenParts[0].trim().contains(":") ? ((typeinfo = StringUtil.split(tokenParts[0].trim(), ":"))[0].equalsIgnoreCase("sample") ? typeinfo[1].trim().toLowerCase() : (typeinfo[0].equalsIgnoreCase("sdi") ? "absolute" : (typeinfo[0].equalsIgnoreCase("link") ? (typeinfo[1].contains(";") ? "REVERSE_FK" : "FK") : typeinfo[0].trim().toLowerCase()))) : "sdidatarelation";
            if (crossSDICalcMap.containsKey(crossSDICalcType)) {
                crossSDICalcMap.get(crossSDICalcType).add(calcrule);
            } else {
                ArrayList<String> calcRules = new ArrayList<String>();
                calcRules.add(calcrule);
                crossSDICalcMap.put(crossSDICalcType, calcRules);
            }
        }
    }

    private DataSet getCrossSDICalcDefs(String rsetid, String sdcid, QueryProcessor qp) {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        String sqlSelect = "SELECT cc.crosssdicalcdefid, cc.fromsdcid, cc.tosdcid, cc.fromsdccolumnid, cc.tosdccolumnid, cc.crosssdicalctype, cc.relationtype, cc.tokeyid1, cc.tokeyid2, cc.tokeyid3, pc.paramlistid, pc.paramlistversionid, pc.variantid,";
        sqlSelect = sqlSelect + (this.database.isOracle() ? " to_char(pc.calcruleparams) calcruleparams" : " convert(varchar, pc.calcruleparams) calcruleparams");
        sqlSelect = sqlSelect + " FROM  crosssdicalcdef cc, paramlistcrosssdicalc pc, paramlist p";
        String activeWhere = " AND  COALESCE( NULLIF( cc.rulestatusflag, '' ),'P' ) = 'E' ";
        String where = " AND pc.crosssdicalcdefid = cc.crosssdicalcdefid " + activeWhere;
        where = where + " AND p.paramlistid = pc.paramlistid AND p.paramlistversionid = pc.paramlistversionid  AND p.variantid = pc.variantid ";
        where = where + " AND COALESCE( NULLIF( p.enableautoredocalcflag, '' ),'N' ) = 'Y'";
        sql.append(sqlSelect).append(" WHERE cc.tosdcid = ").append(safeSQL.addVar(sdcid)).append(where).append(" UNION ").append(sqlSelect).append(", rsetitems r").append(" WHERE cc.crosssdicalctype =  ").append(safeSQL.addVar("absolute")).append(" AND cc.tosdcid = r.sdcid  AND cc.tokeyid1 = r.keyid1").append(" AND coalesce( nullif(cc.tokeyid2, ''),'(null)') = r.keyid2 ").append(" AND coalesce( nullif(cc.tokeyid3, ''),'(null)') = r.keyid3 AND r.rsetid = ").append(safeSQL.addVar(rsetid)).append(where).append(" UNION ").append(sqlSelect).append(", sdirelation s, rsetitems r").append(" WHERE cc.crosssdicalctype =  ").append(safeSQL.addVar("sdirelation")).append(" AND s.relationtype = cc.relationtype AND s.tosdcid = r.sdcid  AND s.tokeyid1 = r.keyid1").append(" AND coalesce( nullif(s.tokeyid2, ''),'(null)') = r.keyid2 ").append(" AND coalesce( nullif(s.tokeyid3, ''),'(null)') = r.keyid3 AND r.rsetid = ").append(safeSQL.addVar(rsetid)).append(where).append(" UNION ").append(sqlSelect).append(", sdirelation s, rsetitems r").append(" WHERE cc.crosssdicalctype = ").append(safeSQL.addVar("reversesdirelation")).append(" AND s.relationtype = cc.relationtype AND  s.fromsdcid = r.sdcid AND  s.fromkeyid1 = r.keyid1").append(" AND coalesce( nullif(s.fromkeyid2, ''),'(null)') = r.keyid2 ").append(" AND coalesce( nullif(s.fromkeyid3, ''),'(null)') = r.keyid3 AND r.rsetid = ").append(safeSQL.addVar(rsetid)).append(where).append(" UNION ").append(sqlSelect).append(", sdidatarelation s, rsetitems r").append(" WHERE cc.crosssdicalctype = ").append(safeSQL.addVar("sdidatarelation")).append(" AND  s.relationtype = cc.relationtype AND s.tosdcid = r.sdcid AND s.tokeyid1 = r.keyid1").append(" AND coalesce( nullif(s.tokeyid2, ''),'(null)') = r.keyid2 AND coalesce( nullif(s.tokeyid3, ''),'(null)') = r.keyid3 AND r.rsetid = ").append(safeSQL.addVar(rsetid)).append(where).append(" UNION ").append(sqlSelect).append(", sdiworkitemrelation s, rsetitems r").append(" WHERE cc.crosssdicalctype = ").append(safeSQL.addVar("sdidatarelation")).append(" AND  s.relationtype = cc.relationtype AND s.tosdcid = r.sdcid AND s.tokeyid1 = r.keyid1").append(" AND coalesce( nullif(s.tokeyid2, ''),'(null)') = r.keyid2 AND coalesce( nullif(s.tokeyid3, ''),'(null)') = r.keyid3 AND r.rsetid = ").append(safeSQL.addVar(rsetid)).append(where).append(" UNION ").append(sqlSelect).append(", sdidata s, s_qcbatchitem qcbi, s_qcbatchsampletype bst, rsetitems r").append(" WHERE cc.crosssdicalctype LIKE  ").append(safeSQL.addVar("AQC%")).append(" AND r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" AND cc.fromsdcid = r.sdcid ").append(" AND cc.tosdcid = r.sdcid AND  s.sdcid = r.sdcid AND s.keyid1 = r.keyid1 ").append(" AND qcbi.s_qcbatchid = s.s_qcbatchid AND qcbi.s_qcbatchitemid = s.s_qcbatchitemid AND cc.relationtype = qcbi.batchitemtype").append(" AND bst.s_qcbatchsampletypeid = qcbi.qcbatchsampletypeid ").append(where);
        if (this.database.isOracle()) {
            sql.append(" AND LOWER( bst.standardlevel) = CASE WHEN INSTR( cc.crosssdicalctype, ';', INSTR( cc.crosssdicalctype, ';' ) + 1 ) > 0 ").append(" THEN LOWER(SUBSTR( cc.crosssdicalctype, INSTR( cc.crosssdicalctype, ';', INSTR( cc.crosssdicalctype, ';' ) + 1 ) + 1 ) ) ELSE LOWER( bst.standardlevel ) END");
        } else {
            sql.append(" AND LOWER( bst.standardlevel) = CASE WHEN  CHARINDEX( ';', cc.crosssdicalctype, CHARINDEX( ';', cc.crosssdicalctype ) + 1 ) > 0 ").append(" THEN LOWER( SUBSTRING( cc.crosssdicalctype, CHARINDEX( ';', cc.crosssdicalctype, CHARINDEX( ';', cc.crosssdicalctype ) + 1 ) + 1, 100 ) ) ELSE LOWER( bst.standardlevel ) END");
        }
        return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private String createFKQuery(int i, DataSet dsUniqCCD, String sdcid, String rsetid, StringBuffer commonSelect, StringBuffer commonWhere1, StringBuffer commonWhere2, SDCProcessor sdcProcessor, SafeSQL safeSQL) {
        StringBuilder sql = new StringBuilder();
        String crosssdicalcdefid = dsUniqCCD.getValue(i, "crosssdicalcdefid");
        String fromSdc = dsUniqCCD.getValue(i, "fromsdcid");
        String fkColumnId = dsUniqCCD.getValue(i, "fromsdccolumnid");
        String fromTable = sdcProcessor.getProperty(fromSdc, "tableid");
        String fromTableKeyColId1 = sdcProcessor.getProperty(fromSdc, "keycolid1");
        String fromTableKeyColId2 = sdcProcessor.getProperty(fromSdc, "keycolid2");
        String fromTableKeyColId3 = sdcProcessor.getProperty(fromSdc, "keycolid3");
        DataSet linksData = sdcProcessor.getLinksData(fromSdc);
        int f = linksData.findRow("linksdcid", sdcid);
        if (f > -1) {
            String sdccolumnid = linksData.getValue(f, "sdccolumnid");
            String sdccolumnid2 = linksData.getValue(f, "sdccolumnid2");
            String sdccolumnid3 = linksData.getValue(f, "sdccolumnid3");
            if (sdccolumnid.equalsIgnoreCase(fkColumnId)) {
                sql.append(commonSelect).append(fromTable + " t ");
                sql.append(commonWhere1).append(safeSQL.addVar(crosssdicalcdefid));
                sql.append(commonWhere2).append(" ccds.sdcid = ").append(safeSQL.addVar(fromSdc)).append(" AND t.").append(fromTableKeyColId1).append("= ccds.keyid1 ");
                if (fromTableKeyColId2.length() > 0) {
                    sql.append(" AND t.").append(fromTableKeyColId2).append(" = ccds.keyid2 ");
                }
                if (fromTableKeyColId3.length() > 0) {
                    sql.append(" AND t.").append(fromTableKeyColId3).append(" = ccds.keyid3 ");
                }
                sql.append(" AND t.").append(fkColumnId).append(" = rsetitems.keyid1 AND rsetitems.rsetid = ").append(safeSQL.addVar(rsetid));
                if (sdccolumnid2.length() > 0) {
                    sql.append(" AND t.").append(sdccolumnid2).append(" = rsetitems.keyid2");
                }
                if (sdccolumnid3.length() > 0) {
                    sql.append(" AND t.").append(sdccolumnid3).append(" = rsetitems.keyid3");
                }
            }
        }
        return sql.toString();
    }

    private String createReverseFKQuery(int i, DataSet dsUniqCCD, String rsetid, StringBuffer commonSelect, StringBuffer commonWhere1, StringBuffer commonWhere2, SDCProcessor sdcProcessor, SafeSQL safeSQL) {
        StringBuilder sql = new StringBuilder();
        String crosssdicalcdefid = dsUniqCCD.getValue(i, "crosssdicalcdefid");
        String fromSdc = dsUniqCCD.getValue(i, "fromsdcid");
        String toSdc = dsUniqCCD.getValue(i, "tosdcid");
        String tofkColumnId = dsUniqCCD.getValue(i, "tosdccolumnid");
        DataSet toSDC_LinksData = sdcProcessor.getLinksData(toSdc);
        String toTable = sdcProcessor.getProperty(toSdc, "tableid");
        HashMap<String, String> find = new HashMap<String, String>();
        find.put("linksdcid", fromSdc);
        find.put("sdccolumnid", tofkColumnId);
        int f = toSDC_LinksData.findRow(find);
        if (f > -1) {
            String tofkColumnId2 = toSDC_LinksData.getValue(f, "sdccolumnid2");
            String tofkColumnId3 = toSDC_LinksData.getValue(f, "sdccolumnid3");
            String toTableKeyColId1 = sdcProcessor.getProperty(toSdc, "keycolid1");
            String toTableKeyColId2 = sdcProcessor.getProperty(toSdc, "keycolid2");
            String toTableKeyColId3 = sdcProcessor.getProperty(toSdc, "keycolid3");
            sql.append(commonSelect).append(toTable + " t ");
            sql.append(commonWhere1).append(safeSQL.addVar(crosssdicalcdefid));
            sql.append(commonWhere2).append(" ccds.sdcid = ").append(safeSQL.addVar(fromSdc)).append(" AND t.").append(tofkColumnId).append("= ccds.keyid1 ");
            if (tofkColumnId2.length() > 0) {
                sql.append(" AND t.").append(tofkColumnId2).append(" = ccds.keyid2 ");
            }
            if (tofkColumnId3.length() > 0) {
                sql.append(" AND t.").append(tofkColumnId3).append(" = ccds.keyid3 ");
            }
            sql.append(" AND t.").append(toTableKeyColId1).append(" = rsetitems.keyid1 AND rsetitems.rsetid =").append(safeSQL.addVar(rsetid));
            if (toTableKeyColId2.length() > 0) {
                sql.append(" AND t.").append(toTableKeyColId2).append(" = rsetitems.keyid2");
            }
            if (toTableKeyColId3.length() > 0) {
                sql.append(" AND t.").append(toTableKeyColId3).append(" = rsetitems.keyid3");
            }
        }
        return sql.toString();
    }

    private String createParentQuery(int i, DataSet dsUniqCCD, String sdcid, String rsetid, StringBuffer commonSelect, StringBuffer commonWhere1, StringBuffer commonWhere2, SafeSQL safeSQL) {
        StringBuilder sql = new StringBuilder();
        String crosssdicalcdefid = dsUniqCCD.getValue(i, "crosssdicalcdefid");
        if ("Sample".equals(sdcid)) {
            sql.append(commonSelect).append("s_childsample cs");
            sql.append(commonWhere1).append(safeSQL.addVar(crosssdicalcdefid));
            sql.append(commonWhere2).append(" ccds.sdcid = 'Sample' AND ccds.keyid1 = cs.s_childsampleid").append(" AND cs.s_sampleid = rsetitems.keyid1 AND rsetitems.rsetid = ").append(safeSQL.addVar(rsetid));
        }
        return sql.toString();
    }

    private String createChildQuery(int i, DataSet dsUniqCCD, String sdcid, String rsetid, StringBuffer commonSelect, StringBuffer commonWhere1, StringBuffer commonWhere2, SafeSQL safeSQL) {
        StringBuilder sql = new StringBuilder();
        String crosssdicalcdefid = dsUniqCCD.getValue(i, "crosssdicalcdefid");
        if ("Sample".equals(sdcid)) {
            sql.append(commonSelect).append("s_childsample cs");
            sql.append(commonWhere1).append(safeSQL.addVar(crosssdicalcdefid));
            sql.append(commonWhere2).append(" ccds.sdcid = 'Sample' AND ccds.keyid1 = cs.s_sampleid").append(" AND cs.s_childsampleid = rsetitems.keyid1 AND rsetitems.rsetid = ").append(safeSQL.addVar(rsetid));
        }
        return sql.toString();
    }

    private String createAncestorQuery(int i, DataSet dsUniqCCD, String sdcid, String rsetid, SafeSQL safeSQL) {
        StringBuilder sql = new StringBuilder();
        String crosssdicalcdefid = dsUniqCCD.getValue(i, "crosssdicalcdefid");
        if ("Sample".equals(sdcid)) {
            sql.append("WITH cs(s_sampleid, s_childsampleid) AS ").append("(SELECT s_sampleid, s_childsampleid ").append("FROM s_childsample, rsetitems ").append("WHERE rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" AND  rsetitems.sdcid = ").append(safeSQL.addVar(sdcid)).append(" and s_sampleid = rsetitems.keyid1 ").append("UNION ALL ").append("SELECT ncs.s_sampleid, ncs.s_childsampleid ").append("FROM s_childsample ncs, cs ").append("WHERE cs.s_childsampleid = ncs.s_sampleid ) ").append(" SELECT ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3, ds.paramlistid, ds.paramlistversionid, ds.variantid, ds.dataset,").append(" di.paramid, di.paramtype, di.replicateid, di.releasedflag ").append(" FROM cs, sdidata ds, sdidataitem di, sdidatacrosssdicalc ccds").append(" WHERE ds.sdcid =").append(safeSQL.addVar(sdcid)).append(" AND ccds.crosssdicalcdefid = ").append(safeSQL.addVar(crosssdicalcdefid)).append(" AND ccds.sdcid = ds.sdcid AND ds.keyid1 = ccds.keyid1 AND ds.keyid2 = ccds.keyid2 AND ds.keyid3 = ccds.keyid3").append(" AND ds.paramlistid = ccds.paramlistid AND ds.paramlistversionid = ccds.paramlistversionid AND ds.variantid = ccds.variantid AND ds.dataset = ccds.dataset ").append(" AND ds.s_datasetstatus NOT IN ('Completed', 'Cancelled') ").append(" AND di.sdcid = ds.sdcid AND di.keyid1 = ds.keyid1 AND di.keyid2 = ds.keyid2 AND di.keyid3 = ds.keyid3 ").append(" AND di.paramlistid = ds.paramlistid AND di.paramlistversionid = ds.paramlistversionid AND di.variantid = ds.variantid AND di.dataset = ds.dataset ").append(" AND cs.s_childsampleid = ds.keyid1");
        }
        return sql.toString();
    }

    private String createDescendantQuery(int i, DataSet dsUniqCCD, String sdcid, String rsetid, SafeSQL safeSQL) {
        StringBuilder sql = new StringBuilder();
        String crosssdicalcdefid = dsUniqCCD.getValue(i, "crosssdicalcdefid");
        if ("Sample".equals(sdcid)) {
            sql.append("WITH cs(s_sampleid, s_childsampleid) AS ").append("(SELECT s_sampleid, s_childsampleid ").append("FROM s_childsample, rsetitems ").append("WHERE rsetitems.rsetid =").append(safeSQL.addVar(rsetid)).append(" AND rsetitems.sdcid = ").append(safeSQL.addVar(sdcid)).append(" and s_childsampleid = rsetitems.keyid1 ").append("UNION ALL ").append("SELECT ncs.s_sampleid, ncs.s_childsampleid ").append("FROM s_childsample ncs, cs ").append("WHERE cs.s_sampleid = ncs.s_childsampleid ) ").append(" SELECT ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3, ds.paramlistid, ds.paramlistversionid, ds.variantid, ds.dataset,").append(" di.paramid, di.paramtype, di.replicateid, di.releasedflag ").append(" FROM cs, sdidata ds, sdidataitem di, sdidatacrosssdicalc ccds").append(" WHERE ds.sdcid = ").append(safeSQL.addVar(sdcid)).append(" AND ccds.crosssdicalcdefid = ").append(safeSQL.addVar(crosssdicalcdefid)).append(" AND ccds.sdcid = ds.sdcid AND ds.keyid1 = ccds.keyid1 AND ds.keyid2 = ccds.keyid2 AND ds.keyid3 = ccds.keyid3").append(" AND ds.paramlistid = ccds.paramlistid AND ds.paramlistversionid = ccds.paramlistversionid AND ds.variantid = ccds.variantid AND ds.dataset = ccds.dataset ").append(" AND ds.s_datasetstatus NOT IN ('Completed', 'Cancelled') ").append(" AND di.sdcid = ds.sdcid AND di.keyid1 = ds.keyid1 AND di.keyid2 = ds.keyid2 AND di.keyid3 = ds.keyid3 ").append(" AND di.paramlistid = ds.paramlistid AND di.paramlistversionid = ds.paramlistversionid AND di.variantid = ds.variantid AND di.dataset = ds.dataset ").append(" AND cs.s_sampleid = ds.keyid1");
        }
        return sql.toString();
    }

    private String createAQCQuery(int i, DataSet dsUniqCCD, String rsetid, StringBuffer commonSelect, StringBuffer commonWhere1, StringBuffer commonWhere2, SafeSQL safeSQL) {
        StringBuilder sql = new StringBuilder();
        String crosssdicalcdefid = dsUniqCCD.getValue(i, "crosssdicalcdefid");
        String type = dsUniqCCD.getValue(i, "crosssdicalctype");
        sql.append(commonSelect).append(" sdidata dsRset");
        sql.append(commonWhere1).append(safeSQL.addVar(crosssdicalcdefid));
        sql.append(commonWhere2).append(" rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" AND dsRset.sdcid = rsetitems.sdcid AND dsRset.keyid1 = rsetitems.keyid1 ").append(" AND ds.s_qcbatchid = dsRset.s_qcbatchid AND ds.s_qcbatchitemid != dsRset.s_qcbatchitemid");
        if (this.database.isOracle()) {
            sql.append(" AND   INSTR( LOWER(di.calcrule), " + safeSQL.addVar(type.toLowerCase()) + ") > 0 ");
        } else {
            sql.append(" AND   CHARINDEX( " + safeSQL.addVar(type.toLowerCase()) + ", LOWER( di.calcrule ) ) > 0 ");
        }
        return sql.toString();
    }

    private String createSDIRelationQuery(int i, DataSet dsUniqCCD, String rsetid, StringBuffer commonSelect, StringBuffer commonWhere1, StringBuffer commonWhere2, SafeSQL safeSQL) {
        StringBuilder sql = new StringBuilder();
        String crosssdicalcdefid = dsUniqCCD.getValue(i, "crosssdicalcdefid");
        String relationtype = dsUniqCCD.getValue(i, "relationtype");
        sql.append(commonSelect).append("sdirelation");
        sql.append(commonWhere1).append(safeSQL.addVar(crosssdicalcdefid));
        sql.append(commonWhere2).append(" ccds.sdcid = sdirelation.fromsdcid AND ccds.keyid1 = sdirelation.fromkeyid1").append(" AND ccds.keyid2 = coalesce( nullif( sdirelation.fromkeyid2, '' ),'(null)' ) AND ccds.keyid3 = coalesce( nullif( sdirelation.fromkeyid3, '' ), '(null)' )").append(" AND sdirelation.tosdcid = rsetitems.sdcid AND sdirelation.tokeyid1 = rsetitems.keyid1 AND coalesce(  nullif( sdirelation.tokeyid2, '') , '(null)') = rsetitems.keyid2  ").append(" AND coalesce( nullif( sdirelation.tokeyid3, ''), '(null)') = rsetitems.keyid3 AND sdirelation.relationtype = ").append(safeSQL.addVar(relationtype)).append(" AND rsetitems.rsetid = ").append(safeSQL.addVar(rsetid));
        return sql.toString();
    }

    private String createReverseSDIRelationQuery(int i, DataSet dsUniqCCD, String rsetid, StringBuffer commonSelect, StringBuffer commonWhere1, StringBuffer commonWhere2, SafeSQL safeSQL) {
        StringBuilder sql = new StringBuilder();
        String crosssdicalcdefid = dsUniqCCD.getValue(i, "crosssdicalcdefid");
        String relationtype = dsUniqCCD.getValue(i, "relationtype");
        sql.append(commonSelect).append("sdirelation");
        sql.append(commonWhere1).append(safeSQL.addVar(crosssdicalcdefid));
        sql.append(commonWhere2).append(" ccds.sdcid = sdirelation.tosdcid AND ccds.keyid1 = sdirelation.tokeyid1").append(" AND ccds.keyid2 = coalesce( nullif( sdirelation.tokeyid2, '' ),'(null)' ) AND ccds.keyid3 = coalesce( nullif( sdirelation.tokeyid3, '' ), '(null)' )").append(" AND sdirelation.fromsdcid = rsetitems.sdcid AND sdirelation.fromkeyid1 = rsetitems.keyid1 AND coalesce(  nullif( sdirelation.fromkeyid2, '') , '(null)') = rsetitems.keyid2  ").append(" AND coalesce( nullif( sdirelation.fromkeyid3, ''), '(null)') = rsetitems.keyid3 AND sdirelation.relationtype = ").append(safeSQL.addVar(relationtype)).append(" AND rsetitems.rsetid = ").append(safeSQL.addVar(rsetid));
        return sql.toString();
    }

    private String createSDIDataRelationQuery(int i, DataSet dsUniqCCD, String rsetid, StringBuffer commonSelect, StringBuffer commonWhere1, StringBuffer commonWhere2, SafeSQL safeSQL) {
        StringBuilder sql = new StringBuilder();
        String crosssdicalcdefid = dsUniqCCD.getValue(i, "crosssdicalcdefid");
        String relationtype = dsUniqCCD.getValue(i, "relationtype");
        sql.append(commonSelect).append("sdidatarelation");
        sql.append(commonWhere1).append(safeSQL.addVar(crosssdicalcdefid));
        sql.append(commonWhere2).append(" ccds.sdcid = sdidatarelation.sdcid AND ccds.keyid1 = sdidatarelation.keyid1").append(" AND ccds.keyid2 = coalesce( nullif( sdidatarelation.keyid2, '' ),('null') ) AND ccds.keyid3 = coalesce( nullif( sdidatarelation.keyid3, '' ), '(null)' )").append(" AND sdidatarelation.tosdcid = rsetitems.sdcid AND sdidatarelation.tokeyid1 = rsetitems.keyid1 AND coalesce(  nullif( sdidatarelation.tokeyid2, '') , '(null)') = rsetitems.keyid2  ").append(" AND coalesce( nullif( sdidatarelation.tokeyid3, ''), '(null)') = rsetitems.keyid3 AND sdidatarelation.relationtype = ").append(safeSQL.addVar(relationtype)).append(" AND rsetitems.rsetid = ").append(safeSQL.addVar(rsetid));
        sql.append(" union ");
        sql.append(commonSelect).append("sdiworkitemrelation");
        sql.append(commonWhere1).append(safeSQL.addVar(crosssdicalcdefid));
        sql.append(commonWhere2).append(" sdiworkitemrelation.workitemid = ds.sourceworkitemid AND sdiworkitemrelation.workiteminstance = ds.sourceworkiteminstance").append(" AND ccds.sdcid = sdiworkitemrelation.sdcid AND ccds.keyid1 = sdiworkitemrelation.keyid1").append(" AND ccds.keyid2 = coalesce( nullif( sdiworkitemrelation.keyid2, '' ),('null') ) AND ccds.keyid3 = coalesce( nullif( sdiworkitemrelation.keyid3, '' ), '(null)' )").append(" AND sdiworkitemrelation.tosdcid = rsetitems.sdcid AND sdiworkitemrelation.tokeyid1 = rsetitems.keyid1 AND coalesce(  nullif( sdiworkitemrelation.tokeyid2, '') , '(null)') = rsetitems.keyid2  ").append(" AND coalesce( nullif( sdiworkitemrelation.tokeyid3, ''), '(null)') = rsetitems.keyid3 AND sdiworkitemrelation.relationtype = ").append(safeSQL.addVar(relationtype)).append(" AND rsetitems.rsetid = ").append(safeSQL.addVar(rsetid));
        return sql.toString();
    }

    private String createAbsoluteQuery(int i, DataSet dsUniqCCD, StringBuffer commonSelect, StringBuffer commonWhere1, StringBuffer commonWhere2, SDCProcessor sdcProcessor, SafeSQL safeSQL) {
        StringBuilder sql = new StringBuilder();
        String crosssdicalcdefid = dsUniqCCD.getValue(i, "crosssdicalcdefid");
        String fromSdc = dsUniqCCD.getValue(i, "fromsdcid");
        String fromTable = sdcProcessor.getProperty(fromSdc, "tableid");
        String fromTableKeyColId1 = sdcProcessor.getProperty(fromSdc, "keycolid1");
        String fromTableKeyColId2 = sdcProcessor.getProperty(fromSdc, "keycolid2");
        String fromTableKeyColId3 = sdcProcessor.getProperty(fromSdc, "keycolid3");
        String commonSelectForAbsolute = commonSelect.toString();
        commonSelectForAbsolute = StringUtil.replaceAll(commonSelectForAbsolute, "rsetitems,", " ");
        sql.append(commonSelectForAbsolute).append(fromTable + " t ");
        sql.append(commonWhere1).append(safeSQL.addVar(crosssdicalcdefid));
        sql.append(commonWhere2).append(" ccds.sdcid = ").append(safeSQL.addVar(fromSdc)).append(" AND t.").append(fromTableKeyColId1).append("= ccds.keyid1 ");
        if (fromTableKeyColId2.length() > 0) {
            sql.append(" AND t.").append(fromTableKeyColId2).append(" = ccds.keyid2 ");
        }
        if (fromTableKeyColId3.length() > 0) {
            sql.append(" AND t.").append(fromTableKeyColId3).append(" = ccds.keyid3 ");
        }
        return sql.toString();
    }

    private int getRedoCalcSDISForQCBatch(DataSet updateDataitems, DataSet dsQCBDataItems, DataSet redoCalc, String currentbatchitemid, String qcBatchId, QueryProcessor qp) throws SapphireException {
        HashSet<String> refSDISet = new HashSet<String>();
        for (int d = 0; d < dsQCBDataItems.getRowCount(); ++d) {
            HashMap<String, Object> findMap = new HashMap<String, Object>();
            String sdcId = dsQCBDataItems.getValue(d, "sdcid");
            String keyid1 = dsQCBDataItems.getValue(d, "keyid1");
            String keyid2 = dsQCBDataItems.getValue(d, "keyid2");
            String keyid3 = dsQCBDataItems.getValue(d, "keyid3");
            String paramlistid = dsQCBDataItems.getValue(d, "paramlistid");
            String paramlistversionid = dsQCBDataItems.getValue(d, "paramlistversionid");
            String variantid = dsQCBDataItems.getValue(d, "variantid");
            String paramid = dsQCBDataItems.getValue(d, "paramid");
            String paramtype = dsQCBDataItems.getValue(d, "paramtype");
            String replicateid = dsQCBDataItems.getValue(d, "replicateid");
            String releasedflag = dsQCBDataItems.getValue(d, "releasedflag");
            String dataset = String.valueOf(dsQCBDataItems.getInt(d, "dataset"));
            String calcrule = dsQCBDataItems.getValue(d, "calcrule");
            if (OpalUtil.isNotEmpty(calcrule) && !this.isCalcRuleMatchingParamFoundForQCBatch(dsQCBDataItems, updateDataitems, d, currentbatchitemid, qcBatchId, qp)) continue;
            refSDISet.add(sdcId + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
            findMap.put("sdcid", sdcId);
            findMap.put("keyid1", keyid1);
            findMap.put("keyid2", keyid2);
            findMap.put("keyid3", keyid3);
            findMap.put("paramlistid", paramlistid);
            findMap.put("paramlistversionid", paramlistversionid);
            findMap.put("variantid", variantid);
            findMap.put("paramid", paramid);
            findMap.put("dataset", new BigDecimal(dataset));
            findMap.put("paramtype", paramtype);
            findMap.put("replicateid", new BigDecimal(replicateid));
            findMap.put("releasedflag", releasedflag);
            findMap.put("calcrule", calcrule);
            if (this.previouslyCalledRedoCalcSDIS.findRow(findMap) > -1 || redoCalc.findRow(findMap) > -1) continue;
            redoCalc.copyRow(dsQCBDataItems, d, 1);
        }
        return refSDISet.size();
    }

    private boolean isCalcRuleMatchingParamFoundForQCBatch(DataSet dsQCBDataItems, DataSet updatedDataItems, int d, String currentbatchitemid, String qcBatchId, QueryProcessor qp) throws SapphireException {
        String paramlist = dsQCBDataItems.getValue(d, "paramlistid");
        String paramlistVersion = dsQCBDataItems.getValue(d, "paramlistversionid");
        String paramlistVariant = dsQCBDataItems.getValue(d, "variantid");
        String paramlistDS = dsQCBDataItems.getValue(d, "dataset");
        String param = dsQCBDataItems.getValue(d, "paramid");
        String paramType = dsQCBDataItems.getValue(d, "paramtype");
        String replicateid = dsQCBDataItems.getValue(d, "replicateid");
        String referencedQCBatchitemid = dsQCBDataItems.getValue(d, "s_qcbatchitemid");
        String calcrule = dsQCBDataItems.getValue(d, "calcrule");
        String[] tokens = StringUtil.getExpressionTokens(calcrule);
        for (int token = 0; token < tokens.length; ++token) {
            String[] paramPartsArray;
            String linkedBatchItemId;
            String[] tokenParts = StringUtil.split(tokens[token], "|");
            String sdiPart = "";
            String paramlistPart = "";
            String paramPart = "";
            String sdiType = "";
            String sdiLinkDetails = "";
            if (tokenParts.length == 1) {
                String tokenPart;
                paramPart = tokenPart = tokenParts[0].trim();
                continue;
            }
            if (tokenParts.length == 2) {
                String tokenPart1 = tokenParts[0].trim();
                String tokenPart2 = tokenParts[1].trim();
                if ("aqc:qcparam".equalsIgnoreCase(tokenPart1)) {
                    sdiPart = "aqc:QCParam";
                    paramlistPart = "";
                } else if ("aqc:Linked".equalsIgnoreCase(tokenPart1)) {
                    sdiPart = "aqc:Linked";
                    paramlistPart = "";
                }
                paramPart = tokenPart2;
            } else if (tokenParts.length == 3) {
                sdiPart = tokenParts[0].trim();
                paramlistPart = tokenParts[1].trim();
                paramPart = tokenParts[2].trim();
            }
            String[] sdiParts = StringUtil.split(sdiPart, ":");
            sdiType = sdiParts.length == 2 ? sdiParts[0].trim() : "";
            String string = sdiLinkDetails = sdiParts.length == 2 ? sdiParts[1].trim() : sdiParts[0].trim();
            if (!sdiType.equalsIgnoreCase("aqc")) continue;
            QCBatch qcBatch = QCBatchPool.getQCBatch(qp, qcBatchId);
            QCBatchItem batchItem = qcBatch.getQCBatchItem(referencedQCBatchitemid);
            if (sdiLinkDetails.equalsIgnoreCase("Blank")) {
                String blankBatchItemId;
                QCBatchItem blankBatchItem = QCUtil.getBlankParent(qcBatch, batchItem);
                if (blankBatchItem != null && !(blankBatchItemId = blankBatchItem.getQCBatchItemID()).equals(currentbatchitemid)) {
                    continue;
                }
            } else if (sdiLinkDetails.equalsIgnoreCase("Linked") && (linkedBatchItemId = batchItem.getLinkedToBatchItemID()) != null && linkedBatchItemId.length() > 0 && linkedBatchItemId.equals(currentbatchitemid) && (paramlistPart == null || paramlistPart.length() == 0)) {
                paramlistPart = "*;*;*;*";
            }
            String ruleParamlist = "";
            String ruleParamlistVersion = "";
            String ruleParamlistVariant = "";
            String ruleParamlistDataset = "";
            String ruleParam = "";
            String ruleParamType = "";
            String ruleParamReplicate = "";
            String[] paramlistPartsArray = StringUtil.split(paramlistPart, ";");
            if (paramlistPartsArray.length > 0 && (ruleParamlist = paramlistPartsArray[0]).equals("#")) {
                ruleParamlist = paramlist;
            }
            if (paramlistPartsArray.length > 1 && (ruleParamlistVersion = paramlistPartsArray[1]).equals("#")) {
                ruleParamlistVersion = paramlistVersion;
            }
            if (paramlistPartsArray.length > 2 && (ruleParamlistVariant = paramlistPartsArray[2]).equals("#")) {
                ruleParamlistVariant = paramlistVariant;
            }
            if (paramlistPartsArray.length > 3 && (ruleParamlistDataset = paramlistPartsArray[3]).equals("#")) {
                ruleParamlistDataset = paramlistDS;
            }
            if ((paramPartsArray = StringUtil.split(paramPart, ";")).length > 0 && (ruleParam = paramPartsArray[0]).equals("#")) {
                ruleParam = param;
            }
            if (paramPartsArray.length > 1 && (ruleParamType = paramPartsArray[1]).equals("#")) {
                ruleParamType = paramType;
            }
            if (paramPartsArray.length > 2 && (ruleParamReplicate = paramPartsArray[2]).equals("#")) {
                ruleParamReplicate = replicateid;
            }
            HashMap<String, Object> findSourceDI = new HashMap<String, Object>();
            if (ruleParamlist.length() > 0 && !ruleParamlist.trim().equals("*")) {
                findSourceDI.put("paramlistid", ruleParamlist);
            }
            if (ruleParamlistVersion.length() > 0 && !ruleParamlistVersion.trim().equals("*")) {
                findSourceDI.put("paramlistversionid", ruleParamlistVersion);
            }
            if (ruleParamlistVariant.length() > 0 && !ruleParamlistVariant.trim().equals("*")) {
                findSourceDI.put("variantid", ruleParamlistVariant);
            }
            if (ruleParamlistDataset.length() > 0 && !ruleParamlistDataset.trim().equals("*")) {
                try {
                    findSourceDI.put("dataset", new BigDecimal(ruleParamlistDataset));
                }
                catch (Exception e) {
                    this.logger.warn("Invalid setting found in calcruleparams of ParamList");
                }
            }
            if (ruleParam.length() > 0 && !ruleParam.trim().equals("*")) {
                findSourceDI.put("paramid", ruleParam);
            }
            if (ruleParamType.length() > 0 && !ruleParamType.trim().equals("*")) {
                findSourceDI.put("paramtype", ruleParamType);
            }
            if (ruleParamReplicate.length() > 0 && !ruleParamReplicate.trim().equals("*")) {
                try {
                    findSourceDI.put("replicateid", new BigDecimal(ruleParamReplicate));
                }
                catch (Exception e) {
                    this.logger.warn("Invalid setting found in calcruleparams of ParamList");
                }
            }
            if (findSourceDI.size() > 0) {
                if (updatedDataItems.findRow(findSourceDI) < 0) continue;
                return true;
            }
            return true;
        }
        return false;
    }
}

