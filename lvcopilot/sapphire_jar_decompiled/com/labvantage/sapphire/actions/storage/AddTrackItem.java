/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.storage;

import com.labvantage.sapphire.actions.sdi.AddSDI;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.xml.PropertyList;

public class AddTrackItem
extends BaseAction
implements sapphire.action.AddTrackItem {
    public static String LABVANTAGE_CVS_ID = "$Revision: 65039 $";
    public static final String TRACKITEMSDCID = "TrackItemSDC";
    public static final String ID = "AddTrackItem";
    public static final String VERSION = "1";
    public static final String PROPERTY_SDCRULECONFIRM = "__sdcruleconfirm";

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", TRACKITEMSDCID);
        props.setProperty("linksdcid", actionProps.getProperty("sdcid"));
        props.setProperty("linkkeyid1", actionProps.getProperty("keyid1"));
        props.setProperty("linkkeyid2", actionProps.getProperty("keyid2"));
        props.setProperty("linkkeyid3", actionProps.getProperty("keyid3"));
        props.setProperty("qtycurrent", actionProps.getProperty("quantity"));
        props.setProperty("qtycurrenttype", actionProps.getProperty("quantitytype"));
        props.setProperty("qtyunits", actionProps.getProperty("quantityunit"));
        props.setProperty("currentstorageunitid", actionProps.getProperty("location"));
        props.setProperty("copies", actionProps.getProperty("numoftrackitems"));
        actionProps.remove("sdcid");
        actionProps.remove("keyid1");
        actionProps.remove("keyid2");
        actionProps.remove("keyid3");
        actionProps.remove("quantity");
        actionProps.remove("quantitytype");
        actionProps.remove("quantityunit");
        actionProps.remove("location");
        actionProps.remove("numoftrackitems");
        for (Object key : actionProps.keySet()) {
            if (!(key instanceof String)) continue;
            String property = (String)key;
            props.setProperty(property, actionProps.getProperty(property));
        }
        try {
            this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
            ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                this.setErrors(this.getActionProcessor().getErrorHandler());
            }
        }
        catch (ActionException e) {
            this.setErrors(e.getErrorHandler());
        }
        actionProps.setProperty("newkeyid1", props.getProperty("newkeyid1"));
    }
}

