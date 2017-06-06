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
 * Represents a char (i.e. a char field in an instance).
 *
 * @author      Bill Foote
 */


public class JavaChar extends JavaValue {

    char value;

    public JavaChar(char value) {
        this.value = value;
    }

    public String toString() {
        return "" + value;
    }

}
