/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.StandardAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class AddSDINotesAdapterConfiguration
implements Serializable {
    private static final String DEFAULT_KEY_ID1_COLUMN = "keyid1";
    private static final String DEFAULT_KEY_ID2_COLUMN = "";
    private static final String DEFAULT_KEY_ID3_COLUMN = "";
    private static final String DEFAULT_MAX_NOTE_LENGTH = "80";
    private static final String DEFAULT_COLUMN_NAME = "sdinotes";
    private static final String DEFAULT_DELIMITER = " -> ";
    private final StandardAdapterConfiguration parent;
    private final String sdcId;
    private final String keyId1Column;
    private final String keyId2Column;
    private final String keyId3Column;
    private final String delimiter;
    private final String columnName;
    private final int maxNoteLength;
    private final StringExpression rSetId;

    public AddSDINotesAdapterConfiguration(PropertyList addSDINotesAdapterProps, StandardAdapterConfiguration parent) {
        if (addSDINotesAdapterProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.sdcId = addSDINotesAdapterProps.getProperty("sdcid");
        this.keyId1Column = addSDINotesAdapterProps.getProperty("keyid1column", DEFAULT_KEY_ID1_COLUMN);
        this.keyId2Column = addSDINotesAdapterProps.getProperty("keyid2column", "");
        this.keyId3Column = addSDINotesAdapterProps.getProperty("keyid3column", "");
        this.delimiter = addSDINotesAdapterProps.getProperty("delimiter", DEFAULT_DELIMITER);
        this.columnName = addSDINotesAdapterProps.getProperty("columnname", DEFAULT_COLUMN_NAME);
        this.maxNoteLength = Integer.parseInt(addSDINotesAdapterProps.getProperty("maxnotelength", DEFAULT_MAX_NOTE_LENGTH));
        this.rSetId = new StringExpression(addSDINotesAdapterProps.getProperty("rsetid"));
    }

    public StringExpression getRSetId() {
        return this.rSetId;
    }

    public String getSdcId() {
        return this.sdcId;
    }

    public String getKeyId1Column() {
        return this.keyId1Column;
    }

    public String getKeyId2Column() {
        return this.keyId2Column;
    }

    public String getKeyId3Column() {
        return this.keyId3Column;
    }

    public StandardAdapterConfiguration getParent() {
        return this.parent;
    }

    public String getDelimiter() {
        return this.delimiter;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public int getMaxNoteLength() {
        return this.maxNoteLength;
    }
}

