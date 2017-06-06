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
 * A visitor for a JavaThing.  @see JavaObject#visitReferencedObjects()
 *
 */


abstract public class AbstractJavaHeapObjectVisitor
                implements JavaHeapObjectVisitor {
    abstract public void visit(JavaHeapObject other);

    /**
     * Should the given field be excluded from the set of things visited?
     * @return true if it should.
     */
    public boolean exclude(JavaClass clazz, JavaField f) {
        return false;
    }

    /**
     * @return true iff exclude might ever return true
     */
    public boolean mightExclude() {
        return false;
    }

}
