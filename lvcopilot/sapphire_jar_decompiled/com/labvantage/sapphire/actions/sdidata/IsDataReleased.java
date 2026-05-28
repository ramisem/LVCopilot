/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.SDI;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class IsDataReleased
extends BaseAction
implements sapphire.action.IsDataReleased {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String dataset;
        String variantid;
        String paramlistversionid;
        SDI sdi = new SDI(properties.getProperty("sdcid"), properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
        if (!sdi.isValid()) {
            throw new SapphireException("NO_SDCID", "No sdcid specified");
        }
        properties.setProperty("datareleased", "No");
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer select = new StringBuffer("SELECT\tparamlistid FROM sdidataitem WHERE\tsdcid = ");
        select.append(safeSQL.addVar(sdi.getSdcid()));
        select.append(" AND\tkeyid1 =");
        select.append(safeSQL.addVar(sdi.getKeyid1()));
        select.append(" AND keyid2 =");
        select.append(safeSQL.addVar(sdi.getKeyid2()));
        select.append(" AND keyid3 =");
        select.append(safeSQL.addVar(sdi.getKeyid3()));
        String paramlistid = properties.getProperty("paramlistid");
        if (paramlistid.length() > 0) {
            select.append(" AND paramlistid = ");
            select.append(safeSQL.addVar(paramlistid));
        }
        if ((paramlistversionid = properties.getProperty("paramlistversionid")).length() > 0) {
            select.append(" AND paramlistversionid = ");
            select.append(safeSQL.addVar(paramlistversionid));
        }
        if ((variantid = properties.getProperty("variantid")).length() > 0) {
            select.append(" AND variantid = ");
            select.append(safeSQL.addVar(variantid));
        }
        if ((dataset = properties.getProperty("dataset")).length() > 0) {
            select.append(" AND dataset = ");
            select.append(safeSQL.addVar(dataset));
        }
        select.append(" AND ( releasedflag = 'N' OR releasedflag is null )");
        this.logger.info(select.toString());
        try {
            this.database.createPreparedResultSet(select.toString(), safeSQL.getValues());
            if (this.database.getNext()) {
                properties.setProperty("datareleased", "No");
            } else {
                properties.setProperty("datareleased", "Yes");
            }
        }
        catch (SapphireException sapphireException) {
            throw new SapphireException("CREATE_RESULTSET_FAILED", ErrorUtil.extractMessage("Failed to get result set. Reason: " + select, ErrorUtil.isUserAdmin(this.getConnectionId())), sapphireException);
        }
    }
}

