/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.documents;

import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class SetDocumentValue
extends BaseAction
implements DocumentConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        if (properties.getProperty("documentid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Document id missing!");
        }
        if (properties.getProperty("documentversionid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Document version id missing!");
        }
        PropertyList editProps = new PropertyList();
        editProps.setProperty("sdcid", "LV_Document");
        editProps.setProperty("keyid1", properties.getProperty("documentid"));
        editProps.setProperty("keyid2", properties.getProperty("documentversionid"));
        editProps.setProperty("sysuserid1", properties.getProperty("sysuserid1"));
        editProps.setProperty("auditreason", properties.getProperty("auditreason"));
        editProps.setProperty("auditactivity", properties.getProperty("auditactivity", ""));
        editProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag", "N"));
        editProps.setProperty("auditdt", properties.getProperty("auditdt"));
        editProps.setProperty("applylock", properties.getProperty("applylock"));
        this.getActionProcessor().processAction("EditSDI", "1", editProps);
    }
}

