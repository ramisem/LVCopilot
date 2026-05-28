/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FindKeyId
extends BaseAction
implements sapphire.action.FindKeyId {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String sdcid = propertyList.getProperty("sdcid");
        String separator = propertyList.getProperty("separator", ";");
        boolean failIfNotFound = "Y".equals(propertyList.getProperty("failifnotfound", "Y"));
        PropertyList externalKeyCols = (PropertyList)propertyList.clone();
        externalKeyCols.deleteProperty("sdcid");
        externalKeyCols.deleteProperty("failifnotfound");
        externalKeyCols.deleteProperty("separator");
        externalKeyCols.deleteProperty("applylock");
        externalKeyCols.deleteProperty("foundkeyid1");
        externalKeyCols.deleteProperty("foundkeyid2");
        externalKeyCols.deleteProperty("foundkeyid3");
        HashMap sdcProperties = this.getSDCProcessor().getSDCProperties(sdcid);
        if (sdcProperties == null || sdcProperties.size() == 0) {
            throw new SapphireException("sdcid is invalid");
        }
        PropertyListCollection c = this.getSDCProcessor().getColumns(sdcid);
        if (c == null || c.size() == 0) {
            throw new SapphireException("No columns found for sdc:" + sdcid);
        }
        Set names = externalKeyCols.keySet();
        String[] colnames = new String[names.size()];
        Iterator iter = names.iterator();
        int colcount = 0;
        String whereclause = "";
        DataSet findInfo = new DataSet();
        while (iter.hasNext()) {
            String columnname = iter.next().toString();
            if (c.find("columnid", columnname) == null) continue;
            if (whereclause.length() > 0) {
                whereclause = whereclause + " AND ";
            }
            whereclause = whereclause + columnname + " = ?";
            colnames[colcount] = columnname;
            String[] keycolvalues = StringUtil.split(externalKeyCols.getProperty(columnname, ""), separator);
            String trimmedkeyes = "";
            for (int i = 0; i < keycolvalues.length; ++i) {
                String currval = keycolvalues[i].trim();
                if (trimmedkeyes.length() > 0) {
                    trimmedkeyes = trimmedkeyes + separator;
                }
                trimmedkeyes = trimmedkeyes + currval;
            }
            findInfo.addColumnValues(columnname, 0, trimmedkeyes, separator);
            ++colcount;
        }
        if (colcount == 0) {
            throw new SapphireException("The criteria to find the keyids is not specified.");
        }
        findInfo.padColumns();
        findInfo.setSequence("sequence");
        DataSet keyesFetched = new DataSet();
        keyesFetched.addColumn("keyid1", 0);
        keyesFetched.addColumn("keyid2", 0);
        keyesFetched.addColumn("keyid3", 0);
        int keycount = Integer.parseInt((String)sdcProperties.get("keycolumns"));
        String[] keycols = new String[keycount];
        String columns = "";
        String tablename = (String)sdcProperties.get("tableid");
        for (int i = 0; i < keycount; ++i) {
            keycols[i] = sdcProperties.get("keycolid" + (i + 1)).toString();
            if (columns.length() > 0) {
                columns = columns + ",";
            }
            columns = columns + keycols[i];
        }
        String sql = " SELECT " + columns + " FROM " + tablename + " WHERE " + whereclause;
        for (int i = 0; i < findInfo.getRowCount(); ++i) {
            int keynum;
            if (keyesFetched.getRowCount() > 0) {
                HashMap<String, String> filter = new HashMap<String, String>();
                for (int findcol = 0; findcol < colcount; ++findcol) {
                    filter.put(colnames[findcol], findInfo.getString(i, colnames[findcol], ""));
                }
                DataSet match = keyesFetched.getFilteredDataSet(filter);
                if (match != null && match.getRowCount() > 0) {
                    int currRow = keyesFetched.addRow();
                    for (int keynum2 = 0; keynum2 < keycount; ++keynum2) {
                        keyesFetched.setString(currRow, "keyid" + (keynum2 + 1), match.getString(0, "keyid" + (keynum2 + 1)));
                    }
                    continue;
                }
            }
            Object[] vals = new Object[colcount];
            for (int col = 0; col < colcount; ++col) {
                vals[col] = findInfo.getString(i, colnames[col], "");
            }
            this.database.createPreparedResultSet(sql, vals);
            int currRow = keyesFetched.addRow();
            if (!this.database.getNext()) {
                if (failIfNotFound) {
                    throw new SapphireException("Did not find a SDI for one or more items");
                }
                for (keynum = 0; keynum < keycount; ++keynum) {
                    keyesFetched.setString(currRow, "keyid" + (keynum + 1), "");
                }
            } else {
                for (keynum = 0; keynum < keycount; ++keynum) {
                    keyesFetched.setString(currRow, "keyid" + (keynum + 1), this.database.getString(keycols[keynum]));
                }
                if (this.database.getNext()) {
                    throw new SapphireException("More than one matches found for an item");
                }
            }
            for (int findcol = 0; findcol < colcount; ++findcol) {
                keyesFetched.setString(currRow, colnames[findcol], vals[findcol].toString());
            }
        }
        propertyList.setProperty("foundkeyid1", keyesFetched.getColumnValues("keyid1", separator));
        propertyList.setProperty("foundkeyid2", keyesFetched.getColumnValues("keyid2", separator));
        propertyList.setProperty("foundkeyid3", keyesFetched.getColumnValues("keyid3", separator));
    }
}

