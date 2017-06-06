/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */


package sun.security.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;

import java.security.AlgorithmConstraints;

import java.util.*;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLParameters;


/**
 * This class provides a simple way for servers to support conventional
 * use of the Secure Sockets Layer (SSL).  Application code uses an
 * SSLServerSocketImpl exactly like it uses a regular TCP ServerSocket; the
 * difference is that the connections established are secured using SSL.
 *
 * <P> Also, the constructors take an explicit authentication context
 * parameter, giving flexibility with respect to how the server socket
 * authenticates itself.  That policy flexibility is not exposed through
 * the standard SSLServerSocketFactory API.
 *
 * <P> System security defaults prevent server sockets from accepting
 * connections if they the authentication context has not been given
 * a certificate chain and its matching private key.  If the clients
 * of your application support "anonymous" cipher suites, you may be
 * able to configure a server socket to accept those suites.
 *
 * @see SSLSocketImpl
 * @see SSLServerSocketFactoryImpl
 *
 * @author David Brownell
 */
final
class SSLServerSocketImpl extends SSLServerSocket
{
    private SSLContextImpl      sslContext;

    /* Do newly accepted connections require clients to authenticate? */
    private byte                doClientAuth = SSLEngineImpl.clauth_none;

    /* Do new connections created here use the "server" mode of SSL? */
    private boolean             useServerMode = true;

    /* Can new connections created establish new sessions? */
    private boolean             enableSessionCreation = true;

    /* what cipher suites to use by default */
    private CipherSuiteList     enabledCipherSuites = null;

    /* which protocol to use by default */
    private ProtocolList        enabledProtocols = null;

    /* could enabledCipherSuites ever complete handshaking? */
    private boolean             checkedEnabled = false;

    // the endpoint identification protocol to use by default
    private String              identificationProtocol = null;

    // The cryptographic algorithm constraints
    private AlgorithmConstraints    algorithmConstraints = null;

    /**
     * Create an SSL server socket on a port, using a non-default
     * authentication context and a specified connection backlog.
     *
     * @param port the port on which to listen
     * @param backlog how many connections may be pending before
     *          the system should start rejecting new requests
     * @param context authentication context for this server
     */
    SSLServerSocketImpl(int port, int backlog, SSLContextImpl context)
    throws IOException, SSLException
    {
        super(port, backlog);
        initServer(context);
    }


    /**
     * Create an SSL server socket on a port, using a specified
     * authentication context and a specified backlog of connections
     * as well as a particular specified network interface.  This
     * constructor is used on multihomed hosts, such as those used
     * for firewalls or as routers, to control through which interface
     * a network service is provided.
     *
     * @param port the port on which to listen
     * @param backlog how many connections may be pending before
     *          the system should start rejecting new requests
     * @param address the address of the network interface through
     *          which connections will be accepted
     * @param context authentication context for this server
     */
    SSLServerSocketImpl(
        int             port,
        int             backlog,
        InetAddress     address,
        SSLContextImpl  context)
        throws IOException
    {
        super(port, backlog, address);
        initServer(context);
    }


    /**
     * Creates an unbound server socket.
     */
    SSLServerSocketImpl(SSLContextImpl context) throws IOException {
        super();
        initServer(context);
    }


    /**
     * Initializes the server socket.
     */
    private void initServer(SSLContextImpl context) throws SSLException {
        if (context == null) {
            throw new SSLException("No Authentication context given");
        }
        sslContext = context;
        enabledCipherSuites = sslContext.getDefaultCipherSuiteList(true);
        enabledProtocols = sslContext.getDefaultProtocolList(true);
    }

    /**
     * Returns the names of the cipher suites which could be enabled for use
     * on an SSL connection.  Normally, only a subset of these will actually
     * be enabled by default, since this list may include cipher suites which
     * do not support the mutual authentication of servers and clients, or
     * which do not protect data confidentiality.  Servers may also need
     * certain kinds of certificates to use certain cipher suites.
     *
     * @return an array of cipher suite names
     */
    public String[] getSupportedCipherSuites() {
        return sslContext.getSuportedCipherSuiteList().toStringArray();
    }

    /**
     * Returns the list of cipher suites which are currently enabled
     * for use by newly accepted connections.  A null return indicates
     * that the system defaults are in effect.
     */
    synchronized public String[] getEnabledCipherSuites() {
        return enabledCipherSuites.toStringArray();
    }

