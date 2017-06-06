/*
 * Copyright (c) 1998, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jdi.request;

/**
 * Thrown to indicate a duplicate event request.
 *
 * @author Robert Field
 * @since  1.3
 */
public class DuplicateRequestException extends RuntimeException
{
    public DuplicateRequestException()
    {
        super();
    }

    public DuplicateRequestException(String s)
    {
        super(s);
    }
}
