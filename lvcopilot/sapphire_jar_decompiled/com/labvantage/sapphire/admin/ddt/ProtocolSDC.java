/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.StabilityUtil;
import com.labvantage.sapphire.stability.ScheduleGridUtil;
import java.sql.PreparedStatement;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ProtocolSDC
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (!this.isCMTImport()) {
            DataSet protocolproducts;
            DataSet plans = sdiData.getDataset("protocol_scheduleplan");
            DataSet productplans = sdiData.getDataset("protocolprod_scheduleplan");
            TranslationProcessor tp = this.getTranslationProcessor();
            if (plans != null && productplans != null) {
                try {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    for (int i = 0; i < plans.size(); ++i) {
                        String planid = plans.getString(i, "scheduleplanid");
                        String newplanid = ScheduleGridUtil.copyPlan(this.getConnectionId(), planid, "P");
                        plans.setString(i, "scheduleplanid", newplanid);
                        filter.put("scheduleplanid", planid);
                        DataSet sublist = productplans.getFilteredDataSet(filter);
                        sublist.setString(-1, "scheduleplanid", newplanid);
                    }
                }
                catch (Exception e) {
                    this.throwError(tp.translate("Copy Plan"), "VALIDATION", tp.translate("Unable to copy plan. Exception thrown: ") + e.getMessage());
                }
            }
            if ((protocolproducts = sdiData.getDataset("protocolproduct")) != null) {
                this.presetPartialPullFlag(protocolproducts);
            }
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet protocolproducts = sdiData.getDataset("protocolproduct");
        if (protocolproducts != null) {
            this.presetPartialPullFlag(protocolproducts);
        }
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet protocolproducts = sdiData.getDataset("protocolproduct");
        DataSet protocolProd_SchedulePlans = sdiData.getDataset("protocolprod_scheduleplan");
        if (protocolproducts != null) {
            this.validateProtocolProductsUnits(protocolproducts);
        }
        if (protocolProd_SchedulePlans != null && protocolproducts == null) {
            this.validateProtocolProd_SchedulePlan(protocolProd_SchedulePlans);
        }
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet protocolproducts = sdiData.getDataset("protocolproduct");
        DataSet protocolProd_SchedulePlans = sdiData.getDataset("protocolprod_scheduleplan");
        if (protocolproducts != null) {
            this.validateProtocolProductsUnits(protocolproducts);
        }
        if (protocolProd_SchedulePlans != null && protocolproducts == null) {
            this.validateProtocolProd_SchedulePlan(protocolProd_SchedulePlans);
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT scheduleplanid FROM protocol_scheduleplan WHERE " + (this.connectionInfo.isOracle() ? "( protocolid, protocolversionid ) IN ( SELECT keyid1, keyid2 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " )" : "( protocolid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " ) AND protocolversionid IN ( SELECT keyid2 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " ) )");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds.size() > 0) {
            StringBuffer planlist = new StringBuffer();
            for (int i = 0; i < ds.size(); ++i) {
                planlist.append(";").append(ds.getString(i, "scheduleplanid"));
            }
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("sdcid", "SchedulePlan");
            props.put("keyid1", planlist.substring(1));
            this.getActionProcessor().processAction("DeleteSDI", "1", props);
        }
        safeSQL.reset();
        sql = "DELETE FROM protocolproduct WHERE " + (this.connectionInfo.isOracle() ? "( protocolid, protocolversionid ) IN ( SELECT keyid1, keyid2 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " )" : "( protocolid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " ) AND protocolversionid IN ( SELECT keyid2 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " ) )");
        this.database.executePreparedUpdate(sql, safeSQL.getValues());
    }

    private void presetPartialPullFlag(DataSet protocolproducts) {
        for (int i = 0; i < protocolproducts.size(); ++i) {
            if (!protocolproducts.isNull(i, "partialpullflag")) continue;
            protocolproducts.setString(i, "partialpullflag", "N");
        }
    }

    private void validateProtocolProductsUnits(DataSet protocolproducts) throws SapphireException {
        DataSet protocolProdSchedulePlans = new DataSet();
        for (int i = 0; i < protocolproducts.size(); ++i) {
            String protocolproductId = protocolproducts.getValue(i, "protocolproductid");
            String protocolId = protocolproducts.getValue(i, "protocolid");
            String protocolVersionId = protocolproducts.getValue(i, "protocolversionid");
            this.database.createPreparedResultSet("getProtocolProd_SchedulePlan", "SELECT  scheduleplanid, protocolid, protocolversionid, protocolproductid, qtyunits, qtyneeded  FROM protocolprod_scheduleplan  WHERE protocolid = ? AND protocolversionid = ? AND protocolproductid = ? ", new Object[]{protocolId, protocolVersionId, protocolproductId});
            DataSet ds = new DataSet(this.database.getResultSet("getProtocolProd_SchedulePlan"));
            if (ds.getRowCount() <= 0) continue;
            protocolProdSchedulePlans.copyRow(ds, -1, 1);
        }
        if (protocolProdSchedulePlans.getRowCount() > 0) {
            this.validateProtocolProd_SchedulePlan(protocolProdSchedulePlans);
        }
        this.database.closeResultSet("getProtocolProd_SchedulePlan");
    }

    private void validateProtocolProd_SchedulePlan(DataSet protocolprod_scplan) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        QueryProcessor qp = this.getQueryProcessor();
        PreparedStatement psmt = this.database.prepareStatement("getContainerUnit", "SELECT  c.sizeunits  FROM protocolproduct p, containertype c  where c.containertypeid = p.containertypeid and p.protocolid = ? and p.protocolversionid = ? and p.protocolproductid = ? ");
        HashMap<String, DataSet> mapPlanWorkItems = new HashMap<String, DataSet>();
        HashMap<String, DataSet> mapPlanItems = new HashMap<String, DataSet>();
        HashMap<String, DataSet> mapPlanConditions = new HashMap<String, DataSet>();
        for (int i = 0; i < protocolprod_scplan.size(); ++i) {
            DataSet dsWorkItems;
            DataSet dsPlanItems;
            DataSet dsPlanConditions;
            String protocolproductId = protocolprod_scplan.getValue(i, "protocolproductid");
            String protocolId = protocolprod_scplan.getValue(i, "protocolid");
            String protocolVersionId = protocolprod_scplan.getValue(i, "protocolversionid");
            String planId = protocolprod_scplan.getValue(i, "scheduleplanid");
            String containerUnit = "";
            try {
                psmt.setString(1, protocolId);
                psmt.setString(2, protocolVersionId);
                psmt.setString(3, protocolproductId);
                DataSet dsContSizeUnit = new DataSet(psmt.executeQuery());
                if (dsContSizeUnit != null && dsContSizeUnit.getRowCount() > 0) {
                    containerUnit = dsContSizeUnit.getValue(0, "sizeunits");
                }
                if (containerUnit.length() == 0) {
                    continue;
                }
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
            if (!mapPlanWorkItems.containsKey(planId)) {
                mapPlanWorkItems.put(planId, StabilityUtil.getPlanItemWorkItems(planId, qp));
            }
            if (!mapPlanItems.containsKey(planId)) {
                mapPlanItems.put(planId, StabilityUtil.getPlanItems(planId, qp));
            }
            if (!mapPlanConditions.containsKey(planId)) {
                mapPlanConditions.put(planId, StabilityUtil.getPlanConditions(planId, qp));
            }
            if (mapPlanConditions != null && (dsPlanConditions = (DataSet)mapPlanConditions.get(planId)).getRowCount() > 0) {
                StabilityUtil.validatePlanConditionUnits(dsPlanConditions, containerUnit, qp, tp);
            }
            if (mapPlanItems.get(planId) != null && (dsPlanItems = (DataSet)mapPlanItems.get(planId)).getRowCount() > 0) {
                StabilityUtil.validatePlanItemPullAmountUnits(dsPlanItems, containerUnit, qp, tp, "Container");
            }
            if (mapPlanWorkItems.get(planId) == null || (dsWorkItems = (DataSet)mapPlanWorkItems.get(planId)).getRowCount() <= 0) continue;
            StabilityUtil.validatePlanItemWorkItemUnits(dsWorkItems, containerUnit, qp, tp);
        }
        this.database.closeStatement("getContainerUnit");
    }
}

