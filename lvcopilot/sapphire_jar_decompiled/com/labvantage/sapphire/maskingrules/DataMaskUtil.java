/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.maskingrules;

import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.maskingrules.BaseDateMasker;
import com.labvantage.sapphire.maskingrules.BaseNumberMasker;
import com.labvantage.sapphire.maskingrules.BaseTextMasker;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataMaskUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    public static final String MASKINGPOLICY = "MaskingPolicy";
    public static final String MASKINGPOLICY_TOPNODE = "Sapphire Custom";
    public static final String MASKVISIBILITY_ROLEONLY = "RO";
    public static final String MASKVISIBILITY_ROLENSDC = "RAS";
    public static final String MASKVISIBILITY_ROLEORSDC = "ROS";
    public static final String MASKVISIBILITY_SDCONLY = "SO";
    public static final String VISIBILITYRULE_ROLE = "R";
    public static final String VISIBILITYRULE_SDCSECURITY = "S";
    public static final String VISIBILITYRULE_LINKED = "L";
    public static final String COL_MASKINGLEVEL = "maskinglevel";
    private SapphireConnection sapphireConnection;
    private ConfigurationProcessor configurationProcessor;
    private ConnectionProcessor connectionProcessor;
    private TranslationProcessor translationProcessor;
    private SDCProcessor sdcProcessor;
    private SDIProcessor sdiProcessor;
    private DAMProcessor damProcessor;
    private QueryProcessor queryProcessor;
    private Logger logger;
    private final String exceptionErrorId = "Data Mask Error";
    private Map<String, DataSet> colSQLDSMap = new HashMap<String, DataSet>();
    private PropertyList policyTopNode;
    private Boolean globalMaskingEnabled = null;
    private Boolean adhocQueryCheckRequired = null;
    private Map<String, Set<String>> allSensitivePrimaryColumns;
    private Boolean userHasGlobalRole = null;

    public DataMaskUtil(PageContext pageContext) throws SapphireException {
        this(new ConnectionProcessor(pageContext).getSapphireConnection());
    }

    public DataMaskUtil(BaseAccessor baseAccessor) throws SapphireException {
        this(new ConnectionProcessor(baseAccessor.getConnectionid()).getSapphireConnection());
    }

    public DataMaskUtil(SapphireConnection sapphireConnection) throws SapphireException {
        this.sapphireConnection = sapphireConnection;
        this.logger = new Logger(sapphireConnection.getConnectionId());
        this.logger.setLoggerName("DataMaskUtil");
        this.init();
    }

    private void init() throws SapphireException {
        this.configurationProcessor = new ConfigurationProcessor(this.sapphireConnection.getConnectionId());
        this.translationProcessor = new TranslationProcessor(this.sapphireConnection.getConnectionId());
        this.sdiProcessor = new SDIProcessor(this.sapphireConnection.getConnectionId());
        this.connectionProcessor = new ConnectionProcessor(this.sapphireConnection.getConnectionId());
        this.sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
        this.damProcessor = new DAMProcessor(this.sapphireConnection.getConnectionId());
        this.queryProcessor = new QueryProcessor(this.sapphireConnection.getConnectionId());
        this.getPolicyTopNode();
        this.isGlobalMaskingEnabled();
        this.isAdhocQueryCheckRequired();
        this.isUserHasGlobalRole();
    }

    public void maskPrimaryDataSet(DataSet ds, SDIRequest sdiRequest) throws SapphireException {
        this.maskPrimaryDataSet(ds, sdiRequest.getSDCid(), sdiRequest.getRequest("primary"), sdiRequest.isShowHiddenRecords());
    }

    public void maskPrimaryDataSet(DataSet ds, String primarySDCId, String primaryRequestItem, boolean showHiddenRecords) throws SapphireException {
        String[] requestCols = new String[]{};
        if (primaryRequestItem.trim().equalsIgnoreCase("primary") || primaryRequestItem.indexOf("[") < 0) {
            PropertyList sdcProps = this.sdcProcessor.getPropertyList(primarySDCId);
            PropertyListCollection sdcCols = sdcProps.getCollection("columns");
            requestCols = new String[sdcCols.size()];
            for (int i = 0; i < sdcCols.size(); ++i) {
                requestCols[i] = ((PropertyList)sdcCols.get(i)).getProperty("columnid");
            }
        } else {
            requestCols = RequestParser.parseColItem(primaryRequestItem);
        }
        this.maskDataSet(ds, "primary", primarySDCId, requestCols, showHiddenRecords);
    }

    public void maskSDIAliasDataSet(DataSet ds, boolean showHiddenRecords) throws SapphireException {
        String[] requestCols = new String[]{"aliasid"};
        this.maskSDIDetailDataSet("sdialias", ds, requestCols, showHiddenRecords);
    }

    public void maskSDIDataItemDataSet(DataSet ds, String dataitemRequestItem, boolean showHiddenRecords) throws SapphireException {
        String[] requestCols = RequestParser.parseColItem(dataitemRequestItem);
        this.maskSDIDetailDataSet("dataitem", ds, requestCols, showHiddenRecords);
    }

    private void maskSDIDetailDataSet(String datasetName, DataSet ds, String[] requestCols, boolean showHiddenRecords) throws SapphireException {
        ArrayList<DataSet> sdcGroupedDSList = ds.getGroupedDataSets("sdcid");
        for (int i = 0; i < sdcGroupedDSList.size(); ++i) {
            DataSet sdcGroupedDS = sdcGroupedDSList.get(i);
            String sdcId = sdcGroupedDS.getString(0, "sdcid", "");
            if (sdcId.length() <= 0) continue;
            PropertyList sdcProps = this.sdcProcessor.getPropertyList(sdcId);
            String keyColId1 = sdcProps.getProperty("keycolid1");
            String keyColId2 = sdcProps.getProperty("keycolid2");
            String keyColId3 = sdcProps.getProperty("keycolid3");
            int sdcKeyCols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
            ArrayList<DataSet> keyIdGroupedDSList = sdcGroupedDS.getGroupedDataSets("keyid1,keyid2,keyid3");
            for (int j = 0; j < keyIdGroupedDSList.size(); ++j) {
                DataSet keyIdGroupedDS = keyIdGroupedDSList.get(j);
                String keyId1 = keyIdGroupedDS.getString(0, "keyid1", "");
                String keyId2 = keyIdGroupedDS.getString(0, "keyid2", "");
                String keyId3 = keyIdGroupedDS.getString(0, "keyid3", "");
                if (!keyIdGroupedDS.isValidColumn(keyColId1)) {
                    keyIdGroupedDS.setString(-1, keyColId1, keyId1);
                }
                if (sdcKeyCols > 1 && !keyIdGroupedDS.isValidColumn(keyColId2)) {
                    keyIdGroupedDS.setString(-1, keyColId2, keyId2);
                }
                if (sdcKeyCols > 2 && !keyIdGroupedDS.isValidColumn(keyColId3)) {
                    keyIdGroupedDS.setString(-1, keyColId3, keyId3);
                }
                this.maskDataSet(keyIdGroupedDS, datasetName, sdcId, requestCols, showHiddenRecords);
            }
        }
    }

    public void maskDetailDataSet() {
    }

    private void maskDataSet(DataSet ds, String datasetName, String sdcId, String[] requestCols, boolean showHiddenRecords) throws SapphireException {
        this.logger.info("maskDataSet triggered for " + sdcId + ", " + datasetName + ", " + StringUtil.arrayToString(requestCols, ";"));
        if (ds == null || ds.getRowCount() == 0) {
            return;
        }
        String roleListStr = this.sapphireConnection.getRoleList();
        List<String> userRoleList = Arrays.asList(StringUtil.split(roleListStr, ";"));
        PropertyList policyGlobalProps = this.policyTopNode.getPropertyListNotNull("globalproperties");
        boolean isGlobalMaskingEnabled = "Y".equals(StringUtil.getYN(policyGlobalProps.getProperty("enablemasking", "N"), "N"));
        if (!isGlobalMaskingEnabled) {
            this.logger.debug("Global Masking disabled. Exiting Masking logic.");
            return;
        }
        String primarySDCId = sdcId;
        PropertyList sdcProps = this.sdcProcessor.getPropertyList(primarySDCId);
        boolean isSDCMaskable = "Y".equals(sdcProps.getProperty("maskableflag", "N"));
        if (!isSDCMaskable) {
            this.logger.debug("Masking disabled for " + sdcId + ". Exiting Masking logic.");
            return;
        }
        PropertyListCollection sdcLinks = sdcProps.getCollectionNotNull("links");
        String primaryTableId = sdcProps.getProperty("tableid");
        String globalVisibilityRule = policyGlobalProps.getProperty("visibilityrule");
        if ((MASKVISIBILITY_ROLEONLY.equalsIgnoreCase(globalVisibilityRule) || MASKVISIBILITY_ROLEORSDC.equalsIgnoreCase(globalVisibilityRule)) && this.userHasGlobalRole.booleanValue()) {
            this.logger.debug("User has Global Role. No masking requried.");
            return;
        }
        DataSet sdiVisibilityInfo = new DataSet();
        int primarySDCKeyCols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        String keyColId1 = sdcProps.getProperty("keycolid1");
        String keyColId2 = sdcProps.getProperty("keycolid2");
        String keyColId3 = sdcProps.getProperty("keycolid3");
        String keyId1List = ds.getColumnValues(keyColId1, ";");
        String keyId2List = ds.getColumnValues(keyColId2, ";");
        String keyId3List = ds.getColumnValues(keyColId3, ";");
        SDIList sdiList = new SDIList();
        sdiList.setAllowDups(false);
        sdiList.setSdcid(primarySDCId);
        sdiList.addSDIList(keyId1List, primarySDCKeyCols > 1 ? keyId2List : "", primarySDCKeyCols > 2 ? keyId3List : "");
        sdiVisibilityInfo = sdiList.toDataSet();
        sdiVisibilityInfo.addColumn("_maskinglevel", 0);
        sdiVisibilityInfo.addColumnValues("link0_keyid1", 0, sdiVisibilityInfo.getColumnValues("keyid1", ";"), ";");
        sdiVisibilityInfo.addColumnValues("link0_keyid2", 0, sdiVisibilityInfo.getColumnValues("keyid2", ";"), ";");
        sdiVisibilityInfo.addColumnValues("link0_keyid3", 0, sdiVisibilityInfo.getColumnValues("keyid3", ";"), ";");
        sdiVisibilityInfo.setString(-1, "link0_sdcid", primarySDCId);
        sdiVisibilityInfo.setString(-1, "_viewableflag", "N");
        this.populateVisibilityInfo(primarySDCId, sdiVisibilityInfo, showHiddenRecords, globalVisibilityRule, 0);
        HashMap<String, String> sdcRequiringMasking = new HashMap<String, String>();
        sdcRequiringMasking.put(primaryTableId, primarySDCId);
        HashMap<String, PropertyList> maskingLevelPolicyCache = new HashMap<String, PropertyList>();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            PropertyList policyNodeForRow;
            if (!this.isRowMaskingRequired(ds, i, globalVisibilityRule, sdiVisibilityInfo, sdcProps)) continue;
            String maskingLevelForRow = this.getMaskingLevelForRow(ds, i, sdiVisibilityInfo, sdcProps);
            if (maskingLevelForRow.length() == 0) {
                policyNodeForRow = this.policyTopNode;
            } else {
                PropertyList policyNodeForLevel = null;
                if (maskingLevelPolicyCache.containsKey(maskingLevelForRow)) {
                    policyNodeForLevel = (PropertyList)maskingLevelPolicyCache.get(maskingLevelForRow);
                } else {
                    this.logger.info("Finding Policy Node for Masking Level: " + maskingLevelForRow);
                    policyNodeForLevel = this.configurationProcessor.findPolicy(MASKINGPOLICY, COL_MASKINGLEVEL, maskingLevelForRow);
                    maskingLevelPolicyCache.put(maskingLevelForRow, policyNodeForLevel);
                }
                policyNodeForRow = policyNodeForLevel == null || policyNodeForLevel.size() == 0 ? this.policyTopNode : policyNodeForLevel;
            }
            block13: for (String columnId : requestCols) {
                if (RequestParser.isSelect(columnId)) {
                    String nestedSqlAlias = RequestParser.parseAlias(columnId);
                    if (nestedSqlAlias.length() <= 0) continue;
                    this.maskValue(ds, datasetName, i, nestedSqlAlias, primarySDCId, nestedSqlAlias, true, policyNodeForRow);
                    continue;
                }
                if (RequestParser.isColidWithAlias(columnId)) {
                    if (!"primary".equals(datasetName)) continue;
                    String colIdWithoutAlias = RequestParser.parseColumn(columnId);
                    String colAliasId = RequestParser.parseAlias(columnId);
                    this.maskValue(ds, datasetName, i, colAliasId, primarySDCId, colIdWithoutAlias, false, policyNodeForRow);
                    continue;
                }
                if (columnId.indexOf(".") > -1) {
                    if (!"primary".equals(datasetName)) continue;
                    String colFKLinkColId = StringUtil.split(columnId, ".")[0].trim();
                    String colColId = StringUtil.split(columnId, ".")[1].trim();
                    if (primaryTableId.equalsIgnoreCase(colFKLinkColId)) {
                        this.maskValue(ds, datasetName, i, colColId, primarySDCId, colColId, false, policyNodeForRow);
                        continue;
                    }
                    if ("trackitem".equalsIgnoreCase(colFKLinkColId)) {
                        sdcRequiringMasking.put(colFKLinkColId, "TrackItemSDC");
                    } else if (!"sdialias".equalsIgnoreCase(colFKLinkColId)) {
                        PropertyList policyColProps;
                        PropertyList policySDCProps = DataMaskUtil.getPolicySDCProps(primarySDCId, policyNodeForRow);
                        if (policySDCProps != null && (policyColProps = DataMaskUtil.getPolicyColProps(columnId, false, policySDCProps)) != null) {
                            this.maskValue(ds, datasetName, i, columnId, primarySDCId, columnId, false, policyNodeForRow);
                            continue;
                        }
                        for (int j = 0; j < sdcLinks.size(); ++j) {
                            String fkcolid;
                            PropertyList linkProps = sdcLinks.getPropertyList(j);
                            if (linkProps == null || !linkProps.getProperty("linktype").equals("F")) continue;
                            String linkcolId = linkProps.getProperty("sdccolumnid");
                            String linkSDCid = linkProps.getProperty("linksdcid");
                            if (!columnId.startsWith(linkcolId + ".") || (fkcolid = RequestParser.parseColumn(columnId).substring(linkcolId.length() + 1)).length() <= 0 || fkcolid.startsWith("_")) continue;
                            sdcRequiringMasking.put(colFKLinkColId, linkSDCid);
                        }
                    }
                    if (!sdcRequiringMasking.containsKey(colFKLinkColId)) continue;
                    String fkcolid = RequestParser.parseColumn(columnId).substring(colFKLinkColId.length() + 1);
                    String columnAlias = RequestParser.parseAlias(columnId);
                    if (columnAlias.length() > 0) {
                        this.maskValue(ds, datasetName, i, columnAlias, (String)sdcRequiringMasking.get(colFKLinkColId), fkcolid, false, policyNodeForRow);
                        continue;
                    }
                    this.maskValue(ds, datasetName, i, columnId, (String)sdcRequiringMasking.get(colFKLinkColId), fkcolid, false, policyNodeForRow);
                    continue;
                }
                switch (columnId.toLowerCase()) {
                    case "keycolid1": {
                        this.maskValue(ds, datasetName, i, sdcProps.getProperty("keycolid1"), primarySDCId, sdcProps.getProperty("keycolid1"), false, policyNodeForRow);
                        continue block13;
                    }
                    case "keycolid2": {
                        this.maskValue(ds, datasetName, i, sdcProps.getProperty("keycolid2"), primarySDCId, sdcProps.getProperty("keycolid2"), false, policyNodeForRow);
                        continue block13;
                    }
                    case "keycolid3": {
                        this.maskValue(ds, datasetName, i, sdcProps.getProperty("keycolid3"), primarySDCId, sdcProps.getProperty("keycolid3"), false, policyNodeForRow);
                        continue block13;
                    }
                    case "desccol": {
                        this.maskValue(ds, datasetName, i, sdcProps.getProperty("desccol"), primarySDCId, sdcProps.getProperty("desccol"), false, policyNodeForRow);
                        continue block13;
                    }
                    default: {
                        this.maskValue(ds, datasetName, i, columnId, primarySDCId, columnId, false, policyNodeForRow);
                    }
                }
            }
        }
    }

    private void maskValue(DataSet ds, String datasetName, int dsRow, String dsColumn, String policySDCId, String policyColumnId, boolean isColumnAlias, PropertyList policyNodeForRow) throws SapphireException {
        PropertyList policySDCProps;
        if (policyNodeForRow != null && policyNodeForRow.size() > 0 && (policySDCProps = DataMaskUtil.getPolicySDCProps(policySDCId, policyNodeForRow)) != null && "Y".equals(policySDCProps.getProperty("enable", "N"))) {
            boolean colMaskingRequired = false;
            PropertyList columnProps = new PropertyList();
            PropertyList colMaskingProps = new PropertyList();
            String colDataType = "";
            if ("sdialias".equalsIgnoreCase(datasetName)) {
                PropertyList aliasProps = policySDCProps.getPropertyListNotNull("aliasprops");
                boolean enableAliasMasking = "Y".equalsIgnoreCase(aliasProps.getProperty("enablemasking", "N"));
                if (enableAliasMasking) {
                    String currentRowAliasType;
                    PropertyListCollection aliasTypes;
                    colDataType = "Text";
                    colMaskingProps = aliasProps.getPropertyListNotNull("maskingprops");
                    String maskCriteria = colMaskingProps.getProperty("maskcriteria", "All");
                    if ("All".equals(maskCriteria)) {
                        colMaskingRequired = true;
                    } else if ("Selected".equals(maskCriteria) && (aliasTypes = colMaskingProps.getCollectionNotNull("aliastypes")).find("aliastypeid", currentRowAliasType = ds.getString(dsRow, "aliastype")) != null) {
                        colMaskingRequired = true;
                        colDataType = "Text";
                    }
                }
            } else if (("primary".equalsIgnoreCase(datasetName) || "dataitem".equalsIgnoreCase(datasetName)) && (columnProps = DataMaskUtil.getPolicyColProps(policyColumnId, isColumnAlias, policySDCProps)) != null) {
                String enableColumnMaskingStr = columnProps.getProperty("enablemasking", "N");
                if (enableColumnMaskingStr.startsWith("$G")) {
                    ConnectionInfo connectionInfo = this.connectionProcessor.getConnectionInfo(this.sapphireConnection.getConnectionId());
                    HashMap<String, Object> groovyBindMap = new HashMap<String, Object>();
                    groovyBindMap.put("user", connectionInfo.getUserAttributeMap());
                    groovyBindMap.put("primary", ds.get(dsRow));
                    DataSet sqlDataSet = this.getSDCColAccessSQLDataSet(policySDCId, policyNodeForRow, ds);
                    groovyBindMap.put("sqldataset", sqlDataSet);
                    groovyBindMap.put("columnid", policyColumnId);
                    enableColumnMaskingStr = GroovyUtil.getInstance(connectionInfo).evaluateSecure(enableColumnMaskingStr, groovyBindMap);
                }
                if ("Y".equalsIgnoreCase(enableColumnMaskingStr)) {
                    colMaskingRequired = true;
                    colMaskingProps = columnProps.getPropertyListNotNull("maskingprops");
                    colDataType = colMaskingProps.getProperty("datatype", "Text");
                } else {
                    colMaskingRequired = false;
                }
            }
            if (colMaskingRequired) {
                String finalMaskedValue = "";
                try {
                    int dsColType;
                    if ("Text".equals(colDataType)) {
                        dsColType = ds.getColumnType(dsColumn);
                        String actualValue = "";
                        actualValue = dsColType != 0 && dsColType != 3 ? ds.getValue(dsRow, dsColumn, true) : ds.getString(dsRow, dsColumn, "");
                        PropertyList textDataTypeProps = colMaskingProps.getPropertyListNotNull("textdatatypeprops");
                        String textTemplate = textDataTypeProps.getProperty("template", "ALL_STAR");
                        BaseTextMasker baseTextMasker = new BaseTextMasker(this.sapphireConnection);
                        baseTextMasker.setMaskingLogic(BaseTextMasker.MASKING_LOGIC.valueOf(textTemplate));
                        finalMaskedValue = baseTextMasker.mask(actualValue, baseTextMasker.parsePolicyProperty(textDataTypeProps));
                    } else if ("Number".equals(colDataType)) {
                        dsColType = ds.getColumnType(dsColumn);
                        if (dsColType != 1) {
                            finalMaskedValue = this.translationProcessor.translate("INVALID MASK DATA TYPE");
                        } else {
                            PropertyList numberDataTypeProps = colMaskingProps.getPropertyListNotNull("numberdatatypeprops");
                            String numberTemplate = numberDataTypeProps.getProperty("template", "ALL_STAR");
                            BaseNumberMasker baseNumberMasker = new BaseNumberMasker(this.sapphireConnection);
                            baseNumberMasker.setMaskingLogic(BaseNumberMasker.MASKING_LOGIC.valueOf(numberTemplate));
                            finalMaskedValue = baseNumberMasker.mask(ds.getBigDecimal(dsRow, dsColumn), baseNumberMasker.parsePolicyProperty(numberDataTypeProps));
                        }
                    } else if ("Date".equals(colDataType)) {
                        dsColType = ds.getColumnType(dsColumn);
                        if (dsColType != 2) {
                            finalMaskedValue = this.translationProcessor.translate("INVALID MASK DATA TYPE");
                        } else {
                            PropertyList dateDataTypeProps = colMaskingProps.getPropertyListNotNull("datedatatypeprops");
                            String dateTemplate = dateDataTypeProps.getProperty("template", "ALL_STAR");
                            BaseDateMasker baseDateMasker = new BaseDateMasker(this.sapphireConnection);
                            baseDateMasker.setMaskingLogic(BaseDateMasker.MASKING_LOGIC.valueOf(dateTemplate));
                            baseDateMasker.setDateFormat(ds.getDateDisplayFormat(dsColumn));
                            finalMaskedValue = baseDateMasker.mask(ds.getCalendar(dsRow, dsColumn), baseDateMasker.parsePolicyProperty(dateDataTypeProps));
                        }
                    } else if ("Expression".equals(colDataType)) {
                        PropertyList expressionDataTypeProps = colMaskingProps.getPropertyListNotNull("expressiondatatypeprops");
                        String expressionStr = expressionDataTypeProps.getProperty("expression");
                        ConnectionInfo connectionInfo = this.connectionProcessor.getConnectionInfo(this.sapphireConnection.getConnectionId());
                        HashMap<String, Object> groovyBindMap = new HashMap<String, Object>();
                        groovyBindMap.put("user", connectionInfo.getUserAttributeMap());
                        groovyBindMap.put("value", ds.getValue(dsRow, dsColumn, true));
                        if ("primary".equals(datasetName)) {
                            groovyBindMap.put("primary", ds.get(dsRow));
                            groovyBindMap.put("columnid", policyColumnId);
                        }
                        finalMaskedValue = GroovyUtil.getInstance(connectionInfo).evaluateSecure(expressionStr, groovyBindMap);
                    } else {
                        finalMaskedValue = this.translationProcessor.translate("INVALID MASK LOGIC");
                    }
                }
                catch (Exception e) {
                    this.logger.error("MASKING ERROR", e);
                    finalMaskedValue = this.translationProcessor.translate("MASKING ERROR");
                }
                ds.setMaskedString(dsRow, dsColumn, finalMaskedValue);
            }
        }
    }

    private boolean isRowMaskingRequired(DataSet ds, int dsRow, String visibilityRule, DataSet sdiVisibilityInfo, PropertyList sdcProps) {
        boolean isMaskingRequired = true;
        if (MASKVISIBILITY_ROLEONLY.equalsIgnoreCase(visibilityRule)) {
            return this.userHasGlobalRole == false;
        }
        switch (visibilityRule) {
            case "RO": {
                isMaskingRequired = this.userHasGlobalRole == false;
                break;
            }
            case "ROS": {
                if (this.userHasGlobalRole.booleanValue()) {
                    isMaskingRequired = false;
                    break;
                }
            }
            case "RAS": {
                if (!this.userHasGlobalRole.booleanValue()) {
                    isMaskingRequired = true;
                    break;
                }
            }
            case "SO": {
                int keyColCount = Integer.parseInt(sdcProps.getProperty("keycolumns"));
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put("_viewableflag", "Y");
                findMap.put("sdcid", sdcProps.getProperty("sdcid"));
                findMap.put("keyid1", ds.getString(dsRow, sdcProps.getProperty("keycolid1")));
                if (keyColCount > 1) {
                    findMap.put("keyid2", ds.getString(dsRow, sdcProps.getProperty("keycolid2")));
                }
                if (keyColCount > 2) {
                    findMap.put("keyid3", ds.getString(dsRow, sdcProps.getProperty("keycolid3")));
                }
                isMaskingRequired = sdiVisibilityInfo.findRow(findMap) <= -1;
            }
        }
        return isMaskingRequired;
    }

    private String getMaskingLevelForRow(DataSet ds, int dsRow, DataSet sdiVisibilityInfo, PropertyList sdcProps) {
        int findRow;
        String maskingLevel = "";
        int keyColCount = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        HashMap<String, String> findMap = new HashMap<String, String>();
        findMap.put("sdcid", sdcProps.getProperty("sdcid"));
        findMap.put("keyid1", ds.getString(dsRow, sdcProps.getProperty("keycolid1")));
        if (keyColCount > 1) {
            findMap.put("keyid2", ds.getString(dsRow, sdcProps.getProperty("keycolid2")));
        }
        if (keyColCount > 2) {
            findMap.put("keyid3", ds.getString(dsRow, sdcProps.getProperty("keycolid3")));
        }
        if ((findRow = sdiVisibilityInfo.findRow(findMap)) > -1) {
            maskingLevel = sdiVisibilityInfo.getString(findRow, "_maskinglevel", "");
        }
        return maskingLevel;
    }

    private void populateVisibilityInfo(String sdcId, DataSet sdiVisibilityInfo, boolean showHiddenRecords, String globalVisibilityRule, int recursionCount) throws SapphireException {
        PropertyList sdcProps = this.sdcProcessor.getPropertyList(sdcId);
        String maskableFlag = sdcProps.getProperty("maskableflag", "N");
        if (!"Y".equals(maskableFlag)) {
            throw new SapphireException("Data Mask Error", "FAILURE", this.translationProcessor.translate("SDC is not marked as Maskable: ") + sdcId);
        }
        String maskDataVisibilityRule = sdcProps.getProperty("maskdatavisibilityrule");
        int keyColCount = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        StringBuffer keyid1List = new StringBuffer();
        StringBuffer keyid2List = new StringBuffer();
        StringBuffer keyid3List = new StringBuffer();
        for (int i = 0; i < sdiVisibilityInfo.getRowCount(); ++i) {
            if (sdiVisibilityInfo.getString(i, "link" + recursionCount + "_keyid1").length() <= 0) continue;
            keyid1List.append(";").append(sdiVisibilityInfo.getString(i, "link" + recursionCount + "_keyid1"));
            keyid2List.append(";").append(sdiVisibilityInfo.getString(i, "link" + recursionCount + "_keyid2"));
            keyid3List.append(";").append(sdiVisibilityInfo.getString(i, "link" + recursionCount + "_keyid3"));
        }
        if (keyid1List.length() > 0) {
            keyid1List.deleteCharAt(0);
            keyid2List.deleteCharAt(0);
            keyid3List.deleteCharAt(0);
            String rsetId = this.damProcessor.createRSet(sdcId, keyid1List.toString(), keyid2List.toString(), keyid3List.toString(), showHiddenRecords, 1);
            this.logger.debug("RSet created for Data Masking: " + rsetId);
            if (rsetId != null && rsetId.length() > 0) {
                SDIRequest sdiInfoRequest = new SDIRequest();
                sdiInfoRequest.setSDCid(sdcId);
                sdiInfoRequest.setRsetid(rsetId);
                sdiInfoRequest.setReturnMaskedData(false);
                sdiInfoRequest.setRequestItem("primary");
                sdiInfoRequest.setRetainRsetid(true);
                SDIData sdiSDIData = this.sdiProcessor.getSDIData(sdiInfoRequest);
                DataSet sdiPrimary = sdiSDIData.getDataset("primary");
                this.logger.debug("Clear Data Masking RSet: " + rsetId);
                this.damProcessor.clearRSet(rsetId);
                this.logger.debug("Clear Data Masking RSet Done for: " + rsetId);
                String keyColId1 = sdcProps.getProperty("keycolid1");
                String keyColId2 = sdcProps.getProperty("keycolid2");
                String keyColId3 = sdcProps.getProperty("keycolid3");
                String sdcDefMaskingLevel = sdcProps.getProperty("defaultmaskinglevel");
                HashMap<String, String> findMap = new HashMap<String, String>();
                for (int i = 0; i < sdiVisibilityInfo.getRowCount(); ++i) {
                    int findRow;
                    findMap.clear();
                    findMap.put(keyColId1, sdiVisibilityInfo.getString(i, "link" + recursionCount + "_keyid1"));
                    if (keyColCount > 1) {
                        findMap.put(keyColId2, sdiVisibilityInfo.getString(i, "link" + recursionCount + "_keyid2"));
                    }
                    if (keyColCount > 2) {
                        findMap.put(keyColId3, sdiVisibilityInfo.getString(i, "link" + recursionCount + "_keyid3"));
                    }
                    if ((findRow = sdiPrimary.findRow(findMap)) > -1) {
                        sdiVisibilityInfo.setString(i, "_maskinglevel", sdiPrimary.getString(findRow, COL_MASKINGLEVEL, sdcDefMaskingLevel));
                        continue;
                    }
                    sdiVisibilityInfo.setString(i, "_maskinglevel", sdcDefMaskingLevel);
                }
                if (MASKVISIBILITY_ROLEORSDC.equalsIgnoreCase(globalVisibilityRule) || MASKVISIBILITY_ROLENSDC.equalsIgnoreCase(globalVisibilityRule) || MASKVISIBILITY_SDCONLY.equalsIgnoreCase(globalVisibilityRule)) {
                    if (VISIBILITYRULE_ROLE.equals(maskDataVisibilityRule)) {
                        if (this.userHasGlobalRole.booleanValue()) {
                            sdiVisibilityInfo.setString(-1, "_viewableflag", "Y");
                        } else {
                            sdiVisibilityInfo.setString(-1, "_viewableflag", "N");
                        }
                    } else if (VISIBILITYRULE_SDCSECURITY.equals(maskDataVisibilityRule)) {
                        String sdcAccessControl = sdcProps.getProperty("accesscontrolledflag", "N");
                        if (VISIBILITYRULE_SDCSECURITY.equals(sdcAccessControl) || "D".equals(sdcAccessControl)) {
                            SDIList sdiList = this.damProcessor.checkSDIAccess(sdcId, keyid1List.toString(), keyid2List.toString(), keyid3List.toString(), showHiddenRecords, "ViewMaskedData");
                            DataSet accessibleSDIs = sdiList.toDataSet();
                            for (int i = 0; i < sdiVisibilityInfo.getRowCount(); ++i) {
                                findMap.clear();
                                findMap.put("sdcid", sdcId);
                                findMap.put("keyid1", sdiVisibilityInfo.getString(i, "link" + recursionCount + "_keyid1"));
                                if (keyColCount > 1) {
                                    findMap.put("keyid2", sdiVisibilityInfo.getString(i, "link" + recursionCount + "_keyid2"));
                                }
                                if (keyColCount > 2) {
                                    findMap.put("keyid3", sdiVisibilityInfo.getString(i, "link" + recursionCount + "_keyid3"));
                                }
                                if (accessibleSDIs.findRow(findMap) > -1) {
                                    sdiVisibilityInfo.setString(i, "_viewableflag", "Y");
                                    continue;
                                }
                                sdiVisibilityInfo.setString(i, "_viewableflag", "N");
                            }
                        } else {
                            sdiVisibilityInfo.setString(-1, "_viewableflag", "Y");
                        }
                    } else if (VISIBILITYRULE_LINKED.equals(maskDataVisibilityRule)) {
                        if (recursionCount > 0) {
                            throw new SapphireException("Data Mask Error", "FAILURE", this.translationProcessor.translate("Only 1 level of Linked Mode Visibility Rule is supported."));
                        }
                        String maskDataVisibilityLink = sdcProps.getProperty("maskdatavisibilitylink");
                        if (maskDataVisibilityLink.length() == 0) {
                            throw new SapphireException("Data Mask Error", "FAILURE", this.translationProcessor.translate("Visibility Link undefined for SDC: ") + sdcId);
                        }
                        PropertyListCollection sdcLinks = sdcProps.getCollectionNotNull("links");
                        PropertyList sdcLinkProps = sdcLinks.getPropertyList(maskDataVisibilityLink);
                        if (sdcLinkProps == null || sdcLinkProps.size() == 0 || !"F".equals(sdcLinkProps.getProperty("linktype"))) {
                            throw new SapphireException("Data Mask Error", "FAILURE", this.translationProcessor.translate("Invalid Visibility Link defined for SDC: ") + sdcId);
                        }
                        String linkSDCid = sdcLinkProps.getProperty("linksdcid");
                        String linkKeyColId1 = sdcLinkProps.getProperty("sdccolumnid");
                        String linkKeyColId2 = sdcLinkProps.getProperty("sdccolumnid2");
                        String linkKeyColId3 = sdcLinkProps.getProperty("sdccolumnid3");
                        sdiVisibilityInfo.addColumn("link" + (recursionCount + 1) + "_sdcid", 0);
                        sdiVisibilityInfo.addColumn("link" + (recursionCount + 1) + "_keyid1", 0);
                        sdiVisibilityInfo.addColumn("link" + (recursionCount + 1) + "_keyid2", 0);
                        sdiVisibilityInfo.addColumn("link" + (recursionCount + 1) + "_keyid3", 0);
                        for (int i = 0; i < sdiVisibilityInfo.getRowCount(); ++i) {
                            int findRow;
                            findMap.clear();
                            findMap.put(sdcProps.getProperty("keycolid1"), sdiVisibilityInfo.getString(i, "link" + recursionCount + "_keyid1"));
                            if (keyColCount > 1) {
                                findMap.put(sdcProps.getProperty("keycolid2"), sdiVisibilityInfo.getString(i, "link" + recursionCount + "_keyid2"));
                            }
                            if (keyColCount > 2) {
                                findMap.put(sdcProps.getProperty("keycolid3"), sdiVisibilityInfo.getString(i, "link" + recursionCount + "_keyid3"));
                            }
                            if ((findRow = sdiPrimary.findRow(findMap)) <= -1) continue;
                            sdiVisibilityInfo.setString(i, "link" + (recursionCount + 1) + "_keyid1", sdiPrimary.getString(findRow, linkKeyColId1, ""));
                            sdiVisibilityInfo.setString(i, "link" + (recursionCount + 1) + "_keyid2", sdiPrimary.getString(findRow, linkKeyColId2, ""));
                            sdiVisibilityInfo.setString(i, "link" + (recursionCount + 1) + "_keyid3", sdiPrimary.getString(findRow, linkKeyColId3, ""));
                        }
                        sdiVisibilityInfo.setString(-1, "link" + (recursionCount + 1) + "_sdcid", linkSDCid);
                        this.populateVisibilityInfo(linkSDCid, sdiVisibilityInfo, showHiddenRecords, globalVisibilityRule, ++recursionCount);
                    } else {
                        throw new SapphireException("Data Mask Error", "FAILURE", this.translationProcessor.translate("Mask Data Visibility Rule undefined for SDC: ") + sdcId);
                    }
                }
            }
        }
    }

    private DataSet getSDCColAccessSQLDataSet(String sdcId, PropertyList policyNodeForRow, DataSet ds) throws SapphireException {
        if (this.colSQLDSMap == null) {
            this.colSQLDSMap = new HashMap<String, DataSet>();
        }
        String maskingLevel = policyNodeForRow.getProperty(COL_MASKINGLEVEL, MASKINGPOLICY_TOPNODE);
        if (!this.colSQLDSMap.containsKey(sdcId + "#@#" + maskingLevel)) {
            PropertyList policySDCProps = DataMaskUtil.getPolicySDCProps(sdcId, policyNodeForRow);
            if (policySDCProps == null) {
                throw new SapphireException("Data Mask Error", "FAILURE", this.translationProcessor.translate("No definition found in Policy for SDC: ") + sdcId);
            }
            String sql = policySDCProps.getProperty("columnaccesssql");
            if (sql.length() == 0) {
                this.colSQLDSMap.put(sdcId + "#@#" + maskingLevel, new DataSet());
            } else {
                PropertyList sdcProps = this.sdcProcessor.getPropertyList(sdcId);
                int sdcKeyCols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
                String keyColId1 = sdcProps.getProperty("keycolid1");
                String keyColId2 = sdcProps.getProperty("keycolid2");
                String keyColId3 = sdcProps.getProperty("keycolid3");
                StringBuffer keyId1List = new StringBuffer();
                StringBuffer keyId2List = new StringBuffer();
                StringBuffer keyId3List = new StringBuffer();
                ArrayList<DataSet> keyIdGroupedList = ds.getGroupedDataSets(keyColId1 + "," + keyColId2 + "," + keyColId3);
                for (int i = 0; i < keyIdGroupedList.size(); ++i) {
                    DataSet keyIdGroupedDS = keyIdGroupedList.get(i);
                    keyId1List.append(";").append(keyIdGroupedDS.getString(0, keyColId1));
                    keyId2List.append(";").append(keyIdGroupedDS.getString(0, keyColId2, ""));
                    keyId3List.append(";").append(keyIdGroupedDS.getString(0, keyColId3, ""));
                }
                String rsetId = "";
                if (keyId1List.length() > 0 && sql.toLowerCase().indexOf("[rsetid]") > -1) {
                    keyId1List.deleteCharAt(0);
                    keyId2List.deleteCharAt(0);
                    keyId3List.deleteCharAt(0);
                    sql = StringUtil.replaceAll(sql, "[currentuser]", this.sapphireConnection.getSysuserId(), false);
                    sql = StringUtil.replaceAll(sql, "[keyid1]", StringUtil.replaceAll(keyId1List.toString(), ";", "','"), false);
                    if (sdcKeyCols > 1) {
                        sql = StringUtil.replaceAll(sql, "[keyid2]", StringUtil.replaceAll(keyId2List.toString(), ";", "','"), false);
                    }
                    if (sdcKeyCols > 2) {
                        sql = StringUtil.replaceAll(sql, "[keyid3]", StringUtil.replaceAll(keyId3List.toString(), ";", "','"), false);
                    }
                    if (sql.indexOf("[rsetid]") > -1) {
                        rsetId = this.damProcessor.createRSet(sdcId, keyId1List.toString(), keyId2List.toString(), keyId3List.toString(), true, 1);
                        sql = StringUtil.replaceAll(sql, "[rsetid]", rsetId, false);
                    }
                }
                DataSet sqlDS = this.queryProcessor.getSqlDataSet(sql);
                this.colSQLDSMap.put(sdcId + "#@#" + maskingLevel, sqlDS);
                if (rsetId.length() > 0) {
                    this.damProcessor.clearRSet(rsetId);
                }
            }
        }
        return this.colSQLDSMap.get(sdcId + "#@#" + maskingLevel);
    }

    private static PropertyList getPolicySDCProps(String sdcId, PropertyList policyNode) {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.clear();
        filterMap.put("sdcid", sdcId);
        ArrayList sdcPropsList = policyNode.getFilteredCollections("sdccollection", filterMap);
        if (sdcPropsList.size() > 0) {
            return (PropertyList)sdcPropsList.get(0);
        }
        return null;
    }

    private static PropertyList getPolicyColProps(String colId, boolean isColAlias, PropertyList policySDCProps) {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        PropertyList columnProps = null;
        if (isColAlias) {
            filterMap.put("columnaliasid", colId);
        } else {
            filterMap.put("columnid", colId);
        }
        ArrayList columnPropsList = policySDCProps.getFilteredCollections("columncollection", filterMap);
        if (columnPropsList.size() > 0) {
            columnProps = (PropertyList)columnPropsList.get(0);
        }
        return columnProps;
    }

    public static void copyDownMaskingLevel(String sdcId, DataSet primaryData, DataSet beforeEditPrimary, HashMap<String, ArrayList<PropertyList>> copyDownPolicy, SDCProcessor sdcProcessor, SDIProcessor sdiProcessor) {
        ArrayList<PropertyList> maskingLevelProps = copyDownPolicy.get(COL_MASKINGLEVEL);
        PropertyList sdcProps = sdcProcessor.getPropertyList(sdcId);
        HashMap updateMap = new HashMap();
        PropertyList maskingColumnProps = new PropertyList();
        maskingColumnProps.setProperty("fromcolumnid", COL_MASKINGLEVEL);
        maskingColumnProps.setProperty("tocolumnid", COL_MASKINGLEVEL);
        maskingColumnProps.setProperty("copyonlyifnull", "Y");
        maskingColumnProps.setProperty("copydownnull", "N");
        PropertyListCollection copyColumnsList = new PropertyListCollection();
        copyColumnsList.add(maskingColumnProps);
        for (PropertyList copyFrom : maskingLevelProps) {
            String fromSdc = copyFrom.getProperty("sdcid");
            String fkcolumnid = copyFrom.getProperty("fkcolumnid");
            String fkcolumnid2 = copyFrom.getProperty("fkcolumnid2");
            String fkcolumnid3 = copyFrom.getProperty("fkcolumnid3");
            String fromSDCMaskableFlag = sdcProcessor.getProperty(fromSdc, "maskableflag");
            if (!"Y".equals(fromSDCMaskableFlag) || fkcolumnid.length() <= 0) continue;
            SdiInfo.copyDownColumnFromFK(fromSdc, fkcolumnid, fkcolumnid2, fkcolumnid3, primaryData, beforeEditPrimary, copyColumnsList, sdcProps, updateMap, sdiProcessor, sdcProcessor);
        }
    }

    public static DataSet getMaskedDataSetOnly(DataSet ds) {
        String[] allColumns;
        HashSet<String> nonStringMaskedCol = new HashSet<String>();
        block0: for (String colId : allColumns = ds.getColumns()) {
            int colType = ds.getColumnType(colId);
            if (colType != 2 && colType != 1) continue;
            for (int i = 0; i < ds.getRowCount(); ++i) {
                if (!ds.isMasked(i, colId)) continue;
                nonStringMaskedCol.add(colId);
                continue block0;
            }
        }
        DataSet ds2 = new DataSet();
        if (null != ds.getM18n()) {
            ds2.setM18NUtil(ds.getM18n());
        }
        for (String colId : allColumns) {
            if (nonStringMaskedCol.contains(colId)) {
                ds2.addColumn(colId, 0);
                continue;
            }
            ds2.addColumn(colId, ds.getColumnType(colId));
            if (ds.getColumnType(colId) != 2) continue;
            ds2.setDateDisplayFormat(colId, ds.getDateDisplayFormat(colId));
        }
        for (int i = 0; i < ds.getRowCount(); ++i) {
            ds2.addRow();
            for (String colId : allColumns) {
                ds2.setValue(i, colId, ds.getValue(i, colId));
            }
        }
        return ds2;
    }

    public static Map<String, Set<String>> getAllSensitivePrimaryColumns(PropertyList maskingPolicy) {
        PropertyList policyGlobalProps;
        boolean isGlobalMaskingEnabled;
        HashMap<String, Set<String>> sdcSensitiveColsMap = new HashMap<String, Set<String>>();
        if (maskingPolicy != null && (isGlobalMaskingEnabled = "Y".equals(StringUtil.getYN((policyGlobalProps = maskingPolicy.getPropertyListNotNull("globalproperties")).getProperty("enablemasking", "N"), "N")))) {
            PropertyListCollection sdcCollection = maskingPolicy.getCollectionNotNull("sdccollection");
            for (int i = 0; i < sdcCollection.size(); ++i) {
                PropertyList sdcProps = sdcCollection.getPropertyList(i);
                String sdcId = sdcProps.getProperty("sdcid");
                if (sdcSensitiveColsMap.containsKey(sdcId)) continue;
                HashSet<String> sensitiveCols = new HashSet<String>();
                sdcSensitiveColsMap.put(sdcId, sensitiveCols);
                if (!"Y".equals(sdcProps.getProperty("enable", "Y"))) continue;
                PropertyListCollection colCollection = sdcProps.getCollectionNotNull("columncollection");
                for (int j = 0; j < colCollection.size(); ++j) {
                    PropertyList colProps = colCollection.getPropertyList(j);
                    String colId = colProps.getProperty("columnid");
                    if (sensitiveCols.contains(colId) || colId.length() <= 0 || colId.indexOf(".") != -1 || "N".equals(colProps.getProperty("enablemasking", "N"))) continue;
                    sensitiveCols.add(colId.toLowerCase());
                }
                PropertyList aliasProps = sdcProps.getPropertyListNotNull("aliasprops");
                boolean enableAliasMasking = "Y".equalsIgnoreCase(aliasProps.getProperty("enablemasking", "N"));
                if (!enableAliasMasking) continue;
                sensitiveCols.add("__sdialias");
            }
        }
        return sdcSensitiveColsMap;
    }

    public Map<String, Set<String>> getAllSensitivePrimaryColumns() throws SapphireException {
        if (this.allSensitivePrimaryColumns == null) {
            this.allSensitivePrimaryColumns = DataMaskUtil.getAllSensitivePrimaryColumns(this.policyTopNode);
        }
        return this.allSensitivePrimaryColumns;
    }

    private boolean isGlobalMaskingEnabled() {
        if (this.globalMaskingEnabled == null) {
            PropertyList policyGlobalProps = this.policyTopNode.getPropertyListNotNull("globalproperties");
            this.globalMaskingEnabled = "Y".equals(StringUtil.getYN(policyGlobalProps.getProperty("enablemasking", "N"), "N"));
        }
        return this.globalMaskingEnabled;
    }

    private PropertyList getPolicyTopNode() throws SapphireException {
        if (this.policyTopNode == null) {
            this.policyTopNode = this.configurationProcessor.getPolicy(MASKINGPOLICY, MASKINGPOLICY_TOPNODE);
        }
        return this.policyTopNode;
    }

    private boolean isUserHasGlobalRole() throws SapphireException {
        if (this.userHasGlobalRole == null) {
            String roleListStr = this.sapphireConnection.getRoleList();
            List<String> userRoleList = Arrays.asList(StringUtil.split(roleListStr, ";"));
            PropertyList policyGlobalProps = this.getPolicyTopNode().getPropertyListNotNull("globalproperties");
            String visibleToRole = policyGlobalProps.getProperty("visibletorole");
            this.userHasGlobalRole = userRoleList.contains(visibleToRole);
        }
        return this.userHasGlobalRole;
    }

    private boolean isAdhocQueryCheckRequired() throws SapphireException {
        if (this.adhocQueryCheckRequired == null) {
            this.adhocQueryCheckRequired = false;
            if (this.policyTopNode != null && this.isGlobalMaskingEnabled()) {
                PropertyList policyGlobalProps = this.policyTopNode.getPropertyListNotNull("globalproperties");
                this.adhocQueryCheckRequired = "Y".equals(StringUtil.getYN(policyGlobalProps.getProperty("applyonadhocquery", "Y"), "Y"));
            }
        }
        return this.adhocQueryCheckRequired;
    }

    public boolean isColumnSensitiveForAdhocQuery(String sdcId, String columnId) throws SapphireException {
        boolean sensitiveColFlag = false;
        if (this.isAdhocQueryCheckRequired() && !this.isUserHasGlobalRole() && this.getAllSensitivePrimaryColumns().containsKey(sdcId)) {
            Set<String> sensitiveColSet = this.getAllSensitivePrimaryColumns().get(sdcId);
            if (columnId.indexOf(".") > -1 || columnId.trim().startsWith("(")) {
                if ("sdialias.aliasid".equalsIgnoreCase(columnId) && sensitiveColSet.contains("__sdialias")) {
                    sensitiveColFlag = true;
                }
            } else if (sensitiveColSet.contains(columnId.toLowerCase())) {
                sensitiveColFlag = true;
            }
        }
        return sensitiveColFlag;
    }
}

