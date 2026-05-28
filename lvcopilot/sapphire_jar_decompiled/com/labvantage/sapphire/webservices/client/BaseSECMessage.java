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

import java.io.Serializable;
import javax.xml.namespace.QName;
import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.ser.BeanDeserializer;
import org.apache.axis.encoding.ser.BeanSerializer;

public abstract class BaseSECMessage
implements Serializable {
    private String error;
    private String id;
    private String log;
    private int msgFlow;
    private String status;
    private String typeId;
    private String version;
    private Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;
    private static TypeDesc typeDesc = new TypeDesc(BaseSECMessage.class, true);

    public BaseSECMessage() {
    }

    public BaseSECMessage(String error, String id, String log, int msgFlow, String status, String typeId, String version) {
        this.error = error;
        this.id = id;
        this.log = log;
        this.msgFlow = msgFlow;
        this.status = status;
        this.typeId = typeId;
        this.version = version;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLog() {
        return this.log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public int getMsgFlow() {
        return this.msgFlow;
    }

    public void setMsgFlow(int msgFlow) {
        this.msgFlow = msgFlow;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTypeId() {
        return this.typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof BaseSECMessage)) {
            return false;
        }
        BaseSECMessage other = (BaseSECMessage)obj;
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
        boolean _equals = (this.error == null && other.getError() == null || this.error != null && this.error.equals(other.getError())) && (this.id == null && other.getId() == null || this.id != null && this.id.equals(other.getId())) && (this.log == null && other.getLog() == null || this.log != null && this.log.equals(other.getLog())) && this.msgFlow == other.getMsgFlow() && (this.status == null && other.getStatus() == null || this.status != null && this.status.equals(other.getStatus())) && (this.typeId == null && other.getTypeId() == null || this.typeId != null && this.typeId.equals(other.getTypeId())) && (this.version == null && other.getVersion() == null || this.version != null && this.version.equals(other.getVersion()));
        this.__equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (this.__hashCodeCalc) {
            return 0;
        }
        this.__hashCodeCalc = true;
        int _hashCode = 1;
        if (this.getError() != null) {
            _hashCode += this.getError().hashCode();
        }
        if (this.getId() != null) {
            _hashCode += this.getId().hashCode();
        }
        if (this.getLog() != null) {
            _hashCode += this.getLog().hashCode();
        }
        _hashCode += this.getMsgFlow();
        if (this.getStatus() != null) {
            _hashCode += this.getStatus().hashCode();
        }
        if (this.getTypeId() != null) {
            _hashCode += this.getTypeId().hashCode();
        }
        if (this.getVersion() != null) {
            _hashCode += this.getVersion().hashCode();
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
        typeDesc.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "BaseSECMessage"));
        ElementDesc elemField = new ElementDesc();
        elemField.setFieldName("error");
        elemField.setXmlName(new QName("", "error"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new QName("", "id"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("log");
        elemField.setXmlName(new QName("", "log"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("msgFlow");
        elemField.setXmlName(new QName("", "msgFlow"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new QName("", "status"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("typeId");
        elemField.setXmlName(new QName("", "typeId"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("version");
        elemField.setXmlName(new QName("", "version"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
    }
}

