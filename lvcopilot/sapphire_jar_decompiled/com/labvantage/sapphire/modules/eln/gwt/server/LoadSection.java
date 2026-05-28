/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import sapphire.SapphireException;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LoadSection
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        SDIData section = this.loadSection(properties.getProperty("worksheetid"), properties.getProperty("worksheetversionid"), properties.getProperty("worksheetsectionid"), properties.getProperty("worksheetsectionversionid"), false, true);
        properties.put("section", section);
    }
}

