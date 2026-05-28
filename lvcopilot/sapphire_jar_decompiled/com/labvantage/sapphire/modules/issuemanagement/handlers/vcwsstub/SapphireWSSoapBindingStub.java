/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.rpc.Service
 *  javax.xml.rpc.encoding.SerializerFactory
 *  org.apache.axis.AxisFault
 *  org.apache.axis.NoEndPointException
 *  org.apache.axis.client.Call
 *  org.apache.axis.client.Service
 *  org.apache.axis.client.Stub
 *  org.apache.axis.constants.Style
 *  org.apache.axis.constants.Use
 *  org.apache.axis.description.OperationDesc
 *  org.apache.axis.description.ParameterDesc
 *  org.apache.axis.encoding.DeserializerFactory
 *  org.apache.axis.encoding.SerializerFactory
 *  org.apache.axis.encoding.XMLType
 *  org.apache.axis.encoding.ser.ArrayDeserializerFactory
 *  org.apache.axis.encoding.ser.ArraySerializerFactory
 *  org.apache.axis.encoding.ser.BeanDeserializerFactory
 *  org.apache.axis.encoding.ser.BeanSerializerFactory
 *  org.apache.axis.encoding.ser.EnumDeserializerFactory
 *  org.apache.axis.encoding.ser.EnumSerializerFactory
 *  org.apache.axis.encoding.ser.SimpleDeserializerFactory
 *  org.apache.axis.encoding.ser.SimpleListDeserializerFactory
 *  org.apache.axis.encoding.ser.SimpleListSerializerFactory
 *  org.apache.axis.encoding.ser.SimpleSerializerFactory
 *  org.apache.axis.soap.SOAPConstants
 *  org.apache.axis.utils.JavaUtils
 */
package com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub;

import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.ActionBlockTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.AttachmentTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.BaseSECMessage;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.DataSetColumnTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.DataSetRowTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.DataSetTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.EmpowerMessage;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.PropertyListCollectionTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.PropertyListTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.PropertyValueTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.SDIDataTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.SDIRequestTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.SapphireWS_PortType;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.XMLMessage;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Vector;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import org.apache.axis.AxisFault;
import org.apache.axis.NoEndPointException;
import org.apache.axis.client.Call;
import org.apache.axis.client.Stub;
import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.encoding.DeserializerFactory;
import org.apache.axis.encoding.SerializerFactory;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.EnumDeserializerFactory;
import org.apache.axis.encoding.ser.EnumSerializerFactory;
import org.apache.axis.encoding.ser.SimpleDeserializerFactory;
import org.apache.axis.encoding.ser.SimpleListDeserializerFactory;
import org.apache.axis.encoding.ser.SimpleListSerializerFactory;
import org.apache.axis.encoding.ser.SimpleSerializerFactory;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.utils.JavaUtils;

