/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public interface ArgumentValue {
    public List<String> getValues(String var1);

    public List<String> getDisplayValues(String var1);

    public List<String> getDefaultValues(ArgumentBarBindingMap var1) throws SapphireException;

    public PropertyList getProps(ArgumentBarBindingMap var1, String var2) throws SapphireException;

    public void setValueList(List<String> var1, List<String> var2, String var3);
}

