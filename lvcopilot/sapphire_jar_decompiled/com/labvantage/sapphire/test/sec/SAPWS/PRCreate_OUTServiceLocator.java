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

import com.labvantage.sapphire.test.sec.SAPWS.PRCreate_OUT;
import com.labvantage.sapphire.test.sec.SAPWS.PRCreate_OUTBindingStub;
import com.labvantage.sapphire.test.sec.SAPWS.PRCreate_OUTService;
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

public class PRCreate_OUTServiceLocator
extends Service
implements PRCreate_OUTService {
    private String PRCreate_OUTPort_address = "http://aprins07:51000/XISOAPAdapter/MessageServlet?channel=:SAPPHIRE:SOAP_IN_PRCreate&version=3.0&Sender.Service=SAPPHIRE&Interface=http%3A%2F%2Flabvantage.com%2Fxi%2FSEC%2FR5%2FPRCreate%2F1.0%5EPRCreate_OUT";
    private String PRCreate_OUTPortWSDDServiceName = "PRCreate_OUTPort";
    private HashSet ports = null;

    public PRCreate_OUTServiceLocator() {
    }

    public PRCreate_OUTServiceLocator(EngineConfiguration config) {
        super(config);
    }

    public PRCreate_OUTServiceLocator(String wsdlLoc, QName sName) throws ServiceException {
        super(wsdlLoc, sName);
    }

    @Override
    public String getPRCreate_OUTPortAddress() {
        return this.PRCreate_OUTPort_address;
    }

    public String getPRCreate_OUTPortWSDDServiceName() {
        return this.PRCreate_OUTPortWSDDServiceName;
    }

    public void setPRCreate_OUTPortWSDDServiceName(String name) {
        this.PRCreate_OUTPortWSDDServiceName = name;
    }

    @Override
    public PRCreate_OUT getPRCreate_OUTPort() throws ServiceException {
        URL endpoint;
        try {
            endpoint = new URL(this.PRCreate_OUTPort_address);
        }
        catch (MalformedURLException e) {
            throw new ServiceException((Throwable)e);
        }
        return this.getPRCreate_OUTPort(endpoint);
    }

    @Override
    public PRCreate_OUT getPRCreate_OUTPort(URL portAddress) throws ServiceException {
        try {
            PRCreate_OUTBindingStub _stub = new PRCreate_OUTBindingStub(portAddress, this);
            _stub.setPortName(this.getPRCreate_OUTPortWSDDServiceName());
            return _stub;
        }
        catch (AxisFault e) {
            return null;
        }
    }

    public void setPRCreate_OUTPortEndpointAddress(String address) {
        this.PRCreate_OUTPort_address = address;
    }

    public Remote getPort(Class serviceEndpointInterface) throws ServiceException {
        try {
            if (PRCreate_OUT.class.isAssignableFrom(serviceEndpointInterface)) {
                PRCreate_OUTBindingStub _stub = new PRCreate_OUTBindingStub(new URL(this.PRCreate_OUTPort_address), this);
                _stub.setPortName(this.getPRCreate_OUTPortWSDDServiceName());
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
        if ("PRCreate_OUTPort".equals(inputPortName)) {
            return this.getPRCreate_OUTPort();
        }
        Remote _stub = this.getPort(serviceEndpointInterface);
        ((Stub)_stub).setPortName(portName);
        return _stub;
    }

    public QName getServiceName() {
        return new QName("http://labvantage.com/xi/SEC/R5/PRCreate/1.0", "PRCreate_OUTService");
    }

    public Iterator getPorts() {
        if (this.ports == null) {
            this.ports = new HashSet();
            this.ports.add(new QName("http://labvantage.com/xi/SEC/R5/PRCreate/1.0", "PRCreate_OUTPort"));
        }
        return this.ports.iterator();
    }

    public void setEndpointAddress(String portName, String address) throws ServiceException {
        if (!"PRCreate_OUTPort".equals(portName)) {
            throw new ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
        this.setPRCreate_OUTPortEndpointAddress(address);
    }

    public void setEndpointAddress(QName portName, String address) throws ServiceException {
        this.setEndpointAddress(portName.getLocalPart(), address);
    }
}

