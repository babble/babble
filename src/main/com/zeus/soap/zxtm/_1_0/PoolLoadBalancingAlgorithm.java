/**
 * PoolLoadBalancingAlgorithm.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.zeus.soap.zxtm._1_0;

public class PoolLoadBalancingAlgorithm implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected PoolLoadBalancingAlgorithm(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _roundrobin = "roundrobin";
    public static final java.lang.String _wroundrobin = "wroundrobin";
    public static final java.lang.String _cells = "cells";
    public static final java.lang.String _connections = "connections";
    public static final java.lang.String _responsetimes = "responsetimes";
    public static final java.lang.String _random = "random";
    public static final PoolLoadBalancingAlgorithm roundrobin = new PoolLoadBalancingAlgorithm(_roundrobin);
    public static final PoolLoadBalancingAlgorithm wroundrobin = new PoolLoadBalancingAlgorithm(_wroundrobin);
    public static final PoolLoadBalancingAlgorithm cells = new PoolLoadBalancingAlgorithm(_cells);
    public static final PoolLoadBalancingAlgorithm connections = new PoolLoadBalancingAlgorithm(_connections);
    public static final PoolLoadBalancingAlgorithm responsetimes = new PoolLoadBalancingAlgorithm(_responsetimes);
    public static final PoolLoadBalancingAlgorithm random = new PoolLoadBalancingAlgorithm(_random);
    public java.lang.String getValue() { return _value_;}
    public static PoolLoadBalancingAlgorithm fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        PoolLoadBalancingAlgorithm enumeration = (PoolLoadBalancingAlgorithm)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static PoolLoadBalancingAlgorithm fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PoolLoadBalancingAlgorithm.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://soap.zeus.com/zxtm/1.0/", "Pool.LoadBalancingAlgorithm"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
