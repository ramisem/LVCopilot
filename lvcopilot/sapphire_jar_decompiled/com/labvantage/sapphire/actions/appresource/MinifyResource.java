/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.javascript.jscomp.CompilationLevel
 *  com.google.javascript.jscomp.Compiler
 *  com.google.javascript.jscomp.CompilerOptions
 *  com.google.javascript.jscomp.SourceFile
 *  org.apache.commons.io.IOUtils
 */
package com.labvantage.sapphire.actions.appresource;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.labvantage.sapphire.servlet.command.ResourceRequest;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;

public class MinifyResource
extends BaseAction {
    public static final String ID = "MinifyResource";
    public static final String VERSIONID = "1";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_MESSAGE = "message";
    private static final String MESSAGE_ERROR = "Minification failed!";
    private static final String HTML_LINE_BREAK = "<br/>";
    private static final String COLUMN_ATTACHMENTNUM = "attachmentnum";
    private static final String COLUMN_ATTACHMENTCLASS = "attachmentclass";
    private static final String COLUMN_CONTENTTYPE = "contenttype";
    public static final String ERROR_ID = "Resource minification";
    private static final SourceFile extern = SourceFile.fromCode((String)"externs.js", (String)"");
    StringBuilder message = new StringBuilder();

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String keyId1 = properties.getProperty(PROPERTY_KEYID1);
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_AppResource");
        sdiRequest.setKeyid1List(keyId1);
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("attachment");
        SDIData data = sdiProcessor.getSDIData(sdiRequest);
        DataSet primary = data.getDataset("primary");
        DataSet attachments = data.getDataset("attachment");
        ResourceRequest.ContentType contentType = ResourceRequest.ContentType.fromString(primary.getString(0, COLUMN_CONTENTTYPE));
        AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.getConnectionId());
        boolean attachmentFound = false;
        for (int i = 0; i < attachments.getRowCount(); ++i) {
            Attachment attachment;
            int attachmentnum = attachments.getInt(i, COLUMN_ATTACHMENTNUM);
            String attachmentClass = attachments.getString(i, COLUMN_ATTACHMENTCLASS);
            if (attachmentClass.equals("AppResource")) {
                attachment = Attachment.getAttachment("LV_AppResource", keyId1, null, null, attachmentnum);
                try {
                    this.generateMinifiedAttachment(attachmentProcessor, attachment, contentType);
                    attachmentFound = true;
                    continue;
                }
                catch (IOException e) {
                    throw new SapphireException(MESSAGE_ERROR, e);
                }
            }
            if (!attachmentClass.equals("AppResourceMinify")) continue;
            attachment = Attachment.getAttachment("LV_AppResource", keyId1, null, null, attachmentnum);
            attachmentProcessor.deleteSDIAttachment(attachment);
        }
        if (!attachmentFound) {
            this.message.append("No attachment found!");
        }
        properties.setProperty(PROPERTY_MESSAGE, this.message.toString());
    }

    void generateMinifiedAttachment(AttachmentProcessor attachmentProcessor, Attachment attachment, ResourceRequest.ContentType contentType) throws IOException, SapphireException {
        String data = (attachment = attachmentProcessor.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.DISABLED)).getClob();
        if (data == null || data.isEmpty()) {
            InputStream input = attachment.getInputStream();
            data = IOUtils.toString((InputStream)input, (Charset)StandardCharsets.UTF_8);
        }
        if (data == null || data.isEmpty()) {
            this.setError(ERROR_ID, "INFORMATION", MESSAGE_ERROR);
            return;
        }
        try (StringReader stringReader = new StringReader(data);
             StringWriter stringWriter = new StringWriter();){
            byte[] minified;
            if (contentType == ResourceRequest.ContentType.JAVASCRIPT) {
                this.compressJavaScript(stringReader, stringWriter);
                minified = stringWriter.toString().getBytes(StandardCharsets.UTF_8);
            } else {
                this.compressCSS(stringReader, stringWriter);
                minified = stringWriter.toString().getBytes(StandardCharsets.UTF_8);
            }
            ByteArrayInputStream is = new ByteArrayInputStream(minified);
            Attachment attachmentMin = Attachment.getAttachment("LV_AppResource", attachment.getKeyId1(), null, null);
            attachmentMin.setAttachmentClass("AppResourceMinify");
            attachmentMin.setDescription(attachment.getDescription());
            attachmentMin.setInputStream(is);
            attachmentMin.setSourceFilename(attachment.getSourceFilename());
            attachmentMin.setAttachmentType(Attachment.AttachmentType.FILE);
            attachmentProcessor.addSDIAttachment(attachmentMin, false, false, null);
            if (attachmentProcessor.hasErrors()) {
                throw new SapphireException(attachmentProcessor.getLastError());
            }
        }
        catch (IOException t) {
            this.message.append(HTML_LINE_BREAK).append(MESSAGE_ERROR);
        }
    }

    void compressJavaScript(StringReader in, StringWriter out) throws IOException {
        String code = MinifyResource.convertReaderToString(in);
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        SourceFile source = SourceFile.fromCode((String)"input.js", (String)code);
        compiler.compile(extern, source, options);
        out.append(compiler.toSource());
    }

    void compressCSS(StringReader in, StringWriter out) throws IOException {
    }

    private static String convertReaderToString(Reader reader) throws IOException {
        String line;
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }
}

