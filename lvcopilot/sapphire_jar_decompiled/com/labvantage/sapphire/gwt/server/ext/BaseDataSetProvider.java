/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.gwt.server.ext;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.gwt.server.ext.DataSetProvider;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DBAccess;

public abstract class BaseDataSetProvider
implements DataSetProvider {
    protected SapphireConnection sapphireConnection;
    protected QueryProcessor queryProcessor;
    protected TranslationProcessor translationProcessor;
    protected DBAccess database;

    public void setSapphireConnection(SapphireConnection sapphireConnection) {
        this.sapphireConnection = sapphireConnection;
        this.queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
        this.translationProcessor = new TranslationProcessor(sapphireConnection.getConnectionId());
    }

    public void setDatabase(DBUtil dbu) {
        this.database = dbu;
    }
}

