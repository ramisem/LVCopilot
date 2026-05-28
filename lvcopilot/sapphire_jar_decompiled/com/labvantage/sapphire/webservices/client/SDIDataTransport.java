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

import com.labvantage.sapphire.webservices.client.DataSetTransport;
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
import sapphire.util.DataSet;

public class SDIDataTransport
implements Serializable {
    private String SDCId;
    private DataSetTransport[] dataSets;
    private String[] keys;
    private String[] names;
    private String rsetId;
    private Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;
    private static TypeDesc typeDesc = new TypeDesc(SDIDataTransport.class, true);

    public SDIDataTransport() {
    }

    public SDIDataTransport(String SDCId, DataSetTransport[] dataSets, String[] keys, String[] names, String rsetId) {
        this.SDCId = SDCId;
        this.dataSets = dataSets;
        this.keys = keys;
        this.names = names;
        this.rsetId = rsetId;
    }

    public String getSDCId() {
        return this.SDCId;
    }

    public void setSDCId(String SDCId) {
        this.SDCId = SDCId;
    }

    public DataSetTransport[] getDataSets() {
        return this.dataSets;
    }

    public void setDataSets(DataSetTransport[] dataSets) {
        this.dataSets = dataSets;
    }

    public String[] getKeys() {
        return this.keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public String[] getNames() {
        return this.names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public String getRsetId() {
        return this.rsetId;
    }

    public void setRsetId(String rsetId) {
        this.rsetId = rsetId;
    }

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof SDIDataTransport)) {
            return false;
        }
        SDIDataTransport other = (SDIDataTransport)obj;
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
        boolean _equals = (this.SDCId == null && other.getSDCId() == null || this.SDCId != null && this.SDCId.equals(other.getSDCId())) && (this.dataSets == null && other.getDataSets() == null || this.dataSets != null && Arrays.equals(this.dataSets, other.getDataSets())) && (this.keys == null && other.getKeys() == null || this.keys != null && Arrays.equals(this.keys, other.getKeys())) && (this.names == null && other.getNames() == null || this.names != null && Arrays.equals(this.names, other.getNames())) && (this.rsetId == null && other.getRsetId() == null || this.rsetId != null && this.rsetId.equals(other.getRsetId()));
        this.__equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        Object obj;
        int i;
        if (this.__hashCodeCalc) {
            return 0;
        }
        this.__hashCodeCalc = true;
        int _hashCode = 1;
        if (this.getSDCId() != null) {
            _hashCode += this.getSDCId().hashCode();
        }
        if (this.getDataSets() != null) {
            for (i = 0; i < Array.getLength(this.getDataSets()); ++i) {
                obj = Array.get(this.getDataSets(), i);
                if (obj == null || obj.getClass().isArray()) continue;
                _hashCode += obj.hashCode();
            }
        }
        if (this.getKeys() != null) {
            for (i = 0; i < Array.getLength(this.getKeys()); ++i) {
                obj = Array.get(this.getKeys(), i);
                if (obj == null || obj.getClass().isArray()) continue;
                _hashCode += obj.hashCode();
            }
        }
        if (this.getNames() != null) {
            for (i = 0; i < Array.getLength(this.getNames()); ++i) {
                obj = Array.get(this.getNames(), i);
                if (obj == null || obj.getClass().isArray()) continue;
                _hashCode += obj.hashCode();
            }
        }
        if (this.getRsetId() != null) {
            _hashCode += this.getRsetId().hashCode();
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

    public DataSet getDataSet(String name) {
        DataSet out = null;
        for (int i = 0; i < this.getNames().length; ++i) {
            if (!this.getNames()[i].equals(name)) continue;
            DataSetTransport dataSetTransport = this.getDataSets()[i];
            out = dataSetTransport.getDataSet();
            break;
        }
        return out;
    }

    static {
        typeDesc.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "SDIDataTransport"));
        ElementDesc elemField = new ElementDesc();
        elemField.setFieldName("SDCId");
        elemField.setXmlName(new QName("", "SDCId"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("dataSets");
        elemField.setXmlName(new QName("", "dataSets"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "DataSetTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("keys");
        elemField.setXmlName(new QName("", "keys"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("names");
        elemField.setXmlName(new QName("", "names"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("rsetId");
        elemField.setXmlName(new QName("", "rsetId"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
    }
}

