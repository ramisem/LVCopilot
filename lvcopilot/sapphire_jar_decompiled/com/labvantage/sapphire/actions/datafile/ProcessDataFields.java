/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.datafile;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.documents.FieldProcessing;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ProcessDataFields
extends BaseAction
implements FieldProcessing {
    public static final String PROPERTY_FIELDS = "fields";
    public static final String PROPERTY_SUBMITTEDVALUES = "submittedvalues";
    public static final String PROPERTY_PROCESSINGSCRIPT = "processingscript";
    public static final String PROPERTY_PROCESSINGSCRIPTTYPE = "processingscripttype";
    public static final String PROPERTY_DATAFILEDEFINTIONID = "datafiledefinitionid";
    public static final String PROPERTY_VERBOSE = "verbose";
    public static final String PROPERTY_STATUS = "status";
    public static final String PROPERTY_LOG = "log";
    public static final String FIELDTYPE_CELL = "cell";
    public static final String SCRIPTTYPE_GROOVY = "G";
    private HashMap fieldMap;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String fieldProps = properties.getProperty(PROPERTY_FIELDS);
        PropertyList fieldProperties = new PropertyList();
        fieldProperties.setPropertyList(fieldProps);
        String submittedValues = properties.getProperty(PROPERTY_SUBMITTEDVALUES);
        String processingScript = properties.getProperty(PROPERTY_PROCESSINGSCRIPT);
        String processingScriptType = properties.getProperty(PROPERTY_PROCESSINGSCRIPTTYPE);
        boolean verbose = "Y".equals(properties.getProperty(PROPERTY_VERBOSE, "Y"));
        PropertyList blockValues = new PropertyList();
        blockValues.setPropertyList(submittedValues, false, false);
        SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
        PropertyListCollection transactionBlocks = blockValues.getCollectionNotNull("transactiondata");
        StringBuffer log = new StringBuffer();
        this.fieldMap = this.setFieldMap(fieldProperties.getCollectionNotNull(PROPERTY_FIELDS));
        for (int blockNum = 0; blockNum < transactionBlocks.size(); ++blockNum) {
            PropertyList currBlockValues = transactionBlocks.getPropertyList(blockNum);
            HashMap bindings = new HashMap();
            bindings.put(PROPERTY_FIELDS, ProcessingUtil.createFieldMap(sapphireConnection, this, currBlockValues, "enteredtext"));
            HashMap output = new HashMap();
            bindings.put("output", output);
            if (processingScript.length() > 0) {
                if (SCRIPTTYPE_GROOVY.equals(processingScriptType)) {
                    try {
                        StringBuffer groovyEvalLog = new StringBuffer();
                        ProcessingUtil.processScript(sapphireConnection, processingScript, bindings, groovyEvalLog, "TESTPROCESSSCRIPT");
                        log.append("\nProcessed groovy block successfully:");
                        if (verbose) {
                            log.append("\nGroovyEvalLog is: ").append(groovyEvalLog.toString());
                        }
                    }
                    catch (ActionException e) {
                        log.append("\nProcessing Groovy block failed: ").append(e.getMessage());
                        throw new ActionException(log.toString());
                    }
                    catch (Exception e) {
                        log.append("\n" + e.getMessage());
                        throw new ActionException("\nGroovy block failed: " + e.getMessage());
                    }
                    properties.setProperty("responsemessage", (String)output.get("responsemessage"));
                    continue;
                }
                ActionBlock ab = new ActionBlock("DataFileDefinition: " + properties.getProperty(PROPERTY_DATAFILEDEFINTIONID), processingScript);
                ab.setDebugMode(true);
                try {
                    ab.setGroovyBindings(bindings);
                    ab.setBlockProperties((HashMap)bindings.get(PROPERTY_FIELDS));
                    this.getActionProcessor().processActionBlock(ab);
                    HashMap retProps = ab.getReturnProperties();
                    properties.putAll(retProps);
                    continue;
                }
                catch (ActionException e) {
                    log.append("\nProcessing Action Block failed: ").append(e.getMessage());
                    if (verbose) {
                        log.append("\nDebug log from action block:\n");
                        log.append(ab.getDebugLog());
                        log.append("\n-------------------End of debug log------------");
                    } else {
                        Trace.log("\nDebug log from action block:\n");
                        Trace.log(ab.getDebugLog());
                        Trace.log("\n-------------------End of debug log------------");
                    }
                    throw new ActionException(e.getMessage());
                }
            }
            log.append("\nProcessing script not specified");
        }
        properties.setProperty(PROPERTY_STATUS, "SUCCESS");
        properties.setProperty(PROPERTY_LOG, log.toString());
    }

    private HashMap setFieldMap(PropertyListCollection fields) {
        this.fieldMap = new HashMap();
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList processFieldProps = new PropertyList();
            String currFieldId = fields.getPropertyList(i).getProperty("fieldid");
            processFieldProps.setProperty("fieldid", currFieldId);
            String currFieldType = fields.getPropertyList(i).getProperty("type");
            if (!currFieldType.equals(FIELDTYPE_CELL) && !currFieldType.equals("input")) {
                processFieldProps.setProperty("repeatable", "Y");
            } else {
                processFieldProps.setProperty("repeatable", "N");
            }
            this.fieldMap.put(currFieldId, processFieldProps);
        }
        return this.fieldMap;
    }

    @Override
    public PropertyList getField(String fieldId) {
        return (PropertyList)this.fieldMap.get(fieldId);
    }
}

