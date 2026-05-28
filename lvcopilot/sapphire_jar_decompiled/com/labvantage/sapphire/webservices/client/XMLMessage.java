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

import com.labvantage.sapphire.webservices.client.BaseSECMessage;
import java.io.Serializable;
import javax.xml.namespace.QName;
import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.ser.BeanDeserializer;
import org.apache.axis.encoding.ser.BeanSerializer;

public class XMLMessage
extends BaseSECMessage
implements Serializable {
    private String xml;
    private Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;
    private static TypeDesc typeDesc = new TypeDesc(XMLMessage.class, true);

    public XMLMessage() {
    }

    public XMLMessage(String error, String id, String log, int msgFlow, String status, String typeId, String version, String xml) {
        super(error, id, log, msgFlow, status, typeId, version);
        this.xml = xml;
    }

    public String getXml() {
        return this.xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    @Override
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof XMLMessage)) {
            return false;
        }
        XMLMessage other = (XMLMessage)obj;
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
        boolean _equals = super.equals(obj) && (this.xml == null && other.getXml() == null || this.xml != null && this.xml.equals(other.getXml()));
        this.__equalsCalc = null;
        return _equals;
    }

    @Override
    public synchronized int hashCode() {
        if (this.__hashCodeCalc) {
            return 0;
        }
        this.__hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (this.getXml() != null) {
            _hashCode += this.getXml().hashCode();
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
        typeDesc.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "XMLMessage"));
        ElementDesc elemField = new ElementDesc();
        elemField.setFieldName("xml");
        elemField.setXmlName(new QName("", "xml"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
    }
}

