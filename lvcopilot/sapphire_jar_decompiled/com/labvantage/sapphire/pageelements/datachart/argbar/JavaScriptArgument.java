/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.pageelements.datachart.argbar.AbstractArgument;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.JavaScriptArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import java.io.Serializable;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class JavaScriptArgument
extends AbstractArgument
implements Serializable {
    public JavaScriptArgument(JavaScriptArgumentConfiguration javaScriptArgumentConf, String connectionId, PropertyList requestParams, PropertyList argumentValueList, String requestId) throws SapphireException {
        super(connectionId, javaScriptArgumentConf.getParent(), new ArgumentBarBindingMap(argumentValueList, requestParams, connectionId), argumentValueList, requestId);
    }

    @Override
    public PropertyList getProps(ArgumentBarBindingMap bindingMap, String requestId) throws SapphireException {
        PropertyList argumentProps = super.getProps(bindingMap, requestId);
        JavaScriptArgumentConfiguration javaScriptArgumentConf = this.getArgumentConfiguration().getJavaScriptArgumentConfiguration();
        PropertyList javaScriptArgumentProps = new PropertyList();
        javaScriptArgumentProps.setProperty("javascript", javaScriptArgumentConf.getJavaScript());
        javaScriptArgumentProps.setProperty("inputtype", javaScriptArgumentConf.getInputType().getName());
        argumentProps.setProperty("javascriptargumentprops", javaScriptArgumentProps);
        return argumentProps;
    }
}

