/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.userfolder;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class DeleteFolderItems
extends BaseAction
implements sapphire.action.DeleteFolderItems {
    @Override
    public int processAction(String actionid, String actionversionid, HashMap properties) {
        int rc = 1;
        try {
            String all;
            String sysuserid = (String)properties.get("sysuserid");
            if (sysuserid == null || sysuserid.length() == 0) {
                sysuserid = this.connectionInfo.getSysuserId();
            }
            String folderid = (String)properties.get("folderid");
            String sdcid = (String)properties.get("sdcid");
            String keyid1 = (String)properties.get("keyid1");
            String keyid2 = (String)properties.get("keyid2");
            String keyid3 = (String)properties.get("keyid3");
            String separator = (String)properties.get("separator");
            if (separator == null || separator.length() == 0) {
                separator = ";";
            }
            if (keyid1.contains(separator) && keyid1.contains("|") && !this.getSDCProcessor().getProperty(sdcid, "keycolumns").equals("1") && (keyid2 == null || keyid2.length() > 0)) {
                String[] sdis = StringUtil.split(keyid1, separator);
                keyid1 = "";
                keyid2 = "";
                keyid3 = "";
                for (int i = 0; i < sdis.length; ++i) {
                    String sdi = sdis[i];
                    String[] parts = StringUtil.split(sdi, "|");
                    keyid1 = keyid1 + separator + parts[0];
                    if (parts.length > 1) {
                        keyid2 = keyid2 + separator + parts[1];
                    }
                    if (parts.length <= 2) continue;
                    keyid3 = keyid3 + separator + parts[2];
                }
                keyid1 = keyid1.substring(separator.length());
                keyid2 = keyid2.length() > 0 ? keyid2.substring(separator.length()) : "";
                String string = keyid3 = keyid3.length() > 0 ? keyid3.substring(separator.length()) : "";
            }
            if ((all = (String)properties.get("all")) != null && all.equals("Y")) {
                this.database.executePreparedUpdate("DELETE FROM sysuserfolderitem WHERE sysuserid=? AND sysuserfolderid=?", new Object[]{sysuserid, folderid});
            } else if (keyid1 != null && keyid1.length() > 0) {
                DataSet ds = new DataSet();
                ds.addColumnValues("linkkeyid1", 0, keyid1, separator);
                ds.addColumnValues("linkkeyid2", 0, keyid2, separator, "(null)");
                ds.addColumnValues("linkkeyid3", 0, keyid3, separator, "(null)");
                ds.padColumns();
                for (int i = 0; i < ds.size(); ++i) {
                    String linkkeyid1 = ds.getValue(i, "linkkeyid1");
                    String linkkeyid2 = ds.getValue(i, "linkkeyid2");
                    String linkkeyid3 = ds.getValue(i, "linkkeyid3");
                    if (linkkeyid1.indexOf("|") > 0) {
                        String[] parts = StringUtil.split(keyid1, "|");
                        linkkeyid1 = parts[0];
                        if (parts.length > 1) {
                            linkkeyid2 = parts[1];
                        }
                        if (parts.length > 2) {
                            linkkeyid3 = parts[2];
                        }
                    }
                    this.database.executePreparedUpdate("DELETE FROM sysuserfolderitem WHERE sysuserid=? AND sysuserfolderid=? AND linksdcid=? AND linkkeyid1=? AND linkkeyid2=? AND linkkeyid3=?", new Object[]{sysuserid, folderid, sdcid, linkkeyid1, linkkeyid2, linkkeyid3});
                }
            }
        }
        catch (SapphireException e) {
            rc = this.setError("Unable to delete items from the folder");
        }
        return rc;
    }
}

