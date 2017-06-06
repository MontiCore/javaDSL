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
 * Represents an integer (i.e. an int field in an instance).
 *
 * @author      Bill Foote
 */


public class JavaInt extends JavaValue {

    int value;

    public JavaInt(int value) {
        this.value = value;
    }

    public String toString() {
        return "" + value;
    }

}
