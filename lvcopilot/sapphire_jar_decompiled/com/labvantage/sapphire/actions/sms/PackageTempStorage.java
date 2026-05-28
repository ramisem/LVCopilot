/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sms;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.xml.PropertyList;

public class PackageTempStorage
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    public static final String PROPERTY_PACKAGEID = "packageid";
    public static final String PROPERTY_STORAGEUNITID = "storageunitid";
    public static final String PROPERTY_PROPSMATCH = "propsmatch";
    public static final String PROPERTY_AUDITREASON = "auditreason";
    public static final String PROPERTY_AUDITACTIVITY = "auditactivity";
    public static final String PROPERTY_AUDITSIGNEDFLAG = "auditsignedflag";
    public static final String PROPERTY_SDCRULECONFIRM = "__sdcruleconfirm";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.setParentStorageUnit(properties);
        this.setCurrentStorageUnit(properties);
    }

    private void setCurrentStorageUnit(PropertyList properties) throws SapphireException {
        String packageid = properties.getProperty(PROPERTY_PACKAGEID);
        String storageunitid = properties.getProperty(PROPERTY_STORAGEUNITID, "(null)");
        this.database.createPreparedResultSet("select trackitemid from trackitem where linksdcid = 'LV_Package' and linkkeyid1=?", new Object[]{packageid});
        if (this.database.getNext()) {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("trackitemid", this.database.getString("trackitemid"));
            actionProps.put("currentstorageunitid", storageunitid);
            actionProps.put(PROPERTY_PROPSMATCH, (String)properties.get(PROPERTY_PROPSMATCH));
            actionProps.put(PROPERTY_AUDITREASON, (String)properties.get(PROPERTY_AUDITREASON));
            actionProps.put(PROPERTY_AUDITACTIVITY, (String)properties.get(PROPERTY_AUDITACTIVITY));
            actionProps.put(PROPERTY_AUDITSIGNEDFLAG, (String)properties.get(PROPERTY_AUDITSIGNEDFLAG));
            actionProps.put(PROPERTY_SDCRULECONFIRM, (String)properties.get(PROPERTY_SDCRULECONFIRM));
            try {
                this.getActionProcessor().processAction("EditTrackItem", "1", actionProps);
                ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
            }
            catch (ActionException e) {
                this.setErrors(e.getErrorHandler());
            }
        }
        this.database.closeResultSet();
    }

    private void setParentStorageUnit(PropertyList properties) throws SapphireException {
        String packageid = properties.getProperty(PROPERTY_PACKAGEID);
        String storageunitid = properties.getProperty(PROPERTY_STORAGEUNITID, "(null)");
        this.database.createPreparedResultSet("select storageunitid from storageunit where linksdcid = 'LV_Package' and linkkeyid1=?", new Object[]{packageid});
        if (this.database.getNext()) {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("sdcid", "StorageUnitSDC");
            actionProps.put("keyid1", this.database.getString(PROPERTY_STORAGEUNITID));
            actionProps.put("parentid", storageunitid);
            actionProps.put(PROPERTY_PROPSMATCH, (String)properties.get(PROPERTY_PROPSMATCH));
            actionProps.put(PROPERTY_AUDITREASON, (String)properties.get(PROPERTY_AUDITREASON));
            actionProps.put(PROPERTY_AUDITACTIVITY, (String)properties.get(PROPERTY_AUDITACTIVITY));
            actionProps.put(PROPERTY_AUDITSIGNEDFLAG, (String)properties.get(PROPERTY_AUDITSIGNEDFLAG));
            actionProps.put(PROPERTY_SDCRULECONFIRM, (String)properties.get(PROPERTY_SDCRULECONFIRM));
            try {
                this.getActionProcessor().processAction("EditSDI", "1", actionProps);
                ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
            }
            catch (ActionException e) {
                this.setErrors(e.getErrorHandler());
            }
        }
        this.database.closeResultSet();
    }
}

