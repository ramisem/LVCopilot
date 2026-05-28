/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.FileUtils
 */
package com.labvantage.sapphire.modules.sdms.handlers;

import com.labvantage.sapphire.util.file.DocumentFileParsingOptions;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.attachmenthandler.BaseAttachmentHandler;
import sapphire.xml.PropertyList;

public class PDFHandler
extends BaseAttachmentHandler {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void handleData(List<Attachment> attachments, PropertyList properties) throws SapphireException {
        block22: {
            if (attachments == null || attachments.size() == 0) {
                throw new SapphireException("No attachments provided.");
            }
            this.logMessage("PDFHandler handler started...");
            try {
                if (attachments.size() > 1) {
                    this.logMessage("More than one attachment/file provided. Only first one will be processed.");
                }
                DocumentFileParsingOptions options = new DocumentFileParsingOptions(properties);
                Path workingdir = FileManager.parseDocumentFile(attachments.get(0).getInputStream(), FileType.getFileType(attachments.get(0).getSourceFilename(), this.getConnectionId()), null, options, null, this.getConnectionId());
                if (workingdir != null) {
                    try {
                        Path p;
                        Stream<Path> stream;
                        if (options.getExtractTables()) {
                            try {
                                stream = Files.find(workingdir, 1, (path, basicFileAttributes) -> {
                                    File file = path.toFile();
                                    return !file.isDirectory() && file.getName().startsWith(options.getTableFilenamePrefix()) && file.getName().endsWith(".csv");
                                }, new FileVisitOption[0]);
                                stream.forEach(file -> {
                                    if (Files.exists(file, new LinkOption[0])) {
                                        this.addFile(file.toString(), file.toFile().getName(), options.getTableAttachmentClass());
                                        this.logMessage("Found and attached Table file.");
                                    }
                                });
                            }
                            catch (IOException e) {
                                this.logMessage("Failed to find images");
                            }
                        }
                        if (options.getExtractImages()) {
                            try {
                                stream = Files.find(workingdir, 1, (path, basicFileAttributes) -> {
                                    File file = path.toFile();
                                    return !file.isDirectory() && file.getName().startsWith(options.getImageFilenamePrefix());
                                }, new FileVisitOption[0]);
                                stream.forEach(file -> {
                                    if (Files.exists(file, new LinkOption[0])) {
                                        this.addFile(file.toString(), file.toFile().getName(), options.getImageAttachmentClass());
                                        this.logMessage("Found and attached image file.");
                                    }
                                });
                            }
                            catch (IOException e) {
                                this.logMessage("Failed to find images");
                            }
                        }
                        if (options.getExtractAsXML() && Files.exists(p = workingdir.resolve(options.getXmlFilenamePrefix() + ".xml"), new LinkOption[0])) {
                            this.addFile(p.toString(), p.toFile().getName(), options.getXmlAttachmentClass());
                            this.logMessage("Found and attached xml file.");
                        }
                        if (options.getGeneratePDFForPS() && Files.exists(p = workingdir.resolve(options.getPdfFilenamePrefix() + ".pdf"), new LinkOption[0])) {
                            this.addFile(p.toString(), p.toFile().getName(), options.getPdfAttachmentClass());
                            this.logMessage("Found and attached pdf file.");
                        }
                        if (options.getExtractText() && Files.exists(p = workingdir.resolve(options.getTextFilenamePrefix() + ".txt"), new LinkOption[0])) {
                            this.addFile(p.toString(), p.toFile().getName(), options.getTextAttachmentClass());
                            this.logMessage("Found and attached text file.");
                        }
                        this.logMessage("Files processed.");
                        break block22;
                    }
                    finally {
                        try {
                            FileUtils.deleteDirectory((File)workingdir.toFile());
                        }
                        catch (Exception e) {
                            this.logMessage("Failed to remove working directory");
                        }
                    }
                }
                throw new SapphireException("Failed to parse PDF");
            }
            finally {
                this.logMessage("PDFHandler handler finished.");
            }
        }
    }

    @Override
    public String getHelperURL() {
        return "rc?command=file&file=WEB-CORE/modules/sdms/handlers/pdfhandlerhelper.jsp";
    }
}

