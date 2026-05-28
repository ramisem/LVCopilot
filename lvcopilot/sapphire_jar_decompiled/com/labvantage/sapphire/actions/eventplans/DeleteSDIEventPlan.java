/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eventplans;

import com.labvantage.opal.handler.ErrorUtil;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteSDIEventPlan
extends BaseAction
implements sapphire.action.DeleteSDIEventPlan {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String rsetid = properties.getProperty("rsetid");
        if (rsetid == null) {
            rsetid = "";
        }
        boolean deleterset = false;
        DAMProcessor dam = this.getDAMProcessor();
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            rsetid = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3")) : dam.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
            if (rsetid.length() == 0) {
                throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for edit");
            }
            deleterset = true;
        }
        String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
        String[] eventplanidprop = StringUtil.split(properties.getProperty("eventplanid"), ";");
        String[] eventplanversionidprop = StringUtil.split(properties.getProperty("eventplanversionid"), ";");
        String[] eventplansdcidprop = StringUtil.split(properties.getProperty("eventplansdcid"), ";");
        if (keyid1prop.length == eventplanidprop.length && eventplanidprop.length == eventplanversionidprop.length) {
            try {
                for (int i = 0; i < keyid1prop.length; ++i) {
                    Object[] bindVars = new Object[]{sdcid, keyid1prop[i], keyid2prop.length > i && keyid2prop[i].length() > 0 ? keyid2prop[i] : "(null)", keyid3prop.length > i && keyid3prop[i].length() > 0 ? keyid3prop[i] : "(null)", eventplanidprop[i], eventplanversionidprop[i], eventplansdcidprop[i]};
                    this.database.executePreparedUpdate("DELETE FROM sdieventplanitemproperty WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND eventplanid = ? AND eventplanversionid = ? AND eventplansdcid = ?", bindVars);
                    this.database.executePreparedUpdate("DELETE FROM sdieventplanitem WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND eventplanid = ? AND eventplanversionid = ? AND eventplansdcid = ?", bindVars);
                    this.database.executePreparedUpdate("DELETE FROM sdieventplan WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND eventplanid = ? AND eventplanversionid = ? AND eventplansdcid = ?", bindVars);
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to delete sdieventplan records. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        if (deleterset) {
            dam.clearRSet(rsetid);
        }
    }
}

