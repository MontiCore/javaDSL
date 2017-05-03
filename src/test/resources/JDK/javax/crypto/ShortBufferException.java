/*
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
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

package javax.crypto;

import java.security.GeneralSecurityException;

/**
 * This exception is thrown when an output buffer provided by the user
 * is too short to hold the operation result.
 *
 * @author Jan Luehe
 *
 * @since 1.4
 */

public class ShortBufferException extends GeneralSecurityException {

    private static final long serialVersionUID = 8427718640832943747L;

    /**
     * Constructs a ShortBufferException with no detail
     * message. A detail message is a String that describes this
     * particular exception.
     */
    public ShortBufferException() {
        super();
    }

    /**
     * Constructs a ShortBufferException with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public ShortBufferException(String msg) {
        super(msg);
    }
}
