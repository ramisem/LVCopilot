/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.storage;

import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public abstract class BaseStorageUnitType {
    static String LABVANTAGE_CVS_ID = "$Revision: 56075 $";
    protected PropertyList storageUnitType = null;
    protected String storageUnitIndex = "";
    protected String storageUnitLabels = "";
    protected String storageUnitRowLabels = "";
    protected String storageUnitColumnLabels = "";
    public static final String YES = "Y";
    public static final String NO = "N";
    public int startindex = 0;

    public BaseStorageUnitType() {
    }

    public BaseStorageUnitType(PropertyList storageUnitType) {
        this.storageUnitType = storageUnitType;
    }

    public abstract void initialize(int var1) throws SapphireException;

    public PropertyList getStorageUnitType() {
        return this.storageUnitType;
    }

    public void setStorageUnitType(PropertyList storageUnitType) {
        this.storageUnitType = storageUnitType;
    }

    public String getStroageUnitIndices() {
        return this.storageUnitIndex;
    }

    public String getStorageUnitLabels() {
        return this.storageUnitLabels;
    }

    protected String[] reverseArray(String[] originalArray) {
        String[] newArray = null;
        if (originalArray != null) {
            newArray = new String[originalArray.length];
            for (int count = 0; count < originalArray.length; ++count) {
                newArray[originalArray.length - count - 1] = originalArray[count];
            }
        }
        return newArray;
    }

    protected String concatenateArrayElements(String[] array, String delimiter) {
        StringBuffer sb = new StringBuffer();
        if (array != null) {
            for (int count = 0; count < array.length; ++count) {
                sb.append(array[count]).append(delimiter);
            }
        }
        return sb.substring(0, sb.length() - delimiter.length());
    }

    public void setStartIndex(int startindex) {
        this.startindex = startindex;
    }

    public String getStorageUnitRowLabels() {
        return this.storageUnitRowLabels;
    }

    public String getStorageUnitColumnLabels() {
        return this.storageUnitColumnLabels;
    }
}

