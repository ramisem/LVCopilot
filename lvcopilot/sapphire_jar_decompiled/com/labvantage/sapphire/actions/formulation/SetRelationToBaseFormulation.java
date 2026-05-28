/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.formulation;

import sapphire.SapphireException;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class SetRelationToBaseFormulation
extends BaseAction
implements sapphire.action.SetRelationToBaseFormulation {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54648 $";
    String RelationType = "Comparator";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String baseFormulation = properties.getProperty("baseformulationid");
        String baseFormulationVersion = properties.getProperty("baseformulationversionid", "1");
        TranslationProcessor tp = this.getTranslationProcessor();
        SequenceProcessor scp = this.getSequenceProcessor();
        String sdcid = "SDIRelation";
        if (baseFormulation.length() == 0) {
            throw new SapphireException(tp.translate("Mandatory base Formulation not passed!"));
        }
        this.deleteExistingBases(baseFormulation, baseFormulationVersion, sdcid);
        SafeSQL safeSQL = new SafeSQL();
        this.database.createPreparedResultSet("getformulations", "select f.s_productid, f.s_productversionid from s_product f, s_product t  where t.s_productid = " + safeSQL.addVar(baseFormulation) + " and t.s_productversionid = " + safeSQL.addVar(baseFormulationVersion) + " and f.formulationprojectid = t.formulationprojectid and f.templateflag !='Y'  and not exists ( select 1 from sdirelation where fromsdcid = 'Product' and fromkeyid1 = f.s_productid and fromkeyid2 = f.s_productversionid   and tosdcid = 'Product' and tokeyid1 = " + safeSQL.addVar(baseFormulation) + " and tokeyid2 = " + safeSQL.addVar(baseFormulationVersion) + " and relationtype = " + safeSQL.addVar(this.RelationType) + ")", safeSQL.getValues());
        DataSet ds = new DataSet(this.database.getResultSet("getformulations"));
        this.database.closeResultSet("getformulations");
        DataSet dsAddRelation = new DataSet();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String fromKeyId1 = ds.getValue(i, "s_productid");
            String fromKeyId2 = ds.getValue(i, "s_productversionid");
            if (baseFormulation.equals(fromKeyId1) && baseFormulationVersion.equals(fromKeyId2)) continue;
            int r = dsAddRelation.addRow();
            dsAddRelation.setString(r, "sdirelationid", this.RelationType + "-" + scp.getSequence(sdcid, this.RelationType));
            dsAddRelation.setString(r, "fromsdcid", "Product");
            dsAddRelation.setString(r, "fromkeyid1", fromKeyId1);
            dsAddRelation.setString(r, "fromkeyid2", fromKeyId2);
            dsAddRelation.setString(r, "fromkeyid3", "(null)");
            dsAddRelation.setString(r, "relationtype", this.RelationType);
            dsAddRelation.setString(r, "tosdcid", "Product");
            dsAddRelation.setString(r, "tokeyid1", baseFormulation);
            dsAddRelation.setString(r, "tokeyid2", baseFormulationVersion);
            dsAddRelation.setString(r, "tokeyid3", "(null)");
        }
        if (dsAddRelation.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.put("sdcid", sdcid);
            props.put("overrideautokey", "Y");
            props.put("keyid1", dsAddRelation.getColumnValues("sdirelationid", ";"));
            props.put("fromsdcid", dsAddRelation.getColumnValues("fromsdcid", ";"));
            props.put("fromkeyid1", dsAddRelation.getColumnValues("fromkeyid1", ";"));
            props.put("fromkeyid2", dsAddRelation.getColumnValues("fromkeyid2", ";"));
            props.put("fromkeyid3", dsAddRelation.getColumnValues("fromkeyid3", ";"));
            props.put("tosdcid", dsAddRelation.getColumnValues("tosdcid", ";"));
            props.put("tokeyid1", dsAddRelation.getColumnValues("tokeyid1", ";"));
            props.put("tokeyid2", dsAddRelation.getColumnValues("tokeyid2", ";"));
            props.put("tokeyid3", dsAddRelation.getColumnValues("tokeyid3", ";"));
            props.put("relationtype", dsAddRelation.getColumnValues("relationtype", ";"));
            this.getActionProcessor().processAction("AddSDI", "1", props);
            String redoKeyId1 = dsAddRelation.getColumnValues("fromkeyid1", "','") + "','" + baseFormulation;
            String selectIngredient = "SELECT s_productid, s_productversionid, s_productitemid FROM s_productformulation WHERE s_productid = ?  AND s_productversionid = '1'";
            this.database.createPreparedResultSet("ingredient", selectIngredient, new Object[]{baseFormulation});
            DataSet dsIngredient = new DataSet(this.database.getResultSet("ingredient"));
            this.database.closeResultSet("ingredient");
            if (dsIngredient.getRowCount() > 0) {
                props.clear();
                props.setProperty("sdcid", "LV_ProductIngredient");
                props.setProperty("keyid1", dsIngredient.getString(0, "s_productid"));
                props.setProperty("keyid2", dsIngredient.getString(0, "s_productversionid"));
                props.setProperty("keyid3", dsIngredient.getString(0, "s_productitemid"));
                this.getActionProcessor().processAction("RedoCalculations", "1", props);
            }
        }
    }

    private void deleteExistingBases(String baseFormulation, String baseFormulationVersion, String sdcid) throws SapphireException {
        String sql = "select s.sdirelationid from sdirelation s, s_product f, s_product t  where s.tosdcid = 'Product' and s.tokeyid1 = f.s_productid and s.tokeyid2 = f.s_productversionid and s.relationtype = ? and f.formulationprojectid = t.formulationprojectid  and f.s_productid != t.s_productid and t.s_productid = ? and t.s_productversionid = ? ";
        this.database.createPreparedResultSet("todeleteexistingbase", sql, new Object[]{this.RelationType, baseFormulation, baseFormulationVersion});
        DataSet ds = new DataSet(this.database.getResultSet("todeleteexistingbase"));
        this.database.closeResultSet("todeleteexistingbase");
        if (ds.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.put("sdcid", sdcid);
            props.put("keyid1", ds.getColumnValues("sdirelationid", ";"));
            this.getActionProcessor().processAction("DeleteSDI", "1", props);
        }
    }
}

