/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AddWorksheetActivity
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String targetsdcid = properties.getProperty("targetsdcid", "LV_Worksheet");
        String worksheetid = properties.getProperty("worksheetid");
        String worksheetversionid = properties.getProperty("worksheetversionid");
        String reporteventid = properties.getProperty("reporteventid");
        DataSet props = new DataSet();
        props.addColumnValues("targetkeyid1", 0, properties.getProperty("targetkeyid1"), ";", worksheetid);
        props.addColumnValues("targetkeyid2", 0, properties.getProperty("targetkeyid2"), ";", worksheetversionid);
        if (worksheetid.length() == 0 || worksheetversionid.length() == 0) {
            DataSet item;
            String firstTargetkeyid1 = props.getValue(0, "targetkeyid1");
            String firstTargetkeyid2 = props.getValue(0, "targetkeyid2");
            if (targetsdcid.equals("LV_WorksheetItem")) {
                item = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetid, worksheetversionid FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{firstTargetkeyid1, firstTargetkeyid2});
                worksheetid = item.getValue(0, "worksheetid");
                worksheetversionid = item.getValue(0, "worksheetversionid");
            } else if (targetsdcid.equals("LV_WorksheetSection")) {
                item = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetid, worksheetversionid FROM worksheetsection WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?", new Object[]{firstTargetkeyid1, firstTargetkeyid2});
                worksheetid = item.getValue(0, "worksheetid");
                worksheetversionid = item.getValue(0, "worksheetversionid");
            } else if (targetsdcid.equals("LV_Worksheet")) {
                worksheetid = firstTargetkeyid1;
                worksheetversionid = firstTargetkeyid2;
            }
        }
        for (int i = 0; i < props.size(); ++i) {
            this.addActivityLog(worksheetid, worksheetversionid, reporteventid, properties.getProperty("activitytype"), targetsdcid, props.getValue(i, "targetkeyid1"), props.getValue(i, "targetkeyid2"), props.getValue(i, "targetkeyid3"), properties.getProperty("activitylog"));
        }
    }
}

