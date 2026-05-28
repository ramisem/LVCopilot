/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.AbstractButton;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.RadioButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class RadioButton
extends AbstractButton {
    private final RadioButtonConfiguration radioButtonConf;

    public RadioButton(RadioButtonConfiguration radioButtonConf) {
        super(radioButtonConf.getParent());
        this.radioButtonConf = radioButtonConf;
    }

    @Override
    public PropertyList getProps(ChartBindingMap chartBindingMap, PropertyList argumentValueList) throws SapphireException {
        String radioButtonArgumentValue;
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Chart binding map is null");
        }
        if (argumentValueList == null) {
            throw new IllegalArgumentException("Argument value list is null");
        }
        PropertyList buttonProps = super.getProps(chartBindingMap, argumentValueList);
        PropertyList radioButtonProps = new PropertyList();
        buttonProps.setProperty("radiobuttonprops", radioButtonProps);
        boolean checked = false;
        String argumentId = this.radioButtonConf.getArgumentId();
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
        if (argumentValue.equals(radioButtonArgumentValue = this.radioButtonConf.getArgumentValue().evaluate(chartBindingMap))) {
            checked = true;
        }
        radioButtonProps.setProperty("argumentid", argumentId);
        radioButtonProps.setProperty("checked", checked ? "Y" : "N");
        radioButtonProps.setProperty("argumentvalue", radioButtonArgumentValue);
        return buttonProps;
    }
}

