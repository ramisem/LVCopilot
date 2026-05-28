/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire.webservices.transport;

import com.labvantage.sapphire.services.Attachment;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.codec.binary.Base64;
import sapphire.util.Logger;

public class AttachmentTransportBean
implements Serializable {
    static final String LABVANTAGE_CVS_ID = "$Revision: 64472 $";
    private String base64data = "";
    private boolean zipped = false;
    private String filename = "";
    private String description = "";
    private String oleclass = "";
    private String type = "";
    private String sourcefilename = "";
    private String attachmentPolicyNode = "";

    public AttachmentTransportBean() {
    }

    public AttachmentTransportBean(Attachment at, boolean zip) {
        this.setAttachment(at, zip);
    }

    public Attachment toAttachment() {
        return this.getAttachment();
    }

    public String getData() {
        return this.base64data;
    }

    public void setData(String value) {
        this.base64data = value;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public String getSourceFilename() {
        return this.sourcefilename;
    }

    public void setSourceFilename(String value) {
        this.sourcefilename = value;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String value) {
        this.filename = value;
    }

    public String getOLEClass() {
        return this.oleclass;
    }

    public void setOLEClass(String value) {
        this.oleclass = value;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public boolean getZipped() {
        return this.zipped;
    }

    public void setZipped(boolean value) {
        this.zipped = value;
    }

    public String getAttachmentPolicyNode() {
        return this.attachmentPolicyNode;
    }

    public void setAttachmentPolicyNode(String attachmentPolicyNode) {
        this.attachmentPolicyNode = attachmentPolicyNode;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void setAttachment(Attachment attachment, boolean zip) {
        if (attachment != null) {
            this.zipped = zip;
            if (!zip) {
                byte[] data = attachment.getData();
                this.base64data = Base64.encodeBase64String((byte[])data);
                Logger.logDebug("Attachment not zipped...length " + data.length);
            } else {
                byte[] data = attachment.getData();
                Logger.logDebug("Attachment zipped...length " + data.length);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);){
                        gzipOutputStream.write(data);
                    }
                    Logger.logDebug("Compression ratio ", Float.valueOf(1.0f * (float)data.length / (float)byteArrayOutputStream.size()));
                    byte[] fin = byteArrayOutputStream.toByteArray();
                    Logger.logDebug("Attachment unzipped...length " + fin.length);
                    this.base64data = Base64.encodeBase64String((byte[])fin);
                }
                catch (IOException e) {
                    Logger.logError("Could not zip attachment.");
                    this.base64data = "";
                }
            }
            this.filename = attachment.getFilename();
            this.oleclass = attachment.getOleClass();
            this.type = attachment.getType();
            this.description = attachment.getDescription();
            this.sourcefilename = attachment.getSourceFilename();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Attachment getAttachment() {
        Attachment att = new Attachment();
        att.setFilename(this.filename);
        att.setOleClass(this.oleclass);
        att.setType(this.type);
        att.setDescription(this.description);
        att.setSourceFilename(this.sourcefilename);
        if (this.zipped) {
            byte[] data = null;
            try {
                data = Base64.decodeBase64((String)this.base64data);
            }
            catch (Exception e) {
                Logger.logError("Could not convert data from base64. Error = " + e.getMessage());
            }
            if (data != null) {
                Logger.logDebug("Attachment zipped...length " + data.length);
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(data);
                    try (GZIPInputStream gzipInputStream = new GZIPInputStream(bis);
                         ByteArrayOutputStream os = new ByteArrayOutputStream();){
                        int len;
                        byte[] buf = new byte[1024];
                        while ((len = gzipInputStream.read(buf)) > 0) {
                            os.write(buf, 0, len);
                        }
                        att.setData(os.toByteArray());
                        Logger.logDebug("Attachment unzipped...length " + att.getData().length);
                    }
                }
                catch (Exception e) {
                    Logger.logError("Failed to uncompress. Error = " + e.getMessage());
                }
            }
        } else {
            try {
                att.setData(Base64.decodeBase64((String)this.base64data));
                Logger.logDebug("Attachment not zipped...length " + att.getData().length);
            }
            catch (Exception e) {
                Logger.logError("Could not convert data from base64.");
            }
        }
        return att;
    }
}

