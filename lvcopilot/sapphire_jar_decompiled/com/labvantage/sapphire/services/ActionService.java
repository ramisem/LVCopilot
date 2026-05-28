/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.LabVantageClassLoader;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.groovy.ArrayUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.groovy.PropertyUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ActionService
extends BaseService
implements CacheNames {
    public static final String LOGNAME = "ActionService";
    private String defaultlockoption = null;

    public ActionService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public void processActionBlock(ActionBlock actionBlock) throws ServiceException {
        super.noLog("Y".equals(actionBlock.getBlockProperty("_nolog")));
        HashMap actionProperties = new HashMap();
        actionBlock.getGroovyBindings().put("user", this.connectionInfo.getUserAttributeMap());
        this.processActionBlock(actionBlock, actionProperties);
    }

    private void processActionBlock(ActionBlock actionBlock, HashMap actionProperties) throws ServiceException {
        block44: {
            this.logInfo("Processing action block");
            actionBlock.startProcessing();
            try {
                boolean executeActionBlock = true;
                String abTest = actionBlock.getTest();
                if (abTest != null && abTest.length() > 0) {
                    actionBlock.log("Evaluating ActionBlock test: " + abTest);
                    boolean bl = executeActionBlock = actionBlock.hasChildCaseActionBlock() || "true".equals(this.evaluateActionGroovy(abTest, actionBlock, actionProperties));
                    if (!executeActionBlock) {
                        actionBlock.log("Action Block will be ignored");
                    }
                }
                if (!executeActionBlock) break block44;
                if (actionBlock.hasChildCaseActionBlock()) {
                    String testResult = this.evaluateActionGroovy(abTest, actionBlock, actionProperties);
                    actionBlock = actionBlock.getChildCaseActionBlock(testResult);
                    actionBlock.log("Evaluated to: " + testResult + ".\nProcessing " + testResult + " case block.");
                }
                int commands = actionBlock.getCommandCount();
                this.logInfo("About to go through " + String.valueOf(commands) + " command(s).");
                for (int i = 0; i < commands; ++i) {
                    String value;
                    Object o;
                    block45: {
                        String value2;
                        o = actionBlock.getCommand(i);
                        if (!(o instanceof ActionBlock.Action)) break block45;
                        ActionBlock.Action action = (ActionBlock.Action)o;
                        String name = action.name;
                        String actionid = action.actionid;
                        String versionid = action.versionid;
                        String actionClass = action.actionClass;
                        String actionTest = action.test;
                        PropertyList properties = action.properties;
                        actionProperties.put(name, properties);
                        boolean isAction = actionid != null && actionid.length() > 0;
                        boolean isActionClass = !isAction && actionClass != null && actionClass.length() > 0;
                        actionBlock.log("");
                        boolean execute = true;
                        if (actionTest != null && actionTest.length() > 0) {
                            actionBlock.log("Evaluating Action " + name + ": " + actionTest + ".");
                            execute = "true".equals(this.evaluateActionGroovy(actionTest, actionBlock, actionProperties));
                            if (!execute) {
                                actionBlock.log("Action will be ignored");
                            }
                        }
                        if (!execute) continue;
                        this.logInfo("Action(" + String.valueOf(i) + ") with the properties: " + properties.toString());
                        if (actionBlock.isDebugMode()) {
                            actionBlock.log("Processing action " + name + " (" + (isAction ? actionid : actionClass) + ")");
                            TreeSet s = new TreeSet(properties.keySet());
                            for (String propertyid : s) {
                                actionBlock.log("  IN:  " + propertyid + "=" + properties.getProperty(propertyid));
                            }
                        }
                        HashMap<String, String> outputproperties = new HashMap<String, String>();
                        DataSet actionProps = null;
                        if (isAction) {
                            actionProps = (DataSet)CacheUtil.get(this.connectionInfo.getDatabaseId(), "ActionPropertyList", actionid.toLowerCase() + ";" + versionid);
                            if (actionProps == null) {
                                DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
                                try {
                                    db.setConnection(this.sapphireConnection);
                                    db.createPreparedResultSet("GetActionProperties", "SELECT propertyid FROM actionproperty WHERE lower( actionid ) = ? AND actionversionid = ? AND lower( propertytypeflag ) = 'o'", new Object[]{actionid.toLowerCase(), versionid});
                                    actionProps = new DataSet(db.getResultSet("GetActionProperties"));
                                    CacheUtil.put(this.connectionInfo.getDatabaseId(), "ActionPropertyList", actionid.toLowerCase() + ";" + versionid, actionProps);
                                    db.closeResultSet("GetActionProperties");
                                }
                                catch (SapphireException se) {
                                    throw new ServiceException("DB_ACTION_FAILED", "Failed to retrieve action properties. " + se.getMessage(), se);
                                }
                                finally {
                                    db.reset();
                                }
                            }
                        } else if (isActionClass) {
                            actionProps = new DataSet();
                        }
                        if (actionProps == null) {
                            if (actionBlock.isDebugMode()) {
                                actionBlock.log("Action details could not be found. Aborting.");
                            }
                            throw new ServiceException("INVALID_PARAMETER", "The action " + actionid + "(v " + versionid + ") has not been found.");
                        }
                        int rows = actionProps.size();
                        for (int row = 0; row < rows; ++row) {
                            value2 = properties.getProperty(actionProps.getString(row, "propertyid"));
                            if (value2.length() <= 0) continue;
                            String[] tokens = StringUtil.getTokens(value2);
                            if (tokens.length == 1) {
                                outputproperties.put(actionProps.getString(row, "propertyid").toLowerCase(), tokens[0]);
                                this.logInfo("saving the reference to: " + actionProps.getString(row, "propertyid").toLowerCase() + "; " + tokens[0]);
                                continue;
                            }
                            outputproperties.put(actionProps.getString(row, "propertyid"), null);
                        }
                        for (String propertyid : properties.keySet()) {
                            String originalvalue;
                            block48: {
                                block46: {
                                    block47: {
                                        if (outputproperties.containsKey(propertyid) || !(properties.get(propertyid) instanceof String) || (value2 = properties.getProperty(propertyid)).indexOf("[") <= -1) continue;
                                        originalvalue = value2;
                                        if (!value2.startsWith("[$G{") || !value2.endsWith("}]")) break block46;
                                        if (value2.indexOf("[$G{", 2) < 0) break block47;
                                        String[] groovyTokens = StringUtil.getTokens(value2, "[$G{", "}]");
                                        if (groovyTokens.length <= 0) break block48;
                                        for (String token : groovyTokens) {
                                            String tokenValue = this.evaluateActionGroovy("$G{" + token + "}", actionBlock, actionProperties);
                                            value2 = StringUtil.replaceAll(value2, "[$G{" + token + "}" + "]", tokenValue);
                                        }
                                        if (!actionBlock.isDebugMode()) break block48;
                                        actionBlock.log("  " + originalvalue + " substituted with '" + value2 + "'");
                                        actionBlock.log("  IN:  " + propertyid + "=" + value2);
                                        break block48;
                                    }
                                    value2 = this.evaluateActionGroovy(value2.substring(1, value2.length() - 1), actionBlock, actionProperties);
                                    break block48;
                                }
                                String[] tokens = StringUtil.getTokens(value2);
                                for (int j = 0; j < tokens.length; ++j) {
                                    String propertyvalue;
                                    Object propertyobject = actionBlock.getBlockProperties().get(tokens[j]);
                                    if (propertyobject == null || (propertyvalue = propertyobject.toString()) == null) continue;
                                    value2 = StringUtil.replaceAll(value2, "[" + tokens[j] + "]", propertyvalue, false);
                                }
                            }
                            properties.setProperty(propertyid, value2);
                            if (!actionBlock.isDebugMode() || originalvalue.equals(value2)) continue;
                            actionBlock.log("  " + originalvalue + " substituted with '" + value2 + "'");
                            actionBlock.log("  IN:  " + propertyid + "=" + value2);
                        }
                        try {
                            PropertyList originalProperties = null;
                            if (actionBlock.isDebugMode()) {
                                originalProperties = properties.copy();
                            }
                            long startTime = System.currentTimeMillis();
                            if (isAction) {
                                this.logInfo("Processing: " + actionid + ";" + versionid);
                                this.processAction(actionid, versionid, properties, actionBlock.getErrorHandler());
                                this.logInfo("Processed: " + actionid + ";" + versionid);
                            } else if (isActionClass) {
                                this.processActionClass(actionClass, actionid, versionid, properties, actionBlock.getErrorHandler());
                            }
                            if (actionBlock.isDebugMode()) {
                                TreeSet s = new TreeSet(properties.keySet());
                                for (String propertyid : s) {
                                    if (propertyid.equals("applylock") || properties.getProperty(propertyid).equals(originalProperties.getProperty(propertyid))) continue;
                                    actionBlock.log("  OUT: " + propertyid + "=" + properties.getProperty(propertyid));
                                }
                            }
                            if (outputproperties.size() > 0) {
                                for (String propertyid : outputproperties.keySet()) {
                                    if (outputproperties.get(propertyid) == null) continue;
                                    String bpid = (String)outputproperties.get(propertyid);
                                    String bpvalue = properties.getProperty(propertyid);
                                    actionBlock.getBlockProperties().put(bpid, bpvalue);
                                    this.logInfo("Setting: " + bpid + " to " + bpvalue);
                                    if (!actionBlock.isDebugMode()) continue;
                                    actionBlock.log("  Adding Block Property: " + bpid + " = " + bpvalue);
                                }
                            }
                            if (actionBlock.isDebugMode()) {
                                actionBlock.log("Processing completed in " + (System.currentTimeMillis() - startTime) + "ms");
                            }
                            actionBlock.log("");
                            continue;
                        }
                        catch (ServiceException e) {
                            actionBlock.log("  ERROR. Action Generated an Error: " + e.getMessage());
                            actionBlock.setErrorAction(i);
                            throw e;
                        }
                    }
                    if (o instanceof ActionBlock) {
                        ActionBlock ab = (ActionBlock)o;
                        this.processActionBlock(ab, actionProperties);
                        continue;
                    }
                    if (o instanceof ActionBlock.BlockProperty) {
                        ActionBlock.BlockProperty bp = (ActionBlock.BlockProperty)o;
                        if (bp.value.startsWith("[$G{") && bp.value.endsWith("}]") || bp.value.startsWith("[") && bp.value.endsWith("]")) {
                            value = this.evaluateActionGroovy(bp.value.substring(1, bp.value.length() - 1), actionBlock, actionProperties);
                            actionBlock.setBlockProperty(bp.propertyid, value);
                            if (!actionBlock.isDebugMode()) continue;
                            actionBlock.log("Adding Block Property: " + bp.propertyid + " = " + bp.value + " = " + value);
                            continue;
                        }
                        actionBlock.setBlockProperty(bp.propertyid, bp.value);
                        if (!actionBlock.isDebugMode()) continue;
                        actionBlock.log("Adding Block Property: " + bp.propertyid + " = " + bp.value);
                        continue;
                    }
                    if (!(o instanceof ActionBlock.ReturnProperty)) continue;
                    ActionBlock.ReturnProperty rp = (ActionBlock.ReturnProperty)o;
                    if (rp.value.startsWith("[$G{") && rp.value.endsWith("}]") || rp.value.startsWith("[") && rp.value.endsWith("]")) {
                        value = this.evaluateActionGroovy(rp.value.substring(1, rp.value.length() - 1), actionBlock, actionProperties);
                        actionBlock.setReturnProperty(rp.propertyid, value);
                        if (!actionBlock.isDebugMode()) continue;
                        actionBlock.log("Adding Return Property: " + rp.propertyid + " = " + rp.value + " = " + value);
                        continue;
                    }
                    actionBlock.setReturnProperty(rp.propertyid, rp.value);
                    if (!actionBlock.isDebugMode()) continue;
                    actionBlock.log("Adding Return Property: " + rp.propertyid + " = " + rp.value);
                }
            }
            catch (Exception e) {
                throw new ServiceException("ACTION_BLOCK_FAILED", "Failed to access action block properties", e);
            }
            finally {
                actionBlock.endProcessing();
            }
        }
    }

    private String evaluateActionGroovy(String groovyExpression, ActionBlock actionBlock, HashMap actionProperties) throws ServiceException {
        HashMap<String, Object> bindingMap = new HashMap<String, Object>();
        bindingMap.put("block", actionBlock.getBlockProperties());
        bindingMap.putAll(actionProperties);
        HashMap groovyBindings = actionBlock.getGroovyBindings();
        for (String name : groovyBindings.keySet()) {
            bindingMap.put(name, groovyBindings.get(name));
        }
        bindingMap.put("propertyutil", new PropertyUtil());
        bindingMap.put("arrayutil", new ArrayUtil());
        bindingMap.put("m18n", new M18NUtil(this.connectionInfo));
        bindingMap.put("user", this.connectionInfo.getUserAttributeMap());
        String result = null;
        try {
            result = GroovyUtil.getInstance(this.sapphireConnection).evaluateSecure(groovyExpression, bindingMap);
        }
        catch (SapphireException e) {
            actionBlock.log("ERROR: " + e.getMessage());
            throw new ServiceException("Unable to parse Groovy expression " + groovyExpression, e);
        }
        return result;
    }

    public void processAction(String actionid, String versionid, PropertyList properties) throws ServiceException {
        ErrorHandler errorHandler = new ErrorHandler();
        this.processAction(actionid, versionid, properties, errorHandler);
    }

    public void processAction(String actionid, String versionid, PropertyList properties, ErrorHandler errorHandler) throws ServiceException {
        if (actionid == null || actionid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Actionid not specified.");
        }
        if (versionid == null || versionid.length() == 0) {
            versionid = "1";
        }
        if (properties.getProperty("applylock").length() == 0) {
            if (this.defaultlockoption == null) {
                try {
                    ConfigService config = new ConfigService(this.sapphireConnection);
                    this.defaultlockoption = config.getProfileProperty(this.connectionInfo.getSysuserId(), "lockactions");
                }
                catch (ServiceException e) {
                    this.logError("Failed to get profile property 'lockactions' - using default of N. Exception:" + e.getMessage());
                }
            }
            properties.setProperty("applylock", this.defaultlockoption);
        }
        DataSet actionDefDs = this.getActionDefinition(actionid, versionid);
        String actionlanguage = actionDefDs.getString(0, "actionlanguage");
        String componentRef = actionDefDs.getValue(0, "componentreference");
        if (componentRef != null && componentRef.length() > 0) {
            actionlanguage = "component";
        }
        if (actionlanguage == null || actionlanguage.length() == 0) {
            actionlanguage = "vbscript";
        }
        try {
            if (actionlanguage.equalsIgnoreCase("java")) {
                ArrayList actionAttsDs = null;
                ClassLoader classLoader = null;
                if (actionAttsDs != null && actionAttsDs.size() > 0) {
                    try {
                        classLoader = LabVantageClassLoader.getClassLoader(LabVantageClassLoader.ClassLoaderType.ACTION, actionid, null, (DataSet)actionAttsDs, null, null, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                    }
                    catch (Exception e) {
                        this.logError("Failed to load attachments as libraries", e);
                    }
                }
                this.processActionClass(actionDefDs.getString(0, "objectname"), actionid, versionid, properties, errorHandler, classLoader != null ? classLoader : this.getClass().getClassLoader());
            } else if (actionlanguage.equalsIgnoreCase("actionblock")) {
                String actionblockxml = actionDefDs.getString(0, "actionscript");
                ActionBlock ab = new ActionBlock();
                try {
                    ab.setXML(actionblockxml);
                }
                catch (SapphireException se) {
                    throw new ServiceException("Unable to process Action XML " + actionblockxml, se);
                }
                ab.setGroovyBindings(properties);
                ab.setBlockProperties(properties);
                this.processActionBlock(ab);
                properties.putAll(ab.getReturnProperties());
            } else {
                if (actionlanguage.equalsIgnoreCase("vbscript")) {
                    throw new ServiceException("VBScript not supported on this platform");
                }
                throw new ServiceException("PowerBuilder and other component actions are not supported");
            }
            properties.setProperty("(return)", String.valueOf(1));
        }
        catch (ServiceException e) {
            properties.setProperty("(return)", String.valueOf(2));
            throw e;
        }
    }

    private DataSet getActionAttachments(String actionid, String versionid) throws ServiceException {
        DataSet actionAttsDs = (DataSet)CacheUtil.get(this.connectionInfo.getDatabaseId(), "ActionAttachments", actionid.toLowerCase() + ";" + versionid);
        if (actionAttsDs == null) {
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                db.createPreparedResultSet("GetActionAtts", "SELECT sdcid, keyid1, keyid2, attachmentnum, attachmentclass FROM sdiattachment WHERE lower( keyid1 ) = ? AND keyid2 = ?", new Object[]{actionid.toLowerCase(), versionid});
                actionAttsDs = new DataSet();
                actionAttsDs.setResultSet(db.getResultSet("GetActionAtts"), true, this.sapphireConnection.getDbms());
                CacheUtil.put(this.connectionInfo.getDatabaseId(), "ActionAttachments", actionid.toLowerCase() + ";" + versionid, actionAttsDs);
                db.closeResultSet("GetActionAtts");
            }
            catch (SapphireException se) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to get action attachments. " + se.getMessage(), se);
            }
            finally {
                db.reset();
            }
        }
        return actionAttsDs;
    }

    private DataSet getActionDefinition(String actionid, String versionid) throws ServiceException {
        DataSet actionDefDs = (DataSet)CacheUtil.get(this.connectionInfo.getDatabaseId(), "ActionDefinition", actionid.toLowerCase() + ";" + versionid);
        if (actionDefDs == null) {
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                db.createPreparedResultSet("GetActionDef", "SELECT componentreference, objectname, actionlanguage, actiontype, spmflag, parsingfunctionflag, actionscript, returnpropertyid FROM action WHERE lower( actionid ) = ? AND actionversionid = ?", new Object[]{actionid.toLowerCase(), versionid});
                actionDefDs = new DataSet();
                actionDefDs.setResultSet(db.getResultSet("GetActionDef"), true, this.sapphireConnection.getDbms());
                CacheUtil.put(this.connectionInfo.getDatabaseId(), "ActionDefinition", actionid.toLowerCase() + ";" + versionid, actionDefDs);
                db.closeResultSet("GetActionDef");
                if ("vbscript".equalsIgnoreCase(actionDefDs.getString(0, "actionlanguage"))) {
                    db.createPreparedResultSet("GetActionDef", "SELECT actiontype, spmflag, parsingfunctionflag, actionscript FROM action WHERE lower( actionid ) = ? AND actionversionid = ?", new Object[]{actionid.toLowerCase(), versionid});
                    if (db.getNext("GetActionDef")) {
                        if (!db.getString("GetActionDef", "actiontype").equals("U")) {
                            throw new ServiceException("UNRECOGNIZED_ACTION", "The action " + actionid + " (v " + versionid + ") is not a user script");
                        }
                        actionDefDs.setString(0, "actionscript", db.getClob("GetActionDef", "actionscript"));
                    }
                }
            }
            catch (SapphireException se) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to get action details. " + se.getMessage(), se);
            }
            finally {
                db.reset();
            }
        }
        if (actionDefDs.size() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "The action " + actionid + "(v " + versionid + ") has not been found.");
        }
        return actionDefDs;
    }

    public void processActionClass(String className, PropertyList properties, ErrorHandler errorHandler) throws ServiceException {
        this.processActionClass(className, null, null, properties, errorHandler);
    }

    public void processActionClass(String className, String actionid, String versionid, PropertyList properties, ErrorHandler errorHandler) throws ServiceException {
        this.processActionClass(className, actionid, versionid, properties, errorHandler, this.getClass().getClassLoader());
    }

    private void processActionClass(String className, String actionid, String versionid, PropertyList properties, ErrorHandler errorHandler, ClassLoader classLoader) throws ServiceException {
        int rc;
        String label;
        BaseAction action;
        if (className == null || className.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Class name not specified.");
        }
        if (actionid == null || actionid.length() == 0) {
            actionid = "Unspecified";
        }
        if (versionid == null || versionid.length() == 0) {
            versionid = "1";
        }
        try {
            this.logInfo("Creating class: " + className);
            Class<?> c = classLoader.loadClass(className);
            action = (BaseAction)c.newInstance();
            action.startAction(actionid, this.sapphireConnection, errorHandler, this.nolog);
        }
        catch (ClassNotFoundException e) {
            throw new ServiceException("The api action class '" + className + "' could not be created. Exception: " + e.getMessage());
        }
        catch (InstantiationException e) {
            throw new ServiceException("Failed to create an instance of the api action class '" + className + "' Exception: " + e.getMessage());
        }
        catch (IllegalAccessException e) {
            throw new ServiceException("Failed to access an instance of the api action class '" + className + "' Exception: " + e.getMessage());
        }
        catch (SapphireException e) {
            throw new ServiceException("Failed to start api action '" + actionid + "' Exception: " + e.getMessage());
        }
        String string = label = className.contains(".") ? className.substring(className.lastIndexOf(".") + 1) : className;
        if (!label.equals(actionid) && !actionid.equals("Unspecified")) {
            label = label + " (" + actionid + ")";
        }
        this.logger.info(LOGNAME, "++++++++++ Processing Action " + label + " for [" + this.getConnectionId() + "] ++++++++++");
        this.logger.info(LOGNAME, "[#" + Thread.currentThread().getId() + "] = " + Thread.currentThread().getName());
        boolean hasProps = false;
        if (this.logging()) {
            for (String propertyid : properties.keySet()) {
                String value;
                if (propertyid == null || propertyid.toLowerCase().contains("password") || (value = properties.getProperty(propertyid)).length() <= 0) continue;
                String v = value;
                if (v.length() > 2000 && !Trace.isDebugEnabled() && !propertyid.contains("keyid")) {
                    v = v.substring(0, 2000) + "...";
                }
                if (v.length() > 50000) {
                    v = v.substring(0, 50000) + "...";
                }
                this.logger.info(LOGNAME, "++ " + propertyid + "=" + v);
                hasProps = true;
            }
            if (hasProps) {
                this.logger.info(LOGNAME, "+++++++++++++++++++++++++++++++++++++++++++++");
            }
        }
        String statsExtraKey = "";
        if (Trace.stats) {
            statsExtraKey = properties.getProperty("sdcid") + (properties.getProperty("linkid").length() > 0 ? " - " + properties.getProperty("linkid") : "");
            Trace.setStartAction(actionid.equals("Unspecified") ? className : actionid, statsExtraKey, properties);
        }
        try {
            if (properties.getProperty("sdcid") != null && properties.getProperty("sdcid").length() > 0) {
                PropertyListCollection links;
                DDTService ddt = new DDTService(this.sapphireConnection);
                PropertyList sdc = null;
                try {
                    sdc = ddt.getSDCProperties(properties.getProperty("sdcid"));
                }
                catch (Exception e) {
                    this.logger.info("Ignore Invalid SDCId:" + properties.getProperty("sdcid"));
                }
                this.copyOldkeycolumnValue(sdc, properties);
                String linkid = properties.getProperty("linkid");
                if (sdc != null && linkid.length() > 0 && (links = sdc.getCollection("links")) != null) {
                    String linksdcid;
                    PropertyList link = null;
                    boolean foundLink = false;
                    for (int i = 0; i < links.size(); ++i) {
                        link = links.getPropertyList(i);
                        if (!linkid.equals(link.getProperty("linkid"))) continue;
                        foundLink = true;
                        break;
                    }
                    if (foundLink && (linksdcid = link.getProperty("linksdcid")).length() > 0) {
                        try {
                            sdc = ddt.getSDCProperties(linksdcid);
                            this.copyOldkeycolumnValue(sdc, properties);
                        }
                        catch (Exception e) {
                            this.logger.info("Ignore Invalid Link SDCId:" + linksdcid);
                        }
                    }
                }
            }
            rc = action.processAction(actionid, versionid, properties);
        }
        catch (RuntimeException e) {
            try {
                throw new ServiceException("Failed to process action class '" + className + ". Exception: " + e, e);
            }
            catch (Throwable throwable) {
                action.endAction();
                this.logger.info(LOGNAME, "[#" + Thread.currentThread().getId() + "] = " + Thread.currentThread().getName());
                this.logger.info(LOGNAME, "++++++++++ Completed Action " + label + " ++++++++++");
                if (Trace.stats) {
                    Trace.setEndAction(actionid.equals("Unspecified") ? className : actionid, statsExtraKey);
                }
                throw throwable;
            }
        }
        action.endAction();
        this.logger.info(LOGNAME, "[#" + Thread.currentThread().getId() + "] = " + Thread.currentThread().getName());
        this.logger.info(LOGNAME, "++++++++++ Completed Action " + label + " ++++++++++");
        if (Trace.stats) {
            Trace.setEndAction(actionid.equals("Unspecified") ? className : actionid, statsExtraKey);
        }
        if (rc == 2) {
            if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                throw new ServiceException(errorHandler.getEncodedString());
            }
            throw new ServiceException("Failed to process action '" + actionid + "'.");
        }
        if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
            throw new ServiceException(errorHandler.getEncodedString());
        }
    }

    private void copyOldkeycolumnValue(PropertyList sdc, PropertyList properties) {
        String keymap1;
        if (sdc != null && sdc.getProperty("tableidmap").length() > 0 && properties.getProperty(keymap1 = sdc.getProperty("keymap1")) != null && properties.getProperty(keymap1).length() > 0) {
            properties.setProperty(sdc.getProperty("keycolid1"), properties.getProperty(keymap1));
            this.logger.info("^^Copied property value from " + keymap1 + " to " + sdc.getProperty("keycolid1"));
        }
    }
}

