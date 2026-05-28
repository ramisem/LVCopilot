/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class QCEvalRule
extends BaseSDCRules {
    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        DataSet linkedQCMethodSampleTypeDS = null;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append(" SELECT s_qcmethodsampletypeid FROM s_qcmethodsampletype ");
        sql.append(" WHERE qcevalruleid IN ( ");
        sql.append(" SELECT keyid1 FROM rsetitems ");
        sql.append(" WHERE rsetid = " + safeSQL.addVar(rsetid));
        sql.append(" ) ");
        linkedQCMethodSampleTypeDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (linkedQCMethodSampleTypeDS.size() > 0) {
            this.throwError("QCEvalRuleError", "VALIDATION", "One or more QC Rule(s) in " + actionProps.getProperty("keyid1") + ", are in use. Cannot be deleted.");
        }
        if (linkedQCMethodSampleTypeDS != null) {
            linkedQCMethodSampleTypeDS = null;
        }
        sql = null;
    }
}

