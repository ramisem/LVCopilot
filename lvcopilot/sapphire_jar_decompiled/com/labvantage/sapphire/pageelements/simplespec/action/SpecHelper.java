/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.simplespec.action;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.simplespec.util.SimpleSpecHelper;
import java.math.BigDecimal;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SpecHelper
extends BaseCustom {
    public SpecHelper(String connectionId) throws SapphireException {
        if (connectionId == null) {
            throw new SapphireException("Connection ID is null");
        }
        if (connectionId.isEmpty()) {
            throw new SapphireException("Connection ID is empty");
        }
        this.setConnectionId(connectionId);
    }

    public DataSet getSpec(String sdcId, String keyId1, String keyId2, String keyId3, String specIdColumn, String specVersionIdColumn) throws SapphireException {
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
        if (specIdColumn == null) {
            throw new SapphireException("Spec ID column is null");
        }
        if (specIdColumn.isEmpty()) {
            throw new SapphireException("Spec ID column is empty");
        }
        if (specVersionIdColumn == null) {
            throw new SapphireException("Spec version ID column is null");
        }
        if (specVersionIdColumn.isEmpty()) {
            throw new SapphireException("Spec version ID column is empty");
        }
        PropertyList primaryProps = new PropertyList(this.getSDCProcessor().getSDCProperties(sdcId));
        String tableId = primaryProps.getProperty("tableid");
        String keyColId1 = primaryProps.getProperty("keycolid1");
        String keyColId2 = primaryProps.getProperty("keycolid2");
        String keyColId3 = primaryProps.getProperty("keycolid3");
        String getSpecPreparedSql = "SELECT p." + specIdColumn + " specid, p." + specVersionIdColumn + " specversionid FROM " + tableId + " p WHERE p." + keyColId1 + " = ? " + (!keyColId2.isEmpty() ? " AND p." + keyColId2 + " = ?" : "") + (!keyColId3.isEmpty() ? " and p." + keyColId3 + " = ?" : "");
        int paramCount = 1;
        if (keyId2.length() > 0) {
            ++paramCount;
        }
        if (keyId3.length() > 0) {
            ++paramCount;
        }
        Object[] getSpecPreparedParams = new String[paramCount];
        getSpecPreparedParams[0] = keyId1;
        if (keyId2.length() > 0) {
            getSpecPreparedParams[1] = keyId2;
        }
        if (keyId3.length() > 0) {
            getSpecPreparedParams[2] = keyId3;
        }
        return this.getQueryProcessor().getPreparedSqlDataSet(getSpecPreparedSql, getSpecPreparedParams);
    }

    public boolean checkSpecReference(String specId, String specVersionId) throws SapphireException {
        if (specId == null) {
            throw new SapphireException("Spec ID is null");
        }
        if (specId.isEmpty()) {
            throw new SapphireException("Spec ID is empty");
        }
        if (specVersionId == null) {
            throw new SapphireException("Spec version ID is null");
        }
        if (specVersionId.isEmpty()) {
            throw new SapphireException("Spec version ID is empty");
        }
        String getSpecReferenceSql = "SELECT COUNT(1) refcount FROM sdispec WHERE specid = ? AND specversionid = ?";
        Object[] getSpecReferenceParams = new Object[]{specId, specVersionId};
        DataSet getSpecReferenceDs = this.getQueryProcessor().getPreparedSqlDataSet(getSpecReferenceSql, getSpecReferenceParams);
        return getSpecReferenceDs.getBigDecimal(0, "refcount").compareTo(BigDecimal.ZERO) > 0;
    }

    public PropertyList createNewSpecVersion(String specId, String specVersionId) throws SapphireException {
        if (specId == null) {
            throw new SapphireException("Spec ID is null");
        }
        if (specId.isEmpty()) {
            throw new SapphireException("Spec ID is empty");
        }
        if (specVersionId == null) {
            throw new SapphireException("Spec version ID is null");
        }
        if (specVersionId.isEmpty()) {
            throw new SapphireException("Spec version ID is empty");
        }
        PropertyList addSpecVersionProps = new PropertyList();
        addSpecVersionProps.setProperty("sdcid", "SpecSDC");
        addSpecVersionProps.setProperty("keyid1", specId);
        addSpecVersionProps.setProperty("keyid2", specVersionId);
        this.getActionProcessor().processAction("AddSDIVersion", "1", addSpecVersionProps);
        return addSpecVersionProps;
    }

    public void addSpecToSDI(String sdcId, String keyId1, String keyId2, String keyId3, String specId, String specVersionId, String specIdColumn, String specVersionIdColumn) throws SapphireException {
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
        if (specId == null) {
            throw new SapphireException("Spec ID is null");
        }
        if (specId.isEmpty()) {
            throw new SapphireException("Spec ID is empty");
        }
        if (specVersionId == null) {
            throw new SapphireException("Spec version ID is null");
        }
        if (specVersionId.isEmpty()) {
            throw new SapphireException("Spec version ID is empty");
        }
        if (specIdColumn == null) {
            throw new SapphireException("Spec ID column is null");
        }
        if (specIdColumn.isEmpty()) {
            throw new SapphireException("Spec ID column is empty");
        }
        if (specVersionIdColumn == null) {
            throw new SapphireException("Spec version ID column is null");
        }
        if (specVersionIdColumn.isEmpty()) {
            throw new SapphireException("Spec version ID column is empty");
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
        editSDIProps.setProperty(specIdColumn, specId);
        editSDIProps.setProperty(specVersionIdColumn, specVersionId);
        this.getActionProcessor().processAction("EditSDI", "1", editSDIProps);
    }

    public void syncVersionStatusWithPrimary(String specId, String specVersionId, String sdcId, String keyId1, String keyId2, String keyId3) throws SapphireException {
        if (specId == null) {
            throw new SapphireException("Spec ID is null");
        }
        if (specId.isEmpty()) {
            throw new SapphireException("Spec ID is empty");
        }
        if (specVersionId == null) {
            throw new SapphireException("Spec version ID is null");
        }
        if (specVersionId.isEmpty()) {
            throw new SapphireException("Spec version ID is empty");
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
        setSpecVersionStatus.setProperty("sdcid", "SpecSDC");
        setSpecVersionStatus.setProperty("keyid1", specId);
        setSpecVersionStatus.setProperty("keyid2", specVersionId);
        setSpecVersionStatus.setProperty("versionstatus", versionStatus);
        this.getActionProcessor().processAction("SetSDIVersionStatus", "1", setSpecVersionStatus);
    }
}

