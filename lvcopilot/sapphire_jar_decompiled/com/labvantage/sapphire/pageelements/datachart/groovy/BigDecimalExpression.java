/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.groovy;

import com.labvantage.sapphire.pageelements.datachart.groovy.AbstractExpression;
import com.labvantage.sapphire.pageelements.datachart.groovy.BindingMap;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import sapphire.SapphireException;

public final class BigDecimalExpression
extends AbstractExpression
implements Serializable {
    public BigDecimalExpression(String expression) {
        super(expression);
    }

    public BigDecimalExpression(BigDecimalExpression copy) {
        super(copy);
    }

    @Override
    public BigDecimal evaluateNoException(BindingMap bindingMap) {
        String evalString = this.evalNoException(bindingMap);
        BigDecimal returnValue = null;
        try {
            returnValue = Util.parseBigDecimal(evalString);
        }
        catch (ParseException parseException) {
            // empty catch block
        }
        return returnValue;
    }

    @Override
    public BigDecimal evaluate(BindingMap bindingMap) throws SapphireException {
        BigDecimal returnValue;
        String evalString = this.eval(bindingMap);
        try {
            returnValue = Util.parseBigDecimal(evalString);
        }
        catch (ParseException e) {
            throw new SapphireException("Cannot parse number: " + evalString, e);
        }
        return returnValue;
    }
}

