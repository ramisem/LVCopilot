/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.opal.util.OpalUtil;
import java.util.HashMap;
import java.util.Map;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class SMSUser {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 54732 $";

    public static String getDefaultDepartment(QueryProcessor queryProcessor, String sysuserid) {
        String cd = "";
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT defaultdepartment FROM sysuser where sysuserid = ").append(safeSQL.addVar(sysuserid));
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            cd = ds.getValue(0, "defaultdepartment");
        }
        return cd;
    }

    public static boolean isGLP(QueryProcessor qp, String sysuserid) {
        String glpflag = OpalUtil.getColumnValue(qp, "sysuser", "glpflag", "sysuserid = ?", new String[]{sysuserid});
        return glpflag != null && "Y".equals(glpflag);
    }

    public static Map<String, String> getContactAddress(QueryProcessor qp, String sysuserid) {
        HashMap<String, String> map = new HashMap<String, String>();
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select sdiaddress.addressid, sdiaddress.addresstype");
        sql.append(" from sdiaddress ");
        sql.append(" where sdiaddress.sdcid = 'User' ");
        sql.append(" and sdiaddress.keyid1 = ").append(safeSQL.addVar(sysuserid));
        sql.append(" order by sdiaddress.usersequence");
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            map.put("addressid", ds.getValue(0, "addressid"));
            map.put("addresstype", ds.getValue(0, "addresstype"));
        }
        return map;
    }
}

