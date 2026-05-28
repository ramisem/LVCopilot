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
package com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub;

import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.PropertyListCollectionTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.PropertyListTransport;
import java.io.Serializable;
import javax.xml.namespace.QName;
import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.ser.BeanDeserializer;
import org.apache.axis.encoding.ser.BeanSerializer;

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

