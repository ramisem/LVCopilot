/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.spec;

import com.labvantage.opal.handler.ErrorUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class GetSDISpecRuleValue
extends BaseAction
implements sapphire.action.GetSDISpecRuleValue {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid", "(null)");
        String keyid1 = properties.getProperty("keyid1", "(null)");
        String keyid2 = properties.getProperty("keyid2", "(null)");
        String keyid3 = properties.getProperty("keyid3", "(null)");
        String specid = properties.getProperty("specid");
        String specversionid = properties.getProperty("specversionid", "1");
        String rulenumber = properties.getProperty("ruleno", "1");
        SafeSQL safeSQL = new SafeSQL();
        String sql = new StringBuffer().append("SELECT rulevalue FROM sdispecrule where sdcid=").append(safeSQL.addVar(sdcid)).append(" and keyid1=").append(safeSQL.addVar(keyid1)).append(" and keyid2=").append(safeSQL.addVar(keyid2)).append(" and keyid3=").append(safeSQL.addVar(keyid3)).append(" and specid=").append(safeSQL.addVar(specid)).append(" and specversionid=").append(safeSQL.addVar(specversionid)).append(" and ruleno=").append(safeSQL.addVar(rulenumber)).toString();
        try {
            String rulevalue;
            this.database.createPreparedResultSet(sql, safeSQL.getValues());
            if (this.database.getNext()) {
                rulevalue = this.database.getString("rulevalue");
                if (rulevalue == null) {
                    rulevalue = "";
                }
            } else {
                throw new SapphireException("EMPTY_RESULTSET", "Failed to find any results for " + sql);
            }
            properties.setProperty("value", rulevalue);
        }
        catch (SapphireException e) {
            throw new SapphireException("CREATE_RESULTSET_FAILED", ErrorUtil.extractMessage("Failed to create any resultset. Reason: " + sql, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }
}

