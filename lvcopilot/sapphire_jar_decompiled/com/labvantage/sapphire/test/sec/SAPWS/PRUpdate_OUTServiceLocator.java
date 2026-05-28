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

import com.labvantage.sapphire.test.sec.SAPWS.PRUpdate_OUT;
import com.labvantage.sapphire.test.sec.SAPWS.PRUpdate_OUTBindingStub;
import com.labvantage.sapphire.test.sec.SAPWS.PRUpdate_OUTService;
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

public class PRUpdate_OUTServiceLocator
extends Service
implements PRUpdate_OUTService {
    private String PRUpdate_OUTPort_address = "http://aprins07:51000/XISOAPAdapter/MessageServlet?channel=:SAPPHIRE:SOAP_IN_PRUpdate&version=3.0&Sender.Service=SAPPHIRE&Interface=http%3A%2F%2Flabvantage.com%2Fxi%2FSEC%2FR5%2FPRUpdate%2F1.0%5EPRUpdate_OUT";
    private String PRUpdate_OUTPortWSDDServiceName = "PRUpdate_OUTPort";
    private HashSet ports = null;

    public PRUpdate_OUTServiceLocator() {
    }

    public PRUpdate_OUTServiceLocator(EngineConfiguration config) {
        super(config);
    }

    public PRUpdate_OUTServiceLocator(String wsdlLoc, QName sName) throws ServiceException {
        super(wsdlLoc, sName);
    }

    @Override
    public String getPRUpdate_OUTPortAddress() {
        return this.PRUpdate_OUTPort_address;
    }

    public String getPRUpdate_OUTPortWSDDServiceName() {
        return this.PRUpdate_OUTPortWSDDServiceName;
    }

    public void setPRUpdate_OUTPortWSDDServiceName(String name) {
        this.PRUpdate_OUTPortWSDDServiceName = name;
    }

    @Override
    public PRUpdate_OUT getPRUpdate_OUTPort() throws ServiceException {
        URL endpoint;
        try {
            endpoint = new URL(this.PRUpdate_OUTPort_address);
        }
        catch (MalformedURLException e) {
            throw new ServiceException((Throwable)e);
        }
        return this.getPRUpdate_OUTPort(endpoint);
    }

    @Override
    public PRUpdate_OUT getPRUpdate_OUTPort(URL portAddress) throws ServiceException {
        try {
            PRUpdate_OUTBindingStub _stub = new PRUpdate_OUTBindingStub(portAddress, this);
            _stub.setPortName(this.getPRUpdate_OUTPortWSDDServiceName());
            return _stub;
        }
        catch (AxisFault e) {
            return null;
        }
    }

    public void setPRUpdate_OUTPortEndpointAddress(String address) {
        this.PRUpdate_OUTPort_address = address;
    }

    public Remote getPort(Class serviceEndpointInterface) throws ServiceException {
        try {
            if (PRUpdate_OUT.class.isAssignableFrom(serviceEndpointInterface)) {
                PRUpdate_OUTBindingStub _stub = new PRUpdate_OUTBindingStub(new URL(this.PRUpdate_OUTPort_address), this);
                _stub.setPortName(this.getPRUpdate_OUTPortWSDDServiceName());
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
        if ("PRUpdate_OUTPort".equals(inputPortName)) {
            return this.getPRUpdate_OUTPort();
        }
        Remote _stub = this.getPort(serviceEndpointInterface);
        ((Stub)_stub).setPortName(portName);
        return _stub;
    }

    public QName getServiceName() {
        return new QName("http://labvantage.com/xi/SEC/R5/PRUpdate/1.0", "PRUpdate_OUTService");
    }

    public Iterator getPorts() {
        if (this.ports == null) {
            this.ports = new HashSet();
            this.ports.add(new QName("http://labvantage.com/xi/SEC/R5/PRUpdate/1.0", "PRUpdate_OUTPort"));
        }
        return this.ports.iterator();
    }

    public void setEndpointAddress(String portName, String address) throws ServiceException {
        if (!"PRUpdate_OUTPort".equals(portName)) {
            throw new ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
        this.setPRUpdate_OUTPortEndpointAddress(address);
    }

    public void setEndpointAddress(QName portName, String address) throws ServiceException {
        this.setEndpointAddress(portName.getLocalPart(), address);
    }
}

