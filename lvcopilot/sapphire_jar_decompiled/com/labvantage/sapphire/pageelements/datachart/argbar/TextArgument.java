/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.pageelements.datachart.argbar.AbstractArgument;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.TextArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import java.io.Serializable;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class TextArgument
extends AbstractArgument
implements Serializable {
    public TextArgument(TextArgumentConfiguration textArgumentConf, String connectionId, PropertyList requestParams, PropertyList argumentValueList, String requestId) throws SapphireException {
        super(connectionId, textArgumentConf.getParent(), new ArgumentBarBindingMap(argumentValueList, requestParams, connectionId), argumentValueList, requestId);
    }
}

