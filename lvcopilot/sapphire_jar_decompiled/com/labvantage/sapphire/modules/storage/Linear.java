/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.storage;

import com.labvantage.sapphire.modules.storage.BaseStorageUnitType;
import com.labvantage.sapphire.modules.storage.Label;
import sapphire.SapphireException;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Linear
extends BaseStorageUnitType {
    static final String LABVANTAGE_CVS_ID = "$Revision: 56075 $";
    public static final String INDEXORDER_LEFT_TO_RIGHT = "Left->Right";
    public static final String INDEXORDER_RIGHT_TO_LEFT = "Right->Left";
    public static final String INDEXORDER_TOP_TO_BOTTOM = "Top->Bottom";
    public static final String INDEXORDER_BOTTOM_TOP = "Bottom->Top";
    private String[] displayLabels = null;
    private String[] diplaySUIndices = null;

    public Linear() {
    }

    public Linear(PropertyList storageUnitType) throws Exception {
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
        if (INDEXORDER_TOP_TO_BOTTOM.equalsIgnoreCase(indexOrder) || INDEXORDER_LEFT_TO_RIGHT.equalsIgnoreCase(indexOrder)) {
            this.storageUnitIndex = masterStorageUnitIndices;
            this.storageUnitLabels = "Y".equalsIgnoreCase(useIndex) ? this.storageUnitIndex : masterLabels;
            this.diplaySUIndices = StringUtil.split(this.storageUnitIndex, ";");
            this.displayLabels = StringUtil.split(this.storageUnitLabels, ";");
        } else if (INDEXORDER_BOTTOM_TOP.equalsIgnoreCase(indexOrder) || INDEXORDER_RIGHT_TO_LEFT.equalsIgnoreCase(indexOrder)) {
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
        if ("Vertical".equals(this.storageUnitType.getProperty("orientation"))) {
            this.storageUnitRowLabels = masterLabels;
        } else {
            this.storageUnitColumnLabels = masterLabels;
        }
    }
}

