/**
 * CatalogRuleRuleInfo.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.zeus.soap.zxtm._1_0;


/**
 * This structure contains basic information for a rule in the catalog.
 */
public class CatalogRuleRuleInfo  implements java.io.Serializable {
    /* The rule text */
    private java.lang.String rule_text;

    /* The descriptive notes for the rule. */
    private java.lang.String rule_notes;

    public CatalogRuleRuleInfo() {
    }

    public CatalogRuleRuleInfo(
           java.lang.String rule_text,
           java.lang.String rule_notes) {
           this.rule_text = rule_text;
           this.rule_notes = rule_notes;
    }


    /**
     * Gets the rule_text value for this CatalogRuleRuleInfo.
     * 
     * @return rule_text   * The rule text
     */
    public java.lang.String getRule_text() {
        return rule_text;
    }


    /**
     * Sets the rule_text value for this CatalogRuleRuleInfo.
     * 
     * @param rule_text   * The rule text
     */
    public void setRule_text(java.lang.String rule_text) {
        this.rule_text = rule_text;
    }


    /**
     * Gets the rule_notes value for this CatalogRuleRuleInfo.
     * 
     * @return rule_notes   * The descriptive notes for the rule.
     */
    public java.lang.String getRule_notes() {
        return rule_notes;
    }


    /**
     * Sets the rule_notes value for this CatalogRuleRuleInfo.
     * 
     * @param rule_notes   * The descriptive notes for the rule.
     */
    public void setRule_notes(java.lang.String rule_notes) {
        this.rule_notes = rule_notes;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CatalogRuleRuleInfo)) return false;
        CatalogRuleRuleInfo other = (CatalogRuleRuleInfo) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.rule_text==null && other.getRule_text()==null) || 
             (this.rule_text!=null &&
              this.rule_text.equals(other.getRule_text()))) &&
            ((this.rule_notes==null && other.getRule_notes()==null) || 
             (this.rule_notes!=null &&
              this.rule_notes.equals(other.getRule_notes())));
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
        if (getRule_text() != null) {
            _hashCode += getRule_text().hashCode();
        }
        if (getRule_notes() != null) {
            _hashCode += getRule_notes().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CatalogRuleRuleInfo.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://soap.zeus.com/zxtm/1.0/", "Catalog.Rule.RuleInfo"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rule_text");
        elemField.setXmlName(new javax.xml.namespace.QName("", "rule_text"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rule_notes");
        elemField.setXmlName(new javax.xml.namespace.QName("", "rule_notes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
