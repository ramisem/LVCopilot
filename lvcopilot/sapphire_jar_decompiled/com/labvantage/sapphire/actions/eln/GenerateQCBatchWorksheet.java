/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseGenerateWorksheet;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class GenerateQCBatchWorksheet
extends BaseGenerateWorksheet
implements sapphire.action.GenerateQCBatchWorksheet {
    private String qcbatchid;
    private String qcmethodid;
    private String qcmethodversionid;
    private String paramlisttype;
    private DataSet qcbatchparamlists;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        super.processAction(properties);
        this.qcbatchid = properties.getProperty("qcbatchid");
        TranslationProcessor tp = this.getTranslationProcessor();
        DataSet qcmethod = this.getQueryProcessor().getPreparedSqlDataSet("SELECT  DISTINCT s_qcmethod.s_qcmethodid, s_qcmethod.s_qcmethodversionid, s_qcmethod.paramlisttype, s_qcbatch.assignedanalyst FROM s_qcbatch, s_qcmethod WHERE s_qcbatch.qcmethodid = s_qcmethod.s_qcmethodid AND s_qcbatch.qcmethodversionid = s_qcmethod.s_qcmethodversionid AND s_qcbatchid = ?", new Object[]{this.qcbatchid});
        if (qcmethod.size() == 1) {
            this.qcmethodid = qcmethod.getValue(0, "s_qcmethodid");
            this.qcmethodversionid = qcmethod.getValue(0, "s_qcmethodversionid");
            this.paramlisttype = qcmethod.getValue(0, "paramlisttype");
            this.loadTemplate("QCMethod", this.qcmethodid, this.qcmethodversionid, "(null)", properties.getProperty("worksheetrule", "default"));
            if ("A".equals(this.authorflag) && this.authorid.length() == 0) {
                this.authorid = qcmethod.getValue(0, "assignedanalyst");
            }
        } else {
            HashMap<String, String> token = new HashMap<String, String>();
            token.put("qcbatchid", this.qcbatchid);
            String msg = tp.translate((qcmethod.size() > 1 ? "Multiple QC methods found" : "No QC method found") + " for the passed in qcbatchid: [qcbatchid]", token);
            throw new SapphireException(msg);
        }
        this.startWorksheet("[qcbatchid] Worksheet [currentdate]-[template_seq;00000]");
        this.addWorksheetSDIs("QCBatch", this.qcbatchid, "", "");
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select distinct sdidata.paramlistid, sdidata.paramlistversionid, sdidata.variantid,sdiworkitemitem.workitemid, sdiworkitemitem.usersequence from sdidata,sdiworkitemitem where sdiworkitemitem.sdcid = sdidata.sdcid AND sdiworkitemitem.keyid1 = sdidata.keyid1  AND sdiworkitemitem.keyid2 = sdidata.keyid2  AND sdiworkitemitem.keyid3 = sdidata.keyid3  AND sdiworkitemitem.itemsdcid = 'ParamList'  AND sdiworkitemitem.itemkeyid1 = sdidata.paramlistid  AND sdiworkitemitem.itemkeyid2 = sdidata.paramlistversionid  AND sdiworkitemitem.itemkeyid3 = sdidata.variantid  AND sdiworkitemitem.iteminstance = sdidata.dataset  AND sdidata.s_qcbatchid =" + safeSQL.addVar(this.qcbatchid) + " order by 4,5";
        this.qcbatchparamlists = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        this.generateSections();
        String[] worksheet = this.finalizeWorksheet();
        properties.setProperty("worksheetid", worksheet[0]);
        properties.setProperty("worksheetversionid", worksheet[1]);
    }

    @Override
    protected DataSet getRepeatSet(String repeat, String parentid) {
        return repeat.equals("QCBatch_ParamList") ? this.qcbatchparamlists : null;
    }

    @Override
    protected String getRepeatKey(String repeat, int repeatRow) {
        return this.repeatset.getValue(repeatRow, "paramlistid") + ";" + this.repeatset.getValue(repeatRow, "paramlistversionid") + ";" + this.repeatset.getValue(repeatRow, "variantid");
    }

    @Override
    protected DataSet getAttributeControlAttributes(PropertyList config) {
        Object[] objectArray;
        String attributeid = config.getProperty("attributeid");
        String worksheetcontext = config.getProperty("worksheetcontext");
        QueryProcessor queryProcessor = this.getQueryProcessor();
        String string = "SELECT * FROM sdiattribute WHERE sdcid = 'QCMethod' AND keyid1 = ? AND keyid2 = ? AND attributesdcid = ? " + (attributeid.length() > 0 ? " AND attributeid = ?" : (worksheetcontext.length() > 0 ? " AND worksheetcontext = ?" : ""));
        if (attributeid.length() > 0) {
            Object[] objectArray2 = new Object[4];
            objectArray2[0] = this.qcmethodid;
            objectArray2[1] = this.qcmethodversionid;
            objectArray2[2] = "LV_WorksheetItem";
            objectArray = objectArray2;
            objectArray2[3] = attributeid;
        } else if (worksheetcontext.length() > 0) {
            Object[] objectArray3 = new Object[4];
            objectArray3[0] = this.qcmethodid;
            objectArray3[1] = this.qcmethodversionid;
            objectArray3[2] = "LV_WorksheetItem";
            objectArray = objectArray3;
            objectArray3[3] = worksheetcontext;
        } else {
            Object[] objectArray4 = new Object[3];
            objectArray4[0] = this.qcmethodid;
            objectArray4[1] = this.qcmethodversionid;
            objectArray = objectArray4;
            objectArray4[2] = "LV_WorksheetItem";
        }
        return queryProcessor.getPreparedSqlDataSet(string, objectArray);
    }

    @Override
    protected PropertyList getWorksheetNameSubstitutions() {
        PropertyList substitutions = new PropertyList();
        substitutions.setProperty("qcmethodid", this.qcmethodid);
        substitutions.setProperty("qcmethodversionid", this.qcmethodversionid);
        substitutions.setProperty("paramlisttype", this.paramlisttype);
        substitutions.setProperty("qcbatchid", this.qcbatchid);
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
        if (token.equalsIgnoreCase("qcbatchid")) {
            return this.qcbatchid;
        }
        if (token.equalsIgnoreCase("qcmethodid")) {
            return this.qcmethodid;
        }
        if (token.equalsIgnoreCase("qcmethodversionid")) {
            return this.qcmethodversionid;
        }
        return "";
    }
}

