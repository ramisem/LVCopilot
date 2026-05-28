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

import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.DataSetColumnTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.DataSetRowTransport;
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

public class DataSetTransport
implements Serializable {
    private DataSetColumnTransport[] columns;
    private String id;
    private DataSetRowTransport[] rows;
    private Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;
    private static TypeDesc typeDesc = new TypeDesc(DataSetTransport.class, true);

    public DataSetTransport() {
    }

    public DataSetTransport(DataSetColumnTransport[] columns, String id, DataSetRowTransport[] rows) {
        this.columns = columns;
        this.id = id;
        this.rows = rows;
    }

    public DataSetColumnTransport[] getColumns() {
        return this.columns;
    }

    public void setColumns(DataSetColumnTransport[] columns) {
        this.columns = columns;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DataSetRowTransport[] getRows() {
        return this.rows;
    }

    public void setRows(DataSetRowTransport[] rows) {
        this.rows = rows;
    }

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof DataSetTransport)) {
            return false;
        }
        DataSetTransport other = (DataSetTransport)obj;
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
        boolean _equals = (this.columns == null && other.getColumns() == null || this.columns != null && Arrays.equals(this.columns, other.getColumns())) && (this.id == null && other.getId() == null || this.id != null && this.id.equals(other.getId())) && (this.rows == null && other.getRows() == null || this.rows != null && Arrays.equals(this.rows, other.getRows()));
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
        if (this.getColumns() != null) {
            for (i = 0; i < Array.getLength(this.getColumns()); ++i) {
                obj = Array.get(this.getColumns(), i);
                if (obj == null || obj.getClass().isArray()) continue;
                _hashCode += obj.hashCode();
            }
        }
        if (this.getId() != null) {
            _hashCode += this.getId().hashCode();
        }
        if (this.getRows() != null) {
            for (i = 0; i < Array.getLength(this.getRows()); ++i) {
                obj = Array.get(this.getRows(), i);
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

    static {
        typeDesc.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "DataSetTransport"));
        ElementDesc elemField = new ElementDesc();
        elemField.setFieldName("columns");
        elemField.setXmlName(new QName("", "columns"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "DataSetColumnTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new QName("", "id"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("rows");
        elemField.setXmlName(new QName("", "rows"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "DataSetRowTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
    }
}

