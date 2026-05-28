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

import java.io.Serializable;
import javax.xml.namespace.QName;
import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.ser.BeanDeserializer;
import org.apache.axis.encoding.ser.BeanSerializer;

public class AttachmentTransport
implements Serializable {
    private String OLEClass;
    private String attachmentPolicyNode;
    private String data;
    private String description;
    private String filename;
    private String sourceFilename;
    private String type;
    private boolean zipped;
    private Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;
    private static TypeDesc typeDesc = new TypeDesc(AttachmentTransport.class, true);

    public AttachmentTransport() {
    }

    public AttachmentTransport(String OLEClass, String attachmentPolicyNode, String data, String description, String filename, String sourceFilename, String type, boolean zipped) {
        this.OLEClass = OLEClass;
        this.attachmentPolicyNode = attachmentPolicyNode;
        this.data = data;
        this.description = description;
        this.filename = filename;
        this.sourceFilename = sourceFilename;
        this.type = type;
        this.zipped = zipped;
    }

    public String getOLEClass() {
        return this.OLEClass;
    }

    public void setOLEClass(String OLEClass) {
        this.OLEClass = OLEClass;
    }

    public String getAttachmentPolicyNode() {
        return this.attachmentPolicyNode;
    }

    public void setAttachmentPolicyNode(String attachmentPolicyNode) {
        this.attachmentPolicyNode = attachmentPolicyNode;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSourceFilename() {
        return this.sourceFilename;
    }

    public void setSourceFilename(String sourceFilename) {
        this.sourceFilename = sourceFilename;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isZipped() {
        return this.zipped;
    }

    public void setZipped(boolean zipped) {
        this.zipped = zipped;
    }

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof AttachmentTransport)) {
            return false;
        }
        AttachmentTransport other = (AttachmentTransport)obj;
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
        boolean _equals = (this.OLEClass == null && other.getOLEClass() == null || this.OLEClass != null && this.OLEClass.equals(other.getOLEClass())) && (this.attachmentPolicyNode == null && other.getAttachmentPolicyNode() == null || this.attachmentPolicyNode != null && this.attachmentPolicyNode.equals(other.getAttachmentPolicyNode())) && (this.data == null && other.getData() == null || this.data != null && this.data.equals(other.getData())) && (this.description == null && other.getDescription() == null || this.description != null && this.description.equals(other.getDescription())) && (this.filename == null && other.getFilename() == null || this.filename != null && this.filename.equals(other.getFilename())) && (this.sourceFilename == null && other.getSourceFilename() == null || this.sourceFilename != null && this.sourceFilename.equals(other.getSourceFilename())) && (this.type == null && other.getType() == null || this.type != null && this.type.equals(other.getType())) && this.zipped == other.isZipped();
        this.__equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (this.__hashCodeCalc) {
            return 0;
        }
        this.__hashCodeCalc = true;
        int _hashCode = 1;
        if (this.getOLEClass() != null) {
            _hashCode += this.getOLEClass().hashCode();
        }
        if (this.getAttachmentPolicyNode() != null) {
            _hashCode += this.getAttachmentPolicyNode().hashCode();
        }
        if (this.getData() != null) {
            _hashCode += this.getData().hashCode();
        }
        if (this.getDescription() != null) {
            _hashCode += this.getDescription().hashCode();
        }
        if (this.getFilename() != null) {
            _hashCode += this.getFilename().hashCode();
        }
        if (this.getSourceFilename() != null) {
            _hashCode += this.getSourceFilename().hashCode();
        }
        if (this.getType() != null) {
            _hashCode += this.getType().hashCode();
        }
        Boolean bl = this.isZipped() ? Boolean.TRUE : Boolean.FALSE;
        this.__hashCodeCalc = false;
        return _hashCode += bl.hashCode();
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
        typeDesc.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "AttachmentTransport"));
        ElementDesc elemField = new ElementDesc();
        elemField.setFieldName("OLEClass");
        elemField.setXmlName(new QName("", "OLEClass"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("attachmentPolicyNode");
        elemField.setXmlName(new QName("", "attachmentPolicyNode"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("data");
        elemField.setXmlName(new QName("", "data"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new QName("", "description"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("filename");
        elemField.setXmlName(new QName("", "filename"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("sourceFilename");
        elemField.setXmlName(new QName("", "sourceFilename"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new QName("", "type"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("zipped");
        elemField.setXmlName(new QName("", "zipped"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
    }
}

