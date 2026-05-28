/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataEntryAction;
import com.labvantage.sapphire.modules.sdms.util.ResultDataGrid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.ResultDataGrid;
import sapphire.util.ResultGridOptions;
import sapphire.xml.PropertyList;

public class EnterResultDataGrid
extends BaseSDIDataEntryAction
implements sapphire.action.EnterResultDataGrid {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.indexOf(";") > 0) {
            sdcid = sdcid.substring(0, sdcid.indexOf(";"));
        }
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        sdcid = sdcProcessor.getProperty(sdcid, "sdcid");
        ResultGridOptions resultGridOptions = new ResultGridOptions();
        resultGridOptions.setSdcId(sdcid);
        resultGridOptions.setAutoAddSDI("Y".equalsIgnoreCase(properties.getProperty("autoaddsdi")) ? ResultGridOptions.AutoAddSDI.ALWAYS : ResultGridOptions.AutoAddSDI.NEVER);
        resultGridOptions.setAutoAddWorkItem("Y".equalsIgnoreCase(properties.getProperty("autoaddsdiworkitem")) ? ResultGridOptions.AutoAddWorkItem.ALWAYS : ResultGridOptions.AutoAddWorkItem.NEVER);
        resultGridOptions.setAutoAddDataset("Y".equalsIgnoreCase(properties.getProperty("autoadddataset")) ? ResultGridOptions.AutoAddDataSet.ALWAYS : ("R".equalsIgnoreCase(properties.getProperty("autoadddataset")) ? ResultGridOptions.AutoAddDataSet.REMEASURE : ResultGridOptions.AutoAddDataSet.NEVER));
        resultGridOptions.setAutoAddReplicate("Y".equalsIgnoreCase(properties.getProperty("autoaddreplicate")) ? ResultGridOptions.AutoAddReplicate.ALWAYS : ResultGridOptions.AutoAddReplicate.NEVER);
        resultGridOptions.setAutoAddParameter("Y".equalsIgnoreCase(properties.getProperty("autoaddparameter")) ? ResultGridOptions.AutoAddParameter.ALWAYS : ResultGridOptions.AutoAddParameter.NEVER);
        resultGridOptions.setReleaseHandlingRule("O".equalsIgnoreCase(properties.getProperty("releasehandlingrule")) ? ResultGridOptions.ReleaseHandlingRule.OVERRIDE : ("I".equalsIgnoreCase(properties.getProperty("releasehandlingrule")) ? ResultGridOptions.ReleaseHandlingRule.IGNORE : ResultGridOptions.ReleaseHandlingRule.ERROR));
        resultGridOptions.setMissingDataErrorHandling("I".equalsIgnoreCase(properties.getProperty("missingdataerrorhandling")) ? ResultGridOptions.MissingDataErrorHandling.IGNORE : ResultGridOptions.MissingDataErrorHandling.ERROR);
        resultGridOptions.setAutoRelease("Y".equalsIgnoreCase(properties.getProperty("autorelease")));
        resultGridOptions.setDefaultDataSet("one".equalsIgnoreCase(properties.getProperty("defaultdataset")) ? ResultGridOptions.DefaultDataSet.DATASET_ONE : ResultGridOptions.DefaultDataSet.FIRST_AVAILABLE);
        resultGridOptions.setDefaultReplicateId("one".equalsIgnoreCase(properties.getProperty("defaultreplicate")) ? ResultGridOptions.DefaultReplicateId.REPLICATE_ONE : ResultGridOptions.DefaultReplicateId.FIRST_AVAILABLE);
        resultGridOptions.setAuditReason(properties.getProperty("auditreason"));
        resultGridOptions.setAuditActivity(properties.getProperty("auditactivity"));
        resultGridOptions.setAuditSignedFlag(properties.getProperty("auditsignedflag"));
        resultGridOptions.setAuditDt(properties.getProperty("auditdt"));
        resultGridOptions.setTraceLogId(properties.getProperty("tracelogid"));
        resultGridOptions.setApplyLock("Y".equals(properties.getProperty("applylock", "Y")));
        List coreColsList = resultGridOptions.getCoreColsList();
        ArrayList<String> additionalColumns = new ArrayList<String>();
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
        propds.addColumnValues("enteredtext", 0, properties.getProperty("enteredtext"), ";");
        propds.addColumnValues("enteredtext", 0, properties.getProperty("enteredtext"), ";");
        propds.addColumnValues("workitemid", 0, properties.getProperty("workitemid"), ";");
        for (Object o : properties.keySet()) {
            String key = (String)o;
            if (coreColsList.contains(key)) continue;
            additionalColumns.add(key);
            propds.addColumnValues(key, 0, properties.getProperty(key), ";");
        }
        ResultDataGrid resultGrid = new ResultDataGrid(this.connectionInfo);
        HashMap<String, String> resultAdditionalColumns = new HashMap<String, String>();
        HashMap<ResultDataGrid.CoreColumns, String> resultCoreCols = new HashMap<ResultDataGrid.CoreColumns, String>();
        for (int i = 0; i < propds.getRowCount(); ++i) {
            resultCoreCols.put(ResultDataGrid.CoreColumns.KEYID1, propds.getValue(i, "keyid1"));
            resultCoreCols.put(ResultDataGrid.CoreColumns.KEYID2, propds.getValue(i, "keyid2"));
            resultCoreCols.put(ResultDataGrid.CoreColumns.KEYID3, propds.getValue(i, "keyid3"));
            resultCoreCols.put(ResultDataGrid.CoreColumns.WORKITEMID, propds.getValue(i, "workitemid"));
            resultCoreCols.put(ResultDataGrid.CoreColumns.PARAMLISTID, propds.getValue(i, "paramlistid"));
            resultCoreCols.put(ResultDataGrid.CoreColumns.PARAMLISTVERSIONID, propds.getValue(i, "paramlistversionid"));
            resultCoreCols.put(ResultDataGrid.CoreColumns.VARIANTID, propds.getValue(i, "variantid"));
            resultCoreCols.put(ResultDataGrid.CoreColumns.DATASET, propds.getValue(i, "dataset"));
            resultCoreCols.put(ResultDataGrid.CoreColumns.PARAMID, propds.getValue(i, "paramid"));
            resultCoreCols.put(ResultDataGrid.CoreColumns.PARAMTYPE, propds.getValue(i, "paramtype"));
            resultCoreCols.put(ResultDataGrid.CoreColumns.REPLICATEID, propds.getValue(i, "replicateid"));
            resultCoreCols.put(ResultDataGrid.CoreColumns.VALUE, propds.getValue(i, "enteredtext"));
            for (int col = 0; col < additionalColumns.size(); ++col) {
                String columnId = (String)additionalColumns.get(col);
                resultAdditionalColumns.put(columnId, propds.getValue(i, columnId));
            }
            resultGrid.addResult(resultCoreCols, resultAdditionalColumns);
            resultCoreCols.clear();
            resultAdditionalColumns.clear();
        }
        if (resultGrid.getRowCount() > 0) {
            resultGrid.save(resultGridOptions);
        }
    }
}

