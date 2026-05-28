/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import sapphire.SapphireException;
import sapphire.accessor.SequenceProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_Worksheet
extends BaseSDCRules
implements ELNConstants {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if ("N".equals(actionProps.getProperty("worksheet_action", "N")) && "N".equals(actionProps.getProperty("importsnapshot", "N"))) {
            throw new SapphireException("New worksheets must be created with the AddWorksheet action or Imported");
        }
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String worksheetid = primary.getValue(i, "worksheetid");
            String worksheetversionid = primary.getValue(i, "worksheetversionid");
            String worksheetname = primary.getValue(i, "worksheetname");
            String worksheetdesc = primary.getValue(i, "worksheetdesc");
            if (worksheetname.contains("[worksheetid]") || worksheetname.contains("[worksheetversionid]")) {
                worksheetname = StringUtil.replaceAll(worksheetname, "[worksheetid]", worksheetid, false);
                worksheetname = StringUtil.replaceAll(worksheetname, "[worksheetversionid]", worksheetversionid, false);
                primary.setString(i, "worksheetname", worksheetname);
            }
            if (!worksheetdesc.contains("[worksheetid]") && !worksheetdesc.contains("[worksheetversionid]")) continue;
            worksheetdesc = StringUtil.replaceAll(worksheetdesc, "[worksheetid]", worksheetid, false);
            worksheetdesc = StringUtil.replaceAll(worksheetdesc, "[worksheetversionid]", worksheetversionid, false);
            primary.setString(i, "worksheetdesc", worksheetdesc);
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (actionProps.getProperty("activeflag").contains("N")) {
            DataSet oldPrimary = this.getBeforeEditImage().getDataset("primary");
            if (oldPrimary.findRow("templateflag", "N") >= 0) {
                throw new SapphireException("Worksheets cannot be made inactive - only Worksheet Templates");
            }
        } else if ("N".equals(actionProps.getProperty("worksheet_action", "N")) && "N".equals(actionProps.getProperty("importsnapshot", "N")) && !"E".equals(actionProps.getProperty("versionstatus")) && !this.hasPrimaryValueChanged(sdiData.getDataset("primary"), 0, "activeflag") && actionProps.getProperty("securityset").length() == 0 && actionProps.getProperty("securityuser").length() == 0 && actionProps.getProperty("securitydepartment").length() == 0) {
            throw new SapphireException("Worksheets must be edited with the worksheet actions or Imported");
        }
    }

    @Override
    public void postAddSDIAttachment(Attachment attachment) throws SapphireException {
        BaseELNAction.createAttachmentActivityRecord(this.getActionProcessor(), attachment, "I");
    }

    @Override
    public void postEditSDIAttachment(Attachment attachment) throws SapphireException {
        BaseELNAction.createAttachmentActivityRecord(this.getActionProcessor(), attachment, "U");
    }

    @Override
    public void postDeleteSDIAttachment(Attachment attachment) throws SapphireException {
        BaseELNAction.createAttachmentActivityRecord(this.getActionProcessor(), attachment, "D");
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.database.executePreparedUpdate("DELETE FROM worksheetactivitylog WHERE worksheetid=? AND worksheetversionid=?", new String[]{actionProps.getProperty("keyid1"), actionProps.getProperty("keyid2")});
        this.database.executePreparedUpdate("DELETE FROM worksheetcontributor WHERE worksheetid=? AND worksheetversionid=?", new String[]{actionProps.getProperty("keyid1"), actionProps.getProperty("keyid2")});
    }

    static void resetSequence(DataSet primary, SequenceProcessor sequenceProcessor, String keyColId) {
        if (primary != null) {
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String keyId = primary.getString(i, keyColId);
                String keySeq = keyId.substring(0, keyId.length() - 5);
                int seqnum = 0;
                try {
                    seqnum = Integer.parseInt(keyId.substring(keyId.length() - 5));
                    int currentseq = sequenceProcessor.getSequence("LV_Worksheet", keySeq, 0);
                    if (currentseq >= seqnum) continue;
                    int seq = sequenceProcessor.getSequence("LV_Worksheet", keySeq, seqnum - currentseq);
                    seq = 0;
                    continue;
                }
                catch (NumberFormatException e) {
                    Logger.logStackTrace(e);
                }
            }
        }
    }
}

