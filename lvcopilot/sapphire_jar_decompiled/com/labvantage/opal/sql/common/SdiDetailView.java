/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.common;

import sapphire.util.SafeSQL;

public class SdiDetailView {
    private String LABVANTAGE_CVS_ID = "$Revision: 54170 $";

    public static String getPrimarySql(String primarySelectList, String sampleIdWhere, String _tableid, String _sdcId, String _keyColId1) {
        StringBuffer sbPrimarySql = new StringBuffer();
        sbPrimarySql.append(" select " + primarySelectList + " ");
        sbPrimarySql.append(" from " + _tableid);
        sbPrimarySql.append(sampleIdWhere);
        sbPrimarySql.append(" order by usersequence, " + _keyColId1);
        return sbPrimarySql.toString();
    }

    public static String getTableidSql(String _sdcId) {
        StringBuffer _tableidSql = new StringBuffer("");
        _tableidSql.append(" select tableid from sdc where sdcid = ");
        _tableidSql.append("'" + _sdcId + "'");
        return _tableidSql.toString();
    }

    public static SafeSQL getSpecSql(String _keyid1, String _paramlistid, String _paramid) {
        StringBuffer sbSpecSql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbSpecSql.append(" select distinct b.specid, a.specdesc, b.specversionid,  b.keyid1, b.keyid2, b.keyid3  from spec a, sdispec b, sdidataitemspec c  where b.keyid1 = " + safeSQL.addVar(_keyid1) + " and a.specid = b.specid  and a.specversionid = b.specversionid ");
        if (!_paramlistid.equalsIgnoreCase("")) {
            sbSpecSql.append(" and c.paramlistid = " + safeSQL.addVar(_paramlistid));
        }
        if (!_paramid.equalsIgnoreCase("")) {
            sbSpecSql.append(" and c.paramid = " + safeSQL.addVar(_paramid));
        }
        sbSpecSql.append(" and c.keyid1 = b.keyid1  and c.sdcid = b.sdcid  and c.specid = b.specid ");
        safeSQL.setPreparedSQL(sbSpecSql.toString());
        return safeSQL;
    }

    public static SafeSQL getDatasetSql(String _keyid1, String _paramlistid, String _paramlistversionid, String _variantid, String _dataset, String _specid) {
        StringBuffer sbDatasetSql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbDatasetSql.append("select distinct a.sdcid, a.keyid1, a.keyid2,  a.keyid3, a.usersequence, a.paramlistid,  a.paramlistversionid, a.variantid, a.dataset, b.specid  from sdidata a, sdidataitemspec b  where a.sdcid = b.sdcid  and a.keyid1 = " + safeSQL.addVar(_keyid1) + " and a.keyid1 = b.keyid1  and b.specid = " + safeSQL.addVar(_specid) + "  and a.paramlistid = b.paramlistid ");
        if (!_paramlistid.equalsIgnoreCase("")) {
            sbDatasetSql.append(" and a.paramlistid = " + safeSQL.addVar(_paramlistid));
        }
        if (!_paramlistversionid.equalsIgnoreCase("")) {
            sbDatasetSql.append(" and a.paramlistversionid = " + safeSQL.addVar(_paramlistversionid));
        }
        if (!_variantid.equalsIgnoreCase("")) {
            sbDatasetSql.append(" and a.variantid = " + safeSQL.addVar(_variantid));
        }
        if (!_dataset.equalsIgnoreCase("")) {
            sbDatasetSql.append(" and a.dataset = " + safeSQL.addVar(_dataset));
        }
        if (!_specid.equalsIgnoreCase("")) {
            sbDatasetSql.append(" and b.specid = " + safeSQL.addVar(_specid));
        }
        sbDatasetSql.append(" order by usersequence, paramlistid, variantid, dataset");
        safeSQL.setPreparedSQL(sbDatasetSql.toString());
        return safeSQL;
    }

