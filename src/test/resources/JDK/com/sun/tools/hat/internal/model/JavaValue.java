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
 * Abstract base class for all value types (ints, longs, floats, etc.)
 *
 * @author      Bill Foote
 */




public abstract class JavaValue extends JavaThing {

    protected JavaValue() {
    }

    public boolean isHeapAllocated() {
        return false;
    }

    abstract public String toString();

    public int getSize() {
        // The size of a value is already accounted for in the class
        // that has the data member.
        return 0;
    }

}
