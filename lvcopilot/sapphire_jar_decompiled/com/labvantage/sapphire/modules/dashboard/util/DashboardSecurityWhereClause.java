/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.util;

import com.labvantage.sapphire.services.QueryService;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class DashboardSecurityWhereClause
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        SapphireConnection sc = this.getConnectionProcessor().getSapphireConnection();
        if (sc.getConnection() == null) {
            sc.setConnection(this.database.getConnection());
        }
        QueryService queryService = new QueryService(sc);
        try {
            properties.setProperty("whereclause", queryService.getSecurityFilterWhere(properties.getProperty("sdcid", "")));
        }
        catch (Exception e) {
            this.logger.warn("Failed to process where clause");
            properties.setProperty("whereclause", "");
        }
    }
}

