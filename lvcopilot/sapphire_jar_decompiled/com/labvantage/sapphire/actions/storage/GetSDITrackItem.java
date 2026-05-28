/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.storage;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetSDITrackItem
extends BaseAction
implements sapphire.action.GetSDITrackItem {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        StringBuilder sb = new StringBuilder();
        String sdcid = properties.getProperty("sdcid", "").trim();
        String keyid1 = properties.getProperty("keyid1", "").trim();
        String keyid2 = properties.getProperty("keyid2", "").trim();
        String keyid3 = properties.getProperty("keyid3", "").trim();
        String mode = properties.getProperty("mode", "children");
        if (StringUtil.getLen(sdcid) > 0L && StringUtil.getLen(keyid1) > 0L) {
            if (mode.equals("children")) {
                SafeSQL safeSQL = new SafeSQL();
                StringBuilder sql = new StringBuilder();
                sql.append("select trackitem.trackitemid from trackitem where trackitem.currentstorageunitid = (");
                sql.append(" select storageunit.storageunitid from storageunit");
                sql.append(" where storageunit.linksdcid = ").append(safeSQL.addVar(sdcid));
                sql.append(" and storageunit.linkkeyid1 = ").append(safeSQL.addVar(keyid1));
                if (StringUtil.getLen(keyid2) > 0L) {
                    sql.append(" and storageunit.linkkeyid2 = ").append(safeSQL.addVar(keyid2));
                }
                if (StringUtil.getLen(keyid3) > 0L) {
                    sql.append(" and storageunit.linkkeyid3 = ").append(safeSQL.addVar(keyid3)).append(")");
                }
                sql.append(")");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null) {
                    sb.append(ds.getColumnValues("trackitemid", ";"));
                }
            } else if (mode.equals("self")) {
                SafeSQL safeSQL = new SafeSQL();
                StringBuilder sql = new StringBuilder();
                sql.append("select trackitemid from trackitem");
                sql.append(" where linksdcid = ").append(safeSQL.addVar(sdcid));
                sql.append(" and linkkeyid1 in (").append(safeSQL.addIn(keyid1, ";")).append(")");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    sb.append(ds.getColumnValues("trackitemid", ";"));
                }
            }
        } else {
            throw new SapphireException("INVALID_SDI", "Invalid input sdi: SDC: " + sdcid + ", KEYID1: " + keyid1 + ", KEYID2: " + keyid2 + ", KEYID3: " + keyid3);
        }
        properties.setProperty("trackitemid", sb.toString());
    }
}

