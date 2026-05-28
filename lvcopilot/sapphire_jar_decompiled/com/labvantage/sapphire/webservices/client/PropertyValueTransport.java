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
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PropertyValueTransport
implements Serializable {
    private String id;
    private String propertyId;
    private PropertyListCollectionTransport propertyListCollectionValue;
    private PropertyListTransport propertyListValue;
    private String propertyValue;
    private int type;
    private Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;
    private static TypeDesc typeDesc = new TypeDesc(PropertyValueTransport.class, true);

    public PropertyValueTransport() {
    }

    public PropertyValueTransport(String id, String propertyId, PropertyListCollectionTransport propertyListCollectionValue, PropertyListTransport propertyListValue, String propertyValue, int type) {
        this.id = id;
        this.propertyId = propertyId;
        this.propertyListCollectionValue = propertyListCollectionValue;
        this.propertyListValue = propertyListValue;
        this.propertyValue = propertyValue;
        this.type = type;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPropertyId() {
        return this.propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public PropertyListCollectionTransport getPropertyListCollectionValue() {
        return this.propertyListCollectionValue;
    }

    public void setPropertyListCollectionValue(PropertyListCollectionTransport propertyListCollectionValue) {
        this.propertyListCollectionValue = propertyListCollectionValue;
    }

    public PropertyListTransport getPropertyListValue() {
        return this.propertyListValue;
    }

    public void setPropertyListValue(PropertyListTransport propertyListValue) {
        this.propertyListValue = propertyListValue;
    }

    public String getPropertyValue() {
        return this.propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof PropertyValueTransport)) {
            return false;
        }
        PropertyValueTransport other = (PropertyValueTransport)obj;
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
        boolean _equals = (this.id == null && other.getId() == null || this.id != null && this.id.equals(other.getId())) && (this.propertyId == null && other.getPropertyId() == null || this.propertyId != null && this.propertyId.equals(other.getPropertyId())) && (this.propertyListCollectionValue == null && other.getPropertyListCollectionValue() == null || this.propertyListCollectionValue != null && this.propertyListCollectionValue.equals(other.getPropertyListCollectionValue())) && (this.propertyListValue == null && other.getPropertyListValue() == null || this.propertyListValue != null && this.propertyListValue.equals(other.getPropertyListValue())) && (this.propertyValue == null && other.getPropertyValue() == null || this.propertyValue != null && this.propertyValue.equals(other.getPropertyValue())) && this.type == other.getType();
        this.__equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (this.__hashCodeCalc) {
            return 0;
        }
        this.__hashCodeCalc = true;
        int _hashCode = 1;
        if (this.getId() != null) {
            _hashCode += this.getId().hashCode();
        }
        if (this.getPropertyId() != null) {
            _hashCode += this.getPropertyId().hashCode();
        }
        if (this.getPropertyListCollectionValue() != null) {
            _hashCode += this.getPropertyListCollectionValue().hashCode();
        }
        if (this.getPropertyListValue() != null) {
            _hashCode += this.getPropertyListValue().hashCode();
        }
        if (this.getPropertyValue() != null) {
            _hashCode += this.getPropertyValue().hashCode();
        }
        this.__hashCodeCalc = false;
        return _hashCode += this.getType();
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

    protected void setValue(String id, Object value) {
        this.setPropertyId(id);
        if (value instanceof PropertyList) {
            this.type = 1;
            this.propertyListValue = new PropertyListTransport();
            this.propertyListValue.setPropertyList((PropertyList)value);
        } else if (value instanceof PropertyListCollection) {
            this.type = 2;
            this.propertyListCollectionValue = new PropertyListCollectionTransport();
            this.propertyListCollectionValue.setPropertyListCollection((PropertyListCollection)value);
        } else {
            this.type = 0;
            this.propertyValue = value.toString();
        }
    }

    protected Object getValue() {
        switch (this.type) {
            case 0: {
                return this.propertyValue;
            }
            case 1: {
                return this.propertyListValue;
            }
            case 2: {
                return this.propertyListCollectionValue;
            }
        }
        return null;
    }

    static {
        typeDesc.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyValueTransport"));
        ElementDesc elemField = new ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new QName("", "id"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("propertyId");
        elemField.setXmlName(new QName("", "propertyId"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("propertyListCollectionValue");
        elemField.setXmlName(new QName("", "propertyListCollectionValue"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyListCollectionTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("propertyListValue");
        elemField.setXmlName(new QName("", "propertyListValue"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyListTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("propertyValue");
        elemField.setXmlName(new QName("", "propertyValue"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new QName("", "type"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
    }
}

