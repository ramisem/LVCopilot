/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.storage;

import com.labvantage.sapphire.modules.storage.BaseStorageUnitType;
import com.labvantage.sapphire.modules.storage.Label;
import sapphire.SapphireException;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Circular
extends BaseStorageUnitType {
    static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    public static final String INDEXORDER_CLOCKWISE = "Clockwise";
    public static final String INDEXORDER_ANTICLOCKWISE = "Anti-Clockwise";
    private String[] displayLabels = null;
    private String[] diplaySUIndices = null;

    public Circular() {
    }

    public Circular(PropertyList storageUnitType) throws Exception {
        super(storageUnitType);
    }

    public String[] getDisplayLabels() {
        return this.displayLabels;
    }

    public String[] getDiplaySUIndices() {
        return this.diplaySUIndices;
    }

    @Override
    public void initialize(int size) throws SapphireException {
        String masterLabels = null;
        String indexOrder = this.storageUnitType.getProperty("indexorder");
        PropertyList labelProps = this.storageUnitType.getPropertyList("labelgenrule");
        String useIndex = labelProps.getProperty("useindex", "N");
        Label label = new Label(labelProps, this.startindex);
        String masterStorageUnitIndices = label.getIndices(size);
        if (!"Y".equalsIgnoreCase(useIndex)) {
            masterLabels = label.getLabels(size);
        }
        this.displayLabels = StringUtil.split(masterLabels, ";");
        if (INDEXORDER_CLOCKWISE.equalsIgnoreCase(indexOrder)) {
            this.storageUnitIndex = masterStorageUnitIndices;
            this.storageUnitLabels = "Y".equalsIgnoreCase(useIndex) ? this.storageUnitIndex : masterLabels;
            this.diplaySUIndices = StringUtil.split(this.storageUnitIndex, ";");
            this.displayLabels = StringUtil.split(this.storageUnitLabels, ";");
        } else if (INDEXORDER_ANTICLOCKWISE.equalsIgnoreCase(indexOrder)) {
            this.storageUnitIndex = masterStorageUnitIndices;
            if ("Y".equalsIgnoreCase(useIndex)) {
                this.storageUnitLabels = this.storageUnitIndex;
                String[] tempArr = StringUtil.split(masterStorageUnitIndices, ";");
                this.diplaySUIndices = this.reverseArray(tempArr);
                this.displayLabels = this.diplaySUIndices;
            } else {
                String[] tempArr = StringUtil.split(masterLabels, ";");
                tempArr = this.reverseArray(tempArr);
                this.storageUnitLabels = this.concatenateArrayElements(tempArr, ";");
                this.displayLabels = StringUtil.split(masterLabels, ";");
                tempArr = StringUtil.split(masterStorageUnitIndices, ";");
                this.diplaySUIndices = this.reverseArray(tempArr);
            }
        }
    }
}

