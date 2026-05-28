/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.labvantage.sapphire.pageelements.lookup;

import com.labvantage.sapphire.pageelements.lookup.FileSystem;
import com.labvantage.sapphire.util.file.FileConstants;
import com.labvantage.sapphire.util.file.FileType;
import java.io.File;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.servlet.http.HttpServletRequest;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.ConnectionInfo;
import sapphire.util.HttpUtil;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;

public class FileView
extends BaseElement
implements FileConstants {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77785 $";
    public static final String FOLDERSPROP = "folders";
    public static final String ACTIVEEXTENSIONPROP = "activeextension";
    public static final String PATHPROP = "path";
    public static final String LOCKDOWNPROP = "lockdown";
    public static final String USEZIPPROP = "usezip";
    public static final String SORTCOLUMNPROP = "sortcolumn";
    public static final String SORTDIRECTIONPROP = "sortdirection";
    public static final int DEFAULTSORTCOLUMN = 1;
    public static final String SORTASCIMAGE = "WEB-CORE/utils/lookup/images/sorta.gif";
    public static final String SORTDESCIMAGE = "WEB-CORE/utils/lookup/images/sortd.gif";
    public static final String SPLITTERIMAGE = "WEB-CORE/utils/lookup/images/splitter.gif";
    public static final String NORMBACKGROUNDIMAGE = "WEB-CORE/utils/lookup/images/td-back.gif";
    public static final String OVERBACKGROUNDIMAGE = "WEB-CORE/utils/lookup/images/td-backover.gif";
    public static final String SAPPHIREDOCIMAGE = "WEB-CORE/utils/lookup/images/sapphiredoc.gif";
    public static final String DEFAULTEXTENSION = "*.*";
    private String path;
    private boolean foldersOnly;
    private boolean lockDown;
    private boolean useZip;
    private String extension;
    private int sortCol;
    private boolean sortDescending;
    private String zipFile;
    private String zipPath;
    private StringBuffer theHTMLBuffer = new StringBuffer();

    public FileView() {
        this.logger.debug("Class created...");
    }

    private void sortFileArray(File[] theFiles, int theSortCol, final boolean isSortDesc) {
        this.logger.debug("sortFileArray called...");
        switch (theSortCol) {
            case 1: {
                Arrays.sort(theFiles, new Comparator(){
                    private Collator collator = Collator.getInstance();

                    public int compare(Object object1, Object object2) {
                        if (!isSortDesc) {
                            return this.collator.compare(((File)object1).getName(), ((File)object2).getName());
                        }
                        return this.collator.compare(((File)object2).getName(), ((File)object1).getName());
                    }
                });
                break;
            }
            case 2: {
                Arrays.sort(theFiles, new Comparator(){

                    public int compare(Object object1, Object object2) {
                        if (((File)object1).length() > ((File)object2).length()) {
                            if (isSortDesc) {
                                return -1;
                            }
                            return 1;
                        }
                        if (((File)object1).length() < ((File)object2).length()) {
                            if (isSortDesc) {
                                return 1;
                            }
                            return -1;
                        }
                        return 0;
                    }
                });
                break;
            }
            case 3: {
                Arrays.sort(theFiles, new Comparator(){
                    private Collator collator = Collator.getInstance();

                    public int compare(Object object1, Object object2) {
                        String ext2;
                        String filename1 = ((File)object1).getName();
                        String filename2 = ((File)object2).getName();
                        String ext1 = filename1.lastIndexOf(".") == -1 ? "" : filename1.substring(filename1.lastIndexOf(".") + 1, filename1.length());
                        String string = ext2 = filename2.lastIndexOf(".") == -1 ? "" : filename2.substring(filename2.lastIndexOf(".") + 1, filename2.length());
                        if (!isSortDesc) {
                            return this.collator.compare(ext1, ext2);
                        }
                        return this.collator.compare(ext2, ext1);
                    }
                });
                break;
            }
            case 4: {
                Arrays.sort(theFiles, new Comparator(){

                    public int compare(Object object1, Object object2) {
                        if (((File)object1).lastModified() > ((File)object2).lastModified()) {
                            if (isSortDesc) {
                                return -1;
                            }
                            return 1;
                        }
                        if (((File)object1).lastModified() < ((File)object2).lastModified()) {
                            if (isSortDesc) {
                                return 1;
                            }
                            return -1;
                        }
                        return 0;
                    }
                });
                break;
            }
            default: {
                this.logger.warn("Invalid sort column thus do not sort.");
            }
        }
    }

    private void renderFolderContents(StringBuffer theBuffer, StringBuffer imageBuffer, File theFileObject, String theExtension, boolean theFoldersOnly, boolean theLockDown, boolean theUseZip, int theSortCol, boolean isSortDesc) {
        this.logger.debug("renderFolderContents called...");
        File[] fileObjectArray = theFileObject.listFiles();
        this.sortFileArray(fileObjectArray, theSortCol, isSortDesc);
        if (fileObjectArray != null) {
            this.logger.debug("Files found = " + fileObjectArray.length);
            StringBuffer folderBuffer = new StringBuffer();
            StringBuffer fileBuffer = new StringBuffer();
            for (int index = 0; index < fileObjectArray.length; ++index) {
                if (fileObjectArray[index].isDirectory()) {
                    this.renderFolder(folderBuffer, fileObjectArray[index].getName(), FileView.getDate(fileObjectArray[index], this.connectionInfo), index, theLockDown, false, theSortCol);
                    continue;
                }
                if (theFoldersOnly) continue;
                this.renderFile(fileBuffer, imageBuffer, theFileObject.getPath(), fileObjectArray[index].getName(), FileView.getDate(fileObjectArray[index], this.connectionInfo), FileView.getSize(fileObjectArray[index].length()), index, theExtension, theLockDown, theUseZip, theSortCol);
            }
            theBuffer.append(folderBuffer);
            theBuffer.append(fileBuffer);
        } else {
            this.logger.error("No files returned.");
        }
    }

    private void sortZipFileArray(ZipEntry[] theZipEntries, int theSortCol, final boolean isSortDesc) {
        this.logger.debug("sortZipFileArray called...");
        switch (theSortCol) {
            case 1: {
                Arrays.sort(theZipEntries, new Comparator(){
                    private Collator collator = Collator.getInstance();

                    public int compare(Object object1, Object object2) {
                        if (!isSortDesc) {
                            return this.collator.compare(((ZipEntry)object1).getName(), ((ZipEntry)object2).getName());
                        }
                        return this.collator.compare(((ZipEntry)object2).getName(), ((ZipEntry)object1).getName());
                    }
                });
                break;
            }
            case 2: {
                Arrays.sort(theZipEntries, new Comparator(){

                    public int compare(Object object1, Object object2) {
                        if (((ZipEntry)object1).getSize() > ((ZipEntry)object2).getSize()) {
                            if (isSortDesc) {
                                return -1;
                            }
                            return 1;
                        }
                        if (((ZipEntry)object1).getSize() < ((ZipEntry)object2).getSize()) {
                            if (isSortDesc) {
                                return 1;
                            }
                            return -1;
                        }
                        return 0;
                    }
                });
                break;
            }
            case 3: {
                Arrays.sort(theZipEntries, new Comparator(){
                    private Collator collator = Collator.getInstance();

                    public int compare(Object object1, Object object2) {
                        String ext2;
                        String filename1 = ((ZipEntry)object1).getName();
                        String filename2 = ((ZipEntry)object2).getName();
                        String ext1 = filename1.lastIndexOf(".") == -1 ? "" : filename1.substring(filename1.lastIndexOf(".") + 1, filename1.length());
                        String string = ext2 = filename2.lastIndexOf(".") == -1 ? "" : filename2.substring(filename2.lastIndexOf(".") + 1, filename2.length());
                        if (!isSortDesc) {
                            return this.collator.compare(ext1, ext2);
                        }
                        return this.collator.compare(ext2, ext1);
                    }
                });
                break;
            }
            case 4: {
                Arrays.sort(theZipEntries, new Comparator(){

                    public int compare(Object object1, Object object2) {
                        if (((ZipEntry)object1).getTime() > ((ZipEntry)object2).getTime()) {
                            if (isSortDesc) {
                                return -1;
                            }
                            return 1;
                        }
                        if (((ZipEntry)object1).getTime() < ((ZipEntry)object2).getTime()) {
                            if (isSortDesc) {
                                return 1;
                            }
                            return -1;
                        }
                        return 0;
                    }
                });
                break;
            }
            default: {
                this.logger.warn("Invalid sort column thus do not sort.");
            }
        }
    }

    private ZipEntry[] createZipEntryArray(Enumeration zipEnum) {
        this.logger.debug("createZipFileArray called...");
        Vector<ZipEntry> buffer = new Vector<ZipEntry>();
        while (zipEnum.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry)zipEnum.nextElement();
            buffer.add(zipEntry);
        }
        this.logger.debug("buffer.size = " + buffer.size());
        ZipEntry[] zipEntryArray = new ZipEntry[buffer.size()];
        for (int index = 0; index < buffer.size(); ++index) {
            zipEntryArray[index] = (ZipEntry)buffer.get(index);
        }
        this.logger.debug("zipEntryArray.length = " + zipEntryArray.length);
        return zipEntryArray;
    }

    private void renderZipEntry(ZipEntry zipEntry, StringBuffer fileBuffer, StringBuffer folderBuffer, String theZipPath, String theExtension, boolean theFoldersOnly, boolean theLockDown, int index, int theSortCol, ArrayList usedFoldersList) {
        this.logger.debug("renderZipEntry called...");
        String name = zipEntry.getName();
        this.logger.debug("name (from zip) = " + name);
        if (!name.contains("/") && !name.contains("\\")) {
            this.logger.debug("Zip entry contains no path info...");
            if (theZipPath == null || theZipPath.length() == 0) {
                this.logger.debug("Zip path is empty and entry has no path thus render as file...");
                if (name.length() > 0 && !theFoldersOnly) {
                    this.renderFile(fileBuffer, null, theZipPath, name, "-", FileView.getSize(zipEntry.getSize()), index, theExtension, theLockDown, true, theSortCol);
                } else {
                    this.logger.debug("File name empty or folders only thus skip...");
                }
            } else {
                this.logger.debug("Zip path is not empty and entry has no path thus skip...");
            }
        } else {
            this.logger.debug("Zip entry contains path info...");
            String aFile = name.substring(name.lastIndexOf("/") + 1);
            String aPath = name.substring(0, name.lastIndexOf("/") + 1);
            this.logger.debug("aFile (filename of file in zip) = " + aFile);
            this.logger.debug("aPath (path of file in zip) = " + aPath);
            if (theZipPath != null && theZipPath.length() > 0) {
                String temp = FileSystem.getEscapedPath(theZipPath);
                if (!temp.endsWith("/")) {
                    temp = temp + "/";
                }
                if (temp.startsWith("/")) {
                    temp = temp.substring(1);
                }
                this.logger.debug("temp (zip path eascaped) = " + temp);
                if (temp.equalsIgnoreCase(aPath)) {
                    this.logger.debug("The zip entry path is equal to the provided zip path therefore render file...");
                    if (aFile != null && aFile.length() > 0 && !theFoldersOnly) {
                        this.renderFile(fileBuffer, null, theZipPath, aFile, "-", FileView.getSize(zipEntry.getSize()), index, theExtension, theLockDown, true, theSortCol);
                    } else {
                        this.logger.debug("File name empty or folders only thus skip...");
                    }
                } else {
                    this.logger.debug("The zip entry path is not equal to provided zip path therefore render as folder...");
                    if (aPath.startsWith(temp)) {
                        this.logger.debug("The zip entry is under the provided zip path so render...");
                        this.logger.debug("aPath = " + aPath);
                        this.logger.debug("temp = " + temp);
                        String aFolder = aPath.substring(temp.length());
                        this.logger.debug("aFolder (1) = " + aFolder);
                        aFolder = aFolder.substring(0, aFolder.indexOf("/"));
                        this.logger.debug("aFolder (2) = " + aFolder);
                        if (usedFoldersList.size() == 0 || !this.isFolderUsed(usedFoldersList, aFolder)) {
                            this.logger.debug(name + " rendered as folder.");
                            usedFoldersList.add(aFolder);
                            this.renderFolder(folderBuffer, aFolder, "-", index, theLockDown, false, theSortCol);
                        } else {
                            this.logger.debug("Folder " + aFolder + " already used...");
                        }
                    } else {
                        this.logger.debug("The zip entry is not under provided zip path so skip...");
                    }
                }
            } else {
                this.logger.debug("Zip entry contains path but no path provided thus render as dir...");
                this.logger.debug("aPath = " + aPath);
                String aFolder = aPath.substring(0, aPath.indexOf("/"));
                this.logger.debug("aFolder = " + aFolder);
                if (usedFoldersList.size() == 0 || !this.isFolderUsed(usedFoldersList, aFolder)) {
                    this.logger.debug(name + " rendered as folder.");
                    usedFoldersList.add(aFolder);
                    this.renderFolder(folderBuffer, aFolder, "-", index, theLockDown, false, theSortCol);
                } else {
                    this.logger.debug("Folder " + aFolder + " already used...");
                }
            }
        }
    }

    private void renderZipFileContents(StringBuffer theBuffer, File theFile, String theExtension, boolean theFoldersOnly, boolean theLockDown, String theZipPath, int theSortCol, boolean isSortDesc) {
        this.logger.debug("renderZipFileContents called...");
        try {
            ZipFile zipFileObject = new ZipFile(theFile);
            this.logger.debug("Zip file obtained.");
            this.logger.debug("Zip size = " + zipFileObject.size());
            StringBuffer folderBuffer = new StringBuffer();
            StringBuffer fileBuffer = new StringBuffer();
            ArrayList usedFoldersList = new ArrayList();
            ZipEntry[] zipEntriesArray = this.createZipEntryArray(zipFileObject.entries());
            this.logger.debug("zipEntriesArray.length = " + zipEntriesArray.length);
            this.sortZipFileArray(zipEntriesArray, theSortCol, isSortDesc);
            for (int arrayIndex = 0; arrayIndex < zipEntriesArray.length; ++arrayIndex) {
                this.renderZipEntry(zipEntriesArray[arrayIndex], fileBuffer, folderBuffer, theZipPath, theExtension, theFoldersOnly, theLockDown, arrayIndex, theSortCol, usedFoldersList);
            }
            theBuffer.append(folderBuffer);
            theBuffer.append(fileBuffer);
        }
        catch (Exception e) {
            this.logger.error(e.getMessage());
        }
    }

    private boolean isFolderUsed(ArrayList theList, String theFolder) {
        this.logger.debug("isFolderUsed called...");
        boolean theReturn = false;
        for (int index = 0; index < theList.size(); ++index) {
            if (!theFolder.equalsIgnoreCase((String)theList.get(index))) continue;
            theReturn = true;
            break;
        }
        return theReturn;
    }

    private String getSortDirection(int newCol, int oldCol, boolean isDesc) {
        this.logger.debug("getSortDirection called...");
        String out = newCol == oldCol ? (isDesc ? "a" : "d") : "a";
        return out;
    }

    private void renderHTML(StringBuffer theBuffer, String theExtension, String thePath, boolean theFoldersOnly, boolean theLockDown, boolean theUseZip, String theZipFile, String theZipPath, int theSortCol, boolean isSortDesc) {
        this.logger.debug("renderHTML called...");
        TranslationProcessor tp = this.getTranslationProcessor();
        theBuffer.append("<style>\n");
        theBuffer.append("body{background-color:white;}\n");
        theBuffer.append(".normhead{position:relative;background-image:url(").append(NORMBACKGROUNDIMAGE).append(");background-repeat:no-repeat;background-position:right;}\n");
        theBuffer.append(".overhead{position:relative;background-image:url(").append(OVERBACKGROUNDIMAGE).append(");background-repeat:no-repeat;background-position:right;}\n");
        theBuffer.append("table{border-collapse:collapse;}\n");
        theBuffer.append("thead.fixedHeader tr{position:relative;top:expression(document.getElementById('tableholder').scrollTop);z-index:20;}\n");
        theBuffer.append("div.tableholder{clear:both;overflow:auto;width:100%;height:100%;}");
        theBuffer.append("div.tableholder table{float:left;width:100%;height:100%;}");
        theBuffer.append("</style>");
        theBuffer.append("<img width=100% height=0 style=\"background-color:white;\">");
        theBuffer.append("<div id=\"tableholder\" class=\"tableholder\" style=\"width:100%;\">");
        theBuffer.append("<table id=\"filetable\" border=0 cellpadding=2 cellspacing=0 style=\"font-family:Arial;font-size:8pt;width:100%;\">");
        theBuffer.append("<thead class=\"fixedHeader\">");
        theBuffer.append("<tr height=21 >");
        theBuffer.append("<td colspan=2 class=\"normhead\" onmouseover=\"this.className = 'overhead';\" onmouseout=\"this.className = 'normhead';\" onclick=\"parent.fileSystem.doSort( 1, '").append(this.getSortDirection(1, theSortCol, isSortDesc)).append("' );\">" + tp.translate("Name") + "&nbsp;&nbsp;");
        if (theSortCol == 1) {
            if (isSortDesc) {
                theBuffer.append("<img src=\"").append(SORTDESCIMAGE).append("\" >");
            } else {
                theBuffer.append("<img src=\"").append(SORTASCIMAGE).append("\" >");
            }
        }
        theBuffer.append("</td>");
        theBuffer.append("<td class=\"normhead\" align=right onmouseover=\"this.className = 'overhead';\" onmouseout=\"this.className = 'normhead';\" onclick=\"parent.fileSystem.doSort( 2, '").append(this.getSortDirection(2, theSortCol, isSortDesc)).append("' );\">" + tp.translate("Size") + "&nbsp;&nbsp;");
        if (theSortCol == 2) {
            if (isSortDesc) {
                theBuffer.append("<img src=\"").append(SORTDESCIMAGE).append("\" >");
            } else {
                theBuffer.append("<img src=\"").append(SORTASCIMAGE).append("\" >");
            }
        }
        theBuffer.append("</td>");
        theBuffer.append("<td class=\"normhead\" onmouseover=\"this.className = 'overhead';\" onmouseout=\"this.className = 'normhead';\" onclick=\"parent.fileSystem.doSort( 3, '").append(this.getSortDirection(3, theSortCol, isSortDesc)).append("' );\">" + tp.translate("Type") + "&nbsp;&nbsp;");
        if (theSortCol == 3) {
            if (isSortDesc) {
                theBuffer.append("<img src=\"").append(SORTDESCIMAGE).append("\" >");
            } else {
                theBuffer.append("<img src=\"").append(SORTASCIMAGE).append("\" >");
            }
        }
        theBuffer.append("</td>");
        theBuffer.append("<td class=\"normhead\" onmouseover=\"this.className = 'overhead';\" onmouseout=\"this.className = 'normhead';\" onclick=\"parent.fileSystem.doSort( 4, '").append(this.getSortDirection(4, theSortCol, isSortDesc)).append("' );\">" + tp.translate("Date Modified") + "&nbsp;&nbsp;");
        if (theSortCol == 4) {
            if (isSortDesc) {
                theBuffer.append("<img src=\"").append(SORTDESCIMAGE).append("\" >");
            } else {
                theBuffer.append("<img src=\"").append(SORTASCIMAGE).append("\" >");
            }
        }
        theBuffer.append("</td>");
        theBuffer.append("</tr>");
        theBuffer.append("</thead>");
        theBuffer.append("<tbody>");
        StringBuffer imageBuffer = new StringBuffer();
        if (theUseZip && theZipFile != null && theZipFile.length() > 0) {
            this.logger.debug("Zip file in use...");
            File oFile = new File(thePath + theZipFile);
            if (oFile.exists()) {
                this.renderZipFileContents(theBuffer, oFile, theExtension, theFoldersOnly, theLockDown, theZipPath, theSortCol, isSortDesc);
            } else {
                this.logger.error("Zip " + theZipFile + " does not exist.");
            }
        } else {
            File oFile = new File(thePath);
            if (oFile.exists()) {
                this.renderFolderContents(theBuffer, imageBuffer, oFile, theExtension, theFoldersOnly, theLockDown, theUseZip, theSortCol, isSortDesc);
            } else if (oFile.getPath().startsWith("\\\\") && oFile.getPath().lastIndexOf("\\") == 1) {
                this.logger.error("Root network host paths like " + thePath + " cannot be viewed.");
            } else {
                this.logger.error("Path " + thePath + " does not exist.");
            }
        }
        theBuffer.append("<tr height=\"*\">");
        theBuffer.append("<td colspan=2 ");
        if (theSortCol == 1) {
            theBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        theBuffer.append(">&nbsp;");
        theBuffer.append("</td>");
        theBuffer.append("<td ");
        if (theSortCol == 2) {
            theBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        theBuffer.append(">&nbsp;");
        theBuffer.append("</td>");
        theBuffer.append("<td ");
        if (theSortCol == 3) {
            theBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        theBuffer.append(">&nbsp;");
        theBuffer.append("</td>");
        theBuffer.append("<td ");
        if (theSortCol == 4) {
            theBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        theBuffer.append(">&nbsp;");
        theBuffer.append("</td>");
        theBuffer.append("</tr>");
        theBuffer.append("</tbody>");
        theBuffer.append("</table>  ");
        theBuffer.append("</div>  ");
        theBuffer.append("<div id=\"image_preview\">");
        theBuffer.append(imageBuffer);
        theBuffer.append("</div>");
    }

    private void renderFolder(StringBuffer folderBuffer, String theFileName, String theDate, int item, boolean theLockDown, boolean asZip, int theSortCol) {
        this.logger.debug("renderFolder called...");
        folderBuffer.append("<tr height=21>");
        folderBuffer.append("<td valign=center width=16 ");
        if (theSortCol == 1) {
            folderBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        folderBuffer.append(" nowrap>");
        folderBuffer.append("<span id=\"oFolderImage").append(item).append("\" ");
        if (!theLockDown) {
            folderBuffer.append("onmouseover=\"parent.fileSystem.doFolderOver(").append(item).append(");\" ");
            folderBuffer.append("onmouseout=\"parent.fileSystem.doFolderOut(").append(item).append(");\" ");
            folderBuffer.append("onclick=\"parent.fileSystem.doFolderClick('").append(StringUtil.replaceAll(theFileName, "'", "\\'")).append("');\"");
            folderBuffer.append("ondblclick=\"parent.fileSystem.doFolderDblClick('").append(StringUtil.replaceAll(theFileName, "'", "\\'")).append("');\"");
            folderBuffer.append("style=\"cursor: pointer;\"");
        }
        folderBuffer.append(">");
        if (asZip) {
            folderBuffer.append("<img height=16 width=16 src=\"").append("WEB-CORE/utils/lookup/images/zip.gif").append("\">");
        } else {
            folderBuffer.append("<img height=16 width=16 src=\"").append("WEB-CORE/utils/lookup/images/folder-closed.gif").append("\">");
        }
        folderBuffer.append("</span>");
        folderBuffer.append("</td>");
        folderBuffer.append("<td valign=center ");
        if (theSortCol == 1) {
            folderBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        folderBuffer.append(" nowrap>");
        folderBuffer.append("<span id=\"oFolderItem").append(item).append("\" ");
        if (!theLockDown) {
            folderBuffer.append("onmouseover=\"parent.fileSystem.doFolderOver(").append(item).append(");\" ");
            folderBuffer.append("onmouseout=\"parent.fileSystem.doFolderOut(").append(item).append(");\" ");
            folderBuffer.append("onclick=\"parent.fileSystem.doFolderClick('").append(StringUtil.replaceAll(theFileName, "'", "\\'")).append("');\"");
            folderBuffer.append("ondblclick=\"parent.fileSystem.doFolderDblClick('").append(StringUtil.replaceAll(theFileName, "'", "\\'")).append("');\"");
            folderBuffer.append("style=\"cursor: pointer;\"");
        }
        folderBuffer.append(">");
        folderBuffer.append(theFileName).append("<br>");
        folderBuffer.append("</span>");
        folderBuffer.append("</td>");
        folderBuffer.append("<td valign=center width=100 ");
        if (theSortCol == 2) {
            folderBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        folderBuffer.append(" nowrap>");
        folderBuffer.append("&nbsp;");
        folderBuffer.append("</td>");
        folderBuffer.append("<td valign=center width=100 ");
        if (theSortCol == 3) {
            folderBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        folderBuffer.append(" nowrap>");
        if (asZip) {
            folderBuffer.append("Zip Folder");
        } else {
            folderBuffer.append("Folder");
        }
        folderBuffer.append("</td>");
        folderBuffer.append("<td valign=center width=100 ");
        if (theSortCol == 4) {
            folderBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        folderBuffer.append(" nowrap>");
        folderBuffer.append(theDate);
        folderBuffer.append("</td></tr>");
    }

    private void renderFileBit(StringBuffer fileBuffer, String fileName, String theDate, String theSize, int item, String theImage, String theType, int theSortCol) {
        fileBuffer.append("<tr height=21>");
        fileBuffer.append("<td valign=center width=16 ");
        if (theSortCol == 1) {
            fileBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        fileBuffer.append(" nowrap>");
        fileBuffer.append("<span id=\"oFileImage").append(item).append("\" ");
        fileBuffer.append("onmouseover=\"parent.fileSystem.doFileOver(").append(item).append(");\" ");
        fileBuffer.append("onmouseout=\"parent.fileSystem.doFileOut(").append(item).append(");\" ");
        fileBuffer.append("onclick=\"parent.fileSystem.doFileClick('").append(StringUtil.replaceAll(fileName, "'", "\\'")).append("');\"");
        fileBuffer.append("ondblclick=\"parent.fileSystem.doFileDblClick('").append(StringUtil.replaceAll(fileName, "'", "\\'")).append("');\"");
        fileBuffer.append("style=\"cursor: pointer;\">");
        fileBuffer.append("<img height=16 width=16 src=\"").append(theImage).append("\">");
        fileBuffer.append("</span>");
        fileBuffer.append("</td>");
        fileBuffer.append("<td valign=center ");
        if (theSortCol == 1) {
            fileBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        fileBuffer.append(" nowrap>");
        fileBuffer.append("<span id=\"oFileItem").append(item).append("\" ");
        fileBuffer.append("onmouseover=\"parent.fileSystem.doFileOver(").append(item).append(");\" ");
        fileBuffer.append("onmouseout=\"parent.fileSystem.doFileOut(").append(item).append(");\" ");
        fileBuffer.append("onclick=\"parent.fileSystem.doFileClick('").append(StringUtil.replaceAll(fileName, "'", "\\'")).append("');\"");
        fileBuffer.append("ondblclick=\"parent.fileSystem.doFileDblClick('").append(StringUtil.replaceAll(fileName, "'", "\\'")).append("');\"");
        fileBuffer.append("style=\"cursor: pointer;\">");
        fileBuffer.append(fileName).append("<br>");
        fileBuffer.append("</span>");
        fileBuffer.append("</td>");
        fileBuffer.append("<td valign=center width=100 align=right ");
        if (theSortCol == 2) {
            fileBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        fileBuffer.append(" nowrap>");
        fileBuffer.append(theSize);
        fileBuffer.append("</td>");
        fileBuffer.append("<td valign=center width=100 ");
        if (theSortCol == 3) {
            fileBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        fileBuffer.append(" nowrap>");
        fileBuffer.append(theType);
        fileBuffer.append("</td>");
        fileBuffer.append("<td valign=center width=100 ");
        if (theSortCol == 4) {
            fileBuffer.append(" style=\"background-color:#F7F7F7;\" ");
        }
        fileBuffer.append(" nowrap>");
        fileBuffer.append(theDate);
        fileBuffer.append("</td></tr>");
    }

    private String getPreviewId(int item) {
        return "previmg_" + item;
    }

    private void renderFile(StringBuffer theFileBuffer, StringBuffer imageBuffer, String theFilePath, String theFileName, String theDate, String theSize, int item, String theExt, boolean theLockDown, boolean theUseZip, int theSortCol) {
        String ext;
        this.logger.debug("renderFolder called...");
        if (theExt.indexOf(".") > -1) {
            try {
                ext = StringUtil.split(theExt, ".")[1];
            }
            catch (Exception e) {
                this.logger.warn("Invalid extension provided.");
                ext = "*";
            }
        } else {
            ext = theExt;
        }
        String name = theFileName.toLowerCase();
        if (ext.equals("*") || name.endsWith(ext.toLowerCase())) {
            String approot;
            FileType fileType = FileType.getFileType(name, this.getConnectionId());
            if (fileType.getType() == FileType.NamedType.IMAGE && (approot = HttpUtil.getWebAppRoot(this.pageContext.getServletContext())) != null && approot.length() > 0) {
                File approotfile = new File(approot);
                File currfile = new File(theFilePath);
                if (approotfile.exists() && currfile.exists() && currfile.getAbsolutePath().indexOf(approotfile.getAbsolutePath()) == 0) {
                    String imagePath = currfile.getAbsolutePath().substring(approotfile.getAbsolutePath().length());
                    imagePath = imagePath.length() > 0 && (imagePath.startsWith("\\") || imagePath.startsWith("/")) ? FileSystem.getEscapedPath(imagePath.substring(1)) + "/" + theFileName : theFileName;
                    if (imageBuffer != null && theFilePath != null && theFilePath.length() > 0) {
                        imageBuffer.append("<image id=\"").append(this.getPreviewId(item)).append("\" width=300 src=\"").append(imagePath).append("\" style=\"position:absolute;border:black 1 dashed;display:none;\">");
                    }
                }
            }
            this.renderFileBit(theFileBuffer, theFileName, theDate, theSize, item, fileType.getImage(), fileType.getType().getName(), theSortCol);
        } else {
            this.logger.debug("File skipped due to invalid extenstion.");
        }
    }

    public static String getDate(File fileObject) {
        return FileView.getDate(fileObject, null);
    }

    public static String getDate(File fileObject, ConnectionInfo connectionInfo) {
        long dateAsLong = fileObject.lastModified();
        Date dateAsDate = new Date(dateAsLong);
        M18NUtil m18NUtil = connectionInfo != null ? new M18NUtil(connectionInfo) : new M18NUtil();
        String theReturn = m18NUtil.getDefaultDateFormat().format(dateAsDate);
        return theReturn;
    }

    public static String getSize(long size) {
        String theReturn;
        if (size < 1024L) {
            theReturn = size + " bytes";
        } else if (size < 0x100000L) {
            BigDecimal theBigDecimal = new BigDecimal((double)size * 9.765625E-4);
            theBigDecimal = theBigDecimal.setScale(2, 4);
            theReturn = theBigDecimal.toString() + " KB";
        } else if (size < 0x40000000L) {
            BigDecimal theBigDecimal = new BigDecimal((double)size * 9.53674316E-7);
            theBigDecimal = theBigDecimal.setScale(2, 4);
            theReturn = theBigDecimal.toString() + " MB";
        } else {
            BigDecimal theBigDecimal = new BigDecimal((double)size * 9.31322575E-10);
            theBigDecimal = theBigDecimal.setScale(2, 4);
            theReturn = theBigDecimal.toString() + " GB";
        }
        return theReturn;
    }

    private boolean loadProperties() {
        this.logger.debug("loadProperties called...");
        boolean theReturn = false;
        if (this.pageContext != null && this.requestContext != null && this.element != null) {
            String ref = ((HttpServletRequest)this.pageContext.getRequest()).getHeader("referer");
            if (ref == null || !ref.endsWith("lookup/filesystem.jsp")) {
                this.logger.error("FileView cannot be used directly.");
                theReturn = false;
            } else {
                theReturn = true;
            }
            String temp = this.element.getProperty(FOLDERSPROP);
            this.foldersOnly = temp != null && temp.equalsIgnoreCase("y");
            this.logger.debug("foldersOnly = " + this.foldersOnly);
            temp = this.element.getProperty(LOCKDOWNPROP);
            this.lockDown = temp != null && temp.equalsIgnoreCase("y");
            this.logger.debug("lockDown = " + this.lockDown);
            temp = this.element.getProperty(USEZIPPROP);
            this.useZip = temp != null && temp.equalsIgnoreCase("y");
            this.logger.debug("useZip = " + this.useZip);
            this.extension = this.element.getProperty(ACTIVEEXTENSIONPROP);
            if (this.extension == null || this.extension.length() == 0) {
                this.logger.debug("No extenstion provided thus default to *.*...");
                this.extension = DEFAULTEXTENSION;
            }
            this.logger.debug("extension = " + this.extension);
            this.path = this.element.getProperty(PATHPROP);
            if (this.path == null || this.path.length() == 0) {
                this.logger.error("No path provided.");
                theReturn = false;
            } else {
                this.logger.debug("path = " + this.path);
                temp = this.path.toLowerCase();
                if (this.useZip) {
                    if (temp.endsWith(".zip") || temp.endsWith(".war") || temp.endsWith(".jar")) {
                        this.logger.debug("In zip mode and path is zip file.");
                        this.zipPath = "";
                        int tempInt = this.path.lastIndexOf("\\");
                        if (tempInt < 0) {
                            tempInt = this.path.lastIndexOf("/");
                        }
                        this.zipFile = this.path.substring(tempInt + 1);
                        this.path = this.path.substring(0, tempInt + 1);
                    } else if (FileSystem.isZipPath(temp)) {
                        this.logger.debug("In zip mode and path is under zip file.");
                        int tempInt = FileSystem.getZipPoint(temp);
                        this.zipPath = this.path.substring(tempInt);
                        this.path = this.path.substring(0, tempInt);
                        tempInt = this.path.lastIndexOf("\\");
                        if (tempInt < 0) {
                            tempInt = this.path.lastIndexOf("/");
                        }
                        this.zipFile = this.path.substring(tempInt + 1);
                        this.path = this.path.substring(0, tempInt + 1);
                    }
                } else {
                    this.zipFile = "";
                    this.zipPath = "";
                }
            }
            this.logger.debug("path = " + this.path);
            this.logger.debug("zipPath = " + this.zipPath);
            this.logger.debug("zipFile = " + this.zipFile);
            temp = this.element.getProperty(SORTCOLUMNPROP);
            if (temp == null || temp.length() == 0) {
                this.logger.debug("No sort column provided... defaulting...");
                this.sortCol = 1;
            } else {
                try {
                    this.sortCol = Integer.parseInt(temp);
                }
                catch (Exception e) {
                    this.logger.warn("Invalid sort column provided... defaulting...");
                    this.sortCol = 1;
                }
            }
            this.logger.debug("sortCol = " + this.sortCol);
            temp = this.element.getProperty(SORTDIRECTIONPROP);
            if (temp == null || temp.length() == 0) {
                this.logger.debug("No sort direction provided... defaulting...");
                this.sortDescending = false;
            } else {
                this.sortDescending = !temp.equalsIgnoreCase("a");
            }
            this.logger.debug("sortDescending = " + this.sortDescending);
        } else {
            this.logger.error("No page context provided.");
        }
        return theReturn;
    }

    @Override
    public String getHtml() {
        this.logger.debug("getHTMML called...");
        String theReturn = "";
        this.theHTMLBuffer.append("<script type=\"text/javascript\">\n");
        this.theHTMLBuffer.append("var oLoading = window.parent.document.getElementById('oFileFrameLoading');\n");
        this.theHTMLBuffer.append("if (oLoading != null){\n");
        this.theHTMLBuffer.append("\toLoading.style.display = 'none';\n");
        this.theHTMLBuffer.append("}\n");
        this.theHTMLBuffer.append("sapphire.events.attachEvent( document, 'onkeypress', parent.fileSystem.doKeydown );\n");
        this.theHTMLBuffer.append("window.onmousewheel = document.onmousewheel = parent.fileSystem.doMouseWheel;\n");
        this.theHTMLBuffer.append("</script>\n");
        if (FileSystem.isServerSideBrowsingPermitted(this.connectionInfo.getConnectionId())) {
            if (this.loadProperties()) {
                if (FileSystem.validRequestAndPath(this.path, this.requestContext.getProperty("$t"), this.connectionInfo, this.pageContext, this.logger)) {
                    this.renderHTML(this.theHTMLBuffer, this.extension, this.path, this.foldersOnly, this.lockDown, this.useZip, this.zipFile, this.zipPath, this.sortCol, this.sortDescending);
                } else {
                    this.logger.error("Not a valid request.");
                }
            } else {
                this.logger.error("Could not load required properties.");
            }
        } else {
            this.logger.error("Serverside browsing disabled.");
        }
        if (this.debugErrorMsg == null || this.debugErrorMsg.length() == 0) {
            if (this.theHTMLBuffer.length() > 0) {
                theReturn = this.theHTMLBuffer.toString();
            }
        } else {
            theReturn = this.getError();
            if (this.theHTMLBuffer.length() > 0) {
                theReturn = theReturn + this.theHTMLBuffer.toString();
            }
        }
        return theReturn;
    }
}

