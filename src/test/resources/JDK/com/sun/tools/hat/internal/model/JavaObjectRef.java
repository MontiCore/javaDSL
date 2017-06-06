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

import com.sun.tools.hat.internal.util.Misc;

/**
 * A forward reference to an object.  This is an intermediate representation
 * for a JavaThing, when we have the thing's ID, but we might not have read
 * the thing yet.
 *
 * @author      Bill Foote
 */
public class JavaObjectRef extends JavaThing {
    private long id;

    public JavaObjectRef(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public boolean isHeapAllocated() {
        return true;
    }

    public JavaThing dereference(Snapshot snapshot, JavaField field) {
        return dereference(snapshot, field, true);
    }

    public JavaThing dereference(Snapshot snapshot, JavaField field, boolean verbose) {
        if (field != null && !field.hasId()) {
            // If this happens, we must be a field that represents an int.
            // (This only happens with .bod-style files)
            return new JavaLong(id);
        }
        if (id == 0) {
            return snapshot.getNullThing();
        }
        JavaThing result = snapshot.findThing(id);
        if (result == null) {
            if (!snapshot.getUnresolvedObjectsOK() && verbose) {
                String msg = "WARNING:  Failed to resolve object id "
                                + Misc.toHex(id);
                if (field != null) {
                    msg += " for field " + field.getName()
                            + " (signature " + field.getSignature() + ")";
                }
                System.out.println(msg);
                // Thread.dumpStack();
            }
            result = new HackJavaValue("Unresolved object "
                                        + Misc.toHex(id), 0);
        }
        return result;
    }

    public int getSize() {
        return 0;
    }

    public String toString() {
        return "Unresolved object " + Misc.toHex(id);
    }
}
