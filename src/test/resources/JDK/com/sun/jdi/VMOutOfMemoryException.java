/*
 * Copyright (c) 1999, 2000, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jdi;

/**
 * Thrown to indicate that the requested operation cannot be
 * completed because the target VM has run out of memory.
 *
 * @author Gordon Hirsch
 * @since  1.3
 */
public class VMOutOfMemoryException extends RuntimeException {
    public VMOutOfMemoryException() {
        super();
    }

    public VMOutOfMemoryException(String s) {
        super(s);
    }
}
