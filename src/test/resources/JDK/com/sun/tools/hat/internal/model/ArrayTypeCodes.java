/*
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
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


/*
 * The Original Code is HAT. The Initial Developer of the
 * Original Code is Bill Foote, with contributions from others
 * at JavaSoft/Sun.
 */

package com.sun.tools.hat.internal.model;

/**
 * Primitive array type codes as defined by VM specification.
 *
 */
public interface ArrayTypeCodes {
    // Typecodes for array elements.
    // Refer to newarray instruction in VM Spec.
    public static final int T_BOOLEAN = 4;
    public static final int T_CHAR    = 5;
    public static final int T_FLOAT   = 6;
    public static final int T_DOUBLE  = 7;
    public static final int T_BYTE    = 8;
    public static final int T_SHORT   = 9;
    public static final int T_INT     = 10;
    public static final int T_LONG    = 11;
}
