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

package com.sun.tools.hat.internal.util;

/**
 * Base class for comparison of two objects.
 * @see VectorSorter
 *
 * @author      Bill Foote
 */

abstract public class Comparer {

    /**
     * @return a number <, == or > 0 depending on lhs compared to rhs
     * @see java.lang.String.compareTo
    **/
    abstract public int compare(Object lhs, Object rhs);
}
