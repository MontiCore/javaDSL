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
 * Represents a chain of references to some target object
 *
 * @author      Bill Foote
 */

public class ReferenceChain {

    JavaHeapObject      obj;    // Object referred to
    ReferenceChain      next;   // Next in chain

    public ReferenceChain(JavaHeapObject obj, ReferenceChain next) {
        this.obj = obj;
        this.next = next;
    }

    public JavaHeapObject getObj() {
        return obj;
    }

    public ReferenceChain getNext() {
        return next;
    }

    public int getDepth() {
        int count = 1;
        ReferenceChain tmp = next;
        while (tmp != null) {
            count++;
            tmp = tmp.next;
        }
        return count;
    }

}
