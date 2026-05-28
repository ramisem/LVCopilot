/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.documents;

import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SetDocumentStatus
extends BaseAction
implements sapphire.action.SetDocumentStatus,
DocumentConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        if (properties.getProperty("documentid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Document id missing!");
        }
        if (properties.getProperty("documentversionid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Document version id missing!");
        }
        if (properties.getProperty("documentstatus").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Document status invalid!");
        }
        PropertyList editProps = new PropertyList();
        editProps.setProperty("sdcid", "LV_Document");
        editProps.setProperty("keyid1", properties.getProperty("documentid"));
        editProps.setProperty("keyid2", properties.getProperty("documentversionid"));
        editProps.setProperty("documentstatus", properties.getProperty("documentstatus"));
        editProps.setProperty("statusmessage", properties.getProperty("statusmessage"));
        editProps.setProperty("auditreason", properties.getProperty("auditreason"));
        editProps.setProperty("auditactivity", properties.getProperty("auditactivity", ""));
        editProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag", "N"));
        editProps.setProperty("auditdt", properties.getProperty("auditdt"));
        editProps.setProperty("applylock", properties.getProperty("applylock"));
        if (properties.getProperty("processinglog").length() > 0) {
            editProps.setProperty("processinglog", properties.getProperty("processinglog"));
        }
        if (properties.getProperty("newversionstatus").length() > 0) {
            editProps.setProperty("versionstatus", properties.getProperty("newversionstatus"));
        }
        this.getActionProcessor().processAction("EditSDI", "1", editProps);
        if (properties.getProperty("documentstatus").equals("CN")) {
            String[] documentids = StringUtil.split(properties.getProperty("documentid"), ";");
            String[] documentversionids = StringUtil.split(properties.getProperty("documentversionid"), ";");
            for (int i = 0; i < documentids.length; ++i) {
                this.database.createPreparedResultSet("SELECT worksheettype FROM form, document WHERE form.formid = document.formid AND form.formversionid = document.formversionid AND documentid = ? AND documentversionid = ?", new Object[]{documentids[i], documentversionids[i]});
                if (this.database.getNext()) {
                    if (this.database.getValue("worksheettype").equals("workitem")) {
                        this.database.executePreparedUpdate("UPDATE sdiworkitem SET documentid = null, documentversionid = null WHERE documentid = ? AND documentversionid = ?", new Object[]{documentids[i], documentversionids[i]});
                    } else if (this.database.getValue("worksheettype").equals("qcbatch")) {
                        this.database.executePreparedUpdate("UPDATE s_qcbatch SET documentid = null, documentversionid = null, blockflag = null WHERE documentid = ? AND documentversionid = ?", new Object[]{documentids[i], documentversionids[i]});
                    }
                }
                this.database.executePreparedUpdate("UPDATE sdidata SET documentid = null, documentversionid = null, blockflag = 'N' WHERE documentid = ? AND documentversionid = ?", new Object[]{documentids[i], documentversionids[i]});
            }
        }
    }
}

