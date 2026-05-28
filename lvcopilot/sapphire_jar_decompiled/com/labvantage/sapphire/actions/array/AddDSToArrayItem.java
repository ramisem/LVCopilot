/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.sdidata.AddDataSet;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddDSToArrayItem
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    public static final String PROPERTY_ARRAYID = "arrayid";
    public static final String PROPERTY_PARAMLISTID = "paramlistid";
    public static final String PROPERTY_PARAMLISTVERSIONID = "paramlistversionid";
    public static final String PROPERTY_VARIANTID = "variantid";
    public static final String PROPERTY_AUDITREASON = "auditreason";
    public static final String PROPERTY_AUDITACTIVITY = "auditactivity";
    public static final String PROPERTY_AUDITSIGNEDFLAG = "auditsignedflag";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.logger.info("processing  AddDataSetToArrayItem action...");
        TranslationProcessor tp = this.getTranslationProcessor();
        String arrayId = properties.getProperty(PROPERTY_ARRAYID, "");
        String paramListId = properties.getProperty(PROPERTY_PARAMLISTID, "");
        String paramListVersionId = properties.getProperty(PROPERTY_PARAMLISTVERSIONID, "");
        String variantId = properties.getProperty(PROPERTY_VARIANTID, "");
        if (arrayId.length() == 0 || paramListId.length() == 0 || paramListVersionId.length() == 0 || variantId.length() == 0) {
            throw new SapphireException("INVALID_PARAMETERS", tp.translate("Missing Required Arguments: arrayid, paramlistid, paramlistversionid and variantid"));
        }
        if (StringUtil.split(arrayId, ";").length > 1) {
            throw new SapphireException(tp.translate("AddDataSetToArrayItem action supports only single arrayid property"));
        }
        String sql = "SELECT arrayitemid, arrayid, xpos, ypos from arrayitem where arrayid = ?";
        DataSet aiDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{arrayId});
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "LV_ArrayItem");
        actionProps.setProperty("keyid1", aiDS.getColumnValues("arrayitemid", ";"));
        actionProps.setProperty(PROPERTY_PARAMLISTID, paramListId);
        actionProps.setProperty(PROPERTY_PARAMLISTVERSIONID, paramListVersionId);
        actionProps.setProperty(PROPERTY_VARIANTID, variantId);
        actionProps.setProperty("propsmatch", "Y");
        actionProps.setProperty(PROPERTY_AUDITACTIVITY, properties.getProperty(PROPERTY_AUDITACTIVITY));
        actionProps.setProperty(PROPERTY_AUDITREASON, properties.getProperty(PROPERTY_AUDITREASON));
        actionProps.setProperty(PROPERTY_AUDITSIGNEDFLAG, properties.getProperty(PROPERTY_AUDITSIGNEDFLAG));
        try {
            this.getActionProcessor().processActionClass(AddDataSet.class.getName(), actionProps);
        }
        catch (Exception e) {
            throw new SapphireException(tp.translate("Error in processing AddDataSetToArrayItem action") + ":" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
    }
}

