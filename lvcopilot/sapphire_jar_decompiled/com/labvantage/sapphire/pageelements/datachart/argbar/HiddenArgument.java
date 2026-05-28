/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.pageelements.datachart.argbar.AbstractArgument;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import java.io.Serializable;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class HiddenArgument
extends AbstractArgument
implements Serializable {
    public HiddenArgument(ArgumentConfiguration argumentConf, String connectionId, PropertyList requestParams, PropertyList argumentValueList, String requestId) throws SapphireException {
        super(connectionId, argumentConf, new ArgumentBarBindingMap(argumentValueList, requestParams, connectionId), argumentValueList, requestId);
    }
}

