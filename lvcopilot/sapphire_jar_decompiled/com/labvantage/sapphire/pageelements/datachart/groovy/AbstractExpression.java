/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.groovy;

import com.labvantage.sapphire.pageelements.datachart.groovy.BindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.Expression;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class AbstractExpression
implements Expression,
Serializable {
    private final String expression;

    public AbstractExpression(String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Expression is null");
        }
        this.expression = expression;
    }

    public AbstractExpression(AbstractExpression copy) {
        this.expression = copy.expression;
    }

    private static boolean isGroovyExpression(String expression) {
        return expression.startsWith("$G{");
    }

    private static boolean isTokenExpression(String expression) {
        return expression.contains("[") && expression.contains("]");
    }

    private static boolean requiresEvaluation(String expression) {
        return AbstractExpression.isTokenExpression(expression) || AbstractExpression.isGroovyExpression(expression);
    }

    @Override
    public String getExpression() {
        return this.expression;
    }

    protected String eval(BindingMap bindingMap) throws SapphireException {
        if (bindingMap == null) {
            throw new IllegalArgumentException("Binding map is null");
        }
        String returnValue = this.expression;
        if (AbstractExpression.requiresEvaluation(this.expression)) {
            returnValue = AbstractExpression.isGroovyExpression(this.expression) ? GroovyUtil.evaluate(this.expression, bindingMap.toHashMap()) : this.replaceTokens(this.expression, bindingMap.getTokenValues());
        }
        return returnValue;
    }

    protected String evalNoException(BindingMap bindingMap) {
        String returnValue;
        try {
            returnValue = this.eval(bindingMap);
        }
        catch (SapphireException e) {
            returnValue = e.toString();
        }
        return returnValue;
    }

    private String replaceTokens(String evalExpression, PropertyListCollection tokenValuesCollection) {
        StringBuffer returnValue = new StringBuffer();
        evalExpression = evalExpression.replaceAll("\n", " ").replaceAll("\r", "");
        Pattern pattern = Pattern.compile("\\[(.+?)\\]");
        Matcher matcher = pattern.matcher(evalExpression);
        while (matcher.find()) {
            String propertyId = matcher.group(1);
            boolean foundReplacement = false;
            for (int i = 0; i < tokenValuesCollection.size(); ++i) {
                PropertyList tokenValues = tokenValuesCollection.getPropertyList(i);
                String tokenValue = tokenValues.getProperty(propertyId);
                if (tokenValue.isEmpty()) continue;
                matcher.appendReplacement(returnValue, "");
                returnValue.append(tokenValue);
                foundReplacement = true;
                break;
            }
            if (foundReplacement) continue;
            matcher.appendReplacement(returnValue, "");
            returnValue.append("");
        }
        matcher.appendTail(returnValue);
        return returnValue.toString();
    }

    protected Set<String> findUsedTokens(BindingMap bindingMap) {
        PropertyListCollection tokenValuesCollection = bindingMap.getTokenValues();
        HashSet<String> returnValue = new HashSet<String>();
        String evalExpression = this.expression.replaceAll("\n", " ").replaceAll("\r", "");
        Pattern pattern = Pattern.compile("\\[(.+?)\\]");
        Matcher matcher = pattern.matcher(evalExpression);
        block0: while (matcher.find()) {
            String propertyId = matcher.group(1);
            boolean foundReplacement = false;
            for (int i = 0; i < tokenValuesCollection.size(); ++i) {
                PropertyList tokenValues = tokenValuesCollection.getPropertyList(i);
                String tokenValue = tokenValues.getProperty(propertyId);
                if (tokenValue.isEmpty()) continue;
                returnValue.add(propertyId);
                continue block0;
            }
        }
        return returnValue;
    }
}

