/**
 * CatalogRule.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.zeus.soap.zxtm._1_0;

public interface CatalogRule extends javax.xml.rpc.Service {

/**
 * The Catalog.Rule interface allows management of ZXTM TrafficScript
 * Rules. Using this interface, you can create, delete and rename rules,
 * and manage their configuration.  You can also syntax-check rule fragments.
 */
    public java.lang.String getCatalogRulePortAddress();

    public com.zeus.soap.zxtm._1_0.CatalogRulePort getCatalogRulePort() throws javax.xml.rpc.ServiceException;

    public com.zeus.soap.zxtm._1_0.CatalogRulePort getCatalogRulePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
