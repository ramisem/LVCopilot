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

public class EditSDCExport
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 60907 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet values = new DataSet();
        values.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), "+||+");
        values.addColumnValues("exportid", 0, properties.getProperty("exportid"), "+||+");
        values.addColumnValues("exportdesc", 0, properties.getProperty("exportdesc"), "+||+");
        values.addColumnValues("usersequence", 1, properties.getProperty("usersequence"), "+||+");
        values.addColumnValues("exportscript", 3, properties.getProperty("exportscript"), "+||+");
        for (int i = 0; i < values.size(); ++i) {
            if (values.getString(i, "exportdesc").equals("(null)")) {
                values.setString(i, "exportdesc", "");
            }
            if (!values.getString(i, "exportscript").equals("(null)")) continue;
            values.setString(i, "exportscript", "");
        }
        DataSetUtil.update(this.database, values, "sdcexport", new String[]{"sdcid", "exportid"});
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
    }
}

