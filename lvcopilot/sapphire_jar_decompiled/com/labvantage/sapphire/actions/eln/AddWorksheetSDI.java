/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SDIList;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddWorksheetSDI
extends BaseELNAction
implements sapphire.action.AddWorksheetSDI {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetversionid;
        String worksheetid = properties.getProperty("worksheetid");
        if (this.worksheetInProgress(this.database, worksheetid, worksheetversionid = properties.getProperty("worksheetversionid"))) {
            int i;
            String sdcid = properties.getProperty("sdcid");
            int keycols = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
            String[] keyid1 = StringUtil.split(properties.getProperty("keyid1"), ";");
            String[] keyid2 = StringUtil.split(properties.getProperty("keyid2"), ";");
            String[] keyid3 = StringUtil.split(properties.getProperty("keyid3"), ";");
            DataSet worksheetsdis = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetsdi WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
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
                worksheetsdis.setValue(row, "worksheetid", worksheetid);
                worksheetsdis.setValue(row, "worksheetversionid", worksheetversionid);
                worksheetsdis.setValue(row, "sdcid", sdcid);
                worksheetsdis.setValue(row, "keyid1", keyid1[i]);
                worksheetsdis.setValue(row, "keyid2", keycols >= 2 ? keyid2[i] : "(null)");
                worksheetsdis.setValue(row, "keyid3", keycols >= 3 ? keyid3[i] : "(null)");
                worksheetsdis.setValue(row, "usersequence", Integer.toString(i + 1));
                worksheetsdis.setString(row, "__add", "Y");
            }
            for (i = worksheetsdis.size() - 1; i >= 0; --i) {
                if (!worksheetsdis.getValue(i, "__add", "N").equals("N")) continue;
                worksheetsdis.deleteRow(i);
            }
            if (worksheetsdis.size() > 0) {
                DataSetUtil.insert(this.database, worksheetsdis, "worksheetsdi");
                SDIList sdiList = new SDIList();
                sdiList.addSDIList(properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
                this.addActivityLog(worksheetid, worksheetversionid, "Add", "LV_Worksheet", worksheetid, worksheetversionid, "Added " + this.getSDCProcessor().getProperty(sdcid, "plural") + ": " + sdiList.toText());
            }
        }
    }
}

