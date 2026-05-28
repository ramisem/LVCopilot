/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class IncrementCounter
extends BaseAction
implements sapphire.action.IncrementCounter {
    @Override
    public boolean isDatabaseRequired() {
        return false;
    }

    @Override
    public void processAction(PropertyList properties) {
        int outputcounter;
        String inputcounter = properties.getProperty("inputcounter");
        try {
            outputcounter = Integer.parseInt(inputcounter) + 1;
        }
        catch (Exception e) {
            outputcounter = 1;
        }
        properties.setProperty("outputcounter", Integer.toString(outputcounter));
    }
}

