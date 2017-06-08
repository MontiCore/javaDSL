/*
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;

/**
 * Indicates some kind of error detected by an SSL subsystem.
 * This class is the general class of exceptions produced
 * by failed SSL-related operations.
 *
 * @since 1.4
 * @author David Brownell
 */
public
class SSLException extends IOException
{
    private static final long serialVersionUID = 4511006460650708967L;

    /**
     * Constructs an exception reporting an error found by
     * an SSL subsystem.
     *
     * @param reason describes the problem.
     */
    public SSLException(String reason)
    {
        super(reason);
    }

    /**
     * Creates a <code>SSLException</code> with the specified
     * detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval
     *          by the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *          {@link #getCause()} method).  (A <tt>null</tt> value is
     *          permitted, and indicates that the cause is nonexistent or
     *          unknown.)
     * @since 1.5
     */
    public SSLException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    /**
     * Creates a <code>SSLException</code> with the specified cause
     * and a detail message of <tt>(cause==null ? null : cause.toString())</tt>
     * (which typically contains the class and detail message of
     * <tt>cause</tt>).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *          {@link #getCause()} method).  (A <tt>null</tt> value is
     *          permitted, and indicates that the cause is nonexistent or
     *          unknown.)
     * @since 1.5
     */
    public SSLException(Throwable cause) {
        super(cause == null ? null : cause.toString());
        initCause(cause);
    }
}
