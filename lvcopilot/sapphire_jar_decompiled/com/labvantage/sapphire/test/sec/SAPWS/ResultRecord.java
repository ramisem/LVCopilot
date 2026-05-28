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
package com.labvantage.sapphire.test.sec.SAPWS;

import java.io.Serializable;
import javax.xml.namespace.QName;
import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.ser.BeanDeserializer;
import org.apache.axis.encoding.ser.BeanSerializer;

public class ResultRecord
implements Serializable {
    private String ZSEC_DATA;
    private Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;
    private static TypeDesc typeDesc = new TypeDesc(ResultRecord.class, true);

    public ResultRecord() {
    }

    public ResultRecord(String ZSEC_DATA) {
        this.ZSEC_DATA = ZSEC_DATA;
    }

    public String getZSEC_DATA() {
        return this.ZSEC_DATA;
    }

    public void setZSEC_DATA(String ZSEC_DATA) {
        this.ZSEC_DATA = ZSEC_DATA;
    }

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof ResultRecord)) {
            return false;
        }
        ResultRecord other = (ResultRecord)obj;
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
        boolean _equals = this.ZSEC_DATA == null && other.getZSEC_DATA() == null || this.ZSEC_DATA != null && this.ZSEC_DATA.equals(other.getZSEC_DATA());
        this.__equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (this.__hashCodeCalc) {
            return 0;
        }
        this.__hashCodeCalc = true;
        int _hashCode = 1;
        if (this.getZSEC_DATA() != null) {
            _hashCode += this.getZSEC_DATA().hashCode();
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
        typeDesc.setXmlType(new QName("http://labvantage.com/xi/SEC/R5/ResultRecording/1.0", "ResultRecord"));
        ElementDesc elemField = new ElementDesc();
        elemField.setFieldName("ZSEC_DATA");
        elemField.setXmlName(new QName("", "ZSEC_DATA"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
    }
}

