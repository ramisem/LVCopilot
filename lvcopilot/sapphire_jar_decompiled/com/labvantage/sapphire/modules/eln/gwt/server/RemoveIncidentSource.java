/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import sapphire.SapphireException;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class RemoveIncidentSource
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sourcesdcid = properties.getProperty("sourcesdcid");
        String sourcekeyid1 = properties.getProperty("sourcekeyid1");
        String sourcekeyid2 = properties.getProperty("sourcekeyid2");
        String sourcekeyid3 = properties.getProperty("sourcekeyid3");
        SafeSQL safeSQL = new SafeSQL();
        String sql = "DELETE FROM incidentitem WHERE sourcesdcid=" + safeSQL.addVar(sourcesdcid) + " AND sourcekeyid1=" + safeSQL.addVar(sourcekeyid1) + (sourcekeyid2.length() > 0 ? " AND sourcekeyid2=" + safeSQL.addVar(sourcekeyid2) : "") + (sourcekeyid3.length() > 0 ? " AND sourcekeyid3=" + safeSQL.addVar(sourcekeyid3) : "");
        this.database.executePreparedUpdate(sql, safeSQL.getValues());
    }
}

