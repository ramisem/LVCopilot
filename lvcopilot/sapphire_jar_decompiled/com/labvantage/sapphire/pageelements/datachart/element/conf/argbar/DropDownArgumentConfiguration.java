/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.argbar;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class DropDownArgumentConfiguration
extends BaseCustom
implements Serializable {
    private static final String DEFAULT_DROP_DOWN_ARGUMENT_TYPE = DropDownArgumentType.VALUE_LIST.getName();
    private static final String DEFAULT_MULTI_SELECT = "N";
    private static final String DEFAULT_ADD_EMPTY_VALUE = "Y";
    private final ArgumentConfiguration parent;
    private final DropDownArgumentType dropDownArgumentType;
    private final String sdcId;
    private final StringExpression sql;
    private final String refTypeId;
    private final StringExpression valueListConf;
    private final boolean translateDisplayValue;
    private final String extendWhere;
    private final String sdcValueColumn;
    private final String sdcDisplayValueColumn;
    private final boolean multiSelect;
    private final boolean addEmptyValue;

    public DropDownArgumentConfiguration(PropertyList dropDownArgumentProps, String connectionId, ArgumentConfiguration parent) {
        if (dropDownArgumentProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (!dropDownArgumentProps.containsKey("jUnit") && parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.setConnectionId(connectionId);
        this.parent = parent;
        this.dropDownArgumentType = DropDownArgumentType.fromString(dropDownArgumentProps.getProperty("dropdowntype", DEFAULT_DROP_DOWN_ARGUMENT_TYPE));
        this.multiSelect = dropDownArgumentProps.getProperty("multiselect", DEFAULT_MULTI_SELECT).toLowerCase().startsWith("y");
        this.addEmptyValue = dropDownArgumentProps.getProperty("addemptyvalue", DEFAULT_ADD_EMPTY_VALUE).toLowerCase().startsWith("y");
        this.sdcId = dropDownArgumentProps.getProperty("sdcid", "");
        this.sql = new StringExpression(dropDownArgumentProps.getProperty("dropdownsql", ""));
        this.refTypeId = dropDownArgumentProps.getProperty("reftypeid", "");
        this.valueListConf = new StringExpression(dropDownArgumentProps.getProperty("valuelistconf", ""));
        this.translateDisplayValue = dropDownArgumentProps.getProperty("translate", "").equals(DEFAULT_ADD_EMPTY_VALUE);
        this.extendWhere = dropDownArgumentProps.getProperty("extendedwhere", "");
        this.sdcValueColumn = dropDownArgumentProps.getProperty("sdcvaluecolumn", "");
        this.sdcDisplayValueColumn = dropDownArgumentProps.getProperty("sdcdisplayvaluecolumn", "");
    }

    public DropDownArgumentType getDropDownArgumentType() {
        return this.dropDownArgumentType;
    }

    public ArgumentConfiguration getParent() {
        return this.parent;
    }

    public boolean isMultiSelect() {
        return this.multiSelect;
    }

    public String getSdcId() {
        if (this.dropDownArgumentType != DropDownArgumentType.SDC) {
            throw new IllegalStateException("Drop-down type is: " + (Object)((Object)this.dropDownArgumentType));
        }
        return this.sdcId;
    }

    public String getExtendWhere() {
        if (this.dropDownArgumentType != DropDownArgumentType.SDC) {
            throw new IllegalStateException("Drop-down type is: " + (Object)((Object)this.dropDownArgumentType));
        }
        return this.extendWhere;
    }

    public String getSDCValueColumn() {
        if (this.dropDownArgumentType != DropDownArgumentType.SDC) {
            throw new IllegalStateException("Drop-down type is: " + (Object)((Object)this.dropDownArgumentType));
        }
        return this.sdcValueColumn;
    }

    public String getSDCDisplayValueColumn() {
        if (this.dropDownArgumentType != DropDownArgumentType.SDC) {
            throw new IllegalStateException("Drop-down type is: " + (Object)((Object)this.dropDownArgumentType));
        }
        return this.sdcDisplayValueColumn;
    }

    public String getRefTypeId() {
        if (this.dropDownArgumentType != DropDownArgumentType.REFERENCE_TYPE) {
            throw new IllegalStateException("Drop-down type is: " + (Object)((Object)this.dropDownArgumentType));
        }
        return this.refTypeId;
    }

    public StringExpression getValueListConf() {
        if (this.dropDownArgumentType != DropDownArgumentType.VALUE_LIST) {
            throw new IllegalStateException("Drop-down type is: " + (Object)((Object)this.dropDownArgumentType));
        }
        return this.valueListConf;
    }

    public StringExpression getSql() {
        return this.sql;
    }

    public boolean getTranslateDisplayValue() {
        return this.translateDisplayValue;
    }

    public boolean addEmptyValue() {
        return this.addEmptyValue;
    }

    public static enum DropDownArgumentType {
        VALUE_LIST("Value list"),
        REFERENCE_TYPE("Reference Type"),
        SQL("SQL"),
        SDC("SDC");

        private final String name;

        private DropDownArgumentType(String name) {
            this.name = name;
        }

        public static DropDownArgumentType fromString(String name) {
            if (name != null) {
                for (DropDownArgumentType type : DropDownArgumentType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }
    }
}

