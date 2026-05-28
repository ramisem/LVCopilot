/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.axis.description.ElementDesc
 *  org.apache.axis.description.FieldDesc
 *  org.apache.axis.description.TypeDesc
 *  org.apache.axis.encoding.Deserializer
 *  org.apache.axis.encoding.Serializer
 *  org.apache.axis.encoding.ser.BeanDeserializer
 *  org.apache.axis.encoding.ser.BeanSerializer
 */
package com.labvantage.sapphire.webservices.client;

import com.labvantage.sapphire.webservices.client.PropertyListCollectionTransport;
import com.labvantage.sapphire.webservices.client.PropertyListTransport;
import java.io.Serializable;
import javax.xml.namespace.QName;
import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.ser.BeanDeserializer;
import org.apache.axis.encoding.ser.BeanSerializer;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ActionBlockTransport
implements Serializable {
    private PropertyListTransport blockProperties;
    private PropertyListCollectionTransport commands;
    private int errorAction;
    private PropertyListTransport returnProperties;
    private String test;
    private Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;
    private static TypeDesc typeDesc = new TypeDesc(ActionBlockTransport.class, true);

    public ActionBlockTransport() {
    }

    public ActionBlockTransport(PropertyListTransport blockProperties, PropertyListCollectionTransport commands, int errorAction, PropertyListTransport returnProperties, String test) {
        this.blockProperties = blockProperties;
        this.commands = commands;
        this.errorAction = errorAction;
        this.returnProperties = returnProperties;
        this.test = test;
    }

    public PropertyListTransport getBlockProperties() {
        return this.blockProperties;
    }

    public void setBlockProperties(PropertyListTransport blockProperties) {
        this.blockProperties = blockProperties;
    }

    public PropertyListCollectionTransport getCommands() {
        return this.commands;
    }

    public void setCommands(PropertyListCollectionTransport commands) {
        this.commands = commands;
    }

    public int getErrorAction() {
        return this.errorAction;
    }

    public void setErrorAction(int errorAction) {
        this.errorAction = errorAction;
    }

    public PropertyListTransport getReturnProperties() {
        return this.returnProperties;
    }

    public void setReturnProperties(PropertyListTransport returnProperties) {
        this.returnProperties = returnProperties;
    }

    public String getTest() {
        return this.test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof ActionBlockTransport)) {
            return false;
        }
        ActionBlockTransport other = (ActionBlockTransport)obj;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.__equalsCalc != null) {
            return this.__equalsCalc == obj;
        }
        this.__equalsCalc = obj;
        boolean _equals = (this.blockProperties == null && other.getBlockProperties() == null || this.blockProperties != null && this.blockProperties.equals(other.getBlockProperties())) && (this.commands == null && other.getCommands() == null || this.commands != null && this.commands.equals(other.getCommands())) && this.errorAction == other.getErrorAction() && (this.returnProperties == null && other.getReturnProperties() == null || this.returnProperties != null && this.returnProperties.equals(other.getReturnProperties())) && (this.test == null && other.getTest() == null || this.test != null && this.test.equals(other.getTest()));
        this.__equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (this.__hashCodeCalc) {
            return 0;
        }
        this.__hashCodeCalc = true;
        int _hashCode = 1;
        if (this.getBlockProperties() != null) {
            _hashCode += this.getBlockProperties().hashCode();
        }
        if (this.getCommands() != null) {
            _hashCode += this.getCommands().hashCode();
        }
        _hashCode += this.getErrorAction();
        if (this.getReturnProperties() != null) {
            _hashCode += this.getReturnProperties().hashCode();
        }
        if (this.getTest() != null) {
            _hashCode += this.getTest().hashCode();
        }
        this.__hashCodeCalc = false;
        return _hashCode;
    }

    public static TypeDesc getTypeDesc() {
        return typeDesc;
    }

    public static Serializer getSerializer(String mechType, Class _javaType, QName _xmlType) {
        return new BeanSerializer(_javaType, _xmlType, typeDesc);
    }

    public static Deserializer getDeserializer(String mechType, Class _javaType, QName _xmlType) {
        return new BeanDeserializer(_javaType, _xmlType, typeDesc);
    }

    public void setActionBlock(ActionBlock actionBlock) {
        if (actionBlock != null) {
            PropertyListCollection coms = new PropertyListCollection();
            for (int i = 0; i < actionBlock.getActionCount(); ++i) {
                try {
                    PropertyList list = new PropertyList();
                    list.setProperty("actionClass", actionBlock.getActionClass(i));
                    list.setProperty("actionid", actionBlock.getActionid(i));
                    list.setProperty("name", actionBlock.getActionName(i));
                    list.setProperty("versionid", actionBlock.getVersionid(i));
                    list.setProperty("properties", new PropertyList(actionBlock.getActionProperties(i)));
                    list.setProperty("test", actionBlock.getActionTest(i));
                    coms.add(list);
                    continue;
                }
                catch (Exception list) {
                    // empty catch block
                }
            }
            this.commands.setPropertyListCollection(coms);
            PropertyList bp = new PropertyList(actionBlock.getBlockProperties());
            this.blockProperties.setPropertyList(bp);
            PropertyList rp = new PropertyList(actionBlock.getReturnProperties());
            this.returnProperties.setPropertyList(rp);
            this.errorAction = actionBlock.getErrorAction();
            this.test = actionBlock.getTest();
        }
    }

    public ActionBlock getActionBlock() {
        ActionBlock out = new ActionBlock();
        PropertyListCollection actions = this.commands.getPropertyListCollection();
        for (int i = 0; i < actions.size(); ++i) {
            PropertyList action = actions.getPropertyList(i);
            try {
                if (action.getProperty("name", "").length() <= 0) continue;
                if (action.getProperty("actionid", "").length() > 0) {
                    out.setAction(action.getProperty("name"), action.getProperty("test"), "", action.getProperty("actionid"), action.getProperty("versionid", "1"), action.getPropertyList("properties"));
                    continue;
                }
                if (action.getProperty("actionClass", "").length() <= 0) continue;
                out.setAction(action.getProperty("name"), action.getProperty("test"), action.getProperty("actionClass"), "", "", action.getPropertyList("properties"));
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        out.setBlockProperties(this.blockProperties.getPropertyList());
        out.setReturnProperties(this.returnProperties.getPropertyList());
        out.setTest(this.test);
        out.setErrorAction(this.errorAction);
        return out;
    }

    static {
        typeDesc.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "ActionBlockTransport"));
        ElementDesc elemField = new ElementDesc();
        elemField.setFieldName("blockProperties");
        elemField.setXmlName(new QName("", "blockProperties"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyListTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("commands");
        elemField.setXmlName(new QName("", "commands"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyListCollectionTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("errorAction");
        elemField.setXmlName(new QName("", "errorAction"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("returnProperties");
        elemField.setXmlName(new QName("", "returnProperties"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyListTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("test");
        elemField.setXmlName(new QName("", "test"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
    }
}

