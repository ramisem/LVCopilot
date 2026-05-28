/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.groovy;

import com.labvantage.sapphire.pageelements.datachart.groovy.AbstractExpression;
import com.labvantage.sapphire.pageelements.datachart.groovy.BindingMap;
import java.io.Serializable;
import java.util.Set;
import sapphire.SapphireException;

public final class StringExpression
extends AbstractExpression
implements Serializable {
    public StringExpression(String expression) {
        super(expression);
    }

    public StringExpression(StringExpression copy) {
        super(copy);
    }

    @Override
    public String evaluateNoException(BindingMap bindingMap) {
        return this.evalNoException(bindingMap);
    }

    @Override
    public String evaluate(BindingMap bindingMap) throws SapphireException {
        return this.eval(bindingMap);
    }

    @Override
    public Set<String> findUsedTokens(BindingMap bindingMap) {
        return super.findUsedTokens(bindingMap);
    }
}

