/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SetWorksheetItemConfig
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetitemversionid;
        String worksheetid = properties.getProperty("worksheetid");
        String worksheetversionid = properties.getProperty("worksheetversionid");
        String worksheetitemid = properties.getProperty("worksheetitemid");
        DataSet item = this.getWorksheetItem(worksheetitemid, worksheetitemversionid = properties.getProperty("worksheetitemversionid", "1"));
        if (this.sectionInProgress(this.database, item.getValue(0, "worksheetsectionid"), item.getValue(0, "worksheetsectionversionid"))) {
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_WorksheetItem");
            editProps.setProperty("keyid1", worksheetitemid);
            editProps.setProperty("keyid2", worksheetitemversionid);
            WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, (HashMap)item.get(0));
            worksheetItem.setTemplate(properties.getProperty("templateflag").equals("Y"));
            PropertyList config = new PropertyList();
            if (properties.getProperty("propertyid").length() > 0) {
                config.setPropertyList(item.getString(0, "config", ""));
                config.setProperty(properties.getProperty("propertyid"), properties.getProperty("propertyvalue"));
            } else {
                config.setPropertyList(properties.getProperty("config"));
            }
            worksheetItem.validateConfig(config);
            worksheetItem.extractConfigParams(config);
            editProps.setProperty("config", config.toXMLString());
            item.setValue(0, "config", editProps.getProperty("config"));
            worksheetItem.setConfig(config);
            properties.put("worksheetitem", worksheetItem);
            editProps.setProperty("worksheet_action", "Y");
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
            worksheetItem.postConfig();
            this.addActivityLog(worksheetid, worksheetversionid, "SetConfig", "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "Set worksheet item config");
        }
    }
}

