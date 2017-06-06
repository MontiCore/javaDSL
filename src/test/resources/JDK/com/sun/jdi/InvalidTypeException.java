/*
 * Copyright (c) 1998, 2005, Oracle and/or its affiliates. All rights reserved.
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
 * Thrown to indicate a type mismatch in setting the value of a field
 * or variable, or in specifying the return value of a method.
 *
 * @author James McIlree
 * @since  1.3
 */
public class InvalidTypeException extends Exception
{
    public InvalidTypeException()
    {
        super();
    }

    public InvalidTypeException(String s)
    {
        super(s);
    }
}
