/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.binary.Base64
 */
package sapphire.util;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.xml.ActionBlockHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.error.ErrorHandler;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ActionBlock
implements Serializable {
    private boolean debugMode = false;
    private ArrayList commands = new ArrayList();
    private String name = "";
    private String asyncDueDt = "";
    private PropertyList blockproperties = new PropertyList();
    private PropertyList returnproperties = new PropertyList();
    private HashMap groovyBindings = new HashMap();
    private String test = "";
    private String testName = "";
    private String caseValue = "";
    private int errorAction = -1;
    private String errorActionName = "";
    private ErrorHandler errorHandler = new ErrorHandler();
    private ActionBlock topActionBlock = this;
    private HashMap<String, ActionBlock> caseChildActionBlockMap = new HashMap();
    private boolean hasChildCaseActionBlock = false;
    private StringBuffer debugLog = new StringBuffer();
    private String todolistid = "";
    public static final String COMMAND_ACTION = "action";
    public static final String COMMAND_ACTIONBLOCK = "actionblock";
    public static final String COMMAND_UNKNOWN = "unknown";

    public ActionBlock() {
    }

    public ActionBlock(String name, String xml) throws SapphireException {
        this.setName(name);
        this.setXML(xml);
    }

    public ActionBlock(String xml) throws SapphireException {
        this.setXML(xml);
    }

    public ActionBlock(JSONObject jsonObject) throws SapphireException {
        this.setJSONObject(jsonObject);
    }

    public void setXML(String xml) throws SapphireException {
        try {
            ActionBlockHandler handler = new ActionBlockHandler(this);
            handler.setPrintStream(null);
            handler.setXMLString(xml);
            SaxUtil.parseString(handler);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new SapphireException("Unable to parse the XML", e);
        }
    }

    public void setJSONObject(JSONObject jsonObject) throws SapphireException {
        try {
            JSONArray blockArray = jsonObject.getJSONArray(COMMAND_ACTIONBLOCK);
            for (int i = 0; i < blockArray.length(); ++i) {
                JSONObject blockObj = blockArray.getJSONObject(i);
                if (blockObj.has("test")) {
                    JSONObject testObject = blockObj.getJSONObject("test");
                    if (testObject.has("name")) {
                        this.setTestName(testObject.getString("name"));
                    }
                    this.setTest(testObject.getString("value"));
                    continue;
                }
                if (blockObj.has(COMMAND_ACTION)) {
                    JSONObject action = blockObj.getJSONObject(COMMAND_ACTION);
                    String actionName = action.getString("name");
                    this.setAction(actionName, action.getString("id"), "1");
                    JSONObject properties = action.getJSONObject("property");
                    Iterator keyItr = properties.keys();
                    while (keyItr.hasNext()) {
                        String key = (String)keyItr.next();
                        this.setActionProperty(actionName, key, properties.getString(key));
                    }
                    continue;
                }
                if (blockObj.has(COMMAND_ACTIONBLOCK)) {
                    ActionBlock childAb = new ActionBlock();
                    childAb.setJSONObject(blockObj);
                    JSONArray blockContentArray = blockObj.getJSONArray(COMMAND_ACTIONBLOCK);
                    if (blockContentArray != null && blockContentArray.length() > 0 && blockContentArray.getJSONObject(0).has("test")) {
                        for (int j = 0; j < blockContentArray.length(); ++j) {
                            JSONObject contentObj = blockContentArray.getJSONObject(j);
                            if (contentObj.has("test")) {
                                JSONObject testObject = contentObj.getJSONObject("test");
                                if (testObject.has("name")) {
                                    childAb.setTestName(testObject.getString("name"));
                                }
                                childAb.setTest(testObject.getString("value"));
                                continue;
                            }
                            if (contentObj.has("true")) {
                                ActionBlock trueChildAb = new ActionBlock();
                                JSONObject trueJSONAb = contentObj.getJSONObject("true");
                                trueChildAb.setJSONObject(trueJSONAb);
                                trueChildAb.setCaseValue("true");
                                childAb.addChildCaseActionBlock("true", trueChildAb);
                                childAb.addActionBlockCommand(trueChildAb);
                                continue;
                            }
                            if (!contentObj.has("false")) continue;
                            ActionBlock falsechildAb = new ActionBlock();
                            falsechildAb.setJSONObject(contentObj.getJSONObject("false"));
                            falsechildAb.setCaseValue("false");
                            childAb.addChildCaseActionBlock("false", falsechildAb);
                            childAb.addActionBlockCommand(falsechildAb);
                        }
                    }
                    this.addActionBlockCommand(childAb);
                    continue;
                }
                if (blockObj.has("blockproperty")) {
                    JSONObject blockpropObject = blockObj.getJSONObject("blockproperty");
                    this.addBlockPropertyCommand(blockpropObject.getString("id"), blockpropObject.getString("value"));
                    continue;
                }
                if (!blockObj.has("returnproperty")) continue;
                JSONObject returnpropObject = blockObj.getJSONObject("returnproperty");
                this.addReturnPropertyCommand(returnpropObject.getString("id"), returnpropObject.getString("value"));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new SapphireException("Unable to parse the JSON", e);
        }
    }

    public String getName() {
        return this.name != null ? this.name : "";
    }

    public String getTestName() {
        return this.testName != null ? this.testName : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaseValue() {
        return this.caseValue;
    }

    public void setCaseValue(String caseValue) {
        this.caseValue = caseValue;
    }

    public String getAsyncDueDt() {
        return this.asyncDueDt;
    }

    public void setAsyncDueDt(String asyncDueDt) {
        this.asyncDueDt = asyncDueDt;
    }

    public String toString() {
        return this.commands.toString();
    }

    public String toXML() {
        return this.toXML(0);
    }

    public String toJSONString() {
        try {
            return this.toJSONObject().toString(4);
        }
        catch (JSONException e) {
            return null;
        }
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        JSONArray abArray = new JSONArray();
        try {
            if (this.test != null && this.test.length() > 0) {
                JSONObject testObject = new JSONObject();
                JSONObject testPropObject = new JSONObject();
                testPropObject.put("name", this.testName);
                testPropObject.put("value", this.test);
                testObject.put("test", testPropObject);
                abArray.put(testObject);
            }
            if (this.caseValue != null && this.caseValue.length() > 0) {
                JSONObject childCaseAb = new JSONObject();
                childCaseAb.put(COMMAND_ACTIONBLOCK, abArray);
                jsonObject.put(this.caseValue, childCaseAb);
            } else {
                jsonObject.put(COMMAND_ACTIONBLOCK, abArray);
            }
            for (Object command : this.commands) {
                if (command instanceof Action) {
                    abArray.put(((Action)command).toJSONObject());
                    continue;
                }
                if (command instanceof ActionBlock) {
                    ActionBlock childAB = (ActionBlock)command;
                    abArray.put(childAB.toJSONObject());
                    continue;
                }
                if (command instanceof BlockProperty) {
                    abArray.put(((BlockProperty)command).toJSONObject());
                    continue;
                }
                if (!(command instanceof ReturnProperty)) continue;
                abArray.put(((ReturnProperty)command).toJSONObject());
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    private String toXML(int indent) {
        String pad = StringUtil.repeat("  ", indent);
        StringBuffer out = new StringBuffer();
        out.append(pad + "<actionblock" + (this.name != null && this.name.length() > 0 ? " name=\"" + this.name + "\"" : "") + (this.caseValue != null && this.caseValue.length() > 0 ? " case=\"" + this.caseValue + "\"" : "") + ">\n");
        if (this.test != null && this.test.length() > 0) {
            out.append(pad + "  <test><![CDATA[" + this.test + "]]></test>");
        }
        TreeSet bp = new TreeSet(this.blockproperties.keySet());
        for (String propertyid : bp) {
            String value = this.blockproperties.getProperty(propertyid);
            out.append(pad + "  <blockproperty id=\"" + propertyid + "\"><![CDATA[" + value + "]]></blockproperty>\n");
        }
        if (this.groovyBindings.size() > 0) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(this.groovyBindings);
                oos.close();
                String groovyData = Base64.encodeBase64String((byte[])baos.toByteArray());
                out.append(pad + "  <groovybindings><![CDATA[" + groovyData + "]]></groovybindings>\n");
            }
            catch (Exception e) {
                Trace.logError("Failed to serialize Groovy bindings. Reason: " + e.getMessage(), e);
            }
        }
        for (Object command : this.commands) {
            if (command instanceof Action) {
                out.append(((Action)command).toXML(indent));
                continue;
            }
            if (command instanceof ActionBlock) {
                out.append(((ActionBlock)command).toXML(indent + 1));
                continue;
            }
            if (command instanceof BlockProperty) {
                out.append(((BlockProperty)command).toXML(indent));
                continue;
            }
            if (!(command instanceof ReturnProperty)) continue;
            out.append(((ReturnProperty)command).toXML(indent));
        }
        out.append(pad + "</actionblock>\n");
        return out.toString();
    }

    public void setTest(String test) {
        this.test = test;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setGroovyBindings(HashMap bindings) {
        this.topActionBlock.groovyBindings = bindings;
    }

    public HashMap getGroovyBindings() {
        return this.topActionBlock.groovyBindings;
    }

    public void setBlockProperties(HashMap properties) {
        this.topActionBlock.blockproperties = new PropertyList(properties);
    }

    public void setBlockProperties(PropertyList properties) {
        this.topActionBlock.blockproperties = properties;
    }

    public void setBlockProperty(String propertyid, String value) {
        this.topActionBlock.blockproperties.setProperty(propertyid, value);
    }

    public String getBlockProperty(String propertyid) {
        return this.topActionBlock.blockproperties.getProperty(propertyid);
    }

    public HashMap getBlockProperties() {
        return this.topActionBlock.blockproperties;
    }

    public void setReturnProperties(HashMap properties) {
        this.topActionBlock.returnproperties = new PropertyList(properties);
    }

    public void setReturnProperties(PropertyList properties) {
        this.topActionBlock.returnproperties = properties;
    }

    public void setReturnProperty(String propertyid, String value) {
        this.topActionBlock.returnproperties.setProperty(propertyid, value);
    }

    public HashMap getReturnProperties() {
        return this.topActionBlock.returnproperties;
    }

    public String getReturnProperty(String propertyid) {
        return this.topActionBlock.returnproperties.getProperty(propertyid);
    }

    public void setAction(String name, String actionid, String versionid) throws ActionException {
        this.setAction(name, "", "", actionid, versionid, new PropertyList());
    }

    public void setAction(String name, String actionid, String versionid, HashMap properties) throws ActionException {
        this.setAction(name, "", "", actionid, versionid, properties);
    }

    public void setAction(String name, String actionid, String versionid, PropertyList properties) throws ActionException {
        this.setAction(name, "", "", actionid, versionid, properties);
    }

    public void setActionClass(String name, String actionClass) throws ActionException {
        this.setAction(name, "", actionClass, "", "", new PropertyList());
    }

    public void setActionClass(String name, String actionClass, HashMap properties) throws ActionException {
        this.setAction(name, "", actionClass, "", "", properties);
    }

    public void setActionClass(String name, String actionClass, PropertyList properties) throws ActionException {
        this.setAction(name, "", actionClass, "", "", properties);
    }

    public void setActionProperty(String name, String propertyid, String value) throws ActionException {
        Action action = this.getAction(name);
        if (action == null) {
            throw new ActionException("The action '" + name + "' does not exist.");
        }
        action.properties.setProperty(propertyid.toLowerCase(), value);
    }

    public String getActionProperty(String name, String propertyid) throws ActionException {
        Action action = this.getAction(name);
        if (action == null) {
            throw new ActionException("The action '" + name + "' does not exist.");
        }
        return action.properties.getProperty(propertyid);
    }

    public String getActionProperty(int actionindex, String propertyid) throws ActionException {
        String name = this.getActionName(actionindex);
        return this.getActionProperty(name, propertyid);
    }

    public void setActionProperties(String name, HashMap properties) throws ActionException {
        this.setActionProperties(name, new PropertyList(properties));
    }

    public void setActionProperties(String name, PropertyList properties) throws ActionException {
        Action action = this.getAction(name);
        if (action == null) {
            throw new ActionException("The action '" + name + "' does not exist.");
        }
        action.properties.putAll(properties);
    }

    public void setActionProperties(int actionindex, HashMap properties) throws ActionException {
        this.setActionProperties(this.getActionName(actionindex), new PropertyList(properties));
    }

    public void setActionProperties(int actionindex, PropertyList properties) throws ActionException {
        this.setActionProperties(this.getActionName(actionindex), properties);
    }

    public HashMap getActionProperties(String name) throws ActionException {
        Action action = this.getAction(name);
        if (action == null) {
            throw new ActionException("The action '" + name + "' does not exist.");
        }
        return action.properties;
    }

    public HashMap getActionProperties(int actionindex) throws ActionException {
        return this.getActionProperties(this.getActionName(actionindex));
    }

    public int getActionCount() {
        return this.commands.size();
    }

    public ActionBlock getChildCaseActionBlock(String caseValue) {
        return this.caseChildActionBlockMap.get(caseValue);
    }

    public boolean hasChildCaseActionBlock() {
        return this.hasChildCaseActionBlock;
    }

    public List<String> getDistinctActions() {
        ArrayList<String> actions = new ArrayList<String>();
        for (int i = 0; i < this.commands.size(); ++i) {
            Object o = this.commands.get(i);
            if (o instanceof Action && ((Action)o).actionid != null && ((Action)o).actionid.length() > 0) {
                if (actions.contains(((Action)o).actionid)) continue;
                actions.add(((Action)o).actionid);
                continue;
            }
            if (!(o instanceof ActionBlock)) continue;
            actions.addAll(((ActionBlock)o).getDistinctActions());
        }
        return actions;
    }

    public List<String> getDistinctActionClasses() {
        ArrayList<String> actionClasses = new ArrayList<String>();
        for (int i = 0; i < this.commands.size(); ++i) {
            Object o = this.commands.get(i);
            if (o instanceof Action && ((Action)o).actionClass != null && ((Action)o).actionClass.length() > 0) {
                if (actionClasses.contains(((Action)o).actionClass)) continue;
                actionClasses.add(((Action)o).actionClass);
                continue;
            }
            if (!(o instanceof ActionBlock)) continue;
            actionClasses.addAll(((ActionBlock)o).getDistinctActionClasses());
        }
        return actionClasses;
    }

    public String getActionName(int actionindex) throws ActionException {
        if (actionindex >= 0 && actionindex < this.commands.size() && this.commands.get(actionindex) instanceof Action) {
            Action action = (Action)this.commands.get(actionindex);
            return action.name;
        }
        throw new ActionException("The action index is invalid: " + String.valueOf(actionindex));
    }

    public String getActionid(String name) throws ActionException {
        return this.getActionid(this.getActionIndex(name));
    }

    public String getActionid(int actionindex) throws ActionException {
        if (actionindex >= 0 && actionindex < this.commands.size() && this.commands.get(actionindex) instanceof Action) {
            Action action = (Action)this.commands.get(actionindex);
            return action.actionid;
        }
        throw new ActionException("The action index is invalid: " + String.valueOf(actionindex));
    }

    public String getActionClass(String name) throws ActionException {
        return this.getActionClass(this.getActionIndex(name));
    }

    public String getActionClass(int actionindex) throws ActionException {
        if (actionindex >= 0 && actionindex < this.commands.size() && this.commands.get(actionindex) instanceof Action) {
            Action action = (Action)this.commands.get(actionindex);
            return action.actionClass;
        }
        throw new ActionException("The action index is invalid: " + String.valueOf(actionindex));
    }

    public String getVersionid(String name) throws ActionException {
        return this.getVersionid(this.getActionIndex(name));
    }

    public String getVersionid(int actionindex) throws ActionException {
        if (actionindex >= 0 && actionindex < this.commands.size() && this.commands.get(actionindex) instanceof Action) {
            Action action = (Action)this.commands.get(actionindex);
            return action.versionid;
        }
        throw new ActionException("The action index is invalid: " + String.valueOf(actionindex));
    }

    public String getTest(String name) throws ActionException {
        return this.getActionTest(this.getActionIndex(name));
    }

    public String getActionTest(int actionindex) throws ActionException {
        if (actionindex >= 0 && actionindex < this.commands.size() && this.commands.get(actionindex) instanceof Action) {
            Action action = (Action)this.commands.get(actionindex);
            return action.test;
        }
        throw new ActionException("The action index is invalid: " + String.valueOf(actionindex));
    }

    public void setActionBlockProperty(String name, String blockpropertyid, String actionpropertyid) throws ActionException {
        this.setActionProperty(name, actionpropertyid, "[" + blockpropertyid + "]");
    }

    public int getErrorAction() {
        return this.topActionBlock.errorAction;
    }

    public void setErrorAction(int errorAction) {
        if (this.topActionBlock.errorAction == -1) {
            this.topActionBlock.errorAction = errorAction;
            try {
                String errorActionName = this.getActionid(errorAction);
                if ((errorActionName == null || errorActionName.length() == 0) && (errorActionName = this.getActionClass(errorAction)) != null && errorActionName.length() > 0) {
                    errorActionName = errorActionName.substring(errorActionName.lastIndexOf(".") + 1);
                }
                this.topActionBlock.errorActionName = errorActionName;
            }
            catch (ActionException actionException) {
                // empty catch block
            }
        }
    }

    public void setDebugMode(boolean debugMode) {
        this.topActionBlock.debugMode = debugMode;
    }

    public boolean isDebugMode() {
        return this.topActionBlock.debugMode;
    }

    public String getDebugLog() {
        return this.topActionBlock.debugLog.toString();
    }

    public String getErrorActionName() {
        return this.topActionBlock.errorActionName;
    }

    public ErrorHandler getErrorHandler() {
        return this.topActionBlock.errorHandler;
    }

    public Action getAction(String name) throws ActionException {
        for (int i = 0; i < this.commands.size(); ++i) {
            ActionBlock ab;
            Action action;
            Object o = this.commands.get(i);
            if (o instanceof Action) {
                Action action2 = (Action)o;
                if (!action2.name.equals(name)) continue;
                return action2;
            }
            if (!(o instanceof ActionBlock) || (action = (ab = (ActionBlock)o).getAction(name)) == null) continue;
            return action;
        }
        return null;
    }

    public String getTodolistid() {
        return this.todolistid;
    }

    public void setTodolistid(String todolistid) {
        this.todolistid = todolistid;
    }

    public void synchronizeProperties(ActionBlock returnactionblock) throws ActionException {
        for (int i = 0; i < this.commands.size(); ++i) {
            Object o = this.commands.get(i);
            if (o instanceof Action) {
                Action a1 = (Action)o;
                Action a2 = (Action)returnactionblock.commands.get(i);
                a1.properties.putAll(a2.properties);
                this.setActionProperties(i, returnactionblock.getActionProperties(i));
                if (a1.hmProperties == null || a1.properties == null) continue;
                a1.hmProperties.putAll(a1.properties);
                continue;
            }
            if (!(o instanceof ActionBlock)) continue;
            ActionBlock ab1 = (ActionBlock)o;
            ActionBlock ab2 = (ActionBlock)returnactionblock.commands.get(i);
            if (ab1 == null || ab2 == null) continue;
            ab1.synchronizeProperties(ab2);
        }
    }

    public void setAction(String name, String test, String actionClass, String actionid, String versionid, PropertyList properties) throws ActionException {
        this.setAction(name, test, actionClass, actionid, versionid, properties, null);
    }

    public void setAction(String name, String test, String actionClass, String actionid, String versionid, HashMap hmProperties) throws ActionException {
        this.setAction(name, test, actionClass, actionid, versionid, null, hmProperties);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void setAction(String name, String test, String actionClass, String actionid, String versionid, PropertyList properties, HashMap hmProperties) throws ActionException {
        Action newaction;
        Action action = this.topActionBlock.getAction(name);
        if (action != null) {
            throw new ActionException("The action name '" + name + "' already exists.");
        }
        if (hmProperties == null) {
            if (actionClass != null && actionClass.length() > 0) {
                newaction = new Action(test, actionClass, properties);
            } else {
                if (actionid == null || actionid.length() <= 0 || versionid == null || versionid.length() <= 0) throw new ActionException("Invalid actionid and/or versionid.");
                newaction = new Action(test, actionid, versionid, properties);
            }
        } else if (actionClass != null && actionClass.length() > 0) {
            newaction = new Action(test, actionClass, hmProperties);
        } else {
            if (actionid == null || actionid.length() <= 0 || versionid == null || versionid.length() <= 0) throw new ActionException("Invalid actionid and/or versionid.");
            newaction = new Action(test, actionid, versionid, hmProperties);
        }
        newaction.name = name;
        this.commands.add(newaction);
    }

    public void setActionLabel(String actionName, String actionlabel) throws ActionException {
        Action action = this.getAction(actionName);
        if (action == null) {
            throw new ActionException("The action '" + this.name + "' does not exist.");
        }
        action.actionlabel = actionlabel;
    }

    public void addActionBlockCommand(ActionBlock actionBlock) throws ActionException {
        if (actionBlock.topActionBlock != this.topActionBlock) {
            this.checkUniqueActionNames(actionBlock);
            this.topActionBlock.blockproperties.putAll(actionBlock.blockproperties);
            actionBlock.topActionBlock = this.topActionBlock;
            actionBlock.blockproperties = new PropertyList();
        }
        this.commands.add(actionBlock);
    }

    private void checkUniqueActionNames(ActionBlock ab) throws ActionException {
        for (int i = 0; i < ab.commands.size(); ++i) {
            Object o = ab.commands.get(i);
            if (o instanceof Action) {
                String name = ((Action)o).name;
                if (this.getAction(name) == null) continue;
                throw new ActionException("The action name '" + name + "' already exists.");
            }
            if (!(o instanceof ActionBlock)) continue;
            this.checkUniqueActionNames((ActionBlock)o);
        }
    }

    public void addChildCaseActionBlock(String caseValue, ActionBlock childActionBlock) {
        this.caseChildActionBlockMap.put(caseValue, childActionBlock);
        this.hasChildCaseActionBlock = true;
    }

    public void addBlockPropertyCommand(String propertyid, String value) throws ActionException {
        BlockProperty bp = new BlockProperty(propertyid, value);
        this.commands.add(bp);
    }

    public void addReturnPropertyCommand(String propertyid, String value) throws ActionException {
        ReturnProperty rp = new ReturnProperty(propertyid, value);
        this.commands.add(rp);
    }

    public void startProcessing() {
        if (this.name != null && this.name.length() > 0) {
            Trace.startActionBlock(this.name);
        }
        if (this.topActionBlock.debugMode && this.topActionBlock == this) {
            this.log("---------- INITIAL STATE ----------");
            this.log(this.toXML());
            this.log("-----------------------------------");
        }
    }

    public void endProcessing() {
        if (this.topActionBlock.debugMode && this.topActionBlock == this) {
            this.log("");
            this.log("----------- FINAL STATE -----------");
            this.log(this.toXML());
            this.log("-----------------------------------");
        }
        if (this.name != null && this.name.length() > 0) {
            Trace.endActionBlock(this.name);
        }
    }

    public void setDebugLog(String log) {
        this.debugLog = new StringBuffer(log);
    }

    public void log(String message) {
        if (this.topActionBlock.debugMode) {
            this.topActionBlock.debugLog.append(message).append("\n");
        }
    }

    public int getCommandCount() {
        return this.commands.size();
    }

    public String getTest() {
        return this.test;
    }

    public ActionBlock getActionBlock(int index) {
        return (ActionBlock)this.commands.get(index);
    }

    private int getActionIndex(String name) throws ActionException {
        for (int i = 0; i < this.commands.size(); ++i) {
            Object o = this.commands.get(i);
            if (!(o instanceof Action) || !((Action)o).name.equals(name)) continue;
            return i;
        }
        throw new ActionException("No action found called " + name);
    }

    public Object getCommand(int index) {
        return this.commands.get(index);
    }

    public class ReturnProperty
    implements Serializable {
        public String propertyid = "";
        public String value = "";

        private ReturnProperty(String propertyid, String value) {
            this.propertyid = propertyid;
            this.value = value;
        }

        public String toString() {
            return this.propertyid + "=" + this.value;
        }

        public String toXML(int indent) {
            String pad = StringUtil.repeat("    ", indent);
            String out = pad + "  <returnproperty id=\"" + this.propertyid + "\"><![CDATA[" + this.value + "]]></returnproperty>\n";
            return out;
        }

        public JSONObject toJSONObject() throws JSONException {
            JSONObject blockPropertyObject = new JSONObject();
            JSONObject idValueObject = new JSONObject();
            blockPropertyObject.put("returnproperty", idValueObject);
            idValueObject.put("id", this.propertyid);
            idValueObject.put("value", this.value);
            return blockPropertyObject;
        }
    }

    public class BlockProperty
    implements Serializable {
        public String propertyid = "";
        public String value = "";

        private BlockProperty(String propertyid, String value) {
            this.propertyid = propertyid;
            this.value = value;
        }

        public String toString() {
            return this.propertyid + "=" + this.value;
        }

        public String toXML(int indent) {
            String pad = StringUtil.repeat("    ", indent);
            String out = pad + "  <blockproperty id=\"" + this.propertyid + "\"><![CDATA[" + this.value + "]]></blockproperty>\n";
            return out;
        }

        public JSONObject toJSONObject() throws JSONException {
            JSONObject blockPropertyObject = new JSONObject();
            JSONObject idValueObject = new JSONObject();
            blockPropertyObject.put("blockproperty", idValueObject);
            idValueObject.put("id", this.propertyid);
            idValueObject.put("value", this.value);
            return blockPropertyObject;
        }
    }

    public class Action
    implements Serializable {
        public String actionid = null;
        public String actionClass = "BaseAction";
        public String versionid = "";
        public String actionlabel = "";
        public String name = "";
        public String test = "";
        public PropertyList properties = null;
        public HashMap hmProperties = null;

        private Action(String test, String actionid, String versionid, PropertyList properties) {
            this.test = test;
            this.actionid = actionid;
            this.versionid = versionid;
            this.properties = properties;
        }

        private Action(String test, String actionClass, PropertyList properties) {
            this.test = test;
            this.actionClass = actionClass;
            this.properties = properties;
        }

        private Action(String test, String actionid, String versionid, HashMap hmProperties) {
            this.test = test;
            this.actionid = actionid;
            this.versionid = versionid;
            this.hmProperties = hmProperties;
            this.properties = new PropertyList(hmProperties);
        }

        private Action(String test, String actionClass, HashMap hmProperties) {
            this.test = test;
            this.actionClass = actionClass;
            this.hmProperties = hmProperties;
            this.properties = new PropertyList(hmProperties);
        }

        public String toString() {
            return this.actionid == null ? this.actionClass : this.actionid;
        }

        public String toXML(int indent) {
            StringBuffer out = new StringBuffer();
            String pad = StringUtil.repeat("    ", indent);
            out.append(pad + "  <action name=\"" + this.name + "\" ");
            if (this.actionid != null && this.actionid.length() > 0) {
                out.append("id=\"" + this.actionid + "\" ");
            }
            if (this.versionid != null && this.versionid.length() > 0) {
                out.append("versionid=\"" + this.versionid + "\" ");
            }
            if (this.actionClass != null && this.actionClass.length() > 0) {
                out.append("class=\"" + this.actionClass + "\" ");
            }
            if (this.test != null && this.test.length() > 0) {
                out.append("test=\"" + this.test + "\" ");
            }
            out.append(">\n");
            TreeSet ap = new TreeSet(this.properties.keySet());
            for (String propertyid : ap) {
                String value = this.properties.getProperty(propertyid);
                out.append(pad + "    <property id=\"" + propertyid + "\"><![CDATA[" + value + "]]></property>\n");
            }
            out.append(pad + "  </action>\n");
            return out.toString();
        }

        public JSONObject toJSONObject() throws JSONException {
            JSONObject actionJSON = new JSONObject();
            JSONObject actionattributeJSON = new JSONObject();
            JSONObject propertyJSON = new JSONObject();
            actionJSON.put(ActionBlock.COMMAND_ACTION, actionattributeJSON);
            actionattributeJSON.put("name", this.name);
            actionattributeJSON.put("id", this.actionid);
            actionattributeJSON.put("property", propertyJSON);
            TreeSet ap = new TreeSet(this.properties.keySet());
            for (String propertyid : ap) {
                String value = this.properties.getProperty(propertyid);
                propertyJSON.put(propertyid, value);
            }
            return actionJSON;
        }
    }
}

