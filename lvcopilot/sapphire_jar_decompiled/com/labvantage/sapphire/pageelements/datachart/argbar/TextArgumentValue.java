/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.pageelements.datachart.argbar.AbstractArgumentValue;
import com.labvantage.sapphire.pageelements.datachart.argbar.Argument;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.TextValueConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;

public class TextArgumentValue
extends AbstractArgumentValue
implements Serializable {
    private final TextValueConfiguration valueConf;

    public TextArgumentValue(TextValueConfiguration valueConf, String connectionId, ArgumentBarBindingMap bindingMap, Argument parent) {
        super(connectionId, valueConf.getParent(), parent);
        if (bindingMap == null) {
            throw new IllegalArgumentException("Binding map is null");
        }
        this.valueConf = valueConf;
    }

    @Override
    public List<String> getDefaultValues(ArgumentBarBindingMap bindingMap) throws SapphireException {
        if (bindingMap == null) {
            throw new IllegalArgumentException("Binding map is null");
        }
        String value = this.valueConf.getValue().evaluate(bindingMap);
        ArrayList<String> defaultValueList = new ArrayList<String>();
        defaultValueList.add(value);
        return defaultValueList;
    }
}

