/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.eln.AddWorksheetSDI;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SDIList;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddWorksheetItemSDI
extends BaseELNAction
implements sapphire.action.AddWorksheetItemSDI {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.getActionProcessor().processActionClass(AddWorksheetSDI.class.getName(), properties);
        String worksheetitemid = properties.getProperty("worksheetitemid");
        String worksheetitemversionid = properties.getProperty("worksheetitemversionid");
        String sdcid = properties.getProperty("sdcid");
        int keycols = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
        String[] keyid1 = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2 = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3 = StringUtil.split(properties.getProperty("keyid3"), ";");
        DataSet item = this.getWorksheetItem(worksheetitemid, worksheetitemversionid);
        if (!item.getValue(0, "itemstatus").equals("InProgress")) {
            throw new SapphireException("Blocked adding SDIs to worksheetitem " + AddWorksheetItemSDI.getIdVersionText(worksheetitemid, worksheetitemversionid) + " as the status is not " + "InProgress");
        }
        if (this.sectionInProgress(this.database, item.getValue(0, "worksheetsectionid"), item.getValue(0, "worksheetsectionversionid"))) {
            int i;
            DataSet worksheetsdis = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitemsdi WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid});
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("sdcid", sdcid);
            for (i = 0; i < keyid1.length; ++i) {
                int findRow;
                findMap.put("keyid1", keyid1[i]);
                if (keycols >= 2) {
                    findMap.put("keyid2", keyid2[i]);
                }
                if (keycols >= 3) {
                    findMap.put("keyid3", keyid3[i]);
                }
                if ((findRow = worksheetsdis.findRow(findMap)) >= 0) {
                    worksheetsdis.deleteRow(findRow);
                    continue;
                }
                int row = worksheetsdis.addRow();
                worksheetsdis.setValue(row, "worksheetitemid", worksheetitemid);
                worksheetsdis.setValue(row, "worksheetitemversionid", worksheetitemversionid);
                worksheetsdis.setValue(row, "sdcid", sdcid);
                worksheetsdis.setValue(row, "keyid1", keyid1[i]);
                worksheetsdis.setValue(row, "keyid2", keycols >= 2 ? keyid2[i] : "(null)");
                worksheetsdis.setValue(row, "keyid3", keycols >= 3 ? keyid3[i] : "(null)");
                worksheetsdis.setString(row, "__add", "Y");
            }
            for (i = worksheetsdis.size() - 1; i >= 0; --i) {
                if (!worksheetsdis.getValue(i, "__add", "N").equals("N")) continue;
                worksheetsdis.deleteRow(i);
            }
            if (worksheetsdis.size() > 0) {
                DataSetUtil.insert(this.database, worksheetsdis, "worksheetitemsdi");
                SDIList sdiList = new SDIList();
                sdiList.addSDIList(properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
                this.addActivityLog(properties.getProperty("worksheetid"), properties.getProperty("worksheetversionid"), "Add", "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "Added " + this.getSDCProcessor().getProperty(sdcid, "plural") + ": " + sdiList.toText());
            }
        }
    }
}

