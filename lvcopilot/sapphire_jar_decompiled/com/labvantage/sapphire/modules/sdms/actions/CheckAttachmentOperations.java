/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.actions;

import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;

public class CheckAttachmentOperations
extends BaseAction
implements SDMSConstants {
    public static final String ID = "CheckAttachmentOperations";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_STATUSCOLUMN = "statuscolumn";
    public static final String PROPERTY_STATUSEND = "statusend";

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String sdcid = propertyList.getProperty(PROPERTY_SDCID);
        String keyid1 = propertyList.getProperty(PROPERTY_KEYID1);
        String keyid2 = null;
        if (propertyList.containsKey(PROPERTY_KEYID2)) {
            keyid2 = propertyList.getProperty(PROPERTY_KEYID2);
        }
        String keyid3 = null;
        if (propertyList.containsKey(PROPERTY_KEYID3)) {
            keyid3 = propertyList.getProperty(PROPERTY_KEYID3);
        }
        if (keyid1.length() <= 0 || sdcid.length() <= 0) throw new SapphireException("No sdcid or keyid1 provided.");
        try {
            if (!CheckAttachmentOperations.checkAttachmentOperationsProcessed(sdcid, keyid1, keyid2, keyid3, this.getSDIProcessor(), this.getSDCProcessor(), this.logger)) return;
            String status = propertyList.getProperty(PROPERTY_STATUSEND);
            String statuscolumn = propertyList.getProperty(PROPERTY_STATUSCOLUMN);
            if (status.length() <= 0 || statuscolumn.length() <= 0) return;
            PropertyList edit = new PropertyList();
            edit.setProperty(PROPERTY_SDCID, sdcid);
            edit.setProperty(PROPERTY_KEYID1, keyid1);
            if (keyid2 != null && keyid2.length() > 0) {
                edit.setProperty(PROPERTY_KEYID2, keyid2);
            }
            if (keyid3 != null && keyid3.length() > 0) {
                edit.setProperty(PROPERTY_KEYID3, keyid3);
            }
            edit.setProperty(statuscolumn, status);
            this.getActionProcessor().processAction("EditSDI", "1", edit, false);
            return;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public static boolean checkAttachmentOperationsProcessed(String sdcid, String keyid1, String keyid2, String keyid3, SDIProcessor sdiProcessor, SDCProcessor sdcProcessor, Logger logger) {
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setQueryFrom(sdcProcessor.getProperty(sdcid, "tableid"));
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("attachmentoperation");
        sdiRequest.setExtendedDataTypes(true);
        StringBuilder qw = new StringBuilder();
        qw.append(sdcProcessor.getProperty(sdcid, "keycolid1")).append(" = '").append(keyid1).append("'");
        String k2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        if (keyid2 != null && keyid2.length() > 0 && k2.length() > 0) {
            qw.append(k2).append(" = '").append(keyid2).append("'");
        }
        String k3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        if (keyid3 != null && keyid3.length() > 0 && k3.length() > 0) {
            qw.append(k3).append(" = '").append(keyid3).append("'");
        }
        sdiRequest.setQueryWhere(qw.toString());
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        boolean processed = true;
        if (sdiData != null && sdiData.getDataset("primary") != null && sdiData.getDataset("primary").getRowCount() > 0) {
            DataSet captureops = sdiData.getDataset("attachmentoperation");
            if (captureops != null && captureops.getRowCount() > 0) {
                for (int r = 0; r < captureops.getRowCount(); ++r) {
                    String status = captureops.getValue(r, "operationstatus", "ready");
                    if (status.equalsIgnoreCase("Processed")) continue;
                    processed = false;
                    break;
                }
            } else {
                logger.info("No attachment operations.");
                processed = true;
            }
        } else {
            logger.info("Failed to obtain data capture.");
            processed = false;
        }
        return processed;
    }
}

