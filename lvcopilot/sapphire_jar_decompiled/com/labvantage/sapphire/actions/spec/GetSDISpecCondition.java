/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.spec;

import com.labvantage.opal.handler.ErrorUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class GetSDISpecCondition
extends BaseAction
implements sapphire.action.GetSDISpecCondition {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1", "(null)");
        String keyid2 = properties.getProperty("keyid2", "(null)");
        String keyid3 = properties.getProperty("keyid3", "(null)");
        String specid = properties.getProperty("specid");
        String specversionid = properties.getProperty("specversionid", "1");
        SafeSQL safeSQL = new SafeSQL();
        String sql = new StringBuffer().append("SELECT condition FROM sdispec where sdcid=").append(safeSQL.addVar(sdcid)).append(" and keyid1=").append(safeSQL.addVar(keyid1)).append(" and keyid2=").append(safeSQL.addVar(keyid2)).append(" and keyid3=").append(safeSQL.addVar(keyid3)).append(" and specid=").append(safeSQL.addVar(specid)).append(" and specversionid=").append(safeSQL.addVar(specversionid)).toString();
        try {
            String condition;
            this.database.createPreparedResultSet(sql, safeSQL.getValues());
            if (this.database.getNext()) {
                condition = this.database.getString("condition");
                if (condition == null) {
                    condition = "";
                }
            } else {
                throw new SapphireException("EMPTY_RESULTSET", "Failed to find any results for " + sql);
            }
            properties.setProperty("condition", condition);
            this.logger.info("Setting condition to " + condition);
        }
        catch (SapphireException e) {
            throw new SapphireException("CREATE_RESULTSET_FAILED", ErrorUtil.extractMessage("Failed to create any resultset. Reason: " + sql, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }
}

