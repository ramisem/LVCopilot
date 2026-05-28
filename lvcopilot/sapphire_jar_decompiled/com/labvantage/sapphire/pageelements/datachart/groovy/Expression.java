/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.groovy;

import com.labvantage.sapphire.pageelements.datachart.groovy.BindingMap;
import sapphire.SapphireException;

public interface Expression {
    public String getExpression();

    public Object evaluateNoException(BindingMap var1);

    public Object evaluate(BindingMap var1) throws SapphireException;
}

