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
 * Represents a double (i.e. a double field in an instance).
 *
 * @author      Bill Foote
 */


public class JavaDouble extends JavaValue {

    double value;

    public JavaDouble(double value) {
        this.value = value;
    }

    public String toString() {
        return Double.toString(value);
    }
}
