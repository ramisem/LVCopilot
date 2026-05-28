/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.sdi.AddSDINote;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SDINotePropertyHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        String jsonNotes;
        String auditActivity = (String)props.get("auditactivity");
        String buttonActivity = (String)props.get("buttonactivity");
        if (auditActivity == null) {
            auditActivity = "";
        }
        if (buttonActivity == null) {
            buttonActivity = "";
        }
        if ((jsonNotes = (String)props.get("__sdinotes_properties")) != null && jsonNotes.length() > 0) {
            try {
                ActionProcessor actionProcessor = new ActionProcessor(this.connectionInfo.getConnectionId());
                PropertyList actionProps = new PropertyList();
                DataSet sdinotes = new DataSet(new JSONObject(jsonNotes));
                for (int i = 0; i < sdinotes.size(); ++i) {
                    actionProps.setProperty("sdcid", sdinotes.getValue(i, "sdcid"));
                    actionProps.setProperty("keyid1", sdinotes.getValue(i, "keyid1"));
                    actionProps.setProperty("keyid2", sdinotes.getValue(i, "keyid2"));
                    actionProps.setProperty("keyid3", sdinotes.getValue(i, "keyid3"));
                    actionProps.setProperty("note", sdinotes.getValue(i, "note"));
                    actionProps.setProperty("notetype", sdinotes.getValue(i, "notetypeflag", "S"));
                    actionProps.setProperty("activity", buttonActivity.trim().length() > 0 ? buttonActivity.trim() : auditActivity.trim());
                    actionProcessor.processActionClass(AddSDINote.class.getName(), actionProps);
                }
            }
            catch (JSONException e) {
                throw new SapphireException("Failed to save SDINotes. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
        }
    }
}

