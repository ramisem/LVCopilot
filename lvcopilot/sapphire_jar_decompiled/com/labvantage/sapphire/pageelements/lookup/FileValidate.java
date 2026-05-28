/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.lookup;

import com.labvantage.sapphire.pageelements.lookup.FileSystem;
import com.labvantage.sapphire.pageelements.lookup.FileView;
import java.io.File;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import sapphire.pageelements.BaseElement;
import sapphire.util.StringUtil;

public class FileValidate
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 55457 $";
    public static final String PATHPROP = "path";
    public static final String RETURNPATHPROP = "returnpath";
    public static final String USEZIPPROP = "usezip";
    private String path;
    private String returnpath;
    private String date;
    private String size;
    private boolean valid;
    private boolean useZip;

    public FileValidate() {
        this.logger.debug("Class created...");
    }

    private boolean isValid(String thePath, boolean theUseZip, StringBuffer dateBuffer, StringBuffer sizeBuffer) {
        this.logger.debug("isValid called...");
        boolean theReturn = false;
        if (theUseZip && FileSystem.isZipPath(thePath)) {
            this.logger.debug("Zip file in use...");
            int temp = FileSystem.getZipPoint(thePath);
            String normPath = thePath.substring(0, temp);
            this.logger.debug("normPath = " + normPath);
            File fileObject = new File(normPath);
            if (fileObject.exists()) {
                String zipPath = FileSystem.getEscapedPath(thePath.substring(temp + 1));
                this.logger.debug("zipPath = " + zipPath);
                try {
                    ZipFile zipObject = new ZipFile(fileObject);
                    Enumeration<? extends ZipEntry> zipEntries = zipObject.entries();
                    while (zipEntries.hasMoreElements()) {
                        ZipEntry zipEntry = zipEntries.nextElement();
                        String name = zipEntry.getName();
                        this.logger.debug("name = " + name);
                        if (name.equalsIgnoreCase(zipPath)) {
                            this.logger.debug("Zip entry found - exact match.");
                            theReturn = true;
                            sizeBuffer.append(FileView.getSize(zipEntry.getSize()));
                            break;
                        }
                        if (name.indexOf(zipPath) <= -1) continue;
                        this.logger.debug("Zip entry found in zip path.");
                        theReturn = true;
                        sizeBuffer.append(FileView.getSize(zipEntry.getSize()));
                        break;
                    }
                    if (!theReturn) {
                        this.debugErrorMsg = "File or folder does not exist.";
                        this.logger.error(this.debugErrorMsg);
                    }
                }
                catch (Exception e) {
                    this.debugErrorMsg = "Zip file could not be opened.";
                    this.logger.error(this.debugErrorMsg);
                }
            } else {
                this.debugErrorMsg = "Zip File does not exist.";
                this.logger.error(this.debugErrorMsg);
            }
        } else {
            this.logger.debug("No zip file in use...");
            File fileObject = new File(thePath);
            if (fileObject.exists()) {
                dateBuffer.append(FileView.getDate(fileObject, this.connectionInfo));
                sizeBuffer.append(FileView.getSize(fileObject.length()));
                theReturn = true;
            } else {
                this.debugErrorMsg = "File or folder does not exist.";
                this.logger.error(this.debugErrorMsg);
            }
        }
        return theReturn;
    }

    private boolean loadProperties() {
        this.logger.debug("loadProperties called...");
        boolean theReturn = false;
        if (this.pageContext != null && this.requestContext != null && this.element != null) {
            theReturn = true;
            String temp = this.element.getProperty(USEZIPPROP);
            this.useZip = temp != null && temp.equalsIgnoreCase("y");
            this.logger.debug("useZip = " + this.useZip);
            this.path = this.element.getProperty(PATHPROP);
            if (this.path == null || this.path.length() == 0) {
                this.debugErrorMsg = "No path provided.";
                this.logger.error(this.debugErrorMsg);
                theReturn = false;
            } else {
                this.logger.debug("path = " + this.path);
            }
            this.returnpath = this.element.getProperty(RETURNPATHPROP, "");
            this.logger.debug("returnpath = " + this.returnpath);
        } else {
            this.debugErrorMsg = "No page context provided.";
            this.logger.error(this.debugErrorMsg);
        }
        return theReturn;
    }

    private void renderScript(StringBuffer theHTMLBuffer, String thePath, String theReturnPath, boolean theValid, String theDate, String theSize, String theError) {
        this.logger.debug("renderScript called...");
        theHTMLBuffer.append("<script>");
        theHTMLBuffer.append("var fileValidate;");
        theHTMLBuffer.append("function FileValidate(){");
        theHTMLBuffer.append("this.sDate  = '").append(theDate).append("';");
        theHTMLBuffer.append("this.sSize  = '").append(theSize).append("';");
        theHTMLBuffer.append("this.sError = '").append(theError).append("';");
        if (theValid) {
            theHTMLBuffer.append("this.lValid = true;");
        } else {
            theHTMLBuffer.append("this.lValid = false;");
        }
        theHTMLBuffer.append("}");
        theHTMLBuffer.append("fileValidate = new FileValidate();");
        StringBuffer logout = new StringBuffer();
        logout.append("-- START FILE INFO --\n");
        logout.append("Path = ").append(thePath).append("\n");
        String escapedpath = FileSystem.getEscapedPath(thePath);
        logout.append("Escaped Path = ").append(escapedpath).append("\n");
        escapedpath = StringUtil.replaceAll(escapedpath, "'", "\\'");
        logout.append("Replaced Escaped Path = ").append(escapedpath).append("\n");
        logout.append("Return Path = ").append(theReturnPath).append("\n");
        String returnescapedpath = FileSystem.getEscapedPath(theReturnPath);
        logout.append("Escaped Return Path = ").append(escapedpath).append("\n");
        returnescapedpath = StringUtil.replaceAll(returnescapedpath, "'", "\\'");
        logout.append("Replaced Escaped Return Path = ").append(returnescapedpath).append("\n");
        logout.append("-- END FILE INFO --\n");
        this.logger.debug(logout.toString());
        theHTMLBuffer.append("parent.fileSystem.doValidate( '").append(escapedpath).append("', false, '").append(returnescapedpath).append("' );");
        theHTMLBuffer.append("</script>");
    }

    @Override
    public String getHtml() {
        this.logger.debug("getHTMML called...");
        StringBuffer theHTMLBuffer = new StringBuffer();
        String theReturn = "";
        if (this.loadProperties()) {
            StringBuffer theDataBuffer = new StringBuffer();
            StringBuffer theSizeBuffer = new StringBuffer();
            if (this.isValid(this.path, this.useZip, theDataBuffer, theSizeBuffer)) {
                if (theDataBuffer.length() > 0) {
                    this.date = theDataBuffer.toString();
                }
                if (theSizeBuffer.length() > 0) {
                    this.size = theSizeBuffer.toString();
                }
                this.valid = true;
                this.logger.debug("valid = " + this.valid);
            } else {
                this.date = "";
                this.size = "";
                this.valid = false;
                this.logger.debug("valid = " + this.valid);
            }
        } else {
            this.debugErrorMsg = "Could not load required properties.";
            this.logger.error(this.debugErrorMsg);
        }
        if (this.debugErrorMsg == null || this.debugErrorMsg.length() == 0) {
            this.renderScript(theHTMLBuffer, this.path, this.returnpath, this.valid, this.date, this.size, "");
        } else {
            this.renderScript(theHTMLBuffer, this.path, this.returnpath, false, "", "", this.debugErrorMsg);
        }
        if (theHTMLBuffer.length() > 0) {
            theReturn = theHTMLBuffer.toString();
        }
        return theReturn;
    }
}

