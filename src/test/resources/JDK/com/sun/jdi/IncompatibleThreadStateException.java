/*
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
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
 * completed while the specified thread is in its current state.
 *
 * @author Gordon Hirsch
 * @since  1.3
 */
public class IncompatibleThreadStateException extends Exception
{
    public IncompatibleThreadStateException()
    {
        super();
    }

    public IncompatibleThreadStateException(String s)
    {
        super(s);
    }
}
