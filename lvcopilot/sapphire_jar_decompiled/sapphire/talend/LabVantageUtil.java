/*
 * Decompiled with CFR 0.152.
 */
package sapphire.talend;

import com.labvantage.sapphire.modules.sdms.handlers.TalendUtil;
import java.util.Map;
import java.util.Properties;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class LabVantageUtil {
    public static void startJob(String jobName, Properties context, Map<String, Object> globalmap) {
        TalendUtil talendUtil = TalendUtil.getTalendUtil(jobName, context, globalmap, null, true);
    }

    public static String getVariable(String name, String defaultValue, Properties context, Map<String, Object> globalmap) {
        return TalendUtil.getVariable(name, defaultValue, context, globalmap);
    }

    public static String getVariable(String name, String defaultValue, Properties context, Map<String, Object> globalmap, boolean ignoreCase) {
        return TalendUtil.getVariable(name, defaultValue, context, globalmap, ignoreCase);
    }

    public static DataSet getSQLDataSet(String query, Properties context, Map<String, Object> globalmap) {
        TalendUtil talendUtil = TalendUtil.getTalendUtil(context, globalmap, null, true);
        return talendUtil.getSQLDataSet(query);
    }

    public static PropertyList processAction(String actionid, String actionversion, PropertyList propertyList, Properties context, Map<String, Object> globalmap) {
        TalendUtil talendUtil = TalendUtil.getTalendUtil(context, globalmap, null, true);
        return talendUtil.processAction(actionid, actionversion, propertyList);
    }
}

