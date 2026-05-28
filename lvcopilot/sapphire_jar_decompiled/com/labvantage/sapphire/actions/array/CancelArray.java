/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.sapphire.actions.array.ArrayUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CancelArray
extends BaseAction
implements sapphire.action.CancelArray {
    public static final String DELIMITER = ";";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String arrayid = properties.getProperty("arrayid");
        if (arrayid == null || arrayid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Array ID is mandatory"));
        }
        String whereClause = "";
        String arraymethodid = "";
        boolean currentinstanceonly = !"N".equals(properties.getProperty("currentinstanceonly", "N"));
        String instance = "";
        if (currentinstanceonly) {
            String tokens = ArrayUtil.getLastArrayMethodItem(this.getQueryProcessor(), arrayid);
            if (tokens.length() > 0) {
                String[] tok = StringUtil.split(tokens, "|");
                arraymethodid = tok[0];
                instance = tok[1];
            }
        } else {
            arraymethodid = properties.getProperty("arraymethodid", "");
            instance = properties.getProperty("arraymethodinstance", "");
        }
        if (arraymethodid.length() > 0) {
            whereClause = whereClause + " arraymethodid = '" + arraymethodid + "'";
        }
        if (instance.length() > 0) {
            if (whereClause.length() > 0) {
                whereClause = whereClause + " AND ";
            }
            whereClause = whereClause + " arraymethodinstance ='" + instance + "'";
        }
        this.updateArrayArrayMethodItemStatus(properties, arrayid, whereClause);
    }

    private void updateArrayArrayMethodItemStatus(PropertyList properties, String arrayid, String whereClause) throws SapphireException {
        String[] arrayidlist = StringUtil.split(arrayid, DELIMITER);
        String sql = "SELECT arrayid, arraymethodid, arraymethodversionid, arraymethodinstance, arraymethoditemstatus FROM arrayarraymethoditem WHERE arrayid = ? ";
        if (whereClause.length() > 0) {
            sql = sql + " AND " + whereClause;
        }
        for (int i = 0; i < arrayidlist.length; ++i) {
            SafeSQL safeSQL = new SafeSQL();
            safeSQL.addVar(arrayidlist[i]);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.getRowCount() > 0) {
                if (!ArrayUtil.validateArrayOperation(this.getQueryProcessor(), arrayid, "CancelArray")) {
                    throw new SapphireException(this.getTranslationProcessor().translate("Invalid status. Cannot cancel array:") + arrayidlist[i]);
                }
                ds.setString(0, "newarraymethoditemstatus", "Cancelled");
                ds.padColumn("newarraymethoditemstatus");
                PropertyList arrayarraymethoditemprops = new PropertyList();
                arrayarraymethoditemprops.setProperty("sdcid", "LV_Array");
                arrayarraymethoditemprops.setProperty("linkid", "Array ArrayMethod Item");
                arrayarraymethoditemprops.setProperty("arrayid", arrayidlist[i]);
                arrayarraymethoditemprops.setProperty("arraymethodid", ds.getColumnValues("arraymethodid", DELIMITER));
                arrayarraymethoditemprops.setProperty("arraymethodversionid", ds.getColumnValues("arraymethodversionid", DELIMITER));
                arrayarraymethoditemprops.setProperty("arraymethodinstance", ds.getColumnValues("arraymethodinstance", DELIMITER));
                arrayarraymethoditemprops.setProperty("arraymethoditemstatus", ds.getColumnValues("newarraymethoditemstatus", DELIMITER));
                arrayarraymethoditemprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                arrayarraymethoditemprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
                arrayarraymethoditemprops.setProperty("auditreason", properties.getProperty("auditreason"));
                this.getActionProcessor().processAction("EditSDIDetail", "1", arrayarraymethoditemprops);
                continue;
            }
            PropertyList arrayprops = new PropertyList();
            arrayprops.setProperty("sdcid", "LV_Array");
            arrayprops.setProperty("keyid1", arrayidlist[i]);
            arrayprops.setProperty("arraystatus", "Cancelled");
            arrayprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            arrayprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
            arrayprops.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("EditSDI", "1", arrayprops);
        }
    }
}

