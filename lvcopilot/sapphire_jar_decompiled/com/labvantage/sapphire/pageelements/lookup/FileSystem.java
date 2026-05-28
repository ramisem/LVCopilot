/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.lookup;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.MiscUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FileSystem
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 102349 $";
    public static final String FUNCTIONTEXTPROP = "function";
    public static final String TITLEPROP = "title";
    public static final String BUTTONTEXTPROP = "button";
    public static final String FOLDERSPROP = "folders";
    public static final String EXTENSIONSPROP = "extensions";
    public static final String ACTIVEEXTENSIONPROP = "activeextension";
    public static final String PATHPROP = "path";
    public static final String LOCKUPPROP = "lockup";
    public static final String DIRLOCKPROP = "dirlock";
    public static final String SHORTCUTSPROP = "shortcuts";
    public static final String VALIDATEPROP = "validate";
    public static final String USEZIPPROP = "usezip";
    public static final String SORTCOLUMNPROP = "sortcolumn";
    public static final String SORTDIRECTIONPROP = "sortdirection";
    public static final String FILELOCATIONTYPEPROP = "filelocationtype";
    public static final String NAVIGATED = "$n";
    public static final String BASEDIRPROP = "basedir";
    public static final String FIELDIDPROP = "fieldid";
    public static final String RELATIVEPROP = "relative";
    public static final String FORCESUBPROP = "forcesub";
    public static final String WEBPROP = "web";
    public static final String EMBEDDEDPROP = "embedded";
    public static final String FILETYPEPROP = "filetype";
    public static final String DEFAULTFUNCTIONTEXT = "Look in";
    public static final String DEFAULTTITLETEXT = "Open";
    public static final String DEFAULTBUTTONTEXT = "Open";
    public static final int DEFAULTSORTCOLUMN = 1;
    public static final String DEFAULTSORTDIR = "a";
    public static final String COMPUTERPLACEIMAGE = "WEB-CORE/utils/lookup/images/computerplace.gif";
    public static final String MOVEUPIMAGE = "WEB-CORE/utils/lookup/images/moveup.gif";
    public static final String MOVEUPDISABLEDIMAGE = "WEB-CORE/utils/lookup/images/moveup-disabled.gif";
    public static final String MYDOCUMENTSIMAGE = "WEB-CORE/utils/lookup/images/mydocuments.gif";
    public static final String SECUIRTY_POLICY = "SecurityPolicy";
    public static final String SECUIRTY_POLICYNODE = "Sapphire Custom";
    public static final String SECURITY_POLICYPROPERTYLIST = "filebrowser";
    public static final String FILELOCATIONS_POLICY = "FileLocationPolicy";
    public static final String FILELOCATIONS_POLICYNODE = "Sapphire Custom";
    public static final String FILELOCATIONS_LOCATIONS_COLLECTION = "locations";
    public static final String FILELOCATIONS_LOCATION_PROPERTY = "location";
    public static final String FILELOCATIONS_ID_PROPERTY = "id";
    public static final String FILELOCATIONS_TITLE_PROPERTY = "title";
    public static final String FILELOCATIONS_DESCRIPTION_PROPERTY = "description";
    public static final String FILELOCATIONS_IMAGE_PROPERTY = "image";
    public static final String FILELOCATIONS_LOCATIONS_STARTLOCATION = "startlocation";
    private boolean securityShowPath = true;
    private boolean securityAllowRoot = true;
    private boolean navigated = false;
    private String functionText;
    private String buttonText;
    private String title;
    private String rootPath;
    private String path;
    private String fieldId;
    private boolean foldersOnly;
    private boolean lockUp;
    private boolean completeLock;
    private boolean relative;
    private boolean web;
    private boolean forceSub;
    private boolean validate;
    private boolean useZip;
    private boolean embedded;
    private String fileLocationType;
    private PropertyListCollection fileLocations;
    private String securityNavigationLock = "";
    private int sortCol;
    private String sortDir;
    private String[] extensionsArray;
    private String[] descsArray;
    private String[] shortCutsArray;
    private String[] shortCutDescsArray;
    private String file;
    private String activeExt;
    private StringBuffer theHTMLBuffer = new StringBuffer();

    public FileSystem() {
        this.logger.debug("Class created...");
    }

    public static String getFileLocation(String location) {
        return FileManager.getFileLocation(location);
    }

    public static boolean validateFileName(String fileName) {
        return FileUtil.validateFileName(fileName);
    }

    public static PropertyListCollection getFileLocations(String locationType, PageContext pageContext) {
        return FileManager.getFileLocations(locationType, pageContext);
    }

    public static PropertyList getFileTypeExcludes(String node, PageContext pageContext) {
        return FileManager.getFileTypeExcludes(node, pageContext);
    }

    private static boolean isParentFile(File child, File parent) {
        return FileUtil.isParentFile(child, parent);
    }

    private boolean validPath(String path, PropertyListCollection fileLocations, boolean web) {
        String tpath = path.indexOf("[") > -1 && path.indexOf("]") > -1 ? FileSystem.getFileLocation(path) : path;
        return FileSystem.validPath(tpath, tpath, fileLocations, false, false, true, web, this.pageContext, this.logger).length() == 0;
    }

    public static boolean isServerSideBrowsingPermitted(String connectionId) {
        QueryProcessor qp = new QueryProcessor(connectionId);
        DataSet ds = qp.getSqlDataSet("SELECT propertyvalue FROM sysconfig WHERE propertyid = 'allowserversidebrowsing'");
        if (ds != null && ds.getRowCount() > 0 && ds.getValue(0, "propertyvalue", "Y").equalsIgnoreCase("N")) {
            return false;
        }
        return SecurityPolicyUtil.isServerSideBrowsingPermitted(connectionId);
    }

    private String getSecurityToken() {
        CacheUtil.removeAllEndWith(this.connectionInfo.getDatabaseId(), "FileSystemSecurity", ";" + this.connectionInfo.getSysuserId());
        String securitytoken = StringUtil.getRandomString(50);
        PropertyList sec = new PropertyList();
        try {
            sec.setProperty("rootPath", this.rootPath);
            sec.setProperty("fileLocations", this.fileLocations);
            sec.setProperty("lockUp", this.lockUp ? "Y" : "N");
            sec.setProperty("completeLock", this.completeLock ? "Y" : "N");
            sec.setProperty("securityNavigationLock", this.securityNavigationLock.equalsIgnoreCase("File Locations") ? "Y" : "N");
            sec.setProperty(WEBPROP, this.web ? "Y" : "N");
        }
        catch (Exception exception) {
            // empty catch block
        }
        CacheUtil.put(this.connectionInfo.getDatabaseId(), "FileSystemSecurity", securitytoken + ";" + this.connectionInfo.getSysuserId(), sec.toJSONString(false, false));
        return securitytoken;
    }

    private String validPath(String path, String rootPath, PropertyListCollection fileLocations, boolean lockUp, boolean completeLock, boolean lockFileLocations, boolean web) {
        return FileSystem.validPath(path, rootPath, fileLocations, lockUp, completeLock, lockFileLocations, web, this.pageContext, this.logger);
    }

    public static boolean validRequestAndPath(String path, String securetoken, ConnectionInfo connectionInfo, PageContext pageContext, Logger logger) {
        Object t = CacheUtil.get(connectionInfo.getDatabaseId(), "FileSystemSecurity", securetoken + ";" + connectionInfo.getSysuserId());
        if (t instanceof String) {
            try {
                return FileSystem.validPath(path, new PropertyList(new JSONObject(t.toString())), pageContext, logger);
            }
            catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private static boolean validPath(String path, PropertyList tokenObject, PageContext pageContext, Logger logger) {
        if (path != null && path.length() > 0) {
            try {
                if (tokenObject.getProperty("rootPath").length() == 0) {
                    return false;
                }
                if (tokenObject.getProperty("lockUp", "Y").equalsIgnoreCase("Y") || tokenObject.getProperty("completeLock", "Y").equalsIgnoreCase("Y") || tokenObject.getProperty(WEBPROP, "Y").equalsIgnoreCase("Y")) {
                    String out = FileSystem.validPath(path, tokenObject.getProperty("rootPath"), tokenObject.getCollectionNotNull("fileLocations"), tokenObject.getProperty("lockUp", "Y").equalsIgnoreCase("Y"), tokenObject.getProperty("completeLock", "Y").equalsIgnoreCase("Y"), tokenObject.getProperty("securityNavigationLock", "Y").equalsIgnoreCase("Y"), tokenObject.getProperty(WEBPROP, "Y").equalsIgnoreCase("Y"), pageContext, logger);
                    return out.length() <= 0;
                }
                if (tokenObject.getProperty("securityNavigationLock", "Y").equalsIgnoreCase("Y")) {
                    return FileSystem.validShortcutPath(path, tokenObject.getCollectionNotNull("fileLocations"), tokenObject.getProperty(WEBPROP, "Y").equalsIgnoreCase("Y"), pageContext, logger);
                }
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private static String checkPath(String path, String rootPath, PropertyListCollection fileLocations, boolean web, PageContext pageContext, Logger logger) {
        String out = rootPath;
        if (fileLocations != null && fileLocations.size() > 0) {
            boolean found = false;
            for (int i = 0; i < fileLocations.size(); ++i) {
                PropertyList fileLocation = fileLocations.getPropertyList(i);
                File location = new File(FileSystem.getFileLocation(fileLocation.getProperty(FILELOCATIONS_LOCATION_PROPERTY)));
                if (!FileSystem.isParentFile(new File(path), location) || !FileSystem.isParentFile(new File(rootPath), location)) continue;
                found = true;
                break;
            }
            if (!found) {
                out = FileSystem.getDefaultFileLocationPath(fileLocations, web, pageContext, logger);
            }
        }
        return out;
    }

    private static boolean validShortcutPath(String path, PropertyListCollection fileLocations, boolean web, PageContext pageContext, Logger logger) {
        String tpath = path.indexOf("[") > -1 && path.indexOf("]") > -1 ? FileSystem.getFileLocation(path) : path;
        boolean out = false;
        if (web) {
            out = FileSystem.validPath(tpath, tpath, fileLocations, false, false, true, web, pageContext, logger).length() == 0;
        } else if (fileLocations != null && fileLocations.size() > 0) {
            for (int i = 0; i < fileLocations.size(); ++i) {
                File fTpath;
                PropertyList fileLocation = fileLocations.getPropertyList(i);
                String location = FileSystem.getFileLocation(fileLocation.getProperty(FILELOCATIONS_LOCATION_PROPERTY));
                File fLocation = new File(location);
                if (fLocation.equals(fTpath = new File(tpath))) {
                    out = fTpath.exists();
                    break;
                }
                if (!FileSystem.isParentFile(fTpath, fLocation)) continue;
                out = fTpath.exists();
                break;
            }
        }
        return out;
    }

    private static String validPath(String path, String rootPath, PropertyListCollection fileLocations, boolean lockUp, boolean completeLock, boolean lockFileLocations, boolean web, PageContext pageContext, Logger logger) {
        String out = "";
        if (lockFileLocations && !web) {
            out = FileSystem.checkPath(path, rootPath, fileLocations, web, pageContext, logger);
        } else if (web) {
            String def = FileSystem.getDefaultFileLocationPath(fileLocations, web, pageContext, logger);
            File fPath = new File(path);
            File fDef = new File(def);
            File fRoot = new File(rootPath);
            if (completeLock) {
                if (!fPath.getAbsolutePath().equalsIgnoreCase(fDef.getAbsolutePath())) {
                    out = def;
                }
            } else if (!FileSystem.isParentFile(fPath, fDef) || !FileSystem.isParentFile(fRoot, fDef)) {
                out = def;
            } else if (lockUp && !FileSystem.isParentFile(fPath, fRoot)) {
                out = def;
            }
        } else if (completeLock) {
            if (!new File(rootPath = FileSystem.checkPath(rootPath, rootPath, fileLocations, web, pageContext, logger)).getAbsolutePath().equalsIgnoreCase(new File(path).getAbsolutePath())) {
                out = rootPath;
            }
        } else if (lockUp) {
            rootPath = FileSystem.checkPath(rootPath, rootPath, fileLocations, web, pageContext, logger);
            File fPath = new File(path);
            if (!FileSystem.isParentFile(new File(path), new File(rootPath))) {
                out = rootPath;
            }
        }
        return out;
    }

    private static String getWebAppRoot(ServletContext servletContext, Logger logger) {
        String p = sapphire.util.HttpUtil.getWebAppRoot(servletContext);
        logger.debug("getWebAppRoot - p(1) " + p);
        String s = p;
        File f = new File(s);
        if (f == null || !f.exists()) {
            logger.debug("getWebAppRoot - failed to create path (1)");
            if (s.startsWith("/") && s.length() > 0) {
                s = s.substring(1);
                logger.debug("getWebAppRoot - s(1a) " + s);
                f = new File(s);
                if (f != null && f.exists()) {
                    s = f.getAbsolutePath();
                    logger.debug("getWebAppRoot - s(3) " + s);
                } else {
                    logger.debug("getWebAppRoot - could not find path. Return original.");
                    s = p;
                    logger.debug("getWebAppRoot - s(4) " + s);
                }
            }
        } else {
            s = f.getAbsolutePath();
            logger.debug("getWebAppRoot - s(2) " + s);
        }
        return s;
    }

    private String getDefaultFileLocationPath(PropertyListCollection fileLocations, boolean web) {
        return FileSystem.getDefaultFileLocationPath(fileLocations, web, this.pageContext, this.logger);
    }

    private static String getDefaultFileLocationPath(PropertyListCollection fileLocations, boolean web, PageContext pageContext, Logger logger) {
        String path = "";
        if (web) {
            path = FileSystem.getWebAppRoot(pageContext.getServletContext(), logger);
        } else if (fileLocations != null) {
            PropertyList location = fileLocations.find("default", "Y");
            if (location != null) {
                path = location.getProperty(FILELOCATIONS_LOCATION_PROPERTY, "");
            } else {
                logger.debug("No start location provided thus use first in file locations...");
                path = fileLocations.getPropertyList(0).getProperty(FILELOCATIONS_LOCATION_PROPERTY, "");
            }
            if (path.length() == 0) {
                logger.debug("defaulting path...");
                try {
                    path = web ? FileSystem.getWebAppRoot(pageContext.getServletContext(), logger) : Configuration.getInstance().getSapphireHome();
                }
                catch (Exception e) {
                    path = "";
                }
            } else {
                path = FileSystem.getFileLocation(path);
            }
        }
        return path;
    }

    private boolean loadProperties() {
        this.logger.debug("loadProperties called...");
        boolean theReturn = true;
        if (this.pageContext != null && this.requestContext != null && this.element != null) {
            String[] tempArray2;
            int iIndex;
            String[] tempArray1;
            String vp;
            ConfigurationProcessor cp = new ConfigurationProcessor(this.pageContext);
            PropertyList policy = null;
            try {
                policy = cp.getPolicy(SECUIRTY_POLICY, "Sapphire Custom");
                if (policy != null && policy.getPropertyList(SECURITY_POLICYPROPERTYLIST) != null) {
                    this.securityAllowRoot = policy.getPropertyList(SECURITY_POLICYPROPERTYLIST).getProperty("allowrootpath", "Y").equalsIgnoreCase("Y");
                    this.securityShowPath = policy.getPropertyList(SECURITY_POLICYPROPERTYLIST).getProperty("showfullpath", "Y").equalsIgnoreCase("Y");
                    this.securityNavigationLock = policy.getPropertyList(SECURITY_POLICYPROPERTYLIST).getProperty("navigationlock", "");
                }
            }
            catch (Exception e) {
                Logger.logError(e.getMessage(), e);
            }
            this.navigated = this.element.getProperty(NAVIGATED).equalsIgnoreCase("Y");
            this.functionText = this.element.getProperty(FUNCTIONTEXTPROP);
            if (this.functionText == null || this.functionText.length() == 0) {
                this.logger.debug("defaulting function...");
                this.functionText = DEFAULTFUNCTIONTEXT;
            }
            this.logger.debug("functionText = " + this.functionText);
            this.buttonText = this.element.getProperty(BUTTONTEXTPROP);
            if (this.buttonText == null || this.buttonText.length() == 0) {
                this.logger.debug("defaulting button...");
                this.buttonText = "Open";
            }
            this.logger.debug("buttonText = " + this.buttonText);
            this.title = this.element.getProperty("title");
            if (this.title == null || this.title.length() == 0) {
                this.logger.debug("defaulting title...");
                this.title = "Open";
            }
            this.logger.debug("title = " + this.title);
            String temp = this.element.getProperty(USEZIPPROP);
            this.useZip = temp != null && temp.equalsIgnoreCase("y");
            this.logger.debug("useZip = " + this.useZip);
            temp = this.element.getProperty(EMBEDDEDPROP);
            this.embedded = temp != null && temp.equalsIgnoreCase("y");
            this.logger.debug("embedded = " + this.embedded);
            temp = this.element.getProperty(WEBPROP);
            this.web = temp != null && temp.equalsIgnoreCase("y");
            this.logger.debug("web = " + this.web);
            if (!this.web) {
                this.fileLocationType = this.element.getProperty(FILELOCATIONTYPEPROP, "");
                if (this.fileLocationType.length() > 0) {
                    this.logger.debug("fileLocationType = " + this.fileLocationType);
                    this.fileLocations = FileSystem.getFileLocations(this.fileLocationType, this.pageContext);
                    if (this.fileLocations == null || this.fileLocations.size() == 0) {
                        this.logger.warn("Could not obtain file locations");
                        this.fileLocationType = "";
                    } else {
                        this.logger.debug("fileLocations.size() = " + this.fileLocations.size());
                    }
                } else {
                    this.fileLocations = FileSystem.getFileLocations("Sapphire Custom", this.pageContext);
                }
            } else {
                this.fileLocationType = "";
                this.fileLocations = null;
            }
            temp = this.element.getProperty(RELATIVEPROP);
            this.relative = temp != null && temp.equalsIgnoreCase("y");
            this.logger.debug("relative = " + this.relative);
            if (this.relative) {
                this.logger.debug("Force lockup from relative.");
                this.lockUp = true;
            } else {
                this.lockUp = this.securityNavigationLock.equalsIgnoreCase("lock up") || this.securityNavigationLock.equalsIgnoreCase("full lock") ? true : (temp = this.element.getProperty(LOCKUPPROP)) != null && temp.equalsIgnoreCase("y");
            }
            this.logger.debug("lockUp = " + this.lockUp);
            this.completeLock = this.securityNavigationLock.equalsIgnoreCase("full lock") ? true : (temp = this.element.getProperty(DIRLOCKPROP)) != null && temp.equalsIgnoreCase("y");
            this.logger.debug("completeLock = " + this.completeLock);
            if (this.pageContext != null && this.pageContext.getRequest() instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
                String query = request.getQueryString().toLowerCase();
                if (!request.getMethod().equalsIgnoreCase("POST") || query.contains("&path=") || query.contains("&basedir=")) {
                    this.logger.error("File browser only works in POST method and cannot pass path and basedir through URL.");
                } else {
                    if (this.securityAllowRoot) {
                        this.rootPath = this.element.getProperty(BASEDIRPROP);
                    } else if (this.fileLocations != null) {
                        if (this.validPath(this.element.getProperty(BASEDIRPROP), this.fileLocations, this.web)) {
                            this.rootPath = this.element.getProperty(BASEDIRPROP);
                        }
                    } else if (this.navigated) {
                        this.rootPath = this.element.getProperty(BASEDIRPROP);
                    }
                    this.path = this.element.getProperty(PATHPROP);
                }
            } else {
                if (this.securityAllowRoot) {
                    this.rootPath = this.element.getProperty(BASEDIRPROP);
                } else if (this.fileLocations != null) {
                    if (this.validPath(this.element.getProperty(BASEDIRPROP), this.fileLocations, this.web)) {
                        this.rootPath = this.element.getProperty(BASEDIRPROP);
                    }
                } else if (this.navigated) {
                    this.rootPath = this.element.getProperty(BASEDIRPROP);
                }
                this.path = this.element.getProperty(PATHPROP);
            }
            if (this.path == null || this.path.length() == 0) {
                this.logger.debug("defaulting path...");
                if (this.rootPath == null || this.rootPath.length() == 0) {
                    if (!this.web && this.fileLocationType.length() > 0 && this.fileLocations != null && this.fileLocations.size() > 0) {
                        this.logger.debug("setting path from file locations...");
                        this.logger.debug("setting basedir from file locations...");
                        this.rootPath = this.path = this.getDefaultFileLocationPath(this.fileLocations, this.web);
                    } else {
                        this.logger.debug("defaulting path...");
                        this.logger.debug("defaulting basedir...");
                        try {
                            this.path = this.web ? FileSystem.getWebAppRoot(this.pageContext.getServletContext(), this.logger) : Configuration.getInstance().getSapphireHome();
                        }
                        catch (Exception e) {
                            this.path = "";
                        }
                        this.rootPath = this.path;
                    }
                } else if (this.rootPath.indexOf("[") > -1 && this.rootPath.indexOf("]") > -1) {
                    this.rootPath = this.path = FileSystem.getFileLocation(this.rootPath);
                } else {
                    this.logger.debug("setting path to basedir...");
                    this.path = this.rootPath;
                }
                this.file = "";
            } else {
                if (this.useZip && FileSystem.isZipPath(this.path)) {
                    this.logger.debug("inside zip or war file...");
                    if (this.path.lastIndexOf(".") == this.path.length() - 4) {
                        this.logger.debug("Zip file...");
                        if (this.path.lastIndexOf("/") > -1) {
                            this.file = this.path.substring(this.path.lastIndexOf("/") + 1);
                            this.path = this.path.substring(0, this.path.lastIndexOf("/"));
                        } else {
                            this.file = this.path.substring(this.path.lastIndexOf("\\") + 1);
                            this.path = this.path.substring(0, this.path.lastIndexOf("\\"));
                        }
                    } else {
                        this.logger.debug("Zip folder...");
                        this.file = "";
                    }
                } else {
                    File oFile;
                    vp = this.validPath(this.path, this.rootPath, this.fileLocations, this.lockUp, this.completeLock, this.securityNavigationLock.equalsIgnoreCase("File Locations"), this.web);
                    if (vp.length() > 0) {
                        this.logger.warn("Path or rootpath out side of security specifications. Thus defaulted to \"" + vp + "\".");
                        this.path = vp;
                        this.rootPath = vp;
                    }
                    if ((oFile = new File(this.path)).exists() && !oFile.isDirectory()) {
                        this.logger.debug("file detected...");
                        if (oFile.getName().toLowerCase().endsWith(".zip") || oFile.getName().toLowerCase().endsWith(".war") || oFile.getName().toLowerCase().endsWith(".jar")) {
                            this.logger.debug("zip or war or jar file...");
                            if (this.useZip) {
                                this.logger.debug("Use zip or war file as folder.");
                                this.path = oFile.getPath();
                                this.file = "";
                            } else {
                                this.logger.debug("cannot use zip or war file as folder.");
                                this.file = oFile.getName();
                                this.path = oFile.getParent();
                            }
                        } else {
                            this.logger.debug("normal file...");
                            this.file = oFile.getName();
                            this.path = oFile.getParent();
                        }
                    } else {
                        if (this.path.endsWith(":")) {
                            this.path = this.path + "\\";
                        }
                        this.file = "";
                    }
                }
                if (this.rootPath == null || this.rootPath.length() == 0) {
                    this.logger.debug("setting basedir to path...");
                    this.rootPath = this.path;
                }
            }
            vp = this.validPath(this.path, this.rootPath, this.fileLocations, this.lockUp, this.completeLock, this.securityNavigationLock.equalsIgnoreCase("File Locations"), this.web);
            if (vp.length() > 0) {
                this.logger.warn("Path or rootpath out side of security specifications. Thus defaulted to \"" + vp + "\".");
                this.path = vp;
                this.rootPath = vp;
            }
            this.logger.debug("path = " + this.path);
            this.logger.debug("file = " + this.file);
            this.logger.debug("rootPath = " + this.rootPath);
            this.fieldId = this.element.getProperty(FIELDIDPROP);
            this.logger.debug("fieldId = " + this.fieldId);
            temp = this.element.getProperty(VALIDATEPROP);
            this.validate = temp == null || !temp.equalsIgnoreCase("n");
            temp = this.element.getProperty(FORCESUBPROP);
            this.forceSub = temp != null && temp.equalsIgnoreCase("y");
            this.logger.debug("forceSub = " + this.forceSub);
            temp = this.element.getProperty(FOLDERSPROP);
            this.foldersOnly = temp != null && temp.equalsIgnoreCase("y");
            this.logger.debug("foldersOnly = " + this.foldersOnly);
            temp = this.element.getProperty(EXTENSIONSPROP);
            if (temp != null && temp.length() > 0) {
                this.logger.debug("temp = " + temp);
                tempArray1 = StringUtil.split(temp, ";");
                this.descsArray = new String[tempArray1.length];
                this.extensionsArray = new String[tempArray1.length];
                for (iIndex = 0; iIndex < tempArray1.length; ++iIndex) {
                    if (tempArray1[iIndex].indexOf("|") > -1) {
                        tempArray2 = StringUtil.split(tempArray1[iIndex], "|");
                        this.descsArray[iIndex] = tempArray2[0];
                        this.logger.debug("Desc " + tempArray2[0] + " added.");
                        this.extensionsArray[iIndex] = tempArray2[1];
                        this.logger.debug("Ext " + tempArray2[1] + " added.");
                        continue;
                    }
                    this.descsArray[iIndex] = tempArray1[iIndex];
                    this.logger.debug("Desc " + tempArray1[iIndex] + " added.");
                    this.extensionsArray[iIndex] = tempArray1[iIndex];
                    this.logger.debug("Ext " + tempArray1[iIndex] + " added.");
                }
                this.activeExt = this.element.getProperty(ACTIVEEXTENSIONPROP);
                if (this.activeExt == null || this.activeExt.length() == 0 || !MiscUtil.MiscArray.isStringInArray(this.extensionsArray, this.activeExt, true)) {
                    this.logger.debug("No valid active extenstion provided thus default to first in array...");
                    this.activeExt = this.extensionsArray[0];
                }
            } else {
                this.logger.debug("No extenstions thus check deprecated filetype...");
                temp = this.element.getProperty(FILETYPEPROP);
                if (temp != null && temp.length() > 0) {
                    this.logger.debug("File Type provided");
                    if (temp.equalsIgnoreCase("folder")) {
                        this.logger.debug("File type set to folder.");
                        this.foldersOnly = true;
                        this.descsArray = new String[]{"All Files"};
                        this.extensionsArray = new String[]{"*.*"};
                        this.activeExt = "*.*";
                    } else if (temp.indexOf(",") > -1) {
                        String[] exts = StringUtil.split(temp, ",");
                        ArrayList<String> d = new ArrayList<String>();
                        ArrayList<String> e = new ArrayList<String>();
                        for (String ex : exts) {
                            int i = ex.indexOf(".");
                            String desc = i > -1 ? ex.substring(i + 1) : ex;
                            d.add(desc.toUpperCase() + " (*." + desc.toUpperCase() + ")");
                            e.add("*." + desc);
                        }
                        this.activeExt = (String)e.get(0);
                        this.descsArray = d.toArray(new String[0]);
                        this.extensionsArray = e.toArray(new String[0]);
                    } else {
                        this.logger.debug("File type = " + temp);
                        this.descsArray = new String[]{temp.toUpperCase() + " (*." + temp.toUpperCase() + ")"};
                        this.extensionsArray = new String[]{"*." + temp};
                        this.activeExt = "*." + temp;
                    }
                } else {
                    this.logger.debug("No extenstions provided thus default to all...");
                    this.descsArray = new String[]{"All Files"};
                    this.extensionsArray = new String[]{"*.*"};
                    this.activeExt = "*.*";
                }
            }
            this.logger.debug("activeExt = " + this.activeExt);
            if (!this.web) {
                temp = this.element.getProperty(SHORTCUTSPROP);
                if (temp != null && temp.length() > 0) {
                    this.logger.debug("temp = " + temp);
                    tempArray1 = StringUtil.split(temp, ";");
                    ArrayList<String> scdesc = new ArrayList<String>();
                    ArrayList<String> sca = new ArrayList<String>();
                    for (iIndex = 0; iIndex < tempArray1.length; ++iIndex) {
                        String vps;
                        if (tempArray1[iIndex].indexOf("|") > -1) {
                            tempArray2 = StringUtil.split(tempArray1[iIndex], "|");
                            vps = this.validPath(tempArray2[1], tempArray2[1], this.fileLocations, this.lockUp, this.completeLock, this.securityNavigationLock.equalsIgnoreCase("File Locations"), this.web);
                            if (vps.length() == 0) {
                                scdesc.add(tempArray2[0]);
                                this.logger.debug("Desc " + tempArray2[0] + " added.");
                                sca.add(tempArray2[1]);
                                this.logger.debug("ShortCut " + tempArray2[1] + " added.");
                                continue;
                            }
                            this.logger.debug("ShortCut " + tempArray2[1] + " skipped.");
                            continue;
                        }
                        vps = this.validPath(tempArray1[iIndex], tempArray1[iIndex], this.fileLocations, this.lockUp, this.completeLock, this.securityNavigationLock.equalsIgnoreCase("File Locations"), this.web);
                        if (vps.length() != 0) continue;
                        scdesc.add(tempArray1[iIndex]);
                        this.logger.debug("Desc " + tempArray1[iIndex] + " added.");
                        sca.add(tempArray1[iIndex]);
                        this.logger.debug("ShortCut " + tempArray1[iIndex] + " added.");
                    }
                    if (scdesc.size() > 0) {
                        this.shortCutDescsArray = scdesc.toArray(new String[scdesc.size()]);
                        this.shortCutsArray = sca.toArray(new String[sca.size()]);
                    } else {
                        this.shortCutDescsArray = null;
                        this.shortCutsArray = null;
                    }
                } else {
                    this.logger.debug("No shortcuts provided...");
                    this.shortCutDescsArray = null;
                    this.shortCutsArray = null;
                }
            } else {
                this.logger.debug("No shortcuts allowed in web mode.");
                this.shortCutDescsArray = null;
                this.shortCutsArray = null;
            }
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
                this.sortDir = DEFAULTSORTDIR;
            } else {
                this.sortDir = temp;
            }
            this.logger.debug("sortDir = " + this.sortDir);
        } else {
            this.logger.error("No PageContext provided.");
        }
        return theReturn;
    }

    public String getFunction() {
        this.logger.debug("getFunction called...");
        return this.functionText;
    }

    public String getButtonText() {
        this.logger.debug("getButtonText called...");
        return this.buttonText;
    }

    public String getTitle() {
        this.logger.debug("getTitle called...");
        return this.title;
    }

    public static boolean isZipPath(String thePath) {
        String theFile = thePath.toLowerCase();
        boolean theReturn = theFile.indexOf(".zip/") > -1 || theFile.indexOf(".zip\\") > -1 || theFile.indexOf(".jar/") > -1 || theFile.indexOf(".jar\\") > -1 || theFile.indexOf(".war/") > -1 || theFile.indexOf(".war\\") > -1;
        return theReturn;
    }

    private String getSpace(int number) {
        String out = "";
        for (int i = 0; i < number; ++i) {
            out = out + "&nbsp;";
        }
        return out;
    }

    private void renderNormalFolderOptions(StringBuffer theBuffer, String thePath, String theRoot, boolean theLockUp) {
        this.logger.debug("renderNormalFolderOptions called...");
        Vector<String> locationsVector = new Vector<String>();
        File fileObject = new File(thePath);
        File rootFileObject = new File(theRoot);
        if (fileObject.exists() && rootFileObject.exists()) {
            String start = fileObject.getPath();
            String escStart = FileSystem.getEscapedPath(start);
            if (!theLockUp) {
                boolean found = false;
                File[] roots = File.listRoots();
                for (int i = roots.length - 1; i > -1; --i) {
                    File root = roots[i];
                    if (!root.exists()) continue;
                    String path = root.getPath();
                    if (start.toLowerCase().startsWith(path.toLowerCase())) {
                        if (!start.equalsIgnoreCase(path)) {
                            found = true;
                            Vector<String> tmp = new Vector<String>();
                            tmp.add(escStart);
                            if (fileObject != null) {
                                while ((fileObject = fileObject.getParentFile()) != null) {
                                    if (fileObject.exists()) {
                                        tmp.add(FileSystem.getEscapedPath(fileObject.getPath()));
                                        continue;
                                    }
                                    this.logger.warn("Parent file does not exist.");
                                    break;
                                }
                            }
                            for (int k = 0; k < tmp.size(); ++k) {
                                locationsVector.add(this.getSpace(tmp.size() - k - 1) + tmp.get(k));
                            }
                            continue;
                        }
                        found = true;
                        locationsVector.add(FileSystem.getEscapedPath(path));
                        continue;
                    }
                    locationsVector.add(FileSystem.getEscapedPath(path));
                }
                if (!found) {
                    Vector<String> tmp = new Vector<String>();
                    tmp.add(escStart);
                    while ((fileObject = fileObject.getParentFile()) != null) {
                        if (fileObject.exists()) {
                            tmp.add(FileSystem.getEscapedPath(fileObject.getPath()));
                            continue;
                        }
                        this.logger.warn("Parent file does not exist.");
                        break;
                    }
                    for (int k = 0; k < tmp.size(); ++k) {
                        locationsVector.add(this.getSpace(tmp.size() - k - 1) + tmp.get(k));
                    }
                }
            } else {
                Vector<String> tmp = new Vector<String>();
                tmp.add(escStart);
                while ((fileObject = fileObject.getParentFile()) != null) {
                    if (fileObject.exists()) {
                        if (this.isPathAboveRoot(theRoot, fileObject.getPath())) {
                            this.logger.debug("Lock up on and parent reached thus break.");
                            break;
                        }
                        tmp.add(FileSystem.getEscapedPath(fileObject.getPath()));
                        continue;
                    }
                    this.logger.warn("Parent file does not exist.");
                    break;
                }
                for (int k = 0; k < tmp.size(); ++k) {
                    locationsVector.add(this.getSpace(tmp.size() - k - 1) + tmp.get(k));
                }
            }
            for (int index = locationsVector.size() - 1; index > -1; --index) {
                String text = (String)locationsVector.get(index);
                String value = StringUtil.replaceAll((String)locationsVector.get(index), "&nbsp;", "", false);
                if (value.equals(escStart)) {
                    theBuffer.append("<option selected value=\"").append(value).append("\" title=\"").append(value).append("\">").append(text).append("</option>");
                    continue;
                }
                theBuffer.append("<option value=\"").append(value).append("\" title=\"").append(value).append("\">").append(text).append("</option>");
            }
        } else {
            if (fileObject.getPath().startsWith("\\\\") && fileObject.getPath().lastIndexOf("\\") == 1) {
                theBuffer.append("<option value='' selected>Network Host Root Inaccessible</option>");
                this.logger.error(">Root network host path like " + thePath + " cannot be viewed.");
            } else {
                theBuffer.append("<option value='' selected>").append("Invalid Folder").append("</option>");
                this.logger.error("Folder " + theRoot + " or " + thePath + " does not exist.");
            }
            File[] roots = File.listRoots();
            for (int i = roots.length - 1; i > -1; --i) {
                File root = roots[i];
                if (!root.exists()) continue;
                String path = root.getPath();
                String escpath = FileSystem.getEscapedPath(path);
                theBuffer.append("<option value=\"").append(path).append("\" title=\"").append(escpath).append("\">").append(escpath).append("</option>");
            }
        }
    }

    public static int getZipPoint(String thePath) {
        int temp = thePath.toLowerCase().lastIndexOf(".zip/");
        if (temp < 0 && (temp = thePath.lastIndexOf(".zip\\")) < 0 && (temp = thePath.lastIndexOf(".war\\")) < 0 && (temp = thePath.lastIndexOf(".war/")) < 0 && (temp = thePath.lastIndexOf(".jar/")) < 0) {
            temp = thePath.lastIndexOf(".jar\\");
        }
        if (temp > -1) {
            temp += 4;
        }
        return temp;
    }

    private void renderZipFolderOptions(StringBuffer theBuffer, String thePath, String theRoot, boolean theLockUp) {
        this.logger.debug("renderZipFolderOptions called...");
        int temp = FileSystem.getZipPoint(thePath);
        String normPath = thePath.substring(0, temp);
        this.logger.debug("normPath = " + normPath);
        String theESPath = FileSystem.getEscapedPath(thePath);
        File zipFileObject = new File(normPath);
        if (zipFileObject.exists()) {
            Vector<String> locationsVector = new Vector<String>();
            if (!theLockUp) {
                File[] roots = File.listRoots();
                for (int i = roots.length - 1; i > -1; --i) {
                    File root = roots[i];
                    if (!root.exists()) continue;
                    String path = FileSystem.getEscapedPath(root.getPath());
                    if (normPath.toLowerCase().startsWith(path.toLowerCase())) {
                        if (!normPath.equalsIgnoreCase(path)) {
                            String theHisPath = "";
                            String[] thePathArray = theESPath.split("/");
                            String delim = "/";
                            Vector<String> tmp = new Vector<String>();
                            for (int index = 0; index < thePathArray.length; ++index) {
                                theHisPath = index == 0 ? theHisPath + thePathArray[index] + delim : (index == 1 ? theHisPath + thePathArray[index] : theHisPath + delim + thePathArray[index]);
                                tmp.add(theHisPath);
                            }
                            for (int k = tmp.size() - 1; k > -1; --k) {
                                locationsVector.add(this.getSpace(k) + tmp.get(k));
                            }
                            continue;
                        }
                        locationsVector.add(path);
                        continue;
                    }
                    locationsVector.add(path);
                }
            } else {
                String theHisPath = "";
                String[] thePathArray = theESPath.split("/");
                Vector<String> tmp = new Vector<String>();
                for (int index = 0; index < thePathArray.length; ++index) {
                    if (this.isPathAboveRoot(theRoot, theHisPath)) {
                        this.logger.debug("Lock up on and parent reached thus dont add.");
                        continue;
                    }
                    tmp.add(theHisPath);
                }
                for (int k = tmp.size() - 1; k > -1; --k) {
                    locationsVector.add(this.getSpace(k) + tmp.get(k));
                }
            }
            for (int index = locationsVector.size() - 1; index > -1; --index) {
                String text = (String)locationsVector.get(index);
                String value = StringUtil.replaceAll((String)locationsVector.get(index), "&nbsp;", "", false);
                if (value.equals(theESPath)) {
                    theBuffer.append("<option selected value=\"").append(value).append("\" title=\"").append(value).append("\">").append(text).append("</option>");
                    continue;
                }
                theBuffer.append("<option value=\"").append(value).append("\" title=\"").append(value).append("\">").append(text).append("</option>");
            }
        } else {
            theBuffer.append("<option value=''>").append("Invalid Folder").append("</option>");
            this.logger.error("Folder " + theRoot + " or " + thePath + " does not exist.");
        }
    }

    private void renderFolderOptions(StringBuffer theBuffer, String thePath, String theRoot, boolean theLockUp, boolean theUseZip) {
        this.logger.debug("renderFolderOptions called...");
        if (theUseZip && FileSystem.isZipPath(thePath)) {
            this.renderZipFolderOptions(theBuffer, thePath, theRoot, theLockUp);
        } else {
            this.renderNormalFolderOptions(theBuffer, thePath, theRoot, theLockUp);
        }
    }

    private void renderHeader(StringBuffer theBuffer, String thePath, String theRoot, String theFieldId, boolean theRelative, boolean theForceSub, boolean theValidate, boolean theUseZip, boolean theWeb, boolean theFolders, boolean theLockUp, boolean theCompleteLock) {
        theBuffer.append("<link href=\"" + HttpUtil.getCSS("WEB-CORE/utils/lookup/stylesheets/filesystem.css", this.pageContext) + "\" rel=\"stylesheet\" type=\"text/css\">\n");
        theBuffer.append("<script type=\"text/javascript\" src=\"WEB-CORE/utils/lookup/scripts/filesystem.js\"></script>\n");
        theBuffer.append("<script type=\"text/javascript\">\n");
        theBuffer.append("document.body.style.overflow = 'hidden';\n");
        theBuffer.append("sapphire.events.attachEvent( window, 'onload', fileSystem.doOnLoad );\n");
        theBuffer.append("sapphire.events.attachEvent( document, 'onkeypress', fileSystem.doKeydown );\n");
        theBuffer.append("window.onmousewheel = document.onmousewheel = fileSystem.doMouseWheel;\n");
        if (theUseZip) {
            theBuffer.append("fileSystem.lUseZip = true;\n");
            theBuffer.append("fileSystem.useZip = true;\n");
        }
        if (theCompleteLock) {
            theBuffer.append("fileSystem.lCompleteLock = true;\n");
            theBuffer.append("fileSystem.completeLock = true;\n");
        }
        if (!theValidate) {
            theBuffer.append("fileSystem.lValidate = false;\n");
            theBuffer.append("fileSystem.validate = false;\n");
        }
        if (theLockUp) {
            theBuffer.append("fileSystem.lLockUp  = true;\n");
            theBuffer.append("fileSystem.lockUp  = true;\n");
        }
        if (theFolders) {
            theBuffer.append("fileSystem.lFolders  = true;\n");
        }
        if (theForceSub && FileSystem.getEscapedPath(thePath).equalsIgnoreCase(FileSystem.getEscapedPath(theRoot))) {
            theBuffer.append("fileSystem.lForceSub  = true;\n");
            theBuffer.append("fileSystem.forceSub  = true;\n");
        }
        if (theRelative) {
            if (theWeb) {
                theBuffer.append("fileSystem.sRelativePath  = '").append(this.getRelativePath(thePath, theRoot, true)).append("';");
                theBuffer.append("fileSystem.iReturnType  = 2;\n");
            } else {
                theBuffer.append("fileSystem.sRelativePath  = '").append(this.getRelativePath(thePath, theRoot, false)).append("';");
                theBuffer.append("fileSystem.iReturnType  = 1;\n");
            }
        }
        if (this.fieldId != null && this.fieldId.length() > 0) {
            theBuffer.append("fileSystem.sFieldId  = '").append(theFieldId).append("';\n");
        }
        theBuffer.append("</script>\n");
    }

    private void renderHTML(StringBuffer theBuffer, String theFunctionText, String theButtonText, String[] theExtArray, String[] theDescArray, String thePath, String theRoot, String theTitleText, boolean theFolders, boolean theLockUp, boolean theCompleteLock, String theFile, String theActiveExt, String[] theShortCutsArray, String[] theShortCutDescsArray, String theFieldId, boolean theRelative, boolean theForceSub, boolean theValidate, boolean theUseZip, boolean theWeb, int theSortCol, String theSortDir, String theFileLocationType, PropertyListCollection theFileLocations) {
        int index;
        boolean sidebar;
        File fileObject;
        this.logger.debug("renderHTML called...");
        theBuffer.append("<title>").append(this.getTitle()).append("</title>");
        TranslationProcessor tp = this.getTranslationProcessor();
        File rootFileObject = new File(theRoot);
        if (!rootFileObject.exists()) {
            try {
                theRoot = Configuration.getInstance().getSapphireHome();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (!(fileObject = new File(thePath)).exists()) {
            thePath = theRoot;
        }
        this.renderHeader(theBuffer, thePath, theRoot, theFieldId, theRelative, theForceSub, theValidate, theUseZip, theWeb, theFolders, theLockUp, theCompleteLock);
        theBuffer.append("<table id=oRootTable border=0 style=\"width:100%;height:100%;\" class=\"roottable\" border=0 cellpadding=0 cellspacing=0> ");
        theBuffer.append("<tr height=30> ");
        theBuffer.append("<td width=80 align=\"right\" style=\"padding-bottom:10px;\"> ");
        theBuffer.append(tp.translate(theFunctionText)).append(":");
        theBuffer.append("</td> ");
        theBuffer.append(" <td width=\"10\">&nbsp;</td> ");
        theBuffer.append("<td style='padding-bottom:10px;'> ");
        if (!theCompleteLock && this.securityShowPath) {
            theBuffer.append("<select id=oFolderSelect style=\"width:80%;\"  onchange=\"fileSystem.doFolderChange();\">");
            this.renderFolderOptions(theBuffer, thePath, theRoot, theLockUp, theUseZip);
            theBuffer.append("</select>");
        } else {
            theBuffer.append("<select id=oFolderSelect style=\"width:80%;\" disabled>");
            String sroot = "";
            if (!this.securityShowPath) {
                try {
                    File fRoot = new File(thePath);
                    sroot = fRoot.getCanonicalFile().getName();
                }
                catch (Exception e) {
                    sroot = thePath;
                }
            } else {
                sroot = theRoot;
            }
            theBuffer.append("<option selected value=\"").append(theCompleteLock ? theRoot : thePath).append("\">").append(sroot).append("</option>");
            theBuffer.append("</select>");
        }
        theBuffer.append(" &nbsp; ");
        if (!theCompleteLock) {
            String parent = this.getParentPath(thePath, theUseZip);
            if (parent == null || parent.length() == 0) {
                theBuffer.append("<img width=16 height=16 src=\"").append(MOVEUPDISABLEDIMAGE).append("\" title=\"Up one level\" style=\"cursor: pointer;\" >&nbsp;");
            } else if (theLockUp && this.isPathAboveRoot(theRoot, parent)) {
                theBuffer.append("<img width=16 height=16 src=\"").append(MOVEUPDISABLEDIMAGE).append("\" title=\"Up one level\" style=\"cursor: pointer;\" >&nbsp;");
            } else {
                theBuffer.append("<img width=16 height=16 src=\"").append(MOVEUPIMAGE).append("\" title=\"Up one level\" style=\"cursor: pointer;\" onclick=\"fileSystem.navigateTo('").append(FileSystem.getEscapedPath(parent)).append("','');\">&nbsp;");
            }
        }
        theBuffer.append("</td>");
        theBuffer.append("</tr>");
        theBuffer.append("<tr>");
        boolean bl = sidebar = (theShortCutsArray != null && theShortCutsArray.length > 0 || theFileLocations != null && theFileLocations.size() > 0) && !this.securityNavigationLock.equalsIgnoreCase("full lock") && !this.securityNavigationLock.equalsIgnoreCase("lock up") && this.securityAllowRoot;
        if (sidebar && !theRelative) {
            theBuffer.append("<td width=80 class=\"shortcutbar\" align=center valign=top>");
            theBuffer.append("<div class=\"shortcutbardiv\">");
            if (theFileLocations != null && theFileLocations.size() > 0) {
                String defaultP = this.getDefaultFileLocationPath(this.fileLocations, this.web);
                if (defaultP.indexOf("[") > -1 && defaultP.indexOf("]") > -1) {
                    defaultP = FileSystem.getFileLocation(defaultP);
                }
                for (index = 0; index < theFileLocations.size(); ++index) {
                    String desc;
                    PropertyList fileLocation = theFileLocations.getPropertyList(index);
                    String value = fileLocation.getProperty(FILELOCATIONS_LOCATION_PROPERTY, "");
                    if (value.length() <= 0 || !FileSystem.validShortcutPath(value, this.fileLocations, this.web, this.pageContext, this.logger)) continue;
                    value = FileSystem.getFileLocation(value);
                    String title = fileLocation.getProperty("title", "");
                    if (title.length() == 0) {
                        String string = title = defaultP.equalsIgnoreCase(value) ? this.getTranslationProcessor().translate("Default Location") : this.getTranslationProcessor().translate("Location") + " " + (index + 1);
                    }
                    if ((desc = fileLocation.getProperty(FILELOCATIONS_DESCRIPTION_PROPERTY, "")).length() == 0) {
                        if (!this.securityShowPath) {
                            try {
                                File f = new File(value);
                                desc = f.getCanonicalFile().getName();
                            }
                            catch (Exception e) {
                                desc = title;
                            }
                        } else {
                            desc = value;
                        }
                    }
                    String image = index == 0 ? fileLocation.getProperty(FILELOCATIONS_IMAGE_PROPERTY, COMPUTERPLACEIMAGE) : fileLocation.getProperty(FILELOCATIONS_IMAGE_PROPERTY, MYDOCUMENTSIMAGE);
                    theBuffer.append("<a class=sidebaritem href=\"javascript:fileSystem.changeRoot('").append(FileSystem.getEscapedPath(value)).append("','')\" title=\"").append(title).append(" - ").append(desc).append("\"><img src=\"").append(image).append("\" width=48 height=48 border=0><br>").append(title).append("</a><p>");
                }
            }
            this.logger.debug("About to render shortcuts...");
            if (!(theShortCutsArray == null || theCompleteLock || theLockUp || theRelative)) {
                for (index = 0; index < theShortCutsArray.length; ++index) {
                    String value = FileSystem.getFileLocation(theShortCutsArray[index]);
                    theBuffer.append("<a class=sidebaritem href=\"javascript:fileSystem.changeRoot('").append(FileSystem.getEscapedPath(value)).append("','')\" title=\"").append(value).append("\"><img src=\"").append(MYDOCUMENTSIMAGE).append("\" width=48 height=48 border=0><br>").append(value).append("</a><p>");
                }
            }
            this.logger.debug("Shortcuts rendered.");
            theBuffer.append("</div>");
            theBuffer.append("</td>");
            theBuffer.append(" <td width=10>&nbsp;</td>");
            theBuffer.append("<td>");
        } else {
            theBuffer.append("<td colspan=3>");
        }
        theBuffer.append(" <table border=0 cellpadding=0 cellspacing=0 class=\"insidetable\" width=\"100%\" height=\"100%\">");
        theBuffer.append("<tr height=\"100%\">");
        theBuffer.append("<td colspan=3 style=\"overflow:hidden;padding-right:2px;\">");
        theBuffer.append("<div id=\"oFileFrameLoading\" style=\"background-color:white;position:relative;top:100px;left:190px;\">Loading...<img src=\"WEB-CORE/utils/lookup/images/loading.gif\"></div>");
        theBuffer.append("<iframe scrolling=\"no\" class=\"frame\" frameborder=0 src=\"").append(this.browser.getBlankSrc()).append("\" style=\"width:100%;height:100%;\" id=oFileFrame name=oFileFrame></iframe>");
        theBuffer.append("</td>");
        theBuffer.append("</tr>");
        theBuffer.append("<tr height=30 style=\"padding-top:10px;\">");
        theBuffer.append("<td width=\"25%\"><span class=\"pad\">" + tp.translate("File name:") + "</span></td>");
        theBuffer.append("<td width=\"65%\" valign=middle><input type=text id=oFileName name=oFileName style=\"width:80%;\" value=\"").append(theFile).append("\"></td>");
        theBuffer.append("<td width=\"10%\" align=right><input style=\"width:80px;").append(this.embedded ? "display:none;" : "").append("\" type=button id=oReturnButton name=oReturnButton value=\"").append(tp.translate(theButtonText)).append("\" onclick=\"fileSystem.doOK();\"></td>");
        theBuffer.append("</tr>");
        theBuffer.append("<tr height=30 >   ");
        if (!theFolders) {
            theBuffer.append("<td width=\"25%\"><span class=\"pad\">" + tp.translate("Files of type:") + "</span></td>");
            theBuffer.append("<td width=\"65%\" valign=middle><select id=oFileExt name=oFileExt style=\"width:80%;\" onchange=\"fileSystem.doExtensionChange();\">");
            this.logger.debug("About to render extensions...");
            for (index = 0; index < theExtArray.length; ++index) {
                if (theExtArray[index].equalsIgnoreCase(theActiveExt)) {
                    theBuffer.append("<option selected value=\"").append(theExtArray[index]).append("\">").append(theDescArray[index]).append("</option>");
                    continue;
                }
                theBuffer.append("<option value=\"").append(theExtArray[index]).append("\">").append(theDescArray[index]).append("</option>");
            }
            this.logger.debug("Extensions rendered.");
            theBuffer.append("</select></td>");
        } else {
            theBuffer.append("<td width=\"25%\">&nbsp;</td>");
            theBuffer.append("<td width=\"65%\">&nbsp;</td>");
        }
        theBuffer.append("<td width=\"10%\" align=\"right\"><input style=\"width:80px;\" type=button id=oCancelButton name=oCancelButton value=\"" + tp.translate("Cancel") + "\" onclick=\"fileSystem.doCancel();\"></td> ");
        theBuffer.append("</tr> ");
        theBuffer.append("</table>");
        theBuffer.append("</td>");
        theBuffer.append("</tr>");
        theBuffer.append("</table>");
        theBuffer.append("<iframe frameborder=0 src='WEB-CORE/blank.html' width=1 height=1 id=oValidateFrame name=oValidateFrame style='display:none;'></iframe>");
        this.renderForms(theBuffer, theFunctionText, theButtonText, theTitleText, thePath, theRoot, theFolders, theLockUp, theCompleteLock, theDescArray, theExtArray, theActiveExt, theShortCutsArray, theShortCutDescsArray, theFieldId, theForceSub, theRelative, theUseZip, theWeb, theSortCol, theSortDir, theFileLocationType);
        theBuffer.append("<div id=\"image_preview\" memwidth=\"50\"></div>");
    }

    private String getRelativePath(String thePath, String theRoot, boolean missFirstDir) {
        String theReturn;
        this.logger.debug("getRelativePath called...");
        String aPath = FileSystem.getEscapedPath(thePath);
        String aRoot = FileSystem.getEscapedPath(theRoot);
        if (aPath.endsWith("/") && !aRoot.endsWith("/")) {
            aRoot = aRoot + "/";
        } else if (aRoot.endsWith("/") && !aPath.endsWith("/")) {
            aPath = aPath + "/";
        }
        this.logger.debug("aPath = " + aPath);
        this.logger.debug("aRoot = " + aRoot);
        if (aPath.length() > aRoot.length()) {
            this.logger.debug("Length of path greater than root so must be sub dir");
            theReturn = aPath.substring(aRoot.length());
            this.logger.debug("theReturn (1) = " + theReturn);
            if (theReturn.startsWith("/")) {
                theReturn = theReturn.substring(1);
            }
            this.logger.debug("theReturn (2) = " + theReturn);
            this.logger.debug("missFirstDir = " + missFirstDir);
            this.logger.debug("theReturn (3) = " + theReturn);
        } else if (aRoot.equals(aPath)) {
            this.logger.debug("Root equal to path ");
            theReturn = "";
        } else {
            this.logger.debug("Path is smaller than root so must be parent so return default of ''.");
            theReturn = "";
        }
        this.logger.debug("theReturn (4) = " + theReturn);
        return theReturn;
    }

    private boolean isPathAboveRoot(String theRoot, String thePath) {
        boolean theReturn;
        this.logger.debug("isPathAboveRoot called...");
        String aPath = FileSystem.getEscapedPath(thePath).toLowerCase();
        String aRoot = FileSystem.getEscapedPath(theRoot).toLowerCase();
        if (aPath.endsWith("/") && !aRoot.endsWith("/")) {
            aRoot = aRoot + "/";
        } else if (aRoot.endsWith("/") && !aPath.endsWith("/")) {
            aPath = aPath + "/";
        }
        this.logger.debug("aPath = " + aPath);
        this.logger.debug("aRoot = " + aRoot);
        if (aPath.length() > aRoot.length()) {
            this.logger.debug("Length of path greater than root so must be sub dir");
            theReturn = false;
        } else if (aRoot.equals(aPath)) {
            this.logger.debug("Root equal to path ");
            theReturn = false;
        } else {
            this.logger.debug("Path is smaller than root so must be parent.");
            theReturn = true;
        }
        this.logger.debug("theReturn = " + theReturn);
        return theReturn;
    }

    private String getParentPath(String thePath, boolean theUseZip) {
        String sReturn;
        block11: {
            this.logger.debug("getParentPath called...");
            sReturn = "";
            if (theUseZip && FileSystem.isZipPath(thePath)) {
                this.logger.debug("Use zip and zip path...");
                int iTemp = FileSystem.getZipPoint(thePath);
                String sNormPath = thePath.substring(0, iTemp);
                this.logger.debug("sNormPath (1) = " + sNormPath);
                File oFile = new File(sNormPath);
                if (oFile.exists()) {
                    sNormPath = FileSystem.getEscapedPath(thePath);
                    this.logger.debug("sNormPath (2) = " + sNormPath);
                    if (sNormPath.endsWith("/")) {
                        sNormPath = sNormPath.substring(0, sNormPath.length() - 1);
                    }
                    sReturn = sNormPath.substring(0, sNormPath.lastIndexOf("/"));
                } else {
                    this.logger.error("The zip file does not exist.");
                }
            } else {
                String sNormPath = thePath;
                File oFile = new File(sNormPath);
                if (oFile.exists()) {
                    String sParent = oFile.getParent();
                    this.logger.debug("sParent = " + sParent);
                    if (sParent != null && sParent.length() > 0) {
                        try {
                            oFile = oFile.getParentFile();
                            if (oFile.exists()) {
                                sReturn = oFile.getPath();
                                break block11;
                            }
                            this.logger.warn("Parent does not exist.");
                        }
                        catch (Exception e) {
                            this.logger.error("Could not obtain parent path.");
                        }
                    }
                } else {
                    this.logger.debug("No parent found.");
                }
            }
        }
        this.logger.debug("sReturn = " + sReturn);
        return sReturn;
    }

    public static String getEscapedPath(String org) {
        return StringUtil.replaceAll(org, "\\", "/");
    }

    private void renderForms(StringBuffer theBuffer, String theFunctionText, String theButtonText, String theTitleText, String thePath, String theRoot, boolean theFolders, boolean theLockUp, boolean theCompleteLock, String[] theDescArray, String[] theExtArray, String theActiveExt, String[] theShortCutsArray, String[] theShortCutDescsArray, String theFieldId, boolean theForceSub, boolean theRelative, boolean theUseZip, boolean theWeb, int theSortCol, String theSortDir, String theFileLocationType) {
        int index;
        this.logger.debug("renderForms called...");
        theBuffer.append("<form id=refreshform name=refreshform method=post style='display:none;' action='rc?command=file&file=WEB-CORE/utils/lookup/filesystem.jsp'>");
        theBuffer.append("<input type=hidden name='").append(FUNCTIONTEXTPROP).append("' value='").append(theFunctionText).append("'>");
        theBuffer.append("<input type=hidden name='").append("title").append("' value='").append(theTitleText).append("'>");
        theBuffer.append("<input type=hidden name='").append(BUTTONTEXTPROP).append("' value='").append(theButtonText).append("'>");
        StringBuffer extBuffer = new StringBuffer();
        if (theExtArray != null) {
            for (index = 0; index < theDescArray.length; ++index) {
                MiscUtil.MiscString.appendDelimeteredString(extBuffer, theDescArray[index] + "|" + theExtArray[index], ";");
            }
        } else {
            extBuffer.append("");
        }
        if (extBuffer.length() > 0) {
            this.logger.debug("extBuffer = " + extBuffer.toString());
        }
        theBuffer.append("<input type=hidden name='").append(EXTENSIONSPROP).append("' value='").append(extBuffer).append("'>");
        theBuffer.append("<input type=hidden name='").append(ACTIVEEXTENSIONPROP).append("' value='").append(theActiveExt).append("'>");
        StringBuffer shortsBuffer = new StringBuffer();
        if (theShortCutsArray != null) {
            for (index = 0; index < theShortCutsArray.length; ++index) {
                MiscUtil.MiscString.appendDelimeteredString(shortsBuffer, theShortCutDescsArray[index] + "|" + theShortCutsArray[index], ";");
            }
        } else {
            shortsBuffer.append("");
        }
        if (shortsBuffer.length() > 0) {
            this.logger.debug("shortsBuffer = " + shortsBuffer.toString());
        }
        theBuffer.append("<input type=hidden name='").append(SHORTCUTSPROP).append("' value='").append(shortsBuffer).append("'>");
        theBuffer.append("<input type=hidden name='").append(FILELOCATIONTYPEPROP).append("' value='").append(theFileLocationType).append("'>");
        theBuffer.append("<input type=hidden name='").append(PATHPROP).append("' value='").append(thePath).append("'>");
        theBuffer.append("<input type=hidden name='").append(NAVIGATED).append("' value='").append("Y").append("'>");
        theBuffer.append("<input type=hidden name='").append(BASEDIRPROP).append("' value='").append(theRoot).append("'>");
        if (theFolders) {
            theBuffer.append("<input type=hidden name='").append(FOLDERSPROP).append("' value='Y'>");
        } else {
            theBuffer.append("<input type=hidden name='").append(FOLDERSPROP).append("' value='N'>");
        }
        if (theLockUp) {
            theBuffer.append("<input type=hidden name='").append(LOCKUPPROP).append("' value='Y'>");
        } else {
            theBuffer.append("<input type=hidden name='").append(LOCKUPPROP).append("' value='N'>");
        }
        if (theCompleteLock) {
            theBuffer.append("<input type=hidden name='").append(DIRLOCKPROP).append("' value='Y'>");
        } else {
            theBuffer.append("<input type=hidden name='").append(DIRLOCKPROP).append("' value='N'>");
        }
        if (theRelative) {
            theBuffer.append("<input type=hidden name='").append(RELATIVEPROP).append("' value='Y'>");
        } else {
            theBuffer.append("<input type=hidden name='").append(RELATIVEPROP).append("' value='N'>");
        }
        if (theWeb) {
            theBuffer.append("<input type=hidden name='").append(WEBPROP).append("' value='Y'>");
        } else {
            theBuffer.append("<input type=hidden name='").append(WEBPROP).append("' value='N'>");
        }
        theBuffer.append("<input type=hidden name='").append(FIELDIDPROP).append("' value='").append(theFieldId).append("'>");
        if (theForceSub) {
            theBuffer.append("<input type=hidden name='").append(FORCESUBPROP).append("' value='Y'>");
        } else {
            theBuffer.append("<input type=hidden name='").append(FORCESUBPROP).append("' value='N'>");
        }
        if (theUseZip) {
            theBuffer.append("<input type=hidden name='").append(USEZIPPROP).append("' value='Y'>");
        }
        theBuffer.append("<input type=hidden name='").append(SORTCOLUMNPROP).append("' value='").append(theSortCol).append("'>");
        theBuffer.append("<input type=hidden name='").append(SORTDIRECTIONPROP).append("' value='").append(theSortDir).append("'>");
        theBuffer.append("</form>");
        theBuffer.append("<form id=fileform name=fileform method=post action='rc?command=file&file=WEB-CORE/utils/lookup/fileview.jsp' target='oFileFrame' style='display:none;'>");
        theBuffer.append("<input type=hidden name='").append("$t").append("' value='").append(this.getSecurityToken()).append("'>");
        theBuffer.append("<input type=hidden name='").append(ACTIVEEXTENSIONPROP).append("' value='").append(theActiveExt).append("'>");
        theBuffer.append("<input type=hidden name='").append(PATHPROP).append("' value='").append(thePath).append("'>");
        if (theFolders || theForceSub && FileSystem.getEscapedPath(thePath).equalsIgnoreCase(FileSystem.getEscapedPath(theRoot))) {
            theBuffer.append("<input type=hidden name='").append(FOLDERSPROP).append("' value='Y'>");
        } else {
            theBuffer.append("<input type=hidden name='").append(FOLDERSPROP).append("' value='N'>");
        }
        if (theCompleteLock) {
            theBuffer.append("<input type=hidden name='").append("lockdown").append("' value='Y'>");
        } else {
            theBuffer.append("<input type=hidden name='").append("lockdown").append("' value='N'>");
        }
        if (theUseZip) {
            theBuffer.append("<input type=hidden name='").append(USEZIPPROP).append("' value='Y'>");
        }
        theBuffer.append("<input type=hidden name='").append(SORTCOLUMNPROP).append("' value='").append(theSortCol).append("'>");
        theBuffer.append("<input type=hidden name='").append(SORTDIRECTIONPROP).append("' value='").append(theSortDir).append("'>");
        theBuffer.append("</form>");
        theBuffer.append("<form id=validateform name=validateform method=post action='rc?command=file&file=WEB-CORE/utils/lookup/filevalidate.jsp' target='oValidateFrame' style='display:none;'>");
        theBuffer.append("<input type=hidden name='").append(PATHPROP).append("' value=''>");
        theBuffer.append("<input type=hidden name='").append("returnpath").append("' value=''>");
        if (theUseZip) {
            theBuffer.append("<input type=hidden name='").append(USEZIPPROP).append("' value='Y'>");
        }
        theBuffer.append("</form>");
    }

    @Override
    public String getHtml() {
        this.logger.debug("getHTMML called...");
        String theReturn = "";
        if (this.pageContext != null) {
            if (FileSystem.isServerSideBrowsingPermitted(this.connectionInfo.getConnectionId())) {
                if (this.loadProperties()) {
                    this.renderHTML(this.theHTMLBuffer, this.functionText, this.buttonText, this.extensionsArray, this.descsArray, this.path, this.rootPath, this.title, this.foldersOnly, this.lockUp, this.completeLock, this.file, this.activeExt, this.shortCutsArray, this.shortCutDescsArray, this.fieldId, this.relative, this.forceSub, this.validate, this.useZip, this.web, this.sortCol, this.sortDir, this.fileLocationType, this.fileLocations);
                    this.logger.debug("HTML rendered.");
                } else {
                    this.logger.error("Could not load required properties.");
                }
            } else {
                this.theHTMLBuffer.append("<span style=\"color:red;\">").append(this.getTranslationProcessor().translate("Serverside browsing is disabled. Please contact your system admistrator.")).append("</span>");
                this.logger.error("Serverside browsing disabled.");
            }
        } else {
            this.logger.error("PageContext has not been set.");
        }
        if (this.debugErrorMsg == null || this.debugErrorMsg.length() == 0) {
            if (this.theHTMLBuffer.length() > 0) {
                theReturn = this.theHTMLBuffer.toString();
            }
        } else {
            theReturn = this.getError();
        }
        return theReturn;
    }
}

