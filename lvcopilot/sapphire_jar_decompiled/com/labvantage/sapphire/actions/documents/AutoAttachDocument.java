/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.documents;

import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AutoAttachDocument
extends BaseAction
implements DocumentConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String documentid = properties.getProperty("documentid");
        String documentversionid = properties.getProperty("documentversionid");
        String[] fieldid = StringUtil.split(properties.getProperty("fieldid"), ";");
        String[] sdcid = StringUtil.split(properties.getProperty("sdcid"), ";");
        String[] sdckeycols = StringUtil.split(properties.getProperty("sdckeycols"), ";");
        String[] action = StringUtil.split(properties.getProperty("action"), ";");
        String[] keyid1 = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2 = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3 = StringUtil.split(properties.getProperty("keyid3"), ";");
        String[] oldkeyid1 = StringUtil.split(properties.getProperty("oldkeyid1"), ";");
        String[] oldkeyid2 = StringUtil.split(properties.getProperty("oldkeyid2"), ";");
        String[] oldkeyid3 = StringUtil.split(properties.getProperty("oldkeyid3"), ";");
        String[] attachmentnum = StringUtil.split(properties.getProperty("attachmentnum"), ";");
        for (int i = 0; i < fieldid.length; ++i) {
            int keycols = Integer.parseInt(sdckeycols[i]);
            if (action[i].equals("U")) {
                this.logger.info("Updating autoattachments on field '" + fieldid[i] + "' with '" + sdcid[i]);
                this.deleteAttachments(sdcid[i], oldkeyid1[i], oldkeyid2[i], oldkeyid3[i], keycols);
                this.insertAttachment(documentid, documentversionid, sdcid[i], keyid1[i], keyid2[i], keyid3[i], attachmentnum[i]);
                continue;
            }
            if (action[i].equals("I")) {
                this.logger.info("Inserting autoattachments on field '" + fieldid[i] + "' with '" + sdcid[i]);
                this.insertAttachment(documentid, documentversionid, sdcid[i], keyid1[i], keyid2[i], keyid3[i], attachmentnum[i]);
                continue;
            }
            if (action[i].equals("D")) {
                this.logger.info("Removing autoattachments on field '" + fieldid[i] + "' with '" + sdcid[i]);
                this.deleteAttachments(sdcid[i], oldkeyid1[i], oldkeyid2[i], oldkeyid3[i], keycols);
                continue;
            }
            if (!action[i].equals("R")) continue;
            this.logger.info("Reconciling autoattachments on field '" + fieldid[i] + "' with '" + sdcid[i]);
            this.database.executePreparedUpdate("DELETE FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum NOT IN ( " + attachmentnum[i] + " )", new Object[]{sdcid[i], oldkeyid1[i], keycols > 1 ? oldkeyid2[i] : "(null)", keycols > 2 ? oldkeyid3[i] : "(null)"});
            this.database.executePreparedUpdate("INSERT INTO sdiattachment ( sdcid, keyid1, keyid2, keyid3, attachmentnum, attachmentdesc, filename, attachment, typeflag, sourcefilename, attachmentuse, attachmentlabel, createby, createdt ) SELECT '" + sdcid[i] + "', '" + keyid1[i] + "', '" + (keyid2[i].length() > 0 ? keyid2[i] : "(null)") + "', '" + (keyid3[i].length() > 0 ? keyid3[i] : "(null)") + "',     attachmentnum, attachmentdesc, filename, attachment, typeflag, sourcefilename, attachmentuse, attachmentlabel, createby, createdt FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum IN ( " + attachmentnum[i] + " ) AND attachmentnum NOT IN (SELECT attachmentnum FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? )", new Object[]{"LV_Document", documentid, documentversionid, "(null)", sdcid[i], keyid1[i], keyid2[i].length() > 0 ? keyid2[i] : "(null)", keyid3[i].length() > 0 ? keyid3[i] : "(null)"});
        }
    }

    private void deleteAttachments(String sdcid, String oldkeyid1, String oldkeyid2, String oldkeyid3, int keycols) throws SapphireException {
        this.database.executePreparedUpdate("DELETE FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ?", new Object[]{sdcid, oldkeyid1, keycols > 1 ? oldkeyid2 : "(null)", keycols > 2 ? oldkeyid3 : "(null)"});
    }

    private void insertAttachment(String documentid, String documentversionid, String sdcid, String keyid1, String keyid2, String keyid3, String attachmentnum) throws SapphireException {
        this.database.executePreparedUpdate("INSERT INTO sdiattachment ( sdcid, keyid1, keyid2, keyid3, attachmentnum, attachmentdesc, filename, attachment, typeflag, sourcefilename, attachmentuse, attachmentlabel, createby, createdt ) SELECT '" + sdcid + "', '" + keyid1 + "', '" + (keyid2.length() > 0 ? keyid2 : "(null)") + "', '" + (keyid3.length() > 0 ? keyid3 : "(null)") + "',     attachmentnum, attachmentdesc, filename, attachment, typeflag, sourcefilename, attachmentuse, attachmentlabel, createby, createdt FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum IN ( " + attachmentnum + " )", new Object[]{"LV_Document", documentid, documentversionid, "(null)"});
    }
}

