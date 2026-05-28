/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.AbstractButton;
import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.Operation;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.SimpleButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class SimpleButton
extends AbstractButton {
    private final Operation operation;

    public SimpleButton(SimpleButtonConfiguration simpleButtonConf) {
        super(simpleButtonConf.getParent());
        this.operation = new Operation(simpleButtonConf.getOperationConfiguration());
    }

    @Override
    public PropertyList getProps(ChartBindingMap chartBindingMap, PropertyList argumentValueList) throws SapphireException {
        PropertyList buttonProps = super.getProps(chartBindingMap, argumentValueList);
        PropertyList simpleButtonProps = new PropertyList();
        buttonProps.setProperty("simplebuttonprops", simpleButtonProps);
        simpleButtonProps.setProperty("operationprops", this.operation.getProps(chartBindingMap));
        return buttonProps;
    }
}

