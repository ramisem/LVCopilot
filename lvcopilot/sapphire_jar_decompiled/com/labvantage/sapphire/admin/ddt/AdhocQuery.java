/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class AdhocQuery
extends BaseSDCRules {
    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT adhocqueryargid FROM adhocqueryarg WHERE ( adhocqueryid ) IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " )";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds.size() > 0) {
            StringBuffer argidlist = new StringBuffer();
            for (int i = 0; i < ds.size(); ++i) {
                argidlist.append(";").append(ds.getString(i, "adhocqueryargid"));
            }
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("sdcid", "AdhocQueryArg");
            props.put("keyid1", argidlist.substring(1));
            this.getActionProcessor().processAction("DeleteSDI", "1", props);
        }
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }
}

