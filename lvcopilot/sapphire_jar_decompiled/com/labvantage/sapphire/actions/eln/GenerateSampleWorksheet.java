/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseGenerateWorksheet;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIList;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class GenerateSampleWorksheet
extends BaseGenerateWorksheet
implements sapphire.action.GenerateSampleWorksheet {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    protected DataSet samples;
    protected String sampleid;
    protected DataSet templatelink;
    protected String sourceSDCId;
    protected String sourceKeyid1;
    protected String sourceKeyid2 = "(null)";
    protected String sourceKeyid3 = "(null)";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        super.processAction(properties);
        this.sampleid = properties.getProperty("sampleid");
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append(this.database.isOracle() ? "SELECT *  FROM s_sample s, TABLE (LV_OrderTab (" + safeSQL.addVar(this.sampleid) + ")) t " : "SELECT *  FROM s_sample s, LV_OrderTab (" + safeSQL.addVar(this.sampleid) + ",default,default,default) t ").append(" WHERE s.s_sampleid = t.id_value ORDER BY t.seq_value");
        this.samples = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        int sampleCount = this.samples.getRowCount();
        QueryProcessor qp = this.getQueryProcessor();
        if (!this.preview) {
            String[] fkeylinks = new String[]{"Sample Sub Type", "SampleTypeId"};
            for (int i = 0; i < fkeylinks.length; ++i) {
                DataSet sdiworksheetrule;
                this.templatelink = qp.getPreparedSqlDataSet("SELECT * FROM sdclink WHERE sdcid = ? AND linkid = ?", (Object[])new String[]{"Sample", fkeylinks[i]});
                this.sourceKeyid2 = "(null)";
                this.sourceKeyid3 = "(null)";
                if (this.templatelink.getRowCount() <= 0) continue;
                this.sourceSDCId = this.templatelink.getValue(0, "linksdcid");
                String fkeycolumn1 = this.templatelink.getValue(0, "sdccolumnid");
                String fkeycolumn2 = this.templatelink.getValue(0, "sdccolumnid2");
                String fkeycolumn3 = this.templatelink.getValue(0, "sdccolumnid3");
                this.sourceKeyid1 = this.samples.getValue(0, fkeycolumn1);
                if (this.sourceKeyid1.length() == 0) continue;
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(fkeycolumn1, this.sourceKeyid1);
                DataSet filterRows = this.samples.getFilteredDataSet(map);
                if (filterRows.getRowCount() < sampleCount) continue;
                if (fkeycolumn2 != null && fkeycolumn2.length() > 0) {
                    this.sourceKeyid2 = this.samples.getValue(0, fkeycolumn2);
                    if (this.sourceKeyid2.length() == 0) continue;
                    map.put(fkeycolumn2, this.sourceKeyid2);
                    filterRows = this.samples.getFilteredDataSet(map);
                    if (filterRows.getRowCount() < sampleCount) continue;
                }
                if (fkeycolumn3 != null && fkeycolumn3.length() > 0) {
                    this.sourceKeyid3 = this.samples.getValue(0, fkeycolumn3);
                    if (this.sourceKeyid3.length() == 0) continue;
                    map.put(fkeycolumn3, this.sourceKeyid3);
                    filterRows = this.samples.getFilteredDataSet(map);
                    if (filterRows.getRowCount() < sampleCount) continue;
                }
                if ((sdiworksheetrule = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM sdiworksheetrule WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND worksheetrule = ?", new Object[]{this.sourceSDCId, this.sourceKeyid1, this.sourceKeyid2, this.sourceKeyid3, properties.getProperty("worksheetrule", "default")})).getRowCount() == 1) break;
            }
        }
        if (sampleCount > 0) {
            this.loadTemplate(this.sourceSDCId, this.sourceKeyid1, this.sourceKeyid2, this.sourceKeyid3, properties.getProperty("worksheetrule", "default"));
            if (this.sdiperworksheet > 0 && this.sdiperworksheet < sampleCount) {
                StringBuffer returnWorksheetId = new StringBuffer();
                StringBuffer returnWorksheetVersionId = new StringBuffer();
                DataSet allSamples = this.samples.copy();
                int sampleIndex = 0;
                while (sampleIndex < sampleCount) {
                    DataSet wsSamples = new DataSet();
                    StringBuffer wsSampleIds = new StringBuffer();
                    for (int s = 0; s < this.sdiperworksheet; ++s) {
                        wsSampleIds.append(";").append(allSamples.getValue(sampleIndex, "s_sampleid"));
                        wsSamples.copyRow(allSamples, sampleIndex, 1);
                        if (++sampleIndex == sampleCount) break;
                    }
                    this.samples = wsSamples;
                    this.sampleid = wsSampleIds.substring(1);
                    String[] worksheet = this.createWorkSheet();
                    returnWorksheetId.append(";").append(worksheet[0]);
                    returnWorksheetVersionId.append(";").append(worksheet[1]);
                    this.renewActionBlock();
                }
                properties.setProperty("worksheetid", returnWorksheetId.substring(1));
                properties.setProperty("worksheetversionid", returnWorksheetVersionId.substring(1));
            } else {
                String[] worksheet = this.createWorkSheet();
                properties.setProperty("worksheetid", worksheet[0]);
                properties.setProperty("worksheetversionid", worksheet[1]);
            }
        } else {
            tokenMap.clear();
            tokenMap.put("sampleid", this.sampleid);
            throw new SapphireException(tp.translate("Samples not found for the passed in sample ids: [sampleid]") + this.sampleid);
        }
    }

    protected String[] createWorkSheet() throws SapphireException {
        this.startWorksheet("Sample Worksheet [currentdate]-[template_seq;00000]");
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sdiworkitemid = new StringBuffer();
        StringBuffer sql = new StringBuffer();
        sql.append(this.database.isOracle() ? "SELECT  sdiworkitemid FROM sdiworkitem, s_sample, TABLE (LV_OrderTab (" + safeSQL.addVar(this.sampleid) + ")) t " : "SELECT sdiworkitemid FROM sdiworkitem, s_sample, LV_OrderTab (" + safeSQL.addVar(this.sampleid) + ",default,default,default) t ");
        sql.append(" WHERE sdiworkitem.sdcid = 'Sample' AND sdiworkitem.keyid1 = s_sample.s_sampleid AND s_sample.s_sampleid = t.id_value ORDER BY t.seq_value");
        this.database.createPreparedResultSet(sql.toString(), safeSQL.getValues());
        while (this.database.getNext()) {
            sdiworkitemid.append(";").append(this.database.getValue("sdiworkitemid"));
        }
        if (sdiworkitemid.length() > 0) {
            this.loadWorkItems(sdiworkitemid.substring(1));
            this.loadWorkItemSamples(sdiworkitemid.substring(1));
        } else {
            this.workitemid = "";
            this.workitemversionid = "";
            this.sdiworkitem = new DataSet();
            this.sdiworkitemitemworkitems = new DataSet();
            this.sdiworkitemitemparamlists = new DataSet();
            this.loadSamples(this.samples);
        }
        this.generateSections();
        String[] worksheet = this.finalizeWorksheet();
        return worksheet;
    }

    @Override
    protected DataSet getAttributeControlAttributes(PropertyList config) {
        String sourcerelation = config.getProperty("sourcerelation", "WorkItem");
        if (sourcerelation.equalsIgnoreCase("Sample")) {
            if (this.templatelink != null && this.templatelink.getRowCount() == 1 && this.sourceSDCId != null && this.sourceKeyid1 != null && this.sourceKeyid1.length() > 0) {
                String attributeid = config.getProperty("attributeid");
                String worksheetcontext = config.getProperty("worksheetcontext");
                StringBuffer sql = new StringBuffer();
                ArrayList<String> params = new ArrayList<String>();
                sql.append("SELECT * FROM sdiattribute WHERE sdcid = ? AND keyid1 = ? ");
                params.add(this.sourceSDCId);
                params.add(this.sourceKeyid1);
                if (this.sourceKeyid2 != null && this.sourceKeyid2.length() > 0 && !"(null)".equals(this.sourceKeyid2)) {
                    sql.append(" AND keyid2 = ? ");
                    params.add(this.sourceKeyid2);
                }
                if (this.sourceKeyid3 != null && this.sourceKeyid3.length() > 0 && !"(null)".equals(this.sourceKeyid3)) {
                    sql.append(" AND keyid3 = ? ");
                    params.add(this.sourceKeyid3);
                }
                sql.append(" AND attributesdcid = ? ");
                params.add("LV_WorksheetItem");
                if (attributeid.length() > 0) {
                    sql.append(" AND attributeid = ?");
                    params.add(attributeid);
                }
                if (worksheetcontext.length() > 0) {
                    sql.append(" AND worksheetcontext = ?");
                    params.add(worksheetcontext);
                }
                return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), params.toArray());
            }
            return null;
        }
        return this.getWorkItemAttributeControlAttributes(config);
    }

    @Override
    protected PropertyList getWorksheetNameSubstitutions() {
        PropertyList substitutions = new PropertyList();
        substitutions.setProperty("sampleid", this.sampleid);
        return substitutions;
    }

    @Override
    protected String getSubstitution(String token) {
        if (this.sdiworkitem != null && token.startsWith("sdiworkitem.")) {
            return this.sdiworkitem.getValue(0, token.substring(12));
        }
        if (this.repeatset != null && token.startsWith("sdiworkitemitem.")) {
            return this.repeatset.getValue(this.repeatsetRow, token.substring(16));
        }
        if (this.repeatset != null && token.equalsIgnoreCase("workitemid")) {
            return this.repeatset.getValue(this.repeatsetRow, "itemsdcid").equals("WorkItem") ? this.repeatset.getValue(this.repeatsetRow, "itemkeyid1") : this.repeatset.getValue(this.repeatsetRow, "workitemid");
        }
        if (this.repeatset != null && token.equalsIgnoreCase("paramlistid")) {
            return this.repeatset.getValue(this.repeatsetRow, "itemkeyid1");
        }
        if (this.repeatset != null && token.equalsIgnoreCase("paramlistversionid")) {
            return this.repeatset.getValue(this.repeatsetRow, "itemkeyid2");
        }
        if (this.repeatset != null && token.equalsIgnoreCase("variantid")) {
            return this.repeatset.getValue(this.repeatsetRow, "itemkeyid3");
        }
        if (this.repeatset != null && token.equalsIgnoreCase("dataset")) {
            return this.repeatset.getValue(this.repeatsetRow, "iteminstance");
        }
        if (token.equalsIgnoreCase("sdiworkitemid")) {
            return this.sdiworkitemid;
        }
        if (token.equalsIgnoreCase("workitemid") || token.equalsIgnoreCase("groupworkitemid")) {
            return this.workitemid;
        }
        if (token.equalsIgnoreCase("workitemversionid") || token.equalsIgnoreCase("groupworkitemversionid")) {
            return this.workitemversionid;
        }
        return "";
    }

    protected void loadSamples(DataSet samples) throws SapphireException {
        SDIList sdiList = new SDIList();
        for (int i = 0; i < samples.getRowCount(); ++i) {
            sdiList.setSdcid("Sample");
            sdiList.addSDI(samples.getValue(i, "s_sampleid"), "(null)", "(null)");
        }
        if (sdiList.size() > 0) {
            this.addWorksheetSDIs(sdiList.getSdcid(), sdiList.getKeyid1(), sdiList.getKeyid2(), sdiList.getKeyid3());
        }
    }
}