    public static SafeSQL getApprovalSql(String _sdcId, String _keyid1, String _paramlistid, String _paramlistversionid, String _variantid, String _dataset) {
        StringBuffer sbDataApprovalSql = new StringBuffer("");
        SafeSQL safeSQL = new SafeSQL();
        sbDataApprovalSql.append(" select sdcid, keyid1, keyid2, keyid3, usersequence, ");
        sbDataApprovalSql.append(" paramlistid, paramlistversionid, variantid, dataset, ");
        sbDataApprovalSql.append(" approvalstep, roleid, mandatoryflag, approvalflag ");
        sbDataApprovalSql.append(" from sdidataapproval where ");
        sbDataApprovalSql.append(" sdcid = " + safeSQL.addVar(_sdcId) + " ");
        sbDataApprovalSql.append(" and keyid1 = " + safeSQL.addVar(_keyid1));
        sbDataApprovalSql.append(" and keyid2 = '(null)' ");
        sbDataApprovalSql.append(" and keyid3 = '(null)' ");
        if (!_paramlistid.equalsIgnoreCase("")) {
            sbDataApprovalSql.append(" and paramlistid = " + safeSQL.addVar(_paramlistid));
        }
        if (!_paramlistversionid.equalsIgnoreCase("")) {
            sbDataApprovalSql.append(" and paramlistversionid = " + safeSQL.addVar(_paramlistversionid));
        }
        if (!_variantid.equalsIgnoreCase("")) {
            sbDataApprovalSql.append(" and variantid = " + safeSQL.addVar(_variantid));
        }
        if (!_dataset.equalsIgnoreCase("")) {
            sbDataApprovalSql.append(" and dataset = " + safeSQL.addVar(_dataset));
        }
        sbDataApprovalSql.append("order by usersequence, keyid1, paramlistid, paramlistversionid, variantid, dataset, usersequence");
        safeSQL.setPreparedSQL(sbDataApprovalSql.toString());
        return safeSQL;
    }

    public static SafeSQL getDataitemSql(String _sdcId, String _keyid1, String _specid, String _paramid, String _paramlistid, String _paramlistversionid, String _variantid, String _dataset, String _paramtype, String _replicateid) {
        StringBuffer sbDataitemSql = new StringBuffer("");
        SafeSQL safeSQL = new SafeSQL();
        sbDataitemSql.append(" select distinct  a.sdcid, a.keyid1, a.keyid2, a.keyid3,  a.paramid, a.paramlistid, a.paramlistversionid,  a.paramtype, a.variantid, a.dataset, a.replicateid,  a.enteredtext, a.displayvalue, a.usersequence,  a.displayunits, a.condition  from sdidataitem a, sdidataitemspec b  where a.sdcid =  " + safeSQL.addVar(_sdcId) + "  and a.sdcid = b.sdcid   and a.keyid1  =   " + safeSQL.addVar(_keyid1) + " and a.keyid1 = b.keyid1  and b.specid = " + safeSQL.addVar(_specid) + " and a.paramid = b.paramid ");
        if (!_paramid.equalsIgnoreCase("")) {
            sbDataitemSql.append(" and a.paramid = " + safeSQL.addVar(_paramid));
        }
        if (!_paramlistid.equalsIgnoreCase("")) {
            sbDataitemSql.append(" and a.paramlistid = " + safeSQL.addVar(_paramlistid));
        }
        if (!_paramlistversionid.equalsIgnoreCase("")) {
            sbDataitemSql.append(" and a.paramlistversionid = " + safeSQL.addVar(_paramlistversionid));
        }
        if (!_variantid.equalsIgnoreCase("")) {
            sbDataitemSql.append(" and a.variantid = " + safeSQL.addVar(_variantid));
        }
        if (!_dataset.equalsIgnoreCase("")) {
            sbDataitemSql.append(" and a.dataset = " + safeSQL.addVar(_dataset));
        }
        if (!_paramtype.equalsIgnoreCase("")) {
            sbDataitemSql.append(" and a.paramtype = " + safeSQL.addVar(_paramtype));
        }
        if (!_replicateid.equalsIgnoreCase("")) {
            sbDataitemSql.append(" and a.replicateid = " + safeSQL.addVar(_replicateid));
        }
        sbDataitemSql.append(" order by a.usersequence, a.paramid, a.paramtype, a.replicateid ");
        safeSQL.setPreparedSQL(sbDataitemSql.toString());
        return safeSQL;
    }

    public static SafeSQL getSpecParamLimitsSql(String _keyid1) {
        StringBuffer sbSpecParamLimitsSql = new StringBuffer("");
        SafeSQL safeSQL = new SafeSQL();
        sbSpecParamLimitsSql.append(" select  a.unitsid, a.operator1, a.operator2,  a.value1, a.value2  from specparamlimits a, sdispec b, sdidataitem c  where a.specid = b.specid  and b.keyid1 = c.keyid1  and b.keyid1 = " + safeSQL.addVar(_keyid1));
        safeSQL.setPreparedSQL(sbSpecParamLimitsSql.toString());
        return safeSQL;
    }

