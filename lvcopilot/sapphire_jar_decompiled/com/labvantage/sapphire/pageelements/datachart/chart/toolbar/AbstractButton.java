/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.Button;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.io.Serializable;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public abstract class AbstractButton
implements Button,
Serializable {
    private boolean visible;
    private final ButtonConfiguration buttonConf;

    public AbstractButton(ButtonConfiguration buttonConf) {
        if (buttonConf == null) {
            throw new IllegalArgumentException("Button configuration is null");
        }
        this.buttonConf = buttonConf;
    }

    @Override
    public String getButtonId() {
        return this.buttonConf.getButtonId();
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public PropertyList getProps(ChartBindingMap chartBindingMap, PropertyList argumentValueList) throws SapphireException {
        ButtonConfiguration.ButtonType buttonType;
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Chart binding map is null");
        }
        if (argumentValueList == null) {
            throw new IllegalArgumentException("Argument value list is null");
        }
        boolean showText = this.buttonConf.showText();
        boolean showIcon = this.buttonConf.showIcon();
        String buttonSetId = this.buttonConf.getParent().getButtonSetId();
        String text = this.buttonConf.getText();
        String tip = this.buttonConf.getTip();
        String iconPosition = this.buttonConf.getIconPosition();
        String primaryIcon = this.buttonConf.getPrimaryIcon();
        String secondaryIcon = this.buttonConf.getSecondaryIcon();
        String icon = primaryIcon;
        if (primaryIcon.isEmpty() && !secondaryIcon.isEmpty()) {
            icon = secondaryIcon;
            iconPosition = "end";
        }
        if ((buttonType = this.buttonConf.getButtonType()).equals((Object)ButtonConfiguration.ButtonType.TOGGLE_BUTTON) || buttonType.equals((Object)ButtonConfiguration.ButtonType.RADIO_BUTTON)) {
            icon = showIcon ? "Y" : "N";
        }
        boolean disabled = this.buttonConf.getDisabled().evaluate(chartBindingMap);
        PropertyList buttonProps = new PropertyList();
        buttonProps.setProperty("buttonid", buttonSetId + this.getButtonId());
        buttonProps.setProperty("buttontype", buttonType.getName());
        buttonProps.setProperty("text", text);
        buttonProps.setProperty("tip", tip);
        buttonProps.setProperty("showtext", showText ? "Y" : "N");
        buttonProps.setProperty("showicon", showIcon ? "Y" : "N");
        buttonProps.setProperty("iconposition", iconPosition);
        buttonProps.setProperty("icon", icon);
        buttonProps.setProperty("disabled", disabled ? "Y" : "N");
        return buttonProps;
    }
}

