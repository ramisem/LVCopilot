/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseGenerateWorksheet;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class GenerateReagentWorksheet
extends BaseGenerateWorksheet
implements sapphire.action.GenerateReagentWorksheet {
    private String reagentlotid;
    private String reagenttypeid;
    private String reagenttypeversionid;
    private final String ReagentLotSDC = "LV_ReagentLot";
    private final String ReagentTypeSDC = "LV_ReagentType";
    private DataSet reagentqualitysampleparamlist;
    private DataSet reagentlotstages;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        super.processAction(properties);
        this.reagentlotid = properties.getProperty("reagentlotid");
        TranslationProcessor tp = this.getTranslationProcessor();
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("select DISTINCT rt.reagenttypeid,rt.reagenttypeversionid from reagenttype rt,reagentlot rl where rl.reagenttypeid=rt.reagenttypeid ");
        sql.append(" and rl.reagenttypeversionid=rt.reagenttypeversionid");
        sql.append(" AND rl.reagentlotid in (").append(safeSQL.addIn(this.reagentlotid, ";")).append(")");
        DataSet reagenttype = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (reagenttype.size() == 1) {
            this.reagenttypeid = reagenttype.getValue(0, "reagenttypeid");
            this.reagenttypeversionid = reagenttype.getValue(0, "reagenttypeversionid");
            this.loadReagentQualitySampleWorkItem();
            this.loadReagentLotStage();
            this.loadTemplate("LV_ReagentType", this.reagenttypeid, this.reagenttypeversionid, "(null)", properties.getProperty("worksheetrule", "default"));
            this.updateAuthor();
            this.startWorksheet("[reagentlotid] Worksheet [currentdate]-[template_seq;00000]");
            this.addWorksheetSDIs("LV_ReagentLot", this.reagentlotid, "", "");
            if (this.workitemid != null && this.workitemid.length() > 0) {
                this.addWorksheetSDIs("WorkItem", this.workitemid, this.workitemversionid, "");
                this.loadWorkItemSamples(this.sdiworkitemid);
            }
        } else {
            HashMap<String, String> token = new HashMap<String, String>();
            token.put("reagentlotid", this.reagentlotid);
            String msg = tp.translate((reagenttype.size() > 1 ? "Multiple Reagent Types found" : "No Reagent Type found") + " for the passed in ReagentlotId: [reagentlotid]", token);
            throw new SapphireException(msg);
        }
        this.reagentqualitysampleparamlist = this.getQueryProcessor().getPreparedSqlDataSet("select sdidata.paramlistid,sdidata.paramlistversionid,sdidata.variantid,sdidata.dataset from s_sample,sdidata where s_sample.reagentlotid=?  and sdidata.keyid1=s_sample.s_sampleid and s_sample.samplestatus!='Cancelled'  order by sdidata.usersequence", new Object[]{this.reagentlotid});
        this.generateSections();
        String[] worksheet = this.finalizeWorksheet();
        properties.setProperty("worksheetid", worksheet[0]);
        properties.setProperty("worksheetversionid", worksheet[1]);
    }

    private void updateAuthor() throws SapphireException {
        if (this.authorid.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            this.database.createPreparedResultSet("getassignedanalyst", "SELECT * FROM reagentlot WHERE reagentlotid IN (" + safeSQL.addIn(this.reagentlotid, ";") + ")", safeSQL.getValues());
            DataSet ds = new DataSet(this.database.getResultSet("getassignedanalyst"));
            DataSet updateAnalyst = new DataSet();
            for (int i = 0; i < ds.getRowCount(); ++i) {
                if (this.authorid.equals(ds.getValue(i, "assignedanalyst"))) continue;
                updateAnalyst.copyRow(ds, i, 1);
            }
            if (updateAnalyst.getRowCount() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_ReagentLot");
                props.setProperty("keyid1", updateAnalyst.getColumnValues("reagentlotid", ";"));
                props.setProperty("assignedanalyst", this.authorid);
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
        }
    }

    private void loadReagentQualitySampleWorkItem() {
        SafeSQL safeSQL = new SafeSQL();
        DataSet workitems = this.getQueryProcessor().getPreparedSqlDataSet("select DISTINCT sdiworkitem.sdiworkitemid,workitem.workitemid,workitem.workitemversionid,sdiworkitem.keyid1 from sdiworkitem, workitem,s_sample where s_sample.reagentlotid=" + safeSQL.addVar(this.reagentlotid) + " and s_sample.s_sampleid=sdiworkitem.keyid1 and sdiworkitem.workitemid = workitem.workitemid AND sdiworkitem.workitemversionid = workitem.workitemversionid order by sdiworkitem.keyid1 asc ", safeSQL.getValues());
        if (workitems != null && workitems.size() > 0) {
            String lastQualitySample = workitems.getValue(workitems.size() - 1, "keyid1");
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("keyid1", lastQualitySample);
            DataSet filteredWI = workitems.getFilteredDataSet(hm);
            this.workitemid = filteredWI.getColumnValues("workitemid", ";");
            this.workitemversionid = filteredWI.getColumnValues("workitemversionid", ";");
            this.sdiworkitemid = filteredWI.getColumnValues("sdiworkitemid", ";");
        }
    }

    private void loadReagentLotStage() {
        SafeSQL safeSQL = new SafeSQL();
        this.reagentlotstages = this.getQueryProcessor().getPreparedSqlDataSet("select reagentlotstageid stageid,stagelabel from reagentlotstage where reagentlotid=" + safeSQL.addVar(this.reagentlotid) + " order by usersequence asc", safeSQL.getValues());
    }

    @Override
    protected DataSet getRepeatSet(String repeat, String parentid) {
        return repeat.equals("SDIWorkItem_ParamList") ? this.reagentqualitysampleparamlist : (repeat.equals("ReagentLot_Stage") ? this.reagentlotstages : null);
    }

    @Override
    protected String getRepeatKey(String repeat, int repeatRow) {
        if (repeat.equals("SDIWorkItem_ParamList")) {
            return this.repeatset.getValue(repeatRow, "paramlistid") + ";" + this.repeatset.getValue(repeatRow, "paramlistversionid") + ";" + this.repeatset.getValue(repeatRow, "variantid");
        }
        if (repeat.equals("ReagentLot_Stage")) {
            return this.repeatset.getValue(repeatRow, "stageid") + ";" + this.repeatset.getValue(repeatRow, "stagelabel");
        }
        return null;
    }

    @Override
    protected DataSet getAttributeControlAttributes(PropertyList config) {
        String sourcerelation = config.getProperty("sourcerelation", "LV_ReagentType");
        if (sourcerelation.equalsIgnoreCase("WorkItem") || sourcerelation.equalsIgnoreCase("ParamList")) {
            return this.getWorkItemAttributeControlAttributes(config);
        }
        String attributeid = config.getProperty("attributeid");
        String worksheetcontext = config.getProperty("worksheetcontext");
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("SELECT * FROM sdiattribute WHERE sdcid = 'LV_ReagentType' ");
        sql.append(" AND keyid1 = ").append(safeSQL.addVar(this.reagenttypeid));
        sql.append(" AND keyid2 = ").append(safeSQL.addVar(this.reagenttypeversionid));
        sql.append(" AND attributesdcid = ").append(safeSQL.addVar("LV_WorksheetItem"));
        sql.append(attributeid.length() > 0 ? " AND attributeid = " + safeSQL.addVar(attributeid) : (worksheetcontext.length() > 0 ? " AND worksheetcontext = " + safeSQL.addVar(worksheetcontext) : ""));
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    @Override
    protected DataSet getWorkItemAttributeControlAttributes(PropertyList config) {
        String attributeid = config.getProperty("attributeid");
        String worksheetcontext = config.getProperty("worksheetcontext");
        String sourcerelation = config.getProperty("sourcerelation", "WorkItem");
        String workitemid = config.getProperty("workitemid");
        String paramlistid = config.getProperty("paramlistid");
        String paramlistversionid = config.getProperty("paramlistversionid");
        String variantid = config.getProperty("variantid");
        StringBuffer sql = new StringBuffer();
        ArrayList<String> params = new ArrayList<String>();
        sql.append("SELECT * FROM sdiattribute WHERE sdcid = 'WorkItem' AND keyid1 = ? AND keyid2 = ? AND attributesdcid = ? ");
        params.add(workitemid);
        params.add(this.workitemversionid);
        params.add("LV_WorksheetItem");
        if (sourcerelation.equalsIgnoreCase("ParamList")) {
            sql.append(" AND copydowncontext = ( SELECT workitemitemid FROM workitemitem WHERE workitemid = ? and workitemversionid = ? AND sdcid = 'ParamList' AND keyid1 = ? AND ( keyid2 = ? OR keyid2 = 'C') AND keyid3 = ? ) ");
            params.add(workitemid);
            params.add(this.workitemversionid);
            params.add(paramlistid);
            params.add(paramlistversionid);
            params.add(variantid);
        } else {
            sql.append(" AND copydowncontext IS NULL ");
        }
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

    @Override
    protected PropertyList getWorksheetNameSubstitutions() {
        PropertyList substitutions = new PropertyList();
        substitutions.setProperty("reagenttypeid", this.reagenttypeid);
        substitutions.setProperty("reagenttypeversionid", this.reagenttypeversionid);
        substitutions.setProperty("reagentlotid", this.reagentlotid);
        return substitutions;
    }

    @Override
    protected String getSubstitution(String token) {
        if (this.repeatset != null && token.equalsIgnoreCase("paramlistid")) {
            return this.repeatset.getValue(this.repeatsetRow, "paramlistid");
        }
        if (this.repeatset != null && token.equalsIgnoreCase("paramlistversionid")) {
            return this.repeatset.getValue(this.repeatsetRow, "paramlistversionid");
        }
        if (this.repeatset != null && token.equalsIgnoreCase("variantid")) {
            return this.repeatset.getValue(this.repeatsetRow, "variantid");
        }
        if (this.repeatset != null && token.equalsIgnoreCase("stagelabel")) {
            return this.repeatset.getValue(this.repeatsetRow, "stagelabel");
        }
        if (this.repeatset != null && token.equalsIgnoreCase("stageid")) {
            return this.repeatset.getValue(this.repeatsetRow, "stageid");
        }
        if (token.equalsIgnoreCase("reagentlotid")) {
            return this.reagentlotid;
        }
        if (token.equalsIgnoreCase("reagenttypeid")) {
            return this.reagenttypeid;
        }
        if (token.equalsIgnoreCase("reagenttypeversionid")) {
            return this.reagenttypeversionid;
        }
        if (token.equalsIgnoreCase("workitemid")) {
            return this.workitemid;
        }
        return "";
    }
}

