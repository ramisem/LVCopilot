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

import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.BaseSECMessage;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.DataSetTransport;
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

public class EmpowerMessage
extends BaseSECMessage
implements Serializable {
    private DataSetTransport batchData;
    private DataSetTransport calibrationData;
    private DataSetTransport curveData;
    private String empowerDatabase;
    private String empowerProject;
    private PropertyListTransport metaData;
    private DataSetTransport peakData;
    private String policyNode;
    private DataSetTransport resultData;
    private DataSetTransport sampleData;
    private Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;
    private static TypeDesc typeDesc = new TypeDesc(EmpowerMessage.class, true);

    public EmpowerMessage() {
    }

    public EmpowerMessage(String error, String id, String log, int msgFlow, String status, String typeId, String version, DataSetTransport batchData, DataSetTransport calibrationData, DataSetTransport curveData, String empowerDatabase, String empowerProject, PropertyListTransport metaData, DataSetTransport peakData, String policyNode, DataSetTransport resultData, DataSetTransport sampleData) {
        super(error, id, log, msgFlow, status, typeId, version);
        this.batchData = batchData;
        this.calibrationData = calibrationData;
        this.curveData = curveData;
        this.empowerDatabase = empowerDatabase;
        this.empowerProject = empowerProject;
        this.metaData = metaData;
        this.peakData = peakData;
        this.policyNode = policyNode;
        this.resultData = resultData;
        this.sampleData = sampleData;
    }

    public DataSetTransport getBatchData() {
        return this.batchData;
    }

    public void setBatchData(DataSetTransport batchData) {
        this.batchData = batchData;
    }

    public DataSetTransport getCalibrationData() {
        return this.calibrationData;
    }

    public void setCalibrationData(DataSetTransport calibrationData) {
        this.calibrationData = calibrationData;
    }

    public DataSetTransport getCurveData() {
        return this.curveData;
    }

    public void setCurveData(DataSetTransport curveData) {
        this.curveData = curveData;
    }

    public String getEmpowerDatabase() {
        return this.empowerDatabase;
    }

    public void setEmpowerDatabase(String empowerDatabase) {
        this.empowerDatabase = empowerDatabase;
    }

    public String getEmpowerProject() {
        return this.empowerProject;
    }

    public void setEmpowerProject(String empowerProject) {
        this.empowerProject = empowerProject;
    }

    public PropertyListTransport getMetaData() {
        return this.metaData;
    }

    public void setMetaData(PropertyListTransport metaData) {
        this.metaData = metaData;
    }

    public DataSetTransport getPeakData() {
        return this.peakData;
    }

    public void setPeakData(DataSetTransport peakData) {
        this.peakData = peakData;
    }

    public String getPolicyNode() {
        return this.policyNode;
    }

    public void setPolicyNode(String policyNode) {
        this.policyNode = policyNode;
    }

    public DataSetTransport getResultData() {
        return this.resultData;
    }

    public void setResultData(DataSetTransport resultData) {
        this.resultData = resultData;
    }

    public DataSetTransport getSampleData() {
        return this.sampleData;
    }

    public void setSampleData(DataSetTransport sampleData) {
        this.sampleData = sampleData;
    }

    @Override
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof EmpowerMessage)) {
            return false;
        }
        EmpowerMessage other = (EmpowerMessage)obj;
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
        boolean _equals = super.equals(obj) && (this.batchData == null && other.getBatchData() == null || this.batchData != null && this.batchData.equals(other.getBatchData())) && (this.calibrationData == null && other.getCalibrationData() == null || this.calibrationData != null && this.calibrationData.equals(other.getCalibrationData())) && (this.curveData == null && other.getCurveData() == null || this.curveData != null && this.curveData.equals(other.getCurveData())) && (this.empowerDatabase == null && other.getEmpowerDatabase() == null || this.empowerDatabase != null && this.empowerDatabase.equals(other.getEmpowerDatabase())) && (this.empowerProject == null && other.getEmpowerProject() == null || this.empowerProject != null && this.empowerProject.equals(other.getEmpowerProject())) && (this.metaData == null && other.getMetaData() == null || this.metaData != null && this.metaData.equals(other.getMetaData())) && (this.peakData == null && other.getPeakData() == null || this.peakData != null && this.peakData.equals(other.getPeakData())) && (this.policyNode == null && other.getPolicyNode() == null || this.policyNode != null && this.policyNode.equals(other.getPolicyNode())) && (this.resultData == null && other.getResultData() == null || this.resultData != null && this.resultData.equals(other.getResultData())) && (this.sampleData == null && other.getSampleData() == null || this.sampleData != null && this.sampleData.equals(other.getSampleData()));
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
        if (this.getBatchData() != null) {
            _hashCode += this.getBatchData().hashCode();
        }
        if (this.getCalibrationData() != null) {
            _hashCode += this.getCalibrationData().hashCode();
        }
        if (this.getCurveData() != null) {
            _hashCode += this.getCurveData().hashCode();
        }
        if (this.getEmpowerDatabase() != null) {
            _hashCode += this.getEmpowerDatabase().hashCode();
        }
        if (this.getEmpowerProject() != null) {
            _hashCode += this.getEmpowerProject().hashCode();
        }
        if (this.getMetaData() != null) {
            _hashCode += this.getMetaData().hashCode();
        }
        if (this.getPeakData() != null) {
            _hashCode += this.getPeakData().hashCode();
        }
        if (this.getPolicyNode() != null) {
            _hashCode += this.getPolicyNode().hashCode();
        }
        if (this.getResultData() != null) {
            _hashCode += this.getResultData().hashCode();
        }
        if (this.getSampleData() != null) {
            _hashCode += this.getSampleData().hashCode();
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
        typeDesc.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "EmpowerMessage"));
        ElementDesc elemField = new ElementDesc();
        elemField.setFieldName("batchData");
        elemField.setXmlName(new QName("", "batchData"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "DataSetTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("calibrationData");
        elemField.setXmlName(new QName("", "calibrationData"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "DataSetTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("curveData");
        elemField.setXmlName(new QName("", "curveData"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "DataSetTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("empowerDatabase");
        elemField.setXmlName(new QName("", "empowerDatabase"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("empowerProject");
        elemField.setXmlName(new QName("", "empowerProject"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("metaData");
        elemField.setXmlName(new QName("", "metaData"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyListTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("peakData");
        elemField.setXmlName(new QName("", "peakData"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "DataSetTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("policyNode");
        elemField.setXmlName(new QName("", "policyNode"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("resultData");
        elemField.setXmlName(new QName("", "resultData"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "DataSetTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("sampleData");
        elemField.setXmlName(new QName("", "sampleData"));
        elemField.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "DataSetTransport"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
    }
}

