/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.handlers;

import com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler;
import java.util.List;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.xml.PropertyList;

public class IterateFilesAttachmentHandler
extends BaseAttachmentHandler {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void handleData(List<Attachment> attachments, PropertyList properties) throws SapphireException {
        if (attachments == null || attachments.size() == 0) {
            this.logWarn("No attachments/files provided to Iterate Files Handler");
        } else {
            String childhandlerid = properties.getProperty("childattachmenthandlerid", "");
            this.logMessage("Child Handler = " + childhandlerid);
            if (childhandlerid.length() > 0) {
                PropertyList childprops = new PropertyList();
                childprops.setProperty("attachmenthandlerid", childhandlerid);
                childprops.setProperty("returnexecutionlog", "Y");
                if (this.getExecutionId() != null && this.getExecutionId().length() > 0) {
                    childprops.setProperty("execid", this.getExecutionId());
                }
                childprops.setProperty("sdcid", this.getSDCId());
                childprops.setProperty("keyid1", this.getKeyId1());
                if (this.getKeyId2() != null && this.getKeyId2().length() > 0) {
                    childprops.setProperty("keyid2", this.getKeyId2());
                }
                if (this.getKeyId3() != null && this.getKeyId3().length() > 0) {
                    childprops.setProperty("keyid3", this.getKeyId3());
                }
                for (Object key : properties.keySet()) {
                    String propid = key.toString();
                    if (propid.equalsIgnoreCase("attachmentclass") || propid.equalsIgnoreCase("attachmentnum") || propid.equalsIgnoreCase("filename") || propid.equalsIgnoreCase("execid")) continue;
                    childprops.setProperty("property_" + propid, properties.getProperty(propid, ""));
                }
                try {
                    this.logMessage("Iterate Files Handler Started.");
                    childprops.setProperty("property_total_iterations", "" + attachments.size());
                    for (int a = 0; a < attachments.size(); ++a) {
                        Attachment attachment = attachments.get(a);
                        try {
                            try {
                                this.logMessage("Attachment " + attachment.getSourceFilename() + " (" + a + ") handling...");
                                PropertyList toUse = childprops.copy();
                                toUse.setProperty("attachmentnum", "" + attachment.getAttachmentNum());
                                toUse.setProperty("property_current_iteration", "" + a);
                                this.getActionProcessor().processAction("AttachmentHandlerProcessor", "1", toUse, false, false);
                                String execLog = toUse.getProperty("returnexecutionlog", "");
                                if (execLog.length() > 0) {
                                    this.getMessageLog().append("\n").append(execLog).append("\n");
                                }
                                if (toUse.getProperty("handlerresult", "1").equalsIgnoreCase("2")) {
                                    throw new SapphireException("Child Handler (" + a + ") failed.");
                                }
                                this.logMessage("Child Handler (" + a + ") finished.");
                                continue;
                            }
                            finally {
                                this.logMessage("Attachment " + attachment.getSourceFilename() + " (" + a + ") handled.");
                            }
                        }
                        catch (Exception e) {
                            throw new SapphireException("Failed to process attachment " + attachment.getSourceFilename() + " (" + a + ").", e);
                        }
                    }
                }
                finally {
                    this.logMessage("Iterate Files Handler Finished.");
                }
            }
            throw new SapphireException("Failed to process multiple files as no handler provided.");
        }
    }
}

