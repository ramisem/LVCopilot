/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.util.samplingplan.SamplingPlanUtil;
import java.sql.PreparedStatement;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LV_ProdVariant
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        HashMap productHM;
        DataSet primary = sdiData.getDataset("primary");
        this.setProdVariantState(primary);
        if (primary.getColumnValues("productid", ";").length() > 0 && (productHM = this.getProductType(primary)).size() > 0) {
            for (int i = 0; i < primary.size(); ++i) {
                HashMap map;
                if (primary.getValue(i, "productid", "").length() <= 0 || !productHM.containsKey(primary.getValue(i, "productid", "") + ":" + primary.getValue(i, "productversionid", "")) || (map = (HashMap)productHM.get(primary.getValue(i, "productid", "") + ":" + primary.getValue(i, "productversionid", ""))) == null || map.size() <= 0 || !map.containsKey("sampletypeid")) continue;
                primary.setValue(i, "prodvarianttype", (String)map.get("sampletypeid"));
            }
        }
        this.setProdVarUserSequence(primary);
    }

    private void setProdVarUserSequence(DataSet primary) throws SapphireException {
        try {
            String productIdList = primary.getColumnValues("productid", ";");
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            String rsetId = this.getDAMProcessor().createRSet("Product", productIdList, "", "");
            sql.append("SELECT productid, max(usersequence) usersequence FROM s_prodvariant, rsetitems WHERE productid = rsetitems.keyid1 AND productversionid = rsetitems.keyid2");
            sql.append(" AND rsetid = ").append(safeSQL.addVar(rsetId));
            sql.append(" GROUP BY productid, productversionid ORDER BY productid, productversionid");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            int rowCount = primary.getRowCount();
            HashMap<String, String> maxUserSeq = new HashMap<String, String>();
            for (int i = 0; i < rowCount; ++i) {
                int maxUserSequence;
                String productId = primary.getString(i, "productid");
                String productVersionId = primary.getString(i, "productversionid");
                if (maxUserSeq.containsKey(productId + ":" + productVersionId)) {
                    maxUserSequence = Integer.parseInt((String)maxUserSeq.get(productId + ":" + productVersionId));
                } else {
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    findMap.put("productid", productId);
                    findMap.put("productversionid", productVersionId);
                    int productRow = ds.findRow(findMap);
                    maxUserSequence = productRow > -1 ? ds.getInt(productRow, "usersequence", 0) : 0;
                }
                primary.setNumber(i, "usersequence", ++maxUserSequence);
                maxUserSeq.put(productId + ":" + productVersionId, String.valueOf(maxUserSequence));
            }
        }
        catch (SapphireException e) {
            this.logger.stackTrace(e);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private HashMap getProductType(DataSet primary) throws SapphireException {
        String rsetid = this.getDAMProcessor().createRSet("Product", primary.getColumnValues("productid", ";"), primary.getColumnValues("productversionid", ";"), null);
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT sampletypeid, s_productid, s_productversionid FROM s_product, rsetitems WHERE ");
        sql.append(" rsetitems.rsetid = ").append(safeSQL.addVar(rsetid));
        sql.append(" AND s_productid = rsetitems.keyid1 AND s_productversionid = rsetitems.keyid2");
        HashMap productHM = new HashMap();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        for (int i = 0; i < ds.size(); ++i) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("sampletypeid", ds.getValue(i, "sampletypeid"));
            productHM.put(ds.getValue(i, "s_productid") + ":" + ds.getValue(i, "s_productversionid"), map);
        }
        if (StringUtil.getLen(rsetid) > 0L) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        return productHM;
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.validateProdVariant(primary);
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.validateProdVariant(primary);
    }

    private void validateProdVariant(DataSet primary) throws SapphireException {
        try {
            for (int i = 0; i < primary.size(); ++i) {
                String prodVariantType = primary.getValue(i, "prodvarianttype", "");
                if (prodVariantType.length() <= 0 || !this.hasProdVariantChanged(prodVariantType, primary, i)) continue;
                boolean hasValue = false;
                PropertyListCollection prodVariantCols = SamplingPlanUtil.getProdVariantPropertyList(prodVariantType, this.getConfigurationProcessor());
                if (prodVariantCols == null) continue;
                String columns = "";
                String prodVariantColumns = "";
                for (int j = 0; j < prodVariantCols.size(); ++j) {
                    PropertyList pl = prodVariantCols.getPropertyList(j);
                    if (pl == null) continue;
                    String prodvariantcolumn = pl.getProperty("prodvariantcolumn", "");
                    String colVal = primary.getValue(i, prodvariantcolumn, "");
                    if (j == 0) {
                        columns = prodvariantcolumn + ":" + colVal;
                        prodVariantColumns = prodvariantcolumn;
                    } else {
                        columns = columns + " or  " + prodvariantcolumn + ":" + colVal;
                        prodVariantColumns = prodVariantColumns + "; " + prodvariantcolumn;
                    }
                    if (colVal.length() <= 0 || hasValue) continue;
                    hasValue = true;
                }
                if (hasValue) continue;
                throw new SapphireException(this.getTranslationProcessor().translate("At least one of the following prodvariant columns must have a value:") + " " + prodVariantColumns);
            }
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
    }

    private boolean hasProdVariantChanged(String prodVariantType, DataSet primary, int rowIndex) {
        boolean hasChanged = false;
        PropertyListCollection prodVariantCols = SamplingPlanUtil.getProdVariantPropertyList(prodVariantType, this.getConfigurationProcessor());
        if (prodVariantCols != null) {
            for (int j = 0; j < prodVariantCols.size(); ++j) {
                String prodvariantcolumn;
                PropertyList pl = prodVariantCols.getPropertyList(j);
                if (pl == null || !this.hasPrimaryValueChanged(primary, rowIndex, prodvariantcolumn = pl.getProperty("prodvariantcolumn", ""))) continue;
                hasChanged = true;
                break;
            }
        }
        if (!hasChanged && this.hasPrimaryValueChanged(primary, rowIndex, "prodvarianttype")) {
            hasChanged = true;
        }
        return hasChanged;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (actionProps.containsKey("approve")) {
            String approve = actionProps.getProperty("approve");
            for (int i = 0; i < primary.size(); ++i) {
                if (approve.equalsIgnoreCase("Yes")) {
                    primary.setValue(i, "currentstateid", primary.getValue(i, "transitionstateid"));
                }
                primary.setValue(i, "transitionstateid", "");
                primary.setValue(i, "transitionapprovalflag", "");
            }
        } else {
            DataSet changedProductDS = new DataSet();
            changedProductDS.addColumn("rowid", 1);
            changedProductDS.addColumn("productid", 0);
            changedProductDS.addColumn("productversionid", 0);
            for (int i = 0; i < primary.size(); ++i) {
                if (this.hasPrimaryValueChanged(primary, i, "currentstateid")) {
                    if (primary.getColumnType("lasttransitiondt") == -1) {
                        primary.addColumn("lasttransitiondt", 2);
                        primary.addColumn("transitionstateid", 0);
                        primary.addColumn("transitionapprovalflag", 0);
                    }
                    primary.setValue(i, "lasttransitiondt", "n");
                    primary.setValue(i, "transitionstateid", "");
                    primary.setValue(i, "transitionapprovalflag", "");
                }
                if (primary.getValue(i, "productid").length() <= 0 || !this.hasPrimaryValueChanged(primary, i, "productid") && !this.hasPrimaryValueChanged(primary, i, "prodvarianttype") && !this.hasPrimaryValueChanged(primary, i, "productversionid")) continue;
                int row = changedProductDS.size();
                changedProductDS.addRow();
                changedProductDS.setValue(row, "productid", primary.getValue(i, "productid"));
                changedProductDS.setValue(row, "productversionid", primary.getValue(i, "productversionid"));
                changedProductDS.setValue(row, "rowid", String.valueOf(i));
            }
            if (changedProductDS.size() > 0) {
                HashMap productHM = this.getProductType(changedProductDS);
                for (int i = 0; i < changedProductDS.size(); ++i) {
                    HashMap map = (HashMap)productHM.get(primary.getValue(i, "productid", "") + ":" + primary.getValue(i, "productversionid", ""));
                    primary.setValue(Integer.parseInt(changedProductDS.getValue(i, "rowid")), "prodvarianttype", (String)map.get("sampletypeid"));
                }
            }
        }
        this.setProdVarStateWithRuleChange(primary);
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT b.s_batchid  FROM   s_batch b, rsetitems  WHERE  rsetitems.rsetid = " + safeSQL.addVar(rsetid) + " AND    b.prodvariantid = rsetitems.keyid1 ";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds.size() > 0) {
            this.throwError("ProdVariantUsed", "VALIDATION", "Selected Product Variant(s) cannot be deleted because of the following references to the Batch(s): " + ds.getColumnValues("s_batchid", ";"));
        }
    }

    private void setProdVariantState(DataSet primary) throws SapphireException {
        PreparedStatement psmt = this.database.prepareStatement("selectprodvariantstate", "SELECT initialstateid FROM s_prodvariantrule WHERE s_prodvariantruleid = ? ");
        primary.addColumn("currentstateid", 0);
        try {
            for (int i = 0; i < primary.size(); ++i) {
                String ruleId = primary.getValue(i, "prodvariantruleid", "");
                String currentState = primary.getValue(i, "currentstateid", "");
                if (ruleId.length() <= 0 || currentState.length() != 0) continue;
                psmt.setString(1, ruleId);
                DataSet ds = new DataSet(psmt.executeQuery());
                if (ds == null || ds.getRowCount() <= 0) continue;
                String state = ds.getString(0, "initialstateid", "");
                primary.setString(i, "currentstateid", state);
            }
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
    }

    public void setProdVarStateWithRuleChange(DataSet dsPrimary) throws SapphireException {
        PreparedStatement selectInitialState = this.database.prepareStatement("selectprodvariantstate", "SELECT initialstateid FROM s_prodvariantrule  WHERE s_prodvariantruleid = ? ");
        try {
            for (int i = 0; i < dsPrimary.size(); ++i) {
                String ruleId;
                if (!this.hasPrimaryValueChanged(dsPrimary, i, "prodvariantruleid") || (ruleId = dsPrimary.getValue(i, "prodvariantruleid", "")).length() <= 0) continue;
                selectInitialState.setString(1, ruleId);
                DataSet ds = new DataSet(selectInitialState.executeQuery());
                String initialState = ds.getString(0, "initialstateid", "");
                dsPrimary.setString(i, "currentstateid", initialState);
            }
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        this.database.closeStatement("selectprodvariantstate");
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

