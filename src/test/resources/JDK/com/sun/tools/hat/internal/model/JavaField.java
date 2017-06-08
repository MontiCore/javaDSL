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
 *
 * @author      Bill Foote
 */

public class JavaField {

    private String name;
    private String signature;

    public JavaField(String name, String signature) {
        this.name = name;
        this.signature = signature;
    }


    /**
     * @return true if the type of this field is something that has an ID.
     *          int fields, for exampe, don't.
     */
    public boolean hasId() {
        char ch = signature.charAt(0);
        return (ch == '[' || ch == 'L');
    }

    public String getName() {
        return name;
    }

    public String getSignature() {
        return signature;
    }

}
