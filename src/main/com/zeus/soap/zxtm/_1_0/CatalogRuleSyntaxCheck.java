/**
 * CatalogRuleSyntaxCheck.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.zeus.soap.zxtm._1_0;


/**
 * This structure contains the results of a rule syntax check against
 * a rule in the catalog.
 */
public class CatalogRuleSyntaxCheck  implements java.io.Serializable {
    /* Whether the rule text is valid or not. */
    private boolean valid;

    /* Any warnings (such as deprecated functions) associated with
     * the rule text. */
    private java.lang.String warnings;

    /* Any errors (such as syntax errors) associated with the rule
     * text. */
    private java.lang.String errors;

    public CatalogRuleSyntaxCheck() {
    }

    public CatalogRuleSyntaxCheck(
           boolean valid,
           java.lang.String warnings,
           java.lang.String errors) {
           this.valid = valid;
           this.warnings = warnings;
           this.errors = errors;
    }


    /**
     * Gets the valid value for this CatalogRuleSyntaxCheck.
     * 
     * @return valid   * Whether the rule text is valid or not.
     */
    public boolean isValid() {
        return valid;
    }


    /**
     * Sets the valid value for this CatalogRuleSyntaxCheck.
     * 
     * @param valid   * Whether the rule text is valid or not.
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }


    /**
     * Gets the warnings value for this CatalogRuleSyntaxCheck.
     * 
     * @return warnings   * Any warnings (such as deprecated functions) associated with
     * the rule text.
     */
    public java.lang.String getWarnings() {
        return warnings;
    }


    /**
     * Sets the warnings value for this CatalogRuleSyntaxCheck.
     * 
     * @param warnings   * Any warnings (such as deprecated functions) associated with
     * the rule text.
     */
    public void setWarnings(java.lang.String warnings) {
        this.warnings = warnings;
    }


    /**
     * Gets the errors value for this CatalogRuleSyntaxCheck.
     * 
     * @return errors   * Any errors (such as syntax errors) associated with the rule
     * text.
     */
    public java.lang.String getErrors() {
        return errors;
    }


    /**
     * Sets the errors value for this CatalogRuleSyntaxCheck.
     * 
     * @param errors   * Any errors (such as syntax errors) associated with the rule
     * text.
     */
    public void setErrors(java.lang.String errors) {
        this.errors = errors;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CatalogRuleSyntaxCheck)) return false;
        CatalogRuleSyntaxCheck other = (CatalogRuleSyntaxCheck) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.valid == other.isValid() &&
            ((this.warnings==null && other.getWarnings()==null) || 
             (this.warnings!=null &&
              this.warnings.equals(other.getWarnings()))) &&
            ((this.errors==null && other.getErrors()==null) || 
             (this.errors!=null &&
              this.errors.equals(other.getErrors())));
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
        _hashCode += (isValid() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getWarnings() != null) {
            _hashCode += getWarnings().hashCode();
        }
        if (getErrors() != null) {
            _hashCode += getErrors().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CatalogRuleSyntaxCheck.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://soap.zeus.com/zxtm/1.0/", "Catalog.Rule.SyntaxCheck"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("valid");
        elemField.setXmlName(new javax.xml.namespace.QName("", "valid"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("warnings");
        elemField.setXmlName(new javax.xml.namespace.QName("", "warnings"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errors");
        elemField.setXmlName(new javax.xml.namespace.QName("", "errors"));
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
