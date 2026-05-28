/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.storage;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ReserveTrackItem
extends BaseAction
implements sapphire.action.ReserveTrackItem {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String[] trackitemidprop = StringUtil.split(properties.getProperty("trackitemid"), ";");
        String[] storageunitidprop = StringUtil.split(properties.getProperty("storageunitid"), ";");
        boolean propsmatch = "Y".equalsIgnoreCase(properties.getProperty("propsmatch"));
        DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
        DataSet dsinsert = new DataSet();
        dsinsert.addColumn("trackitemid", 0);
        dsinsert.addColumn("storageunitid", 0);
        dsinsert.addColumn("createby", 0);
        dsinsert.addColumn("createdt", 2);
        dsinsert.addColumn("createtool", 0);
        if (propsmatch) {
            if (trackitemidprop.length != storageunitidprop.length) throw new SapphireException("INVALID_PROPERTIES", "There must be a storageunitid for each trackitem");
            for (int i = 0; i < trackitemidprop.length; ++i) {
                String trackitemid = trackitemidprop[i];
                String storageunitid = storageunitidprop[i];
                if (trackitemid.length() <= 0 || storageunitid.length() <= 0 || !this.validateReserve(storageunitid, trackitemid)) continue;
                int row = dsinsert.addRow();
                dsinsert.setValue(row, "trackitemid", trackitemid);
                dsinsert.setValue(row, "storageunitid", storageunitid);
            }
        } else {
            for (String trackitemid : trackitemidprop) {
                for (String storageunitid : storageunitidprop) {
                    if (trackitemid.length() <= 0 || storageunitid.length() <= 0 || !this.validateReserve(storageunitid, trackitemid)) continue;
                    int row = dsinsert.addRow();
                    dsinsert.setValue(row, "trackitemid", trackitemid);
                    dsinsert.setValue(row, "storageunitid", storageunitid);
                }
            }
        }
        if (dsinsert.size() > 0) {
            dsinsert.setValue(0, "createby", this.connectionInfo.getSysuserId());
            dsinsert.setDate(0, "createdt", dtu.getCalendar("Now"));
            dsinsert.setValue(0, "createtool", "ReserveTrackItem");
            dsinsert.padColumns();
            DataSetUtil.insert(this.database, dsinsert, "reservestorageunit");
        }
        dsinsert.reset();
    }

    private boolean validateReserve(String storageunitid, String trackitemid) {
        boolean valid = true;
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select count(trackitemid) count from reservestorageunit where trackitemid = ? and storageunitid = ?", (Object[])new String[]{trackitemid, storageunitid});
        if (ds != null && ds.size() > 0) {
            boolean bl = valid = ds.getInt(0, "count") <= 0;
        }
        if (valid && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select trackitemallowedflag from storageunit where storageunitid = ?", (Object[])new String[]{storageunitid})) != null && ds.size() > 0) {
            valid = !"N".equals(ds.getValue(0, "trackitemallowedflag"));
        }
        return valid;
    }
}

