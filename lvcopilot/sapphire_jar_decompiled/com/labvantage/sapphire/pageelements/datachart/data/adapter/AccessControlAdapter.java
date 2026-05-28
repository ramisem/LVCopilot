/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.data.adapter.AbstractDataSetAdapter;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.AccessControlAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;

public class AccessControlAdapter
extends AbstractDataSetAdapter {
    private static final String DELIM = ";";
    private final AccessControlAdapterConfiguration accessControlAdapterConf;

    public AccessControlAdapter(String connectionId, DataBindingMap dataBindingMap, AccessControlAdapterConfiguration accessControlAdapterConf) {
        super(connectionId, dataBindingMap);
        if (accessControlAdapterConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        this.accessControlAdapterConf = accessControlAdapterConf;
    }

    @Override
    public void processDataSetAdapter(DataSet dataSet) {
        boolean isKeyId3Enabled;
        if (dataSet == null) {
            throw new IllegalArgumentException("Data set is null");
        }
        String sdcId = this.accessControlAdapterConf.getSdcId();
        String keyId1Column = this.accessControlAdapterConf.getKeyId1Column();
        String keyId2Column = this.accessControlAdapterConf.getKeyId2Column();
        String keyId3Column = this.accessControlAdapterConf.getKeyId3Column();
        if (sdcId.isEmpty()) {
            throw new IllegalArgumentException("SDC ID is empty");
        }
        PropertyList sdcProps = new PropertyList(this.getSDCProcessor().getSDCProperties(sdcId));
        String keyColId1 = sdcProps.getProperty("keycolid1", "");
        String keyColId2 = sdcProps.getProperty("keycolid2", "");
        String keyColId3 = sdcProps.getProperty("keycolid3", "");
        boolean isKeyId2Supported = !keyColId2.isEmpty();
        boolean isKeyId3Supported = !keyColId3.isEmpty();
        boolean isKeyId2Enabled = isKeyId2Supported && !keyId2Column.isEmpty();
        boolean bl = isKeyId3Enabled = isKeyId3Supported && !keyId3Column.isEmpty();
        if (keyId1Column.isEmpty()) {
            throw new IllegalArgumentException("Key ID1 column is empty");
        }
        if (dataSet.getColumnType(keyId1Column) == -1) {
            throw new IllegalArgumentException("Key ID1 column not found in data set: " + keyId1Column);
        }
        if (isKeyId2Enabled && dataSet.getColumnType(keyId2Column) == -1) {
            throw new IllegalArgumentException("Key ID2 column not found in data set: " + keyId2Column);
        }
        if (isKeyId3Enabled && dataSet.getColumnType(keyId3Column) == -1) {
            throw new IllegalArgumentException("Key ID3 column not found in data set: " + keyId3Column);
        }
        String keyId1ColumnValues = dataSet.getColumnValues(keyId1Column, DELIM);
        String keyId2ColumnValues = "";
        String keyId3ColumnValues = "";
        if (isKeyId2Enabled) {
            keyId2ColumnValues = dataSet.getColumnValues(keyId2Column, DELIM);
            if (isKeyId3Enabled) {
                keyId3ColumnValues = dataSet.getColumnValues(keyId3Column, DELIM);
            }
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcId);
        sdiRequest.setKeyid1List(keyId1ColumnValues);
        if (isKeyId2Enabled) {
            sdiRequest.setKeyid2List(keyId2ColumnValues);
            if (isKeyId3Enabled) {
                sdiRequest.setKeyid3List(keyId3ColumnValues);
            }
        }
        sdiRequest.setRequestItem("primary");
        sdiRequest.setPrimaryLockOption("LA");
        DataSet primary = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
        Set<List<String>> failKeysSet = this.getKeysSet(isKeyId2Enabled, isKeyId3Enabled, keyId1ColumnValues, keyId2ColumnValues, keyId3ColumnValues);
        String passKeyId1ColumnValues = primary.getColumnValues(keyColId1, DELIM);
        String passKeyId2ColumnValues = "";
        String passKeyId3ColumnValues = "";
        if (isKeyId2Enabled) {
            passKeyId2ColumnValues = primary.getColumnValues(keyColId2, DELIM);
            if (isKeyId3Enabled) {
                passKeyId3ColumnValues = primary.getColumnValues(keyColId3, DELIM);
            }
        }
        Set<List<String>> passKeysSet = this.getKeysSet(isKeyId2Enabled, isKeyId3Enabled, passKeyId1ColumnValues, passKeyId2ColumnValues, passKeyId3ColumnValues);
        failKeysSet.removeAll(passKeysSet);
        for (List<String> keys : failKeysSet) {
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put(keyId1Column, keys.get(0));
            if (isKeyId2Enabled) {
                findMap.put(keyId2Column, keys.get(1));
                if (isKeyId3Enabled) {
                    findMap.put(keyId3Column, keys.get(2));
                }
            }
            this.deleteMatchingRows(dataSet, findMap);
        }
        this.setProcessedDataSet(dataSet);
    }

    private void deleteMatchingRows(DataSet dataSet, HashMap<String, String> findMap) {
        int row = dataSet.findRow(findMap);
        for (int counter = dataSet.getRowCount(); row != -1 && counter >= 0; --counter) {
            dataSet.deleteRow(row);
            row = dataSet.findRow(findMap);
        }
    }

    private Set<List<String>> getKeysSet(boolean keyId2Enabled, boolean keyId3Enabled, String keyId1ColumnValues, String keyId2ColumnValues, String keyId3ColumnValues) {
        HashSet<List<String>> failKeysSet = new HashSet<List<String>>();
        ArrayList<String> keyId1List = new ArrayList<String>(Arrays.asList(keyId1ColumnValues.split(DELIM)));
        ArrayList<Object> keyId2List = new ArrayList();
        ArrayList<Object> keyId3List = new ArrayList();
        if (keyId2Enabled) {
            keyId2List = new ArrayList<String>(Arrays.asList(keyId2ColumnValues.split(DELIM)));
            if (keyId3Enabled) {
                keyId3List = new ArrayList<String>(Arrays.asList(keyId3ColumnValues.split(DELIM)));
            }
        }
        for (int i = 0; i < keyId1List.size(); ++i) {
            ArrayList failKeyList = new ArrayList();
            failKeyList.add(keyId1List.get(i));
            if (keyId2Enabled) {
                failKeyList.add(keyId2List.get(i));
                if (keyId3Enabled) {
                    failKeyList.add(keyId3List.get(i));
                }
            }
            failKeysSet.add(failKeyList);
        }
        return failKeysSet;
    }
}

