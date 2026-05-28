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
import com.labvantage.sapphire.webservices.client.PropertyValueTransport;
import com.labvantage.sapphire.webservices.transport.PropertyListCollectionTransportBean;
import com.labvantage.sapphire.webservices.transport.PropertyListTransportBean;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Set;
import javax.xml.namespace.QName;
import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.ser.BeanDeserializer;
import org.apache.axis.encoding.ser.BeanSerializer;
import sapphire.xml.PropertyList;

public class PropertyListTransport
implements Serializable {
    private String id;
    private PropertyValueTransport[] propertyValues;
    private Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;
    private static TypeDesc typeDesc = new TypeDesc(PropertyListTransport.class, true);

    public PropertyListTransport() {
    }

    public PropertyListTransport(String id, PropertyValueTransport[] propertyValues) {
        this.id = id;
        this.propertyValues = propertyValues;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PropertyValueTransport[] getPropertyValues() {
        return this.propertyValues;
    }

    public void setPropertyValues(PropertyValueTransport[] propertyValues) {
        this.propertyValues = propertyValues;
    }

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof PropertyListTransport)) {
            return false;
        }
        PropertyListTransport other = (PropertyListTransport)obj;
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
        boolean _equals = (this.id == null && other.getId() == null || this.id != null && this.id.equals(other.getId())) && (this.propertyValues == null && other.getPropertyValues() == null || this.propertyValues != null && Arrays.equals(this.propertyValues, other.getPropertyValues()));
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
        if (this.getPropertyValues() != null) {
            for (int i = 0; i < Array.getLength(this.getPropertyValues()); ++i) {
                Object obj = Array.get(this.getPropertyValues(), i);
                if (obj == null || obj.getClass().isArray()) continue;
                _hashCode += obj.hashCode();
            }
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

    public void setPropertyList(PropertyList propertyList) {
        if (propertyList != null) {
            this.setId(propertyList.getId());
            Set keyset = propertyList.keySet();
            this.propertyValues = new PropertyValueTransport[keyset.size()];
            int propIndex = 0;
            for (String propertyid : keyset) {
                Object value = propertyList.get(propertyid);
                this.propertyValues[propIndex] = new PropertyValueTransport();
                this.propertyValues[propIndex].setValue(propertyid, value);
                ++propIndex;
            }
        }
    }

    public PropertyList getPropertyList() {
        PropertyList props = new PropertyList();
        props.setId(this.id);
        for (int i = 0; i < this.propertyValues.length; ++i) {
            Object value = this.propertyValues[i].getValue();
            if (value instanceof PropertyListCollectionTransportBean) {
                props.put(this.propertyValues[i].getId(), ((PropertyListCollectionTransport)value).getPropertyListCollection());
                continue;
            }
            if (value instanceof PropertyListTransportBean) {
                props.put(this.propertyValues[i].getId(), ((PropertyListTransport)value).getPropertyList());
                continue;
            }
            props.put(this.propertyValues[i].getId(), value.toString());
        }
        return props;
    }

    static {
        typeDesc.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyListTransport"));
        ElementDesc elemField = new ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new QName("", "id"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("propertyValues");
        elemField.setXmlName(new QName("", "propertyValues"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyValueTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
    }
}

