/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.sapphire.actions.array.ArrayUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SendToInstrument
extends BaseAction
implements sapphire.action.SendToInstrument {
    public static final String DELIMITER = ";";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String arrayid = properties.getProperty("arrayid");
        if (arrayid == null || arrayid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Array ID is mandatory"));
        }
        String instrumentid = properties.getProperty("instrumentid", "");
        if (instrumentid.length() == 0) {
            throw new SapphireException("Instrument should be specified to perform this operation");
        }
        String[] arrayIdList = StringUtil.split(arrayid, DELIMITER);
        for (int aid = 0; aid < arrayIdList.length; ++aid) {
            String currArrayId = arrayIdList[aid];
            String arrayStatus = ArrayUtil.getArrayStatus(this.getQueryProcessor(), currArrayId);
            String lastArrayMethodDetails = ArrayUtil.getLastArrayMethodItem(this.getQueryProcessor(), currArrayId);
            if (lastArrayMethodDetails.length() == 0) {
                throw new SapphireException("Did not find matching array method item for:" + currArrayId);
            }
            String[] tokens = StringUtil.split(lastArrayMethodDetails, "|");
            String lastArrayMethodId = tokens[0];
            String lastInstanceNum = tokens[1];
            String promoteResultsFlag = tokens[3];
            String currentAMIStatus = tokens[2];
            String arraymethodversionid = ArrayUtil.getArrayMethodCurrentVersion(this.getQueryProcessor(), lastArrayMethodId);
            if (!ArrayUtil.validateStatus(arrayStatus, currentAMIStatus, "SendToInstrument", promoteResultsFlag)) {
                throw new SapphireException(this.getTranslationProcessor().translate("Invalid status. Send To Instrument cannot be requested on the array:") + arrayid);
            }
            PropertyList arrayarraymethoditemprops = new PropertyList();
            arrayarraymethoditemprops.setProperty("sdcid", "LV_Array");
            arrayarraymethoditemprops.setProperty("linkid", "Array ArrayMethod Item");
            arrayarraymethoditemprops.setProperty("arrayid", currArrayId);
            arrayarraymethoditemprops.setProperty("arraymethodid", lastArrayMethodId);
            arrayarraymethoditemprops.setProperty("arraymethodversionid", arraymethodversionid);
            arrayarraymethoditemprops.setProperty("arraymethodinstance", lastInstanceNum);
            arrayarraymethoditemprops.setProperty("arraymethoditemstatus", "SentToInstrument");
            arrayarraymethoditemprops.setProperty("instrumentid", instrumentid);
            arrayarraymethoditemprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            arrayarraymethoditemprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
            arrayarraymethoditemprops.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("EditSDIDetail", "1", arrayarraymethoditemprops);
        }
    }
}

