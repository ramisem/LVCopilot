/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.AbstractDataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.SDIListDataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;

public class SDIListDataSetProvider
extends AbstractDataSetProvider {
    private final String sdcId;
    private final String keyId1List;
    private final String keyId2List;
    private final String keyId3List;

    public SDIListDataSetProvider(SDIListDataSetProviderConfiguration sdiListDataSetProviderConf, String connectionId, DataBindingMap dataBindingMap) throws SapphireException {
        super(connectionId);
        if (sdiListDataSetProviderConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        if (dataBindingMap == null) {
            throw new IllegalArgumentException("Data binding map is null");
        }
        this.sdcId = sdiListDataSetProviderConf.getSdcId().evaluate(dataBindingMap);
        if (this.sdcId.contains(";")) {
            throw new IllegalArgumentException("Only single SDC Id allowed");
        }
        this.keyId1List = sdiListDataSetProviderConf.getKeyId1s().evaluate(dataBindingMap);
        this.keyId2List = sdiListDataSetProviderConf.getKeyId2s().evaluate(dataBindingMap);
        this.keyId3List = sdiListDataSetProviderConf.getKeyId3s().evaluate(dataBindingMap);
    }

    @Override
    public DataSet getDataSet() {
        DataSet primary = null;
        if (!this.sdcId.isEmpty() && !this.keyId1List.isEmpty()) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(this.sdcId);
            sdiRequest.setKeyid1List(this.keyId1List);
            sdiRequest.setKeyid2List(this.keyId2List);
            sdiRequest.setKeyid3List(this.keyId3List);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRetainRsetid(true);
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            this.setRSetId(sdiData.getRsetid());
            primary = sdiData.getDataset("primary");
        }
        if (primary == null) {
            primary = new DataSet();
        }
        return primary;
    }
}

