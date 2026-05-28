/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.validation;

import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public abstract class BaseValidation
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54720 $";
    public static final String YES = "Y";
    public static final String NO = "N";
    private SapphireConnection sapphireConnection;

    protected SapphireConnection getSapphireConnection() {
        if (this.sapphireConnection == null) {
            this.sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
        }
        return this.sapphireConnection;
    }

    protected boolean isDepartmentMember(String department) {
        return this.getDepartmentList().contains(department);
    }

    public static String getStorageUnitID(QueryProcessor qp, String sdcid, String keyid1) {
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = qp.getPreparedSqlDataSet(new StringBuffer().append("SELECT STORAGEUNITID FROM STORAGEUNIT WHERE LINKSDCID = ").append(safeSQL.addVar(sdcid)).append(" AND LINKKEYID1 = ").append(safeSQL.addVar(keyid1)).toString(), safeSQL.getValues());
        if (ds != null) {
            return ds.getValue(0, "STORAGEUNITID");
        }
        return "";
    }

    protected String getSysUserId() {
        return this.getSapphireConnection().getSysuserId();
    }

    protected boolean isBBRuleActive(String ruleName) throws SapphireException {
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
        return policy != null && "Active".equals(policy.getPropertyListNotNull("rules").getProperty(ruleName, "Active"));
    }
}

