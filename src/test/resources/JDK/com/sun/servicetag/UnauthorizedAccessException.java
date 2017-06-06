/*
 * Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.servicetag;

/**
 * Thrown if the user is not authorized to
 * {@link Registry#updateServiceTag update} or
 * {@link Registry#removeServiceTag remove}
 * a service tag from a {@link Registry}.
 */
public class UnauthorizedAccessException extends RuntimeException {

    /**
     * Constructs an <code>UnauthorizedAccessException</code> object
     * without detail message.
     */
    public UnauthorizedAccessException() {
    }


    /**
     * Constructs an <code>UnauthorizedAccessException</code> object
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UnauthorizedAccessException(String msg) {
        super(msg);
    }
}
