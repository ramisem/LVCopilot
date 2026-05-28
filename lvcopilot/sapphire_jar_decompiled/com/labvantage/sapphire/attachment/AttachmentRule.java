/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.attachment;

import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class AttachmentRule {
    private String attachmentClassRule = "";
    private FileType fileTypeRule = null;
    private FileTypeGroup fileTypeGroupRule = null;
    private String filenameRule = "";
    private String departmentIdRule = "";
    private long fileSizeRule = -1L;
    private boolean matchedClass = false;
    private boolean matchedFileType = false;
    private boolean matchedFilename = false;
    private boolean matchedFileSize = false;
    private boolean matchedDepartment = false;
    private String fileRepositoryId = "";
    private String fileRepositoryNodeId = "";
    private boolean encrypt = false;
    private boolean zip = false;
    private boolean contentEditable = true;

    public String getFileRepositoryId() {
        return this.fileRepositoryId;
    }

    public String getFileRepositoryNodeId() {
        return this.fileRepositoryNodeId;
    }

    public boolean isEncryptionRequired() {
        return this.encrypt;
    }

    public boolean isZippingRequired() {
        return this.zip;
    }

    public boolean isContentEditable() {
        return this.contentEditable;
    }

    public static AttachmentRule evaluateRule(String attachmentClass, PropertyListCollection rules, ConfigurationProcessor cp) {
        return AttachmentRule.evaluateRule(null, attachmentClass, rules, cp);
    }

    public static AttachmentRule evaluateRule(String filename, String attachmentClass, PropertyListCollection rules, ConfigurationProcessor cp) {
        return AttachmentRule.evaluateRule(filename, attachmentClass, -9999L, rules, cp);
    }

    public static AttachmentRule evaluateRule(String filename, String attachmentClass, long size, PropertyListCollection rules, ConfigurationProcessor cp) {
        return AttachmentRule.evaluateRule(filename, attachmentClass, size, null, rules, cp);
    }

    public static AttachmentRule evaluateRule(String filename, String attachmentClass, long size, String departmentId, PropertyListCollection rules, ConfigurationProcessor cp) {
        HashMap<Integer, AttachmentRule> matches = new HashMap<Integer, AttachmentRule>();
        Integer highestMatch = 0;
        if (rules != null && rules.size() > 0) {
            for (int i = 0; i < rules.size(); ++i) {
                Integer n;
                Integer n2;
                Integer match = 0;
                PropertyList rule = rules.getPropertyList(i);
                AttachmentRule attachmentRule = AttachmentRule.getRule(rule, cp);
                if (attachmentRule.attachmentClassRule.length() > 0) {
                    if (!attachmentRule.attachmentClassRule.equalsIgnoreCase(attachmentClass)) continue;
                    attachmentRule.matchedClass = true;
                    n2 = match;
                    n = match = Integer.valueOf(match + 1);
                }
                if (size != -9999L && attachmentRule.fileSizeRule > -1L) {
                    if (size < attachmentRule.fileSizeRule) continue;
                    attachmentRule.matchedFileSize = true;
                    n2 = match;
                    n = match = Integer.valueOf(match + 1);
                }
                if (departmentId != null) {
                    if (attachmentRule.departmentIdRule != null && attachmentRule.departmentIdRule.length() > 0) {
                        if (!departmentId.equalsIgnoreCase(attachmentRule.departmentIdRule)) continue;
                        attachmentRule.matchedDepartment = true;
                        n2 = match;
                        n = match = Integer.valueOf(match + 1);
                    }
                } else if (attachmentRule.departmentIdRule != null && attachmentRule.departmentIdRule.length() > 0) continue;
                if (filename != null) {
                    Integer n3;
                    Object object;
                    FileType fileType;
                    if (attachmentRule.fileTypeRule != null) {
                        if (!attachmentRule.fileTypeRule.getName().equals("UNKNOWN") && (fileType = FileType.getFileTypeByFileName(filename, cp)) != null && !fileType.getName().equals("UNKNOWN")) {
                            if (!fileType.getName().equals(attachmentRule.fileTypeRule.getName())) continue;
                            attachmentRule.matchedFileType = true;
                            n = match;
                            match = match + 1;
                            object = match;
                        }
                    } else if (attachmentRule.fileTypeGroupRule != null && attachmentRule.fileTypeGroupRule != FileTypeGroup.UNKNOWN && (fileType = FileType.getFileTypeByFileName(filename, cp)) != null && !fileType.getName().equals("UNKNOWN")) {
                        boolean found = false;
                        for (String fte : attachmentRule.fileTypeGroupRule.getFileTypeExtensions()) {
                            if (!fileType.getName().equals(FileType.getFileTypeByExtension(fte, cp).getName())) continue;
                            found = true;
                            break;
                        }
                        if (!found) continue;
                        attachmentRule.matchedFileType = true;
                        object = match;
                        n3 = match = Integer.valueOf(match + 1);
                    }
                    if (attachmentRule.filenameRule != null && attachmentRule.filenameRule.length() > 0 && filename.length() > 0) {
                        Path path;
                        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + attachmentRule.filenameRule);
                        if (!pathMatcher.matches(path = Paths.get(filename, new String[0]))) continue;
                        attachmentRule.matchedFilename = true;
                        object = match;
                        n3 = match = Integer.valueOf(match + 1);
                    }
                }
                if (matches.size() != 0 && matches.containsKey(match)) continue;
                if (highestMatch < match) {
                    highestMatch = match;
                }
                matches.put(match, attachmentRule);
            }
        }
        if (matches.size() > 0 && matches.containsKey(highestMatch)) {
            return (AttachmentRule)matches.get(highestMatch);
        }
        return null;
    }

    public static AttachmentRule getRule(PropertyList ruleProps, ConfigurationProcessor cp) {
        AttachmentRule attachmentRule = new AttachmentRule(){};
        attachmentRule.attachmentClassRule = ruleProps.getProperty("class");
        String ft = ruleProps.getProperty("filetype").toUpperCase();
        if (ft.length() > 0) {
            FileTypeGroup fileTypeGroup = null;
            try {
                fileTypeGroup = FileTypeGroup.valueOf(ft);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (fileTypeGroup == null) {
                FileType fileType = null;
                if (fileType == null) {
                    fileType = FileType.getFileTypeByExtension(ft, cp);
                }
                if (fileType.getName().equals("UNKNOWN")) {
                    fileType = FileType.getFileTypeByMime(ft, cp);
                }
                if (fileType.getName().equals("UNKNOWN")) {
                    attachmentRule.fileTypeRule = null;
                    attachmentRule.fileTypeGroupRule = null;
                } else {
                    attachmentRule.fileTypeRule = fileType;
                    attachmentRule.fileTypeGroupRule = FileTypeGroup.getFileTypeGroupByExtension(fileType.getExtension());
                }
            } else {
                attachmentRule.fileTypeGroupRule = fileTypeGroup;
            }
        } else {
            attachmentRule.fileTypeRule = null;
            attachmentRule.fileTypeGroupRule = null;
        }
        attachmentRule.departmentIdRule = ruleProps.getProperty("departmentid");
        attachmentRule.filenameRule = ruleProps.getProperty("filename");
        attachmentRule.fileSizeRule = 1000000L * Long.parseLong(ruleProps.getProperty("filesize", "-1"));
        attachmentRule.fileRepositoryId = ruleProps.getProperty("filerepositoryid", "");
        attachmentRule.fileRepositoryNodeId = ruleProps.getProperty("filerepositorynode", "");
        attachmentRule.zip = ruleProps.getProperty("zip", "N").equalsIgnoreCase("Y");
        attachmentRule.encrypt = ruleProps.getProperty("encrypt", "N").equalsIgnoreCase("Y");
        attachmentRule.contentEditable = ruleProps.getProperty("contenteditable", "Y").equalsIgnoreCase("Y");
        return attachmentRule;
    }
}

