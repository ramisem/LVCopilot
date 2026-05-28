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
package com.labvantage.sapphire.test.sec.SAPWS;

import com.labvantage.sapphire.test.sec.SAPWS.PRCreate;
import com.labvantage.sapphire.test.sec.SAPWS.PRCreate_OUT;
import com.labvantage.sapphire.test.sec.SAPWS.Result;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Vector;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.encoding.SerializerFactory;
import org.apache.axis.AxisFault;
import org.apache.axis.NoEndPointException;
import org.apache.axis.client.Call;
import org.apache.axis.client.Stub;
import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.encoding.DeserializerFactory;
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

public class PRCreate_OUTBindingStub
extends Stub
implements PRCreate_OUT {
    private Vector cachedSerClasses = new Vector();
    private Vector cachedSerQNames = new Vector();
    private Vector cachedSerFactories = new Vector();
    private Vector cachedDeserFactories = new Vector();
    static OperationDesc[] _operations = new OperationDesc[1];

    private static void _initOperationDesc1() {
        OperationDesc oper = new OperationDesc();
        oper.setName("PRCreate_OUT");
        ParameterDesc param = new ParameterDesc(new QName("http://labvantage.com/xi/SEC/R5/PRCreate/1.0", "PRCreate"), 1, new QName("http://labvantage.com/xi/SEC/R5/PRCreate/1.0", "PRCreate"), PRCreate.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://labvantage.com/xi/SEC/R5/PRCreate/1.0", "Result"));
        oper.setReturnClass(Result.class);
        oper.setReturnQName(new QName("http://labvantage.com/xi/SEC/R5/PRCreate/1.0", "Result"));
        oper.setStyle(Style.DOCUMENT);
        oper.setUse(Use.LITERAL);
        PRCreate_OUTBindingStub._operations[0] = oper;
    }

    public PRCreate_OUTBindingStub() throws AxisFault {
        this(null);
    }

    public PRCreate_OUTBindingStub(URL endpointURL, Service service) throws AxisFault {
        this(service);
        this.cachedEndpoint = endpointURL;
    }

    public PRCreate_OUTBindingStub(Service service) throws AxisFault {
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
        QName qName = new QName("http://labvantage.com/xi/SEC/R5/PRCreate/1.0", "PRCreate");
        this.cachedSerQNames.add(qName);
        Class cls = PRCreate.class;
        this.cachedSerClasses.add(cls);
        this.cachedSerFactories.add(beansf);
        this.cachedDeserFactories.add(beandf);
        qName = new QName("http://labvantage.com/xi/SEC/R5/PRCreate/1.0", "Result");
        this.cachedSerQNames.add(qName);
        cls = Result.class;
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
            PRCreate_OUTBindingStub pRCreate_OUTBindingStub = this;
            synchronized (pRCreate_OUTBindingStub) {
                if (this.firstCall()) {
                    _call.setEncodingStyle(null);
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
                        if (!(x instanceof SerializerFactory)) continue;
                        sf = (org.apache.axis.encoding.SerializerFactory)this.cachedSerFactories.get(i);
                        df = (DeserializerFactory)this.cachedDeserFactories.get(i);
                        _call.registerTypeMapping(cls, qName, (org.apache.axis.encoding.SerializerFactory)sf, (DeserializerFactory)df, false);
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
    public Result PRCreate_OUT(PRCreate PRCreate2) throws RemoteException {
        if (this.cachedEndpoint == null) {
            throw new NoEndPointException();
        }
        Call _call = this.createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://sap.com/xi/WebService/soap1.1");
        _call.setEncodingStyle(null);
        _call.setProperty("sendXsiTypes", (Object)Boolean.FALSE);
        _call.setProperty("sendMultiRefs", (Object)Boolean.FALSE);
        _call.setSOAPVersion((SOAPConstants)SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new QName("", "PRCreate_OUT"));
        this.setRequestHeaders(_call);
        this.setAttachments(_call);
        Object _resp = _call.invoke(new Object[]{PRCreate2});
        if (_resp instanceof RemoteException) {
            throw (RemoteException)_resp;
        }
        this.extractAttachments(_call);
        try {
            return (Result)_resp;
        }
        catch (Exception _exception) {
            return (Result)JavaUtils.convert((Object)_resp, Result.class);
        }
    }

    static {
        PRCreate_OUTBindingStub._initOperationDesc1();
    }
}