    public static String getDataitemLimitSql(String dataitemlimitSelectList, String _sdcId, String keyidWhere, String _paramid, String _paramlistid, String _paramlistversionid, String _paramtype, String _variantid, String _dataset, String _replicateid) {
        StringBuffer _sbDataitemLimitSql = new StringBuffer("");
        _sbDataitemLimitSql.append(" select " + dataitemlimitSelectList + " ");
        _sbDataitemLimitSql.append(" from sdidataitemlimits where ");
        _sbDataitemLimitSql.append(" sdcid = '" + _sdcId + "' ");
        _sbDataitemLimitSql.append(keyidWhere);
        _sbDataitemLimitSql.append(" and keyid2 = '(null)' ");
        _sbDataitemLimitSql.append(" and keyid3 = '(null)' ");
        if (!_paramid.equalsIgnoreCase("")) {
            _sbDataitemLimitSql.append(" and paramid = '" + _paramid + "'");
        }
        if (!_paramlistid.equalsIgnoreCase("")) {
            _sbDataitemLimitSql.append(" and paramlistid = '" + _paramlistid + "'");
        }
        if (!_paramlistversionid.equalsIgnoreCase("")) {
            _sbDataitemLimitSql.append(" and paramlistversionid = '" + _paramlistversionid + "'");
        }
        if (!_paramtype.equalsIgnoreCase("")) {
            _sbDataitemLimitSql.append(" and paramtype = '" + _paramtype + "'");
        }
        if (!_variantid.equalsIgnoreCase("")) {
            _sbDataitemLimitSql.append(" and variantid = '" + _variantid + "'");
        }
        if (!_dataset.equalsIgnoreCase("")) {
            _sbDataitemLimitSql.append(" and dataset = '" + _dataset + "'");
        }
        if (!_replicateid.equalsIgnoreCase("")) {
            _sbDataitemLimitSql.append(" and replicateid = '" + _replicateid + "'");
        }
        _sbDataitemLimitSql.append(" order by keyid1, paramlistid, paramlistversionid, ");
        _sbDataitemLimitSql.append(" variantid, dataset, paramid, paramtype, replicateid, ");
        _sbDataitemLimitSql.append(" limittypeid, usersequence ");
        return _sbDataitemLimitSql.toString();
    }

