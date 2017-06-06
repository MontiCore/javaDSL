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


package javax.net.ssl;

/**
 * Reports an error in the operation of the SSL protocol.  Normally
 * this indicates a flaw in one of the protocol implementations.
 *
 * @since 1.4
 * @author David Brownell
 */
public
class SSLProtocolException extends SSLException
{
    private static final long serialVersionUID = 5445067063799134928L;

    /**
     * Constructs an exception reporting an SSL protocol error
     * detected by an SSL subsystem.
     *
     * @param reason describes the problem.
     */
    public SSLProtocolException(String reason)
    {
        super(reason);
    }
}
