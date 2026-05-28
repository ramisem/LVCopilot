/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseGenerateWorksheet;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class GenerateWorkorderWorksheet
extends BaseGenerateWorksheet
implements sapphire.action.GenerateWorkorderWorksheet {
    private String workorderid;
    private DataSet workorder;
    private DataSet instrument;
    private String certificationInterval = "";
    private String certificationType = "";
    private String templateSDCId = "";

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        int findRow;
        super.processAction(properties);
        this.workorderid = properties.getProperty("workorderid");
        if (this.templateid != null && this.templateid.length() > 0 && (this.templateversionid == null || this.templateversionid.length() == 0 || "C".equals(this.templateversionid))) {
            this.templateversionid = GenerateWorkorderWorksheet.resolveVersion(this.getQueryProcessor(), this.templateid, "", "worksheet");
        }
        this.workorder = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM workorder WHERE workorderid = ?", new Object[]{this.workorderid});
        if (this.workorder.size() != 1) throw new SapphireException((this.workorder.size() > 1 ? "Multiple workorders found" : "No workorder found") + " for the passed in workorderid: " + this.workorderid);
        if (!this.workorder.getValue(0, "sourcesdcid").equals("Instrument")) return;
        this.instrument = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM instrument WHERE instrumentid = ?", new Object[]{this.workorder.getValue(0, "sourcekeyid1")});
        this.certificationInterval = this.workorder.getValue(0, "certificationinterval");
        DataSet workorderProperty = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM workorderproperty WHERE workorderid = ?", new Object[]{this.workorderid});
        if (workorderProperty.getRowCount() > 0 && (findRow = workorderProperty.findRow("propertyid", "certificationtype")) > -1) {
            this.certificationType = workorderProperty.getValue(findRow, "propertyvalue");
        }
        if (this.instrument.size() != 1) throw new SapphireException("No instrument found for the passed in workorderid: " + this.workorderid);
        String sdcid = null;
        String keyid1 = null;
        String keyid2 = "(null)";
        if (this.instrument.getValue(0, "instrumentmodelid").length() > 0) {
            sdcid = "LV_InstrumentModel";
            keyid1 = this.instrument.getValue(0, "instrumentmodelid");
            keyid2 = this.instrument.getValue(0, "instrumenttype");
        } else if (this.instrument.getValue(0, "instrumenttype").length() > 0) {
            sdcid = "LV_InstrumentType";
            keyid1 = this.instrument.getValue(0, "instrumenttype");
        }
        if (sdcid == null || keyid1 == null || keyid1.length() <= 0) throw new SapphireException("No model or type found for instrument: " + this.instrument.getValue(0, "instrumentid"));
        try {
            if (this.instrument.getValue(0, "instrumentmodelid").length() > 0) {
                this.loadTemplate(sdcid, keyid1, keyid2, "(null)", this.certificationInterval);
            } else if (this.instrument.getValue(0, "instrumenttype").length() > 0) {
                this.loadTemplate(sdcid, keyid1, "(null)", "(null)", this.certificationInterval);
            }
            this.templateSDCId = sdcid;
        }
        catch (SapphireException e) {
            if (this.instrument.getValue(0, "instrumentmodelid").length() <= 0) throw e;
            this.loadTemplate("LV_InstrumentType", this.instrument.getValue(0, "instrumenttype"), "(null)", "(null)", this.certificationInterval);
            this.templateSDCId = "LV_InstrumentType";
        }
        if ("A".equals(this.authorflag) && this.authorid.length() == 0) {
            this.authorid = this.workorder.getValue(0, "assignedto");
        }
        this.startWorksheet("[workorderid] Worksheet [currentdate]-[template_seq;00000]");
        this.addWorksheetSDIs("WorkOrderSDC", this.workorderid, "", "");
        this.addWorksheetSDIs("Instrument", this.instrument.getValue(0, "instrumentid"), "", "");
        if (sdcid.equals("LV_InstrumentModel")) {
            this.addWorksheetSDIs("LV_InstrumentModel", keyid1, keyid2, "");
            this.addWorksheetSDIs("LV_InstrumentType", keyid2, "", "");
        } else if (sdcid.equals("LV_InstrumentType")) {
            this.addWorksheetSDIs("LV_InstrumentType", keyid1, "", "");
        }
        StringBuffer sdiworkitemid = new StringBuffer();
        this.database.createPreparedResultSet("SELECT sdiworkitemid FROM sdiworkitem, s_sample WHERE sdcid = 'Sample' AND keyid1 = s_sample.s_sampleid AND workorderid = ?", new Object[]{this.workorderid});
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
            this.loadWorkOrderSamples(this.workorderid);
        }
        this.generateSections();
        String[] worksheet = this.finalizeWorksheet();
        properties.setProperty("worksheetid", worksheet[0]);
        properties.setProperty("worksheetversionid", worksheet[1]);
    }

    @Override
    protected DataSet getAttributeControlAttributes(PropertyList config) {
        String sourcerelation = config.getProperty("sourcerelation", "WorkItem");
        if (sourcerelation.equalsIgnoreCase("WorkOrderSDC")) {
            if (this.instrument != null && this.instrument.size() == 1) {
                String sdcid = null;
                String keyid1 = null;
                String keyid2 = "(null)";
                if (this.templateSDCId != null && this.templateSDCId.length() > 0) {
                    if ("LV_InstrumentModel".equals(this.templateSDCId)) {
                        sdcid = "LV_InstrumentModel";
                        keyid1 = this.instrument.getValue(0, "instrumentmodelid");
                        keyid2 = this.instrument.getValue(0, "instrumenttype");
                    } else if ("LV_InstrumentType".equals(this.templateSDCId)) {
                        sdcid = "LV_InstrumentType";
                        keyid1 = this.instrument.getValue(0, "instrumenttype");
                    }
                    String attributeid = config.getProperty("attributeid");
                    String worksheetcontext = config.getProperty("worksheetcontext");
                    StringBuffer sql = new StringBuffer();
                    ArrayList<String> params = new ArrayList<String>();
                    sql.append("SELECT * FROM sdiattribute WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND attributesdcid = ? ");
                    params.add(sdcid);
                    params.add(keyid1);
                    params.add(keyid2);
                    params.add("LV_WorksheetItem");
                    if (attributeid.length() > 0) {
                        sql.append(" AND attributeid = ?");
                        params.add(attributeid);
                    }
                    if (worksheetcontext.length() > 0) {
                        sql.append(" AND worksheetcontext = ?");
                        params.add(worksheetcontext);
                    }
                    if (this.certificationInterval != null && this.certificationInterval.length() > 0) {
                        sql.append(" AND copydowncontext = ?");
                        params.add(this.certificationInterval);
                    } else {
                        sql.append(" AND copydowncontext IS NULL");
                    }
                    return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), params.toArray());
                }
            }
            return null;
        }
        return this.getWorkItemAttributeControlAttributes(config);
    }

    @Override
    protected PropertyList getWorksheetNameSubstitutions() {
        PropertyList substitutions = new PropertyList();
        substitutions.setProperty("workorderid", this.workorderid);
        substitutions.setProperty("instrumentid", this.instrument != null && this.instrument.size() == 1 ? this.instrument.getValue(0, "instrumentid") : "");
        substitutions.setProperty("certificationtype", this.certificationType);
        substitutions.setProperty("certificationinterval", this.certificationInterval);
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
        if (this.workorder != null && token.startsWith("workorder.")) {
            return this.workorder.getValue(0, token.substring(10));
        }
        if (token.equalsIgnoreCase("workorderid")) {
            return this.workorderid;
        }
        if (token.equalsIgnoreCase("instrumentid")) {
            return this.instrument != null && this.instrument.size() == 1 ? this.instrument.getValue(0, "instrumentid") : "";
        }
        if (this.instrument != null && this.instrument.size() == 1 && token.startsWith("instrument.")) {
            return this.instrument.getValue(0, token.substring(11));
        }
        if (token.startsWith("certificationtype")) {
            return this.certificationType;
        }
        if (token.startsWith("certificationinterval")) {
            return this.certificationInterval;
        }
        return "";
    }
}