    public static SafeSQL getDataItemSpecSql(String _sdcId, String _keyid1, String _keyid2, String _keyid3, String _paramid, String _paramlistid, String _paramlistversionid, String _variantid, String _dataset, String _paramtype, String _replicateid, String _specid, String _specversionid, boolean isOra) {
        StringBuffer sbDataItemSpecSql = new StringBuffer("");
        SafeSQL safeSQL = new SafeSQL();
        sbDataItemSpecSql.append("select ");
        sbDataItemSpecSql.append(" distinct sdis.sdcid, sdis.keyid1, sdis.displayvalue, sdis.condition, ");
        sbDataItemSpecSql.append(" sdis.paramid, sdis.paramlistid, sdis.paramlistversionid, sdis.paramtype, sdis.replicateid, ");
        sbDataItemSpecSql.append(" sdis.variantid, sdis.dataset, sdis.specid, sdis.specversionid, sdis.usersequence, spl.limittypesequence, ");
        if (isOra) {
            sbDataItemSpecSql.append(" spl.operator1 || spl.value1 val1, spl.operator2 || spl.value2 val2, ");
            sbDataItemSpecSql.append(" slt.condition, slt.limittypeid || '(' || slt.condition || ')' typecondition, slt.limittypeid ");
        } else {
            sbDataItemSpecSql.append(" spl.operator1 + spl.value1 val1, spl.operator2 + spl.value2 val2, ");
            sbDataItemSpecSql.append(" slt.condition, slt.limittypeid + '(' + slt.condition + ')' typecondition, slt.limittypeid ");
        }
        sbDataItemSpecSql.append(" from sdidataitemspec sdis, specparamitems spi, specparamlimits spl, speclimittype slt ");
        sbDataItemSpecSql.append(" where ");
        sbDataItemSpecSql.append(" sdis.sdcid = ").append(safeSQL.addVar(_sdcId)).append(" ");
        sbDataItemSpecSql.append(" and sdis.keyid1 = ").append(safeSQL.addVar(_keyid1)).append(" ");
        if (_keyid2.length() > 0) {
            sbDataItemSpecSql.append(" and sdis.keyid2 = ").append(safeSQL.addVar(_keyid2)).append(" ");
        } else {
            sbDataItemSpecSql.append(" and sdis.keyid2 = '(null)' ");
        }
        if (_keyid3.length() > 0) {
            sbDataItemSpecSql.append(" and sdis.keyid3 = ").append(safeSQL.addVar(_keyid3)).append(" ");
        } else {
            sbDataItemSpecSql.append(" and sdis.keyid3 = '(null)' ");
        }
        if (_paramlistid.length() > 0) {
            sbDataItemSpecSql.append(" and sdis.paramlistid = ").append(safeSQL.addVar(_paramlistid)).append("  ");
        }
        if (_paramlistversionid.length() > 0) {
            sbDataItemSpecSql.append(" and sdis.paramlistversionid = ").append(safeSQL.addVar(_paramlistversionid)).append("  ");
        }
        if (_variantid.length() > 0) {
            sbDataItemSpecSql.append(" and sdis.variantid = ").append(safeSQL.addVar(_variantid)).append("  ");
        }
        if (_dataset.length() > 0) {
            sbDataItemSpecSql.append(" and sdis.dataset = ").append(safeSQL.addVar(_dataset)).append("   ");
        }
        if (_paramid.length() > 0) {
            sbDataItemSpecSql.append(" and sdis.paramid = ").append(safeSQL.addVar(_paramid)).append(" ");
        }
        if (_paramtype.length() > 0) {
            sbDataItemSpecSql.append(" and sdis.paramtype = ").append(safeSQL.addVar(_paramtype)).append(" ");
        }
        if (_replicateid.length() > 0) {
            sbDataItemSpecSql.append(" and sdis.replicateid = ").append(safeSQL.addVar(_replicateid)).append(" ");
        }
        if (_specid.length() > 0) {
            sbDataItemSpecSql.append(" and sdis.specid = ").append(safeSQL.addVar(_specid)).append(" ");
        }
        if (_specversionid.length() > 0) {
            sbDataItemSpecSql.append(" and sdis.specversionid = ").append(safeSQL.addVar(_specversionid)).append(" ");
        }
        sbDataItemSpecSql.append(" and sdis.specid = spi.specid     ");
        sbDataItemSpecSql.append(" and sdis.specversionid = spi.specversionid   ");
        sbDataItemSpecSql.append(" and   ");
        sbDataItemSpecSql.append(" ( ");
        sbDataItemSpecSql.append(" ( ");
        sbDataItemSpecSql.append(" spi.allowanyparamlistflag = 'Y' ");
        sbDataItemSpecSql.append(" ) ");
        sbDataItemSpecSql.append("   or    ");
        sbDataItemSpecSql.append(" ( ");
        sbDataItemSpecSql.append(" sdis.paramlistid = spi.paramlistid ");
        sbDataItemSpecSql.append(" and sdis.paramlistversionid = spi.paramlistversionid  ");
        sbDataItemSpecSql.append(" and sdis.variantid = spi.variantid ");
        sbDataItemSpecSql.append(" and (spi.allowanyparamlistflag = 'N' or spi.allowanyparamlistflag is null or spi.allowanyparamlistflag = '') ");
        sbDataItemSpecSql.append(" ) ");
        sbDataItemSpecSql.append("  or   ");
        sbDataItemSpecSql.append(" ( ");
        sbDataItemSpecSql.append(" sdis.paramlistid = spi.paramlistid ");
        sbDataItemSpecSql.append(" and sdis.variantid = spi.variantid ");
        sbDataItemSpecSql.append(" and spi.allowanyparamlistflag = 'V' ");
        sbDataItemSpecSql.append(" ) ");
        sbDataItemSpecSql.append("  or   ");
        sbDataItemSpecSql.append(" ( ");
        sbDataItemSpecSql.append(" sdis.paramlistid = spi.paramlistid ");
        sbDataItemSpecSql.append(" and spi.allowanyparamlistflag = 'A' ");
        sbDataItemSpecSql.append(" ) ");
        sbDataItemSpecSql.append(" ) ");
        sbDataItemSpecSql.append(" and sdis.paramid = spi.paramid  ");
        sbDataItemSpecSql.append(" and sdis.paramtype = spi.paramtype ");
        sbDataItemSpecSql.append(" and spl.specid = spi.specid   ");
        sbDataItemSpecSql.append(" and spl.specversionid = spi.specversionid ");
        sbDataItemSpecSql.append(" and spl.paramlistid = spi.paramlistid ");
        sbDataItemSpecSql.append(" and spl.paramlistversionid = spi.paramlistversionid ");
        sbDataItemSpecSql.append(" and spl.variantid = spi.variantid ");
        sbDataItemSpecSql.append(" and spl.paramid = spi.paramid ");
        sbDataItemSpecSql.append(" and spl.paramtype = spi.paramtype ");
        sbDataItemSpecSql.append(" and slt.specid = spl.specid ");
        sbDataItemSpecSql.append(" and slt.specversionid = spl.specversionid ");
        sbDataItemSpecSql.append(" and slt.limittypesequence = spl.limittypesequence ");
        sbDataItemSpecSql.append(" order by sdis.usersequence ");
        safeSQL.setPreparedSQL(sbDataItemSpecSql.toString());
        return safeSQL;
    }
}

