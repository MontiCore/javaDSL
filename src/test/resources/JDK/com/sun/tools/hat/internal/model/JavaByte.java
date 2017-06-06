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
 * Represents an byte (i.e. a byte field in an instance).
 *
 * @author      Bill Foote
 */


public class JavaByte extends JavaValue {

    byte value;

    public JavaByte(byte value) {
        this.value = value;
    }

    public String toString() {
        return "0x" + Integer.toString(((int) value) & 0xff, 16);
    }

}
