/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.gwt.shared.constants.SDINoteConstants;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostAddNoteEventObject;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.pageelements.controls.RichTextEditor;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.action.BaseAction;
import sapphire.action.BaseSDCRules;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AddSDINote
extends BaseAction
implements sapphire.action.AddSDINote,
SDINoteConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        Timestamp now;
        ArrayList tags;
        PropertyList noteTypeProps;
        PropertyListCollection noteTypes;
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Missing sdcid!");
        }
        String keyid1 = properties.getProperty("keyid1");
        if (keyid1.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Missing keyid1!");
        }
        if (keyid1.equalsIgnoreCase("(Auto)")) {
            keyid1 = properties.getProperty("newkeyid1");
        }
        String keyid2 = properties.getProperty("keyid2", "(null)");
        String keyid3 = properties.getProperty("keyid3", "(null)");
        int threadnum = -1;
        String threadflag = properties.getProperty("threadnum").length() > 0 ? "C" : "R";
        try {
            threadnum = Integer.parseInt(properties.getProperty("threadnum"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (threadflag.equals("C") && threadnum == -1) {
            throw new SapphireException("INVALID_PROPERTY", "Invalid threadnum for a comment to a note!");
        }
        boolean followup = properties.getProperty("followup", "N").equals("Y");
        if (threadflag.equals("C") && followup) {
            throw new SapphireException("INVALID_PROPERTY", "Comment note cannot be a followup!");
        }
        if (threadflag.equals("R")) {
            SequenceProcessor sequenceProcessor = this.getSequenceProcessor();
            threadnum = sequenceProcessor.getSequence("SDINote", "threadnum");
        }
        String notetypeflag = properties.getProperty("notetype", "S");
        ConfigurationProcessor configurationProcessor = this.getConfigurationProcessor();
        PropertyList notesPolicy = configurationProcessor.findPolicy("NotesPolicy", "sdcid", sdcid);
        if (notesPolicy == null || notesPolicy.size() == 0) {
            notesPolicy = configurationProcessor.getPolicy("NotesPolicy", "Sapphire Custom");
        }
        if ((noteTypes = properties.getCollection("notesconfig")) == null) {
            PropertyList elementConf = configurationProcessor.findPolicy("sdinotes", "sdcid", sdcid);
            if (elementConf == null || elementConf.size() == 0) {
                elementConf = configurationProcessor.getPolicy("sdinotes", "Sapphire Custom");
            }
            noteTypes = elementConf.getCollectionNotNull("notetypes");
        }
        if (noteTypes != null) {
            noteTypes.index("notetype");
        }
        PropertyList propertyList = noteTypeProps = noteTypes == null ? null : noteTypes.getIndexedPropertyList(notetypeflag);
        if (noteTypeProps == null) {
            noteTypeProps = new PropertyList();
        }
        boolean escapeHtml = !noteTypeProps.getProperty("inlineformatting", "Y").equals("F");
        String note = properties.getProperty("note");
        if (escapeHtml) {
            note = RichTextEditor.escapeHTML(note, false);
        }
        String activity = properties.getProperty("activity");
        DataSet sdiNote = new DataSet(this.connectionInfo);
        sdiNote.addRow();
        PropertyList sdc = this.getSDCProcessor().getPropertyList("SDINote");
        PropertyListCollection columns = sdc.getCollection("columns");
        PropertyList notetags = notesPolicy != null ? notesPolicy.getPropertyList("notetags") : new PropertyList();
        ArrayList arrayList = tags = notetags != null && notetags.getProperty("allow", "Y").equals("Y") ? notetags.getCollection("tags") : null;
        if (tags != null) {
            for (int i = 0; i < tags.size(); ++i) {
                PropertyList tag = ((PropertyListCollection)tags).getPropertyList(i);
                String scope = tag.getProperty("scope", "F");
                String columnid = tag.getProperty("columnid");
                String value = tag.getProperty("defaultvalue");
                if (properties.containsKey(columnid)) {
                    value = properties.getProperty(columnid);
                }
                if (value.length() <= 0 || !scope.equals("F") && (!scope.equals("N") || !threadflag.equals("R")) && (!scope.equals("C") || threadflag.equals("R"))) continue;
                PropertyList column = columns.getPropertyList(columnid);
                String datatype = column.getProperty("datatype");
                long length = StringUtil.getLen(value);
                if (length <= 0L) continue;
                if (datatype.equals("C")) {
                    sdiNote.addColumn(columnid, 0);
                } else if (datatype.equals("T") || datatype.equals("B")) {
                    sdiNote.addColumn(columnid, 3);
                } else if (datatype.equals("N") || datatype.equals("R")) {
                    sdiNote.addColumn(columnid, 1);
                } else if (datatype.equals("D")) {
                    sdiNote.addColumn(columnid, 2);
                    if ("Y".equals(this.getSDCProcessor().getSDCColumnProperty(sdcid, columnid, "timezoneindependent"))) {
                        sdiNote.setTimeZoneInsensitive(columnid);
                    }
                }
                sdiNote.setValue(0, columnid, value);
            }
        }
        boolean notify = false;
        String threadownerid = "";
        String threadnote = "";
        String contexttype = properties.getProperty("contexttype");
        String context = properties.getProperty("context");
        String contextsdcid = "";
        String contextsdikeyidvalue = "";
        if (threadflag.equals("C")) {
            this.database.createPreparedResultSet("SELECT sdinote.notifyflag, sdinote.ownerid, sdinote.note, sdinote.contexttype, sdinote.context, sdinote.contextsdcid, sdinote.contextsdikeyidvalue FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND threadnum = ? AND threadflag = 'R'", new Object[]{sdcid, keyid1, keyid2, keyid3, threadnum});
            if (this.database.getNext()) {
                notify = this.database.getValue("notifyflag").equals("Y");
                threadownerid = this.database.getValue("ownerid");
                threadnote = this.database.getClob("note");
                contexttype = this.database.getValue("contexttype");
                context = this.database.getValue("context");
                contextsdcid = this.database.getValue("contextsdcid");
                contextsdikeyidvalue = this.database.getValue("contextsdikeyidvalue");
            }
        } else if (contexttype.length() > 0 && context.length() > 0) {
            String[] contextparts = StringUtil.split(context, ";");
            String sdikeyid = null;
            if (contexttype.equals("sdiworkitem") && contextparts.length == 6) {
                this.database.createPreparedResultSet("SELECT sdiworkitemid FROM sdiworkitem WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND workitemid = ? AND workiteminstance = ?", new Object[]{contextparts[0], contextparts[1], contextparts[2], contextparts[3], contextparts[4], contextparts[5]});
                contextsdcid = "SDIWorkItem";
                sdikeyid = "sdiworkitemid";
            } else if (contexttype.equals("dataset") && contextparts.length == 8) {
                this.database.createPreparedResultSet("SELECT sdidataid FROM sdidata WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND paramlistid = ? AND paramlistversionid = ? AND variantid = ? AND dataset = ?", new Object[]{contextparts[0], contextparts[1], contextparts[2], contextparts[3], contextparts[4], contextparts[5], contextparts[6], contextparts[7]});
                contextsdcid = "DataSet";
                sdikeyid = "sdidataid";
            } else if (contexttype.equals("dataitem") && contextparts.length == 11) {
                this.database.createPreparedResultSet("SELECT sdidataitemid FROM sdidataitem WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND paramlistid = ? AND paramlistversionid = ? AND variantid = ? AND dataset = ? AND paramid = ? AND paramtype = ? AND replicateid = ?", new Object[]{contextparts[0], contextparts[1], contextparts[2], contextparts[3], contextparts[4], contextparts[5], contextparts[6], contextparts[7], contextparts[8], contextparts[9], contextparts[10]});
                contextsdcid = "DataItem";
                sdikeyid = "sdidataitemid";
            } else {
                throw new SapphireException("Invalid contexttype '" + contexttype + "' or context '" + context + "' for note!");
            }
            if (this.database.getNext()) {
                contextsdikeyidvalue = this.database.getValue(sdikeyid);
            }
        }
        sdiNote.setString(0, "sdcid", sdcid);
        sdiNote.setString(0, "keyid1", keyid1);
        sdiNote.setString(0, "keyid2", keyid2);
        sdiNote.setString(0, "keyid3", keyid3);
        sdiNote.setNumber(0, "threadnum", threadnum);
        sdiNote.setString(0, "threadflag", threadflag);
        sdiNote.setString(0, "notetypeflag", notetypeflag);
        sdiNote.setString(0, "note", note);
        sdiNote.setString(0, "activity", activity);
        sdiNote.setString(0, "contexttype", contexttype);
        sdiNote.setString(0, "context", context);
        sdiNote.setString(0, "contextsdcid", contextsdcid);
        sdiNote.setString(0, "contextsdikeyidvalue", contextsdikeyidvalue);
        sdiNote.setString(0, "linksdcid", "");
        sdiNote.setString(0, "linkkeyid1", "");
        sdiNote.setString(0, "linkkeyid2", "");
        sdiNote.setString(0, "linkkeyid3", "");
        sdiNote.setNumber(0, "linknotenum", (BigDecimal)null);
        DateTimeUtil dtu = new DateTimeUtil();
        String ownerdt = properties.getProperty("createdt");
        String ownerid = properties.getProperty("createby");
        Timestamp timestamp = now = ownerdt == null || ownerdt.length() == 0 ? DateTimeUtil.getNowTimestamp() : dtu.getTimestamp(ownerdt);
        if (ownerid == null || ownerid.length() == 0) {
            ownerid = this.connectionInfo.getSysuserId();
        }
        sdiNote.setDate(0, "ownerdt", now);
        sdiNote.setDate(0, "createdt", now);
        sdiNote.setDate(0, "moddt", now);
        sdiNote.setDate(0, "ownerdt", now);
        sdiNote.setString(0, "ownerid", ownerid);
        sdiNote.setString(0, "createby", ownerid);
        sdiNote.setString(0, "createtool", "AddSDINote");
        sdiNote.setString(0, "modtool", "AddSDINote");
        sdiNote.setString(0, "modby", ownerid);
        sdiNote.setString(0, "followupflag", followup ? "Y" : "N");
        String followupuserid = properties.getProperty("followupuserid");
        sdiNote.setString(0, "followupuserid", followupuserid);
        sdiNote.setString(0, "followupnotifyuserflag", properties.getProperty("followupnotifyuser", "N"));
        sdiNote.setString(0, "followupnotifyownerflag", properties.getProperty("followupnotifyowner", "N"));
        sdiNote.setString(0, "resolvedflag", "N");
        this.addNote(sdiNote, sdcid, keyid1, keyid2, keyid3, properties);
        int notenum = sdiNote.getInt(0, "notenum");
        String linksdcid = properties.getProperty("linksdcid");
        if (linksdcid.length() > 0) {
            String linkkeyid1 = properties.getProperty("linkkeyid1");
            String linkkeyid2 = properties.getProperty("linkkeyid2", "(null)");
            String linkkeyid3 = properties.getProperty("linkkeyid3", "(null)");
            if (linkkeyid1.length() > 0) {
                sdiNote.setString(0, "sdcid", linksdcid);
                sdiNote.setString(0, "keyid1", linkkeyid1);
                sdiNote.setString(0, "keyid2", linkkeyid2);
                sdiNote.setString(0, "keyid3", linkkeyid3);
                sdiNote.setString(0, "notetypeflag", "L");
                sdiNote.setValue(0, "note", "");
                sdiNote.setValue(0, "activity", "");
                sdiNote.setString(0, "linksdcid", sdcid);
                sdiNote.setString(0, "linkkeyid1", keyid1);
                sdiNote.setString(0, "linkkeyid2", keyid2);
                sdiNote.setString(0, "linkkeyid3", keyid3);
                sdiNote.setNumber(0, "linknotenum", notenum);
                sdiNote.setDate(0, "ownerdt", (Calendar)null);
                sdiNote.setValue(0, "ownerid", this.connectionInfo.getSysuserId());
                sdiNote.setValue(0, "followupflag", "N");
                sdiNote.setValue(0, "followupuserid", "");
                sdiNote.setValue(0, "followupnotifyuserflag", "N");
                sdiNote.setValue(0, "followupnotifyownerflag", "N");
                sdiNote.setValue(0, "resolvedflag", "N");
                this.addNote(sdiNote, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, properties);
            }
        }
        properties.setProperty("notenum", String.valueOf(notenum));
        if (notenum >= 0 && notify) {
            String body;
            PropertyList notifyOptions = notesPolicy.getPropertyList("notify");
            ActionProcessor actionProcessor = this.getActionProcessor();
            PropertyList bulletinProps = new PropertyList();
            bulletinProps.setProperty("actionid", "SendBulletin");
            bulletinProps.setProperty("actionversionid", "1");
            bulletinProps.setProperty("user", threadownerid);
            String language = AddSDINote.getUserLanguage(threadownerid, this.getQueryProcessor());
            String desc = notifyOptions != null ? notifyOptions.getProperty("bulletindesc") : "[currentuser] has commented on your note";
            String transSdcid = sdcid;
            if (!language.isEmpty()) {
                desc = this.getTranslationProcessor().translate(desc, language);
                transSdcid = this.getTranslationProcessor().translate(transSdcid, language);
            }
            desc = AddSDINote.getBulletinText(transSdcid, keyid1, keyid2, keyid3, note, threadnote, desc, this.connectionInfo);
            bulletinProps.setProperty("description", desc);
            String string = body = notifyOptions != null ? notifyOptions.getProperty("bulletinbody") : "Your note <p>[note]</p> has been commented on by [currentuser] with <p>[comment]</p>";
            if (!language.isEmpty()) {
                body = this.getTranslationProcessor().translate(body, language);
            }
            body = AddSDINote.getBulletinText(transSdcid, keyid1, keyid2, keyid3, note, threadnote, body, this.connectionInfo);
            bulletinProps.setProperty("body", body);
            actionProcessor.processAction("AddToDoListEntry", "1", bulletinProps);
        }
        if (followup && properties.getProperty("followupnotifyuser", "N").equals("Y") && followupuserid.length() > 0) {
            ActionProcessor actionProcessor = this.getActionProcessor();
            PropertyList bulletinProps = new PropertyList();
            bulletinProps.setProperty("actionid", "SendBulletin");
            bulletinProps.setProperty("actionversionid", "1");
            String language = AddSDINote.getUserLanguage(followupuserid, this.getQueryProcessor());
            bulletinProps.setProperty("user", followupuserid);
            String followupsubject = properties.getProperty("followupsubject");
            String transSdcid = sdcid;
            if (!language.isEmpty()) {
                followupsubject = this.getTranslationProcessor().translate(followupsubject, language);
                transSdcid = this.getTranslationProcessor().translate(transSdcid, language);
            }
            followupsubject = AddSDINote.getBulletinText(transSdcid, keyid1, keyid2, keyid3, note, threadnote, followupsubject, this.connectionInfo);
            bulletinProps.setProperty("description", followupsubject);
            bulletinProps.setProperty("body", note);
            actionProcessor.processAction("AddToDoListEntry", "1", bulletinProps);
        }
        if (properties.getProperty("index", "Y").equals("Y")) {
            Indexer.indexNote(this.connectionInfo, sdcid, keyid1, keyid2, keyid3, new Integer(notenum));
        }
    }

    protected static String getBulletinText(String sdcid, String keyid1, String keyid2, String keyid3, String note, String threadnote, String body, ConnectionInfo connectionInfo2) {
        body = StringUtil.replaceAll(body, "[sdcid]", sdcid);
        body = StringUtil.replaceAll(body, "[keyid1]", keyid1);
        body = StringUtil.replaceAll(body, "[keyid2]", keyid2);
        body = StringUtil.replaceAll(body, "[keyid3]", keyid3);
        body = StringUtil.replaceAll(body, "[currentuser]", connectionInfo2.getSysuserId());
        body = StringUtil.replaceAll(body, "[sysuserid]", connectionInfo2.getSysuserId());
        body = StringUtil.replaceAll(body, "[sysusername]", connectionInfo2.getSysuserName());
        body = StringUtil.replaceAll(body, "[note]", threadnote);
        body = StringUtil.replaceAll(body, "[comment]", note);
        return body;
    }

    public static String getUserLanguage(String sysuserid, QueryProcessor qp) {
        String sql = "select languageid from sysuser where sysuserid = ? ";
        DataSet ds = qp.getPreparedSqlDataSet(sql, (Object[])new String[]{sysuserid});
        String language = "";
        if (ds.getRowCount() == 1) {
            language = ds.getString(0, "languageid", "");
        }
        return language;
    }

    private synchronized void addNote(DataSet sdiNote, String sdcid, String keyid1, String keyid2, String keyid3, PropertyList properties) throws SapphireException {
        int tries = 10;
        int notenum = -1;
        PropertyList sdc = this.getSDCProcessor().getPropertyList(sdcid);
        SDIData beforeEditImage = null;
        SDIData sdiData = new SDIData();
        sdiData.setDataset("notes", sdiNote);
        BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, sdc, "PreAddNote");
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setSDIList(sdcid, keyid1, keyid2, keyid3);
        sdiRequest.setRetainRsetid(true);
        if (sdcPreRules.requiresEditDetailPrimary() || sdcPreRules.customRulesRequiresEditDetailPrimary()) {
            sdiRequest.setRequestItem("primary");
        }
        if (sdcPreRules.requiresBeforeEditDetailImage() || sdcPreRules.customRulesRequiresBeforeEditDetailImage()) {
            sdiRequest.setRequestItem("notes");
        }
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        beforeEditImage = sdiProcessor.getSDIData(sdiRequest);
        sdcPreRules.setBeforeEditImage(beforeEditImage);
        if (beforeEditImage != null && beforeEditImage.getDataset("primary") != null) {
            sdiData.setDataset("primary", beforeEditImage.getDataset("primary"));
        }
        Trace.startBusinessRule(sdcid + "." + "PreAddNote", true);
        sdcPreRules.preAddNote(sdiData, properties);
        Trace.endBusinessRule(sdcid + "." + "PreAddNote", true);
        Trace.startBusinessRule(sdcid + "." + "PreAddNote", false);
        for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
            customRules.preAddNote(sdiData, properties);
        }
        Trace.endBusinessRule(sdcid + "." + "PreAddNote", false);
        sdcPreRules.endRule();
        this.database.createPreparedResultSet("SELECT MAX( notenum ) notenum FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ?", new Object[]{sdcid, keyid1, keyid2, keyid3});
        notenum = this.database.getNext() ? this.database.getInt("notenum") + 1 : 1;
        sdiNote.setNumber(0, "notenum", notenum);
        try {
            DataSetUtil.insert(this.database, sdiNote, "sdinote");
            BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, null, "PostAddNote");
            Trace.startBusinessRule(sdcid + "." + "PostAddNote", true);
            sdcPostRules.postAddNote(sdiData, properties);
            Trace.endBusinessRule(sdcid + "." + "PostAddNote", true);
            Trace.startBusinessRule(sdcid + "." + "PostAddNote", false);
            for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                customRules.postAddNote(sdiData, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PostAddNote", false);
            sdcPostRules.endRule();
        }
        catch (SapphireException e) {
            throw new SapphireException("Failed to get new unique notenum.");
        }
        EventManager.generateEvent(new SapphireConnection(this.database.getConnection(), this.connectionInfo), new ErrorHandler(), new PostAddNoteEventObject(sdcid, null, sdiData, properties));
    }
}

