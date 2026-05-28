/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDINote;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.pageelements.controls.RichTextEditor;
import com.labvantage.sapphire.services.SapphireConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class EditSDINote
extends BaseAction
implements sapphire.action.EditSDINote {
    private static final String SDINOTE_SDCID = "SDINote";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        ArrayList tags;
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Missing sdcid!");
        }
        String keyid1 = properties.getProperty("keyid1");
        if (keyid1.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Missing keyid1!");
        }
        String keyid2 = properties.getProperty("keyid2", "(null)");
        String keyid3 = properties.getProperty("keyid3", "(null)");
        String notenum = properties.getProperty("notenum");
        if (notenum.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Missing notenum!");
        }
        Timestamp now = DateTimeUtil.getNowTimestamp();
        ConfigurationProcessor configurationProcessor = this.getConfigurationProcessor();
        PropertyList notesPolicy = configurationProcessor.findPolicy("NotesPolicy", "sdcid", sdcid);
        if (notesPolicy == null || notesPolicy.size() == 0) {
            notesPolicy = configurationProcessor.getPolicy("NotesPolicy", "Sapphire Custom");
        }
        this.database.createPreparedResultSet("SELECT notetypeflag, ownerid, note, followupnotifyownerflag FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND notenum = ?", new Object[]{sdcid, keyid1, keyid2, keyid3, notenum});
        this.database.getNext();
        String noteType = this.database.getValue("notetypeflag");
        String sql = "select refdisplayvalue from refvalue where reftypeid = 'NoteType' and refvalueid = ?";
        DataSet noteTypeDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{noteType});
        String noteTypeDesc = "User Note";
        if (noteTypeDs.getRowCount() > 0) {
            noteTypeDesc = noteTypeDs.getString(0, "refdisplayvalue", "User Note");
        }
        DataSet sdiNote = new DataSet(this.connectionInfo);
        sdiNote.addRow();
        sdiNote.setString(0, "sdcid", sdcid);
        sdiNote.setString(0, "keyid1", keyid1);
        sdiNote.setString(0, "keyid2", keyid2);
        sdiNote.setString(0, "keyid3", keyid3);
        sdiNote.setNumber(0, "notenum", notenum);
        sdiNote.setDate(0, "moddt", now);
        sdiNote.setString(0, "modtool", "EditSDINote");
        sdiNote.setString(0, "modby", this.connectionInfo.getSysuserId());
        String note = properties.getProperty("note");
        if (properties.containsKey("note")) {
            boolean escapeHtml;
            PropertyListCollection noteTypes = properties.getCollection("notesconfig");
            if (noteTypes == null) {
                PropertyList elementConf = configurationProcessor.findPolicy("sdinotes", "sdcid", sdcid);
                if (elementConf == null || elementConf.size() == 0) {
                    elementConf = configurationProcessor.getPolicy("sdinotes", "Sapphire Custom");
                }
                noteTypes = elementConf.getCollectionNotNull("notetypes");
            }
            noteTypes.index("notetype");
            PropertyList noteTypeProps = noteTypes.getIndexedPropertyList(noteType);
            if (noteTypeProps == null) {
                noteTypeProps = new PropertyList();
            }
            boolean bl = escapeHtml = !noteTypeProps.getProperty("inlineformatting", "Y").equals("F");
            if (escapeHtml) {
                note = RichTextEditor.escapeHTML(note, false);
            }
            sdiNote.setClob(0, "note", note);
        }
        PropertyList sdcProps = this.getSDCProcessor().getPropertyList(SDINOTE_SDCID);
        PropertyListCollection columns = sdcProps.getCollection("columns");
        PropertyList notetags = notesPolicy != null ? notesPolicy.getPropertyList("notetags") : new PropertyList();
        ArrayList arrayList = tags = notetags != null && notetags.getProperty("allow", "Y").equals("Y") ? notetags.getCollection("tags") : null;
        if (tags != null) {
            for (int i = 0; i < tags.size(); ++i) {
                PropertyList tag = ((PropertyListCollection)tags).getPropertyList(i);
                String columnid = tag.getProperty("columnid");
                if (!properties.containsKey(columnid)) continue;
                String value = properties.getProperty(columnid);
                PropertyList column = columns.getPropertyList(columnid);
                String datatype = column.getProperty("datatype");
                if (datatype.equals("C")) {
                    sdiNote.addColumn(columnid, 0);
                } else if (datatype.equals("T") || datatype.equals("B")) {
                    sdiNote.addColumn(columnid, 3);
                } else if (datatype.equals("N") || datatype.equals("R")) {
                    sdiNote.addColumn(columnid, 1);
                } else if (datatype.equals("D")) {
                    sdiNote.addColumn(columnid, 2);
                    if ("Y".equals(this.getSDCProcessor().getSDCColumnProperty(SDINOTE_SDCID, columnid, "timezoneindependent"))) {
                        sdiNote.setTimeZoneInsensitive(columnid);
                    }
                }
                sdiNote.setValue(0, columnid, value);
            }
        }
        boolean isNotifyFollowUpUser = false;
        boolean isNotifyOwner = false;
        if (properties.containsKey("followupnotifyuserflag")) {
            String notifyFollowupUser = properties.getProperty("followupnotifyuser", "N");
            isNotifyFollowUpUser = notifyFollowupUser.startsWith("Y");
            sdiNote.setString(0, "followupnotifyuserflag", notifyFollowupUser);
        }
        if (properties.containsKey("followupnotifyownerflag")) {
            String notifyOwner = properties.getProperty("followupnotifyowner", "N");
            isNotifyOwner = notifyOwner.startsWith("Y");
            sdiNote.setString(0, "followupnotifyownerflag", notifyOwner);
        } else {
            isNotifyOwner = this.database.getValue("followupnotifyownerflag").startsWith("Y");
        }
        if (properties.containsKey("followupuserid")) {
            String followupuserid = properties.getProperty("followupuserid");
            sdiNote.setString(0, "followupuserid", followupuserid);
            if (!followupuserid.isEmpty() && isNotifyFollowUpUser && !note.isEmpty()) {
                sql = "select followupuserid from sdinote where sdcid = ? and keyid1= ? and keyid2 = ? and keyid3 = ? and notenum = ?";
                DataSet folloupUserDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{sdcid, keyid1, keyid2, keyid3, notenum});
                String oldFollowUpUserId = folloupUserDs.getString(0, "followupuserid", "");
                if (!oldFollowUpUserId.equals(followupuserid)) {
                    String followupsubject = properties.getProperty("followupsubject");
                    ActionProcessor actionProcessor = this.getActionProcessor();
                    PropertyList bulletinProps = new PropertyList();
                    bulletinProps.setProperty("actionid", "SendBulletin");
                    bulletinProps.setProperty("actionversionid", "1");
                    String language = AddSDINote.getUserLanguage(followupuserid, this.getQueryProcessor());
                    String transSdcid = sdcid;
                    bulletinProps.setProperty("user", followupuserid);
                    if (!language.isEmpty()) {
                        followupsubject = this.getTranslationProcessor().translate(followupsubject, language);
                        transSdcid = this.getTranslationProcessor().translate(transSdcid, language);
                    }
                    followupsubject = AddSDINote.getBulletinText(transSdcid, keyid1, keyid2, keyid3, note, "", followupsubject, this.connectionInfo);
                    bulletinProps.setProperty("description", followupsubject);
                    bulletinProps.setProperty("body", note);
                    actionProcessor.processAction("AddToDoListEntry", "1", bulletinProps);
                }
            }
        }
        if (properties.containsKey("resolvednote")) {
            String resolvednote = properties.getProperty("resolvednote");
            String resolvedflag = properties.getProperty("resolvedflag", "Y");
            String resolveddt = properties.getProperty("resolveddt");
            sdiNote.setDate(0, "resolveddt", resolveddt);
            resolvednote = RichTextEditor.escapeHTML(resolvednote, false);
            sdiNote.setString(0, "resolvednote", resolvednote);
            String resolvedby = properties.getProperty("resolvedby");
            if (resolvedby != null && resolvedby.equals("(null)")) {
                resolvedby = null;
            }
            sdiNote.setString(0, "resolvedby", resolvedby);
            sdiNote.setString(0, "resolvedflag", resolvedflag);
            if (!resolvedflag.equals("N") && isNotifyOwner) {
                PropertyList notifyProps = notesPolicy.getPropertyList("notify");
                String bulletinSubject = notifyProps.getProperty("resolvesubject", this.getTranslationProcessor().translate("Your [notetypedesc] has been [resolvedflag]"));
                String bulletinBody = notifyProps.getProperty("resolvenotifytext", this.getTranslationProcessor().translate("Your [notetypedesc] <p> [note] </p> has been [resolvedflag]  by [resolvedby] with <p> [resolvednote] </p>"));
                String newNote = note;
                if (note == null || note.isEmpty()) {
                    newNote = this.database.getClob("note");
                }
                String resolvedFlagText = "";
                if (resolvedflag.equals("Y")) {
                    resolvedFlagText = "Resolved";
                } else if (resolvedflag.equals("R")) {
                    resolvedFlagText = "Rejected";
                }
                resolvedFlagText = this.getTranslationProcessor().translate(resolvedFlagText);
                noteTypeDesc = this.getTranslationProcessor().translate(noteTypeDesc);
                bulletinSubject = StringUtil.replaceAll(bulletinSubject, "[note]", newNote);
                bulletinBody = StringUtil.replaceAll(bulletinBody, "[note]", newNote);
                bulletinSubject = StringUtil.replaceAll(bulletinSubject, "[resolvedby]", resolvedby);
                bulletinBody = StringUtil.replaceAll(bulletinBody, "[resolvedby]", resolvedby);
                bulletinSubject = StringUtil.replaceAll(bulletinSubject, "[resolvednote]", resolvednote);
                bulletinBody = StringUtil.replaceAll(bulletinBody, "[resolvednote]", resolvednote);
                bulletinSubject = StringUtil.replaceAll(bulletinSubject, "[resolvedflag]", resolvedFlagText);
                bulletinBody = StringUtil.replaceAll(bulletinBody, "[resolvedflag]", resolvedFlagText);
                bulletinSubject = StringUtil.replaceAll(bulletinSubject, "[notetypedesc]", noteTypeDesc);
                bulletinBody = StringUtil.replaceAll(bulletinBody, "[notetypedesc]", noteTypeDesc);
                bulletinSubject = StringUtil.replaceAll(bulletinSubject, "[sdcid]", sdiNote.getString(0, "sdcid", ""));
                bulletinBody = StringUtil.replaceAll(bulletinBody, "[sdcid]", sdiNote.getString(0, "sdcid", ""));
                bulletinSubject = StringUtil.replaceAll(bulletinSubject, "[keyid1]", sdiNote.getString(0, "keyid1", ""));
                bulletinBody = StringUtil.replaceAll(bulletinBody, "[keyid1]", sdiNote.getString(0, "keyid1", ""));
                bulletinSubject = StringUtil.replaceAll(bulletinSubject, "[keyid2]", sdiNote.getString(0, "keyid2", ""));
                bulletinBody = StringUtil.replaceAll(bulletinBody, "[keyid2]", sdiNote.getString(0, "keyid2", ""));
                bulletinSubject = StringUtil.replaceAll(bulletinSubject, "[keyid3]", sdiNote.getString(0, "keyid3", ""));
                bulletinBody = StringUtil.replaceAll(bulletinBody, "[keyid3]", sdiNote.getString(0, "keyid3", ""));
                ActionProcessor actionProcessor = this.getActionProcessor();
                PropertyList bulletinProps = new PropertyList();
                bulletinProps.setProperty("actionid", "SendBulletin");
                bulletinProps.setProperty("actionversionid", "1");
                bulletinProps.setProperty("user", this.database.getValue("ownerid"));
                bulletinProps.setProperty("description", bulletinSubject);
                bulletinProps.setProperty("body", bulletinBody);
                actionProcessor.processAction("AddToDoListEntry", "1", bulletinProps);
            }
        }
        if (properties.containsKey("tagcolumnid")) {
            String tagcolumnid = properties.getProperty("tagcolumnid");
            sdiNote.setString(0, tagcolumnid, properties.getProperty(tagcolumnid));
        }
        PropertyList primarySdcProps = this.getSDCProcessor().getPropertyList(sdcid);
        SDIData beforeEditImage = null;
        SDIData sdiData = new SDIData();
        sdiData.setDataset("notes", sdiNote);
        BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, primarySdcProps, "PreEditNote");
        if (sdcPreRules.requiresEditDetailPrimary() || sdcPreRules.customRulesRequiresEditDetailPrimary() || sdcPreRules.requiresBeforeEditDetailImage() || sdcPreRules.customRulesRequiresBeforeEditDetailImage()) {
            BaseSDCRules[] sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(sdcid);
            sdiRequest.setKeyid1List(keyid1);
            sdiRequest.setKeyid2List(keyid2);
            sdiRequest.setKeyid3List(keyid3);
            sdiRequest.setRetainRsetid(true);
            if (sdcPreRules.requiresEditDetailPrimary() || sdcPreRules.customRulesRequiresEditDetailPrimary()) {
                sdiRequest.setRequestItem("primary");
            }
            if (sdcPreRules.requiresBeforeEditDetailImage() || sdcPreRules.customRulesRequiresBeforeEditDetailImage()) {
                sdiRequest.setRequestItem("notes");
            }
            SDIProcessor sdiProcessor = this.getSDIProcessor();
            beforeEditImage = sdiProcessor.getSDIData((SDIRequest)sdiRequest);
            sdcPreRules.setBeforeEditImage(beforeEditImage);
            if (beforeEditImage != null && beforeEditImage.getDataset("primary") != null) {
                sdiData.setDataset("primary", beforeEditImage.getDataset("primary"));
            }
        }
        Trace.startBusinessRule(sdcid + "." + "PreEditNote", true);
        sdcPreRules.preEditNote(sdiData, properties);
        Trace.endBusinessRule(sdcid + "." + "PreEditNote", true);
        Trace.startBusinessRule(sdcid + "." + "PreEditNote", false);
        for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
            customRules.preEditNote(sdiData, properties);
        }
        Trace.endBusinessRule(sdcid + "." + "PreEditNote", false);
        sdcPreRules.endRule();
        DataSetUtil.update(this.database, sdiNote, "sdinote", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "notenum"});
        BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, primarySdcProps, "PostEditNote");
        Trace.startBusinessRule(sdcid + "." + "PostEditNote", true);
        sdcPostRules.postEditNote(sdiData, properties);
        Trace.endBusinessRule(sdcid + "." + "PostEditNote", true);
        Trace.startBusinessRule(sdcid + "." + "PostEditNote", false);
        for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
            customRules.postEditNote(sdiData, properties);
        }
        Trace.endBusinessRule(sdcid + "." + "PostEditNote", false);
        sdcPostRules.endRule();
        if (properties.getProperty("index", "Y").equals("Y")) {
            Indexer.indexNote(this.connectionInfo, sdcid, keyid1, keyid2, keyid3, new Integer(notenum));
        }
    }
}

