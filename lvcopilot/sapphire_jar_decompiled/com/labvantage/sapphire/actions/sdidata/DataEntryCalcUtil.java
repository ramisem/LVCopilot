/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.opal.qcbatch.QCBatch;
import com.labvantage.opal.qcbatch.QCBatchItem;
import com.labvantage.opal.qcbatch.QCBatchPool;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataEntryAction;
import com.labvantage.sapphire.actions.sdidata.DataEntryLimitsUtil;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.calculations.CalcReport;
import com.labvantage.sapphire.util.evaluator.ExpressionUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataEntryCalcUtil {
    private BaseSDIDataEntryAction baseSDIDataEntryAction;
    private SDCProcessor sdcProcessor;
    private QueryProcessor queryProcessor;
    private ConfigurationProcessor configurationProcessor;
    private DBAccess database;
    private Logger logger;
    private ConnectionInfo connectionInfo;
    private FormatUtil systemformatutil = FormatUtil.getInstance();
    private DateTimeUtil systemdtu = new DateTimeUtil();
    private int calcKeySet;
    String sdiPart;
    String paramlistPart;
    String paramPart;
    String primaryPart;
    String ArrayMethodPart;
    boolean isUpdateReleasedCalcs;
    private HashMap<String, String> keySetCache = new HashMap();
    private Set<String> datasetFilterKeysRetrieved = new HashSet<String>();
    private final int ROUND_QUICKTEST = 0;
    private final int ROUND_THISSDI = 1;
    private final int ROUND_OTHERSDI = 2;
    public static final String COL_INITIALKEYSETFLAG = "_initialkeyset";
    public static final String CALC_ALT_DELIMITER = "::";
    public static final String ERROR_ATTRIBUTENOTFOUND = "MissingAttribute";
    boolean honorNullInRelationalExpressions;
    boolean honorNullInANDORConditionals;
    boolean alwaysReturnNullForTextTypes;
    boolean includeCancelledDatasets;
    public static final String POLICY_HONORNULLINRELATIONALEXPRESSIONS = "honornullinrelationalexpressions";
    public static final String POLICY_HONORNULLINANDORCONDITIONALS = "honornullinandorconditionals";
    public static final String POLICY_RETURNFOREMPTYTEXTDATATYPE = "returnforemptytextdatatype";
    public static final String POLICY_EXCLUDECANCELLEDDATASETS = "excludecancelleddataset";
    public static final String POLICY_RETURNFOREMPTYTEXTDATATYPE_ALWAYSRETURNNULL = "Always Return Null";
    public static final String POLICY_RETURNFOREMPTYTEXTDATATYPE_ALWAYSRETURNEMPTYSTRING = "Always Return Empty String";
    private HashMap<String, DataSet> dataitemAttributes = new HashMap();
    private HashMap<String, DataSet> datasetAttributes = new HashMap();
    private DataSet primaryAttributes = new DataSet();
    private HashMap<String, DataSet> sdcColumnDatas = new HashMap();
    private HashMap<String, PropertyList> sdcPropertyList = new HashMap();
    private DataSet allValidDataentryAttributes;
    private String[] datasetKeys = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset"};
    private String[] dataitemKeys = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid"};
    private boolean isFormulationCalc = false;
    private boolean isExtraCalc = false;

    public DataEntryCalcUtil(BaseSDIDataEntryAction baseSDIDataEntryAction, SDCProcessor sdcProcessor, QueryProcessor queryProcessor, ConfigurationProcessor configurationProcessor, DBAccess database, Logger logger, ConnectionInfo connectionInfo) {
        this.baseSDIDataEntryAction = baseSDIDataEntryAction;
        this.sdcProcessor = sdcProcessor;
        this.queryProcessor = queryProcessor;
        this.configurationProcessor = configurationProcessor;
        this.database = database;
        this.logger = logger;
        this.connectionInfo = connectionInfo;
    }

    void performCalcs(DataSet primary, DataSet sdidata, DataSet sdidataitems, DataSet sdidataitemlimits, boolean calcsOnly, boolean calcReportOnly, boolean isLiveLimitChecking, DataSet sdidatarelationOverride, boolean hasCalcExcludeChange, boolean calculateModifiedDatasetsOnly, boolean calculateModifiedTestsOnly, boolean isArrayCalc, boolean isFormulationCalc, boolean isExtraCalc, DataSet arrayitems) throws SapphireException {
        boolean valueEntered;
        long stTime = System.currentTimeMillis();
        this.logger.info("CALC_PERFORMANCE: calculation started");
        this.isFormulationCalc = isFormulationCalc;
        this.isExtraCalc = isExtraCalc;
        this.isUpdateReleasedCalcs = this.configurationProcessor.getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("updatereleasedcalcs", "Y").equals("Y");
        this.honorNullInRelationalExpressions = this.configurationProcessor.getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty(POLICY_HONORNULLINRELATIONALEXPRESSIONS, "N").equals("Y");
        this.honorNullInANDORConditionals = this.configurationProcessor.getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty(POLICY_HONORNULLINANDORCONDITIONALS, "N").equals("Y");
        this.alwaysReturnNullForTextTypes = this.configurationProcessor.getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty(POLICY_RETURNFOREMPTYTEXTDATATYPE, POLICY_RETURNFOREMPTYTEXTDATATYPE_ALWAYSRETURNNULL).equals(POLICY_RETURNFOREMPTYTEXTDATATYPE_ALWAYSRETURNNULL);
        this.includeCancelledDatasets = this.configurationProcessor.getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty(POLICY_EXCLUDECANCELLEDDATASETS, "Y").equals("N");
        sdidataitems.sort("sdcid, keyid1, keyid2, keyid3, paramid, paramtype, dataset, replicateid");
        sdidataitems.addColumn("_hascalc", 0);
        sdidataitems.addColumn("_rownumber", 1);
        sdidataitems.setString(-1, "_recalcrequired", "Y");
        sdidataitems.addColumn("_calckeyset", 0);
        sdidata.addColumn("_calckeyset", 0);
        sdidataitems.setString(-1, COL_INITIALKEYSETFLAG, "Y");
        sdidata.setString(-1, COL_INITIALKEYSETFLAG, "Y");
        this.allValidDataentryAttributes = this.queryProcessor.getSqlDataSet("SELECT distinct attributeid FROM sdcattributedef WHERE sdcid='DataSet' OR sdcid='SDIWorkItem'");
        int rows = sdidataitems.getRowCount();
        int totalcalcs = 2;
        HashSet<String> modifiedDataSets = new HashSet<String>();
        for (int row = 0; row < rows; ++row) {
            if (!sdidataitems.isNull(row, "calcrule") && !sdidataitems.getValue(row, "_hascalc").equals("N")) {
                sdidataitems.setString(row, "_hascalc", "Y");
                ++totalcalcs;
            }
            sdidataitems.setNumber(row, "_rownumber", row);
            if (!calculateModifiedDatasetsOnly && !calculateModifiedTestsOnly || !sdidataitems.getString(row, "_modified").equals("Y")) continue;
            modifiedDataSets.add(sdidataitems.getString(row, "sdcid") + ";" + sdidataitems.getString(row, "keyid1") + ";" + sdidataitems.getString(row, "keyid2") + ";" + sdidataitems.getString(row, "keyid3") + ";" + sdidataitems.getString(row, "paramlistid") + ";" + sdidataitems.getString(row, "paramlistversionid") + ";" + sdidataitems.getString(row, "variantid") + ";" + sdidataitems.getBigDecimal(row, "dataset"));
        }
        if (calculateModifiedTestsOnly) {
            HashMap<String, Object> filter;
            String[] parts;
            HashSet<String> modifiedTests = new HashSet<String>();
            for (String modifiedDataSet : modifiedDataSets) {
                parts = StringUtil.split(modifiedDataSet, ";");
                filter = new HashMap<String, Object>();
                filter.put("sdcid", parts[0]);
                filter.put("keyid1", parts[1]);
                filter.put("keyid2", parts[2]);
                filter.put("keyid3", parts[3]);
                filter.put("paramlistid", parts[4]);
                filter.put("paramlistversionid", parts[5]);
                filter.put("variantid", parts[6]);
                filter.put("dataset", new BigDecimal(parts[7]));
                int row = sdidata.findRow(filter);
                if (row < 0) continue;
                String sourceworkitemid = sdidata.getValue(row, "sourceworkitemid");
                String sourceworkiteminstance = sdidata.getValue(row, "sourceworkiteminstance");
                if (sourceworkitemid.length() <= 0 || sourceworkiteminstance.length() <= 0) continue;
                modifiedTests.add(parts[0] + ";" + parts[1] + ";" + parts[2] + ";" + parts[3] + ";" + sourceworkitemid + ";" + sourceworkiteminstance);
            }
            for (String modifiedTest : modifiedTests) {
                parts = StringUtil.split(modifiedTest, ";");
                filter = new HashMap();
                filter.put("sdcid", parts[0]);
                filter.put("keyid1", parts[1]);
                filter.put("keyid2", parts[2]);
                filter.put("keyid3", parts[3]);
                filter.put("sourceworkitemid", parts[4]);
                filter.put("sourceworkiteminstance", new BigDecimal(parts[5]));
                DataSet datasetsInTest = sdidata.getFilteredDataSet(filter);
                for (int row = 0; row < datasetsInTest.size(); ++row) {
                    modifiedDataSets.add(parts[0] + ";" + parts[1] + ";" + parts[2] + ";" + parts[3] + ";" + datasetsInTest.getString(row, "paramlistid") + ";" + datasetsInTest.getString(row, "paramlistversionid") + ";" + datasetsInTest.getString(row, "variantid") + ";" + datasetsInTest.getBigDecimal(row, "dataset"));
                }
            }
        }
        boolean arrayCalcOnly = false;
        int calcloopcount = 0;
        this.calcKeySet = 0;
        do {
            valueEntered = false;
            ++calcloopcount;
            String initsdcid = "";
            String initkeyid1 = "";
            String initkeyid2 = "";
            String initkeyid3 = "";
            HashMap<String, String> calcFilter = new HashMap<String, String>();
            calcFilter.put("_hascalc", "Y");
            calcFilter.put("_recalcrequired", "Y");
            ArrayList<Integer> rowNumberList = new ArrayList<Integer>();
            ArrayList<String> enterValueList = new ArrayList<String>();
            ArrayList<String> valueStatusList = new ArrayList<String>();
            PropertyList sdcProps = null;
            HashMap<String, String> modifiedFilter = new HashMap<String, String>();
            modifiedFilter.put("_modified", "Y");
            DataSet modifiedDataItems = sdidataitems.getFilteredDataSet(modifiedFilter);
            this.logger.info("CALC_PERFORMANCE: no of sdidataitems rows " + rows);
            this.logger.info("CALC_PERFORMANCE: no of modifiedDataItems rows " + modifiedDataItems.getRowCount());
            for (int row = 0; row < rows; ++row) {
                if (!isExtraCalc && (!isFormulationCalc || "N".equalsIgnoreCase(sdidataitems.getString(row, "_hascalc", "Y"))) && !sdidataitems.getString(row, "_modified", "N").equals("Y") || sdidataitems.getString(row, "keyid1").equals(initkeyid1) && sdidataitems.getString(row, "keyid2").equals(initkeyid2) && sdidataitems.getString(row, "keyid3").equals(initkeyid3) && sdidataitems.getString(row, "sdcid").equals(initsdcid)) continue;
                initsdcid = sdidataitems.getString(row, "sdcid");
                initkeyid1 = sdidataitems.getString(row, "keyid1");
                initkeyid2 = sdidataitems.getString(row, "keyid2");
                initkeyid3 = sdidataitems.getString(row, "keyid3");
                if (sdcProps == null || !initsdcid.equals(sdcProps.getProperty("sdcid"))) {
                    sdcProps = this.sdcProcessor.getPropertyList(initsdcid);
                }
                arrayCalcOnly = isArrayCalc;
                calcFilter.put("sdcid", initsdcid);
                calcFilter.put("keyid1", initkeyid1);
                calcFilter.put("keyid2", initkeyid2);
                calcFilter.put("keyid3", initkeyid3);
                DataSet dscalcs = sdidataitems.getFilteredDataSet(calcFilter);
                int docalcs = dscalcs.getRowCount();
                this.logger.info("CALC_PERFORMANCE: no of calc DataItems rows for calcloop no " + calcloopcount + ": " + docalcs);
                HashMap<String, String> modifiedForSDIFilter = new HashMap<String, String>();
                modifiedForSDIFilter.put("sdcid", initsdcid);
                modifiedForSDIFilter.put("keyid1", initkeyid1);
                modifiedForSDIFilter.put("keyid2", initkeyid2);
                modifiedForSDIFilter.put("keyid3", initkeyid3);
                DataSet modifiedDataItemsForSDI = modifiedDataItems.getFilteredDataSet(modifiedForSDIFilter);
                this.logger.info("CALC_PERFORMANCE: no of modifiedDataItems rows for current SDI ( " + initsdcid + ": " + initkeyid1 + " ) " + modifiedDataItemsForSDI.getRowCount());
                int actualCalcProcessed = 0;
                for (int calc = 0; calc < docalcs; ++calc) {
                    int startround;
                    String calcrule = dscalcs.getString(calc, "calcrule");
                    int rowNumber = dscalcs.getInt(calc, "_rownumber");
                    boolean hasParamFound = false;
                    boolean isPrimaryOrDatasetToken = false;
                    boolean hasThisSDITokenFound = false;
                    String sdcid = dscalcs.getString(calc, "sdcid");
                    String keyid1 = dscalcs.getString(calc, "keyid1");
                    String keyid2 = dscalcs.getString(calc, "keyid2");
                    String keyid3 = dscalcs.getString(calc, "keyid3");
                    String paramlistid = dscalcs.getString(calc, "paramlistid");
                    String paramlistversionid = dscalcs.getString(calc, "paramlistversionid");
                    String variantid = dscalcs.getString(calc, "variantid");
                    BigDecimal dataset = dscalcs.getBigDecimal(calc, "dataset");
                    String paramid = dscalcs.getString(calc, "paramid");
                    String paramtype = dscalcs.getString(calc, "paramtype");
                    BigDecimal replicateid = dscalcs.getBigDecimal(calc, "replicateid");
                    if ((calculateModifiedDatasetsOnly || calculateModifiedTestsOnly) && !modifiedDataSets.contains(sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + paramlistid + ";" + paramlistversionid + ";" + variantid + ";" + dataset)) continue;
                    ++actualCalcProcessed;
                    String[] tokens = StringUtil.getExpressionTokens(calcrule);
                    boolean calcReportRequired = dscalcs.getValue(calc, "_calcreportrequired").equals("Y");
                    CalcReport calcReport = new CalcReport(tokens.length);
                    if (calcReportRequired) {
                        calcReport.sdcid = sdcid;
                        calcReport.keyid1 = keyid1;
                        calcReport.keyid2 = keyid2;
                        calcReport.keyid3 = keyid3;
                        calcReport.paramlistid = paramlistid;
                        calcReport.paramlistversionid = paramlistversionid;
                        calcReport.variantid = variantid;
                        calcReport.dataset = dataset;
                        calcReport.paramid = paramid;
                        calcReport.paramtype = paramtype;
                        calcReport.replicateid = replicateid;
                        calcReport.calcRule = calcrule;
                    }
                    this.logger.info("Evaluating calc rule (" + this.truncateCalcRule(calcrule) + ") for " + keyid1 + "/" + paramlistid + "/" + paramid);
                    String valueStatus = "";
                    boolean quickCheckProceed = false;
                    boolean hasModifiedArguments = false;
                    boolean hasCalcOfCalcs = false;
                    HashMap<String, Object> expressionParams = new HashMap<String, Object>();
                    for (int round = startround = calcReportRequired || calcsOnly || arrayCalcOnly || isFormulationCalc || isExtraCalc ? 1 : 0; round <= 2; ++round) {
                        for (int token = 0; !(token >= tokens.length || round == 0 && quickCheckProceed); ++token) {
                            int k;
                            BigDecimal filterReplicateid;
                            String filterParamType;
                            String filterParamId;
                            boolean thisSDI;
                            boolean maxReplicate = false;
                            boolean maxDataset = false;
                            boolean hasWildcard = false;
                            boolean isDatasetFilter = false;
                            String datasetFilter = "";
                            boolean multiSDIs = false;
                            boolean isDataSetColOrAttribute = false;
                            boolean isPrimaryColOrAttribute = false;
                            String primaryColumn = "";
                            HashMap<String, Object> diTokenFilter = new HashMap<String, Object>();
                            HashMap<String, Object> dsTokenFilter = new HashMap<String, Object>();
                            diTokenFilter.put("calcexcludeflag", "N");
                            String[] tokenParts = StringUtil.split(tokens[token], "|");
                            this.sdiPart = "";
                            this.paramlistPart = "";
                            this.paramPart = "";
                            if (tokenParts.length == 1) {
                                String tokenPart = tokenParts[0].trim();
                                if (tokenPart.startsWith("primary:")) {
                                    isPrimaryColOrAttribute = true;
                                    this.primaryPart = tokenPart;
                                } else {
                                    this.paramPart = tokenPart;
                                }
                            } else if (tokenParts.length == 2) {
                                String tokenPart1 = tokenParts[0].trim();
                                String tokenPart2 = tokenParts[1].trim();
                                if (tokenPart2.startsWith("primary:")) {
                                    isPrimaryColOrAttribute = true;
                                    this.sdiPart = tokenPart1;
                                    this.primaryPart = tokenPart2;
                                } else {
                                    this.paramlistPart = tokenPart1;
                                    this.paramPart = tokenPart2;
                                    if ("aqc:qcparam".equalsIgnoreCase(this.paramlistPart)) {
                                        this.sdiPart = "aqc:QCParam";
                                        this.paramlistPart = "";
                                    } else if ("aqc:Linked".equalsIgnoreCase(this.paramlistPart)) {
                                        this.sdiPart = "aqc:Linked";
                                        this.paramlistPart = "";
                                    }
                                }
                            } else if (tokenParts.length == 3) {
                                this.sdiPart = tokenParts[0].trim();
                                if (this.sdiPart.startsWith("Array")) {
                                    this.ArrayMethodPart = tokenParts[1].trim();
                                    this.paramlistPart = "ArrayData;*;*;*";
                                    this.paramPart = tokenParts[2].trim();
                                } else {
                                    this.paramlistPart = tokenParts[1].trim();
                                    this.paramPart = tokenParts[2].trim();
                                }
                            }
                            boolean bl = thisSDI = this.sdiPart.length() == 0;
                            if (thisSDI) {
                                hasThisSDITokenFound = true;
                            }
                            if (!isPrimaryOrDatasetToken) {
                                boolean bl2 = isPrimaryOrDatasetToken = this.primaryPart != null && this.primaryPart.length() > 0;
                            }
                            if ((!thisSDI || round != 0) && (!thisSDI || round != 1 || !quickCheckProceed && !calcReportRequired && !calcsOnly && !arrayCalcOnly && !isFormulationCalc && !isExtraCalc) && (thisSDI || round != 2 || !hasModifiedArguments && !calcReportRequired && !calcsOnly && !arrayCalcOnly && !isFormulationCalc && !isExtraCalc)) continue;
                            if (thisSDI) {
                                diTokenFilter.put("sdcid", sdcid);
                                diTokenFilter.put("keyid1", keyid1);
                                diTokenFilter.put("keyid2", keyid2);
                                diTokenFilter.put("keyid3", keyid3);
                                diTokenFilter.put(COL_INITIALKEYSETFLAG, "Y");
                            } else {
                                String[] subPLParts = StringUtil.split(this.paramlistPart, ";");
                                isDatasetFilter = subPLParts.length == 4 && (subPLParts[3].indexOf("=") >= 0 || subPLParts[3].indexOf("<>") >= 0);
                                String[] sdiParts = StringUtil.split(this.sdiPart, ":");
                                String sdiType = "";
                                String sdiLinkDetails = "";
                                String arrayItemRepeatCount = "";
                                String arrayItemDilFactor = "";
                                String arrayZone = null;
                                String arrayRowLabel = null;
                                String arrayColumnLabel = null;
                                if (this.sdiPart.startsWith("Array")) {
                                    this.sdiPart = sdiParts[0];
                                    for (int p = 1; p < sdiParts.length; ++p) {
                                        String[] data = StringUtil.split(sdiParts[p], "=");
                                        if (data.length != 2) continue;
                                        if ("Repeat".equalsIgnoreCase(data[0])) {
                                            arrayItemRepeatCount = data[1];
                                            continue;
                                        }
                                        if ("Dilution".equalsIgnoreCase(data[0])) {
                                            arrayItemDilFactor = data[1];
                                            continue;
                                        }
                                        if ("Zone".equalsIgnoreCase(data[0])) {
                                            arrayZone = data[1];
                                            continue;
                                        }
                                        if ("Row".equalsIgnoreCase(data[0])) {
                                            arrayRowLabel = data[1];
                                            continue;
                                        }
                                        if (!"Column".equalsIgnoreCase(data[0])) continue;
                                        arrayColumnLabel = data[1];
                                    }
                                    sdiType = "Array";
                                } else {
                                    sdiType = sdiParts.length == 2 ? sdiParts[0].trim() : "datarelation";
                                    sdiLinkDetails = sdiParts.length == 2 ? sdiParts[1].trim() : sdiParts[0].trim();
                                }
                                this.logger.debug("Parsing calc for " + sdiType);
                                if (sdiType.equals("sdidatarelation") || sdiType.equals("datarelation")) {
                                    multiSDIs = this.crossCalcSDIDataRelation(sdidata, sdidataitems, sdidataitemlimits, sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, isDatasetFilter, diTokenFilter, sdiLinkDetails, sdidatarelationOverride);
                                } else if (sdiType.equals("sdirelation") || sdiType.equals("reversesdirelation")) {
                                    multiSDIs = this.crossCalcSDIRelation(sdidata, sdidataitems, sdidataitemlimits, sdcid, keyid1, keyid2, keyid3, isDatasetFilter, diTokenFilter, sdiLinkDetails, sdiType, isPrimaryColOrAttribute, primary);
                                } else if (sdiType.equals("sdi")) {
                                    multiSDIs = this.crossCalcSDI(sdidata, sdidataitems, sdidataitemlimits, sdcid, isDatasetFilter, diTokenFilter, sdiLinkDetails);
                                } else if (sdiType.equals("link")) {
                                    multiSDIs = this.crossCalcLink(primary, sdidata, sdidataitems, sdidataitemlimits, this.sdcProcessor, sdcProps, sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, tokens[token], isDatasetFilter, isPrimaryColOrAttribute, diTokenFilter, sdiLinkDetails);
                                } else if (sdiType.equalsIgnoreCase("aqc")) {
                                    try {
                                        multiSDIs = this.crossCalcAQC(sdidata, sdidataitems, sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, diTokenFilter, sdiLinkDetails, primary);
                                    }
                                    catch (Exception e) {
                                        valueStatus = "CALC_PARAM_ERROR";
                                        String errorMsg = "CALC_PARAM_ERROR: " + e.getMessage();
                                        this.logger.info(errorMsg);
                                        diTokenFilter.put("dummy", "dummy");
                                    }
                                } else if (sdiType.equalsIgnoreCase("Sample")) {
                                    multiSDIs = this.crossCalcSample(sdidata, sdidataitems, sdidataitemlimits, keyid1, diTokenFilter, sdiLinkDetails, isDatasetFilter, isPrimaryColOrAttribute, primary);
                                } else if (sdiType.equalsIgnoreCase("array")) {
                                    multiSDIs = "ArrayItem".equalsIgnoreCase(this.sdiPart) ? ("LV_ArrayZone".equals(sdcid) ? this.crossCalcArrayItemZone(sdidata, sdidataitems, sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, diTokenFilter, arrayZone, arrayRowLabel, arrayColumnLabel, arrayitems) : this.crossCalcArrayItem(sdidata, sdidataitems, sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, diTokenFilter, arrayZone, arrayRowLabel, arrayColumnLabel, arrayitems)) : this.crossCalcArray(sdidataitems, sdcid, keyid1, keyid2, keyid3, diTokenFilter, arrayItemRepeatCount, arrayItemDilFactor, replicateid);
                                } else {
                                    this.logger.info("CALC_PARAM_ERROR: Did not recognize the type for " + tokens[token]);
                                    diTokenFilter.put("dummy", "dummy");
                                }
                            }
                            if (this.paramlistPart.length() == 0) {
                                diTokenFilter.put("paramlistid", paramlistid);
                                diTokenFilter.put("paramlistversionid", paramlistversionid);
                                diTokenFilter.put("variantid", variantid);
                                diTokenFilter.put("dataset", dataset);
                            } else {
                                String filterVariantid;
                                String filterParamlistVersionid;
                                String filterParamlistid;
                                String[] subparts = StringUtil.split(this.paramlistPart, ";");
                                if (subparts.length == 1) {
                                    filterParamlistid = this.baseSDIDataEntryAction.getParamlistid(subparts[0].trim(), paramlistid);
                                    if (filterParamlistid != null) {
                                        diTokenFilter.put("paramlistid", filterParamlistid);
                                    }
                                    diTokenFilter.put("dataset", dataset);
                                    hasWildcard |= "*".equals(subparts[0].trim());
                                } else if (subparts.length == 2) {
                                    filterParamlistid = this.baseSDIDataEntryAction.getParamlistid(subparts[0].trim(), paramlistid);
                                    if (filterParamlistid != null) {
                                        diTokenFilter.put("paramlistid", filterParamlistid);
                                    }
                                    if ((filterParamlistVersionid = this.baseSDIDataEntryAction.getParamlistVersionid(subparts[1].trim(), paramlistversionid)) != null) {
                                        diTokenFilter.put("paramlistversionid", filterParamlistVersionid);
                                    }
                                    diTokenFilter.put("dataset", dataset);
                                    hasWildcard |= "*".equals(subparts[0].trim()) || "*".equals(subparts[1].trim());
                                } else if (subparts.length == 3) {
                                    filterParamlistid = this.baseSDIDataEntryAction.getParamlistid(subparts[0].trim(), paramlistid);
                                    if (filterParamlistid != null) {
                                        diTokenFilter.put("paramlistid", filterParamlistid);
                                    }
                                    if ((filterParamlistVersionid = this.baseSDIDataEntryAction.getParamlistVersionid(subparts[1].trim(), paramlistversionid)) != null) {
                                        diTokenFilter.put("paramlistversionid", filterParamlistVersionid);
                                    }
                                    if ((filterVariantid = this.baseSDIDataEntryAction.getVariantid(subparts[2].trim(), variantid)) != null) {
                                        diTokenFilter.put("variantid", filterVariantid);
                                    }
                                    diTokenFilter.put("dataset", dataset);
                                    hasWildcard |= "*".equals(subparts[0].trim()) || "*".equals(subparts[1].trim()) || "*".equals(subparts[2].trim());
                                } else if (subparts.length == 4) {
                                    BigDecimal filterDataset;
                                    filterParamlistid = this.baseSDIDataEntryAction.getParamlistid(subparts[0].trim(), paramlistid);
                                    if (filterParamlistid != null) {
                                        diTokenFilter.put("paramlistid", filterParamlistid);
                                    }
                                    if ((filterParamlistVersionid = this.baseSDIDataEntryAction.getParamlistVersionid(subparts[1].trim(), paramlistversionid)) != null) {
                                        diTokenFilter.put("paramlistversionid", filterParamlistVersionid);
                                    }
                                    if ((filterVariantid = this.baseSDIDataEntryAction.getVariantid(subparts[2].trim(), variantid)) != null) {
                                        diTokenFilter.put("variantid", filterVariantid);
                                    }
                                    if ((filterDataset = this.baseSDIDataEntryAction.getDataSet(subparts[3].trim(), dataset)) != null) {
                                        diTokenFilter.put("dataset", filterDataset);
                                    }
                                    hasWildcard |= "*".equals(subparts[0].trim()) || "*".equals(subparts[1].trim()) || "*".equals(subparts[2].trim()) || "*".equals(subparts[3].trim());
                                    maxDataset = subparts[3].trim().equals("max");
                                    boolean bl3 = isDatasetFilter = subparts[3].trim().indexOf("=") >= 0 || subparts[3].trim().indexOf("<>") >= 0;
                                    if (isDatasetFilter) {
                                        datasetFilter = subparts[3].trim();
                                    }
                                }
                            }
                            String valueColumn = "transformvalue";
                            String datasetColumn = "";
                            String altValue = "";
                            if (this.paramPart.contains(CALC_ALT_DELIMITER)) {
                                int pos = this.paramPart.indexOf(CALC_ALT_DELIMITER);
                                altValue = this.paramPart.substring(pos + CALC_ALT_DELIMITER.length());
                                this.paramPart = this.paramPart.substring(0, pos);
                            }
                            String[] paramparts = StringUtil.split(this.paramPart, ";");
                            String limitTypeId = "";
                            if (paramparts.length == 1) {
                                filterParamId = this.baseSDIDataEntryAction.getParamId(paramparts[0].trim(), paramid);
                                if (filterParamId != null) {
                                    diTokenFilter.put("paramid", filterParamId);
                                }
                                hasWildcard |= "*".equals(paramparts[0].trim());
                            } else if (paramparts.length == 2) {
                                filterParamId = this.baseSDIDataEntryAction.getParamId(paramparts[0].trim(), paramid);
                                if (filterParamId != null) {
                                    diTokenFilter.put("paramid", filterParamId);
                                }
                                if ((filterParamType = this.baseSDIDataEntryAction.getParamType(paramparts[1].trim(), paramtype)) != null) {
                                    diTokenFilter.put("paramtype", filterParamType);
                                }
                                hasWildcard |= "*".equals(paramparts[0].trim()) || "*".equals(paramparts[1].trim());
                            } else if (paramparts.length == 3) {
                                filterParamId = this.baseSDIDataEntryAction.getParamId(paramparts[0].trim(), paramid);
                                if (filterParamId != null) {
                                    diTokenFilter.put("paramid", filterParamId);
                                }
                                if ((filterParamType = this.baseSDIDataEntryAction.getParamType(paramparts[1].trim(), paramtype)) != null) {
                                    diTokenFilter.put("paramtype", filterParamType);
                                }
                                if ((filterReplicateid = this.baseSDIDataEntryAction.getReplicate(paramparts[2].trim(), replicateid)) != null) {
                                    diTokenFilter.put("replicateid", filterReplicateid);
                                }
                                hasWildcard |= "*".equals(paramparts[0].trim()) || "*".equals(paramparts[1].trim()) || "*".equals(paramparts[2].trim());
                                maxReplicate = paramparts[2].trim().equals("max");
                            } else if (paramparts.length == 4) {
                                filterParamId = this.baseSDIDataEntryAction.getParamId(paramparts[0].trim(), paramid);
                                if (filterParamId != null) {
                                    diTokenFilter.put("paramid", filterParamId);
                                }
                                if ((filterParamType = this.baseSDIDataEntryAction.getParamType(paramparts[1].trim(), paramtype)) != null) {
                                    diTokenFilter.put("paramtype", filterParamType);
                                }
                                if ((filterReplicateid = this.baseSDIDataEntryAction.getReplicate(paramparts[2].trim(), replicateid)) != null) {
                                    diTokenFilter.put("replicateid", filterReplicateid);
                                }
                                hasWildcard |= "*".equals(paramparts[0].trim()) || "*".equals(paramparts[1].trim()) || "*".equals(paramparts[2].trim());
                                maxReplicate = paramparts[2].trim().equals("max");
                                valueColumn = paramparts[3].trim();
                                if (paramparts[3].toUpperCase().startsWith("PARAMLIMIT")) {
                                    limitTypeId = StringUtil.split(paramparts[3], ":")[1];
                                }
                            }
                            if (round == 0) {
                                DataSet paramValues = modifiedDataItemsForSDI.getFilteredDataSet(diTokenFilter);
                                if (paramValues.size() > 0) {
                                    quickCheckProceed = true;
                                    hasParamFound = true;
                                    continue;
                                }
                                if (hasParamFound) continue;
                                boolean bl4 = hasParamFound = sdidataitems.getFilteredDataSet(diTokenFilter).size() > 0;
                                if (hasParamFound || isPrimaryOrDatasetToken || paramparts.length != 1) continue;
                                isPrimaryOrDatasetToken = true;
                                continue;
                            }
                            CalcReport.Token calcReportToken = calcReport.createToken(token);
                            calcReportToken.tokenid = tokens[token];
                            if (isPrimaryColOrAttribute) {
                                String[] subparts = StringUtil.split(this.primaryPart, ":");
                                if (subparts.length == 2 && subparts[1].length() > 0) {
                                    primaryColumn = subparts[1];
                                    try {
                                        this.resolvePrimaryColumnOrAttribute(tokens[token], expressionParams, calcReportToken, primaryColumn, diTokenFilter, primary);
                                    }
                                    catch (SapphireException e) {
                                        valueStatus = "CALC_PARAM_ERROR";
                                        this.logger.info("CALC_PARAM_ERROR: " + e.getMessage());
                                        calcReportToken.setComment("Error: " + e.getMessage());
                                    }
                                    continue;
                                }
                                valueStatus = "CALC_PARAM_ERROR";
                                String errorMsg = "CALC_PARAM_ERROR: Invalid token (" + tokens[token] + ") in " + paramid + " for " + keyid1;
                                this.logger.info(errorMsg);
                                calcReportToken.setComment("Error: " + errorMsg);
                                continue;
                            }
                            DataSet paramValues = sdidataitems.getFilteredDataSet(diTokenFilter);
                            DataSet paramLimits = new DataSet();
                            if (limitTypeId.length() > 0) {
                                HashMap<String, Object> limitFilter = new HashMap<String, Object>();
                                for (k = 0; k < this.dataitemKeys.length; ++k) {
                                    if (!diTokenFilter.containsKey(this.dataitemKeys[k])) continue;
                                    limitFilter.put(this.dataitemKeys[k], diTokenFilter.get(this.dataitemKeys[k]));
                                }
                                limitFilter.put("limittypeid", limitTypeId);
                                paramLimits = sdidataitemlimits.getFilteredDataSet(limitFilter);
                            }
                            DataSet dataSetValues = new DataSet();
                            if (paramValues.getRowCount() == 0 && paramparts.length == 1 && (sdidata.isValidColumn(paramparts[0].trim()) || this.isValidDataEntryAttribute(paramparts[0].trim()))) {
                                for (k = 0; k < this.datasetKeys.length; ++k) {
                                    if (!diTokenFilter.containsKey(this.datasetKeys[k])) continue;
                                    dsTokenFilter.put(this.datasetKeys[k], diTokenFilter.get(this.datasetKeys[k]));
                                }
                                dataSetValues = sdidata.getFilteredDataSet(dsTokenFilter);
                                isDataSetColOrAttribute = true;
                                datasetColumn = paramparts[0].trim();
                            }
                            if (isDatasetFilter) {
                                HashMap<String, Object> filter = new HashMap<String, Object>();
                                filter.put("sdcid", sdcid);
                                filter.put("keyid1", keyid1);
                                filter.put("keyid2", keyid2);
                                filter.put("keyid3", keyid3);
                                filter.put("paramlistid", paramlistid);
                                filter.put("paramlistversionid", paramlistversionid);
                                filter.put("variantid", variantid);
                                filter.put("dataset", dataset);
                                try {
                                    if (isDataSetColOrAttribute) {
                                        this.filterDataSet(dataSetValues, sdidata, datasetFilter, calcReportRequired, calcReportToken, filter, paramid);
                                    } else {
                                        this.filterDataSet(paramValues, sdidata, datasetFilter, calcReportRequired, calcReportToken, filter, paramid);
                                    }
                                }
                                catch (SapphireException e) {
                                    valueStatus = "CALC_PARAM_ERROR";
                                    this.logger.info("CALC_PARAM_ERROR: " + e.getMessage());
                                    calcReportToken.setComment("Error: " + e.getMessage());
                                }
                            }
                            int paramValueRows = 0;
                            int datasetValueRows = 0;
                            if (isDataSetColOrAttribute) {
                                datasetValueRows = dataSetValues.getRowCount();
                                if (datasetValueRows == 0) {
                                    if (altValue.length() > 0) {
                                        this.useAltValue(tokens[token], expressionParams, calcReportToken, altValue);
                                        continue;
                                    }
                                    valueStatus = this.foundNoDataItems(rowNumberList, enterValueList, valueStatusList, dscalcs.getValue(calc, "valuestatus").length() == 0, rowNumber, "CALC_PARAM_ERROR: No dataset found for " + tokens[token] + " in " + paramid + " for " + keyid1 + " using filter " + dsTokenFilter, calcReportRequired, calcReportToken);
                                    continue;
                                }
                                if (!(datasetValueRows <= 1 || multiSDIs || hasWildcard || maxDataset || isDatasetFilter)) {
                                    valueStatus = this.foundTooManyDataSets("CALC_PARAM_ERROR: Too many datasets found for " + tokens[token] + " for " + keyid1 + " using filter " + dsTokenFilter, calcReportRequired, calcReportToken, dataSetValues);
                                    continue;
                                }
                                try {
                                    if (maxDataset) {
                                        dataSetValues = this.getMaxDataSets(dataSetValues);
                                    }
                                    this.resolveDataSetColumnAttributeValue(tokens[token], expressionParams, calcReportToken, datasetColumn, dataSetValues);
                                }
                                catch (SapphireException e) {
                                    if (e.getErrorid().equals(ERROR_ATTRIBUTENOTFOUND) && altValue.length() > 0) {
                                        this.useAltValue(tokens[token], expressionParams, calcReportToken, altValue);
                                        continue;
                                    }
                                    valueStatus = "CALC_PARAM_ERROR";
                                    this.logger.info("CALC_PARAM_ERROR: " + e.getMessage());
                                    calcReportToken.setComment("Error: " + e.getMessage());
                                }
                                continue;
                            }
                            paramValueRows = paramValues.getRowCount();
                            if (paramValueRows == 0) {
                                if (altValue.length() > 0) {
                                    this.useAltValue(tokens[token], expressionParams, calcReportToken, altValue);
                                    continue;
                                }
                                valueStatus = this.foundNoDataItems(rowNumberList, enterValueList, valueStatusList, dscalcs.getValue(calc, "valuestatus").length() == 0, rowNumber, "CALC_PARAM_ERROR: No parameter found for " + tokens[token] + " in " + paramid + " for " + keyid1 + " using filter " + diTokenFilter, calcReportRequired, calcReportToken);
                                continue;
                            }
                            if (!(multiSDIs || hasWildcard || maxReplicate || maxDataset || isDatasetFilter || paramValueRows <= 1)) {
                                valueStatus = this.foundTooManyDataItems("CALC_PARAM_ERROR: Too many parameters found for " + tokens[token] + " in " + paramid + " for " + keyid1 + " using filter " + diTokenFilter, calcReportRequired, calcReportToken, paramValues);
                                continue;
                            }
                            if (!(paramValues.findRow("_modified", "Y") < 0 || isFormulationCalc && tokens[token].equalsIgnoreCase("#;#;#;displayunits"))) {
                                hasModifiedArguments = true;
                            }
                            if (!(paramValues.findRow("_hascalc", "Y") < 0 || isFormulationCalc && tokens[token].equalsIgnoreCase("#;#;#;displayunits"))) {
                                hasCalcOfCalcs = true;
                            }
                            try {
                                if (paramValueRows == 1) {
                                    this.foundSingleDataItem(tokens[token], expressionParams, calcReportToken, valueColumn, paramValues, paramLimits);
                                    continue;
                                }
                                if (maxReplicate && maxDataset) {
                                    this.resolveMultipleDataItemsMax(tokens[token], expressionParams, calcReportToken, valueColumn, paramValues, "dataset,replicateid", paramLimits);
                                    continue;
                                }
                                if (maxDataset) {
                                    this.resolveMultipleDataItemsMax(tokens[token], expressionParams, calcReportToken, valueColumn, paramValues, "dataset", paramLimits);
                                    continue;
                                }
                                if (maxReplicate) {
                                    this.resolveMultipleDataItemsMax(tokens[token], expressionParams, calcReportToken, valueColumn, paramValues, "replicateid", paramLimits);
                                    continue;
                                }
                                this.foundMultipleDataItems(tokens[token], expressionParams, calcReportToken, valueColumn, paramValues, paramValueRows, paramLimits);
                                continue;
                            }
                            catch (SapphireException e) {
                                valueStatus = "CALC_PARAM_ERROR";
                                this.logger.info("CALC_PARAM_ERROR: " + e.getMessage());
                                calcReportToken.setComment("Error: " + e.getMessage());
                            }
                        }
                    }
                    if (!(calcsOnly || calcReportRequired || arrayCalcOnly || isFormulationCalc || isExtraCalc || hasParamFound || !hasThisSDITokenFound || isPrimaryOrDatasetToken)) {
                        for (int token = 0; token < tokens.length; ++token) {
                            CalcReport.Token calcReportToken = calcReport.createToken(token);
                            calcReportToken.tokenid = tokens[token];
                            valueStatus = this.foundNoDataItems(rowNumberList, enterValueList, valueStatusList, dscalcs.getValue(calc, "valuestatus").length() == 0, rowNumber, "CALC_PARAM_ERROR: No matching parameter found.", calcReportRequired, calcReportToken);
                        }
                        String dataType = dscalcs.getValue(calc, "datatypes");
                        if (dscalcs.getValue(calc, "valuestatus").length() == 0 && (dataType.equals("NC") || dataType.equals("TC") || dataType.equals("DC") || dataType.equals("OC")) && !this.isUpdateReleasedCalcs && dscalcs.getValue(calc, "releasedflag").equals("Y")) {
                            StringBuilder expmsg = this.getReleasedDataItemErrormsg(dscalcs, calc, sdidata);
                            throw new SapphireException("UPDATE_RELEASED_CALC", expmsg.toString());
                        }
                    }
                    if (hasModifiedArguments || calcReportRequired || calcsOnly) {
                        String newEnteredValue = "";
                        String oldEnteredValue = "";
                        String datatype = dscalcs.getValue(calc, "datatypes");
                        if (datatype.equals("N") || datatype.equals("NC")) {
                            oldEnteredValue = dscalcs.getValue(calc, "enteredvalue");
                            if (oldEnteredValue.length() == 0 || dscalcs.getValue(calc, "valuestatus").length() > 0) {
                                oldEnteredValue = dscalcs.getValue(calc, "enteredtext");
                            }
                            try {
                                oldEnteredValue = FormatUtil.getInstance(this.connectionInfo).format(FormatUtil.getInstance().parseBigDecimal(oldEnteredValue), false, true);
                                String enteredOperator = dscalcs.getValue(calc, "enteredoperator");
                                String enteredUnit = dscalcs.getValue(calc, "enteredunits");
                                oldEnteredValue = enteredOperator + oldEnteredValue + enteredUnit;
                            }
                            catch (Exception enteredOperator) {}
                        } else {
                            oldEnteredValue = dscalcs.getValue(calc, "enteredtext");
                        }
                        boolean isReleased = dscalcs.getValue(calc, "releasedflag").equals("Y");
                        dscalcs.setString(calc, "_recalcrequired", hasCalcOfCalcs ? "Y" : "N");
                        if (valueStatus.length() == 0) {
                            try {
                                newEnteredValue = this.executeCalculation(dscalcs, calc, calcrule, keyid1, calcReportRequired, calcReport, expressionParams);
                                this.logger.debug("CALC_DIAG: old value = " + oldEnteredValue + " :: new value = " + newEnteredValue);
                                if (calcReportRequired && !oldEnteredValue.equals(newEnteredValue)) {
                                    String datatypes = dscalcs.getValue(calc, "datatypes");
                                    calcReport.infoText = "Warning: Mismatch with saved result. ";
                                    calcReport.infoText = datatypes.equals("NC") || datatypes.equals("TC") || datatypes.equals("OC") || datatypes.equals("DC") ? calcReport.infoText + "You may need to trigger a recalculation." : calcReport.infoText + "The value may have been entered manually.";
                                }
                            }
                            catch (SapphireException e) {
                                if (calcReportRequired) {
                                    calcReport.errorText = "Calculation Failed. Error message: " + e.getMessage();
                                }
                                valueStatus = "CALC_ERROR";
                                this.logger.info("CALC_ERROR: Unable to process calculation for " + dscalcs.getString(calc, "paramid") + " for " + keyid1 + ". Exception: " + e.getMessage());
                            }
                        }
                        if (!oldEnteredValue.equals(newEnteredValue) || isLiveLimitChecking && !oldEnteredValue.equals(newEnteredValue)) {
                            valueEntered = this.isValueEntered(valueEntered, rowNumberList, enterValueList, valueStatusList, dscalcs, calc, rowNumber, calcReportRequired, valueStatus, newEnteredValue, oldEnteredValue, isReleased, sdidata);
                        } else if (!(calcsOnly || arrayCalcOnly || isFormulationCalc || isExtraCalc || hasCalcExcludeChange)) {
                            valueEntered = this.isValueEntered(valueEntered, rowNumberList, enterValueList, valueStatusList, dscalcs, calc, rowNumber, calcReportRequired, valueStatus, newEnteredValue, oldEnteredValue, isReleased, sdidata);
                        }
                    }
                    if (!calcReportRequired) continue;
                    dscalcs.setString(calc, "_calcreport", calcReport.toHTML(this.connectionInfo));
                }
                this.logger.info("CALC_PERFORMANCE: All calcs done for this sdi. Actual processed: " + actualCalcProcessed);
            }
            this.logger.info("CALC_PERFORMANCE: All calcs done for this iteration. Start entering the values.");
            if (rowNumberList.size() > 0 && !calcReportOnly) {
                int[] rowNumberListArray = new int[rowNumberList.size()];
                for (int i = 0; i < rowNumberList.size(); ++i) {
                    rowNumberListArray[i] = (Integer)rowNumberList.get(i);
                }
                String[] enterValueListArray = enterValueList.toArray(new String[enterValueList.size()]);
                String[] valueStatusListArray = valueStatusList.toArray(new String[valueStatusList.size()]);
                this.baseSDIDataEntryAction.enterDataValues(sdidata, sdidataitems, sdidataitemlimits, rowNumberListArray, enterValueListArray, valueStatusListArray);
            }
            this.logger.info("CALC_PERFORMANCE: Values updation done for this iteration.");
        } while (!calcReportOnly && valueEntered && calcloopcount < totalcalcs);
        long eTime = System.currentTimeMillis();
        this.logger.info("CALC_PERFORMANCE: calculation finished: " + (eTime - stTime));
        if (valueEntered && calcloopcount == totalcalcs && !isLiveLimitChecking) {
            throw new SapphireException("CALC_INFINITE_LOOP", "Infinite loop detected in performCalcs routine. Aborting.");
        }
    }

    private boolean isValueEntered(boolean valueEntered, ArrayList<Integer> rowNumberList, ArrayList<String> enterValueList, ArrayList<String> valueStatusList, DataSet dscalcs, int calc, int rowNumber, boolean calcReportRequired, String valueStatus, String newEnteredValue, String oldEnteredValue, boolean isReleased, DataSet sdidata) throws SapphireException {
        if (this.isUpdateReleasedCalcs || !isReleased || calcReportRequired) {
            rowNumberList.add(new Integer(rowNumber));
            enterValueList.add(newEnteredValue);
            valueStatusList.add(valueStatus);
            valueEntered = true;
        } else {
            String datatypes = dscalcs.getValue(calc, "datatypes");
            if ((datatypes.equals("NC") || datatypes.equals("TC") || datatypes.equals("DC") || datatypes.equals("OC")) && !this.isUpdateReleasedCalcs && !oldEnteredValue.equals(newEnteredValue)) {
                StringBuilder expmsg = this.getReleasedDataItemErrormsg(dscalcs, calc, sdidata);
                throw new SapphireException("UPDATE_RELEASED_CALC", expmsg.toString());
            }
        }
        return valueEntered;
    }

    private StringBuilder getReleasedDataItemErrormsg(DataSet dscalcs, int calc, DataSet sdidata) {
        SDI sdi = new SDI(dscalcs.getValue(calc, "sdcid"), dscalcs.getValue(calc, "keyid1"), dscalcs.getValue(calc, "keyid2"), dscalcs.getValue(calc, "keyid3"));
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("sdcid", dscalcs.getValue(calc, "sdcid"));
        filter.put("keyid1", dscalcs.getValue(calc, "keyid1"));
        filter.put("keyid2", dscalcs.getValue(calc, "keyid2"));
        filter.put("keyid3", dscalcs.getValue(calc, "keyid3"));
        filter.put("paramlistid", dscalcs.getValue(calc, "paramlistid"));
        filter.put("paramlistversionid", dscalcs.getValue(calc, "paramlistversionid"));
        filter.put("variantid", dscalcs.getValue(calc, "variantid"));
        filter.put("dataset", new BigDecimal(dscalcs.getValue(calc, "dataset")));
        int findRow = sdidata.findRow(filter);
        String sourceworkitemid = sdidata.getValue(findRow, "sourceworkitemid");
        String sourceworkiteminstance = sdidata.getValue(findRow, "sourceworkiteminstance");
        String testingdepartmentid = sdidata.getValue(findRow, "testingdepartmentid");
        String workareadepartmentid = sdidata.getValue(findRow, "workareadepartmentid");
        StringBuilder expmsg = new StringBuilder();
        expmsg.append("Illegal attempt to update a released calculated result ");
        expmsg.append("<ul>");
        expmsg.append("<li>" + sdi.toString());
        if (OpalUtil.isNotEmpty(sourceworkitemid) && OpalUtil.isNotEmpty(sourceworkiteminstance)) {
            expmsg.append("<li> Test Method: " + sourceworkitemid + " (" + sourceworkiteminstance + ")</li>");
        }
        expmsg.append("<li> Parameter List: " + dscalcs.getValue(calc, "paramlistid") + " (" + dscalcs.getValue(calc, "variantid") + "," + dscalcs.getValue(calc, "paramlistversionid") + ")</li>");
        expmsg.append("<li> DS#: " + dscalcs.getValue(calc, "dataset") + "</li>");
        expmsg.append("<li> Parameter: " + dscalcs.getValue(calc, "paramid") + " (" + dscalcs.getValue(calc, "paramtype") + " Rep " + dscalcs.getValue(calc, "replicateid") + ")</li>");
        if (OpalUtil.isNotEmpty(testingdepartmentid)) {
            expmsg.append("<li> Testing Lab: " + testingdepartmentid + "</li>");
        }
        if (OpalUtil.isNotEmpty(workareadepartmentid)) {
            expmsg.append("<li> Workarea: " + workareadepartmentid + "</li>");
        }
        expmsg.append("</ul>");
        return expmsg;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void foundSingleDataItem(String token, HashMap<String, Object> expressionParams, CalcReport.Token calcReportToken, String valueColumn, DataSet paramValues, DataSet paramLimits) throws SapphireException {
        int rownum = 0;
        String datatypes = paramValues.getString(rownum, "datatypes");
        if (valueColumn.equals("transformvalue")) {
            if ("D".equals(datatypes) || "O".equals(datatypes) || "DC".equals(datatypes) || "OC".equals(datatypes)) {
                valueColumn = "transformdt";
            } else if ("T".equals(datatypes) || "TC".equals(datatypes) || "R".equals(datatypes) || "V".equals(datatypes) || "S".equals(datatypes)) {
                valueColumn = "transformtext";
            }
        }
        if (valueColumn.equalsIgnoreCase("(all)")) {
            HashMap all = new HashMap();
            all.putAll((Map)paramValues.get(rownum));
            expressionParams.put(token, all);
            calcReportToken.addDataItemRow(paramValues, valueColumn, 0);
            return;
        } else if (paramValues.isValidColumn(valueColumn)) {
            if (paramValues.getColumnType(valueColumn) == 0 && paramValues.getValue(rownum, valueColumn).length() == 0) {
                expressionParams.put(token, this.alwaysReturnNullForTextTypes ? null : "");
            } else {
                int columnType = paramValues.getColumnType(valueColumn);
                if (columnType == 2) {
                    Object dVal;
                    Object object = dVal = paramValues.getCalendar(rownum, valueColumn) == null ? null : paramValues.getCalendar(rownum, valueColumn).clone();
                    if (dVal != null) {
                        ((GregorianCalendar)dVal).set(14, 0);
                    }
                    expressionParams.put(token, dVal);
                } else {
                    expressionParams.put(token, paramValues.getObject(rownum, valueColumn));
                }
            }
            calcReportToken.addDataItemRow(paramValues, valueColumn, 0);
            return;
        } else if (valueColumn.toUpperCase().startsWith("PARAMLIMIT") && valueColumn.contains(":")) {
            if (paramLimits.getRowCount() <= 0) throw new SapphireException("CALC_PARAM_ERROR: Did not recognize the limit type for " + token);
            String[] plLimit = StringUtil.split(valueColumn, ":");
            String limitTypeId = plLimit[1];
            String valueColumnId = plLimit[0].toLowerCase().endsWith("_status") ? "statusflag" : (plLimit[0].endsWith("_2") ? "value2" : "value1");
            String reportValue = "";
            if (valueColumnId.equals("statusflag")) {
                Integer status_value = paramLimits.isNull(rownum, valueColumnId) || "U".equalsIgnoreCase(paramLimits.getValue(rownum, valueColumnId)) ? null : Integer.valueOf("Y".equalsIgnoreCase(paramLimits.getValue(rownum, valueColumnId)) ? 1 : 0);
                reportValue = status_value == null ? "Undefined" : status_value.toString();
                expressionParams.put(token, status_value);
            } else {
                String value = paramLimits.getValue(rownum, valueColumnId);
                if (value.length() > 0) {
                    BigDecimal systemLocaleValue = FormatUtil.getInstance().parseBigDecimal(value);
                    expressionParams.put(token, systemLocaleValue);
                } else {
                    expressionParams.put(token, value);
                }
                reportValue = paramLimits.getValue(rownum, valueColumnId);
            }
            calcReportToken.addDataItemLimitRow(paramValues, rownum, limitTypeId, valueColumnId, reportValue);
            return;
        } else {
            Object result = this.findDataItemAttribute(rownum, calcReportToken, valueColumn, paramValues, true);
            expressionParams.put(token, result);
        }
    }

    private boolean isValidDataEntryAttribute(String attributeid) {
        return this.allValidDataentryAttributes.findRow("attributeid", attributeid) >= 0;
    }

    private void useAltValue(String token, HashMap<String, Object> expressionParams, CalcReport.Token calcReportToken, String altValue) {
        if (altValue.contains("'") || altValue.contains("\"")) {
            altValue = StringUtil.replaceAll(altValue, "'", "");
            altValue = StringUtil.replaceAll(altValue, "\"", "\"");
            expressionParams.put(token, altValue);
        } else {
            try {
                String temp = StringUtil.replaceAll(altValue, ",", ".");
                expressionParams.put(token, this.systemformatutil.parseBigDecimal(temp));
            }
            catch (Exception e) {
                Calendar calendar = this.systemdtu.getCalendar(altValue);
                if (calendar == null) {
                    expressionParams.put(token, altValue);
                }
                expressionParams.put(token, calendar);
            }
        }
        calcReportToken.addAltRow(altValue);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void foundMultipleDataItems(String token, HashMap<String, Object> expressionParams, CalcReport.Token calcReportToken, String valueColumn, DataSet paramValues, int paramValueRows, DataSet paramLimits) throws SapphireException {
        String datatypes = paramValues.getString(0, "datatypes");
        if (valueColumn.equals("transformvalue")) {
            if ("D".equals(datatypes) || "O".equals(datatypes) || "DC".equals(datatypes) || "OC".equals(datatypes)) {
                valueColumn = "transformdt";
            } else if ("T".equals(datatypes) || "TC".equals(datatypes) || "R".equals(datatypes) || "V".equals(datatypes) || "S".equals(datatypes)) {
                valueColumn = "transformtext";
            }
        }
        if (valueColumn.equalsIgnoreCase("(all)")) {
            HashMap[] valueitems = new HashMap[paramValueRows];
            for (int i = 0; i < paramValueRows; ++i) {
                HashMap all = new HashMap();
                all.putAll((Map)paramValues.get(i));
                valueitems[i] = all;
            }
            expressionParams.put(token, valueitems);
            return;
        } else if (paramValues.isValidColumn(valueColumn)) {
            int columnType = paramValues.getColumnType(valueColumn);
            if (columnType == 2) {
                Calendar[] valueitems = new Calendar[paramValueRows];
                for (int i = 0; i < paramValueRows; ++i) {
                    Calendar calendar = valueitems[i] = paramValues.isNull(i, valueColumn) ? null : paramValues.getCalendar(i, valueColumn);
                    if (valueitems[i] == null) continue;
                    valueitems[i].set(14, 0);
                }
                expressionParams.put(token, valueitems);
            } else if (columnType == 0) {
                String[] valueitems = new String[paramValueRows];
                for (int i = 0; i < paramValueRows; ++i) {
                    valueitems[i] = paramValues.isNull(i, valueColumn) ? null : paramValues.getValue(i, valueColumn);
                }
                expressionParams.put(token, valueitems);
            } else {
                BigDecimal[] valueitems = new BigDecimal[paramValueRows];
                for (int i = 0; i < paramValueRows; ++i) {
                    valueitems[i] = paramValues.isNull(i, valueColumn) ? null : paramValues.getBigDecimal(i, valueColumn);
                }
                expressionParams.put(token, valueitems);
            }
            calcReportToken.addDataItems(paramValues, valueColumn);
            return;
        } else if (valueColumn.toUpperCase().startsWith("PARAMLIMIT") && valueColumn.contains(":")) {
            if (paramLimits.getRowCount() <= 0) throw new SapphireException("CALC_PARAM_ERROR: Did not recognize the limit type for " + token);
            String[] plLimit = StringUtil.split(valueColumn, ":");
            String limitTypeId = plLimit[1];
            String valueColumnId = plLimit[0].toLowerCase().endsWith("_status") ? "statusflag" : (plLimit[0].endsWith("_2") ? "value2" : "value1");
            String reportValue = "";
            if (valueColumnId.equals("statusflag")) {
                Integer[] valueItems = new Integer[paramValueRows];
                for (int i = 0; i < paramLimits.getRowCount(); ++i) {
                    valueItems[i] = paramLimits.isNull(i, valueColumnId) || "U".equalsIgnoreCase(paramLimits.getValue(i, valueColumnId)) ? null : Integer.valueOf("Y".equalsIgnoreCase(paramLimits.getValue(i, valueColumnId)) ? 1 : 0);
                    reportValue = valueItems[i] == null ? "Undefined" : valueItems[i].toString();
                    calcReportToken.addDataItemLimitRow(paramLimits, i, limitTypeId, valueColumnId, reportValue);
                }
                expressionParams.put(token, valueItems);
                return;
            } else {
                Object[] valueItems = new Object[paramLimits.getRowCount()];
                for (int i = 0; i < paramLimits.getRowCount(); ++i) {
                    String value = paramLimits.getValue(i, valueColumnId);
                    if (value.length() > 0) {
                        BigDecimal systemLocaleValue = FormatUtil.getInstance().parseBigDecimal(value);
                        valueItems[i] = systemLocaleValue;
                    } else {
                        valueItems[i] = value;
                    }
                    reportValue = paramLimits.getValue(i, valueColumnId);
                    calcReportToken.addDataItemLimitRow(paramLimits, i, limitTypeId, valueColumnId, reportValue);
                }
                expressionParams.put(token, valueItems);
            }
            return;
        } else {
            BigDecimal[] valueitems = new BigDecimal[paramValueRows];
            for (int i = 0; i < paramValueRows; ++i) {
                Object value = this.findDataItemAttribute(i, calcReportToken, valueColumn, paramValues, false);
                if (!(value instanceof BigDecimal)) {
                    throw new SapphireException("Only numeric values can be used in wild card calculations.");
                }
                valueitems[i] = (BigDecimal)value;
            }
            expressionParams.put(token, valueitems);
        }
    }

    private Object findDataItemAttribute(int row, CalcReport.Token calcReportToken, String attributeid, DataSet sdidataitems, boolean multiItemAllowed) throws SapphireException {
        String sdidataitemid = sdidataitems.getValue(row, "sdidataitemid");
        DataSet attributesForDataItem = this.dataitemAttributes.get(sdidataitemid);
        if (attributesForDataItem == null) {
            attributesForDataItem = this.queryProcessor.getPreparedSqlDataSet("SELECT * FROM sdiattribute WHERE sdcid='DataItem' AND keyid1=?", new Object[]{sdidataitemid});
            this.dataitemAttributes.put(sdidataitemid, attributesForDataItem);
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("attributeid", attributeid);
        DataSet foundAttributes = attributesForDataItem.getFilteredDataSet(filter);
        BigDecimal[] attributeValue = null;
        if (foundAttributes.size() == 0) {
            throw new SapphireException("Unable to locate a dataitem column or unique attribute: " + attributeid + ".");
        }
        String dataType = foundAttributes.getValue(0, "datatype");
        String attributeColumn = "";
        if ("D".equals(dataType) || "O".equals(dataType)) {
            attributeColumn = "datevalue";
        } else if ("S".equals(dataType) || "C".equals(dataType)) {
            attributeColumn = "textvalue";
        } else if ("N".equals(dataType)) {
            attributeColumn = "numericvalue";
        }
        if (foundAttributes.size() == 1) {
            attributeValue = foundAttributes.getObject(0, attributeColumn);
            String reportValue = foundAttributes.getValue(0, attributeColumn);
            calcReportToken.addDataItemAttributeRow(sdidataitems, row, attributeid, reportValue);
        } else if (foundAttributes.size() > 0 && "N".equals(dataType) && multiItemAllowed) {
            BigDecimal[] valueitems = new BigDecimal[foundAttributes.size()];
            String[] reportValues = new String[foundAttributes.size()];
            for (int i = 0; i < foundAttributes.size(); ++i) {
                valueitems[i] = foundAttributes.getBigDecimal(i, attributeColumn);
                reportValues[i] = foundAttributes.getValue(i, attributeColumn);
            }
            attributeValue = valueitems;
            calcReportToken.addDataItemAttributeRows(sdidataitems, row, attributeid, reportValues);
        } else {
            throw new SapphireException("Multiple instances found for the attribute: " + attributeid + ".");
        }
        return attributeValue;
    }

    private void resolveMultipleDataItemsMax(String token, HashMap<String, Object> expressionParams, CalcReport.Token calcReportToken, String valueColumn, DataSet paramValues, String sortColumn, DataSet paramLimits) throws SapphireException {
        paramValues.sort(sortColumn);
        ArrayList<DataSet> groupedDS = paramValues.getGroupedDataSets(sortColumn);
        DataSet maxDS = groupedDS.get(groupedDS.size() - 1);
        int rowCount = maxDS.getRowCount();
        if (rowCount > 1) {
            this.foundMultipleDataItems(token, expressionParams, calcReportToken, valueColumn, maxDS, rowCount, paramLimits);
        } else {
            this.foundSingleDataItem(token, expressionParams, calcReportToken, valueColumn, maxDS, paramLimits);
        }
    }

    private DataSet getMaxDataSets(DataSet sdidata) {
        sdidata.sort("dataset");
        ArrayList<DataSet> groupedDS = sdidata.getGroupedDataSets("dataset");
        DataSet maxDS = groupedDS.get(groupedDS.size() - 1);
        return maxDS;
    }

    private void resolveDataSetColumnAttributeValue(String token, HashMap<String, Object> expressionParams, CalcReport.Token calcReportToken, String valueColumn, DataSet sdidata) throws SapphireException {
        int rowCount = sdidata.getRowCount();
        if (rowCount > 1) {
            BigDecimal[] valueitems = new BigDecimal[rowCount];
            if (sdidata.isValidColumn(valueColumn)) {
                for (int i = 0; i < rowCount; ++i) {
                    if (sdidata.isNull(i, valueColumn)) {
                        valueitems[i] = null;
                        continue;
                    }
                    try {
                        valueitems[i] = sdidata.getBigDecimal(i, valueColumn);
                        continue;
                    }
                    catch (Exception e) {
                        throw new SapphireException("Only numeric values can be used in wild card calculations.");
                    }
                }
                expressionParams.put(token, valueitems);
                calcReportToken.addDataSets(sdidata, valueColumn);
            } else {
                for (int i = 0; i < rowCount; ++i) {
                    Object value = this.findDataSetAttribute(i, calcReportToken, valueColumn, sdidata, false);
                    if (!(value instanceof BigDecimal)) {
                        throw new SapphireException("Only numeric values can be used in wild card calculations.");
                    }
                    valueitems[i] = (BigDecimal)value;
                }
                expressionParams.put(token, valueitems);
            }
        } else if (sdidata.isValidColumn(valueColumn)) {
            expressionParams.put(token, sdidata.getObject(0, valueColumn));
            calcReportToken.addDataSetRow(sdidata, valueColumn, 0);
        } else {
            Object result = null;
            result = this.findDataSetAttribute(0, calcReportToken, valueColumn, sdidata, true);
            expressionParams.put(token, result);
        }
    }

    private Object findDataSetAttribute(int row, CalcReport.Token calcReportToken, String attributeid, DataSet sdidatasets, boolean multiItemAllowed) throws SapphireException {
        String sdidataid = sdidatasets.getValue(row, "sdidataid");
        String sourceworkitemid = sdidatasets.getValue(row, "sourceworkitemid");
        DataSet attributesForDataSet = this.datasetAttributes.get(sdidataid);
        if (attributesForDataSet == null) {
            attributesForDataSet = this.queryProcessor.getPreparedSqlDataSet("SELECT * FROM sdiattribute WHERE sdcid = 'DataSet' AND keyid1 = ?", new Object[]{sdidataid});
            if (sourceworkitemid.length() > 0) {
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT * FROM sdiattribute WHERE sdcid = 'SDIWorkItem' AND keyid1 = ").append("( select sdiworkitemid FROM sdiworkitem w, sdidata d").append(" where w.sdcid = d.sdcid and w.keyid1 = d.keyid1 and w.keyid2 = d.keyid2 and w.keyid3 = d.keyid3 and w.workitemid = d.sourceworkitemid and w.workiteminstance = d.sourceworkiteminstance and d.sdidataid =?)");
                if (attributesForDataSet.getRowCount() > 0) {
                    String dsAttributeIds = attributesForDataSet.getColumnValues("attributeid", "','");
                    sql.append(" AND attributeid NOT IN ( '").append(dsAttributeIds).append("' ) ");
                }
                DataSet sdiwiAttributes = this.queryProcessor.getPreparedSqlDataSet(sql.toString(), new Object[]{sdidataid});
                for (int i = 0; i < sdiwiAttributes.getRowCount(); ++i) {
                    attributesForDataSet.copyRow(sdiwiAttributes, i, 1);
                }
            }
            this.datasetAttributes.put(sdidataid, attributesForDataSet);
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("attributeid", attributeid);
        DataSet foundAttributes = attributesForDataSet.getFilteredDataSet(filter);
        BigDecimal[] attributeValue = null;
        if (foundAttributes.size() == 0) {
            throw new SapphireException(ERROR_ATTRIBUTENOTFOUND, "Unable to locate a dataset column or unique attribute: " + attributeid + ".");
        }
        String dataType = foundAttributes.getValue(0, "datatype");
        String attributeColumn = "";
        if ("D".equals(dataType) || "O".equals(dataType)) {
            attributeColumn = "datevalue";
        } else if ("S".equals(dataType) || "C".equals(dataType)) {
            attributeColumn = "textvalue";
        } else if ("N".equals(dataType)) {
            attributeColumn = "numericvalue";
        }
        if (foundAttributes.size() == 1) {
            String sdcid = foundAttributes.getValue(0, "sdcid");
            attributeValue = foundAttributes.getObject(0, attributeColumn);
            String reportValue = foundAttributes.getValue(0, attributeColumn);
            if (sdcid.equalsIgnoreCase("DataSet")) {
                calcReportToken.addDataSetAttributeRow(sdidatasets, row, attributeid, reportValue);
            } else if (sdcid.equalsIgnoreCase("SDIWorkItem")) {
                calcReportToken.addSDIWorkItemAttributeRow(sdidatasets, row, attributeid, reportValue);
            }
        } else if (foundAttributes.size() > 0 && "N".equals(dataType) && multiItemAllowed) {
            BigDecimal[] valueitems = new BigDecimal[foundAttributes.size()];
            String[] reportValues = new String[foundAttributes.size()];
            String sdcid = foundAttributes.getValue(0, "sdcid");
            for (int i = 0; i < foundAttributes.size(); ++i) {
                valueitems[i] = foundAttributes.getBigDecimal(i, attributeColumn);
                reportValues[i] = foundAttributes.getValue(i, attributeColumn);
            }
            attributeValue = valueitems;
            if (sdcid.equalsIgnoreCase("DataSet")) {
                calcReportToken.addDataSetAttributeRows(sdidatasets, row, attributeid, reportValues);
            } else if (sdcid.equalsIgnoreCase("SDIWorkItem")) {
                calcReportToken.addSDIWorkItemAttributeRows(sdidatasets, row, attributeid, reportValues);
            }
        } else {
            throw new SapphireException("Multiple instances found for the dataset attribute: " + attributeid + ".");
        }
        return attributeValue;
    }

    private String executeCalculation(DataSet dscalcs, int calc, String calcrule, String keyid1, boolean calcReportRequired, CalcReport calcReport, HashMap<String, Object> expressionParams) throws SapphireException {
        Object value;
        this.logger.info("Calculating " + keyid1 + " " + dscalcs.getString(calc, "paramid") + ";" + dscalcs.getString(calc, "paramtype") + " using " + this.truncateCalcRule(calcrule));
        if (!calcrule.startsWith("$G{") || !calcrule.endsWith("}")) {
            for (String param : expressionParams.keySet()) {
                value = expressionParams.get(param);
                if (!(value instanceof String[]) && !(value instanceof Calendar[]) && !(value instanceof HashMap[]) && !(value instanceof HashMap)) continue;
                throw new SapphireException(param + " has resolved to an illegal object type (String array, Date array or an (all) columns argument.)");
            }
        }
        String datatypes = dscalcs.getString(calc, "datatypes");
        String enterValue = ExpressionUtil.evaluateSecure(this.connectionInfo, calcrule, expressionParams, this.honorNullInRelationalExpressions, this.honorNullInANDORConditionals, datatypes);
        if (datatypes.equals("A")) {
            datatypes = DataEntryLimitsUtil.getApparentDatatype(enterValue, this.systemdtu, this.systemformatutil);
        }
        if (datatypes.equals("N") || datatypes.equals("NC")) {
            try {
                FormatUtil formatutil = FormatUtil.getInstance();
                value = enterValue;
                String operator = "";
                String enteredUnit = "";
                switch (enterValue.charAt(0)) {
                    case '<': 
                    case '>': {
                        if (enterValue.length() > 1 && enterValue.charAt(1) == '=') {
                            operator = enterValue.substring(0, 2);
                            value = enterValue.substring(2);
                            break;
                        }
                        operator = enterValue.substring(0, 1);
                        value = enterValue.substring(1);
                        break;
                    }
                    case '=': {
                        value = enterValue.substring(1);
                    }
                }
                for (int i = ((String)value).length(); i > 0; --i) {
                    try {
                        formatutil.parseBigDecimal(((String)value).substring(0, i));
                        if (i == 0) break;
                        enteredUnit = ((String)value).substring(i).trim();
                        value = ((String)value).substring(0, i);
                        break;
                    }
                    catch (NumberFormatException numberFormatException) {
                        continue;
                    }
                }
                BigDecimal enteredValue = formatutil.parseBigDecimal((String)value);
                if (this.connectionInfo.getDbms().equals("MSS")) {
                    enteredValue = enteredValue.divide(new BigDecimal(1.0), 10, 4);
                }
                enterValue = operator + FormatUtil.getInstance(this.connectionInfo).format(enteredValue, false, true) + enteredUnit;
            }
            catch (Exception formatutil) {}
        } else if (datatypes.equals("D") || datatypes.equals("O") || datatypes.equals("DC") || datatypes.equals("OC")) {
            try {
                M18NUtil m18NUtil = new M18NUtil(this.connectionInfo);
                if (enterValue != null && enterValue.length() > 0) {
                    enterValue = m18NUtil.format(new DateTimeUtil().getCalendar(enterValue), datatypes.equals("D") || datatypes.equals("DC"));
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (calcReportRequired) {
            calcReport.result = enterValue;
        }
        return enterValue;
    }

    private String foundTooManyDataItems(String message, boolean calcReportRequired, CalcReport.Token calcReportToken, DataSet paramValues) {
        if (calcReportRequired) {
            calcReportToken.addDataItems(paramValues, "");
            calcReportToken.setComment("ERROR. Too many records found. There should be a single record for this parameter.");
        }
        String valueStatus = "CALC_PARAM_ERROR";
        this.logger.info(message);
        return valueStatus;
    }

    private String foundTooManyDataSets(String message, boolean calcReportRequired, CalcReport.Token calcReportToken, DataSet dsValues) {
        if (calcReportRequired) {
            calcReportToken.addDataSets(dsValues, "");
            calcReportToken.setComment("ERROR. Too many records found. There should be a single dataset for this paramlist.");
        }
        String valueStatus = "CALC_PARAM_ERROR";
        this.logger.info(message);
        return valueStatus;
    }

    private String foundNoDataItems(ArrayList<Integer> rowNumberList, ArrayList<String> enterValueList, ArrayList<String> valueStatusList, boolean b, int rowNumber, String message, boolean calcReportRequired, CalcReport.Token calcReportToken) {
        String valueStatus = "CALC_PARAM_ERROR";
        this.logger.info(message);
        if (calcReportRequired) {
            calcReportToken.setComment("ERROR. No matches found.");
        }
        if (b) {
            rowNumberList.add(new Integer(rowNumber));
            enterValueList.add("");
            valueStatusList.add(valueStatus);
        }
        return valueStatus;
    }

    private boolean crossCalcSample(DataSet sdidata, DataSet sdidataitems, DataSet sdidataitemlimits, String keyid1, HashMap<String, Object> difilter, String sdiLinkDetails, boolean datasetFilter, boolean isPrimaryColOrAttribute, DataSet primary) throws SapphireException {
        boolean multiSDIs = true;
        if (sdiLinkDetails.equalsIgnoreCase("Parent") || sdiLinkDetails.equalsIgnoreCase("Child") || sdiLinkDetails.equalsIgnoreCase("Ancestor") || sdiLinkDetails.equalsIgnoreCase("Descendant")) {
            DataSet allHierarchicalSamples = null;
            allHierarchicalSamples = this.getParentChildSamples(keyid1, sdiLinkDetails, allHierarchicalSamples);
            String keySet = null;
            String key = sdiLinkDetails + "_sample:" + keyid1;
            DataSet linkdataitems = new DataSet();
            DataSet linkdatasets = new DataSet();
            DataSet linkPrimary = new DataSet();
            DataSet linkdataitemlimits = new DataSet();
            if (this.keySetCache.containsKey(key)) {
                keySet = this.keySetCache.get(key);
                difilter.put("_calckeyset", keySet);
                this.logger.debug("Cross Calc Filter: Skipped " + sdiLinkDetails + " sample retrieval of " + keyid1);
            } else {
                this.getSampleParentChildDataItems(allHierarchicalSamples, linkdataitems, linkdatasets, datasetFilter, isPrimaryColOrAttribute, linkPrimary, linkdataitemlimits, "dataitem");
                this.logger.debug("linkdataitems: " + linkdataitems);
                this.logger.debug("linkdataitemlimits: " + linkdataitemlimits);
                keySet = Integer.toString(this.calcKeySet++);
                this.keySetCache.put(key, keySet);
                linkdataitems.setString(-1, "_calckeyset", keySet);
                this.cleanNullCalcExcludeFlag(linkdataitems);
                this.baseSDIDataEntryAction.appendLinkedDataItems(sdidataitems, linkdataitems);
                this.baseSDIDataEntryAction.appendLinkedDataItemLimits(sdidataitemlimits, linkdataitemlimits);
                difilter.put("_calckeyset", keySet);
            }
            if (datasetFilter && !this.datasetFilterKeysRetrieved.contains(key)) {
                this.getSampleParentChildDataItems(allHierarchicalSamples, linkdataitems, linkdatasets, datasetFilter, isPrimaryColOrAttribute, linkPrimary, linkdataitemlimits, "dataset");
                linkdatasets.setString(-1, "_calckeyset", keySet);
                this.datasetFilterKeysRetrieved.add(key);
                this.logger.debug("linkdataset: " + linkdatasets);
                this.baseSDIDataEntryAction.appendLinkedDataSet(sdidata, linkdatasets);
            }
            if (isPrimaryColOrAttribute) {
                this.getSampleParentChildDataItems(allHierarchicalSamples, linkdataitems, linkdatasets, datasetFilter, isPrimaryColOrAttribute, linkPrimary, linkdataitemlimits, "primary");
                if (linkPrimary.getRowCount() > 0) {
                    linkPrimary.setString(-1, "sdcid", "Sample");
                    linkPrimary.setString(-1, "_calckeyset", keySet);
                    HashMap<String, String> find = new HashMap<String, String>();
                    find.put("sdcid", "Sample");
                    find.put("_calckeyset", keySet);
                    if (primary.findRow(find) == -1) {
                        primary.copyRow(linkPrimary, -1, 1);
                    }
                }
            }
        }
        return multiSDIs;
    }

    private DataSet getParentChildSamples(String sampleId, String relation, DataSet allHierarchicalSamples) throws SapphireException {
        boolean hierarchSearch;
        DataSet relatedSamples = new DataSet();
        boolean bl = hierarchSearch = "Ancestor".equalsIgnoreCase(relation) || "Descendant".equalsIgnoreCase(relation);
        if ("Parent".equalsIgnoreCase(relation) || "Ancestor".equalsIgnoreCase(relation)) {
            this.database.createPreparedResultSet("getparents", "select sourcesampleid sample from s_samplemap  where destsampleid = ? order by sourcesampleid", new String[]{sampleId});
            relatedSamples = new DataSet(this.database.getResultSet("getparents"));
        } else if ("Child".equalsIgnoreCase(relation) || "Descendant".equalsIgnoreCase(relation)) {
            this.database.createPreparedResultSet("getchilds", "select destsampleid sample from s_samplemap  where sourcesampleid = ? order by destsampleid", new String[]{sampleId});
            relatedSamples = new DataSet(this.database.getResultSet("getchilds"));
        }
        if (OpalUtil.isNotEmpty(relatedSamples)) {
            if (OpalUtil.isEmpty(allHierarchicalSamples)) {
                allHierarchicalSamples = relatedSamples.copy();
            } else {
                allHierarchicalSamples.addAll(relatedSamples);
            }
            if (hierarchSearch) {
                for (int r = 0; r < relatedSamples.getRowCount(); ++r) {
                    this.getParentChildSamples(relatedSamples.getValue(r, "sample"), relation, allHierarchicalSamples);
                }
            }
        }
        return allHierarchicalSamples;
    }

    private boolean crossCalcAQC(DataSet sdidata, DataSet sdidataitems, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, BigDecimal dataset, HashMap<String, Object> difilter, String sdiLinkDetails, DataSet primary) throws SapphireException {
        boolean multiSDIs = false;
        HashMap<String, Object> qcFilterMap = new HashMap<String, Object>();
        qcFilterMap.put("sdcid", sdcid);
        qcFilterMap.put("keyid1", keyid1);
        qcFilterMap.put("keyid2", keyid2);
        qcFilterMap.put("keyid3", keyid3);
        qcFilterMap.put("paramlistid", paramlistid);
        qcFilterMap.put("paramlistversionid", paramlistversionid);
        qcFilterMap.put("variantid", variantid);
        qcFilterMap.put("dataset", dataset);
        int findRow = sdidata.findRow(qcFilterMap);
        String qcBatchId = sdidata.getString(findRow, "s_qcbatchid", "");
        String qcBatchItemId = sdidata.getString(findRow, "s_qcbatchitemid", "");
        QCBatch qcBatch = QCBatchPool.getQCBatch(this.queryProcessor, qcBatchId);
        String qcBatchSDC = qcBatch.getQCBatchSDC();
        QCBatchItem batchItem = qcBatch.getQCBatchItem(qcBatchItemId);
        String[] subparts = StringUtil.split(sdiLinkDetails, ";");
        if (sdiLinkDetails.equalsIgnoreCase("Blank")) {
            QCBatchItem blankBatchItem = QCUtil.getBlankParent(qcBatch, batchItem);
            if (blankBatchItem != null) {
                String blankBatchItemId = blankBatchItem.getQCBatchItemID();
                String aqc_blankKey = "aqc_blank:" + qcBatchId + ";" + blankBatchItemId;
                if (this.keySetCache.containsKey(aqc_blankKey)) {
                    difilter.put("_calckeyset", this.keySetCache.get(aqc_blankKey));
                    this.logger.debug("Cross Calc Filter: Skipped AQC Blank retrieval.");
                } else {
                    DataSet linkdatasets;
                    DataSet linkdataitems = QCUtil.getLinkedDataitems(sdidata, sdidataitems, qcBatchId, blankBatchItemId);
                    if (linkdataitems.getRowCount() == 0) {
                        String sql = "SELECT di.* FROM sdidataitem di, sdidata ds WHERE di.sdcid=ds.sdcid AND di.keyid1=ds.keyid1 AND di.keyid2=ds.keyid2 AND di.keyid3=ds.keyid3 AND di.paramlistid=ds.paramlistid AND di.paramlistversionid = ds.paramlistversionid AND di.variantid = ds.variantid AND di.dataset=ds.dataset AND ds.s_qcbatchid=? AND ds.s_qcbatchitemid=?" + (this.includeCancelledDatasets ? " " : " AND ds.s_datasetstatus <> 'Cancelled' ");
                        this.logger.debug("Looking for QC:Blank dataitems using " + sql + " and " + sdcid + "/" + keyid1 + "/" + keyid2 + "/" + keyid3);
                        this.database.createPreparedResultSet("blankdataitems", sql, new Object[]{qcBatchId, blankBatchItemId});
                        linkdataitems.setResultSet(this.database.getResultSet("blankdataitems"));
                        this.logger.debug("Blank dataitems: " + linkdataitems.toString());
                    }
                    if ((linkdatasets = QCUtil.getLinkedDataSets(sdidata, qcBatchId, blankBatchItemId)).getRowCount() == 0) {
                        this.database.createPreparedResultSet("blankdatasets", "select * from sdidata where s_qcbatchid = ? and s_qcbatchitemid = ?", new Object[]{qcBatchId, blankBatchItemId});
                        linkdatasets.setResultSet(this.database.getResultSet("blankdatasets"));
                        this.logger.debug("Blank datasets: " + linkdatasets.toString());
                    }
                    String keySet = Integer.toString(this.calcKeySet++);
                    this.keySetCache.put(aqc_blankKey, keySet);
                    linkdataitems.setString(-1, "_calckeyset", keySet);
                    this.cleanNullCalcExcludeFlag(linkdataitems);
                    this.baseSDIDataEntryAction.appendLinkedDataItems(sdidataitems, linkdataitems);
                    difilter.put("_calckeyset", keySet);
                    this.populatePrimary(qcBatchSDC, primary, sdidataitems, difilter, keySet);
                    linkdatasets.setString(-1, "_calckeyset", keySet);
                    this.baseSDIDataEntryAction.appendLinkedDataSet(sdidata, linkdatasets);
                }
            }
        } else if (sdiLinkDetails.equalsIgnoreCase("Linked")) {
            String linkedBatchItemId = batchItem.getLinkedToBatchItemID();
            if (linkedBatchItemId != null && linkedBatchItemId.length() > 0) {
                String aqc_linkedKey = "aqc_linked:" + qcBatchId + ";" + linkedBatchItemId;
                if (this.keySetCache.containsKey(aqc_linkedKey)) {
                    difilter.put("_calckeyset", this.keySetCache.get(aqc_linkedKey));
                    this.logger.debug("Cross Calc Filter: Skipped AQC Linked retrieval.");
                } else {
                    DataSet linkdataitems = QCUtil.getLinkedDataitems(sdidata, sdidataitems, qcBatchId, linkedBatchItemId);
                    if (linkdataitems.getRowCount() == 0) {
                        String sql = "SELECT di.* FROM sdidataitem di, sdidata ds WHERE di.sdcid=ds.sdcid AND di.keyid1=ds.keyid1 AND di.keyid2=ds.keyid2 AND di.keyid3=ds.keyid3 AND di.paramlistid=ds.paramlistid AND di.paramlistversionid = ds.paramlistversionid AND di.variantid = ds.variantid AND di.dataset=ds.dataset AND ds.s_qcbatchid=? AND ds.s_qcbatchitemid=?" + (this.includeCancelledDatasets ? " " : " AND ds.s_datasetstatus <> 'Cancelled' ");
                        this.logger.debug("Looking for QC:Linked dataitems using " + sql + " and " + sdcid + "/" + keyid1 + "/" + keyid2 + "/" + keyid3);
                        this.database.createPreparedResultSet("qclinkdataitems", sql, new Object[]{qcBatchId, linkedBatchItemId});
                        linkdataitems.setResultSet(this.database.getResultSet("qclinkdataitems"));
                        this.logger.debug("link dataitems: " + linkdataitems.toString());
                    }
                    String keySet = Integer.toString(this.calcKeySet++);
                    this.keySetCache.put(aqc_linkedKey, keySet);
                    linkdataitems.setString(-1, "_calckeyset", keySet);
                    this.cleanNullCalcExcludeFlag(linkdataitems);
                    this.baseSDIDataEntryAction.appendLinkedDataItems(sdidataitems, linkdataitems);
                    difilter.put("_calckeyset", keySet);
                }
                if (this.paramlistPart == null || this.paramlistPart.length() == 0) {
                    this.paramlistPart = "*;*;*;*";
                }
            }
        } else if (sdiLinkDetails.equalsIgnoreCase("QCParam")) {
            String bstId = batchItem.getQCBatchSampleTypeID();
            String aqc_qcparamKey = "aqc_qcparam:QCBatchSampleType;" + bstId;
            if (this.keySetCache.containsKey(aqc_qcparamKey)) {
                difilter.put("_calckeyset", this.keySetCache.get(aqc_qcparamKey));
                this.logger.debug("Cross Calc Filter: Skipped AQC QCParam retrieval.");
            } else {
                String sql = " SELECT * FROM sdidataitem di, sdidata ds WHERE ds.sdcid = di.sdcid and ds.keyid1 = di.keyid1 and  ds.paramlistid = di.paramlistid and ds.paramlistversionid = di.paramlistversionid and ds.variantid = di.variantid  and ds.dataset = di.dataset and di.sdcid = 'QCBatchSampleType' AND di.keyid1 = ?" + (this.includeCancelledDatasets ? "" : " AND ds.s_datasetstatus <> 'Cancelled'");
                this.logger.debug("Looking for QCParam dataitems using " + sql + " and " + sdcid + "/" + keyid1);
                this.database.createPreparedResultSet("qcparams", sql, new Object[]{bstId});
                DataSet linkdataitems = new DataSet();
                linkdataitems.setResultSet(this.database.getResultSet("qcparams"));
                this.logger.debug("QCParam dataitems: " + linkdataitems.toString());
                String keySet = Integer.toString(this.calcKeySet++);
                this.keySetCache.put(aqc_qcparamKey, keySet);
                linkdataitems.setString(-1, "_calckeyset", keySet);
                this.cleanNullCalcExcludeFlag(linkdataitems);
                this.baseSDIDataEntryAction.appendLinkedDataItems(sdidataitems, linkdataitems);
                difilter.put("_calckeyset", keySet);
            }
            this.paramlistPart = "QCParams;*;*;*";
        } else {
            List batchItems;
            int len = subparts.length;
            String sampleType = "";
            String mode = "";
            String level = "";
            if (len == 1) {
                sampleType = subparts[0].trim();
            } else if (len == 2) {
                sampleType = subparts[0].trim();
                mode = subparts[1].trim();
            } else if (len == 3) {
                sampleType = subparts[0].trim();
                mode = subparts[1].trim();
                level = subparts[2].trim();
            }
            if ("".equals(sampleType)) {
                throw new SapphireException("Unrecognized Calc rule AQC:" + sdiLinkDetails);
            }
            if (mode.length() == 0) {
                mode = "Previous";
            }
            if ((batchItems = QCUtil.getBatchItems(qcBatch, batchItem, sampleType, mode, level)).size() > 0) {
                StringBuffer aqc_keys = new StringBuffer("aqc_keys:" + qcBatchId);
                for (int b = 0; b < batchItems.size(); ++b) {
                    String batchItemId = ((QCBatchItem)batchItems.get(b)).getQCBatchItemID();
                    aqc_keys.append(";").append(batchItemId);
                }
                if (this.keySetCache.containsKey(aqc_keys.toString())) {
                    difilter.put("_calckeyset", this.keySetCache.get(aqc_keys.toString()));
                    this.logger.debug("Cross Calc Filter: Skipped AQC " + sdiLinkDetails + " database retrieval for QCBatch: " + qcBatchId + " BatchItem: " + batchItem.getQCBatchItemID());
                } else {
                    DataSet linkdataitems = new DataSet();
                    DataSet linkdatasets = new DataSet();
                    for (int b = 0; b < batchItems.size(); ++b) {
                        DataSet dataitems = new DataSet();
                        String batchItemId = ((QCBatchItem)batchItems.get(b)).getQCBatchItemID();
                        String sql = "SELECT di.* FROM sdidataitem di, sdidata ds WHERE di.sdcid = ds.sdcid AND di.keyid1 = ds.keyid1 AND di.keyid2 = ds.keyid2 AND di.keyid3 = ds.keyid3  AND di.paramlistid = ds.paramlistid AND di.paramlistversionid = ds.paramlistversionid AND di.variantid = ds.variantid AND di.dataset = ds.dataset AND ds.s_qcbatchid = ? AND ds.s_qcbatchitemid = ?" + (this.includeCancelledDatasets ? " " : " AND ds.s_datasetstatus <> 'Cancelled' ");
                        this.logger.debug("Looking for QC dataitems using " + sql + " and " + sdcid + "/" + keyid1 + "/" + keyid2 + "/" + keyid3);
                        this.database.createPreparedResultSet("qcdataitems", sql, new Object[]{qcBatchId, batchItemId});
                        dataitems.setResultSet(this.database.getResultSet("qcdataitems"));
                        if (!OpalUtil.isNotEmpty(dataitems)) continue;
                        linkdataitems.addAll(dataitems);
                        this.logger.debug("QC dataitems: " + dataitems.toString());
                        DataSet datasets = new DataSet();
                        this.database.createPreparedResultSet("qcdatasets", "select * from sdidata where s_qcbatchid = ? and s_qcbatchitemid = ?", new Object[]{qcBatchId, batchItemId});
                        datasets.setResultSet(this.database.getResultSet("qcdatasets"));
                        if (!OpalUtil.isNotEmpty(datasets)) continue;
                        linkdatasets.addAll(datasets);
                    }
                    String keySet = Integer.toString(this.calcKeySet++);
                    this.keySetCache.put(aqc_keys.toString(), keySet);
                    linkdataitems.setString(-1, "_calckeyset", keySet);
                    this.cleanNullCalcExcludeFlag(linkdataitems);
                    this.baseSDIDataEntryAction.appendLinkedDataItems(sdidataitems, linkdataitems);
                    difilter.put("_calckeyset", keySet);
                    this.populatePrimary(qcBatchSDC, primary, sdidataitems, difilter, keySet);
                    if (linkdatasets.getRowCount() > 0) {
                        linkdatasets.setString(-1, "_calckeyset", keySet);
                        this.baseSDIDataEntryAction.appendLinkedDataSet(sdidata, linkdatasets);
                    }
                }
            } else {
                throw new SapphireException("Unable to locate the batch item: " + sdiLinkDetails + " for QCBatch: " + qcBatch.getQCBatchID() + ", QCBatch Item: " + batchItem.getQCBatchItemID());
            }
        }
        return multiSDIs;
    }

    private void populatePrimary(String sdc, DataSet primary, DataSet sdidataitems, HashMap difilter, String keySet) throws SapphireException {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("_calckeyset", (String)difilter.get("_calckeyset"));
        DataSet primaries = primary.getFilteredDataSet(filter);
        if (primaries.getRowCount() == 0) {
            PropertyList sdcProp = this.sdcProcessor.getPropertyList(sdc);
            String tableId = sdcProp.getProperty("tableid");
            String keycolId1 = sdcProp.getProperty("keycolid1");
            String keycolId2 = sdcProp.getProperty("keycolid2");
            String keycolId3 = sdcProp.getProperty("keycolid3");
            DataSet linkedDataItems = sdidataitems.getFilteredDataSet(filter);
            String linkedPrimarysql = "SELECT * FROM " + tableId + " WHERE " + keycolId1 + "=?" + (keycolId2.length() > 0 ? " AND " + keycolId2 + " = ?" : "") + (keycolId3.length() > 0 ? " AND " + keycolId3 + " = ?" : "");
            HashMap<String, String> findPrimary = new HashMap<String, String>();
            findPrimary.put("sdcid", sdc);
            findPrimary.put("_calckeyset", keySet);
            for (int di = 0; di < linkedDataItems.getRowCount(); ++di) {
                ArrayList<String> primaryParams = new ArrayList<String>();
                String linked_keyid1 = linkedDataItems.getString(di, "keyid1");
                findPrimary.put(keycolId1, linked_keyid1);
                primaryParams.add(linked_keyid1);
                if (keycolId2.length() > 0) {
                    String linked_keyid2 = linkedDataItems.getString(di, "keyid2");
                    primaryParams.add(linked_keyid2);
                    findPrimary.put(keycolId2, linked_keyid2);
                }
                if (keycolId3.length() > 0) {
                    String linked_keyid3 = linkedDataItems.getString(di, "keyid3");
                    primaryParams.add(linked_keyid3);
                    findPrimary.put(keycolId3, linked_keyid3);
                }
                if (primary.findRow(findPrimary) >= 0) continue;
                this.database.createPreparedResultSet("primary", linkedPrimarysql, primaryParams.toArray());
                DataSet linkedPrimary = new DataSet(this.database.getResultSet("primary"));
                this.database.closeResultSet("primary");
                linkedPrimary.setString(-1, "sdcid", sdc);
                linkedPrimary.setString(-1, "_calckeyset", keySet);
                primary.copyRow(linkedPrimary, -1, 1);
            }
        }
    }

    private void getSampleParentChildDataItems(DataSet allHierarchicalSamples, DataSet linkdataitems, DataSet linkdatasets, boolean datasetFilter, boolean isPrimaryColOrAttribute, DataSet linkPrimary, DataSet linkdataitemlimits, String populateFlag) throws SapphireException {
        if (OpalUtil.isNotEmpty(allHierarchicalSamples)) {
            for (int r = 0; r < allHierarchicalSamples.getRowCount(); ++r) {
                String sample = allHierarchicalSamples.getValue(r, "sample");
                if (populateFlag.equalsIgnoreCase("primary") && isPrimaryColOrAttribute) {
                    HashMap<String, String> findPrimary = new HashMap<String, String>();
                    findPrimary.put("s_sampleid", sample);
                    if (linkPrimary.findRow(findPrimary) < 0) {
                        this.database.createPreparedResultSet("linkprimary", "select * from s_sample where s_sampleid = ?", new String[]{sample});
                        DataSet dsPrimary = new DataSet(this.database.getResultSet("linkprimary"));
                        this.database.closeResultSet("linkprimary");
                        linkPrimary.copyRow(dsPrimary, -1, 1);
                    }
                }
                if (populateFlag.equalsIgnoreCase("dataitem")) {
                    String selectsdidataitemlimits;
                    String selectsdidataitem;
                    if (this.includeCancelledDatasets) {
                        selectsdidataitem = "select * from sdidataitem where sdcid = 'Sample' and keyid1 = ?";
                        selectsdidataitemlimits = "select * from sdidataitemlimits where sdcid = 'Sample' and keyid1 = ?";
                    } else {
                        selectsdidataitem = "SELECT sdidataitem.* FROM sdidata, sdidataitem WHERE sdidata.sdcid = 'Sample' AND sdidata.keyid1 = ? AND " + BaseSDIDataEntryAction.getSDICancelWhere("sdidataitem");
                        selectsdidataitemlimits = "SELECT sdidataitemlimits.* FROM sdidata, sdidataitemlimits WHERE sdidata.sdcid = 'Sample' AND sdidata.keyid1 = ? AND " + BaseSDIDataEntryAction.getSDICancelWhere("sdidataitemlimits");
                    }
                    this.database.createPreparedResultSet("getdataitems", selectsdidataitem, new String[]{sample});
                    DataSet dataitems = new DataSet(this.database.getResultSet("getdataitems"));
                    this.database.createPreparedResultSet("getdataitemlimits", selectsdidataitemlimits, new String[]{sample});
                    DataSet dataitemlimits = new DataSet(this.database.getResultSet("getdataitemlimits"));
                    if (dataitems.getRowCount() > 0 && linkdataitems.findRow("keyid1", sample) < 0) {
                        linkdataitems.addAll(dataitems);
                        linkdataitemlimits.addAll(dataitemlimits);
                    }
                }
                if (!populateFlag.equalsIgnoreCase("dataset") || !datasetFilter) continue;
                this.database.createPreparedResultSet("getdatasets", "select * from sdidata where sdcid = 'Sample' and keyid1 = ?" + (this.includeCancelledDatasets ? " " : " AND sdidata.s_datasetstatus <> 'Cancelled' "), new String[]{sample});
                DataSet datasets = new DataSet(this.database.getResultSet("getdatasets"));
                if (datasets.getRowCount() <= 0 || linkdatasets.findRow("keyid1", sample) >= 0) continue;
                linkdatasets.addAll(datasets);
            }
        }
    }

    private boolean crossCalcArray(DataSet sdidataitems, String sdcid, String keyid1, String keyid2, String keyid3, HashMap<String, Object> difilter, String arrayItemRepeatCount, String arrayItemDilFactor, BigDecimal currentReplicate) throws SapphireException {
        boolean multiSDIs = true;
        StringBuffer sql = new StringBuffer();
        ArrayList<Object> sqlParams = new ArrayList<Object>();
        sql.append(" SELECT DISTINCT aidi.* FROM sdidataitem aidi, sdidata aids, arrayitemcontent aic, arrayitem ai, sdiworkitem sdiwi ").append(" WHERE aidi.sdcid = aids.sdcid AND aidi.keyid1 = aids.keyid1 AND aidi.keyid2 = aids.keyid2 AND aidi.keyid3 = aids.keyid3 ").append(" AND aidi.paramlistid = aids.paramlistid AND aidi.paramlistversionid = aids.paramlistversionid ").append(" AND aidi.variantid = aids.variantid AND aidi.dataset = aids.dataset AND aids.sdcid = 'LV_ArrayItem' AND aids.keyid1 = ai.arrayitemid ");
        if (this.ArrayMethodPart == null || this.ArrayMethodPart.trim().length() == 0) {
            throw new SapphireException("CALC_PARAM_ERROR: ArrayMethod not specified!");
        }
        if (!"*".equals(this.ArrayMethodPart)) {
            sql.append(" AND aids.arraymethodid = ? ");
            sqlParams.add(this.ArrayMethodPart);
        }
        sql.append(" AND ai.arrayitemid = aic.arrayitemid AND aic.contentsdcid = sdiwi.sdcid AND aic.contentkeyid1 = sdiwi.keyid1  AND sdiwi.sdcid = ? AND sdiwi.keyid1 = ?");
        sqlParams.add(sdcid);
        sqlParams.add(keyid1);
        if (arrayItemRepeatCount != null && arrayItemRepeatCount.length() > 0) {
            if ("max".equalsIgnoreCase(arrayItemRepeatCount)) {
                sql.append(" AND aic.repeatnum = ( SELECT max(repeatnum) FROM arrayitemcontent ic  WHERE ic.contentsdcid = sdiwi.sdcid AND ic.contentkeyid1 = sdiwi.keyid1 )");
            } else if ("ReplicateId".equalsIgnoreCase(arrayItemRepeatCount) || "#Replicate".equalsIgnoreCase(arrayItemRepeatCount)) {
                sql.append(" AND aic.repeatnum = ?");
                sqlParams.add(currentReplicate);
            } else {
                sql.append(" AND aic.repeatnum = ?");
                sqlParams.add(arrayItemRepeatCount);
            }
        }
        if (arrayItemDilFactor != null && arrayItemDilFactor.length() > 0) {
            sql.append(" AND aic.dilutionfactor = ?");
            sqlParams.add(arrayItemDilFactor);
        }
        sql.append(" ORDER BY aidi.sdcid, aidi.keyid1, aidi.keyid2, aidi.keyid3");
        this.logger.debug("Looking for the related ArrayItem dataitems using " + sql + " and " + sdcid + "/" + keyid1 + "/" + keyid2 + "/" + keyid3);
        this.database.createPreparedResultSet("arrayitemdataitems", sql.toString(), sqlParams.toArray());
        DataSet linkdataitems = new DataSet();
        linkdataitems.setResultSet(this.database.getResultSet("arrayitemdataitems"));
        this.logger.debug("ArrayItem dataitems: " + linkdataitems.toString());
        String keySet = Integer.toString(this.calcKeySet++);
        linkdataitems.setString(-1, "_calckeyset", keySet);
        this.cleanNullCalcExcludeFlag(linkdataitems);
        this.baseSDIDataEntryAction.appendLinkedDataItems(sdidataitems, linkdataitems);
        difilter.put("_calckeyset", keySet);
        return multiSDIs;
    }

    private boolean crossCalcSDI(DataSet sdidata, DataSet sdidataitems, DataSet sdidataitemlimits, String sdcid, boolean datasetFilter, HashMap<String, Object> difilter, String sdiLinkDetails) throws SapphireException {
        boolean multiSDIs = false;
        String[] subparts = StringUtil.split(sdiLinkDetails, ";");
        String linksdcid = "";
        String linkkeyid1 = "";
        String linkkeyid2 = "(null)";
        String linkkeyid3 = "(null)";
        if (subparts.length == 1) {
            linksdcid = sdcid;
            linkkeyid1 = subparts[0].trim();
        } else if (subparts.length == 2) {
            linksdcid = subparts[0].trim();
            linkkeyid1 = subparts[1].trim();
        } else if (subparts.length == 3) {
            linksdcid = subparts[0].trim();
            linkkeyid1 = subparts[1].trim();
            linkkeyid2 = subparts[2].trim();
        } else if (subparts.length == 4) {
            linksdcid = subparts[0].trim();
            linkkeyid1 = subparts[1].trim();
            linkkeyid2 = subparts[2].trim();
            linkkeyid3 = subparts[3].trim();
        }
        String sdiKey = "sdi:" + linksdcid + ";" + linkkeyid1 + ";" + linkkeyid2 + ";" + linkkeyid3;
        if (this.keySetCache.containsKey(sdiKey)) {
            difilter.put("sdcid", linksdcid);
            difilter.put("keyid1", linkkeyid1);
            difilter.put("keyid2", linkkeyid2);
            difilter.put("keyid3", linkkeyid3);
            this.logger.debug("Cross Calc Filter: Skipped sdi retrieval.");
        } else {
            DataSet linkdataitems = new DataSet();
            DataSet linkdataitemlimits = new DataSet();
            if (this.isFormulationCalc || this.isExtraCalc) {
                this.addPriorDataSingle(linkdataitems, sdidataitems, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3);
            }
            if (linkdataitems.size() == 0) {
                String selectdataitems = "";
                String selectdataitemlimits = "";
                if (this.includeCancelledDatasets) {
                    selectdataitems = "SELECT sdidataitem.* FROM sdidataitem WHERE sdcid = ? AND keyid1=? AND keyid2=? AND keyid3=? ORDER BY sdcid, keyid1, keyid2, keyid3";
                    selectdataitemlimits = "SELECT sdidataitemlimits.* FROM sdidataitemlimits WHERE sdcid = ? AND keyid1=? AND keyid2=? AND keyid3=? ORDER BY sdcid, keyid1, keyid2, keyid3";
                } else {
                    selectdataitems = "SELECT sdidataitem.* FROM sdidata, sdidataitem WHERE sdidata.sdcid = ? AND sdidata.keyid1 = ? AND sdidata.keyid2 = ? AND sdidata.keyid3 = ? AND " + BaseSDIDataEntryAction.getSDICancelWhere("sdidataitem") + "ORDER BY sdidataitem.sdcid, sdidataitem.keyid1, sdidataitem.keyid2, sdidataitem.keyid3";
                    selectdataitemlimits = "SELECT sdidataitemlimits.* FROM sdidata, sdidataitemlimits WHERE sdidata.sdcid = ? AND sdidata.keyid1 = ? AND sdidata.keyid2 = ? AND sdidata.keyid3 = ? AND " + BaseSDIDataEntryAction.getSDICancelWhere("sdidataitemlimits") + "ORDER BY sdidataitemlimits.sdcid, sdidataitemlimits.keyid1, sdidataitemlimits.keyid2, sdidataitemlimits.keyid3";
                }
                this.database.createPreparedResultSet(selectdataitems, new String[]{linksdcid, linkkeyid1, linkkeyid2, linkkeyid3});
                linkdataitems.setResultSet(this.database.getResultSet());
                this.logger.debug("linkdataitems: " + linkdataitems.toString());
                this.database.createPreparedResultSet(selectdataitemlimits, new String[]{linksdcid, linkkeyid1, linkkeyid2, linkkeyid3});
                linkdataitemlimits.setResultSet(this.database.getResultSet());
                this.logger.debug("linksdidataitemlimits: " + linkdataitemlimits.toString());
                this.cleanNullCalcExcludeFlag(linkdataitems);
            }
            this.keySetCache.put(sdiKey, "-1");
            this.baseSDIDataEntryAction.appendLinkedDataItems(sdidataitems, linkdataitems);
            this.baseSDIDataEntryAction.appendLinkedDataItemLimits(sdidataitemlimits, linkdataitemlimits);
            difilter.put("sdcid", linksdcid);
            difilter.put("keyid1", linkkeyid1);
            difilter.put("keyid2", linkkeyid2);
            difilter.put("keyid3", linkkeyid3);
        }
        if (datasetFilter && !this.datasetFilterKeysRetrieved.contains(sdiKey)) {
            DataSet linkdataset = new DataSet();
            if (this.isFormulationCalc || this.isExtraCalc) {
                this.addPriorDataSingle(linkdataset, sdidata, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3);
            }
            if (linkdataset.size() == 0) {
                this.logger.debug("Fetching sdidata information for the referenced sample.");
                String dsSql = "SELECT sdidata.* FROM sdidata WHERE sdcid = ? AND keyid1=? AND keyid2=? AND keyid3=?" + (this.includeCancelledDatasets ? " " : " AND sdidata.s_datasetstatus <> 'Cancelled' ") + "ORDER BY sdcid, keyid1, keyid2, keyid3";
                this.database.createPreparedResultSet(dsSql, new String[]{linksdcid, linkkeyid1, linkkeyid2, linkkeyid3});
                linkdataset.setResultSet(this.database.getResultSet());
            }
            this.logger.debug("linkdataset: " + linkdataset.toString());
            this.datasetFilterKeysRetrieved.add(sdiKey);
            this.baseSDIDataEntryAction.appendLinkedDataSet(sdidata, linkdataset);
        }
        return multiSDIs;
    }

    private boolean crossCalcLink(DataSet primary, DataSet sdidata, DataSet sdidataitems, DataSet sdidataitemlimits, SDCProcessor sdcProcessor, PropertyList sdcProps, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, BigDecimal dataset, String paramid, String paramtype, BigDecimal replicateid, String token, boolean datasetFilter, boolean isPrimaryColOrAttribute, HashMap<String, Object> difilter, String sdiLinkDetails) throws SapphireException {
        boolean multiSDIs = false;
        String[] subparts = StringUtil.split(sdiLinkDetails, ";");
        boolean forwardLink = subparts.length == 1;
        String linkSDCPart = subparts.length == 1 ? sdcid : subparts[0].trim();
        String linkLinkidPart = subparts.length == 1 ? subparts[0].trim() : subparts[1].trim();
        PropertyList linkSDCProps = sdcProcessor.getPropertyList(linkSDCPart);
        PropertyListCollection links = linkSDCProps.getCollectionNotNull("links");
        PropertyList link = links.find("sdccolumnid", linkLinkidPart);
        if (link == null) {
            link = links.find("linkid", linkLinkidPart);
        }
        boolean otherLinkedSDC = false;
        if (link == null && forwardLink) {
            String[] otherSDCLinks = new String[]{"SDIWorkItem", "DataSet", "DataItem"};
            for (int k = 0; k < otherSDCLinks.length; ++k) {
                linkSDCPart = otherSDCLinks[k];
                linkSDCProps = sdcProcessor.getPropertyList(linkSDCPart);
                links = linkSDCProps.getCollectionNotNull("links");
                link = links.find("sdccolumnid", linkLinkidPart);
                if (link == null) {
                    link = links.find("linkid", linkLinkidPart);
                }
                if (link == null) continue;
                otherLinkedSDC = true;
                break;
            }
        }
        if (link != null) {
            String linkType = link.getProperty("linktype");
            if (linkType.equals("F") && forwardLink) {
                int primaryRow;
                String keycolid1 = sdcProps.getProperty("keycolid1");
                String keycolid2 = sdcProps.getProperty("keycolid2");
                String keycolid3 = sdcProps.getProperty("keycolid3");
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put("sdcid", sdcid);
                findMap.put(keycolid1, keyid1);
                if (keycolid2.length() > 0) {
                    findMap.put(keycolid2, keyid2);
                }
                if (keycolid3.length() > 0) {
                    findMap.put(keycolid3, keyid3);
                }
                if ((primaryRow = primary.findRow(findMap)) >= 0) {
                    String linkcolumnid1 = link.getProperty("sdccolumnid");
                    String linkcolumnid2 = link.getProperty("sdccolumnid2");
                    String linkcolumnid3 = link.getProperty("sdccolumnid3");
                    String linkkeyid1 = "";
                    String linkkeyid2 = "";
                    String linkkeyid3 = "";
                    if (otherLinkedSDC) {
                        HashMap<String, Object> find = new HashMap<String, Object>();
                        find.put("sdcid", sdcid);
                        find.put("keyid1", keyid1);
                        find.put("keyid2", keyid2);
                        find.put("keyid3", keyid3);
                        find.put("paramlistid", paramlistid);
                        find.put("paramlistversionid", paramlistversionid);
                        find.put("variantid", variantid);
                        find.put("dataset", dataset);
                        int datasetRow = sdidata.findRow(find);
                        if ("SDIWorkItem".equals(linkSDCPart)) {
                            if (datasetRow > -1) {
                                String workItemId = sdidata.getValue(datasetRow, "sourceworkitemid");
                                String workItemInstance = sdidata.getValue(datasetRow, "sourceworkiteminstance");
                                this.database.createPreparedResultSet("select * from sdiworkitem where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ? and workitemid = ? and workiteminstance = ?", new String[]{sdcid, keyid1, keyid2, keyid3, workItemId, workItemInstance});
                                DataSet dsSDIWorkItem = new DataSet();
                                dsSDIWorkItem.setResultSet(this.database.getResultSet());
                                this.logger.debug("sdiworkitem: " + dsSDIWorkItem.toString());
                                linkkeyid1 = dsSDIWorkItem.getValue(0, linkcolumnid1);
                                linkkeyid2 = linkcolumnid2.length() > 0 ? dsSDIWorkItem.getValue(0, linkcolumnid2) : "(null)";
                                linkkeyid3 = linkcolumnid3.length() > 0 ? dsSDIWorkItem.getValue(0, linkcolumnid3) : "(null)";
                            }
                        } else if ("DataSet".equals(linkSDCPart)) {
                            linkkeyid1 = sdidata.getValue(datasetRow, linkcolumnid1);
                            linkkeyid2 = linkcolumnid2.length() > 0 ? sdidata.getValue(datasetRow, linkcolumnid2) : "(null)";
                            linkkeyid3 = linkcolumnid3.length() > 0 ? sdidata.getValue(datasetRow, linkcolumnid3) : "(null)";
                        } else if ("DataItem".equals(linkSDCPart)) {
                            find.put("paramid", paramid);
                            find.put("paramtype", paramtype);
                            find.put("replicateid", replicateid);
                            int dataitemRow = sdidataitems.findRow(find);
                            linkkeyid1 = sdidataitems.getValue(dataitemRow, linkcolumnid1);
                            linkkeyid2 = linkcolumnid2.length() > 0 ? sdidataitems.getValue(dataitemRow, linkcolumnid2) : "(null)";
                            linkkeyid3 = linkcolumnid3.length() > 0 ? sdidataitems.getValue(dataitemRow, linkcolumnid3) : "(null)";
                        }
                    } else {
                        linkkeyid1 = primary.getValue(primaryRow, linkcolumnid1);
                        linkkeyid2 = linkcolumnid2.length() > 0 ? primary.getValue(primaryRow, linkcolumnid2) : "(null)";
                        linkkeyid3 = linkcolumnid3.length() > 0 ? primary.getValue(primaryRow, linkcolumnid3) : "(null)";
                    }
                    String linksdcid = link.getProperty("linksdcid");
                    String forwardKey = "forwardlink:" + linksdcid + ";" + linkkeyid1 + ";" + linkkeyid2 + ";" + linkkeyid3;
                    if (this.keySetCache.containsKey(forwardKey)) {
                        difilter.put("sdcid", linksdcid);
                        difilter.put("keyid1", linkkeyid1);
                        difilter.put("keyid2", linkkeyid2);
                        difilter.put("keyid3", linkkeyid3);
                        this.logger.debug("Cross Calc Filter: Skipped forward link retrieval.");
                    } else {
                        DataSet linkdataitems = new DataSet();
                        DataSet linkdataitemlimits = new DataSet();
                        if (this.isFormulationCalc || this.isExtraCalc) {
                            this.addPriorDataSingle(linkdataitems, sdidataitems, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3);
                        }
                        if (linkdataitems.size() == 0) {
                            String selectdataitems = "";
                            String selectdataitemlimits = "";
                            if (this.includeCancelledDatasets) {
                                selectdataitems = "SELECT sdidataitem.* FROM sdidataitem WHERE sdcid = ? AND keyid1=? AND keyid2=? AND keyid3=? ORDER BY sdcid, keyid1, keyid2, keyid3";
                                selectdataitemlimits = "SELECT sdidataitemlimits.* FROM sdidataitemlimits WHERE sdcid = ? AND keyid1=? AND keyid2=? AND keyid3=? ORDER BY sdcid, keyid1, keyid2, keyid3";
                            } else {
                                selectdataitems = "SELECT sdidataitem.* FROM sdidata, sdidataitem WHERE sdidata.sdcid = ? AND sdidata.keyid1 = ? AND sdidata.keyid2 = ? AND sdidata.keyid3 = ? AND " + BaseSDIDataEntryAction.getSDICancelWhere("sdidataitem") + "ORDER BY sdidataitem.sdcid, sdidataitem.keyid1, sdidataitem.keyid2, sdidataitem.keyid3";
                                selectdataitemlimits = "SELECT sdidataitemlimits.* FROM sdidata, sdidataitemlimits WHERE sdidata.sdcid = ? AND sdidata.keyid1 = ? AND sdidata.keyid2 = ? AND sdidata.keyid3 = ? AND " + BaseSDIDataEntryAction.getSDICancelWhere("sdidataitemlimits") + "ORDER BY sdidataitemlimits.sdcid, sdidataitemlimits.keyid1, sdidataitemlimits.keyid2, sdidataitemlimits.keyid3";
                            }
                            this.database.createPreparedResultSet(selectdataitems, new String[]{linksdcid, linkkeyid1, linkkeyid2, linkkeyid3});
                            linkdataitems.setResultSet(this.database.getResultSet());
                            this.logger.debug("linkdataitems: " + linkdataitems.toString());
                            this.database.createPreparedResultSet(selectdataitemlimits, new String[]{linksdcid, linkkeyid1, linkkeyid2, linkkeyid3});
                            linkdataitemlimits.setResultSet(this.database.getResultSet());
                            this.logger.debug("linksdidataitemlimits: " + linkdataitemlimits.toString());
                            this.cleanNullCalcExcludeFlag(linkdataitems);
                        }
                        this.keySetCache.put(forwardKey, "-1");
                        this.baseSDIDataEntryAction.appendLinkedDataItems(sdidataitems, linkdataitems);
                        this.baseSDIDataEntryAction.appendLinkedDataItemLimits(sdidataitemlimits, linkdataitemlimits);
                        difilter.put("sdcid", linksdcid);
                        difilter.put("keyid1", linkkeyid1);
                        difilter.put("keyid2", linkkeyid2);
                        difilter.put("keyid3", linkkeyid3);
                    }
                    if (datasetFilter && !this.datasetFilterKeysRetrieved.contains(forwardKey)) {
                        DataSet linkdataset = new DataSet();
                        if (this.isFormulationCalc || this.isExtraCalc) {
                            this.addPriorDataSingle(linkdataset, sdidata, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3);
                        }
                        if (linkdataset.size() == 0) {
                            this.logger.debug("Fetching sdidata information for the referenced sample.");
                            String dsSql = "SELECT sdidata.* FROM sdidata WHERE sdcid = ? AND keyid1=? AND keyid2=? AND keyid3=? " + (this.includeCancelledDatasets ? " " : " AND sdidata.s_datasetstatus <> 'Cancelled' ") + "ORDER BY sdcid, keyid1, keyid2, keyid3";
                            this.database.createPreparedResultSet(dsSql, new String[]{linksdcid, linkkeyid1, linkkeyid2, linkkeyid3});
                            linkdataset.setResultSet(this.database.getResultSet());
                        }
                        this.logger.debug("linkdataset: " + linkdataset.toString());
                        this.datasetFilterKeysRetrieved.add(forwardKey);
                        this.baseSDIDataEntryAction.appendLinkedDataSet(sdidata, linkdataset);
                    }
                } else {
                    this.logger.info("CALC_PARAM_ERROR: Could not find the primary record for " + token);
                    difilter.put("dummy", "dummy");
                }
            } else if (linkType.equals("F") && !forwardLink) {
                String linkcolumnid1 = link.getProperty("sdccolumnid");
                String linkcolumnid2 = link.getProperty("sdccolumnid2");
                String linkcolumnid3 = link.getProperty("sdccolumnid3");
                String linkedtableid = linkSDCProps.getProperty("tableid");
                String linkedkeycolid1 = linkSDCProps.getProperty("keycolid1");
                String linkedkeycolid2 = linkSDCProps.getProperty("keycolid2");
                String linkedkeycolid3 = linkSDCProps.getProperty("keycolid3");
                String backwardKey = "backward:" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + linkcolumnid1 + ";" + linkcolumnid2 + ";" + linkcolumnid3 + ";" + linkedtableid + ";" + linkedkeycolid1 + ";" + linkedkeycolid2 + ";" + linkedkeycolid3;
                ArrayList<String> params = new ArrayList<String>();
                params.add(linkSDCPart);
                params.add(keyid1);
                if (linkcolumnid2.length() > 0) {
                    params.add(keyid2);
                }
                if (linkcolumnid3.length() > 0) {
                    params.add(keyid3);
                }
                String keySet = null;
                if (this.keySetCache.containsKey(backwardKey)) {
                    keySet = this.keySetCache.get(backwardKey);
                    difilter.put("_calckeyset", keySet);
                    this.logger.debug("Cross Calc Filter: Skipped backward link retrieval.");
                    multiSDIs = true;
                } else {
                    String selectdataitems = "";
                    String selectdataitemlimits = "";
                    if (this.includeCancelledDatasets) {
                        selectdataitems = "SELECT di.* FROM sdidataitem di, " + linkedtableid + " WHERE di.sdcid=? AND di.keyid1=" + linkedtableid + "." + linkedkeycolid1 + (linkedkeycolid2.length() > 0 ? " AND di.keyid2=" + linkedtableid + "." + linkedkeycolid2 : "") + (linkedkeycolid3.length() > 0 ? " AND di.keyid3=" + linkedtableid + "." + linkedkeycolid3 : "") + " AND " + linkedtableid + "." + linkcolumnid1 + "=?" + (linkcolumnid2.length() > 0 ? " AND " + linkedtableid + "." + linkcolumnid2 + "=?" : "") + (linkcolumnid3.length() > 0 ? " AND " + linkedtableid + "." + linkcolumnid3 + "=?" : "") + " ORDER BY di.sdcid, di.keyid1, di.keyid2, di.keyid3";
                        selectdataitemlimits = "SELECT dil.* FROM sdidataitemlimits dil, " + linkedtableid + " WHERE dil.sdcid=? AND dil.keyid1=" + linkedtableid + "." + linkedkeycolid1 + (linkedkeycolid2.length() > 0 ? " AND dil.keyid2=" + linkedtableid + "." + linkedkeycolid2 : "") + (linkedkeycolid3.length() > 0 ? " AND dil.keyid3=" + linkedtableid + "." + linkedkeycolid3 : "") + " AND " + linkedtableid + "." + linkcolumnid1 + "=?" + (linkcolumnid2.length() > 0 ? " AND " + linkedtableid + "." + linkcolumnid2 + "=?" : "") + (linkcolumnid3.length() > 0 ? " AND " + linkedtableid + "." + linkcolumnid3 + "=?" : "") + " ORDER BY dil.sdcid, dil.keyid1, dil.keyid2, dil.keyid3";
                    } else {
                        selectdataitems = "SELECT di.* FROM sdidata ds, sdidataitem di, " + linkedtableid + " WHERE ds.sdcid=? AND ds.keyid1=" + linkedtableid + "." + linkedkeycolid1 + " AND ds.s_datasetstatus <> 'Cancelled' " + (linkedkeycolid2.length() > 0 ? " AND ds.keyid2=" + linkedtableid + "." + linkedkeycolid2 : "") + (linkedkeycolid3.length() > 0 ? " AND ds.keyid3=" + linkedtableid + "." + linkedkeycolid3 : "") + " AND " + linkedtableid + "." + linkcolumnid1 + "=?" + (linkcolumnid2.length() > 0 ? " AND " + linkedtableid + "." + linkcolumnid2 + "=?" : "") + (linkcolumnid3.length() > 0 ? " AND " + linkedtableid + "." + linkcolumnid3 + "=?" : "") + " AND di.sdcid = ds.sdcid AND di.keyid1 = ds.keyid1 AND di.keyid2 = ds.keyid2 AND di.keyid3 = ds.keyid3  AND di.paramlistid = ds.paramlistid AND di.paramlistversionid = ds.paramlistversionid AND di.variantid = ds.variantid AND di.dataset = ds.dataset  ORDER BY di.sdcid, di.keyid1, di.keyid2, di.keyid3";
                        selectdataitemlimits = "SELECT dil.* FROM sdidata ds, sdidataitemlimits dil, " + linkedtableid + " WHERE ds.sdcid=? AND ds.keyid1=" + linkedtableid + "." + linkedkeycolid1 + " AND ds.s_datasetstatus <> 'Cancelled' " + (linkedkeycolid2.length() > 0 ? " AND ds.keyid2=" + linkedtableid + "." + linkedkeycolid2 : "") + (linkedkeycolid3.length() > 0 ? " AND ds.keyid3=" + linkedtableid + "." + linkedkeycolid3 : "") + " AND " + linkedtableid + "." + linkcolumnid1 + "=?" + (linkcolumnid2.length() > 0 ? " AND " + linkedtableid + "." + linkcolumnid2 + "=?" : "") + (linkcolumnid3.length() > 0 ? " AND " + linkedtableid + "." + linkcolumnid3 + "=?" : "") + " AND dil.sdcid = ds.sdcid AND dil.keyid1 = ds.keyid1 AND dil.keyid2 = ds.keyid2 AND dil.keyid3 = ds.keyid3  AND dil.paramlistid = ds.paramlistid AND dil.paramlistversionid = ds.paramlistversionid AND dil.variantid = ds.variantid AND dil.dataset = ds.dataset  ORDER BY dil.sdcid, dil.keyid1, dil.keyid2, dil.keyid3";
                    }
                    this.database.createPreparedResultSet(selectdataitems, params.toArray());
                    DataSet linkdataitems = new DataSet();
                    linkdataitems.setResultSet(this.database.getResultSet());
                    this.database.createPreparedResultSet(selectdataitemlimits, params.toArray());
                    DataSet linkdataitemlimits = new DataSet();
                    linkdataitemlimits.setResultSet(this.database.getResultSet());
                    if (this.isFormulationCalc || this.isExtraCalc) {
                        this.mergeMultiDataItems(linkdataitems, sdidataitems, sdidata, sdidataitems);
                    }
                    this.logger.debug("linkdataitems: " + linkdataitems.toString());
                    multiSDIs = true;
                    keySet = Integer.toString(this.calcKeySet++);
                    this.keySetCache.put(backwardKey, keySet);
                    linkdataitems.setString(-1, "_calckeyset", keySet);
                    this.cleanNullCalcExcludeFlag(linkdataitems);
                    this.baseSDIDataEntryAction.appendLinkedDataItems(sdidataitems, linkdataitems);
                    this.baseSDIDataEntryAction.appendLinkedDataItemLimits(sdidataitemlimits, linkdataitemlimits);
                    difilter.put("_calckeyset", keySet);
                }
                if (isPrimaryColOrAttribute) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("_calckeyset", (String)difilter.get("_calckeyset"));
                    DataSet primaries = primary.getFilteredDataSet(filter);
                    if (primaries.getRowCount() == 0) {
                        String linkedPrimarysql = "SELECT * FROM " + linkedtableid + " WHERE " + linkcolumnid1 + "=?" + (linkcolumnid2.length() > 0 ? " AND " + linkcolumnid2 + "=?" : "") + (linkcolumnid3.length() > 0 ? " AND " + linkcolumnid3 + "=?" : "");
                        ArrayList<String> primaryParams = new ArrayList<String>();
                        primaryParams.add(keyid1);
                        if (linkcolumnid2.length() > 0) {
                            primaryParams.add(keyid2);
                        }
                        if (linkcolumnid3.length() > 0) {
                            primaryParams.add(keyid3);
                        }
                        this.database.createPreparedResultSet("primary", linkedPrimarysql, primaryParams.toArray());
                        DataSet linkedPrimary = new DataSet(this.database.getResultSet("primary"));
                        this.database.closeResultSet("primary");
                        linkedPrimary.setString(-1, "sdcid", linkSDCPart);
                        linkedPrimary.setString(-1, "_calckeyset", keySet);
                        primary.copyRow(linkedPrimary, -1, 1);
                    }
                }
                if (datasetFilter && !this.datasetFilterKeysRetrieved.contains(backwardKey)) {
                    this.logger.debug("Fetching sdidata information for the referenced sample.");
                    String dsSql = "SELECT ds.* FROM sdidata ds, " + linkedtableid + " WHERE ds.sdcid=? AND ds.keyid1=" + linkedtableid + "." + linkedkeycolid1 + (linkedkeycolid2.length() > 0 ? " AND ds.keyid2=" + linkedtableid + "." + linkedkeycolid2 : "") + (linkedkeycolid3.length() > 0 ? " AND ds.keyid3=" + linkedtableid + "." + linkedkeycolid3 : "") + " AND " + linkedtableid + "." + linkcolumnid1 + "=?" + (linkcolumnid2.length() > 0 ? " AND " + linkedtableid + "." + linkcolumnid2 + "=?" : "") + (linkcolumnid3.length() > 0 ? " AND " + linkedtableid + "." + linkcolumnid3 + "=?" : "") + (this.includeCancelledDatasets ? " " : " AND ds.s_datasetstatus <> 'Cancelled' ") + " ORDER BY ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3";
                    this.database.createPreparedResultSet(dsSql, params.toArray());
                    DataSet linkdataset = new DataSet();
                    linkdataset.setResultSet(this.database.getResultSet());
                    if (this.isFormulationCalc || this.isExtraCalc) {
                        this.mergeMultiDataSets(linkdataset, sdidata);
                    }
                    this.logger.debug("linkdataset: " + linkdataset.toString());
                    linkdataset.setString(-1, "_calckeyset", keySet);
                    this.datasetFilterKeysRetrieved.add(backwardKey);
                    this.baseSDIDataEntryAction.appendLinkedDataSet(sdidata, linkdataset);
                }
            }
        } else {
            this.logger.info("CALC_PARAM_ERROR: Could not find the link for " + token);
            difilter.put("dummy", "dummy");
        }
        return multiSDIs;
    }

    private boolean crossCalcSDIRelation(DataSet sdidata, DataSet sdidataitems, DataSet sdidataitemlimits, String sdcid, String keyid1, String keyid2, String keyid3, boolean datasetFilter, HashMap<String, Object> difilter, String sdiLinkDetails, String sdiType, boolean isPrimaryColOrAttribute, DataSet primary) throws SapphireException {
        boolean multiSDIs = true;
        String keySet = null;
        boolean sdiRelation = "sdirelation".equalsIgnoreCase(sdiType);
        String sdirelationKey = sdiType + ";" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + sdiLinkDetails;
        int keyCols = Integer.parseInt(this.sdcProcessor.getProperty(sdcid, "keycolumns"));
        if (this.keySetCache.containsKey(sdirelationKey)) {
            keySet = this.keySetCache.get(sdirelationKey);
            difilter.put("_calckeyset", keySet);
            this.logger.debug("Cross Calc Filter: Skipped " + sdiType + ":" + sdiLinkDetails + " retrieval for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
        } else {
            SafeSQL safeSQLDI = new SafeSQL();
            SafeSQL safeSQLDIL = new SafeSQL();
            StringBuilder selectdataitems = new StringBuilder();
            StringBuilder selectdataitemlimits = new StringBuilder();
            if (this.includeCancelledDatasets) {
                selectdataitems.append("SELECT di.* FROM sdidataitem di, sdirelation sdir ");
                selectdataitemlimits.append("SELECT dil.* FROM sdidataitemlimits dil, sdirelation sdir ");
            } else {
                selectdataitems.append("SELECT di.* FROM sdidata ds, sdidataitem di, sdirelation sdir ");
                selectdataitemlimits.append("SELECT dil.* FROM sdidata ds, sdidataitemlimits dil, sdirelation sdir ");
            }
            selectdataitems.append(" WHERE ").append(sdiRelation ? " sdir.fromsdcid = " + safeSQLDI.addVar(sdcid) + " AND sdir.fromkeyid1 = " + safeSQLDI.addVar(keyid1) + " " : " sdir.tosdcid = " + safeSQLDI.addVar(sdcid) + " AND sdir.tokeyid1 = " + safeSQLDI.addVar(keyid1) + " ");
            selectdataitemlimits.append(" WHERE ").append(sdiRelation ? " sdir.fromsdcid = " + safeSQLDIL.addVar(sdcid) + " AND sdir.fromkeyid1 = " + safeSQLDIL.addVar(keyid1) + " " : " sdir.tosdcid = " + safeSQLDIL.addVar(sdcid) + " AND sdir.tokeyid1 = " + safeSQLDIL.addVar(keyid1) + " ");
            if (keyCols == 2) {
                selectdataitems.append(" AND ").append(sdiRelation ? " sdir.fromkeyid2 = " + safeSQLDI.addVar(keyid2) + " " : "sdir.tokeyid2 = " + safeSQLDI.addVar(keyid2) + " ");
                selectdataitemlimits.append(" AND ").append(sdiRelation ? " sdir.fromkeyid2 = " + safeSQLDIL.addVar(keyid2) + " " : "sdir.tokeyid2 = " + safeSQLDIL.addVar(keyid2) + " ");
            } else if (keyCols == 3) {
                selectdataitems.append(" AND ").append(sdiRelation ? " sdir.fromkeyid3 = " + safeSQLDI.addVar(keyid3) + " " : "sdir.tokeyid3 = " + safeSQLDI.addVar(keyid3) + " ");
                selectdataitemlimits.append(" AND ").append(sdiRelation ? " sdir.fromkeyid3 = " + safeSQLDIL.addVar(keyid3) + " " : "sdir.tokeyid3 = " + safeSQLDIL.addVar(keyid3) + " ");
            }
            if (this.includeCancelledDatasets) {
                selectdataitems.append(" AND sdir.relationtype in ( " + safeSQLDI.addIn(sdiLinkDetails, ";") + " ) AND di.sdcid = ").append(sdiRelation ? " sdir.tosdcid AND di.keyid1 = sdir.tokeyid1 AND di.keyid2 = coalesce( nullif( sdir.tokeyid2, '' ), '(null)' ) AND di.keyid3 = coalesce( nullif( sdir.tokeyid3, '' ), '(null)')" : " sdir.fromsdcid AND di.keyid1 = sdir.fromkeyid1 AND di.keyid2 = coalesce( nullif( sdir.fromkeyid2, '' ), '(null)' ) AND di.keyid3 = coalesce( nullif( sdir.fromkeyid3, '' ), '(null)' )").append(" ORDER BY di.sdcid, di.keyid1, di.keyid2, di.keyid3");
                selectdataitemlimits.append(" AND sdir.relationtype in ( " + safeSQLDIL.addIn(sdiLinkDetails, ";") + " ) AND dil.sdcid = ").append(sdiRelation ? " sdir.tosdcid AND dil.keyid1 = sdir.tokeyid1 AND dil.keyid2 = coalesce( nullif( sdir.tokeyid2, '' ), '(null)' ) AND dil.keyid3 = coalesce( nullif( sdir.tokeyid3, '' ), '(null)')" : " sdir.fromsdcid AND dil.keyid1 = sdir.fromkeyid1 AND dil.keyid2 = coalesce( nullif( sdir.fromkeyid2, '' ), '(null)' ) AND dil.keyid3 = coalesce( nullif( sdir.fromkeyid3, '' ), '(null)' )").append(" ORDER BY dil.sdcid, dil.keyid1, dil.keyid2, dil.keyid3");
            } else {
                selectdataitems.append(" AND sdir.relationtype in ( " + safeSQLDI.addIn(sdiLinkDetails, ";") + " ) AND ds.sdcid = ").append(sdiRelation ? " sdir.tosdcid AND ds.keyid1 = sdir.tokeyid1 AND ds.keyid2 = coalesce( nullif( sdir.tokeyid2, '' ), '(null)' ) AND ds.keyid3 = coalesce( nullif( sdir.tokeyid3, '' ), '(null)')" : " sdir.fromsdcid AND ds.keyid1 = sdir.fromkeyid1 AND ds.keyid2 = coalesce( nullif( sdir.fromkeyid2, '' ), '(null)' ) AND ds.keyid3 = coalesce( nullif( sdir.fromkeyid3, '' ), '(null)' )").append(" AND ds.s_datasetstatus <> 'Cancelled'").append(" AND di.sdcid = ds.sdcid AND di.keyid1 = ds.keyid1 AND di.keyid2 = ds.keyid2 AND di.keyid3 = ds.keyid3").append(" AND di.paramlistid = ds.paramlistid AND di.paramlistversionid = ds.paramlistversionid AND di.variantid = ds.variantid AND di.dataset = ds.dataset").append(" ORDER BY di.sdcid, di.keyid1, di.keyid2, di.keyid3");
                selectdataitemlimits.append(" AND sdir.relationtype in ( " + safeSQLDIL.addIn(sdiLinkDetails, ";") + " ) AND ds.sdcid = ").append(sdiRelation ? " sdir.tosdcid AND ds.keyid1 = sdir.tokeyid1 AND ds.keyid2 = coalesce( nullif( sdir.tokeyid2, '' ), '(null)' ) AND ds.keyid3 = coalesce( nullif( sdir.tokeyid3, '' ), '(null)')" : " sdir.fromsdcid AND ds.keyid1 = sdir.fromkeyid1 AND ds.keyid2 = coalesce( nullif( sdir.fromkeyid2, '' ), '(null)' ) AND ds.keyid3 = coalesce( nullif( sdir.fromkeyid3, '' ), '(null)' )").append(" AND ds.s_datasetstatus <> 'Cancelled'").append(" AND dil.sdcid = ds.sdcid AND dil.keyid1 = ds.keyid1 AND dil.keyid2 = ds.keyid2 AND dil.keyid3 = ds.keyid3").append(" AND dil.paramlistid = ds.paramlistid AND dil.paramlistversionid = ds.paramlistversionid AND dil.variantid = ds.variantid AND dil.dataset = ds.dataset").append(" ORDER BY dil.sdcid, dil.keyid1, dil.keyid2, dil.keyid3");
            }
            this.logger.debug("Looking for " + sdiType + " using " + selectdataitems + " and " + sdcid + "/" + keyid1 + "/" + keyid2 + "/" + keyid3);
            this.database.createPreparedResultSet(selectdataitems.toString(), safeSQLDI.getValues());
            DataSet linkdataitems = new DataSet();
            linkdataitems.setResultSet(this.database.getResultSet());
            this.database.createPreparedResultSet(selectdataitemlimits.toString(), safeSQLDIL.getValues());
            DataSet linkdataitemlimits = new DataSet();
            linkdataitemlimits.setResultSet(this.database.getResultSet());
            if (this.isFormulationCalc || this.isExtraCalc) {
                this.mergeMultiDataItems(linkdataitems, sdidataitems, sdidata, sdidataitems);
            }
            this.logger.debug("linkdataitems: " + linkdataitems);
            keySet = Integer.toString(this.calcKeySet++);
            this.keySetCache.put(sdirelationKey, keySet);
            linkdataitems.setString(-1, "_calckeyset", keySet);
            this.cleanNullCalcExcludeFlag(linkdataitems);
            this.baseSDIDataEntryAction.appendLinkedDataItems(sdidataitems, linkdataitems);
            this.baseSDIDataEntryAction.appendLinkedDataItemLimits(sdidataitemlimits, linkdataitemlimits);
            difilter.put("_calckeyset", keySet);
        }
        if (isPrimaryColOrAttribute) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("_calckeyset", (String)difilter.get("_calckeyset"));
            DataSet primaries = primary.getFilteredDataSet(filter);
            if (primaries.getRowCount() == 0) {
                StringBuffer sql = new StringBuffer();
                SafeSQL safeSQL = new SafeSQL();
                sql.append("select * from sdirelation where ").append(sdiRelation ? " fromsdcid = " + safeSQL.addVar(sdcid) + " AND fromkeyid1 = " + safeSQL.addVar(keyid1) : " tosdcid = " + safeSQL.addVar(sdcid) + " AND tokeyid1 = " + safeSQL.addVar(keyid1));
                if (keyCols == 2) {
                    sql.append(" AND ").append(sdiRelation ? " fromkeyid2 = " + safeSQL.addVar(keyid2) + " " : "tokeyid2 = " + safeSQL.addVar(keyid2) + " ");
                } else if (keyCols == 3) {
                    sql.append(" AND ").append(sdiRelation ? " fromkeyid3 = " + safeSQL.addVar(keyid3) + " " : "tokeyid3 = " + safeSQL.addVar(keyid3) + " ");
                }
                sql.append(" AND relationtype in ( " + safeSQL.addIn(sdiLinkDetails, ";") + " )");
                this.database.createPreparedResultSet(sql.toString(), safeSQL.getValues());
                DataSet sdiRelationRow = new DataSet(this.database.getResultSet());
                for (int r = 0; r < sdiRelationRow.getRowCount(); ++r) {
                    String linkedSDC = sdiRelationRow.getValue(r, sdiRelation ? "tosdcid" : "fromsdcid");
                    String linkedKeyid1 = sdiRelationRow.getValue(r, sdiRelation ? "tokeyid1" : "fromkeyid1");
                    String linkedKeyid2 = sdiRelationRow.getValue(r, sdiRelation ? "tokeyid2" : "fromkeyid2");
                    String linkedKeyid3 = sdiRelationRow.getValue(r, sdiRelation ? "tokeyid3" : "fromkeyid3");
                    String linkedtableId = this.sdcProcessor.getProperty(linkedSDC, "tableid");
                    String linkedcolumnid1 = this.sdcProcessor.getProperty(linkedSDC, "keycolid1");
                    String linkedcolumnid2 = this.sdcProcessor.getProperty(linkedSDC, "keycolid2");
                    String linkedcolumnid3 = this.sdcProcessor.getProperty(linkedSDC, "keycolid3");
                    String linkedPrimarysql = "SELECT * FROM " + linkedtableId + " WHERE " + linkedcolumnid1 + " = ?" + (linkedcolumnid2.length() > 0 ? " AND " + linkedcolumnid2 + " = ?" : "") + (linkedcolumnid3.length() > 0 ? " AND " + linkedcolumnid3 + " = ?" : "");
                    ArrayList<String> primaryParams = new ArrayList<String>();
                    primaryParams.add(linkedKeyid1);
                    if (linkedcolumnid2.length() > 0) {
                        primaryParams.add(linkedKeyid2);
                    }
                    if (linkedcolumnid3.length() > 0) {
                        primaryParams.add(linkedKeyid3);
                    }
                    this.database.createPreparedResultSet("primary", linkedPrimarysql, primaryParams.toArray());
                    DataSet linkedPrimary = new DataSet(this.database.getResultSet("primary"));
                    this.database.closeResultSet("primary");
                    linkedPrimary.setString(-1, "sdcid", linkedSDC);
                    linkedPrimary.setString(-1, "_calckeyset", keySet);
                    primary.copyRow(linkedPrimary, -1, 1);
                }
            }
        }
        if (datasetFilter && !this.datasetFilterKeysRetrieved.contains(sdirelationKey)) {
            this.logger.debug("Fetching sdidata information for the referenced sample.");
            StringBuffer dsSql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            dsSql.append("SELECT ds.* FROM sdidata ds, sdirelation sdir ").append(" WHERE ").append(sdiRelation ? " sdir.fromsdcid = " + safeSQL.addVar(sdcid) + " AND sdir.fromkeyid1 = " + safeSQL.addVar(keyid1) : " sdir.tosdcid = " + safeSQL.addVar(sdcid) + " AND sdir.tokeyid1 = " + safeSQL.addVar(keyid1));
            if (keyCols == 2) {
                dsSql.append(" AND ").append(sdiRelation ? " sdir.fromkeyid2 = " + safeSQL.addVar(keyid2) : "sdir.tokeyid2 = " + safeSQL.addVar(keyid2));
            } else if (keyCols == 3) {
                dsSql.append(" AND ").append(sdiRelation ? " sdir.fromkeyid3 = " + safeSQL.addVar(keyid3) : "sdir.tokeyid3 = " + safeSQL.addVar(keyid3));
            }
            dsSql.append(" AND sdir.relationtype in ( " + safeSQL.addIn(sdiLinkDetails, ";") + " ) AND ds.sdcid = ").append(sdiRelation ? " sdir.tosdcid AND ds.keyid1 = sdir.tokeyid1 AND ds.keyid2 = coalesce( nullif( sdir.tokeyid2, '' ), '(null)' ) AND ds.keyid3 = coalesce( nullif( sdir.tokeyid3, '' ), '(null)' )" : " sdir.fromsdcid AND ds.keyid1 = sdir.fromkeyid1 AND ds.keyid2 = coalesce( nullif( sdir.fromkeyid2, '' ), '(null)' ) AND ds.keyid3 = coalesce( nullif( sdir.fromkeyid3, '' ), '(null)' )").append(this.includeCancelledDatasets ? " " : " AND ds.s_datasetstatus <> 'Cancelled' ").append(" ORDER BY ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3");
            this.database.createPreparedResultSet(dsSql.toString(), safeSQL.getValues());
            DataSet linkdataset = new DataSet();
            linkdataset.setResultSet(this.database.getResultSet());
            if (this.isFormulationCalc || this.isExtraCalc) {
                this.mergeMultiDataSets(linkdataset, sdidata);
            }
            linkdataset.setString(-1, "_calckeyset", keySet);
            this.datasetFilterKeysRetrieved.add(sdirelationKey);
            this.logger.debug("linkdataset: " + linkdataset.toString());
            this.baseSDIDataEntryAction.appendLinkedDataSet(sdidata, linkdataset);
        }
        return multiSDIs;
    }

    private boolean crossCalcSDIDataRelation(DataSet sdidata, DataSet sdidataitems, DataSet sdidataitemlimits, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, BigDecimal dataset, boolean datasetFilter, HashMap<String, Object> difilter, String sdiLinkDetails, DataSet sdiDataRelation) throws SapphireException {
        boolean multiSDIs = false;
        if (sdiLinkDetails.indexOf(";") > -1) {
            multiSDIs = true;
        }
        String sdidatarelationKey = "sdidatarelation:" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + paramlistid + ";" + paramlistversionid + ";" + variantid + ";" + dataset + ";" + sdiLinkDetails;
        String keySet = null;
        boolean usedSDIWorkItemRelation = false;
        String workItemId = "";
        String workItemInstance = "";
        if (this.keySetCache.containsKey(sdidatarelationKey)) {
            keySet = this.keySetCache.get(sdidatarelationKey);
            difilter.put("_calckeyset", keySet);
            this.logger.debug("Cross Calc Filter: Skipped sdidatarelation retrieval.");
        } else {
            int findRow = -1;
            if (sdiDataRelation != null && sdiDataRelation.getRowCount() > 0) {
                HashMap<String, Object> findMap = new HashMap<String, Object>();
                findMap.put("sdcid", sdcid);
                findMap.put("keyid1", keyid1);
                findMap.put("keyid2", keyid2);
                findMap.put("keyid3", keyid3);
                findMap.put("paramlistid", paramlistid);
                findMap.put("paramlistversionid", paramlistversionid);
                findMap.put("variantid", variantid);
                findMap.put("dataset", dataset);
                findMap.put("relationtype", sdiLinkDetails);
                findRow = sdiDataRelation.findRow(findMap);
            }
            DataSet linkdataitems = new DataSet();
            DataSet linkdataitemlimits = new DataSet();
            if (findRow > -1) {
                String selectdataitems = "";
                String selectdataitemlimits = "";
                if (this.includeCancelledDatasets) {
                    selectdataitems = "SELECT * FROM sdidataitem WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? ORDER BY sdcid, keyid1, keyid2, keyid3";
                    selectdataitemlimits = "SELECT * FROM sdidataitemlimits WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? ORDER BY sdcid, keyid1, keyid2, keyid3";
                } else {
                    selectdataitems = "SELECT sdidataitem.* FROM sdidata, sdidataitem WHERE sdidata.sdcid = ? AND sdidata.keyid1 = ? AND sdidata.keyid2 = ? AND sdidata.keyid3 = ? AND " + BaseSDIDataEntryAction.getSDICancelWhere("sdidataitem") + "ORDER BY sdidataitem.sdcid, sdidataitem.keyid1, sdidataitem.keyid2, sdidataitem.keyid3";
                    selectdataitemlimits = "SELECT sdidataitemlimits.* FROM sdidata, sdidataitemlimits WHERE sdidata.sdcid = ? AND sdidata.keyid1 = ? AND sdidata.keyid2 = ? AND sdidata.keyid3 = ? AND " + BaseSDIDataEntryAction.getSDICancelWhere("sdidataitemlimits") + "ORDER BY sdidataitemlimits.sdcid, sdidataitemlimits.keyid1, sdidataitemlimits.keyid2, sdidataitemlimits.keyid3";
                }
                this.logger.debug("Looking for sdidatarelation using " + selectdataitems);
                this.database.createPreparedResultSet(selectdataitems, new Object[]{sdiDataRelation.getString(findRow, "tosdcid"), sdiDataRelation.getString(findRow, "tokeyid1"), sdiDataRelation.getString(findRow, "tokeyid2", "(null)"), sdiDataRelation.getString(findRow, "tokeyid3", "(null)")});
                linkdataitems.setResultSet(this.database.getResultSet());
                this.database.createPreparedResultSet(selectdataitemlimits, new Object[]{sdiDataRelation.getString(findRow, "tosdcid"), sdiDataRelation.getString(findRow, "tokeyid1"), sdiDataRelation.getString(findRow, "tokeyid2", "(null)"), sdiDataRelation.getString(findRow, "tokeyid3", "(null)")});
                linkdataitemlimits.setResultSet(this.database.getResultSet());
            } else {
                SafeSQL safeSQL = new SafeSQL();
                String selectdataitems = "";
                String selectdataitemlimits = "";
                if (this.includeCancelledDatasets) {
                    selectdataitems = "SELECT di.* FROM sdidataitem di, sdidatarelation dr ";
                    selectdataitemlimits = "SELECT dil.* FROM sdidataitemlimits dil, sdidatarelation dr ";
                } else {
                    selectdataitems = "SELECT di.* FROM sdidata ds, sdidataitem di, sdidatarelation dr ";
                    selectdataitemlimits = "SELECT dil.* FROM sdidata ds, sdidataitemlimits dil, sdidatarelation dr ";
                }
                String drSelectBit = "WHERE dr.sdcid=" + safeSQL.addVar(sdcid) + " AND dr.keyid1=" + safeSQL.addVar(keyid1) + " AND dr.keyid2=" + safeSQL.addVar(keyid2) + " AND dr.keyid3=" + safeSQL.addVar(keyid3) + " AND dr.paramlistid=" + safeSQL.addVar(paramlistid) + " AND dr.paramlistversionid=" + safeSQL.addVar(paramlistversionid) + " AND dr.variantid=" + safeSQL.addVar(variantid) + " AND dr.dataset=" + safeSQL.addVar(dataset) + " AND dr.relationtype in ( " + safeSQL.addIn(sdiLinkDetails, ";") + " ) ";
                selectdataitems = selectdataitems + drSelectBit;
                selectdataitemlimits = selectdataitemlimits + drSelectBit;
                if (this.includeCancelledDatasets) {
                    selectdataitems = selectdataitems + "AND di.sdcid=dr.tosdcid AND di.keyid1=dr.tokeyid1 AND di.keyid2 = coalesce( nullif( dr.tokeyid2, '' ), '(null)' ) AND di.keyid3 = coalesce( nullif( dr.tokeyid3, '' ), '(null)' ) ";
                    selectdataitemlimits = selectdataitemlimits + "AND dil.sdcid=dr.tosdcid AND dil.keyid1=dr.tokeyid1 AND dil.keyid2 = coalesce( nullif( dr.tokeyid2, '' ), '(null)' ) AND dil.keyid3 = coalesce( nullif( dr.tokeyid3, '' ), '(null)' ) ";
                } else {
                    selectdataitems = selectdataitems + "AND ds.sdcid=dr.tosdcid AND ds.keyid1=dr.tokeyid1 AND ds.keyid2 = coalesce( nullif( dr.tokeyid2, '' ), '(null)' ) AND ds.keyid3 = coalesce( nullif( dr.tokeyid3, '' ), '(null)' ) AND ds.s_datasetstatus <> 'Cancelled' AND di.sdcid = ds.sdcid AND di.keyid1 = ds.keyid1 AND di.keyid2 = ds.keyid2 AND di.keyid3 = ds.keyid3 AND di.paramlistid = ds.paramlistid AND di.paramlistversionid = ds.paramlistversionid AND di.variantid = ds.variantid AND di.dataset = ds.dataset ";
                    selectdataitemlimits = selectdataitemlimits + "AND ds.sdcid=dr.tosdcid AND ds.keyid1=dr.tokeyid1 AND ds.keyid2 = coalesce( nullif( dr.tokeyid2, '' ), '(null)' ) AND ds.keyid3 = coalesce( nullif( dr.tokeyid3, '' ), '(null)' ) AND ds.s_datasetstatus <> 'Cancelled' AND dil.sdcid = ds.sdcid AND dil.keyid1 = ds.keyid1 AND dil.keyid2 = ds.keyid2 AND dil.keyid3 = ds.keyid3 AND dil.paramlistid = ds.paramlistid AND dil.paramlistversionid = ds.paramlistversionid AND dil.variantid = ds.variantid AND dil.dataset = ds.dataset ";
                }
                selectdataitems = selectdataitems + "ORDER BY di.sdcid, di.keyid1, di.keyid2, di.keyid3";
                selectdataitemlimits = selectdataitemlimits + "ORDER BY dil.sdcid, dil.keyid1, dil.keyid2, dil.keyid3";
                this.logger.debug("Looking for sdidatarelation using " + selectdataitems + " and " + sdcid + "/" + keyid1 + "/" + keyid2 + "/" + keyid3 + "/" + paramlistid + "/" + paramlistversionid + "/" + variantid + "/" + dataset);
                this.database.createPreparedResultSet(selectdataitems, safeSQL.getValues());
                linkdataitems.setResultSet(this.database.getResultSet());
                this.database.createPreparedResultSet(selectdataitemlimits, safeSQL.getValues());
                linkdataitemlimits.setResultSet(this.database.getResultSet());
            }
            if (linkdataitems.getRowCount() == 0) {
                HashMap<String, Object> findMap = new HashMap<String, Object>();
                findMap.put("sdcid", sdcid);
                findMap.put("keyid1", keyid1);
                findMap.put("keyid2", keyid2);
                findMap.put("keyid3", keyid3);
                findMap.put("paramlistid", paramlistid);
                findMap.put("paramlistversionid", paramlistversionid);
                findMap.put("variantid", variantid);
                findMap.put("dataset", dataset);
                findRow = sdidata.findRow(findMap);
                if (findRow > -1) {
                    workItemId = sdidata.getValue(findRow, "sourceworkitemid");
                    workItemInstance = sdidata.getValue(findRow, "sourceworkiteminstance");
                    if (workItemId.length() > 0) {
                        SafeSQL safeSQL = new SafeSQL();
                        String selectdataitems = "";
                        String selectdataitemlimits = "";
                        if (this.includeCancelledDatasets) {
                            selectdataitems = "SELECT di.* FROM sdidataitem di, sdiworkitemrelation wr ";
                            selectdataitemlimits = "SELECT dil.* FROM sdidataitemlimits dil, sdiworkitemrelation wr ";
                        } else {
                            selectdataitems = "SELECT di.* FROM sdidata ds, sdidataitem di, sdiworkitemrelation wr ";
                            selectdataitemlimits = "SELECT dil.* FROM sdidata ds, sdidataitemlimits dil, sdiworkitemrelation wr ";
                        }
                        String drSelectBit = "WHERE wr.sdcid=" + safeSQL.addVar(sdcid) + " AND wr.keyid1=" + safeSQL.addVar(keyid1) + " AND wr.keyid2=" + safeSQL.addVar(keyid2) + " AND wr.keyid3=" + safeSQL.addVar(keyid3) + " AND wr.workitemid = " + safeSQL.addVar(workItemId) + " AND wr.workiteminstance = " + safeSQL.addVar(workItemInstance) + " AND wr.relationtype in ( " + safeSQL.addIn(sdiLinkDetails, ";") + " ) ";
                        selectdataitems = selectdataitems + drSelectBit;
                        selectdataitemlimits = selectdataitemlimits + drSelectBit;
                        if (this.includeCancelledDatasets) {
                            selectdataitems = selectdataitems + "AND di.sdcid = wr.tosdcid AND di.keyid1 = wr.tokeyid1 AND di.keyid2 = coalesce( nullif( wr.tokeyid2, '' ), '(null)' ) AND di.keyid3 = coalesce( nullif( wr.tokeyid3, '' ), '(null)') ";
                            selectdataitemlimits = selectdataitemlimits + "AND dil.sdcid = wr.tosdcid AND dil.keyid1 = wr.tokeyid1 AND dil.keyid2 = coalesce( nullif( wr.tokeyid2, '' ), '(null)' ) AND dil.keyid3 = coalesce( nullif( wr.tokeyid3, '' ), '(null)') ";
                        } else {
                            selectdataitems = selectdataitems + "AND ds.sdcid = wr.tosdcid AND ds.keyid1 = wr.tokeyid1 AND ds.keyid2 = coalesce( nullif( wr.tokeyid2, '' ), '(null)' ) AND ds.keyid3 = coalesce( nullif( wr.tokeyid3, '' ), '(null)') AND ds.s_datasetstatus <> 'Cancelled' AND di.sdcid = ds.sdcid AND di.keyid1 = ds.keyid1 AND di.keyid2 = ds.keyid2 AND di.keyid3 = ds.keyid3 AND di.paramlistid = ds.paramlistid AND di.paramlistversionid = ds.paramlistversionid AND di.variantid = ds.variantid AND di.dataset = ds.dataset ";
                            selectdataitemlimits = selectdataitemlimits + "AND ds.sdcid = wr.tosdcid AND ds.keyid1 = wr.tokeyid1 AND ds.keyid2 = coalesce( nullif( wr.tokeyid2, '' ), '(null)' ) AND ds.keyid3 = coalesce( nullif( wr.tokeyid3, '' ), '(null)') AND ds.s_datasetstatus <> 'Cancelled' AND dil.sdcid = ds.sdcid AND dil.keyid1 = ds.keyid1 AND dil.keyid2 = ds.keyid2 AND dil.keyid3 = ds.keyid3 AND dil.paramlistid = ds.paramlistid AND dil.paramlistversionid = ds.paramlistversionid AND dil.variantid = ds.variantid AND dil.dataset = ds.dataset ";
                        }
                        selectdataitems = selectdataitems + "ORDER BY di.sdcid, di.keyid1, di.keyid2, di.keyid3";
                        selectdataitemlimits = selectdataitemlimits + "ORDER BY dil.sdcid, dil.keyid1, dil.keyid2, dil.keyid3";
                        this.logger.debug("Looking for sdiworkitemrelation using " + selectdataitems + " and " + sdcid + "/" + keyid1 + "/" + keyid2 + "/" + keyid3 + "/" + workItemId + "/" + workItemInstance);
                        this.database.createPreparedResultSet(selectdataitems, safeSQL.getValues());
                        linkdataitems.setResultSet(this.database.getResultSet());
                        this.database.createPreparedResultSet(selectdataitemlimits, safeSQL.getValues());
                        linkdataitemlimits.setResultSet(this.database.getResultSet());
                        usedSDIWorkItemRelation = true;
                    }
                }
            }
            if (this.isFormulationCalc || this.isExtraCalc) {
                this.mergeMultiDataItems(linkdataitems, sdidataitems, sdidata, sdidataitems);
            }
            this.logger.debug("linkdataitems: " + linkdataitems);
            keySet = Integer.toString(this.calcKeySet++);
            this.keySetCache.put(sdidatarelationKey, keySet);
            linkdataitems.setString(-1, "_calckeyset", keySet);
            this.logger.debug("linkdataitems: " + linkdataitems);
            this.cleanNullCalcExcludeFlag(linkdataitems);
            this.baseSDIDataEntryAction.appendLinkedDataItems(sdidataitems, linkdataitems);
            this.baseSDIDataEntryAction.appendLinkedDataItemLimits(sdidataitemlimits, linkdataitemlimits);
            difilter.put("_calckeyset", keySet);
        }
        if (datasetFilter && !this.datasetFilterKeysRetrieved.contains(sdidatarelationKey)) {
            String dsSql;
            this.logger.debug("Fetching sdidata information for the referenced sample.");
            SafeSQL safeSQL = new SafeSQL();
            if (usedSDIWorkItemRelation) {
                dsSql = "SELECT ds.* FROM sdidata ds, sdiworkitemrelation wr WHERE wr.sdcid=" + safeSQL.addVar(sdcid) + " AND wr.keyid1=" + safeSQL.addVar(keyid1) + " AND wr.keyid2=" + safeSQL.addVar(keyid2) + " AND wr.keyid3=" + safeSQL.addVar(keyid3) + " AND wr.workitemid = " + safeSQL.addVar(workItemId) + " AND wr.workiteminstance = " + safeSQL.addVar(workItemInstance) + " AND wr.relationtype in ( " + safeSQL.addIn(sdiLinkDetails, ";") + " ) AND ds.sdcid = wr.tosdcid AND ds.keyid1 = wr.tokeyid1 AND ds.keyid2 = coalesce( nullif( wr.tokeyid2, '' ), '(null)' ) AND ds.keyid3 = coalesce( nullif( wr.tokeyid3, '' ), '(null)' ) " + (this.includeCancelledDatasets ? " " : " AND ds.s_datasetstatus <> 'Cancelled' ") + "ORDER BY ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3";
                this.database.createPreparedResultSet(dsSql, safeSQL.getValues());
            } else {
                dsSql = "SELECT ds.* FROM sdidata ds, sdidatarelation dr WHERE dr.sdcid=" + safeSQL.addVar(sdcid) + " AND dr.keyid1=" + safeSQL.addVar(keyid1) + " AND dr.keyid2=" + safeSQL.addVar(keyid2) + " AND dr.keyid3=" + safeSQL.addVar(keyid3) + " AND dr.paramlistid=" + safeSQL.addVar(paramlistid) + " AND dr.paramlistversionid=" + safeSQL.addVar(paramlistversionid) + " AND dr.variantid=" + safeSQL.addVar(variantid) + " AND dr.dataset=" + safeSQL.addVar(dataset) + " AND dr.relationtype in ( " + safeSQL.addIn(sdiLinkDetails, ";") + " ) AND ds.sdcid = dr.tosdcid AND ds.keyid1 = dr.tokeyid1 AND ds.keyid2 = coalesce( nullif( dr.tokeyid2, '' ), '(null)' ) AND ds.keyid3 = coalesce( nullif( dr.tokeyid3, '' ), '(null)' ) " + (this.includeCancelledDatasets ? " " : " AND ds.s_datasetstatus <> 'Cancelled' ") + "ORDER BY ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3";
                this.database.createPreparedResultSet(dsSql, safeSQL.getValues());
            }
            DataSet linkdataset = new DataSet();
            linkdataset.setResultSet(this.database.getResultSet());
            if (this.isFormulationCalc || this.isExtraCalc) {
                this.mergeMultiDataSets(linkdataset, sdidata);
            }
            linkdataset.setString(-1, "_calckeyset", keySet);
            this.datasetFilterKeysRetrieved.add(sdidatarelationKey);
            this.logger.debug("linkdataset: " + linkdataset.toString());
            this.baseSDIDataEntryAction.appendLinkedDataSet(sdidata, linkdataset);
        }
        return multiSDIs;
    }

    private void cleanNullCalcExcludeFlag(DataSet linkdataitems) {
        for (int i = 0; i < linkdataitems.size(); ++i) {
            if (!linkdataitems.isNull(i, "calcexcludeflag")) continue;
            linkdataitems.setString(i, "calcexcludeflag", "N");
        }
    }

    private void removeKeySets(DataSet sdidata, DataSet sdidataitems, Set<String> keys) {
        for (String key : keys) {
            int i;
            if (key == null || key.length() <= 0) continue;
            this.keySetCache.remove(key);
            for (i = 0; i < sdidata.size(); ++i) {
                if (!key.equals(sdidata.getValue(i, "_calckeyset"))) continue;
                sdidata.setString(i, "_calckeyset", null);
            }
            for (i = 0; i < sdidataitems.size(); ++i) {
                if (!key.equals(sdidataitems.getValue(i, "_calckeyset"))) continue;
                sdidataitems.setString(i, "_calckeyset", null);
            }
            Iterator<Map.Entry<String, String>> it = this.keySetCache.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> e = it.next();
                if (!key.equals(e.getValue())) continue;
                it.remove();
            }
        }
    }

    private void mergeMultiDataSets(DataSet originalDatasets, DataSet fromDataSets) {
        this.mergeMultiDataItems(originalDatasets, fromDataSets, null, null);
    }

    private void mergeMultiDataItems(DataSet linkdataitems, DataSet redoSdidataitem, DataSet sdidata, DataSet sdidataitems) {
        linkdataitems.sort("sdcid,keyid1,keyid2,keyid3)");
        HashSet<String> removeKeys = new HashSet<String>();
        String lastsdcid = "";
        String lastkeyid1 = "";
        String lastkeyid2 = "";
        String lastkeyid3 = "";
        HashMap<String, String> filter = new HashMap<String, String>();
        boolean keep = true;
        for (int i = linkdataitems.size() - 1; i >= 0; --i) {
            if (!(linkdataitems.getString(i, "sdcid").equals(lastsdcid) && linkdataitems.getString(i, "keyid1").equals(lastkeyid1) && linkdataitems.getString(i, "keyid2").equals(lastkeyid2) && linkdataitems.getString(i, "keyid3").equals(lastkeyid3))) {
                lastsdcid = linkdataitems.getString(i, "sdcid");
                lastkeyid1 = linkdataitems.getString(i, "keyid1");
                lastkeyid2 = linkdataitems.getString(i, "keyid2");
                lastkeyid3 = linkdataitems.getString(i, "keyid3");
                filter.put("sdcid", lastsdcid);
                filter.put("keyid1", lastkeyid1);
                filter.put("keyid2", lastkeyid2);
                filter.put("keyid3", lastkeyid3);
                DataSet matching = redoSdidataitem.getFilteredDataSet(filter);
                if (matching.size() == 0) {
                    keep = true;
                } else {
                    keep = false;
                    linkdataitems.addAll(matching);
                    removeKeys.add(matching.getString(0, "_calckeyset"));
                }
            }
            if (keep) continue;
            linkdataitems.deleteRow(i);
        }
        if (sdidataitems != null && removeKeys.size() > 0) {
            this.removeKeySets(sdidata, sdidataitems, removeKeys);
        }
    }

    private void addPriorDataSingle(DataSet localData, DataSet redoData, String sdcid, String keyid1, String keyid2, String keyid3) {
        HashMap<String, String> f = new HashMap<String, String>();
        f.put("sdcid", sdcid);
        f.put("keyid1", keyid1);
        f.put("keyid2", keyid2);
        f.put("keyid3", keyid3);
        DataSet prior = redoData.getFilteredDataSet(f);
        if (prior.size() > 0) {
            localData.copyRow(prior, -1, 1);
        }
    }

    private void filterDataSet(DataSet ds, DataSet sdidata, String datasetFilter, boolean calcReportRequired, CalcReport.Token calcReportToken, HashMap filter, String paramid) throws SapphireException {
        block0: for (int i = ds.size() - 1; i >= 0; --i) {
            String logicalOperator = datasetFilter.contains("&&") ? "&&" : "";
            String[] logicalParts = null;
            logicalParts = logicalOperator.length() > 0 ? StringUtil.split(datasetFilter, logicalOperator) : new String[]{datasetFilter};
            for (int p = 0; p < logicalParts.length; ++p) {
                boolean include;
                String[] parts = null;
                boolean contains = false;
                if (logicalParts[p].indexOf("!=") >= 0) {
                    parts = StringUtil.split(logicalParts[p], "!=");
                    contains = false;
                } else if (logicalParts[p].indexOf("<>") >= 0) {
                    parts = StringUtil.split(logicalParts[p], "<>");
                    contains = false;
                } else if (logicalParts[p].indexOf("=") >= 0) {
                    parts = StringUtil.split(logicalParts[p], "=");
                    contains = true;
                }
                if (parts == null || parts.length != 2) {
                    if (calcReportRequired) {
                        calcReportToken.addDataItems(ds, "");
                    }
                    throw new SapphireException("CALC_PARAM_ERROR: Unrecognized dataset filter (" + logicalParts[p] + ") in " + paramid + " for " + filter.get("keyid1"));
                }
                String columnid = parts[0].trim();
                String matchValue = parts[1].trim();
                String workitemInstance = null;
                boolean isWorkitemSpecial = columnid.equalsIgnoreCase("workitem");
                if (matchValue.equals("#") || matchValue.equals("%") || isWorkitemSpecial) {
                    int findRow = sdidata.findRow(filter);
                    if (matchValue.equals("#") || matchValue.equals("%")) {
                        matchValue = sdidata.getValue(findRow, isWorkitemSpecial ? "sourceworkitemid" : columnid);
                    }
                    workitemInstance = sdidata.getValue(findRow, "sourceworkiteminstance");
                }
                HashMap<String, Object> filterMap = new HashMap<String, Object>();
                filterMap.put("sdcid", ds.getString(i, "sdcid"));
                filterMap.put("keyid1", ds.getString(i, "keyid1"));
                filterMap.put("keyid2", ds.getString(i, "keyid2"));
                filterMap.put("keyid3", ds.getString(i, "keyid3"));
                filterMap.put("paramlistid", ds.getString(i, "paramlistid"));
                filterMap.put("paramlistversionid", ds.getString(i, "paramlistversionid"));
                filterMap.put("variantid", ds.getString(i, "variantid"));
                filterMap.put("dataset", ds.getBigDecimal(i, "dataset"));
                int findRow = sdidata.findRow(filterMap);
                if (findRow < 0) continue;
                String value = sdidata.getValue(findRow, isWorkitemSpecial ? "sourceworkitemid" : columnid);
                String thisWorkitemInstance = sdidata.getValue(findRow, "sourceworkiteminstance");
                if (isWorkitemSpecial) {
                    include = contains && value.equals(matchValue) && thisWorkitemInstance.equals(workitemInstance) || !contains && !value.equals(matchValue) && !thisWorkitemInstance.equals(workitemInstance);
                } else {
                    boolean bl = include = contains && value.equals(matchValue) || !contains && !value.equals(matchValue);
                }
                if (include || !logicalOperator.equals("") && !logicalOperator.equals("&&")) continue;
                ds.deleteRow(i);
                continue block0;
            }
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void resolvePrimaryColumnOrAttribute(String token, HashMap<String, Object> expressionParams, CalcReport.Token calcReportToken, String valueColumn, HashMap<String, Object> diFilter, DataSet primary) throws SapphireException {
        String sdcId = (String)diFilter.get("sdcid");
        String keyId1 = (String)diFilter.get("keyid1");
        String keyId2 = (String)diFilter.get("keyid2");
        String keyId3 = (String)diFilter.get("keyid3");
        if (keyId1 == null || keyId1.length() == 0) {
            String keySet = (String)diFilter.get("_calckeyset");
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("_calckeyset", keySet);
            DataSet primaries = primary.getFilteredDataSet(filter);
            if (primaries.size() <= 0) return;
            String linkedSdcid = primaries.getString(0, "sdcid");
            String keyColId1 = this.sdcProcessor.getProperty(linkedSdcid, "keycolid1");
            String keyColId2 = this.sdcProcessor.getProperty(linkedSdcid, "keycolid2");
            String keyColId3 = this.sdcProcessor.getProperty(linkedSdcid, "keycolid3");
            DataSet columnsDS = this.sdcColumnDatas.get(linkedSdcid);
            if (columnsDS == null) {
                columnsDS = this.sdcProcessor.getColumnData(linkedSdcid);
                this.sdcColumnDatas.put(linkedSdcid, columnsDS);
            }
            if (columnsDS.findRow("columnid", valueColumn.toLowerCase()) >= 0) {
                if (primaries.size() == 1) {
                    DataSet ds = new DataSet();
                    ds.copyRow(primaries, 0, 1);
                    expressionParams.put(token, ds.getObject(0, valueColumn));
                    calcReportToken.addSDIRowColumn(ds, valueColumn, linkedSdcid, ds.getValue(0, keyColId1), ds.getValue(0, keyColId2), ds.getValue(0, keyColId3));
                    return;
                } else {
                    BigDecimal[] valueitems = new BigDecimal[primaries.size()];
                    for (int i = 0; i < primaries.size(); ++i) {
                        linkedSdcid = primaries.getString(i, "sdcid");
                        keyColId1 = this.sdcProcessor.getProperty(linkedSdcid, "keycolid1");
                        keyColId2 = this.sdcProcessor.getProperty(linkedSdcid, "keycolid2");
                        keyColId3 = this.sdcProcessor.getProperty(linkedSdcid, "keycolid3");
                        DataSet ds = new DataSet();
                        ds.copyRow(primaries, i, 1);
                        valueitems[i] = ds.getBigDecimal(0, valueColumn);
                        calcReportToken.addSDIRowColumn(ds, valueColumn, linkedSdcid, ds.getValue(0, keyColId1), ds.getValue(0, keyColId2), ds.getValue(0, keyColId3));
                    }
                    expressionParams.put(token, valueitems);
                }
                return;
            } else if (primaries.size() == 1) {
                DataSet ds = new DataSet();
                ds.copyRow(primaries, 0, 1);
                DataSet currentAttribute = this.resolveAttribute(valueColumn, linkedSdcid, ds.getValue(0, keyColId1), ds.getValue(0, keyColId2), ds.getValue(0, keyColId3), keyColId2, keyColId3);
                String dataType = currentAttribute.getValue(0, "datatype");
                String attributeColumn = "";
                Object attributeValue = null;
                if ("D".equals(dataType) || "O".equals(dataType)) {
                    attributeColumn = "datevalue";
                } else if ("S".equals(dataType) || "C".equals(dataType)) {
                    attributeColumn = "textvalue";
                } else if ("N".equals(dataType)) {
                    attributeColumn = "numericvalue";
                }
                if (currentAttribute.getRowCount() == 0) {
                    throw new SapphireException("Unable to locate attribute: " + valueColumn + ".");
                }
                if (currentAttribute.getRowCount() == 1) {
                    attributeValue = currentAttribute.getObject(0, attributeColumn);
                    String reportValue = currentAttribute.getValue(0, attributeColumn);
                    expressionParams.put(token, attributeValue);
                    calcReportToken.addSDIAttributeRow(valueColumn, reportValue, linkedSdcid, ds.getValue(0, keyColId1), ds.getValue(0, keyColId2), ds.getValue(0, keyColId3));
                    return;
                } else {
                    if (currentAttribute.getRowCount() <= 0 || !"N".equals(dataType)) throw new SapphireException("Multiple instances found for the attribute of non numeric data type: " + valueColumn + ".");
                    BigDecimal[] valueitems = new BigDecimal[currentAttribute.size()];
                    String[] reportValues = new String[currentAttribute.size()];
                    for (int i = 0; i < currentAttribute.size(); ++i) {
                        valueitems[i] = currentAttribute.getBigDecimal(i, attributeColumn);
                        reportValues[i] = currentAttribute.getValue(i, attributeColumn);
                    }
                    expressionParams.put(token, valueitems);
                    calcReportToken.addSDIAttributeRows(valueColumn, reportValues, linkedSdcid, ds.getValue(0, keyColId1), ds.getValue(0, keyColId2), ds.getValue(0, keyColId3));
                }
                return;
            } else {
                ArrayList<BigDecimal> valueitems = new ArrayList<BigDecimal>();
                for (int i = 0; i < primaries.size(); ++i) {
                    linkedSdcid = primaries.getString(i, "sdcid");
                    keyColId1 = this.sdcProcessor.getProperty(linkedSdcid, "keycolid1");
                    keyColId2 = this.sdcProcessor.getProperty(linkedSdcid, "keycolid2");
                    keyColId3 = this.sdcProcessor.getProperty(linkedSdcid, "keycolid3");
                    DataSet ds = new DataSet();
                    ds.copyRow(primaries, i, 1);
                    DataSet currentAttribute = this.resolveAttribute(valueColumn, linkedSdcid, ds.getValue(0, keyColId1), ds.getValue(0, keyColId2), ds.getValue(0, keyColId3), keyColId2, keyColId3);
                    String dataType = currentAttribute.getValue(0, "datatype");
                    String attributeColumn = "numericvalue";
                    String[] reportValues = new String[currentAttribute.size()];
                    if (currentAttribute.getRowCount() <= 0) continue;
                    if (!"N".equals(dataType)) throw new SapphireException("Multiple Primary found with non numeric attribute: " + valueColumn + ".");
                    for (int c = 0; c < currentAttribute.size(); ++c) {
                        valueitems.add(currentAttribute.getBigDecimal(c, attributeColumn));
                        reportValues[c] = currentAttribute.getValue(c, attributeColumn);
                    }
                    calcReportToken.addSDIAttributeRows(valueColumn, reportValues, linkedSdcid, ds.getValue(0, keyColId1), ds.getValue(0, keyColId2), ds.getValue(0, keyColId3));
                }
                if (valueitems.size() == 1) {
                    expressionParams.put(token, valueitems.get(0));
                    return;
                } else {
                    expressionParams.put(token, valueitems.toArray());
                }
            }
            return;
        } else {
            PropertyList sdcProp = this.sdcPropertyList.get(sdcId);
            if (sdcProp == null) {
                sdcProp = this.sdcProcessor.getPropertyList(sdcId);
                this.sdcPropertyList.put(sdcId, sdcProp);
            }
            String tableId = sdcProp.getProperty("tableid");
            String keycolId1 = sdcProp.getProperty("keycolid1");
            String keycolId2 = sdcProp.getProperty("keycolid2");
            String keycolId3 = sdcProp.getProperty("keycolid3");
            DataSet columnsDS = this.sdcColumnDatas.get(sdcId);
            if (columnsDS == null) {
                columnsDS = this.sdcProcessor.getColumnData(sdcId);
                this.sdcColumnDatas.put(sdcId, columnsDS);
            }
            HashMap<String, String> findColumn = new HashMap<String, String>();
            findColumn.put("columnid", valueColumn.toLowerCase());
            StringBuffer sql = new StringBuffer();
            if (columnsDS.findRow(findColumn) >= 0) {
                Object[] obj = new Object[]{keyId1};
                sql.append("SELECT " + valueColumn + " FROM " + tableId + " WHERE " + keycolId1 + " = ?");
                if (keycolId2.length() > 0) {
                    sql.append(" AND " + keycolId2 + " = ?");
                    obj = new Object[]{keyId1, keyId2};
                }
                if (keycolId3.length() > 0) {
                    sql.append(" AND " + keycolId3 + " = ?");
                    obj = new Object[]{keyId1, keyId2, keyId3};
                }
                DataSet ds = this.queryProcessor.getPreparedSqlDataSet(sql.toString(), obj);
                expressionParams.put(token, ds.getObject(0, valueColumn));
                calcReportToken.addSDIRowColumn(ds, valueColumn, sdcId, keyId1, keyId2, keyId3);
                return;
            } else {
                DataSet sdiAttributes;
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("sdcid", sdcId);
                filter.put("keyid1", keyId1);
                if (keycolId2.length() > 0) {
                    filter.put("keyid2", keyId2);
                }
                if (keycolId3.length() > 0) {
                    filter.put("keyid3", keyId3);
                }
                if ((sdiAttributes = this.primaryAttributes.getFilteredDataSet(filter)).getRowCount() == 0) {
                    Object[] obj = new Object[]{sdcId, keyId1};
                    if (keycolId2.length() > 0) {
                        obj = new Object[]{sdcId, keyId1, keyId2};
                    }
                    if (keycolId3.length() > 0) {
                        obj = new Object[]{sdcId, keyId1, keyId2, keyId3};
                    }
                    sql.append("SELECT * FROM sdiattribute WHERE sdcid = ? and keyid1 = ?");
                    if (keycolId2.length() > 0) {
                        sql.append(" AND  keyid2 = ?");
                    }
                    if (keycolId3.length() > 0) {
                        sql.append(" AND  keyid3 = ?");
                    }
                    sdiAttributes = this.queryProcessor.getPreparedSqlDataSet(sql.toString(), obj);
                    this.primaryAttributes.copyRow(sdiAttributes, -1, 1);
                }
                HashMap<String, String> findCurrentAttribute = new HashMap<String, String>();
                findCurrentAttribute.put("attributeid", valueColumn);
                DataSet currentAttribute = sdiAttributes.getFilteredDataSet(findCurrentAttribute);
                if (currentAttribute.getRowCount() == 0) {
                    throw new SapphireException("Unable to locate a primary column or unique attribute: " + valueColumn + ".");
                }
                String dataType = currentAttribute.getValue(0, "datatype");
                String attributeColumn = "";
                BigDecimal[] attributeValue = null;
                if ("D".equals(dataType) || "O".equals(dataType)) {
                    attributeColumn = "datevalue";
                } else if ("S".equals(dataType) || "C".equals(dataType)) {
                    attributeColumn = "textvalue";
                } else if ("N".equals(dataType)) {
                    attributeColumn = "numericvalue";
                }
                if (currentAttribute.getRowCount() == 1) {
                    attributeValue = currentAttribute.getObject(0, attributeColumn);
                    String reportValue = currentAttribute.getValue(0, attributeColumn);
                    expressionParams.put(token, attributeValue);
                    calcReportToken.addSDIAttributeRow(valueColumn, reportValue, sdcId, keyId1, keyId2, keyId3);
                    return;
                } else {
                    if (currentAttribute.getRowCount() <= 0 || !"N".equals(dataType)) throw new SapphireException("Multiple instances found for the primary attribute: " + valueColumn + ".");
                    BigDecimal[] valueitems = new BigDecimal[currentAttribute.size()];
                    String[] reportValues = new String[currentAttribute.size()];
                    for (int i = 0; i < currentAttribute.size(); ++i) {
                        valueitems[i] = currentAttribute.getBigDecimal(i, attributeColumn);
                        reportValues[i] = currentAttribute.getValue(i, attributeColumn);
                    }
                    attributeValue = valueitems;
                    expressionParams.put(token, valueitems);
                    calcReportToken.addSDIAttributeRows(valueColumn, reportValues, sdcId, keyId1, keyId2, keyId3);
                }
            }
        }
    }

    private DataSet resolveAttribute(String valueColumn, String sdcId, String keyId1, String keyId2, String keyId3, String keycolId2, String keycolId3) throws SapphireException {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("sdcid", sdcId);
        filter.put("keyid1", keyId1);
        if (keycolId2.length() > 0) {
            filter.put("keyid2", keyId2);
        }
        if (keycolId3.length() > 0) {
            filter.put("keyid3", keyId3);
        }
        DataSet sdiAttributes = this.primaryAttributes.getFilteredDataSet(filter);
        StringBuffer sql = new StringBuffer();
        if (sdiAttributes.getRowCount() == 0) {
            Object[] obj = new Object[]{sdcId, keyId1};
            if (keycolId2.length() > 0) {
                obj = new Object[]{sdcId, keyId1, keyId2};
            }
            if (keycolId3.length() > 0) {
                obj = new Object[]{sdcId, keyId1, keyId2, keyId3};
            }
            sql.append("SELECT * FROM sdiattribute WHERE sdcid = ? and keyid1 = ?");
            if (keycolId2.length() > 0) {
                sql.append(" AND  keyid2 = ?");
            }
            if (keycolId3.length() > 0) {
                sql.append(" AND  keyid3 = ?");
            }
            sdiAttributes = this.queryProcessor.getPreparedSqlDataSet(sql.toString(), obj);
            this.primaryAttributes.copyRow(sdiAttributes, -1, 1);
        }
        HashMap<String, String> findCurrentAttribute = new HashMap<String, String>();
        findCurrentAttribute.put("attributeid", valueColumn);
        DataSet currentAttribute = sdiAttributes.getFilteredDataSet(findCurrentAttribute);
        return currentAttribute;
    }

    private boolean crossCalcArrayItemZone(DataSet sdidata, DataSet sdidataitems, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, BigDecimal dataset, HashMap<String, Object> difilter, String arrayZone, String rowLabel, String columnLabel, DataSet arrayItems) throws SapphireException {
        int findRow;
        HashMap<String, Object> filterMap;
        boolean multiSDIs = true;
        difilter.remove("_arraymethodid");
        difilter.remove("_zone");
        difilter.remove("_horizontallabel");
        difilter.remove("_verticallabel");
        difilter.remove("_arrayid");
        difilter.put("sdcid", "LV_ArrayItem");
        long ms = System.currentTimeMillis();
        String msg = "Looking for the related ArrayItem dataitems using: ";
        if (this.ArrayMethodPart == null || this.ArrayMethodPart.trim().length() == 0 || "#".equals(this.ArrayMethodPart)) {
            filterMap = new HashMap<String, Object>();
            filterMap.put("sdcid", sdcid);
            filterMap.put("keyid1", keyid1);
            filterMap.put("keyid2", keyid2);
            filterMap.put("keyid3", keyid3);
            filterMap.put("paramlistid", paramlistid);
            filterMap.put("paramlistversionid", paramlistversionid);
            filterMap.put("variantid", variantid);
            filterMap.put("dataset", dataset);
            findRow = sdidata.findRow(filterMap);
            String arrayMethodId = sdidata.getString(findRow, "arraymethodid", "");
            difilter.put("_arraymethodid", arrayMethodId);
            msg = msg + " current ArrayMethod: " + arrayMethodId;
        } else if (!"*".equals(this.ArrayMethodPart)) {
            difilter.put("_arraymethodid", this.ArrayMethodPart);
            msg = msg + " ArrayMethod: " + this.ArrayMethodPart;
        }
        if (arrayZone == null || arrayZone.trim().length() == 0 || "#".equals(arrayZone)) {
            filterMap = new HashMap();
            filterMap.put("sdcid", sdcid);
            filterMap.put("keyid1", keyid1);
            findRow = sdidataitems.findRow(filterMap);
            String zone = sdidataitems.getString(findRow, "_zone", "");
            if (!"(FullArray)".equals(zone)) {
                filterMap.clear();
                filterMap.put("zone", zone);
                DataSet dsZoneArrayItems = arrayItems.getFilteredDataSet(filterMap);
                String[] arrayitemids = StringUtil.split(dsZoneArrayItems.getColumnValues("arrayitemid", ";"), ";");
                HashSet aiSet = new HashSet();
                Collections.addAll(aiSet, arrayitemids);
                for (int i = 0; i < sdidataitems.size(); ++i) {
                    String sdcID = sdidataitems.getString(i, "sdcid");
                    String keyID = sdidataitems.getString(i, "keyid1");
                    if (!"LV_ArrayItem".equalsIgnoreCase(sdcID) || !aiSet.contains(keyID)) continue;
                    sdidataitems.setString(i, "_zone", zone);
                }
                difilter.put("_zone", zone);
            }
            msg = msg + " current Zone: " + zone;
        } else if (!"*".equals(arrayZone) && !"(FullArray)".equals(arrayZone)) {
            filterMap = new HashMap();
            filterMap.put("zone", arrayZone);
            DataSet dsZoneArrayItems = arrayItems.getFilteredDataSet(filterMap);
            String[] arrayitemids = StringUtil.split(dsZoneArrayItems.getColumnValues("arrayitemid", ";"), ";");
            HashSet aiSet = new HashSet();
            Collections.addAll(aiSet, arrayitemids);
            for (int i = 0; i < sdidataitems.size(); ++i) {
                String sdcID = sdidataitems.getString(i, "sdcid");
                String keyID = sdidataitems.getString(i, "keyid1");
                if (!"LV_ArrayItem".equalsIgnoreCase(sdcID) || !aiSet.contains(keyID)) continue;
                sdidataitems.setString(i, "_zone", arrayZone);
            }
            difilter.put("_zone", arrayZone);
            msg = msg + " Zone: " + arrayZone;
        }
        if (rowLabel != null && rowLabel.length() > 0 && !"*".equals(rowLabel) && !"#".equals(rowLabel)) {
            difilter.put("_verticallabel", rowLabel);
            msg = msg + " verticallabel: " + rowLabel;
        }
        if (columnLabel != null && columnLabel.length() > 0 && !"*".equals(columnLabel) && !"#".equals(columnLabel)) {
            difilter.put("_horizontallabel", columnLabel);
            msg = msg + " horizontallabel: " + columnLabel;
        }
        filterMap = new HashMap();
        filterMap.put("sdcid", sdcid);
        filterMap.put("keyid1", keyid1);
        int findRow2 = sdidataitems.findRow(filterMap);
        String arrayid = sdidataitems.getString(findRow2, "_arrayid", "");
        difilter.put("_arrayid", arrayid);
        this.logger.debug(msg);
        Logger.logInfo("Time elapsed:" + (System.currentTimeMillis() - ms));
        return multiSDIs;
    }

    private boolean crossCalcArrayItem(DataSet sdidata, DataSet sdidataitems, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, BigDecimal dataset, HashMap<String, Object> difilter, String arrayZone, String rowLabel, String columnLabel, DataSet arrayItems) throws SapphireException {
        int findRow;
        HashMap<String, Object> filterMap;
        boolean multiSDIs = true;
        difilter.remove("_arraymethodid");
        difilter.remove("_zone");
        difilter.remove("_horizontallabel");
        difilter.remove("_verticallabel");
        difilter.remove("_arrayid");
        difilter.put("sdcid", "LV_ArrayItem");
        String msg = "Looking for the related ArrayItem dataitems using: ";
        if (this.ArrayMethodPart == null || this.ArrayMethodPart.trim().length() == 0 || "#".equals(this.ArrayMethodPart)) {
            filterMap = new HashMap<String, Object>();
            filterMap.put("sdcid", sdcid);
            filterMap.put("keyid1", keyid1);
            filterMap.put("keyid2", keyid2);
            filterMap.put("keyid3", keyid3);
            filterMap.put("paramlistid", paramlistid);
            filterMap.put("paramlistversionid", paramlistversionid);
            filterMap.put("variantid", variantid);
            filterMap.put("dataset", dataset);
            findRow = sdidata.findRow(filterMap);
            String arrayMethodId = sdidata.getString(findRow, "arraymethodid", "");
            difilter.put("_arraymethodid", arrayMethodId);
            msg = msg + " current ArrayMethod: " + arrayMethodId;
        } else if (!"*".equals(this.ArrayMethodPart)) {
            difilter.put("_arraymethodid", this.ArrayMethodPart);
            msg = msg + " ArrayMethod: " + this.ArrayMethodPart;
        }
        if (arrayZone == null || arrayZone.trim().length() == 0 || "#".equals(arrayZone)) {
            filterMap = new HashMap();
            filterMap.put("arrayitemid", keyid1);
            findRow = arrayItems.findRow(filterMap);
            String zone = arrayItems.getString(findRow, "zone", "");
            if (!"(FullArray)".equals(zone)) {
                filterMap.clear();
                filterMap.put("zone", zone);
                DataSet dsZoneArrayItems = arrayItems.getFilteredDataSet(filterMap);
                String[] arrayitemids = StringUtil.split(dsZoneArrayItems.getColumnValues("arrayitemid", ";"), ";");
                HashSet aiSet = new HashSet();
                Collections.addAll(aiSet, arrayitemids);
                for (int i = 0; i < sdidataitems.size(); ++i) {
                    String sdcID = sdidataitems.getString(i, "sdcid");
                    String keyID = sdidataitems.getString(i, "keyid1");
                    if (!sdcid.equalsIgnoreCase(sdcID) || !aiSet.contains(keyID)) continue;
                    sdidataitems.setString(i, "_zone", zone);
                }
                difilter.put("_zone", zone);
            }
            msg = msg + " current Zone: " + zone;
        } else if (!"*".equals(arrayZone) && !"(FullArray)".equals(arrayZone)) {
            filterMap = new HashMap();
            filterMap.clear();
            filterMap.put("zone", arrayZone);
            DataSet dsZoneArrayItems = arrayItems.getFilteredDataSet(filterMap);
            String[] arrayitemids = StringUtil.split(dsZoneArrayItems.getColumnValues("arrayitemid", ";"), ";");
            HashSet aiSet = new HashSet();
            Collections.addAll(aiSet, arrayitemids);
            for (int i = 0; i < sdidataitems.size(); ++i) {
                String sdcID = sdidataitems.getString(i, "sdcid");
                String keyID = sdidataitems.getString(i, "keyid1");
                if (!sdcid.equalsIgnoreCase(sdcID) || !aiSet.contains(keyID)) continue;
                sdidataitems.setString(i, "_zone", arrayZone);
            }
            difilter.put("_zone", arrayZone);
            msg = msg + " Zone: " + arrayZone;
        }
        if ("#".equals(rowLabel)) {
            filterMap = new HashMap();
            filterMap.put("sdcid", sdcid);
            filterMap.put("keyid1", keyid1);
            int findRow2 = sdidataitems.findRow(filterMap);
            String currentRowLabel = sdidataitems.getString(findRow2, "_verticallabel", "");
            difilter.put("_verticallabel", currentRowLabel);
            msg = msg + " current verticallabel: " + currentRowLabel;
        } else if (rowLabel != null && rowLabel.length() > 0 && !"*".equals(rowLabel) && !"#".equals(rowLabel)) {
            difilter.put("_verticallabel", rowLabel);
            msg = msg + " verticallabel: " + rowLabel;
        }
        if ("#".equals(columnLabel)) {
            filterMap = new HashMap();
            filterMap.put("sdcid", sdcid);
            filterMap.put("keyid1", keyid1);
            int findRow3 = sdidataitems.findRow(filterMap);
            String currentColLabel = sdidataitems.getString(findRow3, "_horizontallabel", "");
            difilter.put("_horizontallabel", currentColLabel);
            msg = msg + " current horizontallabel: " + currentColLabel;
        } else if (columnLabel != null && columnLabel.length() > 0 && !"*".equals(columnLabel) && !"#".equals(columnLabel)) {
            difilter.put("_horizontallabel", columnLabel);
            msg = msg + " horizontallabel: " + columnLabel;
        }
        String arrayid = arrayItems.getValue(arrayItems.findRow("arrayitemid", keyid1), "arrayid");
        difilter.put("_arrayid", arrayid);
        this.logger.debug(msg);
        return multiSDIs;
    }

    private String truncateCalcRule(String calcrule) {
        String truncatedCalcRule = "";
        if (Trace.isDebugEnabled()) {
            if (calcrule != null && calcrule.length() > 50000) {
                truncatedCalcRule = calcrule.substring(0, 50000) + "...";
            }
        } else if (calcrule != null && calcrule.length() > 2000) {
            truncatedCalcRule = calcrule.substring(0, 2000) + "...";
        }
        return truncatedCalcRule.length() > 0 ? truncatedCalcRule : calcrule;
    }
}

