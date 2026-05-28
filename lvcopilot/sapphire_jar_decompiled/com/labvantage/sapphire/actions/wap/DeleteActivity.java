/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.wap;

import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class DeleteActivity
extends BaseAction
implements sapphire.action.DeleteActivity {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        WAPCommands commands = new WAPCommands(this.getConnectionid());
        String activityid = properties.getProperty("activityid");
        if (activityid.length() == 0) {
            activityid = properties.getProperty("keyid1");
        }
        commands.deleteActivities(activityid);
    }
}

