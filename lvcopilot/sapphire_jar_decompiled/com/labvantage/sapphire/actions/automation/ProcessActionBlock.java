/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.automation;

import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;

public class ProcessActionBlock
extends BaseAction
implements sapphire.action.ProcessActionBlock {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String actionBlockName = properties.getProperty("actionblockname");
        String actionBlockXML = properties.getProperty("actionblockxml");
        ActionBlock actionBlock = new ActionBlock(actionBlockName, actionBlockXML);
        ActionProcessor actionProcessor = this.getActionProcessor();
        actionProcessor.processActionBlock(actionBlock);
    }
}

