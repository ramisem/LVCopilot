/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.search;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;

public class ProcessBacklog
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        Indexer indexer = Indexer.getInstance(this.connectionInfo.getDatabaseId());
        if (indexer != null && indexer.getCurrentIndexingStatus().equals("backlog")) {
            int iteration = Integer.parseInt(properties.getProperty("iteration", "1"));
            if (properties.getProperty("createbacklog").equals("Y")) {
                int rowcount = Integer.parseInt(properties.getProperty("rowcount"));
                this.logger.info("Creating backlog");
                indexer.createBacklog(properties.getProperty("sdcid"), properties.getProperty("aftercreatedt"), properties.getProperty("extendedwhere"), rowcount, properties.getProperty("orderby"));
            } else {
                int rowcount = Integer.parseInt(properties.getProperty("rowcount", "1000"));
                this.logger.info("Processing backlog iteration: " + iteration);
                indexer.processBacklog(rowcount);
            }
            if (this.database.getPreparedCount("SELECT count(*) FROM indexmap WHERE indexflag = ?", new Object[]{"B"}) > 0) {
                ProcessBacklog.addToTDL(new SapphireConnection(this.database.getConnection(), this.connectionInfo), iteration + 1, new PropertyList());
            } else {
                Indexer.stopBacklogIndexing(this.connectionInfo);
            }
        }
    }

    public static void addToTDL(SapphireConnection sapphireConnection, int iteration, PropertyList backlogProps) throws SapphireException {
        try {
            AutomationService automationService = new AutomationService(sapphireConnection);
            ActionBlock ab = new ActionBlock();
            backlogProps.setProperty("iteration", String.valueOf(iteration));
            ab.setActionClass("ProcessBacklog", ProcessBacklog.class.getName(), backlogProps);
            automationService.addToDoListEntry("ProcessBacklog", ab, "n", true, "", "");
        }
        catch (Exception e) {
            throw new SapphireException("Failed to add backlog request to TDL. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
    }
}

