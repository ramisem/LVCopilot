/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.argbar;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentValueConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.DropDownArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.EditorStyleArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.JavaScriptArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.NumberArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.TextArgumentConfiguration;
import java.io.Serializable;
import javax.servlet.jsp.PageContext;
import sapphire.xml.PropertyList;

public final class ArgumentConfiguration
extends BaseCustom
implements Serializable {
    private static final String DEFAULT_ARGUMENT_TYPE = ArgumentType.TEXT_ARGUMENT.getName();
    private static final String DEFAULT_ARGUMENT_VALUE_SCOPE = ArgumentValueScope.INHERIT.getName();
    private final ArgumentGroupConfiguration parent;
    private final String argumentId;
    private final String title;
    private final String width;
    private final ArgumentValueScope argumentValueScope;
    private final String tip;
    private final ArgumentType argumentType;
    private final TextArgumentConfiguration textArgumentConf;
    private final NumberArgumentConfiguration numberArgumentConf;
    private final DropDownArgumentConfiguration dropDownArgumentConf;
    private final JavaScriptArgumentConfiguration javaScriptArgumentConf;
    private final EditorStyleArgumentConfiguration editorStyleArgumentConf;
    private final ArgumentValueConfiguration argumentValueConf;

    public ArgumentConfiguration(PropertyList argumentProps, String connectionId, ArgumentGroupConfiguration parent, PageContext pageContext) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (argumentProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.setConnectionId(connectionId);
        this.argumentId = argumentProps.getProperty("argumentid", "");
        if (this.argumentId.isEmpty()) {
            throw new IllegalArgumentException("Argument ID is empty");
        }
        this.title = argumentProps.getProperty("title", this.argumentId);
        this.tip = argumentProps.getProperty("tip", this.argumentId);
        this.width = argumentProps.getProperty("width", "80");
        this.argumentType = ArgumentType.fromString(argumentProps.getProperty("argumenttype", DEFAULT_ARGUMENT_TYPE));
        this.argumentValueScope = ArgumentValueScope.fromString(argumentProps.getProperty("argumentvaluescope", DEFAULT_ARGUMENT_VALUE_SCOPE));
        this.textArgumentConf = this.argumentType == ArgumentType.TEXT_ARGUMENT ? new TextArgumentConfiguration(argumentProps.getPropertyListNotNull("textargumentprops"), connectionId, this) : null;
        this.numberArgumentConf = this.argumentType == ArgumentType.NUMBER_ARGUMENT ? new NumberArgumentConfiguration(argumentProps.getPropertyListNotNull("numberargumentprops"), connectionId, this) : null;
        this.dropDownArgumentConf = this.argumentType == ArgumentType.DROP_DOWN_ARGUMENT ? new DropDownArgumentConfiguration(argumentProps.getPropertyListNotNull("dropdownargumentprops"), connectionId, this) : null;
        this.javaScriptArgumentConf = this.argumentType == ArgumentType.JAVA_SCRIPT_ARGUMENT ? new JavaScriptArgumentConfiguration(argumentProps.getPropertyListNotNull("javascriptargumentprops"), connectionId, this) : null;
        this.editorStyleArgumentConf = this.argumentType == ArgumentType.EDITOR_STYLE_ARGUMENT ? new EditorStyleArgumentConfiguration(argumentProps.getPropertyListNotNull("editorstyleargumentprops"), connectionId, this, pageContext) : null;
        this.argumentValueConf = new ArgumentValueConfiguration(argumentProps.getPropertyListNotNull("argumentvalueprops"), this);
        this.parent = parent;
    }

    public String getTip() {
        return this.tip;
    }

    public ArgumentValueConfiguration getArgumentValueConfiguration() {
        return this.argumentValueConf;
    }

    public ArgumentValueScope getArgumentValueScope() {
        return this.argumentValueScope;
    }

    public DropDownArgumentConfiguration getDropDownArgumentConfiguration() {
        if (this.dropDownArgumentConf == null) {
            throw new IllegalStateException("Argument type is: " + (Object)((Object)this.argumentType));
        }
        return this.dropDownArgumentConf;
    }

    public TextArgumentConfiguration getTextArgumentConfiguration() {
        if (this.textArgumentConf == null) {
            throw new IllegalStateException("Argument type is: " + (Object)((Object)this.argumentType));
        }
        return this.textArgumentConf;
    }

    public JavaScriptArgumentConfiguration getJavaScriptArgumentConfiguration() {
        if (this.javaScriptArgumentConf == null) {
            throw new IllegalStateException("Argument type is: " + (Object)((Object)this.argumentType));
        }
        return this.javaScriptArgumentConf;
    }

    public EditorStyleArgumentConfiguration getEditorStyleArgumentConfiguration() {
        if (this.editorStyleArgumentConf == null) {
            throw new IllegalStateException("Argument type is: " + (Object)((Object)this.argumentType));
        }
        return this.editorStyleArgumentConf;
    }

    public NumberArgumentConfiguration getNumberArgumentConfiguration() {
        if (this.numberArgumentConf == null) {
            throw new IllegalStateException("Argument type is: " + (Object)((Object)this.argumentType));
        }
        return this.numberArgumentConf;
    }

    public ArgumentType getArgumentType() {
        return this.argumentType;
    }

    public String getTitle() {
        return this.title;
    }

    public String getWidth() {
        return this.width;
    }

    public String getArgumentId() {
        return this.argumentId;
    }

    public ArgumentGroupConfiguration getParent() {
        return this.parent;
    }

    public boolean isClientSideArgument() {
        return this.argumentType == ArgumentType.JAVA_SCRIPT_ARGUMENT;
    }

    public static enum ArgumentValueScope {
        REQUEST("Request"),
        INHERIT("Inherit");

        private final String name;

        private ArgumentValueScope(String name) {
            this.name = name;
        }

        public static ArgumentValueScope fromString(String name) {
            if (name != null) {
                for (ArgumentValueScope scope : ArgumentValueScope.values()) {
                    if (!name.equalsIgnoreCase(scope.name)) continue;
                    return scope;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }
    }

    public static enum ArgumentType {
        TEXT_ARGUMENT("Text"),
        HIDDEN_ARGUMENT("Hidden"),
        DROP_DOWN_ARGUMENT("Drop-down List"),
        NUMBER_ARGUMENT("Number"),
        JAVA_SCRIPT_ARGUMENT("JavaScript"),
        EDITOR_STYLE_ARGUMENT("Editor Style");

        private final String name;

        private ArgumentType(String name) {
            this.name = name;
        }

        public static ArgumentType fromString(String name) {
            if (name != null) {
                for (ArgumentType type : ArgumentType.values()) {
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

