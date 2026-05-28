/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql;

import com.labvantage.opal.elements.auditdetails.AuditElementsContainer;
import com.labvantage.sapphire.SDI;
import java.util.HashMap;
import java.util.List;
import sapphire.accessor.QueryProcessor;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public interface SQLGenerator {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 56777 $";
    public static final boolean __Debug = true;

    public String getDBMS();

    public SafeSQL getWorkitemDetails(String var1, String var2, String var3);

    public SafeSQL getSdiWorkitemDetails(SDI var1, String var2, String var3, String var4);

    public SafeSQL getSdiWorkitemDataSets(SDI var1, String var2, String var3);

    public SafeSQL getReleasedCount(SDI var1);

    public SafeSQL getDatasetKey(SDI var1);

    public SafeSQL getNotReleasedCount(SDI var1, String var2);

    public SafeSQL getSdiDatasets(SDI var1);

    public SafeSQL getMaxSdiWorkitemInstance(SDI var1, String var2);

    public SafeSQL getColumnValue(SDI var1, String var2, String var3, String var4, String var5);

    public SafeSQL getParamlistWorkitems(SDI var1, String var2, String var3, String var4);

    public SafeSQL getRemeasureInstance(SDI var1, String var2, String var3, String var4, String var5);

    public SafeSQL getMaxSdiDataset(SDI var1, String var2, String var3, String var4);

    public SafeSQL getSdiDataitemForTypeAndDs(String var1, String var2);

    public SafeSQL getDataItems(String var1, String var2, String var3, String var4, String var5);

    public String getQueryArgsForTrendChartQuery();

    public SafeSQL getQueryArgsForQuery(String var1);

    public SafeSQL getSampleDetails(String var1);

    public SafeSQL getParamlistDetails(String var1);

    public SafeSQL getUserCertificationDetails(String var1, String var2);

    public SafeSQL getInstrumentDetails(String var1);

    public SafeSQL getInstrumentCertificationDetails(String var1);

    public String getCertifiedUsers();

    public String getUserCustodianInfo();

    public SafeSQL getCustodianAndUserInfo(String var1);

    public SafeSQL getQueryDetails(String var1);

    public SafeSQL getQueryAndArgDetails(String var1, String var2);

    public SafeSQL getQueryAndArgDetails2(String var1, String var2);

    public String getSelectSQL(List var1, String var2, List var3, String var4, boolean var5);

    public SafeSQL getTableSDCSQL(String var1);

    public SafeSQL getTableKeysSQL(String var1);

    public String getAuditSQL(String var1, AuditElementsContainer var2, PropertyList var3, QueryProcessor var4, Logger var5, HashMap var6) throws Exception;

    public String getDynamicAuditSQL(String var1, AuditElementsContainer var2, PropertyList var3, QueryProcessor var4, Logger var5, HashMap var6) throws Exception;

    public String getDataitemSpecsAndLimits(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, String var10, String var11, String var12, String var13, String var14, String var15, String var16, String var17, String var18, String var19);

    public SafeSQL getKeysFromRSetItemsSQLStmt(String var1);

    public SafeSQL getQCBatchParamSets(SDI var1);

    public String getQCBatchItems(SDI var1, String var2);

    public SafeSQL getQCDataPointsForEvaluation(String var1, String var2, String var3, String var4, String var5, boolean var6, String var7, String var8, String var9);

    public String getQCDataPointsForEvaluation(String var1, String var2, String var3, String var4, String var5, boolean var6, String var7, String var8, String var9, SafeSQL var10);

    public String getQCDataSets(String var1, SafeSQL var2);

    public String getPrimarySql(String var1, String var2, String var3, String var4, String var5);

    public SafeSQL getSpecSql(String var1, String var2, String var3);

    public SafeSQL getDatasetSql(String var1, String var2, String var3, String var4, String var5, String var6);

    public SafeSQL getApprovalSql(String var1, String var2, String var3, String var4, String var5, String var6);

    public SafeSQL getDataitemSql(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, String var10);

    public SafeSQL getSpecParamLimitsSql(String var1);

    public String getDataitemLimitSql(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, String var10);

    public SafeSQL getDataItemSpecSql(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, String var10, String var11, String var12, String var13);

    public SafeSQL getStorageUnitHierarchySql(String var1);

    public SafeSQL getStorageEnvCondTypesSql(String var1);

    public String getRefTypeSql();
}

