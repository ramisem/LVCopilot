/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class DelSDIDataRelation
extends BaseAction
implements sapphire.action.DelSDIDataRelation {
    private static final String PROPERTY_SEPARATOR = ";";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String relationId;
        String dataSet;
        String variantId;
        String paramListVersionId;
        String sdcId = properties.getProperty("sdcid");
        DataSet input = new DataSet();
        String where = " WHERE sdcid=? AND keyid1=?  AND keyid2=? AND keyid3=? ";
        input.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), PROPERTY_SEPARATOR);
        input.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), PROPERTY_SEPARATOR);
        input.padColumn("sdcid");
        String keyId2 = properties.getProperty("keyid2");
        if (keyId2 == null || keyId2.length() == 0) {
            keyId2 = "(null)";
        }
        input.addColumnValues("keyid2", 0, keyId2, PROPERTY_SEPARATOR);
        input.padColumn("keyid1");
        String keyId3 = properties.getProperty("keyid3");
        if (keyId3 == null || keyId3.length() == 0) {
            keyId3 = "(null)";
        }
        input.addColumnValues("keyid3", 0, keyId3, PROPERTY_SEPARATOR);
        input.padColumn("keyid3");
        String paramListId = properties.getProperty("paramlistid");
        if (paramListId != null && paramListId.length() > 0) {
            input.addColumnValues("paramlistid", 0, paramListId, PROPERTY_SEPARATOR);
            input.padColumn("paramlistid");
            where = where + " AND paramlistid=? ";
        }
        if ((paramListVersionId = properties.getProperty("paramlistversionid")) != null && paramListVersionId.length() > 0) {
            input.addColumnValues("paramlistversionid", 0, paramListVersionId, PROPERTY_SEPARATOR);
            input.padColumn("paramlistversionid");
            where = where + " AND paramlistversionid= ? ";
        }
        if ((variantId = properties.getProperty("variantid")) != null && variantId.length() > 0) {
            input.addColumnValues("variantid", 0, variantId, PROPERTY_SEPARATOR);
            input.padColumn("variantid");
            where = where + " AND variantid=? ";
        }
        if ((dataSet = properties.getProperty("dataset")) != null && dataSet.length() > 0) {
            input.addColumnValues("dataset", 1, dataSet, PROPERTY_SEPARATOR);
            input.padColumn("dataset");
            where = where + " AND dataset=? ";
        }
        if ((relationId = properties.getProperty("relationid")) != null && relationId.length() > 0) {
            input.addColumnValues("relationid", 0, relationId, PROPERTY_SEPARATOR);
            input.padColumn("relationid");
            where = where + " AND relationid=? ";
        }
        String deleteSql = "DELETE FROM sdidatarelation ";
        deleteSql = deleteSql + where;
        PreparedStatement ps = this.database.prepareStatement(deleteSql);
        for (int row = 0; row < input.getRowCount(); ++row) {
            try {
                int offset = 1;
                ps.setString(offset++, input.getString(row, "sdcid"));
                ps.setString(offset++, input.getString(row, "keyid1"));
                ps.setString(offset++, input.getString(row, "keyid2"));
                ps.setString(offset++, input.getString(row, "keyid3"));
                if (paramListId != null && paramListId.length() > 0) {
                    ps.setString(offset++, input.getString(row, "paramlistid"));
                }
                if (paramListVersionId != null && paramListVersionId.length() > 0) {
                    ps.setString(offset++, input.getString(row, "paramlistversionid"));
                }
                if (variantId != null && variantId.length() > 0) {
                    ps.setString(offset++, input.getString(row, "variantid"));
                }
                if (dataSet != null && dataSet.length() > 0) {
                    ps.setBigDecimal(offset++, input.getBigDecimal(row, "dataset"));
                }
                if (relationId != null && relationId.length() > 0) {
                    ps.setString(offset, input.getString(row, "relationid"));
                }
                ps.executeUpdate();
                continue;
            }
            catch (SQLException e) {
                this.database.closeStatement();
                throw new SapphireException("DelSDIDataRelation action failed", e);
            }
        }
        this.database.closeStatement();
    }
}

