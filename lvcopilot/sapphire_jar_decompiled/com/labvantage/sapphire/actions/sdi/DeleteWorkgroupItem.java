/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.util.StringHolder;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteWorkgroupItem
extends BaseAction
implements sapphire.action.DeleteWorkgroupItem {
    private static final String PROPERTY_SEPARATOR = "separator";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DAMProcessor damProcessor = this.getDAMProcessor();
        String separator = properties.getProperty(PROPERTY_SEPARATOR, ";");
        StringHolder rsetidHolder = new StringHolder();
        int rc = damProcessor.createRSet("Workgroup", (String)properties.get("workgroupid"), "", "", rsetidHolder);
        if (rc == 1) {
            int i;
            String rsetid = rsetidHolder.value;
            String[] workgroupid = StringUtil.split(properties.getProperty("workgroupid"), separator);
            String[] workgroupitemid = StringUtil.split(properties.getProperty("workgroupitemid"), separator);
            String[] itemkeyid = StringUtil.split(properties.getProperty("itemkeyid"), separator);
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer delete = new StringBuffer("DELETE FROM workgroupitem WHERE workgroupid=");
            delete.append(safeSQL.addVar(workgroupid[0]));
            delete.append(" ");
            if (workgroupitemid.length > 0 && workgroupitemid[0].length() > 0) {
                delete.append(" AND ( workgroupitemid = ");
                delete.append(safeSQL.addVar(workgroupitemid[0]));
                delete.append(" ");
                for (i = 1; i < workgroupitemid.length; ++i) {
                    delete.append(" OR workgroupitemid = ");
                    delete.append(safeSQL.addVar(workgroupitemid[i]));
                    delete.append(" ");
                }
                delete.append(")");
            } else if (itemkeyid.length > 0 && itemkeyid[0].length() > 0) {
                delete.append(" AND ( keyid1 = ");
                delete.append(safeSQL.addVar(itemkeyid[0]));
                delete.append(" ");
                for (i = 1; i < itemkeyid.length; ++i) {
                    delete.append(" OR keyid1 = ");
                    delete.append(safeSQL.addVar(itemkeyid[i]));
                    delete.append(" ");
                }
                delete.append(")");
            }
            this.logger.info("Deleting workgroupitems with: " + delete.toString());
            try {
                this.database.executePreparedUpdate(delete.toString(), safeSQL.getValues());
            }
            catch (SapphireException e) {
                throw new SapphireException("DB_UPDATE_FAILED", "Failed to delete workgroup items using: " + delete.toString(), e);
            }
            finally {
                if (rsetid != null) {
                    damProcessor.clearRSet(rsetid);
                }
            }
        } else {
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create rset");
        }
    }
}

