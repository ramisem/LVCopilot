/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_ReagentType
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54055 $";
    private String ruleid = "ReagentTypeRule";
    public static final String SDCID = "LV_ReagentType";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (!primary.isValidColumn("contentflag")) {
            primary.addColumn("contentflag", 0);
        }
        for (int i = 0; i < primary.size(); ++i) {
            if (StringUtil.getLen(primary.getValue(i, "contentflag", "")) == 0L) {
                primary.setValue(i, "contentflag", "R");
            }
            if ("(Containers)".equals(primary.getValue(i, "amountexpectedunits")) || "C".equals(primary.getValue(i, "amountexpectedunitstype"))) {
                primary.setValue(i, "amountexpectedunits", "");
                primary.setValue(i, "amountexpectedunitstype", "C");
            } else {
                primary.setValue(i, "amountexpectedunitstype", StringUtil.getLen(primary.getValue(i, "amountexpectedunits")) > 0L ? "U" : "");
            }
            if ("(Containers)".equals(primary.getValue(i, "reorderthresholdunits")) || "C".equals(primary.getValue(i, "reorderthresholdunittype"))) {
                primary.setValue(i, "reorderthresholdunits", "");
                primary.setValue(i, "reorderthresholdunittype", "C");
                continue;
            }
            primary.setValue(i, "reorderthresholdunittype", StringUtil.getLen(primary.getValue(i, "reorderthresholdunits")) > 0L ? "U" : "");
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (this.hasPrimaryValueChanged(primary, i, "amountexpectedunits")) {
                primary.addColumn("amountexpectedunitstype", 0);
                if ("(Containers)".equals(primary.getValue(i, "amountexpectedunits"))) {
                    primary.setValue(i, "amountexpectedunits", "");
                    primary.setValue(i, "amountexpectedunitstype", "C");
                } else {
                    primary.setValue(i, "amountexpectedunitstype", StringUtil.getLen(primary.getValue(i, "amountexpectedunits")) > 0L ? "U" : "");
                }
            }
            if (!this.hasPrimaryValueChanged(primary, i, "reorderthresholdunits")) continue;
            primary.addColumn("reorderthresholdunittype", 0);
            if ("(Containers)".equals(primary.getValue(i, "reorderthresholdunits"))) {
                primary.setValue(i, "reorderthresholdunits", "");
                primary.setValue(i, "reorderthresholdunittype", "C");
                continue;
            }
            primary.setValue(i, "reorderthresholdunittype", StringUtil.getLen(primary.getValue(i, "reorderthresholdunits")) > 0L ? "U" : "");
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String sql = "SELECT rl.reagentlotid FROM reagentlot rl,rsetitems ri WHERE rl.reagenttypeid=ri.keyid1 AND rl.reagenttypeversionid=ri.keyid2 AND ri.rsetid=?";
        DataSet linkedReagentLotDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
        if (linkedReagentLotDS.size() > 0) {
            this.throwError(this.ruleid, "VALIDATION", "One or more Selected Reagent Type(s) are in use in Reagent Lot.");
        }
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.deleteVendorItems(rsetid);
    }

    private void deleteVendorItems(String rsetid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT vendoritemid FROM vendoritem v,rsetitems r ");
        sql.append(" WHERE v.linksdcid = 'LV_ReagentType'");
        sql.append(" AND v.linkkeyid1 = r.keyid1");
        sql.append(" AND v.linkkeyid2 = r.keyid2");
        sql.append(" AND r.sdcid = 'LV_ReagentType'");
        sql.append(" AND r.rsetid = " + safeSQL.addVar(rsetid));
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_VendorItem");
            props.setProperty("keyid1", ds.getColumnValues("vendoritemid", ";"));
            this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String templatekeyid1 = actionProps.getProperty("templatekeyid1");
        String templatekeyid2 = actionProps.getProperty("templatekeyid2");
        String newkeyid1 = sdiData.getDataset("primary").getColumnValues("reagenttypeid", ";");
        String newversionid = sdiData.getDataset("primary").getColumnValues("reagenttypeversionid", ";");
        if (templatekeyid1 != null && templatekeyid1.trim().length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String vendorSql = "SELECT VENDORITEMID FROM vendoritem WHERE linksdcid='LV_ReagentType' and linkkeyid1=" + safeSQL.addVar(templatekeyid1) + " and linkkeyid2=" + safeSQL.addVar(templatekeyid2);
            DataSet sqlDataSet = this.getQueryProcessor().getPreparedSqlDataSet(vendorSql, safeSQL.getValues());
            String[] keyid1 = StringUtil.split(newkeyid1, ";");
            String[] keyid2 = StringUtil.split(newversionid, ";");
            for (int i = 0; i < keyid1.length; ++i) {
                this.insertVendorValue(sqlDataSet, keyid2[i], keyid1[i]);
            }
        }
    }

    private void insertVendorValue(DataSet sqlDataSet, String newversionid, String newkeyid1) throws SapphireException {
        ActionBlock actionBlock = new ActionBlock();
        if (sqlDataSet != null && sqlDataSet.size() > 0) {
            for (int i = 0; i < sqlDataSet.size(); ++i) {
                PropertyList list = new PropertyList();
                String vendoritemid = sqlDataSet.getValue(i, "VENDORITEMID");
                list.setProperty("sdcid", "LV_VendorItem");
                list.setProperty("templateid", vendoritemid);
                list.setProperty("linkkeyid1", newkeyid1);
                list.setProperty("linkkeyid2", newversionid);
                actionBlock.setAction("AddSDI" + (i + 1), "AddSDI", "1", list);
            }
            this.getActionProcessor().processActionBlock(actionBlock);
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public boolean requiresBeforeEditDetailImage() {
        return true;
    }
}

