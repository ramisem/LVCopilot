/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ButtonSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.MenuButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.RadioButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.SimpleButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ToggleButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BooleanExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class ButtonConfiguration
implements Serializable {
    private static final String DEFAULT_TEXT = "Button";
    private static final String DEFAULT_BUTTON_TYPE = ButtonType.SIMPLE_BUTTON.getName();
    private static final String DEFAULT_SHOW_TEXT = "Y";
    private static final String DEFAULT_SHOW_ICON = "Y";
    private static final String DEFAULT_SHOW_CHECKBOX_RADIO_ICON = "N";
    private static final String DEFAULT_ICON_POSITION = "beginning";
    private static final String DEFAULT_PRIMARY_ICON = "";
    private static final String DEFAULT_SECONDARY_ICON = "";
    private static final String DEFAULT_DISABLED = "N";
    private final ButtonSetConfiguration parent;
    private final String buttonId;
    private final String text;
    private final String tip;
    private final String iconPosition;
    private final String primaryIcon;
    private final String secondaryIcon;
    private final boolean showText;
    private final boolean showIcon;
    private final BooleanExpression disabled;
    private final ButtonType buttonType;
    private final SimpleButtonConfiguration simpleButtonConf;
    private final ToggleButtonConfiguration toggleButtonConf;
    private final RadioButtonConfiguration radioButtonConf;
    private final MenuButtonConfiguration menuButtonConf;

    public ButtonConfiguration(PropertyList buttonProps, ButtonSetConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (buttonProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.buttonId = buttonProps.getProperty("buttonid", "");
        this.text = buttonProps.getProperty("text", DEFAULT_TEXT);
        this.tip = buttonProps.getProperty("tip", this.buttonId);
        this.showText = buttonProps.getProperty("showtext", "Y").toLowerCase().startsWith("y");
        this.buttonType = ButtonType.fromString(buttonProps.getProperty("buttontype", DEFAULT_BUTTON_TYPE));
        this.showIcon = this.buttonType.equals((Object)ButtonType.TOGGLE_BUTTON) || this.buttonType.equals((Object)ButtonType.RADIO_BUTTON) ? buttonProps.getProperty("showicon", "N").toLowerCase().startsWith("y") : buttonProps.getProperty("showicon", "Y").toLowerCase().startsWith("y");
        this.iconPosition = buttonProps.getProperty("iconposition", DEFAULT_ICON_POSITION);
        this.primaryIcon = buttonProps.getProperty("primaryicon", "");
        this.secondaryIcon = buttonProps.getProperty("secondaryicon", "");
        this.disabled = new BooleanExpression(buttonProps.getProperty("disabled", "N"));
        this.simpleButtonConf = this.buttonType == ButtonType.SIMPLE_BUTTON ? new SimpleButtonConfiguration(buttonProps.getPropertyListNotNull("simplebuttonprops"), this) : null;
        this.menuButtonConf = this.buttonType == ButtonType.MENU_BUTTON ? new MenuButtonConfiguration(buttonProps.getPropertyListNotNull("menubuttonprops"), this) : null;
        this.toggleButtonConf = this.buttonType == ButtonType.TOGGLE_BUTTON ? new ToggleButtonConfiguration(buttonProps.getPropertyListNotNull("togglebuttonprops"), this) : null;
        this.radioButtonConf = this.buttonType == ButtonType.RADIO_BUTTON ? new RadioButtonConfiguration(buttonProps.getPropertyListNotNull("radiobuttonprops"), this) : null;
        this.parent = parent;
    }

    public ButtonConfiguration(ButtonConfiguration copy, ButtonSetConfiguration parent) {
        this.buttonId = copy.buttonId;
        this.text = copy.text;
        this.tip = copy.tip;
        this.parent = parent;
        this.buttonType = copy.buttonType;
        this.showText = copy.showText;
        this.showIcon = copy.showIcon;
        this.iconPosition = copy.iconPosition;
        this.primaryIcon = copy.primaryIcon;
        this.secondaryIcon = copy.secondaryIcon;
        this.disabled = new BooleanExpression(copy.disabled);
        this.simpleButtonConf = copy.simpleButtonConf != null ? new SimpleButtonConfiguration(copy.simpleButtonConf, this) : null;
        this.menuButtonConf = copy.menuButtonConf != null ? new MenuButtonConfiguration(copy.menuButtonConf, this) : null;
        this.toggleButtonConf = copy.toggleButtonConf != null ? new ToggleButtonConfiguration(copy.toggleButtonConf, this) : null;
        this.radioButtonConf = copy.radioButtonConf != null ? new RadioButtonConfiguration(copy.radioButtonConf, this) : null;
    }

    public SimpleButtonConfiguration getSimpleButtonConfiguration() {
        if (this.simpleButtonConf == null) {
            throw new IllegalStateException("Button type is: " + (Object)((Object)this.buttonType));
        }
        return this.simpleButtonConf;
    }

    public RadioButtonConfiguration getRadioButtonConfiguration() {
        if (this.radioButtonConf == null) {
            throw new IllegalStateException("Button type is: " + (Object)((Object)this.buttonType));
        }
        return this.radioButtonConf;
    }

    public ToggleButtonConfiguration getToggleButtonConfiguration() {
        if (this.toggleButtonConf == null) {
            throw new IllegalStateException("Button type is: " + (Object)((Object)this.buttonType));
        }
        return this.toggleButtonConf;
    }

    public MenuButtonConfiguration getMenuButtonConfiguration() {
        if (this.menuButtonConf == null) {
            throw new IllegalStateException("Button type is: " + (Object)((Object)this.buttonType));
        }
        return this.menuButtonConf;
    }

    public BooleanExpression getDisabled() {
        return this.disabled;
    }

    public String getSecondaryIcon() {
        return this.secondaryIcon;
    }

    public String getPrimaryIcon() {
        return this.primaryIcon;
    }

    public String getIconPosition() {
        return this.iconPosition;
    }

    public boolean showText() {
        return this.showText;
    }

    public boolean showIcon() {
        return this.showIcon;
    }

    public String getText() {
        return this.text;
    }

    public String getTip() {
        return this.tip;
    }

    public String getButtonId() {
        return this.buttonId;
    }

    public ButtonSetConfiguration getParent() {
        return this.parent;
    }

    public ButtonType getButtonType() {
        return this.buttonType;
    }

    public static enum ButtonType {
        SIMPLE_BUTTON("Button"),
        MENU_BUTTON("Menu"),
        TOGGLE_BUTTON("Toggle Button"),
        RADIO_BUTTON("Radio Button");

        private final String name;

        private ButtonType(String name) {
            this.name = name;
        }

        public static ButtonType fromString(String name) {
            if (name != null) {
                for (ButtonType type : ButtonType.values()) {
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

