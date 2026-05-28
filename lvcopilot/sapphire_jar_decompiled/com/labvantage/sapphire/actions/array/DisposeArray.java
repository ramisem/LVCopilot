/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DisposeArray
extends BaseAction
implements sapphire.action.DisposeArray {
    public static final String DELIMITER = ";";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String arrayid = properties.getProperty("arrayid");
        if (arrayid == null || arrayid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Array ID is mandatory"));
        }
        this.updateArrayStatus(properties, arrayid);
    }

    private void updateArrayStatus(PropertyList iproperties, String arrayid) throws SapphireException {
        String[] arrayidlist = StringUtil.split(arrayid, DELIMITER);
        String inClause = "";
        for (int i = 0; i < arrayidlist.length; ++i) {
            if (i != 0) {
                inClause = inClause + ", ";
            }
            inClause = inClause + "'" + arrayidlist[i] + "'";
        }
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT arrayid FROM array WHERE arrayid IN (" + safeSQL.addIn(inClause) + " ) and arraystatus <> 'Disposed'";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            ds.setString(0, "arraystatus", "Disposed");
            ds.padColumn("arraystatus");
            PropertyList properties = new PropertyList();
            properties.setProperty("sdcid", "LV_Array");
            properties.setProperty("keyid1", ds.getColumnValues("arrayid", DELIMITER));
            properties.setProperty("arraystatus", ds.getColumnValues("arraystatus", DELIMITER));
            properties.setProperty("auditsignedflag", iproperties.getProperty("auditsignedflag"));
            properties.setProperty("auditactivity", iproperties.getProperty("auditactivity"));
            properties.setProperty("auditreason", iproperties.getProperty("auditreason"));
            properties.setProperty("applylock", iproperties.getProperty("applylock", ""));
            this.getActionProcessor().processAction("EditSDI", "1", properties);
        }
    }
}

