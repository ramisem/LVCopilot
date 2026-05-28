/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.search;

import com.labvantage.sapphire.DateTimeUtil;
import java.sql.Timestamp;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class UpdateSearchOperation
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String searchid = properties.getProperty("searchid");
        String searchindexitem = properties.getProperty("searchindexitem");
        String searchoperation = properties.getProperty("searchoperation");
        String operationitemtype = properties.getProperty("operationitemtype");
        int searchrank = -1;
        try {
            searchrank = Integer.parseInt(properties.getProperty("searchrank"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        Timestamp now = DateTimeUtil.getNowTimestamp();
        if (searchid.length() == 0) {
            searchid = String.valueOf(this.getSequenceProcessor().getSequence("search", "searchid"));
            String enteredquery = properties.getProperty("enteredquery");
            String term = properties.getProperty("selectedterm");
            String doctermfreq = properties.getProperty("doctermfreq");
            this.getQueryProcessor().execSQL(10301, new Object[]{searchid, now, "S", this.connectionInfo.getSysuserId(), enteredquery, term, 0, 0, 0, doctermfreq});
        }
        this.database.executePreparedUpdate("INSERT INTO searchoperation ( searchid, operationdt, operation, operationitemtype, indexitem, searchrank ) VALUES ( ?, ?, ?, ?, ?, ? )", new Object[]{searchid, now, searchoperation, operationitemtype, searchindexitem, searchrank});
    }
}

