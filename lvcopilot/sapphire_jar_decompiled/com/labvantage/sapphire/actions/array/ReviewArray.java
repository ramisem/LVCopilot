/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.sapphire.actions.array.ArrayUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ReviewArray
extends BaseAction
implements sapphire.action.ReviewArray {
    public static final String DELIMITER = ";";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String arrayids = properties.getProperty("arrayid");
        if (arrayids == null || arrayids.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Array ID is mandatory"));
        }
        String[] arrayIdList = StringUtil.split(arrayids, DELIMITER);
        String arraymethodid = properties.getProperty("arraymethodid");
        String arraymethodinstance = properties.getProperty("arraymethodinstance", "");
        for (int i = 0; i < arrayIdList.length; ++i) {
            String arrayitemdetails = ArrayUtil.getCurrentArrayMethodItemDetails(this.getQueryProcessor(), arrayIdList[i], arraymethodid, arraymethodinstance);
            if (arrayitemdetails.length() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("Cannot find an Array Method Item to review array:") + arrayIdList[i]);
            }
            String[] tokens = StringUtil.split(arrayitemdetails, "|");
            arraymethodinstance = tokens[1];
            if (!ArrayUtil.validateArrayOperation(this.getQueryProcessor(), arrayIdList[i], "ReviewArray")) {
                throw new SapphireException(this.getTranslationProcessor().translate("Invalid status. Cannot perform Review on array:") + arrayIdList[i]);
            }
            this.updateArrayArrayMethodItemStatus(properties, arrayIdList[i], arraymethodid, arraymethodinstance);
        }
    }

    private void updateArrayArrayMethodItemStatus(PropertyList properties, String arrayid, String arraymethodid, String arraymethodinstance) throws SapphireException {
        PropertyList arrayarraymethoditemprops = new PropertyList();
        arrayarraymethoditemprops.setProperty("sdcid", "LV_Array");
        arrayarraymethoditemprops.setProperty("linkid", "Array ArrayMethod Item");
        arrayarraymethoditemprops.setProperty("arrayid", arrayid);
        arrayarraymethoditemprops.setProperty("arraymethodid", arraymethodid);
        arrayarraymethoditemprops.setProperty("arraymethodinstance", arraymethodinstance);
        arrayarraymethoditemprops.setProperty("arraymethoditemstatus", "Reviewed");
        arrayarraymethoditemprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
        arrayarraymethoditemprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
        arrayarraymethoditemprops.setProperty("auditreason", properties.getProperty("auditreason"));
        this.getActionProcessor().processAction("EditSDIDetail", "1", arrayarraymethoditemprops);
    }
}

