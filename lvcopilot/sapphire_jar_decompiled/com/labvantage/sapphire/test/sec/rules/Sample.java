/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec.rules;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class Sample
extends BaseSDCRules {
    @Override
    public boolean requiresDataReleasePrimary() {
        return true;
    }

    @Override
    public boolean requiresBeforeEditSDIDataImage() {
        return true;
    }

    @Override
    public boolean requiresEditSDIDataPrimary() {
        return true;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList propertyList) throws SapphireException {
        if (!"sectester".equals(this.connectionInfo.getSysuserId())) {
            return;
        }
        DataSet primary = sdiData.getDataset("primary");
        if (this.hasPrimaryValueChanged(primary, 0, "samplestatus")) {
            String sampleId;
            String status = primary.getValue(0, "samplestatus");
            String reviewDisposition = primary.getValue(0, "reviewdisposition");
            if ("Reviewed".equals(status)) {
                String sampleId2 = propertyList.getProperty("keyid1");
                if (sampleId2 != null) {
                    HashMap<String, String> props = new HashMap<String, String>();
                    props.put("sampleid", sampleId2);
                    try {
                        this.getActionProcessor().processAction("CreateResultsRecord", "1", props);
                        props.put("reviewdisposition", reviewDisposition);
                        this.getActionProcessor().processAction("CreateUsageDecision", "1", props);
                    }
                    catch (ActionException e) {
                        this.setError("SampleSECRules", "INFORMATION", e.getMessage());
                    }
                }
            } else if ("Cancelled".equals(status) && (sampleId = propertyList.getProperty("keyid1")) != null) {
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("sampleid", sampleId);
                try {
                    props.put("reviewdisposition", "Cancelled");
                    this.getActionProcessor().processAction("CreateUsageDecision", "1", props);
                }
                catch (ActionException e) {
                    this.setError("SampleSECRules", "INFORMATION", e.getMessage());
                }
            }
        }
    }
}

