/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.ddt;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class EditSDCOperation
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 60907 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet values = new DataSet();
        values.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), ";");
        values.addColumnValues("operationid", 0, properties.getProperty("operationid"), ";");
        values.addColumnValues("operationdesc", 0, properties.getProperty("operationdesc"), ";");
        values.addColumnValues("usersequence", 1, properties.getProperty("usersequence"), ";");
        for (int i = 0; i < values.size(); ++i) {
            if (!values.getString(i, "operationdesc").equals("(null)")) continue;
            values.setString(i, "operationdesc", "");
        }
        DataSetUtil.update(this.database, values, "sdcoperation", new String[]{"sdcid", "operationid"});
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
    }
}

