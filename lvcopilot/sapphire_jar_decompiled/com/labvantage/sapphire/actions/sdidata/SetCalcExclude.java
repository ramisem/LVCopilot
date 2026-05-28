/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SetCalcExclude
extends BaseAction
implements sapphire.action.SetCalcExclude {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2", "(null)");
        String keyid3 = properties.getProperty("keyid3", "(null)");
        String paramlistid = properties.getProperty("paramlistid");
        String paramlistversionid = properties.getProperty("paramlistversionid");
        String variantid = properties.getProperty("variantid");
        String dataset = properties.getProperty("dataset");
        String paramid = properties.getProperty("paramid");
        String paramtype = properties.getProperty("paramtype");
        String replicateid = properties.getProperty("replicateid");
        String excludeflag = properties.getProperty("excludeflag", "Y");
        DataSet ds = new DataSet();
        ds.addColumnValues("excludeflag", 0, excludeflag, ";");
        ds.addColumnValues("sdcid", 0, sdcid, ";");
        ds.addColumnValues("keyid1", 0, keyid1, ";");
        ds.addColumnValues("keyid2", 0, keyid2, ";");
        ds.addColumnValues("keyid3", 0, keyid3, ";");
        ds.addColumnValues("paramlistid", 0, paramlistid, ";");
        ds.addColumnValues("paramlistversionid", 0, paramlistversionid, ";");
        ds.addColumnValues("variantid", 0, variantid, ";");
        ds.addColumnValues("dataset", 0, dataset, ";");
        ds.addColumnValues("paramid", 0, paramid, ";");
        ds.addColumnValues("paramtype", 0, paramtype, ";");
        ds.addColumnValues("replicateid", 0, replicateid, ";");
        ds.padColumns();
        StringBuffer sql = new StringBuffer("UPDATE sdidataitem SET calcexcludeflag=? WHERE sdcid=?");
        sql.append(" AND keyid1=?");
        sql.append(" AND keyid2=?");
        sql.append(" AND keyid3=?");
        if (paramlistid.length() > 0) {
            sql.append(" AND paramlistid=?");
        }
        if (paramlistversionid.length() > 0) {
            sql.append(" AND paramlistversionid=?");
        }
        if (variantid.length() > 0) {
            sql.append(" AND variantid=?");
        }
        if (dataset.length() > 0) {
            sql.append(" AND dataset=?");
        }
        if (paramid.length() > 0) {
            sql.append(" AND paramid=?");
        }
        if (paramtype.length() > 0) {
            sql.append(" AND paramtype=?");
        }
        if (replicateid.length() > 0) {
            sql.append(" AND replicateid=?");
        }
        try {
            PreparedStatement update = this.database.prepareStatement(sql.toString());
            for (int i = 0; i < ds.size(); ++i) {
                update.setString(1, ds.getString(i, "excludeflag"));
                update.setString(2, ds.getString(i, "sdcid"));
                update.setString(3, ds.getString(i, "keyid1"));
                update.setString(4, ds.getString(i, "keyid2"));
                update.setString(5, ds.getString(i, "keyid3"));
                int count = 6;
                if (paramlistid.length() > 0) {
                    update.setString(count++, ds.getString(i, "paramlistid"));
                }
                if (paramlistversionid.length() > 0) {
                    update.setString(count++, ds.getString(i, "paramlistversionid"));
                }
                if (variantid.length() > 0) {
                    update.setString(count++, ds.getString(i, "variantid"));
                }
                if (dataset.length() > 0) {
                    update.setInt(count++, Integer.parseInt(ds.getString(i, "dataset")));
                }
                if (paramid.length() > 0) {
                    update.setString(count++, ds.getString(i, "paramid"));
                }
                if (paramtype.length() > 0) {
                    update.setString(count++, ds.getString(i, "paramtype"));
                }
                if (replicateid.length() > 0) {
                    update.setInt(count++, Integer.parseInt(ds.getString(i, "replicateid")));
                }
                update.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new SapphireException("Unable to set the calcexcludeflag", e);
        }
        StringBuffer keyid1list = new StringBuffer();
        StringBuffer keyid2list = new StringBuffer();
        StringBuffer keyid3list = new StringBuffer();
        ArrayList<DataSet> byKey = ds.getGroupedDataSets("keyid1,keyid2,keyid3");
        for (DataSet key : byKey) {
            keyid1list.append(";").append(key.getString(0, "keyid1"));
            keyid2list.append(";").append(key.getString(0, "keyid2"));
            keyid3list.append(";").append(key.getString(0, "keyid3"));
        }
        PropertyList deProps = new PropertyList();
        deProps.setProperty("sdcid", sdcid);
        deProps.setProperty("keyid1", keyid1list.substring(1));
        deProps.setProperty("keyid2", keyid2list.substring(1));
        deProps.setProperty("keyid3", keyid3list.substring(1));
        this.getActionProcessor().processAction("RedoCalculations", "1", deProps);
    }
}

