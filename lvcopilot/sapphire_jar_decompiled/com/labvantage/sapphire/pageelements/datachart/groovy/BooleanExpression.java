/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.groovy;

import com.labvantage.sapphire.pageelements.datachart.groovy.AbstractExpression;
import com.labvantage.sapphire.pageelements.datachart.groovy.BindingMap;
import java.io.Serializable;
import sapphire.SapphireException;

public final class BooleanExpression
extends AbstractExpression
implements Serializable {
    public BooleanExpression(String expression) {
        super(expression);
    }

    public BooleanExpression(BooleanExpression copy) {
        super(copy);
    }

    @Override
    public Boolean evaluateNoException(BindingMap bindingMap) {
        return this.evalNoException(bindingMap).toLowerCase().startsWith("y");
    }

    @Override
    public Boolean evaluate(BindingMap bindingMap) throws SapphireException {
        return this.eval(bindingMap).toLowerCase().startsWith("y");
    }
}

