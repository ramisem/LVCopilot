/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.SapphireSaxHandler;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Stack;
import org.apache.commons.codec.binary.Base64;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.accessor.ActionException;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;

public class ActionBlockHandler
extends SapphireSaxHandler {
    private StringBuffer currentElementChars = new StringBuffer();
    private Stack actionblockStack = new Stack();
    private int actionNameIndex = 0;
    private String propertyid;
    private String blockpropertyid;
    private String returnpropertyid;
    private boolean startedActionBlock = false;
    private String testname = "";

    public ActionBlockHandler(ActionBlock actionBlock) {
        this.actionblockStack.push(actionBlock);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.currentElementChars.delete(0, this.currentElementChars.length());
        Properties attr = this.getAttributes(attributes);
        if (qName.equalsIgnoreCase("ACTIONBLOCK")) {
            String caseValue;
            String name;
            String test;
            if (this.actionblockStack.size() == 0 || !this.startedActionBlock) {
                this.log("Starting ACTIONBLOCK...");
                this.startedActionBlock = true;
                if (this.actionblockStack.size() == 0) {
                    ActionBlock ab = new ActionBlock();
                    this.actionblockStack.push(ab);
                }
            } else {
                try {
                    this.log("Starting nested ACTIONBLOCK...");
                    ActionBlock actionBlock = new ActionBlock();
                    ActionBlock currentAB = (ActionBlock)this.actionblockStack.peek();
                    currentAB.addActionBlockCommand(actionBlock);
                    this.actionblockStack.push(actionBlock);
                }
                catch (ActionException e) {
                    throw new SAXException("Unable to generate a conditional actionblock");
                }
            }
            if ((test = attr.getProperty("test")) != null && test.length() > 0) {
                ((ActionBlock)this.actionblockStack.peek()).setTest(test);
            }
            if ((name = attr.getProperty("name")) != null && name.length() > 0) {
                ((ActionBlock)this.actionblockStack.peek()).setName(name);
            }
            if ((caseValue = attr.getProperty("case")) != null && caseValue.length() > 0) {
                ((ActionBlock)this.actionblockStack.peek()).setCaseValue(caseValue);
            }
        } else if (qName.equalsIgnoreCase("ACTION")) {
            try {
                ActionBlock currentAB = (ActionBlock)this.actionblockStack.peek();
                String actionid = attr.getProperty("id");
                String actionName = attr.getProperty("name");
                String actionTest = attr.getProperty("test");
                String actionLabel = attr.getProperty("label");
                if (actionName == null || actionName.length() == 0) {
                    actionName = String.valueOf(this.actionNameIndex);
                }
                if (actionid != null && actionid.length() > 0) {
                    this.log("Start ACTION " + actionid);
                    String actionversionid = attr.getProperty("version", "1");
                    currentAB.setAction(actionName, actionTest, "", actionid, actionversionid, new PropertyList());
                    currentAB.setActionLabel(actionName, actionLabel);
                }
                String actionclass = attr.getProperty("class", attr.getProperty("actionclass"));
                this.log("Start ACTION " + actionclass);
                currentAB.setAction(actionName, actionTest, actionclass, "", "", new PropertyList());
                currentAB.setActionLabel(actionName, actionLabel);
            }
            catch (ActionException e) {
                throw new SAXException("Unrecognized action or action class", e);
            }
        } else if (qName.equalsIgnoreCase("CASE")) {
            try {
                ActionBlock currentAB = (ActionBlock)this.actionblockStack.peek();
                String test = attr.getProperty("test");
                String actionid = "IsConditionMet";
                String actionversionid = "1";
                String actionName = String.valueOf(this.actionNameIndex);
                this.log("Case Statement: Start ACTION " + actionid);
                currentAB.setAction(actionName, actionid, actionversionid);
                PropertyList props = new PropertyList();
                props.setProperty("condition", "$G{" + test + "}");
                currentAB.setActionProperties(actionName, props);
            }
            catch (ActionException e) {
                throw new SAXException("Unrecognized action or action class", e);
            }
        } else if (qName.equalsIgnoreCase("PROPERTY")) {
            this.propertyid = attr.getProperty("id").toLowerCase();
        } else if (qName.equalsIgnoreCase("BLOCKPROPERTY")) {
            this.blockpropertyid = attr.getProperty("id").toLowerCase();
        } else if (qName.equalsIgnoreCase("RETURNPROPERTY")) {
            this.returnpropertyid = attr.getProperty("id").toLowerCase();
        } else if (qName.equalsIgnoreCase("TEST")) {
            this.testname = attr.getProperty("name") != null ? attr.getProperty("name").toLowerCase() : "";
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String test;
        if (qName.equalsIgnoreCase("ACTIONBLOCK")) {
            ActionBlock parentAB;
            ActionBlock actionBlock;
            this.log("End ACTIONBLOCK...");
            if (this.actionblockStack.size() > 0 && (actionBlock = (ActionBlock)this.actionblockStack.pop()).getCaseValue().length() > 0 && (parentAB = (ActionBlock)this.actionblockStack.peek()).getTest().length() > 0) {
                parentAB.addChildCaseActionBlock(actionBlock.getCaseValue(), actionBlock);
            }
        } else if (qName.equalsIgnoreCase("ACTION")) {
            this.log("End ACTION...");
            ++this.actionNameIndex;
        } else if (qName.equalsIgnoreCase("PROPERTY")) {
            String value = this.currentElementChars.toString().trim();
            try {
                ActionBlock currentAB = (ActionBlock)this.actionblockStack.peek();
                this.log("Setting PROPERTY " + this.propertyid + "=" + value);
                currentAB.getActionProperties(currentAB.getActionCount() - 1).put(this.propertyid, value);
            }
            catch (ActionException e) {
                throw new SAXException("Unable to find action properties", e);
            }
        } else if (qName.equalsIgnoreCase("BLOCKPROPERTY")) {
            ActionBlock currentAB = (ActionBlock)this.actionblockStack.peek();
            String value = this.currentElementChars.toString().trim();
            try {
                this.log("Adding BLOCKPROPERTY command: " + this.blockpropertyid + "=" + value);
                currentAB.addBlockPropertyCommand(this.blockpropertyid, value);
            }
            catch (ActionException e) {
                throw new SAXException("Unable to add a blockproperty command: " + this.blockpropertyid + "=" + value, e);
            }
        } else if (qName.equalsIgnoreCase("GROOVYBINDINGS")) {
            ActionBlock currentAB = (ActionBlock)this.actionblockStack.peek();
            String value = this.currentElementChars.toString().trim();
            try {
                this.log("Adding GROOVYBINDINGS: " + value);
                ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decodeBase64((String)value));
                ObjectInputStream ois = new ObjectInputStream(bais);
                HashMap groovybindings = (HashMap)ois.readObject();
                currentAB.setGroovyBindings(groovybindings);
            }
            catch (Exception e) {
                throw new SAXException("Unable to add Groovy bindings: " + value, e);
            }
        } else if (qName.equalsIgnoreCase("RETURNPROPERTY")) {
            ActionBlock currentAB = (ActionBlock)this.actionblockStack.peek();
            String value = this.currentElementChars.toString().trim();
            try {
                this.log("Adding RETURNPROPERTY command: " + this.returnpropertyid + "=" + value);
                currentAB.addReturnPropertyCommand(this.returnpropertyid, value);
            }
            catch (ActionException e) {
                throw new SAXException("Unable to add a returnproperty command: " + this.returnpropertyid + "=" + value, e);
            }
        } else if (qName.equalsIgnoreCase("TEST") && (test = this.currentElementChars.toString().trim()) != null && test.length() > 0) {
            ActionBlock currentAB = (ActionBlock)this.actionblockStack.peek();
            this.log("Setting ACTIONBLOCK TEST: " + test);
            currentAB.setTest(test);
            currentAB.setTestName(this.testname);
            this.testname = "";
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.currentElementChars != null) {
            this.currentElementChars.append(this.getCharacters(ch, start, length));
        }
    }
}

