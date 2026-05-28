/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.workflow.gwt.server.steprequests;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;

public abstract class BaseStepRequest {
    protected SapphireConnection sapphireConnection;
    protected DBUtil dbu;

    public void init(SapphireConnection sapphireConnection, DBUtil dbu) {
        this.sapphireConnection = sapphireConnection;
        this.dbu = dbu;
    }

    public abstract void executeRequest(CommandRequest var1, CommandResponse var2) throws SapphireException;
}

