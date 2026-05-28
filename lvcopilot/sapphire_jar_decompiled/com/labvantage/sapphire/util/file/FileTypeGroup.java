/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.file;

import com.labvantage.sapphire.util.file.FileConstants;
import com.labvantage.sapphire.util.file.FileType;
import java.util.ArrayList;
import java.util.Arrays;
import sapphire.accessor.ConfigurationProcessor;

public enum FileTypeGroup implements FileConstants
{
    CHEMICAL("Chemical File", new String[]{".mol", ".rxn", ".sdf", ".cml"}, "FlatBlackBeakerFlask", true, true),
    IMAGE("Image File", new String[]{".gif", ".jpg", ".jpeg", ".png", ".tif", ".tiff", ".wmf", ".emf"}, "FlatBlackPageImage", true, true),
    PPT("PowerPoint File", new String[]{".ppt", ".pptx"}, "FlatBlackPagePowerpoint", true, true),
    EXCEL("Excel File", new String[]{".xls", ".xlsx"}, "FlatBlackPageExcel", true, true),
    WORD("Word Document", new String[]{".doc", ".docx"}, "FlatBlackPageWord", true, true),
    TXT("Text File", new String[]{".txt", ".xml", ".xml2", ".csv"}, "FlatBlackText1", true, true),
    PDF("PDF File", new String[]{".pdf"}, "FlatBlackPageFilePdf", true, true),
    HTML("Web File", new String[]{".html", ".htm"}, "FlatBlackBrowserWire", true, true),
    ZIP("Zip File", new String[]{".zip", ".jar", ".gz", ".ear", ".war"}, "FlatBlackCabinet", false, false),
    UNKNOWN("Unknown File", new String[0], "FlatBlackPageQuestion", false, false);

    private ArrayList<String> fileTypeExtensions;
    private String name;
    private String image;
    private boolean supported = false;
    private boolean previewable = false;

    private FileTypeGroup(String name, String[] fileTypeExtensions, String image, boolean previewable, boolean supported) {
        this.name = name;
        this.image = image;
        this.previewable = previewable;
        this.supported = supported;
        this.fileTypeExtensions = new ArrayList();
        this.fileTypeExtensions.addAll(Arrays.asList(fileTypeExtensions));
    }

    public String getName() {
        return this.name;
    }

    public String getImageRefId() {
        return this.image;
    }

    public ArrayList<String> getFileTypeExtensions() {
        return this.fileTypeExtensions;
    }

    public static String getPreviewTypes() {
        return FileTypeGroup.getPreviewTypes(null);
    }

    public static String getPreviewTypes(ArrayList<FileTypeGroup> excludes) {
        StringBuffer out = new StringBuffer();
        for (FileTypeGroup fileType : FileTypeGroup.values()) {
            if (!fileType.previewable || excludes != null && excludes.size() != 0 && excludes.contains(fileType)) continue;
            for (int i = 0; i < fileType.fileTypeExtensions.size(); ++i) {
                if (out.length() > 0) {
                    out.append(",");
                }
                String ext = fileType.fileTypeExtensions.get(i);
                out.append(ext);
            }
        }
        return out.toString();
    }

    public static String getSupportedTypes() {
        StringBuffer out = new StringBuffer();
        for (FileTypeGroup fileType : FileTypeGroup.values()) {
            if (!fileType.supported) continue;
            for (int i = 0; i < fileType.fileTypeExtensions.size(); ++i) {
                if (out.length() > 0) {
                    out.append(",");
                }
                String ext = fileType.fileTypeExtensions.get(i);
                out.append(ext);
            }
        }
        return out.toString();
    }

    public boolean isSupported() {
        return this.supported;
    }

    public boolean isPreviewable() {
        return this.previewable;
    }

    public static FileTypeGroup getFileTypeGroupByType(String type, String connectionId) {
        return FileTypeGroup.getFileTypeGroupByType(type, new ConfigurationProcessor(connectionId));
    }

    public static FileTypeGroup getFileTypeGroupByType(String type, ConfigurationProcessor cp) {
        for (FileTypeGroup fileTypeGroup : FileTypeGroup.values()) {
            for (int i = 0; i < fileTypeGroup.fileTypeExtensions.size(); ++i) {
                String filetypeExtension = fileTypeGroup.fileTypeExtensions.get(i);
                if (!FileType.getFileTypeByExtension(filetypeExtension, cp).getMime().equalsIgnoreCase(type)) continue;
                return fileTypeGroup;
            }
        }
        return UNKNOWN;
    }

    public static FileTypeGroup getFileTypeGroupByFileType(FileType type, String connectionId) {
        return FileTypeGroup.getFileTypeGroupByFileType(type, new ConfigurationProcessor(connectionId));
    }

    public static FileTypeGroup getFileTypeGroupByFileType(FileType type, ConfigurationProcessor cp) {
        for (FileTypeGroup fileTypeGroup : FileTypeGroup.values()) {
            for (int i = 0; i < fileTypeGroup.fileTypeExtensions.size(); ++i) {
                String filetypeExtension = fileTypeGroup.fileTypeExtensions.get(i);
                if (!FileType.getFileTypeByExtension(filetypeExtension, cp).getName().equalsIgnoreCase(type.getName())) continue;
                return fileTypeGroup;
            }
        }
        return UNKNOWN;
    }

    public static boolean isValidTypeGroup(String type, String connectionId) {
        return FileTypeGroup.isValidTypeGroup(type, new ConfigurationProcessor(connectionId));
    }

    public static boolean isValidTypeGroup(String type, ConfigurationProcessor cp) {
        return FileTypeGroup.getFileTypeGroupByType((String)type, (ConfigurationProcessor)cp).supported;
    }

    public static boolean isValidPreviewTypeGroup(String type, String connectionId) {
        return FileTypeGroup.isValidPreviewTypeGroup(type, new ConfigurationProcessor(connectionId));
    }

    public static boolean isValidPreviewTypeGroup(String type, ConfigurationProcessor cp) {
        FileTypeGroup filetype = FileTypeGroup.getFileTypeGroupByType(type, cp);
        return filetype.previewable;
    }

    public static FileTypeGroup getFileTypeGroupByFileName(String fileName) {
        int indexOfDot = fileName.lastIndexOf(46);
        if (indexOfDot > -1) {
            String ext = fileName.substring(indexOfDot + 1);
            return FileTypeGroup.getFileTypeGroupByExtension(ext);
        }
        return UNKNOWN;
    }

    public static FileTypeGroup getFileTypeGroupByExtension(String extension) {
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        for (FileTypeGroup fileTypeGroup : FileTypeGroup.values()) {
            for (int i = 0; i < fileTypeGroup.fileTypeExtensions.size(); ++i) {
                String filetypeExtension = fileTypeGroup.fileTypeExtensions.get(i);
                if (!filetypeExtension.equalsIgnoreCase(extension)) continue;
                return fileTypeGroup;
            }
        }
        return UNKNOWN;
    }

    public static boolean isValidExtension(String extension) {
        return FileTypeGroup.getFileTypeGroupByExtension((String)extension).supported;
    }

    public static boolean isValidFilename(String filename) {
        int i = filename.length() > 0 ? filename.lastIndexOf(".") : -1;
        String ext = i > -1 ? filename.substring(i + 1) : "";
        return ext.length() > 0 ? FileTypeGroup.getFileTypeGroupByExtension((String)ext).supported : false;
    }

    public static boolean isValidPreviewExtension(String extension) {
        FileTypeGroup filetype = FileTypeGroup.getFileTypeGroupByExtension(extension);
        return filetype.previewable;
    }
}

