/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import java.util.ArrayList;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class TableInfo {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 54146 $";

    public static String getSDCId(String tableid, QueryProcessor qp, String dbms) throws Exception {
        String sdcid = null;
        SQLGenerator sqlGenerator = SQLFactory.getSqlGenerator("ORA".equalsIgnoreCase(dbms));
        if (sqlGenerator == null) {
            throw new Exception("Couldnot get SQLGenerator. Unsupported database.");
        }
        SafeSQL safeSQL = sqlGenerator.getTableSDCSQL(tableid);
        DataSet dataset = qp.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        if (dataset.getRowCount() > 0) {
            sdcid = dataset.getValue(0, "sdcid");
        }
        return sdcid;
    }

    public static ArrayList getPrimaryKeys(String tableid, PageContext pageContext) throws Exception {
        ArrayList<String> primaryKeys = new ArrayList<String>();
        SQLGenerator sqlGenerator = SQLFactory.getSqlGenerator(pageContext);
        if (sqlGenerator == null) {
            throw new Exception("Couldnot get SQLGenerator. Unsupported database.");
        }
        SafeSQL safeSQL = sqlGenerator.getTableKeysSQL(tableid);
        QueryProcessor qp = new QueryProcessor(pageContext);
        DataSet dataset = qp.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        for (int i = 0; i < dataset.getRowCount(); ++i) {
            primaryKeys.add(i, dataset.getValue(i, "columnid"));
        }
        return primaryKeys;
    }
}

