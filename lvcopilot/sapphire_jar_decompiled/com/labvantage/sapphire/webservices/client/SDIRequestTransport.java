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

public class SDIRequestTransport
implements Serializable {
    private String datalockoption = "";
    private String datasetlist = "";
    private boolean extendedAudit = false;
    private boolean extendedDataTypes = false;
    private String keyid1List = "";
    private String keyid2List = "";
    private String keyid3List = "";
    private String lockoption = "";
    private boolean overrideLoadFlag = false;
    private String paramlistidlist = "";
    private String paramlistversionidlist = "";
    private String primarylockoption = "";
    private boolean propsmatch = false;
    private String queryfrom = "";
    private String queryid = "";
    private String queryorderby = "";
    private String[] queryparams = new String[0];
    private String querywhere = "";
    private boolean retainrsetid = false;
    private boolean retrieve = true;
    private boolean retrieveMappedKey = true;
    private int retrievelimit = 0;
    private String rsetid = "";
    private String sdcid = "";
    private String[] sdirequestitems = new String[0];
    private boolean showtemplates = false;
    private String variantidlist = "";
    private String versionstatus = "";
    private Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;
    private static TypeDesc typeDesc = new TypeDesc(SDIRequestTransport.class, true);

    public SDIRequestTransport() {
    }

    public SDIRequestTransport(String datalockoption, String datasetlist, boolean extendedAudit, boolean extendedDataTypes, String keyid1List, String keyid2List, String keyid3List, String lockoption, boolean overrideLoadFlag, String paramlistidlist, String paramlistversionidlist, String primarylockoption, boolean propsmatch, String queryfrom, String queryid, String queryorderby, String[] queryparams, String querywhere, boolean retainrsetid, boolean retrieve, boolean retrieveMappedKey, int retrievelimit, String rsetid, String sdcid, String[] sdirequestitems, boolean showtemplates, String variantidlist, String versionstatus) {
        this.datalockoption = datalockoption;
        this.datasetlist = datasetlist;
        this.extendedAudit = extendedAudit;
        this.extendedDataTypes = extendedDataTypes;
        this.keyid1List = keyid1List;
        this.keyid2List = keyid2List;
        this.keyid3List = keyid3List;
        this.lockoption = lockoption;
        this.overrideLoadFlag = overrideLoadFlag;
        this.paramlistidlist = paramlistidlist;
        this.paramlistversionidlist = paramlistversionidlist;
        this.primarylockoption = primarylockoption;
        this.propsmatch = propsmatch;
        this.queryfrom = queryfrom;
        this.queryid = queryid;
        this.queryorderby = queryorderby;
        this.queryparams = queryparams;
        this.querywhere = querywhere;
        this.retainrsetid = retainrsetid;
        this.retrieve = retrieve;
        this.retrieveMappedKey = retrieveMappedKey;
        this.retrievelimit = retrievelimit;
        this.rsetid = rsetid;
        this.sdcid = sdcid;
        this.sdirequestitems = sdirequestitems;
        this.showtemplates = showtemplates;
        this.variantidlist = variantidlist;
        this.versionstatus = versionstatus;
    }

    public String getDatalockoption() {
        return this.datalockoption;
    }

    public void setDatalockoption(String datalockoption) {
        this.datalockoption = datalockoption;
    }

    public String getDatasetlist() {
        return this.datasetlist;
    }

    public void setDatasetlist(String datasetlist) {
        this.datasetlist = datasetlist;
    }

    public boolean isExtendedAudit() {
        return this.extendedAudit;
    }

    public void setExtendedAudit(boolean extendedAudit) {
        this.extendedAudit = extendedAudit;
    }

    public boolean isExtendedDataTypes() {
        return this.extendedDataTypes;
    }

    public void setExtendedDataTypes(boolean extendedDataTypes) {
        this.extendedDataTypes = extendedDataTypes;
    }

    public String getKeyid1List() {
        return this.keyid1List;
    }

    public void setKeyid1List(String keyid1List) {
        this.keyid1List = keyid1List;
    }

    public String getKeyid2List() {
        return this.keyid2List;
    }

    public void setKeyid2List(String keyid2List) {
        this.keyid2List = keyid2List;
    }

    public String getKeyid3List() {
        return this.keyid3List;
    }

    public void setKeyid3List(String keyid3List) {
        this.keyid3List = keyid3List;
    }

    public String getLockoption() {
        return this.lockoption;
    }

    public void setLockoption(String lockoption) {
        this.lockoption = lockoption;
    }

    public boolean isOverrideLoadFlag() {
        return this.overrideLoadFlag;
    }

    public void setOverrideLoadFlag(boolean overrideLoadFlag) {
        this.overrideLoadFlag = overrideLoadFlag;
    }

    public String getParamlistidlist() {
        return this.paramlistidlist;
    }

    public void setParamlistidlist(String paramlistidlist) {
        this.paramlistidlist = paramlistidlist;
    }

    public String getParamlistversionidlist() {
        return this.paramlistversionidlist;
    }

    public void setParamlistversionidlist(String paramlistversionidlist) {
        this.paramlistversionidlist = paramlistversionidlist;
    }

    public String getPrimarylockoption() {
        return this.primarylockoption;
    }

    public void setPrimarylockoption(String primarylockoption) {
        this.primarylockoption = primarylockoption;
    }

    public boolean isPropsmatch() {
        return this.propsmatch;
    }

    public void setPropsmatch(boolean propsmatch) {
        this.propsmatch = propsmatch;
    }

    public String getQueryfrom() {
        return this.queryfrom;
    }

    public void setQueryfrom(String queryfrom) {
        this.queryfrom = queryfrom;
    }

    public String getQueryid() {
        return this.queryid;
    }

    public void setQueryid(String queryid) {
        this.queryid = queryid;
    }

    public String getQueryorderby() {
        return this.queryorderby;
    }

    public void setQueryorderby(String queryorderby) {
        this.queryorderby = queryorderby;
    }

    public String[] getQueryparams() {
        return this.queryparams;
    }

    public void setQueryparams(String[] queryparams) {
        this.queryparams = queryparams;
    }

    public String getQuerywhere() {
        return this.querywhere;
    }

    public void setQuerywhere(String querywhere) {
        this.querywhere = querywhere;
    }

    public boolean isRetainrsetid() {
        return this.retainrsetid;
    }

    public void setRetainrsetid(boolean retainrsetid) {
        this.retainrsetid = retainrsetid;
    }

    public boolean isRetrieve() {
        return this.retrieve;
    }

    public void setRetrieve(boolean retrieve) {
        this.retrieve = retrieve;
    }

    public boolean isRetrieveMappedKey() {
        return this.retrieveMappedKey;
    }

    public void setRetrieveMappedKey(boolean retrieveMappedKey) {
        this.retrieveMappedKey = retrieveMappedKey;
    }

    public int getRetrievelimit() {
        return this.retrievelimit;
    }

    public void setRetrievelimit(int retrievelimit) {
        this.retrievelimit = retrievelimit;
    }

    public String getRsetid() {
        return this.rsetid;
    }

    public void setRsetid(String rsetid) {
        this.rsetid = rsetid;
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public String[] getSdirequestitems() {
        return this.sdirequestitems;
    }

    public void setSdirequestitems(String[] sdirequestitems) {
        this.sdirequestitems = sdirequestitems;
    }

    public boolean isShowtemplates() {
        return this.showtemplates;
    }

    public void setShowtemplates(boolean showtemplates) {
        this.showtemplates = showtemplates;
    }

    public String getVariantidlist() {
        return this.variantidlist;
    }

    public void setVariantidlist(String variantidlist) {
        this.variantidlist = variantidlist;
    }

    public String getVersionstatus() {
        return this.versionstatus;
    }

    public void setVersionstatus(String versionstatus) {
        this.versionstatus = versionstatus;
    }

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof SDIRequestTransport)) {
            return false;
        }
        SDIRequestTransport other = (SDIRequestTransport)obj;
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
        boolean _equals = (this.datalockoption == null && other.getDatalockoption() == null || this.datalockoption != null && this.datalockoption.equals(other.getDatalockoption())) && (this.datasetlist == null && other.getDatasetlist() == null || this.datasetlist != null && this.datasetlist.equals(other.getDatasetlist())) && this.extendedAudit == other.isExtendedAudit() && this.extendedDataTypes == other.isExtendedDataTypes() && (this.keyid1List == null && other.getKeyid1List() == null || this.keyid1List != null && this.keyid1List.equals(other.getKeyid1List())) && (this.keyid2List == null && other.getKeyid2List() == null || this.keyid2List != null && this.keyid2List.equals(other.getKeyid2List())) && (this.keyid3List == null && other.getKeyid3List() == null || this.keyid3List != null && this.keyid3List.equals(other.getKeyid3List())) && (this.lockoption == null && other.getLockoption() == null || this.lockoption != null && this.lockoption.equals(other.getLockoption())) && this.overrideLoadFlag == other.isOverrideLoadFlag() && (this.paramlistidlist == null && other.getParamlistidlist() == null || this.paramlistidlist != null && this.paramlistidlist.equals(other.getParamlistidlist())) && (this.paramlistversionidlist == null && other.getParamlistversionidlist() == null || this.paramlistversionidlist != null && this.paramlistversionidlist.equals(other.getParamlistversionidlist())) && (this.primarylockoption == null && other.getPrimarylockoption() == null || this.primarylockoption != null && this.primarylockoption.equals(other.getPrimarylockoption())) && this.propsmatch == other.isPropsmatch() && (this.queryfrom == null && other.getQueryfrom() == null || this.queryfrom != null && this.queryfrom.equals(other.getQueryfrom())) && (this.queryid == null && other.getQueryid() == null || this.queryid != null && this.queryid.equals(other.getQueryid())) && (this.queryorderby == null && other.getQueryorderby() == null || this.queryorderby != null && this.queryorderby.equals(other.getQueryorderby())) && (this.queryparams == null && other.getQueryparams() == null || this.queryparams != null && Arrays.equals(this.queryparams, other.getQueryparams())) && (this.querywhere == null && other.getQuerywhere() == null || this.querywhere != null && this.querywhere.equals(other.getQuerywhere())) && this.retainrsetid == other.isRetainrsetid() && this.retrieve == other.isRetrieve() && this.retrieveMappedKey == other.isRetrieveMappedKey() && this.retrievelimit == other.getRetrievelimit() && (this.rsetid == null && other.getRsetid() == null || this.rsetid != null && this.rsetid.equals(other.getRsetid())) && (this.sdcid == null && other.getSdcid() == null || this.sdcid != null && this.sdcid.equals(other.getSdcid())) && (this.sdirequestitems == null && other.getSdirequestitems() == null || this.sdirequestitems != null && Arrays.equals(this.sdirequestitems, other.getSdirequestitems())) && this.showtemplates == other.isShowtemplates() && (this.variantidlist == null && other.getVariantidlist() == null || this.variantidlist != null && this.variantidlist.equals(other.getVariantidlist())) && (this.versionstatus == null && other.getVersionstatus() == null || this.versionstatus != null && this.versionstatus.equals(other.getVersionstatus()));
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
        if (this.getDatalockoption() != null) {
            _hashCode += this.getDatalockoption().hashCode();
        }
        if (this.getDatasetlist() != null) {
            _hashCode += this.getDatasetlist().hashCode();
        }
        _hashCode += (this.isExtendedAudit() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (this.isExtendedDataTypes() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (this.getKeyid1List() != null) {
            _hashCode += this.getKeyid1List().hashCode();
        }
        if (this.getKeyid2List() != null) {
            _hashCode += this.getKeyid2List().hashCode();
        }
        if (this.getKeyid3List() != null) {
            _hashCode += this.getKeyid3List().hashCode();
        }
        if (this.getLockoption() != null) {
            _hashCode += this.getLockoption().hashCode();
        }
        _hashCode += (this.isOverrideLoadFlag() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (this.getParamlistidlist() != null) {
            _hashCode += this.getParamlistidlist().hashCode();
        }
        if (this.getParamlistversionidlist() != null) {
            _hashCode += this.getParamlistversionidlist().hashCode();
        }
        if (this.getPrimarylockoption() != null) {
            _hashCode += this.getPrimarylockoption().hashCode();
        }
        _hashCode += (this.isPropsmatch() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (this.getQueryfrom() != null) {
            _hashCode += this.getQueryfrom().hashCode();
        }
        if (this.getQueryid() != null) {
            _hashCode += this.getQueryid().hashCode();
        }
        if (this.getQueryorderby() != null) {
            _hashCode += this.getQueryorderby().hashCode();
        }
        if (this.getQueryparams() != null) {
            for (i = 0; i < Array.getLength(this.getQueryparams()); ++i) {
                obj = Array.get(this.getQueryparams(), i);
                if (obj == null || obj.getClass().isArray()) continue;
                _hashCode += obj.hashCode();
            }
        }
        if (this.getQuerywhere() != null) {
            _hashCode += this.getQuerywhere().hashCode();
        }
        _hashCode += (this.isRetainrsetid() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (this.isRetrieve() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (this.isRetrieveMappedKey() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += this.getRetrievelimit();
        if (this.getRsetid() != null) {
            _hashCode += this.getRsetid().hashCode();
        }
        if (this.getSdcid() != null) {
            _hashCode += this.getSdcid().hashCode();
        }
        if (this.getSdirequestitems() != null) {
            for (i = 0; i < Array.getLength(this.getSdirequestitems()); ++i) {
                obj = Array.get(this.getSdirequestitems(), i);
                if (obj == null || obj.getClass().isArray()) continue;
                _hashCode += obj.hashCode();
            }
        }
        _hashCode += (this.isShowtemplates() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (this.getVariantidlist() != null) {
            _hashCode += this.getVariantidlist().hashCode();
        }
        if (this.getVersionstatus() != null) {
            _hashCode += this.getVersionstatus().hashCode();
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
        typeDesc.setXmlType(new QName("urn:com.labvantage.sapphire.webservices", "SDIRequestTransport"));
        ElementDesc elemField = new ElementDesc();
        elemField.setFieldName("datalockoption");
        elemField.setXmlName(new QName("", "datalockoption"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("datasetlist");
        elemField.setXmlName(new QName("", "datasetlist"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("extendedAudit");
        elemField.setXmlName(new QName("", "extendedAudit"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("extendedDataTypes");
        elemField.setXmlName(new QName("", "extendedDataTypes"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("keyid1List");
        elemField.setXmlName(new QName("", "keyid1list"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("keyid2List");
        elemField.setXmlName(new QName("", "keyid2list"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("keyid3List");
        elemField.setXmlName(new QName("", "keyid3list"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("lockoption");
        elemField.setXmlName(new QName("", "lockoption"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("overrideLoadFlag");
        elemField.setXmlName(new QName("", "overrideLoadFlag"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("paramlistidlist");
        elemField.setXmlName(new QName("", "paramlistidlist"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("paramlistversionidlist");
        elemField.setXmlName(new QName("", "paramlistversionidlist"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("primarylockoption");
        elemField.setXmlName(new QName("", "primarylockoption"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("propsmatch");
        elemField.setXmlName(new QName("", "propsmatch"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("queryfrom");
        elemField.setXmlName(new QName("", "queryfrom"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("queryid");
        elemField.setXmlName(new QName("", "queryid"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("queryorderby");
        elemField.setXmlName(new QName("", "queryorderby"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("queryparams");
        elemField.setXmlName(new QName("", "queryparams"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("querywhere");
        elemField.setXmlName(new QName("", "querywhere"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("retainrsetid");
        elemField.setXmlName(new QName("", "retainrsetid"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("retrieve");
        elemField.setXmlName(new QName("", "retrieve"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("retrieveMappedKey");
        elemField.setXmlName(new QName("", "retrieveMappedKey"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("retrievelimit");
        elemField.setXmlName(new QName("", "retrievelimit"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("rsetid");
        elemField.setXmlName(new QName("", "rsetid"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("sdcid");
        elemField.setXmlName(new QName("", "sdcid"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("sdirequestitems");
        elemField.setXmlName(new QName("", "sdirequestitems"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("showtemplates");
        elemField.setXmlName(new QName("", "showtemplates"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("variantidlist");
        elemField.setXmlName(new QName("", "variantidlist"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
        elemField = new ElementDesc();
        elemField.setFieldName("versionstatus");
        elemField.setXmlName(new QName("", "versionstatus"));
        elemField.setXmlType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc((FieldDesc)elemField);
    }
}

