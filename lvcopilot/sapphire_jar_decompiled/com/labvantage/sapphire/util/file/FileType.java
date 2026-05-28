/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.file;

import com.labvantage.sapphire.util.file.FileConstants;
import java.io.File;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FileType
implements FileConstants {
    private String name;
    private String extension;
    private String image;
    private String imageId;
    private NamedType type;
    private String mime;
    private PropertyListCollection magicbytes;
    private static final String UNKNOWN_EXTENSION = ".*";
    private static final String UNKNOWN_IMAGE = "WEB-CORE/utils/lookup/images/file.gif";
    private static final String UNKNOWN_IMAGEREF = "FlatBlackPageQuestion";
    private static final NamedType UNKNOWN_NAMEDTYPE = NamedType.UNKNOWN;
    private static final String UNKNOWN_MIMETYPE = "application/octet-stream";
    private static final PropertyListCollection UNKNOWN_MAGICBYTES = new PropertyListCollection();
    public static final String UNKNOWN_NAME = "UNKNOWN";
    private static FileType UNKNOWN_FILETYPE = new FileType("UNKNOWN", ".*", "WEB-CORE/utils/lookup/images/file.gif", "FlatBlackPageQuestion", UNKNOWN_NAMEDTYPE, "application/octet-stream", UNKNOWN_MAGICBYTES);

    public FileType(String name, String extension, String basicImage, String imageRefId, NamedType type, String mime, PropertyListCollection magicbytes) {
        this.name = name;
        this.extension = extension;
        this.image = basicImage;
        this.imageId = imageRefId;
        this.type = type;
        this.mime = mime;
        this.magicbytes = magicbytes;
    }

    public String getName() {
        return this.name;
    }

    public String getExtension() {
        return this.extension;
    }

    public NamedType getType() {
        return this.type;
    }

    public String getImage() {
        return this.image;
    }

    public String getMime() {
        return this.mime;
    }

    public String getImageRefId() {
        return this.imageId;
    }

    public PropertyListCollection getMagicBytes() {
        return this.magicbytes;
    }

    public static FileType getFileTypeByName(String name, File rakfile, String connectionid) {
        return FileType.getFileTypeByName(name, new ConfigurationProcessor(rakfile, connectionid));
    }

    public static FileType getFileTypeByName(String name, String connectionid) {
        return FileType.getFileTypeByName(name, new ConfigurationProcessor(connectionid));
    }

    public static FileType getFileTypeByName(String name, ConfigurationProcessor cp) {
        return FileType.findFileType(cp, "name", name);
    }

    public static FileType getFileTypeByFileName(String filename, File rakfile, String connectionid) {
        return FileType.getFileType(filename, rakfile, connectionid);
    }

    public static FileType getFileTypeByFileName(String filename, String connectionid) {
        return FileType.getFileType(filename, connectionid);
    }

    public static FileType getFileTypeByFileName(String filename, ConfigurationProcessor cp) {
        return FileType.getFileType(filename, cp);
    }

    public static FileType getFileType(String filename, File rakfile, String connectionid) {
        return FileType.getFileType(filename, new ConfigurationProcessor(rakfile, connectionid));
    }

    public static FileType getFileType(String filename, String connectionid) {
        return FileType.getFileType(filename, new ConfigurationProcessor(connectionid));
    }

    public static FileType getFileType(String filename, ConfigurationProcessor cp) {
        return FileType.findFileType(cp, "filename", filename);
    }

    public static FileType getFileTypeByExtension(String ext, File rakfile, String connectionid) {
        return FileType.getFileTypeByExtension(ext, new ConfigurationProcessor(rakfile, connectionid));
    }

    public static FileType getFileTypeByExtension(String ext, String connectionid) {
        return FileType.getFileTypeByExtension(ext, new ConfigurationProcessor(connectionid));
    }

    public static FileType getFileTypeByExtension(String ext, ConfigurationProcessor cp) {
        return FileType.findFileType(cp, "extension", ext);
    }

    public static FileType getFileTypeByMime(String mime, File rakfile, String connectionid) {
        return FileType.getFileTypeByMime(mime, new ConfigurationProcessor(rakfile, connectionid));
    }

    public static FileType getFileTypeByMime(String mime, String connectionid) {
        return FileType.getFileTypeByMime(mime, new ConfigurationProcessor(connectionid));
    }

    public static FileType getFileTypeByMime(String mime, ConfigurationProcessor cp) {
        return FileType.findFileType(cp, "mime", mime);
    }

    public static NamedType getNamedTypeByName(String name) {
        NamedType out = NamedType.UNKNOWN;
        for (NamedType nt : NamedType.values()) {
            if (!name.equalsIgnoreCase(nt.getName())) continue;
            out = nt;
            break;
        }
        return out;
    }

    private static FileType findFileType(ConfigurationProcessor cp, String fileterName, String value) {
        FileType matchingFileType = null;
        FileType unknownFileType = null;
        try {
            PropertyList fileLocationPolicy = cp.getPolicy("FileLocationPolicy", "Sapphire Custom");
            PropertyListCollection fileTypeCollection = fileLocationPolicy.getCollectionNotNull("knownfiletypes");
            for (int i = 0; i < fileTypeCollection.size(); ++i) {
                PropertyList fileTypeProps = fileTypeCollection.getPropertyList(i);
                boolean found = false;
                if ("name".equalsIgnoreCase(fileterName) && value.equalsIgnoreCase(fileTypeProps.getProperty("id", ""))) {
                    found = true;
                } else if ("filename".equalsIgnoreCase(fileterName)) {
                    String ext = fileTypeProps.getProperty("extension", "");
                    if (value.regionMatches(true, value.length() - ext.length(), ext, 0, ext.length())) {
                        found = true;
                    }
                } else if ("mime".equalsIgnoreCase(fileterName) && value.equalsIgnoreCase(fileTypeProps.getProperty("mimetype", ""))) {
                    found = true;
                } else if ("extension".equalsIgnoreCase(fileterName) && value.equalsIgnoreCase(fileTypeProps.getProperty("extension", ""))) {
                    found = true;
                }
                String name = fileTypeProps.getProperty("id", "");
                if (!found && !name.equalsIgnoreCase(UNKNOWN_NAME)) continue;
                String extension = fileTypeProps.getProperty("extension", "");
                String basicImage = fileTypeProps.getProperty("icon", "");
                String imagerefid = fileTypeProps.getProperty("imagerefid", "");
                String type = fileTypeProps.getProperty("type", "");
                String mimetype = fileTypeProps.getProperty("mimetype", "");
                PropertyListCollection magicbytes = fileTypeProps.getCollection("magicbytes");
                FileType ft = new FileType(name, extension, basicImage, imagerefid, FileType.getNamedTypeByName(type), mimetype, magicbytes);
                if (found) {
                    matchingFileType = ft;
                    break;
                }
                unknownFileType = ft;
            }
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        if (matchingFileType == null && unknownFileType != null) {
            matchingFileType = unknownFileType;
        } else if (matchingFileType == null && unknownFileType == null) {
            matchingFileType = UNKNOWN_FILETYPE;
        }
        return matchingFileType;
    }

    public String toString() {
        return this.getName().toUpperCase();
    }

    public static enum NamedType {
        CONFIG("Config File"),
        CHEMICAL("Chemical File"),
        TEXT("Text File"),
        LOG("Log File"),
        RICHTEXT("Rich Text File"),
        DATABASE("Database File"),
        WORD("Word Document"),
        EXECUTABLE("Executable File"),
        IMAGE("Image File"),
        MEDIA("Media File"),
        ADOBE("PDF File"),
        POWERPOINT("PowerPoint File"),
        SYSTEM("System File"),
        EXCEL("Excel File"),
        WEB("Web File"),
        ZIP("Zip File"),
        PRINT("Print File"),
        UNKNOWN("Unknown File");

        private String name;

        private NamedType(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}

