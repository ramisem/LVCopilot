/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.statsmonitor;

import com.labvantage.sapphire.modules.statsmonitor.BaseMonitor;
import com.labvantage.sapphire.services.SapphireService;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.xml.PropertyList;

public class MonitorPings
extends BaseMonitor {
    @Override
    public double getValue(String itemid, String args) throws SapphireException {
        String connectionid = SapphireService.getInternalConnectionid(this.databaseid);
        long start = System.currentTimeMillis();
        if ("Database".equals(itemid)) {
            QueryProcessor qp = new QueryProcessor(connectionid);
            qp.getSqlDataSet("select * from sdc");
        } else if ("Action".equals(itemid)) {
            ActionProcessor ap = new ActionProcessor(connectionid);
            PropertyList props = new PropertyList();
            props.setProperty("beepmessage", "Monitor Action Ping");
            ap.processAction("Beep", "1", props);
        } else if ("ToDoList".equals(itemid)) {
            ActionProcessor ap = new ActionProcessor(connectionid);
            PropertyList props = new PropertyList();
            props.setProperty("actionid", "Beep");
            props.setProperty("actionversionid", "1");
            props.setProperty("beepmessage", "Monitor ToDoList Ping");
            ap.processAction("AddToDoListEntry", "1", props, true);
            String todolistid = props.getProperty("todolistid");
            QueryProcessor qp = new QueryProcessor(connectionid);
            boolean processed = false;
            int sleepTime = 100;
            while (!processed) {
                processed = qp.getPreparedCount("SELECT count(*) FROM todolist WHERE todolistid=?", new String[]{todolistid}) == 0;
                if (processed) continue;
                try {
                    Thread.sleep(sleepTime);
                    sleepTime += 100;
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                processed = System.currentTimeMillis() - start > 10000L;
            }
        } else {
            throw new SapphireException("Item " + itemid + " not recognized");
        }
        return System.currentTimeMillis() - start;
    }
}

