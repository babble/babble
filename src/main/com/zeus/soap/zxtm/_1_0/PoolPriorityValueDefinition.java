/**
 * PoolPriorityValueDefinition.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.zeus.soap.zxtm._1_0;


/**
 * This structure contains the priority for a particular node. The
 * priority is used when using the priority lists functionality.
 */
public class PoolPriorityValueDefinition  implements java.io.Serializable {
    /* The name of the node. */
    private java.lang.String node;

    /* The priority value. */
    private int priority;

    public PoolPriorityValueDefinition() {
    }

    public PoolPriorityValueDefinition(
           java.lang.String node,
           int priority) {
           this.node = node;
           this.priority = priority;
    }


    /**
     * Gets the node value for this PoolPriorityValueDefinition.
     * 
     * @return node   * The name of the node.
     */
    public java.lang.String getNode() {
        return node;
    }


    /**
     * Sets the node value for this PoolPriorityValueDefinition.
     * 
     * @param node   * The name of the node.
     */
    public void setNode(java.lang.String node) {
        this.node = node;
    }


    /**
     * Gets the priority value for this PoolPriorityValueDefinition.
     * 
     * @return priority   * The priority value.
     */
    public int getPriority() {
        return priority;
    }


    /**
     * Sets the priority value for this PoolPriorityValueDefinition.
     * 
     * @param priority   * The priority value.
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PoolPriorityValueDefinition)) return false;
        PoolPriorityValueDefinition other = (PoolPriorityValueDefinition) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.node==null && other.getNode()==null) || 
             (this.node!=null &&
              this.node.equals(other.getNode()))) &&
            this.priority == other.getPriority();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getNode() != null) {
            _hashCode += getNode().hashCode();
        }
        _hashCode += getPriority();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PoolPriorityValueDefinition.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://soap.zeus.com/zxtm/1.0/", "Pool.PriorityValueDefinition"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("node");
        elemField.setXmlName(new javax.xml.namespace.QName("", "node"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("priority");
        elemField.setXmlName(new javax.xml.namespace.QName("", "priority"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
