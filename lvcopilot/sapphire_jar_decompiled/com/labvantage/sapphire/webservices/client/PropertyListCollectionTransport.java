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

import com.labvantage.sapphire.webservices.client.PropertyListTransport;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
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

public class PropertyListCollectionTransport
implements Serializable {
    private String id;
    private PropertyListTransport[] propertyLists;
    private Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;
    private static TypeDesc typeDesc = new TypeDesc(PropertyListCollectionTransport.class, true);

    public PropertyListCollectionTransport() {
    }

    public PropertyListCollectionTransport(String id, PropertyListTransport[] propertyLists) {
        this.id = id;
        this.propertyLists = propertyLists;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PropertyListTransport[] getPropertyLists() {
        return this.propertyLists;
    }

    public void setPropertyLists(PropertyListTransport[] propertyLists) {
        this.propertyLists = propertyLists;
    }

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof PropertyListCollectionTransport)) {
            return false;
        }
        PropertyListCollectionTransport other = (PropertyListCollectionTransport)obj;
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
        boolean _equals = (this.id == null && other.getId() == null || this.id != null && this.id.equals(other.getId())) && (this.propertyLists == null && other.getPropertyLists() == null || this.propertyLists != null && Arrays.equals(this.propertyLists, other.getPropertyLists()));
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
        if (this.getPropertyLists() != null) {
            for (int i = 0; i < Array.getLength(this.getPropertyLists()); ++i) {
                Object obj = Array.get(this.getPropertyLists(), i);
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

    public void setPropertyListCollection(PropertyListCollection propertyListCollection) {
        if (propertyListCollection != null) {
            this.setId(propertyListCollection.getId());
            this.propertyLists = new PropertyListTransport[propertyListCollection.size()];
            for (int i = 0; i < propertyListCollection.size(); ++i) {
                PropertyList pl = propertyListCollection.getPropertyList(i);
                this.propertyLists[i] = new PropertyListTransport();
                this.propertyLists[i].setPropertyList(pl);
            }
        }
    }

    public PropertyListCollection getPropertyListCollection() {
        PropertyListCollection props = new PropertyListCollection();
        props.setId(this.id);
        for (int i = 0; i < this.propertyLists.length; ++i) {
            props.add(this.propertyLists[i].getPropertyList());
        }
        return props;
    }

    static {
        typeDesc.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyListCollectionTransport"));
        ElementDesc elemField = new ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new QName("", "id"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("propertyLists");
        elemField.setXmlName(new QName("", "propertyLists"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyListTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
    }
}

