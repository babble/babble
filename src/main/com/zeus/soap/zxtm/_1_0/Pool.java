/**
 * Pool.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.zeus.soap.zxtm._1_0;

public interface Pool extends javax.xml.rpc.Service {

/**
 * The Pool interface allows management of ZXTM pool objects. Using
 * this interface, you can create, delete and rename pool objects, and
 * manage their configuration.
 */
    public java.lang.String getPoolPortAddress();

    public com.zeus.soap.zxtm._1_0.PoolPort getPoolPort() throws javax.xml.rpc.ServiceException;

    public com.zeus.soap.zxtm._1_0.PoolPort getPoolPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
