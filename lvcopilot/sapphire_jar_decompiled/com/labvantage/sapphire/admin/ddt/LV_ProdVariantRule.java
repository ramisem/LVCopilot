/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LV_ProdVariantRule
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53818 $";

    @Override
    public void preDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        String stateids = actionProps.getProperty("s_stateid", "");
        String prodvarruleid = actionProps.getProperty("keyid1", "");
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT currentstateid FROM s_prodvariant WHERE prodvariantruleid = " + safeSQL.addVar(prodvarruleid));
        sql.append(" AND currentstateid IN ( ").append(safeSQL.addIn(stateids, ";")).append(" )");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Cannot delete state(s) for the rule " + prodvarruleid + ". Child record(s) found."));
        }
    }
}

