/**
 * CatalogRulePort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.zeus.soap.zxtm._1_0;

public interface CatalogRulePort extends java.rmi.Remote {

    /**
     * Add new rules to the catalog.
     */
    public void addRule(java.lang.String[] names, java.lang.String[] texts) throws java.rmi.RemoteException;

    /**
     * Check the syntax of each of the supplied trafficscript rule
     * texts.  This method does not modify any configuration.
     */
    public com.zeus.soap.zxtm._1_0.CatalogRuleSyntaxCheck[] checkSyntax(java.lang.String[] rule_text) throws java.rmi.RemoteException;

    /**
     * Copy the named rules in the catalog.
     */
    public void copyRule(java.lang.String[] names, java.lang.String[] new_names) throws java.rmi.RemoteException;

    /**
     * Delete the named rules from the catalog.
     */
    public void deleteRule(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the rule text and notes (if any), for each of the named
     * rules.
     */
    public com.zeus.soap.zxtm._1_0.CatalogRuleRuleInfo[] getRuleDetails(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the names of all rules in the catalog.
     */
    public java.lang.String[] getRuleNames() throws java.rmi.RemoteException;

    /**
     * Rename the named rules in the catalog.
     */
    public void renameRule(java.lang.String[] names, java.lang.String[] new_names) throws java.rmi.RemoteException;

    /**
     * Sets the descriptive notes for each of the named rules in the
     * catalog.
     */
    public void setRuleNotes(java.lang.String[] names, java.lang.String[] notes) throws java.rmi.RemoteException;

    /**
     * Set the TrafficScript text for each of the named rules in the
     * catalog.
     */
    public void setRuleText(java.lang.String[] names, java.lang.String[] text) throws java.rmi.RemoteException;
}
