/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.ResultGridOptions;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ResultDataGrid
extends BaseCustom {
    private ConnectionInfo connectionInfo = null;
    private DataSet grid = null;
    private ResultGridOptions resultGridOptions = null;
    private StringBuilder executionLog = new StringBuilder();

    protected DataSet getDataSet() {
        return this.grid;
    }

    protected void setDataSet(DataSet grid) {
        this.grid = grid;
        this.checkColumns();
    }

    public String getExecutionLog() {
        return this.executionLog != null ? this.executionLog.toString() : "";
    }

    private void logInfo(String msg) {
        this.logger.info(msg);
        this.executionLog.append(msg).append("\n");
    }

    private void checkAndAddColumn(String columnName, int type) {
        if (!this.grid.isValidColumn(columnName)) {
            this.grid.addColumn(columnName, type);
        }
    }

    private void checkColumns() {
        for (CoreColumns c : CoreColumns.values()) {
            this.checkAndAddColumn(c.getColumnId(), c.getType());
        }
    }

    public ResultDataGrid(ConnectionInfo connectionInfo, File rakFile) {
        this.connectionInfo = connectionInfo;
        this.grid = new DataSet(connectionInfo);
        this.setConnectionId(connectionInfo.getConnectionId());
        this.setRakFile(rakFile);
        this.checkColumns();
    }

    public ResultDataGrid(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
        this.grid = new DataSet(connectionInfo);
        this.setConnectionId(connectionInfo.getConnectionId());
        this.checkColumns();
    }

    public int addResult(HashMap<CoreColumns, String> resultFields) {
        return this.addResult(resultFields, null);
    }

    public int addResult(HashMap<CoreColumns, String> resultFields, HashMap<String, String> additionalFields) {
        int r = this.grid.addRow();
        for (CoreColumns coreColumns : resultFields.keySet()) {
            this.grid.setValue(r, coreColumns.getColumnId(), resultFields.get((Object)coreColumns));
        }
        if (additionalFields != null) {
            for (String string : additionalFields.keySet()) {
                if (!this.grid.isValidColumn(string)) {
                    this.grid.addColumn(string, 0);
                }
                this.grid.setValue(r, string, additionalFields.get(string));
            }
        }
        return r;
    }

    protected void setOptions(ResultGridOptions resultGridOptions) {
        this.resultGridOptions = resultGridOptions;
    }

    protected ResultGridOptions getOptions() {
        return this.resultGridOptions;
    }

    public void save() throws SapphireException {
        ResultGridOptions options = this.resultGridOptions != null ? this.resultGridOptions : new ResultGridOptions();
        this.save(options);
    }

    /*
     * Unable to fully structure code
     */
    public void save(ResultGridOptions options) throws SapphireException {
        tp = this.getTranslationProcessor();
        this.executionLog = new StringBuilder();
        optionsForProcessing = options;
        dataToProcess = this.grid;
        if (dataToProcess.isIndexing()) {
            dataToProcess.getIndex().deleteAllIndexes();
        }
        autoAddSDI = optionsForProcessing.getAutoAddSDI();
        autoAddDataSet = optionsForProcessing.getAutoAddDataset();
        autoAddWorkItem = optionsForProcessing.getAutoAddWorkItem();
        autoAddParameter = optionsForProcessing.getAutoAddParameter();
        autoAddReplicate = optionsForProcessing.getAutoAddReplicate();
        defaultReplicateId = optionsForProcessing.getDefaultReplicateId();
        defaultDataSet = optionsForProcessing.getDefaultDataSet();
        releaseHandlingRule = optionsForProcessing.getReleaseHandlingRule();
        missingDataErrorHandling = optionsForProcessing.getMissingDataErrorHandling();
        if (dataToProcess.getRowCount() == 0) {
            throw new SapphireException(tp.translate("Found no data to process!"));
        }
        auditReason = optionsForProcessing.getAuditReason();
        auditActivity = optionsForProcessing.getAuditActivity();
        auditSignedFlag = optionsForProcessing.getAuditSignedFlag();
        auditDt = optionsForProcessing.getAuditDt();
        traceLogId = optionsForProcessing.getTraceLogId();
        applyLock = optionsForProcessing.getApplyLock();
        rg_id = CoreColumns.KEYID1.getColumnId();
        rg_id2 = CoreColumns.KEYID2.getColumnId();
        rg_id3 = CoreColumns.KEYID3.getColumnId();
        rg_plId = CoreColumns.PARAMLISTID.getColumnId();
        rg_plVersionId = CoreColumns.PARAMLISTVERSIONID.getColumnId();
        rg_plVariantId = CoreColumns.VARIANTID.getColumnId();
        rg_plDataSet = CoreColumns.DATASET.getColumnId();
        rg_plParamId = CoreColumns.PARAMID.getColumnId();
        rg_plParamType = CoreColumns.PARAMTYPE.getColumnId();
        rg_plReplicateId = CoreColumns.REPLICATEID.getColumnId();
        rg_plValue = CoreColumns.VALUE.getColumnId();
        rg_plWorkItemId = CoreColumns.WORKITEMID.getColumnId();
        sdcId = optionsForProcessing.getSdcId();
        if (sdcId == null || sdcId.length() == 0) {
            sdcId = "Sample";
        }
        sdcProcessor = this.getSDCProcessor();
        sdiProcessor = this.getSDIProcessor();
        ap = this.getActionProcessor();
        qp = this.getQueryProcessor();
        damProcessor = this.getDAMProcessor();
        sdcSingular = sdcProcessor.getProperty(sdcId, "singular", sdcId);
        sdcPlural = sdcProcessor.getProperty(sdcId, "plural", sdcId);
        keyColId1 = sdcProcessor.getProperty(sdcId, "keycolid1");
        validIdList = new ArrayList<String>();
        for (i = 0; i < dataToProcess.getRowCount(); ++i) {
            v0 = keyid1IsPopulated = dataToProcess.getValue(i, CoreColumns.KEYID1.getColumnId()).length() > 0;
            if (!keyid1IsPopulated) {
                $id = dataToProcess.getValue(i, CoreColumns.ID.getColumnId(), "");
                if ($id.length() <= 0) continue;
                if (validIdList.contains($id)) {
                    dataToProcess.setValue(i, CoreColumns.KEYID1.getColumnId(), $id);
                    continue;
                }
                ds = qp.getPreparedSqlDataSet("SELECT " + keyColId1 + " FROM " + sdcProcessor.getProperty(sdcId, "tableid") + " WHERE " + keyColId1 + " = ?", (Object[])new String[]{$id});
                if (ds.size() <= 0) continue;
                dataToProcess.setValue(i, CoreColumns.KEYID1.getColumnId(), $id);
                validIdList.add($id);
                continue;
            }
            $id = dataToProcess.getValue(i, CoreColumns.ID.getColumnId(), "");
            if ($id.length() <= 0 || $id.equals(dataToProcess.getValue(i, CoreColumns.KEYID1.getColumnId(), ""))) continue;
            throw new SapphireException(tp.translate("Ambiguous situation encountered. Found different values in both \"KEYID1\" and \"ID\" column, which one to be considered!"));
        }
        rsetId = "";
        lockrsetId = "";
        createLock = false;
        try {
            sdi = new SDIRequest();
            sdi.setSDCid(sdcId);
            sdi.setKeyid1List(dataToProcess.getColumnValues(rg_id, ";"));
            sdi.setRequestItem("primary");
            sdi.setRequestItem("dataset");
            sdi.setRequestItem("dataitem");
            sdi.setRequestItem("sdiworkitem");
            keyArray = StringUtil.split(sdi.getKeyid1List(), ";");
            for (k = 0; k < keyArray.length; ++k) {
                if (keyArray[k].length() <= 0) continue;
                createLock = true;
            }
            if (applyLock && this.connectionInfo != null && createLock) {
                isDeptSecurity = "D".equals(sdcProcessor.getProperty(sdcId, "accesscontrolledflag"));
                rsetId = damProcessor.createRSet(sdcId, sdi.getKeyid1List(), "", "", false, isDeptSecurity != false ? 1 : 0);
                lockrsetId = damProcessor.lockRSet(rsetId);
            }
            sdiData = null;
            if (createLock) {
                sdiData = sdiProcessor.getSDIData(sdi);
            }
            dsPrimary = sdiData != null ? sdiData.getDataset("primary") : new DataSet();
            dsDataSet = sdiData != null ? sdiData.getDataset("dataset") : new DataSet();
            dsDataItem = sdiData != null ? sdiData.getDataset("dataitem") : new DataSet();
            dsDataSetIndexColumns = new HashSet<String>();
            dsDataItemIndexColumns = new HashSet<String>();
            if (!dsPrimary.isIndexing() && dsPrimary.size() > 0 && dsPrimary.isValidColumn(keyColId1)) {
                dsPrimary.getIndex().createIndex(keyColId1);
                dsPrimary.getIndex().setMinSizeForIndexing(0);
            }
            if (!dsDataSet.isIndexing() && dsDataSet.size() > 0) {
                if (dsDataSet.isValidColumn("keyid1")) {
                    dsDataSetIndexColumns.add("keyid1");
                }
                if (dsDataSet.isValidColumn("paramlistid")) {
                    dsDataSetIndexColumns.add("paramlistid");
                }
                if (dsDataSet.isValidColumn("paramlistversionid")) {
                    dsDataSetIndexColumns.add("paramlistversionid");
                }
                if (dsDataSet.isValidColumn("variantid")) {
                    dsDataSetIndexColumns.add("variantid");
                }
                if (dsDataSetIndexColumns.size() > 0) {
                    dsDataSet.getIndex().createIndex(dsDataSetIndexColumns);
                    dsDataSet.getIndex().setMinSizeForIndexing(0);
                }
            }
            if (!dsDataItem.isIndexing() && dsDataItem.size() > 0) {
                if (dsDataItem.isValidColumn("keyid1")) {
                    dsDataItemIndexColumns.add("keyid1");
                }
                if (dsDataItem.isValidColumn("paramlistversionid")) {
                    dsDataItemIndexColumns.add("paramlistversionid");
                }
                if (dsDataItem.isValidColumn("paramlistid")) {
                    dsDataItemIndexColumns.add("paramlistid");
                }
                if (dsDataItem.isValidColumn("variantid")) {
                    dsDataItemIndexColumns.add("variantid");
                }
                if (dsDataItem.isValidColumn("paramid")) {
                    dsDataItemIndexColumns.add("paramid");
                }
                if (dsDataItemIndexColumns.size() > 0) {
                    dsDataItem.getIndex().createIndex(dsDataItemIndexColumns);
                    dsDataItem.getIndex().setMinSizeForIndexing(0);
                }
            }
            addPrimary = new DataSet();
            dsAddSDIWorkItem = new DataSet();
            dsAddDataSet = new DataSet();
            dsRemeasureDataSet = new DataSet();
            dsAddDataItem = new DataSet();
            dsAddReplicate = new DataSet();
            dsEnterDataItem = new DataSet();
            columns = dataToProcess.getColumns();
            primaryColumnData = sdcProcessor.getColumnData(sdcId);
            datasetColumnData = sdcProcessor.getColumnData("DataSet");
            sdiworkitemColumnData = sdcProcessor.getColumnData("SDIWorkItem");
            dataitemColumnData = sdcProcessor.getColumnData("DataItem");
            coreColsList = optionsForProcessing.getCoreColsList();
            id_AddSDIDataSetMap = new HashMap<String, DataSet>();
            for (i = 0; i < dataToProcess.getRowCount(); ++i) {
                primaryId = dataToProcess.getValue(i, rg_id);
                v1 = primaryNotFound = dsPrimary.findRow(keyColId1, primaryId) < 0;
                if (!primaryNotFound) continue;
                token = new HashMap<String, String>();
                token.put("primaryid", primaryId);
                token.put("sdcid", sdcSingular);
                if (ResultGridOptions.AutoAddSDI.NEVER == autoAddSDI) {
                    if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                        dataToProcess.setString(i, "__invaliddata", "Y");
                        this.logInfo(tp.translate("[sdcid] [primaryid] could not be found. [sdcid] is not be added as AutoAddSDI option is set.", token));
                        continue;
                    }
                    if (primaryId.length() > 0) {
                        throw new SapphireException(tp.translate("[sdcid] [primaryid] could not be found. [sdcid] could not be added as AutoAddSDI option is not set", token));
                    }
                    throw new SapphireException(tp.translate("[sdcid] could not be found. [sdcid] could not be added as AutoAddSDI option is not set", token));
                }
                if (ResultGridOptions.AutoAddSDI.ALWAYS != autoAddSDI) continue;
                if (primaryId.length() > 0) {
                    if (addPrimary.findRow("keyid1", primaryId) >= 0) continue;
                    r = addPrimary.addRow();
                    addPrimary.setString(r, "keyid1", primaryId);
                    for (col = 0; col < columns.length; ++col) {
                        if (coreColsList.contains(columns[col]) || primaryColumnData.findRow("columnid", columns[col]) <= -1) continue;
                        addPrimary.setString(r, columns[col], dataToProcess.getValue(i, columns[col]));
                    }
                    continue;
                }
                id = dataToProcess.getValue(i, CoreColumns.ID.getColumnId());
                if (id.length() > 0) {
                    if (id_AddSDIDataSetMap.containsKey(id)) continue;
                    addPrimaryWithNoId = new DataSet();
                    r = addPrimaryWithNoId.addRow();
                    addPrimaryWithNoId.setString(r, "keyid1", primaryId);
                    for (col = 0; col < columns.length; ++col) {
                        if (coreColsList.contains(columns[col]) || primaryColumnData.findRow("columnid", columns[col]) <= -1) continue;
                        addPrimaryWithNoId.setString(r, columns[col], dataToProcess.getValue(i, columns[col]));
                    }
                    id_AddSDIDataSetMap.put(id, addPrimaryWithNoId);
                    continue;
                }
                if (id_AddSDIDataSetMap.containsKey("id_Empty")) continue;
                addPrimaryWithNoId = new DataSet();
                r = addPrimaryWithNoId.addRow();
                addPrimaryWithNoId.setString(r, "keyid1", primaryId);
                for (col = 0; col < columns.length; ++col) {
                    if (coreColsList.contains(columns[col]) || primaryColumnData.findRow("columnid", columns[col]) <= -1) continue;
                    addPrimaryWithNoId.setString(r, columns[col], dataToProcess.getValue(i, columns[col]));
                }
                id_AddSDIDataSetMap.put("id_Empty", addPrimaryWithNoId);
            }
            if (addPrimary.getRowCount() > 0) {
                addSDIProps = optionsForProcessing.getAddSDIProperties();
                actionProps = new PropertyList();
                if (addSDIProps != null) {
                    actionProps = new PropertyList(addSDIProps);
                }
                actionProps.setProperty("sdcid", sdcId);
                actionProps.setProperty("keyid1", addPrimary.getColumnValues("keyid1", ";"));
                cols = addPrimary.getColumns();
                for (c = 0; c < cols.length; ++c) {
                    actionProps.setProperty(cols[c], addPrimary.getColumnValues(cols[c], ";"));
                }
                actionProps.setProperty("overrideautokey", "Y");
                actionProps.setProperty("auditreason", auditReason);
                actionProps.setProperty("auditactivity", auditActivity);
                actionProps.setProperty("auditsignedflag", auditSignedFlag);
                actionProps.setProperty("auditdt", auditDt);
                actionProps.setProperty("tracelogid", traceLogId);
                ap.processAction("AddSDI", "1", actionProps);
                newKeyid = actionProps.getProperty("newkeyid1");
                if (newKeyid.length() > 0) {
                    for (i = 0; i < dataToProcess.getRowCount(); ++i) {
                        primaryId = dataToProcess.getValue(i, rg_id);
                        if (!primaryId.equals(newKeyid)) continue;
                        dataToProcess.setString(i, CoreColumns.NEWKEYID1.getColumnId(), newKeyid);
                    }
                } else {
                    throw new SapphireException(sdcPlural + " " + tp.translate("could not be created!"));
                }
            }
            for (String idKey : id_AddSDIDataSetMap.keySet()) {
                addSDI = (DataSet)id_AddSDIDataSetMap.get(idKey);
                addSDIProps = optionsForProcessing.getAddSDIProperties();
                actionProps = new PropertyList();
                if (addSDIProps != null) {
                    actionProps = new PropertyList(addSDIProps);
                }
                copies = 1;
                actionProps.setProperty("sdcid", sdcId);
                actionProps.setProperty("copies", Integer.toString(copies));
                cols = addSDI.getColumns();
                for (c = 0; c < cols.length; ++c) {
                    actionProps.setProperty(cols[c], addSDI.getColumnValues(cols[c], ";"));
                }
                actionProps.setProperty("auditreason", auditReason);
                actionProps.setProperty("auditactivity", auditActivity);
                actionProps.setProperty("auditsignedflag", auditSignedFlag);
                actionProps.setProperty("auditdt", auditDt);
                actionProps.setProperty("tracelogid", traceLogId);
                ap.processAction("AddSDI", "1", actionProps);
                newKeyid = actionProps.getProperty("newkeyid1");
                if (newKeyid.length() > 0) {
                    for (i = 0; i < dataToProcess.getRowCount(); ++i) {
                        primaryId = dataToProcess.getValue(i, rg_id);
                        if (primaryId.length() != 0) continue;
                        $id = dataToProcess.getValue(i, CoreColumns.ID.getColumnId());
                        if (idKey.equalsIgnoreCase("id_Empty")) {
                            if ($id.length() != 0) continue;
                            dataToProcess.setString(i, rg_id, newKeyid);
                            dataToProcess.setString(i, CoreColumns.NEWKEYID1.getColumnId(), newKeyid);
                            continue;
                        }
                        if (!$id.equals(idKey)) continue;
                        dataToProcess.setString(i, rg_id, newKeyid);
                        dataToProcess.setString(i, CoreColumns.NEWKEYID1.getColumnId(), newKeyid);
                    }
                    continue;
                }
                throw new SapphireException(sdcPlural + " " + tp.translate("could not be created!"));
            }
            plVersionMap = new HashMap<String, String>();
            firstAvailableDataset = false;
            firstAvailableReplicate = false;
            if (ResultGridOptions.DefaultReplicateId.MAX_PLUS_ONE == defaultReplicateId) {
                throw new SapphireException(tp.translate("\"Max plus One\" is now deprecated. New DefaultReplicate option is \"First Available\""));
            }
            if (ResultGridOptions.DefaultDataSet.MAX_PLUS_ONE == defaultDataSet) {
                throw new SapphireException(tp.translate("\"Max plus One\" is now deprecated. New DefaultDataSet option is \"First Available\""));
            }
            if (ResultGridOptions.DefaultReplicateId.FIRST_AVAILABLE == defaultReplicateId) {
                firstAvailableReplicate = true;
            }
            if (ResultGridOptions.DefaultDataSet.FIRST_AVAILABLE == defaultDataSet) {
                firstAvailableDataset = true;
            }
            for (i = 0; i < dataToProcess.getRowCount(); ++i) {
                if ("Y".equalsIgnoreCase(dataToProcess.getValue(i, "__invaliddata"))) continue;
                primaryId = dataToProcess.getValue(i, rg_id);
                workitemId = dataToProcess.getValue(i, rg_plWorkItemId);
                paramlistId = dataToProcess.getValue(i, rg_plId);
                paramlistVersionId = dataToProcess.getValue(i, rg_plVersionId);
                variantId = dataToProcess.getValue(i, rg_plVariantId);
                dataset = dataToProcess.getBigDecimal(i, rg_plDataSet);
                if (dataset != null && dataset.equals(new BigDecimal(0))) {
                    dataset = null;
                    dataToProcess.setValue(i, rg_plDataSet, null);
                }
                paramId = dataToProcess.getValue(i, rg_plParamId);
                value = dataToProcess.getValue(i, rg_plValue);
                if (value.length() == 0 && dataToProcess.isValidColumn("enteredtext")) {
                    value = dataToProcess.getValue(i, "enteredtext");
                }
                paramType = dataToProcess.getValue(i, rg_plParamType);
                replicateId = dataToProcess.getBigDecimal(i, rg_plReplicateId);
                if (replicateId != null && replicateId.equals(new BigDecimal(0))) {
                    replicateId = null;
                    dataToProcess.setValue(i, rg_plReplicateId, null);
                }
                if (replicateId == null && !firstAvailableReplicate) {
                    replicateId = new BigDecimal(1);
                    dataToProcess.setNumber(i, rg_plReplicateId, replicateId);
                }
                if (dataset == null && !firstAvailableDataset) {
                    dataset = new BigDecimal(1);
                    dataToProcess.setNumber(i, rg_plDataSet, "1");
                }
                if (paramId.length() == 0) {
                    throw new SapphireException(tp.translate("Cannot continue. It is mandatory to provide Param in each row."));
                }
                findDS = new HashMap<String, Object>();
                findDS.put("keyid1", primaryId);
                token = new HashMap<String, String>();
                token.put("primaryid", primaryId);
                token.put("sdcid", sdcSingular);
                token.put("paramid", paramId);
                token.put("value", value);
                if (paramlistId.length() > 0) {
                    findDS.put("paramlistid", paramlistId);
                }
                if (paramlistVersionId.length() > 0 && !"C".equalsIgnoreCase(paramlistVersionId)) {
                    findDS.put("paramlistversionid", paramlistVersionId);
                }
                if (variantId.length() > 0) {
                    findDS.put("variantid", variantId);
                }
                if (workitemId.length() > 0) {
                    findDS.put("sourceworkitemid", workitemId);
                }
                datasetRow = -1;
                dsFilterWithPLVariant = dsDataSet.getFilteredDataSet(findDS);
                plVariantFound = dsFilterWithPLVariant.getRowCount() > 0;
                datasetFound = false;
                maxDataSet = null;
                if (plVariantFound) {
                    dsFilterWithPLVariant.sort("dataset D");
                    maxDataSet = dsFilterWithPLVariant.getBigDecimal(0, "dataset");
                    if (dataset != null) {
                        findDS.put("dataset", dataset);
                        datasetRow = dsDataSet.findRow(findDS);
                        datasetFound = datasetRow > -1;
                        token = new HashMap<K, V>();
                        token.put("primaryid", primaryId);
                        token.put("dataset", dataset.toString());
                        token.put("sdcid", sdcSingular);
                        if (!datasetFound) {
                            if (paramlistId.length() > 0 && variantId.length() > 0) {
                                if (dataset.compareTo(maxDataSet) < 0) {
                                    token.put("paramlistid", paramlistId);
                                    token.put("variantid", variantId);
                                    errMsg = "DataSet cannot be created. The required dataset number [dataset] is lesser than the max dataset number found in the [sdcid] [primaryid] with the matching";
                                    errMsg = errMsg + " paramlistid [paramlistid] and variantid [variantid].";
                                    throw new SapphireException(tp.translate(errMsg, token));
                                }
                            } else {
                                throw new SapphireException(tp.translate("[sdcid] [primaryid] does not contain dataset [dataset]. ParamList and Variant are mandatory to add the dataset.", token));
                            }
                            if (ResultGridOptions.AutoAddDataSet.REMEASURE == autoAddDataSet) {
                                dataToProcess.setNumber(i, "__remeasuredataset", maxDataSet);
                                if (paramlistVersionId.length() == 0 || "C".equalsIgnoreCase(paramlistVersionId)) {
                                    paramlistVersionId = dsFilterWithPLVariant.getValue(0, "paramlistversionid");
                                    dataToProcess.setString(i, rg_plVersionId, paramlistVersionId);
                                }
                            } else if (ResultGridOptions.AutoAddDataSet.ALWAYS != autoAddDataSet) {
                                if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                    dataToProcess.setString(i, "__invaliddata", "Y");
                                    this.logInfo(tp.translate("Dataset [dataset] not found in [sdcid] [primaryid]. New dataset is not be added as AutoAddDataSet option is set.", token));
                                    continue;
                                }
                                throw new SapphireException(tp.translate("Dataset [dataset] not found in [sdcid] [primaryid]. New dataset could not be added as AutoAddDataSet option is not set.", token));
                            }
                        } else {
                            datasetStatus = dsDataSet.getValue(datasetRow, "s_datasetstatus");
                            if ("Completed".equalsIgnoreCase(datasetStatus) || "Cancelled".equalsIgnoreCase(datasetStatus) || "Released".equalsIgnoreCase(datasetStatus)) {
                                if (ResultGridOptions.AutoAddDataSet.ALWAYS == autoAddDataSet || ResultGridOptions.AutoAddDataSet.REMEASURE == autoAddDataSet) {
                                    datasetFound = false;
                                    if (ResultGridOptions.AutoAddDataSet.REMEASURE == autoAddDataSet) {
                                        dataToProcess.setNumber(i, "__remeasuredataset", dataset);
                                    }
                                    dataset = maxDataSet.add(new BigDecimal(1));
                                    dataToProcess.setNumber(i, rg_plDataSet, dataset.toString());
                                } else {
                                    throw new SapphireException(tp.translate("DataSet [dataset] in [sdcid] [primaryid] is in " + datasetStatus + " status. DataSet in " + datasetStatus + " status cannot be posted with result.", token));
                                }
                            }
                            if (paramlistVersionId.length() == 0 || "C".equalsIgnoreCase(paramlistVersionId)) {
                                paramlistVersionId = dsDataSet.getValue(datasetRow, "paramlistversionid");
                                dataToProcess.setString(i, rg_plVersionId, paramlistVersionId);
                            }
                            if (datasetFound) {
                                continue;
                            }
                        }
                    } else if (paramId.length() > 0) {
                        findDS.remove("sourceworkitemid");
                        findDS.put("paramid", paramId);
                        paramNotExists = dsDataItem.findRow(findDS) < 0;
                        findDS.remove("paramid");
                        if (paramNotExists) {
                            findDS.put("instrumentfieldid", paramId);
                            paramNotExists = dsDataItem.findRow(findDS) < 0;
                            findDS.remove("instrumentfieldid");
                            if (paramNotExists) {
                                findDS.put("aliasid", paramId);
                                paramNotExists = dsDataItem.findRow(findDS) < 0;
                                findDS.remove("aliasid");
                            }
                        }
                        token.put("paramlistid", paramlistId);
                        token.put("variantid", variantId);
                        findDS.put("sourceworkitemid", workitemId);
                        if (paramNotExists) {
                            if (ResultGridOptions.AutoAddParameter.ALWAYS != autoAddParameter) {
                                msg = "[paramid] does not exist in [sdcid] [primaryid]. To add non existing parameter AutoAddParameter is not set.";
                                if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                    dataToProcess.setString(i, "__invaliddata", "Y");
                                    this.logInfo(tp.translate(msg, token));
                                    continue;
                                }
                                throw new SapphireException(tp.translate(msg, token));
                            }
                            if (firstAvailableDataset && dsFilterWithPLVariant.getRowCount() > 0) {
                                d = this.getFirstAvailableDataSet(dsFilterWithPLVariant);
                                if (d > -1) {
                                    dataset = dsFilterWithPLVariant.getBigDecimal(d, "dataset");
                                    datasetFound = true;
                                    dataToProcess.setNumber(i, rg_plDataSet, dataset);
                                    if (paramlistVersionId.length() == 0) {
                                        paramlistVersionId = dsFilterWithPLVariant.getValue(d, "paramlistversionid");
                                        dataToProcess.setString(i, rg_plVersionId, paramlistVersionId);
                                    }
                                }
                                if (!datasetFound && autoAddDataSet != ResultGridOptions.AutoAddDataSet.ALWAYS) {
                                    msg = tp.translate("DataSet(s) in [sdcid] [primaryid] are not in Initial or InProgress status. New Dataset cannot be created as AutoAddDataSet not set. No DataSet can be posted with result [value] in [paramid].", token);
                                    if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                        dataToProcess.setString(i, "__invaliddata", "Y");
                                        this.logInfo(msg);
                                        continue;
                                    }
                                    throw new SapphireException(msg);
                                }
                            }
                        }
                    }
                    if (datasetFound) continue;
                    if (dataset == null) {
                        findDS.remove("sourceworkitemid");
                        findDS.put("paramid", paramId);
                        if (paramType.length() > 0) {
                            findDS.put("paramtype", paramType);
                        }
                        if (replicateId != null) {
                            findDS.put("replicateid", replicateId);
                        }
                        if ((dataItems = dsDataItem.getFilteredDataSet(findDS)).getRowCount() == 0) {
                            findDS.remove("paramid");
                            findDS.put("instrumentfieldid", paramId);
                            dataItems = dsDataItem.getFilteredDataSet(findDS);
                            if (dataItems.getRowCount() == 0) {
                                findDS.remove("instrumentfieldid");
                                findDS.put("aliasid", paramId);
                                dataItems = dsDataItem.getFilteredDataSet(findDS);
                            }
                            if (dataItems.getRowCount() > 0) {
                                dataToProcess.setString(i, "_actualparamid", dataItems.getValue(0, "paramid"));
                            }
                        }
                        if (dataItems.getRowCount() > 0) {
                            freeDataItemCnt = 0;
                            for (r = 0; r < dataItems.getRowCount(); ++r) {
                                if (dataItems.getValue(r, "enteredtext").length() != 0) continue;
                                ++freeDataItemCnt;
                                dataItems.setValue(r, "enteredtext", value);
                                break;
                            }
                            if (freeDataItemCnt > 0) continue;
                            if (ResultGridOptions.AutoAddReplicate.ALWAYS == autoAddReplicate && replicateId == null) {
                                if (firstAvailableDataset && dsFilterWithPLVariant.getRowCount() > 0 && (d = this.getFirstAvailableDataSet(dsFilterWithPLVariant)) > -1) {
                                    dataset = dsFilterWithPLVariant.getBigDecimal(d, "dataset");
                                    datasetFound = true;
                                    dataToProcess.setNumber(i, rg_plDataSet, dataset);
                                    if (paramlistVersionId.length() == 0) {
                                        paramlistVersionId = dsFilterWithPLVariant.getValue(d, "paramlistversionid");
                                        dataToProcess.setString(i, rg_plVersionId, paramlistVersionId);
                                    }
                                }
                            } else if (ResultGridOptions.AutoAddDataSet.ALWAYS == autoAddDataSet) {
                                dataItems.sort("dataset D");
                                if (paramlistId.length() == 0) {
                                    paramlistId = dataItems.getValue(0, "paramlistid");
                                    dataToProcess.setValue(i, rg_plId, paramlistId);
                                }
                                if (variantId.length() == 0) {
                                    variantId = dataItems.getValue(0, "variantid");
                                    dataToProcess.setValue(i, rg_plVariantId, variantId);
                                }
                            } else if (ResultGridOptions.AutoAddDataSet.REMEASURE != autoAddDataSet) {
                                msg = "All dataitems are already filled in. Options to create new dataset not selected.";
                                msg = msg + " Param [paramid] in [sdcid] [primaryid] cannot be posted with [value].";
                                if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                    this.logInfo(tp.translate(msg, token));
                                    dataToProcess.setString(i, "__invaliddata", "Y");
                                    continue;
                                }
                                throw new SapphireException(tp.translate(msg, token));
                            }
                        } else if (ResultGridOptions.AutoAddReplicate.ALWAYS == autoAddReplicate && firstAvailableDataset && dsFilterWithPLVariant.getRowCount() > 0 && (d = this.getFirstAvailableDataSet(dsFilterWithPLVariant)) > -1) {
                            dataset = dsFilterWithPLVariant.getBigDecimal(d, "dataset");
                            datasetFound = true;
                            dataToProcess.setNumber(i, rg_plDataSet, dataset);
                            if (paramlistVersionId.length() == 0) {
                                paramlistVersionId = dsFilterWithPLVariant.getValue(d, "paramlistversionid");
                                dataToProcess.setString(i, rg_plVersionId, paramlistVersionId);
                            }
                        }
                    }
                }
                if (datasetFound) continue;
                token = new HashMap<K, V>();
                token.put("primaryid", primaryId);
                token.put("sdcid", sdcSingular);
                token.put("paramlistid", paramlistId);
                if (dataset != null) {
                    token.put("dataset", dataset.toString());
                } else {
                    token.put("dataset", "");
                }
                if (ResultGridOptions.AutoAddDataSet.NEVER == autoAddDataSet) {
                    msg = "";
                    if (paramlistId.length() > 0) {
                        msg = msg + "ParamList [paramlistid] ";
                    }
                    if (paramlistVersionId.length() > 0) {
                        token.put("paramlistversionid", paramlistVersionId);
                        msg = msg + ", ParamListVersion [paramlistversionid] ";
                    }
                    if (variantId.length() > 0) {
                        token.put("variantid", variantId);
                        msg = msg + ", Variant [variantid] ";
                    }
                    msg = msg + " DataSet " + (dataset != null ? "[dataset]" : "") + " could not be found in [sdcid] [primaryid].";
                    if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                        dataToProcess.setString(i, "__invaliddata", "Y");
                        this.logInfo(tp.translate(msg + " DataSet not added as AutoAddDataSet option is not set.", token));
                        continue;
                    }
                    throw new SapphireException(tp.translate(msg + " DataSet not added as AutoAddDataSet option is not set.", token));
                }
                if (ResultGridOptions.AutoAddDataSet.ALWAYS == autoAddDataSet) {
                    if (ResultGridOptions.AutoAddWorkItem.ALWAYS == autoAddWorkItem) {
                        if (workitemId.length() > 0) {
                            dsWorkItem = qp.getPreparedSqlDataSet("select workitemversionid from workitem where workitemid = ? and ( versionstatus = 'P' or versionstatus = 'C' )  order by versionstatus, cast (workitemversionid as numeric) desc", (Object[])new String[]{workitemId});
                            token.put("workitemid", workitemId);
                            if (dsWorkItem.getRowCount() == 0) {
                                missingMsg = tp.translate("Invalid data: WorkItem [workitemid] does not not exist.", token);
                                if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                    dataToProcess.setString(i, "__invaliddata", "Y");
                                    this.logInfo(missingMsg);
                                    continue;
                                }
                                throw new SapphireException(missingMsg);
                            }
                            workitemVersionId = dsWorkItem.getValue(0, "workitemversionid");
                            dsWorkItemItem = qp.getPreparedSqlDataSet("select * from workitemitem where workitemid = ? and sdcid = ?", (Object[])new String[]{workitemId, "ParamList"});
                            findParamList = new HashMap<String, String>();
                            token.put("workitemversionid", workitemVersionId);
                            findParamList.put("workitemversionid", workitemVersionId);
                            msg = new StringBuffer("Invalid data: No matching ");
                            if (paramlistId.length() > 0) {
                                findParamList.put("keyid1", paramlistId);
                                token.put("paramlistid", paramlistId);
                                msg.append("ParamList [paramlistid]");
                            }
                            if (paramlistVersionId.length() > 0) {
                                findParamList.put("keyid2", paramlistVersionId);
                                token.put("paramlistversionid", paramlistVersionId);
                                msg.append(" ( Version : [paramlistversionid] )");
                            }
                            if (variantId.length() > 0) {
                                findParamList.put("keyid3", variantId);
                                token.put("variantid", variantId);
                                msg.append(", Variant: [variantid]");
                            }
                            if (findParamList.size() > 0 && dsWorkItemItem.findRow(findParamList) < 0) {
                                msg.append(" found in the WorkItem [workitemid]( Version: [workitemversionid] ) to be added to the [sdcid] [primaryid].");
                                if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                    dataToProcess.setString(i, "__invaliddata", "Y");
                                    this.logInfo(tp.translate(msg.toString(), token));
                                    continue;
                                }
                                throw new SapphireException(tp.translate(msg.toString(), token));
                            }
                            if (dsAddSDIWorkItem.getRowCount() > 0) {
                                find = new HashMap<String, String>();
                                find.put(rg_id, primaryId);
                                find.put(rg_plWorkItemId, workitemId);
                                if (dsAddSDIWorkItem.findRow(find) > -1) continue;
                            }
                            dsAddSDIWorkItem.copyRow(dataToProcess, i, 1);
                            continue;
                        }
                        if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                            dataToProcess.setString(i, "__invaliddata", "Y");
                            this.logInfo(tp.translate("AutoAddWorkItem option is set, but workitemid is not specified!"));
                            continue;
                        }
                        throw new SapphireException(tp.translate("AutoAddWorkItem option is set, but workitemid is not specified!"));
                    }
                    if (paramlistId.length() == 0 || variantId.length() == 0) {
                        if (missingDataErrorHandling != ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                            throw new SapphireException(tp.translate("[sdcid] [primaryid] does not contain dataset [dataset]. ParamList and Variant are mandatory to add the dataset.", token));
                        }
                        dataToProcess.setString(i, "__invaliddata", "Y");
                        this.logInfo(tp.translate("[sdcid] [primaryid] does not contain dataset [dataset]. ParamList and Variant are mandatory to add the dataset.", token));
                        continue;
                    }
                    if (workitemId.length() > 0) {
                        token = new HashMap<K, V>();
                        token.put("primaryid", primaryId);
                        token.put("sdcid", sdcSingular);
                        token.put("paramlistid", paramlistId);
                        token.put("variantid", variantId);
                        if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                            dataToProcess.setString(i, "__invaliddata", "Y");
                            this.logInfo(tp.translate("AutoAddWorkItem option is not set, but workitemid is specified! [sdcid] [primaryid] cannot be added with dataset for [paramlistid],[variantid]."));
                            continue;
                        }
                        throw new SapphireException(tp.translate("AutoAddWorkItem option is not set, but workitemid is specified! [sdcid] [primaryid] cannot be added with dataset for [paramlistid],[variantid]."));
                    }
                    if (dsAddDataSet.getRowCount() > 0) {
                        find = new HashMap<String, Object>();
                        find.put(rg_id, primaryId);
                        find.put(rg_plId, paramlistId);
                        if (paramlistVersionId.length() > 0 && !"C".equalsIgnoreCase(paramlistVersionId)) {
                            find.put(rg_plVersionId, paramlistVersionId);
                        }
                        find.put(rg_plVariantId, variantId);
                        if (dataset != null) {
                            find.put(rg_plDataSet, dataset);
                        } else {
                            find.put(rg_plDataSet, new BigDecimal("-1"));
                        }
                        findDataSetRow = dsAddDataSet.findRow(find);
                        if (findDataSetRow > -1) {
                            if (paramlistVersionId.length() != 0) continue;
                            dataToProcess.setString(i, rg_plVersionId, dsAddDataSet.getValue(findDataSetRow, "paramlistversionid"));
                            continue;
                        }
                    }
                    checkSql = "select 1 from paramlist where paramlistid = ? AND variantid = ?";
                    versionGiven = paramlistVersionId.length() > 0 && "C".equalsIgnoreCase(paramlistVersionId) == false;
                    checkSql = checkSql + (versionGiven != false ? " AND paramlistversionid = ?" : "");
                    if (versionGiven) {
                        v2 = new String[3];
                        v2[0] = paramlistId;
                        v2[1] = variantId;
                        v3 = v2;
                        v2[2] = paramlistVersionId;
                    } else {
                        v4 = new String[2];
                        v4[0] = paramlistId;
                        v3 = v4;
                        v4[1] = variantId;
                    }
                    args = v3;
                    dsPL = qp.getPreparedSqlDataSet(checkSql, args);
                    if (dsPL.getRowCount() == 0) {
                        tokenMap = new HashMap<String, String>();
                        tokenMap.put("paramlistid", paramlistId);
                        tokenMap.put("variantid", variantId);
                        tokenMap.put("paramlistversionid", paramlistVersionId);
                        missingMsg = "Invalid Data: ParamList [paramlistid],";
                        if (versionGiven) {
                            missingMsg = missingMsg + " ,version [paramlistversionid]";
                        }
                        missingMsg = missingMsg + " variant [variantid]";
                        missingMsg = missingMsg + " does not exist.";
                        if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                            dataToProcess.setString(i, "__invaliddata", "Y");
                            this.logInfo(tp.translate(missingMsg, tokenMap));
                            continue;
                        }
                        throw new SapphireException(tp.translate(missingMsg, tokenMap));
                    }
                    if (paramlistVersionId.length() == 0 || "C".equalsIgnoreCase(paramlistVersionId)) {
                        mapKey = paramlistId + ";" + variantId;
                        if (plVersionMap.containsKey(mapKey)) {
                            paramlistVersionId = (String)plVersionMap.get(mapKey);
                        } else {
                            paramlistVersionId = this.getCurrentPLVersion(paramlistId, variantId, qp, tp);
                            plVersionMap.put(mapKey, paramlistVersionId);
                        }
                        dataToProcess.setValue(i, rg_plVersionId, paramlistVersionId);
                    }
                    dsAddDataSet.copyRow(dataToProcess, i, 1);
                    if (dsAddDataSet.getValue(dsAddDataSet.getRowCount() - 1, "dataset").length() != 0) continue;
                    dsAddDataSet.setNumber(dsAddDataSet.getRowCount() - 1, "dataset", new BigDecimal("-1"));
                    continue;
                }
                if (ResultGridOptions.AutoAddDataSet.REMEASURE != autoAddDataSet) continue;
                if (paramlistId.length() == 0 || variantId.length() == 0) {
                    if (missingDataErrorHandling != ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                        throw new SapphireException(tp.translate("[sdcid] [primaryid] does not contain dataset [dataset]. ParamList and Variant are mandatory to add the dataset.", token));
                    }
                    dataToProcess.setString(i, "__invaliddata", "Y");
                    this.logInfo(tp.translate("[sdcid] [primaryid] does not contain dataset [dataset]. ParamList and Variant are mandatory to add the dataset.", token));
                    continue;
                }
                if (plVariantFound) {
                    find = new HashMap<String, Object>();
                    find.put(rg_id, primaryId);
                    find.put(rg_plId, paramlistId);
                    find.put(rg_plVersionId, paramlistVersionId);
                    find.put(rg_plVariantId, variantId);
                    if (dataset != null) {
                        find.put(rg_plDataSet, dataset);
                    } else {
                        dataToProcess.setNumber(i, "__remeasuredataset", maxDataSet);
                        dataToProcess.setValue(i, rg_plDataSet, String.valueOf(maxDataSet.intValue() + 1));
                        if (paramlistVersionId.length() == 0 || "C".equalsIgnoreCase(paramlistVersionId)) {
                            paramlistVersionId = dsFilterWithPLVariant.getValue(0, "paramlistversionid");
                            dataToProcess.setString(i, rg_plVersionId, paramlistVersionId);
                        }
                    }
                    if (dsRemeasureDataSet.findRow(find) > -1) continue;
                    dsRemeasureDataSet.copyRow(dataToProcess, i, 1);
                    continue;
                }
                errorMsg = "AutoAddDataSet option set as \"REMEASURE\". DataSet cannot be remeasured as no matching dataset exists in the [sdcid] [primaryid] ";
                token.put("paramlistid", paramlistId);
                token.put("variantid", variantId);
                if (paramlistId.length() > 0) {
                    errorMsg = errorMsg + " with the paramlistid [paramlistid]";
                }
                if (variantId.length() > 0) {
                    errorMsg = errorMsg + " and variantid [variantid]";
                }
                if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                    dataToProcess.setString(i, "__invaliddata", "Y");
                    this.logInfo(tp.translate(errorMsg, token));
                    continue;
                }
                throw new SapphireException(tp.translate(errorMsg, token));
            }
            if (dsAddSDIWorkItem.getRowCount() > 0) {
                actionProps = new PropertyList();
                actionProps.setProperty("sdcid", sdcId);
                actionProps.setProperty("keyid1", dsAddSDIWorkItem.getColumnValues(rg_id, ";"));
                actionProps.setProperty("workitemid", dsAddSDIWorkItem.getColumnValues(rg_plWorkItemId, ";"));
                for (col = 0; col < columns.length; ++col) {
                    if (coreColsList.contains(columns[col]) || sdiworkitemColumnData.findRow("columnid", columns[col]) <= -1) continue;
                    actionProps.setProperty(columns[col], dsAddSDIWorkItem.getColumnValues(columns[col], ";"));
                }
                actionProps.setProperty("propsmatch", "Y");
                actionProps.setProperty("auditreason", auditReason);
                actionProps.setProperty("auditactivity", auditActivity);
                actionProps.setProperty("auditsignedflag", auditSignedFlag);
                actionProps.setProperty("auditdt", auditDt);
                actionProps.setProperty("tracelogid", traceLogId);
                ap.processAction("AddSDIWorkItem", "1", actionProps);
                wiInstances = actionProps.getProperty("workiteminstance");
                dsAddSDIWorkItem.addColumnValues("workiteminstance", 1, wiInstances, ";");
                for (i = 0; i < dsAddSDIWorkItem.getRowCount(); ++i) {
                    keyid1 = dsAddSDIWorkItem.getValue(i, rg_id);
                    workItemId = dsAddSDIWorkItem.getValue(i, rg_plWorkItemId);
                    workItemInstance = dsAddSDIWorkItem.getValue(i, "workiteminstance");
                    dsPLWorkItemWorkItemData = qp.getPreparedSqlDataSet("select * from sdidata where sdcid = ? and keyid1 = ? and sourceworkitemid = ? and sourceworkiteminstance = ?", (Object[])new String[]{sdcId, keyid1, workItemId, workItemInstance});
                    findDataSets = new HashMap<String, Object>();
                    findDataSets.put(rg_id, keyid1);
                    findDataSets.put(rg_plWorkItemId, workItemId);
                    filteredDs = dataToProcess.getFilteredDataSet(findDataSets);
                    if (filteredDs.getRowCount() <= 0) continue;
                    for (k = 0; k < filteredDs.getRowCount(); ++k) {
                        plId = filteredDs.getValue(k, rg_plId);
                        plVariantId = filteredDs.getValue(k, rg_plVariantId);
                        plDataSet = filteredDs.getValue(k, rg_plDataSet);
                        sdidataid = filteredDs.getValue(k, CoreColumns.SDIDATAID.getColumnId());
                        if (sdidataid.length() > 0) continue;
                        findDataSets.clear();
                        if (plId.length() > 0) {
                            findDataSets.put("paramlistid", plId);
                        }
                        if (plVariantId.length() > 0) {
                            findDataSets.put("variantid", plVariantId);
                        }
                        if (plDataSet.length() > 0) {
                            findDataSets.put("dataset", new BigDecimal(plDataSet));
                        }
                        if ((r = dsPLWorkItemWorkItemData.findRow(findDataSets)) > -1) {
                            if (plDataSet.length() == 0) {
                                filteredDs.getFilteredDataSet(findDataSets).setValue(-1, rg_plDataSet, dsPLWorkItemWorkItemData.getValue(r, "dataset"));
                            }
                            filteredDs.getFilteredDataSet(findDataSets).setValue(-1, rg_plVersionId, dsPLWorkItemWorkItemData.getValue(r, "paramlistversionid"));
                            filteredDs.getFilteredDataSet(findDataSets).setValue(-1, CoreColumns.SDIDATAID.getColumnId(), dsPLWorkItemWorkItemData.getValue(r, "sdidataid"));
                            continue;
                        }
                        token = new HashMap<String, String>();
                        token.put("workitemid", workItemId);
                        errorMsg = tp.translate("Required dataset could not be created by AddSDIWorkItem action. Specified dataset number is higher than the created one.", token);
                        if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                            dataToProcess.setString(i, "__invaliddata", "Y");
                            this.logInfo(tp.translate(errorMsg, token));
                            continue;
                        }
                        throw new SapphireException(errorMsg);
                    }
                }
            }
            if (dsAddDataSet.getRowCount() > 0) {
                filter = new HashMap<String, BigDecimal>();
                filter.put("dataset", new BigDecimal(-1));
                dsAddDataSet.getFilteredDataSet(filter).setValue(-1, "dataset", "");
                addDataSetProps = optionsForProcessing.getAddDatasetProperties();
                actionProps = new PropertyList();
                if (addDataSetProps != null) {
                    actionProps = new PropertyList(addDataSetProps);
                }
                actionProps.setProperty("sdcid", sdcId);
                actionProps.setProperty("keyid1", dsAddDataSet.getColumnValues(rg_id, ";"));
                actionProps.setProperty("paramlistid", dsAddDataSet.getColumnValues(rg_plId, ";"));
                actionProps.setProperty("paramlistversionid", dsAddDataSet.getColumnValues(rg_plVersionId, ";"));
                actionProps.setProperty("variantid", dsAddDataSet.getColumnValues(rg_plVariantId, ";"));
                actionProps.setProperty("dataset", dsAddDataSet.getColumnValues(rg_plDataSet, ";"));
                actionProps.setProperty("propsmatch", "Y");
                for (col = 0; col < columns.length; ++col) {
                    if (coreColsList.contains(columns[col]) || datasetColumnData.findRow("columnid", columns[col]) <= -1) continue;
                    actionProps.setProperty(columns[col], dsAddDataSet.getColumnValues(columns[col], ";"));
                }
                actionProps.setProperty("auditreason", auditReason);
                actionProps.setProperty("auditactivity", auditActivity);
                actionProps.setProperty("auditsignedflag", auditSignedFlag);
                actionProps.setProperty("auditdt", auditDt);
                actionProps.setProperty("tracelogid", traceLogId);
                ap.processAction("AddDataSet", "1", actionProps);
                dsInstances = new DataSet(actionProps.getProperty("newdatasetinstancexml"));
                for (i = 0; i < dsInstances.getRowCount(); ++i) {
                    findDataSets = new HashMap<String, Object>();
                    ds_sdidataid = dsInstances.getValue(i, "sdidataid");
                    findDataSets.put(rg_id, dsInstances.getValue(i, "keyid1"));
                    findDataSets.put(rg_plId, dsInstances.getValue(i, "paramlistid"));
                    findDataSets.put(rg_plVariantId, dsInstances.getValue(i, "variantid"));
                    dataset = dsInstances.getValue(i, "dataset");
                    findDataSets.put(rg_plDataSet, new BigDecimal(dataset));
                    filteredDs = dataToProcess.getFilteredDataSet(findDataSets);
                    if (filteredDs.getRowCount() > 0) {
                        filteredDs.setString(-1, CoreColumns.SDIDATAID.getColumnId(), ds_sdidataid);
                    }
                    findDataSets.remove(rg_plDataSet);
                    filteredDs = dataToProcess.getFilteredDataSet(findDataSets);
                    for (f = 0; f < filteredDs.getRowCount(); ++f) {
                        datasetNo = filteredDs.getValue(f, "dataset");
                        if (datasetNo.length() != 0) continue;
                        filteredDs.setNumber(f, rg_plDataSet, dataset);
                        filteredDs.setString(f, CoreColumns.SDIDATAID.getColumnId(), ds_sdidataid);
                    }
                }
            }
            if (dsRemeasureDataSet.getRowCount() > 0) {
                dsRemeasureActionProps = new DataSet();
                dsRemeasureDataSet.sort(rg_id + "," + rg_plId + "," + rg_plVersionId + "," + rg_plVariantId);
                plGroups = dsRemeasureDataSet.getGroupedDataSets(rg_id + "," + rg_plId + "," + rg_plVersionId + "," + rg_plVariantId);
                for (g = 0; g < plGroups.size(); ++g) {
                    ds = plGroups.get(g);
                    remeasureDs = ds.getBigDecimal(0, "__remeasuredataset");
                    maxRequiredDS = new BigDecimal(remeasureDs.intValue() + 1);
                    for (d = 0; d < ds.getRowCount(); ++d) {
                        dsNumber = ds.getValue(d, rg_plDataSet);
                        if (dsNumber.length() <= 0 || (bdDsNumber = new BigDecimal(dsNumber)).intValue() <= maxRequiredDS.intValue()) continue;
                        maxRequiredDS = bdDsNumber;
                    }
                    noOfDataSetToCreate = maxRequiredDS.intValue() - remeasureDs.intValue();
                    dsRemeasureActionProps.copyRow(ds, 0, noOfDataSetToCreate);
                }
                dsRemeasureActionProps.sort(rg_id);
                primaryGroups = dsRemeasureActionProps.getGroupedDataSets(rg_id);
                dsInstances = new DataSet();
                for (g = 0; g < primaryGroups.size(); ++g) {
                    dsGrp = primaryGroups.get(g);
                    actionProps = new PropertyList();
                    actionProps.setProperty("sdcid", sdcId);
                    actionProps.setProperty("keyid1", dsGrp.getValue(0, rg_id));
                    actionProps.setProperty("paramlistid", dsGrp.getColumnValues(rg_plId, ";"));
                    actionProps.setProperty("paramlistversionid", dsGrp.getColumnValues(rg_plVersionId, ";"));
                    actionProps.setProperty("variantid", dsGrp.getColumnValues(rg_plVariantId, ";"));
                    actionProps.setProperty("dataset", dsGrp.getColumnValues("__remeasuredataset", ";"));
                    actionProps.setProperty("auditreason", auditReason);
                    actionProps.setProperty("auditactivity", auditActivity);
                    actionProps.setProperty("auditsignedflag", auditSignedFlag);
                    actionProps.setProperty("tracelogid", traceLogId);
                    actionProps.setProperty("createmultipleinstances", "Y");
                    actionProps.setProperty("newdsstatus", "Initial");
                    ap.processAction("RemeasureDataSet", "1", actionProps);
                    instances = new DataSet(actionProps.getProperty("newdatasetinstancexml"));
                    dsInstances.copyRow(instances, -1, 1);
                }
                for (i = 0; i < dsInstances.getRowCount(); ++i) {
                    findDataSets = new HashMap<String, Object>();
                    ds_sdidataid = dsInstances.getValue(i, "sdidataid");
                    findDataSets.put(rg_id, dsInstances.getValue(i, "keyid1"));
                    findDataSets.put(rg_plId, dsInstances.getValue(i, "paramlistid"));
                    findDataSets.put(rg_plVersionId, dsInstances.getValue(i, "paramlistversionid"));
                    findDataSets.put(rg_plVariantId, dsInstances.getValue(i, "variantid"));
                    dataset = dsInstances.getValue(i, "dataset");
                    findDataSets.put(rg_plDataSet, new BigDecimal(dataset));
                    filteredDs = dataToProcess.getFilteredDataSet(findDataSets);
                    if (filteredDs.getRowCount() <= 0) continue;
                    filteredDs.setString(-1, CoreColumns.SDIDATAID.getColumnId(), ds_sdidataid);
                }
            }
            sdi.setSDCid(sdcId);
            sdi.setKeyid1List(dataToProcess.getColumnValues(rg_id, ";"));
            sdiData = sdiProcessor.getSDIData(sdi);
            dsDataItem = sdiData.getDataset("dataitem");
            dsDataSet = sdiData.getDataset("dataset");
            if (!dsDataSet.isIndexing() && dsDataSet.size() > 0) {
                if (dsDataSet.isValidColumn("keyid1")) {
                    dsDataSetIndexColumns.add("keyid1");
                }
                if (dsDataSet.isValidColumn("paramlistid")) {
                    dsDataSetIndexColumns.add("paramlistid");
                }
                if (dsDataSet.isValidColumn("paramlistversionid")) {
                    dsDataSetIndexColumns.add("paramlistversionid");
                }
                if (dsDataSet.isValidColumn("variantid")) {
                    dsDataSetIndexColumns.add("variantid");
                }
                if (dsDataSetIndexColumns.size() > 0) {
                    dsDataSet.getIndex().createIndex(dsDataSetIndexColumns);
                    dsDataSet.getIndex().setMinSizeForIndexing(0);
                }
            }
            if (!dsDataItem.isIndexing() && dsDataItem.size() > 0) {
                if (dsDataItem.isValidColumn("keyid1")) {
                    dsDataItemIndexColumns.add("keyid1");
                }
                if (dsDataItem.isValidColumn("paramlistversionid")) {
                    dsDataItemIndexColumns.add("paramlistversionid");
                }
                if (dsDataItem.isValidColumn("paramlistid")) {
                    dsDataItemIndexColumns.add("paramlistid");
                }
                if (dsDataItem.isValidColumn("variantid")) {
                    dsDataItemIndexColumns.add("variantid");
                }
                if (dsDataItem.isValidColumn("paramid")) {
                    dsDataItemIndexColumns.add("paramid");
                }
                if (dsDataItemIndexColumns.size() > 0) {
                    dsDataItem.getIndex().createIndex(dsDataItemIndexColumns);
                    dsDataItem.getIndex().setMinSizeForIndexing(0);
                }
            }
            for (i = 0; i < dataToProcess.getRowCount(); ++i) {
                block303: {
                    block304: {
                        block305: {
                            block306: {
                                block301: {
                                    block302: {
                                        primaryId = dataToProcess.getValue(i, rg_id);
                                        workitemId = dataToProcess.getValue(i, rg_plWorkItemId);
                                        if ("Y".equalsIgnoreCase(dataToProcess.getValue(i, "__invaliddata"))) continue;
                                        paramlistId = dataToProcess.getValue(i, rg_plId);
                                        paramlistVersionId = dataToProcess.getValue(i, rg_plVersionId);
                                        variantId = dataToProcess.getValue(i, rg_plVariantId);
                                        dataset = dataToProcess.getBigDecimal(i, rg_plDataSet);
                                        paramId = dataToProcess.getValue(i, rg_plParamId);
                                        paramType = dataToProcess.getValue(i, rg_plParamType);
                                        replicateId = dataToProcess.getBigDecimal(i, rg_plReplicateId);
                                        value = dataToProcess.getValue(i, rg_plValue);
                                        findDS = new HashMap<K, V>();
                                        dsFilterWithParamId = new DataSet();
                                        dataitemFound = false;
                                        paramIdFound = false;
                                        copyToEnterDataItem = true;
                                        findDS.put("keyid1", primaryId);
                                        if (paramlistId.length() > 0) {
                                            findDS.put("paramlistid", paramlistId);
                                        }
                                        if (paramlistVersionId.length() > 0 && !"C".equalsIgnoreCase(paramlistVersionId)) {
                                            findDS.put("paramlistversionid", paramlistVersionId);
                                        }
                                        if (variantId.length() > 0) {
                                            findDS.put("variantid", variantId);
                                        }
                                        if (workitemId.length() > 0) {
                                            findDS.put("sourceworkitemid", workitemId);
                                        }
                                        if (dataset != null) {
                                            findDS.put("dataset", dataset);
                                        }
                                        dsFilterDataSet = dsDataSet.getFilteredDataSet(findDS);
                                        findDS.remove("sourceworkitemid");
                                        findDS.put("paramid", paramId);
                                        if (paramType.length() > 0) {
                                            findDS.put("paramtype", paramType);
                                        }
                                        dataItems = dsDataItem.getFilteredDataSet(findDS);
                                        if (dataset == null) {
                                            for (r = 0; r < dsFilterDataSet.getRowCount(); ++r) {
                                                findDS.put("dataset", dsFilterDataSet.getBigDecimal(r, "dataset"));
                                                filteredDataItemsByDataSet = dsDataItem.getFilteredDataSet(findDS);
                                                if (filteredDataItemsByDataSet.getRowCount() == 0) {
                                                    findDS.remove("paramid");
                                                    findDS.put("instrumentfieldid", paramId);
                                                    filteredDataItemsByDataSet = dsDataItem.getFilteredDataSet(findDS);
                                                    if (filteredDataItemsByDataSet.getRowCount() == 0) {
                                                        findDS.remove("instrumentfieldid");
                                                        findDS.put("aliasid", paramId);
                                                        filteredDataItemsByDataSet = dsDataItem.getFilteredDataSet(findDS);
                                                    }
                                                    if (filteredDataItemsByDataSet.getRowCount() > 0) {
                                                        dataToProcess.setString(i, "_actualparamid", filteredDataItemsByDataSet.getValue(0, "paramid"));
                                                    }
                                                }
                                                if (filteredDataItemsByDataSet.getRowCount() <= 0) continue;
                                                dsFilterWithParamId.copyRow(filteredDataItemsByDataSet, -1, 1);
                                            }
                                            if (dsFilterWithParamId.getRowCount() > 0) {
                                                paramIdFound = true;
                                            }
                                            findDS.remove("dataset");
                                        } else {
                                            if (dataItems.getRowCount() == 0) {
                                                findDS.remove("paramid");
                                                findDS.put("instrumentfieldid", paramId);
                                                dsFilterWithParamId = dsDataItem.getFilteredDataSet(findDS);
                                                if (dsFilterWithParamId.getRowCount() == 0) {
                                                    findDS.remove("instrumentfieldid");
                                                    findDS.put("aliasid", paramId);
                                                    dsFilterWithParamId = dsDataItem.getFilteredDataSet(findDS);
                                                }
                                                if (dsFilterWithParamId.getRowCount() > 0) {
                                                    dataToProcess.setString(i, "_actualparamid", dsFilterWithParamId.getValue(0, "paramid"));
                                                }
                                            } else {
                                                dsFilterWithParamId = dataItems;
                                            }
                                            if (dsFilterWithParamId.getRowCount() > 0) {
                                                paramIdFound = true;
                                            }
                                        }
                                        token = new HashMap<String, String>();
                                        token.put("primaryid", primaryId);
                                        token.put("sdcid", sdcSingular);
                                        token.put("paramid", paramId);
                                        token.put("paramtype", paramType);
                                        token.put("replicateid", replicateId != null ? replicateId.toString() : "");
                                        token.put("paramlistid", paramlistId);
                                        token.put("paramlistversionid", paramlistVersionId);
                                        token.put("variantid", variantId);
                                        token.put("dataset", dataset != null ? dataset.toString() : "");
                                        token.put("value", value);
                                        if (!paramIdFound) break block301;
                                        if (replicateId == null) {
                                            dsFilterWithParamId.sort("dataset,replicateid");
                                            matchingDataitemCount = 0;
                                            findDI = new HashMap<String, Object>();
                                            for (r = 0; r < dsFilterWithParamId.getRowCount(); ++r) {
                                                findDI.put("sdcid", sdcId);
                                                findDI.put("keyid1", primaryId);
                                                findDI.put("paramlistid", dsFilterWithParamId.getValue(r, "paramlistid"));
                                                findDI.put("paramlistversionid", dsFilterWithParamId.getValue(r, "paramlistversionid"));
                                                findDI.put("variantid", dsFilterWithParamId.getValue(r, "variantid"));
                                                findDI.put("dataset", dsFilterWithParamId.getBigDecimal(r, "dataset"));
                                                findDI.put("paramid", dsFilterWithParamId.getValue(r, "paramid"));
                                                findDI.put("paramtype", dsFilterWithParamId.getValue(r, "paramtype"));
                                                findDI.put("replicateid", dsFilterWithParamId.getBigDecimal(r, "replicateid"));
                                                diRow = dsDataItem.findRow(findDI);
                                                if (diRow <= -1 || dsDataItem.getValue(diRow, "enteredtext").length() != 0) continue;
                                                row = dsEnterDataItem.addRow();
                                                dsEnterDataItem.setString(row, rg_id, primaryId);
                                                dsEnterDataItem.setString(row, rg_plId, dsFilterWithParamId.getValue(r, "paramlistid"));
                                                dsEnterDataItem.setString(row, rg_plVersionId, dsFilterWithParamId.getValue(r, "paramlistversionid"));
                                                dsEnterDataItem.setString(row, rg_plVariantId, dsFilterWithParamId.getValue(r, "variantid"));
                                                dsEnterDataItem.setNumber(row, rg_plDataSet, dsFilterWithParamId.getValue(r, "dataset"));
                                                dsEnterDataItem.setString(row, rg_plParamId, dsFilterWithParamId.getValue(r, "paramid"));
                                                dsEnterDataItem.setString(row, rg_plParamType, dsFilterWithParamId.getValue(r, "paramtype"));
                                                dsEnterDataItem.setNumber(row, rg_plReplicateId, dsFilterWithParamId.getValue(r, "replicateid"));
                                                dsEnterDataItem.setString(row, rg_plValue, dataToProcess.getValue(i, rg_plValue));
                                                dsEnterDataItem.setString(row, "datacaptureid", dataToProcess.getValue(i, "datacaptureid"));
                                                dsEnterDataItem.setString(row, "instrumentid", dataToProcess.getValue(i, "instrumentid"));
                                                dataToProcess.setString(i, CoreColumns.SDIDATAIIEMID.getColumnId(), dsFilterWithParamId.getValue(r, "sdidataitemid"));
                                                dataToProcess.setString(i, CoreColumns.PARAMTYPE.getColumnId(), dsFilterWithParamId.getValue(r, "paramtype"));
                                                dataToProcess.setString(i, CoreColumns.PARAMLISTVERSIONID.getColumnId(), dsFilterWithParamId.getValue(r, "paramlistversionid"));
                                                dataitemFound = true;
                                                ++matchingDataitemCount;
                                                dsDataItem.setValue(diRow, "enteredtext", dataToProcess.getValue(i, rg_plValue));
                                                break;
                                            }
                                            if (matchingDataitemCount == 1) continue;
                                            if (ResultGridOptions.AutoAddReplicate.ALWAYS == autoAddReplicate) {
                                                if ((paramlistId.length() == 0 || variantId.length() == 0 || paramType.length() == 0) && dsFilterWithParamId.getRowCount() > 1) {
                                                    dsFilterWithParamId.sort("paramlistid, variantid, paramtype");
                                                    lst = dsFilterWithParamId.getGroupedDataSets("paramlistid, variantid, paramtype");
                                                    if (lst.size() > 1) {
                                                        token.put("replicateid", replicateId != null ? replicateId.toString() : "");
                                                        errorMsg = tp.translate("Cannot proceed to add new replicate. ParamListId, VariantId and/or ParamType are not provided and there exists multiple dataitems in the [sdcid] [primaryid] for the parameter [paramid].", token);
                                                        if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                                            dataToProcess.setString(i, "__invaliddata", "Y");
                                                            this.logInfo(tp.translate(errorMsg, token));
                                                            continue;
                                                        }
                                                        throw new SapphireException(errorMsg);
                                                    }
                                                }
                                                dataToProcess.setString(i, rg_plId, dsFilterWithParamId.getValue(0, "paramlistid"));
                                                dataToProcess.setString(i, rg_plVersionId, dsFilterWithParamId.getValue(0, "paramlistversionid"));
                                                dataToProcess.setString(i, rg_plVariantId, dsFilterWithParamId.getValue(0, "variantid"));
                                                dataToProcess.setString(i, rg_plParamType, dsFilterWithParamId.getValue(0, "paramtype"));
                                                dataToProcess.setNumber(i, rg_plDataSet, dsFilterWithParamId.getValue(0, "dataset"));
                                                dsFilterWithParamId.sort("replicateid");
                                                lastRow = dsFilterWithParamId.getRowCount() - 1;
                                                maxExistingReplicate = dsFilterWithParamId.getBigDecimal(lastRow, "replicateid");
                                                newReplicateId = maxExistingReplicate.add(new BigDecimal(1));
                                                findDI.put("replicateid", newReplicateId);
                                                findDI.remove("sdcid");
                                                dataToProcessRow = dataToProcess.findRow(findDI);
                                                if (dataToProcessRow > -1) {
                                                    dataToProcess.setNumber(i, rg_plReplicateId, newReplicateId.add(new BigDecimal(1)).toString());
                                                } else {
                                                    dataToProcess.setNumber(i, rg_plReplicateId, newReplicateId.toString());
                                                }
                                                dataToProcess.setString(i, "numreplicate", "1");
                                                dataToProcess.setString(i, "enteredtext", value);
                                                dsDataItem.copyRow(dataToProcess, i, 1);
                                            } else {
                                                errorMsg = tp.translate("All existing replicates are found to be filled in with data. AutoAddReplicate is not set. So, no new replicate can be added to the [sdcid] [primaryid], Param [paramid].", token);
                                                if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                                    dataToProcess.setString(i, "__invaliddata", "Y");
                                                    this.logInfo(tp.translate(errorMsg, token));
                                                    continue;
                                                }
                                                throw new SapphireException(errorMsg);
                                            }
                                        }
                                        if (replicateId == null) break block302;
                                        findDS.put("replicateid", replicateId);
                                        dsFilteredReplicates = dsFilterWithParamId.getFilteredDataSet(findDS);
                                        v5 = dataitemFound = dsFilteredReplicates.getRowCount() > 0;
                                        if (dataitemFound) ** GOTO lbl1275
                                        if (dsFilterWithParamId.getRowCount() > 1 && (paramlistId.length() == 0 || variantId.length() == 0 || paramType.length() == 0)) {
                                            dsFilterWithParamId.sort("paramlistid, variantid, paramtype");
                                            if (dsFilterWithParamId.getGroupedDataSets("paramlistid, variantid, paramtype").size() > 1) {
                                                errorMsg = tp.translate("Matching replicate [replicateid] does not exist in the [sdcid] [primaryid] for the parameter [paramid]. Cannot proceed to add new replicate since ParamListId, VariantId and/or ParamType are not provided and there exists multiple dataitems in the [sdcid] [primaryid] for the parameter [paramid]", token);
                                                if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                                    dataToProcess.setString(i, "__invaliddata", "Y");
                                                    this.logInfo(tp.translate(errorMsg, token));
                                                    continue;
                                                }
                                                throw new SapphireException(errorMsg);
                                            }
                                        }
                                        dataToProcess.setString(i, rg_plId, dsFilterWithParamId.getValue(0, "paramlistid"));
                                        dataToProcess.setString(i, rg_plVersionId, dsFilterWithParamId.getValue(0, "paramlistversionid"));
                                        dataToProcess.setString(i, rg_plVariantId, dsFilterWithParamId.getValue(0, "variantid"));
                                        dataToProcess.setString(i, rg_plParamType, dsFilterWithParamId.getValue(0, "paramtype"));
                                        dataToProcess.setNumber(i, rg_plDataSet, dsFilterWithParamId.getValue(0, "dataset"));
                                        dsFilterWithParamId.sort("replicateid D");
                                        maxReplicate = dsFilterWithParamId.getBigDecimal(0, "replicateid");
                                        if (ResultGridOptions.AutoAddReplicate.ALWAYS == autoAddReplicate) {
                                            if (replicateId.compareTo(maxReplicate) < 0) {
                                                errorMsg = tp.translate("The required replicate number [replicateid] is not valid. It is lesser than the max replicateid found in the [sdcid] [primaryid], ParamId [paramid]", token);
                                                if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                                    dataToProcess.setString(i, "__invaliddata", "Y");
                                                    this.logInfo(tp.translate(errorMsg, token));
                                                    continue;
                                                }
                                                throw new SapphireException(errorMsg);
                                            }
                                            numReplicate = replicateId.intValue() - maxReplicate.intValue();
                                            dataToProcess.setString(i, "numreplicate", "" + numReplicate);
                                            dsDataItem.copyRow(dataToProcess, i, 1);
                                        } else {
                                            errorMsg = tp.translate("AutoAddReplicate option is not set. Missing replicate [replicateid] cannot be added to [sdcid] [primaryid], ParamId [paramid]", token);
                                            if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                                dataToProcess.setString(i, "__invaliddata", "Y");
                                                this.logInfo(tp.translate(errorMsg, token));
                                                continue;
                                            }
                                            throw new SapphireException(errorMsg);
lbl1275:
                                            // 1 sources

                                            matchingDataitemCount = 0;
                                            findDI = new HashMap<String, Object>();
                                            for (di = 0; di < dsFilteredReplicates.getRowCount(); ++di) {
                                                findDI.put("sdcid", sdcId);
                                                findDI.put("keyid1", primaryId);
                                                findDI.put("paramlistid", dsFilteredReplicates.getValue(di, "paramlistid"));
                                                findDI.put("paramlistversionid", dsFilteredReplicates.getValue(di, "paramlistversionid"));
                                                findDI.put("variantid", dsFilteredReplicates.getValue(di, "variantid"));
                                                findDI.put("dataset", dsFilteredReplicates.getBigDecimal(di, "dataset"));
                                                findDI.put("paramid", dsFilteredReplicates.getValue(di, "paramid"));
                                                findDI.put("paramtype", dsFilteredReplicates.getValue(di, "paramtype"));
                                                findDI.put("replicateid", dsFilteredReplicates.getBigDecimal(di, "replicateid"));
                                                diRow = dsDataItem.findRow(findDI);
                                                if (diRow > -1 && dsDataItem.getValue(diRow, "__currententeredtext").length() == 0) {
                                                    released = "Y".equalsIgnoreCase(dsFilteredReplicates.getValue(di, "releasedflag"));
                                                    if (released) {
                                                        msg = new StringBuffer();
                                                        msg.append(" [sdcid]: [primaryid],");
                                                        if (paramlistId.length() > 0) {
                                                            msg.append(" Parameter List: [paramlistid]");
                                                            msg.append(paramlistVersionId.length() > 0 ? "( Version: [paramlistversionid] )" : "");
                                                        }
                                                        if (variantId.length() > 0) {
                                                            msg.append(" Variant: [variantid]");
                                                        }
                                                        msg.append(" Dataset: [dataset] Parameter: [paramid] ");
                                                        if (paramType.length() > 0) {
                                                            msg.append("( [paramtype] )");
                                                        }
                                                        msg.append(" Replicate: [replicateid] is already released, hence cannot be posted with the value \"[value]\"");
                                                        if (ResultGridOptions.ReleaseHandlingRule.ERROR == releaseHandlingRule) {
                                                            throw new SapphireException(tp.translate("ReleaseHandlingRule option is set as ERROR." + msg.toString(), token));
                                                        }
                                                        if (ResultGridOptions.ReleaseHandlingRule.IGNORE == releaseHandlingRule) {
                                                            this.logInfo(tp.translate("ReleaseHandlingRule option is set as IGNORE." + msg.toString(), token));
                                                            copyToEnterDataItem = false;
                                                            continue;
                                                        }
                                                    }
                                                    row = dsEnterDataItem.addRow();
                                                    dsEnterDataItem.setString(row, rg_id, primaryId);
                                                    dsEnterDataItem.setString(row, rg_plId, dsFilteredReplicates.getValue(di, "paramlistid"));
                                                    dsEnterDataItem.setString(row, rg_plVersionId, dsFilteredReplicates.getValue(di, "paramlistversionid"));
                                                    dsEnterDataItem.setString(row, rg_plVariantId, dsFilteredReplicates.getValue(di, "variantid"));
                                                    dsEnterDataItem.setNumber(row, rg_plDataSet, dsFilteredReplicates.getValue(di, "dataset"));
                                                    dsEnterDataItem.setString(row, rg_plParamId, dsFilteredReplicates.getValue(di, "paramid"));
                                                    dsEnterDataItem.setString(row, rg_plParamType, dsFilteredReplicates.getValue(di, "paramtype"));
                                                    dsEnterDataItem.setNumber(row, rg_plReplicateId, dataToProcess.getValue(i, "replicateid"));
                                                    dsEnterDataItem.setString(row, rg_plValue, dataToProcess.getValue(i, rg_plValue));
                                                    dsEnterDataItem.setString(row, "datacaptureid", dataToProcess.getValue(i, "datacaptureid"));
                                                    dsEnterDataItem.setString(row, "instrumentid", dataToProcess.getValue(i, "instrumentid"));
                                                    dataToProcess.setString(i, CoreColumns.SDIDATAIIEMID.getColumnId(), dsFilteredReplicates.getValue(di, "sdidataitemid"));
                                                    dataToProcess.setString(i, CoreColumns.PARAMTYPE.getColumnId(), dsFilteredReplicates.getValue(di, "paramtype"));
                                                    copyToEnterDataItem = false;
                                                    if (dataset == null) {
                                                        dsDataItem.setString(diRow, "__currententeredtext", dataToProcess.getValue(i, rg_plValue));
                                                    }
                                                    ++matchingDataitemCount;
                                                    break;
                                                }
                                                if (matchingDataitemCount == 1) continue;
                                                throw new SapphireException(tp.translate("Error: Data cannot be populated, no dataitem available."));
                                            }
                                        }
                                    }
                                    if (!dataitemFound) {
                                        if (dsAddReplicate.getRowCount() > 0) {
                                            find = new HashMap<String, Object>();
                                            find.put(rg_id, primaryId);
                                            find.put(rg_plId, paramlistId);
                                            if (paramlistVersionId.length() > 0 && !"C".equalsIgnoreCase(paramlistVersionId)) {
                                                find.put(rg_plVersionId, paramlistVersionId);
                                            }
                                            find.put(rg_plVariantId, variantId);
                                            find.put(rg_plDataSet, dataset);
                                            find.put(rg_plParamId, paramId);
                                            find.put(rg_plParamType, paramType);
                                            find.put(rg_plReplicateId, dataToProcess.getBigDecimal(i, rg_plReplicateId));
                                            if (dsAddReplicate.findRow(find) > -1) continue;
                                        }
                                        dsAddReplicate.copyRow(dataToProcess, i, 1);
                                    }
                                    break block303;
                                }
                                if (ResultGridOptions.AutoAddParameter.NEVER == autoAddParameter) {
                                    if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                        dataToProcess.setString(i, "__invaliddata", "Y");
                                        this.logInfo(tp.translate("DataItem for [sdcid] [primaryid] with paramid [paramid] not found.  AutoAddParameter option not set. Hence, dataitem cannot be added.", token));
                                        continue;
                                    }
                                    throw new SapphireException(tp.translate("DataItem for [sdcid] [primaryid] with paramid [paramid] not found.  AutoAddParameter option not set. Hence, dataitem cannot be added.", token));
                                }
                                if (ResultGridOptions.AutoAddParameter.ALWAYS != autoAddParameter) break block304;
                                checkSql = "select 1 from param where paramid = ?";
                                args = new String[]{paramId};
                                errorMsg = "";
                                dsParam = qp.getPreparedSqlDataSet(checkSql, args);
                                tokenMap = new HashMap<String, String>();
                                if (dsParam.getRowCount() == 0) {
                                    tokenMap.put("paramid", paramId);
                                    errorMsg = "Parameter [paramid] does not exist.";
                                }
                                if (paramType.length() > 0 && (dsParamType = qp.getPreparedSqlDataSet(checkSql = "select 1 from refvalue where reftypeid = 'Param Type' and refvalueid = ?", args = new String[]{paramType})).getRowCount() == 0) {
                                    tokenMap.put("paramtype", paramType);
                                    errorMsg = errorMsg + " ParamType [paramtype] is not valid.";
                                }
                                if (errorMsg.length() > 0) {
                                    if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                        dataToProcess.setString(i, "__invaliddata", "Y");
                                        this.logInfo(tp.translate("Invalid data: " + errorMsg, tokenMap));
                                        continue;
                                    }
                                    throw new SapphireException(tp.translate("Invalid data: " + errorMsg, tokenMap));
                                }
                                if (replicateId != null) {
                                    numReplicate = replicateId.intValue();
                                    if (numReplicate > 1) {
                                        if (ResultGridOptions.AutoAddReplicate.ALWAYS == autoAddReplicate) {
                                            dataToProcess.setString(i, "numreplicate", "" + numReplicate);
                                        } else {
                                            if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                                dataToProcess.setString(i, "__invaliddata", "Y");
                                                this.logInfo(tp.translate("DataItem for [sdcid] [primaryid] with paramid [paramid] not found.   To add higher replicate, AutoAddReplicate must be set", token));
                                                continue;
                                            }
                                            throw new SapphireException(tp.translate("DataItem for [sdcid] [primaryid] with paramid [paramid] not found.   To add higher replicate, AutoAddReplicate must be set", token));
                                        }
                                    }
                                    dataToProcess.setString(i, "numreplicate", "" + numReplicate);
                                } else {
                                    dataToProcess.setNumber(i, rg_plReplicateId, new BigDecimal(1).toString());
                                    dataToProcess.setString(i, "numreplicate", "1");
                                }
                                if (paramType.length() == 0) {
                                    paramType = "Standard";
                                    dataToProcess.setValue(i, rg_plParamType, "Standard");
                                }
                                if (dsAddDataItem.getRowCount() <= 0) break block305;
                                find = new HashMap<String, Object>();
                                find.put(rg_id, primaryId);
                                find.put(rg_plId, paramlistId);
                                if (paramlistVersionId.length() > 0 && !"C".equalsIgnoreCase(paramlistVersionId)) {
                                    find.put(rg_plVersionId, paramlistVersionId);
                                }
                                find.put(rg_plVariantId, variantId);
                                find.put(rg_plDataSet, dataset);
                                find.put(rg_plParamId, paramId);
                                find.put(rg_plParamType, paramType);
                                itemRow1 = dsAddDataItem.findRow(find);
                                if (itemRow1 <= -1) ** GOTO lbl1487
                                find.put(rg_plReplicateId, dataToProcess.getBigDecimal(i, rg_plReplicateId));
                                itemRow2 = dsAddDataItem.findRow(find);
                                if (itemRow2 >= 0) break block306;
                                addReplicate = dsAddDataItem.getBigDecimal(itemRow1, rg_plReplicateId);
                                currentReplicate = dataToProcess.getBigDecimal(i, rg_plReplicateId);
                                if (currentReplicate.intValue() > addReplicate.intValue()) {
                                    dsAddDataItem.setString(itemRow1, "numreplicate", "" + currentReplicate.intValue());
                                }
                                break block303;
                            }
                            if (replicateId != null) ** GOTO lbl1481
                            if (ResultGridOptions.AutoAddReplicate.ALWAYS == autoAddReplicate) {
                                numReplicate = new BigDecimal(dsAddDataItem.getValue(itemRow2, "numreplicate"));
                                dsAddDataItem.setString(itemRow2, "numreplicate", numReplicate.add(new BigDecimal(1)).toString());
                                addReplicate = dsAddDataItem.getBigDecimal(itemRow2, rg_plReplicateId);
                                dataToProcess.setNumber(i, rg_plReplicateId, numReplicate.add(new BigDecimal(1)).toString());
                            } else {
                                if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                    dataToProcess.setString(i, "__invaliddata", "Y");
                                    this.logInfo(tp.translate("AutoAddReplicate option is not set. Cannot add replicate for the parameter [parameter] in the [sdcid] [primaryid]", token));
                                    continue;
                                }
                                throw new SapphireException(tp.translate("AutoAddReplicate option is not set. Cannot add replicate for the parameter [parameter] in the [sdcid] [primaryid]", token));
