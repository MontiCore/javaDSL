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
 * This is used to represent values that the program doesn't really understand.
 * This includes the null vlaue, and unresolved references (which shouldn't
 * happen in well-formed hprof files).
 *
 *
 * @author      Bill Foote
 */




public class HackJavaValue extends JavaValue {

    private String value;
    private int size;

    public HackJavaValue(String value, int size) {
        this.value = value;
        this.size = size;
    }

    public String toString() {
        return value;
    }

    public int getSize() {
        return size;
    }

}
