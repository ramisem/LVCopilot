/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.userfolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SequenceProcessor;
import sapphire.action.BaseAction;
import sapphire.util.M18NUtil;

public class AddFolder
extends BaseAction
implements sapphire.action.AddFolder {
    @Override
    public int processAction(String actionid, String actionversionid, HashMap properties) {
        int rc = 1;
        try {
            SequenceProcessor sp = this.getSequenceProcessor();
            String sdcid = (String)properties.get("sdcid");
            if (sdcid != null && sdcid.length() > 0) {
                int nextSequence;
                String label;
                String sysuserid = (String)properties.get("sysuserid");
                if (sysuserid == null || sysuserid.length() == 0) {
                    sysuserid = this.connectionInfo.getSysuserId();
                    properties.put("sysuserid", sysuserid);
                }
                if ((label = (String)properties.get("label")) == null || label.length() == 0) {
                    String baselabel;
                    String labelformat = (String)properties.get("labelformat");
                    if (labelformat == null || labelformat.length() == 0) {
                        labelformat = "MMM dd HH:mm";
                    }
                    M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
                    SimpleDateFormat sdf = new SimpleDateFormat();
                    sdf.setTimeZone(m18n.getTimezone());
                    sdf.applyPattern(labelformat);
                    label = baselabel = sdf.format(Calendar.getInstance().getTime());
                    int count = 0;
                    while (this.database.getPreparedCount("SELECT count(*) FROM sysuserfolder WHERE sysuserid=? and folderlabel=?", new Object[]{sysuserid, label}) > 0) {
                        label = baselabel + "-" + ++count;
                    }
                }
                if ((nextSequence = this.database.getPreparedCount("SELECT max(usersequence) maxusersequence FROM sysuserfolder WHERE sysuserid=?", new Object[]{sysuserid}) + 1) < 0) {
                    nextSequence = 0;
                }
                String folderid = "" + sp.getSequence("SysUserid", "__folder");
                this.database.executePreparedUpdate("INSERT INTO sysuserfolder ( sysuserid, sysuserfolderid, linksdcid, folderlabel, usersequence ) VALUES ( ?, ?, ?, ?, ? )", new Object[]{sysuserid, folderid, sdcid, label, new Integer(nextSequence)});
                properties.put("folderid", folderid);
                properties.put("newfolderid", folderid);
                this.getActionProcessor().processAction("AddFolderItems", "1", properties);
            } else {
                rc = this.setError("Unable to create a new folder: No SDC specified.");
            }
        }
        catch (SapphireException e) {
            rc = this.setError("Unable to create a new folder");
        }
        return rc;
    }
}

