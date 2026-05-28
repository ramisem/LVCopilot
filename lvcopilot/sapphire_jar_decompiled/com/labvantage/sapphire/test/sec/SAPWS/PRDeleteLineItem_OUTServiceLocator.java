/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.rpc.ServiceException
 *  org.apache.axis.AxisFault
 *  org.apache.axis.EngineConfiguration
 *  org.apache.axis.client.Service
 *  org.apache.axis.client.Stub
 */
package com.labvantage.sapphire.test.sec.SAPWS;

import com.labvantage.sapphire.test.sec.SAPWS.PRDeleteLineItem_OUT;
import com.labvantage.sapphire.test.sec.SAPWS.PRDeleteLineItem_OUTBindingStub;
import com.labvantage.sapphire.test.sec.SAPWS.PRDeleteLineItem_OUTService;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Remote;
import java.util.HashSet;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import org.apache.axis.AxisFault;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.Service;
import org.apache.axis.client.Stub;

public class PRDeleteLineItem_OUTServiceLocator
extends Service
implements PRDeleteLineItem_OUTService {
    private String PRDeleteLineItem_OUTPort_address = "http://aprins07:51000/XISOAPAdapter/MessageServlet?channel=:SAPPHIRE:SOAP_IN_PRDeleteLineItem&version=3.0&Sender.Service=SAPPHIRE&Interface=http%3A%2F%2Flabvantage.com%2Fxi%2FSEC%2FR5%2FDeleteLineItemOfPR%2F1.0%5EPRDeleteLineItem_OUT";
    private String PRDeleteLineItem_OUTPortWSDDServiceName = "PRDeleteLineItem_OUTPort";
    private HashSet ports = null;

    public PRDeleteLineItem_OUTServiceLocator() {
    }

    public PRDeleteLineItem_OUTServiceLocator(EngineConfiguration config) {
        super(config);
    }

    public PRDeleteLineItem_OUTServiceLocator(String wsdlLoc, QName sName) throws ServiceException {
        super(wsdlLoc, sName);
    }

    @Override
    public String getPRDeleteLineItem_OUTPortAddress() {
        return this.PRDeleteLineItem_OUTPort_address;
    }

    public String getPRDeleteLineItem_OUTPortWSDDServiceName() {
        return this.PRDeleteLineItem_OUTPortWSDDServiceName;
    }

    public void setPRDeleteLineItem_OUTPortWSDDServiceName(String name) {
        this.PRDeleteLineItem_OUTPortWSDDServiceName = name;
    }

    @Override
    public PRDeleteLineItem_OUT getPRDeleteLineItem_OUTPort() throws ServiceException {
        URL endpoint;
        try {
            endpoint = new URL(this.PRDeleteLineItem_OUTPort_address);
        }
        catch (MalformedURLException e) {
            throw new ServiceException((Throwable)e);
        }
        return this.getPRDeleteLineItem_OUTPort(endpoint);
    }

    @Override
    public PRDeleteLineItem_OUT getPRDeleteLineItem_OUTPort(URL portAddress) throws ServiceException {
        try {
            PRDeleteLineItem_OUTBindingStub _stub = new PRDeleteLineItem_OUTBindingStub(portAddress, this);
            _stub.setPortName(this.getPRDeleteLineItem_OUTPortWSDDServiceName());
            return _stub;
        }
        catch (AxisFault e) {
            return null;
        }
    }

    public void setPRDeleteLineItem_OUTPortEndpointAddress(String address) {
        this.PRDeleteLineItem_OUTPort_address = address;
    }

    public Remote getPort(Class serviceEndpointInterface) throws ServiceException {
        try {
            if (PRDeleteLineItem_OUT.class.isAssignableFrom(serviceEndpointInterface)) {
                PRDeleteLineItem_OUTBindingStub _stub = new PRDeleteLineItem_OUTBindingStub(new URL(this.PRDeleteLineItem_OUTPort_address), this);
                _stub.setPortName(this.getPRDeleteLineItem_OUTPortWSDDServiceName());
                return _stub;
            }
        }
        catch (Throwable t) {
            throw new ServiceException(t);
        }
        throw new ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    public Remote getPort(QName portName, Class serviceEndpointInterface) throws ServiceException {
        if (portName == null) {
            return this.getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("PRDeleteLineItem_OUTPort".equals(inputPortName)) {
            return this.getPRDeleteLineItem_OUTPort();
        }
        Remote _stub = this.getPort(serviceEndpointInterface);
        ((Stub)_stub).setPortName(portName);
        return _stub;
    }

    public QName getServiceName() {
        return new QName("http://labvantage.com/xi/SEC/R5/DeleteLineItemOfPR/1.0", "PRDeleteLineItem_OUTService");
    }

    public Iterator getPorts() {
        if (this.ports == null) {
            this.ports = new HashSet();
            this.ports.add(new QName("http://labvantage.com/xi/SEC/R5/DeleteLineItemOfPR/1.0", "PRDeleteLineItem_OUTPort"));
        }
        return this.ports.iterator();
    }

    public void setEndpointAddress(String portName, String address) throws ServiceException {
        if (!"PRDeleteLineItem_OUTPort".equals(portName)) {
            throw new ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
        this.setPRDeleteLineItem_OUTPortEndpointAddress(address);
    }

    public void setEndpointAddress(QName portName, String address) throws ServiceException {
        this.setEndpointAddress(portName.getLocalPart(), address);
    }
}

