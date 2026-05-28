/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.digest.DigestUtils
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.admin.system.TranslationUtil;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import org.apache.commons.codec.digest.DigestUtils;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class I18NService
extends BaseService {
    public static final String LOGNAME = "I18NService";

    public I18NService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public boolean isAutoFillTempAllowed() throws ServiceException {
        this.logInfo("Determining if master trans update is allowed");
        try {
            ConfigService config = new ConfigService(this.sapphireConnection);
            return config.getProfileProperty(this.connectionInfo.getSysuserId(), "masterupdate", "N").equals("Y");
        }
        catch (ServiceException e) {
            throw new ServiceException("TRANSLATION_SERVICE_FAILED", "Failed to get profile property 'masterupdate'", e);
        }
    }

    public static String generateTransmasterid(String textid, String texttype) {
        return "M" + DigestUtils.md5Hex((String)(textid + texttype));
    }

    public void addToTransmasterTemp(String originaltext, String texttype) throws ServiceException {
        block9: {
            this.logInfo("Adding transmastertemp data");
            if (originaltext == null || originaltext.length() <= 0 || originaltext.indexOf("____") >= 0 || originaltext.indexOf("$") == 0 && originaltext.lastIndexOf("$") == originaltext.length() - 1) break block9;
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                db.createPreparedResultSet("selecttransmaster", "SELECT transmasterid FROM transmaster WHERE textid = ? and texttype = ?", new String[]{originaltext, texttype});
                PreparedStatement ps = db.prepareStatement("inserttransmaster", "INSERT INTO transmastertemp ( transmasterid, textid, texttype, clientsideflag, loadpriority ) VALUES ( ?, ?, ?, ?, 'Other' )");
                if (db.getNext("selecttransmaster")) break block9;
                try {
                    ps.setString(1, I18NService.generateTransmasterid(originaltext, texttype));
                    ps.setString(2, originaltext);
                    ps.setString(3, texttype);
                    ps.setString(4, "");
                    ps.execute();
                }
                catch (SQLException e) {
                    block11: {
                        block10: {
                            if (this.sapphireConnection.getDbms().equals("ORA") && e.getMessage().indexOf("ORA-00001") > -1) break block10;
                            if (!this.sapphireConnection.getDbms().equals("MSS")) break block11;
                            if (e.getMessage().indexOf("duplicate key") <= -1) {
                                // empty if block
                            }
                        }
                        this.logInfo("The textid and type already exists: " + originaltext + ", " + texttype);
                        break block9;
                    }
                    this.logError("Cannot insert the textid/type: " + originaltext + "/" + texttype + " into transmastertemp table", e);
                }
            }
            catch (SapphireException e) {
                throw new ServiceException("TRANSLATION_SERVICE_FAILED", "Failed to add transmastertemp record", e);
            }
            finally {
                db.reset();
            }
        }
    }

    public void saveTranslation(String language, String textidlist, String transtextlist, String texttypelist) throws ServiceException {
        this.logInfo("Saving translation data");
        CacheUtil.remove(this.connectionInfo.getDatabaseId(), "TranslationELNSpreadsheet", language);
        if (textidlist != null && textidlist.length() > 0) {
            String[] texttypes;
            DataSet saveDS = new DataSet();
            saveDS.addColumn("transmasterid", 0);
            saveDS.addColumn("textid", 0);
            saveDS.addColumn("texttype", 0);
            saveDS.addColumn("languageid", 0);
            saveDS.addColumn("transtext", 0);
            String[] textids = StringUtil.split(textidlist, ";");
            String[] transtexts = StringUtil.split(transtextlist, ";");
            String[] stringArray = texttypes = texttypelist == null ? null : StringUtil.split(texttypelist, ";");
            if (texttypes == null) {
                texttypes = new String[textids.length];
                for (int i = 0; i < texttypes.length; ++i) {
                    texttypes[i] = "W";
                }
            }
            for (int r = 0; r < textids.length; ++r) {
                int row = saveDS.addRow();
                saveDS.setValue(row, "textid", textids[r]);
                saveDS.setValue(row, "texttype", texttypes[r]);
                saveDS.setValue(row, "transmasterid", I18NService.generateTransmasterid(saveDS.getValue(r, "textid"), saveDS.getValue(r, "texttype")));
                saveDS.setValue(row, "transtext", r < transtexts.length ? transtexts[r] : "");
            }
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            String byuser = this.sapphireConnection.getSysuserId();
            String bytool = "Save Or Excel Import";
            Calendar createOrModdt = Calendar.getInstance();
            try {
                db.setConnection(this.sapphireConnection);
                HashSet<String> webtextidCache = new HashSet<String>();
                db.createResultSet("SELECT transmasterid, textid, texttype FROM transmaster");
                while (db.getNext()) {
                    String transmasterid = db.getString("transmasterid");
                    if (transmasterid.indexOf("CA-") == 0) {
                        String textid = db.getString("textid");
                        String texttype = db.getString("texttype");
                        String newtransmasterid = I18NService.generateTransmasterid(textid, texttype);
                        db.executePreparedUpdate("DELETE transmaster WHERE transmasterid=?", new String[]{transmasterid});
                        db.executePreparedUpdate("INSERT INTO transmaster ( transmasterid, textid, texttype, createby, createtool, createdt ) VALUES ( ?, ?, ?, ?, ?, ? )", new Object[]{newtransmasterid, textid, texttype, byuser, bytool, new Timestamp(createOrModdt.getTime().getTime())});
                        db.executePreparedUpdate("UPDATE translanguage SET transmasterid=? WHERE transmasterid=?", new String[]{newtransmasterid, transmasterid});
                        webtextidCache.add(I18NService.generateTransmasterid(textid, texttype));
                        continue;
                    }
                    webtextidCache.add(db.getString("transmasterid"));
                }
                db.closeResultSet();
                PreparedStatement insertTransmaster = db.prepareStatement("insertTransmaster", "INSERT INTO transmaster ( transmasterid, textid, texttype, clientsideflag, createby, createtool, createdt  ) VALUES ( ?, ?, ?, 'N', ?, ?, ? )");
                PreparedStatement insertTransLang = db.prepareStatement("insertTransLang", "INSERT INTO translanguage ( transmasterid, languageid, transtext, createby, createtool, createdt ) VALUES ( ?, ?, ?, ?, ?, ? )");
                PreparedStatement updateTranslang = db.prepareStatement("updateTranslang", "UPDATE translanguage SET transtext = ?, modby = ?, modtool = ?, moddt = ?  WHERE transmasterid = ? AND languageid = ? ");
                for (int i = 0; i < saveDS.getRowCount(); ++i) {
                    String textid = saveDS.getValue(i, "textid");
                    String texttype = saveDS.getValue(i, "texttype");
                    String transtext = saveDS.getValue(i, "transtext");
                    String transmasterid = saveDS.getValue(i, "transmasterid");
                    if (!TranslationUtil.isExcludedText(textid)) {
                        if (!webtextidCache.contains(transmasterid)) {
                            insertTransmaster.setString(1, transmasterid);
                            insertTransmaster.setString(2, textid);
                            insertTransmaster.setString(3, texttype);
                            insertTransmaster.setString(4, byuser);
                            insertTransmaster.setString(5, bytool);
                            insertTransmaster.setTimestamp(6, new Timestamp(createOrModdt.getTime().getTime()));
                            try {
                                insertTransmaster.execute();
                            }
                            catch (Exception e) {
                                this.logInfo("Failed inserting into transmaster: " + transmasterid + ";" + textid + ":" + texttype);
                                throw new SapphireException("Failed inserting into transmaster table. Exception: " + e.getMessage());
                            }
                            this.logInfo("insert into transmaster: " + textid + ":" + texttype);
                            webtextidCache.add(transmasterid);
                        }
                        if (language == null || language.length() <= 0 || !webtextidCache.contains(transmasterid) || transtext.trim().length() <= 0) continue;
                        String checkExist = "SELECT transmasterid FROM translanguage WHERE transmasterid = ? AND languageid = ?";
                        if (!db.checkPreparedExists(checkExist, new Object[]{transmasterid, language})) {
                            insertTransLang.setString(1, transmasterid);
                            insertTransLang.setString(2, language);
                            insertTransLang.setString(3, transtext);
                            insertTransLang.setString(4, byuser);
                            insertTransLang.setString(5, bytool);
                            insertTransLang.setTimestamp(6, new Timestamp(createOrModdt.getTime().getTime()));
                            insertTransLang.execute();
                            this.logInfo("insert into translanguage: " + transmasterid + ", " + language + ", " + transtext);
                            continue;
                        }
                        updateTranslang.setString(1, transtext);
                        updateTranslang.setString(2, byuser);
                        updateTranslang.setString(3, bytool);
                        updateTranslang.setTimestamp(4, new Timestamp(createOrModdt.getTime().getTime()));
                        updateTranslang.setString(5, transmasterid);
                        updateTranslang.setString(6, language);
                        updateTranslang.execute();
                        this.logInfo("update translanguage: " + transtext + "," + transmasterid + "," + language);
                        continue;
                    }
                    this.logWarn("Textid or Transtext excluded: " + transtext + "," + textid + "," + texttype + "," + language);
                }
            }
            catch (Exception e) {
                throw new ServiceException("TRANSLATION_SERVICE_FAILED", "Failed to save translation data", e);
            }
            finally {
                db.reset();
            }
        }
    }

    public PropertyList translateTable(String languageid, PropertyList transTable) throws ServiceException {
        this.logInfo("Translating table to language '" + languageid + "'");
        if (languageid == null || languageid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Languageid not specified");
        }
        if (transTable.size() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Translations not specified");
        }
        StringBuffer sql = new StringBuffer("SELECT m.textid, m.texttype, l.transtext FROM transmaster m, translanguage l ");
        String[] textKeys = transTable.keySet().toArray(new String[transTable.size()]);
        sql.append(" WHERE m.transmasterid = l.transmasterid and m.textid in ( '" + textKeys[0].replaceAll("'", "''") + "' ");
        for (int i = 1; i < textKeys.length; ++i) {
            sql.append(", '" + textKeys[i].replaceAll("'", "''") + "' ");
        }
        sql.append(") AND l.languageid = '" + languageid.replaceAll("'", "''") + "'");
        sql.append(" ORDER BY m.textid");
        return this.loadTranslations(sql.toString());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PropertyList getWebTranslations(String languageid) throws ServiceException {
        this.logInfo("Getting web translations for language '" + languageid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.createResultSet("SELECT transmasterid, textid, texttype, clientsideflag FROM transmaster where transmasterid not like 'M%'");
            while (db.getNext()) {
                String transmasterid = db.getString("transmasterid");
                if (transmasterid.indexOf("M") == 0) continue;
                String textid = db.getString("textid");
                String texttype = db.getString("texttype");
                String clientsideflag = db.getString("clientsideflag");
                String newtransmasterid = I18NService.generateTransmasterid(textid, texttype);
                db.executePreparedUpdate("DELETE transmaster WHERE transmasterid=?", new String[]{transmasterid});
                db.executePreparedUpdate("INSERT INTO transmaster ( transmasterid, textid, texttype, clientsideflag ) VALUES ( ?, ?, ?, ? )", new String[]{newtransmasterid, textid, texttype, clientsideflag});
                db.executePreparedUpdate("UPDATE translanguage SET transmasterid=? WHERE transmasterid=?", new String[]{newtransmasterid, transmasterid});
            }
            db.closeResultSet();
        }
        catch (SapphireException sapphireException) {
        }
        finally {
            db.reset();
        }
        if (languageid == null || languageid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Languageid not specified");
        }
        return this.loadTranslations("SELECT m.textid, m.texttype, l.transtext FROM transmaster m, translanguage l WHERE m.transmasterid=l.transmasterid AND l.languageid = '" + languageid.replaceAll("'", "''") + "' ORDER BY m.textid, m.texttype");
    }

    private PropertyList loadTranslations(String sql) throws ServiceException {
        PropertyList transTable = new PropertyList();
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.createResultSet(sql);
            String pTextid = "";
            String pTransText = "";
            String pTextType = "";
            while (db.getNext()) {
                String textid = db.getString("textid");
                String texttype = db.getString("texttype");
                String transtext = db.getValue("transtext");
                if (transtext.length() <= 0) continue;
                if (pTextid.length() > 0 && pTextid.equals(textid)) {
                    if (transTable.get(textid) instanceof String) {
                        PropertyList p = new PropertyList();
                        p.setProperty(pTextType, pTransText);
                        transTable.setProperty(textid, p);
                        transTable.setProperty(textid.toLowerCase(), p);
                    }
                    try {
                        ((PropertyList)transTable.get(textid)).setProperty(texttype, transtext);
                    }
                    catch (Exception e) {
                        String string = e.getMessage();
                    }
                } else {
                    transTable.setProperty(textid, transtext);
                    transTable.setProperty(textid.toLowerCase(), transtext);
                }
                pTextid = textid;
                pTransText = transtext;
                pTextType = texttype;
            }
            db.closeResultSet();
            PropertyList propertyList = transTable;
            return propertyList;
        }
        catch (SapphireException e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to load translations using " + sql, e);
        }
        finally {
            db.reset();
        }
    }
}

