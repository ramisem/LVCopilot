/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_SamplingPlan
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54325 $";
    public static final String SDCID = "LV_SamplingPlan";
    public static final String COLUMN_EMBEDDEDFLAG = "embeddedflag";

    @Override
    public void preDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        String linkId = actionProps.getProperty("linkid", "");
        String[] spIds = StringUtil.split(actionProps.getProperty("s_samplingplanid", ""), ";");
        String[] spVerIds = StringUtil.split(actionProps.getProperty("s_samplingplanversionid", ""), ";");
        if ("detail".equalsIgnoreCase(linkId)) {
            String[] spDetailNos = StringUtil.split(actionProps.getProperty("s_samplingplandetailno", ""), ";");
            StringBuffer deleteSPDetailItems = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            deleteSPDetailItems.append("DELETE s_spdetailitem WHERE");
            for (int i = 0; i < spDetailNos.length; ++i) {
                if (i > 0) {
                    deleteSPDetailItems.append(" OR ");
                }
                deleteSPDetailItems.append(" (");
                deleteSPDetailItems.append(" s_samplingplandetailno = ").append(safeSQL.addVar(spDetailNos[i]));
                deleteSPDetailItems.append(" AND s_samplingplanid = ").append(safeSQL.addVar(spIds[i]));
                deleteSPDetailItems.append(" AND s_samplingplanversionid = ").append(safeSQL.addVar(spVerIds[i]));
                deleteSPDetailItems.append(" )");
            }
            this.logger.info("Deleting Sampling Plan Detail items: - " + deleteSPDetailItems.toString());
            this.database.executePreparedUpdate(deleteSPDetailItems.toString(), safeSQL.getValues());
        }
    }
}

