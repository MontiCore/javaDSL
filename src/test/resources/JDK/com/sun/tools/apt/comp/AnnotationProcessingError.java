/*
 * Copyright (c) 2004, 2009, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.apt.comp;

public class AnnotationProcessingError extends Error {
    private static final long serialVersionUID = 3279196183341719287L;
    AnnotationProcessingError(Throwable cause) {
        super(cause);
    }
}
