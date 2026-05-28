/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.lucene.document.Document
 */
package com.labvantage.sapphire.modules.search;

import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import org.apache.lucene.document.Document;
import sapphire.SapphireException;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SearchDocument {
    private Indexer indexer;
    private M18NUtil m18n;
    private Document document;
    private String[] idparts;
    private int docid;
    private float score;
    private HashMap primary;
    private HashMap note;
    private HashMap attachment;
    private ArrayList<String> matchingColumns;
    private ArrayList<String> boostMatchingColumns;
    private String keycolid1;
    private boolean keyMatch = false;
    private PropertyList sdcRender;
    private PropertyListCollection resultColumns;
    private boolean childSDC = false;
    private PropertyList sdcPolicy;
    private ArrayList<SearchDocument> searchDocuments = new ArrayList();
    private boolean showLinkPage = true;

    public SearchDocument(Indexer indexer, PropertyListCollection sdcRendering, M18NUtil m18n, Document document, int docid, float score) throws IOException {
        this.indexer = indexer;
        this.m18n = m18n;
        this.document = document;
        this.docid = docid;
        this.score = score;
        this.idparts = StringUtil.split(this.getId(), ";");
        if (sdcRendering != null) {
            this.sdcRender = sdcRendering.getIndexedPropertyList(this.idparts[1]);
            if (this.sdcRender != null) {
                this.resultColumns = this.sdcRender.getCollection("resultcolumns");
            }
        }
        this.sdcPolicy = indexer.getSDCPolicy(this.idparts[1]);
        if (this.sdcPolicy == null) {
            this.sdcPolicy = new PropertyList();
        }
        this.childSDC = this.sdcPolicy.getProperty("childsdc").equals("Y");
    }

    public int getDocid() {
        return this.docid;
    }

    public float getScore() {
        return this.score;
    }

    public String getId() {
        return StringUtil.replaceAll(this.document.get("id"), "__", "-");
    }

    public String getParentId() {
        return this.getType() + ";" + this.getParentSdcid() + ";" + this.getParentKeyid1() + ";" + this.getParentKeyid2() + ";" + this.getParentKeyid3() + (this.isAttachment() || this.isNote() ? ";" + this.getAttachmentNum() : "");
    }

    public boolean isChildSDC() {
        return this.childSDC;
    }

    public String getDescCol() {
        return this.document.get("desccol");
    }

    public String getCreatedt() {
        return this.getDateField("createdt");
    }

    public String getModdt() {
        return this.getDateField("moddt");
    }

    public String getDateField(String fieldid) {
        String datefield = this.document.get(fieldid);
        if (datefield != null) {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyyMMddhhmmss");
            try {
                sdf.parse(datefield);
                return this.m18n.format(sdf.getCalendar());
            }
            catch (ParseException e) {
                return datefield;
            }
        }
        return "";
    }

    public String getField(String fieldid) {
        return this.document.get(fieldid);
    }

    public String getType() {
        return this.idparts[0];
    }

    public boolean isSDI() {
        return this.idparts[0].equals("SDI");
    }

    public boolean isNote() {
        return this.idparts[0].equals("NOTE");
    }

    public boolean isAttachment() {
        return this.idparts[0].equals("ATTACHMENT");
    }

    public String getSdcid() {
        return this.idparts[1];
    }

    public String getParentSdcid() {
        return this.sdcPolicy.getProperty("parentsdcid");
    }

    public String getKeyid1() {
        return this.idparts[2];
    }

    public String getParentKeyid1() {
        String value = this.document.get(this.sdcPolicy.getProperty("parentkeycolid1"));
        return value != null ? StringUtil.replaceAll(value, "__", "-") : "";
    }

    public boolean hasKeyid2() {
        return this.idparts[3].length() > 0 && !this.idparts[3].equals("(null)");
    }

    public String getKeyid2() {
        return this.idparts[3];
    }

    public String getParentKeyid2() {
        String value = this.document.get(this.sdcPolicy.getProperty("parentkeycolid2"));
        return value != null ? StringUtil.replaceAll(value, "__", "-") : "";
    }

    public boolean hasKeyid3() {
        return this.idparts[4].length() > 0 && !this.idparts[4].equals("(null)");
    }

    public String getKeyid3() {
        return this.idparts[4];
    }

    public String getParentKeyid3() {
        String value = this.document.get(this.sdcPolicy.getProperty("parentkeycolid3"));
        return value != null ? StringUtil.replaceAll(value, "__", "-") : "";
    }

    public String getAttachmentNum() {
        return this.idparts[5];
    }

    public String getImage(String connectionId) {
        String image = "";
        if (this.sdcRender != null) {
            image = this.sdcRender.getProperty("imagesrc");
            boolean evaluated = false;
            if (image.startsWith("$G{")) {
                HashMap<String, HashMap> bindMap = new HashMap<String, HashMap>();
                HashMap<String, Boolean> type = new HashMap<String, Boolean>();
                type.put("sdi", new Boolean(this.isSDI()));
                type.put("note", new Boolean(this.isNote()));
                type.put("attachment", new Boolean(this.isAttachment()));
                bindMap.put("type", type);
                bindMap.put("primary", this.primary != null ? this.primary : new HashMap());
                bindMap.put("note", this.note != null ? this.note : new HashMap());
                bindMap.put("attachment", this.attachment != null ? this.attachment : new HashMap());
                try {
                    image = GroovyUtil.evaluate(image, bindMap);
                    if (!image.equals("(default)")) {
                        evaluated = true;
                    }
                }
                catch (SapphireException e) {
                    image = "";
                }
            }
            if (!evaluated) {
                if (this.isSDI()) {
                    image = this.sdcRender.getProperty("imagesrc");
                } else if (this.isAttachment()) {
                    if (this.attachment != null) {
                        FileType fileType = FileType.getFileType((String)this.attachment.get("filename"), connectionId);
                        image = fileType.getType().equals((Object)FileType.NamedType.IMAGE) ? "rc?command=image&attachment=" + this.getSdcid() + ";" + this.getKeyid1() + ";" + this.getKeyid2() + ";" + this.getKeyid3() + ";" + this.getAttachmentNum() + "&size=32" : "rc?command=image&image=" + fileType.getImageRefId() + "&size=32";
                    }
                    if (image.length() == 0) {
                        image = "WEB-CORE/imageref/basic_application_icons/text_and_documents/documents/32/document_attachment.png";
                    }
                } else if (this.isNote()) {
                    image = "WEB-CORE/imageref/finance_business_and_trade/office/notes/32/note.png";
                }
            }
        }
        return image.length() > 0 ? image : "WEB-CORE/images/png32/Document.png";
    }

    public void setKeycolid1(String keycolid1) {
        this.keycolid1 = keycolid1;
    }

    public void setPrimaryData(HashMap primary) {
        this.primary = primary;
    }

    public void setNote(HashMap note) {
        this.note = note;
    }

    public String getNoteValue(String columnid) {
        return this.note != null && this.note.get(columnid) != null ? (String)this.note.get(columnid) : "";
    }

    public boolean isShowLinkPage() {
        return this.showLinkPage;
    }

    public void setShowLinkPage(boolean showLinkPage) {
        this.showLinkPage = showLinkPage;
    }

    public void setAttachment(HashMap attachment) {
        this.attachment = attachment;
    }

    public String getValue(String columnid) {
        Object value;
        Object v0 = value = this.primary != null ? this.primary.get(columnid) : null;
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return StringUtil.replaceAll((String)this.primary.get(columnid), "\n", "<br/>");
        }
        if (value instanceof Calendar) {
            return this.m18n.format((Calendar)value);
        }
        if (value instanceof String) {
            return this.m18n.format((BigDecimal)value);
        }
        return this.primary != null && this.primary.get(columnid) != null ? StringUtil.replaceAll((String)this.primary.get(columnid), "\n", "<br/>") : "";
    }

    public int getColumnType(String columnid) {
        return this.primary != null && this.primary.get(columnid) != null && this.primary.get(columnid + "__type") != null ? (Integer)this.primary.get(columnid + "__type") : 0;
    }

    public String getMatchTitle() {
        return this.isSDI() ? this.sdcPolicy.getProperty("matchtitle") : (this.isNote() ? StringUtil.initCaps(this.sdcPolicy.getProperty("singular")) + " note match" : StringUtil.initCaps(this.sdcPolicy.getProperty("singular")) + " attachment match");
    }

    public void setMatchingColumn(String matchingColumnid) {
        if (this.matchingColumns == null) {
            this.matchingColumns = new ArrayList();
        }
        if (!this.matchingColumns.contains(matchingColumnid)) {
            this.matchingColumns.add(matchingColumnid);
        }
        if (matchingColumnid.equals(this.keycolid1)) {
            this.keyMatch = true;
        } else if (this.sdcRender != null) {
            PropertyList column;
            PropertyList propertyList = column = this.resultColumns != null ? this.resultColumns.getIndexedPropertyList(matchingColumnid) : null;
            if (column != null && column.getProperty("boost").equals("Y")) {
                if (this.boostMatchingColumns == null) {
                    this.boostMatchingColumns = new ArrayList();
                }
                this.boostMatchingColumns.add(matchingColumnid);
            }
        }
    }

    public boolean isKeyMatch() {
        return this.keyMatch;
    }

    public boolean isBoostMatch() {
        return this.boostMatchingColumns != null && this.boostMatchingColumns.size() > 0;
    }

    public ArrayList<String> getMatchingColumns() {
        return this.matchingColumns != null ? this.matchingColumns : new ArrayList<String>();
    }

    public boolean matches(SearchDocument searchDocument) {
        if (this.isChildSDC()) {
            if (searchDocument.isChildSDC()) {
                return this.getParentSdcid().equals(searchDocument.getParentSdcid()) && this.getParentKeyid1().equals(searchDocument.getParentKeyid1()) && this.getParentKeyid2().equals(searchDocument.getParentKeyid2()) && this.getParentKeyid3().equals(searchDocument.getParentKeyid3());
            }
            return this.getParentSdcid().equals(searchDocument.getSdcid()) && this.getParentKeyid1().equals(searchDocument.getKeyid1()) && this.getParentKeyid2().equals(searchDocument.getKeyid2()) && this.getParentKeyid3().equals(searchDocument.getKeyid3());
        }
        if (searchDocument.isChildSDC()) {
            return this.getSdcid().equals(searchDocument.getParentSdcid()) && this.getKeyid1().equals(searchDocument.getParentKeyid1()) && this.getKeyid2().equals(searchDocument.getParentKeyid2()) && this.getKeyid3().equals(searchDocument.getParentKeyid3());
        }
        return this.getSdcid().equals(searchDocument.getSdcid()) && this.getKeyid1().equals(searchDocument.getKeyid1()) && this.getKeyid2().equals(searchDocument.getKeyid2()) && this.getKeyid3().equals(searchDocument.getKeyid3());
    }

    public void addMatchingDocument(SearchDocument searchDocument) {
        this.searchDocuments.add(0, searchDocument);
    }

    public ArrayList<SearchDocument> getSearchDocuments() {
        return this.searchDocuments;
    }
}

