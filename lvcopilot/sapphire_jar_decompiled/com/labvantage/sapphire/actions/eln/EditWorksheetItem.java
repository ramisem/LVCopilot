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

public class EditWorksheetItem
extends BaseELNAction
implements sapphire.action.EditWorksheetItem {
    String[] optionProps = new String[]{"showitemoptions", "showitemconfig", "showdefinecaption", "allowdeleteitem", "esigdeleteitem", "esigsaveitem", "allowmodifyitem", "allowitemnotes", "allowitemattributes", "allowitemattachments", "excludeitemfromexport", "itemcompletion", "esigitemcompletion"};

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetitemid = properties.getProperty("worksheetitemid");
        String worksheetitemversionid = properties.getProperty("worksheetitemversionid", "1");
        DataSet item = this.getWorksheetItem(worksheetitemid, worksheetitemversionid);
        String worksheetid = properties.getProperty("worksheetid", item.getValue(0, "worksheetid"));
        String worksheetversionid = properties.getProperty("worksheetversionid", item.getValue(0, "worksheetversionid"));
        if (!item.getValue(0, "itemstatus").equals("InProgress")) {
            throw new SapphireException("Blocked editing worksheetitem " + EditWorksheetItem.getIdVersionText(worksheetitemid, worksheetitemversionid) + " as the status is not " + "InProgress");
        }
        if (properties.containsKey("itemstatus")) {
            throw new SapphireException("INVALID_PROPERTY", "Status cannot be set with EditWorksheetItem - use SetWorksheetItemStatus");
        }
        if (properties.containsKey("contents")) {
            throw new SapphireException("INVALID_PROPERTY", "Contents cannot be set with EditWorksheetItem - use SetWorksheetItemContent");
        }
        if (properties.containsKey("config")) {
            throw new SapphireException("INVALID_PROPERTY", "Config cannot be set with EditWorksheetItem - use SetWorksheetItemConfig");
        }
        if (this.sectionInProgress(this.database, item.getValue(0, "worksheetsectionid"), item.getValue(0, "worksheetsectionversionid"))) {
            properties.remove("worksheetitemid");
            properties.remove("worksheetitemversionid");
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_WorksheetItem");
            editProps.setProperty("keyid1", worksheetitemid);
            editProps.setProperty("keyid2", worksheetitemversionid);
            editProps.putAll(properties);
            boolean hasOption = false;
            for (int i = 0; i < this.optionProps.length && !hasOption; ++i) {
                hasOption = properties.containsKey(this.optionProps[i]) && properties.getProperty(this.optionProps[i]).length() > 0;
            }
            PropertyList initialOptions = new PropertyList();
            String options = item.getValue(0, "options");
            initialOptions.setPropertyList(options);
            if (hasOption) {
                this.buildOptionProps(this.optionProps, options, properties, editProps);
            }
            WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, (HashMap)item.get(0));
            worksheetItem.validateEdit(editProps);
            String changes = this.propertyValues(editProps, initialOptions, new String[0]);
            editProps.setProperty("worksheet_action", "Y");
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
            worksheetItem.postEdit();
            this.addActivityLog(worksheetid, worksheetversionid, "Edit", "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "Edited worksheet item values: " + changes);
        }
    }
}

