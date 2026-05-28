/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.PreparedSqlDataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.PreparedSqlDateParamConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.PreparedSqlNumberParamConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BooleanExpression;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class PreparedSqlParamConfiguration
implements Serializable {
    private static final String DEFAULT_PREPARED_SQL_PARAM_TYPE = PreparedSqlParamType.STRING.getName();
    private static final String DEFAULT_ENABLE = "Y";
    private static final String DEFAULT_PARAM_STRING_VALUE_SEPARATOR = ";";
    private final PreparedSqlDataSetConfiguration parent;
    private final PreparedSqlParamType preparedSqlParamType;
    private final PreparedSqlDateParamConfiguration preparedSqlDateParamConf;
    private final PreparedSqlNumberParamConfiguration preparedSqlNumberParamConf;
    private final StringExpression paramStringValue;
    private final BooleanExpression enable;
    private final String paramStringValueSeparator;

    public PreparedSqlParamConfiguration(PropertyList preparedSqlParamProps, PreparedSqlDataSetConfiguration parent) {
        if (preparedSqlParamProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.preparedSqlParamType = PreparedSqlParamType.fromString(preparedSqlParamProps.getProperty("preparedsqlparamtype", DEFAULT_PREPARED_SQL_PARAM_TYPE));
        this.preparedSqlDateParamConf = this.preparedSqlParamType == PreparedSqlParamType.DATE ? new PreparedSqlDateParamConfiguration(preparedSqlParamProps.getPropertyListNotNull("preparedsqldateparamprops"), this) : null;
        this.preparedSqlNumberParamConf = this.preparedSqlParamType == PreparedSqlParamType.NUMBER ? new PreparedSqlNumberParamConfiguration(preparedSqlParamProps.getPropertyListNotNull("preparedsqlnumberparamprops"), this) : null;
        this.paramStringValue = new StringExpression(preparedSqlParamProps.getProperty("paramstringvalue"));
        this.enable = new BooleanExpression(preparedSqlParamProps.getProperty("enable", DEFAULT_ENABLE));
        this.paramStringValueSeparator = preparedSqlParamProps.getProperty("paramstringvalueseparator", DEFAULT_PARAM_STRING_VALUE_SEPARATOR);
        this.parent = parent;
    }

    public String getParamStringValueSeparator() {
        return this.paramStringValueSeparator;
    }

    public BooleanExpression isEnabled() {
        return this.enable;
    }

    public PreparedSqlParamType getPreparedSqlParamType() {
        return this.preparedSqlParamType;
    }

    public PreparedSqlDateParamConfiguration getPreparedSqlDateParamConfiguration() {
        if (this.preparedSqlDateParamConf == null) {
            throw new IllegalStateException("Prepared SQL param type is: " + (Object)((Object)this.preparedSqlParamType));
        }
        return this.preparedSqlDateParamConf;
    }

    public StringExpression getParamStringValue() {
        return this.paramStringValue;
    }

    public PreparedSqlNumberParamConfiguration getPreparedSqlNumberParamConfiguration() {
        if (this.preparedSqlNumberParamConf == null) {
            throw new IllegalStateException("Prepared SQL param type is: " + (Object)((Object)this.preparedSqlParamType));
        }
        return this.preparedSqlNumberParamConf;
    }

    public PreparedSqlDataSetConfiguration getParent() {
        return this.parent;
    }

    public static enum PreparedSqlParamType {
        STRING("String"),
        NUMBER("Number"),
        DATE("Date");

        private final String name;

        private PreparedSqlParamType(String name) {
            this.name = name;
        }

        public static PreparedSqlParamType fromString(String name) {
            if (name != null) {
                for (PreparedSqlParamType type : PreparedSqlParamType.values()) {
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

