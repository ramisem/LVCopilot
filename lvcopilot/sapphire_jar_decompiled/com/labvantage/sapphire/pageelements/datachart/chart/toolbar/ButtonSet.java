/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.AbstractButton;
import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.Button;
import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.MenuButton;
import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.RadioButton;
import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.SimpleButton;
import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.ToggleButton;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ButtonSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ButtonSet
implements Serializable {
    private final String buttonSetId;
    private final String label;
    private final List<Button> buttonList;
    private final String tip;

    public ButtonSet(ButtonSetConfiguration buttonSetConf) {
        if (buttonSetConf == null) {
            throw new IllegalArgumentException("Button set configuration is null");
        }
        this.buttonSetId = buttonSetConf.getButtonSetId();
        this.label = buttonSetConf.getLabel();
        this.tip = buttonSetConf.getTip();
        this.buttonList = new ArrayList<Button>();
        for (ButtonConfiguration buttonConf : buttonSetConf.getButtonConfigurationList()) {
            AbstractButton button;
            ButtonConfiguration.ButtonType buttonType = buttonConf.getButtonType();
            if (buttonType == ButtonConfiguration.ButtonType.SIMPLE_BUTTON) {
                button = new SimpleButton(buttonConf.getSimpleButtonConfiguration());
            } else if (buttonType == ButtonConfiguration.ButtonType.MENU_BUTTON) {
                button = new MenuButton(buttonConf.getMenuButtonConfiguration());
            } else if (buttonType == ButtonConfiguration.ButtonType.TOGGLE_BUTTON) {
                button = new ToggleButton(buttonConf.getToggleButtonConfiguration());
            } else if (buttonType == ButtonConfiguration.ButtonType.RADIO_BUTTON) {
                button = new RadioButton(buttonConf.getRadioButtonConfiguration());
            } else {
                throw new IllegalArgumentException("Unknown button type: " + (Object)((Object)buttonType));
            }
            this.buttonList.add(button);
        }
    }

    public void addButton(Button button) {
        if (button == null) {
            throw new IllegalArgumentException("Button is null");
        }
        this.buttonList.add(button);
    }

    public List<Button> getButtonList() {
        return this.buttonList;
    }

    public String getButtonSetId() {
        return this.buttonSetId;
    }

    public String getLabel() {
        return this.label;
    }

    public String getTip() {
        return this.tip;
    }

    public PropertyList getProps(ChartBindingMap chartBindingMap, PropertyList argumentValueList) throws SapphireException {
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Chart binding map is null");
        }
        if (argumentValueList == null) {
            throw new IllegalArgumentException("Argument value list is null");
        }
        PropertyList buttonSetProps = new PropertyList();
        buttonSetProps.setProperty("buttonsetid", this.getButtonSetId());
        buttonSetProps.setProperty("label", this.getLabel());
        buttonSetProps.setProperty("tip", this.getTip());
        PropertyListCollection buttonCollection = new PropertyListCollection();
        buttonSetProps.setProperty("buttoncollection", buttonCollection);
        int buttonCount = 0;
        for (Button button : this.getButtonList()) {
            if (!button.isVisible()) continue;
            ++buttonCount;
            buttonCollection.add(button.getProps(chartBindingMap, argumentValueList));
        }
        buttonSetProps.setProperty("buttoncount", Integer.toString(buttonCount));
        return buttonSetProps;
    }
}

