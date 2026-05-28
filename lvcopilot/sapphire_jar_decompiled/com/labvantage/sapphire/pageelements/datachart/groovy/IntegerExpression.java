/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.groovy;

import com.labvantage.sapphire.pageelements.datachart.groovy.AbstractExpression;
import com.labvantage.sapphire.pageelements.datachart.groovy.BindingMap;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import java.io.Serializable;
import java.text.ParseException;
import sapphire.SapphireException;

public final class IntegerExpression
extends AbstractExpression
implements Serializable {
    public IntegerExpression(String expression) {
        super(expression);
    }

    @Override
    public Integer evaluateNoException(BindingMap bindingMap) {
        String evalString = this.evalNoException(bindingMap);
        Integer returnValue = null;
        try {
            returnValue = Util.parseInteger(evalString);
        }
        catch (ParseException parseException) {
            // empty catch block
        }
        return returnValue;
    }

    @Override
    public Integer evaluate(BindingMap bindingMap) throws SapphireException {
        Integer returnValue;
        String evalString = this.eval(bindingMap);
        try {
            returnValue = Util.parseInteger(evalString);
        }
        catch (ParseException e) {
            throw new SapphireException("Cannot parse number: " + evalString, e);
        }
        return returnValue;
    }
}

