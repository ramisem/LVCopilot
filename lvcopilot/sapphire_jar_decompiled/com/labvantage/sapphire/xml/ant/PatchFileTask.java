/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.admin.install.PackagerFile;
import com.labvantage.sapphire.pageelements.attachment.BaseAttachmentType;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.xml.PatchXML;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class PatchFileTask
extends Task {
    ConnectionTask connection;
    String release;
    int patchbuildnumber = -1;
    boolean check = false;
    File rootdir;

    public void setCheck(boolean check) {
        this.check = check;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public void setPatchbuildnumber(int patchbuildnumber) {
        this.patchbuildnumber = patchbuildnumber;
    }

    public void setRootdir(File rootdir) {
        this.rootdir = rootdir;
    }

    public void execute() throws BuildException {
        if (this.connection == null) {
            throw new BuildException("Connection task not defined");
        }
        if (this.release == null || this.release.length() == 0) {
            throw new BuildException("Release not defined");
        }
        DBUtil dbu = this.connection.getConnection(true);
        try {
            String[] releases;
            File patchdir = new File(this.rootdir, "buildfiles/patch");
            FileUtil.deleteAll(patchdir);
            for (String release : releases = StringUtil.split(this.release, "+")) {
                this.log("Building patch file for '" + release + "'...");
                dbu.createPreparedResultSet("selectrelease", "SELECT u_releaseid, releasedesc, patchbuildnum FROM u_release WHERE u_releaseid = ?", release);
                if (dbu.getNext("selectrelease")) {
                    if (!this.check) {
                        int lengthread;
                        if (this.rootdir == null || !this.rootdir.exists()) {
                            throw new BuildException("Build root directory not defined!");
                        }
                        int patchbuildnum = this.patchbuildnumber > 0 ? this.patchbuildnumber : (dbu.getBigDecimal("selectrelease", "patchbuildnum") != null ? dbu.getInt("selectrelease", "patchbuildnum") + 1 : 1);
                        this.log("Patch build number: " + patchbuildnum);
                        File patchZipFile = new File(patchdir, release + "_" + patchbuildnum + ".zip");
                        patchZipFile.getParentFile().mkdirs();
                        PackagerFile patchZip = new PackagerFile(patchZipFile);
                        PatchXML patchXML = new PatchXML(new File(patchdir, "patch.xml"));
                        PatchXML.Patch patchXMLPatch = patchXML.getPatchInstance("P", release + "_" + patchbuildnum, String.valueOf(patchbuildnum), release + "_" + patchbuildnum + ".zip", dbu.getValue("selectrelease", "releasedesc"), DateTimeUtil.getNowCalendar());
                        patchXML.addPatch(patchXMLPatch);
                        dbu.createPreparedResultSet("releaseitems", "SELECT source, target, jar, filename, typeflag, description, action FROM u_releaseitem WHERE u_releaseid = ?", release);
                        DataSet releaseFiles = new DataSet(dbu.getResultSet("releaseitems"));
                        for (int i = 0; i < releaseFiles.size(); ++i) {
                            String source = releaseFiles.getValue(i, "source");
                            String filename = releaseFiles.getValue(i, "filename");
                            String jar = "";
                            if (source.equals("javaAPI") || source.equals("javaCore")) {
                                source = "java\\classes";
                                jar = "sapphire.jar";
                            } else if (source.equals("webRoot") || source.equals("webCore") || source.equals("gwt") || source.equals("webOpal")) {
                                source = "webapplication\\sapphireadmin";
                            } else if (source.equals("jar")) {
                                source = "deployment\\ear\\jars";
                            } else if (source.equals("lvh")) {
                                source = "buildfiles\\labvantagehome60";
                            } else if (source.equals("attachment")) {
                                try {
                                    Integer attachmentnum = new Integer(filename);
                                    dbu.createPreparedResultSet("attachment", "SELECT attachment, attachmentdesc, typeflag, filename, attachmentclob, sourcefilename FROM sdiattachment WHERE sdcid = 'Release' AND keyid1 = ? AND keyid2 = '(null)' AND keyid3 = '(null)' AND attachmentnum = ?", new Object[]{release, attachmentnum});
                                    if (!dbu.getNext("attachment")) {
                                        throw new Exception("Attachment record not found");
                                    }
                                    Attachment attachment = null;
                                    attachment = new Attachment();
                                    attachment.setBlob(dbu.getBlob("attachment", "attachment"));
                                    attachment.setFilename(dbu.getString("attachment", "filename"));
                                    attachment.setClob(dbu.getClob("attachment", "attachmentclob"));
                                    attachment.setType(dbu.getString("attachment", "typeflag"));
                                    attachment.setDescription(dbu.getString("attachment", "attachmentdesc"));
                                    attachment.setSourceFilename(dbu.getString("attachment", "sourcefilename"));
                                    BaseAttachmentType type = Attachment.getAttachmentType(attachment.getType());
                                    filename = attachment.getFilename() != null ? (attachment.getFilename().lastIndexOf("\\") >= 0 ? attachment.getFilename().substring(attachment.getFilename().lastIndexOf("\\") + 1) : attachment.getFilename()) : "Attachment" + attachmentnum;
                                    try {
                                        type.processGetAttachment(attachment, null);
                                        File attachmentFile = new File(patchdir, filename);
                                        FileOutputStream fos = new FileOutputStream(attachmentFile);
                                        fos.write(attachment.getData());
                                        fos.close();
                                    }
                                    catch (Exception e) {
                                        throw new BuildException("Failed to load attachmentnum '" + attachmentnum + "'! " + e.getMessage());
                                    }
                                    source = "buildfiles\\patch";
                                }
                                catch (Exception e) {
                                    throw new BuildException("Attachment reference invalid! " + e.getMessage());
                                }
                            }
                            File path = new File(this.rootdir, source);
                            File file = new File(path, filename);
                            if (!file.exists()) {
                                throw new BuildException("Patch file '" + file.getAbsolutePath() + "' not found!");
                            }
                            patchZip.addFile(releaseFiles.getValue(i, "target", "none").equals("none") ? "misc" : releaseFiles.getValue(i, "target"), path, file);
                            patchXMLPatch.addPatchFile(patchXML.getPatchFileInstance(releaseFiles.getValue(i, "target"), jar, filename, releaseFiles.getValue(i, "typeflag"), path.getAbsolutePath(), releaseFiles.getValue(i, "description"), releaseFiles.getValue(i, "action"), "N", null, null));
                        }
                        patchXML.save();
                        patchZip.addFile(patchdir, patchXML.getFile());
                        patchZip.save();
                        dbu.executePreparedUpdate("DELETE FROM sdiattachment WHERE sdcid = 'Release' AND keyid1 = ? AND keyid2 = '(null)' AND keyid3 = '(null)' AND attachmentdesc = 'Generated patch.zip from build process'", new Object[]{release});
                        dbu.createPreparedResultSet("max", "SELECT max( attachmentnum ) + 1 \"max\" FROM sdiattachment WHERE sdcid = 'Release' AND keyid1 = ? AND keyid2 = '(null)' AND keyid3 = '(null)'", new Object[]{release});
                        dbu.getNext("max");
                        int attachmentnum = Math.max(dbu.getInt("max", "max"), 1);
                        Timestamp now = DateTimeUtil.getNowTimestamp();
                        dbu.executePreparedUpdate("INSERT INTO sdiattachment ( sdcid, keyid1, keyid2, keyid3, filename, typeflag, attachmentdesc, attachmentnum, createdt, createby, createtool, moddt, modby, modtool ) values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ? )", new Object[]{"Release", release, "(null)", "(null)", patchZip.getFile().getAbsolutePath(), "F", "Generated patch.zip from build process", attachmentnum, now, "(system)", "Build", now, "(system)", "Build"});
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        FileInputStream fis = new FileInputStream(patchZip.getFile());
                        byte[] bytebuff = new byte[500];
                        while ((lengthread = fis.read(bytebuff)) != -1) {
                            output.write(bytebuff, 0, lengthread);
                        }
                        fis.close();
                        output.close();
                        dbu.executePreparedUpdate("INSERT INTO sdiattachment ( sdcid, keyid1, keyid2, keyid3, filename, typeflag, attachmentdesc, attachmentnum, createdt, createby, createtool, moddt, modby, modtool, sdiattachment ) values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ? )", new Object[]{"Release", release, "(null)", "(null)", patchZip.getFile().getAbsolutePath(), "F", "Generated patch.zip from build process", attachmentnum, now, "(system)", "Build", now, "(system)", "Build", output.toByteArray()});
                        dbu.executePreparedUpdate("UPDATE u_release SET patchbuildnum = ? WHERE u_releaseid = ?", new Object[]{patchbuildnum, release});
                    }
                } else {
                    throw new BuildException("Release '" + release + "' not found in DMS");
                }
                this.log("Patch file for '" + release + "' complete");
            }
        }
        catch (Exception se) {
            throw new BuildException("SapphireException: " + se.getMessage());
        }
        finally {
            dbu.reset();
        }
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connection = connection;
    }
}

