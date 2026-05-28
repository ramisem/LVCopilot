/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class CopyWorksheet
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String[] worksheet = this.copyWorksheet(properties.getProperty("worksheetid"), properties.getProperty("worksheetversionid"), properties.getProperty("workbookid"), properties.getProperty("workbookversionid"), properties);
        properties.setProperty("worksheetid", worksheet[0]);
        properties.setProperty("worksheetversionid", worksheet[1]);
    }
}

