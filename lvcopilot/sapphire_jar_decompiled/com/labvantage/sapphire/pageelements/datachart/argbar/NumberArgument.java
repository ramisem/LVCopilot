/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.pageelements.datachart.argbar.AbstractArgument;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.NumberArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;

public class NumberArgument
extends AbstractArgument
implements Serializable {
    public NumberArgument(NumberArgumentConfiguration numberArgumentConf, String connectionId, PropertyList requestParams, PropertyList argumentValueList, String requestId) throws SapphireException {
        super(connectionId, numberArgumentConf.getParent(), new ArgumentBarBindingMap(argumentValueList, requestParams, connectionId), argumentValueList, requestId);
    }

    @Override
    public List<String> createValueList(List<String> displayValueList) {
        if (displayValueList == null) {
            throw new IllegalArgumentException("Display value list is null");
        }
        M18NUtil m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        ArrayList<String> valueList = new ArrayList<String>();
        for (String displayValue : displayValueList) {
            if (displayValue.isEmpty()) continue;
            BigDecimal value = m18NUtil.parseBigDecimal(displayValue);
            valueList.add(value.toString());
        }
        return valueList;
    }

    @Override
    public List<String> createDisplayValueList(List<String> valueList) {
        if (valueList == null) {
            throw new IllegalArgumentException("Value list is null");
        }
        M18NUtil m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        ArrayList<String> displayValueList = new ArrayList<String>();
        for (String value : valueList) {
            BigDecimal numberValue = new BigDecimal(value);
            String displayValue = m18NUtil.format(numberValue);
            displayValueList.add(displayValue);
        }
        return displayValueList;
    }
}

