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
package com.labvantage.sapphire.webservices.client;

import com.labvantage.sapphire.webservices.client.SapphireWSService;
import com.labvantage.sapphire.webservices.client.SapphireWSSoapBindingStub;
import com.labvantage.sapphire.webservices.client.SapphireWS_PortType;
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

public class SapphireWSServiceLocator
extends Service
implements SapphireWSService {
    private String SapphireWS_address = "https://lt6491eu.lims.com:8443/labvantage/services/SapphireWS";
    private String SapphireWSWSDDServiceName = "SapphireWS";
    private HashSet ports = null;

    public SapphireWSServiceLocator() {
    }

    public SapphireWSServiceLocator(EngineConfiguration config) {
        super(config);
    }

    public SapphireWSServiceLocator(String wsdlLoc, QName sName) throws ServiceException {
        super(wsdlLoc, sName);
    }

    @Override
    public String getSapphireWSAddress() {
        return this.SapphireWS_address;
    }

    public String getSapphireWSWSDDServiceName() {
        return this.SapphireWSWSDDServiceName;
    }

    public void setSapphireWSWSDDServiceName(String name) {
        this.SapphireWSWSDDServiceName = name;
    }

    @Override
    public SapphireWS_PortType getSapphireWS() throws ServiceException {
        URL endpoint;
        try {
            endpoint = new URL(this.SapphireWS_address);
        }
        catch (MalformedURLException e) {
            throw new ServiceException((Throwable)e);
        }
        return this.getSapphireWS(endpoint);
    }

    @Override
    public SapphireWS_PortType getSapphireWS(URL portAddress) throws ServiceException {
        try {
            SapphireWSSoapBindingStub _stub = new SapphireWSSoapBindingStub(portAddress, this);
            _stub.setPortName(this.getSapphireWSWSDDServiceName());
            return _stub;
        }
        catch (AxisFault e) {
            return null;
        }
    }

    public void setSapphireWSEndpointAddress(String address) {
        this.SapphireWS_address = address;
    }

    public Remote getPort(Class serviceEndpointInterface) throws ServiceException {
        try {
            if (SapphireWS_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                SapphireWSSoapBindingStub _stub = new SapphireWSSoapBindingStub(new URL(this.SapphireWS_address), this);
                _stub.setPortName(this.getSapphireWSWSDDServiceName());
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
        if ("SapphireWS".equals(inputPortName)) {
            return this.getSapphireWS();
        }
        Remote _stub = this.getPort(serviceEndpointInterface);
        ((Stub)_stub).setPortName(portName);
        return _stub;
    }

    public QName getServiceName() {
        return new QName("https://lt6491eu.lims.com:8443/labvantage/services/SapphireWS", "SapphireWSService");
    }

    public Iterator getPorts() {
        if (this.ports == null) {
            this.ports = new HashSet();
            this.ports.add(new QName("https://lt6491eu.lims.com:8443/labvantage/services/SapphireWS", "SapphireWS"));
        }
        return this.ports.iterator();
    }

    public void setEndpointAddress(String portName, String address) throws ServiceException {
        if (!"SapphireWS".equals(portName)) {
            throw new ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
        this.setSapphireWSEndpointAddress(address);
    }

    public void setEndpointAddress(QName portName, String address) throws ServiceException {
        this.setEndpointAddress(portName.getLocalPart(), address);
    }
}

