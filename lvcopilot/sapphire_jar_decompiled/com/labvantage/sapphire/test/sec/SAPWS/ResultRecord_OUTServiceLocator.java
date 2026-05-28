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

import com.labvantage.sapphire.test.sec.SAPWS.ResultRecord_OUT;
import com.labvantage.sapphire.test.sec.SAPWS.ResultRecord_OUTBindingStub;
import com.labvantage.sapphire.test.sec.SAPWS.ResultRecord_OUTService;
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

public class ResultRecord_OUTServiceLocator
extends Service
implements ResultRecord_OUTService {
    private String ResultRecord_OUTPort_address = "http://aprins07:51000/XISOAPAdapter/MessageServlet?channel=:SAPPHIRE:SOAP_IN_ResultRecording&version=3.0&Sender.Service=SAPPHIRE&Interface=http%3A%2F%2Flabvantage.com%2Fxi%2FSEC%2FR5%2FResultRecording%2F1.0%5EResultRecord_OUT";
    private String ResultRecord_OUTPortWSDDServiceName = "ResultRecord_OUTPort";
    private HashSet ports = null;

    public ResultRecord_OUTServiceLocator() {
    }

    public ResultRecord_OUTServiceLocator(EngineConfiguration config) {
        super(config);
    }

    public ResultRecord_OUTServiceLocator(String wsdlLoc, QName sName) throws ServiceException {
        super(wsdlLoc, sName);
    }

    @Override
    public String getResultRecord_OUTPortAddress() {
        return this.ResultRecord_OUTPort_address;
    }

    public String getResultRecord_OUTPortWSDDServiceName() {
        return this.ResultRecord_OUTPortWSDDServiceName;
    }

    public void setResultRecord_OUTPortWSDDServiceName(String name) {
        this.ResultRecord_OUTPortWSDDServiceName = name;
    }

    @Override
    public ResultRecord_OUT getResultRecord_OUTPort() throws ServiceException {
        URL endpoint;
        try {
            endpoint = new URL(this.ResultRecord_OUTPort_address);
        }
        catch (MalformedURLException e) {
            throw new ServiceException((Throwable)e);
        }
        return this.getResultRecord_OUTPort(endpoint);
    }

    @Override
    public ResultRecord_OUT getResultRecord_OUTPort(URL portAddress) throws ServiceException {
        try {
            ResultRecord_OUTBindingStub _stub = new ResultRecord_OUTBindingStub(portAddress, this);
            _stub.setPortName(this.getResultRecord_OUTPortWSDDServiceName());
            return _stub;
        }
        catch (AxisFault e) {
            return null;
        }
    }

    public void setResultRecord_OUTPortEndpointAddress(String address) {
        this.ResultRecord_OUTPort_address = address;
    }

    public Remote getPort(Class serviceEndpointInterface) throws ServiceException {
        try {
            if (ResultRecord_OUT.class.isAssignableFrom(serviceEndpointInterface)) {
                ResultRecord_OUTBindingStub _stub = new ResultRecord_OUTBindingStub(new URL(this.ResultRecord_OUTPort_address), this);
                _stub.setPortName(this.getResultRecord_OUTPortWSDDServiceName());
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
        if ("ResultRecord_OUTPort".equals(inputPortName)) {
            return this.getResultRecord_OUTPort();
        }
        Remote _stub = this.getPort(serviceEndpointInterface);
        ((Stub)_stub).setPortName(portName);
        return _stub;
    }

    public QName getServiceName() {
        return new QName("http://labvantage.com/xi/SEC/R5/ResultRecording/1.0", "ResultRecord_OUTService");
    }

    public Iterator getPorts() {
        if (this.ports == null) {
            this.ports = new HashSet();
            this.ports.add(new QName("http://labvantage.com/xi/SEC/R5/ResultRecording/1.0", "ResultRecord_OUTPort"));
        }
        return this.ports.iterator();
    }

    public void setEndpointAddress(String portName, String address) throws ServiceException {
        if (!"ResultRecord_OUTPort".equals(portName)) {
            throw new ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
        this.setResultRecord_OUTPortEndpointAddress(address);
    }

    public void setEndpointAddress(QName portName, String address) throws ServiceException {
        this.setEndpointAddress(portName.getLocalPart(), address);
    }
}

