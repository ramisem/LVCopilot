/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.pageelements.datachart.argbar.AbstractArgument;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.EditorStyleArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import java.io.Serializable;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class EditorStyleArgument
extends AbstractArgument
implements Serializable {
    public EditorStyleArgument(EditorStyleArgumentConfiguration editorSyleArgumentConf, String connectionId, PropertyList requestParams, PropertyList argumentValueList, String requestId) throws SapphireException {
        super(connectionId, editorSyleArgumentConf.getParent(), new ArgumentBarBindingMap(argumentValueList, requestParams, connectionId), argumentValueList, requestId);
    }

    @Override
    public PropertyList getProps(ArgumentBarBindingMap bindingMap, String requestId) throws SapphireException {
        PropertyList argumentProps = super.getProps(bindingMap, requestId);
        argumentProps.setProperty("editorstyle", this.getArgumentConfiguration().getEditorStyleArgumentConfiguration().getEditorStyle());
        argumentProps.setProperty("html", this.getArgumentConfiguration().getEditorStyleArgumentConfiguration().getHtml());
        return argumentProps;
    }
}

