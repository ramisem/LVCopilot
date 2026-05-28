/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.userfolder;

import com.labvantage.sapphire.DataSetUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddFolderItems
extends BaseAction
implements sapphire.action.AddFolderItems {
    @Override
    public void processAction(PropertyList props) throws SapphireException {
        String folderid = props.getProperty("folderid");
        String sdcid = props.getProperty("sdcid");
        String keyid1 = props.getProperty("keyid1");
        String keyid2 = props.getProperty("keyid2");
        String keyid3 = props.getProperty("keyid3");
        String separator = props.getProperty("separator", ";");
        String sysuserid = props.getProperty("sysuserid", this.connectionInfo.getSysuserId());
        if (keyid1 != null && keyid1.length() > 0) {
            DataSet ds = new DataSet();
            ds.addColumn("sysuserid", 0);
            ds.addColumn("sysuserfolderid", 0);
            ds.addColumn("linksdcid", 0);
            ds.addColumn("usersequence", 1);
            ds.addColumnValues("linkkeyid1", 0, keyid1, separator);
            ds.addColumnValues("linkkeyid2", 0, keyid2, separator, "(null)");
            ds.addColumnValues("linkkeyid3", 0, keyid3, separator, "(null)");
            for (int i = 0; i < ds.size(); ++i) {
                String linkkeyid1 = ds.getValue(i, "linkkeyid1");
                if (linkkeyid1.indexOf("|") < 0) continue;
                String[] parts = StringUtil.split(linkkeyid1, "|");
                ds.setString(i, "linkkeyid1", parts[0]);
                if (parts.length > 1) {
                    ds.setString(i, "linkkeyid2", parts[1]);
                }
                if (parts.length <= 2) continue;
                ds.setString(i, "linkkeyid3", parts[2]);
            }
            ds.setValue(0, "sysuserid", sysuserid);
            ds.setValue(0, "sysuserfolderid", folderid);
            ds.setValue(0, "linksdcid", sdcid);
            ds.padColumns();
            String sql = "SELECT linkkeyid1, linkkeyid2, linkkeyid3 FROM sysuserfolderitem WHERE sysuserid=? AND sysuserfolderid=?";
            this.database.createPreparedResultSet(sql, new Object[]{sysuserid, folderid});
            DataSet existing = new DataSet(this.database.getResultSet());
            HashMap<String, String> find = new HashMap<String, String>();
            for (int i = 0; i < existing.size(); ++i) {
                find.put("linkkeyid1", existing.getString(i, "linkkeyid1"));
                find.put("linkkeyid2", existing.getString(i, "linkkeyid2"));
                find.put("linkkeyid3", existing.getString(i, "linkkeyid3"));
                int findRow = ds.findRow(find);
                if (findRow < 0) continue;
                ds.deleteRow(findRow);
            }
            DataSetUtil.insert(this.database, ds, "sysuserfolderitem");
        }
    }
}

