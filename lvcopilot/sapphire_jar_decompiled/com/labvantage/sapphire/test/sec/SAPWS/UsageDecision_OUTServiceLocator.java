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

import com.labvantage.sapphire.test.sec.SAPWS.UsageDecision_OUT;
import com.labvantage.sapphire.test.sec.SAPWS.UsageDecision_OUTBindingStub;
import com.labvantage.sapphire.test.sec.SAPWS.UsageDecision_OUTService;
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

public class UsageDecision_OUTServiceLocator
extends Service
implements UsageDecision_OUTService {
    private String UsageDecision_OUTPort_address = "http://aprins07:51000/XISOAPAdapter/MessageServlet?channel=:SAPPHIRE:SOAP_IN_UsageDecision&version=3.0&Sender.Service=SAPPHIRE&Interface=http%3A%2F%2Flabvantage.com%2Fxi%2FSEC%2FR5%2FUsageDecision%2F1.0%5EUsageDecision_OUT";
    private String UsageDecision_OUTPortWSDDServiceName = "UsageDecision_OUTPort";
    private HashSet ports = null;

    public UsageDecision_OUTServiceLocator() {
    }

    public UsageDecision_OUTServiceLocator(EngineConfiguration config) {
        super(config);
    }

    public UsageDecision_OUTServiceLocator(String wsdlLoc, QName sName) throws ServiceException {
        super(wsdlLoc, sName);
    }

    @Override
    public String getUsageDecision_OUTPortAddress() {
        return this.UsageDecision_OUTPort_address;
    }

    public String getUsageDecision_OUTPortWSDDServiceName() {
        return this.UsageDecision_OUTPortWSDDServiceName;
    }

    public void setUsageDecision_OUTPortWSDDServiceName(String name) {
        this.UsageDecision_OUTPortWSDDServiceName = name;
    }

    @Override
    public UsageDecision_OUT getUsageDecision_OUTPort() throws ServiceException {
        URL endpoint;
        try {
            endpoint = new URL(this.UsageDecision_OUTPort_address);
        }
        catch (MalformedURLException e) {
            throw new ServiceException((Throwable)e);
        }
        return this.getUsageDecision_OUTPort(endpoint);
    }

    @Override
    public UsageDecision_OUT getUsageDecision_OUTPort(URL portAddress) throws ServiceException {
        try {
            UsageDecision_OUTBindingStub _stub = new UsageDecision_OUTBindingStub(portAddress, this);
            _stub.setPortName(this.getUsageDecision_OUTPortWSDDServiceName());
            return _stub;
        }
        catch (AxisFault e) {
            return null;
        }
    }

    public void setUsageDecision_OUTPortEndpointAddress(String address) {
        this.UsageDecision_OUTPort_address = address;
    }

    public Remote getPort(Class serviceEndpointInterface) throws ServiceException {
        try {
            if (UsageDecision_OUT.class.isAssignableFrom(serviceEndpointInterface)) {
                UsageDecision_OUTBindingStub _stub = new UsageDecision_OUTBindingStub(new URL(this.UsageDecision_OUTPort_address), this);
                _stub.setPortName(this.getUsageDecision_OUTPortWSDDServiceName());
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
        if ("UsageDecision_OUTPort".equals(inputPortName)) {
            return this.getUsageDecision_OUTPort();
        }
        Remote _stub = this.getPort(serviceEndpointInterface);
        ((Stub)_stub).setPortName(portName);
        return _stub;
    }

    public QName getServiceName() {
        return new QName("http://labvantage.com/xi/SEC/R5/UsageDecision/1.0", "UsageDecision_OUTService");
    }

    public Iterator getPorts() {
        if (this.ports == null) {
            this.ports = new HashSet();
            this.ports.add(new QName("http://labvantage.com/xi/SEC/R5/UsageDecision/1.0", "UsageDecision_OUTPort"));
        }
        return this.ports.iterator();
    }

    public void setEndpointAddress(String portName, String address) throws ServiceException {
        if (!"UsageDecision_OUTPort".equals(portName)) {
            throw new ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
        this.setUsageDecision_OUTPortEndpointAddress(address);
    }

    public void setEndpointAddress(QName portName, String address) throws ServiceException {
        this.setEndpointAddress(portName.getLocalPart(), address);
    }
}

