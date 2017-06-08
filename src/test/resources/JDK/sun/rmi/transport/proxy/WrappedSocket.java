/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
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
package sun.rmi.transport.proxy;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * The WrappedSocket class provides a general wrapper for providing an
 * extended implementation of java.net.Socket that can be attached to
 * a pre-existing Socket object.  WrappedSocket itself provides a
 * constructor for specifying alternate input or output streams to be
 * returned than those of the underlying Socket.
 */
class WrappedSocket extends Socket {

    /** the underlying concrete socket */
    protected Socket socket;

    /** the input stream to return for socket */
    protected InputStream in = null;

    /** the output stream to return for socket */
    protected OutputStream out = null;

    /**
     * Layer on top of a pre-existing Socket object, and use specified
     * input and output streams.  This allows the creator of the
     * underlying socket to peek at the beginning of the input with a
     * BufferedInputStream and determine which kind of socket
     * to create, without consuming the input.
     * @param socket the pre-existing socket to use
     * @param in the InputStream to return to users (can be null)
     * @param out the OutputStream to return to users (can be null)
     */
    public WrappedSocket(Socket socket, InputStream in, OutputStream out)
        throws IOException
    {
        super((java.net.SocketImpl)null);       // no underlying SocketImpl for this object
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    /**
     * Get the address to which the socket is connected.
     */
    public InetAddress getInetAddress()
    {
        return socket.getInetAddress();
    }

    /**
     * Get the local address to which the socket is bound.
     */
    public InetAddress getLocalAddress() {
        return socket.getLocalAddress();
    }

    /**
     * Get the remote port to which the socket is connected.
     */
    public int getPort()
    {
        return socket.getPort();
    }

    /**
     * Get the local port to which the socket is connected.
     */
    public int getLocalPort()
    {
        return socket.getLocalPort();
    }

    /**
     * Get an InputStream for this socket.
     */
    public InputStream getInputStream() throws IOException
    {
        if (in == null)
            in = socket.getInputStream();
        return in;
    }

    /**
     * Get an OutputStream for this socket.
     */
    public OutputStream getOutputStream() throws IOException
    {
        if (out == null)
            out = socket.getOutputStream();
        return out;
    }

    /**
     * Enable/disable TCP_NODELAY.
     */
    public void setTcpNoDelay(boolean on) throws SocketException
    {
        socket.setTcpNoDelay(on);
    }

    /**
     * Retrieve whether TCP_NODELAY is enabled.
     */
    public boolean getTcpNoDelay() throws SocketException
    {
        return socket.getTcpNoDelay();
    }

    /**
     * Enable/disable SO_LINGER with the specified linger time.
     */
    public void setSoLinger(boolean on, int val) throws SocketException
    {
        socket.setSoLinger(on, val);
    }

    /**
     * Retrive setting for SO_LINGER.
     */
    public int getSoLinger() throws SocketException
    {
        return socket.getSoLinger();
    }

    /**
     * Enable/disable SO_TIMEOUT with the specified timeout
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException
    {
        socket.setSoTimeout(timeout);
    }

    /**
     * Retrive setting for SO_TIMEOUT.
     */
    public synchronized int getSoTimeout() throws SocketException
    {
        return socket.getSoTimeout();
    }

    /**
     * Close the socket.
     */
    public synchronized void close() throws IOException
    {
        socket.close();
    }

    /**
     * Return string representation of the socket.
     */
    public String toString()
    {
        return "Wrapped" + socket.toString();
    }
}
