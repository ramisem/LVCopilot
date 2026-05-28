/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.ddt;

import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DDTUtil {
    public static void appendUserColumns(PageContext pageContext, String var, String tableid) {
        RequestContext requestContext = RequestContext.getRequestContext(pageContext);
        QueryProcessor qp = new QueryProcessor(pageContext);
        SafeSQL safeSQL = new SafeSQL();
        DataSet userCols = qp.getPreparedSqlDataSet("SELECT columnid, columndesc FROM syscolumn WHERE tableid = " + safeSQL.addVar(tableid) + " AND columntype = 'U'", safeSQL.getValues());
        if (userCols != null) {
            PropertyListCollection columns = requestContext.getPropertyList(var).getCollection("columns");
            for (int i = 0; i < userCols.size(); ++i) {
                PropertyList column = new PropertyList(userCols.getString(i, "columnid"));
                column.setProperty("columnid", userCols.getString(i, "columnid"));
                column.setProperty("title", userCols.getString(i, "columndesc"));
                columns.add(column);
            }
        }
    }

    public static boolean checkTableExists(DBAccess database, String tableid) throws SapphireException {
        database.createPreparedResultSet("SELECT tableid FROM systable WHERE Lower( tableid ) = ?", new Object[]{tableid.toLowerCase()});
        return database.getNext();
    }
}

