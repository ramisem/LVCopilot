/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostDataReleaseEventObject;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.StringHolder;
import com.labvantage.sapphire.util.WorkItemItemRuleEvaluator;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public abstract class BaseReleaseUnreleaseData
extends BaseAction {
    public static final String ALLOWMANDATORYNULLS = "allowmandatorynulls";

    protected void releaseData(PropertyList properties, String releaseFlag, boolean dataitems) throws SapphireException {
        int rc = 1;
        StringHolder rsetidHolder = new StringHolder();
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.indexOf(";") > 0) {
            sdcid = sdcid.substring(0, sdcid.indexOf(";") - 1);
        }
        PropertyList sdcProps = this.getSDCProcessor().getPropertyList(sdcid);
        sdcid = sdcProps.getProperty("sdcid");
        DataSet propds = new DataSet();
        propds.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), ";");
        propds.addColumnValues("keyid2", 0, properties.getProperty("keyid2"), ";", "(null)");
        propds.addColumnValues("keyid3", 0, properties.getProperty("keyid3"), ";", "(null)");
        propds.addColumnValues("paramlistid", 0, properties.getProperty("paramlistid"), ";");
        propds.addColumnValues("paramlistversionid", 0, properties.getProperty("paramlistversionid"), ";", "1");
        propds.addColumnValues("variantid", 0, properties.getProperty("variantid"), ";");
        propds.addColumnValues("dataset", 1, properties.getProperty("dataset"), ";", "1");
        if (dataitems) {
            propds.addColumnValues("paramid", 0, properties.getProperty("paramid"), ";");
            propds.addColumnValues("paramtype", 0, properties.getProperty("paramtype"), ";", "Standard");
            propds.addColumnValues("replicateid", 1, properties.getProperty("replicateid"), ";", "1");
        }
        propds.padColumns();
        StringBuffer keyid1list = new StringBuffer();
        StringBuffer keyid2list = new StringBuffer();
        StringBuffer keyid3list = new StringBuffer();
        int proprows = propds.getRowCount();
        propds.sort("sdcid,keyid1,keyid2,keyid3");
        HashSet<String> tobeReleasedKeysSet = new HashSet<String>();
        String initKeyid1 = "";
        String initKeyid2 = "";
        String initKeyid3 = "";
        String dataitemkey = "";
        for (int i = 0; i < proprows; ++i) {
            String keyid1 = propds.getString(i, "keyid1");
            String keyid2 = propds.getString(i, "keyid2");
            String keyid3 = propds.getString(i, "keyid3");
            if (!(keyid1.equals(initKeyid1) && keyid2.equals(initKeyid2) && keyid3.equals(initKeyid3))) {
                keyid1list.append(";").append(keyid1);
                keyid2list.append(";").append(keyid2);
                keyid3list.append(";").append(keyid3);
                initKeyid1 = keyid1;
                initKeyid2 = keyid2;
                initKeyid3 = keyid3;
            }
            dataitemkey = sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + propds.getString(i, "paramlistid") + ";" + propds.getString(i, "paramlistversionid") + ";" + propds.getString(i, "variantid") + ";" + propds.getValue(i, "dataset");
            if (dataitems) {
                dataitemkey = dataitemkey + ";" + propds.getString(i, "paramid") + ";" + propds.getString(i, "paramtype") + ";" + propds.getValue(i, "replicateid");
            }
            tobeReleasedKeysSet.add(dataitemkey);
        }
        boolean applylock = properties.getProperty("applylock").equals("Y");
        DAMProcessor damProcessor = this.getDAMProcessor();
        rc = damProcessor.createRSetDS(sdcid, keyid1list.toString(), keyid2list.toString(), keyid3list.toString(), "", "", "", "", false, true, false, rsetidHolder);
        if (rc == 2) {
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create Rset");
        }
        if (applylock && (rc = damProcessor.lockRSet(rsetidHolder)) == 2) {
            throw new SapphireException("CREATE_LOCK_FAILURE", "Failed to create lock rset");
        }
        String rsetid = rsetidHolder.value;
        boolean mandatorynulls = properties.getProperty(ALLOWMANDATORYNULLS).equals("Y");
        DataSet updateds = null;
        boolean allReleased = true;
        boolean allMandatoryReleased = true;
        if (dataitems) {
            updateds = propds;
            updateds.addColumn("releasedflag", 0);
            updateds.addColumn("sdcid", 0);
            updateds.setString(-1, "releasedflag", releaseFlag);
            updateds.setString(-1, "sdcid", sdcid);
        } else {
            String selectdi = "SELECT\tsdidataitem.* FROM\tsdidataitem, rsetitems WHERE\tsdidataitem.sdcid = rsetitems.sdcid AND \t\tsdidataitem.keyid1 = rsetitems.keyid1 AND \t\tsdidataitem.keyid2 = rsetitems.keyid2 AND \t\tsdidataitem.keyid3 = rsetitems.keyid3 AND \t\trsetid = ?";
            DataSet sdidataitems = null;
            try {
                this.database.createPreparedResultSet("sdidataitems", selectdi, new Object[]{rsetid});
                sdidataitems = new DataSet(this.database.getResultSet("sdidataitems"));
            }
            catch (SapphireException e) {
                damProcessor.clearRSet(rsetidHolder.value);
                throw new SapphireException("CREATE_RESULTSET_FAILED", "Failed to create resultset for dataitems or dataitemlimits", e);
            }
            sdidataitems.addColumn("_modified", 0);
            HashMap findmap = new HashMap();
            sdidataitems.setString(-1, "_modified", "N");
            int sdidataitemRowCnt = sdidataitems.getRowCount();
            for (int row = 0; row < sdidataitemRowCnt; ++row) {
                dataitemkey = sdidataitems.getValue(row, "sdcid") + ";" + sdidataitems.getValue(row, "keyid1") + ";" + sdidataitems.getValue(row, "keyid2") + ";" + sdidataitems.getValue(row, "keyid3") + ";" + sdidataitems.getValue(row, "paramlistid") + ";" + sdidataitems.getValue(row, "paramlistversionid") + ";" + sdidataitems.getValue(row, "variantid") + ";" + sdidataitems.getValue(row, "dataset");
                if (dataitems) {
                    dataitemkey = dataitemkey + ";" + sdidataitems.getValue(row, "paramid") + ";" + sdidataitems.getValue(row, "paramtype") + ";" + sdidataitems.getValue(row, "replicateid");
                }
                if (!tobeReleasedKeysSet.contains(dataitemkey)) continue;
                sdidataitems.setString(row, "_modified", "Y");
                sdidataitems.setString(row, "releasedflag", releaseFlag);
                if (mandatorynulls || !releaseFlag.equals("Y") || !sdidataitems.getValue(row, "mandatoryflag").equals("Y") || StringUtil.getLen(sdidataitems.getValue(row, "enteredtext")) != 0L) continue;
                damProcessor.clearRSet(rsetidHolder.value);
                throw new SapphireException("NULL_MANDATORY", "Null result found for mandatory value for " + sdidataitems.getString(row, "keyid1") + " for " + sdidataitems.getString(row, "paramid"));
            }
            if (releaseFlag.equals("Y")) {
                for (int i = 0; (allReleased || allMandatoryReleased) && i < sdidataitems.size(); ++i) {
                    if (sdidataitems.getValue(i, "releasedflag").equals("Y")) continue;
                    allReleased = false;
                    if (!sdidataitems.getValue(i, "mandatoryflag").equals("Y")) continue;
                    allMandatoryReleased = false;
                }
            }
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("_modified", "Y");
            updateds = sdidataitems.getFilteredDataSet(filter);
        }
        this.logger.info("Saving changes to sdidataitems");
        if (properties.getProperty("tracelogid", "").trim().length() == 0) {
            String traceLogId = this.getTracelogid(sdcid, "Item(s) " + (releaseFlag.equals("Y") ? "Released" : "Unreleased"), properties.getProperty("auditreason"), properties.getProperty("auditactivity", ""), properties.getProperty("auditsignedflag", "N"), properties.getProperty("auditdt"));
            updateds.setString(-1, "tracelogid", traceLogId);
            properties.setProperty("tracelogid", traceLogId);
        } else {
            updateds.setString(-1, "tracelogid", properties.getProperty("tracelogid"));
        }
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdcProps, "PreDataRelease");
        SDIData sdiData = new SDIData(sdcid);
        SDIData beforeEditImage = null;
        boolean requiresDataReleasePrimary = sdcPreRules.requiresDataReleasePrimary() || sdcPreRules.customRulesRequiresDataReleasePrimary();
        boolean requiresBeforeDataReleaseImage = sdcPreRules.requiresBeforeDataReleaseImage() || sdcPreRules.customRulesRequiresBeforeDataReleaseImage();
        PostDataReleaseEventObject postDataReleaseEventObject = dataitems ? new PostDataReleaseEventObject(sdcid, null, sdiData, properties, releaseFlag, false, false) : new PostDataReleaseEventObject(sdcid, null, sdiData, properties, releaseFlag, allReleased, allMandatoryReleased);
        boolean requiresSupplementalData = EventManager.requiresSupplementalData(sapphireConnection, this.getErrorHandler(), postDataReleaseEventObject);
        boolean sdiDataApprovalRollback = StringUtil.getYN(releaseFlag, "Y").equals("N");
        if (sdiDataApprovalRollback) {
            PropertyList policy = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom");
            sdiDataApprovalRollback = "Y".equals(policy.getProperty("resetapproval", "N"));
        }
        boolean bl = requiresBeforeDataReleaseImage = requiresBeforeDataReleaseImage || "Y".equalsIgnoreCase(releaseFlag) && dataitems;
        if (requiresDataReleasePrimary || requiresBeforeDataReleaseImage || sdiDataApprovalRollback || requiresSupplementalData) {
            BaseSDCRules[] sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(sdcid);
            sdiRequest.setRsetid(rsetid);
            sdiRequest.setRetainRsetid(true);
            if (requiresDataReleasePrimary) {
                sdiRequest.setRequestItem("primary");
            }
            if (requiresBeforeDataReleaseImage) {
                sdiRequest.setRequestItem("dataset");
            }
            if (requiresSupplementalData) {
                postDataReleaseEventObject.addRequestItems((SDIRequest)sdiRequest);
            }
            if (requiresBeforeDataReleaseImage || sdiDataApprovalRollback) {
                if (!"Y".equals(properties.getProperty("calculatemodifiedtestsonly")) && !"Y".equals(properties.getProperty("calculatemodifieddatasetsonly"))) {
                    sdiRequest.setRequestItem("dataitem");
                }
                sdiRequest.setRequestItem("dataapproval");
            }
            SDIProcessor sdiProcessor = this.getSDIProcessor();
            beforeEditImage = sdiProcessor.getSDIData((SDIRequest)sdiRequest);
            sdcPreRules.setBeforeEditImage(beforeEditImage);
        }
        Trace.startBusinessRule(sdcid + "." + "PreDataRelease", true);
        sdcPreRules.preReleaseData(updateds, properties);
        Trace.endBusinessRule(sdcid + "." + "PreDataRelease", true);
        Trace.startBusinessRule(sdcid + "." + "PreDataRelease", false);
        for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
            customRules.preReleaseData(updateds, properties);
        }
        Trace.endBusinessRule(sdcid + "." + "PreDataRelease", false);
        sdcPreRules.endRule();
        DataSetUtil.update(this.database, updateds, "sdidataitem", new SDIData().getKeys("dataitem"));
        if (sdiDataApprovalRollback) {
            BaseSDIDataAction.dataApprovalRollback(beforeEditImage.getDataset("dataitem"), beforeEditImage.getDataset("dataapproval"), updateds, this.getActionProcessor());
        }
        boolean[] releasedAndManadatoryReleased = new boolean[]{true, true};
        if (dataitems) {
            this.database.createPreparedResultSet("SELECT sdidataitem.sdcid, sdidataitem.keyid1, sdidataitem.keyid2, sdidataitem.keyid3, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, releasedflag, mandatoryflag, enteredtext FROM sdidataitem, rsetitems WHERE sdidataitem.sdcid = rsetitems.sdcid AND sdidataitem.keyid1 = rsetitems.keyid1 AND sdidataitem.keyid2 = rsetitems.keyid2 AND sdidataitem.keyid3 = rsetitems.keyid3 AND rsetid = ?", new Object[]{rsetid});
            while (this.database.getNext()) {
                dataitemkey = this.database.getString("sdcid") + ";" + this.database.getString("keyid1") + ";" + this.database.getString("keyid2") + ";" + this.database.getString("keyid3") + ";" + this.database.getString("paramlistid") + ";" + this.database.getString("paramlistversionid") + ";" + this.database.getString("variantid") + ";" + this.database.getInt("dataset");
                if (dataitems) {
                    dataitemkey = dataitemkey + ";" + this.database.getString("paramid") + ";" + this.database.getString("paramtype") + ";" + this.database.getInt("replicateid");
                }
                if (tobeReleasedKeysSet.contains(dataitemkey) && !mandatorynulls && releaseFlag.equals("Y") && this.database.getString("mandatoryflag").equals("Y") && StringUtil.getLen(this.database.getString("enteredtext")) == 0L) {
                    damProcessor.clearRSet(rsetidHolder.value);
                    throw new SapphireException("NULL_MANDATORY", "Null result found for mandatory value for " + this.database.getString("keyid1") + " for " + this.database.getString("paramid"));
                }
                if (!releasedAndManadatoryReleased[0] && !releasedAndManadatoryReleased[1] || this.database.getValue("releasedflag").equals("Y")) continue;
                releasedAndManadatoryReleased[0] = false;
                if (!this.database.getValue("mandatoryflag").equals("Y")) continue;
                releasedAndManadatoryReleased[1] = false;
            }
        }
        if (requiresSupplementalData) {
            if (dataitems) {
                postDataReleaseEventObject.setAllDataItemsReleased(releasedAndManadatoryReleased[0]);
                postDataReleaseEventObject.setAllMandatoryDataItemsReleased(releasedAndManadatoryReleased[1]);
            }
            sdiData.setDataset("primary", beforeEditImage.getDataset("primary"));
            sdiData.setDataset("dataitem", updateds);
            sdiData.setDataset("sdispec", beforeEditImage.getDataset("sdispec"));
            sdiData.setDataset("dataspec", beforeEditImage.getDataset("dataspec"));
            EventManager.generateEvent(sapphireConnection, this.getErrorHandler(), postDataReleaseEventObject);
        }
        BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdcProps, "PostAddDetail");
        sdcPostRules.setBeforeEditImage(beforeEditImage);
        if ("Y".equalsIgnoreCase(releaseFlag) && dataitems && beforeEditImage != null) {
            if (sdiData.getDataset("dataitem") == null) {
                sdiData.setDataset("dataitem", updateds);
            }
            sdiData.setDataset("dataapproval", beforeEditImage.getDataset("dataapproval"));
            WorkItemItemRuleEvaluator ruleProcessor = new WorkItemItemRuleEvaluator();
            ruleProcessor.evaluateRuleOnDataItemRelease(this.getSDCProcessor(), sdcid, beforeEditImage, sdiData, this.database, this.connectionInfo);
        }
        Trace.startBusinessRule(sdcid + "." + "PostDataRelease", true);
        sdcPostRules.postReleaseData(updateds, properties);
        Trace.endBusinessRule(sdcid + "." + "PostDataRelease", true);
        Trace.startBusinessRule(sdcid + "." + "PostDataRelease", false);
        for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
            customRules.postReleaseData(updateds, properties);
        }
        Trace.endBusinessRule(sdcid + "." + "PostDataRelease", false);
        sdcPostRules.endRule();
        damProcessor.clearRSet(rsetidHolder.value);
    }

    protected String getTracelogid(String sdcid, String desc, String auditReason, String auditActivity, String auditSignedFlag, String auditDt) throws SapphireException {
        String tracelogid = null;
        if (auditReason.length() > 0) {
            if (Trace.on) {
                this.logger.info("Generate the tracelog record");
            }
            PropertyList tracelogprops = new PropertyList();
            tracelogprops.setProperty("sdcid", sdcid);
            tracelogprops.setProperty("description", desc);
            tracelogprops.setProperty("auditreason", auditReason);
            tracelogprops.setProperty("auditactivity", auditActivity);
            tracelogprops.setProperty("auditsignedflag", auditSignedFlag);
            tracelogprops.setProperty("auditdt", auditDt);
            ActionProcessor ap = this.getActionProcessor();
            ap.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
            tracelogid = (String)tracelogprops.get("tracelogid");
        }
        return tracelogid;
    }
}

