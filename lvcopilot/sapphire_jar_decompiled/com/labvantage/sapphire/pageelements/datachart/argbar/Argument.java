/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public interface Argument {
    public PropertyList getProps(ArgumentBarBindingMap var1, String var2) throws SapphireException;

    public String getArgumentId();

    public void applyChanges(PropertyList var1, String var2);

    public void setArgumentValue(String var1, String var2);

    public void populateArgumentValueList(PropertyList var1, ArgumentBarBindingMap var2, String var3) throws SapphireException;

    public List<String> createValueList(List<String> var1);

    public List<String> createDisplayValueList(List<String> var1);

    public ArgumentConfiguration getArgumentConfiguration();
}