lbl1481:
                                // 1 sources

                                if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                                    dataToProcess.setString(i, "__invaliddata", "Y");
                                    this.logInfo(tp.translate("Invalid data! Multiple dataitems found with same replicateid for parameter [parameter] in [sdcid] [primaryid]", token));
                                    continue;
                                }
                                throw new SapphireException(tp.translate("Invalid data! Multiple dataitems found with same replicateid for parameter [parameter] in [sdcid] [primaryid]", token));
lbl1487:
                                // 1 sources

                                dsAddDataItem.copyRow(dataToProcess, i, 1);
                            }
                            break block303;
                        }
                        dsAddDataItem.copyRow(dataToProcess, i, 1);
                        break block303;
                    }
                    if (ResultGridOptions.AutoAddReplicate.ALWAYS == autoAddReplicate) {
                        msg = new StringBuffer("Replicate cannot be added as no dataitem exists with the matching [sdcid] [primaryid], ");
                        if (paramlistId.length() > 0) {
                            msg.append(" paramlistid [paramlistid], ");
                        }
                        if (variantId.length() > 0) {
                            msg.append("variantid [variantid], ");
                        }
                        if (dataset != null) {
                            msg.append(" dataset [dataset], ");
                        }
                        msg.append("ParamId [paramid]");
                        if (paramType.length() > 0) {
                            msg.append("ParamType [paramtype]");
                        }
                        if (missingDataErrorHandling == ResultGridOptions.MissingDataErrorHandling.IGNORE) {
                            dataToProcess.setString(i, "__invaliddata", "Y");
                            this.logInfo(tp.translate(msg.toString(), token));
                            continue;
                        }
                        throw new SapphireException(tp.translate(msg.toString(), token));
                    }
                }
                if (!copyToEnterDataItem) continue;
                dsEnterDataItem.copyRow(dataToProcess, i, 1);
            }
            if (dsAddDataItem.getRowCount() > 0) {
                addDataItemProps = optionsForProcessing.getAddDataItemProperties();
                actionProps = new PropertyList();
                if (addDataItemProps != null) {
                    actionProps = new PropertyList(addDataItemProps);
                }
                actionProps.setProperty("sdcid", sdcId);
                actionProps.setProperty("keyid1", dsAddDataItem.getColumnValues(rg_id, ";"));
                actionProps.setProperty("paramlistid", dsAddDataItem.getColumnValues(rg_plId, ";"));
                actionProps.setProperty("paramlistversionid", dsAddDataItem.getColumnValues(rg_plVersionId, ";"));
                actionProps.setProperty("variantid", dsAddDataItem.getColumnValues(rg_plVariantId, ";"));
                actionProps.setProperty("dataset", dsAddDataItem.getColumnValues(rg_plDataSet, ";"));
                actionProps.setProperty("paramid", dsAddDataItem.getColumnValues(rg_plParamId, ";"));
                actionProps.setProperty("paramtype", dsAddDataItem.getColumnValues(rg_plParamType, ";"));
                actionProps.setProperty("numreplicate", dsAddDataItem.getColumnValues("numreplicate", ";"));
                actionProps.setProperty("propsmatch", "Y");
                for (col = 0; col < columns.length; ++col) {
                    if (coreColsList.contains(columns[col]) || dataitemColumnData.findRow("columnid", columns[col]) <= -1) continue;
                    actionProps.setProperty(columns[col], dsAddDataItem.getColumnValues(columns[col], ";"));
                }
                actionProps.setProperty("auditreason", auditReason);
                actionProps.setProperty("auditactivity", auditActivity);
                actionProps.setProperty("auditsignedflag", auditSignedFlag);
                actionProps.setProperty("auditdt", auditDt);
                actionProps.setProperty("tracelogid", traceLogId);
                ap.processAction("ExtendDataSet", "1", actionProps);
            }
            if (dsAddReplicate.getRowCount() > 0) {
                for (rp = 0; rp < dsAddReplicate.getRowCount(); ++rp) {
                    paramId = dsAddReplicate.getValue(rp, "_actualparamid");
                    if (paramId.length() <= 0) continue;
                    dsAddReplicate.setValue(rp, "paramid", paramId);
                }
                actionProps = new PropertyList();
                actionProps.setProperty("sdcid", sdcId);
                actionProps.setProperty("keyid1", dsAddReplicate.getColumnValues(rg_id, ";"));
                actionProps.setProperty("paramlistid", dsAddReplicate.getColumnValues(rg_plId, ";"));
                actionProps.setProperty("paramlistversionid", dsAddReplicate.getColumnValues(rg_plVersionId, ";"));
                actionProps.setProperty("variantid", dsAddReplicate.getColumnValues(rg_plVariantId, ";"));
                actionProps.setProperty("dataset", dsAddReplicate.getColumnValues(rg_plDataSet, ";"));
                actionProps.setProperty("paramid", dsAddReplicate.getColumnValues(rg_plParamId, ";"));
                actionProps.setProperty("paramtype", dsAddReplicate.getColumnValues(rg_plParamType, ";"));
                actionProps.setProperty("numreplicate", dsAddReplicate.getColumnValues("numreplicate", ";"));
                actionProps.setProperty("propsmatch", "Y");
                actionProps.setProperty("auditreason", auditReason);
                actionProps.setProperty("auditactivity", auditActivity);
                actionProps.setProperty("auditsignedflag", auditSignedFlag);
                actionProps.setProperty("auditdt", auditDt);
                actionProps.setProperty("tracelogid", traceLogId);
                ap.processAction("AddReplicate", "1", actionProps);
            }
            if (dsEnterDataItem.getRowCount() > 0) {
                for (rp = 0; rp < dsEnterDataItem.getRowCount(); ++rp) {
                    paramId = dsEnterDataItem.getValue(rp, "_actualparamid");
                    if (paramId.length() > 0) {
                        dsEnterDataItem.setString(rp, "paramidalias", dsEnterDataItem.getValue(rp, "paramid"));
                        dsEnterDataItem.setValue(rp, "paramid", paramId);
                    }
                    dsEnterDataItem.setString(rp, "__sequence", "" + rp);
                }
                enterDataItemProps = optionsForProcessing.getEnterDataItemProperties();
                actionProps = new PropertyList();
                if (enterDataItemProps != null) {
                    actionProps = new PropertyList(enterDataItemProps);
                }
                autoRelease = optionsForProcessing.getAutoRelease();
                dsEnterDataItem.sort("keyid1, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid");
                groups = dsEnterDataItem.getGroupedDataSets("keyid1, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid");
                for (grp = 0; grp < groups.size(); ++grp) {
                    dsDI = groups.get(grp);
                    if (dsDI.getRowCount() <= 1) continue;
                    while (dsDI.getRowCount() > 1) {
                        di = 0;
                        actionProps.setProperty("sdcid", sdcId);
                        actionProps.setProperty("keyid1", dsDI.getValue(di, rg_id));
                        actionProps.setProperty("paramlistid", dsDI.getValue(di, rg_plId));
                        actionProps.setProperty("paramlistversionid", dsDI.getValue(di, rg_plVersionId));
                        actionProps.setProperty("variantid", dsDI.getValue(di, rg_plVariantId));
                        actionProps.setProperty("dataset", dsDI.getValue(di, rg_plDataSet));
                        actionProps.setProperty("paramid", dsDI.getValue(di, rg_plParamId));
                        actionProps.setProperty("paramtype", dsDI.getValue(di, rg_plParamType));
                        actionProps.setProperty("replicateid", dsDI.getValue(di, rg_plReplicateId));
                        actionProps.setProperty("enteredtext", dsDI.getValue(di, rg_plValue));
                        actionProps.setProperty("datacaptureid", dsDI.getValue(di, "datacaptureid"));
                        actionProps.setProperty("instrumentid", dsDI.getValue(di, "instrumentid"));
                        actionProps.setProperty("propsmatch", "Y");
                        actionProps.setProperty("auditreason", auditReason);
                        actionProps.setProperty("auditactivity", auditActivity);
                        actionProps.setProperty("auditsignedflag", auditSignedFlag);
                        actionProps.setProperty("auditdt", auditDt);
                        actionProps.setProperty("tracelogid", traceLogId);
                        if (autoRelease) {
                            actionProps.setProperty("autorelease", "Y");
                        }
                        ap.processAction("EnterDataItem", "1", actionProps);
                        __sequence = dsDI.getString(di, "__sequence");
                        enterDIRow = dsEnterDataItem.findRow("__sequence", __sequence);
                        if (enterDIRow > -1) {
                            dsEnterDataItem.remove(enterDIRow);
                        }
                        dsDI.remove(di);
                    }
                }
                actionProps.setProperty("sdcid", sdcId);
                actionProps.setProperty("keyid1", dsEnterDataItem.getColumnValues(rg_id, ";"));
                actionProps.setProperty("paramlistid", dsEnterDataItem.getColumnValues(rg_plId, ";"));
                actionProps.setProperty("paramlistversionid", dsEnterDataItem.getColumnValues(rg_plVersionId, ";"));
                actionProps.setProperty("variantid", dsEnterDataItem.getColumnValues(rg_plVariantId, ";"));
                actionProps.setProperty("dataset", dsEnterDataItem.getColumnValues(rg_plDataSet, ";"));
                actionProps.setProperty("paramid", dsEnterDataItem.getColumnValues(rg_plParamId, ";"));
                actionProps.setProperty("paramtype", dsEnterDataItem.getColumnValues(rg_plParamType, ";"));
                actionProps.setProperty("replicateid", dsEnterDataItem.getColumnValues(rg_plReplicateId, ";"));
                actionProps.setProperty("enteredtext", dsEnterDataItem.getColumnValues(rg_plValue, ";"));
                actionProps.setProperty("datacaptureid", dsEnterDataItem.getColumnValues("datacaptureid", ";"));
                actionProps.setProperty("instrumentid", dsEnterDataItem.getColumnValues("instrumentid", ";"));
                actionProps.setProperty("propsmatch", "Y");
                actionProps.setProperty("auditreason", auditReason);
                actionProps.setProperty("auditactivity", auditActivity);
                actionProps.setProperty("auditsignedflag", auditSignedFlag);
                actionProps.setProperty("auditdt", auditDt);
                actionProps.setProperty("tracelogid", traceLogId);
                if (autoRelease) {
                    actionProps.setProperty("autorelease", "Y");
                }
                ap.processAction("EnterDataItem", "1", actionProps);
                keyid1s = new StringBuffer();
                set = new HashSet<String>();
                qcBatchSet = new HashSet<String>();
                sdiworkitemSet = new HashSet<String>();
                if (!dataToProcess.isIndexing() && dataToProcess.size() > 0) {
                    dataToProcessIndexColumns = new HashSet<String>();
                    if (dataToProcess.isValidColumn("keyid1")) {
                        dataToProcessIndexColumns.add("keyid1");
                    }
                    if (dataToProcess.isValidColumn("variantid")) {
                        dataToProcessIndexColumns.add("variantid");
                    }
                    if (dataToProcess.isValidColumn("paramlistversionid")) {
                        dataToProcessIndexColumns.add("paramlistversionid");
                    }
                    if (dataToProcess.isValidColumn("paramlistid")) {
                        dataToProcessIndexColumns.add("paramlistid");
                    }
                    if (dataToProcessIndexColumns.size() > 0) {
                        dataToProcess.getIndex().createIndex(dataToProcessIndexColumns);
                        dataToProcess.getIndex().setMinSizeForIndexing(0);
                    }
                }
                for (k = 0; k < dsEnterDataItem.getRowCount(); ++k) {
                    id = dsEnterDataItem.getValue(k, rg_id);
                    set.add(id);
                    sdiwi = qp.getPreparedSqlDataSet("select 1 from sdidata where sdcid = ? and keyid1 = ? and dataset = ? and sourceworkitemid is not null", (Object[])new String[]{sdcId, id, dsEnterDataItem.getValue(k, rg_plDataSet)});
                    if (sdiwi.getRowCount() > 0) {
                        sdiworkitemSet.add(id);
                    }
                    finddataitem = new HashMap<String, Object>();
                    plId = dsEnterDataItem.getValue(k, "paramlistid");
                    plVer = dsEnterDataItem.getValue(k, "paramlistversionid");
                    plVar = dsEnterDataItem.getValue(k, "variantid");
                    dsNo = dsEnterDataItem.getBigDecimal(k, "dataset");
                    param = dsEnterDataItem.getValue(k, "paramid");
                    paramidalias = dsEnterDataItem.getValue(k, "paramidalias");
                    paramType = dsEnterDataItem.getValue(k, "paramtype");
                    replicateNo = dsEnterDataItem.getBigDecimal(k, "replicateid");
                    finddataitem.put("keyid1", id);
                    finddataitem.put("paramlistid", plId);
                    finddataitem.put("paramlistversionid", plVer);
                    finddataitem.put("variantid", plVar);
                    finddataitem.put("dataset", dsNo);
                    finddataitem.put("paramid", param);
                    finddataitem.put("paramtype", paramType);
                    finddataitem.put("replicateid", replicateNo);
                    diRow = dataToProcess.findRow(finddataitem);
                    if (diRow < 0) {
                        finddataitem.put("paramid", paramidalias);
                        diRow = dataToProcess.findRow(finddataitem);
                    }
                    if (diRow <= -1 || (sdidataitemid = dataToProcess.getValue(diRow, "sdidataitemid")).length() != 0 || (sdidataitem = qp.getPreparedSqlDataSet("select sdidataitemid from sdidataitem where sdcid = ? and keyid1 = ? and  paramlistid =? and paramlistversionid=? and variantid=? and dataset=? and paramid=? and paramtype=? and replicateid=?", (Object[])new String[]{sdcId, id, plId, plVer, plVar, dsEnterDataItem.getValue(k, "dataset"), param, paramType, dsEnterDataItem.getValue(k, "replicateid")})).getRowCount() <= 0) continue;
                    dataToProcess.setString(diRow, "sdidataitemid", sdidataitem.getValue(0, "sdidataitemid"));
                }
                for (String keyId1 : set) {
                    keyid1s.append(";").append(keyId1);
                    qcBatch = qp.getPreparedSqlDataSet("select distinct s_qcbatchid from sdidata where sdcid = ? and keyid1 = ? and s_qcbatchid is not null and s_qcbatchitemid is not null", (Object[])new String[]{sdcId, keyId1});
                    if (qcBatch.getRowCount() <= 0) continue;
                    for (qc = 0; qc < qcBatch.getRowCount(); ++qc) {
                        qcBatchId = qcBatch.getValue(qc, "s_qcbatchid");
                        if (qcBatchId.length() <= 0) continue;
                        qcBatchSet.add(qcBatchId);
                    }
                }
                actionProps.clear();
                actionProps.setProperty("sdcid", sdcId);
                actionProps.setProperty("keyid1", keyid1s.substring(1));
                actionProps.setProperty("auditreason", auditReason);
                actionProps.setProperty("auditsignedflag", auditSignedFlag);
                actionProps.setProperty("auditactivity", auditActivity);
                actionProps.setProperty("tracelogid", traceLogId);
                ap.processAction("UpdateDatasetStatus", "1", actionProps);
                if (sdiworkitemSet.size() > 0) {
                    sdiwiSetItr = sdiworkitemSet.iterator();
                    ids = new StringBuffer();
                    while (sdiwiSetItr.hasNext()) {
                        id = (String)sdiwiSetItr.next();
                        ids.append(";").append(id);
                    }
                    actionProps.clear();
                    actionProps.setProperty("sdcid", sdcId);
                    actionProps.setProperty("keyid1", ids.substring(1));
                    actionProps.setProperty("auditreason", auditReason);
                    actionProps.setProperty("auditsignedflag", auditSignedFlag);
                    actionProps.setProperty("auditactivity", auditActivity);
                    actionProps.setProperty("tracelogid", traceLogId);
                    ap.processAction("SyncSDIWIStatus", "1", actionProps);
                }
                if ("Sample".equalsIgnoreCase(sdcId)) {
                    actionProps.clear();
                    actionProps.setProperty("sdcid", sdcId);
                    actionProps.setProperty("keyid1", keyid1s.substring(1));
                    actionProps.setProperty("auditreason", auditReason);
                    actionProps.setProperty("auditsignedflag", auditSignedFlag);
                    actionProps.setProperty("auditactivity", auditActivity);
                    actionProps.setProperty("tracelogid", traceLogId);
                    ap.processAction("SyncSDIDataSetStatus", "1", actionProps);
                }
                if (qcBatchSet.size() > 0) {
                    batchItr = qcBatchSet.iterator();
                    qcBatchIds = new StringBuffer();
                    while (batchItr.hasNext()) {
                        batch_id = (String)batchItr.next();
                        qcBatchIds.append(";").append(batch_id);
                    }
                    actionProps.clear();
                    actionProps.setProperty("sdcid", "QCBatch");
                    actionProps.setProperty("keyid1", qcBatchIds.substring(1));
                    actionProps.setProperty("auditreason", auditReason);
                    actionProps.setProperty("auditactivity", auditActivity);
                    actionProps.setProperty("auditsignedflag", auditSignedFlag);
                    actionProps.setProperty("tracelogid", traceLogId);
                    ap.processAction("UpdateQCBatchStatus", "1", actionProps);
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            if (rsetId != null && rsetId.length() > 0 && damProcessor != null) {
                damProcessor.clearRSet(rsetId);
                if (lockrsetId != null && lockrsetId.length() > 0) {
                    damProcessor.clearRSet(lockrsetId);
                }
            }
        }
    }

    public String getValue(int row, CoreColumns column, String defaultValue) {
        return this.grid.getValue(row, column.getColumnId().toLowerCase(), defaultValue);
    }

    public String getValue(int row, CoreColumns column) {
        return this.grid.getValue(row, column.getColumnId().toLowerCase());
    }

    public String getColumnValues(CoreColumns column, String delimeter) {
        return this.grid.getColumnValues(column.getColumnId().toLowerCase(), delimeter);
    }

    public int getRowCount() {
        return this.getDataSet().getRowCount();
    }

    public int addRow() {
        return this.grid.addRow();
    }

    public void setValue(int row, CoreColumns column, String value) {
        this.grid.setValue(row, column.getColumnId().toLowerCase(), value);
    }

    public void setString(int row, String column, String value) {
        this.grid.setString(row, column, value);
    }

    private String getCurrentPLVersion(String plId, String variant, QueryProcessor qp, TranslationProcessor tp) throws SapphireException {
        String sql = "SELECT paramlistversionid FROM paramlist WHERE paramlistid = ? and variantid = ? AND ( versionstatus = 'P' or versionstatus = 'C' ) order by versionstatus, cast (paramlistversionid as numeric) desc";
        DataSet ds = qp.getPreparedSqlDataSet(sql, (Object[])new String[]{plId, variant});
        if (ds.getRowCount() > 0) {
            return ds.getValue(0, "paramlistversionid");
        }
        HashMap<String, String> token = new HashMap<String, String>();
        token.put("paramlistid", plId);
        token.put("variant", variant);
        throw new SapphireException(tp.translate("Failed to get the current version of ParamList [paramlistid], variant [variant]", token));
    }

    private int getFirstAvailableDataSet(DataSet dsFilterWithPLVariant) {
        dsFilterWithPLVariant.sort("dataset");
        for (int d = 0; d < dsFilterWithPLVariant.getRowCount(); ++d) {
            String datasetStatus = dsFilterWithPLVariant.getValue(d, "s_datasetstatus");
            if ("Completed".equalsIgnoreCase(datasetStatus) || "Cancelled".equalsIgnoreCase(datasetStatus) || "Released".equalsIgnoreCase(datasetStatus)) continue;
            return d;
        }
        return -1;
    }

    public static enum CoreColumns {
        ID("id", 0),
        SDCID("sdcid", 0),
        KEYID1("keyid1", 0),
        KEYID2("keyid2", 0),
        KEYID3("keyid3", 0),
        WORKITEMID("workitemid", 0),
        PARAMLISTID("paramlistid", 0),
        PARAMLISTVERSIONID("paramlistversionid", 0),
        VARIANTID("variantid", 0),
        DATASET("dataset", 1),
        PARAMID("paramid", 0),
        PARAMTYPE("paramtype", 0),
        REPLICATEID("replicateid", 1),
        VALUE("value", 0),
        ENTEREDTEXT("enteredtext", 0),
        NEWKEYID1("newkeyid1", 0),
        SDIDATAID("sdidataid", 0),
        SDIDATAIIEMID("sdidataitemid", 0);

        private String columnid = "";
        private int type = -1;

        private CoreColumns(String columnid, int type) {
            this.columnid = columnid;
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public String getColumnId() {
            return this.columnid;
        }
    }
}