public class SapphireWSSoapBindingStub
extends Stub
implements SapphireWS_PortType {
    private Vector cachedSerClasses = new Vector();
    private Vector cachedSerQNames = new Vector();
    private Vector cachedSerFactories = new Vector();
    private Vector cachedDeserFactories = new Vector();
    static OperationDesc[] _operations = new OperationDesc[30];

    private static void _initOperationDesc1() {
        OperationDesc oper = new OperationDesc();
        oper.setName("getDatabaseList");
        oper.setReturnType(new QName("http://vmvctest1us.lims.com/vcstg/services/SapphireWS", "ArrayOf_soapenc_string"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "getDatabaseListReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[0] = oper;
        oper = new OperationDesc();
        oper.setName("translateTable");
        ParameterDesc param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "languageid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "propertyListXML"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "translateTableReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[1] = oper;
        oper = new OperationDesc();
        oper.setName("translateTable");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "languageid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "propertyList"), 1, new QName("urn:com.labvantage.sapphire.webservices", "PropertyListTransport"), PropertyListTransport.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyListTransport"));
        oper.setReturnClass(PropertyListTransport.class);
        oper.setReturnQName(new QName("", "translateTableReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[2] = oper;
        oper = new OperationDesc();
        oper.setName("addSDIAttachment");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sdcid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "keyid1"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "keyid2"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "keyid3"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "attachment"), 1, new QName("urn:com.labvantage.sapphire.webservices", "AttachmentTransport"), AttachmentTransport.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(XMLType.AXIS_VOID);
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[3] = oper;
        oper = new OperationDesc();
        oper.setName("execSQL");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sqlCode"), 1, new QName("http://www.w3.org/2001/XMLSchema", "int"), Integer.TYPE, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "bindVars"), 1, new QName("http://vmvctest1us.lims.com/vcstg/services/SapphireWS", "ArrayOf_xsd_anyType"), Object[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(Integer.TYPE);
        oper.setReturnQName(new QName("", "execSQLReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[4] = oper;
        oper = new OperationDesc();
        oper.setName("execSQL");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sqlCode"), 1, new QName("http://www.w3.org/2001/XMLSchema", "int"), Integer.TYPE, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(Integer.TYPE);
        oper.setReturnQName(new QName("", "execSQLReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[5] = oper;
        oper = new OperationDesc();
        oper.setName("execSQL");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sql"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(Integer.TYPE);
        oper.setReturnQName(new QName("", "execSQLReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[6] = oper;
        oper = new OperationDesc();
        oper.setName("getSDCProperties");
        param = new ParameterDesc(new QName("", "connectionId"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sdcid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyListTransport"));
        oper.setReturnClass(PropertyListTransport.class);
        oper.setReturnQName(new QName("", "getSDCPropertiesReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[7] = oper;
        oper = new OperationDesc();
        oper.setName("getSDIAttachment");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sdcid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "keyid1"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "keyid2"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "keyid3"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "attachmentNum"), 1, new QName("http://www.w3.org/2001/XMLSchema", "int"), Integer.TYPE, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "zip"), 1, new QName("http://www.w3.org/2001/XMLSchema", "boolean"), Boolean.TYPE, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("urn:com.labvantage.sapphire.webservices", "AttachmentTransport"));
        oper.setReturnClass(AttachmentTransport.class);
        oper.setReturnQName(new QName("", "getSDIAttachmentReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[8] = oper;
        oper = new OperationDesc();
        oper.setName("processAction");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "actionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "actionversionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "propertyList"), 1, new QName("urn:com.labvantage.sapphire.webservices", "PropertyListTransport"), PropertyListTransport.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("urn:com.labvantage.sapphire.webservices", "PropertyListTransport"));
        oper.setReturnClass(PropertyListTransport.class);
        oper.setReturnQName(new QName("", "processActionReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[9] = oper;
    }

    private static void _initOperationDesc2() {
        OperationDesc oper = new OperationDesc();
        oper.setName("processAction");
        ParameterDesc param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "actionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "actionversionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "propertyListXML"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "processActionReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[10] = oper;
        oper = new OperationDesc();
        oper.setName("processMessage");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "message"), 1, new QName("urn:com.labvantage.sapphire.webservices", "BaseSECMessage"), BaseSECMessage.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "processingMode"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("urn:com.labvantage.sapphire.webservices", "BaseSECMessage"));
        oper.setReturnClass(BaseSECMessage.class);
        oper.setReturnQName(new QName("", "processMessageReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[11] = oper;
        oper = new OperationDesc();
        oper.setName("processMessage");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "id"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "typeId"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "message"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "processingMode"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "processMessageReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[12] = oper;
        oper = new OperationDesc();
        oper.setName("getSqlDataSet");
        param = new ParameterDesc(new QName("", "connectionId"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sqlCode"), 1, new QName("http://www.w3.org/2001/XMLSchema", "int"), Integer.TYPE, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "bindVars"), 1, new QName("http://vmvctest1us.lims.com/vcstg/services/SapphireWS", "ArrayOf_xsd_anyType"), Object[].class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "returnClobs"), 1, new QName("http://www.w3.org/2001/XMLSchema", "boolean"), Boolean.TYPE, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("urn:com.labvantage.sapphire.webservices", "DataSetTransport"));
        oper.setReturnClass(DataSetTransport.class);
        oper.setReturnQName(new QName("", "getSqlDataSetReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[13] = oper;
        oper = new OperationDesc();
        oper.setName("getSqlDataSet");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sqlCode"), 1, new QName("http://www.w3.org/2001/XMLSchema", "int"), Integer.TYPE, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "getSqlDataSetReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[14] = oper;
        oper = new OperationDesc();
        oper.setName("getSqlDataSet");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sqlCode"), 1, new QName("http://www.w3.org/2001/XMLSchema", "int"), Integer.TYPE, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "bindVars"), 1, new QName("http://vmvctest1us.lims.com/vcstg/services/SapphireWS", "ArrayOf_xsd_anyType"), Object[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "getSqlDataSetReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[15] = oper;
        oper = new OperationDesc();
        oper.setName("getSqlDataSet");
        param = new ParameterDesc(new QName("", "connectionId"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sql"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "returnClobs"), 1, new QName("http://www.w3.org/2001/XMLSchema", "boolean"), Boolean.TYPE, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("urn:com.labvantage.sapphire.webservices", "DataSetTransport"));
        oper.setReturnClass(DataSetTransport.class);
        oper.setReturnQName(new QName("", "getSqlDataSetReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[16] = oper;
        oper = new OperationDesc();
        oper.setName("getSqlDataSet");
        param = new ParameterDesc(new QName("", "connectionId"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sqlCode"), 1, new QName("http://www.w3.org/2001/XMLSchema", "int"), Integer.TYPE, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "returnClobs"), 1, new QName("http://www.w3.org/2001/XMLSchema", "boolean"), Boolean.TYPE, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("urn:com.labvantage.sapphire.webservices", "DataSetTransport"));
        oper.setReturnClass(DataSetTransport.class);
        oper.setReturnQName(new QName("", "getSqlDataSetReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[17] = oper;
        oper = new OperationDesc();
        oper.setName("getSqlDataSet");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sql"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "getSqlDataSetReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[18] = oper;
        oper = new OperationDesc();
        oper.setName("processApplicationCommand");
        param = new ParameterDesc(new QName("", "command"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "paramsXML"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "processApplicationCommandReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[19] = oper;
    }

    private static void _initOperationDesc3() {
        OperationDesc oper = new OperationDesc();
        oper.setName("getSDIData");
        ParameterDesc param = new ParameterDesc(new QName("", "connectionId"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sdireqbean"), 1, new QName("urn:com.labvantage.sapphire.webservices", "SDIRequestTransport"), SDIRequestTransport.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("urn:com.labvantage.sapphire.webservices", "SDIDataTransport"));
        oper.setReturnClass(SDIDataTransport.class);
        oper.setReturnQName(new QName("", "getSDIDataReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[20] = oper;
        oper = new OperationDesc();
        oper.setName("processActionBlock");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "block"), 1, new QName("urn:com.labvantage.sapphire.webservices", "ActionBlockTransport"), ActionBlockTransport.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("urn:com.labvantage.sapphire.webservices", "ActionBlockTransport"));
        oper.setReturnClass(ActionBlockTransport.class);
        oper.setReturnQName(new QName("", "processActionBlockReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[21] = oper;
        oper = new OperationDesc();
        oper.setName("editSDIAttachment");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sdcid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "keyid1"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "keyid2"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "keyid3"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "attachmentNum"), 1, new QName("http://www.w3.org/2001/XMLSchema", "int"), Integer.TYPE, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "attachment"), 1, new QName("urn:com.labvantage.sapphire.webservices", "AttachmentTransport"), AttachmentTransport.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(XMLType.AXIS_VOID);
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[22] = oper;
        oper = new OperationDesc();
        oper.setName("deleteSDIAttachment");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sdcid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "keyid1"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "keyid2"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "keyid3"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "attachmentNum"), 1, new QName("http://www.w3.org/2001/XMLSchema", "int"), Integer.TYPE, false, false);
        oper.addParameter(param);
        oper.setReturnType(XMLType.AXIS_VOID);
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[23] = oper;
        oper = new OperationDesc();
        oper.setName("getPublicKey");
        oper.setReturnType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "getPublicKeyReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[24] = oper;
        oper = new OperationDesc();
        oper.setName("getVersion");
        oper.setReturnType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "getVersionReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[25] = oper;
        oper = new OperationDesc();
        oper.setName("getSequence");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sdcid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "sequenceid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "startsequencenumber"), 1, new QName("http://www.w3.org/2001/XMLSchema", "int"), Integer.TYPE, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "incrementby"), 1, new QName("http://www.w3.org/2001/XMLSchema", "int"), Integer.TYPE, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(Integer.TYPE);
        oper.setReturnQName(new QName("", "getSequenceReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[26] = oper;
        oper = new OperationDesc();
        oper.setName("getConnectionId");
        param = new ParameterDesc(new QName("", "databaseid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "userid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new ParameterDesc(new QName("", "password"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "getConnectionIdReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[27] = oper;
        oper = new OperationDesc();
        oper.setName("checkConnection");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(Boolean.TYPE);
        oper.setReturnQName(new QName("", "checkConnectionReturn"));
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[28] = oper;
        oper = new OperationDesc();
        oper.setName("clearConnection");
        param = new ParameterDesc(new QName("", "connectionid"), 1, new QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(XMLType.AXIS_VOID);
        oper.setStyle(Style.RPC);
        oper.setUse(Use.ENCODED);
        SapphireWSSoapBindingStub._operations[29] = oper;
    }

    public SapphireWSSoapBindingStub() throws AxisFault {
        this(null);
    }

    public SapphireWSSoapBindingStub(URL endpointURL, Service service) throws AxisFault {
        this(service);
        this.cachedEndpoint = endpointURL;
    }

    public SapphireWSSoapBindingStub(Service service) throws AxisFault {
        this.service = service == null ? new org.apache.axis.client.Service() : service;
        ((org.apache.axis.client.Service)this.service).setTypeMappingVersion("1.2");
        Class<BeanSerializerFactory> beansf = BeanSerializerFactory.class;
        Class<BeanDeserializerFactory> beandf = BeanDeserializerFactory.class;
        Class<EnumSerializerFactory> enumsf = EnumSerializerFactory.class;
        Class<EnumDeserializerFactory> enumdf = EnumDeserializerFactory.class;
        Class<ArraySerializerFactory> arraysf = ArraySerializerFactory.class;
        Class<ArrayDeserializerFactory> arraydf = ArrayDeserializerFactory.class;
        Class<SimpleSerializerFactory> simplesf = SimpleSerializerFactory.class;
        Class<SimpleDeserializerFactory> simpledf = SimpleDeserializerFactory.class;
        Class<SimpleListSerializerFactory> simplelistsf = SimpleListSerializerFactory.class;
        Class<SimpleListDeserializerFactory> simplelistdf = SimpleListDeserializerFactory.class;
        QName qName = new QName("http://vmvctest1us.lims.com/vcstg/services/SapphireWS", "ArrayOf_soapenc_string");
        this.cachedSerQNames.add(qName);
        Class cls = String[].class;
        this.cachedSerClasses.add(cls);
        qName = new QName("http://schemas.xmlsoap.org/soap/encoding/", "string");
        QName qName2 = null;
        this.cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
        this.cachedDeserFactories.add(new ArrayDeserializerFactory());
        qName = new QName("http://vmvctest1us.lims.com/vcstg/services/SapphireWS", "ArrayOf_tns1_DataSetColumnTransport");
        this.cachedSerQNames.add(qName);
        cls = DataSetColumnTransport[].class;
        this.cachedSerClasses.add(cls);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "DataSetColumnTransport");
        qName2 = null;
        this.cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
        this.cachedDeserFactories.add(new ArrayDeserializerFactory());
        qName = new QName("http://vmvctest1us.lims.com/vcstg/services/SapphireWS", "ArrayOf_tns1_DataSetRowTransport");
        this.cachedSerQNames.add(qName);
        cls = DataSetRowTransport[].class;
        this.cachedSerClasses.add(cls);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "DataSetRowTransport");
        qName2 = null;
        this.cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
        this.cachedDeserFactories.add(new ArrayDeserializerFactory());
        qName = new QName("http://vmvctest1us.lims.com/vcstg/services/SapphireWS", "ArrayOf_tns1_DataSetTransport");
        this.cachedSerQNames.add(qName);
        cls = DataSetTransport[].class;
        this.cachedSerClasses.add(cls);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "DataSetTransport");
        qName2 = null;
        this.cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
        this.cachedDeserFactories.add(new ArrayDeserializerFactory());
        qName = new QName("http://vmvctest1us.lims.com/vcstg/services/SapphireWS", "ArrayOf_tns1_PropertyListTransport");
        this.cachedSerQNames.add(qName);
        cls = PropertyListTransport[].class;
        this.cachedSerClasses.add(cls);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "PropertyListTransport");
        qName2 = null;
        this.cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
        this.cachedDeserFactories.add(new ArrayDeserializerFactory());
        qName = new QName("http://vmvctest1us.lims.com/vcstg/services/SapphireWS", "ArrayOf_tns1_PropertyValueTransport");
        this.cachedSerQNames.add(qName);
        cls = PropertyValueTransport[].class;
        this.cachedSerClasses.add(cls);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "PropertyValueTransport");
        qName2 = null;
        this.cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
        this.cachedDeserFactories.add(new ArrayDeserializerFactory());
        qName = new QName("http://vmvctest1us.lims.com/vcstg/services/SapphireWS", "ArrayOf_xsd_anyType");
        this.cachedSerQNames.add(qName);
        cls = Object[].class;
        this.cachedSerClasses.add(cls);
        qName = new QName("http://www.w3.org/2001/XMLSchema", "anyType");
        qName2 = null;
        this.cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
        this.cachedDeserFactories.add(new ArrayDeserializerFactory());
        qName = new QName("urn:com.labvantage.sapphire.webservices", "ActionBlockTransport");
        this.cachedSerQNames.add(qName);
        cls = ActionBlockTransport.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "AttachmentTransport");
        this.cachedSerQNames.add(qName);
        cls = AttachmentTransport.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "BaseSECMessage");
        this.cachedSerQNames.add(qName);
        cls = BaseSECMessage.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "DataSetColumnTransport");
        this.cachedSerQNames.add(qName);
        cls = DataSetColumnTransport.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "DataSetRowTransport");
        this.cachedSerQNames.add(qName);
        cls = DataSetRowTransport.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "DataSetTransport");
        this.cachedSerQNames.add(qName);
        cls = DataSetTransport.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "EmpowerMessage");
        this.cachedSerQNames.add(qName);
        cls = EmpowerMessage.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "PropertyListCollectionTransport");
        this.cachedSerQNames.add(qName);
        cls = PropertyListCollectionTransport.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "PropertyListTransport");
        this.cachedSerQNames.add(qName);
        cls = PropertyListTransport.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "PropertyValueTransport");
        this.cachedSerQNames.add(qName);
        cls = PropertyValueTransport.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "SDIDataTransport");
        this.cachedSerQNames.add(qName);
        cls = SDIDataTransport.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "SDIRequestTransport");
        this.cachedSerQNames.add(qName);
        cls = SDIRequestTransport.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
        qName = new QName("urn:com.labvantage.sapphire.webservices", "XMLMessage");
        this.cachedSerQNames.add(qName);
        cls = XMLMessage.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Call createCall() throws RemoteException {
        try {
            Call _call = super._createCall();
            if (this.maintainSessionSet) {
                _call.setMaintainSession(this.maintainSession);
            }
            if (this.cachedUsername != null) {
                _call.setUsername(this.cachedUsername);
            }
            if (this.cachedPassword != null) {
                _call.setPassword(this.cachedPassword);
            }
            if (this.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(this.cachedEndpoint);
            }
            if (this.cachedTimeout != null) {
                _call.setTimeout(this.cachedTimeout);
            }
            if (this.cachedPortName != null) {
                _call.setPortName(this.cachedPortName);
            }
            Enumeration<Object> keys = this.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                _call.setProperty(key, this.cachedProperties.get(key));
            }
            SapphireWSSoapBindingStub sapphireWSSoapBindingStub = this;
            synchronized (sapphireWSSoapBindingStub) {
                if (this.firstCall()) {
                    _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
                    _call.setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");
                    for (int i = 0; i < this.cachedSerFactories.size(); ++i) {
                        Class df;
                        Class sf;
                        Class cls = (Class)this.cachedSerClasses.get(i);
                        QName qName = (QName)this.cachedSerQNames.get(i);
                        Object x = this.cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            sf = (Class)this.cachedSerFactories.get(i);
                            df = (Class)this.cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                            continue;
                        }
                        if (!(x instanceof javax.xml.rpc.encoding.SerializerFactory)) continue;
                        sf = (SerializerFactory)this.cachedSerFactories.get(i);
                        df = (DeserializerFactory)this.cachedDeserFactories.get(i);
                        _call.registerTypeMapping(cls, qName, (SerializerFactory)sf, (DeserializerFactory)df, false);
                    }
                }
            }
            return _call;
        }
        catch (Throwable _t) {
            throw new AxisFault("Failure trying to get the Call object", _t);
        }
    }

    @Override
    public String[] getDatabaseList() throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getDatabaseList"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[0]);
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (String[])_resp;
        }
        catch (Exception _exception) {
            return (String[])JavaUtils.convert((Object)_resp, String[].class);
        }
    }

    @Override
    public String translateTable(String connectionid, String languageid, String propertyListXML) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "translateTable"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, languageid, propertyListXML});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (String)_resp;
        }
        catch (Exception _exception) {
            return (String)JavaUtils.convert((Object)_resp, String.class);
        }
    }

    @Override
    public PropertyListTransport translateTable(String connectionid, String languageid, PropertyListTransport propertyList) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "translateTable"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, languageid, propertyList});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (PropertyListTransport)_resp;
        }
        catch (Exception _exception) {
            return (PropertyListTransport)JavaUtils.convert((Object)_resp, PropertyListTransport.class);
        }
    }

    @Override
    public void addSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, AttachmentTransport attachment) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "addSDIAttachment"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, sdcid, keyid1, keyid2, keyid3, attachment});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
    }

    @Override
    public int execSQL(String connectionid, int sqlCode, Object[] bindVars) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "execSQL"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, new Integer(sqlCode), bindVars});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (Integer)_resp;
        }
        catch (Exception _exception) {
            return (Integer)JavaUtils.convert((Object)_resp, Integer.TYPE);
        }
    }

    @Override
    public int execSQL(String connectionid, int sqlCode) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "execSQL"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, new Integer(sqlCode)});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (Integer)_resp;
        }
        catch (Exception _exception) {
            return (Integer)JavaUtils.convert((Object)_resp, Integer.TYPE);
        }
    }

    @Override
    public int execSQL(String connectionid, String sql) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "execSQL"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, sql});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (Integer)_resp;
        }
        catch (Exception _exception) {
            return (Integer)JavaUtils.convert((Object)_resp, Integer.TYPE);
        }
    }

    @Override
    public PropertyListTransport getSDCProperties(String connectionId, String sdcid) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getSDCProperties"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionId, sdcid});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (PropertyListTransport)_resp;
        }
        catch (Exception _exception) {
            return (PropertyListTransport)JavaUtils.convert((Object)_resp, PropertyListTransport.class);
        }
    }

    @Override
    public AttachmentTransport getSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, boolean zip) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getSDIAttachment"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, sdcid, keyid1, keyid2, keyid3, new Integer(attachmentNum), new Boolean(zip)});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (AttachmentTransport)_resp;
        }
        catch (Exception _exception) {
            return (AttachmentTransport)JavaUtils.convert((Object)_resp, AttachmentTransport.class);
        }
    }

    @Override
    public PropertyListTransport processAction(String connectionid, String actionid, String actionversionid, PropertyListTransport propertyList) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "processAction"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, actionid, actionversionid, propertyList});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (PropertyListTransport)_resp;
        }
        catch (Exception _exception) {
            return (PropertyListTransport)JavaUtils.convert((Object)_resp, PropertyListTransport.class);
        }
    }

    @Override
    public String processAction(String connectionid, String actionid, String actionversionid, String propertyListXML) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "processAction"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, actionid, actionversionid, propertyListXML});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (String)_resp;
        }
        catch (Exception _exception) {
            return (String)JavaUtils.convert((Object)_resp, String.class);
        }
    }

    @Override
    public BaseSECMessage processMessage(String connectionid, BaseSECMessage message, String processingMode) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "processMessage"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, message, processingMode});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (BaseSECMessage)_resp;
        }
        catch (Exception _exception) {
            return (BaseSECMessage)JavaUtils.convert((Object)_resp, BaseSECMessage.class);
        }
    }

    @Override
    public String processMessage(String connectionid, String id, String typeId, String message, String processingMode) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "processMessage"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, id, typeId, message, processingMode});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (String)_resp;
        }
        catch (Exception _exception) {
            return (String)JavaUtils.convert((Object)_resp, String.class);
        }
    }

    @Override
    public DataSetTransport getSqlDataSet(String connectionId, int sqlCode, Object[] bindVars, boolean returnClobs) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[13]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getSqlDataSet"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionId, new Integer(sqlCode), bindVars, new Boolean(returnClobs)});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (DataSetTransport)_resp;
        }
        catch (Exception _exception) {
            return (DataSetTransport)JavaUtils.convert((Object)_resp, DataSetTransport.class);
        }
    }

    @Override
    public String getSqlDataSet(String connectionid, int sqlCode) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[14]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getSqlDataSet"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, new Integer(sqlCode)});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (String)_resp;
        }
        catch (Exception _exception) {
            return (String)JavaUtils.convert((Object)_resp, String.class);
        }
    }

    @Override
    public String getSqlDataSet(String connectionid, int sqlCode, Object[] bindVars) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[15]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getSqlDataSet"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, new Integer(sqlCode), bindVars});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (String)_resp;
        }
        catch (Exception _exception) {
            return (String)JavaUtils.convert((Object)_resp, String.class);
        }
    }

    @Override
    public DataSetTransport getSqlDataSet(String connectionId, String sql, boolean returnClobs) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[16]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getSqlDataSet"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionId, sql, new Boolean(returnClobs)});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (DataSetTransport)_resp;
        }
        catch (Exception _exception) {
            return (DataSetTransport)JavaUtils.convert((Object)_resp, DataSetTransport.class);
        }
    }

    @Override
    public DataSetTransport getSqlDataSet(String connectionId, int sqlCode, boolean returnClobs) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[17]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getSqlDataSet"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionId, new Integer(sqlCode), new Boolean(returnClobs)});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (DataSetTransport)_resp;
        }
        catch (Exception _exception) {
            return (DataSetTransport)JavaUtils.convert((Object)_resp, DataSetTransport.class);
        }
    }

    @Override
    public String getSqlDataSet(String connectionid, String sql) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[18]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getSqlDataSet"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, sql});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (String)_resp;
        }
        catch (Exception _exception) {
            return (String)JavaUtils.convert((Object)_resp, String.class);
        }
    }

    @Override
    public String processApplicationCommand(String command, String paramsXML) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[19]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "processApplicationCommand"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{command, paramsXML});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (String)_resp;
        }
        catch (Exception _exception) {
            return (String)JavaUtils.convert((Object)_resp, String.class);
        }
    }

    @Override
    public SDIDataTransport getSDIData(String connectionId, SDIRequestTransport sdireqbean) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[20]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getSDIData"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionId, sdireqbean});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (SDIDataTransport)_resp;
        }
        catch (Exception _exception) {
            return (SDIDataTransport)JavaUtils.convert((Object)_resp, SDIDataTransport.class);
        }
    }

    @Override
    public ActionBlockTransport processActionBlock(String connectionid, ActionBlockTransport block) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[21]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "processActionBlock"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, block});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (ActionBlockTransport)_resp;
        }
        catch (Exception _exception) {
            return (ActionBlockTransport)JavaUtils.convert((Object)_resp, ActionBlockTransport.class);
        }
    }

    @Override
    public void editSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, AttachmentTransport attachment) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[22]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "editSDIAttachment"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, sdcid, keyid1, keyid2, keyid3, new Integer(attachmentNum), attachment});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
    }

    @Override
    public void deleteSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[23]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "deleteSDIAttachment"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, sdcid, keyid1, keyid2, keyid3, new Integer(attachmentNum)});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
    }

    @Override
    public String getPublicKey() throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[24]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getPublicKey"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[0]);
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (String)_resp;
        }
        catch (Exception _exception) {
            return (String)JavaUtils.convert((Object)_resp, String.class);
        }
    }

    @Override
    public String getVersion() throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[25]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getVersion"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[0]);
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (String)_resp;
        }
        catch (Exception _exception) {
            return (String)JavaUtils.convert((Object)_resp, String.class);
        }
    }

    @Override
    public int getSequence(String connectionid, String sdcid, String sequenceid, int startsequencenumber, int incrementby) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[26]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getSequence"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid, sdcid, sequenceid, new Integer(startsequencenumber), new Integer(incrementby)});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (Integer)_resp;
        }
        catch (Exception _exception) {
            return (Integer)JavaUtils.convert((Object)_resp, Integer.TYPE);
        }
    }

    @Override
    public String getConnectionId(String databaseid, String userid, String password) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[27]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "getConnectionId"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{databaseid, userid, password});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (String)_resp;
        }
        catch (Exception _exception) {
            return (String)JavaUtils.convert((Object)_resp, String.class);
        }
    }

    @Override
    public boolean checkConnection(String connectionid) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[28]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "checkConnection"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (Boolean)_resp;
        }
        catch (Exception _exception) {
            return (Boolean)JavaUtils.convert((Object)_resp, Boolean.TYPE);
        }
    }

    @Override
    public void clearConnection(String connectionid) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[29]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("http://webservices.sapphire.labvantage.com", "clearConnection"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{connectionid});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
    }

    static {
        SapphireWSSoapBindingStub._initOperationDesc1();
        SapphireWSSoapBindingStub._initOperationDesc2();
        SapphireWSSoapBindingStub._initOperationDesc3();
    }
}

