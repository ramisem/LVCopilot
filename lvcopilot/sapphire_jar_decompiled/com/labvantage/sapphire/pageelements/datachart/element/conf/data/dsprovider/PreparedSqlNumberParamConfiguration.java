/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.PreparedSqlParamConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class PreparedSqlNumberParamConfiguration
implements Serializable {
    private static final String DEFAULT_PREPARED_SQL_DATE_PARAM_TYPE = PreparedSqlNumberParamType.USER.getName();
    private final PreparedSqlParamConfiguration parent;
    private final PreparedSqlNumberParamType preparedSqlNumberParamType;

    public PreparedSqlNumberParamConfiguration(PropertyList preparedSqlNumberParamProps, PreparedSqlParamConfiguration parent) {
        if (preparedSqlNumberParamProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.preparedSqlNumberParamType = PreparedSqlNumberParamType.fromString(preparedSqlNumberParamProps.getProperty("preparedsqlnumberparamtype", DEFAULT_PREPARED_SQL_DATE_PARAM_TYPE));
        this.parent = parent;
    }

    public PreparedSqlNumberParamType getPreparedSqlNumberParamType() {
        return this.preparedSqlNumberParamType;
    }

    public PreparedSqlParamConfiguration getParent() {
        return this.parent;
    }

    public static enum PreparedSqlNumberParamType {
        SYSTEM("System"),
        USER("User");

        private final String name;

        private PreparedSqlNumberParamType(String name) {
            this.name = name;
        }

        public static PreparedSqlNumberParamType fromString(String name) {
            if (name != null) {
                for (PreparedSqlNumberParamType type : PreparedSqlNumberParamType.values()) {
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

