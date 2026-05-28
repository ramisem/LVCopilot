/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.AbstractButton;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ToggleButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ToggleButton
extends AbstractButton {
    private final ToggleButtonConfiguration toggleButtonConf;

    public ToggleButton(ToggleButtonConfiguration toggleButtonConf) {
        super(toggleButtonConf.getParent());
        this.toggleButtonConf = toggleButtonConf;
    }

    @Override
    public PropertyList getProps(ChartBindingMap chartBindingMap, PropertyList argumentValueList) throws SapphireException {
        PropertyList buttonProps = super.getProps(chartBindingMap, argumentValueList);
        PropertyList toggleButtonProps = new PropertyList();
        buttonProps.setProperty("togglebuttonprops", toggleButtonProps);
        boolean checked = false;
        String argumentId = this.toggleButtonConf.getArgumentId();
        PropertyList argumentProps = argumentValueList.getPropertyListNotNull(argumentId);
        PropertyListCollection valueCollection = argumentProps.getCollectionNotNull("valuecollection");
        String valueSeparator = argumentProps.getProperty("valueseparator");
        StringBuilder argumentValueBuilder = new StringBuilder();
        for (int i = 0; i < valueCollection.size(); ++i) {
            argumentValueBuilder.append(valueSeparator).append(valueCollection.getPropertyList(i).getProperty("value"));
        }
        String argumentValue = "";
        if (argumentValueBuilder.length() > 0) {
            argumentValue = argumentValueBuilder.substring(valueSeparator.length());
        }
        String onValue = this.toggleButtonConf.getOnValue().evaluate(chartBindingMap);
        String offValue = this.toggleButtonConf.getOffValue().evaluate(chartBindingMap);
        if (argumentValue.equals(onValue)) {
            checked = true;
        }
        toggleButtonProps.setProperty("argumentid", argumentId);
        toggleButtonProps.setProperty("checked", checked ? "Y" : "N");
        toggleButtonProps.setProperty("argumentvalue", checked ? offValue : onValue);
        return buttonProps;
    }
}

