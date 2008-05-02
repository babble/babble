/**
 * PoolLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.zeus.soap.zxtm._1_0;

public class PoolLocator extends org.apache.axis.client.Service implements com.zeus.soap.zxtm._1_0.Pool {

/**
 * The Pool interface allows management of ZXTM pool objects. Using
 * this interface, you can create, delete and rename pool objects, and
 * manage their configuration.
 */

    public PoolLocator() {
    }


    public PoolLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public PoolLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for PoolPort
    private java.lang.String PoolPort_address = "https://admin_server:9090/soap";

    public java.lang.String getPoolPortAddress() {
        return PoolPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String PoolPortWSDDServiceName = "PoolPort";

    public java.lang.String getPoolPortWSDDServiceName() {
        return PoolPortWSDDServiceName;
    }

    public void setPoolPortWSDDServiceName(java.lang.String name) {
        PoolPortWSDDServiceName = name;
    }

    public com.zeus.soap.zxtm._1_0.PoolPort getPoolPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(PoolPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getPoolPort(endpoint);
    }

    public com.zeus.soap.zxtm._1_0.PoolPort getPoolPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.zeus.soap.zxtm._1_0.PoolBindingStub _stub = new com.zeus.soap.zxtm._1_0.PoolBindingStub(portAddress, this);
            _stub.setPortName(getPoolPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setPoolPortEndpointAddress(java.lang.String address) {
        PoolPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.zeus.soap.zxtm._1_0.PoolPort.class.isAssignableFrom(serviceEndpointInterface)) {
                com.zeus.soap.zxtm._1_0.PoolBindingStub _stub = new com.zeus.soap.zxtm._1_0.PoolBindingStub(new java.net.URL(PoolPort_address), this);
                _stub.setPortName(getPoolPortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("PoolPort".equals(inputPortName)) {
            return getPoolPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://soap.zeus.com/zxtm/1.0/", "Pool");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://soap.zeus.com/zxtm/1.0/", "PoolPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("PoolPort".equals(portName)) {
            setPoolPortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
