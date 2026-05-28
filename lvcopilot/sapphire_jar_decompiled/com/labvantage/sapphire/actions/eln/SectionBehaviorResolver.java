/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import java.math.BigDecimal;
import java.util.HashMap;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SectionBehaviorResolver
implements ELNConstants {
    private HashMap<String, Integer> sectionToSequence = new HashMap();
    private HashMap<String, Integer[]> itemToSequence = new HashMap();
    private QueryProcessor queryProcessor;

    public SectionBehaviorResolver(QueryProcessor queryProcessor) {
        this.queryProcessor = queryProcessor;
    }

    public void addSectionToSequence(String worksheetsectionid, String worksheetsectionversionid, int sectionSequence) {
        this.addSectionToSequence(worksheetsectionid, worksheetsectionversionid, "", sectionSequence);
    }

    public void addSectionToSequence(String worksheetsectionid, String worksheetsectionversionid, String loopkey, int sectionSequence) {
        String key = loopkey + ";" + worksheetsectionid + ";" + worksheetsectionversionid;
        this.sectionToSequence.put(key, sectionSequence);
    }

    public void addItemToSequence(String worksheetitemid, String worksheetitemvesionid, int sectionSequence, int itemsequeence) {
        this.addItemToSequence(worksheetitemid, worksheetitemvesionid, "", sectionSequence, itemsequeence);
    }

    public void addItemToSequence(String worksheetitemid, String worksheetitemvesionid, String loopkey, int sectionSequence, int itemsequeence) {
        String key = loopkey + ";" + worksheetitemid + ";" + worksheetitemvesionid;
        this.itemToSequence.put(key, new Integer[]{sectionSequence, itemsequeence});
    }

    public void resolveBehaviorReferences(String worksheetid, String worksheetversionid) {
        DataSet sections = this.queryProcessor.getPreparedSqlDataSet("SELECT worksheetsectionid, worksheetsectionversionid, sectionlevel, usersequence, options  FROM worksheetsection WHERE worksheetid=? AND worksheetversionid=?  order by usersequence", (Object[])new String[]{worksheetid, worksheetversionid}, true);
        DataSet items = this.queryProcessor.getPreparedSqlDataSet("SELECT worksheetitemid, worksheetitemversionid, worksheetsectionid, worksheetsectionversionid, usersequence  FROM worksheetitem WHERE worksheetid=? AND worksheetversionid=?  order by usersequence", (Object[])new String[]{worksheetid, worksheetversionid}, true);
        for (int i = 0; i < sections.size(); ++i) {
            String sectionid = sections.getValue(i, "worksheetsectionid");
            String sectionversionid = sections.getValue(i, "worksheetsectionversionid");
            try {
                String targetControlid;
                int currentlevel = Integer.parseInt(sections.getValue(i, "sectionlevel", "1"));
                int currentSequence = sections.getInt(i, "usersequence");
                if (currentlevel <= 0) continue;
                PropertyList wssOptions = new PropertyList();
                if (sections.getClob(i, "options", "{}").startsWith("{")) {
                    wssOptions.setJSONString(sections.getClob(i, "options"));
                } else {
                    wssOptions.setPropertyList(sections.getClob(i, "options"));
                }
                String loopkey = wssOptions.getProperty("sbr_loopkey");
                String behaviorType = wssOptions.getProperty("sectionbehaviortype", "none");
                String behaviorWhen = wssOptions.getProperty("sectionbehaviorwhen", "always");
                String behaviorWhenType = wssOptions.getProperty("sectionbehaviorwhentype");
                if (behaviorType.equals("none") || behaviorWhen.equals("always")) continue;
                if (behaviorWhenType.equals("section_status") || behaviorWhenType.equals("section_metadata")) {
                    String targetSectionid = wssOptions.getProperty("sectionid");
                    if (!targetSectionid.startsWith("sectionid")) continue;
                    Integer seq = this.findReferencedSection("OUTER", currentSequence, targetSectionid.substring("sectionid".length() + 1));
                    if (seq == null) {
                        seq = this.findReferencedSection(loopkey, currentSequence, targetSectionid.substring("sectionid".length() + 1));
                    }
                    if (seq == null) continue;
                    HashMap<String, BigDecimal> sectionFind = new HashMap<String, BigDecimal>();
                    sectionFind.put("usersequence", new BigDecimal(seq));
                    int sectionrow = sections.findRow(sectionFind);
                    if (sectionrow < 0) continue;
                    String thisKey = sections.getString(sectionrow, "worksheetsectionid") + ";" + sections.getString(sectionrow, "worksheetsectionversionid");
                    wssOptions.setProperty("sectionid", "sectionid=" + thisKey);
                    this.queryProcessor.execPreparedUpdate("UPDATE worksheetsection SET options=? WHERE worksheetsectionid=? AND worksheetsectionversionid=?", new String[]{wssOptions.toXMLString(), sectionid, sectionversionid});
                    continue;
                }
                if (!behaviorWhenType.equals("control_status") && !behaviorWhenType.equals("control_dataavailability") && !behaviorWhenType.equals("control_metadata") || !(targetControlid = wssOptions.getProperty("controlid")).startsWith("controlid")) continue;
                String key = loopkey + ";" + targetControlid.substring("controlid".length() + 1);
                Integer[] seq = this.itemToSequence.get(key);
                if (seq == null) {
                    seq = this.itemToSequence.get("OUTER;" + targetControlid.substring("controlid".length() + 1));
                }
                if (seq == null) continue;
                HashMap<String, BigDecimal> sectionFind = new HashMap<String, BigDecimal>();
                sectionFind.put("usersequence", new BigDecimal(seq[0]));
                int sectionrow = sections.findRow(sectionFind);
                if (sectionrow < 0) continue;
                HashMap<String, Object> itemFind = new HashMap<String, Object>();
                itemFind.put("worksheetsectionid", sections.getString(sectionrow, "worksheetsectionid"));
                itemFind.put("worksheetsectionversionid", sections.getString(sectionrow, "worksheetsectionversionid"));
                itemFind.put("usersequence", new BigDecimal(seq[1]));
                int itemrow = items.findRow(itemFind);
                if (itemrow < 0) continue;
                String worksheetitemid = items.getString(itemrow, "worksheetitemid");
                String worksheetitemversionid = items.getString(itemrow, "worksheetitemversionid");
                wssOptions.setProperty("controlid", "controlid=" + worksheetitemid + ";" + worksheetitemversionid);
                this.queryProcessor.execPreparedUpdate("UPDATE worksheetsection SET options=? WHERE worksheetsectionid=? AND worksheetsectionversionid=?", new String[]{wssOptions.toXMLString(), sectionid, sectionversionid});
                continue;
            }
            catch (Exception e) {
                Trace.logWarn("Failed to determine section behavior: " + e.getMessage());
            }
        }
    }

    private Integer findReferencedSection(String loopkey, int currentSequence, String targetSectionid) {
        String key = loopkey + ";" + targetSectionid;
        Integer seq = this.sectionToSequence.get(key);
        if (seq == null) {
            int min = -1;
            for (String tempkey : this.sectionToSequence.keySet()) {
                Integer tempseq;
                if (!tempkey.endsWith(";" + targetSectionid) || (tempseq = this.sectionToSequence.get(tempkey)) == null || tempseq <= min || tempseq >= currentSequence) continue;
                min = tempseq;
            }
            if (min >= 0) {
                seq = min;
            }
        }
        return seq;
    }
}

