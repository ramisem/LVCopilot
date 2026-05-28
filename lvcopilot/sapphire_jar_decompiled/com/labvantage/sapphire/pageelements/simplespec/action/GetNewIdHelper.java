/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.simplespec.action;

import com.labvantage.sapphire.BaseCustom;
import java.math.BigDecimal;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class GetNewIdHelper
extends BaseCustom {
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    private static final int MAX_ID_SIZE = 40;
    private static final String DELIMITER = "_";
    private static final String FIRST_SEQUENCE = "1";

    public GetNewIdHelper(String connectionId) {
        if (connectionId == null || connectionId.isEmpty()) {
            throw new IllegalArgumentException("Connection ID is null or empty: " + connectionId);
        }
        this.setConnectionId(connectionId);
    }

    public String getNewId(String sdcId, String keyId1, String tableId, String keyId1Column) throws SapphireException {
        if (sdcId == null || sdcId.isEmpty()) {
            throw new SapphireException("SDC ID is null or empty: " + sdcId);
        }
        if (keyId1 == null || keyId1.isEmpty()) {
            throw new SapphireException("Key ID1 is null or empty: " + keyId1);
        }
        if (tableId == null || tableId.isEmpty()) {
            throw new IllegalArgumentException("Table ID is null or empty: " + tableId);
        }
        if (keyId1Column == null || keyId1Column.isEmpty()) {
            throw new IllegalArgumentException("Key ID 1 column is null or empty: " + keyId1Column);
        }
        String getSdcNameSql = "SELECT singular FROM sdc WHERE sdcid = ?";
        Object[] getSdcNameParams = new Object[]{sdcId};
        DataSet getSdcNameDs = this.getQueryProcessor().getPreparedSqlDataSet(getSdcNameSql, getSdcNameParams);
        if (getSdcNameDs.getRowCount() == 0) {
            throw new SapphireException("Unknown SDC ID: " + sdcId);
        }
        String sdcName = getSdcNameDs.getString(0, "singular");
        String newId = sdcName + DELIMITER + keyId1;
        String sequenceWithDelimiterStr = "_1";
        String newIdWithSequence = newId + sequenceWithDelimiterStr;
        if (newIdWithSequence.length() > 40) {
            newId = newId.substring(0, 40 - sequenceWithDelimiterStr.length());
            newIdWithSequence = newId + sequenceWithDelimiterStr;
        }
        boolean exists = this.checkExists(tableId, keyId1Column, newIdWithSequence);
        String sequenceString = null;
        int safetyCnt = 0;
        if (exists) {
            int sequence = 0;
            while (exists && safetyCnt < 1000) {
                ++safetyCnt;
                sequenceString = Integer.toString(++sequence);
                sequenceWithDelimiterStr = DELIMITER + sequenceString;
                newIdWithSequence = newId + sequenceWithDelimiterStr;
                if (newIdWithSequence.length() > 40) {
                    newId = newId.substring(0, 40 - sequenceWithDelimiterStr.length());
                    newIdWithSequence = newId + sequenceWithDelimiterStr;
                }
                exists = this.checkExists(tableId, keyId1Column, newIdWithSequence);
            }
        } else {
            sequenceString = FIRST_SEQUENCE;
        }
        int maxIdSizeWithoutSequence = 40 - (DELIMITER.length() + sequenceString.length());
        if (newId.length() > maxIdSizeWithoutSequence) {
            newId = newId.substring(0, maxIdSizeWithoutSequence);
        }
        newId = newId + DELIMITER + sequenceString;
        return newId;
    }

    private String getMaxSequence(String tableId, String keyId1Column, String newId) {
        String escapedNewId = StringUtil.replaceAll(newId + DELIMITER + "%", DELIMITER, "\\_");
        String getMaxSequenceSql = "SELECT MAX(" + keyId1Column + ") maxid FROM " + tableId + " WHERE " + keyId1Column + " like '" + escapedNewId + "' ESCAPE '\\'";
        DataSet getMaxSequenceDs = this.getQueryProcessor().getSqlDataSet(getMaxSequenceSql);
        return getMaxSequenceDs.getString(0, "maxid", "");
    }

    private boolean checkExists(String tableId, String keyId1Column, String newIdWithSequence) {
        String existsSql = "SELECT count(1) c FROM " + tableId + " WHERE " + keyId1Column + " = ?";
        Object[] existsParams = new Object[]{newIdWithSequence};
        DataSet existsDs = this.getQueryProcessor().getPreparedSqlDataSet(existsSql, existsParams);
        return existsDs.getBigDecimal(0, "c", BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0;
    }
}

