/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.simplespec.action;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.simplespec.action.AutomaticVersioning;
import com.labvantage.sapphire.pageelements.simplespec.action.GetNewSPId;
import com.labvantage.sapphire.pageelements.simplespec.util.SimpleSpecHelper;
import java.math.BigDecimal;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SamplingPlanHelper
extends BaseCustom {
    public SamplingPlanHelper(String connectionId) throws SapphireException {
        if (connectionId == null) {
            throw new SapphireException("Connection ID is null");
        }
        if (connectionId.isEmpty()) {
            throw new SapphireException("Connection ID is empty");
        }
        this.setConnectionId(connectionId);
    }

    public DataSet getSamplingPlan(String sdcId, String keyId1, String keyId2, String keyId3, String samplingPlanIdColumn, String samplingPlanVersionIdColumn) throws SapphireException {
        if (sdcId == null) {
            throw new SapphireException("SDC ID is null");
        }
        if (keyId1 == null) {
            throw new SapphireException("Key ID 1 is null");
        }
        if (keyId2 == null) {
            throw new SapphireException("Key ID 2 is null");
        }
        if (keyId3 == null) {
            throw new SapphireException("Key ID 3 is null");
        }
        if (samplingPlanIdColumn == null) {
            throw new SapphireException("Sampling plan ID column is null");
        }
        if (samplingPlanVersionIdColumn == null) {
            throw new SapphireException("Sampling plan ID column is null");
        }
        PropertyList primaryProps = new PropertyList(this.getSDCProcessor().getSDCProperties(sdcId));
        String primaryTableId = primaryProps.getProperty("tableid");
        String primaryKeyColId1 = primaryProps.getProperty("keycolid1");
        String primaryKeyColId2 = primaryProps.getProperty("keycolid2");
        String primaryKeyColId3 = primaryProps.getProperty("keycolid3");
        ArrayList<String> getSamplingPlanParams = new ArrayList<String>();
        getSamplingPlanParams.add(keyId1);
        if (!primaryKeyColId2.isEmpty()) {
            getSamplingPlanParams.add(keyId2);
        }
        if (!primaryKeyColId3.isEmpty()) {
            getSamplingPlanParams.add(keyId3);
        }
        String getSamplingPlanSql = "SELECT sp.s_samplingplanid, sp.s_samplingplanversionid FROM " + primaryTableId + " p JOIN s_samplingplan sp ON p." + samplingPlanIdColumn + " = sp.s_samplingplanid AND p." + samplingPlanVersionIdColumn + " = sp.s_samplingplanversionid WHERE p." + primaryKeyColId1 + " = ?" + (!primaryKeyColId2.isEmpty() ? " AND p." + primaryKeyColId2 + " = ?" : "") + (!primaryKeyColId3.isEmpty() ? " AND p." + primaryKeyColId3 + " = ?" : "");
        return this.getQueryProcessor().getPreparedSqlDataSet(getSamplingPlanSql, getSamplingPlanParams.toArray());
    }

    public PropertyList createNewSamplingPlanVersion(String samplingPlanId, String samplingPlanVersionId) throws SapphireException {
        if (samplingPlanId == null) {
            throw new SapphireException("Sampling plan ID is null");
        }
        if (samplingPlanId.isEmpty()) {
            throw new SapphireException("Sampling plan ID is empty");
        }
        if (samplingPlanVersionId == null) {
            throw new SapphireException("Sampling plan version ID is null");
        }
        if (samplingPlanVersionId.isEmpty()) {
            throw new SapphireException("Sampling plan version ID is empty");
        }
        PropertyList addSamplingPlanVersionProps = new PropertyList();
        addSamplingPlanVersionProps.setProperty("sdcid", "LV_SamplingPlan");
        addSamplingPlanVersionProps.setProperty("keyid1", samplingPlanId);
        addSamplingPlanVersionProps.setProperty("keyid2", samplingPlanVersionId);
        this.getActionProcessor().processAction("AddSDIVersion", "1", addSamplingPlanVersionProps);
        return addSamplingPlanVersionProps;
    }

    public boolean checkSamplingPlanReference(String samplingPlanId, String samplingPlanVersionId) throws SapphireException {
        if (samplingPlanId == null) {
            throw new SapphireException("Sampling plan ID is null");
        }
        if (samplingPlanId.isEmpty()) {
            throw new SapphireException("Sampling plan ID is empty");
        }
        if (samplingPlanVersionId == null) {
            throw new SapphireException("Sampling plan version ID is null");
        }
        if (samplingPlanVersionId.isEmpty()) {
            throw new SapphireException("Sampling plan version ID is empty");
        }
        String getSamplingPlanReferenceSql = "SELECT COUNT(1) refcount FROM s_batch WHERE samplingplanid = ? AND samplingplanversionid = ?";
        Object[] getSamplingPlanReferenceParams = new Object[]{samplingPlanId, samplingPlanVersionId};
        DataSet getSamplingPlanReferenceDs = this.getQueryProcessor().getPreparedSqlDataSet(getSamplingPlanReferenceSql, getSamplingPlanReferenceParams);
        return getSamplingPlanReferenceDs.getBigDecimal(0, "refcount").compareTo(BigDecimal.ZERO) > 0;
    }

    public void addSamplingPlanToSDI(String sdcId, String keyId1, String keyId2, String keyId3, String samplingPlanIdColumn, String samplingPlanVersionIdColumn, String samplingPlanId, String samplingPlanVersionId) throws SapphireException {
        if (sdcId == null) {
            throw new SapphireException("SDC ID is null");
        }
        if (sdcId.isEmpty()) {
            throw new SapphireException("SDC ID is empty");
        }
        if (keyId1 == null) {
            throw new SapphireException("Key ID 1 is null");
        }
        if (keyId1.isEmpty()) {
            throw new SapphireException("Key ID 1 is empty");
        }
        if (keyId2 == null) {
            throw new SapphireException("Key ID 2 is null");
        }
        if (keyId3 == null) {
            throw new SapphireException("Key ID 3 is null");
        }
        if (samplingPlanId == null) {
            throw new SapphireException("Spec ID is null");
        }
        if (samplingPlanId.isEmpty()) {
            throw new SapphireException("Spec ID is empty");
        }
        if (samplingPlanVersionId == null) {
            throw new SapphireException("Sampling plan version ID is null");
        }
        if (samplingPlanVersionId.isEmpty()) {
            throw new SapphireException("Sampling plan version ID is empty");
        }
        if (samplingPlanIdColumn == null) {
            throw new SapphireException("Sampling plan ID column is null");
        }
        if (samplingPlanIdColumn.isEmpty()) {
            throw new SapphireException("Sampling plan ID column is empty");
        }
        if (samplingPlanVersionIdColumn == null) {
            throw new SapphireException("Sampling plan version ID column is null");
        }
        if (samplingPlanVersionIdColumn.isEmpty()) {
            throw new SapphireException("Sampling plan version ID column is empty");
        }
        PropertyList editSDIProps = new PropertyList();
        editSDIProps.setProperty("sdcid", sdcId);
        editSDIProps.setProperty("keyid1", keyId1);
        if (keyId2.length() > 0) {
            editSDIProps.setProperty("keyid2", keyId2);
        }
        if (keyId3.length() > 0) {
            editSDIProps.setProperty("keyid3", keyId3);
        }
        editSDIProps.setProperty(samplingPlanIdColumn, samplingPlanId);
        editSDIProps.setProperty(samplingPlanVersionIdColumn, samplingPlanVersionId);
        this.getActionProcessor().processAction("EditSDI", "1", editSDIProps);
    }

    public void syncVersionStatusWithPrimary(String samplingPlanId, String samplingPlanVersionId, String sdcId, String keyId1, String keyId2, String keyId3) throws SapphireException {
        if (samplingPlanId == null) {
            throw new SapphireException("Sampling plan ID is null");
        }
        if (samplingPlanId.isEmpty()) {
            throw new SapphireException("Sampling plan ID is empty");
        }
        if (samplingPlanVersionId == null) {
            throw new SapphireException("Sampling plan version ID is null");
        }
        if (samplingPlanVersionId.isEmpty()) {
            throw new SapphireException("Sampling plan version ID is empty");
        }
        if (sdcId == null) {
            throw new SapphireException("SDC ID is null");
        }
        if (sdcId.isEmpty()) {
            throw new SapphireException("SDC ID is empty");
        }
        if (keyId1 == null) {
            throw new SapphireException("Key ID1 is null");
        }
        if (keyId1.isEmpty()) {
            throw new SapphireException("Key ID1 is empty");
        }
        if (keyId2 == null) {
            throw new SapphireException("Key ID2 is null");
        }
        if (keyId3 == null) {
            throw new SapphireException("Key ID3 is null");
        }
        SimpleSpecHelper simpleSpecHelper = new SimpleSpecHelper(this.getConnectionId());
        String versionStatus = simpleSpecHelper.getPrimaryVersionStatus(sdcId, keyId1, keyId2, keyId3);
        PropertyList setSpecVersionStatus = new PropertyList();
        setSpecVersionStatus.setProperty("sdcid", "LV_SamplingPlan");
        setSpecVersionStatus.setProperty("keyid1", samplingPlanId);
        setSpecVersionStatus.setProperty("keyid2", samplingPlanVersionId);
        setSpecVersionStatus.setProperty("versionstatus", versionStatus);
        this.getActionProcessor().processAction("SetSDIVersionStatus", "1", setSpecVersionStatus);
    }

    public PropertyList createNewSamplingPlan(String sdcId, String keyId1, String keyId2, String keyId3) throws SapphireException {
        if (sdcId == null) {
            throw new SapphireException("SDC ID is null");
        }
        if (sdcId.isEmpty()) {
            throw new SapphireException("SDC ID is empty");
        }
        if (keyId1 == null) {
            throw new SapphireException("Key ID 1 is null");
        }
        if (keyId1.isEmpty()) {
            throw new SapphireException("Key ID 1 is empty");
        }
        if (keyId2 == null) {
            throw new SapphireException("Key ID 2 is null");
        }
        if (keyId3 == null) {
            throw new SapphireException("Key ID 3 is null");
        }
        PropertyList getNewSamplingPlanIdProps = new PropertyList();
        getNewSamplingPlanIdProps.setProperty("sdcid", sdcId);
        getNewSamplingPlanIdProps.setProperty("keyid1", keyId1);
        this.getActionProcessor().processActionClass(GetNewSPId.class.getName(), getNewSamplingPlanIdProps);
        String newSamplingPlanId = getNewSamplingPlanIdProps.getProperty("newsamplingplanid");
        String sdcName = this.getQueryProcessor().getPreparedSqlDataSet("SELECT singular FROM sdc WHERE sdcid = ?", (Object[])new String[]{sdcId}).getString(0, "singular");
        PropertyList addSamplingPlanProps = new PropertyList();
        addSamplingPlanProps.setProperty("sdcid", "LV_SamplingPlan");
        addSamplingPlanProps.setProperty("keyid1", newSamplingPlanId);
        addSamplingPlanProps.setProperty("keyid2", "1");
        addSamplingPlanProps.setProperty("samplingplandesc", "Auto created and maintained simple sampling plan for " + sdcName);
        addSamplingPlanProps.setProperty("embeddedflag", "Y");
        this.getActionProcessor().processAction("AddSDI", "1", addSamplingPlanProps);
        return addSamplingPlanProps;
    }

    public PropertyList prepareSamplingPlanForAddOrEdit(String sdcId, String keyId1, String keyId2, String keyId3, AutomaticVersioning automaticVersioning, String samplingPlanIdColumn, String samplingPlanVersionIdColumn) throws SapphireException {
        if (sdcId == null) {
            throw new SapphireException("SDC ID is null");
        }
        if (sdcId.isEmpty()) {
            throw new SapphireException("SDC ID is empty");
        }
        if (keyId1 == null) {
            throw new SapphireException("Key ID 1 is null");
        }
        if (keyId1.isEmpty()) {
            throw new SapphireException("Key ID 1 is empty");
        }
        if (keyId2 == null) {
            throw new SapphireException("Key ID 2 is null");
        }
        if (keyId3 == null) {
            throw new SapphireException("Key ID 3 is null");
        }
        if (automaticVersioning == null) {
            throw new SapphireException("Automatic versioning is null");
        }
        if (samplingPlanIdColumn == null) {
            throw new SapphireException("Sampling plan ID column is null");
        }
        if (samplingPlanIdColumn.isEmpty()) {
            throw new SapphireException("Sampling plan ID column is empty");
        }
        if (samplingPlanVersionIdColumn == null) {
            throw new SapphireException("Sampling plan version ID column is null");
        }
        if (samplingPlanVersionIdColumn.isEmpty()) {
            throw new SapphireException("Sampling plan version ID column is empty");
        }
        PropertyList samplingPlanProps = new PropertyList();
        DataSet samplingPlanDs = this.getSamplingPlan(sdcId, keyId1, keyId2, keyId3, samplingPlanIdColumn, samplingPlanVersionIdColumn);
        String samplingPlanId = samplingPlanDs.getString(0, "s_samplingplanid");
        String samplingPlanVersionId = samplingPlanDs.getString(0, "s_samplingplanversionid");
        if (samplingPlanDs.getRowCount() == 0) {
            PropertyList addSamplingPlanProps = this.createNewSamplingPlan(sdcId, keyId1, keyId2, keyId3);
            samplingPlanId = addSamplingPlanProps.getProperty("newkeyid1");
            samplingPlanVersionId = addSamplingPlanProps.getProperty("newkeyid2");
            this.addSamplingPlanToSDI(sdcId, keyId1, keyId2, keyId3, samplingPlanIdColumn, samplingPlanVersionIdColumn, samplingPlanId, samplingPlanVersionId);
            if (automaticVersioning == AutomaticVersioning.ALWAYS || automaticVersioning == AutomaticVersioning.ONLY_REFERENCED) {
                this.syncVersionStatusWithPrimary(samplingPlanId, samplingPlanVersionId, sdcId, keyId1, keyId2, keyId3);
            }
        } else if (automaticVersioning == AutomaticVersioning.ALWAYS || automaticVersioning == AutomaticVersioning.ONLY_REFERENCED && this.checkSamplingPlanReference(samplingPlanId, samplingPlanVersionId)) {
            PropertyList addSamplingPlanVersionProps = this.createNewSamplingPlanVersion(samplingPlanId, samplingPlanVersionId);
            samplingPlanId = addSamplingPlanVersionProps.getProperty("newkeyid1");
            samplingPlanVersionId = addSamplingPlanVersionProps.getProperty("newkeyid2");
            this.addSamplingPlanToSDI(sdcId, keyId1, keyId2, keyId3, samplingPlanIdColumn, samplingPlanVersionIdColumn, samplingPlanId, samplingPlanVersionId);
            this.syncVersionStatusWithPrimary(samplingPlanId, samplingPlanVersionId, sdcId, keyId1, keyId2, keyId3);
        }
        samplingPlanProps.setProperty("samplingplanid", samplingPlanId);
        samplingPlanProps.setProperty("samplingplanversionid", samplingPlanVersionId);
        return samplingPlanProps;
    }
}

