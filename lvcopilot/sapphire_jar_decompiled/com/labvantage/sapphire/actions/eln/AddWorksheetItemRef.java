/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AddWorksheetItemRef
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetitemversionid;
        String worksheetitemid = properties.getProperty("worksheetitemid");
        DataSet item = this.getWorksheetItem(worksheetitemid, worksheetitemversionid = properties.getProperty("worksheetitemversionid"));
        if (!item.getValue(0, "itemstatus").equals("InProgress")) {
            throw new SapphireException("Blocked adding references to worksheetitem " + AddWorksheetItemRef.getIdVersionText(worksheetitemid, worksheetitemversionid) + " as the status is not " + "InProgress");
        }
        if (this.sectionInProgress(this.database, item.getValue(0, "worksheetsectionid"), item.getValue(0, "worksheetsectionversionid"))) {
            DataSet worksheetrefs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitemreference WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid});
            PropertyList config = new PropertyList();
            config.setPropertyList(item.getClob(0, "config"));
            boolean update = true;
            if (worksheetrefs.size() == 0) {
                worksheetrefs.addRow();
                worksheetrefs.setValue(0, "worksheetitemid", worksheetitemid);
                worksheetrefs.setValue(0, "worksheetitemversionid", worksheetitemversionid);
                worksheetrefs.setValue(0, "referenceid", String.valueOf(this.getSequenceProcessor().getSequence("LV_WorksheetItem", "reference")));
                update = false;
            }
            if (properties.getProperty("refworksheetid").length() > 0) {
                worksheetrefs.setValue(0, "refworksheetid", properties.getProperty("refworksheetid"));
            }
            if (properties.getProperty("refworksheetversionid").length() > 0) {
                worksheetrefs.setValue(0, "refworksheetversionid", properties.getProperty("refworksheetversionid"));
            }
            if (properties.getProperty("refsdcid").length() > 0) {
                worksheetrefs.setValue(0, "refsdcid", properties.getProperty("refsdcid"));
            }
            if (properties.getProperty("refkeyid1").length() > 0) {
                worksheetrefs.setValue(0, "refkeyid1", properties.getProperty("refkeyid1"));
            }
            if (properties.getProperty("refkeyid2").length() > 0) {
                worksheetrefs.setValue(0, "refkeyid2", properties.getProperty("refkeyid2"));
            }
            if (properties.getProperty("refkeyid3").length() > 0) {
                worksheetrefs.setValue(0, "refkeyid3", properties.getProperty("refkeyid3"));
            }
            worksheetrefs.setValue(0, "reffunction", properties.getProperty("reffunction", config.getProperty("copycontent", "N").equals("N") ? "link" : "include"));
            if (update) {
                DataSetUtil.update(this.database, worksheetrefs, "worksheetitemreference", new String[]{"worksheetitemid", "worksheetitemversionid", "referenceid"});
            } else {
                DataSetUtil.insert(this.database, worksheetrefs, "worksheetitemreference");
            }
            WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, (HashMap)item.get(0));
            worksheetItem.validateReference(worksheetrefs.getInt(0, "referenceid"));
            this.addActivityLog(properties.getProperty("worksheetid"), properties.getProperty("worksheetversionid"), "Add", "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "Added reference to worksheet " + AddWorksheetItemRef.getIdVersionText(properties.getProperty("refworksheetid"), properties.getProperty("refworksheetversionid")));
        }
    }
}

