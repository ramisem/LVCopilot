/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.SapphireSaxHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.SapphireException;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PatchXML {
    private File patchXML;
    private ArrayList<Patch> patches = new ArrayList();

    public PatchXML(File patchXML) {
        this.patchXML = patchXML;
    }

    public File getFile() {
        return this.patchXML;
    }

    public void setPatchXML(File patchXML) {
        this.patchXML = patchXML;
    }

    public Patch getPatchInstance(String type, String patchid, String version, String patchfile, String desc, Calendar timestamp) {
        Patch patch = new Patch(type, patchfile, patchid, version, desc);
        patch.setCreatedt(timestamp);
        return patch;
    }

    public PatchFile getPatchFileInstance(String target, String jar, String file, String type, String path, String desc, String action, String build, String dbms, String platform) {
        return new PatchFile(target, jar, file, type, path, desc, action, build, dbms, platform);
    }

    public void addPatch(Patch patch) {
        this.patches.add(patch);
    }

    public ArrayList<Patch> getPatches() {
        return this.patches;
    }

    public ArrayList<Patch> load() throws SapphireException {
        PatchXMLHandler handler = new PatchXMLHandler();
        handler.setPrintStream(null);
        handler.setXMLFile(this.patchXML);
        SaxUtil.parseFile(handler);
        return this.patches;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void save() {
        FileOutputStream fos = null;
        PrintWriter out = null;
        try {
            fos = new FileOutputStream(this.patchXML);
            out = new PrintWriter(fos);
            out.println("<PATCHPACKAGE>");
            for (int i = 0; i < this.patches.size(); ++i) {
                Patch patch = this.patches.get(i);
                Calendar createdt = patch.getCreatedt();
                String createattr = "";
                if (createdt != null) {
                    createattr = " createdt=\"" + new SimpleDateFormat().format(createdt.getTime()) + "\"";
                }
                Calendar applydt = patch.getApplydt();
                String applyattr = "";
                if (applydt != null) {
                    applyattr = " applydt=\"" + new SimpleDateFormat().format(applydt.getTime()) + "\"";
                }
                String applyapp = patch.getApplyApplicationid();
                String applyappattr = "";
                if (applyapp != null) {
                    applyappattr = " applyapplicationid=\"" + applyapp + "\"";
                }
                String applydb = patch.getApplyDatabaseid();
                String applydbattr = "";
                if (applydb != null) {
                    applydbattr = " applydatabaseid=\"" + applydb + "\"";
                }
                out.println("    <PATCH patchid=\"" + patch.getPatchid() + "\" version=\"" + patch.getVersion() + "\" type=\"" + patch.getType() + "\" patchfile=\"" + patch.getPatchfile() + "\" description=\"" + HttpUtil.htmlEncode(patch.getDesc()) + "\"" + createattr + applyattr + applyappattr + applydbattr + ">");
                ArrayList<PatchFile> patchFiles = patch.getPatchFiles();
                for (int j = 0; j < patchFiles.size(); ++j) {
                    PatchFile patchFile = patchFiles.get(j);
                    out.println("        <PATCHFILE target=\"" + patchFile.getTarget() + "\" jar=\"" + patchFile.getJar() + "\" file=\"" + patchFile.getFile() + "\" type=\"" + patchFile.getType() + "\" path=\"" + patchFile.getPath() + "\" description=\"" + HttpUtil.htmlEncode(patchFile.getDesc()) + "\" action=\"" + patchFile.getAction() + "\" build=\"" + patchFile.getBuild() + "\" dbms=\"" + patchFile.getDbms() + "\" platform=\"" + patchFile.getPlatform() + "\"/>");
                }
                out.println("    </PATCH>");
            }
            out.println("</PATCHPACKAGE>");
        }
        catch (Exception e) {
            Trace.logError("Failed to save patch XML. Reason: " + e.getMessage(), e);
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
            catch (IOException iOException) {}
        }
    }

    private String encode(String s) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("\"", "&quot;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        return s;
    }

    public PropertyListCollection getPatchesCollection() {
        return this.getPatchesCollection(File.separatorChar);
    }

    public PropertyListCollection getPatchesCollection(char separator) {
        M18NUtil m18n = new M18NUtil();
        PropertyListCollection patchesCollection = new PropertyListCollection();
        for (int i = 0; i < this.patches.size(); ++i) {
            Patch patch = this.patches.get(i);
            PropertyList patchProperties = new PropertyList();
            patchProperties.setProperty("patchfile", patch.getPatchfile());
            patchProperties.setProperty("type", patch.getType());
            patchProperties.setProperty("patchid", patch.getPatchid());
            patchProperties.setProperty("version", patch.getVersion());
            patchProperties.setProperty("desc", patch.getDesc());
            patchProperties.setProperty("createdt", patch.getCreatedt() != null ? m18n.format(patch.getCreatedt()) : "");
            patchProperties.setProperty("applydt", patch.getApplydt() != null ? m18n.format(patch.getApplydt()) : "");
            patchProperties.put("compareCal", patch.getApplydt());
            patchProperties.setProperty("applyapplicationid", patch.getApplyApplicationid());
            patchProperties.setProperty("applydatabaseid", patch.getApplyDatabaseid());
            PropertyListCollection patchFilesCollection = new PropertyListCollection();
            patchProperties.setProperty("patchfiles", patchFilesCollection);
            ArrayList<PatchFile> patchFiles = patch.getPatchFiles();
            for (int j = 0; j < patchFiles.size(); ++j) {
                PatchFile patchFile = patchFiles.get(j);
                PropertyList patchFileProperties = new PropertyList();
                patchFileProperties.setProperty("target", patchFile.getTarget());
                patchFileProperties.setProperty("dbms", patchFile.getDbms());
                patchFileProperties.setProperty("platform", patchFile.getPlatform());
                patchFileProperties.setProperty("jar", patchFile.getJar());
                patchFileProperties.setProperty("file", patchFile.getFile(separator));
                patchFileProperties.setProperty("type", patchFile.getType());
                patchFileProperties.setProperty("path", patchFile.getPath(separator));
                patchFileProperties.setProperty("desc", patchFile.getDesc());
                patchFileProperties.setProperty("action", patchFile.getAction());
                patchFileProperties.setProperty("build", patchFile.getBuild());
                patchFilesCollection.add(patchFileProperties);
            }
            patchesCollection.add(patchProperties);
        }
        Collections.sort(patchesCollection, new Comparator(){

            public int compare(Object o1, Object o2) {
                Calendar appydt1 = (Calendar)((PropertyList)o1).get("compareCal");
                Calendar appydt2 = (Calendar)((PropertyList)o2).get("compareCal");
                if (appydt1 == null && appydt2 == null) {
                    return 0;
                }
                if (appydt1 == null && appydt2 != null) {
                    return 1;
                }
                if (appydt1 != null && appydt2 == null) {
                    return -1;
                }
                return appydt2.compareTo(appydt1);
            }
        });
        return patchesCollection;
    }

    public class PatchFile {
        public static final String TARGET_APP = "app";
        public static final String TARGET_EAR = "ear";
        public static final String TARGET_JAR = "jar";
        public static final String TARGET_WAR = "war";
        public static final String TARGET_NONE = "none";
        public static final String TARGET_LVH = "lvh";
        public static final String TARGET_APPHOME = "apphome";
        public static final String TARGET_DB = "database";
        public static final String TARGET_WEBXML = "webxml";
        public static final String TARGET_CONSOLE = "console";
        public static final String ACTION_NONE = "none";
        public static final String ACTION_ADD = "add";
        public static final String ACTION_REPLACE = "replace";
        public static final String ACTION_BACKUP_REPLACE = "backupreplace";
        public static final String ACTION_DELETE = "delete";
        public static final String ACTION_BACKUP_DELETE = "backupdelete";
        public static final String ACTION_EXECUTECTT = "executectt";
        public static final String ACTION_EXECUTESQL = "executesql";
        public static final String ACTION_EXECUTEINST = "executeinst";
        public static final String ACTION_RUNANT = "runant";
        public static final String ACTION_EXTRACT = "extract";
        public static final String TYPE_FILE = "F";
        public static final String TYPE_DIR = "D";
        private String target;
        private String jar;
        private String file;
        private String type;
        private String dbms;
        private String platform;
        private String path;
        private String desc;
        private String action;
        private String build;

        public PatchFile(String target, String jar, String file, String type, String path, String desc, String action, String build, String dbms, String platform) {
            this.target = target;
            this.jar = jar;
            this.file = file;
            this.type = type;
            this.path = path;
            this.desc = desc;
            this.action = action;
            this.build = build;
            this.dbms = dbms;
            this.platform = platform;
        }

        public String getTarget() {
            return this.target != null && this.target.length() > 0 ? this.target : "";
        }

        public String getJar() {
            return this.jar != null && this.jar.length() > 0 ? this.jar : "";
        }

        public String getFile() {
            return this.file != null && this.file.length() > 0 ? this.file.replace('\\', '/') : "";
        }

        public String getFile(char separator) {
            return this.file != null && this.file.length() > 0 ? this.file.replace('\\', separator).replace('/', separator) : "";
        }

        public String getType() {
            return this.type != null && this.type.length() > 0 ? this.type : TYPE_FILE;
        }

        public String getPath() {
            return this.path != null && this.path.length() > 0 ? this.path.replace('\\', '/') : "";
        }

        public String getPath(char separator) {
            return this.path != null && this.path.length() > 0 ? this.path.replace('\\', separator).replace('/', separator) : "";
        }

        public String getDesc() {
            return this.desc != null ? this.desc : "";
        }

        public String getAction() {
            return this.action != null && this.action.length() > 0 ? this.action : "none";
        }

        public String getBuild() {
            return this.build != null && this.build.length() > 0 ? this.build : "N";
        }

        public String toString() {
            return this.getFile();
        }

        public String getDbms() {
            return this.dbms != null ? this.dbms : "";
        }

        public String getPlatform() {
            return this.platform != null ? this.platform : "";
        }
    }

    public class Patch {
        private String patchfile;
        private String type;
        private String patchid;
        private String version;
        private String desc;
        private Calendar createdt;
        private Calendar applydt;
        private String applyApplicationid;
        private String applyDatabaseid;
        private ArrayList<PatchFile> patchFiles;

        public Patch(String type, String patchfile, String patchid, String version, String desc) {
            this.patchfile = patchfile;
            this.type = type;
            this.patchid = patchid;
            this.version = version;
            this.desc = desc;
            this.patchFiles = new ArrayList();
        }

        public String getPatchfile() {
            return this.patchfile;
        }

        public String getType() {
            return this.type;
        }

        public String getPatchid() {
            return this.patchid;
        }

        public String getVersion() {
            return this.version;
        }

        public String getDesc() {
            return this.desc;
        }

        public void setCreatedt(Calendar createdt) {
            this.createdt = createdt;
        }

        public Calendar getCreatedt() {
            return this.createdt;
        }

        public void setApplydt(Calendar applydt) {
            this.applydt = applydt;
        }

        public Calendar getApplydt() {
            return this.applydt;
        }

        public String getApplyApplicationid() {
            return this.applyApplicationid;
        }

        public void setApplyApplicationid(String applyApplicationid) {
            this.applyApplicationid = applyApplicationid;
        }

        public String getApplyDatabaseid() {
            return this.applyDatabaseid;
        }

        public void setApplyDatabaseid(String applyDatabaseid) {
            this.applyDatabaseid = applyDatabaseid;
        }

        public ArrayList<PatchFile> getPatchFiles() {
            return this.patchFiles;
        }

        public void addPatchFile(PatchFile patchFile) {
            this.patchFiles.add(patchFile);
        }
    }

    private class PatchXMLHandler
    extends SapphireSaxHandler {
        private StringBuffer currentElementChars = new StringBuffer();

        private PatchXMLHandler() {
        }

        @Override
        public void startDocument() throws SAXException {
            this.println("Processing patch file: " + this._xmlFile.getName() + "...");
        }

        @Override
        public void endDocument() throws SAXException {
            this.println("Finished processing patch file: " + this._xmlFile.getName());
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            this.currentElementChars.delete(0, this.currentElementChars.length());
            Properties attr = this.getAttributes(attributes);
            if (qName.equalsIgnoreCase("PATCHPACKAGE")) {
                this.println("Parsing PATCHPACKAGE");
            } else if (qName.equalsIgnoreCase("PATCH")) {
                DateTimeUtil dtu;
                Patch patch = new Patch(attr.getProperty("type", "P"), attr.getProperty("patchfile"), attr.getProperty("patchid"), attr.getProperty("version", "N/A"), attr.getProperty("description"));
                if (attr.getProperty("createdt") != null && attr.getProperty("createdt").length() > 0) {
                    dtu = new DateTimeUtil();
                    patch.setCreatedt(dtu.getCalendar(attr.getProperty("createdt")));
                }
                if (attr.getProperty("applydt") != null && attr.getProperty("applydt").length() > 0) {
                    dtu = new DateTimeUtil();
                    patch.setApplydt(dtu.getCalendar(attr.getProperty("applydt")));
                }
                if (attr.getProperty("applyapplicationid") != null && attr.getProperty("applyapplicationid").length() > 0) {
                    patch.setApplyApplicationid(attr.getProperty("applyapplicationid"));
                }
                if (attr.getProperty("applydatabaseid") != null && attr.getProperty("applydatabaseid").length() > 0) {
                    patch.setApplyApplicationid(attr.getProperty("applydatabaseid"));
                }
                PatchXML.this.patches.add(patch);
                this.println("Parsing PATCH " + patch.patchid);
            } else if (qName.equalsIgnoreCase("PATCHFILE")) {
                PatchFile patchFile = new PatchFile(attr.getProperty("target"), attr.getProperty("jar"), attr.getProperty("file"), attr.getProperty("type"), attr.getProperty("path"), attr.getProperty("description"), attr.getProperty("action", "replace"), attr.getProperty("build", "N"), attr.getProperty("dbms", ""), attr.getProperty("platform", ""));
                ((Patch)PatchXML.this.patches.get(PatchXML.this.patches.size() - 1)).patchFiles.add(patchFile);
                this.println("Parsing PATCHFILE " + patchFile.file + ", Description: " + patchFile.desc);
            } else {
                throw new SAXException("Unrecognized element " + qName + " found in document " + this._xmlFile.getName());
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equalsIgnoreCase("PATCHPACKAGE")) {
                this.println("Parsed PATCHPACKAGE");
            } else if (qName.equalsIgnoreCase("PATCH")) {
                this.println("Parsed PATCH: " + ((Patch)PatchXML.this.patches.get(PatchXML.this.patches.size() - 1)).patchid);
            } else if (qName.equalsIgnoreCase("PATCHFILE")) {
                this.println("Parsed PATCHFILE");
            } else {
                throw new SAXException("Unrecognized element " + qName + " found in document " + this._xmlFile.getName());
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (this.currentElementChars != null) {
                this.currentElementChars.append(this.getCharacters(ch, start, length));
            }
        }
    }
}

