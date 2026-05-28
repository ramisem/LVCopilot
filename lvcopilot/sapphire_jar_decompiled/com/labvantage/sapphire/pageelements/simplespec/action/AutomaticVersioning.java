/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.simplespec.action;

public enum AutomaticVersioning {
    ALWAYS("Always"),
    ONLY_REFERENCED("Only Referenced"),
    NEVER("Never");

    private final String name;

    private AutomaticVersioning(String name) {
        this.name = name;
    }

    public static AutomaticVersioning fromString(String name) {
        if (name != null) {
            for (AutomaticVersioning type : AutomaticVersioning.values()) {
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

