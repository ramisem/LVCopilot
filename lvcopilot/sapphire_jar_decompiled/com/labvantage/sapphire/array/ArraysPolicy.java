/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array;

import java.io.Serializable;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ArraysPolicy
implements Serializable {
    public static final String PROPERTY_CONTENTINFO = "ContentInfo";
    public static final String PROPERTY_ARRAYSUMMARY = "ArraySummary";
    public static final String PROPERTY_CONTENTPAGEMAPPING = "ContentPageMapping";
    public static final String PROPERTY_ALTERNATEPRIMARYKEYTYPE = "AlternatePrimaryKeyType";
    public static final String ALTERNATEPRIMARYKEYTYPE_SAMPLESDCCOLUMN = "SampleSDCColumn";
    public static final String ALTERNATEPRIMARYKEYTYPE_SDIALIAS = "SDIAlias";
    public static final String PROPERTY_ALTERNATEKEYCOLUMN = "AlternateKeyColumn";
    public static final String PROPERTY_PRIMARYALIASTYPE = "PrimaryAliasType";
    public static final String PROPERTY_COLUMNLIST = "ColumnList";
    public static final String PROPERTY_COLUMNID = "columnid";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_SHOW = "show";
    public static final String PROPERTY_CHILDSAMPLESTATUS = "childsamplestatus";
    public static final String PROPERTY_CHILDSAMPLESTORAGESTATUS = "childsamplestoragestatus";
    public static final String PROPERTY_DISPOSESOURCEWHENCONSUMED = "disposesourcewhenconsumed";
    private PropertyList contentInfo = null;
    private PropertyList arraysummary = null;
    private PropertyList contentpagemapping = null;
    private PropertyList arrayitempushrules = null;
    private String alternatePrimaryKeyType = "";
    private String alternateKeyColumn = "";
    private String primaryAliasType = "";
    private String childsamplestatus = "";
    private String childsamplestoragestatus = "";
    private String disposesourcewhenconsumed = "";
    private String addnewdatasetifreleased = "";

    public ArraysPolicy(PropertyList policy) throws SapphireException {
        if (policy == null) {
            throw new SapphireException("Policy is null.");
        }
        this.contentInfo = policy.getPropertyListNotNull(PROPERTY_CONTENTINFO);
        this.alternatePrimaryKeyType = this.contentInfo.getProperty(PROPERTY_ALTERNATEPRIMARYKEYTYPE);
        if (ALTERNATEPRIMARYKEYTYPE_SAMPLESDCCOLUMN.equals(this.alternatePrimaryKeyType)) {
            this.alternateKeyColumn = this.contentInfo.getProperty(PROPERTY_ALTERNATEKEYCOLUMN, "");
        }
        if (ALTERNATEPRIMARYKEYTYPE_SDIALIAS.equals(this.alternatePrimaryKeyType)) {
            this.primaryAliasType = this.contentInfo.getProperty(PROPERTY_PRIMARYALIASTYPE, "");
        }
        this.arraysummary = policy.getPropertyListNotNull(PROPERTY_ARRAYSUMMARY);
        this.contentpagemapping = policy.getPropertyListNotNull(PROPERTY_CONTENTPAGEMAPPING);
        this.arrayitempushrules = policy.getPropertyListNotNull("arrayitempushrules");
        this.childsamplestatus = policy.getProperty(PROPERTY_CHILDSAMPLESTATUS);
        this.childsamplestoragestatus = policy.getProperty(PROPERTY_CHILDSAMPLESTORAGESTATUS);
        this.disposesourcewhenconsumed = policy.getProperty(PROPERTY_DISPOSESOURCEWHENCONSUMED, "N");
        this.addnewdatasetifreleased = this.arrayitempushrules.getProperty("addnewdatasetifreleased", "N");
    }

    public boolean isAlternateKeyDefined() {
        return this.alternateKeyColumn.length() > 0;
    }

    public boolean isAlternateAliasDefined() {
        return this.primaryAliasType.length() > 0;
    }

    public String getAlternateKeyColumn() {
        return this.alternateKeyColumn;
    }

    public String getPropertyAliasType() {
        return this.primaryAliasType;
    }

    public String getChildSampleStatus() {
        return this.childsamplestatus;
    }

    public String getChildSampleStorageStatus() {
        return this.childsamplestoragestatus;
    }

    public String getDisposeSourceWhenConsumed() {
        return this.disposesourcewhenconsumed;
    }

    public PropertyListCollection getColumnList() {
        return this.arraysummary.getCollection(PROPERTY_COLUMNLIST);
    }

    public PropertyList getContentPage() {
        return this.contentpagemapping;
    }

    public String getAddNewDataSetIfReleased() {
        return this.addnewdatasetifreleased;
    }
}

