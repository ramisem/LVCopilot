/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.groovy;

import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.documents.Field;
import com.labvantage.sapphire.modules.documents.FieldProcessing;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.DBRead;
import com.labvantage.sapphire.util.groovy.GroovyLogger;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.WorkflowProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.LogContext;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ProcessingUtil {
    public static HashMap createFieldMap(SapphireConnection sapphireConnection, FieldProcessing fieldSource, PropertyList submittedValues, String dataProperty) {
        return ProcessingUtil.createFieldMap(sapphireConnection, fieldSource, submittedValues, dataProperty, false);
    }

    public static HashMap createFieldMap(SapphireConnection sapphireConnection, FieldProcessing fieldSource, PropertyList submittedValues, String dataProperty, boolean resetFieldInstance) {
        HashMap<String, Field> fieldMap = new HashMap<String, Field>();
        M18NUtil m18n = new M18NUtil(sapphireConnection);
        if (submittedValues != null) {
            for (String fieldid : submittedValues.keySet()) {
                PropertyListCollection instances = submittedValues.getCollection(fieldid);
                PropertyList field = fieldSource.getField(fieldid);
                if (field == null) continue;
                fieldMap.put(fieldid, new Field(field, instances, dataProperty, m18n, resetFieldInstance));
                ProcessingUtil.addSectionInstances(fieldMap, fieldid);
            }
        }
        return fieldMap;
    }

    public static void addSectionInstances(HashMap fieldMap, String fieldid) {
        String sectionid;
        Field formField = (Field)fieldMap.get(fieldid);
        if (formField != null && formField.isRepeatable() && (sectionid = formField.getProperty("sectionid")).length() > 0) {
            ArrayList values = formField.getValueList();
            ArrayList fieldinstances = (ArrayList)formField.get("fieldinstance");
            if (values.size() > 0) {
                for (int i = 0; i < values.size(); ++i) {
                    int instance = Integer.parseInt((String)fieldinstances.get(i));
                    HashMap sectionInstances = (HashMap)fieldMap.get(sectionid + "_" + instance);
                    if (sectionInstances == null) {
                        sectionInstances = new HashMap();
                        fieldMap.put(sectionid + "_" + instance, sectionInstances);
                    }
                    sectionInstances.put(fieldid, values.get(i));
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean processScript(SapphireConnection sapphireConnection, String script, HashMap bindings, StringBuffer log, String loggerName) throws Exception {
        DBUtil dbu = new DBUtil();
        try {
            HashMap bindingMap = new HashMap();
            for (String name : bindings.keySet()) {
                bindingMap.put(name, bindings.get(name));
            }
            bindingMap.put("user", new ConnectionInfo(sapphireConnection).getUserAttributeMap());
            ProcessingUtil.getSapphireObjectBindings(sapphireConnection, bindingMap, dbu, log, loggerName, true, true, true, true, true, true);
            script = ProcessingUtil.insertHeaderCode(script);
            GroovyUtil.getInstance(sapphireConnection).evaluateSecure(script, bindingMap);
            boolean bl = true;
            return bl;
        }
        finally {
            dbu.reset();
        }
    }

    public static HashMap createBindingsMap(SapphireConnection sapphireConnection, String loggerName) {
        return ProcessingUtil.createBindingsMap(sapphireConnection, loggerName, false, false, false, false, false);
    }

    public static HashMap createBindingsMap(SapphireConnection sapphireConnection, String loggerName, boolean ap, boolean qp, boolean sdcp, boolean sdip, boolean seqp) {
        HashMap<String, Object> bindings = new HashMap<String, Object>();
        bindings.put("user", new ConnectionInfo(sapphireConnection).getUserAttributeMap());
        bindings.put("m18n", new M18NUtil(sapphireConnection));
        if (loggerName != null && loggerName.length() > 0) {
            LogContext logContext = new LogContext(loggerName, sapphireConnection.getConnectionId());
            GroovyLogger logger = new GroovyLogger(logContext);
            bindings.put("logger", logger);
        }
        if (ap) {
            bindings.put("actionProcessor", new ActionProcessor(sapphireConnection.getConnectionId()));
        }
        if (qp) {
            bindings.put("queryProcessor", new QueryProcessor(sapphireConnection.getConnectionId()));
        }
        if (sdcp) {
            bindings.put("sdcProcessor", new SDCProcessor(sapphireConnection.getConnectionId()));
        }
        if (seqp) {
            bindings.put("sequenceProcessor", new SequenceProcessor(sapphireConnection.getConnectionId()));
        }
        if (sdip) {
            bindings.put("sdiProcessor", new SDIProcessor(sapphireConnection.getConnectionId()));
        }
        return bindings;
    }

    public static HashMap createBindingsMap(ActionProcessor ap, QueryProcessor qp, SDCProcessor sdcp, SDIProcessor sdip, SequenceProcessor seqp) {
        HashMap<String, BaseAccessor> bindings = new HashMap<String, BaseAccessor>();
        if (ap != null) {
            bindings.put("actionProcessor", ap);
        }
        if (qp != null) {
            bindings.put("queryProcessor", qp);
        }
        if (sdcp != null) {
            bindings.put("sdcProcessor", sdcp);
        }
        if (seqp != null) {
            bindings.put("sequenceProcessor", sdip);
        }
        if (sdip != null) {
            bindings.put("sdiProcessor", sdip);
        }
        return bindings;
    }

    public static void getSapphireObjectBindings(SapphireConnection sapphireConnection, HashMap bindingMap, DBUtil dbu, StringBuffer log, String loggerName, boolean ap, boolean qp, boolean sdcp, boolean sdip, boolean seqp, boolean wfp) {
        HashMap<String, Object> sapphireObjects = new HashMap<String, Object>();
        if (ap) {
            sapphireObjects.put("actionProcessor", new ActionProcessor(sapphireConnection.getConnectionId()));
        }
        if (qp) {
            sapphireObjects.put("queryProcessor", new QueryProcessor(sapphireConnection.getConnectionId()));
        }
        if (sdcp) {
            sapphireObjects.put("sdcProcessor", new SDCProcessor(sapphireConnection.getConnectionId()));
        }
        if (seqp) {
            sapphireObjects.put("sequenceProcessor", new SequenceProcessor(sapphireConnection.getConnectionId()));
        }
        if (sdip) {
            sapphireObjects.put("sdiProcessor", new SDIProcessor(sapphireConnection.getConnectionId()));
        }
        if (wfp) {
            sapphireObjects.put("workflowProcessor", new WorkflowProcessor(sapphireConnection.getConnectionId()));
        }
        LogContext logContext = new LogContext(loggerName, sapphireConnection.getConnectionId());
        GroovyLogger logger = new GroovyLogger(logContext, log);
        sapphireObjects.put("logger", logger);
        dbu.setConnection(sapphireConnection);
        if (dbu != null) {
            DBRead database = new DBRead(dbu);
            sapphireObjects.put("database", database);
        }
        M18NUtil m18n = new M18NUtil(sapphireConnection);
        sapphireObjects.put("m18n", m18n);
        bindingMap.put("sapphireobjects", sapphireObjects);
    }

    public static String insertHeaderCode(String script) {
        return ProcessingUtil.insertHeaderCode(script, null, true);
    }

    public static String insertHeaderCode(String script, boolean appendReturn) {
        return ProcessingUtil.insertHeaderCode(script, null, appendReturn);
    }

    public static String insertHeaderCode(String script, String extraDefs, boolean appendReturn) {
        if (script.indexOf("$G{") == 0) {
            script = script.substring(3);
            script = script.substring(0, script.lastIndexOf("}"));
        }
        script = "//startinsert\nimport sapphire.*;import sapphire.accessor.*;import sapphire.action.*;import sapphire.util.*;import sapphire.xml.*;import sapphire.util.ActionBlock;def logger = sapphireobjects.logger;def actionProcessor = sapphireobjects.actionProcessor;def queryProcessor = sapphireobjects.queryProcessor;def sdcProcessor = sapphireobjects.sdcProcessor;def sdiProcessor = sapphireobjects.sdiProcessor;def workflowProcessor = sapphireobjects.workflowProcessor;def database = sapphireobjects.database;def m18n = sapphireobjects.m18n;def processAction = { actionid, actionprops -> actionProcessor.processAction( actionid, \"1\", actionprops ) };def processActionBlock = { actionblock -> actionProcessor.processActionBlock( actionblock ) };def getSQLDataSet = { sql -> queryProcessor.getSqlDataSet( sql ) };" + (extraDefs != null ? extraDefs : "") + "def getScriptClass = { scriptClass -> sapphire.ext.BaseGroovy.getInstance( scriptClass, this ) };//endinsert\n" + script + (script.indexOf("return") == -1 && appendReturn ? ";\nreturn 1;" : "");
        return script;
    }

    public static String stripHeaderCode(String text) {
        if (text.contains("//startinsert") && text.contains("//endinsert")) {
            return text.substring(0, text.indexOf("//startinsert")) + "\n" + text.substring(text.indexOf("//endinsert") + 11);
        }
        return text;
    }
}

