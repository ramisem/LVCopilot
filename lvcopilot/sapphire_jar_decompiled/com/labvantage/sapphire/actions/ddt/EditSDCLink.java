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

public class EditSDCLink
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 60907 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet newValues = new DataSet();
        newValues.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), ";");
        newValues.addColumnValues("linkid", 0, properties.getProperty("linkid"), ";");
        newValues.addColumnValues("deleteflag", 0, properties.getProperty("deleteflag"), ";");
        newValues.addColumnValues("usersequence", 1, properties.getProperty("usersequence"), ";");
        for (int i = 0; i < newValues.size(); ++i) {
            if (newValues.getString(i, "deleteflag") != null && !newValues.getString(i, "deleteflag").equals("(null)") && newValues.getString(i, "deleteflag").length() <= 1) continue;
            newValues.setString(i, "deleteflag", "");
        }
        DataSetUtil.update(this.database, newValues, "sdclink", new String[]{"sdcid", "linkid"});
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
    }
}

