/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.sql;

import com.labvantage.opal.sql.MSSSqlGenerator;
import com.labvantage.opal.sql.ORASqlGenerator;
import com.labvantage.opal.sql.SQLGenerator;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class SQLFactory {
    protected String LABVANTAGE_CVS_ID = "$Revision: 53248 $";
    public static final String DBMS_ORA = "ORA";
    public static final String DBMS_MSS = "MSS";

    public static SQLGenerator getSqlGenerator(boolean ora) {
        if (ora) {
            return new ORASqlGenerator();
        }
        return new MSSSqlGenerator();
    }

    public static SQLGenerator getSqlGenerator(PageContext pageContext) {
        String dbms = SQLFactory.getDBMS(pageContext);
        if (dbms.equalsIgnoreCase(DBMS_ORA)) {
            return new ORASqlGenerator(pageContext, DBMS_ORA);
        }
        if (dbms.equalsIgnoreCase(DBMS_MSS)) {
            return new MSSSqlGenerator(pageContext, DBMS_MSS);
        }
        return null;
    }

    public static SQLGenerator getSqlGenerator(String connectionId) {
        String dbms = "";
        dbms = SQLFactory.getDBMS(connectionId);
        if (dbms.equalsIgnoreCase(DBMS_ORA)) {
            return new ORASqlGenerator(connectionId, dbms);
        }
        if (dbms.equalsIgnoreCase(DBMS_MSS)) {
            return new MSSSqlGenerator(connectionId, dbms);
        }
        return null;
    }

    public static String getDBMS(PageContext pageContext) {
        String dbms = "";
        PropertyList plRequestdata = null;
        if (pageContext.getAttribute("requestdata", 2) != null) {
            plRequestdata = (PropertyList)pageContext.getAttribute("requestdata", 2);
            dbms = plRequestdata.getProperty("dbms");
        }
        return dbms;
    }

    public static String getDBMS(String connectionId) {
        String dbms = "";
        QueryProcessor qp = new QueryProcessor(connectionId);
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select connectionid, sysuserid, dbms from connection where connectionid=" + safeSQL.addVar(connectionId);
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds.getRowCount() > 0) {
            dbms = ds.getValue(0, "dbms", "");
        }
        return dbms;
    }
}

