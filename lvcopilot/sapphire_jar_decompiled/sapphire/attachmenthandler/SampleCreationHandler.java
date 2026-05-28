/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.IOUtils
 */
package sapphire.attachmenthandler;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.attachmenthandler.BaseAttachmentHandler;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;

public class SampleCreationHandler
extends BaseAttachmentHandler {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void handleData(List<Attachment> attachments, PropertyList properties) throws SapphireException {
        block27: {
            this.logDebug("Inside SampleCreationHandler");
            int copies = Integer.parseInt(properties.getProperty("copies", "-1"));
            String sampledesc = properties.getProperty("sampledesc", "");
            if (attachments == null || attachments.size() == 0) {
                throw new SapphireException("No attachments provided.");
            }
            this.logMessage("Sample creation handler started...");
            try {
                if (attachments.size() > 1) {
                    this.logMessage("More than one file/attachment provided. Only first one will be processed.");
                }
                String json = "";
                try {
                    Attachment attachment = attachments.get(0);
                    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                         InputStream is = attachment.getInputStream();){
                        IOUtils.copy((InputStream)is, (OutputStream)bos);
                    }
                    if (bos.size() > 0) {
                        json = bos.toString(StandardCharsets.UTF_8.name());
                    } else {
                        this.logMessage("No data in attachment.");
                    }
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to read file/attachment");
                }
                PropertyList propertyList = null;
                if (json.length() > 0) {
                    try {
                        JSONObject job = new JSONObject(json);
                        Iterator it = job.keys();
                        while (it.hasNext()) {
                            String k = (String)it.next();
                            Object val = job.get(k);
                            if (val instanceof String) continue;
                            job.put(k, val.toString());
                        }
                        propertyList = new PropertyList(job);
                        if (copies > -1) {
                            propertyList.setProperty("copies", "" + copies);
                        }
                        if (sampledesc.length() > 0) {
                            propertyList.setProperty("sampledesc", "" + sampledesc);
                        }
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to parse JSON.");
                    }
                }
                if (propertyList == null) break block27;
                this.logMessage("About to generate AddSDI");
                try {
                    ActionBlock actionBlock = new ActionBlock();
                    actionBlock.setAction("SampleCreationHandler", "AddSDI", "1", propertyList);
                    this.setActionBlock(actionBlock);
                    this.addLinkSDI("Sample", "[newkeyid1]", "", "");
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
                finally {
                    this.logMessage("AddSDI generated.");
                }
            }
            finally {
                this.logMessage("Sample creation handler finished.");
            }
        }
    }
}

