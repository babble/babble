/**
 * PoolPort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.zeus.soap.zxtm._1_0;

public interface PoolPort extends java.rmi.Remote {

    /**
     * Add nodes to the lists of draining nodes, for each of the named
     * pools.
     */
    public void addDrainingNodes(java.lang.String[] names, java.lang.String[][] values) throws java.rmi.RemoteException;

    /**
     * Add monitors to each of the named pools.
     */
    public void addMonitors(java.lang.String[] names, java.lang.String[][] values) throws java.rmi.RemoteException;

    /**
     * Add new nodes to each of the named pools.
     */
    public void addNodes(java.lang.String[] names, java.lang.String[][] values) throws java.rmi.RemoteException;

    /**
     * Add each of the named pools, using the node lists for each.
     */
    public void addPool(java.lang.String[] names, java.lang.String[][] nodes) throws java.rmi.RemoteException;

    /**
     * Copy each of the named pools.
     */
    public void copyPool(java.lang.String[] names, java.lang.String[] new_names) throws java.rmi.RemoteException;

    /**
     * Delete each of the named pools.
     */
    public void deletePool(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the Bandwidth Classes that each of the named pools uses.
     */
    public java.lang.String[] getBandwidthClass(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the lists of draining nodes for each of the named pools.
     */
    public java.lang.String[][] getDrainingNodes(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the file names of the error texts that each of the named
     * pools will send back to a client when no nodes are available to handle
     * a request.
     */
    public java.lang.String[] getErrorFile(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the pool to use when all nodes in a pool fail, for each
     * of the named pools.
     */
    public java.lang.String[] getFailpool(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get whether each of the named pools should maintain HTTP keepalive
     * connections to the nodes.
     */
    public boolean[] getKeepalive(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the load balancing algorithms that each of the named pools
     * uses.
     */
    public com.zeus.soap.zxtm._1_0.PoolLoadBalancingAlgorithm[] getLoadBalancingAlgorithm(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the times that each of the named pools should wait for
     * a connection to establish to a node before trying another node, in
     * seconds.
     */
    public org.apache.axis.types.UnsignedInt[] getMaxConnectTime(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the maximum numbers of unused HTTP keepalive connections
     * that each of the named pools should maintain to an individual node.
     */
    public org.apache.axis.types.UnsignedInt[] getMaxKeepalivesPerNode(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the time that each of the named pools should wait for a
     * response from a node before either discarding the request or trying
     * another node, in seconds (retryable requests only).
     */
    public org.apache.axis.types.UnsignedInt[] getMaxReplyTime(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the list of all monitors.
     */
    public java.lang.String[][] getMonitors(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the lists of nodes for each of the named pools.
     */
    public java.lang.String[][] getNodes(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the number of active connections to each of the specified
     * nodes.
     */
    public int[] getNodesConnectionCounts(java.lang.String[] nodes) throws java.rmi.RemoteException;

    /**
     * Get the number of seconds since each of the specified nodes
     * was last used.
     */
    public int[] getNodesLastUsed(java.lang.String[] nodes) throws java.rmi.RemoteException;

    /**
     * For each of the named pools, get the priority values for the
     * named nodes in each pool.
     */
    public com.zeus.soap.zxtm._1_0.PoolPriorityValueDefinition[][] getNodesPriorityValue(java.lang.String[] names, java.lang.String[][] nodes) throws java.rmi.RemoteException;

    /**
     * For each of the named pools, get the weighting values for the
     * specified nodes in this pool.
     */
    public com.zeus.soap.zxtm._1_0.PoolWeightingsDefinition[][] getNodesWeightings(java.lang.String[] names, java.lang.String[][] nodes) throws java.rmi.RemoteException;

    /**
     * Get whether this pool uses passive monitoring.
     */
    public boolean[] getPassiveMonitoring(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the default Session Persistence classes that each of the
     * named pools uses.
     */
    public java.lang.String[] getPersistence(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the names of all of the configured pools.
     */
    public java.lang.String[] getPoolNames() throws java.rmi.RemoteException;

    /**
     * Get whether each of the named pools uses priority lists.
     */
    public boolean[] getPriorityEnabled(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get the minimum number of highest-priority active nodes, for
     * each of the named pools.
     */
    public org.apache.axis.types.UnsignedInt[] getPriorityNodes(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * For each of the named pools, get the priority values for each
     * of the nodes in each pool.
     */
    public com.zeus.soap.zxtm._1_0.PoolPriorityValueDefinition[][] getPriorityValues(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get whether each of the named pools should upgrade SMTP connections
     * to SSL using STARTTLS (the alternative is to encrypt the entire connection).
     */
    public boolean[] getSMTPSendStartTLS(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get whether each of the named pools should use client authentication.
     * If client authentication is enabled and a back-end node asks for a
     * client authentication, a suitable certificate and private key will
     * be used from the SSL Client Certificates catalog.
     */
    public boolean[] getSSLClientAuth(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get whether each of the named pools should encrypt data to
     * the back-end nodes using SSL.
     */
    public boolean[] getSSLEncrypt(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get whether each of the named pools should use SSL protocol
     * enhancements. These enhancements allow Zeus Web Servers to run multiple
     * SSL sites, and to discover the client's IP address. Only use enable
     * this if you are using Zeus Web Servers for this pool.
     */
    public boolean[] getSSLEnhance(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get whether each of the named pools should perform strict certificate
     * validation on SSL certificates from the back-end nodes.
     */
    public boolean[] getSSLStrictVerify(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Get whether each of the named pools should make connections
     * to the back-ends appear to originate from the source client IP address.
     */
    public boolean[] getTransparent(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * For each of the named pools, get the weightings for each of
     * the nodes in each pool.
     */
    public com.zeus.soap.zxtm._1_0.PoolWeightingsDefinition[][] getWeightings(java.lang.String[] names) throws java.rmi.RemoteException;

    /**
     * Remove nodes from the lists of draining nodes, for each of
     * the named pools.
     */
    public void removeDrainingNodes(java.lang.String[] names, java.lang.String[][] values) throws java.rmi.RemoteException;

    /**
     * Remove monitors from each of the named pools.
     */
    public void removeMonitors(java.lang.String[] names, java.lang.String[][] values) throws java.rmi.RemoteException;

    /**
     * Remove nodes from each of the named pools.
     */
    public void removeNodes(java.lang.String[] names, java.lang.String[][] values) throws java.rmi.RemoteException;

    /**
     * Rename each of the named pools.
     */
    public void renamePool(java.lang.String[] names, java.lang.String[] new_names) throws java.rmi.RemoteException;

    /**
     * Set the Bandwidth Classes that each of the named pools uses.
     */
    public void setBandwidthClass(java.lang.String[] names, java.lang.String[] values) throws java.rmi.RemoteException;

    /**
     * Set the lists of draining nodes for each of the named pools.
     */
    public void setDrainingNodes(java.lang.String[] names, java.lang.String[][] values) throws java.rmi.RemoteException;

    /**
     * Set the file names of the error texts that each of the named
     * pools will send back to a client when no nodes are available to handle
     * a request.
     */
    public void setErrorFile(java.lang.String[] names, java.lang.String[] values) throws java.rmi.RemoteException;

    /**
     * Set the pool to use when all nodes in a pool fail, for each
     * of the named pools.
     */
    public void setFailpool(java.lang.String[] names, java.lang.String[] values) throws java.rmi.RemoteException;

    /**
     * Set whether each of the named pools should maintain HTTP keepalive
     * connections to the nodes.
     */
    public void setKeepalive(java.lang.String[] names, boolean[] values) throws java.rmi.RemoteException;

    /**
     * Set the load balancing algorithms that each of the named pools
     * uses.
     */
    public void setLoadBalancingAlgorithm(java.lang.String[] names, com.zeus.soap.zxtm._1_0.PoolLoadBalancingAlgorithm[] values) throws java.rmi.RemoteException;

    /**
     * Set the times that each of the named pools should wait for
     * a connection to establish to a node before trying another node, in
     * seconds.
     */
    public void setMaxConnectTime(java.lang.String[] names, org.apache.axis.types.UnsignedInt[] values) throws java.rmi.RemoteException;

    /**
     * Set the maximum numbers of unused HTTP keepalive connections
     * that each of the named pools should maintain to an individual node.
     */
    public void setMaxKeepalivesPerNode(java.lang.String[] names, org.apache.axis.types.UnsignedInt[] values) throws java.rmi.RemoteException;

    /**
     * Set the time that each of the named pools should wait for a
     * response from a node before either discarding the request or trying
     * another node, in seconds (retryable requests only).
     */
    public void setMaxReplyTime(java.lang.String[] names, org.apache.axis.types.UnsignedInt[] values) throws java.rmi.RemoteException;

    /**
     * Set the list of all monitors.
     */
    public void setMonitors(java.lang.String[] names, java.lang.String[][] values) throws java.rmi.RemoteException;

    /**
     * Set the lists of nodes for each of the named pools.
     */
    public void setNodes(java.lang.String[] names, java.lang.String[][] values) throws java.rmi.RemoteException;

    /**
     * For each of the named pools, set the priority values for the
     * named nodes in each pool.
     */
    public void setNodesPriorityValue(java.lang.String[] names, com.zeus.soap.zxtm._1_0.PoolPriorityValueDefinition[][] node_values) throws java.rmi.RemoteException;

    /**
     * For each of the named pools, set the weighting (for the Weighted
     * Round Robin algorithm) for each node in that pool.
     */
    public void setNodesWeightings(java.lang.String[] names, com.zeus.soap.zxtm._1_0.PoolWeightingsDefinition[][] nodes_values) throws java.rmi.RemoteException;

    /**
     * Set whether this pool uses passive monitoring.
     */
    public void setPassiveMonitoring(java.lang.String[] names, boolean[] values) throws java.rmi.RemoteException;

    /**
     * Set the default Session Persistence classes that each of the
     * named pools uses.
     */
    public void setPersistence(java.lang.String[] names, java.lang.String[] values) throws java.rmi.RemoteException;

    /**
     * Set whether each of the named pools uses priority lists.
     */
    public void setPriorityEnabled(java.lang.String[] names, boolean[] values) throws java.rmi.RemoteException;

    /**
     * Set the minimum number of highest-priority active nodes, for
     * each of the named pools.
     */
    public void setPriorityNodes(java.lang.String[] names, org.apache.axis.types.UnsignedInt[] values) throws java.rmi.RemoteException;

    /**
     * Set whether each of the named pools should upgrade SMTP connections
     * to SSL using STARTTLS (the alternative is to encrypt the entire connection).
     */
    public void setSMTPSendStartTLS(java.lang.String[] names, boolean[] values) throws java.rmi.RemoteException;

    /**
     * Set whether each of the named pools should use client authentication.
     * If client authentication is enabled and a back-end node asks for a
     * client authentication, a suitable certificate and private key will
     * be used from the SSL Client Certificates catalog.
     */
    public void setSSLClientAuth(java.lang.String[] names, boolean[] values) throws java.rmi.RemoteException;

    /**
     * Set whether each of the named pools should encrypt data to
     * the back-end nodes using SSL.
     */
    public void setSSLEncrypt(java.lang.String[] names, boolean[] values) throws java.rmi.RemoteException;

    /**
     * Set whether each of the named pools should use SSL protocol
     * enhancements. These enhancements allow Zeus Web Servers to run multiple
     * SSL sites, and to discover the client's IP address. Only use enable
     * this if you are using Zeus Web Servers for this pool.
     */
    public void setSSLEnhance(java.lang.String[] names, boolean[] values) throws java.rmi.RemoteException;

    /**
     * Set whether each of the named pools should perform strict certificate
     * validation on SSL certificates from the back-end nodes.
     */
    public void setSSLStrictVerify(java.lang.String[] names, boolean[] values) throws java.rmi.RemoteException;

    /**
     * Set whether each of the named pools should make connections
     * to the back-ends appear to originate from the source client IP address.
     */
    public void setTransparent(java.lang.String[] names, boolean[] values) throws java.rmi.RemoteException;
}
