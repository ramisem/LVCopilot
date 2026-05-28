/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.exception;

import sapphire.accessor.ActionException;
import sapphire.util.ActionBlock;

public class ActionBlockException
extends ActionException {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private String __ActionProcessorMsg;
    private ActionBlock __ActionBlock;

    public ActionBlockException(String msg) {
        super(msg);
    }

    public ActionBlockException(String actionname, int actionindex, String msg) {
        super(actionname, actionindex, msg);
    }

    public ActionBlockException(String actionname, int actionindex, String msg, ActionBlock actionblock, String actionProcessorMsg) {
        super(actionname, actionindex, msg);
        this.__ActionBlock = actionblock;
        this.__ActionProcessorMsg = new String(actionProcessorMsg);
    }

    public ActionBlock getActionBlock() {
        return this.__ActionBlock;
    }

    public String getActionProcessorMessage() {
        return this.__ActionProcessorMsg;
    }
}

