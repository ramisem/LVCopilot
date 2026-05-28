/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.stability;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.util.StringUtil;

public class PPPGridPropertyHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        block8: {
            String protocolPlanRows;
            if (Trace.on) {
                Trace.log("Processing properties for protocol product plan grid...");
            }
            if ((protocolPlanRows = (String)props.get("__protocol_scheduleplan_rows")) != null && protocolPlanRows.length() > 0) {
                if (Trace.on) {
                    Trace.log(protocolPlanRows + " protocol plan rows found");
                }
                try {
                    int rows = Integer.parseInt(protocolPlanRows);
                    StringBuffer schedulePlans = new StringBuffer();
                    for (int i = 0; i < rows; ++i) {
                        if (props.get("__protocol_scheduleplan" + i + "_rs") == null || !((String)props.get("__protocol_scheduleplan" + i + "_rs")).equals("D")) continue;
                        String[] keys = StringUtil.split((String)props.get("__protocol_scheduleplan" + i + "_key"), ";");
                        schedulePlans.append(";" + keys[2]);
                    }
                    if (schedulePlans.length() <= 0) break block8;
                    ActionProcessor ap = new ActionProcessor(this.connectionInfo.getConnectionId());
                    HashMap<String, String> actionProps = new HashMap<String, String>();
                    actionProps.put("sdcid", "SchedulePlan");
                    actionProps.put("keyid1", schedulePlans.substring(1));
                    ap.processAction("DeleteSDI", "1", actionProps);
                }
                catch (NumberFormatException nfe) {
                    throw new SapphireException("Invalid property __protocol_scheduleplan_rows");
                }
            }
        }
    }
}