    /**
     * Controls which particular SSL cipher suites are enabled for use
     * by accepted connections.
     *
     * @param suites Names of all the cipher suites to enable; null
     *  means to accept system defaults.
     */
    synchronized public void setEnabledCipherSuites(String[] suites) {
        enabledCipherSuites = new CipherSuiteList(suites);
        checkedEnabled = false;
    }

    public String[] getSupportedProtocols() {
        return sslContext.getSuportedProtocolList().toStringArray();
    }

    /**
     * Controls which protocols are enabled for use.
     * The protocols must have been listed by
     * getSupportedProtocols() as being supported.
     *
     * @param protocols protocols to enable.
     * @exception IllegalArgumentException when one of the protocols
     *  named by the parameter is not supported.
     */
    synchronized public void setEnabledProtocols(String[] protocols) {
        enabledProtocols = new ProtocolList(protocols);
    }

    synchronized public String[] getEnabledProtocols() {
        return enabledProtocols.toStringArray();
    }

    /**
     * Controls whether the connections which are accepted must include
     * client authentication.
     */
    public void setNeedClientAuth(boolean flag) {
        doClientAuth = (flag ?
            SSLEngineImpl.clauth_required : SSLEngineImpl.clauth_none);
    }

    public boolean getNeedClientAuth() {
        return (doClientAuth == SSLEngineImpl.clauth_required);
    }

    /**
     * Controls whether the connections which are accepted should request
     * client authentication.
     */
    public void setWantClientAuth(boolean flag) {
        doClientAuth = (flag ?
            SSLEngineImpl.clauth_requested : SSLEngineImpl.clauth_none);
    }

    public boolean getWantClientAuth() {
        return (doClientAuth == SSLEngineImpl.clauth_requested);
    }

    /**
     * Makes the returned sockets act in SSL "client" mode, not the usual
     * server mode.  The canonical example of why this is needed is for
     * FTP clients, which accept connections from servers and should be
     * rejoining the already-negotiated SSL connection.
     */
    public void setUseClientMode(boolean flag) {
        /*
         * If we need to change the socket mode and the enabled
         * protocols haven't specifically been set by the user,
         * change them to the corresponding default ones.
         */
        if (useServerMode != (!flag) &&
                sslContext.isDefaultProtocolList(enabledProtocols)) {
            enabledProtocols = sslContext.getDefaultProtocolList(!flag);
        }

        useServerMode = !flag;
    }

    public boolean getUseClientMode() {
        return !useServerMode;
    }


    /**
     * Controls whether new connections may cause creation of new SSL
     * sessions.
     */
    public void setEnableSessionCreation(boolean flag) {
        enableSessionCreation = flag;
    }

    /**
     * Returns true if new connections may cause creation of new SSL
     * sessions.
     */
    public boolean getEnableSessionCreation() {
        return enableSessionCreation;
    }

    /**
     * Returns the SSLParameters in effect for newly accepted connections.
     */
    synchronized public SSLParameters getSSLParameters() {
        SSLParameters params = super.getSSLParameters();

        // the super implementation does not handle the following parameters
        params.setEndpointIdentificationAlgorithm(identificationProtocol);
        params.setAlgorithmConstraints(algorithmConstraints);

        return params;
    }

    /**
     * Applies SSLParameters to newly accepted connections.
     */
    synchronized public void setSSLParameters(SSLParameters params) {
        super.setSSLParameters(params);

        // the super implementation does not handle the following parameters
        identificationProtocol = params.getEndpointIdentificationAlgorithm();
        algorithmConstraints = params.getAlgorithmConstraints();
    }

    /**
     * Accept a new SSL connection.  This server identifies itself with
     * information provided in the authentication context which was
     * presented during construction.
     */
    public Socket accept() throws IOException {
        SSLSocketImpl s = new SSLSocketImpl(sslContext, useServerMode,
            enabledCipherSuites, doClientAuth, enableSessionCreation,
            enabledProtocols, identificationProtocol, algorithmConstraints);

        implAccept(s);
        s.doneConnect();
        return s;
    }

    /**
     * Provides a brief description of this SSL socket.
     */
    public String toString() {
        return "[SSL: "+ super.toString() + "]";
    }
}
