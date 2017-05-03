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
 * Indicates that the peer's identity has not been verified.
 * <P>
 * When the peer was not able to
 * identify itself (for example; no certificate, the particular
 * cipher suite being used does not support authentication, or no
 * peer authentication was established during SSL handshaking) this
 * exception is thrown.
 *
 * @since 1.4
 * @author David Brownell
 */
public
class SSLPeerUnverifiedException extends SSLException
{
    private static final long serialVersionUID = -8919512675000600547L;

    /**
     * Constructs an exception reporting that the SSL peer's
     * identity has not been verifiied.
     *
     * @param reason describes the problem.
     */
    public SSLPeerUnverifiedException(String reason)
    {
        super(reason);
    }
}
