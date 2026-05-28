/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.validation.sample;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.validation.BaseAjaxValidation;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public abstract class BaseSampleValidation
extends BaseAjaxValidation {
    protected static final String MODE_CHECKSTORAGEUNIT = "CHECKSTORAGEUNIT";
    protected static final String MODE_CHECKDOMAIN = "CHECKDOMAIN";
    protected static final String MODE_ALL = "ALL";
    protected static final String PARAMETER_KEYID1 = "keyid1";

    protected DataSet getSamples(String keyId1) {
        DataSet ds;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        if (this.getConnectionProcessor().isOra()) {
            safeSQL.reset();
            sql.append("select distinct s.s_sampleid, previousstoragestatus, ").append(" ( select custodialdepartmentid from trackitem where linksdcid = 'Sample' and ").append("linkkeyid1 = s.s_sampleid ) custodialdomain, ").append("NVL( (select 'true' returnflag from trackitem where linksdcid = 'Sample' and ").append("linkkeyid1 = s.s_sampleid and custodialdepartmentid IN (select departmentid from departmentsysuser ").append("where sysuserid = ").append(safeSQL.addVar(this.getSysUserId())).append(") ),'false') isuserdept, ").append("NVL( (select 'false' from trackitem where linksdcid = 'Sample' and linkkeyid1 = s_sampleid ").append("and (currentstorageunitid is null or LV_SMSQuery.GetStorageNodeBySDC(currentstorageunitid,'LV_Package') is null )) , 'true' ) insidestorageunit, storagestatus");
            sql.append(" from s_sample s ");
            sql.append(" where s_sampleid in (").append(safeSQL.addIn(keyId1, ";")).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        } else {
            safeSQL.reset();
            sql.append("select distinct s.s_sampleid, previousstoragestatus, storagestatus, ").append(" ( select custodialdepartmentid from trackitem where linksdcid = 'Sample' and ").append("linkkeyid1 = s.s_sampleid ) custodialdomain, ").append(" ( select currentstorageunitid from trackitem where linksdcid = 'Sample' and ").append("linkkeyid1 = s.s_sampleid ) currentstorageunitid, ").append("ISNULL( (select 'true' returnflag from trackitem where linksdcid = 'Sample' and ").append("linkkeyid1 = s.s_sampleid and custodialdepartmentid IN (select departmentid from departmentsysuser ").append("where sysuserid = ").append(safeSQL.addVar(this.getSysUserId())).append(") ),'false') isuserdept");
            sql.append(" from s_sample s ");
            sql.append(" where s_sampleid in (").append(safeSQL.addIn(keyId1, ";")).append(")");
            sql.append(" order by s.s_sampleid");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                int size = ds.size();
                ds.addColumn("insidestorageunit", 0);
                for (int i = 0; i < size; ++i) {
                    String currentpackageid;
                    String sampleinpackage = "false";
                    String currentstorageunitid = ds.getValue(i, "currentstorageunitid");
                    if (StringUtil.getLen(currentstorageunitid) > 0L && StringUtil.getLen(currentpackageid = StorageUnitSDC.getStorageNodeBySDC(this.getQueryProcessor(), currentstorageunitid, "LV_Package")) > 0L) {
                        sampleinpackage = "true";
                    }
                    ds.setValue(i, "insidestorageunit", sampleinpackage);
                }
            }
        }
        return ds;
    }

    protected void validate(String validationMode, DataSet ds, ErrorHandler errorHandler, String message) {
        for (int i = 0; i < ds.size(); ++i) {
            String _custodialDomain = ds.getValue(i, "custodialdomain");
            String _isUserDept = ds.getValue(i, "isuserdept");
            String _insideStorageUnit = ds.getValue(i, "insidestorageunit");
            String _previousStorageStatus = ds.getValue(i, "previousstoragestatus", "");
            if (validationMode.equals(MODE_ALL) || validationMode.equals(MODE_CHECKDOMAIN)) {
                if (!OpalUtil.isEmpty(_custodialDomain)) {
                    if (!_isUserDept.equals("true")) {
                        errorHandler.add(message, "", this.getTranslationProcessor().translate("Validation failure"), "VALIDATION", this.getTranslationProcessor().translate("Operation not allowed.") + " " + this.getTranslationProcessor().translate("User must be a member of selected sample's Custodial Department."));
                        continue;
                    }
                } else {
                    if ("Allocated".equals(_previousStorageStatus)) continue;
                    errorHandler.add(message, "", this.getTranslationProcessor().translate("Validation failure"), "VALIDATION", this.getTranslationProcessor().translate("Operation not allowed.") + " " + this.getTranslationProcessor().translate("One or more selected sample(s) does not belong to any Custodial Department."));
                    continue;
                }
            }
            if (!validationMode.equals(MODE_ALL) && !validationMode.equals(MODE_CHECKSTORAGEUNIT) || !_insideStorageUnit.equals("true")) continue;
            errorHandler.add(message, "", this.getTranslationProcessor().translate("Validation failure"), "VALIDATION", this.getTranslationProcessor().translate("Operation not allowed.") + " " + this.getTranslationProcessor().translate("Selected sample is in a Storage Unit."));
        }
    }
}

