/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.admin.ddt.LV_Worksheet;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_WorksheetItem
extends BaseSDCRules
implements ELNConstants {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if ("N".equals(actionProps.getProperty("worksheet_action", "N")) && "N".equals(actionProps.getProperty("importsnapshot", "N"))) {
            throw new SapphireException("New worksheet items must be created with the AddWorksheetItem action or Imported");
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if ("N".equals(actionProps.getProperty("worksheet_action", "N")) && "N".equals(actionProps.getProperty("importsnapshot", "N")) && !"E".equals(actionProps.getProperty("versionstatus"))) {
            throw new SapphireException("Worksheet items must be edited with the worksheet item actions or Imported");
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
    public void preCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        LV_Worksheet.resetSequence(primary, this.getSequenceProcessor(), "worksheetitemid");
    }
}

