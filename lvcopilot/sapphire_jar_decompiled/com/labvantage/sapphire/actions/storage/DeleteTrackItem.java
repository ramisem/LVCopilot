/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.storage;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.xml.PropertyList;

public class DeleteTrackItem
extends BaseAction
implements sapphire.action.DeleteTrackItem {
    public static final String TRACKITEMSDCID = "TrackItemSDC";
    public static final String ID = "EditTrackItem";
    public static final String VERSION = "1";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        HashMap<String, String> actionProps = new HashMap<String, String>();
        actionProps.put("sdcid", TRACKITEMSDCID);
        actionProps.put("keyid1", (String)properties.get("trackitemid"));
        for (String propertyid : properties.keySet()) {
            if (propertyid.equals("trackitemid")) continue;
            actionProps.put(propertyid, properties.getProperty(propertyid));
        }
        try {
            this.getActionProcessor().processAction("DeleteSDI", VERSION, actionProps);
            ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                this.setErrors(this.getActionProcessor().getErrorHandler());
            }
        }
        catch (ActionException e) {
            this.setErrors(e.getErrorHandler());
        }
    }
}

