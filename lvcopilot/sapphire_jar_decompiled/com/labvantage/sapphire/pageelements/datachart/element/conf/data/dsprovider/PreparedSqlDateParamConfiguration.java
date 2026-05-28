/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.PreparedSqlParamConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class PreparedSqlDateParamConfiguration
implements Serializable {
    private static final String DEFAULT_PREPARED_SQL_DATE_PARAM_TYPE = PreparedSqlDateParamType.USER.getName();
    private final PreparedSqlParamConfiguration parent;
    private final PreparedSqlDateParamType preparedSqlDateParamType;
    private final StringExpression customFormat;

    public PreparedSqlDateParamConfiguration(PropertyList preparedSqlDateParamProps, PreparedSqlParamConfiguration parent) {
        if (preparedSqlDateParamProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.preparedSqlDateParamType = PreparedSqlDateParamType.fromString(preparedSqlDateParamProps.getProperty("preparedsqldateparamtype", DEFAULT_PREPARED_SQL_DATE_PARAM_TYPE));
        this.customFormat = new StringExpression(preparedSqlDateParamProps.getProperty("customformat"));
        this.parent = parent;
    }

    public PreparedSqlDateParamType getPreparedSqlDateParamType() {
        return this.preparedSqlDateParamType;
    }

    public StringExpression getCustomFormat() {
        if (this.preparedSqlDateParamType != PreparedSqlDateParamType.CUSTOM) {
            throw new IllegalArgumentException("Prepared SQL date param type is not " + (Object)((Object)PreparedSqlDateParamType.CUSTOM));
        }
        return this.customFormat;
    }

    public PreparedSqlParamConfiguration getParent() {
        return this.parent;
    }

    public static enum PreparedSqlDateParamType {
        SYSTEM("System"),
        USER("User"),
        CUSTOM("Custom");

        private final String name;

        private PreparedSqlDateParamType(String name) {
            this.name = name;
        }

        public static PreparedSqlDateParamType fromString(String name) {
            if (name != null) {
                for (PreparedSqlDateParamType type : PreparedSqlDateParamType.values()) {
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

