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
 * This represents a set of data members that should be excluded from the
 * reachable objects query. This is useful to exclude observers from the
 * transitive closure of objects reachable from a given object, allowing
 * some kind of real determination of the "size" of that object.
 *
 */

public interface ReachableExcludes {
    /**
     * @return true iff the given field is on the hitlist of excluded
     *          fields.
     */
    public boolean isExcluded(String fieldName);
}
