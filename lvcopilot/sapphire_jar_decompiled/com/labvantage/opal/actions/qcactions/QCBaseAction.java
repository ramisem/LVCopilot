/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import com.labvantage.sapphire.Trace;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;

public abstract class QCBaseAction
extends BaseAction {
    public static String LABVANTAGE_CVS_ID = "$Revision: 53489 $";

    @Override
    public abstract int processAction(String var1, String var2, HashMap var3);

    public static String getParameterType(QueryProcessor queryProcessor, String actionid) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        String actionClass = null;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT OBJECTNAME FROM ACTION WHERE ACTIONID = ").append(safeSQL.addVar(actionid));
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds == null) {
            Logger.logError("DataSet is null");
            return null;
        }
        actionClass = ds.getValue(0, "objectname");
        Class<?> c = Class.forName(actionClass);
        Method m = c.getDeclaredMethod("getQCParameterType", null);
        Object parameterType = m.invoke(c.newInstance(), null);
        return parameterType.toString();
    }

    public static boolean hasBracket(QueryProcessor queryProcessor, String actionid) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        String actionClass = null;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT OBJECTNAME FROM ACTION WHERE ACTIONID = ").append(safeSQL.addVar(actionid));
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds == null) {
            Logger.logError("DataSet is null");
            return false;
        }
        actionClass = ds.getValue(0, "objectname");
        boolean returnvalue = false;
        try {
            Class<?> c = Class.forName(actionClass);
            Method m = c.getDeclaredMethod("hasBracket", null);
            Boolean b = (Boolean)m.invoke(c.newInstance(), null);
            returnvalue = b;
        }
        catch (ClassNotFoundException e) {
            Trace.logInfo("QCBatch Error - Class not found for action " + actionid + " (class=" + actionClass + ")");
        }
        return returnvalue;
    }
}

