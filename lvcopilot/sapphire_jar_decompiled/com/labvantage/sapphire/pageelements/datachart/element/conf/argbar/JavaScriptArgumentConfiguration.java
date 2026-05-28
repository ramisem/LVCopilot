/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.argbar;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class JavaScriptArgumentConfiguration
extends BaseCustom
implements Serializable {
    private static final String DEFAULT_INPUT_TYPE = InputType.HIDDEN.getName();
    private final ArgumentConfiguration parent;
    private final String javaScript;
    private final InputType inputType;

    public JavaScriptArgumentConfiguration(PropertyList javaScriptArgumentProps, String connectionId, ArgumentConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (javaScriptArgumentProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.setConnectionId(connectionId);
        this.javaScript = javaScriptArgumentProps.getProperty("javascript");
        this.inputType = InputType.fromString(javaScriptArgumentProps.getProperty("inputtype", DEFAULT_INPUT_TYPE));
        this.parent = parent;
    }

    public ArgumentConfiguration getParent() {
        return this.parent;
    }

    public String getJavaScript() {
        return this.javaScript;
    }

    public InputType getInputType() {
        return this.inputType;
    }

    public static enum InputType {
        INPUT("Input"),
        HIDDEN("Hidden");

        private final String name;

        private InputType(String name) {
            this.name = name;
        }

        public static InputType fromString(String name) {
            if (name != null) {
                for (InputType type : InputType.values()) {
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

