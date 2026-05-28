/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.util.DataSet;

public class QueryRO
extends BaseSDCRO {
    public static final String SEPARATOR = "|!|";

    public void initialize(SapphireConnection connection) throws SapphireException {
        super.initialize("Query", connection);
    }

    public String getQueryId() {
        return this.getKeyid1();
    }

    public String getQueryBasedOnSDC() {
        return this.getKeyid2();
    }

    public String getQueryDesc() {
        return this.getDescription();
    }

    public String getFromClause() {
        return this.getPrimaryValue("fromclause");
    }

    public String getWhereClause() {
        return this.getPrimaryValue("whereclause");
    }

    public String getSelectClause() {
        return this.getPrimaryValue("selectclause");
    }

    public String getOrderByClause() {
        return this.getPrimaryValue("orderbyclause");
    }

    public String getCascadedArgFlag() {
        String flag = this.getPrimaryValue("cascadedargflag");
        if ("Y".equals(flag)) {
            return "Yes";
        }
        return "No";
    }

    public String getDistinctFlag() {
        String flag = this.getPrimaryValue("distinctflag");
        if ("Y".equals(flag)) {
            return "Yes";
        }
        return "No";
    }

    public DataSet getQueryArgs() {
        return this.getDataSet("queryarg");
    }
}

