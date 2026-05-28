/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

public class PLDataSet {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private String __ParamListId;
    private String __ParamListVersionId;
    private String __VariantId;
    private String __Dataset;

    public PLDataSet(String __ParamListId, String __ParamListVersionId, String __VariantId, String __Dataset) {
        this.__ParamListId = __ParamListId;
        this.__ParamListVersionId = __ParamListVersionId;
        this.__VariantId = __VariantId;
        this.__Dataset = __Dataset;
    }

    public String getParamListId() {
        return this.__ParamListId;
    }

    public String getParamListVersionId() {
        return this.__ParamListVersionId;
    }

    public String getVariantId() {
        return this.__VariantId;
    }

    public String getDataset() {
        return this.__Dataset;
    }

    public String toString() {
        return "PLDataSet{__ParamListId='" + this.__ParamListId + "', __ParamListVersionId='" + this.__ParamListVersionId + "', __VariantId='" + this.__VariantId + "', __Dataset='" + this.__Dataset + "'}";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PLDataSet)) {
            return false;
        }
        PLDataSet plDataSet = (PLDataSet)o;
        if (this.__Dataset != null ? !this.__Dataset.equals(plDataSet.__Dataset) : plDataSet.__Dataset != null) {
            return false;
        }
        if (this.__ParamListId != null ? !this.__ParamListId.equals(plDataSet.__ParamListId) : plDataSet.__ParamListId != null) {
            return false;
        }
        if (this.__ParamListVersionId != null ? !this.__ParamListVersionId.equals(plDataSet.__ParamListVersionId) : plDataSet.__ParamListVersionId != null) {
            return false;
        }
        return !(this.__VariantId != null ? !this.__VariantId.equals(plDataSet.__VariantId) : plDataSet.__VariantId != null);
    }

    public int hashCode() {
        int result = this.__ParamListId != null ? this.__ParamListId.hashCode() : 0;
        result = 29 * result + (this.__ParamListVersionId != null ? this.__ParamListVersionId.hashCode() : 0);
        result = 29 * result + (this.__VariantId != null ? this.__VariantId.hashCode() : 0);
        result = 29 * result + (this.__Dataset != null ? this.__Dataset.hashCode() : 0);
        return result;
    }
}

