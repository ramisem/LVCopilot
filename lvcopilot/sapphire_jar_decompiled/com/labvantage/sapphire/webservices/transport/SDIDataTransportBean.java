/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.webservices.transport;

import com.labvantage.sapphire.webservices.transport.DataSetTransportBean;
import java.util.Iterator;
import java.util.Set;
import sapphire.util.SDIData;

public class SDIDataTransportBean {
    private DataSetTransportBean[] datasets = new DataSetTransportBean[0];
    private String[] datasetNames = new String[0];
    private String[] datasetKeys = new String[0];
    private String rsetId = "";
    private String sdcid = "";

    public DataSetTransportBean[] getDataSets() {
        return this.datasets;
    }

    public String[] getNames() {
        return this.datasetNames;
    }

    public String[] getKeys() {
        return this.datasetKeys;
    }

    public String getRsetId() {
        return this.rsetId;
    }

    public String getSDCId() {
        return this.sdcid;
    }

    public SDIDataTransportBean() {
    }

    public SDIDataTransportBean(SDIData sdidata) {
        this.setSDIData(sdidata);
    }

    protected void setSDIData(SDIData sdidata) {
        if (sdidata != null) {
            this.rsetId = sdidata.getRsetid();
            this.sdcid = sdidata.getSdcid();
            Set ds = sdidata.getDatasets();
            int iS = ds.size();
            Iterator it = ds.iterator();
            this.datasets = new DataSetTransportBean[iS];
            this.datasetNames = new String[iS];
            this.datasetKeys = new String[iS];
            int dsindex = 0;
            while (it.hasNext()) {
                String name = it.next().toString();
                DataSetTransportBean dsb = new DataSetTransportBean();
                dsb.setDataSet(sdidata.getDataset(name), true, true);
                this.datasets[dsindex] = dsb;
                this.datasetNames[dsindex] = name;
                String[] dsks = sdidata.getKeys(name);
                if (dsks != null) {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < dsks.length; ++i) {
                        if (i > 0) {
                            sb.append(";");
                        }
                        sb.append(dsks[i]);
                    }
                    this.datasetKeys[dsindex] = sb.toString();
                } else {
                    this.datasetKeys[dsindex] = "";
                }
                ++dsindex;
            }
        } else {
            this.datasets = new DataSetTransportBean[0];
            this.datasetNames = new String[0];
            this.datasetKeys = new String[0];
            this.rsetId = "";
            this.sdcid = "";
        }
    }
}

