/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseGenerateWorksheet;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class GenerateTestMethodWorksheet
extends BaseGenerateWorksheet
implements sapphire.action.GenerateTestMethodWorksheet {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        super.processAction(properties);
        String sdiworkitemid = properties.getProperty("sdiworkitemid");
        this.loadWorkItem(sdiworkitemid);
        this.loadTemplate("WorkItem", this.workitemid, this.workitemversionid, "(null)", properties.getProperty("worksheetrule", "default"));
        DataSet wiSDIS = this.workItemSDIs(sdiworkitemid);
        DataSet sdis = new DataSet();
        sdis.addColumn("sdcid", 0);
        sdis.addColumn("keyid1", 0);
        sdis.addColumn("keyid2", 0);
        sdis.addColumn("keyid3", 0);
        for (int i = 0; i < wiSDIS.getRowCount(); ++i) {
            String sdcid = wiSDIS.getValue(i, "sdcid");
            String keyid1 = wiSDIS.getValue(i, "keyid1");
            String keyid2 = wiSDIS.getValue(i, "keyid2");
            String keyid3 = wiSDIS.getValue(i, "keyid3");
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("sdcid", sdcid);
            map.put("keyid1", keyid1);
            map.put("keyid2", keyid2);
            map.put("keyid3", keyid3);
            if (sdis.findRow(map) >= 0) continue;
            int r = sdis.addRow();
            sdis.setString(r, "sdcid", sdcid);
            sdis.setString(r, "keyid1", keyid1);
            sdis.setString(r, "keyid2", keyid2);
            sdis.setString(r, "keyid3", keyid3);
        }
        int sdiCount = sdis.getRowCount();
        if (this.sdiperworksheet > 0 && this.sdiperworksheet < sdiCount) {
            StringBuffer returnWorksheetId = new StringBuffer();
            StringBuffer returnWorksheetVersionId = new StringBuffer();
            int wiIndex = 0;
            while (wiIndex < sdiCount) {
                StringBuffer sdiworkitemids = new StringBuffer();
                for (int i = 0; i < this.sdiperworksheet; ++i) {
                    String sdcid = sdis.getValue(wiIndex, "sdcid");
                    String keyid1 = sdis.getValue(wiIndex, "keyid1");
                    String keyid2 = sdis.getValue(wiIndex, "keyid2");
                    String keyid3 = sdis.getValue(wiIndex, "keyid3");
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("sdcid", sdcid);
                    map.put("keyid1", keyid1);
                    map.put("keyid2", keyid2);
                    map.put("keyid3", keyid3);
                    DataSet sdiwi = wiSDIS.getFilteredDataSet(map);
                    if (sdiwi.getRowCount() > 0) {
                        sdiworkitemids.append(";").append(sdiwi.getColumnValues("sdiworkitemid", ";"));
                    }
                    if (++wiIndex == sdiCount) break;
                }
                String[] worksheet = this.createWorkSheet(sdiworkitemids.substring(1));
                returnWorksheetId.append(";").append(worksheet[0]);
                returnWorksheetVersionId.append(";").append(worksheet[1]);
                this.renewActionBlock();
            }
            properties.setProperty("worksheetid", returnWorksheetId.substring(1));
            properties.setProperty("worksheetversionid", returnWorksheetVersionId.substring(1));
        } else {
            String[] worksheet = this.createWorkSheet(sdiworkitemid);
            properties.setProperty("worksheetid", worksheet[0]);
            properties.setProperty("worksheetversionid", worksheet[1]);
        }
    }

    private DataSet workItemSDIs(String sdiworkitemid) throws SapphireException {
        List<String> list = this.getSDIWIIDListBySupportedLimit(sdiworkitemid);
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        DataSet dsSDIS = new DataSet();
        for (String sdiwiid : list) {
            sql.setLength(0);
            safeSQL.reset();
            DataSet ds = new DataSet();
            try {
                sql.append(this.database.isOracle() ? "SELECT  sdcid, keyid1, keyid2, keyid3, sdiworkitemid FROM sdiworkitem, TABLE (LV_OrderTab (" + safeSQL.addVar(sdiwiid) + ")) t " : "SELECT  sdcid, keyid1, keyid2, keyid3, sdiworkitemid FROM sdiworkitem, LV_OrderTab (" + safeSQL.addVar(sdiwiid) + ",default,default,default) t ");
                sql.append(" WHERE sdiworkitem.sdiworkitemid = t.id_value ORDER BY t.seq_value");
                this.database.createPreparedResultSet("workitemsamples", sql.toString(), safeSQL.getValues());
                if (dsSDIS.isEmpty()) {
                    dsSDIS.setResultSet(this.database.getResultSet("workitemsamples"));
                    continue;
                }
                ds.setResultSet(this.database.getResultSet("workitemsamples"));
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    int r = dsSDIS.addRow();
                    dsSDIS.setValue(r, "sdcid", ds.getValue(i, "sdcid", ""));
                    dsSDIS.setValue(r, "keyid1", ds.getValue(i, "keyid1", ""));
                    dsSDIS.setValue(r, "keyid2", ds.getValue(i, "keyid2", ""));
                    dsSDIS.setValue(r, "keyid3", ds.getValue(i, "keyid3", ""));
                    dsSDIS.setValue(r, "sdiworkitemid", ds.getValue(i, "sdiworkitemid", ""));
                }
            }
            catch (SapphireException e) {
                throw new SapphireException(e.getMessage());
            }
            finally {
                this.database.closeStatement("workitemsamples");
                this.database.closeResultSet("workitemsamples");
            }
        }
        return dsSDIS;
    }

    private String[] createWorkSheet(String sdiwi) throws SapphireException {
        DataSet ds;
        SafeSQL safeSQL = new SafeSQL();
        this.database.createPreparedResultSet("SELECT sdiwi2.sdiworkitemid FROM sdiworkitem sdiwi1, sdiworkitem sdiwi2 WHERE sdiwi1.sdiworkitemid IN (" + safeSQL.addIn(sdiwi, ";") + ") AND sdiwi1.workitemtypeflag = 'P' AND sdiwi2.workitemtypeflag = 'W' AND sdiwi2.groupid IS NOT NULL   AND sdiwi1.sdcid = sdiwi2.sdcid AND sdiwi1.keyid1 = sdiwi2.keyid1 AND sdiwi1.keyid2 = sdiwi2.keyid2 AND sdiwi1.keyid3 = sdiwi2.keyid3", safeSQL.getValues());
        while (this.database.getNext()) {
            sdiwi = sdiwi + ";" + this.database.getValue("sdiworkitemid");
        }
        TranslationProcessor tp = this.getTranslationProcessor();
        safeSQL.reset();
        if (this.authorid.length() == 0) {
            if ("A".equals(this.authorflag)) {
                this.database.createPreparedResultSet("getassignedanalyst", "SELECT  DISTINCT s_assignedanalyst FROM sdiworkitem WHERE sdiworkitemid IN (" + safeSQL.addIn(sdiwi, ";") + ")", safeSQL.getValues());
                ds = new DataSet(this.database.getResultSet("getassignedanalyst"));
                String assignedAnalyst = "";
                if (ds.getRowCount() > 1) {
                    throw new SapphireException(tp.translate("To set assigned analyst as worksheet author, selected items should have the same 'Assigned Analyst'."));
                }
                assignedAnalyst = ds.getValue(0, "s_assignedanalyst");
                if (assignedAnalyst.length() > 0) {
                    this.authorid = assignedAnalyst;
                }
            }
        } else {
            this.database.createPreparedResultSet("getassignedanalyst", "SELECT * FROM sdiworkitem WHERE sdiworkitemid IN (" + safeSQL.addIn(sdiwi, ";") + ")", safeSQL.getValues());
            ds = new DataSet(this.database.getResultSet("getassignedanalyst"));
            DataSet updateAnalyst = new DataSet();
            for (int i = 0; i < ds.getRowCount(); ++i) {
                if (this.authorid.equals(ds.getValue(i, "s_assignedanalyst"))) continue;
                updateAnalyst.copyRow(ds, i, 1);
            }
            if (updateAnalyst.getRowCount() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", updateAnalyst.getValue(0, "sdcid"));
                props.setProperty("keyid1", updateAnalyst.getColumnValues("keyid1", ";"));
                props.setProperty("keyid2", updateAnalyst.getColumnValues("keyid2", ";"));
                props.setProperty("keyid3", updateAnalyst.getColumnValues("keyid3", ";"));
                props.setProperty("workitemid", updateAnalyst.getColumnValues("workitemid", ";"));
                props.setProperty("workiteminstance", updateAnalyst.getColumnValues("workiteminstance", ";"));
                props.setProperty("s_assignedanalyst", this.authorid);
                props.setProperty("postworksheetcreation", "Y");
                this.getActionProcessor().processAction("EditSDIWorkItem", "1", props);
            }
        }
        this.database.closeResultSet("getassignedanalyst");
        this.startWorksheet("[workitemid] Worksheet [currentdate]-[template_seq;00000]");
        this.addWorksheetSDIs("WorkItem", this.workitemid, this.workitemversionid, "");
        this.loadWorkItemSamples(sdiwi);
        this.generateSections();
        String[] worksheet = this.finalizeWorksheet();
        return worksheet;
    }

    @Override
    protected DataSet getAttributeControlAttributes(PropertyList config) {
        return this.getWorkItemAttributeControlAttributes(config);
    }

    @Override
    protected PropertyList getWorksheetNameSubstitutions() {
        PropertyList substitutions = new PropertyList();
        substitutions.setProperty("sdiworkitemid", this.sdiworkitemid);
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
}

